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

import java.util.List;

import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactFilter;
import org.exoplatform.contact.service.AddressBook;
import org.exoplatform.contact.service.ContactPageList;
import org.exoplatform.contact.service.GroupContactData;
import org.exoplatform.contact.service.Tag;
import org.exoplatform.contact.service.DataPageList;
import org.exoplatform.services.jcr.ext.common.SessionProvider;



/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Jul 2, 2007  
 */
public interface DataStorage {  
  public List<Contact> getAllContact(String username) throws Exception ;
  public ContactPageList getContactPageListByGroup(String username, String groupId) throws Exception ;
  public ContactPageList getContactPageListByTag(String username, ContactFilter filter) throws Exception ;
  public ContactPageList getContactPageListByGroup(String username, ContactFilter filter, boolean isPublic) throws Exception;
  public List<String>  getAllEmailAddressByGroup(String username, String groupId) throws Exception ;
  public Contact getContact(String username, String contactId) throws Exception ;
  public void saveContact(String username, Contact contact, boolean isNew) throws Exception ;
  public List<Contact> removeContacts(String username, List<String> contactIds) throws Exception ;
  public List<Contact> moveContacts(SessionProvider sysProvider, String username, List<String> contactIds, String[] groupId, boolean toPublic) throws Exception ;
  
  public ContactPageList getSharedContactsByGroup(String groupId) throws Exception ;
  public List<GroupContactData> getSharedContacts(String[] groupIds) throws Exception ;
  public List<String> getSharedGroupContacts(String[] groupIds) throws Exception ;
  public Contact getSharedContact(String contactId) throws Exception ;
  public void saveSharedContact(Contact contact, boolean isNew) throws Exception ;
  public Contact removeSharedContact(String contactId) throws Exception ;
  
  public List<AddressBook> getGroups(String username) throws Exception ;
  public AddressBook getGroup(String username, String groupId) throws Exception ;
  public void saveGroup(String username, AddressBook group, boolean isNew) throws Exception ;
  public AddressBook removeGroup(String username, String groupId) throws Exception ;
  
  public List<Contact> shareContacts(String username, List<String> contactIds, String[] groupIds) throws Exception ;
  public List<GroupContactData> getPublicContacts(String[] groupIds) throws Exception ;
  
  public List<Tag> getTags(String username) throws Exception ;
  public Tag getTag(String username, String tagName) throws Exception ;
  public void addTag(String username, List<String> contactIds, String tagId) throws Exception ;
  public DataPageList getContactPageListByTag(String username, String tagName) throws Exception ;
  public void addTag(String username, List<Contact> contacts, List<Tag> tags) throws Exception ;
  public Tag removeTag(String username, String tagName) throws Exception ;
  public void removeContactTag(String username, List<String>contactIds, List<String> tags) throws Exception ;
}
