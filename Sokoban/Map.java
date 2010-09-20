package Sokoban;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Map {	
	
	private int[][] mapMatrix;

	/*
	* Default constructor.
	*/
	public Map () {
	}
	
	/*
	* Constructor that takes a matrix.
	*/
	public Map (int[][] matrix) {
		mapMatrix = matrix;
	}

	/**
	* Check if given state has reached the goal on this map.
	**/
	public boolean goalReached(State state) {
		for (Box box : state.getBoxes) {
			if (mapMatrix[box.getPosition().getY()][box.getPosition().getX()] != 2)
				return false;
		}
		return true;
	}

	/*
	* Return the matrix.
	*/
	public int[][] getMatrix() {
		return mapMatrix;
	}

	/*
	* Sets the matrix.
	*/
	public void setMatrix(int[][] matrix) {
		mapMatrix = matrix;
	}

	/*
	* Reads a map from the input stream and parses it to an int matrix.
	*/ 
	public static int[][] parse(InputStream stream, State state) throws Exception {
		
		byte[] boardBytes = new byte[1024];
		String boardString = null;
		stream.read(boardBytes);
		boardString = new String(boardBytes);
		System.out.println(boardString);
        return parse(boardString, state);
    }

    public static int[][] parse(String boardString, State state) {
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
		
		// for debugging purposes
		//System.out.println("mRow: " + row + "mCol:" + maxCol);

		col = 0;
		row = 0;
		for (byte current : boardString.getBytes()) {
            switch(current) {
                case '*':
                    state.addBox(new Box(col, row));
                case '.':
                    matrix[row][col] = 2;
                    break;
                case '#':
                    matrix[row][col] = 1;
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
		return matrix;
	}

	public static void main(String[] args) throws Exception {
		Socket socket = new Socket("cvap103.nada.kth.se",5555);
		PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
		out.println("5");
        State state = new State();
        int matrix[][] = parse(socket.getInputStream(), state);

		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				System.out.print(matrix[i][j]);	
			}
			System.out.println();
		} 
	}
}
