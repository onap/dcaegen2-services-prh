package org.onap.dcaegen2.services.service.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.onap.dcaegen2.services.config.AAIHttpClientConfiguration;
import org.onap.dcaegen2.services.config.ImmutableAAIHttpClientConfiguration;

public class AAIHttpClientConfigurationTest {

    private static AAIHttpClientConfiguration client;
    private static final String AAI_HOST = "/aai/v11/network/pnfs/pnf/NOKQTFCOC540002E";
    private static final Integer PORT = 1234;
    private static final String PROTOCOL = "https";
    private static final String USER_NAME_PASSWORD = "PRH";

    @BeforeAll
    public static void init() {
        client = ImmutableAAIHttpClientConfiguration.builder()
                .aaiHost(AAI_HOST)
                .aaiHostPortNumber(PORT)
                .aaiProtocol(PROTOCOL)
                .aaiUserName(USER_NAME_PASSWORD)
                .aaiUserPassword(USER_NAME_PASSWORD)
                .aaiIgnoreSSLCertificateErrors(true)
                .build();
    }

    @Test
    public void testGetters_success() {
        Assertions.assertEquals(AAI_HOST,client.aaiHost());
        Assertions.assertEquals(PORT, client.aaiHostPortNumber());
        Assertions.assertEquals(PROTOCOL,client.aaiProtocol());
        Assertions.assertEquals(USER_NAME_PASSWORD, client.aaiUserName());
        Assertions.assertEquals(USER_NAME_PASSWORD, client.aaiUserPassword());
        Assertions.assertEquals(true, client.aaiIgnoreSSLCertificateErrors());
    }


}
