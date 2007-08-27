/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui;

import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/calendar/webui/UIListView.gtmpl",
    events = {
      @EventConfig(listeners = UIListView.AddEventActionListener.class),      
      @EventConfig(listeners = UIListView.DeleteEventActionListener.class),
      @EventConfig(listeners = UIListView.ChangeCategoryActionListener.class)
    }
)
public class UIListView extends UIForm {
  
  public UIListView() {
    CalendarService calendarService = (CalendarService)PortalContainer.getComponent(CalendarService.class) ;
    //calendarService.getEventCategories(Utils.)
  }
  
  static  public class AddEventActionListener extends EventListener<UIListView> {
    public void execute(Event<UIListView> event) throws Exception {
      UIListView uiForm = event.getSource() ;
      System.out.println(" ===========> AddEventActionListener") ;
    }
  }
  static  public class DeleteEventActionListener extends EventListener<UIListView> {
    public void execute(Event<UIListView> event) throws Exception {
      UIListView uiForm = event.getSource() ;
      System.out.println(" ===========> DeleteEventActionListener") ;
    }
  }
  static  public class ChangeCategoryActionListener extends EventListener<UIListView> {
    public void execute(Event<UIListView> event) throws Exception {
      UIListView uiForm = event.getSource() ;
    }
  }
  
}
