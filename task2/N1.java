

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
//import java.util.List;
import java.util.Map;

public class N1 {
	private final SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	Map<Integer,N1UserRecords> users;

	public N1(final String filename) {
		users = new HashMap<Integer,N1UserRecords>();
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
				final int userId = Integer.parseInt(splits[0]);
				final String timeString = splits[1];
				final String formatTimeString = timeString.substring(0, 10)
						+ " " + timeString.substring(11, 19);
				final Date time = sdf.parse(formatTimeString);
				final double latitude = Double.valueOf(splits[2]);
				final double longitude = Double.valueOf(splits[3]);
				final String locationId = splits[4];
				// if ("00000000000000000000000000000000"
				// .equals(locationId)) {
				// System.out.println(line);
				// }
				final Record record = new Record(userId, time, latitude,
						longitude, locationId);
				if (record.isWeekday) {
					weekdayCount++;
					daySectionCount[record.daySection]++;
				} else {
					weekendCount++;
					weekendSectionCount[record.daySection]++;
				}
				N1UserRecords oneUser = users.get(userId);
				if (oneUser == null) {
					oneUser = new N1UserRecords();
					users.put(userId, oneUser);
				}
				oneUser.addRecord(record);
				line = reader.readLine();
			}
			//printRecords(users.get(44907));
			//printRecords(users.get(25269));
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
		System.out.println("N1 loaded " + users.size() + " weekday count "
				+ weekdayCount + ", weekend count " + weekendCount
				+ ", weekday day section " + Arrays.toString(daySectionCount)
				+ ", weekend day section " + Arrays.toString(weekendSectionCount));
	}
	
	//private void printRecords(final List<Record> records) {
	//	for (final Record record : records) {
	//		System.out.println(record.userId + "," + record.timestamp + ","
	//				+ record.lat + "," + record.lon + "," + record.locationId);
	//	}
	//}
}
