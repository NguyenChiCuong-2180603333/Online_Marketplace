
echo "🚀 Starting Render build process..."

# Set Java version
echo "☕ Setting up Java 17..."
export JAVA_HOME=/opt/java/openjdk
export PATH=$JAVA_HOME/bin:$PATH

# Verify Java version
java -version

# Make mvnw executable
echo "🔧 Making Maven wrapper executable..."
chmod +x mvnw

# Create uploads directory
echo "📁 Creating uploads directory..."
mkdir -p /tmp/uploads/images
mkdir -p /tmp/uploads/avatars

# Download dependencies and build
echo "📦 Downloading dependencies..."
./mvnw dependency:go-offline -B

echo "🏗️ Building application..."
./mvnw clean package -DskipTests -B

# Verify build
if [ -f target/*.jar ]; then
    echo "✅ Build successful!"
    ls -la target/*.jar
else
    echo "❌ Build failed - no JAR file found"
    exit 1
fi

echo "🎉 Render build completed!"