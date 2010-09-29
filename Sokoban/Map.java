package Sokoban;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.ArrayList;
import java.awt.Point;

public class Map {
    private Point start;
    private List<Box> boxes;
    private List<Point> goals;
    private int[][] matrix;

    private Map (Point start, int[][] matrix, List<Box> boxes) {
        this.matrix = matrix;
        this.boxes = boxes;
        this.start = start;
        this.goals = new ArrayList<Point>();

        for (int y = 0; y < this.matrix.length; y++)
            for (int x = 0; x < this.matrix[0].length; x++)
                if (this.matrix[y][x] == 2)
                    this.goals.add(new Point(x, y));
    }

    public List<Box> getBoxes() {
        return boxes;
    }

    public List<Point> getGoals() {
        return goals;
    }

    public Point getStart() {
        return start;
    }

    public int getNumRows() {
        return matrix.length;
    }

    public int getNumCols() {
        return matrix.length > 0 ? matrix[0].length : 0;
    }

    public boolean isWall(Point p) {
        return matrix[p.y][p.x] == 1;
    }

    public boolean isGoal(Point p) {
        return matrix[p.y][p.x] == 2;
    }

    public boolean isFree(Point p) {
        return matrix[p.y][p.x] == 0;
    }

    /*
    * Reads a map from the input stream and parses it to an int matrix.
    */
    public static Map parse(InputStream stream) throws Exception {

        byte[] boardBytes = new byte[1024];
        String boardString = null;
        stream.read(boardBytes);
        boardString = new String(boardBytes);
        return parse(boardString);
    }

    public static Map parse(String boardString) {
        int col = 0;
        int row = 0;

        int maxCol = 0;
        int maxRow = 0;

        for (byte current : boardString.getBytes()) {

            if (current == '\n') {
                row++;
                if (col > maxCol)
                    maxCol = col;
                col = 0;
            } else {
                col++;
            }
        }

        int[][] matrix = new int[row][maxCol];
        List<Box> boxes = new ArrayList<Box>();

        Point start = new Point(0, 0);
        col = 0;
        row = 0;
        for (byte current : boardString.getBytes()) {
            switch(current) {
                case '*':
                    boxes.add(new Box(col, row));
                case '.':
                    matrix[row][col] = 2;
                    break;
                case '#':
                    matrix[row][col] = 1;
                    break;
                case '$':
                    boxes.add(new Box(col, row));
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
