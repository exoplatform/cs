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

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.SessionsUtils;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
    template =  "app:/templates/calendar/webui/UICalendarViewContainer.gtmpl"
)
public class UICalendarViewContainer extends UIContainer  {

  final public static String DAY_VIEW = "UIDayView".intern() ;
  final public static String WEEK_VIEW = "UIWeekView".intern() ;
  final public static String MONTH_VIEW = "UIMonthView".intern() ;
  final public static String YEAR_VIEW = "UIYearView".intern() ;
  final public static String LIST_VIEW = "UIListContainer".intern() ;
  final public static String SCHEDULE_VIEW = "UIScheduleView".intern() ;

  final public static String[] TYPES = {DAY_VIEW, WEEK_VIEW, MONTH_VIEW, YEAR_VIEW, LIST_VIEW, SCHEDULE_VIEW} ;

  public UICalendarViewContainer() throws Exception {
    initView(null) ;
    refresh() ;
  }  
  public void initView(String viewType) throws Exception {
    
    if(viewType == null) {
      CalendarService cservice = CalendarUtils.getCalendarService() ;
      String username = Util.getPortalRequestContext().getRemoteUser() ;
      CalendarSetting calendarSetting = cservice.getCalendarSetting(SessionsUtils.getSessionProvider(), username) ;
      viewType = TYPES[Integer.parseInt(calendarSetting.getViewType())] ;
    }
    if(viewType.equals("UIListContainer")) {
      /*UIListView uiListView = uiViewContainer.findFirstComponentOfType(UIListView.class) ;
      alendarService calendarService = uiActionBar.getApplicationComponent(CalendarService.class) ;
      String username = CalendarUtils.getCurrentUser() ;
      EventQuery eventQuery = new EventQuery() ;
      java.util.Calendar fromcalendar =  uiListView.getBeginDay(new GregorianCalendar(uiListView.getCurrentYear(), uiListView.getCurrentMonth(), uiListView.getCurrentDay())) ;
      eventQuery.setFromDate(fromcalendar) ;
      java.util.Calendar tocalendar =  uiListView.getEndDay(new GregorianCalendar(uiListView.getCurrentYear(), uiListView.getCurrentMonth(), uiListView.getCurrentDay())) ;
      eventQuery.setToDate(tocalendar) ;
      uiListView.update(calendarService.searchEvent(SessionsUtils.getSystemProvider(), username, eventQuery, uiListView.getPublicCalendars())) ;*/ 
      
    }
   /* addChild(UIMonthView.class, null, null).setRendered(false) ;
    addChild(UIWeekView.class, null, null).setRendered(false) ;
    addChild(UIYearView.class, null, null).setRendered(false) ;
    addChild(UIListContainer.class, null, null).setRendered(false) ;
    addChild(UIScheduleView.class, null, null).setRendered(false) ;
    */
    if(DAY_VIEW.equals(viewType)) {
      UIDayView uiView = getChild(UIDayView.class) ;
      if(uiView == null) uiView =  addChild(UIDayView.class, null, null) ;
      setRenderedChild(viewType) ;
    } else
    if(WEEK_VIEW.equals(viewType)) {
      UIWeekView uiView = getChild(UIWeekView.class) ;
      if(uiView == null) uiView =  addChild(UIWeekView.class, null, null) ;
      setRenderedChild(viewType) ;
    } else
    if(MONTH_VIEW.equals(viewType)) {
      UIMonthView uiView = getChild(UIMonthView.class) ;
      if(uiView == null) uiView =  addChild(UIMonthView.class, null, null) ;
      setRenderedChild(viewType) ;
    } else
    if(YEAR_VIEW.equals(viewType)) {
      UIYearView uiView = getChild(UIYearView.class) ;
      if(uiView == null) uiView =  addChild(UIYearView.class, null, null) ;
      setRenderedChild(viewType) ;
    } else
    if(LIST_VIEW.equals(viewType)) {
      UIListContainer uiView = getChild(UIListContainer.class) ;
      if(uiView == null) uiView =  addChild(UIListContainer.class, null, null) ;
      UIListView uiListView = uiView.getChild(UIListView.class) ;
      uiListView.refresh(null) ;
      uiListView.setShowEventAndTask(false) ;
      uiListView.setDisplaySearchResult(false) ;
      uiListView.isShowEvent_ = true ;
      setRenderedChild(viewType) ;
    } else
    if(SCHEDULE_VIEW.equals(viewType)) {
      UIScheduleView uiView = getChild(UIScheduleView.class) ;
      if(uiView == null) uiView =  addChild(UIScheduleView.class, null, null) ;
      setRenderedChild(viewType) ;
    }
  }
  public void refresh() throws Exception {
    for(UIComponent comp : getChildren()) {
      if(comp.isRendered() && comp instanceof CalendarView){
        ((CalendarView)comp).update() ;
        ((CalendarView)comp).refresh() ;
      }
      ((CalendarView)comp).applySeting() ;
    }
  }
  protected boolean isShowPane() {
    return getAncestorOfType(UICalendarWorkingContainer.class).getChild(UICalendarContainer.class).isRendered() ;
  }
  public UIComponent getRenderedChild() {
    for(UIComponent comp : getChildren()) {
      if(comp.isRendered()) return comp ;
    }
    return null ;
  }
}
