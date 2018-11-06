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

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SslFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(SslFactory.class);

    public SslContext createSecureContext(String keyStoreFilename,
        String keyStorePassword,
        String trustStoreFilename,
        String trustStorePassword) throws SSLException {
        LOGGER.info("Creating secure ssl context for: {} {}", keyStoreFilename, trustStoreFilename);
        try {
            return SslContextBuilder
                .forClient()
                .keyManager(keyManagerFactory(keyStoreFilename, loadPasswordFromFile(keyStorePassword)))
                .trustManager(trustManagerFactory(trustStoreFilename, loadPasswordFromFile(trustStorePassword)))
                .build();
        } catch (Exception ex) {
            throw new SSLException(ex);
        }
    }

    public SslContext createInsecureContext() throws SSLException {
        LOGGER.info("Creating insecure ssl context");
        return SslContextBuilder
            .forClient()
            .trustManager(InsecureTrustManagerFactory.INSTANCE)
            .build();
    }

    private KeyManagerFactory keyManagerFactory(String fileName, String password) throws Exception {
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(loadKeyStoreFromFile(fileName, password),
            password.toCharArray());
        return kmf;
    }

    private TrustManagerFactory trustManagerFactory(String fileName, String password) throws Exception {
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(loadKeyStoreFromFile(fileName, password));
        return tmf;
    }

    private KeyStore loadKeyStoreFromFile(String fileName, String keyStorePassword) throws Exception {
        KeyStore ks = KeyStore.getInstance("jks");
        ks.load(getResource(fileName), keyStorePassword.toCharArray());
        return ks;
    }

    private InputStream getResource(String fileName) throws Exception {
        return new FileInputStream(fileName);
    }

    private String loadPasswordFromFile(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)));
    }
}
