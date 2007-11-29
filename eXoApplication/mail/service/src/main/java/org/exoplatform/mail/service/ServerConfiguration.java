/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.service;

import java.util.HashMap;
/**
 * Created by The eXo Platform SARL
 * Author : Phung Nam 
 *          phunghainam@gmail.com
 * Nov 23, 2007  
 */
public class ServerConfiguration extends HashMap<String, String> {
  private static final long serialVersionUID = 1L;
  private String protocol ;
  private String incomingHost ;
  private String incomingPort ;
  private String outgoingHost ;
  private String outgoingPort ;
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
  
  public String getIncomingHost() { return incomingHost ; }
  public void   setIncomingHost(String s) { incomingHost = s ; }
  
  public String getIncomingPort() { return incomingPort; }
  public void   setIncomingPort(String s) { incomingPort = s ; }
  
  public String getOutgoingHost() { return outgoingHost ; }
  public void   setOutgoingHost(String s) { outgoingHost = s ; }
  
  public String getOutgoingPort() { return outgoingPort; }
  public void   setOutgoingPort(String s) { outgoingPort = s ; }
  
  public String getFolder() { return folder ; }
  public void   setFolder(String s) { folder = s ; }
  
  public String getUserName() { return username ; }
  public void   setUserName(String s) { username = s ; }
  
  public String getPassword() { return password ; }
  public void   setPassword(String s) { password = s ; }
  
  public boolean isSsl() {  return ssl_;  }
  public void setSsl(boolean ssl) { this.ssl_ = ssl; }
  
}
