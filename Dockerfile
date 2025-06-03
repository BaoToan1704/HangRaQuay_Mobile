FROM python:3.10-slim

# Install system dependencies
RUN apt-get update && apt-get install -y \
    curl unzip gnupg2 fonts-liberation libnss3 libxss1 libasound2 libatk-bridge2.0-0 libgtk-3-0 \
    chromium chromium-driver && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Set environment variables
ENV CHROME_BIN=/usr/bin/chromium
ENV PATH=$PATH:/usr/lib/chromium/

# Set workdir and copy app files
WORKDIR /app
COPY . /app

# Install Python dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Expose the port
EXPOSE 8080

# Start the app
CMD ["python", "API/app.py"]
