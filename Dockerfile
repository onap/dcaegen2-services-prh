#This image is sample template only.
FROM azul/zulu-openjdk:8u152

MAINTAINER Przemysław Wąsala "przemyslaw.wasala@nokia.com"

COPY target/pnf-registration-handler-1.0.0-SNAPSHOT.jar /opt/app/Prh/
WORKDIR /opt/app/Prh

ENV HOME /opt/app/Prh
ENV JAVA_HOME /usr
#RUN apt-get update && apt-get install -y curl vim

EXPOSE 8080

CMD [ "java", "-jar", "pnf-registration-handler-1.0.0-SNAPSHOT.jar" ]