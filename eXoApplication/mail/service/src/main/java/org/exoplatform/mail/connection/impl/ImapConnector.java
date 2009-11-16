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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.UIDFolder;
import javax.mail.URLName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.Utils;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;


/**
 * Created by The eXo Platform SAS
 * Author : Nam Phung
 *          nam.phung@exoplatform.com
 * Sep 18, 2009  
 */
public class ImapConnector extends BaseConnector {
  private static final Log logger = LogFactory.getLog(Utils.class);
  
  private Account account_;  
  
  public ImapConnector(Account account) throws Exception {
    Session session = getSession(account);
    IMAPStore imapStore = (IMAPStore)session.getStore("imap");
    store_ = imapStore;
    account_ = account;
    store_.connect(account_.getIncomingHost(), Integer.valueOf(account_.getIncomingPort()), 
                   account_.getIncomingUser(), account_.getIncomingPassword());
  }
  
  public Store getStore() { return store_; } 

  public void openStore(Account account) throws Exception { }
  
  private Session getSession(Account account) throws Exception {
    Properties props = System.getProperties();
    String socketFactoryClass = "javax.net.SocketFactory";
    if (account.isIncomingSsl()) socketFactoryClass = Utils.SSL_FACTORY;
    props.setProperty("mail.imap.socketFactory.class", socketFactoryClass);
    props.setProperty("mail.mime.base64.ignoreerrors", "true");
    props.setProperty("mail.imap.socketFactory.fallback", "false");
    return Session.getInstance(props, null);
  }

  public javax.mail.Folder createFolder(Folder folder) throws Exception {
    return createFolder(null, folder);
  }
  
  public javax.mail.Folder createFolder(Folder parentFolder, Folder folder) throws Exception {
    IMAPFolder imapFolder = null;
    if (parentFolder == null) {
      imapFolder = (IMAPFolder) ((IMAPStore)store_).getFolder(folder.getName());
      imapFolder.create((int) folder.getType());
    } else {
      URLName url = new URLName(parentFolder.getURLName());
      IMAPFolder parentImapFolder = (IMAPFolder) ((IMAPStore)store_).getFolder(url);
      if (parentImapFolder != null && parentImapFolder.exists()) {
        boolean isOpen = parentImapFolder.isOpen();
        if (!isOpen) parentImapFolder.open(javax.mail.Folder.READ_WRITE);
        imapFolder = (IMAPFolder) parentImapFolder.getFolder(folder.getName());
        if (!imapFolder.exists()) {
          imapFolder.create((int) folder.getType());
        }
        parentImapFolder.close(true);
      }
    }
    
    return imapFolder;
  }

  public boolean renameFolder(String newName, Folder folder) throws Exception {
    try {
      boolean result = false;
      URLName url = new URLName(folder.getURLName());
      IMAPFolder folderToBeRenamed = (IMAPFolder) ((IMAPStore)store_).getFolder(url);
      if (folderToBeRenamed.exists()) {
        if (folderToBeRenamed.isOpen()) folderToBeRenamed.close(false);
        IMAPFolder f1 = (IMAPFolder) ((IMAPStore)store_).getFolder(newName);
        result = folderToBeRenamed.renameTo(f1);
        if (!result) logger.info("Error while renaming folder!");
      } else {
        logger.info("Folder does not exists!");
      }
      return result;
    } catch (Exception ex) {
      return false;
    }
  }
  
  public int emptyFolder(Folder folder) throws Exception {
    IMAPFolder folderToEmpty = (IMAPFolder) ((IMAPStore)store_).getFolder(folder.getName());
    javax.mail.Message[] messages = folderToEmpty.getMessagesByUID(0, UIDFolder.LASTUID);
    int messageCount = messages.length;
    for (int i = 0; i < messageCount; i++) {
      messages[i].setFlag(Flags.Flag.DELETED, true);
    }
    folderToEmpty.close(true);
    return messageCount;
  }
  
  public boolean deleteFolder(Folder folder) throws Exception {
    try {
      boolean result = false;
      URLName url = new URLName(folder.getURLName());
      IMAPFolder folderToBeRemoved = (IMAPFolder) ((IMAPStore)store_).getFolder(url);
      if (folderToBeRemoved.exists()) {
        if (folderToBeRemoved.isOpen()) folderToBeRemoved.close(true);
        result = folderToBeRemoved.delete(true);
        if (!result) logger.info("Error while deleting folder!");
      } else {
        logger.info("Folder does not exists!");
      }
      return result;
    } catch (Exception ex) {
      return false;
    }
  }

