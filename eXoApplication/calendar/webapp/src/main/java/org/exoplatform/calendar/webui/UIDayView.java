/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
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
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/calendar/webui/UIDayView.gtmpl", 
    events = {
      @EventConfig(listeners = UICalendarView.RefreshActionListener.class),
      @EventConfig(listeners = UICalendarView.AddEventActionListener.class),  
      @EventConfig(listeners = UICalendarView.DeleteEventActionListener.class),
      @EventConfig(listeners = UICalendarView.ChangeCategoryActionListener.class), 
      @EventConfig(listeners = UIDayView.MoveNextActionListener.class), 
      @EventConfig(listeners = UIDayView.QuickAddActionListener.class), 
      @EventConfig(listeners = UIDayView.MovePreviousActionListener.class), 
      @EventConfig(listeners = UIDayView.SaveEventActionListener.class), 
      @EventConfig(listeners = UICalendarView.AddCategoryActionListener.class)
    }

)
public class UIDayView extends UICalendarView {

  private Map<String, CalendarEvent> eventData_ = new HashMap<String, CalendarEvent>() ;
  private Map<String, CalendarEvent> allDayEvent_ = new HashMap<String, CalendarEvent>() ;
  private boolean isShowWorkingTime_ = false ;
  private int startTime_ = 0 ;
  private int endTime_ = 24 ;
  private int timeInterval_ = 30 ;

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
    toDate.set(Calendar.HOUR, toDate.getActualMaximum(Calendar.HOUR)) ;
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

  protected List<String> getDisplayTimes(String timeFormat, int timeInterval) {
    if(isShowWorkingTime_) {
      return CalendarUtils.getDisplayTimes(timeFormat, timeInterval,startTime_, endTime_*(60/timeInterval)) ;
    } 
    else {
      return CalendarUtils.getDisplayTimes(timeFormat, timeInterval,0, 24*(60/timeInterval)) ;
    }
  }

  static  public class MoveNextActionListener extends EventListener<UIDayView> {
    public void execute(Event<UIDayView> event) throws Exception {
      UIDayView calendarview = event.getSource() ;
      calendarview.moveDateTo(1) ;
      calendarview.refresh() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
    }
  }
  static  public class QuickAddActionListener extends EventListener<UIDayView> {
    public void execute(Event<UIDayView> event) throws Exception {
      UIDayView calendarview = event.getSource() ;
      String type = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String startTime = event.getRequestContext().getRequestParameter("startTime") ;
      String finishTime = event.getRequestContext().getRequestParameter("finishTime") ;
      UICalendarPortlet uiPortlet = calendarview.getAncestorOfType(UICalendarPortlet.class) ;
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
      if(CalendarEvent.TYPE_EVENT.equals(type)) {
        UIQuickAddEvent uiQuickAddEvent = uiPopupAction.activate(UIQuickAddEvent.class, 600) ;
        DateFormat df =new SimpleDateFormat("MM/dd/yyyy") ;
        try {
          String beginTime = df.format(calendarview.getCurrentDate())+ " " + startTime ;
          String endTime = df.format(calendarview.getCurrentDate())+ " " + finishTime ;
          System.out.println("\n\n begin " + beginTime);
          System.out.println("\n\n end " + endTime);
          uiQuickAddEvent.init(beginTime, endTime) ;
        } catch (Exception e) {
          e.printStackTrace() ;
        }
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      } else {

      }
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
      CalendarService calendarService = calendarview.getApplicationComponent(CalendarService.class) ;
      CalendarEvent ce = calendarService.getUserEvent(username, calendarId, eventId) ;
      if(ce != null) {
        try {
          int hoursBg = (Integer.parseInt(startTime)/60) ;
          int minutesBg = (Integer.parseInt(startTime)%60) ;

          int hoursEnd = (Integer.parseInt(endTime)/60) ;
          int minutesEnd = (Integer.parseInt(endTime)%60) ;

          Calendar fromDateTime = new GregorianCalendar(calendarview.getCurrentYear(), calendarview.getCurrentMonth(), calendarview.getCurrentDay()) ;
          fromDateTime.set(Calendar.HOUR, hoursBg) ;
          fromDateTime.set(Calendar.MINUTE, minutesBg) ;
          Calendar toDateTime = new GregorianCalendar(calendarview.getCurrentYear(), calendarview.getCurrentMonth(), calendarview.getCurrentDay()) ;
          toDateTime.set(Calendar.HOUR, hoursEnd) ;
          toDateTime.set(Calendar.MINUTE, minutesEnd) ;

          ce.setFromDateTime(fromDateTime.getTime());
          ce.setToDateTime(toDateTime.getTime()) ;
          calendarService.saveUserEvent(username, calendarId, ce, false) ;
        } catch (Exception e) {
          e.printStackTrace() ;
        }
      }
      calendarview.refresh() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
    }
  }
}
