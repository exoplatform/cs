/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.service.impl;

import java.util.List;

import org.exoplatform.mail.service.Account;
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
  public List<Account> getAccounts(String username) throws Exception ;
  public Account getAccount(String username, String id) throws Exception ;
  public void saveAccount(String username, Account account, boolean isNew) throws Exception ;
  public void removeAccount(String username, Account account) throws Exception ;
  
  public Message getMessageById(String username, String id) throws Exception ;
  public void saveMessage(String username, Message message, boolean isNew) throws Exception ;
  public void removeMessage(String username, String messageId) throws Exception ;
  public void removeMessage(String username, String[] messageId) throws Exception ;
  public List<MessageHeader> getMessages(String username, MessageFilter filter) throws Exception ;
}