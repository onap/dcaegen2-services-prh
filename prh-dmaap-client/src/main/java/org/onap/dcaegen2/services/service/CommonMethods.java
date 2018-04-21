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
package org.onap.dcaegen2.services.service;

import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class CommonMethods {

    private static Logger logger = LoggerFactory.getLogger(CommonMethods.class);

    private CommonMethods() {}

    public static ResponseHandler<Optional<String>> dmaapResponseHandler() {
        return httpResponse ->  {
            final int responseCode = httpResponse.getStatusLine().getStatusCode();
            final HttpEntity responseEntity = httpResponse.getEntity();

            if (HttpUtils.isSuccessfulResponseCode(responseCode) && responseEntity != null) {
                logger.info("HTTP response successful.");
                final String response = EntityUtils.toString(responseEntity);
                return Optional.of(response);
            } else {
                String response = responseEntity != null ? EntityUtils.toString(responseEntity) : "";
                logger.error("HTTP response not successful : {}", response);
                return Optional.empty();
            }
        };
    }
}
