import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import helper.InputReaderPartA;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class Tester {
	static List<Node> nodes = new ArrayList<Node>();
	static List<Edge> edges = new ArrayList<Edge>();

	final static String dataFolderPath = "D:\\Dropbox\\SMU\\Year3Sem2\\Enterprise Analytics for Decision Support\\project\\supplementary\\supplementary\\training\\";
	final static String INPUT_FILE = "sin_train_5_5.txt";

	static PrintWriter w;

	public static void main(String[] args) {

		try {
			w = new PrintWriter(new BufferedWriter(new FileWriter("ans.txt", false)));
			System.out.println("running...");
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
		// Stack<Edge> p = runDijkstra(graph, 5453, 3915);
		// Double total = getTotalDist(p);

		InputReaderPartA partA = new InputReaderPartA(dataFolderPath + INPUT_FILE, 5);
		ArrayList<Integer[]> demands = partA.getDemands();
		ArrayList<Integer> taxiLocations = partA.getTaxiLocations();

		int[][] ans = runModel(graph, demands, taxiLocations);
		for (int i = 0; i < demands.size(); i++) {
			for (int j = 0; j < taxiLocations.size(); j++) {
				System.out.print(ans[i][j] + " ");
				w.print(ans[i][j] + " ");
			}
			System.out.println();
			w.println();
		}

		// Double t = getShortestPathDistanceBetweenNodes(graph, 0, 9);
		// w.println("dist: " + t);

		w.flush();
		w.close();

	}

	private static int[][] runModel(Graph graph, ArrayList<Integer[]> demands,
			ArrayList<Integer> taxiLocations) {

		int[][] assignment = new int[demands.size()][taxiLocations.size()];

		try {
			// Define an empty model
			IloCplex model = new IloCplex();

			// Define the binary variables
			IloNumVar[][] x = new IloNumVar[demands.size()][taxiLocations.size()];
			for (int i = 0; i < demands.size(); i++) {
				for (int j = 0; j < taxiLocations.size(); j++) {
					x[i][j] = model.boolVar();
				}
			}

			// Define the objective function
			IloLinearNumExpr obj = model.linearNumExpr();
			for (int i = 0; i < demands.size(); i++) {
				for (int j = 0; j < taxiLocations.size(); j++) {
					int a = demands.get(i)[0];
					int b = taxiLocations.get(j);

					System.out.println("demand: " + a);
					System.out.println("taxi: " + b);

					obj.addTerm(getShortestPathDistanceBetweenNodes(graph, a, b), x[i][j]);
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
				System.out.println("obj_val = " + objValue);
				w.println("obj value: " + objValue);

				for (int i = 0; i < demands.size(); i++) {
					for (int j = 0; j < taxiLocations.size(); j++) {
						assignment[i][j] = (int) model.getValue(x[i][j]);
					}
				}

				double totalDistForDemand = 0.0;
				for (Integer[] i : demands) {
					int startPoint = i[0];
					int endPoint = i[1];
					totalDistForDemand += getShortestPathDistanceBetweenNodes(graph, startPoint,
							endPoint);
				}
				System.out.println("totalDistForDemand: " + totalDistForDemand);
				System.out.println("Final final value: " + (totalDistForDemand + objValue));

				w.println("totalDistforDemand: " + totalDistForDemand);
				w.println("final value: " + (totalDistForDemand + objValue));

			} else {
				System.out.println("Model not solved :(");
			}

		} catch (IloException e) {
			e.printStackTrace();
		}

		System.out.println("model completed!");
		return assignment;

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
