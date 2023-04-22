package com.main.stepper.application.implementation.console;

import com.main.stepper.application.api.IApplication;
import com.main.stepper.application.implementation.console.data.parser.DataParser;
import com.main.stepper.engine.data.api.IFlowInformation;
import com.main.stepper.engine.definition.api.IEngine;
import com.main.stepper.engine.definition.implementation.Engine;
import com.main.stepper.engine.executor.api.IFlowRunResult;
import com.main.stepper.engine.executor.implementation.ExecutionUserInputs;
import com.main.stepper.exceptions.data.BadReadException;
import com.main.stepper.exceptions.xml.XMLException;
import com.main.stepper.io.api.IDataIO;

import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class ConsoleApplication implements IApplication {
    public static void main(String[] args) {
        ConsoleApplication application = new ConsoleApplication(new Engine());
        while(application.run)
            application.presentMenu();
    }

    private boolean run = true;
    private final IEngine engine;
    private Scanner scanner;

    public ConsoleApplication(IEngine engine) {
        this.engine = engine;
        this.scanner = new Scanner(System.in);
    }

    private Integer getChoiceOfFlow(){
        List<String> flowNames = engine.getFlowNames();
        if(flowNames.size() == 0){
            System.out.println("There are no flows in the system.");
            return -1;
        }
        System.out.println("Please choose one of the following flows: (or enter '0' to exit)");

        for(int i = 0; i < flowNames.size(); i++)
            System.out.println((i + 1) + ") " + flowNames.get(i));

        String input;
        Integer choice = null;
        do {
            input = scanner.nextLine();
            try{
                choice = Integer.parseInt(input);
                if(choice < 0 || choice > flowNames.size())
                    throw new NumberFormatException();
            }catch (NumberFormatException e){
                System.out.println("Invalid input, please try again.");
                choice = null;
            }
        }while(choice == null);

        return choice;
    }

    @Override
    public void presentMenu() {
        System.out.println("Main menu, please choose one of the following options:");
        System.out.println("1) Read system from file.");
        System.out.println("2) Show flow information.");
        System.out.println("3) Execute flow.");
        System.out.println("4) Show full information about a past run.");
        System.out.println("5) Get system statistics.");
        System.out.println("6) Exit.");

        String input = scanner.nextLine();
        switch (input){
            case "1":
                readSystemFromFile();
                break;
            case "2":
                showFlowInformation();
                break;
            case "3":
                executeFlow();
                break;
            case "4":
                pastRunFullInformation();
                break;
            case "5":
                getSystemStatistics();
                break;
            case "6":
                exit();
                break;
            default:
                System.out.println("Invalid input, please try again.");
                break;
        }
        System.out.println();
    }

    @Override
    public void readSystemFromFile() {
        System.out.println("Please enter the path to the file:");
        String path = scanner.nextLine();
        List<String> errors = null;
        try{
            errors = engine.readSystemFromXML(path);
        } catch (XMLException e) {
            System.out.println("An error occurred while reading the file:");
            System.out.println(e.getMessage());
            return;
        }
        if(errors.size() > 0){
            System.out.println("The following errors were encountered:");
            for(String error : errors)
                System.out.println(error);
        }
        else
            System.out.println("The system was read successfully.");
    }

    @Override
    public void showFlowInformation() {
        Integer choice = getChoiceOfFlow();
        if(choice == 0)
            return;

        IFlowInformation flowInfo = engine.getFlowInfo(engine.getFlowNames().get(choice - 1));
        System.out.println("\nFlow information:");

        System.out.println("Name: " + flowInfo.name());

        System.out.println("Description: " + flowInfo.description());

        System.out.println("Formal outputs: ");
        flowInfo.formalOutputs().forEach(f->{
            System.out.println("\tName: " + f.getName() + ", Type: " + f.getDataDefinition().getName());
        });

        System.out.println("Read only: " + flowInfo.isReadOnly());

        System.out.println("Steps in flow:");
        flowInfo.steps().forEach(s->{
            System.out.println("\tName: " + s.step().getName() + (s.name().equals(s.step().getName()) ? "" : ", alias: " + s.name()) + ", read only: " + s.step().isReadOnly());
        });

        System.out.println("Open user inputs: ");
        flowInfo.openUserInputs().forEach(input->{
            System.out.println("\tName: " + input.getName() + ", Type: " + input.getDataDefinition().getName() + ", necessity: " + input.getNecessity());
            System.out.println("\tLinked steps: ");
            flowInfo.linkedSteps(input).forEach(step->{
                System.out.println("\t\tName: " + step.step().getName());
            });
            System.out.println();
        });

        System.out.println("Internal outputs: ");
        flowInfo.internalOutputs().forEach(output->{
            System.out.println("\tName: " + output.getName() + ", Type: " + output.getDataDefinition().getName());
            System.out.println("\tProduced by step: " + flowInfo.producer(output).name());
            System.out.println();
        });
    }

    @Override
    public void executeFlow() {
        Integer choice = getChoiceOfFlow();
        if(choice == 0)
            return;

        // Get required inputs to run the flow
        String flowName = engine.getFlowNames().get(choice - 1);
        ExecutionUserInputs inputs = engine.getExecutionUserInputs(flowName);
        // Read the inputs from the user
        Boolean run = false;
        Integer numInputs = inputs.getOpenUserInputs().size();
        while(!run){
            System.out.println("Free flow inputs: (or enter '0' to exit)");
            for(Integer i = 0; i < numInputs; i++){
                IDataIO input = inputs.getOpenUserInputs().get(i);
                System.out.println((i + 1) + ") " + input.getUserString()
                        + " (" + inputs.getStep(input).name() + ")"
                        + ", Type: " + input.getDataDefinition().getName()
                        + ", Necessity: " + input.getNecessity());
            }
            if(inputs.validateUserInputs())
                System.out.println(numInputs + 1 + ") Run flow.");

            String input;
            choice = null;
            do {
                input = scanner.nextLine();
                try{
                    choice = Integer.parseInt(input);
                    if(inputs.validateUserInputs() && choice == numInputs + 1){
                        run = true;
                    }
                    else if(choice < 0 || choice > inputs.getOpenUserInputs().size())
                        throw new NumberFormatException();
                }catch (NumberFormatException e){
                    System.out.println("Invalid input, please try again.");
                    choice = null;
                }
            }while(choice == null);

            if(choice == 0)
                return;

            if(!run){
                System.out.println("Please enter the value for the selected input:");
                input = scanner.nextLine();
                try {
                    inputs.readUserInput(inputs.getOpenUserInputs().get(choice - 1), input);
                } catch (BadReadException e) {
                    System.out.println("The input doesn't match the required type!");
                }
            }
        }

        System.out.println("Executing flow...");
        IFlowRunResult result = engine.runFlow(flowName, inputs);
        System.out.println("Flow run info:");
        System.out.println("\tFlow run id: " + result.runId());
        System.out.println("\tFlow name: " + result.name());
        System.out.println("\tFlow execution result flag: " + result.result());
        System.out.println("\tFlow formal outputs:");
        DataParser parser = DataParser.instance();
        for(IDataIO output : result.flowOutputs().keySet()){
            Object value = result.flowOutputs().get(output);
            // TODO: check to see if all types have a toString method.
            System.out.println("\t\t" + output.getUserString() + ": ");
            System.out.println("\t\t\t" + parser.parse(value).replaceAll("\n","\n\t\t\t"));
        }
    }

    @Override
    public void pastRunFullInformation() {
        System.out.println("Please enter the UUID of the run:");
        String uuid = scanner.nextLine();
        IFlowRunResult result = engine.getFlowRunInfo(UUID.fromString(uuid));
        if(result == null)
            System.out.println("No such run was found.");
        else
            System.out.println(result);
    }

    @Override
    public void getSystemStatistics() {
        // TODO: implement this method.
    }

    @Override
    public void exit() {
        System.out.println("Exiting...");
        run = false;
    }
}
