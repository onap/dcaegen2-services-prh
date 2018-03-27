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
