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
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyIterator;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactAttachment;
import org.exoplatform.contact.service.ContactFilter;
import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.contact.service.ContactPageList;
import org.exoplatform.contact.service.DataPageList;
import org.exoplatform.contact.service.GroupContactData;
import org.exoplatform.contact.service.Tag;
import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.jcr.util.IdGenerator;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 10, 2007  
 */
public class JCRDataStorage{
  
  final private static String CONTACTS = "contacts".intern() ;
  final private static String ADDRESS_BOOK = "contactGroup".intern() ;
  final private static String TAGS = "tags".intern() ;
  final private static String NT_UNSTRUCTURED = "nt:unstructured".intern() ;
  final private static String CONTACT_APP = "ContactApplication".intern() ;
  final private static String SHARED_CONTACT = "SharedContact".intern() ;
  final private static String SHARED_MIXIN = "exo:contactShared".intern();
  final private static String SHARED_PROP = "exo:sharedId".intern();
  final public static String PRIVATE = "0".intern();
  final public static String SHARED = "1".intern();
  final public static String PUBLIC = "2".intern();
  private NodeHierarchyCreator nodeHierarchyCreator_ ;
  
  public JCRDataStorage(NodeHierarchyCreator nodeHierarchyCreator)throws Exception {
    nodeHierarchyCreator_ = nodeHierarchyCreator ;
  }  
  
  private Node getUserContactServiceHome(SessionProvider sProvider, String username) throws Exception {
    Node userApp = nodeHierarchyCreator_.getUserApplicationNode(sProvider, username)  ;
  	try {
      return  userApp.getNode(CONTACT_APP) ;
    } catch (PathNotFoundException ex) {
      Node userHome = userApp.addNode(CONTACT_APP, NT_UNSTRUCTURED) ;
      userApp.getSession().save() ;
      return userHome ;
    }  	
  }
  
  private Node getPublicContactServiceHome(SessionProvider sProvider) throws Exception {
    Node userApp = nodeHierarchyCreator_.getPublicApplicationNode(sProvider) ;
    try {
      return  userApp.getNode(CONTACT_APP) ;
    } catch (PathNotFoundException ex) {
      Node publicHome = userApp.addNode(CONTACT_APP, NT_UNSTRUCTURED) ;
      userApp.save() ;
      return publicHome ;
    }
  }
  
  private Node getUserContactHome(SessionProvider sProvider, String username) throws Exception {
    Node contactServiceHome = getUserContactServiceHome(sProvider, username) ;
    try {
      return contactServiceHome.getNode(CONTACTS) ;
    } catch (PathNotFoundException ex) {
      Node userHome = contactServiceHome.addNode(CONTACTS, NT_UNSTRUCTURED) ;
      contactServiceHome.save() ;
      return userHome ;
    }
  }
  
  protected Node getUserContactGroupHome(SessionProvider sProvider, String username) throws Exception {
    Node contactServiceHome = getUserContactServiceHome(sProvider, username) ;
    try {
      return contactServiceHome.getNode(ADDRESS_BOOK) ;
    } catch (PathNotFoundException ex) {
      Node addHome = contactServiceHome.addNode(ADDRESS_BOOK, NT_UNSTRUCTURED) ;
      contactServiceHome.save() ;
      return addHome ;
    }
  }
  
  protected Node getPublicContactHome(SessionProvider sProvider) throws Exception {
    Node contactServiceHome = getPublicContactServiceHome(sProvider) ;
    try {
      return contactServiceHome.getNode(CONTACTS) ;
    } catch (PathNotFoundException ex) {
      Node publicHome = contactServiceHome.addNode(CONTACTS, NT_UNSTRUCTURED) ;
      contactServiceHome.save() ;
      return publicHome ;
    }
  }
  
  private Node getTagHome(SessionProvider sProvider, String username) throws Exception {
    Node contactServiceHome = getUserContactServiceHome(sProvider, username) ;
    try {
      return contactServiceHome.getNode(TAGS) ;
    } catch (PathNotFoundException ex) {
      Node tagHome = contactServiceHome.addNode(TAGS, NT_UNSTRUCTURED) ;
      contactServiceHome.save() ;
      return tagHome ;
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
    if(contactNode.hasProperty("exo:categories"))contact.setAddressBook(ValuesToStrings(contactNode.getProperty("exo:categories").getValues()));
    if(contactNode.hasProperty("exo:tags")) contact.setTags(ValuesToStrings(contactNode.getProperty("exo:tags").getValues()));
    if(contactNode.hasProperty("exo:editPermission")) contact.setEditPermission(ValuesToStrings(contactNode.getProperty("exo:editPermission").getValues()));
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

  public List<Contact> getAllContact(SessionProvider sProvider, String username) throws Exception {
  	Node contactHomeNode = getUserContactHome(sProvider, username);
    List<Contact> contacts = new ArrayList<Contact>();
    NodeIterator iter = contactHomeNode.getNodes();
    Contact contact;
    while (iter.hasNext()) {
      Node contactNode = iter.nextNode();
      contact = getContact(contactNode, PRIVATE);
      contacts.add(contact);
    }
    return contacts;
  }

  public ContactPageList getContactPageListByGroup(SessionProvider sProvider, String username, ContactFilter filter, String type) throws Exception {
    QueryManager qm = null ;
    if (type.equals(PRIVATE)) {
      Node contactHomeNode = getUserContactHome(sProvider, username);
      filter.setAccountPath(contactHomeNode.getPath()) ;
      qm = contactHomeNode.getSession().getWorkspace().getQueryManager();
    } else if (type.equals(PUBLIC)) {
      Node publicContactHomeNode = getPublicContactHome(sProvider) ;
      filter.setAccountPath(publicContactHomeNode.getPath()) ;
      qm = publicContactHomeNode.getSession().getWorkspace().getQueryManager();
    } else if (type.equals(SHARED)) {
      Node sharedAddressBookHome = getSharedAddressBookHome(SessionProvider.createSystemProvider()) ;
      if(sharedAddressBookHome.hasNode(username)) {
        Node userNode = sharedAddressBookHome.getNode(username) ;
        PropertyIterator iter = userNode.getReferences() ;
        Node addressBook ;
        while(iter.hasNext()) {
          addressBook = iter.nextProperty().getParent() ;
          if(addressBook.getProperty("exo:id").getString().equals(filter.getCategories()[0])) {
            Node contacts = addressBook.getParent().getParent().getNode(CONTACTS) ;
            filter.setAccountPath(contacts.getPath()) ;
            qm = contacts.getSession().getWorkspace().getQueryManager() ;
            break ;
          }      
        }
      }
    }      
    if (qm != null) {
      Query query = qm.createQuery(filter.getStatement(), Query.XPATH);  
      QueryResult result = query.execute(); 
      return new ContactPageList(username, result.getNodes(), 10, filter.getStatement(), true, "0") ; 
    }
    return null ;
  }
  
  public Contact getContact(SessionProvider sProvider, String username, String contactId) throws Exception {
    Node contactHomeNode = getUserContactHome(sProvider, username);
    try {
      Node contactNode = contactHomeNode.getNode(contactId);
      return getContact(contactNode, "0");
    } catch (PathNotFoundException ex) {
      return null;
    }
  }

  public ContactPageList getContactPageListByGroup(SessionProvider sProvider, String username, String groupId) throws Exception {
    Node contactHome = getUserContactHome(sProvider, username);
    QueryManager qm = contactHome.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root" + contactHome.getPath() 
                                                + "//element(*,exo:contact)[@exo:categories='").
                                                append(groupId).
                                                append("']").append("order by @exo:fullName ascending");
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    ContactPageList pageList = new ContactPageList(username, result.getNodes(), 10, queryString.toString(), true, "0") ;
    return pageList ;
  }
  
  public List<String>  getAllEmailAddressByGroup(SessionProvider sProvider, String username, String groupId) throws Exception {
    Node contactHome = getUserContactHome(sProvider, username);
    QueryManager qm = contactHome.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root" + contactHome.getPath() 
                                                + "//element(*,exo:contact)[@exo:categories='")
                                                .append(groupId).append("']");                                                
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    NodeIterator it = result.getNodes();
    List<String> address = new ArrayList<String>();
    Node contact = null ;
    while (it.hasNext()){
      contact = it.nextNode();
      if(contact.hasProperty("exo:emailAddress"))
        address.add(contact.getProperty("exo:emailAddress").getString());
    }
    return address ;
  }
  
