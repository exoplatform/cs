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
  public void moveContacts(SessionProvider sysProvider, String username, List<Contact> contacts, String addressType ) throws Exception ;
  //public ContactPageList getContactPageListByTag(SessionProvider sProvider, String username, ContactFilter filter) throws Exception ;
  public ContactPageList getContactPageListByGroup(SessionProvider sProvider, String username, ContactFilter filter, String type) throws Exception;
  public DataPageList searchContact(SessionProvider sProvider, String username, ContactFilter filter) throws Exception ;
  
  public ContactPageList getPublicContactsByAddressBook(SessionProvider sProvider, String addressBookId) throws Exception ;
  public List<GroupContactData> getPublicContacts(SessionProvider sProvider, String[] groupIds) throws Exception ;
  public List<String> getPublicAddressBookContacts(SessionProvider sProvider, String[] groupIds) throws Exception ;
  public Contact getPublicContact(String contactId) throws Exception ;
  public void savePublicContact(Contact contact, boolean isNew) throws Exception ;
  public Contact removePublicContact(SessionProvider sProvider, String contactId) throws Exception ;
  public List<String> getAllEmailByPublicGroup(String username, String groupId) throws Exception ;
  public void addGroupToPersonalContact(String userId, String groupId) throws Exception ;
  
  public List<ContactGroup> getGroups(SessionProvider sProvider, String username) throws Exception ;
  public ContactGroup getGroup(SessionProvider sProvider, String username, String groupId) throws Exception ;
  public void saveGroup(SessionProvider sProvider, String username, ContactGroup group, boolean isNew) throws Exception ;
  public ContactGroup removeGroup(SessionProvider sProvider, String username, String groupId) throws Exception ;
  
  public void shareAddressBook(SessionProvider sProvider, String username, String addressBookId, List<String> receiverUsers) throws Exception ;
  public ContactGroup getSharedGroup(String username, String groupId) throws Exception ;
  public List<String> getSharedAddressBooks(SessionProvider sProvider, String username) throws Exception ;
  public ContactPageList getSharedContactsByAddressBook(SessionProvider sProvider, String username, String addressBookId) throws Exception ;
  public void removeSharedAddressBook(SessionProvider sProvider, String username, String addressBookId) throws Exception ;
  public void saveContactToSharedAddressBook(SessionProvider sProvider, String username, String addressBookId, Contact contact, boolean isNew) throws Exception ;
  public Contact getSharedContacts(SessionProvider sProvider, String username, String contactId) throws Exception ;
  public List<String> getAllEmailBySharedGroup(String username, String addressBookId) throws Exception ;
  
  public List<Tag> getTags(SessionProvider sProvider, String username) throws Exception ;
  public Tag getTag(SessionProvider sProvider, String username, String tagId) throws Exception ;
  public DataPageList getContactPageListByTag(SessionProvider sProvider, String username, String tagId) throws Exception ;
  public void addTag(SessionProvider sProvider, String username, List<String> contactIds, List<Tag> tags) throws Exception ;
  public void addTag(SessionProvider sProvider, String username, List<String> contactIds, String tagId) throws Exception ;
  public Tag removeTag(SessionProvider sProvider, String username, String tagName) throws Exception ;
  public void removeContactTag(SessionProvider sProvider, String username, List<String>contactIds, List<String> tags) throws Exception ;
  public void updateTag(SessionProvider sProvider, String username,Tag tag) throws Exception ;
  
  public void pasteAddressBook(SessionProvider sProvider, String username, String srcAddress, String srcType, String destAddress, String destType) throws Exception ;
  public void pasteContacts(SessionProvider sProvider, String username, String destAddress, String destType, List<Contact> contacts) throws Exception ;
  public ContactImportExport getContactImportExports(String type) ;
  public String[] getImportExportType() throws Exception ;
  
}
