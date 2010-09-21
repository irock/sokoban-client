package Sokoban;
import java.util.LinkedList;
import java.util.List;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Queue;
import java.util.Map.Entry;
import java.util.AbstractMap.SimpleEntry;

public class State{
	
    private Map map;
	private Box[] boxes;
    private Set<Point> reachablePositions;
    private Set<Entry<Direction, Box>> availableMoves;
	
	public State(Point position, List<Box> boxes, Map map){
        this.map = map;
		this.boxes = new Box[boxes.size()];

        for(int i = 0; i < boxes.size(); i++)
            this.boxes[i] = boxes.get(i);

        calculateMoves(position);
	}

	@Override
   	public int hashCode() {
		int hash = 0;
		for (Box box : boxes) 
			hash = hash ^ box.getPosition().hashCode();
		hash = hash * 11;
		return (hash + reachablePositions.iterator().next().hashCode()); // TODO <-- fix?
    	}

    private void calculateMoves(Point start) {
        Queue<Point> queue = new LinkedList<Point>();
        Set<Point> positions = new HashSet<Point>();
        Set<Entry<Direction, Box>> moves =
            new HashSet<Entry<Direction, Box>>();

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
                            moves.add(new SimpleEntry<Direction, Box>(d, getBoxByPoint(p)));
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

    public Set<Entry<Direction, Box>> getAvailableMoves() {
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

    /**
	* Check if given state has reached the goal on this map.
	**/
	public boolean goalReached() {
		for (Box box : getBoxes()) {
			if (map.getMatrix()[box.position.y][box.position.x] != 2)
				return false;
		}
		return true;
	}
}
