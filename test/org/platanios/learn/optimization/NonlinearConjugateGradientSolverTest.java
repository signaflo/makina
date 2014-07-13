package org.platanios.learn.optimization;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Emmanouil Antonios Platanios
 */
public class NonlinearConjugateGradientSolverTest {
    @Test
    public void testFletcherReevesSolver() {
        System.out.println("Rosenbrock Function Fletcher-Reeves No-Restart Solver:\n");
        NonlinearConjugateGradientSolver fletcherReevesSolver =
                new NonlinearConjugateGradientSolver.Builder(new RosenbrockFunction(), new double[] { -1.2, 1 })
                        .method(NonlinearConjugateGradientSolver.Method.FLETCHER_RIEVES)
                        .restartMethod(NonlinearConjugateGradientSolver.RestartMethod.NO_RESTART)
                        .build();
        double[] actualResult = fletcherReevesSolver.solve().getArray();
        double[] expectedResult = new double[] { 1, 1 };
        Assert.assertArrayEquals(expectedResult, actualResult, 1e-2);

        System.out.println("Rosenbrock Function Fletcher-Reeves N-Step-Restart Solver:\n");
        fletcherReevesSolver =
                new NonlinearConjugateGradientSolver.Builder(new RosenbrockFunction(), new double[] { -1.2, 1 })
                        .method(NonlinearConjugateGradientSolver.Method.FLETCHER_RIEVES)
                        .restartMethod(NonlinearConjugateGradientSolver.RestartMethod.N_STEP)
                        .build();
        actualResult = fletcherReevesSolver.solve().getArray();
        Assert.assertArrayEquals(expectedResult, actualResult, 1e-2);

        System.out.println("Rosenbrock Function Fletcher-Reeves Gradients-Orthogonality-Check-Restart Solver:\n");
        fletcherReevesSolver =
                new NonlinearConjugateGradientSolver.Builder(new RosenbrockFunction(), new double[] { -1.2, 1 })
                        .method(NonlinearConjugateGradientSolver.Method.FLETCHER_RIEVES)
                        .restartMethod(NonlinearConjugateGradientSolver.RestartMethod.GRADIENTS_ORTHOGONALITY_CHECK)
                        .build();
        actualResult = fletcherReevesSolver.solve().getArray();
        Assert.assertArrayEquals(expectedResult, actualResult, 1e-2);
    }

    @Test
    public void testPolakRibiereSolver() {
//        System.out.println("Rosenbrock Function Polak-Ribiere Solver:\n");
//        NonlinearConjugateGradientSolver polakRibiereSolver =
//                new NonlinearConjugateGradientSolver.Builder(new RosenbrockFunction(), new double[] { -1.2, 1 })
//                        .method(NonlinearConjugateGradientSolver.Method.POLAK_RIBIERE)
//                        .restartMethod(NonlinearConjugateGradientSolver.RestartMethod.NO_RESTART)
//                        .build();
//        double[] actualResult = polakRibiereSolver.solve().getArray();
        double[] expectedResult = new double[] { 1, 1 };
//        Assert.assertArrayEquals(expectedResult, actualResult, 1e-2);

//        System.out.println("Rosenbrock Function Polak-Ribiere N-Step-Restart Solver:\n");
//        polakRibiereSolver =
//                new NonlinearConjugateGradientSolver.Builder(new RosenbrockFunction(), new double[] { -1.2, 1 })
//                        .method(NonlinearConjugateGradientSolver.Method.POLAK_RIBIERE)
//                        .restartMethod(NonlinearConjugateGradientSolver.RestartMethod.N_STEP)
//                        .build();
//        actualResult = polakRibiereSolver.solve().getArray();
//        Assert.assertArrayEquals(expectedResult, actualResult, 1e-2);

        System.out.println("Rosenbrock Function Polak-Ribiere Gradients-Orthogonality-Check-Restart Solver:\n");
        NonlinearConjugateGradientSolver polakRibiereSolver =
                new NonlinearConjugateGradientSolver.Builder(new RosenbrockFunction(), new double[] { -1.2, 1 })
                        .method(NonlinearConjugateGradientSolver.Method.POLAK_RIBIERE)
                        .restartMethod(NonlinearConjugateGradientSolver.RestartMethod.GRADIENTS_ORTHOGONALITY_CHECK)
                        .build();
        double[] actualResult = polakRibiereSolver.solve().getArray();
        Assert.assertArrayEquals(expectedResult, actualResult, 1e-2);
    }

