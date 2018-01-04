

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class N1UserRecords {
	final List<Record> records;
	Date minTime;
	Date maxTime;

	public N1UserRecords() {
		records = new LinkedList<Record>();
	}

	public void addRecord(final Record record) {
		records.add(record);
		if (minTime == null || record.timestamp.before(minTime)) {
			minTime = record.timestamp;
		}
		if (maxTime == null || record.timestamp.after(maxTime)) {
			maxTime = record.timestamp;
		}
	}
}
