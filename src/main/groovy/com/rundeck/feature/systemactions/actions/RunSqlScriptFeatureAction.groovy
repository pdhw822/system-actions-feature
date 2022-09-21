package com.rundeck.feature.systemactions.actions;

import com.rundeck.feature.api.action.FeatureAction;
import com.rundeck.feature.api.context.FeatureActionContext
import com.rundeck.feature.api.event.ActionEventPublisher
import com.rundeck.feature.api.model.CompletionStatus
import com.rundeck.feature.api.output.OutputLevel
import com.rundeck.feature.systemactions.events.LogActionOutputEvent
import groovy.sql.Sql;
import org.springframework.context.ApplicationContext;

public class RunSqlScriptFeatureAction implements FeatureAction<RunSqlScriptFeatureActionData> {

    public static final String NAME = "run-sql";
    public static final String DESC = "Execute a SQL script on the Rundeck database";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESC;
    }

    @Override
    CompletionStatus execute(FeatureActionContext featureActionContext) {
        String actionId = featureActionContext.actionId
        def evtPublisher = featureActionContext.eventPublisher
        RunSqlScriptFeatureActionData data = featureActionContext.get(FeatureActionContext.KEY_ACTION_DATA, RunSqlScriptFeatureActionData.class);
        if(!data) {
            evtPublisher.publishOutput(new LogActionOutputEvent(actionId: id, level: OutputLevel.ERROR, message: "Action data was invalid"))
            return CompletionStatus.ERROR;
        }
        ApplicationContext ctx = featureActionContext.get("spring-context", ApplicationContext.class);
        try {
            def dataSource = ctx.getBean("dataSource")

            Sql sql = new Sql(dataSource)
            String stmtType = data.sql.substring(0, data.sql.indexOf(" ")).toUpperCase()
            if ("SELECT" == stmtType) processSelect(actionId, evtPublisher, sql, data)
            else if ("DELETE" == stmtType) processModification(actionId, "Deleted", evtPublisher, sql, data)
            else if ("UPDATE" == stmtType) processModification(actionId, "Updated", evtPublisher, sql, data)
            else if ("INSERT" == stmtType) processInsert(actionId, evtPublisher, sql, data)
            return CompletionStatus.SUCCESS
        } catch(Exception ex) {
            evtPublisher.publishOutput(new LogActionOutputEvent(actionId: actionId, message: ex.cause?.message?:ex.message))
            return CompletionStatus.ERROR
        }
    }

    void processModification(String id, String modType, ActionEventPublisher eventPublisher, Sql sql, RunSqlScriptFeatureActionData data) {
        sql.withTransaction {
            int updated = sql.executeUpdate(data.sql, data.params)
            eventPublisher.publishOutput(new LogActionOutputEvent(actionId: id, message: "${modType} ${updated} row(s)"))

        }
    }

    void processInsert(String id, ActionEventPublisher eventPublisher, Sql sql, RunSqlScriptFeatureActionData data) {
        sql.withTransaction {
            def result = sql.executeInsert(data.sql, data.params)
            eventPublisher.publishOutput(new LogActionOutputEvent(actionId: id, message: "Inserted: ${result.toString()}"))
        }
    }

    void processSelect(String id, ActionEventPublisher eventPublisher, Sql sql, RunSqlScriptFeatureActionData data) {
        StringBuilder b = new StringBuilder()
        def headers = []
        def metaOut = { meta ->
            (1..meta.columnCount).each {
                headers.add(meta.getColumnLabel(it))
            }
            b.append(headers.join(","))
            b.append('\n')
        }

        def rowOut = { rs ->
            b.append(rs.toRowResult().values().join(","))
            b.append('\n')
        }
        sql.eachRow(data.sql, data.params, metaOut, rowOut)
        eventPublisher.publishOutput(new LogActionOutputEvent(actionId: id, message: b.toString()))
    }

    @Override
    public Class<RunSqlScriptFeatureActionData> getFeatureActionDataClass() {
        return RunSqlScriptFeatureActionData.class;
    }

    @Override
    public RunSqlScriptFeatureActionData getSampleActionData() {
        return SAMPLE;
    }

    public static final RunSqlScriptFeatureActionData SAMPLE = new RunSqlScriptFeatureActionData(sql:"SELECT count(*) FROM projects WHERE date_created > ?", params:["2022-01-01"]);
}
