/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.service;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan.nguyen@exoplatform.com
 * Jun 23, 2007  
 * 
 * Mail Folder is an object that keep track  of  the information of a set of messages
 * 
 */
public class Folder {
  private String id_ ;
  private String name_ ;
  private String label_ ;
  private int unreadMessage_ ;
  
  /**
   * The id folder should have the form AccountId/DefaultFolder/folderName or AccountId/UserFolder/folderName
   * @return the id of the folder
   */
  public String getId()  { return id_ ; }
  public void   setId(String s) { id_ = s ; }
  
  /**
   * The name of the folder like Inbox, Sent, MyFolder
   * @return The name of the folder
   */
  public String getName() { return name_ ; }
  public void   setName(String s) { name_ = s ; }
  
  /**
   * The display label of the folder like Inbox, Sent, MyFolder
   * @return The label of the folder
   */
  public String getLabel() { return label_ ; }
  public void   setLabel(String s) { label_ = s ; }
  
  /**
   * @return  The number of the unread messages
   */
  public int  getNumberOfUnreadMessage() { return unreadMessage_ ; }
  public void setNumberOfUnreadMessage(int number) { unreadMessage_ = number ; }
  
  /**
   * @return Calculate and return the account id  of the folder base on the id of  the folder
   */
  public String getAccountId() { return null ; }
}
