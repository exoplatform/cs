/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui;

import java.util.List;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.contact.service.ContactService;
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
    String username = ContactUtils.getCurrentUser() ;
    ContactService contactService = ContactUtils.getContactService() ;
    List<ContactGroup> groups = contactService.getGroups(username) ;
    String selectedGroup = null ;
    if(groups != null && groups.size() > 0) selectedGroup = groups.get(0).getId() ;
    uiContacts.setContacts(contactService.getContactsByGroup(username, selectedGroup)) ;
    if(uiContacts.getContacts() != null && uiContacts.getContacts().length > 0) 
      uiContactPreview.setContact(uiContacts.getContacts()[0]) ;
  }
  
}
