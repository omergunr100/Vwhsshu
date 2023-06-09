package com.main.stepper.step.definition.implementation;

import com.main.stepper.data.DDRegistry;
import com.main.stepper.engine.executor.api.IStepRunResult;
import com.main.stepper.engine.executor.implementation.StepRunResult;
import com.main.stepper.io.api.DataNecessity;
import com.main.stepper.io.api.IDataIO;
import com.main.stepper.io.implementation.DataIO;
import com.main.stepper.step.definition.api.AbstractStepDefinition;
import com.main.stepper.step.definition.api.StepResult;
import com.main.stepper.step.execution.api.IStepExecutionContext;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class FileDumperStep extends AbstractStepDefinition {
    public FileDumperStep() {
        super("File Dumper", true);
        addInput(new DataIO("CONTENT", "Content", DataNecessity.MANDATORY, DDRegistry.STRING));
        addInput(new DataIO("FILE_NAME", "Target file path", DataNecessity.MANDATORY, DDRegistry.STRING));
        addOutput(new DataIO("RESULT", "File Creation Result", DDRegistry.STRING));
    }

    @Override
    public Class<? extends AbstractStepDefinition> getStepClass() {
        return FileDumperStep.class;
    }

    @Override
    public IStepRunResult execute(IStepExecutionContext context) {
        Instant startTime = Instant.now();
        // Get dataIOs
        List<IDataIO> inputs = getInputs();
        IDataIO contentIO = inputs.get(0);
        IDataIO fileNameIO = inputs.get(1);
        List<IDataIO> outputs = getOutputs();
        IDataIO resultIO = outputs.get(0);

        // Get inputs
        String content = (String) context.getInput(contentIO, contentIO.getDataDefinition().getType());
        String fileName = (String) context.getInput(fileNameIO, fileNameIO.getDataDefinition().getType());

        // Create file and dump content
        File file = new File(fileName);
        try {
            if(file.createNewFile()){
                if(content.isEmpty()){
                    context.log("No content to write");
                    context.setOutput(resultIO, "SUCCESS");

                    Duration duration = Duration.between(startTime, Instant.now());
                    return new StepRunResult(context.getUniqueRunId(), getName(), StepResult.WARNING, startTime, duration, "No content to write");
                }
                else{
                    try (FileWriter fileWriter = new FileWriter(file)) {
                        fileWriter.write(content);
                        context.setOutput(resultIO, "SUCCESS");

                        Duration duration = Duration.between(startTime, Instant.now());
                        return new StepRunResult(context.getUniqueRunId(), getName(), StepResult.SUCCESS, startTime, duration, "Success");
                    } catch (IOException e) {
                        context.log("Failed in writing content to file: " + e.getMessage());
                        context.setOutput(resultIO, "Failed in writing content to file");

                        Duration duration = Duration.between(startTime, Instant.now());
                        return new StepRunResult(context.getUniqueRunId(), getName(), StepResult.FAILURE, startTime, duration, "Failed in writing content to file");
                    }
                }
            }
            else{
                context.log("File already exists");
                context.setOutput(resultIO, "File already exists");

                Duration duration = Duration.between(startTime, Instant.now());
                return new StepRunResult(context.getUniqueRunId(), getName(), StepResult.FAILURE, startTime, duration, "File already exists");
            }
        } catch (IOException e) {
            context.log("Failed in creation of new file: " + e.getMessage());
            context.setOutput(resultIO, "Failed in creation of new file");

            Duration duration = Duration.between(startTime, Instant.now());
            return new StepRunResult(context.getUniqueRunId(), getName(), StepResult.FAILURE, startTime, duration, "Failed in creation of new file");
        }
    }
}
