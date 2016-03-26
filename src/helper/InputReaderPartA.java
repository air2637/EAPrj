package helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class InputReaderPartA {
	public static void main(String[] args) {
		final String dataFolderPath = "D:\\Dropbox\\SMU\\Year3Sem2\\Enterprise Analytics for Decision Support\\project\\supplementary\\supplementary\\training\\";
		final String INPUT_FILE = "sin_train_5_5.txt";
		// InputReaderPartA i = new InputReaderPartA("inputa.txt");
		InputReaderPartA i = new InputReaderPartA(dataFolderPath + INPUT_FILE, 5);

		// get the demands
		ArrayList<Integer[]> d = i.getDemands();

		// print out demands
		Iterator<Integer[]> iter1 = d.iterator();
		while (iter1.hasNext()) {
			Integer[] next = iter1.next();
			System.out.println("O: " + next[0] + " -- D: " + next[1]);
		}

		// get initial taxi locations
		ArrayList<Integer> t = i.getTaxiLocations();

		// print out taxi locations
		Iterator<Integer> iter2 = t.iterator();
		while (iter2.hasNext()) {
			Integer next = iter2.next();
			System.out.println("Taxi location: " + next);
		}
	}

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
	public InputReaderPartA(String inputFilePath, int num) {
		File f = new File(inputFilePath);
		// to read a file
		BufferedReader br = null;

		try {
			br = new BufferedReader(new FileReader(f));

			// get the number of demands K
			int k = num;
			// get the number of taxis N
			int n = num;

			// read N taxi locations
			for (int i = 0; i < n; i++) {
				// this array contains 1 integer value: taxi location
				taxiLocations.add(Integer.parseInt(br.readLine()));
			}

			// read K demands
			for (int i = 0; i < k; i++) {
				String[] line = br.readLine().split(", ");

				// this integer array contains 2 integer values: origin, destination
				Integer[] d = new Integer[2];
				d[0] = Integer.parseInt(line[0]);
				d[1] = Integer.parseInt(line[1]);

				demands.add(d);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * //constructor public InputReaderPartA(String inputFilePath) { File f = new
	 * File(inputFilePath); //to read a file BufferedReader br = null;
	 * 
	 * try { br = new BufferedReader(new FileReader(f));
	 * 
	 * //get the number of demands K int k = Integer.parseInt(br.readLine()); //get the number of
	 * taxis N int n = Integer.parseInt(br.readLine());
	 * 
	 * //read K demands for(int i = 0; i < k ; i++){ String[] line = br.readLine().split(", ");
	 * 
	 * //this integer array contains 2 integer values: origin, destination Integer[] d = new
	 * Integer[2]; d[0] = Integer.parseInt(line[0]); d[1] = Integer.parseInt(line[1]);
	 * 
	 * demands.add(d); } //read N taxi locations for(int i = 0; i < n ; i++){ //this array contains
	 * 1 integer value: taxi location taxiLocations.add(Integer.parseInt(br.readLine())); } } catch
	 * (IOException e) { e.printStackTrace(); } }
	 */
}
