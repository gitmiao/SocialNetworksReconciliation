
import java.util.Calendar;
import java.util.Date;

public class Record {
	private static final long TEN_HOURS = 10 * 60 * 60 * 1000;
	private static final double MAX_DIST_MILISEC = 50.0 / 3600000;
	// final int userId;
	final Date timestamp;
	// we are not using the lat lon, so don't keep it to save space
	final double lat;
	final double lon;
	final String locationId;
	final boolean isWeekday;
	// 0: 11pm to 8am; 1: 8am to 6pm; 2: 6pm - 23pm
	final int daySection;
	final int monthPoint;

	public Record(final int userId, final Date timestamp, final double lat,
			final double lon, final String locationId, final boolean isFirstYear) {
		super();
		// this.userId = userId;
		this.timestamp = timestamp;
		this.lat = lat;
		this.lon = lon;
		this.locationId = locationId;
		Calendar c = Calendar.getInstance();
		c.setTime(timestamp);
		isWeekday = c.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
				|| c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
		final int hour = c.get(Calendar.HOUR_OF_DAY);
		if (hour >= 23 || hour < 8) {
			daySection = 0;
		} else if (hour < 18) {
			daySection = 1;
		} else {
			daySection = 2;
		}
		final int month = c.get(Calendar.MONTH);
		if (isFirstYear) {
			if (month == Calendar.DECEMBER) {
				// final int day = c.get(Calendar.DAY_OF_MONTH);
				// if (day <= 16) {
				monthPoint = 8;
				// } else if (day <= 24) {
				// monthPoint = 16;
				// } else if (day <= 28) {
				// monthPoint = 32;
				// } else if (day <= 30) {
				// monthPoint = 64;
				// } else {
				// monthPoint = 128;
				// }
			} else if (month == Calendar.NOVEMBER || month == Calendar.OCTOBER) {
				monthPoint = 4;
			} else if (month == Calendar.SEPTEMBER || month == Calendar.AUGUST
					|| month == Calendar.JULY) {
				monthPoint = 2;
			} else {
				monthPoint = 1;
			}
		} else {
			if (month == Calendar.JANUARY) {
				// final int day = c.get(Calendar.DAY_OF_MONTH);
				// if (day >= 16) {
				monthPoint = 8;
				// } else if (day >= 8) {
				// monthPoint = 16;
				// } else if (day >= 4) {
				// monthPoint = 32;
				// } else if (day >= 2) {
				// monthPoint = 64;
				// } else {
				// monthPoint = 128;
				// }
			} else if (month == Calendar.FEBRUARY || month == Calendar.MARCH) {
				monthPoint = 4;
			} else if (month == Calendar.APRIL || month == Calendar.MAY
					|| month == Calendar.JUNE) {
				monthPoint = 2;
			} else {
				monthPoint = 1;
			}
		}
	}

	public static double distance(final Record r1, final Record r2) {
		return distance(r1.lat, r2.lat, r1.lon, r2.lon);
	}

	public static double distance(final double r1Lat, final double r2Lat,
			final double r1Lon, final double r2Lon) {
		final double lat1 = r1Lat * Math.PI / 180;
		final double lat2 = r2Lat * Math.PI / 180;
		final double deltaLat = (r2Lat - r1Lat) * Math.PI / 180;
		final double deltaLon = (r2Lon - r1Lon) * Math.PI / 180;
		final double t1 = Math.sin(deltaLat / 2);
		final double t2 = Math.sin(deltaLon / 2);
		final double a = t1 * t1 + Math.cos(lat1) * Math.cos(lat2) * t2 * t2;
		final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return 6371 * c;
	}

	public static boolean canTravel(final Record r1, final Record r2) {
		final long timeDiff = Math.abs(r1.timestamp.getTime()
				- r2.timestamp.getTime());
		if (timeDiff > TEN_HOURS) {
			return true;
		}
		final double maxDistance = MAX_DIST_MILISEC * timeDiff;
		final double distance = distance(r1, r2);
		if (distance < maxDistance) {
			return true;
		} else {
			return false;
		}
	}
}
