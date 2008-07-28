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
package org.exoplatform.contact.service;

import java.util.List;
import java.util.Map;

import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public interface ContactService {
  
  /**
   * get all contacts in all private address books belong current user
   * @return list contacts belong current user
   * @param sProvider session of current user
   * @param username current user
   * @exception 
   */
  public List<Contact> getAllContact(SessionProvider sProvider, String username) throws Exception ;
  
  /**
   * get contact page list in a specific personal addressbook
   * @return a contact page list contains contact nodes 
   * @param sProvider session of current user
   * @param username current user
   * @param groupId id of address book getted
   * @exception 
   */
  public ContactPageList getContactPageListByGroup(SessionProvider sProvider, String username, String groupId) throws Exception ;
  
  /**
   * get all email addresses in a specific personal addressbook
   * @return emails list in this address book
   * @param sProvider session of current user
   * @param username current user
   * @param groupId id of address book
   * @exception 
   */
  public List<String>  getAllEmailAddressByGroup(SessionProvider sProvider, String username, String groupId) throws Exception ;
  
  /**
   * get personal contact base on id of contact
   * @return object Contact has specific id   
   * @param sProvider session of current user
   * @param username current user
   * @param contactId id of contact is getted
   * @exception 
   */
  public Contact getContact(SessionProvider sProvider, String username, String contactId) throws Exception ;
  
  /**
   * save a contact to personal address book
   * @param sProvider session of current user
   * @param username current user
   * @param  contact contact is saved
   * @param isNew is true if save a new contact and false if save an edited contact
   * @exception 
   */
  public void saveContact(SessionProvider sProvider, String username, Contact contact, boolean isNew) throws Exception ;
  
  /**
   * remove some personal contacts belong current user
   * @return contacts list were deleted
   * @param sProvider session of current user
   * @param username current user
   * @param contactIds id of contacts will be removed
   * @exception 
   */
  public List<Contact> removeContacts(SessionProvider sProvider, String username, List<String> contactIds) throws Exception ;
  
  /**
   * move contacts to another address books
   * @param sProvider session of current user
   * @param username current user
   * @param contacts contacts will be moved
   * @param addressType type of address book which contacts will be moved to . type is personal or shared.
   * @exception 
   */
  public void moveContacts(SessionProvider sysProvider, String username, List<Contact> contacts, String addressType ) throws Exception ;
  //public ContactPageList getContactPageListByTag(SessionProvider sProvider, String username, ContactFilter filter) throws Exception ;
  
  /**
   * get contact page list base on id of address book was setted into a ContactFilter and type of this address book
   * @return a contact page list in address book was setted into ContactFilter 
   * @param sProvider session of current user
   * @param username current user
   * @param filter setted some properties like : address book, sorted by, is Ascending...
   * @param type type of address book : private or shared or public 
   * @exception 
   */
  public ContactPageList getContactPageListByGroup(SessionProvider sProvider, String username, ContactFilter filter, String type) throws Exception;
  
  /**
   * search all contacts with some properties equals ContactFilter properties.
   * @return a data page list contains list of contacts
   * @param sProvider session of current user
   * @param username current user
   * @param filter setted some properties to filter contacts
   * @exception 
   */
  public DataPageList searchContact(SessionProvider sProvider, String username, ContactFilter filter) throws Exception ;
  
  /**
   * remove contact in a address book which was shared for current user
   * @param sProvider session of current user
   * @param username current user
   * @param addressBookId id of address book shared, this address books contains contact will be removed.
   * @param contactId id of contact will be removed
   * @exception 
   */
  public void removeSharedContact(SessionProvider sProvider, String username, String addressBookId, String contactId) throws Exception ;
  
  /**
   * get id, name, email of contacts base on properties of ContactFilter
   * @return a map contains id, name, email of contacts.
   * @param sProvider session of current user
   * @param username current user 
   * @param filter setted some properties to filter contacts.
   * @exception 
   */
  public Map<String, String> searchEmails(SessionProvider sysProvider, String username, ContactFilter filter)throws Exception ;
  
  /**
   * get contact page list of public address book specific by id of address book
   * @return a ContactPageList contains contact nodes
   * @param sProvider use system provider to perform this action
   * @param addressBookId id of address book want to get contacts
   * @exception 
   */
  public ContactPageList getPublicContactsByAddressBook(SessionProvider sProvider, String addressBookId) throws Exception ;
  //public List<GroupContactData> getPublicContacts(SessionProvider sProvider, String[] groupIds) throws Exception ;
  
 // public List<String> getPublicAddressBookContacts(SessionProvider sProvider, String[] groupIds) throws Exception ;
  
  /**
   * get public contact base on id of contact
   * @return object contact has id equals input id
   * @param contactId id of contact will be getted
   * @exception 
   */
  public Contact getPublicContact(String contactId) throws Exception ;  

  /**
   * get all email addresses in a specific public addressbook
   * @return emails list in this address book
   * @param sProvider use system provider to perform this action
   * @param username current user
   * @param groupId id of address book
   * @exception 
   */
  public List<String> getAllEmailByPublicGroup(String username, String groupId) throws Exception ;
  
  /**
   * the default contact (owner contact) is added a new address book
   * @param userId username of user 
   * @param groupId id of group will be added to contact
   * @exception 
   */
  public void addGroupToPersonalContact(String userId, String groupId) throws Exception ;
  
  public Contact getPersonalContact(String userId) throws Exception ;
  
  /**
   * get all personal address books of user
   * @return list all groups of user 
   * @param sProvider session of current user
   * @param username username of user get groups
   * @exception 
   */
  public List<ContactGroup> getGroups(SessionProvider sProvider, String username) throws Exception ;
  
  /**
   * get personal address book of user base on id of address book
   * @return object ContactGroup has id equals input id
   * @param sProvider session provider of user
   * @param username userId of a user
   * @param groupId id of group will be getted
   * @exception 
   */
  public ContactGroup getGroup(SessionProvider sProvider, String username, String groupId) throws Exception ;
  
  /**
   * save a personal address book of specific user
   * @param sProvider session of user
   * @param username userId of user
   * @param isNew true if save a new address book and false if save a editted  address book
   * @exception 
   */
  public void saveGroup(SessionProvider sProvider, String username, ContactGroup group, boolean isNew) throws Exception ;
  
  /**
   * remove a personal address book of specific user
   * @return removed ContactGroup object 
   * @param sProvider session of user
   * @param username userId of user
   * @exception 
   */
  public ContactGroup removeGroup(SessionProvider sProvider, String username, String groupId) throws Exception ;
  
  
  /**
   * remove view permission of user shared contact
   * @param sProvider session of current user
   * @param username userId of user has contact
   * @param removedUser userId of user has permission view contact
   * @param contactId id of contact shared
   * @exception 
   */
  public void removeUserShareContact(SessionProvider sProvider, String username, String contactId, String removedUser) throws Exception ;
  
  /**
   * share some contacts to another users
   * @param sProvider session of current user
   * @param username userId of current user
   * @param contactIds id of contacts will be shared
   * @exception 
   */
  public void shareContact(SessionProvider sProvider, String username, String[] contactIds, List<String> receiveUsers) throws Exception ;
  
  /**
   * get all contacts shared by any user for current user
   * @return DataPageList object contains contacts list
   * @param username userId of current user
   * @exception 
   */
  public DataPageList getSharedContacts(String username) throws Exception ;
  
  /**
   * shared a personal address book of current user to  another users
   * @param sProvider session of current user
   * @param username userId of user has address book to share
   * @param receiverUsers userIds of users will be shared
   * @param addressBookId id of address books will be shared
   * @exception 
   */
  public void shareAddressBook(SessionProvider sProvider, String username, String addressBookId, List<String> receiverUsers) throws Exception ;
  
  /**
   * remove view permission of users are shared address book
   * @param sProvider session of current user
   * @param username userId has shared address book
   * @param removedUser userId of shared user 
   * @param addressBookId id of shared address book 
   * @exception
   */
  public void removeUserShareAddressBook(SessionProvider sProvider, String username, String addressBookId, String removedUser) throws Exception ;
  
  /**
   * get a shared address book specific by id of address
   * @param 
   * @exception 
   */
  public ContactGroup getSharedGroup(String username, String groupId) throws Exception ;
  
  /**
   * get all address books were shared by another users
   * @return SharedAddressBook list contains some information about address books like name, shared user
   * @param sProvider session of current user
   * @param username userId of current user
   * @exception 
   */
  public List<SharedAddressBook> getSharedAddressBooks(SessionProvider sProvider, String username) throws Exception ;
  
 // public void removeSharedAddressBook(SessionProvider sProvider, String username, String addressBookId) throws Exception ;
  
  /**
   * if user has edit permission on a shared address book then this user can save a contact to this address book
   * @param username userId of current user
   * @param addressBookId id of address book user wants to save contact to
   * @param contact saved contact to shared address book
   * @param isNew true if save a new contact, false if save a editted contact  
   * @exception 
   */
  public void saveContactToSharedAddressBook(String username, String addressBookId, Contact contact, boolean isNew) throws Exception ;
  
  /**
   * set Node Hierarchy Creator
   * @param 
   * @exception 
   */
  public ContactPageList getSharedContactsByAddressBook(SessionProvider sProvider, String username, SharedAddressBook addressBook) throws Exception ;
  
  /**
   * set Node Hierarchy Creator
   * @param 
   * @exception 
   */
  public Contact getSharedContact(SessionProvider sProvider, String username, String contactId) throws Exception ;
  
  /**
   * set Node Hierarchy Creator
   * @param 
   * @exception 
   */
  public void saveSharedContact(String username, Contact contact) throws Exception ;  
  
  /**
   * set Node Hierarchy Creator
   * @param 
   * @exception 
   */
  public List<String> getAllEmailBySharedGroup(String username, String addressBookId) throws Exception ;
  
  /**
   * set Node Hierarchy Creator
   * @param 
   * @exception 
   */
  public Contact getSharedContactAddressBook(String username, String contactId) throws Exception ;
  
  /**
   * set Node Hierarchy Creator
   * @param 
   * @exception 
   */
  public List<Tag> getTags(SessionProvider sProvider, String username) throws Exception ;
  
  /**
   * set Node Hierarchy Creator
   * @param 
   * @exception 
   */
  public Tag getTag(SessionProvider sProvider, String username, String tagId) throws Exception ;
  
  /**
   * set Node Hierarchy Creator
   * @param 
   * @exception 
   */
  public DataPageList getContactPageListByTag(SessionProvider sProvider, String username, String tagId) throws Exception ;
  
  /**
   * set Node Hierarchy Creator
   * @param 
   * @exception 
   */
  public void addTag(SessionProvider sProvider, String username, List<String> contactIds, List<Tag> tags) throws Exception ;
  
  /**
   * set Node Hierarchy Creator
   * @param 
   * @exception 
   */
  public void addTag(SessionProvider sProvider, String username, List<String> contactIds, String tagId) throws Exception ;
  
  /**
   * set Node Hierarchy Creator
   * @param 
   * @exception 
   */
  public Tag removeTag(SessionProvider sProvider, String username, String tagName) throws Exception ;
  
  /**
   * set Node Hierarchy Creator
   * @param 
   * @exception 
   */
  public void removeContactTag(SessionProvider sProvider, String username, List<String> contactIds, List<String> tags) throws Exception ;
  
  /**
   * set Node Hierarchy Creator
   * @param 
   * @exception 
   */
  public void updateTag(SessionProvider sProvider, String username,Tag tag) throws Exception ;
  
  
  /**
   * set Node Hierarchy Creator
   * @param 
   * @exception 
   */
  public void pasteAddressBook(SessionProvider sProvider, String username, String srcAddress, String srcType, String destAddress, String destType) throws Exception ;
  
  /**
   * set Node Hierarchy Creator
   * @param 
   * @exception 
   */
  public void pasteContacts(SessionProvider sProvider, String username, String destAddress, String destType, List<Contact> contacts) throws Exception ;
  
  /**
   * set Node Hierarchy Creator
   * @param 
   * @exception 
   */
  public ContactImportExport getContactImportExports(String type) ;
  
  /**
   * set Node Hierarchy Creator
   * @param 
   * @exception 
   */
  public String[] getImportExportType() throws Exception ;
  
}
