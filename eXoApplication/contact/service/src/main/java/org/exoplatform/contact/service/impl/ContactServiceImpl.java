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
import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.contact.service.ContactImportExport;
import org.exoplatform.contact.service.ContactPageList;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.GroupContactData;
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
  
  public List<Contact> getAllContact(SessionProvider sProvider, String username) throws Exception {
    return storage_.getAllContact(sProvider, username);
  }
  
  /*public ContactPageList getContactPageListByTag(String username, ContactFilter filter) throws Exception {
    return storage_.getContactPageListByTag(username, filter);
  }*/
  
  public ContactPageList getContactPageListByGroup(SessionProvider sProvider, String username, String groupId) throws Exception {
    return storage_.getContactPageListByGroup(sProvider, username, groupId);
  }

  public ContactPageList getContactPageListByGroup(SessionProvider sProvider, String username, ContactFilter filter, boolean isPublic) throws Exception {
    return storage_.getContactPageListByGroup(sProvider, username, filter, isPublic) ;
  }
  
  public List<String> getAllEmailAddressByGroup(SessionProvider sProvider, String username, String groupId) throws Exception {
    return storage_.getAllEmailAddressByGroup(sProvider, username, groupId);
  }
  public Contact getContact(SessionProvider sProvider, String username, String contactId) throws Exception {
    return storage_.getContact(sProvider, username, contactId);
  }
  
  public void saveContact(SessionProvider sProvider, String username, Contact contact, boolean isNew) throws Exception {
    storage_.saveContact(sProvider, username, contact, isNew);    
  }
  
  public List<Contact> removeContacts(SessionProvider sProvider, String username, List<String> contactIds) throws Exception {
    return storage_.removeContacts(sProvider, username, contactIds);
  }
  
  public List<Contact> moveContacts(SessionProvider sProvider, String username, List<String> contactIds, String[] groupId, boolean toPublic) throws Exception {
    return storage_.moveContacts(sProvider, username, contactIds, groupId, toPublic) ;
  }
   
  public List<ContactGroup> getGroups(SessionProvider sProvider, String username) throws Exception {
    return storage_.getGroups(sProvider, username);
  }
  
  public ContactGroup getGroup(SessionProvider sProvider, String username, String groupId) throws Exception {
    return storage_.getGroup(sProvider, username, groupId);
  }
  
  public void saveGroup(SessionProvider sProvider, String username, ContactGroup group, boolean isNew) throws Exception {
    storage_.saveGroup(sProvider, username, group, isNew);    
  }
  
  public ContactGroup removeGroup(SessionProvider sProvider, String username, String groupId) throws Exception {
    return storage_.removeGroup(sProvider, username, groupId);
  }

  public List<GroupContactData> getPublicContacts(SessionProvider sProvider, String[] groupIds) throws Exception {
    return storage_.getPublicContacts(sProvider, groupIds);
  }

  public List<Contact> shareContacts(SessionProvider sProvider, String username, List<String> contactIds, String[] groupIds) throws Exception {
    return storage_.shareContacts(sProvider, username, contactIds, groupIds) ;
  }

  public Contact getSharedContact(SessionProvider sProvider, String contactId) throws Exception {
    return storage_.getSharedContact(sProvider, contactId);
  }

  public List<GroupContactData> getSharedContacts(SessionProvider sProvider, String[] groupIds) throws Exception {
    return storage_.getSharedContacts(sProvider, groupIds);
  }
  
  public List<String> getSharedGroupContacts(SessionProvider sProvider, String[] groupIds) throws Exception{
    return storage_.getSharedGroupContacts(sProvider, groupIds);
  }
  public Contact removeSharedContact(SessionProvider sProvider, String contactId) throws Exception {
    return storage_.removeSharedContact(sProvider, contactId);
  }

  public void saveSharedContact(SessionProvider sProvider, Contact contact, boolean isNew) throws Exception {
    storage_.saveSharedContact(sProvider, contact, isNew);
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
  
  public void removeContactTag(SessionProvider sProvider, String username, List<String>contactIds, List<String> tags) throws Exception {
    storage_.removeContactTag(sProvider, username, contactIds, tags) ;
  }
  
  public ContactPageList getSharedContactsByGroup(SessionProvider sProvider, String groupId) throws Exception {
    return storage_.getSharedContactsByGroup(sProvider, groupId) ;
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
}
