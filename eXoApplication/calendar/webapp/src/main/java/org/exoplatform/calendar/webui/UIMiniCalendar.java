/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.Map;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.EventQuery;
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
    template =  "app:/templates/calendar/webui/UIMiniCalendar.gtmpl",
    events = {
      @EventConfig(listeners = UIMiniCalendar.MoveNextActionListener.class), 
      @EventConfig(listeners = UIMiniCalendar.MovePreviousActionListener.class),
      @EventConfig(listeners = UICalendarView.GotoDateActionListener.class)
    }

)
public class UIMiniCalendar extends UICalendarView  {
  private Map<Integer, String> dataMap ;
  public UIMiniCalendar() throws Exception {
    updateMiniCal() ;
  }  

  public void updateMiniCal() throws Exception {
    EventQuery eventQuery = new EventQuery() ;
    java.util.Calendar fromcalendar = new GregorianCalendar(getCurrentYear(), getCurrentMonth(), 1, 0,0,0) ;
    eventQuery.setFromDate(CalendarUtils.getBeginDay(fromcalendar)) ;
    java.util.Calendar tocalendar = new GregorianCalendar(getCurrentYear(), getCurrentMonth(), getDaysInMonth(), 24,0,0) ;
    eventQuery.setToDate(CalendarUtils.getEndDay(tocalendar)) ;
    CalendarService calendarService = CalendarUtils.getCalendarService() ;
    dataMap = calendarService.searchHightLightEvent(CalendarUtils.getCurrentUser(), eventQuery, getPublicCalendars());
  }
  protected int getWeeksOfTheMonth(int year, int month, int day) {
    return new GregorianCalendar(year, month, day).getActualMaximum(java.util.Calendar.WEEK_OF_MONTH) ;
  }
  private Map<Integer, String> getData(){ return dataMap ; }
 /* protected void moveYear(int yearStep) {
    calendar_.add(Calendar.YEAR, yearStep) ;
  }*/
  public LinkedHashMap<String, CalendarEvent> getDataMap(){ return null ; }
  
  public java.util.Calendar getBeginDateOfMonth() throws Exception{
    java.util.Calendar temCal = CalendarUtils.getInstanceTempCalendar() ;
    temCal.setTime(calendar_.getTime()) ;
    temCal.setFirstDayOfWeek(Calendar.SUNDAY) ;
    temCal.set(java.util.Calendar.DATE, 1) ;
    int amount1 = temCal.getFirstDayOfWeek() - temCal.get(java.util.Calendar.DAY_OF_WEEK) ;
    return getBeginDay(getDateByValue(getCurrentYear(), getCurrentMonth(),1, UICalendarView.TYPE_DATE, amount1)) ;
  }
  
  static  public class MoveNextActionListener extends EventListener<UIMiniCalendar> {
    public void execute(Event<UIMiniCalendar> event) throws Exception {
      UIMiniCalendar miniCal = event.getSource() ;
      String type = event.getRequestContext().getRequestParameter(OBJECTID) ;
      if(TYPE_MONTH == Integer.parseInt(type)) {
        miniCal.calendar_.add(java.util.Calendar.MONTH, 1) ;
      } else {
        miniCal.calendar_.add(Calendar.YEAR, 1) ;
      }
      miniCal.setCurrentDay(1);
      miniCal.updateMiniCal() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(miniCal.getParent()) ;
    }
  }

  static  public class MovePreviousActionListener extends EventListener<UIMiniCalendar> {
    public void execute(Event<UIMiniCalendar> event) throws Exception {
      UIMiniCalendar miniCal = event.getSource() ;
      String type = event.getRequestContext().getRequestParameter(OBJECTID) ;
      if(TYPE_MONTH == Integer.parseInt(type)) {
        miniCal.calendar_.add(java.util.Calendar.MONTH, -1) ;
      } else {
        miniCal.calendar_.add(Calendar.YEAR, -1) ;
      }
      miniCal.setCurrentDay(1);
      miniCal.updateMiniCal() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(miniCal.getParent()) ;
    }
  }
}
