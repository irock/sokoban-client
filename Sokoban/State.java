package Sokoban;

import java.util.Arrays;
import java.util.Set;
import java.util.List;
import java.util.Queue;
import java.util.HashSet;
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
     * Used for remembering old result of getGoalDistance() since that method
     * is quite expensive.
     */
    private int goalDistance;

    /**
     * For holding the result of getTotalScore().
     */
    private float score;

    /**
     * Create a new State.
     *
     * @param start The start position in this state.
     * @param boxes The list of boxes in the map.
     * @param map The map this state is derived from.
     */
    public State(Point start, Point[] boxes, Map map) {
        this(start, boxes, map, null, false);
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
     * @param isSorted true iff the array of boxes already is sorted.
     */
    public State(Point start, Point[] boxes, Map map, State previous, boolean isSorted) {
        this.map = map;
        this.min = null;
        this.boxes = boxes;
        this.previous = previous;
        this.start = start;
        this.goalDistance = -1;
        this.score = 1000;

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
     * Getter for the reachable positions in this state. Lazy evaluation is
     * used.
     *
     * @return the set of reachable positions in this state.
     */
    private Set<Point> getReachablePositions() {
        Queue<Point> queue = new LinkedList<Point>();
        Set<Point> reachablePositions = new HashSet<Point>();

        queue.add(start);
        reachablePositions.add(start);

        while (!queue.isEmpty()) {
            Point current = queue.poll();
            for (Direction d : Direction.getArray()) {
                Point p = map.getPoint(current.x + d.dx, current.y + d.dy);
                if (!map.isWall(p) && !hasBox(p) &&
                        !reachablePositions.contains(p)) {
                    queue.add(p);
                    reachablePositions.add(p);
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
    private Point getMinPosition(Set<Point> reachablePositions) {
        Point min = null;
        for (Point point : reachablePositions)
            if (min == null || point.compareTo(min) < 0)
                min = point;
        return min;
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
                Point p = map.getPoint(box.x - d.dx, box.y - d.dy);
                if (reachable.contains(p)) {
                    p = map.getPoint(p.x + 2*d.dx, p.y + 2*d.dy);
                    if (!map.isWall(p) && !map.isForbidden(p) &&
                            !hasBox(p) && !wouldLock(box, d) &&
                            wouldBeConsistent(box, d))
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

    /**
     * Checks if the given position contains a box in this state.
     *
     * @param x The x-coordinate of the point to examine.
     * @param y The y-coordinate of the point to examine.
     * @return true iff there is a box on the given Point.
     */
    public boolean hasBox(int x, int y) {
        return hasBox(map.getPoint(x, y));
    }

    /**
     * @return the total score for this state.
     */
    public float getTotalScore() {
        if (score == 1000) {
            score = 0;
            for (Point box : boxes)
                score += map.getScore(box);
        }
        return score;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof State))
            return false;

        State state = (State) o;
        /* Use the fact that the boxes are ordered. */
        for (int i = 0; i < boxes.length; i++)
            if (!boxes[i].equals(state.boxes[i]))
                return false;

        return state.getMinPosition().equals(getMinPosition());
    }

    /**
     *  If we put a box at given point, this method will tell us if that would
     *  generate a blocking cycle. This method don't have to check all possible
     *  positions around the box for cycles, but the brute force method is much
     *  easier to understand and not very expensive.
     */
    private boolean wouldCreateBlockingCycle(Point p) {
        for (int dx = -1; dx <= 1; dx++)
            for (int dy = -1; dy <= 1; dy++)
                for (Direction d : Direction.getArray())
                    if (wouldCreateBlockingCycle(map.getPoint(p.x+dx, p.y+dy), d))
                        return true;
        return false;
    }

    /**
     * Check if theres a blocking, clockwise cycle starting in the given point
     * directed in the given direction. A blocking cycle is a cycle in which
     * the surrounding "walls" contains at least one box that cannot be moved
     * to a goal square.
     *
     * There are all in all four interesting points:
     *
     * 123 
     * ↑ 4
     *
     * The arrow indicates the current position and direction. Either the cycle
     * continues straight ahead, or it turns right (and right again). A turn is
     * indicated by one of the following patterns:
     *
     * Pattern 1: 124
     * Pattern 2: 234
     *
     * @param pos The position from where to start looking for a cycle.
     * @param forward The direction in which to look for a cycle.
     * @return true iff there is a blocking cycle starting in the given
     * position, heading in the given direction.
     */
    private boolean wouldCreateBlockingCycle(Point pos, Direction forward) {
        if (!(map.isWall(pos) || hasBox(pos)))
            return false;

        Point source = pos;
        boolean turned = false;
        int turns = 0;
        int notInGoal = 0;
        int goalsFound = 0;

        while (turns < 3) {
            Direction right = forward.getRelative(1);

            /* check so that we haven't reached the end of the map. */
            int xlimit = pos.x + forward.dx + 2*right.dx;
            int ylimit = pos.y + forward.dy + 2*right.dy;
            if (xlimit < 0 || ylimit < 0 || xlimit > map.getNumCols()-1 ||
                    ylimit > map.getNumRows()-1)
                return false;

            Point p1 = map.getPoint(pos.x + forward.dx, pos.y + forward.dy);
            Point p2 = map.getPoint(pos.x + forward.dx + right.dx,
                    pos.y + forward.dy + right.dy);
            Point p3 = map.getPoint(pos.x + forward.dx + 2*right.dx,
                    pos.y + forward.dy + 2*right.dy);
            Point p4 = map.getPoint(pos.x + 2*right.dx, pos.y + 2*right.dy);
            Point p5 = map.getPoint(pos.x + right.dx, pos.y + right.dy);

            if (start.equals(p5) || !(map.isWall(p4) || hasBox(p4)))
                return false;

            if (!hasBox(p5) && map.isGoal(p5))
                ++goalsFound;

            if (!turned &&
                    ((map.isWall(p1) || hasBox(p1)) ||
                        (map.isWall(p3) || hasBox(p3))) &&
                     (map.isWall(p2) || hasBox(p2)) &&
                     (map.isWall(p4) || hasBox(p4))) {

                notInGoal += (hasBox(p1) && !map.isGoal(p1) ? 1 : 0) +
                              (hasBox(p2) && !map.isGoal(p2) ? 1 : 0) +
                              (hasBox(p3) && !map.isGoal(p3) ? 1 : 0) +
                              (hasBox(p4) && !map.isGoal(p4) ? 1 : 0);

                if (p1.equals(source) || p2.equals(source) ||
                        p3.equals(source) || p4.equals(source))
                    break;

                pos = p4;
                forward = forward.getRelative(2);
                turned = true;
                ++turns;
            } else if (map.isWall(p1) || hasBox(p1)) {
                if (p1.equals(source))
                    break;
                pos = p1;
                turned = false;
                notInGoal += hasBox(p1) && !map.isGoal(p1) ? 1 : 0;
            } else {
                return false;
            }
        }

        return (notInGoal - goalsFound/2) > 0;
    }


    /**
     * Check if the movement of the given box in the given direction results in
     * a lock, i.e.\ unsolvable game.
     *
     * @param box The box to check.
     * @param direction The direction of movement to move the given box.
     * @return true iff the movement of the given box in the given direction
     * would result in a locked state.
     */
    private boolean wouldLock(Point box, Direction direction) {
        int index = 0;
        for (int i = 0; i < boxes.length; i++)
            if (boxes[i].equals(box))
                index = i;

        Point backup = box;
        box = map.getPoint(box.x + direction.dx, box.y + direction.dy);
        boxes[index] = box;
        boolean locked = false;

        for (int dx = -1; !locked && dx <= 1; dx++)
            for (int dy = -1; dy <= 1; dy++) {
                Point p = map.getPoint(box.x + dx, box.y + dy);
                if ((dx == 0 && dy == 0) || start.equals(p) || map.isWall(p) ||
                        map.isGoal(p))
                    continue;

                /* safe to assume that p is not a map edge. */
                Point up = map.getPoint(p.x, p.y-1);
                Point down = map.getPoint(p.x, p.y+1);
                Point left = map.getPoint(p.x-1, p.y);
                Point right = map.getPoint(p.x+1, p.y);

                if ((!map.isWall(up) && !hasBox(up)) ||
                        (!map.isWall(down) && !hasBox(down)) ||
                        (!map.isWall(left) && !hasBox(left)) ||
                        (!map.isWall(right) && !hasBox(right)))
                    continue;

                /* up, down, left, right is blocked. */
                int notInGoal = 0;
                for (int ddx = -1; notInGoal == 0 && ddx <= 1; ddx++)
                    for (int ddy = -1; ddy <= 1; ddy++) {
                        Point pp = map.getPoint(p.x + ddx, p.y + ddy);
                        if (!map.isGoal(pp) && hasBox(pp)) {
                            notInGoal++;
                            break;
                        }
                    }

                if (notInGoal > 0 &&
                        (((map.isWall(p.x-1, p.y-1) || hasBox(p.x-1, p.y-1)) &&
                          (map.isWall(p.x+1, p.y+1) || hasBox(p.x+1, p.y+1))) ||
                         ((map.isWall(p.x-1, p.y+1) || hasBox(p.x-1, p.y+1)) &&
                          (map.isWall(p.x+1, p.y-1) || hasBox(p.x+1, p.y-1))))) {
                    locked = true;
                    break;
                }
            }

        if (!locked)
            for (Point neighbor : boxes)
                if (Math.abs(box.x - neighbor.x) <= 1 &&
                        Math.abs(box.y - neighbor.y) <= 1 &&
                        !map.isGoal(neighbor) && boxIsLocked(neighbor)) {
                    locked = true;
                    break;
                }

        if (!locked && wouldCreateBlockingCycle(box))
            locked = true;

        boxes[index] = backup;

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

        for (int i = 0; i < numDirections; i++) {
            Set<Point> children = new HashSet<Point>(checkedBoxes);
            Direction d1 = Direction.getArray()[i];
            Direction d2 = Direction.getArray()[(i+1)%numDirections];

            Point p1 = map.getPoint(box.x + d1.dx, box.y + d1.dy);
            Point p2 = map.getPoint(box.x + d2.dx, box.y + d2.dy);
            if (isBlocked(p1, children) && isBlocked(p2, children)) {
                checkedBoxes.addAll(children);
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
     * A greedy method for finding the sum of distances between goal squares
     * and boxes. The boxes and goal squares are paired in a greedy way so that
     * each point in order finds the nearest box.
     *
     * @return the sum of distances between each box in the state and a goal
     * square in such a way so that each goal square is paired with one box.
     */
    public int getGoalDistance() {
        if (goalDistance == -1) {
            List<Point> goalsLeft = new LinkedList<Point>();
            Set<Point> boxesLeft = new HashSet<Point>();
            goalsLeft.addAll(map.getGoals());

            for (Point box : boxes)
                if (map.isGoal(box))
                    goalsLeft.remove(box);
                else
                    boxesLeft.add(box);

            goalDistance = 0;

            for (Point goal : goalsLeft) {
                Entry<Point, Integer> answer = findMinPathLength(goal, boxesLeft);
                boxesLeft.remove(answer.getKey());
                goalDistance += answer.getValue();
            }
        }
        return goalDistance;
    }

    /**
     * Finds the minimum path from the point given to any of the points in the
     * given destination set.
     *
     * @param from The source in the path.
     * @param dst The set of possible destinations.
     * @return the minimum path from the point from to one of the points in the
     * destination set.
     */
    private Entry<Point, Integer> findMinPathLength(Point from, Set<Point> dst) {
        Queue<Point> queue = new LinkedList<Point>();
        int[][] visited = new int[map.getNumRows()][map.getNumCols()];

        if (dst.contains(from))
            return new SimpleEntry<Point, Integer>(from, 0);

        queue.add(from);

        while (!queue.isEmpty()) {
            Point current = queue.poll();
            for (Direction d : Direction.getArray()) {
                int x = current.x + d.dx;
                int y = current.y + d.dy;
                if (visited[y][x] == 0 && !map.isWall(x, y) && !map.isForbidden(x, y)) {
                    Point to = new Point(x, y);
                    if (dst.contains(to))
                        return new SimpleEntry<Point, Integer>(
                                to, visited[current.y][current.x] + 1);

                    queue.add(to);
                    visited[y][x] = visited[current.y][current.x] + 1;
                }
            }
        }
        return null;
    }

    /**
     * Calculate and return the players distance from the goal in number of
     * steps. Boxes are not treated as obstacles here.
     *
     * @return the minimum number of steps needed to reach a goal.
     */
    public int getStepsFromGoal() {
        Queue<Point> queue = new LinkedList<Point>();
        int[][] visited = new int[map.getNumRows()][map.getNumCols()];

        int stepsFromGoal = 0;
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

        buffer.append(String.format("score: %d\n", getNumBoxesInGoal()));
        buffer.append(String.format("distance: %d\n", getGoalDistance()));

        for (int y = 0; y < map.getNumRows(); y++) {
            for (int x = 0; x < map.getNumCols(); x++) {
                Point p = map.getPoint(x, y);
                if (map.isWall(p))
                    buffer.append('#');
                else if (hasBox(p))
                    buffer.append((map.isGoal(p) ? '*' : '$'));
                else if (map.isGoal(p))
                    buffer.append((reachable.contains(p) ? '+' : '.'));
                else if (map.isForbidden(p))
                    buffer.append('x');
                else
                    buffer.append((reachable.contains(p) ? ' ' : '-'));
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
                Point p = map.getPoint(current.x + d.dx, current.y + d.dy);
                if (!map.isWall(p) && !hasBox(p) && !visited.contains(p)) {
                    queue.add(p);
                    visited.add(p);
                    traceback.put(p, new SimpleEntry<Point, Direction>(current, d));
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
        Direction left = direction.getRelative(-1);
        Direction right = direction.getRelative(1);
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

        int index = 0;
        Point newBox = new Point(moveBox.x+direction.dx, moveBox.y+direction.dy);

        /* Insert boxes in sorted order. */
        for (int i = 0; i < boxes.length; i++) {
            Point box = from.boxes[i];

            if (box.equals(moveBox))
                continue;

            if (newBox != null && box.compareTo(newBox) > 0) {
                boxes[index++] = newBox;
                newBox = null;
            }

            boxes[index++] = box;
        }

        if (newBox != null)
            boxes[index] = newBox;
        return new State(moveBox, boxes, from.getMap(), from, true);
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

    private boolean wouldBeConsistent(Point box, Direction direction) {
        int index = 0;
        for (int i = 0; i < boxes.length; i++)
            if (boxes[i].equals(box))
                index = i;

        boxes[index] = map.getPoint(box.x + direction.dx, box.y + direction.dy);
        boolean consistent = isConsistent();
        boxes[index] = box;
        return consistent;
    }

    /**
     * A three level check of consistency. Checks that given any three boxes in
     * the state, they can each be placed on a different goal.
     *
     * @return true iff the state is consistent.
     */
    private boolean isConsistent() {
        for (int i = 0; i < boxes.length; i++) {
            Point box1 = boxes[i];
            Set<Point> r1 = map.getReachableGoals(box1);
            if (r1.size() > 2)
                continue;

            for (int j = i+1; j < boxes.length; j++) {
                Point box2 = boxes[j];
                Set<Point> r2 = map.getReachableGoals(box2);
                if (r2.size() > 2)
                    continue;

                for (int k = j+1; k < boxes.length; k++) {
                    Point box3 = boxes[k];
                    Set<Point> r3 = map.getReachableGoals(box3);
                    if (r3.size() > 2)
                        continue;

                    boolean found = false;
                    for (Point p1 : r1)
                        for (Point p2 : r2)
                            for (Point p3 : r3)
                                if (!p1.equals(p2) && !p1.equals(p3) &&
                                        !p2.equals(p3)) {
                                    found = true;
                                    break;
                                }
                    if (!found)
                        return false;
                }
            }
        }
        return true;
    }
}
