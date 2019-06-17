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

import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpMethod.DELETE;
import static org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpMethod.GET;
import static org.onap.dcaegen2.services.sdk.rest.services.adapters.http.HttpMethod.PUT;

import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
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
import reactor.core.publisher.Mono;

class BbsActionsTaskTest {

    private static final String AAI_URL = "https://aai.onap.svc.cluster.local:8443/aai/v12/network";
    private static final String PNF_URL = "/pnfs/pnf";
    private static final String LOGICAL_LINK_URL = "/logical-links/logical-link";
    private static final String ATTACHMENT_POINT = "attachment-point";

    private static final String PNF_WITHOUT_LINK_JSON = "BbsActionsTaskTestFiles/pnfWithoutLinks.json";
    private static final String PNF_WITH_LINK_JSON = "BbsActionsTaskTestFiles/pnfWithLogicalLink.json";
    private static final String LOGICAL_LINK_JSON = "BbsActionsTaskTestFiles/oldLogicalLink.json";
    private static final String LOGICAL_LINK_BODY = "BbsActionsTaskTestFiles/logicalLinkBody.json";


    private CbsConfiguration cbsConfiguration = mock(CbsConfiguration.class);
    private AaiClientConfiguration aaiClientConfiguration = TestAppConfiguration.createDefaultAaiClientConfiguration();
    private RxHttpClient httpClient = mock(RxHttpClient.class);

    private ClassLoader loader = getClass().getClassLoader();

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
        additionalFields.addProperty(ATTACHMENT_POINT, "");
        ConsumerDmaapModel consumerDmaapModel = buildConsumerDmaapModel(additionalFields);

        // when
        ConsumerDmaapModel result = new BbsActionsTaskImpl(cbsConfiguration, httpClient).execute(consumerDmaapModel).block();

