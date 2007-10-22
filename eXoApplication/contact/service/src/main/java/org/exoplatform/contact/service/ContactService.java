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
  public ContactPageList getContactPageListByGroup(String username, String groupId) throws Exception ;
  public ContactPageList getContactPageListByGroup(String username, String groupId, String viewQuery, String orderBy, boolean isAscending) throws Exception;
  public List<String>  getAllEmailAddressByGroup(String username, String groupId) throws Exception ;
  public Contact getContact(String username, String contactId) throws Exception ;
  public void saveContact(String username, Contact contact, boolean isNew) throws Exception ;
  public List<Contact> removeContacts(String username, List<String> contactIds) throws Exception ;
  public List<Contact> moveContacts(String username, List<String> contactIds, String[] groupId) throws Exception ;
  
  public ContactPageList getSharedContactsByGroup(String groupId) throws Exception ;
  public List<GroupContactData> getSharedContacts(String[] groupIds) throws Exception ;
  public List<String> getSharedGroupContacts(String[] groupIds) throws Exception ;
  public Contact getSharedContact(String contactId) throws Exception ;
  public void saveSharedContact(Contact contact, boolean isNew) throws Exception ;
  public Contact removeSharedContact(String contactId) throws Exception ;
  
  public List<ContactGroup> getGroups(String username) throws Exception ;
  public ContactGroup getGroup(String username, String groupId) throws Exception ;
  public void saveGroup(String username, ContactGroup group, boolean isNew) throws Exception ;
  public ContactGroup removeGroup(String username, String groupId) throws Exception ;
  
  public List<Contact> shareContacts(String username, List<String> contactIds, String[] groupIds) throws Exception;
  public List<GroupContactData> getPublicContacts(String[] groupIds) throws Exception ;
  
  public List<Tag> getTags(String username) throws Exception ;
  public Tag getTag(String username, String tagName) throws Exception ;
  public TagPageList getContactPageListByTag(String username, String tagName) throws Exception ;
  public ContactPageList getContactPageListByTag(String username, String tagName, String viewQuery, String orderBy, boolean isAscending) throws Exception ;
  public void addTag(String username, List<String> contactIds, List<Tag> tags) throws Exception ;
  public Tag removeTag(String username, String tagName) throws Exception ;
  public void removeContactTag(String username, List<String>contactIds, List<String> tags) throws Exception ;
  
  
  public ContactImportExport getContactImportExports(String type) ;
  public String[] getImportExportType() throws Exception ;

}
