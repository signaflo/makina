package makina.experiment.graph;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import makina.learn.graph.Graph;
import makina.learn.graph.PageRankAlgorithm;
import makina.learn.graph.Vertex;
import makina.math.matrix.Vector;
import makina.math.matrix.Vectors;
import makina.learn.neural.graph.UpdateFunctionType;
import makina.learn.neural.graph.DeepGraph;
import makina.learn.neural.graph.VertexRankingDeepGraph;

import java.util.*;

/**
 * @author Emmanouil Antonios Platanios
 */
public class DeepPageRankExperiment {
    private final Logger logger = LogManager.getFormatterLogger("Deep PageRank Experiment");
    private final Map<Integer, Vertex<PageRankAlgorithm.VertexContentType, Void>> prVertexIndexesMap = new HashMap<>();
    private final Map<Integer, Vertex<DeepGraph.VertexContent, Void>> rnnVertexIndexesMap = new HashMap<>();
    private final Map<Integer, Vector> trainingData = new HashMap<>();
    private final Map<Integer, Double> prScores = new TreeMap<>();
    private final Map<Integer, Double> rnnScores = new TreeMap<>();

    private final String dataSetName;
    private final Graph<PageRankAlgorithm.VertexContentType, Void> prGraph;
    private final Graph<DeepGraph.VertexContent, Void> rnnGraph;

    public DeepPageRankExperiment(DataSets.DataSet dataSet) {
        dataSetName = dataSet.getName();
        logger.info("Number of vertices: " + dataSet.getVertexIndices().size());
        logger.info("Number of edges: " + dataSet.getEdges().size());
        prGraph = new Graph<>();
        rnnGraph = new Graph<>();
        for (int vertexIndex : dataSet.getVertexIndices()) {
            prVertexIndexesMap.put(vertexIndex, new Vertex<>(new PageRankAlgorithm.VertexContentType(vertexIndex, 0.0)));
            Vertex<DeepGraph.VertexContent, Void> rnnGraphVertex =
                    new Vertex<>(new DeepGraph.VertexContent(vertexIndex, 0, null, null, null, null));
            rnnVertexIndexesMap.put(vertexIndex, rnnGraphVertex);
        }
        for (DataSets.Edge edge : dataSet.getEdges()) {
            prGraph.addEdge(prVertexIndexesMap.get(edge.getSourceVertexIndex()), prVertexIndexesMap.get(edge.getDestinationVertexIndex()));
            rnnGraph.addEdge(rnnVertexIndexesMap.get(edge.getSourceVertexIndex()), rnnVertexIndexesMap.get(edge.getDestinationVertexIndex()));
        }
        logger.info("Finished generating graphs for the " + dataSetName + " data set.");
    }

    public void runPageRank() {
        logger.info("Running PageRank for the " + dataSetName + " data set.");
        PageRankAlgorithm pageRankAlgorithm =
                new PageRankAlgorithm.Builder<>(prGraph)
                        .dampingFactor(0.85)
                        .maximumNumberOfIterations(1000)
                        .checkForRankConvergence(false)
                        .loggingLevel(5)
                        .build();
        pageRankAlgorithm.computeRanks();
        for (Vertex<PageRankAlgorithm.VertexContentType, Void> vertex : prGraph.vertices())
            prScores.put(vertex.content().getId(), vertex.content().getRank());
        logger.info("Finished running PageRank for the " + dataSetName + " data set.");
    }

    public void sampleRNNTrainingData(double percentageOfTrainingData) {
        logger.info("Sampling RNN training data for the " + dataSetName + " data set.");
        final List<Map.Entry<Integer, Double>> entries = new ArrayList<>(prScores.entrySet());
        Collections.shuffle(entries);
        for (int sample = 0; sample < Math.floor(percentageOfTrainingData * prScores.size()); sample++)
            trainingData.put(entries.get(sample).getKey(), Vectors.dense(entries.get(sample).getValue()));
        logger.info("Finished sampling RNN training data for the " + dataSetName + " data set.");
    }

