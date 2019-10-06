package tables;

import java.util.HashMap;
import java.util.Map;


public class TableProbability<T1, T2, T3, Double> {
	
	private Map<T1, HashMap<T2, HashMap<T3, Double>>> table = new HashMap<T1, HashMap<T2, HashMap<T3, Double>>>();

	public void put(T1 s, T2 a, T3 s0, Double p) {
		HashMap<T3, Double> s0p = new HashMap<T3, Double>(); 
		s0p.put(s0, p);
		
		HashMap<T2, HashMap<T3, Double>> as0p = new HashMap<T2, HashMap<T3, Double>>();
		as0p.put(a, s0p);
		
		table.put(s, as0p);
	}
	
	public double getProbability(T1 s, T2 a, T3 s0) {
		if (table.get(s) == null) return 0.0;
		if (table.get(s).get(a) == null) return 0.0;
		if (table.get(s).get(a).get(s0) == null) return 0.0;	
		return (double) table.get(s).get(a).get(s0);
	}

	
}
