package com.apress.crm.customer.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.test.context.MockIntegrationContext;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.integration.test.mock.MockIntegration.mockMessageHandler;

@SpringBootTest
@SpringIntegrationTest
class FeedbackFlowTest {

    @Autowired
    private MockIntegrationContext mockIntegrationContext;

    @Autowired
    @Qualifier("feedbackInputChannel")
    private MessageChannel inputChannel;

    @Test
    void verifyFeedbackProcessingFlow() throws InterruptedException {
        // 1. Setup a Latch to wait for the result
        CountDownLatch latch = new CountDownLatch(1);

        // 2. Substitute the final '.handle()' endpoint with a Mock
        mockIntegrationContext.substituteMessageHandlerFor("processFeedbackFlow.handler",
                mockMessageHandler().handleNext(message -> {
                    // 3. Assert the transformation logic (Trim) happened
                    assertThat(message.getPayload()).isEqualTo("Great Service!");
                    latch.countDown();
                }));

        // 4. Send a test message with whitespace
        inputChannel.send(new GenericMessage<>("   Great Service!   "));

        // 5. Verify the flow completed successfully
        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
    }
}
