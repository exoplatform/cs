/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui;

import java.util.Calendar;

import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.webui.popup.UICalendarSettingForm;
import org.exoplatform.calendar.webui.popup.UIFeed;
import org.exoplatform.calendar.webui.popup.UIPopupAction;
import org.exoplatform.calendar.webui.popup.UIQuickAddEvent;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
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

  private String currentView_ = "UIDayView" ;
  
  protected String[] getViewTypes() {return UICalendarViewContainer.TYPES ;} 
  protected String getCurrentView() {return currentView_ ;}
  protected void setCurrentView(String viewName) {currentView_ = viewName ;}
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
        uiQuickAddEvent.setId("UIQuickAddTask") ;
      }
      uiQuickAddEvent.init() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }
  static public class ChangeViewActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ;     
      String viewType = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UICalendarPortlet uiPortlet = uiActionBar.getAncestorOfType(UICalendarPortlet.class) ;
      UICalendarViewContainer uiViewContainer = uiPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
      uiViewContainer.refresh() ;
      uiViewContainer.setRenderedChild(viewType);
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
      UICalendarView renderedChild = (UICalendarView)uiViewContainer.getRenderedChild() ;
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
      popupAction.activate(UICalendarSettingForm.class, 600) ;
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
