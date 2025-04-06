import os
import chromadb
from dotenv import load_dotenv
from sentence_transformers import SentenceTransformer
# from transformers import


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
        self.model = SentenceTransformer("all-MiniLM-L12-v2")
    def embed(self,txt):
        return self.model.encode(txt)

class VectorDB(DataLoader):
    def __init__(self):
        super().__init__()
        self.client = chromadb.PersistentClient()
        self.embedder = EmbeddingModel()
        self.collections = {}
        self.incollections()   
    def incollections(self):
        
        file_types = {
            
        }
        
        