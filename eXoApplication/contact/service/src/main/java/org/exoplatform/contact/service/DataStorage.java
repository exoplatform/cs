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
import javax.jcr.Value;

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

  public Node getContactUserDataHome(String username) throws Exception;
  
  public Node getContactApplicationDataHome() throws Exception;
  
  public Node getPersonalContactsHome(String username) throws Exception;
  public Node getPersonalAddressBooksHome(String username) throws Exception;
  public Node getPublicContactsHome() throws Exception;
  public Node getTagsHome(String username) throws Exception;
  
  /**
   * get the node that holds references to all address books shared to a given user.
   * @param userId
   * @return
   * @throws Exception
   */
  public Node getSharedAddressBooksHome(String userId) throws Exception;

  
  
  public Node getSharedContact(String userId) throws Exception;


  public String[] ValuesToStrings(Value[] Val) throws Exception;

  /**
   * Load the public contact for a given username
   * @param userId username for which the corresponding contact will be loaded
   * @return the contact or null if the contact could not be loaded
   * @throws Exception
   */
  public Contact loadPublicContactByUser(String userId) throws Exception;

  public Contact getContact(Node contactNode, String contactType) throws Exception;

  public List<Contact> findAllContactsByOwner(String username) throws Exception;

  public ContactPageList findContactsByFilter(String username, ContactFilter filter, String type) throws Exception;

  public Contact loadPersonalContact(String ownerUserId, String contactId) throws Exception;

  public ContactPageList findPersonalContactsByAddressBook(String owner, String addressBookId) throws Exception;

  public List<String> findEmailsByAddressBook(String username, String addressBookId) throws AddressBookNotFoundException,
                                                                                    Exception;

  public AddressBookType getAddressBookType(String username, String addressBookId) throws Exception;

  public AddressBook findPersonalAddressBookById(String username, String addressBookId);

  public List<String> findEmailsInPersonalAddressBook(String username, String addressBookId) throws Exception;

  public List<String> findEmailsInPublicAddressBook(String username, String groupId) throws Exception;

  public List<String> getAllEmailBySharedGroup(String username, String addressBookId) throws Exception;

  public AddressBook toAddressBook(Node contactGroupNode) throws Exception;

  public AddressBook loadPersonalAddressBook(String username, String groupId) throws Exception;

  public AddressBook getSharedAddressBookById(String username, String addressBookId) throws Exception;

  public List<AddressBook> findPersonalAddressBooksByOwner(String username) throws Exception;

  public List<Contact> removeContacts(String username, List<String> contactIds) throws Exception;

  public void moveContacts(String username, List<Contact> contacts, String addressType) throws Exception;
 
  public List<String> getUserContactNodesByGroup(String username, String groupId) throws Exception;

  /**
   * Remove a personal addressBook. Does not clean the contacts it contains
   * @param username
   * @param addressBookId
   * @return
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

  public void saveContact(String username, Contact contact, boolean isNew) throws Exception;

  public void savePersonalOrSharedAddressBook(String username,
                                              AddressBook addressbook,
                                              boolean isNew) throws Exception;

  public void removeUserShareContact(String username, String contactId, String removedUser) throws Exception;

  public void unshareAddressBook(String username, String addressBookId, String removedUser) throws Exception;

  public void shareAddressBook(String username, String addressBookId, List<String> receiveUsers) throws Exception;

  public void shareContact(String username, String[] contactIds, List<String> receiveUsers) throws Exception;

  public List<SharedAddressBook> findSharedAddressBooksByUser(String username) throws Exception;

  public void removeSharedContact(String username, String addressBookId, String contactId) throws Exception;

  public Contact getSharedContact(String username, String contactId) throws Exception;

  public DataPageList getSharedContacts(String username) throws Exception;

  public void saveSharedContact(String username, Contact contact) throws Exception;

  public void saveContactToSharedAddressBook(String username,
                                             String addressBookId,
                                             Contact contact,
                                             boolean isNew) throws Exception;

  public Contact getSharedContactAddressBook(String username, String contactId) throws Exception;

  public ContactPageList getSharedContactsByAddressBook(String username,
                                                        SharedAddressBook addressBook) throws Exception;

  public ContactPageList getPublicContactsByAddressBook(String groupId) throws Exception;

  public List<GroupContactData> getPublicContacts(String[] groupIds) throws Exception;
  public List<String> getPublicAddressBookContacts(String[] groupIds) throws Exception;


  public boolean hasContacts(String groupId) throws Exception;

  public void addUserContactInAddressBook(String userId, String addressBookId) throws Exception;

  public void contactToNode(Node contactsHome, Contact contact, boolean isNew) throws Exception;

  public Tag getTag(Node tagNode) throws Exception;

  public void updateTag(String username, Tag tag) throws Exception;

  public Tag getTag(String username, String tagId) throws Exception;

  public List<Tag> getTags(String username) throws Exception;

  public DataPageList getContactPageListByTag(String username, String tagId) throws Exception;

  public void addTag(String username, List<String> contactIds, String tagId) throws Exception;

  public void addTag(String username, List<String> contactIds, List<Tag> tags) throws Exception;

  public void removeTagInContacts(NodeIterator it, String tagId) throws Exception;

  public Tag removeTag(String username, String tagId) throws Exception;

  public void removeContactTag(String username, List<String> contactIds, List<String> tags) throws Exception;

  public DataPageList searchContact(String username, ContactFilter filter) throws Exception;

  public Map<String, String> findEmailsByFilter(String username, ContactFilter filter) throws Exception;

  public void feedEmailResult(Map<String, String> emails, Node contactNode) throws Exception;

  public void copyNodes(String username,Node srcHomeNode, NodeIterator iter, String destAddress, String destType ) throws Exception;
  
  public void pasteAddressBook(String username,
                               String srcAddress,
                               String srcType,
                               String destAddress,
                               String destType) throws Exception;

  public List<Contact> pasteContacts(String username,
                                     String destAddress,
                                     String destType,
                                     Map<String, String> contactsMap) throws Exception;

  public Node saveCopyContact(Node contactHomeNode,
                              Contact contact,
                              String destAddress,
                              String destType) throws Exception;

  public void registerNewUser(User user, boolean isNew) throws Exception;

  /**
   * Create a session provider for current context. The method first try to get a normal session provider, 
   * then attempts to create a system provider if the first one was not available.
   * @return a SessionProvider initialized by current SessionProviderService
   * @see SessionProviderService#getSessionProvider(null)
   */
  public SessionProvider createSessionProvider();

  /*
  public SessionProvider createUserProvider() {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    SessionProviderService service = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class);
    return service.getSessionProvider(null) ;    
  }  
   */
  public SessionProvider createSystemProvider();

  /**
   * Safely closes JCR session provider. Call this method in finally to clean any provider initialized by createSessionProvider()
   * @param sessionProvider the sessionProvider to close
   * @see SessionProvider#close();
   */
  public void closeSessionProvider(SessionProvider sessionProvider);

  public void saveAddress(String username, String emailAddress) throws Exception;

  public Node getNodeByPath(String nodePath, SessionProvider sessionProvider) throws Exception;

  
  public String valuesToString(Value[] values);

  public boolean haveEditPermissionOnAddressBook(String username, String addressBookId) throws Exception;

  public boolean haveEditPermissionOnContact(String username, Contact contact) throws Exception;

  public Session getSession(SessionProvider sprovider) throws Exception;
  
  public enum AddressBookType {Personal, Shared, Public};
  
  public Node getSharedContactsHome(String user) throws Exception;

}
