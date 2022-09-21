package com.rundeck.feature.systemactions.actions

import com.rundeck.feature.api.context.FeatureActionContext
import com.rundeck.feature.api.event.ActionCompleteEvent
import com.rundeck.feature.api.event.ActionEvent
import com.rundeck.feature.api.event.ActionEventPublisher
import com.rundeck.feature.api.event.ActionStartEvent
import com.rundeck.feature.api.model.CompletionStatus
import com.rundeck.feature.api.output.ActionOutputEvent
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.springframework.context.ApplicationContext
import spock.lang.Ignore
import spock.lang.Specification

@Ignore
class RunSqlScriptFeatureActionSpec extends Specification {
    def "Execute"() {
        setup:
        Properties dbprops = new Properties()
        HikariDataSource ds = new HikariDataSource(new HikariConfig(dbprops))
        def appContext = Mock(ApplicationContext) {
            getBean("dataSource") >> ds
        }
        RunSqlScriptFeatureAction action = new RunSqlScriptFeatureAction()
        FeatureActionContext ctx = new TestFeatureActionContext()
        ctx.actionId = "1"
        ctx.put("spring-context", appContext)
        ctx.put(FeatureActionContext.KEY_ACTION_DATA, new RunSqlScriptFeatureActionData(sql: "SELECT * FROM rduser WHERE login = ?", params:["user"]))

        when:
        def complete = action.execute(ctx)
        println ctx.eventPublisher.events[0]
        then:
        complete == CompletionStatus.SUCCESS

    }

    static class TestFeatureActionContext implements FeatureActionContext {
        String actionId
        TestEventPublisher eventPublisher = new TestEventPublisher()
        Map<String,Object> ctx = [:]

        @Override
        void put(String s, Object o) {
            ctx.put(s,o)
        }

        @Override
        def <T> T get(String s, Class<T> aClass) {
            return (T)ctx.get(s)
        }
    }

    static class TestEventPublisher implements ActionEventPublisher {

        List<ActionOutputEvent> events = []
        ActionStartEvent startEvent
        ActionCompleteEvent completeEvent

        @Override
        void publish(String s, ActionEvent actionEvent) {

        }

        @Override
        void publishOutput(ActionOutputEvent actionOutputEvent) {
            events.add(actionOutputEvent)
        }

        @Override
        void publishStart(ActionStartEvent actionStartEvent) {
            startEvent = actionStartEvent
        }

        @Override
        void publishCompletion(ActionCompleteEvent actionCompleteEvent) {
            completeEvent = actionCompleteEvent
        }
    }
}
