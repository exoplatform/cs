/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui.popup;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SARL
 * Author : Phung Nam
 *          phunghainam@gmail.com
 * Nov 7, 2007  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/mail/webui/UIFeed.gtmpl",
    events = {  
      @EventConfig(listeners = UIFeed.RssActionListener.class),
      @EventConfig(listeners = UIFeed.CancelActionListener.class)
    }
)
public class UIFeed extends UIForm implements UIPopupComponent {
  
  public UIFeed() { }
   
  public String[] getAction() { return new String[] {"generate", "cancel"}; }
  
  public void activate() throws Exception { }

  public void deActivate() throws Exception { }
  
  static  public class RssActionListener extends EventListener<UIFeed> {
    public void execute(Event<UIFeed> event) throws Exception {
      
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIFeed> {
    public void execute(Event<UIFeed> event) throws Exception {

    }
  }
}