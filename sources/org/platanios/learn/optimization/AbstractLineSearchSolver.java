package org.platanios.learn.optimization;

import org.platanios.learn.math.matrix.CholeskyDecomposition;
import org.platanios.learn.math.matrix.Matrix;
import org.platanios.learn.optimization.function.AbstractFunction;
import org.platanios.learn.optimization.function.QuadraticFunction;
import org.platanios.learn.optimization.linesearch.*;

/**
 * TODO: Add a "set-line-search" option in the builders of all classes that inherit from this class.
 *
 * @author Emmanouil Antonios Platanios
 */
abstract class AbstractLineSearchSolver extends AbstractIterativeSolver {
    /** Default value: If quadratic or linear function it is ExactLineSearch, otherwise it is StrongWolfeLineSearch
     * with CONSERVE_FIRST_ORDER_CHANGE for the step size initialization method. */
    LineSearch lineSearch;

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>>
            extends AbstractIterativeSolver.AbstractBuilder<T> {
        protected LineSearch lineSearch;

        protected AbstractBuilder(AbstractFunction objective,
                                  double[] initialPoint) {
            super(objective, initialPoint);

            if (objective instanceof QuadraticFunction) {
                Matrix quadraticFactorMatrix = ((QuadraticFunction) objective).getA();
                CholeskyDecomposition choleskyDecomposition = new CholeskyDecomposition(quadraticFactorMatrix);
                if (choleskyDecomposition.isSymmetricAndPositiveDefinite()) {
                    lineSearch = new ExactLineSearch((QuadraticFunction) objective);
                    return;
                }
            }

            lineSearch = new StrongWolfeInterpolationLineSearch(objective, 1e-4, 0.9, 10);
            ((StrongWolfeInterpolationLineSearch) lineSearch)
                    .setStepSizeInitializationMethod(StepSizeInitializationMethod.CONSERVE_FIRST_ORDER_CHANGE);
        }

        public T lineSearch(LineSearch lineSearch) {
            this.lineSearch = lineSearch;
            return self();
        }
    }

    public static class Builder extends AbstractBuilder<Builder> {
        public Builder(AbstractFunction objective,
                       double[] initialPoint) {
            super(objective, initialPoint);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    protected AbstractLineSearchSolver(AbstractBuilder<?> builder) {
        super(builder);
        lineSearch = builder.lineSearch;
        previousPoint = currentPoint;
        previousGradient = currentGradient;
    }

    @Override
    public void performIterationUpdates() {
        previousDirection = currentDirection;
        updateDirection();
        previousStepSize = currentStepSize;
        updateStepSize();
        previousPoint = currentPoint;
        updatePoint();
        previousGradient = currentGradient;
        currentGradient = objective.getGradient(currentPoint);
        previousObjectiveValue = currentObjectiveValue;
        currentObjectiveValue = objective.getValue(currentPoint);
    }

    public void updateStepSize() {
        currentStepSize = lineSearch.computeStepSize(currentPoint,
                                                     currentDirection,
                                                     previousPoint,
                                                     previousDirection,
                                                     previousStepSize);
    }

    public abstract void updateDirection();
    public abstract void updatePoint();
}
