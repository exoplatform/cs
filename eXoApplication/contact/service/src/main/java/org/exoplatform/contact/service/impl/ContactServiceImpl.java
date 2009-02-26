/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.contact.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactFilter;
import org.exoplatform.contact.service.AddressBook;
import org.exoplatform.contact.service.ContactImportExport;
import org.exoplatform.contact.service.ContactPageList;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.GroupContactData;
import org.exoplatform.contact.service.SharedAddressBook;
import org.exoplatform.contact.service.Tag;
import org.exoplatform.contact.service.DataPageList;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public class ContactServiceImpl implements ContactService {
  
  final private static String VCARD = "x-vcard".intern() ;
  
  private JCRDataStorage storage_ ;
  private Map<String, ContactImportExport> contactImportExport_ = new HashMap<String, ContactImportExport>() ;
  
  public ContactServiceImpl(NodeHierarchyCreator nodeHierarchyCreator) throws Exception {
      storage_ = new JCRDataStorage(nodeHierarchyCreator) ;
      
      contactImportExport_.put(VCARD, new VCardImportExport(storage_)) ;
  }
  
  /**
   * {@inheritDoc}
   */
  public List<Contact> getPersonalContacts(String username) throws Exception {
    return storage_.findAllContactsByOwner(username);
  }

  
  public Contact getPersonalContact(String userId) throws Exception {
    return storage_.getPersonalContact(userId) ;
  }
  
  public Map<String, String> searchEmails(SessionProvider sysProvider, String username, ContactFilter filter)throws Exception {
    return storage_.searchEmails(sysProvider, username, filter) ;
  }
  
  /*
  public ContactPageList getContactPageListByTag(String username, ContactFilter filter) throws Exception {
    return storage_.getContactPageListByTag(username, filter);
  }*/
  
  /**
   * {@inheritDoc}
   */
  public ContactPageList getContactsByAddressBook(String username, String groupId) throws Exception {
    return storage_.getContactPageListByGroup(username, groupId);
  }
  

  public ContactPageList getContactPageListByGroup(SessionProvider sProvider, String username, ContactFilter filter, String type) throws Exception {
    return storage_.getContactPageListByGroup(sProvider, username, filter, type) ;
  }
  
  /**
   * {@inheritDoc}
   */
  public List<String> getEmailsByAddressBook(String username, String groupId) throws Exception {
    return storage_.getAllEmailAddressByGroup(username, groupId);
  }
  

  
  public Contact getContact(String username, String contactId) throws Exception {
    return storage_.getContact(username, contactId);
  }
  
  /**
   * {@inheritDoc}
   */
  public void saveContact(String username, Contact contact, boolean isNew) throws Exception {
    storage_.saveContact(username, contact, isNew);    
  }
  
  public List<Contact> removeContacts(SessionProvider sProvider, String username, List<String> contactIds) throws Exception {
    return storage_.removeContacts(sProvider, username, contactIds);
  }
  
  public void moveContacts(SessionProvider sysProvider, String username, List<Contact> contacts, String addressType ) throws Exception {
    storage_.moveContacts(sysProvider, username, contacts, addressType) ;
  }
  
  public void addGroupToPersonalContact(String userId, String groupId) throws Exception {
  	storage_.addGroupToPersonalContact(userId, groupId) ;
  }
  
  public List<AddressBook> getGroups(SessionProvider sProvider, String username) throws Exception {
    return storage_.getGroups(sProvider, username);
  }
  
  /**
   * {@inheritDoc}
   */
  public AddressBook getPersonalAddressBook(String username, String addressBookId) throws Exception {
    return storage_.loadPersonalAddressBook(username, addressBookId);
  }
  

  
  /**
   * {@inheritDoc}
   */  
  public void saveAddressBook(String username, AddressBook group, boolean isNew) throws Exception {
    storage_.savePersonalOrSharedAddressBook(username, group, isNew);    
  } 
  
  /**
   * {@inheritDoc}
   */  
  public AddressBook removeAddressBook(String username, String addressBookId) throws Exception {
    // step 1 : remove content
    storage_.clearAddressBook(username, addressBookId);
    
    // step 2 : remove address book
    AddressBook removed =  storage_.removePersonalAddressBook(username, addressBookId);
    
    return removed;
  }
  
  
  public void removeUserShareContact(SessionProvider sProvider, String username, String contactId, String removedUser) throws Exception {
    storage_.removeUserShareContact(sProvider, username, contactId, removedUser) ;
  }
  
  public void shareContact(SessionProvider sProvider, String username, String[] contactIds, List<String> receiveUsers) throws Exception {
  	storage_.shareContact(sProvider, username, contactIds, receiveUsers) ;
  }
  public DataPageList getSharedContacts(String username) throws Exception {
  	return storage_.getSharedContacts(username) ;
  }
  public void shareAddressBook(String username, String addressBookId, List<String> receiverUsers) throws Exception {
  	storage_.shareAddressBook(username, addressBookId, receiverUsers) ;
  }
  
  /**
   * {@inheritDoc}
   */
  public void unshareAddressBook(String owner,
                                 String addressBookId,
                                 String unsharedUser) throws Exception {
    storage_.unshareAddressBook(owner, addressBookId, unsharedUser) ;
  }

  /**
   * {@inheritDoc}
   */
  public List<SharedAddressBook> getSharedAddressBooks(String username) throws Exception {
    return storage_.findSharedAddressBooksByUser(username) ;
  }
  
  public ContactPageList getSharedContactsByAddressBook(SessionProvider sProvider, String username, SharedAddressBook addressBook) throws Exception {
  	return storage_.getSharedContactsByAddressBook(sProvider, username, addressBook) ;
  }
  /*public void removeSharedAddressBook(SessionProvider sProvider, String username, String addressBookId) throws Exception {
  	storage_.removeSharedAddressBook(sProvider, username, addressBookId) ;
  }*/
  
  public void removeSharedContact(SessionProvider sProvider, String username, String addressBookId, String contactId) throws Exception { 
    storage_.removeSharedContact(sProvider, username, addressBookId, contactId) ;
  }
  
  public void saveContactToSharedAddressBook(String username, String addressBookId, Contact contact, boolean isNew) throws Exception {
  	storage_.saveContactToSharedAddressBook(username, addressBookId, contact, isNew) ;
  }
  
  public Contact getSharedContactAddressBook(String username, String contactId) throws Exception {
    return storage_.getSharedContactAddressBook(username, contactId) ;
  }
  
  public void saveSharedContact(String username, Contact contact) throws Exception  {
    storage_.saveSharedContact(username, contact) ;
  }
  
  public Contact getSharedContact(SessionProvider sProvider, String username, String contactId) throws Exception {
    return storage_.getSharedContact(sProvider, username, contactId) ;
  }
  
  public Contact getPublicContact(String contactId) throws Exception {
    return storage_.getPublicContact(contactId);
  }

  public List<GroupContactData> getPublicContacts(SessionProvider sProvider, String[] groupIds) throws Exception {
    return storage_.getPublicContacts(sProvider, groupIds);
  }

  public Tag getTag(SessionProvider sProvider, String username, String tagName) throws Exception {
    return storage_.getTag(sProvider, username, tagName) ;
  }
  
  public List<Tag> getTags(SessionProvider sProvider, String username) throws Exception {
    return storage_.getTags(sProvider, username);
  }
  public DataPageList getContactPageListByTag(SessionProvider sProvider, String username, String tagName) throws Exception {
    return storage_.getContactPageListByTag(sProvider, username, tagName);
  }
  
  public void addTag(SessionProvider sProvider, String username, List<String> contactIds, List<Tag> tags) throws Exception {
    storage_.addTag(sProvider, username, contactIds, tags);
  }
  
  public void addTag(SessionProvider sProvider, String username, List<String> contactIds, String tagId) throws Exception {
	  storage_.addTag(sProvider, username, contactIds, tagId);
  }
  
  public Tag removeTag(SessionProvider sProvider, String username, String tagName) throws Exception {
    return storage_.removeTag(sProvider, username, tagName);
  }
  
  public void updateTag(SessionProvider sProvider, String username,Tag tag) throws Exception {
    storage_.updateTag(sProvider, username, tag) ;
  }
  
  public void removeContactTag(SessionProvider sProvider, String username, List<String> contactIds, List<String> tags) throws Exception {
    storage_.removeContactTag(sProvider, username, contactIds, tags) ;
  }
  
  public ContactPageList getPublicContactsByAddressBook(SessionProvider sProvider, String groupId) throws Exception {
    return storage_.getPublicContactsByAddressBook(sProvider, groupId) ;
  }
  
  public void pasteAddressBook(SessionProvider sProvider, String username, String srcAddress, String srcType, String destAddress, String destType) throws Exception {
  	storage_.pasteAddressBook(sProvider, username, srcAddress, srcType, destAddress, destType) ;
  }
  
  public List<Contact> pasteContacts(SessionProvider sProvider, String username, String destAddress, String destType, Map<String, String> contactsMap) throws Exception {
    return storage_.pasteContacts(sProvider, username, destAddress, destType, contactsMap) ;
  }
  
  public ContactImportExport getContactImportExports(String type) {
    return contactImportExport_.get(type) ;
  }
  
  public String[] getImportExportType() throws Exception {
    return contactImportExport_.keySet().toArray(new String[]{}) ;
  }

  public DataPageList searchContact(SessionProvider sProvider, String username, ContactFilter filter) throws Exception {
    return storage_.searchContact(sProvider, username, filter) ;
  }
  
  /**
   * {@inheritDoc}
   */
  public AddressBook getSharedAddressBook(String username, String addressBookId) throws Exception {
    return storage_.getSharedAddressBookById(username, addressBookId) ;
  }
  
  /**
   * {@inheritDoc}
   */
  public List<String> getAllEmailBySharedGroup(String username, String addressBookId) throws Exception {
    return storage_.getAllEmailBySharedGroup(username, addressBookId) ;
  }
  
  /**
   * {@inheritDoc}
   */
  public List<String> getAllEmailByPublicGroup(String username, String groupId) throws Exception { 
    return storage_.getAllEmailByPublicGroup(username, groupId) ;
  }

  

   
  
  ////// LEGACY API //////
  
  /**
   * {@inheritDoc}
   */
  public ContactPageList getContactPageListByGroup(SessionProvider sProvider, String username, String groupId) throws Exception {
    return getContactsByAddressBook(username, groupId);
  }
  
  /**
   * {@inheritDoc}
   */
  public void saveGroup(SessionProvider sProvider, String username, AddressBook group, boolean isNew) throws Exception {
    saveAddressBook(username,group, isNew);    
  }
  
  /**
   * {@inheritDoc}
   */
  public AddressBook getGroup(SessionProvider sProvider, String username, String groupId) throws Exception {
    return getPersonalAddressBook(username, groupId);
  }
  
  /**
   * {@inheritDoc}
   */
  public AddressBook removeGroup(SessionProvider sProvider, String username, String groupId) throws Exception {
    return removeAddressBook(username, groupId);
  }
    
  /**
   * {@inheritDoc}
   */
  public AddressBook getSharedGroup(SessionProvider sProvider, String username, String groupId) throws Exception {
    return getSharedAddressBook(username, groupId);
  }  
  
  
  /**
   * {@inheritDoc}
   */
  public List<SharedAddressBook> getSharedAddressBooks(SessionProvider sProvider, String username) throws Exception  {
   return getSharedAddressBooks(username);
  }  
  
  /**
   * {@inheritDoc}
   */
  public void removeUserShareAddressBook(SessionProvider sProvider, String username, String addressBookId, String removedUser) throws Exception {
    unshareAddressBook(username, addressBookId,removedUser);
  }  
    
  /**
   * {@inheritDoc}
   */
  public void saveContact(SessionProvider sProvider, String username, Contact contact, boolean isNew) throws Exception {
    storage_.saveContact(username, contact, isNew);    
  } 
  
  /**
   * {@inheritDoc}
   */
  public List<Contact> getAllContacts(SessionProvider sProvider, String username) throws Exception {
    return getPersonalContacts(username);
  }

  /**
   * {@inheritDoc}
   */
  public List<String> getAllEmailAddressByGroup(SessionProvider sProvider, String username, String groupId) throws Exception {
    return getEmailsByAddressBook(username, groupId);
  } 
  
}