        // then
        verifyZeroInteractions(httpClient);
        assertThat(result).isEqualTo(consumerDmaapModel);
    }

    @Test
    void whenPassedObjectHasLogicalLink_and_pnfHasNoLogicalLink_createLogicalLink_and_associateWithPnf_and_ReturnPayloadTransparently() {
        // given
        given(cbsConfiguration.getAaiClientConfiguration()).willReturn(aaiClientConfiguration);

        JsonObject additionalFields = new JsonObject();
        String linkName = "some-link";
        additionalFields.addProperty(ATTACHMENT_POINT, linkName);
        ConsumerDmaapModel consumerDmaapModel = buildConsumerDmaapModel(additionalFields);

        given(httpClient.call(any()))
            .willReturn(Mono.just(buildAaiResponse(OK, getBodyJson(PNF_WITHOUT_LINK_JSON))),
                Mono.just(buildAaiResponse(OK, "")));

        // when
        Mono<ConsumerDmaapModel> response = new BbsActionsTaskImpl(cbsConfiguration, httpClient).execute(consumerDmaapModel);

        // then
        assertEquals(consumerDmaapModel, response.block());

        ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient, times(2)).call(captor.capture());

        List<HttpRequest> args = captor.getAllValues();
        assertEquals(2, args.size());

        HttpRequest pnfGet = args.get(0);
        HttpRequest linkPut = args.get(1);

        assertEquals(AAI_URL + PNF_URL + "/Nokia123", pnfGet.url());
        assertEquals(GET, pnfGet.method());
        assertEquals(AAI_URL + LOGICAL_LINK_URL + "/" + linkName, linkPut.url());
        assertEquals(PUT, linkPut.method());
        assertEquals(getBodyJson(LOGICAL_LINK_BODY), extractBodyFromRequest(linkPut));
    }

    @Test
    void whenPassedObjectHasLogicalLink_and_pnfHasLogicalLink_deleteOldLogicalLink_and_createLogicalLink_and_associateWithPnf_and_ReturnPayloadTransparently() {
        // given
        given(cbsConfiguration.getAaiClientConfiguration()).willReturn(aaiClientConfiguration);

        JsonObject additionalFields = new JsonObject();
        String linkName = "some-link";
        additionalFields.addProperty(ATTACHMENT_POINT, linkName);
        ConsumerDmaapModel consumerDmaapModel = buildConsumerDmaapModel(additionalFields);

        given(httpClient.call(any()))
            .willReturn(Mono.just(buildAaiResponse(OK, getBodyJson(PNF_WITH_LINK_JSON))),
                Mono.just(buildAaiResponse(OK, "")),
                Mono.just(buildAaiResponse(OK, getBodyJson(LOGICAL_LINK_JSON))),
                Mono.just(buildAaiResponse(OK, "")));

        // when
        Mono<ConsumerDmaapModel> response = new BbsActionsTaskImpl(cbsConfiguration, httpClient).execute(consumerDmaapModel);

        // then
        assertEquals(consumerDmaapModel, response.block());

        ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient, times(4)).call(captor.capture());

        List<HttpRequest> args = captor.getAllValues();
        assertEquals(4, args.size());

        HttpRequest pnfGet = args.get(0);
        HttpRequest linkPut = args.get(1);
        HttpRequest linkGet = args.get(2);
        HttpRequest linkDelete = args.get(3);

        assertEquals(AAI_URL + PNF_URL + "/Nokia123", pnfGet.url());
        assertEquals(GET, pnfGet.method());
        assertEquals(AAI_URL + LOGICAL_LINK_URL + "/" + linkName, linkPut.url());
        assertEquals(PUT, linkPut.method());
        assertEquals(AAI_URL + LOGICAL_LINK_URL + "/" + linkName, linkGet.url());
        assertEquals(GET, linkGet.method());
        assertEquals(AAI_URL + LOGICAL_LINK_URL + "/" + linkName + "?resource-version=1560171816043", linkDelete.url());
        assertEquals(DELETE, linkDelete.method());
        assertEquals(getBodyJson(LOGICAL_LINK_BODY), extractBodyFromRequest(linkPut));
    }

    @Test
    void whenPassedObjectHasLogicalLink_butAaiQueryFails_returnError() {
        // given
        given(cbsConfiguration.getAaiClientConfiguration()).willReturn(aaiClientConfiguration);

        JsonObject additionalFields = new JsonObject();
        String linkName = "some-link";
        additionalFields.addProperty(ATTACHMENT_POINT, linkName);
        ConsumerDmaapModel consumerDmaapModel = buildConsumerDmaapModel(additionalFields);

        given(httpClient.call(
                ArgumentMatchers.argThat(argument -> argument.url().equals(AAI_URL + PNF_URL + "/Nokia123")
                    || argument.url().equals(AAI_URL + LOGICAL_LINK_URL + "/" + linkName))))
            .willReturn(Mono.just(buildAaiResponse(INTERNAL_SERVER_ERROR, "")));

        // when
        Mono<ConsumerDmaapModel> response = new BbsActionsTaskImpl(cbsConfiguration, httpClient)
            .execute(consumerDmaapModel);

        // then
        assertThatThrownBy(response::block).hasCauseInstanceOf(AaiFailureException.class).hasMessage(
            "org.onap.dcaegen2.services.prh.exceptions.AaiFailureException: "
                + "Incorrect response when performing BBS-related actions: 500. Occurred in GET PNF request. Pnf name: Nokia123");
    }

    private ConsumerDmaapModel buildConsumerDmaapModel(JsonObject additionalFields) {
        return ImmutableConsumerDmaapModel.builder()
            .ipv4("10.16.123.234")
            .ipv6("0:0:0:0:0:FFFF:0A10:7BEA")
            .correlationId("Nokia123")
            .serialNumber("QTFCOC540002E")
            .equipVendor("nokia")
            .equipModel("3310")
            .equipType("type")
            .nfRole("role")
            .swVersion("v4.5.0.1")
            .additionalFields(additionalFields)
            .build();
    }

    private HttpResponse buildAaiResponse(HttpResponseStatus status, String body) {
        return ImmutableHttpResponse
            .builder()
            .statusCode(status.code())
            .url("")
            .rawBody(body.getBytes())
            .build();
    }

    private String extractBodyFromRequest(HttpRequest linkPut) {
        return Mono.from(linkPut.body().contents()).block().toString(StandardCharsets.UTF_8);
    }

    private String getBodyJson(String filename) {
        return new Scanner(loader.getResourceAsStream(filename)).useDelimiter("\\A").next();
    }
}