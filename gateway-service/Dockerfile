FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace
COPY pom.xml pom.xml
COPY gateway-service/pom.xml gateway-service/pom.xml
COPY order-service/pom.xml order-service/pom.xml
RUN mvn -B -pl gateway-service -am dependency:go-offline
COPY gateway-service/src gateway-service/src
RUN mvn -B -pl gateway-service -am package -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build /workspace/gateway-service/target/gateway-service-*.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75","-jar","app.jar"]
