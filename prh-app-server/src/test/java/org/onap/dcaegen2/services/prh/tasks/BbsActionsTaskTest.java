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

package org.onap.dcaegen2.services.prh.tasks;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.onap.dcaegen2.services.prh.TestAppConfiguration;
import org.onap.dcaegen2.services.prh.configuration.CbsConfiguration;
import org.onap.dcaegen2.services.prh.exceptions.AaiFailureException;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.onap.dcaegen2.services.prh.model.ImmutableConsumerDmaapModel;
import org.onap.dcaegen2.services.sdk.rest.services.aai.client.config.AaiClientConfiguration;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpRequest;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.ImmutableHttpResponse;
import org.onap.dcaegen2.services.sdk.rest.services.adapters.http.RxHttpClient;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.io.InputStreamReader;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class BbsActionsTaskTest {

    private static final String AAI_URL = "https://aai.onap.svc.cluster.local:8443/aai/v12/network/logical-links/logical-link/network/logical-links/logical-link/some-link";

    private CbsConfiguration cbsConfiguration = mock(CbsConfiguration.class);
    private AaiClientConfiguration aaiClientConfiguration = TestAppConfiguration.createDefaultAaiClientConfiguration();
    private RxHttpClient httpClient = mock(RxHttpClient.class);

    @Test
    void whenPassedObjectDoesntHaveAdditionalFields_ReturnPayloadTransparently() {
        // given
        given(cbsConfiguration.getAaiClientConfiguration()).willReturn(aaiClientConfiguration);
        ConsumerDmaapModel consumerDmaapModel = buildConsumerDmaapModel(null);

        // when
        ConsumerDmaapModel result = new BbsActionsTaskImpl(cbsConfiguration, httpClient).execute(consumerDmaapModel).block();

        // then
        verifyZeroInteractions(httpClient);
        assertThat(result).isEqualTo(consumerDmaapModel);
    }

    @Test
    void whenPassedObjectHasEmptyLogicalLink_ReturnPayloadTransparently() {
        // given
        given(cbsConfiguration.getAaiClientConfiguration()).willReturn(aaiClientConfiguration);

        JsonObject additionalFields = new JsonObject();
        additionalFields.addProperty("attachmentPoint", "");
        ConsumerDmaapModel consumerDmaapModel = buildConsumerDmaapModel(additionalFields);

        // when
        ConsumerDmaapModel result = new BbsActionsTaskImpl(cbsConfiguration, httpClient).execute(consumerDmaapModel).block();

        // then
        verifyZeroInteractions(httpClient);
        assertThat(result).isEqualTo(consumerDmaapModel);
    }

    @Test
    void whenPassedObjectHasLogicalLink_createLogicalLink_and_associateWithPnf_and_ReturnPayloadTransparently() {
        // given
        given(cbsConfiguration.getAaiClientConfiguration()).willReturn(aaiClientConfiguration);

        JsonObject additionalFields = new JsonObject();
        additionalFields.addProperty("attachmentPoint", "some-link");
        ConsumerDmaapModel consumerDmaapModel = buildConsumerDmaapModel(additionalFields);

        given(httpClient.call(any())).willReturn(Mono.just(buildAaiResponse(HttpResponseStatus.OK)));

        // when
        Mono<ConsumerDmaapModel> response = new BbsActionsTaskImpl(cbsConfiguration, httpClient).execute(consumerDmaapModel);

        // then
        assertEquals(consumerDmaapModel, response.block());

    }

    @Test
    void whenPassedObjectHasLogicalLink_butAaiQueryFails_returnError() {
        // given
        given(cbsConfiguration.getAaiClientConfiguration()).willReturn(aaiClientConfiguration);

        JsonObject additionalFields = new JsonObject();
        additionalFields.addProperty("attachmentPoint", "some-link");
        ConsumerDmaapModel consumerDmaapModel = buildConsumerDmaapModel(additionalFields);

        given(httpClient.call(any())).willReturn(Mono.just(buildAaiResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR)));

        // when
        Mono<ConsumerDmaapModel> response = new BbsActionsTaskImpl(cbsConfiguration, httpClient).execute(consumerDmaapModel);

        // then
        ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient).call(captor.capture());
        verifyNoMoreInteractions(httpClient);

        HttpRequest request = captor.getValue();
        assertThat(request.url()).isEqualTo(AAI_URL);
        assertThatThrownBy(response::block).hasCauseInstanceOf(AaiFailureException.class);
    }

    private ConsumerDmaapModel buildConsumerDmaapModel(JsonObject additionalFields) {
        return ImmutableConsumerDmaapModel.builder()
            .ipv4("10.16.123.234")
            .ipv6("0:0:0:0:0:FFFF:0A10:7BEA")
            .correlationId("NOKQTFCOC540002E")
            .serialNumber("QTFCOC540002E")
            .equipVendor("nokia")
            .equipModel("3310")
            .equipType("type")
            .nfRole("role")
            .swVersion("v4.5.0.1")
            .additionalFields(additionalFields)
            .build();
    }

    private HttpResponse buildAaiResponse(HttpResponseStatus status) {
        return ImmutableHttpResponse
            .builder()
            .statusCode(status.code())
            .url("")
            .rawBody("".getBytes())
            .build();
    }

    private void assertJsonEquals(Publisher<ByteBuf> requestBody, String path) {
        JsonParser parser = new JsonParser();
        JsonElement result = parser.parse(Mono.from(requestBody).block().toString(UTF_8));
        JsonElement expected = parser
            .parse(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(path)));

        assertThat(result).isEqualTo(expected);
    }
}