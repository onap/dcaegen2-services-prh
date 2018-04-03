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
import org.onap.dcaegen2.services.config.AAIHttpClientConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class AAIHttpClientImpl implements AAIHttpClient {

    Logger logger = LoggerFactory.getLogger(AAIHttpClientImpl.class);

    private AAIHttpClientConfiguration aaiHttpClientConfig;

    @Autowired
    public AAIHttpClientImpl(AAIHttpClientConfiguration aaiHttpClientConfiguration) {
        this.aaiHttpClientConfig = aaiHttpClientConfiguration;
    }

    @Override
    public CloseableHttpClient getAAIHttpClient() {

        final HttpClientBuilder httpClientBuilder = HttpClients.custom().useSystemProperties();
        final boolean aaiIgnoreSSLCertificateErrors = aaiHttpClientConfig.aaiIgnoreSSLCertificateErrors();

        TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;

        if (aaiIgnoreSSLCertificateErrors) {
            try {
                SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
                sslContextBuilder.loadTrustMaterial(null, acceptingTrustStrategy);
                httpClientBuilder.setSSLContext(sslContextBuilder.build());

            } catch (NoSuchAlgorithmException | KeyStoreException |  KeyManagementException e )  {
                logger.error("Exception while setting SSL Context for AAI HTTP Client: {}", e);
            }

            httpClientBuilder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
        }

        final String aaiUserName = aaiHttpClientConfig.aaiUserName();

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

        if (aaiUserName != null) {
            final String aaiHost = aaiHttpClientConfig.aaiHost();
            final Integer aaiHostPortNumber = aaiHttpClientConfig.aaiHostPortNumber();
            final String aaiUserPassword = aaiHttpClientConfig.aaiUserPassword();
            final AuthScope aaiHostPortAuthScope = new AuthScope(aaiHost, aaiHostPortNumber);
            final Credentials aaiCredentials = new UsernamePasswordCredentials(aaiUserName, aaiUserPassword);
            credentialsProvider.setCredentials(aaiHostPortAuthScope, aaiCredentials);
        }

        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);

        return httpClientBuilder.build();
    }
}
