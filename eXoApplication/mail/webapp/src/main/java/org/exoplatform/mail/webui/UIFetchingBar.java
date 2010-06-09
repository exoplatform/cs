/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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

import javax.jcr.PathNotFoundException;

import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.CheckingInfo;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.StatusInfo;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;

/**
 * Created by The eXo Platform SAS
 * Author : Phung Nam
 *          phunghainam@gmail.com
 * 15-01-2009  
 */

@ComponentConfig(
  lifecycle = UIFormLifecycle.class, 
  template =  "app:/templates/mail/webui/UIFetchingBar.gtmpl",
  events = {
	@EventConfig(listeners = UIFetchingBar.RefreshActionListener.class),
	@EventConfig(listeners = UIFetchingBar.UpdateListActionListener.class),
  @EventConfig(listeners = UIFetchingBar.UpdateFolderActionListener.class)
  }
)
public class UIFetchingBar extends UIForm {
	
  private boolean isShown_ = false;
  private MailService mailService ;
  private StatusInfo statusInfo;
  
  public UIFetchingBar() throws Exception {
    mailService = MailUtils.getMailService();
    
  }
	
  
  
  public StatusInfo loadStatusInfo() {
    UIMailPortlet mailportlet = this.getAncestorOfType(UIMailPortlet.class);
    
    try {
      CheckingInfo ckInfo = mailService.getCheckingInfo(mailportlet.getRemoteUser(), mailportlet.getAccountId());
      if (ckInfo != null) {
        statusInfo = ckInfo.getStatus();        
      }
    } catch (Exception e) {
       e.printStackTrace();
    }
    return statusInfo;
  }
  /**
   * check: show this bar or not.
   * @return
   */
  public boolean isShown() {
    if (statusInfo != null) {
      if (statusInfo.getStatus() != CheckingInfo.FINISHED_CHECKMAIL_STATUS) {
        return true;
      }
    }
    return false;

  }
  
  /**
   * check: show status text or not
   * @return
   */
  public boolean showStatusText() {
    if (statusInfo != null) {
      if (statusInfo.getStatus() != CheckingInfo.CONNECTION_FAILURE 
          && statusInfo.getStatus() != CheckingInfo.RETRY_PASSWORD && 
          statusInfo.getStatus() != CheckingInfo.COMMON_ERROR
          && statusInfo.getStatus() != CheckingInfo.REQUEST_STOP_STATUS) {
        return true;
      }
      
    }
    return false;
  }
  
  public boolean showStopIcon() {
    if (statusInfo != null) {
      if (statusInfo.getStatus() != CheckingInfo.CONNECTION_FAILURE 
          && statusInfo.getStatus() != CheckingInfo.RETRY_PASSWORD && 
          statusInfo.getStatus() != CheckingInfo.COMMON_ERROR && 
          statusInfo.getStatus() != CheckingInfo.FINISHED_CHECKMAIL_STATUS && 
          statusInfo.getStatus() != CheckingInfo.REQUEST_STOP_STATUS) {
        return true;
      }
      
    }
    return false;
  }
  
  public boolean showStoppingIcon() {
    if (statusInfo != null) {
      return !showStopIcon() && statusInfo.getStatus() == CheckingInfo.REQUEST_STOP_STATUS;
    }
     return false;
  }
  
  public boolean showLoadingIcon() {
    if (statusInfo != null) {
      return statusInfo.getStatus() != CheckingInfo.FINISHED_CHECKMAIL_STATUS && 
      statusInfo.getStatus() != CheckingInfo.CONNECTION_FAILURE 
      && statusInfo.getStatus() != CheckingInfo.RETRY_PASSWORD && 
      statusInfo.getStatus() != CheckingInfo.COMMON_ERROR;
    }
    return false;
  }
  
  public boolean showWarningMessage() {
    if (statusInfo != null) {
      return !showStatusText() && statusInfo.getStatus() != CheckingInfo.REQUEST_STOP_STATUS;
    }
     return false;
  }
  
