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
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
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
      @EventConfig(listeners = UICalendarView.MoveNextActionListener.class), 
      @EventConfig(listeners = UICalendarView.MovePreviousActionListener.class), 
      @EventConfig(listeners = UIDayView.SaveEventActionListener.class)
    }
)
public class UIDayView extends UICalendarView {

  private Map<String, CalendarEvent> eventData_ = new HashMap<String, CalendarEvent>() ;
  private Map<String, CalendarEvent> allDayEvent_ = new HashMap<String, CalendarEvent>() ;

  public UIDayView() throws Exception{
    super() ;
  }
  @Override
  public void refresh() throws Exception {
    System.out.println("\n\n>>>>>>>>>> DAY VIEW") ;
    eventData_.clear() ;
    allDayEvent_.clear() ;
    Calendar begin = getBeginDay(getCurrentCalendar()) ; //getBeginDay(new GregorianCalendar(getCurrentYear(), getCurrentMonth(), getCurrentDay())) ;
    Calendar end = getEndDay(getCurrentCalendar()) ;//getEndDay(new GregorianCalendar(getCurrentYear(), getCurrentMonth(), getCurrentDay())) ;
    end.add(Calendar.MILLISECOND, -1) ;
    List<CalendarEvent> events = new ArrayList<CalendarEvent>() ;
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    EventQuery eventQuery = new EventQuery() ;
    eventQuery.setFromDate(begin) ;
    eventQuery.setToDate(end) ;
    //eventQuery.setFilterCalendarIds(getFilterCalendarIds()) ;
  /*  System.out.println("\n\n " + begin.getTime());
    System.out.println("\n\n " + end.getTime());
    System.out.println("eventQuery " + eventQuery.getQueryStatement());*/
    events = calendarService.getEvents(getSystemSession(), username, eventQuery, getPublicCalendars()) ;
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
  static  public class SaveEventActionListener extends EventListener<UIDayView> {
    public void execute(Event<UIDayView> event) throws Exception {
      UIDayView calendarview = event.getSource() ;
      calendarview.refresh() ;
      String eventId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String calendarId = event.getRequestContext().getRequestParameter("calendarId") ;
      String startTime = event.getRequestContext().getRequestParameter("startTime") ;
      String endTime = event.getRequestContext().getRequestParameter("finishTime") ;
      String username = event.getRequestContext().getRemoteUser() ;
      CalendarEvent ce = calendarview.eventData_.get(eventId) ;
      if(ce != null) {
        
        if(!ce.getCalType().equals(CalendarUtils.PRIVATE_TYPE)) {
          CalendarService calService = CalendarUtils.getCalendarService() ;
          org.exoplatform.calendar.service.Calendar calendar = null ;
          if(ce.getCalType().equals(CalendarUtils.SHARED_TYPE)){
            calendar = 
            calService.getSharedCalendars(SessionProviderFactory.createSystemProvider(), username, true).getCalendarById(calendarId) ;
          } else if(ce.getCalType().equals(CalendarUtils.PUBLIC_TYPE)) {
            calendar = calService.getGroupCalendar(SessionProviderFactory.createSystemProvider(), calendarId) ;
          }
          if(!CalendarUtils.canEdit(calendarview.getApplicationComponent(OrganizationService.class), calendar.getEditPermission(), CalendarUtils.getCurrentUser())) {
            UIApplication uiApp = calendarview.getAncestorOfType(UIApplication.class) ;
            uiApp.addMessage(new ApplicationMessage("UICalendars.msg.have-no-permission-to-edit", null, 1)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            
            calendarview.refresh() ;
            //calendarview.eventData_.put(ce.getId(), ce) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
            
            return ;
          }
        }
        
        try {
          int hoursBg = (Integer.parseInt(startTime)/60) ;
          int minutesBg = (Integer.parseInt(startTime)%60) ;

          int hoursEnd = (Integer.parseInt(endTime)/60) ;
          int minutesEnd = (Integer.parseInt(endTime)%60) ;
          
          Calendar cal = calendarview.getInstanceTempCalendar()  ; //calendarview.getBeginDay(new GregorianCalendar(calendarview.getCurrentYear(), calendarview.getCurrentMonth(), calendarview.getCurrentDay())) ;
          cal.setTime(calendarview.getCurrentDate()) ;
          //cal.setTimeInMillis(Long.parseLong(startTime)) ;
          cal.set(Calendar.HOUR_OF_DAY, hoursBg) ;
          cal.set(Calendar.MINUTE, minutesBg) ; 
          ce.setFromDateTime(cal.getTime());
          cal.set(Calendar.HOUR_OF_DAY, hoursEnd) ;
          cal.set(Calendar.MINUTE, minutesEnd) ; 
          //cal.setTimeInMillis(Long.parseLong(endTime)) ;
          ce.setToDateTime(cal.getTime()) ;        
          if(ce.getToDateTime().before(ce.getFromDateTime())) {
            //System.out.println("\n\n UIDayView updateEvent to date must after from date");
            return ;
          }
          if(ce.getCalType().equals(CalendarUtils.PRIVATE_TYPE)) {
            CalendarUtils.getCalendarService().saveUserEvent(calendarview.getSession(), username, calendarId, ce, false) ;
          }else if(ce.getCalType().equals(CalendarUtils.SHARED_TYPE)){
            CalendarUtils.getCalendarService().saveEventToSharedCalendar(calendarview.getSystemSession(), username, calendarId, ce, false) ;
          }else if(ce.getCalType().equals(CalendarUtils.PUBLIC_TYPE)){
            CalendarUtils.getCalendarService().savePublicEvent(calendarview.getSystemSession(), calendarId, ce, false) ;          
          }
        } catch (Exception e) {
          e.printStackTrace() ;
        }
      } else  {
        UICalendarWorkingContainer uiWorkingContainer = calendarview.getAncestorOfType(UICalendarWorkingContainer.class) ;
        UIMiniCalendar uiMiniCalendar = uiWorkingContainer.findFirstComponentOfType(UIMiniCalendar.class) ;
        //uiMiniCalendar.updateMiniCal() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingContainer) ;
        UIApplication uiApp = calendarview.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UICalendarView.msg.event-not-found", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      }
      calendarview.setLastUpdatedEventId(eventId) ;
      calendarview.refresh() ;
      //calendarview.eventData_.put(ce.getId(), ce) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
    }
  }
}
