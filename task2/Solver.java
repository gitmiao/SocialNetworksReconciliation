

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class Solver {
	private N1 n1;
	private N2 n2;
	private Map<Integer, Integer> mappings;
	int count = 0;
	boolean append = false;

	public void solve(final String n1Filename, final String n2Filename) {
		n1 = new N1(n1Filename);
		n2 = new N2(n2Filename);
		mappings = new HashMap<Integer, Integer>();

		double p = 0.9;
		for (int i = 0; i < 20; i++) {
			oneRun(p);
			p /= 10;
		}
		oneRun(-100);
		writeResult(mappings, append);
	}

	private void oneRun(final double minP) {
		int processCount = 0;
		System.out.println("Min P= " + minP);
		Iterator<N2UserRecords> n2Iter = n2.orderByRecordsCount.iterator();
		while (n2Iter.hasNext()) {
			final N2UserRecords n2User = n2Iter.next();
			final boolean matched = findOneMatch(n2User, minP);
			processCount++;
			if (processCount % 500 == 0) {
				System.out.println("Processed " + processCount
						+ ", records in N2 " + n2User.records.size()
						+ ", total matched " + count);
			}
			if (matched) {
				count++;
				if (count % 100 == 0) {
					// System.out.println("Matched " + count);
					writeResult(mappings, append);
					append = true;
					mappings.clear();
				}
				n2Iter.remove();
			}
		}
		System.out.println("Matches found: " + count);
	}

	private void writeResult(final Map<Integer, Integer> toWrite,
			final boolean append) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter("output.txt", append));
			for (final Entry<Integer, Integer> mapping : toWrite.entrySet()) {
				writer.write(mapping.getKey() + " " + mapping.getValue());
				writer.newLine();
			}
		} catch (final Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private boolean findOneMatch(final N2UserRecords n2User, final double minP) {
		// System.out.println(n2User.records.size());
		int maxN1 = -1;
		double maxP = minP;
		int countOfOne = 0;
		for (final Entry<Integer, N1UserRecords> n1User : n1.users.entrySet()) {
			final double p = n2User.getP(n1User.getValue(), maxP);
			if (p > maxP) {
				maxP = p;
				maxN1 = n1User.getKey();
			}
			if (1 - p <= 1e-10) {
				countOfOne++;
			}
		}
		if (maxN1 == -1) {
			return false;
		}
		if (minP > 0 && countOfOne > 1) {
			return false;
		}
		// System.out.println("Match "+maxN1+" "+n2User.userId+", maxP "+maxP);
		n1.users.remove(maxN1);
		mappings.put(maxN1, n2User.userId);
		return true;
	}
}
