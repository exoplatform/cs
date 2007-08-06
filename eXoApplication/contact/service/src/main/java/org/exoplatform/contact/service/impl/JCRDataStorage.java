/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.service.impl;



import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.contact.service.GroupContactData;
import org.exoplatform.registry.JCRRegistryService;
import org.exoplatform.registry.ServiceRegistry;
import org.exoplatform.services.jcr.RepositoryService;

//import com.sun.org.apache.xalan.internal.xsltc.NodeIterator;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 10, 2007  
 */
public class JCRDataStorage implements DataStorage{
  
  final private static String CONTACTS = "contacts".intern() ;
  final private static String CONTACT_GROUP = "contactGroup".intern() ;
  
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
    return contactServiceHome.addNode(CONTACTS) ;
  }
  
  private Node getContactGroupHome(String username) throws Exception {
    Node contactServiceHome = getContactServiceHome(username) ;
    if(contactServiceHome.hasNode(CONTACT_GROUP)) return contactServiceHome.getNode(CONTACT_GROUP) ;
    return contactServiceHome.addNode(CONTACT_GROUP) ;
  }
  
  private Node getPublicContactGroupHome() throws Exception {
    Node contactServiceHome = getPublicContactServiceHome() ;
    if(contactServiceHome.hasNode(CONTACT_GROUP)) return contactServiceHome.getNode(CONTACT_GROUP) ;
    return contactServiceHome.addNode(CONTACT_GROUP) ;
  }
  
  private Node getPublicContactHome() throws Exception {
    Node contactServiceHome = getPublicContactServiceHome() ;
    if(contactServiceHome.hasNode(CONTACTS)) return contactServiceHome.getNode(CONTACTS) ;
    return contactServiceHome.addNode(CONTACTS) ;
  }
  
  private Session getJCRSession() throws Exception {
    String defaultWS = 
      repositoryService_.getDefaultRepository().getConfiguration().getDefaultWorkspaceName() ;
    return repositoryService_.getDefaultRepository().getSystemSession(defaultWS) ;
  }

  private String [] ValuesToStrings(Value[] Val) throws Exception {
    if(Val.length == 1) {
      return new String[]{Val[0].getString()};
    }
    String[] Str = new String[Val.length];
    for(int i = 0; i < Val.length; ++i) {
      Str[i] = Val[i].getString();
    }
    return Str;
  }
  
  private Contact getContact(Node contactNode) throws Exception {
    Contact contact = new Contact();
    if(contactNode.hasProperty("exo:id"))contact.setId(contactNode.getProperty("exo:id").getString());
    if(contactNode.hasProperty("exo:firstName"))contact.setFirstName(contactNode.getProperty("exo:firstName").getString());
    if(contactNode.hasProperty("exo:lastName"))contact.setLastName(contactNode.getProperty("exo:lastName").getString());
    if(contactNode.hasProperty("exo:emailAddress"))contact.setEmailAddress(contactNode.getProperty("exo:emailAddress").getString());
    if(contactNode.hasProperty("exo:homePhone"))contact.setHomePhone(contactNode.getProperty("exo:homePhone").getString());
    if(contactNode.hasProperty("exo:workPhone"))contact.setWorkPhone(contactNode.getProperty("exo:workPhone").getString());
    if(contactNode.hasProperty("exo:homeAddress"))contact.setHomeAddress(contactNode.getProperty("exo:homeAddress").getString());
    if(contactNode.hasProperty("exo:country"))contact.setCountry(contactNode.getProperty("exo:country").getString());
    if(contactNode.hasProperty("exo:postalCode"))contact.setPostalCode(contactNode.getProperty("exo:postalCode").getString());
    if(contactNode.hasProperty("exo:personalSite"))contact.setPersonalSite(contactNode.getProperty("exo:personalSite").getString());
    if(contactNode.hasProperty("exo:organization"))contact.setOrganization(contactNode.getProperty("exo:organization").getString());
    if(contactNode.hasProperty("exo:jobTitle"))contact.setJobTitle(contactNode.getProperty("exo:jobTitle").getString());
    if(contactNode.hasProperty("exo:companyAddress"))contact.setCompanyAddress(contactNode.getProperty("exo:companyAddress").getString());
    if(contactNode.hasProperty("exo:companySite"))contact.setCompanySite(contactNode.getProperty("exo:companySite").getString());
    if(contactNode.hasProperty("exo:groups"))contact.setCategories(ValuesToStrings(contactNode.getProperty("exo:groups").getValues()));
    contact.setPath(contactNode.getPath()) ;
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

  public Contact getContact(String username, String contactId) throws Exception {
    Node contactHomeNode = getContactHome(username);
    if(contactHomeNode.hasNode(contactId)) {
      Node contactNode = contactHomeNode.getNode(contactId);
      Contact contact = new Contact();
      contact = getContact(contactNode);
      return contact;
    }
    return null;
  }

  public List<Contact> getContactsByGroup(String username, String groupId) throws Exception {
    Node contactHome = getContactHome(username);
    QueryManager qm = contactHome.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root" + contactHome.getPath() 
                                                + "//element(*,exo:contact)[@exo:groups='").
                                                append(groupId).
                                                append("']");
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    NodeIterator it = result.getNodes();
    List<Contact> contacts = new ArrayList<Contact>();
    while (it.hasNext()) {
      contacts.add(getContact(it.nextNode()));
    }
    return contacts ;
  }
 
  private ContactGroup getGroup(Node contactGroupNode) throws Exception {
    ContactGroup contactGroup = new ContactGroup();
    if (contactGroupNode.hasProperty("exo:id")) contactGroup.setId(contactGroupNode.getProperty("exo:id").getString());
    if (contactGroupNode.hasProperty("exo:name")) contactGroup.setName(contactGroupNode.getProperty("exo:name").getString());
    return contactGroup;
  }

  public ContactGroup getGroup(String username, String groupId) throws Exception {
    Node contactGroupHomeNode = getContactGroupHome(username);
    if (contactGroupHomeNode.hasNode(groupId)) {
      Node contactGroupNode = contactGroupHomeNode.getNode(groupId);
      ContactGroup contactGroup = new ContactGroup();
      contactGroup = getGroup(contactGroupNode);
      return contactGroup;
    }
    return null;
  }

  public List<ContactGroup> getGroups(String username) throws Exception {
    Node contactGroupHomeNode = getContactGroupHome(username);
    List<ContactGroup> contactGroups = new ArrayList<ContactGroup>();
    NodeIterator iter = contactGroupHomeNode.getNodes();
    ContactGroup contactGroup;
    while (iter.hasNext()) {
      Node contactGroupNode = iter.nextNode();
      contactGroup = getGroup(contactGroupNode);
      contactGroups.add(contactGroup);
    }
    return contactGroups;
  }

  public List<Contact> getPublicContact() throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public Contact removeContact(String username, String contactId) throws Exception {
    Node contactHomeNode = getContactHome(username);
    Contact contact = new Contact();
    if (contactHomeNode.hasNode(contactId)) {
      contact = getContact(username, contactId);
      contactHomeNode.getNode(contactId).remove();
      
      contactHomeNode.getSession().save();
      return contact;
    }
    return null;
  }

  public ContactGroup removeGroup(String username, String groupId) throws Exception {
    Node contactGroupHomeNode = getContactGroupHome(username);
    ContactGroup contactGroup = new ContactGroup();
    if (contactGroupHomeNode.hasNode(groupId)) {
      contactGroup = getGroup(username, groupId);
      contactGroupHomeNode.getNode(groupId).remove();
      contactGroupHomeNode.save();
      contactGroupHomeNode.getSession().save();
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
    contactNode.setProperty("exo:firstName", contact.getFirstName());
    contactNode.setProperty("exo:lastName", contact.getLastName());
    contactNode.setProperty("exo:emailAddress", contact.getEmailAddress());
    contactNode.setProperty("exo:homePhone", contact.getHomePhone());
    contactNode.setProperty("exo:workPhone", contact.getWorkPhone());
    contactNode.setProperty("exo:homeAddress", contact.getHomeAddress());
    contactNode.setProperty("exo:country", contact.getCountry());
    contactNode.setProperty("exo:postalCode", contact.getPostalCode());
    contactNode.setProperty("exo:personalSite", contact.getPersonalSite());
    contactNode.setProperty("exo:organization", contact.getOrganization());
    contactNode.setProperty("exo:jobTitle", contact.getJobTitle());
    contactNode.setProperty("exo:companyAddress", contact.getCompanyAddress()); 
    contactNode.setProperty("exo:companySite", contact.getCompanySite());
    contactNode.setProperty("exo:groups", contact.getCategories());
    
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

  public Contact shareContact(Contact contact, String[] groupIds) throws Exception {
    Node groupNode ;
    Node publicGroupHome = getPublicContactGroupHome() ;
    for(String groupId : groupIds){
      if(publicGroupHome.hasNode(groupId)) groupNode = publicGroupHome.getNode(groupId) ;
      else {
        groupNode = publicGroupHome.addNode(groupId, "exo:contactGroup") ;
        groupNode.setProperty("exo:id", groupId) ;
        groupNode.setProperty("exo:name", groupId) ;
        groupNode.addMixin("mix:referenceable") ;        
      }
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
          if(refNode.getPath().equals(groupNode.getPath()))return null ;
          vals.add(value);
        }
        vals.add(value2add);
        contactNode.setProperty("exo:category", vals.toArray(new Value[vals.size()]));        
      }
    }
    publicGroupHome.getSession().save() ;
    return contact;
  }

  public Contact getSharedContact(String contactId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public List<GroupContactData> getSharedContacts(String[] groupIds) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public Contact removeSharedContact(String contactId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public void saveSharedContact(Contact contact, boolean isNew) throws Exception {
    // TODO Auto-generated method stub
    
  }  
}
