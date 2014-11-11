package eu.leads.api.com;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class TimeConvertionUtils {
	
	public static  Long timestampToDay(Long timestamp) {
		System.out.println(new Date(timestamp));
		Long day = new Long(0);
		
		Calendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(timestamp);
		calendar.set(Calendar.HOUR_OF_DAY,0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		Long dayTimestamp = calendar.getTimeInMillis();
		System.out.println(calendar.getTime());
		System.out.println(dayTimestamp);
		day = dayTimestamp / 24 / 60 / 60 / 1000;
		
		return day;
	}
	
	public static Long timestampToWeek(Long timestamp) {
		Long week = new Long(0);
		
		Long day = timestampToDay(timestamp);
		week = (long)Math.floor((day+4.0)/7.0);
		
		return week;
	}
	
	public static Long timestampToMonth(Long timestamp) {
		return null;
	}	
	
	/////////////////////////////////////////////////
	
	public static void main(String[] args) {
//		Calendar cal = new GregorianCalendar(2014,9,13,19,13);
//		System.out.println(timestampToDay(cal.getTimeInMillis()));
//		cal = new GregorianCalendar(2014,9,13,07,00);
//		System.out.println(timestampToDay(cal.getTimeInMillis()));
//		cal = new GregorianCalendar(2014,9,13,23,54);
//		System.out.println(timestampToDay(cal.getTimeInMillis()));

//		cal = new GregorianCalendar(2014,9,13,07,00);
//		System.out.println(timestampToWeek(cal.getTimeInMillis()));
//		cal = new GregorianCalendar(2014,9,19,23,54);
//		System.out.println(timestampToWeek(cal.getTimeInMillis()));		

		System.out.println(timestampToWeek(1410867171323L));
		System.out.println(timestampToWeek(1411324587283L));
	}
	
}
