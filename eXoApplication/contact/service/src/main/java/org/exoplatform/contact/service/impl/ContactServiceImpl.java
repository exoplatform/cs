/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.service.impl;

import java.util.List;

import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.contact.service.ContactService;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public class ContactServiceImpl implements ContactService{
  
  public List<Contact> getPublicContact() throws Exception {
    return null ;
  }
  public List<Contact> getAllContact(String username) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
  public List<Contact> getContactsByGroup(String username, String groupId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
  public Contact getContact(String username, String groupId, String contactId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
  public void saveContact(String username, Contact contact, boolean isNew) throws Exception {
    // TODO Auto-generated method stub
    
  }
  public Contact removeContact(String username, String groupId, String contactId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
  
  
  public List<ContactGroup> getGroups(String username) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
  public ContactGroup getGroup(String username, String groupId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
  public void saveGroup(String username, ContactGroup group, boolean isNew) throws Exception {
    // TODO Auto-generated method stub
    
  }
  public ContactGroup removeGroup(String username, String groupId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
  

  
}
