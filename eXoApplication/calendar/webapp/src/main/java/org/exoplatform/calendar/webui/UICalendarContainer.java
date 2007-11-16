/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
    lifecycle = UIContainerLifecycle.class ,
    template =  "app:/templates/calendar/webui/UICalendarContainer.gtmpl",
    events = {
      @EventConfig(listeners = UICalendarContainer.AddCalendarCategoryActionListener.class),
      @EventConfig(listeners = UICalendarContainer.AddCalendarActionListener.class)
    }
)
public class UICalendarContainer extends UIContainer  {
  public UICalendarContainer() throws Exception {
    addChild(UISearchForm.class, null, null) ;
    addChild(UIMiniCalendar.class, null, null) ;
    addChild(UICalendars.class, null, null) ;    
  } 
  
  static  public class AddCalendarCategoryActionListener extends EventListener<UICalendarContainer> {
    public void execute(Event<UICalendarContainer> event) throws Exception {
      UICalendarContainer uicomp = event.getSource() ;
    }
  }
  static  public class AddCalendarActionListener extends EventListener<UICalendarContainer> {
    public void execute(Event<UICalendarContainer> event) throws Exception {
      UICalendarContainer uicomp = event.getSource() ;
    }
  }
}
