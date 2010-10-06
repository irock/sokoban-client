package Sokoban;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.Map.Entry;
import java.util.Set;
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
     * Tells whether a path of directions should be generated and written when
     * a path is found. This applies both to stdout and server.
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
     * The maximum number of searches to do. After this number of expanded
     * nodes, the search is considered failed.
     */
    static int searchLimit = 50000;

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
     * @return the start state of the map.
     */
    public State getStartState() {
        return startState;
    }

    /**
     * Do a breadth first search for a solution.
     *
     * @param heuristic The heuristic to use.
     * @return the number of expanded nodes before a solution was found,
     * -1 if no solution was found in time and 0 if the search tree got
     *  exhausted before a solution was found.
     */
    public int breadthFirstSearch(Comparator<State> heuristic) {
        int numExpanded = 0;
        int numInspected = 0;

        Queue<State> queue = new PriorityQueue<State>(1000, heuristic);
        Set<State> visited = new HashSet<State>();

        queue.add(startState);
        visited.add(startState);

        while (!queue.isEmpty() && numExpanded < searchLimit) {
            State curState = queue.poll();
            numExpanded++;
            if (printProgress)
                System.out.printf("expanded: %6d, inspected: %6d, num filled: %2d\r",
                        numExpanded, numInspected, curState.getNumBoxesInGoal());

            for (Entry<Direction, Point> move : curState.getAvailableMoves()) {
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

        if (printProgress)
            System.out.println();
        return queue.isEmpty() ? 0 : -1;
    }

    /**
     * Run the solver on the puzzle given in args[0].
     */
    public static void main(String[] args) {
        int puzzle;
        String mapString;

        if (args.length != 1) {
            System.out.println("No puzzle number specified.");
            return;
        }

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

        Solver solver = new Solver(mapString);

        if (printPuzzle)
            System.out.println(solver.getStartState());

        Heuristics.MultipleHeuristic heuristic =
            new Heuristics.MultipleHeuristic();

        heuristic.add(new Heuristics.MaxNumDone());
        heuristic.add(new Heuristics.MinStepsFromGoal());

        long time = System.currentTimeMillis();
        int num = solver.breadthFirstSearch(heuristic);
        time = System.currentTimeMillis() - time;

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

        System.out.printf("time: %d\n", time);
    }
}
