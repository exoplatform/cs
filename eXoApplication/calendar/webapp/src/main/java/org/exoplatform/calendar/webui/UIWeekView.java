/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.webui.popup.UIPopupAction;
import org.exoplatform.calendar.webui.popup.UIQuickAddEvent;
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
    lifecycle =UIFormLifecycle.class,
    template = "app:/templates/calendar/webui/UIWeekView.gtmpl",
    events = {
      @EventConfig(listeners = UICalendarView.AddEventActionListener.class),  
      @EventConfig(listeners = UICalendarView.DeleteEventActionListener.class),
      @EventConfig(listeners = UICalendarView.GotoDateActionListener.class),
      @EventConfig(listeners = UICalendarView.AddCategoryActionListener.class),
      @EventConfig(listeners = UICalendarView.SwitchViewActionListener.class),
      @EventConfig(listeners = UICalendarView.QuickAddActionListener.class), 
      @EventConfig(listeners = UICalendarView.ViewActionListener.class),
      @EventConfig(listeners = UICalendarView.EditActionListener.class), 
      @EventConfig(listeners = UICalendarView.DeleteActionListener.class),
      @EventConfig(listeners = UIWeekView.UpdateEventActionListener.class),
      @EventConfig(listeners = UIWeekView.MoveNextActionListener.class), 
      @EventConfig(listeners = UIWeekView.MovePreviousActionListener.class)
    }

)
public class UIWeekView extends UICalendarView {

  protected Map<String, Map<String, CalendarEvent>> eventData_ = new HashMap<String, Map<String, CalendarEvent>>() ;
  protected LinkedHashMap<String, CalendarEvent> allWeekData_ = new LinkedHashMap<String,  CalendarEvent>() ;
  protected LinkedHashMap<String, CalendarEvent> dataMap_ = new LinkedHashMap<String,  CalendarEvent>() ;
  protected  List<CalendarEvent> daysData_  = new ArrayList<CalendarEvent>() ;
  protected boolean isShowCustomView_ = false ;
  protected Date beginDate_ ;
  protected Date endDate_ ;

  public UIWeekView() throws Exception {
    super() ;
  }

  public void refresh() throws Exception {
    System.out.println("\n\n>>>>>>>>>> WEEK VIEW") ;
    int week = getCurrentWeek() ;
    eventData_.clear() ;
    allWeekData_.clear() ;
    for(Calendar c : getDaysOfWeek(week)) {
      //List<CalendarEvent> list = new ArrayList<CalendarEvent>() ;
      Map<String, CalendarEvent> list = new HashMap<String, CalendarEvent>() ;
      String key = keyGen(c.get(Calendar.DATE), c.get(Calendar.MONTH), c.get(Calendar.YEAR)) ;
      eventData_.put(key, list) ;
    }
    CalendarService calendarService = getApplicationComponent(CalendarService.class) ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    EventQuery eventQuery = new EventQuery() ;
    List<Calendar> days =  getDaysOfWeek(week) ;
    java.util.Calendar fromcalendar = days.get(0) ;
    fromcalendar.set(Calendar.HOUR, 0) ;
    eventQuery.setFromDate(fromcalendar) ;
    java.util.Calendar tocalendar = days.get(days.size() - 1) ;
    tocalendar.set(Calendar.HOUR, 0) ;
    tocalendar.add(Calendar.DATE, 1) ;
    eventQuery.setToDate(tocalendar) ;
    List<CalendarEvent> allEvents = calendarService.getEvent(username, eventQuery, getPublicCalendars())  ;
    Iterator iter = allEvents.iterator() ;
    while(iter.hasNext()) {
      CalendarEvent event = (CalendarEvent)iter.next() ;
      Date beginEvent = event.getFromDateTime() ;
      Date endEvent = event.getToDateTime() ;
      long amount = endEvent.getTime() - beginEvent.getTime() ; 
      for(Calendar c : getDaysOfWeek(week)) {
        String key = keyGen(c.get(Calendar.DATE), c.get(Calendar.MONTH), c.get(Calendar.YEAR)) ;
        if(isSameDate(c.getTime(), beginEvent) && isSameDate(c.getTime(), endEvent) &&  amount < 24*60*60*1000 ) { 
          eventData_.get(key).put(event.getId(), event) ;
          dataMap_.put(event.getId(), event) ;
          iter.remove() ;
        } 
      }
    }
    for( CalendarEvent ce : allEvents) {
      allWeekData_.put(ce.getId(), ce) ;
      dataMap_.put(ce.getId(), ce) ;
    }
  }
  protected void moveTo(int weeks) {
    calendar_.add(Calendar.WEEK_OF_YEAR, weeks) ;
  }

  protected List<Calendar> getDaysOfWeek(int week) throws Exception {
    List<Calendar> calendarData = new ArrayList<Calendar>() ;
    Calendar cl = GregorianCalendar.getInstance() ;
    if(!isShowCustomView_) {
      CalendarSetting calSetting  = null ;
      try{
        calSetting = getAncestorOfType(UICalendarPortlet.class).getCalendarSetting() ;
      } catch (Exception e) {
        CalendarService calService = getApplicationComponent(CalendarService.class) ;
        calSetting  = calService.getCalendarSetting(Util.getPortalRequestContext().getRemoteUser()) ;
      }
      cl.setFirstDayOfWeek(Integer.parseInt(calSetting.getWeekStartOn())) ;
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
    } else {
      cl.setTime(beginDate_) ;
      while(cl.before(endDate_)) {
        calendarData.add(cl) ;
        cl.add(Calendar.DATE, 1) ;
      }
    }
    return calendarData ;
  }

