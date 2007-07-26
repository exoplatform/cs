/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.service.impl;



import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.registry.JCRRegistryService;
import org.exoplatform.registry.ServiceRegistry;
import org.exoplatform.services.jcr.RepositoryService;

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

  public List<Contact> getAllContact(String username) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public Contact getContact(String username, String groupId, String contactId) throws Exception {
    // TODO Auto-generated method stub
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
