/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.webui.popup.UIPopupAction;
import org.exoplatform.calendar.webui.popup.UIQuickAddEvent;
import org.exoplatform.portal.webui.util.Util;
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
      @EventConfig(listeners = UICalendarView.RefreshActionListener.class),
      @EventConfig(listeners = UICalendarView.AddEventActionListener.class),      
      @EventConfig(listeners = UICalendarView.DeleteEventActionListener.class),
      @EventConfig(listeners = UICalendarView.ChangeCategoryActionListener.class), 
      @EventConfig(listeners = UICalendarView.EventSelectActionListener.class), 
      @EventConfig(listeners = UICalendarView.AddCategoryActionListener.class),
      @EventConfig(listeners = UICalendarView.EditEventActionListener.class), 
      @EventConfig(listeners = UICalendarView.QuickDeleteEventActionListener.class),
      @EventConfig(listeners = UICalendarView.GotoDateActionListener.class), 
      @EventConfig(listeners = UIMonthView.MoveNextActionListener.class), 
      @EventConfig(listeners = UIMonthView.MovePreviousActionListener.class),
      @EventConfig(listeners = UIMonthView.QuickAddNewEventActionListener.class), 
      @EventConfig(listeners = UIMonthView.QuickAddNewTaskActionListener.class), 
      @EventConfig(listeners = UIMonthView.UpdateEventActionListener.class)
    }
)
public class UIMonthView extends UICalendarView {
  private Map<String, String> calendarIds_ = new HashMap<String, String>() ;

  private Map<Integer, List<CalendarEvent>> eventData_ = new HashMap<Integer, List<CalendarEvent>>() ;

  public UIMonthView() throws Exception{
    super() ;
  }

  protected int getWeeksOfTheMonth(int year, int month, int day) {
    return new GregorianCalendar(year, month, day).getActualMaximum(java.util.Calendar.WEEK_OF_MONTH) ;
  }
  protected void refreshEvents() throws Exception {
    CalendarService calendarService = getApplicationComponent(CalendarService.class) ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    EventQuery eventQuery = new EventQuery() ;
    java.util.Calendar fromcalendar = new GregorianCalendar(getCurrentYear(), getCurrentMonth(), 1, 0,0,0) ;
    eventQuery.setFromDate(fromcalendar) ;
    java.util.Calendar tocalendar = new GregorianCalendar(getCurrentYear(), getCurrentMonth(), getDaysInMonth(), 24,0,0) ;
    eventQuery.setToDate(tocalendar) ;
    List<CalendarEvent> allEvents = calendarService.getUserEvents(username, eventQuery);    
    allEvents.addAll(calendarService.getPublicEvents(eventQuery))  ;
    Iterator<UIComponent> iter = getChildren().iterator() ;
    while (iter.hasNext()) {
      if( iter.next() instanceof UIFormCheckBoxInput) iter.remove() ; 
    }
    eventData_.clear() ;
    for(int day =1 ;  day <= getDaysInMonth(); day++) {
      List<CalendarEvent> list =  new ArrayList<CalendarEvent>() ;
      eventData_.put(day, list) ;
    }
    Iterator<CalendarEvent> eventIter = allEvents.iterator() ;
    java.util.Calendar tempBegin = GregorianCalendar.getInstance()  ;
    java.util.Calendar tempEnd = GregorianCalendar.getInstance()  ;
    while (eventIter.hasNext()) {
      CalendarEvent ce = eventIter.next() ;
      tempBegin.setTime(ce.getFromDateTime()) ;
      tempEnd.setTime(ce.getToDateTime()) ;
      int fromDate = 1 ;
      int toDate = getDaysInMonth();
      if(tempBegin.after(fromcalendar)) {
        fromDate = tempBegin.get(java.util.Calendar.DATE) ;
      }
      if(tempEnd.before(tocalendar)) {
        toDate = tempEnd.get(java.util.Calendar.DATE) ;
      }
      for(int i = fromDate; i <= toDate; i ++) {
        eventData_.get(i).add(ce) ;
        if(tempBegin.get(java.util.Calendar.DATE) >= i){
          UIFormCheckBoxInput<Boolean> input = new UIFormCheckBoxInput<Boolean>(ce.getId(), ce.getId(), false) ;
          input.setBindingField(ce.getCalendarId()) ;
          addChild(input) ;
        }
      }
      eventIter.remove() ;
    }
  }

