/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
 * This interface contains all deprecated methods of the ContactService API.
 * Don't rely on them as they will be removed in future.
 */
public interface ContactServiceLegacy {

  /**
   * @deprecated use
   *             {@link ContactService#saveAddressBook(String, AddressBook, boolean)}
   */
  public void saveGroup(SessionProvider sProvider,
                        String username,
                        AddressBook addressBook,
                        boolean isNew) throws Exception;

  /**
   * @deprecated use
   *             {@link ContactService#getPersonalAddressBook(String, String)}
   */
  public AddressBook getGroup(SessionProvider sProvider, String owner, String addressBookID) throws Exception;

  /**
   * @deprecated use {@link ContactService#removeAddressBook(String, String)}
   */
  public AddressBook removeGroup(SessionProvider sProvider, String username, String groupId) throws Exception;

  /**
   * @deprecated use
   *             {@link ContactService#unshareAddressBook(String, String, String)}
   */
  public void removeUserShareAddressBook(SessionProvider sProvider,
                                         String username,
                                         String addressBookId,
                                         String removedUser) throws Exception;

  /**
   * @deprecated use {@link ContactService#getSharedAddressBook(String, String)}
   */
  public AddressBook getSharedGroup(SessionProvider sProvider, String username, String groupId) throws Exception;

  /**
   * @deprecated use {@link ContactService#getSharedAddressBooks(String)}
   */
  public List<SharedAddressBook> getSharedAddressBooks(SessionProvider sProvider, String username) throws Exception;

  /**
   * @deprecated use
   *             {@link ContactService#saveContact(String, Contact, boolean)}
   */
  public void saveContact(SessionProvider sProvider, String username, Contact contact, boolean isNew) throws Exception;

  /**
   * @deprecated use {@link ContactService#getPersonalContacts(String)}
   */
  public List<Contact> getAllContacts(SessionProvider sProvider, String username) throws Exception;

  /**
   * @deprecated use
   *             {@link ContactService#getPersonalContactsByAddressBook(String, String)}
   */
  public ContactPageList getContactPageListByGroup(SessionProvider sProvider,
                                                   String username,
                                                   String groupId) throws Exception;

  /**
   * @deprecated use
   *             {@link ContactService#getEmailsByAddressBook(String, String)}
   */
  public List<String> getAllEmailAddressByGroup(SessionProvider sProvider,
                                                String username,
                                                String groupId) throws Exception;

  /**
   * @deprecated use
   *             {@link ContactService#getEmailsByAddressBook(String, String)}
   */
  public List<String> getAllEmailBySharedGroup(String username, String addressBookId) throws Exception;

  /**
   * @deprecated use
   *             {@link ContactService#getEmailsByAddressBook(String, String)}
   */
  public List<String> getAllEmailByPublicGroup(String username, String groupId) throws Exception;

  /**
   * @deprecated use
   *             {@link ContactService#addUserContactInAddressBook(String, String)}
   */
  public void addGroupToPersonalContact(String userId, String groupId) throws Exception;

  /**
   * @deprecated use {@link ContactService#getPublicContact(String)}
   */
  public Contact getPersonalContact(String userId) throws Exception;

  /**
   * @deprecated use {@link ContactService#searchEmails(String, ContactFilter)}
   */
  public Map<String, String> searchEmails(SessionProvider sysProvider,
                                          String username,
                                          ContactFilter filter) throws Exception;
  
  /**
   * @deprecated use {@link ContactService#getPersonalContactsByFilter(String, ContactFilter)}
   * or {@link ContactService#getSharedContactsByFilter(String, ContactFilter)} or {@link ContactService#getPublicContactsByFilter(String, ContactFilter)}
   */
  public ContactPageList getContactPageListByGroup(SessionProvider sProvider, String username, ContactFilter filter, String type) throws Exception;

  /** 
   * @deprecated use {@link ContactService#removeContacts(String, List)}
   */
  public List<Contact> removeContacts(SessionProvider sProvider, String username, List<String> contactIds) throws Exception ;
}
