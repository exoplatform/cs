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
package org.exoplatform.mail.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.services.jcr.ext.common.SessionProvider;


/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen <tuan.nguyen@exoplatform.com>
 *          Phung Nam <phunghainam@gmail.com>
 * Jun 23, 2007  
 */
public interface MailService {
  
  /**
   * This method should: 
   * 1. The service should load  the accounts belong to the user and cache in the service.
   * 2. The service should return the  list of the account in the cache.  If the user  hasn't configured
   *    an account, an empty list will be cached and return. 
   * 
   * @param username
   * @return List<Account>
   * @throws Exception
   */
  public List<Account> getAccounts(SessionProvider sProvider, String username) throws Exception ;
  
  public String getMailHierarchyNode(SessionProvider sProvider) throws Exception ;
  
  /**
   * This method should:
   * 1. Check if the list of the accounts is cached. If not call the method List<Account> getAccounts(String username)
   *    to load all the account belong to the user and cached
   * 2. Find the account in the list of the account and return.
   * 3. return null if no account is found.
   * @param username
   * @param  id
   * @return Account
   * @throws Exception
   */
  public Account getAccountById(SessionProvider sProvider, String username, String id) throws Exception ;
  
  /**
   * Use save for create and update 
   * 
   * This method should:
   * 1. Check all the madatory  field of the account and save the account into the database. The method 
   *    should throw exception , if any mandatory field is missing.
   * 2. The method should update or invalidate the list of account of the user in the cache
   * 
   * @param username
   * @param account
   * @throws Exception
   */
  public void createAccount(SessionProvider sProvider, String username, Account account) throws Exception ;
  
  /**
   * This method should:
   * 1. This method check the madatory field and save the updated account into the database
   * 2. This method should update the cache if the data is updated successfully.
   * @param username
   * @param account
   * @throws Exception
   */
  public void updateAccount(SessionProvider sProvider, String username, Account account) throws Exception ;
  
  /**
   * Remove the account from the database
   * Update the cache
   * @param username
   * @param account
   * @throws Exception
   */
  public void removeAccount(SessionProvider sProvider, String username, String accountId) throws Exception ;
  
  /**
   * @param username
   * @param accountId
   * @return List folder
   * @throws Exception
   */
  public List<Folder> getFolders(SessionProvider sProvider, String username, String accountId) throws Exception ;
  
  /**
   * Get folders depend on they are personal folders or default folders
   * @param username
   * @param accountId
   * @param isPersonal
   * @return List of folders
   * @throws Exception
   */
  public List<Folder> getFolders(SessionProvider sProvider, String username, String accountId, boolean isPersonal) throws Exception ;
  
  /**
   * This method uses to get a folder object by folderId
   * @param username
   * @param accountId
   * @param folderId
   * @return exo Folder object
   * @throws Exception
   */
  public Folder getFolder(SessionProvider sProvider, String username, String accountId, String folderId) throws Exception ;
  
  /**
   * This method get id of parent folder by id of child folder  
   * @param username
   * @param accountId
   * @param folderName
   * @return string Id of parent folder 
   * @throws Exception
   */
  public String getFolderParentId(SessionProvider sProvider, String username, String accountId, String folderId) throws Exception ;
  
  /**
   * Check whether the folder contains one child folder with identify id string   
   * @param username
   * @param accountId
   * @param parentId
   * @param folderName
   * @return boolean
   * @throws Exception
   */
  public boolean isExistFolder(SessionProvider sProvider, String username, String accountId, String parentId, String folderName) throws Exception ;
  
  /**
   * Save folder under special account 
   * @param username
   * @param accountId
   * @param folder
   * @throws Exception
   */
  public void saveFolder(SessionProvider sProvider, String username, String accountId, Folder folder) throws Exception ;
  
  /**
   * This method remove all the messages in the folder then remove the folder of the account
   * Save the account into the database
   * @param username
   * @param account
   * @param folderId 
   * @throws Exception
   */
  public void removeUserFolder(SessionProvider sProvider, String username, String accountId, String folderId) throws Exception ;
  
  /**
   * Get all MessageFilter in special account
   * @param username
   * @param accountId
   * @return list of all filters
   * @throws Exception
   */
  public List<MessageFilter> getFilters(SessionProvider sProvider, String username, String accountId) throws Exception ;
  
