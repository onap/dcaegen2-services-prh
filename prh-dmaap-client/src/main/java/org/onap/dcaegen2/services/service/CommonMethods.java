package org.onap.dcaegen2.services.service;

import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class CommonMethods {

    private static Logger logger = LoggerFactory.getLogger(CommonMethods.class);

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
