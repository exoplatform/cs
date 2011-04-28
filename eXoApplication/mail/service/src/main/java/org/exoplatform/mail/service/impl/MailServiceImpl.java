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

import java.io.InputStream;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.Security;
import java.util.ArrayList;
import java.util.Calendar;
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

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.MailcapCommandMap;
import javax.mail.AuthenticationFailedException;
import javax.mail.Flags;
import javax.mail.Header;
import javax.mail.MessagingException;
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
import javax.net.ssl.SSLSocketFactory;

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
import org.exoplatform.mail.service.Tag;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.scheduler.JobInfo;
import org.exoplatform.services.scheduler.JobSchedulerService;
import org.exoplatform.services.scheduler.PeriodInfo;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;
import org.picocontainer.Startable;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
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
  
  private JobSchedulerService schedulerService_;

  public MailServiceImpl(NodeHierarchyCreator nodeHierarchyCreator, JobSchedulerService schedulerService,  RepositoryService reposervice) throws Exception {
    storage_ = new JCRDataStorage(nodeHierarchyCreator,reposervice);
    emlImportExport_ = new EMLImportExport(storage_);
    checkingLog_ = new HashMap<String, CheckingInfo>();
    this.schedulerService_ = schedulerService;
  }

  public String getMailHierarchyNode() throws Exception {
    return storage_.getMailHierarchyNode();
  }
  
  public void removeCheckingInfo(String username, String accountId) throws Exception {
    String key = username + ":" + accountId;
    if(checkingLog_ == null) checkingLog_ = new HashMap<String, CheckingInfo>();
    checkingLog_.remove(key);
  }

  public CheckingInfo getCheckingInfo(String username, String accountId) {
    String key = username + ":" + accountId;
    if(checkingLog_ == null) checkingLog_ = new HashMap<String, CheckingInfo>();
    return checkingLog_.get(key);
  }

  /**
   * @param username
   * @return
   * @throws Exception
   */
  public List<Account> getAccounts(String username) throws Exception {
    return storage_.getAccounts(username);
  }

  public Account getAccountById(String username, String id) throws Exception {
    return storage_.getAccountById(username, id);
  }

  public void saveAccount(String username, Account account, boolean isNew) throws Exception {
    storage_.saveAccount(username, account, isNew);
  }

  public void updateAccount(String username, Account account) throws Exception {
    saveAccount(username, account, false);
  }

  public void removeAccount(String username, String accountId) throws Exception {
    stopAllJobs(username, accountId);
    storage_.removeAccount(username, accountId);
  }

  public Folder getFolder(String username, String accountId, String folderId) throws Exception {
    return storage_.getFolder(username, accountId, folderId);
  }

  public String getFolderParentId(String username, String accountId, String folderId) throws Exception {
    return storage_.getFolderParentId(username, accountId, folderId);
  }

  public boolean isExistFolder(String username, String accountId, String parentId, String folderName) throws Exception {
    return storage_.isExistFolder(username, accountId, parentId, folderName);
  }

  public void saveFolder(String username, String accountId, Folder folder) throws Exception {
    saveFolder(username, accountId, folder, true);
  }
  
  private void saveFolder(String username, String accountId, Folder folder, boolean b) throws Exception {
    Account account = getAccountById(username, accountId);
    if (account.getProtocol().equalsIgnoreCase(Utils.IMAP) && folder.isPersonalFolder() && b) {
    	IMAPFolder imapFolder = null;
      try {
        Connector connector = new ImapConnector(account, this.getSSLSocketFactory(null));
        imapFolder = (IMAPFolder) connector.createFolder(folder);
        saveFolder(username, accountId, null, imapFolder);
      } catch(Exception e) {
        return;
      }finally {
			if (imapFolder != null && imapFolder.isOpen()) {
				imapFolder.close(true);
			}
		}
    } else {
      storage_.saveFolder(username, accountId, folder);
    }
  }
  
  public void saveFolder(String username, String accountId, String parentId,
                         Folder folder) throws Exception {
    saveFolder(username, accountId, parentId, folder, true);
  }
  
  private void saveFolder(String username, String accountId, String parentId,
                         Folder folder, boolean b) throws Exception {
    Account account = getAccountById(username, accountId);
    if (account.getProtocol().equalsIgnoreCase(Utils.IMAP) && folder.isPersonalFolder() && b) {
      try {
        Folder parentFolder = getFolder(username, accountId, parentId);
        Connector connector = new ImapConnector(account, this.getSSLSocketFactory(null));
        IMAPFolder imapFolder = (IMAPFolder) connector.createFolder(parentFolder, folder);
        if (imapFolder != null) {
          saveFolder(username, accountId, parentFolder, imapFolder);
        }
      } catch(Exception e) {
        e.printStackTrace();
        return;
      }
    } else {
      storage_.saveFolder(username, accountId, parentId, folder);
    }
  }

  public void renameFolder(String username, String accountId, String newName,  String folderId) throws Exception {
    Account account = getAccountById(username, accountId);
    Folder folder = this.getFolder(username, accountId, folderId);
    if (account.getProtocol().equalsIgnoreCase(Utils.IMAP) && folder.isPersonalFolder()) {
      try {
        Connector connector = new ImapConnector(account, this.getSSLSocketFactory(null));
        folder = connector.renameFolder(newName, folder);
      } catch(Exception e) {
        e.printStackTrace();
        return;
      }
    }
    if (folder != null) storage_.renameFolder(username, accountId, newName, folder);
  }
  
  private void deleteLocalFolder(String username, String accountId, String folderId) throws Exception {
    storage_.removeUserFolder(username, accountId, folderId);
  }
  
  public void removeUserFolder(String username, String accountId, String folderId) throws Exception {
    Account account = getAccountById(username, accountId);
    Folder folder = this.getFolder(username, accountId, folderId);
    boolean success = true;
    if (account.getProtocol().equalsIgnoreCase(Utils.IMAP) && folder.isPersonalFolder()) {
      try {
        Connector connector = new ImapConnector(account, this.getSSLSocketFactory(null));
        success = connector.deleteFolder(folder);
      } catch(Exception e) {
        e.printStackTrace();
        return;
      }
    }
    if (success) storage_.removeUserFolder(username, accountId, folderId);
  }

  public List<MessageFilter> getFilters(String username, String accountId) throws Exception {
    return storage_.getFilters(username, accountId);
  }

  public MessageFilter getFilterById(String username, String accountId, String filterId) throws Exception {
    return storage_.getFilterById(username, accountId, filterId);
  }

  public void saveFilter(String username, String accountId, MessageFilter filter, boolean applyAll) throws Exception {
    storage_.saveFilter(username, accountId, filter, applyAll);
  }

  public void removeFilter(String username, String accountId, String filterId) throws Exception {
    storage_.removeFilter(username, accountId, filterId);
  }

  public Message getMessageById(String username, String accountId, String msgId) throws Exception {
    return storage_.getMessageById(username, accountId, msgId);
  }

  public void removeMessage(String username, String accountId, Message message) throws Exception {
    storage_.removeMessage(username, accountId, message);
  }

  public void removeMessages(String username, String accountId, List<Message> messages, boolean moveReference) throws Exception {
    storage_.removeMessages(username, accountId, messages, moveReference);
  }

  public List<Message> moveMessages(String userName, String accountId, List<Message> msgList, String currentFolderId, String destFolderId) throws Exception {
    Account account = getAccountById(userName, accountId);
    Folder currentFolder = this.getFolder(userName, accountId, currentFolderId);
    List<Message> successList = new ArrayList<Message>();
    Folder destFolder = this.getFolder(userName, accountId, destFolderId);
    if (account.getProtocol().equalsIgnoreCase(Utils.IMAP)) {
      try {
        Connector connector = new ImapConnector(account, this.getSSLSocketFactory(null));
        successList = connector.moveMessage(msgList, currentFolder, destFolder);
      } catch (Exception e) {
        if(logger.isDebugEnabled()) logger.debug("MailServiceImpl: Move message error " + e.getMessage());
      }
    }
    if (successList != null && successList.size() > 0) storage_.moveMessages(userName, accountId, successList, currentFolderId, destFolderId);
    return successList;
  }
  
  public List<Message> moveMessages(String userName, String accountId, List<Message> msgList,
                          String currentFolderId, String destFolderId, boolean updateReference) throws Exception {
    Account account = getAccountById(userName, accountId);
    Folder currentFolder = this.getFolder(userName, accountId, currentFolderId);
    Folder destFolder = this.getFolder(userName, accountId, destFolderId);
    List<Message> successList = new ArrayList<Message>();
    if (account.getProtocol().equalsIgnoreCase(Utils.IMAP)) {
      try {
        Connector connector = new ImapConnector(account, this.getSSLSocketFactory(null));
        successList = connector.moveMessage(msgList, currentFolder, destFolder);
      } catch (Exception e) {
        logger.error("Mailservice: Move message to trash folder error", e);
      }
    }
    if (successList != null && successList.size() > 0) storage_.moveMessages(userName, accountId, successList, currentFolderId, destFolderId, updateReference);
    return successList;
  }

  public Message moveMessage(String userName, String accountId, Message msg,
    String currentFolderId, String destFolderId) throws Exception {
    Account account = getAccountById(userName, accountId);
    Folder currentFolder = this.getFolder(userName, accountId, currentFolderId);
    Folder destFolder = this.getFolder(userName, accountId, destFolderId);
    List<Message> successList = new ArrayList<Message>();
    if (account.getProtocol().equalsIgnoreCase(Utils.IMAP)) {
      try {
        List<Message> msgList = new ArrayList<Message>();
        Connector connector = new ImapConnector(account, this.getSSLSocketFactory(null));
        msgList.add(msg);
        successList = connector.moveMessage(msgList, currentFolder, destFolder);
      } catch (Exception e) {
        if(logger.isDebugEnabled()) logger.debug("Mailservice: Move message fail. ", e);
      }
    }
    if (successList != null && successList.size() > 0){
      storage_.moveMessage(userName, accountId, msg, currentFolderId, destFolderId, true);
      return msg;
    }
    return null;
  }
  
  public void moveMessage(String userName, String accountId, Message msg,
	  String currentFolderId, String destFolderId, boolean updateReference) throws Exception {
    Account account = getAccountById(userName, accountId);
    Folder currentFolder = this.getFolder(userName, accountId, currentFolderId);
    Folder destFolder = this.getFolder(userName, accountId, destFolderId);
    boolean success = true;
    if (account.getProtocol().equalsIgnoreCase(Utils.IMAP)) {
      try {
        Connector connector = new ImapConnector(account, this.getSSLSocketFactory(null));
        List<Message> msgList = new ArrayList<Message>();
        msgList.add(msg);
        msgList = connector.moveMessage(msgList, currentFolder, destFolder);
        if (msgList.size() <= 0) success = false;
      } catch (Exception e) {
        return;
      }
    }
    if (success) storage_.moveMessage(userName, accountId, msg, currentFolderId, destFolderId, updateReference);
  }

  public MessagePageList getMessagePageList(String username, MessageFilter filter) throws Exception {
    return storage_.getMessagePageList(username, filter);
  }

  public boolean saveMessage(String username, Account account, String targetMsgPath, Message message, boolean isNew) throws Exception {
    List<Message> msgList = new ArrayList<Message>();
    List<Message> successList = null;
    msgList.add(message);
    String folderId = message.getFolders()[0];
    Folder destFolder = null;
    if (folderId != null) {
      destFolder = getFolder(username, account.getId(), folderId);
    }
    if (destFolder != null && account.getProtocol().equalsIgnoreCase(Utils.IMAP)) {
      Connector connector = new ImapConnector(account, this.getSSLSocketFactory(null));
      successList = connector.createMessage(msgList, destFolder);
    }
    storage_.saveMessage(username, account.getId(), targetMsgPath, message, isNew);
    if(successList != null && successList.size() > 0) return true;
    return false;
  }

  public List<Message> getMessagesByTag(String username, String accountId, String tagId) throws Exception {
    MessageFilter filter = new MessageFilter("Tag");
    filter.setAccountId(accountId);
    filter.setFolder(new String[] { tagId });
    return getMessages(username, filter);
  }

  public List<Message> getMessagesByFolder(String username, String accountId,
      String folderId) throws Exception {
    MessageFilter filter = new MessageFilter("Folder");
    filter.setAccountId(accountId);
    filter.setFolder(new String[] { folderId });
    return getMessages(username, filter);
  }

  public List<Message> getMessages(String username, MessageFilter filter) throws Exception {
    return storage_.getMessages(username, filter);
  }

  public void saveMessage(String username, String accountId, Message message, boolean isNew) throws Exception {
    storage_.saveMessage(username, accountId, message, isNew);
  }

  public Message sendMessage(String username, String accId, Message message) throws Exception {
    Account acc = getAccountById(username, accId);
    return sendMessage(username, acc, message);
  }
  
  public Message sendMessage(String username, Account acc, Message message) throws Exception {   
	Security.setProperty("ssl.SocketFactory.provider", Utils.EXOMAIL_SSL_SOCKET_FACTORY_CLASS);
	String smtpUser = acc.getIncomingUser();
    String outgoingHost = acc.getOutgoingHost();
    String outgoingPort = acc.getOutgoingPort();
    String isSSl = acc.getServerProperties().get(Utils.SVR_OUTGOING_SSL);
    boolean isSMTPAuth = acc.isOutgoingAuthentication();
    Properties props = System.getProperties();
    String protocolName        = Utils.SVR_SMTP;
    String smtpPort            = Utils.SVR_SMTP_SOCKET_FACTORY_PORT;
    String svrSmtpUser         = Utils.SVR_SMTP_USER;
    String svrSmtpHost         = Utils.SVR_SMTP_HOST;
    String svrSmtpPort         = Utils.SVR_SMTP_PORT;
    String dsnNotify           = Utils.SMTP_DNS_NOTIFY;
    String smtpDnsRet          = Utils.SMTP_DNS_RET;
    String smtpconnecttimeout  = Utils.SMTP_CONECT_TIMEOUT;
    String smtptimeout         = Utils.SMTP_TIMEOUT;
    String smtpSocketFacFallback  = Utils.SVR_SMTP_SOCKET_FACTORY_FALLBACK;
    String smtpQuitWait           = Utils.SMTP_QUIT_WAIT;
       
    String smtpSslProtocols       = "mail.smtp.ssl.protocols";
    String smtpSsl                = Utils.MAIL_SMTP_SSL_ENABLE;
    String smtpAuth               = Utils.SVR_SMTP_AUTH;
    String smtpPropSocketFactory  = Utils.SVR_SMTP_SOCKET_FACTORY_CLASS;
    String socketFactoryClass     = Utils.SOCKET_FACTORY;
    String propSocketFact         = Utils.SMTP_SSL_FACTORY;
    
    if (Boolean.valueOf(isSSl)) {
    	protocolName = Utils.SVR_SMTPS;
    	smtpPort=Utils.SVR_SMTP_SSL_SOCKET_FACTORY_PORT;
    	//smtpPropSocketFactory = Utils.SVR_SMTP_SSL_SOCKET_FACTORY_CLASS.replace(Utils.SVR_SMTP, Utils.SVR_SMTPS);
    	socketFactoryClass = Utils.EXOMAIL_SSL_SOCKET_FACTORY_CLASS;
    	props.put(smtpSsl.replace("smtp", "smtps"), true);
    	props.put(Utils.SVR_SMTP_STARTTLS_ENABLE.replace("smtp", "smtps"), true);
    	props.put(smtpSslProtocols.replace("smtp", "smtps"), "SSLv3 TLSv1");
    	props.put(propSocketFact.replace("smtp", Utils.SVR_SMTPS), this.getSSLSocketFactory(null));
    }
    if(Utils.isGmailAccount(smtpUser) || Utils.isGmailAccount(acc.getOutgoingUserName())){
        protocolName = Utils.SVR_SMTPS;
        props.put(smtpQuitWait.replace("smtp", "smtps"), false);
        if(isSMTPAuth) props.put(smtpAuth.replace("smtp", "smtps"), true);
    }

    if(protocolName.equalsIgnoreCase(Utils.SVR_SMTP)){
        props.put(svrSmtpUser, smtpUser);
        props.put(svrSmtpHost, outgoingHost);
        props.put(svrSmtpPort, outgoingPort);
        props.put(dsnNotify, "SUCCESS,FAILURE ORCPT=rfc822;" + acc.getEmailAddress());
        props.put(smtpDnsRet, "FULL");
        props.put(smtpconnecttimeout, "0");
        props.put(smtptimeout, "10000");
        props.put(smtpPropSocketFactory, socketFactoryClass);
        props.put(smtpSocketFacFallback, false);
        props.put(smtpPort, outgoingPort);
        if (isSMTPAuth)  props.put(Utils.SVR_SMTP_AUTH, true);
        else  props.put(Utils.SVR_SMTP_AUTH, false);
      }else{
        props.put(svrSmtpUser.replace("smtp", "smtps"), smtpUser);
        props.put(svrSmtpHost.replace("smtp", "smtps"), outgoingHost);
        props.put(svrSmtpPort.replace("smtp", "smtps"), outgoingPort);
        props.put(dsnNotify.replace("smtp", "smtps"), "SUCCESS,FAILURE ORCPT=rfc822;" + acc.getEmailAddress());
        props.put(smtpDnsRet.replace("smtp", "smtps"), "FULL");
        props.put(smtpconnecttimeout.replace("smtp", "smtps"), "0");
        props.put(smtptimeout.replace("smtp", "smtps"), "10000");
        props.put(smtpPropSocketFactory.replace("smtp", "smtps"), socketFactoryClass);
        props.put(smtpSocketFacFallback.replace("smtp", "smtps"), false);
        //  props.put(smtpPort.replace("smtp", "smtps"), outgoingPort);
        if (isSMTPAuth)  props.put(smtpAuth.replace("smtp", "smtps"), true);
        else  props.put(smtpAuth.replace("smtp", "smtps"), false);
        }
        Session session = Session.getDefaultInstance(props, null);
        logger.debug(" #### Sending email ..f  ");
        if(protocolName.equalsIgnoreCase(Utils.SVR_SMTPS)) session.setProtocolForAddress("rfc822", Utils.SVR_SMTPS);
        SMTPTransport transport = (SMTPTransport)session.getTransport(protocolName);
    try {
      if (!isSMTPAuth) {
        transport.connect() ;
      } else if (acc.useIncomingSettingForOutgoingAuthent()) {
        transport.connect(outgoingHost, Integer.parseInt(outgoingPort), smtpUser, acc.getIncomingPassword());
      } else {
        transport.connect(outgoingHost, Integer.parseInt(outgoingPort), acc.getOutgoingUserName(), acc.getOutgoingPassword());
      }
    } catch(Exception ex) {
      if(logger.isDebugEnabled()) logger.debug("#### Can not connect to smtp server ...");
      throw ex;
    }
    Message msg = send(session, transport, message);
    transport.close();

    return msg;
  }

  public Message sendMessage(String username, Message message)
  throws Exception {
    return sendMessage(username, message.getAccountId(), message);
  }

  public void sendMessage(Message message) throws Exception {
    List<Message> msgList = new ArrayList<Message>();
    msgList.add(message);
    sendMessages(msgList, message.getServerConfiguration());
  }

  public void sendMessages(List<Message> msgList, ServerConfiguration serverConfig) throws Exception {
    Security.setProperty("ssl.SocketFactory.provider", Utils.EXOMAIL_SSL_SOCKET_FACTORY_CLASS);
    Properties props = System.getProperties();
    String protocolName = Utils.SVR_SMTP;
    String propSmtpPort            = Utils.SVR_SMTP_SOCKET_FACTORY_PORT;
    String smtpSslProtocols        = "mail.smtp.ssl.protocols";
    String smtpSsl                = Utils.MAIL_SMTP_SSL_ENABLE;
    String smtpAuth               = Utils.SVR_SMTP_AUTH;
    String propSocketFactoryClass =     Utils.SVR_SMTP_SOCKET_FACTORY_CLASS;
    String socketFactoryClass      =     Utils.SOCKET_FACTORY;
    boolean isSMTPAuth = serverConfig.isOutgoingAuthentication(); 
    
    if (serverConfig.isOutgoingSsl()) {
      protocolName = Utils.SVR_SMTPS;
      socketFactoryClass = Utils.EXOMAIL_SSL_SOCKET_FACTORY_CLASS;
      props.put(smtpSsl.replace("smtp", "smtps"), true);
      props.put(Utils.SVR_SMTP_STARTTLS_ENABLE.replace("smtp", "smtps"), true);
      props.put(smtpSslProtocols.replace("smtp", "smtps"), "SSLv3 TLSv1");
    }
    
    if(Utils.isGmailAccount(serverConfig.getUserName())){
      protocolName = Utils.SVR_SMTPS;
      if(isSMTPAuth) props.put(smtpAuth.replace("smtp", "smtps"), true);
    }
    
    if(protocolName.equalsIgnoreCase(Utils.SVR_SMTP)){
      props.put(Utils.SVR_INCOMING_USERNAME, serverConfig.getUserName());
      props.put(Utils.SVR_INCOMING_PASSWORD, serverConfig.getPassword());
      props.put(Utils.SVR_SMTP_USER, serverConfig.getUserName());
      props.put(Utils.SVR_SMTP_HOST, serverConfig.getOutgoingHost());
      props.put(Utils.SVR_SMTP_PORT, serverConfig.getOutgoingPort());
      props.put(propSmtpPort, serverConfig.getOutgoingPort());
      props.put(Utils.SVR_SMTP_SOCKET_FACTORY_FALLBACK, "false");
      props.put(propSocketFactoryClass, socketFactoryClass);
      if (isSMTPAuth)  props.put(Utils.SVR_SMTP_AUTH, true);
      else  props.put(Utils.SVR_SMTP_AUTH, false);
    }else{
      props.put(Utils.SVR_INCOMING_USERNAME, serverConfig.getUserName());
      props.put(Utils.SVR_INCOMING_PASSWORD, serverConfig.getPassword());
      props.put(Utils.SVR_SMTP_USER.replace("smtp", "smtps"), serverConfig.getUserName());
      props.put(Utils.SVR_SMTP_HOST.replace("smtp", "smtps"), serverConfig.getOutgoingHost());
      props.put(Utils.SVR_SMTP_PORT.replace("smtp", "smtps"), serverConfig.getOutgoingPort());
     // props.put(propSmtpPort.replace("smtp", "smtps"), serverConfig.getOutgoingPort());
      props.put(Utils.SVR_SMTP_SOCKET_FACTORY_FALLBACK.replace("smtp", "smtps"), "false");
      props.put(propSocketFactoryClass.replace("smtp", "smtps"), socketFactoryClass);
      if (isSMTPAuth)  props.put(Utils.SVR_SMTP_AUTH.replace("smtp", "smtps"), true);
      else  props.put(Utils.SVR_SMTP_AUTH.replace("smtp", "smtps"), false);
    }
 
    Session session = Session.getDefaultInstance(props, null);
    if(protocolName.equalsIgnoreCase(Utils.SVR_SMTPS)) session.setProtocolForAddress("rfc822", Utils.SVR_SMTPS);
    Transport transport = session.getTransport(protocolName);
    try {
      if (!isSMTPAuth) {
        transport.connect() ;
      } else {
        transport.connect(serverConfig.getOutgoingHost(), serverConfig.getUserName(), serverConfig
          .getPassword());
      }
    } catch(Exception e) {
      try {
        transport.connect() ;
      } catch(Exception ex) {
        logger.debug("#### Can not connect to smtp server ...") ;
        return ;
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
      mimeMessage.setRecipients(javax.mail.Message.RecipientType.TO, InternetAddress.parse(message
          .getMessageTo()));

    if (message.getMessageCc() != null)
      mimeMessage.setRecipients(javax.mail.Message.RecipientType.CC, InternetAddress.parse(message
          .getMessageCc(), true));

    if (message.getMessageBcc() != null)
      mimeMessage.setRecipients(javax.mail.Message.RecipientType.BCC, InternetAddress.parse(message
          .getMessageBcc(), false));

    if (message.getReplyTo() != null)
      mimeMessage.setReplyTo(Utils.getInternetAddress(message.getReplyTo()));

    mimeMessage.setSubject(message.getSubject(), "UTF-8");
    mimeMessage.setSentDate(message.getSendDate());

    MimeMultipart multipPartRoot = new MimeMultipart("mixed");

    MimeMultipart multipPartContent = new MimeMultipart("alternative");

    List<Attachment> attachList = message.getAttachments();
    if (attachList != null && attachList.size() != 0) {
      MimeBodyPart contentPartRoot = new MimeBodyPart();
      if (message.getContentType() != null && message.getContentType().indexOf("text/plain") > -1)
        contentPartRoot.setContent(message.getMessageBody(), "text/plain; charset=utf-8");
      else
        contentPartRoot.setContent(message.getMessageBody(), "text/html; charset=utf-8");

      MimeBodyPart mimeBodyPart1 = new MimeBodyPart();
      mimeBodyPart1.setContent(message.getMessageBody(), message.getContentType());
      multipPartContent.addBodyPart(mimeBodyPart1);
      multipPartRoot.addBodyPart(contentPartRoot);

      for (Attachment att : attachList) {
        InputStream is = att.getInputStream();
        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        ByteArrayDataSource byteArrayDataSource = new ByteArrayDataSource(is, att.getMimeType());
        mimeBodyPart.setDataHandler(new DataHandler(byteArrayDataSource));

        mimeBodyPart.setDisposition(Part.ATTACHMENT);
        mimeBodyPart.setFileName(MimeUtility.encodeText(att.getName(), "utf-8", null));
        multipPartRoot.addBodyPart(mimeBodyPart);
      }
      mimeMessage.setContent(multipPartRoot);
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
    mimeMessage.saveChanges();
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
      // logger.debug(" #### Info : " + status);
    }
    logger.debug(" #### Info : " + status);

    return message;
  }

  public void checkMail(String username, String accountId) throws Exception {

    JobDetail job = loadCheckmailJob(username, accountId);
    
    // trigger now
    if (job != null) {
      schedulerService_.executeJob(job.getName(), job.getGroup(), job.getJobDataMap());
    }
  }
  
  public void checkMail(String username, String accountId, String folderId) throws Exception {
    if (Utils.isEmptyField(folderId)) checkMail(username, accountId);
    else {
      JobDetail job = loadCheckmailJob(username, accountId, folderId);

      // trigger now
      if (job != null) {
        schedulerService_.executeJob(job.getName(), job.getGroup(), job.getJobDataMap());
      }
    }
  }
  
  public void stopCheckMail(String username, String accountId)  {
    CheckingInfo checkingInfo = getCheckingInfo(username, accountId);
    if (checkingInfo != null) {
      checkingInfo.setRequestStop(true);
      System.out.println("Requested check loop to stop ");
    } 
  }
  
  public void stopAllJobs(String username, String accountId) throws Exception {
    JobInfo info = CheckMailJob.getJobInfo(username, accountId);
    stopCheckMail(username, accountId);
    schedulerService_.removeJob(info);
  }

  private JobDetail loadCheckmailJob(String username, String accountId) throws Exception {
    return loadCheckmailJob(username, accountId, "");
  }
  
  /**
   * Load or register the CheckMailJob against scheduler
   * @return
   * @throws Exception 
   */
  private JobDetail loadCheckmailJob(String username, String accountId, String folderId) throws Exception {

    JobInfo info = CheckMailJob.getJobInfo(username, accountId);
    JobDetail job = findCheckmailJob(username, accountId);

    if (job == null) {
      JobDataMap jobData = new JobDataMap();
      jobData.put(CheckMailJob.USERNAME, username);
      jobData.put(CheckMailJob.ACCOUNTID, accountId);     
      jobData.put(CheckMailJob.FOLDERID, folderId);
      PeriodInfo periodInfo = new PeriodInfo(new GregorianCalendar().getTime(), null, 1, 24*60*60*1000);
      schedulerService_.addPeriodJob(info, periodInfo, jobData);
      
    }
    return job;
  }

  private JobDetail findCheckmailJob(String username,
                                     String accountId) throws Exception {
    // TODO current implementation is inefficient
    /// Need to upgrade to 2.0.3 and use this instead : 
    //schedulerService_.getJob(info) 
    for (Object obj : schedulerService_.getAllJobs()) {
      JobDetail tmp = (JobDetail) obj;
      if (tmp.getName().equals(username + ":" + accountId)) {
        return tmp;
      }
    }
    return null;
  }
  
  private LinkedHashMap<javax.mail.Message, List<String>> getMessages(LinkedHashMap<javax.mail.Message, List<String>> msgMap, 
      javax.mail.Folder folder, boolean isImap, Date fromDate, Date toDate, List<MessageFilter> filters) throws Exception {
    javax.mail.Message[] messages ;
    SearchTerm searchTerm = null;
    
    if (fromDate != null && toDate != null && fromDate.equals(toDate)) return msgMap;
    
    if (fromDate == null && toDate == null) { 
      messages = folder.getMessages();
    } else {
      searchTerm = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
      SearchTerm dateTerm = null ;
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
      if (!isImap) searchTerm = new OrTerm(searchTerm, dateTerm);
      else searchTerm = dateTerm ;
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
          for (int k = 0; k < filteredMsgNumber ; k++) { 
            if (MimeMessageParser.getReceivedDate(filteredMsg[k]).getTime().before(fromDate)) {
              getFrom++ ;
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
          
          if (filterList == null) filterList = new ArrayList<String>();
          
          if (!filterList.contains(filter.getId())) filterList.add(filter.getId());
          
          if (fromDate != null && toDate != null) {
            if (betweenTime || (!(isImap && !MimeMessageParser.getReceivedDate(msg).getTime().before(toDate)))) {
              betweenTime = true ;
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
            if (beforeTime || !(isImap && !MimeMessageParser.getReceivedDate(msg).getTime().before(toDate))) {
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
      Date date ;
      if (fromDate != null) { 
        for (int l = 0; l < messages.length ; l++) { 
          date = MimeMessageParser.getReceivedDate(messages[l]).getTime();
          if (date.before(fromDate) || date.equals(fromDate)) {
            getFrom++ ;
          } else {
            break;
          }
        }
      }
      
      for (int l = messages.length ; l > 0; l--) {
        msg = messages[l-1];
        if (!msgMap.containsKey(msg)) {
          if (fromDate != null && toDate != null) {
            if (betweenTime || (!(isImap && !MimeMessageParser.getReceivedDate(msg).getTime().before(toDate)))) {
              betweenTime = true ;
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
            if (beforeTime || !(isImap && !MimeMessageParser.getReceivedDate(msg).getTime().before(toDate))) {
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
    return msgMap ;
  }
  
  public synchronized void synchImapFolders(String username, String accountId) throws Exception {
    CheckingInfo info = new CheckingInfo();
    String key = username + ":" + accountId;
    if(checkingLog_ == null) checkingLog_ = new HashMap<String, CheckingInfo>();
    checkingLog_.put(key, info);
    IMAPStore store = null;
    try {
	    Account account = getAccountById(username, accountId);
	    store = openIMAPConnection(username, account, info);  
	    if (store != null) {
	      checkingLog_.get(key).setSyncFolderStatus(CheckingInfo.START_SYNC_FOLDER);
	      synchImapFolders(username, accountId, null, store.getDefaultFolder().list());
	      checkingLog_.get(key).setSyncFolderStatus(CheckingInfo.FINISH_SYNC_FOLDER);
	    }
    } finally {
		if (store != null && store.isConnected()) {
			store.close();
		}
	}
  }
  
  private synchronized List<javax.mail.Folder> synchImapFolders(String username, String accountId, Folder parentFolder, javax.mail.Folder[] folders) throws Exception {
    List<javax.mail.Folder> folderList = new ArrayList<javax.mail.Folder>();
    List<String> serverFolderId = new ArrayList<String>();
    String folderId, folderName;
    Folder folder;
    for (javax.mail.Folder fd : folders) {
      folderName = fd.getName();
      if (parentFolder == null && (folderName.equalsIgnoreCase(Utils.FD_TRASH) || folderName.equalsIgnoreCase(Utils.FD_DRAFTS) ||
          folderName.equalsIgnoreCase(Utils.FD_SENT) || folderName.equalsIgnoreCase(Utils.FD_SPAM))) {
       continue;
      }
      if (!fd.getName().equalsIgnoreCase(Utils.FD_INBOX)) {        
        if (fd.getType() != javax.mail.Folder.HOLDS_FOLDERS) {
          folderId = Utils.generateFID(accountId, String.valueOf(((IMAPFolder) fd).getUIDValidity()), true);
        } else {
          folderId = Utils.escapeIllegalJcrChars(folderName);
        } 
        serverFolderId.add(folderId);
        folder = storage_.getFolder(username, accountId, folderId);
        if (folder == null) {
          folder = new Folder();
          folder.setId(folderId);
          folder.setName(folderName);
          folder.setURLName(fd.getURLName().toString());
          folder.setNumberOfUnreadMessage(0);
          folder.setTotalMessage(0);
          folder.setPersonalFolder(true);
          folder.setType(fd.getType());
          try {
            if (parentFolder == null) {
              storage_.saveFolder(username, accountId, folder);
            } else {
              storage_.saveFolder(username, accountId, parentFolder.getId(), folder);
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
          
        } else if (folder.getName().equalsIgnoreCase(fd.getName())) {  // update available one
          folder.setName(fd.getName());
          folder.setURLName(fd.getURLName().toString());
          
          saveFolder(username, accountId, folder, false);
        }
        
        List<Folder> localFolders = new ArrayList<Folder>();
        if (parentFolder == null) {
          localFolders = getFolders(username, accountId, true);
        } else {
          localFolders = getSubFolders(username, accountId, parentFolder.getPath());
        }
        
        for (Folder f : localFolders) {
          if (!serverFolderId.contains(f.getId())) deleteLocalFolder(username, accountId, f.getId());
        }

        folderList.add(fd);
        if ((fd.getType() == 2) || (fd.list().length > 0)) {
          folderList.addAll(synchImapFolders(username, accountId, getFolder(username, accountId, folderId), fd.list()));
        }
      } else {
        Folder inbox = getFolder(username, accountId, Utils.generateFID(accountId, Utils.FD_INBOX, false));
        inbox.setURLName(fd.getURLName().toString());
        saveFolder(username, accountId, inbox, false);
      }
    }      
    
    return folderList ;
  }
  
  private void saveFolder(String username, String accountId, Folder parentFolder, javax.mail.Folder serverFolder) throws Exception {
    String folderId;
    Folder folder;     
    if (serverFolder.getType() != javax.mail.Folder.HOLDS_FOLDERS) {
      folderId = Utils.generateFID(accountId, String.valueOf(((IMAPFolder) serverFolder).getUIDValidity()), true);
    } else {
      folderId = Utils.escapeIllegalJcrChars(serverFolder.getName());
    } 
    folder = storage_.getFolder(username, accountId, folderId);
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
          storage_.saveFolder(username, accountId, folder);
        } else {
          storage_.saveFolder(username, accountId, parentFolder.getId(), folder);
        }
      } catch (Exception e) {
        e.printStackTrace();
      } 
    }
  }  
  
  public SSLSocketFactory getSSLSocketFactory(String protocol){
    ExoMailSSLSocketFactory sslSocketFact = new ExoMailSSLSocketFactory(protocol);
    return sslSocketFact.getSSLSocketFactory();
  }
  
  public IMAPStore openIMAPConnection(String username, Account account) {
    return openIMAPConnection(username, account, null);
  }
  
  public IMAPStore openIMAPConnection(String username, Account account, CheckingInfo info) {
    try {
      logger.debug(" #### Getting mail from " + account.getIncomingHost() + " ... !");
      Properties props = System.getProperties();
      String socketFactoryClass =  Utils.SOCKET_FACTORY;
      String host = account.getIncomingHost();
      int port =  Integer.valueOf(account.getIncomingPort());
      String fallback = Utils.IMAP_SOCKETFACTORY_FALLBACK;
      String imapPropSocketFactoryClass = Utils.IMAP_SOCKET_FACTORY_CLASS;;
      String emailAddress = account.getIncomingUser();
      String protocolName = Utils.SVR_IMAP;
      String propSocketFact = Utils.IMAP_SSL_FACTORY;

      props.put("mail.mime.base64.ignoreerrors", "true");
      
      if (account.isIncomingSsl()) {
        socketFactoryClass = Utils.EXOMAIL_SSL_SOCKET_FACTORY_CLASS;
        //imapPropSocketFactoryClass = Utils.IMAP_SSL_SOCKET_FACTORY_CLASS;
        props.put(propSocketFact, this.getSSLSocketFactory(null));//default protocol is TLS 
        props.put(Utils.MAIL_IMAP_SSL_ENABLE, true);
       // props.put(Utils.IMAP_STARTTLS_ENABLE, true);
        props.put(fallback, false);
      }
      if(Utils.isGmailAccount(emailAddress)) protocolName = Utils.SVR_IMAPS;
      props.put(imapPropSocketFactoryClass, socketFactoryClass);
      
      Session session = Session.getInstance(props, null);

      IMAPStore imapStore = (IMAPStore) session.getStore(protocolName);
      try {
        imapStore.connect(host, port, emailAddress, account.getIncomingPassword());
      } catch (AuthenticationFailedException e) {
        if (!account.isSavePassword()) {   // about remember password, in the first time get email.
          account.setIncomingPassword("");
          updateAccount(username, account);
          logger.debug("Exception while connecting to server : " + e.getMessage());
        }
        if (info != null) {
          info.setStatusMsg("The username or password may be wrong.");
          info.setStatusCode(CheckingInfo.RETRY_PASSWORD);
        }
        return null;
      } catch (MessagingException e) {
        logger.debug("Exception while connecting to server : " + e.getMessage());
        if (info != null) {
          info.setStatusMsg("Connecting failed. Please check server configuration.");
          info.setStatusCode(CheckingInfo.CONNECTION_FAILURE);
        }
        return null;
      } catch (Exception e) {
        logger.debug("Exception while connecting to server : " + e.getMessage());
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        StringBuffer sb = sw.getBuffer();
        logger.error(sb.toString());
        if (info != null) {
          info.setStatusMsg("There was an unexpected error. Connecting failed.");
          info.setStatusCode(CheckingInfo.CONNECTION_FAILURE);
        }
        return null;
      }
      
      return imapStore ;
    } catch (Exception ex) {
      System.out.println("Cannot connect to the server, please check your configuration ! ");
      ex.printStackTrace();
      return null;
    }
  }
  
  private synchronized void getSynchnizeImapServer(String username, String accountId, String folderId, boolean synchFolders) throws Exception {
    CheckingInfo info = new CheckingInfo();
    String key = username + ":" + accountId;
    if(checkingLog_ == null)
      checkingLog_ = new HashMap<String, CheckingInfo>();
    checkingLog_.put(key, info);
    
    Account account = getAccountById(username, accountId);
    IMAPStore store = openIMAPConnection(username, account, info);  
    
    if (store != null) {
      List<javax.mail.Folder> folderList = new ArrayList<javax.mail.Folder>();
      if (synchFolders) {
        info.setSyncFolderStatus(CheckingInfo.START_SYNC_FOLDER);
        info.setStatusMsg("Synchronizing imap folder ...");
        folderList = synchImapFolders(username, accountId, null, store.getDefaultFolder().list());
        info.setSyncFolderStatus(CheckingInfo.FINISH_SYNC_FOLDER);
        info.setStatusMsg("Finished synchronizing imap folder ...");
      }
      if (!Utils.isEmptyField(folderId)) {
        javax.mail.Folder fd = null;
        Folder f = getFolder(username, accountId, folderId);
        if (f == null || Utils.isEmptyField(f.getURLName())) {
          f = getFolder(username, accountId, Utils.generateFID(accountId, Utils.FD_INBOX, false));
        }
        try {
          if (f != null && !Utils.isEmptyField(f.getURLName())) {
            URLName url = new URLName(f.getURLName());
            fd = store.getFolder(url);
            if (fd != null) {
              synchImapMessage(username, accountId, fd, key);
            }
          }  
        } catch (Exception e) { 
        } finally {
          checkingLog_.get(key).setStatusCode(CheckingInfo.FINISHED_CHECKMAIL_STATUS);    
          removeCheckingInfo(username, accountId);
          store.close();
        }
      } else {
        for (javax.mail.Folder folder : folderList) {
          if (!Utils.isEmptyField(info.getRequestingForFolder_()) && !info.getRequestingForFolder_().equals("checkall")) {          
            break;
          } 
          if(info.isRequestStop()) {
            if (logger.isDebugEnabled()) {
              logger.debug("Stop requested on checkmail for " + account.getId());
            }
            break;
          }
          try {
            synchImapMessage(username, accountId, folder, key);
          } catch (MessagingException e){
            System.err.println("Failed to open '" + folder.getName() + "' folder as read-only");
            checkingLog_.get(key).setStatusCode(CheckingInfo.FINISHED_CHECKMAIL_STATUS);    
            removeCheckingInfo(username, accountId);
            e.printStackTrace();
          }
        }
        checkingLog_.get(key).setStatusCode(CheckingInfo.FINISHED_CHECKMAIL_STATUS);    
        removeCheckingInfo(username, accountId);
      }
      if (!account.isSavePassword()) account.setIncomingPassword("");
      updateAccount(username, account);
      logger.debug("/////////////////////////////////////////////////////////////");
      logger.debug("/////////////////////////////////////////////////////////////");
    }
  }
  
  private synchronized void synchImapMessage(String username, String accountId, javax.mail.Folder folder, String key) throws Exception {
    Account account = getAccountById(username, accountId) ;
    boolean saved = false ;
    int totalNew = -1;
    Info infoObj = new Info();
    ContinuationService continuation = Utils.getContinuationService();
    if (folder == null) return;
    try {
      folder.open(javax.mail.Folder.READ_WRITE);
      if(checkingLog_ == null) checkingLog_ = new HashMap<String, CheckingInfo>();
      logger.debug(" #### Getting mails from folder " + folder.getName() + " !");
      checkingLog_.get(key).setSyncFolderStatus(CheckingInfo.FINISHED_SYNC_FOLDER);
      checkingLog_.get(key).setStatusMsg("Getting mails from folder " + folder.getName() + " !");
      
      String folderId = Utils.generateFID(accountId, String.valueOf(((IMAPFolder) folder).getUIDValidity()), true);
      if (folder.getName().equalsIgnoreCase(Utils.FD_INBOX)) folderId = Utils.generateFID(accountId, Utils.FD_INBOX, false);
      
      Folder eXoFolder = getFolder(username, accountId, folderId);
      
      Date lastCheckedDate = eXoFolder.getLastCheckedDate();
      Date checkFromDate = eXoFolder.getCheckFromDate();
      Date accCheckFromDate = account.getCheckFromDate();
      Date date1 = null, date2 = null;
      LinkedHashMap<javax.mail.Message, List<String>> msgMap = new LinkedHashMap<javax.mail.Message, List<String>>();
      
      if (accCheckFromDate == null) { // if do not limit time
        date2 = checkFromDate;
      } else { 
        if (lastCheckedDate == null) { // if it's the first time, user checks mail messages.
          lastCheckedDate = accCheckFromDate;
        } else {
          if (checkFromDate != null && accCheckFromDate.before(checkFromDate)) { // if new checking time in account setting is set before current starting time.
            date1 = accCheckFromDate;
            date2 = checkFromDate;
          } else if (accCheckFromDate.after(lastCheckedDate)) { // if new checking time in account setting is set after last checking message time.
            lastCheckedDate = accCheckFromDate;
          }
        }
      }
      eXoFolder.setCheckFromDate(accCheckFromDate);
      
      msgMap = getMessageMap(username, accountId, folder, date1, date2, lastCheckedDate);
      /* get the moment that finish checking mail */
      if (eXoFolder.getLastCheckedDate() == null || eXoFolder.getLastCheckedDate().before(Calendar.getInstance().getTime()))
        eXoFolder.setLastCheckedDate(new Date(System.currentTimeMillis()));

      boolean isImap = account.getProtocol().equals(Utils.IMAP); 
      boolean leaveOnserver = (isImap && Boolean.valueOf(account.getServerProperties().get(Utils.SVR_LEAVE_ON_SERVER)));
      
      totalNew = msgMap.size();

      logger.debug(" #### Folder " + folder.getName() + " contains " + totalNew + " messages !");

      if (totalNew > 0) {        
        int i = 0; long msgUID;
        String folderStr;
        javax.mail.Message msg;
        String[] folderIds ;
        List<String> filterList, folderList, tagList;;
        MessageFilter filter;
        
//        Date lastFromDate = null, receivedDate = null;
        List<javax.mail.Message> msgList = new ArrayList<javax.mail.Message>(msgMap.keySet()) ;
        SpamFilter spamFilter = getSpamFilter(username, account.getId());
        
        while (i < totalNew) {
          if(checkingLog_.get(key).isRequestStop()) {
            if (logger.isDebugEnabled()) {
              logger.debug("Stop requested on checkmail for " + account.getId());
            }
            break;
          }
          if (!Utils.isEmptyField(checkingLog_.get(key).getRequestingForFolder_()) && 
              !String.valueOf(((IMAPFolder) folder).getUIDValidity()).equals(Utils.getFolderNameFromFolderId(checkingLog_.get(key).getRequestingForFolder_()))) {
            break;
          }
           
          folderIds =  new String[]{ folderId };
          msg = msgList.get(i);
          
          checkingLog_.get(key).setFetching(i + 1);
          checkingLog_.get(key).setStatusMsg("Synchronizing  " + folder.getName()+ " : " + (i + 1) + "/" + totalNew);
          
          filterList = msgMap.get(msg);
          try {            
            folderList = new ArrayList<String>();
            tagList = new ArrayList<String>();
            if (filterList != null && filterList.size() > 0) {
              String tagId;
              for (int j = 0; j < filterList.size(); j++) {
                filter = getFilterById(username, accountId, filterList.get(j));
                folderList.add(filter.getApplyFolder());
                tagId = filter.getApplyTag();
                if (tagId != null && tagId.trim().length() > 0) tagList.add(tagId);
              }
              folderIds = folderList.toArray(new String[] {});
            }
            
            folderStr = "";
            for (int k = 0; k < folderIds.length; k++) {
              folderStr += folderIds[k] + ",";
            }
            infoObj.setFolders(folderStr);
            
            msgUID = ((IMAPFolder)folder).getUID(msg);
            saved = storage_.saveMessage(username, accountId, msgUID, msg, folderIds, tagList, spamFilter, infoObj, continuation, false);
            
            if (saved && !leaveOnserver) msg.setFlag(Flags.Flag.DELETED, true);       
              
//            receivedDate = MimeMessageParser.getReceivedDate(msg).getTime();

//            if (i == 0) lastFromDate = receivedDate;                  
//            eXoFolder.setLastCheckedDate(receivedDate);
//            if ((i == (totalNew - 1))) eXoFolder.setCheckFromDate(lastFromDate);
            
//            if (lastFromDate != null && (eXoFolder.getLastStartCheckingTime() == null || eXoFolder.getLastStartCheckingTime().before(lastFromDate))) {
//              eXoFolder.setLastStartCheckingTime(lastFromDate);
//            }
          } catch (Exception e) {
            i++;
            continue;
          }
          i++;
        }

        saveFolder(username, accountId, eXoFolder, false);
        
        FetchMailContentThread downloadContentMail = new FetchMailContentThread(storage_, msgMap, i, folder, username, accountId);
        new Thread(downloadContentMail).start();        
      } else {
        saveFolder(username, accountId, eXoFolder, false);
      }
      checkingLog_.get(key).setStatusMsg("Finished download for " + folder.getName() + " folder.");
      logger.debug("#### Synchronization finished for " + folder.getName() + " folder.");

    } catch (Exception e) {
      logger.error("Error while checking emails from folder " + folder.getName() + " of username " + username + " on account " + accountId, e);
    }
  }
  
  /**
   * get map of message and its filter list. <br />
   * Actions of this function: <br />
   * <i>action 1:</i> get messages from {date3} to now. <br>
   * <i>action 2:</i> get messages from {date1} to {date2}. <br><br>
   * Example: <br>
   * --------[date1]*********[date2]--------[date3]**********[now]
   * @param username
   * @param accountId
   * @param folder - mail folder
   * @param date1 if it is null, time is not limited.
   * @param date2 if it is null,  action 2 is <b>not</b> executed.  
   * @param date3 if it is null, time is not limited. All suitable messages are get. 
   * @return list of message. return empty map if no action is executed.
   * @throws Exception
   */
  private LinkedHashMap<javax.mail.Message, List<String>> getMessageMap(String username, String accountId, javax.mail.Folder folder, Date date1, 
                                                                        Date date2, Date date3) throws Exception{
    LinkedHashMap<javax.mail.Message, List<String>> msgMap = new LinkedHashMap<javax.mail.Message, List<String>>();
    List<MessageFilter> filters = getFilters(username, accountId);
    
    msgMap = getMessages(msgMap, folder, true, date3, null, filters);
    
    if (date3 != null && date2 != null) { // if date3 == null, all messages are get, we don't need run below block.
      msgMap = getMessages(msgMap, folder, true, date1, date2, filters);
    }
    return msgMap;
  }
  
  public List<Message> checkNewMessage(String username, String accountId) throws Exception {
    return checkNewMessage(username, accountId, null);
  }
  
  public List<Message> checkNewMessage(String username, String accountId, String folderId) throws Exception {
    Account account = getAccountById(username, accountId);
    List<Message> messageList = new ArrayList<Message>();
    if(account != null) {
      boolean isPop3 = account.getProtocol().equals(Utils.POP3);
      boolean isImap = account.getProtocol().equals(Utils.IMAP);
      if (isPop3) {
        return checkPop3Server(username, accountId);
      } else if (isImap) {
        boolean synchFolder = !(getFolders(username, accountId, true).size() > 0);
        getSynchnizeImapServer(username, accountId, folderId, synchFolder);
      }
    }
    return messageList ;
  }
  
  //TODO: refactor code for checking mail from POP3 server.
  public synchronized List<Message> checkPop3Server(String username, String accountId) throws Exception {
    Account account = getAccountById(username, accountId);
    List<Message> messageList = new ArrayList<Message>();
    if(checkingLog_ == null)
      checkingLog_ = new HashMap<String, CheckingInfo>();
    if(account != null) {
      CheckingInfo info = new CheckingInfo();
      String key = username + ":" + accountId;
      if(checkingLog_ != null) checkingLog_.put(key, info);
      long t1, t2, tt1, tt2;
      if (Utils.isEmptyField(account.getIncomingPassword()))
        info.setStatusCode(CheckingInfo.RETRY_PASSWORD);

      logger.debug(" #### Getting mail from " + account.getIncomingHost() + " ... !");
      info.setStatusMsg("Getting mail from " + account.getIncomingHost() + " ... !");
      int totalNew = 0;
      String protocol = account.getProtocol();
      boolean isImap = account.getProtocol().equals(Utils.IMAP); 
      String protocolName = "pop3";
      String emailAddr = account.getIncomingUser();
      String propSocketFactoryClass = Utils.POP3_SOCKET_FACTORY_CLASS;
      String socketFactoryClass = Utils.SOCKET_FACTORY;
      
      if(Utils.isGmailAccount(emailAddr)) protocolName = Utils.POP3S;
      
      try {
        Properties props = System.getProperties();
        if (protocol.equals(Utils.POP3)) {
          if (account.isIncomingSsl()) {
            //propSocketFactoryClass = Utils.POP3_SSL_SOCKET_FACTORY_CLASS;
            socketFactoryClass = Utils.EXOMAIL_SSL_SOCKET_FACTORY_CLASS;
            props.put(Utils.POP3_SSL_SOCKET_FACTORY_FALLBACK, false);         
            props.put(Utils.POP3_SSL_ENABLE, true);
            props.put(Utils.POP3_SSL_SOCKET_FACTORY, this.getSSLSocketFactory(null));
            props.put(Utils.POP3_STARTTLS_ENABLE, true);
          }
          props.put("mail.mime.base64.ignoreerrors", true);
          props.put("mail.pop3.host", account.getIncomingHost());
          props.put("mail.pop3.user", account.getIncomingUser());
          props.put("mail.pop3.port", account.getIncomingPort());
          props.put("mail.pop3.auth", "true");
          props.put("mail.pop3.socketFactory.port", account.getIncomingPort());
          props.put(propSocketFactoryClass, socketFactoryClass);
        }

        Session session = Session.getInstance(props, null);
        String[] incomingFolders = account.getIncomingFolder().split(",");

        // Later : the part inside of this loop should be in a separated method
        for (String incomingFolder : incomingFolders) {
          incomingFolder = incomingFolder.trim();
          URLName storeURL = new URLName(account.getProtocol(), account.getIncomingHost(), Integer
              .valueOf(account.getIncomingPort()), incomingFolder, account.getIncomingUser(), account
              .getIncomingPassword());
          //Store store = session.getStore(storeURL);
          Store store = session.getStore(protocolName);
          try {
            // Later : so for each more folder you need to connect again :-)
            store.connect(account.getIncomingHost(),
                          Integer.valueOf(account.getIncomingPort()),
                          emailAddr,
                          account.getIncomingPassword());
          } catch (AuthenticationFailedException e) {
            if (!account.isSavePassword()) {   // about remember password, in the first time get email.
              account.setIncomingPassword("");
              updateAccount(username, account);
              logger.debug("Exception while connecting to server : " + e.getMessage());
            }
            info.setStatusMsg("The username or password may be wrong.");
            info.setStatusCode(CheckingInfo.RETRY_PASSWORD);
            return messageList;
          } catch (MessagingException e) {
            if(Utils.isGmailAccount(emailAddr)) logger.warn("You are using Gmail account and certain that your mail account set incoming/outgoing protocol is SSL");
            logger.debug("Exception while connecting to server : " + e.getMessage());
            info.setStatusMsg("Connecting failed. Please check server configuration.");
            info.setStatusCode(CheckingInfo.CONNECTION_FAILURE);
            return messageList;
          } catch (Exception e) {
            logger.debug("Exception while connecting to server : " + e.getMessage());
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            StringBuffer sb = sw.getBuffer();
            logger.error(sb.toString());
            info.setStatusMsg("There was an unexpected error. Connecting failed.");
            info.setStatusCode(CheckingInfo.CONNECTION_FAILURE);
            return messageList;
          }

          javax.mail.Folder folder = store.getFolder(storeURL.getFile());
          if (!folder.exists()) {
            logger.debug(" #### Folder " + incomingFolder + " is not exists !");
            info.setStatusMsg("Folder " + incomingFolder + " is not exists");
            store.close();
            continue;
          } else {
            logger.debug(" #### Getting mails from folder " + incomingFolder + " !");
            info.setStatusMsg("Getting mails from folder " + incomingFolder + " !");
          }
          folder.open(javax.mail.Folder.READ_WRITE);

          Date lastCheckedDate = account.getLastCheckedDate();
          Date lastCheckedFromDate = account.getLastStartCheckingTime();
          Date checkFromDate = account.getCheckFromDate();
          
          List<MessageFilter> filters = getFilters(username, accountId);
          LinkedHashMap<javax.mail.Message, List<String>> msgMap = new LinkedHashMap<javax.mail.Message, List<String>>();
          
          if (checkFromDate == null) {
            if (lastCheckedDate != null && lastCheckedFromDate != null) {
              msgMap = getMessages(msgMap, folder, isImap, lastCheckedFromDate, null, filters);
              msgMap = getMessages(msgMap, folder, isImap, null, lastCheckedDate, filters);
            } else if (lastCheckedFromDate != null) {
              msgMap = getMessages(msgMap, folder, isImap, lastCheckedFromDate, null, filters);
            } else if (lastCheckedDate != null) {
              msgMap = getMessages(msgMap, folder, isImap, null, lastCheckedDate, filters);
            }  else {
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
            }  else {
              msgMap = getMessages(msgMap, folder, isImap, checkFromDate, null, filters);
            }
          }
          
          totalNew = msgMap.size();

          logger.debug("=============================================================");
          logger.debug("=============================================================");
          logger.debug(" #### Folder contains " + totalNew + " messages !");

          tt1 = System.currentTimeMillis();
          boolean saved = false ;

          if (totalNew > 0) {
            boolean leaveOnServer = (Boolean.valueOf(account.getServerProperties().get(
                Utils.SVR_LEAVE_ON_SERVER)));

            info.setTotalMsg(totalNew);
            
            int i = 0;
            javax.mail.Message msg;
            List<String> filterList;
            MessageFilter filter;
            String folderStr;
            Date lastFromDate = null;
            Date receivedDate = null;
            List<String> folderList, tagList;
            List<javax.mail.Message> msgList = new ArrayList<javax.mail.Message>(msgMap.keySet()) ;
            SpamFilter spamFilter = getSpamFilter(username, account.getId());
            String folderId = makeStoreFolder(username, accountId, incomingFolder);
            
            while (i < totalNew) {
              if(info.isRequestStop()) {
                if (logger.isDebugEnabled()) {
                  logger.debug("Stop requested on checkmail for " + account.getId());
                }
                break;
              }
              
              msg = msgList.get(i);
              
              logger.debug("Fetching message " + (i + 1) + " ...");
              if(checkingLog_ != null){
                checkingLog_.get(key).setFetching(i + 1);
                checkingLog_.get(key).setStatusMsg("Fetching message " + (i + 1) + "/" + totalNew);
              }
              t1 = System.currentTimeMillis();
              
              filterList = msgMap.get(msg);
              try {
                String[] folderIds = { folderId };
                folderList = new ArrayList<String>();
                tagList = new ArrayList<String>();
                if (filterList != null && filterList.size() > 0) {
                  String tagId;
                  for (int j = 0; j < filterList.size(); j++) {
                    filter = getFilterById(username, accountId, filterList.get(j));
                    folderList.add(filter.getApplyFolder());
                    tagId = filter.getApplyTag();
                    if (tagId != null && tagId.trim().length() > 0) tagList.add(tagId);
                  }
                  folderIds = folderList.toArray(new String[] {});
                }
                
                saved = storage_.savePOP3Message(username, accountId, msg, folderIds, tagList, spamFilter, null, null);
                
                if (saved) {
                  msg.setFlag(Flags.Flag.SEEN, true);
                  if (!leaveOnServer) msg.setFlag(Flags.Flag.DELETED, true);
                  
                  folderStr = "";
                  for (int k = 0; k < folderIds.length; k++) {
                    folderStr += folderIds[k] + ",";
                  }
                  if(checkingLog_ != null){
                    checkingLog_.get(key).setFetchingToFolders(folderStr);
                    checkingLog_.get(key).setMsgId(MimeMessageParser.getMessageId(msg));
                  }
                }
                
                receivedDate = MimeMessageParser.getReceivedDate(msg).getTime();
                if (i == 0) lastFromDate = receivedDate;                  
                account.setLastCheckedDate(receivedDate);
                if ((i == (totalNew - 1))) account.setCheckFromDate(lastFromDate);
                
                if (lastFromDate != null && (account.getLastStartCheckingTime() == null || account.getLastStartCheckingTime().before(lastFromDate))) {
                  account.setLastStartCheckingTime(lastFromDate);
                }
              } catch (Exception e) {
                if(checkingLog_ != null) checkingLog_.get(key).setStatusMsg("An error occurs while fetching messsge " + (i + 1));
                e.printStackTrace();
                i++;
                continue;
              }
              i++;
              t2 = System.currentTimeMillis();
              logger.debug("Message " + i + " saved : " + (t2 - t1) + " ms");
            }

            tt2 = System.currentTimeMillis();
            logger.debug(" ### Check mail finished total took: " + (tt2 - tt1) + " ms");
          }

          if (!account.isSavePassword()) account.setIncomingPassword("");
          updateAccount(username, account);

          folder.close(true);
          store.close();
          if (totalNew == 0) {
            info.setStatusMsg("There is no new messages !");
          } else {
            info.setStatusMsg("Check mail finished !");
          }
        }
        info.setStatusCode(CheckingInfo.FINISHED_CHECKMAIL_STATUS);
        
        removeCheckingInfo(username, accountId);
       
        logger.debug("/////////////////////////////////////////////////////////////");
        logger.debug("/////////////////////////////////////////////////////////////");
      } catch (Exception e) {
        e.printStackTrace();
        logger.error("Error while checking emails for " + username + " on account " + accountId, e);
      }
    }
    return messageList;
  } 
  
  private String makeStoreFolder(String username, String accountId, String incomingFolder) throws Exception {
    String folderId = Utils.generateFID(accountId, incomingFolder, false);
    Folder storeFolder = storage_.getFolder(username, accountId, folderId);
    if (storeFolder == null) {
      folderId = Utils.generateFID(accountId, incomingFolder, true);
      Folder storeUserFolder = storage_.getFolder(username, accountId, folderId);
      if (storeUserFolder != null) {
        storeFolder = storeUserFolder;
      } else {
        storeFolder = new Folder();
      }
      storeFolder.setId(folderId);
      storeFolder.setName(incomingFolder);
      storeFolder.setPersonalFolder(true);
      storage_.saveFolder(username, accountId, storeFolder);
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

  public void createAccount(String username, Account account)
  throws Exception {
    saveAccount(username, account, true);
  }

  public List<Folder> getFolders(String username, String accountId)
  throws Exception {
    return storage_.getFolders(username, accountId);
  }

  public List<Folder> getFolders(String username, String accountId, boolean isPersonal) throws Exception {
    List<Folder> folders = new ArrayList<Folder>();
    for (Folder folder : storage_.getFolders(username, accountId))
      if (isPersonal) {
        if (folder.isPersonalFolder())
          folders.add(folder);
      } else {
        if (!folder.isPersonalFolder())
          folders.add(folder);
      }
    return folders;
  }

  public void addTag(String username, String accountId, Tag tag) throws Exception {
    storage_.addTag(username, accountId, tag);
  }

  public void addTag(String username, String accountId, List<Message> messages, List<Tag> tag) throws Exception {
    storage_.addTag(username, accountId, messages, tag);
  }

  public List<Tag> getTags(String username, String accountId) throws Exception {
    return storage_.getTags(username, accountId);
  }

  public Tag getTag(String username, String accountId, String tagId) throws Exception {
    return storage_.getTag(username, accountId, tagId);
  }

  public void removeTagsInMessages(String username, String accountId, List<Message> msgList, List<String> tagIdList) throws Exception {
    storage_.removeTagsInMessages(username, accountId, msgList, tagIdList);
  }

  public void removeTag(String username, String accountId, String tag) throws Exception {
    storage_.removeTag(username, accountId, tag);
  }

  public void updateTag(String username, String accountId, Tag tag) throws Exception {
    storage_.updateTag(username, accountId, tag);
  }

  public List<Message> getMessageByTag(String username, String accountId, String tagName) throws Exception {
    return storage_.getMessageByTag(username, accountId, tagName);
  }

  public MessagePageList getMessagePagelistByTag(String username, String accountId, String tagId) throws Exception {
    MessageFilter filter = new MessageFilter("Filter By Tag");
    filter.setAccountId(accountId);
    filter.setTag(new String[] { tagId });
    return getMessagePageList(username, filter);
  }

  public MessagePageList getMessagePageListByFolder(String username, String accountId,
      String folderId) throws Exception {
    MessageFilter filter = new MessageFilter("Filter By Folder");
    filter.setAccountId(accountId);
    filter.setFolder(new String[] { folderId });
    return getMessagePageList(username, filter);
  }

  public MailSetting getMailSetting(String username) throws Exception {
    return storage_.getMailSetting(username);
  }

  public void saveMailSetting(String username, MailSetting newSetting)
  throws Exception {
    storage_.saveMailSetting(username, newSetting);
  }

  public boolean importMessage(String username, String accountId, String folderId,
      InputStream inputStream, String type) throws Exception {
    return emlImportExport_.importMessage(username, accountId, folderId, inputStream, type);
  }

  public OutputStream exportMessage(String username, String accountId, Message message) throws Exception {
    return emlImportExport_.exportMessage(username, accountId, message);
  }

  public SpamFilter getSpamFilter(String username, String accountId) throws Exception {
    return storage_.getSpamFilter(username, accountId);
  }

  public void saveSpamFilter(String username, String accountId, SpamFilter spamFilter) throws Exception {
    storage_.saveSpamFilter(username, accountId, spamFilter);
  }

  public void toggleMessageProperty(String username, String accountId, List<Message> msgList, String folderId,
      String property, boolean value) throws Exception {
    Account account = getAccountById(username, accountId);
    Folder folder = getFolder(username, accountId, folderId);
    boolean success = true;
    if (account.getProtocol().equalsIgnoreCase(Utils.IMAP)) {
      try {
        Connector connector = new ImapConnector(account, this.getSSLSocketFactory(null));
        if (property.equals(Utils.EXO_STAR)) {
          if (folder != null && !Utils.isEmptyField(folder.getName())) {
            success = connector.setIsStared(msgList, value, folder);
          } else {
            List<Message> l ;
            for (Message m : msgList) {
              folder = getFolder(username, accountId, m.getFolders()[0]);
              if (folder != null) {
                l = new ArrayList<Message>();
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
            List<Message> l ;
            for (Message m : msgList) {
              folder = getFolder(username, accountId, m.getFolders()[0]);
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
        
      } catch(Exception e) {
        return;
      }
    }
    if (success) storage_.toggleMessageProperty(username, accountId, msgList, property, value);
  }

  public List<AccountData> getAccountDatas(SessionProvider sProvider) throws Exception {
    return null;
  }

  public String getFolderHomePath(String username, String accountId)
  throws Exception {
    return storage_.getFolderHomePath(username, accountId);
  }

  public List<Folder> getSubFolders(String username, String accountId, String parentPath) throws Exception {
    return storage_.getSubFolders(username, accountId, parentPath);
  }

  public List<Message> getReferencedMessages(String username, String accountId,
      String msgPath) throws Exception {
    return storage_.getReferencedMessages(username, accountId, msgPath);
  }

  public Account getDefaultAccount(String username) throws Exception {
    MailSetting mailSetting = storage_.getMailSetting(username);
    String defaultAccount = mailSetting.getDefaultAccount();
    Account account = null;
    if (defaultAccount != null) {
      account = getAccountById(username, defaultAccount);
    } else {
      List<Account> accList = getAccounts(username);
      if (accList.size() > 0)
        account = getAccounts(username).get(0);
    }
    return account;
  }

  public Message loadTotalMessage(String username, String accountId, Message msg) throws Exception {
    Account account = getAccountById(username, accountId);
    try {
    	IMAPStore store = openIMAPConnection(username, account);
      if (!msg.isLoaded() && store != null) {
        javax.mail.Message message = null;
        javax.mail.Folder fd = null;
        URLName url = new URLName(getFolder(username, accountId, msg.getFolders()[0]).getURLName());
        fd = store.getFolder(url);
        if (fd != null) {
          if (fd.isOpen()) {
            message = ((IMAPFolder)fd).getMessageByUID(Long.valueOf(msg.getUID()));
            storage_.saveTotalMessage(username, accountId, msg.getId(), message, null);
          } else {
            fd.open(javax.mail.Folder.READ_WRITE);
            message = ((IMAPFolder)fd).getMessageByUID(Long.valueOf(msg.getUID()));
            storage_.saveTotalMessage(username, accountId, msg.getId(), message, null);
            fd.close(true);
          }
        }
      }
    } catch(Exception e) {
      logger.info("Download content failure");
    }
    return storage_.loadTotalMessage(username, accountId, msg);
  }

  public List<MailUpdateStorageEventListener> listeners_ = new ArrayList<MailUpdateStorageEventListener>();
  
  public void start() {
    for (MailUpdateStorageEventListener updateListener : listeners_) {
      updateListener.preUpdate() ;
    }
  }

  public void stop() {
  }

  public synchronized void addListenerPlugin(ComponentPlugin listener) throws Exception {
    if(listener instanceof MailUpdateStorageEventListener ) {
      listeners_.add((MailUpdateStorageEventListener)listener) ;
    }
  }

  private Properties getAccountProperties(Account acc){
    Properties props = new Properties();
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
    props.put("mail.smtp.connectiontimeout", "0" );
    props.put("mail.smtp.timeout", "0" );
    //props.put("mail.debug", "true");
    String socketFactoryClass = "javax.net.SocketFactory";
    if (Boolean.valueOf(isSSl)) {
      socketFactoryClass = Utils.SSL_FACTORY;
      props.put(Utils.SVR_SMTP_STARTTLS_ENABLE, "true");
      props.put("mail.smtp.ssl.protocols","SSLv3 TLSv1");
    }
    props.put(Utils.SVR_SMTP_SOCKET_FACTORY_CLASS, socketFactoryClass);
    
    return props;
  }
  
  
  public boolean sendReturnReceipt(String username, String accId, String msgId, ResourceBundle res) throws Exception {
    //TODO need to implement
    Account acc = getAccountById(username, accId);
    Message msg = getMessageById(username, accId, msgId);
    
    String subject = new String("Disposition notification");
    String text = new String("The message sent on {0} to {1} with subject \"{2}\" has been displayed. This is no guarantee that the message has been read or understood.");
    if(res != null){
      try {
        subject = res.getString("UIMessagePreview.msg.return-receipt-subject");      
      } catch (MissingResourceException e) {
        subject = new String("Disposition notification");
        e.printStackTrace();
      }
      try {
        text = res.getString("UIMessagePreview.msg.return-receipt-text");
      } catch (MissingResourceException e) {
        text = new String("The message sent on {0} to {1} with subject \"{2}\" has been displayed. This is no guarantee that the message has been read or understood.");
        e.printStackTrace();
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
    disNotification.getNotifications().setHeader("Reporting-UA", "cs.exoplatform.com;" + " CS-Mail");
    disNotification.getNotifications().setHeader("MDN-Gateway", "smtp;" + " " + acc.getOutgoingHost());
    disNotification.getNotifications().setHeader("Original-Recipient", "rfc822;" + " " + msg.getFrom());
    disNotification.getNotifications().setHeader("Final-Recipient", "rfc822;" + " " + acc.getUserDisplayName()+ "<" + acc.getEmailAddress() + ">");
    disNotification.getNotifications().setHeader("Original-Message-ID", msg.getId());
    disNotification.getNotifications().setHeader("Disposition", "manual-action/MDN-sent-automatically;" + " displayed");
    
    MultipartReport report = new MultipartReport(text , disNotification);
    
    Properties props = getAccountProperties(acc);
    Session session = Session.getInstance(props, null);
    logger.debug(" #### Sending email ... ");
    SMTPTransport transport = (SMTPTransport)session.getTransport(Utils.SVR_SMTP);
    try {
      if (!acc.isOutgoingAuthentication()) {
        transport.connect() ;
      } else if (acc.useIncomingSettingForOutgoingAuthent()) {
        transport.connect(acc.getOutgoingHost(), Integer.parseInt(acc.getOutgoingPort()), acc.getIncomingUser(), acc.getIncomingPassword());
      } else {
        transport.connect(acc.getOutgoingHost(), Integer.parseInt(acc.getOutgoingPort()), acc.getOutgoingUserName(), acc.getOutgoingPassword());
      }
    } catch(Exception ex) {
      logger.debug("#### Can not connect to smtp server ...") ;
      throw ex;
    }
    
    MailcapCommandMap mailcap = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
    mailcap.addMailcap("message/disposition-notification;; x-java-content-handler=org.exoplatform.mail.service.impl.Message_DispositionNotification");
    CommandMap.setDefaultCommandMap(mailcap);

    sendReturnReceipt(session, transport, receiptMsg, report);
    transport.close();
    
    return true;
  }
  
  
  private void sendReturnReceipt(Session session, Transport transport, Message message, MultipartReport report) throws Exception {
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
      mimeMessage.setRecipients(javax.mail.Message.RecipientType.TO, InternetAddress.parse(message
          .getMessageTo()));

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
      e.printStackTrace();
      throw e;
    } catch (Exception e) {
      status = "There was an unexpected error. Sending Falied !" + e.getMessage();
      throw e;
    } finally {
      // logger.debug(" #### Info : " + status);
    }
    logger.debug(" #### Info : " + status);
  }
}
