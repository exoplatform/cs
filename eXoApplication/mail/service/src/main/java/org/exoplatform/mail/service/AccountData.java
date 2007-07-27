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
 * 
 */
public class AccountData {
  private Account account ;
  private List<Folder> defaultFolders ;
  private List<Folder> userFolders ;  
  private List<Tag> tags ;
  
  
  public Account getAccount() { return account ; }
  public void setAccount( Account acc) { this.account = acc ; }
  
  /**
   * @return Return a list of the default folder: Inbox, Sent, Draft, Spam and Trash
   */
  public List<Folder> getDefaultFolder() { return defaultFolders ; }
  public void setDefaultFolder(List<Folder> folders) { defaultFolders = folders ; }
  
  /**
   * @return Return a list of the folder that is created by the user
   */
  public List<Folder> getUserFolder() { return userFolders ; }
  public void setUserFolder(List<Folder> folders) { userFolders = folders ; }
  
  public List<Tag> getTags() { return tags ; }
  public void setTags(List<Tag> tg) { tags = tg ; }
  
}
