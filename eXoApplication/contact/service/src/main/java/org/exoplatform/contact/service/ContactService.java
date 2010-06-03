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

import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserProfile;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public interface ContactService {
  
  /**
   * get all contacts in all personal address books that belong to current user
   * @param username current user ID
   * @return list of contacts of the user
   * @throws Exception
   */
  public List<Contact> getPersonalContacts(String username) throws Exception ;
  
  /**
   * get contact page list in a specific personal address book
   * @param username current user
   * @param addressBookId id of address book to get
   * @return a contact page list 
   * @throws Exception 
   */
  public ContactPageList getPersonalContactsByAddressBook(String username, String addressBookId) throws Exception ;
  
  /**
   * Get all email addresses of contacts in a personal address book
   * @return email list in this address book
   * @param sProvider session of current user
   * @param username current user
   * @param addressBookId id of address book
   * @throws Exception 
   */
  public List<String>  getEmailsByAddressBook(String username, String addressBookId) throws Exception ;
  
  /**
   * Get a personal contact for a given owner
   * @param owner user id of the contact owner
   * @param contactId id of contact to get
   * @return Contact in the personal contacts of owner 
   * @throws Exception 
   */
  public Contact getContact(String owner, String contactId) throws Exception ;
  
  /**
   * Save a contact to a personal address book
   * @param owner user ID of the owner of the contact
   * @param  contact contact to save
   * @param isNew is true if save a new contact and false if save an edited contact
   * @throws Exception 
   */
  public void saveContact(String owner, Contact contact, boolean isNew) throws Exception ;
  
  /**
   * Remove several personal contacts
   * @param owner user ID of the owner of the contacts
   * @param contactIds IDs of contacts will be removed
   * @return contacts list of deleted contacts
   * @throws Exception 
   */
  public List<Contact> removeContacts(String owner, List<String> contactIds) throws Exception ;
  
  /**
   * move contacts to another address books
   * @param username current user
   * @param contacts contacts will be moved
   * @param addressType type of address book which contacts will be moved to . type is personal or shared.
   * @param sProvider session of current user
   * @throws Exception 
   */
  public void moveContacts(String username, List<Contact> contacts, String addressType ) throws Exception ;
  
  /**
   * Get personal contacts by filter
   * @param username owner user ID
   * @param filter used to restrict results
   * @return List of contacts
   */
  public ContactPageList getPersonalContactsByFilter(String username, ContactFilter filter) throws Exception  ;
  
  /**
   * Get shared contacts by filter
   * @param username owner user ID
   * @param filter used to restrict results
   * @return List of contacts
   */
  public ContactPageList getSharedContactsByFilter(String username, ContactFilter filter) throws Exception ;
  
  /**
   * Get public contacts by filter
   * @param username owner user ID
   * @param filter used to restrict results
   * @return List of contacts
   */
  public ContactPageList getPublicContactsByFilter(String username, ContactFilter filter) throws Exception  ;

  /**
   * search all contacts with some properties equals ContactFilter properties.
   * @param username current user
   * @param filter setted some properties to filter contacts
   * @return a data page list contains list of contacts
   * @throws Exception 
   */
  public DataPageList searchContact(String username, ContactFilter filter) throws Exception ;

  /**
   * remove contact in a address book which was shared for current user
   * @param username current user
   * @param addressBookId id of address book shared, this address books contains contact will be removed.
   * @param contactId id of contact will be removed
   * @throws Exception 
   */
  public void removeSharedContact(String username, String addressBookId, String contactId) throws Exception ;
  
  /**
   * get id, name, email of contacts base on properties of ContactFilter
   * @param username current user 
   * @param filter set some properties to filter contacts.
   * @param sProvider should use system provider
   * @return a map contains id, name, email of contacts.
   * @throws Exception 
   */
  public Map<String, String> searchEmails(String username, ContactFilter filter)throws Exception ;

  public List<String> searchEmailsByFilter(String username, ContactFilter filter) throws Exception ;
  /**
   * get contact page list of public address book specific by id of address book
   * @param addressBookId id of address book want to get contacts
   * @return a ContactPageList contains contact nodes
   * @throws Exception 
   */
  public ContactPageList getPublicContactsByAddressBook(String addressBookId) throws Exception ;
  //public List<GroupContactData> getPublicContacts(SessionProvider sProvider, String[] groupIds) throws Exception ;
  
 // public List<String> getPublicAddressBookContacts(SessionProvider sProvider, String[] groupIds) throws Exception ;
  
  /**
   * Get the public contact for a given user Id
   * @param userId User ID whose corresponding contact will be retrieved
   * @return The self contact for the given user
   * @throws Exception 
   */
  public Contact getPublicContact(String userId) throws Exception ;  


  
  /**
   * Add a user's own contact to an address book
   * @param userId user ID for the user whose contact will be added to the address book
   * @param addressBookId id of the address book where the contact is added
   * @throws Exception 
   */
  public void addUserContactInAddressBook(String userId, String addressBookId) throws Exception ;

  /**
   * get all personal address books of user
   * @param username user name of user get groups
   * @return list all groups of user 
   * @throws Exception 
   */
  public List<AddressBook> getGroups(String username) throws Exception ;
  
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
   * @param username userId of user has contact
   * @param contactId id of contact shared
   * @param removedUser userId of user has permission view contact
   * @throws Exception 
   */
  public void removeUserShareContact(String username, String contactId, String removedUser) throws Exception ;

  /**
   * share some contacts to another users
   * @param username userId of current user
   * @param contactIds id of contacts will be shared
   * @throws Exception 
   */
  public void shareContact(String username, String[] contactIds, List<String> receiveUsers) throws Exception ;
  
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
   * Un-share an address book with another user
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
   * @param isNew true if save a new contact, false if save a edited contact  
   * @throws Exception 
   */
  public void saveContactToSharedAddressBook(String username, String addressBookId, Contact contact, boolean isNew) throws Exception ;

  /**
   * when a user has a shared address book from another user then shared user can view contacts in this shared address book
   * @param username userId of current user
   * @param addressBook shared address book that user wants to get contacts
   * @return a contact page list contains list of contacts in specific address book
   * @throws Exception 
   */
  public ContactPageList getSharedContactsByAddressBook(String username, SharedAddressBook addressBook) throws Exception ;

  /**
   * when a user is shared contact by another user then this user can get contact info base on id of shared contact
   * @param username userId of current user
   * @param contactId id of shared contact that user wants to get  
   * @return shared contact object 
   * @throws Exception 
   */
  public Contact getSharedContact(String username, String contactId) throws Exception ;
  
  /**
   * user can edit a shared contact and save to database
   * @param username userId of current user
   * @param contact shared contact will be saved
   * @throws Exception 
   */
  public void saveSharedContact(String username, Contact contact) throws Exception ;  
  
  
  
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
   * @param username userId of current user
   * @return a tags list contains all tags belong this user
   * @throws Exception 
   */
  public List<Tag> getTags(String username) throws Exception ;

  /**
   * user can get a specific tag base on id of tag
   * @param username userId of current user
   * @param tagId id of tag that user wants to get
   * @return tag object has id same input id 
   * @throws Exception 
   */
  public Tag getTag(String username, String tagId) throws Exception ;

  /**
   * this method will get contacts by a specific tag
   * @param username userId of current user
   * @param tagId is of tag that user wants to get contacts
   * @return DataPageList object contains contacts list
   * @throws Exception 
   */
  public DataPageList getContactPageListByTag(String username, String tagId) throws Exception ;

  /**
   * this method allow add some tags for some contacts
   * @param username userId of current user
   * @param contactIds id of contacts that user wants to add tags
   * @param tags tags that user wants to add to contacts 
   * @throws Exception 
   */
  public void addTag(String username, List<String> contactIds, List<Tag> tags) throws Exception ;

  /**
   * this method allow add an exist tag to some contacts
   * @param username userId of current user
   * @param contactIds is of contacts that user wants to add tag
   * @param tagId id of tag will be added to contacts
   * @throws Exception 
   */
  public void addTag(String username, List<String> contactIds, String tagId) throws Exception ;

  /**
   * user can remove an exist tag base on tagId
   * @param username userId of current user
   * @param tagId id of tag that user wants to remove
   * @return removed tag object  
   * @throws Exception 
   */
  public Tag removeTag(String username, String tagId) throws Exception ;

  /**
   * this method allow remove tags info in some contacts but don't remove tags
   * @param username userId of current user
   * @param contactIds id of contacts that user wants to remove tags information
   * @param tags list tags id will be removed in contacts
   * @throws Exception 
   */
  public void removeContactTag(String username, List<String> contactIds, List<String> tags) throws Exception ;
  
  /**
   * user can edit a tag and save to database 
   * @param username userId of current user
   * @param tag tag object will be saved
   * @throws Exception 
   */
  public void updateTag(String username, Tag tag) throws Exception ;
  
  /**
   * user can copy all contacts in a address book and paste them to another address book 
   * @param username userId of current user
   * @param srcAddress id of copied address book
   * @param srcType type of copied address book (it's private, shared, public)
   * @param destAddress id of  
   * @throws Exception 
   */
  public void pasteAddressBook(String username, String srcAddress, String srcType, String destAddress, String destType) throws Exception ;
  
  /**
   * this method allow add contacts list to a address book
   * @param username userId of current user
   * @param destAddress id of address book that user wants to add contacts
   * @param destType type of address book (it's private, shared, public)
   * @throws Exception 
   */

  public List<Contact> pasteContacts(String username, String destAddress, String destType, Map<String, String> contactsMap) throws Exception ;

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
  
  /**
   * save default address and default contact of new user
   * @param user oject user registed
   * @param isNew edit or save new user
   * @throws Exception 
   */
  public void registerNewUser(User user, boolean isNew) throws Exception ;
  
  /**
   * update profile of user to contact.
   * @param userProfile profile of user to update
   * @throws Exception 
   */
  public void updateProfile(UserProfile userProfile) throws Exception ;
  
  /**
   * save collected email addresses
   * @param emailAddress collected email
   * @throws Exception 
   */
  public void saveAddress(String username, String emailAddress) throws Exception ;
  
  /**
   * use this method to look all public AddressBookIds corresponding to groups in which the user has at least one membership.
   * @param user The username of the user
   * @return A collection of the found AddressBookIds. The return collection cannot be null, but it can be empty if no AddressBookId is found.
   */
  public List<String> getPublicAddressBookIdsOfUser(String user) throws Exception;
  
  /**
   * Use this method to get all public AddressBookIds corresponding to groups that the user has the permission to view (including groups
   * in which the user has at least one membership).
   * @param user The username of the user
   * @return
   */
  public List<String> getAllsPublicAddressBookIds(String user) throws Exception;
  /**
   * search all mail from a group of user 
   * @param username : given user id
   * @param groupId : given group id
   * @return list of mail address
   * @throws Exception
   */
  public List<String> getAllEmailByPublicGroup(String username, String groupId) throws Exception;
  /**
   * search all mail from shared group
   * @param username : given user id
   * @param addressBookId : given address book id
   * @return list of mail address
   * @throws Exception
   */
  public List<String> getAllEmailBySharedGroup(String username, String addressBookId) throws Exception;
  /**
   * Check username whether edit permission of contact
   * @param user name
   * @return contact
   */
  public boolean haveEditPermissionOnContact(String username, Contact contact) throws Exception;
  
}
