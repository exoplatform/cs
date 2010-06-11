/*
 * Copyright (C) 20\03-2007 eXo Platform SAS .
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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.AuthenticationFailedException;
import javax.mail.Flags;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.search.AndTerm;
import javax.mail.search.BodyTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.FromStringTerm;
import javax.mail.search.NotTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.RecipientStringTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SentDateTerm;
import javax.mail.search.SubjectTerm;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.mail.connection.Connector;
import org.exoplatform.mail.connection.impl.ImapConnector;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.AccountData;
import org.exoplatform.mail.service.Attachment;
import org.exoplatform.mail.service.CheckMailJob;
import org.exoplatform.mail.service.CheckingInfo;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.Info;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.MailSetting;
import org.exoplatform.mail.service.MailUpdateStorageEventListener;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.MessageFilter;
import org.exoplatform.mail.service.MessagePageList;
import org.exoplatform.mail.service.MimeMessageParser;
import org.exoplatform.mail.service.ServerConfiguration;
import org.exoplatform.mail.service.SpamFilter;
import org.exoplatform.mail.service.StatusInfo;
import org.exoplatform.mail.service.Tag;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.scheduler.JobInfo;
import org.exoplatform.services.scheduler.JobSchedulerService;
import org.exoplatform.services.scheduler.PeriodInfo;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;
import org.exoplatform.ws.frameworks.json.impl.JsonException;
import org.exoplatform.ws.frameworks.json.impl.JsonGeneratorImpl;
import org.exoplatform.ws.frameworks.json.value.JsonValue;
import org.picocontainer.Startable;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.pop3.POP3Store;
import com.sun.mail.smtp.SMTPSendFailedException;
import com.sun.mail.smtp.SMTPTransport;

/**
 * Created by The eXo Platform SARL Author : Tuan Nguyen
 * tuan.nguyen@exoplatform.com Jun 23, 2007
 */
public class MailServiceImpl implements MailService, Startable {

  private static final Log          logger = LogFactory.getLog(MailServiceImpl.class);

  private JCRDataStorage            storage_;

  private EMLImportExport           emlImportExport_;

  private Map<String, CheckingInfo> checkingLog_;

  private JobSchedulerService       schedulerService_;
  private ContinuationService continuationService_;

  public MailServiceImpl(NodeHierarchyCreator nodeHierarchyCreator,
                         JobSchedulerService schedulerService,
                         RepositoryService reposervice) throws Exception {
    storage_ = new JCRDataStorage(nodeHierarchyCreator, reposervice);
    emlImportExport_ = new EMLImportExport(storage_);
    checkingLog_ = new HashMap<String, CheckingInfo>();
    this.schedulerService_ = schedulerService;
  }

  public String getMailHierarchyNode() throws Exception {
    return storage_.getMailHierarchyNode();

  }

  public void removeCheckingInfo(String userName, String accountId) throws Exception {
    String key = userName + ":" + accountId;
    checkingLog_.remove(key);
  }

  public CheckingInfo getCheckingInfo(String userName, String accountId) {
    String key = userName + ":" + accountId;
    CheckingInfo info = checkingLog_.get(key);
    return info;
  }

  /**
   * @param username
   * @return
   * @throws Exception
   */
  public List<Account> getAccounts(String userName) throws Exception {
    return storage_.getAccounts(userName);
  }

  public Account getAccountById(String userName, String id) throws Exception {
    return storage_.getAccountById(userName, id);
  }

  public void saveAccount(String userName, Account account, boolean isNew) throws Exception {
    storage_.saveAccount(userName, account, isNew);
  }

  public void updateAccount(String userName, Account account) throws Exception {
    saveAccount(userName, account, false);
  }

  public void updateErrorAccount(String userName, Account account) throws Exception {
    saveAccount(userName, account, false);
    String accountId = account.getId();
    CheckingInfo info = getCheckingInfo(userName, accountId);
    String key = userName + ":" + accountId;
    if (info == null) {
      checkingLog_.put(key, new CheckingInfo());
    }

    IMAPStore store = openIMAPConnection(userName, account, info);
    if (store != null) {
      synchImapFolders(userName, accountId);
      removeCheckingInfo(userName, accountId);
    }
  }

  public void removeAccount(String userName, String accountId) throws Exception {
    stopAllJobs(userName, accountId);
    storage_.removeAccount(userName, accountId);
  }

  public Folder getFolder(String userName, String accountId, String folderId) throws Exception {
    return storage_.getFolder(userName, accountId, folderId);
  }

  public String getFolderParentId(String userName, String accountId, String folderId) throws Exception {
    return storage_.getFolderParentId(userName, accountId, folderId);
  }

  public boolean isExistFolder(String userName, String accountId, String parentId, String folderName) throws Exception {
    return storage_.isExistFolder(userName, accountId, parentId, folderName);
  }

  public void saveFolder(String userName, String accountId, Folder folder) throws Exception {
    saveFolder(userName, accountId, folder, true);
  }

  private void saveFolder(String userName, String accountId, Folder folder, boolean b) throws Exception {
    Account account = getAccountById(userName, accountId);
    if (account.getProtocol().equalsIgnoreCase(Utils.IMAP) && folder.isPersonalFolder() && b) {
      IMAPFolder imapFolder = null;
      try {
        Connector connector = new ImapConnector(account);
        imapFolder = (IMAPFolder) connector.createFolder(folder);
        saveFolder(userName, accountId, null, imapFolder);
      } catch (Exception e) {
      	e.printStackTrace();
        return;
      } finally {
        if (imapFolder != null && imapFolder.isOpen()) {
          imapFolder.close(true);
        }
      }
    } else {
      storage_.saveFolder(userName, accountId, folder);
    }
  }

  public void saveFolder(String userName, String accountId, String parentId, Folder folder) throws Exception {
    saveFolder(userName, accountId, parentId, folder, true);
  }

  private void saveFolder(String userName,
                          String accountId,
                          String parentId,
                          Folder folder,
                          boolean b) throws Exception {
    Account account = getAccountById(userName, accountId);
    if (account.getProtocol().equalsIgnoreCase(Utils.IMAP) && folder.isPersonalFolder() && b) {
      try {
        Folder parentFolder = getFolder(userName, accountId, parentId);
        Connector connector = new ImapConnector(account);
        IMAPFolder imapFolder = (IMAPFolder) connector.createFolder(parentFolder, folder);
        if (imapFolder != null) {
          saveFolder(userName, accountId, parentFolder, imapFolder);
        }
      } catch (Exception e) {
        return;
      }
    } else {
      storage_.saveFolder(userName, accountId, parentId, folder);
    }
  }

  private void saveFolder(String userName,
                          String accountId,
                          Folder parentFolder,
                          javax.mail.Folder serverFolder) throws Exception {
    String folderId;
    Folder folder;
    if (serverFolder.getType() != javax.mail.Folder.HOLDS_FOLDERS) {
      folderId = Utils.generateFID(accountId,
                                   String.valueOf(((IMAPFolder) serverFolder).getUIDValidity()),
                                   true);
    } else {
      folderId = Utils.escapeIllegalJcrChars(serverFolder.getName());
    }
    folder = storage_.getFolder(userName, accountId, folderId);
    if (folder == null) {
      folder = new Folder();
      folder.setId(folderId);
      folder.setName(serverFolder.getName());
      folder.setURLName(serverFolder.getURLName().toString());
      folder.setNumberOfUnreadMessage(0);
      folder.setTotalMessage(0);
      folder.setPersonalFolder(true);
      folder.setType(serverFolder.getType());
      try {
        if (parentFolder == null) {
          storage_.saveFolder(userName, accountId, folder);
        } else {
          storage_.saveFolder(userName, accountId, parentFolder.getId(), folder);
        }
      } catch (Exception e) {
      }
    }
  }

  public void renameFolder(String userName, String accountId, String newName, String folderId) throws Exception {
    Account account = getAccountById(userName, accountId);
    Folder folder = this.getFolder(userName, accountId, folderId);
    if (account.getProtocol().equalsIgnoreCase(Utils.IMAP) && folder.isPersonalFolder()) {
      try {
        Connector connector = new ImapConnector(account);
        folder = connector.renameFolder(newName, folder);
      } catch (Exception e) {
        return;
      }
    }
    if (folder != null)
      storage_.renameFolder(userName, accountId, newName, folder);
  }

  private void deleteLocalFolder(String userName, String accountId, String folderId) throws Exception {
    storage_.removeUserFolder(userName, accountId, folderId);
  }

  public void removeUserFolder(String userName, String accountId, String folderId) throws Exception {
    Account account = getAccountById(userName, accountId);
    Folder folder = this.getFolder(userName, accountId, folderId);
    boolean success = true;
    if (account.getProtocol().equalsIgnoreCase(Utils.IMAP) && folder.isPersonalFolder()) {
      try {
        Connector connector = new ImapConnector(account);
        success = connector.deleteFolder(folder);
      } catch (Exception e) {
        return;
      }
    }
    if (success)
      storage_.removeUserFolder(userName, accountId, folderId);
  }

  public List<MessageFilter> getFilters(String userName, String accountId) throws Exception {
    return storage_.getFilters(userName, accountId);
  }

  public MessageFilter getFilterById(String userName, String accountId, String filterId) throws Exception {
    return storage_.getFilterById(userName, accountId, filterId);
  }

  public void saveFilter(String userName, String accountId, MessageFilter filter, boolean applyAll) throws Exception {
    String toFolder = filter.getApplyFolder();
    List<Message> messageList = storage_.getMessagePageList(userName, filter).getAll(userName);
    String fromFolder = null;
    if (messageList.size() > 0) {
      fromFolder = messageList.get(0).getFolders()[0];
    }
    moveMessages(userName, accountId, messageList, fromFolder, toFolder);

    storage_.saveFilter(userName, accountId, filter, applyAll);
  }

  public void removeFilter(String userName, String accountId, String filterId) throws Exception {
    storage_.removeFilter(userName, accountId, filterId);
  }

  public Message getMessageById(String userName, String accountId, String msgId) throws Exception {
    return storage_.getMessageById(userName, accountId, msgId);
  }

  public void removeMessage(String userName, String accountId, Message message) throws Exception {
    storage_.removeMessage(userName, accountId, message);
  }

  public void removeMessages(String userName,
                             String accountId,
                             List<Message> messages,
                             boolean moveReference) throws Exception {
    storage_.removeMessages(userName, accountId, messages, moveReference);
  }

  public void moveMessages(String userName,
                           String accountId,
                           List<Message> msgList,
                           String currentFolderId,
                           String destFolderId) throws Exception {
    Account account = getAccountById(userName, accountId);
    Folder currentFolder = this.getFolder(userName, accountId, currentFolderId);
    Folder destFolder = this.getFolder(userName, accountId, destFolderId);
    boolean success = true;
    if (account.getProtocol().equalsIgnoreCase(Utils.IMAP)) {
      try {
        Connector connector = new ImapConnector(account);
        msgList = connector.moveMessage(msgList, currentFolder, destFolder);
        if (msgList == null)
          success = false;
      } catch (Exception e) {
        if(logger.isDebugEnabled()) logger.debug("\n\n move message error " + e.getMessage());
      }
    }else if (account.getProtocol().equalsIgnoreCase(Utils.POP3)) {
      //move only on Local
    }
    if (success)
      storage_.moveMessages(userName, accountId, msgList, currentFolderId, destFolderId);
  }

  public void moveMessages(String userName,
                           String accountId,
                           List<Message> msgList,
                           String currentFolderId,
                           String destFolderId,
                           boolean updateReference) throws Exception {
    Account account = getAccountById(userName, accountId);
    Folder currentFolder = this.getFolder(userName, accountId, currentFolderId);
    Folder destFolder = this.getFolder(userName, accountId, destFolderId);
    boolean success = true;
    if (account.getProtocol().equalsIgnoreCase(Utils.IMAP)) {
      try {
        Connector connector = new ImapConnector(account);
        msgList = connector.moveMessage(msgList, currentFolder, destFolder);
        if (msgList == null)
          success = false;
      } catch (Exception e) {
        return;
      }
    }
    if (success)
      storage_.moveMessages(userName,
                            accountId,
                            msgList,
                            currentFolderId,
                            destFolderId,
                            updateReference);
  }

  public void moveMessage(String userName,
                          String accountId,
                          Message msg,
                          String currentFolderId,
                          String destFolderId) throws Exception {
    Account account = getAccountById(userName, accountId);
    Folder currentFolder = this.getFolder(userName, accountId, currentFolderId);
    Folder destFolder = this.getFolder(userName, accountId, destFolderId);
    boolean success = true;
    if (account.getProtocol().equalsIgnoreCase(Utils.IMAP)) {
      try {
        List<Message> msgList = new ArrayList<Message>();
        Connector connector = new ImapConnector(account);
        msgList.add(msg);
        msgList = connector.moveMessage(msgList, currentFolder, destFolder);
        if (msgList == null) {
          success = false;
        } else {
          msg = msgList.get(0);
        }
      } catch (Exception e) {
        return;
      }
    }
    if (success)
      moveMessage(userName, accountId, msg, currentFolderId, destFolderId, true);
  }

