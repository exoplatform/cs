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

import java.util.GregorianCalendar;
import java.util.List;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.SessionsUtils;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.webui.popup.UICalendarSettingForm;
import org.exoplatform.calendar.webui.popup.UIFeed;
import org.exoplatform.calendar.webui.popup.UIPopupAction;
import org.exoplatform.calendar.webui.popup.UIQuickAddEvent;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
    template =  "app:/templates/calendar/webui/UIActionBar.gtmpl", 
    events = {
        @EventConfig(listeners = UIActionBar.QuickAddEventActionListener.class),
        @EventConfig(listeners = UIActionBar.ChangeViewActionListener.class),
        @EventConfig(listeners = UIActionBar.SettingActionListener.class),
        @EventConfig(listeners = UIActionBar.RSSActionListener.class),
        @EventConfig(listeners = UIActionBar.TodayActionListener.class)
    }
)
public class UIActionBar extends UIContainer  {

  private boolean isShowPane_ = true ;
  private String currentView_ = null ;
  public UIActionBar() throws Exception {
    CalendarService calService = getApplicationComponent(CalendarService.class) ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    CalendarSetting calSetting = calService.getCalendarSetting(SessionsUtils.getSessionProvider(), username) ;
    currentView_ = UICalendarViewContainer.TYPES[Integer.parseInt(calSetting.getViewType())] ;
  }
  protected String[] getViewTypes() {return UICalendarViewContainer.TYPES ;} 
  protected String getCurrentView() {return currentView_ ;}
  public void setCurrentView(String viewName) {currentView_ = viewName ;}
  
  protected boolean isShowPane() {return isShowPane_ ;}
  protected void setShowPane(boolean isShow) {isShowPane_ = isShow ;}
  
  
  static public class QuickAddEventActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
    	UIActionBar uiActionBar = event.getSource() ;
      CalendarService calendarService = uiActionBar.getApplicationComponent(CalendarService.class) ;
      UIApplication uiApp = uiActionBar.getAncestorOfType(UIApplication.class) ;
      List<org.exoplatform.calendar.service.Calendar> privateCalendars = 
        calendarService.getUserCalendars(SessionsUtils.getSessionProvider(), CalendarUtils.getCurrentUser(), true) ;
      if(privateCalendars.isEmpty()) {
        uiApp.addMessage(new ApplicationMessage("UICalendarView.msg.calendar-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      String type = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UICalendarPortlet uiPortlet = uiActionBar.getAncestorOfType(UICalendarPortlet.class) ;
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
      UIQuickAddEvent uiQuickAddEvent = uiPopupAction.activate(UIQuickAddEvent.class, 600) ;
      if(CalendarEvent.TYPE_EVENT.equals(type)) {
        uiQuickAddEvent.setEvent(true) ;        
      } else {
        uiQuickAddEvent.setEvent(false) ;
        uiQuickAddEvent.setId(UIQuickAddEvent.UIQUICKADDTASK) ;
      }
      uiQuickAddEvent.init(uiPortlet.getCalendarSetting(), null, null) ;
      uiQuickAddEvent.update("0", null) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }

  static public class ChangeViewActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ;     
      String viewType = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UICalendarPortlet uiPortlet = uiActionBar.getAncestorOfType(UICalendarPortlet.class) ;
      UICalendarViewContainer uiViewContainer = uiPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
      uiViewContainer.initView(viewType);      
      uiViewContainer.refresh() ;
      uiActionBar.setCurrentView(viewType) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiActionBar) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiViewContainer) ;
    }
  }  
  
  static public class TodayActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ;     
      UICalendarPortlet uiPortlet = uiActionBar.getAncestorOfType(UICalendarPortlet.class) ;
      UICalendarViewContainer uiViewContainer = uiPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
      CalendarView renderedChild = (CalendarView)uiViewContainer.getRenderedChild() ;
      renderedChild.setCurrentCalendar(CalendarUtils.getInstanceTempCalendar()) ;
      renderedChild.refresh() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiViewContainer) ;
    }
  }  
  static public class SettingActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ;
      UICalendarPortlet calendarPortlet = uiActionBar.getAncestorOfType(UICalendarPortlet.class) ;
      UIPopupAction popupAction = calendarPortlet.getChild(UIPopupAction.class) ;
      UICalendarSettingForm uiCalendarSettingForm = popupAction.activate(UICalendarSettingForm.class, 600) ;
      CalendarService cservice = CalendarUtils.getCalendarService() ;
      String username = Util.getPortalRequestContext().getRemoteUser() ;
      CalendarSetting calendarSetting = cservice.getCalendarSetting(SessionsUtils.getSessionProvider(), username) ;
      uiCalendarSettingForm.init(calendarSetting, cservice) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }

  static public class RSSActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ;
      UICalendarPortlet calendarPortlet = uiActionBar.getAncestorOfType(UICalendarPortlet.class) ;
      UIPopupAction popupAction = calendarPortlet.getChild(UIPopupAction.class) ;
      popupAction.activate(UIFeed.class, 600) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
}
