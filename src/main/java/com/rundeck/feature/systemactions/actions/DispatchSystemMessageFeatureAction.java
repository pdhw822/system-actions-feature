package com.rundeck.feature.systemactions.actions;

import com.rundeck.feature.api.action.FeatureAction;
import com.rundeck.feature.api.context.ContextKeys;
import com.rundeck.feature.api.context.FeatureActionContext;
import com.rundeck.feature.api.model.CompletionStatus;
import com.rundeck.feature.api.output.OutputLevel;
import com.rundeck.feature.systemactions.events.LogActionOutputEvent;
import com.rundeck.feature.systemactions.events.SystemMessageActionEvent;

public class DispatchSystemMessageFeatureAction implements FeatureAction<DispatchSystemMessageFeatureActionData> {
    public static final String NAME = "dispatch-message";
    public static final String DESC = "Dispatch a payload to a topic on the Rundeck EventBus";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESC;
    }

    @Override
    public CompletionStatus execute(FeatureActionContext featureActionContext) {
        var evtPublisher = featureActionContext.getEventPublisher();
        String aid = featureActionContext.getActionId();
        try {
            var data = featureActionContext.get(ContextKeys.ACTION_DATA, DispatchSystemMessageFeatureActionData.class);

            featureActionContext.getEventPublisher().publish(data.topic, new SystemMessageActionEvent(aid,System.nanoTime(), data.getPayload(), data.getAsUser(), featureActionContext.getUser()));

            evtPublisher.publishOutput(new LogActionOutputEvent(aid, OutputLevel.NORMAL,
                                                                String.format("Published a message to topic: %s", data.topic), featureActionContext.getUser(), System.nanoTime()));

            return CompletionStatus.SUCCESS;
        } catch(Exception ex) {
            ex.printStackTrace();
            evtPublisher.publishOutput(new LogActionOutputEvent(aid, OutputLevel.ERROR,ex.getMessage(), featureActionContext.getUser(), System.nanoTime()));
        }
        return CompletionStatus.ERROR;
    }

    @Override
    public Class<DispatchSystemMessageFeatureActionData> getFeatureActionDataClass() {
        return DispatchSystemMessageFeatureActionData.class;
    }

    @Override
    public DispatchSystemMessageFeatureActionData getSampleActionData() {
        return new DispatchSystemMessageFeatureActionData("clean-executions", "{\"cleanOlderThan\": \"2022-09-01\"}", "system");
    }
}
