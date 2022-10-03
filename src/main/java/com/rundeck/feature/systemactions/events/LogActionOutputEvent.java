package com.rundeck.feature.systemactions.events;

import com.rundeck.feature.api.output.ActionOutputEvent;
import com.rundeck.feature.api.output.OutputLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogActionOutputEvent implements ActionOutputEvent {
    String actionId;
    OutputLevel level = OutputLevel.NORMAL;
    String message;
    String user;
    Long timestamp = System.nanoTime();
}
