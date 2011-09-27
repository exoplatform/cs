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
import java.util.LinkedHashMap;
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
import org.exoplatform.services.jcr.impl.core.query.QueryImpl;
import org.exoplatform.services.jcr.impl.core.query.lucene.QueryResultImpl;

/**
 * @author Phung Nam (phunghainam@gmail.com) 
 * @since July 25, 2007
 */
public class MessagePageList extends JCRPageList {

  private NodeIterator iter_ = null;

  private String       value_;

  private boolean      hasStructure_;

  public MessagePageList(long pageSize, String value, boolean hasStructure) throws Exception {
    super(pageSize);
    value_ = value;
    hasStructure_ = hasStructure;
  }

  protected void populateCurrentPage(long page, String username) throws Exception {
    long pageSize = getPageSize();
    long totalPage = 0;
    Node currentNode;
    Session session = getJCRSession(username);
    QueryImpl queryImpl = createXPathQuery(session, username, value_);
    if (page > 1){
      queryImpl.setOffset((page - 1) * pageSize);
    }
    queryImpl.setLimit(pageSize);
    QueryResult result = queryImpl.execute();
    iter_ = result.getNodes();
    totalPage = ((QueryResultImpl) result).getTotalSize();
    setAvailablePage(totalPage);

    currentListPage_ = new LinkedHashMap<String, Message>();
    int i = 0;
    boolean existRefNode = false;
    String accId, sentFolderId, curMsgFolder;
    Value[] folders, values;
    String[] refFolders;

    while (i < pageSize) {
      if (iter_.hasNext()) {
        currentNode = iter_.nextNode();
        values = new Value[] {};
        existRefNode = false;
        folders = currentNode.getProperty(Utils.MSG_FOLDERS).getValues();
        accId = currentNode.getProperty(Utils.EXO_ACCOUNT).getString();
        sentFolderId = Utils.generateFID(accId, Utils.FD_SENT, false);
        refFolders = new String[] { sentFolderId };
        curMsgFolder = "";

        if (folders != null && folders.length > 0) {
          curMsgFolder = currentNode.getProperty(Utils.MSG_FOLDERS).getValues()[0].getString();
          refFolders = new String[] { sentFolderId, curMsgFolder };
        }
        if (hasStructure_) {
          try {
            values = currentNode.getProperty(Utils.EXO_CONVERSATIONID).getValues();
            for (int j = 0; j < values.length; j++) {
              String uuid = values[j].getString();
              Node refNode = currentNode.getSession().getNodeByUUID(uuid);
              String refMsgFolder = refNode.getProperty(Utils.MSG_FOLDERS).getValues()[0].getString();

              if (refMsgFolder.equals(curMsgFolder))
                existRefNode = true;
              else if (refMsgFolder.equals(sentFolderId)) {
                existRefNode = true;
                Message refMsg = getMessage(refNode, refFolders);
                if (refMsg.getFolders() != null && refMsg.getFolders().length > 0) {
                  currentListPage_.put(refMsg.getId(), refMsg);
                  i++;
                  currentListPage_ = getMessageList(currentListPage_, refNode, curMsgFolder, refFolders);
                }
              }
            }
          } catch (Exception e) {
          }

          if (!currentNode.isNodeType("exo:messageMixin") || !existRefNode) {
            Message msg = getMessage(currentNode, refFolders);
            if (msg.getFolders() != null && msg.getFolders().length > 0) {
              currentListPage_.put(msg.getId(), msg);
              i++;
              currentListPage_ = getMessageList(currentListPage_, currentNode, curMsgFolder, refFolders);
            }
          }
        } else {
          Message msg = getMessage(currentNode, null);
          if (msg.getFolders() != null && msg.getFolders().length > 0) {
            currentListPage_.put(msg.getId(), msg);
            i++;
          }
        }
      } else {
        break;
      }
    }
    iter_ = null;
  }

  private LinkedHashMap<String, Message> getMessageList(LinkedHashMap<String, Message> listPage, Node currentNode, String folderId, String[] refFolders) throws Exception {
    PropertyIterator prosIter = currentNode.getReferences();
    String accId = currentNode.getProperty(Utils.EXO_ACCOUNT).getString();
    String sentFolderId = Utils.generateFID(accId, Utils.FD_SENT, false);
    Node msgNode;
    while (prosIter.hasNext()) {
      msgNode = prosIter.nextProperty().getParent();
      if (msgNode.isNodeType("exo:message")) {
        try {
          String msgFolder = msgNode.getProperty(Utils.MSG_FOLDERS).getValues()[0].getString();
          if (folderId.equals(msgFolder) || sentFolderId.equals(msgFolder)) {
            Message msg = getMessage(msgNode, refFolders);
            msg.setIsRootConversation(false);
            if (msg.getFolders() != null && msg.getFolders().length > 0) {
              listPage.put(msg.getId(), msg);
              if (msgNode.isNodeType("mix:referenceable"))
                listPage = getMessageList(listPage, msgNode, folderId, refFolders);
            }
          }
        } catch (Exception e) {
        }
      }
    }
    return listPage;
  }

