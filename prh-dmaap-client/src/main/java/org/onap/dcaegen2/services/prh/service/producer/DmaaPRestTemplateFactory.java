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

package org.onap.dcaegen2.services.prh.service.producer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.onap.dcaegen2.services.prh.config.DmaapPublisherConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class DmaaPRestTemplateFactory {

    /**
     * Function for creating RestTemplate object.
     *
     * @param publisherConfiguration - DMaaP publisher configuration object
     * @return RestTemplate with correct ssl configuration
     */
    public RestTemplate build(DmaapPublisherConfiguration publisherConfiguration) throws SSLException {
        if (publisherConfiguration.enableDmaapCertAuth()) {
            return createRestTemplateWithSslSetup(publisherConfiguration);
        }

        return new RestTemplate();
    }

    private RestTemplate createRestTemplateWithSslSetup(DmaapPublisherConfiguration publisherConfiguration)
            throws SSLException {
        try {
            RestTemplateBuilder builder = new RestTemplateBuilder();

            String keyStorePassword = loadPasswordFromFile(publisherConfiguration.keyStorePasswordPath());
            String trustStorePassword = loadPasswordFromFile(publisherConfiguration.trustStorePasswordPath());
            SSLContext sslContext = new SSLContextBuilder()
                    .loadKeyMaterial(
                            keyStore(publisherConfiguration.keyStorePath(), keyStorePassword),
                            keyStorePassword.toCharArray())
                    .loadTrustMaterial(
                            getFile(publisherConfiguration.trustStorePath()), trustStorePassword.toCharArray())
                    .build();

            return builder
                    .requestFactory(() -> createRequestFactory(sslContext)).build();

        } catch (GeneralSecurityException | IOException e) {
            throw new SSLException(e);
        }
    }

    private HttpComponentsClientHttpRequestFactory createRequestFactory(SSLContext sslContext) {
        SSLConnectionSocketFactory socketFactory =
                new SSLConnectionSocketFactory(sslContext);
        HttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(socketFactory).build();

        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }

    private KeyStore keyStore(String keyStoreFile, String keyStorePassword)
            throws GeneralSecurityException, IOException {
        KeyStore ks = KeyStore.getInstance("jks");
        ks.load(getResource(keyStoreFile), keyStorePassword.toCharArray());
        return ks;
    }

    private File getFile(String fileName) {
        return new File(fileName);
    }

    private InputStream getResource(String fileName) throws FileNotFoundException {
        return new FileInputStream(fileName);
    }

    private String loadPasswordFromFile(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)));
    }

}
