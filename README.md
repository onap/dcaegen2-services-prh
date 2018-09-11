<!--
  ~ ============LICENSE_START=======================================================
  ~ PNF-REGISTRATION-HANDLER
  ~ ================================================================================
  ~ Copyright (C) 2018 NOKIA Intellectual Property. All rights reserved.
  ~ ================================================================================
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~      http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  ~ ============LICENSE_END=========================================================
-->

# PRH (PNF Registration Handler)

Physical Network Function Registration Handler is responsible for registration of PNF (Physical Network Function) to 
ONAP (Open Network Automation Platform) in plug and play manner. 

## Introduction 

PRH is delivered as one **Docker container** which hosts application server and can be started by `docker-compose`.

## Functionality

![](docs/prhAlgo.png)
        
## Compiling PRH

Whole project (top level of PRH directory) and each module (sub module directory) can be compiled using 
`mvn clean install` command.       

## Maven GroupId:

org.onap.dcaegen2.services

### Maven Parent ArtifactId:

dcae-services

### Maven Children Artifacts:
1. prh-app-server: Pnf Registration Handler (PRH) server
2. prh-aai-client: Contains implementation of AAI client
3. prh-dmaap-client: Contains implementation of DmaaP client
4. prh-commons: Common code for whole prh modules