  /**
   * Get MessageFilter by id of filter 
   * @param username
   * @param accountId
   * @param filterId
   * @return MessageFilter
   * @throws Exception
   */
  public MessageFilter getFilterById(SessionProvider sProvider, String username, String accountId, String filterId) throws Exception ;
  
  /**
   * Save filter to JCR 
   * @param username
   * @param accountId
   * @param filter
   * @param applyAll TODO
   * @return save filter to database
   * @throws Exception
   */
  public void saveFilter(SessionProvider sProvider, String username, String accountId, MessageFilter filter, boolean applyAll) throws Exception ;
  
  /**
   * Remove filter by filter id  
   * @param username
   * @param accountId
   * @param filterId 
   * @throws Exception
   */
  public void removeFilter(SessionProvider sProvider, String username, String accountId, String filterId) throws Exception ;
  
  /**
   * Get all messages of the given tag id  
   * @param username
   * @param accountId
   * @param tagId 
   * @throws Exception
   */
  public List<Message> getMessageByTag(SessionProvider sProvider, String username, String accountId, String tagId) throws Exception ;
  
  /**
   * Get MessagePageList by tag id. This method get all message and put it to MessagePageList object 
   * this object will use getPage() method to return a list of messages.
   * @param username
   * @param accountId
   * @param tagId 
   * @return MessagePageList that hold message by page
   * @throws Exception
   */
  public MessagePageList getMessagePagelistByTag(SessionProvider sProvider, String username, String accountId, String tagId) throws Exception ;
 
  /**
   * Get MessagePageList by folder id. This method get all message and put it to MessagePageList object 
   * this object will use getPage() method to return a list of messages.
   * @param username
   * @param accountId
   * @param folderId 
   * @return MessagePageList
   * @throws Exception
   */
  public MessagePageList getMessagePageListByFolder(SessionProvider sProvider, String username, String accountId, String folderId) throws Exception ;
  
  /**
   * Get all tags of account
   * @param username
   * @param accountId 
   * @return List of Tag
   * @throws Exception
   */
  public List<Tag> getTags(SessionProvider sProvider, String username, String accountId) throws Exception ;
  
  /**
   * Get tag by tagId
   * @param username
   * @param accountId 
   * @return List of Tag
   * @throws Exception
   */
  public Tag getTag(SessionProvider sProvider, String username, String accountId, String tagId) throws Exception;
  
  /**
   * Check the tag name to see if  the tag name is configured in the account
   * If not create a new tag
   * @param username
   * @param accountId
   * @param tag
   * @throws Exception
   */
  public void addTag(SessionProvider sProvider, String username, String accountId, Tag tag) throws Exception ;
  
  /** 
    * Check the tag name to see if  the tag name is configured in the account
    * Check to see if the tag is already set in the message
    * Add the tag to the message and save the message.
    * Invalidate or update the cache.
    * @param username
    * @param message
    * @param tag
    * @throws Exception
    */
   public void addTag(SessionProvider sProvider, String username, String accountId, List<Message> messages, List<Tag> tag) throws Exception ;
   
   /**
    * Remove the tag from the message
    * Save the message into the database
    * Update or invalidate the cache
    * @param username
    * @param message
    * @param tags
    * @throws Exception
    */
   public void removeTagsInMessages(SessionProvider sProvider, String username, String accountId, List<Message> messages, List<String> tags) throws Exception ;
  
   /**
   * Find all the message that has the tag , remove the tag from the message and save
   * Remove the tag from the account and save 
   * Update or invalidate the cache if needed
   * @param username
   * @param account
   * @param tag
   * @throws Exception
   */
  public void removeTag(SessionProvider sProvider, String username, String accountId, String tag) throws Exception ;
  
  /**
   * Update a tag.
   * @param username
   * @param accountId
   * @param tag
   * @throws Exception
   */
  public void updateTag(SessionProvider sProvider, String username, String accountId, Tag tag) throws Exception ;
  
  /**
   * Load the message from the database if it existed and return.
   * This method should implement a cache to cache the message by  the message id and the username
   * @param username
   * @param nodeName
   * @return message
   * @throws Exception
   */
  public Message getMessageById(SessionProvider sProvider, String username, String accountId, String nodeName) throws Exception ;
  
