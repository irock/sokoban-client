package Sokoban;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;

/**
 * A representation of a map in the Sokoban game. The map is a puzzle and
 * should be solved by the player by moving boxes to the goals until all boxes
 * are standing on goal squares.
 */
public class Map {
    /**
     * A representation of a square in the map. Each position on the map is a
     * square and each square can be either a goal, a wall or neither. A
     * forbidden square is a square that boxes shouldn't be moved to if they're
     * not to be stuck.
     */
    private enum Square {
        NONE, GOAL, WALL, FORBIDDEN
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
     * Create a new Map.
     *
     * @param start The start position of the player.
     * @param matrix The map board.
     */
    private Map (Point start, Square[][] matrix, List<Point> boxes) {
        this.matrix = matrix;
        this.boxes = boxes;
        this.start = start;
        this.goals = new LinkedList<Point>();

        /* Find all goals in the map. */
        for (int y = 0; y < this.matrix.length; y++)
            for (int x = 0; x < this.matrix[0].length; x++)
                if (this.matrix[y][x] == Square.GOAL)
                    this.goals.add(new Point(x, y));

        for (Point p : findForbiddenSquares())
            matrix[p.y][p.x] = Square.FORBIDDEN;
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
        return matrix[y][x] == Square.FORBIDDEN;
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
     * Searches for forbidden squares in the map. Forbidden squares are those
     * that satisfy the following condition: If a box is placed in a forbidden
     * square, the puzzle can't be solved.
     *
     * @return the set of forbidden squares found in the map.
     */
    private Set<Point> findForbiddenSquares() {
        Set<Point> forbidden = new HashSet<Point>();

        Point box = new Point(0, 0);
        for (int y = 1; y < getNumRows()-1; y++)
            for (int x = 1; x < getNumCols()-1; x++) {
                box.set(x, y);

                if (isWall(box) || forbidden.contains(box))
                    continue;

                for (int i = 0; i < Direction.getArray().length/2; i++) {
                    boolean trapped = true;

                    for (int j = i; j < Direction.getArray().length;
                            j += Direction.getArray().length/2) {
                        Direction forward = Direction.getArray()[j];

                        Direction up = Direction.getArray()[(forward.ordinal() + 1) %
                            Direction.getArray().length];
                        Direction down = Direction.getArray()[(forward.ordinal() - 1 +
                                Direction.getArray().length) %
                            Direction.getArray().length];

                        /* Check if it's have a corner. */
                        if (isWall(box.x + up.dx, box.y + up.dy) &&
                                isWall(box.x + forward.dx, box.y +
                                    forward.dy) && !isGoal(box)) {
                            trapped = true;
                            break;
                        }

                        while (trapped && !isWall(box)) {
                            if ((!isWall(box.x + up.dx, box.y + up.dy) &&
                                        !isWall(box.x + down.dx, box.y +
                                            down.dy)) || isGoal(box))
                                trapped = false;

                            box.translate(forward.dx, forward.dy);
                        }

                        box.set(x, y);
                    }
                    if (trapped)
                        forbidden.add(new Point(box));
                }
            }
        return forbidden;
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

        Square[][] matrix = new Square[row][maxCol];
        List<Point> boxes = new LinkedList<Point>();

        Point start = null;
        col = 0;
        row = 0;

        for (byte current : boardString.getBytes()) {
            switch(current) {
                case '*':
                    boxes.add(new Point(col, row));
                case '.':
                    matrix[row][col] = Square.GOAL;
                    break;
                case '#':
                    matrix[row][col] = Square.WALL;
                    break;
                case '$':
                    boxes.add(new Point(col, row));
                    break;
                case '@':
                    start = new Point(col, row);
                    break;
                case '\n':
                    row++;
                    col = 0;
                    break;
            }

            if (current != '\n') {
                col++;
            }
        }
        return new Map(start, matrix, boxes);
    }
}
