package makina.learn.classification;

import makina.learn.data.PredictedDataInstance;
import makina.math.matrix.Vector;
import makina.math.matrix.VectorType;
import makina.math.matrix.Vectors;
import makina.utilities.UnsafeSerializationUtilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.io.OutputStream;

/**
 * This abstract class provides some functionality that is common to all binary logistic regression classes. All those
 * classes should extend this class.
 *
 * TODO: Add bias term.
 *
 * Note that the two class labels are represented using the integer values of 0 and 1.
 *
 * @author Emmanouil Antonios Platanios
 */
public class LogisticRegressionPrediction implements Classifier<Vector, Double> {
    /** The number of features used. */
    protected int numberOfFeatures;
    /** Indicates whether sparse vectors are being used or not. */
    protected boolean sparse;
    /** Indicates whether a separate bias term must be used along with the feature weights. Note that if a computeValue of
     * 1 has already been appended to all feature vectors, then there is no need for a bias term. */
    protected boolean useBiasTerm;

    /** The weights (i.e., parameters) used by this logistic regression model. Note that the size of this vector is
     * equal to 1 + {@link #numberOfFeatures}. */
    protected Vector weights;

    /**
     * This abstract class needs to be extended by the builders of all binary logistic regression classes. It provides
     * an implementation for those parts of those builders that are common. This is basically part of a small "hack" so
     * that we can have inheritable builder classes.
     *
     * @param   <T> This type corresponds to the type of the final object to be built. That is, the super class of the
     *              builder class that extends this class.
     */
    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> {
        /** A self-reference to this builder class. This is basically part of a small "hack" so that we can have
         * inheritable builder classes. */
        protected abstract T self();

        /** The number of features used. */
        protected int numberOfFeatures;
        /** Indicates whether sparse vectors should be used or not. */
        private boolean sparse = false;
        /** Indicates whether a separate bias term must be used along with the feature weights. Note that if a computeValue of
         * 1 has already been appended to all feature vectors, then there is no need for a bias term. */
        protected boolean useBiasTerm = true;
        /** The weights (i.e., parameters) used by this logistic regression model. Note that the size of this vector is
         * equal to 1 + {@link #numberOfFeatures}. */
        protected Vector weights = null;

        protected AbstractBuilder() { }

        protected AbstractBuilder(int numberOfFeatures, Vector weights) {
            this.numberOfFeatures = numberOfFeatures;
            this.weights = weights;
        }

        /**
         * Sets the {@link #sparse} field that indicates whether sparse vectors should be used.
         *
         * @param   sparse  The computeValue to which to set the {@link #sparse} field.
         * @return          This builder object itself. That is done so that we can use a nice and expressive code
         *                  format when we build objects using this builder class.
         */
        public T sparse(boolean sparse) {
            this.sparse = sparse;
            return self();
        }

        /**
         * Sets the {@link #useBiasTerm} field that indicates whether a separate bias term must be used along with the
         * feature weights. Note that if a computeValue of 1 has already been appended to all feature vectors, then there is no
         * need for a bias term.
         *
         * @param   useBiasTerm The computeValue to which to set the {@link #useBiasTerm} field.
         * @return              This builder object itself. That is done so that we can use a nice and expressive code
         *                      format when we build objects using this builder class.
         */
        public T useBiasTerm(boolean useBiasTerm) {
            this.useBiasTerm = useBiasTerm;
            return self();
        }

        public T setParameter(String name, Object value) {
            switch (name) {
                case "sparse":
                    sparse = (boolean) value;
                    break;
                case "useBiasTerm":
                    useBiasTerm = (boolean) value;
                    break;
                default:
                    break;
            }
            return self();
        }

        public LogisticRegressionPrediction build() {
            return new LogisticRegressionPrediction(this);
        }
    }

    /**
     * The builder class for this abstract class. This is basically part of a small "hack" so that we can have
     * inheritable builder classes.
     */
    public static class Builder extends AbstractBuilder<Builder> {
        public Builder(int numberOfFeatures, Vector weights) {
            super(numberOfFeatures, weights);
        }

        /** {@inheritDoc} */
        @Override
        protected Builder self() {
            return this;
        }
    }

    /**
     * Constructs a binary logistic regression object given an appropriate builder object. This constructor can only be
     * used from within the builder class of this class.
     *
     * @param   builder The builder object to use.
     */
    protected LogisticRegressionPrediction(AbstractBuilder<?> builder) {
        numberOfFeatures = builder.numberOfFeatures;
        sparse = builder.sparse;
        useBiasTerm = builder.useBiasTerm;
        if (builder.weights != null)
            weights = builder.weights;
        else
            if (builder.sparse)
                weights = Vectors.build(useBiasTerm ? numberOfFeatures + 1 : numberOfFeatures, VectorType.SPARSE);
            else
                weights = Vectors.build(useBiasTerm ? numberOfFeatures + 1 : numberOfFeatures, VectorType.DENSE);
    }

    @Override
    public ClassifierType type() {
        return ClassifierType.LOGISTIC_REGRESSION_PREDICTION;
    }

    /**
     * Predict the probability of the class label being 1 for some data instance.
     *
     * @param   dataInstance    The data instance for which the probability is computed.
     * @return                  The probability of the class label being 1 for the given data instance.
     */
    @Override
    public PredictedDataInstance<Vector, Double> predictInPlace(PredictedDataInstance<Vector, Double> dataInstance) {
        double probability = useBiasTerm ?
                1 / (1 + Math.exp(-weights.dotPlusConstant(dataInstance.features()))) :
                1 / (1 + Math.exp(-weights.dot(dataInstance.features())));
        if (probability >= 0.5) {
            dataInstance.probability(probability);
            dataInstance.label((double) 1);
        } else {
            dataInstance.probability(1 - probability);
            dataInstance.label((double) 0);
        }
        return dataInstance;
    }

    /** {@inheritDoc} */
    @Override
    public void write(OutputStream outputStream, boolean includeType) throws IOException {
        if (includeType)
            UnsafeSerializationUtilities.writeInt(outputStream, type().ordinal());
        UnsafeSerializationUtilities.writeInt(outputStream, numberOfFeatures);
        UnsafeSerializationUtilities.writeBoolean(outputStream, sparse);
        weights.write(outputStream, true);
    }

    public static LogisticRegressionPrediction read(InputStream inputStream, boolean includeType) throws IOException {
        if (includeType) {
            ClassifierType classifierType = ClassifierType.values()[UnsafeSerializationUtilities.readInt(inputStream)];
            if (!ClassifierType.LOGISTIC_REGRESSION_PREDICTION
                    .getStorageCompatibleTypes()
                    .contains(classifierType))
                throw new InvalidObjectException("The stored classifier is of type " + classifierType.name() + "!");
        }
        int numberOfFeatures = UnsafeSerializationUtilities.readInt(inputStream);
        boolean sparse = UnsafeSerializationUtilities.readBoolean(inputStream);
        Vector weights = Vectors.build(inputStream);
        return new Builder(numberOfFeatures, weights).sparse(sparse).build();
    }
}
