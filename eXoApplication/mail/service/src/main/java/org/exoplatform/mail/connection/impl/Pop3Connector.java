/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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

import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.Utils;

import com.sun.mail.pop3.POP3Store;
import com.sun.mail.util.MailSSLSocketFactory;

/**
 * Created by The eXo Platform SAS
 * Author : Tran Hung Phong
 *          phongth@exoplatform.com
 * Oct 03, 2011  
 */
public class Pop3Connector extends BaseConnector {
  protected POP3Store pop3Store;
  
  public Pop3Connector(Account account, MailSSLSocketFactory sslSocket) throws Exception {
    Session session = getSession(account, sslSocket);
    
    String protocolName = Utils.SVR_POP3;
    String emailAddr = account.getIncomingUser();
    if (Utils.isGmailAccount(emailAddr)) {
      protocolName = Utils.SVR_POP3S;
    }
    
    store_ = session.getStore(protocolName);
    openStore(account);
    pop3Store = (POP3Store) store_;
  }
  
  @Override
  public Session getSession(Account account, MailSSLSocketFactory sslSocket) throws Exception {
    Properties props = System.getProperties();

    props.put("mail.mime.base64.ignoreerrors", "true");
    props.put("mail.pop3.host", account.getIncomingHost());
    props.put("mail.pop3.user", account.getIncomingUser());
    props.put("mail.pop3.port", account.getIncomingPort());
    props.put("mail.pop3.auth", "true");
    props.put("mail.pop3.socketFactory.port", account.getIncomingPort());
    props.put("mail.pop3.socketFactory.class", "javax.net.SocketFactory");

    if (account.isIncomingSsl() && sslSocket != null) {
      props.put("mail.pop3.socketFactory.fallback", "false");
      props.put("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
      props.put(Utils.POP3_SSL_FACTORY, sslSocket);
      if (account.getSecureAuthsIncoming().equalsIgnoreCase(Utils.STARTTLS)) {
        props.put(Utils.POP3_SSL_STARTTLS_ENABLE, true);
      } else {
        props.put(Utils.MAIL_POP3_SSL_ENABLE, true);
      }
    }
    
    return Session.getInstance(props, null);
  }
}
