/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui;

import java.text.DecimalFormat;
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

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/calendar/webui/UIDayView.gtmpl", 
    events = {
      @EventConfig(listeners = UICalendarView.RefreshActionListener.class),
      @EventConfig(listeners = UICalendarView.AddEventActionListener.class),      
      @EventConfig(listeners = UICalendarView.DeleteEventActionListener.class),
      @EventConfig(listeners = UICalendarView.ChangeCategoryActionListener.class), 
      @EventConfig(listeners = UIDayView.MoveNextActionListener.class), 
      @EventConfig(listeners = UIDayView.MovePreviousActionListener.class), 
      @EventConfig(listeners = UICalendarView.AddCategoryActionListener.class)
    }

)
public class UIDayView extends UICalendarView {

  private Map<String, CalendarEvent> eventData_ = new HashMap<String, CalendarEvent>() ;
  private Map<String, CalendarEvent> allDayEvent_ = new HashMap<String, CalendarEvent>() ;
  public UIDayView() throws Exception{
    super() ;
    refresh() ;
  }
  protected void moveDateTo(int days) {
    calendar_.add(Calendar.DATE, days) ;
  }
  
  @Override
  public void refresh() throws Exception {
    eventData_.clear() ;
    allDayEvent_.clear() ;
    Calendar begin = getCurrentDayBegin() ;
    Calendar end = getCurrentDayEnd() ;
    List<CalendarEvent> events = new ArrayList<CalendarEvent>() ;
    CalendarService calendarService = getApplicationComponent(CalendarService.class) ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    EventQuery eventQuery = new EventQuery() ;
    eventQuery.setFromDate(begin) ;
    eventQuery.setToDate(end) ;
    events = calendarService.getUserEvents(username, eventQuery) ;
    events.addAll(calendarService.getPublicEvents(eventQuery)) ;
    System.out.println("\n\n events " + events.size());
    for(CalendarEvent ce : events){
      if(ce.getFromDateTime().before(begin.getTime()) && ce.getToDateTime().after(end.getTime())) {
        allDayEvent_.put(ce.getId(),ce) ;
      } else {
        eventData_.put(ce.getId(), ce) ;
      }
    }
  }
  protected Map<String, CalendarEvent> getEventData() {return eventData_ ;}
  protected Map<String, CalendarEvent> getAllDayEvents() {return allDayEvent_ ;} ;
  
  protected Calendar getCurrentDayEnd()  {
    Calendar toDate = new GregorianCalendar(getCurrentYear(), getCurrentMonth(), getCurrentDay()) ;
    toDate.set(Calendar.HOUR, toDate.getActualMaximum(Calendar.HOUR)-1) ;
    toDate.set(Calendar.MINUTE, toDate.getActualMaximum(Calendar.MINUTE)) ;
    toDate.set(Calendar.SECOND, toDate.getActualMaximum(Calendar.SECOND)) ;
    toDate.set(Calendar.MILLISECOND, toDate.getActualMaximum(Calendar.MILLISECOND)) ;
    return toDate ;
  }
  protected Calendar getCurrentDayBegin() {
    Calendar fromDate = new GregorianCalendar(getCurrentYear(), getCurrentMonth(), getCurrentDay()) ;
    fromDate.set(Calendar.HOUR, fromDate.getActualMinimum(Calendar.HOUR)) ;
    fromDate.set(Calendar.MINUTE, fromDate.getActualMinimum(Calendar.MINUTE)) ;
    fromDate.set(Calendar.SECOND, fromDate.getActualMinimum(Calendar.SECOND)) ;
    fromDate.set(Calendar.MILLISECOND, fromDate.getActualMinimum(Calendar.MILLISECOND)) ;
    return fromDate ;
  }
  
  static  public class MoveNextActionListener extends EventListener<UIDayView> {
    public void execute(Event<UIDayView> event) throws Exception {
      UIDayView calendarview = event.getSource() ;
      calendarview.moveDateTo(1) ;
      calendarview.refresh() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
    }
  }

  static  public class MovePreviousActionListener extends EventListener<UIDayView> {
    public void execute(Event<UIDayView> event) throws Exception {
      UIDayView calendarview = event.getSource() ;
      calendarview.moveDateTo(-1) ;
      calendarview.refresh() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
    }
  }

}
