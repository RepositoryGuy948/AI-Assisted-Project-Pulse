# Stage 1: Build Vue frontend
FROM node:20-alpine AS frontend-builder
WORKDIR /frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# Stage 2: Build Spring Boot backend
FROM eclipse-temurin:17-jdk-alpine AS backend-builder
WORKDIR /app
RUN apk add --no-cache maven
COPY backend/pom.xml .
COPY backend/src ./src
# Copy compiled Vue dist into Spring Boot static resources
COPY --from=frontend-builder /frontend/dist ./src/main/resources/static
RUN mvn clean package -DskipTests

# Stage 3: Runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=backend-builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=azure"]
