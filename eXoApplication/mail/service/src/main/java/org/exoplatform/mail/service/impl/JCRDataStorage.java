/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.Attachment;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.MessageFilter;
import org.exoplatform.mail.service.MessageHeader;
import org.exoplatform.mail.service.BufferAttachment;
import org.exoplatform.registry.JCRRegistryService;
import org.exoplatform.registry.ServiceRegistry;
import org.exoplatform.services.jcr.RepositoryService;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan.nguyen@exoplatform.com
 * Jun 23, 2007  
 */
public class JCRDataStorage implements DataStorage{
  private RepositoryService  repositoryService_ ;
  private JCRRegistryService jcrRegistryService_ ;
  //private SimpleCredentials credentials_ = new SimpleCredentials("exoadmin", "exo".toCharArray());
  
  public JCRDataStorage(RepositoryService  repositoryService, JCRRegistryService jcrRegistryService) {
    repositoryService_ = repositoryService ;
    jcrRegistryService_ = jcrRegistryService ;
  }
  

  public Account getAccountById(String username, String id) throws Exception {
    //  get the account of the specified user with the specified id
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
      if (node.hasProperty("exo:label")) account.setLabel(node.getProperty("exo:label").getString());
      if (node.hasProperty("exo:userDisplayName")) account.setUserDisplayName(node.getProperty("exo:userDisplayName").getString());
      if (node.hasProperty("exo:emailAddress")) account.setEmailAddress(node.getProperty("exo:emailAddress").getString());
      if (node.hasProperty("exo:emailReplyAddress")) account.setEmailReplyAddress(node.getProperty("exo:emailReplyAddress").getString());
      if (node.hasProperty("exo:signature")) account.setSignature(node.getProperty("exo:signature").getString());
      if (node.hasProperty("exo:description")) account.setDescription(node.getProperty("exo:description").getString());
    }
    return account ;
  }

  public List<Account> getAccounts(String username) throws Exception {
    //  get all accounts of the specified user
    List<Account> accounts = new ArrayList<Account>();
    Node homeNode = getMailHomeNode(username);
    NodeIterator it = homeNode.getNodes();
    while (it.hasNext()) {
      // browse the accounts and add them to the return list
      Node node = it.nextNode();
      if (node.isNodeType("exo:account")) {
        Account account = getAccountById(username, node.getProperty("exo:id").getString());
        accounts.add(account);
      }
    }
    return accounts ;
  }

  public Message getMessageById(String username, String accountId, String id) throws Exception {
    //  gets a message (email) from its id and username
    Message msg = null;
    Node messages = getMessageHome(username, accountId);
    //  if this message exists, creates the object and returns it
    if (messages.hasNode(id)) {
      Node message = messages.getNode(id);
      msg = new Message();
      msg.setAccountId(accountId);
      msg.setId(id);
      if (message.hasProperty("exo:to")) msg.setMessageTo(message.getProperty("exo:to").getString());
      if (message.hasProperty("exo:subject")) msg.setSubject(message.getProperty("exo:subject").getString());
      if (message.hasProperty("exo:cc")) msg.setMessageCc(message.getProperty("exo:cc").getString());
      if (message.hasProperty("exo:bcc")) msg.setMessageBcc(message.getProperty("exo:bcc").getString());
      if (message.hasProperty("exo:body")) msg.setMessageBody(message.getProperty("exo:body").getString());
      if (message.hasProperty("exo:tags")) {
        Value[] propTags = message.getProperty("exo:tags").getValues();
        String[] tags = new String[(int)propTags.length];
        for (int i = 0; i < propTags.length; i++) {
          tags[i] = propTags[i].getString();
        }
        msg.setTags(tags);
      }
      if (message.hasProperty("exo:folders")) {
        Value[] propFolders = message.getProperty("exo:folders").getValues();
        String[] folders = new String[(int)propFolders.length];
        for (int i = 0; i < propFolders.length; i++) {
          folders[i] = propFolders[i].getString();
        }
        msg.setFolders(folders);
      }
      NodeIterator msgIt = message.getNodes();
      List<Attachment> attachments = new ArrayList<Attachment>();
      while (msgIt.hasNext()) {
        Node node = msgIt.nextNode();
        if (node.isNodeType("nt:file")) {
          BufferAttachment file = new BufferAttachment();
          file.setId(node.getPath());
          file.setMimeType(node.getNode("jcr:content").getProperty("jcr:mimeType").getString());
          file.setName(node.getName());
          file.setInputStream(node.getNode("jcr:content").getProperty("jcr:data").getStream());
          attachments.add(file);
        }
      }
      msg.setAttachements(attachments);
      GregorianCalendar cal = new GregorianCalendar();
      if (message.hasProperty("exo:receivedDate")) {
        cal.setTimeInMillis(message.getProperty("exo:receivedDate").getLong());
        msg.setReceivedDate(cal.getTime());
      }
      if (message.hasProperty("exo:sendDate")) {
        cal.setTimeInMillis(message.getProperty("exo:sendDate").getLong());
        msg.setReceivedDate(cal.getTime());
      }
    }
    return msg ;
  }

  public List<MessageHeader> getMessages(String username, MessageFilter filter) throws Exception {
    Node homeMsg = getMessageHome(username, filter.getAccountId());
    List<MessageHeader> list = new ArrayList<MessageHeader>();
    NodeIterator mit = homeMsg.getNodes();
    while (mit.hasNext()) {
      boolean addToList = false;
      Node msg = mit.nextNode();
      Message message = getMessageById(username, filter.getAccountId(), msg.getName());
      if (filter.getSubject() != null) addToList |= message.getSubject().contains(filter.getSubject());
      // condition !addToList : doesn't check the other filters if the message already corresponds
      if (filter.getBody() != null && !addToList) addToList |= message.getMessageBody().contains(filter.getBody());
      if (filter.getFolder() != null && !addToList) {
        String[] folders = message.getFolders();
        String[] filterFolders = filter.getFolder();
        for (int i = 0; i < folders.length && !addToList; i++) { // !addToList : stop the loop if one folder matches
          for (int j = 0; j < filterFolders.length; j++) {
            if (folders[i].equalsIgnoreCase(filterFolders[j])) {
              addToList |= true;
              break ;
            }
          }
        }
      }
      if (filter.getTag() != null && !addToList) {
        String[] tags = message.getTags();
        String[] filterTags = filter.getTag();
        for (int i = 0; i < tags.length && !addToList; i++) { // !addToList : stop the loop if one tag matches
          for (int j = 0; j < filterTags.length; j++) {
            if (tags[i].equalsIgnoreCase(filterTags[j])) {
              addToList |= true;
              break ;
            }
          }
        }
      }
      if (addToList) list.add(message);
    }
    return list ;
  }
  
  public List<MessageHeader> getMessageByFolder(String username, Folder folder, String accountId) throws Exception {
    // gets all the messages from the specified folder, using a filter
    MessageFilter filter = new MessageFilter("filter by folder "+folder.getName());
    filter.setAccountId(accountId);
    String[] folders = {folder.getName()};
    filter.setFolder(folders);
    return getMessages(username, filter);
  }

  public void removeAccount(String username, Account account) throws Exception {
    Node accountHome = getMailHomeNode(username) ;
    // gets the specified account, and removes it
    accountHome.getNode(account.getUserDisplayName()).remove();
    
    accountHome.getSession().save() ;
  }

  public void removeMessage(String username, String accountId, String messageId) throws Exception {
    Node messages = getMessageHome(username, accountId);
    //  removes it
    if (messages.hasNode(messageId)) messages.getNode(messageId).remove();
    messages.getSession().save();
  }

  public void removeMessage(String username, String accountId, String[] messageId) throws Exception {
    //  loops on the message names array, and removes each message
    for (int i=0; i<messageId.length; i++) {
      removeMessage(username, messageId[i], accountId);
    }
  }

  public void saveAccount(String username, Account account, boolean isNew) throws Exception {
    // creates or updates an account, depending on the isNew flag
    Node mailHome = getMailHomeNode(username) ;
    Node newAccount = null;
    if (isNew) { // creates the node
      newAccount = mailHome.addNode(account.getUserDisplayName(), "exo:account");
    } else { // gets the specified account
      newAccount = mailHome.getNode(account.getUserDisplayName());
    }
    if (newAccount != null) {
      // add some properties
      newAccount.setProperty("exo:id", account.getId());
      newAccount.setProperty("exo:label", account.getLabel());
      newAccount.setProperty("exo:userDisplayName", account.getUserDisplayName());
      newAccount.setProperty("exo:emailAddress", account.getEmailAddress());
      newAccount.setProperty("exo:emailReplyAddress", account.getEmailReplyAddress());
      newAccount.setProperty("exo:signature", account.getSignature());
      newAccount.setProperty("exo:description", account.getDescription());
      // saves changes
      mailHome.getSession().save();
    }
  }

  public void saveMessage(String username, String accountId, Message message, boolean isNew) throws Exception {
    Node homeMsg = getMessageHome(username, accountId);
    Node nodeMsg = null;
    if (isNew) { // creates the node
      nodeMsg = homeMsg.addNode(message.getId(), "exo:message");
    } else { // gets the specified message
      nodeMsg = homeMsg.getNode(message.getId());
    }
    if (nodeMsg != null) {
      // add some properties
      nodeMsg.setProperty("exo:to", message.getMessageTo());
      nodeMsg.setProperty("exo:subject", message.getSubject());
      nodeMsg.setProperty("exo:cc", message.getMessageCc());
      nodeMsg.setProperty("exo:bcc", message.getMessageBcc());
      nodeMsg.setProperty("exo:body", message.getMessageBody());
      nodeMsg.setProperty("exo:isUnread", message.isUnread());
      nodeMsg.setProperty("exo:to", message.getMessageTo());
      if (message.getSendDate() != null)
        nodeMsg.setProperty("exo:sendDate", message.getSendDate().getTime());
      if (message.getReceivedDate() != null)
        nodeMsg.setProperty("exo:receivedDate", message.getReceivedDate().getTime());
      String[] tags = message.getTags();
      nodeMsg.setProperty("exo:tags", tags);
      String[] folders = message.getFolders();
      nodeMsg.setProperty("exo:folders", folders);
      List<Attachment> attachments = message.getAttachments();
      Iterator<Attachment> it = attachments.iterator();
      while (it.hasNext()) {
        BufferAttachment file = (BufferAttachment)it.next();
        Node nodeFile = null;
        if (!nodeMsg.hasNode(file.getName())) nodeFile = nodeMsg.addNode(file.getName(), "nt:file");
        else nodeFile = nodeMsg.getNode(file.getName());
        Node nodeContent = null;
        if (!nodeFile.hasNode("jcr:content")) nodeContent = nodeFile.addNode("jcr:content", "nt:resource");
        else nodeContent = nodeFile.getNode("jcr:content");
        nodeContent.setProperty("jcr:mimeType", file.getMimeType());
        nodeContent.setProperty("jcr:data", file.getInputStream());
        nodeContent.setProperty("jcr:lastModified", Calendar.getInstance().getTimeInMillis());
      }
      homeMsg.getSession().save();
    }
  }
  
  public Folder getFolder(String username, String accountId, String folderName) throws Exception {
    Folder folder = null;
    Node folderHome = getFolderHome(username, accountId);
    Node node = null;
    // if this folder exists, creates the object and returns it
    if (folderHome.hasNode(folderName)) {
      node = folderHome.getNode(folderName);
      folder = new Folder();
      folder.setLabel(node.getProperty("exo:label").getString());
      folder.setName(node.getProperty("exo:name").getString());
      folder.setNumberOfUnreadMessage((int)node.getProperty("exo:unreadMessages").getLong());
    }
    return folder ;
  }
  
  public void saveUserFolder(String username, String accountId, Folder folder) throws Exception {
    // gets folder home node of the specified account
    Node home = getFolderHome(username, accountId);
    Node myFolder = null;
    if (home.hasNode(folder.getName())) { // if the folder exists, gets it
      myFolder = home.getNode(folder.getName());
    } else { // if it doesn't exist, creates it
      myFolder = home.addNode(folder.getName(), "exo:folder");
    }
    // sets some properties
    myFolder.setProperty("exo:label", folder.getLabel());
    myFolder.setProperty("exo:unreadMessages", folder.getNumberOfUnreadMessage());
    myFolder.setProperty("exo:name", folder.getName());

    home.getSession().save();
  }
  
  public void removeUserFolder(String username, Folder folder) throws Exception {
    // gets the mail home node
    Session sess = getMailHomeNode(username).getSession();
    QueryManager qm = sess.getWorkspace().getQueryManager();
    // gets the specified folder node
    StringBuffer queryString = new StringBuffer("//element(*,exo:folder)[@exo:name='").
                                  append(folder.getName()).
                                  append("']");
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    NodeIterator it = result.getNodes();
    // removes the folder it it exists
    if (it.hasNext()) it.nextNode().remove();
    sess.save();
  }
  
  public void removeUserFolder(String username, Account account, Folder folder) throws Exception {
    //  gets the specified folder
    Node folderHome = getFolderHome(username, account.getId());
    if (folderHome.hasNode(folder.getName())) {
      folderHome.getNode(folder.getName()).remove();
    }
    folderHome.getSession().save();
  }
  
  public Node getMailHomeNode(String username) throws Exception {
    ServiceRegistry serviceRegistry = new ServiceRegistry("MailService") ;
    Session session = getJCRSession() ;
    if(jcrRegistryService_.getUserNode(session, username) == null)
      jcrRegistryService_.createUserHome(username, false) ;
    jcrRegistryService_.createServiceRegistry(username, serviceRegistry, false) ;    
    return jcrRegistryService_.getServiceRegistryNode(session, username, serviceRegistry.getName()) ;
  }
  
  public Node getMessageHome(String username, String accountId) throws Exception {
    Node home = getMailHomeNode(username);
    Account account = getAccountById(username, accountId);
    Node returnNode = null;
    if (home.getNode(account.getUserDisplayName()).hasNode("Messages"))
      returnNode = home.getNode(account.getUserDisplayName()).getNode("Messages");
    else
      returnNode = home.getNode(account.getUserDisplayName()).addNode("Messages", "nt:unstructured");
    return returnNode;
  }
  
  public Node getFolderHome(String username, String accountId) throws Exception {
    Node home = getMailHomeNode(username);
    Account account = getAccountById(username, accountId);
    Node returnNode = null;
    if (home.getNode(account.getUserDisplayName()).hasNode("Folders")) 
      returnNode = home.getNode(account.getUserDisplayName()).getNode("Folders");
    else
      returnNode = home.getNode(account.getUserDisplayName()).addNode("Folders", "nt:unstructured");
    return returnNode;
  }
  
  public Node getTagHome(String username, String accountId) throws Exception {
    Node home = getMailHomeNode(username);
    Account account = getAccountById(username, accountId);
    Node returnNode = null;
    if (home.getNode(account.getUserDisplayName()).hasNode("Tags")) 
      returnNode = home.getNode(account.getUserDisplayName()).getNode("Tags");
    else
      returnNode = home.getNode(account.getUserDisplayName()).addNode("Tags", "nt:unstructured");
    return returnNode;
  }
  
  private Session getJCRSession() throws Exception {
    String defaultWS = 
      repositoryService_.getDefaultRepository().getConfiguration().getDefaultWorkspaceName() ;
    return repositoryService_.getDefaultRepository().getSystemSession(defaultWS) ;
  }
}