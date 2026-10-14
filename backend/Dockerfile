FROM maven:3.9.12-eclipse-temurin-17 AS build

WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre-jammy

RUN apt-get update \
    && apt-get install -y --no-install-recommends gosu \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --create-home --home-dir /home/codearena codearena \
    && mkdir -p /app /var/data \
    && chown -R codearena:codearena /app /var/data

WORKDIR /app
COPY --from=build --chown=codearena:codearena /build/target/backend-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "mkdir -p /var/data && chown codearena:codearena /var/data && exec gosu codearena:codearena java -jar /app/app.jar"]
