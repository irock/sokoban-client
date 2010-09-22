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
import java.util.Collections;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

public class State {
    private Map map;
    private Box[] boxes;
    private Set<Point> reachablePositions;
    private Set<Entry<Direction, Box>> availableMoves;
    private State previous;
    private Point start;

    public State(Point position, List<Box> boxes, Map map){
        Box[] tmp = new Box[boxes.size()];

        for(int i = 0; i < tmp.length; i++)
            tmp[i] = boxes.get(i);

        this.map = map;
        this.boxes = tmp;
        this.previous = null;
        this.start = position;
        calculateMoves();
    }

    public State(Point position, Box[] boxes, Map map) {
        this(position, boxes, map, null);
    }

    public State(Point position, Box[] boxes, Map map, State previous) {
        this.map = map;
        this.boxes = boxes;
        this.previous = previous;
        this.start = position;
        calculateMoves();
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (Box box : boxes)
            hash = hash ^ box.hashCode();
        hash = hash * 17;
        return hash + reachablePositions.hashCode();
    }

    private void calculateMoves() {
        Queue<Point> queue = new LinkedList<Point>();
        Set<Point> positions = new HashSet<Point>();
        Set<Entry<Direction, Box>> moves =
            new HashSet<Entry<Direction, Box>>();

        queue.add(start);
        positions.add(start);

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

    public int getNumDone() {
        int num = 0;
        for (Box box : boxes)
            if (map.isGoal(box.getPosition()))
                num++;
        return num;
    }

    public Set<Point> getReachablePositions() {
        return reachablePositions;
    }

    public Map getMap(){
        return map;
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

    public boolean equals(Object o) {
        if (!(o instanceof State))
            return false;

        State state = (State) o;
        if (boxes.length != state.boxes.length)
            return false;

        for (int i = 0; i < boxes.length; i++) {
            boolean found = false;
            for(int j = 0; j < state.boxes.length; j++)
                if(boxes[i].equals(state.boxes[j])) {
                    found = true;
                    break;
                }

            if(!found)
                return false;
        }

        return state.reachablePositions.contains(start);
    }

    public boolean isTrapped() {
        int numDirections = Direction.values().length;
        for (Box box : boxes) {
            for (Direction d : Direction.values()) {
                Point p = new Point(box.getPosition().x+d.dx,
                        box.getPosition().y+d.dy);
                if (map.isWall(p)) {
                    boolean trapped = true;
                    for (int i = -1; trapped && i <= 1; i += 2) {
                        Direction ds = Direction.values()[
                            (d.ordinal()+i+numDirections) % numDirections];

                        Point pos = new Point(box.getPosition());
                        Point above = new Point(p);
                        Point side = new Point(box.getPosition().x + ds.dx,
                                box.getPosition().y + ds.dy);

                        Point pushSide = new Point(box.getPosition().x -ds.dx,
                                box.getPosition().y - ds.dy);

                        if (map.isGoal(pos)) {
                            trapped = false;
                        } else if (!map.isWall(pushSide)) {
                            while (trapped && !map.isWall(pos)) {
                                if (map.isGoal(pos) || !map.isWall(above))
                                    trapped = false;

                                pos.x += ds.dx;
                                pos.y += ds.dy;
                                above.x += ds.dx;
                                above.y += ds.dy;
                                side.x += ds.dx;
                                side.y += ds.dy;
                            }
                        }
                    }
                    if (trapped)
                        return true;
                }
            }
        }
        return false;
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

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer(40 + map.getNumRows() * map.getNumCols());

        buffer.append(String.format("id: %d\n", hashCode()));
        buffer.append(String.format("trapped? %b\n", isTrapped()));

        for (int i = 0; i < map.getNumRows(); i++) {
            for (int j = 0; j < map.getNumCols(); j++) {
                Point p = new Point(j, i);
                if (map.isWall(p)) {
                    buffer.append('#');
                } else if (!isFree(p)) {
                    buffer.append((map.isGoal(p) ? '*' : '$'));
                } else if (map.isGoal(p)) {
                    buffer.append((reachablePositions.contains(p) ? '\\' : '.'));
                } else {
                    buffer.append((reachablePositions.contains(p) ? '/' : ' '));
                }
            }
            buffer.append('\n');
        }

        return buffer.toString();
    }

    public String statePath() {
        return (previous != null ? previous.statePath() + "\n" : "") + toString();
    }

    public List<Direction> directionPath() {
        return previous.directionPath(this);
    }

    private List<Direction> directionPath(State next) {
        List<Direction> directions;
        if (previous != null)
            directions = previous.directionPath(this);
        else
            directions = new LinkedList<Direction>();

        Direction direction = null;
        Point target = null;
        for (int i = 0; i < boxes.length; i++)
            if (boxes[i] != next.boxes[i]) {
                Point from = boxes[i].getPosition();
                Point to = next.boxes[i].getPosition();

                int dx = to.x - from.x;
                int dy = to.y - from.y;

                for (Direction d : Direction.values())
                    if (d.dx == dx && d.dy == dy) {
                        direction = d;
                        break;
                    }

                target = new Point(boxes[i].getPosition());
                target.translate(-dx, -dy);
                break;
            }

        directions.addAll(pathSearch(start, target));
        directions.add(direction);

        return directions;
    }

    public List<Direction> pathSearch(Point from, Point to) {
        Queue<Point> queue = new LinkedList<Point>();
        Set<Point> visited = new HashSet<Point>();
        HashMap<Point, Entry<Point, Direction>> trackback =
            new HashMap<Point, Entry<Point, Direction>>();

        queue.add(from);
        visited.add(from);

        while (!queue.isEmpty()) {
            Point current = queue.poll();

            if (current.equals(to)) {
                LinkedList<Direction> directions = new LinkedList<Direction>();
                while (current != from) {
                    Entry<Point, Direction> previous = trackback.get(current);
                    current = previous.getKey();
                    directions.addFirst(previous.getValue());
                }
                return directions;
            }

            for(Direction d : Direction.values()) {
                Point p = new Point(current.x + d.dx, current.y + d.dy);

                if(map.getMatrix()[p.y][p.x] != 1 && isFree(p)) {
                    if(!visited.contains(p)) {
                        queue.add(p);
                        visited.add(p);
                        trackback.put(p,
                                new SimpleEntry<Point, Direction>(current, d));
                    }
                }
            }
        }

        return null;
    }

    public static State getStateAfterMove(State from, Entry<Direction, Box> move) {
        Direction direction = move.getKey();
        Box box = move.getValue();
        Box[] boxes = new Box[from.boxes.length];

        for (int i = 0; i < boxes.length;i++) {
            if (from.boxes[i] == box)
                boxes[i] = new Box(box.getPosition().x + direction.dx,
                        box.getPosition().y + direction.dy);
            else
                boxes[i] = from.boxes[i];
        }

        return new State(box.getPosition(), boxes, from.getMap(), from);
    }
}
