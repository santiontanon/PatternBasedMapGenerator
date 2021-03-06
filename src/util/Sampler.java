/********************************************************************************
Organization		: Drexel University
Authors			: Santiago Ontanon
Class			: Sampler
Function		: This class contains methods to sample
                          from a given distribution. Including support
                          for exploration vs exploitation.
 *********************************************************************************/
package util;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Sampler {

    /*
     * Returns a random element in the distribution
     */
    public static int random(double []distribution) {
        Random generator = new Random();
        return generator.nextInt(distribution.length);
    }

    /*
     * Returns the element with maximum probability (ties are resolved randomly)
     */
    public static int max(double []distribution) throws Exception {
        List<Integer> best = new LinkedList<Integer>();
        double max = distribution[0];

        for (int i = 0; i < distribution.length; i++) {
            Double f = distribution[i];
            if (f == max) {
                best.add(new Integer(i));
            } else {
                if (f > max) {
                    best.clear();
                    best.add(new Integer(i));
                    max = f;
                }
            }
        }

        if (best.size() > 0) {
            Random generator = new Random();
            return best.get(generator.nextInt(best.size()));
        }

        throw new Exception("Input distribution empty in Sampler.max!");
    }

    /*
     * Returns the score with maximum probability (ties are resolved randomly)
     */
    public static Double maxScore(double []distribution) {
        List<Integer> best = new LinkedList<Integer>();
        double max = distribution[0];

        for (int i = 0; i < distribution.length; i++) {
            Double f = distribution[i];
            if (f == max) {
                best.add(new Integer(i));
            } else {
                if (f > max) {
                    best.clear();
                    best.add(new Integer(i));
                    max = f;
                }
            }
        }

        return max;

    }

    /*
     * Returns an element in the distribution, using the weights as their relative probabilities
     */
    public static int weighted(double []distribution) throws Exception {
        Random generator = new Random();
        double total = 0, accum = 0, tmp;

        for (double f : distribution) {
            total += f;
        }

        tmp = generator.nextDouble() * total;
        for (int i = 0; i < distribution.length; i++) {
            accum += distribution[i];
            if (accum >= tmp) {
                return i;
            }
        }

        throw new Exception("Input distribution empty in Sampler.weighted (array)!");
    }
    
    
    /*
     * Returns an element in the distribution, using the weights as their relative probabilities
     */
    public static int weighted(List<Double> distribution) throws Exception {
        Random generator = new Random();
        double total = 0, accum = 0, tmp;

        for (double f : distribution) {
            total += f;
        }

        tmp = generator.nextDouble() * total;
        int i = 0;
        for (double f : distribution) {
            accum += f;
            if (accum >= tmp) {
                return i;
            }
            i++;
        }

        throw new Exception("Input distribution empty in Sampler.weighted (list)!");
    }
    

    /*
     * Returns an element in the distribution following the probabilities, but using 'e' as the exploration factor.
     * For instance:
     * If "e" = 1.0, then it has the same effect as the "max" method
     * If "e" = 0.5, then it has the same effect as the "weighted" method
     * If "e" = 0, then it has the same effect as the "random" method
     */
    public static int explorationWeighted(double []distribution, double e) throws Exception {
        /*
         * exponent = 1/(1-e)-1
         */

        double exponent = 0;
        double quotient = 1 - e;
        if (quotient != 0) {
            exponent = 1 / quotient - 1;
        } else {
            exponent = 1000;
        }
        double []exponentiated = new double[distribution.length];

        for(int i = 0;i<distribution.length;i++)
            exponentiated[i] = Math.pow(distribution[i],exponent);

        return weighted(exponentiated);
    }
}
