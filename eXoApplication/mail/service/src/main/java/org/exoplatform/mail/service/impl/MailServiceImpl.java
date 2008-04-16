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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
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
import javax.mail.search.ComparisonTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SentDateTerm;
import javax.mail.util.ByteArrayDataSource;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.AccountData;
import org.exoplatform.mail.service.Attachment;
import org.exoplatform.mail.service.CheckingInfo;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.MailSetting;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.MessageFilter;
import org.exoplatform.mail.service.MessagePageList;
import org.exoplatform.mail.service.MimeMessageParser;
import org.exoplatform.mail.service.ServerConfiguration;
import org.exoplatform.mail.service.SpamFilter;
import org.exoplatform.mail.service.Tag;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.scheduler.JobInfo;
import org.exoplatform.services.scheduler.JobSchedulerService;
import org.exoplatform.services.scheduler.PeriodInfo;

import com.sun.mail.smtp.SMTPSendFailedException;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan.nguyen@exoplatform.com
 * Jun 23, 2007  
 */
public class MailServiceImpl implements MailService{

  private JCRDataStorage storage_ ;
  //will be use map for multi import/export email type 
  private EMLImportExport emlImportExport_ ;
  private Map<String, CheckingInfo> checkingLog_ ;
  
  public MailServiceImpl(NodeHierarchyCreator nodeHierarchyCreator) throws Exception {
    storage_ = new JCRDataStorage(nodeHierarchyCreator) ;
    emlImportExport_ = new EMLImportExport(storage_) ;
    checkingLog_ = new HashMap<String, CheckingInfo>(); 
  }
  
  public void removeCheckingInfo(String username, String accountId)throws Exception {
    String key = username + ":" + accountId ;
    checkingLog_.remove(key) ;
  }
  
  public CheckingInfo getCheckingInfo(String username, String accountId) throws Exception {
    String key = username + ":" + accountId ;
    return checkingLog_.get(key) ;
  }

  /**
   * @param username
   * @return
   * @throws Exception
   */
  public List<Account> getAccounts(SessionProvider sProvider, String username) throws Exception {
    return storage_.getAccounts(sProvider, username);
  }

  public Account getAccountById(SessionProvider sProvider, String username, String id) throws Exception {
    return storage_.getAccountById(sProvider, username, id);
  }

  public void saveAccount(SessionProvider sProvider, String username, Account account, boolean isNew) throws Exception {
    storage_.saveAccount(sProvider, username, account, isNew);
  }

  public void updateAccount(SessionProvider sProvider, String username, Account account) throws Exception {
    saveAccount(sProvider, username, account, false);
  }

  public void removeAccount(SessionProvider sProvider, String username, String accountId) throws Exception {
    storage_.removeAccount(sProvider, username, accountId);
  }

  public Folder getFolder(SessionProvider sProvider, String username, String accountId, String folderId) throws Exception {
    return storage_.getFolder(sProvider, username, accountId, folderId);
  } 
  
  public void saveFolder(SessionProvider sProvider, String username, String accountId, Folder folder) throws Exception {
    storage_.saveFolder(sProvider, username, accountId, folder);
  }


  public void removeUserFolder(SessionProvider sProvider, String username, Folder folder) throws Exception {
    storage_.removeUserFolder(sProvider, username, folder);
  }

  public void removeUserFolder(SessionProvider sProvider, String username, Account account, Folder folder) throws Exception {
    storage_.removeUserFolder(sProvider, username, account, folder);
  }
  
  public List<MessageFilter> getFilters(SessionProvider sProvider, String username, String accountId) throws Exception {
    return storage_.getFilters(sProvider, username, accountId);
  }
  
  public MessageFilter getFilterById(SessionProvider sProvider, String username, String accountId, String filterId) throws Exception {
    return storage_.getFilterById(sProvider, username, accountId, filterId);
  }
  
  public void saveFilter(SessionProvider sProvider, String username, String accountId, MessageFilter filter) throws Exception {
    storage_.saveFilter(sProvider, username, accountId, filter);
  }
  
  public void removeFilter(SessionProvider sProvider, String username, String accountId, String filterId) throws Exception {
    storage_.removeFilter(sProvider, username, accountId, filterId);
  }

  public Message getMessageById(SessionProvider sProvider, String username, String accountId, String msgId) throws Exception {
    return storage_.getMessageById(sProvider, username, accountId, msgId);
  }

