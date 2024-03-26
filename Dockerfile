FROM gradle:8.2-jdk11 AS cache
RUN mkdir -p /home/gradle/cache_home
ENV GRADLE_USER_HOME=/home/gradle/cache_home
COPY build.gradle /home/gradle/java-code/
WORKDIR /home/gradle/java-code
RUN GRADLE_OPTS="-Xmx256m" gradle build --build-cache --stacktrace -i --no-daemon

FROM gradle:8.2-jdk11 as builder
COPY --from=cache /home/gradle/cache_home /home/gradle/.gradle
COPY . /usr/src/java-code
WORKDIR /usr/src/java-code
RUN GRADLE_OPTS="-Xmx256m" gradle shadowJar --build-cache --stacktrace --no-daemon

FROM openjdk:11
WORKDIR /app
COPY --from=builder /usr/src/java-code/build/libs/SimilarImagesBot-1.0.4-all.jar .
RUN echo -n 0 > uniqueId.dat
#ENV BOT_TOKEN="6420625082:AAHUlWjbsMmDaOwdS7cKqN7PiZL1r3DGGzI" \
#    ADMIN_ID=421629406 \
#    AUTO_REMOVE=true

ENTRYPOINT ["java", "-jar", "/app/SimilarImagesBot-1.0.4-all.jar"]
