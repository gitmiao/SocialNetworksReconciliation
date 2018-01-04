package simulator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class DataGenerator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BufferedReader reader = null;
		BufferedWriter writer1 = null;
		BufferedWriter writer2 = null;
		BufferedWriter writer3 = null;
		final Random rnd = new Random();
		final int n = 10;
		try {
			final File file = new File("Brightkite.txt");
			reader = new BufferedReader(new FileReader(file));
			writer1 = new BufferedWriter(new FileWriter("N1_Test.txt"));
			writer2 = new BufferedWriter(new FileWriter("N2_Test.txt"));
			writer3 = new BufferedWriter(new FileWriter("T2_Test.txt"));
			String line = reader.readLine();
			String prevUser = null;
			List<String> lines = new LinkedList<String>();
			while (line != null) {
				if (line.isEmpty()) {
					continue;
				}
				final String[] splits = line.split("\t");
				final String userId = splits[0];
				if (splits.length < 5) {
					System.out.println(line);
				}
				if (prevUser != null && !prevUser.equals(userId)) {
					if (lines.size() > n) {
						for (int i = 1; i <= n; i++) {
							final String n1Line = lines.remove(rnd
									.nextInt(lines.size()));
							writer1.write(n1Line);
							writer1.newLine();
							// final String locationId = n1Line.split("\t")[4];
							// if ("00000000000000000000000000000000"
							// .equals(locationId)) {
							// System.out.println(n1Line);
							// }
						}
						for (final String n2Line : lines) {
							writer2.write(n2Line);
							writer2.newLine();
							// final String locationId = n2Line.split("\t")[4];
							// if ("00000000000000000000000000000000"
							// .equals(locationId)) {
							// System.out.println(n2Line);
							// }
						}
						writer3.write(prevUser + " " + prevUser);
						writer3.newLine();
					}
					lines.clear();
				} else {
					lines.add(line);
				}
				prevUser = userId;
				line = reader.readLine();
			}
		} catch (final Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (writer1 != null) {
				try {
					writer1.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (writer2 != null) {
				try {
					writer2.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (writer3 != null) {
				try {
					writer3.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
