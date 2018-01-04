package simulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AnswerChecker {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final Map<Integer, Integer> answers = new HashMap<Integer, Integer>();
		BufferedReader reader = null;
		try {
			final File file = new File("T1.txt");
			reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			while (line != null) {
				final String[] splits = line.split(" +");
				final Integer node1 = Integer.valueOf(splits[0]);
				final Integer node2 = Integer.valueOf(splits[1]);
				answers.put(node1, node2);
				line = reader.readLine();
			}
			reader.close();
			reader = new BufferedReader(new FileReader("output.txt"));
			line = reader.readLine();
			int count = 0;
			int correct = 0;
			while (line != null) {
				count++;
				final String[] splits = line.split(" +");
				final Integer node1 = Integer.valueOf(splits[0]);
				final int node2 = Integer.valueOf(splits[1]);
				final int node2Answer = answers.get(node1);
				if (node2 == node2Answer) {
					correct++;
				} else {
					//System.out.println("Wrong match: " + node1 + ": " + node2
					//		+ ", should match to " + node2Answer);
				}
				line = reader.readLine();
			}
			System.out.println("Matched guessed "+count+", correct guess: "+correct);
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
		}
	}

}
