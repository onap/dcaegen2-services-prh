FROM docker.io/openjdk:11-jre-slim

LABEL copyright="Copyright (C) 2018-2019 NOKIA" \
      license.name="The Apache Software License, Version 2.0" \
      license.url="http://www.apache.org/licenses/LICENSE-2.0" \
      maintainer="Nokia Wroclaw ONAP Team"

ARG PRH_USER=prh
ARG PRH_USER_ID_NUMBER=1414

RUN useradd --user-group --uid ${PRH_USER_ID_NUMBER} -d ${docker.user.dir} ${PRH_USER}; \
chmod -R a+w /var/log

USER ${docker.user.name}
WORKDIR ${docker.user.dir}

EXPOSE 8100 8433
ENTRYPOINT ["java", "-jar", "${project.artifactId}-${project.version}.jar"]

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
