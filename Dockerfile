# Use the smallest Python image
FROM python:3.10-slim

# Working Directory
WORKDIR /app

# Install only necessary system packages
RUN apt-get update && apt-get install -y \
    libgl1-mesa-glx \
    libglib2.0-0 \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Copy only requirements first to leverage Docker layer caching
COPY requirements.txt .

# Install dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Copy the entire app
COPY . .

# Expose the port the app runs on

CMD ["sh", "-c", "gunicorn --bind 0.0.0.0:${PORT:-8080} ThaparGpt2:app"]

