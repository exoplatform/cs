/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
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
import org.exoplatform.webui.form.UIFormCheckBoxInput;

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
      @EventConfig(listeners = UIWeekView.MoveNextActionListener.class), 
      @EventConfig(listeners = UIWeekView.MovePreviousActionListener.class)
    }

)
public class UIWeekView extends UICalendarView {

  protected Map<String, List<CalendarEvent>> eventData_ = new HashMap<String, List<CalendarEvent>>() ;
  public UIWeekView() throws Exception {
    super() ;
    refresh() ;
  }

  public void refresh() throws Exception {
    int week = getCurrentWeek() ;
    eventData_.clear() ;
    CalendarService calendarService = getApplicationComponent(CalendarService.class) ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    EventQuery eventQuery = new EventQuery() ;
    java.util.Calendar fromcalendar = getDaysOfWeek(week).get(0) ;
    eventQuery.setFromDate(fromcalendar) ;
    java.util.Calendar tocalendar = getDaysOfWeek(week).get(getDaysOfWeek(week).size()-1) ;
    eventQuery.setToDate(tocalendar) ;
    List<CalendarEvent> allEvents = calendarService.getUserEvents(username, eventQuery);    
    allEvents.addAll(calendarService.getPublicEvents(eventQuery))  ;

    removeChild(UIFormCheckBoxInput.class) ;

    for(Calendar c : getDaysOfWeek(week)) {
      List<CalendarEvent> existEvents = new ArrayList<CalendarEvent>() ;
      for(CalendarEvent ce : allEvents) {
        java.util.Calendar fromDate = new GregorianCalendar() ;
        fromDate.setTime(ce.getFromDateTime()) ;
        java.util.Calendar endDate = new GregorianCalendar() ;
        endDate.setTime(ce.getToDateTime()) ;
        if((fromDate.before(c) && endDate.after(c))||
            (isSameDate(c, fromDate)) || 
            (isSameDate(c, endDate))) {
          existEvents.add(ce) ;
          addChild(new UIFormCheckBoxInput<Boolean>(ce.getId(), ce.getId(), false)) ;
        } 
      }
      String key = keyGen(c.get(Calendar.DATE), c.get(Calendar.MONTH), c.get(Calendar.YEAR)) ;
      eventData_.put(key, existEvents) ;
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
  
  private Map<String, List<CalendarEvent>> getEventData() {return eventData_ ;}

  private List getEventList() {
    return null ;
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
