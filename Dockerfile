FROM gradle:8.4-jdk17 AS build
WORKDIR /app
COPY --chown=gradle:gradle . /app
RUN gradle buildFatJar --no-daemon

FROM openjdk:17-jdk-slim
ENV PORT=8081
EXPOSE ${PORT}
COPY --from=build /app/build/libs/*.jar /app/app.jar
ENTRYPOINT ["java", "-Dio.ktor.development=false", "-jar", "/app/app.jar"]
