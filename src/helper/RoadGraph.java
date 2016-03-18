package helper;

import java.io.*;
import java.util.*;

/**
 * Created by khoi on 2/29/2016.
 */
public class RoadGraph {
	public static void main(String[] args) {
		RoadGraph m = new RoadGraph();

		// get the number of vertices
		System.out.println("Number of vertices is " + m.getNumberOfVertices());

		// get the Edge Id of 2 arbitrary vertices
		// If 2 vertices are not connected, return 0
		// return type : integer
		System.out.println("Edge id of connected vertices 0 and 2 is " + m.getEdgeId(0, 2));
		System.out.println("Edge id of unconnected vertices 0 and 1 is " + m.getEdgeId(1, 0));

		// get the Travel Time of 2 arbitrary vertices
		// If 2 vertices are not connected, return 0
		// return type : double
		System.out.println("Travel time of connected vertices 1 and 3 is " + m.getTimeTravel(3, 1));
		System.out.println("Travel time of unconnected vertices 1 and 4 is " + m.getTimeTravel(4, 1));

		// get the adjacent list of connected vertices
		// return type: ArrayList<Integer> of vertexId
		System.out.println("Adjacent list of vertex 2 is " + m.getConnectedVertices(2));
	}

	// edit the file path accordingly
	final String dataFolderPath = "C:\\Users\\superLin\\Dropbox\\Enterprise Analytics\\project\\supplementary\\network\\";
	final String EDGE_INDEX_FILE_NAME = "edge_idx_list.csv";
	final String TRAVEL_TIME_FILE_NAME = "travel_time_list.csv";

	private int NUMBER_OF_VERTICES = 9948;

	public int getNumberOfVertices() {
		return NUMBER_OF_VERTICES;
	}

	public ArrayList<Integer> getConnectedVertices(int vertexId) {
		return connectedVerticesHash.get(vertexId);
	}

	public int getEdgeId(int vertexId1, int vertexId2) {
		// sort the para
		if (vertexId1 > vertexId2) {
			int cache = vertexId1;
			vertexId1 = vertexId2;
			vertexId2 = cache;
		} else if (vertexId1 == vertexId2)
			return 0;

		// return value, if value is null, return 0
		try {
			Integer value = (int) edgeIndexHash.get(vertexId1).get(vertexId2);
			return value;
		} catch (NullPointerException e) {
			return 0;
		}
	}

	public double getTimeTravel(int vertexId1, int vertexId2) {
		// sort the para
		if (vertexId1 > vertexId2) {
			int cache = vertexId1;
			vertexId1 = vertexId2;
			vertexId2 = cache;
		} else if (vertexId1 == vertexId2)
			return 0;

		// return value, if value is null, return 0
		try {
			Double value = (double) travelTimeHash.get(vertexId1).get(vertexId2);
			return value;
		} catch (NullPointerException e) {
			return 0;
		}
	}

	/////////// Ignore this part
	/////////// ////////////////////////////////////////////////////////////////////
	private HashMap<Integer, HashMap<Integer, Object>> edgeIndexHash = new HashMap<>();
	private HashMap<Integer, HashMap<Integer, Object>> travelTimeHash = new HashMap<>();
	private HashMap<Integer, ArrayList<Integer>> connectedVerticesHash = new HashMap<>();

	public RoadGraph() {
		try {
			// put empty ArrayList in the connectedVerticesHash first
			for (int i = 0; i < NUMBER_OF_VERTICES; i++) {
				connectedVerticesHash.put(i, new ArrayList<Integer>());
			}

			File f1 = new File(dataFolderPath + EDGE_INDEX_FILE_NAME);
			File f2 = new File(dataFolderPath + TRAVEL_TIME_FILE_NAME);

			BufferedReader br = null;

			// read edge index file
			br = new BufferedReader(new FileReader(f1));
			readCSV(br, edgeIndexHash, false);

			// read max speed file
			br = new BufferedReader(new FileReader(f2));
			readCSV(br, travelTimeHash, true);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void readCSV(BufferedReader br, HashMap<Integer, HashMap<Integer, Object>> hashMapMatrix,
			boolean valueIsDoubleType) {
		try {
			// skip the header
			br.readLine();

			String currentLine = "";
			while ((currentLine = br.readLine()) != null) {
				String[] s = currentLine.split(",");

				HashMap<Integer, Object> h = hashMapMatrix.get(Integer.parseInt(s[0]));
				if (h == null)
					h = new HashMap<>();

				if (valueIsDoubleType)
					h.put(Integer.parseInt(s[1]), Double.parseDouble(s[2]));
				else {
					//h.put(Integer.parseInt(s[1]), Integer.parseInt(s[2]));
					h.put(Integer.parseInt(s[1]), (int)Double.parseDouble(s[2]));

					// update the connectedVerticesHash
					connectedVerticesHash.get(Integer.parseInt(s[0])).add(Integer.parseInt(s[1]));
					connectedVerticesHash.get(Integer.parseInt(s[1])).add(Integer.parseInt(s[0]));
				}

				hashMapMatrix.put(Integer.parseInt(s[0]), h);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
