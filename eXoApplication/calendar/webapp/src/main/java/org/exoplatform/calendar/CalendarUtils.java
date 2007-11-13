/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.container.PortalContainer;
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
  final public static String UNDERSCORE = "_".intern() ;
  final public static String TIMEFORMAT  = "HH:mm".intern() ;
  final public static String DATEFORMAT = "MM/dd/yyyy".intern() ;
  final public static String DATETIMEFORMAT = DATEFORMAT + " " +TIMEFORMAT ;   
  final public static int DEFAULT_TIMEITERVAL = 15 ;
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

  public static List<SelectItemOption<String>> getTimesSelectBoxOptions(String timeFormat) {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    GregorianCalendar cal = new GregorianCalendar(Locale.US) ;
    cal.set(java.util.Calendar.AM_PM, java.util.Calendar.AM) ;
    cal.set(java.util.Calendar.HOUR, 0) ;
    cal.set(java.util.Calendar.MINUTE, 0) ;
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
    GregorianCalendar cal = new GregorianCalendar(Locale.US) ;
    cal.set(java.util.Calendar.AM_PM, java.util.Calendar.AM) ;
    cal.set(java.util.Calendar.HOUR, 0) ;
    cal.set(java.util.Calendar.MINUTE, 0) ;
    DateFormat df = new SimpleDateFormat(timeFormat) ;
    DateFormat df2 = new SimpleDateFormat(TIMEFORMAT) ;
    int time = 0 ;
    while (time ++ < 24*60/(timeInteval)) {
      options.add(new SelectItemOption<String>(df.format(cal.getTime()), df2.format(cal.getTime()))) ;
      cal.add(java.util.Calendar.MINUTE, timeInteval) ;
    }
    return options ;
  }
  public static String parse(Date date, String timeFormat) throws Exception {
    DateFormat df = new SimpleDateFormat(timeFormat) ;
    return df.format(date) ;    
  }
  public static List<String> getDisplayTimes(String timeFormat, int timeInterval, int workStart, int workEnd) {
    List<String> times = new ArrayList<String>() ;
    GregorianCalendar cal = new GregorianCalendar(Locale.US) ;
    cal.set(Calendar.AM_PM, Calendar.AM) ;
    cal.set(java.util.Calendar.HOUR, 0) ;
    cal.set(java.util.Calendar.MINUTE, 0) ;
    DateFormat df = new SimpleDateFormat(timeFormat) ;
    int time = workStart ;
    while (time < workEnd) {
      times.add(df.format(cal.getTime())) ;
      cal.add(java.util.Calendar.MINUTE, timeInterval) ;
      time ++ ;
    }
    return times ;
  }
  
  static public String getCurrentUser() throws Exception {
    return Util.getPortalRequestContext().getRemoteUser() ; 
  }
  
  public static boolean isAllDayEvent(CalendarEvent eventCalendar) {
    Calendar cal1 = new GregorianCalendar() ;
    Calendar cal2 = new GregorianCalendar() ;
    cal1.setTime(eventCalendar.getFromDateTime()) ;
    cal2.setTime(eventCalendar.getToDateTime()) ;
    return (cal1.get(Calendar.HOUR) == 0  && 
            cal1.get(Calendar.MINUTE) == 0 &&
            cal2.get(Calendar.HOUR) == 0 && 
            cal2.get(Calendar.MINUTE) == 0 );
  }
}
