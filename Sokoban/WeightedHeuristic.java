package Sokoban;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * The class can combine several heuristics, and prioritize them in different orders.
 */
public class WeightedHeuristic implements Comparator<State> {

	List<Comparator<State>> heuristics;
	
	/**
	 * Default constructor.
	 */
	public WeightedHeuristic() {
		heuristics = new LinkedList<Comparator<State>>();
	}
	
	/**
	 * Heuristics are added with the most important one being first.
	 **/
	public void add(Comparator<State> heuristic) {
		heuristics.add(heuristic);
	}
	
	/**
	 * The WeightedHeuristic is interpreted as any normal heuristic,
	 * by the rest of the program.
	 */
	public int compare(State a, State b) {
		for (Comparator<State> heuristic : heuristics) {
			if (heuristic.compare(a, b) == 0) {
				continue;
			} else {
				return heuristic.compare(a, b);
			}
		}
		return 0;
	}
}

