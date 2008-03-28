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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyIterator;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.mail.Header;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.Attachment;
import org.exoplatform.mail.service.BufferAttachment;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.JCRMessageAttachment;
import org.exoplatform.mail.service.MailSetting;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.MessageFilter;
import org.exoplatform.mail.service.MessagePageList;
import org.exoplatform.mail.service.MimeMessageParser;
import org.exoplatform.mail.service.SpamFilter;
import org.exoplatform.mail.service.Tag;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan.nguyen@exoplatform.com
 * Jun 23, 2007  
 */
public class JCRDataStorage{
	private NodeHierarchyCreator nodeHierarchyCreator_ ;
	private static final String MAIL_SERVICE = "MailApplication" ; 
  public JCRDataStorage(NodeHierarchyCreator nodeHierarchyCreator) {
  	nodeHierarchyCreator_ = nodeHierarchyCreator ;
  }

  private Node getMailHomeNode(SessionProvider sProvider, String username) throws Exception {
    Node userApp = nodeHierarchyCreator_.getUserApplicationNode(sProvider, username) ;
    Node mailNode = null;
    try {
      mailNode = userApp.getNode(MAIL_SERVICE) ;
    } catch(PathNotFoundException e) {
      mailNode = userApp.addNode(MAIL_SERVICE, Utils.NT_UNSTRUCTURED) ;
      userApp.save();
    }
    return mailNode ;
  }
  
  public Account getAccountById(SessionProvider sProvider, String username, String id) throws Exception {
    Node mailHome = getMailHomeNode(sProvider, username) ;
    if(mailHome.hasNode(id)) {
      return getAccount(mailHome.getNode(id)) ;
    }
    return null ;
  }
  
  public List<Account> getAccounts(SessionProvider sProvider, String username) throws Exception {
    List<Account> accounts = new ArrayList<Account>();
    Node homeNode = getMailHomeNode(sProvider, username);
    NodeIterator it = homeNode.getNodes();
    while (it.hasNext()) {
      Node node = it.nextNode();
      if (node.isNodeType("exo:account")) accounts.add(getAccount(node));
    }
    return accounts ;
  }

  public Account getAccount(Node accountNode) throws Exception{
    Account account = new Account();
    account.setId(accountNode.getProperty(Utils.EXO_ID).getString());
    try {
      account.setLabel(accountNode.getProperty(Utils.EXO_LABEL).getString());
    } catch(Exception e) { }
    try {
      account.setUserDisplayName(accountNode.getProperty(Utils.EXO_USERDISPLAYNAME).getString());
    } catch(Exception e) { }
    try {
      account.setEmailAddress(accountNode.getProperty(Utils.EXO_EMAILADDRESS).getString());
    } catch(Exception e) { }
    try {
      account.setEmailReplyAddress(accountNode.getProperty(Utils.EXO_REPLYEMAIL).getString());
    } catch(Exception e) { }
    try {
      account.setSignature(accountNode.getProperty(Utils.EXO_SIGNATURE).getString());
    } catch(Exception e) { }
    try {
      account.setDescription(accountNode.getProperty(Utils.EXO_DESCRIPTION).getString());
    } catch(Exception e) { }
    try {
      account.setCheckedAuto(accountNode.getProperty(Utils.EXO_CHECKMAILAUTO).getBoolean());
    } catch(Exception e) { }
    try {
      account.setEmptyTrashWhenExit(accountNode.getProperty(Utils.EXO_EMPTYTRASH).getBoolean());
    } catch(Exception e) { }
    try {
      account.setPlaceSignature(accountNode.getProperty(Utils.EXO_PLACESIGNATURE).getString());
    } catch(Exception e) { }
    try {
      GregorianCalendar cal = new GregorianCalendar();
      cal.setTimeInMillis(accountNode.getProperty(Utils.EXO_LAST_CHECKED_TIME).getLong());
      account.setLastCheckedDate(cal.getTime());
    } catch(Exception e) { }
    try {
      Value[] properties = accountNode.getProperty(Utils.EXO_SERVERPROPERTIES).getValues();
      for (int i=0; i<properties.length; i++) {
        String property = properties[i].getString();
        int index = property.indexOf('=');
        if (index != -1) account.setServerProperty(property.substring(0, index), property.substring(index+1));
      }
    } catch(Exception e) { }
    
    try {
      Value[] properties = accountNode.getProperty(Utils.EXO_POPSERVERPROPERTIES).getValues();
      for (int i=0; i<properties.length; i++) {
        String property = properties[i].getString();
        int index = property.indexOf('=');
        if (index != -1) account.setPopServerProperty(property.substring(0, index), property.substring(index+1));
      }
    } catch(Exception e) { }
    
    try {
      Value[] properties = accountNode.getProperty(Utils.EXO_IMAPSERVERPROPERTIES).getValues();
      for (int i=0; i<properties.length; i++) {
        String property = properties[i].getString();
        int index = property.indexOf('=');
        if (index != -1) account.setImapServerProperty(property.substring(0, index), property.substring(index+1));
      }
    } catch(Exception e) { }
    
    return account ;
  }
  public Message getMessageById(SessionProvider sProvider, String username, String accountId, String msgId) throws Exception {
    Session sess = getMailHomeNode(sProvider, username).getSession();
    QueryManager qm = sess.getWorkspace().getQueryManager();
    // gets the specified folder node
    StringBuffer queryString = new StringBuffer("//element(*,exo:message)[@exo:id='").
    append(msgId).
    append("']");
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    NodeIterator it = result.getNodes();
    Node node = null ; 
    if (it.hasNext()) node = it.nextNode();
    Message msg = getMessage(node);
    return msg ;
  }
  
  public MailSetting getMailSetting(SessionProvider sProvider, String username) throws Exception {
    Node homeNode = getMailHomeNode(sProvider, username);
    Node settingNode = null;
    if (homeNode.hasNode(Utils.KEY_MAIL_SETTING)) settingNode = homeNode.getNode(Utils.KEY_MAIL_SETTING) ;
    MailSetting setting = new MailSetting();
    if (settingNode != null ){
      try {
        setting.setNumberMsgPerPage((settingNode.getProperty(Utils.EXO_NUMBER_MSG_PER_PAGE).getLong()));
      } catch(Exception e) { }
      try {
        setting.setPeriodCheckAuto((settingNode.getProperty(Utils.EXO_PERIOD_CHECKMAIL_AUTO).getLong()));
      } catch(Exception e) { }
      try {
        setting.setDefaultAccount((settingNode.getProperty(Utils.EXO_DEFAULT_ACCOUNT).getString()));
      } catch(Exception e) {}
      try {
        setting.setUseWysiwyg(settingNode.getProperty(Utils.EXO_USE_WYSIWYG).getBoolean());
      } catch(Exception e) { }
      try {
        setting.setFormatAsOriginal((settingNode.getProperty(Utils.EXO_FORMAT_AS_ORIGINAL).getBoolean()));
      } catch(Exception e) { }
      try {
        setting.setReplyWithAttach(settingNode.getProperty(Utils.EXO_REPLY_WITH_ATTACH).getBoolean());
      } catch(Exception e) { }
      try {
        setting.setForwardWithAtt(settingNode.getProperty(Utils.EXO_FORWARD_WITH_ATTACH).getBoolean());
      } catch(Exception e) { }
      try { 
        setting.setPrefixMessageWith((settingNode.getProperty(Utils.EXO_PREFIX_MESSAGE_WITH).getString()));
      } catch(Exception e) { }
      try { 
        setting.setSaveMessageInSent((settingNode.getProperty(Utils.EXO_SAVE_SENT_MESSAGE).getBoolean()));
      } catch (Exception e) { }
    }
    return setting; 
  }

