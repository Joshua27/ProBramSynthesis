package de.hhu.stups;

public class Main {

  public static void main(String[] args) {
    BSynthesisLiftExample bSynthesisLiftExample = new BSynthesisLiftExample();
    String operation = bSynthesisLiftExample.synthesizeOperationLiftUp();
    String predicate = bSynthesisLiftExample.synthesizePredicateLift();

    System.out.println("Operation: " + operation);
    System.out.println("Predicate: " + predicate);
    bSynthesisLiftExample.killStateSpace();
  }
}
