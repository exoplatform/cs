/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.service;

import java.util.HashMap;
/**
 * <p>
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * July 2, 2007  
 * <br/><br/>
 * MailServerConfiguration contains the following information :
 * <ul>
 * <li>host : the server ip address or fqdn</li>
 * <li>port : POP3 : 110, POP3 (SSL) : 995, IMAP : 143, IMAP (SSL) : 993</li>
 * <li>protocol : pop3 or imap</li>
 * <li>ssl : true if you want to encrypt with ssl, false otherwise</li>
 * <li>username</li>
 * <li>password</li>
 * <li>folder : the folder to open (e.g. INBOX)</li>
 * </ul>
 * </p>
 */
//TOTO: rename to AccountProperties
public class MailServerConfiguration extends HashMap<String, String> {
  private String protocol ;
  private String host ;
  private String port ;
  private String folder ;
  private String username ;
  private String password ;
  private boolean ssl_;
  
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
  
  public boolean isSsl() {  return ssl_;  }
  public void setSsl(boolean ssl) { this.ssl_ = ssl; }
  
}
