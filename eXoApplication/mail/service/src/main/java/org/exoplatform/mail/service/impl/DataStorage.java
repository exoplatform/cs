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
import org.exoplatform.mail.service.MailSetting;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.MessageFilter;
import org.exoplatform.mail.service.MessageHeader;
import org.exoplatform.mail.service.Tag;

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
  
  public MailSetting  getMailSetting(String username) throws Exception ;
  public void saveMailSetting(String username, MailSetting newSetting) throws Exception;

  public void removeMessage(String username, String accountId, String messageId) throws Exception ;

  public void removeMessage(String username, String accountId, List<String> messageIds) throws Exception ;

  public void saveAccount(String username, Account account, boolean isNew) throws Exception ;

  public void saveMessage(String username, String accountId, Message message, boolean isNew) throws Exception ;

  public Folder getFolder(String username, String accountId, String folderName) throws Exception ;
  public List<Folder> getFolders(String username, String accountId) throws Exception ;
  public void saveUserFolder(String username, String accountId, Folder folder) throws Exception ;

  public void removeUserFolder(String username, Folder folder) throws Exception ;
  public void removeUserFolder(String username, Account account, Folder folder) throws Exception ;
  
  public List<Tag> getTags(String username, String accountId) throws Exception ;
  public void addTag(String username, String accountId, List<String> messagesId, List<Tag> listTag) throws Exception ;
  public void removeMessageTag(String username, String accountId, List<String> messageIds, List<String> tagNames) throws Exception ;
  public void removeTag(String username, String accountId, String tagName) throws Exception ;
  
}