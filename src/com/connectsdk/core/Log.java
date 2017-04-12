package com.connectsdk.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Log {

	private static final Logger l = LoggerFactory.getLogger(Log.class);

	public static int d(java.lang.String tag, java.lang.String msg) {
		l.debug(String.format("%s - %s", tag, msg));
		return 0;
	}

	public static int w(java.lang.String tag, java.lang.String msg) {
		l.warn(String.format("%s - %s", tag, msg));
		return 0;
	}
	
	public static int e(java.lang.String tag, java.lang.String msg) {
		l.error(String.format("%s - %s",tag, msg));
		return 0;
	}

	public static int e(java.lang.String tag, java.lang.String msg, java.lang.Throwable tr) {
		l.error(String.format("%s - %s",tag, msg), tr);
		return 0;
	}


}
