package Sokoban;

import java.util.Arrays;
import java.util.Set;
import java.util.List;
import java.util.Queue;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.AbstractMap.SimpleEntry;

/**
 * A representation of a state in a Sokoban puzzle. The state is built up by
 * the formation of boxes and the current position of the player.
 */
public class State {
    /**
     * The Sokoban map this state is derived from.
     */
    private Map map;

    /**
     * The boxes in this state.
     */
    private Point[] boxes;

    /**
     * The state that led to this state.
     */
    private State previous;

    /**
     * The point where the player starts in this state.
     */
    private Point start;

    /**
     * The minimum point that is reachable in this state.
     */
    private Point min;

    /**
     * A unique number for this state.
     */
    private int id;

    /**
     * The number of states created in total.
     */
    private static int totalNum = 0;

    /**
     * A temporary point. Used for saving heap memory.
     */
    private static Point tmp = new Point(0, 0);

    /**
     * Used for remembering old result of getStepsFromGoal() since that method
     * is quite expensive.
     */
    int stepsFromGoal;

    /**
     * Used for remembering old result of getGoalDistance() since that method
     * is quite expensive.
     */
    int goalDistance;

    /**
     * Create a new State.
     *
     * @param start The start position in this state.
     * @param boxes The list of boxes in the map.
     * @param map The map this state is derived from.
     */
    public State(Point start, Point[] boxes, Map map) {
        this(start, boxes, map, null);
    }

    /**
     * Create a new State.
     *
     * @param start The start position in this state.
     * @param boxes The list of boxes in the map.
     * @param map The map this state is derived from.
     */
    public State(Point start, List<Point> boxes, Map map) {
        this(start, boxes.toArray(new Point[0]), map);
    }

    /**
     * Create a new State.
     *
     * @param start The start position in this state.
     * @param boxes The array of boxes in the map.
     * @param map The map this state is derived from.
     * @param previous The state this state is derived from.
     */
    public State(Point start, Point[] boxes, Map map, State previous) {
        this.map = map;
        this.min = null;
        this.boxes = boxes;
        this.previous = previous;
        this.start = start;
        this.stepsFromGoal = -1;
        this.goalDistance = -1;
        this.id = totalNum++;

        Arrays.sort(this.boxes);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (Point box : boxes)
            hash = hash ^ box.hashCode();
        hash = hash * 17;
        return hash + getMinPosition().hashCode();
    }

    /**
     * @return the unique id of this state.
     */
    public int getId() {
        return id;
    }

    /**
     * Getter for the reachable positions in this state. Lazy evaluation is
     * used.
     *
     * @return the set of reachable positions in this state.
     */
    private TreeSet<Point> getReachablePositions() {
        Queue<Point> queue = new LinkedList<Point>();
        TreeSet<Point> reachablePositions = new TreeSet<Point>();

        queue.add(start);
        reachablePositions.add(start);

        while (!queue.isEmpty()) {
            Point current = queue.poll();
            for (Direction d : Direction.getArray()) {
                tmp.set(current.x + d.dx, current.y + d.dy);
                if (!map.isWall(tmp) && !hasBox(tmp) &&
                        !reachablePositions.contains(tmp)) {
                    queue.add(tmp);
                    reachablePositions.add(tmp);
                    tmp = new Point(0, 0);
                }
            }
        }

        if (min == null)
            min = getMinPosition(reachablePositions);

        return reachablePositions;
    }

    /**
     * Getter for the minimum position in this state. Lazy evaluation is used.
     *
     * @return the minimum position reachable by the player in this state.
     */
    public Point getMinPosition() {
        if (min == null)
            min = getMinPosition(getReachablePositions());
        return min;
    }

    /**
     * Find the minimum position in a set of reachable positions.
     *
     * @return the minimum position reachable in the given set.
     */
    private Point getMinPosition(TreeSet<Point> reachablePositions) {
        tmp.set(0, 0);
        return reachablePositions.ceiling(tmp);
    }

    /**
     * @return the number of boxes in goal.
     */
    public int getNumBoxesInGoal() {
        int num = 0;
        for (Point box : boxes)
            if (map.isGoal(box))
                num++;
        return num;
    }

