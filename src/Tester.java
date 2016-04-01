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
import java.util.TreeMap;

import helper.InputReaderPartA;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class Tester {
	static List<Node> nodes = new ArrayList<Node>();
	static List<Edge> edges = new ArrayList<Edge>();

	final static String dataFolderPath = "D:\\Dropbox\\SMU\\Year3Sem2\\Enterprise Analytics for Decision Support\\project\\supplementary\\supplementary\\training\\";
	final static int NUM = 10;
	final static String INPUT_FILE = "sin_train_" + NUM + "_" + NUM + ".txt";

	static PrintWriter w;
	static PrintWriter w1;

	public static void main(String[] args) {
		Date startTime = new Date();

		try {
			w = new PrintWriter(new BufferedWriter(
					new FileWriter("part a/summary-a-" + NUM + "_" + NUM + ".txt", false)));
			w1 = new PrintWriter(new BufferedWriter(
					new FileWriter("part a/results-a-" + NUM + "_" + NUM + ".csv", false)));
			System.out.println("running Test A. Size: " + NUM);
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

		InputReaderPartA partA = new InputReaderPartA(dataFolderPath + INPUT_FILE, NUM);
		ArrayList<Integer[]> demands = partA.getDemands();
		ArrayList<Integer> taxiLocations = partA.getTaxiLocations();

		int[][] result = runModel(graph, demands, taxiLocations);
		for (int i = 0; i < demands.size(); i++) {
			for (int j = 0; j < taxiLocations.size(); j++) {
				w.print(result[i][j] + " ");
				System.out.print(result[i][j] + " ");

				// printing to final
				if (result[i][j] == 1) {
					int t = taxiLocations.get(j);
					int s = demands.get(i)[0];
					int e = demands.get(i)[1];

					Stack<Edge> p = runDijkstra(graph, t, s);
					int porg = p.size();
					while (!p.isEmpty()) {
						if (p.size() == porg) {
							w1.println("Taxi," + p.pop().id);
						} else {
							w1.println("Trans," + p.pop().id);
						}
					}

					Stack<Edge> p2 = runDijkstra(graph, s, e);
					int p2org = p2.size();
					while (!p2.isEmpty()) {
						if (p2.size() == 1) {
							w1.println("End," + p2.pop().id);
						} else if (p2.size() == p2org) {
							w1.println("Start," + p2.pop().id);
						} else {
							w1.println("Trans," + p2.pop().id);
						}

					}
				}
			}
			System.out.println();
			w.println();
		}

		// Double t = getShortestPathDistanceBetweenNodes(graph, 0, 9);
		// w.println("dist: " + t);
		Date nowTime = new Date();

		System.out.println("run complete");
		print("duration (min): " + ((nowTime.getTime() - startTime.getTime()) / 60000.0));

		w1.flush();
		w1.close();
		w.flush();
		w.close();

	}

	private static int[][] runModel(Graph graph, ArrayList<Integer[]> demands,
			ArrayList<Integer> taxiLocations) {

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

			// Define the objective function
			IloLinearNumExpr obj = model.linearNumExpr();
			for (int i = 0; i < demands.size(); i++) {
				for (int j = 0; j < taxiLocations.size(); j++) {
					int a = demands.get(i)[0];
					int b = taxiLocations.get(j);

					System.out.println("demand: " + a + ", taxi: " + b);

					System.out.println("x[" + i + "][" + j + "] = " + x[i][j]);
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
				print("obj value: " + objValue);

				for (int i = 0; i < demands.size(); i++) {
					for (int j = 0; j < taxiLocations.size(); j++) {
						assignment[i][j] = (int) model.getValue(x[i][j]);
					}
				}

				// to add the distances for demand
				double totalDistForDemand = 0.0;
				for (Integer[] i : demands) {
					int startPoint = i[0];
					int endPoint = i[1];
					totalDistForDemand += getShortestPathDistanceBetweenNodes(graph, startPoint,
							endPoint);
				}
				print("totalDistForDemand: " + totalDistForDemand);
				print("Final final value: " + (totalDistForDemand + objValue));

			} else {
				print("Model not solved :(");
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
			print("No available path between " + start + " and " + end);
		}
		return path;
	}

	private static TreeMap<Integer, Stack<Edge>> mapDikjstra(Graph graph, int startNode) {
		return null;
	}

	private static void addLane(String laneId, int sourceLocNo, int destLocNo, int duration) {
		Edge lane = new Edge(laneId, nodes.get(sourceLocNo), nodes.get(destLocNo), duration);
		edges.add(lane);
	}

	private static void print(String s) {
		System.out.println(s);
		w.println(s);
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
