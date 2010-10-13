package Sokoban;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.Queue;

/**
 * A representation of a map in the Sokoban game. The map is a puzzle and
 * should be solved by the player by moving boxes to the goals until all boxes
 * are standing on goal squares.
 */
public class Map {
    /**
     * A representation of a square in the map. Each position on the map is a
     * square and each square can be either a goal, a wall or neither.
     */
    private enum Square {
        NONE, GOAL, WALL, INVALID
    };

    /**
     * The start position. The position where the player should start.
     */
    private Point start;

    /**
     * The boxes in the map.
     */
    private List<Point> boxes;

    /**
     * A convenience list of all goals in the map.
     */
    private List<Point> goals;

    /**
     * All squares in the map, represented with a matrix.
     */
    private Square[][] matrix;

    /**
     * A set of points covering the whole map, for saving memory.
     */
    private Point[][] points;

    /**
     * A set of goals reachable in each point of the map.
     */
    private Set<Point>[][] goalsReachable;

    /**
     * A calculated score for each point in the map.
     */
    private float[][] scores;

    /**
     * Create a new Map.
     *
     * @param start The start position of the player.
     * @param matrix The map board.
     * @param points The points covering the map.
     */
    private Map (Point start, Square[][] matrix, List<Point> boxes,
            Point[][] points) {
        this.matrix = matrix;
        this.boxes = boxes;
        this.start = start;
        this.goals = new LinkedList<Point>();
        this.points = points;

        /* Find all goals in the map. */
        for (int y = 0; y < this.matrix.length; y++)
            for (int x = 0; x < this.matrix[0].length; x++)
                if (this.matrix[y][x] == Square.GOAL)
                    this.goals.add(points[y][x]);

        goalsReachable = findReachableGoals();
        scores = findScores();
    }

    /**
     * Getter for map points, for saving memory.
     *
     * @param x The x-coordinate of the point to return.
     * @param y The y-coordinate of the point to return.
     * @return the point representing the given x and y coordinates.
     */
    public Point getPoint(int x, int y) {
        return points[y][x];
    }

    /**
     * Getter for scores.
     *
     * @param x The x-coordinate of the point to return.
     * @param y The y-coordinate of the point to return.
     * @return the score of point represented by the given x and y coordinates.
     */
    public float getScore(int x, int y) {
        return scores[y][x];
    }

    /**
     * Getter for scores.
     *
     * @param p The point in question.
     * @return the score of the given point.
     */
    public float getScore(Point p) {
        return getScore(p.x, p.y);
    }

    /**
     * @return the list of boxes.
     */
    public List<Point> getBoxes() {
        return boxes;
    }

    /**
     * @return the list of goals.
     */
    public List<Point> getGoals() {
        return goals;
    }

    /**
     * @return the start point.
     */
    public Point getStart() {
        return start;
    }

    /**
     * @return the number of rows in the map.
     */
    public int getNumRows() {
        return matrix.length;
    }

    /**
     * @return the number of columns in the map.
     */
    public int getNumCols() {
        return matrix.length > 0 ? matrix[0].length : 0;
    }

    /**
     * Check if a point in the map is a wall.
     *
     * @param p The point to check.
     * @return true iff the given point is a wall.
     */
    public boolean isWall(Point p) {
        return isWall(p.x, p.y);
    }

    /**
     * Check if a point in the map is a wall.
     *
     * @param x The x coordinate of the point
     * @param y The y coordinate of the point.
     * @return true iff the given point is a wall.
     */
    public boolean isWall(int x, int y) {
        return matrix[y][x] == Square.WALL;
    }

    /**
     * Check if a given point in the map is a goal square.
     *
     * @param p The point to check.
     * @return true iff the given point is a goal.
     */
    public boolean isGoal(Point p) {
        return isGoal(p.x, p.y);
    }

    /**
     * Check if a given point in the map is a goal square.
     *
     * @param x The x coordinate of the point.
     * @param y The y coordinate of the point.
     * @return true iff the given point is a goal square.
     */
    public boolean isGoal(int x, int y) {
        return matrix[y][x] == Square.GOAL;
    }

    /**
     * Check if a given point in the map is a forbidden square.
     *
     * @param p The point to check.
     * @return true iff the given point is a forbidden square.
     */
    public boolean isForbidden(Point p) {
        return isForbidden(p.x, p.y);
    }

    /**
     * Check if a given point in the map is a forbidden square.
     *
     * @param x The x coordinate of the point.
     * @param y The y coordinate of the point.
     * @return true iff the given point is a forbidden square.
     */
    public boolean isForbidden(int x, int y) {
        return goalsReachable[y][x].size() == 0;
    }

    /**
     * Getter for reachable goals.
     *
     * @param p The point in question.
     * @return the reachable goals in the given position.
     */
    public Set<Point> getReachableGoals(Point p) {
        return getReachableGoals(p.x, p.y);
    }

    /**
     * Getter for reachable goals.
     *
     * @param x The x coordinate of the position.
     * @param y The y coordinate of the position.
     * @return the reachable goals in the given position.
     */
    public Set<Point> getReachableGoals(int x, int y) {
        return goalsReachable[y][x];
    }

    /**
     * Check if a given point in the map is free.
     *
     * @param p The point to check.
     * @return true iff the given point is neither a goal nor a wall.
     */
    public boolean isFree(Point p) {
        return isFree(p.x, p.y);
    }

    /**
     * Check if a given point in the map is free.
     *
     * @param x The x coordinate of the point.
     * @param y The y coordinate of the point.
     * @return true iff the given point is neither a goal nor a wall.
     */
    public boolean isFree(int x, int y) {
        return matrix[y][x] == Square.NONE;
    }

