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
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
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
      @EventConfig(listeners = UICalendarView.AddEventActionListener.class),      
      @EventConfig(listeners = UICalendarView.DeleteEventActionListener.class, confirm="UICalendarView.msg.confirm-delete"),
      @EventConfig(listeners = UICalendarView.ChangeCategoryActionListener.class), 
      @EventConfig(listeners = UICalendarView.EventSelectActionListener.class), 
      @EventConfig(listeners = UICalendarView.AddCategoryActionListener.class),
      @EventConfig(listeners = UICalendarView.ViewActionListener.class),
      @EventConfig(listeners = UICalendarView.EditActionListener.class), 
      @EventConfig(listeners = UICalendarView.DeleteActionListener.class, confirm="UICalendarView.msg.confirm-delete"),
      @EventConfig(listeners = UICalendarView.GotoDateActionListener.class), 
      @EventConfig(listeners = UICalendarView.QuickAddActionListener.class), 
      @EventConfig(listeners = UICalendarView.MoveNextActionListener.class), 
      @EventConfig(listeners = UICalendarView.MovePreviousActionListener.class),
      @EventConfig(listeners = UICalendarView.SwitchViewActionListener.class),
      @EventConfig(listeners = UICalendarView.ExportEventActionListener.class),
      @EventConfig(listeners = UICalendarView.MoveEventActionListener.class),
      @EventConfig(listeners = UIMonthView.UpdateEventActionListener.class),
      @EventConfig(listeners = UICalendarView.ConfirmDeleteOnlyInstance.class),
      @EventConfig(listeners = UICalendarView.ConfirmDeleteAllSeries.class),
      @EventConfig(listeners = UICalendarView.ConfirmDeleteCancel.class)
    }
)
public class UIMonthView extends UICalendarView {
  private LinkedHashMap<String, CalendarEvent> dataMap_ = new LinkedHashMap<String, CalendarEvent>() ;
  private List<CalendarEvent> eventData_ = new ArrayList<CalendarEvent>();
  public UIMonthView() throws Exception{
    super() ;
  }

  protected int getWeeksOfTheMonth(int year, int month, int day) {
    Calendar cal = getInstanceTempCalendar() ;
    cal.set(Calendar.YEAR, year);
    cal.set(Calendar.MONTH, month);
    cal.set(Calendar.DATE, day);
    cal.setMinimalDaysInFirstWeek(1);
    return cal.getActualMaximum(java.util.Calendar.WEEK_OF_MONTH) ;
  }
  public void refresh() throws Exception {
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    String username = CalendarUtils.getCurrentUser() ;
    EventQuery eventQuery = new EventQuery() ;
    eventQuery.setFromDate(getBeginDateOfMonthView()) ;
    eventQuery.setToDate(getEndDateOfMonthView()) ;
    eventQuery.setExcludeRepeatEvent(true);
    List<CalendarEvent> allEvents = calendarService.getEvents(username, eventQuery, getPublicCalendars()) ;
    
    String timezone = CalendarUtils.getCurrentUserCalendarSetting().getTimeZone();
    List<CalendarEvent> originalRecurEvents = calendarService.getOriginalRecurrenceEvents(username, eventQuery.getFromDate(), eventQuery.getToDate(), getPublicCalendars());    
    if (originalRecurEvents != null && originalRecurEvents.size() > 0) {
      Iterator<CalendarEvent> recurEventsIter = originalRecurEvents.iterator();
      while (recurEventsIter.hasNext()) {
        CalendarEvent recurEvent = recurEventsIter.next();
        Map<String,CalendarEvent> tempMap = calendarService.getOccurrenceEvents(recurEvent, eventQuery.getFromDate(), eventQuery.getToDate(), timezone);
        if (tempMap != null) {
          recurrenceEventsMap.put(recurEvent.getId(), tempMap);
          allEvents.addAll(tempMap.values());
        }
      }
    }
    
    Iterator childIter = getChildren().iterator() ;
    while(childIter.hasNext()) {
      UIComponent comp = (UIComponent)childIter.next() ;
      if (comp instanceof UIFormCheckBoxInput ) {
        childIter.remove() ;
      }
    }
    dataMap_.clear() ;
    eventData_.clear();
    Iterator<CalendarEvent> eventIter = allEvents.iterator() ;
    
    while(eventIter.hasNext()) {
      CalendarEvent event = eventIter.next();      
      dataMap_.put(event.getId(), event) ;
      eventData_.add(event);
      // if event is a occurrence
      UIFormCheckBoxInput<Boolean> input;
      if (!CalendarEvent.RP_NOREPEAT.equals(event.getRepeatType()) && !CalendarUtils.isEmpty(event.getRecurrenceId())) {
        input = new UIFormCheckBoxInput<Boolean>(getCheckboxId(event), getCheckboxId(event), false) ;
      } else {
        input = new UIFormCheckBoxInput<Boolean>(event.getId(), event.getId(), false) ;
      }
      input.setBindingField(event.getCalendarId()) ;
      addChild(input) ;
      
      eventIter.remove() ;
    }  
  }
  
