/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.mail.connection.impl;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;

import org.exoplatform.mail.connection.Connector;
import org.exoplatform.mail.service.Account;

import com.sun.mail.util.MailSSLSocketFactory;

/**
 * Created by The eXo Platform SAS
 * Author : Phung Hai Nam
 *          nam.phung@exoplatform.com
 * Sep 18, 2009  
 * Modified by Nguyen Van Hoang
 */
public abstract class BaseConnector implements Connector {
  protected Store store_;
  
  protected Account account_;

  public abstract Session getSession(Account account, MailSSLSocketFactory sslSocket) throws Exception;
  
  public boolean isConnected() {
    return (store_ != null) && store_.isConnected();
  }
  
  public void openStore(Account account) throws Exception {
    if (store_ == null) {
      return;
    }
    this.account_ = account;
    store_.connect(account.getIncomingHost(), Integer.valueOf(account.getIncomingPort()), account.getIncomingUser(), account.getIncomingPassword());
  }
  
  public Store getStore() {
    return store_;
  }

  public Account getAccount() {
    return account_;
  }
  
  public javax.mail.Folder getDefaultFolder() throws MessagingException {
    return store_.getDefaultFolder();
  }
  
  public javax.mail.Folder getFolder(String folderUrl) throws MessagingException {
    return store_.getFolder(new URLName(folderUrl));
  }
  
  public javax.mail.Folder getFolder(URLName folderUrl) throws MessagingException {
    return store_.getFolder(folderUrl);
  }
  
  public void close() {
    if (store_ != null) {
      try {
        store_.close();
      } catch (MessagingException e) {
      }
    }
  }
}
