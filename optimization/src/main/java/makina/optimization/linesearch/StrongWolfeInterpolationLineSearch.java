package makina.optimization.linesearch;

import com.google.common.base.Preconditions;
import makina.math.matrix.Vector;
import makina.optimization.function.AbstractFunction;
import makina.optimization.function.NonSmoothFunctionException;

/**
 * Implements an interpolation based line search algorithm that returns a step size value that satisfies the strong
 * Wolfe conditions. This is an implementation of algorithms 3.5 and 3.6, described in pages 60-62 of the book
 * "Numerical Optimization", by Jorge Nocedal and Stephen Wright.
 *
 * @author Emmanouil Antonios Platanios
 */
public final class StrongWolfeInterpolationLineSearch extends IterativeLineSearch {
    /** Maximum number of allowed line search iterations with no improvement in the objective function value. */
    private static final int MAXIMUM_ITERATIONS_WITH_NO_OBJECTIVE_IMPROVEMENT = 10;
    /** Threshold for the minimum allowed distance between a new step size value, computed using a cubic interpolation
     * approach, and the allowed step size values interval endpoints. */
    private static final double MINIMUM_DISTANCE_FROM_INTERVAL_ENDPOINTS = 1e-3;

    /** The constant used for the first of the two Wolfe conditions (that is, the Armijo condition). */
    private final double c1;
    /** The constant used for the second of the two Wolfe conditions (that is, the curvature condition). */
    private final double c2;
    /** The maximum allowed value for the step size. */
    private final double aMax;

    /**
     * Constructs a strong Wolfe interpolation based line search solver for the provided objective function instance and
     * using the provided parameters.
     *
     * @param   objective   The objective function instance.
     * @param   c1          The proportionality constant used for the first of the two Wolfe conditions (that is, the
     *                      Armijo condition). The value provided must lie between 0 and 1.
     * @param   c2          The proportionality constant used for the second of the two Wolfe conditions (that is, the
     *                      curvature condition). The value provided must lie between {@code c1} and 1.
     * @param   aMax        The maximum allowed value for the step size.
     */
    public StrongWolfeInterpolationLineSearch(AbstractFunction objective,
                                              double c1,
                                              double c2,
                                              double aMax) {
        super(objective);

        Preconditions.checkArgument(c1 > 0 && c1 < 1);
        Preconditions.checkArgument(c2 > c1 && c2 < 1);
        Preconditions.checkArgument(aMax > 0);

        this.c1 = c1;
        this.c2 = c2;
        this.aMax = aMax;
    }

    /**
     * {@inheritDoc}
     *
     * @return  A step size value that satisfies the strong Wolfe conditions.
     */
    @Override
    public double performLineSearch(Vector point,
                                    Vector direction)
            throws NonSmoothFunctionException {
        double phi0 = objective.getValue(point);
        double phiPrime0 = objective.getGradient(point).inner(direction);
        double a0 = 0;
        double a1 = initialStepSize;

        if (a1 <= 0 || a1 >= aMax) {
            a1 = aMax / 2;
        }

        boolean firstIteration = true;

        while (true) {
            Vector a1Point = point.add(direction.mult(a1));
            double phiA1 = objective.getValue(a1Point);
            double phiA0 = objective.getValue(point.add(direction.mult(a0)));
            if (phiA1 > phi0 + c1 * a1 * phiPrime0 || (phiA1 >= phiA0 && !firstIteration)) {
                return zoom(point, direction, a0, a1);
            }
            double phiPrimeA1 = objective.getGradient(a1Point).inner(direction);
            if (Math.abs(phiPrimeA1) <= -c2 * phiPrime0) {
                return a1;
            } else if (phiPrimeA1 >= 0) {
                return zoom(point, direction, a1, a0);
            }

            a0 = a1;
            a1 = 2 * a1;
            if (a1 > aMax) {
                return aMax;
            }
            firstIteration = false;
        }
    }

