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

package services.config;

import java.net.URL;

public class AAIHttpClientConfigBuilder {

    private String aaiHost;
    private Integer aaiHostPortNumber;
    private String aaiProtocol;
    private String aaiUserName;
    private String aaiUserPassword;
    private URL aaiProxyURL;
    private boolean aaiIgnoreSSLCertificateErrors;

    AAIHttpClientConfigBuilder(final String aaiHost, final Integer aaiHostPortNumber, final String aaiProtocol,
                               final String aaiUserName, final String aaiUserPassword, final URL aaiProxyURL,
                               final boolean aaiIgnoreSSLCertificateErrors) {
        this.aaiHost = aaiHost;
        this.aaiHostPortNumber = aaiHostPortNumber;
        this.aaiProtocol = aaiProtocol;
        this.aaiUserName = aaiUserName;
        this.aaiUserPassword = aaiUserPassword;
        this.aaiProxyURL = aaiProxyURL;
        this.aaiIgnoreSSLCertificateErrors = aaiIgnoreSSLCertificateErrors;
    }

    public AAIHttpClientConfigBuilder setAaiHost(String aaiHost) {
        this.aaiHost = aaiHost;
        return this;
    }

    public AAIHttpClientConfigBuilder setAaiHostPortNumber(Integer aaiHostPortNumber) {
        this.aaiHostPortNumber = aaiHostPortNumber;
        return this;
    }

    public AAIHttpClientConfigBuilder setAaiProtocol(String aaiProtocol) {
        this.aaiProtocol = aaiProtocol;
        return this;
    }

    public AAIHttpClientConfigBuilder setAaiUserName(String aaiUserName) {
        this.aaiUserName = aaiUserName;
        return this;
    }

    public AAIHttpClientConfigBuilder setAaiUserPassword(String aaiUserPassword) {
        this.aaiUserPassword = aaiUserPassword;
        return this;
    }

    public AAIHttpClientConfigBuilder setAaiProxyURL(URL aaiProxyURL) {
        this.aaiProxyURL = aaiProxyURL;
        return this;
    }

    public AAIHttpClientConfigBuilder setAaiIgnoreSSLCertificateErrors(boolean aaiIgnoreSSLCertificateErrors) {
        this.aaiIgnoreSSLCertificateErrors = aaiIgnoreSSLCertificateErrors;
        return this;
    }

    public AAIHttpClientConfig build() {
        return new AAIHttpClientConfig(aaiHost, aaiHostPortNumber, aaiProtocol, aaiUserName, aaiUserPassword,
                aaiProxyURL, aaiIgnoreSSLCertificateErrors);
    }
}
