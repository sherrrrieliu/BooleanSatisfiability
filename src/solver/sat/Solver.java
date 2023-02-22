package solver.sat;
import java.util.*;
public class Solver {
	private HashMap<Integer, Boolean> solution = new HashMap<>();

	// The SAT Result
	public String SATResult() {
			// Convert the set of literals to a list and sort the keys by ascending absolute value
    	List<Integer> literalsList = new ArrayList<>(solution.keySet());
			// Sort the keys by ascending absolute value
    	Collections.sort(literalsList, (a, b) -> Math.abs(a) - Math.abs(b));
			
			StringBuilder sb = new StringBuilder();
			for (Integer literal : literalsList) {
					sb.append(Math.abs(literal)).append(" ");
					if (solution.getOrDefault(literal, false) == true) {
							sb.append("true ");
					} else {
							sb.append("false ");
					}
			}
		return sb.toString().trim(); // Remove trailing whitespace and return the string
	}

	// 1. find unit clauses
	public int findUnitClause(List<Clause> clauses){
		int index = -1;
		for (Clause clause: clauses) {
			if (clause.literals.size() == 1) {
				index = clauses.indexOf(clause);
			}
		}
		return index;
	}
	public boolean unitPropagation(List<Clause> clauses) {
		int index = findUnitClause(clauses);
		if (index == -1) return false;
		Clause unitClause = clauses.get(index);
		clauses.remove(unitClause);
		Iterator<Integer> iterator = unitClause.literals.iterator();
		Integer literal = iterator.next();
		solution.put(Math.abs(literal), literal > 0 ? true : false);
		Iterator<Clause> iter = clauses.iterator();
		while (iter.hasNext()) {
    Clause clause = iter.next();
    if (clause.literals.contains(literal)) {
        iter.remove();
    } else if (clause.literals.contains(-literal)) {
        clause.literals.remove(-literal);
    }
}

		return true;
	}

	// 2. find pure literals
	// finds a pure literal by iterating through all clauses
int findPureLiteral(List<Clause> clauses){
  // create a lookup table to keep track of literal pureness
	HashMap<Integer, Integer> lookup = new HashMap<>();
	for (Clause clause: clauses) {
		Iterator<Integer> it = clause.literals.iterator();
		while (it.hasNext()) {
			int literal = it.next();
			int seen = lookup.getOrDefault(Math.abs(literal), 0);
			if (seen == 0) lookup.put(Math.abs(literal), Integer.signum(literal));
			else if (seen == -Integer.signum(literal)) lookup.put(Math.abs(literal), 2);
		}
	}

  // iterate over the lookup table to send the first pure literal found
	for (Map.Entry<Integer, Integer> entry : lookup.entrySet()) {
		if (entry.getValue() == -1 || entry.getValue() == 1) return entry.getValue() * entry.getKey();
	}
	// no pure literal found, return 0
	return 0;
}

// implements pure literal elimination algorithm
// returns 0 if it's unable to perform the algorithm in case there are no pure literals
boolean pureLiteralElimination(List<Clause> clauses){
  int literal = findPureLiteral(clauses);
  if (literal == 0) return false;
  // set the solution for that literal
	solution.put(Math.abs(literal), literal > 0 ? true : false);

	// remove clauses containing the pure literal
	Iterator<Clause> iter = clauses.iterator();
		while (iter.hasNext()) {
    Clause clause = iter.next();
    if (clause.literals.contains(literal)) {
        iter.remove();
    }
	}
  return true;
}

	// 3. No unit/pure case, then do branching
	List<Clause> branch(List<Clause> clauses, int literal) {
		solution.put(Math.abs(literal), (literal > 0) ? true : false);
		List<Clause> copyList = new ArrayList<>(clauses);
		Set<Integer> newClause = new HashSet<>();
    newClause.add(literal);
    copyList.add(0, new Clause(newClause));
    return copyList;
	}

	public int dpll(List<Clause> clauses) {
		// 1. check if we are already in a solved state
		int check = checkSolution(clauses);
		if (check != 0) {
			return check;
		}

		// 2. do unit-propagation as long as the clause set allows
		while (true) {
			check = checkSolution(clauses);
			if (check != 0) return check;
			if (!unitPropagation(clauses)) break;
		}

		// // 3. do pure-literal-elimination as long as the clause set allows
		while (true) {
			check = checkSolution(clauses);
			if (check != 0) return check;
			if (!pureLiteralElimination(clauses)) break;
		}

		// 4. if stuck, choose a random literal and branch on it
		if(dpll(branch(clauses, selectLiteral(clauses))) == 1) {
			return 1;
		}
		return dpll(branch(clauses, -1 * selectLiteral(clauses)));
	}

	int selectLiteral(List<Clause> clauses) {
    for (Clause clause : clauses) {
        for (int literal : clause.literals) {
            return literal;
        }
    }
    return 0; // No literal found
}

	private int checkSolution(List<Clause> clauses) {
		if (areAllClausesUnit(clauses)) return 1;
		// if (containsEmpty(clauses)) return -1;
		return 0;
	}

	public boolean areAllClausesUnit(List<Clause> clauses) {
		HashMap<Integer, Integer> isUnit = new HashMap<>();
    for (Clause clause : clauses) {
        for (Integer literal : clause.literals) {
            int seen = isUnit.getOrDefault(Math.abs(literal), 0);
						if (seen == 0) {
							isUnit.put(Math.abs(literal), Integer.signum(literal));
						} else if (seen == -Integer.signum(literal)) {
							return false;
						}
        }
    }
    for (Clause clause : clauses) {
        for (Integer literal : clause.literals) {
					if (literal > 0) {
						solution.put(Math.abs(literal), true);
					} else {
						solution.put(Math.abs(literal), false);
					}
        }
    }
    return true;
}

	private boolean containsEmpty(List<Clause> clauses) {
		for (Clause clause: clauses) {
			if (clause.literals.size() <= 0) {
				System.out.println(clauses.indexOf(clause));
				return true;
			}
		}
		return false;
	}

}
