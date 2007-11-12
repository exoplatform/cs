/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.mail.internet.InternetAddress;

import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.Attachment;
import org.exoplatform.mail.service.BufferAttachment;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.JCRMessageAttachment;
import org.exoplatform.mail.service.MailSetting;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.MessageFilter;
import org.exoplatform.mail.service.MessagePageList;
import org.exoplatform.mail.service.Tag;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.registry.JCRRegistryService;
import org.exoplatform.registry.ServiceRegistry;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

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
    /*QueryManager qm = getMailHomeNode(username).getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root" +currentAccount.getPath() + "//element(*,exo:message)[@exo:tags='").
                                  append(tagName).
                                  append("']");
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    NodeIterator it = result.getNodes();*/
    Node mailHome = getMailHomeNode(username) ;
    //  if an account is found, creates the object
    if(mailHome.hasNode(id)) {
      return getAccount(mailHome.getNode(id)) ;
    }
    return null ;
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
        accounts.add(getAccount(node));
      }
    }
    return accounts ;
  }

  public Account getAccount(Node accountNode) throws Exception{
    Account account = new Account();
    account.setId(accountNode.getProperty(Utils.EXO_ID).getString());
    if (accountNode.hasProperty(Utils.EXO_LABEL)) account.setLabel(accountNode.getProperty(Utils.EXO_LABEL).getString());
    if (accountNode.hasProperty(Utils.EXO_USERDISPLAYNAME)) account.setUserDisplayName(accountNode.getProperty(Utils.EXO_USERDISPLAYNAME).getString());
    if (accountNode.hasProperty(Utils.EXO_EMAILADDRESS)) account.setEmailAddress(accountNode.getProperty(Utils.EXO_EMAILADDRESS).getString());
    if (accountNode.hasProperty(Utils.EXO_REPLYEMAIL)) account.setEmailReplyAddress(accountNode.getProperty(Utils.EXO_REPLYEMAIL).getString());
    if (accountNode.hasProperty(Utils.EXO_SIGNATURE)) account.setSignature(accountNode.getProperty(Utils.EXO_SIGNATURE).getString());
    if (accountNode.hasProperty(Utils.EXO_DESCRIPTION)) account.setDescription(accountNode.getProperty(Utils.EXO_DESCRIPTION).getString());
    if (accountNode.hasProperty(Utils.EXO_CHECKMAILAUTO)) account.setCheckedAuto(accountNode.getProperty(Utils.EXO_CHECKMAILAUTO).getBoolean());
    if (accountNode.hasProperty(Utils.EXO_EMPTYTRASH)) account.setEmptyTrashWhenExit(accountNode.getProperty(Utils.EXO_EMPTYTRASH).getBoolean());
    if (accountNode.hasProperty(Utils.EXO_PLACESIGNATURE)) account.setPlaceSignature(accountNode.getProperty(Utils.EXO_PLACESIGNATURE).getString());
    if (accountNode.hasProperty(Utils.EXO_SERVERPROPERTIES)) {
      Value[] properties = accountNode.getProperty(Utils.EXO_SERVERPROPERTIES).getValues();
      for (int i=0; i<properties.length; i++) {
        String property = properties[i].getString();
        int index = property.indexOf('=');
        if (index != -1) account.setServerProperty(property.substring(0, index), property.substring(index+1));
      }
    }
    return account ;
  }
  public Message getMessageById(String username, String accountId, String msgId) throws Exception {
    //  gets a message (email) from its id and username
    Node messageHome = getMessageHome(username, accountId);
    //  if this message exists, creates the object and returns it
    if (messageHome.hasNode(msgId)) {
      Message msg = getMessage(messageHome.getNode(msgId));
      msg.setAccountId(accountId);
      return msg ;
    }
    return null ;
  }
  
  public MailSetting getMailSetting(String username) throws Exception {
    Node homeNode = getMailHomeNode(username);
    NodeIterator it = homeNode.getNodes();
    Node mailSettingNode = null;
    while (it.hasNext()) {
      Node node = it.nextNode();
      if (node.isNodeType(Utils.EXO_MAIL_SETTING)) {
        mailSettingNode = node;
      }
    }
    MailSetting mailSetting = new MailSetting();
    if (mailSettingNode !=null ){
      if (mailSettingNode.hasProperty(Utils.EXO_NUMBER_OF_CONVERSATION)) 
        mailSetting.setShowNumberMessage((mailSettingNode.getProperty(Utils.EXO_NUMBER_OF_CONVERSATION).getLong()));
      if (mailSettingNode.hasProperty(Utils.EXO_PERIOD_CHECKMAIL_AUTO)) 
        mailSetting.setPeriodCheckMailAuto((mailSettingNode.getProperty(Utils.EXO_PERIOD_CHECKMAIL_AUTO).getLong()));
      if (mailSettingNode.hasProperty(Utils.EXO_DEFAULT_ACCOUNT)) 
        mailSetting.setDefaultAccount((mailSettingNode.getProperty(Utils.EXO_DEFAULT_ACCOUNT).getString()));
      if (mailSettingNode.hasProperty(Utils.EXO_EDITOR)) 
        mailSetting.setTypeOfEditor((mailSettingNode.getProperty(Utils.EXO_EDITOR).getString()));
      if (mailSettingNode.hasProperty(Utils.EXO_FORMAT_WHEN_REPLYFORWARD)) 
        mailSetting.setFormatWhenReplyForward((mailSettingNode.getProperty(Utils.EXO_FORMAT_WHEN_REPLYFORWARD).getString()));
      if (mailSettingNode.hasProperty(Utils.EXO_REPLY_MESSAGE_WITH)) 
        mailSetting.setReplyMessageWith((mailSettingNode.getProperty(Utils.EXO_REPLY_MESSAGE_WITH).getString()));
      if (mailSettingNode.hasProperty(Utils.EXO_FORWARD_MESSAGE_WITH)) 
        mailSetting.setForwardMessageWith((mailSettingNode.getProperty(Utils.EXO_FORWARD_MESSAGE_WITH).getString()));
      if (mailSettingNode.hasProperty(Utils.EXO_PREFIX_MESSAGE_WITH)) 
        mailSetting.setPrefixMessageWith((mailSettingNode.getProperty(Utils.EXO_PREFIX_MESSAGE_WITH).getString()));
      if (mailSettingNode.hasProperty(Utils.EXO_SAVE_SENT_MESSAGE)) 
        mailSetting.setSaveMessageInSent((mailSettingNode.getProperty(Utils.EXO_SAVE_SENT_MESSAGE).getBoolean()));
    }
    return mailSetting; 
  }

  public MessagePageList getMessages(String username, MessageFilter filter) throws Exception {
    Node homeMsg = getMessageHome(username, filter.getAccountId());
    filter.setAccountPath(homeMsg.getPath()) ;
    QueryManager qm = homeMsg.getSession().getWorkspace().getQueryManager();
    System.out.println("\n QueryString >>" + filter.getStatement() + "\n") ;
    Query query = qm.createQuery(filter.getStatement(), Query.XPATH);
    QueryResult result = query.execute();    
    MessagePageList pageList = new MessagePageList(result.getNodes(), getMailSetting(username).getShowNumberMessage(), filter.getStatement(), true) ;
    return pageList ;
  }

  public Message getMessage(Node messageNode) throws Exception {
    Message msg = new Message();
    if (messageNode.hasProperty(Utils.EXO_ID)) msg.setId(messageNode.getProperty(Utils.EXO_ID).getString());
    if (messageNode.hasProperty(Utils.EXO_ACCOUNT)) msg.setAccountId(messageNode.getProperty(Utils.EXO_ACCOUNT).getString()) ;
    if (messageNode.hasProperty(Utils.EXO_FROM)) msg.setFrom(messageNode.getProperty(Utils.EXO_FROM).getString());
    if (messageNode.hasProperty(Utils.EXO_TO)) msg.setMessageTo(messageNode.getProperty(Utils.EXO_TO).getString());
    if (messageNode.hasProperty(Utils.EXO_SUBJECT)) msg.setSubject(messageNode.getProperty(Utils.EXO_SUBJECT).getString());
    if (messageNode.hasProperty(Utils.EXO_CC)) msg.setMessageCc(messageNode.getProperty(Utils.EXO_CC).getString());
    if (messageNode.hasProperty(Utils.EXO_BCC)) msg.setMessageBcc(messageNode.getProperty(Utils.EXO_BCC).getString());
    if (messageNode.hasProperty(Utils.EXO_REPLYTO)) msg.setReplyTo(messageNode.getProperty(Utils.EXO_REPLYTO).getString());
    if (messageNode.hasProperty(Utils.EXO_CONTENT_TYPE)) msg.setContentType(messageNode.getProperty(Utils.EXO_CONTENT_TYPE).getString());
    if (messageNode.hasProperty(Utils.EXO_BODY)) msg.setMessageBody(messageNode.getProperty(Utils.EXO_BODY).getString());
    if (messageNode.hasProperty(Utils.EXO_SIZE)) msg.setSize(messageNode.getProperty(Utils.EXO_SIZE).getLong());
    if (messageNode.hasProperty(Utils.EXO_STAR)) msg.setHasStar(messageNode.getProperty(Utils.EXO_STAR).getBoolean());
    if (messageNode.hasProperty(Utils.EXO_PRIORITY)) msg.setPriority(messageNode.getProperty(Utils.EXO_PRIORITY).getLong());
    if (messageNode.hasProperty(Utils.EXO_ISUNREAD)) msg.setUnread(messageNode.getProperty(Utils.EXO_ISUNREAD).getBoolean());
    
    if (messageNode.hasProperty(Utils.EXO_TAGS)) {
      Value[] propTags = messageNode.getProperty(Utils.EXO_TAGS).getValues();
      String[] tags = new String[propTags.length];
      for (int i = 0; i < propTags.length; i++) {
        tags[i] = propTags[i].getString();
      }
      msg.setTags(tags);
    }
    if (messageNode.hasProperty(Utils.EXO_FOLDERS)) {
      Value[] propFolders = messageNode.getProperty(Utils.EXO_FOLDERS).getValues();
      String[] folders = new String[propFolders.length];
      for (int i = 0; i < propFolders.length; i++) {
        folders[i] = propFolders[i].getString();
      }
      msg.setFolders(folders);
    }
    NodeIterator msgAttachmentIt = messageNode.getNodes();
    List<Attachment> attachments = new ArrayList<Attachment>();
    while (msgAttachmentIt.hasNext()) {
      Node node = msgAttachmentIt.nextNode();
      if (node.isNodeType(Utils.NT_FILE)) {
        JCRMessageAttachment file = new JCRMessageAttachment();
        file.setId(node.getPath());
        file.setMimeType(node.getNode(Utils.JCR_CONTENT).getProperty(Utils.JCR_MIMETYPE).getString());
        file.setName(node.getName());
        file.setWorkspace(node.getSession().getWorkspace().getName()) ;
        //file.setInputStream(node.getNode(Utils.JCR_CONTENT).getProperty(Utils.JCR_DATA).getStream());
        attachments.add(file);
      }
    }
    msg.setAttachements(attachments);
    
    GregorianCalendar cal = new GregorianCalendar();
    if (messageNode.hasProperty(Utils.EXO_RECEIVEDDATE)) {
      cal.setTimeInMillis(messageNode.getProperty(Utils.EXO_RECEIVEDDATE).getLong());
      msg.setReceivedDate(cal.getTime());
    }

    if (messageNode.hasProperty(Utils.EXO_SENDDATE)) {
      cal.setTimeInMillis(messageNode.getProperty(Utils.EXO_SENDDATE).getLong());
      msg.setSendDate(cal.getTime());
    }
    return msg ;
  }

  public void removeAccount(String username, Account account) throws Exception {
    Node accountHome = getMailHomeNode(username) ;
    // gets the specified account, and removes it
    accountHome.getNode(account.getId()).remove();    
    accountHome.getSession().save() ;
  }

  public void removeMessage(String username, String accountId, String messageId) throws Exception {
    Node messages = getMessageHome(username, accountId);
    //  removes it
    if (messages.hasNode(messageId)) messages.getNode(messageId).remove();
    messages.getSession().save();
  }

  public void removeMessage(String username, String accountId, List<String> messageIds) throws Exception {
    //  loops on the message names array, and removes each message
    for (String messageId : messageIds) {
      removeMessage(username, accountId, messageId);
    }
  }

  public void saveAccount(String username, Account account, boolean isNew) throws Exception {
    // creates or updates an account, depending on the isNew flag
    Node mailHome = getMailHomeNode(username) ;
    Node newAccount = null;
    String accId = account.getId() ;
    if (isNew) { // creates the node
      newAccount = mailHome.addNode(accId, Utils.EXO_ACCOUNT);
      newAccount.setProperty(Utils.EXO_ID, accId);
    } else { // gets the specified account
      newAccount = mailHome.getNode(accId);
    }
    if (newAccount != null) {
      // add some properties
      newAccount.setProperty(Utils.EXO_LABEL, account.getLabel());
      newAccount.setProperty(Utils.EXO_USERDISPLAYNAME, account.getUserDisplayName());
      newAccount.setProperty(Utils.EXO_EMAILADDRESS, account.getEmailAddress());
      newAccount.setProperty(Utils.EXO_REPLYEMAIL, account.getEmailReplyAddress());
      newAccount.setProperty(Utils.EXO_SIGNATURE, account.getSignature());
      newAccount.setProperty(Utils.EXO_DESCRIPTION, account.getDescription());
      newAccount.setProperty(Utils.EXO_CHECKMAILAUTO, account.checkedAuto());
      newAccount.setProperty(Utils.EXO_EMPTYTRASH, account.isEmptyTrashWhenExit());
      newAccount.setProperty(Utils.EXO_PLACESIGNATURE, account.getPlaceSignature());
      Iterator it = account.getServerProperties().keySet().iterator();
      ArrayList<String> values = new ArrayList<String>(account.getServerProperties().size());
      while (it.hasNext()) {
        String key = it.next().toString();
        values.add(key+"="+account.getServerProperties().get(key));
      }
      newAccount.setProperty(Utils.EXO_SERVERPROPERTIES, values.toArray(new String[account.getServerProperties().size()]));
      // saves changes
      mailHome.getSession().save();
    }
  }
  
  public void saveMailSetting(String username, MailSetting newSetting) throws Exception {
    Node mailHome = getMailHomeNode(username) ;
    Node mailSetting = null;
    if (mailHome.hasNode(Utils.EXO_MAIL_SETTING)) {
      mailSetting = mailHome.getNode(Utils.EXO_MAIL_SETTING);
    } else {
      mailSetting = mailHome.addNode(Utils.EXO_MAIL_SETTING, Utils.EXO_MAIL_SETTING);
    }
    
    mailSetting.setProperty(Utils.EXO_NUMBER_OF_CONVERSATION, newSetting.getShowNumberMessage());
    mailSetting.setProperty(Utils.EXO_PERIOD_CHECKMAIL_AUTO, newSetting.getPeriodCheckMailAuto());
    mailSetting.setProperty(Utils.EXO_DEFAULT_ACCOUNT, newSetting.getDefaultAccount());
    mailSetting.setProperty(Utils.EXO_FORMAT_WHEN_REPLYFORWARD, newSetting.getFormatWhenReplyForward());
    mailSetting.setProperty(Utils.EXO_EDITOR, newSetting.getTypeOfEditor());
    mailSetting.setProperty(Utils.EXO_REPLY_MESSAGE_WITH, newSetting.getReplyMessageWith());
    mailSetting.setProperty(Utils.EXO_FORWARD_MESSAGE_WITH, newSetting.getForwardMessageWith());
    mailSetting.setProperty(Utils.EXO_PREFIX_MESSAGE_WITH, newSetting.getPrefixMessageWith());
    mailSetting.setProperty(Utils.EXO_SAVE_SENT_MESSAGE, newSetting.saveMessageInSent());
    
    mailHome.getSession().save();
  }

  public void saveMessage(String username, String accountId, Message message, boolean isNew) throws Exception {
    Node homeMsg = getMessageHome(username, accountId);
    Node nodeMsg = null;
    if (isNew) { // creates the node
      nodeMsg = homeMsg.addNode(message.getId(), Utils.EXO_MESSAGE);
    } else { // gets the specified message
      nodeMsg = homeMsg.getNode(message.getId());
    }
    if (nodeMsg != null) {
      // add some properties
      nodeMsg.setProperty(Utils.EXO_ID, message.getId());
      nodeMsg.setProperty(Utils.EXO_ACCOUNT, accountId);
      nodeMsg.setProperty(Utils.EXO_FROM, message.getFrom());
      nodeMsg.setProperty(Utils.EXO_TO, message.getMessageTo());
      nodeMsg.setProperty(Utils.EXO_SUBJECT, message.getSubject());
      nodeMsg.setProperty(Utils.EXO_CC, message.getMessageCc());
      nodeMsg.setProperty(Utils.EXO_BCC, message.getMessageBcc());
      nodeMsg.setProperty(Utils.EXO_BODY, message.getMessageBody());
      nodeMsg.setProperty(Utils.EXO_REPLYTO, message.getReplyTo());
      nodeMsg.setProperty(Utils.EXO_SIZE, message.getSize());
      nodeMsg.setProperty(Utils.EXO_STAR, message.hasStar());
      nodeMsg.setProperty(Utils.EXO_PRIORITY, message.getPriority());
      nodeMsg.setProperty(Utils.EXO_ISUNREAD, message.isUnread());
      nodeMsg.setProperty(Utils.EXO_CONTENT_TYPE, message.getContentType());
      nodeMsg.setProperty(Utils.EXO_HASATTACH, false);
      if (message.getSendDate() != null)
        nodeMsg.setProperty(Utils.EXO_SENDDATE, message.getSendDate().getTime());
      if (message.getReceivedDate() != null)
        nodeMsg.setProperty(Utils.EXO_RECEIVEDDATE, message.getReceivedDate().getTime());
      String[] tags = message.getTags();
      nodeMsg.setProperty(Utils.EXO_TAGS, tags);
      String[] folders = message.getFolders();
      nodeMsg.setProperty(Utils.EXO_FOLDERS, folders);
      if (isNew) {
        List<Attachment> attachments = message.getAttachments();
        if(attachments != null) { 
          Iterator<Attachment> it = attachments.iterator();
          while (it.hasNext()) {
            BufferAttachment file = (BufferAttachment)it.next();
            Node nodeFile = null;
            if (!nodeMsg.hasNode(file.getName())) nodeFile = nodeMsg.addNode(file.getName(), Utils.NT_FILE);
            else nodeFile = nodeMsg.getNode(file.getName());
            Node nodeContent = null;
            if (!nodeFile.hasNode(Utils.JCR_CONTENT)) nodeContent = nodeFile.addNode(Utils.JCR_CONTENT, Utils.NT_RESOURCE);
            else nodeContent = nodeFile.getNode(Utils.JCR_CONTENT);
            nodeContent.setProperty(Utils.JCR_MIMETYPE, file.getMimeType());
            nodeContent.setProperty(Utils.JCR_DATA, file.getInputStream());
            nodeContent.setProperty(Utils.JCR_LASTMODIFIED, Calendar.getInstance().getTimeInMillis());
            nodeMsg.setProperty(Utils.EXO_HASATTACH, true);
          }
        }
      }
      homeMsg.getSession().save();
    }
  }
  
  public String setAddress(String strAddress) throws Exception {
    String str = "";
    try {
      if (strAddress != null && strAddress.trim() != ""){
        InternetAddress[] internetAddress = InternetAddress.parse(strAddress);
        int i = 0;
        if(internetAddress != null && internetAddress.length > 0) {
          while (i < internetAddress.length) {
            String personal = internetAddress[i].getPersonal();
            String address = internetAddress[i].getAddress();
            String sender = address + ";" + address;
            if (personal != null && personal != "") 
              sender = personal + " ;" + address ;
            if(str.length() < 1)  {
              str = sender ;              
            }else {
              str += "," + sender ;
            }           
            i++ ;
          }
        }
      }
    } catch(Exception e) {
      str = strAddress;
    }
    return str ;
  }

  public Folder getFolder(String username, String accountId, String folderId) throws Exception {
    Folder folder = null;
    Node folderHome = getFolderHome(username, accountId);
    Node node = null;
    // if this folder exists, creates the object and returns it
    if (folderHome.hasNode(folderId)) {
      node = folderHome.getNode(folderId);
      folder = new Folder();
      folder.setId(node.getProperty(Utils.EXO_ID).getString());
      folder.setLabel(node.getProperty(Utils.EXO_LABEL).getString());
      folder.setName(node.getProperty(Utils.EXO_NAME).getString());
      folder.setPersonalFolder(node.getProperty(Utils.EXO_PERSONAL).getBoolean()) ;
      folder.setNumberOfUnreadMessage(node.getProperty(Utils.EXO_UNREADMESSAGES).getLong());
      folder.setTotalMessage(node.getProperty(Utils.EXO_TOTALMESSAGE).getLong());
    }
    return folder ;
  }

  public List<Folder> getFolders(String username, String accountId) throws Exception {
    List<Folder> folders = new ArrayList<Folder>() ;
    Node folderHomeNode = getFolderHome(username, accountId) ;
    NodeIterator iter = folderHomeNode.getNodes() ;
    while (iter.hasNext()){
      Node folder = (Node)iter.next() ;
      folders.add(getFolder(username, accountId, folder.getName())) ;
    }
    return folders ;
  }

  public void saveUserFolder(String username, String accountId, Folder folder) throws Exception {
    // gets folder home node of the specified account
    Node home = getFolderHome(username, accountId);
    Node myFolder = null;
    if (home.hasNode(folder.getId())) { // if the folder exists, gets it
      myFolder = home.getNode(folder.getId());
    } else { // if it doesn't exist, creates it
      myFolder = home.addNode(folder.getId(), Utils.EXO_FOLDER);
    }
    // sets some properties
    myFolder.setProperty(Utils.EXO_ID, folder.getId());
    myFolder.setProperty(Utils.EXO_NAME, folder.getName());
    myFolder.setProperty(Utils.EXO_LABEL, folder.getLabel());
    myFolder.setProperty(Utils.EXO_UNREADMESSAGES, folder.getNumberOfUnreadMessage());
    myFolder.setProperty(Utils.EXO_TOTALMESSAGE, folder.getTotalMessage());
    myFolder.setProperty(Utils.EXO_PERSONAL, folder.isPersonalFolder()) ;
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
    if (folderHome.hasNode(folder.getId())) {
      folderHome.getNode(folder.getId()).remove();
    }
    folderHome.getSession().save();
  }
  
  private Node getMailHomeNode(String username) throws Exception {
    ServiceRegistry serviceRegistry = new ServiceRegistry("MailService") ;
    Session session = getJCRSession() ;
    if(jcrRegistryService_.getUserNode(session, username) == null)
      jcrRegistryService_.createUserHome(username, false) ;
    jcrRegistryService_.createServiceRegistry(username, serviceRegistry, false) ;    
    return jcrRegistryService_.getServiceRegistryNode(session, username, serviceRegistry.getName()) ;
  }

  public Node getMessageHome(String username, String accountId) throws Exception {
    Node accountHome = getMailHomeNode(username).getNode(accountId);
    if(accountHome.hasNode(Utils.KEY_MESSAGE)) return accountHome.getNode(Utils.KEY_MESSAGE) ;
    else return accountHome.addNode(Utils.KEY_MESSAGE, Utils.NT_UNSTRUCTURED) ;
  }

  private Node getFolderHome(String username, String accountId) throws Exception {
    Node home = getMailHomeNode(username);
    Account account = getAccountById(username, accountId);
    Node returnNode = null;
    if (home.getNode(account.getId()).hasNode(Utils.KEY_FOLDERS)) 
      returnNode = home.getNode(account.getId()).getNode(Utils.KEY_FOLDERS);
    else
      returnNode = home.getNode(account.getId()).addNode(Utils.KEY_FOLDERS, Utils.NT_UNSTRUCTURED);
    return returnNode;
  }

  private Node getTagHome(String username, String accountId) throws Exception {
    Node accountNode = getMailHomeNode(username).getNode(accountId);
    if(accountNode.hasNode(Utils.KEY_TAGS)) return accountNode.getNode(Utils.KEY_TAGS) ;
    else return accountNode.addNode(Utils.KEY_TAGS, Utils.NT_UNSTRUCTURED) ;    
  }

  public Session getJCRSession() throws Exception {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider() ;
    String defaultWS = 
      repositoryService_.getDefaultRepository().getConfiguration().getDefaultWorkspaceName() ;
    return sessionProvider.getSession(defaultWS, repositoryService_.getCurrentRepository()) ;
  }


  public void addTag(String username, String accountId, List<String> messageIds, List<Tag> tagList)
  throws Exception {    
    Map<String, String> tagMap = new HashMap<String, String> () ;
    Node tagHome = getTagHome(username, accountId) ;
    for(Tag tag : tagList) {
      if(!tagHome.hasNode(tag.getId())) {
        Node tagNode = tagHome.addNode(tag.getId(), Utils.EXO_MAILTAG) ;
        tagNode.setProperty(Utils.EXO_ID, tag.getId()) ;
        tagNode.setProperty(Utils.EXO_NAME, tag.getName()) ;
        tagNode.setProperty(Utils.EXO_DESCRIPTION, tag.getDescription()) ;
        tagNode.setProperty(Utils.EXO_COLOR, tag.getColor()) ;
      }      
      tagMap.put(tag.getId(), tag.getId()) ;
    }
    tagHome.getSession().save() ;
    
    Node messageHome = getMessageHome(username, accountId) ;
    for(String messageId : messageIds) {
      Map<String, String> messageTagMap = new HashMap<String, String> () ;
      if(messageHome.hasNode(messageId)) {
        Node messageNode = messageHome.getNode(messageId) ;
        if(messageNode.hasProperty(Utils.EXO_TAGS)) {
          Value[] values = messageNode.getProperty(Utils.EXO_TAGS).getValues() ;
          for(Value value : values) {
            messageTagMap.put(value.getString(), value.getString()) ;
          }
        }
        messageTagMap.putAll(tagMap) ;
        messageNode.setProperty(Utils.EXO_TAGS, messageTagMap.values().toArray(new String[]{})) ;
        messageNode.save() ;
      }
    }
    messageHome.getSession().save() ;
  }


  public List<Tag> getTags(String username, String accountId) throws Exception {
    List<Tag> tags = new ArrayList<Tag>() ;
    Node tagHomeNode = getTagHome(username, accountId) ;
    NodeIterator iter = tagHomeNode.getNodes() ;
    while (iter.hasNext()){
      Node tagNode = (Node)iter.next() ;
      Tag tag = new Tag();
      tag.setId((tagNode.getProperty(Utils.EXO_ID).getString())) ;
      tag.setName(tagNode.getProperty(Utils.EXO_NAME).getString()) ;
      tag.setDescription(tagNode.getProperty(Utils.EXO_DESCRIPTION).getString()) ;
      tag.setColor(tagNode.getProperty(Utils.EXO_COLOR).getString()) ;
      tags.add(tag);
    }
    return tags ;
  }
  
  public Tag getTag(String username, String accountId, String tagId) throws Exception {
    Node tagHomeNode = getTagHome(username, accountId) ;
    Tag tag = new Tag();
    NodeIterator iter = tagHomeNode.getNodes() ;
    while (iter.hasNext()){
      Node tagNode = (Node)iter.next() ;
      if (tagNode.getProperty(Utils.EXO_ID).getString().equals(tagId)) {
        tag.setId((tagNode.getProperty(Utils.EXO_ID).getString())) ;
        tag.setName(tagNode.getProperty(Utils.EXO_NAME).getString()) ;
        tag.setDescription(tagNode.getProperty(Utils.EXO_DESCRIPTION).getString()) ;
        tag.setColor(tagNode.getProperty(Utils.EXO_COLOR).getString()) ;
      }
    }
    return tag ;
  }

  public void removeMessageTag(String username, String accountId, List<String> messageIds, List<String> tagIds) 
  throws Exception {
    Node messageHome = getMessageHome(username, accountId);
    for (String messageId : messageIds) {
      if (messageHome.hasNode(messageId)) {
        Node messageNode = messageHome.getNode(messageId);
        if (messageNode.hasProperty(Utils.EXO_TAGS)) {
          Message message = getMessage(messageNode);
          String[] tags = message.getTags();
          List<String> listTags = new ArrayList<String>(Arrays.asList(tags)); 
          for (String tagId : tagIds) listTags.remove(tagId);
          tags = listTags.toArray(new String[listTags.size()]);
          message.setTags(tags);
          saveMessage(username, message.getAccountId(), message, false);
        }
      }
    }
    messageHome.getSession().save();
  }


  public void removeTag(String username, String accountId, String tagId) throws Exception {
    // remove this tag in all messages
    List<Message> listMessage = getMessageByTag(username, accountId, tagId);
    List<String> listTag = new ArrayList<String>();
    List<String> listMessageId = new ArrayList<String>();
    for (Message mess : listMessage) {
      listMessageId.add(mess.getId());
    }
    listTag.add(tagId);
    removeMessageTag(username, accountId, listMessageId, listTag);

    // remove tag node
    Node tagHomeNode = getTagHome(username, accountId) ;
    if (tagHomeNode.hasNode(tagId)) {
      tagHomeNode.getNode(tagId).remove() ;
    }

    tagHomeNode.getSession().save() ;
  } 
  
  public void updateTag(String username, String accountId, Tag tag) throws Exception {
    Node tagHome = getTagHome(username, accountId) ;
    if (tagHome.hasNode(tag.getId())) {
      Node tagNode = tagHome.getNode(tag.getId());
      tagNode.setProperty(Utils.EXO_NAME, tag.getName());
      tagNode.setProperty(Utils.EXO_DESCRIPTION, tag.getDescription());
      tagNode.setProperty(Utils.EXO_COLOR, tag.getColor());
    }
    tagHome.save();
  }

  public List<Message> getMessageByTag(String username, String accountId, String tagId)
  throws Exception {
    List<Message> messages = new ArrayList<Message>();
    QueryManager qm = getMailHomeNode(username).getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root" + getMailHomeNode(username).getNode(accountId).getPath() + "//element(*,exo:message)[@exo:tags='").
    append(tagId).
    append("']");
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    NodeIterator it = result.getNodes();
    while(it.hasNext()) {
      Message message = getMessage(it.nextNode());
      messages.add(message);
    }
    return messages;
  }
}