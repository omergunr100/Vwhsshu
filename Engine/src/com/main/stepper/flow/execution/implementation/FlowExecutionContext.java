package com.main.stepper.flow.execution.implementation;

import com.main.stepper.engine.executor.api.IFlowRunResult;
import com.main.stepper.engine.executor.api.IStepRunResult;
import com.main.stepper.flow.definition.api.IStepUsageDeclaration;
import com.main.stepper.flow.execution.api.IFlowExecutionContext;
import com.main.stepper.io.api.IDataIO;
import com.main.stepper.logger.api.ILogger;
import com.main.stepper.shared.structures.flow.FlowExecutionContextDTO;
import com.main.stepper.statistics.StatManager;
import com.main.stepper.step.execution.api.IStepExecutionContext;
import com.main.stepper.step.execution.implementation.StepExecutionContext;

import java.util.*;

public class FlowExecutionContext implements IFlowExecutionContext {
    private final UUID uniqueRunId;
    private final ILogger logger;
    private final StatManager statistics;
    private final Map<IStepUsageDeclaration, Map<IDataIO, IDataIO>> mappings;
    private final Map<IDataIO, Object> variables;
    private final List<IStepRunResult> stepRunResults;

    public FlowExecutionContext(Map<IStepUsageDeclaration, Map<IDataIO, IDataIO>> mappings, ILogger logger, StatManager statistics) {
        uniqueRunId = UUID.randomUUID();
        this.logger = logger.getSubLogger(uniqueRunId.toString());
        this.mappings = mappings;
        variables = new HashMap<>();
        this.statistics = statistics;
        stepRunResults = new ArrayList<>();
    }

    @Override
    public UUID getUniqueRunId() {
        return uniqueRunId;
    }

    @Override
    public ILogger getLogger() {
        return logger;
    }

    @Override
    public IDataIO getVariableIOByName(String name) {
        Optional<IDataIO> io = variables.keySet().stream().filter(dataIO -> dataIO.getName().equals(name)).findFirst();
        return io.orElse(null);
    }

    @Override
    public <T> T getVariable(IDataIO name, Class<T> type) {
        if (variables.get(name) == null)
            return null;
        return type.cast(variables.get(name));
    }

    @Override
    public void setVariable(IDataIO name, Object value) {
        variables.put(name, value);
    }

    @Override
    public IStepExecutionContext getStepExecutionContext(IStepUsageDeclaration step) {
        return new StepExecutionContext(variables, mappings.get(step), logger);
    }

    @Override
    public Map<IDataIO, Object> variables() {
        return variables;
    }

    @Override
    public void addStepRunResult(IStepRunResult stepRunResult) {
        statistics.addRunResult(stepRunResult);
    }

    @Override
    public void addFlowRunResult(IFlowRunResult flowRunResult) {
        statistics.addRunResult(flowRunResult);
    }

    @Override
    public String getUserCookie() {
        return null;
    }

    @Override
    public void setUserCookie(String userCookie) {

    }

    @Override
    public FlowExecutionContextDTO toDTO() {
        return null;
    }
}
