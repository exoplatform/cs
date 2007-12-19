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

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.SessionsUtils;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.hibernate.type.YesNoType;

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
    addChild(UIMonthView.class, null, null).setRendered(false) ;
    addChild(UIDayView.class, null, null).setRendered(false) ;
    addChild(UIWeekView.class, null, null).setRendered(false) ;
    addChild(UIYearView.class, null, null).setRendered(false) ;
    addChild(UIListContainer.class, null, null).setRendered(false) ;
    addChild(UIScheduleView.class, null, null).setRendered(false) ;
    CalendarService cservice = CalendarUtils.getCalendarService() ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    CalendarSetting calendarSetting = cservice.getCalendarSetting(SessionsUtils.getSessionProvider(), username) ;
    setRenderedChild(TYPES[Integer.parseInt(calendarSetting.getViewType())]) ;
    refresh() ;
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
