/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
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

  private String            protocol;

  private String            incomingHost;

  private String            incomingPort;

  private String            outgoingHost;

  private String            outgoingPort;

  private String            folder;

  private String            username;

  private String            password;

  private boolean           ssl_;

  private boolean           outgoingSsl_;

  private boolean           isAuthen_        = true;

  /**
   * The protocol_ supported by mail server ex: pop3, imap
   * @return the protocol_
   */
  public String getProtocol() {
    return protocol;
  }

  public void setProtocol(String s) {
    protocol = s;
  }

  public String getIncomingHost() {
    return incomingHost;
  }

  public void setIncomingHost(String s) {
    incomingHost = s;
  }

  public String getIncomingPort() {
    return incomingPort;
  }

  public void setIncomingPort(String s) {
    incomingPort = s;
  }

  public String getOutgoingHost() {
    return outgoingHost;
  }

  public void setOutgoingHost(String s) {
    outgoingHost = s;
  }

  public String getOutgoingPort() {
    return outgoingPort;
  }

  public void setOutgoingPort(String s) {
    outgoingPort = s;
  }

  public boolean isOutgoingAuthentication() {
    return isAuthen_;
  }

  public void setIsOutgoingAuthentication(boolean b) {
    isAuthen_ = b;
  }

  public String getFolder() {
    return folder;
  }

  public void setFolder(String s) {
    folder = s;
  }

  public String getUserName() {
    return username;
  }

  public void setUserName(String s) {
    username = s;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String s) {
    password = s;
  }

  public boolean isSsl() {
    return ssl_;
  }

  public void setSsl(boolean ssl) {
    this.ssl_ = ssl;
  }

  public boolean isOutgoingSsl() {
    return outgoingSsl_;
  }

  public void setOutgoingSsl(boolean ssl) {
    this.outgoingSsl_ = ssl;
  }

}