  public void removeMessage(SessionProvider sProvider, String username, String accountId, Message message) throws Exception {
    storage_.removeMessage(sProvider, username, accountId, message);
  }

  public void removeMessage(SessionProvider sProvider, String username,String accountId, List<Message> messages) throws Exception {
    storage_.removeMessage(sProvider, username, accountId, messages);
  } 
  
  public void moveMessages(SessionProvider sProvider, String username,String accountId, Message msg, String currentFolderId, String destFolderId) throws Exception {
    storage_.moveMessages(sProvider, username, accountId, msg, currentFolderId, destFolderId);
  }

  public MessagePageList getMessagePageList(SessionProvider sProvider, String username, MessageFilter filter) throws Exception {
    return storage_.getMessagePageList(sProvider, username, filter);
  }
  
  public void saveMessage(SessionProvider sProvider, String username, String accountId, String targetMsgPath, Message message) throws Exception {
    storage_.saveMessage(sProvider, username, accountId, targetMsgPath, message) ;
  }
  
  public List<Message> getMessagesByTag(SessionProvider sProvider, String username, String accountId, String tagId) throws Exception {
    MessageFilter filter = new MessageFilter("Tag") ;
    filter.setAccountId(accountId) ;
    filter.setFolder(new String[]{ tagId }) ;
    return getMessages(sProvider, username, filter) ;   
  }
  
  public List<Message> getMessagesByFolder(SessionProvider sProvider, String username, String accountId, String folderId) throws Exception {
    MessageFilter filter = new MessageFilter("Folder") ;
    filter.setAccountId(accountId) ;
    filter.setFolder(new String[]{ folderId }) ;
    return getMessages(sProvider, username, filter) ;   
  }
  
  public List<Message> getMessages(SessionProvider sProvider, String username, MessageFilter filter) throws Exception {
    return storage_.getMessages(sProvider, username, filter) ;
  }

  public void saveMessage(SessionProvider sProvider, String username, String accountId, Message message, boolean isNew) throws Exception {
    storage_.saveMessage(sProvider, username, accountId, message, isNew);
  }

  public Message sendMessage(SessionProvider sProvider, String username, String accId, Message message) throws Exception {
    Account acc = getAccountById(sProvider, username, accId) ;
    String smtpUser = acc.getIncomingUser() ;
    String outgoingHost = acc.getOutgoingHost() ;
    String outgoingPort  = acc.getOutgoingPort() ;
    String isSSl =  acc.getServerProperties().get(Utils.SVR_INCOMING_SSL)  ;
    Properties props = new Properties();
    props.put(Utils.SVR_SMTP_HOST, outgoingHost) ;
    props.put(Utils.SVR_SMTP_PORT, outgoingPort) ;
    props.put(Utils.SVR_SMTP_AUTH, "true");
    props.put(Utils.SVR_SMTP_SOCKET_FACTORY_FALLBACK, "false");
    String socketFactoryClass = "javax.net.SocketFactory";
    if (Boolean.valueOf(isSSl)) socketFactoryClass = Utils.SSL_FACTORY;
    props.put(Utils.SVR_SMTP_SOCKET_FACTORY_CLASS,  socketFactoryClass);
    props.put(Utils.SVR_SMTP_SOCKET_FACTORY_PORT, outgoingPort);
    props.put(Utils.SVR_SMTP_USER, smtpUser) ;
    props.put(Utils.SVR_SMTP_STARTTLS_ENABLE, "true");
    props.put(Utils.SVR_INCOMING_SSL, isSSl);
    
    props.put(Utils.SVR_INCOMING_USERNAME, acc.getIncomingUser());
    props.put(Utils.SVR_INCOMING_PASSWORD, acc.getIncomingPassword());
        
    //TODO : add authenticator 
    /*Session session = Session.getInstance(props, new javax.mail.Authenticator(){     
      protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
        return new javax.mail.PasswordAuthentication(acc.getOutgoingUser(), acc.getOutgoingPassword());
      }});
    */
    Session session = Session.getInstance(props, null);
    System.out.println(" #### Sending email ... ");
    Transport transport = session.getTransport(Utils.SVR_SMTP);
    transport.connect(outgoingHost, smtpUser, acc.getIncomingPassword()) ;
    Message msg = send(session, transport, message);
    transport.close();
    
    return msg ;
  }
  