    /**
     * @return the number of moves since the beginning of the map. Moves are
     * measured in number of box moves here.
     */
    public int getNumMoves() {
        return previous == null ? 0 : previous.getNumMoves() + 1;
    }

    /**
     * @return the map this state is derived from.
     */
    public Map getMap() {
        return map;
    }

    /**
     * Calculate the available box moves that can be done from this state.
     *
     * @return the list of box moves that are available.
     */
    public List<Entry<Direction, Point>> getAvailableMoves() {
        List<Entry<Direction, Point>> moves =
            new LinkedList<Entry<Direction, Point>>();

        Set<Point> reachable = getReachablePositions();

        for (Point box : boxes)
            for (Direction d : Direction.getArray()) {
                tmp.set(box.x - d.dx, box.y - d.dy);
                if (reachable.contains(tmp)) {
                    tmp.translate(2 * d.dx, 2 * d.dy);
                    if (!map.isWall(tmp) && !map.isForbidden(tmp) &&
                            !hasBox(tmp) && !wouldLock(box, d))
                        moves.add(new SimpleEntry<Direction, Point>(d, box));
                }
            }
        return moves;
    }

    /*
     * Returns an arraylist of the boxes in this state
     */
    public Point[] getBoxes() {
        return boxes;
    }

    /**
     * Checks if the given position contains a box in this state.
     *
     * @param pos The position to examine.
     * @return true iff there is a box on the given Point.
     */
    public boolean hasBox(Point pos) {
        for (int i = 0; i < boxes.length; i++)
            if (boxes[i].equals(pos))
                return true;
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof State))
            return false;

        State state = (State) o;
        for (Point box : boxes)
            if (!state.hasBox(box))
                return false;

        return state.getMinPosition().equals(getMinPosition());
    }

    /**
     * Check if the movement of the given box in the given direction results in
     * a lock, i.e.\ unsoluble game.
     *
     * @param box The box to check.
     * @param direction The direction of movement to move the given box.
     * @return true iff the movement of the given box in the given direction
     * would result in a locked state.
     */
    private boolean wouldLock(Point box, Direction direction) {
        box.translate(direction.dx, direction.dy);
        boolean locked = false;

        for (Point neighbor : boxes)
            if (Math.abs(box.x - neighbor.x) <= 1 &&
                    Math.abs(box.y - neighbor.y) <= 1 &&
                    !map.isGoal(neighbor) && boxIsLocked(neighbor)) {
                locked = true;
                break;
            }

        box.translate(-direction.dx, -direction.dy);
        return locked;
    }

    /**
     * Checks if the given box is locked.
     *
     * @param box The box to check.
     * @return true iff the given box is locked.
     */
    public boolean boxIsLocked(Point box) {
        return boxIsLocked(box, new HashSet<Point>());
    }

    /**
     * Checks if the given box is locked. The set of boxes given are assumed to
     * be locked.
     *
     * @param box The box to check.
     * @param checkedBoxes Boxes priorly checked and assumed to be locked.
     * @return true iff the box is locked.
     */
    private boolean boxIsLocked(Point box, Set<Point> checkedBoxes) {
        int numDirections = Direction.getArray().length;
        checkedBoxes.add(box);

        Point p1 = new Point(0, 0);
        Point p2 = new Point(0, 0);

        for (int i = 0; i < numDirections; i++) {
            Set<Point> tmp = new HashSet<Point>(checkedBoxes);
            Direction d1 = Direction.getArray()[i];
            Direction d2 = Direction.getArray()[(i+1)%numDirections];

            p1.set(box.x+d1.dx, box.y+d1.dy);
            p2.set(box.x+d2.dx, box.y+d2.dy);
            if (isBlocked(p1, tmp) && isBlocked(p2, tmp)) {
                checkedBoxes.addAll(tmp);
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the given Point p is blocked.
     *
     * @param p The point to check if blocked.
     * @param checkedBoxes Boxes already checked and assumed to be locked.
     * @return true iff the point in question is a wall, or iff it's a box and
     * the box in question is locked or is assumed to be locked.
     */
    private boolean isBlocked(Point p, Set<Point> checkedBoxes) {
        return map.isWall(p) || (hasBox(p) &&
                (checkedBoxes.contains(p) || boxIsLocked(p, checkedBoxes)));
    }

    /**
     * Calculate and return the shortest distance each of the boxes in the
     * state must be moved to reach a goal. Each goal can be used multiple
     * times. Boxes are not treated as obstacles.
     *
     * @return the minimal number of moves needed to move all boxes to a goal.
     */
    public int getGoalDistance() {
        if (goalDistance == -1) {
            goalDistance = 0;
            for (Point box : boxes) {
                Queue<Point> queue = new LinkedList<Point>();
                int[][] visited = new int[map.getNumRows()][map.getNumCols()];

                queue.add(box);

                while (!queue.isEmpty()) {
                    Point current = queue.poll();
                    if (map.isGoal(current)) {
                        goalDistance += visited[current.y][current.x];
                        break;
                    }

                    for (Direction d : Direction.getArray()) {
                        int x = current.x + d.dx;
                        int y = current.y + d.dy;
                        if (visited[y][x] == 0 && !map.isWall(x, y)) {
                            queue.add(new Point(x, y));
                            visited[y][x] = visited[current.y][current.x]+1;
                        }
                    }
                }
            }
        }
        return goalDistance;
    }

    /**
     * Calculate and return the players distance from the goal in number of
     * steps. Boxes are not treated as obstacles here.
     *
     * @return the minimum number of steps needed to reach a goal.
     */
    public int getStepsFromGoal() {
        if (stepsFromGoal == -1) {
            Queue<Point> queue = new LinkedList<Point>();
            int[][] visited = new int[map.getNumRows()][map.getNumCols()];

            queue.add(start);

            while (!queue.isEmpty()) {
                Point current = queue.poll();
                if (map.isGoal(current)) {
                    stepsFromGoal = visited[current.y][current.x];
                    break;
                }

                for (Direction d : Direction.getArray()) {
                    int x = current.x + d.dx;
                    int y = current.y + d.dy;
                    if (visited[y][x] == 0 && !map.isWall(x, y)) {
                        queue.add(new Point(x, y));
                        visited[y][x] = visited[current.y][current.x] + 1;
                    }
                }
            }
        }
        return stepsFromGoal;
    }

    /**
     * Goal test. Check if all the boxes are on goal squares.
     */
    public boolean goalReached() {
        for (Point box : boxes)
            if (!map.isGoal(box))
                return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer(map.getNumRows()*map.getNumCols());
        Set<Point> reachable = getReachablePositions();

        buffer.append(String.format("num done: %d\n", getNumBoxesInGoal()));
        buffer.append(String.format("steps from goal: %d\n", getStepsFromGoal()));

        for (int y = 0; y < map.getNumRows(); y++) {
            for (int x = 0; x < map.getNumCols(); x++) {
                tmp.set(x, y);
                if (map.isWall(tmp))
                    buffer.append('#');
                else if (hasBox(tmp))
                    buffer.append((map.isGoal(tmp) ? '*' : '$'));
                else if (map.isGoal(tmp))
                    buffer.append((reachable.contains(tmp) ? '\\' : '.'));
                else if (map.isForbidden(tmp))
                    buffer.append((reachable.contains(tmp) ? '+' : 'x'));
                else
                    buffer.append((reachable.contains(tmp) ? '/' : ' '));
            }
            buffer.append('\n');
        }
        return buffer.toString();
    }

    /**
     * @return a string containing the string representation of all states from
     * the start state to the current.
     */
    public String statePath() {
        return (previous != null ? previous.statePath() + "\n" : "") + toString();
    }

    /**
     * @return a list of directions describing the player movements needed to
     * reach this state from the start state.
     */
    public List<Direction> directionPath() {
        return previous.directionPath(this);
    }

    /**
     * Calculate the movements needed to go from this state to the next state.
     * Append these movements to the movements needed to go from the start
     * state to this state.
     *
     * @param next The state to reach.
     * @return the list of directions needed for the player to go from the
     * start state to this state.
     */
    private List<Direction> directionPath(State next) {
        List<Direction> directions;
        Point from = null;
        Point to = null;

        if (previous != null)
            directions = previous.directionPath(this);
        else
            directions = new LinkedList<Direction>();

        for (Point box : boxes)
            if (!next.hasBox(box)) {
                from = box;
                break;
            }

        for (Point box : next.boxes)
            if (!hasBox(box)) {
                to = box;
                break;
            }

        int dx = to.x - from.x;
        int dy = to.y - from.y;
        directions.addAll(pathSearch(start, new Point(from.x - dx, from.y - dy)));

        for (Direction d : Direction.getArray())
            if (d.dx == dx && d.dy == dy) {
                directions.add(d);
                break;
            }

        return directions;
    }

    /**
     * Search for a path from the Point from to the Point to.
     *
     * @param from The start point.
     * @param to The point to reach.
     * @return a list of directions for the player to go from the start to the
     * end of the path.
     */
    private List<Direction> pathSearch(Point from, Point to) {
        Queue<Point> queue = new LinkedList<Point>();
        Set<Point> visited = new HashSet<Point>();
        HashMap<Point, Entry<Point, Direction>> traceback =
            new HashMap<Point, Entry<Point, Direction>>();

        queue.add(from);
        visited.add(from);

        while (!queue.isEmpty()) {
            Point current = queue.poll();

            if (current.equals(to)) {
                LinkedList<Direction> directions = new LinkedList<Direction>();
                while (current != from) {
                    Entry<Point, Direction> previous = traceback.get(current);
                    current = previous.getKey();
                    directions.addFirst(previous.getValue());
                }
                return directions;
            }

            for (Direction d : Direction.getArray()) {
                tmp.set(current.x + d.dx, current.y + d.dy);
                if (!map.isWall(tmp) && !hasBox(tmp) && !visited.contains(tmp)) {
                    queue.add(tmp);
                    visited.add(tmp);
                    traceback.put(tmp, new SimpleEntry<Point, Direction>(current, d));
                    tmp = new Point(0, 0);
                }
            }
        }
        return null;
    }

    /**
     * Given a point, determine if the point is in a tunnel. If so, move it as
     * far into the tunnel as possible - or to the goal square that is farest
     * into the tunnel. Boxes are treated as obstacles.
     *
     * NOT FINISHED YET.
     *
     * @param start The start point of the check
     * @param direction The direction where the tunnel might be.
     * @return the position that is farest into the tunnel given the above
     * definition.
     */
    public Point getTunnelEndPoint(Point start, Direction direction) {
        int numDirections = Direction.getArray().length;
        Direction left = Direction.getArray()[(direction.ordinal()-1)%numDirections];
        Direction right = Direction.getArray()[(direction.ordinal()+1)%numDirections];
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

    /**
     * Create a new state from the given state and the movement described.
     *
     * @param from The source state.
     * @param move The movement to apply on the source state to reach a new
     * state.
     * @return the new state resulting from applying move on from.
     */
    public static State getStateAfterMove(State from, Entry<Direction, Point> move) {
        Direction direction = move.getKey();
        Point moveBox = move.getValue();
        Point[] boxes = new Point[from.boxes.length];

        for (int i = 0; i < boxes.length; i++) {
            Point box = from.boxes[i];
            if (box.equals(moveBox))
                boxes[i] = new Point(box.x + direction.dx, box.y + direction.dy);
            else
                boxes[i] = box;
        }
        return new State(moveBox, boxes, from.getMap(), from);
    }

    /**
     * Calculate the sum of squared distances of each box to each goal point.
     *
     * @return the sum of squared distances as described above.
     */
    public int getSumOfSquaredDistances() {
        int goalDist = 0;
        for (Point box : boxes)
            for (Point goalPoint : map.getGoals())
                goalDist += (int)Math.pow(box.x - goalPoint.x, 2)
                          + (int)Math.pow(box.y - goalPoint.y, 2);
        return goalDist;
    }
}
