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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.activation.DataHandler;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.mail.AuthenticationFailedException;
import javax.mail.Flags;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.AddressException;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.AccountData;
import org.exoplatform.mail.service.Attachment;
import org.exoplatform.mail.service.BufferAttachment;
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
import org.exoplatform.services.jcr.util.IdGenerator;
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
  public MailServiceImpl(NodeHierarchyCreator nodeHierarchyCreator) throws Exception {
    storage_ = new JCRDataStorage(nodeHierarchyCreator) ;
    emlImportExport_ = new EMLImportExport(storage_) ;
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

  public void sendMessage(SessionProvider sProvider, String username, Message message) throws Exception {
    String accountId = message.getAccountId() ;
    Account acc = getAccountById(sProvider, username, accountId) ;
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
    String status = send(session, transport, message);
    System.out.println(" ### Infor : " + status);
    transport.close();
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
      String status = "";
      try {
        status = send(session , transport, msg);
        i++;
      } catch(Exception e) {
        System.out.println(" #### Info : send fail at message " + i + " \n" + status);
      }
    }
    System.out.println(" #### Info : Sent " + i + " email(s)");
    transport.close();
  }
  
  private String send(Session session,Transport transport, Message message) throws Exception {
    javax.mail.Message mimeMessage = new MimeMessage(session);
    String status = "";
    InternetAddress addressFrom ;
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
    
    MimeBodyPart contentPartRoot = new MimeBodyPart();
    contentPartRoot.setContent(multipPartContent);

    MimeBodyPart  mimeBodyPart1 = new MimeBodyPart();
    mimeBodyPart1.setContent(message.getMessageBody(), "text/html");
    multipPartContent.addBodyPart(mimeBodyPart1);
    
    multipPartRoot.addBodyPart(contentPartRoot);
    
    List<Attachment> attachList = message.getAttachments();
    if (attachList != null) {
      for (Attachment att : attachList) {
        InputStream is = att.getInputStream();
        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        ByteArrayDataSource byteArrayDataSource = new ByteArrayDataSource(is, att.getMimeType());
        mimeBodyPart.setDataHandler(new DataHandler(byteArrayDataSource));

        mimeBodyPart.setDisposition(Part.ATTACHMENT);
        mimeBodyPart.setFileName(att.getName());
        multipPartRoot.addBodyPart(mimeBodyPart);
      }        
    }
    mimeMessage.setHeader("X-Priority", String.valueOf(message.getPriority()));
    String priority = "Normal";
    if (message.getPriority() == Utils.PRIORITY_HIGH) {
      priority = "High";
    } else if (message.getPriority() == Utils.PRIORITY_LOW) {
      priority = "Low";
    }     
    if (message.getPriority() != 0 ) mimeMessage.setHeader("Importance", priority);
    
    mimeMessage.setContent(multipPartRoot);
    mimeMessage.saveChanges();
    try {
      transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
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
    return status ;
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
    System.out.println(" #### Getting mail from " + account.getIncomingHost() + " ... !");
    List<Message> messageList = new ArrayList<Message>();
    int totalNew = -1;
    String protocol = account.getProtocol();
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
          continue ;
        } else {
          System.out.println(" #### Getting mails from folder " + incomingFolder + " !");
        }
        folder.open(javax.mail.Folder.READ_WRITE);

        javax.mail.Message[] messages = folder.getMessages() ;
        Vector<javax.mail.Message> vector = new Vector<javax.mail.Message>();
        boolean isPop3 = account.getProtocol().equals(Utils.POP3);
        for (int i=1 ; i< messages.length; i++) {
          if (!messages[i].isSet(Flags.Flag.SEEN)) vector.add(messages[i]); 
          messages[i].setFlag(Flags.Flag.SEEN, true); 
          if (isPop3) messages[i].setFlag(Flags.Flag.DELETED, true); 
        }  

        totalNew = vector.size() ;

        System.out.println(" #### Folder contains " + totalNew + " messages !");

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
          while (i < totalNew) {
            javax.mail.Message msg = vector.get(i) ;      
            try {
              saveMessage(sProvider, msg, account.getId(), username, folderId, spamFilter) ;
            } catch(Exception e) {
              e.printStackTrace();
              i++ ;
              continue ;
            }  
            i ++ ;          
            System.out.println(" ####  " + i + " messages saved");
          }
          Calendar cc = GregorianCalendar.getInstance();
          javax.mail.Message firstMsg = vector.get(0) ;
          if (firstMsg.getReceivedDate() != null)
            cc.setTime(firstMsg.getReceivedDate());
          else cc.setTime(firstMsg.getSentDate());
          storage_.execActionFilter(sProvider, username, accountId, cc);
          folder.close(true);      
          store.close();
        }
      }
    }  catch (Exception e) { 
      e.printStackTrace();
    }
    return messageList;
  }
  
  private void saveMessage(SessionProvider sProvider, javax.mail.Message msg, String accId, String username, String folderId, SpamFilter spamFilter) throws Exception {
    Message newMsg = new Message();
    Calendar gc = MimeMessageParser.getReceivedDate(msg) ;
    Node msgHomeNode = storage_.getDateStoreNode(sProvider, username, accId, gc.getTime()) ;
  	Node node = msgHomeNode.addNode(newMsg.getId(), Utils.EXO_MESSAGE) ;
    msgHomeNode.save();
    node.setProperty(Utils.EXO_ID, newMsg.getId());
  	node.setProperty(Utils.EXO_ACCOUNT, accId);
    node.setProperty(Utils.EXO_FROM, InternetAddress.toString(msg.getFrom()));
    node.setProperty(Utils.EXO_TO, InternetAddress.toString(msg.getRecipients(javax.mail.Message.RecipientType.TO)));
    node.setProperty(Utils.EXO_CC, InternetAddress.toString(msg.getRecipients(javax.mail.Message.RecipientType.CC)));
    node.setProperty(Utils.EXO_BCC, InternetAddress.toString(msg.getRecipients(javax.mail.Message.RecipientType.BCC)));
    node.setProperty(Utils.EXO_REPLYTO, InternetAddress.toString(msg.getReplyTo()));
    node.setProperty(Utils.EXO_SUBJECT, msg.getSubject());
    node.setProperty(Utils.EXO_RECEIVEDDATE, gc);
    Calendar sc = GregorianCalendar.getInstance();
    if (msg.getSentDate() != null) sc.setTime(msg.getSentDate());
    else sc = gc ;
    node.setProperty(Utils.EXO_SENDDATE, sc); //TODO send date
    
    node.setProperty(Utils.EXO_CONTENT_TYPE, msg.getContentType());
    node.setProperty(Utils.EXO_SIZE, Math.abs(msg.getSize()));
    node.setProperty(Utils.EXO_ISUNREAD, true);
    node.setProperty(Utils.EXO_STAR, false);     
    
    node.setProperty(Utils.EXO_PRIORITY, MimeMessageParser.getPriority(msg));
    
    String[] folderIds = { folderId };
    
    if ( spamFilter.checkSpam(msg) ) {
      folderIds = new String[] { Utils.createFolderId(accId, Utils.FD_SPAM, false) } ;
    }
    
    node.setProperty(Utils.EXO_FOLDERS, folderIds);
    Object obj = msg.getContent() ;
    if (obj instanceof Multipart) {
      setMultiPart((Multipart)obj, node);
    } else {
      setPart(msg, node);
    }
    node.save() ;
    
    storage_.saveConversation(sProvider, username, accId, Utils.getAllRecipients(msg), node) ;
    
    Node folderHomeNode = storage_.getFolderHome(sProvider, username, accId) ;
    try { 
      Node folderNode = folderHomeNode.getNode(folderId);
      folderNode.setProperty(Utils.EXO_UNREADMESSAGES, folderNode.getProperty(Utils.EXO_UNREADMESSAGES).getLong() + 1) ;
      folderNode.setProperty(Utils.EXO_TOTALMESSAGE , folderNode.getProperty(Utils.EXO_TOTALMESSAGE).getLong() + 1) ;
      folderNode.save();
    } catch(PathNotFoundException e) {}
    
  }
  
  private void setMultiPart(Multipart multipart, Node node) {
    try {
      int i = 0 ;
      int n = multipart.getCount() ;
      while( i < n) {
        setPart(multipart.getBodyPart(i), node);
        i++ ;
      }     
    }catch(Exception e) {
      e.printStackTrace() ;
    }   
  }

  private void setPart(Part part, Node node){
    try {
      String disposition = part.getDisposition();
      String contentType = part.getContentType();
      if (disposition == null) {
        if (part.getContent() instanceof MimeMultipart) {
          MimeMultipart mimeMultiPart = (MimeMultipart) part.getContent() ;
          for (int i = 0; i< mimeMultiPart.getCount(); i++) {
            // for each part, set the body content
            setPart(mimeMultiPart.getBodyPart(i), node);
          }
        } else {
          setMessageBody(part, node);
        }
      } else {
        if (disposition.equalsIgnoreCase(Part.INLINE)) {
          /* this must be presented INLINE, hence inside the body of the message */
          if (part.isMimeType("text/plain") || part.isMimeType("text/html")) {
            setMessageBody(part, node);
          }
        } else if (disposition.equalsIgnoreCase(Part.ATTACHMENT)) {
          /* this part must be presented as an attachment, hence we add it to the attached files */
          BufferAttachment file = new BufferAttachment();
          file.setId("Attachment" + IdGenerator.generate());
          file.setName(MimeUtility.decodeText(part.getFileName()));
          InputStream is = part.getInputStream();
          file.setInputStream(is);
          file.setSize(is.available());
          if (contentType.indexOf(";") > 0) {
            String[] type = contentType.split(";") ;
            file.setMimeType(type[0]);
          } else {
            file.setMimeType(contentType) ;
          }
          Node nodeFile = node.addNode(file.getName(), Utils.NT_FILE);
          Node nodeContent = nodeFile.addNode(Utils.JCR_CONTENT, Utils.NT_RESOURCE);
          nodeContent.setProperty(Utils.JCR_MIMETYPE, file.getMimeType());
          nodeContent.setProperty(Utils.JCR_DATA, file.getInputStream());
          nodeContent.setProperty(Utils.JCR_LASTMODIFIED, Calendar.getInstance().getTimeInMillis());
          node.setProperty(Utils.EXO_HASATTACH, true);
        }
      }
    } catch(Exception e) {
      e.printStackTrace() ;
    }
  }

  private void setMessageBody(Part part, Node node) throws Exception {
    StringBuffer messageBody =new StringBuffer();
    InputStream is = part.getInputStream();
    String contentType = part.getContentType() ;
    String charset = "UTF-8" ;
    if(contentType != null){
      String cs = new ContentType(contentType).getParameter("charset");
      if (cs != null) { charset = cs ; }
    }
    BufferedReader reader = new BufferedReader(new InputStreamReader(is , charset));
    String inputLine;
    
    while ((inputLine = reader.readLine()) != null) {
      messageBody.append(inputLine + "\n");
    }
    node.setProperty(Utils.EXO_BODY, messageBody.toString());
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
  
  public void importMessage(SessionProvider sProvider, String username, String accountId, String folderId, InputStream inputStream, String type) throws Exception {
  	emlImportExport_.importMessage(sProvider, username, accountId, folderId, inputStream, type) ;
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
    }
    return account ;
  }
}