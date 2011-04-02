/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

import org.exoplatform.contact.service.impl.AddressBookNotFoundException;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.organization.User;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jan 19, 2010  
 */
public interface DataStorage {

  final public static String USERS_PATH = "usersPath".intern();

  final public static String PERSONAL   = "0".intern();

  final public static String SHARED     = "1".intern();

  final public static String PUBLIC     = "2".intern();

  final public static String SPLIT      = "::".intern();

  final public static String HYPHEN     = "shared_".intern();

  /**
   * Get the Contact application user data storage root
   * @param username
   * @return Node object
   * @throws Exception
   */
  public Node getContactUserDataHome(String username) throws Exception;

  /**
   * Get the public Contact application storage
   * @return JCR Node
   * @throws Exception
   */
  public Node getContactApplicationDataHome() throws Exception;

  /**
   * Get the home node of user contacts under user contact application storage
   * @param username
   * @return JCR Node
   * @throws Exception
   */
  public Node getPersonalContactsHome(String username) throws Exception;

  /**
   * Get the home node of user address books under user contact application storage
   * @param username
   * @return JCR Node
   * @throws Exception
   */
  public Node getPersonalAddressBooksHome(String username) throws Exception;

  /**
   * Get the home node of public contacts under public Contact application storage  
   * @return JCR Node
   * @throws Exception
   */
  public Node getPublicContactsHome() throws Exception;

  /**
   * Get the home node of user tags under user contact application storage
   * @param username
   * @return JCR Node
   * @throws Exception
   */
  public Node getTagsHome(String username) throws Exception;

  /**
   * get the node that holds references to all address books shared to a given user.
   * @param userId
   * @return
   * @throws Exception
   */
  public Node getSharedAddressBooksHome(String userId) throws Exception;

  /**
   * Get the home node of shared contacts under shared folder of user
   * @param userId
   * @return JCR Node
   * @throws Exception
   */
  public Node getSharedContact(String userId) throws Exception;

  /**
   * Load the public contact for a given username
   * @param userId username for which the corresponding contact will be loaded
   * @return the contact or null if the contact could not be loaded
   * @throws Exception
   */
  public Contact loadPublicContactByUser(String userId) throws Exception;

  /**
   * Get all contacts of a user
   * @param username
   * @return list of Contact objects
   * @throws Exception
   */
  public List<Contact> findAllContactsByOwner(String username) throws Exception;

  /**
   * Get all contacts by a filter
   * @param username
   * @param filter
   * @param type
   * @return ContactPageList object
   * @throws Exception
   */
  public ContactPageList findContactsByFilter(String username, ContactFilter filter, String type) throws Exception;

  /**
   * Get a personal contact from owner and contact id
   * @param ownerUserId the owner of contact
   * @param contactId the contact id
   * @return Contact object
   * @throws Exception
   */
  public Contact loadPersonalContact(String ownerUserId, String contactId) throws Exception;

  /**
   * @param owner
   * @param addressBookId
   * @return ContactPageList object
   * @throws Exception
   */
  public ContactPageList findPersonalContactsByAddressBook(String owner, String addressBookId) throws Exception;

  /**
   * Get all emails from address book
   * @param username
   * @param addressBookId
   * @return list of email addresses
   * @throws AddressBookNotFoundException
   * @throws Exception
   */
  public List<String> findEmailsByAddressBook(String username, String addressBookId) throws AddressBookNotFoundException, Exception;

  /**
   * Get type of the address book
   * @param username
   * @param addressBookId
   * @return AddressBookType object
   * @throws Exception
   */
  public AddressBookType getAddressBookType(String username, String addressBookId) throws Exception;

  /**
   * Get address book by id
   * @param username
   * @param addressBookId
   * @return AddressBook object
   */
  public AddressBook findPersonalAddressBookById(String username, String addressBookId);

  /**
   * Get all emails from personal address book
   * @param username
   * @param addressBookId
   * @return
   * @throws Exception
   */
  public List<String> findEmailsInPersonalAddressBook(String username, String addressBookId) throws Exception;

