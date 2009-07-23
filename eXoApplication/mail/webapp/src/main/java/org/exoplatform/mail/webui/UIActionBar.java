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

import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.popup.UIAccountCreation;
import org.exoplatform.mail.webui.popup.UIAccountSetting;
import org.exoplatform.mail.webui.popup.UIAddressBookForm;
import org.exoplatform.mail.webui.popup.UIComposeForm;
import org.exoplatform.mail.webui.popup.UIEventForm;
import org.exoplatform.mail.webui.popup.UIMailSettings;
import org.exoplatform.mail.webui.popup.UIMessageFilter;
import org.exoplatform.mail.webui.popup.UIPopupAction;
import org.exoplatform.mail.webui.popup.UIPopupActionContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
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

  public UIActionBar()throws Exception {}

  static  public class CheckMailActionListener extends EventListener<UIActionBar> {    
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ;
      UIMailPortlet uiPortlet = uiActionBar.getAncestorOfType(UIMailPortlet.class) ;
      UIFolderContainer uiFolderContainer = uiPortlet.findFirstComponentOfType(UIFolderContainer.class);
      String folderId = uiFolderContainer.getSelectedFolder();
      UIMessageList uiMsgList = uiPortlet.findFirstComponentOfType(UIMessageList.class);
      UIApplication uiApp = uiActionBar.getAncestorOfType(UIApplication.class) ;
      String accId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue() ;
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
      if(Utils.isEmptyField(accId)) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.account-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } else {
        if (MailUtils.isFieldEmpty(folderId)) {
          context.getJavascriptManager().addJavascript("eXo.mail.MailServiceHandler.checkMail(true) ;");
        } else {
          context.getJavascriptManager().addJavascript("eXo.mail.MailServiceHandler.checkMail(true, '" + folderId + "') ;");
        }
        context.getJavascriptManager().addJavascript("eXo.mail.MailServiceHandler.showStatusBox() ;");        
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMsgList) ;
    }
  }

  static public class ComposeActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      
      //System.out.println("\n\n hi :" + Util.getUIPortalApplication().getOrientation().isRT() + "\n\n");
      UIActionBar uiActionBar = event.getSource() ; 
      UIMailPortlet uiPortlet = uiActionBar.getParent() ;
      UIApplication uiApp = uiActionBar.getAncestorOfType(UIApplication.class) ;
      UINavigationContainer uiNavigation = uiPortlet.getChild(UINavigationContainer.class) ;
      UISelectAccount uiSelect = uiNavigation.getChild(UISelectAccount.class) ;
      String accId = uiSelect.getSelectedValue() ;      
      if(Utils.isEmptyField(accId)) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.account-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
      UIPopupActionContainer uiPopupContainer = uiPopupAction.createUIComponent(UIPopupActionContainer.class, null, "UIPopupActionComposeContainer") ;
      uiPopupAction.activate(uiPopupContainer, MailUtils.MAX_POPUP_WIDTH, 0, true);
      
      UIComposeForm uiComposeForm = uiPopupContainer.createUIComponent(UIComposeForm.class, null, null);
      uiComposeForm.init(accId, null, 0);
      uiPopupContainer.addChild(uiComposeForm) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
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
      UIActionBar uiActionBar = event.getSource() ; 
      UIMailPortlet uiPortlet = uiActionBar.getAncestorOfType(UIMailPortlet.class);
      UIApplication uiApp = uiActionBar.getAncestorOfType(UIApplication.class) ;
      String accId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue() ;
      if(Utils.isEmptyField(accId)) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.account-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      UIPopupAction uiPopupAction = uiPortlet.findFirstComponentOfType(UIPopupAction.class);
      UIPopupActionContainer uiPopupContainer = uiPopupAction.createUIComponent(UIPopupActionContainer.class, null, "UIPopupActionFilterContainer");
      uiPopupAction.activate(uiPopupContainer, 600, 0, false) ;
      UIMessageFilter uiMessageFilter = uiPopupContainer.createUIComponent(UIMessageFilter.class, null, null);
      uiMessageFilter.init(accId) ;
      uiPopupContainer.addChild(uiMessageFilter) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }
  
  static public class MailSettingsActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ; 
      UIMailPortlet mailPortlet = uiActionBar.getParent() ;
      UIApplication uiApp = uiActionBar.getAncestorOfType(UIApplication.class) ;
      String accId = mailPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue() ;
      if(Utils.isEmptyField(accId)) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.account-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      UIPopupAction uiPopupAction = mailPortlet.getChild(UIPopupAction.class) ;
      UIMailSettings uiMailSetting = uiPopupAction.activate(UIMailSettings.class, 750) ;
      uiMailSetting.init() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }

  static public class AccountSettingsActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiForm = event.getSource() ;
      UIMailPortlet uiPortlet = uiForm.getAncestorOfType(UIMailPortlet.class) ;
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
      UISelectAccount uiSelectAcc = uiPortlet.findFirstComponentOfType(UISelectAccount.class);
      if(Utils.isEmptyField(uiSelectAcc.getSelectedValue())) {
        UIPopupActionContainer uiAccContainer = uiPortlet.createUIComponent(UIPopupActionContainer.class, null, null) ;
        uiAccContainer.setId("UIAccountPopupCreation");
        uiAccContainer.addChild(UIAccountCreation.class, null, null) ;
        uiPopupAction.activate(uiAccContainer, 700, 0, true) ;
      } else {
        UIPopupActionContainer uiPopupContainer = uiPopupAction.activate(UIPopupActionContainer.class, 800) ;
        uiPopupContainer.setId("UIAccountPopupSetting");
        UIAccountSetting uiAccountSetting = uiPopupContainer.createUIComponent(UIAccountSetting.class, null, null);
        uiPopupContainer.addChild(uiAccountSetting) ; 
        uiAccountSetting.setSelectedAccountId(uiSelectAcc.getSelectedValue());
        try {
          uiAccountSetting.fillField();
        } catch (NullPointerException e) {
          uiPortlet.findFirstComponentOfType(UIMessageList.class).setMessagePageList(null) ;
          uiPortlet.findFirstComponentOfType(UISelectAccount.class).refreshItems();
          event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
          UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
          uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.deleted_account", null, ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }
}