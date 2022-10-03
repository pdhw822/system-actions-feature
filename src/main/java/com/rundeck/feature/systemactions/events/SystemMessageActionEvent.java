package com.rundeck.feature.systemactions.events;

import com.rundeck.feature.api.event.ActionEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemMessageActionEvent implements ActionEvent {
    String actionId;
    Long timestamp;
    String payload;
    String asUser;
    String user;
}
