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
public interface ContactService extends ContactServiceLegacy {
  
  /**
   * get all contacts in all personal address books that belong to current user
   * @param username current user ID
   * @return list of contacts of the user
   * @throws Exception
   */
  public List<Contact> getPersonalContacts(String username) throws Exception ;
  
  /**
   * get contact page list in a specific personal addressbook
   * @param username current user
   * @param addressBookId id of address book getted
   * @return a contact page list 
   * @throws Exception 
   */
  public ContactPageList getContactsByAddressBook(String username, String addressBookId) throws Exception ;
  
  /**
   * get all email addresses in a specific personal addressbook
   * @return emails list in this address book
   * @param sProvider session of current user
   * @param username current user
   * @param groupId id of address book
   * @throws Exception 
   */
  public List<String>  getAllEmailAddressByGroup(SessionProvider sProvider, String username, String groupId) throws Exception ;
  
  /**
   * get personal contact base on id of contact
   * @param username current user
   * @param contactId id of contact is getted
   * @return object Contact has specific id   
   * @throws Exception 
   */
  public Contact getContact(String username, String contactId) throws Exception ;
  
  /**
   * Save a contact to a personal address book
   * @param username current user
   * @param  contact contact to save
   * @param isNew is true if save a new contact and false if save an edited contact
   * @throws Exception 
   */
  public void saveContact(String username, Contact contact, boolean isNew) throws Exception ;
  
  /**
   * remove some personal contacts belong current user
   * @return contacts list were deleted
   * @param sProvider session of current user
   * @param username current user
   * @param contactIds id of contacts will be removed
   * @throws Exception 
   */
  public List<Contact> removeContacts(SessionProvider sProvider, String username, List<String> contactIds) throws Exception ;
  
  /**
   * move contacts to another address books
   * @param sProvider session of current user
   * @param username current user
   * @param contacts contacts will be moved
   * @param addressType type of address book which contacts will be moved to . type is personal or shared.
   * @throws Exception 
   */
  public void moveContacts(SessionProvider sysProvider, String username, List<Contact> contacts, String addressType ) throws Exception ;
  //public ContactPageList getContactPageListByTag(SessionProvider sProvider, String username, ContactFilter filter) throws Exception ;
  
  /**
   * get contact page list base on id of address book was setted into a ContactFilter and type of this address book
   * @return a contact page list in address book was setted into ContactFilter 
   * @param sProvider should use system provider
   * @param username current user
   * @param filter setted some properties like : address book, sorted by, is Ascending...
   * @param type type of address book : private or shared or public 
   * @throws Exception 
   */
  public ContactPageList getContactPageListByGroup(SessionProvider sProvider, String username, ContactFilter filter, String type) throws Exception;
  
  /**
   * search all contacts with some properties equals ContactFilter properties.
   * @return a data page list contains list of contacts
   * @param sProvider session of current user
   * @param username current user
   * @param filter setted some properties to filter contacts
   * @throws Exception 
   */
  public DataPageList searchContact(SessionProvider sProvider, String username, ContactFilter filter) throws Exception ;
  
  /**
   * remove contact in a address book which was shared for current user
   * @param sProvider should use system provider
   * @param username current user
   * @param addressBookId id of address book shared, this address books contains contact will be removed.
   * @param contactId id of contact will be removed
   * @throws Exception 
   */
  public void removeSharedContact(SessionProvider sProvider, String username, String addressBookId, String contactId) throws Exception ;
  
  /**
   * get id, name, email of contacts base on properties of ContactFilter
   * @return a map contains id, name, email of contacts.
   * @param sProvider should use system provider
   * @param username current user 
   * @param filter setted some properties to filter contacts.
   * @throws Exception 
   */
  public Map<String, String> searchEmails(SessionProvider sysProvider, String username, ContactFilter filter)throws Exception ;
  
  /**
   * get contact page list of public address book specific by id of address book
   * @return a ContactPageList contains contact nodes
   * @param sProvider use system provider to perform this action
   * @param addressBookId id of address book want to get contacts
   * @throws Exception 
   */
  public ContactPageList getPublicContactsByAddressBook(SessionProvider sProvider, String addressBookId) throws Exception ;
  //public List<GroupContactData> getPublicContacts(SessionProvider sProvider, String[] groupIds) throws Exception ;
  
 // public List<String> getPublicAddressBookContacts(SessionProvider sProvider, String[] groupIds) throws Exception ;
  
  /**
   * get public contact base on id of contact
   * @return object contact has id equals input id
   * @param contactId id of contact will be getted
   * @throws Exception 
   */
  public Contact getPublicContact(String contactId) throws Exception ;  

  /**
   * get all email addresses in a specific public addressbook
   * @return emails list in this address book
   * @param sProvider use system provider to perform this action
   * @param username current user
   * @param groupId id of address book
   * @throws Exception 
   */
  public List<String> getAllEmailByPublicGroup(String username, String groupId) throws Exception ;
  
