/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.exoplatform.services.jcr.ext.common.SessionProvider;


/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan.nguyen@exoplatform.com
 * Jun 23, 2007  
 */
public interface MailService {
  
  //public List<AccountData> getAccountDatas() throws Exception ;
  
  /**
   * This method should: 
   * 1. The service should load  the accounts belong to the user and cache in the service.
   * 2. The service should return the  list of the account in the cache.  If the user  hasn't configured
   *    an account, an empty list will be cached and return. 
   * 
   * @param username
   * @return
   * @throws Exception
   */
  public List<Account> getAccounts(SessionProvider sProvider, String username) throws Exception ;
  /**
   * This method should:
   * 1. Check if the list of the accounts is cached. If not call the method List<Account> getAccounts(String username)
   *    to load all the account belong to the user and cached
   * 2. Find the account in the list of the account and return.
   * 3. return null if no account is found.
   * @param username
   * @param id
   * @return
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
   * This method should:
   * 1.  Remove the account from the database
   * 2.  Update the cache
   * @param username
   * @param account
   * @throws Exception
   */
  public void removeAccount(SessionProvider sProvider, String username, Account account) throws Exception ;
  /**
   * This method should: 
   * @param username
   * @param accountId
   * @return List folder
   * @throws Exception
   */
  public List<Folder> getFolders(SessionProvider sProvider, String username, String accountId) throws Exception ;
  /**
   * This method should: 
   * @param username
   * @param accountId
   * @param isPersonal
   * @return List folder
   * @throws Exception
   */
  public List<Folder> getFolders(SessionProvider sProvider, String username, String accountId, boolean isPersonal) throws Exception ;
  /**
   * This method should: 
   * @param username
   * @param accountId
   * @param folderId
   * @return Folder
   * @throws Exception
   */
  public Folder getFolder(SessionProvider sProvider, String username, String accountId, String folderId) throws Exception ;  
  /**
   * This method should:
   * 1. Get account
   * 2. Save folder under account 
   * @param username
   * @param accountId
   * @param folder
   * @throws Exception
   */
  public void saveFolder(SessionProvider sProvider, String username, String accountId, Folder folder) throws Exception ;  
  /**
   * This method should:
   * 1. Move all the message in the  folder to the  default inbox folder
   * 2. Remove the folder from the account 
   * 3. Save the account into the database
   * 4. Update or invalidate the cache if needed 
   * @param username
   * @param account
   * @param folder
   * @throws Exception
   */
  public void removeUserFolder(SessionProvider sProvider, String username, Account account, Folder folder) throws Exception ;
  
  /**
   * This method should: 
   * @param username
   * @param accountId
   * @return List all filter
   * @throws Exception
   */
  public List<MessageFilter> getFilters(SessionProvider sProvider, String username, String accountId) throws Exception ;
  
  /**
   * This method should: 
   * @param username
   * @param accountId
   * @param filterId
   * @return MessageFilter
   * @throws Exception
   */
  public MessageFilter getFilterById(SessionProvider sProvider, String username, String accountId, String filterId) throws Exception ;
  
  /**
   * This method should: 
   * @param username
   * @param accountId
   * @param filter
   * @return save filter to database
   * @throws Exception
   */
  public void saveFilter(SessionProvider sProvider, String username, String accountId, MessageFilter filter) throws Exception ;
  
  public void removeFilter(SessionProvider sProvider, String username, String accountId, String filterId) throws Exception ;
  
  public List<Message> getMessageByTag(SessionProvider sProvider, String username, String accountId, String tagId) throws Exception ;
  
  public MessagePageList getMessagePagelistByTag(SessionProvider sProvider, String username, String accountId, String tagId) throws Exception ;
 
  public MessagePageList getMessagePageListByFolder(SessionProvider sProvider, String username, String accountId, String folderId) throws Exception ;
  
  public List<Tag> getTags(SessionProvider sProvider, String username, String accountId) throws Exception ;
  
  public Tag getTag(SessionProvider sProvider, String username, String accountId, String tagId) throws Exception;
  /** This method should:
    * 1. Check the tag name to see if  the tag name is configured in the account
    * 2. Check to see if the tag is already set in the message
    * 3. Add the tag to the message and save the message.
    * 4. Invalidate or update the cache.
    * @param username
    * @param message
    * @param tag
    * @throws Exception
    */
   public void addTag(SessionProvider sProvider, String username, String accountId, List<String> messagesId, List<Tag> tag) throws Exception ;
   /**
    * This method should: 
    * 1. Remove the tag from the message
    * 2. Save the message into the database
    * 3. Update or invalidate the cache
    * @param username
    * @param message
    * @param tags
    * @throws Exception
    */
   public void removeMessageTag(SessionProvider sProvider, String username, String accountId, List<String> messageId, List<String> tags) throws Exception ;
  /**
   * This method should:
   * 1. Find all the message that has the tag , remove the tag from the message and save
   * 2. Remove the tag from the account and save 
   * 3. Update or invalidate the cache if needed
   * @param username
   * @param account
   * @param tag
   * @throws Exception
   */
  public void removeTag(SessionProvider sProvider, String username, String accountId, String tag) throws Exception ;
  /**
   * This method should:
   * 1. Update a tag.
   * @param username
   * @param accountId
   * @param tag
   * @throws Exception
   */
  public void updateTag(SessionProvider sProvider, String username, String accountId, Tag tag) throws Exception ;
  
