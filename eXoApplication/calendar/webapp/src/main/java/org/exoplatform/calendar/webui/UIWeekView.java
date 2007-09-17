/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
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
    lifecycle =UIFormLifecycle.class,
    template = "app:/templates/calendar/webui/UIWeekView.gtmpl",
    events = {
      @EventConfig(listeners = UIWeekView.MoveNextActionListener.class), 
      @EventConfig(listeners = UIWeekView.MovePreviousActionListener.class)
    }

)
public class UIWeekView extends UICalendarView {

  public UIWeekView() throws Exception {
    super() ;
  }


  protected void weekNext(int weeks) {
    if(getCurrentWeek() == calendar_.getActualMaximum(Calendar.WEEK_OF_YEAR)) {
      calendar_.roll(Calendar.YEAR, true) ;
      calendar_.roll(Calendar.WEEK_OF_YEAR, calendar_.getActualMinimum(Calendar.WEEK_OF_YEAR)) ;
    } else {
      calendar_.roll(Calendar.WEEK_OF_YEAR, weeks) ;
    }
  }
  protected void weekBack(int weeks) {
    if(getCurrentWeek() == calendar_.getActualMinimum(Calendar.WEEK_OF_YEAR)) {
      calendar_.roll(Calendar.YEAR, false) ;
      calendar_.roll(Calendar.WEEK_OF_YEAR, calendar_.getActualMaximum(Calendar.WEEK_OF_YEAR)) ;
    } else {
      calendar_.roll(Calendar.WEEK_OF_YEAR, weeks) ;
    }
  }

  protected Calendar nextDayOf(int value, int day,int month,int year) {
    Calendar cl = new GregorianCalendar(year, month, day) ;
    cl.roll(Calendar.DATE, day + value) ;
    return cl ;
  }
  protected Calendar previousDayOf(int value, int day,int month,int year) {
    Calendar cl = new GregorianCalendar(year, month, day) ;
    cl.roll(Calendar.DATE, day + value) ;
    return cl ;
  }
  protected List<Calendar> getDaysOfWeek(int week) {
    List<Calendar> calendarData = new ArrayList<Calendar>() ;
    Calendar cl = GregorianCalendar.getInstance() ;
    cl.set(Calendar.WEEK_OF_YEAR, week) ;
    int day = cl.get(Calendar.DATE) ;
    int month = cl.get(Calendar.MONTH) ;
    int year = cl.get(Calendar.YEAR) ;
    calendarData.add(previousDayOf(-1, day, month, year)) ;
    calendarData.add(cl) ;
    for(int d = 1 ;  d < 5 ; d++) {
      cl = nextDayOf(d, day, month, year) ;
      calendarData.add(cl) ;
    }
    return calendarData ;
  }

  private List getEventList() {
    return null ;
  }
  static  public class MoveNextActionListener extends EventListener<UIWeekView> {
    public void execute(Event<UIWeekView> event) throws Exception {
      UIWeekView calendarview = event.getSource() ;
      calendarview.weekNext(1) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
    }
  }

  static  public class MovePreviousActionListener extends EventListener<UIWeekView> {
    public void execute(Event<UIWeekView> event) throws Exception {
      UIWeekView calendarview = event.getSource() ;
      calendarview.weekBack(-1) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(calendarview.getParent()) ;
    }
  }
}