  public Message sendMessage(SessionProvider sProvider, String username, Message message) throws Exception {
    return sendMessage(sProvider, username, message.getAccountId(), message) ;
  }
  
  public void sendMessage(Message message) throws Exception {
    List<Message> msgList = new ArrayList<Message>();
    msgList.add(message);
    sendMessages(msgList, message.getServerConfiguration());
  }
  
  public void sendMessages(List<Message> msgList, ServerConfiguration serverConfig) throws Exception {
    Properties props = new Properties();
    props.put(Utils.SVR_INCOMING_USERNAME, serverConfig.getUserName());
    props.put(Utils.SVR_INCOMING_PASSWORD, serverConfig.getPassword());
    props.put(Utils.SVR_SMTP_USER, serverConfig.getUserName()) ;
    props.put(Utils.SVR_SMTP_HOST, serverConfig.getOutgoingHost()) ;
    props.put(Utils.SVR_SMTP_PORT, serverConfig.getOutgoingPort()) ;
    props.put(Utils.SVR_SMTP_AUTH, "true");
    props.put(Utils.SVR_SMTP_SOCKET_FACTORY_PORT, serverConfig.getOutgoingPort());
    if (serverConfig.isSsl()) {
      props.put(Utils.SVR_INCOMING_SSL, String.valueOf(serverConfig.isSsl()));
      props.put(Utils.SVR_SMTP_STARTTLS_ENABLE, "true");
      props.put(Utils.SVR_SMTP_SOCKET_FACTORY_CLASS,  "javax.net.ssl.SSLSocketFactory");
    }
    props.put(Utils.SVR_SMTP_SOCKET_FACTORY_FALLBACK, "false");
    Session session = Session.getInstance(props, null);
    Transport transport = session.getTransport(Utils.SVR_SMTP);
    transport.connect(serverConfig.getOutgoingHost(), serverConfig.getUserName(), serverConfig.getPassword()) ;
    System.out.println(" #### Sending email ... ");
    int i = 0;
    for (Message msg : msgList) {
      msg.setServerConfiguration(serverConfig);
      try {
        send(session , transport, msg);
        i++;
      } catch(Exception e) {
        System.out.println(" #### Info : send fail at message " + i + " \n");
      }
    }
    System.out.println(" #### Info : Sent " + i + " email(s)");
    transport.close();
  }
  
