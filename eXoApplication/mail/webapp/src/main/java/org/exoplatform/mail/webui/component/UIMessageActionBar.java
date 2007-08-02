/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui.component;

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
    template =  "app:/templates/mail/webui/component/UIMessageActionBar.gtmpl",
    events = {
        @EventConfig(listeners = UIMessageActionBar.ReplyActionListener.class),
        @EventConfig(listeners = UIMessageActionBar.ReplyAllActionListener.class),
        @EventConfig(listeners = UIMessageActionBar.ForwardActionListener.class), 
        @EventConfig(listeners = UIMessageActionBar.MarkAsReadActionListener.class),
        @EventConfig(listeners = UIMessageActionBar.MarkAsUnReadActionListener.class),
        @EventConfig(listeners = UIMessageActionBar.AddStarActionListener.class),
        @EventConfig(listeners = UIMessageActionBar.RemoveStarActionListener.class),
        @EventConfig(listeners = UIMessageActionBar.AddTagActionListener.class),
        @EventConfig(listeners = UIMessageActionBar.MoveMessagesActionListener.class),
        @EventConfig(listeners = UIMessageActionBar.ImportActionListener.class),
        @EventConfig(listeners = UIMessageActionBar.ExportActionListener.class)
    }
)

public class UIMessageActionBar extends UIContainer {
  
  public UIMessageActionBar()throws Exception {}
  
  static  public class ReplyActionListener extends EventListener<UIMessageActionBar> {    
    public void execute(Event<UIMessageActionBar> event) throws Exception {
      UIMessageActionBar uiActionBar = event.getSource() ;      
    }
  }

  static public class ReplyAllActionListener extends EventListener<UIMessageActionBar> {
    public void execute(Event<UIMessageActionBar> event) throws Exception {
      UIMessageActionBar uiActionBar = event.getSource() ;      
    }
  } 
     
  static public class ForwardActionListener extends EventListener<UIMessageActionBar> {
    public void execute(Event<UIMessageActionBar> event) throws Exception {
      UIMessageActionBar uiActionBar = event.getSource() ;      
    }
  }  
  static public class MarkAsReadActionListener extends EventListener<UIMessageActionBar> {
    public void execute(Event<UIMessageActionBar> event) throws Exception {
      UIMessageActionBar uiActionBar = event.getSource() ;      
    }
  }
  static public class MarkAsUnReadActionListener extends EventListener<UIMessageActionBar> {
    public void execute(Event<UIMessageActionBar> event) throws Exception {
      UIMessageActionBar uiActionBar = event.getSource() ;      
    }
  }
  static public class AddStarActionListener extends EventListener<UIMessageActionBar> {
    public void execute(Event<UIMessageActionBar> event) throws Exception {
      UIMessageActionBar uiActionBar = event.getSource() ;      
    }
  }
  static public class RemoveStarActionListener extends EventListener<UIMessageActionBar> {
    public void execute(Event<UIMessageActionBar> event) throws Exception {
      UIMessageActionBar uiActionBar = event.getSource() ;      
    }
  }
  static public class AddTagActionListener extends EventListener<UIMessageActionBar> {
    public void execute(Event<UIMessageActionBar> event) throws Exception {
      UIMessageActionBar uiActionBar = event.getSource() ;      
    }
  }
  static public class MoveMessagesActionListener extends EventListener<UIMessageActionBar> {
    public void execute(Event<UIMessageActionBar> event) throws Exception {
      UIMessageActionBar uiActionBar = event.getSource() ;      
    }
  }
  static public class ImportActionListener extends EventListener<UIMessageActionBar> {
    public void execute(Event<UIMessageActionBar> event) throws Exception {
      UIMessageActionBar uiActionBar = event.getSource() ;      
    }
  }
  static public class ExportActionListener extends EventListener<UIMessageActionBar> {
    public void execute(Event<UIMessageActionBar> event) throws Exception {
      UIMessageActionBar uiActionBar = event.getSource() ;      
    }
  }
}