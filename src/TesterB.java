import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import helper.InputReaderPartB;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class TesterB {
	static ArrayList<Integer[]> demands;
	static ArrayList<Integer> taxiLocations;

	final static String dataFolderPath = "D:\\Dropbox\\SMU\\Year3Sem2\\Enterprise Analytics for Decision Support\\project\\supplementary\\supplementary\\training\\";
	// {taxis, demands}
	final static int[] NUM = new int[] { 5, 6 };
	// final static int[] NUM = new int[] { 10, 15 };
	// final static int[] NUM = new int[] { 20, 25 };
	// final static int[] NUM = new int[] { 50, 60 };
	// final static int[] NUM = new int[] { 100, 120 };

	static PrintWriter w;
	static PrintWriter w1;

	static int GREEDY_CHOICE = 1; // ********* select greedy method

	static HashMap<Integer, Dijkstra> dijkstraMap = new HashMap<Integer, Dijkstra>();

	public static void main(String[] args) {

		if (args.length != 0) {
			NUM[0] = Integer.parseInt(args[0]);
			NUM[1] = Integer.parseInt(args[1]);
			GREEDY_CHOICE = Integer.parseInt(args[2]);
		}

		Date startTime = new Date();

		try {
			w = new PrintWriter(
					new BufferedWriter(
							new FileWriter(
									"part b/greedy" + GREEDY_CHOICE + "/summary-b-greedy"
											+ GREEDY_CHOICE + "-" + NUM[0] + "_" + NUM[1] + ".txt",
									false)));
			w1 = new PrintWriter(
					new BufferedWriter(
							new FileWriter(
									"part b/greedy" + GREEDY_CHOICE + "/results-b-greedy"
											+ GREEDY_CHOICE + "-" + NUM[0] + "_" + NUM[1] + ".csv",
									false)));
			System.out.println("running TestB. Size: " + NUM[0] + ", " + NUM[1]);
		} catch (IOException e) {
			e.printStackTrace();
		}

		InputReaderPartB partB = new InputReaderPartB(new Integer[] { NUM[0], NUM[1] });
		demands = partB.getDemands();
		taxiLocations = partB.getTaxiLocations();

		createDijkstraMap();

		// ArrayList<int[]> assignment = runGreedy();
		ArrayList<int[]> assignment = runModel();

		printFinalCSV(assignment);

		Date endTime = new Date();
		System.out.println("Run complete");

		print("Start Time: " + startTime);
		print("End Time: " + endTime);
		print("Duration (sec): " + ((endTime.getTime() - startTime.getTime()) / 1000.0));

		w1.flush();
		w1.close();
		w.flush();
		w.close();

	}

	private static ArrayList<int[]> runModel() {

		ArrayList<int[]> assignment = new ArrayList<int[]>();

		try {
			// Define an empty model
			IloCplex model = new IloCplex();

			// Define the binary/decision variables
			// Xijk - if taxi k serves customer i after customer j
			IloNumVar[][][] x = new IloNumVar[demands.size()][demands.size()][taxiLocations.size()];
			for (int i = 0; i < demands.size(); i++) {
				for (int j = 0; j < demands.size(); j++) {
					for (int k = 0; k < taxiLocations.size(); k++) {
						x[i][j][k] = model.boolVar();
					}
				}
			}
			// Yik - if customer i is served by taxi k
			IloNumVar[][] y = new IloNumVar[demands.size()][taxiLocations.size()];
			for (int i = 0; i < demands.size(); i++) {
				for (int k = 0; k < taxiLocations.size(); k++) {
					y[i][k] = model.boolVar();
				}
			}

			double totalDistForDemand = 0.0; // to add the distances for demand

			// Define the objective function
			IloLinearNumExpr obj = model.linearNumExpr();
			// Setting Yik
			for (int i = 0; i < demands.size(); i++) {
				int startLoc = demands.get(i)[0];
				int requestTime = demands.get(i)[2];
				Dijkstra dijkstra = dijkstraMap.get(startLoc);

				for (int k = 0; k < taxiLocations.size(); k++) {
					int taxiLoc = taxiLocations.get(k);
					double travelTime = dijkstra.getShortestDistanceTo(Integer.toString(taxiLoc));

					double waitTime = travelTime - requestTime;
					if (waitTime < 0) {
						waitTime = 0;
					}

					obj.addTerm(waitTime, y[i][k]);
				}
			}
			// Setting Xijk
			// for (int i = 0; i < demands.size(); i++) {
			// int descLoc = demands.get(i)[1];
			// Dijkstra dijkstra = dijkstraMap.get(Integer.toString(descLoc));
			//
			// for (int j = 0; j < demands.size(); j++) {
			// int startLoc = demands.get(j)[0];
			// double dist = dijkstra.getShortestDistanceTo(Integer.toString(startLoc));
			//
			// for (int k = 0; k < taxiLocations.size(); k++) {
			// obj.addTerm(dist, x[i][j][k]);
			// }
			// }
			// }
			model.addMinimize(obj);

			// Add the constraints
			// For every demand, only 1 taxi serves
			for (int i = 0; i < demands.size(); i++) {
				IloLinearNumExpr rowSum = model.linearNumExpr();
				for (int k = 0; k < taxiLocations.size(); k++) {
					rowSum.addTerm(1, y[i][k]);
				}
				model.addEq(rowSum, 1);
			}

			int[][] assign = new int[demands.size()][taxiLocations.size()];

			// Solve the model
			boolean isSolved = model.solve();
			if (isSolved) {
				double objValue = model.getObjValue();
				print("Obj value: " + objValue);

				for (int i = 0; i < demands.size(); i++) {
					for (int k = 0; k < taxiLocations.size(); k++) {
						assign[i][k] = (int) model.getValue(y[i][k]);
						System.out.print(assign[i][k] + " ");
					}
					System.out.println();
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
		return assignment;
	}

	private static ArrayList<int[]> runGreedy() {

		double totalWait = 0.0;

		// Set customer to taxi [taxi id, taxi loc, request id, request loc]
		ArrayList<int[]> assignment = new ArrayList<int[]>();

		// Set taxi available list [ taxi id, taxi location, time available]
		List<Double[]> taxiAvail = new ArrayList<Double[]>();
		for (int i = 0; i < taxiLocations.size(); i++) {
			taxiAvail.add(new Double[] { i + 1.0, (double) taxiLocations.get(i), (double) i });
		}

		// Set customer request time [request id, request time, start loc, destination loc]
		List<Integer[]> requests = new ArrayList<Integer[]>();
		for (int i = 0; i < demands.size(); i++) {
			Integer[] j = demands.get(i);
			requests.add(new Integer[] { i + 1, j[2], j[0], j[1] });
		}
		// sort by request time
		Collections.sort(requests, new Comparator<Integer[]>() {
			@Override
			public int compare(Integer[] o1, Integer[] o2) {
				return o1[1].compareTo(o2[1]);
			}
		});

		// ****** MODEL START ******
		while (!requests.isEmpty()) {
			Integer[] r = requests.remove(0);
			int requestId = r[0];
			int requestTime = r[1];
			int startLoc = r[2];
			int descLoc = r[3];
			// print("request-id:" + requestId + ", t:" + requestTime + ", ogn:" + startLoc + ",
			// des:"
			// + descLoc);

			Dijkstra dijkstra = dijkstraMap.get(startLoc);

			double shortestWait = -1;
			int nearestTaxiLoc = -1;
			int nearestTaxiId = -1;
			boolean traversed = false;
			double chosenJourneyTime = -1; // from taxiLoc to startLoc

			for (Double[] i : taxiAvail) {
				double taxiAvailTime = i[2];
				int taxiId = i[0].intValue();
				int taxiLoc = i[1].intValue();

				// Choosing logic
				double travelTime = dijkstra.getShortestDistanceTo(Integer.toString(taxiLoc));
				double setOffTime = requestTime - travelTime;
				double waitTime = taxiAvailTime - setOffTime; // negative value means on time

				// print("waitTime:" + waitTime);

				// ***** greedy logic *****
				boolean chosen = false;
				if (GREEDY_CHOICE == 2) {
					System.out.println("greedy logic 2");
					// print("con1:" + (traversed == false));
					// print("con2:" + (shortestWait > 0 && waitTime <= 0));
					// print("con3:" + (shortestWait < 0 && waitTime < 0 && waitTime >
					// shortestWait));
					// print("con4:" + (shortestWait > 0 && waitTime > 0 && waitTime <
					// shortestWait));
					chosen = (traversed == false || (shortestWait > 0 && waitTime <= 0)
							|| (shortestWait < 0 && waitTime < 0 && waitTime > shortestWait)
							|| (shortestWait > 0 && waitTime > 0 && waitTime < shortestWait));
					traversed = true;

				} else if (GREEDY_CHOICE == 1) {
					System.out.println("greedy logic 1");
					chosen = (traversed == false || waitTime < shortestWait);
					traversed = true;

				}
				// ***** greedy logic *****

				if (chosen) {
					shortestWait = waitTime;
					nearestTaxiLoc = taxiLoc;
					nearestTaxiId = taxiId;
					chosenJourneyTime = travelTime; // from taxiLoc to startLoc
				}
			}
			print("chosen-taxiId:" + nearestTaxiId + ", reqId:" + requestId + ", taxiLoc:"
					+ nearestTaxiLoc + ", startLoc:" + startLoc + ", descLoc:" + descLoc + ", wait:"
					+ shortestWait);

			// add to assignment
			assignment.add(new int[] { nearestTaxiId, nearestTaxiLoc, requestId, requestTime,
					startLoc, descLoc });

			// Update taxi avail time
			Double[] taxi = taxiAvail.remove(nearestTaxiId - 1);
			if (shortestWait < 0) {
				shortestWait = 0;
			}
			totalWait += shortestWait;
			taxi[1] = (double) descLoc; // update taxi location
			taxi[2] = requestTime + shortestWait + chosenJourneyTime; // update available time
			taxiAvail.add(taxi);

			Collections.sort(taxiAvail, new Comparator<Double[]>() {
				@Override
				public int compare(Double[] o1, Double[] o2) {
					return o1[0].compareTo(o2[0]);
				}
			});

			// taxi id, taxi location, time available
			// for (Double[] i : taxiAvail) {
			// print("taxiList-id:" + i[0].intValue() + ", loc:" + i[1].intValue() + ", time:"
			// + i[2].intValue());
			// }
			// print("");
		}

		print("\nANS");
		for (int[] i : assignment) {
			print(i[0] + ", " + i[1] + ", " + i[2] + ", " + i[3] + ", " + i[4] + ", " + i[5]);
		}

		print("");
		print("Total Wait: " + totalWait);
		return assignment;

	}

	private static void printFinalCSV(ArrayList<int[]> assignment) {
		// print to w1
		// [taxi id, taxi loc, request id, request loc, desc loc]
		for (int[] i : assignment) {
			int taxiId = i[0];
			int taxiLoc = i[1];
			// int requestId = i[2];
			int requestTime = i[3];
			int startLoc = i[4];
			int descLoc = i[5];

			Dijkstra dijkstra = dijkstraMap.get(startLoc);

			List<Edge> p1 = dijkstra.getShortestPathTo(Integer.toString(taxiLoc));
			List<Edge> p2 = dijkstra.getShortestPathTo(Integer.toString(descLoc));

			// taxi travel to startLoc
			for (int a = p1.size() - 1; a >= 0; a--) {
				if (a == p1.size() - 1) {
					w1.println(taxiId + ",Taxi,NA," + p1.get(a).id);
				} else {
					w1.println(taxiId + ",Trans,NA," + p1.get(a).id);
				}
			}

			// start travelling journey
			for (int a = 0; a < p2.size(); a++) {
				if (a == 0) {
					w1.println(taxiId + ",Start," + requestTime + "," + p2.get(a).id);
				} else if (a == p2.size() - 1) {
					w1.println(taxiId + ",End,NA," + p2.get(a).id);
				} else {
					w1.println(taxiId + ",Trans,NA," + p2.get(a).id);
				}
			}
		}
	}

	private static Dijkstra getDijkstra(String source) {
		Dijkstra d = new Dijkstra();
		d.computePaths(source);
		return d;
	}

	private static void createDijkstraMap() {
		for (int i = 0; i < demands.size(); i++) {
			int startLoc = demands.get(i)[0];
			System.out.println((i + 1) + ". Get dijkstra for " + startLoc);
			Dijkstra d = getDijkstra(Integer.toString(startLoc));
			dijkstraMap.put(startLoc, d);
		}
	}

	private static void print(String s) {
		System.out.println(s);
		w.println(s);
	}

}
