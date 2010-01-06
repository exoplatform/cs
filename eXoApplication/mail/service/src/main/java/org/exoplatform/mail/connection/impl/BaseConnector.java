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

import java.util.Properties;

import javax.mail.Session;
import javax.mail.Store;

import org.exoplatform.mail.connection.Connector;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.Utils;

/**
 * Created by The eXo Platform SAS
 * Author : Phung Hai Nam
 *          nam.phung@exoplatform.com
 * Sep 18, 2009  
 */
public abstract class BaseConnector implements Connector {
  protected Store store_;
  
  public Session getSession(Account account) throws Exception {
    Properties props = System.getProperties();
    String socketFactoryClass = "javax.net.SocketFactory";
    if (account.isIncomingSsl()) socketFactoryClass = Utils.SSL_FACTORY;
    props.setProperty("mail.imap.socketFactory.class", socketFactoryClass);
    props.setProperty("mail.mime.base64.ignoreerrors", "true");
    props.setProperty("mail.imap.socketFactory.fallback", "false");
    return Session.getInstance(props, null);
  }
  
  
  
}
