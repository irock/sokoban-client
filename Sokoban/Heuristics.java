package Sokoban;

import java.util.List;
import java.util.LinkedList;
import java.util.Comparator;

/**
 * A collection of heuristics that can be used to solve Sokoban games.
 */
public class Heuristics {
    /**
     * A heuristic expanding the states with the most boxes in a goal first.
     */
    public static class MaxNumDone implements Comparator<State> {
        public int compare(State a, State b) {
            return b.getNumBoxesInGoal() - a.getNumBoxesInGoal();
        }
    }

    /**
     * A heuristic expanding the states with the least sum of squared distances
     * to the goal squares.
     */
    public static class MinSumOfSquaredDistances implements Comparator<State> {
        public int compare(State a, State b) {
            return a.getSumOfSquaredDistances() - b.getSumOfSquaredDistances();
        }
    }

    /**
     * A heuristic comparing the minimum number of box moves from the goal in
     * each state.
     */
    public static class MinNumMoves implements Comparator<State> {
        public int compare(State a, State b) {
            return a.getNumMoves() - b.getNumMoves();
        }
    }

    /**
     * A container class for multiple heuristics. The order in which they are
     * applied is decided by what order they were put in the heuristics list.
     */
    public static class MultipleHeuristic implements Comparator<State> {
        /**
         * The list of heuristics to test.
         */
        List<Comparator<State>> comparators;

        /**
         * Create a new MultipleHeuristic.
         */
        public MultipleHeuristic() {
            comparators = new LinkedList<Comparator<State>>();
        }

        /**
         * Add a heuristic to the list of heuristics to test.
         *
         * @param heuristic The heuristic to add.
         */
        public void add(Comparator<State> heuristic) {
            comparators.add(heuristic);
        }

        public int compare(State a, State b) {
            for (Comparator<State> comparator : comparators) {
                int tmp = comparator.compare(a, b);
                if (tmp != 0)
                    return tmp;
            }
            return 0;
        }
    }

    /**
     * A heuristic comparing the number of steps from the goal the player is in
     * each state.
     */
    public static class MinStepsFromGoal implements Comparator<State> {
        public int compare(State a, State b) {
            return a.getStepsFromGoal() - b.getStepsFromGoal();
        }
    }

    /**
     * A totally random heuristic.
     */
    public static class Random implements Comparator<State> {
        /**
         * The random number generator.
         */
        java.util.Random random;

        /**
         * Create a new Random heuristic.
         */
        public Random() {
            random = new java.util.Random();
        }

        public int compare(State a, State b) {
            return random.nextBoolean() ? -1 : 1;
        }
    }

}
