/*-
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
package org.onap.dcaegen2.services.prh.tasks;

import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.onap.dcaegen2.services.prh.IT.junit5.mockito.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 3/27/18
 */

@Configuration
@ComponentScan
@RunWith(SpringRunner.class)
@ExtendWith(MockitoExtension.class)
@ContextConfiguration(locations = {"classpath:scheduled-context.xml"})
public class ScheduledXmlContextITest extends AbstractTestNGSpringContextTests {

    private static final int WAIT_FOR_SCHEDULING = 1000;

    @Autowired
    private DmaapConsumerTask dmaapConsumerTaskSpy;


    @Test
    public void testScheduling() throws InterruptedException {
        Thread.sleep(WAIT_FOR_SCHEDULING);
        verify(dmaapConsumerTaskSpy, atLeast(2)).execute();
    }
}


