/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui.popup;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/mail/webui/UIViewMessage.jstmpl",
    events = {
      @EventConfig(listeners = UIViewMessage.ReplyActionListener.class),      
      @EventConfig(listeners = UIViewMessage.ReplyAllActionListener.class),
      @EventConfig(listeners = UIViewMessage.ForwardActionListener.class),
      @EventConfig(listeners = UIViewMessage.DeleteActionListener.class),      
      @EventConfig(listeners = UIViewMessage.PrintActionListener.class),
      @EventConfig(listeners = UIViewMessage.CloseActionListener.class)
    }
)
public class UIViewMessage extends UIComponent {
  
  public UIViewMessage() {
    
  }
  
  static  public class ReplyActionListener extends EventListener<UIViewMessage> {
    public void execute(Event<UIViewMessage> event) throws Exception {
      UIViewMessage uiForm = event.getSource() ;
    }
  }
  static  public class ReplyAllActionListener extends EventListener<UIViewMessage> {
    public void execute(Event<UIViewMessage> event) throws Exception {
      UIViewMessage uiForm = event.getSource() ;
    }
  }
  static  public class ForwardActionListener extends EventListener<UIViewMessage> {
    public void execute(Event<UIViewMessage> event) throws Exception {
      UIViewMessage uiForm = event.getSource() ;
    }
  }
  static  public class DeleteActionListener extends EventListener<UIViewMessage> {
    public void execute(Event<UIViewMessage> event) throws Exception {
      UIViewMessage uiForm = event.getSource() ;
    }
  }
  static  public class PrintActionListener extends EventListener<UIViewMessage> {
    public void execute(Event<UIViewMessage> event) throws Exception {
      UIViewMessage uiForm = event.getSource() ;
    }
  }
  static  public class CloseActionListener extends EventListener<UIViewMessage> {
    public void execute(Event<UIViewMessage> event) throws Exception {
      UIViewMessage uiForm = event.getSource() ;
    }
  }
}