  /**
   * Get all emails from public address book of group
   * @param username
   * @param groupId
   * @return
   * @throws Exception
   */
  public List<String> findEmailsInPublicAddressBook(String username, String groupId) throws Exception;

  /**
   * @param username
   * @param addressBookId
   * @return
   * @throws Exception
   */
  public List<String> getAllEmailBySharedGroup(String username, String addressBookId) throws Exception;

  /**
   * Get address book from contact group node
   * @param contactGroupNode
   * @return AddressBook object
   * @throws Exception
   */
  public AddressBook toAddressBook(Node contactGroupNode) throws Exception;

  /**
   * Get personal address book with group id
   * @param username
   * @param groupId
   * @return AddressBook object
   * @throws Exception
   */
  public AddressBook loadPersonalAddressBook(String username, String groupId) throws Exception;

  public AddressBook loadPublicAddressBook(String username, String groupId) throws Exception;

  /**
   * Get a shared address book by id
   * @param username the username
   * @param addressBookId the address book id
   * @return AddressBook object
   * @throws Exception
   */
  public AddressBook getSharedAddressBookById(String username, String addressBookId) throws Exception;

  /**
   * Get all address book of a user
   * @param username
   * @return list of AddressBook objects
   * @throws Exception
   */
  public List<AddressBook> findPersonalAddressBooksByOwner(String username) throws Exception;

  /**
   * Remove contacts from user's contact
   * @param username
   * @param contactIds
   * @return the list of Contact objects removed
   * @throws Exception
   */
  public List<Contact> removeContacts(String username, List<String> contactIds) throws Exception;

  /**
   * Move user's contacts to another type
   * @param username
   * @param contacts
   * @param addressType
   * @throws Exception
   */
  public void moveContacts(String username, List<Contact> contacts, String addressType) throws Exception;

  /**
   * Get personal contacts by group
   * @param username
   * @param groupId
   * @return List of contact id
   * @throws Exception
   */
  public List<String> getUserContactNodesByGroup(String username, String groupId) throws Exception;

  /**
   * Remove a personal addressBook. Does not clean the contacts it contains
   * @param username
   * @param addressBookId
   * @return AddressBook object which is removed
   * @throws Exception
   */
  public AddressBook removePersonalAddressBook(String username, String addressBookId) throws Exception;

  /**
   * Remove all contacts contained in an address book
   * @param username
   * @param addressBookId
   * @throws Exception
   */
  public void clearAddressBook(String username, String addressBookId) throws Exception;

  /**
   * Save a personal contact to JCR data storage
   * @param username
   * @param contact
   * @param isNew
   * @throws Exception
   */
  public void saveContact(String username, Contact contact, boolean isNew) throws Exception;

  /**
   * 
   * @param username
   * @param addressbook
   * @param isNew
   * @throws Exception
   */
  public void savePersonalOrSharedAddressBook(String username, AddressBook addressbook, boolean isNew) throws Exception;

  /**
   * @param username
   * @param contactId
   * @param removedUser
   * @throws Exception
   */
  public void removeUserShareContact(String username, String contactId, String removedUser) throws Exception;

  /**
   * @param username
   * @param addressBookId
   * @param removedUser
   * @throws Exception
   */
  public void unshareAddressBook(String username, String addressBookId, String removedUser) throws Exception;

  /**
   * Share user's address book to list of users
   * @param username
   * @param addressBookId
   * @param receiveUsers
   * @throws Exception
   */
  public void shareAddressBook(String username, String addressBookId, List<String> receiveUsers) throws Exception;

  /**
   * Share a list of contacts to list of users
   * @param username
   * @param contactIds
   * @param receiveUsers
   * @throws Exception
   */
  public void shareContact(String username, String[] contactIds, List<String> receiveUsers) throws Exception;