  private Message send(Session session,Transport transport, Message message) throws Exception {
    javax.mail.Message mimeMessage = new MimeMessage(session);
    String status = "";
    InternetAddress addressFrom ;
    mimeMessage.setHeader("Message-ID", message.getId()) ;
    if (message.getFrom() != null) 
      addressFrom = new InternetAddress(message.getFrom());
    else 
      addressFrom = new InternetAddress(session.getProperties().getProperty(Utils.SVR_SMTP_USER));
    
    mimeMessage.setFrom(addressFrom);
    if(message.getMessageTo() != null) 
      mimeMessage.setRecipients(javax.mail.Message.RecipientType.TO, InternetAddress.parse(message.getMessageTo()));
    
    if(message.getMessageCc() != null) 
      mimeMessage.setRecipients(javax.mail.Message.RecipientType.CC, InternetAddress.parse(message.getMessageCc(), true));
    
    if(message.getMessageBcc() != null)    
      mimeMessage.setRecipients(javax.mail.Message.RecipientType.BCC, InternetAddress.parse(message.getMessageBcc(), false));
    
    if(message.getReplyTo() != null)    
      mimeMessage.setReplyTo(Utils.getInternetAddress(message.getReplyTo()));
    
    mimeMessage.setSubject(message.getSubject());
    mimeMessage.setSentDate(message.getSendDate());
    
    MimeMultipart  multipPartRoot = new MimeMultipart("mixed");
    
    MimeMultipart  multipPartContent = new MimeMultipart("alternative");
    
    List<Attachment> attachList = message.getAttachments();
    if (attachList != null && attachList.size() != 0) {
      MimeBodyPart contentPartRoot = new MimeBodyPart();
      contentPartRoot.setContent(multipPartContent);
      
      MimeBodyPart  mimeBodyPart1 = new MimeBodyPart();
      mimeBodyPart1.setContent(message.getMessageBody(), message.getContentType());
      multipPartContent.addBodyPart(mimeBodyPart1);
      multipPartRoot.addBodyPart(contentPartRoot);
      
      for (Attachment att : attachList) {
        InputStream is = att.getInputStream();
        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        ByteArrayDataSource byteArrayDataSource = new ByteArrayDataSource(is, att.getMimeType());
        mimeBodyPart.setDataHandler(new DataHandler(byteArrayDataSource));

        mimeBodyPart.setDisposition(Part.ATTACHMENT);
        mimeBodyPart.setFileName(att.getName());
        multipPartRoot.addBodyPart(mimeBodyPart);
      }
      mimeMessage.setContent(multipPartRoot);
    } else {
      if (message.getContentType().indexOf("text/plain") > -1)
        mimeMessage.setText(message.getMessageBody());
      else
        mimeMessage.setContent(message.getMessageBody(), "text/html");
    }
    mimeMessage.setHeader("X-Priority", String.valueOf(message.getPriority()));
    String priority = "Normal";
    if (message.getPriority() == Utils.PRIORITY_HIGH) {
      priority = "High";
    } else if (message.getPriority() == Utils.PRIORITY_LOW) {
      priority = "Low";
    }     
    if (message.getPriority() != 0 ) mimeMessage.setHeader("Importance", priority);
    
    Iterator iter = message.getHeaders().keySet().iterator() ;
    while (iter.hasNext()) {
      String key = iter.next().toString() ;
      mimeMessage.setHeader(key, message.getHeaders().get(key)) ;
    }
    mimeMessage.saveChanges();
    try {
      transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
      message.setId(MimeMessageParser.getMessageId(mimeMessage)) ;
      Enumeration enu = mimeMessage.getAllHeaders() ;
      while (enu.hasMoreElements()) {
        Header header = (Header)enu.nextElement() ;
        message.setHeader(header.getName(), header.getValue()) ;
      }
      status = "Mail Delivered !";
    } catch (AddressException e) {
      status = "There was an error parsing the addresses. Sending Falied !" + e.getMessage(); 
    } catch(AuthenticationFailedException e) {
      status = "The Username or Password may be wrong. Sending Falied !" + e.getMessage(); 
    } catch (SMTPSendFailedException e) {
      status = "Sorry,There was an error sending the message. Sending Falied !" + e.getMessage();           
    } catch (MessagingException e) {
      status = "There was an unexpected error. Sending Failed ! " + e.getMessage();
    } catch (Exception e) {
      status = "There was an unexpected error. Sending Falied !" + e.getMessage();
    } finally {
      //System.out.println(" #### Info : " + status);      
    } 
    System.out.println(" #### Info : " + status) ;
    
    return message ;
  }
  
  public void checkMail(String username, String accountId) throws Exception {
  	Calendar cal = new GregorianCalendar() ;
  	PeriodInfo periodInfo = new PeriodInfo(cal.getTime(), null, 1, 86400000) ;
  	Class clazz = Class.forName("org.exoplatform.mail.service.CheckMailJob") ;
  	JobInfo info = new JobInfo(username + ":" + accountId, "CollaborationSuite-webmail", clazz) ;
  	ExoContainer container = ExoContainerContext.getCurrentContainer();
		JobSchedulerService schedulerService = 
			(JobSchedulerService) container.getComponentInstanceOfType(JobSchedulerService.class);
		schedulerService.addPeriodJob(info, periodInfo) ;
		
  }
  
