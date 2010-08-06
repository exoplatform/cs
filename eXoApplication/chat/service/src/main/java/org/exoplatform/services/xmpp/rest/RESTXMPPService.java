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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.presence.DefaultPresenceStatus;
import org.exoplatform.services.resources.ResourceBundleService;
import org.exoplatform.services.rest.impl.RuntimeDelegateImpl;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.xmpp.bean.ConfigRoomBean;
import org.exoplatform.services.xmpp.bean.ContactBean;
import org.exoplatform.services.xmpp.bean.FormBean;
import org.exoplatform.services.xmpp.bean.FullRoomInfoBean;
import org.exoplatform.services.xmpp.bean.HostedRoomBean;
import org.exoplatform.services.xmpp.bean.InitInfoBean;
import org.exoplatform.services.xmpp.bean.InterlocutorListBean;
import org.exoplatform.services.xmpp.bean.JsResourceBundleBean;
import org.exoplatform.services.xmpp.bean.MessageBean;
import org.exoplatform.services.xmpp.bean.MessageListBean;
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

@Path("/xmpp")
public class RESTXMPPService implements ResourceContainer, Startable {

  private static final Map<String, String> jsResourceBundle = new HashMap<String, String>(){
    {
      put("chat.message.xmpp.session.is.null", "chat_message_xmpp_session_is_null");
      put("chat.message.room.creation.error", "chat_message_room_creation_error");
      put("chat.message.room.show.config", "chat_message_room_show_config");
      put("chat.message.room.default.config.commit", "chat_message_room_default_config_commit");
      put("chat.message.room.configuration.error", "chat_message_room_configuration_error");
      put("chat.message.room.password.error", "chat_message_room_password_error");
      put("chat.message.room.not.unlocked.error", "chat_message_room_not_unlocked_error");
      put("chat.message.room.user.not.member", "chat_message_room_user_not_member");
      put("chat.message.room.name.is.invalid", "chat_message_room_name_is_invalid");
      put("chat.message.room.secret.key.to.access", "chat_message_room_secret_key_to_access");
      put("chat.message.room.invite.to.join", "chat_message_room_invite_to_join");
      put("chat.message.room.user.left", "chat_message_room_user_left");
      put("chat.message.room.user.join", "chat_message_room_user_join");
      put("chat.message.confirm.allow.to.see.status", "chat_message_confirm_allow_to_see_status");
      put("chat.message.confirm.remove.buddy", "chat_message_confirm_remove_buddy");
      put("chat.message.system.info", "chat_message_system_info");
      put("chat.message.administrative.message", "chat_message_administrative_message");
      put("chat.message.file.transport.request", "chat_message_file_transport_request");
      put("chat.message.file.transport.response.completed", "chat_message_file_transport_response_completed");
      put("chat.message.file.transport.response.denied", "chat_message_file_transport_response_denied");
      put("chat.message.file.transport.response.receiver.offline", "chat_message_file_transport_response_receiver_offline");
      put("chat.message.file.event.time.out", "chat_message_file_event_time_out");
      put("chat.message.file.exchange.waiting.for.authorize", "chat_message_file_exchange_waiting_for_authorize");
      put("chat.message.file.exchange.uploading.file.to.server", "chat_message_file_exchange_uploading_file_to_server");
    }
  };

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
	//TODO: to find the reason why RESTXMPPService loaded before ResourceBinder
	RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
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

  public ResourceBundle loadResourceBundle(){
    this.rb = this.rbs.getResourceBundle(BUNDLE_NAME, Locale.getDefault());
    return this.rb;
  }

  @GET
  @Path("/loadJsResourceBundle/{locale}/")
  @Produces(MediaType.APPLICATION_JSON)
  public Response loadJsResourceBundle(@PathParam("locale") String locale){
    ResourceBundle jsRb = this.rbs.getResourceBundle(BUNDLE_NAME, new Locale(locale));
    StringBuilder sb = new StringBuilder();
    try {
      sb.append("eXo.communication.chatbar.locale.ResourceBundle = {\n");
      for(Map.Entry<String, String> entry : jsResourceBundle.entrySet()){
    	String value = (jsRb != null && jsRb.getString(entry.getKey()) != null) ? jsRb.getString(entry.getKey()) : entry.getKey();
        sb.append(entry.getValue() + " : \"").append(value).append("\",\n");
      }
      sb.append("chat_message_finish_load_resource_bundle : \"finish load resource bundle\"\n");
      sb.append("};");
      JsResourceBundleBean jsResourceBundleBean = new JsResourceBundleBean();
      jsResourceBundleBean.setScript(sb.toString());
      return Response.ok(jsResourceBundleBean, JSON_CONTENT_TYPE)
      .cacheControl(cc)
      .build();
    } catch (Exception e){
     // if (log.isDebugEnabled())
        e.printStackTrace();
      return Response.status(HTTPStatus.INTERNAL_ERROR).build() ;
    }
  }

