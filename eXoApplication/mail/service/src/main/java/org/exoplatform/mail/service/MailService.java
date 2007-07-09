/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.service;

import java.util.List;

import javax.jcr.Node;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan.nguyen@exoplatform.com
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
   * @param folderName
   * @return Folder
   * @throws Exception
   */
  public Folder getFolder(String username, String accountId, String folderName) throws Exception ;  
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
  public void removeTag(String username, Account account, String tag) throws Exception ;
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
  public Message getMessageById(String username, String id) throws Exception ;
  /**
   * This method should:
   * 1. Remove the message from the database if it is existed
   * 2. Update or invalidate the cache if the message is cached
   * @param username
   * @param messageId
   * @throws Exception
   */
  public void removeMessage(String username, String messageId) throws Exception ;
  /**
   * This method should:
   * 1. Remove all the messages 
   * 2. Update or invalidate the cache 
   * 
   * @param username
   * @param messageId
   * @throws Exception
   */
  public void removeMessage(String username, String[] messageId) throws Exception ;
  /**
   * This method should:
   * 1. Check the tag name to see if  the tag name is configured in the account
   * 2. Check to see if the tag is already set in the message
   * 3. Add the tag to the message and save the message.
   * 4. Invalidate or update the cache.
   * @param username
   * @param message
   * @param tag
   * @throws Exception
   */
  public void addTag(String username, Message message, String tag) throws Exception ;
  /**
   * This method should: 
   * 1. Remove the tag from the message
   * 2. Save the message into the database
   * 3. Update or invalidate the cache
   * @param username
   * @param message
   * @param tag
   * @throws Exception
   */
  public void removeTag(String username, Message message, String tag) throws Exception ;
  /**
   * This method should:
   * 1. Find all the message according the parameter that is specified in the filter object
   * 
   * @param username
   * @param filter
   * @return
   * @throws Exception
   */
  public List<MessageHeader> getMessageByFilter(String username, MessageFilter filter) throws Exception ;
  /**
   * This method should send out the message
   * @param message
   * @throws Exception
   */
  public void sendMessage(Message message) throws Exception ;
  /**
   * This method should check  for the new message in the mail server, download and save them in the 
   * Inbox folder
   * @param username
   * @param account
   * @return the number of the new message
   * @throws Exception
   */
  public int  checkNewMessage(String username, Account account) throws Exception ;  
  
  /**
   * This method should: 
   * 1. The service should load  the contacts belong to the user and cache in the service.
   * 2. The service should return the  list of the contact in the cache.  If the user  hasn't configured
   *    an contact, an empty list will be cached and return.
   * @param username
   * @return contact list
   * @throws Exception
   */
  public List<Contact> getContacts(String username) throws Exception ;
  
  /**
   * This method should: 
   * 1. Check exists of contact display name.
   * 2. Add and save contact to current user. 
   * 3. invalidate or refresh cache list
   * @param username
   * @param contact
   * @return
   * @throws Exception
   */
  public Contact getContactById(String username, String id) throws Exception ;
  
  /**
   * This method should: 
   * 1. Get contact node by identify of current user.
   * 2. Map contact node to object of Contact class
   * @param username
   * @param contact id
   * @return Contact
   * @throws Exception
   */
  public void addContact(String username, Contact contact, boolean isAddNew) throws Exception ;
  
  public List <Contact> getContactByGroup(String username, String groupName) throws Exception ;
  
  public void removeContact(String username, String contactName) throws Exception ;
  
  public List<Group> getGroups(String username) throws Exception ;
  
  public void addGroup(String username, Group group, boolean isAddNew) throws Exception ;
  
  public void removeGroup(String username, String groupName) throws Exception ;
  
  public Node getMailHomeNode(String username) throws Exception ;
  
  
  
}