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
package org.exoplatform.mail.service.impl;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;

/**
 * @author : Huu-Dung Kieu
 *           huu-dung.kieu@bull.be
 *           
 * This is a plugin running every time a new user is created.
 * The goal is to create a default mail account for each user.
 * The plugin configuration is defined in the portal/conf/cs/cs-plugin-configuration.xml file. 
 *
 */
public class NewUserListener extends UserEventListener {
  private MailService mservice_ ;

  final static public String FD_INBOX = "Inbox".intern();
  final static public String FD_DRAFTS = "Drafts".intern() ;
  final static public String FD_SENT = "Sent".intern() ;
  final static public String FD_SPAM = "Spam".intern() ;
  final static public String FD_TRASH = "Trash".intern() ;
  final static public String[] defaultFolders_ =  {FD_INBOX ,FD_DRAFTS, FD_SENT, FD_SPAM, FD_TRASH} ;

  String protocol;
  boolean isSSL;
  String incomingHost;
  String incomingPort;
  String incomingFolder;
  String outgoingHost;
  String outgoingPort;

  public NewUserListener(MailService mservice, InitParams params) throws Exception {
    mservice_ = mservice ;

    // parameters defined in the plugin configuration
    protocol       = params.getValueParam("protocol").getValue() ;
    String ssl     = params.getValueParam("ssl").getValue() ;
    isSSL          = (ssl != null) && ssl.equalsIgnoreCase("true");
    incomingHost   = params.getValueParam("incomingServer").getValue() ;
    incomingPort   = params.getValueParam("incomingPort").getValue() ;
    incomingFolder = params.getValueParam("incomingFolder").getValue() ;
    outgoingHost   = params.getValueParam("outgoingServer").getValue() ;
    outgoingPort   = params.getValueParam("outgoingPort").getValue() ;
  }

  @SuppressWarnings("deprecation")
  public void postSave(User user, boolean isNew) throws Exception {
    if(!isNew) return ;
    String fullName     = user.getFullName();
    String email        = user.getEmail();
    // Once eXo is connected to LDAP users, the password will be correct !!!
    String password     = user.getPassword();

    Account acc = new Account();

    String incomingUserName = email;

    acc.setLabel(fullName) ;
    acc.setDescription("") ;
    acc.setUserDisplayName(fullName) ;
    acc.setEmailAddress(email) ;
    acc.setEmailReplyAddress(email) ;
    acc.setSignature("") ;
    acc.setIncomingUser(incomingUserName); 
    acc.setIncomingPassword(password);
    acc.setIsSavePassword(true) ;
    acc.setIncomingHost(incomingHost);
    acc.setIncomingPort(incomingPort);  
    acc.setProtocol(protocol);  
    acc.setIncomingSsl(isSSL);
    acc.setIncomingFolder(incomingFolder) ;
    acc.setServerProperty(Utils.SVR_SMTP_USER, incomingUserName);
    acc.setOutgoingHost(outgoingHost);
    acc.setOutgoingPort(outgoingPort);

    SessionProvider sProvider = SessionProvider.createSystemProvider();
    String username = user.getUserName();
    String accId    = acc.getId();
    String folderId = null;
    Folder folder   = null;
    try {
      mservice_.createAccount(sProvider, username, acc);
      for(String folderName : defaultFolders_) {
        folderId = Utils.createFolderId(accId, folderName, false);
        folder = mservice_.getFolder(sProvider, username, accId, folderId) ;
        if(folder == null) {
          folder = new Folder() ;
          folder.setId(folderId);
          folder.setName(folderName) ;
          folder.setLabel(folderName) ;
          folder.setPersonalFolder(false) ;
          mservice_.saveFolder(sProvider, username, accId, folder) ;
        }
      }
    } catch (Exception e) {
      e.printStackTrace() ;
    } finally {
      sProvider.close();
    }
  }

}