  public List<String> getAllEmailByPublicGroup(String username, String groupId) throws Exception {
    Node publicContactHome = getPublicContactHome(SessionProvider.createSystemProvider()) ;
    QueryManager qm = publicContactHome.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root" + publicContactHome.getPath() 
                                                + "//element(*,exo:contact)[@exo:categories='")
                                                .append(groupId).append("']");                                                
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    NodeIterator it = result.getNodes();
    List<String> address = new ArrayList<String>();
    Node contact = null ;
    while (it.hasNext()){
      contact = it.nextNode();
      if(contact.hasProperty("exo:emailAddress"))
        address.add(contact.getProperty("exo:emailAddress").getString());
    }
    return address ;
  }
  
  public List<String> getAllEmailBySharedGroup(String username, String addressBookId) throws Exception {
    Node sharedHome = getSharedAddressBookHome(SessionProvider.createSystemProvider()) ;
    if(sharedHome.hasNode(username)) {
      PropertyIterator iter = sharedHome.getNode(username).getReferences() ;
      while(iter.hasNext()) {
        try{
          Node addressBook = iter.nextProperty().getParent() ; 
          if(addressBookId.equals(addressBook.getProperty("exo:id").getString())) {
            QueryManager qm = sharedHome.getSession().getWorkspace().getQueryManager();
            StringBuffer queryString = new StringBuffer("/jcr:root" + addressBook.getParent().getParent().getNode(CONTACTS).getPath() 
                                                        + "//element(*,exo:contact)[(@exo:categories='").
                                                        append(addressBookId).append("')]") ;
            Query query = qm.createQuery(queryString.toString(), Query.XPATH);
            QueryResult result = query.execute();
            NodeIterator it = result.getNodes();
            List<String> address = new ArrayList<String>();
            Node contact = null ;
            while (it.hasNext()){
              contact = it.nextNode();
              if(contact.hasProperty("exo:emailAddress"))
                address.add(contact.getProperty("exo:emailAddress").getString());
            }
            return address ;    
          }
        }catch (Exception e) {
          e.printStackTrace() ;
        }
      }
    }
    return null ;
  }
  
  private ContactGroup getGroup(Node contactGroupNode) throws Exception {
    ContactGroup contactGroup = new ContactGroup();
    if (contactGroupNode.hasProperty("exo:id")) 
      contactGroup.setId(contactGroupNode.getProperty("exo:id").getString());
    if (contactGroupNode.hasProperty("exo:name")) 
      contactGroup.setName(contactGroupNode.getProperty("exo:name").getString());
    if (contactGroupNode.hasProperty("exo:description")) 
      contactGroup.setDescription(contactGroupNode.getProperty("exo:description").getString());
    if (contactGroupNode.hasProperty("exo:editPermissions"))
      contactGroup.setEditPermission(
          ValuesToStrings(contactGroupNode.getProperty("exo:editPermissions").getValues())) ;
    return contactGroup;
  }

  public ContactGroup getGroup(SessionProvider sProvider, String username, String groupId) throws Exception {
    Node contactGroupHomeNode = getUserContactGroupHome(sProvider, username);
    String str = groupId.replaceAll("/", "") ;
    if (contactGroupHomeNode.hasNode(str))
      return getGroup(contactGroupHomeNode.getNode(str)) ;
    return null;
  }
  
  public ContactGroup getSharedGroup(String username, String groupId) throws Exception {
    Node sharedAddressBookHome = getSharedAddressBookHome(SessionProvider.createSystemProvider()) ;
    if(sharedAddressBookHome.hasNode(username)) {
      Node userNode = sharedAddressBookHome.getNode(username) ;
      PropertyIterator iter = userNode.getReferences() ;
      Node addressBook ;
      while(iter.hasNext()) {
        addressBook = iter.nextProperty().getParent() ;
        if(addressBook.getProperty("exo:id").getString().equals(groupId)) {
          return getGroup(addressBook) ; 
        }
      }      
    } 
    return null;
  }

  public List<ContactGroup> getGroups(SessionProvider sProvider, String username) throws Exception {
    Node contactGroupHomeNode = getUserContactGroupHome(sProvider, username);
    List<ContactGroup> contactGroups = new ArrayList<ContactGroup>();
    NodeIterator iter = contactGroupHomeNode.getNodes();
    while (iter.hasNext()) {
      Node contactGroupNode = iter.nextNode();
      contactGroups.add(getGroup(contactGroupNode));
    }
    return contactGroups;
  }

  public List<Contact> removeContacts(SessionProvider sysProvider, String username, List<String> contactIds) throws Exception {
    Node contactHomeNode = getUserContactHome(sysProvider, username);
    
    // edited
    Node publicContactHomeNode = getPublicContactHome(SessionProvider.createSystemProvider()) ;
    Node sharedHome = getSharedAddressBookHome(SessionProvider.createSystemProvider()) ; 
    List<Contact> contacts = new ArrayList<Contact>() ;
    Contact contact ;
    for (String contactId : contactIds) {    
      if (contactHomeNode.hasNode(contactId)) {
        contact = getContact(sysProvider, username, contactId);
        if(canRemove(username, contact)) {
          contactHomeNode.getNode(contactId).remove();
          contactHomeNode.getSession().save();
        }else {
          contacts.add(contact) ;
        }
      }else if(publicContactHomeNode.hasNode(contactId)){        
        contact = getPublicContact(contactId);
        if(canRemove(username, contact)) {
          publicContactHomeNode.getNode(contactId).remove();
          publicContactHomeNode.getSession().save();
        } else {
          contacts.add(contact) ;
        }
      } else if(sharedHome.hasNode(username)) {
        PropertyIterator iter = sharedHome.getNode(username).getReferences() ;
        while(iter.hasNext()) {
          try {
            Node addressBook = iter.nextProperty().getParent() ; 
            Node sharedContactHomeNode = addressBook.getParent().getParent().getNode(CONTACTS) ;
            if (sharedContactHomeNode.hasNode(contactId)) {
              contact = getContact(sharedContactHomeNode.getNode(contactId), JCRDataStorage.SHARED) ;
              if(canRemove(username, contact)) {
                sharedContactHomeNode.getNode(contactId).remove();
                sharedContactHomeNode.getSession().save();
              } else {
                contacts.add(contact) ;
              }
              break ;
            }            
          } catch (Exception e) {
            e.printStackTrace() ;
          }
        }
      }
    }    
    return contacts ;
  }
  
  private boolean canRemove(String username, Contact contact) {
    return true ;
  }
  
  
  public void moveContacts(SessionProvider sysProvider, String username, List<Contact> contacts, String addressType ) throws Exception {
    Node privateContactHome = getUserContactHome(sysProvider, username);
    Node publicContactHome = getPublicContactHome(SessionProvider.createSystemProvider()) ;
    for(Contact contact : contacts) {
      try{
    		if(addressType.equals(PRIVATE)) {
      		if(contact.getContactType().equals(SHARED)) {
      			saveContact(sysProvider, username, contact, true) ;
      			removeSharedContact(sysProvider, username, contact.getAddressBook()[0], contact.getId()) ;            
      		} else if(contact.getContactType().equals(PRIVATE)){
            saveContact(sysProvider, username, contact, false) ;            
          }else if(contact.getContactType().equals(PRIVATE)){
            saveContact(sysProvider, username, contact, false) ;            
          }
          /*else if(contact.getContactType().equals(PUBLIC)) {
            publicContactHome.getNode(contact.getId()).setProperty("exo:categories", contact.getAddressBook()) ;
            publicContactHome.getSession().move(publicContactHome.getPath() +"/" + contact.getId(), 
      					                                 privateContactHome.getPath() +"/" + contact.getId()) ;
            privateContactHome.save() ;   
            publicContactHome.getSession().save() ;
            ExtendedNode extNode ;
            try{
              extNode = (ExtendedNode)privateContactHome.getNode(contact.getId()) ;
            }catch (Exception e) {
              extNode = (ExtendedNode)getUserContactHome(SessionProvider.createSystemProvider(), username).getNode(contact.getId()) ;
            }
            if (extNode.canAddMixin("exo:privilegeable")) extNode.addMixin("exo:privilegeable");
            String[] arrayPers = {PermissionType.READ, PermissionType.ADD_NODE, PermissionType.SET_PROPERTY, PermissionType.REMOVE} ;
            extNode.setPermission(username, arrayPers) ;
            extNode.save() ;
            //id = contact.getId() ;
      		}
          */
          
          
      	}else if(addressType.equals(SHARED)) {
      		if(contact.getContactType().equals(PRIVATE)) {
      			saveContactToSharedAddressBook(sysProvider, username, contact.getAddressBook()[0], contact, true) ;
            if (privateContactHome.hasNode(contact.getId()))
              privateContactHome.getNode(contact.getId()).remove() ;
      		} else if(contact.getContactType().equals(SHARED)){
            saveContactToSharedAddressBook(sysProvider, username, contact.getAddressBook()[0], contact, false) ;
          }
          
          /*
          else if(contact.getContactType().equals(PUBLIC)) {
      			saveContactToSharedAddressBook(sysProvider, username, contact.getAddressBook()[0], contact, true) ;
      			publicContactHome.getNode(contact.getId()).remove() ;
      		}
          */
          
      	}
        /*
        else if(addressType.equals(PUBLIC)) {
      		if(contact.getContactType().equals(PRIVATE)) {
            Node node = privateContactHome.getNode(contact.getId()) ;
            node.setProperty("exo:categories", contact.getAddressBook()) ;
            node.save() ;
            publicContactHome.getSession().move(privateContactHome.getPath() +"/" + contact.getId(), 
                publicContactHome.getPath() +"/" + contact.getId()) ;
      		}else if(contact.getContactType().equals(SHARED)) {
      			savePublicContact(contact, true) ;
      			removeSharedContact(sysProvider, username, contact.getAddressBook()[0], contact.getId()) ;      			      			
      		}else if(contact.getContactType().equals(PUBLIC)) {
      			savePublicContact(contact, false) ;
      		}
      	}
        */
    	}catch(Exception e) {
    		e.printStackTrace() ;
    	}    	    	
    }
    if(privateContactHome.getSession().hasPendingChanges()) privateContactHome.getSession().save() ;
    if(publicContactHome.getSession().hasPendingChanges()) publicContactHome.getSession().save() ;
  }
  
  private List<String> getUserContactNodesByGroup(SessionProvider sProvider, String username, String groupId) throws Exception {
    Node contactHome = getUserContactHome(sProvider, username);
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
  
  public ContactGroup removeGroup(SessionProvider sProvider, String username, String groupId) throws Exception {
    Node contactGroupHomeNode = getUserContactGroupHome(sProvider, username);
    if (contactGroupHomeNode.hasNode(groupId)) {
      ContactGroup contactGroup = getGroup(sProvider, username, groupId);
      contactGroupHomeNode.getNode(groupId).remove();
      contactGroupHomeNode.save();
      contactGroupHomeNode.getSession().save();
      //Can not call removeContacts(...) here!!!
      removeContacts(sProvider, username, getUserContactNodesByGroup(sProvider, username, groupId)) ;
      return contactGroup;
    }
    return null;
  }
  
  public void saveContact(SessionProvider sProvider, String username, Contact contact, boolean isNew) throws Exception {
    Node contactHomeNode = getUserContactHome(sProvider, username);
    saveContact(contactHomeNode, contact, isNew) ;
    contactHomeNode.getSession().save();    
  }

  public void saveGroup(SessionProvider sProvider, String username, ContactGroup group, boolean isNew) throws Exception {
    Node groupHomeNode = getUserContactGroupHome(sProvider, username);
    Node sharedAddressBookHome = getSharedAddressBookHome(SessionProvider.createSystemProvider()) ;
    Node groupNode = null ;
    if (isNew) {
      groupNode = groupHomeNode.addNode(group.getId(), "exo:contactGroup");
      groupNode.setProperty("exo:id", group.getId());
    } else if (groupHomeNode.hasNode(group.getId())){
      groupNode = groupHomeNode.getNode(group.getId());
    } else {
      if(sharedAddressBookHome.hasNode(username)) {
        Node userNode = sharedAddressBookHome.getNode(username) ;
        PropertyIterator iter = userNode.getReferences() ;
        Node addressBook ;
        while(iter.hasNext()) {
          addressBook = iter.nextProperty().getParent() ;
          if(addressBook.getProperty("exo:id").getString().equals(group.getId())) {
            groupNode = addressBook ;
            break ;
          }
        }      
      }      
    }
    if (groupNode != null) {
      groupNode.setProperty("exo:name", group.getName());
      groupNode.setProperty("exo:description", group.getDescription());
      groupNode.setProperty("exo:editPermissions", group.getEditPermission()) ;
      groupHomeNode.getSession().save();
      sharedAddressBookHome.getSession().save() ;
    }
  }
  
  private Node getSharedAddressBookHome(SessionProvider sProvider) throws Exception {
    Node contactServiceHome = getPublicContactServiceHome(SessionProvider.createSystemProvider()) ;
    try {
      return contactServiceHome.getNode(SHARED_CONTACT) ;
    } catch (PathNotFoundException ex) {
      Node sharedHome = contactServiceHome.addNode(SHARED_CONTACT, NT_UNSTRUCTURED) ;
      contactServiceHome.save() ;
      return sharedHome ;
    }
  }
  
  public void shareAddressBook(SessionProvider sProvider, String username, String addressBookId, List<String> receiverUsers) throws Exception {
    Node sharedContactHome = getSharedAddressBookHome(sProvider) ;
    Node contactNode = getUserContactGroupHome(sProvider, username).getNode(addressBookId);
    Value[] values = {};
    if (contactNode.isNodeType(SHARED_MIXIN)) {     
      values = contactNode.getProperty(SHARED_PROP).getValues();
    } else {
      contactNode.addMixin(SHARED_MIXIN);     
    }
    Session systemSession = sharedContactHome.getSession() ;
    Node userNode ;
    List<Value> valueList = new ArrayList<Value>() ;
    for(String user : receiverUsers) {
      try {
        userNode = sharedContactHome.getNode(user) ;
      } catch (PathNotFoundException ex) {
        userNode = sharedContactHome.addNode(user, NT_UNSTRUCTURED) ;
        if(userNode.canAddMixin("mix:referenceable")) {
          userNode.addMixin("mix:referenceable") ;
        }
      }
      boolean isExist = false ; 
      for (int i = 0; i < values.length; i++) {
        Value value = values[i];
        String uuid = value.getString();
        Node refNode = systemSession.getNodeByUUID(uuid);
        if(refNode.getPath().equals(userNode.getPath())) {
          isExist = true ; 
          break ;
        }
        valueList.add(value) ;
      }
      if(!isExist) {
        Value value2add = contactNode.getSession().getValueFactory().createValue(userNode);
        valueList.add(value2add) ;
      }      
    }
    if(valueList.size() > 0) {
      contactNode.setProperty(SHARED_PROP, valueList.toArray( new Value[valueList.size()]));
      contactNode.save() ;
      sharedContactHome.getSession().save() ;
      contactNode.getSession().save();
      systemSession.logout() ;
    }
  }
  
  public List<String> getSharedAddressBooks(SessionProvider sProvider, String username) throws Exception {
  	Node sharedHome = getSharedAddressBookHome(sProvider) ;
  	List<String> addressBooks = new ArrayList<String>() ;
    if(sharedHome.hasNode(username)) {
      Node sharedNode = sharedHome.getNode(username) ;      
      PropertyIterator iter = sharedNode.getReferences() ;
      while(iter.hasNext()) {
        try{
          Node addressNode = iter.nextProperty().getParent() ;
          addressBooks.add(addressNode.getProperty("exo:name").getString()+"::"+addressNode.getName()) ;
        }catch(Exception e){
          e.printStackTrace() ;
        }
      }      
    }
    return addressBooks ;
  }

  public ContactPageList getSharedContactsByAddressBook(SessionProvider sProvider, String username, String addressBookId) throws Exception {
    Node sharedHome = getSharedAddressBookHome(SessionProvider.createSystemProvider()) ;
    if(sharedHome.hasNode(username)) {
      PropertyIterator iter = sharedHome.getNode(username).getReferences() ;
      while(iter.hasNext()) {
        try{
          Node addressBook = iter.nextProperty().getParent() ; 
          if(addressBookId.equals(addressBook.getProperty("exo:id").getString())) {
          	QueryManager qm = sharedHome.getSession().getWorkspace().getQueryManager();
            StringBuffer queryString = new StringBuffer("/jcr:root" + addressBook.getParent().getParent().getNode(CONTACTS).getPath() 
                                                        + "//element(*,exo:contact)[(@exo:categories='").
                                                        append(addressBookId).append("')]")
                                                        .append(" order by @exo:fullName ascending");
            Query query = qm.createQuery(queryString.toString(), Query.XPATH);
            QueryResult result = query.execute();
            return new ContactPageList(username, result.getNodes(), 10, queryString.toString(), true, "1") ;
          }
        }catch (Exception e) {
          e.printStackTrace() ;
        }
      }
    }
    return null ;    
  }
  
  public void removeSharedAddressBook(SessionProvider sProvider, String username, String addressBookId) throws Exception {
    Node sharedAddressBookHome = getSharedAddressBookHome(SessionProvider.createSystemProvider()) ;
    if(sharedAddressBookHome.hasNode(username)) {
      Node userNode = sharedAddressBookHome.getNode(username) ;
      String uuid = userNode.getProperty("jcr:uuid").getString() ;
      PropertyIterator iter = userNode.getReferences() ;
      Node addressBook ;
      List<Value> newValues = new ArrayList<Value>() ;
      while(iter.hasNext()) {
        addressBook = iter.nextProperty().getParent() ;
        if(addressBook.getProperty("exo:id").getString().equals(addressBookId)) {
          Value[] values = addressBook.getProperty(SHARED_PROP).getValues() ;
          for(Value value : values){
            if(!value.getString().equals(uuid)) {
              newValues.add(value) ;
            }
          }
          addressBook.setProperty(SHARED_PROP, newValues.toArray(new Value[newValues.size()])) ;
          addressBook.save() ;
          break ;
        }
      }      
    }
  }
  
  public void removeSharedContact(SessionProvider sProvider, String username, String addressBookId, String contactId) throws Exception {
    Node sharedAddressBookHome = getSharedAddressBookHome(SessionProvider.createSystemProvider()) ;
    if(sharedAddressBookHome.hasNode(username)) {
      Node userNode = sharedAddressBookHome.getNode(username) ;
      PropertyIterator iter = userNode.getReferences() ;
      Node addressBook ;
      while(iter.hasNext()) {
        addressBook = iter.nextProperty().getParent() ;        
        // addressBookId changed need some changes
        //if(addressBook.getProperty("exo:id").getString().equals(addressBookId)) {
      	Node contacts = addressBook.getParent().getParent().getNode(CONTACTS) ;
      	if(contacts.hasNode(contactId)) {
      		contacts.getNode(contactId).remove() ;
      		contacts.save() ;
          break ;
      	}
      }      
    }
  }
  
  public Contact getSharedContacts(SessionProvider sProvider, String username, String contactId) throws Exception {
    Node sharedAddressBookHome = getSharedAddressBookHome(SessionProvider.createSystemProvider()) ;
    if(sharedAddressBookHome.hasNode(username)) {
      Node userNode = sharedAddressBookHome.getNode(username) ;
      PropertyIterator iter = userNode.getReferences() ;
      Node addressBook ;
      while(iter.hasNext()) {
        addressBook = iter.nextProperty().getParent() ;
        Node contacts = addressBook.getParent().getParent().getNode(CONTACTS) ;
        if(contacts.hasNode(contactId)) {
          return getContact(contacts.getNode(contactId), "1") ;
        }        
      }      
    }
    return null ;
  }
  
  public void saveContactToSharedAddressBook(SessionProvider sProvider, String username, String addressBookId, Contact contact, boolean isNew) throws Exception  {
    Node sharedAddressBookHome = getSharedAddressBookHome(sProvider) ;
    if(sharedAddressBookHome.hasNode(username)) {
      Node userNode = sharedAddressBookHome.getNode(username) ;
      PropertyIterator iter = userNode.getReferences() ;
      Node addressBook ;      
      while(iter.hasNext()) {
        addressBook = iter.nextProperty().getParent() ;
        if(addressBook.getProperty("exo:id").getString().equals(addressBookId)) {
        	Node contactHomeNode = addressBook.getParent().getParent().getNode(CONTACTS) ;
          saveContact(contactHomeNode, contact, isNew) ;
          contactHomeNode.save() ;
          break ;
        }
      }      
    }
  } 
  
  public Contact getPublicContact(String contactId) throws Exception {
    Node contactHomeNode = getPublicContactHome(SessionProvider.createSystemProvider());
    if(contactHomeNode.hasNode(contactId)) {
      Node contactNode = contactHomeNode.getNode(contactId);
      Contact contact = new Contact();
      contact = getContact(contactNode, "2");
      return contact;
    }
    return null;
  }

  public ContactPageList getPublicContactsByAddressBook(SessionProvider sysProvider, String groupId) throws Exception {
  	String usersPath = nodeHierarchyCreator_.getJcrPath("usersPath") ;
    Node contactHome = getPublicContactHome(SessionProvider.createSystemProvider());
    /*
    System.out.println("\n\n userpath:" + usersPath + "\n\n");
    System.out.println("\n\n publicHome:" + contactHome.getPath() + "\n\n");
    System.out.println("\n\n ccc:" + contactHome.getNodes().getSize());
    */
    QueryManager qm = contactHome.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root" + usersPath 
                                                + "//element(*,exo:contact)[@exo:categories='")
                                                .append(groupId).append("' and @exo:isOwner='true'] ")
                                                .append("order by @exo:fullName ascending");
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    return new ContactPageList(null, result.getNodes(), 10, queryString.toString(), true, "2") ;
  }
  
  public List<GroupContactData> getPublicContacts(SessionProvider sysProvider, String[] groupIds) throws Exception {
    List<GroupContactData> contactByGroup = new ArrayList<GroupContactData>() ;
    List<Contact> contacts;
    for(String groupId : groupIds) { 
      contacts = getPublicContactsByAddressBook(sysProvider, groupId).getAll();
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
    Node contactHome = getPublicContactHome(sysProvider);
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
  
  // not use ?
  public Contact removePublicContact(SessionProvider sysProvider, String contactId) throws Exception {
    Node contactHomeNode = getPublicContactHome(sysProvider);
    if (contactHomeNode.hasNode(contactId)) {
      Contact contact = getPublicContact(contactId);
      contactHomeNode.getNode(contactId).remove();
      contactHomeNode.getSession().save();
      return contact;
    }
    return null;
  }

  public void savePublicContact(Contact contact, boolean isNew) throws Exception {
    Node contactHomeNode = getUserContactHome(SessionProvider.createSystemProvider(), contact.getOwnerId()) ;
    saveContact(contactHomeNode, contact, isNew) ;
    contactHomeNode.getSession().save(); 
  }
  
  public void addGroupToPersonalContact(String userId, String groupId) throws Exception {
  	Node contactHome = getUserContactHome(SessionProvider.createSystemProvider(), userId) ;
  	Node contactNode = contactHome.getNode(userId) ;
  	Value[] values = contactNode.getProperty("exo:categories").getValues() ;
  	List<String> ls = new ArrayList<String>() ;
  	for(Value vl : values) {
  		if(vl.getString().equals(groupId)) return ;
  		ls.add(vl.getString()) ;  		
  	}
  	ls.add(groupId) ;
  	contactNode.setProperty("exo:categories", ls.toArray(new String[]{})) ;
  	contactNode.save() ;
  }
  
  private void reparePermissions(Node node, String owner) throws Exception {
  	ExtendedNode extNode = (ExtendedNode)node ;
  	if (extNode.canAddMixin("exo:privilegeable")) extNode.addMixin("exo:privilegeable");
    String[] arrayPers = {PermissionType.READ, PermissionType.ADD_NODE, PermissionType.SET_PROPERTY, PermissionType.REMOVE} ;
    extNode.setPermission(owner, arrayPers) ;
    List<AccessControlEntry> permsList = extNode.getACL().getPermissionEntries() ;    
    for(AccessControlEntry accessControlEntry : permsList) {
      extNode.setPermission(accessControlEntry.getIdentity(), arrayPers) ;      
    } 
    extNode.removePermission("any") ;
    
  }
  
  private void saveContact(Node contactHomeNode, Contact contact, boolean isNew) throws Exception {
  	Node contactNode;
    if (isNew) {
      contactNode = contactHomeNode.addNode(contact.getId(), "exo:contact"); 
      contactNode.setProperty("exo:id", contact.getId());
      if(contact.isOwner()) {
      	contactNode.setProperty("exo:isOwner", true) ;
        contactNode.setProperty("exo:ownerId", contact.getOwnerId()) ;
      	reparePermissions(contactHomeNode, contact.getOwnerId()) ;
      	reparePermissions(contactNode, contact.getOwnerId()) ;
      }
    } else {
      contactNode = contactHomeNode.getNode(contact.getId());
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
    contactNode.setProperty("exo:categories", contact.getAddressBook());
    contactNode.setProperty("exo:tags", contact.getTags());
    contactNode.setProperty("exo:editPermission", contact.getEditPermission());

    if (contact.getLastUpdated() != null) {
      dateTime.setTime(contact.getLastUpdated()) ;
      contactNode.setProperty("exo:lastUpdated", dateTime);
    }
//  save image to contact
    ContactAttachment attachment = contact.getAttachment() ;
    if (attachment != null) {
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
  
  public void updateTag(SessionProvider sProvider, String username,Tag tag) throws Exception {
    Node tagHome = getTagHome(sProvider, username) ;
    if (tagHome.hasNode(tag.getId())) {
      Node tagNode = tagHome.getNode(tag.getId());
      tagNode.setProperty("exo:name", tag.getName());
      tagNode.setProperty("exo:description", tag.getDescription());
      tagNode.setProperty("exo:color", tag.getColor());
    }
    tagHome.save();
  }
  
  
  public Tag getTag(SessionProvider sProvider, String username, String tagId) throws Exception {
    Node tagHomeNode = getTagHome(sProvider, username);
    if (tagHomeNode.hasNode(tagId)) 
      return getTag(tagHomeNode.getNode(tagId));
    return null ;
  }
  
  public List<Tag> getTags(SessionProvider sProvider, String username) throws Exception {
    Node tagHomeNode = getTagHome(sProvider, username);
    List<Tag> tags = new ArrayList<Tag>();
    NodeIterator iter = tagHomeNode.getNodes();
    while (iter.hasNext()) {
      Node tagNode = iter.nextNode();
      tags.add(getTag(tagNode));
    }
    return tags;
  }

  public DataPageList getContactPageListByTag(SessionProvider sysProvider, String username, String tagId) throws Exception {
    //query on private contacts
    Node contactHome = getUserContactHome(sysProvider, username);
    QueryManager qm = contactHome.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root" + contactHome.getPath() 
                                                + "//element(*,exo:contact)[@exo:tags='").
                                                append(tagId).
                                                append("']");
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    NodeIterator it = result.getNodes();    
    List<Contact> contacts = new ArrayList<Contact>();
    while (it.hasNext()) {
      contacts.add(getContact(it.nextNode(), "0"));
    }
    
    //query on public contacts
    Node publicContactHome = getPublicContactHome(SessionProvider.createSystemProvider());
    queryString = new StringBuffer("/jcr:root" + publicContactHome.getPath() 
                                                + "//element(*,exo:contact)[@exo:tags='").
                                                append(tagId).
                                                append("']");
    query = qm.createQuery(queryString.toString(), Query.XPATH);
    result = query.execute();
    it = result.getNodes();    
    while (it.hasNext()) {
      contacts.add(getContact(it.nextNode(), "2"));
    }
    
    // query on shared contacts
    Node sharedHome = getSharedAddressBookHome(SessionProvider.createSystemProvider()) ;   
    if(sharedHome.hasNode(username)) {
      PropertyIterator iter = sharedHome.getNode(username).getReferences() ;
      while(iter.hasNext()) {
        try{
          Node addressBook = iter.nextProperty().getParent() ; 
          Node shareContactHomeNode = addressBook.getParent().getParent().getNode(CONTACTS) ;
          queryString = new StringBuffer("/jcr:root" + shareContactHomeNode.getPath() 
                                                      + "//element(*,exo:contact)[@exo:tags='").
                                                      append(tagId).
                                                      append("']");
          query = qm.createQuery(queryString.toString(), Query.XPATH);
          result = query.execute();
          it = result.getNodes();    
          while (it.hasNext()) {
          contacts.add(getContact(it.nextNode(), "1"));
          }          
        }catch (Exception e) {
          e.printStackTrace() ;
        }
      }
    }
    return new DataPageList(contacts, 10, null, false) ;
  }
  
  public void addTag(SessionProvider sysProvider, String username, List<Contact> contacts, String tagId) throws Exception {
    Map<String, String> tagMap = new HashMap<String, String> () ;
    tagMap.put(tagId, tagId) ;
    Node contactHomeNode = getUserContactHome(sysProvider, username);
    
    Node sharedHome = getSharedAddressBookHome(SessionProvider.createSystemProvider()) ;
    Node contactNode = null ;
    for(Contact contact : contacts) {  
      if(contactHomeNode.hasNode(contact.getId())) {
        contactNode = contactHomeNode.getNode(contact.getId()) ;
      } else if(sharedHome.hasNode(username)) {
        PropertyIterator iter = sharedHome.getNode(username).getReferences() ;
        while(iter.hasNext()) {
          try{
            Node addressBook = iter.nextProperty().getParent() ; 
            Node shareContactHomeNode = addressBook.getParent().getParent().getNode(CONTACTS) ;
            if (shareContactHomeNode.hasNode(contact.getId())) {  
              contactNode = shareContactHomeNode.getNode(contact.getId()) ;
              break ;
            }
          }catch (Exception e) {
            e.printStackTrace() ;
          }
        }
      } else {
        Node publicContactHomeNode = getUserContactHome(SessionProvider.createSystemProvider(), contact.getOwnerId());
        contactNode = publicContactHomeNode.getNode(contact.getId());       
      } 
      
      if (contactNode != null) {
        Value[] values = null ;
        if(contactNode.hasProperty("exo:tags")){
          values = contactNode.getProperty("exo:tags").getValues() ;
          for(Value value : values) tagMap.put(value.getString(), value.getString()) ;         
        }
        contactNode.setProperty("exo:tags", tagMap.values().toArray(new String[]{})) ;
        if (values != null)
          for(Value value : values) tagMap.remove(value.getString()) ;
      }      
    }
    contactHomeNode.getSession().save() ;
    //publicContactHomeNode.getSession().save();
    sharedHome.getSession().save() ;
  }

  public void addTag(SessionProvider sysProvider, String username, List<Contact> contacts, List<Tag> tags) throws Exception {
    Node tagHomeNode = getTagHome(sysProvider, username);
    Map<String, String> tagMap = new HashMap<String, String> () ;
    for(Tag tag : tags) {
      if(!tagHomeNode.hasNode(tag.getId())) {
        Node tagNode = tagHomeNode.addNode(tag.getId(), "exo:contactTag") ;
        tagNode.setProperty("exo:id", tag.getId());
        tagNode.setProperty("exo:name", tag.getName());
        tagNode.setProperty("exo:description", tag.getDescription());
        tagNode.setProperty("exo:color", tag.getColor());
      }
      tagMap.put(tag.getId(), tag.getId()) ;
    }
    tagHomeNode.getSession().save() ;
    Node contactHomeNode = getUserContactHome(sysProvider, username);
    
    
    //Node publicContactHomeNode = getPublicContactHome(SessionProvider.createSystemProvider());
    // hoang hung add
    Node sharedHome = getSharedAddressBookHome(SessionProvider.createSystemProvider()) ; 
    Node contactNode = null ;
    for(Contact contact : contacts) {
      if(contactHomeNode.hasNode(contact.getId())) {
        contactNode = contactHomeNode.getNode(contact.getId()) ;
      } else if(sharedHome.hasNode(username)) {
        PropertyIterator iter = sharedHome.getNode(username).getReferences() ;
        while(iter.hasNext()) {
          try{
            Node addressBook = iter.nextProperty().getParent() ; 
            Node shareContactHomeNode = addressBook.getParent().getParent().getNode(CONTACTS) ;
            if (shareContactHomeNode.hasNode(contact.getId())) {  
              contactNode = shareContactHomeNode.getNode(contact.getId()) ;
              break ;
            }
          }catch (Exception e) {
            e.printStackTrace() ;
          }
        }
      } else {
        Node publicContactHomeNode = getUserContactHome(SessionProvider.createSystemProvider(), contact.getOwnerId());
        contactNode = publicContactHomeNode.getNode(contact.getId());
        publicContactHomeNode.getSession().save();
      }       
      
      if (contactNode != null) {
        Map<String, String> thisTagMap = new HashMap<String, String> () ;
        thisTagMap = tagMap ;
        Value[] values = null ;
        if(contactNode.hasProperty("exo:tags")){
          values = contactNode.getProperty("exo:tags").getValues() ;
          for(Value value : values) thisTagMap.put(value.getString(), value.getString()) ;         
        }
        contactNode.setProperty("exo:tags", thisTagMap.values().toArray(new String[]{})) ;        
      }     
    }          
        
    contactHomeNode.getSession().save() ;
    sharedHome.getSession().save() ;
  }
  
  public Tag removeTag(SessionProvider sysProvider, String username, String tagId) throws Exception {
    Node tagHomeNode = getTagHome(sysProvider, username);
    if (tagHomeNode.hasNode(tagId)) {
      Node contactHome = getUserContactHome(sysProvider, username) ;
      Node publicContactHome = getPublicContactHome(sysProvider) ;
      List<Contact> contacts = getContactPageListByTag(sysProvider, username, tagId).getAll();
      for (Contact contact : contacts) {
        List<String> tags = new ArrayList<String>(Arrays.asList(contact.getTags()));
        tags.remove(tagId) ;        
        String contactId = contact.getId() ;
        if (contactHome.hasNode(contactId)) {
          contactHome.getNode(contactId).setProperty("exo:tags", tags.toArray(new String[]{})) ;
        } else if(publicContactHome.hasNode(contactId)) {
          publicContactHome.getNode(contactId).setProperty("exo:tags", tags.toArray(new String[]{})) ;
        }
      }
      contactHome.getSession().save() ;
      publicContactHome.getSession().save() ;
      
      Tag tag = getTag(sysProvider, username, tagId);
      tagHomeNode.getNode(tagId).remove();
      tagHomeNode.save();
      tagHomeNode.getSession().save();
      return tag;
    }
    return null;
  }
  
  public void removeContactTag(SessionProvider sysProvider, String username, List<String> contactIds, List<String> tags) throws Exception {
    Node contactHome = getUserContactHome(sysProvider, username) ;
    Node publicContactHome = getPublicContactHome(sysProvider) ;
    Node sharedAddressBookHome = getSharedAddressBookHome(SessionProvider.createSystemProvider()) ;
    Node contact = null ;
    for(String contactId : contactIds) {
      if(contactHome.hasNode(contactId)) {
        contact = contactHome.getNode(contactId) ;
      } else if(publicContactHome.hasNode(contactId)) {
          contact = publicContactHome.getNode(contactId) ;
      } else if(sharedAddressBookHome.hasNode(username)) {
        Node userNode = sharedAddressBookHome.getNode(username) ;
        PropertyIterator iter = userNode.getReferences() ;
        Node addressBook ;
        while(iter.hasNext()) {
          addressBook = iter.nextProperty().getParent() ; 
          //if(addressBook.getProperty("exo:id").getString().equals(addressBookId)) {
          Node contacts = addressBook.getParent().getParent().getNode(CONTACTS) ;
          if(contacts.hasNode(contactId)) {
            contact = contacts.getNode(contactId) ;
          }
          // break only exit while loop
          break ;    
        }      
      }
      
      if (contact != null) {
        if(contact.hasProperty("exo:tags")){
          Value[] values = contact.getProperty("exo:tags").getValues() ;
          List<String> tagList = new ArrayList<String>() ;
          for(Value value : values) { tagList.add(value.getString()) ; }
          for(String tag : tags) {
            if(tagList.contains(tag)) tagList.remove(tag) ;
          }
          contact.setProperty("exo:tags", tagList.toArray(new String[]{})) ;
          contact.save() ;
        }
      } 
    }
    contactHome.getSession().save() ;
    publicContactHome.getSession().save() ;
  }
  
  public DataPageList searchContact(SessionProvider sysProvider, String username, ContactFilter filter)throws Exception {
    List<Contact> contacts = new ArrayList<Contact>() ;
    Query query ;
    QueryManager qm ;
    // private contacts
    if(username != null && username.length() > 0) {
      Node contactHome = getUserContactHome(sysProvider, username) ;
      filter.setAccountPath(contactHome.getPath()) ;
      qm = contactHome.getSession().getWorkspace().getQueryManager() ;      
      query = qm.createQuery(filter.getStatement(), Query.XPATH) ;
      NodeIterator it = query.execute().getNodes() ;
      while(it.hasNext()) {
        contacts.add(getContact(it.nextNode(), "0")) ;        
      }
    }
    //public contacts
    Node publicContactHome = getPublicContactHome(sysProvider) ;
    filter.setAccountPath(publicContactHome.getPath()) ;
    qm = publicContactHome.getSession().getWorkspace().getQueryManager() ;
    query = qm.createQuery(filter.getStatement(), Query.XPATH) ;
    NodeIterator itpublic = query.execute().getNodes();
    while(itpublic.hasNext()) {
      contacts.add(getContact(itpublic.nextNode(), "2")) ;
    }
    //share contacts
    Node sharedAddressBookHome = getSharedAddressBookHome(SessionProvider.createSystemProvider()) ;
    if(sharedAddressBookHome.hasNode(username)) {
      Node userNode = sharedAddressBookHome.getNode(username) ;
      PropertyIterator iter = userNode.getReferences() ;
      Node addressBook ;
      while(iter.hasNext()) {
        addressBook = iter.nextProperty().getParent() ;
        Node sharedContactsNode = addressBook.getParent().getParent().getNode(CONTACTS) ;
        filter.setAccountPath(sharedContactsNode.getPath()) ;
        qm = sharedContactsNode.getSession().getWorkspace().getQueryManager() ;
        query = qm.createQuery(filter.getStatement(), Query.XPATH) ;
        NodeIterator itshare = query.execute().getNodes();
        while(itshare.hasNext()) {
          contacts.add(getContact(itshare.nextNode(), "1")) ;
        }
      }      
    }
    return new DataPageList(contacts, 10, null, false) ;    
  }

  private void copyNodes(SessionProvider sProvider, String username,Node srcHomeNode, NodeIterator iter, String destAddress, String destType ) throws Exception {
    if (destType.equals(PRIVATE)) {
      
//      Node contactHomeNode = getUserContactHome(sProvider, username);
//      while (iter.hasNext()) {
//        Node oldNode = iter.nextNode() ;
//        String newId = "Contact" + IdGenerator.generate() ;
//        srcHomeNode.getSession().getWorkspace().copy(srcHomeNode.getPath() + "/"
//             + oldNode.getProperty("exo:id").getString(), contactHomeNode.getPath() + "/" + newId) ;
//        ExtendedNode extNode ;
//        try{
//          System.out.println("\n\n 11111111 \n\n");
//          extNode = (ExtendedNode)contactHomeNode.getNode(newId) ;
//        }catch (Exception e) {
//          
//          System.out.println("\n\n 222222222222222 \n\n");
//          extNode = (ExtendedNode)getUserContactHome(SessionProvider.createSystemProvider(), username).getNode(newId) ;
//        }
//        // not need if private to private (very litte)
//        if (extNode.canAddMixin("exo:privilegeable")) extNode.addMixin("exo:privilegeable");
//        String[] arrayPers = {PermissionType.READ, PermissionType.ADD_NODE, PermissionType.SET_PROPERTY, PermissionType.REMOVE} ;
//        extNode.setPermission(username, arrayPers) ; 
//        
//        extNode.setProperty("exo:categories", new String [] {destAddress}) ;
//        extNode.setProperty("exo:id", newId) ;          
//        extNode.save() ;       
//      }
//      contactHomeNode.getSession().save() ;
        
      Node contactHomeNode = getUserContactHome(sProvider, username);
      while (iter.hasNext()) {
        Node oldNode = iter.nextNode() ;
        String newId = "Contact" + IdGenerator.generate() ;
        srcHomeNode.getSession().getWorkspace().copy(srcHomeNode.getPath() + "/"
             + oldNode.getProperty("exo:id").getString(), contactHomeNode.getPath() + "/" + newId) ;
        ExtendedNode extNode ;
        try{
          extNode = (ExtendedNode)contactHomeNode.getNode(newId) ;
        }catch (Exception e) {
          
          extNode = (ExtendedNode)getUserContactHome(SessionProvider.createSystemProvider(), username).getNode(newId) ;
        }
        // not need if private to private (very litte)
        if (extNode.canAddMixin("exo:privilegeable")) extNode.addMixin("exo:privilegeable");
        String[] arrayPers = {PermissionType.READ, PermissionType.ADD_NODE, PermissionType.SET_PROPERTY, PermissionType.REMOVE} ;
        extNode.setPermission(username, arrayPers) ; 
        extNode.save() ;
        
        Node newNode = contactHomeNode.getNode(newId) ;
        newNode.setProperty("exo:categories", new String [] {destAddress}) ;
        newNode.setProperty("exo:id", newId) ;          
               
      }
      contactHomeNode.getSession().save() ;
      
      
    } else if (destType.equals(SHARED)) {
      Node sharedHome = getSharedAddressBookHome(SessionProvider.createSystemProvider()) ;
      while (iter.hasNext()) {
        Node oldNode = iter.nextNode() ;
        String newId = "Contact" + IdGenerator.generate() ;
        if(sharedHome.hasNode(username)) {
          PropertyIterator sharedIter = sharedHome.getNode(username).getReferences() ;
          while(sharedIter.hasNext()) {
            try{
              Node addressBook = sharedIter.nextProperty().getParent() ; 
              if(destAddress.equals(addressBook.getProperty("exo:id").getString())) {
                Node sharedContacts = addressBook.getParent().getParent().getNode(CONTACTS) ;
                sharedContacts.getSession().getWorkspace().copy(srcHomeNode.getPath() + "/"
                    + oldNode.getProperty("exo:id").getString(), sharedContacts.getPath() + "/" + newId) ;
                Node newNode = sharedContacts.getNode(newId) ;
                newNode.setProperty("exo:categories", new String [] {destAddress}) ;  
                newNode.setProperty("exo:id", newId) ;
                break ;
              }                
            } catch (Exception e) {
              e.printStackTrace() ;
            }
          }  
        } 
      }
     sharedHome.getSession().save() ; 
    } else { // destType = Public ;
      Node publicContactHome = getPublicContactHome(SessionProvider.createSystemProvider());
      while (iter.hasNext()) {
        Node oldNode = iter.nextNode() ;
        String newId = "Contact" + IdGenerator.generate() ;          
        publicContactHome.getSession().getWorkspace().copy(srcHomeNode.getPath() + "/"
             + oldNode.getProperty("exo:id").getString(), publicContactHome.getPath() + "/" + newId) ;
        Node newNode = publicContactHome.getNode(newId) ;
        newNode.setProperty("exo:categories", new String [] {destAddress}) ;
        newNode.setProperty("exo:id", newId) ;
      }    
      publicContactHome.getSession().save() ;
    }      
  }
  public void pasteAddressBook(SessionProvider sProvider, String username, String srcAddress, String srcType, String destAddress, String destType) throws Exception {
    NodeIterator iter = null ;
    if (srcType.equals(PRIVATE)) {
      Node contactHome = getUserContactHome(sProvider, username);
      QueryManager qm = contactHome.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer("/jcr:root" + contactHome.getPath() 
                                                  + "//element(*,exo:contact)[@exo:categories='").
                                                  append(srcAddress).
                                                  append("']");
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      iter = result.getNodes() ;
      copyNodes(sProvider, username, contactHome, iter, destAddress, destType) ;      
    } else if (srcType.equals(SHARED)) {
      SessionProvider sessionProvider = SessionProvider.createSystemProvider() ;
      Node sharedHome = getSharedAddressBookHome(sessionProvider) ;
      if (sharedHome.hasNode(username)) {
        PropertyIterator sharedIter = sharedHome.getNode(username).getReferences() ;
        Node sharedContacts = null ;
        while(sharedIter.hasNext()) {
          try{
            Node addressBook = sharedIter.nextProperty().getParent() ; 
            if(srcAddress.equals(addressBook.getProperty("exo:id").getString())) {
              QueryManager qm = sharedHome.getSession().getWorkspace().getQueryManager();
              sharedContacts = addressBook.getParent().getParent().getNode(CONTACTS) ;
              StringBuffer queryString = new StringBuffer("/jcr:root" + sharedContacts.getPath() 
                                                          + "//element(*,exo:contact)[(@exo:categories='").
                                                          append(srcAddress).append("')]") ;
              Query query = qm.createQuery(queryString.toString(), Query.XPATH);
              QueryResult result = query.execute();
              iter = result.getNodes() ;
              break ;
            }
          }catch (Exception e) {
            e.printStackTrace() ;
          }
        }
        if (sharedContacts != null)
          copyNodes(sessionProvider, username, sharedContacts, iter, destAddress, destType) ;      
      }            
    } else {
      Node publicContactHome = getPublicContactHome(SessionProvider.createSystemProvider());
      QueryManager qm = publicContactHome.getSession().getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer("/jcr:root" + publicContactHome.getPath() 
                                                  + "//element(*,exo:contact)[@exo:categories='")
                                                  .append(srcAddress).append("']") ;
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      iter = result.getNodes() ;
      copyNodes(sProvider, username, publicContactHome, iter, destAddress, destType) ;
    }
  }
  
  public void pasteContacts(SessionProvider sProvider, String username, String destAddress, String destType, List<Contact> contacts) throws Exception {
    if (destType.equals(PRIVATE)) {
      for (Contact contact : contacts) {
        contact.setId("Contact" + IdGenerator.generate()) ;
        contact.setAddressBook(new String[] {destAddress}) ;
        saveContact(sProvider, username, contact, true) ;         
      }      
    } else if (destType.equals(SHARED)) {
      for (Contact contact : contacts) {
        contact.setId("Contact" + IdGenerator.generate()) ;
        contact.setAddressBook(new String[] {destAddress}) ;
        saveContactToSharedAddressBook(sProvider, username, destAddress, contact, true) ;
      }
    } else {
      for (Contact contact : contacts) {
        contact.setId("Contact" + IdGenerator.generate()) ;
        contact.setAddressBook(new String[] {destAddress}) ;
        savePublicContact(contact, true) ;
      }
    }
  }
  
}
