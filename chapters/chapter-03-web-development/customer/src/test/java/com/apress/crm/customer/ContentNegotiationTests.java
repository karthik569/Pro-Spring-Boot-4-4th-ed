package com.apress.crm.customer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class ContentNegotiationTests {

    public static final String APPLICATION_XML_UTF8_VALUE = "application/xml;charset=UTF-8";

    @Autowired
    private RestTestClient client;

    @Test
    void shouldReturnJsonByDefault() {
        client.get().uri("/api/v1/customers")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON);
    }

    @Test
    void shouldReturnXmlWhenRequested() {
        client.get().uri("/api/v1/customers")
                .accept(MediaType.APPLICATION_XML)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(APPLICATION_XML_UTF8_VALUE);
    }

}
