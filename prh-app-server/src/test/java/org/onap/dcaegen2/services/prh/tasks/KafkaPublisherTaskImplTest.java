/*
 * ============LICENSE_START=======================================================
 * PROJECT
 * ================================================================================
 * Copyright (C) 2018 NOKIA Intellectual Property. All rights reserved.
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

package org.onap.dcaegen2.services.prh.tasks;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.dcaegen2.services.prh.adapter.aai.api.ImmutableConsumerPnfModel;
import org.onap.dcaegen2.services.prh.exceptions.KafkaNotFoundException;
import org.onap.dcaegen2.services.prh.exceptions.PrhTaskException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.concurrent.SettableListenableFuture;
import reactor.test.StepVerifier;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 5/17/18
 */
@ExtendWith(MockitoExtension.class)
class KafkaPublisherTaskImplTest {

    private static final String TOPIC = "unauthenticated.PNF_READY";

    private KafkaPublisherTaskImpl kafkaPublisherTask;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    void execute_whenPassedObjectDoesntFit_ThrowsPrhTaskException() {
        kafkaPublisherTask = new KafkaPublisherTaskImpl(kafkaTemplate, () -> TOPIC);
        Executable executableFunction = () -> kafkaPublisherTask.execute(null);
        assertThrows(PrhTaskException.class, executableFunction, "The specified parameter is incorrect");
    }

    @Test
    void execute_whenPassedObjectFits_ReturnsCorrectStatus() throws KafkaNotFoundException {
        kafkaPublisherTask = new KafkaPublisherTaskImpl(kafkaTemplate, () -> TOPIC);

        SettableListenableFuture mockFuture = new SettableListenableFuture();
        mockFuture.set(null);
        when(kafkaTemplate.send(eq(TOPIC), org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(mockFuture);

        StepVerifier.create(kafkaPublisherTask.execute(createConsumerPnfModel()))
                .expectNext(TOPIC)
                .verifyComplete();

        verify(kafkaTemplate).send(eq(TOPIC), org.mockito.ArgumentMatchers.contains("NOKQTFCOC540002E"));
    }


    private ImmutableConsumerPnfModel createConsumerPnfModel() {
        return ImmutableConsumerPnfModel.builder()
                .ipv4("10.16.123.234")
                .ipv6("0:0:0:0:0:FFFF:0A10:7BEA")
                .correlationId("NOKQTFCOC540002E")
                .serialNumber("QTFCOC540002E")
                .equipVendor("nokia")
                .equipModel("3310")
                .equipType("type")
                .nfRole("gNB")
                .swVersion("v4.5.0.1")
                .additionalFields(null)
                .build();
    }
}
