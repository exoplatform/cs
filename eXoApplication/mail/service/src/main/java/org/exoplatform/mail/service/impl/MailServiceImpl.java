/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.service.impl;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePartDataSource;
import javax.mail.util.ByteArrayDataSource;

import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.AccountData;
import org.exoplatform.mail.service.Attachment;
import org.exoplatform.mail.service.BufferAttachment;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.MessageFilter;
import org.exoplatform.mail.service.MessageHeader;
import org.exoplatform.mail.service.Tag;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.registry.JCRRegistryService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan.nguyen@exoplatform.com
 * Jun 23, 2007  
 */
public class MailServiceImpl implements MailService{

  private JCRDataStorage storage_ ;

  public MailServiceImpl(RepositoryService  repositoryService, 
      JCRRegistryService jcrRegistryService) throws Exception {
    storage_ = new JCRDataStorage(repositoryService, jcrRegistryService) ;      
  }

  public List<AccountData> getAccountDatas() throws Exception {
    return null ;
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

  public void removeAccount(String username, Account account) throws Exception {
    storage_.removeAccount(username, account);
  }

  public Folder getFolder(String username, String accountId, String folderName) throws Exception {
    return storage_.getFolder(username, accountId, folderName);
  }

  public void saveUserFolder(String username, String accountId, Folder folder) throws Exception {
    storage_.saveUserFolder(username, accountId, folder);
  }


  public void removeUserFolder(String username, Folder folder) throws Exception {
    storage_.removeUserFolder(username, folder);
  }

  public void removeUserFolder(String username, Account account, Folder folder) throws Exception {
    storage_.removeUserFolder(username, account, folder);
  }

  public Message getMessageById(String username, String messageName, String accountId) throws Exception {
    return storage_.getMessageById(username, accountId, messageName);
  }

  public void removeMessage(String username, String messageName, String accountId) throws Exception {
    storage_.removeMessage(username, accountId, messageName);
  }

  public void removeMessage(String username, String[] messageName, String accountId) throws Exception {
    storage_.removeMessage(username, accountId, messageName);
  } 

  public List<MessageHeader> getMessages(String username, MessageFilter filter) throws Exception {
    return storage_.getMessages(username, filter);
  }

  public void saveMessage(String username, String accountId, Message message, boolean isNew) throws Exception {
    storage_.saveMessage(username, accountId, message, isNew);
  }

  public void sendMessage(String username, Message message) throws Exception {
    String accountId = message.getAccountId() ;
    Account acc = getAccountById(username, accountId) ;
    String smtpUser = acc.getServerProperties().get(Utils.SVR_SMTP_USER) ;
    String host = acc.getServerProperties().get(Utils.SVR_SMTP_HOST) ;
    String port  = acc.getServerProperties().get(Utils.SVR_SMTP_PORT) ;
    String isSSl =  acc.getServerProperties().get(Utils.SVR_SSL)  ;
    Properties props = new Properties();
    props.put(Utils.SVR_SMTP_USER, smtpUser) ;
    props.put(Utils.SVR_SMTP_HOST, host) ;
    props.put(Utils.SVR_SMTP_PORT, port) ;
    props.put(Utils.SVR_SSL, isSSl);
    props.put(Utils.SVR_SMTP_STARTTLS_ENABLE,"true");
    props.put(Utils.SVR_SMTP_AUTH, "true");
    props.put(Utils.SVR_SMTP_SOCKETFACTORY_PORT, port);
    props.put(Utils.SVR_SMTP_SOCKETFACTORY_CLASS,  "javax.net.ssl.SSLSocketFactory");
    props.put(Utils.SVR_SMTP_SOCKETFACTORY_FALLBACK, "false");
    Session session = Session.getInstance(props, null);
    Transport transport = session.getTransport(Utils.SVR_SMTP);
    transport.connect(host, acc.getUserName(), acc.getPassword()) ;
    javax.mail.Message msg = new MimeMessage(session);
    
    InternetAddress addressFrom = new InternetAddress(message.getFrom());
    msg.setFrom(addressFrom);
    msg.setRecipients(javax.mail.Message.RecipientType.TO, InternetAddress.parse(message.getMessageTo()));
    if(message.getMessageCc() != null) {
      msg.setRecipients(javax.mail.Message.RecipientType.CC, InternetAddress.parse(message.getMessageCc(), true));
    }
    if(message.getMessageBcc() != null) {   
      msg.setRecipients(javax.mail.Message.RecipientType.BCC, InternetAddress.parse(message.getMessageBcc(), false));
    }
    msg.setSubject(message.getSubject());
    msg.setSentDate(message.getSendDate());
   
    MimeBodyPart mimeBodyPart1 = new MimeBodyPart();
    mimeBodyPart1.setText(message.getMessageBody(), "us-ascii");
    mimeBodyPart1.setDisposition(Utils.INLINE);
    
    Multipart multiPart = new MimeMultipart();
    multiPart.addBodyPart(mimeBodyPart1);
    
    List<Attachment> attachList = message.getAttachments();
    for (Attachment att : attachList) {
      BufferAttachment attach = (BufferAttachment) att;
      InputStream is = attach.getInputStream();
          
      MimeBodyPart mimeBodyPart = new MimeBodyPart();
      ByteArrayDataSource byteArrayDataSource = new ByteArrayDataSource(is, att.getMimeType());
      mimeBodyPart.setDataHandler(new DataHandler(byteArrayDataSource));
      
      mimeBodyPart.setDisposition(Utils.ATTACHMENT);
      mimeBodyPart.setFileName(attach.getName());
      multiPart.addBodyPart(mimeBodyPart);
    }        
    msg.setContent(multiPart);
    
    msg.saveChanges();
    transport.sendMessage(msg, msg.getAllRecipients());
  }
  
  public void sendMessage(Message message) throws Exception {
  }

  public int checkNewMessage(String username, Account account) throws Exception {
    System.out.println("\n ### Getting mail from " + account.getHost() + " ... !");
    int totalMess = -1;
    try {
      Properties props = System.getProperties();
      String socketFactoryClass = "javax.net.SocketFactory";
      if (account.isSsl()) socketFactoryClass = "javax.net.ssl.SSLSocketFactory";
      if(account.getProtocol().equals("pop3")) {
        props.setProperty("mail.pop3.socketFactory.fallback", "false");
        props.setProperty( "mail.pop3.socketFactory.class", socketFactoryClass);
      } else if (account.getProtocol().equals("imap")) {
        props.setProperty("mail.imap.socketFactory.fallback", "false");
        props.setProperty("mail.imap.socketFactory.class", socketFactoryClass);
      }
      javax.mail.Session session = javax.mail.Session.getDefaultInstance(props);
      URLName url = new URLName(account.getProtocol(), account.getHost(), Integer.valueOf(account.getPort()), account.getFolder(), account.getUserName(), account.getPassword()) ;
      Store store = session.getStore(url) ;
      store.connect();
      System.out.println("\n ### Connected !");
      javax.mail.Folder folder = store.getFolder(account.getFolder());
      folder.open(javax.mail.Folder.READ_ONLY);

      // gets the new messages from the folder specified in the configuration object
      javax.mail.Message[] mess = folder.getMessages() ;
      totalMess = mess.length ;
      System.out.println("\n ### Folder contains "+totalMess+" messages !");
      if(totalMess > 0) {
        int i = 0 ;
        while(i < totalMess){
          // for each new email, creates a Message object and saves it in the repository
          javax.mail.Message mes = mess[i] ;
          Message newMsg = new Message();
          Calendar gc = GregorianCalendar.getInstance();
          Date receivedDate = gc.getTime();
          newMsg.setAccountId(account.getId());
          newMsg.setMessageBcc(getAddress(mes.getRecipients(javax.mail.Message.RecipientType.BCC)));
          newMsg.setMessageCc(getAddress(mes.getRecipients(javax.mail.Message.RecipientType.CC)));
          newMsg.setMessageTo(getAddress(mes.getRecipients(javax.mail.Message.RecipientType.TO)));
          newMsg.setSubject(mes.getSubject());
          newMsg.setFrom(getAddress(mes.getFrom()));
          newMsg.setReplyTo(getAddress(mes.getReplyTo()));
          newMsg.setUnread(true);
          newMsg.setReceivedDate(receivedDate);
          newMsg.setSendDate(mes.getSentDate());
          newMsg.setSize(mes.getSize());
          newMsg.setAttachements(new ArrayList<Attachment>());
          String[] folders = {account.getFolder()};
          newMsg.setFolders(folders);
          Object obj = mes.getContent() ;
          if (obj instanceof Multipart) {
            setMultiPart((Multipart)obj, newMsg, username);
          } else {
            setPart(mes, newMsg, username);
          }
          storage_.saveMessage(username, account.getId(), newMsg, true);
          i ++ ;
          for(String f : folders) {
            Folder fd = storage_.getFolder(username, account.getId(), f) ;
            if(fd == null) {
              fd = new Folder() ;
              fd.setName(f) ;
              fd.setLabel(f) ;
              fd.setPersonalFolder(false) ;
            }  
            fd.setNumberOfUnreadMessage(fd.getNumberOfUnreadMessage()+1) ;
            storage_.saveUserFolder(username, account.getId(), fd) ;
          }
        }
      }
      folder.close(false);
      store.close();
    }  catch (Exception e) { 
      throw e ;
    }
    return totalMess;
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
        if (part.isMimeType("text/plain") || part.isMimeType("text/html")) {
          newMail.setMessageBody((String)part.getContent());
        } else {
          MimeMultipart mimeMultiPart = (MimeMultipart)part.getContent() ;
          for (int i=0; i<mimeMultiPart.getCount();i++) {
            // for each part, set the body content
            setPart(mimeMultiPart.getBodyPart(i), newMail, username);
          }
        }
      } else {
        if (disposition.equalsIgnoreCase(Part.INLINE)) {
          // this must be presented INLINE, hence inside the body of the message
          if (part.isMimeType("text/plain") || part.isMimeType("text/html")) {
            newMail.setMessageBody((String)part.getContent());
          }
        } else {
          // this part must be presented as an attachment, hence we add it to the attached files
          BufferAttachment file = new BufferAttachment();
          file.setId("Attachment" + IdGenerator.generate());
          //file.setId(storage_.getMessageHome(username, newMail.getAccountId()).getPath()+"/"+newMail.getId()+"/"+part.getFileName());
          file.setName(part.getFileName());
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
    }catch(Exception e) {
      e.printStackTrace() ;
    }
  }
  private String getAddress(javax.mail.Address[] addr) {
    InternetAddress[] internetAddress = ((InternetAddress[]) addr);
    String str = "" ;
    int i = 0;
    if(internetAddress != null && internetAddress.length > 0) {
      while (i < internetAddress.length) {
        String personal = internetAddress[i].getPersonal();
        String address = internetAddress[i].getAddress();
        String sender =  address + "<" + address + ">";
        if (personal != null && personal != "") 
          sender = personal + " <" + address + ">";
        if(str.length() < 1)  {
          str = sender ;              
        }else {
          str += "," + sender ;
        }           
        i++ ;
      }
    }   
    return str;
  }

  public void createAccount(String username, Account account) throws Exception {
    saveAccount(username, account, true);
  }

  public List<Folder> getFolders(String username, String accountId, boolean isPersonal) throws Exception {
    List<Folder> folders = new ArrayList<Folder>() ;
    for(Folder folder : storage_.getFolders(username, accountId))  
      if(isPersonal) {
        if(folder.isPersonalFolder()) folders.add(folder) ;
      } else {
        if(!folder.isPersonalFolder()) folders.add(folder) ;
      }
    return folders ;
  }

  public void addTag(String username, String accountId, List<String> messagesId, List<Tag> tag)
      throws Exception {
    storage_.addTag(username, accountId, messagesId, tag);
  }

  public List<Tag> getTags(String username, String accountId) throws Exception {
    return storage_.getTags(username, accountId);
  }

  public void removeMessageTag(String username, String accountId, List<String> messageIds, List<String> tags)
      throws Exception {
    storage_.removeMessageTag(username, accountId, messageIds, tags);   
  }

  public void removeTag(String username, String accountId, String tag) throws Exception {
    storage_.removeTag(username, accountId, tag);
  }

  public List<Message> getMessageByTag(String username, String accountId, String tagName)
      throws Exception {
    return storage_.getMessageByTag(username, accountId, tagName);
  }
}