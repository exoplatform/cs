/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.webui;

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
    template =  "app:/templates/forum/webui/UIBreadcumbs.gtmpl" +
    		"",
    events = {
        @EventConfig(listeners = UIBreadcumbs.ChangePathActionListener.class),
        @EventConfig(listeners = UIBreadcumbs.RssActionListener.class)
    }
)

public class UIBreadcumbs extends UIContainer {
  
  public UIBreadcumbs()throws Exception {}
  
  static public class ChangePathActionListener extends EventListener<UIBreadcumbs> {
    public void execute(Event<UIBreadcumbs> event) throws Exception {
      UIBreadcumbs uiActionBar = event.getSource() ;      
    }
  }  
  
  static public class RssActionListener extends EventListener<UIBreadcumbs> {
    public void execute(Event<UIBreadcumbs> event) throws Exception {
      UIBreadcumbs uiActionBar = event.getSource() ;      
    }
  }  
}