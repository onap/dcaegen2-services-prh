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
package org.onap.dcaegen2.services.consumer;

import java.net.URI;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import org.apache.http.impl.client.CloseableHttpClient;
import org.onap.dcaegen2.services.config.DmaapConsumerConfiguration;
import org.onap.dcaegen2.services.response.DMaaPConsumerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:przemyslaw.wasala@nokia.com">Przemysław Wąsala</a> on 4/17/18
 */
public class DMaaPConsumerImpl implements DMaaPConsumer {

    private static final Logger logger = LoggerFactory.getLogger(DMaaPConsumerImpl.class);
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final DmaapConsumerConfiguration dmaapConsumerConfiguration;
    private final CloseableHttpClient closeableHttpClient;
    private final URI subscriberUri;
    private final Date subscriberCreationTime;

    public DMaaPConsumerImpl(DmaapConsumerConfiguration dmaapConsumerConfiguration,
        CloseableHttpClient closeableHttpClient, URI subscriberUri) {
        this.dmaapConsumerConfiguration = dmaapConsumerConfiguration;
        this.closeableHttpClient = closeableHttpClient;
        this.subscriberUri = subscriberUri;
        this.subscriberCreationTime = Date.from(Instant.from(LocalTime.now()));
    }

    @Override
    public DMaaPConsumerResponse fetchMessages() {
        return null;
    }

    @Override
    public Date getSubscriberCreationTime() {
        return null;
    }

    @Override
    public void close() throws Exception {

    }
}
