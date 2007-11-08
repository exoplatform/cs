/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui.popup;

import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SARL
 * Author : Phung Nam
 *          phunghainam@gmail.com
 * Nov 8, 2007  
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class, 
    template = "app:/templates/mail/webui/UIAddContactForm.gtmpl", 
    events = {
      @EventConfig(listeners = UIAddContactForm.SaveActionListener.class),
      @EventConfig(listeners = UIAddContactForm.CancelActionListener.class, phase = Phase.DECODE)
    }
)
    
public class UIAddContactForm extends UIForm implements UIPopupComponent {
  
  public UIAddContactForm() { }
  
  public void activate() throws Exception { }

  public void deActivate() throws Exception { }
  
  public static class SaveActionListener extends EventListener<UIAddContactForm> {
    public void execute(Event<UIAddContactForm> event) throws Exception {
      System.out.println(" === >>> Quick Add Contact") ; 
    }
  }
  
  public static class CancelActionListener extends EventListener<UIAddContactForm> {
    public void execute(Event<UIAddContactForm> event) throws Exception {
      event.getSource().getAncestorOfType(UIMailPortlet.class).cancelAction();
    }
  }
}