    public void runRNNExperiments(int[] featureVectorsSizes, int[] maximumNumberOfStepsToTry) {
        EvaluationResults[][] results = new EvaluationResults[featureVectorsSizes.length][maximumNumberOfStepsToTry.length];
        for (int i = 0; i < featureVectorsSizes.length; i++)
            for (int j = 0; j < maximumNumberOfStepsToTry.length; j++)
                results[i][j] = runRNNExperiment(featureVectorsSizes[i], maximumNumberOfStepsToTry[j]);
        logger.info("Results for the " + dataSetName + " data set:");
        for (int i = 0; i < featureVectorsSizes.length; i++)
            for (int j = 0; j < maximumNumberOfStepsToTry.length; j++)
                logger.info("\tF = %d, K = %d:\t { Train MSE: %20s | Test MSE: %20s | Overall MSE: %20s }",
                            featureVectorsSizes[i],
                            maximumNumberOfStepsToTry[j],
                            results[i][j].trainMSE,
                            results[i][j].testMSE,
                            results[i][j].overallMSE);
    }

    public EvaluationResults runRNNExperiment(int featureVectorsSize, int maximumNumberOfSteps) {
        VertexRankingDeepGraph<Void> graphRNNAlgorithm =
                new VertexRankingDeepGraph<>(featureVectorsSize, 1, maximumNumberOfSteps, rnnGraph, UpdateFunctionType.SIGMOID);
        logger.info("Training RNN for the " + dataSetName + " data set.");
        graphRNNAlgorithm.train(trainingData);
        logger.info("Finished training RNN for the " + dataSetName + " data set.");
        logger.info("Storing RNN results for the " + dataSetName + " data set.");
        for (Vertex<DeepGraph.VertexContent, Void> vertex : rnnGraph.vertices())
            rnnScores.put(vertex.content().id(), graphRNNAlgorithm.output(vertex).get(0));
        logger.info("Finished storing RNN results for the " + dataSetName + " data set.");
        EvaluationResults results = evaluateResults();
        graphRNNAlgorithm.resetGraph();
        return results;
    }

    private EvaluationResults evaluateResults() {
        double trainMSE = 0.0;
        double testMSE = 0.0;
        double overallMSE = 0.0;
        for (int vertexID : prScores.keySet()) {
            double loss = Math.pow(rnnScores.get(vertexID) - prScores.get(vertexID), 2);
            if (trainingData.containsKey(vertexID))
                trainMSE += loss;
            else
                testMSE += loss;
            overallMSE += loss;
        }
        trainMSE /= trainingData.size();
        testMSE /= (prScores.size() - trainingData.size());
        overallMSE /= prScores.size();
        return new EvaluationResults(trainMSE, testMSE, overallMSE);
    }

    public static class EvaluationResults {
        private final double trainMSE;
        private final double testMSE;
        private final double overallMSE;

        public EvaluationResults(double trainMSE, double testMSE, double overallMSE) {
            this.trainMSE = trainMSE;
            this.testMSE = testMSE;
            this.overallMSE = overallMSE;
        }

        public double getTrainMSE() {
            return trainMSE;
        }

        public double getTestMSE() {
            return testMSE;
        }

        public double getOverallMSE() {
            return overallMSE;
        }
    }

    public static void main(String[] args) {
//        DataSets.DataSet dataSet = DataSets.loadUnlabeledDataSet("/Users/Anthony/Downloads/Pokec Graph Data Set/soc-pokec-relationships.txt");
//        DataSets.DataSet dataSet = DataSets.loadTwitterSocialCirclesDataSet("/Users/Anthony/Downloads/Twitter Social Circles Data Set/twitter_combined.txt");
//        DataSets.DataSet dataSet = DataSets.loadUnlabeledDataSet("/Users/Anthony/Downloads/soc-Epinions1.txt");
//        DataSets.DataSet dataSet = DataSets.loadTwitterSocialCirclesDataSet("/Users/Anthony/Downloads/facebook_combined.txt");
//        DataSets.DataSet dataSet;
//        if (args[1].equals("0"))
//            dataSet = DataSets.loadUnlabeledDataSet(args[0]);
//        else
//            dataSet = DataSets.loadTwitterSocialCirclesDataSet(args[0]);
//        DeepPageRankExperiment experiment = new DeepPageRankExperiment(dataSet);
//        experiment.runPageRank();
//        experiment.sampleRNNTrainingData(0.8);
//        experiment.runRNNExperiments(new int[] { 1, 10 }, new int[] { 1, 2, 5, 10, 20, 50 });
    }
}
