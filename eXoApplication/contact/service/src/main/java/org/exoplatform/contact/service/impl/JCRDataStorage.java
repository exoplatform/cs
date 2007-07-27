/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.service.impl;



import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.Value;

import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactGroup;
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
  
  private Node getContactHome(String username) throws Exception {
    Node contactServiceHome = getContactServiceHome(username) ;
    if(contactServiceHome.hasNode(CONTACTS)) return contactServiceHome.getNode(CONTACTS) ;
    return contactServiceHome.addNode(CONTACTS) ;
  }
  
  public Node getContactGroupHome(String username) throws Exception {
    Node contactServiceHome = getContactServiceHome(username) ;
    if(contactServiceHome.hasNode(CONTACT_GROUP)) return contactServiceHome.getNode(CONTACT_GROUP) ;
    return contactServiceHome.addNode(CONTACT_GROUP) ;
  }
  
  private Session getJCRSession() throws Exception {
    String defaultWS = 
      repositoryService_.getDefaultRepository().getConfiguration().getDefaultWorkspaceName() ;
    return repositoryService_.getDefaultRepository().getSystemSession(defaultWS) ;
  }

  private String [] ValuesToStrings(Value[] Val) {
		String[] Str = new String[(int) Val.length];
		for(int i = 0; i < Val.length; ++i) {
		  Str[i] = Val[i].toString();
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
		if(contactNode.hasProperty("exo:groups"))contact.setGroups(ValuesToStrings(contactNode.getProperty("exo:groups").getValues()));
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

  public Contact getContact(String username, String groupId, String contactId) throws Exception {
    Node contactHomeNode = getContactHome(username);
    Node contactGroupHomeNode = getContactGroupHome(username);
    if(contactGroupHomeNode.hasNode(groupId)) {
    	if(contactHomeNode.hasNode(contactId)) {
    		Node contactNode = contactHomeNode.getNode(contactId);
    		Contact contact = new Contact();
    		contact = getContact(contactNode);
    		return contact;
    	}
    }
  	return null;
  }

  public List<Contact> getContactsByGroup(String username, String groupId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public ContactGroup getGroup(String username, String groupId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public List<ContactGroup> getGroups(String username) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public List<Contact> getPublicContact() throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public Contact removeContact(String username, String groupId, String contactId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public ContactGroup removeGroup(String username, String groupId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public void saveContact(String username, Contact contact, boolean isNew) throws Exception {
    // TODO Auto-generated method stub
    
  }

  public void saveGroup(String username, ContactGroup group, boolean isNew) throws Exception {
    // TODO Auto-generated method stub
    
  }

  
}
