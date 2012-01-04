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
package org.exoplatform.services.xmpp.connection.impl;

import java.io.File;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.UUID;

import javax.jcr.RepositoryException;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.presence.DefaultPresenceStatus;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.uistate.UIStateSession;
import org.exoplatform.services.uistate.bean.UIStateDataBean;
import org.exoplatform.services.xmpp.bean.ChangeNickBean;
import org.exoplatform.services.xmpp.bean.ConfigRoomBean;
import org.exoplatform.services.xmpp.bean.ContactBean;
import org.exoplatform.services.xmpp.bean.DeclineBean;
import org.exoplatform.services.xmpp.bean.EventsBean;
import org.exoplatform.services.xmpp.bean.FileTransferEventBean;
import org.exoplatform.services.xmpp.bean.FileTransferRequestBean;
import org.exoplatform.services.xmpp.bean.FileTransferResponseBean;
import org.exoplatform.services.xmpp.bean.FormBean;
import org.exoplatform.services.xmpp.bean.FullRoomInfoBean;
import org.exoplatform.services.xmpp.bean.HostedRoomBean;
import org.exoplatform.services.xmpp.bean.InitInfoBean;
import org.exoplatform.services.xmpp.bean.InviteBean;
import org.exoplatform.services.xmpp.bean.KickedBannedBean;
import org.exoplatform.services.xmpp.bean.MUCPacketBean;
import org.exoplatform.services.xmpp.bean.MessageBean;
import org.exoplatform.services.xmpp.bean.OccupantBean;
import org.exoplatform.services.xmpp.bean.PresenceBean;
import org.exoplatform.services.xmpp.bean.PrivilegeChangeBean;
import org.exoplatform.services.xmpp.bean.SubjectChangeBean;
import org.exoplatform.services.xmpp.connection.XMPPSession;
import org.exoplatform.services.xmpp.ext.transport.Transport;
import org.exoplatform.services.xmpp.filter.ErrorMessageFilter;
import org.exoplatform.services.xmpp.filter.MessageFilter;
import org.exoplatform.services.xmpp.filter.SubscriptionFilter;
import org.exoplatform.services.xmpp.groupchat.MultiUserChatManager;
import org.exoplatform.services.xmpp.history.HistoricalMessage;
import org.exoplatform.services.xmpp.history.Interlocutor;
import org.exoplatform.services.xmpp.history.impl.jcr.HistoryImpl;
import org.exoplatform.services.xmpp.userinfo.UserInfo;
import org.exoplatform.services.xmpp.userinfo.UserInfoService;
import org.exoplatform.services.xmpp.util.CodingUtils;
import org.exoplatform.services.xmpp.util.CometdChannels;
import org.exoplatform.services.xmpp.util.HistoryUtils;
import org.exoplatform.services.xmpp.util.MUCConstants;
import org.exoplatform.services.xmpp.util.PresenceUtil;
import org.exoplatform.services.xmpp.util.SearchFormFields;
import org.exoplatform.services.xmpp.util.TransformUtils;
import org.exoplatform.services.xmpp.util.XMPPConnectionUtils;
import org.exoplatform.ws.frameworks.cometd.transport.ContinuationServiceDelegate;
import org.exoplatform.ws.frameworks.json.impl.JsonException;
import org.exoplatform.ws.frameworks.json.impl.JsonGeneratorImpl;
import org.exoplatform.ws.frameworks.json.value.JsonValue;
import org.jivesoftware.smack.PacketInterceptor;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.ReportedData;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.muc.Affiliate;
import org.jivesoftware.smackx.muc.DefaultParticipantStatusListener;
import org.jivesoftware.smackx.muc.DefaultUserStatusListener;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.InvitationRejectionListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.Occupant;
import org.jivesoftware.smackx.muc.RoomInfo;
import org.jivesoftware.smackx.muc.SubjectUpdatedListener;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.jivesoftware.smackx.packet.DiscoverInfo.Identity;
import org.jivesoftware.smackx.packet.DiscoverItems;
import org.jivesoftware.smackx.packet.DiscoverItems.Item;
import org.jivesoftware.smackx.packet.MUCUser;
import org.jivesoftware.smackx.packet.MUCUser.Invite;
import org.jivesoftware.smackx.search.UserSearchManager;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */

public class XMPPSessionImpl implements XMPPSession, UIStateSession {

  // public enum XMPPEvents {
  // NEW_MESSAGE, NEW_SUBCRIPTION, CHANGE_PRESENCE, CHANGE_ROSTER, MUC_EVENT
  // }
  /**
   * Current XMPPConnection.
   */
  private final XMPPConnection              connection_;

  /**
   * 
   */
  private final String                      username_;

  /**
   * 
   */
  private int                               contFileTransfers;

  /**
   * 
   */
  private MultiUserChatManager              multiUserChatManager;

  /**
   * 
   */
  private SessionProvider                   sessionProvider;

  /**
   * 
   */
  private final HistoryImpl                 history_;

  /**
   * 
   */
  private FileTransferManager               fileTransferManager;

  /**
   * Logger.
   */
  private final Log                         log                    = ExoLogger.getLogger("lr.XMPPSessionImpl");

  /**
   * 
   */
  private Map<String, FileTransferRequest>  fileTransferRequestMap = new HashMap<String, FileTransferRequest>();

  /**
   * 
   */
  private final UserInfoService             organization_;

  /**
   * 
   */
  private UIStateDataBean                   uiStateData_;

  /**
   * 
   */
  private final ContinuationServiceDelegate delegate_;

  /**
   * 
   */
  private final ResourceBundle              rb_;

  /**
   * 
   */
  private final static String               DESCENDING             = "desc";

  private final static String               ASCENDING              = "asc";

  private static ArrayList<OccupantBean>    beanList               = null;

  private final DateFormat                  dateFormat             = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");

  /**
   * It's value not from spec. I add it myself for sending real JID of room to UI client.
   */
  private final static String               ROOM_JID_VALUE         = "muc#roomconfig_roomjid";

  private String                            status_                = DefaultPresenceStatus.DEFAULT_STATUS;

