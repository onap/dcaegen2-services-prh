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

package org.onap.dcaegen2.services.prh.integration;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.onap.dcaegen2.services.prh.configuration.KafkaConfig;
import org.onap.dcaegen2.services.prh.tasks.commit.KafkaConsumerTask;
import org.onap.dcaegen2.services.prh.tasks.commit.ScheduledTasksRunnerWithCommit;
import org.onap.dcaegen2.services.prh.tasks.commit.ScheduledTasksWithCommit;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doAnswer;


@SpringBootTest
@TestPropertySource (properties = {"prh.workflow-scheduling-interval=20ms","spring.main.allow-bean-definition-overriding=true"})
@DirtiesContext
@ActiveProfiles(value = "autoCommitDisabled")
@Disabled
class PrhWorkflowSchedulingIntegrationForAutoCommitDisabledTest {

    private static final int EXPECTED_INVOCATIONS_NUMBER = 1;
    private static final int REMAINING_INVOCATIONS_NUMBER = 0;
    
    @MockBean
    private ScheduledTasksRunnerWithCommit scheduledTasksRunnerWithCommit;
    
    @MockBean
    private ScheduledTasksWithCommit scheduledTasksWithCommit;
    
    @MockBean
    private KafkaConsumerTask kafkaConsumerTask;
   
    
    @MockBean
    private KafkaConfig kafkaConfig;
    
    @MockBean
    private ConsumerFactory<String, String> consumerFactory;
    
    @MockBean
    private ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory;
    
    private CountDownLatch invocationLatch;
    
    @Test
    void prhWorkflowShouldBeExecutedRightAfterApplicationStart() {
        try {
            
            invocationLatch = new CountDownLatch(EXPECTED_INVOCATIONS_NUMBER);
            doAnswer(registerInvocation(invocationLatch)).when(scheduledTasksWithCommit).scheduleKafkaPrhEventTask();
            assertThatMethodWasInvokedOnce();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void assertThatMethodWasInvokedOnce() throws InterruptedException {
        boolean awaitResult = invocationLatch.await(1, TimeUnit.SECONDS);
        System.out.println("###awaitResult="+awaitResult);
        assertEquals(REMAINING_INVOCATIONS_NUMBER, invocationLatch.getCount());
    }

    private static Answer registerInvocation(CountDownLatch invocationLatch) {
        return invocation -> {
            System.out.println("###before countDown:"+invocationLatch.getCount());
            invocationLatch.countDown();
            System.out.println("###after countDown:"+invocationLatch.getCount());
            return null;
        };
    }
}