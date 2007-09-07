/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(   
    template = "app:/templates/calendar/webui/UIListView.gtmpl",
    lifecycle = UIFormLifecycle.class,
    events = {
      @EventConfig(listeners = UICalendarView.RefreshActionListener.class),
      @EventConfig(listeners = UICalendarView.AddEventActionListener.class),      
      @EventConfig(listeners = UICalendarView.DeleteEventActionListener.class),
      @EventConfig(listeners = UICalendarView.ChangeCategoryActionListener.class), 
      @EventConfig(listeners = UICalendarView.AddCategoryActionListener.class)
    }
)
public class UIListView extends UICalendarView {
  public UIListView() throws Exception{
    super() ;
  } 
  public String getTemplate(){
    if(isShowEvent) return "app:/templates/calendar/webui/UIEventView.gtmpl" ;
    return "app:/templates/calendar/webui/UITaskView.gtmpl" ;
  }
}
