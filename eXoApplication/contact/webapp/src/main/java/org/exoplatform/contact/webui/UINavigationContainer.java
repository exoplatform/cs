/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui;

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
    template =  "app:/templates/contact/webui/UINavigationContainer.gtmpl"
)
public class UINavigationContainer extends UIContainer  {
  public UINavigationContainer() throws Exception {
    UIAddressBooks uiAddressBook = addChild(UIAddressBooks.class, null, null) ;
    addChild(UITags.class, null, null) ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    ContactService contactService = getApplicationComponent(ContactService.class) ;
    if(contactService.getGroups(username) != null &&contactService.getGroups(username).size() > 0) {
      String groupId = contactService.getGroups(username).get(0).getId() ;
      uiAddressBook.setSelectedGroup(groupId) ;
    }
  }  
}
