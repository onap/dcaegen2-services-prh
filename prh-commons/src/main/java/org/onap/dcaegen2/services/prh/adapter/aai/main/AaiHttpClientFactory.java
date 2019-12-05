/*
 * ============LICENSE_START=======================================================
 * DCAEGEN2-SERVICES-SDK
 * ================================================================================
 * Copyright (C) 2018-2019 NOKIA Intellectual Property. All rights reserved.
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

package org.onap.dcaegen2.services.prh.adapter.aai.main;

import java.nio.file.Paths;
import java.util.UUID;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClient;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClientFactory;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.ImmutableRequestDiagnosticContext;
import org.onap.dcaegen2.services.sdk.rest.services.model.logging.RequestDiagnosticContext;
import org.onap.dcaegen2.services.sdk.security.ssl.ImmutableSecurityKeys;
import org.onap.dcaegen2.services.sdk.security.ssl.ImmutableSecurityKeysStore;
import org.onap.dcaegen2.services.sdk.security.ssl.Passwords;
import org.onap.dcaegen2.services.sdk.security.ssl.SecurityKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AaiHttpClientFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(AaiHttpClientFactory.class);

    private final AaiClientConfiguration configuration;

    public AaiHttpClientFactory(AaiClientConfiguration configuration) {
        this.configuration = configuration;
    }

    public RxHttpClient build() {
        LOGGER.debug("Setting ssl context");

        if (configuration.enableAaiCertAuth()) {
            return RxHttpClientFactory.create(createSslKeys());
        } else {
            return RxHttpClientFactory.createInsecure();
        }
    }

    private SecurityKeys createSslKeys() {
        return ImmutableSecurityKeys.builder()
            .keyStore(ImmutableSecurityKeysStore.of(Paths.get(configuration.keyStorePath())))
            .keyStorePassword(Passwords.fromPath(Paths.get(configuration.keyStorePasswordPath())))
            .trustStore(ImmutableSecurityKeysStore.of(Paths.get(configuration.trustStorePath())))
            .trustStorePassword(Passwords.fromPath(Paths.get(configuration.trustStorePasswordPath())))
            .build();
    }

    public static RequestDiagnosticContext createRequestDiagnosticContext() {
        return ImmutableRequestDiagnosticContext.builder()
            .invocationId(UUID.randomUUID()).requestId(UUID.randomUUID()).build();
    }

}
