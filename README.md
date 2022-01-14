## Program Synthesis for B in ProB

Program synthesis is the task of generating executable programs from a specification usually considering a domain specific language. There are many ways to specify the behavior of a program to be synthesized such as natural language, logical or mathematical formulae (e.g., in the form of pre- and post-conditions) or explicit input-output examples (called *Inductive Program Synthesis* or *Programming by Examples*).

The ProB Prolog core provides an implementation to synthesize B predicates or complete machine operations from explicit state input-output examples. The ProB Java API provides an interface to utilize the program synthesis backend.
The implemented synthesis technique is based on the work by Susmit Jha, Sumit Gulwani et al. [https://people.eecs.berkeley.edu/%7esseshia/pubdir/icse10-TR.pdf]
A synthesis task is encoded as a constraint satisfaction problem in B using the ProB constraint solver and its available backends to find valid solutions.

This repository demonstrates how to synthesize B predicates (including expressions) and B machine operations with the [ProB Java API](https://www3.hhu.de/stups/prob/index.php/ProB_2.0_Tutorial).

## Input-Output Examples
We deem input and output examples to refer to the state of a B machine, i.e., a subset of machine variables.
When synthesizing a B machine operation, the input-output examples describe explicit state transitions. For instance, given a single integer machine variable (set of machine variables is [x]), the example with input [1] and output [2] might describe an integer incrementation by one. In case of synthesizing B predicates, an example consists of an input state considering all variables that occur in the predicate. The output is either true or false depending on the predicate to be synthesized, i.e., the user provides positive and negative input examples.

## Synthesis of B Machine Operations
We use a sketch for synthesizing B machine operations describing a parallel substitution assigning single machine variables. The operation might define a precondition or parameters. The full sketch is specified as follows where the parts that are actually synthesized are: p1,..,pk, predicate, expression1,..,expressionj

```B
name(p1,..,pk) =
  PRE predicate THEN
    v1 := expression1 || ... || vj := expressionj
  END
```

## API
In order to use the synthesis backend, we expect the B machine to be loaded for which code should be synthesized.
The main class is **BSynthesizer** which expects the statespace of a currently loaded B machine on construction:
```Java
BSynthesizer synthesizer = new BSynthesizer(stateSpace);
```

Using input-output examples to specify the behavior of a program is most comfortable for the user but most difficult for program synthesis itself. As input-output examples possibly describe ambiguous behavior we provide two modes for synthesis:
* **FIRST_SOLUTION**: Return the first solution found.
* **INTERACTIVE**: Search for another non-equivalent program after finding a solution. There are three possible outcomes:
  * If the constraint solver finds a contradiction, we have found a unique solution and return the program synthesized so far.
  * If the constraint solver cannot find a solution because of exceeding the solver timeout, we return the program synthesized so far. On the one hand, this program might not be the one expected by the user if examples are missing and the present ones describe ambiguous behavior. On the other hand, if synthesis succeeds, a synthesized program always satisfies the provided examples. In practice, completeness with respect to the provided examples thus depends on the selected solver timeout.
  * If we find another non-equivalent program, we search for an example distinguishing both programs referred to as a *distinguishing example*. That is, an input state for which both programs yield different outputs. This example can be validated by the user and added to the set of examples for another run of synthesis possibly guiding synthesis to a unique solution. For instance, assume we want to synthesize a B predicate by providing a set of positive and negative examples and synthesis has found the predicate "x > 0" first. Assuming the examples describe ambiguous behavior synthesis may find another non-equivalent predicate "x > 1". A distinguishing example would then be "x = 1" as the first predicate is true and the second one is false for this input.

The synthesis mode can be set using the method **setSynthesisMode()**:
```Java
synthesizer.setSynthesisMode(SynthesisMode.INTERACTIVE);
```

We provide three classes to create examples:
* VariableExample.java: An example for a single machine variable consisting of the machine variable's name and its pretty printed B value.
* Example.java: An example for a machine state which is described by a set of variable examples.
* IOExample.java: An input-output example which is described by two examples for input and output respectively.

As mentioned above one can either synthesize a B predicate or a complete machine operation. To do so, a BSynthesizer object provides two methods **synthesizePredicate()** and **synthesizeOperation()**.

Synthesizing a B predicate expects a set of positive examples and a set of negative examples (Example.java).
For instance, assume we have loaded a machine that has an integer variable called "level" and want to synthesize the predicate "level > 0".
First, we create the set of positive and negative examples:
```Java
HashSet<Example> positiveExamples = new HashSet<>();
positiveExamples.add(new Example().addf(new VariableExample("level", "1")));
positiveExamples.add(new Example().addf(new VariableExample("level", "2")));
positiveExamples.add(new Example().addf(new VariableExample("level", "3")));

HashSet<Example> negativeExamples = new HashSet<>();
negativeExamples.add(new Example().addf(new VariableExample("level", "0")));
negativeExamples.add(new Example().addf(new VariableExample("level", "-1")));
negativeExamples.add(new Example().addf(new VariableExample("level", "-2")));
```
Note that the method addf() is just a wrapper for a Java set's add() method enabling a functional style.
Afterwards, we are able to run synthesis using synthesizePredicate():
```Java
try {
  BSynthesisResult solution = synthesizer.synthesizePredicate(positiveExamples, negativeExamples);
  if (solution.isProgram()) {
    SynthesizedProgram synthesizedProgram = (SynthesizedProgram) solution;
    System.out.println("Predicate: " + synthesizedProgram);
  }
} catch (BSynthesisException e) {
  //
}
```
A **BSynthesisResult** is either a program or a distinguishing example depending on the selected synthesis mode.
If synthesis fails, an exception is thrown providing an appropriate error message.

Now assume that we want to synthesize a B machine operation that increases the variable "level" by one. Again, we first create the set of positive and negative examples which are now examples for input and output:
```Java
HashSet<IOExample> positiveIOExamples = new HashSet<>();
positiveIOExamples.add(new IOExample(
    new Example().addf(new VariableExample("level", "0")),
    new Example().addf(new VariableExample("level", "1"))));
positiveIOExamples.add(new IOExample(
    new Example().addf(new VariableExample("level", "1")),
    new Example().addf(new VariableExample("level", "2"))));
positiveIOExamples.add(new IOExample(
    new Example().addf(new VariableExample("level", "2")),
    new Example().addf(new VariableExample("level", "3"))));
```
Afterwards, we are able to run synthesis:
```Java
try {
  BSynthesisResult solution =
      bSynthesizer.synthesizeOperation(positiveIOExamples, new HashSet<>());
  if (solution.isProgram()) {
    SynthesizedProgram synthesizedProgram = (SynthesizedProgram) solution;
    System.out.println("Operation: " + synthesizedProgram);
  }
} catch (BSynthesisException e) {
  //
}
```
Note that we do not provide any negative examples but pass an empty set. If we would provide negative input-output examples, an appropriate precondition would be synthesized for the machine operation if possible. That is, the synthesized operation is enabled for the inputs of the positive examples and disabled for the inputs of the negative examples.

The employed synthesis technique is based on the combination of program components and thus requires a predefined set of library components. For instance, an integer addition is such a library component. The main bottleneck for performance is the configuration of the set of components for a specific synthesis task. If using synthesis as described above, a default library configuration is used. This default configuration tries to use as little components as possible and successively mixes components or increases the amount of specific components if no solution can be found using the current library configuration. As this default library configuration is mainly selected randomly, synthesis possibly lacks for performance compared to using the exact library of components that is necessary to synthesize a program. To that effect, the user is also able to specify the exact library configuration to be considered during synthesis. We provide the class **BLibrary** to create a specific library configuration.

The enum LibraryComponentName provides all B components that are supported by the synthesis backend. Constructing a BLibrary object provides an empty library considerings the default configuration. We thus have to state that we want to use a specific library of components only and add the desired components using their names. For instance, we want to create a component library using an integer addition and subtraction:
```Java
BLibrary lib = new BLibrary();
lib.setUseDefaultLibrary(false);
lib.addLibraryComponent(LibraryComponentName.ADD);
lib.addLibraryComponent(LibraryComponentName.MINUS);
```
A BLibrary object can be passed as the third argument of synthesizePredicate() and synthesizeOperation() respectively.

The employed synthesis technique is based on constraint solving where each component has a unique output within a synthesized program. For instance, to synthesize the predicate "x + y + z > 2" we need two addition components in the library of components. There are three ways to adapt the amount how often a specific component should be considered:
* Use **addLibraryComponent(LibraryComponentName)** several times.
* Use **updateComponentAmount(LibraryComponentName,AddAmount)** adding the second argument to the amount of the specific component.
* Use **setComponentAmount(LibraryComponentName,Amount)** explicitly setting a component's amount.

Moreover, one can define whether synthesis should consider constants that have to be enumerated by the solver by using the method setEnumerateConstants(). If is false, only constants that are in the scope of the currently loaded machine are considered. For instance, if the current machine does not define any integer constant with the value 1 and we want to synthesize a predicate "x + 1 > y" the constraint solver needs to enumerate an integer variable to the constant value of 1 to achieve the desired behavior.

If synthesizing an operation, one can further define whether if-statements should be considered during synthesis represented by the enum **ConsiderIfType**. There are three possibilities:
* **NONE**: Do not consider if-statements.
* **EXPLICIT**: Use explicit if-then-else expressions as supported by ProB (this might be slow depending on the problem at hand).
* **IMPLICIT**: Do not use explicit if-statements but possibly synthesize several machine operations with appropriate preconditions instead (semantically equivalent to using explicit if-statements in a single machine operation).
