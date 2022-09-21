package com.rundeck.feature.systemactions;


import com.rundeck.feature.api.Feature;
import com.rundeck.feature.api.action.FeatureAction;
import com.rundeck.feature.systemactions.actions.DispatchSystemMessageFeatureAction;
import com.rundeck.feature.systemactions.actions.RunSqlScriptFeatureAction;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Component
public class SystemActionsFeature implements Feature<SystemActionsFeatureConfig> {
    public static final String NAME = "system-actions";

    Map<String, FeatureAction<?>> actions = Map.of(RunSqlScriptFeatureAction.NAME, new RunSqlScriptFeatureAction(),
            DispatchSystemMessageFeatureAction.NAME, new DispatchSystemMessageFeatureAction());

    boolean enabled;
    SystemActionsFeatureConfig config;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void enable() {
        enabled = true;
    }

    @Override
    public void disable() {
        enabled = false;
    }

    @Override
    public void cleanup() {

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
