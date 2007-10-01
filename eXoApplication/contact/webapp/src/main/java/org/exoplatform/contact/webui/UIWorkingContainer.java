/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    template =  "app:/templates/contact/webui/UIWorkingContainer.gtmpl"
)
public class UIWorkingContainer extends UIContainer  {
  private Map<String, ContactGroup> groupMap_ = new HashMap<String, ContactGroup> () ;
  private String selectedGroup_ = null ;
  
  public UIWorkingContainer() throws Exception {
    String username = ContactUtils.getCurrentUser() ;
    ContactService contactService = ContactUtils.getContactService() ;
    List<ContactGroup> groups = contactService.getGroups(username) ;
    if(groups != null && groups.size() > 0) selectedGroup_ = groups.get(0).getId() ;
    for (ContactGroup group : groups) {
      groupMap_.put(group.getId(), group) ; 
    }
    addChild(UINavigationContainer.class, null, null) ;
    addChild(UIContactContainer.class, null, null) ;
  } 
  
  public void updateContactGroup(ContactGroup contactGroup) {
    groupMap_.put(contactGroup.getId(), contactGroup) ;
  }
  
  public ContactGroup[] getContactGroups() throws Exception {
    return groupMap_.values().toArray(new ContactGroup[]{}) ;
  }
  public void removeContactGroup(String id) { 
    groupMap_.remove(id) ; 
    setSelectedGroup(null) ;
  }
  
  public void setSelectedGroup(String groupId) { selectedGroup_ = groupId ; }
  public String getSelectedGroup() { return selectedGroup_ ; }
  
}
