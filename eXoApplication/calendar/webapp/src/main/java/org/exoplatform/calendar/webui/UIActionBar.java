/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.webui.popup.UICalendarSettingForm;
import org.exoplatform.calendar.webui.popup.UIFeed;
import org.exoplatform.calendar.webui.popup.UIPopupAction;
import org.exoplatform.calendar.webui.popup.UIPopupContainer;
import org.exoplatform.calendar.webui.popup.UIQuickAddEvent;
import org.exoplatform.calendar.webui.popup.UITaskForm;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
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
        @EventConfig(listeners = UIActionBar.AddTasksActionListener.class),
        @EventConfig(listeners = UIActionBar.ChangeViewActionListener.class),
        @EventConfig(listeners = UIActionBar.SettingActionListener.class),
        @EventConfig(listeners = UIActionBar.RSSActionListener.class),
        @EventConfig(listeners = UIActionBar.ShowHiddenActionListener.class),
        @EventConfig(listeners = UIActionBar.TodayActionListener.class)
    }
)
public class UIActionBar extends UIContainer  {

  private boolean isShowPane_ = true ;
  private String currentView_ = null ;
  public UIActionBar() throws Exception {
    CalendarService calService = getApplicationComponent(CalendarService.class) ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    CalendarSetting calSetting = calService.getCalendarSetting(username) ;
    currentView_ = UICalendarViewContainer.TYPES[Integer.parseInt(calSetting.getViewType())] ;
  }
  protected String[] getViewTypes() {return UICalendarViewContainer.TYPES ;} 
  protected String getCurrentView() {return currentView_ ;}
  protected void setCurrentView(String viewName) {currentView_ = viewName ;}
  
  protected boolean isShowPane() {return isShowPane_ ;}
  protected void setShowPane(boolean isShow) {isShowPane_ = isShow ;}
  
  
  static public class QuickAddEventActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ;
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


  static public class AddTasksActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ;
      UICalendarPortlet uiPortlet = uiActionBar.getAncestorOfType(UICalendarPortlet.class) ;
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
      UIPopupContainer uiContainer = uiPopupAction.activate(UIPopupContainer.class, 700) ;
      uiContainer.setId(UIPopupContainer.UITASKPOPUP) ;
      UITaskForm uiTaskForm = uiContainer.createUIComponent(UITaskForm.class, null, null) ;
      uiTaskForm.initForm() ;
      uiContainer.addChild(uiTaskForm) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }
  
  
  static public class ChangeViewActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ;     
      String viewType = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UICalendarPortlet uiPortlet = uiActionBar.getAncestorOfType(UICalendarPortlet.class) ;
      UICalendarViewContainer uiViewContainer = uiPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
      if(viewType.equals("UIListContainer")) {
        UIListView uiListView = uiViewContainer.findFirstComponentOfType(UIListView.class) ;
        CalendarService calendarService = uiActionBar.getApplicationComponent(CalendarService.class) ;
        String username = CalendarUtils.getCurrentUser() ;
        EventQuery eventQuery = new EventQuery() ;
        java.util.Calendar fromcalendar = new GregorianCalendar(uiListView.getCurrentYear(), uiListView.getCurrentMonth(), uiListView.getCurrentDay()) ;
        fromcalendar.set(java.util.Calendar.HOUR, 0) ;
        fromcalendar.set(java.util.Calendar.MINUTE, 0) ;
        fromcalendar.set(java.util.Calendar.MILLISECOND, 0) ;
        eventQuery.setFromDate(fromcalendar) ;
        java.util.Calendar tocalendar = new GregorianCalendar(uiListView.getCurrentYear(), uiListView.getCurrentMonth(), uiListView.getCurrentDay()) ;
        tocalendar.set(java.util.Calendar.HOUR, tocalendar.getActualMaximum(java.util.Calendar.HOUR)) ;
        tocalendar.set(java.util.Calendar.MINUTE, tocalendar.getActualMaximum(java.util.Calendar.MINUTE)) ;
        tocalendar.set(java.util.Calendar.MILLISECOND, tocalendar.getActualMaximum(java.util.Calendar.MILLISECOND)) ;
        eventQuery.setToDate(tocalendar) ;
        uiListView.update(calendarService.searchEvent(username, eventQuery)) ; 
        uiListView.setShowEventAndTask(false) ;
        uiListView.setDisplaySearchResult(false) ;
        uiListView.isShowEvent_ = true ;
      }
      uiViewContainer.setRenderedChild(viewType);      
      uiActionBar.setCurrentView(viewType) ;
      uiViewContainer.refresh() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiActionBar) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiViewContainer) ;
    }
  }  
  
  static public class ShowHiddenActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ;    
      uiActionBar.isShowPane_ = ! uiActionBar.isShowPane_ ;
      UICalendarPortlet uiPortlet = uiActionBar.getAncestorOfType(UICalendarPortlet.class) ;
      UICalendarWorkingContainer uiWorkingContainer = uiPortlet.findFirstComponentOfType(UICalendarWorkingContainer.class) ;
      uiWorkingContainer.getChild(UICalendarContainer.class).setRendered(uiActionBar.isShowPane_ ) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiActionBar) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingContainer) ;
    }
  }  
  static public class TodayActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ;     
      UICalendarPortlet uiPortlet = uiActionBar.getAncestorOfType(UICalendarPortlet.class) ;
      UICalendarViewContainer uiViewContainer = uiPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
      CalendarView renderedChild = (CalendarView)uiViewContainer.getRenderedChild() ;
      renderedChild.setCurrentCalendar(Calendar.getInstance()) ;
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
      CalendarSetting calendarSetting = cservice.getCalendarSetting(username) ;
      uiCalendarSettingForm.init(calendarSetting) ;
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
