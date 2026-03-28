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

public final class PrhModelAwareGsonBuilder {

    // Build and cache a single Gson instance with all TypeAdapterFactories.
    // Uses this class's own classloader for ServiceLoader discovery, instead of
    // Thread.contextClassLoader, because Netty reactor-http-epoll threads may
    // have a null/bootstrap context classloader that cannot find SPI files.
    private static final Gson GSON = buildGson();

    private static Gson buildGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        ServiceLoader.load(TypeAdapterFactory.class, PrhModelAwareGsonBuilder.class.getClassLoader())
                .forEach(gsonBuilder::registerTypeAdapterFactory);
        return gsonBuilder.create();
    }

    public static Gson createGson() {
        return GSON;
    }
}