  public void moveMessage(String userName,
                          String accountId,
                          Message msg,
                          String currentFolderId,
                          String destFolderId,
                          boolean updateReference) throws Exception {
    Account account = getAccountById(userName, accountId);
    Folder currentFolder = this.getFolder(userName, accountId, currentFolderId);
    Folder destFolder = this.getFolder(userName, accountId, destFolderId);
    boolean success = true;
    if (account.getProtocol().equalsIgnoreCase(Utils.IMAP)) {
      try {
        Connector connector = new ImapConnector(account);
        List<Message> msgList = new ArrayList<Message>();
        msgList.add(msg);
        msgList = connector.moveMessage(msgList, currentFolder, destFolder);
        if (msgList == null)
          success = false;
        else
          msg = msgList.get(0);
      } catch (Exception e) {
        return;
      }
    }
    if (success)
      storage_.moveMessage(userName, accountId, msg, currentFolderId, destFolderId, updateReference);
  }

  public MessagePageList getMessagePageList(String userName, MessageFilter filter) throws Exception {
    return storage_.getMessagePageList(userName, filter);
  }

  public void saveMessage(String userName,
                          Account account,
                          String targetMsgPath,
                          Message message,
                          boolean isNew) throws Exception {
    List<Message> msgList = new ArrayList<Message>();
    msgList.add(message);
    String folderId = message.getFolders()[0];
    Folder destFolder = null;
    if (folderId != null) {
      destFolder = getFolder(userName, account.getId(), folderId);
    }
    if (destFolder != null && account.getProtocol().equalsIgnoreCase(Utils.IMAP)) {
      Connector connector = new ImapConnector(account);
      connector.createMessage(msgList, destFolder);
    }
    storage_.saveMessage(userName, account.getId(), targetMsgPath, message, isNew);
  }
  
  public void saveMessage(String userName,
                          String accountId,
                          String targetMsgPath,
                          Message message,
                          boolean isNew) throws Exception {
    Account account = getAccountById(userName, accountId);
    saveMessage(userName, account, targetMsgPath, message, isNew);
  }

  public List<Message> getMessagesByTag(String userName, String accountId, String tagId) throws Exception {
    MessageFilter filter = new MessageFilter("Tag");
    filter.setAccountId(accountId);
    filter.setFolder(new String[] { tagId });
    return getMessages(userName, filter);
  }

  public List<Message> getMessagesByFolder(String userName, String accountId, String folderId) throws Exception {
    MessageFilter filter = new MessageFilter("Folder");
    filter.setAccountId(accountId);
    filter.setFolder(new String[] { folderId });
    List<Message> list = getMessages(userName, filter);
    return list;
  }

  public List<Message> getMessages(String userName, MessageFilter filter) throws Exception {
    return storage_.getMessages(userName, filter);
  }

  public void saveMessage(String userName, String accountId, Message message, boolean isNew) throws Exception {
    storage_.saveMessage(userName, accountId, message, isNew);
  }

  public Message sendMessage(String userName, String accId, Message message) throws Exception {
    Account acc = getAccountById(userName, accId);
    return sendMessage(userName, acc, message);
  }

  public Message sendMessage(String userName, Account acc, Message message) throws Exception {
    String smtpUser = acc.getIncomingUser();
    String outgoingHost = acc.getOutgoingHost();
    String outgoingPort = acc.getOutgoingPort();
    String isSSl = acc.getServerProperties().get(Utils.SVR_OUTGOING_SSL);
    boolean isSMTPAuth = acc.isOutgoingAuthentication();
    Properties props = System.getProperties();
    props.put(Utils.SVR_SMTP_USER, smtpUser);
    props.put(Utils.SVR_SMTP_HOST, outgoingHost);
    props.put(Utils.SVR_SMTP_PORT, outgoingPort);
    props.put("mail.smtp.dsn.notify", "SUCCESS,FAILURE ORCPT=rfc822;" + acc.getEmailAddress());
    props.put("mail.smtp.dsn.ret", "FULL");
    props.put("mail.smtp.socketFactory.port", outgoingPort);
    props.put(Utils.SVR_SMTP_SOCKET_FACTORY_FALLBACK, "true");
    props.put("mail.smtp.connectiontimeout", "0");
    props.put("mail.smtp.timeout", "0");
    String socketFactoryClass = "javax.net.SocketFactory";
    if (Boolean.valueOf(isSSl)) {
      socketFactoryClass = Utils.SSL_FACTORY;
      props.put(Utils.SVR_SMTP_STARTTLS_ENABLE, "true");
      props.put("mail.smtp.ssl.protocols", "SSLv3 TLSv1");
    }
    props.put(Utils.SVR_SMTP_SOCKET_FACTORY_CLASS, socketFactoryClass);

    if (isSMTPAuth) {
      props.put(Utils.SVR_SMTP_AUTH, "true");
    } else {
      props.put(Utils.SVR_SMTP_AUTH, "false");
    }

    Session session = Session.getDefaultInstance(props, null);
    logger.debug(" #### Sending email ... ");
    SMTPTransport transport = (SMTPTransport) session.getTransport(Utils.SVR_SMTP);

    try {
      if (!isSMTPAuth) {
        transport.connect();
      } else if (acc.useIncomingSettingForOutgoingAuthent()) {
        transport.connect(outgoingHost,
                          Integer.parseInt(outgoingPort),
                          smtpUser,
                          acc.getIncomingPassword());
      } else {
        transport.connect(outgoingHost,
                          Integer.parseInt(outgoingPort),
                          acc.getOutgoingUserName(),
                          acc.getOutgoingPassword());
      }
    } catch (Exception ex) {
      logger.debug("#### Can not connect to smtp server ...");
      throw ex;
    }
    Message msg = send(session, transport, message);
    transport.close();

    return msg;
  }

  public Message sendMessage(String userName, Message message) throws Exception {
    return sendMessage(userName, message.getAccountId(), message);
  }

  public void sendMessage(Message message) throws Exception {
    List<Message> msgList = new ArrayList<Message>();
    msgList.add(message);
    sendMessages(msgList, message.getServerConfiguration());
  }

  public void sendMessages(List<Message> msgList, ServerConfiguration serverConfig) throws Exception {
    Properties props = System.getProperties();
    props.put(Utils.SVR_INCOMING_USERNAME, serverConfig.getUserName());
    props.put(Utils.SVR_INCOMING_PASSWORD, serverConfig.getPassword());
    props.put(Utils.SVR_SMTP_USER, serverConfig.getUserName());
    props.put(Utils.SVR_SMTP_HOST, serverConfig.getOutgoingHost());
    props.put(Utils.SVR_SMTP_PORT, serverConfig.getOutgoingPort());
    boolean isSMTPAuth = serverConfig.isOutgoingAuthentication();
    if (isSMTPAuth) {
      props.put(Utils.SVR_SMTP_AUTH, "true");
    } else {
      props.put(Utils.SVR_SMTP_AUTH, "false");
    }
    props.put(Utils.SVR_SMTP_SOCKET_FACTORY_PORT, serverConfig.getOutgoingPort());
    if (serverConfig.isOutgoingSsl()) {
      props.put(Utils.SVR_INCOMING_SSL, String.valueOf(serverConfig.isSsl()));
      props.put(Utils.SVR_SMTP_STARTTLS_ENABLE, "true");
      props.put(Utils.SVR_SMTP_SOCKET_FACTORY_CLASS, "javax.net.ssl.SSLSocketFactory");
    }
    props.put(Utils.SVR_SMTP_SOCKET_FACTORY_FALLBACK, "false");
    Session session = Session.getDefaultInstance(props, null);
    Transport transport = session.getTransport(Utils.SVR_SMTP);

    try {
      if (!isSMTPAuth) {
        transport.connect();
      } else {
        transport.connect(serverConfig.getOutgoingHost(),
                          serverConfig.getUserName(),
                          serverConfig.getPassword());
      }
    } catch (Exception e) {
      try {
        transport.connect();
      } catch (Exception ex) {
        logger.debug("#### Can not connect to smtp server ...");
        return;
      }
    }
    logger.debug(" #### Sending email ... ");
    int i = 0;
    for (Message msg : msgList) {
      msg.setServerConfiguration(serverConfig);
      try {
        send(session, transport, msg);
        i++;
      } catch (Exception e) {
        logger.error(" #### Info : send fail at message " + i + " \n");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        StringBuffer sb = sw.getBuffer();
        logger.error(sb.toString());
      }
    }
    logger.debug(" #### Info : Sent " + i + " email(s)");
    transport.close();
  }

  @SuppressWarnings("unchecked")
  private Message send(Session session, Transport transport, Message message) throws Exception {
    MimeMessage mimeMessage = new MimeMessage(session);
    String status = "";
    InternetAddress addressFrom;
    mimeMessage.setHeader("Message-ID", message.getId());
    mimeMessage.setHeader("Content-Transfer-Encoding", "utf-8");

    if (message.getFrom() != null)
      addressFrom = new InternetAddress(message.getFrom());
    else
      addressFrom = new InternetAddress(session.getProperties().getProperty(Utils.SVR_SMTP_USER));

    mimeMessage.setFrom(addressFrom);
    if (message.getMessageTo() != null)
      mimeMessage.setRecipients(javax.mail.Message.RecipientType.TO,
                                InternetAddress.parse(message.getMessageTo()));

    if (message.getMessageCc() != null)
      mimeMessage.setRecipients(javax.mail.Message.RecipientType.CC,
                                InternetAddress.parse(message.getMessageCc(), true));

    if (message.getMessageBcc() != null)
      mimeMessage.setRecipients(javax.mail.Message.RecipientType.BCC,
                                InternetAddress.parse(message.getMessageBcc(), false));

    if (message.getReplyTo() != null)
      mimeMessage.setReplyTo(Utils.getInternetAddress(message.getReplyTo()));

    mimeMessage.setSubject(message.getSubject(), "UTF-8");
    mimeMessage.setSentDate(message.getSendDate());

    List<Attachment> attachList = message.getAttachments();
    if (attachList != null && attachList.size() > 0) {
      MimeBodyPart contentPartRoot = new MimeBodyPart();
      if (message.getContentType() != null && message.getContentType().indexOf("text/plain") > -1)
        contentPartRoot.setContent(message.getMessageBody(), "text/plain; charset=utf-8");
      else
        contentPartRoot.setContent(message.getMessageBody(), "text/html; charset=utf-8");

      MimeMultipart multipPartContent = new MimeMultipart("alternative");
      MimeBodyPart mimeBodyPart1 = new MimeBodyPart();
      mimeBodyPart1.setContent(message.getMessageBody(), message.getContentType());
      multipPartContent.addBodyPart(mimeBodyPart1);

      for (Attachment att : attachList) {
        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        InputStream inputStream = att.getInputStream();
        String contentType = att.getMimeType();

        DataSource dataSource = new ByteArrayDataSource(inputStream, contentType);
        mimeBodyPart.setDataHandler(new DataHandler(dataSource));
        mimeBodyPart.setDisposition(Part.ATTACHMENT);
        String fileName = att.getName();
        if (fileName != null && fileName.length() > 0) {
          mimeBodyPart.setFileName(MimeUtility.encodeText(fileName, "utf-8", null));
        }
        multipPartContent.addBodyPart(mimeBodyPart);

      }
      mimeMessage.setContent(multipPartContent);
    } else {
      if (message.getContentType() != null && message.getContentType().indexOf("text/plain") > -1)
        mimeMessage.setContent(message.getMessageBody(), "text/plain; charset=utf-8");
      else
        mimeMessage.setContent(message.getMessageBody(), "text/html; charset=utf-8");
    }

    if (message.isReturnReceipt()) {
      mimeMessage.setHeader("Disposition-Notification-To", message.getReplyTo());
    }

    mimeMessage.setHeader("X-Priority", String.valueOf(message.getPriority()));
    String priority = "Normal";
    if (message.getPriority() == Utils.PRIORITY_HIGH) {
      priority = "High";
    } else if (message.getPriority() == Utils.PRIORITY_LOW) {
      priority = "Low";
    }
    if (message.getPriority() != 0)
      mimeMessage.setHeader("Importance", priority);

    Iterator<String> iter = message.getHeaders().keySet().iterator();
    while (iter.hasNext()) {
      String key = iter.next().toString();
      mimeMessage.setHeader(key, message.getHeaders().get(key));
    }
    try {
      mimeMessage.saveChanges();
    } catch (Exception ex) {
    }
    try {
      transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
      message.setId(MimeMessageParser.getMessageId(mimeMessage));
      Enumeration enu = mimeMessage.getAllHeaders();
      while (enu.hasMoreElements()) {
        Header header = (Header) enu.nextElement();
        message.setHeader(header.getName(), header.getValue());
      }
      status = "Mail Delivered !";
    } catch (AddressException e) {
      status = "There was an error parsing the addresses. Sending Failed !" + e.getMessage();
      throw e;
    } catch (AuthenticationFailedException e) {
      status = "The Username or Password may be wrong. Sending Failed !" + e.getMessage();
      throw e;
    } catch (SMTPSendFailedException e) {
      status = "Sorry, There was an error sending the message. Sending Failed !" + e.getMessage();
      throw e;
    } catch (MessagingException e) {
      status = "There was an unexpected error. Sending Failed ! " + e.getMessage();
      throw e;
    } catch (Exception e) {
      status = "There was an unexpected error. Sending Falied !" + e.getMessage();
      throw e;
    } finally {
      logger.debug(" #### Info : " + status);
    }
    logger.debug(" #### Info : " + status);

    return message;
  }

