/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.AccountData;
import org.exoplatform.mail.service.Attachment;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.MailServerConfiguration;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.MessageFilter;
import org.exoplatform.mail.service.MessageHeader;
import org.exoplatform.mail.service.BufferAttachment;
import org.exoplatform.registry.JCRRegistryService;
import org.exoplatform.services.jcr.RepositoryService;
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
  
  public List<MessageHeader> getMessageByFolder(String username, Folder folder, String accountId) throws Exception {
    return storage_.getMessageByFolder(username, folder, accountId);
  }
 
  
  public List<MessageHeader> getMessageByFilter(String username, MessageFilter filter) throws Exception {
    return storage_.getMessages(username, filter);
  }

  public void saveMessage(String username, String accountId, Message message, boolean isNew) throws Exception {
    storage_.saveMessage(username, accountId, message, isNew);
  }
  
  public void sendMessage(Message message) throws Exception {
    // sends an email with the parameters from message
    Properties props = new Properties();
    props.put("mail.smtp.host", "smtp.jcom.net");
    javax.mail.Session session = javax.mail.Session.getDefaultInstance(props, null);
    javax.mail.Message msg = new MimeMessage(session);
    InternetAddress addressFrom = new InternetAddress("philippe.aristote@gmail.com");
    msg.setFrom(addressFrom);

    InternetAddress[] addressTo = new InternetAddress[1];
    addressTo[0] = new InternetAddress(message.getMessageTo());
    msg.setRecipients(javax.mail.Message.RecipientType.TO, addressTo);
   
    // Optional : You can also set your custom headers in the Email if you Want
    msg.addHeader("MyHeaderName", "myHeaderValue");

    // Setting the Subject and Content Type
    msg.setSubject(message.getSubject());
    msg.setContent(message.getMessageBody(), "text/plain");
    Transport.send(msg);
  }

  public void addTag(String username, Message message, String tag) throws Exception {
    Node homeTags = storage_.getTagHome(username, message.getAccountId());
    if (!homeTags.hasNode(tag)) { // if the tag doesn't exist in jcr, we create it
      homeTags.addNode(tag, "exo:tag");
    }
    // gets the tags from the message
    String[] tags = message.getTags();
    // creates a new array that will contain all the existing tags, plus the new one
    String[] newtags = new String[tags.length+1];

    boolean addTag = true;
    // if the message already has the tag, addTag will be set to false
    for (int i=0; i<tags.length && addTag; i++) {
      addTag &= !tags[i].equalsIgnoreCase(tag);
      newtags[i] = tags[i];
    }
    if (addTag) {
      // adds the new tag to the array
      newtags[tags.length] = tag;
      Node homeMsg = storage_.getMessageHome(username, message.getAccountId());
      NodeIterator it = homeMsg.getNodes();
      while (it.hasNext()) {
        Node msg = it.nextNode();
        // if we find the node representing the message, we modify its property tags.
        // since there is no id property in the message node, we use the received date information
        // to find the specified message (we consider that receivedDate is unique)
        if (msg.getProperty("exo:receivedDate").getLong() == message.getReceivedDate().getTime()) {
          msg.setProperty("exo:tags", newtags);
          break;
        }
      }
    }
    homeTags.getSession().save();
  }
  
  public void removeTag(String username, Account account, String tag) throws Exception {
    // creates a filter containing the specified tag, to find all messages tagged with tag
    MessageFilter filter = new MessageFilter("filter by tag "+tag);
    filter.setAccountId(account.getId());
    String[] tags = {tag};
    filter.setTag(tags);
    // creates the list of messages tagged with the specified tag
    List<MessageHeader> list = storage_.getMessages(username, filter);
    if (list.size() > 0) {
      Iterator<MessageHeader> it = list.iterator();
      while (it.hasNext()) {
        // the list contains Message objects, that inherit from MessageHeader
        Message message = (Message)it.next();
        // for each message tagged, removes the tag
        removeTag(username, message, tag);
      }
    }
    // gets the home tag node
    Node homeTags = storage_.getTagHome(username, account.getId());
    // deletes the node that contains the specified tag
    if (homeTags.hasNode(tag)) homeTags.getNode(tag).remove();
    homeTags.getSession().save();
  }

  public void removeTag(String username, Message message, String tag) throws Exception {
    String[] tags = message.getTags();
    String[] newtags = new String[tags.length];
    for (int i=0; i<tags.length; i++) {
      // we copy all the tags except the specified one
      if (!tags[i].equalsIgnoreCase(tag)) newtags[i] = tags[i];
    }
    message.setTags(newtags);
    // saves the message with the new tags
    storage_.saveMessage(username, message.getAccountId(), message, false);
  }
  
  public int checkNewMessage(String username, Account account) throws Exception {
    MailServerConfiguration conf = account.getConfiguration();
    System.out.println("\n ### Getting mail from " + conf.getHost() + " ... !");
    int totalMess = -1;
    try {
      Properties props = System.getProperties();
      String socketFactoryClass = "javax.net.SocketFactory";
      if (conf.isSsl()) socketFactoryClass = "javax.net.ssl.SSLSocketFactory";
      if(conf.getProtocol().equals("pop3")) {
        props.setProperty("mail.pop3.socketFactory.fallback", "false");
        props.setProperty( "mail.pop3.socketFactory.class", socketFactoryClass);
      } else if (conf.getProtocol().equals("imap")) {
        props.setProperty("mail.imap.socketFactory.fallback", "false");
        props.setProperty("mail.imap.socketFactory.class", socketFactoryClass);
      }
      
      javax.mail.Session session = javax.mail.Session.getDefaultInstance(props);
      URLName url = new URLName(conf.getProtocol(), conf.getHost(), Integer.valueOf(conf.getPort()), conf.getFolder(), conf.getUserName(), conf.getPassword()) ;
      Store store = session.getStore(url) ;
      store.connect();
      System.out.println("\n ### Connected !");
      javax.mail.Folder folder = store.getFolder(conf.getFolder());
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
          newMsg.setId(String.valueOf(receivedDate.getTime()));
          newMsg.setMessageBcc(getAddress(mes.getRecipients(javax.mail.Message.RecipientType.BCC)));
          newMsg.setMessageCc(getAddress(mes.getRecipients(javax.mail.Message.RecipientType.CC)));
          newMsg.setMessageTo(getAddress(mes.getRecipients(javax.mail.Message.RecipientType.TO)));
          newMsg.setSubject(mes.getSubject());
          newMsg.setFrom(getAddress(mes.getFrom()));
          newMsg.setUnread(true);
          newMsg.setReceivedDate(receivedDate);
          newMsg.setSendDate(mes.getSentDate());
          newMsg.setAttachements(new ArrayList<Attachment>());
          String[] folders = {conf.getFolder()};
          newMsg.setFolders(folders);
          Object obj = mes.getContent() ;
          if (obj instanceof Multipart) {
            setMultiPart((Multipart)obj, newMsg, username);
          } else {
            setPart(mes, newMsg, username);
          }
          storage_.saveMessage(username, account.getId(), newMsg, true);
          i ++ ;
        }
      }
      folder.close(false);
      store.close();
    } catch (Exception e) { e.printStackTrace(); }
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
          file.setId(storage_.getMessageHome(username, newMail.getAccountId()).getPath()+"/"+newMail.getId()+"/"+part.getFileName());
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
    String str = "" ;
    int i = 0;
    if(addr != null && addr.length > 0) {
      while (i < addr.length) {
        if(str.length() < 1)  {
          str = addr[i].toString() ;              
        }else {
          str = str + ", " + addr[i].toString() ;
        }           
        i++ ;
      }
    }   
    return str ;
  }

  public void createAccount(String username, Account account) throws Exception {
    saveAccount(username, account, true);
  }

}