  public boolean createMessage(Message msg) throws Exception {
    // TODO not yet implemented
    return false;
  }

  public boolean deleteMessage(List<Message> msgs, Folder folder) throws Exception {
    try {
      URLName url = new URLName(folder.getURLName());
      IMAPFolder inFolder = (IMAPFolder) ((IMAPStore)store_).getFolder(url);
      boolean isOpen = inFolder.isOpen(); 
      if (!isOpen) inFolder.open(javax.mail.Folder.READ_WRITE);
      javax.mail.Message message;
      for (Message msg : msgs) {
        message = inFolder.getMessageByUID(Long.valueOf(msg.getUID()));
        if (message != null) message.setFlag(Flags.Flag.DELETED, true);
      }
      inFolder.close(true);
      return true;
    } catch(Exception e) {
      return false;
    }
  }

  public boolean moveMessage(List<Message> msgs, Folder currentFolder, Folder desFolder) throws Exception {
    try {
      //TODO: not yet implemented for CS-3544
      if (!currentFolder.isPersonalFolder() && !currentFolder.getName().equalsIgnoreCase(Utils.FD_INBOX)) return true;
      if (!desFolder.isPersonalFolder() && !desFolder.getName().equalsIgnoreCase(Utils.FD_INBOX)) return true;
      
      URLName fromURL = new URLName(currentFolder.getURLName());
      IMAPFolder fromFolder = (IMAPFolder) ((IMAPStore)store_).getFolder(fromURL);
      URLName toURL = new URLName(desFolder.getURLName());
      IMAPFolder toFolder = (IMAPFolder) ((IMAPStore)store_).getFolder(toURL);

      fromFolder.open(javax.mail.Folder.READ_WRITE);
      toFolder.open(javax.mail.Folder.READ_WRITE);
      List<javax.mail.Message> copiedMsgs = new ArrayList<javax.mail.Message>();
      javax.mail.Message msg;
      for (Message m : msgs) {
        msg = fromFolder.getMessageByUID(Long.valueOf(m.getUID()));
        if (msg != null) copiedMsgs.add(msg);
      }
      fromFolder.copyMessages(copiedMsgs.toArray(new javax.mail.Message[copiedMsgs.size()]), toFolder);
      
      for (int k=0; k<copiedMsgs.size(); k++) {
        copiedMsgs.get(k).setFlag(Flags.Flag.DELETED, true);
      }
      fromFolder.close(true);
      toFolder.close(true);
      return true;
    } catch (Exception e) {
      logger.error("Error in moveMessage()",e);
    }
    return false;
  }

  public boolean markAsRead(List<Message> msgList, Folder f) throws Exception {
    try {
      URLName url = new URLName(f.getURLName());
      IMAPFolder folder = (IMAPFolder) ((IMAPStore)store_).getFolder(url);
      if (!folder.isOpen()) folder.open(javax.mail.Folder.READ_WRITE);
      javax.mail.Message message;
      for (Message msg : msgList) {
        message = folder.getMessageByUID(Long.valueOf(msg.getUID()));
        if (message != null) message.setFlag(Flags.Flag.SEEN, true); 
      }
      folder.close(true);
      return true;
    } catch(Exception e) {
      return false;
    }
  }

  public boolean markAsUnread(List<Message> msgList, Folder f) throws Exception {
    try {
      URLName url = new URLName(f.getURLName());	
      IMAPFolder folder = (IMAPFolder) ((IMAPStore)store_).getFolder(url);
      if (!folder.isOpen()) folder.open(javax.mail.Folder.READ_WRITE);
      javax.mail.Message message;
      for (Message msg : msgList) {
        message = folder.getMessageByUID(Long.valueOf(msg.getUID()));
        if (message != null) message.setFlag(Flags.Flag.SEEN, false); 
      }
      folder.close(true);
      return true;
    } catch(Exception e) {
      return false;
    }
  }

  public boolean setIsStared(List<Message> msgList, boolean isStared, Folder f) throws Exception {
    try {
      URLName url = new URLName(f.getURLName());
      IMAPFolder folder = (IMAPFolder) ((IMAPStore)store_).getFolder(url);
      if (!folder.isOpen()) folder.open(javax.mail.Folder.READ_WRITE);
      javax.mail.Message message;
      for (Message msg : msgList) {
        message = folder.getMessageByUID(Long.valueOf(msg.getUID()));
        if (message != null) message.setFlag(Flags.Flag.FLAGGED, isStared); 
      }
      folder.close(true);
      return true;
    } catch(Exception e) {
      return false;
    }
  }

}
