#This image is sample template only.
FROM azul/zulu-openjdk:8u152

MAINTAINER Przemysław Wąsala "przemyslaw.wasala@nokia.com"

ADD /prh-app-server/target/prh-app-server.jar /opt/app/Prh/
WORKDIR /opt/app/Prh

ENV HOME /opt/app/Prh
ENV JAVA_HOME /usr
#RUN apt-get update && apt-get install -y curl vim

EXPOSE 8100

CMD [ "java", "-jar", "prh-app-server.jar" ]