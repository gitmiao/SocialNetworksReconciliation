

import java.util.Calendar;
import java.util.Date;

public class Record {
	final int userId;
	final Date timestamp;
	final double lat;
	final double lon;
	final String locationId;
	final boolean isWeekday;
	// 0: midnight to 8am; 1: 8am to 7pm; 2: 7pm - midnight
	final int daySection;

	public Record(final int userId, final Date timestamp, final double lat,
			final double lon, final String locationId) {
		super();
		this.userId = userId;
		this.timestamp = timestamp;
		this.lat = lat;
		this.lon = lon;
		this.locationId = locationId;
		Calendar c = Calendar.getInstance();
		c.setTime(timestamp);
		isWeekday = c.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
				|| c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
		final int hour = c.get(Calendar.HOUR_OF_DAY);
		if (hour < 8) {
			daySection = 0;
		} else if (hour < 19) {
			daySection = 1;
		} else {
			daySection = 2;
		}
	}

	@Override
	public String toString() {
		return "Record [userId=" + userId + ", timestamp=" + timestamp
				+ ", lat=" + lat + ", lon=" + lon + ", locationId="
				+ locationId + ", isWeekday=" + isWeekday + ", daySection="
				+ daySection + "]";
	}
}
