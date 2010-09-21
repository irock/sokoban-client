package Sokoban;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;


public class TestSokoban extends TestCase {

	String file;
	int numberOfTestCases;
	BufferedReader tests;
	
	public static void Main(String[] args) throws Exception {
		new TestSokoban("sample", 11);
	}
	
	public TestSokoban(String file, int numberOfTestCases) throws Exception {
		this.file = file;
		this.numberOfTestCases = numberOfTestCases;
		DataInputStream in = new DataInputStream(new FileInputStream(file));
		tests = new BufferedReader(new InputStreamReader(in));
		for (int i = 0; i < numberOfTestCases; i++) {
			String testCase = getTestCase();
			List<Direction> solution = solveProblem(testCase);
			trySolution(solution);
		}
	}
	
	protected String getTestCase() throws IOException {
		String testCase = "";
		String temp = tests.readLine();
		while (!temp.contains(";")) {
			testCase = testCase + "\n" + tests.readLine();
			temp = tests.readLine();
		}
		return testCase;
	}
	
	protected List<Direction> solveProblem(String problem) {
		//TODO
		List<Direction> solution = new LinkedList<Direction>();
		return solution;
	}
	
	protected void trySolution(List<Direction> solution) {
		//TODO
	}
}