  protected XMPPSessionImpl(String username, String password, final UserInfoService organization, final ContinuationServiceDelegate delegate, final HistoryImpl history, final ResourceBundle rb) throws XMPPException {
    this.username_ = username;
    username = encodeUserName(username);
    // 17/06/2010 add start
    if (beanList == null)
      beanList = new ArrayList<OccupantBean>();
    // 17/06/2010 add end

    XMPPConnection.DEBUG_ENABLED = true;
    this.delegate_ = delegate;
    this.history_ = history;
    this.organization_ = organization;
    this.uiStateData_ = new UIStateDataBean();
    this.rb_ = rb;
    this.connection_ = new XMPPConnection(XMPPMessenger.getConnectionConfiguration());
    try {
      connection_.connect();
      connection_.login(username, password, null);
      if (log.isDebugEnabled())
        log.info("Client '" + username + "' logged in.");
      multiUserChatManager = new MultiUserChatManager();
      fileTransferManager = new FileTransferManager(connection_);

      fileTransferManager.addFileTransferListener(new FileTransferListener() {
        public void fileTransferRequest(FileTransferRequest request) {
          try {
            String uuid = UUID.randomUUID().toString();
            fileTransferRequestMap.put(uuid, request);
            JsonGeneratorImpl generatorImpl = new JsonGeneratorImpl();
            FileTransferRequestBean requestBean = new FileTransferRequestBean(request, uuid);
            List<FileTransferRequestBean> list = new ArrayList<FileTransferRequestBean>();
            list.add(requestBean);
            FileTransferEventBean bean = new FileTransferEventBean();
            bean.setFileTransportRequests(list);
            EventsBean eventsBean = new EventsBean();
            eventsBean.addFileEvent(bean);
            eventsBean.setEventId(Packet.nextID());
            try {
              List<ContactBean> list2 = new ArrayList<ContactBean>();
              for (ContactBean b : eventsBean.getRoster()) {
                UserInfo info = getUserInfo(b.getUser().split("@")[0]);
                b.setFullName(info.getFirstName() + " " + info.getLastName());
                list2.add(b);
              }
              eventsBean.setRoster(list2);
            } catch (Exception e) {
            }

            JsonValue json = generatorImpl.createJsonObject(eventsBean);
            delegate_.sendMessage(username_, CometdChannels.FILE_EXCHANGE, json.toString(), null);
            if (log.isDebugEnabled())
              log.debug(json.toString());
          } catch (Exception e) {
            if (log.isDebugEnabled()) {
              log.debug("File transfer fail", e);
            }
          }
        }
      });
      MessageFilter msgFilter = new MessageFilter();
      connection_.addPacketListener(new PacketListener() {
        public void processPacket(Packet packet) {
          try {
            if (packet.getPacketID() == null)
              packet.setPacketID(CodingUtils.encodeToHex(UUID.randomUUID().toString()));
            HistoricalMessage historyMsg = HistoryUtils.messageToHistoricalMessage((Message) packet);
            MessageBean message = TransformUtils.messageToBean(historyMsg);
            if(Message.Type.groupchat.name().equals(message.getType())){
              message.setFrom(decodeUsername(message.getFrom()));
            }
            /*
             * history.addHistoricalMessage(HistoryUtils.messageToHistoricalMessage((Message) packet), sessionProvider);
             */
            // Fix for CS-3246: contact list in public room is empty in special case
            // Because persistence operations are massive -> much delay time -> bug ->to solve by using cache
            historyMsg.setFrom(decodeUsername(historyMsg.getFrom()));
            historyMsg.setTo(decodeUsername(historyMsg.getTo()));
            history_.logMessage(historyMsg);
            
            sendMessage(message);
          } catch (Exception e) {
            if (log.isDebugEnabled()) {
              log.debug("Send message fail", e);
            }
          }
        }
      }, msgFilter);

      SubscriptionFilter subFilter = new SubscriptionFilter();
      connection_.addPacketListener(new PacketListener() {
        public void processPacket(Packet packet) {
          try {
            JsonGeneratorImpl generatorImpl = new JsonGeneratorImpl();
            PresenceBean subscription = TransformUtils.presenceToBean((Presence) packet);
            EventsBean eventsBean = new EventsBean();
            UserInfo userInfo = getUserInfo(subscription.getFrom().split("@")[0]);
            subscription.setFromName(userInfo.getFirstName() + " " + userInfo.getLastName());
            eventsBean.addSubscription(subscription);
            if (subscription.getType().equals(Type.subscribed.name())) {
              Presence presence = connection_.getRoster().getPresence(packet.getFrom());
              eventsBean.addPresence(TransformUtils.presenceToBean(presence));
            }
            eventsBean.setEventId(Packet.nextID());
            try {
              List<ContactBean> list = new ArrayList<ContactBean>();
              for (ContactBean b : eventsBean.getRoster()) {
                UserInfo info = getUserInfo(b.getUser().split("@")[0]);
                b.setFullName(info.getFirstName() + " " + info.getLastName());
                list.add(b);
              }
              eventsBean.setRoster(list);
            } catch (Exception e) {
            }

            JsonValue json = generatorImpl.createJsonObject(eventsBean);
            delegate_.sendMessage(username_, CometdChannels.SUBSCRIPTION, json.toString(), null);
          } catch (Exception e) {
            if (log.isDebugEnabled()) {
              log.debug("Send subcription fail", e);
            }
          }
        }
      }, subFilter);

      MultiUserChat.addInvitationListener(connection_, new InvitationListener() {
        public void invitationReceived(XMPPConnection conn, String room, String inviter, String reason, String password, Message message) {
          try {
            MUCPacketBean bean = new MUCPacketBean();
            bean.setAction(MUCConstants.Action.INVITE);
            bean.setInvite(new InviteBean(inviter, TransformUtils.messageToBean(message), password, reason, room));
            bean.setIsRoomPasswordProtect(isPasswordRequired(room));
            bean.setRoom(room);
            sendGroupChatEvent(bean);
          } catch (Exception e) {
            if (log.isDebugEnabled()) {
              log.debug("Process the invitation fail", e);
            }
          }

        }
      });

      ErrorMessageFilter errorMessageFilter = new ErrorMessageFilter();
      connection_.addPacketListener(new PacketListener() {
        public void processPacket(Packet packet) {
          try {
            final Message message = (Message) packet;
            String errorMessage = "";
            if (message.getError().getCode() == 403 && message.getSubject() != null) {
              errorMessage = rb_.getString("chat.message.subject.change.error");
            } else if (message.getError().getCode() == 403) {
              errorMessage = rb_.getString("chat.message.forbidden.error");
              MUCUser packetExtension = (MUCUser) packet.getExtension("x", "http://jabber.org/protocol/muc#user");
              if (packetExtension != null) {
                Invite invite = packetExtension.getInvite();
                if (invite != null) {
                  errorMessage = rb_.getString("chat.message.room.invite.forbidden.error");
                  Object values[] = { invite.getTo(), message.getFrom() };
                  errorMessage = MessageFormat.format(errorMessage, values);
                }
              }
            }
            String id = CodingUtils.encodeToHex(UUID.randomUUID().toString());
            MessageBean messageBean = new MessageBean(id, message.getFrom(), message.getTo(), message.getType().name(), errorMessage);
            messageBean.setDateSend(Calendar.getInstance().getTime().toString());
            
            sendMessage(messageBean);
          } catch (Exception e) {
            if (log.isDebugEnabled()) {
              log.debug("Fail to process the error message package", e);
            }
          }
        }
      }, errorMessageFilter);

      // For CS-2908
      PacketFilter discoverInfoFilter = new PacketTypeFilter(DiscoverInfo.class);
      connection_.addPacketWriterInterceptor(new PacketInterceptor() {
        public void interceptPacket(Packet packet) {
          DiscoverInfo discoverInfo = (DiscoverInfo) packet;
          if (discoverInfo != null && discoverInfo.getType() == IQ.Type.RESULT) {
            Iterator<Identity> it = discoverInfo.getIdentities();
            if (it.hasNext()) {
              Identity id = it.next();
              if (id.getCategory().equalsIgnoreCase("client")) {
                if (!discoverInfo.containsFeature("http://jabber.org/protocol/si/profile/file-transfer")) {
                  discoverInfo.addFeature("http://jabber.org/protocol/xhtml-im");
                  discoverInfo.addFeature("http://jabber.org/protocol/muc");
                  discoverInfo.addFeature("http://jabber.org/protocol/si/profile/file-transfer");
                  discoverInfo.addFeature("http://jabber.org/protocol/si");
                  discoverInfo.addFeature("http://jabber.org/protocol/bytestreams");
                  discoverInfo.addFeature("http://jabber.org/protocol/ibb");
                }
              }
            }
          }
        }
      }, discoverInfoFilter);

      connection_.getRoster().addRosterListener(new RosterListener() {
        public void entriesAdded(java.util.Collection<String> arg0) {
          sendRoster();
        }

        public void entriesDeleted(java.util.Collection<String> arg0) {
          sendRoster();
        }

        public void entriesUpdated(java.util.Collection<String> arg0) {
          sendRoster();
        }

        public void presenceChanged(Presence presence) {
          try {
            JsonGeneratorImpl generatorImpl = new JsonGeneratorImpl();
            EventsBean eventsBean = new EventsBean();
            eventsBean.addPresence(TransformUtils.presenceToBean(presence));
            eventsBean.setEventId(Packet.nextID());
            try {
              List<ContactBean> list = new ArrayList<ContactBean>();
              for (ContactBean b : eventsBean.getRoster()) {
                UserInfo info = getUserInfo(b.getUser().split("@")[0]);
                b.setFullName(info.getFirstName() + " " + info.getLastName());
                list.add(b);
              }
              eventsBean.setRoster(list);
            } catch (Exception e) {
            }
            JsonValue json = generatorImpl.createJsonObject(eventsBean);
            delegate_.sendMessage(username_, CometdChannels.PRESENCE, json.toString(), null);
            if (log.isDebugEnabled())
              log.debug(json.toString());
          } catch (Exception e) {
            if (log.isDebugEnabled()) {
              log.debug("Can not send the roster", e);
            }
          }
        };
      });

      sessionProvider = new SessionProvider(ConversationState.getCurrent());
      // for keeping session in cache
      sessionProvider.getSession(history_.getWorkspace(), history_.getRepository());

    } catch (XMPPException e) {
      // CS-4712: disconnect when login failed
      if (connection_.isConnected()) {
        connection_.disconnect();
      }
      throw new XMPPException("Create XMPP connection for user '" + username + "' failed. ", e);
    } catch (RepositoryException e) {
      if (log.isDebugEnabled()) {
        log.debug("RepositoryException in constructor XMPPSessionImpl", e);
      }
    } catch (RepositoryConfigurationException e) {
      if (log.isDebugEnabled()) {
        log.debug("RepositoryConfigurationException in constructor XMPPSessionImpl", e);
      }
    }
    if (log.isDebugEnabled())
      log.debug("finish initialize for the user:'" + username + "'.");
  }

