package Sokoban;

import java.util.List;
import java.util.LinkedList;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.AbstractMap.SimpleEntry;

/**
 * A collection of heuristics that can be used to solve Sokoban games.
 */
public class Heuristics {
    /**
     * A heuristic forcing states to be processed after some time.
     */
    public static class MinId implements Comparator<State> {
        /**
         * The minimum distance between two states to compare them.
         */
        int range;

        /**
         * Create a new MinId heuristic.
         *
         * @param range The minimum difference in id of two states to compare
         * them.
         */
        public MinId(int range) {
            this.range = range;
        }

        public int compare(State a, State b) {
            int tmp = a.getId() - b.getId();
            if (Math.abs(tmp) < range)
                return 0;
            return tmp;
        }
    }

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
     * A heuristic comparing the states on basis of the minimum real distance
     * for each box to goal squares.
     */
    public static class MinGoalDistance implements Comparator<State> {
        public int compare(State a, State b) {
            return a.getGoalDistance() - b.getGoalDistance();
        }
    }

    /**
     * A heuristic comparing the number box moves so far in each state.
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
         * For describing when a specific heuristic should be applied.
         */
        private class Range {
            /**
             * At what move the heuristic should start.
             */
            int from;

            /**
             * At what move the heuristic should end.
             */
            int to;

            /**
             * Create a new Range.
             *
             * @param from The start of the range.
             * @param to The end of the range.
             */
            public Range(int from, int to) {
                this.from = from;
                this.to = to;
            }
        }

        /**
         * The list of heuristics to test.
         */
        List<Entry<Range, Comparator<State>>> comparators;

        /**
         * Create a new MultipleHeuristic.
         */
        public MultipleHeuristic() {
            comparators = new LinkedList<Entry<Range, Comparator<State>>>();
        }

        /**
         * Add a heuristic to the list of heuristics to test.
         *
         * @param heuristic The heuristic to add.
         * @param from At what height the heuristic should begin.
         * @param to At what height the heuristic should end.
         */
        public void add(Comparator<State> heuristic, int from, int to) {
            comparators.add(new SimpleEntry<Range, Comparator<State>>(
                        new Range(from, to), heuristic));
        }

        /**
         * @see add(Comparator<State>, int, int);
         */
        public void add(Comparator<State> heuristic, int from) {
            add(heuristic, from, 0);
        }

        /**
         * @see add(Comparator<State>, int);
         */
        public void add(Comparator<State> heuristic) {
            add(heuristic, 0);
        }

        public int compare(State a, State b) {
            for (Entry<Range, Comparator<State>> entry : comparators) {
                Range range = entry.getKey();
                if (range.from > a.getNumMoves() || range.from > b.getNumMoves() ||
                        (range.to != 0 && (range.to < a.getNumMoves() ||
                                           range.to < b.getNumMoves())))
                    continue;

                Comparator<State> comparator = entry.getValue();
                int tmp = comparator.compare(a, b);
                if (tmp != 0)
                    return tmp;
            }
            return a.getNumMoves() - b.getNumMoves();
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
