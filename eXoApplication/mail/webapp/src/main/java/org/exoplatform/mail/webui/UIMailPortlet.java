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


import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.PathNotFoundException;
import javax.servlet.http.HttpServletRequest;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.cs.common.webui.UIPopupAction;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.MailSetting;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.MessageFilter;
import org.exoplatform.mail.service.MessagePageList;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPopupMessages;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;
import org.mortbay.cometd.AbstractBayeux;
import org.mortbay.cometd.continuation.EXoContinuationBayeux;

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
  
  private String formId = "";
  
  public String getFormId() {
    return formId;
  }

  public void setFormId(String formId) {
    this.formId = formId;
  }

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
  
  @Override
  public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {
    PortalRequestContext portalContext = Util.getPortalRequestContext();
    String isAjax = portalContext.getRequestParameter("ajaxRequest");
    if(isAjax != null && Boolean.parseBoolean(isAjax)) {
      super.processRender(app, context);
      return;
    }
    String url = ((HttpServletRequest)portalContext.getRequest()).getRequestURL().toString();
    try {
      MailService mailService = MailUtils.getMailService();
      String username = MailUtils.getCurrentUser();
      String[] content = url.split("/");
      int length = content.length;
      String account = content[length-4];
      if (mailService.getAccountById(username, account) == null) throw new PathNotFoundException();
      String folder = content[length-3];
      String tag = content[length-2];
      String msgId = URLDecoder.decode(content[length-1], "UTF-8");
      UISelectAccount uiSelectAccount = getChild(UINavigationContainer.class).getChild(UISelectAccount.class);
      uiSelectAccount.setSelectedValue(account);
      UIMessageArea uiMessageArea = getChild(UIMessageArea.class);
      uiMessageArea.init(account);
      MessageFilter filter = new MessageFilter("Folder");
      if (!Utils.isEmptyField(folder) && !folder.equals("_")) {
        UIFolderContainer uiFolderContainer = findFirstComponentOfType(UIFolderContainer.class);
        uiFolderContainer.setSelectedFolder(folder);
        filter.setFolder(new String[] { folder });
      } else if (!Utils.isEmptyField(tag) && !tag.equals("_")) {
        UITagContainer uiTagContainer = findFirstComponentOfType(UITagContainer.class);
        uiTagContainer.setSelectedTagId(tag);
        filter.setTag(new String[] {tag});
      }
      UIMessageList uiMessageList = findFirstComponentOfType(UIMessageList.class);
      boolean isFound = false;
            
      filter.setAccountId(account) ;
      MessagePageList currentPageList = mailService.getMessagePageList(username, filter) ;
      uiMessageList.setMessagePageList(currentPageList);
      for (int page = 1; page <= currentPageList.getAvailablePage(); page ++) {
        List<Message> messList = currentPageList.getPage(page, MailUtils.getCurrentUser());
        for (Message message : messList)
          if (message.getId().equals(msgId)) {
            uiMessageList.updateList(page);
            message.setUnread(false);
            uiMessageList.messageList_.put(msgId, message);
            uiMessageList.setSelectedMessageId(msgId);
            UIFormCheckBoxInput<Boolean> uiCheckbox = uiMessageList.getChildById(msgId);
            if (uiCheckbox != null ) {
              uiCheckbox.setChecked(true);
            }            
            List<Message> showedMessages = new ArrayList<Message>() ;
            showedMessages.add(message) ;
            mailService.toggleMessageProperty(username, account, showedMessages, folder, Utils.EXO_ISUNREAD, false);
            UIMessagePreview uiMessagePreview = findFirstComponentOfType(UIMessagePreview.class);
            message = MailUtils.getMailService().loadTotalMessage(MailUtils.getCurrentUser(), account, message) ;
            uiMessagePreview.setMessage(message);
            uiMessagePreview.setShowedMessages(showedMessages) ;
            isFound = true;
          }
        if (isFound) break;
      }    
      context.addUIComponentToUpdateByAjax(this);
    }catch (PathNotFoundException ex) {
      
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      super.processRender(app, context);      
    }   
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
} 
