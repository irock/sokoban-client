package Sokoban;
import java.util.LinkedList;
import java.util.List;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Queue;

public class State{
	
    private Map map;
	private Box[] boxes;
    private Set<Point> reachablePositions;
    private Set<java.util.Map.Entry<Direction, Box>> availableMoves;
	
	public State(Point position, List<Box> boxes, Map map){
        this.map = map;
		this.boxes = new Box[boxes.size()];

        for(int i = 0; i < boxes.size(); i++)
            this.boxes[i] = boxes.get(i);

        calculateMoves(position);
	}

    private void calculateMoves(Point start) {
        Queue<Point> queue = new LinkedList<Point>();
        Set<Point> positions = new HashSet<Point>();
        Set<java.util.Map.Entry<Direction, Box>> moves =
            new HashSet<java.util.Map.Entry<Direction, Box>>();

        queue.add(start);

        while(!queue.isEmpty()) {
            Point current = queue.poll();

            for(Direction d : Direction.values()) {
                Point p = new Point(current.x + d.dx, current.y + d.dy);
                
                if(map.getMatrix()[p.y][p.x] != 1) {
                    if(isFree(p)) {
                        if(!positions.contains(p)) {
                            queue.add(p);
                            positions.add(p);
                        }
                    } else {
                        Point p2 = new Point(p.x + d.dx, p.y + d.dy);
                        if(map.getMatrix()[p2.y][p2.x] != 1 && isFree(p2))
                            moves.add(new java.util.AbstractMap.SimpleEntry<Direction, Box>(d, getBoxByPoint(p)));
                    }
                }
            }
        }

        availableMoves = moves;
        reachablePositions = positions;
    }

    public Set<Point> getReachablePositions() {
        return reachablePositions;
    }

    public Set<java.util.Map.Entry<Direction, Box>> getAvailableMoves() {
        return availableMoves;
    }

	/*
	 * Returns an arraylist of the boxes in this state
	 */
	public Box[] getBoxes(){
		return boxes;
	}

	/*
	 * Get box if it is at the position and else returns null
	 */
	public Box getBoxByPoint(Point pos){
        for(Box box : boxes) {
			if(pos.equals(box.position)){
				return box;
			}
		}
        return null;
	}

	public boolean isFree(Point pos){
        for(Box box : boxes) {
			if(pos.equals(box.position)){
				return false;
			}
		}
		return true;
	}

    public boolean equals(State state) {
        if(boxes.length != state.boxes.length)
            return false;

        for(int i = 0; i < boxes.length; i++) {
            boolean found = false;
            for(int j = 0; j < state.boxes.length; j++)
                if(boxes[i].equals(state.boxes[j])) {
                    found = true;
                    break;
                }

            if(!found)
                return false;
        }

        return true;
    }
}
