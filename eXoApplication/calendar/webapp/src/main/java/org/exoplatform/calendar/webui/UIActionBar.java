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

import java.util.List;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.webui.popup.UICalendarSettingForm;
import org.exoplatform.calendar.webui.popup.UIFeed;
import org.exoplatform.calendar.webui.popup.UIPopupAction;
import org.exoplatform.calendar.webui.popup.UIQuickAddEvent;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
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
  public UIActionBar() throws Exception {}
  protected String[] getViewTypes() {return UICalendarViewContainer.TYPES ;} 
  protected String getCurrentView() {return currentView_ ;}
  public void setCurrentView(String viewName) {currentView_ = viewName ;}

  protected boolean isShowPane() {return isShowPane_ ;}
  protected void setShowPane(boolean isShow) {isShowPane_ = isShow ;}
  private SessionProvider getSession() {
    return SessionProviderFactory.createSessionProvider() ;
  }
  static public class QuickAddEventActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ;
      CalendarService calendarService = uiActionBar.getApplicationComponent(CalendarService.class) ;
      UIApplication uiApp = uiActionBar.getAncestorOfType(UIApplication.class) ;
      List<org.exoplatform.calendar.service.Calendar> privateCalendars = 
        calendarService.getUserCalendars(uiActionBar.getSession(), CalendarUtils.getCurrentUser(), true) ;
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
      uiQuickAddEvent.setSelectedCategory("Meeting") ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }

  static public class ChangeViewActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ;     
      String viewType = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String categoryId = event.getRequestContext().getRequestParameter("categoryId") ;
      System.out.println("category id " + categoryId);
      UICalendarPortlet uiPortlet = uiActionBar.getAncestorOfType(UICalendarPortlet.class) ;
      UICalendarViewContainer uiViewContainer = uiPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
      uiViewContainer.initView(viewType);      
      uiViewContainer.refresh() ;
      UIMiniCalendar miniCalendar = uiPortlet.findFirstComponentOfType(UIMiniCalendar.class) ;
      miniCalendar.setCategoryId(categoryId) ; 
      if(uiViewContainer.getRenderedChild() instanceof UIListContainer) {
        UIListContainer listContainer = (UIListContainer)uiViewContainer.getRenderedChild() ;
        listContainer.setSelectedCategory(categoryId) ;
      } else  if(uiViewContainer.getRenderedChild() instanceof UIYearView) {
        UIYearView uiYearView = (UIYearView)uiViewContainer.getRenderedChild() ;
        uiYearView.setCategoryId(categoryId) ;
        uiYearView.refresh() ;
      }
      uiActionBar.setCurrentView(viewType) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiActionBar) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiViewContainer) ;
    }
  }  

  static public class TodayActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ;     
      UICalendarPortlet uiPortlet = uiActionBar.getAncestorOfType(UICalendarPortlet.class) ;
      UIMiniCalendar uiMiniCalendar = uiPortlet.findFirstComponentOfType(UIMiniCalendar.class) ;
      UICalendarViewContainer uiViewContainer = uiPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
      CalendarView renderedChild = (CalendarView)uiViewContainer.getRenderedChild() ;
      renderedChild.setCurrentCalendar(CalendarUtils.getInstanceTempCalendar()) ;
      renderedChild.refresh() ;
      uiMiniCalendar.setCurrentCalendar(CalendarUtils.getInstanceTempCalendar()) ;
      uiMiniCalendar.updateMiniCal() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMiniCalendar) ;
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
      //String username = Util.getPortalRequestContext().getRemoteUser() ;
      CalendarSetting calendarSetting = calendarPortlet.getCalendarSetting() ;
        //cservice.getCalendarSetting(uiActionBar.getSession(), username) ;
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
