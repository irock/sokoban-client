package Sokoban;

import java.awt.Point;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.LinkedList;
import java.util.Random;
import java.util.Map.Entry;
import java.util.Set;
import java.util.HashSet;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.Comparator;

// Unsolved puzzles with numDone+stepsFromGoal: 33, 52, 56, 66, 70, 72, 73, 77

public class Solver {
    public class NumDoneHeuristic implements Comparator<State> {
        public int compare(State a, State b) {
            return b.getNumDone() - a.getNumDone();
        }
    }

    public class GoalDistanceHeuristic implements Comparator<State> {
        public int compare(State a, State b) {
            return a.getGoalDistance() - b.getGoalDistance();
        }
    }

    public class NumMovesHeuristic implements Comparator<State> {
        public int compare(State a, State b) {
            return a.getNumMoves() - b.getNumMoves();
        }
    }

    public class MultipleHeuristic implements Comparator<State> {
        List<Comparator<State>> comparators;

        public MultipleHeuristic() {
            comparators = new LinkedList<Comparator<State>>();
        }

        public void add(Comparator<State> comparator) {
            comparators.add(comparator);
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

    public class StepsFromGoalHeuristic implements Comparator<State> {
        public int compare(State a, State b) {
            return a.getStepsFromGoal() - b.getStepsFromGoal();
        }
    }

    public class RandomHeuristic implements Comparator<State> {
        Random random;
        public RandomHeuristic() {
            random = new Random();
        }

        public int compare(State a, State b) {
            return random.nextBoolean() ? -1 : 1;
        }
    }

    static boolean printStatePath = false;
    static boolean printDirectionPath = true;
    static boolean printPuzzle = true;
    static boolean printProgress = true;
    static boolean useServer = true;
    static int searchLimit = 50000;

    Map map;
    State startState;
    State endState;

    public Solver(String mapString) {
        map = Map.parse(mapString);
        startState = new State(map.getStart(), map.getBoxes(), map);
        endState = null;
    }

    public int breadthFirstSearch() {
        int numExpanded = 0;
        int numInspected = 0;

        Comparator<State> heuristic1 = new NumDoneHeuristic();
        Comparator<State> heuristic2 = new GoalDistanceHeuristic();
        Comparator<State> heuristic3 = new NumMovesHeuristic();
        MultipleHeuristic heuristic4 = new MultipleHeuristic();
        Comparator<State> heuristic5 = new RandomHeuristic();
        Comparator<State> heuristic6 = new StepsFromGoalHeuristic();
        heuristic4.add(heuristic1);
        heuristic4.add(heuristic6);

        Queue<State> queue = new PriorityQueue<State>(10000, heuristic4);
        Set<State> visited = new HashSet<State>();

        queue.add(startState);
        visited.add(startState);

        while (!queue.isEmpty() && numExpanded < searchLimit) {
            State curState = queue.poll();
            numExpanded++;
            if (printProgress)
                System.out.printf("expanded: %6d, inspected: %6d, num filled: %2d\r",
                        numExpanded, numInspected, curState.getNumDone());

            for (Entry<Direction, Box> move : curState.getAvailableMoves()) {
                if (!curState.wouldBeTrapped(move.getKey(), move.getValue())) {
                    State nextState = State.getStateAfterMove(curState, move);
                    numInspected++;

                    if (!visited.contains(nextState)) {
                        if (nextState.goalReached()) {
                            endState = nextState;
                            System.out.println();
                            return numInspected;
                        }
                        queue.add(nextState);
                        visited.add(nextState);
                    }
                }
            }
        }
        System.out.println();
        return 0;
    }

    public static void main(String[] args) {
        String mapString;

        if (args.length != 1) {
            System.out.println("No puzzle number specified.");
            return;
        }

        int puzzle;

        try {
            puzzle = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid puzzle number specified.");
            return;
        }

        Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;

        if (useServer) {
            try {
                char[] buffer = new char[1024];
                socket = new Socket("cvap103.nada.kth.se", 5555);
                in = new BufferedReader(new InputStreamReader(
                            socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                out.println(String.format("%d", puzzle));

                in.read(buffer, 0, buffer.length);
                mapString = new String(buffer);

                if (mapString.substring(0, "Wrong ID".length()).equals("Wrong ID")) {
                    System.out.println("Invalid puzzle number specified!");
                    socket.close();
                    return;
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
                return;
            }
        } else {
            mapString = Puzzle.getPuzzle(puzzle);
        }

        if (printPuzzle)
            System.out.print(mapString);

        Solver solver = new Solver(mapString);

        long start = System.currentTimeMillis();
        int num = solver.breadthFirstSearch();

        if (solver.endState != null) {
            if (printStatePath)
                System.out.println(solver.endState.statePath());

            if (printDirectionPath) {
                for (Direction d : solver.endState.directionPath())
                    System.out.print(d);
                System.out.println();
            }

            if (useServer) {
                for (Direction d : solver.endState.directionPath())
                    out.print(d);
                out.println();
                try {
                    String result = in.readLine();
                    System.out.println(result);
                } catch (IOException e) { }
            }
        } else
            System.out.println("No solution found.");

        if (useServer) {
            try {
                socket.close();
            } catch (IOException e) { }
        }

        System.out.printf("time: %d\n", System.currentTimeMillis()-start);
    }
}
