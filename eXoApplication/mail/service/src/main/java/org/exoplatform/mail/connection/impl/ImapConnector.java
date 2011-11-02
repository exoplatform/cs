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
import javax.mail.Flags.Flag;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.UIDFolder;
import javax.mail.URLName;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.MimeMessageParser;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.util.MailSSLSocketFactory;

/**
 * Created by The eXo Platform SAS Author : Nam Phung nam.phung@exoplatform.com
 * Sep 18, 2009
 */
public class ImapConnector extends BaseConnector {
  private static final Log logger = ExoLogger.getLogger("cs.mail.service");
  
  protected IMAPStore imapStore;
  
  public ImapConnector(Account account, MailSSLSocketFactory sslSocket) throws Exception {
    Session session = getSession(account, sslSocket);
    String protocolName = Utils.SVR_IMAP;
    String emailaddr = account.getIncomingUser();
    if (Utils.isGmailAccount(emailaddr)) {
      protocolName = Utils.SVR_IMAPS;
    }
    
    store_ = session.getStore(protocolName);
    openStore(account);
    imapStore = (IMAPStore) store_;
  }
  
  public Session getSession(Account account, MailSSLSocketFactory sslSocket) throws Exception {
    Properties props = System.getProperties();
    props.put("mail.imap.socketFactory.class", "javax.net.SocketFactory");
    props.put("mail.mime.base64.ignoreerrors", "true");
    props.put("mail.imap.socketFactory.fallback", "false");
    
    if (account.isIncomingSsl() && sslSocket != null) {
      props.put(Utils.IMAP_SSL_FACTORY, sslSocket);
      if (account.getSecureAuthsIncoming().equalsIgnoreCase(Utils.STARTTLS)) {
        props.put(Utils.IMAP_SSL_STARTTLS_ENABLE, true);
      } else {
        props.put(Utils.MAIL_IMAP_SSL_ENABLE, "true");
      }
      props.put(Utils.IMAP_SASL_MECHS, account.getAuthMechsIncoming());
    }
    
    return Session.getInstance(props, null);
  }
  
  public IMAPFolder createFolder(Folder folder) throws Exception {
    return createFolder(null, folder);
  }

  public IMAPFolder createFolder(Folder parentFolder, Folder folder) throws Exception {
    IMAPFolder imapFolder = null;
    if (parentFolder == null) {
      imapFolder = (IMAPFolder) imapStore.getFolder(folder.getName());
      if (!imapFolder.exists()) {
        imapFolder.create((int) folder.getType());
      }
    } else {
      IMAPFolder parentImapFolder = openFolderForReadWrite(parentFolder.getURLName());
      if (parentImapFolder != null && parentImapFolder.exists()) {
        imapFolder = (IMAPFolder) parentImapFolder.getFolder(folder.getName());
        if (!imapFolder.exists()) {
          imapFolder.create((int) folder.getType());
        }
      }
    }
    return imapFolder;
  }

  public Folder renameFolder(String newName, Folder folder) throws Exception {
    try {
      boolean result = false;
      IMAPFolder folderToBeRenamed = openFolderForReadWrite(folder.getURLName());
      if (folderToBeRenamed.exists()) {
        if (folderToBeRenamed.isOpen()) {
          folderToBeRenamed.close(true);
        }
        IMAPFolder f1 = (IMAPFolder) imapStore.getFolder(newName);
        result = folderToBeRenamed.renameTo(f1);
        folder.setURLName(f1.getURLName().toString());
        folder.setName(newName);
        if (!result) {
          logger.info("Error while renaming folder!");
        }
      } else {
        logger.info("Folder does not exists!");
      }
      return folder;
    } catch (Exception ex) {
      if (logger.isDebugEnabled()) {
        logger.debug("Exception in method renameFolder", ex);
      }
      return null;
    }
  }

