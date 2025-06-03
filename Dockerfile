FROM python:3.10-slim

# Install system dependencies
RUN apt-get update && apt-get install -y \
    wget unzip curl gnupg2 fonts-liberation libnss3 libxss1 libappindicator1 libasound2 \
    libatk-bridge2.0-0 libgtk-3-0 libx11-xcb1 libxcb1 libxcomposite1 libxcursor1 libxdamage1 \
    libxrandr2 libgbm1 xdg-utils chromium chromium-driver && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Set environment variables for Chrome
ENV CHROME_BIN=/usr/bin/chromium
ENV PATH="$PATH:/usr/lib/chromium/"

# Set work directory
WORKDIR /app

# Copy app files
COPY API/ /app/

# Install Python dependencies
RUN pip install --upgrade pip && pip install -r requirements.txt

# Expose port
EXPOSE 10000

# Start the Flask app
CMD ["python", "app.py"]
