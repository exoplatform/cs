/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle =UIFormLifecycle.class,
    template = "app:/templates/calendar/webui/UIWeekView.gtmpl",
    events = {
      @EventConfig(listeners = UICalendarView.AddEventActionListener.class),  
      @EventConfig(listeners = UICalendarView.DeleteEventActionListener.class),
      @EventConfig(listeners = UICalendarView.GotoDateActionListener.class),
      @EventConfig(listeners = UICalendarView.AddCategoryActionListener.class),
      @EventConfig(listeners = UIWeekView.MoveNextActionListener.class), 
      @EventConfig(listeners = UICalendarView.SwitchViewActionListener.class),
      @EventConfig(listeners = UIWeekView.MovePreviousActionListener.class)
    }

)
public class UIWeekView extends UICalendarView {

  protected Map<String, List<CalendarEvent>> eventData_ = new HashMap<String, List<CalendarEvent>>() ;
  protected Map<String, CalendarEvent> allWeekData_ = new HashMap<String,  CalendarEvent>() ;
  protected  List<CalendarEvent> daysData_  = new ArrayList<CalendarEvent>() ;

  public UIWeekView() throws Exception {
    super() ;
  }

  public void refresh() throws Exception {
    int week = getCurrentWeek() ;
    eventData_.clear() ;
    allWeekData_.clear() ;
    for(Calendar c : getDaysOfWeek(week)) {
      List<CalendarEvent> list = new ArrayList<CalendarEvent>() ;
      String key = keyGen(c.get(Calendar.DATE), c.get(Calendar.MONTH), c.get(Calendar.YEAR)) ;
      eventData_.put(key, list) ;
    }
    CalendarService calendarService = getApplicationComponent(CalendarService.class) ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    EventQuery eventQuery = new EventQuery() ;
    List<Calendar> days =  getDaysOfWeek(week) ;
    java.util.Calendar fromcalendar = days.get(0) ;
    fromcalendar.set(Calendar.HOUR, 0) ;
    System.out.println("\n\n from " + fromcalendar.getTime()); 
    eventQuery.setFromDate(fromcalendar) ;
    java.util.Calendar tocalendar = days.get(days.size() - 1) ;
    tocalendar.set(Calendar.HOUR, 0) ;
    tocalendar.add(Calendar.DATE, 1) ;
    System.out.println("\n\n to " + tocalendar.getTime()); 
    eventQuery.setToDate(tocalendar) ;
    List<CalendarEvent> allEvents = calendarService.getUserEvents(username, eventQuery);    
    allEvents.addAll(calendarService.getPublicEvents(eventQuery))  ;
    System.out.println("\n\n query result " + allEvents.size());
    Iterator iter = allEvents.iterator() ;
    while(iter.hasNext()) {
      CalendarEvent event = (CalendarEvent)iter.next() ;
      Date beginEvent = event.getFromDateTime() ;
      Date endEvent = event.getToDateTime() ;
      for(Calendar c : getDaysOfWeek(week)) {
        String key = keyGen(c.get(Calendar.DATE), c.get(Calendar.MONTH), c.get(Calendar.YEAR)) ;
        if(isSameDate(c.getTime(), beginEvent) &&  isSameDate(c.getTime(), endEvent)) { 
          eventData_.get(key).add(event) ;
          iter.remove() ;
        } 
      }
    }
    for( CalendarEvent ce : allEvents) {
      allWeekData_.put(ce.getId(), ce) ;
    }
  }
  protected void moveTo(int weeks) {
    calendar_.add(Calendar.WEEK_OF_YEAR, weeks) ;
  }

  protected List<Calendar> getDaysOfWeek(int week) {
    List<Calendar> calendarData = new ArrayList<Calendar>() ;
    Calendar cl = GregorianCalendar.getInstance() ;
    cl.set(Calendar.WEEK_OF_YEAR, week) ;
    int day = cl.get(Calendar.DATE) ;
    int month = cl.get(Calendar.MONTH) ;
    int year = cl.get(Calendar.YEAR) ;
    int amount = cl.getFirstDayOfWeek() - cl.get(Calendar.DAY_OF_WEEK) ;
    cl = getDateByValue(year, month, day, UICalendarView.TYPE_DATE, amount) ;
    calendarData.add(cl) ;
    day = cl.get(Calendar.DATE) ;
    month = cl.get(Calendar.MONTH) ;
    year = cl.get(Calendar.YEAR) ;
    for(int d = 1 ;  d < 7 ; d++) {
      calendarData.add(getDateByValue(year, month, day, UICalendarView.TYPE_DATE, d)) ;
    }
    return calendarData ;
  }

  protected Map<String, List<CalendarEvent>> getEventData() {return eventData_ ;}

  protected Map<String, CalendarEvent>  getEventList() {
    return allWeekData_ ;
  }

  static  public class MoveNextActionListener extends EventListener<UIWeekView> {
    public void execute(Event<UIWeekView> event) throws Exception {
      UIWeekView calendarview = event.getSource() ;
      calendarview.moveTo(1) ;
      calendarview.refresh() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
    }
  }

  static  public class MovePreviousActionListener extends EventListener<UIWeekView> {
    public void execute(Event<UIWeekView> event) throws Exception {
      UIWeekView calendarview = event.getSource() ;
      calendarview.moveTo(-1) ;
      calendarview.refresh() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
    }
  }
}
