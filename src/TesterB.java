import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
	final static double THRESHOLD = 0;

	static HashMap<Integer, Dijkstra> dijkstraMap = new HashMap<Integer, Dijkstra>();

	public static void main(String[] args) {

		boolean compile = false;
		if (args.length != 0) {
			compile = true;
			NUM[0] = Integer.parseInt(args[0]);
			NUM[1] = Integer.parseInt(args[1]);
			GREEDY_CHOICE = Integer.parseInt(args[2]);
		}

		Date startTime = new Date();

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

			if (compile) {
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

		List<Request> requests = new ArrayList<Request>();
		for (int i = 0; i < demands.size(); i++) {
			Integer[] j = demands.get(i);
			requests.add(new Request(i + 1, j[0], j[1], j[2]));
		}
		Collections.sort(requests, new Comparator<Request>() {
			@Override
			public int compare(Request o1, Request o2) {
				return o1.requestTime - o2.requestTime;
			}
		});

		Map<Integer, Double[]> taxisAvailList = new TreeMap<Integer, Double[]>();
		for (int i = 0; i < taxiLocations.size(); i++) {
			taxisAvailList.put(i + 1, new Double[] { (double) taxiLocations.get(i), 0.0 });
		}

		createDijkstraMap();

		List<Assignment> assignment = runGreedy(new ArrayList<Request>(requests),
				new HashMap<Integer, Double[]>(taxisAvailList));
		// ArrayList<int[]> assignment = runModel();

		assignment = localSwap(new ArrayList<Request>(requests), assignment,
				new HashMap<Integer, Double[]>(taxisAvailList));

		double totalWait = calculateTotalWait(assignment);

		printFinalCSV(assignment);

		if (compile) {
			overallWriter.println("Greedy:" + GREEDY_CHOICE + ", Size:" + NUM[0] + "-" + NUM[1]
					+ ", Wait: " + totalWait);
			overallWriter.flush();
			overallWriter.close();
		}

		Date endTime = new Date();
		System.out.println("Run complete");

		print("Assignment...");
		for (Assignment a : assignment) {
			print(a.toString());
		}
		print("");
		print("Total Wait: " + totalWait);
		print("Start Time: " + startTime);
		print("End Time: " + endTime);
		print("Duration (sec): " + ((endTime.getTime() - startTime.getTime()) / 1000.0));

		resultsWriter.flush();
		resultsWriter.close();
		summaryWriter.flush();
		summaryWriter.close();
	}

	private static List<int[]> runModel() {

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
				int descLoc = demands.get(i)[1];
				int requestTime = demands.get(i)[2];

				Dijkstra dijkstra = dijkstraMap.get(startLoc);

				for (int k = 0; k < taxiLocations.size(); k++) {
					int taxiLoc = taxiLocations.get(k);

					double travelTime = dijkstra.getShortestDistanceTo(taxiLoc);
					double setOffTime = requestTime - travelTime;
					// double waitTime = y[i][k]*

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

	private static List<Assignment> runGreedy(List<Request> requests,
			Map<Integer, Double[]> taxisAvailList) {

		ArrayList<Assignment> assignment = new ArrayList<Assignment>();

		while (!requests.isEmpty()) {
			Request r = requests.remove(0);
			Dijkstra dijkstra = dijkstraMap.get(r.startLoc);

			boolean hasTravelled = false;
			double shortestWait = 0;
			int shortestTaxiLoc = 0;
			int shortestTaxiId = 0;

			// <taxiId, [taxiLoc, taxiAvail]>
			for (Map.Entry<Integer, Double[]> e : taxisAvailList.entrySet()) {
				int taxiId = e.getKey();
				int taxiLoc = e.getValue()[0].intValue();
				double taxiAvailTime = e.getValue()[1];

				// ********************************************************************
				// ************************ CHOOSE GREEDY TYPE ************************
				// ********************************************************************

				// Time to travel from taxiLoc to startLoc
				double travelTime = dijkstra.getShortestDistanceTo(taxiLoc);
				// Time taxi must set off to reach startLoc at requestTime. ** what if <0?
				double setOffTime = r.requestTime - travelTime;
				// Time customer needs to wait for this taxi
				double waitTime = taxiAvailTime - setOffTime;

				// System.out.println("taxiLoc:" + taxiLoc);
				// System.out.println("travelTime:" + travelTime);
				// System.out.println("setOffTime:" + setOffTime);
				// System.out.println("waitTime:" + waitTime);
				// System.out.println();

				boolean isShorter = false;
				if (GREEDY_CHOICE == 1) {
					isShorter = (hasTravelled == false || waitTime < shortestWait);
				} else if (GREEDY_CHOICE == 2) {
					isShorter = (hasTravelled == false
							|| (shortestWait > THRESHOLD && waitTime <= THRESHOLD)
							|| (shortestWait < THRESHOLD && waitTime < THRESHOLD
									&& waitTime > shortestWait)
							|| (shortestWait > THRESHOLD && waitTime > THRESHOLD
									&& waitTime < shortestWait));
				}

				if (isShorter) {
					hasTravelled = true;
					shortestWait = waitTime;
					shortestTaxiLoc = taxiLoc;
					shortestTaxiId = taxiId;
				}
			}

			// Add the new assignment
			assignment.add(new Assignment(shortestTaxiId, shortestTaxiLoc, r.requestId,
					r.requestTime, r.startLoc, r.descLoc));

			double journeyTime = dijkstra.getShortestDistanceTo(shortestTaxiLoc);

			// Update taxi avail time
			double nextTaxiAvailTime = r.requestTime + shortestWait + journeyTime;
			taxisAvailList.put(shortestTaxiId,
					new Double[] { (double) r.descLoc, nextTaxiAvailTime });
		}

		return assignment;

	}

	private static List<Assignment> localSwap(List<Request> baseRequests,
			List<Assignment> assignment, Map<Integer, Double[]> baseTaxisAvailList) {

		System.out.println("Running local optimization");

		List<Assignment> bestAssignment = assignment;
		Map<Integer, Double[]> bestTaxiAvailList = new HashMap<Integer, Double[]>(
				baseTaxisAvailList);
		double shortestTotalWait = calculateTotalWait(assignment);

		for (int i = 0; i < assignment.size(); i++) {
			for (int j = 0; j < assignment.size(); j++) {
				if (i == j) {
					continue;
				}

				List<Assignment> testAssignment = new ArrayList<Assignment>();
				Map<Integer, Double[]> taxiAvailList = new HashMap<Integer, Double[]>(
						bestTaxiAvailList);

				Assignment a = assignment.get(i);
				Assignment b = assignment.get(j);

				Assignment newA = new Assignment(a.taxiId, a.taxiLoc, b.requestId, b.requestTime,
						b.startLoc, b.descLoc);
				Assignment newB = new Assignment(b.taxiId, b.taxiLoc, a.requestId, a.requestTime,
						a.startLoc, a.descLoc);

				testAssignment.add(newA);
				testAssignment.add(newB);

				List<Request> newRequests = new ArrayList<Request>(baseRequests);
				Iterator<Request> iterator = newRequests.iterator();
				while (iterator.hasNext()) {
					Request r = (Request) iterator.next();
					if (r.requestId == newA.requestId)
						iterator.remove();
					else if (r.requestId == newB.requestId)
						iterator.remove();
				}

				// newRequests.remove();
				// newRequests.remove(j);

				// Update taxi availability
				Dijkstra d1 = dijkstraMap.get(newA.startLoc);
				Dijkstra d2 = dijkstraMap.get(newB.startLoc);

				double taxiAAvail = taxiAvailList.get(newA.taxiId)[1];
				double taxiBAvail = taxiAvailList.get(newB.taxiId)[1];

				double travelTimeA = d1.getShortestDistanceTo(newA.taxiLoc);
				double setOffTimeA = newA.requestTime - travelTimeA;
				double waitTimeA = taxiAAvail - setOffTimeA;
				double journeyTimeA = d1.getShortestDistanceTo(newA.descLoc);
				double taxiNextAvailA = journeyTimeA + waitTimeA + newA.requestTime;

				double travelTimeB = d2.getShortestDistanceTo(newB.taxiLoc);
				double setOffTimeB = newB.requestTime - travelTimeB;
				double waitTimeB = taxiBAvail - setOffTimeB;
				double journeyTimeB = d2.getShortestDistanceTo(newB.descLoc);
				double taxiNextAvailB = journeyTimeB + waitTimeB + newB.requestTime;

				taxiAvailList.put(newA.taxiId,
						new Double[] { (double) newA.descLoc, taxiNextAvailA });
				taxiAvailList.put(newB.taxiId,
						new Double[] { (double) newB.descLoc, taxiNextAvailB });

				testAssignment.addAll(runGreedy(newRequests, baseTaxisAvailList));

				double totalWait = calculateTotalWait(testAssignment);

				if (totalWait < shortestTotalWait) {
					bestAssignment = testAssignment;
					shortestTotalWait = totalWait;
					bestTaxiAvailList = taxiAvailList;
				}
			}
		}

		return bestAssignment;
	}

	private static void printFinalCSV(List<Assignment> assignment) {
		// print to resultsWriter
		// [taxi id, taxi loc, request id, request loc, desc loc]
		for (Assignment i : assignment) {

			Dijkstra dijkstra = dijkstraMap.get(i.startLoc);

			List<Edge> p1 = dijkstra.getShortestPathTo(i.taxiLoc);
			List<Edge> p2 = dijkstra.getShortestPathTo(i.descLoc);

			// taxi travel to startLoc
			for (int a = p1.size() - 1; a >= 0; a--) {
				if (a == p1.size() - 1) {
					resultsWriter.println(i.taxiId + ",Taxi,NA," + p1.get(a).id);
				} else {
					resultsWriter.println(i.taxiId + ",Trans,NA," + p1.get(a).id);
				}
			}

			// start travelling journey
			for (int a = 0; a < p2.size(); a++) {
				if (a == 0) {
					resultsWriter
							.println(i.taxiId + ",Start," + i.requestTime + "," + p2.get(a).id);
				} else if (a == p2.size() - 1) {
					resultsWriter.println(i.taxiId + ",End,NA," + p2.get(a).id);
				} else {
					resultsWriter.println(i.taxiId + ",Trans,NA," + p2.get(a).id);
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

	private static double calculateTotalWait(List<Assignment> assignment) {
		double totalWait = 0.0;

		Map<Integer, Double> taxisAvail = new HashMap<Integer, Double>();
		for (int i = 0; i < taxiLocations.size(); i++) {
			taxisAvail.put(i + 1, 0.0);
		}

		// 0-nearestTaxiId, 1-nearestTaxiLoc, 2-requestId, 3-requestTime, 4-startLoc, 5-descLoc
		for (Assignment i : assignment) {
			Dijkstra dijkstra = dijkstraMap.get(i.startLoc);
			double taxiAvailTime = taxisAvail.get(i.taxiId);
			double travelTime = dijkstra.getShortestDistanceTo(i.taxiLoc);

			double setOffTime = i.requestTime - travelTime;
			double waitTime = taxiAvailTime - setOffTime;
			if (waitTime < 0)
				waitTime = 0;
			totalWait += waitTime;
		}
		System.out.println("Calculate wait: " + totalWait);
		return totalWait;
	}

	private static double calculateWait(double requestTime, double taxiAvailTime,
			double travelTime) {
		// travelTime is the time required for taxi to travel from its location to the start
		// location
		double setOffTime = requestTime - travelTime;
		double waitTime = taxiAvailTime - setOffTime; // negative value means on time

		return waitTime;
	}

	private static void print(String s) {
		System.out.println(s);
		summaryWriter.println(s);
	}

}