  public int emptyFolder(Folder folder) throws Exception {
    IMAPFolder folderToEmpty = (IMAPFolder) imapStore.getFolder(folder.getName());
    javax.mail.Message[] messages = folderToEmpty.getMessagesByUID(0, UIDFolder.LASTUID);
    int messageCount = messages.length;
    for (int i = 0; i < messageCount; i++) {
      messages[i].setFlag(Flags.Flag.DELETED, true);
    }
    return messageCount;
  }

  public boolean deleteFolder(Folder folder) throws Exception {
    try {
      boolean result = false;
      URLName url = new URLName(folder.getURLName());
      IMAPFolder folderToBeRemoved = (IMAPFolder) imapStore.getFolder(url);
      if (folderToBeRemoved.exists()) {
        if (folderToBeRemoved.isOpen()) {
          folderToBeRemoved.close(true);
        }
        result = folderToBeRemoved.delete(true);
        if (!result) {
          logger.info("Error while deleting folder!");
        }
      } else {
        logger.info("Folder does not exists!");
      }
      return result;
    } catch (Exception ex) {
      return false;
    }
  }

  public List<Message> createMessage(List<Message> msgs, Folder folder) throws Exception {
    if (msgs == null || msgs.size() == 0 || folder == null) {
      return null;
    }
    
    // In the case the message already exist, then delete old message
    List<Message> messagesToDelete = new ArrayList<Message>();
    for (Message message : msgs) {
      if (!StringUtils.isEmpty(message.getUID())) {
        messagesToDelete.add(message);
      }
    }
    
    if (messagesToDelete.size() > 0) {
      deleteMessage(messagesToDelete, folder);
    }
    
    // Create new messages
    List<Message> successList = new ArrayList<Message>();
    IMAPFolder remoteFolder = openFolderForReadWrite(folder.getURLName());
    if (remoteFolder == null) {
      createFolder(folder);
      remoteFolder = openFolderForReadWrite(folder.getURLName());
    }
    
    Properties props = System.getProperties();
    Session session = Session.getInstance(props, null);
    javax.mail.Message[] messages = new javax.mail.Message[msgs.size()];
    javax.mail.Message[] createdMsgs = null;
    for (int i = 0; i < msgs.size(); i++) {
      MimeMessage mimeMessage = new MimeMessage(session);
      mimeMessage = Utils.mergeToMimeMessage(msgs.get(i), mimeMessage);
      messages[i] = (javax.mail.Message) mimeMessage;
    }
    
    try {
      createdMsgs = remoteFolder.addMessages(messages);
    } catch (MessagingException me) {
      logger.error("Synchronize message from local to server fail.\n", me);
    }
    if (createdMsgs != null && createdMsgs.length > 0 && createdMsgs.length == msgs.size()) {
      String uid = "";
      for (int l = 0; l < createdMsgs.length; l++) {
        if (createdMsgs[l] != null) {
          try {
            uid = String.valueOf(remoteFolder.getUID(createdMsgs[l]));
          } catch (MessagingException me) {
            logger.warn("Not found UID for \"" + createdMsgs[l].getSubject() + "\".");
          }
          if (Utils.isEmptyField(uid))
            uid = MimeMessageParser.getMsgUID();
          msgs.get(l).setId(MimeMessageParser.getMessageId(createdMsgs[l]));
          msgs.get(l).setUID(uid);
        } else
          logger.warn("Mail server could not append a new UID for message: " + msgs.get(l).getSubject());
        successList.add(msgs.get(l));
      }
    } else {
      logger.warn("Not all messages are synchronized with server.");
    }
    return successList;
  }
  
