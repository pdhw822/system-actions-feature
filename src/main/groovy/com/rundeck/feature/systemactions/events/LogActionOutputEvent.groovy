package com.rundeck.feature.systemactions.events

import com.rundeck.feature.api.output.ActionOutputEvent
import com.rundeck.feature.api.output.OutputLevel
import groovy.transform.ToString

@ToString
class LogActionOutputEvent implements ActionOutputEvent {
    String actionId
    OutputLevel level = OutputLevel.NORMAL
    String message
    Long timestamp = System.nanoTime()
}
