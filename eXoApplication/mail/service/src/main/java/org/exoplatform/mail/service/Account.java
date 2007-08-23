/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan.nguyen@exoplatform.com
 * Jun 23, 2007  
 * 
 */
public class Account {
  private String id ;
  private String label ;
  private String userDisplayName ;
  private String emailAddress ;
  private String emailReplyAddress ;
  private String signature ;  
  private String description ;
  
  private Map<String, String> serverProperties ;  
  private List<Folder> defaultFolders ;
  private List<Folder> userFolders ;  
  private List<String> tags ;
  private List<MessageFilter> filters ;
  //TODO: use AccountProperty
//  private MailServerConfiguration mailServerConfiguration ;
  
  public Account() {
    id = Utils.KEY_ACCOUNT + IdGenerator.generate() ;
  }
  
  /**
   * The id of the account for ex: GmailAccount, YahooAccount
   * @return the id of the account
   */
  public String getId()  { return id ; }
  public void   setId(String s) { id = s ; }
  
  /**
   * The display label of the account for ex:  Google Mail, Yahoo Mail
   * @return The label of the account
   */
  public String getLabel() { return label ; }
  public void   setLabel(String s) { label = s ; }
  
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
  
  /**
   * @return Return a description_ of account
   */
  public String getDescription() { return description ; }
  public void setDescription(String s) { description = s ; }
  
  /**
   * @return Return a signature of account
   */
  public String getSignature() { return signature ; }
  public void setSignature(String s) { signature = s ; }
  
  /**
   * @return Return a reply email address name of account
   */
  public String getEmailReplyAddress() { return emailReplyAddress ; }
  public void setEmailReplyAddress(String s) { emailReplyAddress = s ; }
  
  /**
   * @return Return a email address name of account
   */
  public String getEmailAddress() { return emailAddress ; }
  public void setEmailAddress(String s) { emailAddress = s ; }
  
  /**
   * @return Return a display name of account
   */
  public String getUserDisplayName() { return userDisplayName ; }
  public void setUserDisplayName(String s) { userDisplayName = s ; }
  
  /**
   * @return Return a mail server configuration of account
   */
//  public MailServerConfiguration getConfiguration() { return mailServerConfiguration ; }
//  public void setConfiguration(MailServerConfiguration config) { mailServerConfiguration = config ; }
  
  public Folder  getFolderByName(String name) { return null ; }
  
  /**
   * Manages the server properties, based on the serverProperties attribute
   */
  public void setServerProperty(String key, String value) {
    if (serverProperties == null) serverProperties = new HashMap<String, String>();
    serverProperties.put(key, value) ;
  }
  
  public Map<String, String> getServerProperties() { return serverProperties ; }
  
  public String getProtocol()  { return serverProperties.get("protocol") ; }
  
  public String getHost()  { return serverProperties.get("host") ; }
  
  public String getPort()  { return serverProperties.get("port") ; }
  
  public String getFolder()  { return serverProperties.get("folder") ; }
  
  public String getUserName()  { return serverProperties.get("username") ; }
  
  public String getPassword()  { return serverProperties.get("password") ; }
  
  public boolean isSsl()  { return serverProperties.get("ssl").equalsIgnoreCase("true");  }
}
