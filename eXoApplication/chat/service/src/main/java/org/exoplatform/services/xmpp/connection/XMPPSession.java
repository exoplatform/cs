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
package org.exoplatform.services.xmpp.connection;

import java.util.Date;
import java.util.List;

import org.exoplatform.services.xmpp.bean.ConfigRoomBean;
import org.exoplatform.services.xmpp.bean.FormBean;
import org.exoplatform.services.xmpp.bean.FullRoomInfoBean;
import org.exoplatform.services.xmpp.ext.transport.Transport;
import org.exoplatform.services.xmpp.history.HistoricalMessage;
import org.exoplatform.services.xmpp.history.Interlocutor;
import org.exoplatform.services.xmpp.userinfo.UserInfo;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.ReportedData;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.RoomInfo;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public interface XMPPSession {

  /**
   * Check is <code>Transport</code> supported and add it to <code>List</code>
   * of available transports. User can use remote accounts on other IM servers.
   * (ICQ, Yahoo, etc).
   * 
   * @param transport the name of transport
   * @param remoteUser the username
   * @param remotePassword the password
   * @param autoLogin not working now
   * @return true if successfully
   */
  boolean addTransport(Transport transport,
                       String remoteUser,
                       String remotePassword,
                       boolean autoLogin);

  /**
   * Add new buddy to roster.
   * 
   * @param username the username
   * @param nickname the nickname
   * @param group the group
   * @throws XMPPException the XMPPException
   */
  void addBuddy(String username, String nickname, String group) throws XMPPException;

  /**
   * Ask for subscription to user with given name.
   *  
   * @param username the username
   * @param nickname the nickname
   * @throws XMPPException the XMPPException
   */
  void askForSubscription(String username, String nickname);

  /**
   * Return the roster entry by the name of user.
   * 
   * @param name the username
   * @return the rosterEntry
   */
  RosterEntry getBuddy(String name);

  /**
   * Update the buddy in roster.
   * 
   * @param username the useranme
   * @param nickname the nickanme
   * @param group the group
   * @throws XMPPException the XMPPException
   */
  void updateBuddy(String username, String nickname, String group) throws XMPPException;

  /**
   * Clean buddies list.
   * 
   * @return true if all user removed from the list of user false otherwise.
   */
  boolean cleanBuddiesList() throws XMPPException;

  /**
   * Close current connection and send notification to buddies.
   */
  void close();

  /**
   * Get list of <code>Presence</code> for all buddies for current user.
   * 
   * @return list of <code>Presence</code>.
   */
  List<Presence> getBuddiesPresense();

  /**
   * @return XMPPConnection.
   */
  XMPPConnection getConnection();

  /**
   * Create new group chat.
   * 
   * @param room the room name
   * @param nickname the nickname in this room
   * @return new MultiUserChat
   * @throws XMPPException the XMPPException
   */
  FormBean createRoom(String room, String nickname) throws XMPPException;

  /**
   * get configuration form for group chat.
   * 
   * @param room the room name
   * @return the configuration form of MultiUserChat 
   * @throws XMPPException the XMPPException
   */
  FormBean getConfigFormRoom(String room) throws XMPPException;

  /**
   * Configure the group chat.
   * 
   * @param room the room name
   * @param configRoom the configuration of group chat  
   * @return the MultiUserChat
   * @throws XMPPException the XMPPException
   */
  MultiUserChat configRoom(String room, ConfigRoomBean configRoom) throws XMPPException;

  /**
   * Join to the group chat.
   * 
   * @param room the room name
   * @param nickname the nickname
   * @param password the password
   * @throws XMPPException the XMPPException
   */
  void joinRoom(String room, String nickname, String password) throws XMPPException;

  /**
   * Destroy the group chat.
   * 
   * @param room the room name
   * @param reason the reason
   * @param altRoom the name of alter room
   * @return true if success
   * @throws XMPPException the XMPPException
   */
  Boolean destroyRoom(String room, String reason, String altRoom) throws XMPPException;

  /**
   * Get full information about room.
   * 
   * @param room the name of room
   * @return the fullRoomInfoBean
   * @throws XMPPException the XMPPException
   */
  FullRoomInfoBean getRoomInfoBean(String room) throws XMPPException;

  /**
   * Leave the room.
   * 
   * @param room the room name
   * @return true if successfully
   * @throws XMPPException the XMPPException
   */
  Boolean leaveRoom(String room) throws XMPPException;

  /**
   * Decline the invitation to the room.
   * 
   * @param room the room
   * @param inviter the inviter
   * @param reason the reason
   * @throws XMPPException the XMPPException
   */
  void declineRoom(String room, String inviter, String reason);

  /**
   * Invite to the room.
   * 
   * @param room the room name
   * @param invitee the invitee
   * @param reason the reason
   * @return true if successfully
   * @throws XMPPException the XMPPException
   */
  Boolean inviteToRoom(String room, String invitee, String reason) throws XMPPException;

  /**
   * The manage role of user in the room.
   * 
   * @param chat
   * @param nickname the nickname 
   * @param role the role 
   * @param command the command (grant or revoke)
   * @throws XMPPException the XMPPException
   */
  void manageRole(String room, String nickname, String role, String command) throws XMPPException;

  /**
   * @param chat
   * @param username the username
   * @param affiliation the affiliate
   * @param command the command (grant or revoke)
   * @throws XMPPException the XMPPException
   */
  void manageAffiliation(String room, String username, String affiliation, String command) throws XMPPException;

  /**
   * @param chat
   * @param nickname the nickname
   * @param reason the reason
   * @throws XMPPException the XMPPException
   */
  void kickUser(String room, String nickname, String reason) throws XMPPException;

  /**
   * @param chat
   * @param username the username
   * @param reason the reason
   * @throws XMPPException the XMPPException
   */
  void banUser(String room, String username, String reason) throws XMPPException;

  /**
   * Get form for searching users.
   * 
   * @param searchService the name of searchService 
   * @return the Form.
   */
  Form getSearchForm(String searchService) throws XMPPException;

  /**
   * @return the List of search services names.
   */
  List<String> getSearchServices();

  /**
   * Get the information about user by user id.
   * 
   * @param userID the user id
   * @return the userInfo
   */
  UserInfo getUserInfo(String userID);

  /**
   * Get the username.
   * 
   * @return the username;
   */
  String getUsername();

  /**
   * Remove all transport subscribed for current user.
   */
  void removeAllTransport() throws XMPPException;

  /**
   * Remove remote user from contacts list.
   * 
   * @param fullUserID the full ID of remote user.
   * @return true if user removed successful false otherwise.
   */
  boolean removeBuddy(String fullUserID) throws XMPPException;

  /**
   * Remove transport from available list. For user it minds close connection
   * with remote users which were available through this transport (ICQ, Yahoo,
   * etc).
   * 
   * @param transport the Transport.
   * @return true if successfully 
   */
  boolean removeTransport(Transport transport) throws XMPPException;

  /**
   * @param search the search string.
   * @param byUsername search in useranme
   * @param byName search in name
   * @param byEmail search in mail
   * @param searchService the SearchService name.
   * @return the list of users which match for searching string.
   */
  ReportedData searchUser(String search,
                          boolean byUsername,
                          boolean byName,
                          boolean byEmail,
                          String searchService) throws XMPPException;

  /**
   * Send message.
   * 
   * @param message the message
   */
  void sendMessage(Message message);

  /**
   * Send message to the room.
   * 
   * @param room the room name
   * @param body the body of message
   * @throws XMPPException the XMPPException
   */
  void sendMessageToMUC(String room, String body) throws XMPPException;

  /**
   * Send message to the room.
   * 
   * @param room the room name
   * @param body the body of message
   * @param chat message id
   * @throws XMPPException the XMPPException
   */
  void sendMessageToMUC(String room, String body, String id) throws XMPPException;
  
  /**
   * Change presence for current user.
   * 
   * @param presence the new <code>Presence</code>.
   */
  void sendPresence(Presence presence);

  /**
   * Subscribe remote user and send message to that user.
   * 
   * @param fullUserID the full ID of remote user.
   */
  void subscribeUser(String fullUserID);

  /**
   * Subscribe remote user and send message to that user.
   * 
   * @param forUser the remote user.
   * @param serviceName the service name.
   */
  void subscribeUser(String forUser, String serviceName);

  /**
   * Unsubscribe remote user and send message to that user.
   * 
   * @param fullUserID the full ID of remote user.
   */
  void unsubscribeUser(String fullUserID);

  /**
   * Unsubscribe remote user and send message to that user.
   * 
   * @param forUser the remote user.
   * @param serviceName the service name.
   */
  void unsubscribeUser(String forUser, String serviceName);

  /**
   * Get history.
   * 
   * @param usernameto the username who recive message
   * @param usernamefrom the username who send message
   * @param isGroupChat  is the group chat
   * @return list of historyMessages
   */
  List<HistoricalMessage> getAllHistory(String usernameto,
                                               String usernamefrom,
                                               boolean isGroupChat);

  /**
   * Get history between two date.
   * 
   * @param usernameto the username who recive message
   * @param usernamefrom the username who send message
   * @param isGroupChat  is the group chat
   * @param dateFrom the begin
   * @param dateTo the end
   * @return list of historyMessages
   */
  List<HistoricalMessage> getHistoryBetweenDate(String usernameto,
                                                       String usernamefrom,
                                                       boolean isGroupChat,
                                                       Date dateFrom,
                                                       Date dateTo);

  /**
   * Get history between the date and current time.
   * 
   * @param usernameto the username who recive message
   * @param usernamefrom the username who send message
   * @param isGroupChat  is the group chat
   * @param dateFrom the begin
   * @return list of historyMessages
   */
  List<HistoricalMessage> getHistoryFromDateToNow(String usernameto,
                                                         String usernamefrom,
                                                         boolean isGroupChat,
                                                         Date dateFrom);

  /**
   * Get all interlocutors of user.
   * 
   * @param username thr username
   * @return list 
   */
  List<Interlocutor> getInterlocutors(String username);

  /**
   * Set that message is received.
   * 
   * @param messageId the id of message
   */
  void messageReceive(String messageId);

  /**
   * Get all not received message.
   * 
   * @return list of not received message
   */
  List<HistoricalMessage> getNotRecieveMessages();

  /**
   * @param room the name of room
   * @return the MultiuserChat
   * @throws XMPPException the XMPPException
   */
  MultiUserChat getMultiUserChat(String room) throws XMPPException;

  /**
   * True if join in the room.
   * 
   * @param room the room name
   * @return true if joined to the room
   * @throws XMPPException the XMPPException
   */
  Boolean isJoin(String room) throws XMPPException;

  /**
   * Get already joined rooms.
   * 
   * @return the joined rooms
   */
  List<String> getJoinedRooms();

  /**
   * Change subject of the room.  
   * 
   * @param room the room name
   * @param newSubject the new subject
   * @throws XMPPException the XMPPException
   */
  void changeSubject(String room, String newSubject) throws XMPPException;

  /**
   * Change status in the room (e.g Extend Away).
   * 
   * @param room the room name
   * @param status the status
   * @param mode the mode
   * @throws XMPPException the XMPPException
   */
  void changeAvailabilityStatusInRoom(String room, String status, String mode) throws XMPPException;

  /**
   * Change nickname in the room.
   * 
   * @param room the room name
   * @param nickname the new nickname 
   * @throws XMPPException the XMPPException
   */
  void changeNickname(String room, String nickname) throws XMPPException;
  
  RoomInfo getRoomInfo(String room) throws XMPPException;

  void addFullUserNames(String userName, String fullUserName);
  
  
  public String getPresenceStatus_();
  
  public void setPresenceStatus_(String status);
  
}
