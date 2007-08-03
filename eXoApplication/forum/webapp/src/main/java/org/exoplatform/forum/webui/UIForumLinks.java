/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.webui;

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
    template = "app:/templates/forum/webui/UIForumLinks.jstmpl",
    events = {
      @EventConfig(listeners = UIForumLinks.SelectActionListener.class)      
    }
)
public class UIForumLinks extends UIForm {
  public UIForumLinks() {
    
  }
  
  static  public class SelectActionListener extends EventListener<UIForumLinks> {
    public void execute(Event<UIForumLinks> event) throws Exception {
      UIForumLinks uiForm = event.getSource() ;
    }
  }
}
