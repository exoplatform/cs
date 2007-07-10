/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.Contact;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.Group;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.MessageFilter;
import org.exoplatform.mail.service.MessageHeader;
import org.exoplatform.registry.JCRRegistryService;
import org.exoplatform.registry.ServiceRegistry;
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
  
  /**
   * @param username
   * @return
   * @throws Exception
   */
  public List<Account> getAccounts(String username) throws Exception {
    // get all accounts of the specified user
    List<Account> accounts = new ArrayList<Account>();
    Node homeNode = getMailHomeNode(username);
    NodeIterator it = homeNode.getNodes();
    while (it.hasNext()) {
      // browse the accounts and add them to the return list
      Node node = it.nextNode();
      if (node.isNodeType("exo:account")) {
        Account account = new Account();
        account.setId(node.getProperty("exo:id").toString());
        account.setLabel(node.getProperty("exo:label").toString());
        account.setUserDisplayName(node.getProperty("exo:userDisplayName").toString());
        account.setEmailAddress(node.getProperty("exo:emailAddress").toString());
        account.setEmailReplyAddress(node.getProperty("exo:emailReplyAddress").toString());
        account.setSignature(node.getProperty("exo:signature").toString());
        account.setDescription(node.getProperty("exo:description").toString());
        accounts.add(account);
      }
    }
    return accounts ;
  }
  
  public Account getAccountById(String username, String id) throws Exception {
    // get the account of the specified user with the specified id
    Account account = null;
    QueryManager qm = getMailHomeNode(username).getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("//element(*,exo:account)[@exo:id='").
                                  append(id).
                                  append("']");
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    NodeIterator it = result.getNodes();
    // if an account is found, creates the object
    if (it.hasNext()) {
      Node node = it.nextNode();
      account = new Account();
      account.setId(node.getProperty("exo:id").getString());
      account.setLabel(node.getProperty("exo:label").getString());
      account.setUserDisplayName(node.getProperty("exo:userDisplayName").getString());
      account.setEmailAddress(node.getProperty("exo:emailAddress").getString());
      account.setEmailReplyAddress(node.getProperty("exo:emailReplyAddress").getString());
      account.setSignature(node.getProperty("exo:signature").getString());
      account.setDescription(node.getProperty("exo:description").getString());
    }
    return account ;
  }
  
  public void saveAccount(String username, Account account, boolean isNew) throws Exception {
    // creates or updates an account, depending on the isNew flag
    if (isNew) createAccount(username, account);
    else updateAccount(username, account);
  }
  
  public void updateAccount(String username, Account account) throws Exception {
    // gets the specified account
    Node accountHome = getMailHomeNode(username) ;
    Node myAccount = accountHome.getNode(account.getUserDisplayName());
    // modifies some properties
    myAccount.setProperty("exo:label", account.getLabel());
    myAccount.setProperty("exo:userDisplayName", account.getUserDisplayName());
    myAccount.setProperty("exo:emailAddress", account.getEmailAddress());
    myAccount.setProperty("exo:emailReplyAddress", account.getEmailReplyAddress());
    myAccount.setProperty("exo:signature", account.getSignature());
    myAccount.setProperty("exo:description", account.getDescription());
    // saves the changes
    accountHome.save() ;
  }
  
  public void removeAccount(String username, Account account) throws Exception {
    Node accountHome = getMailHomeNode(username) ;
    // gets the specified account, and removes it
    accountHome.getNode(account.getUserDisplayName()).remove();
    
    accountHome.save() ;
  }
  
  public Folder getFolder(String username, String accountId, String folderName) throws Exception {
    // gets the folder of the specified id (from any account of the user username)
    Folder folder = null;
    Node node = getFolderHome(username, accountId).getNode(folderName);
    // if this folder exists, creates the object and returns it
    if (node != null) {
      folder = new Folder();
      folder.setLabel(node.getProperty("exo:label").getString());
      folder.setName(node.getProperty("exo:name").getString());
      folder.setNumberOfUnreadMessage((int)node.getProperty("exo:unreadMessages").getLong());
    }
    return folder ;
  }
  
  public void saveUserFolder(String username, String accountId, Folder folder) throws Exception {
    // gets the specified account node
//    Session sess = getMailHomeNode(username).getSession();
    Node home = getFolderHome(username, accountId);
    Node myFolder = home.getNode(folder.getName());
    if (myFolder == null) {
      // if it doesn't exist, creates it
      myFolder = home.addNode(folder.getName(), "exo:folder");
    }
    myFolder.setProperty("exo:label", folder.getLabel());
    myFolder.setProperty("exo:unreadMessages", folder.getNumberOfUnreadMessage());
    myFolder.setProperty("exo:name", folder.getName());
//    sess.save();
    myFolder.save();
  }
  

  public void removeUserFolder(String username, Folder folder) throws Exception {
    // gets the specified folder
    Session sess = getMailHomeNode(username).getSession();
    QueryManager qm = sess.getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("//element(*,exo:folder)[@exo:name='").
                                  append(folder.getName()).
                                  append("']");
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    NodeIterator it = result.getNodes();
    // if it exists, removes it
    if (it.hasNext()) it.nextNode().remove();
    sess.save();
  }
  
  public void removeUserFolder(String username, Account account, Folder folder) throws Exception {
    //  gets the specified folder
    Node myFolder = getFolderHome(username, account.getId()).getNode(folder.getName());
    if (myFolder != null) {
      myFolder.remove();
    }
    myFolder.save();
//    Session sess = myFolder.getSession();
//    sess.save();
  }
  
  public Message getMessageById(String username, String messageName, String accountId) throws Exception {
    // gets a message (email) from its id and username
    Message msg = null;
    Node messages = getMessageHome(username, accountId);
    Node message = messages.getNode(messageName);
    //  if this message exists, creates the object and returns it
    if (message != null) {
      msg = new Message();
      msg.setMessageTo(message.getProperty("exo:to").getString());
      msg.setSubject(message.getProperty("exo:subject").getString());
      msg.setMessageCc(message.getProperty("exo:cc").getString());
      msg.setMessageBcc(message.getProperty("exo:bcc").getString());
      msg.setMessageBody(message.getProperty("exo:content").getString());
//      NodeIterator pit = message.getNodes("nt:file");
//      List<Attachment> attachments = new ArrayList<Attachment>();
//      while (pit.hasNext()) {
//        Node content = pit.nextNode().getNode("jcr:content");
//        attachments.add(new Attachment(content));
//      }
//      msg.setAttachements(attachments);
    }
    return msg ;
  }
  
  public void removeMessage(String username, String messageName, String accountId) throws Exception {
//    Session sess = getMailHomeNode(username).getSession();
    Node messages = getMessageHome(username, accountId);
    //  gets the specified message
    Node message = messages.getNode(messageName);
    //  removes it
    if (message != null) message.remove();
    message.save();
//    sess.save();
  }
  
  public void removeMessage(String username, String[] messageName, String accountId) throws Exception {
    // loops on the message names array, and removes each message
    for (int i=0; i<messageName.length; i++) {
      removeMessage(username, messageName[i], accountId);
    }
  }
  
  public List<MessageHeader> getMessageByFolder(String username, Folder folder, String accountId) throws Exception {
    // gets all the messages from the specified folder
    List<MessageHeader> list = new ArrayList<MessageHeader>();
    Node folderHome = getFolderHome(username, accountId);
    Node myFolder = folderHome.getNode(folder.getName());
    // if the folder exists, gets the messages in it (exo:mail) to the return list
    if (myFolder != null) {
      NodeIterator nit = myFolder.getNodes();
      while (nit.hasNext()) {
        Node message = nit.nextNode();
        if (message.isNodeType("exo:mail")) {
          MessageHeader header = new MessageHeader();
          //header.setId(message.getName());
          //header.setFolderId(folder.getId());
          list.add(header);
        }
      }
    }
    return list ;
  }
  
