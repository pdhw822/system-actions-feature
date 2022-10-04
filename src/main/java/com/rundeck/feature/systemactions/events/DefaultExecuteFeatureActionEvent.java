package com.rundeck.feature.systemactions.events;

import com.rundeck.feature.api.event.ExecuteFeatureActionEvent;
import lombok.Data;

import java.util.Map;

@Data
public class DefaultExecuteFeatureActionEvent implements ExecuteFeatureActionEvent {
    String actionId;
    Long timestamp;
    String feature;
    String action;
    String user;
    String actionDataJson;
}
