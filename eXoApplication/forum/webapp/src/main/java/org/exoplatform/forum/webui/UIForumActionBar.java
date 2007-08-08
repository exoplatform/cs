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
    template =  "app:/templates/forum/webui/UIForumActionBar.gtmpl", 
    events = {
        @EventConfig(listeners = UIForumActionBar.AddCategoryActionListener.class),
        @EventConfig(listeners = UIForumActionBar.AddForumActionListener.class),
        @EventConfig(listeners = UIForumActionBar.ManageModeratorActionListener.class),
        @EventConfig(listeners = UIForumActionBar.ForumOptionActionListener.class)
    }
)
public class UIForumActionBar extends UIContainer  {
  public UIForumActionBar() throws Exception {    
  } 
  
  static public class AddCategoryActionListener extends EventListener<UIForumActionBar> {
    public void execute(Event<UIForumActionBar> event) throws Exception {
      UIForumActionBar uiActionBar = event.getSource() ;      
    }
  }  
  
  static public class AddForumActionListener extends EventListener<UIForumActionBar> {
    public void execute(Event<UIForumActionBar> event) throws Exception {
      UIForumActionBar uiActionBar = event.getSource() ;      
    }
  }  
  static public class ManageModeratorActionListener extends EventListener<UIForumActionBar> {
    public void execute(Event<UIForumActionBar> event) throws Exception {
      UIForumActionBar uiActionBar = event.getSource() ;      
    }
  }  
  
  static public class ForumOptionActionListener extends EventListener<UIForumActionBar> {
    public void execute(Event<UIForumActionBar> event) throws Exception {
      UIForumActionBar uiActionBar = event.getSource() ;      
    }
  }  
}
