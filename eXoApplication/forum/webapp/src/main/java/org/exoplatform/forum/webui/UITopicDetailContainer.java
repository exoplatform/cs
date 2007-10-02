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
    template =  "app:/templates/forum/webui/UITopicDetailContainer.gtmpl", 
    events = {
      @EventConfig(listeners = UITopicDetailContainer.AddPostActionListener.class )  
    }
)
public class UITopicDetailContainer extends UIContainer  {
  public UITopicDetailContainer() throws Exception {
    addChild(UITopicPoll.class, null, null).setRendered(false) ;
    addChild(UITopicDetail.class, null, null) ;
  }
  
  static public class AddPostActionListener extends EventListener<UITopicDetailContainer> {
    public void execute(Event<UITopicDetailContainer> event) throws Exception {
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;      
    }
  }
}
