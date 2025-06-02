#!/usr/bin/env bash

# Exit on error
set -e

# Install Google Chrome
echo "Installing Google Chrome..."
wget -q https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb
apt-get update
apt-get install -y ./google-chrome-stable_current_amd64.deb

# Install ChromeDriver manually
echo "Installing ChromeDriver manually..."
CHROME_VERSION=$(google-chrome --version | grep -oP '\d+\.\d+\.\d+')
CHROMEDRIVER_VERSION=$(wget -qO- "https://chromedriver.storage.googleapis.com/LATEST_RELEASE_$CHROME_VERSION")
wget -q -O /tmp/chromedriver.zip "https://chromedriver.storage.googleapis.com/$CHROMEDRIVER_VERSION/chromedriver_linux64.zip"
unzip /tmp/chromedriver.zip -d /usr/local/bin/
chmod +x /usr/local/bin/chromedriver

# Install Python dependencies
pip install -r requirements.txt
