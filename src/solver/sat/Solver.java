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
	//iterates through the clauses in the list
	public int findUnitClause(List<Clause> clauses){
		int index = -1;
		for (Clause clause: clauses) {
			if (clause.literals.size() == 1) {
				// returns the index of the first clause that has exactly one literal. 
				index = clauses.indexOf(clause);
			}
		}
		// If no such clause is found, it returns -1.
		return index;
	}


	public boolean unitPropagation(List<Clause> clauses) {
		// check if there is a unit clause in the list. 
		int index = findUnitClause(clauses);
		// return false to indicate that no further progress can be made with unit propagation.
		if (index == -1) return false;
		// find a unit clause 
		Clause unitClause = clauses.get(index);
		// removes the unit clause from the list of clauses 
		clauses.remove(unitClause);
		Iterator<Integer> iterator = unitClause.literals.iterator();
		Integer literal = iterator.next();
		// adds the literal from the unit clause to the solution map.
		solution.put(Math.abs(literal), literal > 0 ? true : false);
		Iterator<Clause> iter = clauses.iterator();
		while (iter.hasNext()) {
			Clause clause = iter.next();
			// removes any clause that contains the same literal as the unit clause (because it is already satisfied)
			if (clause.literals.contains(literal)) {
					iter.remove();
			// removes the negation of the literal from any clauses that contain it.
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
	// pure literal not found
  if (literal == 0) return false;
  // adds the literal to the solution
	solution.put(Math.abs(literal), literal > 0 ? true : false);

	// remove clauses containing the pure literal
	Iterator<Clause> iter = clauses.iterator();
		while (iter.hasNext()) {
    Clause clause = iter.next();
		// a pure literal is found, removes all clauses containing the literal
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

		int literal = selectLiteral(clauses);
		if (literal == 0) {
			return -1;
		}
		// 4. if stuck, choose a random literal and branch on it
		if(dpll(branch(clauses, literal)) == 1) {
			return 1;
		}
		return dpll(branch(clauses, -1 * literal));
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
		if (hasNoConflict(clauses)) return 1; // SAT || 
		if (containsEmpty(clauses)) return -1; // UNSAT
		return 0; // continue searching for a solution
	}

	// checks if all literals in all clauses have no opposite literals anymore
	public boolean hasNoConflict(List<Clause> clauses) {
		if (clauses.isEmpty()) {
			return true;
		}
		HashMap<Integer, Integer> noConflict = new HashMap<>();
    for (Clause clause : clauses) {
        for (Integer literal : clause.literals) {
            int seen = noConflict.getOrDefault(Math.abs(literal), 0);
						if (seen == 0) {
							// sets their solutions accordingly
							noConflict.put(Math.abs(literal), Integer.signum(literal));
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
// if a clause contains no literal and is not removed, it means UNSAT.
	private boolean containsEmpty(List<Clause> clauses) {
		for (Clause clause: clauses) {
			if (clause.literals.isEmpty()) {
				return true;
			}
		}
		return false;
	}

}
