/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui.popup;

import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.Group;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;

/**
 * Created by The eXo Platform SARL
 * Author : Phung Nam
 *          phunghainam@gmail.com
 * Nov 26, 2007  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UIAddGroupForm.AddActionListener.class), 
      @EventConfig(listeners = UIAddGroupForm.CancelActionListener.class, phase = Phase.DECODE)
    }  
)
public class UIAddGroupForm extends UIForm implements UIPopupComponent{
  public final static String GROUP_NAME = "group-name".intern();
  public final static String GROUP_DESCRIPTION = "group-description".intern();
  
  public UIAddGroupForm() {
    addUIFormInput(new UIFormStringInput(GROUP_NAME, GROUP_NAME, null));
    addUIFormInput(new UIFormTextAreaInput(GROUP_DESCRIPTION, GROUP_DESCRIPTION, null));
  }
  
  public String[] getActions() {return (new String[]{"Add", "Cancel"}); }
  
  public void activate() throws Exception { }

  public void deActivate() throws Exception { }
  
  static  public class AddActionListener extends EventListener<UIAddGroupForm> {
    public void execute(Event<UIAddGroupForm> event) throws Exception {
      UIAddGroupForm uiAddGroupForm = event.getSource();
      UIMailPortlet uiPortlet = uiAddGroupForm.getAncestorOfType(UIMailPortlet.class);
      String groupName = uiAddGroupForm.getUIStringInput(GROUP_NAME).getValue();
      String groupDescription = uiAddGroupForm.getUIFormTextAreaInput(GROUP_DESCRIPTION).getValue();
      String username = MailUtils.getCurrentUser();
      ContactService contactSrv = uiAddGroupForm.getApplicationComponent(ContactService.class);
      UIApplication uiApp = uiAddGroupForm.getAncestorOfType(UIApplication.class) ;
      if (groupName == null || groupName.equals("")) {
        uiApp.addMessage(new ApplicationMessage("UIAddGroupForm.msg.group-name-required", null,
          ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ; 
      } else {
        ContactGroup group = new ContactGroup();
        group.setName(groupName);
        group.setDescription(groupDescription);
        contactSrv.saveGroup(username, group, true);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.getChild(UIPopupAction.class)) ;
      }
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIAddGroupForm> {
    public void execute(Event<UIAddGroupForm> event) throws Exception {
      
    }
  }

}