  public boolean deleteMessage(List<Message> msgs, Folder folder) throws Exception {
    try {
      IMAPFolder remoteFolder = openFolderForReadWrite(folder.getURLName());
      if (remoteFolder == null) {
        return false;
      }
      
      if (!remoteFolder.isOpen()) {
        remoteFolder.open(javax.mail.Folder.READ_WRITE);
      }
      
      javax.mail.Message message;
      for (Message msg : msgs) {
        message = remoteFolder.getMessageByUID(Long.valueOf(msg.getUID()));
        if (message != null) {
          message.setFlag(Flags.Flag.DELETED, true);
        }
      }
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public List<Message> moveMessage(List<Message> msgs, Folder sourceFolder, Folder desFolder) throws Exception {
    if (!sourceFolder.isPersonalFolder() && !sourceFolder.getName().equalsIgnoreCase(Utils.FD_INBOX) && desFolder.isPersonalFolder()) {// local->server
      return moveMessages(msgs, sourceFolder, desFolder, true, true);
    } 
    
    if (!sourceFolder.isPersonalFolder() && !sourceFolder.getName().equalsIgnoreCase(Utils.FD_INBOX) && !desFolder.isPersonalFolder()) {// local to local
      return moveMessages(msgs, sourceFolder, desFolder, true, false);
    }
    
    if ((sourceFolder.isPersonalFolder() && !desFolder.getName().equalsIgnoreCase(Utils.FD_INBOX) && !desFolder.isPersonalFolder()) || (sourceFolder.getName().equalsIgnoreCase(Utils.FD_INBOX) && !desFolder.getName().equalsIgnoreCase(Utils.FD_INBOX) && !desFolder.isPersonalFolder())) {// server to local
      return moveMessages(msgs, sourceFolder, desFolder, false, false);
    }
    
    if ((sourceFolder.isPersonalFolder() && desFolder.isPersonalFolder()) || (sourceFolder.getName().equalsIgnoreCase(Utils.FD_INBOX) && desFolder.isPersonalFolder()) || (desFolder.getName().equalsIgnoreCase(Utils.FD_INBOX) && sourceFolder.isPersonalFolder())) {// server to server
      return moveMessages(msgs, sourceFolder, desFolder, false, true);
    }
    return null;
  }

  /**
   * Move message(s). Folder will be synchronized before message(s) moved. There 4 instances when move the message action:
   * <pre>
   (Source folder   ->    Remote folder)
   Local            -    Remote.
   Local            -    Local.
   Remote           -    Local.
   Remote           -    Remote.
   </pre>
   * return {@link List} of mails that were not moved/deleted*/
  private List<Message> moveMessages(List<Message> msgs, Folder sourceFolder, Folder desFolder, boolean isLocalFolder, boolean isRemoteFolder) throws Exception {
    if (msgs == null || msgs.size() == 0 || (sourceFolder.getId() == desFolder.getId()) || sourceFolder == null || desFolder == null) {
      return null;
    }
    
    IMAPFolder sourceImapFolder = null, desImapFolder = null;
    try {
      if (isLocalFolder && isRemoteFolder) {// local -> remote
        sourceImapFolder = (IMAPFolder) createFolder(sourceFolder);
        URLName desURL = new URLName(desFolder.getURLName());
        desImapFolder = (IMAPFolder) imapStore.getFolder(desURL);
      } else if (isLocalFolder && !isRemoteFolder) {// local -> local
        sourceImapFolder = (IMAPFolder) createFolder(sourceFolder);
        desImapFolder = (IMAPFolder) createFolder(desFolder);
      } else if (!isLocalFolder && isRemoteFolder) {// remote -> remote
        URLName srcURL = new URLName(sourceFolder.getURLName());
        sourceImapFolder = (IMAPFolder) imapStore.getFolder(srcURL);
        URLName desURL = new URLName(desFolder.getURLName());
        desImapFolder = (IMAPFolder) imapStore.getFolder(desURL);
      } else if (!isLocalFolder && !isRemoteFolder) {// remote -> local
        URLName srcURL = new URLName(sourceFolder.getURLName());
        sourceImapFolder = (IMAPFolder) imapStore.getFolder(srcURL);
        desImapFolder = (IMAPFolder) createFolder(desFolder);
      }
      
      if (sourceImapFolder == null || desImapFolder == null) {
        return null;
      }
      
      try {
        if (!sourceImapFolder.isOpen()) {
          sourceImapFolder.open(javax.mail.Folder.READ_WRITE);
        }
        
        if (!desImapFolder.isOpen()) {
          desImapFolder.open(javax.mail.Folder.READ_WRITE);
        }
      } catch (Exception e) {
        logger.debug("ImapConnector: \"" + sourceFolder + "\" or \"" + desFolder + "\" folder was not synchronized with server\n", e);
      }

      if (sourceImapFolder.isOpen() && desImapFolder.isOpen()) {
        List<javax.mail.Message> copiedMsgs = new ArrayList<javax.mail.Message>();
        javax.mail.Message msg = null;
        for (Message m : msgs) {
          try {
            if (m != null && m.getUID() != null) {
              msg = sourceImapFolder.getMessageByUID(Long.valueOf(m.getUID()));
            } else {
              logger.warn("Message is null or UID is null.");
            }
          } catch (Exception e) {
            logger.warn("The UID: \"" + m.getUID() + "\" for message: \"" + m.getSubject() + "\" is not exist on server mail\n");
          }
          
          if (msg != null) {
            copiedMsgs.add(msg);
          }
        }
        
        if (copiedMsgs != null && copiedMsgs.size() > 0) {
          javax.mail.Message[] messages = copiedMsgs.toArray(new javax.mail.Message[copiedMsgs.size()]);
          sourceImapFolder.copyMessages(messages, desImapFolder);
          Flags flags = new Flags();
          flags.add(Flags.Flag.DELETED);
          sourceImapFolder.setFlags(messages, flags, true);
          sourceImapFolder.expunge();
          desImapFolder.expunge();
        }
      }
    } catch (Exception e) {
      logger.error("ImapConnector: Error in move message.\n", e);
    }
    return msgs;
  }

  public boolean markIsReadStared(List<Message> msgList, Folder f, Object isRead, Object isStared) throws Exception {
    try {
      Flag flag = null;
      if (isRead != null) {
        isStared = null;
      } else if (isStared != null) {
        isRead = null;
      }

      IMAPFolder folder = openFolderForReadWrite(f.getURLName());
      if (!folder.isOpen()) {
        folder.open(javax.mail.Folder.READ_WRITE);
      }
      
      javax.mail.Message message;
      for (Message msg : msgList) {
        message = folder.getMessageByUID(Long.valueOf(msg.getUID()));
        if (message != null) {
          boolean value = false;
          if (isRead != null) {
            value = Boolean.valueOf(isRead.toString());
            flag = Flags.Flag.SEEN;
            message.setFlag(flag, value);
          } else if (isStared != null) {
            flag = Flags.Flag.FLAGGED;
            value = Boolean.valueOf(isStared.toString());
            message.setFlag(flag, value);
          } else if (isStared == null && isRead == null) {
            message.setFlag(Flags.Flag.SEEN, value);
            message.setFlag(Flags.Flag.FLAGGED, value);
          }
        }
      }
      return true;
    } catch (Exception e) {
      return false;
    }
  }
  
  public IMAPFolder openFolderForReadWrite(String folderUrl) throws MessagingException {
    return openFolderForReadWrite(new URLName(folderUrl));
  }
  
  public IMAPFolder openFolderForReadWrite(URLName folderUrl) throws MessagingException {
    IMAPFolder remoteFolder = (IMAPFolder) getFolder(folderUrl);
    if (remoteFolder != null) {
      if (!remoteFolder.isOpen()) {
        remoteFolder.open(javax.mail.Folder.READ_WRITE);
      }
    }
    return remoteFolder;
  }
  
  public javax.mail.Message getMessageByUID(String uid, String folderUrl) throws MessagingException {
    IMAPFolder folder = openFolderForReadWrite(folderUrl);
    javax.mail.Message message = folder.getMessageByUID(Long.valueOf(uid));
    return message;
  }
  
  public void importMessageIntoServerMail(String folderUrl, MimeMessage mimeMessage, long[] msgUID) throws Exception {
    IMAPFolder remoteFolder = openFolderForReadWrite(folderUrl);
    remoteFolder.addMessages(new javax.mail.Message[] { mimeMessage });
  }
}
