# Use official Python image
FROM python:3.10-slim

# Install Chrome and dependencies
RUN apt-get update && apt-get install -y \
    libreoffice \
    wget unzip curl gnupg2 fonts-liberation libnss3 libxss1 libappindicator3-1 libasound2 \
    libatk-bridge2.0-0 libgtk-3-0 libx11-xcb1 libxcb1 libxcomposite1 libxcursor1 \
    libxdamage1 libxrandr2 libgbm1 xdg-utils chromium chromium-driver && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Set environment variables
ENV PYTHONDONTWRITEBYTECODE 1
ENV PYTHONUNBUFFERED 1

# Set working directory
WORKDIR /app

# Copy requirements and install Python dependencies
COPY requirements.txt /app/
RUN pip install --no-cache-dir -r requirements.txt
RUN pip install gunicorn

# Copy your API folder
COPY API /app/API

# Expose port 8080
EXPOSE 8080

# Use gunicorn to run the Flask app
CMD ["gunicorn", "--chdir", "API", "--bind", "0.0.0.0:8080", "app:app"]
