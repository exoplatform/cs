/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.service;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * July 2, 2007  
 * 
 */
public class MailServerConfiguration {
  private String protocol ;
  private String host ;
  private String port ;
  private String folder ;
  private String username ;
  private String password ;
  
  /**
   * The protocol_ supported by mail server ex: pop3, imap
   * @return the protocol_
   */
  public String getProtocol()  { return protocol ; }
  public void   setProtocol(String s) { protocol = s ; }
  
  public String getHost() { return host ; }
  public void   setHost(String s) { host = s ; }
  
  public String getPort() { return port ; }
  public void   setPort(String s) { port = s ; }
  
  public String getFolder() { return folder ; }
  public void   setFolder(String s) { folder = s ; }
  
  public String getUserName() { return username ; }
  public void   setUserName(String s) { username = s ; }
  
  public String getPassword() { return password ; }
  public void   setPassword(String s) { password = s ; }
  
}
