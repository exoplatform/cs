/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/mail/webui/component/UISelectAccountForm.jstmpl",
    events = {
      @EventConfig(listeners = UISelectAccountForm.AddAccountActionListener.class)      
    }
)
public class UISelectAccountForm extends UIForm {
  
  public UISelectAccountForm() {
    
  }
  
  static  public class AddAccountActionListener extends EventListener<UISelectAccountForm> {
    public void execute(Event<UISelectAccountForm> event) throws Exception {
      UISelectAccountForm uiForm = event.getSource() ;
    }
  }
}
