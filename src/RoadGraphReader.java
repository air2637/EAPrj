import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

public class RoadGraphReader {
	// edit the file path accordingly
	final String dataFolderPath = "D:\\Dropbox\\SMU\\Year3Sem2\\Enterprise Analytics for Decision Support\\project\\supplementary\\supplementary\\network\\";
	final String EDGE_INDEX_FILE_NAME = "edge_idx_list.csv";
	final String TRAVEL_TIME_FILE_NAME = "travel_time_list.csv";

	// private int NUMBER_OF_VERTICES = 9948;
	private HashMap<String, Edge> loadedEdges;

	public RoadGraphReader() {
		try {

			File f1 = new File(dataFolderPath + EDGE_INDEX_FILE_NAME);
			File f2 = new File(dataFolderPath + TRAVEL_TIME_FILE_NAME);

			BufferedReader br1 = null;
			BufferedReader br2 = null;

			// read edge index file
			br1 = new BufferedReader(new FileReader(f1));

			// read max speed file
			br2 = new BufferedReader(new FileReader(f2));
			loadedEdges = new HashMap<String, Edge>();
			readCSV(br1, br2, loadedEdges);

			br1.close();
			br2.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void readCSV(BufferedReader br1, BufferedReader br2,
			HashMap<String, Edge> loadedEdges) {
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
				Node n1 = new Node(s1[0]);
				Node n2 = new Node(s1[1]);

				String[] tmp = s1[2].split("\\.");
				String edgeId = tmp[0];
				loadedEdges.put(edgeId, new Edge(edgeId, n1, n2, weight));

			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public HashMap<String, Edge> getLoadedEdges() {
		return this.loadedEdges;
	}
}
