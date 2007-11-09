/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
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
import org.exoplatform.registry.JCRRegistryService;
import org.exoplatform.registry.ServiceRegistry;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 10, 2007  
 */
public class JCRDataStorage implements DataStorage {
  
  final private static String CONTACTS = "contacts".intern() ;
  final private static String CONTACT_GROUP = "contactGroup".intern() ;
  final private static String TAGS = "tags".intern() ;
  
  private RepositoryService  repositoryService_ ; 
  private JCRRegistryService jcrRegistryService_ ;
  
  public JCRDataStorage(RepositoryService  repositoryService, 
                        JCRRegistryService jcrRegistryService)throws Exception {
    repositoryService_ = repositoryService ;
    jcrRegistryService_ = jcrRegistryService ;
  }  
  
  private Node getContactServiceHome(String username) throws Exception {
    ServiceRegistry serviceRegistry = new ServiceRegistry("ContactService") ;
    Session session = getJCRSession() ;
    if(jcrRegistryService_.getUserNode(session, username) == null)
      jcrRegistryService_.createUserHome(username, false) ;
    jcrRegistryService_.createServiceRegistry(username, serviceRegistry, false) ;    
    return jcrRegistryService_.getServiceRegistryNode(session, username, serviceRegistry.getName()) ;
  }
  
  private Node getPublicContactServiceHome() throws Exception {
    ServiceRegistry serviceRegistry = new ServiceRegistry("ContactService") ;
    Session session = getJCRSession() ;
    jcrRegistryService_.createServiceRegistry(serviceRegistry, false) ;    
    return jcrRegistryService_.getServiceRegistryNode(session, serviceRegistry.getName()) ;
  }
  
  private Node getContactHome(String username) throws Exception {
    Node contactServiceHome = getContactServiceHome(username) ;
    if(contactServiceHome.hasNode(CONTACTS)) return contactServiceHome.getNode(CONTACTS) ;
    return contactServiceHome.addNode(CONTACTS, "nt:unstructured") ;
  }
  
  private Node getContactGroupHome(String username) throws Exception {
    Node contactServiceHome = getContactServiceHome(username) ;
    if(contactServiceHome.hasNode(CONTACT_GROUP)) return contactServiceHome.getNode(CONTACT_GROUP) ;
    return contactServiceHome.addNode(CONTACT_GROUP, "nt:unstructured") ;
  }
  
  private Node getTagHome(String username) throws Exception {
    Node contactServiceHome = getContactServiceHome(username) ;
    if(contactServiceHome.hasNode(TAGS)) return contactServiceHome.getNode(TAGS) ;
    return contactServiceHome.addNode(TAGS, "nt:unstructured") ;
  }
  
  private Node getPublicContactGroupHome() throws Exception {
    Node contactServiceHome = getPublicContactServiceHome() ;
    if(contactServiceHome.hasNode(CONTACT_GROUP)) return contactServiceHome.getNode(CONTACT_GROUP) ;
    return contactServiceHome.addNode(CONTACT_GROUP,"nt:unstructured") ;
  }
  
  private Node getPublicContactHome() throws Exception {
    Node contactServiceHome = getPublicContactServiceHome() ;
    if(contactServiceHome.hasNode(CONTACTS)) return contactServiceHome.getNode(CONTACTS) ;
    return contactServiceHome.addNode(CONTACTS, "nt:unstructured") ;
  }
  
  private Session getJCRSession() throws Exception {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider() ;
    String defaultWS = 
      repositoryService_.getDefaultRepository().getConfiguration().getDefaultWorkspaceName() ;
    return sessionProvider.getSession(defaultWS, repositoryService_.getCurrentRepository());
  }

  private String [] ValuesToStrings(Value[] Val) throws Exception {
    if(Val.length == 1) return new String[]{Val[0].getString()};
    String[] Str = new String[Val.length];
    for(int i = 0; i < Val.length; ++i) {
      Str[i] = Val[i].getString();
    }
    return Str;
  }
  
