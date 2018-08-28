/*
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


package org.onap.dcaegen2.services.prh.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HttpGetClientTest {


    private static final String SOMEURL = "http://someurl";
    private static final String DATA = "{}";
    private Gson gson = new Gson();
    private WebClient webClient = mock(WebClient.class);
    private WebClient.RequestHeadersUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
    private WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

    @Test
    void shouldReturnHttpOkStatusOnGetCall() {
        //given
        mockWebClientDependantObject();
        HttpGetClient httpGetClient = new HttpGetClient(webClient);

        //when
        Mono<JsonObject> jsonObjectMono = httpGetClient.callHttpGet(SOMEURL, JsonObject.class);

        //then
        assertThat(jsonObjectMono).isNotNull();
        assertThat(jsonObjectMono.block()).isEqualTo(gson.fromJson(DATA, JsonObject.class));
    }


    private void mockWebClientDependantObject() {
        doReturn(requestBodyUriSpec).when(webClient).get();
        when(requestBodyUriSpec.uri(SOMEURL)).thenReturn(requestBodyUriSpec);
        doReturn(responseSpec).when(requestBodyUriSpec).retrieve();
        doReturn(responseSpec).when(responseSpec).onStatus(any(), any());
        doReturn(responseSpec).when(responseSpec).onStatus(any(), any());
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(DATA));
    }

}