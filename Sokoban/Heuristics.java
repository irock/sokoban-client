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
     * A heuristic expanding the states with the most boxes in a goal first.
     */
    public static class MaxNumDone implements Comparator<State> {
        public int compare(State a, State b) {
            return b.getNumBoxesInGoal() - a.getNumBoxesInGoal();
        }
    }

    /**
     * A heuristic for comparing state scores.
     */
    public static class MaxScore implements Comparator<State> {
        public int compare(State a, State b) {
            float cmp = b.getTotalScore() - a.getTotalScore();
            if (cmp < 0)
                return -1;
            else if (cmp > 0)
                return 1;
            return 0;
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

    /**
     * The class can combine several heuristics, and prioritize them in
     * different orders.
     */
    public class WeightedHeuristic implements Comparator<State> {
        List<Entry<Comparator<State>, Float>> heuristics;

        /**
         * Default constructor.
         */
        public WeightedHeuristic() {
            heuristics = new LinkedList<Entry<Comparator<State>, Float>>();
        }

        /**
         * Heuristics are added with the most important one being first.
         **/
        public void add(Comparator<State> heuristic, float weight) {
            heuristics.add(new SimpleEntry<Comparator<State>, Float>(
                        heuristic, weight));
        }

        /**
         * The WeightedHeuristic is interpreted as any normal heuristic,
         * by the rest of the program.
         */
        public int compare(State a, State b) {
            float total = 0;
            for (Entry<Comparator<State>, Float> entry : heuristics)
                total += entry.getValue() * entry.getKey().compare(a, b);

            if (total < 0)
                return -1;
            else if (total > 0)
                return 1;
            return 0;
        }
    }
}
