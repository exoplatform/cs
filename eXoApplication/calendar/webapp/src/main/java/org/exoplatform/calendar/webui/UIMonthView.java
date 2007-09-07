/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.jgroups.ExitEvent;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/calendar/webui/UIMonthView.gtmpl", 
    events = {
      @EventConfig(listeners = UICalendarView.RefreshActionListener.class),
      @EventConfig(listeners = UICalendarView.AddEventActionListener.class),      
      @EventConfig(listeners = UICalendarView.DeleteEventActionListener.class),
      @EventConfig(listeners = UICalendarView.ChangeCategoryActionListener.class), 
      @EventConfig(listeners = UICalendarView.AddCategoryActionListener.class)
    }

)
public class UIMonthView extends UICalendarView {

  final public static String JANUARY = "January".intern() ;
  final public static String FEBRUARY = "February".intern() ;
  final public static String MARCH = "March".intern() ;
  final public static String APRIL = "April".intern() ;
  final public static String MAY = "May".intern() ;
  final public static String JUNE = "June".intern() ;
  final public static String JULY = "July".intern() ;
  final public static String AUGUST = "August".intern() ;
  final public static String SEPTEMBER = "September".intern() ;
  final public static String OCTOBER = "October".intern() ;
  final public static String NOVEMBER = "November".intern() ;
  final public static String DECEMBER = "December".intern() ;
  final public static String[] MONTHS = {JANUARY, FEBRUARY, MARCH, APRIL, MAY, JUNE, JULY, AUGUST, SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER} ;

  final public static String MONDAY = "Monday".intern() ;
  final public static String TUESDAY = "Tuesday".intern() ;
  final public static String WEDNESDAY = "Wednesday".intern() ;
  final public static String THURSDAY = "Thursday".intern() ;
  final public static String FRIDAY = "Friday".intern() ;
  final public static String SATURDAY = "Saturday".intern() ;
  final public static String SUNDAY = "Sunday".intern() ;
  final public static String[] DAYS = {SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY} ; 

  public java.util.Calendar calendar_ = java.util.Calendar.getInstance() ;

  private int currentDay_ = 0 ;
  private int currentMonth_ = 0 ;
  private int currentYear_ = 1900 ;

  private Map<String, String> calendarIds_ = new HashMap<String, String>() ;
  final public static Map<Integer, String> monthsName_ = new HashMap<Integer, String>() ;

  private Map<Integer, String> daysMap_ = new HashMap<Integer, String>() ;

  private Map<Integer, String> monthsMap_ = new HashMap<Integer, String>() ;

  private List<CalendarEvent> allEvent_ = new ArrayList<CalendarEvent>() ;
  private Map<Integer, List<CalendarEvent>> eventData_ = new HashMap<Integer, List<CalendarEvent>>() ;

  public UIMonthView() throws Exception{
    super() ;
    calendar_.setTime(new Date()) ;
    currentDay_ = calendar_.get(java.util.Calendar.DATE) ;
    currentMonth_ = calendar_.get(java.util.Calendar.MONTH) ;
    currentYear_  = calendar_.get(java.util.Calendar.YEAR) ;
    int i = 0 ; 
    for(String month : MONTHS) {
      monthsMap_.put(i, month) ;
      i++ ;
    }
    int j = 1 ;
    for(String month : DAYS) {
      daysMap_.put(j, month) ;
      j++ ;
    }
    refreshSelectedCalendarIds() ;
    refreshEvents() ;
  }
  protected void refreshEvents() throws Exception {
    List<String> calendarIds = new ArrayList<String>(getCalendarIds().values()) ;
    CalendarService calendarService = getApplicationComponent(CalendarService.class) ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    for(int k =1 ;  k <= getDaysInMonth(); k++) {
      List<CalendarEvent> existEvents = new ArrayList<CalendarEvent>() ;
      List<CalendarEvent> allEvents = calendarService.getUserEventByCalendar(username, calendarIds) ;
      for(CalendarEvent ce : allEvents) {
        GregorianCalendar gc = new GregorianCalendar(getCurrentYear(), getCurrentMonth(), k) ;
        Date tempDate = gc.getTime() ;
        Date fromDate = ce.getFromDateTime() ;
        System.out.println("\n\n event " + ce.getSummary());
        System.out.println("\n\n from " + ce.getFromDateTime());
        System.out.println("\n\n to " + ce.getToDateTime());
        
        Date endDate = ce.getToDateTime() ;
        if(tempDate.getTime() >= fromDate.getTime() && tempDate.getTime() <= endDate.getTime())  {
          existEvents.add(ce) ;
        } 
       //if(allEvents.indexOf(ce) == 3) break ;
      }
      eventData_.put(k, existEvents) ;
    }
  }
  protected void addCalendarId(String id) {calendarIds_.put(id,id) ;}
  protected Map<String, String> getCalendarIds() {return calendarIds_ ;}
  private int getCurrentDay() {return currentDay_; }  
  protected void setCurrentDay(int day) {currentDay_ = day; }  
  private int getCurrentMonth() {return currentMonth_; }  
  protected void setCurrentMonth(int month) {currentMonth_ = month; }  
  private int getCurrentYear() {return currentYear_;}
  protected void setCurrentYear(int year) {currentYear_ = year;}

  private  String getMonthName(int month) {return monthsMap_.get(month).toString() ;} ;
  private  String getDayName(int day) {return daysMap_.get(day).toString() ;} ;

  private List getEventList() {
    return null ;
  }

  protected void refreshSelectedCalendarIds() throws Exception {
    CalendarService calendarService = getApplicationComponent(CalendarService.class) ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    for(Calendar c : calendarService.getUserCalendars(username)) {
      addCalendarId(c.getId()) ;
    }
  }

  public void refresh() throws Exception {
    refreshSelectedCalendarIds() ;
    refreshEvents() ;
  }
  private int getDaysInMonth() {
    int month = getCurrentMonth() ;
    int year =  getCurrentYear();
    Integer[] days = {31, ((!((year % 4 ) == 0) && ( (year % 100  == 0) || ( year % 400 != 0) ))? 29:28), 31, 30, 31, 30, 31, 31, 30, 31, 30, 31} ;
    return days[month] ;
  }
  private Date getDateOf(int year, int month, int day) {
    GregorianCalendar gc = new GregorianCalendar(year, month, day) ;
    return gc.getTime() ;
  }
  private int getStartDayOfWeek(int year, int month, int day) {
    GregorianCalendar gc = new GregorianCalendar(year, month, day) ;
    return gc.getFirstDayOfWeek() ;
  }
  private int getDayOfWeek(int year, int month, int day) {
    GregorianCalendar gc = new GregorianCalendar(year, month, day) ;
    return gc.get(java.util.Calendar.DAY_OF_WEEK) ;
  }
  private String[] getMonthsName() { return MONTHS ;}
  private String[] getDaysName() {return DAYS ;}

  private Map<Integer, List<CalendarEvent>> getEventsData() {
    return eventData_ ;
  }
}