  public void checkMail(String userName, String accountId) throws Exception {
    JobDetail job = loadCheckmailJob(userName, accountId);
    
    /*JobInfo info = CheckMailJob.getJobInfo(userName, accountId);
    JobDetail oldJob = schedulerService_.getJob(info);
    if (oldJob == null) {
      // trigger now
      //executeJob(job);
    } else {
    }*/
  }

  public void checkMail(String userName, String accountId, String folderId) throws Exception {
    if (Utils.isEmptyField(folderId))
      checkMail(userName, accountId);
    else {
      JobDetail job = loadCheckmailJob(userName, accountId, folderId);
      
      /*JobInfo info = CheckMailJob.getJobInfo(userName, accountId);
      JobDetail oldJob = schedulerService_.getJob(info);
      if (oldJob == null) {
        // trigger now
//        executeJob(job);
      } else {
        logger.warn("client [ "
                    + userName
                    + ":"
                    + accountId
                    + " ] has requested a checking mail job, but the system can not serve because old job hasn't stopped yet!!!");
      }*/
    }
  }

  private void executeJob(JobDetail job) { 
    if (job != null) {
      try {
        schedulerService_.executeJob(job.getName(), job.getGroup(), job.getJobDataMap());
      } catch (Exception e) {
        logger.error("Failed to execute job " + job.getName(), e);
      }
    }
  }

  public void stopCheckMail(String userName, String accountId) {
    CheckingInfo checkingInfo = getCheckingInfo(userName, accountId);
    if (checkingInfo != null) {
      logger.info(" user [ " + userName + " ] request to stop checking emails");
      checkingInfo.setRequestStop(true);
      updateCheckingMailStatusByCometd(userName, accountId, checkingInfo);
      
    }
  }

  public void stopAllJobs(String userName, String accountId) throws Exception {
    JobInfo info = CheckMailJob.getJobInfo(userName, accountId);
    stopCheckMail(userName, accountId);
    schedulerService_.removeJob(info);
  }

  private JobDetail loadCheckmailJob(String userName, String accountId) throws Exception {
    return loadCheckmailJob(userName, accountId, "");
  }

  /**
   * Load or register the CheckMailJob against scheduler
   * 
   * @return
   * @throws Exception
   */
  private JobDetail loadCheckmailJob(String userName, String accountId, String folderId) throws Exception {
    JobInfo info = CheckMailJob.getJobInfo(userName, accountId);
    JobDetail job = findCheckmailJob(userName, accountId);
    if (job == null) {
      JobDataMap jobData = new JobDataMap();
      jobData.put(CheckMailJob.USERNAME, userName);
      jobData.put(CheckMailJob.ACCOUNTID, accountId);
      if (folderId != null && folderId.length() > 0)
        jobData.put(CheckMailJob.FOLDERID, folderId);
      PeriodInfo periodInfo = new PeriodInfo(null,
                                             null,
                                             0,
                                             24 * 60 * 60 * 1000);
      schedulerService_.addPeriodJob(info, periodInfo, jobData);
    } else {
      JobDetail activeJob = findActiveCheckmailJob(userName, accountId);
      if (activeJob == null) {
        executeJob(job);
      } else {
        logger.warn("job [ "
                    + job.getName()
                    + " ] still exist. Can not start new job!");
        job = new JobDetail(info.getJobName(), info.getGroupName(), info.getJob());
      }
    }
    return job;

  }

  private JobDetail findActiveCheckmailJob(String userName, String accountId) throws Exception {
    // TODO current implementation is inefficient
    // / Need to upgrade to 2.0.3 and use this instead :
    // schedulerService_.getJob(info)
    List list = schedulerService_.getAllExcutingJobs();
    for (Object obj : list) {
      JobExecutionContext jec = (JobExecutionContext) obj;
      JobDetail tmp = jec.getJobDetail();
      if (tmp.getName().equals(userName + ":" + accountId)) {
        return tmp;
      }
    }
    return null;
  }
  
  private JobDetail findCheckmailJob(String userName, String accountId) throws Exception {
    // TODO current implementation is inefficient
    // / Need to upgrade to 2.0.3 and use this instead :
    // schedulerService_.getJob(info)
    List<Object> list = schedulerService_.getAllJobs();
    for (Object obj : list) {
      JobDetail tmp = (JobDetail) obj;
      if (tmp.getName().equals(userName + ":" + accountId)) {
        return tmp;
      }
    }
    return null;
  }

  private LinkedHashMap<javax.mail.Message, List<String>> getMessages(LinkedHashMap<javax.mail.Message, List<String>> msgMap,
                                                                      javax.mail.Folder folder,
                                                                      boolean isImap,
                                                                      Date fromDate,
                                                                      Date toDate,
                                                                      List<MessageFilter> filters) throws Exception {
    javax.mail.Message[] messages;
    SearchTerm searchTerm = null;

    if (fromDate != null && toDate != null && fromDate.equals(toDate))
      return msgMap;

    if (!folder.isOpen())
      folder.open(javax.mail.Folder.READ_WRITE);
    if (fromDate == null && toDate == null) {
      messages = folder.getMessages();
    } else {
      searchTerm = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
      SearchTerm dateTerm = null;
      if (fromDate != null) {
        dateTerm = new SentDateTerm(ComparisonTerm.GT, fromDate);
      }
      if (toDate != null) {
        if (dateTerm != null) {
          dateTerm = new AndTerm(dateTerm, new SentDateTerm(ComparisonTerm.LE, toDate));
        } else {
          dateTerm = new SentDateTerm(ComparisonTerm.LE, toDate);
        }
      }
      if (!isImap)
        searchTerm = new OrTerm(searchTerm, dateTerm);
      else
        searchTerm = dateTerm;
      messages = folder.search(searchTerm);
    }

    boolean beforeTime = false;
    boolean betweenTime = false;
    List<String> filterList;
    int filteredMsgNumber = 0;
    int getFrom = 0;

    SearchTerm st;
    javax.mail.Message[] filteredMsg;
    javax.mail.Message msg;

    for (MessageFilter filter : filters) {
      beforeTime = false;
      betweenTime = false;
      st = getSearchTerm(searchTerm, filter);
      filteredMsg = folder.search(st);
      filteredMsgNumber = filteredMsg.length;

      if (filteredMsgNumber > 0) {
        getFrom = 0;
        if (fromDate != null) {
          for (int k = 0; k < filteredMsgNumber; k++) {
            if (MimeMessageParser.getReceivedDate(filteredMsg[k]).getTime().before(fromDate)) {
              getFrom++;
            } else {
              break;
            }
          }
        }

        for (int k = filteredMsgNumber - 1; k >= 0; k--) {
          msg = filteredMsg[k];
          if (msgMap.containsKey(msg)) {
            filterList = msgMap.get(msg);
          } else {
            filterList = new ArrayList<String>();
          }

          if (filterList == null)
            filterList = new ArrayList<String>();

          if (!filterList.contains(filter.getId()))
            filterList.add(filter.getId());

          if (fromDate != null && toDate != null) {
            if (betweenTime
                || (!(isImap && !MimeMessageParser.getReceivedDate(msg).getTime().before(toDate)))) {
              betweenTime = true;
              if (!(isImap && !(k > getFrom))) {
                msgMap.put(msg, filterList);
              } else {
                betweenTime = false;
              }
            }
          } else if (fromDate != null) {
            if (!(isImap && !(k > getFrom))) {
              msgMap.put(msg, filterList);
            }
          } else if (toDate != null) {
            if (beforeTime
                || !(isImap && !MimeMessageParser.getReceivedDate(msg).getTime().before(toDate))) {
              beforeTime = true;
              msgMap.put(msg, filterList);
            }
          } else {
            msgMap.put(msg, filterList);
          }
        }
      }
    }

    if (messages.length > 0) {
      beforeTime = false;
      betweenTime = false;
      getFrom = 0;
      Date receivedDate;
      if (fromDate != null) {
        for (int l = 0; l < messages.length; l++) {
          receivedDate = MimeMessageParser.getReceivedDate(messages[l]).getTime();
          if (receivedDate.before(fromDate) || receivedDate.equals(fromDate)) {
            getFrom++;
          } else {
            break;
          }
        }
      }

      for (int l = messages.length; l > 0; l--) {
        msg = messages[l - 1];
        if (!msgMap.containsKey(msg)) {
          if (fromDate != null && toDate != null) {
            if (betweenTime
                || (!(isImap && !MimeMessageParser.getReceivedDate(msg).getTime().before(toDate)))) {
              betweenTime = true;
              if (!(isImap && !(l > getFrom))) {
                msgMap.put(msg, null);
              } else {
                betweenTime = false;
              }
            }
          } else if (fromDate != null) {
            if (!(isImap && !(l > getFrom))) {
              msgMap.put(msg, null);
            }
          } else if (toDate != null) {
            if (beforeTime
                || !(isImap && !MimeMessageParser.getReceivedDate(msg).getTime().before(toDate))) {
              beforeTime = true;
              msgMap.put(msg, null);
            }
          } else {
            msgMap.put(msg, null);
          }
        } else {
          List<String> temp = msgMap.get(msg);
          msgMap.remove(msg);
          msgMap.put(msg, temp);
        }
      }
    }
    return msgMap;
  }

  public void synchImapFolders(String userName, String accountId) throws Exception {
    IMAPStore store = null;
    CheckingInfo info = getCheckingInfo(userName, accountId);
    try {
      Account account = getAccountById(userName, accountId);
      store = openIMAPConnection(userName, account, info);
      if (store != null) {
      	String key = userName + ":" + accountId;
    		if(info == null) info = new CheckingInfo();
      	info.setStatusCode(CheckingInfo.START_SYNC_FOLDER);
      	checkingLog_.put(key, info);
      	synchImapFolders(userName, accountId, null, store.getDefaultFolder().list());
        info.setRequestStop(true);
        info.setStatusCode(CheckingInfo.FINISH_SYNC_FOLDER);
        info.setSyncFolderStatus(CheckingInfo.FINISH_SYNC_FOLDER);
        checkingLog_.put(key, info);
      }
    } catch (Exception e) {
			e.printStackTrace();
		} finally {
      if (store != null && store.isConnected()) {
        store.close();
      }
      if (info != null)
        info.setStatusCode(CheckingInfo.FINISH_SYNC_FOLDER);
    }
  }

  private List<javax.mail.Folder> synchImapFolders(String userName,
                                                   String accountId,
                                                   Folder parentFolder,
                                                   javax.mail.Folder[] folders) throws Exception {
  	
  	
  	
    List<javax.mail.Folder> folderList = new ArrayList<javax.mail.Folder>();
    List<String> serverFolderId = new ArrayList<String>();
    String folderId, folderName;

    for (javax.mail.Folder fd : folders) {

      folderName = fd.getName();
      if (parentFolder == null
          && (folderName.equalsIgnoreCase(Utils.FD_DRAFTS)
              || folderName.equalsIgnoreCase(Utils.FD_SENT) || folderName.equalsIgnoreCase(Utils.FD_SPAM))
              || folderName.equalsIgnoreCase(Utils.FD_TRASH)) {
        continue;
      }

      int folderType = fd.getType();
      if (!folderName.equalsIgnoreCase(Utils.FD_INBOX)) {
        if (folderType != javax.mail.Folder.HOLDS_FOLDERS) {
          folderId = Utils.generateFID(accountId,
                                       String.valueOf(((IMAPFolder) fd).getUIDValidity()),
                                       true);
        } else {
          folderId = Utils.escapeIllegalJcrChars(folderName);
        }

        serverFolderId.add(folderId);
        Folder folder = storage_.getFolder(userName, accountId, folderId);
        
        if (folder == null) {
          folder = new Folder();
          folder.setId(folderId);
          folder.setName(folderName);
          folder.setURLName(fd.getURLName().toString());
          folder.setNumberOfUnreadMessage(0);
          folder.setTotalMessage(0);
          folder.setPersonalFolder(true);
          folder.setType(folderType);
          try {
            if (parentFolder == null) {
              storage_.saveFolder(userName, accountId, folder);
            } else {
              storage_.saveFolder(userName, accountId, parentFolder.getId(), folder);
            }
          } catch (Exception e) {
            logger.warn(e);
          }

          // update available one
        } else {
        	if (!folder.getName().equalsIgnoreCase(folderName)) {
        		folder.setName(folderName);
        		folder.setURLName(fd.getURLName().toString());
        		saveFolder(userName, accountId, folder, false);
        	}
        } 

        folderList.add(fd);
        if ((folderType == 2) || (fd.list().length > 0)) {
          folderList.addAll(synchImapFolders(userName, accountId, getFolder(userName,
                                                                            accountId,
                                                                            folderId), fd.list()));
        }
      } else {
        Folder inbox = getFolder(userName, accountId, Utils.generateFID(accountId,
                                                                        Utils.FD_INBOX,
                                                                        false));
        inbox.setURLName(fd.getURLName().toString());
        saveFolder(userName, accountId, inbox, false);
      }
    }

    List<Folder> localFolders = null;
    if (parentFolder == null) {
      localFolders = getFolders(userName, accountId, true);
    } else {
      localFolders = getSubFolders(userName, accountId, parentFolder.getPath());
    }
    if (localFolders != null) {
      for (Folder f : localFolders) {
        if (!serverFolderId.contains(f.getId())) {
          deleteLocalFolder(userName, accountId, f.getId());
        }
      }
    }
    return folderList;
  }

