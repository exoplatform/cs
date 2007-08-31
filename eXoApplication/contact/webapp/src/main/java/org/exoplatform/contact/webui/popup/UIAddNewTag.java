/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui.popup;

import java.util.List;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.Tag;
import org.exoplatform.contact.webui.UIContactPortlet;
import org.exoplatform.contact.webui.UIContacts;
import org.exoplatform.contact.webui.UITags;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
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
    template = "system:/groovy/webui/form/UIForm.gtmpl", 
    events = {
      @EventConfig(listeners = UIAddNewTag.SaveActionListener.class),      
      @EventConfig(listeners = UIAddNewTag.CancelActionListener.class)
    }
)
public class UIAddNewTag extends UIForm implements UIPopupComponent {
  public static final String FIELD_TAGNAME_INPUT = "tagName";
  
  public UIAddNewTag() {
    addUIFormInput(new UIFormStringInput(FIELD_TAGNAME_INPUT, FIELD_TAGNAME_INPUT, null));
  }
  
  public String[] getActions() { return new String[] {"Save", "Cancel"} ; }
  
  public void activate() throws Exception {
    // TODO Auto-generated method stub
  }

  public void deActivate() throws Exception {
    // TODO Auto-generated method stub
  }
  
  static  public class SaveActionListener extends EventListener<UIAddNewTag> {
    public void execute(Event<UIAddNewTag> event) throws Exception {
      UIAddNewTag uiForm = event.getSource() ;
      String  tagName = uiForm.getUIStringInput(FIELD_TAGNAME_INPUT).getValue(); 
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      if (tagName == null || tagName.trim().length() == 0) {
        uiApp.addMessage(new ApplicationMessage("UIAddNewTag.msg.tagName-required", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ; 
      }
      UIContactPortlet uiContactPortlet = uiForm.getAncestorOfType(UIContactPortlet.class);
      UIContacts uiContacts = uiContactPortlet.findFirstComponentOfType(UIContacts.class);
      List<String> contactIds = uiContacts.getCheckedContacts();
      for (String contactId : contactIds) {
        System.out.println("contact Id selected : " + contactId + "\n\n");
      }
      Tag tag = new Tag() ;
      tag.setName(tagName) ;
      ContactService contactService = uiForm.getApplicationComponent(ContactService.class);
      String username = Util.getPortalRequestContext().getRemoteUser() ;
      contactService.addTag(username, contactIds, tag);
      
      UITags uiTags = uiContactPortlet.findFirstComponentOfType(UITags.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTags) ;
    
      uiContactPortlet.cancelAction() ;  
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContactPortlet) ;
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIAddNewTag> {
    public void execute(Event<UIAddNewTag> event) throws Exception {
      UIAddNewTag uiForm = event.getSource() ;
      UIContactPortlet contactPortlet = uiForm.getAncestorOfType(UIContactPortlet.class) ;
      contactPortlet.cancelAction() ; 
    }
  }
}
