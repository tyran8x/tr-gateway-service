# stage 1
# Start with a base image containing Java runtime
FROM openjdk:21 AS builder
WORKDIR api-gateway-service
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} api-gateway-service.jar
RUN java -Djarmode=layertools -jar api-gateway-service.jar extract

# the second stage of our build will copy the extracted layers
FROM openjdk:21
WORKDIR api-gateway-service
COPY --from=builder api-gateway-service/dependencies/ ./
COPY --from=builder api-gateway-service/spring-boot-loader/ ./
COPY --from=builder api-gateway-service/snapshot-dependencies/ ./
COPY --from=builder api-gateway-service/application/ ./

EXPOSE 8061

ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
