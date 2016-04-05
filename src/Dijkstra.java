import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

class Vertex implements Comparable<Vertex> {
	public final String name;

	// it stores target vertex and corresponding edges
	public HashMap<Vertex, Edge> adjacencies = new HashMap<Vertex, Edge>();

	public double minDistance = Double.POSITIVE_INFINITY;
	public Vertex previous;

	public Vertex(String argName) {
		name = argName;
	}

	public String toString() {
		return name;
	}

	public int compareTo(Vertex other) {
		return Double.compare(minDistance, other.minDistance);
	}

}

class Edge {
	public final Vertex target;
	public final double weight;
	public final String id;

	public Edge(Vertex argTarget, double argWeight, String edgeId) {
		target = argTarget;
		weight = argWeight;
		id = edgeId;
	}

	public String toString() {
		return id;
	}
}

public class Dijkstra {
	private Vertex[] vertices;
	private HashMap<String, Vertex> verticesHashMap;

	public Dijkstra() {
		GraphLoader gl = new GraphLoader();
		verticesHashMap = gl.loadGraph();
		vertices = verticesHashMap.values().toArray(new Vertex[0]);
	}

	public void computePaths(Vertex source) {

		source.minDistance = 0.;
		PriorityQueue<Vertex> vertexQueue = new PriorityQueue<Vertex>();
		vertexQueue.add(source);

		while (!vertexQueue.isEmpty()) {
			Vertex u = vertexQueue.poll();

			// Visit each edge exiting u
			for (Edge e : u.adjacencies.values().toArray(new Edge[0])) {
				Vertex v = e.target;
				double weight = e.weight;
				double distanceThroughU = u.minDistance + weight;
				if (distanceThroughU < v.minDistance) {
					vertexQueue.remove(v);

					v.minDistance = distanceThroughU;
					v.previous = u;
					vertexQueue.add(v);
				}
			}
		}
	}

	public void computePaths(String source) {
		Vertex v_start = verticesHashMap.get(source);
		computePaths(v_start);
	}

	public double getShortestDistanceTo(Vertex target, Vertex[] vertices) {

		for (Vertex v : vertices) {
			if (v.equals(target)) {
				return v.minDistance;
			}

		}
		return -1;
	}

	public double getShortestDistanceTo(String target) {
		Vertex v_end = verticesHashMap.get(target);
		return getShortestDistanceTo(v_end, vertices);
	}

	public List<Edge> getShortestPathTo(Vertex target) {
		List<Edge> path = new ArrayList<Edge>();

		List<Vertex> visitedVertex = new ArrayList<Vertex>();

		boolean notEnd = true;
		while (notEnd) {
			Vertex vertex = target.previous;
			if (vertex == null) {
				break;
			}
			if (visitedVertex.contains(vertex)) {
				notEnd = false;
			} else {
				visitedVertex.add(vertex);
				Edge tmpEdge = target.adjacencies.get(vertex);
				path.add(tmpEdge);
				target = vertex;
			}
		}

		Collections.reverse(path);
		return path;
	}

	public List<Edge> getShortestPathTo(String target) {
		Vertex v_end = verticesHashMap.get(target);
		return getShortestPathTo(v_end);
	}

}

class GraphLoader {
	final String dataFolderPath = "D:\\Dropbox\\SMU\\Year3Sem2\\Enterprise Analytics for Decision Support\\project\\supplementary\\supplementary\\network\\";
	final String EDGE_INDEX_FILE_NAME = "edge_idx_list.csv";
	final String TRAVEL_TIME_FILE_NAME = "travel_time_list.csv";

	HashMap<String, Vertex> verticesHashMap = new HashMap<String, Vertex>();

	public HashMap<String, Vertex> loadGraph() {
		try {
			File f1 = new File(dataFolderPath + EDGE_INDEX_FILE_NAME);
			File f2 = new File(dataFolderPath + TRAVEL_TIME_FILE_NAME);

			BufferedReader br1 = null;
			BufferedReader br2 = null;

			// read nodes-edge index file
			br1 = new BufferedReader(new FileReader(f1));

			// read nodes-time file
			br2 = new BufferedReader(new FileReader(f2));

			readCSV(br1, br2);

			br1.close();
			br2.close();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			return verticesHashMap;
		}

	}

	private void readCSV(BufferedReader br1, BufferedReader br2) {
		// TODO Auto-generated method stub
		try {
			// skip header
			br1.readLine();
			br2.readLine();
			String currentLine1 = "";
			String currentLine2 = "";

			while ((currentLine1 = br1.readLine()) != null
					&& (currentLine2 = br2.readLine()) != null) {

				String[] s1 = currentLine1.split(",");
				String[] s2 = currentLine2.split(",");
				// is reading weight
				double weight = Double.parseDouble(s2[2]);

				// is reading edge indexes
				Vertex v1 = null, v2 = null;
				if (verticesHashMap.keySet().contains(s1[0])) {
					v1 = verticesHashMap.get(s1[0]);
				} else {
					v1 = new Vertex(s1[0]);

					verticesHashMap.put(s1[0], v1);
				}
				if (verticesHashMap.keySet().contains(s1[1])) {
					v2 = verticesHashMap.get(s1[1]);
				} else {
					v2 = new Vertex(s1[1]);
					verticesHashMap.put(s1[1], v2);
				}

				String[] tmp = s1[2].split("\\."); // edgeId
				String edgeId = tmp[0];

				v1.adjacencies.put(v2, new Edge(v2, weight, edgeId));
				v2.adjacencies.put(v1, new Edge(v1, weight, edgeId));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
