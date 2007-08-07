/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.webui ;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
    template =  "app:/templates/forum/webui/UITopicList.gtmpl",
    events = {
        @EventConfig(listeners = UITopicList.SelectTopicActionListener.class)
        
    }
)

public class UITopicList extends UIComponent {
  public UITopicList() throws Exception {}
  
  
  static public class SelectTopicActionListener extends EventListener<UITopicList> {
    public void execute(Event<UITopicList> event) throws Exception {      
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;      
    }
  }
  
}