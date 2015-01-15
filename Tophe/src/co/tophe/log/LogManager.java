package co.tophe.log;

import android.util.Log;

/**
 * Class that holds the {@link LoggerTagged} for this library, defaults to {@link LoggerAndroid} to send logs to android {@link Log}.
 */
public final class LogManager {

	private static LoggerTagged logger = new LoggerAndroid();
	
	public static void setLogger(LoggerTagged newLogger) {
		logger = newLogger;
	}
	
	public static LoggerTagged getLogger() {
		return logger;
	}

}
