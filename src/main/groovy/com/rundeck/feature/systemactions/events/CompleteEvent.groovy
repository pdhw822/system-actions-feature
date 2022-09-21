package com.rundeck.feature.systemactions.events

import com.rundeck.feature.api.event.ActionCompleteEvent

class CompleteEvent implements ActionCompleteEvent {
    String actionId
    CompletionStatus status
    Long timestamp = System.nanoTime()

}