  public IMAPStore openIMAPConnection(String userName, Account account) {
    return openIMAPConnection(userName, account, null);
  }

  public IMAPStore openIMAPConnection(String userName, Account account, CheckingInfo info) {
    try {
      logger.debug(" #### Getting mail from " + account.getIncomingHost() + " ... !");
      Properties props = System.getProperties();
      // this line fix for base64 encode problem with corrupted
      // attachments
      props.setProperty("mail.mime.base64.ignoreerrors", "true");

      String socketFactoryClass = "javax.net.SocketFactory";
      if (account.isIncomingSsl()) {
        socketFactoryClass = Utils.SSL_FACTORY;
      }
      props.setProperty("mail.imap.socketFactory.fallback", "false");
      props.setProperty("mail.imap.socketFactory.class", socketFactoryClass);

      Session session = Session.getDefaultInstance(props, null);
      IMAPStore imapStore = (IMAPStore) session.getStore("imap");
      try {
        imapStore.connect(account.getIncomingHost(),
                          Integer.valueOf(account.getIncomingPort()),
                          account.getIncomingUser(),
                          account.getIncomingPassword());
      } catch (AuthenticationFailedException e) {
        if (!account.isSavePassword()) {
          account.setIncomingPassword("");
          updateAccount(userName, account);
          logger.debug("Exception while connecting to server : " + e.getMessage());
          logger.error("Exception while connecting to server : " + e.getMessage());
        }
        if (info != null) {
//          info.setStatusMsg("msg-retry-password");
          info.setStatusCode(CheckingInfo.RETRY_PASSWORD);
          updateCheckingMailStatusByCometd(userName, account.getId(), info);
        }
        return null;
      } catch (MessagingException e) {
        logger.debug("Exception while connecting to server : " + e.getMessage());
        if (info != null) {
//          info.setStatusMsg("Connecting failed. Please check server configuration.");
          info.setStatusCode(CheckingInfo.CONNECTION_FAILURE);
          updateCheckingMailStatusByCometd(userName, account.getId(), info);
        }
        return null;
      } catch (IllegalStateException e) {
        logger.error("Exception while connecting to server", e);
        return null;
      } catch (Exception e) {
        logger.debug("Exception while connecting to server : " + e.getMessage());
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        StringBuffer sb = sw.getBuffer();
        logger.error(sb.toString());
        if (info != null) {
//          info.setStatusMsg("There was an unexpected error. Connecting failed.");
          info.setStatusCode(CheckingInfo.CONNECTION_FAILURE);
          updateCheckingMailStatusByCometd(userName, account.getId(), info);
        }
        return null;
      }
      return imapStore;
    } catch (Exception ex) {
      logger.error("Exception while connecting to server", ex);
      return null;
    }
  }