  /**
   * the default contact (owner contact) is added a new address book
   * @param userId username of user 
   * @param groupId id of group will be added to contact
   * @throws Exception 
   */
  public void addGroupToPersonalContact(String userId, String groupId) throws Exception ;
  
  /**
   * get contact information of current user : by default, when init new user a default contact was 
   * being inited, this contact contains info of new user 
   * @return default contact object
   * @param userId userId of user want to get default Contact
   * @throws Exception
   */
  public Contact getPersonalContact(String userId) throws Exception ;
  
  /**
   * get all personal address books of user
   * @return list all groups of user 
   * @param sProvider session of current user
   * @param username username of user get groups
   * @throws Exception 
   */
  public List<AddressBook> getGroups(SessionProvider sProvider, String username) throws Exception ;
  
  /**
   * Get a personal address book by ID
   * @param owner user ID of the owning user
   * @param addressBookID ID of the address book to retrieve.
   * @return object ContactGroup for the given ID
   * @throws Exception 
   */
  public AddressBook getPersonalAddressBook(String owner, String addressBookID) throws Exception ;
  

  /**
   * Save a personal or shared address book.
   * @param username user ID of the owner
   * @param addressBook the address book to save
   * @param isNew true if save a new address book and false to update an existing address book
   * @throws Exception 
   */
  public void saveAddressBook(String username, AddressBook addressBook, boolean isNew) throws Exception ;
  

    
  
  /**
   * Remove a personal address book
   * @param username user ID of address book owner
   * @param addressBookId ID of the address book to remove
   * @return the removed address book
   * @throws Exception 
   */
  public AddressBook removeAddressBook(String username, String addressBookId) throws Exception ;
  

  
  /**
   * remove view permission of user shared contact
   * @param sProvider should use system provider
   * @param username userId of user has contact
   * @param removedUser userId of user has permission view contact
   * @param contactId id of contact shared
   * @throws Exception 
   */
  public void removeUserShareContact(SessionProvider sProvider, String username, String contactId, String removedUser) throws Exception ;
  
  /**
   * share some contacts to another users
   * @param sProvider session of current user
   * @param username userId of current user
   * @param contactIds id of contacts will be shared
   * @throws Exception 
   */
  public void shareContact(SessionProvider sProvider, String username, String[] contactIds, List<String> receiveUsers) throws Exception ;
  
  /**
   * get all contacts shared by any user for current user
   * @return DataPageList object contains contacts list
   * @param username userId of current user
   * @throws Exception 
   */
  public DataPageList getSharedContacts(String username) throws Exception ;
  
  /**
   * Share a personal address book with other users.
   * @param username user ID of the owner of the address book to share
   * @param addressBookId ID of the address book to share
   * @param receiverUsers user IDs of users that will receive the shared address book
   * @throws Exception 
   */
  public void shareAddressBook(String username, String addressBookId, List<String> receiverUsers) throws Exception ;
  

  /**
   * Unshare an address book with another user
   * @param owner user ID of the address book owner
   * @param addressBookId id of shared address book 
   * @param unsharedUser user ID of the user that looses sharing to this address book
   * @throws Exception
   */
  public void unshareAddressBook(String owner, String addressBookId, String unsharedUser) throws Exception ;
  
  /**
   * Get a shared address book by ID for a given user
   * @return a shared address book object
   * @param username user ID to whom the address book is shared
   * @param addressBookId ID of address group to retrieve for user
   * @throws Exception 
   */
  public AddressBook getSharedAddressBook(String username, String addressBookId) throws Exception ;
  

  /**
   * Get address books that are shared (by others) to a given user
   * @param username userId of current user
   * @return SharedAddressBook list contains some information about address books like name, shared user
   * @throws Exception 
   */
  public List<SharedAddressBook> getSharedAddressBooks(String username) throws Exception ;
  

  /**
   * if user has edit permission on a shared address book then this user can save a contact to this address book
   * @param username userId of current user
   * @param addressBookId id of address book user wants to save contact to
   * @param contact saved contact to shared address book
   * @param isNew true if save a new contact, false if save a editted contact  
   * @throws Exception 
   */
  public void saveContactToSharedAddressBook(String username, String addressBookId, Contact contact, boolean isNew) throws Exception ;
  
  /**
   * when a user has a shared address book from another user then shared user can view contacts in this shared address book
   * @return a contact page list contains list of contacts in specific address book
   * @param sProvider should use system provider
   * @param username userId of current user
   * @param addressBook shared address book that user wants to get contacts
   * @throws Exception 
   */
  public ContactPageList getSharedContactsByAddressBook(SessionProvider sProvider, String username, SharedAddressBook addressBook) throws Exception ;
  
  /**
   * when a user is shared contact by another user then this user can get contact info base on id of shared contact
   * @return shared contact object 
   * @param sProvider should use system provider
   * @param username userId of current user
   * @param contactId id of shared contact that user wants to get  
   * @throws Exception 
   */
  public Contact getSharedContact(SessionProvider sProvider, String username, String contactId) throws Exception ;
  
