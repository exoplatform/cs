/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui;

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
      @EventConfig(listeners = UIMiniCalendar.GotoDateActionListener.class)
    }

)
public class UIMiniCalendar extends UIMonthView  {
  public UIMiniCalendar() throws Exception {

  }  
  public void refresh() {}
  static  public class GotoDateActionListener extends EventListener<UIMiniCalendar> {
    public void execute(Event<UIMiniCalendar> event) throws Exception {
      UIMonthView calendarview = event.getSource() ;
      String date = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UICalendarPortlet portlet = calendarview.getAncestorOfType(UICalendarPortlet.class) ;
      UICalendarViewContainer uiContainer = portlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
      UIDayView uiDayView = uiContainer.getChild(UIDayView.class) ;
      if(uiDayView != null) {
        int day = Integer.parseInt(date) ;
        int month = calendarview.getCurrentMonth() ;
        int year = calendarview.getCurrentYear() ;
        uiDayView.gotoDate(day, month, year) ;
        uiDayView.refresh() ;
        uiContainer.setRenderedChild(uiDayView.getId()) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
      }
    }
  }

  static  public class MoveNextActionListener extends EventListener<UIMiniCalendar> {
    public void execute(Event<UIMiniCalendar> event) throws Exception {
      UIMonthView calendarview = event.getSource() ;
      calendarview.monthNext(1) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
    }
  }

  static  public class MovePreviousActionListener extends EventListener<UIMiniCalendar> {
    public void execute(Event<UIMiniCalendar> event) throws Exception {
      UIMonthView calendarview = event.getSource() ;
      calendarview.monthBack(-1) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
    }
  }
}
