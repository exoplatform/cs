/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.service.impl;

import java.util.List;

import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.contact.service.ContactPageList;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.GroupContactData;
import org.exoplatform.contact.service.Tag;
import org.exoplatform.contact.service.TagPageList;
import org.exoplatform.registry.JCRRegistryService;
import org.exoplatform.services.jcr.RepositoryService;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public class ContactServiceImpl implements ContactService {
  private JCRDataStorage storage_ ;
  
  public ContactServiceImpl(RepositoryService  repositoryService, 
      JCRRegistryService jcrRegistryService) throws Exception {
      storage_ = new JCRDataStorage(repositoryService, jcrRegistryService) ;
  }
  
  public List<Contact> getAllContact(String username) throws Exception {
    return storage_.getAllContact(username);
  }
  
  public ContactPageList getContactPageListByGroup(String username, String groupId) throws Exception {
    return storage_.getContactPageListByGroup(username, groupId);
  }
  public List<String> getAllEmailAddressByGroup(String username, String groupId) throws Exception {
    return storage_.getAllEmailAddressByGroup(username, groupId);
  }
  public Contact getContact(String username, String contactId) throws Exception {
    return storage_.getContact(username, contactId);
  }
  
  public void saveContact(String username, Contact contact, boolean isNew) throws Exception {
    storage_.saveContact(username, contact, isNew);    
  }
  
  public List<Contact> removeContacts(String username, List<String> contactIds) throws Exception {
    return storage_.removeContacts(username, contactIds);
  }
  
  public List<Contact> moveContacts(String username, List<String> contactIds, String[] groupId) throws Exception {
    return storage_.moveContacts(username, contactIds, groupId) ;
  }
   
  public List<ContactGroup> getGroups(String username) throws Exception {
    return storage_.getGroups(username);
  }
  
  public ContactGroup getGroup(String username, String groupId) throws Exception {
    return storage_.getGroup(username, groupId);
  }
  
  public void saveGroup(String username, ContactGroup group, boolean isNew) throws Exception {
    storage_.saveGroup(username, group, isNew);    
  }
  
  public ContactGroup removeGroup(String username, String groupId) throws Exception {
    return storage_.removeGroup(username, groupId);
  }

  public List<GroupContactData> getPublicContacts(String[] groupIds) throws Exception {
    return storage_.getPublicContacts(groupIds);
  }

  public List<Contact> shareContacts(String username, List<String> contactIds, String[] groupIds) throws Exception {
    return storage_.shareContacts(username, contactIds, groupIds) ;
  }

  public Contact getSharedContact(String contactId) throws Exception {
    return storage_.getSharedContact(contactId);
  }

  public List<GroupContactData> getSharedContacts(String[] groupIds) throws Exception {
    return storage_.getSharedContacts(groupIds);
  }
  
  public List<String> getSharedGroupContacts(String[] groupIds) throws Exception{
    return storage_.getSharedGroupContacts(groupIds);
  }
  public Contact removeSharedContact(String contactId) throws Exception {
    return storage_.removeSharedContact(contactId);
  }

  public void saveSharedContact(Contact contact, boolean isNew) throws Exception {
    storage_.saveSharedContact(contact, isNew);
  } 
  
  public Tag getTag(String username, String tagName) throws Exception {
    return storage_.getTag(username, tagName) ;
  }
  
  public List<Tag> getTags(String username) throws Exception {
    return storage_.getTags(username);
  }
  public TagPageList getContactPageListByTag(String username, String tagName) throws Exception {
    return storage_.getContactPageListByTag(username, tagName);
  }
  
  public void addTag(String username, List<String> contactIds, List<Tag> tags) throws Exception {
    storage_.addTag(username, contactIds, tags);
  }
  
  public Tag removeTag(String username, String tagName) throws Exception {
    return storage_.removeTag(username, tagName);
  }
  
  public void removeContactTag(String username, List<String>contactIds, List<String> tags) throws Exception {
    storage_.removeContactTag(username, contactIds, tags) ;
  }
  
  public ContactPageList getSharedContactsByGroup(String groupId) throws Exception {
    return storage_.getSharedContactsByGroup(groupId) ;
  }
  
}
