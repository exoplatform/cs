/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui;

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
    template = "app:/templates/calendar/webui/UIYearView.gtmpl",
    events = {
      @EventConfig(listeners = UICalendarView.GotoDateActionListener.class),
      @EventConfig(listeners = UIYearView.MoveNextActionListener.class), 
      @EventConfig(listeners = UIYearView.MovePreviousActionListener.class),
      @EventConfig(listeners = UICalendarView.AddEventActionListener.class), 
      @EventConfig(listeners = UICalendarView.DeleteEventActionListener.class)
    }

)
public class UIYearView extends UICalendarView {

  private Map<Integer, Map<Integer, Boolean> > yearData_ = new HashMap<Integer, Map<Integer, Boolean>>() ;

  private Map<Integer, Boolean> monthData_ = new HashMap<Integer, Boolean>() ;


  public UIYearView() throws Exception {
    super() ;
  }

  protected void yearNext(int years) {
    calendar_.add(Calendar.YEAR, years) ;
  }
  protected void yearBack(int years) {
    calendar_.add(Calendar.YEAR, years) ;
  }
  public void refresh() throws Exception {
    Calendar cal = new GregorianCalendar(getCurrentYear(), 0, 1, 0, 0, 0) ;
    Calendar cal2 = new GregorianCalendar(getCurrentYear(), 0, 1, 0, 0, 0) ;
    Calendar beginYear = cal ;
    cal2.add(Calendar.YEAR, 1) ;
    Calendar endYear = cal2 ;
    CalendarService calendarService = getApplicationComponent(CalendarService.class) ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    EventQuery eventQuery = new EventQuery() ;
    eventQuery.setFromDate(beginYear) ;
    eventQuery.setToDate(endYear) ;
    List<CalendarEvent> allEvents = calendarService.getUserEvents(username, eventQuery);    
    allEvents.addAll(calendarService.getPublicEvents(eventQuery))  ;
    System.out.println("\n\n  year query result " + allEvents.size());
    yearData_.clear() ;
    monthData_.clear() ;
    for(int month = 0 ; month <= 11; month++){
      for(int i = 1 ; i < getDaysInMonth(getCurrentYear(), month); i++){
        monthData_.put(i, false) ;
      }
      yearData_.put(month, monthData_) ;
    }

  }

  private List getEventList() {
    return null ;
  }

  static  public class MoveNextActionListener extends EventListener<UIYearView> {
    public void execute(Event<UIYearView> event) throws Exception {
      UIYearView calendarview = event.getSource() ;
      calendarview.yearNext(1) ;
      calendarview.refresh() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
    }
  }

  static  public class MovePreviousActionListener extends EventListener<UIYearView> {
    public void execute(Event<UIYearView> event) throws Exception {
      UIYearView calendarview = event.getSource() ;
      calendarview.yearBack(-1) ;
      calendarview.refresh() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
    }
  }

}
