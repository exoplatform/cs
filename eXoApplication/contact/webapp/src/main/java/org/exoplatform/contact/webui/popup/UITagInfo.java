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


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/contact/webui/popup/UITagInfo.gtmpl",
    events = {     
      @EventConfig(listeners = UITagInfo.CancelActionListener.class)
    }
)
public class UITagInfo extends UIForm implements UIPopupComponent {
  
  public UITagInfo() { }
  public String[] getActions() { return new String[] { "Cancel" } ; }
  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
  
  static  public class CancelActionListener extends EventListener<UITagInfo> {
    public void execute(Event<UITagInfo> event) throws Exception {
      UITagInfo uiEditTagForm = event.getSource() ;
      UIPopupAction uiPopupAction = uiEditTagForm.getAncestorOfType(UIPopupAction.class) ;
      uiPopupAction.deActivate() ;
    }
  }
}