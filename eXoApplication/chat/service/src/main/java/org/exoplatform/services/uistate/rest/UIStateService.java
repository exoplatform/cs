/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.services.uistate.rest;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.rest.impl.RuntimeDelegateImpl;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.uistate.UIStateSession;
import org.exoplatform.services.uistate.bean.UIStateDataBean;
import org.exoplatform.services.xmpp.connection.impl.XMPPMessenger;

/**
 * Created by The eXo Platform SAS
 * Author : viet.nguyen
 *          vietnt84@gmail.com
 * Jan 25, 2010  
 */

@Path("/uistateservice")
public class UIStateService implements ResourceContainer {
  private static final CacheControl cc;
  
  private static String unreadMessageCount;
  
  static {
  //TODO: to find the reason why UIStateService loaded before ResourceBinder
  RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
    cc = new CacheControl();
    cc.setNoCache(true);
    cc.setNoStore(true);

  }
  
  /**
   * 
   */
  public UIStateService() {
  }
  
  @POST 
  @Path("/save/{username}/{unreadMessageCnt}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response saveState(@PathParam("username") String userName, @PathParam("unreadMessageCnt") String unreadMessageCnt, UIStateDataBean stateData) throws Exception {
    try {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      XMPPMessenger messenger = (XMPPMessenger) container.getComponentInstanceOfType(XMPPMessenger.class);
      if(messenger != null){
        UIStateSession uiSession = messenger.getUISateSession(userName);
        stateData = (stateData != null) ? stateData : new UIStateDataBean();
        uiSession.setUIStateData(stateData);
      }
    } catch (Exception e){
    }
    unreadMessageCount = unreadMessageCnt;
    UIStateDataBean stateDataBean = new UIStateDataBean("null");
    stateDataBean.setUnreadMessageCnt(unreadMessageCount);
    return Response.ok(stateDataBean, MediaType.APPLICATION_JSON).cacheControl(cc).build();
  }
  
  @GET
  @Path("/get/{username}/")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getState(@PathParam("username") String userName) throws Exception {  
    try {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      XMPPMessenger messenger = (XMPPMessenger) container.getComponentInstanceOfType(XMPPMessenger.class);
      if(messenger != null){
        UIStateSession uiSession = messenger.getUISateSession(userName);
        UIStateDataBean uiStateData = uiSession.getUIStateData();
        uiStateData = (uiStateData != null) ? uiStateData : new UIStateDataBean();;
        return Response.ok(uiStateData, MediaType.APPLICATION_JSON).cacheControl(cc).build();
      }
    } catch (Exception e){
    }
    UIStateDataBean stateDataBean = new UIStateDataBean();
    return Response.ok(stateDataBean, MediaType.APPLICATION_JSON).cacheControl(cc).build();
  }
}
