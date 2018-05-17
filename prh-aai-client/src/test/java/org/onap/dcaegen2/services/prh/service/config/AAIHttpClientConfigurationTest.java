/*-
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
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

package org.onap.dcaegen2.services.prh.service.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.prh.config.AAIClientConfiguration;
import org.onap.dcaegen2.services.prh.config.ImmutableAAIClientConfiguration;

public class AAIHttpClientConfigurationTest {

    private static AAIClientConfiguration client;
    private static final String AAI_HOST = "/aai/v11/network/pnfs/pnf/NOKQTFCOC540002E";
    private static final Integer PORT = 1234;
    private static final String PROTOCOL = "https";
    private static final String USER_NAME_PASSWORD = "PRH";
    private static final String BASE_PATH = "/aai/v11";
    private static final String PNF_PATH = "/network/pnfs/pnf";

    @BeforeAll
    public static void init() {
        client = new ImmutableAAIClientConfiguration.Builder()
            .aaiHost(AAI_HOST)
            .aaiHostPortNumber(PORT)
            .aaiProtocol(PROTOCOL)
            .aaiUserName(USER_NAME_PASSWORD)
            .aaiUserPassword(USER_NAME_PASSWORD)
            .aaiIgnoreSSLCertificateErrors(true)
            .aaiBasePath(BASE_PATH)
            .aaiPnfPath(PNF_PATH)
            .build();
    }

    @Test
    public void testGetters_success() {
        Assertions.assertEquals(AAI_HOST, client.aaiHost());
        Assertions.assertEquals(PORT, client.aaiHostPortNumber());
        Assertions.assertEquals(PROTOCOL, client.aaiProtocol());
        Assertions.assertEquals(USER_NAME_PASSWORD, client.aaiUserName());
        Assertions.assertEquals(USER_NAME_PASSWORD, client.aaiUserPassword());
        Assertions.assertEquals(true, client.aaiIgnoreSSLCertificateErrors());
    }
}
