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

package services.service;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

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
import services.config.AAIHttpClientConfig;
import services.exception.PnfRuntimeException;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class AAIHttpClientImpl implements AAIHttpClient {

    private final AAIHttpClientConfig aaiHttpClientConfig;

    @Inject
    public AAIHttpClientImpl(@Assisted final AAIHttpClientConfig aaiHttpClientConfig) {
        this.aaiHttpClientConfig = aaiHttpClientConfig;
    }

    @Override
    public CloseableHttpClient getAAIHttpClient() {

        final HttpClientBuilder httpClientBuilder = HttpClients.custom().useSystemProperties();
        final boolean aaiIgnoreSSLCertificateErrors = aaiHttpClientConfig.isAaiIgnoreSSLCertificateErrors();

        TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;

        if (aaiIgnoreSSLCertificateErrors) {
            try {
                SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
                sslContextBuilder.loadTrustMaterial(null, acceptingTrustStrategy);
                httpClientBuilder.setSSLContext(sslContextBuilder.build());

            } catch (NoSuchAlgorithmException e) {
                final String errorMessage = "NoSuchAlgorithmException while setting SSL Context for AAI HTTP Client.";
                throw new PnfRuntimeException(errorMessage, e);
            } catch (KeyStoreException e) {
                final String errorMessage = "KeyStoreException while setting SSL Context for AAI HTTP Client.";
                throw new PnfRuntimeException(errorMessage, e);
            } catch (KeyManagementException e) {
                final String errorMessage = "KeyManagementException while setting SSL Context for AAI HTTP Client.";
                throw new PnfRuntimeException(errorMessage, e);
            }

            httpClientBuilder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);

        }

        final String aaiUserName = aaiHttpClientConfig.getAaiUserName();

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

        if (aaiUserName != null) {
            final String aaiHost = aaiHttpClientConfig.getAaiHost();
            final Integer aaiHostPortNumber = aaiHttpClientConfig.getAaiHostPortNumber();
            final String aaiUserPassword = aaiHttpClientConfig.getAaiUserPassword();
            final AuthScope aaiHostPortAuthScope = new AuthScope(aaiHost, aaiHostPortNumber);
            final Credentials aaiCredentials = new UsernamePasswordCredentials(aaiUserName, aaiUserPassword);
            credentialsProvider.setCredentials(aaiHostPortAuthScope, aaiCredentials);
        }

        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);

        return httpClientBuilder.build();
    }
}