  /**
   * Find all the message according the parameter that is specified in the filter object
   * @param username
   * @param filter
   * @return MessagePageList
   * @throws Exception
   */
  public MessagePageList getMessagePageList(SessionProvider sProvider, String username, MessageFilter filter) throws Exception ;
  
  /**
   * Get all messages by tag id.
   * @param username
   * @param accountId
   * @param tagId
   * @return List of messages
   * @throws Exception
   */
  public List<Message> getMessagesByTag(SessionProvider sProvider, String username, String accountId, String tagId) throws Exception ;
  
  /**
   * Get all messages by given folder id.
   * @param username
   * @param accountId
   * @param folderId
   * @return List of messages
   * @throws Exception
   */
  public List<Message> getMessagesByFolder(SessionProvider sProvider, String username, String accountId, String folderId) throws Exception ;
  
  /**
   * Get messages by given filter. This method get all message which are statisfied all conditions of filter 
   * @param username
   * @param accountId
   * @param folderId
   * @return List of messages
   * @throws Exception
   */
  public List<Message> getMessages(SessionProvider sProvider, String username, MessageFilter filter) throws Exception ;
  
  /**
   * Save message to Account/Messages/Year/Month/Day tree node. If message is new then Day node will create a new node 
   * if not it will update the exist message.
   * @param username
   * @param accountId
   * @param targetMsgPath this param is path of node Account/Messages/Year/Month/Day
   * @throws Exception
   */
  public void saveMessage(SessionProvider sProvider, String username, String accountId, String targetMsgPath, Message message, boolean isNew) throws Exception ;
  
  public void saveMessage(SessionProvider sProvider, String username, String accountId, Message message, boolean isNew) throws Exception;
  
  /**
   * This method should:
   * 1. Remove the message from the database if it is existed
   * 2. Update or invalidate the cache if the message is cached
   * @param username
   * @param message
   * @throws Exception
   */
  public void removeMessage(SessionProvider sProvider, String username, String accountId, Message message) throws Exception ;
  
  /**
   * This method should:
   * 1. Remove all the messages 
   * 2. Update or invalidate the cache 
   * 
   * @param username
   * @param messageId
   * @throws Exception
   */
  public void removeMessages(SessionProvider sProvider, String username, String accountId, List<Message> messages, boolean moveReference) throws Exception ;
  
  /**
   * Move a list of message from the current folder to the given folder
   * @param username
   * @param accountId
   * @param msg
   * @param currentFolderId
   * @param destFolderId
   * @throws Exception
   */
  public void moveMessages(SessionProvider sProvider, String username, String accountId, List<Message> msgList, String currentFolderId, String destFolderId) throws Exception ;
  
  public void moveMessages(SessionProvider sProvider, String username, String accountId, List<Message> msgList, String currentFolderId, String destFolderId, boolean updateReference) throws Exception ;
  
  /**
   * Move a message from the current folder to the given folder
   * @param username
   * @param accountId
   * @param msg
   * @param currentFolderId
   * @param destFolderId
   * @throws Exception
   */
  public void moveMessage(SessionProvider sProvider, String username,String accountId, Message msg, String currentFolderId, String destFolderId) throws Exception ;

  public void moveMessage(SessionProvider sProvider, String username,String accountId, Message msg, String currentFolderId, String destFolderId, boolean updateReference) throws Exception ;
  /**
   * Use smtp to send message with given server configuration
   * @param msgList
   * @param serverConfig
   * @throws Exception
   */
  public void sendMessages(List<Message> msgList, ServerConfiguration serverConfig) throws Exception ;
  
  public Message sendMessage(SessionProvider sProvider, String username, Account acc, Message message) throws Exception ;
  
  public Message sendMessage(SessionProvider sProvider, String username, String accId, Message message) throws Exception ;
  
  /**
   * This method should send out the message
   * @param message
   * @throws Exception
   */
  public void sendMessage(Message message) throws Exception ;
  
  /**
   * This method should send out the message
   * @param username
   * @param message
   * @return Message
   * @throws Exception
   */
  public Message sendMessage(SessionProvider sProvider, String username, Message message) throws Exception ;
 
