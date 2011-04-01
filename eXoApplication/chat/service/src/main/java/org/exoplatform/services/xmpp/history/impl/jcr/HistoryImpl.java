/**
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
package org.exoplatform.services.xmpp.history.impl.jcr;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.commons.utils.ISO8601;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.xmpp.history.HistoricalMessage;
import org.exoplatform.services.xmpp.history.Interlocutor;
import org.exoplatform.services.xmpp.util.CodingUtils;
import org.jcrom.Jcrom;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.util.StringUtils;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public class HistoryImpl implements Startable{

  /**
   * 
   */
  private static final String CONVERSATIONS         = "conversations".intern();

  /**
   * 
   */
  private static final String PARTICIPANTS          = "participants".intern();

  /**
   * 
   */
  private static final String PARTICIPANTS_NT       = "lr:participantschat".intern();

  /**
   * 
   */
  private static final String CONVERSATIONS_NT      = "lr:conversations".intern();

  /**
   * 
   */
  private static final String HISTORICAL_MESSAGE_NT = "lr:historicalmessage".intern();

  /**
   * 
   */
  private static final String LR_MESSAGE_DATESEND   = "lr:messagedateSend";

  /**
   * 
   */
  private static final String LR_MESSAGE_TO         = "lr:messageto";

  /**
   * 
   */
  private static final String LR_MESSAGE_RECIEVE    = "lr:messagereceive";

  /**
   * 
   */
  private static final String HISTORY_NT            = "lr:chathistory";

  /**
   * 
   */
  private static final String CONFIG_NAME           = "history-conf";

  /**
   * 
   */
  private static final String APPLICATION           = "eXoChat";

  /**
   * 
   */
  private static final String DEFAULTPRESENCESTATUS_NT             = "lr:defaultpresencestatus".intern();
  
  private static final String DEFAULTPRESENCESTATUS                = "defaultpresencestatus".intern();
  
  /**
   * 
   * */
  private String              historyPath;

  /**
   * 
   */
  private String              wsName;

  /**
   * 
   */
  private RepositoryService   repositoryService;

  public RepositoryService getRepositoryService() {
    return repositoryService;
  }

  /**
   * 
   */
  private Jcrom               jcrom;
  
  /**
   * Queue that holds the messages to log.
   */
  private Queue<HistoricalMessage > logQueue = new ConcurrentLinkedQueue<HistoricalMessage >();
  
  Log log = ExoLogger.getExoLogger(this.getClass());
  
  public void start() {
    try{
      Session sysSession = this.repositoryService.getCurrentRepository().getSystemSession(wsName);
      initNodes(sysSession);
      jcrom = new Jcrom();
      jcrom.map(HistoricalMessageImpl.class);
      jcrom.map(Conversation.class);
      jcrom.map(InterlocutorImpl.class);
      jcrom.map(Participant.class);
      
      jcrom.map(PresenceStatus.class);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
    
  
  
  public void stop() {
    logAllMessages();
  }

  /**
   * @param initParams the initParams
   * @param registryService the registryService 
   */
  public HistoryImpl(InitParams initParams, RepositoryService repositoryService) {
    this.repositoryService = repositoryService;
    historyPath = initParams.getValueParam("path").getValue();
    wsName = initParams.getValueParam("workspace").getValue();
    
    if(jcrom == null) jcrom = new Jcrom();
  }
  
  /**
   * @param sysSession the session
   * @throws RepositoryException
   * @throws RepositoryConfigurationException
   */
  private void initNodes(Session sysSession) throws RepositoryException,
                                            RepositoryConfigurationException {
    Node node = sysSession.getRootNode();
    if (!node.hasNode(historyPath)) {
      Node tmpNode;
      String[] path = historyPath.split("/");
      for (int i = 0; i < path.length - 1; i++) {
        if (node.hasNode(path[i]))
          tmpNode = node.getNode(path[i]);
        else
          tmpNode = node.addNode(path[i]);
        node = tmpNode;
      }
      Node fNode = node.addNode(path[path.length - 1], HISTORY_NT);
      NodeImpl cNode = (NodeImpl) fNode.addNode(CONVERSATIONS, CONVERSATIONS_NT);
      if (cNode.canAddMixin("exo:privilegeable")) {
        cNode.addMixin("exo:privilegeable");
      }
      String[] perm = { PermissionType.ADD_NODE, PermissionType.READ, PermissionType.SET_PROPERTY,
          PermissionType.REMOVE };
      cNode.setPermission(SystemIdentity.ANY, PermissionType.ALL);
      NodeImpl pNode = (NodeImpl) fNode.addNode(PARTICIPANTS, PARTICIPANTS_NT);
      if (pNode.canAddMixin("exo:privilegeable")) {
        pNode.addMixin("exo:privilegeable");
      }
    
      pNode.setPermission(SystemIdentity.ANY, PermissionType.ALL);
      
      //defaul presence status *** initialize [lr:defaultpresencestatus] node type 
      NodeImpl dps = (NodeImpl) fNode.addNode(DEFAULTPRESENCESTATUS, DEFAULTPRESENCESTATUS_NT);
      if (dps.canAddMixin("exo:privilegeable")) {
        dps.addMixin("exo:privilegeable");
      }
      dps.setPermission(SystemIdentity.ANY, PermissionType.ALL);

      sysSession.save();
    }
    
    sysSession.logout();
  }
  
  public Queue<HistoricalMessage > getLogQueue() {
    return logQueue;
  }

  public void logMessage(HistoricalMessage message) {
    // Only log messages that have a body. Otherwise ignore it.
    if (message.getBody() != null) {
        logQueue.add(message);
    }
  }
  
  /**
   * Logs all the remaining message log entries to the database. Use this method to force
   * saving all the message log entries before the service becomes unavailable.
   */
  private void logAllMessages() {
	  HistoricalMessage message;
      SessionProvider provider = SessionProvider.createSystemProvider();
      while (!logQueue.isEmpty()) {
        message = logQueue.poll();
          if (message != null) {
              this.addHistoricalMessage(message, provider);
          }
      }
      provider.close();
  }
  
  /**
   * @return
   * @throws RepositoryException
   * @throws RepositoryConfigurationException
   */
  public ManageableRepository getRepository() throws RepositoryException,RepositoryConfigurationException{
    return repositoryService.getCurrentRepository();
  }
  
  /**
   * @return
   */
  public String getWorkspace(){
    return wsName; 
  }
  
  
  /**
   * @param message the message add to history 
   * @param sessionProvider the session provider
   */
  public boolean addHistoricalMessage(HistoricalMessage message, SessionProvider sessionProvider) {
    if (message.getTo() != null && message.getFrom() != null) {
      try {
        HistoricalMessageImpl historicalMessage = (HistoricalMessageImpl) message;
        Date date = Calendar.getInstance().getTime();
        String usernameTo = StringUtils.parseName(historicalMessage.getTo());
        String usernameFrom = StringUtils.parseName(historicalMessage.getFrom());
        String conversationId = new String();
        Boolean isGroupChat = historicalMessage.getType().equals(Message.Type.groupchat.name());
        Node conversationNode = getConversationsNode(sessionProvider);
        Node participantsNode = getParticipantsNode(sessionProvider);
        Conversation conversation = getConversation(conversationNode,
                                                    participantsNode,
                                                    usernameTo,
                                                    usernameFrom,
                                                    isGroupChat,
                                                    true);
        if (conversation != null) {
          conversation.addMessage(historicalMessage);
          conversation.setLastActiveDate(date);
          updateConversation(conversationNode, conversation, usernameTo);
        } else {
          conversationId = CodingUtils.encodeToHex(UUID.randomUUID().toString());
          createNewConversation(conversationNode,
                                participantsNode,
                                conversationId,
                                date,
                                historicalMessage);
        }
        conversationNode.getSession().save();
      } catch (Exception e) {
        //TODO: find why exception happens
        //e.printStackTrace();
        return false;
      }
    }
    return true;
  }

  
  /**
   * @param usernameTo the receiver username
   * @param usernameFrom the sender username
   * @param isGroupChat is group chat
   * @param sessionProvider the session provider
   * @return the list of message 
   */
  public List<HistoricalMessage> getHistoricalMessages(String usernameTo,
                                                       String usernameFrom,
                                                       Boolean isGroupChat,
                                                       SessionProvider sessionProvider) {
    List<HistoricalMessage> list = new ArrayList<HistoricalMessage>();
    try {
      Node conversationsNode = getConversationsNode(sessionProvider);
      Node participantsNode = getParticipantsNode(sessionProvider);
      Conversation conversation = getConversation(conversationsNode,
                                                  participantsNode,
                                                  usernameTo,
                                                  usernameFrom,
                                                  isGroupChat,
                                                  true);
      if (conversation != null) {
        list.addAll(conversation.getMessageList());
        //Merge new messages from cache
        list.addAll(getHistoricalMessagesFromCache(usernameTo, usernameFrom, null, null));
        return list;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return list;
  }

  
  /**
   * @param usernameTo the receiver username
   * @param usernameFrom the sender username
   * @param isGroupChat is group chat
   * @param dateFrom the date from
   * @param sessionProvider the session provider
   * @return the list of message 
   */
  public List<HistoricalMessage> getHistoricalMessages(String usernameTo,
                                                       String usernameFrom,
                                                       Boolean isGroupChat,
                                                       Date dateFrom,
                                                       SessionProvider sessionProvider) {
    List<HistoricalMessage> list = new ArrayList<HistoricalMessage>();
    try {
      Node conversationsNode = getConversationsNode(sessionProvider);
      Node participantsNode = getParticipantsNode(sessionProvider);
      Conversation conversation = getConversation(conversationsNode,
                                                  participantsNode,
                                                  usernameTo,
                                                  usernameFrom,
                                                  isGroupChat,
                                                  true);
      if (conversation != null) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateFrom);
        String dateFromStr = ISO8601.format(calendar);
        Node node = conversationsNode.getNode(conversation.getConversationId());
        String sql = "SELECT * FROM " + HISTORICAL_MESSAGE_NT + " WHERE jcr:path LIKE '"
            + node.getPath() + "/%' " + " AND " + LR_MESSAGE_DATESEND + " > TIMESTAMP '"
            + dateFromStr + "' order by " + LR_MESSAGE_DATESEND;
        QueryManager queryManager = node.getSession().getWorkspace().getQueryManager();
        Query query = queryManager.createQuery(sql, Query.SQL);
        QueryResult queryResult = query.execute();
        NodeIterator nodeIterator = queryResult.getNodes();
        while (nodeIterator.hasNext()) {
          Node msgNode = (Node) nodeIterator.next();
          HistoricalMessageImpl message = jcrom.fromNode(HistoricalMessageImpl.class, msgNode);
          list.add(message);
        }
        conversationsNode.getSession().save();
        //Merge new messages from cache
        list.addAll(getHistoricalMessagesFromCache(usernameTo, usernameFrom, dateFrom, null));
        return list;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return list;
  }

  /**
   * @param usernameTo the receiver username
   * @param usernameFrom the sender username
   * @param isGroupChat is group chat
   * @param dateFrom the date from
   * @param dateTo the date to
   * @param sessionProvider the session provider
   * @return the list of message 
   * @return
   */
  public List<HistoricalMessage> getHistoricalMessages(String usernameTo,
                                                       String usernameFrom,
                                                       Boolean isGroupChat,
                                                       Date dateFrom,
                                                       Date dateTo,
                                                       SessionProvider sessionProvider) {
    List<HistoricalMessage> list = new ArrayList<HistoricalMessage>();
    try {
      Node conversationsNode = getConversationsNode(sessionProvider);
      Node participantsNode = getParticipantsNode(sessionProvider);
      Conversation conversation = getConversation(conversationsNode,
                                                  participantsNode,
                                                  usernameTo,
                                                  usernameFrom,
                                                  isGroupChat,
                                                  false);
      if (conversation != null) {
        Node node = conversationsNode.getNode(conversation.getConversationId());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateFrom);
        String dateFromStr = ISO8601.format(calendar);
        calendar.setTime(dateTo);
        String dateToStr = ISO8601.format(calendar);
        String sql = "SELECT * FROM " + HISTORICAL_MESSAGE_NT + " WHERE jcr:path LIKE '"
            + node.getPath() + "/%' " + " AND " + LR_MESSAGE_DATESEND + " BETWEEN TIMESTAMP '"
            + dateFromStr + "' AND TIMESTAMP '" + dateToStr + "'";
        QueryManager queryManager = node.getSession().getWorkspace().getQueryManager();
        Query query = queryManager.createQuery(sql, Query.SQL);
        QueryResult queryResult = query.execute();
        NodeIterator nodeIterator = queryResult.getNodes();
        while (nodeIterator.hasNext()) {
          Node msgNode = (Node) nodeIterator.next();
          list.add(jcrom.fromNode(HistoricalMessageImpl.class, msgNode));
        }
        //Merge new messages from cache
        list.addAll(getHistoricalMessagesFromCache(usernameTo, usernameFrom, dateFrom, dateTo));
        return list;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return list;
  }


  /**
   * @param participantName the name
   * @param sessionProvider the session provider
   * @return the list of interlocutors
   */
  public List<Interlocutor> getInterlocutors(String participantName, SessionProvider sessionProvider) {
    List<Interlocutor> list = new ArrayList<Interlocutor>();
    try {
      Node participantsNode = getParticipantsNode(sessionProvider);
      Participant participant = getParticipant(participantsNode, CodingUtils.encodeToHex(participantName));
      if (participant != null) {
        if (participant.getInterlocutorList() != null) {
          list.addAll(participant.getInterlocutorList());
        }
        if (participant.getGroupChatList() != null) {
          list.addAll(participant.getGroupChatList());
        }
        return list;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return list;
  }

  /**
   * @param messageId the id
   * @param sessionProvider the session provider
   */
  public void messageReceive(String messageId, SessionProvider sessionProvider) {
    try {
      Node node = getConversationsNode(sessionProvider);
      String sql = "SELECT * FROM " + HISTORICAL_MESSAGE_NT + " WHERE jcr:path LIKE '"
          + node.getPath() + "/%/" + messageId + "'";
      QueryManager queryManager = node.getSession().getWorkspace().getQueryManager();
      Query query = queryManager.createQuery(sql, Query.SQL);
      QueryResult queryResult = query.execute();
      NodeIterator nodeIterator = queryResult.getNodes();
      while (nodeIterator.hasNext()) {
        Node nodeMsg = (Node) nodeIterator.next();
        HistoricalMessageImpl msg = jcrom.fromNode(HistoricalMessageImpl.class, nodeMsg);
        msg.setReceive(true);
        jcrom.updateNode(nodeMsg, msg);
      }
      node.getSession().save();
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  /**
   * @param usernameTo the username 
   * @param sessionProvider the session provider
   * @return the list of message
   */
  public List<HistoricalMessage> getNotReciveMessage(String usernameTo,
                                                     SessionProvider sessionProvider) {
    List<HistoricalMessage> list = new ArrayList<HistoricalMessage>();
    try {
      Node node = getConversationsNode(sessionProvider);
      String sql = "SELECT * FROM " + HISTORICAL_MESSAGE_NT + " WHERE jcr:path LIKE '"
          + node.getPath() + "/%' " + " AND " + LR_MESSAGE_TO + " LIKE '" + usernameTo + "%' AND "
          + LR_MESSAGE_RECIEVE + " = 'false'";
      QueryManager queryManager = node.getSession().getWorkspace().getQueryManager();
      Query query = queryManager.createQuery(sql, Query.SQL);
      QueryResult queryResult = query.execute();
      NodeIterator nodeIterator = queryResult.getNodes();
      while (nodeIterator.hasNext()) {
        Node nodeMsg = (Node) nodeIterator.next();
        HistoricalMessageImpl msg = jcrom.fromNode(HistoricalMessageImpl.class, nodeMsg);
        list.add(msg);
      }
      return list;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return list;
  }
  
  private List<HistoricalMessage> getHistoricalMessagesFromCache(String usernameTo, String usernameFrom, Date dateFrom, Date dateTo){
	List<HistoricalMessage> list = new ArrayList<HistoricalMessage>();
	for(HistoricalMessage msg : logQueue){
      if(msg.getTo() != null && msg.getFrom() != null){
        String to = StringUtils.parseName(msg.getTo());
        String from = StringUtils.parseName(msg.getFrom());
        Date sendDate = msg.getDateSend();
        if(to.equals(usernameTo) && from.equals(usernameFrom) || from.equals(usernameTo) && to.equals(usernameFrom))
          if((dateFrom == null && dateTo == null) || (dateFrom != null && dateTo == null && sendDate.after(dateFrom)) || 
        	  (dateFrom != null && dateTo != null && sendDate.after(dateFrom) && sendDate.before(dateTo))) 
            list.add(msg);
      }
    }
	return list;
  }

  /**
   * Load object from repository.
   * 
   * @param participantsNode the node
   * @param jid the username
   * @return the participant
   */
  private Participant getParticipant(Node participantsNode, String hexName) {
    try {
      if (participantsNode.hasNode(hexName)) {
        Node node = participantsNode.getNode(hexName);
        Participant participant = jcrom.fromNode(Participant.class, node);
        return participant;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Update object in the repository.
   * 
   * @param participantsNode the node
   * @param participant the participant
   */
  private void updateParticipant(Node participantsNode, Participant participant) {
    try {
      String hexName = CodingUtils.encodeToHex(participant.getUsername());
      if (participantsNode.hasNode(hexName)) {
        Node node = participantsNode.getNode(hexName);
        jcrom.updateNode(node, participant);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Load object Conversation from repository if withMessages is true loading
   * with message (with child nodes) else without.
   * 
   * @param convesationNode the node
   * @param participantsNode the node
   * @param usernameTo the receiver username
   * @param usernameFrom the sender username
   * @param isRoom is group chat
   * @param withMessages return with messge or not
   * @return the converstion
   */
  private Conversation getConversation(Node convesationNode,
                                       Node participantsNode,
                                       String usernameTo,
                                       String usernameFrom,
                                       Boolean isRoom,
                                       Boolean withMessages) {
    try {
      Participant participant = getParticipant(participantsNode, CodingUtils.encodeToHex(usernameTo));
      String conversationId = null;
      if (participant != null) {
        InterlocutorImpl interlocutor;
        if (!isRoom)
          interlocutor = participant.getInterlocutor(usernameFrom);
        else
          interlocutor = participant.getGroupChat(usernameFrom);
        if (interlocutor != null) {
          conversationId = interlocutor.getConversationId();
          if (convesationNode.hasNode(conversationId)) {
            Node node = convesationNode.getNode(conversationId);
            Conversation conversation = new Conversation();
            if (withMessages) {
              conversation = jcrom.fromNode(Conversation.class, node);
            } else
              conversation = jcrom.fromNode(Conversation.class, node, "-messageList", -1);
            return conversation;
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Create new Conversation and accordingly Participant and Interlocutor.
   * 
   * @param conversationsNode the node
   * @param participantsNode the node
   * @param conversationId the id
   * @param date the date
   * @param message the message
   * @return the conversation
   */
  private Conversation createNewConversation(Node conversationsNode,
                                             Node participantsNode,
                                             String conversationId,
                                             Date date,
                                             HistoricalMessageImpl message) {
    String jidTo = message.getTo();
    String jidFrom = message.getFrom();
    Boolean isGroupChat = message.getType().equals(Message.Type.groupchat.name());
    Participant participantTo = getParticipant(participantsNode, CodingUtils.encodeToHex(StringUtils.parseName(jidTo)));
    InterlocutorImpl interlocutorFrom = new InterlocutorImpl(jidFrom, conversationId, isGroupChat);
    if (!isGroupChat) {
      if (participantTo != null) {
        if (participantTo.getInterlocutorList() != null) {
          participantTo.addInterlocutor(interlocutorFrom);
        } else {
          List<InterlocutorImpl> list = new ArrayList<InterlocutorImpl>();
          list.add(interlocutorFrom);
          participantTo.setInterlocutorList(list);
        }
        updateParticipant(participantsNode, participantTo);
      } else {
        List<InterlocutorImpl> list = new ArrayList<InterlocutorImpl>();
        list.add(interlocutorFrom);
        participantTo = new Participant(jidTo, list, new ArrayList<InterlocutorImpl>());
        addParticipant(participantsNode, participantTo);
      }
      Participant participantFrom = getParticipant(participantsNode, CodingUtils.encodeToHex(StringUtils.parseName(jidFrom)));
      InterlocutorImpl interlocutorTo = new InterlocutorImpl(jidTo, conversationId, isGroupChat);
      if (participantFrom != null) {
        if (participantFrom.getInterlocutorList() != null) {
          participantFrom.addInterlocutor(interlocutorTo);
        } else {
          List<InterlocutorImpl> list = new ArrayList<InterlocutorImpl>();
          list.add(interlocutorTo);
          participantFrom.setInterlocutorList(list);
        }
        updateParticipant(participantsNode, participantFrom);
      } else {
        List<InterlocutorImpl> list = new ArrayList<InterlocutorImpl>();
        list.add(interlocutorTo);
        participantFrom = new Participant(jidFrom, list, new ArrayList<InterlocutorImpl>());
        addParticipant(participantsNode, participantFrom);
      }
    } else {
      if (participantTo != null) {
        if (participantTo.getGroupChatList() != null) {
          participantTo.addGroupChat(interlocutorFrom);
        } else {
          List<InterlocutorImpl> list = new ArrayList<InterlocutorImpl>();
          list.add(interlocutorFrom);
          participantTo.setGroupChatList(list);
        }
        updateParticipant(participantsNode, participantTo);
      } else {
        List<InterlocutorImpl> list = new ArrayList<InterlocutorImpl>();
        list.add(interlocutorFrom);
        participantTo = new Participant(jidTo, new ArrayList<InterlocutorImpl>(), list);
        addParticipant(participantsNode, participantTo);
      }
    }
    Conversation conversation = new Conversation(conversationId, date, date);
    conversation.addMessage(message);
    addConversation(conversationsNode, conversation);
    return conversation;
  }

  /**
   * Put object into repository.
   * 
   * @param participantsNode the node
   * @param participant the participant
   */
  private void addParticipant(Node participantsNode, Participant participant) {
    try {
      jcrom.addNode(participantsNode, participant);
      participantsNode.getSession().save();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Put new conversation into repository.
   * 
   * @param conversationsNode the node
   * @param conversation the conversation
   */
  private void addConversation(Node conversationsNode, Conversation conversation) {
    try {
      jcrom.addNode(conversationsNode, conversation);
      conversationsNode.getSession().save();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Update conversation in repository.
   * 
   * @param conversationsNode the node
   * @param conversation the conversation
   * @param username the username
   * @throws Exception 
   */
  private void updateConversation(Node conversationsNode, Conversation conversation, String username) throws Exception {
    try {
      if (conversationsNode.hasNode(conversation.getConversationId())) {
        Node node = conversationsNode.getNode(conversation.getConversationId());
        jcrom.updateNode(node, conversation);
        node.getSession().save();
      }
    } catch (Exception e) {
        throw e;
      //e.printStackTrace();
    }
  }

  /**
   * @param sessionProvider the session provider
   * @return the node
   */
  private Node getConversationsNode(SessionProvider sessionProvider) {
    try {
      ManageableRepository repository = repositoryService.getCurrentRepository();
      Session session = repository.getSystemSession(wsName);
      return session.getRootNode().getNode(historyPath + "/" + CONVERSATIONS);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * @param sessionProvider the session provider
   * @return the node
   */
  private Node getParticipantsNode(SessionProvider sessionProvider) {
    try {
      ManageableRepository repository = repositoryService.getCurrentRepository();
      Session session = repository.getSystemSession(wsName);
      return session.getRootNode().getNode(historyPath + "/" + PARTICIPANTS);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
  
  /**
   * Saving user chat status
   * @param provider
   * @param userId
   * @param status
   * **/
  public void savePresenceStatus(SessionProvider provider, String userId, String status) throws Exception{
    Node dpsNode = getDefaultPresenceStatusNode(provider);
    String hexName = CodingUtils.encodeToHex(userId);
    PresenceStatus presenceStatus = getPresenceStatus(dpsNode, hexName);
    
    if(dpsNode == null){ //add new lr:defaultpresencestatus\lr:presencestatus node
      try {
        ManageableRepository repository = repositoryService.getCurrentRepository();
        Session session = provider.getSession(wsName, repository);
        Node node = session.getRootNode();
        Node fNode = null;
        Node historyNode = node.getNode(historyPath);
        String[] path = historyPath.split("/");
        
        if (historyNode == null) {
          Node tmpNode;
          for (int i = 0; i < path.length - 1; i++) {
            if (node.hasNode(path[i]))
              tmpNode = node.getNode(path[i]);
            else
              tmpNode = node.addNode(path[i]);
            node = tmpNode;
          }
         fNode = node.addNode(path[path.length - 1], HISTORY_NT);
        }else{
          fNode = historyNode.getNode(path[path.length - 1]);
        }
        
        NodeImpl dps = (NodeImpl) fNode.addNode(DEFAULTPRESENCESTATUS, DEFAULTPRESENCESTATUS_NT);
        if (dps.canAddMixin("exo:privilegeable")) {
          dps.addMixin("exo:privilegeable");
        }
        dps.setPermission(SystemIdentity.ANY, PermissionType.ALL);
        presenceStatus = new PresenceStatus(userId, status);
        //presenceStatus.setStatus(status);
       // String presenceStatusPath = historyPath + "/" + DEFAULTPRESENCESTATUS;
      //  if(!dps.getPath().equals(presenceStatusPath)) presenceStatus.setPath(presenceStatusPath);
        
        addPresenceStatus(dps, presenceStatus);
        
        session.save();
        session.logout();
      } catch (Exception e) {
        log.error("Could not add a new node for [lr:defaultpresecestatus] node type: " + e.getMessage());
      }
    }else { //update lr:status property
      if(presenceStatus == null){
        presenceStatus = new PresenceStatus(userId, status);
        //presenceStatus.setStatus(status);
        addPresenceStatus(dpsNode, presenceStatus);
      }else{
        if(presenceStatus.getHexName().equals(hexName)){
          Node presenceStatusNode = null;
          presenceStatus.setStatus(status);
          try {
            if(dpsNode.hasNode(presenceStatus.getHexName())){
              presenceStatusNode = dpsNode.getNode(presenceStatus.getHexName());
              jcrom.updateNode(presenceStatusNode, presenceStatus);
              dpsNode.getSession().save();
            }
           } catch (Exception e) {
             log.error("Could not update [lr:presencestatus] node: " + e.getMessage());
          }
        }
      }
    }
  }

  /**
   * Getting user chat status 
   * */
  public String getPresenceStatusHistory(SessionProvider provider, String userId){
    Node dpsNode = getDefaultPresenceStatusNode(provider);
    String hexName = CodingUtils.encodeToHex(userId);
    PresenceStatus ps = getPresenceStatus(dpsNode, hexName);
    if(ps != null) {
      return ps.getStatus();
    }
    return null;
  }
  
  
  private void addPresenceStatus(Node dpsNode, PresenceStatus presenceStatus){
    try {
      jcrom.addNode(dpsNode, presenceStatus);
      dpsNode.getSession().save();
    } catch (Exception e) {
      log.error("Could not add new a node to [lr:presencestatus] node type: " + e.getMessage());
    }
  }
  /**
   * Getting presence status from Node 
   * */
  public PresenceStatus getPresenceStatus(Node dpsNode, String hexName){
    PresenceStatus presenceStatus = null;
    try {
      if(dpsNode.hasNode(hexName)){
        Node presenceStatusNode = dpsNode.getNode(hexName);
        presenceStatus  = jcrom.fromNode(PresenceStatus.class, presenceStatusNode);  
      }
    }catch (Exception e) {
      log.error("Getting a PresenceStatus fail: " + e.getMessage(), e.getCause());
    }
    
    return presenceStatus;
  }
  
  /**
   * Getting exist user chat status node 
   * */
  private Node getDefaultPresenceStatusNode(SessionProvider sessionProvider){
    Node defaultPresenceStatusNode = null;
    try {
      ManageableRepository repository = repositoryService.getCurrentRepository();
      Session session = sessionProvider.getSession(wsName, repository);
      Node root = session.getRootNode();
      defaultPresenceStatusNode = root.getNode(historyPath + "/" + DEFAULTPRESENCESTATUS) ;
    } catch (Exception e) {
      log.error("Default Presence status node is not exist:  " + e.getMessage());
    }    
    return defaultPresenceStatusNode;
  }
  
}