  public List<Message> checkNewMessage(SessionProvider sProvider, String username, String accountId) throws Exception {
    Account account = getAccountById(sProvider, username, accountId) ;
    CheckingInfo info = new CheckingInfo() ;
    String key = username + ":" + accountId ;
    checkingLog_.put(key, info) ;
    long t1, t2 , tt1, tt2;
    System.out.println(" #### Getting mail from " + account.getIncomingHost() + " ... !");
    checkingLog_.get(key).setStatusMsg("Getting mail from " + account.getIncomingHost() + " ... !") ;
    List<Message> messageList = new ArrayList<Message>();
    int totalNew = -1;
    String protocol = account.getProtocol();
    boolean isPop3 = account.getProtocol().equals(Utils.POP3) ;
    boolean isImap = account.getProtocol().equals(Utils.IMAP) ;
    try {
      Properties props = System.getProperties();
      String socketFactoryClass = "javax.net.SocketFactory";
      if (account.isIncomingSsl()) socketFactoryClass = Utils.SSL_FACTORY;
      
      if(protocol.equals(Utils.POP3)) {
        props.setProperty("mail.pop3.socketFactory.fallback", "false");
        props.setProperty( "mail.pop3.socketFactory.class", socketFactoryClass);
      } else if (protocol.equals(Utils.IMAP)) {
        props.setProperty("mail.imap.socketFactory.fallback", "false");
        props.setProperty("mail.imap.socketFactory.class", socketFactoryClass);
      }
      
      Session session = Session.getDefaultInstance(props);
      String[] incomingFolders = account.getIncomingFolder().split(",") ;
      for (String incomingFolder : incomingFolders) {
        incomingFolder = incomingFolder.trim() ;
        URLName storeURL = new URLName(account.getProtocol(), account.getIncomingHost(), Integer.valueOf(account.getIncomingPort()), incomingFolder, account.getIncomingUser(), account.getIncomingPassword()) ;
        Store store = session.getStore(storeURL) ;
        store.connect();
        javax.mail.Folder folder = store.getFolder(storeURL.getFile());
        if (!folder.exists()) {
          System.out.println(" #### Folder " + incomingFolder + " is not exists !");
          checkingLog_.get(key).setStatusMsg("Folder " + incomingFolder + " is not exists") ;
          continue ;
        } else {
          System.out.println(" #### Getting mails from folder " + incomingFolder + " !");
          checkingLog_.get(key).setStatusMsg("Getting mails from folder " + incomingFolder + " !") ;
        }
        folder.open(javax.mail.Folder.READ_WRITE);
        
        javax.mail.Message[] messages ;
        if (account.getLastCheckedDate() == null) {
          messages = folder.getMessages() ;
        } else {
          SearchTerm unseenFlag = new FlagTerm(new Flags(Flags.Flag.SEEN), false) ;
          SentDateTerm dateTerm = new SentDateTerm(ComparisonTerm.GT, account.getLastCheckedDate());
          unseenFlag = new OrTerm(unseenFlag, dateTerm) ;
          if (isImap) unseenFlag = new ReceivedDateTerm(ComparisonTerm.GT, account.getLastCheckedDate());
          messages = folder.search(unseenFlag) ;
        }
        boolean leaveOnServer = (isPop3 && Boolean.valueOf(account.getPopServerProperties().get(Utils.SVR_POP_LEAVE_ON_SERVER))) ;
        boolean markAsDelete = (isImap && Boolean.valueOf(account.getImapServerProperties().get(Utils.SVR_IMAP_MARK_AS_DELETE))) ;
        
        boolean deleteOnServer = (isPop3 && !leaveOnServer) || (isImap && markAsDelete);
        
        totalNew = messages.length ;
        checkingLog_.get(key).setTotalMsg(totalNew) ;
        
        System.out.println(" #### Folder contains " + totalNew + " messages !");
        tt1 = System.currentTimeMillis();

        if (totalNew > 0) {
          int i = 0 ;
          SpamFilter spamFilter = getSpamFilter(sProvider, username, account.getId());
          String folderId = Utils.createFolderId(accountId, incomingFolder, false) ;
          Folder storeFolder = storage_.getFolder(sProvider, username, account.getId(), folderId) ;
          if(storeFolder == null) {
            folderId = Utils.createFolderId(accountId, incomingFolder, true) ;
            Folder storeUserFolder = storage_.getFolder(sProvider, username, account.getId(), folderId) ;
            if (storeUserFolder != null) storeFolder = storeUserFolder ;
            else storeFolder = new Folder() ;
            storeFolder.setId(folderId);
            storeFolder.setName(incomingFolder) ;
            storeFolder.setLabel(incomingFolder) ;
            storeFolder.setPersonalFolder(true) ;
            storage_.saveFolder(sProvider, username, account.getId(), storeFolder) ;
          }
          javax.mail.Message msg ;
          while (i < totalNew && !checkingLog_.get(key).isRequestStop()) {
            System.out.println(" [DEBUG] Fetching message " + (i+1) + " ...") ;
            checkingLog_.get(key).setStatusMsg("Fetching message ") ;
            t1 = System.currentTimeMillis();
            msg = messages[i] ;   
            msg.setFlag(Flags.Flag.SEEN, true);
            if (deleteOnServer) msg.setFlag(Flags.Flag.DELETED, true);
            try {
              checkingLog_.get(key).setFetching(i) ;
              storage_.saveMessage(sProvider, username, account.getId(), msg, folderId, spamFilter) ;
              account.setLastCheckedDate(MimeMessageParser.getReceivedDate(msg).getTime()) ;
            } catch(Exception e) {
              e.printStackTrace() ;
              i++ ;
              continue ;
            }  
            i ++ ;          
            t2 = System.currentTimeMillis();
            System.out.println(" [DEBUG] Message " + i + " saved : " + (t2-t1) + " ms");
          }
          saveAccount(sProvider, username, account, false) ;
          checkingLog_.get(key).setStatusCode(CheckingInfo.FINISHED_CHECKMAIL_STATUS) ;
          Calendar cc = GregorianCalendar.getInstance();
          javax.mail.Message firstMsg = messages[0] ;
          cc = MimeMessageParser.getReceivedDate(firstMsg);
          System.out.println(" [DEBUG] Executing the filter ...") ;
          checkingLog_.get(key).setStatusMsg("Executing the filter ") ;
          t1 = System.currentTimeMillis();
          storage_.execActionFilter(sProvider, username, accountId, cc);
          t2 = System.currentTimeMillis();
          System.out.println(" [DEBUG] Executed the filter finished : " + (t2 - t1) + " ms") ;
          tt2 = System.currentTimeMillis();
          System.out.println(" ### Check mail finished total took: " + (tt2 - tt1) + " ms") ;
          
          folder.close(true);      
          store.close();
        }
      }
    }  catch (Exception e) { 
      e.printStackTrace();
    }
    return messageList;
  }
  
