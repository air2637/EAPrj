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
import java.util.Map.Entry;
import java.util.TreeMap;

import helper.InputReaderPartB;

public class TesterB {
	static ArrayList<Integer[]> demands;
	static ArrayList<Integer> taxiLocations;
	static PrintWriter summaryWriter, resultsWriter, overallWriter;
	final static String dataFolderPath = "D:\\Dropbox\\SMU\\Year3Sem2\\Enterprise Analytics for Decision Support\\project\\supplementary\\supplementary\\training\\";
	//	final static String dataFolderPath = "D:\\Dropbox\\SMU\\Year3Sem2\\Enterprise Analytics for Decision Support\\project\\supplementary\\supplementary\\test\\test instances\\instance_b\\";

	// {taxis, demands}
	//	final static int[] NUM = new int[] { 30, 100 };
	final static int[] NUM = new int[] { 5, 6 };
	//	final static int[] NUM = new int[] { 10, 15 };
	//	final static int[] NUM = new int[] { 20, 25 };
	//	final static int[] NUM = new int[] { 50, 60 };
	//	final static int[] NUM = new int[] { 100, 120 };

	static HashMap<Integer, Dijkstra> dijkstraMap = new HashMap<Integer, Dijkstra>();

	public static void main(String[] args) {

		Date startTime = new Date();
		System.out.println("running testb3");

		boolean compile = false;
		if (args.length > 0) {
			compile = true;
			NUM[0] = Integer.parseInt(args[0]);
			NUM[1] = Integer.parseInt(args[1]);
		}

		try {

			summaryWriter = new PrintWriter(new BufferedWriter(
					new FileWriter("part b/results-" + NUM[0] + "_" + NUM[1] + ".txt", false)));
			resultsWriter = new PrintWriter(new BufferedWriter(
					new FileWriter("part b/results-" + NUM[0] + "_" + NUM[1] + ".csv", false)));

			if (compile) {
				overallWriter = new PrintWriter(
						new BufferedWriter(new FileWriter("part b/overall_summary.txt", true)));
			}

		} catch (IOException e1) {
			e1.printStackTrace();
		}

		InputReaderPartB partB = new InputReaderPartB(dataFolderPath,
				new Integer[] { NUM[0], NUM[1] });
		demands = partB.getDemands();
		taxiLocations = partB.getTaxiLocations();

		ArrayList<Request> requests = new ArrayList<>();
		for (int i = 0; i < demands.size(); i++) {
			Integer[] i2 = demands.get(i);
			requests.add(new Request(i + 1, i2[0], i2[1], i2[2]));
		}
		//		for (Request r : requests) {
		//			System.out.println(r.toString());
		//		}
		Collections.sort(requests, new Comparator<Request>() {
			@Override
			public int compare(Request o1, Request o2) {
				if (o1.requestTime < o2.requestTime) {
					return -1;
				} else if (o1.requestTime >= o2.requestTime) {
					return 1;
				} else {
					return 0;
				}
			}
		});

		Map<Integer, Double[]> taxiList = new TreeMap<>();
		for (int i = 0; i < taxiLocations.size(); i++) {
			taxiList.put(i + 1, new Double[] { 0.0, taxiLocations.get(i).doubleValue() }); // id, avail time, loc
		}
		//		for (Map.Entry<Integer, Stack<Double[]>> e : taxiList.entrySet()) {
		//			System.out.println(
		//					e.getKey() + ", " + e.getValue().peek()[0] + ", " + e.getValue().peek()[1]);
		//		}

		createDijkstraMap();

		//greedy***
		List<Assignment> assignments = runAll(requests, taxiList);

		//		assignments = localSwap(requests, assignments, taxiList);

		//		assignments = improvementAlgo(requests, assignments, taxiList);

		print("\nFinal Assignment...");
		for (Assignment a : assignments) {
			print(a.toString());
		}
		double finalTotal = calculateTotalWait(assignments);
		double finalAvg = (finalTotal / NUM[1]);

		printFinalCSV(assignments);

		if (compile) {
			overallWriter.println("Size:" + NUM[0] + "-" + NUM[1] + ", Total: " + finalTotal
					+ ", Avg: " + finalAvg);
			overallWriter.flush();
			overallWriter.close();
		}

		Date endTime = new Date();

		print("");
		print("Total: " + finalTotal);
		print("Avg: " + finalAvg);
		print("");
		print("Start Time:" + startTime);
		print("End Time:" + endTime);
		print("Duration(sec):" + ((endTime.getTime() - startTime.getTime()) / 1000.0));

		resultsWriter.flush();
		resultsWriter.close();
		summaryWriter.flush();
		summaryWriter.close();

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

				ArrayList<Request> newRequests = new ArrayList<Request>(baseRequests);
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

				testAssignment.addAll(runNewGreedy(newRequests, baseTaxisAvailList));

				double totalWait = calculateTotalWait(testAssignment);

				if (totalWait < shortestTotalWait) {
					bestAssignment = testAssignment;
					shortestTotalWait = totalWait;
					bestTaxiAvailList = taxiAvailList;

					Collections.sort(assignment, new Comparator<Assignment>() {
						@Override
						public int compare(Assignment o1, Assignment o2) {
							return o1.requestTime - o2.requestTime;
						}
					});
				}
			}
		}

		return bestAssignment;
	}

	private static List<Assignment> runAll(ArrayList<Request> baseRequests,
			Map<Integer, Double[]> originalTaxiList) {

		ArrayList<Request> remainingRequests = new ArrayList<Request>(baseRequests);
		ArrayList<Assignment> finalAssignments = new ArrayList<Assignment>();
		Map<Integer, Double[]> levelTaxiList = new TreeMap<Integer, Double[]>(originalTaxiList);

		while (!remainingRequests.isEmpty()) {
			System.out.println("Requests remaining: " + remainingRequests.size());
			//			System.out.println("\n\ncount:" + counter++);

			Request r = remainingRequests.remove(0);
			Dijkstra dijkstra = dijkstraMap.get(r.startLoc);
			double journeyTime = dijkstra.getShortestDistanceTo(r.descLoc);
			double bestWait = -1;
			Assignment bestAssignment = null;
			Map<Integer, Double[]> bestTaxiList = null;

			//			System.out.println("print leveltaxi");
			//			for (Map.Entry<Integer, Double[]> e : levelTaxiList.entrySet()) {
			//				System.out.println(e.getKey() + ", " + e.getValue()[0] + ", " + e.getValue()[1]);
			//			}

			for (Map.Entry<Integer, Double[]> e : levelTaxiList.entrySet()) {
				ArrayList<Assignment> tempAssignments = new ArrayList<Assignment>();
				Map<Integer, Double[]> tempTaxiList = new TreeMap<Integer, Double[]>(levelTaxiList);
				//				System.out.println(e.getKey() + ", " + e.getValue()[0] + ", " + e.getValue()[1]);

				int taxiId = e.getKey();
				double taxiAvailTime = e.getValue()[0];
				int taxiLoc = e.getValue()[1].intValue();

				double waitTime = calculateWait(r.requestTime, taxiAvailTime,
						dijkstra.getShortestDistanceTo(taxiLoc));

				if (waitTime < 0)
					waitTime = 0;

				// new assignment
				Assignment newAssignment = new Assignment(taxiId, taxiLoc, r.requestId,
						r.requestTime, r.startLoc, r.descLoc);
				tempAssignments.add(newAssignment);

				// update new taxilist
				tempTaxiList.put(taxiId, new Double[] { r.requestTime + waitTime + journeyTime,
						(double) r.descLoc });

				//				System.out.println("checking........");
				//				for (Request r2 : remainingRequests) {
				//					System.out.println(r2.toString());
				//				}
				//				for (Map.Entry<Integer, Double[]> e2 : tempTaxiList.entrySet()) {
				//					System.out.println(
				//							e2.getKey() + ", " + e2.getValue()[0] + ", " + e2.getValue()[1]);
				//				}

				tempAssignments.addAll(finalAssignments);
				tempAssignments.addAll(runNewGreedy(remainingRequests, tempTaxiList));

				Collections.sort(tempAssignments, new Comparator<Assignment>() {
					@Override
					public int compare(Assignment o1, Assignment o2) {
						if (o1.requestTime < o2.requestTime) {
							return -1;
						} else if (o1.requestTime >= o2.requestTime) {
							return 1;
						} else {
							return 0;
						}
					}
				});

				double totalWait = calculateTotalWait(tempAssignments);

				if (bestWait == -1 || totalWait < bestWait) {
					bestWait = totalWait;
					bestAssignment = newAssignment;
					bestTaxiList = tempTaxiList;
				}

			}

			levelTaxiList = bestTaxiList;

			//			System.out.println("print leveltaxi222222");
			//			for (Map.Entry<Integer, Double[]> e : levelTaxiList.entrySet()) {
			//				System.out.println(e.getKey() + ", " + e.getValue()[0] + ", " + e.getValue()[1]);
			//			}
			finalAssignments.add(bestAssignment);

		}

		return finalAssignments;

	}

	private static List<Assignment> runNewGreedy(ArrayList<Request> requests,
			Map<Integer, Double[]> baseTaxiList) {

		List<Assignment> assignments = new ArrayList<Assignment>();
		ArrayList<Request> newRequests = new ArrayList<Request>(requests);
		Map<Integer, Double[]> taxiList = new TreeMap<Integer, Double[]>(baseTaxiList);

		//greedy***
		while (!newRequests.isEmpty()) {
			Request r = newRequests.remove(0);

			Dijkstra dijkstra = dijkstraMap.get(r.startLoc);
			ArrayList<NextBestTaxi> rankTaxi = new ArrayList<NextBestTaxi>();
			double journeyTime = dijkstra.getShortestDistanceTo(r.descLoc);

			for (Entry<Integer, Double[]> e : taxiList.entrySet()) {
				double taxiAvailTime = e.getValue()[0];
				int taxiLoc = e.getValue()[1].intValue();
				int taxiId = e.getKey();

				//				System.out.println("taxiId:" + taxiId + ", taxiLoc:" + taxiLoc + ", taxiAvailTime:"
				//						+ taxiAvailTime);

				double waitTime = calculateWait(r.requestTime, taxiAvailTime,
						dijkstra.getShortestDistanceTo(taxiLoc));

				NextBestTaxi bt = new NextBestTaxi(taxiLoc, taxiId, waitTime, false);
				//				System.out.println(bt.toString() + ", taxiAvailTime:" + taxiAvailTime);
				rankTaxi.add(bt);

			}
			//			for (NextBestTaxi nextBestTaxi : rankTaxi) {
			//				System.out.println(nextBestTaxi.toString());
			//			}
			Collections.sort(rankTaxi, new Comparator<NextBestTaxi>() {
				@Override
				public int compare(NextBestTaxi o1, NextBestTaxi o2) {
					if (o1.waitTime < o2.waitTime) {
						return -1;
					} else if (o1.waitTime >= o2.waitTime) {
						return 1;
					} else {
						return 0;
					}
				}
			});

			//			for (NextBestTaxi nextBestTaxi : rankTaxi) {
			//				System.out.println(nextBestTaxi.toString());
			//			}

			NextBestTaxi bestTaxi = rankTaxi.remove(0);

			double actualWait = bestTaxi.waitTime;
			if (actualWait < 0) {
				actualWait = 0;
			}

			Assignment newAssignment = new Assignment(bestTaxi.taxiId, bestTaxi.taxiLoc,
					r.requestId, r.requestTime, r.startLoc, r.descLoc, rankTaxi, actualWait);
			//			System.out.println(newAssignment.toString());
			//			System.out.println();
			assignments.add(newAssignment); // add new assignment

			// update taxi avail time
			//			Double[] stack = taxiList.get(bestTaxi.taxiId);

			double nextAvailTime = r.requestTime + actualWait + journeyTime;
			//			stack.push(new Double[] { nextAvailTime, (double) r.descLoc });
			taxiList.put(bestTaxi.taxiId, new Double[] { nextAvailTime, (double) r.descLoc });

		}

		return assignments;
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
		// print("totalWait");

		Map<Integer, Double> taxisAvail = new HashMap<Integer, Double>();
		for (int i = 0; i < taxiLocations.size(); i++) {
			taxisAvail.put(i + 1, 0.0);
		}

		for (Assignment a : assignment) {
			Dijkstra dijkstra = dijkstraMap.get(a.startLoc);
			int requestTime = a.requestTime;
			double taxiAvailTime = taxisAvail.get(a.taxiId);
			double travelTime = dijkstra.getShortestDistanceTo(a.taxiLoc);
			double journeyTime = dijkstra.getShortestDistanceTo(a.descLoc);

			double setOffTime = requestTime - travelTime;
			double waitTime = taxiAvailTime - setOffTime;

			if (waitTime < 0) {
				waitTime = 0;
			}
			totalWait += waitTime;

			double nextTaxiAvailTime = waitTime + requestTime + journeyTime;
			taxisAvail.put(a.taxiId, nextTaxiAvailTime);

			// System.out.println("\ntaxiavail:" + taxiAvailTime + "\nrequestTime:" + requestTime
			// + "\ntravelTime:" + travelTime + "\nsetOffTime:" + setOffTime + "\njourneyTime:"
			// + journeyTime + "\nwaitTime:" + waitTime + "\n" + a.toString());

			// print(Double.toString(waitTime));

			// for (Map.Entry<Integer, Double> e : taxisAvail.entrySet()) {
			// System.out.println(e.getKey() + " " + e.getValue());
			// }

		}

		//		System.out.println("Calculate Total Wait:" + totalWait);
		//		System.out.println("Calculate Avg Wait:" + (totalWait / assignment.size()));
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

	private static void printFinalCSV(List<Assignment> assignments) {
		// print to resultsWriter
		// [taxi id, taxi loc, request id, request loc, desc loc]
		for (Assignment i : assignments) {

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

}
