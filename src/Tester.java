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
		} catch (IOException e) {
			e.printStackTrace();
		}

		/*
		 * for(int i=0; i<11; i++){ //Node location = new Node("Node_"+i); Node location = new
		 * Node(""+i); nodes.add(location); }
		 * 
		 * addLane("Edge_0", 0, 1, 85); addLane("Edge_1", 0, 2, 217); addLane("Edge_2", 0, 4, 173);
		 * addLane("Edge_3", 2, 6, 186); addLane("Edge_4", 2, 7, 103); addLane("Edge_5", 3, 7, 183);
		 * addLane("Edge_6", 5, 8, 250); addLane("Edge_7", 8, 9, 84); addLane("Edge_8", 7, 9, 167);
		 * addLane("Edge_9", 4, 9, 502); addLane("Edge_10", 9, 10, 40); addLane("Edge_11", 1, 10,
		 * 600);
		 */

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

		InputReaderPartA partA = new InputReaderPartA(dataFolderPath + INPUT_FILE);
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

	}

	private static int[][] runModel(Graph graph, ArrayList<Integer[]> demands,
			ArrayList<Integer> taxiLocations) {
		System.out.println("running model...");

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
					obj.addTerm(getDistBetweenNodes(graph, demands.get(i)[0], taxiLocations.get(j)),
							x[i][j]);
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

				for (int i = 0; i < demands.size(); i++) {
					for (int j = 0; j < taxiLocations.size(); j++) {
						assignment[i][j] = (int) model.getValue(x[i][j]);
					}
				}
			} else {
				System.out.println("Model not solved :(");
			}

		} catch (IloException e) {
			e.printStackTrace();
		}

		System.out.println("model completed!");
		return assignment;

	}

	private static double getDistBetweenNodes(Graph graph, int n1, int n2) {
		Stack<Edge> p = runDijkstra(graph, n1, n2);
		return getTotalDist(p);
	}

	private static double getTotalDist(Stack<Edge> path) {
		Double total = 0.0;
		for (int i = 0; i < path.size() - 1; i++) {
			total += path.get(i).weight;
		}
		System.out
				.println("path " + path.get(0) + " to " + path.get(path.size() - 1) + ": " + total);
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

		// if (path == null) {
		// System.out.println("No available path");
		// } else {
		// while (!path.isEmpty()) {
		// System.out.println(path.pop());
		// }
		// }
		// System.out.println("done!");
		System.out.println("completed dijkstra, start: " + start + ", end: " + end);
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
