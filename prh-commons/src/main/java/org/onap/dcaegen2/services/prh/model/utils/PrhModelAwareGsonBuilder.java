/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2019 NOKIA Intellectual Property. All rights reserved.
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

package org.onap.dcaegen2.services.prh.model.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;
import java.util.ServiceLoader;
import org.onap.dcaegen2.services.prh.model.queries.NamedNode;
import org.onap.dcaegen2.services.prh.model.queries.NamedNodeAdapter;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.GsonAdaptersLogicalLinkComplete;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.GsonAdaptersLogicalLinkRequired;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.GsonAdaptersPnfComplete;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.GsonAdaptersPnfRequired;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.GsonAdaptersServiceInstanceComplete;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.GsonAdaptersServiceInstanceRequired;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.LogicalLinkComplete;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.LogicalLinkRequired;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.PnfComplete;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.PnfRequired;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.ServiceInstanceComplete;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.model.ServiceInstanceRequired;

public final class PrhModelAwareGsonBuilder {

    private static final Iterable<TypeAdapterFactory> TYPE_ADAPTER_FACTORIES =
            ServiceLoader.load(TypeAdapterFactory.class);

    public static Gson createGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        TYPE_ADAPTER_FACTORIES.forEach(gsonBuilder::registerTypeAdapterFactory);

        gsonBuilder.registerTypeAdapterFactory(new GsonAdaptersPnfRequired());
        gsonBuilder.registerTypeAdapterFactory(new GsonAdaptersPnfComplete());
        gsonBuilder.registerTypeAdapterFactory(new GsonAdaptersLogicalLinkRequired());
        gsonBuilder.registerTypeAdapterFactory(new GsonAdaptersLogicalLinkComplete());
        gsonBuilder.registerTypeAdapterFactory(new GsonAdaptersServiceInstanceRequired());
        gsonBuilder.registerTypeAdapterFactory(new GsonAdaptersServiceInstanceComplete());

        gsonBuilder.registerTypeAdapter(NamedNode.class, new NamedNodeAdapter());
        return gsonBuilder.create();
    }
}
