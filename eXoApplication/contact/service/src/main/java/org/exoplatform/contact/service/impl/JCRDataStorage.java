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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyIterator;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.logging.Log;
import org.exoplatform.contact.service.AddressBook;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactAttachment;
import org.exoplatform.contact.service.ContactFilter;
import org.exoplatform.contact.service.ContactPageList;
import org.exoplatform.contact.service.DataPageList;
import org.exoplatform.contact.service.GroupContactData;
import org.exoplatform.contact.service.SharedAddressBook;
import org.exoplatform.contact.service.Tag;
import org.exoplatform.contact.service.Utils;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.impl.GroupImpl;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 10, 2007  
 */
public class JCRDataStorage {
  
  private static final String PROP_ADDRESSBOOK_REFS = "exo:categories";
  final private static String CONTACTS = "contacts".intern() ;
  final private static String PERSONAL_ADDRESS_BOOKS = "contactGroup".intern() ;
  final private static String TAGS = "tags".intern() ;
  final private static String NT_UNSTRUCTURED = "nt:unstructured".intern() ;
  final private static String CONTACT_APP = "ContactApplication".intern() ;
  final private static String SHARED_HOME = "Shared".intern() ;
  final private static String SHARED_CONTACT = "SharedContact".intern() ;
  final private static String SHARED_ADDRESSBOOK = "SharedAddressBook".intern() ;
  final private static String SHARED_MIXIN = "exo:contactShared".intern();
  final private static String SHARED_PROP = "exo:sharedId".intern();
  final public static String USERS_PATH = "usersPath".intern() ;
  final public static String PERSONAL = "0".intern();
  final public static String SHARED = "1".intern();
  final public static String PUBLIC = "2".intern();
  final public static String SPLIT = "::".intern();
  final public static String HYPHEN = "shared_".intern();
  
  private static final Log log = ExoLogger.getLogger(JCRDataStorage.class);
  
  private NodeHierarchyCreator nodeHierarchyCreator_ ;
  
  public JCRDataStorage(NodeHierarchyCreator nodeHierarchyCreator)throws Exception {
    nodeHierarchyCreator_ = nodeHierarchyCreator ;
  }  
  

  /**
   * Get the home node for user data of contact service
   * @param sProvider
   * @param username
   * @return
   * @throws Exception
   */
  public Node getContactUserDataHome(SessionProvider sProvider, String username) throws Exception {
    Node userDataHome = nodeHierarchyCreator_.getUserApplicationNode(sProvider, username)  ;
    try {
      return  userDataHome.getNode(CONTACT_APP) ;
    } catch (PathNotFoundException ex) {
      Node contactUserDataHome = userDataHome.addNode(CONTACT_APP, NT_UNSTRUCTURED) ;
      userDataHome.getSession().save() ;
      return contactUserDataHome ;
    }   
  }
  
  public Node getContactApplicationDataHome(SessionProvider sProvider) throws Exception {
    Node applicationDataHome = nodeHierarchyCreator_.getPublicApplicationNode(sProvider) ;
    try {
      return  applicationDataHome.getNode(CONTACT_APP) ;
    } catch (PathNotFoundException ex) {
      Node contactApplicationDataHome = applicationDataHome.addNode(CONTACT_APP, NT_UNSTRUCTURED) ;
      applicationDataHome.save() ;
      return contactApplicationDataHome ;
    }
  }
  
  public Node getPersonalContactsHome(SessionProvider sProvider, String username) throws Exception {
    Node userDataHome = getContactUserDataHome(sProvider, username) ;
    try {
      return userDataHome.getNode(CONTACTS) ;
    } catch (PathNotFoundException ex) {
      Node personalContactsHome = userDataHome.addNode(CONTACTS, NT_UNSTRUCTURED) ;
      userDataHome.save() ;
      return personalContactsHome ;
    }
  }
  
  public Node getPersonalAddressBooksHome(SessionProvider sProvider, String username) throws Exception {
    Node userDataHome = getContactUserDataHome(sProvider, username) ;
    try {
      return userDataHome.getNode(PERSONAL_ADDRESS_BOOKS) ;
    } catch (PathNotFoundException ex) {
      Node padHome = userDataHome.addNode(PERSONAL_ADDRESS_BOOKS, NT_UNSTRUCTURED) ;
      userDataHome.save() ;
      return padHome ;
    }
  }
  
  public Node getPublicContactsHome(SessionProvider sProvider) throws Exception {
    Node contactServiceHome = getContactApplicationDataHome(sProvider) ;
    try {
      return contactServiceHome.getNode(CONTACTS) ;
    } catch (PathNotFoundException ex) {
      Node publicHome = contactServiceHome.addNode(CONTACTS, NT_UNSTRUCTURED) ;
      contactServiceHome.save() ;
      return publicHome ;
    }
  }
  
  public Node getTagsHome(SessionProvider sProvider, String username) throws Exception {
    Node contactServiceHome = getContactUserDataHome(sProvider, username) ;
    try {
      return contactServiceHome.getNode(TAGS) ;
    } catch (PathNotFoundException ex) {
      Node tagHome = contactServiceHome.addNode(TAGS, NT_UNSTRUCTURED) ;
      contactServiceHome.save() ;
      //contactServiceHome.getSession().save() ;
      return tagHome ;
    } 
  }
  
  /**
   * get the node that holds references to all address books shared to a given user.
   * @param userId
   * @return
   * @throws Exception
   */
  public Node getSharedAddressBooksHome(SessionProvider provider, String userId) throws Exception {
      Node userData = getContactUserDataHome(provider, userId);
      Node sharedHome;
      if (!userData.hasNode(SHARED_HOME)) {
        sharedHome = userData.addNode(SHARED_HOME, NT_UNSTRUCTURED);
        userData.save();
      } else {
        sharedHome = userData.getNode(SHARED_HOME);
      }

      Node sharedAddressBooksHome = null;
      if (!sharedHome.hasNode(SHARED_ADDRESSBOOK)) {
        sharedAddressBooksHome = sharedHome.addNode(SHARED_ADDRESSBOOK, NT_UNSTRUCTURED);
        if (sharedAddressBooksHome.canAddMixin("mix:referenceable")) {
          sharedAddressBooksHome.addMixin("mix:referenceable");
        }
        sharedHome.save();        
      } else {
        sharedAddressBooksHome = sharedHome.getNode(SHARED_ADDRESSBOOK);
      }
      return sharedAddressBooksHome;
  }
  
  
  public Node getSharedContactsHome(SessionProvider provider, String userId) throws Exception {
    Node userData = getContactUserDataHome(provider, userId);
    Node sharedHome;
    if (!userData.hasNode(SHARED_HOME)) {
      sharedHome = userData.addNode(SHARED_HOME, NT_UNSTRUCTURED);
      userData.save();
    } else {
      sharedHome = userData.getNode(SHARED_HOME);
    }

    Node sharedContactsHome = null;
    if (!sharedHome.hasNode(SHARED_CONTACT)) {
      sharedContactsHome = sharedHome.addNode(SHARED_CONTACT, NT_UNSTRUCTURED);
      if (sharedContactsHome.canAddMixin("mix:referenceable")) {
        sharedContactsHome.addMixin("mix:referenceable");
      }
      sharedHome.save();        
    } else {
      sharedContactsHome = sharedHome.getNode(SHARED_CONTACT);
    }
    return sharedContactsHome;
} 
  
 
  private Node getSharedContact(String userId) throws Exception {
    SessionProvider provider = SessionProvider.createSystemProvider();
    try {
    Node contactHome = getContactUserDataHome(provider, userId);
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
    } finally {
      //provider.close();
    }
  }  
  
/**
 * @deprecated use {@link #getSharedAddressBooksHome(SessionProvider, String) and provide an appropriate SessionProvider
 * this method leaves a system SessionProvider open.
 */
  public Node getSharedAddressBooksHome(String userId) throws Exception {
    SessionProvider provider = null;
    try {
      provider = createSystemProvider();
      Node contactHome = getContactUserDataHome(provider, userId);
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
    } finally {
      // we can't close because receiving code may want to call Node.getSession() on result
    }

  }    
  

  private String [] ValuesToStrings(Value[] Val) throws Exception {
    if(Val.length == 1) return new String[]{Val[0].getString()};
    String[] Str = new String[Val.length];
    for(int i = 0; i < Val.length; ++i) {
      Str[i] = Val[i].getString();
    }
    return Str;
  }
  

  /**
   * Load the public contact for a given username
   * @param userId username for which the corresponding contact will be loaded
   * @return the contact or null if the contact could not be loaded
   * @throws Exception
   */
  public Contact loadPublicContactByUser(String userId) throws Exception {
    SessionProvider provider = SessionProvider.createSystemProvider();
    try {
      // public contact is stored under the corresponding user application data
      Node contactHomeNode = getPersonalContactsHome(provider, userId) ;
      return getContact(contactHomeNode.getNode(userId), PUBLIC);
    } catch (PathNotFoundException e) {
      log.error("Public contact " + userId + " not found");
      return null ;
    } finally {
      provider.close();
    }
  }  
  
