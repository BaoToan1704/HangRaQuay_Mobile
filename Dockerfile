FROM python:3.10-slim

# Install Chrome and dependencies
RUN apt-get update && apt-get install -y \
    wget unzip curl gnupg2 fonts-liberation libnss3 libxss1 libappindicator1 libindicator7 libasound2 \
    libatk-bridge2.0-0 libgtk-3-0 libx11-xcb1 libxcb1 libxcomposite1 libxcursor1 libxdamage1 \
    libxrandr2 libgbm1 xdg-utils chromium

# Set environment variable for Chrome binary
ENV CHROME_BIN=/usr/bin/chromium

# Set working directory to API folder
WORKDIR /app

# Copy files
COPY API/ /app/

# Install dependencies
RUN pip install --upgrade pip && pip install -r requirements.txt

# Expose port
EXPOSE 10000

# Run the Flask app
CMD ["python", "app.py"]
