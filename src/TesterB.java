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
import java.util.Stack;

import helper.InputReaderPartB;

public class TesterB {
	static List<Node> nodes = new ArrayList<Node>();
	static List<Edge> edges = new ArrayList<Edge>();

	static ArrayList<Integer[]> demands;
	static ArrayList<Integer> taxiLocations;

	final static String dataFolderPath = "D:\\Dropbox\\SMU\\Year3Sem2\\Enterprise Analytics for Decision Support\\project\\supplementary\\supplementary\\training\\";
	final static int NUM_TAXI = 100;
	final static int NUM_DEMAND = 120;
	final static String INPUT_FILE = "sin_train_" + NUM_TAXI + "_" + NUM_DEMAND + ".txt";

	static Graph graph;

	static PrintWriter w;
	static PrintWriter w1;

	static HashMap<Integer, Dijkstra> dijkstraMap = new HashMap<Integer, Dijkstra>();

	public static void main(String[] args) {
		Date startTime = new Date();

		try {
			w = new PrintWriter(new BufferedWriter(new FileWriter(
					"part b/summary-b-" + NUM_TAXI + "_" + NUM_DEMAND + ".txt", false)));
			w1 = new PrintWriter(new BufferedWriter(new FileWriter(
					"part b/results-b-" + NUM_TAXI + "_" + NUM_DEMAND + ".csv", false)));
			System.out.println("running TestB. Size: " + NUM_TAXI + ", " + NUM_DEMAND);
		} catch (IOException e) {
			e.printStackTrace();
		}

		loadGraph();

		InputReaderPartB partB = new InputReaderPartB(new Integer[] { NUM_TAXI, NUM_DEMAND });
		demands = partB.getDemands();
		taxiLocations = partB.getTaxiLocations();

		System.out.println("taxi size: " + taxiLocations.size());
		System.out.println("demand size: " + demands.size());

		createDijkstraMap();

		runGreedy();

		Date nowTime = new Date();

		System.out.println("TestB run complete");
		System.out.println(
				"duration (min): " + ((nowTime.getTime() - startTime.getTime()) / 60000.0));

		w1.flush();
		w1.close();
		w.flush();
		w.close();

	}

	private static void runGreedy() {

		// Set customer to taxi [taxi id, taxi loc, request id, request loc]
		ArrayList<Integer[]> assignment = new ArrayList<Integer[]>();

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
			print("request-id:" + requestId + ", t:" + requestTime + ", ogn:" + startLoc + ", des:"
					+ descLoc);

			Dijkstra dijkstra = dijkstraMap.get(startLoc);

			double shortestWait = -1;
			int nearestTaxiLoc = -1;
			int nearestTaxiId = -1;
			double chosenJourneyTime = -1; // from taxiLoc to startLoc

			for (Double[] i : taxiAvail) {
				double taxiAvailTime = i[2];
				int taxiId = i[0].intValue();
				int taxiLoc = i[1].intValue();

				// Choosing logic
				double travelTime = dijkstra.shortestPathWeight(getNode(taxiLoc));
				double setOffTime = requestTime - travelTime;
				double waitTime = taxiAvailTime - setOffTime; // negative value means on time

				print("waitTime:" + waitTime);

				if (shortestWait == -1 || waitTime < shortestWait) {
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
			assignment.add(new Integer[] { nearestTaxiId, nearestTaxiLoc, requestId, requestTime,
					startLoc, descLoc });

			// Update taxi avail time
			Double[] taxi = taxiAvail.remove(nearestTaxiId - 1);
			if (shortestWait < 0) {
				shortestWait = 0;
			}
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
			for (Double[] i : taxiAvail) {
				print("taxiList-id:" + i[0].intValue() + ", loc:" + i[1].intValue() + ", time:"
						+ i[2].intValue());
			}
			print("");
		}

		print("ANS");
		for (Integer[] i : assignment) {
			print(i[0] + ", " + i[1] + ", " + i[2] + ", " + i[3] + ", " + i[4] + ", " + i[5]);
		}

		// print to w1
		// [taxi id, taxi loc, request id, request loc, desc loc]
		for (Integer[] i : assignment) {
			int taxiId = i[0];
			int taxiLoc = i[1];
			// int requestId = i[2];
			int requestTime = i[3];
			int startLoc = i[4];
			int descLoc = i[5];

			Dijkstra dijkstra = dijkstraMap.get(startLoc);

			Stack<Edge> p1 = dijkstra.shortestPath(getNode(taxiLoc));
			Stack<Edge> p2 = dijkstra.shortestPath(getNode(descLoc));

			// taxi travel to startLoc
			w1.println(taxiId + ",Taxi,NA," + p1.pop().id); // remove first
			while (!p1.isEmpty()) {
				w1.println(taxiId + ",Trans,NA," + p1.remove(0).id);
			}

			// start travelling journey
			w1.println(taxiId + ",Start," + requestTime + "," + p2.pop().id);
			while (!p2.isEmpty()) {
				if (p2.size() == 1) {
					w1.println(taxiId + ",End,NA," + p2.pop().id);
				} else {
					w1.println(taxiId + ",Trans,NA," + p2.pop().id);
				}
			}

		}

	}

	private static Dijkstra getDijkstra(Graph graph, int from) {
		Dijkstra dijkstra = new Dijkstra(graph);

		Node startNode = null;
		int breakCnt = 0;
		for (int i = 0; i < nodes.size(); i++) {
			if (breakCnt == 2) {
				break;
			}
			if (nodes.get(i).equals(new Node(String.valueOf(from)))) {
				startNode = nodes.get(i);
				breakCnt++;
			}
			if (nodes.get(i).equals(new Node(String.valueOf(from)))) {
				breakCnt++;
			}
		}
		dijkstra.execute(startNode);
		return dijkstra;
	}

	private static void createDijkstraMap() {
		for (int i = 0; i < demands.size(); i++) {
			int startLoc = demands.get(i)[0];
			System.out.println((i + 1) + ". Get dijkstra for " + startLoc);
			Dijkstra d = getDijkstra(graph, startLoc);
			dijkstraMap.put(startLoc, d);
		}
	}

	private static Node getNode(int num) {
		for (Node n : nodes) {
			if (Integer.parseInt(n.id) == num) {
				return n;
			}
		}
		return null;
	}

	private static void print(String s) {
		System.out.println(s);
		w.println(s);
	}

	private static void loadGraph() {
		RoadGraphReader roadGraphReader = new RoadGraphReader();
		HashMap<String, Edge> loadedEdges = roadGraphReader.getLoadedEdges();
		Iterator<String> iter = loadedEdges.keySet().iterator();
		while (iter.hasNext()) {
			String edgeId = (String) iter.next();
			Edge edge = loadedEdges.get(edgeId);
			addLane(edgeId, edge);
		}
		graph = new Graph(nodes, edges);
	}

	private static void addLane(String laneId, Edge edge) {
		Edge lane = new Edge(laneId, edge.source, edge.destination, edge.weight);
		Edge lane2 = new Edge(laneId, edge.destination, edge.source, edge.weight);
		if (!nodes.contains(edge.source))
			nodes.add(edge.source);
		if (!nodes.contains(edge.destination))
			nodes.add(edge.destination);

		edges.add(lane);
		edges.add(lane2);
	}

}
