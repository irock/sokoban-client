package Sokoban;

import java.util.LinkedList;
import java.util.Map.Entry;

public class Solver {
	
	private LinkedList<State> queue;

	/**
	 * 
	 */
	public static void main(String[] args) {
		
		
	}
	public void bfsSolver(){
		queue = new LinkedList<State>();
		State tmpState;
		Box[] tmpBoxList;
		while(!queue.isEmpty()){
			State curState = queue.poll();
			for(Entry<Direction, Box> e : curState.getAvailableMoves()){
				tmpBoxList = curState.getCopyBoxesWithNewMove(e.getValue().getPosition(),e.getKey());
				tmpState = new State(e.getValue().getPosition(), tmpBoxList, curState.getMap() );
				queue.add(tmpState);
			}
		}
	}
}

	