  /**
   * 
   * @param b
   */
  
  public void setIsShown(boolean b) { isShown_ = b; }
  
  public boolean isUpdate() throws Exception {
  	UIMessageList uiMsgList = getAncestorOfType(UIMailPortlet.class).findFirstComponentOfType(UIMessageList.class);
  	if(uiMsgList.getMessagePageList() != null) {
  	  if (uiMsgList.getMessagePageList().getCurrentPage() > 1 
  			|| uiMsgList.getMessagePageList().getAvailablePage() > 1) {
  	    return false;
  	  }
  	} 
  	return true;
  }
	
  static public class RefreshActionListener extends EventListener<UIFetchingBar> {
    public void execute(Event<UIFetchingBar> event) throws Exception {
      UIFetchingBar uiFetchingBar = event.getSource();
      UIMailPortlet uiPortlet = uiFetchingBar.getAncestorOfType(UIMailPortlet.class);
      UIMessageList uiMsgList = uiPortlet.findFirstComponentOfType(UIMessageList.class) ;    
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      if (accountId == null) return ;
      //cs-2127 
      try {
        uiMsgList.init(accountId);
      } catch (PathNotFoundException e) {
        uiMsgList.setMessagePageList(null) ;
        uiPortlet.findFirstComponentOfType(UISelectAccount.class).refreshItems();
        uiPortlet.findFirstComponentOfType(UISelectAccount.class).setSelectedValue(null) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet); 
        
        UIApplication uiApp = uiMsgList.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIFetchingBar.msg.deleted_account", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;        
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.findFirstComponentOfType(UIFolderContainer.class));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMsgList.getParent());
      UIFolderContainer folderContainer = uiPortlet.findFirstComponentOfType(UIFolderContainer.class);
      folderContainer.setIsChecking(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(folderContainer);
    }
  }	
	
  static public class UpdateListActionListener extends EventListener<UIFetchingBar> {
    public void execute(Event<UIFetchingBar> event) throws Exception {
      UIFetchingBar uiFetchingBar = event.getSource();
      UIMailPortlet uiPortlet = uiFetchingBar.getAncestorOfType(UIMailPortlet.class);
      UIMessageList uiMsgList = uiPortlet.findFirstComponentOfType(UIMessageList.class) ;    
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      MailService mailSrv = uiMsgList.getApplicationComponent(MailService.class);
      String username = uiPortlet.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      Message msg = null ;
      try {
        msg = mailSrv.getMessageById(username, accountId, msgId);
      }  catch (PathNotFoundException e) {

        uiMsgList.setMessagePageList(null) ;
        uiPortlet.findFirstComponentOfType(UISelectAccount.class).refreshItems();
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet); 
        
        UIApplication uiApp = uiMsgList.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIFetchingBar.msg.deleted_account", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;        
      }
      UIFormCheckBoxInput<Boolean> uiCheckBox = new UIFormCheckBoxInput<Boolean>(msg.getId(), msg.getId(), false);
      uiMsgList.addUIFormInput(uiCheckBox);
      boolean updateList = false ;
      if (msg.getFolders() != null && msg.getFolders().length >= 1) {
        for (int i = 0; i < msg.getFolders().length; i++) {
          if (uiMsgList.getSelectedFolderId() != null && msg.getFolders()[i].equals(uiMsgList.getSelectedFolderId())) 
            updateList = true ;
        }
      }
      if (updateList) uiMsgList.messageList_.put(msg.getId(), msg);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMsgList);   
    }
  }
  
  static public class UpdateFolderActionListener extends EventListener<UIFetchingBar> {
    public void execute(Event<UIFetchingBar> event) throws Exception {
      UIFetchingBar uiForm = event.getSource() ;  
      UIMailPortlet uiPortlet = uiForm.getAncestorOfType(UIMailPortlet.class);
      UIFolderContainer uiFolderContainer = uiPortlet.findFirstComponentOfType(UIFolderContainer.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiFolderContainer) ;
    }
  }
}
