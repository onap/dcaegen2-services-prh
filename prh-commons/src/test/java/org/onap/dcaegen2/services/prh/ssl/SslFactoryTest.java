/*
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

package org.onap.dcaegen2.services.prh.ssl;

import javax.net.ssl.SSLException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class SslFactoryTest {

    private static final String KEY_STORE = "org.onap.dcae.jks";
    private static final String KEYSTORE_PASSWORD = "keystore.password";
    private static final String TRUSTSTORE_PASSWORD = "truststore.password";
    private static final String TRUST_STORE = "org.onap.dcae.trust.jks";
    private SslFactory sslFactory = new SslFactory();

    @Test
    void shouldCreateInsecureContext() throws SSLException {
        Assertions.assertNotNull(sslFactory.createInsecureContext());
    }

    @Test
    void shouldCreateSecureContext() throws SSLException {
        Assertions.assertNotNull(sslFactory.createSecureContext(
                getPath(KEY_STORE),
                getPath(KEYSTORE_PASSWORD),
                getPath(TRUST_STORE),
                getPath(TRUSTSTORE_PASSWORD)));
    }

    @Test
    void shouldThrowSslExceptionWhenCreatingSecureContextWasFailed() {
        Assertions.assertThrows(SSLException.class, () -> sslFactory.createSecureContext(
                getPath(KEY_STORE),
                getPath(TRUSTSTORE_PASSWORD),
                getPath(TRUST_STORE),
                getPath(TRUSTSTORE_PASSWORD)));
    }

    private String getPath(String fileName) {
        return this.getClass().getClassLoader().getResource(fileName).getPath();
    }
}