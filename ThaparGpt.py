import os
import chromadb
from chromadb import EmbeddingFunction
from dotenv import load_dotenv
from sentence_transformers import SentenceTransformer
# from transformers import AutoTokenizer,AutoModelForCasualLM
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
        return self.model.encode(txt)

class VectorDB(DataLoader):
    def __init__(self):
        super().__init__()
        self.client = chromadb.PersistentClient()
        self.embedder = EmbeddingModel()
        self.collections = {}
        for collection_name in ["thapar_hostels", "thapar_academics", "thapar_activities"]:
            try:
                self.client.delete_collection(collection_name)
            except:
                pass
        self.incollections()   
    def incollections(self):
        
        file_types = {
          "hostels":["Hostel_fees.txt","Hostel_info.txt"],
          "activities":["TIET_Events.txt","clubs_thapar.txt","Thapar_Societies.txt"],
          "academics":["scholarships.txt","PG_Course_Fee.txt"]  
        }
        
        for col_type,files in file_types.items():
            col_name = f"{'thapar_'}{col_type}"
            self.collections[col_type] = self.client.create_collection(name=col_name)
    def chunkData(self,txt,delimiter = "###"):
        return [chunk.strip() for chunk in txt.split(delimiter) if chunk.strip()]
    def  populate_db(self):
        files_data = self.load_files()
        
        for filename,content in files_data.items():
            if 'hostel' in filename.lower():
                col_type ="hostels"
            elif 'scholarship' in filename.lower() or 'Course' in filename.lower():
                col_type = "academics"
            else:
                col_type = "activities"
            
            chunks = self.chunkData(content)
            embeddings = self.embedder.embed(chunks).tolist()
            self.collections[col_type].add(
                documents = chunks,
                embeddings = embeddings,
                ids = [f"{filename}_{i}" for i in range(len(chunks))],
                metadatas =[{"source":filename}]*len(chunks)
            )
    
    def query(self,query,collection_type,top_k=2):
        query_embeddings = self.embedder.embed(query).tolist()
        results = self.collections[collection_type].query(
            query_embeddings = [query_embeddings],
            top_k = top_k
        )
        return results["documents"][0]
class Falcon7B:
    def __init__(self):
        load_dotenv()
        self.LLM = "tiiuae/falcon-7b-instruct"
        self.hf_api_key = os.getenv("HUGGINGFACE_API_KEY")
        if not self.hf_api_key :
            raise ValueError("Check .env file as api key not found")
        self.client = InferenceClient(token=self.hf_api_key)

    
    def generate(self,prompt,max_new_token=100,temprature = 0.7,top_p = 0.9,reptition_penalty = 1.1):
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


class ThaparAssistant(VectorDB,Falcon7B):
    
    def __init__(self):
        VectorDB.__init__(self)
        Falcon7B.__init__(self)
        self.populate_db()
        
    def _determineCollectionType(self,query):
        query_lower =query.lower()
        if any(kw in query_lower for kw in ["hostel","room","mess"]):
            return "hostels"
        elif any(kw in query_lower for kw in ["scholarships","fee","course"]):
            return "academics"
        else:
            return "activities"
    def builPrompt(self,query,context):
        context_str="\n".join(context)
        return f""" Answer question using only this context : {context_str}
    else say I don't know the answer to question:{query} and ask user to update the database. 
    Answer.. """
    
    def ask(self,query):
        try:
            col_type=self._determineCollectionType(query)
            context = self.query(query,col_type)
            prompt = self.builPrompt(query,context)
            response= self.generate(prompt)
            
            return response
        except Exception as e:
            return "Sorry for the inconvience . An error occured."




assistant = ThaparAssistant()
queries = [
        "What are the fees for 2-sharing rooms in Hostel B?",
        "Which coding-related societies exist?",
        "Tell me about MSc scholarships for 8.5 CGPA students"
]

for query in queries:
    print(f"\n{query}")
    print(f"\n{assistant.ask(query)}")