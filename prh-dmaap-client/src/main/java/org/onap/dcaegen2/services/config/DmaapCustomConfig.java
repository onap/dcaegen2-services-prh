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
package org.onap.dcaegen2.services.config;

import java.io.Serializable;
import org.immutables.value.Value;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 3/28/18
 */
public interface DmaapCustomConfig extends Serializable {

    @Value.Parameter
    String dmmapHostName();

    @Value.Parameter
    Integer dmmapPortNumber();

    @Value.Parameter
    String dmmapTopicName();

    @Value.Parameter
    String dmmapProtocol();

    @Value.Parameter
    String dmmapUserName();

    @Value.Parameter
    String dmmapUserPassword();

    @Value.Parameter
    String dmmapContentType();


    interface Builder<T extends DmaapCustomConfig, B extends Builder<T, B>> {

        B dmmapHostName(String dmmapHostName);

        B dmmapPortNumber(Integer dmmapPortNumber);

        B dmmapTopicName(String dmmapTopicName);

        B dmmapProtocol(String dmmapProtocol);

        B dmmapUserName(String dmmapUserName);

        B dmmapUserPassword(String dmmapUserPassword);

        B dmmapContentType(String dmmapContentType);

        T build();
    }
}
