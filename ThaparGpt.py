import os
from dotenv import load_dotenv
from huggingface_hub import InferenceClient
import chromadb
from chromadb.utils import embedding_functions
from sentence_transformers import SentenceTransformer


load_dotenv()

hf_api_key = os.getenv("HUGGINGFACE_API_KEY")


if not hf_api_key:
    raise ValueError("Hugging Face api key not found. Check .env file.")

