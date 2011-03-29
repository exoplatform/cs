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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyIterator;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.contact.service.AddressBook;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactAttachment;
import org.exoplatform.contact.service.ContactFilter;
import org.exoplatform.contact.service.ContactPageList;
import org.exoplatform.contact.service.DataPageList;
import org.exoplatform.contact.service.DataStorage;
import org.exoplatform.contact.service.GroupContactData;
import org.exoplatform.contact.service.SharedAddressBook;
import org.exoplatform.contact.service.Tag;
import org.exoplatform.contact.service.Utils;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 10, 2007  
 */
public class JCRDataStorage implements DataStorage {
  
  private static final String PROP_ADDRESSBOOK_REFS = "exo:categories";
  final private static String CONTACTS = "contacts".intern() ;
  final private static String PERSONAL_ADDRESS_BOOKS = "contactGroup".intern() ;
  final private static String GROUP_ADDRESS_BOOKS = "GroupAdSdress".intern() ;
  final private static String TAGS = "tags".intern() ;
  final private static String NT_UNSTRUCTURED = "nt:unstructured".intern() ;
  final private static String CONTACT_APP = "ContactApplication".intern() ;
  final private static String SHARED_HOME = "Shared".intern() ;
  final private static String SHARED_CONTACT = "SharedContact".intern() ;
  final private static String SHARED_ADDRESSBOOK = "SharedAddressBook".intern() ;
  final private static String SHARED_MIXIN = "exo:contactShared".intern();
  final private static String SHARED_PROP = "exo:sharedId".intern();
  private static final Log log = ExoLogger.getLogger("cs.contact.service");
  
  private NodeHierarchyCreator nodeHierarchyCreator_ ;
  private RepositoryService repoService_ ;
  
  public JCRDataStorage(NodeHierarchyCreator nodeHierarchyCreator, RepositoryService repoService)throws Exception {
    nodeHierarchyCreator_ = nodeHierarchyCreator ;
    repoService_ = repoService ;
  }  
  
  /**
   * {@inheritDoc}
   */
  public Node getSharedContact(String userId) throws Exception {
    Node contactHome = getContactUserDataHome(userId);
    Node sharedHome ;
    try {
      sharedHome = contactHome.getNode(SHARED_HOME) ;
    } catch (PathNotFoundException ex) {
      sharedHome = contactHome.addNode(SHARED_HOME, NT_UNSTRUCTURED) ;
      contactHome.save() ;
    }    
    try{
      return sharedHome.getNode(SHARED_CONTACT) ;
    }catch(PathNotFoundException ex) {
      Node sharedContact = sharedHome.addNode(SHARED_CONTACT, NT_UNSTRUCTURED) ;
      if(sharedContact.canAddMixin("mix:referenceable")) {
        sharedContact.addMixin("mix:referenceable") ;
      }
      sharedHome.save() ;
      return sharedContact ;
    }
  }  
  
/**
 * {@inheritDoc}
 */
  public Node getSharedAddressBooksHome(String userId) throws Exception {
      Node contactHome = getContactUserDataHome(userId);
      Node sharedHome;
      try {
        sharedHome = contactHome.getNode(SHARED_HOME);
      } catch (PathNotFoundException ex) {
        sharedHome = contactHome.addNode(SHARED_HOME, NT_UNSTRUCTURED);
        contactHome.save();
      }
      try {
        return sharedHome.getNode(SHARED_ADDRESSBOOK);
      } catch (PathNotFoundException ex) {
        Node sharedAddressBooksHome = sharedHome.addNode(SHARED_ADDRESSBOOK, NT_UNSTRUCTURED);
        if (sharedAddressBooksHome.canAddMixin("mix:referenceable")) {
          sharedAddressBooksHome.addMixin("mix:referenceable");
        }
        sharedHome.save();
        return sharedAddressBooksHome;
      }
  }  

  /**
   * {@inheritDoc}
   */
  public Contact loadPublicContactByUser(String userId) throws Exception {
    try {
      Node contactHomeNode = getPersonalContactsHome(userId) ;
      return Utils.getContact(contactHomeNode.getNode(userId), PUBLIC);
    } catch (PathNotFoundException e) {
      log.error("Public contact " + userId + " not found");
      return null ;
    } 
  }

  /**
   * {@inheritDoc}
   */
  public List<Contact> findAllContactsByOwner(String username) throws Exception {
      Node contactHomeNode = getPersonalContactsHome(username);
      List<Contact> contacts = new ArrayList<Contact>();
      NodeIterator iter = contactHomeNode.getNodes();
      Contact contact;
      while (iter.hasNext()) {
        Node contactNode = iter.nextNode();
        contact = Utils.getContact(contactNode, PERSONAL);
        contacts.add(contact);
      }
      return contacts;
  }

  /**
   * {@inheritDoc}
   */
  public ContactPageList findContactsByFilter(String username,
                                                   ContactFilter filter,
                                                   String type) throws Exception {
    boolean canfind = false ;
    
      if (type.equals(PERSONAL)) {
        // look in user home
        Node contactHomeNode = getPersonalContactsHome(username);
        filter.setAccountPath(contactHomeNode.getPath());
        canfind = true;
        //qm = getSession(createSystemProvider()).getWorkspace().getQueryManager();
      } else if (type.equals(PUBLIC)) {
        // look in all users
        Node publicContactHomeNode = getPublicContactsHome();
        String usersPath = nodeHierarchyCreator_.getJcrPath(USERS_PATH);
        filter.setAccountPath(usersPath);
        canfind = true;
        //qm = publicContactHomeNode.getSession().getWorkspace().getQueryManager();
      } else if (type.equals(SHARED)) {
        // look in contacts shared to username
        Node sharedAddressBookHolder = getSharedAddressBooksHome(username);
        PropertyIterator iter = sharedAddressBookHolder.getReferences();
        Node addressBook;
        while (iter.hasNext()) {
          addressBook = iter.nextProperty().getParent();
          if (addressBook.getName().equals(filter.getCategories()[0])) {
            Node contacts = addressBook.getParent().getParent().getNode(CONTACTS);
            filter.setAccountPath(contacts.getPath());
            canfind = true;
            //qm = contacts.getSession().getWorkspace().getQueryManager();
            break;
          }
        }
      }
      if (canfind) {
        return new ContactPageList(username,
                                   10,
                                   filter.getStatement(),
                                   type);
      }
      return null;
  }
  
  /**
   * {@inheritDoc}
   */
  public Contact loadPersonalContact(String ownerUserId, String contactId) throws Exception {
      Node contactHomeNode = getPersonalContactsHome(ownerUserId);
      try {
        return Utils.getContact(contactHomeNode.getNode(contactId), PERSONAL);
      } catch (PathNotFoundException ex) {
        return null;
      }
  }

  /**
   * {@inheritDoc}
   */
  public ContactPageList findPersonalContactsByAddressBook(String owner, String addressBookId) throws Exception {
      Node userContactsHome = getPersonalContactsHome(owner);
      String queryString = new StringBuffer("/jcr:root" + userContactsHome.getPath()
          + "//element(*,exo:contact)[@exo:categories='").append(addressBookId)
                                                         .append("']")
                                                         .append("order by @exo:fullName,@exo:id ascending")
                                                         .toString();
      ContactPageList pageList = new ContactPageList(owner,
                                                     10,
                                                     queryString,
                                                     PERSONAL);
      return pageList;
  }
 
  
 /**
  * {@inheritDoc}
  */
public List<String> findEmailsByAddressBook(String username, String addressBookId) throws AddressBookNotFoundException, Exception {
   AddressBookType type = getAddressBookType(username, addressBookId);

   if (type == AddressBookType.Personal) {
     return findEmailsInPersonalAddressBook(username, addressBookId);
   } else if (type == AddressBookType.Shared) {
     return findEmailsInPersonalAddressBook(username, addressBookId);
   } else if (type == AddressBookType.Public) {
     return findEmailsInPublicAddressBook(username, addressBookId);
   }
   throw new AddressBookNotFoundException(addressBookId);
 }
  
  /**
   * {@inheritDoc}
   */
  public AddressBookType getAddressBookType(String username, String addressBookId) throws Exception {
    AddressBook shared = getSharedAddressBookById(username, addressBookId);
    if (shared != null) {
      return AddressBookType.Shared;
    }
    
    AddressBook personal = findPersonalAddressBookById(username, addressBookId);
    if (personal != null) {
      return AddressBookType.Personal;
    }
    return null;
  }

  public AddressBook findPersonalAddressBookById(String username, String addressBookId) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public List<String> findEmailsInPersonalAddressBook(String username, String addressBookId) throws Exception {
      Node contactHome = getPersonalContactsHome(username);
      QueryManager qm = contactHome.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer("/jcr:root" + contactHome.getPath()
          + "//element(*,exo:contact)[@exo:categories='").append(addressBookId).append("']");
      NodeIterator it = qm.createQuery(queryString.toString(), Query.XPATH).execute().getNodes();
      List<String> address = new ArrayList<String>();
      while (it.hasNext()) {
        Node contact = it.nextNode();
        try {
          String emails = Utils.valuesToString(contact.getProperty("exo:emailAddress").getValues());
          if (!Utils.isEmpty(emails))
            address.add(emails.split(",")[0].split(";")[0]);
        } catch (PathNotFoundException e) { }
      }
      return address;
  }
  
