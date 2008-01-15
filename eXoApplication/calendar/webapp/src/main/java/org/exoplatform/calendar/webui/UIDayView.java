/**
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 **/
package org.exoplatform.calendar.webui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.SessionsUtils;
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
      @EventConfig(listeners = UICalendarView.AddEventActionListener.class),  
      @EventConfig(listeners = UICalendarView.DeleteEventActionListener.class, confirm="UICalendarVIew.msg.confirm-delete"),
      @EventConfig(listeners = UICalendarView.AddCategoryActionListener.class),
      @EventConfig(listeners = UICalendarView.ChangeCategoryActionListener.class), 
      @EventConfig(listeners = UICalendarView.ViewActionListener.class),
      @EventConfig(listeners = UICalendarView.EditActionListener.class), 
      @EventConfig(listeners = UICalendarView.DeleteActionListener.class, confirm="UICalendarVIew.msg.confirm-delete"),
      @EventConfig(listeners = UICalendarView.GotoDateActionListener.class),
      @EventConfig(listeners = UICalendarView.SwitchViewActionListener.class),
      @EventConfig(listeners = UICalendarView.QuickAddActionListener.class), 
      @EventConfig(listeners = UIDayView.MoveNextActionListener.class), 
      @EventConfig(listeners = UIDayView.MovePreviousActionListener.class), 
      @EventConfig(listeners = UIDayView.SaveEventActionListener.class)
    }
)
public class UIDayView extends UICalendarView {

  private Map<String, CalendarEvent> eventData_ = new HashMap<String, CalendarEvent>() ;
  private Map<String, CalendarEvent> allDayEvent_ = new HashMap<String, CalendarEvent>() ;

  public UIDayView() throws Exception{
    super() ;
  }
  protected void moveDateTo(int days) {
    calendar_.add(Calendar.DATE, days) ;
  }

  @Override
  public void refresh() throws Exception {
    System.out.println("\n\n>>>>>>>>>> DAY VIEW") ;
    eventData_.clear() ;
    allDayEvent_.clear() ;
    Calendar begin = getBeginDay(new GregorianCalendar(getCurrentYear(), getCurrentMonth(), getCurrentDay())) ;
    Calendar end = getEndDay(new GregorianCalendar(getCurrentYear(), getCurrentMonth(), getCurrentDay())) ;
    end.add(Calendar.MILLISECOND, -1) ;
    List<CalendarEvent> events = new ArrayList<CalendarEvent>() ;
    CalendarService calendarService = getApplicationComponent(CalendarService.class) ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    EventQuery eventQuery = new EventQuery() ;
    eventQuery.setFromDate(begin) ;
    eventQuery.setToDate(end) ;
    events = calendarService.getEvent(SessionsUtils.getSystemProvider(), username, eventQuery, getPublicCalendars()) ;
    Iterator<CalendarEvent> iter = events.iterator() ;
    while (iter.hasNext()) {
      CalendarEvent ce = iter.next() ;
      long eventAmount = ce.getToDateTime().getTime() - ce.getFromDateTime().getTime() ;
      if (isSameDate(ce.getFromDateTime(), getCurrentDate())
          && isSameDate(ce.getToDateTime(), getCurrentDate())
          && eventAmount < CalendarUtils.MILISECONS_OF_DAY) {
        eventData_.put(ce.getId(), ce) ;
        iter.remove() ;
      } 
    }
    /*iter = events.iterator() ;
    while (iter.hasNext()) {
      CalendarEvent ce = iter.next() ;
      if(ce.getFromDateTime().equals(begin.getTime()) && ce.getToDateTime().before(end.getTime())) {
        eventData_.put(ce.getId(), ce) ;
        iter.remove() ;
      } 
    }
    iter = events.iterator() ;
    while (iter.hasNext()) {
      CalendarEvent ce = iter.next() ;
      if(ce.getFromDateTime().after(begin.getTime()) && ce.getToDateTime().equals(end.getTime())) {
        eventData_.put(ce.getId(), ce) ;
        iter.remove() ;
      } 
    }*/
    for(CalendarEvent ce : events){
      allDayEvent_.put(ce.getId(), ce) ;
    } 
  }
  protected Map<String, CalendarEvent> getEventData() {return eventData_ ;}
  protected Map<String, CalendarEvent> getAllDayEvents() {return allDayEvent_ ;} ;

  public LinkedHashMap<String, CalendarEvent> getDataMap() {
    LinkedHashMap<String, CalendarEvent> dataMap = new LinkedHashMap<String, CalendarEvent>() ;
    dataMap.putAll(eventData_) ;
    dataMap.putAll(allDayEvent_) ;
    return dataMap ;
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

  static  public class SaveEventActionListener extends EventListener<UIDayView> {
    public void execute(Event<UIDayView> event) throws Exception {
      UIDayView calendarview = event.getSource() ;
      String eventId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String calendarId = event.getRequestContext().getRequestParameter("calendarId") ;
      String startTime = event.getRequestContext().getRequestParameter("startTime") ;
      String endTime = event.getRequestContext().getRequestParameter("finishTime") ;
      String username = event.getRequestContext().getRemoteUser() ;
      CalendarEvent ce = calendarview.eventData_.get(eventId) ;
      if(ce != null) {
        try {
          int hoursBg = (Integer.parseInt(startTime)/60) ;
          int minutesBg = (Integer.parseInt(startTime)%60) ;

          int hoursEnd = (Integer.parseInt(endTime)/60) ;
          int minutesEnd = (Integer.parseInt(endTime)%60) ;
          Calendar cal = calendarview.getBeginDay(new GregorianCalendar(calendarview.getCurrentYear(), calendarview.getCurrentMonth(), calendarview.getCurrentDay())) ;
          cal.set(Calendar.HOUR_OF_DAY, hoursBg) ;
          cal.set(Calendar.MINUTE, minutesBg) ;
          ce.setFromDateTime(cal.getTime());
          cal.set(Calendar.HOUR_OF_DAY, hoursEnd) ;
          cal.set(Calendar.MINUTE, minutesEnd) ;
          ce.setToDateTime(cal.getTime()) ;        
          if(ce.getToDateTime().before(ce.getFromDateTime())) {
            System.out.println("\n\n UIDayView updateEvent to date must after from date");
            return ;
          }
          if(ce.getCalType().equals(CalendarUtils.PRIVATE_TYPE)) {
            CalendarUtils.getCalendarService().saveUserEvent(SessionsUtils.getSessionProvider(), username, calendarId, ce, false) ;
          }else if(ce.getCalType().equals(CalendarUtils.SHARED_TYPE)){
            CalendarUtils.getCalendarService().saveEventToSharedCalendar(SessionsUtils.getSystemProvider(), username, calendarId, ce, false) ;
          }else if(ce.getCalType().equals(CalendarUtils.PUBLIC_TYPE)){
            CalendarUtils.getCalendarService().savePublicEvent(SessionsUtils.getSystemProvider(), calendarId, ce, false) ;          
          }
        } catch (Exception e) {
          e.printStackTrace() ;
        }
      }
      calendarview.setLastUpdatedEventId(eventId) ;
      calendarview.refresh() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
    }
  }
}
