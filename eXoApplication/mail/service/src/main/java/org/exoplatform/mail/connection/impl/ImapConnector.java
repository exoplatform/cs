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
import javax.mail.internet.MimeMessage;

import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.MimeMessageParser;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

/**
 * Created by The eXo Platform SAS Author : Nam Phung nam.phung@exoplatform.com
 * Sep 18, 2009
 */
public class ImapConnector extends BaseConnector {
  private static final Log logger = ExoLogger.getLogger("cs.mail.service");

  private Account          account_;

  public ImapConnector(Account account) throws Exception {
    Session session = getSession(account);
    IMAPStore imapStore = (IMAPStore) session.getStore("imap");
    store_ = imapStore;
    account_ = account;
    store_.connect(account_.getIncomingHost(),
                   Integer.valueOf(account_.getIncomingPort()),
                   account_.getIncomingUser(),
                   account_.getIncomingPassword());
  }

  public Store getStore() {
    return store_;
  }

  public void openStore(Account account) throws Exception {
  }

  public javax.mail.Folder createFolder(Folder folder) throws Exception {
    return createFolder(null, folder);
  }

  public javax.mail.Folder createFolder(Folder parentFolder, Folder folder) throws Exception {
    IMAPFolder imapFolder = null;
    if (parentFolder == null) {
      imapFolder = (IMAPFolder) ((IMAPStore) store_).getFolder(folder.getName());
      if (!imapFolder.exists())
        imapFolder.create((int) folder.getType());
    } else {
      URLName url = new URLName(parentFolder.getURLName());
      IMAPFolder parentImapFolder = (IMAPFolder) ((IMAPStore) store_).getFolder(url);
      if (parentImapFolder != null && parentImapFolder.exists()) {
        boolean isOpen = parentImapFolder.isOpen();
        if (!isOpen)
          parentImapFolder.open(javax.mail.Folder.READ_WRITE);
        imapFolder = (IMAPFolder) parentImapFolder.getFolder(folder.getName());
        if (!imapFolder.exists()) {
          imapFolder.create((int) folder.getType());
        }
        parentImapFolder.close(true);
      }
    }

    return imapFolder;
  }

  public Folder renameFolder(String newName, Folder folder) throws Exception {
    try {
      boolean result = false;
      URLName url = new URLName(folder.getURLName());
      IMAPFolder folderToBeRenamed = (IMAPFolder) ((IMAPStore) store_).getFolder(url);
      if (folderToBeRenamed.exists()) {
        if (folderToBeRenamed.isOpen())
          folderToBeRenamed.close(false);
        IMAPFolder f1 = (IMAPFolder) ((IMAPStore) store_).getFolder(newName);
        result = folderToBeRenamed.renameTo(f1);
        folder.setURLName(f1.getURLName().toString());
        folder.setName(newName);
        if (!result)
          logger.info("Error while renaming folder!");
      } else {
        logger.info("Folder does not exists!");
      }
      return folder;
    } catch (Exception ex) {
      ex.printStackTrace();
      return null;
    }
  }

