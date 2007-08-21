/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.service.impl;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.MessageFilter;
import org.exoplatform.mail.service.MessageHeader;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan.nguyen@exoplatform.com
 * Jun 23, 2007  
 */
public interface DataStorage {
  /**
   * @param username
   * @return
   * @throws Exception
   */
  
  public Account getAccountById(String username, String id) throws Exception ;
   

  public List<Account> getAccounts(String username) throws Exception ;

  public Account getAccount(Node accountNode) throws Exception;
  public Message getMessageById(String username, String accountId, String id) throws Exception ;

  public List<MessageHeader> getMessages(String username, MessageFilter filter) throws Exception ;

  public Message getMessage(Node messageNode) throws Exception ;

  public void removeMessage(String username, String accountId, String messageId) throws Exception ;

  public void removeMessage(String username, String accountId, String[] messageId) throws Exception ;

  public void saveAccount(String username, Account account, boolean isNew) throws Exception ;

  public void saveMessage(String username, String accountId, Message message, boolean isNew) throws Exception ;

  public Folder getFolder(String username, String accountId, String folderName) throws Exception ;
  public void saveUserFolder(String username, String accountId, Folder folder) throws Exception ;

  public void removeUserFolder(String username, Folder folder) throws Exception ;

  public void removeUserFolder(String username, Account account, Folder folder) throws Exception ;

  public Node getMailHomeNode(String username) throws Exception ;

  public Node getMessageHome(String username, String accountId) throws Exception ;

  public Node getFolderHome(String username, String accountId) throws Exception ;

  public Node getTagHome(String username, String accountId) throws Exception ;

  public Session getJCRSession() throws Exception ;
}