  /**
   * user can edit a shared contact and save to database
   * @param username userId of current user
   * @param contact shared contact will be saved
   * @throws Exception 
   */
  public void saveSharedContact(String username, Contact contact) throws Exception ;  
  
  /**
   * user can get all emails of all contacts in a shared address book
   * @return list emails in this address book
   * @param username userId of current user
   * @param addressBookId id of address book user wants to get emails
   * @throws Exception 
   */
  public List<String> getAllEmailBySharedGroup(String username, String addressBookId) throws Exception ;
  
  /**
   * user can get a contact in a shared address book by id of contact
   * @return contact object in a shared address book
   * @param username userId of current user
   * @param contactId id of contact that user wants to get 
   * @throws Exception 
   */
  public Contact getSharedContactAddressBook(String username, String contactId) throws Exception ;
  
  /**
   * this method will get all tags that user has been added
   * @return a tags list contains all tags belong this user
   * @param sProvider session of current user
   * @param username userId of current user
   * @throws Exception 
   */
  public List<Tag> getTags(SessionProvider sProvider, String username) throws Exception ;
  
  /**
   * user can get a specific tag base on id of tag
   * @return tag object has id same input id 
   * @param sProvider session of current user
   * @param username userId of current user
   * @param tagId id of tag that user wants to get
   * @throws Exception 
   */
  public Tag getTag(SessionProvider sProvider, String username, String tagId) throws Exception ;
  
  /**
   * this method will get contacts by a specific tag
   * @return DataPageList object contains contacts list
   * @param sProvider session of current user
   * @param username userId of current user
   * @param tagId is of tag that user wants to get contacts
   * @throws Exception 
   */
  public DataPageList getContactPageListByTag(SessionProvider sProvider, String username, String tagId) throws Exception ;
  
  /**
   * this method allow add some tags for some contacts
   * @param sProvider session of current user
   * @param username userId of current user
   * @param contactIds id of contacts that user wants to add tags
   * @param tags tags that user wants to add to contacts 
   * @throws Exception 
   */
  public void addTag(SessionProvider sProvider, String username, List<String> contactIds, List<Tag> tags) throws Exception ;
  
  /**
   * this method allow add an exist tag to some contacts
   * @param sProvider session of current user
   * @param username userId of current user
   * @param contactIds is of contacts that user wants to add tag
   * @param tagId id of tag will be added to contacts
   * @throws Exception 
   */
  public void addTag(SessionProvider sProvider, String username, List<String> contactIds, String tagId) throws Exception ;
  
  /**
   * user can remove an exist tag base on tagId
   * @return removed tag object  
   * @param sProvider session of current user
   * @param username userId of current user
   * @param tagId id of tag that user wants to remove
   * @throws Exception 
   */
  public Tag removeTag(SessionProvider sProvider, String username, String tagId) throws Exception ;
  
  /**
   * this method allow remove tags info in some contacts but don't remove tags
   * @param sProvider session of current user
   * @param username userId of current user
   * @param contactIds id of contacts that user wants to remove tags information
   * @param tags list tags id will be removed in contacts
   * @throws Exception 
   */
  public void removeContactTag(SessionProvider sProvider, String username, List<String> contactIds, List<String> tags) throws Exception ;
  
  /**
   * user can edit a tag and save to database 
   * @param sProvider session of current user
   * @param username userId of current user
   * @param tag tag object will be saved
   * @throws Exception 
   */
  public void updateTag(SessionProvider sProvider, String username,Tag tag) throws Exception ;
  
  
  /**
   * user can copy all contacts in a address book and paste them to another address book 
   * @param sProvider session of current user
   * @param username userId of current user
   * @param srcAddress id of copied address book
   * @param srcType type of copied address book (it's private, shared, public)
   * @param destAddress id of  
   * @throws Exception 
   */
  public void pasteAddressBook(SessionProvider sProvider, String username, String srcAddress, String srcType, String destAddress, String destType) throws Exception ;
  
  /**
   * this method allow add contacts list to a address book
   * @param sProvider session of current user
   * @param username userId of current user
   * @param destAddress id of address book that user wants to add contacts
   * @param destType type of address book (it's private, shared, public)
   * @throws Exception 
   */

  public List<Contact> pasteContacts(SessionProvider sProvider, String username, String destAddress, String destType, Map<String, String> contactsMap) throws Exception ;

  /**
   * this method allow get a object used to import, export contacts
   * @return a ContactImportExport object
   * @param type type of import, export format , for example : x-vcard
   * @throws Exception 
   */
  public ContactImportExport getContactImportExports(String type) ;
  
  /**
   * get type of import, export formats, for example : x-vcard
   * @return an array of import, export type
   * @throws Exception 
   */
  public String[] getImportExportType() throws Exception ;
  
}
