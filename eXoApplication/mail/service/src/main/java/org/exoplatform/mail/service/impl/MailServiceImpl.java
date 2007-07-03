/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.service.impl;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.Contact;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.MessageFilter;
import org.exoplatform.mail.service.MessageHeader;
import org.exoplatform.services.jcr.RepositoryService;
/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan.nguyen@exoplatform.com
 * Jun 23, 2007  
 */
public class MailServiceImpl implements MailService{
  
  final protected static String MAIL_HOME = "mailHome".intern() ;
  
  private DataStorage storage_ ;
  RepositoryService  repositoryService_ ;
  
  public MailServiceImpl(RepositoryService  repositoryService) {
    repositoryService_ = repositoryService ;
    
    //storage_ =  storage  ;
  }
  
  /**
   * @param username
   * @return
   * @throws Exception
   */
  public List<Account> getAccounts(String username) throws Exception {
    return null ;
  }
  
  public Account getAccountById(String username, String id) throws Exception {
    return null ;
  }
  
  public void saveAccount(String username, Account account, boolean isNew) throws Exception {
    
  }
  
  public void updateAccount(String username, Account account) throws Exception {
    // TODO Auto-generated method stub
    
  }
  
  public void removeAccount(String username, Account account) throws Exception {
    
  }
  
  public Folder getFolder(String username, String folderId) throws Exception {
    return null ;
  }
  
  public void saveUserFolder(String username, String accountId, Folder folder) throws Exception {
    
  }
  
  public void removeUserFolder(String username, Folder folder) throws Exception {
    
  }
  
  public void removeUserFolder(String username, Account account, Folder folder) throws Exception {
    // TODO Auto-generated method stub
    
  }
  
  public Message getMessageById(String username, String id) throws Exception {
    return null ;
  }
  
  public void removeMessage(String username, String messageId) throws Exception {
    
  }
  
  public void removeMessage(String username, String[] messageId) throws Exception {
    
  }
  
  public List<MessageHeader> getMessageByFolder(String username, Folder folder) throws Exception {
    return null ;
  }
  
  public List<MessageHeader> getMessageByFilter(String username, MessageFilter filter) throws Exception {
    return null ;
  }

  public void sendMessage(Message message) throws Exception {
    
  }

  public void addTag(String username, Message message, String tag) throws Exception {
    // TODO Auto-generated method stub
    
  }
  
  public void removeTag(String username, Account account, String tag) throws Exception {
    // TODO Auto-generated method stub
    
  }

  public void removeTag(String username, Message message, String tag) throws Exception {
    // TODO Auto-generated method stub
    
  }
  
  public int checkNewMessage(String username, Account account) throws Exception {
    // TODO Auto-generated method stub
    return 0;
  }

  public void createAccount(String username, Account account) throws Exception {
    Node mailHome = getMailHome() ;
    // Add new account node to mailHome node
    
  }
  
  public void addContact(String username, Contact contact) throws Exception {
    // TODO Auto-generated method stub
    
  }
  
  public List<Contact> getContacts(String username) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  private Node getMailHome() throws Exception {
    String defaultWS = repositoryService_.getDefaultRepository().getConfiguration().getDefaultWorkspaceName() ;
    Session session = repositoryService_.getDefaultRepository().getSystemSession(defaultWS) ;
    if(session.getRootNode().hasNode(MAIL_HOME)) return session.getRootNode().getNode(MAIL_HOME) ;
    return session.getRootNode().addNode(MAIL_HOME) ;
  }

 
}