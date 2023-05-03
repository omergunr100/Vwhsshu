package com.main.stepper.step.definition.api;

import com.main.stepper.engine.executor.api.IStepRunResult;
import com.main.stepper.io.api.IDataIO;
import com.main.stepper.step.execution.api.IStepExecutionContext;

import java.io.Serializable;
import java.util.List;

public interface IStepDefinition extends Serializable {
    String getName();
    Boolean isReadOnly();
    List<IDataIO> getInputs();
    List<IDataIO> getOutputs();
    IStepRunResult execute(IStepExecutionContext context);
}
