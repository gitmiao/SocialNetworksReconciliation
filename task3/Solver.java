import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Solver {
	private CMore cMore;
	private CLess cLess;
	// key is id in cMore, value is id in cLess
	private Map<Integer, Integer> mappings;
	int count = 0;
	boolean append = false;
	private boolean isMoreFirst;
	private Set<Integer> noOverlapCMoreUsers;

	public void solve(final String c1Filename, final String c2Filename) {
		System.out.println(new Date());
		final File c1File = new File(c1Filename);
		final File c2File = new File(c2Filename);
		if (c1File.length() > c2File.length()) {
			cMore = new CMore(c1Filename, true);
			cLess = new CLess(c2Filename, false);
			isMoreFirst = true;
		} else {
			cMore = new CMore(c2Filename, false);
			cLess = new CLess(c1Filename, true);
			isMoreFirst = false;
		}
		mappings = new HashMap<Integer, Integer>();
		noOverlapCMoreUsers = new HashSet<Integer>();

		oneRunByLocation(0.99, 60, Integer.MAX_VALUE);

		oneRunByLocation(0.5, 60, Integer.MAX_VALUE);
		oneRunByLocation(0.99, 0, 60);
		oneRunByLocation(0.32, 60, Integer.MAX_VALUE);
		oneRunByLocation(0.5, 0, 60);
		double p = 0.32;
		oneRunByLocation(p, 60, Integer.MAX_VALUE);
		for (int i = 1; i <= 40; i++) {
			oneRunByLocation(p, 0, 60);
			p /= 2;
			oneRunByLocation(p, 60, Integer.MAX_VALUE);
		}

		for (int i = 1; i <= 30; i++) {
			double maxScore = 1.5;
			int newMatch = oneRunByScore(maxScore);
			for (int j = 1; j < 4 && newMatch < 2; j++) {
				maxScore *= 2;
				newMatch = oneRunByScore(maxScore);
			}
		}
		oneRunByScore(null);
		writeResult(mappings, append);
		System.out.println(new Date());
	}

	private void oneRunByLocation(final double minP, final int minRecords,
			final int maxRecord) {
		int processCount = 0;
		System.out.println("By Location, Min P= " + minP + ", min records= "
				+ minRecords);
		Iterator<UserRecords> cMoreIter = cMore.orderByRecordsCount.iterator();
		while (cMoreIter.hasNext()) {
			final UserRecords cMoreUser = cMoreIter.next();
			if (cMoreUser.records.size() >= maxRecord) {
				continue;
			}
			if (cMoreUser.records.size() < minRecords) {
				break;
			}
			final boolean matched = findOneMatchByLocation(cMoreUser, minP);
			processCount++;
			if (processCount % 500 == 0) {
				System.out.println("Processed " + processCount
						+ " records in CMore " + cMoreUser.records.size()
						+ ", total matched " + count);
			}
			if (matched) {
				count++;
				if (count % 100 == 0) {
					// System.out.println("Matched " + count);
					writeResult(mappings, append);
					// writeTask1L(mappings, "L_Test.txt", append);
					append = true;
					mappings.clear();
				}
				cMoreIter.remove();
			}
		}
		System.out.println("Matches found: " + count
				+ ", no overlap nodes count " + noOverlapCMoreUsers.size());
	}

	private int oneRunByScore(final Double maxRank) {
		int newMatch = 0;
		int processCount = 0;
		System.out.println("By DateTime, Max R= " + maxRank);
		Iterator<UserRecords> cMoreIter = cMore.orderByRecordsCount.iterator();
		while (cMoreIter.hasNext()) {
			final UserRecords cMoreUser = cMoreIter.next();
			final boolean matched = findOneMatchByRank(cMoreUser, maxRank);
			processCount++;
			if (processCount % 500 == 0) {
				System.out.println("Processed " + processCount
						+ " records in CMore " + cMoreUser.records.size()
						+ ", total matched " + count);
			}
			if (matched) {
				newMatch++;
				count++;
				if (count % 200 == 0) {
					// System.out.println("Matched " + count);
					writeResult(mappings, append);
					append = true;
					mappings.clear();
				}
				cMoreIter.remove();
			}
		}
		System.out.println("Matches found: " + count);
		return newMatch;
	}

	private void writeResult(final Map<Integer, Integer> toWrite,
			final boolean append) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter("output.txt", append));
			for (final Entry<Integer, Integer> mapping : toWrite.entrySet()) {
				if (isMoreFirst) {
					writer.write(mapping.getKey() + " " + mapping.getValue());
				} else {
					writer.write(mapping.getValue() + " " + mapping.getKey());
				}
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

	private boolean findOneMatchByLocation(final UserRecords cMoreUser,
			final double minP) {
		if (minP > 0 && noOverlapCMoreUsers.contains(cMoreUser.userId)) {
			return false;
		}
		// System.out.println(n2User.records.size());
		int maxCLess = -1;
		double maxP = minP;
		int countOfOne = 0;
		boolean hasOverlap = false;
		for (final Entry<Integer, UserRecords> cLessUser : cLess.users
				.entrySet()) {
			final double p1 = cMoreUser
					.getLocationP(cLessUser.getValue(), maxP);
			if (p1 >= 0) {
				hasOverlap = true;
			}
			if (p1 < maxP) {
				continue;
			}
			final double p2 = cLessUser.getValue()
					.getLocationP(cMoreUser, maxP);
			final double p = Math.min(p1, p2);
			if (p > maxP) {
				maxP = p;
				maxCLess = cLessUser.getKey();
			}
			if (1 - p <= 1e-10) {
				countOfOne++;
			}
		}
		if (!hasOverlap) {
			noOverlapCMoreUsers.add(cMoreUser.userId);
		}
		if (maxCLess == -1) {
			return false;
		}
		if (minP > 0 && countOfOne > 1) {
			return false;
		}
		// System.out.println("Match "+cMoreUser.userId+" "+maxCLess+", maxP "+maxP);
		cLess.users.remove(maxCLess);
		mappings.put(cMoreUser.userId, maxCLess);
		return true;
	}

	private boolean findOneMatchByRank(final UserRecords cMoreUser,
			final Double maxRank) {
		final List<Score<Integer>> timeScores = new ArrayList<Score<Integer>>(
				cLess.users.size());
		final List<Score<Integer>> positionScores = new ArrayList<Score<Integer>>(
				cLess.users.size());
		final List<Score<Integer>> duplicateScores = new ArrayList<Score<Integer>>(
				cLess.users.size());
		for (final Entry<Integer, UserRecords> cLessUser : cLess.users
				.entrySet()) {
			final double timeScore = cMoreUser.getDateTimeP(cLessUser
					.getValue());
			timeScores.add(new Score<Integer>(cLessUser.getKey(),
					(int) (100 * timeScore)));
			final double positionScore = cMoreUser.getPositionScore(cLessUser
					.getValue());
			positionScores.add(new Score<Integer>(cLessUser.getKey(),
					(int) positionScore));
			duplicateScores.add(new Score<Integer>(cLessUser.getKey(),
					(int) (100 * Math.abs(cMoreUser.duplicate
							- cLessUser.getValue().duplicate))));
		}
		Collections.sort(timeScores);
		Collections.sort(positionScores);
		Collections.sort(duplicateScores);
		final Map<Integer, Double> ranksMap = new HashMap<Integer, Double>();
		Integer prevScore = Integer.MIN_VALUE;
		int rank = 0;
		for (final Score<Integer> timeScore : timeScores) {
			final int score = timeScore.score;
			if (score > prevScore) {
				rank++;
			}
			ranksMap.put(timeScore.userId, 0.33 * rank);
			prevScore = score;
		}
		prevScore = Integer.MIN_VALUE;
		rank = 0;
		for (final Score<Integer> positionScore : positionScores) {
			final int score = positionScore.score;
			if (score > prevScore) {
				rank++;
			}
			ranksMap.put(positionScore.userId,
					0.33 * rank + ranksMap.get(positionScore.userId));
			prevScore = score;
		}
		prevScore = Integer.MIN_VALUE;
		rank = 0;
		for (final Score<Integer> duplicateScore : duplicateScores) {
			final int score = duplicateScore.score;
			if (score > prevScore) {
				rank++;
			}
			ranksMap.put(duplicateScore.userId,
					0.33 * rank + ranksMap.get(duplicateScore.userId));
			prevScore = score;
		}
		final List<Score<Double>> finalRanks = new ArrayList<Score<Double>>(
				cLess.users.size());
		for (final Entry<Integer, Double> r : ranksMap.entrySet()) {
			finalRanks.add(new Score<Double>(r.getKey(), r.getValue()));
		}
		Collections.sort(finalRanks);
		final int maxCLess = finalRanks.get(0).userId;
		final double score = finalRanks.get(0).score;
		if (maxRank != null) {
			if (score > maxRank) {
				return false;
			}
			if (finalRanks.size() >= 2) {
				final double secondScore = finalRanks.get(1).score;
				if (score == secondScore) {
					return false;
				}
			}
		}
		// System.out.println("Match " + cMoreUser.userId + " " + maxCLess
		// + ", maxP " + maxP);
		cLess.users.remove(maxCLess);
		mappings.put(cMoreUser.userId, maxCLess);
		return true;
	}

	private static final class Score<T extends Comparable<T>> implements
			Comparable<Score<T>> {
		final int userId;
		final T score;

		private Score(final int userId, final T score) {
			super();
			this.userId = userId;
			this.score = score;
		}

		@Override
		public int compareTo(Score<T> o) {
			return score.compareTo(o.score);
		}
	}
}
