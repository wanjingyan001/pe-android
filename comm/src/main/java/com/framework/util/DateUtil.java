package com.framework.util;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期操作工具类.
 *
 * @author Mars
 */

public class DateUtil {

	private static final String FORMAT = "yyyy-MM-dd HH:mm:ss";

	public static Date str2Date(String str) {
		return str2Date(str, null);
	}

	public static Date str2Date(String str, String format) {
		if (str == null || str.length() == 0) {
			return null;
		}
		if (format == null || format.length() == 0) {
			format = FORMAT;
		}
		Date date = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			date = sdf.parse(str);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return date;

	}

	public static Calendar str2Calendar(String str) {
		return str2Calendar(str, null);

	}

	public static Calendar str2Calendar(String str, String format) {

		Date date = str2Date(str, format);
		if (date == null) {
			return null;
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);

		return c;

	}

	public static String date2Str(Calendar c) {// yyyy-MM-dd HH:mm:ss
		return date2Str(c, null);
	}

	public static String date2Str(Calendar c, String format) {
		if (c == null) {
			return null;
		}
		return date2Str(c.getTime(), format);
	}

	public static String date2Str(Date d) {// yyyy-MM-dd HH:mm:ss
		return date2Str(d, null);
	}

	public static String date2Str(Date d, String format) {// yyyy-MM-dd HH:mm:ss
		if (d == null) {
			return null;
		}
		if (format == null || format.length() == 0) {
			format = FORMAT;
		}
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		String s = sdf.format(d);
		return s;
	}

	public static String getCurDateStr() {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		return c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-"
				+ c.get(Calendar.DAY_OF_MONTH) + "-"
				+ c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE)
				+ ":" + c.get(Calendar.SECOND);
	}

	/**
	 * 获得当前日期的字符串格式
	 *
	 * @param format
	 * @return
	 */
	public static String getCurDateStr(String format) {
		Calendar c = Calendar.getInstance();
		return date2Str(c, format);
	}

	// 格式到秒
	public static String getMillon(long time) {
		return new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(time);
	}

	// 格式到秒
	public static String getMinute(long time) {
		return new SimpleDateFormat("MM月dd日 HH:mm").format(time);
	}

	// 格式到天
	public static String getDay(long time) {
		return new SimpleDateFormat("yyyy-MM-dd").format(time);
	}

	// 格式到毫秒
	public static String getSMillon(long time) {
		return new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS").format(time);
	}

	// 格式到毫秒
	public static String getHour(long time) {
		return new SimpleDateFormat("HH:mm").format(time);
	}

	// 格式到毫秒
	public static int getExHour(long time) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date(time));
		return calendar.get(Calendar.HOUR);
	}

	public static int getMonth(){
		return Calendar.getInstance().get(Calendar.MONTH);
	}

	public static int getYear(){
		return Calendar.getInstance().get(Calendar.YEAR);
	}

	public static int getDate(){
		return Calendar.getInstance().get(Calendar.DATE);
	}
}