//  public void updateMessage(String username, String accountId, String messageName, Message message) throws Exception {
//    Message msg = getMessageById(username, messageName, accountId);
//    if (msg != null) {
//      msg.setAttachements(message.getAttachments());
//      msg.setFolders(message.getFolders());
//      msg.setId(message.getId());
//      msg.setMessageBcc(message.getMessageBcc());
//      msg.setMessageBody(message.getMessageBody());
//      msg.setMessageCc(message.getMessageCc());
//      msg.setMessageTo(message.getMessageTo());
//      msg.setReceivedDate(message.getReceivedDate());
//      msg.setSendDate(message.getSendDate());
//      msg.setSubject(message.getSubject());
//      msg.setTags(message.getTags());
//      msg.setUnread(message.isUnread());
//    }
//    getMailHomeNode(username).save();
//  }
  
  public List<MessageHeader> getMessageByFilter(String username, MessageFilter filter) throws Exception {
    Node homeMsg = getMessageHome(username, filter.getAccountId());
    List<MessageHeader> list = new ArrayList<MessageHeader>();
    NodeIterator mit = homeMsg.getNodes();
    while (mit.hasNext()) {
      boolean addToList = false;
      Node msg = mit.nextNode();
      Message message = getMessageById(username, msg.getName(), filter.getAccountId());
      addToList |= message.getMessageBody().contains(filter.getBody());
      addToList |= message.getSubject().contains(filter.getSubject());
      //addToList |= message.getFolders()...
      addToList |= message.getAccountId().equalsIgnoreCase(filter.getAccountId());
      String[] tags = message.getTags();
      String[] filterTags = filter.getTag();
      for (int i = 0; i < tags.length; i++) {
        for (int j = 0; j < filterTags.length; j++) {
          if (tags[i].equalsIgnoreCase(filterTags[j])) {
            addToList |= true;
            break ;
          }
        }
      }
      if (addToList) {
        MessageHeader header = new MessageHeader();
        header.setId(msg.getName());
        list.add(header);
      }
    }
    return list ;
  }

  public void sendMessage(Message message) throws Exception {
    // sends an email with the parameters in message
    Properties props = new Properties();
    props.put("mail.smtp.host", "smtp.jcom.net");
    javax.mail.Session session = javax.mail.Session.getDefaultInstance(props, null);
    javax.mail.Message msg = new MimeMessage(session);
    InternetAddress addressFrom = new InternetAddress("");
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
    Node homeTags = getTagHome(username, message.getAccountId());
    if (homeTags.getNode(tag) == null) {
      // if the tag doesn't exist already, adds it to the tree
      homeTags.addNode(tag);
    }
    // gets the tags from the message
    String[] tags = message.getTags();
    boolean addTag = true;
    // if the message already has the tag, addTag will be set to false
    for (int i=0; i<tags.length && addTag; i++) addTag &= tags[i].equalsIgnoreCase(tag);
    if (addTag) {
      tags[tags.length] = tag;
      Node homeMsg = getMessageHome(username, message.getAccountId());
      NodeIterator it = homeMsg.getNodes();
      while (it.hasNext()) {
        Node msg = it.nextNode();
        // if we find the node representing the message, we modify its property tags
        if (msg.getProperty("exo:receivedDate").equals(message.getReceivedDate())) {
          msg.setProperty("exo:tags", tags);
          msg.save();
          break;
        }
      }
    }
    homeTags.save();
    
  }
  
  public void removeTag(String username, Account account, String tag) throws Exception {
    MessageFilter filter = new MessageFilter("filter by tag "+tag);
    String[] tags = {tag};
    filter.setTag(tags);
    List<MessageHeader> list = getMessageByFilter(username, filter);
    if (list.size() > 0) {
      Iterator<MessageHeader> it = list.iterator();
      while (it.hasNext()) {
        MessageHeader header = it.next();
        Message message = getMessageById(username, header.getId(), account.getId());
        removeTag(username, message, tag);
      }
    }
  }

  public void removeTag(String username, Message message, String tag) throws Exception {
    String[] tags = message.getTags();
    Node msgNode = null;
    Node homeMsg = getMessageHome(username, message.getAccountId());
    NodeIterator it = homeMsg.getNodes();
    while (it.hasNext()) {
      Node msg = it.nextNode();
      // if we find the node representing the message, we modify its property tags
      if (msg.getProperty("exo:receivedDate").equals(message.getReceivedDate())) {
        msgNode = msg;
        break;
      }
    }
    if (msgNode != null) {
      for (int i=0; i<tags.length; i++) {
        if (!tags[i].equalsIgnoreCase(tag)) msgNode.setProperty("exo:tags", tags[i]);
      }
    }
  }
  
  public int checkNewMessage(String username, Account account) throws Exception {
    // TODO Auto-generated method stub
    return 0;
  }


  public void createAccount(String username, Account account) throws Exception {
    // creates the node
    Node mailHome = getMailHomeNode(username) ;
    Node newAccount = mailHome.addNode(account.getUserDisplayName(), "exo:account");
    // add some properties
    newAccount.setProperty("exo:id", account.getId());
    newAccount.setProperty("exo:label", account.getLabel());
    newAccount.setProperty("exo:userDisplayName", account.getUserDisplayName());
    newAccount.setProperty("exo:emailAddress", account.getEmailAddress());
    newAccount.setProperty("exo:emailReplyAddress", account.getEmailReplyAddress());
    newAccount.setProperty("exo:signature", account.getSignature());
    newAccount.setProperty("exo:description", account.getDescription());
    // saves changes
    mailHome.getSession().save() ;
  }  

  public void addContact(String username, Contact contact) throws Exception {
    // TODO Auto-generated method stub
  }
}