    @Test
    public void testPolakRibierePlusSolver() {
//        System.out.println("Rosenbrock Function Polak-Ribiere+ Solver:\n");
//        NonlinearConjugateGradientSolver polakRibierePlusSolver =
//                new NonlinearConjugateGradientSolver.Builder(new RosenbrockFunction(), new double[] { -1.2, 1 })
//                        .method(NonlinearConjugateGradientSolver.Method.POLAK_RIBIERE_PLUS)
//                        .restartMethod(NonlinearConjugateGradientSolver.RestartMethod.NO_RESTART)
//                        .build();
//        double[] actualResult = polakRibierePlusSolver.solve().getArray();
        double[] expectedResult = new double[] { 1, 1 };
//        Assert.assertArrayEquals(expectedResult, actualResult, 1e-2);

//        System.out.println("Rosenbrock Function Polak-Ribiere+ N-Step-Restart Solver:\n");
//        polakRibierePlusSolver =
//                new NonlinearConjugateGradientSolver.Builder(new RosenbrockFunction(), new double[] { -1.2, 1 })
//                        .method(NonlinearConjugateGradientSolver.Method.POLAK_RIBIERE_PLUS)
//                        .restartMethod(NonlinearConjugateGradientSolver.RestartMethod.N_STEP)
//                        .build();
//        actualResult = polakRibierePlusSolver.solve().getArray();
//        Assert.assertArrayEquals(expectedResult, actualResult, 1e-2);

        System.out.println("Rosenbrock Function Polak-Ribiere+ Gradients-Orthogonality-Check-Restart Solver:\n");
        NonlinearConjugateGradientSolver polakRibierePlusSolver =
                new NonlinearConjugateGradientSolver.Builder(new RosenbrockFunction(), new double[] { -1.2, 1 })
                        .method(NonlinearConjugateGradientSolver.Method.POLAK_RIBIERE_PLUS)
                        .restartMethod(NonlinearConjugateGradientSolver.RestartMethod.GRADIENTS_ORTHOGONALITY_CHECK)
                        .build();
        double[] actualResult = polakRibierePlusSolver.solve().getArray();
        Assert.assertArrayEquals(expectedResult, actualResult, 1e-2);
    }

    @Test
    public void testHestenesStiefelSolver() {
//        System.out.println("Rosenbrock Function Hestenes-Stiefel Solver:\n");
//        NonlinearConjugateGradientSolver hestenesStiefelSolver =
//                new NonlinearConjugateGradientSolver.Builder(new RosenbrockFunction(), new double[] { -1.2, 1 })
//                        .method(NonlinearConjugateGradientSolver.Method.HESTENES_STIEFEL)
//                        .restartMethod(NonlinearConjugateGradientSolver.RestartMethod.NO_RESTART)
//                        .build();
//        double[] actualResult = hestenesStiefelSolver.solve().getArray();
        double[] expectedResult = new double[] { 1, 1 };
//        Assert.assertArrayEquals(expectedResult, actualResult, 1e-2);

        System.out.println("Rosenbrock Function Hestenes-Stiefel N-Step-Restart Solver:\n");
        NonlinearConjugateGradientSolver hestenesStiefelSolver =
                new NonlinearConjugateGradientSolver.Builder(new RosenbrockFunction(), new double[] { -1.2, 1 })
                        .method(NonlinearConjugateGradientSolver.Method.HESTENES_STIEFEL)
                        .restartMethod(NonlinearConjugateGradientSolver.RestartMethod.N_STEP)
                        .build();
        double[] actualResult = hestenesStiefelSolver.solve().getArray();
        Assert.assertArrayEquals(expectedResult, actualResult, 1e-2);

        System.out.println("Rosenbrock Function Hestenes-Stiefel Gradients-Orthogonality-Check-Restart Solver:\n");
        hestenesStiefelSolver =
                new NonlinearConjugateGradientSolver.Builder(new RosenbrockFunction(), new double[] { -1.2, 1 })
                        .method(NonlinearConjugateGradientSolver.Method.HESTENES_STIEFEL)
                        .restartMethod(NonlinearConjugateGradientSolver.RestartMethod.GRADIENTS_ORTHOGONALITY_CHECK)
                        .build();
        actualResult = hestenesStiefelSolver.solve().getArray();
        Assert.assertArrayEquals(expectedResult, actualResult, 1e-2);
    }

    @Test
    public void testFletcherRievesPolakRibiereSolver() {
        System.out.println("Rosenbrock Function Fletcher-Rieves-Polak-Ribiere Solver:\n");
        NonlinearConjugateGradientSolver fletcherRievesPolakRibiereSolver =
                new NonlinearConjugateGradientSolver.Builder(new RosenbrockFunction(), new double[] { -1.2, 1 })
                        .method(NonlinearConjugateGradientSolver.Method.FLETCHER_RIEVES_POLAK_RIBIERE)
                        .restartMethod(NonlinearConjugateGradientSolver.RestartMethod.NO_RESTART)
                        .build();
        double[] actualResult = fletcherRievesPolakRibiereSolver.solve().getArray();
        double[] expectedResult = new double[] { 1, 1 };
        Assert.assertArrayEquals(expectedResult, actualResult, 1e-2);

        System.out.println("Rosenbrock Function Fletcher-Rieves-Polak-Ribiere N-Step-Restart Solver:\n");
        fletcherRievesPolakRibiereSolver =
                new NonlinearConjugateGradientSolver.Builder(new RosenbrockFunction(), new double[] { -1.2, 1 })
                        .method(NonlinearConjugateGradientSolver.Method.FLETCHER_RIEVES_POLAK_RIBIERE)
                        .restartMethod(NonlinearConjugateGradientSolver.RestartMethod.N_STEP)
                        .build();
        actualResult = fletcherRievesPolakRibiereSolver.solve().getArray();
        Assert.assertArrayEquals(expectedResult, actualResult, 1e-2);

        System.out.println("Rosenbrock Function Fletcher-Rieves-Polak-Ribiere Gradients-Orthogonality-Check-Restart Solver:\n");
        fletcherRievesPolakRibiereSolver =
                new NonlinearConjugateGradientSolver.Builder(new RosenbrockFunction(), new double[] { -1.2, 1 })
                        .method(NonlinearConjugateGradientSolver.Method.FLETCHER_RIEVES_POLAK_RIBIERE)
                        .restartMethod(NonlinearConjugateGradientSolver.RestartMethod.GRADIENTS_ORTHOGONALITY_CHECK)
                        .build();
        actualResult = fletcherRievesPolakRibiereSolver.solve().getArray();
        Assert.assertArrayEquals(expectedResult, actualResult, 1e-2);
    }

