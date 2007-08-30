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
  public static String DAY_VIEW = "1" ;
  public static String WEEK_VIEW = "2" ;
  public static String MONTH_VIEW = "3" ;
  public static String YEAR_VIEW = "4" ;
  public static String SCHEDULE_VIEW = "5" ;
  public static String EVENTS_VIEW = "6" ;
  public static String TASKS_VIEW = "7" ;
  
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
  private String[] defaultCalendars ;
  
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
  
  public void setDefaultCalendars(String[] defaultCalendars) { this.defaultCalendars = defaultCalendars ; }
  public String[] getDefaultCalendars() { return defaultCalendars ; }
  
  
}
