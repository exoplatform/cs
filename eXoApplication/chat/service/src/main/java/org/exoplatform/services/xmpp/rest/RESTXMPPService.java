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

package org.exoplatform.services.xmpp.rest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.exoplatform.common.http.HTTPMethods;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.resources.ResourceBundleService;
import org.exoplatform.services.rest.CacheControl;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.InputTransformer;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.QueryParam;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URIParam;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.transformer.PassthroughOutputTransformer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.xmpp.bean.ConfigRoomBean;
import org.exoplatform.services.xmpp.bean.ContactBean;
import org.exoplatform.services.xmpp.bean.FormBean;
import org.exoplatform.services.xmpp.bean.HostedRoomBean;
import org.exoplatform.services.xmpp.bean.InitInfoBean;
import org.exoplatform.services.xmpp.bean.InterlocutorListBean;
import org.exoplatform.services.xmpp.bean.MessageBean;
import org.exoplatform.services.xmpp.bean.MessageListBean;
import org.exoplatform.services.xmpp.bean.FullRoomInfoBean;
import org.exoplatform.services.xmpp.connection.XMPPSession;
import org.exoplatform.services.xmpp.connection.impl.XMPPMessenger;
import org.exoplatform.services.xmpp.connection.impl.XMPPSessionImpl;
import org.exoplatform.services.xmpp.ext.transport.AIMTransport;
import org.exoplatform.services.xmpp.ext.transport.GtalkTransport;
import org.exoplatform.services.xmpp.ext.transport.ICQTransport;
import org.exoplatform.services.xmpp.ext.transport.MSNTransport;
import org.exoplatform.services.xmpp.ext.transport.YahooTransport;
import org.exoplatform.services.xmpp.history.HistoricalMessage;
import org.exoplatform.services.xmpp.history.impl.jcr.HistoryImpl;
import org.exoplatform.services.xmpp.userinfo.UserInfo;
import org.exoplatform.services.xmpp.userinfo.UserInfoService;
import org.exoplatform.services.xmpp.util.PresenceUtil;
import org.exoplatform.services.xmpp.util.SearchFormFields;
import org.exoplatform.services.xmpp.util.TransformUtils;
import org.exoplatform.ws.frameworks.cometd.transport.ContinuationServiceDelegate;
import org.exoplatform.ws.frameworks.json.transformer.Bean2JsonOutputTransformer;
import org.exoplatform.ws.frameworks.json.transformer.Json2BeanInputTransformer;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.ReportedData;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.RoomInfo;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public class RESTXMPPService implements ResourceContainer, Startable {

  /**
   * 
   */
  private static final String JSON_CONTENT_TYPE    = "application/json";

  /**
   * 
   */
  private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

  /**
   * 
   */
  private final Log           log                  = ExoLogger.getLogger("lr.RESTXMPPService");

  /**
   * 
   */
  private final XMPPMessenger messenger;
  
  private final UserInfoService organization;
  
  private final ContinuationServiceDelegate delegate;
  
  private final ResourceBundleService rbs;
  
  private ResourceBundle rb;
  
  private final static String BUNDLE_NAME = "locale.message.chat.serverMessage"; 
  
  private final HistoryImpl history;
  
  private static final CacheControl cc;
  static {
    cc = new CacheControl();
    cc.setNoCache(true);
    cc.setNoStore(true);

  }

  public RESTXMPPService(XMPPMessenger messenger,
                         UserInfoService organization,
                         ContinuationServiceDelegate delegate,
                         HistoryImpl history,
                         ResourceBundleService rbs) {
    this.messenger = messenger;
    this.organization = organization;
    this.delegate = delegate;
    this.history = history;
    this.rbs = rbs;
  }
  
  
  public void start() {
    loadResourceBundle();
  }

  public void stop() {
  }
  
  private void loadResourceBundle(){
    this.rb = this.rbs.getResourceBundle(BUNDLE_NAME, Locale.getDefault());
  }

  // //////////// Group chat //////////////////
  /**
   * @param username
   * @param room
   * @return
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/xmpp/muc/createroom/{username}/")
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response createRoom(@URIParam("username") String username,
                             @QueryParam("room") String room,
                             @QueryParam("nickname") String nickname) {
    if (this.rb == null) loadResourceBundle();
    if (room == null)
      return Response.Builder.withStatus(HTTPStatus.BAD_REQUEST)
                             .errorMessage(rb.getString("chat.message.roomid.null"))
                             .build();
    XMPPSession session = messenger.getSession(username);
    if (session != null) {
      String roomEscape = StringUtils.escapeNode(room.toLowerCase());
      try {
        RoomInfo roomInfo = session.getRoomInfo(roomEscape);
        if (roomInfo != null) 
          return joinRoom(username, room, nickname, null);
        } catch (XMPPException e) {
          //nothing to do         
        }
       try { 
          FormBean formBean = session.createRoom(roomEscape, nickname);          
          List<String> values = new ArrayList<String>();
          values.add(room);
          // Tricks, add change field to configuration form for sending to UI client name of room that entered user. 
          formBean = TransformUtils.changeFieldForm(formBean, "muc#roomconfig_roomname", values);
          formBean = TransformUtils.changeFieldForm(formBean, "muc#roomconfig_roomdesc", values);
          return Response.Builder.ok(formBean, JSON_CONTENT_TYPE).cacheControl(cc).build();
      } catch (Exception e) {
        e.printStackTrace();
        return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
                               .errorMessage(rb.getString("chat.message.room.creation.error") + "\n" + e.getMessage())
                               .build();
      }
    } else {
      if (log.isDebugEnabled())
        log.debug(rb.getString("chat.message.room.xmppsession.null"));
      return Response.Builder.withStatus(HTTPStatus.BAD_REQUEST)
                             .errorMessage(rb.getString("chat.message.room.xmppsession.null"))
                             .build();
    }
  }

  
  @HTTPMethod(HTTPMethods.POST)
  @URITemplate("/xmpp/muc/configroom/{username}/")
  @InputTransformer(Json2BeanInputTransformer.class)
  public Response configRoom(@URIParam("username") String username,
                             @QueryParam("room") String room,
                             ConfigRoomBean configRoom) {
    if (this.rb == null) loadResourceBundle();
    if (room == null)
      return Response.Builder.withStatus(HTTPStatus.BAD_REQUEST)
                             .errorMessage(rb.getString("chat.message.roomid.null"))
                             .build();
    try {
      XMPPSession session = messenger.getSession(username);
      if (session != null) {
        room = StringUtils.escapeNode(room.toLowerCase());
        if (session.getMultiUserChat(room) != null)
          session.configRoom(room, configRoom);
        return Response.Builder.ok().cacheControl(cc).build();
      } else {
        if (log.isDebugEnabled())
          log.debug(rb.getString("chat.message.room.xmppsession.null"));
        return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
                               .errorMessage(rb.getString("chat.message.room.xmppsession.null"))
                               .build();
      }
    } catch (XMPPException e) {
      if (log.isDebugEnabled()) 
        e.printStackTrace();
      XMPPError error = e.getXMPPError();
      return Response.Builder.withStatus(error.getCode())
                             .errorMessage(rb.getString("chat.message.conference.configuration.error")
                                                        + "\n" + error.getMessage())
                             .build();
    }
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/xmpp/muc/getroomconfig/{username}/")
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response getRoomConfigForm(@URIParam("username") String username,
                                    @QueryParam("room") String room) {
    if (this.rb == null) loadResourceBundle();
    if (room == null)
      return Response.Builder.withStatus(HTTPStatus.BAD_REQUEST)
                             .errorMessage(rb.getString("chat.message.roomid.null"))
                             .build();
    XMPPSession session = messenger.getSession(username);
    if (session != null) {
      try {
        FormBean formBean = session.getConfigFormRoom(room);
        if (formBean != null) {
          return Response.Builder.ok(formBean, JSON_CONTENT_TYPE)
                                 .cacheControl(cc)
                                 .build();
        }
        return Response.Builder.withStatus(HTTPStatus.BAD_REQUEST)
                               .cacheControl(cc)
                               .build();
      } catch (XMPPException e) {
        if (log.isDebugEnabled()) 
          e.printStackTrace();
        XMPPError error = e.getXMPPError();
        return Response.Builder.withStatus(error.getCode())
                       .errorMessage(rb.getString("chat.message.conference.configuration.error")
                                   + "\n" + error.getMessage())
                       .build();
      }
    }
    return Response.Builder.withStatus(HTTPStatus.BAD_REQUEST)
                           .errorMessage(rb.getString("chat.message.room.xmppsession.null"))
                           .build();
  }

  /**
   * @param username
   * @param room
   * @return
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/xmpp/muc/getroominfo/{username}/")
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response getRoomInfo(@URIParam("username") String username,
                              @QueryParam("room") String room) {
    if (this.rb == null) loadResourceBundle();
    if (room == null)
      return Response.Builder.withStatus(HTTPStatus.BAD_REQUEST)
                             .errorMessage(rb.getString("chat.message.roomid.null"))
                             .build();
    XMPPSession session = messenger.getSession(username);
    if (session != null) {
      try {
        FullRoomInfoBean infoBean = session.getRoomInfoBean(room.toLowerCase());
        if (infoBean != null)
          return Response.Builder.ok(infoBean, JSON_CONTENT_TYPE)
                                 .cacheControl(cc)
                                 .build();
        return Response.Builder.withStatus(HTTPStatus.BAD_REQUEST)
                               .cacheControl(cc)
                               .build();
      } catch (XMPPException e) {
        if (log.isDebugEnabled()) 
          e.printStackTrace();
        XMPPError error = e.getXMPPError();
        return Response.Builder.withStatus(error.getCode())
                               .errorMessage(rb.getString("chat.message.conference.info.error") + "\n" + error.getMessage())
                               .build();
      }
    }
    return Response.Builder.withStatus(HTTPStatus.BAD_REQUEST)
                           .errorMessage(rb.getString("chat.message.room.xmppsession.null"))
                           .build();
  }

  /**
   * @param username
   * @return
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/xmpp/muc/joinedrooms/{username}/")
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response getJoinedRooms(@URIParam("username") String username) {
    if (this.rb == null) loadResourceBundle();
    XMPPSession session = messenger.getSession(username);
    if (session != null) {
      try {
        List<FullRoomInfoBean> joinedRooms = new ArrayList<FullRoomInfoBean>();
        List<String> list = session.getJoinedRooms();
        for (String room : list) {
          joinedRooms.add(session.getRoomInfoBean(room));
        }
        InitInfoBean bean = new InitInfoBean();
        bean.setJoinedRooms(joinedRooms);
        bean.setTotalJoinedRooms(list.size());
        return Response.Builder.ok(bean, JSON_CONTENT_TYPE).cacheControl(cc).build();
      } catch (XMPPException e) {
        if (log.isDebugEnabled()) 
          e.printStackTrace();
        XMPPError error = e.getXMPPError();
        return Response.Builder.withStatus(error.getCode())
                               .errorMessage(rb.getString("chat.message.conference.info.error") + "\n" + error.getMessage())
                               .build();
      }
    }
    return Response.Builder.withStatus(HTTPStatus.BAD_REQUEST)
                           .errorMessage(rb.getString("chat.message.room.xmppsession.null"))
                           .build();
  }
  
  @Deprecated
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/xmpp/muc/rooms-old/{username}/")
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response getRooms(@URIParam("username") String username) {
    if (this.rb == null) loadResourceBundle();
    XMPPSession session = messenger.getSession(username);
    if (session != null) {
      try {
        XMPPConnection connection = session.getConnection();
        Collection<String> collectionMUCService = MultiUserChat.getServiceNames(connection);
        String mucService = collectionMUCService.toArray()[0].toString();
        List<HostedRoomBean> rooms = new ArrayList<HostedRoomBean>();
        Collection<HostedRoom> hostedRooms = MultiUserChat.getHostedRooms(connection, mucService);
        for (HostedRoom hostedRoom : hostedRooms) {
          HostedRoomBean roomBean = new HostedRoomBean();
          RoomInfo roomInfo = MultiUserChat.getRoomInfo(connection, hostedRoom.getJid());
          if (roomInfo != null) {
            roomBean = new HostedRoomBean(roomInfo);
          }
          roomBean.setJid(hostedRoom.getJid());
          roomBean.setName(hostedRoom.getName());
          rooms.add(roomBean);
        }
        InitInfoBean infoBean = new InitInfoBean();
        infoBean.setHostedRooms(rooms);
        return Response.Builder.ok(infoBean, JSON_CONTENT_TYPE).cacheControl(cc).build();
      } catch (XMPPException e) {
        if (log.isDebugEnabled()) 
          e.printStackTrace();
        XMPPError error = e.getXMPPError();
        return Response.Builder.withStatus(error.getCode())
                               .errorMessage(error.getMessage())
                               .build();
      }
    }
    return Response.Builder.withStatus(HTTPStatus.BAD_REQUEST)
                           .errorMessage(rb.getString("chat.message.room.xmppsession.null"))
                           .build();
  }
  
  
  
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/xmpp/muc/rooms/{username}/")
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response getRooms(@URIParam("username") String username,
                           @QueryParam("from") Integer from,
                           @QueryParam("to") Integer to,
                           @QueryParam("sort") String sort) {
    if (this.rb == null) loadResourceBundle();
    XMPPSessionImpl session = (XMPPSessionImpl) messenger.getSession(username);
    if (session != null) {
      try {
        return Response.Builder.ok(session.getRooms(from, to,sort), JSON_CONTENT_TYPE).cacheControl(cc).build();
      } catch (XMPPException e) {
        if (log.isDebugEnabled()) 
          e.printStackTrace();
        XMPPError error = e.getXMPPError();
        return Response.Builder.withStatus(error.getCode())
                               .errorMessage(rb.getString("chat.message.conference.info.error") + "\n" + error.getMessage())
                               .build();
      }
    }
    return Response.Builder.withStatus(HTTPStatus.BAD_REQUEST)
                           .errorMessage(rb.getString("chat.message.room.xmppsession.null"))
                           .build();
  }

  /**
   * @param username
   * @param inviter
   * @param room
   * @return
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/xmpp/muc/decline/{username}/{inviter}/")
  public Response declineToRoom(@URIParam("username") String username,
                                @URIParam("inviter") String inviter,
                                @QueryParam("room") String room,
                                @QueryParam("reason") String reason) {
    if (this.rb == null) loadResourceBundle();
    if (room == null)
      return Response.Builder.withStatus(HTTPStatus.BAD_REQUEST)
                             .errorMessage(rb.getString("chat.message.roomid.null"))
                             .build();
    XMPPSession session = messenger.getSession(username);
    if (session != null) {
      session.declineRoom(room, inviter, reason);
      return Response.Builder.ok().cacheControl(cc).build();
    } else {
      if (log.isDebugEnabled())
        log.debug(rb.getString("chat.message.room.xmppsession.null"));
      return Response.Builder.withStatus(HTTPStatus.BAD_REQUEST)
                             .errorMessage(rb.getString("chat.message.room.xmppsession.null"))
                             .build();
    }
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/xmpp/muc/destroy/{username}/")
  public Response destroyRoom(@URIParam("username") String username,
                              @QueryParam("room") String room,
                              @QueryParam("reason") String reason,
                              @QueryParam("altroom") String altRoom) {
    if (this.rb == null) loadResourceBundle();
    if (room == null)
      return Response.Builder.withStatus(HTTPStatus.BAD_REQUEST)
                             .errorMessage(rb.getString("chat.message.roomid.null"))
                             .build();
    XMPPSession session = messenger.getSession(username);
    if (session != null) {
      try {
        if (session.destroyRoom(room, reason, altRoom))
          return Response.Builder.ok().cacheControl(cc).build();
        else
          return Response.Builder.withStatus(HTTPStatus.BAD_REQUEST)
                                 .errorMessage(rb.getString("chat.message.room.not.found"))
                                 .cacheControl(cc)
                                 .build();
      } catch (XMPPException e) {
        if (log.isDebugEnabled()) 
          e.printStackTrace();
        XMPPError error = e.getXMPPError();
        return Response.Builder.withStatus(error.getCode())
                               .errorMessage(rb.getString("chat.message.room.destroy.error") + "\n" + error.getMessage())
                               .build();
      }
    } else {
      if (log.isDebugEnabled())
        log.debug(rb.getString("chat.message.room.xmppsession.null"));
      return Response.Builder.withStatus(HTTPStatus.BAD_REQUEST)
                             .errorMessage(rb.getString("chat.message.room.xmppsession.null"))
                             .build();
    }
  }

  /**
   * @param username
   * @param inviter
   * @param room
   * @return
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/xmpp/muc/invite/{username}/{invitee}/")
  public Response inviteToRoom(@URIParam("username") String username,
                               @URIParam("invitee") String invitee,
                               @QueryParam("room") String room,
                               @QueryParam("reason") String reason) {
    if (this.rb == null) loadResourceBundle();
    if (room == null)
      return Response.Builder.withStatus(HTTPStatus.BAD_REQUEST)
                             .errorMessage(rb.getString("chat.message.roomid.null"))
                             .build();
    XMPPSession session = messenger.getSession(username);
    if (session != null) {
      try {
        if (session.inviteToRoom(room, invitee, reason))
          return Response.Builder.ok().cacheControl(cc).build();
        return Response.Builder.withStatus(HTTPStatus.BAD_REQUEST)//.errorMessage()
                               .cacheControl(cc)
                               .build();
      } catch (XMPPException e) {
//        if (log.isDebugEnabled()) 
          e.printStackTrace();
        XMPPError error = e.getXMPPError();
        return Response.Builder.withStatus(error.getCode())
                               .errorMessage(rb.getString("chat.message.conference.service.error"))
                               .build();
      }
    }
    return Response.Builder.withStatus(HTTPStatus.BAD_REQUEST)
                           .cacheControl(cc)
                           .errorMessage(rb.getString("chat.message.room.xmppsession.null"))
                           .build();
  }

  /**
   * @param username
   * @param room
   * @return
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/xmpp/muc/join/{username}/")
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response joinRoom(@URIParam("username") String username,
                           @QueryParam("room") String room,
                           @QueryParam("nickname") String nickname,
                           @QueryParam("password") String password) {
    if (this.rb == null) loadResourceBundle();
    if (room == null)
      return Response.Builder.withStatus(HTTPStatus.BAD_REQUEST)
                             .errorMessage(rb.getString("chat.message.roomid.null"))
                             .build();
    XMPPSession session = messenger.getSession(username);
    if (session != null) {
      try {
        session.joinRoom(room, nickname, password);
        return Response.Builder.ok().cacheControl(cc).build();
      } catch (XMPPException e) {
        if (log.isDebugEnabled()) 
          e.printStackTrace();
        XMPPError error = e.getXMPPError();
        String em = new String();
        switch (error.getCode()) {
        case 401:
          em = rb.getString("chat.message.password.private.room.error");
          break;
        case 403:
          em = rb.getString("chat.message.you.have.been.banned");
          break;
        case 404:
          em = rb.getString("chat.message.no.room.to.join.error");
          break;
        case 407:
          em = rb.getString("chat.message.room.user.not.member");
          break;
        case 409:
          em = rb.getString("chat.message.room.nickname.already.exist");
        default:
          em = rb.getString("chat.message.default.error");
          break;
        }
        return Response.Builder.withStatus(error.getCode())
                               .errorMessage(em)
                               .build();
      }
    }
    return Response.Builder.withStatus(HTTPStatus.BAD_REQUEST)
                           .errorMessage(rb.getString("chat.message.room.xmppsession.null"))
                           .cacheControl(cc)
                           .build();
  }

  /**
   * @param username
   * @param room
   * @return
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/xmpp/muc/leaveroom/{username}/")
  public Response leftRoom(@URIParam("username") String username,
                           @QueryParam("room") String room) {
    if (this.rb == null) loadResourceBundle();
    if (room == null)
      return Response.Builder.withStatus(HTTPStatus.BAD_REQUEST)
                             .errorMessage(rb.getString("chat.message.roomid.null"))
                             .build();
    XMPPSession session = messenger.getSession(username);
    if (session != null) {
      try {
        if (session.leaveRoom(room))
          return Response.Builder.ok().cacheControl(cc).build();
        return Response.Builder.withStatus(HTTPStatus.BAD_REQUEST)
                               .cacheControl(cc)
                               .build();
      } catch (XMPPException e) {
        if (log.isDebugEnabled()) 
          e.printStackTrace();
        XMPPError error = e.getXMPPError();
        return Response.Builder.withStatus(error.getCode())
                               .errorMessage(rb.getString("chat.message.default.error"))
                               .build();
      }
    }
    return Response.Builder.withStatus(HTTPStatus.BAD_REQUEST)
                           .errorMessage(rb.getString("chat.message.room.xmppsession.null"))
                           .cacheControl(cc)
                           .build();
  }

  /**
   * @param username
   * @param room
   * @param nickname
   * @return
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/xmpp/muc/changenickname/{username}/{nickname}/")
  public Response changeNickname(@URIParam("username") String username,
                                 @QueryParam("nickname") String nickname,
                                 @QueryParam("room") String room) {
    if (this.rb == null) loadResourceBundle();
    if (room == null || nickname == null)
      return Response.Builder.withStatus(HTTPStatus.BAD_REQUEST)
                             .errorMessage(rb.getString("chat.message.roomid.null"))
                             .build();
    XMPPSession session = messenger.getSession(username);
    if (session != null) {
      try {
        session.changeNickname(room, nickname);
        return Response.Builder.ok().cacheControl(cc).build();
      } catch (XMPPException e) {
        if (log.isDebugEnabled()) 
          e.printStackTrace();
        XMPPError error = e.getXMPPError();
        String em = new String();
        switch (error.getCode()) {
        case 409:
          em = rb.getString("chat.message.room.nickname.already.exist");
          break;
        default:
          em = rb.getString("chat.message.default.error");
          break;
        }
        return Response.Builder.withStatus(error.getCode())
                               .errorMessage(em)
                               .build();
      }
    }
    return Response.Builder.withStatus(HTTPStatus.BAD_REQUEST)
                           .errorMessage(rb.getString("chat.message.room.xmppsession.null"))
                           .cacheControl(cc)
                           .build();
  }

  /**
   * @param username
   * @param room
   * @param mode
   * @param status
   * @return
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/xmpp/muc/changestatus/{username}/{mode}/")
  public Response changeAvailabilityStatusInRoom(@URIParam("username") String username,
                                                 @URIParam("mode") String mode,
                                                 @QueryParam("room") String room,
                                                 @QueryParam("status") String status) {
    if (this.rb == null) loadResourceBundle();
    if (room == null)
      return Response.Builder.withStatus(HTTPStatus.BAD_REQUEST)
                             .errorMessage(rb.getString("chat.message.roomid.null"))
                             .build();
    XMPPSession session = messenger.getSession(username);
    if (session != null) {
      try {
        session.changeAvailabilityStatusInRoom(room, status, mode);
        return Response.Builder.ok().cacheControl(cc).build();
      } catch (XMPPException e) {
        if (log.isDebugEnabled()) 
          e.printStackTrace();
        XMPPError error = e.getXMPPError();
        return Response.Builder.withStatus(error.getCode())
                               .errorMessage(rb.getString("chat.message.default.error"))
                               .build();
      }
    }
    return Response.Builder.notFound().errorMessage(rb.getString("chat.message.room.xmppsession.null")).build();
  }

  /**
   * @param username
   * @param room
   * @param subject
   * @return
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/xmpp/muc/changesubject/{username}/")
  public Response changeSubject(@URIParam("username") String username,
                                @QueryParam("room") String room,
                                @QueryParam("subject") String subject) {
    if (this.rb == null) loadResourceBundle();
    if (room == null || subject == null)
      return Response.Builder.withStatus(HTTPStatus.BAD_REQUEST)
                             .errorMessage(rb.getString("chat.message.roomid.null"))
                             .build();
    XMPPSession session = messenger.getSession(username);
    if (session != null) {
      try {
        session.changeSubject(room, subject);
        return Response.Builder.ok().cacheControl(cc).build();
      } catch (XMPPException e) {
        if (log.isDebugEnabled()) 
          e.printStackTrace();
        XMPPError error = e.getXMPPError();
        String em= new String();
        switch (error.getCode()) {
        case 403:
          em = rb.getString("chat.message.subject.change.error");
          break;
        default:
          em = rb.getString("chat.message.default.error");
          break;
        }
        return Response.Builder.withStatus(error.getCode())
                               .errorMessage(error.getMessage())
                               .build();
      }
    }
    return Response.Builder.notFound().errorMessage(rb.getString("chat.message.room.xmppsession.null")).build();
  }

  /**
   * @param username
   * @param room
   * @param nickname
   * @param role
   * @param command
   * @return
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/xmpp/muc/managerole/{username}/")
  public Response manageRoleRoom(@URIParam("username") String username,
                                 @QueryParam("room") String room,
                                 @QueryParam("nickname") String nickname,
                                 @QueryParam("role") String role,
                                 @QueryParam("command") String command) {
    if (this.rb == null) loadResourceBundle();
    if (room == null || nickname == null)
      return Response.Builder.withStatus(HTTPStatus.BAD_REQUEST)
                             .errorMessage(rb.getString("chat.message.roomid.null"))
                             .build();
    XMPPSession session = messenger.getSession(username);
    if (session != null) {
      try {
          session.manageRole(room, nickname, role, command);
          return Response.Builder.ok().cacheControl(cc).build();
      } catch (XMPPException e) {
        if (log.isDebugEnabled()) 
          e.printStackTrace();
        XMPPError error = e.getXMPPError();
        String em= new String();
        switch (error.getCode()) {
        case 403:
          em = rb.getString("chat.message.forbidden.error");
          break;
        case 400:
          em = rb.getString("chat.message.user.not.found");
          break;
        default:
          em = rb.getString("chat.message.default.error");
          break;
        }
        return Response.Builder.withStatus(error.getCode())
                               .errorMessage(em)
                               .build();
      }
    } else {
      if (log.isDebugEnabled())
        log.debug("Sesion is null");
      return Response.Builder.withStatus(HTTPStatus.BAD_REQUEST)
                             .errorMessage(rb.getString("chat.message.room.xmppsession.null"))
                             .cacheControl(cc)
                             .build();
    }
  }

  /**
   * @param username
   * @param room
   * @param nickname
   * @param affiliation
   * @param command
   * @return
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/xmpp/muc/manageaffiliation/{username}/")
  public Response manageAffilationRoom(@URIParam("username") String username,
                                       @QueryParam("room") String room,
                                       @QueryParam("nickname") String nickname,
                                       @QueryParam("affiliation") String affiliation,
                                       @QueryParam("command") String command) {
    if (this.rb == null) loadResourceBundle();
    if (room == null || nickname == null)
      return Response.Builder.withStatus(HTTPStatus.BAD_REQUEST)
                             .errorMessage(rb.getString("chat.message.roomid.null"))
                             .build();
    XMPPSession session = messenger.getSession(username);
    if (session != null) {
      try {
          session.manageAffiliation(room, nickname, affiliation, command);
          return Response.Builder.ok().cacheControl(cc).build();
      } catch (XMPPException e) {
        if (log.isDebugEnabled()) 
          e.printStackTrace();
        XMPPError error = e.getXMPPError();
        String em = new String();
        switch (error.getCode()) {
        case 403:
          em = rb.getString("chat.message.forbidden.error");
          break;
        case 400:
          em = rb.getString("chat.message.user.not.found");
          break;
        default:
          em = rb.getString("chat.message.default.error");
          break;
        }
        return Response.Builder.withStatus(error.getCode())
                               .errorMessage(em)
                               .build();
      }
    } else {
      if (log.isDebugEnabled())
        log.debug("Sesion is null");
      return Response.Builder.withStatus(HTTPStatus.NOT_FOUND)
                             .errorMessage(rb.getString("chat.message.room.xmppsession.null"))
                             .cacheControl(cc)
                             .build();
    }
  }

  /**
   * @param username
   * @param room
   * @param nickname
   * @param reason
   * @return
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/xmpp/muc/kick/{username}/")
  public Response kickUserFromRoom(@URIParam("username") String username,
                                   @QueryParam("room") String room,
                                   @QueryParam("nickname") String nickname,
                                   @QueryParam("reason") String reason) {
    if (this.rb == null) loadResourceBundle();
    if (room == null || nickname == null)
      return Response.Builder.withStatus(HTTPStatus.BAD_REQUEST)
                             .errorMessage(rb.getString("chat.message.roomid.null"))
                             .build();
    XMPPSession session = messenger.getSession(username);
    if (session != null) {
      try {
          session.kickUser(room, nickname, reason);
          return Response.Builder.ok().cacheControl(cc).build();
      } catch (XMPPException e) {
        if (log.isDebugEnabled()) 
          e.printStackTrace();
        XMPPError error = e.getXMPPError();
        String em = new String();
        switch (error.getCode()) {
        case 405:
          em = rb.getString("chat.message.not.allowed.error");
        case 403:
          em = rb.getString("chat.message.forbidden.error");
          break;
        case 400:
          em = rb.getString("chat.message.user.not.found");
          break;
        default:
          em = rb.getString("chat.message.default.error");
          break;
        }
        return Response.Builder.withStatus(error.getCode())
                               .errorMessage(em)
                               .build();
      }
    } else {
      if (log.isDebugEnabled())
        log.debug("Sesion is null");
      return Response.Builder.withStatus(HTTPStatus.NOT_FOUND)
                             .errorMessage(rb.getString("chat.message.room.xmppsession.null"))
                             .cacheControl(cc)
                             .build();
    }
  }

  /**
   * @param username
   * @param room
   * @param name
   * @param reason
   * @return
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/xmpp/muc/ban/{username}/")
  public Response banUserFromRoom(@URIParam("username") String username,
                                  @QueryParam("room") String room,
                                  @QueryParam("name") String name,
                                  @QueryParam("reason") String reason) {
    if (this.rb == null) loadResourceBundle();
    if (room == null || name ==null)
      return Response.Builder.withStatus(HTTPStatus.BAD_REQUEST)
                             .errorMessage(rb.getString("chat.message.roomid.null"))
                             .build();
    XMPPSession session = messenger.getSession(username);
    if (session != null) {
      try {
          session.banUser(room, name, reason);
          return Response.Builder.ok().cacheControl(cc).build();
      } catch (XMPPException e) {
        if (log.isDebugEnabled()) 
          e.printStackTrace();
        XMPPError error = e.getXMPPError();
        String em = new String();
        switch (error.getCode()) {
        case 405:
          em = rb.getString("chat.message.not.allowed.error");
        case 403:
          em = rb.getString("chat.message.forbidden.error");
          break;
        case 400:
          em = rb.getString("chat.message.user.not.found");
          break;
        default:
          em = rb.getString("chat.message.default.error");
          break;
        }
        return Response.Builder.withStatus(error.getCode())
                               .errorMessage(em)
                               .build();
      }
    } else {
      if (log.isDebugEnabled())
        log.debug(rb.getString("chat.message.room.xmppsession.null"));
      return Response.Builder.withStatus(HTTPStatus.NOT_FOUND)
                             .errorMessage(rb.getString("chat.message.room.xmppsession.null"))
                             .cacheControl(cc)
                             .build();
    }
  }

  // TODO may be change
  /**
   * @param username
   * @param remoteUser
   * @param remotePassword
   * @param transport
   * @return
   */
  @HTTPMethod("POST")
  @URITemplate("/xmpp/addtransport/")
  public Response addTransport(@QueryParam("username") String username,
                               @QueryParam("remoteusername") String remoteUser,
                               @QueryParam("remotepassword") String remotePassword,
                               @QueryParam("transport") String transport) {
    if (this.rb == null) loadResourceBundle();
    try {
      XMPPSession session = messenger.getSession(username);
      if (session != null) {
        String serviceName = session.getConnection().getServiceName();
        if (transport.equalsIgnoreCase("yahoo")) {
          YahooTransport yahooTransport = new YahooTransport(serviceName);
          session.addTransport(yahooTransport, remoteUser, remotePassword, true);
        } else if (transport.equalsIgnoreCase("icq")) {
          ICQTransport icqTransport = new ICQTransport(serviceName);
          session.addTransport(icqTransport, remoteUser, remotePassword, true);
        } else if (transport.equalsIgnoreCase("msn")) {
          MSNTransport msnTransport = new MSNTransport(serviceName);
          session.addTransport(msnTransport, remoteUser, remotePassword, true);
        } else if (transport.equalsIgnoreCase("aim")) {
          AIMTransport aimTransport = new AIMTransport(serviceName);
          session.addTransport(aimTransport, remoteUser, remotePassword, true);
        } else if (transport.equalsIgnoreCase("gtalk")) {
          GtalkTransport gtalkTransport = new GtalkTransport(serviceName);
          session.addTransport(gtalkTransport, remoteUser, remotePassword, true);
        } else {
          if (log.isDebugEnabled())
            log.debug("Wrong transport name!");
        }
        return Response.Builder.ok().cacheControl(cc).build();
      } else {
        if (log.isDebugEnabled())
          log.debug(rb.getString("chat.message.room.xmppsession.null"));
        return Response.Builder.withStatus(HTTPStatus.FORBIDDEN)
                               .errorMessage(rb.getString("chat.message.room.xmppsession.null"))
                               .build();
      }
    } catch (Exception e) {
      if (log.isDebugEnabled()) 
        e.printStackTrace();
      return Response.Builder.withStatus(HTTPStatus.BAD_REQUEST)
                             .errorMessage(e.getMessage())
                             .build();
    }
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/xmpp/roster/add/{username}/{adduser}/")
  public Response addBoddyToRoster(@URIParam("username") String username,
                                   @URIParam("adduser") String adduser,
                                   @QueryParam("nickname") String nickname,
                                   @QueryParam("group") String group) {
    if (this.rb == null) loadResourceBundle();
    XMPPSession session = messenger.getSession(username);
    try {
      if (session != null) {
        if (nickname == null)
          nickname = adduser;
        session.addBuddy(adduser, nickname, group);
        return Response.Builder.ok().cacheControl(cc).build();
      } else {
        if (log.isDebugEnabled())
          log.debug(rb.getString("chat.message.room.xmppsession.null"));
        return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR).build();
      }
    } catch (XMPPException e) {
      if (log.isDebugEnabled()) 
        e.printStackTrace();
      XMPPError error = e.getXMPPError();
      return Response.Builder.withStatus(error.getCode())
                             .errorMessage(rb.getString("chat.message.default.error"))
                             .build();
    }
  }

  /**
   * @param username
   * @param upduser
   * @param nickname
   * @param group
   * @return
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/xmpp/roster/update/{username}/{upduser}/")
  public Response updateBoddy(@URIParam("username") String username,
                              @URIParam("upduser") String upduser,
                              @QueryParam("nickname") String nickname,
                              @QueryParam("group") String group) {
    if (this.rb == null) loadResourceBundle();
    XMPPSession session = messenger.getSession(username);
    try {
      if (session != null) {
        if (nickname == null)
          nickname = upduser;
        session.updateBuddy(upduser, nickname, group);
        return Response.Builder.ok().cacheControl(cc).build();
      } else {
        return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR).build();
      }
    } catch (XMPPException e) {
      if (log.isDebugEnabled()) 
        e.printStackTrace();
      XMPPError error = e.getXMPPError();
      return Response.Builder.withStatus(error.getCode())
                             .errorMessage(rb.getString("chat.message.default.error"))
                             .build();
    }
  }

  /**
   * @param username
   * @param group
   * @return
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/xmpp/roster/group/{username}/{group}/")
  public Response createGroup(@URIParam("username") String username,
                              @URIParam("group") String group) { 
    if (this.rb == null) loadResourceBundle();
    XMPPSessionImpl session = (XMPPSessionImpl) messenger.getSession(username);
    if (session != null) {
      session.createGroup(group);
      return Response.Builder.ok().cacheControl(cc).build();
    } else {
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR).build();
    }
  }

  /**
   * @param _username
   * @param _askuser
   * @return
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/xmpp/askforsubscription/{username}/{askuser}/")
  public Response askForSubscription(@URIParam("username") String username,
                                     @URIParam("askuser") String askuser,
                                     @QueryParam("nickname") String nickname) {
    if (this.rb == null) loadResourceBundle();
    XMPPSession session = messenger.getSession(username);
    if (session != null) {
      if (session.getBuddy(askuser) == null)
        return Response.Builder.withStatus(HTTPStatus.BAD_REQUEST)
                               .errorMessage(rb.getString("chat.message.user.not.found"))
                               .build();
        if (nickname == null)
          nickname = askuser;
        session.askForSubscription(askuser, nickname);
        return Response.Builder.ok().cacheControl(cc).build();
      } else {
        if (log.isDebugEnabled())
          log.debug(rb.getString("chat.message.room.xmppsession.null"));
        return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR).build();
      }
   }

  /**
   * @param username
   * @return
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/xmpp/rosterclean/{username}/")
  public Response cleanBuddylist(@URIParam("username") String username) {
    if (this.rb == null) loadResourceBundle();
    try {
      XMPPSession session = messenger.getSession(username);
      if (session != null) {
        session.cleanBuddiesList();
        return Response.Builder.ok().cacheControl(cc).build();
      } else {
        if (log.isDebugEnabled())
          log.debug(rb.getString("chat.message.room.xmppsession.null"));
        return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
                               .errorMessage(rb.getString("chat.message.room.xmppsession.null"))
                               .build();
      }
    } catch (XMPPException e) {
      if (log.isDebugEnabled()) 
        e.printStackTrace();
      XMPPError error = e.getXMPPError();
      return Response.Builder.withStatus(error.getCode())
                             .errorMessage(rb.getString("chat.message.default.error"))
                             .build();
    }
  }

  /**
   * @param username
   * @param jid
   * @return
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/xmpp/history/getmessages/{usernameto}/{isGroupChat}/")
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response getAllHistory(@URIParam("usernameto") String usernameto,
                                @URIParam("isGroupChat") Boolean isGroupChat,
                                @QueryParam("usernamefrom") String usernamefrom) {
    if (this.rb == null) loadResourceBundle();
    if (usernamefrom == null || usernamefrom.length() == 0)
      return Response.Builder.withStatus(HTTPStatus.BAD_REQUEST)
                              .errorMessage(rb.getString("chat.message.history.participant.name.not.set"))
                              .build();
    try {
      XMPPSession session = messenger.getSession(usernameto);
      if (session != null) {
        List<HistoricalMessage> list = new ArrayList<HistoricalMessage>();
        list = session.getAllHistory(usernameto, usernamefrom, isGroupChat);
        List<MessageBean> listBean = new ArrayList<MessageBean>();
        if (!list.isEmpty()) {
          for (HistoricalMessage historicalMessage : list) {
            listBean.add(TransformUtils.messageToBean(historicalMessage));
          }
        }
        return Response.Builder.ok(new MessageListBean(listBean), JSON_CONTENT_TYPE)
                               .cacheControl(cc)
                               .build();
      } else
        return Response.Builder.withStatus(HTTPStatus.FORBIDDEN).build();
    } catch (Exception e) {
      if (log.isDebugEnabled())
       e.printStackTrace();
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR).build();
    }
  }

  /**
   * @param username
   * @param jid
   * @param dateformat
   * @param from
   * @param to
   * @return
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/xmpp/history/getmessages/{usernameto}/{isGroupChat}/{dateformat}/{from}/{to}/")
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response getHistoryBetweenDate(@URIParam("usernameto") String usernameto,
                                        @URIParam("isGroupChat") Boolean isGroupChat,
                                        @URIParam("dateformat") String dateformat,
                                        @URIParam("from") String from,
                                        @URIParam("to") String to,
                                        @QueryParam("usernamefrom") String usernamefrom) {
    if (this.rb == null) loadResourceBundle();
    if (usernamefrom == null || usernamefrom.length() == 0)
      return Response.Builder.withStatus(HTTPStatus.BAD_REQUEST)
                              .errorMessage(rb.getString("chat.message.history.participant.name.not.set"))
                              .build();
    try {
      XMPPSession session = messenger.getSession(usernameto);
      if (session != null) {
        List<HistoricalMessage> list = new ArrayList<HistoricalMessage>();
        DateFormat dateFormat = new SimpleDateFormat(dateformat);
        Date dateFrom = dateFormat.parse(from);
        Date dateTo = dateFormat.parse(to);
        List<MessageBean> listBean = new ArrayList<MessageBean>();
        if (dateFrom.before(dateTo)) {
          list = session.getHistoryBetweenDate(usernameto,
                                               usernamefrom,
                                               isGroupChat,
                                               dateFrom,
                                               dateTo);
          if (!list.isEmpty()) {
            for (HistoricalMessage historicalMessage : list) {
              listBean.add(TransformUtils.messageToBean(historicalMessage));
            }
          }
          return Response.Builder.ok(new MessageListBean(listBean), JSON_CONTENT_TYPE)
                                 .cacheControl(cc)
                                 .build();
        } else {
          return Response.Builder.withStatus(HTTPStatus.CONFLICT).build();
        }
      } else
        return Response.Builder.withStatus(HTTPStatus.FORBIDDEN).build();
    } catch (Exception e) {
      if (log.isDebugEnabled())
        e.printStackTrace();
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR).build();
    }
  }

  /**
   * @param username
   * @param jid
   * @param dateformat
   * @param from
   * @return
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/xmpp/history/getmessages/{usernameto}/{isGroupChat}/{dateformat}/{from}/")
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response getHistoryFromDateToNow(@URIParam("usernameto") String usernameto,
                                          @URIParam("isGroupChat") Boolean isGroupChat,
                                          @URIParam("dateformat") String dateformat,
                                          @URIParam("from") String from,
                                          @QueryParam("usernamefrom") String usernamefrom) {
    if (this.rb == null) loadResourceBundle();
    if (usernamefrom == null || usernamefrom.length() == 0)
      return Response.Builder.withStatus(HTTPStatus.BAD_REQUEST)
                              .errorMessage(rb.getString("chat.message.history.participant.name.not.set"))
                              .build();
    try {
      XMPPSession session = messenger.getSession(usernameto);
      if (session != null) {
        List<HistoricalMessage> list = new ArrayList<HistoricalMessage>();
        DateFormat dateFormat = new SimpleDateFormat(dateformat);
        Date dateFrom = dateFormat.parse(from);
        List<MessageBean> listBean = new ArrayList<MessageBean>();
        if (dateFrom.before(Calendar.getInstance().getTime())) {
          list = session.getHistoryFromDateToNow(usernameto, usernamefrom, isGroupChat, dateFrom);
          if (!list.isEmpty()) {
            for (HistoricalMessage historicalMessage : list) {
              listBean.add(TransformUtils.messageToBean(historicalMessage));
            }
          }
          return Response.Builder.ok(new MessageListBean(listBean), JSON_CONTENT_TYPE)
                                 .cacheControl(cc)
                                 .build();
        } else {
          return Response.Builder.withStatus(HTTPStatus.CONFLICT).build();
        }
      } else
        return Response.Builder.withStatus(HTTPStatus.FORBIDDEN).build();
    } catch (Exception e) {
      if (log.isDebugEnabled())
        e.printStackTrace();
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR).build();
    }
  }

  /**
   * @param username
   * @return
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/xmpp/history/getinterlocutors/{username}/")
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response getInterlocutors(@URIParam("username") String username) {
    if (this.rb == null) loadResourceBundle();
    try {
      XMPPSession session = messenger.getSession(username);
      if (session != null) {
        return Response.Builder.ok(new InterlocutorListBean(session.getInterlocutors(username)),
                                   JSON_CONTENT_TYPE).cacheControl(cc).build();
      } else
        return Response.Builder.withStatus(HTTPStatus.FORBIDDEN).build();
    } catch (Exception e) {
      if (log.isDebugEnabled())
        e.printStackTrace();
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR).build();
    }
  }

  /**
   * @param username
   * @param jid
   * @return
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/xmpp/history/file/getmessages/{usernameto}/{isGroupChat}/")
  @OutputTransformer(PassthroughOutputTransformer.class)
  public Response getAllHistoryFile(@URIParam("usernameto") String usernameto,
                                    @URIParam("isGroupChat") Boolean isGroupChat,
                                    @QueryParam("usernamefrom") String usernamefrom) {
    if (this.rb == null) loadResourceBundle();
    if (usernamefrom == null || usernamefrom.length() == 0)
      return Response.Builder.withStatus(HTTPStatus.BAD_REQUEST)
                              .errorMessage(rb.getString("chat.message.history.participant.name.not.set"))
                              .build();
    try {
      //XMPPSession session = messenger.getSession(usernameto);
      //For CS-2668
      XMPPSession session = messenger.getSession(usernamefrom);
      if (session != null) {
        List<HistoricalMessage> list = new ArrayList<HistoricalMessage>();
        list = session.getAllHistory(usernameto, usernamefrom, isGroupChat);
        InputStream inputStream = historyBeanToStream(list);
        return Response.Builder.ok(inputStream, DEFAULT_CONTENT_TYPE)
                               .header("Content-disposition",
                                       "attachment; filename=" + usernameto + "-" + usernamefrom
                                           + ".txt")
                               .cacheControl(cc)
                               .build();
      } else
        return Response.Builder.withStatus(HTTPStatus.FORBIDDEN).build();
    } catch (Exception e) {
      if (log.isDebugEnabled())
        e.printStackTrace();
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR).build();
    }
  }

  /**
   * @param username
   * @param jid
   * @param dateformat
   * @param from
   * @return
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/xmpp/history/file/getmessages/{usernameto}/{isGroupChat}/{dateformat}/{from}/")
  @OutputTransformer(PassthroughOutputTransformer.class)
  public Response getHistoryFromDateToNowFile(@URIParam("usernameto") String usernameto,
                                              @URIParam("isGroupChat") Boolean isGroupChat,
                                              @URIParam("dateformat") String dateformat,
                                              @URIParam("from") String from,
                                              @QueryParam("usernamefrom") String usernamefrom) {
    if (this.rb == null) loadResourceBundle();
    try {
      //XMPPSession session = messenger.getSession(usernameto);
    //For CS-2668
      XMPPSession session = messenger.getSession(usernamefrom);
      if (session != null) {
        List<HistoricalMessage> list = new ArrayList<HistoricalMessage>();
        DateFormat dateFormat = new SimpleDateFormat(dateformat);
        Date dateFrom = dateFormat.parse(from);
        if (dateFrom.before(Calendar.getInstance().getTime())) {
          list = session.getHistoryFromDateToNow(usernameto, usernamefrom, isGroupChat, dateFrom);
          InputStream inputStream = historyBeanToStream(list);
          return Response.Builder.ok(inputStream, DEFAULT_CONTENT_TYPE)
                                 .header("Content-disposition",
                                         "attachment; filename=" + usernameto + "-" + usernamefrom
                                             + "(" + from + ").txt")
                                 .cacheControl(cc)
                                 .build();
        } else {
          return Response.Builder.withStatus(HTTPStatus.CONFLICT).build();
        }
      } else
        return Response.Builder.withStatus(HTTPStatus.FORBIDDEN).build();
    } catch (Exception e) {
      if (log.isDebugEnabled())
        e.printStackTrace();
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR).build();
    }
  }

  /**
   * @param username
   * @param jid
   * @param dateformat
   * @param from
   * @param to
   * @return
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/xmpp/history/file/getmessages/{usernameto}/{isGroupChat}/{dateformat}/{from}/{to}/")
  @OutputTransformer(PassthroughOutputTransformer.class)
  public Response getHistoryBetweenDateFile(@URIParam("usernameto") String usernameto,
                                            @URIParam("isGroupChat") Boolean isGroupChat,
                                            @URIParam("dateformat") String dateformat,
                                            @URIParam("from") String from,
                                            @URIParam("to") String to,
                                            @QueryParam("usernamefrom") String usernamefrom) {
    if (this.rb == null) loadResourceBundle();
    try {
      //XMPPSession session = messenger.getSession(usernameto);
    //For CS-2668
      XMPPSession session = messenger.getSession(usernamefrom);
      if (session != null) {
        List<HistoricalMessage> list = new ArrayList<HistoricalMessage>();
        DateFormat dateFormat = new SimpleDateFormat(dateformat);
        Date dateFrom = dateFormat.parse(from);
        Date dateTo = dateFormat.parse(to);
        if (dateFrom.before(dateTo)) {
          list = session.getHistoryBetweenDate(usernameto,
                                               usernamefrom,
                                               isGroupChat,
                                               dateFrom,
                                               dateTo);
          InputStream inputStream = historyBeanToStream(list);
          return Response.Builder.ok(inputStream, DEFAULT_CONTENT_TYPE)
                                 .header("Content-disposition",
                                         "attachment; filename=" + usernameto + "-" + usernamefrom
                                             + "(" + from + "-" + to + ").txt")
                                 .cacheControl(cc)
                                 .build();
        } else {
          return Response.Builder.withStatus(HTTPStatus.CONFLICT).build();
        }
      } else
        return Response.Builder.withStatus(HTTPStatus.FORBIDDEN).build();
    } catch (Exception e) {
      if (log.isDebugEnabled())
        e.printStackTrace();
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR).build();
    }
  }

  /**
   * @param history
   * @return
   */
  private InputStream historyBeanToStream(List<HistoricalMessage> history) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try {
      if (!history.isEmpty()) {
        for (HistoricalMessage historicalMessage : history) {
          String buffer = new String();
          buffer = buffer.concat(StringUtils.parseBareAddress(historicalMessage.getFrom()) + "("
              + historicalMessage.getDateSend().toString() + ") : " + historicalMessage.getBody()
              + "\n");
          outputStream.write(buffer.getBytes());
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
    return inputStream;
  }

  /**
   * @param username
   * @param searchService
   * @return
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/xmpp/getsearchform/{username}/")
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response getSearchUsersForm(@URIParam("username") String username,
                                     @QueryParam(SearchFormFields.SEARCH_SERVICE) String searchService) {
    if (this.rb == null) loadResourceBundle();
    try {
      XMPPSession session = messenger.getSession(username);
      if (session != null) {
        Form form = session.getSearchForm(searchService);
        return Response.Builder.ok(TransformUtils.formToFormBean(form), JSON_CONTENT_TYPE)
                               .cacheControl(cc)
                               .build();
      }
      return Response.Builder.withStatus(HTTPStatus.FORBIDDEN)
                             .cacheControl(cc)
                             .errorMessage(rb.getString("chat.message.xmppsession.null"))
                             .build();
    } catch (XMPPException e) {
      if (log.isDebugEnabled()) 
        e.printStackTrace();
      XMPPError error = e.getXMPPError();
      return Response.Builder.withStatus(error.getCode())
                             .errorMessage(rb.getString("chat.message.default.error"))
                             .build();
    }
  }

  /**
   * @param username
   * @param needinfo
   * @return
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/xmpp/getuserinfo/{username}/{needinfo}/")
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response getUserInfo(@URIParam("username") String username,
                              @URIParam("needinfo") String needinfo) {
    if (this.rb == null) loadResourceBundle();
    try {
      XMPPSession session = messenger.getSession(username);
      if (session != null) {
        return Response.Builder.ok(session.getUserInfo(needinfo), JSON_CONTENT_TYPE)
                               .cacheControl(cc)
                               .build();
      } else
        return Response.Builder.withStatus(HTTPStatus.FORBIDDEN).build();
    } catch (Exception e) {
      if (log.isDebugEnabled()) 
        e.printStackTrace();
      return Response.Builder.badRequest().errorMessage(rb.getString("chat.message.default.error")).build();
    }
  }

   

  /**
   * @param forcache
   * @return
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/xmpp/login2/{forcache}/")
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response login2(@URIParam("forcache") String forcache) {
    if (this.rb == null) loadResourceBundle();
    try {
      // log.info("Random number for cache problem: " + forcache);
      ConversationState curentState = ConversationState.getCurrent();
      String username = curentState.getIdentity().getUserId();
      if (log.isDebugEnabled())
        log.info("Userid for login : " + username);
      String password = organization.getOrganizationService()
                                    .getUserHandler()
                                    .findUserByName(username)
                                    .getPassword();
      messenger.login(username, password, organization, delegate, history,rb);
      XMPPSession session = messenger.getSession(username);
      XMPPConnection connection = session.getConnection();
      String mainServiceName = session.getConnection().getServiceName();
      List<String> services = session.getSearchServices();
      Form form = session.getSearchForm(services.get(0));
      Roster buddyList = session.getConnection().getRoster();
      // ////// Temporary //////////
      session.removeAllTransport();
      // ///////////////////////////
      Collection<String> collectionMUCService = MultiUserChat.getServiceNames(connection);
      String mucService = collectionMUCService.toArray()[0].toString();
      Collection<HostedRoom> hostedRooms = MultiUserChat.getHostedRooms(connection, mucService);
      List<HostedRoomBean> rooms = new ArrayList<HostedRoomBean>();
      for (HostedRoom hostedRoom : hostedRooms) {
        HostedRoomBean roomBean = new HostedRoomBean();
        roomBean.setJid(hostedRoom.getJid());
        roomBean.setName(hostedRoom.getName());
        // RoomInfo roomInfo = MultiUserChat.getRoomInfo(connection,
        // hostedRoom.getJid());
        // if (roomInfo != null) {
        // roomBean.setDescription(roomInfo.getDescription());
        // }
        rooms.add(roomBean);
      }
      InitInfoBean initInfoBean = new InitInfoBean();
      initInfoBean.setForm(TransformUtils.formToFormBean(form));
      initInfoBean.setMainServiceName(mainServiceName);
      initInfoBean.setMucServicesNames(collectionMUCService);
      initInfoBean.setRoster(TransformUtils.rosterToRosterBean(buddyList));
      
      
      try {
        List<ContactBean> list = new ArrayList<ContactBean>() ;
        for (ContactBean b : initInfoBean.getRoster()) {
          UserInfo info = session.getUserInfo(b.getUser().split("@")[0]) ;
          b.setFullName(info.getFirstName() + " " + info.getLastName()) ;
          list.add(b) ;
        }
        initInfoBean.setRoster(list) ;
      } catch (Exception e) { }
  
      initInfoBean.setSearchServicesNames(services);
      initInfoBean.setHostedRooms(rooms);
      initInfoBean.setTotalRooms(rooms.size());
      // TODO: temper temporarily comment until we not have confirmation about
      // receive messages
      // initInfoBean.setMessages(session.getNotRecieveMessages());
      return Response.Builder.ok(initInfoBean, JSON_CONTENT_TYPE)
                             .cacheControl(cc)
                             .header("Set-Cookie", "userTicket=" + UUID.randomUUID().toString())
                             .build();
    } catch (XMPPException e) {
      if (log.isDebugEnabled()) 
        e.printStackTrace();
      XMPPError error = e.getXMPPError();
      return Response.Builder.withStatus(error.getCode())
                             .errorMessage(error.getMessage())
                             .build();
    } catch (Exception e) {
      if (log.isDebugEnabled())
        e.printStackTrace();
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
                             .errorMessage("Thrown exception : " + e)
                             .build();
    }
  }

  /**
   * @param _username
   * @return
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/xmpp/logout/{username}/")
  public Response logout(@URIParam("username") String _username) {
    if (this.rb == null) loadResourceBundle();
    try {
      XMPPSession session =    messenger.getSession(_username);
      if (session != null) session.removeAllTransport();
      messenger.logout(_username);
      return Response.Builder.ok().cacheControl(cc).build();
    } catch (XMPPException e) {
      if (log.isDebugEnabled()) 
        e.printStackTrace();
      XMPPError error = e.getXMPPError();
      return Response.Builder.withStatus(error.getCode())
                             .errorMessage(error.getMessage())
                             .build();
    }
  }

  /**
   * @param username
   * @param needinfo
   * @return
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/xmpp/history/messagereceive/{username}/{messageid}/")
  public Response messageReceive(@URIParam("username") String username,
                                 @URIParam("messageid") String messageId) {
    if (this.rb == null) loadResourceBundle();
    try {
      XMPPSession session = messenger.getSession(username);
      if (session != null) {
        session.messageReceive(messageId);
        return Response.Builder.ok().cacheControl(cc).build();
      } else
        return Response.Builder.withStatus(HTTPStatus.FORBIDDEN).build();
    } catch (Exception e) {
      if (log.isDebugEnabled())
        e.printStackTrace();
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR).build();
    }
  }

  /**
   * @param username
   * @param removeboddy
   * @return
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/xmpp/roster/del/{username}/{removeboddy}/")
  public Response removeBuddy(@URIParam("username") String username,
                              @URIParam("removeboddy") String removeboddy) {
    if (this.rb == null) loadResourceBundle();
    try {
      XMPPSession session = messenger.getSession(username);
      if (session != null) {
        if (session.removeBuddy(removeboddy)) {
          return Response.Builder.ok().cacheControl(cc).build();
        } else {
          return Response.Builder.withStatus(HTTPStatus.NOT_FOUND)
                                 .errorMessage(rb.getString("chat.message.user.not.found"))
                                 .build();
        }
      } else {
        return Response.Builder.withStatus(HTTPStatus.FORBIDDEN)
                               .errorMessage(rb.getString("chat.message.xmppsession.null"))
                               .build();
      }
    } catch (XMPPException e) {
      if (log.isDebugEnabled()) 
        e.printStackTrace();
      XMPPError error = e.getXMPPError();
      return Response.Builder.withStatus(error.getCode())
                             .errorMessage(rb.getString("chat.message.default.error"))
                             .build();
    }
  }

  /**
   * @param username
   * @param _transport
   * @return
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/xmpp/removetransport/{username}/{transport}/")
  public Response removeTransport(@URIParam("username") String username,
                                  @URIParam("transport") String _transport) {
    if (this.rb == null) loadResourceBundle();
    try {
      XMPPSession session = messenger.getSession(username);
      if (session != null) {
        String serviceName = session.getConnection().getServiceName();
        if (_transport.equalsIgnoreCase("yahoo")) {
          YahooTransport yahooTransport = new YahooTransport(serviceName);
          session.removeTransport(yahooTransport);
        } else if (_transport.equalsIgnoreCase("icq")) {
          ICQTransport icqTransport = new ICQTransport(serviceName);
          session.removeTransport(icqTransport);
        } else if (_transport.equalsIgnoreCase("msn")) {
          MSNTransport msnTransport = new MSNTransport(serviceName);
          session.removeTransport(msnTransport);
        } else if (_transport.equalsIgnoreCase("aim")) {
          AIMTransport aimTransport = new AIMTransport(serviceName);
          session.removeTransport(aimTransport);
        } else if (_transport.equalsIgnoreCase("gtalk")) {
          GtalkTransport gtalkTransport = new GtalkTransport(serviceName);
          session.removeTransport(gtalkTransport);
        }
        return Response.Builder.ok().cacheControl(cc).build();
      } else {
        return Response.Builder.withStatus(HTTPStatus.FORBIDDEN)
                               .errorMessage("sesion is null")
                               .build();
      }
    } catch (XMPPException e) {
      if (log.isDebugEnabled()) 
        e.printStackTrace();
      XMPPError error = e.getXMPPError();
      return Response.Builder.withStatus(error.getCode())
                             .errorMessage(error.getMessage())
                             .build();
    }
  }

  /**
   * @param username
   * @param search
   * @param byUsername
   * @param byName
   * @param byEmail
   * @param searchService
   * @return
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/xmpp/searchuser/{username}/")
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response searchUsers(@URIParam("username") String username,
                              @QueryParam(SearchFormFields.SEARCH) String search,
                              @QueryParam(SearchFormFields.USERNAME) Boolean byUsername,
                              @QueryParam(SearchFormFields.NAME) Boolean byName,
                              @QueryParam(SearchFormFields.EMAIL) Boolean byEmail,
                              @QueryParam(SearchFormFields.SEARCH_SERVICE) String searchService) {
    if (this.rb == null) loadResourceBundle();
    try {
      if (byUsername == null)
        byUsername = new Boolean(false);
      if (byName == null)
        byName = new Boolean(false);
      if (byEmail == null)
        byEmail = new Boolean(false);
      XMPPSession session = messenger.getSession(username);
      if (session != null) {
        ReportedData reportedData = session.searchUser(search,
                                                       byUsername,
                                                       byName,
                                                       byEmail,
                                                       searchService);
        return Response.Builder.ok(TransformUtils.reportedSateToSearchResultsBean(reportedData),
                                   JSON_CONTENT_TYPE).cacheControl(cc).build();
      }
      return Response.Builder.withStatus(HTTPStatus.FORBIDDEN).errorMessage("session null").build();
    } catch (XMPPException e) {
      if (log.isDebugEnabled()) 
        e.printStackTrace();
      XMPPError error = e.getXMPPError();
      return Response.Builder.withStatus(error.getCode())
                             .errorMessage(rb.getString("chat.message.default.error"))
                             .build();
    }
  }

  /**
   * @param username
   * @param messageBean
   * @return
   */
  @HTTPMethod("POST")
  @URITemplate("/xmpp/sendmessage/{username}/")
  @InputTransformer(Json2BeanInputTransformer.class)
  public Response sendMessage(@URIParam("username") String username, MessageBean messageBean) {
    if (this.rb == null) loadResourceBundle();
      XMPPSession session = messenger.getSession(username);
      if (session != null) {
        String from = session.getUsername().split("/")[0];
        Message message = new Message(messageBean.getTo(), Message.Type.chat);
        message.setFrom(from);
        message.setBody(messageBean.getBody());
        session.sendMessage(message);
        return Response.Builder.ok().cacheControl(cc).build();
      } else {
        return Response.Builder.withStatus(HTTPStatus.BAD_REQUEST)
                               .errorMessage(rb.getString("chat.message.room.xmppsession.null"))
                               .build();
      }
  }

  /**
   * @param username
   * @param messageBean
   * @return
   */
  @HTTPMethod(HTTPMethods.POST)
  @URITemplate("/xmpp/muc/sendmessage/{username}/")
  @InputTransformer(Json2BeanInputTransformer.class)
  public Response sendMUCMessage(@URIParam("username") String username, 
                                 MessageBean messageBean) {
    if (this.rb == null) loadResourceBundle();
    try {
      String room = messageBean.getTo();
      String body = messageBean.getBody();
      XMPPSession session = messenger.getSession(username);
      if (session != null) {
        session.sendMessageToMUC(room, body);
        return Response.Builder.ok().cacheControl(cc).build();
      } else {
        return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR).build();
      }
    } catch (XMPPException e) {
      if (log.isDebugEnabled()) 
        e.printStackTrace();
      XMPPError error = e.getXMPPError();
      return Response.Builder.withStatus(error.getCode())
                             .errorMessage(rb.getString("chat.message.default.error"))
                             .build();
    }

  }

  /**
   * @param username
   * @param status
   * @return
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/xmpp/sendstatus/{username}/{status}/")
  public Response setUserStatus(@URIParam("username") String username,
                                @URIParam("status") String status) {
    if (this.rb == null) loadResourceBundle();
    XMPPSession session = messenger.getSession(username);
    Presence presence = PresenceUtil.getPresence(status);
    if (presence == null)
      return Response.Builder.withStatus(HTTPStatus.FORBIDDEN)
                             .errorMessage("Get unknow status.")
                             .build();
    session.sendPresence(presence);
    return Response.Builder.ok().cacheControl(cc).build();
  }

  /**
   * @param _username
   * @param _subsuser
   * @return
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/xmpp/subscribeuser/{username}/{subsuser}/")
  public Response subscribeUser(@URIParam("username") String _username,
                                @URIParam("subsuser") String _subsuser) {
    if (this.rb == null) loadResourceBundle();
    XMPPSession session = messenger.getSession(_username);
    if (session != null) {
      session.subscribeUser(_subsuser);
      return Response.Builder.ok().cacheControl(cc).build();
    } else {
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
                             .errorMessage(rb.getString("chat.message.room.xmppsession.null"))
                             .build();
    }
  }

  /**
   * @param username
   * @param unsubsuser
   * @return
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/xmpp/unsubscribeuser/{username}/{unsubsuser}/")
  public Response unsubscribeUser(@URIParam("username") String username,
                                  @URIParam("unsubsuser") String unsubsuser) {
    if (this.rb == null) loadResourceBundle();
    XMPPSession session = messenger.getSession(username);
    if (session != null) {
      session.unsubscribeUser(unsubsuser);
      return Response.Builder.ok().cacheControl(cc).build();
    } else {
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
                             .errorMessage(rb.getString("chat.message.room.xmppsession.null"))
                             .build();
    }
  }

  /**
   * @param username
   * @param uuid
   * @return
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/xmpp/fileexchange/accept/{username}/{uuid}/")
  @OutputTransformer(PassthroughOutputTransformer.class)
  public Response acceptFile(@URIParam("username") String username, @URIParam("uuid") String uuid) {
    if (this.rb == null) loadResourceBundle();
    try {
      XMPPSessionImpl session = (XMPPSessionImpl) messenger.getSession(username);
      String sender;
      if (session != null) {
        FileTransferRequest request = session.getFileTransferRequest(uuid);
        sender =  request.getRequestor();
        Presence presence = session.getConnection().getRoster().getPresence(sender);
        if (presence.getType().equals(Presence.Type.available)) {
          IncomingFileTransfer fileTransfer = request.accept();
          return Response.Builder.ok(fileTransfer.recieveFile(), DEFAULT_CONTENT_TYPE)
                                   .header("Content-disposition",
                                           "attachment; filename=\"" + fileTransfer.getFileName()
                                               + "\"")
                                   .contentLenght(fileTransfer.getFileSize())
                                   .cacheControl(cc)
                                   .build();
        } else {
          String errorMessage = rb.getString("chat.message.filetransfer.sender.accept.offline");
          session.sendErrorMessage(errorMessage.trim(),sender);
          return Response.Builder.withStatus(HTTPStatus.NOT_FOUND)
                                 .errorMessage(rb.getString("chat.message.filetransfer.sender.accept.offline"))
                                 .build();
        }
      } else {
        return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
                               .errorMessage(rb.getString("chat.message.room.xmppsession.null"))
                               .build();
      }
    } catch (Exception e) {
      if (log.isDebugEnabled()) 
        e.printStackTrace();
      return Response.Builder.withStatus(HTTPStatus.BAD_REQUEST)
                             .errorMessage(e.getMessage())
                             .build();
    }

  }

  /**
   * @param username
   * @param uuid
   * @return
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/xmpp/fileexchange/reject/{username}/{uuid}/")
  public Response rejectFile(@URIParam("username") String username,
                             @URIParam("uuid") String uuid) {
    if (this.rb == null) loadResourceBundle();
    XMPPSessionImpl session = (XMPPSessionImpl) messenger.getSession(username);
    String sender;
    if (session != null) {
      FileTransferRequest request = session.getFileTransferRequest(uuid);
      sender = request.getRequestor();
      Presence presence = session.getConnection().getRoster().getPresence(sender);
      if (presence.getType().equals(Presence.Type.available)) {
        request.reject();
        return Response.Builder.ok().cacheControl(cc).build();
      } else {
        String errorMessage = rb.getString("chat.message.filetransfer.sender.reject.offline");
        session.sendErrorMessage(errorMessage.trim(),sender);
        return Response.Builder.withStatus(HTTPStatus.NOT_FOUND)
                               .errorMessage(rb.getString("chat.message.filetransfer.sender.reject.offline"))
                               .build();
      }
    } else {
      return Response.Builder.withStatus(HTTPStatus.INTERNAL_ERROR)
                             .errorMessage(rb.getString("chat.message.room.xmppsession.null"))
                             .build();
    }
  }


}
