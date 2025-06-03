FROM python:3.10-slim

# System packages
RUN apt-get update && apt-get install -y \
    wget unzip curl gnupg2 fonts-liberation libnss3 libxss1 libappindicator1 libasound2 \
    libatk-bridge2.0-0 libgtk-3-0 libx11-xcb1 libxcb1 libxcomposite1 libxcursor1 libxdamage1 \
    libxrandr2 libgbm1 xdg-utils ca-certificates gnupg lsb-release && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Install Google Chrome (stable)
RUN wget https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb && \
    apt-get update && apt-get install -y ./google-chrome-stable_current_amd64.deb && \
    rm google-chrome-stable_current_amd64.deb

# Install ChromeDriver
RUN CHROME_VERSION=$(google-chrome --version | grep -oP '\d+\.\d+\.\d+') && \
    DRIVER_VERSION=$(wget -qO- "https://chromedriver.storage.googleapis.com/LATEST_RELEASE_$CHROME_VERSION") && \
    wget -O /tmp/chromedriver.zip "https://chromedriver.storage.googleapis.com/$DRIVER_VERSION/chromedriver_linux64.zip" && \
    unzip /tmp/chromedriver.zip -d /usr/local/bin/ && \
    chmod +x /usr/local/bin/chromedriver && \
    rm /tmp/chromedriver.zip

# Set env vars for Selenium
ENV CHROME_BIN=/usr/bin/google-chrome
ENV PATH="/usr/local/bin:$PATH"

# Set working directory
WORKDIR /app

# Copy your Flask app and templates
COPY API/ /app/

# Install Python dependencies
RUN pip install --upgrade pip && pip install -r requirements.txt

# Expose port
EXPOSE 10000

# Start Flask app
CMD ["python", "app.py"]
