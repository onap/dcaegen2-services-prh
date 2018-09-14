/*-
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

package org.onap.dcaegen2.services.prh.service.producer;

import static org.onap.dcaegen2.services.prh.model.CommonFunctions.createJsonBody;
import static org.onap.dcaegen2.services.prh.model.logging.MdcVariables.REQUEST_ID;
import static org.onap.dcaegen2.services.prh.model.logging.MdcVariables.X_INVOCATION_ID;
import static org.onap.dcaegen2.services.prh.model.logging.MdcVariables.X_ONAP_REQUEST_ID;

import java.net.URI;
import java.util.UUID;

import org.onap.dcaegen2.services.prh.config.AaiClientConfiguration;
import org.onap.dcaegen2.services.prh.model.ConsumerDmaapModel;
import org.slf4j.MDC;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import reactor.core.publisher.Mono;




public class AaiProducerReactiveHttpClient {

    private WebClient webClient;
    private final String aaiHost;
    private final String aaiProtocol;
    private final Integer aaiHostPortNumber;
    private final String aaiBasePath;
    private final String aaiPnfPath;

    /**
     * Constructor of AaiProducerReactiveHttpClient.
     *
     * @param configuration - AAI producer configuration object
     */
    public AaiProducerReactiveHttpClient(AaiClientConfiguration configuration) {
        this.aaiHost = configuration.aaiHost();
        this.aaiProtocol = configuration.aaiProtocol();
        this.aaiHostPortNumber = configuration.aaiPort();
        this.aaiBasePath = configuration.aaiBasePath();
        this.aaiPnfPath = configuration.aaiPnfPath();
    }

    /**
     * Function for calling AAI Http producer - patch request to AAI database.
     *
     * @param consumerDmaapModelMono - object which will be sent to AAI database
     * @return status code of operation
     */
    public Mono<ClientResponse> getAaiProducerResponse(ConsumerDmaapModel consumerDmaapModelMono) {
        return patchAaiRequest(consumerDmaapModelMono);
    }

    public AaiProducerReactiveHttpClient createAaiWebClient(WebClient webClient) {
        this.webClient = webClient;
        return this;
    }

    private Mono<ClientResponse> patchAaiRequest(ConsumerDmaapModel dmaapModel) {
        return
            webClient.patch()
                .uri(getUri(dmaapModel.getCorrelationId()))
                .header(X_ONAP_REQUEST_ID, MDC.get(REQUEST_ID))
                .header(X_INVOCATION_ID, UUID.randomUUID().toString())
                .body(Mono.just(createJsonBody(dmaapModel)), String.class)
                .exchange();
    }

    URI getUri(String pnfName) {
        return new DefaultUriBuilderFactory().builder().scheme(aaiProtocol).host(aaiHost).port(aaiHostPortNumber)
            .path(aaiBasePath + aaiPnfPath + "/" + pnfName).build();
    }
}