  /**
   * {@inheritDoc}
   */
  public List<String> findEmailsInPublicAddressBook(String username, String groupId) throws Exception {
    String usersPath = nodeHierarchyCreator_.getJcrPath(USERS_PATH) ;
    QueryManager qm = getSession(createSystemProvider()).getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root" + usersPath 
                                                + "//element(*,exo:contact)[@exo:categories='")
                                                .append(groupId).append("']");                                                
    NodeIterator it = qm.createQuery(queryString.toString(), Query.XPATH).execute().getNodes();
    List<String> address = new ArrayList<String>();
    while (it.hasNext()){
      Node contact = it.nextNode();
      try {
        String emails = Utils.valuesToString(contact.getProperty("exo:emailAddress").getValues());
        if(!Utils.isEmpty(emails))
          address.add(emails.split(",")[0].split(";")[0]);
      } catch (PathNotFoundException e) {}
    }
    return address ;
  }
  
  /**
   * {@inheritDoc}
   */
  public List<String> getAllEmailBySharedGroup(String username, String addressBookId) throws Exception {
    Node sharedAddressBookMock = getSharedAddressBooksHome(username) ;
    PropertyIterator iter = sharedAddressBookMock.getReferences() ;
    Node addressBook ;      
    QueryManager qm = sharedAddressBookMock.getSession().getWorkspace().getQueryManager();
    while(iter.hasNext()) {
      addressBook = iter.nextProperty().getParent() ;
      if(addressBook.getName().equals(addressBookId)) {
        StringBuffer queryString = new StringBuffer("/jcr:root" + addressBook.getParent().getParent().getNode(CONTACTS).getPath() 
                        + "//element(*,exo:contact)[(@exo:categories='").
                        append(addressBookId).append("')]") ;
        NodeIterator it = qm.createQuery(queryString.toString(), Query.XPATH).execute().getNodes();
        List<String> address = new ArrayList<String>();
        while (it.hasNext()){
          Node contact = it.nextNode();
          try {
            String emails = Utils.valuesToString(contact.getProperty("exo:emailAddress").getValues());
            if(!Utils.isEmpty(emails))
              address.add(emails.split(",")[0].split(";")[0]);
          } catch (PathNotFoundException e) {}
        }
        return address ;         
      } 
    }
    return null ;
  }
  
  /**
   * {@inheritDoc}
   */
  public AddressBook toAddressBook(Node contactGroupNode) throws Exception {
    AddressBook contactGroup = new AddressBook();
    if (contactGroupNode.hasProperty("exo:id")) 
      contactGroup.setId(Utils.decodeGroupId(contactGroupNode.getProperty("exo:id").getString()));
    if (contactGroupNode.hasProperty("exo:name")) 
      contactGroup.setName(contactGroupNode.getProperty("exo:name").getString());
    if (contactGroupNode.hasProperty("exo:description")) 
      contactGroup.setDescription(contactGroupNode.getProperty("exo:description").getString());
    if (contactGroupNode.hasProperty("exo:viewPermissionUsers"))
      contactGroup.setViewPermissionUsers(
          Utils.valuesToStringArray(contactGroupNode.getProperty("exo:viewPermissionUsers").getValues())) ;
    if (contactGroupNode.hasProperty("exo:editPermissionUsers"))
      contactGroup.setEditPermissionUsers(
          Utils.valuesToStringArray(contactGroupNode.getProperty("exo:editPermissionUsers").getValues())) ;
    
    if (contactGroupNode.hasProperty("exo:viewPermissionGroups"))
      contactGroup.setViewPermissionGroups(
          Utils.valuesToStringArray(contactGroupNode.getProperty("exo:viewPermissionGroups").getValues())) ;
    if (contactGroupNode.hasProperty("exo:editPermissionGroups"))
      contactGroup.setEditPermissionGroups(
          Utils.valuesToStringArray(contactGroupNode.getProperty("exo:editPermissionGroups").getValues())) ;
    return contactGroup;
  }

  /**
   * {@inheritDoc}
   */
  public AddressBook loadPersonalAddressBook(String username, String groupId) throws Exception {
      if (groupId == null)
        return null;
      Node contactGroupHomeNode = getPersonalAddressBooksHome(username);
      if (contactGroupHomeNode.hasNode(groupId))
        return toAddressBook(contactGroupHomeNode.getNode(groupId));
      return null;
  }
  
  public AddressBook loadPublicAddressBook(String username, String groupId) throws Exception {
    if (groupId == null) return null;
    Node contactGroupHomeNode = getPublicAddressHome();
    if (contactGroupHomeNode.hasNode(Utils.encodeGroupId(groupId)))
      return toAddressBook(contactGroupHomeNode.getNode(Utils.encodeGroupId(groupId)));
    return null;
  }

  public AddressBook getSharedAddressBookById(String username, String addressBookId) throws Exception {
    Node sharedAddressBookNode = getSharedAddressBooksHome(username) ;
    PropertyIterator iter = sharedAddressBookNode.getReferences() ;
    Node addressBook ;      
    while(iter.hasNext()) {
      addressBook = iter.nextProperty().getParent() ;
      if(addressBook.getName().equals(addressBookId)) {
        return toAddressBook(addressBook) ;
      }
    }
    return null ;
  }

  /**
   * {@inheritDoc}
   */
  public List<AddressBook> findPersonalAddressBooksByOwner(String username) throws Exception {
      Node addressBooksHome = getPersonalAddressBooksHome(username);
      List<AddressBook> addressBooks = new ArrayList<AddressBook>();
      NodeIterator iter = addressBooksHome.getNodes();
      while (iter.hasNext()) {
        Node addressBook = iter.nextNode();
        addressBooks.add(toAddressBook(addressBook));
      }
      return addressBooks;
  }

  public List<String> getPublicAddresses(String username) throws Exception {
    OrganizationService organizationService = 
      (OrganizationService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(OrganizationService.class) ;
    Object[] objGroupIds = organizationService.getGroupHandler().findGroupsOfUser(username).toArray() ;
    List<String> groupIds = new ArrayList<String>() ;
    for (Object object : objGroupIds) {
      groupIds.add(((Group)object).getId()) ;
    }
    Node addressBooksHome = getPublicAddressHome();
    List<String> addressBooks = new ArrayList<String>();
    NodeIterator iter = addressBooksHome.getNodes();
    while (iter.hasNext()) {
      Node addressBook = iter.nextNode();
      String id = Utils.decodeGroupId(addressBook.getProperty("exo:id").getString());
      if (addressBook.hasProperty("exo:viewPermissionUsers")) {
        String[] viewUsers = Utils.valuesToStringArray(addressBook.getProperty("exo:viewPermissionUsers").getValues());
        if (viewUsers.length > 0 && Arrays.asList(viewUsers).contains(username)) {
          if (!addressBooks.contains(id)) addressBooks.add(id);
        }
      }
      if (addressBook.hasProperty("exo:viewPermissionGroups")) {        
        String[] viewGroups = Utils.valuesToStringArray(addressBook.getProperty("exo:viewPermissionGroups").getValues());
        for (String viewGroup : viewGroups) {
          if (viewGroup.contains(Utils.COLON)) viewGroup = viewGroup.split(Utils.COLON)[0];
          if (groupIds.contains(viewGroup)) {
            if (!addressBooks.contains(id)) addressBooks.add(id);
            break;
          }
        }
      }
    }
    return addressBooks;
  }
  
  /**
   * {@inheritDoc}
   */
  public List<Contact> removeContacts(String username,
                                      List<String> contactIds) throws Exception {
     
      Node contactHomeNode = getPersonalContactsHome(username);
      List<Contact> contacts = new ArrayList<Contact>();
      for (String contactId : contactIds) {
        if (contactHomeNode.hasNode(contactId)) {
          Contact contact = loadPersonalContact(username, contactId);
          contactHomeNode.getNode(contactId).remove();
          contactHomeNode.getSession().save();
          contacts.add(contact);
        }
      }
      return contacts;
  }
  
  /**
   * {@inheritDoc}
   */
  public void moveContacts(String username, List<Contact> contacts, String addressType ) throws Exception {
      Node publicContactHome = getPersonalContactsHome(username);
      for(Contact contact : contacts) {
        if(addressType.equals(PERSONAL)) {        
          saveContactUser(username, contact, false) ;
        }else if(addressType.equals(SHARED)) {
         //CS-2389
         if (!haveEditPermissionOnAddressBook(username, contact.getAddressBookIds()[0]) || 
             (contact.getContactType().equals(SHARED) && !haveEditPermissionOnContact(username, contact))){
           System.out.println("\n Do not have edit permission. \n");
           throw new Exception();
         }
         saveContactToSharedAddressBook(username, contact.getAddressBookIds()[0], contact, true) ;
         if (publicContactHome.hasNode(contact.getId()))
           publicContactHome.getNode(contact.getId()).remove() ;
        }         
      }
      if(publicContactHome.getSession().hasPendingChanges()) publicContactHome.getSession().save() ;
  }
   
  /**
   * {@inheritDoc}
   */
  public AddressBook removePersonalAddressBook(String username, String addressBookId) throws Exception {
      Node addressBooksHomeNode = getPersonalAddressBooksHome(username);
      if (addressBooksHomeNode.hasNode(addressBookId)) {
        // load before removing
        AddressBook contactGroup = loadPersonalAddressBook(username, addressBookId);
        // remove the address book
        addressBooksHomeNode.getNode(addressBookId).remove();
        addressBooksHomeNode.save();
        addressBooksHomeNode.getSession().save();
        // Can not call removeContacts(...) here!!!
        return contactGroup;
      }
      return null;
  }

  /**
   * {@inheritDoc}
   */
  public void clearAddressBook(String username, String addressBookId) throws Exception {
      List<String> contactIds = getUserContactNodesByGroup(username, addressBookId);
      removeContacts(username, contactIds);
  }
  
  /**
   * {@inheritDoc}
   */
  public void saveContact(String username, Contact contact, boolean isNew) throws Exception {
      Node contactHomeNode = getPersonalContactsHome(username);
      contactToNode(contactHomeNode, contact, isNew);
      contactHomeNode.getSession().save();
      contactHomeNode.getSession().logout();
  }

  private void saveContactUser(String username, Contact contact, boolean isNew) throws Exception {
  	Node contactHomeNode = getPersonalContactsHome(username);
  	contactToNode(contactHomeNode, contact, isNew);
  	contactHomeNode.getSession().save();
  }

  /**
   * {@inheritDoc}
   */
  public void savePersonalOrSharedAddressBook(String username, AddressBook addressbook, boolean isNew) throws Exception {
      Node groupNode = null ;
      String id = addressbook.getId();
      if (isNew) {
        groupNode = getPersonalAddressBooksHome(username).addNode(id, "exo:contactGroup");
        groupNode.setProperty("exo:id", id);
      } else {
        try {
          groupNode = getPersonalAddressBooksHome(username).getNode(id);
        } catch (PathNotFoundException e) {
          Node sharedAddressBookMock = getSharedAddressBooksHome(username) ;
          PropertyIterator iter = sharedAddressBookMock.getReferences() ;
          Node addressBook ;      
          while(iter.hasNext()) {
            addressBook = iter.nextProperty().getParent() ;
            if(addressBook.getName().equals(id)) {

              //CS-2389
              if (!haveEditPermissionOnAddressBook(username, addressbook.getId())){
                System.out.println("\n Do not have edit permission. \n");
                throw new Exception();
              }
              groupNode = addressBook ;
              break ;
            }
          }
        } 
      }
   // TODO : should throw a business exception instead of PathNotFoundException
      if (groupNode == null && !isNew) throw new PathNotFoundException("No personal or shared address book for user " + username + " was found with ID " + addressbook) ; 
      groupNode.setProperty("exo:name", addressbook.getName());
      groupNode.setProperty("exo:description", addressbook.getDescription());
      groupNode.setProperty("exo:editPermissionUsers", addressbook.getEditPermissionUsers()) ;
      groupNode.setProperty("exo:viewPermissionUsers", addressbook.getViewPermissionUsers()) ;
      groupNode.setProperty("exo:editPermissionGroups", addressbook.getEditPermissionGroups()) ;
      groupNode.setProperty("exo:viewPermissionGroups", addressbook.getViewPermissionGroups()) ;
      if (isNew) groupNode.getSession().save() ;
      else groupNode.save() ;
  }

  public void savePublicAddressBook(AddressBook addressbook, boolean isNew) throws Exception {
    Node groupNode = null ;
    String id = Utils.encodeGroupId(addressbook.getId());
    if (isNew) {
      groupNode = getPublicAddressHome().addNode(id, "exo:contactGroup");
      groupNode.setProperty("exo:id", id);
    } else {
      try {
        groupNode = getPublicAddressHome().getNode(id);
        if (groupNode == null) throw new PathNotFoundException("No address book was found with ID " + addressbook) ; 
      } catch (PathNotFoundException e) {
        log.error(e.getMessage());
      }
    }
    groupNode.setProperty("exo:name", addressbook.getName());
    groupNode.setProperty("exo:description", addressbook.getDescription());
    groupNode.setProperty("exo:editPermissionUsers", addressbook.getEditPermissionUsers()) ;
    groupNode.setProperty("exo:viewPermissionUsers", addressbook.getViewPermissionUsers()) ;
    groupNode.setProperty("exo:editPermissionGroups", addressbook.getEditPermissionGroups()) ;
    groupNode.setProperty("exo:viewPermissionGroups", addressbook.getViewPermissionGroups()) ;
    if (isNew) groupNode.getSession().save() ;
    else groupNode.save() ;
  }

  public void removeUserShareContact(String username, String contactId, String removedUser) throws Exception {
      Node contactNode ;
      String split = "/" ;    
      // shared contacts
      if (username.split(split).length > 1) {
        String usersPath = nodeHierarchyCreator_.getJcrPath(USERS_PATH) ;
        String temp = username.split(usersPath)[1] ;
        String userId = temp.split(split)[1] ;
        contactNode = getPersonalContactsHome(userId).getNode(contactId);
      } else {
        contactNode = getPersonalContactsHome(username).getNode(contactId);
      }
      List<String> values = new ArrayList<String>(
          Arrays.asList(Utils.valuesToStringArray(contactNode.getProperty(SHARED_PROP).getValues())));
      List<String> newValues = new ArrayList<String>(values) ;
      Node sharedContact = getSharedContact(removedUser) ;
      for (String value : values) {
        Node refNode = sharedContact.getSession().getNodeByUUID(value);
        if(refNode.getPath().equals(sharedContact.getPath())) {
          newValues.remove(value) ;
        }
      }
      try {
        String[] viewPer = Utils.valuesToStringArray(contactNode.getProperty("exo:viewPermissionUsers").getValues()) ;
        if (viewPer != null) {
          List<String> newViewPer = new ArrayList<String>() ;
          newViewPer.addAll(Arrays.asList(viewPer)) ;
          newViewPer.remove(removedUser + HYPHEN) ;
          contactNode.setProperty("exo:viewPermissionUsers", newViewPer.toArray(new String [] {})) ;      
          String[] editPer = Utils.valuesToStringArray(contactNode.getProperty("exo:editPermissionUsers").getValues()) ;
          if (editPer != null) {
            List<String> newEditPer = new ArrayList<String>() ;
            newEditPer.addAll(Arrays.asList(editPer)) ;
            newEditPer.remove(removedUser + HYPHEN) ;
            contactNode.setProperty("exo:editPermissionUsers", newEditPer.toArray(new String [] {})) ;
          }
        }
      } catch (PathNotFoundException e) { }
      contactNode.setProperty(SHARED_PROP, newValues.toArray(new String[] {}));
      contactNode.save() ;
      contactNode.getSession().save();
  }
  
  /**
   * {@inheritDoc}
   */
  public void unshareAddressBook(String username, String addressBookId, String removedUser) throws Exception {
    
      Node addressBookNode = getPersonalAddressBooksHome(username).getNode(addressBookId);
      List<String> values = new ArrayList<String>(Arrays.asList(Utils.valuesToStringArray(addressBookNode.getProperty(SHARED_PROP)
                                                                                               .getValues())));
      List<String> newValues = new ArrayList<String>(values);

      Node sharedAddress = getSharedAddressBooksHome(removedUser);
      for (String value : values) {
        Node refNode = sharedAddress.getSession().getNodeByUUID(value);
        if (refNode.getPath().equals(sharedAddress.getPath())) {
          newValues.remove(value);
        }
      }

      String[] viewPer = null;
      try {
        viewPer = Utils.valuesToStringArray(addressBookNode.getProperty("exo:viewPermissionUsers")
                                                 .getValues());
      } catch (PathNotFoundException e) {
      }
      if (viewPer != null) {
        List<String> newViewPer = new ArrayList<String>();
        newViewPer.addAll(Arrays.asList(viewPer));
        newViewPer.remove(removedUser + HYPHEN);
        addressBookNode.setProperty("exo:viewPermissionUsers", newViewPer.toArray(new String[] {}));
        String[] editPer = null;
        try {
          editPer = Utils.valuesToStringArray(addressBookNode.getProperty("exo:editPermissionUsers")
                                                   .getValues());
        } catch (PathNotFoundException e) {
        }
        if (editPer != null) {
          List<String> newEditPer = new ArrayList<String>();
          newEditPer.addAll(Arrays.asList(editPer));
          newEditPer.remove(removedUser + HYPHEN);
          addressBookNode.setProperty("exo:editPermissionUsers",
                                      newEditPer.toArray(new String[] {}));
        }
      }
      addressBookNode.setProperty(SHARED_PROP, newValues.toArray(new String[] {}));
      addressBookNode.save();
      addressBookNode.getSession().save();
  }

  /**
   * {@inheritDoc}
   */
  public void shareAddressBook(String username, String addressBookId, List<String> receiveUsers) throws Exception {
      Node addressBookNode = getPersonalAddressBooksHome(username).getNode(addressBookId);
      Value[] values = {};
      if (addressBookNode.isNodeType(SHARED_MIXIN)) {
        values = addressBookNode.getProperty(SHARED_PROP).getValues();
      } else {
        addressBookNode.addMixin(SHARED_MIXIN);
        addressBookNode.setProperty("exo:sharedUserId", username);
      }

      List<Value> valueList = new ArrayList<Value>();
      for (String userId : receiveUsers) {
        Node sharedAddress = getSharedAddressBooksHome(userId.replaceFirst(DataStorage.HYPHEN, ""));
        boolean isExist = false;
        for (int i = 0; i < values.length; i++) {
          Value value = values[i];
          String uuid = value.getString();
          Node refNode = sharedAddress.getSession().getNodeByUUID(uuid);
          if (refNode.getPath().equals(sharedAddress.getPath())) {
            isExist = true;
            break;
          }
          valueList.add(value);
        }
        if (!isExist) {
          Value value2add = addressBookNode.getSession()
                                           .getValueFactory()
                                           .createValue(sharedAddress);
          valueList.add(value2add);
        }
      }

      if (valueList.size() > 0) {
        Map<String, Value> newValue = new LinkedHashMap<String, Value>();
        for (Value value : values)
          newValue.put(value.getString(), value);
        for (Value value : valueList)
          newValue.put(value.getString(), value);
        addressBookNode.setProperty(SHARED_PROP, newValue.values()
                                                         .toArray(new Value[newValue.size()]));
      } else {
        try {
          addressBookNode.getProperty(SHARED_PROP);
        } catch (PathNotFoundException e) {
          addressBookNode.setProperty(SHARED_PROP, new Value[] {}); // add to
                                                                    // fix bug
                                                                    // cs-1449
        }
      }
      addressBookNode.save();
      addressBookNode.getSession().save();
  }
  
  /**
   * {@inheritDoc}
   */
  public void shareContact(String username, String[] contactIds, List<String> receiveUsers) throws Exception {
      for(String contactId : contactIds) {
        Node contactNode = getPersonalContactsHome(username).getNode(contactId);
        Value[] values = {};
        if (contactNode.isNodeType(SHARED_MIXIN)) {     
          values = contactNode.getProperty(SHARED_PROP).getValues();
        } else {
          contactNode.addMixin(SHARED_MIXIN);          
          contactNode.setProperty("exo:sharedUserId", username) ;
        }
        List<Value> valueList = new ArrayList<Value>() ;
        for(String user : receiveUsers) {
          Node sharedContact = getSharedContact(user.replaceFirst(DataStorage.HYPHEN, "")) ;
          boolean isExist = false ; 
          for (int i = 0; i < values.length; i++) {
            Value value = values[i];
            String uuid = value.getString();
            Node refNode = sharedContact.getSession().getNodeByUUID(uuid);
            if(refNode.getPath().equals(sharedContact.getPath())) {
              isExist = true ; 
              break ;
            }
            valueList.add(value) ;
          }
          if(!isExist) {
            Value value2add = contactNode.getSession().getValueFactory().createValue(sharedContact);
            valueList.add(value2add) ;
          }
        }
        if(valueList.size() > 0) {
          Map<String, Value> newValue = new LinkedHashMap<String, Value>() ;
          for (Value value : values )
            newValue.put(value.getString(), value) ;
          for (Value value : valueList)
            newValue.put(value.getString(), value) ;
          
          contactNode.setProperty(SHARED_PROP, newValue.values().toArray(new Value[newValue.size()]));
          contactNode.save() ;
          contactNode.getSession().save();
        }   
      }   
  }
  
  /**
   * {@inheritDoc}
   */
  public List<SharedAddressBook> findSharedAddressBooksByUser(String username) throws Exception {
      List<SharedAddressBook> addressBooks = new ArrayList<SharedAddressBook>();
      Node sharedAddress = getSharedAddressBooksHome(username);
      PropertyIterator iter = sharedAddress.getReferences();
      while (iter.hasNext()) {
        try {
          Node addressNode = iter.nextProperty().getParent();
          SharedAddressBook sharedAddressBook = new SharedAddressBook(addressNode.getProperty("exo:name")
                                                                                 .getString(),
                                                                      addressNode.getName(),
                                                                      addressNode.getProperty("exo:sharedUserId")
                                                                                 .getString());
          if (addressNode.hasProperty("exo:editPermissionUsers"))
            sharedAddressBook.setEditPermissionUsers(Utils.valuesToStringArray(addressNode.getProperty("exo:editPermissionUsers")
                                                                                .getValues()));
          if (addressNode.hasProperty("exo:editPermissionGroups"))
            sharedAddressBook.setEditPermissionGroups(Utils.valuesToStringArray(addressNode.getProperty("exo:editPermissionGroups")
                                                                                 .getValues()));
          addressBooks.add(sharedAddressBook);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      return addressBooks;
  }
  
  
  
  /**
   * {@inheritDoc}
   */
  public void removeSharedContact(String username, String addressBookId, String contactId) throws Exception {
    //CS-2389
    if (!haveEditPermissionOnAddressBook(username, addressBookId)){
      System.out.println("\n Do not have edit permission. \n");
      throw new Exception();
    }
    Node sharedAddressBookMock = getSharedAddressBooksHome(username) ;
    PropertyIterator iter2 = sharedAddressBookMock.getReferences() ;
    Node addressBook ;      
    while(iter2.hasNext()) {
      addressBook = iter2.nextProperty().getParent() ;
      
      // need improved
      if(addressBook.getName().equals(addressBookId)) {
        Node contactHomeNode = addressBook.getParent().getParent().getNode(CONTACTS) ;
        try {
          contactHomeNode.getNode(contactId).remove() ;        
          contactHomeNode.getSession().save() ;
          break ;
        } catch (PathNotFoundException e) { }      
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public Contact getSharedContact(String username, String contactId) throws Exception {
    Node sharedContactMock = getSharedContact(username) ;
    PropertyIterator iter = sharedContactMock.getReferences() ;
    while(iter.hasNext()) {
      try{
        Node contactNode = iter.nextProperty().getParent() ;   
        if(contactNode.getName().equals(contactId)) {
          return Utils.getContact(contactNode, DataStorage.SHARED) ;
        }
      }catch(Exception e){
        e.printStackTrace() ;
      }
    }
    return null ;
  }
  
  /**
   * {@inheritDoc}
   */
  public DataPageList getSharedContacts(String username) throws Exception {
    List<Contact> sharedContacts = new ArrayList<Contact>() ;
    Node sharedContact = getSharedContact(username) ;      
    PropertyIterator iter = sharedContact.getReferences() ;
    while(iter.hasNext()) {
      try{
        Node contactNode = iter.nextProperty().getParent() ;
        sharedContacts.add(Utils.getContact(contactNode, SHARED)) ;
      }catch(Exception e){
        e.printStackTrace() ;
      }
    }
    return new DataPageList(sharedContacts, 10, null, false) ;
  }

  /**
   * {@inheritDoc}
   */
  public void saveSharedContact(String username, Contact contact) throws Exception  {
    Node sharedContactMock = getSharedContact(username) ;
    PropertyIterator iter = sharedContactMock.getReferences() ;
    boolean isEdit = false ;
    while(iter.hasNext()) {
      try{
        Node contactNode = iter.nextProperty().getParent() ;
        if(contactNode.getName().equals(contact.getId())) {
//        CS-2389
          if (!haveEditPermissionOnAddressBook(username, contact.getAddressBookIds()[0]) && !haveEditPermissionOnContact(username, contact)){
            System.out.println("\n Do not have edit permission. \n");
            throw new Exception();
          }
          
          isEdit = true ;
          contactToNode(contactNode.getParent(), contact, false) ;
          contactNode.getParent().getSession().save() ;
          return ;
        }
      }catch(Exception e){
        e.printStackTrace() ;
      }
    }
    if (!isEdit) throw new PathNotFoundException() ;
  }

  /**
   * {@inheritDoc}
   */
  public void saveContactToSharedAddressBook(String username, String addressBookId, Contact contact, boolean isNew) throws Exception  {
    Node sharedAddressBookMock = getSharedAddressBooksHome(username) ;
    PropertyIterator iter = sharedAddressBookMock.getReferences() ;
    Node addressBook ;      
    while(iter.hasNext()) {
      addressBook = iter.nextProperty().getParent() ;
      if(addressBook.getName().equals(addressBookId)) {
        //CS-2389
        if (!haveEditPermissionOnAddressBook(username, addressBookId) && !haveEditPermissionOnContact(username, contact)){
          System.out.println("\n Do not have edit permission. \n");
          throw new Exception();
        }
        
        Node contactHomeNode = addressBook.getParent().getParent().getNode(CONTACTS) ;
        contact.setOwner(false) ;
        contactToNode(contactHomeNode, contact, isNew) ;
        contactHomeNode.getSession().save() ;   
        return ;
      }
    }      
  }
  
  /**
   * {@inheritDoc}
   */
  public Contact getSharedContactAddressBook(String username, String contactId) throws Exception {
    Node sharedAddressBookMock = getSharedAddressBooksHome(username) ;
    PropertyIterator iter = sharedAddressBookMock.getReferences() ;
    Node addressBook ;      
    while(iter.hasNext()) {
      addressBook = iter.nextProperty().getParent() ;
      Node contactHomeNode = addressBook.getParent().getParent().getNode(CONTACTS) ;
      try {
        // cs-2073
        Node contactNode = contactHomeNode.getNode(contactId) ;
        if (Arrays.asList(Utils.valuesToStringArray(contactNode.getProperty(PROP_ADDRESSBOOK_REFS).getValues()))
            .contains(addressBook.getProperty("exo:id").getString())) return Utils.getContact(contactNode, DataStorage.SHARED) ;
      } catch (PathNotFoundException e) { }
    }
    return null ;
  }
  
  /**
   * {@inheritDoc}
   */
  public ContactPageList getSharedContactsByAddressBook(String username, SharedAddressBook addressBook) throws Exception {
    if (addressBook == null) return null ;
      Node contactHome = getPersonalContactsHome(addressBook.getSharedUserId()) ;
      StringBuffer queryString = new StringBuffer("/jcr:root" + contactHome.getPath() 
                                                  + "//element(*,exo:contact)[(@exo:categories='").
                                                  append(addressBook.getId()).append("')]")
                                                  .append(" order by @exo:fullName,@exo:id ascending");
      return new ContactPageList(username, 10, queryString.toString(), SHARED) ;
  }

  /**
   * {@inheritDoc}
   */
  public ContactPageList getPublicContactsByAddressBook(String groupId) throws Exception {   
    String usersPath = nodeHierarchyCreator_.getJcrPath(USERS_PATH) ;
    try {
    StringBuffer queryString = new StringBuffer("/jcr:root" + usersPath 
                                                + "//element(*,exo:contact)[@exo:categories='")
                                                .append(groupId).append("' and @exo:isOwner='true'] ")
                                                .append("order by @exo:fullName,@exo:id ascending");
    return new ContactPageList(null, 10, queryString.toString(), PUBLIC) ;
    } finally {
      //sysProvider.close();
    }
  }
  
 
  /**
   * {@inheritDoc}
   */
  public void addUserContactInAddressBook(String userId, String addressBookId) throws Exception {
      Node contactHome = getPersonalContactsHome(userId);
      Node contactNode = contactHome.getNode(userId);
      Value[] values = contactNode.getProperty(PROP_ADDRESSBOOK_REFS).getValues();
      List<String> ls = new ArrayList<String>();
      for (Value vl : values) {
        if (vl.getString().equals(addressBookId))
          return;
        ls.add(vl.getString());
      }
      ls.add(addressBookId);
      contactNode.setProperty(PROP_ADDRESSBOOK_REFS, ls.toArray(new String[] {}));
      contactNode.save();
  }
  
  /**
   * {@inheritDoc}
   */
  public void contactToNode(Node contactsHome, Contact contact, boolean isNew) throws Exception {
    Node contactNode;
    if (isNew) {
      contactNode = contactsHome.addNode(contact.getId(), "exo:contact"); 
      contactNode.setProperty("exo:id", contact.getId());
      if(contact.isOwner()) {
        contactNode.setProperty("exo:isOwner", true) ;
        contactNode.setProperty("exo:ownerId", contact.getOwnerId()) ;
      }
    } else {
      contactNode = contactsHome.getNode(contact.getId());
    }
    contactNode.setProperty("exo:fullName", contact.getFullName());
    contactNode.setProperty("exo:firstName", contact.getFirstName());
    contactNode.setProperty("exo:lastName", contact.getLastName());
    contactNode.setProperty("exo:nickName", contact.getNickName());
    contactNode.setProperty("exo:gender", contact.getGender()) ;
    GregorianCalendar dateTime = new GregorianCalendar() ;
    Date birthday = contact.getBirthday() ;
    if (birthday != null) {
      dateTime.setTime(birthday) ;    
      contactNode.setProperty("exo:birthday", dateTime) ;
    } else try { contactNode.getProperty("exo:birthday").remove() ; } catch (PathNotFoundException e) {}
    
    contactNode.setProperty("exo:jobTitle", contact.getJobTitle());
    
    if (contact.getEmailAddresses() == null || contact.getEmailAddress().length()==0)
      contactNode.setProperty("exo:emailAddress", new String[] {""});
    else
      contactNode.setProperty("exo:emailAddress", contact.getEmailAddresses().toArray(new String[] {}));
    
    contactNode.setProperty("exo:exoId", contact.getExoId());
    contactNode.setProperty("exo:googleId", contact.getGoogleId());
    contactNode.setProperty("exo:msnId", contact.getMsnId());
    contactNode.setProperty("exo:aolId", contact.getAolId());
    contactNode.setProperty("exo:yahooId", contact.getYahooId());
    contactNode.setProperty("exo:icrId", contact.getIcrId());
    contactNode.setProperty("exo:skypeId", contact.getSkypeId());
    contactNode.setProperty("exo:icqId", contact.getIcqId());

    contactNode.setProperty("exo:homeAddress", contact.getHomeAddress());
    contactNode.setProperty("exo:homeCity", contact.getHomeCity());    
    contactNode.setProperty("exo:homeState_province", contact.getHomeState_province());
    contactNode.setProperty("exo:homePostalCode", contact.getHomePostalCode());
    contactNode.setProperty("exo:homeCountry", contact.getHomeCountry());
    contactNode.setProperty("exo:homePhone1", contact.getHomePhone1());
    contactNode.setProperty("exo:homePhone2", contact.getHomePhone2());
    contactNode.setProperty("exo:homeFax", contact.getHomeFax());
    contactNode.setProperty("exo:personalSite", contact.getPersonalSite());
    
    contactNode.setProperty("exo:workAddress", contact.getWorkAddress());
    contactNode.setProperty("exo:workCity", contact.getWorkCity());
    contactNode.setProperty("exo:workState_province", contact.getWorkStateProvince());
    contactNode.setProperty("exo:workPostalCode", contact.getWorkPostalCode());
    contactNode.setProperty("exo:workCountry", contact.getWorkCountry());
    contactNode.setProperty("exo:workPhone1", contact.getWorkPhone1());
    contactNode.setProperty("exo:workPhone2", contact.getWorkPhone2());
    contactNode.setProperty("exo:workFax", contact.getWorkFax());
    contactNode.setProperty("exo:mobilePhone", contact.getMobilePhone());
    contactNode.setProperty("exo:webPage", contact.getWebPage());
    
    contactNode.setProperty("exo:note", contact.getNote());
    contactNode.setProperty(PROP_ADDRESSBOOK_REFS, contact.getAddressBookIds());
    contactNode.setProperty("exo:tags", contact.getTags());
    contactNode.setProperty("exo:editPermissionUsers", contact.getEditPermissionUsers());
    contactNode.setProperty("exo:viewPermissionUsers", contact.getViewPermissionUsers());    
    contactNode.setProperty("exo:editPermissionGroups", contact.getEditPermissionGroups());
    contactNode.setProperty("exo:viewPermissionGroups", contact.getViewPermissionGroups());
    
    if (contact.getLastUpdated() != null) {
      dateTime.setTime(contact.getLastUpdated()) ;
      contactNode.setProperty("exo:lastUpdated", dateTime);
    }
//  save image to contact
    ContactAttachment attachment = contact.getAttachment() ;
    if (attachment != null) {
//    fix load image on IE6 UI
      ExtendedNode extNode = (ExtendedNode)contactNode ;
      if (extNode.canAddMixin("exo:privilegeable")) extNode.addMixin("exo:privilegeable");
      String[] arrayPers = {PermissionType.READ, PermissionType.ADD_NODE, PermissionType.SET_PROPERTY, PermissionType.REMOVE} ;
      extNode.setPermission(SystemIdentity.ANY, arrayPers) ;
      List<AccessControlEntry> permsList = extNode.getACL().getPermissionEntries() ;   
      for(AccessControlEntry accessControlEntry : permsList) {
        extNode.setPermission(accessControlEntry.getIdentity(), arrayPers) ;      
      } 
      
      if (attachment.getFileName() != null) {
        Node nodeFile = null ;
        try {
          nodeFile = contactNode.getNode("image") ;
        } catch (PathNotFoundException ex) {
          nodeFile = contactNode.addNode("image", "nt:file");
        }
        Node nodeContent = null ;
        try {
          nodeContent = nodeFile.getNode("jcr:content") ;
        } catch (PathNotFoundException ex) {
          nodeContent = nodeFile.addNode("jcr:content", "nt:resource") ;
        }
        nodeContent.setProperty("jcr:mimeType", attachment.getMimeType()) ;
        nodeContent.setProperty("jcr:data", attachment.getInputStream());
        nodeContent.setProperty("jcr:lastModified", Calendar.getInstance().getTimeInMillis());
      }
    }else {
      if(contactNode.hasNode("image")) contactNode.getNode("image").remove() ;
    }    
  }
  
  /**
   * {@inheritDoc}
   */
  public Tag getTag(Node tagNode) throws Exception {
    Tag tag = new Tag();
    if (tagNode.hasProperty("exo:id"))
      tag.setId(tagNode.getProperty("exo:id").getString());
    if (tagNode.hasProperty("exo:name"))
      tag.setName(tagNode.getProperty("exo:name").getString());
    if (tagNode.hasProperty("exo:description"))
      tag.setDescription(tagNode.getProperty("exo:description").getString());
    if (tagNode.hasProperty("exo:color"))
      tag.setColor(tagNode.getProperty("exo:color").getString());
    return tag;
  }
  
  /**
   * {@inheritDoc}
   */
  public void updateTag(String username,Tag tag) throws Exception {
    Node tagHome = getTagsHome(username) ;
    Node tagNode = tagHome.getNode(tag.getId());
    tagNode.setProperty("exo:name", tag.getName());
    tagNode.setProperty("exo:description", tag.getDescription());
    tagNode.setProperty("exo:color", tag.getColor());
    tagHome.save();
  }
  
  
  /**
   * {@inheritDoc}
   */
  public Tag getTag(String username, String tagId) throws Exception {
      Node tagHomeNode = getTagsHome(username);
      if (tagHomeNode.hasNode(tagId)) 
        return getTag(tagHomeNode.getNode(tagId));
      return null ;
  }
  
  /**
   * {@inheritDoc}
   */
  public List<Tag> getTags(String username) throws Exception {
    Node tagHomeNode = getTagsHome(username);
    List<Tag> tags = new ArrayList<Tag>();
    NodeIterator iter = tagHomeNode.getNodes();
    while (iter.hasNext()) {
      Node tagNode = iter.nextNode();
      tags.add(getTag(tagNode));
    }
    return tags;
  }

  /**
   * {@inheritDoc}
   */
  public DataPageList getContactPageListByTag(String username, String tagId) throws Exception {
      Map<String, Contact> contacts = new LinkedHashMap<String, Contact>() ;
      QueryResult result = null ;
      NodeIterator it = null ;
      Query query = null ;
      QueryManager qm = null;
      StringBuffer queryString = null ;
      
      //query on public contacts
      Node contactHome = getPersonalContactsHome(username);
      qm = contactHome.getSession().getWorkspace().getQueryManager();
      queryString = new StringBuffer("/jcr:root" + contactHome.getPath() 
                                                  + "//element(*,exo:contact)[@exo:tags='").
                                                  append(tagId).
                                                  append("']");
      query = qm.createQuery(queryString.toString(), Query.XPATH);
      result = query.execute();
      it = result.getNodes();    
      
      while (it.hasNext()) {
        Contact contact = Utils.getContact(it.nextNode(), PERSONAL) ;
        contacts.put(contact.getId(), contact);
      }
      
      //query on public contacts
      String usersPath = nodeHierarchyCreator_.getJcrPath(USERS_PATH) ;      
      Node publicContactHome = getPublicContactsHome();
      qm = publicContactHome.getSession().getWorkspace().getQueryManager();
      queryString = new StringBuffer("/jcr:root" + usersPath 
                                                  + "//element(*,exo:contact)[@exo:tags='")
                                                  .append(tagId).append("' and @exo:isOwner='true'] ") ;
      query = qm.createQuery(queryString.toString(), Query.XPATH);
      result = query.execute();
      it = result.getNodes();    
      while (it.hasNext()) {
        Contact contact = Utils.getContact(it.nextNode(), PUBLIC);
        if (contact.getId().equals(username)) contact.setContactType(PERSONAL) ;
        contacts.put(contact.getId(), contact);
      }
      
      // query on shared contacts
      try {
        Node sharedContact = getSharedContact(username) ;      
        PropertyIterator iter = sharedContact.getReferences() ;
        while(iter.hasNext()) {
          try{
            Node contactNode = iter.nextProperty().getParent() ;
            if (contactNode.hasProperty("exo:tags") && 
                Arrays.asList(Utils.valuesToStringArray(contactNode.getProperty("exo:tags").getValues())).contains(tagId)) {
              Contact contact = Utils.getContact(contactNode, SHARED) ;
              contacts.put(contact.getId(), contact) ;
            }
          }catch(Exception e){
            e.printStackTrace() ;
          }
        }
      } catch (PathNotFoundException e) { }
      //Query shared contacts
      Node sharedAddressBookMock = getSharedAddressBooksHome(username) ;
      PropertyIterator iter = sharedAddressBookMock.getReferences() ;
      Node addressBook ;      
      while(iter.hasNext()) {
        addressBook = iter.nextProperty().getParent() ;  
        Node contactHomeNode = addressBook.getParent().getParent().getNode(CONTACTS) ;
        queryString = new StringBuffer("/jcr:root" + contactHomeNode.getPath() 
            + "//element(*,exo:contact)[@exo:tags='").
            append(tagId).
            append("']");
        
        // CS-3644
        //qm = contactHomeNode.getSession().getWorkspace().getQueryManager();
        qm = sharedAddressBookMock.getSession().getWorkspace().getQueryManager();
        query = qm.createQuery(queryString.toString(), Query.XPATH);
        result = query.execute();
        it = result.getNodes();    
        while (it.hasNext()) {
          Contact contact = Utils.getContact(it.nextNode(), SHARED);
          if (Arrays.asList(contact.getAddressBookIds()).contains(addressBook.getProperty("exo:id").getString()))
            contacts.put(contact.getId(), contact) ;
        }
      }
      return new DataPageList(Arrays.asList(contacts.values().toArray(new Contact[] {})), 10, null, false) ;
  }
  
  /**
   * {@inheritDoc}
   */
  public void addTag(String username, List<String> contactIds, String tagId) throws Exception {
    Map<String, String> tagMap = new HashMap<String, String> () ;
    tagMap.put(tagId, tagId) ;
      for(String contact : contactIds) {  
        Node contactNode = null ;
        String contactId = contact.split(SPLIT)[0] ;
        String contactType = contact.split(SPLIT)[1] ;
        if (contactType.equals(PERSONAL)) {
          contactNode = getPersonalContactsHome(username).getNode(contactId) ;
        } else if (contactType.equals(PUBLIC)) {
          contactNode = getPersonalContactsHome(contactId).getNode(contactId);
        } else {
          Node sharedContactMock = getSharedContact(username) ;      
          PropertyIterator iter = sharedContactMock.getReferences() ;
          while(iter.hasNext()) {
            try{
              Node node = iter.nextProperty().getParent() ;
              if(node.getName().equals(contactId)) {
                contactNode = node ;
                break ;
              }
            }catch(Exception exx){
              exx.printStackTrace() ;
            }
          }
          if (contactNode == null) {
            Node sharedAddressBookMock = getSharedAddressBooksHome(username) ;
            PropertyIterator iter1 = sharedAddressBookMock.getReferences() ;
            Node addressBook ;      
            while(iter1.hasNext()) {
              addressBook = iter1.nextProperty().getParent() ;
              Node contacts = addressBook.getParent().getParent().getNode(CONTACTS) ;
              // loop all shared address books; faster if parameter is : List<contact>
              if(contacts.hasNode(contactId)) {
                contactNode = contacts.getNode(contactId) ;
                if (Arrays.asList(Utils.valuesToStringArray(contactNode.getProperty(PROP_ADDRESSBOOK_REFS).getValues()))
                    .contains(addressBook.getProperty("exo:id").getString())) break ;
                else {
                  contactNode = null ;
                }
              }
            }
          }
        }
        if (contactNode == null) {
          throw new PathNotFoundException() ;
        } else {
          Map<String, String> thisTagMap = new HashMap<String, String> () ;
          thisTagMap = tagMap ;
          Value[] values = null ;
          if(contactNode.hasProperty("exo:tags")){
            values = contactNode.getProperty("exo:tags").getValues() ;
            for(Value value : values) thisTagMap.put(value.getString(), value.getString()) ;         
          }
          contactNode.setProperty("exo:tags", thisTagMap.values().toArray(new String[]{})) ;
          contactNode.save() ;
        }
      }
  }

  /**
   * {@inheritDoc}
   */
  public void addTag(String username, List<String> contactIds, List<Tag> tags) throws Exception {
    Node tagHomeNode = getTagsHome(username);
    Map<String, String> tagMap = new HashMap<String, String> () ;
    String newTag = null ;
      for(Tag tag : tags) {
        if(!tagHomeNode.hasNode(tag.getId())) {
          newTag = tag.getId() ;
          Node tagNode = tagHomeNode.addNode(tag.getId(), "exo:contactTag") ;
          tagNode.setProperty("exo:id", tag.getId());
          tagNode.setProperty("exo:name", tag.getName());
          tagNode.setProperty("exo:description", tag.getDescription());
          tagNode.setProperty("exo:color", tag.getColor());
          tagHomeNode.getSession().save() ;
        }
        tagMap.put(tag.getId(), tag.getId()) ;
      }
      if (contactIds == null || contactIds.size() == 0) return ;
      for(String contact : contactIds) {
        Node contactNode = null ;
        String contactId = contact.split(SPLIT)[0] ;
        String contactType = contact.split(SPLIT)[1] ;
        if (contactType.equals(PERSONAL)) {
          contactNode = getPersonalContactsHome(username).getNode(contactId) ;
        } else if (contactType.equals(PUBLIC)) {
          
          contactNode = getPersonalContactsHome(contactId).getNode(contactId);
        } else {
          Node sharedContactMock = getSharedContact(username) ;      
          PropertyIterator iter = sharedContactMock.getReferences() ;
          while(iter.hasNext()) {
            try{
              Node node = iter.nextProperty().getParent() ;
              if(node.getName().equals(contactId)) {
                contactNode = node ;
                break ;
              }
            }catch(Exception exx){
              exx.printStackTrace() ;
            }
          }
          if (contactNode == null) {
            Node sharedAddressBookMock = getSharedAddressBooksHome(username) ;
            PropertyIterator iter1 = sharedAddressBookMock.getReferences() ;
            Node addressBook ;      
            while(iter1.hasNext()) {
              addressBook = iter1.nextProperty().getParent() ;
              Node contacts = addressBook.getParent().getParent().getNode(CONTACTS) ;
              // loop all shared address books; faster if parameter is : List<contact>
              //cs-1962            
              if(contacts.hasNode(contactId)) {
                contactNode = contacts.getNode(contactId) ;
                if (Arrays.asList(Utils.valuesToStringArray(contactNode.getProperty(PROP_ADDRESSBOOK_REFS).getValues()))
                    .contains(addressBook.getProperty("exo:id").getString())) break ;
                else {
                  contactNode = null ;      
                }
              }
            }
          }
        }
        if (contactNode == null) {
          if (contactIds.get(0).equals(contact) && (newTag != null)) {
            tagHomeNode.getNode(newTag).remove() ;
            tagHomeNode.getSession().save() ;
          }
          throw new PathNotFoundException() ;
        } else {
          Map<String, String> thisTagMap = new HashMap<String, String>() ;
          thisTagMap.putAll(tagMap) ;
          Value[] values = null ;
          if(contactNode.hasProperty("exo:tags")){
            values = contactNode.getProperty("exo:tags").getValues() ;
            for(Value value : values) thisTagMap.put(value.getString(), value.getString()) ;         
          }
          contactNode.setProperty("exo:tags", thisTagMap.values().toArray(new String[]{})) ;   
          contactNode.save() ;
        }
      }
  }

  /**
   * {@inheritDoc}
   */
  public void removeTagInContacts (NodeIterator it, String tagId) throws Exception {
    while (it.hasNext()) {
      Node contactNode = it.nextNode() ;
      if (contactNode.hasProperty("exo:tags")) {
        String[] tagIds = Utils.valuesToStringArray(contactNode.getProperty("exo:tags").getValues()) ;
        List<String> newTagIds = new ArrayList<String>() ;
        for (String id : tagIds) 
          if (!id.equals(tagId)) newTagIds.add(id) ;
        contactNode.setProperty("exo:tags", newTagIds.toArray(new String[]{})) ;
        contactNode.save() ;
      }
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public Tag removeTag(String username, String tagId) throws Exception {
      Node tagHomeNode = getTagsHome(username);
      Node tagNode = tagHomeNode.getNode(tagId) ;
      Tag tag = getTag(tagNode) ;
      tagNode.remove();
      tagHomeNode.save() ;
      
      // remove tagId in contact property       
      // query on public contacts
        Node contactHome = getPersonalContactsHome(username);
        QueryManager qm = contactHome.getSession().getWorkspace().getQueryManager();
        StringBuffer queryString = new StringBuffer("/jcr:root" + contactHome.getPath() 
                                                    + "//element(*,exo:contact)[@exo:tags='").
                                                    append(tagId).append("']");
        Query query = qm.createQuery(queryString.toString(), Query.XPATH);
        removeTagInContacts(query.execute().getNodes(), tagId) ;
        
        //query on public contacts
        String usersPath = nodeHierarchyCreator_.getJcrPath(USERS_PATH) ;
        try {
        Node publicContactHome = getPublicContactsHome();
        qm = publicContactHome.getSession().getWorkspace().getQueryManager();
        queryString = new StringBuffer("/jcr:root" + usersPath 
                                                    + "//element(*,exo:contact)[@exo:tags='")
                                                    .append(tagId).append("' and @exo:isOwner='true'] ") ;
        query = qm.createQuery(queryString.toString(), Query.XPATH);
        removeTagInContacts(query.execute().getNodes(), tagId) ;
        } finally {
          //sysProvider.close();
        }
        
        // query on shared contacts
        try {
          Node sharedContact = getSharedContact(username) ;      
          PropertyIterator iter = sharedContact.getReferences() ;
          while(iter.hasNext()) {
            try{
              Node contactNode = iter.nextProperty().getParent() ;
              if (contactNode.hasProperty("exo:tags")) {
                String[] tagIds = Utils.valuesToStringArray(contactNode.getProperty("exo:tags").getValues()) ;
                List<String> newTagIds = new ArrayList<String>() ;
                for (String id : tagIds) 
                  if (!id.equals(tagId)) newTagIds.add(id) ;
                contactNode.setProperty("exo:tags", newTagIds.toArray(new String[]{})) ;
                contactNode.save() ;
              }
            }catch(Exception e){
              e.printStackTrace() ;
            }
          }
        } catch (PathNotFoundException e) { }
  
        Node sharedAddressBookMock = getSharedAddressBooksHome(username) ;
        PropertyIterator iter = sharedAddressBookMock.getReferences() ;
        qm = sharedAddressBookMock.getSession().getWorkspace().getQueryManager();
        Node addressBook ;      
        while(iter.hasNext()) {
          addressBook = iter.nextProperty().getParent() ;
          Node contactHomeNode = addressBook.getParent().getParent().getNode(CONTACTS) ;
          queryString = new StringBuffer("/jcr:root" + contactHomeNode.getPath() 
              + "//element(*,exo:contact)[@exo:tags='").
              append(tagId).
              append("']");
          query = qm.createQuery(queryString.toString(), Query.XPATH);
          removeTagInContacts(query.execute().getNodes(), tagId) ;
        }
      return tag ; 
  }
  
  /**
   * {@inheritDoc}
   */
  public void removeContactTag(String username, List<String> contactIds, List<String> tags) throws Exception { 
      for(String contact : contactIds) {
        Node contactNode = null ;
        String contactId = contact.split(SPLIT)[0] ;
        String contactType = contact.split(SPLIT)[1] ;
        if (contactType.equals(PERSONAL)) {
          contactNode = getPersonalContactsHome(username).getNode(contactId) ;
        } else if (contactType.equals(PUBLIC)) {
          
          contactNode = getPersonalContactsHome(contactId).getNode(contactId);
        } else {
          Node sharedContactMock = getSharedContact(username) ;      
          PropertyIterator iter = sharedContactMock.getReferences() ;
          while(iter.hasNext()) {
            try {
              Node node = iter.nextProperty().getParent() ;
              if(node.getName().equals(contactId)) {
                contactNode = node ;
                break ;
              }
            }catch(Exception exx){
              exx.printStackTrace() ;
            }
          }
          if (contactNode == null) {
            Node sharedAddressBookMock = getSharedAddressBooksHome(username) ;
            PropertyIterator iter1 = sharedAddressBookMock.getReferences() ;
            Node addressBook ;      
            while(iter1.hasNext()) {
              addressBook = iter1.nextProperty().getParent() ;
              Node contacts = addressBook.getParent().getParent().getNode(CONTACTS) ;
              // loop all shared address books; faster if parameter is : List<contact>
              if(contacts.hasNode(contactId)) {
                contactNode = contacts.getNode(contactId) ;
                if (Arrays.asList(Utils.valuesToStringArray(contactNode.getProperty(PROP_ADDRESSBOOK_REFS).getValues()))
                    .contains(addressBook.getProperty("exo:id").getString())) break; 
                else {
                  contactNode = null ;
                }
              }
            }
          }
        }
        
        if (contactNode == null) {
          throw new PathNotFoundException() ;
        } else {
          if(contactNode.hasProperty("exo:tags")){
            Value[] values = contactNode.getProperty("exo:tags").getValues() ;
            List<String> tagList = new ArrayList<String>() ;
            for(Value value : values) { tagList.add(value.getString()) ; }
            for(String tag : tags) {
              if(tagList.contains(tag)) tagList.remove(tag) ;
            }
            contactNode.setProperty("exo:tags", tagList.toArray(new String[]{})) ;
            contactNode.save() ;
          }
        }
      }
  }
 
  /**
   * {@inheritDoc}
   */
  public DataPageList searchContact(String username, ContactFilter filter)throws Exception {
    Map<String, Contact> contacts = new LinkedHashMap<String, Contact>() ;
    filter.setUsername(username) ;
      QueryManager qm = getSession(createSystemProvider()).getWorkspace().getQueryManager() ;
      Query query = null ;
      String usersPath = nodeHierarchyCreator_.getJcrPath(USERS_PATH) ;
      //public contacts
      if (filter.getType() == null || filter.getType().equals(PUBLIC)) {
        //Node publicContactHome = getPublicContactsHome(sysProvider) ; 
        usersPath = nodeHierarchyCreator_.getJcrPath(USERS_PATH) ;
        filter.setAccountPath(usersPath) ;
        // minus shared contacts
        filter.setOwner("true") ; 
        query = qm.createQuery(filter.getStatement(), Query.XPATH) ;
        NodeIterator itpublic = query.execute().getNodes();
        while(itpublic.hasNext()) {
          Contact contact = Utils.getContact(itpublic.nextNode(), PUBLIC) ;
          contacts.put(contact.getId(), contact) ;
        }
      }
    filter.setOwner(null) ;
    if (filter.getType() == null || filter.getType().equals(PERSONAL)) {
      // public contacts
      if(username != null && username.length() > 0) {
        Node contactHome = getPersonalContactsHome(username) ;
        filter.setAccountPath(contactHome.getPath()) ;      
        qm = contactHome.getSession().getWorkspace().getQueryManager() ;
        query = qm.createQuery(filter.getStatement(), Query.XPATH) ;      
        NodeIterator it = query.execute().getNodes() ; 
        while(it.hasNext()) {
          Contact contact = Utils.getContact(it.nextNode(), PERSONAL) ;
          contacts.put(contact.getId(), contact) ; 
        }
      }
    }

    if (filter.getType() == null || filter.getType().equals(SHARED)) {
    //share contacts
      try {
        Node sharedContact = getSharedContact(username) ;      
        PropertyIterator iter = sharedContact.getReferences() ;
        List<Contact> sharedContacts = getSharedContacts(username).getAll() ;
        List<String> sharedContactIds = new ArrayList<String>() ;
        for (Contact contact : sharedContacts) sharedContactIds.add(contact.getId()) ;      
        while(iter.hasNext()) {
          try{
            Node sharedContactHomeNode = iter.nextProperty().getParent().getParent() ;
            filter.setAccountPath(sharedContactHomeNode.getPath()) ;
            
            // add
            String split = "/" ;
            String temp = sharedContactHomeNode.getPath().split(usersPath)[1] ;
            String userId = temp.split(split)[1] ;
            filter.setUsername(userId) ;
            //qm = getSession(sysProvider).getWorkspace().getQueryManager() ;      
            query = qm.createQuery(filter.getStatement(), Query.XPATH) ;
            NodeIterator it = query.execute().getNodes() ;
            while(it.hasNext()) {
              Node contactNode = it.nextNode() ;
              if (sharedContactIds.contains(contactNode.getProperty("exo:id").getString())) {
                Contact contact = Utils.getContact(contactNode, SHARED) ;
                
                // add
                if (!contacts.containsKey(contact.getId())) {
                  contacts.put(contact.getId(), contact) ;
                } else {
                  contacts.get(contact.getId()).setContactType(DataStorage.SHARED) ;
                }
                sharedContactIds.remove(contact.getId()) ;
              } 
            }
          }catch(Exception e){
            e.printStackTrace() ;
          }
        }
      } catch (PathNotFoundException e) { }
      
      if (filter.isSearchSharedContacts() == false) {
        Node sharedAddressBookMock = getSharedAddressBooksHome(username) ;
        PropertyIterator iter = sharedAddressBookMock.getReferences() ;
        Node addressBook ;      
        boolean searchByAddress = false ;
        if (filter.getCategories() != null && filter.getCategories().length >0) searchByAddress = true ; 
        while(iter.hasNext()) {
          addressBook = iter.nextProperty().getParent() ;
          Node contactHomeNode = addressBook.getParent().getParent().getNode(CONTACTS) ;
          filter.setAccountPath(contactHomeNode.getPath()) ;
          
          // avoid getAllContacts from contactHomeNode of another user. 
          if (!searchByAddress) filter.setCategories(new String[] {addressBook.getName()}) ;
          filter.setUsername(addressBook.getProperty("exo:sharedUserId").getString()) ;
          //qm = getSession(sysProvider).getWorkspace().getQueryManager();
          qm = contactHomeNode.getSession().getWorkspace().getQueryManager() ;
          query = qm.createQuery(filter.getStatement(), Query.XPATH) ;  
          NodeIterator it = query.execute().getNodes() ;
          while(it.hasNext()) {
            Contact contact = Utils.getContact(it.nextNode(), SHARED) ;
            contacts.put(contact.getId(), contact) ;
          }
        }
      }
    }
    
    List<Contact> contactList = new ArrayList<Contact>() ;
    String tagId = "";
    
    //get contact by tag
    if(filter.getTag() != null){
      if(filter.getTag().length > 0){
        tagId = filter.getTag()[0];
     }
      if(!Utils.isEmpty(tagId)){
        DataPageList dpl = getContactPageListByTag(username, tagId);
        contactList = dpl.getAll();
     }
    }

    contactList.addAll(contacts.values()) ;
    
    return new DataPageList(contactList, 10, null, false) ;
  }
  
  /**
   * {@inheritDoc}
   */
  public Map<String, String> findEmailsByFilter(String username, ContactFilter filter)throws Exception {
    Map<String, String> emails = new LinkedHashMap<String, String>() ;
    filter.setUsername(username) ;
    filter.setHasEmails(true);
      QueryManager  qm = getContactUserDataHome(username).getSession().getWorkspace().getQueryManager() ;
      Query query ;
      String usersPath = nodeHierarchyCreator_.getJcrPath(USERS_PATH) ;
//      TODO query public contacts
      if (filter.getType() == null ||  filter.getType().equals(PUBLIC)) {
        filter.setAccountPath(usersPath) ;
        // TODO minus shared contacts
        filter.setOwner("true") ; 
       
        query = qm.createQuery(filter.getStatement(), Query.XPATH) ;
        NodeIterator itpublic = query.execute().getNodes();
        while(itpublic.hasNext()) {
          Node contactNode = itpublic.nextNode() ;
          if (filter.getLimit() > 0 && filter.getLimit() <= emails.size()) break;
          feedEmailResult(emails, contactNode);
        }
        filter.setOwner(null) ;
      }
//      TODO query personal contacts
      if (filter.getType() == null ||  filter.getType().equals(PERSONAL)) {
        if(username != null && username.length() > 0) {
          Node contactHome = getPersonalContactsHome(username) ;
          filter.setAccountPath(contactHome.getPath()) ;      
          qm = contactHome.getSession().getWorkspace().getQueryManager() ;
          query = qm.createQuery(filter.getStatement(), Query.XPATH) ;
          NodeIterator it = query.execute().getNodes() ;
          while(it.hasNext()) {
            Node contactNode = it.nextNode() ;
            if (filter.getLimit() > 0 && filter.getLimit() <= emails.size()) break;
            feedEmailResult(emails, contactNode);        
          }
        }
      }
      
      // TODO query shared contacts
      if (filter.getType() == null ||  filter.getType().equals(SHARED)) {
        try {
          Node sharedContact = getSharedContact(username) ;      
          PropertyIterator iter = sharedContact.getReferences() ;
          while(iter.hasNext()) {
            try{
              Node sharedContactHomeNode = iter.nextProperty().getParent().getParent() ;
              filter.setAccountPath(sharedContactHomeNode.getPath()) ;
    
              String split = "/" ;
              String temp = sharedContactHomeNode.getPath().split(usersPath)[1] ;
              String userId = temp.split(split)[1] ;
              filter.setUsername(userId) ;
              
              qm = sharedContactHomeNode.getSession().getWorkspace().getQueryManager() ;      
              query = qm.createQuery(filter.getStatement(), Query.XPATH) ;
              NodeIterator it = query.execute().getNodes() ;
              while(it.hasNext()) {
                Node contactNode = it.nextNode() ;
                if (filter.getLimit() > 0 && filter.getLimit() <= emails.size()) break;
                feedEmailResult(emails, contactNode);  
              }
            }catch(Exception e){
              e.printStackTrace() ;
            }
          }
        } catch (PathNotFoundException e) { }
        
        if (!filter.isSearchSharedContacts()) { 
          Node sharedAddressBookMock = getSharedAddressBooksHome(username) ;
          PropertyIterator iter = sharedAddressBookMock.getReferences() ;
          Node addressBook ;
          
      //  TODO add if to fix bug 1407
          boolean hasGroup = (filter.getCategories() != null && filter.getCategories().length > 0) ; 
          while(iter.hasNext()) {
            addressBook = iter.nextProperty().getParent() ;
            Node contactHomeNode = addressBook.getParent().getParent().getNode(CONTACTS) ;
            filter.setAccountPath(contactHomeNode.getPath()) ;
            if (!hasGroup) filter.setCategories(new String[] {addressBook.getName()}) ;
            filter.setUsername(addressBook.getProperty("exo:sharedUserId").getString()) ;
            //qm = getSession(sysProvider).getWorkspace().getQueryManager();
            qm = contactHomeNode.getSession().getWorkspace().getQueryManager() ;      
            query = qm.createQuery(filter.getStatement(), Query.XPATH) ;
            NodeIterator it = query.execute().getNodes() ;
            while(it.hasNext()) {
              Node contactNode = it.nextNode() ;
              if (filter.getLimit() > 0 && filter.getLimit() <= emails.size()) break;
              feedEmailResult(emails, contactNode); 
            }
          }
        }     
      }
      return emails ;
  }

  /**
   * {@inheritDoc}
   */
  public void feedEmailResult(Map<String, String> emails, Node contactNode) throws Exception {
    String id = contactNode.getProperty("exo:id").getString();
    String fullName = contactNode.getProperty("exo:fullName").getString() ;
    String emailAddresses = Utils.valuesToString(contactNode.getProperty("exo:emailAddress").getValues());
    emails.put(id, fullName + Utils.SPLIT + emailAddresses) ;
  }
  
  // created by Duy Tu <tu.duy@exoplatform.com>
  public List<String> searchEmailsByFilter(String username, ContactFilter filter)throws Exception {
  	Map<String, String> emails = new LinkedHashMap<String, String>() ;
  	filter.setUsername(username) ;
  	filter.setHasEmails(true);
  	QueryManager  qm = getContactUserDataHome(username).getSession().getWorkspace().getQueryManager() ;
  	Query query ;
  	String usersPath = nodeHierarchyCreator_.getJcrPath(USERS_PATH) ;
//      query public contacts
  	if (filter.getType() == null ||  filter.getType().equals(PUBLIC)) {
  		filter.setAccountPath(usersPath) ;
  		// minus shared contacts
  		filter.setOwner("true") ; 
  		
  		query = qm.createQuery(filter.getStatement(), Query.XPATH) ;
  		NodeIterator itpublic = query.execute().getNodes();
  		while(itpublic.hasNext()) {
  			Node contactNode = itpublic.nextNode() ;
  			if (filter.getLimit() > 0 && filter.getLimit() <= emails.size()){
  				return new ArrayList<String>(emails.values());
  			}
  			calculateEmailResult(contactNode, filter, emails);
  		}
  		filter.setOwner(null) ;
  	}
  	
//   query personal contacts
  	if (filter.getType() == null ||  filter.getType().equals(PERSONAL)) {
  		if(username != null && username.length() > 0) {
  			Node contactHome = getPersonalContactsHome(username) ;
  			filter.setAccountPath(contactHome.getPath()) ;      
  			qm = contactHome.getSession().getWorkspace().getQueryManager() ;
  			query = qm.createQuery(filter.getStatement(), Query.XPATH) ;
  			NodeIterator it = query.execute().getNodes() ;
  			while(it.hasNext()) {
  				Node contactNode = it.nextNode() ;
  				if (filter.getLimit() > 0 && filter.getLimit() <= emails.size()) {
  					return new ArrayList<String>(emails.values());
  				}
  				calculateEmailResult(contactNode, filter, emails);    
  			}
  		}
  	}
  	// query shared contacts
  	if (filter.getType() == null ||  filter.getType().equals(SHARED)) {
  		try {
  			Node sharedContact = getSharedContact(username) ;      
  			PropertyIterator iter = sharedContact.getReferences() ;
  			while(iter.hasNext()) {
  				try{
  					Node sharedContactHomeNode = iter.nextProperty().getParent().getParent() ;
  					filter.setAccountPath(sharedContactHomeNode.getPath()) ;
  					
  					String split = "/" ;
  					String temp = sharedContactHomeNode.getPath().split(usersPath)[1] ;
  					String userId = temp.split(split)[1] ;
  					filter.setUsername(userId) ;
  					
  					qm = sharedContactHomeNode.getSession().getWorkspace().getQueryManager() ;      
  					query = qm.createQuery(filter.getStatement(), Query.XPATH) ;
  					NodeIterator it = query.execute().getNodes() ;
  					while(it.hasNext()) {
  						Node contactNode = it.nextNode() ;
  						if (filter.getLimit() > 0 && filter.getLimit() <= emails.size()) {
  							return new ArrayList<String>(emails.values());
  						}
  						calculateEmailResult(contactNode, filter, emails); 
  					}
  				}catch(Exception e){
  					e.printStackTrace() ;
  				}
  			}
  		} catch (PathNotFoundException e) { }
  		
  		if (!filter.isSearchSharedContacts()) { 
  			Node sharedAddressBookMock = getSharedAddressBooksHome(username) ;
  			PropertyIterator iter = sharedAddressBookMock.getReferences() ;
  			Node addressBook ;
  			
  			// add if to fix bug 1407
  			boolean hasGroup = (filter.getCategories() != null && filter.getCategories().length > 0) ; 
  			while(iter.hasNext()) {
  				addressBook = iter.nextProperty().getParent() ;
  				Node contactHomeNode = addressBook.getParent().getParent().getNode(CONTACTS) ;
  				filter.setAccountPath(contactHomeNode.getPath()) ;
  				if (!hasGroup) filter.setCategories(new String[] {addressBook.getName()}) ;
  				filter.setUsername(addressBook.getProperty("exo:sharedUserId").getString()) ;
  				qm = contactHomeNode.getSession().getWorkspace().getQueryManager() ;      
  				query = qm.createQuery(filter.getStatement(), Query.XPATH) ;
  				NodeIterator it = query.execute().getNodes() ;
  				while(it.hasNext()) {
  					Node contactNode = it.nextNode() ;
  					if (filter.getLimit() > 0 && filter.getLimit() <= emails.size()) {
  						return new ArrayList<String>(emails.values());
  					}
  					calculateEmailResult(contactNode, filter, emails);
  				}
  			}
  		}     
  	}
  	return new ArrayList<String>(emails.values());
  }
// created by Duy Tu
  private void calculateEmailResult(Node contactNode, ContactFilter filter, Map<String, String> emails) throws Exception {
  	String emailAddresses = "";
  	String fullName = contactNode.getProperty("exo:fullName").getString() ;
    try {
    	Value [] values = contactNode.getProperty("exo:emailAddress").getValues();
    	int i = 0;
    	String email, classCss;
      for (Value value : values) {
      	email = value.getString().trim();
      	if(email.length() > 0) {
      		if(emails.containsKey(fullName + email)) continue;
      		if(i > 2 || filter.getLimit() <= emails.size()) break;
      		classCss = (emails.isEmpty())?"<div class='AutoCompleteItem AutoCompleteOver'>":"<div class='AutoCompleteItem'>";
      		emailAddresses = 
      			classCss + 
      				StringUtils.replace(fullName, filter.getNickName(), "<b>" + filter.getNickName() + "</b>") + "&lt;" +
      				StringUtils.replace(email, filter.getNickName(), "<b>" + filter.getNickName() + "</b>") + "&gt;" +
      			"</div>";
      		emails.put(fullName + email, emailAddresses) ;
      		++i;
      	}
      }
    } catch (Exception e) {}
  }
  
  /**
   * {@inheritDoc}
   */
  public void copyNodes(String username,Node srcHomeNode, NodeIterator iter, String destAddress, String destType ) throws Exception {
    if (destType.equals(PERSONAL)) {        
      Node contactHomeNode = getPersonalContactsHome(username);
      while (iter.hasNext()) {
        Node oldNode = iter.nextNode() ;
        String newId = "Contact" + IdGenerator.generate() ;
        try {
          contactHomeNode.getSession().getWorkspace().copy(oldNode.getPath(), contactHomeNode.getPath() + "/" + newId) ;        
        } catch (AccessDeniedException ex) {
          Node userContactHome = getPersonalContactsHome(oldNode.getProperty("exo:id").getString()) ;
          userContactHome.getSession().getWorkspace().copy(oldNode.getPath(), contactHomeNode.getPath() + "/" + newId) ; 
        }        
        ExtendedNode extNode ;
        try{
          extNode = (ExtendedNode)contactHomeNode.getNode(newId) ;
        }catch (Exception e) {          
          extNode = (ExtendedNode)getPersonalContactsHome(username).getNode(newId) ;
        }
        if (extNode.canAddMixin("exo:privilegeable")) extNode.addMixin("exo:privilegeable");
        String[] arrayPers = {PermissionType.READ, PermissionType.ADD_NODE, PermissionType.SET_PROPERTY, PermissionType.REMOVE} ;
        extNode.setPermission(username, arrayPers) ; 
        extNode.save() ;
        
        Node newNode = contactHomeNode.getNode(newId) ;
        newNode.setProperty(PROP_ADDRESSBOOK_REFS, new String [] {destAddress}) ;
        newNode.setProperty("exo:id", newId) ;          
        newNode.setProperty("exo:isOwner", false) ;

        newNode.setProperty("exo:editPermissionUsers", new String[] {});
        newNode.setProperty("exo:viewPermissionUsers", new String[] {});    
        newNode.setProperty("exo:editPermissionGroups", new String[] {});
        newNode.setProperty("exo:viewPermissionGroups", new String[] {});
      }
      contactHomeNode.getSession().save() ;
    } else if (destType.equals(SHARED)) {
      Node sharedAddressBookMock = getSharedAddressBooksHome(username) ;
      PropertyIterator proIter = sharedAddressBookMock.getReferences() ;
      Node addressBook ;
      while(proIter.hasNext()) {
        addressBook = proIter.nextProperty().getParent() ;
        if(addressBook.getName().equals(destAddress)) {
          Node contactHomeNode = addressBook.getParent().getParent().getNode(CONTACTS) ;          
          while (iter.hasNext()) {
            String newId = "Contact" + IdGenerator.generate() ;
            Node oldNode = iter.nextNode() ;
            contactHomeNode.getSession().getWorkspace().copy(srcHomeNode.getPath() + "/"
                + oldNode.getProperty("exo:id").getString(), contactHomeNode.getPath() + "/" + newId) ;            
            Node newNode = contactHomeNode.getNode(newId) ;
            newNode.setProperty(PROP_ADDRESSBOOK_REFS, new String [] {destAddress}) ;  
            newNode.setProperty("exo:id", newId) ;
            newNode.setProperty("exo:isOwner", false) ;
            
            newNode.setProperty("exo:editPermissionUsers", new String[] {});
            newNode.setProperty("exo:viewPermissionUsers", new String[] {});    
            newNode.setProperty("exo:editPermissionGroups", new String[] {});
            newNode.setProperty("exo:viewPermissionGroups", new String[] {});
            //newNode.setProperty("exo:tags", new String [] {}) ; 
          }  
          contactHomeNode.getSession().save() ;
          break ;         
        }         
      }      
    }
  }
  
  /**
   * {@inheritDoc} 
   */
  public void pasteAddressBook(String username, String srcAddress, String srcType, String destAddress, String destType) throws Exception {
    // CS-2389
    if (destType.equals(SHARED) && !haveEditPermissionOnAddressBook(username, destAddress)) {
      throw new AccessDeniedException();
    }
      if (srcType.equals(PERSONAL)) {
        Node contactHome = getPersonalContactsHome(username);
        QueryManager qm = contactHome.getSession().getWorkspace().getQueryManager();
        StringBuffer queryString = new StringBuffer("/jcr:root" + contactHome.getPath() 
                                                    + "//element(*,exo:contact)[@exo:categories='").
                                                    append(srcAddress).
                                                    append("']");
        Query query = qm.createQuery(queryString.toString(), Query.XPATH);
        QueryResult result = query.execute();
        NodeIterator iter = result.getNodes() ;
        copyNodes(username, contactHome, iter, destAddress, destType) ;      
      } else if (srcType.equals(SHARED)) {
        Node sharedAddressBookMock = getSharedAddressBooksHome(username) ;
        PropertyIterator proIter = sharedAddressBookMock.getReferences() ;
        QueryManager qm = sharedAddressBookMock.getSession().getWorkspace().getQueryManager();
        Node addressBook ;      
        while(proIter.hasNext()) {
          addressBook = proIter.nextProperty().getParent() ;
          if(addressBook.getName().equals(srcAddress)) {
            Node contactHomeNode = addressBook.getParent().getParent().getNode(CONTACTS) ;
            StringBuffer queryString = new StringBuffer("/jcr:root" + contactHomeNode.getPath() 
                                                        + "//element(*,exo:contact)[@exo:categories='").
                                                        append(srcAddress).
                                                        append("']");
            Query query = qm.createQuery(queryString.toString(), Query.XPATH);
            QueryResult result = query.execute();
            NodeIterator iter = result.getNodes() ;
            copyNodes(username, contactHomeNode, iter, destAddress, destType) ;
            break ;          
          }
        }          
      } else {
        String usersPath = nodeHierarchyCreator_.getJcrPath(USERS_PATH) ;
        Node publicContactHome = getPublicContactsHome();
        QueryManager qm = publicContactHome.getSession().getWorkspace().getQueryManager();
        StringBuffer queryString = new StringBuffer("/jcr:root" + usersPath
                                                    + "//element(*,exo:contact)[@exo:categories='")
                                                    .append(srcAddress).append("']") ;
        Query query = qm.createQuery(queryString.toString(), Query.XPATH);
        QueryResult result = query.execute();
        NodeIterator iter = result.getNodes() ;
        copyNodes(username, publicContactHome, iter, destAddress, destType) ;        
      }
  }
  
  /**
   * {@inheritDoc}
   */
  public List<Contact> pasteContacts(String username, String destAddress, String destType,  Map<String, String> contactsMap) throws Exception {
    // CS-2389
    if (destType.equals(SHARED) && !haveEditPermissionOnAddressBook(username, destAddress)){
      throw new AccessDeniedException();
    }
      List<Contact> contacts = new ArrayList<Contact>() ;
      List<Contact> pastedContacts = new ArrayList<Contact>() ;
      for (String contactId : contactsMap.keySet()) {
        String type = contactsMap.get(contactId) ;
        Contact contact = null ;
        if (type.equals(PERSONAL)) {
          contact = loadPersonalContact(username, contactId) ;
        } else if (type.equals(PUBLIC)) {
          contact = loadPublicContactByUser(contactId) ;
        } else { // test here
          contact = getSharedContact(username, contactId) ;
          if (contact ==  null) contact = getSharedContactAddressBook(username, contactId) ;
//        CS-2389
          if (contact != null && !haveEditPermissionOnContact(username, contact)
              && !haveEditPermissionOnAddressBook(username,contact.getAddressBookIds()[0])){
            log.error("do not have permission");
            throw new AccessDeniedException();
          }
        }        
        if (contact != null) contacts.add(contact) ; 
      }    
      
      for (Contact contact : contacts) {
        if (destType.equals(PERSONAL)) {
          Node contactHomeNode = getPersonalContactsHome(username);
          pastedContacts.add(Utils.getContact(saveCopyContact(contactHomeNode, contact, destAddress, destType), destType)) ; 
        } else if (destType.equals(SHARED)) {
          Node sharedAddressBookMock = getSharedAddressBooksHome(username) ;
          PropertyIterator iter = sharedAddressBookMock.getReferences() ;
          Node addressBook ;      
          while(iter.hasNext()) {
            addressBook = iter.nextProperty().getParent() ;
            if(addressBook.getName().equals(destAddress)) {
              Node contactHomeNode = addressBook.getParent().getParent().getNode(CONTACTS) ;
              pastedContacts.add(Utils.getContact(saveCopyContact(contactHomeNode, contact, destAddress, destType), destType)) ;   
              break ;
            }
          }
        }       
      }
      return pastedContacts ;
  }
  
  /**
   * {@inheritDoc}
   */
  public Node saveCopyContact(Node contactHomeNode, Contact contact, String destAddress, String destType) throws Exception {
    String newId = "Contact" + IdGenerator.generate() ;
    Node contactNode = contactHomeNode.addNode(newId, "exo:contact"); 
    contactNode.setProperty("exo:id", newId);
 
    contactNode.setProperty("exo:fullName", contact.getFullName());
    contactNode.setProperty("exo:firstName", contact.getFirstName());
    contactNode.setProperty("exo:lastName", contact.getLastName());
    contactNode.setProperty("exo:nickName", contact.getNickName());
    contactNode.setProperty("exo:gender", contact.getGender()) ;
    GregorianCalendar dateTime = new GregorianCalendar() ;
    Date birthday = contact.getBirthday() ;
    if (birthday != null) {
      dateTime.setTime(birthday) ;    
      contactNode.setProperty("exo:birthday", dateTime) ;
    }
    contactNode.setProperty("exo:jobTitle", contact.getJobTitle());
    if (contact.getEmailAddresses() != null)
      contactNode.setProperty("exo:emailAddress", contact.getEmailAddresses().toArray(new String[] {}));
    
    contactNode.setProperty("exo:exoId", contact.getExoId());
    contactNode.setProperty("exo:googleId", contact.getGoogleId());
    contactNode.setProperty("exo:msnId", contact.getMsnId());
    contactNode.setProperty("exo:aolId", contact.getAolId());
    contactNode.setProperty("exo:yahooId", contact.getYahooId());
    contactNode.setProperty("exo:icrId", contact.getIcrId());
    contactNode.setProperty("exo:skypeId", contact.getSkypeId());
    contactNode.setProperty("exo:icqId", contact.getIcqId());
    
    contactNode.setProperty("exo:homeAddress", contact.getHomeAddress());
    contactNode.setProperty("exo:homeCity", contact.getHomeCity());
    contactNode.setProperty("exo:homeState_province", contact.getHomeState_province());
    contactNode.setProperty("exo:homePostalCode", contact.getHomePostalCode());
    contactNode.setProperty("exo:homeCountry", contact.getHomeCountry());
    contactNode.setProperty("exo:homePhone1", contact.getHomePhone1());
    contactNode.setProperty("exo:homePhone2", contact.getHomePhone2());
    contactNode.setProperty("exo:homeFax", contact.getHomeFax());
    contactNode.setProperty("exo:personalSite", contact.getPersonalSite());
    
    contactNode.setProperty("exo:workAddress", contact.getWorkAddress());
    contactNode.setProperty("exo:workCity", contact.getWorkCity());
    contactNode.setProperty("exo:workState_province", contact.getWorkStateProvince());
    contactNode.setProperty("exo:workPostalCode", contact.getWorkPostalCode());
    contactNode.setProperty("exo:workCountry", contact.getWorkCountry());
    contactNode.setProperty("exo:workPhone1", contact.getWorkPhone1());
    contactNode.setProperty("exo:workPhone2", contact.getWorkPhone2());
    contactNode.setProperty("exo:workFax", contact.getWorkFax());
    contactNode.setProperty("exo:mobilePhone", contact.getMobilePhone());
    contactNode.setProperty("exo:webPage", contact.getWebPage());
    
    contactNode.setProperty("exo:note", contact.getNote());
    contactNode.setProperty("exo:tags", contact.getTags());
    contactNode.setProperty(PROP_ADDRESSBOOK_REFS, new String[] {destAddress}); 
    if (contact.getLastUpdated() != null) {
      dateTime.setTime(contact.getLastUpdated()) ;
      contactNode.setProperty("exo:lastUpdated", dateTime);
    }
    
//  save image to contact
    ContactAttachment attachment = contact.getAttachment() ;
    if (attachment != null) {
      if (attachment.getFileName() != null) {
//      fix load image on IE6 UI
        ExtendedNode extNode = (ExtendedNode)contactNode ;
        if (extNode.canAddMixin("exo:privilegeable")) extNode.addMixin("exo:privilegeable");
        String[] arrayPers = {PermissionType.READ, PermissionType.ADD_NODE, PermissionType.SET_PROPERTY, PermissionType.REMOVE} ;
        extNode.setPermission(SystemIdentity.ANY, arrayPers) ;
        List<AccessControlEntry> permsList = extNode.getACL().getPermissionEntries() ;   
        for(AccessControlEntry accessControlEntry : permsList) {
          extNode.setPermission(accessControlEntry.getIdentity(), arrayPers) ;      
        }
        
        Node nodeFile = null ;
        try {
          nodeFile = contactNode.getNode("image") ;
        } catch (PathNotFoundException ex) {
          nodeFile = contactNode.addNode("image", "nt:file");
        }
        Node nodeContent = null ;
        try {
          nodeContent = nodeFile.getNode("jcr:content") ;
        } catch (PathNotFoundException ex) {
          nodeContent = nodeFile.addNode("jcr:content", "nt:resource") ;
        }
        nodeContent.setProperty("jcr:mimeType", attachment.getMimeType()) ;
        nodeContent.setProperty("jcr:data", attachment.getInputStream());
        nodeContent.setProperty("jcr:lastModified", Calendar.getInstance().getTimeInMillis());
      }
    }else {
      if(contactNode.hasNode("image")) contactNode.getNode("image").remove() ;
    }
    contactHomeNode.getSession().save() ;
    return contactNode ;
  }
  
  /**
   * {@inheritDoc}
   */
  public void registerNewUser(User user, boolean isNew) throws Exception {
    Contact contact = null ;
    if (isNew) contact = new Contact() ;
    else contact = loadPublicContactByUser(user.getUserName()) ;
    if (contact != null) {
      contact.setFullName(user.getFirstName() + " " + user.getLastName()) ;
      contact.setFirstName(user.getFirstName()) ;
      contact.setLastName(user.getLastName()) ;
      contact.setEmailAddress(user.getEmail()) ;    
      Calendar cal = new GregorianCalendar() ;
      contact.setLastUpdated(cal.getTime()) ;
    }
      if(isNew) {
        Node addressHome = getPersonalAddressBooksHome(user.getUserName()) ;
        
        AddressBook addressbook = new AddressBook() ;
        addressbook.setId(NewUserListener.DEFAULTGROUP+user.getUserName()) ;
        addressbook.setName(NewUserListener.DEFAULTGROUPNAME) ;
        addressbook.setDescription(NewUserListener.DEFAULTGROUPDES) ;
        Node groupNode = addressHome.addNode(addressbook.getId(), "exo:contactGroup");
        groupNode.setProperty("exo:id", addressbook.getId()); 
        groupNode.setProperty("exo:name", addressbook.getName());
        groupNode.setProperty("exo:description", addressbook.getDescription());
        groupNode.setProperty("exo:editPermissionUsers", addressbook.getEditPermissionUsers()) ;
        groupNode.setProperty("exo:viewPermissionUsers", addressbook.getViewPermissionUsers()) ;
        groupNode.setProperty("exo:editPermissionGroups", addressbook.getEditPermissionGroups()) ;
        groupNode.setProperty("exo:viewPermissionGroups", addressbook.getViewPermissionGroups()) ;
        
//      TODO cs-1141
        Node addressesGroup = addressHome.addNode(NewUserListener.ADDRESSESGROUP + user.getUserName(), "exo:contactGroup");
        addressesGroup.setProperty("exo:id", NewUserListener.ADDRESSESGROUP + user.getUserName()); 
        addressesGroup.setProperty("exo:name", NewUserListener.ADDRESSESGROUPNAME);
        addressHome.getSession().save();
        
        // save contact
        contact.setId(user.getUserName()) ;
        Set<String> groupIds = new HashSet<String>() ;
        groupIds.add(addressbook.getId()) ;   
        ExoContainer container = ExoContainerContext.getCurrentContainer();
        OrganizationService organizationService = 
          (OrganizationService)container.getComponentInstanceOfType(OrganizationService.class) ;
        Object[] groupsOfUser = organizationService.getGroupHandler().findGroupsOfUser(user.getUserName()).toArray() ;
        for (Object object : groupsOfUser) {
          String id = ((Group)object).getId() ;
          groupIds.add(id) ;
        }
        
        contact.setAddressBookIds(groupIds.toArray(new String[] {})) ;
        contact.setOwner(true) ;
        contact.setOwnerId(user.getUserName()) ;
        saveContactUser(user.getUserName(), contact, true) ;
        QueryManager qm = getSession(createSystemProvider()).getWorkspace().getQueryManager();
        String usersPath = nodeHierarchyCreator_.getJcrPath(DataStorage.USERS_PATH) ;
        List<String> recievedUser = new ArrayList<String>() ;
        recievedUser.add(user.getUserName()) ;
        for (Object object : groupsOfUser) {  
          String groupId = ((Group)object).getId() ;
          // get all address books that current user can see thank to his groups
          StringBuffer queryString = new StringBuffer("/jcr:root" + usersPath 
              + "//element(*,exo:contactGroup)[@exo:viewPermissionGroups='").append(groupId + "']") ;        
          Query query = qm.createQuery(queryString.toString(), Query.XPATH);
          QueryResult result = query.execute();
          NodeIterator nodes = result.getNodes() ;
          while (nodes.hasNext()) {
            Node addressBook = nodes.nextNode() ;
            //share between adressbook owner and current user
            shareAddressBook(addressBook.getProperty("exo:sharedUserId")
                .getString(), addressBook.getProperty("exo:id").getString(), recievedUser) ;
          }

          // lookup shared contacts that user can see thank to his groups
          queryString = new StringBuffer("/jcr:root" + usersPath 
              + "//element(*,exo:contact)[@exo:viewPermissionGroups='").append(groupId + "']") ;        
          query = qm.createQuery(queryString.toString(), Query.XPATH);
          result = query.execute();
          nodes = result.getNodes() ;
          while (nodes.hasNext()) {
            Node contactNode = nodes.nextNode() ;
            String split = "/" ;
            String temp = contactNode.getPath().split(usersPath)[1] ;
            String userId = temp.split(split)[1] ;
            shareContact(userId,
                new String[] {contactNode.getProperty("exo:id").getString()}, recievedUser) ;
          }
        }
        Node userApp = nodeHierarchyCreator_.getUserApplicationNode(createSystemProvider(), user.getUserName()) ;
        userApp.getSession().save() ;
      } else {
        if (contact != null) {
          saveContactUser(user.getUserName(), contact, false) ; 
        }
      }
  }  
  
  /**
   * {@inheritDoc}
   */
  public SessionProvider createSessionProvider() {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    SessionProviderService service = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class);
    SessionProvider provider = service.getSessionProvider(null);
    if (provider == null) {
      log.info("No user session provider was available, using a system session provider");
      provider = service.getSystemSessionProvider(null);
    }
    return provider;
  }

  /**
   * {@inheritDoc}
   */
  public SessionProvider createSystemProvider() {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    SessionProviderService service = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class);
    return service.getSystemSessionProvider(null) ;    
  }
  
  /**
   * {@inheritDoc}
   */
  public void closeSessionProvider(SessionProvider sessionProvider) {
   if (sessionProvider != null) sessionProvider.close(); 
  }
  
  /**
   * {@inheritDoc}
   */
  public void saveAddress(String username, String emailAddress) throws Exception {
    ContactFilter filter = new ContactFilter() ;
    for (String email : Utils.parseEmails(emailAddress)) {
      filter.setEmailAddress(email) ;
      if (searchContact(username, filter).getAll().size() == 0) {
        Contact contact = new Contact() ;
        String name = email.split("@")[0] ;
        contact.setLastName(name) ;
        contact.setFullName(name) ;
        contact.setEmailAddress(email) ;
        contact.setAddressBookIds(new String[] {NewUserListener.ADDRESSESGROUP + username}) ;
        saveContactUser(username, contact, true) ;
      }
    }
  } 
  
  
  public Node getNodeByPath(String nodePath, SessionProvider sessionProvider) throws Exception {
    return (Node) getSession(sessionProvider).getItem(nodePath);
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean haveEditPermissionOnAddressBook(String username, String addressBookId) throws Exception {
    AddressBook addressbook = getSharedAddressBookById(username, addressBookId);
    if (addressbook == null) return false;
    if (addressbook.getEditPermissionUsers() != null &&
        Arrays.asList(addressbook.getEditPermissionUsers()).contains(username + DataStorage.HYPHEN)) {
      return true ;
    }
    String[] editPerGroups = addressbook.getEditPermissionGroups() ;
    if (editPerGroups != null) {                  
      List<String> groupIds = new ArrayList<String>() ;
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      OrganizationService organizationService = 
        (OrganizationService)container.getComponentInstanceOfType(OrganizationService.class) ;
      Object[] groupsOfUser = organizationService.getGroupHandler().findGroupsOfUser(username).toArray() ;
      for (Object object : groupsOfUser) {
        String groupId = ((Group)object).getId() ;
        groupIds.add(groupId) ;
      }                  
      for (String editPer : editPerGroups)
        if (groupIds.contains(editPer)) return true ;
    }
    return false;
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean haveEditPermissionOnContact(String username, Contact contact) throws Exception {
    if (contact.getEditPermissionUsers() != null &&
        Arrays.asList(contact.getEditPermissionUsers()).contains(username + DataStorage.HYPHEN)) {
      return true ;
    }
    List<String> groupIds = new ArrayList<String>() ;
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    OrganizationService organizationService = 
      (OrganizationService)container.getComponentInstanceOfType(OrganizationService.class) ;
    Object[] groupsOfUser = organizationService.getGroupHandler().findGroupsOfUser(username).toArray() ;
    for (Object object : groupsOfUser) {
      String groupId = ((Group)object).getId() ;
      groupIds.add(groupId) ;
    }
    String[] editPerGroups = contact.getEditPermissionGroups() ;
    if (editPerGroups != null)
      for (String editPer : editPerGroups)
        if (groupIds.contains(editPer)) {
          return true ;
        }
    return false ;
  }
  
  /**
   * {@inheritDoc}
   */
  public Session getSession(SessionProvider sprovider) throws Exception{
    ManageableRepository currentRepo = repoService_.getCurrentRepository() ;
    return sprovider.getSession(currentRepo.getConfiguration().getDefaultWorkspaceName(), currentRepo) ;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Node getContactApplicationDataHome() throws Exception {
    SessionProvider sProvider = createSystemProvider();
    Node applicationDataHome = getNodeByPath(nodeHierarchyCreator_.getPublicApplicationNode(sProvider).getPath(),sProvider) ;
    try {
      return  applicationDataHome.getNode(CONTACT_APP) ;
    } catch (PathNotFoundException ex) {
      Node contactApplicationDataHome = applicationDataHome.addNode(CONTACT_APP, NT_UNSTRUCTURED) ;
      applicationDataHome.save() ;
      return contactApplicationDataHome ;
    }
  }
  
/*  public void makePublicAddresses() throws Exception {    
    Node appNode = getContactApplicationDataHome();
    if (appNode.hasNode(GROUP_ADDRESS_BOOKS)) return;
    System.out.println("\n\n make \n\n");
    Node addressHome = appNode.addNode(GROUP_ADDRESS_BOOKS);
    appNode.getSession().save();
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    OrganizationService organizationService = 
      (OrganizationService)container.getComponentInstanceOfType(OrganizationService.class) ;
    Object[] groupsOfUser = organizationService.getGroupHandler().getAllGroups().toArray();
    for (Object object : groupsOfUser) {
      Group group = (Group)object;
      String groupId = Utils.encodeGroupId(group.getId()) ;
      Node groupNode = addressHome.addNode(groupId, "exo:contactGroup");
      groupNode.setProperty("exo:id", groupId); 
      groupNode.setProperty("exo:name", group.getGroupName());
      groupNode.setProperty("exo:viewPermissionGroups", new String [] { Utils.decodeGroupId(groupId) }) ;
      groupNode.setProperty("exo:editPermissionGroups", new String [] {"/platform/administrators"});
    }
    addressHome.getSession().save();
  }*/

  @Override
  public Node getContactUserDataHome(String username) throws Exception {
    SessionProvider sessionProvider = createSystemProvider() ;
    Node userDataHome = getNodeByPath(nodeHierarchyCreator_.getUserApplicationNode(sessionProvider, username).getPath(), sessionProvider)  ;
    try {
      return  userDataHome.getNode(CONTACT_APP) ;
    } catch (PathNotFoundException ex) {
      Node contactUserDataHome = userDataHome.addNode(CONTACT_APP, NT_UNSTRUCTURED) ;
      userDataHome.getSession().save() ;
      return contactUserDataHome ;
    }   
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Node getPersonalAddressBooksHome(String username) throws Exception {
    Node userDataHome = getContactUserDataHome(username) ;
    try {
      return userDataHome.getNode(PERSONAL_ADDRESS_BOOKS) ;
    } catch (PathNotFoundException ex) {
      Node padHome = userDataHome.addNode(PERSONAL_ADDRESS_BOOKS, NT_UNSTRUCTURED) ;
      userDataHome.save() ;
      return padHome ;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Node getPersonalContactsHome(String username) throws Exception {
    Node userDataHome = getContactUserDataHome(username) ;
    try {
      return userDataHome.getNode(CONTACTS) ;
    } catch (PathNotFoundException ex) {
      Node personalContactsHome = userDataHome.addNode(CONTACTS, NT_UNSTRUCTURED) ;
      userDataHome.save() ;
      return personalContactsHome ;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> getPublicAddressBookContacts(String[] groupIds) throws Exception {
    List<String> groups = new ArrayList<String>();
    for(String groupId : groupIds) { 
      if(hasContacts(groupId))  groups.add(groupId) ; 
    }
    return groups;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<GroupContactData> getPublicContacts(String[] groupIds) throws Exception {
    List<GroupContactData> contactByGroup = new ArrayList<GroupContactData>() ;
    List<Contact> contacts;
    for(String groupId : groupIds) { 
      contacts = getPublicContactsByAddressBook(groupId).getAll();
      if(contacts.size() > 0)
        contactByGroup.add(new GroupContactData(groupId, contacts));     
    }
    return contactByGroup;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Node getPublicContactsHome() throws Exception {
    Node contactServiceHome = getContactApplicationDataHome() ;
    try {
      return contactServiceHome.getNode(CONTACTS) ;
    } catch (PathNotFoundException ex) {
      Node publicHome = contactServiceHome.addNode(CONTACTS, NT_UNSTRUCTURED) ;
      contactServiceHome.save() ;
      return publicHome ;
    }
  }
  
  public Node getPublicAddressHome() throws Exception {
    Node contactServiceHome = getContactApplicationDataHome() ;
    try {
      return contactServiceHome.getNode(GROUP_ADDRESS_BOOKS) ;
    } catch (PathNotFoundException ex) {
      Node publicHome = contactServiceHome.addNode(GROUP_ADDRESS_BOOKS, NT_UNSTRUCTURED) ;
      contactServiceHome.save() ;
      return publicHome ;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Node getTagsHome(String username) throws Exception {
    Node contactServiceHome = getContactUserDataHome(username) ;
    try {
      return contactServiceHome.getNode(TAGS) ;
    } catch (PathNotFoundException ex) {
      Node tagHome = contactServiceHome.addNode(TAGS, NT_UNSTRUCTURED) ;
      contactServiceHome.save() ;
      return tagHome ;
    } 
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> getUserContactNodesByGroup(String username, String groupId) throws Exception {
    
    Node contactHome = getPersonalContactsHome(username);
    QueryManager qm = contactHome.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root" + contactHome.getPath() 
                                                + "//element(*,exo:contact)[@exo:categories='").
                                                append(groupId).
                                                append("']");
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    NodeIterator it = result.getNodes();
    List<String> contactIds = new ArrayList<String>();
    while (it.hasNext()) contactIds.add(it.nextNode().getProperty("exo:id").getString());
    return contactIds ;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean hasContacts(String groupId) throws Exception {
    Node contactHome = getPublicContactsHome();
    QueryManager qm = contactHome.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root" + contactHome.getPath() 
                                                + "//element(*,exo:contact)[@exo:categories='").
                                                append(groupId).
                                                append("']");
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    if(result.getNodes().getSize() > 0) return true;
    return false ;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public Node getSharedContactsHome(String user) throws Exception{
    Node contactHome = getContactUserDataHome(user);
    Node sharedHome ;
    try {
      sharedHome = contactHome.getNode(SHARED_HOME) ;
    } catch (PathNotFoundException ex) {
      sharedHome = contactHome.addNode(SHARED_HOME, NT_UNSTRUCTURED) ;
      contactHome.save();
    }   
    return sharedHome;
  }
}