  private Contact getContact(Node contactNode, String contactType) throws Exception {
    Contact contact = new Contact();
    contact.setContactType(contactType) ;
    if(contactNode.hasProperty("exo:id")) contact.setId(contactNode.getProperty("exo:id").getString()) ;
    if(contactNode.hasProperty("exo:fullName"))contact.setFullName(contactNode.getProperty("exo:fullName").getString());
    if(contactNode.hasProperty("exo:firstName"))contact.setFirstName(contactNode.getProperty("exo:firstName").getString());
    if(contactNode.hasProperty("exo:lastName"))contact.setLastName(contactNode.getProperty("exo:lastName").getString());
    if(contactNode.hasProperty("exo:nickName"))contact.setNickName(contactNode.getProperty("exo:nickName").getString());
    if(contactNode.hasProperty("exo:gender"))contact.setGender(contactNode.getProperty("exo:gender").getString());
    if(contactNode.hasProperty("exo:birthday"))contact.setBirthday(contactNode.getProperty("exo:birthday").getDate().getTime());
    if(contactNode.hasProperty("exo:jobTitle"))contact.setJobTitle(contactNode.getProperty("exo:jobTitle").getString());
    if(contactNode.hasProperty("exo:emailAddress"))contact.setEmailAddress(contactNode.getProperty("exo:emailAddress").getString());
    
    if(contactNode.hasProperty("exo:exoId"))contact.setExoId(contactNode.getProperty("exo:exoId").getString());
    if(contactNode.hasProperty("exo:googleId"))contact.setGoogleId(contactNode.getProperty("exo:googleId").getString());
    if(contactNode.hasProperty("exo:msnId"))contact.setMsnId(contactNode.getProperty("exo:msnId").getString());
    if(contactNode.hasProperty("exo:aolId"))contact.setAolId(contactNode.getProperty("exo:aolId").getString());
    if(contactNode.hasProperty("exo:yahooId"))contact.setYahooId(contactNode.getProperty("exo:yahooId").getString());
    if(contactNode.hasProperty("exo:icrId"))contact.setIcrId(contactNode.getProperty("exo:icrId").getString());
    if(contactNode.hasProperty("exo:skypeId"))contact.setSkypeId(contactNode.getProperty("exo:skypeId").getString());
    if(contactNode.hasProperty("exo:icqId"))contact.setIcqId(contactNode.getProperty("exo:icqId").getString());
    
    if(contactNode.hasProperty("exo:homeAddress"))contact.setHomeAddress(contactNode.getProperty("exo:homeAddress").getString());
    if(contactNode.hasProperty("exo:homeCity"))contact.setHomeCity(contactNode.getProperty("exo:homeCity").getString());
    if(contactNode.hasProperty("exo:homeState_province"))contact.setHomeState_province(contactNode.getProperty("exo:homeState_province").getString());
    if(contactNode.hasProperty("exo:homePostalCode"))contact.setHomePostalCode(contactNode.getProperty("exo:homePostalCode").getString());
    if(contactNode.hasProperty("exo:homeCountry"))contact.setHomeCountry(contactNode.getProperty("exo:homeCountry").getString());
    if(contactNode.hasProperty("exo:homePhone1"))contact.setHomePhone1(contactNode.getProperty("exo:homePhone1").getString());
    if(contactNode.hasProperty("exo:homePhone2"))contact.setHomePhone2(contactNode.getProperty("exo:homePhone2").getString());
    if(contactNode.hasProperty("exo:homeFax"))contact.setHomeFax(contactNode.getProperty("exo:homeFax").getString());
    if(contactNode.hasProperty("exo:personalSite"))contact.setPersonalSite(contactNode.getProperty("exo:personalSite").getString());
    
    if(contactNode.hasProperty("exo:workAddress"))contact.setWorkAddress(contactNode.getProperty("exo:workAddress").getString());
    if(contactNode.hasProperty("exo:workCity"))contact.setWorkCity(contactNode.getProperty("exo:workCity").getString());
    if(contactNode.hasProperty("exo:workState_province"))contact.setWorkStateProvince(contactNode.getProperty("exo:workState_province").getString());
    if(contactNode.hasProperty("exo:workPostalCode"))contact.setWorkPostalCode(contactNode.getProperty("exo:workPostalCode").getString());
    if(contactNode.hasProperty("exo:workCountry"))contact.setWorkCountry(contactNode.getProperty("exo:workCountry").getString());
    if(contactNode.hasProperty("exo:workPhone1"))contact.setWorkPhone1(contactNode.getProperty("exo:workPhone1").getString());
    if(contactNode.hasProperty("exo:workPhone2"))contact.setWorkPhone2(contactNode.getProperty("exo:workPhone2").getString());
    if(contactNode.hasProperty("exo:workFax"))contact.setWorkFax(contactNode.getProperty("exo:workFax").getString());
    if(contactNode.hasProperty("exo:mobilePhone"))contact.setMobilePhone(contactNode.getProperty("exo:mobilePhone").getString());
    if(contactNode.hasProperty("exo:webPage"))contact.setWebPage(contactNode.getProperty("exo:webPage").getString());
    if(contactNode.hasProperty("exo:note"))contact.setNote(contactNode.getProperty("exo:note").getString());
    if(contactNode.hasProperty(PROP_ADDRESSBOOK_REFS))contact.setAddressBookIds(ValuesToStrings(contactNode.getProperty(PROP_ADDRESSBOOK_REFS).getValues()));
    if(contactNode.hasProperty("exo:tags")) contact.setTags(ValuesToStrings(contactNode.getProperty("exo:tags").getValues()));
    if(contactNode.hasProperty("exo:editPermissionUsers")) contact.setEditPermissionUsers(ValuesToStrings(contactNode.getProperty("exo:editPermissionUsers").getValues()));
    if(contactNode.hasProperty("exo:viewPermissionUsers")) contact.setViewPermissionUsers(ValuesToStrings(contactNode.getProperty("exo:viewPermissionUsers").getValues()));
    
    if(contactNode.hasProperty("exo:editPermissionGroups")) contact.setEditPermissionGroups(ValuesToStrings(contactNode.getProperty("exo:editPermissionGroups").getValues()));
    if(contactNode.hasProperty("exo:viewPermissionGroups")) contact.setViewPermissionGroups(ValuesToStrings(contactNode.getProperty("exo:viewPermissionGroups").getValues()));
    
    if(contactNode.hasProperty("exo:lastUpdated"))contact.setLastUpdated(contactNode.getProperty("exo:lastUpdated").getDate().getTime());
    contact.setPath(contactNode.getPath()) ;
    if(contactNode.hasNode("image")){
      Node image = contactNode.getNode("image");
      if (image.isNodeType("nt:file")) {
        ContactAttachment file = new ContactAttachment() ;
        file.setId(image.getPath()) ;
        file.setMimeType(image.getNode("jcr:content").getProperty("jcr:mimeType").getString()) ;
        file.setFileName(image.getName()) ;
        file.setWorkspace(image.getSession().getWorkspace().getName()) ;
        contact.setAttachment(file) ;
      }
    }
    if(contactNode.hasProperty("exo:isOwner")) contact.setOwner(contactNode.getProperty("exo:isOwner").getBoolean());
    if(contactNode.hasProperty("exo:ownerId")) contact.setOwnerId(contactNode.getProperty("exo:ownerId").getString());
    return contact;
  }

