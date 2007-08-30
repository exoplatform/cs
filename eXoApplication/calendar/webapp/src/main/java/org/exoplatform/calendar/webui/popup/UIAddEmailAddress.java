/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui.popup;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Aug 30, 2007  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/calendar/webui/UIPopup/UIAddEmailAddress.gtmpl",
    events = {
        @EventConfig(listeners = UIAddEmailAddress.SaveActionListener.class),
        @EventConfig(listeners = UIAddEmailAddress.CancelActionListener.class)
      }
)
public class UIAddEmailAddress extends UIForm implements UIPopupComponent {

  public void activate() throws Exception {
    // TODO Auto-generated method stub
    
  }
  public void deActivate() throws Exception {
    // TODO Auto-generated method stub
    
  }
  static  public class SaveActionListener extends EventListener<UIAddEmailAddress> {
    public void execute(Event<UIAddEmailAddress> event) throws Exception {
      UIAddEmailAddress uiForm = event.getSource() ;
    }
  }
  static  public class CancelActionListener extends EventListener<UIAddEmailAddress> {
    public void execute(Event<UIAddEmailAddress> event) throws Exception {
      UIAddEmailAddress uiForm = event.getSource() ;
      uiForm.getAncestorOfType(UIPopupAction.class).deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax( uiForm.getAncestorOfType(UIPopupAction.class)) ;
    }
  }
}
