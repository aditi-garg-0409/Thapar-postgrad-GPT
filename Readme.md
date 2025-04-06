# Thapar PG Student Assistant

A Retrieval-Augmented Generation (RAG) system designed to answer queries about Thapar University's PG programs, hostels, scholarships, and student activities using Falcon-7B and ChromaDB.

## Features

- **Information Retrieval**:
  - Hostel fees and facilities
  - Scholarship details
  - PG course information
  - Student clubs and societies
  - University events

- **Technical Stack**:
  - 🦅 **LLM**: Falcon-7B-instruct (Hugging Face API)
  - 🗄️ **VectorDB**: ChromaDB
  - 🔍 **Embeddings**: all-MiniLM-L6-v2
  - 🐍 **Backend**: Python 3.9+

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/thapar-pg-assistant.git
   cd thapar-pg-assistant
2. Install Dependencies:
   ``` bash
   pip install chromadb
   pip install dotenv
   pip install huggingface_hub
   pip install sentence_transformers
3. Data Preparation 
   Convert all the .xlsx file into .txt file
4. Format each file with delimiters like ###
5. Usage 
   ```python
   from assistant import ThaparAssistant
   assistant = ThaparAssistant()
   Query examples
   queries = [
    "What are hostel fees for 2-sharing rooms?",
    "Which societies are best for coders?",
    "Tell me about MSc scholarships"
    ]
    for query in queries:
        print(f"Q: {query}")
        print(f"A: {assistant.ask(query)}\n")
6. Limitations
   1. Requires accurate source data formatting
   2. Falcon-7B may hallucinate without strict prompting
   3. ChromaDB collections need manual reset for data updates