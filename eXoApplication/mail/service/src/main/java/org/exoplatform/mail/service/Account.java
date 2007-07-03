/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.service;

import java.util.List;
import java.util.Map;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan.nguyen@exoplatform.com
 * Jun 23, 2007  
 * 
 */
public class Account {
  private String id_ ;
  private String label_ ;
  private String userDisplayName_ ;
  private String emailAddress_ ;
  private String emailReplyAddress_ ;
  private String signature_ ;  
  private String description_ ;
  
  private Map<String, String> serverProperties_ ;  
  private List<Folder> defaultFolders_ ;
  private List<Folder> userFolders_ ;  
  private List<String> tags_ ;
  private List<MessageFilter> filters_ ;
  private MailServerConfiguration mailServerConfiguration_ ;
  
  /**
   * The id of the account for ex: GmailAccount, YahooAccount
   * @return the id of the account
   */
  public String getId()  { return id_ ; }
  public void   setId(String s) { id_ = s ; }
  
  /**
   * The display label of the account for ex:  Google Mail, Yahoo Mail
   * @return The label of the account
   */
  public String getLabel() { return label_ ; }
  public void   setLabel(String s) { label_ = s ; }
  
  /**
   * @return Return a list of the default folder: Inbox, Sent, Draft, Spam and Trash
   */
  public List<Folder> getDefaultFolder() { return defaultFolders_ ; }
  public void setDefaultFolder(List<Folder> folders) { defaultFolders_ = folders ; }
  
  /**
   * @return Return a list of the folder that is created by the user
   */
  public List<Folder> getUserFolder() { return userFolders_ ; }
  public void setUserFolder(List<Folder> folders) { userFolders_ = folders ; }
  
  /**
   * @return Return a description_ of account
   */
  public String getDescription() { return description_ ; }
  public void setDescription(String s) { description_ = s ; }
  
  /**
   * @return Return a signature of account
   */
  public String getSignature() { return signature_ ; }
  public void setSignature(String s) { signature_ = s ; }
  
  /**
   * @return Return a reply email address name of account
   */
  public String getEmailReplyAddress() { return emailReplyAddress_ ; }
  public void setEmailReplyAddress(String s) { emailReplyAddress_ = s ; }
  
  /**
   * @return Return a email address name of account
   */
  public String getEmailAddress() { return emailAddress_ ; }
  public void setEmailAddress(String s) { emailAddress_ = s ; }
  
  /**
   * @return Return a display name of account
   */
  public String getUserDisplayName() { return userDisplayName_ ; }
  public void setUserDisplayName(String s) { userDisplayName_ = s ; }
  
  /**
   * @return Return a mail server configuration of account
   */
  public MailServerConfiguration getConfiguration() { return mailServerConfiguration_ ; }
  public void setConfiguration(MailServerConfiguration config) { mailServerConfiguration_ = config ; }
  
  public Folder  getFolderByName(String name) { return null ; }
  
}