  public String getCheckboxId(CalendarEvent event) throws Exception {
    if (!CalendarEvent.RP_NOREPEAT.equals(event.getRepeatType()) && !CalendarUtils.isEmpty(event.getRecurrenceId())) {
      return event.getId() + "-" + event.getRecurrenceId();
    }
    else return event.getId();
  }
  
  public java.util.Calendar getBeginDateOfMonthView() throws Exception{
    java.util.Calendar temCal = getBeginDateOfMonth() ;
    temCal.setFirstDayOfWeek(Integer.parseInt(calendarSetting_.getWeekStartOn())) ;
    int amount = temCal.getFirstDayOfWeek() - temCal.get(java.util.Calendar.DAY_OF_WEEK) ;
    temCal.add(java.util.Calendar.DATE, amount) ;
    while ((temCal.get(Calendar.DATE) < 20) && (temCal.get(Calendar.DATE) != 1)) {
      temCal.add(Calendar.DATE, -7); 
    }
    return getBeginDay(temCal) ;
  }

  public java.util.Calendar getEndDateOfMonthView() throws Exception{
    java.util.Calendar temCal = getBeginDateOfMonthView() ;
    temCal.setFirstDayOfWeek(Integer.parseInt(calendarSetting_.getWeekStartOn())) ;
    temCal.add(java.util.Calendar.DATE, getWeeksOfTheMonth(getCurrentYear(), getCurrentMonth(), 1)*7) ;
    return getBeginDay(temCal) ;
  }

  public java.util.Calendar getBeginDateOfMonth() throws Exception{
    java.util.Calendar temCal = getInstanceTempCalendar() ;
    temCal.setTime(calendar_.getTime()) ;
    temCal.set(java.util.Calendar.DATE, 1) ;
    return getBeginDay(temCal) ;
  }

  public java.util.Calendar getEndDateOfMonth() throws Exception{
    java.util.Calendar temCal = getInstanceTempCalendar() ;
    temCal.setTime(calendar_.getTime()) ;
    temCal.set(java.util.Calendar.DATE, getDaysInMonth()) ;
    return getEndDay(temCal) ;
  }

  protected List<CalendarEvent> getSelectedEvents() {
    List<CalendarEvent> events = new ArrayList<CalendarEvent>() ;
    UIFormCheckBoxInput<Boolean>  checkbox;
    for(String id : dataMap_.keySet()) {
      checkbox = getChildById(id )  ;
      if(checkbox != null && checkbox.isChecked()) events.add(dataMap_.get(id)) ;
    }
    
    if (recurrenceEventsMap.isEmpty()) return events;
    
    // get all selected events with occurrence event
    Iterator<String> occurIter = recurrenceEventsMap.keySet().iterator();
    while (occurIter.hasNext()) {
      String eventId = occurIter.next();
      Iterator<String> recurIdIter = recurrenceEventsMap.get(eventId).keySet().iterator();
      while (recurIdIter.hasNext()) {
        String recurId = recurIdIter.next();
        checkbox = getChildById(eventId + "-" + recurId);
        if(checkbox != null && checkbox.isChecked()) events.add(recurrenceEventsMap.get(eventId).get(recurId)) ;
      }
    }
    
    return events ; 
  }
  
  public LinkedHashMap<String, CalendarEvent> getDataMap() {
    return dataMap_ ;
  }
  
  protected List<CalendarEvent> getEventData() {
    return eventData_;
  }
  
