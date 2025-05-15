# Use an official Python base image with Ubuntu
FROM python:3.10-slim

# Set work directory
WORKDIR /app

# Install system dependencies for chromadb and sentence-transformers
RUN apt-get update && apt-get install -y \
    build-essential \
    libgl1-mesa-glx \
    libglib2.0-0 \
    && rm -rf /var/lib/apt/lists/*

# Copy files
COPY . .

# Install Python dependencies
RUN pip install --upgrade pip
RUN pip install -r requirements.txt

# Expose the port
EXPOSE 5000

# Use environment variable PORT from Railway
ENV PORT=5000

# Command to run the app
CMD ["python", "ThparGpt2.py"]
