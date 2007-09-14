/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
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
    template = "app:/templates/calendar/webui/UIPopup/UIFeed.gtmpl",
    events = {
      @EventConfig(listeners = UIFeed.CloseActionListener.class)
    }
)
public class UIFeed extends UIForm implements UIPopupComponent{
  public UIFeed() {}
  
  public List getFeeds() throws Exception {
    CalendarService calService = CalendarUtils.getCalendarService() ;
    return calService.getFeeds(Util.getPortalRequestContext().getRemoteUser()) ;
  }
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}  
  
  static  public class SelectActionListener extends EventListener<UIFeed> {
    public void execute(Event<UIFeed> event) throws Exception {
      UIFeed uiForm = event.getSource() ;
      
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      //event.getRequestContext().getJavascriptManager().addJavascript("ajaxRedirect('" + downloadLink + "');") ;
      calendarPortlet.cancelAction() ;      
    }
  }
  
  static  public class CloseActionListener extends EventListener<UIFeed> {
    public void execute(Event<UIFeed> event) throws Exception {
      UIFeed uiForm = event.getSource() ;
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      calendarPortlet.cancelAction() ;
    }
  }  
}
