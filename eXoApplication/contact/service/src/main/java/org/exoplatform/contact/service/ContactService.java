/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.service;

import java.util.List;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public interface ContactService {
  
  public List<Contact> getAllContact(String username) throws Exception ;
  public List<Contact> getContactsByGroup(String username, String groupId) throws Exception ;
  public Contact getContact(String username, String contactId) throws Exception ;
  public void saveContact(String username, Contact contact, boolean isNew) throws Exception ;
  public Contact removeContact(String username, String contactId) throws Exception ;
  
  public List<GroupContactData> getSharedContacts(String[] groupIds) throws Exception ;
  public Contact getSharedContact(String contactId) throws Exception ;
  public void saveSharedContact(Contact contact, boolean isNew) throws Exception ;
  public Contact removeSharedContact(String contactId) throws Exception ;
  
  public List<ContactGroup> getGroups(String username) throws Exception ;
  public ContactGroup getGroup(String username, String groupId) throws Exception ;
  public void saveGroup(String username, ContactGroup group, boolean isNew) throws Exception ;
  public ContactGroup removeGroup(String username, String groupId) throws Exception ;
  
  public Contact shareContact(Contact contact, String[] groupIds) throws Exception ;
  public List<GroupContactData> getPublicContacts(String[] groupIds) throws Exception ;

}