  /**
   * This method should:
   * 1. Load the message from the database if it existed and return.
   * 2. This method should implement a cache to cache the message by  the message id and the username
   *  
   * @param username
   * @param id
   * @return
   * @throws Exception
   */
  public Message getMessageById(SessionProvider sProvider, String username, String accountId, String nodeName) throws Exception ;
  /**
   * This method should:
   * 1. Find all the message according the parameter that is specified in the filter object
   * 
   * @param username
   * @param filter
   * @return
   * @throws Exception
   */
  public MessagePageList getMessages(SessionProvider sProvider, String username, MessageFilter filter) throws Exception ;
  
  public void saveMessage(SessionProvider sProvider, String username, String accountId, Message message, boolean isNew) throws Exception;
  /**
   * This method should:
   * 1. Update the message in the database if it is existed
   * 2. Update or invalidate the cache if the message is cached
   * @param username
   * @param accountId
   * @param messageName
   * @param message
   * @throws Exception
   */
  //public void updateMessage(String username, String accountId, String messageName, Message message) throws Exception;
  /**
   * This method should:
   * 1. Remove the message from the database if it is existed
   * 2. Update or invalidate the cache if the message is cached
   * @param username
   * @param messageId
   * @throws Exception
   */
  public void removeMessage(SessionProvider sProvider, String username, String accountId, String messageId) throws Exception ;
  /**
   * This method should:
   * 1. Remove all the messages 
   * 2. Update or invalidate the cache 
   * 
   * @param username
   * @param messageId
   * @throws Exception
   */
  public void removeMessage(SessionProvider sProvider, String username, String accountId, List<String> messageIds) throws Exception ;
  
  public void moveMessages(SessionProvider sProvider, String username,String accountId, String msgId, String currentFolderId, String destFolderId) throws Exception ;

  public void sendMessages(List<Message> msgList, ServerConfiguration serverConfig) throws Exception ;
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
   * @throws Exception
   */
  public void sendMessage(SessionProvider sProvider, String username, Message message) throws Exception ;
  /**
   * This method should check  for the new message in the mail server, download and save them in the 
   * Inbox folder
   * @param username
   * @param account
   * @return the number of the new message
   * @throws Exception
   */
  public List<Message>  checkNewMessage(SessionProvider sProvider, String username, String accountId) throws Exception ;  
  /**
   * This method get mail settings
   * @param username
   * @return settings of mail
   * @throws Exception
   */
  public MailSetting  getMailSetting(SessionProvider sProvider, String username) throws Exception ;  
  /**
   * This method to update mail setting
   * @param username
   * @param newSetting
   * @throws Exception
   */
  public void saveMailSetting(SessionProvider sProvider, String username, MailSetting newSetting) throws Exception;
  public String getCurrentAccount(SessionProvider sProvider, String username) throws Exception;
  public void updateCurrentAccount(SessionProvider sProvider, String username, String accountId) throws Exception;
  
  public void importMessage(SessionProvider sProvider, String username, String accountId, String folderId, InputStream inputStream, String type) throws Exception;
  public OutputStream exportMessage(SessionProvider sProvider, String username,String accountId, String messageId) throws Exception;
  
  public void execFilters(SessionProvider sProvider, String username, String accountId) throws Exception ;
  
  public SpamFilter getSpamFilter(SessionProvider sProvider, String username, String accountId) throws Exception ;
  
  public void saveSpamFilter(SessionProvider sProvider, String username, String accountId, SpamFilter spamFilter) throws Exception ;
  
  public void toggleMessageProperty(SessionProvider sProvider, String username, String accountId, List<String> msgList, String property) throws Exception ;
  
  public String getFolderHomePath(SessionProvider sProvider, String username, String accountId) throws Exception ;
  
  public void saveFolder(SessionProvider sProvider, String username, String accountId, String parentPath, Folder folder) throws Exception ;
  
  public List<Folder> getSubFolders(SessionProvider sProvider, String username, String accountId, String parentPath) throws Exception ;
}