  protected void addCalendarId(String id) {calendarIds_.put(id,id) ;}
  protected Map<String, String> getCalendarIds() {return calendarIds_ ;}

  private List getEventList()throws Exception {
    return getList() ;
  }

  protected void refreshSelectedCalendarIds() throws Exception {
    CalendarService calendarService = getApplicationComponent(CalendarService.class) ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    for(Calendar c : calendarService.getUserCalendars(username)) {
      addCalendarId(c.getId()) ;
    }
  }

  public void refresh() throws Exception {
    refreshSelectedCalendarIds() ;
    refreshEvents() ;

  }
  private Date getDateOf(int year, int month, int day) {
    GregorianCalendar gc = new GregorianCalendar(year, month, day) ;
    return gc.getTime() ;
  }
  private Map<Integer, List<CalendarEvent>> getEventsData() {
    return eventData_ ;
  }

  protected void monthNext(int months) {
    calendar_.add(java.util.Calendar.MONTH, months) ;
  }
  protected void monthBack(int months) {
    calendar_.add(java.util.Calendar.MONTH, months) ;
  }
  protected List<CalendarEvent> getSelectedEvents() {
    List<CalendarEvent> events = new ArrayList<CalendarEvent>() ;
    for(List<CalendarEvent> items : getEventsData().values()) {
      for(CalendarEvent ce : items) {
        UIFormCheckBoxInput<Boolean>  checkbox = getChildById(ce.getId())  ;
        if(checkbox != null && checkbox.isChecked()) events.add(ce) ;
      }
    }
    return events ; 
  }
  static  public class GotoDayActionListener extends EventListener<UIMonthView> {
    public void execute(Event<UIMonthView> event) throws Exception {
      UIMonthView calendarview = event.getSource() ;
      
    }
  }
  static  public class MoveNextActionListener extends EventListener<UIMonthView> {
    public void execute(Event<UIMonthView> event) throws Exception {
      UIMonthView calendarview = event.getSource() ;
      calendarview.monthNext(1) ;
      calendarview.refresh() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
    }
  }
  static  public class MovePreviousActionListener extends EventListener<UIMonthView> {
    public void execute(Event<UIMonthView> event) throws Exception {
      UIMonthView calendarview = event.getSource() ;
      calendarview.monthBack(-1) ;
      calendarview.refresh() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
    }
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