  public POP3Store openPOPConnection(String userName, Account account, CheckingInfo info) {
    try {
      logger.debug(" #### Getting mail from " + account.getIncomingHost() + " ... !");
      if (info != null) {
        info.setStatusCode(CheckingInfo.START_CHECKMAIL_STATUS);
        updateCheckingMailStatusByCometd(userName, account.getId(), info);
      }
      Properties props = System.getProperties();
      props.setProperty("mail.mime.base64.ignoreerrors", "true");

      props.put("mail.pop3.host", account.getIncomingHost());
      props.put("mail.pop3.user", account.getIncomingUser());
      props.put("mail.pop3.port", account.getIncomingPort());
      props.put("mail.pop3.starttls.enable", "true");
      props.put("mail.pop3.auth", "true");
      props.put("mail.pop3.socketFactory.port", account.getIncomingPort());
      props.put("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
      props.put("mail.pop3.socketFactory.fallback", "false");
      Session session = Session.getDefaultInstance(props, null);
      POP3Store pop3Store = (POP3Store) session.getStore("pop3");
      try {
        pop3Store.connect(account.getIncomingHost(),
                          Integer.valueOf(account.getIncomingPort()),
                          account.getIncomingUser(),
                          account.getIncomingPassword());
      } catch (AuthenticationFailedException e) {
        if (!account.isSavePassword()) {
          account.setIncomingPassword("");
          updateAccount(userName, account);
          logger.debug("Exception while connecting to server : " + e.getMessage());
          logger.error("Exception while connecting to server : " + e.getMessage());
        }
        if (info != null) {
//          info.setStatusMsg("msg-retry-password");
          info.setStatusCode(CheckingInfo.RETRY_PASSWORD);
          updateCheckingMailStatusByCometd(userName, account.getId(), info);
        }
        return null;
      } catch (MessagingException e) {
        e.printStackTrace();
        logger.debug("Exception while connecting to server : " + e.getMessage());
        if (info != null) {
//          info.setStatusMsg("Connecting failed. Please check server configuration.");
          info.setStatusCode(CheckingInfo.CONNECTION_FAILURE);
          updateCheckingMailStatusByCometd(userName, account.getId(), info);
        }
        return null;
      } catch (Exception e) {
        logger.debug("Exception while connecting to server : " + e.getMessage());
        StringWriter sw = new StringWriter();
        StringBuffer sb = sw.getBuffer();
        logger.error(sb.toString());
        if (info != null) {
//          info.setStatusMsg("There was an unexpected error. Connecting failed.");
          info.setStatusCode(CheckingInfo.CONNECTION_FAILURE);
          updateCheckingMailStatusByCometd(userName, account.getId(), info);
        }
        return null;
      }
      return pop3Store;
    } catch (Exception ex) {
      return null;
    }
  }

  public List<Message> removeMessageFromJCR(String userName,
                                            String accountId,
                                            List<Message> msgListFromJcrFolder,
                                            List<String> msgIDListFromMailServer) throws Exception {
    List<Message> msgList = new ArrayList<Message>();
    for (Message message : msgListFromJcrFolder) {
      String id = message.getId();
      if (!msgIDListFromMailServer.contains(id)) {
        removeMessage(userName, accountId, message);
      } else {
        msgList.add(message);
      }
    }

    return msgList;
  }

  public void addMessageNotInJCR(String userName,
                                 String accountId,
                                 String folderId,
                                 javax.mail.Folder mailServerFolder) throws Exception {
    Map<String, javax.mail.Message> msgServerMap = getServerMessageMap(mailServerFolder);
    List<String> msgIDListFromJcrFolder = getMessageIDFromJcrFolder(userName, accountId, folderId);
    if (!mailServerFolder.isOpen())
      mailServerFolder.open(javax.mail.Folder.READ_WRITE);
    Set<String> msgKeySet = msgServerMap.keySet();
    for (String msgID : msgKeySet) {
      if (!msgIDListFromJcrFolder.contains(msgID)) {
        storage_.saveTotalMessage(userName, accountId, msgID, msgServerMap.get(msgID), null);
      }
    }
  }

  public void addMessageNotInJCR(String userName,
                                 String accountId,
                                 String folderId,
                                 List<javax.mail.Message> messageList) throws Exception {
    Map<String, javax.mail.Message> msgServerMap = new HashMap<String, javax.mail.Message>();
    for (javax.mail.Message message : messageList) {
      MimeMessage mimeMessage = (MimeMessage) message;
      msgServerMap.put(mimeMessage.getMessageID(), message);
    }
    List<String> msgIDListFromJcrFolder = getMessageIDFromJcrFolder(userName, accountId, folderId);

    for (String msgID : msgIDListFromJcrFolder) {
      if (!msgServerMap.containsKey(msgID)) {
        storage_.saveTotalMessage(userName, accountId, msgID, msgServerMap.get(msgID), null);
      }
    }
  }

  public List<String> getMessageIDFromJcrFolder(String userName, String accountId, String folderId) throws Exception {
    List<Message> msgListFromJcrFolder = getMessagesByFolder(userName, accountId, folderId);
    List<String> msgIDListFromJcrFolder = new ArrayList<String>();
    for (Message message : msgListFromJcrFolder) {
      msgIDListFromJcrFolder.add(message.getId());
    }

    return msgIDListFromJcrFolder;
  }

  public List<String> getMessageIDFromServerMailFolder(String userName,
                                                       String accountId,
                                                       javax.mail.Folder mailServerFolder) throws Exception {
    List<String> msgIDListFromMailServer = new ArrayList<String>();

    if (!mailServerFolder.isOpen()) {
      mailServerFolder.open(javax.mail.Folder.READ_ONLY);
    }
    javax.mail.Message[] msgListFromMailServer = mailServerFolder.getMessages();
    for (javax.mail.Message message : msgListFromMailServer) {
      MimeMessage mimeMessage = (MimeMessage) message;
      msgIDListFromMailServer.add(mimeMessage.getMessageID());
    }
    return msgIDListFromMailServer;
  }

  public List<String> getMessageIDFromServerMailFolder(List<javax.mail.Message> messageList) throws Exception {
    List<String> msgIDListFromMailServer = new ArrayList<String>();
    for (javax.mail.Message message : messageList) {
      MimeMessage mimeMessage = (MimeMessage) message;
      msgIDListFromMailServer.add(mimeMessage.getMessageID());
    }
    return msgIDListFromMailServer;
  }

  public Map<String, javax.mail.Message> getServerMessageMap(javax.mail.Folder mailServerFolder) throws Exception {
    javax.mail.Message[] msgListFromMailServer;
    Map<String, javax.mail.Message> map = null;
    /*try {*/
      if (!mailServerFolder.isOpen())
        mailServerFolder.open(javax.mail.Folder.READ_WRITE);
      msgListFromMailServer = mailServerFolder.getMessages();
      map = new HashMap<String, javax.mail.Message>();
      for (javax.mail.Message message : msgListFromMailServer) {
        MimeMessage mimeMessage = (MimeMessage) message;
        map.put(mimeMessage.getMessageID(), message);
      }
    /*} catch (MessagingException e) {
      logger.
    }*/
    return map;
  }

  public Map<String, javax.mail.Message> getServerMessageMap(String userName,
                                                             String accountId,
                                                             List<javax.mail.Message> messageList) {
    Map<String, javax.mail.Message> map = null;
    try {
      map = new HashMap<String, javax.mail.Message>();
      for (javax.mail.Message message : messageList) {
        MimeMessage mimeMessage = (MimeMessage) message;
        map.put(mimeMessage.getMessageID(), message);
      }
    } catch (MessagingException e) {
    }

    return map;
  }

  public void mergeMessageBetweenJcrAndServerMail(IMAPStore store,
                                                  String userName,
                                                  String accountId,
                                                  String folderId,
                                                  CheckingInfo info) throws Exception {
    Folder jcrFolder = getFolder(userName, accountId, folderId);
    if (jcrFolder == null || Utils.isEmptyField(jcrFolder.getURLName())) {
      jcrFolder = getFolder(userName,
                            accountId,
                            Utils.generateFID(accountId, Utils.FD_INBOX, false));
    }
/*    try {*/
      String urlName = jcrFolder.getURLName();
      if (jcrFolder != null && !Utils.isEmptyField(urlName)) {
        URLName url = new URLName(urlName);
        javax.mail.Folder mailServerFolder = store.getFolder(url);
        if (!mailServerFolder.isOpen())
          mailServerFolder.open(javax.mail.Folder.READ_WRITE);
        if (mailServerFolder != null) {
          synchImapMessage(userName, accountId, mailServerFolder, info);
          List<String> msgIDListFromMailServer = getMessageIDFromServerMailFolder(userName,
                                                                                  accountId,
                                                                                  mailServerFolder);
          List<Message> msgListFromJcrFolder = getMessagesByFolder(userName, accountId, folderId);
          msgListFromJcrFolder = removeMessageFromJCR(userName,
                                                      accountId,
                                                      msgListFromJcrFolder,
                                                      msgIDListFromMailServer);
          addMessageNotInJCR(userName, accountId, folderId, mailServerFolder);
          Map<String, javax.mail.Message> map = getServerMessageMap(mailServerFolder);
          for (Message message : msgListFromJcrFolder) {
            if (info.isRequestStop()) {
              // check stopping request value from user.
              throw new CheckMailInteruptedException("stop checking mail while synchronizing between email server and database!");
            }
            String id = message.getId();
            javax.mail.Message serverMessage = map.get(id);
            message.setHasStar(serverMessage.isSet(Flags.Flag.FLAGGED));
            message.setUnread(!serverMessage.isSet(Flags.Flag.SEEN));
            IMAPFolder serverFolder = (IMAPFolder) mailServerFolder;
            message.setUID(String.valueOf(serverFolder.getUID(serverMessage)));
            storage_.saveMessage(userName, accountId, message, false);
          }
        }
      }
 /*   } catch (CheckMailInteruptedException ce) {
      throw ce;
    } finally {
      try {
        if (store != null && store.isConnected()) {
          store.close();
        }
      } catch (MessagingException me) { logger.warn(me); }
    }*/
  }

  private void getSynchnizeImapServer(String userName,
                                      String accountId,
                                      String folderId,
                                      boolean synchFolders) throws Exception {
    String key = userName + ":" + accountId;
    CheckingInfo info = new CheckingInfo();
    info.setAccountId(accountId);
    info.setStatusCode(CheckingInfo.START_CHECKMAIL_STATUS);
//    info.setStatusMsg("start checking emails ...");
    
    checkingLog_.put(key, info);
    updateCheckingMailStatusByCometd(userName, accountId, info);
    Account account = getAccountById(userName, accountId);
    IMAPStore store = openIMAPConnection(userName, account, info);
    
    //after connect to server, we check stopping mail request of user.
    if (info.isRequestStop()) {
      throw new CheckMailInteruptedException("stopped checking emails!");
    }
    try {
    if (store != null) {
      List<javax.mail.Folder> folderList = null;
      if (synchFolders) {
        info.setSyncFolderStatus(CheckingInfo.START_SYNC_FOLDER);
        info.setStatusCode(CheckingInfo.START_SYNC_FOLDER);
//        info.setStatusMsg("Synchronizing imap folder ...");
        updateCheckingMailStatusByCometd(userName, accountId, info); // update checking mail job by cometd.
        folderList = synchImapFolders(userName, accountId, null, store.getDefaultFolder().list());
        info.setSyncFolderStatus(CheckingInfo.FINISH_SYNC_FOLDER);
        info.setStatusCode(CheckingInfo.FINISH_SYNC_FOLDER);
//        info.setStatusMsg("Finished synchronizing imap folder ...");
        updateCheckingMailStatusByCometd(userName, accountId, info);
        //Thread.sleep(2000);
      }
      
      if (info.isRequestStop()) {
        throw new CheckMailInteruptedException("stopped checking emails!");
      }
      
      if (!Utils.isEmptyField(folderId)) {
        mergeMessageBetweenJcrAndServerMail(store, userName, accountId, folderId, info);
      } else {
        if (folderList != null && folderList.size() > 0) {
          for (javax.mail.Folder folder : folderList) {
            if (!Utils.isEmptyField(info.getRequestingForFolder_())
                && !info.getRequestingForFolder_().equals("checkall")) {
              break;
            }
            if (info != null && info.isRequestStop()) {
              if (logger.isDebugEnabled()) {
                logger.debug("Stop requested on checkmail for " + account.getId());
              }
//              info.setStatusMsg("Stop getting mails from folder " + folder.getName() + " !");// duy
              throw new CheckMailInteruptedException("Stop getting mails from folder " + folder.getName() + " !");
//              updateCheckingMailStatusByCometd(userName, accountId, info);
              //Thread.sleep(2000);// duy
              //Thread.
//              break;
            }
/*            try {*/
              synchImapMessage(userName, accountId, folder, info);
            /*}  finally {
              if (info != null) {
                info.setStatusMsg("Finish getting messages");
                info.setStatusCode(CheckingInfo.CONNECTION_FAILURE);
                updateCheckingMailStatusByCometd(userName, info);
              }
            }*/
          }
        }
      }
      
      logger.debug("/////////////////////////////////////////////////////////////");
      logger.debug("/////////////////////////////////////////////////////////////");
    } else {
      return;
    }
    
    } finally {
      //try to close mail server section when finished.
      if (store != null && store.isConnected()) {
        store.close();
      }      
    }
    // after release all resource, we send finished status to client to announce that the job has finished.
    // Note: we check my status with finished status to ensure that my status has been set before or not. 
    // If it hasn't been set yet, we will create a new finish message.  
    if (info != null) {
      
      if (info.getStatusCode() != CheckingInfo.FINISHED_CHECKMAIL_STATUS) {
//        info.setStatusMsg("Finish getting messages");
        info.setStatusCode(CheckingInfo.FINISHED_CHECKMAIL_STATUS);          
      }
      removeCheckingInfo(userName, accountId);
      updateCheckingMailStatusByCometd(userName, accountId, info);
    }
  }

  private void synchImapMessage(String userName,
                                String accountId,
                                javax.mail.Folder folder,
                                CheckingInfo info) throws Exception {
    Account account = getAccountById(userName, accountId);
    boolean saved = false;
    int totalNew = -1;
    Info infoObj = new Info();
    if (folder == null) return;
    String folderId = null;
    String folderName = folder.getName();
      if (!folder.isOpen()) {
        folder.open(javax.mail.Folder.READ_ONLY);
      }
      logger.debug(" #### Getting mails from folder " + folderName + " !");
      if (info != null) {
        info.setStatusCode(CheckingInfo.DOWNLOADING_MAIL_STATUS);
//        info.setStatusMsg("Getting mails from folder " + folder.getName() + " !");
        updateCheckingMailStatusByCometd(userName, accountId, info);
      }
      folderId = Utils.generateFID(accountId,
                                   String.valueOf(((IMAPFolder) folder).getUIDValidity()),
                                   true);
      String[] localFolders = Utils.DEFAULT_FOLDERS;
      for (String localFolder : localFolders) {
        if (localFolder.equalsIgnoreCase(folderName)) {
          folderId = Utils.generateFID(accountId, localFolder, false);
        }
      }
      Folder eXoFolder = getFolder(userName, accountId, folderId);
      if (eXoFolder != null) {
        Date checkFromDate = eXoFolder.getCheckFromDate();

        if (account.getCheckFromDate() == null) {
          checkFromDate = null;
        } else if (checkFromDate == null || checkFromDate.before(account.getCheckFromDate())) {
          checkFromDate = account.getCheckFromDate();
        }

        boolean isImap = account.getProtocol().equals(Utils.IMAP);
        boolean leaveOnserver = (isImap && Boolean.valueOf(account.getServerProperties()
                                                           .get(Utils.SVR_LEAVE_ON_SERVER)));
        //after check folder, we see the stopping request of user.
        if (info.isRequestStop()) {
          throw new CheckMailInteruptedException("stopped checking emails!");
        }
        
        LinkedHashMap<javax.mail.Message, List<String>> msgMap = getMessageMap(userName,
                                                                               accountId,
                                                                               folder,
                                                                               null,
                                                                               checkFromDate,
                                                                               null);
        //after get messages map, we see the stopping request of user.
        if (info.isRequestStop()) {
          throw new CheckMailInteruptedException("stopped checking emails!");
        }
        
        totalNew = msgMap.size();

        logger.debug(" #### Folder " + folderName + " contains " + totalNew + " messages !");
        if (totalNew > 0) {
          int i = 0;
          long msgUID;
          String folderStr;
          javax.mail.Message msg;
          String[] folderIds;
          List<String> filterList, folderList, tagList;
          MessageFilter filter;

          Date lastFromDate = null, receivedDate = null;
          List<javax.mail.Message> msgList = new ArrayList<javax.mail.Message>(msgMap.keySet());
          SpamFilter spamFilter = getSpamFilter(userName, account.getId());
          //info.setStatusMsg("Synchronizing messages of [" + folderName + "] : " + (i + 1) + "/"
                          //+ totalNew);
          info.setStatusCode(CheckingInfo.DOWNLOADING_MAIL_STATUS);
          updateCheckingMailStatusByCometd(userName, accountId, info);
          while (i < totalNew) {
            
            if (info != null && info.isRequestStop()) {
              if (logger.isDebugEnabled()) {
                logger.debug("Stop requested on checkmail for " + account.getId());
              }// duy
              //info.setStatusCode(CheckingInfo.FINISHED_CHECKMAIL_STATUS);
              //info.setStatusMsg("Stop getting mails from folder " + folder.getName() + " !");
              //updateCheckingMailStatusByCometd(userName, info);
              throw new CheckMailInteruptedException("Stop getting mails from folder " + folder.getName() + " !");
              //Thread.sleep(2000);
              // duy
              //break;
            } else if (info != null
                && !Utils.isEmptyField(info.getRequestingForFolder_())
                && !String.valueOf(((IMAPFolder) folder).getUIDValidity())
                .equals(Utils.getFolderNameFromFolderId(info.getRequestingForFolder_()))) {
              break;
            }
            if (info != null) {
              folderIds = new String[] { folderId };
              msg = msgList.get(i);

              int unreadMsgCount = folder.getUnreadMessageCount();
              if (info != null/* && i < unreadMsgCount*/) {
                info.setFetching(i + 1);
              }
              filterList = msgMap.get(msg);
              try {
                folderList = new ArrayList<String>();
                tagList = new ArrayList<String>();
                if (filterList != null && filterList.size() > 0) {
                  String tagId;
                  for (int j = 0; j < filterList.size(); j++) {
                    filter = getFilterById(userName, accountId, filterList.get(j));
                    folderList.add(filter.getApplyFolder());
                    tagId = filter.getApplyTag();
                    if (tagId != null && tagId.trim().length() > 0)
                      tagList.add(tagId);
                  }
                  folderIds = folderList.toArray(new String[] {});
                }

                folderStr = "";
                for (int k = 0; k < folderIds.length; k++) {
                  folderStr += folderIds[k] + ",";
                }
                infoObj.setFolders(folderStr);

                msgUID = ((IMAPFolder) folder).getUID(msg);
                saved = storage_.saveMessage(userName,
                                             accountId,
                                             msgUID,
                                             msg,
                                             folderIds,
                                             tagList,
                                             spamFilter,
                                             infoObj,
                                             this.continuationService_,
                                             false);
                if (saved && !leaveOnserver)
                  msg.setFlag(Flags.Flag.DELETED, true);

                receivedDate = MimeMessageParser.getReceivedDate(msg).getTime();

                if (i == 0)
                  lastFromDate = receivedDate;
                eXoFolder.setLastCheckedDate(receivedDate);
                if ((i == (totalNew - 1)))
                  eXoFolder.setCheckFromDate(lastFromDate);

                if (lastFromDate != null
                    && (eXoFolder.getLastStartCheckingTime() == null || eXoFolder.getLastStartCheckingTime()
                        .before(lastFromDate))) {
                  eXoFolder.setLastStartCheckingTime(lastFromDate);
                }
              } catch (Exception e) {
                e.printStackTrace();
                i++;
                continue;
              }
              i++;
            } else {
              break;
            }
          }
          
          saveFolder(userName, accountId, eXoFolder, false);
          /*FetchMailContentThread downloadContentMail = new FetchMailContentThread(this, storage_,
                                                                                  msgMap,
                                                                                  i,
                                                                                  folder,
                                                                                  userName,
                                                                                  accountId);
          new Thread(downloadContentMail).start();*/
          
          /*info.setStatusCode(CheckingInfo.FINISHED_CHECKMAIL_STATUS);
          info.setStatusMsg("finished checking emails!");
          updateCheckingMailStatusByCometd(userName, info);
          removeCheckingInfo(userName, accountId);*/
          } else {
          /*if (info != null) {
            // we set status code as finished checkmail but not send it to client until cleaning all resource. 
            info.setStatusCode(CheckingInfo.FINISHED_CHECKMAIL_STATUS);
//            info.setStatusMsg("There is no messages in the " + folderName + " folder!");
            
            
            //updateCheckingMailStatusByCometd(userName, info);
            //removeCheckingInfo(userName, accountId);
          }*/
        }
      }
    /*} catch (Exception e) {
      logger.error("Error while checking emails from folder" + folderName + " of userName "
                   + userName + " on account " + accountId, e);
      if (info != null) {
        info.setStatusCode(CheckingInfo.COMMON_ERROR);
        info.setStatusMsg("Error while checking emails from folder" + folderName + " of userName "
                          + userName + " on account " + accountId);
        updateCheckingMailStatusByCometd(userName, info);
      }
      stopAllJobs(userName, accountId);
    }*/
  }

  private LinkedHashMap<javax.mail.Message, List<String>> getMessageMap(String userName,
                                                                        String accountId,
                                                                        javax.mail.Folder folder,
                                                                        Date lastCheckedDate,
                                                                        Date checkFromDate,
                                                                        Date lastCheckedFromDate) throws Exception {
    LinkedHashMap<javax.mail.Message, List<String>> msgMap = new LinkedHashMap<javax.mail.Message, List<String>>();
    List<MessageFilter> filters = getFilters(userName, accountId);
    Account account = getAccountById(userName, accountId);
    boolean isImap = account.getProtocol().equals(Utils.IMAP) ? true : false;
    if (checkFromDate == null) {
      if (lastCheckedDate != null && lastCheckedFromDate != null) {
        msgMap = getMessages(msgMap, folder, isImap, lastCheckedFromDate, null, filters);
        msgMap = getMessages(msgMap, folder, isImap, null, lastCheckedDate, filters);
      } else if (lastCheckedFromDate != null) {
        msgMap = getMessages(msgMap, folder, isImap, lastCheckedFromDate, null, filters);
      } else if (lastCheckedDate != null) {
        msgMap = getMessages(msgMap, folder, isImap, null, lastCheckedDate, filters);
      } else {
        msgMap = getMessages(msgMap, folder, isImap, null, null, filters);
      }
    } else {
      if (lastCheckedDate != null && lastCheckedFromDate != null) {
        msgMap = getMessages(msgMap, folder, isImap, lastCheckedFromDate, null, filters);
        msgMap = getMessages(msgMap, folder, isImap, checkFromDate, lastCheckedDate, filters);
      } else if (lastCheckedFromDate != null) {
        msgMap = getMessages(msgMap, folder, isImap, lastCheckedFromDate, null, filters);
      } else if (lastCheckedDate != null && lastCheckedDate.after(checkFromDate)) {
        msgMap = getMessages(msgMap, folder, isImap, checkFromDate, lastCheckedDate, filters);
      } else {
        msgMap = getMessages(msgMap, folder, isImap, checkFromDate, null, filters);
      }
    }
    return msgMap;
  }

  public List<Message> checkNewMessage(String userName, String accountId) throws Exception {
    return checkNewMessage(userName, accountId, null);
  }

  public List<Message> checkNewMessage(String username, String accountId, String folderId) throws Exception {
    Account account = getAccountById(username, accountId);
    List<Message> messageList = new ArrayList<Message>();
    if (account != null) {
      try {
        if (account.getProtocol().equals(Utils.POP3)) {
          return checkPop3Server(username, accountId);
        } else if (account.getProtocol().equals(Utils.IMAP)) {
          boolean synchFolder = !(getFolders(username, accountId, true).size() > 0);
          getSynchnizeImapServer(username, accountId, folderId, synchFolder);
        }
      } catch (CheckMailInteruptedException cme) {
        
        CheckingInfo info = getCheckingInfo(username, accountId);
        info.setRequestStop(false);
        info.assignInterruptedStatus();
        removeCheckingInfo(username, accountId);
        updateCheckingMailStatusByCometd(username, accountId, info);
      } catch (MessagingException e) {
        // System.err.println("Failed to open '" + folder.getName() +
        // "' folder as read-only");
        CheckingInfo info = getCheckingInfo(username, accountId);
        if (info != null) {
//          info.setStatusMsg("error-connection-fail");
          info.setStatusCode(CheckingInfo.CONNECTION_FAILURE);
          updateCheckingMailStatusByCometd(username, accountId, info);
        }
        // stopCheckMail(username, accountId);
      } finally {
        if (!account.isSavePassword()) {
          account.setIncomingPassword("");
          updateAccount(username, account);
        }
      }
    }

    return messageList;
  }

  // TODO: refactor code for checking mail from POP3 server.
  public List<Message> checkPop3Server(String userName, String accountId) throws Exception {
    Account account = getAccountById(userName, accountId);
    List<Message> messageList = new ArrayList<Message>();
    if (account != null) {
      String key = userName + ":" + accountId;
      CheckingInfo info = new CheckingInfo();
      info.setAccountId(accountId);
      checkingLog_.put(key, info);
      long t1, t2, tt1, tt2;
      if (Utils.isEmptyField(account.getIncomingPassword())) {
//        info.setStatusMsg("msg-retry-password");
        info.setStatusCode(CheckingInfo.RETRY_PASSWORD);
        updateCheckingMailStatusByCometd(userName, accountId, info);
        return messageList;
      }
      logger.debug(" #### Getting mail from " + account.getIncomingHost() + " ... !");
      info.setStatusCode(CheckingInfo.START_CHECKMAIL_STATUS);
//      info.setStatusMsg("Getting mail from:" + account.getIncomingHost() + " ... !");
      updateCheckingMailStatusByCometd(userName, accountId, info);
      int totalNew = 0;
      boolean isImap = account.getProtocol().equals(Utils.IMAP);

//      try {
        // String[] incomingFolders = account.getIncomingFolder().split(",");

        // String incomingFolder = incomingFolders[0].trim();
        String incomingFolder = account.getIncomingFolder().trim();
        URLName storeURL = new URLName(account.getProtocol(),
                                       account.getIncomingHost(),
                                       Integer.valueOf(account.getIncomingPort()),
                                       incomingFolder,
                                       account.getIncomingUser(),
                                       account.getIncomingPassword());

        POP3Store store = openPOPConnection(userName, account, info);
        if (store == null) {
          return messageList;
        }
        
        if (info.isRequestStop()) {
          throw new CheckMailInteruptedException("stopped checking emails");
        }
        
        javax.mail.Folder folder = store.getFolder(storeURL.getFile());
        
        // after get folder, check stopping request of user.
        
        if (info.isRequestStop()) {
          throw new CheckMailInteruptedException("stopped checking emails");
        }
        
        try {
        if (!folder.exists()) {
          logger.debug(" #### Folder " + incomingFolder + " is not exists !");
          info.setStatusCode(CheckingInfo.COMMON_ERROR);
//          info.setStatusMsg("Folder " + incomingFolder + " is not exists");
          
          updateCheckingMailStatusByCometd(userName, accountId, info);
          store.close();
          return messageList;
        } else {
          logger.debug(" #### Getting mails from folder " + incomingFolder + " !");
//          info.setStatusMsg("Getting mails from folder " + incomingFolder + " !");
          info.setStatusCode(CheckingInfo.DOWNLOADING_MAIL_STATUS);
          updateCheckingMailStatusByCometd(userName, accountId, info);
        }
        folder.open(javax.mail.Folder.READ_WRITE);

        Date lastCheckedDate = account.getLastCheckedDate();
        Date lastCheckedFromDate = account.getLastStartCheckingTime();
        Date checkFromDate = account.getCheckFromDate();

        List<MessageFilter> filters = getFilters(userName, accountId);
        LinkedHashMap<javax.mail.Message, List<String>> msgMap = new LinkedHashMap<javax.mail.Message, List<String>>();

        if (checkFromDate == null) {
          if (lastCheckedDate != null && lastCheckedFromDate != null) {
            msgMap = getMessages(msgMap, folder, isImap, lastCheckedFromDate, null, filters);
            msgMap = getMessages(msgMap, folder, isImap, null, lastCheckedDate, filters);
          } else if (lastCheckedFromDate != null) {
            msgMap = getMessages(msgMap, folder, isImap, lastCheckedFromDate, null, filters);
          } else if (lastCheckedDate != null) {
            msgMap = getMessages(msgMap, folder, isImap, null, lastCheckedDate, filters);
          } else {
            msgMap = getMessages(msgMap, folder, isImap, null, null, filters);
          }
        } else {
          if (lastCheckedDate != null && lastCheckedFromDate != null) {
            msgMap = getMessages(msgMap, folder, isImap, lastCheckedFromDate, null, filters);
            msgMap = getMessages(msgMap, folder, isImap, checkFromDate, lastCheckedDate, filters);
          } else if (lastCheckedFromDate != null) {
            msgMap = getMessages(msgMap, folder, isImap, lastCheckedFromDate, null, filters);
          } else if (lastCheckedDate != null && lastCheckedDate.after(checkFromDate)) {
            msgMap = getMessages(msgMap, folder, isImap, checkFromDate, lastCheckedDate, filters);
          } else {
            msgMap = getMessages(msgMap, folder, isImap, checkFromDate, null, filters);
          }
        }

        /*
         * after get message map, check stopping request of user.
         */
        if (info.isRequestStop()) {
          throw new CheckMailInteruptedException("stopped checking emails");
         }
        
        totalNew = msgMap.size();

        logger.debug("=============================================================");
        logger.debug("=============================================================");
        logger.debug(" #### Folder contains " + totalNew + " messages !");

        tt1 = System.currentTimeMillis();
        boolean saved = false;

        if (totalNew > 0) {
          boolean leaveOnServer = (Boolean.valueOf(account.getServerProperties()
                                                   .get(Utils.SVR_LEAVE_ON_SERVER)));

          info.setTotalMsg(totalNew);

          int i = 0;
          javax.mail.Message msg;
          List<String> filterList;
          MessageFilter filter;
          String folderStr;
          Date lastFromDate = null;
          Date receivedDate = null;
          List<String> folderList, tagList;
          List<javax.mail.Message> msgList = new ArrayList<javax.mail.Message>(msgMap.keySet());
          SpamFilter spamFilter = getSpamFilter(userName, account.getId());
          String folderId = makeStoreFolder(userName, accountId, incomingFolder);

          //info.setStatusMsg("Fetching message " + (i + 1) + "/" + totalNew);
//          info.setStatusMsg("fetching-mail");
          updateCheckingMailStatusByCometd(userName, accountId, info);
          while (i < totalNew) {
            if (info != null && info.isRequestStop()) {
              if (logger.isDebugEnabled()) {
                logger.debug("Stop requested on checkmail for " + account.getId());
              }
              break;
            }

            msg = msgList.get(i);

            info.setFetching(i + 1);
            logger.debug("Fetching message " + (i + 1) + " ...");
            t1 = System.currentTimeMillis();

            filterList = msgMap.get(msg);
            try {
              String[] folderIds = { folderId };
              folderList = new ArrayList<String>();
              tagList = new ArrayList<String>();
              if (filterList != null && filterList.size() > 0) {
                String tagId;
                for (int j = 0; j < filterList.size(); j++) {
                  filter = getFilterById(userName, accountId, filterList.get(j));
                  folderList.add(filter.getApplyFolder());
                  tagId = filter.getApplyTag();
                  if (tagId != null && tagId.trim().length() > 0)
                    tagList.add(tagId);
                }
                folderIds = folderList.toArray(new String[] {});
              }

              saved = storage_.savePOP3Message(userName,
                                               accountId,
                                               msg,
                                               folderIds,
                                               tagList,
                                               spamFilter,
                                               null,
                                               null);

              if (saved) {
                msg.setFlag(Flags.Flag.SEEN, true);
                if (!leaveOnServer)
                  msg.setFlag(Flags.Flag.DELETED, true);

                folderStr = "";
                for (int k = 0; k < folderIds.length; k++) {
                  folderStr += folderIds[k] + ",";
                }
                info.setFetchingToFolders(folderStr);
                info.setMsgId(MimeMessageParser.getMessageId(msg));
              }

              receivedDate = MimeMessageParser.getReceivedDate(msg).getTime();
              if (i == 0)
                lastFromDate = receivedDate;
              account.setLastCheckedDate(receivedDate);
              if ((i == (totalNew - 1)))
                account.setCheckFromDate(lastFromDate);

              if (lastFromDate != null
                  && (account.getLastStartCheckingTime() == null || account.getLastStartCheckingTime()
                      .before(lastFromDate))) {
                account.setLastStartCheckingTime(lastFromDate);
              }
            } catch (Exception e) {
//              info.setStatusMsg("An error occurs while fetching messsge " + (i + 1));
              info.setStatusCode(CheckingInfo.COMMON_ERROR);
//              removeCheckingInfo(userName, accountId);
              updateCheckingMailStatusByCometd(userName, accountId, info);
              i++;
              continue;
            }
            i++;
            t2 = System.currentTimeMillis();
            logger.debug("Message " + i + " saved : " + (t2 - t1) + " ms");
          }
          // end while
          tt2 = System.currentTimeMillis();
          logger.debug(" ### Check mail finished total took: " + (tt2 - tt1) + " ms");
        } else {
          if (info != null) {
            info.setStatusCode(CheckingInfo.FINISHED_CHECKMAIL_STATUS);
            //info.setStatusMsg("There is no message!");
            updateCheckingMailStatusByCometd(userName, accountId, info);
            removeCheckingInfo(userName, accountId);
            return messageList;
          }
        }

        /*if (!account.isSavePassword()) {
          account.setIncomingPassword("");
          updateAccount(userName, account);
        }*/
        } finally {
          folder.close(true);
          store.close();          
        }
//        info.setStatusMsg("Finish fetching mail!");
        info.setStatusCode(CheckingInfo.FINISHED_CHECKMAIL_STATUS);
        updateCheckingMailStatusByCometd(userName, accountId, info);

        logger.debug("/////////////////////////////////////////////////////////////");
        logger.debug("/////////////////////////////////////////////////////////////");
      /*} catch (Exception e) {
        logger.error("Error while checking emails for " + userName + " on account " + accountId, e);
      }*/
    }
    return messageList;
  }

  private String makeStoreFolder(String userName, String accountId, String incomingFolder) throws Exception {
    String folderId = Utils.generateFID(accountId, incomingFolder, false);
    Folder storeFolder = storage_.getFolder(userName, accountId, folderId);
    if (storeFolder == null) {
      folderId = Utils.generateFID(accountId, incomingFolder, true);
      Folder storeUserFolder = storage_.getFolder(userName, accountId, folderId);
      if (storeUserFolder != null) {
        storeFolder = storeUserFolder;
      } else {
        storeFolder = new Folder();
      }
      storeFolder.setId(folderId);
      storeFolder.setName(incomingFolder);
      storeFolder.setPersonalFolder(true);
      storage_.saveFolder(userName, accountId, storeFolder);
    }
    return folderId;
  }

  public SearchTerm getSearchTerm(SearchTerm sTerm, MessageFilter filter) throws Exception {
    if (!Utils.isEmptyField(filter.getFrom())) {
      FromStringTerm fsTerm = new FromStringTerm(filter.getFrom());
      if (filter.getFromCondition() == Utils.CONDITION_CONTAIN) {
        if (sTerm == null) {
          sTerm = fsTerm;
        } else {
          sTerm = new AndTerm(sTerm, fsTerm);
        }
      } else if (filter.getFromCondition() == Utils.CONDITION_NOT_CONTAIN) {
        if (sTerm == null) {
          sTerm = new NotTerm(fsTerm);
        } else {
          sTerm = new AndTerm(sTerm, new NotTerm(fsTerm));
        }
      }
    }

    if (!Utils.isEmptyField(filter.getTo())) {
      RecipientStringTerm toTerm = new RecipientStringTerm(RecipientType.TO, filter.getTo());
      if (filter.getToCondition() == Utils.CONDITION_CONTAIN) {
        if (sTerm == null) {
          sTerm = toTerm;
        } else {
          sTerm = new AndTerm(sTerm, toTerm);
        }
      } else if (filter.getToCondition() == Utils.CONDITION_NOT_CONTAIN) {
        if (sTerm == null) {
          sTerm = new NotTerm(toTerm);
        } else {
          sTerm = new AndTerm(sTerm, new NotTerm(toTerm));
        }
      }
    }

    if (!Utils.isEmptyField(filter.getSubject())) {
      SubjectTerm subjectTerm = new SubjectTerm(filter.getSubject());
      if (filter.getSubjectCondition() == Utils.CONDITION_CONTAIN) {
        if (sTerm == null) {
          sTerm = subjectTerm;
        } else {
          sTerm = new AndTerm(sTerm, subjectTerm);
        }
      } else if (filter.getSubjectCondition() == Utils.CONDITION_NOT_CONTAIN) {
        if (sTerm == null) {
          sTerm = new NotTerm(subjectTerm);
        } else {
          sTerm = new AndTerm(sTerm, new NotTerm(subjectTerm));
        }
      }
    }

    if (!Utils.isEmptyField(filter.getBody())) {
      BodyTerm bodyTerm = new BodyTerm(filter.getBody());
      if (filter.getBodyCondition() == Utils.CONDITION_CONTAIN) {
        if (sTerm == null) {
          sTerm = bodyTerm;
        } else {
          sTerm = new AndTerm(sTerm, bodyTerm);
        }
      } else if (filter.getBodyCondition() == Utils.CONDITION_NOT_CONTAIN) {
        if (sTerm == null) {
          sTerm = new NotTerm(bodyTerm);
        } else {
          sTerm = new AndTerm(sTerm, new NotTerm(bodyTerm));
        }
      }
    }

    return sTerm;
  }

  public void createAccount(String userName, Account account) throws Exception {
    saveAccount(userName, account, true);
  }

  public List<Folder> getFolders(String userName, String accountId) throws Exception {
    return storage_.getFolders(userName, accountId);
  }

  public List<Folder> getFolders(String userName, String accountId, boolean isPersonal) throws Exception {
  	List<Folder> folders = new ArrayList<Folder>();
    List<Folder> gottenFolderList = storage_.getFolders(userName, accountId);
    Account account = getAccountById(userName, accountId);
    CheckingInfo info = getCheckingInfo(userName, accountId);
    Store store = null;
    List<Folder> defaultFolders = new ArrayList<Folder>();
    for (Folder folder : gottenFolderList) {
      if (isPersonal) {
        if (folder.isPersonalFolder()) {
          folders.add(folder);
        }
      } else {
        defaultFolders.add(folder);
      }
    }
    // if (info != null && account.getProtocol().equals(Utils.POP3)) {
    // store = openPOPConnection(userName, account, info);
    // } else if (info != null && account.getProtocol().equals(Utils.IMAP)) {
    // store = openIMAPConnection(userName, account, info);
    // }
    // for (Folder folder : defaultFolders) {
    // String urlName = folder.getURLName();
    // if (!folder.isPersonalFolder()) {
    // if (Utils.isEmptyField(urlName)) {
    // if (store != null) {
    // javax.mail.Folder fd = store.getFolder(folder.getName());
    // folder.setURLName(fd.getURLName().toString());
    // storage_.saveFolder(userName, accountId, folder);
    // } else {
    // break;
    // }
    // }
    // folders.add(folder);
    // }
    // }
    if (info != null) {
      if (account.getProtocol().equals(Utils.POP3)) {
        store = openPOPConnection(userName, account, info);
      } else if (account.getProtocol().equals(Utils.IMAP)) {
        store = openIMAPConnection(userName, account, info);
      }
      for (Folder folder : defaultFolders) {
        String urlName = folder.getURLName();
        if (!folder.isPersonalFolder()) {
          if (Utils.isEmptyField(urlName)) {
            if (store != null) {
              javax.mail.Folder fd = store.getFolder(folder.getName());
              folder.setURLName(fd.getURLName().toString());
              storage_.saveFolder(userName, accountId, folder);
            } else {
              break;
            }
          }
          folders.add(folder);
        }
      }
    } else {
      for (Folder folder : defaultFolders) {
        if (!folder.isPersonalFolder()) {
          folders.add(folder);
        }
      }
    }
    return folders;
  }

  public List<Folder> getFolders_DUYROOT(String username, String accountId, boolean isPersonal) throws Exception {
    List<Folder> folders = new ArrayList<Folder>();
    List<Folder> gottenFolderList = storage_.getFolders(username, accountId);
    Account account = getAccountById(username, accountId);
    CheckingInfo info = new CheckingInfo();
    String key = username + ":" + accountId;
    checkingLog_.put(key, info);
    Store store = null;
    for (Folder folder : gottenFolderList) {
      String urlName = folder.getURLName();
      if (isPersonal) {
        if (folder.isPersonalFolder()) {
          folders.add(folder);
        }
      } else {
        if (!folder.isPersonalFolder()) {
          if (Utils.isEmptyField(urlName)) {
            if (account.getProtocol().equals(Utils.POP3)) {
              store = openPOPConnection(username, account, info);
            } else if (account.getProtocol().equals(Utils.IMAP)) {
              store = openIMAPConnection(username, account, info);
            }
            javax.mail.Folder fd = store.getFolder(folder.getName());
            folder.setURLName(fd.getURLName().toString());
            storage_.saveFolder(username, accountId, folder);
          }
          folders.add(folder);
        }
      }
    }
    removeCheckingInfo(username, accountId);
    return folders;
  }

  public void addTag(String userName, String accountId, Tag tag) throws Exception {
    storage_.addTag(userName, accountId, tag);
  }

  public void addTag(String userName, String accountId, List<Message> messages, List<Tag> tag) throws Exception {
    storage_.addTag(userName, accountId, messages, tag);
  }

  public List<Tag> getTags(String userName, String accountId) throws Exception {
    return storage_.getTags(userName, accountId);
  }

  public Tag getTag(String userName, String accountId, String tagId) throws Exception {
    return storage_.getTag(userName, accountId, tagId);
  }

  public void removeTagsInMessages(String userName,
                                   String accountId,
                                   List<Message> msgList,
                                   List<String> tagIdList) throws Exception {
    storage_.removeTagsInMessages(userName, accountId, msgList, tagIdList);
  }

  public void removeTag(String userName, String accountId, String tag) throws Exception {
    storage_.removeTag(userName, accountId, tag);
  }

  public void updateTag(String userName, String accountId, Tag tag) throws Exception {
    storage_.updateTag(userName, accountId, tag);
  }

  public List<Message> getMessageByTag(String userName, String accountId, String tagName) throws Exception {
    return storage_.getMessageByTag(userName, accountId, tagName);
  }

  public MessagePageList getMessagePagelistByTag(String userName, String accountId, String tagId) throws Exception {
    MessageFilter filter = new MessageFilter("Filter By Tag");
    filter.setAccountId(accountId);
    filter.setTag(new String[] { tagId });
    return getMessagePageList(userName, filter);
  }

  public MessagePageList getMessagePageListByFolder(String userName,
                                                    String accountId,
                                                    String folderId) throws Exception {
    MessageFilter filter = new MessageFilter("Filter By Folder");
    filter.setAccountId(accountId);
    filter.setFolder(new String[] { folderId });
    return getMessagePageList(userName, filter);
  }

  public MailSetting getMailSetting(String userName) throws Exception {
    return storage_.getMailSetting(userName);
  }

  public void saveMailSetting(String userName, MailSetting newSetting) throws Exception {
    storage_.saveMailSetting(userName, newSetting);
  }

  public boolean importMessage(String userName,
                               String accountId,
                               String folderId,
                               InputStream inputStream,
                               String type) throws Exception {
    Properties props = System.getProperties();
    Session session = Session.getDefaultInstance(props, null);
    MimeMessage mimeMessage = new MimeMessage(session, inputStream);
    long[] msgUID = { 0 };
    boolean result = false;
    try {
      //importMessageIntoServerMail(userName, accountId, folderId, mimeMessage, msgUID);
      //emlImportExport_.importMessage(userName, accountId, folderId, mimeMessage, msgUID);
      msgUID = importMessageIntoServerMail(userName, accountId, folderId, mimeMessage);
      emlImportExport_.importMessage(userName, accountId, folderId, mimeMessage, msgUID);
      result = true;
    } catch (Exception ex) {
    }
    return result;
  }


  private long[] importMessageIntoServerMail(String userName,
                                              String accountId,
                                              String folderId,
                                              MimeMessage mimeMessage) throws Exception {
    IMAPFolder remoteFolder = null;
    long[] uid = {0};
    try {
      remoteFolder = getIMAPFolder(userName, accountId, folderId);
      if (remoteFolder != null) {
        if (!remoteFolder.isOpen()) {
          remoteFolder.open(javax.mail.Folder.READ_WRITE);
        }
        javax.mail.Message[] messages = { mimeMessage };
        javax.mail.Message[] updatedMsgs = remoteFolder.addMessages(messages);
        uid[0] = remoteFolder.getUIDNext();
        remoteFolder.close(true);
      }
    } catch (Exception e) {
      logger.error("Error in importing message into remote folder", e);
    }
    
    return uid;

  }
  
  private boolean importMessageIntoServerMail(String userName,
                                              String accountId,
                                              String folderId,
                                              MimeMessage mimeMessage,
                                              long[] msgUID) throws Exception {
    boolean result = false;
    try {
      IMAPFolder remoteFolder = getIMAPFolder(userName, accountId, folderId);
      if (remoteFolder != null) {
        if (!remoteFolder.isOpen()) {
          remoteFolder.open(javax.mail.Folder.READ_WRITE);
        }
        javax.mail.Message[] messages = { mimeMessage };
        javax.mail.Message[] updatedMsgs = remoteFolder.addMessages(messages);
        msgUID[0] = remoteFolder.getUIDNext();
        if (updatedMsgs.length == 1) {
          result = true;
        }
        remoteFolder.close(true);
      }
    } catch (Exception e) {
      logger.error("Error in importing message into remote folder", e);
    }

    return result;

  }

  private IMAPFolder getIMAPFolder(String userName, String accountId, String folderId) throws Exception {
    Folder folder = getFolder(userName, accountId, folderId);
    Account account = getAccountById(userName, accountId);
    IMAPStore store = openIMAPConnection(userName, account);
    URLName remoteURL = new URLName(folder.getURLName());
    IMAPFolder remoteFolder = null;
    if (store != null) {
      remoteFolder = (IMAPFolder) store.getFolder(remoteURL);
    }
    return remoteFolder;

  }

  public OutputStream exportMessage(String userName, String accountId, Message message) throws Exception {
    return emlImportExport_.exportMessage(userName, accountId, message);
  }

  public SpamFilter getSpamFilter(String userName, String accountId) throws Exception {
    return storage_.getSpamFilter(userName, accountId);
  }

  public void saveSpamFilter(String userName, String accountId, SpamFilter spamFilter) throws Exception {
    storage_.saveSpamFilter(userName, accountId, spamFilter);
  }

  public void toggleMessageProperty(String userName,
                                    String accountId,
                                    List<Message> msgList,
                                    String folderId,
                                    String property,
                                    boolean value) throws Exception {
    Account account = getAccountById(userName, accountId);
    Folder folder = getFolder(userName, accountId, folderId);
    boolean success = true;
    if (account.getProtocol().equalsIgnoreCase(Utils.IMAP)) {
      try {
        Connector connector = new ImapConnector(account);
        if (property.equals(Utils.EXO_STAR)) {
          if (folder != null && !Utils.isEmptyField(folder.getName())) {
            success = connector.setIsStared(msgList, value, folder);
          } else {
            List<Message> l = new ArrayList<Message>();
            for (Message m : msgList) {
              folder = getFolder(userName, accountId, m.getFolders()[0]);
              if (folder != null) {
                l.add(m);
                success = connector.setIsStared(l, value, folder);
              }
            }
          }
        } else if (property.equals(Utils.EXO_ISUNREAD)) {
          if (folder != null && !Utils.isEmptyField(folder.getName())) {
            if (value) {
              success = connector.markAsUnread(msgList, folder);
            } else {
              success = connector.markAsRead(msgList, folder);
            }
          } else {
            List<Message> l;
            for (Message m : msgList) {
              folder = getFolder(userName, accountId, m.getFolders()[0]);
              if (folder != null) {
                l = new ArrayList<Message>();
                l.add(m);
                if (value) {
                  success = connector.markAsUnread(l, folder);
                } else {
                  success = connector.markAsRead(l, folder);
                }
              }
            }
          }
        }

      } catch (Exception e) {
        return;
      }
    }
    if (success)
      storage_.toggleMessageProperty(userName, accountId, msgList, property, value);
  }

  public List<AccountData> getAccountDatas(SessionProvider sProvider) throws Exception {
    return null;
  }

  public String getFolderHomePath(String userName, String accountId) throws Exception {
    return storage_.getFolderHomePath(userName, accountId);
  }

  public List<Folder> getSubFolders(String userName, String accountId, String parentPath) throws Exception {
    return storage_.getSubFolders(userName, accountId, parentPath);
  }

  public List<Message> getReferencedMessages(String userName, String accountId, String msgPath) throws Exception {
    return storage_.getReferencedMessages(userName, accountId, msgPath);
  }

  public Account getDefaultAccount(String userName) throws Exception {
    MailSetting mailSetting = storage_.getMailSetting(userName);
    String defaultAccount = mailSetting.getDefaultAccount();
    Account account = null;
    if (defaultAccount != null) {
      account = getAccountById(userName, defaultAccount);
    } else {
      List<Account> accList = getAccounts(userName);
      if (accList.size() > 0)
        account = getAccounts(userName).get(0);
    }
    return account;
  }

  public Message loadTotalMessage(String userName, String accountId, Message msg) throws Exception {
    Account account = getAccountById(userName, accountId);
    Store store = null;
    javax.mail.Folder fd = null;
    try {
      if (account.getProtocol().equals(Utils.IMAP)) {
        store = openIMAPConnection(userName, account);
        if (store != null) {
          javax.mail.Message message = null;
          URLName url = new URLName(getFolder(userName, accountId, msg.getFolders()[0]).getURLName());
          fd = store.getFolder(url);
          if (fd != null) {
            if (!fd.isOpen()) {
              fd.open(javax.mail.Folder.READ_WRITE);
            }
            message = ((IMAPFolder) fd).getMessageByUID(Long.valueOf(msg.getUID()));
            msg = storage_.loadTotalMessage(userName, accountId, msg, message);
          }
        }
      } else if (account.getProtocol().equals(Utils.POP3)) {
        CheckingInfo info = getCheckingInfo(userName, accountId);
        store = openPOPConnection(userName, account, info);
        msg = storage_.loadTotalMessage(userName, accountId, msg);
      }
    } catch (Exception e) {
      try {
        msg = storage_.loadTotalMessage(userName, accountId, msg, null);
      } catch (Exception ex) {}
      logger.info("Download content failure");
    } finally {
      if (store != null && store.isConnected()) {
        store.close();
      }
      if (fd != null && fd.isOpen()) {
        fd.close(true);
      }
    }
    return msg;
  }

  public List<MailUpdateStorageEventListener> listeners_ = new ArrayList<MailUpdateStorageEventListener>();

  public void start() {
    for (MailUpdateStorageEventListener updateListener : listeners_) {
      updateListener.preUpdate();
    }
  }

  public void stop() {
  }

  public synchronized void addListenerPlugin(ComponentPlugin listener) throws Exception {
    if (listener instanceof MailUpdateStorageEventListener) {
      listeners_.add((MailUpdateStorageEventListener) listener);
    }
  }

  private Properties getAccountProperties(Account acc) {
    Properties props = System.getProperties();
    String smtpUser = acc.getIncomingUser();
    String outgoingHost = acc.getOutgoingHost();
    String outgoingPort = acc.getOutgoingPort();
    String isSSl = acc.getServerProperties().get(Utils.SVR_OUTGOING_SSL);
    props.put(Utils.SVR_SMTP_USER, smtpUser);
    props.put(Utils.SVR_SMTP_HOST, outgoingHost);
    props.put(Utils.SVR_SMTP_PORT, outgoingPort);
    props.put("mail.smtp.dsn.notify", "SUCCESS,FAILURE ORCPT=rfc822;" + acc.getEmailAddress());
    props.put("mail.smtp.dsn.ret", "FULL");
    props.put("mail.smtp.socketFactory.port", outgoingPort);
    props.put(Utils.SVR_SMTP_AUTH, "true");
    props.put(Utils.SVR_SMTP_SOCKET_FACTORY_FALLBACK, "true");
    props.put("mail.smtp.connectiontimeout", "0");
    props.put("mail.smtp.timeout", "0");
    // props.put("mail.debug", "true");
    String socketFactoryClass = "javax.net.SocketFactory";
    if (Boolean.valueOf(isSSl)) {
      socketFactoryClass = Utils.SSL_FACTORY;
      props.put(Utils.SVR_SMTP_STARTTLS_ENABLE, "true");
      props.put("mail.smtp.ssl.protocols", "SSLv3 TLSv1");
    }
    props.put(Utils.SVR_SMTP_SOCKET_FACTORY_CLASS, socketFactoryClass);

    return props;
  }

  public boolean sendReturnReceipt(String userName, String accId, String msgId, ResourceBundle res) throws Exception {
    // TODO need to implement
    Account acc = getAccountById(userName, accId);
    Message msg = getMessageById(userName, accId, msgId);

    String subject = new String("Disposition notification");
    String text = new String("The message sent on {0} to {1} with subject \"{2}\" has been displayed. This is no guarantee that the message has been read or understood.");
    if (res != null) {
      try {
        subject = res.getString("UIMessagePreview.msg.return-receipt-subject");
      } catch (MissingResourceException e) {
        subject = new String("Disposition notification");
      }
      try {
        text = res.getString("UIMessagePreview.msg.return-receipt-text");
      } catch (MissingResourceException e) {
        text = new String("The message sent on {0} to {1} with subject \"{2}\" has been displayed. This is no guarantee that the message has been read or understood.");
      }
    }
    text = text.replace("{0}", msg.getSendDate().toString());
    text = text.replace("{1}", msg.getMessageTo());
    text = text.replace("{2}", msg.getSubject());

    Message receiptMsg = new Message();
    receiptMsg.setMessageTo(msg.getFrom());
    receiptMsg.setSubject(subject);
    receiptMsg.setSendDate(new Date());

    DispositionNotification disNotification = new DispositionNotification();
    disNotification.getNotifications()
    .setHeader("Reporting-UA", "cs.exoplatform.com;" + " CS-Mail");
    disNotification.getNotifications().setHeader("MDN-Gateway",
                                                 "smtp;" + " " + acc.getOutgoingHost());
    disNotification.getNotifications().setHeader("Original-Recipient",
                                                 "rfc822;" + " " + msg.getFrom());
    disNotification.getNotifications().setHeader("Final-Recipient",
                                                 "rfc822;" + " " + acc.getUserDisplayName() + "<"
                                                 + acc.getEmailAddress() + ">");
    disNotification.getNotifications().setHeader("Original-Message-ID", msg.getId());
    disNotification.getNotifications().setHeader("Disposition",
                                                 "manual-action/MDN-sent-automatically;"
                                                 + " displayed");

    MultipartReport report = new MultipartReport(text, disNotification);

    Properties props = getAccountProperties(acc);
    Session session = Session.getDefaultInstance(props, null);
    logger.debug(" #### Sending email ... ");
    SMTPTransport transport = (SMTPTransport) session.getTransport(Utils.SVR_SMTP);
    try {
      if (!acc.isOutgoingAuthentication()) {
        transport.connect();
      } else if (acc.useIncomingSettingForOutgoingAuthent()) {
        transport.connect(acc.getOutgoingHost(),
                          Integer.parseInt(acc.getOutgoingPort()),
                          acc.getIncomingUser(),
                          acc.getIncomingPassword());
      } else {
        transport.connect(acc.getOutgoingHost(),
                          Integer.parseInt(acc.getOutgoingPort()),
                          acc.getOutgoingUserName(),
                          acc.getOutgoingPassword());
      }
    } catch (Exception ex) {
      logger.debug("#### Can not connect to smtp server ...");
      throw ex;
    }

    MailcapCommandMap mailcap = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
    mailcap.addMailcap("message/disposition-notification;; x-java-content-handler=org.exoplatform.mail.service.impl.Message_DispositionNotification");
    CommandMap.setDefaultCommandMap(mailcap);

    sendReturnReceipt(session, transport, receiptMsg, report);
    transport.close();

    return true;
  }

  private void sendReturnReceipt(Session session,
                                 Transport transport,
                                 Message message,
                                 MultipartReport report) throws Exception {
    MimeMessage mimeMessage = new MimeMessage(session);
    String status = "";
    InternetAddress addressFrom;
    mimeMessage.setHeader("Message-ID", message.getId());
    mimeMessage.setHeader("Content-Transfer-Encoding", "utf-8");

    if (message.getFrom() != null)
      addressFrom = new InternetAddress(message.getFrom());
    else
      addressFrom = new InternetAddress(session.getProperties().getProperty(Utils.SVR_SMTP_USER));

    mimeMessage.setFrom(addressFrom);
    if (message.getMessageTo() != null)
      mimeMessage.setRecipients(javax.mail.Message.RecipientType.TO,
                                InternetAddress.parse(message.getMessageTo()));

    mimeMessage.setSubject(message.getSubject(), "UTF-8");
    mimeMessage.setSentDate(message.getSendDate());

    mimeMessage.setContent(report);

    mimeMessage.saveChanges();
    try {
      transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
      status = "Mail Delivered !";
    } catch (AddressException e) {
      status = "There was an error parsing the addresses. Sending Failed !" + e.getMessage();
      throw e;
    } catch (AuthenticationFailedException e) {
      status = "The Username or Password may be wrong. Sending Failed !" + e.getMessage();
      throw e;
    } catch (SMTPSendFailedException e) {
      status = "Sorry, There was an error sending the message. Sending Failed !" + e.getMessage();
      throw e;
    } catch (MessagingException e) {
      status = "There was an unexpected error. Sending Failed ! " + e.getMessage();
      throw e;
    } catch (Exception e) {
      status = "There was an unexpected error. Sending Falied !" + e.getMessage();
      throw e;
    }
    logger.debug(" #### Info : " + status);
  }

  public ContinuationService getContinuationService() {
    return continuationService_;
  }

  public void setContinuationService(ContinuationService continuationService){
    continuationService_ = continuationService;

  }
  
  public void updateCheckingMailStatusByCometd(String userName, String accountId, CheckingInfo info) {
    if (info != null && info.hasChanged()) {     
      try {      
      JsonGeneratorImpl generatorImpl = new JsonGeneratorImpl();
      JsonValue json = generatorImpl.createJsonObject(info.getStatus());
      continuationService_.sendMessage(userName, "/eXo/Application/mail/ckmailsts/" + accountId, json);
      //logger.info("client [ " + userName + ":" + accountId + " ] is sent message: " + info.getStatus().getStatusMsg());
      info.setHasChanged(false);
      } catch (JsonException je) { 
        logger.warn("can not send cometd message to client [ " + userName + " ]!", je);
      } catch (Exception e) {
        if(logger.isDebugEnabled()) logger.debug("n\n can not send update message to UI " + e.getMessage());
      }
    }
  }
  
  class CheckMailInteruptedException extends Exception {
    private String message;
    public CheckMailInteruptedException(String msg) {
      super();
      this.message = msg;
    }
    
    public String getMessage() {
      return message;
    }
  }
  
}