    /**
     * This function "zooms in" in the interval {@code [aLow, aHigh]} and searches for a step size within that interval
     * that satisfies the strong Wolfe conditions. In each iteration the interval of possible values for the step size
     * is "shrinked" and a new value to test is chosen each time using cubic interpolation. {@code aLow} and
     * {@code aHigh} are such that:
     * <br><br>
     * <ul>
     *     <li>The interval bounded by {@code aLow} and {@code aHigh} contains step lengths that satisfy the strong
     *     Wolfe conditions.</li>
     *     <li>Among all step sizes generated so far and satisfying the Armijo condition (also known as the sufficient
     *     decrease condition), {@code aLow} is the one giving the smallest function value.</li>
     *     <li>{@code aHigh} is chosen so that &phi;'({@code aLow})({@code aHigh} - {@code aLow}) &lt; 0.</li>
     * </ul>
     * <br>
     * Each iteration of this function generates a new step size, between {@code aLow} and {@code aLow} and then
     * replaces one of these endpoints by that new step size, in such a way that the above mentioned properties continue
     * to hold. Once a step size value satisfying the strong Wolfe conditions has been found, that value is returned.
     * <br><br>
     * Note that {@code aLow} does not necessarily have to be smaller than {@code aHigh}.
     *
     * @param   point       The point at which we perform the line search.
     * @param   direction   The direction for which we perform the line search.
     * @param   aLow        One of the two endpoints of the interval of possible values for the step size.
     * @param   aHigh       One of the two endpoints of the interval of possible values for the step size.
     * @return              A step size value that lies in the interval {@code [aLow, aHigh]} and satisfies the strong
     *                      Wolfe conditions.
     */
    private double zoom(Vector point,
                        Vector direction,
                        double aLow,
                        double aHigh)
            throws NonSmoothFunctionException {
        double phi0 = objective.getValue(point);
        double phiPrime0 = objective.getGradient(point).inner(direction);

        // Declare variables used in the loop that follows.
        double aNew;
        double phiANew;
        double phiALow;
        double phiPrimeANew;
        Vector aNewPoint;

        // Declare and initialize variables used to test for convergence of the objective function value.
        double minimumObjectiveValue = Double.MAX_VALUE;
        double minimumObjectiveValueIterationNumber = -1;
        int iterationNumber = 0;

        while (true) {
            aNew = performCubicInterpolation(point, direction, aLow, aHigh);
            aNewPoint = point.add(direction.mult(aNew));
            phiANew = objective.getValue(aNewPoint);
            phiALow = objective.getValue(point.add(direction.mult(aLow)));

            if (phiANew > phi0 + c1 * aNew * phiPrime0 || phiANew >= phiALow) {
                aHigh = aNew;
            } else {
                phiPrimeANew = objective.getGradient(aNewPoint).inner(direction);
                if (Math.abs(phiPrimeANew) <= -c2 * phiPrime0) {
                    return aNew;
                } else if (phiPrimeANew * (aHigh - aLow) >= 0) {
                    aHigh = aLow;
                }
                aLow = aNew;
            }

            // Check for convergence of the objective function value.
            iterationNumber++;
            if (phiANew < minimumObjectiveValue) {
                minimumObjectiveValue = phiANew;
                minimumObjectiveValueIterationNumber = iterationNumber;
            } else if (iterationNumber - minimumObjectiveValueIterationNumber
                    > MAXIMUM_ITERATIONS_WITH_NO_OBJECTIVE_IMPROVEMENT) {
                return aNew;
            }
        }
    }

    /**
     * Performs a cubic interpolation using the available information in order to obtain an approximation of the &phi;
     * function and returns the step size value, in the interval {@code [aLow, aHigh]}, that minimizes that
     * approximation.
     *
     * @param   point       The point at which we perform the line search.
     * @param   direction   The direction for which we perform the line search.
     * @param   aLow        One of the two endpoints of the interval of possible values for the step size.
     * @param   aHigh       One of the two endpoints of the interval of possible values for the step size.
     * @return              A point in the interval {@code [aLow, aHigh]} that minimizes a cubic interpolation
     *                      approximation of the &phi; function computed using available information.
     */
    private double performCubicInterpolation(Vector point,
                                             Vector direction,
                                             double aLow,
                                             double aHigh)
            throws NonSmoothFunctionException {
        Vector newPointLow = point.add(direction.mult(aLow));
        Vector newPointHigh = point.add(direction.mult(aHigh));
        double phiALow = objective.getValue(newPointLow);
        double phiAHigh = objective.getValue(newPointHigh);
        double phiPrimeALow = objective.getGradient(newPointLow).inner(direction);
        double phiPrimeAHigh = objective.getGradient(newPointHigh).inner(direction);
        double d1 = phiPrimeALow + phiPrimeAHigh - 3 * (phiALow - phiAHigh) / (aLow - aHigh);
        double d2 = Math.signum(aHigh - aLow) * Math.sqrt(Math.pow(d1, 2) - phiPrimeALow * phiPrimeAHigh);
        double aNew = aHigh - (aHigh - aLow) * (phiPrimeAHigh + d2 - d1) / (phiPrimeAHigh - phiPrimeALow + 2 * d2);

        // Check whether the minimizer is one of the endpoints of the interval or if it is the newly computed value.
        if (aLow <= aNew && aNew <= aHigh) {
            double phiANew = objective.getValue(point.add(direction.mult(aNew)));
            if (phiALow <= phiANew) {
                if (phiALow <= phiAHigh) {
                    aNew = aLow;
                } else {
                    aNew = aHigh;
                }
            } else if (phiAHigh <= phiANew) {
                aNew = aHigh;
            }
        } else {
            if (phiALow <= phiAHigh) {
                aNew = aLow;
            } else {
                aNew = aHigh;
            }
        }

        // Ensure that the new step size is not too close to the endpoints of the interval.
        if (Math.abs(aNew - aLow) <= MINIMUM_DISTANCE_FROM_INTERVAL_ENDPOINTS
                || Math.abs(aNew - aHigh) <= MINIMUM_DISTANCE_FROM_INTERVAL_ENDPOINTS) {
            aNew = (aLow + aHigh) / 2;
        }

        return aNew;
    }
}