  public List<Contact> findAllContactsByOwner(String username) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSessionProvider();
      Node contactHomeNode = getPersonalContactsHome(sProvider, username);
      List<Contact> contacts = new ArrayList<Contact>();
      NodeIterator iter = contactHomeNode.getNodes();
      Contact contact;
      while (iter.hasNext()) {
        Node contactNode = iter.nextNode();
        contact = getContact(contactNode, PERSONAL);
        contacts.add(contact);
      }
      return contacts;
    } finally {
      closeSessionProvider(sProvider);
    }
  }

  public ContactPageList findContactsByFilter(String username,
                                                   ContactFilter filter,
                                                   String type) throws Exception {
    QueryManager qm = null;
    SessionProvider sysp = createSystemProvider();
    try {
      if (type.equals(PERSONAL)) {
        // look in user home
        Node contactHomeNode = getPersonalContactsHome(sysp, username);
        filter.setAccountPath(contactHomeNode.getPath());
        qm = contactHomeNode.getSession().getWorkspace().getQueryManager();
      } else if (type.equals(PUBLIC)) {
        // look in all users
        Node publicContactHomeNode = getPublicContactsHome(sysp);
        String usersPath = nodeHierarchyCreator_.getJcrPath(USERS_PATH);
        filter.setAccountPath(usersPath);
        qm = publicContactHomeNode.getSession().getWorkspace().getQueryManager();
      } else if (type.equals(SHARED)) {
        // look in contacts shared to username
        Node sharedAddressBookHolder = getSharedAddressBooksHome(sysp,username);
        PropertyIterator iter = sharedAddressBookHolder.getReferences();
        Node addressBook;
        while (iter.hasNext()) {
          addressBook = iter.nextProperty().getParent();
          if (addressBook.getName().equals(filter.getCategories()[0])) {
            Node contacts = addressBook.getParent().getParent().getNode(CONTACTS);
            filter.setAccountPath(contacts.getPath());
            qm = contacts.getSession().getWorkspace().getQueryManager();
            break;
          }
        }
      }
      if (qm != null) {
        Query query = qm.createQuery(filter.getStatement(), Query.XPATH);
        QueryResult result = query.execute();
        return new ContactPageList(username,
                                   result.getNodes(),
                                   10,
                                   filter.getStatement(),
                                   true,
                                   type);
      }
      return null;
    } finally {
      closeSessionProvider(sysp);
    }
  }
  
  
  public Contact loadPersonalContact(String ownerUserId, String contactId) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSessionProvider();
      Node contactHomeNode = getPersonalContactsHome(sProvider, ownerUserId);
      try {
        return getContact(contactHomeNode.getNode(contactId), PERSONAL);
      } catch (PathNotFoundException ex) {
        return null;
      }
    } finally {
      closeSessionProvider(sProvider);
    }
  }

  public ContactPageList findPersonalContactsByAddressBook(String owner, String addressBookId) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSessionProvider();
      Node userContactsHome = getPersonalContactsHome(sProvider, owner);
      QueryManager qm = userContactsHome.getSession().getWorkspace().getQueryManager();
      String queryString = new StringBuffer("/jcr:root" + userContactsHome.getPath()
          + "//element(*,exo:contact)[@exo:categories='").append(addressBookId)
                                                         .append("']")
                                                         .append("order by @exo:fullName,@exo:id ascending")
                                                         .toString();
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      ContactPageList pageList = new ContactPageList(owner,
                                                     result.getNodes(),
                                                     10,
                                                     queryString,
                                                     true,
                                                     PERSONAL);
      return pageList;
    } finally {
      closeSessionProvider(sProvider);
    }
  }
  
  enum AddressBookType {Personal, Shared, Public};
  
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
  
  private AddressBookType getAddressBookType(String username, String addressBookId) throws Exception {
    AddressBook shared = getSharedAddressBookById(username, addressBookId);
    if (shared != null) {
      return AddressBookType.Shared;
    }
    
    AddressBook personal = findPersonalAddressBookById(username, addressBookId);
    if (personal != null) {
      return AddressBookType.Personal;
    }
    
    AddressBook publicAb = findPublicAddressBookById(username, addressBookId);
    if (publicAb != null) {
      return AddressBookType.Public;
    }
    
    return null;
    


  }





  private AddressBook findPublicAddressBookById(String username, String addressBookId) {
    // TODO Auto-generated method stub
    return null;
  }


  private AddressBook findPersonalAddressBookById(String username, String addressBookId) {
    // TODO Auto-generated method stub
    return null;
  }


  public List<String> findEmailsInPersonalAddressBook(String username, String addressBookId) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSessionProvider();
      Node contactHome = getPersonalContactsHome(sProvider, username);
      QueryManager qm = contactHome.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer("/jcr:root" + contactHome.getPath()
          + "//element(*,exo:contact)[@exo:categories='").append(addressBookId).append("']");
      NodeIterator it = qm.createQuery(queryString.toString(), Query.XPATH).execute().getNodes();
      List<String> address = new ArrayList<String>();
      while (it.hasNext()) {
        Node contact = it.nextNode();
        if (contact.hasProperty("exo:emailAddress")
            && !Utils.isEmpty(contact.getProperty("exo:emailAddress").getString()))
          address.add(contact.getProperty("exo:emailAddress").getString());
      }
      return address;
    } finally {
      closeSessionProvider(sProvider);
    }
  }
  
  public List<String> findEmailsInPublicAddressBook(String username, String groupId) throws Exception {
    String usersPath = nodeHierarchyCreator_.getJcrPath(USERS_PATH) ;
    SessionProvider provider = SessionProvider.createSystemProvider();
    try {
    Node publicContactHome = getPublicContactsHome(provider) ;
    QueryManager qm = publicContactHome.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root" + usersPath 
                                                + "//element(*,exo:contact)[@exo:categories='")
                                                .append(groupId).append("']");                                                
    NodeIterator it = qm.createQuery(queryString.toString(), Query.XPATH).execute().getNodes();
    List<String> address = new ArrayList<String>();
    while (it.hasNext()){
      Node contact = it.nextNode();
      if(contact.hasProperty("exo:emailAddress") && !Utils.isEmpty(contact.getProperty("exo:emailAddress").getString()))
        address.add(contact.getProperty("exo:emailAddress").getString());
    }
    return address ;
    } finally {
      provider.close();
    }
  }
  
  public List<String> getAllEmailBySharedGroup(String username, String addressBookId) throws Exception {
    Node sharedAddressBookMock = getSharedAddressBooksHome(username) ;
    PropertyIterator iter = sharedAddressBookMock.getReferences() ;
    Node addressBook ;      
    while(iter.hasNext()) {
      addressBook = iter.nextProperty().getParent() ;
      if(addressBook.getName().equals(addressBookId)) {
        QueryManager qm = sharedAddressBookMock.getSession().getWorkspace().getQueryManager();
        StringBuffer queryString = new StringBuffer("/jcr:root" + addressBook.getParent().getParent().getNode(CONTACTS).getPath() 
                        + "//element(*,exo:contact)[(@exo:categories='").
                        append(addressBookId).append("')]") ;
        NodeIterator it = qm.createQuery(queryString.toString(), Query.XPATH).execute().getNodes();
        List<String> address = new ArrayList<String>();
        while (it.hasNext()){
          Node contact = it.nextNode();
          if(contact.hasProperty("exo:emailAddress") && !Utils.isEmpty(contact.getProperty("exo:emailAddress").getString()))
            address.add(contact.getProperty("exo:emailAddress").getString());
        }
        return address ;         
      } 
    }
    return null ;
  }
  
  private AddressBook toAddressBook(Node contactGroupNode) throws Exception {
    AddressBook contactGroup = new AddressBook();
    if (contactGroupNode.hasProperty("exo:id")) 
      contactGroup.setId(contactGroupNode.getProperty("exo:id").getString());
    if (contactGroupNode.hasProperty("exo:name")) 
      contactGroup.setName(contactGroupNode.getProperty("exo:name").getString());
    if (contactGroupNode.hasProperty("exo:description")) 
      contactGroup.setDescription(contactGroupNode.getProperty("exo:description").getString());
    if (contactGroupNode.hasProperty("exo:viewPermissionUsers"))
      contactGroup.setViewPermissionUsers(
          ValuesToStrings(contactGroupNode.getProperty("exo:viewPermissionUsers").getValues())) ;
    if (contactGroupNode.hasProperty("exo:editPermissionUsers"))
      contactGroup.setEditPermissionUsers(
          ValuesToStrings(contactGroupNode.getProperty("exo:editPermissionUsers").getValues())) ;
    
    if (contactGroupNode.hasProperty("exo:viewPermissionGroups"))
      contactGroup.setViewPermissionGroups(
          ValuesToStrings(contactGroupNode.getProperty("exo:viewPermissionGroups").getValues())) ;
    if (contactGroupNode.hasProperty("exo:editPermissionGroups"))
      contactGroup.setEditPermissionGroups(
          ValuesToStrings(contactGroupNode.getProperty("exo:editPermissionGroups").getValues())) ;
    return contactGroup;
  }

  public AddressBook loadPersonalAddressBook(String username, String groupId) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSessionProvider();
      if (groupId == null)
        return null;
      Node contactGroupHomeNode = getPersonalAddressBooksHome(sProvider, username);
      if (contactGroupHomeNode.hasNode(groupId))
        return toAddressBook(contactGroupHomeNode.getNode(groupId));
      return null;
    } finally {
      closeSessionProvider(sProvider);
    }
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

  public List<AddressBook> findPersonalAddressBooksByOwner(String username) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSessionProvider();
      Node addressBooksHome = getPersonalAddressBooksHome(sProvider, username);
      List<AddressBook> addressBooks = new ArrayList<AddressBook>();
      NodeIterator iter = addressBooksHome.getNodes();
      while (iter.hasNext()) {
        Node addressBook = iter.nextNode();
        addressBooks.add(toAddressBook(addressBook));
      }
      return addressBooks;
    } finally {
      closeSessionProvider(sProvider) ;
    }
  }

  public List<Contact> removeContacts(String username,
                                      List<String> contactIds) throws Exception {
    SessionProvider sp = null;
    try {
      sp = createSessionProvider();
      Node contactHomeNode = getPersonalContactsHome(sp, username);
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
    } finally {
      closeSessionProvider(sp);
    }
  }

  
  public void moveContacts(String username, List<Contact> contacts, String addressType ) throws Exception {
    SessionProvider sysProvider = null ;
    try {
      sysProvider = createSystemProvider() ;
      Node privateContactHome = getPersonalContactsHome(sysProvider, username);
      for(Contact contact : contacts) {
        if(addressType.equals(PERSONAL)) {        
          saveContact(username, contact, false) ;
        }else if(addressType.equals(SHARED)) {
         saveContactToSharedAddressBook(username, contact.getAddressBookIds()[0], contact, true) ;
         if (privateContactHome.hasNode(contact.getId()))
           privateContactHome.getNode(contact.getId()).remove() ;
        }         
      }
      if(privateContactHome.getSession().hasPendingChanges()) privateContactHome.getSession().save() ;
    } finally {
      closeSessionProvider(sysProvider) ;
    }
  }
  
  private List<String> getUserContactNodesByGroup(SessionProvider sProvider, String username, String groupId) throws Exception {
    Node contactHome = getPersonalContactsHome(sProvider, username);
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
   * Remove a personal addressBook. Does not clean the contacts it contains
   * @param username
   * @param addressBookId
   * @return
   * @throws Exception
   */
  public AddressBook removePersonalAddressBook(String username, String addressBookId) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSessionProvider();
      Node addressBooksHomeNode = getPersonalAddressBooksHome(sProvider, username);
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
    } finally {
      closeSessionProvider(sProvider);
    }
  }

  /**
   * Remove all contacts contained in an address book
   * @param username
   * @param addressBookId
   * @throws Exception
   */
  public void clearAddressBook(String username, String addressBookId) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSessionProvider();
      List<String> contactIds = getUserContactNodesByGroup(sProvider, username, addressBookId);
      removeContacts(username, contactIds);
    } finally {
      closeSessionProvider(sProvider);
    }
  }
  
  
  
  public void saveContact(String username, Contact contact, boolean isNew) throws Exception {
    SessionProvider sProvider = null;
    try {
      boolean systemPrivilege = false;
      if (systemPrivilege) {
        sProvider = createSessionProvider();  
      } else {
        sProvider = createSystemProvider();
      }
      
      Node contactHomeNode = getPersonalContactsHome(sProvider, username);
      contactToNode(contactHomeNode, contact, isNew);
      contactHomeNode.getSession().save();
    } finally {
      closeSessionProvider(sProvider);
    }
  }

  public void savePersonalOrSharedAddressBook(String username, AddressBook addressbook, boolean isNew) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSessionProvider();
      Node groupNode = null ;
      String id = addressbook.getId();
      if (isNew) {
        groupNode = getPersonalAddressBooksHome(sProvider, username).addNode(id, "exo:contactGroup");
        groupNode.setProperty("exo:id", id);
      } else {
        try {
          groupNode = getPersonalAddressBooksHome(sProvider, username).getNode(id);
        } catch (PathNotFoundException e) {
          Node sharedAddressBookMock = getSharedAddressBooksHome(username) ;
          PropertyIterator iter = sharedAddressBookMock.getReferences() ;
          Node addressBook ;      
          while(iter.hasNext()) {
            addressBook = iter.nextProperty().getParent() ;
            if(addressBook.getName().equals(id)) {
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
    } finally {
      closeSessionProvider(sProvider);
    } 
  }


  


  public void removeUserShareContact(String username, String contactId, String removedUser) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSystemProvider();
      Node contactNode ;
      String split = "/" ;    
      // shared contacts
      if (username.split(split).length > 1) {
        String usersPath = nodeHierarchyCreator_.getJcrPath(USERS_PATH) ;
        String temp = username.split(usersPath)[1] ;
        String userId = temp.split(split)[1] ;
        contactNode = getPersonalContactsHome(sProvider, userId).getNode(contactId);
      } else {
        contactNode = getPersonalContactsHome(sProvider, username).getNode(contactId);
      }
      List<String> values = new ArrayList<String>(
          Arrays.asList(ValuesToStrings(contactNode.getProperty(SHARED_PROP).getValues())));
      List<String> newValues = new ArrayList<String>(values) ;
      Node sharedContact = getSharedContact(removedUser) ;
      for (String value : values) {
        Node refNode = sharedContact.getSession().getNodeByUUID(value);
        if(refNode.getPath().equals(sharedContact.getPath())) {
          newValues.remove(value) ;
        }
      }
      try {
        String[] viewPer = ValuesToStrings(contactNode.getProperty("exo:viewPermissionUsers").getValues()) ;
        if (viewPer != null) {
          List<String> newViewPer = new ArrayList<String>() ;
          newViewPer.addAll(Arrays.asList(viewPer)) ;
          newViewPer.remove(removedUser + HYPHEN) ;
          contactNode.setProperty("exo:viewPermissionUsers", newViewPer.toArray(new String [] {})) ;      
          String[] editPer = ValuesToStrings(contactNode.getProperty("exo:editPermissionUsers").getValues()) ;
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
    } finally {
      closeSessionProvider(sProvider) ;
    }
  }
  
  public void unshareAddressBook(String username, String addressBookId, String removedUser) throws Exception {
    SessionProvider sysProvider = null;
    try {
      sysProvider = createSystemProvider();// current user may not be the owner, so we require a system provider
      Node addressBookNode = getPersonalAddressBooksHome(sysProvider, username).getNode(addressBookId);
      List<String> values = new ArrayList<String>(Arrays.asList(ValuesToStrings(addressBookNode.getProperty(SHARED_PROP)
                                                                                               .getValues())));
      List<String> newValues = new ArrayList<String>(values);

      Node sharedAddress = getSharedAddressBooksHome(sysProvider, removedUser);
      for (String value : values) {
        Node refNode = sharedAddress.getSession().getNodeByUUID(value);
        if (refNode.getPath().equals(sharedAddress.getPath())) {
          newValues.remove(value);
        }
      }

      String[] viewPer = null;
      try {
        viewPer = ValuesToStrings(addressBookNode.getProperty("exo:viewPermissionUsers")
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
          editPer = ValuesToStrings(addressBookNode.getProperty("exo:editPermissionUsers")
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

    } finally {
      closeSessionProvider(sysProvider);
    }
  }

  public void shareAddressBook(String username, String addressBookId, List<String> receiveUsers) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSessionProvider();
      Node addressBookNode = getPersonalAddressBooksHome(sProvider, username).getNode(addressBookId);
      Value[] values = {};
      if (addressBookNode.isNodeType(SHARED_MIXIN)) {
        values = addressBookNode.getProperty(SHARED_PROP).getValues();
      } else {
        addressBookNode.addMixin(SHARED_MIXIN);
        addressBookNode.setProperty("exo:sharedUserId", username);
      }

      List<Value> valueList = new ArrayList<Value>();
      for (String userId : receiveUsers) {
        Node sharedAddress = getSharedAddressBooksHome(userId.replaceFirst(JCRDataStorage.HYPHEN, ""));
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
    } finally {
      closeSessionProvider(sProvider);
    }
  }
  
  public void shareContact(String username, String[] contactIds, List<String> receiveUsers) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSystemProvider();
      for(String contactId : contactIds) {
        Node contactNode = getPersonalContactsHome(sProvider, username).getNode(contactId);
        Value[] values = {};
        if (contactNode.isNodeType(SHARED_MIXIN)) {     
          values = contactNode.getProperty(SHARED_PROP).getValues();
        } else {
          contactNode.addMixin(SHARED_MIXIN);          
          contactNode.setProperty("exo:sharedUserId", username) ;
        }
        List<Value> valueList = new ArrayList<Value>() ;
        for(String user : receiveUsers) {
          Node sharedContact = getSharedContact(user.replaceFirst(JCRDataStorage.HYPHEN, "")) ;
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
    } finally {
      closeSessionProvider(sProvider) ;
    }
  }
  
  public List<SharedAddressBook> findSharedAddressBooksByUser(String username) throws Exception {
    SessionProvider sysProvider = null;
    try {
      sysProvider = createSystemProvider(); // reading shared address books requires a system session
      List<SharedAddressBook> addressBooks = new ArrayList<SharedAddressBook>();
      Node sharedAddress = getSharedAddressBooksHome(sysProvider, username);
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
            sharedAddressBook.setEditPermissionUsers(ValuesToStrings(addressNode.getProperty("exo:editPermissionUsers")
                                                                                .getValues()));
          if (addressNode.hasProperty("exo:editPermissionGroups"))
            sharedAddressBook.setEditPermissionGroups(ValuesToStrings(addressNode.getProperty("exo:editPermissionGroups")
                                                                                 .getValues()));
          addressBooks.add(sharedAddressBook);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      return addressBooks;
    } finally {
      closeSessionProvider(sysProvider);
    }
  }
  
  
  // not use present ;
  /*public void removeSharedAddressBook(SessionProvider sProvider, String username, String addressBookId) throws Exception {
    Node sharedAddressBookMock = getSharedAddressBook(username) ;
    PropertyIterator iter = sharedAddressBookMock.getReferences() ;
    
    
    while(iter.hasNext()) {
      Node addressBook = iter.nextProperty().getParent() ;
      if(addressBook.getName().equals(addressBookId)) {
        addressBook.remove() ;
        sharedAddressBookMock.getSession().save() ;
        return ;
      }
    } 
  }
  */
  
  public void removeSharedContact(String username, String addressBookId, String contactId) throws Exception {
    /*
    Node sharedContactMock = getSharedContact(username) ;
    PropertyIterator iter1 = sharedContactMock.getReferences() ;
    while(iter1.hasNext()) {
      try{
        Node contactNode = iter1.nextProperty().getParent() ;
   
        if(contactNode.getName().equals(contactId)) {
          contactNode.remove() ;
        }
      }catch(Exception e){
        e.printStackTrace() ;
      }
    }
    sharedContactMock.getSession().save() ;
    */
    
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

  public Contact getSharedContact(String username, String contactId) throws Exception {
    /*Node sharedAddressBookHome = getSharedAddressBookHome(SessionProvider.createSystemProvider()) ;
    if(sharedAddressBookHome.hasNode(username)) {
      Node userNode = sharedAddressBookHome.getNode(username) ;
      PropertyIterator iter = userNode.getReferences() ;
      Node addressBook ;
      while(iter.hasNext()) {
        addressBook = iter.nextProperty().getParent() ;
        Node contacts = addressBook.getParent().getParent().getNode(CONTACTS) ;
        if(contacts.hasNode(contactId)) {
          return getContact(contacts.getNode(contactId), SHARED) ;
        }        
      }      
    }*/
    
    Node sharedContactMock = getSharedContact(username) ;
    PropertyIterator iter = sharedContactMock.getReferences() ;
    while(iter.hasNext()) {
      try{
        Node contactNode = iter.nextProperty().getParent() ;   
        if(contactNode.getName().equals(contactId)) {
          return getContact(contactNode, JCRDataStorage.SHARED) ;
        }
      }catch(Exception e){
        e.printStackTrace() ;
      }
    }
    return null ;
  }
  
  public DataPageList getSharedContacts(String username) throws Exception {
    List<Contact> sharedContacts = new ArrayList<Contact>() ;
    Node sharedContact = getSharedContact(username) ;      
    PropertyIterator iter = sharedContact.getReferences() ;
    while(iter.hasNext()) {
      try{
        Node contactNode = iter.nextProperty().getParent() ;
        sharedContacts.add(getContact(contactNode, SHARED)) ;
      }catch(Exception e){
        e.printStackTrace() ;
      }
    }
    return new DataPageList(sharedContacts, 10, null, false) ;
  }

  public void saveSharedContact(String username, Contact contact) throws Exception  {
    Node sharedContactMock = getSharedContact(username) ;
    PropertyIterator iter = sharedContactMock.getReferences() ;
    boolean isEdit = false ;
    while(iter.hasNext()) {
      try{
        Node contactNode = iter.nextProperty().getParent() ;
        if(contactNode.getName().equals(contact.getId())) {
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

  public void saveContactToSharedAddressBook(String username, String addressBookId, Contact contact, boolean isNew) throws Exception  {
    Node sharedAddressBookMock = getSharedAddressBooksHome(username) ;
    PropertyIterator iter = sharedAddressBookMock.getReferences() ;
    Node addressBook ;      
    while(iter.hasNext()) {
      addressBook = iter.nextProperty().getParent() ;
      if(addressBook.getName().equals(addressBookId)) {
        Node contactHomeNode = addressBook.getParent().getParent().getNode(CONTACTS) ;
        contact.setOwner(false) ;
        contactToNode(contactHomeNode, contact, isNew) ;
        contactHomeNode.getSession().save() ;   
        return ;
      }
    }      
  }
  
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
        if (Arrays.asList(ValuesToStrings(contactNode.getProperty(PROP_ADDRESSBOOK_REFS).getValues()))
            .contains(addressBook.getProperty("exo:id").getString())) return getContact(contactNode, JCRDataStorage.SHARED) ;
      } catch (PathNotFoundException e) { }
    }
    return null ;
  }
  
  public ContactPageList getSharedContactsByAddressBook(String username, SharedAddressBook addressBook) throws Exception {
    if (addressBook == null) return null ;
    SessionProvider sysProvider = SessionProvider.createSystemProvider();
    try {
      Node contactHome = getPersonalContactsHome(sysProvider, addressBook.getSharedUserId()) ;
      QueryManager qm = contactHome.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer("/jcr:root" + contactHome.getPath() 
                                                  + "//element(*,exo:contact)[(@exo:categories='").
                                                  append(addressBook.getId()).append("')]")
                                                  .append(" order by @exo:fullName,@exo:id ascending");
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      return new ContactPageList(username, result.getNodes(), 10, queryString.toString(), true, SHARED) ;
    } finally {
      sysProvider.close();
    }
  }
  


  public ContactPageList getPublicContactsByAddressBook(String groupId) throws Exception {   
    String usersPath = nodeHierarchyCreator_.getJcrPath(USERS_PATH) ;
    SessionProvider sysProvider = SessionProvider.createSystemProvider();
    try {
    Node contactHome = getPublicContactsHome(sysProvider);
    QueryManager qm = contactHome.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root" + usersPath 
                                                + "//element(*,exo:contact)[@exo:categories='")
                                                .append(groupId).append("' and @exo:isOwner='true'] ")
                                                .append("order by @exo:fullName,@exo:id ascending");
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    return new ContactPageList(null, result.getNodes(), 10, queryString.toString(), true, PUBLIC) ;
    } finally {
      sysProvider.close();
    }
  }
  
  public List<GroupContactData> getPublicContacts(SessionProvider sysProvider, String[] groupIds) throws Exception {
    List<GroupContactData> contactByGroup = new ArrayList<GroupContactData>() ;
    List<Contact> contacts;
    for(String groupId : groupIds) { 
      contacts = getPublicContactsByAddressBook(groupId).getAll();
      if(contacts.size() > 0)
        contactByGroup.add(new GroupContactData(groupId, contacts));     
    }
    return contactByGroup;
  }
  
  public List<String> getPublicAddressBookContacts(SessionProvider sysProvider, String[] groupIds) throws Exception {
    List<String> groups = new ArrayList<String>();
    for(String groupId : groupIds) { 
      if(hasContacts(sysProvider, groupId))  groups.add(groupId) ; 
    }
    return groups;
  }
  
  private boolean hasContacts(SessionProvider sysProvider, String groupId) throws Exception {
    Node contactHome = getPublicContactsHome(sysProvider);
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
  
  // don't use ?
  /*public Contact removePublicContact(SessionProvider sysProvider, String contactId) throws Exception {
    Node contactHomeNode = getPublicContactHome(sysProvider);
    if (contactHomeNode.hasNode(contactId)) {
      Contact contact = getPublicContact(contactId);
      contactHomeNode.getNode(contactId).remove();
      contactHomeNode.getSession().save();
      return contact;
    }
    return null;
  }*/
/*
  public void savePublicContact(Contact contact, boolean isNew) throws Exception {
    Node contactHomeNode = getUserContactHome(SessionProvider.createSystemProvider(), contact.getOwnerId()) ;
    saveContact(contactHomeNode, contact, isNew) ;
    contactHomeNode.getSession().save(); 
  }
  */
  public void addUserContactInAddressBook(String userId, String addressBookId) throws Exception {
    SessionProvider provider = SessionProvider.createSystemProvider();
    try {
      Node contactHome = getPersonalContactsHome(provider, userId);
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
    } finally {
      provider.close();
    }
  }
  
  private void reparePermissions(Node node, String owner) throws Exception {
    /*ExtendedNode extNode = (ExtendedNode)node ;
    if (extNode.canAddMixin("exo:privilegeable")) extNode.addMixin("exo:privilegeable");
    String[] arrayPers = {PermissionType.READ, PermissionType.ADD_NODE, PermissionType.SET_PROPERTY, PermissionType.REMOVE} ;
    extNode.setPermission(owner, arrayPers) ;
    List<AccessControlEntry> permsList = extNode.getACL().getPermissionEntries() ;    
    for(AccessControlEntry accessControlEntry : permsList) {
      extNode.setPermission(accessControlEntry.getIdentity(), arrayPers) ;      
    } 
    extNode.removePermission("any") ;*/
    
  }
  
  private void contactToNode(Node contactsHome, Contact contact, boolean isNew) throws Exception {
    Node contactNode;
    if (isNew) {
      contactNode = contactsHome.addNode(contact.getId(), "exo:contact"); 
      contactNode.setProperty("exo:id", contact.getId());
      if(contact.isOwner()) {
        contactNode.setProperty("exo:isOwner", true) ;
        contactNode.setProperty("exo:ownerId", contact.getOwnerId()) ;
        reparePermissions(contactsHome, contact.getOwnerId()) ;
        reparePermissions(contactNode, contact.getOwnerId()) ;
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
    } else try { contactNode.getProperty("exo:birthday").remove() ; } catch (PathNotFoundException e) {} // cs-2021
    
    contactNode.setProperty("exo:jobTitle", contact.getJobTitle());
    contactNode.setProperty("exo:emailAddress", contact.getEmailAddress());
    
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
  /*private void saveSharedGroup(SessionProvider sysProvider, ContactGroup group, boolean isNew) throws Exception  {
    Node sharedGroupHomeNode = getPublicContactGroupHome(sysProvider);
    Node groupNode;
    if (isNew) {
      groupNode = sharedGroupHomeNode.addNode(group.getId(), "exo:contactGroup");
      groupNode.setProperty("exo:id", "contactGroup" + group.getId());
    } else {
      groupNode = sharedGroupHomeNode.getNode(group.getId());
    }
    groupNode.setProperty("exo:name", group.getName());
    groupNode.setProperty("exo:description", group.getDescription());
    sharedGroupHomeNode.getSession().save();
  } */
  
  private Tag getTag(Node tagNode) throws Exception {
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
  
  public void updateTag(String username,Tag tag) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSessionProvider() ;
    Node tagHome = getTagsHome(sProvider, username) ;
    Node tagNode = tagHome.getNode(tag.getId());
    tagNode.setProperty("exo:name", tag.getName());
    tagNode.setProperty("exo:description", tag.getDescription());
    tagNode.setProperty("exo:color", tag.getColor());
    tagHome.save();
    } finally {
      closeSessionProvider(sProvider) ;
    }
  }
  
  
  public Tag getTag(String username, String tagId) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSystemProvider();
      Node tagHomeNode = getTagsHome(sProvider, username);
      if (tagHomeNode.hasNode(tagId)) 
        return getTag(tagHomeNode.getNode(tagId));
      return null ;
    } finally {
      closeSessionProvider(sProvider) ;
    }
  }
  
  public List<Tag> getTags(String username) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSystemProvider();
    Node tagHomeNode = getTagsHome(sProvider, username);
    List<Tag> tags = new ArrayList<Tag>();
    NodeIterator iter = tagHomeNode.getNodes();
    while (iter.hasNext()) {
      Node tagNode = iter.nextNode();
      tags.add(getTag(tagNode));
    }
    return tags;
    } finally {
      closeSessionProvider(sProvider) ; 
    }    
  }

  public DataPageList getContactPageListByTag(String username, String tagId) throws Exception {
    SessionProvider sysProvider = SessionProvider.createSystemProvider();
    try {
      //query on private contacts
      Node contactHome = getPersonalContactsHome(sysProvider, username);
      QueryManager qm = contactHome.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer("/jcr:root" + contactHome.getPath() 
                                                  + "//element(*,exo:contact)[@exo:tags='").
                                                  append(tagId).
                                                  append("']");
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator it = result.getNodes();    
      Map<String, Contact> contacts = new LinkedHashMap<String, Contact>() ;
      while (it.hasNext()) {
        Contact contact = getContact(it.nextNode(), PERSONAL) ;
        contacts.put(contact.getId(), contact);
      }
      
      //query on public contacts
      String usersPath = nodeHierarchyCreator_.getJcrPath(USERS_PATH) ;
      
      Node publicContactHome = getPublicContactsHome(sysProvider);
      qm = publicContactHome.getSession().getWorkspace().getQueryManager();
      queryString = new StringBuffer("/jcr:root" + usersPath 
                                                  + "//element(*,exo:contact)[@exo:tags='")
                                                  .append(tagId).append("' and @exo:isOwner='true'] ") ;
      query = qm.createQuery(queryString.toString(), Query.XPATH);
      result = query.execute();
      it = result.getNodes();    
      while (it.hasNext()) {
        Contact contact = getContact(it.nextNode(), PUBLIC);
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
                Arrays.asList(ValuesToStrings(contactNode.getProperty("exo:tags").getValues())).contains(tagId)) {
              Contact contact = getContact(contactNode, SHARED) ;
              contacts.put(contact.getId(), contact) ;
            }
          }catch(Exception e){
            e.printStackTrace() ;
          }
        }
      } catch (PathNotFoundException e) { }
  
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
        query = qm.createQuery(queryString.toString(), Query.XPATH);
        result = query.execute();
        it = result.getNodes();    
        while (it.hasNext()) {
          Contact contact = getContact(it.nextNode(), SHARED);
          if (Arrays.asList(contact.getAddressBookIds()).contains(addressBook.getProperty("exo:id").getString()))
            contacts.put(contact.getId(), contact) ;
        }
      }
      return new DataPageList(Arrays.asList(contacts.values().toArray(new Contact[] {})), 10, null, false) ;
    } finally {
      sysProvider.close();
    }
  }
  
  public void addTag(String username, List<String> contactIds, String tagId) throws Exception {
    Map<String, String> tagMap = new HashMap<String, String> () ;
    tagMap.put(tagId, tagId) ;
    SessionProvider sysProvider = SessionProvider.createSystemProvider();
    try {
      for(String contact : contactIds) {  
        Node contactNode = null ;
        String contactId = contact.split(SPLIT)[0] ;
        String contactType = contact.split(SPLIT)[1] ;
        if (contactType.equals(PERSONAL)) {
          contactNode = getPersonalContactsHome(sysProvider, username).getNode(contactId) ;
        } else if (contactType.equals(PUBLIC)) {
          contactNode = getPersonalContactsHome(sysProvider, contactId).getNode(contactId);
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
                if (!Arrays.asList(ValuesToStrings(contactNode.getProperty(PROP_ADDRESSBOOK_REFS).getValues()))
                    .contains(addressBook.getProperty("exo:id").getString())) contactNode = null ;
                break ;
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
    } finally {
     if (sysProvider != null) sysProvider.close();
    }
  }

  public void addTag(String username, List<String> contactIds, List<Tag> tags) throws Exception {
    SessionProvider sysProvider = SessionProvider.createSystemProvider();
    Node tagHomeNode = getTagsHome(sysProvider, username);
    Map<String, String> tagMap = new HashMap<String, String> () ;
    String newTag = null ;
    try {    
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
          contactNode = getPersonalContactsHome(sysProvider, username).getNode(contactId) ;
        } else if (contactType.equals(PUBLIC)) {
          
          contactNode = getPersonalContactsHome(sysProvider, contactId).getNode(contactId);
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
                if (!Arrays.asList(ValuesToStrings(contactNode.getProperty(PROP_ADDRESSBOOK_REFS).getValues()))
                    .contains(addressBook.getProperty("exo:id").getString())) contactNode = null ;      
                break ;
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
    } finally {
      if (sysProvider != null) sysProvider.close();
    }
  }

  private void removeTagInContacts (NodeIterator it, String tagId) throws Exception {
    while (it.hasNext()) {
      Node contactNode = it.nextNode() ;
      if (contactNode.hasProperty("exo:tags")) {
        String[] tagIds = ValuesToStrings(contactNode.getProperty("exo:tags").getValues()) ;
        List<String> newTagIds = new ArrayList<String>() ;
        for (String id : tagIds) 
          if (!id.equals(tagId)) newTagIds.add(id) ;
        contactNode.setProperty("exo:tags", newTagIds.toArray(new String[]{})) ;
        contactNode.save() ;
      }
    }
  }
  
  public Tag removeTag(String username, String tagId) throws Exception {
    SessionProvider sysProvider = SessionProvider.createSystemProvider();
    try {
      Node tagHomeNode = getTagsHome(sysProvider, username);
      Node tagNode = tagHomeNode.getNode(tagId) ;
      Tag tag = getTag(tagNode) ;
      tagNode.remove();
      tagHomeNode.save() ;
      
      // remove tagId in contact property       
      // query on private contacts
        Node contactHome = getPersonalContactsHome(sysProvider, username);
        QueryManager qm = contactHome.getSession().getWorkspace().getQueryManager();
        StringBuffer queryString = new StringBuffer("/jcr:root" + contactHome.getPath() 
                                                    + "//element(*,exo:contact)[@exo:tags='").
                                                    append(tagId).append("']");
        Query query = qm.createQuery(queryString.toString(), Query.XPATH);
        removeTagInContacts(query.execute().getNodes(), tagId) ;
        
        //query on public contacts
        String usersPath = nodeHierarchyCreator_.getJcrPath(USERS_PATH) ;
        try {
        Node publicContactHome = getPublicContactsHome(sysProvider);
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
                String[] tagIds = ValuesToStrings(contactNode.getProperty("exo:tags").getValues()) ;
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
    } finally {
      closeSessionProvider(sysProvider) ;
    }
  }
  
  public void removeContactTag(String username, List<String> contactIds, List<String> tags) throws Exception { 
    SessionProvider sysProvider = SessionProvider.createSystemProvider();
    try {
      for(String contact : contactIds) {
        Node contactNode = null ;
        String contactId = contact.split(SPLIT)[0] ;
        String contactType = contact.split(SPLIT)[1] ;
        if (contactType.equals(PERSONAL)) {
          contactNode = getPersonalContactsHome(sysProvider, username).getNode(contactId) ;
        } else if (contactType.equals(PUBLIC)) {
          
          contactNode = getPersonalContactsHome(sysProvider, contactId).getNode(contactId);
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
                if (!Arrays.asList(ValuesToStrings(contactNode.getProperty(PROP_ADDRESSBOOK_REFS).getValues()))
                    .contains(addressBook.getProperty("exo:id").getString())) contactNode = null ;
                break ;
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
    } finally {
      if (sysProvider != null) sysProvider.close();
    }
  }
 
  public DataPageList searchContact(String username, ContactFilter filter)throws Exception {
    Map<String, Contact> contacts = new LinkedHashMap<String, Contact>() ;
    filter.setUsername(username) ;
    SessionProvider sysProvider = SessionProvider.createSystemProvider();
    try {      
      QueryManager qm = null ;
      Query query = null ;
      String usersPath = nodeHierarchyCreator_.getJcrPath(USERS_PATH) ;
      //public contacts
      if (filter.getType() == null || filter.getType().equals(PUBLIC)) {
        Node publicContactHome = getPublicContactsHome(sysProvider) ; 
        usersPath = nodeHierarchyCreator_.getJcrPath(USERS_PATH) ;
        filter.setAccountPath(usersPath) ;
        // minus shared contacts
        filter.setOwner("true") ; 
        qm = publicContactHome.getSession().getWorkspace().getQueryManager() ;
        query = qm.createQuery(filter.getStatement(), Query.XPATH) ;
        NodeIterator itpublic = query.execute().getNodes();
        while(itpublic.hasNext()) {
          Contact contact = getContact(itpublic.nextNode(), PUBLIC) ;
          contacts.put(contact.getId(), contact) ;
        }
      }
    filter.setOwner(null) ;
    if (filter.getType() == null || filter.getType().equals(PERSONAL)) {
      // private contacts
      if(username != null && username.length() > 0) {
        Node contactHome = getPersonalContactsHome(sysProvider, username) ;
        filter.setAccountPath(contactHome.getPath()) ;      
        qm = contactHome.getSession().getWorkspace().getQueryManager() ;
        query = qm.createQuery(filter.getStatement(), Query.XPATH) ;      
        NodeIterator it = query.execute().getNodes() ; 
        while(it.hasNext()) {
          Contact contact = getContact(it.nextNode(), PERSONAL) ;
          contacts.put(contact.getId(), contact) ; 
          
          // need test remove usershare contact 
  /*        String[] adds = contact.getAddressBook() ;
          if (adds == null || (adds.length == 1 && !adds[0].contains("ContactGroup"))) {
            List<String> contactIds = new ArrayList<String>() ;
            contactIds.add(contact.getId()) ;
            removeContacts(sysProvider, username, contactIds) ;
          } else {
            contacts.put(contact.getId(), contact) ;          
          }*/
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
            
            qm = sharedContactHomeNode.getSession().getWorkspace().getQueryManager() ;      
            query = qm.createQuery(filter.getStatement(), Query.XPATH) ;
            NodeIterator it = query.execute().getNodes() ;
            while(it.hasNext()) {
              Node contactNode = it.nextNode() ;
              if (sharedContactIds.contains(contactNode.getProperty("exo:id").getString())) {
                Contact contact = getContact(contactNode, SHARED) ;
                
                // add
                if (!contacts.containsKey(contact.getId())) {
                  contacts.put(contact.getId(), contact) ;
                } else {
                  contacts.get(contact.getId()).setContactType(JCRDataStorage.SHARED) ;
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
          qm = contactHomeNode.getSession().getWorkspace().getQueryManager() ;
          query = qm.createQuery(filter.getStatement(), Query.XPATH) ;  
          NodeIterator it = query.execute().getNodes() ;
          while(it.hasNext()) {
            Contact contact = getContact(it.nextNode(), SHARED) ;
            contacts.put(contact.getId(), contact) ;
          }
        }
      }
    }
    List<Contact> contactList = new ArrayList<Contact>() ;
    contactList.addAll(contacts.values()) ;    
    return new DataPageList(contactList, 10, null, false) ;
    } finally {
     if (sysProvider != null) sysProvider.close();
    }
  }
  
  public Map<String, String> findEmailsByFilter(String username, ContactFilter filter)throws Exception {
    Map<String, String> emails = new LinkedHashMap<String, String>() ;
    filter.setUsername(username) ;
    filter.setHasEmails(true); // contact must have emails
    SessionProvider sysProvider = SessionProvider.createSystemProvider();
    try {
    //public contacts
    Node publicContactHome = getPublicContactsHome(sysProvider) ; 
    String usersPath = nodeHierarchyCreator_.getJcrPath(USERS_PATH) ;
    filter.setAccountPath(usersPath) ;
    // minus shared contacts
    filter.setOwner("true") ; 
    QueryManager qm = publicContactHome.getSession().getWorkspace().getQueryManager() ;
    Query query = qm.createQuery(filter.getStatement(), Query.XPATH) ; // TODO add criteria on emailAddress not null to lower number of results
    NodeIterator itpublic = query.execute().getNodes();
    while(itpublic.hasNext()) {
      Node contactNode = itpublic.nextNode() ;
      //if (contactNode.hasProperty("exo:emailAddress"))
        feedEmailResult(emails, contactNode);
    }
    filter.setOwner(null) ;

    // private contacts
    if(username != null && username.length() > 0) {
      Node contactHome = getPersonalContactsHome(sysProvider, username) ;
      filter.setAccountPath(contactHome.getPath()) ;      
      qm = contactHome.getSession().getWorkspace().getQueryManager() ;
      query = qm.createQuery(filter.getStatement(), Query.XPATH) ;// TODO add criteria on emailAddress not null to lower number of results
      NodeIterator it = query.execute().getNodes() ;
      while(it.hasNext()) {
        Node contactNode = it.nextNode() ;
        //if (contactNode.hasProperty("exo:emailAddress"))
          feedEmailResult(emails, contactNode);        
      }
    }

    //share contacts
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
          query = qm.createQuery(filter.getStatement(), Query.XPATH) ;// TODO add criteria on emailAddress not null to lower number of results
          NodeIterator it = query.execute().getNodes() ;
          while(it.hasNext()) {
            Node contactNode = it.nextNode() ;
            //if (contactNode.hasProperty("exo:emailAddress"))
              feedEmailResult(emails, contactNode);  
          }
        }catch(Exception e){
          e.printStackTrace() ;
        }
      }
    } catch (PathNotFoundException e) { }
    Node sharedAddressBookMock = getSharedAddressBooksHome(sysProvider, username) ;
    PropertyIterator iter = sharedAddressBookMock.getReferences() ;
    Node addressBook ;
    
//  add if to fix bug 1407
    boolean hasGroup = (filter.getCategories() != null && filter.getCategories().length > 0) ; 
    while(iter.hasNext()) {
      addressBook = iter.nextProperty().getParent() ;
      Node contactHomeNode = addressBook.getParent().getParent().getNode(CONTACTS) ;
      filter.setAccountPath(contactHomeNode.getPath()) ;
      if (!hasGroup) filter.setCategories(new String[] {addressBook.getName()}) ;
      filter.setUsername(addressBook.getProperty("exo:sharedUserId").getString()) ;
      qm = contactHomeNode.getSession().getWorkspace().getQueryManager() ;      
      query = qm.createQuery(filter.getStatement(), Query.XPATH) ;  // TODO add criteria on emailAddress not null to lower number of results
      NodeIterator it = query.execute().getNodes() ;
      while(it.hasNext()) {
        Node contactNode = it.nextNode() ;
        //if (contactNode.hasProperty("exo:emailAddress"))
          feedEmailResult(emails, contactNode); 
      }
    }
    return emails ;
    } finally {
      if (sysProvider != null) sysProvider.close();
    }
  }


  private void feedEmailResult(Map<String, String> emails, Node contactNode) throws Exception {
    String id = contactNode.getProperty("exo:id").getString();
    String fullName = contactNode.getProperty("exo:fullName").getString() ;
    String emailAddresses = contactNode.getProperty("exo:emailAddress").getString();
    emails.put(id, fullName + Utils.SPLIT + emailAddresses) ;
  }
  
  
  // no public ;
  private void copyNodes(SessionProvider sProvider, String username,Node srcHomeNode, NodeIterator iter, String destAddress, String destType ) throws Exception {
    SessionProvider sysProvider = SessionProvider.createSystemProvider();
    try {
    if (destType.equals(PERSONAL)) {        
      Node contactHomeNode = getPersonalContactsHome(sProvider, username);
      while (iter.hasNext()) {
        Node oldNode = iter.nextNode() ;
        String newId = "Contact" + IdGenerator.generate() ;
        try {
          contactHomeNode.getSession().getWorkspace().copy(oldNode.getPath(), contactHomeNode.getPath() + "/" + newId) ;        
        } catch (AccessDeniedException ex) {
          Node userContactHome = getPersonalContactsHome(sysProvider, oldNode.getProperty("exo:id").getString()) ;
          userContactHome.getSession().getWorkspace().copy(oldNode.getPath(), contactHomeNode.getPath() + "/" + newId) ; 
        }        
        ExtendedNode extNode ;
        try{
          extNode = (ExtendedNode)contactHomeNode.getNode(newId) ;
        }catch (Exception e) {          
          extNode = (ExtendedNode)getPersonalContactsHome(sysProvider, username).getNode(newId) ;
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

        //newNode.setProperty("exo:tags", new String [] {}) ;
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
    } finally {
      if (sysProvider != null) sysProvider.close();
    }
  }
  public void pasteAddressBook(String username, String srcAddress, String srcType, String destAddress, String destType) throws Exception {
    SessionProvider sysProvider = SessionProvider.createSystemProvider();
    try {
      if (srcType.equals(PERSONAL)) {
        Node contactHome = getPersonalContactsHome(sysProvider, username);
        QueryManager qm = contactHome.getSession().getWorkspace().getQueryManager();
        StringBuffer queryString = new StringBuffer("/jcr:root" + contactHome.getPath() 
                                                    + "//element(*,exo:contact)[@exo:categories='").
                                                    append(srcAddress).
                                                    append("']");
        Query query = qm.createQuery(queryString.toString(), Query.XPATH);
        QueryResult result = query.execute();
        NodeIterator iter = result.getNodes() ;
        copyNodes(sysProvider, username, contactHome, iter, destAddress, destType) ;      
      } else if (srcType.equals(SHARED)) {
        Node sharedAddressBookMock = getSharedAddressBooksHome(username) ;
        PropertyIterator proIter = sharedAddressBookMock.getReferences() ;
        Node addressBook ;      
        while(proIter.hasNext()) {
          addressBook = proIter.nextProperty().getParent() ;
          if(addressBook.getName().equals(srcAddress)) {
            Node contactHomeNode = addressBook.getParent().getParent().getNode(CONTACTS) ;
            QueryManager qm = contactHomeNode.getSession().getWorkspace().getQueryManager();
            StringBuffer queryString = new StringBuffer("/jcr:root" + contactHomeNode.getPath() 
                                                        + "//element(*,exo:contact)[@exo:categories='").
                                                        append(srcAddress).
                                                        append("']");
            Query query = qm.createQuery(queryString.toString(), Query.XPATH);
            QueryResult result = query.execute();
            NodeIterator iter = result.getNodes() ;
            copyNodes(sysProvider, username, contactHomeNode, iter, destAddress, destType) ;
            break ;          
          }
        }          
      } else {
        String usersPath = nodeHierarchyCreator_.getJcrPath(USERS_PATH) ;
        Node publicContactHome = getPublicContactsHome(sysProvider);
        QueryManager qm = publicContactHome.getSession().getWorkspace().getQueryManager();
        StringBuffer queryString = new StringBuffer("/jcr:root" + usersPath
                                                    + "//element(*,exo:contact)[@exo:categories='")
                                                    .append(srcAddress).append("']") ;
        Query query = qm.createQuery(queryString.toString(), Query.XPATH);
        QueryResult result = query.execute();
        NodeIterator iter = result.getNodes() ;
        copyNodes(sysProvider, username, publicContactHome, iter, destAddress, destType) ;        
      }
    } finally {
      sysProvider.close();
    }
  }
  
  
  public List<Contact> pasteContacts(String username, String destAddress, String destType,  Map<String, String> contactsMap) throws Exception {
    SessionProvider sProvider = null ;
    try {
      sProvider = createSystemProvider() ;    
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
        }        
        if (contact != null) contacts.add(contact) ; 
      }    
      
      for (Contact contact : contacts) {
        if (destType.equals(PERSONAL)) {
          Node contactHomeNode = getPersonalContactsHome(sProvider, username);
          pastedContacts.add(getContact(saveCopyContact(contactHomeNode, contact, destAddress, destType), destType)) ; 
        } else if (destType.equals(SHARED)) {
          Node sharedAddressBookMock = getSharedAddressBooksHome(username) ;
          PropertyIterator iter = sharedAddressBookMock.getReferences() ;
          Node addressBook ;      
          while(iter.hasNext()) {
            addressBook = iter.nextProperty().getParent() ;
            if(addressBook.getName().equals(destAddress)) {
              Node contactHomeNode = addressBook.getParent().getParent().getNode(CONTACTS) ;
              pastedContacts.add(getContact(saveCopyContact(contactHomeNode, contact, destAddress, destType), destType)) ;   
              break ;
            }
          }
        }       
      }
      return pastedContacts ;
    }finally {
      closeSessionProvider(sProvider) ;
    }
  }
  
  private Node saveCopyContact(Node contactHomeNode, Contact contact, String destAddress, String destType) throws Exception {
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
    contactNode.setProperty("exo:emailAddress", contact.getEmailAddress());
    
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
    /*contactNode.setProperty("exo:editPermissionUsers", contact.getEditPermissionUsers());
    contactNode.setProperty("exo:viewPermissionUsers", contact.getViewPermissionUsers());    
    contactNode.setProperty("exo:editPermissionGroups", contact.getEditPermissionGroups());
    contactNode.setProperty("exo:viewPermissionGroups", contact.getViewPermissionGroups());*/

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
    SessionProvider sysProvider = createSystemProvider() ;
    try {
      if(isNew) {
        AddressBook addressbook = new AddressBook() ;
        addressbook.setId(NewUserListener.DEFAULTGROUP+user.getUserName()) ;
        addressbook.setName(NewUserListener.DEFAULTGROUPNAME) ;
        addressbook.setDescription(NewUserListener.DEFAULTGROUPDES) ;
        Node groupNode = getPersonalAddressBooksHome(sysProvider, user.getUserName()).addNode(addressbook.getId(), "exo:contactGroup");
        groupNode.setProperty("exo:id", addressbook.getId()); 
        groupNode.setProperty("exo:name", addressbook.getName());
        groupNode.setProperty("exo:description", addressbook.getDescription());
        groupNode.setProperty("exo:editPermissionUsers", addressbook.getEditPermissionUsers()) ;
        groupNode.setProperty("exo:viewPermissionUsers", addressbook.getViewPermissionUsers()) ;
        groupNode.setProperty("exo:editPermissionGroups", addressbook.getEditPermissionGroups()) ;
        groupNode.setProperty("exo:viewPermissionGroups", addressbook.getViewPermissionGroups()) ;
        groupNode.getSession().save() ;
        
        // save contact
        contact.setId(user.getUserName()) ;
        Map<String, String> groupIds = new LinkedHashMap<String, String>() ;
        groupIds.put(addressbook.getId(), addressbook.getId()) ;   
        ExoContainer container = ExoContainerContext.getCurrentContainer();
        OrganizationService organizationService = 
          (OrganizationService)container.getComponentInstanceOfType(OrganizationService.class) ;
        Object[] groupsOfUser = organizationService.getGroupHandler().findGroupsOfUser(user.getUserName()).toArray() ;
        for (Object object : groupsOfUser) {
          String id = ((GroupImpl)object).getId() ;
          groupIds.put(id, id) ;
        }
        
        contact.setAddressBookIds(groupIds.keySet().toArray(new String[] {})) ;
        contact.setOwner(true) ;
        contact.setOwnerId(user.getUserName()) ;
        saveContact(user.getUserName(), contact, true) ;

        JCRDataStorage storage_ = new JCRDataStorage(nodeHierarchyCreator_) ;
        Node publicContactHome = storage_.getPublicContactsHome(sysProvider) ;      
        String usersPath = nodeHierarchyCreator_.getJcrPath(JCRDataStorage.USERS_PATH) ;
        QueryManager qm = publicContactHome.getSession().getWorkspace().getQueryManager();
        List<String> recievedUser = new ArrayList<String>() ;
        recievedUser.add(user.getUserName()) ;

        
        for (Object object : groupsOfUser) {  
          String groupId = ((GroupImpl)object).getId() ;
          // get all address books that current user can see thank to his groups
          StringBuffer queryString = new StringBuffer("/jcr:root" + usersPath 
              + "//element(*,exo:contactGroup)[@exo:viewPermissionGroups='").append(groupId + "']") ;        
          Query query = qm.createQuery(queryString.toString(), Query.XPATH);
          QueryResult result = query.execute();
          NodeIterator nodes = result.getNodes() ;
          while (nodes.hasNext()) {
            Node addressBook = nodes.nextNode() ;
            //share between adressbook owner and current user
            storage_.shareAddressBook(addressBook.getProperty("exo:sharedUserId")
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
            storage_.shareContact(userId,
                new String[] {contactNode.getProperty("exo:id").getString()}, recievedUser) ;
          }
        }
        Node userApp = nodeHierarchyCreator_.getUserApplicationNode(sysProvider, user.getUserName()) ;
        //reparePermissions(userApp, user.getUserName()) ;
        //reparePermissions(userApp.getNode("ContactApplication"), user.getUserName()) ;
        //reparePermissions(userApp.getNode("ContactApplication/contactGroup"), user.getUserName()) ;
        //reparePermissions(userApp.getNode("ContactApplication/contactGroup/" + group.getId()), user.getUserName()) ;
        userApp.getSession().save() ;   
      } else {
        if (contact != null) {
          saveContact(user.getUserName(), contact, false) ; 
        }
      }
    } catch (Exception e) {
      e.printStackTrace() ;
    } finally {
      closeSessionProvider(sysProvider);
    }
  }  
  
  
  /**
   * Create a session provider for current context. The method first try to get a normal session provider, 
   * then attempts to create a system provider if the first one was not available.
   * @return a SessionProvider initialized by current SessionProviderService
   * @see SessionProviderService#getSessionProvider(null)
   */
  private SessionProvider createSessionProvider() {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    SessionProviderService service = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class);
    SessionProvider provider = service.getSessionProvider(null);
    if (provider == null) {
      log.info("No user session provider was available, using a system session provider");
      provider = service.getSystemSessionProvider(null);
    }
    return provider;
  }
  
  private SessionProvider createUserProvider() {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    SessionProviderService service = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class);
    return service.getSessionProvider(null) ;    
  }  
  
  private SessionProvider createSystemProvider() {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    SessionProviderService service = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class);
    return service.getSystemSessionProvider(null) ;    
  }
  

  /**
   * Safely closes JCR session provider. Call this method in finally to clean any provider initialized by createSessionProvider()
   * @param sessionProvider the sessionProvider to close
   * @see SessionProvider#close();
   */
  private void closeSessionProvider(SessionProvider sessionProvider) {
    if (sessionProvider != null) {
      sessionProvider.close();
    }
  }
  
 
  
}
