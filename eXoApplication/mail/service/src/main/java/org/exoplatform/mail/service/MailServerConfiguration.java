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
  private String protocol_ ;
  private String host_ ;
  private String port_ ;
  private String folder_ ;
  private String username_ ;
  private String password_ ;
  
  /**
   * The protocol_ supported by mail server ex: pop3, imap
   * @return the protocol_
   */
  public String getProtocol()  { return protocol_ ; }
  public void   setProtocol(String s) { protocol_ = s ; }
  
  public String getHost() { return host_ ; }
  public void   setHost(String s) { host_ = s ; }
  
  public String getPort() { return port_ ; }
  public void   setPort(String s) { port_ = s ; }
  
  public String getFolder() { return folder_ ; }
  public void   setFolder(String s) { folder_ = s ; }
  
  public String getUserName() { return username_ ; }
  public void   setUserName(String s) { username_ = s ; }
  
  public String getPassword() { return password_ ; }
  public void   setPassword(String s) { password_ = s ; }
  
}
