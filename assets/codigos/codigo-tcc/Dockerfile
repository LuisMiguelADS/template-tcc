ARG BUILD_IMAGE=maven:3.9-eclipse-temurin-25
ARG RUNTIME_IMAGE=eclipse-temurin:25-jre

FROM ${BUILD_IMAGE} AS build
ARG MODULE
WORKDIR /src
COPY . .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp -q -pl ${MODULE} -am package

FROM ${RUNTIME_IMAGE}
ARG MODULE
WORKDIR /app
COPY --from=build /src/${MODULE}/target/app.jar /app/app.jar
ENV JVM_OPTS=""
ENTRYPOINT ["sh", "-c", "exec java $JVM_OPTS -jar /app/app.jar"]
