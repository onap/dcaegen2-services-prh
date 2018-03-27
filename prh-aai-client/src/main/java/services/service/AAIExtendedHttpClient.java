package services.service;

import java.util.Map;

public interface AAIExtendedHttpClient {
    String getExtendedDetails(String aaiAPIPath, Map<String, String> queryParams, Map<String, String> headers);
}