  /**
   * Get all shared address books that shared to user 
   * @param username the username
   * @return List of SharedAddressBook object
   * @throws Exception
   */
  public List<SharedAddressBook> findSharedAddressBooksByUser(String username) throws Exception;

  /**
   * @param username
   * @param addressBookId
   * @param contactId
   * @throws Exception
   */
  public void removeSharedContact(String username, String addressBookId, String contactId) throws Exception;

  /**
   * @param username
   * @param contactId
   * @return
   * @throws Exception
   */
  public Contact getSharedContact(String username, String contactId) throws Exception;

  /**
   * @param username
   * @return
   * @throws Exception
   */
  public DataPageList getSharedContacts(String username) throws Exception;

  /**
   * @param username
   * @param contact
   * @throws Exception
   */
  public void saveSharedContact(String username, Contact contact) throws Exception;

  /**
   * @param username
   * @param addressBookId
   * @param contact
   * @param isNew
   * @throws Exception
   */
  public void saveContactToSharedAddressBook(String username, String addressBookId, Contact contact, boolean isNew) throws Exception;

  /**
   * @param username
   * @param contactId
   * @return
   * @throws Exception
   */
  public Contact getSharedContactAddressBook(String username, String contactId) throws Exception;

  /**
   * @param username
   * @param addressBook
   * @return
   * @throws Exception
   */
  public ContactPageList getSharedContactsByAddressBook(String username, SharedAddressBook addressBook) throws Exception;

  /**
   * @param groupId
   * @return
   * @throws Exception
   */
  public ContactPageList getPublicContactsByAddressBook(String groupId) throws Exception;

  /**
   * @param groupIds
   * @return
   * @throws Exception
   */
  public List<GroupContactData> getPublicContacts(String[] groupIds) throws Exception;

  /**
   * @param groupIds
   * @return
   * @throws Exception
   */
  public List<String> getPublicAddressBookContacts(String[] groupIds) throws Exception;

  /**
   * @param groupId
   * @return
   * @throws Exception
   */
  public boolean hasContacts(String groupId) throws Exception;

  /**
   * @param userId
   * @param addressBookId
   * @throws Exception
   */
  public void addUserContactInAddressBook(String userId, String addressBookId) throws Exception;

  /**
   * @param contactsHome
   * @param contact
   * @param isNew
   * @throws Exception
   */
  public void contactToNode(Node contactsHome, Contact contact, boolean isNew) throws Exception;

  /**
   * @param tagNode
   * @return
   * @throws Exception
   */
  public Tag getTag(Node tagNode) throws Exception;

  /**
   * @param username
   * @param tag
   * @throws Exception
   */
  public void updateTag(String username, Tag tag) throws Exception;

  /**
   * @param username
   * @param tagId
   * @return
   * @throws Exception
   */
  public Tag getTag(String username, String tagId) throws Exception;

  /**
   * @param username
   * @return
   * @throws Exception
   */
  public List<Tag> getTags(String username) throws Exception;

  /**
   * @param username
   * @param tagId
   * @return
   * @throws Exception
   */
  public DataPageList getContactPageListByTag(String username, String tagId) throws Exception;

  /**
   * @param username
   * @param contactIds
   * @param tagId
   * @throws Exception
   */
  public void addTag(String username, List<String> contactIds, String tagId) throws Exception;

  /**
   * @param username
   * @param contactIds
   * @param tags
   * @throws Exception
   */
  public void addTag(String username, List<String> contactIds, List<Tag> tags) throws Exception;

  /**
   * @param it
   * @param tagId
   * @throws Exception
   */
  public void removeTagInContacts(NodeIterator it, String tagId) throws Exception;

  /**
   * @param username
   * @param tagId
   * @return
   * @throws Exception
   */
  public Tag removeTag(String username, String tagId) throws Exception;

  /**
   * @param username
   * @param contactIds
   * @param tags
   * @throws Exception
   */
  public void removeContactTag(String username, List<String> contactIds, List<String> tags) throws Exception;

