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
 **/
package org.exoplatform.chatbar.webui;

import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;
import org.mortbay.cometd.AbstractBayeux;
import org.mortbay.cometd.continuation.EXoContinuationBayeux;

/**
 * Author : pham tuan
 *          tuan.pham@exoplatform.com
 * May 04, 2009
 */
@ComponentConfig(
                 lifecycle = UIApplicationLifecycle.class
)
public class UIChatBarPortlet extends UIPortletApplication {
  private Log log = ExoLogger.getLogger(this.getClass());
  private String windowId; 

  protected static final String VIEWMODE_TEMP = "app:/templates/chatbar/webui/UIChatBarPortlet.gtmpl" ;
  protected static final String EDITMODE_TEMP = "app:/templates/chatbar/webui/UIChatBarEdit.gtmpl" ;
  private static final String BASE_URL_VALUE = "/portal/intranet/";
  private static final String BASE_URL_KEY = "cs.chatbar.shortcut.baseUrl";

  private String templatePath_ = VIEWMODE_TEMP ;

  private String status_  = null;
  
  public UIChatBarPortlet() throws Exception {
    PortletRequestContext context = (PortletRequestContext)  WebuiRequestContext.getCurrentInstance() ;
    PortletRequest prequest = context.getRequest() ;
    windowId = prequest.getWindowID() ;
    
    //get previous status, now we use not this mesthod.
/*  ExoContainer container = ExoContainerContext.getCurrentContainer();
    XMPPMessenger messenger = (XMPPMessenger) container.getComponentInstanceOfType(XMPPMessenger.class);
    XMPPSession session = messenger.getSession(this.getRemoteUser());
    DefaultPresenceStatus dps = null;
    if(container != null) dps = (DefaultPresenceStatus)container.getComponentInstance(DefaultPresenceStatus.class);
    if(session != null){//chat server available
      if(dps != null){
        String ps = dps.getPreviousStatus(this.getRemoteUser());
        if(ps != null) setStatus(ps);
      }  
    }else  setStatus(null);*/
  }

  public String getId() {
    return windowId ;
  }

  public String getRemoteUser() {
    return Util.getPortalRequestContext().getRemoteUser() ;
  }

  public String getUserToken() {
    try {
      return this.getContinuationService().getUserToken(this.getRemoteUser());
    } catch (Exception e) {
      log.info("\n\n can not get UserToken");
      return "" ;
    }
  }

  protected ContinuationService getContinuationService() {
    ContinuationService continuation = (ContinuationService) PortalContainer.getInstance().getComponentInstanceOfType(ContinuationService.class);
    return continuation;
  }

  protected String getRestContextName() {
    String restBaseUri = Util.getPortalRequestContext().getRequestContextPath() + "/" + PortalContainer.getInstance().getRestContextName();
    if(restBaseUri.startsWith("/")){
      restBaseUri = restBaseUri.substring(1, restBaseUri.length());
    }
    return restBaseUri;
  }

  protected String getCometdContextName() {
    String cometdContextName = "cometd";
    try {
      EXoContinuationBayeux bayeux = (EXoContinuationBayeux) PortalContainer.getInstance()
      .getComponentInstanceOfType(AbstractBayeux.class);
      return (bayeux == null ? "cometd" : bayeux.getCometdContextName());
    } catch (Exception e) {
    }
    return cometdContextName;
  }

  public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {    
    try {
      PortletRequestContext portletReqContext = (PortletRequestContext)  context ;
      if(portletReqContext.getApplicationMode() == PortletMode.VIEW) {
        templatePath_ = VIEWMODE_TEMP;
      } else if(portletReqContext.getApplicationMode() == PortletMode.EDIT) {
        UIConfigForm uiForm = getChild(UIConfigForm.class) ;
        if(uiForm == null) uiForm = addChild(UIConfigForm.class, null, null);
        uiForm.reset() ;
        uiForm.init();

        templatePath_ = EDITMODE_TEMP;
      }
      super.processRender(app, context);
    } catch (Exception e) {
      log.error("Cannot display the content of the chatbar", e);
    }
  }

  public String getTemplate() {
    return templatePath_;
  }

  public void setTemplate(String temp) {
    templatePath_ = temp;
  }
  
  protected boolean isShowEmailLink () {
    return Boolean.parseBoolean(getPortletPreferences().getValue(UIConfigForm.MAIL_APP, null));
  }
  protected boolean isShowCalendarLink () {
    return Boolean.parseBoolean(getPortletPreferences().getValue(UIConfigForm.CAL_APP, null));
  }
  protected boolean isShowContactLink () {
    return Boolean.parseBoolean(getPortletPreferences().getValue(UIConfigForm.CON_APP, null));
  }
  
  protected String getEmailLink () {
    String path = getPortletPreferences().getValue(UIConfigForm.MAIL_URL, null);
    return getShortcutBaseUrl() + path;
  }
  
  
  private String getShortcutBaseUrl() {
    String sUrl = null;
    try {
      sUrl = System.getProperties().getProperty(BASE_URL_KEY);
    } catch (Exception e) {
    }
    if(!StringUtils.isNotBlank(sUrl)){
      sUrl = BASE_URL_VALUE;
    } 
    return sUrl;
  }

  protected String getCalendarLink () {
    String path = getPortletPreferences().getValue(UIConfigForm.CAL_URL, null);
    return getShortcutBaseUrl() + path;
  }
  protected String getContactLink () {
    String path = getPortletPreferences().getValue(UIConfigForm.CON_URL, null);
    return getShortcutBaseUrl() + path;
  }
  

  private PortletPreferences getPortletPreferences() {
    PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
    return pcontext.getRequest().getPreferences() ;
  }
  
  /**
   * Get StatusText, use for show title of status***/
  public String getStatus(){
    return status_;
  }
  public void setStatus(String status){ status_ = status;}
}
