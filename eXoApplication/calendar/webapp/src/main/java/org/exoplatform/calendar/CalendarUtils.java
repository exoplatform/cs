/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.exoplatform.calendar.service.Attachment;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.impl.GroupImpl;
import org.exoplatform.webui.core.model.SelectItemOption;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public class CalendarUtils {
	public static final String PRIVATE_TYPE = "0".intern() ;
	public static final String SHARED_TYPE = "1".intern() ;
	public static final String PUBLIC_TYPE = "2".intern() ;

	final public static String SEMICOLON = ";".intern() ;
	final public static String COLON = ",".intern() ;
	final public static String SLASH = "/".intern() ;
	final public static String BACKSLASH = "\\".intern() ;

	final public static String UNDERSCORE = "_".intern() ;
	final public static String TIMEFORMAT  = "HH:mm".intern() ;
	final public static String DATEFORMAT = "MM/dd/yyyy".intern() ;
	final public static String DATETIMEFORMAT = DATEFORMAT + " " +TIMEFORMAT ;   
	final public static int DEFAULT_TIMEITERVAL = 15 ;
	final public static long MILISECONS_OF_DAY = 24*60*60*1000 ;


	final public static String SPECIALCHARACTER[] = {SEMICOLON,COLON,SLASH,BACKSLASH,"'","|",">","<","\"", "?", "!", "@", "#", "$", "%","^","&","*"} ;

	static public String[] getUserGroups(String username) throws Exception {
		OrganizationService organization = (OrganizationService)PortalContainer.getComponent(OrganizationService.class) ;
		Object[] objs = organization.getGroupHandler().findGroupsOfUser(username).toArray() ;
		String[] groups = new String[objs.length] ;
		for(int i = 0; i < objs.length ; i ++) {
			groups[i] = ((GroupImpl)objs[i]).getId() ;
		}
		return groups ;
	}
	static public String[] getAllGroups() throws Exception {
		OrganizationService organization = (OrganizationService)PortalContainer.getComponent(OrganizationService.class) ;
		Object[] objs = organization.getGroupHandler().getAllGroups().toArray() ;
		String[] groups = new String[objs.length] ;
		for(int i = 0; i < objs.length ; i ++) {
			groups[i] = ((GroupImpl)objs[i]).getId() ;
		}
		return groups ;
	}
	public static boolean isEmpty(String value) {
		return (value == null || value.trim().length() == 0) ;
	}

	static public CalendarService getCalendarService() throws Exception {
		return (CalendarService)PortalContainer.getComponent(CalendarService.class) ;
	}
	public static Calendar getInstanceTempCalendar() { 
		Calendar  calendar = GregorianCalendar.getInstance() ;
    calendar.setLenient(false) ;
		int gmtoffset = calendar.get(Calendar.DST_OFFSET) + calendar.get(Calendar.ZONE_OFFSET);
		calendar.setTimeInMillis(System.currentTimeMillis() - gmtoffset) ;
		return  calendar;
	}
	public static List<SelectItemOption<String>> getTimesSelectBoxOptions(String timeFormat) {
		List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
		Calendar cal = getBeginDay(GregorianCalendar.getInstance()) ;
		DateFormat df = new SimpleDateFormat(timeFormat) ;
		DateFormat df2 = new SimpleDateFormat(TIMEFORMAT) ;
		int time = 0 ;
		while (time ++ < 24*60/(15)) {
			options.add(new SelectItemOption<String>(df.format(cal.getTime()), df2.format(cal.getTime()))) ;
			cal.add(java.util.Calendar.MINUTE, 15) ;
		}
		return options ;
	}
	public static List<SelectItemOption<String>> getTimesSelectBoxOptions(String timeFormat, int timeInteval) {
		List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
		Calendar cal = getBeginDay(GregorianCalendar.getInstance()) ;
		DateFormat df = new SimpleDateFormat(timeFormat) ;
		DateFormat df2 = new SimpleDateFormat(TIMEFORMAT) ;
		int time = 0 ;
		while (time ++ < 24*60/(timeInteval)) {
			options.add(new SelectItemOption<String>(df.format(cal.getTime()), df2.format(cal.getTime()))) ;
			cal.add(java.util.Calendar.MINUTE, timeInteval) ;
		}
		return options ;
	}

	public static List<SelectItemOption<String>> getTimeZoneSelectBoxOptions(String[] timeZoneIds) {
		List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
		for (String tz : timeZoneIds){
			TimeZone timeZone = TimeZone.getTimeZone(tz) ;
			int rawOffset = timeZone.getRawOffset() / 60000;
			int hours = rawOffset / 60;
			int minutes = Math.abs(rawOffset) % 60;
			String hrStr = "";
			if (Math.abs(hours) < 10) {
				if (hours < 0) {
					hrStr = "-0" + Math.abs(hours);
				} else {
					hrStr = "0" + Math.abs(hours);
				}
			} else {
				hrStr = Integer.toString(hours);
			}
			String minStr = (minutes < 10) ? ("0" + Integer.toString(minutes)) : Integer.toString(minutes);
			String str = "(GMT " + ((timeZone.getRawOffset() >= 0) ? "+" : "") + hrStr + ":" + minStr + ") " + timeZone.getID();
			options.add(new SelectItemOption<String>( str , tz)); 
		}
		return options ;
	}
	public static List<SelectItemOption<String>> getLocaleSelectBoxOptions(Locale[] locale) {
		List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
		for(Locale local : locale) {
			String country = local.getISO3Country() ;
			if( country != null && country.trim().length() > 0) options.add(new SelectItemOption<String>(local.getDisplayCountry() , country)) ;
		}
		return options ;
	}
	public static String parse(Date date, String timeFormat) throws Exception {
		DateFormat df = new SimpleDateFormat(timeFormat) ;
		return df.format(date) ;    
	}

	static public String getCurrentUser() throws Exception {
		return Util.getPortalRequestContext().getRemoteUser() ; 
	}

	public static boolean isAllDayEvent(CalendarEvent eventCalendar) {
		Calendar cal1 = getInstanceTempCalendar() ;
		Calendar cal2 = getInstanceTempCalendar() ;
		cal1.setTime(eventCalendar.getFromDateTime()) ;
		cal2.setTime(eventCalendar.getToDateTime()) ;
		return (cal1.get(Calendar.HOUR_OF_DAY) == 0  && 
				cal1.get(Calendar.MINUTE) == 0 &&
				cal2.get(Calendar.HOUR_OF_DAY) == 0 && 
				cal2.get(Calendar.MINUTE) == 0 );
	}

	public static boolean isSameDate(java.util.Calendar date1, java.util.Calendar date2) {
		return ( date1.get(java.util.Calendar.DATE) == date2.get(java.util.Calendar.DATE) &&
				date1.get(java.util.Calendar.MONTH) == date2.get(java.util.Calendar.MONTH) &&
				date1.get(java.util.Calendar.YEAR) == date2.get(java.util.Calendar.YEAR)
		) ;
	}
	public static boolean isSameDate(Date value1, Date value2) {
		Calendar date1 = getInstanceTempCalendar() ;
		date1.setTime(value1) ;
		Calendar date2 = getInstanceTempCalendar() ;
		date2.setTime(value2) ;
		return isSameDate(date1, date2) ;
	}

	public static Calendar getBeginDay(Calendar cal) {
    Calendar newCal = new GregorianCalendar() ;
    newCal.setTime(cal.getTime()) ;
    newCal.set(Calendar.HOUR_OF_DAY, 0) ;
    newCal.set(Calendar.MINUTE, 0) ;
    newCal.set(Calendar.SECOND, 0) ;
    newCal.set(Calendar.MILLISECOND, 0) ;
		return newCal ;
	}
	public static Calendar getEndDay(Calendar cal)  {
    Calendar newCal = new GregorianCalendar() ;
    newCal.setTime(cal.getTime()) ;
    newCal.set(Calendar.HOUR_OF_DAY, 0) ;
    newCal.set(Calendar.MINUTE, 0) ;
    newCal.set(Calendar.SECOND, 0) ;
    newCal.set(Calendar.MILLISECOND, 0) ;
    newCal.add(Calendar.HOUR_OF_DAY, 24) ;
		return newCal ;
	}

	public static Calendar getBeginDay(Date date) {
		Calendar cal = getInstanceTempCalendar() ;
		cal.setTime(date) ;
		return getBeginDay(cal) ;
	}
	public static Calendar getEndDay(Date date)  {
		Calendar cal = getInstanceTempCalendar() ;
		cal.setTime(date) ;
		return getEndDay(cal) ;
	}
	public static String getImageSource(Attachment attach, DownloadService dservice) throws Exception {      
		if (attach != null) {
			InputStream input = attach.getInputStream() ;
			byte[] imageBytes = null ;
			if (input != null) {
				imageBytes = new byte[input.available()] ;
				input.read(imageBytes) ;
				ByteArrayInputStream byteImage = new ByteArrayInputStream(imageBytes) ;
				InputStreamDownloadResource dresource = new InputStreamDownloadResource(byteImage, "image") ;
				dresource.setDownloadName(attach.getName()) ;
				return  dservice.getDownloadLink(dservice.addDownloadResource(dresource)) ;        
			}
		}
		return null ;
	}

	public static boolean isNameValid(String name, String[] regexpression) {
		for(String c : regexpression){ if(name.contains(c)) return false ;}
		return true ;
	}

	public static boolean isNameEmpty(String name) {
		return (name == null || name.trim().length() == 0) ;
	}
}
