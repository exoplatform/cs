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

import org.exoplatform.common.http.HTTPMethods;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.rest.CacheControl;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.InputTransformer;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URIParam;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.uistate.UIStateSession;
import org.exoplatform.services.uistate.bean.UIStateDataBean;
import org.exoplatform.services.xmpp.connection.impl.XMPPMessenger;
import org.exoplatform.ws.frameworks.json.transformer.Bean2JsonOutputTransformer;
import org.exoplatform.ws.frameworks.json.transformer.Json2BeanInputTransformer;

/**
 * Created by The eXo Platform SAS
 * Author : viet.nguyen
 *          vietnt84@gmail.com
 * Jan 25, 2010  
 */

public class UIStateService implements ResourceContainer {
  
  private static final String JSON_CONTENT_TYPE    = "application/json";
  
  private static final CacheControl cc;
  
  private static String unreadMessageCount;
  
  static {
    cc = new CacheControl();
    cc.setNoCache(true);
    cc.setNoStore(true);
  }
  
  /**
   * 
   */
  public UIStateService() {
  }
  
  @HTTPMethod(HTTPMethods.POST)
  @URITemplate("/uistateservice/save/{username}/{unreadMessageCnt}/")
  @InputTransformer(Json2BeanInputTransformer.class)
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response saveState(@URIParam("username") String userName,  @URIParam("unreadMessageCnt") String unreadMessageCnt, UIStateDataBean stateData) throws Exception {
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
    return Response.Builder.ok(stateDataBean, JSON_CONTENT_TYPE).cacheControl(cc).build();
  }
  
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/uistateservice/get/{username}/")
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response getState(@URIParam("username") String userName) throws Exception {  
    try {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      XMPPMessenger messenger = (XMPPMessenger) container.getComponentInstanceOfType(XMPPMessenger.class);
      if(messenger != null){
        UIStateSession uiSession = messenger.getUISateSession(userName);
        UIStateDataBean uiStateData = uiSession.getUIStateData();
        uiStateData = (uiStateData != null) ? uiStateData : new UIStateDataBean();;
        return Response.Builder.ok(uiStateData, JSON_CONTENT_TYPE).cacheControl(cc).build();
      }
    } catch (Exception e){
    }
    UIStateDataBean stateDataBean = new UIStateDataBean();
    return Response.Builder.ok(stateDataBean, JSON_CONTENT_TYPE).cacheControl(cc).build();
  }
}
