package org.platanios.learn.logic.grounding;

import org.platanios.learn.logic.LogicManager;
import org.platanios.learn.logic.formula.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Emmanouil Antonios Platanios
 */
public abstract class Grounding<T, R> {
    final LogicManager<T, R> logicManager;

    int numberOfComponents = 0;

    List<R> groundingTruthValues = new ArrayList<>();
    List<Variable<T>> groundedVariables = new ArrayList<>();
    List<List<T>> partialVariableGroundings = new ArrayList<>();
    List<List<GroundedPredicate<T, R>>> groundedPredicates = new ArrayList<>();
    List<Boolean> ruleUnobservedVariableIndicators = new ArrayList<>();
    R currentPredicateTruthValue;

    public Grounding(LogicManager<T, R> logicManager) {
        this.logicManager = logicManager;
        currentPredicateTruthValue = logicManager.logic().falseValue();
    }

    public void ground(Formula<T> formula) {
        numberOfComponents = ((Disjunction<T>) formula).getNumberOfComponents();
        ground(formula.toDisjunctiveNormalForm(), 0);
        List<List<T>> filteredPartialVariableGroundings = new ArrayList<>();
        List<List<GroundedPredicate<T, R>>> filteredGroundedPredicates = new ArrayList<>();
        for (int groundedRuleIndex = 0; groundedRuleIndex < groundedPredicates.size(); groundedRuleIndex++) {
            if (ruleUnobservedVariableIndicators.get(groundedRuleIndex)) {
                filteredPartialVariableGroundings.add(partialVariableGroundings.get(groundedRuleIndex));
                filteredGroundedPredicates.add(groundedPredicates.get(groundedRuleIndex));
            }
        }
        partialVariableGroundings = filteredPartialVariableGroundings;
        groundedPredicates = filteredGroundedPredicates;
    }

