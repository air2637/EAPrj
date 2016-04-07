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
import java.util.Map;

import helper.InputReaderPartB;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class TesterB {
	static ArrayList<Integer[]> demands;
	static ArrayList<Integer> taxiLocations;
	static PrintWriter summaryWriter, resultsWriter, overallWriter;
	final static String dataFolderPath = "D:\\Dropbox\\SMU\\Year3Sem2\\Enterprise Analytics for Decision Support\\project\\supplementary\\supplementary\\training\\";
	// {taxis, demands}
	final static int[] NUM = new int[] { 5, 6 };
	// final static int[] NUM = new int[] { 10, 15 };
	// final static int[] NUM = new int[] { 20, 25 };
	// final static int[] NUM = new int[] { 50, 60 };
	// final static int[] NUM = new int[] { 100, 120 };

	static int GREEDY_CHOICE = 1; // ********* select greedy method
	static double THRESHOLD = 0;
	static double totalWait;

	static HashMap<Integer, Dijkstra> dijkstraMap = new HashMap<Integer, Dijkstra>();

	public static void main(String[] args) {

		if (args.length != 0) {
			NUM[0] = Integer.parseInt(args[0]);
			NUM[1] = Integer.parseInt(args[1]);
			GREEDY_CHOICE = Integer.parseInt(args[2]);
		}

		Date startTime = new Date();
		totalWait = 0.0;

		try {
			summaryWriter = new PrintWriter(
					new BufferedWriter(
							new FileWriter(
									"part b/greedy" + GREEDY_CHOICE + "/summary-b-greedy"
											+ GREEDY_CHOICE + "-" + NUM[0] + "_" + NUM[1] + ".txt",
									false)));

			resultsWriter = new PrintWriter(
					new BufferedWriter(
							new FileWriter(
									"part b/greedy" + GREEDY_CHOICE + "/results-b-greedy"
											+ GREEDY_CHOICE + "-" + NUM[0] + "_" + NUM[1] + ".csv",
									false)));

			if (args.length != 0) {
				overallWriter = new PrintWriter(
						new BufferedWriter(new FileWriter("part b/overall_summary.txt", true)));
			}

			System.out.println("running TestB. Size: " + NUM[0] + ", " + NUM[1]);
		} catch (IOException e) {
			e.printStackTrace();
		}

		InputReaderPartB partB = new InputReaderPartB(new Integer[] { NUM[0], NUM[1] });
		demands = partB.getDemands();
		taxiLocations = partB.getTaxiLocations();

		createDijkstraMap();

		ArrayList<int[]> assignment = runGreedy(demands, taxiLocations, null);
		// ArrayList<int[]> assignment = runModel();

		// nearestTaxiId, nearestTaxiLoc, requestId, requestTime, startLoc, descLoc
		// for (int i = 0; i < assignment.size(); i++) {
		// int[] a = assignment.get(i);
		// System.out.println("requestTime:" + a[3] + ", taxi:" + a[0] + ", req:" + a[2]);
		// }

		// assignment = localOptimization(assignment);

		calculateWait(assignment);

		// printFinalCSV(assignment);

		if (args.length != 0) {
			overallWriter.println("Greedy:" + GREEDY_CHOICE + ", Size:" + NUM[0] + "-" + NUM[1]
					+ ", Wait: " + totalWait);
			overallWriter.flush();
			overallWriter.close();
		}

		Date endTime = new Date();
		System.out.println("Run complete");

		print("Start Time: " + startTime);
		print("End Time: " + endTime);
		print("Duration (sec): " + ((endTime.getTime() - startTime.getTime()) / 1000.0));

		resultsWriter.flush();
		resultsWriter.close();
		summaryWriter.flush();
		summaryWriter.close();
	}

	private static ArrayList<int[]> localOptimization(ArrayList<int[]> assignment) {
		// final double DIFFERENCE = 3; // set tolerance between requestTime

		System.out.println("local optimization");

		HashMap<Integer, Double[]> taxiAvail = new HashMap<Integer, Double[]>();
		for (int i = 0; i < taxiLocations.size(); i++) {
			taxiAvail.put(i + 1, new Double[] { (double) taxiLocations.get(i), 0.0 });
		}

		ArrayList<int[]> bestAssignment = assignment;
		double shortestWait = calculateWait(assignment);

		for (int num = 0; num < assignment.size() - 1; num++) {
			ArrayList<int[]> newAssignment = new ArrayList<int[]>();

			System.out.println("num:" + num);
			// 0-nearestTaxiId, 1-nearestTaxiLoc, 2-requestId, 3-requestTime, 4-startLoc, 5-descLoc
			int[] a = assignment.get(num);
			int[] b = assignment.get(num + 1);

			int taxiIdA = a[0];
			int taxiIdB = b[0];
			if (taxiIdA != taxiIdB) {
				int[] newA = new int[] { a[0], a[1], b[2], b[3], b[4], b[5] };
				int[] newB = new int[] { b[0], b[1], a[2], a[3], a[4], a[5] };
				newAssignment.add(newA);
				newAssignment.add(newB);

				double taxiAAvail = taxiAvail.get(newA[0])[1];
				System.out.println("problem:" + (newA[0]));
				System.out.println("problem:" + (newB[0]));
				double taxiBAvail = taxiAvail.get(newB[0])[1];
				Dijkstra d1 = dijkstraMap.get(newA[4]);
				Dijkstra d2 = dijkstraMap.get(newB[4]);

				double travelTimeA = d1.getShortestDistanceTo(newA[1]);
				double journeyTimeA = d1.getShortestDistanceTo(newA[5]);
				double setOffTimeA = newA[3] - travelTimeA;
				double waitTimeA = taxiAAvail - setOffTimeA;

				double travelTimeB = d2.getShortestDistanceTo(newB[1]);
				double journeyTimeB = d2.getShortestDistanceTo(newB[5]);
				double setOffTimeB = newB[3] - travelTimeB;
				double waitTimeB = taxiBAvail - setOffTimeB;

				if (waitTimeA < 0)
					waitTimeA = 0;
				if (waitTimeB < 0)
					waitTimeB = 0;

				double taxiANextAvail = waitTimeA + journeyTimeA + newA[3];
				double taxiBNextAvail = waitTimeB + journeyTimeB + newB[3];

				taxiAvail.put(taxiIdA, new Double[] { (double) newA[5], taxiANextAvail });
				taxiAvail.put(taxiIdB, new Double[] { (double) newB[5], taxiBNextAvail });

			}

			System.out.println("print taxi avail");
			for (Map.Entry<Integer, Double[]> doubles : taxiAvail.entrySet()) {
				System.out.println(doubles.getKey() + " " + doubles.getValue()[0] + " "
						+ doubles.getValue()[1]);
			}

			ArrayList<Integer[]> newDemands = new ArrayList<Integer[]>(demands);
			Collections.sort(newDemands, new Comparator<Integer[]>() { // sort by requesttime
				@Override
				public int compare(Integer[] o1, Integer[] o2) {
					return o1[2].compareTo(o2[2]);
				}
			});
			System.out.println("newdemands size: " + newDemands.size());
			newDemands.remove(num);
			newDemands.remove(num);

			System.out.println("new demands");
			for (Integer[] integers : newDemands) {
				System.out.println(integers[0] + " " + integers[1] + " " + integers[2]);
			}
			System.out.println("taxiavail");
			for (Map.Entry<Integer, Double[]> e : taxiAvail.entrySet()) {
				System.out.println(e.getKey() + " " + e.getValue()[0] + " " + e.getValue()[1]);
			}

			// 0-nearestTaxiId, 1-nearestTaxiLoc, 2-requestId, 3-requestTime, 4-startLoc, 5-descLoc
			newAssignment.addAll(runGreedy(newDemands, null, taxiAvail));
			for (int[] i : newAssignment) {
				System.out.println(
						i[0] + " " + i[1] + " " + i[2] + " " + i[3] + " " + i[4] + " " + i[5]);
			}

			double newAssignmentWait = calculateWait(newAssignment);
			if (newAssignmentWait < shortestWait) {
				shortestWait = newAssignmentWait;
				bestAssignment = newAssignment;
			}
		}

		return bestAssignment;

	}

	private static double calculateWait(ArrayList<int[]> assignment) {
		double totalWait = 0.0;

		Map<Integer, Double> taxisAvail = new HashMap<Integer, Double>();
		for (int i = 0; i < taxiLocations.size(); i++) {
			taxisAvail.put(i + 1, 0.0);
		}

		// 0-nearestTaxiId, 1-nearestTaxiLoc, 2-requestId, 3-requestTime, 4-startLoc, 5-descLoc
		for (int[] i : assignment) {
			int taxiId = i[0];
			int taxiLoc = i[1];
			int requestId = i[2];
			int requestTime = i[3];
			int startLoc = i[4];
			int descLoc = i[5];

			Dijkstra dijkstra = dijkstraMap.get(startLoc);
			double taxiAvailTime = taxisAvail.get(taxiId);
			double travelTime = dijkstra.getShortestDistanceTo(taxiLoc);

			double setOffTime = requestTime - travelTime;
			double waitTime = taxiAvailTime - setOffTime;
			if (waitTime < 0)
				waitTime = 0;

			totalWait += waitTime;

		}

		System.out.println("Calculate wait: " + totalWait);
		return totalWait;
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
					double travelTime = dijkstra.getShortestDistanceTo(taxiLoc);

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

	private static ArrayList<int[]> runGreedy(ArrayList<Integer[]> demands,
			ArrayList<Integer> taxiLocations, HashMap<Integer, Double[]> taxiAvail) {

		totalWait = 0.0;

		// Set customer to taxi [taxi id, taxi loc, request id, request loc]
		ArrayList<int[]> assignment = new ArrayList<int[]>();

		// Set taxi available list [ taxi id, taxi location, time available]
		if (taxiAvail == null) {
			taxiAvail = new HashMap<Integer, Double[]>();
			for (int i = 0; i < taxiLocations.size(); i++) {
				taxiAvail.put(i + 1, new Double[] { (double) taxiLocations.get(i), 0.0 });
			}
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

			for (Map.Entry<Integer, Double[]> i : taxiAvail.entrySet()) {
				double taxiAvailTime = i.getValue()[1];
				int taxiId = i.getKey();
				int taxiLoc = i.getValue()[0].intValue();

				// Choosing logic
				double travelTime = dijkstra.getShortestDistanceTo(taxiLoc);
				double setOffTime = requestTime - travelTime;
				double waitTime = taxiAvailTime - setOffTime; // negative value means on time

				// System.out.println("traveltime:" + travelTime);
				// System.out.println("setofftime:" + setOffTime);
				// System.out.println("waittime:" + waitTime);
				// System.out.println("taxiavailtime:" + taxiAvailTime);
				// System.out.println("reqtime:" + requestTime);
				// System.out.println();

				// print("waitTime:" + waitTime);

				// ***** greedy logic *****
				boolean chosen = false;
				if (GREEDY_CHOICE == 2) {
					// print("con1:" + (traversed == false));
					// print("con2:" + (shortestWait > 0 && waitTime <= 0));
					// print("con3:" + (shortestWait < 0 && waitTime < 0 && waitTime >
					// shortestWait));
					// print("con4:" + (shortestWait > 0 && waitTime > 0 && waitTime <
					// shortestWait));
					chosen = (traversed == false
							|| (shortestWait > THRESHOLD && waitTime <= THRESHOLD)
							|| (shortestWait < THRESHOLD && waitTime < THRESHOLD
									&& waitTime > shortestWait)
							|| (shortestWait > THRESHOLD && waitTime > THRESHOLD
									&& waitTime < shortestWait));
					traversed = true;

				} else if (GREEDY_CHOICE == 1) {
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
			Double[] taxi = taxiAvail.get(nearestTaxiId);
			if (shortestWait < 0) {
				shortestWait = 0;
			}
			totalWait += shortestWait;
			taxi[0] = (double) descLoc; // update taxi location
			taxi[1] = requestTime + shortestWait + chosenJourneyTime; // update available time
			taxiAvail.put(nearestTaxiId, taxi);

		}

		print("\nANS");
		for (int[] i : assignment) {
			print(i[0] + ", " + i[1] + ", " + i[2] + ", " + i[3] + ", " + i[4] + ", " + i[5]);
		}

		print("");
		print("Greedy Total Wait: " + totalWait);
		return assignment;

	}

	private static void printFinalCSV(ArrayList<int[]> assignment) {
		// print to resultsWriter
		// [taxi id, taxi loc, request id, request loc, desc loc]
		for (int[] i : assignment) {
			int taxiId = i[0];
			int taxiLoc = i[1];
			// int requestId = i[2];
			int requestTime = i[3];
			int startLoc = i[4];
			int descLoc = i[5];

			Dijkstra dijkstra = dijkstraMap.get(startLoc);

			List<Edge> p1 = dijkstra.getShortestPathTo(taxiLoc);
			List<Edge> p2 = dijkstra.getShortestPathTo(descLoc);

			// taxi travel to startLoc
			for (int a = p1.size() - 1; a >= 0; a--) {
				if (a == p1.size() - 1) {
					resultsWriter.println(taxiId + ",Taxi,NA," + p1.get(a).id);
				} else {
					resultsWriter.println(taxiId + ",Trans,NA," + p1.get(a).id);
				}
			}

			// start travelling journey
			for (int a = 0; a < p2.size(); a++) {
				if (a == 0) {
					resultsWriter.println(taxiId + ",Start," + requestTime + "," + p2.get(a).id);
				} else if (a == p2.size() - 1) {
					resultsWriter.println(taxiId + ",End,NA," + p2.get(a).id);
				} else {
					resultsWriter.println(taxiId + ",Trans,NA," + p2.get(a).id);
				}
			}
		}
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
