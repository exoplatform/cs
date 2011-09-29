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

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.cs.common.webui.UIPopupAction;
import org.exoplatform.cs.common.webui.UIPopupActionContainer;
import org.exoplatform.mail.DataCache;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.popup.UIAccountCreation;
import org.exoplatform.mail.webui.popup.UIAccountSetting;
import org.exoplatform.mail.webui.popup.UIAddressBookForm;
import org.exoplatform.mail.webui.popup.UIComposeForm;
import org.exoplatform.mail.webui.popup.UIEventForm;
import org.exoplatform.mail.webui.popup.UIMailSettings;
import org.exoplatform.mail.webui.popup.UIMessageFilter;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
                 template =  "app:/templates/mail/webui/UIActionBar.gtmpl",
                 events = {
                     @EventConfig(listeners = UIActionBar.ComposeActionListener.class),
                     @EventConfig(listeners = UIActionBar.CheckMailActionListener.class),
                     @EventConfig(listeners = UIActionBar.AddressActionListener.class),
                     @EventConfig(listeners = UIActionBar.AddEventActionListener.class),
                     @EventConfig(listeners = UIActionBar.FilterActionListener.class),
                     @EventConfig(listeners = UIActionBar.MailSettingsActionListener.class),
                     @EventConfig(listeners = UIActionBar.AccountSettingsActionListener.class)
                 }
)

public class UIActionBar extends UIContainer {
  private static final Log log = ExoLogger.getExoLogger(UIActionBar.class);

  public UIActionBar()throws Exception {}

  static  public class CheckMailActionListener extends EventListener<UIActionBar> {    
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource();
      UIMailPortlet uiPortlet = uiActionBar.getAncestorOfType(UIMailPortlet.class);
      DataCache dataCache = uiPortlet.getDataCache();
      
      UIApplication uiApp = uiActionBar.getAncestorOfType(UIApplication.class) ;
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
      String formId = ((PortletRequestContext)context).getWindowId();
      uiPortlet.setFormId(formId);
      
      String username = uiPortlet.getCurrentUser();
      String accId = dataCache.getSelectedAccountId();
      if(Utils.isEmptyField(accId) || (dataCache.getAccounts(username).isEmpty() && dataCache.getDelegatedAccounts(username).isEmpty())) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.account-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      
      Folder currentF = null;
      UIFolderContainer uiFolderContainer = uiPortlet.findFirstComponentOfType(UIFolderContainer.class);
      String folderId = uiFolderContainer.getSelectedFolder();
      try {
        if (MailUtils.isFieldEmpty(folderId)) {
          context.getJavascriptManager().addJavascript("eXo.mail.MailServiceHandler.checkMail(true) ;");
        } else {
          context.getJavascriptManager().addJavascript("eXo.mail.MailServiceHandler.checkMail(true, '" + folderId + "') ;");
          currentF = uiFolderContainer.getCurrentFolder();
          if (currentF.getNumberOfUnreadMessage() < 0) {
            currentF.setNumberOfUnreadMessage(0);
          }
        }
        context.getJavascriptManager().addJavascript("eXo.mail.MailServiceHandler.showStatusBox('checkmail-notice') ;");
        uiPortlet.findFirstComponentOfType(UIFetchingBar.class).setIsShown(true);
      } catch (Exception e) {
        if (log.isDebugEnabled()) {
          log.debug("Exception in method execute of class CheckMailActionListener", e);
        }
        return;
      }
      
      UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType(UIMessageList.class) ;
      List<Message> msgList = new  ArrayList<Message>(uiMessageList.messageList_.values());
      long numberOfUnread = Utils.getNumberOfUnreadMessageReally(msgList);
      if ((numberOfUnread >= 0) && !MailUtils.isFieldEmpty(folderId) && (msgList.size() > 0)) {
        currentF.setNumberOfUnreadMessage(numberOfUnread);
      }
      UIMessageArea uiMessageArea = uiPortlet.findFirstComponentOfType(UIMessageArea.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageArea) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiFolderContainer);
    }
  }

  static public class ComposeActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource(); 
      UIMailPortlet uiPortlet = uiActionBar.getParent();
      DataCache dataCache = uiPortlet.getDataCache();
      
      UIApplication uiApp = uiActionBar.getAncestorOfType(UIApplication.class) ;
      String accId = dataCache.getSelectedAccountId();
      String username = uiPortlet.getCurrentUser();
      
      Account acc = dataCache.getDelegatedAccount(username, accId);
      if(Utils.isEmptyField(accId) || (dataCache.getAccounts(username).isEmpty() && !MailUtils.isFull(accId, dataCache))) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.account-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } else if(MailUtils.isDelegatedAccount(acc, username)) {
        if(!MailUtils.isFull(username, acc.getPermissions().get(username))) {
          uiActionBar.showMessage(event);
        } else {
          UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
          UIPopupActionContainer uiPopupContainer = uiPopupAction.createUIComponent(UIPopupActionContainer.class, null, "UIPopupActionComposeContainer") ;
          uiPopupAction.activate(uiPopupContainer, MailUtils.MAX_POPUP_WIDTH, 0, true);
          UIComposeForm uiComposeForm = uiPopupContainer.createUIComponent(UIComposeForm.class, null, null);
          uiComposeForm.init(accId, null, 0);
          uiPopupContainer.addChild(uiComposeForm) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
        }
      } else {
        UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
        UIPopupActionContainer uiPopupContainer = uiPopupAction.createUIComponent(UIPopupActionContainer.class, null, "UIPopupActionComposeContainer") ;
        uiPopupAction.activate(uiPopupContainer, MailUtils.MAX_POPUP_WIDTH, 0, true);
        UIComposeForm uiComposeForm = uiPopupContainer.createUIComponent(UIComposeForm.class, null, null);
        uiComposeForm.init(accId, null, 0);
        uiPopupContainer.addChild(uiComposeForm) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      }
    }
  }

  static public class AddressActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ; 
      UIMailPortlet uiPortlet = uiActionBar.getAncestorOfType(UIMailPortlet.class);
      UIPopupAction uiPopupAction = uiPortlet.findFirstComponentOfType(UIPopupAction.class);
      UIPopupActionContainer uiPopupContainer = uiPopupAction.createUIComponent(UIPopupActionContainer.class, null, "UIPopupActionAddressContainer");
      uiPopupAction.activate(uiPopupContainer, 800, 0, true) ;
      UIAddressBookForm uiAddressBookForm = uiPopupContainer.createUIComponent(UIAddressBookForm.class, null, null);
      String contactId = event.getRequestContext().getRequestParameter(OBJECTID);
      uiAddressBookForm.setSelectedContactMap(uiAddressBookForm, contactId);
      uiPopupContainer.addChild(uiAddressBookForm) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }

  static public class AddEventActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ; 
      CalendarService calendarService = uiActionBar.getApplicationComponent(CalendarService.class);
      UIMailPortlet uiPortlet = uiActionBar.getParent() ;
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
      UIPopupActionContainer uiPopupContainer = uiPopupAction.createUIComponent(UIPopupActionContainer.class, null, "UIPopupActionEventContainer");
      uiPopupAction.activate(uiPopupContainer, 600, 0, true) ;
      UIEventForm uiEventForm = uiPopupContainer.createUIComponent(UIEventForm.class, null, null);
      uiPopupContainer.addChild(uiEventForm) ;
      uiEventForm.initForm(calendarService.getCalendarSetting(MailUtils.getCurrentUser()), null) ;
      uiEventForm.update(CalendarUtils.PRIVATE_TYPE, null) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }

  static public class FilterActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource(); 
      UIMailPortlet uiPortlet = uiActionBar.getAncestorOfType(UIMailPortlet.class);
      DataCache dataCache = uiPortlet.getDataCache();
      
      UIApplication uiApp = uiActionBar.getAncestorOfType(UIApplication.class);
      String accId = dataCache.getSelectedAccountId();
      
      if(Utils.isEmptyField(accId)) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.account-list-empty", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return ;
      }
      
      String username = MailUtils.getCurrentUser();
      Account delegatorAcc = dataCache.getDelegatedAccount(username, accId);
      if(MailUtils.isDelegatedAccount(delegatorAcc, username)){
        uiActionBar.showMessage(event);
        return;
      }

      UIPopupAction uiPopupAction = uiPortlet.findFirstComponentOfType(UIPopupAction.class);
      UIPopupActionContainer uiPopupContainer = uiPopupAction.createUIComponent(UIPopupActionContainer.class, null, "UIPopupActionFilterContainer");
      uiPopupAction.activate(uiPopupContainer, 600, 0, false);
      UIMessageFilter uiMessageFilter = uiPopupContainer.createUIComponent(UIMessageFilter.class, null, null);
      uiMessageFilter.init(accId);
      uiPopupContainer.addChild(uiMessageFilter);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
    }
  }

  static public class MailSettingsActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource();
      UIMailPortlet mailPortlet = uiActionBar.getParent();
      DataCache dataCache = mailPortlet.getDataCache();

      UIApplication uiApp = uiActionBar.getAncestorOfType(UIApplication.class);
      String accId = dataCache.getSelectedAccountId();
      String uid = MailUtils.getCurrentUser();
      
      if (Utils.isEmptyField(accId) || dataCache.getAccounts(uid).isEmpty()) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.account-list-empty", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      
      UIPopupAction uiPopupAction = mailPortlet.getChild(UIPopupAction.class);
      UIMailSettings uiMailSetting = uiPopupAction.activate(UIMailSettings.class, 750);
      uiMailSetting.init();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
    }
  }

  static public class AccountSettingsActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiForm = event.getSource();
      UIMailPortlet uiPortlet = uiForm.getAncestorOfType(UIMailPortlet.class);
      DataCache dataCache = uiPortlet.getDataCache();

      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class);
      String userId = MailUtils.getCurrentUser();
      String accountId = dataCache.getSelectedAccountId();
      if (dataCache.getAccountById(userId, accountId) != null) {
        UIPopupActionContainer uiPopupContainer = uiPopupAction.activate(UIPopupActionContainer.class, 800);
        uiPopupContainer.setId("UIAccountPopupSetting");
        UIAccountSetting uiAccountSetting = uiPopupContainer.createUIComponent(UIAccountSetting.class, null, null);
        uiPopupContainer.addChild(uiAccountSetting);
        uiAccountSetting.setSelectedAccountId(accountId);
        try {
          uiAccountSetting.fillField();
        } catch (NullPointerException e) {
          uiPortlet.findFirstComponentOfType(UIMessageList.class).setMessagePageList(null);
          uiPortlet.findFirstComponentOfType(UISelectAccount.class).refreshItems();
          event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
          UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class);
          uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.deleted_account", null, ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          return;
        }
      } else {
        UIPopupActionContainer uiAccContainer = uiPortlet.createUIComponent(UIPopupActionContainer.class, null, null);
        uiAccContainer.setId("UIAccountPopupCreation");
        uiAccContainer.addChild(UIAccountCreation.class, null, null);
        uiPopupAction.activate(uiAccContainer, 700, 0, true);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
    }
  }

  private void showMessage(Event<UIActionBar> event) {
    UIApplication uiApp = getAncestorOfType(UIApplication.class) ;
    uiApp.addMessage(new ApplicationMessage("UISelectAccount.msg.account-list-no-permission", null)) ;
    event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
    return ;
  }
}