    /**
     * Finds the goals that are reachable in each point in the map. Forbidden
     * states are those where the number of reachable goals is zero.
     *
     * @return an array of sets where each set contains the goals that are
     * reachable in the given position.
     */
    private Set<Point>[][] findReachableGoals() {
        @SuppressWarnings("unchecked")
        Set<Point>[][] goalSets = new Set[getNumRows()][getNumCols()];
        Queue<Point> queue = new LinkedList<Point>();
        Set<Point> visited = new HashSet<Point>();

        for (int y = 0; y < getNumRows(); y++)
            for (int x = 0; x < getNumCols(); x++) {
                goalSets[y][x] = new HashSet<Point>();
                visited.clear();
                queue.add(getPoint(x, y));
                visited.add(getPoint(x, y));

                while (!queue.isEmpty()) {
                    Point p = queue.poll();
                    if (isGoal(p))
                        goalSets[y][x].add(p);

                    for (Direction d : Direction.getArray()) {
                        Direction opp = d.getRelative(2);
                        if (p.x + d.dx >= 0 && p.x + d.dx < getNumCols() &&
                                p.y + d.dy >= 0 && p.y + d.dy < getNumRows() &&
                                p.x + opp.dx >= 0 && p.x + opp.dx < getNumCols() &&
                                p.y + opp.dy >= 0 && p.y + opp.dy < getNumRows()) {
                            Point dst = getPoint(p.x + d.dx, p.y + d.dy);

                            if (!isWall(p.x + opp.dx, p.y + opp.dy) &&
                                    !isWall(dst) && !visited.contains(dst)) {
                                queue.add(dst);
                                visited.add(dst);
                            }
                        }
                    }
                }
            }

        return goalSets;
    }

    /**
     * Calculate the score of each point in the map. The score is 0 if the
     * point is not a goal. Otherwise, it's a function of how easy it is to
     * push a box into the position. A goal square with walls along each side
     * gives the highest score.
     *
     * @return a two-dimensional array where the indexes are the x and y
     * coordinate of the points and its value is the calculated score.
     */
    private float[][] findScores() {
        float[][] scores = new float[getNumRows()][getNumCols()];

        for (int y = 1; y < getNumRows()-1; y++)
            for (int x = 1; x < getNumCols()-1; x++) {
                if (isGoal(x, y)) {
                    scores[y][x] = 1;
                    for (Direction d : Direction.getArray()) {
                        if (isWall(x + d.dx, y + d.dy))
                            scores[y][x] += 2;
                        else if (isWall(x + 2*d.dx, y + 2*d.dy))
                            scores[y][x] += 1;
                    }
                }
            }

        return scores;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer(getNumRows()*getNumCols());

        for (int y = 0; y < getNumRows(); y++) {
            for (int x = 0; x < getNumCols(); x++) {
                Point p = getPoint(x, y);
                if (isWall(p))
                    buffer.append('#');
                else if (isGoal(p))
                    buffer.append('.');
                else if (isForbidden(p))
                    buffer.append('x');
                else
                    buffer.append(' ');
            }
            buffer.append('\n');
        }
        return buffer.toString();
    }

    /**
     * Parse a string containing a map to a Map object.
     *
     * The boardString could for example have the following appearance:
     *
     * ################
     * #@ #...#       #
     * #  #   # $ $ $ #
     * #              #
     * ################
     *
     * @param boardString A string representation of a map.
     */
    public static Map parse(String boardString) {
        int col = 0;
        int row = 0;
        int maxRow = 0;
        int maxCol = 0;

        for (byte current : boardString.getBytes()) {
            if (current == '\n') {
                if (col != 0)
                    row++;
                if (col > maxCol)
                    maxCol = col;
                col = 0;
            } else {
                col++;
            }
        }

        maxRow = row;

        Square[][] matrix = new Square[maxRow][maxCol];
        Point[][] points = new Point[maxRow][maxCol];
        List<Point> boxes = new LinkedList<Point>();

        for (int y = 0; y < maxRow; y++)
            for (int x = 0; x < maxCol; x++)
                points[y][x] = new Point(x, y);

        Point start = null;
        col = 0;
        row = 0;

        for (byte current : boardString.getBytes()) {
            switch(current) {
                case '*':
                    boxes.add(points[row][col]);
                case '.':
                    matrix[row][col] = Square.GOAL;
                    break;
                case '#':
                    matrix[row][col] = Square.WALL;
                    break;
                case '$':
                    boxes.add(points[row][col]);
                    matrix[row][col] = Square.NONE;
                    break;
                case '+':
                    matrix[row][col] = Square.GOAL;
                case '@':
                    start = points[row][col];
                    break;
                case ' ':
                    /* check if we're inside the map. */
                    if (col == 0 || row == 0 || matrix[row-1][col] == Square.INVALID)
                        matrix[row][col] = Square.INVALID;
                    else
                        matrix[row][col] = Square.NONE;
                    break;
                case '\n':
                    row++;
                    col = 0;
                    break;
            }

            if (current != '\n')
                col++;
        }

        for (int y = 0; y < maxRow; y++)
            for (int x = 0; x < maxCol; x++)
                if (matrix[y][x] == Square.INVALID)
                    matrix[y][x] = Square.WALL;

        for (int x = 0; x < maxCol; x++)
            matrix[0][x] = matrix[maxRow-1][x] = Square.WALL;
        for (int y = 0; y < maxRow; y++)
            matrix[y][0] = matrix[y][maxCol-1] = Square.WALL;

        return new Map(start, matrix, boxes, points);
    }
}