  public void createAccount(SessionProvider sProvider, String username, Account account) throws Exception {
    saveAccount(sProvider, username, account, true);
  }

  public List<Folder> getFolders(SessionProvider sProvider, String username, String accountId) throws Exception {
    return storage_.getFolders(sProvider, username, accountId) ;  
  }
  
  public List<Folder> getFolders(SessionProvider sProvider, String username, String accountId, boolean isPersonal) throws Exception {
    List<Folder> folders = new ArrayList<Folder>() ;
    for(Folder folder : storage_.getFolders(sProvider, username, accountId))  
      if(isPersonal) {
        if(folder.isPersonalFolder()) folders.add(folder) ;
      } else {
        if(!folder.isPersonalFolder()) folders.add(folder) ;
      }
    return folders ;
  }

  public void addTag(SessionProvider sProvider, String username, String accountId, List<Message> messages, List<Tag> tag)
      throws Exception {
    storage_.addTag(sProvider, username, accountId, messages, tag);
  }

  public List<Tag> getTags(SessionProvider sProvider, String username, String accountId) throws Exception {
    return storage_.getTags(sProvider, username, accountId);
  }
  
  public Tag getTag(SessionProvider sProvider, String username, String accountId, String tagId) throws Exception {
    return storage_.getTag(sProvider, username, accountId, tagId);
  }

  public void removeMessageTag(SessionProvider sProvider, String username, String accountId, List<Message> messageIds, List<String> tags)
      throws Exception {
    storage_.removeMessageTag(sProvider, username, accountId, messageIds, tags);   
  }

  public void removeTag(SessionProvider sProvider, String username, String accountId, String tag) throws Exception {
    storage_.removeTag(sProvider, username, accountId, tag);
  }

  public void updateTag(SessionProvider sProvider, String username, String accountId, Tag tag) throws Exception {
    storage_.updateTag(sProvider, username, accountId, tag);
  }
  
  public List<Message> getMessageByTag(SessionProvider sProvider, String username, String accountId, String tagName)
      throws Exception {
    return storage_.getMessageByTag(sProvider, username, accountId, tagName);
  }
  
  public MessagePageList getMessagePagelistByTag(SessionProvider sProvider, String username, String accountId, String tagId) throws Exception {
    MessageFilter filter = new MessageFilter("Filter By Tag") ;
    filter.setAccountId(accountId) ;
    filter.setTag(new String[]{tagId} ) ;
    return getMessagePageList(sProvider, username, filter) ;   
  }
  
