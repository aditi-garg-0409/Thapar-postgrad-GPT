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
  - 🧠 **LLM**: Mistral (Ollama)
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

7. TroubleShooting
   - Problem : Hallucinated answers
   - Solution : Strengthen Prompt Constraints
   ```python
   def build_prompt(self, query, context):
    return f"""Answer ONLY using:
    {context}
    Rules:
    1. Never invent numbers
    2. Use only Rs for currency
    3. If unsure, say "Not in records"
    Answer:"""

8. Response.
   - Query : List all boys hostel.

``` Terminal
List all boys hostels?

RETRIEVED CONTEXT FOR 'List all boys hostels?':
[Context 1]: Hostel-FRF/G
- Residence Type: Boys
- AC Room Availability: Not Available
- Mess Fees: â‚¹25500 per semester
- Warden: Dr. Parmod Kumar (Email: wardenmfrfg@thapar.edu)
- Caretakers:
  - Day: Mr. Birje...
[Context 2]: Vyan Hall (Previously: Hostel-H)
- Residence Type: Boys
- Capacity: 680
- AC Room Availability: Available (only for 4 sharing)
- Mess Fees: â‚¹25500 per semester
- Warden: Dr. Sandeep Pandey Contact:...
[Context 3]: Viyat Hall (Previously: Hostel-L)
- Residence Type: Boys
- Capacity: 300
- AC Room Availability: Available
- Mess Fees: â‚¹25500 per semester
- Warden: Dr. Bharat Garg Contact: 9115608834, Email: war...

The boy's hostels at Thapar University include FR F&amp;G with room fees being  ₹58000 per semester for two students, Vyan hall previously known as H having non-ac four student rooms priced at   ₹32600 & ac ones costing    ₹42100 alongwith three occupancy options available both on an Ac basis(₹47000 )and NonAc one which costs     ₹37500 , Viyathall formerly called L offering solely air conditioned double shared accommodation charging      ₹53500 every term..

