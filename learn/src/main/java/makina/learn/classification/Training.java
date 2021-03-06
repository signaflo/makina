package makina.learn.classification;

import makina.learn.data.DataSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import makina.learn.data.LabeledDataInstance;
import makina.math.matrix.Vector;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Emmanouil Antonios Platanios
 */
public abstract class Training<T extends Vector, S> {
    private static final Logger logger = LogManager.getLogger("Classification / Training");

    private final LossFunction lossFunction;
    private final Function<LossFunctionArguments<S>, Double> customLossFunction;

    protected final TrainableClassifier.Builder<T, S> classifierBuilder;
    protected final DataSet<? extends LabeledDataInstance<T, S>> labeledDataSet;
    protected final List<Map.Entry<String, Object[]>> allowedParameterValues;
    protected final SearchMethod searchMethod;

    protected TrainableClassifier<T, S> bestClassifier = null;
    protected double bestClassifierLoss = Double.MAX_VALUE;

    protected static abstract class AbstractBuilder<B extends AbstractBuilder<B, T, S>, T extends Vector, S> {
        protected abstract B self();

        protected TrainableClassifier.Builder<T, S> classifierBuilder;
        protected DataSet<? extends LabeledDataInstance<T, S>> labeledDataSet;

        protected List<Map.Entry<String, Object[]>> allowedParameterValues = new ArrayList<>();
        protected SearchMethod searchMethod = SearchMethod.GRID_SEARCH;
        protected LossFunction lossFunction = LossFunction.MEAN_ZERO_ONE_LOSS;
        protected Function<LossFunctionArguments<S>, Double> customLossFunction = null;

        protected AbstractBuilder(TrainableClassifier.Builder<T, S> classifierBuilder,
                                  DataSet<? extends LabeledDataInstance<T, S>> trainingDataSet) {
            this.classifierBuilder = classifierBuilder;
            this.labeledDataSet = trainingDataSet;
        }

        public B addAllowedParameterValues(String parameterName, Object... values) {
            allowedParameterValues.add(new AbstractMap.SimpleEntry<>(parameterName, values));
            return self();
        }

        public B searchMethod(SearchMethod searchMethod) {
            this.searchMethod = searchMethod;
            return self();
        }

        public B lossFunction(LossFunction lossFunction) {
            this.lossFunction = lossFunction;
            return self();
        }

        public B lossFunction(Function<LossFunctionArguments<S>, Double> lossFunction) {
            this.customLossFunction = lossFunction;
            return self();
        }
    }

    public static class Builder<T extends Vector, S> extends AbstractBuilder<Builder<T, S>, T, S> {
        public Builder(TrainableClassifier.Builder<T, S> classifierBuilder,
                       DataSet<? extends LabeledDataInstance<T, S>> trainingDataSet) {
            super(classifierBuilder, trainingDataSet);
        }

        /** {@inheritDoc} */
        @Override
        protected Builder<T, S> self() {
            return this;
        }
    }

    protected Training(AbstractBuilder<?, T, S> builder) {
        classifierBuilder = builder.classifierBuilder;
        labeledDataSet = builder.labeledDataSet;
        allowedParameterValues = builder.allowedParameterValues;
        searchMethod = builder.searchMethod;
        lossFunction = builder.lossFunction;
        customLossFunction = builder.customLossFunction;
    }

    public TrainedClassifier train() {
        searchMethod.searchOverParameterValues(this);
        if (needsTrainingAfterSearch())
            bestClassifier.train(labeledDataSet);
        return new TrainedClassifier(bestClassifier, bestClassifierLoss);
    }

    protected double computeLoss(List<S> predictedLabels, List<S> trueLabels, int[] dataSetIndexes) {
        if (customLossFunction == null)
            return lossFunction.computeLoss(predictedLabels, trueLabels, dataSetIndexes);
        else
            return customLossFunction.apply(new LossFunctionArguments<>(predictedLabels, trueLabels, dataSetIndexes));
    }

    protected abstract double trainAndEvaluateClassifier(TrainableClassifier<T, S> classifier);
    protected abstract boolean needsTrainingAfterSearch();

    public enum SearchMethod {
        GRID_SEARCH {
            @Override
            protected <T extends Vector, S> void searchOverParameterValues(Training<T, S> training) {
                searchOverParameterValues(training, 0);
            }

            private <T extends Vector, S> void searchOverParameterValues(Training<T, S> training, int parameterIndex) {
                if (parameterIndex < training.allowedParameterValues.size()) {
                    String parameterName = training.allowedParameterValues.get(parameterIndex).getKey();
                    Object[] parameterValues = training.allowedParameterValues.get(parameterIndex).getValue();
                    for (Object parameterValue : parameterValues) {
                        training.classifierBuilder.setParameter(parameterName, parameterValue);
                        searchOverParameterValues(training, parameterIndex + 1);
                    }
                } else {
                    TrainableClassifier<T, S> classifier = training.classifierBuilder.build();
                    double classifierLoss = training.trainAndEvaluateClassifier(classifier);
                    if (classifierLoss < training.bestClassifierLoss) {
                        training.bestClassifier = classifier;
                        training.bestClassifierLoss = classifierLoss;
                    }
                }
            }
        },
        LOCAL_SEARCH {
            @Override
            protected <T extends Vector, S> void searchOverParameterValues(Training<T, S> training) {
                throw new UnsupportedOperationException();
            }
        };

        protected abstract <T extends Vector, S> void searchOverParameterValues(Training<T, S> training);
    }

    public enum LossFunction {
        MEAN_SQUARED_ERROR {
            @Override
            protected <S> double computeLoss(List<S> predictedLabels, List<S> trueLabels, int[] dataSetIndexes) {
                if (predictedLabels.size() != trueLabels.size())
                    throw new IllegalArgumentException("The two lists of labels must have the same length!");

                throw new UnsupportedOperationException();
            }
        },
        MEAN_ZERO_ONE_LOSS {
            @Override
            protected <S> double computeLoss(List<S> predictedLabels, List<S> trueLabels, int[] dataSetIndexes) {
                if (predictedLabels.size() != trueLabels.size())
                    throw new IllegalArgumentException("The two lists of labels must have the same length!");
                double loss = 0;
                for (int i = 0; i < predictedLabels.size(); i++)
                    if (!predictedLabels.get(i).equals(trueLabels.get(i)))
                        loss += 1;
                return loss / predictedLabels.size();
            }
        };

        protected abstract <S> double computeLoss(List<S> predictedLabels, List<S> trueLabels, int[] dataSetIndexes);
    }

    public class TrainedClassifier {
        private final TrainableClassifier<T, S> classifier;
        private final double loss;

        public TrainedClassifier(TrainableClassifier<T, S> classifier, double loss) {
            this.classifier = classifier;
            this.loss = loss;
        }

        public TrainableClassifier<T, S> getClassifier() {
            return classifier;
        }

        public double getLoss() {
            return loss;
        }
    }

    public static class LossFunctionArguments<S> {
        private final List<S> predictedLabels;
        private final List<S> trueLabels;
        private final int[] dataSetIndexes;

        public LossFunctionArguments(List<S> predictedLabels, List<S> trueLabels, int[] dataSetIndexes) {
            this.predictedLabels = predictedLabels;
            this.trueLabels = trueLabels;
            this.dataSetIndexes = dataSetIndexes;
        }

        public List<S> getPredictedLabels() {
            return predictedLabels;
        }

        public List<S> getTrueLabels() {
            return trueLabels;
        }

        public int[] getDataSetIndexes() {
            return dataSetIndexes;
        }
    }
}
