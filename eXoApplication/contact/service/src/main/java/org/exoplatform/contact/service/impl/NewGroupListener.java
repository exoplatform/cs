/***************************************************************************
 * Copyright 2001-2008 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.service.impl;

import org.exoplatform.contact.service.AddressBook;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.Utils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupEventListener;

public class NewGroupListener extends GroupEventListener {

  protected ContactService contactService_;

  /**
   * 
   * @param calendarService Calendar service geeting from the Portlet Container
   * @param params  parameters defined in the cs-plugins-configuration.xml
   */
  @SuppressWarnings("unchecked")
  public NewGroupListener(ContactService contactService, InitParams params) {
    contactService_ = contactService;
  }

  public void postSave(Group group, boolean isNew) throws Exception {
    String groupId = group.getId();
    AddressBook addressbook = new AddressBook();
    addressbook.setId(groupId);
    addressbook.setName(group.getGroupName());
    addressbook.setViewPermissionGroups(new String[] { groupId, groupId + Utils.COLON + Utils.MEMBERSHIP + Utils.MANAGER });
    addressbook.setEditPermissionGroups(new String[] { groupId + Utils.COLON + Utils.MEMBERSHIP + Utils.MANAGER });
    contactService_.savePublicAddressBook(addressbook, isNew);
  }

  @Override
  public void postDelete(Group group) throws Exception {

  }
}