  /**
   * {@inheritDoc}
   */
  public boolean addTransport(Transport transport, String remoteUser, String remotePassword, boolean autoLogin) {
    remoteUser = encodeUserName(remoteUser);
    if (!isTransportAvailable(transport) && isTransportSupported(transport)) {
      try {
        XMPPConnectionUtils.registerUser(connection_, transport.getServiceName(), remoteUser, remotePassword);
      } catch (XMPPException e) {
        if (log.isDebugEnabled()) {
          log.debug("Can not register the user", e);
        }
        return false;
      }
      if (autoLogin) {
        Presence presence = new Presence(Presence.Type.available);
        presence.setTo(transport.getServiceName());
        sendPresence(presence);
      }
      return true;
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public void addBuddy(String username, String nickname, String group) throws XMPPException {
    username = encodeUserName(username);
    String jid = username + "@" + connection_.getServiceName();
    Roster roster = connection_.getRoster();
    roster.createEntry(jid, nickname, new String[] { group });
    if (log.isDebugEnabled())
      log.debug(username_ + " add to roster " + jid);
  }

  /**
   * {@inheritDoc}
   */
  public void askForSubscription(String username, String nickname) {
    username = encodeUserName(username);
    String jid = username + "@" + connection_.getServiceName();
    Presence precense = new Presence(Presence.Type.subscribe);
    precense.setTo(jid);
    sendPresence(precense);
    if (log.isDebugEnabled())
      log.debug(username_ + " ask subscription " + jid);
  }

  /**
   * {@inheritDoc}
   */
  public RosterEntry getBuddy(String name) {
    name = encodeUserName(name);
    String jid = name + "@" + connection_.getServiceName();
    return connection_.getRoster().getEntry(jid);
  }

  /**
   * {@inheritDoc}
   */
  public void updateBuddy(String username, String nickname, String group) throws XMPPException {
    username = encodeUserName(username);
    String jid = username + "@" + connection_.getServiceName();
    Roster roster = connection_.getRoster();
    RosterEntry entry = roster.getEntry(jid);
    // entry.setName(nickname);
    if (roster.getGroup(group) == null) {
      RosterGroup rosterGroup = roster.createGroup(group);
      rosterGroup.addEntry(entry);
    } else {
      RosterGroup rosterGroup = roster.getGroup(group);
      rosterGroup.addEntry(roster.getEntry(jid));
    }
  }

  /**
   * @param grop the group name
   */
  public void createGroup(String grop) {
    try {
      Roster roster = connection_.getRoster();
      roster.createGroup(grop);
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("Fail to create new group", e);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public boolean cleanBuddiesList() throws XMPPException {
    Roster buddies = connection_.getRoster();
    for (RosterEntry re : buddies.getEntries())
      buddies.removeEntry(re);
    return true;
  }

  /**
   * {@inheritDoc}
   */
  public void close() {
    sessionProvider.close();
    connection_.disconnect(new Presence(Presence.Type.unavailable));
    if (log.isDebugEnabled())
      log.info("Client '" + username_ + "' logged out.");
  }

  /**
   * {@inheritDoc}
   */
  public List<HistoricalMessage> getAllHistory(String usernameto, String usernamefrom, boolean isGroupChat) {
    return history_.getHistoricalMessages(usernameto, usernamefrom, isGroupChat, sessionProvider);
  }

  /**
   * {@inheritDoc}
   */
  public List<Presence> getBuddiesPresense() {
    List<Presence> list = new ArrayList<Presence>();
    Roster buddies = connection_.getRoster();
    for (RosterEntry re : buddies.getEntries()) {
      list.add(buddies.getPresence(re.getUser()));
    }
    return list;
  }

  /**
   * {@inheritDoc}
   */
  public XMPPConnection getConnection() {
    return connection_;
  }

  /**
   * {@inheritDoc}
   */
  public List<HistoricalMessage> getHistoryBetweenDate(String usernameto, String usernamefrom, boolean isGroupChat, Date dateFrom, Date dateTo) {
    return history_.getHistoricalMessages(usernameto, usernamefrom, isGroupChat, dateFrom, dateTo, sessionProvider);

  }

  /**
   * {@inheritDoc}
   */
  public List<HistoricalMessage> getHistoryFromDateToNow(String usernameto, String usernamefrom, boolean isGroupChat, Date dateFrom) {
    return history_.getHistoricalMessages(usernameto, usernamefrom, isGroupChat, dateFrom, sessionProvider);
  }

  /**
   * {@inheritDoc}
   */
  public List<HistoricalMessage> getNotRecieveMessages() {
    String usernameTo = username_ + "@";
    return history_.getNotReciveMessage(usernameTo, sessionProvider);
  }

  /**
   * {@inheritDoc}
   */
  public List<Interlocutor> getInterlocutors(String username) {
    return history_.getInterlocutors(username, sessionProvider);
  }

  /**
   * @return the MultiuserChat
   */
  public MultiUserChatManager getMultiUserChatManager() {
    return multiUserChatManager;
  }

  /**
   * {@inheritDoc}
   */
  public Boolean isJoin(String room) throws XMPPException {
    MultiUserChat chat = getMultiUserChat(room);
    if (chat != null) {
      return chat.isJoined();
    }
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public Form getSearchForm(String searchService) throws XMPPException {
    return new UserSearchManager(connection_).getSearchForm(searchService);
  }

  /**
   * {@inheritDoc}
   */
  public List<String> getSearchServices() {
    DiscoverItems discoverItems = XMPPConnectionUtils.getDiscoverItems(connection_, connection_.getServiceName());
    Iterator<Item> iterator = discoverItems.getItems();
    List<String> services = new ArrayList<String>();
    ServiceDiscoveryManager discoveryManager = ServiceDiscoveryManager.getInstanceFor(connection_);
    while (iterator.hasNext()) {
      Item item = iterator.next();
      DiscoverInfo info;
      try {
        info = discoveryManager.discoverInfo(item.getEntityID());
      } catch (XMPPException e) {
        continue;
      }
      if (info.containsFeature("jabber:iq:search")) {
        services.add(item.getEntityID());
      }
    }
    return services;
  }

  public UIStateDataBean getUIStateData() {
    return uiStateData_;
  }

  public void setUIStateData(UIStateDataBean uiStateData) {
    uiStateData_ = uiStateData;
  }

  /**
   * {@inheritDoc}
   */
  public UserInfo getUserInfo(String userID) {
    userID = decodeUsername(userID);
    return organization_.getUserInfo(userID);
  }

  /**
   * {@inheritDoc}
   */
  public String getUsername() {
    return username_;
  }

  /**
   * Check if <code>Transport</code> is in available list.
   * 
   * @param transport the Transport.
   * @return - <code>true</code> if supported otherwise <code>false</code>.
   */
  private boolean isTransportAvailable(Transport transport) {
    return XMPPConnectionUtils.isRegistered(connection_, transport.getServiceName());
  }

  /**
   * Check is <code>Transport</code> supported.
   * 
   * @param transport the Transport.
   * @return - <code>true</code> if supported otherwise <code>false</code>.
   */
  private boolean isTransportSupported(Transport transport) {
    List<Transport> supportedTransports = XMPPConnectionUtils.getSupportedTransports(connection_);
    for (Transport t : supportedTransports) {
      if (t.getServiceName().equals(transport.getServiceName())) {
        return true;
      }
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public void removeAllTransport() throws XMPPException {
    List<Transport> list = XMPPConnectionUtils.getSupportedTransports(connection_);
    for (Transport transport : list) {
      removeTransport(transport);
    }
  }

  /**
   * {@inheritDoc}
   */
  public boolean removeBuddy(String name) throws XMPPException {
    name = encodeUserName(name);
    String jid = name + "@" + connection_.getServiceName();
    Roster buddies = connection_.getRoster();
    for (RosterEntry re : buddies.getEntries()) {
      if (jid.equals(re.getUser())) {
        buddies.removeEntry(re);
        return true;
      }
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public boolean removeTransport(Transport transport) throws XMPPException {
    if (isTransportAvailable(transport)) {
      XMPPConnectionUtils.unregisterUser(connection_, transport.getServiceName());
      Presence presence = new Presence(Presence.Type.unavailable);
      presence.setTo(transport.getServiceName());
      sendPresence(presence);
    }
    return true;
  }

  /**
   * {@inheritDoc}
   */
  public ReportedData searchUser(String search, boolean byUsername, boolean byName, boolean byEmail, String searchService) throws XMPPException {
    // Probably: change to ask about search form for each search.
    // In this case it will not necessary to check form - it will be impossible
    // to put wrong fields in the form.
    UserSearchManager searchManager = new UserSearchManager(connection_);
    Form searchForm = searchManager.getSearchForm(searchService);

    Form answerForm = searchForm.createAnswerForm();

    Iterator<FormField> searchableFieldsIterator = searchForm.getDataFormToSend().getFields();

    while (searchableFieldsIterator.hasNext()) {
      String searchVariable = searchableFieldsIterator.next().getVariable();
      if (searchVariable.equalsIgnoreCase(SearchFormFields.USERNAME)) {
        answerForm.setAnswer(searchVariable, byUsername);
      }
      if (searchVariable.equalsIgnoreCase(SearchFormFields.NAME)) {
        answerForm.setAnswer(searchVariable, byName);
      }
      if (searchVariable.equalsIgnoreCase(SearchFormFields.EMAIL)) {
        answerForm.setAnswer(searchVariable, byEmail);
      }
      if (searchVariable.equalsIgnoreCase(SearchFormFields.SEARCH)) {
        if (search != null) {
          answerForm.setAnswer(searchVariable, search);
        }
      }
    }

    return searchManager.getSearchResults(answerForm, searchService);
  }

  /**
   * {@inheritDoc}
   */
  public void sendMessage(Message message) {
    message.setFrom(encodeUserName(message.getFrom()));
    if (connection_.isConnected()) {
      connection_.sendPacket(message);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void sendMessageToMUC(String room, String body, Map<String, Object> params) throws XMPPException {
    MultiUserChat chat = multiUserChatManager.getMultiUserChat(room);
    Message message = chat.createMessage();
    message.setBody(body);
    message.setFrom(encodeUserName(getUsername()));
    Iterator<Entry<String, Object>> iter = params.entrySet().iterator();
    while (iter.hasNext()) {
      Entry<String, Object> entry = iter.next();
      message.setProperty(entry.getKey(), entry.getValue());
    }
    chat.sendMessage(message);
  }

  /**
   * {@inheritDoc}
   */
  public void sendPresence(Presence presence) {
    if (connection_.isConnected()) {
      connection_.sendPacket(presence);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void subscribeUser(String user) {
    user = encodeUserName(user);
    String jid = user + "@" + connection_.getServiceName();
    Presence precense = new Presence(Presence.Type.subscribed);
    precense.setTo(jid);
    sendPresence(precense);
  }

  /**
   * {@inheritDoc}
   */
  public void subscribeUser(String toUser, String serviceName) {
    toUser = encodeUserName(toUser);
    Presence precense = new Presence(Presence.Type.subscribed);
    precense.setTo(toUser + "@" + serviceName);
    sendPresence(precense);
  }

  /**
   * {@inheritDoc}
   */
  public void unsubscribeUser(String user) {
    user = encodeUserName(user);
    String jid = user + "@" + connection_.getServiceName();
    Presence precense = new Presence(Presence.Type.unsubscribed);
    precense.setTo(jid);
    sendPresence(precense);
  }

  /**
   * {@inheritDoc}
   */
  public void unsubscribeUser(String toUser, String serviceName) {
    toUser = encodeUserName(toUser);
    Presence precense = new Presence(Presence.Type.unsubscribed);
    precense.setTo(toUser + "@" + serviceName);
    sendPresence(precense);
  }

  /**
   * {@inheritDoc}
   */
  public void messageReceive(String messageId) {
    history_.messageReceive(messageId, sessionProvider);
  }

  /**
   * @return the sessionProvider
   */
  public SessionProvider getSessionProvider() {
    return sessionProvider;
  }

  /**
   * {@inheritDoc}
   */
  public MultiUserChat getMultiUserChat(String room) throws XMPPException {
    String roomJID = validateRoomJID(room);
    return multiUserChatManager.getMultiUserChat(roomJID);
  }

  /**
   * {@inheritDoc}
   */
  public FormBean createRoom(String room, String nickname) throws XMPPException {
    String roomJID = validateRoomJID(room);
    MultiUserChat chat = new MultiUserChat(connection_, roomJID);
    if (nickname == null)
      nickname = username_;
    nickname = encodeUserName(nickname);
    chat.create(nickname);
    multiUserChatManager.addMultiUserChat(chat);
    addListeners(chat);
    MUCPacketBean bean = new MUCPacketBean();
    bean.setAction(MUCConstants.Action.CREATED);
    bean.setRoom(roomJID);
    bean.setCreatedRoom(getRoomInfoBean(roomJID));
    sendGroupChatEvent(bean);
    Form form = chat.getConfigurationForm();
    // Tricks, add one more field to configuration form for sending real JID to UI client
    form = addRoomIDField(form, chat.getRoom());
    return TransformUtils.formToFormBean(form);
  }

  /**
   * {@inheritDoc}
   */
  public FormBean getConfigFormRoom(String room) throws XMPPException {
    MultiUserChat chat = getMultiUserChat(room);
    if (chat != null) {
      Form form = chat.getConfigurationForm();
      if (form != null) {

        // Tricks, add one more field to configuration form for sending real JID to UI client
        form = addRoomIDField(form, chat.getRoom());
        FormBean formBean = TransformUtils.formToFormBean(form);
        List<String> users = new ArrayList<String>();
        List<String> fullNames = new ArrayList<String>();
        Iterator<String> occupants = chat.getOccupants();
        while (occupants.hasNext()) {
          String occ = (String) occupants.next();
          String jid = chat.getOccupant(occ).getJid();
          users.add(jid);
          UserInfo info = getUserInfo(jid.split("@")[0]);
          fullNames.add(info.getFirstName() + " " + info.getLastName());
        }
        formBean.setMembers(users);
        formBean.setFullNames(fullNames);
        return formBean;
      }
    }
    return null;
  }

  private Form addRoomIDField(Form form, String roomJID) {
    FormField formField = new FormField(ROOM_JID_VALUE);
    formField.addValue(roomJID);
    formField.setDescription("A fully qualified xmpp ID, e.g. roomName@service");
    formField.setLabel("Room JID");
    formField.setType(FormField.TYPE_JID_SINGLE);
    form.addField(formField);
    return form;
  }

  /**
   * {@inheritDoc}
   */
  public MultiUserChat configRoom(String room, ConfigRoomBean cr) throws XMPPException {
    String service = connection_.getServiceName();
    ConfigRoomBean configRoom = new ConfigRoomBean();
    configRoom.setAllowinvites(cr.getAllowinvites());
    configRoom.setCanchangenick(cr.getCanchangenick());
    configRoom.setChangesubject(cr.getChangesubject());
    configRoom.setMaxusers(cr.getMaxusers());
    configRoom.setMembersonly(cr.getMembersonly());
    configRoom.setModeratedroom(cr.getModeratedroom());
    configRoom.setPasswordprotectedroom(cr.getPasswordprotectedroom());
    configRoom.setPersistentroom(cr.getPersistentroom());
    configRoom.setPresencebroadcast(cr.getPresencebroadcast());
    configRoom.setPublicroom(cr.getPublicroom());
    configRoom.setReservednick(cr.getReservednick());
    configRoom.setRoomdesc(cr.getRoomdesc());
    configRoom.setRoomname(cr.getRoomname());
    configRoom.setRoomsecret(cr.getRoomsecret());
    configRoom.setWhois(cr.getWhois());
    configRoom.setEnablelogging(cr.getEnablelogging());
    List<String> roomadmins = new ArrayList<String>();
    if (cr.getRoomadmins() != null) {
      for (String name : cr.getRoomadmins()) {
        if (!name.contains("@"))
          roomadmins.add(name + "@" + service);
        else
          roomadmins.add(name);
      }
    }
    configRoom.setRoomadmins(roomadmins);
    List<String> roomowners = new ArrayList<String>();
    if (cr.getRoomowners() != null) {
      for (String name : cr.getRoomowners()) {
        if (!name.contains("@"))
          roomowners.add(name + "@" + service);
        else
          roomowners.add(name);
      }
    }
    configRoom.setRoomowners(roomowners);
    MultiUserChat chat = getMultiUserChat(room);
    if (chat != null) {
      Form answerform = chat.getConfigurationForm().createAnswerForm();
      if (configRoom.getRoomname() != null)
        answerform.setAnswer("muc#roomconfig_roomname", configRoom.getRoomname());
      else
        answerform.setDefaultAnswer("muc#roomconfig_roomname");
      if (configRoom.getRoomdesc() != null)
        answerform.setAnswer("muc#roomconfig_roomdesc", configRoom.getRoomdesc());
      else
        answerform.setDefaultAnswer("muc#roomconfig_roomdesc");
      if (configRoom.getEnablelogging() != null)
        answerform.setAnswer("muc#roomconfig_enablelogging", configRoom.getEnablelogging());
      else
        answerform.setDefaultAnswer("muc#roomconfig_enablelogging");
      if (configRoom.getChangesubject() != null)
        answerform.setAnswer("muc#roomconfig_changesubject", configRoom.getChangesubject());
      else
        answerform.setDefaultAnswer("muc#roomconfig_changesubject");
      if (configRoom.getMaxusers() != null)
        answerform.setAnswer("muc#roomconfig_maxusers", configRoom.getMaxusers());
      else
        answerform.setDefaultAnswer("muc#roomconfig_maxusers");
      if (configRoom.getPresencebroadcast() != null)
        answerform.setAnswer("muc#roomconfig_presencebroadcast", configRoom.getPresencebroadcast());
      else
        answerform.setDefaultAnswer("muc#roomconfig_presencebroadcast");
      if (configRoom.getPublicroom() != null)
        answerform.setAnswer("muc#roomconfig_publicroom", configRoom.getPublicroom());
      else
        answerform.setDefaultAnswer("muc#roomconfig_publicroom");
      if (configRoom.getPersistentroom() != null)
        answerform.setAnswer("muc#roomconfig_persistentroom", configRoom.getPersistentroom());
      else
        answerform.setDefaultAnswer("muc#roomconfig_persistentroom");
      if (configRoom.getModeratedroom() != null)
        answerform.setAnswer("muc#roomconfig_moderatedroom", configRoom.getModeratedroom());
      else
        answerform.setDefaultAnswer("muc#roomconfig_moderatedroom");
      if (configRoom.getMembersonly() != null)
        answerform.setAnswer("muc#roomconfig_membersonly", configRoom.getMembersonly());
      else
        answerform.setDefaultAnswer("muc#roomconfig_membersonly");
      if (configRoom.getAllowinvites() != null)
        answerform.setAnswer("muc#roomconfig_allowinvites", configRoom.getAllowinvites());
      else
        answerform.setDefaultAnswer("muc#roomconfig_allowinvites");
      if (configRoom.getPasswordprotectedroom() != null)
        answerform.setAnswer("muc#roomconfig_passwordprotectedroom", configRoom.getPasswordprotectedroom());
      else
        answerform.setDefaultAnswer("muc#roomconfig_passwordprotectedroom");
      if (configRoom.getRoomsecret() != null)
        answerform.setAnswer("muc#roomconfig_roomsecret", configRoom.getRoomsecret());
      else
        answerform.setDefaultAnswer("muc#roomconfig_roomsecret");
      if (configRoom.getWhois() != null)
        answerform.setAnswer("muc#roomconfig_whois", configRoom.getWhois());
      else
        answerform.setDefaultAnswer("muc#roomconfig_whois");
      if (configRoom.getReservednick() != null)
        answerform.setAnswer("x-muc#roomconfig_reservednick", configRoom.getReservednick());
      else
        answerform.setDefaultAnswer("x-muc#roomconfig_reservednick");
      if (configRoom.getCanchangenick() != null)
        answerform.setAnswer("x-muc#roomconfig_canchangenick", configRoom.getCanchangenick());
      else
        answerform.setDefaultAnswer("x-muc#roomconfig_canchangenick");
      if (configRoom.getRoomadmins() != null) {
        if (configRoom.getRoomadmins().isEmpty()) {
          Collection<String> adminsjid = new ArrayList<String>();
          Collection<Affiliate> admins = chat.getAdmins();
          for (Affiliate affiliate : admins) {
            adminsjid.add(affiliate.getJid());
          }
          chat.revokeAdmin(adminsjid);
        } else
          answerform.setAnswer("muc#roomconfig_roomadmins", configRoom.getRoomadmins());
      } else
        answerform.setDefaultAnswer("muc#roomconfig_roomadmins");
      if (configRoom.getRoomowners() != null)
        answerform.setAnswer("muc#roomconfig_roomowners", configRoom.getRoomowners());
      else
        answerform.setDefaultAnswer("muc#roomconfig_roomowners");
      chat.sendConfigurationForm(answerform);
      multiUserChatManager.updateMultiUserChat(chat);
      return chat;
    }
    return null;
  }

  /**
   * @param roomJID the room id
   * @return true if password required
   */
  public boolean isPasswordRequired(String room) {
    try {
      String roomJID = validateRoomJID(room);
      ServiceDiscoveryManager discover = new ServiceDiscoveryManager(connection_);
      DiscoverInfo info = discover.discoverInfo(roomJID);
      return info.containsFeature("muc_passwordprotected");
    } catch (XMPPException e) {
      if (log.isDebugEnabled()) {
        log.debug("XMPPException when check if password require", e);
      }
    }
    return false;
  }

  // 17/06/2010 add start
  @Override
  public void addFullUserNames(String userName, String fullUserName) {
    userName = encodeUserName(userName);
    OccupantBean bean = new OccupantBean();
    bean.setFullName(fullUserName);
    bean.setNick(userName);
    beanList.add(bean);
  }

  // 17/06/2010 add end

  /**
   * {@inheritDoc}
   */
  public void joinRoom(String room, String nickname, String password) throws XMPPException {
    // 17/06/2010 add start
    JsonGeneratorImpl generatorImpl = new JsonGeneratorImpl();

    FullRoomInfoBean infoBean = new FullRoomInfoBean();
    infoBean.setOccupants(beanList);

    try {
      JsonValue json = generatorImpl.createJsonObject(infoBean);
      for (OccupantBean bean : beanList) {
        delegate_.sendMessage(bean.getNick(), CometdChannels.FULLNAME_EXCHANGE, json.toString(), null);
      }
    } catch (JsonException e) {
    }
    // 17/06/2010 add end

    if (nickname == null) {
      nickname = username_;
    }
    nickname = encodeUserName(nickname);
    MultiUserChat chat = getMultiUserChat(room);
    if (chat == null) {
      String roomJID = validateRoomJID(room);
      chat = new MultiUserChat(connection_, roomJID);
      addListeners(chat);
      multiUserChatManager.addMultiUserChat(chat);
    }
    if (!chat.isJoined()) {
      if (password != null) {
        chat.join(nickname, password);
      } else
        chat.join(nickname);
    }
  }

  public InitInfoBean getRooms(Integer from, Integer to, String sort) throws XMPPException {
    Collection<String> collectionMUCService = MultiUserChat.getServiceNames(connection_);
    String mucService = collectionMUCService.toArray()[0].toString();
    List<HostedRoom> hr = new ArrayList<HostedRoom>();
    List<HostedRoomBean> rooms = new ArrayList<HostedRoomBean>();
    Collection<HostedRoom> hostedRoomsAll = MultiUserChat.getHostedRooms(connection_, mucService);
    for (HostedRoom hostedRoom : hostedRoomsAll) {
      hr.add(hostedRoom);
    }
    if (sort == null)
      sort = ASCENDING;
    if (sort.equalsIgnoreCase(DESCENDING))
      Collections.sort(hr, new RoomNameComparatorDesc());
    else
      Collections.sort(hr, new RoomNameComparatorAsc());

    if (from == null || from < 0 || from > hr.size())
      from = 0;
    if (to == null || from > to || to < 0 || to > hr.size())
      to = hr.size();
    List<HostedRoom> hrp = hr.subList(from, to);
    for (HostedRoom hostedRoom : hrp) {
      HostedRoomBean roomBean = new HostedRoomBean();
      RoomInfo roomInfo = MultiUserChat.getRoomInfo(connection_, hostedRoom.getJid());
      if (roomInfo != null) {
        roomBean = new HostedRoomBean(roomInfo);
      }
      roomBean.setJid(hostedRoom.getJid());
      roomBean.setName(hostedRoom.getName());
      rooms.add(roomBean);
    }
    InitInfoBean infoBean = new InitInfoBean();
    infoBean.setHostedRooms(rooms);
    infoBean.setTotalRooms(hostedRoomsAll.size());
    return infoBean;
  }

  private class RoomNameComparatorAsc implements Comparator<HostedRoom> {
    public int compare(HostedRoom room1, HostedRoom room2) {
      if (room1.getName().length() == room2.getName().length()) {
        return room1.getName().compareTo(room2.getName());
      } else {
        return room1.getName().length() > room2.getName().length() ? 1 : -1;
      }
    }
  }

  private class RoomNameComparatorDesc implements Comparator<HostedRoom> {
    public int compare(HostedRoom room1, HostedRoom room2) {
      if (room1.getName().length() == room2.getName().length()) {
        return room2.getName().compareTo(room1.getName());
      } else {
        return room2.getName().length() > room1.getName().length() ? 1 : -1;
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public void changeSubject(String room, String newSubject) throws XMPPException {
    MultiUserChat chat = getMultiUserChat(room);
    if (chat != null)
      chat.changeSubject(newSubject);
  }

  /**
   * {@inheritDoc}
   */
  public void changeAvailabilityStatusInRoom(String room, String status, String mode) throws XMPPException {
    MultiUserChat chat = getMultiUserChat(room);
    if (chat != null) {
      Presence.Mode m = Presence.Mode.valueOf(mode);
      if (status == null || status.length() == 0) {
        status = PresenceUtil.getDefaultStatusMode(m);
        if (status == null)
          status = mode;
      }
      chat.changeAvailabilityStatus(status, m);
    }
  }

  /**
   * {@inheritDoc}
   */
  public List<String> getJoinedRooms() {
    Collection<MultiUserChat> collection = multiUserChatManager.getAll();
    List<String> joinedRooms = new ArrayList<String>();
    for (Iterator<MultiUserChat> iterator = collection.iterator(); iterator.hasNext();) {
      MultiUserChat multiUserChat = (MultiUserChat) iterator.next();
      if (multiUserChat.isJoined()) {
        joinedRooms.add(multiUserChat.getRoom());
      }
    }
    return joinedRooms;
  }

  /**
   * {@inheritDoc}
   */
  public FullRoomInfoBean getRoomInfoBean(String room) throws XMPPException {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    UserInfoService organization = (UserInfoService) container.getComponentInstanceOfType(UserInfoService.class);
    MultiUserChat chat = getMultiUserChat(room);
    if (chat != null) {
      RoomInfo roomInfo = MultiUserChat.getRoomInfo(connection_, chat.getRoom());
      Collection<OccupantBean> occupants = new ArrayList<OccupantBean>();
      Iterator<String> occ = chat.getOccupants();
      while (occ.hasNext()) {
        String user = (String) occ.next();
        Occupant occupant = chat.getOccupant(user);
        OccupantBean occupantBean = new OccupantBean();
        occupantBean.setAffiliation(occupant.getAffiliation());
        occupantBean.setJid(occupant.getJid());
        String username = decodeUsername(occupant.getNick());
        occupantBean.setNick(occupant.getNick());
        occupantBean.setRole(occupant.getRole());
        UserInfo userInfo = organization.getUserInfo(username);
        occupantBean.setFullName(userInfo.getFirstName() + " " + userInfo.getLastName());
        occupants.add(occupantBean);
      }
      FullRoomInfoBean infoBean = new FullRoomInfoBean(occupants, roomInfo);
      return infoBean;
    }
    return null;
  }

  public RoomInfo getRoomInfo(String room) throws XMPPException {
    String roomJID = validateRoomJID(room);
    return MultiUserChat.getRoomInfo(connection_, roomJID);
  }

  /**
   * {@inheritDoc}
   */
  public void declineRoom(String room, String inviter, String reason) {
    inviter = encodeUserName(inviter);
    String roomJID = validateRoomJID(room);
    String inviterJID = inviter + "@" + connection_.getServiceName();
    MultiUserChat.decline(connection_, roomJID, inviterJID, reason);
  }

  /**
   * {@inheritDoc}
   */
  public Boolean inviteToRoom(String room, String invitee, String reason) throws XMPPException {
    invitee = encodeUserName(invitee);
    String inviteeJID = invitee + "@" + connection_.getServiceName();
    MultiUserChat chat = getMultiUserChat(room);
    if (chat != null) {
      if (chat.isJoined()) {
        chat.invite(inviteeJID, reason);
        return true;
      }
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public Boolean leaveRoom(String room) throws XMPPException {
    MultiUserChat chat = getMultiUserChat(room);
    if (chat != null) {
      if (chat.isJoined())
        chat.leave();
      multiUserChatManager.removeMultiUserChat(chat);
      return true;
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public void manageRole(String room, String nickname, String role, String command) throws XMPPException {
    nickname = encodeUserName(nickname);
    MultiUserChat chat = getMultiUserChat(room);
    if (chat == null)
      throw new XMPPException("Chat not found", new XMPPError(XMPPError.Condition.item_not_found));
    if (role.equals(MUCConstants.Role.MODERATOR)) {
      if (command.equals(MUCConstants.Manage.GRANT)) {
        chat.grantModerator(nickname);
      } else if (command.equals(MUCConstants.Manage.REVOKE)) {
        chat.revokeModerator(nickname);
      } else {
        throw (new XMPPException("Wrong command must be: '" + MUCConstants.Manage.GRANT + "' or '" + MUCConstants.Manage.REVOKE + "'"));
      }
    } else if (role.equals(MUCConstants.Role.PARTICIPANT)) {
      if (command.equals(MUCConstants.Manage.GRANT)) {
        chat.grantVoice(nickname);
      } else if (command.equals(MUCConstants.Manage.REVOKE)) {
        chat.revokeVoice(nickname);
      } else {
        throw (new XMPPException("Wrong command must be: '" + MUCConstants.Manage.GRANT + "' or '" + MUCConstants.Manage.REVOKE + "'"));
      }
    } else {
      throw (new XMPPException("Wrong role must be: '" + MUCConstants.Role.MODERATOR + "' or '" + MUCConstants.Role.PARTICIPANT + "'"));
    }
  }

  /**
   * {@inheritDoc}
   */
  public void kickUser(String room, String nickname, String reason) throws XMPPException {
    nickname = encodeUserName(nickname);
    MultiUserChat chat = getMultiUserChat(room);
    if (chat == null)
      throw new XMPPException("Chat not found", new XMPPError(XMPPError.Condition.item_not_found));
    chat.kickParticipant(nickname, reason);
  }

  /**
   * {@inheritDoc}
   */
  public void banUser(String room, String nickname, String reason) throws XMPPException {
    nickname = encodeUserName(nickname);
    MultiUserChat chat = getMultiUserChat(room);
    if (chat == null)
      throw new XMPPException("Chat not found", new XMPPError(XMPPError.Condition.item_not_found));
    Occupant occupant = chat.getOccupant(chat.getRoom() + "/" + nickname);
    if (occupant != null) {
      String jid = occupant.getJid();
      chat.banUser(jid, reason);
    } else {
      if (log.isDebugEnabled())
        log.debug("Occupants witn nickname " + nickname + " not found!");
    }
  }

  /**
   * {@inheritDoc}
   */
  public void changeNickname(String room, String nickname) throws XMPPException {
    nickname = encodeUserName(nickname);
    MultiUserChat chat = getMultiUserChat(room);
    if (chat != null && nickname != null)
      chat.changeNickname(nickname);
  }

  /**
   * {@inheritDoc}
   */
  public Boolean destroyRoom(String room, String reason, String altRoom) throws XMPPException {
    String alternateJID = validateRoomJID(altRoom);
    MultiUserChat chat = getMultiUserChat(room);
    if (chat != null) {
      chat.destroy(reason, alternateJID);
      return true;
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public void manageAffiliation(String room, String nickname, String affiliation, String command) throws XMPPException {
    nickname = encodeUserName(nickname);
    MultiUserChat chat = getMultiUserChat(room);
    if (chat == null)
      throw new XMPPException("Chat not found", new XMPPError(XMPPError.Condition.item_not_found));
    Occupant occupant = chat.getOccupant(chat.getRoom() + "/" + nickname);
    if (occupant != null) {
      String jid = occupant.getJid();
      if (affiliation.equals(MUCConstants.Affiliation.ADMIN)) {
        if (command.equals(MUCConstants.Manage.GRANT)) {
          chat.grantAdmin(jid);
        } else if (command.equals(MUCConstants.Manage.REVOKE)) {
          chat.revokeAdmin(jid);
        } else {
          throw (new XMPPException("Wrong command must be: '" + MUCConstants.Manage.GRANT + "' or '" + MUCConstants.Manage.REVOKE + "'"));
        }
      } else if (affiliation.equals(MUCConstants.Affiliation.MEMBER)) {
        if (command.equals(MUCConstants.Manage.GRANT)) {
          chat.grantMembership(jid);
        } else if (command.equals(MUCConstants.Manage.REVOKE)) {
          chat.revokeMembership(jid);
        } else {
          throw (new XMPPException("Wrong command must be: '" + MUCConstants.Manage.GRANT + "' or '" + MUCConstants.Manage.REVOKE + "'"));
        }
      } else if (affiliation.equals(MUCConstants.Affiliation.OWNER)) {
        if (command.equals(MUCConstants.Manage.GRANT)) {
          chat.grantOwnership(jid);
        } else if (command.equals(MUCConstants.Manage.REVOKE)) {
          chat.revokeOwnership(jid);
        } else {
          throw (new XMPPException("Wrong command must be: '" + MUCConstants.Manage.GRANT + "' or '" + MUCConstants.Manage.REVOKE + "'"));
        }
      } else {
        throw (new XMPPException("Wrong affiliation must be: '" + MUCConstants.Affiliation.OWNER + ", '" + MUCConstants.Affiliation.ADMIN + "' or '" + MUCConstants.Affiliation.MEMBER + "'"));
      }
    }
  }

  /**
   * @param uuid the id
   * @return the FileTransferRequest
   */
  public FileTransferRequest getFileTransferRequest(String uuid) {
    return fileTransferRequestMap.remove(uuid);
  }

  /**
   * @param requestor the requestor
   * @param path the path
   * @param description the description
   * @param isRoom true if file send to the group chat
   */
  public void sendFile(String requestor, String path, String description, boolean isRoom) throws Exception {
    requestor = encodeUserName(requestor);
    if (!isRoom) {
      contFileTransfers = 1;
      String fullJID = connection_.getRoster().getPresence(requestor + "@" + connection_.getServiceName()).getFrom();
      sendFile(fullJID, path, description);
    } else {
      MultiUserChat chat = getMultiUserChat(requestor);
      Iterator<String> occ = chat.getOccupants();
      List<String> reqs = new ArrayList<String>();
      while (occ.hasNext()) {
        String user = (String) occ.next();
        String jid = chat.getOccupant(user).getJid();
        if (!jid.split("@")[0].equals(username_)) {
          reqs.add(jid);
        }
      }
      contFileTransfers = reqs.size();
      for (String jid : reqs) {
        sendFile(jid, path, description);
      }
    }
  }

  /**
   * @param fullJID the jabber id
   * @param path the apth
   * @param description the description
   */
  private void sendFile(String fullJID, String path, String description) throws Exception {
    // Create the file transfer manager
    FileTransferManager manager = new FileTransferManager(connection_);
    // Create the outgoing file transfer
    OutgoingFileTransfer transfer = manager.createOutgoingFileTransfer(fullJID);
    // Send the file
    transfer.sendFile(new File(path), description);
    CheckStatusFileTransfer check = new CheckStatusFileTransfer(transfer);
    check.start();
  }

  /**
   * @param uuid the id
   */
  public void rejectFile(String uuid) {
    getFileTransferRequest(uuid).reject();
  }

  /**
   * Delete file after receive.
   * 
   * @param path the path
   */
  private void delFile(String path) {
    try {
      File file = new File(path);
      File dir = new File(file.getParent());
      file.delete();
      dir.delete();
      if (log.isDebugEnabled())
        log.info("Delete file : " + file.getAbsolutePath());
    } catch (SecurityException e) {
      if (log.isDebugEnabled()) {
        log.debug("SecurityException when delete file: " + path, e);
      }
    }
  }

  /**
   * Check the state of file transfer.
   * 
   * @author vetal
   */
  class CheckStatusFileTransfer extends Thread {
    /**
     * 
     */
    OutgoingFileTransfer transfer;

    /**
     * @param transfer the OutgoingFileTransfer
     */
    public CheckStatusFileTransfer(OutgoingFileTransfer transfer) {
      this.transfer = transfer;
    }

    @Override
    public void run() {
      try {
        while (!transfer.isDone()) {
          Thread.sleep(1000);
        }
        // when sending file for an user who isn't in the contact list (CS-3989), an error is return with null message, so
        // statements in the following comments create a NullPointerException Exception and it forbid the error message
        // returned to client. So don't call OutgoingFileTransfer.getError().getMessage() in this situation.
        /*
         * if (transfer.getStatus().equals(Status.error)) { log.error("ERROR!!! " + transfer.getError().getMessage()); }
         */
        JsonGeneratorImpl generatorImpl = new JsonGeneratorImpl();
        FileTransferResponseBean responseBean = new FileTransferResponseBean(transfer, connection_.getUser());
        List<FileTransferResponseBean> list = new ArrayList<FileTransferResponseBean>();
        list.add(responseBean);
        FileTransferEventBean bean = new FileTransferEventBean();
        bean.setFileTransportResponses(list);
        EventsBean eventsBean = new EventsBean();
        eventsBean.addFileEvent(bean);
        eventsBean.setEventId(Packet.nextID());

        try {
          List<ContactBean> list2 = new ArrayList<ContactBean>();
          for (ContactBean b : eventsBean.getRoster()) {
            UserInfo info = getUserInfo(b.getUser().split("@")[0]);
            b.setFullName(info.getFirstName() + " " + info.getLastName());
            list2.add(b);
          }
          eventsBean.setRoster(list2);
        } catch (Exception e) {
        }

        JsonValue json = generatorImpl.createJsonObject(eventsBean);
        delegate_.sendMessage(username_, "/eXo/Application/Chat/FileExchange", json.toString(), null);
        contFileTransfers--;
        if (contFileTransfers == 0) {
          delFile(transfer.getFilePath());
        }
      } catch (Exception e) {
        if (log.isDebugEnabled()) {
          log.debug("Fail to transfer the file", e);
        }
      }
    }
  }

  /**
   * Add all listener to the chat.
   * 
   * @param chat the MultiUserChat
   */
  private void addListeners(final MultiUserChat chat) {
    chat.addInvitationRejectionListener(new InvitationRejectionListener() {
      public void invitationDeclined(String invitee, String reason) {

        MUCPacketBean bean = new MUCPacketBean();
        bean.setAction(MUCConstants.Action.DECLINE);
        bean.setDecline(new DeclineBean(invitee, reason));
        bean.setRoom(chat.getRoom());
        sendGroupChatEvent(bean);
      }
    });
    chat.addParticipantListener(new PacketListener() {
      public void processPacket(Packet packet) {
        Presence prs = (Presence) packet;
        if (prs.getStatus() != null) {
          MUCPacketBean bean = new MUCPacketBean();
          bean.setAction(MUCConstants.Action.PRESENCE_CHANGE);
          String mode = new String();
          String type = new String();
          if (prs.getMode() != null) {
            mode = prs.getMode().name();
          }
          if (prs.getType() != null)
            type = prs.getType().name();
          bean.setPresence(new PresenceBean(prs.getFrom(), mode, type, prs.getStatus()));
          bean.setRoom(chat.getRoom());
          sendGroupChatEvent(bean);
        }
      }
    });

    chat.addSubjectUpdatedListener(new SubjectUpdatedListener() {
      public void subjectUpdated(String subject, String from) {
        MUCPacketBean bean = new MUCPacketBean();
        bean.setAction(MUCConstants.Action.SUBJECT_CHANGE);
        bean.setSubjectChange(new SubjectChangeBean(from, subject));
        bean.setRoom(chat.getRoom());
        sendGroupChatEvent(bean);
      }

    });

    chat.addParticipantStatusListener(new DefaultParticipantStatusListener() {
      public void ownershipGranted(String participant) {
        super.ownershipGranted(participant);
        MUCPacketBean bean = new MUCPacketBean();
        bean.setAction(MUCConstants.Action.AFFILIATE_CHANGE);
        bean.setAffiliate(new PrivilegeChangeBean(MUCConstants.Affiliation.OWNER, MUCConstants.Manage.GRANTED, participant));
        bean.setRoom(chat.getRoom());
        sendGroupChatEvent(bean);
      }

      public void ownershipRevoked(String participant) {
        super.ownershipRevoked(participant);
        MUCPacketBean bean = new MUCPacketBean();
        bean.setAction(MUCConstants.Action.AFFILIATE_CHANGE);
        bean.setAffiliate(new PrivilegeChangeBean(MUCConstants.Affiliation.OWNER, MUCConstants.Manage.REVOKED, participant));
        bean.setRoom(chat.getRoom());
        sendGroupChatEvent(bean);
      }

      public void adminGranted(String participant) {
        super.adminGranted(participant);
        MUCPacketBean bean = new MUCPacketBean();
        bean.setAction(MUCConstants.Action.AFFILIATE_CHANGE);
        bean.setAffiliate(new PrivilegeChangeBean(MUCConstants.Affiliation.ADMIN, MUCConstants.Manage.GRANTED, participant));
        bean.setRoom(chat.getRoom());
        sendGroupChatEvent(bean);
      }

      public void adminRevoked(String participant) {
        super.adminRevoked(participant);
        MUCPacketBean bean = new MUCPacketBean();
        bean.setAction(MUCConstants.Action.AFFILIATE_CHANGE);
        bean.setAffiliate(new PrivilegeChangeBean(MUCConstants.Affiliation.ADMIN, MUCConstants.Manage.REVOKED, participant));
        bean.setRoom(chat.getRoom());
        sendGroupChatEvent(bean);
      }

      public void membershipGranted(String participant) {
        super.membershipGranted(participant);
        MUCPacketBean bean = new MUCPacketBean();
        bean.setAction(MUCConstants.Action.AFFILIATE_CHANGE);
        bean.setAffiliate(new PrivilegeChangeBean(MUCConstants.Affiliation.MEMBER, MUCConstants.Manage.GRANTED, participant));
        bean.setRoom(chat.getRoom());
        sendGroupChatEvent(bean);
      }

      public void membershipRevoked(String participant) {
        super.membershipRevoked(participant);
        MUCPacketBean bean = new MUCPacketBean();
        bean.setAction(MUCConstants.Action.AFFILIATE_CHANGE);
        bean.setAffiliate(new PrivilegeChangeBean(MUCConstants.Affiliation.MEMBER, MUCConstants.Manage.REVOKED, participant));
        bean.setRoom(chat.getRoom());
        sendGroupChatEvent(bean);
      }

      public void moderatorGranted(String participant) {
        super.moderatorGranted(participant);
        MUCPacketBean bean = new MUCPacketBean();
        bean.setAction(MUCConstants.Action.ROLE_CHANGE);
        bean.setRole(new PrivilegeChangeBean(MUCConstants.Role.MODERATOR, MUCConstants.Manage.GRANTED, participant));
        bean.setRoom(chat.getRoom());
        sendGroupChatEvent(bean);
      }

      public void moderatorRevoked(String participant) {
        super.moderatorRevoked(participant);
        MUCPacketBean bean = new MUCPacketBean();
        bean.setAction(MUCConstants.Action.ROLE_CHANGE);
        bean.setRole(new PrivilegeChangeBean(MUCConstants.Role.MODERATOR, MUCConstants.Manage.REVOKED, participant));
        bean.setRoom(chat.getRoom());
        sendGroupChatEvent(bean);
      }

      public void voiceGranted(String participant) {
        super.voiceGranted(participant);
        MUCPacketBean bean = new MUCPacketBean();
        bean.setAction(MUCConstants.Action.ROLE_CHANGE);
        bean.setRole(new PrivilegeChangeBean(MUCConstants.Role.VISITOR, MUCConstants.Manage.GRANTED, participant));
        bean.setRoom(chat.getRoom());
        sendGroupChatEvent(bean);
      }

      public void voiceRevoked(String participant) {
        super.voiceRevoked(participant);
        MUCPacketBean bean = new MUCPacketBean();
        bean.setAction(MUCConstants.Action.ROLE_CHANGE);
        bean.setRole(new PrivilegeChangeBean(MUCConstants.Role.VISITOR, MUCConstants.Manage.REVOKED, participant));
        bean.setRoom(chat.getRoom());
        sendGroupChatEvent(bean);
      }

      public void nicknameChanged(String participant, String newNickname) {
        super.nicknameChanged(participant, newNickname);
        MUCPacketBean bean = new MUCPacketBean();
        bean.setAction(MUCConstants.Action.NICKNAME_CHANGE);
        bean.setChangeNick(new ChangeNickBean(newNickname, participant));
        bean.setRoom(chat.getRoom());
        sendGroupChatEvent(bean);
      };

      public void banned(String participant, String actor, String reason) {
        super.banned(participant, actor, reason);
        MUCPacketBean bean = new MUCPacketBean();
        bean.setAction(MUCConstants.Action.BANNED);
        bean.setBanned(new KickedBannedBean(actor, participant, reason));
        bean.setRoom(chat.getRoom());
        sendGroupChatEvent(bean);
      }

      public void kicked(String participant, String actor, String reason) {
        super.kicked(participant, actor, reason);
        MUCPacketBean bean = new MUCPacketBean();
        bean.setAction(MUCConstants.Action.KICKED);
        bean.setKicked(new KickedBannedBean(actor, participant, reason));
        bean.setRoom(chat.getRoom());
        sendGroupChatEvent(bean);
      }

      public void joined(String participant) {
        super.joined(participant);
        MUCPacketBean bean = new MUCPacketBean();
        bean.setAction(MUCConstants.Action.JOINED);
        bean.setJoined(participant);
        bean.setOccupant(chat.getOccupant(participant));
        bean.setRoom(chat.getRoom());
        sendGroupChatEvent(bean);
      }

      public void left(String participant) {
        super.left(participant);
        MUCPacketBean bean = new MUCPacketBean();
        bean.setAction(MUCConstants.Action.LEFT);
        bean.setLeft(participant);
        bean.setRoom(chat.getRoom());
        sendGroupChatEvent(bean);
      }

    });

    chat.addUserStatusListener(new DefaultUserStatusListener() {

      public void ownershipGranted() {
        super.ownershipGranted();
        MUCPacketBean bean = new MUCPacketBean();
        bean.setAction(MUCConstants.Action.YOU_AFFILIATE_CHANGE);
        bean.setAffiliate(new PrivilegeChangeBean(MUCConstants.Affiliation.OWNER, MUCConstants.Manage.GRANTED, null));
        bean.setRoom(chat.getRoom());
        sendGroupChatEvent(bean);
      }

      public void ownershipRevoked() {
        super.ownershipRevoked();
        MUCPacketBean bean = new MUCPacketBean();
        bean.setAction(MUCConstants.Action.YOU_AFFILIATE_CHANGE);
        bean.setAffiliate(new PrivilegeChangeBean(MUCConstants.Affiliation.OWNER, MUCConstants.Manage.REVOKED, null));
        bean.setRoom(chat.getRoom());
        sendGroupChatEvent(bean);
      }

      public void adminGranted() {
        super.adminGranted();
        MUCPacketBean bean = new MUCPacketBean();
        bean.setAction(MUCConstants.Action.YOU_AFFILIATE_CHANGE);
        bean.setAffiliate(new PrivilegeChangeBean(MUCConstants.Affiliation.ADMIN, MUCConstants.Manage.GRANTED, null));
        bean.setRoom(chat.getRoom());
        sendGroupChatEvent(bean);
      }

      public void adminRevoked() {
        super.adminRevoked();
        MUCPacketBean bean = new MUCPacketBean();
        bean.setAction(MUCConstants.Action.YOU_AFFILIATE_CHANGE);
        bean.setAffiliate(new PrivilegeChangeBean(MUCConstants.Affiliation.ADMIN, MUCConstants.Manage.REVOKED, null));
        bean.setRoom(chat.getRoom());
        sendGroupChatEvent(bean);
      }

      public void membershipGranted() {
        super.membershipGranted();
        MUCPacketBean bean = new MUCPacketBean();
        bean.setAction(MUCConstants.Action.YOU_AFFILIATE_CHANGE);
        bean.setAffiliate(new PrivilegeChangeBean(MUCConstants.Affiliation.MEMBER, MUCConstants.Manage.GRANTED, null));
        bean.setRoom(chat.getRoom());
        sendGroupChatEvent(bean);
      }

      public void membershipRevoked() {
        super.membershipRevoked();
        MUCPacketBean bean = new MUCPacketBean();
        bean.setAction(MUCConstants.Action.YOU_AFFILIATE_CHANGE);
        bean.setAffiliate(new PrivilegeChangeBean(MUCConstants.Affiliation.MEMBER, MUCConstants.Manage.REVOKED, null));
        bean.setRoom(chat.getRoom());
        sendGroupChatEvent(bean);
      }

      public void moderatorGranted() {
        super.moderatorGranted();
        MUCPacketBean bean = new MUCPacketBean();
        bean.setAction(MUCConstants.Action.YOU_ROLE_CHANGE);
        bean.setRole(new PrivilegeChangeBean(MUCConstants.Role.MODERATOR, MUCConstants.Manage.GRANTED, null));
        bean.setRoom(chat.getRoom());
        sendGroupChatEvent(bean);
      }

      public void moderatorRevoked() {
        super.moderatorRevoked();
        MUCPacketBean bean = new MUCPacketBean();
        bean.setAction(MUCConstants.Action.YOU_ROLE_CHANGE);
        bean.setRole(new PrivilegeChangeBean(MUCConstants.Role.MODERATOR, MUCConstants.Manage.REVOKED, null));
        bean.setRoom(chat.getRoom());
        sendGroupChatEvent(bean);
      }

      public void voiceGranted() {
        super.voiceGranted();
        MUCPacketBean bean = new MUCPacketBean();
        bean.setAction(MUCConstants.Action.YOU_ROLE_CHANGE);
        bean.setRole(new PrivilegeChangeBean(MUCConstants.Role.VISITOR, MUCConstants.Manage.GRANTED, null));
        bean.setRoom(chat.getRoom());
        sendGroupChatEvent(bean);
      }

      public void voiceRevoked() {
        super.voiceRevoked();
        MUCPacketBean bean = new MUCPacketBean();
        bean.setAction(MUCConstants.Action.YOU_ROLE_CHANGE);
        bean.setRole(new PrivilegeChangeBean(MUCConstants.Role.VISITOR, MUCConstants.Manage.REVOKED, null));
        bean.setRoom(chat.getRoom());
        sendGroupChatEvent(bean);
      }

      public void banned(String actor, String reason) {
        super.banned(actor, reason);
        MUCPacketBean bean = new MUCPacketBean();
        bean.setAction(MUCConstants.Action.YOU_BANNED);
        bean.setBanned(new KickedBannedBean(actor, null, reason));
        bean.setRoom(chat.getRoom());
        sendGroupChatEvent(bean);
      }

      public void kicked(String actor, String reason) {
        super.kicked(actor, reason);
        MUCPacketBean bean = new MUCPacketBean();
        bean.setAction(MUCConstants.Action.YOU_KICKED);
        bean.setKicked(new KickedBannedBean(actor, null, reason));
        bean.setRoom(chat.getRoom());
        sendGroupChatEvent(bean);
      }
    });

  }

  public void sendErrorMessage(String msg, String sender) {
    try {
      JsonGeneratorImpl generatorImpl = new JsonGeneratorImpl();
      String id = CodingUtils.encodeToHex(UUID.randomUUID().toString());
      MessageBean messageBean = new MessageBean();
      messageBean.setBody(msg);
      messageBean.setFrom(sender);
      messageBean.setId(id);
      messageBean.setTo(connection_.getUser());
      messageBean.setType(Message.Type.error.name());
      messageBean.setDateSend(Calendar.getInstance().getTime().toString());
      EventsBean eventsBean = new EventsBean();
      eventsBean.addMessage(messageBean);
      eventsBean.setEventId(Packet.nextID());

      try {
        List<ContactBean> list = new ArrayList<ContactBean>();
        for (ContactBean b : eventsBean.getRoster()) {
          UserInfo info = getUserInfo(b.getUser().split("@")[0]);
          b.setFullName(info.getFirstName() + " " + info.getLastName());
          list.add(b);
        }
        eventsBean.setRoster(list);
      } catch (Exception e) {
      }
      JsonValue json = generatorImpl.createJsonObject(eventsBean);
      delegate_.sendMessage(username_, CometdChannels.MESSAGE, json.toString(), null);
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("Fail to send the error message", e);
      }
    }
  }

  /**
   * Send group chat events.
   * 
   * @param bean the MUCPacketBean
   */
  private void sendGroupChatEvent(MUCPacketBean bean) {
    try {
      JsonGeneratorImpl generatorImpl = new JsonGeneratorImpl();
      EventsBean eventsBean = new EventsBean();
      eventsBean.addMUCEvent(bean);
      try {
        List<ContactBean> list = new ArrayList<ContactBean>();
        for (ContactBean b : eventsBean.getRoster()) {
          UserInfo info = getUserInfo(b.getUser().split("@")[0]);
          b.setFullName(info.getFirstName() + " " + info.getLastName());
          list.add(b);
        }
        eventsBean.setRoster(list);
      } catch (Exception e) {
      }

      JsonValue json = generatorImpl.createJsonObject(eventsBean);
      delegate_.sendMessage(username_, CometdChannels.GROUP_CHAT, json.toString(), null);
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("Fail to send the group chat", e);
      }
    }
  }

  private String validateRoomJID(String room) {
    String roomJID = new String();
    try {
      if (room.contains("@"))
        roomJID = room;
      else {
        String mucService = MultiUserChat.getServiceNames(connection_).toArray()[0].toString();
        roomJID = room + "@" + mucService;
      }
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("Fail when validate the room jid", e);
      }
    }
    return roomJID;
  }
  
  /**
   * Encoding sensitive user names to bypass the limitation in only insensitive user names of Openfire
   * @param username
   * @return
   */
  public static String encodeUserName(String username) {
    if (username == null) {
      return "";
    }
    String lusername = username.toLowerCase();
    if (username.equals(lusername)) {
      return username.replaceAll(" ", "autb3nx8s847w022s");
    }
    StringBuilder sb = new StringBuilder("");
    for (int i = 0; i < username.length(); i++) {
      char c = username.charAt(i);
      char lc = lusername.charAt(i);
      if (c == lc) {
        sb.append(c);
      } else {
        sb.append("s220w748s8xn3btua").append(lc);
      }
    }
    return sb.toString().replaceAll(" ", "autb3nx8s847w022s");
  }
  
  /**
   * Decoding sensitive user names to bypass the limitation in only insensitive user names of Openfire
   * @param username
   * @return
   */
  public static String decodeUsername(String username) {
    if (username == null) {
      return null;
    }
    if (username.indexOf("s220w748s8xn3btua") < 0) {
      return username.replaceAll("autb3nx8s847w022s", " ");
    }
    String[] tokens = username.split("s220w748s8xn3btua");
    StringBuilder sb = new StringBuilder("");
    for (int i = 0; i < tokens.length; i++) {
      if (i > 0 && tokens[i].length() > 0) {
        tokens[i] = tokens[i].substring(0, 1).toUpperCase() + tokens[i].substring(1);
      }
      sb.append(tokens[i]);
    }
    return sb.toString().replaceAll("autb3nx8s847w022s", " ");
  }

  /**
   * set/get presence status***/
  public String getPresenceStatus_() {
    return status_;
  }

  public void setPresenceStatus_(String status) {
    status_ = status;
  }

  private void sendMessage(MessageBean message) throws JsonException {
    JsonGeneratorImpl generatorImpl = new JsonGeneratorImpl();
    EventsBean eventsBean = new EventsBean();
    eventsBean.addMessage(message);
    eventsBean.setEventId(Packet.nextID());
    try {
      List<ContactBean> list = new ArrayList<ContactBean>();
      for (ContactBean b : eventsBean.getRoster()) {
        UserInfo info = getUserInfo(b.getUser().split("@")[0]);
        b.setFullName(info.getFirstName() + " " + info.getLastName());
        list.add(b);
      }
      eventsBean.setRoster(list);
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("Fail to send message", e);
      }
    }

    JsonValue json = generatorImpl.createJsonObject(eventsBean);
    delegate_.sendMessage(username_, CometdChannels.MESSAGE, json.toString(), null);
    if (log.isDebugEnabled())
      log.debug(json.toString());
  }
  
  private void sendRoster() {
    try {
      JsonGeneratorImpl generatorImpl = new JsonGeneratorImpl();
      EventsBean eventsBean = new EventsBean();
      eventsBean.setRoster(TransformUtils.rosterToRosterBean(connection_.getRoster()));
      eventsBean.setEventId(Packet.nextID());

      try {
        List<ContactBean> list = new ArrayList<ContactBean>();
        for (ContactBean b : eventsBean.getRoster()) {
          UserInfo info = getUserInfo(b.getUser().split("@")[0]);
          b.setFullName(info.getFirstName() + " " + info.getLastName());
          list.add(b);
        }
        eventsBean.setRoster(list);
      } catch (Exception e) {
      }

      JsonValue json = generatorImpl.createJsonObject(eventsBean);
      delegate_.sendMessage(username_, CometdChannels.ROSTER, json.toString(), null);
      if (log.isDebugEnabled())
        log.debug(json.toString());
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("Exception in sendRoster method", e);
      }
    }
  }
}
