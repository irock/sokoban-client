package Sokoban;

import java.awt.Point;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.HashSet;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.Comparator;

// Unsolved puzzles: 8, 38, 52, 56, 57, 61, 64, 69

public class Solver {
    public class NumDoneHeuristic implements Comparator<State> {
        public int compare(State a, State b) {
            return b.getNumDone() - a.getNumDone();
        }
    }

    static boolean printStatePath = false;
    static boolean printDirectionPath = true;
    static boolean printPuzzle = true;
    static boolean useServer = true;
    static int searchLimit = 500000;

    Map map;
    State startState;
    State endState;

    public Solver(String mapString) {
        map = Map.parse(mapString);
        startState = new State(map.getStart(), map.getBoxes(), map);
        endState = null;
    }

    public int breadthFirstSearch() {
        int numInspected = 0;
        Comparator<State> heuristic = new NumDoneHeuristic();
        Queue<State> queue = new PriorityQueue<State>(200000, heuristic);
        Set<State> visited = new HashSet<State>();

        queue.add(startState);
        visited.add(startState);

        while (!queue.isEmpty() && visited.size() < searchLimit) {
            State curState = queue.poll();
            System.out.printf("visited: %d, inspected: %d, num filled: %d\r",
                    visited.size(), numInspected, curState.getNumDone());

            for (Entry<Direction, Box> move : curState.getAvailableMoves()) {
                State nextState = State.getStateAfterMove(curState, move);
                numInspected++;

                if (!nextState.isTrapped() && !visited.contains(nextState)) {
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
            } catch (IOException e) {
                System.err.println(e.getMessage());
                return;
            }
        } else {
            mapString = Puzzle.getPuzzle(puzzle);
        }

        if (printPuzzle)
            System.out.println(mapString);

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
        }

        if (useServer) {
            try {
                socket.close();
            } catch (IOException e) { }
        }

        if (num == 0)
            System.out.println("No solution found                             ");
        else
            System.out.printf("Total number of generated states: %d\n", num);

        System.out.printf("time: %d\n", System.currentTimeMillis()-start);
    }
}
