FROM python:3.11-slim

WORKDIR /app

# Install system dependencies (ffmpeg + build tools)
RUN apt-get update && apt-get install -y \
    ffmpeg \
    build-essential \
    gcc \
    && rm -rf /var/lib/apt/lists/*

# Install uv
RUN pip install --no-cache-dir uv

# Copy deps first for caching
COPY requirements.txt ./

# Install Python deps
RUN uv pip install --system -r requirements.txt

# Copy application code
COPY . .

CMD ["python", "main.py"]
