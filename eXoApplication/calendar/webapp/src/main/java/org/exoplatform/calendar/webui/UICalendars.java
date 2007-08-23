/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui;

import java.util.List;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.calendar.webui.popup.UICalendarCategoryForm;
import org.exoplatform.calendar.webui.popup.UICalendarForm;
import org.exoplatform.calendar.webui.popup.UIPopupAction;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/templates/calendar/webui/UICalendars.gtmpl",
    events = {
        @EventConfig(listeners = UICalendars.AddCalendarActionListener.class),
        @EventConfig(listeners = UICalendars.AddCalendarCategoryActionListener.class)
    }
)
public class UICalendars extends UIForm  {
  public UICalendars() throws Exception {
    
  } 
  
  private List<GroupCalendarData> getPersonalCategories() throws Exception{
    CalendarService calendarService = (CalendarService)PortalContainer.getComponent(CalendarService.class) ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    return calendarService.getCalendarCategories(username);
  }
  
  private List<GroupCalendarData> getSharedGroups() throws Exception{
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    String[] groups = CalendarUtils.getUserGroups(username) ;
    CalendarService calendarService = (CalendarService)PortalContainer.getComponent(CalendarService.class) ;
    return calendarService.getGroupCalendars(groups) ;
  }
  
  static  public class AddCalendarActionListener extends EventListener<UICalendars> {
    public void execute(Event<UICalendars> event) throws Exception {
      UICalendars uiComponent = event.getSource() ;
      UICalendarPortlet forumPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
      UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
      popupAction.activate(UICalendarForm.class, 600) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  static  public class AddCalendarCategoryActionListener extends EventListener<UICalendars> {
    public void execute(Event<UICalendars> event) throws Exception {
      UICalendars uiComponent = event.getSource() ;
      UICalendarPortlet forumPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
      UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
      popupAction.activate(UICalendarCategoryForm.class, 600) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
}
