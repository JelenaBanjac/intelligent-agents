package variables;

public class SolutionCost {
	
	public double marginalCost;
	public Solution solution;
	
	public SolutionCost(double marginalCost, Solution solution) {
		this.marginalCost = marginalCost;
		this.solution = solution;
	}

	public double getMarginalCost() {
		return marginalCost;
	}

	public void setMarginalCost(double marginalCost) {
		this.marginalCost = marginalCost;
	}

	public Solution getSolution() {
		return solution;
	}

	public void setSolution(Solution solution) {
		this.solution = solution;
	}
	
	
}
