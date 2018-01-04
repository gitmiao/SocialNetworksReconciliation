

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Solver {
	private Graph g1;
	private Graph g2;
	private Map<Integer, Integer> mappings;
	private Map<Integer, Integer> pendingToWrite;
	private Set<Integer> g2InMappings;
	private NodePairs nodePairs;
	private int maxD;
	private int T = 1;

	public void solve(final String g1Filename, final String g2Filename,
			final String lFilename) {
		g1 = new Graph(g1Filename);
		g2 = new Graph(g2Filename);
		final Set<Integer> distinctNodes=new HashSet<Integer>(g1.graph.keySet());
		distinctNodes.addAll(g2.graph.keySet());
		g1.computeP(distinctNodes.size());
		g2.computeP(distinctNodes.size());
		final double r = g1.getP() / g2.getP();
		final int maxD1 = g1.getMaxDegree();
		final int maxD2 = g2.getMaxDegree();
		maxD = maxD1 > maxD2 ? maxD1 : maxD2;
		System.out.println("MaxD= " + maxD);
		final int[] degreeDiff = new int[maxD + 1];
		mappings = new HashMap<Integer, Integer>();
		g2InMappings = new HashSet<Integer>();
		BufferedReader reader = null;
		try {
			final File file = new File(lFilename);
			reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			while (line != null) {
				if (line.isEmpty()) {
					continue;
				}
				final String[] splits = line.split(" +");
				// TODO depends on the format of L.txt
				// final Integer node1 = Integer.valueOf(splits[0])+1;
				final Integer node1 = Integer.valueOf(splits[0]);
				final Integer node2 = Integer.valueOf(splits[1]);
				if(g1.contains(node1) && g2.contains(node2))
				{
					final int d1 = g1.getDegree(node1);
					final int d2 = (int) Math.round(g2.getDegree(node2) * r);
					degreeDiff[Math.abs(d1 - d2)]++;
					mappings.put(node1, node2);
					g2InMappings.add(node2);	
				}
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
		}
		System.out.println("Mappings loaded from L "+mappings.size());
		System.out.println(Arrays.toString(degreeDiff));
		final double[] degreeDiffPer = new double[maxD + 1];
		int total = 0;
		for (int i = 0; i < maxD; i++) {
			if (degreeDiff[i] < degreeDiff[i + 1]) {
				// smaller degree diff can't have lower probability
				degreeDiff[i] = degreeDiff[i + 1];
			}
			total += degreeDiff[i];
		}
		total += degreeDiff[maxD];
		System.out.println(Arrays.toString(degreeDiff));
		for (int i = 0; i <= maxD; i++) {
			degreeDiffPer[i] = 1.0 * degreeDiff[i] / total;
		}
		System.out.println(Arrays.toString(degreeDiffPer));
		nodePairs = new NodePairs(maxD, g1, g2, degreeDiffPer);
		nodePairs.initPairs(mappings, g2InMappings);
		pendingToWrite = new HashMap<Integer, Integer>();
		solve();
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

	private void solve() {
		writeResult(mappings, false);
		int maxIter = Integer.MAX_VALUE;
		int count = 0;
		for (int i = (int) (Math.log(maxD) / Math.log(2)); i >= 0; i--) {
			final int minD = (int) Math.pow(2, i);
			oneIter(minD);
			count++;
		}
		boolean b;
		do {
			b = oneIter(1);
			count++;
			if (count % 100 == 0) {
				System.out.println("Matched " + count);
				writeResult(pendingToWrite, true);
				pendingToWrite.clear();
			}
		} while (b && count <= maxIter);
		writeResult(pendingToWrite, true);
		nodePairs.printStats();

	}

	// return true if a new match is found
	private boolean oneIter(final int minD) {
		// System.out.println("one iter");
		if (minD != 1) {
			System.out.println("MinD= " + minD);
		}
		final Set<Integer> g1Nodes = g1.nodesDegreeGreaterThan(minD);
		final Set<Integer> g2Nodes = g2.nodesDegreeGreaterThan(minD);

		NodePair max = nodePairs.getPairToMatch(g1Nodes, g2Nodes, T, -1);
		if (max == null) {
			return false;
		}
		// System.out.println("Match " + max.getNode1() + " " + max.getNode2());
		mappings.put(max.getNode1(), max.getNode2());
		g2InMappings.add(max.getNode2());
		pendingToWrite.put(max.getNode1(), max.getNode2());
		nodePairs.addOneMatch(max.getNode1(), max.getNode2(), mappings,
				g2InMappings);
		return true;
	}
}
