/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.webui.popup;

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
    template = "app:/templates/forum/webui/UIPollForm.gtmpl",
    events = {
      @EventConfig(listeners = UIPollForm.SaveActionListener.class), 
      @EventConfig(listeners = UIPollForm.CancelActionListener.class)
    }
)
public class UIPollForm extends UIForm {
  
  
  public UIPollForm() {
    
  }
  
  static  public class SaveActionListener extends EventListener<UIPollForm> {
    public void execute(Event<UIPollForm> event) throws Exception {
      UIPollForm uiForm = event.getSource() ;
    }
  }
  static  public class CancelActionListener extends EventListener<UIPollForm> {
    public void execute(Event<UIPollForm> event) throws Exception {
      UIPollForm uiForm = event.getSource() ;
    }
  }
}
