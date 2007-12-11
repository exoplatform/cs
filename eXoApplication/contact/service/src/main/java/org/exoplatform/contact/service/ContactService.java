/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.service;

import java.util.List;

import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public interface ContactService {
  
  public List<Contact> getAllContact(SessionProvider sProvider, String username) throws Exception ;
  public ContactPageList getContactPageListByGroup(SessionProvider sProvider, String username, String groupId) throws Exception ;
  public List<String>  getAllEmailAddressByGroup(SessionProvider sProvider, String username, String groupId) throws Exception ;
  public Contact getContact(SessionProvider sProvider, String username, String contactId) throws Exception ;
  public void saveContact(SessionProvider sProvider, String username, Contact contact, boolean isNew) throws Exception ;
  public List<Contact> removeContacts(SessionProvider sProvider, String username, List<String> contactIds) throws Exception ;
  public List<Contact> moveContacts(SessionProvider sProvider, String username, List<String> contactIds, String[] groupId, boolean toPublic) throws Exception ;
  //public ContactPageList getContactPageListByTag(SessionProvider sProvider, String username, ContactFilter filter) throws Exception ;
  public ContactPageList getContactPageListByGroup(SessionProvider sProvider, String username, ContactFilter filter, boolean isPublic) throws Exception;
  public DataPageList searchContact(SessionProvider sProvider, String username, ContactFilter filter) throws Exception ;
  
  public ContactPageList getSharedContactsByGroup(SessionProvider sProvider, String groupId) throws Exception ;
  public List<GroupContactData> getSharedContacts(SessionProvider sProvider, String[] groupIds) throws Exception ;
  public List<String> getSharedGroupContacts(SessionProvider sProvider, String[] groupIds) throws Exception ;
  public Contact getSharedContact(SessionProvider sProvider, String contactId) throws Exception ;
  public void saveSharedContact(SessionProvider sProvider, Contact contact, boolean isNew) throws Exception ;
  public Contact removeSharedContact(SessionProvider sProvider, String contactId) throws Exception ;
  
  public List<ContactGroup> getGroups(SessionProvider sProvider, String username) throws Exception ;
  public ContactGroup getGroup(SessionProvider sProvider, String username, String groupId) throws Exception ;
  public void saveGroup(SessionProvider sProvider, String username, ContactGroup group, boolean isNew) throws Exception ;
  public ContactGroup removeGroup(SessionProvider sProvider, String username, String groupId) throws Exception ;
  
  public List<Contact> shareContacts(SessionProvider sProvider, String username, List<String> contactIds, String[] groupIds) throws Exception;
  public List<GroupContactData> getPublicContacts(SessionProvider sProvider, String[] groupIds) throws Exception ;
  
  public List<Tag> getTags(SessionProvider sProvider, String username) throws Exception ;
  public Tag getTag(SessionProvider sProvider, String username, String tagId) throws Exception ;
  public DataPageList getContactPageListByTag(SessionProvider sProvider, String username, String tagId) throws Exception ;
  public void addTag(SessionProvider sProvider, String username, List<String> contactIds, List<Tag> tags) throws Exception ;
  public void addTag(SessionProvider sProvider, String username, List<String> contactIds, String tagId) throws Exception ;
  public Tag removeTag(SessionProvider sProvider, String username, String tagName) throws Exception ;
  public void removeContactTag(SessionProvider sProvider, String username, List<String>contactIds, List<String> tags) throws Exception ;
  public void updateTag(SessionProvider sProvider, String username,Tag tag) throws Exception ;
  
  public ContactImportExport getContactImportExports(String type) ;
  public String[] getImportExportType() throws Exception ;
  
}
