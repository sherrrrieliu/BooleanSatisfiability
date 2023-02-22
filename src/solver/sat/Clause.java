package solver.sat;
import java.util.*;
public class Clause {
	// all literals in this clause
	public Set<Integer> literals = new HashSet<>();
	public boolean clauseSatisfied = false;
	public Clause(Set<Integer> clause) {
			this.literals = clause;
			clauseSatisfied = false;

	}

	boolean isUnitClause() {
			return !clauseSatisfied && literals.size() == 1;
	}
}