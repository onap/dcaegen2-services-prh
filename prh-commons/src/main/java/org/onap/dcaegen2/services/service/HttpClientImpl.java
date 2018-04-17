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

package org.onap.dcaegen2.services.service;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.onap.dcaegen2.services.config.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class HttpClientImpl implements HttpClient {

    private static Logger logger = LoggerFactory.getLogger(HttpClientImpl.class);

    //@Override
    public static <T> CloseableHttpClient getHttpClient(T clientConfig) {

        final HttpClientBuilder httpClientBuilder = HttpClients.custom().useSystemProperties();

        if (clientConfig instanceof AAIClientConfiguration) {

            AAIClientConfiguration aaiClientConfiguration = (AAIClientConfiguration) clientConfig;
            final boolean aaiIgnoreSSLCertificateErrors = aaiClientConfiguration.aaiIgnoreSSLCertificateErrors();

            TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;

            if (aaiIgnoreSSLCertificateErrors) {
                try {
                    logger.info("Setting SSL Context for AAI HTTP Client");
                    httpClientBuilder.setSSLContext(new SSLContextBuilder()
                            .loadTrustMaterial(null, acceptingTrustStrategy)
                            .build());

                } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
                    logger.error("Exception while setting SSL Context for AAI HTTP Client: {}", e);
                }

                httpClientBuilder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
            }

            final String aaiUserName = aaiClientConfiguration.aaiUserName();
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

            if (aaiUserName != null) {
                final String aaiHost = aaiClientConfiguration.aaiHost();
                final Integer aaiHostPortNumber = aaiClientConfiguration.aaiHostPortNumber();
                final String aaiUserPassword = aaiClientConfiguration.aaiUserPassword();
                final AuthScope aaiHostPortAuthScope = new AuthScope(aaiHost, aaiHostPortNumber);
                final Credentials aaiCredentials = new UsernamePasswordCredentials(aaiUserName, aaiUserPassword);
                credentialsProvider.setCredentials(aaiHostPortAuthScope, aaiCredentials);
            }

            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);

            return httpClientBuilder.build();

        } else if (clientConfig instanceof DmaapCustomConfig) {

            DmaapCustomConfig dmaapCustomConfig = (DmaapCustomConfig) clientConfig;
            final String dmaapUserName = dmaapCustomConfig.dmmapUserName();
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

            if (dmaapUserName != null) {
                final String dmaapHostName = dmaapCustomConfig.dmmapHostName();
                final Integer dmaapPortNumber = dmaapCustomConfig.dmmapPortNumber();
                final String dmaapUserPassword = dmaapCustomConfig.dmmapUserPassword();
                final AuthScope dmaapHostPortAuthScope = new AuthScope(dmaapHostName, dmaapPortNumber);
                final Credentials dmaapCredentials = new UsernamePasswordCredentials(dmaapUserName, dmaapUserPassword);
                credentialsProvider.setCredentials(dmaapHostPortAuthScope, dmaapCredentials);
            }

            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);

            return httpClientBuilder.build();

        }  else {
            return null;
        }
    }
}
