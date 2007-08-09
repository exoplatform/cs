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
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
    template =  "app:/templates/forum/webui/UITopicDetail.gtmpl", 
    events = {
      @EventConfig(listeners = UITopicDetail.AddPostActionListener.class )  
    }
)
public class UITopicDetail extends UIForm  {
  public UITopicDetail() throws Exception {
    // render post page list
    // render actions bar
    // render posts detail and post actions
    addChild(UIPostRules.class, null, null);
    addChild(UIForumLinks.class, null, null);
  }
  
  static public class AddPostActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;      
    }
  }
}
