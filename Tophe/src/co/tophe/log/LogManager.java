package co.tophe.log;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;

/**
 * class that holds the {@link LoggerTagged} for this library, defaults to {@link LoggerDefault} to send logs to android {@link Log}
 */
public final class LogManager {

	static LoggerTagged logger = new LoggerDefault();
	
	public static void setLogger(LoggerTagged newLogger) {
		logger = newLogger;
	}
	
	public static LoggerTagged getLogger() {
		return logger;
	}

	/**
	 * Helper class to redirect {@link LogManager#logger} to {@link Log}
	 */
	public static class LoggerDefault implements LoggerTagged {
		private static final String tag = "TOPHE";
		
		public LoggerDefault() {}
		
		@Override
		public int v(String msg) {
			return Log.v(tag, msg);
		}

		@Override
		public int v(String msg, Throwable tr) {
			return Log.v(tag, msg, tr);
		}

		@Override
		public int d(String msg) {
			return Log.d(tag, msg);
		}

		@Override
		public int d(String msg, Throwable tr) {
			return Log.d(tag, msg, tr);
		}

		@Override
		public int i(String msg) {
			return Log.i(tag, msg);
		}

		@Override
		public int i(String msg, Throwable tr) {
			return Log.i(tag, msg, tr);
		}

		@Override
		public int w(String msg) {
			return Log.w(tag, msg);
		}

		@Override
		public int w(String msg, Throwable tr) {
			return Log.w(tag, msg, tr);
		}

		@Override
		public int e(String msg) {
			return Log.e(tag, msg);
		}

		@Override
		public int e(String msg, Throwable tr) {
			return Log.e(tag, msg, tr);
		}

		@TargetApi(Build.VERSION_CODES.FROYO)
		@Override
        public int wtf(String msg) {
	        return Log.wtf(tag, msg);
        }

		@TargetApi(Build.VERSION_CODES.FROYO)
		@Override
        public int wtf(String msg, Throwable tr) {
	        return Log.wtf(tag, msg, tr);
        }

	}
}
