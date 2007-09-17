/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui;

import java.util.Calendar;
import java.util.List;

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
      @EventConfig(listeners = UIDayView.MovePreviousActionListener.class), 
      @EventConfig(listeners = UICalendarView.AddCategoryActionListener.class)
    }

)
public class UIDayView extends UICalendarView {

  public UIDayView() throws Exception{
    super() ;
  }

  protected void dayNext(int days) {
    if(calendar_.getActualMaximum(Calendar.DAY_OF_MONTH) == getCurrentDay()) {
      if(calendar_.get(Calendar.MONTH) == Calendar.DECEMBER){
        calendar_.roll(Calendar.YEAR, true) ;
        calendar_.set(Calendar.MONTH, Calendar.JANUARY) ;
      } else {
        calendar_.roll(Calendar.MONTH, true) ;
      } 
      calendar_.set(Calendar.DATE, calendar_.getActualMinimum(Calendar.DAY_OF_MONTH)) ;
    } else {
      calendar_.roll(Calendar.DATE, days) ;
    }
  }

  protected void dayBack(int days) throws Exception {
    if(calendar_.getActualMinimum(Calendar.DAY_OF_MONTH) == getCurrentDay()) {
      if(calendar_.get(Calendar.MONTH) == Calendar.JANUARY){
        calendar_.roll(Calendar.YEAR, false) ;
        calendar_.set(Calendar.MONTH, Calendar.DECEMBER) ;
      } else { 
        calendar_.roll(Calendar.MONTH, false) ;
      }
      calendar_.set(Calendar.DATE, calendar_.getActualMaximum(Calendar.DAY_OF_MONTH)) ;
    } else {
      calendar_.roll(Calendar.DATE, days) ;
    }
  }
  private List getEventList() {
    return null ;
  }

  static  public class MoveNextActionListener extends EventListener<UIDayView> {
    public void execute(Event<UIDayView> event) throws Exception {
      UIDayView calendarview = event.getSource() ;
      calendarview.dayNext(1) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
    }
  }

  static  public class MovePreviousActionListener extends EventListener<UIDayView> {
    public void execute(Event<UIDayView> event) throws Exception {
      UIDayView calendarview = event.getSource() ;
      calendarview.dayBack(-1) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
    }
  }

}
