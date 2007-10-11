/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

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
      /*@EventConfig(listeners = UICalendarView.GotoMonthActionListener.class),
      @EventConfig(listeners = UICalendarView.GotoYearActionListener.class)*/
    }

)
public class UIMiniCalendar extends UIMonthView  {
  /*final public static String TYPE_MONTH = "month".intern() ;
  final public static String TYPE_YEAR = "year".intern() ;*/
  
  public UIMiniCalendar() throws Exception {
   refresh() ;
  }  
  
   
  protected void moveYear(int yearStep) {
    calendar_.add(Calendar.YEAR, yearStep) ;
  }
  /*static  public class GotoDateActionListener extends EventListener<UIMiniCalendar> {
    public void execute(Event<UIMiniCalendar> event) throws Exception {
      UIMiniCalendar calendarview = event.getSource() ;
      String date = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UICalendarPortlet portlet = calendarview.getAncestorOfType(UICalendarPortlet.class) ;
      UICalendarViewContainer uiContainer = portlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
      uiContainer.setRenderedChild(UIDayView.class) ;
      UIDayView uiDayView = uiContainer.getChild(UIDayView.class) ;
      calendarview.gotoDate(Integer.parseInt(date), calendarview.getCurrentMonth(), calendarview.getCurrentYear()) ;
      uiDayView.setCurrentCalendar(calendarview.getCurrentCalendar()) ;
      uiDayView.refresh() ;
      calendarview.refresh() ;
      UIActionBar uiActionBar = portlet.findFirstComponentOfType(UIActionBar.class) ;
      uiActionBar.setCurrentView(uiContainer.getRenderedChild().getId()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiActionBar) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
    }
  }
  static  public class GotoMonthActionListener extends EventListener<UIMiniCalendar> {
    public void execute(Event<UIMiniCalendar> event) throws Exception {
      UIMiniCalendar calendarview = event.getSource() ;
      String date = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UICalendarPortlet portlet = calendarview.getAncestorOfType(UICalendarPortlet.class) ;
      UICalendarViewContainer uiContainer = portlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
      uiContainer.setRenderedChild(UIMonthView.class) ;
      UIMonthView uiMonthView = uiContainer.getChild(UIMonthView.class) ;
      uiMonthView.setCurrentDay(1) ;
      calendarview.setCurrentDay(1);
      uiMonthView.setCurrentMonth(Integer.parseInt(date)) ;
      uiMonthView.setCurrentYear(calendarview.getCurrentYear()) ;
      uiMonthView.refresh() ;
      UIActionBar uiActionBar = portlet.findFirstComponentOfType(UIActionBar.class) ;
      uiActionBar.setCurrentView(uiContainer.getRenderedChild().getId()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiActionBar) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
    }
  }
  static  public class GotoYearActionListener extends EventListener<UIMiniCalendar> {
    public void execute(Event<UIMiniCalendar> event) throws Exception {
      UIMiniCalendar calendarview = event.getSource() ;
      String date = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UICalendarPortlet portlet = calendarview.getAncestorOfType(UICalendarPortlet.class) ;
      UICalendarViewContainer uiContainer = portlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
      uiContainer.setRenderedChild(UIYearView.class) ;
      UIYearView uiYearView = uiContainer.getChild(UIYearView.class) ;
      uiYearView.setCurrentDay(1) ;
      calendarview.setCurrentDay(1);
      uiYearView.setCurrentMonth(Calendar.JANUARY) ;
      uiYearView.setCurrentYear(Integer.parseInt(date)) ;
      uiYearView.refresh() ;
      UIActionBar uiActionBar = portlet.findFirstComponentOfType(UIActionBar.class) ;
      uiActionBar.setCurrentView(uiContainer.getRenderedChild().getId()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiActionBar) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
    }
  }*/

  static  public class MoveNextActionListener extends EventListener<UIMiniCalendar> {
    public void execute(Event<UIMiniCalendar> event) throws Exception {
      UIMiniCalendar calendarview = event.getSource() ;
      String type = event.getRequestContext().getRequestParameter(OBJECTID) ;
      if(TYPE_MONTH == Integer.parseInt(type)) {
        calendarview.monthNext(1) ;
      } else {
        calendarview.moveYear(1) ;
      }
      calendarview.setCurrentDay(1);
      calendarview.refresh() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
    }
  }

  static  public class MovePreviousActionListener extends EventListener<UIMiniCalendar> {
    public void execute(Event<UIMiniCalendar> event) throws Exception {
      UIMiniCalendar calendarview = event.getSource() ;
      String type = event.getRequestContext().getRequestParameter(OBJECTID) ;
      if(TYPE_MONTH == Integer.parseInt(type)) {
        calendarview.monthNext(-1) ;
      } else {
        calendarview.moveYear(-1) ;
      }
      calendarview.setCurrentDay(1);
      calendarview.refresh() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
    }
  }
}
