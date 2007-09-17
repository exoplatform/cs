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
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
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
      @EventConfig(listeners = UIMonthView.MoveNextActionListener.class), 
      @EventConfig(listeners = UIMonthView.MovePreviousActionListener.class),
      @EventConfig(listeners = UICalendarView.AddCategoryActionListener.class)
    }

)
public class UIMonthView extends UICalendarView {

  private Map<String, String> calendarIds_ = new HashMap<String, String>() ;

  private Map<Integer, List<CalendarEvent>> eventData_ = new HashMap<Integer, List<CalendarEvent>>() ;

  public UIMonthView() throws Exception{
    super() ;
    refreshSelectedCalendarIds() ;
    refreshEvents() ;
  }
  protected void refreshEvents() throws Exception {
    List<String> calendarIds = new ArrayList<String>(getCalendarIds().values()) ;
    CalendarService calendarService = getApplicationComponent(CalendarService.class) ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    List<CalendarEvent> allEvents = calendarService.getUserEventByCalendar(username, calendarIds) ;
    for(int day =1 ;  day <= getDaysInMonth(); day++) {
      List<CalendarEvent> existEvents = new ArrayList<CalendarEvent>() ;
      for(CalendarEvent ce : allEvents) {
        GregorianCalendar gc = new GregorianCalendar(getCurrentYear(), getCurrentMonth(), day) ;
        Date tempDate = gc.getTime() ;
        Date fromDate = ce.getFromDateTime() ;
        Date endDate = ce.getToDateTime() ;
        gc.setTime(fromDate) ;
        int beginDay = gc.get(java.util.Calendar.DATE) ;
        gc.setTime(endDate) ;
        int endDay = gc.get(java.util.Calendar.DATE) ;
        if((fromDate.before(tempDate) && endDate.after(tempDate))||(beginDay == day) || (endDay == day) )  {
          existEvents.add(ce) ;
        } 
      }
      eventData_.put(day, existEvents) ;
    }
  }
  protected void addCalendarId(String id) {calendarIds_.put(id,id) ;}
  protected Map<String, String> getCalendarIds() {return calendarIds_ ;}

  // private int getCurrentDay() {return currentDay_; }  
  // void setCurrentDay(int day) {currentDay_ = day; }  
  //private int getCurrentMonth() {return currentMonth_; }  
  // protected void setCurrentMonth(int month) {currentMonth_ = month; }  
  //private int getCurrentYear() {return currentYear_;}
  //protected void setCurrentYear(int year) {currentYear_ = year;}

  private List getEventList()throws Exception {
    return getList() ;
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
  private Date getDateOf(int year, int month, int day) {
    GregorianCalendar gc = new GregorianCalendar(year, month, day) ;
    return gc.getTime() ;
  }

  private Map<Integer, List<CalendarEvent>> getEventsData() {
    return eventData_ ;
  }

  protected void monthNext(int months) {
    if(calendar_.get(java.util.Calendar.MONTH) == java.util.Calendar.DECEMBER) {
      calendar_.roll(java.util.Calendar.YEAR, true) ;
      calendar_.set(java.util.Calendar.MONTH, java.util.Calendar.JANUARY) ;
    } else {
      calendar_.roll(java.util.Calendar.MONTH, months) ;
    }
  }
  protected void monthBack(int months) {
    if(calendar_.get(java.util.Calendar.MONTH) == java.util.Calendar.JANUARY) {
      calendar_.roll(java.util.Calendar.YEAR, false) ;
      calendar_.set(java.util.Calendar.MONTH, java.util.Calendar.DECEMBER) ;
    } else {
      calendar_.roll(java.util.Calendar.MONTH, months) ;
    }
  }

  static  public class MoveNextActionListener extends EventListener<UIMonthView> {
    public void execute(Event<UIMonthView> event) throws Exception {
      UIMonthView calendarview = event.getSource() ;
      calendarview.monthNext(1) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
    }
  }

  static  public class MovePreviousActionListener extends EventListener<UIMonthView> {
    public void execute(Event<UIMonthView> event) throws Exception {
      UIMonthView calendarview = event.getSource() ;
      calendarview.monthBack(-1) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
    }
  }

}
