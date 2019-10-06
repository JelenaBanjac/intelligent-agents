package tables;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class Table<T1, T2, Double> {
	
	// row, col, value
	private Map<T1, HashMap<T2, Double>> table = new HashMap<T1, HashMap<T2, Double>>();

	public void put(T1 row, T2 col, Double val) {
		HashMap<T2, Double> colVal = new HashMap<T2, Double>(); 
		colVal.put(col, val);
		table.put(row, colVal);
	}
	
	public double getValue(T1 row, T2 col) {
		if (table.get(row) == null) return 0.0;
		if (table.get(row).get(col) == null) return 0.0;
		return (double) table.get(row).get(col);
	}

	public double getValue(T1 row) {
		if (table.get(row).values().iterator().next() == null) return 0.0;
		return (double) table.get(row).values().iterator().next();
	}
	
	public HashMap<T2, Double> getValues(T1 row) {
		return table.get(row);
	}
	
	public Set<T1> getKeys() {
		return table.keySet();
	}
}
