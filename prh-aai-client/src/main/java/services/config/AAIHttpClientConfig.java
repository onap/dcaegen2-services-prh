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

public class AAIHttpClientConfig implements AAIConfig {
    private static final long serialVersionUID = 1L;

    private final String aaiHost;
    private final Integer aaiHostPortNumber;
    private final String aaiProtocol;
    private final String aaiUserName;
    private final String aaiUserPassword;
    private final boolean aaiIgnoreSSLCertificateErrors;

    AAIHttpClientConfig(final String aaiHost, final Integer aaiHostPortNumber, final String aaiProtocol,
                        final String aaiUserName, final String aaiUserPassword, final URL aaiProxyURL,
                        final boolean aaiIgnoreSSLCertificateErrors) {
        this.aaiHost = aaiHost;
        this.aaiHostPortNumber = aaiHostPortNumber;
        this.aaiProtocol = aaiProtocol;
        this.aaiUserName = aaiUserName;
        this.aaiUserPassword = aaiUserPassword;
        this.aaiIgnoreSSLCertificateErrors = aaiIgnoreSSLCertificateErrors;
    }

    public String getAaiHost() {
        return aaiHost;
    }

    public Integer getAaiHostPortNumber() {
        return aaiHostPortNumber;
    }

    public String getAaiProtocol() {
        return aaiProtocol;
    }

    public String getAaiUserName() {
        return aaiUserName;
    }

    public String getAaiUserPassword() {
        return aaiUserPassword;
    }

    public boolean isAaiIgnoreSSLCertificateErrors() {
        return aaiIgnoreSSLCertificateErrors;
    }
}
