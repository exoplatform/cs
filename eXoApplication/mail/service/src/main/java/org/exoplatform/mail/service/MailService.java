/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.service;

import java.util.List;


/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan.nguyen@exoplatform.com
 * Jun 23, 2007  
 */
public interface MailService {
  
  public List<AccountData> getAccountDatas() throws Exception ;
  
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
  public List<Account> getAccounts(String username) throws Exception ;
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
  public Account getAccountById(String username, String id) throws Exception ;
  
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
  public void createAccount(String username, Account account) throws Exception ;
  /**
   * This method should:
   * 1. This method check the madatory field and save the updated account into the database
   * 2. This method should update the cache if the data is updated successfully.
   * @param username
   * @param account
   * @throws Exception
   */
  public void updateAccount(String username, Account account) throws Exception ;
  /**
   * This method should:
   * 1.  Remove the account from the database
   * 2.  Update the cache
   * @param username
   * @param account
   * @throws Exception
   */
  public void removeAccount(String username, Account account) throws Exception ;
  /**
   * This method should: 
   * @param username
   * @param accountId
   * @param isPersonal
   * @return List folder
   * @throws Exception
   */
  public List<Folder> getFolders(String username, String accountId, boolean isPersonal) throws Exception ;
  /**
   * This method should: 
   * @param username
   * @param accountId
   * @param folderId
   * @return Folder
   * @throws Exception
   */
  public Folder getFolder(String username, String accountId, String folderId) throws Exception ;  
  /**
   * This method should:
   * 1. Get account
   * 2. Save folder under account 
   * @param username
   * @param accountId
   * @param folder
   * @throws Exception
   */
  public void saveUserFolder(String username, String accountId, Folder folder) throws Exception ;  
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
  public void removeUserFolder(String username, Account account, Folder folder) throws Exception ;
  
  public List<Message> getMessageByTag(String username, String accountId, String tagId) throws Exception ;
  
  public MessagePageList getMessagePagelistByTag(String username, String accountId, String tagId) throws Exception ;
  
  public MessagePageList getMessagePagelistByTag(String username, String accountId, String tagId, String viewQuery) throws Exception ;
  
  public MessagePageList getMessagePagelistByTag(String username, String accountId, String tagId, String viewQuery, String orderBy, boolean isAscending) throws Exception ;
  
  public MessagePageList getMessageByFolder(String username, String accountId, String folderName) throws Exception ;
  
  public MessagePageList getMessageByFolder(String username, String accountId, String folderName, String viewQuery) throws Exception ;
  
  public MessagePageList getMessageByFolder(String username, String accountId, String folderName, String viewQuery, String orderBy, boolean isAscending) throws Exception;
  
  public List<Tag> getTags(String username, String accountId) throws Exception ;
  
  public Tag getTag(String username, String accountId, String tagId) throws Exception;
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
   public void addTag(String username, String accountId, List<String> messagesId, List<Tag> tag) throws Exception ;
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
   public void removeMessageTag(String username, String accountId, List<String> messageId, List<String> tags) throws Exception ;
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
  public void removeTag(String username, String accountId, String tag) throws Exception ;
  /**
   * This method should:
   * 1. Update a tag.
   * @param username
   * @param accountId
   * @param tag
   * @throws Exception
   */
  public void updateTag(String username, String accountId, Tag tag) throws Exception ;
  
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
  public Message getMessageById(String username, String nodeName, String accountId) throws Exception ;
  /**
   * This method should:
   * 1. Find all the message according the parameter that is specified in the filter object
   * 
   * @param username
   * @param filter
   * @return
   * @throws Exception
   */
  public MessagePageList getMessages(String username, MessageFilter filter) throws Exception ;
  
  public void saveMessage(String username, String accountId, Message message, boolean isNew) throws Exception;
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
  public void removeMessage(String username, String accountId, String messageId) throws Exception ;
  /**
   * This method should:
   * 1. Remove all the messages 
   * 2. Update or invalidate the cache 
   * 
   * @param username
   * @param messageId
   * @throws Exception
   */
  public void removeMessage(String username, String accountId, List<String> messageIds) throws Exception ;

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
  public void sendMessage(String username, Message message) throws Exception ;
  /**
   * This method should check  for the new message in the mail server, download and save them in the 
   * Inbox folder
   * @param username
   * @param account
   * @return the number of the new message
   * @throws Exception
   */
  public List<Message>  checkNewMessage(String username, String accountId) throws Exception ;  
  /**
   * This method get mail settings
   * @param username
   * @return settings of mail
   * @throws Exception
   */
  public MailSetting  getMailSetting(String username) throws Exception ;  
  /**
   * This method to update mail setting
   * @param username
   * @param newSetting
   * @throws Exception
   */
  public void saveMailSetting(String username, MailSetting newSetting) throws Exception;
}