
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class UserRecords {
	final int userId;
	final List<Record> records;
	private final Map<String, Double> byLocation;
	private int total = 0;
	// private double minP;

	private boolean byWeekday;
	private Map<String, Map<Integer, Double>> weekdayLocations;
	private Map<String, Map<Integer, Double>> weekendLocations;
	private int weekdayTotal[];
	private int weekendTotal[];
	double[] weekdayWeekendPer = new double[2];
	double[] weekdayPer = new double[3];
	double[] weekendPer = new double[3];
	double minLat = 1000;
	double maxLat = -1000;
	double minLon = 1000;
	double maxLon = -1000;
	Record firstLastRecord = null;
	Record prevRecord = null;
	double duplicate = 0;

	public UserRecords(final int userId) {
		this.userId = userId;
		records = new LinkedList<Record>();
		byLocation = new HashMap<String, Double>();
		weekdayTotal = new int[3];
		weekendTotal = new int[3];
		weekdayLocations = new HashMap<String, Map<Integer, Double>>();
		weekendLocations = new HashMap<String, Map<Integer, Double>>();
	}

	public void addRecord(final Record record, final boolean isFirstYear) {
		if (firstLastRecord == null) {
			firstLastRecord = record;
		} else {
			if (isFirstYear) {
				if (record.timestamp.after(firstLastRecord.timestamp)) {
					firstLastRecord = record;
				}
			} else {
				if (record.timestamp.before(firstLastRecord.timestamp)) {
					firstLastRecord = record;
				}
			}
		}
		if (prevRecord == null) {
			prevRecord = record;
		} else {
			if (record.locationId.equals(prevRecord.locationId)
					&& record.timestamp.getTime()
							- prevRecord.timestamp.getTime() < 1000 * 60 * 5) {
				duplicate += 1;
			}
		}
		records.add(record);
		final int monthPoint = record.monthPoint;
		total += monthPoint;
		if (record.lat > maxLat) {
			maxLat = record.lat;
		}
		if (record.lat < minLat) {
			minLat = record.lat;
		}
		if (record.lon > maxLon) {
			maxLon = record.lon;
		}
		if (record.lon < minLon) {
			minLon = record.lon;
		}
		Double count = byLocation.get(record.locationId);
		if (count == null) {
			count = 0.0;
		}
		count += monthPoint;
		byLocation.put(record.locationId, count);

		if (record.isWeekday) {
			weekdayTotal[record.daySection] += monthPoint;
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
			ct += monthPoint;
			allTimes.put(record.daySection, ct);
		} else {
			weekendTotal[record.daySection] += monthPoint;
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
			ct += monthPoint;
			allTimes.put(record.daySection, ct);
		}
	}

	public void allRecordsLoaded(final int numberOfAllLocations) {
		// minP = (1.0 / numberOfAllLocations + 1.0 / (10 * total)) / 2;
		duplicate = duplicate / records.size();
		int weekdayT = 0;
		int weekendT = 0;
		for (int i = 0; i < 3; i++) {
			weekdayT += weekdayTotal[i];
			weekendT += weekendTotal[i];
		}
		weekdayWeekendPer[0] = 1.0 * weekdayT / total;
		weekdayWeekendPer[1] = 1.0 * weekendT / total;
		for (int i = 0; i < 3; i++) {
			weekdayPer[i] = 1.0 * weekdayTotal[i] / weekdayT;
			weekendPer[i] = 1.0 * weekendTotal[i] / weekendT;
		}
		if (total >= 200) {
			byWeekday = true;
			byWeekday();
		} else {
			allLocations();
		}
	}

	private void byWeekday() {
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

	private Double getLocationP(final Record record) {
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
			return p;
		} else {
			Double p = byLocation.get(record.locationId);
			return p;
		}
	}

	public double getLocationP(final UserRecords c, final double currentMax) {
		if (!Record.canTravel(firstLastRecord, c.firstLastRecord)) {
			return -1;
		}
		// if(Math.abs(firstLastRecord.timestamp.getTime()-c.firstLastRecord.timestamp.getTime())>maxGapInMilliSec)
		// {
		// return -1;
		// }
		boolean hasOverlap = false;
		final int originSize = c.records.size();
		final int halfSize = originSize / 2;
		int size = originSize;
		double locationP = 0;
		for (final Record record : c.records) {
			final Double p = getLocationP(record);
			if (p == null) {
				size += halfSize;
			} else {
				hasOverlap = true;
				locationP += p;
			}
		}
		if (!hasOverlap) {
			return -1;
		}
		double adjustedP = locationP / size;
		if (adjustedP < currentMax) {
			return adjustedP;
		}
		if (Math.abs(duplicate - c.duplicate) > 0.25) {
			adjustedP *= 0.8;
		}
		if (adjustedP < currentMax) {
			return adjustedP;
		}
		double positionScore = getPositionScore(c);
		if (positionScore > 1000) {
			adjustedP *= 0.8;
		}
		if (adjustedP < currentMax) {
			return adjustedP;
		}
		double dateTimeP=getDateTimeP(c);
		if(dateTimeP>0.75)
		{
			adjustedP *= 0.8;
		}
		return adjustedP;
	}

	public double getDateTimeP(final UserRecords c) {
		if (!Record.canTravel(firstLastRecord, c.firstLastRecord)) {
			return 100;
		}
		double p = 0;
		for (int i = 0; i < 2; i++) {
			double t = (weekdayWeekendPer[i] - c.weekdayWeekendPer[i]);
			p += t * t;
		}
		for (int i = 0; i < 3; i++) {
			double t = (weekdayPer[i] - c.weekdayPer[i]);
			p += t * t;
		}
		for (int i = 0; i < 3; i++) {
			double t = (weekendPer[i] - c.weekendPer[i]);
			p += t * t;
		}
		return p;
	}

	public double getPositionScore(final UserRecords c) {
		if (!Record.canTravel(firstLastRecord, c.firstLastRecord)) {
			return 1000000;
		}
		final UserRecords more;
		final UserRecords less;
		if (records.size() > c.records.size()) {
			more = this;
			less = c;
		} else {
			more = c;
			less = this;
		}
		double total = 0;
		for (final Record record : less.records) {
			final double lat2;
			final double lon2;
			if (record.lat > more.maxLat) {
				lat2 = more.maxLat;
			} else if (record.lat < more.minLat) {
				lat2 = more.minLat;
			} else {
				lat2 = record.lat;
			}
			if (record.lon > more.maxLon) {
				lon2 = more.maxLon;
			} else if (record.lon < more.minLon) {
				lon2 = more.minLon;
			} else {
				lon2 = record.lon;
			}
			total += Record.distance(record.lat, lat2, record.lon, lon2);
		}
		return total / less.records.size();
	}

	private final static long maxGap = 1000L * 60 * 60 * 24;

	public boolean hasEdge(final UserRecords o) {
		final Set<String> locations1 = new HashSet<String>(byLocation.keySet());
		final Set<String> locations2 = new HashSet<String>(
				o.byLocation.keySet());
		locations1.retainAll(locations2);
		if (locations1.isEmpty()) {
			return false;
		}
		for (final Record r1 : records) {
			for (final Record r2 : o.records) {
				if (r1.locationId.equals(r2.locationId)
						&& Math.abs(r1.timestamp.getTime()
								- r2.timestamp.getTime()) < maxGap) {
					return true;
				}
			}
		}
		return false;
	}
}
