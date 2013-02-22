package com.janus.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class DateUtil {
	
	/**
	 * Parser based on sqlite date format 
	 */
	private static final SimpleDateFormat DATE_PARSER = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss");
	
	/**
	 * Private constructor for utility class
	 * 
	 */
	private DateUtil() {
		
	}

	/**
	 * Clone a given date
	 * 
	 * @param date
	 * @return
	 */
	public static Date dateClone(Date date) {
		Date cloneDate = new Date(date.getTime());
		return cloneDate;
	}
	
	/**
	 * Given a string in the form 'yyyy-MM-DD HH:mm:ss' (which is the
	 * timestamp format from SQLite) convert it into a date object.
	 * 
	 * @param input
	 * @return
	 */
	public static Date parseFromSQLiteString(String input) {
		Date output;
		try {
			output = DateUtil.DATE_PARSER.parse(input);
		} catch (ParseException e) {
			output = null;
			e.printStackTrace();
		}
		return output;		
	}
	
}
