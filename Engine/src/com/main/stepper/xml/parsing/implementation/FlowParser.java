package com.main.stepper.xml.parsing.implementation;

import com.main.stepper.exceptions.xml.XMLException;
import com.main.stepper.flow.definition.api.IStepUsageDeclaration;
import com.main.stepper.flow.definition.implementation.Flow;
import com.main.stepper.io.api.DataNecessity;
import com.main.stepper.io.api.IDataIO;
import com.main.stepper.xml.generated.STFlow;
import com.main.stepper.xml.parsing.api.IParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class FlowParser implements IParser {
    private STFlow stflow;
    private Flow flow = null;

    public FlowParser(STFlow stflow){
        this.stflow = stflow;
    }

    public FlowParser load(STFlow stflow){
        this.stflow = stflow;
        this.flow = null;
        return this;
    }

    @Override
    public Flow parse() throws XMLException {
        // Get flow properties and mapping
        flow = new Flow(stflow.getName(), stflow.getSTFlowDescription());
        Map<IStepUsageDeclaration, Map<IDataIO, IDataIO>> flowMapping = new MappingParser(stflow).parse();
        // Add all step usages and mappings to flow
        flowMapping.keySet().stream().forEach(step -> flow.addStep(step, flowMapping.get(step)));
        // Get all outputs after aliasing
        List<IDataIO> outputs = new ArrayList<>();
        flowMapping.values().stream().forEach(map -> {
            outputs.addAll(map.values());
        });

        // Add formal outputs
        for(String formalName : stflow.getSTFlowOutput().split(",")) {
            Optional<IDataIO> match = outputs.stream().filter(dataIO -> dataIO.getName().equals(formalName)).findFirst();

            if(!match.isPresent())
                throw new XMLException("No match found for formal output name: " + formalName + " in flow: " + flow.name());

            flow.addFormalOutput(match.get());
        }

        // Add user required inputs
        List<IDataIO> currOutputs = new ArrayList<>();
        for(IStepUsageDeclaration step : flow.steps()){
            // There is a match for each dataIO at this stage
            List<IDataIO> mappedInputs = step.step().getInputs().stream().map(value -> flowMapping.get(step).get(value)).collect(Collectors.toList());
            List<IDataIO> mappedOutputs = step.step().getOutputs().stream().map(value -> flowMapping.get(step).get(value)).collect(Collectors.toList());
            // Check if there is a matching output at this stage
            mappedInputs.removeAll(currOutputs);
            for(IDataIO input : mappedInputs){
                if(input.getNecessity() == DataNecessity.MANDATORY){
                    flow.addUserRequiredInput(input);
                    currOutputs.add(input);
                }
                else
                    flow.addUserOptionalInput(input);
            }
            currOutputs.addAll(mappedOutputs);
        }

        return flow;
    }

    @Override
    public Flow get() {
        return flow;
    }
}