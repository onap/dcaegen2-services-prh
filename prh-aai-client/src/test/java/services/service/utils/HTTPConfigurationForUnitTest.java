/*-
 * ============LICENSE_START=======================================================
 * PROJECT
 * ================================================================================
 * Copyright (C) 2018 NOKIA Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package services.service.utils;

import services.config.AAIHttpClientConfiguration;

public class HTTPConfigurationForUnitTest extends AAIHttpClientConfiguration {

    protected static final String AAI_HOST_NAME = "1.2.3.4";
    protected static final Integer AAI_HOST_PORT_NUMBER = 1234;
    protected static final String AAI_HOST_PROTOCOL = "https";
    protected static final String AAI_USER_NAME = "PRH";
    protected static final String AAI_USER_PASS = "PRH";

    @Override
    public String aaiHost() {
        return AAI_HOST_NAME;
    }

    @Override
    public Integer aaiHostPortNumber() {
        return AAI_HOST_PORT_NUMBER;
    }

    @Override
    public String aaiProtocol() {
        return AAI_HOST_PROTOCOL;
    }

    @Override
    public String aaiUserName() {
        return AAI_USER_NAME;
    }

    @Override
    public String aaiUserPassword() {
        return AAI_USER_PASS;
    }

    @Override
    public boolean aaiIgnoreSSLCertificateErrors() {
        return true;
    }
}
