/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui.popup;

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
    template = "app:/templates/contact/webui/UIContactPreviewForm.gtmpl",
    events = {
      @EventConfig(listeners = UIContactPreviewForm.SaveActionListener.class),      
      @EventConfig(listeners = UIContactPreviewForm.CancelActionListener.class)
    }
)
public class UIContactPreviewForm extends UIForm {
  
  public UIContactPreviewForm() {
    
  }
  
  static  public class SaveActionListener extends EventListener<UIContactPreviewForm> {
    public void execute(Event<UIContactPreviewForm> event) throws Exception {
      UIContactPreviewForm uiForm = event.getSource() ;
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIContactPreviewForm> {
    public void execute(Event<UIContactPreviewForm> event) throws Exception {
      UIContactPreviewForm uiForm = event.getSource() ;
    }
  }
}