  static  public class QuickAddNewEventActionListener extends EventListener<UIMonthView> {
    public void execute(Event<UIMonthView> event) throws Exception {
      UIMonthView calendarview = event.getSource() ;
      System.out.println(" ===========> AddEventActionListener") ;
      String selectedDate = event.getRequestContext().getRequestParameter(OBJECTID) ;
      CalendarService calendarService = calendarview.getApplicationComponent(CalendarService.class) ;
      String username = event.getRequestContext().getRemoteUser() ;
      UIApplication uiApp = calendarview.getAncestorOfType(UIApplication.class) ;
      if(calendarService.getUserCalendars(username).size() <= 0) {
        uiApp.addMessage(new ApplicationMessage("UICalendarView.msg.calendar-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      } else {
        UICalendarPortlet uiPortlet = calendarview.getAncestorOfType(UICalendarPortlet.class) ;
        UIPopupAction uiParenPopup = uiPortlet.getChild(UIPopupAction.class) ;
        UIQuickAddEvent uiEventForm = uiParenPopup.activate(UIQuickAddEvent.class, 700) ;
        uiEventForm.setEvent(true) ;
        try {
          int day = Integer.parseInt(selectedDate) ;
          java.util.Calendar date = new GregorianCalendar(calendarview.getCurrentYear(), calendarview.getCurrentMonth(), day) ;
          DateFormat df = SimpleDateFormat.getInstance() ;
          String startTime = df.format(date.getTime()) ;
          date.add(java.util.Calendar.MINUTE, 30) ;
          String endTime = df.format(date.getTime())  ;
          uiEventForm.init(startTime, endTime) ;
        } catch (Exception e) {
          e.printStackTrace() ;
          uiEventForm.init() ;
        }
        event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiParenPopup) ;
      }
    }
  }
  static  public class QuickAddNewTaskActionListener extends EventListener<UIMonthView> {
    public void execute(Event<UIMonthView> event) throws Exception {
      UIMonthView calendarview = event.getSource() ;
      System.out.println("\n\n AddNewTaskActionListener");
      String selectedDate = event.getRequestContext().getRequestParameter(OBJECTID) ;
      CalendarService calendarService = calendarview.getApplicationComponent(CalendarService.class) ;
      String username = event.getRequestContext().getRemoteUser() ;
      UIApplication uiApp = calendarview.getAncestorOfType(UIApplication.class) ;
      if(calendarService.getUserCalendars(username).size() <= 0) {
        uiApp.addMessage(new ApplicationMessage("UICalendarView.msg.calendar-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      } else {
        UICalendarPortlet uiPortlet = calendarview.getAncestorOfType(UICalendarPortlet.class) ;
        UIPopupAction uiParenPopup = uiPortlet.getChild(UIPopupAction.class) ;
        UIQuickAddEvent uiEventForm = uiParenPopup.activate(UIQuickAddEvent.class,700) ;
        uiEventForm.setEvent(false) ;
        uiEventForm.setId("UIQuickAddTask") ;
        try {
          int day = Integer.parseInt(selectedDate) ;
          java.util.Calendar date = new GregorianCalendar(calendarview.getCurrentYear(), calendarview.getCurrentMonth(), day) ;
          DateFormat df = SimpleDateFormat.getInstance() ;
          String startTime = df.format(date.getTime()) ;
          date.add(java.util.Calendar.MINUTE, 30) ;
          String endTime = df.format(date.getTime())  ;
          uiEventForm.init(startTime, endTime) ;
        } catch (Exception e) {
          e.printStackTrace() ;
          uiEventForm.init() ;
        }
        event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiParenPopup) ;
      }
    }
  }
  static  public class UpdateEventActionListener extends EventListener<UIMonthView> {
    public void execute(Event<UIMonthView> event) throws Exception {
      UIMonthView calendarview = event.getSource() ;
      UICalendarPortlet uiPortlet = calendarview.getAncestorOfType(UICalendarPortlet.class) ;
      String username = event.getRequestContext().getRemoteUser() ;
      String value = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String eventId = event.getRequestContext().getRequestParameter(EVENTID) ;
      String calendarId = event.getRequestContext().getRequestParameter(CALENDARID) ;
      try {
        int day = Integer.parseInt(value) ;
        CalendarService calService = calendarview.getApplicationComponent(CalendarService.class) ;
        CalendarEvent calEvent = calService.getUserEvent(username, calendarId, eventId) ;
        java.util.Calendar cal1 = GregorianCalendar.getInstance() ;
        cal1.setTime(calEvent.getFromDateTime()) ;
        int amount = cal1.get(java.util.Calendar.DATE) - day ;
        cal1.add(java.util.Calendar.DATE, amount) ;
        calEvent.setFromDateTime(cal1.getTime()) ;
        cal1.setTime(calEvent.getToDateTime()) ;
        cal1.add(java.util.Calendar.DATE, amount) ;
        calEvent.setFromDateTime(cal1.getTime()) ;
        calService.saveUserEvent(username, calendarId, calEvent, false) ;
        UIMiniCalendar uiMiniCalendar = uiPortlet.findFirstComponentOfType(UIMiniCalendar.class) ;
        uiMiniCalendar.refresh() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiMiniCalendar) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
      } catch (Exception e) {
        e.printStackTrace() ;
      }
    }
  }
}
