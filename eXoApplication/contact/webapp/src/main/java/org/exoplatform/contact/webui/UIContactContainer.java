/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.contact.webui;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.SessionsUtils;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
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
    .setContacts(contactService.getContactPageListByGroup(SessionsUtils.getSessionProvider(), ContactUtils.getCurrentUser(), group)) ;
  }
  public static class SendEmailActionListener extends EventListener<UIContactContainer> {
    public void execute(Event<UIContactContainer> event) throws Exception {
      /*UIContactContainer uiContactContainer = event.getSource() ;  
      List<String> emails = new ArrayList<String>() ;
      String contactId = event.getRequestContext().getRequestParameter(OBJECTID);
      String username = ContactUtils.getCurrentUser() ;
      ContactService contactService = ContactUtils.getContactService() ;
      if (!ContactUtils.isEmpty(contactId)) emails.add(contactService.getContact(username, contactId).getEmailAddress()) ;
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
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;*/
      
    }
  }
  
}
