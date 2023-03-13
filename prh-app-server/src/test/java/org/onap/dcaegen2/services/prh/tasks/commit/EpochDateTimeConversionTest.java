/*
 * ============LICENSE_START=======================================================
 * PNF-REGISTRATION-HANDLER
 * ================================================================================
 * Copyright (C) 2023 Deutsche Telekom Intellectual Property. All rights reserved.
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
package org.onap.dcaegen2.services.prh.tasks.commit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EpochDateTimeConversionTest {

    private EpochDateTimeConversion epochDateTimeConversion;

    @BeforeEach
    void setUp() {
        epochDateTimeConversion = new EpochDateTimeConversion();
        epochDateTimeConversion.setDaysForRecords("3");
    }

    @Test
    public void getStartDateOfTheDayTest(){
        epochDateTimeConversion.getDaysForRecords();
        Long day = epochDateTimeConversion.getStartDateOfTheDay();
        Assertions.assertNotNull(day);
    }

    @Test
    public void getEndDateOfTheDayTest(){
        Long day = epochDateTimeConversion.getEndDateOfTheDay();
        Assertions.assertNotNull(day);
    }
}
