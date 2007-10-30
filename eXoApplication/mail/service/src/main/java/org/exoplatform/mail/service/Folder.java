/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.service;

/**
 * Created by The eXo Platform SARL
 * Author : Nam Phung
 *          phunghainam@gmail.com
 * Jun 23, 2007  
 * 
 * Mail Folder is an object that keep track  of  the information of a set of messages
 * 
 */
public class Folder {
  private String id ;
  private String name ;
  private String label ;
  private long unreadMessage = 0 ;
  private long allMessages = 0;
  private boolean isPersonalFolder = true ;

  /**
   * The id folder should have the form AccountId/DefaultFolder/folderName or AccountId/UserFolder/folderName
   * @return the id of the folder
   */
  public String getId()  { return id ; }
  public void   setId(String s) { id = s ; }
  
  /**
   * The name of the folder like Inbox, Sent, MyFolder
   * @return The name of the folder
   */
  public String getName() { return name ; }
  public void   setName(String s) { name = s ; }
  
  /**
   * The display label of the folder like Inbox, Sent, MyFolder
   * @return The label of the folder
   */
  public String getLabel() { return label ; }
  public void   setLabel(String s) { label = s ; }
  
  /**
   * @return  The number of the unread messages
   */
  public long  getNumberOfUnreadMessage() { return unreadMessage ; }
  public void setNumberOfUnreadMessage(long number) { unreadMessage = number ; }
  
  /**
   * The total of the folder
   */
  public long getTotalMessage() { return allMessages; }
  public void setTotalMessage(long number) { this.allMessages = number; }
  
  /**
   * @return Calculate and return the account id  of the folder base on the id of  the folder
   */
  public String getAccountId() { return null ; }
  
  public boolean  isPersonalFolder() { return isPersonalFolder ; }
  public void setPersonalFolder(boolean isPersonal) { isPersonalFolder = isPersonal ; }
}
