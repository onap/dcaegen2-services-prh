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
package org.onap.dcaegen2.services.prh.config;

import java.io.File;
import java.io.Serializable;
import org.immutables.gson.Gson;
import org.immutables.value.Value;
import org.springframework.stereotype.Component;

@Component
@Value.Immutable(prehash = true)
@Value.Style(builder = "new")
@Gson.TypeAdapters
public abstract class AaiSecurityConfiguration implements Serializable {

    private static final long serialVersionUID =1L;

    @Value.Parameter
    public abstract File keyFile();

    @Value.Parameter
    public abstract File trustStore();

    @Value.Parameter
    public abstract  String trustStorePass();

    @Value.Parameter
    public abstract File keyStore();

    @Value.Parameter
    public abstract String keyStorePass();

    @Value.Parameter
    public abstract Boolean enableAaiCertAuth();
}