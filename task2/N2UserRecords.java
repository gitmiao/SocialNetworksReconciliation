

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class N2UserRecords {
	final int userId;
	final List<Record> records;
	private final Map<String, Double> byLocation;
	private int total;
	private double minP;

	private boolean byWeekday;
	private Map<String, Map<Integer, Double>> weekdayLocations;
	private Map<String, Map<Integer, Double>> weekendLocations;
	private int weekdayTotal[] = new int[3];
	private int weekendTotal[] = new int[3];
	private Date minTime;
	private Date maxTime;

	public N2UserRecords(final int userId) {
		this.userId = userId;
		records = new LinkedList<Record>();
		byLocation = new HashMap<String, Double>();
		weekdayLocations = new HashMap<String, Map<Integer, Double>>();
		weekendLocations = new HashMap<String, Map<Integer, Double>>();
	}

	public void addRecord(final Record record) {
		records.add(record);
		if (minTime == null || record.timestamp.before(minTime)) {
			minTime = record.timestamp;
		}
		if (maxTime == null || record.timestamp.after(maxTime)) {
			maxTime = record.timestamp;
		}
		Double count = byLocation.get(record.locationId);
		if (count == null) {
			count = 0.0;
		}
		count += 1;
		byLocation.put(record.locationId, count);
		if (record.isWeekday) {
			weekdayTotal[record.daySection]++;
			Map<Integer, Double> allTimes = weekdayLocations
					.get(record.locationId);
			if (allTimes == null) {
				allTimes = new HashMap<Integer, Double>();
				weekdayLocations.put(record.locationId, allTimes);
			}
			Double ct = allTimes.get(record.daySection);
			if (ct == null) {
				ct = 0.0;
			}
			ct += 1;
			allTimes.put(record.daySection, ct);
		} else {
			weekendTotal[record.daySection]++;
			Map<Integer, Double> allTimes = weekendLocations
					.get(record.locationId);
			if (allTimes == null) {
				allTimes = new HashMap<Integer, Double>();
				weekendLocations.put(record.locationId, allTimes);
			}
			Double ct = allTimes.get(record.daySection);
			if (ct == null) {
				ct = 0.0;
			}
			ct += 1;
			allTimes.put(record.daySection, ct);
		}
	}

	public void allRecordsLoaded(final int numberOfAllLocations) {
		total = records.size();
		//minP = 1.0 / numberOfAllLocations;
		minP = (1.0 / numberOfAllLocations + 1.0 / (10 * total)) / 2;
		if (total >= 200) {
			byWeekday = true;
			byWeekday();
		} else {
			allLocations();
		}
	}

	private void byWeekday() {
		byLocation.clear();
		for (final Entry<String, Map<Integer, Double>> oneLocation : weekdayLocations
				.entrySet()) {
			for (final Entry<Integer, Double> oneDaySection : oneLocation
					.getValue().entrySet()) {
				oneDaySection.setValue(oneDaySection.getValue()
						/ weekdayTotal[oneDaySection.getKey()]);
			}
		}
		for (final Entry<String, Map<Integer, Double>> oneLocation : weekendLocations
				.entrySet()) {
			for (final Entry<Integer, Double> oneDaySection : oneLocation
					.getValue().entrySet()) {
				oneDaySection.setValue(oneDaySection.getValue()
						/ weekendTotal[oneDaySection.getKey()]);
			}
		}
	}

	private void allLocations() {
		for (final Entry<String, Double> oneLocation : byLocation.entrySet()) {
			oneLocation.setValue(oneLocation.getValue() / total);
		}
	}

	public double getLocationP(final Record record) {
		if (byWeekday) {
			Double p = null;
			if (record.isWeekday) {
				final Map<Integer, Double> oneLocation = weekdayLocations
						.get(record.locationId);
				if (oneLocation != null) {
					p = oneLocation.get(record.daySection);
				}
			} else {
				final Map<Integer, Double> oneLocation = weekendLocations
						.get(record.locationId);
				if (oneLocation != null) {
					p = oneLocation.get(record.daySection);
				}
			}
			return p == null ? minP : p;
		} else {
			Double p = byLocation.get(record.locationId);
			return p == null ? minP : p;
		}
	}

	public double getP(final N1UserRecords n1, final double currentMax) {
		double locationP = 1;
		for (final Record record : n1.records) {
			locationP *= getLocationP(record);
			if (locationP <= currentMax) {
				return locationP;
			}
		}
		final double afterTimeP = applyDateP(n1, locationP, currentMax);
		return afterTimeP;
	}

	public double applyDateP(final N1UserRecords n1, final double locationP,
			final double currentMax) {
		final double p1;
		int withinRange;
		int moreThanMax = 0;
		int lessThanMin = 0;
		final Date min;
		final Date max;
		final List<Record> lessRecords;
		final List<Record> moreRecords;
		final Date lessRecordsMin;
		final Date lessRecordsMax;
		if (n1.records.size() < records.size()) {
			lessRecords = n1.records;
			moreRecords = records;
			min = minTime;
			max = maxTime;
			lessRecordsMin = n1.minTime;
			lessRecordsMax = n1.maxTime;
			withinRange = records.size();
		} else {
			lessRecords = records;
			moreRecords = n1.records;
			min = n1.minTime;
			max = n1.maxTime;
			lessRecordsMin = minTime;
			lessRecordsMax = maxTime;
			withinRange = n1.records.size();
		}
		if (!lessRecordsMin.before(min) && !lessRecordsMax.after(max)) {
			p1 = 1;
		} else {
			for (final Record record : lessRecords) {
				if (record.timestamp.after(max)) {
					moreThanMax++;
				} else if (record.timestamp.before(min)) {
					lessThanMin++;
				} else {
					withinRange++;
				}
			}
			final int total = withinRange + moreThanMax + lessThanMin;
			p1 = Math.pow(1.0 * moreThanMax / total, moreThanMax)
					* Math.pow(1.0 * lessThanMin / total, lessThanMin)
					* Math.pow(1.0 * withinRange / total, withinRange);
		}
		if (lessRecords.size() < 5) {
			return p1 * locationP;
		}
		final double maxFinalP = (p1 + 1) / 2 * locationP;
		if (maxFinalP <= currentMax) {
			return maxFinalP;
		}
		int count = 0;
		for (final Record record : moreRecords) {
			if (!record.timestamp.before(lessRecordsMin)
					&& !record.timestamp.after(lessRecordsMax)) {
				count++;
			}
		}
		final double p2 = 1.0 * count / moreRecords.size();
		return locationP * ((p1 + p2) / 2);
	}
}
