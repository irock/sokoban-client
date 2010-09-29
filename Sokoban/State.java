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

        queue.add(start);
        positions.add(start);

        while(!queue.isEmpty()) {
            Point current = queue.poll();

            for(Direction d : Direction.values()) {
                Point p = new Point(current.x + d.dx, current.y + d.dy);

                if(!map.isWall(p) && isFree(p)) {
                    if(!positions.contains(p)) {
                        queue.add(p);
                        positions.add(p);
                    }
                }
            }
        }

        reachablePositions = positions;
    }

    public int getNumDone() {
        int num = 0;
        for (Box box : boxes)
            if (map.isGoal(box.getPosition()))
                num++;
        return num;
    }

    public int getNumMoves() {
        return previous == null ? 0 : previous.getNumMoves() + 1;
    }

    public Set<Point> getReachablePositions() {
        return reachablePositions;
    }

    public Map getMap(){
        return map;
    }

    public List<Entry<Direction, Box>> getAvailableMoves() {
        List<Entry<Direction, Box>> moves = new LinkedList<Entry<Direction, Box>>();

        for (Point from : getReachablePositions()) {
            for (Box box : getBoxes()) {
                Point p = box.getPosition();
                int dx = p.x-from.x;
                int dy = p.y-from.y;

                if (((dx == -1 || dx == 1) && dy == 0) ||
                        ((dy == -1 || dy == 1) && dx == 0)) {
                    Point to = new Point(from.x+2*dx, from.y+2*dy);
                    if (!map.isWall(to) && isFree(to))
                        moves.add(new SimpleEntry<Direction, Box>(
                                    Direction.getDirection(dx, dy), box));
                }
            }
        }
        return moves;
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

    public boolean wouldBeTrapped(Direction direction, Box box) {
        Point src = new Point(box.getPosition());
        Point dst = new Point(src);
        dst.translate(direction.dx, direction.dy);

        box.setPosition(dst);
        boolean trapped = isTrapped();
        box.setPosition(src);

        return trapped;
    }

    public boolean boxIsLocked(Box box) {
        return boxIsLocked(box, new HashSet<Box>());
    }

    public boolean boxIsLocked(Box box, Set<Box> checkedBoxes) {
        int numDirections = Direction.values().length;
        checkedBoxes.add(box);

        for (int i = 0; i < numDirections; i++) {
            Set<Box> tmp = new HashSet<Box>(checkedBoxes);
            Direction d1 = Direction.values()[i];
            Direction d2 = Direction.values()[(i+1)%numDirections];
            Point p1 = new Point(box.getPosition().x+d1.dx,
                    box.getPosition().y+d1.dy);
            Point p2 = new Point(box.getPosition().x+d2.dx,
                    box.getPosition().y+d2.dy);

            if (isBlocked(p1, tmp) && isBlocked(p2, tmp)) {
                checkedBoxes.addAll(tmp);
                return true;
            }
        }
        return false;
    }

    private boolean isBlocked(Point p, Set<Box> checkedBoxes) {
        if (map.isWall(p))
            return true;

        Box box = getBoxByPoint(p);
        if (box != null)
            if (checkedBoxes.contains(box) || boxIsLocked(box, checkedBoxes))
                return true;

        return false;
    }

    public boolean boxIsTrapped(Box box) {
        int numDirections = Direction.values().length;
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
        return false;
    }

    public boolean isTrapped() {
        int numDirections = Direction.values().length;
        for (Box box : boxes) {
            if (false && (!map.isGoal(box.getPosition()) &&
                    boxIsLocked(box)) && !boxIsTrapped(box))
                System.out.println(this);
            if ((!map.isGoal(box.getPosition()) && boxIsLocked(box)))
                return true;
            else if (boxIsTrapped(box))
                return true;
        }
        return false;
    }

    public int getGoalDistance() {
        int total = 0;
        for (Box box : boxes) {
            Queue<Point> queue = new LinkedList<Point>();
            int[][] visited = new int[map.getNumRows()][map.getNumCols()];

            queue.add(box.getPosition());

            while (!queue.isEmpty()) {
                Point current = queue.poll();
                if (map.isGoal(current)) {
                    total += visited[current.y][current.x];
                    break;
                }

                for(Direction d : Direction.values()) {
                    Point p = new Point(current.x + d.dx, current.y + d.dy);

                    if(visited[p.y][p.x] == 0 && !map.isWall(p)) {
                        queue.add(p);
                        visited[p.y][p.x] = visited[current.y][current.x]+1;
                    }
                }
            }
        }
        return total;
    }

    public int getStepsFromGoal() {
        Queue<Point> queue = new LinkedList<Point>();
        int[][] visited = new int[map.getNumRows()][map.getNumCols()];

        queue.add(start);

        while (!queue.isEmpty()) {
            Point current = queue.poll();
            if (map.isGoal(current))
                return visited[current.y][current.x];

            for(Direction d : Direction.values()) {
                Point p = new Point(current.x + d.dx, current.y + d.dy);

                if(visited[p.y][p.x] == 0 && !map.isWall(p)) {
                    queue.add(p);
                    visited[p.y][p.x] = visited[current.y][current.x]+1;
                }
            }
        }
        return Integer.MAX_VALUE;
    }

    /**
    * Check if given state has reached the goal on this map.
    **/
    public boolean goalReached() {
        for (Box box : getBoxes()) {
            if (!map.isGoal(box.getPosition()))
                return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer(map.getNumRows()*map.getNumCols());

        buffer.append(String.format("hash: %d\n", hashCode()));
        buffer.append(String.format("trapped? %b\n", isTrapped()));
        buffer.append(String.format("dist: %d\n", getGoalDistance())); 
        buffer.append(String.format("bird dist^2: %d\n", getAvgGoalDist()));
        
        int numGoal = 0;
        int numLocked = 0;
        for (Box box : boxes) {
            if (boxIsLocked(box))
                numLocked++;
            if (map.isGoal(box.getPosition()))
                numGoal++;
        }

        buffer.append(String.format("locked: %d\n", numLocked));
        buffer.append(String.format("in goal: %d\n", numGoal));

        for (int i = 0; i < map.getNumRows(); i++) {
            buffer.append('\n');
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

                if(!map.isWall(p) && isFree(p)) {
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

    public Point getTunnelEndPoint(Point start, Direction direction) {
        int numDirections = Direction.values().length;
        Direction left = Direction.values()[(direction.ordinal()-1)%numDirections];
        Direction right = Direction.values()[(direction.ordinal()+1)%numDirections];
        Point last = start;
        Point current = new Point(start);
        Point goal = null;

        while (!map.isWall(current)) {
            Point leftSide = new Point(current.x+left.dx, current.y+left.dy);
            Point rightSide = new Point(current.x+right.dx, current.y+right.dy);

            if (map.isGoal(current))
                goal = current;

            if (!map.isWall(leftSide) || !map.isWall(rightSide))
                return null;
            last = current;
            current = new Point(current.x+direction.dx, current.y+direction.dy);
        }
        return goal != null ? goal : last;
    }

    public static State getStateAfterMove(State from, Entry<Direction, Box> move) {
        Direction direction = move.getKey();
        Box box = move.getValue();
        Box[] boxes = new Box[from.boxes.length];

        for (int i = 0; i < boxes.length;i++) {
            if (from.boxes[i] == box) {
                //Point p = from.getTunnelEndPoint(
                boxes[i] = new Box(box.getPosition().x + direction.dx,
                        box.getPosition().y + direction.dy);
            } else
                boxes[i] = from.boxes[i];
        }

        return new State(box.getPosition(), boxes, from.getMap(), from);
    }

    public int getAvgGoalDist() {
        int goalDist = 0;
        for (Box box : boxes) {
            for (Point goalPoint : map.getGoals()) {
                goalDist += (int)Math.pow(box.getPosition().x - goalPoint.x, 2)
                          + (int)Math.pow(box.getPosition().y - goalPoint.y, 2);
            }
        }
        return goalDist;
    }
}
