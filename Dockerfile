# Use official Python image
FROM python:3.10-slim

# Install Chrome and dependencies
RUN apt-get update && apt-get install -y \
    wget unzip curl gnupg2 fonts-liberation libnss3 libxss1 libappindicator3-1 libasound2 \
    libatk-bridge2.0-0 libgtk-3-0 libx11-xcb1 libxcb1 libxcomposite1 libxcursor1 \
    libxdamage1 libxrandr2 libgbm1 xdg-utils chromium chromium-driver && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Set environment variables
ENV PYTHONDONTWRITEBYTECODE 1
ENV PYTHONUNBUFFERED 1

# Set work directory
WORKDIR /app

# Copy requirement file and install dependencies
COPY requirements.txt /app/
RUN pip install --no-cache-dir -r requirements.txt

# Copy entire API folder into container
COPY API /app/API

# Expose port
EXPOSE 8080

# Run the Flask app inside API
RUN pip install gunicorn
CMD ["gunicorn", "--bind", "0.0.0.0:8080", "API.app:app"]
