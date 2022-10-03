package com.rundeck.feature.systemactions.actions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DispatchSystemMessageFeatureActionData {
    String topic;
    String payload;
    String asUser;
}
