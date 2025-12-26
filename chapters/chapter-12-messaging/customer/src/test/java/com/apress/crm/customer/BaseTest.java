package com.apress.crm.customer;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.CockroachContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = CustomerApplication.class)
@ActiveProfiles("test")
@Import(TestCacheConfiguration.class)
@Testcontainers
public abstract class BaseTest {

    @Autowired
    private WebApplicationContext context;

    protected MockMvc mockMvc;
    protected RestTestClient restTestClient;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .build();
        this.restTestClient = RestTestClient.bindTo(mockMvc).build();
    }

    static final CockroachContainer cockroachContainer;

    static {
        cockroachContainer = new CockroachContainer("cockroachdb/cockroach:v25.4.0")
                .withCommand("start-single-node --insecure");
        cockroachContainer.start();
        System.setProperty("spring.datasource.url", cockroachContainer.getJdbcUrl());
        System.setProperty("spring.datasource.username", cockroachContainer.getUsername());
        System.setProperty("spring.datasource.password", cockroachContainer.getPassword());
    }
}