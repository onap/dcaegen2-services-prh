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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.onap.dcaegen2.services.prh.adapter.aai.impl.AaiClientConfigurations.secureConfiguration;

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.prh.adapter.aai.api.AaiServiceInstanceQueryModel;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpRequest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class AaiGetServiceInstanceClientTest extends AbstractHttpClientTest {

    public static final String SERVICE_INSTANCE_PATH =
        "https://aai.onap.svc.cluster.local:8443/aai/v23/business/customers/customer/Demonstration/"
            + "service-subscriptions/service-subscription/VCPE/service-instances/service-instance/df018f76-7fc8-46ab-8444-7d67e1efc284";

    @Test
    void getAaiResponse_shouldCallGetMethod_withGivenAaiHeaders() {

        // given
        AaiServiceInstanceQueryModel model = mock(AaiServiceInstanceQueryModel.class);
        Map<String, String> headers = HashMap.of("sample-key", "sample-value");
        AaiGetServiceInstanceClient cut = new AaiGetServiceInstanceClient(secureConfiguration(headers.toJavaMap()),
            httpClient);

        given(model.customerId()).willReturn("Demonstration");
        given(model.serviceInstanceId()).willReturn("df018f76-7fc8-46ab-8444-7d67e1efc284");
        given(model.serviceType()).willReturn("VCPE");

        given(httpClient.call(any(HttpRequest.class)))
            .willReturn(Mono.just(response));

        // when
        StepVerifier
            .create(cut.getAaiResponse(model))
            .expectNext(response)
            .verifyComplete();

        //then
        verify(httpClient)
            .call(argThat(httpRequest -> httpRequest.customHeaders().equals(headers) &&
                httpRequest.url().equals(SERVICE_INSTANCE_PATH)));
    }
}
