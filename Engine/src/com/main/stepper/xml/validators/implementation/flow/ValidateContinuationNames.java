package com.main.stepper.xml.validators.implementation.flow;

import com.main.stepper.xml.generated.ex2.STContinuation;
import com.main.stepper.xml.generated.ex2.STContinuations;
import com.main.stepper.xml.generated.ex2.STFlow;
import com.main.stepper.xml.validators.api.IValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ValidateContinuationNames implements IValidator {
    private List<STFlow> flows;

    public ValidateContinuationNames(List<STFlow> flows) {
        this.flows = flows;
    }

    @Override
    public List<String> validate() {
        List<String> errors = new ArrayList<>();

        for(STFlow stFlow : flows) {
            STContinuations stContinuations = stFlow.getSTContinuations();
            if (stContinuations != null) {
                for (STContinuation continuation : stContinuations.getSTContinuation()) {
                    Optional<String> possibleMatch = flows.stream().map(STFlow::getName).filter(name -> name.equals(continuation.getTargetFlow())).findFirst();
                    if(!possibleMatch.isPresent()){
                        errors.add("Flow: " + stFlow.getName() + " has a continuation flow that does not exist: " + continuation.getTargetFlow());
                    }
                }
            }
        }
        return errors;
    }
}
