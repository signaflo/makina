package org.platanios.learn.data;

import org.platanios.learn.math.matrix.Vector;

import java.util.Map;

/**
 * @author Emmanouil Antonios Platanios
 */
public class MultiViewPredictedDataInstance<T extends Vector, S> extends MultiViewLabeledDataInstance<T, S> {
    protected double probability;

    public MultiViewPredictedDataInstance(String name, S label, Object source, double probability) {
        super(name, label, source);
        this.probability = probability;
    }

    public MultiViewPredictedDataInstance(String name, Map<Integer, T> features, S label, Object source, double probability) {
        super(name, features, label, source);
        this.probability = probability;
    }

    public double probability() {
        return probability;
    }

    public void probability(double probability) {
        this.probability = probability;
    }

    @Override
    public PredictedDataInstance<T, S> getSingleViewDataInstance(int view) {
        return new PredictedDataInstance<>(name, features.get(view), label, source, probability);
    }

    @Override
    protected PredictedDataInstanceBase<T, S> toDataInstanceBase() {
        return new PredictedDataInstanceBase<>(name, label, source, probability);
    }
}
