import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import helper.InputReaderPartA;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class Tester {
	static ArrayList<Integer[]> demands;
	static ArrayList<Integer> taxiLocations;

	final static String dataFolderPath = "D:\\Dropbox\\SMU\\Year3Sem2\\Enterprise Analytics for Decision Support\\project\\supplementary\\supplementary\\training\\";
	static int NUM = 5;
	// static int NUM = 10;
	// static int NUM = 20;
	// static int NUM = 50;
	// static int NUM = 100;
	static String INPUT_FILE = "sin_train_" + NUM + "_" + NUM + ".txt";

	static PrintWriter summaryWriter, resultsWriter;

	static HashMap<Integer, Dijkstra> dijkstraMap = new HashMap<Integer, Dijkstra>();

	public static void main(String[] args) {
		if (args.length != 0) {
			NUM = Integer.parseInt(args[0]);
			INPUT_FILE = "sin_train_" + NUM + "_" + NUM + ".txt";
		}

		Date startTime = new Date();

		try {
			summaryWriter = new PrintWriter(new BufferedWriter(
					new FileWriter("part a/summary-a-" + NUM + "_" + NUM + ".txt", false)));
			resultsWriter = new PrintWriter(new BufferedWriter(
					new FileWriter("part a/results-a-" + NUM + "_" + NUM + ".csv", false)));
			System.out.println("running Test A. Size: " + NUM);
		} catch (IOException e) {
			e.printStackTrace();
		}

		InputReaderPartA partA = new InputReaderPartA(dataFolderPath + INPUT_FILE, NUM);
		demands = partA.getDemands();
		taxiLocations = partA.getTaxiLocations();

		createDijkstraMap();

		int[][] result = runModel();

		printFinalCSV(result);

		Date endTime = new Date();
		System.out.println("run complete");

		print("Start Time: " + startTime);
		print("End Time: " + endTime);
		print("Duration (sec): " + ((endTime.getTime() - startTime.getTime()) / 1000.0));

		resultsWriter.flush();
		resultsWriter.close();
		summaryWriter.flush();
		summaryWriter.close();

	}

	private static void printFinalCSV(int[][] result) {
		System.out.println("Printing to final CSV");

		// printing for final
		for (int i = 0; i < demands.size(); i++) {
			for (int j = 0; j < taxiLocations.size(); j++) {
				summaryWriter.print(result[i][j] + " ");
				System.out.print(result[i][j] + " ");

				// printing to final
				if (result[i][j] == 1) {
					int t = taxiLocations.get(j);
					int s = demands.get(i)[0];
					int e = demands.get(i)[1];
					Dijkstra dijkstra = dijkstraMap.get(s);

					List<Edge> p = dijkstra.getShortestPathTo(t);
					for (int a = p.size() - 1; a >= 0; a--) {
						if (a == p.size() - 1) {
							resultsWriter.println("Taxi," + p.get(a).id);
						} else {
							resultsWriter.println("Trans," + p.get(a).id);
						}
					}

					List<Edge> p2 = dijkstra.getShortestPathTo(e);
					for (int a = 0; a < p2.size(); a++) {
						if (a == 0) {
							resultsWriter.println("Start," + p2.get(a).id);
						} else if (a == p2.size() - 1) {
							resultsWriter.println("End," + p2.get(a).id);
						} else {
							resultsWriter.println("Trans," + p2.get(a).id);
						}
					}

				}
			}
			System.out.println();
			summaryWriter.println();
		}
		System.out.println("Printing to final CSV done");
	}

	private static int[][] runModel() {
		System.out.println("Run model");

		int[][] assignment = new int[demands.size()][taxiLocations.size()];

		try {
			// Define an empty model
			IloCplex model = new IloCplex();

			// Define the binary/decision variables
			IloNumVar[][] x = new IloNumVar[demands.size()][taxiLocations.size()];
			for (int i = 0; i < demands.size(); i++) {
				for (int j = 0; j < taxiLocations.size(); j++) {
					x[i][j] = model.boolVar();
				}
			}

			double totalDistForDemand = 0.0; // to add the distances for demand

			// Define the objective function
			IloLinearNumExpr obj = model.linearNumExpr();
			for (int i = 0; i < demands.size(); i++) {
				int startLoc = demands.get(i)[0];
				int descLoc = demands.get(i)[1];

				Dijkstra dijkstra = dijkstraMap.get(startLoc);

				// add to demands distance
				totalDistForDemand += dijkstra.getShortestDistanceTo(descLoc);

				for (int j = 0; j < taxiLocations.size(); j++) {
					int b = taxiLocations.get(j);

					System.out.println("demand: " + startLoc + ", taxi: " + b);
					System.out.println("x[" + i + "][" + j + "] = " + x[i][j]);
					obj.addTerm(dijkstra.getShortestDistanceTo(b), x[i][j]);

				}
			}
			model.addMinimize(obj);

			// Add the constraints
			// Each row sums up to 1
			for (int i = 0; i < demands.size(); i++) {
				IloLinearNumExpr rowSum = model.linearNumExpr();
				for (int j = 0; j < taxiLocations.size(); j++) {
					rowSum.addTerm(1, x[i][j]);
				}
				model.addEq(rowSum, 1);
			}
			// Each column sums up to 1
			for (int i = 0; i < demands.size(); i++) {
				IloLinearNumExpr colSum = model.linearNumExpr();
				for (int j = 0; j < taxiLocations.size(); j++) {
					colSum.addTerm(1, x[j][i]);
				}
				model.addEq(colSum, 1);
			}

			// Solve the model
			boolean isSolved = model.solve();
			if (isSolved) {
				double objValue = model.getObjValue();
				print("Obj value: " + objValue);

				for (int i = 0; i < demands.size(); i++) {
					for (int j = 0; j < taxiLocations.size(); j++) {
						assignment[i][j] = (int) model.getValue(x[i][j]);
					}
				}

				print("Total time for all demands: " + totalDistForDemand);
				print("Grand total duration (Obj value + demands): "
						+ (totalDistForDemand + objValue));

			} else {
				print("Model not solved :(");
			}
			print("");

		} catch (IloException e) {
			e.printStackTrace();
		}

		System.out.println("Model completed!");
		return assignment;
	}

	private static Dijkstra getDijkstra(int source) {
		Dijkstra d = new Dijkstra();
		d.computePaths(source);
		return d;
	}

	private static void createDijkstraMap() {
		for (int i = 0; i < demands.size(); i++) {
			int startLoc = demands.get(i)[0];
			System.out.println((i + 1) + ". Get dijkstra for " + startLoc);
			Dijkstra d = getDijkstra(startLoc);
			dijkstraMap.put(startLoc, d);
		}
	}

	private static void print(String s) {
		System.out.println(s);
		summaryWriter.println(s);
	}

}
