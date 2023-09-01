# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [1.10.0] - 2023/09/01
### Changed
- [DCAEGEN2-3365] Code changed so that the autoCommitDisabled mode of PRH use CBSContentParser for environment variables.

## [1.9.0] - 2023/01/13
### Changed
- [DCAEGEN2-3312] Code additions to handle (optionally) early PNF registrations. This is enabled if the deployment Helm contains an environment variable: name: SPRING_PROFILES_ACTIVE
          value: autoCommitDisabled. 
  If this is set with the value of "autoCommitDisabled", then other required environment variables are:
       - name: kafkaBoostrapServerConfig
          value: onap-strimzi-kafka-bootstrap:9092
        - name: groupIdConfig
          value: OpenDCAE-c12
        - name: kafkaUsername
          value: strimzi-kafka-admin
        - name: kafkaPassword
          valueFrom:
            secretKeyRef:
              key: password
              name: strimzi-kafka-admin
        - name: kafkaTopic
          value: unauthenticated.VES_PNFREG_OUTPUT
        - name: SPRING_PROFILES_ACTIVE
          value: autoCommitDisabled
        - name: JAAS_CONFIG
          valueFrom:
            secretKeyRef:
              key: sasl.jaas.config
              name: strimzi-kafka-admin
  [DCAEGEN2-3357] Updated dependencies for vulnerability check

## [1.8.1] - 2022/08/11
### Changed
- [DCAEGEN2-3219] dcaegen2-services-prh vulnerability update


## [1.8.0] - 2022/02/14
### Changed
- Update DCAE SDK version from 1.8.7 to 1.8.8
- [DCAEGEN2-3050] Update Spring Boot version from 2.4.8 to 2.5.9

## [1.7.1] - 2021/08/24
### Changed
- Update DCAE SDK version from 1.6.0 to 1.8.7

## [1.7.0] - 2021/08/11
### Changed
- Change AAI variable syntax to "{{variable}}"

## [1.6.1] - 2021/07/29
### Changed
- Fix vulnerabilities (top up spring-boot version to 2.4.8)

## [1.6.0] - 2021/02/25
### Changed
- Change Docker's base image to integration-java11

## [1.5.6] - 2021/02/10
### Changed
- Updated to use DCAE-SDK 1.6.0

## [1.5.5] - 2021/02/10
### Changed
- [DCAEGEN2-2537](https://jira.onap.org/browse/DCAEGEN2-2537) - Upgrade Spring Web from 5.2.12 to 5.3.3

