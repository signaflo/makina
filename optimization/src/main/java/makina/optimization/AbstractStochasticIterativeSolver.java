package makina.optimization;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import makina.math.matrix.Vector;
import makina.math.matrix.VectorNorm;
import makina.math.matrix.Vectors;
import makina.optimization.function.AbstractStochasticFunction;

import java.util.function.Function;

/**
 * TODO: Generalize the support for regularization. I could add a addRegularization(RegularizationType) method to add
 * many and any kind of regularizations.
 *
 * @author Emmanouil Antonios Platanios
 */
abstract class AbstractStochasticIterativeSolver implements Solver {
    private static final Logger logger = LogManager.getFormatterLogger("Stochastic Optimization");

    private final int maximumNumberOfIterations;
    private final int maximumNumberOfIterationsWithNoPointChange;
    private final double pointChangeTolerance;
    private final boolean checkForPointConvergence;
    private final Function<Vector, Boolean> additionalCustomConvergenceCriterion;
    private final int batchSize;
    private final StochasticSolverStepSize stepSize;
    private final double[] stepSizeParameters;
    /** Indicates whether /(L_1/) regularization is used. */
    protected final boolean useL1Regularization;
    /** The /(L_1/) regularization weight used. This variable is only used when {@link #useL1Regularization} is set to
     * true. */
    protected final double l1RegularizationWeight;
    /** Indicates whether /(L_2/) regularization is used. */
    private final boolean useL2Regularization;
    /** The /(L_2/) regularization weight used. This variable is only used when {@link #useL2Regularization} is set
     * to true. */
    private final double l2RegularizationWeight;
    private final int loggingLevel;

    private double pointChange;
    private int numberOfIterationsWithNoPointChange = 0;
    private boolean pointConverged = false;

    final AbstractStochasticFunction objective;
    final Vector lowerBound;
    final Vector upperBound;

    Vector currentPoint;
    int currentIteration;
    Vector previousPoint;
    Vector currentGradient;
    Vector currentDirection;
    double currentStepSize;

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> {
        protected abstract T self();

        protected final AbstractStochasticFunction objective;
        protected final Vector initialPoint;

        protected Vector lowerBound = null;
        protected Vector upperBound = null;
        protected boolean sampleWithReplacement = true;
        protected int maximumNumberOfIterations = 10000;
        protected int maximumNumberOfIterationsWithNoPointChange = 1;
        protected double pointChangeTolerance = 1e-10;
        protected boolean checkForPointConvergence = true;
        private Function<Vector, Boolean> additionalCustomConvergenceCriterion = currentPoint -> false;
        protected int batchSize = 100;
        protected StochasticSolverStepSize stepSize = StochasticSolverStepSize.SCALED;
        protected double[] stepSizeParameters = new double[] { 10, 0.75 };
        private double l1RegularizationWeight = 0.0;
        private double l2RegularizationWeight = 0.0;
        private int loggingLevel = 0;

        protected AbstractBuilder(AbstractStochasticFunction objective, Vector initialPoint) {
            this.objective = objective;
            this.initialPoint = initialPoint;
        }

        public T lowerBound(double lowerBound) {
            this.lowerBound = Vectors.build(1, initialPoint.type());
            this.lowerBound.setAll(lowerBound);
            return self();
        }

        public T lowerBound(Vector lowerBound) {
            this.lowerBound = lowerBound;
            return self();
        }

        public T upperBound(double upperBound) {
            this.upperBound = Vectors.build(1, initialPoint.type());
            this.upperBound.setAll(upperBound);
            return self();
        }

        public T upperBound(Vector upperBound) {
            this.upperBound = upperBound;
            return self();
        }

        public T sampleWithReplacement(boolean sampleWithReplacement) {
            this.sampleWithReplacement = sampleWithReplacement;
            return self();
        }

        public T maximumNumberOfIterations(int maximumNumberOfIterations) {
            this.maximumNumberOfIterations = maximumNumberOfIterations;
            return self();
        }

        public T maximumNumberOfIterationsWithNoPointChange(int maximumNumberOfIterationsWithNoPointChange) {
            this.maximumNumberOfIterationsWithNoPointChange = maximumNumberOfIterationsWithNoPointChange;
            return self();
        }

        public T pointChangeTolerance(double pointChangeTolerance) {
            this.pointChangeTolerance = pointChangeTolerance;
            return self();
        }

        public T checkForPointConvergence(boolean checkForPointConvergence) {
            this.checkForPointConvergence = checkForPointConvergence;
            return self();
        }

        public T additionalCustomConvergenceCriterion(Function<Vector, Boolean> additionalCustomConvergenceCriterion) {
            this.additionalCustomConvergenceCriterion = additionalCustomConvergenceCriterion;
            return self();
        }

        public T batchSize(int batchSize) {
            this.batchSize = batchSize;
            return self();
        }

        public T stepSize(StochasticSolverStepSize stepSize) {
            this.stepSize = stepSize;
            return self();
        }

        public T stepSizeParameters(double... stepSizeParameters) {
            this.stepSizeParameters = stepSizeParameters;
            return self();
        }

        /**
         * Sets the {@link #l1RegularizationWeight} field that contains the value of the /(L_1/) regularization weight
         * used. This variable is only used when {@link #useL1Regularization} is set to true.
         *
         * @param   l1RegularizationWeight  The value to which to set the {@link #l1RegularizationWeight} field.
         * @return                          This builder object itself. That is done so that we can use a nice and
         *                                  expressive code format when we build objects using this builder class.
         */
        public T l1RegularizationWeight(double l1RegularizationWeight) {
            this.l1RegularizationWeight = l1RegularizationWeight;
            return self();
        }

        /**
         * Sets the {@link #l2RegularizationWeight} field that contains the value of the /(L_2/) regularization weight
         * used. This variable is only used when {@link #useL2Regularization} is set to true.
         *
         * @param   l2RegularizationWeight  The value to which to set the {@link #l2RegularizationWeight} field.
         * @return                          This builder object itself. That is done so that we can use a nice and
         *                                  expressive code format when we build objects using this builder class.
         */
        public T l2RegularizationWeight(double l2RegularizationWeight) {
            this.l2RegularizationWeight = l2RegularizationWeight;
            return self();
        }

        public T loggingLevel(int loggingLevel) {
            this.loggingLevel = loggingLevel;
            return self();
        }
    }

