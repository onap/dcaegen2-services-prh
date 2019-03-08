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


package org.onap.dcaegen2.services.prh.service;

class PnfRegistrationFields {

    static final String COMMON_FORMAT_FOR_STRING = "\": \"%s\"";
    static final String COMMON_FORMAT_FOR_JSON_OBJECT = "\": %s";
    static final String EVENT = "event";
    static final String COMMON_EVENT_HEADER = "commonEventHeader";
    static final String PNF_REGISTRATION_FIELDS = "pnfRegistrationFields";
    static final String SOURCE_NAME = "sourceName";
    static final String CORRELATION_ID = "correlationId";

    // optional fields
    static final String SERIAL_NUMBER = "serialNumber";
    static final String EQUIP_VENDOR = "vendorName";
    static final String EQUIP_MODEL = "modelNumber";
    static final String EQUIP_TYPE = "unitType";
    static final String NF_ROLE = "nfNamingCode";
    static final String SW_VERSION = "softwareVersion";
    static final String ADDITIONAL_FIELDS = "additionalFields";

    private PnfRegistrationFields() {}
}
