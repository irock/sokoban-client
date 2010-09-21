package Sokoban;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.awt.Point;

public class Solver {

	private static LinkedList<State> queue;

	public static void main(String[] args) throws Exception {
        Map map;

        if(false) {
            Socket socket = new Socket("cvap103.nada.kth.se",5555);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("1");

            map = Map.parse(socket.getInputStream());
        } else {
            String mapString = "#######\n" +
                               "###  ##\n" +
                               "#@$   #\n" +
                               "###.  #\n" +
                               "#######\n";
            map = Map.parse(mapString);
        }

		State state = new State(map.getStart(), map.getBoxes(), map);
		bfsSolver(state);
	}

	public static void bfsSolver(State start) {
		queue = new LinkedList<State>();
		queue.add(start);
		State tmpState;
		Box[] tmpBoxList;
		while (!queue.isEmpty()) {
			State curState = queue.poll();
			for (Entry<Direction, Box> e : curState.getAvailableMoves()) {
				tmpBoxList = curState.getCopyBoxesWithNewMove(e.getValue()
						.getPosition(), e.getKey());
				tmpState = new State(e.getValue().getPosition(), tmpBoxList,
						curState.getMap());

				if (tmpState.goalReached()) {
					System.out.println("WIN!");
					return;
				}
				queue.add(tmpState);
			}
		}
	}
}
