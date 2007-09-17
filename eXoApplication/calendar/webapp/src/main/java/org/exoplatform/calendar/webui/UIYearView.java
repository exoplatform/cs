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
    template = "app:/templates/calendar/webui/UIYearView.gtmpl",
    events = {
      @EventConfig(listeners = UIYearView.MoveNextActionListener.class), 
      @EventConfig(listeners = UIYearView.MovePreviousActionListener.class)
    }

)
public class UIYearView extends UICalendarView {

  public UIYearView() throws Exception {
    super() ;
  }

  protected void yearNext(int years) {
    calendar_.roll(Calendar.YEAR, years) ;
    calendar_.set(Calendar.MONTH, Calendar.JANUARY) ;
    calendar_.set(Calendar.DATE, calendar_.getMinimum(Calendar.DAY_OF_MONTH)) ;
  }
  protected void yearBack(int years) {
    calendar_.roll(Calendar.YEAR, years) ;
    calendar_.set(Calendar.MONTH, Calendar.JANUARY) ;
    calendar_.set(Calendar.DATE, calendar_.getMinimum(Calendar.DAY_OF_MONTH)) ;
  }
  
  private List getEventList() {
    return null ;
  }

  static  public class MoveNextActionListener extends EventListener<UIYearView> {
    public void execute(Event<UIYearView> event) throws Exception {
      UIYearView calendarview = event.getSource() ;
      calendarview.yearNext(1) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
    }
  }

  static  public class MovePreviousActionListener extends EventListener<UIYearView> {
    public void execute(Event<UIYearView> event) throws Exception {
      UIYearView calendarview = event.getSource() ;
      calendarview.yearBack(-1) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
    }
  }


}
