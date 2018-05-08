/*
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
package org.onap.dcaegen2.services.prh.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.prh.exceptions.DmaapNotFoundException;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 5/8/18
 */
class DmaapConsumerJsonValidatorTest {

    private String incorrectMessage = "{\"event\":{\"commonEventHeader\":{\"domain\":\"other\",\"eventId\":\"<<SerialNumber>>-reg\",\"eventName\":\"pnfRegistration_5GDU\",\"eventType\":\"pnfRegistration\",\"internalHeaderFields\":{},\"lastEpochMicrosec\":1519837825682,\"nfNamingCode\":\"5GRAN\",\"nfcNamingCode\":\"5DU\",\"priority\":\"Normal\",\"reportingEntityName\":\"5GRAN_DU\",\"sequence\":0,\"sourceId\":\"<<SerialNumber>>\",\"sourceName\":\"5GRAN_DU\",\"startEpochMicrosec\":1519837825682,\"version\":3}}}";

    private String jsonWithoutPnfVendorAndSerialNumber = "{\"event\":{\"commonEventHeader\":{\"domain\":\"other\",\"eventId\":\"<<SerialNumber>>-reg\",\"eventName\":\"pnfRegistration_5GDU\",\"eventType\":\"pnfRegistration\",\"internalHeaderFields\":{},\"lastEpochMicrosec\":1519837825682,\"nfNamingCode\":\"5GRAN\",\"nfcNamingCode\":\"5DU\",\"priority\":\"Normal\",\"reportingEntityName\":\"5GRAN_DU\",\"sequence\":0,\"sourceId\":\"<<SerialNumber>>\",\"sourceName\":\"5GRAN_DU\",\"startEpochMicrosec\":1519837825682,\"version\":3},\"otherFields\":{\"otherFieldsVersion\":1,\"pnfFamily\":\"BBU\",\"pnfLastServiceDate\":1517206400,\"pnfManufactureDate\":1516406400,\"pnfModelNumber\":\"AJ02\",\"pnfOamIpv4Address\":\"10.16.123.234\",\"pnfOamIpv6Address\":\"<<NONE>>\",\"pnfSoftwareVersion\":\"v4.5.0.1\",\"pnfType\":\"AirScale\"}}}";

    private String jsonWithoutIPInformation = "{\"event\":{\"commonEventHeader\":{\"domain\":\"other\",\"eventId\":\"<<SerialNumber>>-reg\",\"eventName\":\"pnfRegistration_5GDU\",\"eventType\":\"pnfRegistration\",\"internalHeaderFields\":{},\"lastEpochMicrosec\":1519837825682,\"nfNamingCode\":\"5GRAN\",\"nfcNamingCode\":\"5DU\",\"priority\":\"Normal\",\"reportingEntityName\":\"5GRAN_DU\",\"sequence\":0,\"sourceId\":\"<<SerialNumber>>\",\"sourceName\":\"5GRAN_DU\",\"startEpochMicrosec\":1519837825682,\"version\":3},\"otherFields\":{\"otherFieldsVersion\":1,\"pnfFamily\":\"BBU\",\"pnfLastServiceDate\":1517206400,\"pnfManufactureDate\":1516406400,\"pnfModelNumber\":\"AJ02\",\"pnfSerialNumber\":\"QTFCOC540002E\",\"pnfSoftwareVersion\":\"v4.5.0.1\",\"pnfType\":\"AirScale\",\"pnfVendorName\":\"Nokia\"}}}";

    @Test
    public void whenPassingCorrectedJson_ValidationNotThrowingAnException() throws DmaapNotFoundException {
        //when
        String message = "{\"event\":{\"commonEventHeader\":{\"domain\":\"other\",\"eventId\":\"<<SerialNumber>>-reg\",\"eventName\":\"pnfRegistration_5GDU\",\"eventType\":\"pnfRegistration\",\"internalHeaderFields\":{},\"lastEpochMicrosec\":1519837825682,\"nfNamingCode\":\"5GRAN\",\"nfcNamingCode\":\"5DU\",\"priority\":\"Normal\",\"reportingEntityName\":\"5GRAN_DU\",\"sequence\":0,\"sourceId\":\"<<SerialNumber>>\",\"sourceName\":\"5GRAN_DU\",\"startEpochMicrosec\":1519837825682,\"version\":3},\"otherFields\":{\"otherFieldsVersion\":1,\"pnfFamily\":\"BBU\",\"pnfLastServiceDate\":1517206400,\"pnfManufactureDate\":1516406400,\"pnfModelNumber\":\"AJ02\",\"pnfOamIpv4Address\":\"10.16.123.234\",\"pnfOamIpv6Address\":\"<<NONE>>\",\"pnfSerialNumber\":\"QTFCOC540002E\",\"pnfSoftwareVersion\":\"v4.5.0.1\",\"pnfType\":\"AirScale\",\"pnfVendorName\":\"Nokia\"}}}";
        String parsedObject = DmaapConsumerJsonValidator.validate(message);
        //then
        Assertions.assertNotNull(parsedObject);
        String createdJson = "{\"pnf-name\":\"NOKQTFCOC540002E\",\"ipaddress-v4-oam\":\"10.16.123.234\",\"ipaddress-v6-oam\":\"\\u003c\\u003cNONE\\u003e\\u003e\"}";
        Assertions.assertEquals(createdJson, parsedObject);
    }

    @Test
    public void whenPassingIncorrectJson_ValidationThrowingAnException() {
        Assertions.assertThrows(DmaapNotFoundException.class, () -> {
            DmaapConsumerJsonValidator.validate(incorrectMessage);
        });
    }

    @Test
    public void whenPassingIncorrectJsonWithoutPnfSerialNumberOrPnfVendorName_ValidationThrowingAnException() {
        Assertions.assertThrows(DmaapNotFoundException.class, () -> {
            DmaapConsumerJsonValidator.validate(jsonWithoutPnfVendorAndSerialNumber);
        });
    }

    @Test
    public void whenPassingIncorrectJsonWithoutIPInformation_ValidationThrowingAnException() {
        Assertions.assertThrows(DmaapNotFoundException.class, () -> {
            DmaapConsumerJsonValidator.validate(jsonWithoutIPInformation);
        });
    }
}