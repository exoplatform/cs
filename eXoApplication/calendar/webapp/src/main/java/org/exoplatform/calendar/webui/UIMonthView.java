/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormCheckBoxInput;

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
      @EventConfig(listeners = UIMonthView.SaveEventActionListener.class), 
      @EventConfig(listeners = UICalendarView.EventSelectActionListener.class), 
      @EventConfig(listeners = UICalendarView.AddCategoryActionListener.class)
    }

)
public class UIMonthView extends UICalendarView {

  private Map<String, String> calendarIds_ = new HashMap<String, String>() ;

  private Map<String, List<CalendarEvent>> eventData_ = new HashMap<String, List<CalendarEvent>>() ;

  public UIMonthView() throws Exception{
    super() ;
    refresh() ;
  }
  
  protected int getWeeksOfTheMonth(int year, int month, int day) {
    return new GregorianCalendar(year, month, day).getActualMaximum(java.util.Calendar.WEEK_OF_MONTH) ;
  }
  
  protected void refreshEvents() throws Exception {
    CalendarService calendarService = getApplicationComponent(CalendarService.class) ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    EventQuery eventQuery = new EventQuery() ;
    java.util.Calendar fromcalendar = new GregorianCalendar(getCurrentYear(), getCurrentMonth(), 1) ;
    eventQuery.setFromDate(fromcalendar) ;
    java.util.Calendar tocalendar = new GregorianCalendar(getCurrentYear(), getCurrentMonth(), getDaysInMonth()) ;
    eventQuery.setToDate(tocalendar) ;
    List<CalendarEvent> allEvents = calendarService.getUserEvents(username, eventQuery);    
    allEvents.addAll(calendarService.getPublicEvents(eventQuery))  ;
    
    removeChild(UIFormCheckBoxInput.class) ;
    for(int day =1 ;  day <= getDaysInMonth(); day++) {
      List<CalendarEvent> existEvents = new ArrayList<CalendarEvent>() ;
      for(CalendarEvent ce : allEvents) {
        java.util.Calendar tempDate = new GregorianCalendar(getCurrentYear(), getCurrentMonth(), day) ;
        java.util.Calendar fromDate = new GregorianCalendar() ;
        fromDate.setTime(ce.getFromDateTime()) ;
        java.util.Calendar endDate = new GregorianCalendar() ;
        endDate.setTime(ce.getToDateTime()) ;
        if((fromDate.before(tempDate) && endDate.after(tempDate))||
            (isSameDate(tempDate, fromDate)) || 
            (isSameDate(tempDate, endDate))) {
          existEvents.add(ce) ;
          addChild(new UIFormCheckBoxInput<Boolean>(ce.getId(), ce.getId(), false)) ;
        } 
      }
      String key = keyGen(day, getCurrentMonth(), getCurrentYear()) ;
      eventData_.put(key, existEvents) ;
    }
  }
  
  protected void addCalendarId(String id) {calendarIds_.put(id,id) ;}
  protected Map<String, String> getCalendarIds() {return calendarIds_ ;}

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
  private Map<String, List<CalendarEvent>> getEventsData() {
    return eventData_ ;
  }

  protected void monthNext(int months) {
    calendar_.add(java.util.Calendar.MONTH, months) ;
  }
  protected void monthBack(int months) {
    calendar_.add(java.util.Calendar.MONTH, months) ;
  }
  protected List<CalendarEvent> getSelectedEvents() {
    List<CalendarEvent> events = new ArrayList<CalendarEvent>() ;
    for(List<CalendarEvent> items : getEventsData().values()) {
      for(CalendarEvent ce : items) {
       UIFormCheckBoxInput<Boolean>  checkbox = getChildById(ce.getId())  ;
       if(checkbox != null && checkbox.isChecked()) events.add(ce) ;
      }
    }
    return events ; 
  }
  static  public class MoveNextActionListener extends EventListener<UIMonthView> {
    public void execute(Event<UIMonthView> event) throws Exception {
      UIMonthView calendarview = event.getSource() ;
      calendarview.monthNext(1) ;
      calendarview.refresh() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
    }
  }
  static  public class MovePreviousActionListener extends EventListener<UIMonthView> {
    public void execute(Event<UIMonthView> event) throws Exception {
      UIMonthView calendarview = event.getSource() ;
      calendarview.monthBack(-1) ;
      calendarview.refresh() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
    }
  }
  static  public class SaveEventActionListener extends EventListener<UIMonthView> {
    public void execute(Event<UIMonthView> event) throws Exception {
      UIMonthView calendarview = event.getSource() ;
      String eventId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String calendarId = event.getRequestContext().getRequestParameter("calendarId") ;
      String startTime = event.getRequestContext().getRequestParameter("startTime") ;
      String endTime = event.getRequestContext().getRequestParameter("finishTime") ;
      System.out.println("\n\n SaveEventActionListener");
      System.out.println("\n\n event " + eventId);
      System.out.println("\n\n begin " + startTime);
      System.out.println("\n\n end " + endTime);
      String username = event.getRequestContext().getRemoteUser() ;
      CalendarService calendarService = calendarview.getApplicationComponent(CalendarService.class) ;
      CalendarEvent ce = calendarService.getUserEvent(username, calendarId, eventId) ;
      calendarview.refresh() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
    }
  }

}
