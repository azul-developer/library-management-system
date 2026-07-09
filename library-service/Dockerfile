FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# copy only build files first to leverage docker layer cache
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw

# Pre-fetch dependencies with retries to reduce flaky network failures.
RUN n=0; until [ "$n" -ge 3 ]; do \
			./mvnw -B -q -DskipTests -Dstyle.color=never -Dmaven.wagon.http.retryHandler.count=5 dependency:go-offline && break; \
			n=$((n+1)); \
			echo "Maven dependency download failed, retry $n/3"; \
			sleep 3; \
		done; \
		[ "$n" -lt 3 ]

COPY src ./src

# perform a quiet, non-interactive Maven build
RUN n=0; until [ "$n" -ge 3 ]; do \
			./mvnw -B -q -DskipTests -Dstyle.color=never -Dmaven.wagon.http.retryHandler.count=5 package && break; \
			n=$((n+1)); \
			echo "Maven package failed, retry $n/3"; \
			sleep 3; \
		done; \
		[ "$n" -lt 3 ]

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
COPY --from=builder /app/target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