  protected Map<String, Map<String, CalendarEvent>> getEventData() {return eventData_ ;}

  protected LinkedHashMap<String, CalendarEvent>  getEventList() {
    return allWeekData_ ;
  }
  public LinkedHashMap<String, CalendarEvent> getDataMap() {
    return dataMap_ ;
  }

  static  public class QuickAddActionListener extends EventListener<UIWeekView> {
    public void execute(Event<UIWeekView> event) throws Exception {
      System.out.println("QuickAddActionListener");
      UIWeekView calendarview = event.getSource() ;
      String type = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String startTime = event.getRequestContext().getRequestParameter("startTime") ;
      String finishTime = event.getRequestContext().getRequestParameter("finishTime") ;
      UICalendarPortlet uiPortlet = calendarview.getAncestorOfType(UICalendarPortlet.class) ;
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
      UIQuickAddEvent uiQuickAddEvent = uiPopupAction.activate(UIQuickAddEvent.class, 600) ;
      if(CalendarEvent.TYPE_EVENT.equals(type)) {
        uiQuickAddEvent.setEvent(true) ;
        uiQuickAddEvent.setId("UIQuickAddEvent") ;
      } else {
        uiQuickAddEvent.setEvent(false) ;
        uiQuickAddEvent.setId("UIQuickAddTask") ;
      }
      try {
        Long.parseLong(startTime) ;
      }catch (Exception e) {
        startTime = null ;
      }
      try {
        Long.parseLong(finishTime) ;
      }catch (Exception e) {
        finishTime = null ;
      }
      uiQuickAddEvent.init(uiPortlet.getCalendarSetting(), startTime, finishTime) ;
      uiQuickAddEvent.update(CalendarUtils.PRIVATE_TYPE, null) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
    }
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
  static  public class UpdateEventActionListener extends EventListener<UIWeekView> {
    public void execute(Event<UIWeekView> event) throws Exception {
      UIWeekView calendarview = event.getSource() ;
      String eventId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String calendarId = event.getRequestContext().getRequestParameter(CALENDARID) ;
      String calType = event.getRequestContext().getRequestParameter(CALTYPE) ;
      String startTime = event.getRequestContext().getRequestParameter("startTime") ;
      String finishTime = event.getRequestContext().getRequestParameter("finishTime") ;
      String currentDate = event.getRequestContext().getRequestParameter("currentDate") ;
      String username = event.getRequestContext().getRemoteUser() ;
      CalendarEvent eventCalendar = null ;
      CalendarService calendarService = CalendarUtils.getCalendarService() ;
      if(calType.equals(CalendarUtils.PRIVATE_TYPE)) {
        eventCalendar = calendarService.getUserEvent(username, calendarId, eventId) ;
      } else if(calType.equals(CalendarUtils.SHARED_TYPE)) {
        eventCalendar = calendarService.getUserEvent(username, calendarId, eventId) ;
      } else if(calType.equals(CalendarUtils.PUBLIC_TYPE)) {
        eventCalendar = calendarService.getGroupEvent(calendarId, eventId) ;
      }
      Calendar calBegin = GregorianCalendar.getInstance() ;
      Calendar calEnd = GregorianCalendar.getInstance() ;
      calBegin.setTimeInMillis(Long.parseLong(currentDate)) ;
      calEnd.setTimeInMillis(Long.parseLong(currentDate)) ;
      
      int hoursBg = (Integer.parseInt(startTime)/60) ;
      int minutesBg = (Integer.parseInt(startTime)%60) ;
      
      int hoursEnd = (Integer.parseInt(finishTime)/60) ;
      int minutesEnd = (Integer.parseInt(finishTime)%60) ;
      calBegin.set(Calendar.AM_PM, Calendar.AM) ;
      calBegin.set(Calendar.HOUR, hoursBg) ;
      calBegin.set(Calendar.MINUTE, minutesBg) ;
      eventCalendar.setFromDateTime(calBegin.getTime()) ;
      
      calEnd.set(Calendar.AM_PM, Calendar.AM) ;
      calEnd.set(Calendar.HOUR, hoursEnd) ;
      calEnd.set(Calendar.MINUTE, minutesEnd) ;
      eventCalendar.setToDateTime(calEnd.getTime()) ;
      
      if(calType.equals(CalendarUtils.PRIVATE_TYPE)) {
        calendarService.saveUserEvent(username, calendarId, eventCalendar, false) ;
      }else if(calType.equals(CalendarUtils.SHARED_TYPE)){
        calendarService.saveEventToSharedCalendar(username, calendarId, eventCalendar, false) ;
      }else if(calType.equals(CalendarUtils.PUBLIC_TYPE)){
        calendarService.saveGroupEvent(calendarId, eventCalendar, false) ;          
      }
      calendarview.refresh() ;
      UIMiniCalendar uiMiniCalendar = calendarview.getAncestorOfType(UICalendarPortlet.class).findFirstComponentOfType(UIMiniCalendar.class) ;
      uiMiniCalendar.updateMiniCal() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMiniCalendar) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
    }
  }
}