    public static class Builder extends AbstractBuilder<Builder> {
        public Builder(AbstractStochasticFunction objective, Vector initialPoint) {
            super(objective, initialPoint);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    protected AbstractStochasticIterativeSolver(AbstractBuilder<?> builder) {
        objective = builder.objective;
        lowerBound = builder.lowerBound;
        upperBound = builder.upperBound;
        objective.setSampleWithReplacement(builder.sampleWithReplacement);
        maximumNumberOfIterations = builder.maximumNumberOfIterations;
        maximumNumberOfIterationsWithNoPointChange = builder.maximumNumberOfIterationsWithNoPointChange;
        pointChangeTolerance = builder.pointChangeTolerance;
        checkForPointConvergence = builder.checkForPointConvergence;
        additionalCustomConvergenceCriterion = builder.additionalCustomConvergenceCriterion;
        batchSize = builder.batchSize;
        stepSize = builder.stepSize;
        stepSizeParameters = builder.stepSizeParameters;
        useL1Regularization = builder.l1RegularizationWeight > 0;
        l1RegularizationWeight = builder.l1RegularizationWeight;
        useL2Regularization = builder.l2RegularizationWeight > 0;
        l2RegularizationWeight = builder.l2RegularizationWeight;
        loggingLevel = builder.loggingLevel;
        currentPoint = builder.initialPoint;
        currentGradient = objective.getGradientEstimate(currentPoint, batchSize);
        currentIteration = 0;
    }

    @Override
    public Vector solve() {
        if (loggingLevel > 0)
            logger.info("Optimization is starting.");
        while (!checkTerminationConditions() && !additionalCustomConvergenceCriterion.apply(currentPoint)) {
            performIterationUpdates();
            currentIteration++;
            if ((loggingLevel == 1 && currentIteration % 1000 == 0)
                    || (loggingLevel == 2 && currentIteration % 100 == 0)
                    || (loggingLevel == 3 && currentIteration % 10 == 0)
                    || loggingLevel > 3)
                printIteration();
        }
        if (loggingLevel > 0)
            printTerminationMessage();
        return currentPoint;
    }

    public void updateStepSize() {
        currentStepSize = stepSize.compute(currentIteration, stepSizeParameters);
    }

    public void performIterationUpdates() {
        updateDirection();
        updateStepSize();
        previousPoint = currentPoint;
        updatePoint();
        handleBoxConstraints();
        currentGradient = objective.getGradientEstimate(currentPoint, batchSize);
        if (useL2Regularization)
            currentGradient.addInPlace(currentPoint.mult(2 * l2RegularizationWeight));
    }

    public boolean checkTerminationConditions() {
        if (currentIteration > 0) {
            if (currentIteration >= maximumNumberOfIterations)
                return true;
            if (checkForPointConvergence) {
                pointChange = currentPoint.sub(previousPoint).norm(VectorNorm.L2_FAST);
                numberOfIterationsWithNoPointChange =
                        (pointChange <= pointChangeTolerance) ? numberOfIterationsWithNoPointChange + 1 : 0;
                if (numberOfIterationsWithNoPointChange >= maximumNumberOfIterationsWithNoPointChange) {
                    pointConverged = true;
                }
            }
            return checkForPointConvergence && pointConverged;
        } else {
            return false;
        }
    }

    public void printIteration() {
        logger.info("Iteration #: %10d | Point Change: %20s", currentIteration, DECIMAL_FORMAT.format(pointChange));
    }

    public void printTerminationMessage() {
        if (pointConverged)
            logger.info("The L2 norm of the point change, %s, " +
                                "was below the convergence threshold of %s for more than %d iterations.",
                        DECIMAL_FORMAT.format(pointChange),
                        DECIMAL_FORMAT.format(pointChangeTolerance),
                        maximumNumberOfIterationsWithNoPointChange);
        if (currentIteration >= maximumNumberOfIterations)
            logger.info("Reached the maximum number of allowed iterations, %d.", maximumNumberOfIterations);
    }

    /**
     *
     *
     * Note: Care must be taken when implementing this method to include the relevant cases for when \(L_1\) or \(L_2\)
     * regularization is used.
     */
    public abstract void updateDirection();

    /**
     *
     *
     * Note 1: Care must be taken when implementing this method to include the relevant cases for when \(L_1\) or \(L_2\)
     * regularization is used.
     *
     * Note 2: Care must be taken when implementing this method because the previousPoint variable is simply updated to
     * point to currentPoint, at the beginning of each iteration. That means that when the new value is computed, a new
     * object has to be instantiated for holding that values.
     */
    public abstract void updatePoint();

    public abstract void handleBoxConstraints();
}
