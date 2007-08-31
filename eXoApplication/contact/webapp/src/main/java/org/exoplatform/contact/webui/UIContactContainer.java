/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui;

import java.util.List;

import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
    template =  "app:/templates/contact/webui/UIContactContainer.gtmpl"
)
public class UIContactContainer extends UIContainer  {
  
  public UIContactContainer() throws Exception {
    UIContacts uiContacts = addChild(UIContacts.class, null, null) ;
    UIContactPreview uiContactPreview = addChild(UIContactPreview.class, null, null) ;
    
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    ContactService contactSvr = getApplicationComponent(ContactService.class) ;
    if(contactSvr.getGroups(username).size() > 0) {
      String groupId = contactSvr.getGroups(username).get(0).getId() ;
      List<Contact> contacts = contactSvr.getContactsByGroup(username, groupId) ;
      uiContacts.setContacts(contacts) ;
      if(contacts.size() > 0) {
        uiContactPreview.setContact(contacts.get(contacts.size() - 1)) ;
      }
    }
  }
}