    @Test
    public void testDaiYuanSolver() {
        System.out.println("Rosenbrock Function Dai-Yuan Solver:\n");
        NonlinearConjugateGradientSolver daiYuanSolver =
                new NonlinearConjugateGradientSolver.Builder(new RosenbrockFunction(), new double[] { -1.2, 1 })
                        .method(NonlinearConjugateGradientSolver.Method.DAI_YUAN)
                        .restartMethod(NonlinearConjugateGradientSolver.RestartMethod.NO_RESTART)
                        .build();
        double[] actualResult = daiYuanSolver.solve().getArray();
        double[] expectedResult = new double[] { 1, 1 };
        Assert.assertArrayEquals(expectedResult, actualResult, 1e-2);

        System.out.println("Rosenbrock Function Dai-Yuan N-Step-Restart Solver:\n");
        daiYuanSolver =
                new NonlinearConjugateGradientSolver.Builder(new RosenbrockFunction(), new double[] { -1.2, 1 })
                        .method(NonlinearConjugateGradientSolver.Method.DAI_YUAN)
                        .restartMethod(NonlinearConjugateGradientSolver.RestartMethod.N_STEP)
                        .build();
        actualResult = daiYuanSolver.solve().getArray();
        Assert.assertArrayEquals(expectedResult, actualResult, 1e-2);

        System.out.println("Rosenbrock Function Dai-Yuan Gradients-Orthogonality-Check-Restart Solver:\n");
        daiYuanSolver =
                new NonlinearConjugateGradientSolver.Builder(new RosenbrockFunction(), new double[] { -1.2, 1 })
                        .method(NonlinearConjugateGradientSolver.Method.DAI_YUAN)
                        .restartMethod(NonlinearConjugateGradientSolver.RestartMethod.GRADIENTS_ORTHOGONALITY_CHECK)
                        .build();
        actualResult = daiYuanSolver.solve().getArray();
        Assert.assertArrayEquals(expectedResult, actualResult, 1e-2);
    }

    @Test
    public void testHagerZhangSolver() {
        System.out.println("Rosenbrock Function Hager-Zhang Solver:\n");
        NonlinearConjugateGradientSolver hagerZhangSolver =
                new NonlinearConjugateGradientSolver.Builder(new RosenbrockFunction(), new double[] { -1.2, 1 })
                        .method(NonlinearConjugateGradientSolver.Method.HAGER_ZHANG)
                        .restartMethod(NonlinearConjugateGradientSolver.RestartMethod.NO_RESTART)
                        .build();
        double[] actualResult = hagerZhangSolver.solve().getArray();
        double[] expectedResult = new double[] { 1, 1 };
        Assert.assertArrayEquals(expectedResult, actualResult, 1e-2);

        System.out.println("Rosenbrock Function Hager-Zhang N-Step-Restart Solver:\n");
        hagerZhangSolver =
                new NonlinearConjugateGradientSolver.Builder(new RosenbrockFunction(), new double[] { -1.2, 1 })
                        .method(NonlinearConjugateGradientSolver.Method.HAGER_ZHANG)
                        .restartMethod(NonlinearConjugateGradientSolver.RestartMethod.N_STEP)
                        .build();
        actualResult = hagerZhangSolver.solve().getArray();
        Assert.assertArrayEquals(expectedResult, actualResult, 1e-2);

        System.out.println("Rosenbrock Function Hager-Zhang Gradients-Orthogonality-Check-Restart Solver:\n");
        hagerZhangSolver =
                new NonlinearConjugateGradientSolver.Builder(new RosenbrockFunction(), new double[] { -1.2, 1 })
                        .method(NonlinearConjugateGradientSolver.Method.HAGER_ZHANG)
                        .restartMethod(NonlinearConjugateGradientSolver.RestartMethod.GRADIENTS_ORTHOGONALITY_CHECK)
                        .build();
        actualResult = hagerZhangSolver.solve().getArray();
        Assert.assertArrayEquals(expectedResult, actualResult, 1e-2);
    }
}
