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
import java.util.List;

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
    currentListPage_ = new ArrayList<Message>() ;
    for(int i = 0; i < pageSize; i ++) {
      if(iter_.hasNext()){
        currentNode = iter_.nextNode() ;
        String nodeType = "exo:message" ;        
        if (Utils.SHOWCONVERSATION) nodeType = "exo:conversationMixin" ;
        Message msg = getMessage(currentNode) ;
        
        Value[] values = {};
        boolean isExist = false ; 
        if (currentNode.hasProperty("exo:conversationId")) {
          values = currentNode.getProperty("exo:conversationId").getValues();
          for (int j = 0; j < values.length; j++) {
            Value value = values[j];
            String uuid = value.getString();
            Node refNode = currentNode.getSession().getNodeByUUID(uuid);
            String refMsgFolder = refNode.getProperty("exo:folders").getValues()[0].getString() ;
            if (refMsgFolder.equals(currentNode.getProperty("exo:folders").getValues()[0].getString()))
              isExist = true ; 
            String sentFolderId = Utils.createFolderId(currentNode.getProperty(Utils.EXO_ACCOUNT).getString(), Utils.FD_SENT, false) ;
            if (refMsgFolder.equals(sentFolderId)) {
              currentListPage_.add(getMessage(refNode)) ;
              msg.setIsRootConversation(false) ;
            }
          }
        }
        if (Utils.SHOWCONVERSATION && (!currentNode.isNodeType("exo:messageMixin") || !isExist)) {
          currentListPage_.add(msg) ;
          currentListPage_ = getMessageList(currentListPage_, currentNode, msg.getFolders()[0]) ;
        } else if(!Utils.SHOWCONVERSATION && currentNode.isNodeType(nodeType)) {
          currentListPage_.add(getMessage(currentNode)) ;
        }
      }else {
        break ;
      }
    }
    iter_ = null ;    
  }
  
  private List<Message> getMessageList(List<Message> listPage, Node currentNode, String folderId) throws Exception {
    PropertyIterator prosIter = currentNode.getReferences() ;
    Node msgNode ;
    while (prosIter.hasNext()) {
      msgNode = prosIter.nextProperty().getParent() ;
      if (msgNode.isNodeType("exo:message")) {
        Message msg = getMessage(msgNode) ;
        msg.setIsRootConversation(false) ;
        String sentFolderId = Utils.createFolderId(msg.getAccountId(), Utils.FD_SENT, false) ;
        String draftFolderId = Utils.createFolderId(msg.getAccountId(), Utils.FD_DRAFTS, false) ;
        if (folderId.equals(msg.getFolders()[0]) || sentFolderId.equals(msg.getFolders()[0]) || draftFolderId.equals(msg.getFolders()[0])) {
          listPage.add(msg) ;
          if (msgNode.isNodeType("mix:referenceable")) {
            listPage = getMessageList(listPage, msgNode, folderId) ;
          }
        }
      }
    }
    return listPage ;
  }
  
  private Message getMessage(Node messageNode) throws Exception {
    Message msg = new Message();
    if (messageNode.hasProperty(Utils.EXO_ID)) msg.setId(messageNode.getProperty(Utils.EXO_ID).getString());
    msg.setPath(messageNode.getPath());
    if (messageNode.hasProperty(Utils.EXO_ACCOUNT)) msg.setAccountId(messageNode.getProperty(Utils.EXO_ACCOUNT).getString()) ;
    if (messageNode.hasProperty(Utils.EXO_FROM)) msg.setFrom(messageNode.getProperty(Utils.EXO_FROM).getString());
    if (messageNode.hasProperty(Utils.EXO_TO)) msg.setMessageTo(messageNode.getProperty(Utils.EXO_TO).getString());
    if (messageNode.hasProperty(Utils.EXO_SUBJECT)) msg.setSubject(messageNode.getProperty(Utils.EXO_SUBJECT).getString());
    if (messageNode.hasProperty(Utils.EXO_CC)) msg.setMessageCc(messageNode.getProperty(Utils.EXO_CC).getString());
    if (messageNode.hasProperty(Utils.EXO_BCC)) msg.setMessageBcc(messageNode.getProperty(Utils.EXO_BCC).getString());
    if (messageNode.hasProperty(Utils.EXO_REPLYTO)) msg.setReplyTo(messageNode.getProperty(Utils.EXO_REPLYTO).getString());
    if (messageNode.hasProperty(Utils.EXO_BODY)) msg.setMessageBody(messageNode.getProperty(Utils.EXO_BODY).getString());
    if (messageNode.hasProperty(Utils.EXO_SIZE)) msg.setSize(messageNode.getProperty(Utils.EXO_SIZE).getLong());
    if (messageNode.hasProperty(Utils.EXO_STAR)) msg.setHasStar(messageNode.getProperty(Utils.EXO_STAR).getBoolean());
    if (messageNode.hasProperty(Utils.EXO_PRIORITY)) msg.setPriority(messageNode.getProperty(Utils.EXO_PRIORITY).getLong());
    if (messageNode.hasProperty(Utils.EXO_ISUNREAD)) msg.setUnread(messageNode.getProperty(Utils.EXO_ISUNREAD).getBoolean());
    if (messageNode.hasProperty(Utils.EXO_CONTENT_TYPE)) msg.setContentType(messageNode.getProperty(Utils.EXO_CONTENT_TYPE).getString());
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
        file.setSize(node.getNode(Utils.JCR_CONTENT).getProperty(Utils.JCR_DATA).getLength());
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
    
    if (Utils.SHOWCONVERSATION) {
      List<String> referedMessageIds = new ArrayList<String>() ;
      PropertyIterator prosIter = messageNode.getReferences() ;
      Node msgNode ;
      while (prosIter.hasNext()) {
        msgNode = prosIter.nextProperty().getParent() ;
        if (msgNode.isNodeType("exo:message")) referedMessageIds.add(msgNode.getProperty(Utils.EXO_ID).getString()) ;
      }
      msg.setReferedMessageIds(referedMessageIds);

      List<String> groupedMessageIds = new ArrayList<String>() ;
      groupedMessageIds = getGroupedMessageIds(groupedMessageIds, messageNode, msg.getFolders()[0]) ;
      msg.setGroupedMessageIds(groupedMessageIds);
    }
    
    return msg ;
  }
  
  private List<String> getGroupedMessageIds(List<String> list, Node currentNode, String folderId) throws Exception {
    PropertyIterator prosIter = currentNode.getReferences() ;
    Node msgNode ;
    String accId = currentNode.getProperty(Utils.EXO_ACCOUNT).getString() ;
    while (prosIter.hasNext()) {
      msgNode = prosIter.nextProperty().getParent() ;
      if (msgNode.isNodeType("exo:message")) {
        String sentFolderId = Utils.createFolderId(accId, Utils.FD_SENT, false) ;
        String draftFolderId = Utils.createFolderId(accId, Utils.FD_DRAFTS, false) ;
        String msgFolderId = msgNode.getProperty(Utils.EXO_FOLDERS).getValues()[0].getString() ;
        if (folderId.equals(msgFolderId) || sentFolderId.equals(msgFolderId) || draftFolderId.equals(msgFolderId)) {
          list.add(msgNode.getProperty(Utils.EXO_ID).getString()) ;
          if (msgNode.isNodeType("mix:referenceable")) {
            list = getGroupedMessageIds(list, msgNode, folderId) ;
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
