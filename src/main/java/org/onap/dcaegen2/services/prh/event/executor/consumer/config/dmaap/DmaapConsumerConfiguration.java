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
package org.onap.dcaegen2.services.prh.event.executor.consumer.config.dmaap;

import org.immutables.value.Value;
import org.onap.dcaegen2.services.prh.event.executor.mutual.config.DmaapConfig;

/**
 * @author Przemysław Wąsala <przemyslaw.wasala@nokia.com> on 3/23/18
 * @project pnf-registration-handler
 */
@Value.Immutable(prehash = true)
@Value.Style(stagedBuilder = true)
public abstract class DmaapConsumerConfiguration extends DmaapConfig {

    private static final long serialVersionUID = 1L;

    private String consumerId;
    private String consumerGroup;
    private Integer timeoutMS;
    private Integer messageLimit;
}
