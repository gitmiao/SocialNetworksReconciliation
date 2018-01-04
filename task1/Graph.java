

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Graph {
	final Map<Integer, Set<Integer>> graph;
	// max to min
	private final ArrayList<Integer> nodesOrderByDegree;
	// estimate of p(chance to put an edge in G)*s(chnace to pust an edge in G
	// to this graph)
	private double p;
	int count = 0;

	public Graph(final String inputFile) {
		graph = new HashMap<Integer, Set<Integer>>();
		BufferedReader reader = null;
		try {
			final File file = new File(inputFile);
			reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			while (line != null) {
				if (line.isEmpty()) {
					continue;
				}
				final String[] splits = line.split(" +");
				final Integer node1 = Integer.valueOf(splits[0]);
				final Integer node2 = Integer.valueOf(splits[1]);
				addToMap(node1, node2);
				addToMap(node2, node1);
				count++;
				line = reader.readLine();
			}
			System.out.println("Edges loaded: " + count);
			nodesOrderByDegree = new ArrayList<Integer>(graph.keySet());
			Collections.sort(nodesOrderByDegree, new Comparator<Integer>() {
				@Override
				public int compare(final Integer arg0, final Integer arg1) {
					return Integer.valueOf(graph.get(arg1).size()).compareTo(
							graph.get(arg0).size());
				}

			});
			final int[] degreeDist = new int[getMaxDegree() + 1];
			for (final Set<Integer> node : graph.values()) {
				degreeDist[node.size()]++;
			}
			// System.out.println(Arrays.toString(degreeDist));
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
	
	public void computeP(final int numOfNodes)
	{
		p = 1.0 * count / (numOfNodes * (numOfNodes - 1) / 2);
	}

	private void addToMap(final Integer node1, final Integer node2) {
		Set<Integer> neighbors = graph.get(node1);
		if (neighbors == null) {
			neighbors = new HashSet<Integer>();
			graph.put(node1, neighbors);
		}
		neighbors.add(node2);
	}

	public boolean contains(final Integer node)
	{
		return graph.containsKey(node);
	}
	
	public int getDegree(final Integer node) {
		return graph.get(node).size();
	}

	public int getMaxDegree() {
		return getDegree(nodesOrderByDegree.get(0));
	}

	public Set<Integer> nodesDegreeGreaterThan(final int minDegree) {
		if (minDegree <= getDegree(nodesOrderByDegree.get(nodesOrderByDegree
				.size() - 1))) {
			return graph.keySet();
		}
		if (minDegree > getMaxDegree()) {
			return Collections.emptySet();
		}
		int start = 0;
		int end = nodesOrderByDegree.size() - 1;
		int mid = (start + end) / 2;
		while (start <= end) {
			final int degree = getDegree(nodesOrderByDegree.get(mid));
			if (degree >= minDegree) {
				start = mid + 1;
			} else {
				if (getDegree(nodesOrderByDegree.get(mid - 1)) >= minDegree) {
					break;
				} else {
					end = mid - 1;
				}
			}
			mid = (start + end) / 2;
		}
		return new HashSet<Integer>(nodesOrderByDegree.subList(0, mid + 1));
	}

	public Set<Integer> getNeighbor(final Integer node) {
		return graph.get(node);
	}

	public double getP() {
		return p;
	}
}
