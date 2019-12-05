/*
 * ============LICENSE_START=======================================================
 * DCAEGEN2-SERVICES-SDK
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
package org.onap.dcaegen2.services.prh.adapter.aai.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.onap.dcaegen2.services.prh.adapter.aai.impl.AaiClientConfigurations.secureConfiguration;

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.prh.adapter.aai.main.AaiClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpRequest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class AaiHttpPatchClientTest extends AbstractHttpClientTest {

    private final Map<String, String> DEFAULT_PATCH_HEADERS =
        HashMap.of("Content-Type", "application/merge-patch+json");

    @Test
    void getAaiResponse_shouldCallPatchMethod_withGivenHeaders_combinedWithContentType() {

        // given
        Map<String, String> headers = HashMap.of("sample-key", "sample-value");
        Map<String, String> expectedHeaders = DEFAULT_PATCH_HEADERS.merge(headers);

        AaiHttpPatchClient cut =
            new AaiHttpPatchClient(secureConfiguration(headers.toJavaMap()), bodyBuilder, httpClient);

        given(bodyBuilder.createJsonBody(eq(aaiModel)))
            .willReturn("test-body");

        given(httpClient.call(any(HttpRequest.class)))
            .willReturn(Mono.just(response));

        // when
        StepVerifier
            .create(cut.getAaiResponse(aaiModel))
            .expectNext(response)
            .verifyComplete();

        // then
        verify(httpClient)
            .call(argThat(httpRequest -> httpRequest.customHeaders().equals(expectedHeaders)));
    }

    @Test
    void getAaiResponse_shouldCallPatchMethod_withProperUri() {

        // given
        AaiClientConfiguration configuration = secureConfiguration();
        String uri = constructAaiUri(configuration, aaiModel.getCorrelationId());
        AaiHttpPatchClient cut = new AaiHttpPatchClient(configuration, bodyBuilder, httpClient);

        given(bodyBuilder.createJsonBody(eq(aaiModel)))
            .willReturn("test-body");

        given(httpClient.call(any(HttpRequest.class)))
            .willReturn(Mono.just(response));

        // when
        StepVerifier
            .create(cut.getAaiResponse(aaiModel))
            .expectNext(response)
            .verifyComplete();

        // then
        verify(httpClient)
            .call(argThat(httpRequest -> httpRequest.url().equals(uri)));
    }
}
