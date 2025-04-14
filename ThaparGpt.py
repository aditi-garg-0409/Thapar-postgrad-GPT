import os
import chromadb
from chromadb import EmbeddingFunction
from dotenv import load_dotenv
from sentence_transformers import SentenceTransformer
from huggingface_hub import InferenceClient


class DataLoader:
    def __init__(self):
        self.data_dir = "Structured_Data"
    def load_files(self):
        data = {}
        for filename in os.listdir(self.data_dir):
            if filename.endswith('.txt'):
                with open(os.path.join(self.data_dir,filename),'r') as f:
                    data[filename]=f.read()
        return data
class EmbeddingModel:
    def __init__(self):
        self.model = SentenceTransformer("all-MiniLM-L6-v2")
    def embed(self,txt):
        if isinstance(txt, str):
            txt = [txt]
        return self.model.encode(txt)

class VectorDB(DataLoader):
    def __init__(self):
        super().__init__()
        self.client = chromadb.PersistentClient()
        self.embedder = EmbeddingModel()
        self.collections = {}
        for collection_name in ["thapar_hostels", "thapar_academics", "thapar_activities","thapar_placements"]:
            try:
                self.client.delete_collection(collection_name)
            except:
                pass
        self.incollections()   
    def incollections(self):
        
        file_types = {
          "hostels":["Hostel_info.txt"],
          "activities":["TIET_Events.txt","clubs_thapar.txt","Thapar_Societies.txt"],
          "academics":["scholarships.txt","Pg_Course_Fee.txt","Pg_Program_Structure.txt"],
          "placements":["Placement_Record.txt"]  
        }
        
        for col_type,files in file_types.items():
            col_name = f"thapar_{col_type}"
            self.collections[col_type] = self.client.create_collection(name=col_name)
    def chunkData(self,txt,delimiter = "###"):
        return [chunk.strip() for chunk in txt.split(delimiter) if chunk.strip()]
    def  populate_db(self):
        files_data = self.load_files()
        
        for filename,content in files_data.items():
            if 'hostel' in filename.lower():
                col_type ="hostels"
            elif 'scholarship' in filename.lower() or 'pg' in filename.lower():
                col_type = "academics"
            elif "placement" in filename.lower():
                col_type ="placements"
            else:
                col_type = "activities"
            
            chunks = self.chunkData(content)
            embeddings = self.embedder.embed(chunks)
            self.collections[col_type].add(
                documents = chunks,
                embeddings = embeddings,
                ids = [f"{filename}_{i}" for i in range(len(chunks))],
                metadatas =[{"source":filename}]*len(chunks)
            )
        # After populate_db(), add verification:
        # print("Collection counts:")
        # for name, col in self.collections.items():
        #     print(f"{name}: {col.count()} items")
    
    def query(self,query,collection_type,top_k=3):
        try:
            query_embeddings = self.embedder.embed([query])
            results = self.collections[collection_type].query(
                query_embeddings = query_embeddings.tolist(),
                n_results = top_k
            )
            return results["documents"][0]
        except Exception as e:
            print(f"\nQuery Errors:{str(e)}")
            raise
class Mixtral:
    def __init__(self):
        load_dotenv()
        self.LLM = "mistralai/Mixtral-8x7B-Instruct-v0.1"
        self.hf_api_key = os.getenv("HUGGINGFACE_API_KEY")
        if not self.hf_api_key :
            raise ValueError("Check .env file as api key not found")
        self.client = InferenceClient(token=self.hf_api_key)

    
    def generate(self,prompt,max_new_token=300,temprature = 0.1,top_p = 0.9,reptition_penalty = 1.5):
        try:
            response = self.client.text_generation(
                prompt=prompt,
                model=self.LLM,
                max_new_tokens= max_new_token,
                temperature= temprature,
                top_p=top_p,
                repetition_penalty=reptition_penalty,
                do_sample=True
            )
            return response.strip()
        except Exception as e:
            raise RuntimeError("Failed to generate any respose check code.")


class ThaparAssistant(VectorDB,Mixtral):
    
    def __init__(self):
        VectorDB.__init__(self)
        Mixtral.__init__(self)
        self.populate_db()
        # print("\nTESTING VECTORDB QUERIES:")
        # test_queries = [
        #     ("hostel fees", "hostels"),
        #     ("coding society", "activities"),
        #     ("scholarship", "academics"),
        #     ("recruiters","placements")
        # ]
        
        # for query, col_type in test_queries:
        #     try:
        #         results = self.query(query, col_type)
        #         print(f"\nQuery: '{query}'")
        #         print(f"Results: {results}")
        #     except Exception as e:
        #         print(f"Query failed: {str(e)}")
        
    def _determineCollectionType(self,query):
        query_lower =query.lower()
        if any(kw in query_lower for kw in ["hostel","room","mess","sharing","hall","accomodation"]):
            return "hostels"
        elif any(kw in query_lower for kw in ["scholarships","fee","course","syllabus","program"]):
            return "academics"
        elif any(kw in query_lower for kw in ["record","package","recruiter","placement"]):
            return "placements"
        else:
            return "activities"
    def build_prompt(self, query, context):
        context_str = "\n\n".join([
            f"CONTEXT {i+1}:\n{text}" 
            for i, text in enumerate(context)
            ])
        return f"""You are a factual assistant for Thapar University, and you must ONLY use the provided context to answer.
Rules:

Respond in EXACTLY ONE SENTENCE.

1.If the answer isn`t in the context, say: 'Information not found in official records.'

2.Use only exact numbers, terms, and currency (₹) from the context—no approximations.

3.Never add, infer, or invent details—strictly verbatim from context.

4.If the query is irrelevant or unanswerable, respond: 'Information not found in official records.'

5.Preserve formatting (e.g., ₹, dates, names) exactly as provided use ₹ instead of $ or Rs. or INR .

6. Replace synonyms.

Context:
{context_str}

User Question: {query}

Answer: """
    
    def ask(self, query):
        try:
            col_type = self._determineCollectionType(query)
            context = self.query(query, col_type)
            print(f"\nRETRIEVED CONTEXT FOR '{query}':")
            for i, text in enumerate(context, 1):
                print(f"[Context {i}]: {text[:200]}...")
            prompt = self.build_prompt(query, context)
            response = self.generate(prompt)
            if "Rs" not in response and any("Rs." in ctx for ctx in context):
                response = "Information not found in records"
            return response
        except Exception as e:
            return f"System error: {str(e)}"



assistant = ThaparAssistant()
queries = [
        "List all boys hostels?",
        "What is the mess fee for agira hall ?",
        "Which coding-related societies exist?",
        "Tell me various events that occur in Thapar?",
        "What is the average package for MTECH CSE in Thapar?",
        "Who are the top recruiters for MCA?",
        "Tell me about MSc scholarships for 8.5 CGPA students"
]


for query in queries:
    print(f"\n{query}")
    print(f"\n{assistant.ask(query)}")

