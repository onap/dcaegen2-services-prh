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

import org.onap.dcaegen2.services.prh.adapter.aai.api.ConsumerDmaapModel;
import org.springframework.boot.configurationprocessor.json.JSONException;
import reactor.core.publisher.Flux;

/**
 * @author <a href="mailto:ajinkya-patil@t-systems.com">Ajinkya Patil</a> on 3/13/23
 */

public interface KafkaConsumerTask {
    Flux<ConsumerDmaapModel> execute() throws JSONException;

    void commitOffset();
}
