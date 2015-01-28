package co.tophe.utils;

import java.util.Date;

import org.apache.http.impl.cookie.DateParseException;

import android.os.Build;

/**
 * @author Created by Steve Lhomme on 28/01/2015.
 */
public final class DateUtils {
	private static final String ALL_DATE_FORMATS[];
	static {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ALL_DATE_FORMATS = new String[] {
					org.apache.http.impl.cookie.DateUtils.PATTERN_RFC1036,
					org.apache.http.impl.cookie.DateUtils.PATTERN_RFC1123,
					org.apache.http.impl.cookie.DateUtils.PATTERN_ASCTIME,
					"EEE, dd-MMM-yyyy HH:mm:ss zzz",
			};
		} else {
			// Gingerbread has issues with the timezone
			ALL_DATE_FORMATS = new String[] {
					org.apache.http.impl.cookie.DateUtils.PATTERN_RFC1036,
					org.apache.http.impl.cookie.DateUtils.PATTERN_RFC1123,
					org.apache.http.impl.cookie.DateUtils.PATTERN_ASCTIME,
					"EEE, dd-MMM-yyyy HH:mm:ss zzz",
					"EEE, dd-MMM-yyyy HH:mm:ss"
			};
		}
	}

	/**
	 * Parse a date {@code String} coming from HTTP headers.
	 * @throws DateParseException when the String cannot be parsed
	 */
	public static Date parseDate(String dateString) throws DateParseException {
		return org.apache.http.impl.cookie.DateUtils.parseDate(dateString, ALL_DATE_FORMATS);
	}
}
