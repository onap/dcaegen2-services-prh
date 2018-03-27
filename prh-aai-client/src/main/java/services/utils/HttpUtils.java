package services.utils;

public class HttpUtils {

    private HttpUtils() {}

    public static final Integer HTTP_OK_RESPONSE_CODE = 200;
    public static final Integer HTTP_ACCEPTED_RESPONSE_CODE = 200;
    public static final Integer HTTP_NONAUTHORATIVE_INFORMATION_RESPONSE_CODE = 203;
    public static final Integer HTTP_NO_CONTENT_RESPONSE_CODE = 204;
    public static final Integer HTTP_RESET_CONTENT_RESPONSE_CODE = 205;
    public static final Integer HTTP_PARTIAL_CONTENT_RESPONSE_CODE = 206;
    public static final String JSON_APPLICATION_TYPE = "application/json";

    public static boolean isSuccessfulResponseCode(Integer statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }
}
