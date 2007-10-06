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
      @EventConfig(listeners = UIYearView.GotoMonthActionListener.class),
      @EventConfig(listeners = UIYearView.MovePreviousActionListener.class)
    }

)
public class UIYearView extends UICalendarView {

  public UIYearView() throws Exception {
    super() ;
  }

  protected void yearNext(int years) {
    calendar_.add(Calendar.YEAR, years) ;
  }
  protected void yearBack(int years) {
    calendar_.add(Calendar.YEAR, years) ;
  }
  public void refresh() {}
  
  private List getEventList() {
    return null ;
  }

  static  public class MoveNextActionListener extends EventListener<UIYearView> {
    public void execute(Event<UIYearView> event) throws Exception {
      UIYearView calendarview = event.getSource() ;
      calendarview.yearNext(1) ;
      calendarview.refresh() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
    }
  }

  static  public class MovePreviousActionListener extends EventListener<UIYearView> {
    public void execute(Event<UIYearView> event) throws Exception {
      UIYearView calendarview = event.getSource() ;
      calendarview.yearBack(-1) ;
      calendarview.refresh() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
    }
  }

  static  public class GotoMonthActionListener extends EventListener<UIYearView> {
    public void execute(Event<UIYearView> event) throws Exception {
      UIYearView calendarview = event.getSource() ;
      String date = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UICalendarPortlet portlet = calendarview.getAncestorOfType(UICalendarPortlet.class) ;
      UICalendarViewContainer uiContainer = portlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
      uiContainer.setRenderedChild(UIMonthView.class) ;
      UIMonthView uiMonthView = uiContainer.getChild(UIMonthView.class) ;
      uiMonthView.setCurrentDay(1) ;
      //calendarview.setCurrentDay(1);
      uiMonthView.setCurrentMonth(Integer.parseInt(date)) ;
      uiMonthView.refresh() ;
      UIActionBar uiActionBar = portlet.findFirstComponentOfType(UIActionBar.class) ;
      uiActionBar.setCurrentView(uiContainer.getRenderedChild().getId()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiActionBar) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
    }
  }

}
