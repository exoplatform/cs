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
package org.exoplatform.mail.webui;


import org.exoplatform.container.PortalContainer;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.MailSetting;
import org.exoplatform.mail.webui.popup.UIPopupAction;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPopupMessages;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;

/**
 * Author : Phung Hai Nam
 *          phunghainam@gmail.com
 * May 30, 2006
 */
@ComponentConfig(
   lifecycle = UIApplicationLifecycle.class,
   template = "app:/templates/mail/webui/UIMailPortlet.gtmpl"
)
public class UIMailPortlet extends UIPortletApplication {
  public UIMailPortlet() throws Exception {
    addChild(UIActionBar.class, null, null) ;
    addChild(UINavigationContainer.class, null, null) ;
    String accId = getChild(UINavigationContainer.class).getChild(UISelectAccount.class).getSelectedValue();
    UIMessageArea uiMessageArea = createUIComponent(UIMessageArea.class, null, null);
    uiMessageArea.init(accId);
    uiMessageArea.setMailSetting(getMailSetting());
    addChild(uiMessageArea);
    addChild(UIPopupAction.class, null, null) ;
  }
  
  public String getAccountId() {
    return getChild(UINavigationContainer.class).getChild(UISelectAccount.class).getSelectedValue();
  }
  
  public String getCurrentUser() {
    return Util.getPortalRequestContext().getRemoteUser() ;
  }
  
  public long getPeriodCheckAuto() throws Exception {
    Long period = getMailSetting().getPeriodCheckAuto() * 60 * 1000 ;
    return period ;
  }
  
  public MailSetting getMailSetting() throws Exception {
    MailService mailSrv = getApplicationComponent(MailService.class) ;
    return mailSrv.getMailSetting(getCurrentUser()) ;    
  }
  
  public void renderPopupMessages() throws Exception {
    UIPopupMessages popupMess = getUIPopupMessages();
    if(popupMess == null)  return ;
    WebuiRequestContext  context =  WebuiRequestContext.getCurrentInstance() ;
    popupMess.processRender(context);
  }
  public void cancelAction() throws Exception {
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
    UIPopupAction popupAction = getChild(UIPopupAction.class) ;
    popupAction.deActivate() ;
    context.addUIComponentToUpdateByAjax(popupAction) ;
  }
  public String getRemoteUser() throws Exception {
    return CalendarUtils.getCurrentUser() ;
  }
  public String getUserToken()throws Exception {
    ContinuationService continuation = getApplicationComponent(ContinuationService.class) ;
    try {
        return continuation.getUserToken(this.getRemoteUser());
	  } catch (Exception e) {
		  System.out.println("\n\n can not get UserToken");
		  return "" ;
	  }
  }
  public String getRestContextName(){	  
	  return PortalContainer.getInstance().getRestContextName();
  }
} 
