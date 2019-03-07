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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Please use the one from SDK security
 * */
@Deprecated
public class SslFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(SslFactory.class);

    /**
     * Function for creating secure ssl context.
     *
     * @param keyStorePath - path to file with keystore
     * @param keyStorePasswordPath - path to file with keystore password
     * @param trustStorePath - path to file with truststore
     * @param trustStorePasswordPath - path to file with truststore password
     * @return configured ssl context
     */
    public SslContext createSecureContext(String keyStorePath,
        String keyStorePasswordPath,
        String trustStorePath,
        String trustStorePasswordPath) throws SSLException {
        LOGGER.info("Creating secure ssl context for: {} {}", keyStorePath, trustStorePath);
        try {
            return SslContextBuilder
                .forClient()
                .keyManager(keyManagerFactory(keyStorePath, loadPasswordFromFile(keyStorePasswordPath)))
                .trustManager(trustManagerFactory(trustStorePath, loadPasswordFromFile(trustStorePasswordPath)))
                .build();
        } catch (GeneralSecurityException | IOException ex) {
            throw new SSLException(ex);
        }
    }

    /**
     * Function for creating insecure ssl context.
     *
     * @return configured insecure ssl context
     */
    public SslContext createInsecureContext() throws SSLException {
        LOGGER.info("Creating insecure ssl context");
        return SslContextBuilder
            .forClient()
            .trustManager(InsecureTrustManagerFactory.INSTANCE)
            .build();
    }

    private KeyManagerFactory keyManagerFactory(String path, String password)
            throws GeneralSecurityException, IOException {
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(loadKeyStoreFromFile(path, password),
            password.toCharArray());
        return kmf;
    }

    private TrustManagerFactory trustManagerFactory(String path, String password)
            throws GeneralSecurityException, IOException {
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(loadKeyStoreFromFile(path, password));
        return tmf;
    }

    private KeyStore loadKeyStoreFromFile(String path, String keyStorePassword)
            throws GeneralSecurityException, IOException {
        KeyStore ks = KeyStore.getInstance("jks");
        ks.load(getResource(path), keyStorePassword.toCharArray());
        return ks;
    }

    private InputStream getResource(String path) throws FileNotFoundException {
        return new FileInputStream(path);
    }

    private String loadPasswordFromFile(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)));
    }
}