  public MessagePageList getMessagePageListByFolder(SessionProvider sProvider, String username, String accountId, String folderId) throws Exception {
    MessageFilter filter = new MessageFilter("Filter By Folder") ;
    filter.setAccountId(accountId) ;
    filter.setFolder(new String[]{folderId} ) ;
    return getMessagePageList(sProvider, username, filter) ;   
  }
  
  public MailSetting getMailSetting(SessionProvider sProvider, String username) throws Exception {
    return storage_.getMailSetting(sProvider, username);
  }  
  
  public void saveMailSetting(SessionProvider sProvider, String username, MailSetting newSetting) throws Exception {
    storage_.saveMailSetting(sProvider, username, newSetting);
  }
  
  public boolean importMessage(SessionProvider sProvider, String username, String accountId, String folderId, InputStream inputStream, String type) throws Exception {
  	return emlImportExport_.importMessage(sProvider, username, accountId, folderId, inputStream, type) ;
  }
  
  public OutputStream exportMessage(SessionProvider sProvider, String username, String accountId, Message message) throws Exception {
    return emlImportExport_.exportMessage(sProvider, username, accountId, message) ; 
  }
  
  public void runFilter(SessionProvider sProvider, String username, String accountId, MessageFilter filter) throws Exception {
    List<Message> msgList = getMessagePageList(sProvider, username, filter).getAll(username);
    String applyFolder = filter.getApplyFolder();
    String applyTag = filter.getApplyTag();
    List<Tag> tagList = new ArrayList<Tag>();
    for (Message msg : msgList) {
      Folder folder = getFolder(sProvider, username, accountId, applyFolder);
      if (folder != null && (msg.getFolders()[0] != applyFolder)) {
        Folder appFolder = getFolder(sProvider, username, accountId, applyFolder);
        if (appFolder != null)
          moveMessages(sProvider, username, accountId, msg, msg.getFolders()[0], applyFolder);
      }
    }
    if (!Utils.isEmptyField(applyTag)) {
      Tag tag = getTag(sProvider, username, accountId, applyTag);
      if (tag != null) {
        tagList.add(tag);
        addTag(sProvider, username, accountId, msgList, tagList);
      }
    }
  }
  
  public SpamFilter getSpamFilter(SessionProvider sProvider, String username, String accountId) throws Exception {
    return storage_.getSpamFilter(sProvider, username, accountId);
  }
  
  public void saveSpamFilter(SessionProvider sProvider, String username, String accountId, SpamFilter spamFilter) throws Exception {
    storage_.saveSpamFilter(sProvider, username, accountId, spamFilter);
  }
  
  public void toggleMessageProperty(SessionProvider sProvider, String username, String accountId, List<Message> msgList, String property) throws Exception {
    storage_.toggleMessageProperty(sProvider, username, accountId, msgList, property) ;
  }

	public List<AccountData> getAccountDatas(SessionProvider sProvider) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}	
  
  public String getFolderHomePath(SessionProvider sProvider, String username, String accountId) throws Exception {
    return storage_.getFolderHomePath(sProvider, username, accountId);
  }
  
  public void saveFolder(SessionProvider sProvider, String username, String accountId, String parentId, Folder folder) throws Exception {
    storage_.saveFolder(sProvider, username, accountId, parentId, folder);
  }
  
  public List<Folder> getSubFolders(SessionProvider sProvider, String username, String accountId, String parentPath) throws Exception {
    return storage_.getSubFolders(sProvider, username, accountId, parentPath) ;
  }
  
  public List<Message> getReferencedMessages(SessionProvider sProvider, String username, String accountId, String msgPath) throws Exception {
    return storage_.getReferencedMessages(sProvider, username, accountId, msgPath);
  }
  
  public Account getDefaultAccount(SessionProvider sProvider, String username) throws Exception {
    MailSetting mailSetting = storage_.getMailSetting(sProvider, username) ;
    String defaultAccount = mailSetting.getDefaultAccount() ;
    Account account = null ;
    if (defaultAccount != null) { 
      account = getAccountById(sProvider, username, defaultAccount) ;
    } else {
      List<Account> accList = getAccounts(sProvider, username) ;
      if (accList.size() > 0) 
        account = getAccounts(sProvider, username).get(0) ;
    }
    return account ;
  }
}