  public MessagePageList getMessagePageList(SessionProvider sProvider, String username, MessageFilter filter) throws Exception {
    Node homeMsg = getMessageHome(sProvider, username, filter.getAccountId());
    filter.setAccountPath(homeMsg.getPath()) ;
    QueryManager qm = homeMsg.getSession().getWorkspace().getQueryManager();
    String queryString = filter.getStatement();
    long pageSize = getMailSetting(sProvider, username).getNumberMsgPerPage();
    Query query = qm.createQuery(queryString, Query.XPATH);
    QueryResult result = query.execute();    
    MessagePageList pageList = new MessagePageList(result.getNodes(), pageSize, queryString, true) ;
    return pageList ;
  }
  
  public List<Message> getMessages(SessionProvider sProvider, String username, MessageFilter filter) throws Exception {
    Node homeMsg = getMessageHome(sProvider, username, filter.getAccountId());
    filter.setAccountPath(homeMsg.getPath()) ;
    QueryManager qm = homeMsg.getSession().getWorkspace().getQueryManager();
    String queryString = filter.getStatement();
    Query query = qm.createQuery(queryString, Query.XPATH);
    QueryResult result = query.execute();  
    NodeIterator iter = result.getNodes();
    List<Message> strList = new ArrayList<Message>();
    while (iter.hasNext()) {
      Node node = iter.nextNode();
      strList.add(getMessage(node));
    }
    return strList ;
  }

