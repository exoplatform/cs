/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.form.UIFormCheckBoxInput;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(   
    template = "app:/templates/calendar/webui/UIListView.gtmpl",
    lifecycle = UIFormLifecycle.class,
    events = {
      @EventConfig(listeners = UICalendarView.RefreshActionListener.class),
      @EventConfig(listeners = UICalendarView.AddEventActionListener.class),      
      @EventConfig(listeners = UICalendarView.DeleteEventActionListener.class),
      @EventConfig(listeners = UICalendarView.ChangeCategoryActionListener.class), 
      @EventConfig(listeners = UICalendarView.AddCategoryActionListener.class)
    }
)
public class UIListView extends UICalendarView {
  public UIListView() throws Exception{
    super() ;
  } 

  public void refresh() {}
  protected List<CalendarEvent> getEvents() throws Exception {
    CalendarService calendarService = getApplicationComponent(CalendarService.class) ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    EventQuery eventQuery = new EventQuery() ;
    java.util.Calendar fromcalendar = new GregorianCalendar(getCurrentYear(), getCurrentMonth(), getCurrentDay()) ;
    fromcalendar.set(java.util.Calendar.HOUR, 0) ;
    fromcalendar.set(java.util.Calendar.MINUTE, 0) ;
    fromcalendar.set(java.util.Calendar.MILLISECOND, 0) ;
    eventQuery.setFromDate(fromcalendar) ;
    java.util.Calendar tocalendar = new GregorianCalendar(getCurrentYear(), getCurrentMonth(), getCurrentDay()) ;
    tocalendar.set(java.util.Calendar.HOUR, tocalendar.getActualMaximum(java.util.Calendar.HOUR)) ;
    tocalendar.set(java.util.Calendar.MINUTE, tocalendar.getActualMaximum(java.util.Calendar.MINUTE)) ;
    tocalendar.set(java.util.Calendar.MILLISECOND, tocalendar.getActualMaximum(java.util.Calendar.MILLISECOND)) ;
    eventQuery.setToDate(tocalendar) ;
    
    System.out.println("\n\n formDate "+fromcalendar.getTime());
    System.out.println("\n\n formDate "+tocalendar.getTime());
    
    List<CalendarEvent> allEvents = calendarService.getUserEvents(username, eventQuery);    
    allEvents.addAll(calendarService.getPublicEvents(eventQuery))  ;
    removeChild(UIFormCheckBoxInput.class) ;
    for(CalendarEvent ce : allEvents) {
      addChild(new UIFormCheckBoxInput<Boolean>(ce.getId(), ce.getId(), false)) ;
    } 
    return allEvents ;
  }
  protected boolean isShowEvent() {
  return isShowEvent_ ;}
}
