package Sokoban;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

public class TrySokoban {

	String file;
	int numberOfTestCases;
	BufferedReader tests;

	public static void main(String[] args) throws Exception {
		new TrySokoban("/Users/agnes/sokoban-client/Sokoban/Sokoban/samples", 11);
	}

	public TrySokoban(String file, int numberOfTestCases) throws Exception {
		this.file = file;
		this.numberOfTestCases = numberOfTestCases;
		DataInputStream in = new DataInputStream(new FileInputStream(file));
		tests = new BufferedReader(new InputStreamReader(in));
		for (int i = 0; i < numberOfTestCases; i++) {
			String testCase = getTestCase();
			// List<Direction> solution = solveProblem(testCase);
			// trySolution(solution);
			System.out.println(testCase);
			int[][] parse = parseTestCase(testCase);
			printMatrix(parse);
		}
	}

	protected String getTestCase() throws IOException {
		String testCase = "";
		String temp = "";
		while (!(temp = tests.readLine()).contains(";")) {
			testCase += temp;
			testCase += "\n";
		}
		return testCase;
	}

	protected int[][] parseTestCase(String testCase) {
		State state = new State();
		return Map.parse(testCase, state);
	}

	protected void printMatrix(int[][] matrix) {

		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				System.out.print(matrix[i][j]);
			}
			System.out.println();
		}
		System.out.println();
	}

	protected List<Direction> solveProblem(String problem) {
		// TODO
		List<Direction> solution = new LinkedList<Direction>();
		return solution;
	}

	protected void trySolution(List<Direction> solution) {
		// TODO
	}
}
