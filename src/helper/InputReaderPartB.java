package helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class InputReaderPartB {
	// public static void main(String[] args) {
	// InputReaderPartB i = new InputReaderPartB(new Integer[] { 5, 6 });// put an array list of
	// // (N,K) and N < K
	//
	// // get the demands
	// ArrayList<Integer[]> d = i.getDemands();
	//
	// // print out demands
	// Iterator<Integer[]> iter1 = d.iterator();
	// while (iter1.hasNext()) {
	// Integer[] next = iter1.next();
	// System.out.println("O: " + next[0] + " -- D: " + next[1] + " -- Time:" + next[2]);
	// }
	//
	// // get initial taxi locations
	// ArrayList<Integer> t = i.getTaxiLocations();
	//
	// // print out taxi locations
	// Iterator<Integer> iter2 = t.iterator();
	// while (iter2.hasNext()) {
	// Integer next = iter2.next();
	// System.out.println("Taxi location: " + next);
	// }
	// }

	private ArrayList<Integer[]> demands = new ArrayList<>();
	private ArrayList<Integer> taxiLocations = new ArrayList<>();

	// getter
	public ArrayList<Integer[]> getDemands() {
		return demands;
	}

	public ArrayList<Integer> getTaxiLocations() {
		return taxiLocations;
	}

	// constructor
	public InputReaderPartB(String folderPath, Integer[] nK) {
		if (nK[0] >= nK[1]) {
			System.out.println("Not valid N, K. N must lower than K");
			return;
		}

		// String folderPath = "D:\\Dropbox\\SMU\\Year3Sem2\\Enterprise Analytics for Decision
		// Support\\project\\supplementary\\supplementary\\test\\test instances\\instance_b\\";
		// String folderPath = "D:\\Dropbox\\SMU\\Year3Sem2\\Enterprise Analytics for Decision
		// Support\\project\\supplementary\\supplementary\\training\\";

		File f = new File(folderPath + "sin_train_" + nK[0] + "_" + nK[1] + ".txt");
		//		File f = new File(folderPath + "sin_test_" + nK[0] + "_" + nK[1] + ".txt");
		// to read a file
		BufferedReader br = null;

		try {
			br = new BufferedReader(new FileReader(f));

			// read N taxi locations
			for (int i = 0; i < nK[0]; i++) {
				// this array contains 1 integer value: taxi location
				taxiLocations.add(Integer.parseInt(br.readLine()));
			}

			// read K demands
			for (int i = 0; i < nK[1]; i++) {
				String[] line = br.readLine().split(", ");

				// this array contains 3 integer values: origin, destination, time
				Integer[] d = new Integer[3];
				d[0] = Integer.parseInt(line[0]);
				d[1] = Integer.parseInt(line[1]);
				d[2] = Integer.parseInt(line[2]);

				demands.add(d);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
