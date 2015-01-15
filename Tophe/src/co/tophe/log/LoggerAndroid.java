package co.tophe.log;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;

/**
 * Helper class to send calls to {@link co.tophe.log.LoggerTagged} to {@link android.util.Log}
 */
public class LoggerAndroid implements LoggerTagged {
	private static final String tag = "TOPHE";

	public LoggerAndroid() {}

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