  public int emptyFolder(Folder folder) throws Exception {
    IMAPFolder folderToEmpty = (IMAPFolder) ((IMAPStore) store_).getFolder(folder.getName());
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
      IMAPFolder folderToBeRemoved = (IMAPFolder) ((IMAPStore) store_).getFolder(url);
      if (folderToBeRemoved.exists()) {
        if (folderToBeRemoved.isOpen())
          folderToBeRemoved.close(true);
        result = folderToBeRemoved.delete(true);
        if (!result)
          logger.info("Error while deleting folder!");
      } else {
        logger.info("Folder does not exists!");
      }
      return result;
    } catch (Exception ex) {
      return false;
    }
  }

  public List<Message> createMessage(List<Message> msgs, Folder folder) throws Exception {
    try {
      URLName remoteURL = new URLName(folder.getURLName());
      IMAPFolder remoteFolder = (IMAPFolder) ((IMAPStore) store_).getFolder(remoteURL);
      if(!remoteFolder.isOpen()) remoteFolder.open(javax.mail.Folder.READ_WRITE);
      Properties props = System.getProperties();
      Session session = Session.getInstance(props, null);
      javax.mail.Message[] messages = new javax.mail.Message[msgs.size()];
      for (int i = 0; i < msgs.size(); i++) {
        MimeMessage mimeMessage = new MimeMessage(session);
        mimeMessage = Utils.mergeToMimeMessage(msgs.get(i), mimeMessage);
        messages[i] = (javax.mail.Message) mimeMessage;
      }
      if(remoteFolder.isOpen()){
        javax.mail.Message[] updatedMsgs = remoteFolder.addMessages(messages);
        if (updatedMsgs.length == msgs.size()) {
          String uid = "";
          for (int l = 0; l < updatedMsgs.length; l++) {
            if(updatedMsgs[l] != null) {
              uid = String.valueOf(remoteFolder.getUID(updatedMsgs[l]));
              if (Utils.isEmptyField(uid)) uid = MimeMessageParser.getMsgUID();
              msgs.get(l).setId(MimeMessageParser.getMessageId(updatedMsgs[l]));
              msgs.get(l).setUID(uid);
          }
        }
          remoteFolder.close(true);
        }
      }
    } catch (Exception e) {
      logger.error("Error in move messages to remote folder.\n",e);
      return null;
    }
    return msgs;
  }

  public boolean deleteMessage(List<Message> msgs, Folder folder) throws Exception {
    try {
      URLName url = new URLName(folder.getURLName());
      IMAPFolder inFolder = (IMAPFolder) ((IMAPStore) store_).getFolder(url);
      boolean isOpen = inFolder.isOpen();
      if (!isOpen)
        inFolder.open(javax.mail.Folder.READ_WRITE);
      javax.mail.Message message;
      for (Message msg : msgs) {
        message = inFolder.getMessageByUID(Long.valueOf(msg.getUID()));
        if (message != null)
          message.setFlag(Flags.Flag.DELETED, true);
      }
      inFolder.close(true);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public List<Message> moveMessage(List<Message> msgs, Folder sourceFolder, Folder desFolder) throws Exception {
    if (!sourceFolder.isPersonalFolder() && !sourceFolder.getName().equalsIgnoreCase(Utils.FD_INBOX) && desFolder.isPersonalFolder()) {//local->server
      return moveMessages(msgs, sourceFolder, desFolder, true, true);
    }else if(!sourceFolder.isPersonalFolder() && !sourceFolder.getName().equalsIgnoreCase(Utils.FD_INBOX) && !desFolder.isPersonalFolder()){//local to local
      return moveMessages(msgs, sourceFolder, desFolder, true, false);
    }else if ((sourceFolder.isPersonalFolder() && !desFolder.getName().equalsIgnoreCase(Utils.FD_INBOX) && !desFolder.isPersonalFolder()) ||
          (sourceFolder.getName().equalsIgnoreCase(Utils.FD_INBOX) && !desFolder.getName().equalsIgnoreCase(Utils.FD_INBOX) && !desFolder.isPersonalFolder())) {//server to local
      return moveMessages(msgs, sourceFolder, desFolder, false, false);
    }else if((sourceFolder.isPersonalFolder() && desFolder.isPersonalFolder()) ||
          (sourceFolder.getName().equalsIgnoreCase(Utils.FD_INBOX) &&  desFolder.isPersonalFolder()) || 
          (desFolder.getName().equalsIgnoreCase(Utils.FD_INBOX) &&  sourceFolder.isPersonalFolder())){//server to server
      return moveMessages(msgs, sourceFolder, desFolder, false, true);
    }
  return null;
  }

  /**
   * Move message(s). Folder will be synchronized before message(s) moved. There 4 instances when move the message action:
   (Source folder   ->    Remote folder)
   Local            -    Remote.
   Local            -    Local.
   Remote           -    Local.
   Remote           -    Remote.
   * return {@link List} of mails that were not moved/deleted*/
  private List<Message> moveMessages(List<Message> msgs, Folder sourceFolder, Folder desFolder, boolean isLocalFolder, boolean isRemoteFolder) throws Exception {
    IMAPFolder sourceImapFolder = null, desImapFolder = null;
    List<Message> successes = new ArrayList<Message>();
    javax.mail.Message[] updatedMsgs = null;
    try {
      if(isLocalFolder && isRemoteFolder){//local -> remote
        sourceImapFolder = (IMAPFolder) createFolder(sourceFolder);
        URLName desURL = new URLName(desFolder.getURLName());
        desImapFolder = (IMAPFolder) ((IMAPStore)store_).getFolder(desURL);
      } else if(isLocalFolder && !isRemoteFolder){//local -> local
        sourceImapFolder = (IMAPFolder) createFolder(sourceFolder);
        desImapFolder = (IMAPFolder) createFolder(desFolder);
      }else if(!isLocalFolder && isRemoteFolder){//remote -> remote
        URLName srcURL = new URLName(sourceFolder.getURLName());
        sourceImapFolder = (IMAPFolder) ((IMAPStore)store_).getFolder(srcURL);
        URLName desURL = new URLName(desFolder.getURLName());
        desImapFolder = (IMAPFolder) ((IMAPStore)store_).getFolder(desURL);
      } else if(!isLocalFolder && !isRemoteFolder){//remote -> local
        URLName srcURL = new URLName(sourceFolder.getURLName());
        sourceImapFolder = (IMAPFolder) ((IMAPStore)store_).getFolder(srcURL);
        desImapFolder = (IMAPFolder) createFolder(desFolder);
      }
      if(sourceImapFolder == null || desImapFolder == null) return null;
      try {
        if(!sourceImapFolder.isOpen()) sourceImapFolder.open(javax.mail.Folder.READ_WRITE);
        if(!desImapFolder.isOpen()) desImapFolder.open(javax.mail.Folder.READ_WRITE);  
      } catch (Exception e) {
        logger.debug("ImapConnector: \"" + sourceFolder + "\" or \"" + desFolder+  "\" folder was not synchronized with server\n", e);
      }
       
      if(sourceImapFolder.isOpen() && desImapFolder.isOpen()){
        List<javax.mail.Message> copiedMsgs = new ArrayList<javax.mail.Message>();
        javax.mail.Message msg = null;
        for (Message m : msgs) {
          try{
          msg = sourceImapFolder.getMessageByUID(Long.valueOf(m.getUID()));
          successes.add(m);
          }catch (Exception e){
            logger.warn("The UID: \"" + m.getUID() + "\" for message: \""+ m.getSubject() +"\" is not exist on server mail\n", e);
          }
          if (msg != null) copiedMsgs.add(msg);
        }
        try{
          updatedMsgs = desImapFolder.addMessages(copiedMsgs.toArray(new javax.mail.Message[copiedMsgs.size()]));
        }catch(MessagingException me){
          logger.warn("Cannot add message(s) to server mail\n",  me);
          return null;
        }
        for (int k = 0; k < copiedMsgs.size(); k++) {
          try{
            copiedMsgs.get(k).setFlag(Flags.Flag.DELETED, true);  
          }catch (Exception e){logger.warn("Cannot mark delete flag to server mail. \n",  e);}
        }
        if (updatedMsgs.length == msgs.size()) {
          String uid = "";
          for (int l = 0; l < updatedMsgs.length; l++) {
           try {
             uid = String.valueOf(desImapFolder.getUID(updatedMsgs[l]));            
            } catch (Exception e) {
              if(logger.isDebugEnabled()) logger.debug("Can not get Message UID on server. \n", e);  
            } 
            if (Utils.isEmptyField(uid)) uid = MimeMessageParser.getMsgUID();
            msgs.get(l).setUID(uid);
          }
        }else logger.warn("Not all messages were moved/deleted");
        sourceImapFolder.close(true);
        desImapFolder.close(true);
      }
    } catch (Exception e) {
      logger.error("ImapConnector: Error in move message.\n",e);
    }
    return successes;
  }

  public boolean markAsRead(List<Message> msgList, Folder f) throws Exception {
    try {
      URLName url = new URLName(f.getURLName());
      IMAPFolder folder = (IMAPFolder) ((IMAPStore) store_).getFolder(url);
      if (!folder.isOpen())
        folder.open(javax.mail.Folder.READ_WRITE);
      javax.mail.Message message;
      for (Message msg : msgList) {
        message = folder.getMessageByUID(Long.valueOf(msg.getUID()));
        if (message != null)
          message.setFlag(Flags.Flag.SEEN, true);
      }
      folder.close(true);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public boolean markAsUnread(List<Message> msgList, Folder f) throws Exception {
    try {
      URLName url = new URLName(f.getURLName());
      IMAPFolder folder = (IMAPFolder) ((IMAPStore) store_).getFolder(url);
      if (!folder.isOpen())
        folder.open(javax.mail.Folder.READ_WRITE);
      javax.mail.Message message;
      for (Message msg : msgList) {
        message = folder.getMessageByUID(Long.valueOf(msg.getUID()));
        if (message != null)
          message.setFlag(Flags.Flag.SEEN, false);
      }
      folder.close(true);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public boolean setIsStared(List<Message> msgList, boolean isStared, Folder f) throws Exception {
    try {
      URLName url = new URLName(f.getURLName());
      IMAPFolder folder = (IMAPFolder) ((IMAPStore) store_).getFolder(url);
      if (!folder.isOpen())
        folder.open(javax.mail.Folder.READ_WRITE);
      javax.mail.Message message;
      for (Message msg : msgList) {
        message = folder.getMessageByUID(Long.valueOf(msg.getUID()));
        if (message != null)
          message.setFlag(Flags.Flag.FLAGGED, isStared);
      }
      folder.close(true);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

}
