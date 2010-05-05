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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.PathNotFoundException;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.EventQuery;
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
      @EventConfig(listeners = UICalendarView.DeleteEventActionListener.class, confirm="UICalendarView.msg.confirm-delete"),
      @EventConfig(listeners = UICalendarView.AddCategoryActionListener.class),
      @EventConfig(listeners = UICalendarView.ViewActionListener.class),
      @EventConfig(listeners = UICalendarView.EditActionListener.class), 
      @EventConfig(listeners = UICalendarView.DeleteActionListener.class, confirm="UICalendarView.msg.confirm-delete"),
      @EventConfig(listeners = UICalendarView.GotoDateActionListener.class),
      @EventConfig(listeners = UICalendarView.SwitchViewActionListener.class),
      @EventConfig(listeners = UICalendarView.QuickAddActionListener.class), 
      @EventConfig(listeners = UICalendarView.MoveNextActionListener.class), 
      @EventConfig(listeners = UICalendarView.MovePreviousActionListener.class), 
      @EventConfig(listeners = UICalendarView.ExportEventActionListener.class),
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
    eventData_.clear() ;
    allDayEvent_.clear() ;
    Calendar begin = getBeginDay(getCurrentCalendar()) ;  
    Calendar end = getEndDay(getCurrentCalendar()) ; 
    end.add(Calendar.MILLISECOND, -1) ;
    List<CalendarEvent> events = new ArrayList<CalendarEvent>() ;
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    String username = CalendarUtils.getCurrentUser() ;
    EventQuery eventQuery = new EventQuery() ;
    eventQuery.setFromDate(begin) ;
    eventQuery.setToDate(end) ;
    events = calendarService.getEvents(username, eventQuery, getPublicCalendars()) ;
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
  
  public String renderDayViewInTitleBar(String monthOpenTag, String monthCloseTag, 
                                        String yearOpenTag, String yearCloseTag) {
    String formatPattern = "";
    String dateFormat = this.getDateFormat();
    if (dateFormat.equalsIgnoreCase(CalendarUtils.FORMATPATTERN1)) { //dd/MM/yyyy
      formatPattern = "%1$td / %2$s%1$tm%3$s / %4$s%1$tY%5$s";// day/<string>month<string>/<string>year<string>
    } else if (dateFormat.equalsIgnoreCase(CalendarUtils.FORMATPATTERN2)) {// dd-MM-yyyy
      formatPattern = "%1$td - %2$s%1$tm%3$s - %4$s%1$tY%5$s";// day-<string>month<string>-<string>year<string>
    } else if (dateFormat.equalsIgnoreCase(CalendarUtils.FORMATPATTERN3)) { //MM/dd/yyyy
      formatPattern = "%2$s%1$tm%3$s / %1$td / %4$s%1$tY%5$s";// <string>month<string>/day/<string>year<string>
    } else if (dateFormat.equalsIgnoreCase(CalendarUtils.FORMATPATTERN4)) { //MM-dd-yyyy
      formatPattern = "%2$s%1$tm%3$s - %1$td - %4$s%1$tY%5$s";// <string>month<string>/day/<string>year<string>      
    }
    return String.format(formatPattern, this.getCurrentCalendar(), monthOpenTag, monthCloseTag, 
                         yearOpenTag, yearCloseTag);
  }
  
  static  public class SaveEventActionListener extends EventListener<UIDayView> {
    public void execute(Event<UIDayView> event) throws Exception {
      UIDayView calendarview = event.getSource() ;
      UICalendarPortlet uiCalendarPortlet = calendarview.getAncestorOfType(UICalendarPortlet.class) ;
      calendarview.refresh() ;
      String eventId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String calendarId = event.getRequestContext().getRequestParameter("calendarId") ;
      String startTime = event.getRequestContext().getRequestParameter("startTime") ;
      String endTime = event.getRequestContext().getRequestParameter("finishTime") ;
      String username = CalendarUtils.getCurrentUser() ;
      CalendarEvent ce = calendarview.eventData_.get(eventId) ;
      if(ce != null) {
        CalendarService calService = CalendarUtils.getCalendarService() ;
        try {
          org.exoplatform.calendar.service.Calendar calendar = null ;
          if(ce.getCalType().equals(CalendarUtils.PRIVATE_TYPE)) {
            calendar = calService.getUserCalendar(username, calendarId) ;
          } else if(ce.getCalType().equals(CalendarUtils.SHARED_TYPE)){
            if(calService.getSharedCalendars(username, true) != null)
              calendar = calService.getSharedCalendars(username, true).getCalendarById(calendarId) ;
          } else if(ce.getCalType().equals(CalendarUtils.PUBLIC_TYPE)) {
            calendar = calService.getGroupCalendar(calendarId) ;
          }
          if(calendar == null) {
            UIApplication uiApp = calendarview.getAncestorOfType(UIApplication.class) ;
            uiApp.addMessage(new ApplicationMessage("UICalendars.msg.have-no-calendar", null, 1)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          } else {
            if(!ce.getCalType().equals(CalendarUtils.PRIVATE_TYPE) && !CalendarUtils.canEdit(calendarview.getApplicationComponent(OrganizationService.class), calendar.getEditPermission(), CalendarUtils.getCurrentUser())) {
              UIApplication uiApp = calendarview.getAncestorOfType(UIApplication.class) ;
              uiApp.addMessage(new ApplicationMessage("UICalendars.msg.have-no-permission-to-edit-event", null, 1)) ;
              event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
              calendarview.refresh() ;
              event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
              return ;
            }
            int hoursBg = (Integer.parseInt(startTime)/60) ;
            int minutesBg = (Integer.parseInt(startTime)%60) ;
            int hoursEnd = (Integer.parseInt(endTime)/60) ;
            int minutesEnd = (Integer.parseInt(endTime)%60) ;
            Calendar cal = calendarview.getInstanceTempCalendar()  ; 
            cal.setTime(calendarview.getCurrentDate()) ;
            try {
              if(hoursBg < cal.getMinimum(Calendar.HOUR_OF_DAY)) {
                hoursBg = 0 ;
                minutesBg = 0 ;
              }
              cal.set(Calendar.HOUR_OF_DAY, hoursBg) ;
              cal.set(Calendar.MINUTE, minutesBg) ; 
              ce.setFromDateTime(cal.getTime());
              if(hoursEnd >= 24) {
                hoursEnd = 23 ;
                minutesEnd = 59 ;
              }
              cal.set(Calendar.HOUR_OF_DAY, hoursEnd) ;
              cal.set(Calendar.MINUTE, minutesEnd) ; 
              ce.setToDateTime(cal.getTime()) ; 
            } catch (Exception e) {
              e.printStackTrace() ;
              return ;
            }
            if(ce.getToDateTime().before(ce.getFromDateTime())) {
              return ;
            }
            if(ce.getCalType().equals(CalendarUtils.PRIVATE_TYPE)) {
              CalendarUtils.getCalendarService().saveUserEvent(username, calendarId, ce, false) ;
            }else if(ce.getCalType().equals(CalendarUtils.SHARED_TYPE)){
              CalendarUtils.getCalendarService().saveEventToSharedCalendar(username, calendarId, ce, false) ;
            }else if(ce.getCalType().equals(CalendarUtils.PUBLIC_TYPE)){
              CalendarUtils.getCalendarService().savePublicEvent(calendarId, ce, false) ;          
            }
            calendarview.setLastUpdatedEventId(eventId) ;
            calendarview.refresh() ;
            event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
          }

        } catch (PathNotFoundException e) {
          e.printStackTrace() ;
          UIApplication uiApp = calendarview.getAncestorOfType(UIApplication.class) ;
          uiApp.addMessage(new ApplicationMessage("UICalendars.msg.have-no-calendar", null, 1)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        }
        UICalendarViewContainer uiViewContainer = uiCalendarPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
        CalendarSetting setting = calService.getCalendarSetting(username) ;
        uiViewContainer.refresh() ;
        uiCalendarPortlet.setCalendarSetting(setting) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendarPortlet) ;
      } else  {
        UICalendarWorkingContainer uiWorkingContainer = calendarview.getAncestorOfType(UICalendarWorkingContainer.class) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingContainer) ;
        UIApplication uiApp = calendarview.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UICalendarView.msg.event-not-found", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      }
    }
  }
}