  /**
   * @param username
   * @param filter
   * @return
   * @throws Exception
   */
  public DataPageList searchContact(String username, ContactFilter filter) throws Exception;

  /**
   * @param username
   * @param filter
   * @return
   * @throws Exception
   */
  public Map<String, String> findEmailsByFilter(String username, ContactFilter filter) throws Exception;

  /**
   * @param username
   * @param filter
   * @return
   * @throws Exception
   */
  public List<String> searchEmailsByFilter(String username, ContactFilter filter) throws Exception;

  /**
   * @param emails
   * @param contactNode
   * @throws Exception
   */
  public void feedEmailResult(Map<String, String> emails, Node contactNode) throws Exception;

  /**
   * @param username
   * @param srcHomeNode
   * @param iter
   * @param destAddress
   * @param destType
   * @throws Exception
   */
  public void copyNodes(String username, Node srcHomeNode, NodeIterator iter, String destAddress, String destType) throws Exception;

  /**
   * @param username
   * @param srcAddress
   * @param srcType
   * @param destAddress
   * @param destType
   * @throws Exception
   */
  public void pasteAddressBook(String username, String srcAddress, String srcType, String destAddress, String destType) throws Exception;

  /**
   * @param username
   * @param destAddress
   * @param destType
   * @param contactsMap
   * @return
   * @throws Exception
   */
  public List<Contact> pasteContacts(String username, String destAddress, String destType, Map<String, String> contactsMap) throws Exception;

  /**
   * @param contactHomeNode
   * @param contact
   * @param destAddress
   * @param destType
   * @return
   * @throws Exception
   */
  public Node saveCopyContact(Node contactHomeNode, Contact contact, String destAddress, String destType) throws Exception;

  /**
   * @param user
   * @param isNew
   * @throws Exception
   */
  public void registerNewUser(User user, boolean isNew) throws Exception;

  /**
   * Create a session provider for current context. The method first try to get a normal session provider, 
   * then attempts to create a system provider if the first one was not available.
   * @return a SessionProvider initialized by current SessionProviderService
   * @see SessionProviderService#getSessionProvider(null)
   */
  public SessionProvider createSessionProvider();

  /**
   * @return SessionProvider
   */
  public SessionProvider createSystemProvider();

  /**
   * Safely closes JCR session provider. Call this method in finally to clean any provider initialized by createSessionProvider()
   * @param sessionProvider the sessionProvider to close
   * @see SessionProvider#close();
   */
  public void closeSessionProvider(SessionProvider sessionProvider);

  /**
   * @param username
   * @param emailAddress
   * @throws Exception
   */
  public void saveAddress(String username, String emailAddress) throws Exception;

  /**
   * Get JCR Node object by absolute path
   * @param nodePath
   * @param sessionProvider
   * @return JCR Node object
   * @throws Exception
   */
  public Node getNodeByPath(String nodePath, SessionProvider sessionProvider) throws Exception;

  /**
   * This method checks if the user has edit permission on a address book
   * @param username
   * @param addressBookId
   * @return true if user has edit permission, otherwise false
   * @throws Exception
   */
  public boolean haveEditPermissionOnAddressBook(String username, String addressBookId) throws Exception;

  /**
   * This method checks if the user has edit permission on a contact
   * @param username
   * @param contact
   * @return true if user has edit permission, otherwise false
   * @throws Exception
   */
  public boolean haveEditPermissionOnContact(String username, Contact contact) throws Exception;

  /**
   * @param sprovider
   * @return
   * @throws Exception
   */
  public Session getSession(SessionProvider sprovider) throws Exception;

  public enum AddressBookType {
    Personal, Shared, Public
  };

  /**
   * Get home node of share data under user Contact application storage
   * @param user
   * @return
   * @throws Exception
   */
  public Node getSharedContactsHome(String user) throws Exception;

  public List<String> getPublicAddresses(String username) throws Exception;

  public void savePublicAddressBook(AddressBook addressbook, boolean isNew) throws Exception;
}
