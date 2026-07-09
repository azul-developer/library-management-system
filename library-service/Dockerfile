FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# copy only build files first to leverage docker layer cache
COPY pom.xml mvnw ./
COPY .mvn .mvn
COPY src ./src

# perform a quiet, non-interactive Maven build
RUN mvn -B -DskipTests package -q

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
