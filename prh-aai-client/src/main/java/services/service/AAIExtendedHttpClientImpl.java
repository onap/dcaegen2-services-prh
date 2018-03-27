package services.service;


import com.google.common.base.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;

import services.config.AAIHttpClientConfiguration;
import services.utils.HttpUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;

public class AAIExtendedHttpClientImpl implements AAIExtendedHttpClient {

    private final CloseableHttpClient closeableHttpClient;
    private final String aaiHost;
    private final String aaiProtocol;
    private final Integer aaiHostPortNumber;

    @Autowired
    public AAIExtendedHttpClientImpl (AAIHttpClientConfiguration aaiHttpClientConfiguration) {
        final AAIHttpClient aaiHttpClient = new AAIHttpClientImpl(aaiHttpClientConfiguration);
        closeableHttpClient = aaiHttpClient.getAAIHttpClient();
        aaiHost = aaiHttpClientConfiguration.aaiHost();
        aaiProtocol = aaiHttpClientConfiguration.aaiProtocol();
        aaiHostPortNumber = aaiHttpClientConfiguration.aaiHostPortNumber();
    }

    @Override
    public String getExtendedDetails(final String aaiAPIPath, final Map<String, String> queryParams,
                                       final Map<String, String> headers) {
        final URI extendedURI =
                createAAIExtendedURI(aaiProtocol, aaiHost, aaiHostPortNumber, aaiAPIPath, queryParams);

        if (extendedURI == null) {
            return null;
        }

        final HttpGet getRequest = new HttpGet(extendedURI);

        for (Map.Entry<String, String> headersEntry : headers.entrySet()) {
            getRequest.addHeader(headersEntry.getKey(), headersEntry.getValue());
        }

        Optional<String> extendedDetails = Optional.absent();

        try {
            extendedDetails = closeableHttpClient.execute(getRequest, aaiResponseHandler());
        } catch (IOException ex) {
            //ToDo loging
        }

        // return response
        if (extendedDetails.isPresent()) {
            return extendedDetails.get();
        } else {
            return null;
        }
    }

    private URI createAAIExtendedURI(final String protocol, final String hostName,  final Integer portNumber,
                                     final String path, Map<String, String> queryParams) {
        final URIBuilder uriBuilder = new URIBuilder().setScheme(protocol).setHost(hostName).setPort(portNumber)
                .setPath(path);

        final String customQuery = createCustomQuery(queryParams);
        if (StringUtils.isNoneBlank(customQuery)) {
            uriBuilder.setCustomQuery(customQuery);
        }

        URI extendedURI = null;

        try {
            extendedURI = uriBuilder.build();
        } catch (URISyntaxException e) {
            // ToDo loging
        }

        return extendedURI;
    }

    private static String createCustomQuery(@Nonnull final Map<String, String> queryParams) {
        final StringBuilder queryStringBuilder = new StringBuilder("");
        final Iterator<Map.Entry<String, String>> queryParamIterator = queryParams.entrySet().iterator();
        while (queryParamIterator.hasNext()) {
            final Map.Entry<String, String> queryParamsEntry = queryParamIterator.next();
            queryStringBuilder.append(queryParamsEntry.getKey());
            queryStringBuilder.append("=");
            queryStringBuilder.append(queryParamsEntry.getValue());
            if (queryParamIterator.hasNext()) {
                queryStringBuilder.append("&");
            }
        }
        return queryStringBuilder.toString();
    }

    public static ResponseHandler<Optional<String>> aaiResponseHandler() {
        return httpResponse ->  {
            final int responseCode = httpResponse.getStatusLine().getStatusCode();
            final HttpEntity responseEntity = httpResponse.getEntity();

            if (HttpUtils.isSuccessfulResponseCode(responseCode) && null != responseEntity) {
                final String aaiResponse = EntityUtils.toString(responseEntity);
                return Optional.of(aaiResponse);
            } else {
                String aaiResponse = responseEntity != null ? EntityUtils.toString(responseEntity) : "";
                //ToDo loging
                return Optional.absent();
            }
        };
    }

}