  public Message getMessage(Node messageNode) throws Exception {
    Message msg = new Message();
    try {
      msg.setId(messageNode.getProperty(Utils.EXO_ID).getString());
    } catch(Exception e) { }
    msg.setPath(messageNode.getPath());
    try {
      msg.setAccountId(messageNode.getProperty(Utils.EXO_ACCOUNT).getString()) ;
    } catch(Exception e) { }
    try {
      msg.setFrom(messageNode.getProperty(Utils.EXO_FROM).getString());
    } catch(Exception e) { }
    try {
      msg.setMessageTo(messageNode.getProperty(Utils.EXO_TO).getString());
    } catch(Exception e) { }
    try {
      msg.setSubject(messageNode.getProperty(Utils.EXO_SUBJECT).getString());
    } catch(Exception e) { }
    try {
      msg.setMessageCc(messageNode.getProperty(Utils.EXO_CC).getString());
    } catch(Exception e) { }
    try {
      msg.setMessageBcc(messageNode.getProperty(Utils.EXO_BCC).getString());
    } catch(Exception e) { }
    try {
      msg.setReplyTo(messageNode.getProperty(Utils.EXO_REPLYTO).getString());
    } catch(Exception e) { }
    try {
      msg.setContentType(messageNode.getProperty(Utils.EXO_CONTENT_TYPE).getString());
    } catch(Exception e) { }
    try {
      msg.setMessageBody(messageNode.getProperty(Utils.EXO_BODY).getString());
    } catch(Exception e) { }
    try {
      msg.setSize(messageNode.getProperty(Utils.EXO_SIZE).getLong());
    } catch(Exception e) { }
    try {
      msg.setHasStar(messageNode.getProperty(Utils.EXO_STAR).getBoolean());
    } catch(Exception e) { }
    try {
      msg.setPriority(messageNode.getProperty(Utils.EXO_PRIORITY).getLong());
    } catch(Exception e) { }
    try {
      msg.setUnread(messageNode.getProperty(Utils.EXO_ISUNREAD).getBoolean());
    } catch(Exception e) { }
    
    try {
      Value[] propTags = messageNode.getProperty(Utils.EXO_TAGS).getValues();
      String[] tags = new String[propTags.length];
      for (int i = 0; i < propTags.length; i++) {
        tags[i] = propTags[i].getString();
      }
      msg.setTags(tags);
    } catch(Exception e) { }
    try {
      Value[] propFolders = messageNode.getProperty(Utils.EXO_FOLDERS).getValues();
      String[] folders = new String[propFolders.length];
      for (int i = 0; i < propFolders.length; i++) {
        folders[i] = propFolders[i].getString();
      }
      msg.setFolders(folders);
    } catch(Exception e) { }
    
    try {
      Value[] properties = messageNode.getProperty(Utils.EXO_HEADERS).getValues();
      for (int i=0; i<properties.length; i++) {
        String property = properties[i].getString();
        int index = property.indexOf('=');
        if (index != -1) msg.setHeader(property.substring(0, index), property.substring(index+1));
      }
    } catch(Exception e) { }
    
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
        file.setSize(node.getNode(Utils.JCR_CONTENT).getProperty(Utils.JCR_DATA).getLength());
        //file.setInputStream(node.getNode(Utils.JCR_CONTENT).getProperty(Utils.JCR_DATA).getStream());
        attachments.add(file);
      }
    }
    msg.setAttachements(attachments);
    
    GregorianCalendar cal = new GregorianCalendar();
    try {
      cal.setTimeInMillis(messageNode.getProperty(Utils.EXO_RECEIVEDDATE).getLong());
      msg.setReceivedDate(cal.getTime());
    } catch(Exception e) { }

    try {
      cal.setTimeInMillis(messageNode.getProperty(Utils.EXO_SENDDATE).getLong());
      msg.setSendDate(cal.getTime());
    } catch(Exception e) { }
    return msg ;
  }

  public void removeAccount(SessionProvider sProvider, String username, String accountId) throws Exception {
    Node homeNode = getMailHomeNode(sProvider, username) ;
    // gets the specified account, and removes it
    if (homeNode.hasNode(accountId)) {
      homeNode.getNode(accountId).remove() ;
      homeNode.getSession().save() ;
    }
  }

  public void removeMessage(SessionProvider sProvider, String username, String accountId, Message message) throws Exception {
    Node msgStoreNode = getDateStoreNode(sProvider, username, accountId, message.getReceivedDate());
    try {
      Node node = msgStoreNode.getNode(message.getId()) ;
      moveReference(node) ;
      node.remove() ;
      msgStoreNode.getSession().save();
    } catch(PathNotFoundException e) {}
  }

  public void removeMessage(SessionProvider sProvider, String username, String accountId, List<Message> messages) throws Exception {
    //  loops on the message names array, and removes each message
    for (Message message : messages) {
      removeMessage(sProvider, username, accountId, message);
    }
  }
  
  public void moveMessages(SessionProvider sProvider, String username, String accountId, Message msg, String currentFolderId, String destFolderId) throws Exception {
    Node messageHome = getMessageHome(sProvider, username, accountId);
    Node msgNode = (Node)messageHome.getSession().getItem(msg.getPath()) ;
    moveReference(msgNode) ;
    if (msgNode.hasProperty(Utils.EXO_FOLDERS)) {
      Boolean isUnread = msgNode.getProperty(Utils.EXO_ISUNREAD).getBoolean();
      Node currentFolderNode = getFolderNodeById(sProvider, username, accountId, currentFolderId);
      Node destFolderNode = getFolderNodeById(sProvider, username, accountId, destFolderId);
      Value[] propFolders = msgNode.getProperty(Utils.EXO_FOLDERS).getValues();
      String[] folderIds = new String[propFolders.length];
      for (int i = 0; i < propFolders.length; i++) {
        String folderId = propFolders[i].getString() ;
        if (currentFolderId.equals(folderId)) folderIds[i] = destFolderId ;
        else folderIds[i] = folderId;
      }
      msgNode.setProperty(Utils.EXO_FOLDERS, folderIds);
      msgNode.save();
      // Update number of unread messages
      if (isUnread) {
        currentFolderNode.setProperty(Utils.EXO_UNREADMESSAGES, (currentFolderNode.getProperty(Utils.EXO_UNREADMESSAGES).getLong() - 1));
        destFolderNode.setProperty(Utils.EXO_UNREADMESSAGES, (destFolderNode.getProperty(Utils.EXO_UNREADMESSAGES).getLong() + 1));
      }
      currentFolderNode.setProperty(Utils.EXO_TOTALMESSAGE, (currentFolderNode.getProperty(Utils.EXO_TOTALMESSAGE).getLong() - 1));
      destFolderNode.setProperty(Utils.EXO_TOTALMESSAGE, (destFolderNode.getProperty(Utils.EXO_TOTALMESSAGE).getLong() + 1));
      currentFolderNode.save();
      destFolderNode.save();
    }
  }

  public void saveAccount(SessionProvider sProvider, String username, Account account, boolean isNew) throws Exception {
    // creates or updates an account, depending on the isNew flag
    Node mailHome = getMailHomeNode(sProvider, username) ;
    Node newAccount = null;
    String accId = account.getId() ;
    if (isNew) { // creates the node
      newAccount = mailHome.addNode(accId, Utils.EXO_ACCOUNT);
      mailHome.save();
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
      if (account.getLastCheckedDate() != null)
        newAccount.setProperty(Utils.EXO_LAST_CHECKED_TIME, account.getLastCheckedDate().getTime());
      Iterator<String> it = account.getServerProperties().keySet().iterator();
      ArrayList<String> values = new ArrayList<String>(account.getServerProperties().size());
      while (it.hasNext()) {
        String key = it.next().toString();
        values.add(key+"="+account.getServerProperties().get(key));
      }
      newAccount.setProperty(Utils.EXO_SERVERPROPERTIES, values.toArray(new String[account.getServerProperties().size()]));
      
      if (account.getPopServerProperties() != null) {
        it = account.getPopServerProperties().keySet().iterator();
        values = new ArrayList<String>(account.getPopServerProperties().size());
        while (it.hasNext()) {
          String key = it.next().toString();
          values.add(key+"="+account.getPopServerProperties().get(key));
        }
        newAccount.setProperty(Utils.EXO_POPSERVERPROPERTIES, values.toArray(new String[account.getPopServerProperties().size()]));
      }
      
      if (account.getImapServerProperties() != null) {
        it = account.getImapServerProperties().keySet().iterator();
        values = new ArrayList<String>(account.getImapServerProperties().size());
        while (it.hasNext()) {
          String key = it.next().toString();
          values.add(key+"="+account.getImapServerProperties().get(key));
        }
        newAccount.setProperty(Utils.EXO_IMAPSERVERPROPERTIES, values.toArray(new String[account.getImapServerProperties().size()]));
      }
      // saves changes
      mailHome.getSession().save();
    }
  }
  
  public void saveMailSetting(SessionProvider sProvider, String username, MailSetting newSetting) throws Exception {
    Node mailHome = getMailHomeNode(sProvider, username) ;
    Node settingNode = null;
    try {
      settingNode = mailHome.getNode(Utils.KEY_MAIL_SETTING) ;
    } catch(PathNotFoundException e) {
      settingNode = mailHome.addNode(Utils.KEY_MAIL_SETTING, Utils.EXO_MAIL_SETTING) ;
      mailHome.save();
    }
   
    if (settingNode != null) {
      settingNode.setProperty(Utils.EXO_NUMBER_MSG_PER_PAGE, newSetting.getNumberMsgPerPage());
      settingNode.setProperty(Utils.EXO_PERIOD_CHECKMAIL_AUTO, newSetting.getPeriodCheckAuto());
      settingNode.setProperty(Utils.EXO_DEFAULT_ACCOUNT, newSetting.getDefaultAccount());
      settingNode.setProperty(Utils.EXO_FORMAT_AS_ORIGINAL, newSetting.formatAsOriginal());
      settingNode.setProperty(Utils.EXO_USE_WYSIWYG, newSetting.useWysiwyg());
      settingNode.setProperty(Utils.EXO_REPLY_WITH_ATTACH, newSetting.replyWithAttach());
      settingNode.setProperty(Utils.EXO_FORWARD_WITH_ATTACH, newSetting.forwardWithAtt());
      settingNode.setProperty(Utils.EXO_PREFIX_MESSAGE_WITH, newSetting.getPrefixMessageWith());
      settingNode.setProperty(Utils.EXO_SAVE_SENT_MESSAGE, newSetting.saveMessageInSent());
      // saves change
      settingNode.save();
    }
  }
  
  public void saveMessage(SessionProvider sProvider, String username, String accountId, String targetMsgPath, Message message) throws Exception {
    Node msgNode = saveMessage(sProvider, username, accountId, message, true) ;
    if (targetMsgPath != null && !targetMsgPath.equals("")) {
      Node mailHome = getMailHomeNode(sProvider, username) ;
      Node targetNode = (Node) mailHome.getSession().getItem(targetMsgPath) ;
      createReference(msgNode, targetNode) ;
    }
  }
  
  public Node saveMessage(SessionProvider sProvider, String username, String accountId, Message message, boolean isNew) throws Exception {
    Node mailHome = getMailHomeNode(sProvider, username) ;
    Node homeMsg = getDateStoreNode(sProvider, username, accountId, message.getReceivedDate());
    Node nodeMsg = null;
    if (isNew) { // creates the node
      nodeMsg = homeMsg.addNode(message.getId(), Utils.EXO_MESSAGE);
      homeMsg.save();
    } else { // gets the specified message
      nodeMsg = (Node) mailHome.getSession().getItem(message.getPath()) ;
    }
    if (nodeMsg != null) {
      // add some properties
      nodeMsg.setProperty(Utils.EXO_ID, message.getId());
      nodeMsg.setProperty(Utils.EXO_ACCOUNT, accountId);
      nodeMsg.setProperty(Utils.EXO_PATH, message.getPath());
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
      Iterator<String> ith = message.getHeaders().keySet().iterator();
      ArrayList<String> values = new ArrayList<String>(message.getHeaders().size());
      while (ith.hasNext()) {
        String key = ith.next().toString();
        values.add(key+"="+message.getHeaders().get(key));
      }
      nodeMsg.setProperty(Utils.EXO_HEADERS, values.toArray(new String[message.getHeaders().size()]));
      
      if (isNew) {
        List<Attachment> attachments = message.getAttachments();
        if(attachments != null) { 
          Iterator<Attachment> it = attachments.iterator();
          while (it.hasNext()) {
            Attachment file = it.next();
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
      
      if(nodeMsg.canAddMixin("mix:referenceable")) nodeMsg.addMixin("mix:referenceable") ;
      nodeMsg.setProperty(Utils.EXO_SUBJECT, message.getSubject()) ;
      nodeMsg.save();
    }
    return nodeMsg ;
  }
  
  public void saveMessage(SessionProvider sProvider, String username, String accId, javax.mail.Message msg, String folderId, SpamFilter spamFilter) throws Exception {
    long t1, t2, t3, t4 ;
    String msgId = MimeMessageParser.getMessageId(msg) ;
    Calendar gc = MimeMessageParser.getReceivedDate(msg) ;
    Node msgHomeNode = getDateStoreNode(sProvider, username, accId, gc.getTime()) ;
    if (msgHomeNode.hasNode(msgId)) return ;
    System.out.println("   [DEBUG] Saving message to JCR ...") ;
    t1 = System.currentTimeMillis();
    Node node = msgHomeNode.addNode(msgId, Utils.EXO_MESSAGE) ;
    msgHomeNode.save();
    node.setProperty(Utils.EXO_ID, msgId);
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
    
    if (spamFilter != null && spamFilter.checkSpam(msg)) {
      folderIds = new String[] { Utils.createFolderId(accId, Utils.FD_SPAM, false) } ;
    }
    node.setProperty(Utils.EXO_FOLDERS, folderIds);
    
    ArrayList<String> values = new ArrayList<String>() ;
    Enumeration enu = msg.getAllHeaders() ;
    while (enu.hasMoreElements()) {
      Header header = (Header)enu.nextElement() ;
      values.add(header.getName()+"="+header.getValue()) ;
    }
    node.setProperty(Utils.EXO_HEADERS, values.toArray(new String[]{}));
    
    System.out.println("     [DEBUG] Saved body and attachment of message .... size : " + Math.abs(msg.getSize()) + " B") ;
    t2 = System.currentTimeMillis();
    Object obj = msg.getContent() ;
    if (obj instanceof Multipart) {
      setMultiPart((Multipart)obj, node);
    } else {
      setPart(msg, node);
    }
    t3 = System.currentTimeMillis();
    System.out.println("     [DEBUG] Saved body (and attachments) of message finished : " + (t3 - t2) + " ms") ;
    node.save() ;
    t4 = System.currentTimeMillis();
    System.out.println("   [DEBUG] Saved total message to JCR finished : " + (t4 - t1) + " ms") ;
    
    System.out.println("   [DEBUG] Adding message to thread ...") ;
    t1 = System.currentTimeMillis();
    addMessageToThread(sProvider, username, accId, MimeMessageParser.getInReplyToHeader(msg), node) ;
    t2 = System.currentTimeMillis();
    System.out.println("   [DEBUG] Added message to thread finished : " + (t2 - t1) + " ms") ;
    
    System.out.println("   [DEBUG] Updating number message to folder ...") ;
    t1 = System.currentTimeMillis();
    
    Node folderHomeNode = getFolderHome(sProvider, username, accId) ;
    try { 
      Node folderNode = folderHomeNode.getNode(folderId);
      folderNode.setProperty(Utils.EXO_UNREADMESSAGES, folderNode.getProperty(Utils.EXO_UNREADMESSAGES).getLong() + 1) ;
      folderNode.setProperty(Utils.EXO_TOTALMESSAGE , folderNode.getProperty(Utils.EXO_TOTALMESSAGE).getLong() + 1) ;
      folderNode.save();
    } catch(PathNotFoundException e) { }
    
    t2 = System.currentTimeMillis();
    System.out.println("   [DEBUG] Updated number message to folder finished : " + (t2 - t1) + " ms") ;
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
            } else {
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

  public Folder getFolder(SessionProvider sProvider, String username, String accountId, String folderId) throws Exception {
    Folder folder = null;
    Node node = getFolderNodeById(sProvider, username, accountId, folderId);
    if (node != null) { 
      folder = new Folder();
      folder.setId(node.getProperty(Utils.EXO_ID).getString());
      folder.setLabel(node.getProperty(Utils.EXO_LABEL).getString());
      folder.setPath(node.getPath());
      folder.setName(node.getProperty(Utils.EXO_NAME).getString());
      folder.setPersonalFolder(node.getProperty(Utils.EXO_PERSONAL).getBoolean()) ;
      folder.setNumberOfUnreadMessage(node.getProperty(Utils.EXO_UNREADMESSAGES).getLong());
      folder.setTotalMessage(node.getProperty(Utils.EXO_TOTALMESSAGE).getLong());
    }
    return folder ;
  }
  
  public Node getFolderNodeById(SessionProvider sProvider, String username, String accountId, String folderId) throws Exception {
    Session sess = getMailHomeNode(sProvider, username).getSession();
    QueryManager qm = sess.getWorkspace().getQueryManager();
    // gets the specified folder node
    StringBuffer queryString = new StringBuffer("//element(*,exo:folder)[@exo:id='").
    append(folderId).
    append("']");
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    NodeIterator it = result.getNodes();
    Node node = null ; 
    if (it.hasNext()) node = it.nextNode();
    return node;
  }
  
  public Folder getFolder(Node node) throws Exception {
    Folder folder = new Folder();
  // if this folder exists, creates the object and returns it
    folder.setId(node.getProperty(Utils.EXO_ID).getString());
    folder.setLabel(node.getProperty(Utils.EXO_LABEL).getString());
    folder.setPath(node.getPath());
    folder.setName(node.getProperty(Utils.EXO_NAME).getString());
    folder.setPersonalFolder(node.getProperty(Utils.EXO_PERSONAL).getBoolean()) ;
    folder.setNumberOfUnreadMessage(node.getProperty(Utils.EXO_UNREADMESSAGES).getLong());
    folder.setTotalMessage(node.getProperty(Utils.EXO_TOTALMESSAGE).getLong());
    
    return folder ;
  }

  public List<Folder> getFolders(SessionProvider sProvider, String username, String accountId) throws Exception {
    List<Folder> folders = new ArrayList<Folder>() ;
    Node folderHomeNode = getFolderHome(sProvider, username, accountId) ;
    NodeIterator iter = folderHomeNode.getNodes() ;
    while (iter.hasNext()){
      Node folder = (Node)iter.next() ;
      folders.add(getFolder(sProvider, username, accountId, folder.getName())) ;
    }
    return folders ;
  }

  public void saveFolder(SessionProvider sProvider, String username, String accountId, Folder folder) throws Exception {
    // gets folder home node of the specified account
    Node home = getFolderHome(sProvider, username, accountId);
    Node myFolder = null;
    Node node = getFolderNodeById(sProvider, username, accountId, folder.getId());
    if (node != null) { // if the folder exists, gets it
      myFolder = node;
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
  
  public void saveFolder(SessionProvider sProvider, String username, String accountId, String parentId, Folder folder) throws Exception {
    // gets folder home node of the specified account
    Node home = getFolderHome(sProvider, username, accountId);
    Node parentNode = getFolderNodeById(sProvider, username, accountId, parentId);
    Node myFolder = null;
    if (parentNode.hasNode(folder.getId())) { // if the folder exists, gets it
      myFolder = parentNode.getNode(folder.getId());
    } else { // if it doesn't exist, creates it
      myFolder = parentNode.addNode(folder.getId(), Utils.EXO_FOLDER);
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

  public void removeUserFolder(SessionProvider sProvider, String username, Folder folder) throws Exception {
    // gets the mail home node
    Session sess = getMailHomeNode(sProvider, username).getSession();
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

  public void removeUserFolder(SessionProvider sProvider, String username, Account account, Folder folder) throws Exception {
    //  gets the specified folder
    Node node = getFolderNodeById(sProvider, username, account.getId(), folder.getId()); 
    if (node != null) {
      node.remove();
    }
    node.getSession().save();
  }
  
  
  
  public Node getFilterHome(SessionProvider sProvider, String username, String accountId) throws Exception {
    Node accountHome = getMailHomeNode(sProvider, username).getNode(accountId);
    if(accountHome.hasNode(Utils.KEY_FILTER)) return accountHome.getNode(Utils.KEY_FILTER) ;
    else {
      accountHome.addNode(Utils.KEY_FILTER, Utils.NT_UNSTRUCTURED) ;
      accountHome.save() ;
    } return accountHome.getNode(Utils.KEY_FILTER) ;
  }
  
  public List<MessageFilter> getFilters(SessionProvider sProvider, String username, String accountId) throws Exception {
    List<MessageFilter> filterList = new ArrayList<MessageFilter>();
    Node filterHomeNode = getFilterHome(sProvider, username, accountId) ;
    NodeIterator iter = filterHomeNode.getNodes() ;
    while (iter.hasNext()){
      Node filterNode = (Node)iter.next() ;
      MessageFilter filter = new MessageFilter("");
      try {
        filter.setId((filterNode.getProperty(Utils.EXO_ID).getString())) ;
      } catch(Exception e) { }
      try {
        filter.setName(filterNode.getProperty(Utils.EXO_NAME).getString()) ;
      } catch(Exception e) { }
      try {
        filter.setFrom(filterNode.getProperty(Utils.EXO_FROM).getString());
      } catch(Exception e) { }
      try {
        filter.setFromCondition((int)(filterNode.getProperty(Utils.EXO_FROM_CONDITION).getLong()));
      } catch(Exception e) { }
      try {
        filter.setTo(filterNode.getProperty(Utils.EXO_TO).getString());
      } catch(Exception e) { }
      try {
        filter.setToCondition((int)(filterNode.getProperty(Utils.EXO_TO_CONDITION).getLong()));
      } catch(Exception e) { }
      try {
        filter.setSubject(filterNode.getProperty(Utils.EXO_SUBJECT).getString());
      } catch(Exception e) { }
      try {
        filter.setSubjectCondition((int)(filterNode.getProperty(Utils.EXO_SUBJECT_CONDITION).getLong()));
      } catch(Exception e) { }
      try {
        filter.setBody(filterNode.getProperty(Utils.EXO_BODY).getString());
      } catch(Exception e) { }
      try {
        filter.setBodyCondition((int)(filterNode.getProperty(Utils.EXO_BODY_CONDITION).getLong()));
      } catch(Exception e) { }
      try {
        filter.setApplyFolder(filterNode.getProperty(Utils.EXO_APPLY_FOLDER).getString());
      } catch(Exception e) { }
      try {
        filter.setApplyTag(filterNode.getProperty(Utils.EXO_APPLY_TAG).getString());
      } catch(Exception e) { }
      try {
        filter.setKeepInInbox(filterNode.getProperty(Utils.EXO_KEEP_IN_INBOX).getBoolean());
      } catch(Exception e) { }
      try {
        filter.setApplyForAll(filterNode.getProperty(Utils.EXO_APPLY_FOR_ALL).getBoolean());
      } catch(Exception e) { }
      filterList.add(filter);
    }
    return filterList ;
  }
  
  public MessageFilter getFilterById(SessionProvider sProvider, String username, String accountId, String filterId) throws Exception {
    Node filterHomeNode = getFilterHome(sProvider, username, accountId) ;
    MessageFilter filter = new MessageFilter("");
    if (filterHomeNode.hasNode(filterId)) {
      Node filterNode = filterHomeNode.getNode(filterId);
      try {
        filter.setId((filterNode.getProperty(Utils.EXO_ID).getString())) ;
      } catch(Exception e) { }
      try {
        filter.setName(filterNode.getProperty(Utils.EXO_NAME).getString()) ;
      } catch(Exception e) { }
      try {
        filter.setFrom(filterNode.getProperty(Utils.EXO_FROM).getString());
      } catch(Exception e) { }
      try {
        filter.setFromCondition((int)(filterNode.getProperty(Utils.EXO_FROM_CONDITION).getLong()));
      } catch(Exception e) { }
      try {
        filter.setTo(filterNode.getProperty(Utils.EXO_TO).getString());
      } catch(Exception e) { }
      try {
        filter.setToCondition((int)(filterNode.getProperty(Utils.EXO_TO_CONDITION).getLong()));
      } catch(Exception e) { }
      try { 
        filter.setSubject(filterNode.getProperty(Utils.EXO_SUBJECT).getString());
      } catch(Exception e) { }
      try {
        filter.setSubjectCondition((int)(filterNode.getProperty(Utils.EXO_SUBJECT_CONDITION).getLong()));
      } catch(Exception e) { }
      try {
        filter.setBody(filterNode.getProperty(Utils.EXO_BODY).getString());
      } catch(Exception e) { }
      try {
        filter.setBodyCondition((int)(filterNode.getProperty(Utils.EXO_BODY_CONDITION).getLong()));
      } catch(Exception e) { }
      try {
        filter.setApplyFolder(filterNode.getProperty(Utils.EXO_APPLY_FOLDER).getString());
      } catch(Exception e) { }
      try {
        filter.setApplyTag(filterNode.getProperty(Utils.EXO_APPLY_TAG).getString());
      } catch(Exception e) { }
      try {
        filter.setKeepInInbox(filterNode.getProperty(Utils.EXO_KEEP_IN_INBOX).getBoolean());
      } catch(Exception e) { }
      try {
        filter.setApplyForAll(filterNode.getProperty(Utils.EXO_APPLY_FOR_ALL).getBoolean());
      } catch(Exception e) { }
    }
    return filter ;
  }
  
  public void saveFilter(SessionProvider sProvider, String username, String accountId, MessageFilter filter) throws Exception {
    Node home = getFilterHome(sProvider, username, accountId);
    Node filterNode = null;
    if (home.hasNode(filter.getId())) { // if the filter exists, gets it
      filterNode = home.getNode(filter.getId());
    } else { // if it doesn't exist, creates it
      filterNode = home.addNode(filter.getId(), Utils.EXO_FILTER);
    }
    // sets some properties
    filterNode.setProperty(Utils.EXO_ID, filter.getId());
    filterNode.setProperty(Utils.EXO_NAME, filter.getName());
    filterNode.setProperty(Utils.EXO_FROM, filter.getFrom());
    filterNode.setProperty(Utils.EXO_FROM_CONDITION, (long)filter.getFromCondition());
    filterNode.setProperty(Utils.EXO_TO, filter.getTo());
    filterNode.setProperty(Utils.EXO_TO_CONDITION, (long)filter.getToCondition());
    filterNode.setProperty(Utils.EXO_SUBJECT, filter.getSubject());
    filterNode.setProperty(Utils.EXO_SUBJECT_CONDITION, (long)filter.getSubjectCondition());
    filterNode.setProperty(Utils.EXO_BODY, filter.getBody());
    filterNode.setProperty(Utils.EXO_BODY_CONDITION, (long)filter.getBodyCondition());
    filterNode.setProperty(Utils.EXO_APPLY_FOLDER, filter.getApplyFolder());
    filterNode.setProperty(Utils.EXO_APPLY_TAG, filter.getApplyTag());
    filterNode.setProperty(Utils.EXO_KEEP_IN_INBOX, filter.keepInInbox());
    filterNode.setProperty(Utils.EXO_APPLY_FOR_ALL, filter.applyForAll());
    home.getSession().save();
  }
  
  public void removeFilter(SessionProvider sProvider, String username, String accountId, String filterId) throws Exception {
    Node filterHome = getFilterHome(sProvider, username, accountId);
    if (filterHome.hasNode(filterId)) {
      filterHome.getNode(filterId).remove();
    }
    filterHome.getSession().save();
  }

  public Node getMessageHome(SessionProvider sProvider, String username, String accountId) throws Exception {
    Node accountHome = getMailHomeNode(sProvider, username).getNode(accountId) ;
    Node msgHome = null ;
    try {
      msgHome = accountHome.getNode(Utils.KEY_MESSAGE) ;
    } catch(PathNotFoundException e) {
      msgHome = accountHome.addNode(Utils.KEY_MESSAGE, Utils.NT_UNSTRUCTURED) ;
      accountHome.save() ;
    }
    return msgHome ;
  }

  public Node getFolderHome(SessionProvider sProvider, String username, String accountId) throws Exception {
    Node accountHome = getMailHomeNode(sProvider, username).getNode(accountId) ;
    Node folderHome = null ;
    try {
      folderHome = accountHome.getNode(Utils.KEY_FOLDERS) ;
    } catch(PathNotFoundException e) {
      folderHome = accountHome.addNode(Utils.KEY_FOLDERS, Utils.NT_UNSTRUCTURED);
      accountHome.save() ;
    }
    return folderHome ;    
  }

  public Node getTagHome(SessionProvider sProvider, String username, String accountId) throws Exception {
    Node accountHome = getMailHomeNode(sProvider, username).getNode(accountId) ;
    Node tagHome = null ;
    try {
      tagHome = accountHome.getNode(Utils.KEY_TAGS) ;
    } catch(PathNotFoundException e) {
      tagHome = accountHome.addNode(Utils.KEY_TAGS, Utils.NT_UNSTRUCTURED) ;
      accountHome.save() ;
    }    
    return tagHome ;   
  }

  public void addTag(SessionProvider sProvider, String username, String accountId, List<Message> messages, List<Tag> tagList)
  throws Exception {    
    Map<String, String> tagMap = new HashMap<String, String> () ;
    Node tagHome = getTagHome(sProvider, username, accountId) ;
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
    
    Node mailHome = getMailHomeNode(sProvider, username) ;
    for(Message message : messages) {
      Map<String, String> messageTagMap = new HashMap<String, String> () ;
      Node messageNode = (Node) mailHome.getSession().getItem(message.getPath()) ;
      try {
        Value[] values = messageNode.getProperty(Utils.EXO_TAGS).getValues() ;
        for(Value value : values) {
          messageTagMap.put(value.getString(), value.getString()) ;
        }
      } catch(Exception e) { }
      messageTagMap.putAll(tagMap) ;
      messageNode.setProperty(Utils.EXO_TAGS, messageTagMap.values().toArray(new String[]{})) ;
      
      messageNode.save() ;
    }
  }


  public List<Tag> getTags(SessionProvider sProvider, String username, String accountId) throws Exception {
    List<Tag> tags = new ArrayList<Tag>() ;
    Node tagHomeNode = getTagHome(sProvider, username, accountId) ;
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
  
  public Tag getTag(SessionProvider sProvider, String username, String accountId, String tagId) throws Exception {
    Node tagHomeNode = getTagHome(sProvider, username, accountId) ;
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

  public void removeMessageTag(SessionProvider sProvider, String username, String accountId, List<Message> messages, List<String> tagIds) 
  throws Exception {
    Node mailHome = getMailHomeNode(sProvider, username) ;
    for (Message msg : messages) {
      try {
        Node msgNode = (Node) mailHome.getSession().getItem(msg.getPath());
        try {
          Value[] propTags = msgNode.getProperty(Utils.EXO_TAGS).getValues();
          String[] oldTagIds = new String[propTags.length];
          for (int i = 0; i < propTags.length; i++) {
            oldTagIds[i] = propTags[i].getString();
          }
          List<String> tagList = new ArrayList<String>(Arrays.asList(oldTagIds)); 
          tagList.removeAll(tagIds);
          String[] newTagIds = tagList.toArray(new String[tagList.size()]);
          msgNode.setProperty(Utils.EXO_TAGS, newTagIds);
          msgNode.save();
        } catch(Exception e) { }
      } catch(PathNotFoundException e) {}
    }
  }


  public void removeTag(SessionProvider sProvider, String username, String accountId, String tagId) throws Exception {
    // remove this tag in all messages
    List<Message> listMessage = getMessageByTag(sProvider, username, accountId, tagId);
    List<String> listTag = new ArrayList<String>();
    listTag.add(tagId);
    removeMessageTag(sProvider, username, accountId, listMessage, listTag);

    // remove tag node
    Node tagHomeNode = getTagHome(sProvider, username, accountId) ;
    if (tagHomeNode.hasNode(tagId)) {
      tagHomeNode.getNode(tagId).remove() ;
    }

    tagHomeNode.getSession().save() ;
  } 
  
  public void updateTag(SessionProvider sProvider, String username, String accountId, Tag tag) throws Exception {
    Node tagHome = getTagHome(sProvider, username, accountId) ;
    if (tagHome.hasNode(tag.getId())) {
      Node tagNode = tagHome.getNode(tag.getId());
      tagNode.setProperty(Utils.EXO_NAME, tag.getName());
      tagNode.setProperty(Utils.EXO_DESCRIPTION, tag.getDescription());
      tagNode.setProperty(Utils.EXO_COLOR, tag.getColor());
    }
    tagHome.save();
  }

  public List<Message> getMessageByTag(SessionProvider sProvider, String username, String accountId, String tagId)
  throws Exception {
    List<Message> messages = new ArrayList<Message>();
    Node mailHomeNode = getMailHomeNode(sProvider, username) ;
    QueryManager qm = mailHomeNode.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root" + mailHomeNode.getNode(accountId).getPath() + "//element(*,exo:message)[@exo:tags='").
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
  
  public Node getSpamFilterHome(SessionProvider sProvider, String username, String accountId) throws Exception {
    Node accountHome = getMailHomeNode(sProvider, username).getNode(accountId);
    if(accountHome.hasNode(Utils.KEY_SPAM_FILTER)) return accountHome.getNode(Utils.KEY_SPAM_FILTER) ;
    else return accountHome.addNode(Utils.KEY_SPAM_FILTER, Utils.NT_UNSTRUCTURED) ;
  }
  
  public SpamFilter getSpamFilter(SessionProvider sProvider, String username, String accountId) throws Exception { 
    Node accountNode = getSpamFilterHome(sProvider, username, accountId);
    NodeIterator it = accountNode.getNodes();
    Node spamFilterNode = null;
    while (it.hasNext()) {
      Node node = it.nextNode();
      if (node.isNodeType(Utils.EXO_SPAM_FILTER)) {
        spamFilterNode = node;
        break ;
      }
    }
    SpamFilter spamFilter = new SpamFilter();
    if (spamFilterNode != null) {
      try {
        Value[] propFroms = spamFilterNode.getProperty(Utils.EXO_FROMS).getValues();
        String[] froms = new String[propFroms.length];
        for (int i = 0; i < propFroms.length; i++) {
          froms[i] = propFroms[i].getString();
        }
        spamFilter.setSenders(froms);
      } catch(Exception e) { }
    }
    return spamFilter ;
  }
  
  public void saveSpamFilter(SessionProvider sProvider, String username, String accountId, SpamFilter spamFilter) throws Exception {
    Node accountNode = getSpamFilterHome(sProvider, username, accountId);
    Node spamFilterNode = null;
    if (accountNode.hasNode(Utils.EXO_SPAM_FILTER)) {
      spamFilterNode = accountNode.getNode(Utils.EXO_SPAM_FILTER);
    } else {
      spamFilterNode = accountNode.addNode(Utils.EXO_SPAM_FILTER, Utils.EXO_SPAM_FILTER);
    }
    
    spamFilterNode.setProperty(Utils.EXO_FROMS, spamFilter.getSenders());
    accountNode.getSession().save();
  }
  
  public void toggleMessageProperty(SessionProvider sProvider, String username, String accountId, List<Message> msgList, String property) throws Exception {
    Node mailHome = getMailHomeNode(sProvider, username);
    for (Message msg : msgList) {
      Node msgNode = (Node) mailHome.getSession().getItem(msg.getPath()) ;
      if (property.equals(Utils.EXO_STAR)) {
        msgNode.setProperty(Utils.EXO_STAR, !msgNode.getProperty(Utils.EXO_STAR).getBoolean());
        msgNode.save();
      } else if (property.equals(Utils.EXO_ISUNREAD)) {
        Boolean isUnread = msgNode.getProperty(Utils.EXO_ISUNREAD).getBoolean();
        msgNode.setProperty(Utils.EXO_ISUNREAD, !isUnread);
        msgNode.save();

        Node currentFolderNode = getFolderNodeById(sProvider, username, accountId, msgNode.getProperty(Utils.EXO_FOLDERS).getValues()[0].getString());
        if (isUnread) {
          currentFolderNode.setProperty(Utils.EXO_UNREADMESSAGES, (currentFolderNode.getProperty(Utils.EXO_UNREADMESSAGES).getLong() - 1));
        } else { 
          currentFolderNode.setProperty(Utils.EXO_UNREADMESSAGES, (currentFolderNode.getProperty(Utils.EXO_UNREADMESSAGES).getLong() + 1));
        }
        currentFolderNode.save();
      }
    }
  }
    
  public String getFolderHomePath(SessionProvider sProvider, String username, String accountId) throws Exception {
    return getFolderHome(sProvider, username, accountId).getPath();
  }
  
  public List<Folder> getSubFolders(SessionProvider sProvider, String username, String accountId, String parentPath) throws Exception {
    Node home = getFolderHome(sProvider, username, accountId);
    Node parentNode = (Node)home.getSession().getItem(parentPath);
    List<Folder> childFolders = new ArrayList<Folder>();
    NodeIterator it = parentNode.getNodes();
    while (it.hasNext()) {
      // browse the accounts and add them to the return list
      Node node = it.nextNode();
      if (node.isNodeType(Utils.EXO_FOLDER)) {
        if (node.hasProperty(Utils.EXO_PERSONAL) && node.getProperty(Utils.EXO_PERSONAL).getBoolean()) childFolders.add(getFolder(node));
      }
    }
    return childFolders ;
  }
  
  public void execActionFilter(SessionProvider sProvider, String username, String accountId, Calendar checkTime) throws Exception {
    List<MessageFilter> msgFilters = getFilters(sProvider, username, accountId);
    Node homeMsg = getMessageHome(sProvider, username, accountId);
    Session sess = getMailHomeNode(sProvider, username).getSession();
    QueryManager qm = sess.getWorkspace().getQueryManager();
    for (MessageFilter filter : msgFilters) {
      String applyFolder = filter.getApplyFolder() ;
      String applyTag = filter.getApplyTag() ;
      filter.setAccountPath(homeMsg.getPath()) ;
      filter.setAccountId(accountId);
      filter.setFromDate(checkTime);
      String queryString = filter.getStatement();
      Query query = qm.createQuery(queryString, Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator it = result.getNodes();
      while (it.hasNext()) {
        Message msg  = getMessage(it.nextNode());
        if (!Utils.isEmptyField(applyFolder) && (getFolder(sProvider, username, accountId, applyFolder) != null)) {
          Folder folder = getFolder(sProvider, username, accountId, applyFolder);
          if (folder !=null)
            moveMessages(sProvider, username, accountId, msg, msg.getFolders()[0], applyFolder);  
        }
        if (!Utils.isEmptyField(applyTag)) {
          Tag tag = getTag(sProvider, username, accountId, applyTag);
          if (tag != null) {
            List<Message> msgList = new ArrayList<Message>();
            msgList.add(msg);
            List<Tag> tagList = new ArrayList<Tag>();
            tagList.add(tag);
            addTag(sProvider, username, accountId, msgList, tagList);
          }
        }  
      }
    } 
  }
  
  public Node getDateStoreNode(SessionProvider sProvider, String username, String accountId, Date date) throws Exception {
    Node msgHome = getMessageHome(sProvider, username, accountId);
    java.util.Calendar calendar = new GregorianCalendar() ;
    calendar.setTime(date) ;
    Node yearNode;
    Node monthNode;
    String year = "Y" + String.valueOf(calendar.get(java.util.Calendar.YEAR)) ;
    String month = "M" + String.valueOf(calendar.get(java.util.Calendar.MONTH) + 1) ;
    String day = "D" + String.valueOf(calendar.get(java.util.Calendar.DATE)) ;
    try {
      yearNode = msgHome.getNode(year) ;
    } catch (Exception e) {
      yearNode = msgHome.addNode(year, Utils.NT_UNSTRUCTURED) ;
      msgHome.save();
    }
    try {
      monthNode = yearNode.getNode(month) ;
    } catch (Exception e) {
      monthNode = yearNode.addNode(month, Utils.NT_UNSTRUCTURED) ;
      yearNode.save() ;
    }
    try {
      return monthNode.getNode(day) ;
    } catch (Exception e) {
      Node dayNode = monthNode.addNode(day, Utils.NT_UNSTRUCTURED) ;
      monthNode.save();
      return dayNode ;
    }
  }
  
  private Node getMatchingThread(SessionProvider sProvider, String username, String accountId, String inReplyToHeader, Node msg) throws Exception {
    Node converNode = null ;
    if (inReplyToHeader.equals(msg.getName())) return null ;
    Session sess = getMailHomeNode(sProvider, username).getSession();
    QueryManager qm = sess.getWorkspace().getQueryManager() ;
    StringBuffer queryString = new StringBuffer("//element(*,exo:message)[@exo:id='").
    append(inReplyToHeader).
    append("']");
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    NodeIterator it = result.getNodes();
    if (it.hasNext()) converNode = it.nextNode();
    return converNode;
  }
  
  public void addMessageToThread(SessionProvider sProvider, String username, String accountId, String inReplyToHeader, Node msgNode) throws Exception {
    Node converNode = getMatchingThread(sProvider, username, accountId, inReplyToHeader, msgNode) ;  
    try {
      if (converNode != null && converNode.isNodeType("exo:message")) {
        //TODO: add when save message
        msgNode.addMixin("mix:referenceable") ;
        createReference(msgNode, converNode) ;
        msgNode.save() ;
        converNode.save() ;
      } else {
        msgNode.addMixin("mix:referenceable") ;
        msgNode.save() ;
      }
    } catch(Exception e) {
      e.printStackTrace() ;
    }
  }
  
  private void createReference(Node msgNode, Node converNode) throws Exception {
    List<Value> valueList = new ArrayList<Value>() ;
    Value[] values = {};
    if (msgNode.isNodeType("exo:messageMixin")) {     
      values = msgNode.getProperty("exo:conversationId").getValues();
    } else {
      msgNode.addMixin("exo:messageMixin");
    }
    boolean isExist = false ; 
    for (int i = 0; i < values.length; i++) {
      Value value = values[i];
      String uuid = value.getString();
      Node refNode = converNode.getSession().getNodeByUUID(uuid);
      if(refNode.getPath().equals(converNode.getPath())) {
        isExist = true ; 
        break ;
      }
      valueList.add(value) ;
    }
    if(!isExist) {
      Value value2add = msgNode.getSession().getValueFactory().createValue(converNode);
      valueList.add(value2add) ;
    } 
    
    if(valueList.size() > 0) {
      msgNode.setProperty("exo:conversationId", valueList.toArray( new Value[valueList.size()]));
      msgNode.save() ;
    }
  }
  
  /*
   * Move reference : to first parent if it is exist, if not move reference to first child message. 
   */
  private void moveReference(Node node) throws Exception {
    List<Value> valueList = new ArrayList<Value>() ;
    Value[] values = {};
    PropertyIterator iter = node.getReferences() ;
    Node msgNode ;
    Node firstNode = null;
    while (iter.hasNext()) {
      msgNode = iter.nextProperty().getParent() ;
      if (msgNode.isNodeType("exo:messageMixin")) {     
        values = msgNode.getProperty("exo:conversationId").getValues();
        
        for (int i = 0; i < values.length; i++) valueList.add(values[i]) ;
        
        Node parentNode = null ;
        if (node.hasProperty("exo:conversationId")) {
          Value[] currentValues = node.getProperty("exo:conversationId").getValues();
          //TODO: get parent have the same folder with child message
          if (currentValues.length > 0) {
            parentNode = node.getSession().getNodeByUUID(currentValues[0].getString()) ;
          }
        }

        if (parentNode != null) {
          valueList.add(msgNode.getSession().getValueFactory().createValue(parentNode)) ;
        } else if (firstNode != null) {
          valueList.add(msgNode.getSession().getValueFactory().createValue(firstNode)) ;
        }
        
        if (firstNode == null) firstNode = msgNode ;
        
        msgNode.setProperty("exo:conversationId", valueList.toArray( new Value[valueList.size()]));
        msgNode.save() ;
      }
    }
  }
  
  public List<Message> getReferencedMessages(SessionProvider sProvider, String username, String accountId, String msgPath) throws Exception {
    Node mailHome = getMailHomeNode(sProvider, username);
    List<Message> msgList = new ArrayList<Message>() ;
    Node converNode = (Node) mailHome.getSession().getItem(msgPath) ;
    PropertyIterator iter = converNode.getReferences() ;
    Node msgNode ;
    while (iter.hasNext()) {
      msgNode = iter.nextProperty().getParent() ;
      msgList.add(getMessage(msgNode)) ;
    }
    return msgList ;
  }
}