/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.service;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Jul 16, 2007  
 */
public class CalendarSetting {
  //view types
  public static String DAY_VIEW = "0" ;
  public static String WEEK_VIEW = "1" ;
  public static String MONTH_VIEW = "2" ;
  public static String YEAR_VIEW = "3" ;
  public static String SCHEDULE_VIEW = "4" ;
  public static String LIST_VIEW = "5" ;

  // time weekStartOn types
  public static String MONDAY = "1" ;
  public static String TUESDAY = "2" ;
  public static String WENDNESDAY = "3" ;
  public static String THURSDAY = "4" ;
  public static String FRIDAY = "5" ;
  public static String SATURDAY = "6" ;
  public static String SUNDAY = "7" ;

  private String viewType ;
  private long timeInterval ;
  private String weekStartOn ;
  private String dateFormat ;
  private String timeFormat ;
  private String location ;
  private String timeZone ;
  private String baseURL ;
  private String[] defaultPrivateCalendars ;
  private String[] defaultPublicCalendars ;
  private boolean isShowWorkingTime = false ; 
  private String workingTimeBegin ;
  private String workingTimeEnd ;
  private String[] sharedCalendarsColors ;

  public CalendarSetting() {
    viewType = DAY_VIEW ;
    timeInterval = 15 ;
    weekStartOn = MONDAY ;
    dateFormat = "MM/dd/yyyy" ;
    timeFormat = "hh:mm a" ;
    isShowWorkingTime = false ;
    baseURL = "http://localhost:8080/calendar/iCalRss" ;
    defaultPrivateCalendars = new String[]{} ;
    defaultPublicCalendars = new String[]{} ;
    sharedCalendarsColors = new String[]{} ;
  }

  public void setViewType(String viewType) { this.viewType = viewType ; }
  public String getViewType() { return viewType ; }

  public void setTimeInterval(long timeInterval) { this.timeInterval = timeInterval ; }
  public long getTimeInterval() { return timeInterval ; }

  public void setWeekStartOn(String weekStartOn) { this.weekStartOn = weekStartOn ; }
  public String getWeekStartOn() { return weekStartOn ; }

  public void setDateFormat(String dateFormat) { this.dateFormat = dateFormat ; }
  public String getDateFormat() { return dateFormat ; }

  public void setTimeFormat(String timeFormat) { this.timeFormat = timeFormat ; }
  public String getTimeFormat() { return timeFormat ; }

  public void setLocation(String location) { this.location = location ; }
  public String getLocation() { return location ; }

  public void setBaseURL(String url) { this.baseURL = url ; }
  public String getBaseURL() { return baseURL ; }

  public void setDefaultPrivateCalendars(String[] defaultCalendars) { this.defaultPrivateCalendars = defaultCalendars ; }
  public String[] getDefaultPrivateCalendars() { return defaultPrivateCalendars ; }

  public void setDefaultPublicCalendars(String[] defaultCalendars) { this.defaultPublicCalendars = defaultCalendars ; }
  public String[] getDefaultPublicCalendars() { return defaultPublicCalendars ; }

  public void setShowWorkingTime(boolean isShowWorkingTime) {
    this.isShowWorkingTime = isShowWorkingTime;
  }

  public boolean isShowWorkingTime() {
    return isShowWorkingTime;
  }

  public void setWorkingTimeBegin(String workingTimeBegin) {
    this.workingTimeBegin = workingTimeBegin;
  }

  public String getWorkingTimeBegin() {
    return workingTimeBegin;
  }

  public void setWorkingTimeEnd(String workingTimeEnd) {
    this.workingTimeEnd = workingTimeEnd;
  }

  public String getWorkingTimeEnd() {
    return workingTimeEnd;
  }

  public void setTimeZone(String timeZone) {
    this.timeZone = timeZone;
  }

  public String getTimeZone() {
    return timeZone;
  }

  public void setSharedCalendarsColors(String[] sharedCalendarColor) {
    sharedCalendarsColors = sharedCalendarColor;
  }

  public String[] getSharedCalendarsColors() {
    return sharedCalendarsColors;
  }
}

