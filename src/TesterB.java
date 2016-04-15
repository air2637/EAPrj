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
import java.util.Map.Entry;
import java.util.TreeMap;

import helper.InputReaderPartB;

public class TesterB {
	static ArrayList<Integer[]> demands;
	static ArrayList<Integer> taxiLocations;
	static PrintWriter summaryWriter, resultsWriter, overallWriter;
	static HashMap<Integer, Dijkstra> dijkstraMap = new HashMap<Integer, Dijkstra>();

	final static String dataFolderPath = "D:\\Dropbox\\SMU\\Year3Sem2\\Enterprise Analytics for Decision Support\\project\\supplementary\\supplementary\\training\\";
	//	final static String dataFolderPath = "D:\\Dropbox\\SMU\\Year3Sem2\\Enterprise Analytics for Decision Support\\project\\supplementary\\supplementary\\test\\test instances\\instance_b\\";

	// ********** Uncomment the data file as appropriate **********
	//	final static int[] NUM = new int[] { 30, 100 };
	final static int[] NUM = new int[] { 5, 6 };
	//	final static int[] NUM = new int[] { 10, 15 };
	//	final static int[] NUM = new int[] { 20, 25 };
	//	final static int[] NUM = new int[] { 50, 60 };
	//	final static int[] NUM = new int[] { 100, 120 };

	public static void main(String[] args) {

		Date startTime = new Date();
		System.out.println("running testb");

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
			requests.add(new Request(i + 1, i2[0], i2[1], i2[2])); // Add the demands into request object for easy calling
		}
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
			taxiList.put(i + 1, new Double[] { 0.0, taxiLocations.get(i).doubleValue() }); // taxiId, taxiAvailTime, taxiLoc
		}

		createDijkstraMap();

		List<Assignment> assignments = runAll(requests, taxiList); // Run greedy algorithm

		print("\nFinal Assignment...");
		for (Assignment a : assignments) {
			print(a.toString());
		}
		double finalTotal = calculateTotalWait(assignments);
		double finalAvg = (finalTotal / NUM[1]);

		printFinalCSV(assignments); // Print assignment to csv

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

	private static List<Assignment> runAll(ArrayList<Request> baseRequests,
			Map<Integer, Double[]> originalTaxiList) {

		ArrayList<Request> remainingRequests = new ArrayList<Request>(baseRequests);
		ArrayList<Assignment> finalAssignments = new ArrayList<Assignment>();
		Map<Integer, Double[]> levelTaxiList = new TreeMap<Integer, Double[]>(originalTaxiList);

		// While there are still requests that we have not tried
		while (!remainingRequests.isEmpty()) {
			System.out.println("Requests remaining: " + remainingRequests.size());

			Request r = remainingRequests.remove(0);
			Dijkstra dijkstra = dijkstraMap.get(r.startLoc);

			// To keep the taxi assignment with the best potential
			double journeyTime = dijkstra.getShortestDistanceTo(r.descLoc);
			double bestWait = -1;
			Assignment bestAssignment = null;
			Map<Integer, Double[]> bestTaxiList = null;

			for (Map.Entry<Integer, Double[]> e : levelTaxiList.entrySet()) {
				ArrayList<Assignment> tempAssignments = new ArrayList<Assignment>();
				Map<Integer, Double[]> tempTaxiList = new TreeMap<Integer, Double[]>(levelTaxiList);

				int taxiId = e.getKey();
				double taxiAvailTime = e.getValue()[0];
				int taxiLoc = e.getValue()[1].intValue();

				// The smaller the waitTime, the closer the taxi is from the start location
				double waitTime = calculateWait(r.requestTime, taxiAvailTime,
						dijkstra.getShortestDistanceTo(taxiLoc));

				// If calculated waitTime is < 0, change it to 0
				if (waitTime < 0)
					waitTime = 0;

				// Assign this taxi to this request for now and try run greedy for later requests
				Assignment newAssignment = new Assignment(taxiId, taxiLoc, r.requestId,
						r.requestTime, r.startLoc, r.descLoc);
				tempAssignments.add(newAssignment);

				// Update the taxi's available time
				tempTaxiList.put(taxiId, new Double[] { r.requestTime + waitTime + journeyTime,
						(double) r.descLoc });

				tempAssignments.addAll(finalAssignments); // Add the previously chosen "best" assignments for previous requests  
				tempAssignments.addAll(runNewGreedy(remainingRequests, tempTaxiList)); // Let greedy algorithm take care of the other later requests

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

				// If the totalWait of this arrangement has the best totalWait, keep it 
				if (bestWait == -1 || totalWait < bestWait) {
					bestWait = totalWait;
					bestAssignment = newAssignment;
					bestTaxiList = tempTaxiList;
				}

			}

			levelTaxiList = bestTaxiList;
			finalAssignments.add(bestAssignment);

		}

		return finalAssignments;

	}

	private static List<Assignment> runNewGreedy(ArrayList<Request> requests,
			Map<Integer, Double[]> baseTaxiList) {

		List<Assignment> assignments = new ArrayList<Assignment>();
		ArrayList<Request> newRequests = new ArrayList<Request>(requests);
		Map<Integer, Double[]> taxiList = new TreeMap<Integer, Double[]>(baseTaxiList);

		// While there are still requests 
		while (!newRequests.isEmpty()) {
			Request r = newRequests.remove(0);
			Dijkstra dijkstra = dijkstraMap.get(r.startLoc);

			ArrayList<NextBestTaxi> rankTaxi = new ArrayList<NextBestTaxi>();
			double journeyTime = dijkstra.getShortestDistanceTo(r.descLoc);

			for (Entry<Integer, Double[]> e : taxiList.entrySet()) {
				double taxiAvailTime = e.getValue()[0];
				int taxiLoc = e.getValue()[1].intValue();
				int taxiId = e.getKey();

				double waitTime = calculateWait(r.requestTime, taxiAvailTime,
						dijkstra.getShortestDistanceTo(taxiLoc));

				NextBestTaxi bt = new NextBestTaxi(taxiLoc, taxiId, waitTime, false);
				rankTaxi.add(bt);

			}
			// Rank the taxis according to their waitTime
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

			NextBestTaxi bestTaxi = rankTaxi.remove(0); // Retrieve the taxi with the shortest waitTime

			double actualWait = bestTaxi.waitTime;
			if (actualWait < 0) {
				actualWait = 0;
			}

			Assignment newAssignment = new Assignment(bestTaxi.taxiId, bestTaxi.taxiLoc,
					r.requestId, r.requestTime, r.startLoc, r.descLoc, rankTaxi, actualWait);
			assignments.add(newAssignment); // Add new assignment

			// Update the taxi's available time
			double nextAvailTime = r.requestTime + actualWait + journeyTime;
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

		}

		//		System.out.println("Calculate Total Wait:" + totalWait);
		//		System.out.println("Calculate Avg Wait:" + (totalWait / assignment.size()));
		return totalWait;
	}

	private static double calculateWait(double requestTime, double taxiAvailTime,
			double travelTime) {
		double setOffTime = requestTime - travelTime; // travelTime is the time required for taxi to travel from its location to the start location
		double waitTime = taxiAvailTime - setOffTime; // Negative value means on time

		return waitTime;
	}

	private static void print(String s) {
		System.out.println(s);
		summaryWriter.println(s);
	}

	private static void printFinalCSV(List<Assignment> assignments) {
		// print to resultsWriter
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
