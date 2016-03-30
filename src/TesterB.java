import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import helper.InputReaderPartB;

public class TesterB {
	static List<Node> nodes = new ArrayList<Node>();
	static List<Edge> edges = new ArrayList<Edge>();

	final static String dataFolderPath = "D:\\Dropbox\\SMU\\Year3Sem2\\Enterprise Analytics for Decision Support\\project\\supplementary\\supplementary\\training\\";
	final static String INPUT_FILE = "sin_train_5_6.txt";

	static PrintWriter w;
	static PrintWriter w1;

	public static void main(String[] args) {
		Date startTime = new Date();

		try {
			w = new PrintWriter(new BufferedWriter(new FileWriter("ans2.txt", false)));
			w1 = new PrintWriter(new BufferedWriter(new FileWriter("final2.csv", false)));
			System.out.println("running TestB...");
		} catch (IOException e) {
			e.printStackTrace();
		}

		RoadGraphReader roadGraphReader = new RoadGraphReader();

		HashMap<String, Edge> loadedEdges = roadGraphReader.getLoadedEdges();
		Iterator<String> iter = loadedEdges.keySet().iterator();
		while (iter.hasNext()) {
			String edgeId = (String) iter.next();
			Edge edge = loadedEdges.get(edgeId);
			addLane(edgeId, edge);
		}

		Graph graph = new Graph(nodes, edges);

		InputReaderPartB partB = new InputReaderPartB(new Integer[] { 5, 6 });
		ArrayList<Integer[]> demands = partB.getDemands();
		ArrayList<Integer> taxiLocations = partB.getTaxiLocations();

		for (Integer[] i : demands) {
			System.out.println(i[0] + ", " + i[1] + ", " + i[2]);
		}
		for (Integer i : taxiLocations) {
			System.out.println(i);
		}

		/*
		 * Run model here TODO
		 */

		Date nowTime = new Date();

		System.out.println("TestB run complete");
		System.out.println("duration (mins): " + (nowTime.getMinutes() - startTime.getMinutes()));

		w1.flush();
		w1.close();
		w.flush();
		w.close();

	}

	private static void runModel(Graph graph, ArrayList<Integer[]> demands,
			ArrayList<Integer> taxiLocations) {

	}

	private static double getShortestPathDistanceBetweenNodes(Graph graph, int n1, int n2) {
		System.out.println("running shortestdistdistm, start: " + n1 + ", end: " + n2);

		Stack<Edge> path = runDijkstra(graph, n1, n2);

		Double total = 0.0;
		for (int i = 0; i < path.size(); i++) {
			System.out.print(path.get(i) + " ");
			total += path.get(i).weight;
		}
		System.out.println();
		System.out.println("path " + n1 + " to " + n2 + ": " + total);
		w.println("path " + n1 + " to " + n2 + ": " + total);
		return total;
	}

	private static Stack<Edge> runDijkstra(Graph graph, int start, int end) {
		System.out.println("running dijkstra, start: " + start + ", end: " + end);

		Dijkstra dijkstra = new Dijkstra(graph);

		Node startNode = null;
		Node endNode = null;
		int breakCnt = 0;
		for (int i = 0; i < nodes.size(); i++) {
			if (breakCnt == 2) {
				break;
			}
			if (nodes.get(i).equals(new Node(String.valueOf(start)))) {
				startNode = nodes.get(i);
				breakCnt++;
			}
			if (nodes.get(i).equals(new Node(String.valueOf(end)))) {
				endNode = nodes.get(i);
				breakCnt++;
			}
		}
		dijkstra.execute(startNode);

		Stack<Edge> path = dijkstra.shortestPath(endNode);

		if (path == null) {
			System.out.println("No available path between " + start + " and " + end);
		}
		return path;
	}

	private static void addLane(String laneId, int sourceLocNo, int destLocNo, int duration) {
		Edge lane = new Edge(laneId, nodes.get(sourceLocNo), nodes.get(destLocNo), duration);
		edges.add(lane);
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
