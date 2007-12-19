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
package org.exoplatform.mail.service.impl;

import java.util.List;

import javax.jcr.Node;

import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.MailSetting;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.MessageFilter;
import org.exoplatform.mail.service.MessagePageList;
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

  public MessagePageList getMessages(String username, MessageFilter filter) throws Exception ;

  public Message getMessage(Node messageNode) throws Exception ;
  
  public MailSetting  getMailSetting(String username) throws Exception ;
  public void saveMailSetting(String username, MailSetting newSetting) throws Exception;

  public void removeMessage(String username, String accountId, String messageId) throws Exception ;

  public void removeMessage(String username, String accountId, List<String> messageIds) throws Exception ;
  
  public void moveMessages(String username, String accountId, String msgId, String currentFolderId, String destFolderId) throws Exception ;

  public void saveAccount(String username, Account account, boolean isNew) throws Exception ;

  public void saveMessage(String username, String accountId, Message message, boolean isNew) throws Exception ;

  public Folder getFolder(String username, String accountId, String folderId) throws Exception ;
  public List<Folder> getFolders(String username, String accountId) throws Exception ;
  public void saveFolder(String username, String accountId, Folder folder) throws Exception ;

  public void removeUserFolder(String username, Folder folder) throws Exception ;
  public void removeUserFolder(String username, Account account, Folder folder) throws Exception ;
  
  public List<MessageFilter> getFilters(String username, String accountId) throws Exception ;
  
  public MessageFilter getFilterById(String username, String accountId, String filterId) throws Exception ;
  
  public void saveFilter(String username, String accountId, MessageFilter filter) throws Exception ;
  
  public void removeFilter(String username, String accountId, String filterId) throws Exception ;
  
  public List<Tag> getTags(String username, String accountId) throws Exception ;
  public Tag getTag(String username, String accountId, String tagId) throws Exception ;
  public void addTag(String username, String accountId, List<String> messagesId, List<Tag> listTag) throws Exception ;
  public void removeMessageTag(String username, String accountId, List<String> messageIds, List<String> tagIds) throws Exception ;
  public void removeTag(String username, String accountId, String tagId) throws Exception ;
  public void updateTag(String username, String accountId, Tag tag) throws Exception ;
  
  public void execFilters(String username, String accountId) throws Exception ;
  
}