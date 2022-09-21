package com.rundeck.feature.systemactions.actions

import com.rundeck.feature.api.action.FeatureAction
import com.rundeck.feature.api.context.FeatureActionContext
import com.rundeck.feature.api.model.CompletionStatus
import com.rundeck.feature.systemactions.events.LogActionOutputEvent
import org.springframework.context.ApplicationContext

class DispatchSystemMessageFeatureAction implements FeatureAction<DispatchSystemMessageFeatureActionData> {
    public static final String NAME = "dispatch-message";
    public static final String DESC = "Dispatch a payload to a topic on the Rundeck EventBus";

    @Override
    String getName() {
        return NAME
    }

    @Override
    String getDescription() {
        return DESC
    }

    @Override
    CompletionStatus execute(FeatureActionContext featureActionContext) {
        try {
            def evtPublisher = featureActionContext.eventPublisher
            def data = featureActionContext.get(FeatureActionContext.KEY_ACTION_DATA, DispatchSystemMessageFeatureActionData)

            ApplicationContext ctx = featureActionContext.get("spring-context", ApplicationContext.class);
            def eventBus = ctx.getBean("eventBus")
            eventBus.publish(data.topic, [actionId: featureActionContext.actionId, payload: data.payload])
            evtPublisher.publishOutput(new LogActionOutputEvent(actionId: featureActionContext.actionId,
                                                                message: "Published a message to topic: ${data.topic}"))

            return CompletionStatus.SUCCESS
        } catch(Exception ex) {

        }
        return CompletionStatus.ERROR
    }

    @Override
    Class<DispatchSystemMessageFeatureActionData> getFeatureActionDataClass() {
        return DispatchSystemMessageFeatureActionData
    }

    @Override
    DispatchSystemMessageFeatureActionData getSampleActionData() {
        return new DispatchSystemMessageFeatureActionData(topic: "clean-executions", payload: [cleanOlderThan: "2022-09-01"])
    }
}