  private Contact getContact(Node contactNode) throws Exception {
    Contact contact = new Contact();
    if(contactNode.hasProperty("exo:id")) contact.setId(contactNode.getProperty("exo:id").getString()) ;
    if(contactNode.hasProperty("exo:fullName"))contact.setFullName(contactNode.getProperty("exo:fullName").getString());
    if(contactNode.hasProperty("exo:firstName"))contact.setFirstName(contactNode.getProperty("exo:firstName").getString());
    if(contactNode.hasProperty("exo:middleName"))contact.setMiddleName(contactNode.getProperty("exo:middleName").getString());
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
    if(contactNode.hasProperty("exo:categories"))contact.setCategories(ValuesToStrings(contactNode.getProperty("exo:categories").getValues()));
    if(contactNode.hasProperty("exo:tags")) contact.setTags(ValuesToStrings(contactNode.getProperty("exo:tags").getValues()));
    if(contactNode.hasProperty("exo:editPermission")) contact.setEditPermission(ValuesToStrings(contactNode.getProperty("exo:editPermission").getValues()));
    if(contactNode.hasProperty("exo:lastUpdated"))contact.setLastUpdated(contactNode.getProperty("exo:lastUpdated").getDate().getTime());
    if(contactNode.hasProperty("exo:isShared")) contact.setShared(contactNode.getProperty("exo:isShared").getBoolean());

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
    return contact;
  }

  public List<Contact> getAllContact(String username) throws Exception {
    Node contactHomeNode = getContactHome(username);
    List<Contact> contacts = new ArrayList<Contact>();
    NodeIterator iter = contactHomeNode.getNodes();
    Contact contact;
    while (iter.hasNext()) {
      Node contactNode = iter.nextNode();
      contact = getContact(contactNode);
      contacts.add(contact);
    }
    return contacts;
  }

  public ContactPageList getContactPageListByGroup(String username, ContactFilter filter, boolean isPublic) throws Exception {
    QueryManager qm ;
    if (isPublic) {
      Node publicContactHomeNode = getPublicContactHome() ;
      filter.setAccountPath(publicContactHomeNode.getPath()) ;
      qm = publicContactHomeNode.getSession().getWorkspace().getQueryManager();
    } else {
      Node contactHomeNode = getContactHome(username);
      filter.setAccountPath(contactHomeNode.getPath()) ;
      qm = contactHomeNode.getSession().getWorkspace().getQueryManager();
    }
    Query query = qm.createQuery(filter.getStatement(), Query.XPATH);    
    QueryResult result = query.execute();    
    ContactPageList pageList = new ContactPageList(username, result.getNodes(), 10, filter.getStatement(), true) ;
    return pageList ;
  }

  public ContactPageList getContactPageListByTag(String username, ContactFilter filter) throws Exception {
    return null ;
  }
  
  public Contact getContact(String username, String contactId) throws Exception {
    Node contactHomeNode = getContactHome(username);
    if(contactHomeNode.hasNode(contactId)) {
      Node contactNode = contactHomeNode.getNode(contactId);
      return getContact(contactNode);
    }
    return null;
  }

