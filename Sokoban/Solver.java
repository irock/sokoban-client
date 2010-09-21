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

public class Solver {
    public class NumDoneHeuristic implements Comparator<State> {
        public int compare(State a, State b) {
            return b.getNumDone() - a.getNumDone();
        }
    }

    static boolean printPath = false;
    static boolean printPuzzle = false;
    static boolean useServer = false;
    static int searchLimit = 200000;

    Map map;
    State startState;
    State endState;

    public Solver(String mapString) {
        map = Map.parse(mapString);
		startState = new State(map.getStart(), map.getBoxes(), map);
        endState = null;
    }

	public int breadthFirstSearch() {
        int num = 1;
        Comparator<State> heuristic = new NumDoneHeuristic();
		Queue<State> queue = new PriorityQueue<State>(200000, heuristic);
        Set<State> visited = new HashSet<State>();

		queue.add(startState);
        visited.add(startState);

		while (!queue.isEmpty() && num < searchLimit) {
			State curState = queue.poll();
            System.out.printf("states expanded: %d, num filled: %d\r", num,
                    curState.getNumDone());

			for (Entry<Direction, Box> move : curState.getAvailableMoves()) {
                State nextState = State.getStateAfterMove(curState, move);

                if (!nextState.isTrapped() && !visited.contains(nextState)) {
                    num++;
                    if (nextState.goalReached()) {
                        endState = nextState;
                        return num;
                    }
                    queue.add(nextState);
                    visited.add(nextState);
                }
			}
		}
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

        if (useServer) {
            try {
                char[] buffer = new char[1024];
                Socket socket = new Socket("cvap103.nada.kth.se", 5555);
                InputStreamReader in = new InputStreamReader(socket.getInputStream());
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println(String.format("%d", puzzle));

                in.read(buffer, 0, buffer.length);
                mapString = new String(buffer);
                socket.close();
            } catch (IOException e) {
                System.err.println(e.getMessage());
                return;
            }
        } else {
            mapString = Puzzle.getPuzzle(puzzle);
        }

        Solver solver = new Solver(mapString);

        long start = System.currentTimeMillis();
		int num = solver.breadthFirstSearch();

        if (num == 0)
            System.out.println("No solution found");
        else
            System.out.printf("Total number of generated states: %d\n", num);


        if (printPuzzle) {
            boolean nl = false;
            System.err.printf("        // Puzzle %d\n", puzzle);
            System.err.print("        \"");
            for (char c : mapString.toCharArray()) {
                if (c == '\0')
                    break;

                if (nl) {
                    System.err.print("\\n\" +\n        \"");
                    nl = false;
                }

                if (c == '\n')
                    nl = true;
                else
                    System.err.print(c);
            }
            System.err.print("\",\n");

            if (num == 0)
                System.err.println("        // no solution found");
            else
                System.err.printf("        // number of generated states: %d\n", num);
            System.err.println();
        }

        if (printPath && solver.endState != null) {
            System.out.println(solver.endState.path());
        }

        System.out.printf("time: %d\n", System.currentTimeMillis()-start);
	}
}