    @SuppressWarnings("unchecked")
    void ground(Formula<T> formula, int callNumber) {
        if (callNumber != 0 && partialVariableGroundings.size() == 0)
            return;
        if (formula instanceof Atom || formula instanceof Negation) {
            List<Variable<T>> argumentVariables = formula
                    .getVariables()
                    .stream()
                    .collect(Collectors.toList());
            List<Variable<T>> newGroundedVariables = new ArrayList<>(groundedVariables);
            List<R> candidateGroundingTruthValues = new ArrayList<>(groundingTruthValues);
            List<List<T>> candidateVariableGroundings = new ArrayList<>(partialVariableGroundings);
            List<List<GroundedPredicate<T, R>>> candidateGroundedPredicates = new ArrayList<>(groundedPredicates);
            List<Boolean> candidateRuleUnobservedVariableIndicators = new ArrayList<>(ruleUnobservedVariableIndicators);
            argumentVariables.stream()
                    .filter(argumentVariable -> !groundedVariables.contains(argumentVariable))
                    .forEach(argumentVariable -> {
                        candidateGroundingTruthValues.clear();
                        candidateVariableGroundings.clear();
                        candidateGroundedPredicates.clear();
                        candidateRuleUnobservedVariableIndicators.clear();
                        newGroundedVariables.add(argumentVariable);
                        if (partialVariableGroundings.size() > 0) {
                            for (int index = 0; index < partialVariableGroundings.size(); index++) {
                                for (T variableValue : logicManager.getVariableValues(argumentVariable)) {
                                    List<T> variableGrounding = new ArrayList<>(partialVariableGroundings.get(index));
                                    variableGrounding.add(variableValue);
                                    candidateGroundingTruthValues.add(groundingTruthValues.get(index));
                                    candidateVariableGroundings.add(variableGrounding);
                                    candidateGroundedPredicates.add(new ArrayList<>(groundedPredicates.get(index)));
                                    candidateRuleUnobservedVariableIndicators.add(ruleUnobservedVariableIndicators.get(index));
                                }
                            }
                        } else {
                            for (T variableValue : logicManager.getVariableValues(argumentVariable)) {
                                List<T> variableGrounding = new ArrayList<>();
                                variableGrounding.add(variableValue);
                                candidateGroundingTruthValues.add(logicManager.logic().falseValue());
                                candidateVariableGroundings.add(variableGrounding);
                                candidateGroundedPredicates.add(new ArrayList<>());
                                candidateRuleUnobservedVariableIndicators.add(false);
                            }
                        }
                        groundingTruthValues = new ArrayList<>(candidateGroundingTruthValues);
                        partialVariableGroundings = new ArrayList<>(candidateVariableGroundings);
                        groundedPredicates = new ArrayList<>(candidateGroundedPredicates);
                        ruleUnobservedVariableIndicators = new ArrayList<>(candidateRuleUnobservedVariableIndicators);
                    });
            List<R> truthValues = new ArrayList<>(groundingTruthValues);
            groundedVariables = newGroundedVariables;
            groundingTruthValues = new ArrayList<>();
            partialVariableGroundings = new ArrayList<>();
            groundedPredicates = new ArrayList<>();
            ruleUnobservedVariableIndicators = new ArrayList<>();
            for (int candidateIndex = 0; candidateIndex < candidateGroundedPredicates.size(); candidateIndex++) {
                List<T> candidateVariableGrounding = candidateVariableGroundings.get(candidateIndex);
                Map<Variable<T>, T> variableAssignments = new HashMap<>();
                for (int variableIndex = 0; variableIndex < groundedVariables.size(); variableIndex++)
                    variableAssignments.put(groundedVariables.get(variableIndex), candidateVariableGrounding.get(variableIndex));
                List<R> disjunctionComponentsSoFar = new ArrayList<>();
                disjunctionComponentsSoFar.add(truthValues.get(candidateIndex));
                if (formula instanceof Atom)
                    currentPredicateTruthValue = formula.evaluate(logicManager, variableAssignments);
                else
                    currentPredicateTruthValue = ((Negation<T>) formula).getFormula().evaluate(logicManager, variableAssignments);
                if (!pruneGroundingAndSetCurrentPredicateTruthValue(formula, variableAssignments, disjunctionComponentsSoFar)) {
                    groundingTruthValues.add(currentPredicateTruthValue);
                    partialVariableGroundings.add(candidateVariableGrounding);
                    GroundedPredicate<T, R> groundedPredicate;
                    Predicate<T> predicate;
                    if (formula instanceof Atom)
                        predicate = ((Atom<T>) formula).getPredicate();
                    else
                        predicate = ((Atom<T>) ((Negation<T>) formula).getFormula()).getPredicate();
                    candidateVariableGrounding = new ArrayList<>();
                    for (Variable<T> variable : formula.getOrderedVariables())
                        candidateVariableGrounding.add(variableAssignments.get(variable));
                    boolean unobservedVariable = candidateRuleUnobservedVariableIndicators.get(candidateIndex);
                    if (logicManager.groundedPredicateExists(predicate,
                                                             candidateVariableGrounding)) {
                        groundedPredicate = logicManager.getGroundedPredicate(
                                predicate,
                                candidateVariableGrounding
                        );
                    } else {
                        groundedPredicate = logicManager.addGroundedPredicate(
                                predicate,
                                candidateVariableGrounding
                        );
                    }
                    candidateGroundedPredicates.get(candidateIndex).add(groundedPredicate);
                    groundedPredicates.add(candidateGroundedPredicates.get(candidateIndex));
                    ruleUnobservedVariableIndicators.add(unobservedVariable | groundedPredicate.getValue() == null);
                    onGroundedPredicateAddition(candidateGroundedPredicates.get(candidateIndex));
                }
            }
        } else if (formula instanceof Conjunction) {
            if (callNumber == 0)
                throw new IllegalStateException("The formula being grounded was not converted to valid disjunctive " +
                                                        "normal form for some unknown reason.");

            throw new UnsupportedOperationException();
//            int numberOfComponents = ((Conjunction<T>) formula).getNumberOfComponents();
//            for (int componentIndex = 0; componentIndex < numberOfComponents; componentIndex++) {
//                Formula<T> componentFormula = ((Conjunction<T>) formula).getComponent(componentIndex);
//                if (componentFormula instanceof Atom || componentFormula instanceof Negation) {
//                    groundAtomOrNegation(formula, truthValues, candidateVariableGroundings, candidatePredicateGroundings);
//                } else {
//                    throw new IllegalStateException("The formula being grounded was not converted to valid disjunctive " +
//                                                            "normal form for some unknown reason.");
//                }
//            }
        } else if (formula instanceof Disjunction) {
            if (callNumber != 0)
                throw new IllegalStateException("The formula being grounded was not converted to valid disjunctive " +
                                                        "normal form for some unknown reason.");

            int numberOfComponents = ((Disjunction<T>) formula).getNumberOfComponents();
            for (int componentIndex = 0; componentIndex < numberOfComponents; componentIndex++) {
                Formula<T> componentFormula = ((Disjunction<T>) formula).getComponent(componentIndex);
                if (componentFormula instanceof Atom || componentFormula instanceof Negation) {
                    ground(componentFormula, callNumber++);
                } else {
                    throw new IllegalStateException("The formula being grounded was not converted to valid disjunctive " +
                                                            "normal form for some unknown reason.");
                }
            }
        } else {
            throw new IllegalStateException("The formula being grounded was not converted to valid disjunctive " +
                                                    "normal form for some unknown reason.");
        }
    }

    abstract boolean pruneGroundingAndSetCurrentPredicateTruthValue(Formula<T> formula,
                                                                    Map<Variable<T>, T> variableAssignments,
                                                                    List<R> disjunctionComponentsSoFar);

    void onGroundedPredicateAddition(List<GroundedPredicate<T, R>> groundedPredicate) {

    }

    public List<List<GroundedPredicate<T, R>>> getGroundedPredicates() {
        return groundedPredicates;
    }
}
