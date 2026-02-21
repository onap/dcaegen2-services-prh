FROM eclipse-temurin:11-jre-alpine

LABEL copyright="Copyright (C) 2018-2020 NOKIA, Copyright (C) 2026 Deutsche Telekom Intellectual Property. All rights reserved." \
      license.name="The Apache Software License, Version 2.0" \
      license.url="http://www.apache.org/licenses/LICENSE-2.0" \
      maintainer="Nokia Wroclaw ONAP Team"

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
ENTRYPOINT ["java", "-javaagent:opentelemetry-javaagent.jar", "-jar", "${project.artifactId}-${project.version}.jar"]

COPY ${project.build.directory}/${ext.dep.dir.path}/ ${ext.dep.dir.path}/
COPY ${project.build.directory}/${int.dep.dir.path}/ ${int.dep.dir.path}/
COPY ${project.build.directory}/${project.artifactId}-${project.version}.jar .

LABEL git.branch="${git.branch}" \
      git.build.host="${git.build.host}" \
      git.build.time="${git.build.time}" \
      git.build.user.email="${git.build.user.email}" \
      git.build.user.name="${git.build.user.name}" \
      git.build.version="${git.build.version}" \
      git.closest.tag.name="${git.closest.tag.name}" \
      git.commit.id="${git.commit.id}" \
      git.commit.message.short="${git.commit.message.short}" \
      git.commit.time="${git.commit.time}" \
      git.commit.user.email="${git.commit.user.email}" \
      git.commit.user.name="${git.commit.user.name}"
