FROM eclipse-temurin:11-jre-alpine

USER root
RUN apk add --no-cache curl && \
    adduser -D -u ${docker.user.id} -h ${docker.user.dir} ${docker.user.name} && \
    chmod -R a+w /var/log && \
    curl -fSL -o ${docker.user.dir}/opentelemetry-javaagent.jar \
        https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${opentelemetry.agent.version}/opentelemetry-javaagent.jar && \
    chown ${docker.user.name}:${docker.user.name} ${docker.user.dir}/opentelemetry-javaagent.jar

USER ${docker.user.name}
WORKDIR ${docker.user.dir}

EXPOSE 8100 8433
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -javaagent:opentelemetry-javaagent.jar -jar ${project.artifactId}-${project.version}.jar"]

COPY ${project.build.directory}/${ext.dep.dir.path}/ ${ext.dep.dir.path}/
COPY ${project.build.directory}/${int.dep.dir.path}/ ${int.dep.dir.path}/
COPY ${project.build.directory}/${project.artifactId}-${project.version}.jar .