  // //////////// Group chat //////////////////
  /**
   * @param username
   * @param room
   * @return
   */
  @GET
  @Path("/muc/createroom/{username}/")
  @Produces(MediaType.APPLICATION_JSON)
  public Response createRoom(@PathParam("username") String username,
                             @QueryParam("room") String room,
                             @QueryParam("nickname") String nickname) {
    if (this.rb == null) loadResourceBundle();
    if (room == null) {
      Response.status(HTTPStatus.BAD_REQUEST) ;
      return Response.ok(rb.getString("chat.message.roomid.null"))
      .build();
    }
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
        return Response.ok(formBean, JSON_CONTENT_TYPE).cacheControl(cc).build();
      } catch (Exception e) {
        /*e.printStackTrace();*/
        return Response.status(HTTPStatus.INTERNAL_ERROR).entity(rb.getString("chat.message.room.creation.error") + "\n" + e.getMessage())
        .build();
      }
    } else {
      if (log.isDebugEnabled())
        log.debug(rb.getString("chat.message.room.xmppsession.null"));
      return Response.status(HTTPStatus.BAD_REQUEST).entity(rb.getString("chat.message.room.xmppsession.null"))
      .build();
    }
  }


  @POST
  @Path("/muc/configroom/{username}/")
  @Consumes(MediaType.APPLICATION_JSON)
  //@InputTransformer(Json2BeanInputTransformer.class)
  public Response configRoom(@PathParam("username") String username,
                             @QueryParam("room") String room,
                             ConfigRoomBean configRoom) {
    if (this.rb == null) loadResourceBundle();
    if (room == null){
      return Response.status(HTTPStatus.BAD_REQUEST).entity(rb.getString("chat.message.roomid.null"))
      .build();
    }
    try {
      XMPPSession session = messenger.getSession(username);
      if (session != null) {
        room = StringUtils.escapeNode(room.toLowerCase());
        if (session.getMultiUserChat(room) != null)
          session.configRoom(room, configRoom);
        return Response.ok().cacheControl(cc).build();
      } else {
        if (log.isDebugEnabled())
          log.debug(rb.getString("chat.message.room.xmppsession.null"));

        return 
        Response.status(HTTPStatus.INTERNAL_ERROR).entity(rb.getString("chat.message.room.xmppsession.null"))
        .build();
      }
    } catch (XMPPException e) {
      /*if (log.isDebugEnabled()) 
        e.printStackTrace();*/
      XMPPError error = e.getXMPPError();

      return Response.status(error.getCode()).entity(rb.getString("chat.message.conference.configuration.error")
                                                     + "\n" + error.getMessage())
                                                     .build();
    }
  }

  @GET
  @Path("/muc/getroomconfig/{username}/")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getRoomConfigForm(@PathParam("username") String username,
                                    @QueryParam("room") String room) {
    if (this.rb == null) loadResourceBundle();
    if (room == null) {
      return Response.status(HTTPStatus.BAD_REQUEST).entity(rb.getString("chat.message.roomid.null"))
      .build();
    }
    XMPPSession session = messenger.getSession(username);
    if (session != null) {
      try {
        FormBean formBean = session.getConfigFormRoom(room);
        if (formBean != null) {
          return Response.ok(formBean, JSON_CONTENT_TYPE)
          .cacheControl(cc)
          .build();
        }
        
        return Response.status(HTTPStatus.BAD_REQUEST).cacheControl(cc)
        .build();
      } catch (XMPPException e) {
        /*if (log.isDebugEnabled()) 
          e.printStackTrace();*/
        XMPPError error = e.getXMPPError();

        return Response.status(error.getCode()).entity(rb.getString("chat.message.conference.configuration.error"))
        .build();
      }
    }

    return Response.status(HTTPStatus.BAD_REQUEST).entity(rb.getString("chat.message.room.xmppsession.null"))
    .build();
  }

  /**
   * @param username
   * @param room
   * @return
   */
  @GET
  @Path("/muc/getroominfo/{username}/")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getRoomInfo(@PathParam("username") String username,
                              @QueryParam("room") String room) {
    if (this.rb == null) loadResourceBundle();
    if (room == null)
      return Response.status(HTTPStatus.BAD_REQUEST)
      .entity(rb.getString("chat.message.roomid.null"))
      .build();
    XMPPSession session = messenger.getSession(username);
    if (session != null) {
      try {
        FullRoomInfoBean infoBean = session.getRoomInfoBean(room.toLowerCase());
        if (infoBean != null)
          return Response.ok(infoBean, JSON_CONTENT_TYPE)
          .cacheControl(cc)
          .build();

        return Response.status(HTTPStatus.BAD_REQUEST).cacheControl(cc)
        .build();
      } catch (XMPPException e) {
        if (log.isDebugEnabled()) 
          e.printStackTrace();
        XMPPError error = e.getXMPPError();
        return Response.status(error.getCode())
        .entity(rb.getString("chat.message.conference.info.error") + "\n" + error.getMessage())
        .build();
      }
    }
    return Response.status(HTTPStatus.BAD_REQUEST)
    .entity(rb.getString("chat.message.room.xmppsession.null"))
    .build();
  }

  /**
   * @param username
   * @return
   */
  @GET
  @Path("/muc/joinedrooms/{username}/")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getJoinedRooms(@PathParam("username") String username) {
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
        return Response.ok(bean, JSON_CONTENT_TYPE).cacheControl(cc).build();
      } catch (XMPPException e) {
        if (log.isDebugEnabled()) 
          e.printStackTrace();
        XMPPError error = e.getXMPPError();

        return Response.status(error.getCode()).entity(rb.getString("chat.message.conference.info.error") + "\n" + error.getMessage())
        .build();
      }
    }
    return Response.status(HTTPStatus.BAD_REQUEST).entity(rb.getString("chat.message.room.xmppsession.null"))
    .build();
  }

  @Deprecated
  @GET
  @Path("/muc/rooms-old/{username}/")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getRooms(@PathParam("username") String username) {
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
        return Response.ok(infoBean, JSON_CONTENT_TYPE).cacheControl(cc).build();
      } catch (XMPPException e) {
        if (log.isDebugEnabled()) 
          e.printStackTrace();
        XMPPError error = e.getXMPPError();
        return Response.status(error.getCode())
        .entity(error.getMessage())
        .build();
      }
    }
    return Response.status(HTTPStatus.BAD_REQUEST)
    .entity(rb.getString("chat.message.room.xmppsession.null"))
    .build();
  }



  @GET
  @Path("/muc/rooms/{username}/")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getRooms(@PathParam("username") String username,
                           @QueryParam("from") Integer from,
                           @QueryParam("to") Integer to,
                           @QueryParam("sort") String sort) {
    if (this.rb == null) loadResourceBundle();
    XMPPSessionImpl session = (XMPPSessionImpl) messenger.getSession(username);
    if (session != null) {
      try {
        return Response.ok(session.getRooms(from, to,sort), JSON_CONTENT_TYPE).cacheControl(cc).build();
      } catch (XMPPException e) {
        if (log.isDebugEnabled()) 
          e.printStackTrace();
        XMPPError error = e.getXMPPError();
        return Response.status(error.getCode())
        .entity(rb.getString("chat.message.conference.info.error") + "\n" + error.getMessage())
        .build();
      }
    }
    return Response.status(HTTPStatus.BAD_REQUEST)
    .entity(rb.getString("chat.message.room.xmppsession.null"))
    .build();
  }

  /**
   * @param username
   * @param inviter
   * @param room
   * @return
   */
  @GET
  @Path("/muc/decline/{username}/{inviter}/")
  public Response declineToRoom(@PathParam("username") String username,
                                @PathParam("inviter") String inviter,
                                @QueryParam("room") String room,
                                @QueryParam("reason") String reason) {
    if (this.rb == null) loadResourceBundle();
    if (room == null)
      return Response.status(HTTPStatus.BAD_REQUEST)
      .entity(rb.getString("chat.message.roomid.null"))
      .build();
    XMPPSession session = messenger.getSession(username);
    if (session != null) {
      session.declineRoom(room, inviter, reason);
      return Response.ok().cacheControl(cc).build();
    } else {
      if (log.isDebugEnabled())
        log.debug(rb.getString("chat.message.room.xmppsession.null"));
      return Response.status(HTTPStatus.BAD_REQUEST)
      .entity(rb.getString("chat.message.room.xmppsession.null"))
      .build();
    }
  }

  @GET
  @Path("/muc/destroy/{username}/")
  public Response destroyRoom(@PathParam("username") String username,
                              @QueryParam("room") String room,
                              @QueryParam("reason") String reason,
                              @QueryParam("altroom") String altRoom) {
    if (this.rb == null) loadResourceBundle();
    if (room == null)
      return Response.status(HTTPStatus.BAD_REQUEST)
      .entity(rb.getString("chat.message.roomid.null"))
      .build();
    XMPPSession session = messenger.getSession(username);
    if (session != null) {
      try {
        if (session.destroyRoom(room, reason, altRoom))
          return Response.ok().cacheControl(cc).build();
        else
          return Response.status(HTTPStatus.BAD_REQUEST)
          .entity(rb.getString("chat.message.room.not.found"))
          .cacheControl(cc)
          .build();
      } catch (XMPPException e) {
        if (log.isDebugEnabled()) 
          e.printStackTrace();
        XMPPError error = e.getXMPPError();
        return Response.status(error.getCode())
        .entity(rb.getString("chat.message.room.destroy.error") + "\n" + error.getMessage())
        .build();
      }
    } else {
      if (log.isDebugEnabled())
        log.debug(rb.getString("chat.message.room.xmppsession.null"));
      return Response.status(HTTPStatus.BAD_REQUEST)
      .entity(rb.getString("chat.message.room.xmppsession.null"))
      .build();
    }
  }

  /**
   * @param username
   * @param inviter
   * @param room
   * @return
   */
  @GET
  @Path("/muc/invite/{username}/{invitee}/")
  public Response inviteToRoom(@PathParam("username") String username,
                               @PathParam("invitee") String invitee,
                               @QueryParam("room") String room,
                               @QueryParam("reason") String reason) {
    if (this.rb == null) loadResourceBundle();
    
    // 09/06/2010 add start
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    UserInfoService organization = (UserInfoService) container.getComponentInstanceOfType(UserInfoService.class);
    
    InitInfoBean inviteeBean = new InitInfoBean();
    
    ContactBean inviteeProfile = new ContactBean();
    inviteeProfile.setUser(invitee);
    UserInfo inviteeInfo = organization.getUserInfo(invitee);
    inviteeProfile.setFullName(inviteeInfo.getFirstName() + " " + inviteeInfo.getLastName());
    inviteeBean.setMyProfile(inviteeProfile);
    // 09/06/2010 add end
    
    if (room == null)
      return Response.status(HTTPStatus.BAD_REQUEST)
      .entity(rb.getString("chat.message.roomid.null"))
      .build();
    XMPPSession session = messenger.getSession(username);
    
    if (session != null) {
      try {
        if (session.inviteToRoom(room, invitee, reason))
          return Response.ok(inviteeBean, JSON_CONTENT_TYPE).cacheControl(cc).build(); // 09/06/2010 DungLV modify
        return Response.status(HTTPStatus.BAD_REQUEST)//.entity()
        .cacheControl(cc)
        .build();
      } catch (XMPPException e) {
        //        if (log.isDebugEnabled()) 
        e.printStackTrace();
        XMPPError error = e.getXMPPError();
        return Response.status(error.getCode())
        .entity(rb.getString("chat.message.conference.service.error"))
        .build();
      }
    }
    return Response.status(HTTPStatus.BAD_REQUEST)
    .cacheControl(cc)
    .entity(rb.getString("chat.message.room.xmppsession.null"))
    .build();
  }

  /**
   * @param username
   * @param room
   * @return
   */
  @GET
  @Path("/muc/join/{username}/")
  @Produces(MediaType.APPLICATION_JSON)
  public Response joinRoom(@PathParam("username") String username,
                           @QueryParam("room") String room,
                           @QueryParam("nickname") String nickname,
                           @QueryParam("password") String password) {
    if (this.rb == null) loadResourceBundle();
    if (room == null)
      return Response.status(HTTPStatus.BAD_REQUEST)
      .entity(rb.getString("chat.message.roomid.null"))
      .build();
    XMPPSession session = messenger.getSession(username);
    if (session != null) {
      try {
        session.joinRoom(room, nickname, password);
        return Response.ok().cacheControl(cc).build();
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
        return Response.status(error.getCode())
        .entity(em)
        .build();
      }
    }
    return Response.status(HTTPStatus.BAD_REQUEST)
    .entity(rb.getString("chat.message.room.xmppsession.null"))
    .cacheControl(cc)
    .build();
  }

  /**
   * @param username
   * @param room
   * @return
   */
  @GET
  @Path("/muc/leaveroom/{username}/")
  public Response leftRoom(@PathParam("username") String username,
                           @QueryParam("room") String room) {
    if (this.rb == null) loadResourceBundle();
    if (room == null)
      return Response.status(HTTPStatus.BAD_REQUEST)
      .entity(rb.getString("chat.message.roomid.null"))
      .build();
    XMPPSession session = messenger.getSession(username);
    if (session != null) {
      try {
        if (session.leaveRoom(room))
          return Response.ok().cacheControl(cc).build();
        return Response.status(HTTPStatus.BAD_REQUEST)
        .cacheControl(cc)
        .build();
      } catch (XMPPException e) {
        if (log.isDebugEnabled()) 
          e.printStackTrace();
        XMPPError error = e.getXMPPError();
        return Response.status(error.getCode())
        .entity(rb.getString("chat.message.default.error"))
        .build();
      }
    }
    return Response.status(HTTPStatus.BAD_REQUEST)
    .entity(rb.getString("chat.message.room.xmppsession.null"))
    .cacheControl(cc)
    .build();
  }

  /**
   * @param username
   * @param room
   * @param nickname
   * @return
   */
  @GET
  @Path("/muc/changenickname/{username}/{nickname}/")
  public Response changeNickname(@PathParam("username") String username,
                                 @QueryParam("nickname") String nickname,
                                 @QueryParam("room") String room) {
    if (this.rb == null) loadResourceBundle();
    if (room == null || nickname == null)
      return Response.status(HTTPStatus.BAD_REQUEST)
      .entity(rb.getString("chat.message.roomid.null"))
      .build();
    XMPPSession session = messenger.getSession(username);
    if (session != null) {
      try {
        session.changeNickname(room, nickname);
        return Response.ok().cacheControl(cc).build();
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
        return Response.status(error.getCode())
        .entity(em)
        .build();
      }
    }
    return Response.status(HTTPStatus.BAD_REQUEST)
    .entity(rb.getString("chat.message.room.xmppsession.null"))
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
  @GET
  @Path("/muc/changestatus/{username}/{mode}/")
  public Response changeAvailabilityStatusInRoom(@PathParam("username") String username,
                                                 @PathParam("mode") String mode,
                                                 @QueryParam("room") String room,
                                                 @QueryParam("status") String status) {
    if (this.rb == null) loadResourceBundle();
    if (room == null)
      return Response.status(HTTPStatus.BAD_REQUEST)
      .entity(rb.getString("chat.message.roomid.null"))
      .build();
    XMPPSession session = messenger.getSession(username);
    if (session != null) {
      try {
        session.changeAvailabilityStatusInRoom(room, status, mode);
        return Response.ok().cacheControl(cc).build();
      } catch (XMPPException e) {
        if (log.isDebugEnabled()) 
          e.printStackTrace();
        XMPPError error = e.getXMPPError();

        return Response.status(error.getCode()).entity(rb.getString("chat.message.default.error"))
        .build();
      }
    }
    return Response.ok(rb.getString("chat.message.room.xmppsession.null")).build();
  }

  /**
   * @param username
   * @param room
   * @param subject
   * @return
   */
  @GET
  @Path("/muc/changesubject/{username}/")
  public Response changeSubject(@PathParam("username") String username,
                                @QueryParam("room") String room,
                                @QueryParam("subject") String subject) {
    if (this.rb == null) loadResourceBundle();
    if (room == null || subject == null)
      return Response.status(HTTPStatus.BAD_REQUEST)
      .entity(rb.getString("chat.message.roomid.null"))
      .build();
    XMPPSession session = messenger.getSession(username);
    if (session != null) {
      try {
        session.changeSubject(room, subject);
        return Response.ok().cacheControl(cc).build();
      } catch (XMPPException e) {
        if (log.isDebugEnabled()) 
          e.printStackTrace();
        XMPPError error = e.getXMPPError();
        /* String em= new String();
        switch (error.getCode()) {
        case 403:
          em = rb.getString("chat.message.subject.change.error");
          break;
        default:
          em = rb.getString("chat.message.default.error");
          break;
        }*/
        return Response.status(error.getCode())
        .entity(error.getMessage())
        .build();
      }
    }
    return Response.ok(rb.getString("chat.message.room.xmppsession.null")).build();
  }

  /**
   * @param username
   * @param room
   * @param nickname
   * @param role
   * @param command
   * @return
   */
  @GET
  @Path("/muc/managerole/{username}/")
  public Response manageRoleRoom(@PathParam("username") String username,
                                 @QueryParam("room") String room,
                                 @QueryParam("nickname") String nickname,
                                 @QueryParam("role") String role,
                                 @QueryParam("command") String command) {
    if (this.rb == null) loadResourceBundle();
    if (room == null || nickname == null)
      return Response.status(HTTPStatus.BAD_REQUEST)
      .entity(rb.getString("chat.message.roomid.null"))
      .build();
    XMPPSession session = messenger.getSession(username);
    if (session != null) {
      try {
        session.manageRole(room, nickname, role, command);
        return Response.ok().cacheControl(cc).build();
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
        return Response.status(error.getCode())
        .entity(em)
        .build();
      }
    } else {
      if (log.isDebugEnabled())
        log.debug("Sesion is null");
      return Response.status(HTTPStatus.BAD_REQUEST)
      .entity(rb.getString("chat.message.room.xmppsession.null"))
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
  @GET
  @Path("/muc/manageaffiliation/{username}/")
  public Response manageAffilationRoom(@PathParam("username") String username,
                                       @QueryParam("room") String room,
                                       @QueryParam("nickname") String nickname,
                                       @QueryParam("affiliation") String affiliation,
                                       @QueryParam("command") String command) {
    if (this.rb == null) loadResourceBundle();
    if (room == null || nickname == null)
      return Response.status(HTTPStatus.BAD_REQUEST)
      .entity(rb.getString("chat.message.roomid.null"))
      .build();
    XMPPSession session = messenger.getSession(username);
    if (session != null) {
      try {
        session.manageAffiliation(room, nickname, affiliation, command);
        return Response.ok().cacheControl(cc).build();
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
        return Response.status(error.getCode())
        .entity(em)
        .build();
      }
    } else {
      if (log.isDebugEnabled())
        log.debug("Sesion is null");
      return Response.status(HTTPStatus.NOT_FOUND)
      .entity(rb.getString("chat.message.room.xmppsession.null"))
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
  @GET
  @Path("/muc/kick/{username}/")
  public Response kickUserFromRoom(@PathParam("username") String username,
                                   @QueryParam("room") String room,
                                   @QueryParam("nickname") String nickname,
                                   @QueryParam("reason") String reason) {
    if (this.rb == null) loadResourceBundle();
    if (room == null || nickname == null)
      return Response.status(HTTPStatus.BAD_REQUEST)
      .entity(rb.getString("chat.message.roomid.null"))
      .build();
    XMPPSession session = messenger.getSession(username);
    if (session != null) {
      try {
        session.kickUser(room, nickname, reason);
        return Response.ok().cacheControl(cc).build();
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
        return Response.status(error.getCode())
        .entity(em)
        .build();
      }
    } else {
      if (log.isDebugEnabled())
        log.debug("Sesion is null");
      return Response.status(HTTPStatus.NOT_FOUND)
      .entity(rb.getString("chat.message.room.xmppsession.null"))
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
  @GET
  @Path("/muc/ban/{username}/")
  public Response banUserFromRoom(@PathParam("username") String username,
                                  @QueryParam("room") String room,
                                  @QueryParam("name") String name,
                                  @QueryParam("reason") String reason) {
    if (this.rb == null) loadResourceBundle();
    if (room == null || name ==null)
      return Response.status(HTTPStatus.BAD_REQUEST)
      .entity(rb.getString("chat.message.roomid.null"))
      .build();
    XMPPSession session = messenger.getSession(username);
    if (session != null) {
      try {
        session.banUser(room, name, reason);
        return Response.ok().cacheControl(cc).build();
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
        return Response.status(error.getCode())
        .entity(em)
        .build();
      }
    } else {
      if (log.isDebugEnabled())
        log.debug(rb.getString("chat.message.room.xmppsession.null"));
      return Response.status(HTTPStatus.NOT_FOUND)
      .entity(rb.getString("chat.message.room.xmppsession.null"))
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
  @POST
  @Path("/addtransport/")
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
        return Response.ok().cacheControl(cc).build();
      } else {
        if (log.isDebugEnabled())
          log.debug(rb.getString("chat.message.room.xmppsession.null"));
        return Response.status(HTTPStatus.FORBIDDEN)
        .entity(rb.getString("chat.message.room.xmppsession.null"))
        .build();
      }
    } catch (Exception e) {
      if (log.isDebugEnabled()) 
        e.printStackTrace();
      return Response.status(HTTPStatus.BAD_REQUEST)
      .entity(e.getMessage())
      .build();
    }
  }

  @GET
  @Path("/roster/add/{username}/{adduser}/")
  public Response addBoddyToRoster(@PathParam("username") String username,
                                   @PathParam("adduser") String adduser,
                                   @QueryParam("nickname") String nickname,
                                   @QueryParam("group") String group) {
    if (this.rb == null) loadResourceBundle();
    XMPPSession session = messenger.getSession(username);
    try {
      if (session != null) {
        if (nickname == null)
          nickname = adduser;
        session.addBuddy(adduser, nickname, group);
        return Response.ok().cacheControl(cc).build();
      } else {
        if (log.isDebugEnabled())
          log.debug(rb.getString("chat.message.room.xmppsession.null"));
        return Response.status(HTTPStatus.INTERNAL_ERROR).build();
      }
    } catch (XMPPException e) {
      if (log.isDebugEnabled()) 
        e.printStackTrace();
      XMPPError error = e.getXMPPError();
      return Response.status(error.getCode())
      .entity(rb.getString("chat.message.default.error"))
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
  @GET
  @Path("/roster/update/{username}/{upduser}/")
  public Response updateBoddy(@PathParam("username") String username,
                              @PathParam("upduser") String upduser,
                              @QueryParam("nickname") String nickname,
                              @QueryParam("group") String group) {
    if (this.rb == null) loadResourceBundle();
    XMPPSession session = messenger.getSession(username);
    try {
      if (session != null) {
        if (nickname == null)
          nickname = upduser;
        session.updateBuddy(upduser, nickname, group);
        return Response.ok().cacheControl(cc).build();
      } else {
        return Response.status(HTTPStatus.INTERNAL_ERROR).build();
      }
    } catch (XMPPException e) {
      if (log.isDebugEnabled()) 
        e.printStackTrace();
      XMPPError error = e.getXMPPError();
      return Response.status(error.getCode())
      .entity(rb.getString("chat.message.default.error"))
      .build();
    }
  }

  /**
   * @param username
   * @param group
   * @return
   */
  @GET
  @Path("/roster/group/{username}/{group}/")
  public Response createGroup(@PathParam("username") String username,
                              @PathParam("group") String group) { 
    if (this.rb == null) loadResourceBundle();
    XMPPSessionImpl session = (XMPPSessionImpl) messenger.getSession(username);
    if (session != null) {
      session.createGroup(group);
      return Response.ok().cacheControl(cc).build();
    } else {
      return Response.status(HTTPStatus.INTERNAL_ERROR).build();
    }
  }

  /**
   * @param _username
   * @param _askuser
   * @return
   */
  @GET
  @Path("/askforsubscription/{username}/{askuser}/")
  public Response askForSubscription(@PathParam("username") String username,
                                     @PathParam("askuser") String askuser,
                                     @QueryParam("nickname") String nickname) {
    if (this.rb == null) loadResourceBundle();
    XMPPSession session = messenger.getSession(username);
    if (session != null) {
      if (session.getBuddy(askuser) == null)
        return Response.status(HTTPStatus.BAD_REQUEST)
        .entity(rb.getString("chat.message.user.not.found"))
        .build();
      if (nickname == null)
        nickname = askuser;
      session.askForSubscription(askuser, nickname);
      return Response.ok().cacheControl(cc).build();
    } else {
      if (log.isDebugEnabled())
        log.debug(rb.getString("chat.message.room.xmppsession.null"));
      return Response.status(HTTPStatus.INTERNAL_ERROR).build();
    }
  }

  /**
   * @param username
   * @return
   */
  @GET
  @Path("/rosterclean/{username}/")
  public Response cleanBuddylist(@PathParam("username") String username) {
    if (this.rb == null) loadResourceBundle();
    try {
      XMPPSession session = messenger.getSession(username);
      if (session != null) {
        session.cleanBuddiesList();
        return Response.ok().cacheControl(cc).build();
      } else {
        if (log.isDebugEnabled())
          log.debug(rb.getString("chat.message.room.xmppsession.null"));
        return Response.status(HTTPStatus.INTERNAL_ERROR)
        .entity(rb.getString("chat.message.room.xmppsession.null"))
        .build();
      }
    } catch (XMPPException e) {
      if (log.isDebugEnabled()) 
        e.printStackTrace();
      XMPPError error = e.getXMPPError();
      return Response.status(error.getCode())
      .entity(rb.getString("chat.message.default.error"))
      .build();
    }
  }

  /**
   * @param username
   * @param jid
   * @return
   */
  @GET
  @Path("/history/getmessages/{usernameto}/{isGroupChat}/")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAllHistory(@PathParam("usernameto") String usernameto,
                                @PathParam("isGroupChat") Boolean isGroupChat,
                                @QueryParam("usernamefrom") String usernamefrom) {
    if (this.rb == null) loadResourceBundle();
    if (usernamefrom == null || usernamefrom.length() == 0)
      return Response.status(HTTPStatus.BAD_REQUEST)
      .entity(rb.getString("chat.message.history.participant.name.not.set"))
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
        return Response.ok(new MessageListBean(listBean), JSON_CONTENT_TYPE)
        .cacheControl(cc)
        .build();
      } else
        return Response.status(HTTPStatus.FORBIDDEN).build();
    } catch (Exception e) {
      if (log.isDebugEnabled())
        e.printStackTrace();
      return Response.status(HTTPStatus.INTERNAL_ERROR).build();
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
  @GET
  @Path("/history/getmessages/{usernameto}/{isGroupChat}/{from}/{to}/")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getHistoryBetweenDate(@PathParam("usernameto") String usernameto,
                                        @PathParam("isGroupChat") Boolean isGroupChat,
                                        @PathParam("from") String from,
                                        @PathParam("to") String to,
                                        @QueryParam("usernamefrom") String usernamefrom) {
    if (this.rb == null) loadResourceBundle();
    if (usernamefrom == null || usernamefrom.length() == 0)
      return Response.status(HTTPStatus.BAD_REQUEST)
      .entity(rb.getString("chat.message.history.participant.name.not.set"))
      .build();
    try {
      XMPPSession session = messenger.getSession(usernameto);
      if (session != null) {
        List<HistoricalMessage> list = new ArrayList<HistoricalMessage>();
        Date dateFrom = new Date(Long.parseLong(from));
        Date dateTo = new Date(Long.parseLong(to));
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
          return Response.ok(new MessageListBean(listBean), JSON_CONTENT_TYPE)
          .cacheControl(cc)
          .build();
        } else {
          return Response.status(HTTPStatus.CONFLICT).build();
        }
      } else
        return Response.status(HTTPStatus.FORBIDDEN).build();
    } catch (Exception e) {
      if (log.isDebugEnabled())
        e.printStackTrace();
      return Response.status(HTTPStatus.INTERNAL_ERROR).build();
    }
  }

  /**
   * @param username
   * @param jid
   * @param dateformat
   * @param from
   * @return
   */
  @GET
  @Path("/history/getmessages/{usernameto}/{isGroupChat}/{from}/")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getHistoryFromDateToNow(@PathParam("usernameto") String usernameto,
                                          @PathParam("isGroupChat") Boolean isGroupChat,
                                          @PathParam("from") String from,
                                          @QueryParam("usernamefrom") String usernamefrom) {
    if (this.rb == null) loadResourceBundle();
    if (usernamefrom == null || usernamefrom.length() == 0)
      return Response.status(HTTPStatus.BAD_REQUEST)
      .entity(rb.getString("chat.message.history.participant.name.not.set"))
      .build();
    try {
      XMPPSession session = messenger.getSession(usernameto);
      if (session != null) {
        List<HistoricalMessage> list = new ArrayList<HistoricalMessage>();
        Date dateFrom = new Date(Long.parseLong(from));
        List<MessageBean> listBean = new ArrayList<MessageBean>();
        if (dateFrom.before(Calendar.getInstance().getTime())) {
          list = session.getHistoryFromDateToNow(usernameto, usernamefrom, isGroupChat, dateFrom);
          if (!list.isEmpty()) {
            for (HistoricalMessage historicalMessage : list) {
              listBean.add(TransformUtils.messageToBean(historicalMessage));
            }
          }
          return Response.ok(new MessageListBean(listBean), JSON_CONTENT_TYPE)
          .cacheControl(cc)
          .build();
        } else {
          return Response.status(HTTPStatus.CONFLICT).build();
        }
      } else
        return Response.status(HTTPStatus.FORBIDDEN).build();
    } catch (Exception e) {
      if (log.isDebugEnabled())
        e.printStackTrace();
      return Response.status(HTTPStatus.INTERNAL_ERROR).build();
    }
  }

  /**
   * @param username
   * @return
   */
  @GET
  @Path("/history/getinterlocutors/{username}/")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getInterlocutors(@PathParam("username") String username) {
    if (this.rb == null) loadResourceBundle();
    try {
      XMPPSession session = messenger.getSession(username);
      if (session != null) {
        return Response.ok(new InterlocutorListBean(session.getInterlocutors(username)),
                           JSON_CONTENT_TYPE).cacheControl(cc).build();
      } else
        return Response.status(HTTPStatus.FORBIDDEN).build();
    } catch (Exception e) {
      if (log.isDebugEnabled())
        e.printStackTrace();
      return Response.status(HTTPStatus.INTERNAL_ERROR).build();
    }
  }

  /**
   * @param username
   * @param jid
   * @return
   */
  @GET
  @Path("/history/file/getmessages/{usernameto}/{isGroupChat}/{clientTimezoneOffset}/")
  //@OutputTransformer(PassthroughOutputTransformer.class)
  public Response getAllHistoryFile(@PathParam("usernameto") String usernameto,
                                    @PathParam("isGroupChat") Boolean isGroupChat,
                                    @PathParam("clientTimezoneOffset") String clientTimezoneOffset,
                                    @QueryParam("usernamefrom") String usernamefrom) {
    if (this.rb == null) loadResourceBundle();
    if (usernamefrom == null || usernamefrom.length() == 0)
      return Response.status(HTTPStatus.BAD_REQUEST)
      .entity(rb.getString("chat.message.history.participant.name.not.set"))
      .build();
    try {
      //XMPPSession session = messenger.getSession(usernameto);
      //For CS-2668
      XMPPSession session = messenger.getSession(usernamefrom);
      if (session != null) {
        List<HistoricalMessage> list = new ArrayList<HistoricalMessage>();
        list = session.getAllHistory(usernameto, usernamefrom, isGroupChat);
        Integer clientTimeZoneOffset = Integer.valueOf(clientTimezoneOffset);
        for (HistoricalMessage message : list){
          Date dateSend = message.getDateSend();
          dateSend = TransformUtils.convertToClientTime(dateSend, clientTimeZoneOffset);
          message.setDateSend(dateSend);
        }
        InputStream inputStream = historyBeanToStream(list);
        return Response.ok(inputStream, DEFAULT_CONTENT_TYPE)
        .header("Content-disposition",
                "attachment; filename=" + usernameto + "-" + usernamefrom
                + ".txt")
                .cacheControl(cc)
                .build();
      } else
        return Response.status(HTTPStatus.FORBIDDEN).build();
    } catch (Exception e) {
      if (log.isDebugEnabled())
        e.printStackTrace();
      return Response.status(HTTPStatus.INTERNAL_ERROR).build();
    }
  }

  /**
   * @param username
   * @param jid
   * @param dateformat
   * @param from
   * @return
   */
  @GET
  @Path("/history/file/getmessages/{usernameto}/{isGroupChat}/{from}/{clientTimezoneOffset}/")
  //@OutputTransformer(PassthroughOutputTransformer.class)
  public Response getHistoryFromDateToNowFile(@PathParam("usernameto") String usernameto,
                                              @PathParam("isGroupChat") Boolean isGroupChat,
                                              @PathParam("from") String from,
                                              @PathParam("clientTimezoneOffset") String clientTimezoneOffset,
                                              @QueryParam("usernamefrom") String usernamefrom) {
    if (this.rb == null) loadResourceBundle();
    try {
      //XMPPSession session = messenger.getSession(usernameto);
      //For CS-2668
      XMPPSession session = messenger.getSession(usernamefrom);
      if (session != null) {
        List<HistoricalMessage> list = new ArrayList<HistoricalMessage>();
        Integer clientTimeZoneOffset = Integer.valueOf(clientTimezoneOffset);
        Date dateFrom = new Date(Long.valueOf(from));
        if (dateFrom.before(Calendar.getInstance().getTime())) {
          list = session.getHistoryFromDateToNow(usernameto, usernamefrom, isGroupChat, dateFrom);
          for (HistoricalMessage message : list){
            Date dateSend = message.getDateSend();
            dateSend = TransformUtils.convertToClientTime(dateSend, clientTimeZoneOffset);
            message.setDateSend(dateSend);
          }
          InputStream inputStream = historyBeanToStream(list);
          CacheControl ccIEfixed = new CacheControl();//Fix for http://jira.exoplatform.org/browse/CS-3179
          //          MultivaluedMetadata headers= new MultivaluedMetadata();
          //          headers.putSingle("Content-disposition", "attachment; filename=" + usernameto + "-" + usernamefrom
          //                                             + "(" + from + ").txt");
          //          headers.putSingle("Expires", "Sun, 17 Dec 1989 07:30:00 GMT");
          return Response.ok(inputStream, DEFAULT_CONTENT_TYPE)
          .header("Content-disposition", "attachment; filename=" + usernameto + "-" + usernamefrom 
                  + "(" + from + ").txt").header("Expires", "Sun, 17 Dec 1989 07:30:00 GMT")
                  .cacheControl(ccIEfixed)
                  .build();
        } else {
          return Response.status(HTTPStatus.CONFLICT).build();
        }
      } else
        return Response.status(HTTPStatus.FORBIDDEN).build();
    } catch (Exception e) {
      if (log.isDebugEnabled())
        e.printStackTrace();
      return Response.status(HTTPStatus.INTERNAL_ERROR).build();
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
  @GET
  @Path("/history/file/getmessages/{usernameto}/{isGroupChat}/{from}/{to}/{clientTimezoneOffset}/")
  //@OutputTransformer(PassthroughOutputTransformer.class)
  public Response getHistoryBetweenDateFile(@PathParam("usernameto") String usernameto,
                                            @PathParam("isGroupChat") Boolean isGroupChat,
                                            @PathParam("from") String from,
                                            @PathParam("to") String to,
                                            @PathParam("clientTimezoneOffset") String clientTimezoneOffset,
                                            @QueryParam("usernamefrom") String usernamefrom) {
    if (this.rb == null) loadResourceBundle();
    try {
      //XMPPSession session = messenger.getSession(usernameto);
      //For CS-2668
      XMPPSession session = messenger.getSession(usernamefrom);
      if (session != null) {
        List<HistoricalMessage> list = new ArrayList<HistoricalMessage>();
        Integer clientTimeZoneOffset = Integer.valueOf(clientTimezoneOffset);
        Date dateFrom = new Date(Long.valueOf(from));
        Date dateTo = new Date(Long.valueOf(to));
        if (dateFrom.before(dateTo)) {
          list = session.getHistoryBetweenDate(usernameto,
                                               usernamefrom,
                                               isGroupChat,
                                               dateFrom,
                                               dateTo);
          for (HistoricalMessage message : list){
            Date dateSend = message.getDateSend();
            dateSend = TransformUtils.convertToClientTime(dateSend, clientTimeZoneOffset);
            message.setDateSend(dateSend);
          }
          InputStream inputStream = historyBeanToStream(list);
          return Response.ok(inputStream, DEFAULT_CONTENT_TYPE)
          .header("Content-disposition",
                  "attachment; filename=" + usernameto + "-" + usernamefrom
                  + "(" + from + "-" + to + ").txt")
                  .cacheControl(cc)
                  .build();
        } else {
          return Response.status(HTTPStatus.CONFLICT).build();
        }
      } else
        return Response.status(HTTPStatus.FORBIDDEN).build();
    } catch (Exception e) {
      if (log.isDebugEnabled())
        e.printStackTrace();
      return Response.status(HTTPStatus.INTERNAL_ERROR).build();
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
  @GET
  @Path("/getsearchform/{username}/")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getSearchUsersForm(@PathParam("username") String username,
                                     @QueryParam(SearchFormFields.SEARCH_SERVICE) String searchService) {
    if (this.rb == null) loadResourceBundle();
    try {
      XMPPSession session = messenger.getSession(username);
      if (session != null) {
        Form form = session.getSearchForm(searchService);
        return Response.ok(TransformUtils.formToFormBean(form), JSON_CONTENT_TYPE)
        .cacheControl(cc)
        .build();
      }
      return Response.status(HTTPStatus.FORBIDDEN)
      .cacheControl(cc)
      .entity(rb.getString("chat.message.xmppsession.null"))
      .build();
    } catch (XMPPException e) {
      if (log.isDebugEnabled()) 
        e.printStackTrace();
      XMPPError error = e.getXMPPError();
      return Response.status(error.getCode())
      .entity(rb.getString("chat.message.default.error"))
      .build();
    }
  }

  /**
   * @param username
   * @param needinfo
   * @return
   */
  @GET
  @Path("/getuserinfo/{username}/{needinfo}/")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getUserInfo(@PathParam("username") String username,
                              @PathParam("needinfo") String needinfo) {
    if (this.rb == null) loadResourceBundle();
    try {
      XMPPSession session = messenger.getSession(username);
      if (session != null) {
        return Response.ok(session.getUserInfo(needinfo), JSON_CONTENT_TYPE)
        .cacheControl(cc)
        .build();
      } else
        return Response.status(HTTPStatus.FORBIDDEN).build();
    } catch (Exception e) {
      if (log.isDebugEnabled()) 
        e.printStackTrace();
      return Response.status(HTTPStatus.BAD_REQUEST).entity(rb.getString("chat.message.default.error")).build();
    }
  }



  /**
   * @param forcache
   * @return
   */
  @GET
  @Path("/login2/{forcache}/")
  @Produces(MediaType.APPLICATION_JSON)
  public Response login2(@PathParam("forcache") String forcache) {
    if (this.rb == null) loadResourceBundle();
    try {
      // log.info("Random number for cache problem: " + forcache);
      ConversationState curentState = ConversationState.getCurrent();
      String username = curentState.getIdentity().getUserId();
      if (log.isDebugEnabled())
        log.info("Userid for login : " + username);
      /*String password = organization.getOrganizationService()
                                    .getUserHandler()
                                    .findUserByName(username)
                                    .getPassword();*/
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      UserInfoService organization = (UserInfoService) container.getComponentInstanceOfType(UserInfoService.class);
      String password = organization.providePassword(username);
      messenger.login(username, password, organization, delegate, history,rb);
      XMPPSession session = messenger.getSession(username);
      XMPPConnection connection = session.getConnection();
      String mainServiceName = session.getConnection().getServiceName();
      List<String> services = session.getSearchServices();
      Form form = session.getSearchForm(services.get(0));
      Roster buddyList = session.getConnection().getRoster();
      // ////// Temporary //////////
      //session.removeAllTransport();
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

      ContactBean myProfile = new ContactBean();
      myProfile.setUser(username);
      UserInfo myInfo = organization.getUserInfo(username);
      myProfile.setFullName(myInfo.getFirstName() + " " + myInfo.getLastName());
      initInfoBean.setMyProfile(myProfile);
      
      // Add 17/06
      session.addFullUserNames(username, myProfile.getFullName());
      
      initInfoBean.setSearchServicesNames(services);
      initInfoBean.setHostedRooms(rooms);
      initInfoBean.setTotalRooms(rooms.size());
      // TODO: temper temporarily comment until we not have confirmation about
      // receive messages
      // initInfoBean.setMessages(session.getNotRecieveMessages());
      return Response.ok(initInfoBean, JSON_CONTENT_TYPE)
      .cacheControl(cc)
      .header("Set-Cookie", "userTicket=" + UUID.randomUUID().toString())
      .build();
    } catch (XMPPException e) {
      /*if (log.isDebugEnabled()) 
        e.printStackTrace();*/
      XMPPError error = e.getXMPPError();
      return Response.status(error.getCode())
      .entity(error.getMessage())
      .build();
    } catch (Exception e) {
      /*if (log.isDebugEnabled())
        e.printStackTrace();*/
      return Response.status(HTTPStatus.INTERNAL_ERROR)
      .entity("Thrown exception : " + e)
      .build();
    }
  }

  /**
   * @param _username
   * @return
   */
  @GET
  @Path("/logout/{username}/")
  public Response logout(@PathParam("username") String _username) {
    if (this.rb == null) loadResourceBundle();
    try {
      XMPPSession session =    messenger.getSession(_username);
      if (session != null) session.removeAllTransport();
      messenger.logout(_username);
      return Response.ok().cacheControl(cc).build();
    } catch (XMPPException e) {
      if (log.isDebugEnabled()) 
        e.printStackTrace();
      XMPPError error = e.getXMPPError();
      return Response.status(error.getCode())
      .entity(error.getMessage())
      .build();
    }
  }

  /**
   * @param username
   * @param needinfo
   * @return
   */
  @GET
  @Path("/history/messagereceive/{username}/{messageid}/")
  public Response messageReceive(@PathParam("username") String username,
                                 @PathParam("messageid") String messageId) {
    if (this.rb == null) loadResourceBundle();
    try {
      XMPPSession session = messenger.getSession(username);
      if (session != null) {
        session.messageReceive(messageId);
        return Response.ok().cacheControl(cc).build();
      } else
        return Response.status(HTTPStatus.FORBIDDEN).build();
    } catch (Exception e) {
      if (log.isDebugEnabled())
        e.printStackTrace();
      return Response.status(HTTPStatus.INTERNAL_ERROR).build();
    }
  }

  /**
   * @param username
   * @param removeboddy
   * @return
   */
  @GET
  @Path("/roster/del/{username}/{removeboddy}/")
  public Response removeBuddy(@PathParam("username") String username,
                              @PathParam("removeboddy") String removeboddy) {
    if (this.rb == null) loadResourceBundle();
    try {
      XMPPSession session = messenger.getSession(username);
      if (session != null) {
        if (session.removeBuddy(removeboddy)) {
          return Response.ok().cacheControl(cc).build();
        } else {
          return Response.status(HTTPStatus.NOT_FOUND)
          .entity(rb.getString("chat.message.user.not.found"))
          .build();
        }
      } else {
        return Response.status(HTTPStatus.FORBIDDEN)
        .entity(rb.getString("chat.message.xmppsession.null"))
        .build();
      }
    } catch (XMPPException e) {
      if (log.isDebugEnabled()) 
        e.printStackTrace();
      XMPPError error = e.getXMPPError();
      return Response.status(error.getCode())
      .entity(rb.getString("chat.message.default.error"))
      .build();
    }
  }

  /**
   * @param username
   * @param _transport
   * @return
   */
  @GET
  @Path("/removetransport/{username}/{transport}/")
  public Response removeTransport(@PathParam("username") String username,
                                  @PathParam("transport") String _transport) {
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
        return Response.ok().cacheControl(cc).build();
      } else {
        return Response.status(HTTPStatus.FORBIDDEN)
        .entity("sesion is null")
        .build();
      }
    } catch (XMPPException e) {
      if (log.isDebugEnabled()) 
        e.printStackTrace();
      XMPPError error = e.getXMPPError();
      return Response.status(error.getCode())
      .entity(error.getMessage())
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
  @GET
  @Path("/searchuser/{username}/")
  @Produces(MediaType.APPLICATION_JSON)
  public Response searchUsers(@PathParam("username") String username,
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
        return Response.ok(TransformUtils.reportedSateToSearchResultsBean(reportedData),
                           JSON_CONTENT_TYPE).cacheControl(cc).build();
      }
      return Response.status(HTTPStatus.FORBIDDEN).entity("session null").build();
    } catch (XMPPException e) {
      if (log.isDebugEnabled()) 
        e.printStackTrace();
      XMPPError error = e.getXMPPError();
      return Response.status(error.getCode())
      .entity(rb.getString("chat.message.default.error"))
      .build();
    }
  }

  /**
   * @param username
   * @param messageBean
   * @return
   */
  @POST
  @Path("/sendmessage/{username}/")
  @Consumes(MediaType.APPLICATION_JSON)
  //@InputTransformer(Json2BeanInputTransformer.class)
  public Response sendMessage(@PathParam("username") String username, MessageBean messageBean) {
    if (this.rb == null) loadResourceBundle();
    XMPPSession session = messenger.getSession(username);
    if (session != null) {
      String from = session.getUsername().split("/")[0];
      Message message = new Message(messageBean.getTo(), Message.Type.chat);
      message.setFrom(from);
      message.setBody(messageBean.getBody());
      session.sendMessage(message);
      return Response.ok().cacheControl(cc).build();
    } else {
      return Response.status(HTTPStatus.BAD_REQUEST)
      .entity(rb.getString("chat.message.room.xmppsession.null"))
      .build();
    }
  }

  /**
   * @param username
   * @param messageBean
   * @return
   */
  @POST
  @Path("/muc/sendmessage/{username}/")
  @Consumes(MediaType.APPLICATION_JSON)
  //@InputTransformer(Json2BeanInputTransformer.class)
  public Response sendMUCMessage(@PathParam("username") String username, 
                                 MessageBean messageBean) {
    if (this.rb == null) loadResourceBundle();
    try {
      String room = messageBean.getTo();
      String body = messageBean.getBody();
      XMPPSession session = messenger.getSession(username);
      if (session != null) {
        session.sendMessageToMUC(room, body);
        return Response.ok().cacheControl(cc).build();
      } else {
        return Response.status(HTTPStatus.INTERNAL_ERROR).build();
      }
    } catch (XMPPException e) {
      if (log.isDebugEnabled()) 
        e.printStackTrace();
      XMPPError error = e.getXMPPError();
      return Response.status(error.getCode())
      .entity(rb.getString("chat.message.default.error"))
      .build();
    }

  }

  /**
   * @param username
   * @param status
   * @return
   */
  @GET
  @Path("/sendstatus/{username}/{status}/")
  public Response setUserStatus(@PathParam("username") String username,
                                @PathParam("status") String status) {
    if (this.rb == null) loadResourceBundle();
    XMPPSession session = messenger.getSession(username);
    DefaultPresenceStatus dps = new DefaultPresenceStatus();
    dps.setStatus_(status);
    //DefaultPresenceStatus dps = (DefaultPresenceStatus)container.getComponentInstance(DefaultPresenceStatus.class);
    if(dps != null){
      dps.savePresenceStatus(username, status);  
    }else   {
      log.debug("Can not save presence status from service sendstatus() method"); 
    }
    
    if(session != null){
      Presence presence = PresenceUtil.getPresence(status);
      if (presence == null)
        return Response.status(HTTPStatus.FORBIDDEN)
        .entity("Get unknow status.")
        .build();
      session.sendPresence(presence);
      return Response.ok().cacheControl(cc).build();
    }
    else {
      return Response.status(HTTPStatus.INTERNAL_ERROR)
      .entity(rb.getString("chat.message.room.xmppsession.null"))
      .build();
    }

  }

  /**
   * @param _username
   * @param _subsuser
   * @return
   */
  @GET
  @Path("/subscribeuser/{username}/{subsuser}/")
  public Response subscribeUser(@PathParam("username") String _username,
                                @PathParam("subsuser") String _subsuser) {
    if (this.rb == null) loadResourceBundle();
    XMPPSession session = messenger.getSession(_username);
    if (session != null) {
      session.subscribeUser(_subsuser);
      return Response.ok().cacheControl(cc).build();
    } else {
      return Response.status(HTTPStatus.INTERNAL_ERROR)
      .entity(rb.getString("chat.message.room.xmppsession.null"))
      .build();
    }
  }

  /**
   * @param username
   * @param unsubsuser
   * @return
   */
  @GET
  @Path("/unsubscribeuser/{username}/{unsubsuser}/")
  public Response unsubscribeUser(@PathParam("username") String username,
                                  @PathParam("unsubsuser") String unsubsuser) {
    if (this.rb == null) loadResourceBundle();
    XMPPSession session = messenger.getSession(username);
    if (session != null) {
      session.unsubscribeUser(unsubsuser);
      return Response.ok().cacheControl(cc).build();
    } else {
      return Response.status(HTTPStatus.INTERNAL_ERROR)
      .entity(rb.getString("chat.message.room.xmppsession.null"))
      .build();
    }
  }

  /**
   * @param username
   * @param uuid
   * @return
   */
  @GET
  @Path("/fileexchange/accept/{username}/{uuid}/")
  //@OutputTransformer(PassthroughOutputTransformer.class)
  public Response acceptFile(@PathParam("username") String username, @PathParam("uuid") String uuid) {
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
          CacheControl ccIEfixed = new CacheControl();//Fix for http://jira.exoplatform.org/browse/CS-3319
          //          MultivaluedMetadata headers= new MultivaluedMetadata();
          //          headers.putSingle("Content-disposition",
          //                            "attachment; filename=\"" + fileTransfer.getFileName()
          //                            + "\"");
          //          headers.putSingle("Expires", "Sun, 17 Dec 1989 07:30:00 GMT");
          return Response.ok(fileTransfer.recieveFile(), DEFAULT_CONTENT_TYPE)
          .header("Content-disposition",
                  "attachment; filename=\"" + fileTransfer.getFileName()
                  + "\"").header("Expires", "Sun, 17 Dec 1989 07:30:00 GMT")
                  .cacheControl(ccIEfixed)
                  .build();
        } else {
          String errorMessage = rb.getString("chat.message.filetransfer.sender.accept.offline");
          session.sendErrorMessage(errorMessage.trim(),sender);
          return Response.status(HTTPStatus.NOT_FOUND)
          .entity(rb.getString("chat.message.filetransfer.sender.accept.offline"))
          .build();
        }
      } else {
        return Response.status(HTTPStatus.INTERNAL_ERROR)
        .entity(rb.getString("chat.message.room.xmppsession.null"))
        .build();
      }
    } catch (Exception e) {
      if (log.isDebugEnabled()) 
        e.printStackTrace();
      return Response.status(HTTPStatus.BAD_REQUEST)
      .entity(e.getMessage())
      .build();
    }

  }

  /**
   * @param username
   * @param uuid
   * @return
   */
  @GET
  @Path("/fileexchange/reject/{username}/{uuid}/")
  public Response rejectFile(@PathParam("username") String username,
                             @PathParam("uuid") String uuid) {
    if (this.rb == null) loadResourceBundle();
    XMPPSessionImpl session = (XMPPSessionImpl) messenger.getSession(username);
    String sender;
    if (session != null) {
      FileTransferRequest request = session.getFileTransferRequest(uuid);
      sender = request.getRequestor();
      Presence presence = session.getConnection().getRoster().getPresence(sender);
      if (presence.getType().equals(Presence.Type.available)) {
        request.reject();
        return Response.ok().cacheControl(cc).build();
      } else {
        String errorMessage = rb.getString("chat.message.filetransfer.sender.reject.offline");
        session.sendErrorMessage(errorMessage.trim(),sender);
        return Response.status(HTTPStatus.NOT_FOUND)
        .entity(rb.getString("chat.message.filetransfer.sender.reject.offline"))
        .build();
      }
    } else {
      return Response.status(HTTPStatus.INTERNAL_ERROR)
      .entity(rb.getString("chat.message.room.xmppsession.null"))
      .build();
    }
  }

  @GET
  @Path("/getprevstatus/{username}/")
  public Response getPreviousStatus(@PathParam("username") String username){
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    DefaultPresenceStatus dps = null;
    if (this.rb == null) loadResourceBundle();
    XMPPSession session = messenger.getSession(username);
    if(session != null){
      if(container != null)
        dps = (DefaultPresenceStatus)container.getComponentInstance(DefaultPresenceStatus.class);
      if(dps != null){
        Map<String, String> statusmap = dps.getPreviousStatus(username);
        String responseText = "<staustext>" + statusmap.keySet().toArray(new String[]{""})[0]+ "</staustext>";
        responseText += "<responseIcon>" + statusmap.values().toArray(new String[]{""})[0] + "</responseIcon>"; 
        return Response.ok().entity(responseText).build();
      }
    }else {
      return Response.status(HTTPStatus.INTERNAL_ERROR)
      .entity(rb.getString("chat.message.room.xmppsession.null"))
      .build();
    }
    
    return Response.ok().entity("Away").build();
  }
}
