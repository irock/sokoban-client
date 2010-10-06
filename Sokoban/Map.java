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
     * @return true iff the given point is a wall.
     */
    public boolean isWall(Point p) {
        return matrix[p.y][p.x] == Square.WALL;
    }

    /**
     * @return true iff the given point is a goal.
     */
    public boolean isGoal(Point p) {
        return matrix[p.y][p.x] == Square.GOAL;
    }

    /**
     * @return true iff the given point is a forbidden square.
     */
    public boolean isForbidden(Point p) {
        return matrix[p.y][p.x] == Square.FORBIDDEN;
    }

    /**
     * @return true iff the given point is neither a goal nor a wall.
     */
    public boolean isFree(Point p) {
        return matrix[p.y][p.x] == Square.NONE;
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

        for (int y = 0; y < getNumRows(); y++)
            for (int x = 0; x < getNumCols(); x++) {
                Point box = new Point(x, y);

                if (isWall(box) || forbidden.contains(box))
                    continue;

                for (int i = 0; i < Direction.values().length/2; i++) {
                    boolean trapped = true;

                    for (int j = i; j < Direction.values().length;
                            j += Direction.values().length/2) {
                        Direction forward = Direction.values()[j];

                        Direction up = Direction.values()[(forward.ordinal() + 1) %
                            Direction.values().length];
                        Direction down = Direction.values()[(forward.ordinal() - 1 +
                                Direction.values().length) %
                            Direction.values().length];

                        Point current = new Point(box);
                        Point front = new Point(box.x + forward.dx,
                                box.y + forward.dy);
                        Point above = new Point(box.x + up.dx, box.y + up.dy);
                        Point below = new Point(box.x + down.dx, box.y + down.dy);

                        /* Check if it's have a corner. */
                        if (isWall(above) && isWall(front) && !isGoal(current)) {
                            trapped = true;
                            break;
                        }

                        while (trapped && !isWall(current)) {
                            if ((!isWall(above) && !isWall(below)) || isGoal(current))
                                trapped = false;

                            current.translate(forward.dx, forward.dy);
                            front.translate(forward.dx, forward.dy);
                            above.translate(forward.dx, forward.dy);
                            below.translate(forward.dx, forward.dy);
                        }
                    }
                    if (trapped)
                        forbidden.add(box);
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

        Point start = new Point(0, 0);
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
