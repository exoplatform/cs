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

import javax.jcr.PathNotFoundException;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.organization.OrganizationService;
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
      @EventConfig(listeners = UIMonthView.UpdateEventActionListener.class)
    }
)
public class UIMonthView extends UICalendarView {
  private LinkedHashMap<String, CalendarEvent> dataMap_ = new LinkedHashMap<String, CalendarEvent>() ;
  public UIMonthView() throws Exception{
    super() ;
  }

  protected int getWeeksOfTheMonth(int year, int month, int day) {
    Calendar cal = getInstanceTempCalendar() ;
    cal.set(Calendar.YEAR, year);
    cal.set(Calendar.MONTH, month);
    cal.set(Calendar.DATE, day);
    return cal.getActualMaximum(java.util.Calendar.WEEK_OF_MONTH) ;
  }
  public void refresh() throws Exception {
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    String username = CalendarUtils.getCurrentUser() ;
    EventQuery eventQuery = new EventQuery() ;
    eventQuery.setFromDate(getBeginDateOfMonthView()) ;
    eventQuery.setToDate(getEndDateOfMonthView()) ;
    List<CalendarEvent> allEvents = calendarService.getEventsSys( username, eventQuery, getPublicCalendars()) ;
    Iterator childIter = getChildren().iterator() ;
    while(childIter.hasNext()) {
      UIComponent comp = (UIComponent)childIter.next() ;
      if (comp instanceof UIFormCheckBoxInput ) {
        childIter.remove() ;
      }
    }
    dataMap_.clear() ;
    Iterator<CalendarEvent> eventIter = allEvents.iterator() ;
    while(eventIter.hasNext()) {
      CalendarEvent ce = (CalendarEvent)eventIter.next() ; 
      dataMap_.put(ce.getId(), ce) ;
      UIFormCheckBoxInput<Boolean> input = new UIFormCheckBoxInput<Boolean>(ce.getId(), ce.getId(), false) ;
      input.setBindingField(ce.getCalendarId()) ;
      addChild(input) ;
      eventIter.remove() ;
    }
  }
  public java.util.Calendar getBeginDateOfMonthView() throws Exception{
    java.util.Calendar temCal = getBeginDateOfMonth() ;
    temCal.setFirstDayOfWeek(java.util.Calendar.SUNDAY) ;
    int amount = temCal.getFirstDayOfWeek() - temCal.get(java.util.Calendar.DAY_OF_WEEK) ;
    temCal.add(java.util.Calendar.DATE, amount) ;
    return getBeginDay(temCal) ;
  }

  public java.util.Calendar getEndDateOfMonthView() throws Exception{
    java.util.Calendar temCal = getBeginDateOfMonthView() ;
    temCal.setFirstDayOfWeek(java.util.Calendar.SUNDAY) ;
    temCal.add(java.util.Calendar.DATE, getWeeksOfTheMonth(getCurrentYear(), getCurrentMonth(), 1)*7) ;
    return getBeginDay(temCal) ;
  }

  public java.util.Calendar getBeginDateOfMonth() throws Exception{
    java.util.Calendar temCal = getInstanceTempCalendar() ;
    temCal.setTime(calendar_.getTime()) ;
    temCal.setFirstDayOfWeek(java.util.Calendar.SUNDAY) ;
    temCal.set(java.util.Calendar.DATE, 1) ;
    return getBeginDay(temCal) ;
  }

  public java.util.Calendar getEndDateOfMonth() throws Exception{
    java.util.Calendar temCal = getInstanceTempCalendar() ;
    temCal.setTime(calendar_.getTime()) ;
    temCal.setFirstDayOfWeek(java.util.Calendar.SUNDAY) ;
    temCal.set(java.util.Calendar.DATE, getDaysInMonth()) ;
    return getEndDay(temCal) ;
  }

  protected List<CalendarEvent> getSelectedEvents() {
    List<CalendarEvent> events = new ArrayList<CalendarEvent>() ;
    for(String id : dataMap_.keySet()) {
      UIFormCheckBoxInput<Boolean>  checkbox = getChildById(id )  ;
      if(checkbox != null && checkbox.isChecked()) events.add(dataMap_.get(id)) ;
    }
    return events ; 
  }
  public LinkedHashMap<String, CalendarEvent> getDataMap() {
    return dataMap_ ;
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
      //String eventId = event.getRequestContext().getRequestParameter(EVENTID) ;
      //String calendarId = event.getRequestContext().getRequestParameter(CALENDARID) ;
      CalendarService calService = CalendarUtils.getCalendarService() ;
      try {
        List<CalendarEvent> list = calendarview.getSelectedEvents() ;
        List<CalendarEvent> dataList = new ArrayList<CalendarEvent>(){} ;
        java.util.Calendar tempCalFrom = calendarview.getInstanceTempCalendar() ;
        tempCalFrom.setTimeInMillis((Long.parseLong(value))) ;
        java.util.Calendar cal = CalendarUtils.getInstanceTempCalendar() ;
        for(CalendarEvent ce : list) {
          String calendarId = ce.getCalendarId() ;
          if(ce != null) {
            org.exoplatform.calendar.service.Calendar calendar = null ;
            if(ce.getCalType().equals(CalendarUtils.PRIVATE_TYPE)) {
              calendar = calService.getUserCalendar(calendarview.getSession(), username, calendarId) ;
            } else if(ce.getCalType().equals(CalendarUtils.SHARED_TYPE)){
              if(calService.getSharedCalendars(SessionProviderFactory.createSystemProvider(), username, true) != null)
                calendar = calService.getSharedCalendars(SessionProviderFactory.createSystemProvider(), username, true).getCalendarById(calendarId) ;
            } else if(ce.getCalType().equals(CalendarUtils.PUBLIC_TYPE)) {
              calendar = calService.getGroupCalendar(SessionProviderFactory.createSystemProvider(), calendarId) ;
            }
            if(calendar == null) {
              continue ;
            } else {
              if(!CalendarUtils.PRIVATE_TYPE.equals(ce.getCalType()) && !CalendarUtils.canEdit(CalendarUtils.getOrganizationService(), calendar.getEditPermission(), username)) {
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
      CalendarSetting setting = calService.getCalendarSetting(calendarview.getSession(), username) ;
      uiViewContainer.refresh() ;
      uiPortlet.setCalendarSetting(setting) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
    }
  }
}
