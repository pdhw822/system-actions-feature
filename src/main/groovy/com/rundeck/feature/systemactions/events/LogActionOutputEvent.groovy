package com.rundeck.feature.systemactions.events

import com.rundeck.feature.api.output.ActionOutputEvent
import com.rundeck.feature.api.output.OutputLevel

class LogActionOutputEvent implements ActionOutputEvent {
    String actionId
    OutputLevel level = OutputLevel.NORMAL
    String message
    Long timestamp = System.nanoTime()
}
