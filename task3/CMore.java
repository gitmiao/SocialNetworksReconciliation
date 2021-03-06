

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class CMore {

	private final SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	LinkedList<UserRecords> orderByRecordsCount;

	public CMore(final String filename, final boolean isFirstYear) {
		// final Set<String> exclude=exclude();

		final Map<Integer, UserRecords> users = new HashMap<Integer, UserRecords>();
		final Set<String> allLocations = new HashSet<String>();
		BufferedReader reader = null;
		int weekdayCount = 0;
		int weekendCount = 0;
		int[] daySectionCount = new int[3];
		int[] weekendSectionCount = new int[3];
		try {
			final File file = new File(filename);
			reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			while (line != null) {
				if (line.isEmpty()) {
					continue;
				}
				final String[] splits = line.split("\t");
				if (splits.length < 5) {
					// System.out.println(line);
					line = reader.readLine();
					continue;
				}

				// if(exclude.contains(splits[0]))
				// {
				// line = reader.readLine();
				// continue;
				// }

				final int userId = Integer.parseInt(splits[0]);
				final String timeString = splits[1];
				final String formatTimeString = timeString.substring(0, 10)
						+ " " + timeString.substring(11, 19);
				final Date time = sdf.parse(formatTimeString);
				final double latitude = Double.valueOf(splits[2]);
				final double longitude = Double.valueOf(splits[3]);
				final String locationId = splits[4];
				allLocations.add(locationId);
				// if ("00000000000000000000000000000000"
				// .equals(locationId)) {
				// System.out.println(line);
				// }
				final Record record = new Record(userId, time, latitude,
						longitude, locationId, isFirstYear);
				if (record.isWeekday) {
					weekdayCount++;
					daySectionCount[record.daySection]++;
				} else {
					weekendCount++;
					weekendSectionCount[record.daySection]++;
				}
				UserRecords oneUser = users.get(userId);
				if (oneUser == null) {
					oneUser = new UserRecords(userId);
					users.put(userId, oneUser);
				}
				oneUser.addRecord(record, isFirstYear);
				line = reader.readLine();
			}
			orderByRecordsCount = new LinkedList<UserRecords>();
			orderByRecordsCount.addAll(users.values());
			Collections.sort(orderByRecordsCount,
					new Comparator<UserRecords>() {
						@Override
						public int compare(final UserRecords o1,
								final UserRecords o2) {
							return o2.records.size() - o1.records.size();
						}
					});
			final int numOfAllLocations = allLocations.size();
			System.out.println("Number of locations " + numOfAllLocations);
			for (final UserRecords oneUser : orderByRecordsCount) {
				oneUser.allRecordsLoaded(numOfAllLocations);
			}
			//user=users.get(6558);
			//printRecords(users.get(11210));
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
		System.out.println("CMore loaded " + orderByRecordsCount.size()
				+ " weekday count " + weekdayCount + ", weekend count "
				+ weekendCount + ", weekday day section "
				+ Arrays.toString(daySectionCount) + ", weekend day section "
				+ Arrays.toString(weekendSectionCount));
	}

	private void printRecords(final UserRecords records) {
		// System.out.println(records.userId);
		// System.out.println(Arrays.toString(records.weekdayWeekendPer));
		// System.out.println(Arrays.toString(records.weekdayPer));
		// System.out.println(Arrays.toString(records.weekendPer));
		for (final Record record : records.records) {
			System.out.println(records.userId + "," + record.timestamp + ","
					+ record.lat + "," + record.lon + "," + record.locationId);
		}
	}
}