  /**
   * This method should check  for the new message in the mail server, download and save them in the 
   * Inbox folder
   * @param username
   * @param account
   * @return List<Message>
   * @throws Exception
   */
  public List<Message>  checkNewMessage(SessionProvider sProvider, String username, String accountId) throws Exception ;
  
  public void removeCheckingInfo(String username, String accountId)throws Exception ;
  
  public CheckingInfo getCheckingInfo(String username, String accountId) throws Exception ;
  
  /**
   * Getting new mail from server and store to JCR
   * @param username
   * @param accountId
   * @throws Exception
   */
  public void checkMail(String username, String accountId) throws Exception ;
  
  /**
   * Requests to stop mail checking
   * @param username userid
   * @param accountId mail account
   * @throws Exception
   */
  public void stopCheckMail(String username, String accountId) ;
  
  /**
   * This method get mail settings
   * @param username
   * @return MailSetting
   * @throws Exception
   */
  public MailSetting getMailSetting(SessionProvider sProvider, String username) throws Exception ;  
  
  /**
   * This method to update mail setting
   * @param username
   * @param newSetting
   * @throws Exception
   */
  public void saveMailSetting(SessionProvider sProvider, String username, MailSetting newSetting) throws Exception;
  
  /**
   * Import message to eXo Mail. This method get InputStream and parse it to eXo Message object in special type 
   * @param username
   * @param newSetting
   * @throws Exception
   */
  public boolean importMessage(SessionProvider sProvider, String username, String accountId, String folderId, InputStream inputStream, String type) throws Exception;
  
  /**
   * Export message from eXo Mail. The exported file can reimport to eXo Mail or import to other mail system
   * @param username
   * @param accountId
   * @param folderId
   * @param inputStream
   * @param type
   * @return OutputStream
   * @throws Exception
   */
  public OutputStream exportMessage(SessionProvider sProvider, String username,String accountId, Message message) throws Exception;
  
  /**
   * @param username
   * @param accountId
   * @throws Exception
   */
  public SpamFilter getSpamFilter(SessionProvider sProvider, String username, String accountId) throws Exception ;
  
  /**
   * Save the given spam filter to JCR
   * @param username
   * @param accountId
   * @param spamFilter
   * @throws Exception
   */
  public void saveSpamFilter(SessionProvider sProvider, String username, String accountId, SpamFilter spamFilter) throws Exception ;
  
  /**
   * Toggle the property of message. For example read/unread, star/unstar
   * @param username
   * @param accountId
   * @param msgList
   * @param property
   * @throws Exception
   */
  public void toggleMessageProperty(SessionProvider sProvider, String username, String accountId, List<Message> msgList, String property) throws Exception ;
  
  /**
   * Get path of node, that contains all folders
   * @param username
   * @param accountId
   * @return String
   * @throws Exception
   */
  public String getFolderHomePath(SessionProvider sProvider, String username, String accountId) throws Exception ;
  
  /**
   * Save folder to JCR as child of given folder
   * @param username
   * @param accountId
   * @param parentId
   * @param folder
   * @throws Exception
   */
  public void saveFolder(SessionProvider sProvider, String username, String accountId, String parentId, Folder folder) throws Exception ;
  
  /**
   * Get all sub folders of the given folder path
   * @param username
   * @param accountId
   * @param folderPath
   * @return List<Folder>
   * @throws Exception
   */
  public List<Folder> getSubFolders(SessionProvider sProvider, String username, String accountId, String parentPath) throws Exception ;
  
  /**
   * Get all referenced messages of give message path
   * @param username
   * @param accountId
   * @param msgPath
   * @return List<Message>
   * @throws Exception
   */
  public List<Message> getReferencedMessages(SessionProvider sProvider, String username, String accountId, String msgPath) throws Exception ;
  
  /**
   * Get default account
   * @param username
   * @param accountId
   * @return Account
   * @throws Exception
   */
  public Account getDefaultAccount(SessionProvider sProvider, String username) throws Exception ;
  
  /**
   * If the message object didn't load attachment yet, then this method will load it's attachment from JCR and return it's message
   * @param username
   * @param accountId
   * @param msg
   * @return Message
   * @throws Exception
   */
  public Message loadAttachments(SessionProvider sProvider, String username, String accountId, Message msg) throws Exception ;
  
  public void addListenerPlugin(ComponentPlugin listener) throws Exception ;
}