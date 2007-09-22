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
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL
 * Author : Philippe Aristote
 *          philippe.aristote@gmail.com
 * Aug 10, 2007  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/mail/webui/UIMailSettings.gtmpl",
    events = {
        @EventConfig(listeners = UIAccountSetting.SaveActionListener.class),
        @EventConfig(listeners = UIAccountSetting.CancelActionListener.class)
    }
)
public class UIMailSettings extends UIForm implements UIPopupComponent {

  
  public UIMailSettings() {
    
  }
  
  public String[] getActions() { return new String[]{"Save", "Cancel"}; }
  
  public void activate() throws Exception { }

  public void deActivate() throws Exception { }
  
  static  public class SaveActionListener extends EventListener<UIAccountSetting> {
    public void execute(Event<UIAccountSetting> event) throws Exception {
      System.out.println(" ==========> SaveActionListener") ;
      event.getSource().getAncestorOfType(UIMailPortlet.class).cancelAction();
    }
  }
 
  static  public class CancelActionListener extends EventListener<UIAccountSetting> {
    public void execute(Event<UIAccountSetting> event) throws Exception {
      System.out.println(" ==========> CancelActionListener") ;
      event.getSource().getAncestorOfType(UIMailPortlet.class).cancelAction();
    }
  }
}
