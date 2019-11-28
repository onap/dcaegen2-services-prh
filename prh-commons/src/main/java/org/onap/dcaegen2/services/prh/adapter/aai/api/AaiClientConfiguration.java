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

package org.onap.dcaegen2.services.prh.adapter.aai.api;

import java.io.Serializable;
import java.util.Map;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

@Value.Immutable(prehash = true)
@Value.Style(builder = "new")
@Gson.TypeAdapters
public abstract class AaiClientConfiguration implements Serializable {

    private static final String PNF_PATH = "/network/pnfs/pnf";
    private static final String SERVICE_INSTANCE_PATH = "/business/customers/customer/${customer}/service-subscriptions/service-subscription/${serviceType}/service-instances/service-instance/${serviceInstanceId}";

    private static final long serialVersionUID = 1L;

    @Value.Parameter
    @Value.Default
    public String baseUrl() {
        return "";
    }

    /**
     * Please use baseUrl() instead
     */
    @Deprecated
    @Value.Default
    public String pnfUrl() {
        return baseUrl() + PNF_PATH;
    }

    @Value.Parameter
    public abstract String aaiUserName();

    @Value.Parameter
    public abstract String aaiUserPassword();

    @Value.Parameter
    public abstract Boolean aaiIgnoreSslCertificateErrors();

    /**
     * Please use baseUrl() instead
     */
    @Deprecated
    @Value.Default
    public String aaiServiceInstancePath() {
        return SERVICE_INSTANCE_PATH;
    }

    @Value.Parameter
    public abstract Map<String, String> aaiHeaders();

    @Value.Parameter
    public abstract String trustStorePath();

    @Value.Parameter
    public abstract  String trustStorePasswordPath();

    @Value.Parameter
    public abstract String keyStorePath();

    @Value.Parameter
    public abstract String keyStorePasswordPath();

    @Value.Parameter
    public abstract Boolean enableAaiCertAuth();
}