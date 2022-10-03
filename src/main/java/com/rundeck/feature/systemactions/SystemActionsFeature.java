package com.rundeck.feature.systemactions;


import com.rundeck.feature.api.Feature;
import com.rundeck.feature.api.action.FeatureAction;
import com.rundeck.feature.systemactions.actions.DispatchSystemMessageFeatureAction;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Component
public class SystemActionsFeature implements Feature<SystemActionsFeatureConfig> {
    public static final String NAME = "system-actions";

    Map<String, FeatureAction<?>> actions = Map.of(DispatchSystemMessageFeatureAction.NAME, new DispatchSystemMessageFeatureAction());

    boolean enabled;
    SystemActionsFeatureConfig config = new SystemActionsFeatureConfig();

    SqsMessageListenerService sqsMessageListenerService;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Provides actions that can do system maintenance and support";
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void enable() {
        enabled = true;
        if(config.isEnableSqsListener()) {
            if(sqsMessageListenerService == null) sqsMessageListenerService = new SqsMessageListenerService(config);
            sqsMessageListenerService.start();
        }
    }

    @Override
    public void disable() {
        enabled = false;
        cleanup();
    }

    @Override
    public void cleanup() {
        if(sqsMessageListenerService != null) sqsMessageListenerService.stop();
        sqsMessageListenerService = null;
    }

    @Override
    public Collection<FeatureAction<?>> getActions() {
        return actions.values();
    }

    @Override
    public Optional<FeatureAction<?>> getActionByName(String actionName) {
        return Optional.ofNullable(actions.get(actionName));
    }

    @Override
    public void configure(SystemActionsFeatureConfig systemActionsFeatureConfig) {
        config = systemActionsFeatureConfig;
        cleanup();
        if(config.isEnableSqsListener()) {
            sqsMessageListenerService = new SqsMessageListenerService(config);
        }
    }

    @Override
    public SystemActionsFeatureConfig getConfiguration() {
        return config;
    }

    @Override
    public Class<SystemActionsFeatureConfig> getConfigClass() {
        return SystemActionsFeatureConfig.class;
    }
}
