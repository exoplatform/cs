/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui;

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.calendar.CalendarUtils;
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
  final public static Map<String, String> VIEWS = new HashMap<String, String>() ;
  
  final public static String[] TYPES = {DAY_VIEW, WEEK_VIEW, MONTH_VIEW, YEAR_VIEW, LIST_VIEW, SCHEDULE_VIEW} ;


  public UICalendarViewContainer() throws Exception {
    VIEWS.put(CalendarSetting.DAY_VIEW, DAY_VIEW) ;
    VIEWS.put(CalendarSetting.WEEK_VIEW, WEEK_VIEW) ;
    VIEWS.put(CalendarSetting.MONTH_VIEW, MONTH_VIEW) ;
    VIEWS.put(CalendarSetting.YEAR_VIEW, YEAR_VIEW) ;
    VIEWS.put(CalendarSetting.LIST_VIEW, LIST_VIEW) ;
    VIEWS.put(CalendarSetting.SCHEDULE_VIEW, SCHEDULE_VIEW) ;
    addChild(UIMonthView.class, null, null).setRendered(false) ;
    addChild(UIDayView.class, null, null).setRendered(false) ;
    addChild(UIWeekView.class, null, null).setRendered(false) ;
    addChild(UIYearView.class, null, null).setRendered(false) ;
    addChild(UIListContainer.class, null, null).setRendered(false) ;
    addChild(UIScheduleView.class, null, null).setRendered(false) ;
    CalendarService cservice = CalendarUtils.getCalendarService() ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    CalendarSetting calendarSetting = cservice.getCalendarSetting(username) ;
    setRenderedChild(VIEWS.get(calendarSetting.getViewType())) ;
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
