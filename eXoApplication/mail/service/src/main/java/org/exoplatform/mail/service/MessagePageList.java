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
package org.exoplatform.mail.service;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PropertyIterator;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
/**
 * @author Hung Nguyen (hung.nguyen@exoplatform.com)
 * @since July 25, 2007
 */
public class MessagePageList extends JCRPageList {
  
  private NodeIterator iter_ = null ;
  private boolean isQuery_ = false ;
  private String value_ ;
  Map<String, Message> previousListPage ;
  
  public MessagePageList(NodeIterator iter, long pageSize, String value, boolean isQuery ) throws Exception{
    super(pageSize) ;
    iter_ = iter ;
    value_ = value ;
    isQuery_ = isQuery ;
    setAvailablePage(iter.getSize()) ;    
  }
  
  protected void populateCurrentPage(long page, String username) throws Exception  {
    if(iter_ == null) {
      Session session = getJCRSession(username) ;
      if(isQuery_) {
        QueryManager qm = session.getWorkspace().getQueryManager() ;
        Query query = qm.createQuery(value_, Query.XPATH);
        QueryResult result = query.execute();
        iter_ = result.getNodes();
      } else {
        Node node = (Node)session.getItem(value_) ;
        iter_ = node.getNodes() ;
      }
      session.logout() ;
    }
    setAvailablePage(iter_.getSize()) ;
    Node currentNode ;
    long pageSize = getPageSize() ;
    long position = 0 ;
    if(page == 1) position = 0;
    else {
      position = (page-1) * pageSize ;
      iter_.skip(position) ;
    }
    previousListPage = new HashMap<String, Message>() ;
    if (currentListPage_ != null) previousListPage = currentListPage_;
    currentListPage_ = new LinkedHashMap<String, Message>() ;
    
    for(int i = 0; i < pageSize; i ++) {
      if(iter_.hasNext()){
        currentNode = iter_.nextNode() ;
        /*String nodeType = "exo:message" ;        
        if (Utils.SHOWCONVERSATION) nodeType = "exo:conversationMixin" ;*/
        Value[] values = {};
        boolean existRefNode = false ; 
        String curMsgFolder = currentNode.getProperty(Utils.EXO_FOLDERS).getValues()[0].getString() ;
        String accId = currentNode.getProperty(Utils.EXO_ACCOUNT).getString() ;
        String sentFolderId = Utils.createFolderId(accId, Utils.FD_SENT, false) ;
        String[] refFolders = new String[] {sentFolderId, curMsgFolder} ;
        try {
          values = currentNode.getProperty("exo:conversationId").getValues();
          for (int j = 0; j < values.length; j++) {
            Value value = values[j];
            String uuid = value.getString();
            Node refNode = currentNode.getSession().getNodeByUUID(uuid);
            String refMsgFolder = refNode.getProperty(Utils.EXO_FOLDERS).getValues()[0].getString() ;
            if (refMsgFolder.equals(curMsgFolder)) existRefNode = true ;
            if (refMsgFolder.equals(sentFolderId)) {
              existRefNode = true ; 
              Message refMsg ;
              if (previousListPage.size() > 0 && previousListPage.containsKey(refNode.getName())) refMsg = previousListPage.get(refNode.getName()) ;
              else refMsg = getMessage(refNode, refFolders) ;
              currentListPage_.put(refMsg.getId(), refMsg) ;
              currentListPage_ = getMessageList(currentListPage_, refNode, curMsgFolder, refFolders) ;
            }
          }
        } catch(Exception e) { }
        if (Utils.SHOWCONVERSATION && (!currentNode.isNodeType("exo:messageMixin") || !existRefNode)) {
          Message msg ; 
          if (previousListPage.size() > 0 && previousListPage.containsKey(currentNode.getName())) msg = previousListPage.get(currentNode.getName()) ;
          else msg = getMessage(currentNode, refFolders) ;
          currentListPage_.put(msg.getId(), msg) ;
          currentListPage_ = getMessageList(currentListPage_, currentNode, curMsgFolder, refFolders) ;
        }
        /* else if(!Utils.SHOWCONVERSATION && currentNode.isNodeType(nodeType)) {
          currentListPage_.put(msg.getId(), msg) ;
        }*/
      }else {
        break ;
      }
    }
    iter_ = null ; 
  }
  
