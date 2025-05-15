import os
import chromadb
from chromadb import EmbeddingFunction
from dotenv import load_dotenv
# from sentence_transformers import SentenceTransformer
# import ollama
from flask import Flask, request, jsonify   
# from pyngrok import ngrok
from flask_cors import CORS
# import threading
import cohere



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
        load_dotenv()
        self.api_key = os.getenv("COHERE_API_KEY")
        self.use_cohere = self.api_key is not None
        self.cohere_client = None
        
        if self.use_cohere:
            try:
                self.cohere_client = cohere.Client(self.api_key)
                # Test the API to ensure it's working
                self.cohere_client.embed(texts=["test"], model="embed-english-v3.0",input_type="search_document")
                print("[EmbeddingModel] Using Cohere API for embeddings.")
            except Exception as e:
                print(f"[EmbeddingModel] Failed to use Cohere API. Falling back to local model. Reason: {str(e)}")
                self._load_local_model()
        else:
            print("[EmbeddingModel] COHERE_API_KEY not found. Using local model.")
            self._load_local_model()

    def _load_local_model(self):
        self.use_cohere = False
        self.local_model = SentenceTransformer("intfloat/e5-small-v2")

    def embed(self, txt):
        if isinstance(txt, str):
            txt = [txt]
        try:
            if self.use_cohere and self.cohere_client:
                response = self.cohere_client.embed(
                    texts=txt,
                    model="embed-english-v3.0"
                )
                return response.embeddings
            else:
                return self.local_model.encode(txt)
        except Exception as e:
            print(f"[EmbeddingModel] Error during embedding. Fallback to local. Error: {str(e)}")
            return self.local_model.encode(txt)


class VectorDB(DataLoader):
    def __init__(self):
        super().__init__()
        self.client = chromadb.Client()
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

import requests

class Mixtral:
    def __init__(self):
        load_dotenv()
        self.GROQ_API_KEY = os.getenv("GROQ_API_KEY")
        self.api_url = "https://api.groq.com/openai/v1/chat/completions"
        self.headers = {
            "Authorization": f"Bearer {self.GROQ_API_KEY}",
            "Content-Type": "application/json"
        }

    def generate(self, prompt, max_new_token=500, temperature=0.1, top_p=0.9):
        try:
            payload = {
                "model": "mistral-saba-24b",
                "messages": [
                    {"role": "system", "content": "You are ThaparGPT. Answer like a helpful university assistant."},
                    {"role": "user", "content": prompt}
                ],
                "temperature": temperature,
                "top_p": top_p,
                "max_tokens": max_new_token
            }

            response = requests.post(
                self.api_url, headers=self.headers, json=payload, timeout=10
            )
            response.raise_for_status()
            return response.json()["choices"][0]["message"]["content"].strip()

        except Exception as e:
            print(f"[Groq API ERROR]: {str(e)}")
            raise RuntimeError("Groq API call failed. Check your API key or prompt formatting.")

class ThaparAssistant(VectorDB, Mixtral):
    
    def __init__(self):
        VectorDB.__init__(self)
        Mixtral.__init__(self)
        self.populate_db()
        
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
        return f"""You are a helpful assistant for Thapar University with two response modes:

# When context exists:
1. STRICTLY prioritize the provided official context
2. Use exact figures/terms (₹ instead of Rs/INR)
3. Format lists with bullet points
4. For numerical queries, provide exact values only
5. Never hallucinate details - say "Not specified in official records" if unsure

# For general queries:
1. Use your knowledge about Indian universities
2. Clearly mark non-context answers with [General Knowledge]
3. For comparisons, maintain neutrality
4. When estimating, disclose it's an approximation

# For greetings like hello , hi , bye , thanks and thank you:
Greet the user.

Current Context:
{context_str if context else 'No specific context provided'}

User Question: {query}

First determine if this is:
A) A specific factual query about Thapar (use context)
B) A general higher-education question (wider knowledge)
C) Administrative (dates/processes - be precise)

Then provide a 1-2 sentence response accordingly, using the format:
[Response Type]: [Your answer]"""
    
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

# Create Flask app to serve the assistant
app = Flask(__name__)
# CORS(app,origins=["https://thapargptweb.onrender.com"],supports_credentials=True,headers=["Content-Type"])
CORS(app)
assistant = ThaparAssistant()

@app.route('/api/ask', methods=['POST','OPTIONS'])
def api_ask():
    if request.method == "OPTIONS":
        return jsonify({
            'status':'ok'
        })
        
    if not request.json or 'query' not in request.json:
        return jsonify({'error': 'Query parameter is required'}), 400
    
    query = request.json['query']
    response = assistant.ask(query)
    
    return jsonify({
        'query': query,
        'response': response
    })

@app.route('/', methods=['GET'])
def health_check():
    return jsonify({
        'status': 'ok',
        'message': 'Thapar Assistant API is running'
    })

# def start_ngrok():
#     # Start ngrok tunnel
#     try:
#         # Free ngrok connections time out after a while, so consider checking the active tunnels
#         # and reconnecting if needed in a production environment
#         http_tunnel = ngrok.connect(5000)
#         public_url = http_tunnel.public_url
#         print(f"\n* Ngrok tunnel established at: {public_url}")
#         print(f"* API endpoint available at: {public_url}/api/ask")
#         print("* Send POST requests with JSON payload: {\"query\": \"your question here\"}")
#     except Exception as e:
#         print(f"Error starting ngrok tunnel: {str(e)}")

if __name__ == "__main__":
    # Start ngrok in a separate thread
    # threading.Thread(target=start_ngrok, daemon=True).start()
    
    # Start Flask app
    print("Starting Thapar Assistant API...")
    port =int(os.environ.get("PORT",5000))
    app.run(host="0.0.0.0", port=port)