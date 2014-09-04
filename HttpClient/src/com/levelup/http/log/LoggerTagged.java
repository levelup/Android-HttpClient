package com.levelup.http.log;

/**
 * interface for a logger class to replace the static calls to {@link android.util.Log} with implicit tag
 */
public interface LoggerTagged {
    /**
     * Send a {@link android.util.Log#VERBOSE VERBOSE} log message.
     * @param msg The message you would like logged.
     */
	int v(String msg);
	
    /**
     * Send a {@link android.util.Log#VERBOSE VERBOSE} log message and log the exception.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    int v(String msg, Throwable tr);
	
    /**
     * Send a {@link android.util.Log#DEBUG DEBUG} log message.
     * @param msg The message you would like logged.
     */
    int d(String msg);
	
    /**
     * Send a {@link android.util.Log#DEBUG DEBUG} log message and log the exception.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    int d(String msg, Throwable tr);

    /**
     * Send an {@link android.util.Log#INFO INFO} log message.
     * @param msg The message you would like logged.
     */
    int i(String msg);
	
    /**
     * Send an {@link android.util.Log#INFO INFO} log message and log the exception.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    int i(String msg, Throwable tr);
	
    /**
     * Send a {@link android.util.Log#WARN WARN} log message.
     * @param msg The message you would like logged.
     */
    int w(String msg);
	
    /**
     * Send a {@link android.util.Log#WARN WARN} log message and log the exception.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    int w(String msg, Throwable tr);
	
    /**
     * Send an {@link android.util.Log#ERROR ERROR} log message.
     * @param msg The message you would like logged.
     */
    int e(String msg);
	
    /**
     * Send an {@link android.util.Log#ERROR ERROR} log message and log the exception.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    int e(String msg, Throwable tr);

    /**
     * Send an {@link android.util.Log#ASSERT ASSERT} log message.
     * @param msg The message you would like logged.
     */
    int wtf(String msg);
	
    /**
     * Send an {@link android.util.Log#ASSERT ASSERT} log message and log the exception.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    int wtf(String msg, Throwable tr);
}
