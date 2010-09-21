package Sokoban;

import java.util.LinkedList;
import java.util.Map.Entry;

public class Solver {

	private static LinkedList<State> queue;

	/**
	 * 
	 */
	public static void main(String[] args) {
		String mapString = "################\n" + "#@  $         .#\n"
				+ "##  #          #\n" + "################\n";
		Map map = Map.parse(mapString);

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
