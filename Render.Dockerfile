# Build the React website first, then package it into the Spring Boot app.
FROM node:24-alpine AS frontend-build
WORKDIR /frontend
COPY ecommerce-frontend/package.json ecommerce-frontend/package-lock.json ./
RUN npm ci
COPY ecommerce-frontend/ ./
RUN npm run build

FROM maven:3.9.11-eclipse-temurin-21 AS backend-build
WORKDIR /backend
COPY ecommerce-backend/pom.xml ./
COPY ecommerce-backend/src ./src
COPY --from=frontend-build /frontend/dist ./src/main/resources/static
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 10001 appuser
COPY --from=backend-build /backend/target/raithu-bazaar-backend-1.0.0.jar app.jar
USER 10001
EXPOSE 8080
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=65 -XX:+UseSerialGC"
ENTRYPOINT ["java", "-jar", "app.jar"]
