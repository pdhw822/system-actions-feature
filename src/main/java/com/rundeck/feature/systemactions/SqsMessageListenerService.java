package com.rundeck.feature.systemactions;

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

public class SqsMessageListenerService {
    ExecutorService exec;
    AtomicBoolean shutdown = new AtomicBoolean(false);

    SqsClient sqsClient;
    private SystemActionsFeatureConfig config;
    private String queueUrl;

    public List<String> receivedMessages = new ArrayList<>();

    public SqsMessageListenerService(SystemActionsFeatureConfig config) {
        this.config = config;
        sqsClient = SqsClient.builder()
                .region(config.getAwsRegion())
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
                    System.out.println("\n\n===RECEIVED MESSAGES===\n\n");
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
            receivedMessages.add(m.body());
            deleteEntries.add(DeleteMessageBatchRequestEntry.builder().id(m.messageId()).receiptHandle(m.receiptHandle()).build());
        });
        rqb.entries(deleteEntries);
        if(!deleteEntries.isEmpty()) sqsClient.deleteMessageBatch(rqb.build());
    }

    public void stop() {
        shutdown.set(true);
        if(exec != null) exec.shutdown();
    }
}
