package com.rundeck.feature.systemactions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rundeck.feature.systemactions.events.DefaultExecuteFeatureActionEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.message.ObjectMessage;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class SqsMessageListenerService {
    private final ApplicationEventPublisher eventPublisher;
    ExecutorService exec;
    AtomicBoolean shutdown = new AtomicBoolean(false);

    SqsClient sqsClient;
    private SystemActionsFeatureConfig config;
    private String queueUrl;

    ObjectMapper mapper = new ObjectMapper();
    public List<String> receivedMessages = new ArrayList<>();

    public SqsMessageListenerService(ApplicationEventPublisher eventPublisher, SystemActionsFeatureConfig config) {
        this.eventPublisher = eventPublisher;
        this.config = config;
        sqsClient = SqsClient.builder()
                .region(Region.of(config.getAwsRegion()))
                .build();
        Map<QueueAttributeName, String> attributes = new HashMap<>();
        attributes.put(QueueAttributeName.RECEIVE_MESSAGE_WAIT_TIME_SECONDS, "20");
        GetQueueUrlRequest getQueueRequest = GetQueueUrlRequest.builder()
                .queueName(config.getAwsSqsQueue())
                .build();
        queueUrl = sqsClient.getQueueUrl(getQueueRequest).queueUrl();
        SetQueueAttributesRequest setAttrsRequest = SetQueueAttributesRequest.builder()
                .queueUrl(queueUrl)
                .attributes(attributes)
                .build();

        sqsClient.setQueueAttributes(setAttrsRequest);

    }

    public void start() {
        exec = Executors.newSingleThreadExecutor();
        shutdown.set(false);
        exec.execute(() -> {

            while(!shutdown.get()) {
                try {
                    // Enable long polling on a message receipt.
                    ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                            .queueUrl(queueUrl)
                            .waitTimeSeconds(config.getMessagePollTimeoutSeconds())
                            .build();

                    var rsp = sqsClient.receiveMessage(receiveRequest);
                    handleQueueMessage(rsp);
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
            System.out.println("Shutting down the listener");
            sqsClient.close();

        });
    }

    private void handleQueueMessage(ReceiveMessageResponse rsp) {
        var rqb = DeleteMessageBatchRequest.builder();
        rqb.queueUrl(queueUrl);
        List<DeleteMessageBatchRequestEntry> deleteEntries = new ArrayList<>();
        rsp.messages().forEach(m -> {
            String payload = m.body();
            try {
                processMessage(payload);
            } catch(Exception ex) {
                log.error(String.format("Failed to process message: %s", payload), ex);
            }
            deleteEntries.add(DeleteMessageBatchRequestEntry.builder().id(m.messageId()).receiptHandle(m.receiptHandle()).build());
        });
        rqb.entries(deleteEntries);
        if(!deleteEntries.isEmpty()) sqsClient.deleteMessageBatch(rqb.build());
    }

    void processMessage(String msg) throws JsonProcessingException {
        var executeEvent = mapper.readValue(msg, DefaultExecuteFeatureActionEvent.class);
        eventPublisher.publishEvent(executeEvent);
    }

    public void stop() {
        shutdown.set(true);
        if(exec != null) exec.shutdown();
    }
}
