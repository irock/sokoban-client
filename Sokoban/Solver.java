package Sokoban;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.Map.Entry;
import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.Comparator;

/**
 * A class used for solving Sokoban puzzles. This is the main class of the
 * project.
 */
public class Solver {
    /**
     * Tells whether the state path should be written to stdout when a path is
     * found.
     */
    static boolean printStatePath = false;

    /**
     * Tells whether a path of directions should be written to stdout when
     * a path is found.
     */
    static boolean printDirectionPath = false;

    /**
     * Tells whether the puzzle should be written to stdout upon start.
     */
    static boolean printPuzzle = true;

    /**
     * Tells whether a progress meter should be displayed.
     */
    static boolean printProgress = true;

    /**
     * Tells whether the puzzle server should be used. If set to false, offline
     * puzzles are used instead.
     */
    static boolean useServer = true;

    /**
     * The time limit for a search.
     */
    static int searchLimit = 60000;

    /**
     * The interval to wait between check of time and prints.
     */
    static int interval = 200;

    /**
     * The map to solve.
     */
    Map map;

    /**
     * The start state of the map.
     */
    State startState;

    /**
     * The end state of the map. If no solution has been found, it is null.
     */
    State endState;

    /**
     * Create a new Solver.
     *
     * @param mapString A string representation of a map.
     */
    public Solver(String mapString) {
        map = Map.parse(mapString);
        startState = new State(map.getStart(), map.getBoxes(), map);
        endState = null;
    }

    /**
     * @return the map to solve.
     */
    public Map getMap() {
        return map;
    }

    /**
     * @return the start state of the map.
     */
    public State getStartState() {
        return startState;
    }

    /**
     * @return the end state of the map.
     */
    public State getEndState() {
        return endState;
    }

    private void printInfo(int expanded, int inspected, int visited,
            int filled, long start) {
        System.out.printf("expanded: %6d, inspected: %6d, queue: %6d, " +
                "filled: %2d, time: %2.2f s\r", expanded, inspected, visited,
                filled, (float)(System.currentTimeMillis()-start)/1000);
    }

    /**
     * Do an A* search for a solution.
     *
     * @param heuristic The heuristic to use.
     * @param limit The time limit.
     * @return the number of expanded nodes.
     */
    public int search(Comparator<State> heuristic, int limit) {
        int numExpanded = 0;
        int numInspected = 0;

        Queue<State> queue = new PriorityQueue<State>(1000, heuristic);
        Set<State> visited = new HashSet<State>();

        queue.add(startState);
        visited.add(startState);

        int i = 0;
        long start = System.currentTimeMillis();

        while (!queue.isEmpty()) {
            State curState = queue.poll();

            if (numExpanded == 1 || i == interval) {
                if (printProgress) {
                    printInfo(numExpanded, numInspected, queue.size(),
                            curState.getNumBoxesInGoal(), start);
                }

                if (System.currentTimeMillis()-start >= limit)
                    break;
                i = 0;
            }
            i++;
            numExpanded++;

            for (Entry<Direction, Point> move : curState.getAvailableMoves()) {
                State nextState = State.getStateAfterMove(curState, move);
                numInspected++;

                if (!visited.contains(nextState)) {
                    if (nextState.goalReached()) {
                        endState = nextState;
                        if (printProgress) {
                            printInfo(numExpanded, numInspected, queue.size(),
                                    endState.getNumBoxesInGoal(), start);
                            System.out.println();
                        }
                        return numExpanded;
                    }
                    queue.add(nextState);
                    visited.add(nextState);
                }
            }
        }

        if (printProgress) {
            printInfo(numExpanded, numInspected, queue.size(), 0, start);
            System.out.println();
        }
        return numExpanded;
    }

    /**
     * Run the solver on the puzzle given in args[0].
     */
    public static void main(String[] args) {
        int puzzle = 0;
        String mapString = null;

        if (args.length == 0) {
            System.err.println("No puzzle number specified.");
            System.exit(1);
        }

        try {
            puzzle = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.err.println("Invalid puzzle number specified.");
            System.exit(1);
        }

        if (args.length > 1) {
            if (args[1].equals("-q")) {
                printProgress = false;
                printDirectionPath = false;
                printStatePath = false;
                printPuzzle = false;
            } else {
                System.err.println("Invalid option specified.");
                System.exit(1);
            }
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

                String wrong = "Wrong ID";
                if (mapString.substring(0, wrong.length()).equals(wrong)) {
                    mapString = null;
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        } else
            mapString = Puzzle.getPuzzleFromSamples(puzzle);

        if (mapString == null) {
            System.err.println("No puzzle with the supplied number found.");
            System.exit(1);
        }

        Solver solver = new Solver(mapString);

        if (printPuzzle)
            System.out.println(solver.getStartState());

        Heuristics.MultipleHeuristic heuristic =
            new Heuristics.MultipleHeuristic();

        heuristic.add(new Heuristics.MinGoalDistance(), 3);

        long time = System.currentTimeMillis();
        long num = solver.search(heuristic, (int)(3.0/4 * searchLimit));
        time = System.currentTimeMillis() - time;

        if (solver.getEndState() == null) {
            heuristic = new Heuristics.MultipleHeuristic();
            heuristic.add(new Heuristics.MaxScore(), 3);
            time = System.currentTimeMillis();
            num += solver.search(heuristic, (int)(1.0/4 * searchLimit));
            time = System.currentTimeMillis() - time;
        }

        boolean success = false;

        if (solver.getEndState() != null) {
            if (useServer) {
                for (Direction d : solver.getEndState().directionPath())
                    out.print(d);
                out.println();
                try {
                    String result = in.readLine();
                    success = result.equals("Good solution");
                    if (printPuzzle)
                        System.out.println(result);
                } catch (IOException e) { }
            } else
                success = true;
        }

        if (success) {
            if (printStatePath)
                System.out.println(solver.getEndState().statePath());

            if (printDirectionPath) {
                List<Direction> path = solver.getEndState().directionPath();
                System.out.println("path length: " + path.size());
                for (Direction d : path)
                    System.out.print(d);
                System.out.println();
            }
        } else if (printPuzzle)
            System.out.println("No solution found in time.");

        if (useServer) {
            try {
                socket.close();
            } catch (IOException e) { }
        }

        if (printPuzzle) {
            if (solver.getEndState() != null)
                System.out.printf("num states: %d\n",
                        solver.getEndState().getNumMoves());
            System.out.printf("num expanded: %d\n", num);
            System.out.printf("time: %d\n", time);
        }

        System.exit(solver.getEndState() == null ? 1 : 0);
    }
}