  /**
   * 
   * @param messageNode
   * @param refFolders
   * @return message object, that is new one or have some properties that have to update in this page.
   * @throws Exception
   */
  private Message getMessage(Node messageNode, String[] refFolders) throws Exception {
    Message msg = Utils.getMessage(messageNode);
    if (hasStructure_) {
      if (refFolders == null)
        refFolders = new String[] { msg.getFolders()[0] };
      List<String> referedMessageIds = getReferedMessageIds(messageNode, refFolders);
      msg.setReferedMessageIds(referedMessageIds);

      // update refMessageIds in refered message if this message also contains child of current message
      Value[] values = {};
      try {
        values = messageNode.getProperty("exo:conversationId").getValues();
        for (int j = 0; j < values.length; j++) {
          Value value = values[j];
          String uuid = value.getString();
          Node refNode = messageNode.getSession().getNodeByUUID(uuid);
          Message refMsg = currentListPage_.get(refNode.getName());
          List<String> sibling = refMsg.getReferedMessageIds();
          sibling.removeAll(referedMessageIds);
          if (refMsg != null) {
            refMsg.setReferedMessageIds(sibling);
            if (refMsg.getFolders() != null && refMsg.getFolders().length > 0)
              currentListPage_.put(refNode.getName(), refMsg);
          }
        }
      } catch (Exception e) {
      }

      List<String> groupedMessageIds = new ArrayList<String>();
      groupedMessageIds = getGroupedMessageIds(groupedMessageIds, messageNode, refFolders);
      msg.setGroupedMessageIds(groupedMessageIds);
    }

    return msg;
  }

  public List<String> getReferedMessageIds(Node node, String[] refFolders) throws Exception {
    List<String> referedMessageIds = new ArrayList<String>();
    PropertyIterator prosIter = node.getReferences();
    Node msgNode;
    String id;
    while (prosIter.hasNext()) {
      msgNode = prosIter.nextProperty().getParent();
      for (int i = 0; i < refFolders.length; i++) {
        try {
          if (refFolders[i].equals(msgNode.getProperty(Utils.MSG_FOLDERS).getValues()[0].getString())) {
            id = msgNode.getProperty(Utils.EXO_ID).getString();
            if (!referedMessageIds.contains(id))
              referedMessageIds.add(id);
            break;
          }
        } catch (Exception e) {
        }
      }
    }
    return referedMessageIds;
  }

  private List<String> getGroupedMessageIds(List<String> list, Node currentNode, String[] refFolders) throws Exception {
    PropertyIterator prosIter = currentNode.getReferences();
    Node msgNode;
    String msgFolderId, msgNodeId;
    while (prosIter.hasNext()) {
      msgNode = prosIter.nextProperty().getParent();
      if (msgNode.isNodeType("exo:message")) {
        try {
          msgFolderId = msgNode.getProperty(Utils.MSG_FOLDERS).getValues()[0].getString();
          msgNodeId = msgNode.getProperty(Utils.EXO_ID).getString();
          for (int i = 0; i < refFolders.length; i++) {
            if (refFolders[i].equals(msgFolderId)) {
              if (!list.contains(msgNodeId))
                list.add(msgNodeId);
              if (msgNode.isNodeType("mix:referenceable")) {
                list = getGroupedMessageIds(list, msgNode, refFolders);
              }
              break;
            }
          }
        } catch (Exception e) {
        }
      }
    }
    return list;
  }

  @Override
  public List<Message> getAll() throws Exception {
    return null;
  }

  public List<Message> getAll(String username) throws Exception {
    List<Message> messageList = new ArrayList<Message>();
    for (int i = 1; i <= getAvailablePage(); i++) {
      messageList.addAll(getPage(i, username));
    }
    return messageList;
  }

  @SuppressWarnings("deprecation")
  private Session getJCRSession(String username) throws Exception {
    RepositoryService repositoryService = (RepositoryService) PortalContainer.getComponent(RepositoryService.class);
    SessionProvider sessionProvider = Utils.createSystemProvider();
    String defaultWS = repositoryService.getCurrentRepository().getConfiguration().getDefaultWorkspaceName();
    return sessionProvider.getSession(defaultWS, repositoryService.getCurrentRepository());
  }

  private QueryImpl createXPathQuery(Session session, String username, String xpath) throws Exception {
    QueryManager queryManager = session.getWorkspace().getQueryManager();
    return (QueryImpl) queryManager.createQuery(xpath, Query.XPATH);
  }
}
