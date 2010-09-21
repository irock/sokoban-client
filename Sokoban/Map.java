package Sokoban;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.ArrayList;
import java.awt.Point;

public class Map {	
	
    Point start;
	private int[][] matrix;
    List<Box> boxes;

	/*
	* Constructor that takes a matrix.
	*/
	private Map (Point start, int[][] matrix, List<Box> boxes) {
		this.matrix = matrix;
        this.boxes = new ArrayList<Box>();
        this.boxes.addAll(boxes);
        this.start = start;
	}

    /*
	* Return the matrix.
	*/
	public int[][] getMatrix() {
		return matrix;
	}

    public List<Box> getBoxes() {
        return boxes;
    }

    public Point getStart() {
        return start;
    }

	/*
	* Reads a map from the input stream and parses it to an int matrix.
	*/ 
	public static Map parse(InputStream stream) throws Exception {
		
		byte[] boardBytes = new byte[1024];
		String boardString = null;
		stream.read(boardBytes);
		boardString = new String(boardBytes);
		System.out.println(boardString);
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
		
		// for debugging purposes
		//System.out.println("mRow: " + row + "mCol:" + maxCol);

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

	public static void main(String[] args) throws Exception {
		//Socket socket = new Socket("cvap103.nada.kth.se",5555);
		//PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
		//out.println("1");

        //Map map = parse(socket.getInputStream());

        String mapString = "######\n" +
                           "#@$ .#\n" +
                           "###  #\n" +
                           "######\n";
        Map map = parse(mapString);

		for(int i = 0; i < map.matrix.length; i++) {
			for(int j = 0; j < map.matrix[0].length; j++) {
				System.out.print(map.matrix[i][j]);	
			}
			System.out.println();
		} 

        State state = new State(map.getStart(), map.getBoxes(), map);

        System.out.println("Positions:");
        for(Point p : state.getReachablePositions())
            System.out.printf("x: %d, y: %d\n", p.x, p.y);
        
        System.out.println("\nMoves:");
        for(java.util.Map.Entry<Direction, Box> move : state.getAvailableMoves())
            System.out.printf("direction: %s, x: %d, y: %d\n", move.getKey(), move.getValue().getPosition().x, move.getValue().getPosition().y);
	}
}
