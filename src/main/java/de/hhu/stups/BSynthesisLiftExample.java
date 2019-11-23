package de.hhu.stups;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import de.prob.MainModule;
import de.prob.scripting.Api;
import de.prob.scripting.ModelTranslationError;
import de.prob.statespace.StateSpace;
import de.prob.synthesis.*;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

class BSynthesisLiftExample {

  private final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

  private StateSpace stateSpace;


  BSynthesisLiftExample() {
    Injector injector =
        Guice.createInjector(Stage.PRODUCTION, new BSynthesisModule(), new MainModule());
    final Api api = injector.getInstance(Api.class);
    final URL machineUrl = getClass().getClassLoader().getResource("Lift.mch");
    if (machineUrl == null) {
      return;
    }
    try {
      stateSpace = api.b_load(machineUrl.getPath());
    } catch (IOException | ModelTranslationError e) {
      logger.log(Level.SEVERE, "Machine could not be loaded. See ProB log file.");
    }
  }

  void killStateSpace() {
    stateSpace.kill();
  }

  String synthesizeOperationLiftUp() {
    BSynthesizer synthesizer = new BSynthesizer(stateSpace);
    synthesizer.setSynthesisMode(SynthesisMode.FIRST_SOLUTION);
    HashSet<IOExample> positiveIOExamples = new HashSet<>();
    positiveIOExamples.add(new IOExample(
        new Example().addf(new VariableExample("floor", "0")),
        new Example().addf(new VariableExample("floor", "1"))));
    positiveIOExamples.add(new IOExample(
        new Example().addf(new VariableExample("floor", "1")),
        new Example().addf(new VariableExample("floor", "2"))));
    positiveIOExamples.add(new IOExample(
        new Example().addf(new VariableExample("floor", "2")),
        new Example().addf(new VariableExample("floor", "3"))));
    try {
      BSynthesisResult solution =
          synthesizer.synthesizeOperation(positiveIOExamples, new HashSet<>());
      if (solution.isProgram()) {
        SynthesizedProgram synthesizedProgram = (SynthesizedProgram) solution;
        return synthesizedProgram.toString();
      }
    } catch (BSynthesisException e) {
      logger.log(Level.SEVERE, e.getMsg());
    }
    return "";
  }

  String synthesizePredicateLift() {
    BSynthesizer synthesizer = new BSynthesizer(stateSpace);
    synthesizer.setSynthesisMode(SynthesisMode.FIRST_SOLUTION);
    HashSet<Example> positiveExamples = new HashSet<>();
    positiveExamples.add(new Example().addf(new VariableExample("floor", "1")));
    positiveExamples.add(new Example().addf(new VariableExample("floor", "2")));
    positiveExamples.add(new Example().addf(new VariableExample("floor", "3")));

    HashSet<Example> negativeExamples = new HashSet<>();
    negativeExamples.add(new Example().addf(new VariableExample("floor", "0")));
    negativeExamples.add(new Example().addf(new VariableExample("floor", "-1")));
    negativeExamples.add(new Example().addf(new VariableExample("floor", "-2")));
    try {
      BSynthesisResult solution =
          synthesizer.synthesizePredicate(positiveExamples, negativeExamples);
      if (solution.isProgram()) {
        SynthesizedProgram synthesizedProgram = (SynthesizedProgram) solution;
        return synthesizedProgram.toString();
      }
    } catch (BSynthesisException e) {
      logger.log(Level.SEVERE, e.getMsg());
    }
    return "";
  }
}