  static  public class ChangeViewActionListener extends EventListener<UIMonthView> {
    public void execute(Event<UIMonthView> event) throws Exception {
      UIMonthView calendarview = event.getSource() ;
      UICalendarViewContainer uiContainer = calendarview.getAncestorOfType(UICalendarViewContainer.class) ;
      uiContainer.setRenderedChild(UIDayView.class) ;
      uiContainer.refresh() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
    }
  }
  static  public class UpdateEventActionListener extends EventListener<UIMonthView> {
    public void execute(Event<UIMonthView> event) throws Exception {
      UIMonthView calendarview = event.getSource() ;
      UICalendarPortlet uiPortlet = calendarview.getAncestorOfType(UICalendarPortlet.class) ;
      String username = CalendarUtils.getCurrentUser() ;
      String value = event.getRequestContext().getRequestParameter(OBJECTID) ;
      
      CalendarService calService = CalendarUtils.getCalendarService() ;
      try {
        List<CalendarEvent> list = calendarview.getSelectedEvents() ;
        List<CalendarEvent> dataList = new ArrayList<CalendarEvent>(){} ;
        List<CalendarEvent> occurrenceList = new ArrayList<CalendarEvent>();
        java.util.Calendar tempCalFrom = calendarview.getInstanceTempCalendar() ;
        tempCalFrom.setTimeInMillis((Long.parseLong(value))) ;
        java.util.Calendar cal = CalendarUtils.getInstanceOfCurrentCalendar() ;
        for(CalendarEvent ce : list) {
          String calendarId = ce.getCalendarId() ;
          if(ce != null) {
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
              continue ;
            } else {
              // cs-4429: fix for group calendar permission
              if((CalendarUtils.SHARED_TYPE.equals(ce.getCalType()) && !CalendarUtils.canEdit(CalendarUtils.getOrganizationService(), Utils.getEditPerUsers(calendar), username)) ||
                 (CalendarUtils.PUBLIC_TYPE.equals(ce.getCalType()) && !CalendarUtils.canEdit(CalendarUtils.getOrganizationService(), calendar.getEditPermission(), username))) 
              {
                continue ;
              }
              CalendarEvent calEvent = ce ;
              Long amount = calEvent.getToDateTime().getTime() - calEvent.getFromDateTime().getTime() ;
              cal.setTime(calEvent.getFromDateTime()) ;
              cal.set(Calendar.DATE, tempCalFrom.get(Calendar.DATE)) ;
              cal.set(Calendar.MONTH, tempCalFrom.get(Calendar.MONTH)) ;
              cal.set(Calendar.YEAR, tempCalFrom.get(Calendar.YEAR)) ;
              calEvent.setFromDateTime(cal.getTime()) ;
              cal.setTimeInMillis(calEvent.getFromDateTime().getTime() + amount) ;
              calEvent.setToDateTime(cal.getTime()) ;
              dataList.add(calEvent) ;
            }
          }
        }
        if(!dataList.isEmpty()) calendarview.moveEvents(dataList, null, null) ;
        if(dataList.size() < list.size()) {
          int number = list.size() - dataList.size() ;
          UIApplication uiApp = calendarview.getAncestorOfType(UIApplication.class) ;
          uiApp.addMessage(new ApplicationMessage("UICalendarView.msg.some-events-cannot-move", new Object[]{String.valueOf(number)}, 1)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          calendarview.refresh() ;
          event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
        }
         

      } catch (PathNotFoundException e) {
        e.printStackTrace() ;
        UIApplication uiApp = calendarview.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UICalendars.msg.have-no-calendar", null, 1)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      }
      UIMiniCalendar uiMiniCalendar = uiPortlet.findFirstComponentOfType(UIMiniCalendar.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMiniCalendar) ;
      UICalendarViewContainer uiViewContainer = uiPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
      CalendarSetting setting = calService.getCalendarSetting(username) ;
      uiViewContainer.refresh() ;
      uiPortlet.setCalendarSetting(setting) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
    }
  }
  
  @Override
  public String getDefaultStartTimeOfEvent() {
    if (isCurrentMonth(calendar_.get(Calendar.MONTH), calendar_.get(Calendar.YEAR))) {
      // if selected month is current month, the start time is present
      return String.valueOf(System.currentTimeMillis());
    } else {
      Calendar c = Calendar.getInstance();
      c.setTime(calendar_.getTime());
      int month = c.get(Calendar.MONTH);
      while (c.get(Calendar.MONTH) == month) {
        c.add(Calendar.DATE, 1);
      }
      c.add(Calendar.DATE, -1);
      return String.valueOf(c.getTimeInMillis());
    }
  }
}