  public ContactPageList getContactPageListByGroup(String username, String groupId) throws Exception {
    Node contactHome = getContactHome(username);
    QueryManager qm = contactHome.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root" + contactHome.getPath() 
                                                + "//element(*,exo:contact)[@exo:categories='").
                                                append(groupId).
                                                append("']");
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    ContactPageList pageList = new ContactPageList(username, result.getNodes(), 10, queryString.toString(), true) ;
    return pageList ;
  }
  
  public List<String>  getAllEmailAddressByGroup(String username, String groupId) throws Exception {
    Node contactHome = getContactHome(username);
    QueryManager qm = contactHome.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root" + contactHome.getPath() 
                                                + "//element(*,exo:contact)[@exo:categories='").
                                                append(groupId).
                                                append("']");
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
  private ContactGroup getGroup(Node contactGroupNode) throws Exception {
    ContactGroup contactGroup = new ContactGroup();
    if (contactGroupNode.hasProperty("exo:id")) 
      contactGroup.setId(contactGroupNode.getProperty("exo:id").getString());
    if (contactGroupNode.hasProperty("exo:name")) 
      contactGroup.setName(contactGroupNode.getProperty("exo:name").getString());
    if (contactGroupNode.hasProperty("exo:description")) 
      contactGroup.setDescription(contactGroupNode.getProperty("exo:description").getString());    
    return contactGroup;
  }

  public ContactGroup getGroup(String username, String groupId) throws Exception {
    Node contactGroupHomeNode = getContactGroupHome(username);
    String str = groupId.replaceAll("/", "") ;
    if (contactGroupHomeNode.hasNode(str))
      return getGroup(contactGroupHomeNode.getNode(str)) ;
    return null;
  }

  public List<ContactGroup> getGroups(String username) throws Exception {
    Node contactGroupHomeNode = getContactGroupHome(username);
    List<ContactGroup> contactGroups = new ArrayList<ContactGroup>();
    NodeIterator iter = contactGroupHomeNode.getNodes();
    while (iter.hasNext()) {
      Node contactGroupNode = iter.nextNode();
      contactGroups.add(getGroup(contactGroupNode));
    }
    return contactGroups;
  }

  public List<Contact> removeContacts(String username, List<String> contactIds) throws Exception {
    Node contactHomeNode = getContactHome(username);
    Node publicContactHomeNode = getPublicContactHome() ;
    List<Contact> contacts = new ArrayList<Contact>() ;
    Contact contact ;
    for (String contactId : contactIds)    
      if (contactHomeNode.hasNode(contactId)) {
        contact = getContact(username, contactId);
        if(canRemove(username, contact)) {
          contactHomeNode.getNode(contactId).remove();
          contactHomeNode.getSession().save();
        }else {
          contacts.add(contact) ;
        }
      }else {
        if(publicContactHomeNode.hasNode(contactId)){
          contact = getSharedContact(contactId);
          if(canRemove(username, contact)) {
            publicContactHomeNode.getNode(contactId).remove();
            publicContactHomeNode.getSession().save();
          }else {
            contacts.add(contact) ;
          }
        }
      }
    return contacts ;
  }
  
  private boolean canRemove(String username, Contact contact) {
    return true ;
  }
  private boolean canMove(String username, Contact contact) {
    return true ;
  }
  
  public List<Contact> moveContacts(String username, List<String> contactIds, String[] groupId) throws Exception {
    Node contactHomeNode = getContactHome(username);
    Node publicContactHomeNode = getPublicContactHome() ;
    Contact contact ;
    List<Contact> contacts = new ArrayList<Contact>() ;
    Node contactNode ;
    for (String contactId : contactIds)    
      if (contactHomeNode.hasNode(contactId)) {
        contact = getContact(username, contactId);
        if(canMove(username, contact)) {
          contactNode = contactHomeNode.getNode(contactId) ;
          contactNode.setProperty("exo:categories", groupId);
          contactHomeNode.getSession().save();
          contacts.add(contact) ;
        }
      }else {
        if(publicContactHomeNode.hasNode(contactId)){
          contact = getSharedContact(contactId);
          if(canMove(username, contact)) {
            contactNode =  publicContactHomeNode.getNode(contactId);
            contactNode.setProperty("exo:categories", groupId);
            publicContactHomeNode.getSession().save();
            contacts.add(contact) ;
          }
        }
      }
    return contacts ;
  }
  
  private List<String> getContactNodesByGroup(String username, String groupId) throws Exception {
    Node contactHome = getContactHome(username);
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
  
  public ContactGroup removeGroup(String username, String groupId) throws Exception {
    Node contactGroupHomeNode = getContactGroupHome(username);
    if (contactGroupHomeNode.hasNode(groupId)) {
      ContactGroup contactGroup = getGroup(username, groupId);
      contactGroupHomeNode.getNode(groupId).remove();
      contactGroupHomeNode.save();
      contactGroupHomeNode.getSession().save();
      removeContacts(username, getContactNodesByGroup(username, groupId)) ;
      return contactGroup;
    }
    return null;
  }
  
  public void saveContact(String username, Contact contact, boolean isNew) throws Exception {
    Node contactHomeNode = getContactHome(username);
    Node contactNode;
    if (isNew) {
      contactNode = contactHomeNode.addNode(contact.getId(), "exo:contact"); 
      contactNode.setProperty("exo:id", contact.getId());
    } else {
      contactNode = contactHomeNode.getNode(contact.getId());
    }
    
    contactNode.setProperty("exo:fullName", contact.getFullName());
    contactNode.setProperty("exo:firstName", contact.getFirstName());
    contactNode.setProperty("exo:middleName", contact.getMiddleName());
    contactNode.setProperty("exo:lastName", contact.getLastName());
    contactNode.setProperty("exo:nickName", contact.getNickName());
    contactNode.setProperty("exo:gender", contact.getGender()) ;
    
    GregorianCalendar dateTime = new GregorianCalendar() ;
    dateTime.setTime(contact.getBirthday()) ;    
    contactNode.setProperty("exo:birthday", dateTime) ;
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
    contactNode.setProperty("exo:categories", contact.getCategories());
    contactNode.setProperty("exo:tags", contact.getTags());
    contactNode.setProperty("exo:editPermission", contact.getEditPermission());
    
    if (contact.getLastUpdated() != null) {
      dateTime.setTime(contact.getLastUpdated()) ;    
      contactNode.setProperty("exo:lastUpdated", dateTime); 
    }
    contactNode.setProperty("exo:isShared", contact.isShared());
    
    // save image to contact
    ContactAttachment attachment = contact.getAttachment() ;
    if (attachment != null) {
      if (attachment.getFileName() != null) {
        Node nodeFile = null ;
        if (contactNode.hasNode("image"))nodeFile = contactNode.getNode("image") ;
        else nodeFile = contactNode.addNode("image", "nt:file");
        Node nodeContent = null ;
        if (nodeFile.hasNode("jcr:content")) nodeContent = nodeFile.getNode("jcr:content") ;
        else nodeContent = nodeFile.addNode("jcr:content", "nt:resource") ;
        nodeContent.setProperty("jcr:mimeType", attachment.getMimeType()) ;
        nodeContent.setProperty("jcr:data", attachment.getInputStream());
        nodeContent.setProperty("jcr:lastModified", Calendar.getInstance().getTimeInMillis());
      }
    }else {
      if(contactNode.hasNode("image")) contactNode.getNode("image").remove() ;
    }   
    contactHomeNode.getSession().save();
  }
  
  

  public void saveGroup(String username, ContactGroup group, boolean isNew) throws Exception {
    Node groupHomeNode = getContactGroupHome(username);
    Node groupNode;
    if (isNew) {
      groupNode = groupHomeNode.addNode(group.getId(), "exo:contactGroup");
      groupNode.setProperty("exo:id", group.getId());
    } else {
      groupNode = groupHomeNode.getNode(group.getId());
    }    
    groupNode.setProperty("exo:name", group.getName());
    groupNode.setProperty("exo:description", group.getDescription());
    groupHomeNode.getSession().save();
  }

  public List<GroupContactData> getPublicContacts(String[] groupIds) throws Exception {
    Node publicGroupHome = getPublicContactGroupHome() ;
    List<Contact> contacts  ;
    List<GroupContactData> contactByGroup = new ArrayList<GroupContactData>() ;
    for(String groupId : groupIds) {
      if(publicGroupHome.hasNode(groupId)) {
        Node groupNode = publicGroupHome.getNode(groupId) ;
        if(groupNode.isNodeType("mix:referenceable")) {
          PropertyIterator referencedProperties = groupNode.getReferences() ;
          if(referencedProperties.getSize() > 0) {
            contacts = new ArrayList<Contact> () ;
            while(referencedProperties.hasNext()) {
              contacts.add(getContact(referencedProperties.nextProperty().getParent())) ;
            }
            contactByGroup.add(new GroupContactData(groupId, contacts)) ;
          }          
        }
      }
    }
    return contactByGroup ;
  }
  
  public List<Contact> shareContacts(String username, List<String> contactIds, String[] groupIds) throws Exception {
    Node groupNode ;
    Node publicGroupHome = getPublicContactGroupHome() ;
    List<Contact> contacts = new ArrayList<Contact>() ;
    Contact contact ;
    for(String groupId : groupIds){
      if(publicGroupHome.hasNode(groupId)) groupNode = publicGroupHome.getNode(groupId) ;
      else {
        groupNode = publicGroupHome.addNode(groupId, "exo:contactGroup") ;
        groupNode.setProperty("exo:id", groupId) ;
        groupNode.setProperty("exo:name", groupId) ;
        groupNode.addMixin("mix:referenceable") ;        
      }
      for (String contactId : contactIds) {
        contact = getContact(username, contactId) ;
        contacts.add(contact) ;
        Value value2add = publicGroupHome.getSession().getValueFactory().createValue(groupNode); 
        Node contactNode = (Node)publicGroupHome.getSession().getItem(contact.getPath()) ;
        if (!contactNode.isNodeType("exo:categorized")) {     
          contactNode.addMixin("exo:categorized");    
          contactNode.setProperty("exo:category", new Value[] {value2add});
        } else {
          List<Value> vals = new ArrayList<Value>();
          Value[] values = contactNode.getProperty("exo:category").getValues();
          for (int i = 0; i < values.length; i++) {
            Value value = values[i];
            String uuid = value.getString();
            Node refNode = publicGroupHome.getSession().getNodeByUUID(uuid);       
            if(!refNode.getPath().equals(groupNode.getPath()))
              vals.add(value);
          }
          vals.add(value2add);
          contactNode.setProperty("exo:category", vals.toArray(new Value[vals.size()]));        
        }
      }
    }
    publicGroupHome.getSession().save() ;
    return contacts;
  }

  public Contact getSharedContact(String contactId) throws Exception {
    Node contactHomeNode = getPublicContactHome();
    if(contactHomeNode.hasNode(contactId)) {
      Node contactNode = contactHomeNode.getNode(contactId);
      Contact contact = new Contact();
      contact = getContact(contactNode);
      return contact;
    }
    return null;
  }

  public ContactPageList getSharedContactsByGroup(String groupId) throws Exception {
    Node contactHome = getPublicContactHome();
    QueryManager qm = contactHome.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root" + contactHome.getPath() 
                                                + "//element(*,exo:contact)[@exo:categories='").
                                                append(groupId).
                                                append("']");
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    return new ContactPageList(null, result.getNodes(), 10, queryString.toString(), true) ;
  }
  
  public List<GroupContactData> getSharedContacts(String[] groupIds) throws Exception {
    List<GroupContactData> contactByGroup = new ArrayList<GroupContactData>() ;
    List<Contact> contacts;
    for(String groupId : groupIds) { 
      contacts = getSharedContactsByGroup(groupId).getAll();
      if(contacts.size() > 0)
        contactByGroup.add(new GroupContactData(groupId, contacts));     
    }
    return contactByGroup;
  }
  
  public List<String> getSharedGroupContacts(String[] groupIds) throws Exception {
    List<String> groups = new ArrayList<String>();
    for(String groupId : groupIds) { 
      if(hasContacts(groupId))  groups.add(groupId) ; 
    }
    return groups;
  }
  
  private boolean hasContacts(String groupId) throws Exception {
    Node contactHome = getPublicContactHome();
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
  public Contact removeSharedContact(String contactId) throws Exception {
    Node contactHomeNode = getPublicContactHome();
    if (contactHomeNode.hasNode(contactId)) {
      Contact contact = getSharedContact(contactId);
      contactHomeNode.getNode(contactId).remove();
      contactHomeNode.getSession().save();
      return contact;
    }
    return null;
  }

  public void saveSharedContact(Contact contact, boolean isNew) throws Exception {
    Node contactHomeNode = getPublicContactHome();
    Node contactNode;
    if (isNew) {
      contactNode = contactHomeNode.addNode(contact.getId(), "exo:contact"); 
      contactNode.setProperty("exo:id", contact.getId());
    } else {
      contactNode = contactHomeNode.getNode(contact.getId());
    }
    contactNode.setProperty("exo:fullName", contact.getFullName());
    contactNode.setProperty("exo:firstName", contact.getFirstName());
    contactNode.setProperty("exo:middleName", contact.getMiddleName());
    contactNode.setProperty("exo:lastName", contact.getLastName());
    contactNode.setProperty("exo:nickName", contact.getNickName());
    contactNode.setProperty("exo:gender", contact.getGender()) ;
    
    GregorianCalendar dateTime = new GregorianCalendar() ;
    dateTime.setTime(contact.getBirthday()) ;    
    contactNode.setProperty("exo:birthday", dateTime) ;
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
    contactNode.setProperty("exo:categories", contact.getCategories());
    contactNode.setProperty("exo:editPermission", contact.getEditPermission());

    dateTime.setTime(contact.getLastUpdated()) ;
    contactNode.setProperty("exo:lastUpdated", dateTime);
    contactNode.setProperty("exo:isShared", contact.isShared());
    
//  save image to contact
    ContactAttachment attachment = contact.getAttachment() ;
    if (attachment != null) {
      if (attachment.getFileName() != null) {
        Node nodeFile = null ;
        if (contactNode.hasNode("image"))nodeFile = contactNode.getNode("image") ;
        else nodeFile = contactNode.addNode("image", "nt:file");
        Node nodeContent = null ;
        if (nodeFile.hasNode("jcr:content")) nodeContent = nodeFile.getNode("jcr:content") ;
        else nodeContent = nodeFile.addNode("jcr:content", "nt:resource") ;
        nodeContent.setProperty("jcr:mimeType", attachment.getMimeType()) ;
        nodeContent.setProperty("jcr:data", attachment.getInputStream());
        nodeContent.setProperty("jcr:lastModified", Calendar.getInstance().getTimeInMillis());
      }
    }else {
      if(contactNode.hasNode("image")) contactNode.getNode("image").remove() ;
    }    
    contactHomeNode.getSession().save(); 
  }

  private void saveSharedGroup(ContactGroup group, boolean isNew) throws Exception  {
    Node sharedGroupHomeNode = getPublicContactGroupHome();
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
  } 
  
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
    Node tagHome = getTagHome(username) ;
    if (tagHome.hasNode(tag.getId())) {
      Node tagNode = tagHome.getNode(tag.getId());
      tagNode.setProperty("exo:name", tag.getName());
      tagNode.setProperty("exo:description", tag.getDescription());
      tagNode.setProperty("exo:color", tag.getColor());
    }
    tagHome.save();
  }
  
  
  public Tag getTag(String username, String tagId) throws Exception {
    Node tagHomeNode = getTagHome(username);
    if (tagHomeNode.hasNode(tagId)) 
      return getTag(tagHomeNode.getNode(tagId));
    return null ;
  }
  
  public List<Tag> getTags(String username) throws Exception {
    Node tagHomeNode = getTagHome(username);
    List<Tag> tags = new ArrayList<Tag>();
    NodeIterator iter = tagHomeNode.getNodes();
    while (iter.hasNext()) {
      Node tagNode = iter.nextNode();
      tags.add(getTag(tagNode));
    }
    return tags;
  }

  public DataPageList getContactPageListByTag(String username, String tagName) throws Exception {
    //query on private contacts
    Node contactHome = getContactHome(username);
    QueryManager qm = contactHome.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root" + contactHome.getPath() 
                                                + "//element(*,exo:contact)[@exo:tags='").
                                                append(tagName).
                                                append("']");
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    NodeIterator it = result.getNodes();    
    List<Contact> contacts = new ArrayList<Contact>();
    while (it.hasNext()) {
      contacts.add(getContact(it.nextNode()));
    }
    //query on public contacts
    Node publicContactHome = getPublicContactHome();
    queryString = new StringBuffer("/jcr:root" + publicContactHome.getPath() 
                                                + "//element(*,exo:contact)[@exo:tags='").
                                                append(tagName).
                                                append("']");
    query = qm.createQuery(queryString.toString(), Query.XPATH);
    result = query.execute();
    it = result.getNodes();    
    while (it.hasNext()) {
      contacts.add(getContact(it.nextNode()));
    }
    
    return new DataPageList(contacts, 10, null, false) ;
  }
  
  public void addTag(String username, List<String> contactIds, List<Tag> tags) throws Exception {
    Node tagHomeNode = getTagHome(username);
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
    Node contactHomeNode = getContactHome(username);
    Node publicContactHomeNode = getPublicContactHome();
    Node contactNode = null ;
    for(String contactId : contactIds) {      
      if(contactHomeNode.hasNode(contactId)) {
        contactNode = contactHomeNode.getNode(contactId) ;
      } else if (publicContactHomeNode.hasNode(contactId)) {
        contactNode = publicContactHomeNode.getNode(contactId);       
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
    publicContactHomeNode.getSession().save();
  }
  
  public Tag removeTag(String username, String tagId) throws Exception {
    Node tagHomeNode = getTagHome(username);
    if (tagHomeNode.hasNode(tagId)) {
      Node contactHome = getContactHome(username) ;
      Node publicContactHome = getPublicContactHome() ;
      List<Contact> contacts = getContactPageListByTag(username, tagId).getAll();
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
      
      Tag tag = getTag(username, tagId);
      tagHomeNode.getNode(tagId).remove();
      tagHomeNode.save();
      tagHomeNode.getSession().save();
      return tag;
    }
    return null;
  }
  
  public void removeContactTag(String username, List<String> contactIds, List<String> tags) throws Exception {
    Node contactHome = getContactHome(username) ;
    Node publicContactHome = getPublicContactHome() ;
    Node contact = null ;
    for(String contactId : contactIds) {
      if(contactHome.hasNode(contactId)) {
        contact = contactHome.getNode(contactId) ;
      } else if(publicContactHome.hasNode(contactId)) {
          contact = publicContactHome.getNode(contactId) ;
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
  
  public DataPageList searchContact(String username, ContactFilter filter)throws Exception {
    List<Contact> contacts = new ArrayList<Contact>() ;
    Query query ;
    QueryManager qm ;
    if(username != null && username.length() > 0) {
      Node contactHome = getContactHome(username) ;
      filter.setAccountPath(contactHome.getPath()) ;
      qm = contactHome.getSession().getWorkspace().getQueryManager() ;      
      query = qm.createQuery(filter.getStatement(), Query.XPATH) ;      
      NodeIterator it = query.execute().getNodes() ;
      while(it.hasNext()) {
        contacts.add(getContact(it.nextNode())) ;        
      }
    }
    Node publicContactHome = getPublicContactHome() ;
    filter.setAccountPath(publicContactHome.getPath()) ;
    qm = publicContactHome.getSession().getWorkspace().getQueryManager() ;
    query = qm.createQuery(filter.getStatement(), Query.XPATH) ;
    QueryResult result = query.execute();
    NodeIterator itpublic = result.getNodes();
    while(itpublic.hasNext()) {
      contacts.add(getContact(itpublic.nextNode())) ;
    }
    return new DataPageList(contacts, 10, null, false) ;    
  }
}
