#!/usr/bin/env bash
set -e
# Build and run the application as one command.
# Usage: ./start.sh

# Build (packages frontend into Spring Boot jar)
mvn clean package -DskipTests

# Find the jar
JAR=$(ls target/*.jar | head -n 1)
if [ -z "$JAR" ]; then
  echo "No jar found in target/"
  exit 1
fi

# Start the jar (PORT env var is respected by Spring Boot via application.properties)
java -jar "$JAR"