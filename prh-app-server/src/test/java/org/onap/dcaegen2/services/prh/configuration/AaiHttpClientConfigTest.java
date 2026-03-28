/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom Intellectual Property. All rights reserved.
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

package org.onap.dcaegen2.services.prh.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import io.vavr.collection.HashMultimap;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.prh.adapter.aai.api.AaiPnfResultModel;
import org.onap.dcaegen2.services.prh.adapter.aai.api.AaiServiceInstanceResultModel;
import org.onap.dcaegen2.services.prh.model.utils.PrhModelAwareGsonBuilder;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.ImmutableHttpResponse;

/**
 * Tests that replicate the exact production deserialization path used in
 * {@link AaiHttpClientConfig#getGetClient()} and {@link AaiHttpClientConfig#getServiceInstanceClient()}.
 *
 * In production, the AAI HTTP client receives an {@link HttpResponse} and calls
 * {@code httpResponse.bodyAsJson(charset, gson, targetClass)} to deserialize it.
 * The Gson instance is created via {@link PrhModelAwareGsonBuilder#createGson()},
 * which uses {@link java.util.ServiceLoader} to discover TypeAdapterFactories.
 *
 * Existing tests mock all AAI clients, so Gson deserialization is never exercised.
 * These tests close that gap by constructing a real {@link HttpResponse} and calling
 * {@code bodyAsJson} — the same code path that runs in production.
 */
class AaiHttpClientConfigTest {

    @Test
    void bodyAsJson_shouldDeserializeAaiPnfResultModel() {
        String json = "{\"pnf-name\":\"test-pnf\",\"pnf-name2\":\"test2\",\"in-maint\":false}";
        HttpResponse response = ImmutableHttpResponse.builder()
                .url("https://aai/aai/v23/network/pnfs/pnf/test-pnf")
                .statusCode(200)
                .rawBody(json.getBytes(StandardCharsets.UTF_8))
                .headers(HashMultimap.withSeq().empty())
                .build();

        AaiPnfResultModel result = response.bodyAsJson(
                StandardCharsets.UTF_8,
                PrhModelAwareGsonBuilder.createGson(),
                AaiPnfResultModel.class);

        assertThat(result).isNotNull();
        assertThat(result.getPnfName()).isEqualTo("test-pnf");
        assertThat(result.getPnfName2()).isEqualTo("test2");
        assertThat(result.isInMaint()).isFalse();
    }

    @Test
    void bodyAsJson_shouldDeserializeAaiServiceInstanceResultModel() {
        String json = "{\"orchestration-status\":\"Active\"}";
        HttpResponse response = ImmutableHttpResponse.builder()
                .url("https://aai/aai/v23/business/customers/customer/test")
                .statusCode(200)
                .rawBody(json.getBytes(StandardCharsets.UTF_8))
                .headers(HashMultimap.withSeq().empty())
                .build();

        AaiServiceInstanceResultModel result = response.bodyAsJson(
                StandardCharsets.UTF_8,
                PrhModelAwareGsonBuilder.createGson(),
                AaiServiceInstanceResultModel.class);

        assertThat(result).isNotNull();
        assertThat(result.getOrchestrationStatus()).isEqualTo("Active");
    }

    @Test
    void bodyAsJson_shouldDeserializeAaiPnfResultModel_withRelationships() {
        String json = "{"
                + "\"pnf-name\":\"test-pnf\","
                + "\"relationship-list\":{"
                + "  \"relationship\":[{"
                + "    \"related-to\":\"service-instance\","
                + "    \"relationship-data\":[{"
                + "      \"relationship-key\":\"customer.global-customer-id\","
                + "      \"relationship-value\":\"customer1\""
                + "    }]"
                + "  }]"
                + "}"
                + "}";
        HttpResponse response = ImmutableHttpResponse.builder()
                .url("https://aai/aai/v23/network/pnfs/pnf/test-pnf")
                .statusCode(200)
                .rawBody(json.getBytes(StandardCharsets.UTF_8))
                .headers(HashMultimap.withSeq().empty())
                .build();

        AaiPnfResultModel result = response.bodyAsJson(
                StandardCharsets.UTF_8,
                PrhModelAwareGsonBuilder.createGson(),
                AaiPnfResultModel.class);

        assertThat(result).isNotNull();
        assertThat(result.getPnfName()).isEqualTo("test-pnf");
        assertThat(result.getRelationshipList()).isNotNull();
        assertThat(result.getRelationshipList().getRelationship()).hasSize(1);
        assertThat(result.getRelationshipList().getRelationship().get(0).getRelatedTo())
                .isEqualTo("service-instance");
    }

    /**
     * Reproduces the production failure where Gson deserialization of
     * {@link AaiPnfResultModel} fails on Netty reactor-http-epoll threads.
     *
     * In production, the AAI HTTP response arrives on a Netty event loop thread
     * which has a null/bootstrap context classloader. If {@code ServiceLoader.load()}
     * relies on the context classloader, it cannot discover the TypeAdapterFactory
     * SPI files, causing: "Interfaces can't be instantiated! Register an
     * InstanceCreator or a TypeAdapter for this type."
     *
     * This test simulates that scenario by setting the context classloader to null
     * before calling {@code PrhModelAwareGsonBuilder.createGson()}.
     */
    @Test
    void bodyAsJson_shouldWorkOnThreadWithNullContextClassloader() throws Exception {
        String json = "{\"pnf-name\":\"test-pnf\",\"in-maint\":false}";
        HttpResponse response = ImmutableHttpResponse.builder()
                .url("https://aai/aai/v23/network/pnfs/pnf/test-pnf")
                .statusCode(200)
                .rawBody(json.getBytes(StandardCharsets.UTF_8))
                .headers(HashMultimap.withSeq().empty())
                .build();

        AtomicReference<AaiPnfResultModel> resultRef = new AtomicReference<>();
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        Thread nettySimulator = new Thread(() -> {
            // Simulate Netty reactor thread: null context classloader
            Thread.currentThread().setContextClassLoader(null);
            try {
                AaiPnfResultModel result = response.bodyAsJson(
                        StandardCharsets.UTF_8,
                        PrhModelAwareGsonBuilder.createGson(),
                        AaiPnfResultModel.class);
                resultRef.set(result);
            } catch (Exception e) {
                errorRef.set(e);
            }
        }, "reactor-http-epoll-test");
        nettySimulator.start();
        nettySimulator.join();

        assertThat(errorRef.get()).as("Gson deserialization should not fail on reactor thread").isNull();
        assertThat(resultRef.get()).isNotNull();
        assertThat(resultRef.get().getPnfName()).isEqualTo("test-pnf");
    }
}
