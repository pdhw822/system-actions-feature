package com.rundeck.feature.systemactions;

import software.amazon.awssdk.regions.Region;

public class SystemActionsFeatureConfig {

    private boolean enableSqsListener;
    private Region awsRegion;
    private String awsSqsQueue;
    private Integer messagePollTimeoutSeconds = 20;

    public Region getAwsRegion() {
        return awsRegion;
    }

    public void setAwsRegion(Region awsRegion) {
        this.awsRegion = awsRegion;
    }

    public String getAwsSqsQueue() {
        return awsSqsQueue;
    }

    public void setAwsSqsQueue(String awsSqsQueue) {
        this.awsSqsQueue = awsSqsQueue;
    }

    public boolean isEnableSqsListener() {
        return enableSqsListener;
    }

    public void setEnableSqsListener(boolean enableSqsListener) {
        this.enableSqsListener = enableSqsListener;
    }

    public Integer getMessagePollTimeoutSeconds() {
        return messagePollTimeoutSeconds;
    }

    public void setMessagePollTimeoutSeconds(Integer messagePollTimeoutSeconds) {
        this.messagePollTimeoutSeconds = messagePollTimeoutSeconds;
    }
}
