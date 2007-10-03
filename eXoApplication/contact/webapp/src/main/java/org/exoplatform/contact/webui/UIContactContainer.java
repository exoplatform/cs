/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.webui.popup.UIPopupAction;
import org.exoplatform.contact.webui.popup.UISendEmail;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
    template =  "app:/templates/contact/webui/UIContactContainer.gtmpl",
    events = {
        @EventConfig(listeners = UIContactContainer.SendEmailActionListener.class)   
    }     
)
public class UIContactContainer extends UIContainer  {
  
  public UIContactContainer() throws Exception {
    addChild(UIContacts.class, null, null) ;
    addChild(UIContactPreview.class, null, null) ;    
  }
  
  public void setSeletedGroup(String group) throws Exception{
    ContactService contactService = ContactUtils.getContactService() ;
    getChild(UIContacts.class)
    .setContacts(contactService.getContactPageListByGroup(ContactUtils.getCurrentUser(), group)) ;
  }
  public static class SendEmailActionListener extends EventListener<UIContactContainer> {
    public void execute(Event<UIContactContainer> event) throws Exception {
      UIContactContainer uiContactContainer = event.getSource() ;  
      List<String> emails = new ArrayList<String>() ;
      String contactId = event.getRequestContext().getRequestParameter(OBJECTID);
      String username = ContactUtils.getCurrentUser() ;
      ContactService contactService = ContactUtils.getContactService() ;
      if (!ContactUtils.IsEmpty(contactId)) emails.add(contactService.getContact(username, contactId).getEmailAddress()) ;
      else {
        UIApplication uiApp = uiContactContainer.getAncestorOfType(UIApplication.class) ;
        List<String> contactIds = uiContactContainer.getChild(UIContacts.class).getCheckedContacts() ;
        if (contactIds.size() == 0) {
          uiApp.addMessage(new ApplicationMessage("UIContacts.msg.checkContact-required", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
        for (String id : contactIds) emails.add(contactService.getContact(username, id).getEmailAddress()) ;
      }
      UIContactPortlet uiContactPortlet = uiContactContainer.getAncestorOfType(UIContactPortlet.class) ;
      UIPopupAction uiPopupAction = uiContactPortlet.getChild(UIPopupAction.class) ;
      UISendEmail uiSendEmail = uiPopupAction.createUIComponent(UISendEmail.class, null, "UISendEmail") ;
      uiSendEmail.setEmails(emails) ;
      uiPopupAction.activate(uiSendEmail, 700, 0, true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      
    }
  }
  
}
