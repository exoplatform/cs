/**
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 **/
package org.exoplatform.calendar.webui;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


import org.exoplatform.calendar.SessionsUtils;
import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.EventQuery;
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
    template = "app:/templates/calendar/webui/UIYearView.gtmpl",
    events = {
      @EventConfig(listeners = UICalendarView.AddEventActionListener.class),  
      @EventConfig(listeners = UICalendarView.DeleteEventActionListener.class),
      @EventConfig(listeners = UICalendarView.AddCategoryActionListener.class),
      @EventConfig(listeners = UIYearView.MoveNextActionListener.class), 
      @EventConfig(listeners = UICalendarView.GotoDateActionListener.class),
      @EventConfig(listeners = UIYearView.MovePreviousActionListener.class)
    }

)
public class UIYearView extends UICalendarView {
  private Map<Integer, String > yearData_ = new HashMap<Integer, String>() ;
  private final static String VALUE = "value".intern() ; 
  public UIYearView() throws Exception {
    super() ;
  }

  protected void yearNext(int years) {
    calendar_.add(Calendar.YEAR, years) ;
  }
  protected void yearBack(int years) {
    calendar_.add(Calendar.YEAR, years) ;
  }
  private Map<Integer, String> getValueMap() { return yearData_ ; }
  public void refresh() throws Exception { 
    System.out.println("\n\n>>>>>>>>>> YEAR VIEW") ;
    yearData_.clear() ;
    Calendar cal =  new GregorianCalendar(getCurrentYear(), 0, 1, 0, 0, 0) ;
    Calendar cal2 = new GregorianCalendar(getCurrentYear(), 0, 1, 0, 0, 0) ;
    Calendar beginYear = CalendarUtils.getBeginDay(cal) ;
    cal2.add(Calendar.YEAR, 1) ;
    Calendar endYear = CalendarUtils.getEndDay(cal2) ;
    CalendarService calendarService = getApplicationComponent(CalendarService.class) ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    EventQuery eventQuery = new EventQuery() ;
    eventQuery.setFromDate(beginYear) ;
    eventQuery.setToDate(endYear) ;
    yearData_ = calendarService.searchHightLightEvent(SessionsUtils.getSystemProvider(), username, eventQuery, getPublicCalendars());
  }
  
 
  @Override
  public LinkedHashMap<String, CalendarEvent> getDataMap() {
    // TODO Auto-generated method stub
    return null;
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


}
