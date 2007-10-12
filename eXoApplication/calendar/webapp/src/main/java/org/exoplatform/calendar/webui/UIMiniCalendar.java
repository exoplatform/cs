/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui;

import java.util.Calendar;

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
public class UIMiniCalendar extends UIMonthView  {
  public UIMiniCalendar() throws Exception {
    refresh() ;
  }  


  protected void moveYear(int yearStep) {
    calendar_.add(Calendar.YEAR, yearStep) ;
  }

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
