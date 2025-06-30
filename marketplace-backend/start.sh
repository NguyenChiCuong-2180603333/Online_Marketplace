#!/bin/bash

echo "🚀 Starting Marketplace Backend..."

# Set production profile
export SPRING_PROFILES_ACTIVE=production

# Find the JAR file
JAR_FILE=$(find target -name "*.jar" -type f | head -n 1)

if [ -z "$JAR_FILE" ]; then
    echo "❌ No JAR file found in target directory"
    ls -la target/
    exit 1
fi

echo "📦 Found JAR file: $JAR_FILE"

# Start the application
echo "🚀 Starting application on port ${PORT:-8080}..."
exec java \
    -Xmx512m \
    -Xms256m \
    -Dserver.port=${PORT:-8080} \
    -Dspring.profiles.active=production \
    -jar "$JAR_FILE"