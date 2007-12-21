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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.AuthenticationFailedException;
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
import org.exoplatform.mail.service.ServerConfiguration;
import org.exoplatform.mail.service.SpamFilter;
import org.exoplatform.mail.service.Tag;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.jcr.util.IdGenerator;

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

  public void removeAccount(SessionProvider sProvider, String username, Account account) throws Exception {
    storage_.removeAccount(sProvider, username, account);
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

  public void removeMessage(SessionProvider sProvider, String username, String accountId, String messageId) throws Exception {
    storage_.removeMessage(sProvider, username, accountId, messageId);
  }

  public void removeMessage(SessionProvider sProvider, String username,String accountId, List<String> messageIds) throws Exception {
    storage_.removeMessage(sProvider, username, accountId, messageIds);
  } 
  
  public void moveMessages(SessionProvider sProvider, String username,String accountId, String msgId, String currentFolderId, String destFolderId) throws Exception {
    storage_.moveMessages(sProvider, username, accountId, msgId, currentFolderId, destFolderId);
  }

  public MessagePageList getMessages(SessionProvider sProvider, String username, MessageFilter filter) throws Exception {
    return storage_.getMessages(sProvider, username, filter);
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
    if (message.getFrom() != null) {
      addressFrom = new InternetAddress(message.getFrom());
    } else {
      addressFrom = new InternetAddress(session.getProperties().getProperty(Utils.SVR_SMTP_USER));
    }
    mimeMessage.setFrom(addressFrom);
    if(message.getMessageTo() != null) {
      mimeMessage.setRecipients(javax.mail.Message.RecipientType.TO, InternetAddress.parse(message.getMessageTo()));
    }
    if(message.getMessageCc() != null) {
      mimeMessage.setRecipients(javax.mail.Message.RecipientType.CC, InternetAddress.parse(message.getMessageCc(), true));
    }
    if(message.getMessageBcc() != null) {   
      mimeMessage.setRecipients(javax.mail.Message.RecipientType.BCC, InternetAddress.parse(message.getMessageBcc(), false));
    }
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
        BufferAttachment attach = (BufferAttachment) att;
        InputStream is = attach.getInputStream();

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        ByteArrayDataSource byteArrayDataSource = new ByteArrayDataSource(is, att.getMimeType());
        mimeBodyPart.setDataHandler(new DataHandler(byteArrayDataSource));

        mimeBodyPart.setDisposition(Part.ATTACHMENT);
        mimeBodyPart.setFileName(attach.getName());
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
      URLName storeURL = new URLName(account.getProtocol(), account.getIncomingHost(), Integer.valueOf(account.getIncomingPort()), account.getIncomingFolder(), account.getIncomingUser(), account.getIncomingPassword()) ;
      Store store = session.getStore(storeURL) ;
      store.connect();
      
      javax.mail.Folder folder = store.getFolder(account.getIncomingFolder());
      folder.open(javax.mail.Folder.READ_ONLY);

      javax.mail.Message[] messages = folder.getMessages() ;
      totalNew = messages.length ;
      
      System.out.println(" #### Folder contains " + totalNew + " messages !");
      
      if (totalNew > 0) {
        int i = 0 ;
        while (i < totalNew) {
          javax.mail.Message msg = messages[i] ;
          Message newMsg = new Message();
          Calendar gc = GregorianCalendar.getInstance();
          Date receivedDate = gc.getTime();
          newMsg.setAccountId(account.getId());
          newMsg.setMessageTo(InternetAddress.toString(msg.getRecipients(javax.mail.Message.RecipientType.TO)));
          newMsg.setMessageCc(InternetAddress.toString(msg.getRecipients(javax.mail.Message.RecipientType.CC)));
          newMsg.setMessageBcc(InternetAddress.toString(msg.getRecipients(javax.mail.Message.RecipientType.BCC)));
          newMsg.setSubject(msg.getSubject());
          newMsg.setContentType(msg.getContentType());
          newMsg.setFrom(InternetAddress.toString(msg.getFrom()));
          newMsg.setReplyTo(InternetAddress.toString(msg.getReplyTo()));
          newMsg.setReceivedDate(receivedDate);
          newMsg.setSendDate(msg.getSentDate());
          newMsg.setSize(msg.getSize());
          newMsg.setUnread(true);
          newMsg.setHasStar(false);       
          newMsg.setPriority(Utils.PRIORITY_NORMAL);
          String[] xPriority = msg.getHeader("X-Priority");
          String[] importance = msg.getHeader("Importance");
          
          if (xPriority != null && xPriority.length > 0) {
            for (int j = 0 ; j < xPriority.length; j++) {
              newMsg.setPriority(Long.valueOf(msg.getHeader("X-Priority")[j].substring(0,1)));
            }          
          }
          
          if (importance != null && importance.length > 0) {
            for (int j = 0 ; j < importance.length; j++) {
              if (importance[j].equalsIgnoreCase("Low")) {
                newMsg.setPriority(Utils.PRIORITY_LOW);
              } else if (importance[j].equalsIgnoreCase("high")) {
                newMsg.setPriority(Utils.PRIORITY_HIGH);
              } 
            }
          }
          
          newMsg.setAttachements(new ArrayList<Attachment>());
          String[] folderIds = { Utils.createFolderId(accountId, account.getIncomingFolder(), false)};
          if (getSpamFilter(sProvider, username, account.getId()).checkSpam(msg)) {
            folderIds = new String[] { Utils.createFolderId(accountId, Utils.FD_SPAM, false) } ;
          }
          newMsg.setFolders(folderIds);
          Object obj = msg.getContent() ;
          if (obj instanceof Multipart) {
            setMultiPart((Multipart)obj, newMsg, username);
          } else {
            setPart(msg, newMsg, username);
          }
          
          boolean rootIsUnread = true; 
          // Use for conversation
          Message rootMsg  = getRootMessage(sProvider, username, accountId, newMsg) ;
          if (rootMsg != null) {
            if (!rootMsg.isUnread()) {
              rootIsUnread = false ;
            }
            newMsg = updateRootMessage(sProvider, username, accountId, rootMsg, newMsg);
          } else {
            newMsg.setMessageIds(new String[] {newMsg.getId()});
            Map<String, String> addressMap = new HashMap<String, String> () ;
            List<String> addressList = new ArrayList<String>(); 
            if (newMsg.getFrom() != null) addressList.addAll(new ArrayList<String>(Arrays.asList(Utils.getAddresses(newMsg.getFrom()))));
            if (newMsg.getMessageTo() != null) addressList.addAll(new ArrayList<String>(Arrays.asList(Utils.getAddresses(newMsg.getMessageTo()))));
            if (newMsg.getMessageCc() != null) addressList.addAll(new ArrayList<String>(Arrays.asList(Utils.getAddresses(newMsg.getMessageCc()))));
            if (newMsg.getMessageBcc() != null) addressList.addAll(new ArrayList<String>(Arrays.asList(Utils.getAddresses(newMsg.getMessageBcc()))));
            
            for(String value : addressList) {
              addressMap.put(value, value) ;
            }
            newMsg.setAddresses(addressMap.values().toArray(new String[] {}));
            newMsg.setRoot(newMsg.getId());
          }
          storage_.saveMessage(sProvider, username, account.getId(), newMsg, true);
          messageList.add(newMsg);
                    
          for(String folderId : folderIds) {
            Folder storeFolder = storage_.getFolder(sProvider, username, account.getId(), folderId) ;
            if(storeFolder == null) {
              storeFolder = new Folder() ;
              storeFolder.setId(folderId);
              storeFolder.setName(account.getIncomingFolder()) ;
              storeFolder.setLabel(account.getIncomingFolder()) ;
              storeFolder.setPersonalFolder(false) ;
            }  

            if (rootMsg == null || (!rootIsUnread)) {
              storeFolder.setNumberOfUnreadMessage((storeFolder.getNumberOfUnreadMessage() + 1)) ;
            } 
            if (rootMsg == null) {
              storeFolder.setTotalMessage(storeFolder.getTotalMessage() + 1) ;
            }
            storage_.saveFolder(sProvider, username, account.getId(), storeFolder) ;
          }
          
          i ++ ;
        }
      }
      folder.close(false);
      store.close();
    }  catch (Exception e) { 
      e.printStackTrace();
    }
    execFilters(sProvider, username, accountId);
    return messageList;
  }

  private void setMultiPart(Multipart multipart, Message newMail, String username) {
    try {
      int i = 0 ;
      int n = multipart.getCount() ;
      while( i < n) {
        setPart(multipart.getBodyPart(i), newMail, username);
        i++ ;
      }     
    }catch(Exception e) {
      e.printStackTrace() ;
    }   
  }

  private void setPart(Part part, Message newMail, String username){
    try {
      String disposition = part.getDisposition();
      String contentType = part.getContentType();
      if (disposition == null) {
        if (part.getContent() instanceof MimeMultipart) {
          MimeMultipart mimeMultiPart = (MimeMultipart) part.getContent() ;
          for (int i = 0; i< mimeMultiPart.getCount(); i++) {
            // for each part, set the body content
            setPart(mimeMultiPart.getBodyPart(i), newMail, username);
          }
        } else {
          setMessageBody(part, newMail);
        }
      } else {
        if (disposition.equalsIgnoreCase(Part.INLINE)) {
          /* this must be presented INLINE, hence inside the body of the message */
          if (part.isMimeType("text/plain") || part.isMimeType("text/html")) {
            setMessageBody(part, newMail);
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
          newMail.getAttachments().add(file);
        }
      }
    } catch(Exception e) {
      e.printStackTrace() ;
    }
  }

  private void setMessageBody(Part part, Message newMail) throws Exception {
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
    newMail.setMessageBody(messageBody.toString());
  }
  
  private Message getRootMessage(SessionProvider sProvider, String username, String accountId, Message newMsg) throws Exception {
    MessageFilter filter = new MessageFilter("");
    filter.setAccountId(accountId);
    filter.setSubjectCondition(Utils.CONDITION_IS);
    String subject = newMsg.getSubject() ;
    subject = subject.replace("&", "&amp;");
    if (subject.indexOf("Re:") == 0) {
      subject = subject.substring(3, subject.length()).trim();
    } else if(subject.indexOf("Fwd:") == 0) {
      subject = subject.substring(4, subject.length()).trim();
    }
    filter.setSubject(subject);
    List<Message> msgList = getMessages(sProvider, username, filter).getAll(username);
    if (msgList != null && msgList.size() > 0) {
      for (Message msg : msgList) {
        if (msg.isRootConversation()) {
          List<String> addressList = new ArrayList<String>() ;
          if (msg.getAddresses() != null && msg.getAddresses().length > 0) {
            addressList.addAll(new ArrayList<String>(Arrays.asList(msg.getAddresses())));
          }
          if (msg.getFrom() != null) addressList.addAll(new ArrayList<String>(Arrays.asList(Utils.getAddresses(msg.getFrom()))));
          if (msg.getMessageTo() != null) addressList.addAll(new ArrayList<String>(Arrays.asList(Utils.getAddresses(msg.getMessageTo()))));
          if (msg.getMessageCc() != null) addressList.addAll(new ArrayList<String>(Arrays.asList(Utils.getAddresses(msg.getMessageCc()))));
          if (msg.getMessageBcc() != null) addressList.addAll(new ArrayList<String>(Arrays.asList(Utils.getAddresses(msg.getMessageBcc()))));
          if (addressList.indexOf(Utils.getAddresses(newMsg.getFrom())[0]) > -1) {
            return msg;
          }
        }
      }
    }
    return null;
  }
  
  private Message updateRootMessage(SessionProvider sProvider, String username, String accountId, Message rootMsg, Message newMsg) throws Exception {
    Map<String, String> addressMap = new HashMap<String, String> () ;
    List<String> addressList = new ArrayList<String>();
    if (rootMsg.getAddresses() != null && rootMsg.getAddresses().length > 0) {
      addressList.addAll(new ArrayList<String>(Arrays.asList(rootMsg.getAddresses())));
    }
    for(String value : addressList) {
      addressMap.put(value, value) ;
    }
    
    Map<String, String> newAddressMap = new HashMap<String, String> () ;
    List<String> newAddressList = new ArrayList<String>(); 
    if (newMsg.getFrom() != null) newAddressList.addAll(new ArrayList<String>(Arrays.asList(Utils.getAddresses(newMsg.getFrom()))));
    if (newMsg.getMessageTo() != null) newAddressList.addAll(new ArrayList<String>(Arrays.asList(Utils.getAddresses(newMsg.getMessageTo()))));
    if (newMsg.getMessageCc() != null) newAddressList.addAll(new ArrayList<String>(Arrays.asList(Utils.getAddresses(newMsg.getMessageCc()))));
    if (newMsg.getMessageBcc() != null) newAddressList.addAll(new ArrayList<String>(Arrays.asList(Utils.getAddresses(newMsg.getMessageBcc()))));
    
    for(String value : newAddressList) {
      newAddressMap.put(value, value) ;
    }
    addressMap.putAll(newAddressMap);
    newMsg.setAddresses(addressMap.values().toArray(new String[]{}));
    
    List<String> msgIdList = new ArrayList<String>();
    if (rootMsg.getMessageIds() != null && rootMsg.getMessageIds().length > 0) {
      msgIdList.addAll(new ArrayList<String>(Arrays.asList(rootMsg.getMessageIds())));
    }
    msgIdList.add(newMsg.getId());
    newMsg.setMessageIds(msgIdList.toArray(new String[]{}));
    
    rootMsg.setIsRootConversation(false);
    rootMsg.setUnread(false);
    rootMsg.setRoot(newMsg.getId());
    rootMsg.setAddresses(new String[] {});
    rootMsg.setMessageIds(new String[] {});
    saveMessage(sProvider, username, accountId, rootMsg, false);
    return newMsg ;
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

  public void addTag(SessionProvider sProvider, String username, String accountId, List<String> messagesId, List<Tag> tag)
      throws Exception {
    storage_.addTag(sProvider, username, accountId, messagesId, tag);
  }

  public List<Tag> getTags(SessionProvider sProvider, String username, String accountId) throws Exception {
    return storage_.getTags(sProvider, username, accountId);
  }
  
  public Tag getTag(SessionProvider sProvider, String username, String accountId, String tagId) throws Exception {
    return storage_.getTag(sProvider, username, accountId, tagId);
  }

  public void removeMessageTag(SessionProvider sProvider, String username, String accountId, List<String> messageIds, List<String> tags)
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
    return getMessages(sProvider, username, filter) ;   
  }
  
  public MessagePageList getMessagePageListByFolder(SessionProvider sProvider, String username, String accountId, String folderId) throws Exception {
    MessageFilter filter = new MessageFilter("Filter By Folder") ;
    filter.setAccountId(accountId) ;
    filter.setFolder(new String[]{folderId} ) ;
    return getMessages(sProvider, username, filter) ;   
  }
  
  public MailSetting getMailSetting(SessionProvider sProvider, String username) throws Exception {
    return storage_.getMailSetting(sProvider, username);
  }  
  
  public void saveMailSetting(SessionProvider sProvider, String username, MailSetting newSetting) throws Exception {
    storage_.saveMailSetting(sProvider, username, newSetting);
  }
  
  public String getCurrentAccount(SessionProvider sProvider, String username) throws Exception{
  	return storage_.getCurrentAccount(sProvider, username) ;
  }
  public void updateCurrentAccount(SessionProvider sProvider, String username, String accountId) throws Exception{
  	storage_.updateCurrentAccount(sProvider, username, accountId) ;
  }
  
  public void importMessage(SessionProvider sProvider, String username, String accountId, String folderId, InputStream inputStream, String type) throws Exception {
    /*Properties props = System.getProperties();
    Session session = Session.getDefaultInstance(props, null);
    MimeMessage mimeMessage = new MimeMessage(session, inputStream);
    Message message = new Message();
    message.setAccountId(accountId);
    message = Utils.mergeFromMimeMessage(message, mimeMessage);
    message.setFolders(new String[] {folderId});
    saveMessage(sProvider, username, accountId, message, true);*/
  	emlImportExport_.importMessage(sProvider, username, accountId, folderId, inputStream, type) ;
  }
  
  public OutputStream exportMessage(SessionProvider sProvider, String username, String accountId, String messageId) throws Exception {
    /*Properties props = System.getProperties();
    Session session = Session.getDefaultInstance(props, null);
    Message message = getMessageById(sProvider, username, accountId, messageId);
    MimeMessage mimeMessage = new MimeMessage(session);
    mimeMessage = Utils.mergeToMimeMessage(message, mimeMessage);
    OutputStream outputStream = new ByteArrayOutputStream();
    mimeMessage.writeTo(outputStream);*/
    return emlImportExport_.exportMessage(sProvider, username, accountId, messageId) ; 
  }
  
  public void execFilters(SessionProvider sProvider, String username, String accountId) throws Exception {
    storage_.execFilters(sProvider, username, accountId);
  }
  
  public SpamFilter getSpamFilter(SessionProvider sProvider, String username, String accountId) throws Exception {
    return storage_.getSpamFilter(sProvider, username, accountId);
  }
  
  public void saveSpamFilter(SessionProvider sProvider, String username, String accountId, SpamFilter spamFilter) throws Exception {
    storage_.saveSpamFilter(sProvider, username, accountId, spamFilter);
  }
  
  public void toggleMessageProperty(SessionProvider sProvider, String username, String accountId, List<String> msgList, String property) throws Exception {
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
}