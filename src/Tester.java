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

import helper.InputReaderPartA;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class Tester {
	static Graph graph;
	static List<Node> nodes = new ArrayList<Node>();
	static List<Edge> edges = new ArrayList<Edge>();

	static ArrayList<Integer[]> demands;
	static ArrayList<Integer> taxiLocations;

	final static String dataFolderPath = "D:\\Dropbox\\SMU\\Year3Sem2\\Enterprise Analytics for Decision Support\\project\\supplementary\\supplementary\\training\\";
	final static int NUM = 5;
	final static String INPUT_FILE = "sin_train_" + NUM + "_" + NUM + ".txt";

	static PrintWriter w;
	static PrintWriter w1;

	static HashMap<Integer, Dijkstra> dijkstraMap = new HashMap<Integer, Dijkstra>();

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

		loadGraph();

		InputReaderPartA partA = new InputReaderPartA(dataFolderPath + INPUT_FILE, NUM);
		demands = partA.getDemands();
		taxiLocations = partA.getTaxiLocations();

		createDijkstraMap();

		int[][] result = runModel();

		// printing for final
		for (int i = 0; i < demands.size(); i++) {
			for (int j = 0; j < taxiLocations.size(); j++) {
				w.print(result[i][j] + " ");
				System.out.print(result[i][j] + " ");

				// printing to final
				if (result[i][j] == 1) {
					int t = taxiLocations.get(j);
					int s = demands.get(i)[0];
					int e = demands.get(i)[1];
					Dijkstra dijkstra = dijkstraMap.get(s);

					Stack<Edge> p = dijkstra.shortestPath(getNode(t));
					int porg = p.size();
					while (!p.isEmpty()) {
						if (p.size() == porg) {
							w1.println("Taxi," + p.remove(0).id);
						} else {
							w1.println("Trans," + p.remove(0).id);
						}
					}

					Stack<Edge> p2 = dijkstra.shortestPath(getNode(e));
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

		Date endTime = new Date();
		System.out.println("run complete");
		print("startTime: " + startTime);
		print("endTime: " + endTime);
		print("duration (min): " + ((endTime.getTime() - startTime.getTime()) / 60000.0));

		w1.flush();
		w1.close();
		w.flush();
		w.close();

	}

	private static int[][] runModel() {

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

			double totalDistForDemand = 0.0; // to add the distances for demand

			// Define the objective function
			IloLinearNumExpr obj = model.linearNumExpr();
			for (int i = 0; i < demands.size(); i++) {
				int startLoc = demands.get(i)[0];
				int descLoc = demands.get(i)[1];

				Dijkstra dijkstra = dijkstraMap.get(startLoc);

				// add to demands distance
				totalDistForDemand += dijkstra.shortestPathWeight(getNode(descLoc));

				for (int j = 0; j < taxiLocations.size(); j++) {
					int b = taxiLocations.get(j);

					System.out.println("demand: " + startLoc + ", taxi: " + b);

					System.out.println("x[" + i + "][" + j + "] = " + x[i][j]);
					obj.addTerm(dijkstra.shortestPathWeight(getNode(b)), x[i][j]);
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
