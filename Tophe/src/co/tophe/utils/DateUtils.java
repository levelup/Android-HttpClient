package co.tophe.utils;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import org.apache.http.impl.cookie.DateParseException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import co.tophe.BuildConfig;

/**
 * @author Created by Steve Lhomme on 28/01/2015.
 */
public final class DateUtils {
    public static final String HTTP_DATE_FORMATS[];

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            HTTP_DATE_FORMATS = new String[] {
                    org.apache.http.impl.cookie.DateUtils.PATTERN_RFC1036,
                    org.apache.http.impl.cookie.DateUtils.PATTERN_RFC1123,
                    org.apache.http.impl.cookie.DateUtils.PATTERN_ASCTIME,
                    "EEE, dd MMM yyyy HH:mm:ss 'Z'",
            };
        } else {
            // Gingerbread has issues with the timezone
            HTTP_DATE_FORMATS = new String[] {
                    org.apache.http.impl.cookie.DateUtils.PATTERN_RFC1036.substring(0, org.apache.http.impl.cookie.DateUtils.PATTERN_RFC1036.length() - 4),
                    org.apache.http.impl.cookie.DateUtils.PATTERN_RFC1123.substring(0, org.apache.http.impl.cookie.DateUtils.PATTERN_RFC1123.length() - 4),
                    org.apache.http.impl.cookie.DateUtils.PATTERN_ASCTIME,
            };
        }
    }

    private static final ThreadLocal<Map<String, SimpleDateFormat>> formatMap = new ThreadLocal<Map<String, SimpleDateFormat>>() {
        protected Map<String, SimpleDateFormat> initialValue() {
            return new HashMap<>();
        }
    };

    /**
     * Parse a date {@code String} coming from HTTP headers.
     *
     * @throws DateParseException when the String cannot be parsed
     * @see #parseDate(String, String[])
     */
    @Nullable
    public static Date parseDate(@Nullable String dateString) throws DateParseException {
        return parseDate(dateString, HTTP_DATE_FORMATS);
    }

    /**
     * Parse a date {@code String} with multiple date format support.
     *
     * @param dateString  the String to parse
     * @param dateFormats date formats supported, see {@link java.text.SimpleDateFormat}
     * @return the parsed date or {@code null} if parsing failed.
     * @throws DateParseException
     * @see #parseDate(String)
     */
    public static Date parseDate(@Nullable String dateString, @NonNull String[] dateFormats) throws DateParseException {
        Log.v("DateUtils", "parsing " + dateString);
        if (!TextUtils.isEmpty(dateString)) {
            try {
                if (false && Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                    // Gingerbread has issues with the timezone
                    if (dateString.endsWith(" UTC"))
                        dateString = dateString.substring(0, dateString.length()-4);
                }

                return org.apache.http.impl.cookie.DateUtils.parseDate(dateString, dateFormats);
            } catch (DateParseException e) {
                if (false) {
                    for (String dateFormat : HTTP_DATE_FORMATS) {
                        SimpleDateFormat format = formatMap.get().get(dateFormat);
                        if (format == null) {
                            format = new SimpleDateFormat(dateFormat, Locale.ENGLISH);
                            format.setTimeZone(TimeZone.getTimeZone("UTC"));
                            formatMap.get().put(dateFormat, format);
                        }
                        try {
                            return format.parse(dateString);
                        } catch (ParseException ignored) {
                        }
                    }
                }

                if (BuildConfig.DEBUG)
                    throw new RuntimeException("failed to parse date " + dateString); // for testing purposes of unsupported date formats
            }
        }
        return null;
    }
}
