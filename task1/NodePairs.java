

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class NodePairs {

	private final int maxDegree;
	private final ArrayList<Set<NodePair>> orderBySWSize;
	private final Map<NodePair, NodePair> allPairs;
	private final Graph g1;
	private final Graph g2;
	private final double[] degreeDiffPer;
	private final double r;
	private double bestGuessPercentage;

	public NodePairs(final int maxDegree, final Graph g1, final Graph g2,
			final double[] degreeDiffPer) {
		this.maxDegree = maxDegree;
		this.g1 = g1;
		this.g2 = g2;
		r = g1.getP() / g2.getP();
		this.degreeDiffPer = degreeDiffPer;
		allPairs = new HashMap<NodePair, NodePair>();
		orderBySWSize = new ArrayList<Set<NodePair>>(maxDegree);
		// init all size slots (0 is not used)
		for (int i = 0; i <= maxDegree; i++) {
			orderBySWSize.add(new HashSet<NodePair>());
		}
	}

	public void initPairs(final Map<Integer, Integer> mappings,
			final Set<Integer> g2InMappings) {
		final Map<NodePair, NodePair> pairs = new HashMap<NodePair, NodePair>();
		for (final Entry<Integer, Integer> oneMapping : mappings.entrySet()) {
			final Integer node1 = oneMapping.getKey();
			final Integer node2 = oneMapping.getValue();
			final Set<Integer> node1Neighbors = g1.getNeighbor(node1);
			final Set<Integer> node2Neighbors = g2.getNeighbor(node2);
			for (final Integer neighbor1 : node1Neighbors) {
				if (mappings.containsKey(neighbor1)) {
					continue;
				}
				for (final Integer neighbor2 : node2Neighbors) {
					if (g2InMappings.contains(neighbor2)) {
						continue;
					}
					final NodePair pair = new NodePair(neighbor1, neighbor2);
					// System.out.println(pair);
					NodePair existingPair = pairs.get(pair);
					if (existingPair == null) {
						existingPair = pair;
						pairs.put(pair, pair);
					}
					existingPair.getCommonKnownNodes().add(node1);
				}
			}
		}
		for (final NodePair pair : pairs.values()) {
			final int swSize = pair.getCommonKnownNodes().size();
			orderBySWSize.get(swSize).add(pair);
			allPairs.put(pair, pair);
		}
		printStats();
	}

	public void addOneMatch(final int node1, final int node2,
			final Map<Integer, Integer> allMappings,
			final Set<Integer> g2InMappings) {
		// 1 delete existing pairs contains node1 or node2
		final Iterator<NodePair> iter = allPairs.keySet().iterator();
		while (iter.hasNext()) {
			final NodePair pair = iter.next();
			if (pair.getNode1() == node1 || pair.getNode2() == node2) {
				orderBySWSize.get(pair.getCommonKnownNodes().size()).remove(
						pair);
				iter.remove();
			}
		}
		// 2 add node1 node2 neighbors to pairs
		final Set<Integer> node1Neighbors = g1.getNeighbor(node1);
		final Set<Integer> node2Neighbors = g2.getNeighbor(node2);
		for (final Integer neighbor1 : node1Neighbors) {
			if (allMappings.containsKey(neighbor1)) {
				continue;
			}
			for (final Integer neighbor2 : node2Neighbors) {
				if (g2InMappings.contains(neighbor2)) {
					continue;
				}
				final NodePair pair = new NodePair(neighbor1, neighbor2);
				NodePair existingPair = allPairs.get(pair);
				if (existingPair == null) {
					// new pair
					pair.getCommonKnownNodes().add(node1);
					allPairs.put(pair, pair);
					orderBySWSize.get(1).add(pair);
				} else {
					// existing pair
					final int oldSize = existingPair.getCommonKnownNodes()
							.size();
					existingPair.getCommonKnownNodes().add(node1);
					orderBySWSize.get(oldSize).remove(existingPair);
					orderBySWSize.get(oldSize + 1).add(existingPair);
				}
			}
		}
	}

	/**
	 * 
	 * @return null if there is not pair can be found
	 */
	public NodePair getPairToMatch(final Set<Integer> validNode1s,
			final Set<Integer> validNode2s, final int minMatch,
			final double minP) {
		if (allPairs.isEmpty()) {
			return null;
		}
		for (int i = maxDegree; i >= minMatch; i--) {
			final Set<NodePair> pairs = orderBySWSize.get(i);
			if (!pairs.isEmpty()) {
				// key is sw set
				final Map<Set<Integer>, Set<NodePair>> candidates = new HashMap<Set<Integer>, Set<NodePair>>();
				for (final NodePair pair : pairs) {
					if (validNode1s.contains(pair.getNode1())
							&& validNode2s.contains(pair.getNode2())) {
						Set<NodePair> candidatesForOneSW = candidates.get(pair
								.getCommonKnownNodes());
						if (candidatesForOneSW == null) {
							candidatesForOneSW = new HashSet<NodePair>();
							candidates.put(pair.getCommonKnownNodes(),
									candidatesForOneSW);
						}
						candidatesForOneSW.add(pair);
					}
				}
				if (!candidates.isEmpty()) {
					for (final Set<NodePair> oneSW : candidates.values()) {
						if (oneSW.size() == 1) {
							return oneSW.iterator().next();
						}
					}
					final NodePair equivCandidate = processEquivCandidates(
							candidates, minP);
					if (equivCandidate != null) {
						return equivCandidate;
					}
				}
			}
		}
		return null;
	}

	private NodePair processEquivCandidates(
			final Map<Set<Integer>, Set<NodePair>> candidates, final double minP) {
		NodePair bestPair = null;
		double globalBestPercentage = 0;
		for (final Set<NodePair> oneSet : candidates.values()) {
			final Set<Integer> node1s = new HashSet<Integer>();
			final Set<Integer> node2s = new HashSet<Integer>();
			for (final NodePair pair : oneSet) {
				node1s.add(pair.getNode1());
				node2s.add(pair.getNode2());
			}
			final NodePair bestThisSet = getBestMatch(new ArrayList<Integer>(
					node1s), new ArrayList<Integer>(node2s));
			if (bestThisSet != null
					&& bestGuessPercentage > globalBestPercentage) {
				globalBestPercentage = bestGuessPercentage;
				bestPair = bestThisSet;
			}
		}
		if (globalBestPercentage > minP) {
			// System.out.println("Best guess "+bestPair+" percentage= "+globalBestPercentage);
			return bestPair;
		}
		return null;
	}

	private NodePair getBestMatch(final List<Integer> node1s,
			final List<Integer> node2s) {
		final int[] node1Degrees = new int[node1s.size()];
		int i = 0;
		for (final Integer node1 : node1s) {
			node1Degrees[i++] = g1.getDegree(node1);
		}
		i = 0;
		final int[] node2Degrees = new int[node2s.size()];
		for (final Integer node2 : node2s) {
			node2Degrees[i++] = (int) Math.round(g2.getDegree(node2) * r);
		}
		final double[] node1Totals = new double[node1s.size()];
		final double[] node2Totals = new double[node2s.size()];
		final double[][] percentages = new double[node1s.size()][node2s.size()];
		i = 0;
		for (i = 0; i < node1s.size(); i++) {
			for (int j = 0; j < node2s.size(); j++) {
				final int degreeDiff = Math.abs(node1Degrees[i]
						- node2Degrees[j]);
				final double percentage;
				if (degreeDiff < degreeDiffPer.length) {
					percentage = degreeDiffPer[degreeDiff];
				} else {
					percentage = 0;
				}
				node1Totals[i] += percentage;
				node2Totals[j] += percentage;
				percentages[i][j] = percentage;
			}
		}
		NodePair bestPair = null;
		bestGuessPercentage = 0;
		for (i = 0; i < node1s.size(); i++) {
			for (int j = 0; j < node2s.size(); j++) {
				final double per = Math.max(percentages[i][j] / node1Totals[i],
						percentages[i][j] / node2Totals[j]);
				// final double
				// per=(percentages[i][j]/node1Totals[i]+percentages[i][j]/node2Totals[j])/2;
				if (per > bestGuessPercentage) {
					bestGuessPercentage = per;
					bestPair = new NodePair(node1s.get(i), node2s.get(j));
				}
			}
		}
		return bestPair;
	}

	public void printStats() {
		for (int i = maxDegree; i >= 0; i--) {
			System.out.println("SW size " + i + ", pairs: "
					+ orderBySWSize.get(i).size());
		}
	}
}
