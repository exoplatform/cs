/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui;
import java.util.Calendar;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
    lifecycle = UIContainerLifecycle.class,  
    template =  "app:/templates/calendar/webui/UIListContainer.gtmpl"
)
public class UIListContainer extends UIContainer implements CalendarView {
  public UIListContainer() throws Exception {
    addChild(UIListView.class, null, null).setRendered(true) ;
    addChild(UIPreview.class, null, null).setRendered(true) ;    
  }

  public void refresh() throws Exception {
    getChild(UIListView.class).refresh() ;
    getChild(UIPreview.class).refresh() ;
  }
  public void update() {
    
  }

  public void setCurrentCalendar(Calendar cal) {
    // TODO Auto-generated method stub
    
  }
  
  
}
