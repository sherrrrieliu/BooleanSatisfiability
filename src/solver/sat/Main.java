package solver.sat;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Usage example: read a given cnf instance file to create 
 * a simple sat instance object and print out its parameter fields. 
 */
public class Main
{  
  public static void main(String[] args) throws Exception
  {
		if(args.length == 0)
		{
			System.out.println("Usage: java Main <cnf file>");
			return;
		}
		
		String input = args[0];
		Path path = Paths.get(input);
		String filename = path.getFileName().toString();
    
    Timer watch = new Timer();
    watch.start();
    
		SATInstance instance = DimacsParser.parseCNFFile(input);
		System.out.println(instance);
    Solver solver = new Solver();
		List<Set<Integer>> org_clauses = instance.clauses;
		List<Clause> clauses = new LinkedList<>();
		for (Set<Integer> literals: org_clauses) {
			Clause clause = new Clause(literals);
			clauses.add(clause);
		}
		int res = solver.dpll(clauses);
		watch.stop();
		
    if (res == 1) {
			String result = solver.SATResult();
		System.out.println("{\"Instance\": \"" + filename + "\", \"Time\": " + String.format("%.2f",watch.getTime()) + ", \"Result\": \"SAT\"" + ", \"Instance\": \"" + result);
		} else {
			System.out.println("{\"Instance\": \"" + filename + "\", \"Time\": " + String.format("%.2f",watch.getTime()) + ", \"Result\": \"UNSAT\"}");
		}
  }
}