  private LinkedHashMap<String, Message> getMessageList(LinkedHashMap<String, Message> listPage, Node currentNode, String folderId, String[] refFolders) throws Exception {
    PropertyIterator prosIter = currentNode.getReferences() ;
    String accId = currentNode.getProperty(Utils.EXO_ACCOUNT).getString() ;
    String sentFolderId = Utils.createFolderId(accId, Utils.FD_SENT, false) ;
    Node msgNode ;
    while (prosIter.hasNext()) {
      msgNode = prosIter.nextProperty().getParent() ;
      if (msgNode.isNodeType("exo:message")) {
        String msgFolder = msgNode.getProperty(Utils.EXO_FOLDERS).getValues()[0].getString() ;
        if (folderId.equals(msgFolder) || sentFolderId.equals(msgFolder)) {
          Message msg ; 
          if (previousListPage.size() > 0 && previousListPage.containsKey(msgNode.getName())) msg = previousListPage.get(msgNode.getName()) ;
          else msg = getMessage(msgNode, refFolders) ;
          msg.setIsRootConversation(false) ;
          listPage.put(msg.getId(), msg) ;
          if (msgNode.isNodeType("mix:referenceable")) {
            listPage = getMessageList(listPage, msgNode, folderId, refFolders) ;
          }
        }
      }
    }
    return listPage ;
  }
  
  private Message getMessage(Node messageNode, String[] refFolders) throws Exception {
    Message msg = new Message();
    if (messageNode.hasProperty(Utils.EXO_ID)) msg.setId(messageNode.getProperty(Utils.EXO_ID).getString());
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
      msg.setContentType(messageNode.getProperty(Utils.EXO_CONTENT_TYPE).getString());
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
    
    if (Utils.SHOWCONVERSATION) {
      if (refFolders == null) refFolders = new String[]{ msg.getFolders()[0] } ;
      List<String> referedMessageIds = new ArrayList<String>() ;
      PropertyIterator prosIter = messageNode.getReferences() ;
      Node msgNode ;
      while (prosIter.hasNext()) {
        msgNode = prosIter.nextProperty().getParent() ;
        for(int i=0; i < refFolders.length; i ++) {
          if (refFolders[i].equals(msgNode.getProperty(Utils.EXO_FOLDERS).getValues()[0].getString())) {
            if (msgNode.isNodeType("exo:message")) referedMessageIds.add(msgNode.getProperty(Utils.EXO_ID).getString()) ;
            break ;
          }
        }
        
      }
      msg.setReferedMessageIds(referedMessageIds);
      
      List<String> groupedMessageIds = new ArrayList<String>() ;
      groupedMessageIds = getGroupedMessageIds(groupedMessageIds, messageNode, refFolders) ;
      msg.setGroupedMessageIds(groupedMessageIds);
    }
    
    return msg ;
  }
  
  private List<String> getGroupedMessageIds(List<String> list, Node currentNode, String[] refFolders) throws Exception {
    PropertyIterator prosIter = currentNode.getReferences() ;
    Node msgNode ;
    while (prosIter.hasNext()) {
      msgNode = prosIter.nextProperty().getParent() ;
      if (msgNode.isNodeType("exo:message")) {
        String msgFolderId = msgNode.getProperty(Utils.EXO_FOLDERS).getValues()[0].getString() ;
        String msgNodeId = msgNode.getProperty(Utils.EXO_ID).getString() ;
        for(int i=0; i < refFolders.length; i ++) {
          if (refFolders[i].equals(msgFolderId)) {
            list.add(msgNodeId) ;
            if (msgNode.isNodeType("mix:referenceable")) {
              list = getGroupedMessageIds(list, msgNode, refFolders) ;
            }
            break ;
          }
        }
      }
    }
    return list ;
  }
  
  
/*  private String [] ValuesToStrings(Value[] Val) throws Exception {
  	if(Val.length == 1)
  		return new String[]{Val[0].getString()};
		String[] Str = new String[Val.length];
		for(int i = 0; i < Val.length; ++i) {
		  Str[i] = Val[i].getString();
		}
		return Str;
  }*/
  
	@Override
  public List<Message> getAll() throws Exception { return null; }
	
  public List<Message> getAll(String username) throws Exception { 
    List<Message> messageList = new ArrayList<Message>();
    for (int i = 1; i <= getAvailablePage(); i++) {
      messageList.addAll(getPage(i, username));
    }
    return messageList;
  }

  private Session getJCRSession(String username) throws Exception {
    RepositoryService  repositoryService = (RepositoryService)PortalContainer.getComponent(RepositoryService.class) ;
    SessionProvider sessionProvider = SessionProvider.createSystemProvider() ;
    String defaultWS = 
      repositoryService.getDefaultRepository().getConfiguration().getDefaultWorkspaceName() ;
    return sessionProvider.getSession(defaultWS, repositoryService.getCurrentRepository()) ;
  }

}
