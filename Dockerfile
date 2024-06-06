FROM openjdk:17-alpine

EXPOSE 8080
EXPOSE 9091
VOLUME /tmp

ARG JAR_FILE
#=target/mobile-access-gateway-0.1.0-SNAPSHOT-spring-boot.jar

ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en' LC_ALL='en_US.UTF-8'
ENV matadata-idp-location='idp-metadata.xml'
ENV http_proxyHost=host.docker.internal
ENV http_proxyPort=3128
ENV https_proxyHost=host.docker.internal
ENV https_proxyPort=3128


COPY ${JAR_FILE} /app.jar
COPY ../src/main/resources/idp-metadata.xml /idp-metadata.xml
COPY ../src/main/resources/keystore.jks /keystore.jks
COPY ../src/main/resources/cacerts /cacerts
COPY ../src/main/resources/example-client-certificate.jks /example-client-certificate.jks

USER root
ENV JAVA_OPTS=""
ENTRYPOINT java $JAVA_OPTS -jar /app.jar
