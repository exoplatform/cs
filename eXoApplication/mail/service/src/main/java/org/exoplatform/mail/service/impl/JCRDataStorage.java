/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.service.impl;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.MessageFilter;
import org.exoplatform.mail.service.MessageHeader;
import org.exoplatform.registry.JCRRegistryService;
import org.exoplatform.registry.ServiceRegistry;
import org.exoplatform.services.jcr.RepositoryService;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan.nguyen@exoplatform.com
 * Jun 23, 2007  
 */
public class JCRDataStorage implements DataStorage{
  private RepositoryService  repositoryService_ ;
  private JCRRegistryService jcrRegistryService_ ;
  //private SimpleCredentials credentials_ = new SimpleCredentials("exoadmin", "exo".toCharArray());
  
  public JCRDataStorage(RepositoryService  repositoryService, JCRRegistryService jcrRegistryService) {
    repositoryService_ = repositoryService ;
    jcrRegistryService_ = jcrRegistryService ;
  }
  

  public Account getAccountById(String username, String id) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public List<Account> getAccounts(String username) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public Message getMessageById(String username, String id) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public List<MessageHeader> getMessages(String username, MessageFilter filter) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public void removeAccount(String username, Account account) throws Exception {
    // TODO Auto-generated method stub
    
  }

  public void removeMessage(String username, String messageId) throws Exception {
    // TODO Auto-generated method stub
    
  }

  public void removeMessage(String username, String[] messageId) throws Exception {
    // TODO Auto-generated method stub
    
  }

  public void saveAccount(String username, Account account, boolean isNew) throws Exception {
    // TODO Auto-generated method stub
    
  }

  public void saveMessage(String username, Message message, boolean isNew) throws Exception {
    // TODO Auto-generated method stub
    
  }
  
  private Node getMailHomeNode(String username) throws Exception {
    ServiceRegistry serviceRegistry = new ServiceRegistry("MailService") ;
    Session session = getJCRSession() ;
    if(jcrRegistryService_.getUserNode(session, username) == null)
      jcrRegistryService_.createUserHome(username, false) ;
    jcrRegistryService_.createServiceRegistry(username, serviceRegistry, false) ;    
    return jcrRegistryService_.getServiceRegistryNode(session, username, serviceRegistry.getName()) ;
  }
  
  private Node getMessageHome(String username, String accountId) throws Exception {
    Node home = getMailHomeNode(username);
    Account account = getAccountById(username, accountId);
    return home.getNode(account.getUserDisplayName()).getNode("Messages");
  }
  
  private Node getFolderHome(String username, String accountId) throws Exception {
    Node home = getMailHomeNode(username);
    Account account = getAccountById(username, accountId);
    return home.getNode(account.getUserDisplayName()).getNode("Folders");
  }
  
  private Node getTagHome(String username, String accountId) throws Exception {
    Node home = getMailHomeNode(username);
    Account account = getAccountById(username, accountId);
    return home.getNode(account.getUserDisplayName()).getNode("Tags");
  }
  
  private Session getJCRSession() throws Exception {
    String defaultWS = 
      repositoryService_.getDefaultRepository().getConfiguration().getDefaultWorkspaceName() ;
    return repositoryService_.getDefaultRepository().getSystemSession(defaultWS) ;
  }
}