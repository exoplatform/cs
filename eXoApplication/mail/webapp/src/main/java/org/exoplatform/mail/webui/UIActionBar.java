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

import javax.mail.AuthenticationFailedException;

import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.SessionsUtils;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.MailSetting;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.popup.UIAddressBookForm;
import org.exoplatform.mail.webui.popup.UIComposeForm;
import org.exoplatform.mail.webui.popup.UIEventForm;
import org.exoplatform.mail.webui.popup.UIFeed;
import org.exoplatform.mail.webui.popup.UIMailSettings;
import org.exoplatform.mail.webui.popup.UIMessageFilter;
import org.exoplatform.mail.webui.popup.UIPopupAction;
import org.exoplatform.mail.webui.popup.UIPopupActionContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.model.SelectItemOption;
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
        @EventConfig(listeners = UIActionBar.RssActionListener.class),
        @EventConfig(listeners = UIActionBar.FilterActionListener.class),
        @EventConfig(listeners = UIActionBar.MailSettingsActionListener.class)
    }
)

public class UIActionBar extends UIContainer {

  public UIActionBar()throws Exception {}

  static  public class CheckMailActionListener extends EventListener<UIActionBar> {    
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ;
      UIMailPortlet uiPortlet = uiActionBar.getAncestorOfType(UIMailPortlet.class) ;
      UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType(UIMessageList.class);
      UINavigationContainer uiNavigation = uiPortlet.getChild(UINavigationContainer.class) ;
      UISelectAccount uiSelect = uiNavigation.getChild(UISelectAccount.class) ;
      MailService mailSvr = uiActionBar.getApplicationComponent(MailService.class) ;
      UIApplication uiApp = uiActionBar.getAncestorOfType(UIApplication.class) ;
      String accId = uiSelect.getSelectedValue() ;
      if(Utils.isEmptyField(accId)) {
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.account-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      String username =  uiPortlet.getCurrentUser() ;
      try {
        mailSvr.checkNewMessage(SessionsUtils.getSessionProvider(), username, accId) ;
        uiMessageList.init(accId);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.findFirstComponentOfType(UINavigationContainer.class)); 
        event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class)); 
      } catch (AuthenticationFailedException afe) {
        afe.printStackTrace() ;
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.userName-password-incorrect", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      } catch (Exception e) {
        //e.printStackTrace() ;
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.check-mail-error", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      }
    }
  }

  static public class ComposeActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
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
      UIPopupActionContainer uiPopupContainer = uiPopupAction.createUIComponent(UIPopupActionContainer.class, null, null) ;
      uiPopupAction.activate(uiPopupContainer, 850, 0, true);
      
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
      uiEventForm.initForm(calendarService.getCalendarSetting(SessionsUtils.getSessionProvider() ,MailUtils.getCurrentUser()), null) ;
      uiEventForm.update(CalendarUtils.PRIVATE_TYPE, null) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }
  static public class RssActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ; 
      UIMailPortlet uiPortlet = uiActionBar.getAncestorOfType(UIMailPortlet.class);
      UIPopupAction uiPopup = uiPortlet.findFirstComponentOfType(UIPopupAction.class);
      uiPopup.activate(UIFeed.class, 600);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup);
    }
  }
  
  static public class FilterActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ; 
      UIMailPortlet uiPortlet = uiActionBar.getAncestorOfType(UIMailPortlet.class);
      UIPopupAction uiPopupAction = uiPortlet.findFirstComponentOfType(UIPopupAction.class);
      UIPopupActionContainer uiPopupContainer = uiPopupAction.createUIComponent(UIPopupActionContainer.class, null, "UIPopupActionFilterContainer");
      uiPopupAction.activate(uiPopupContainer, 600, 0, false) ;
      UIMessageFilter uiMessageFilter = uiPopupContainer.createUIComponent(UIMessageFilter.class, null, null);
      uiPopupContainer.addChild(uiMessageFilter) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }
  
  static public class MailSettingsActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ; 
      UIMailPortlet mailPortlet = uiActionBar.getParent() ;
      UIPopupAction uiPopupAction = mailPortlet.getChild(UIPopupAction.class) ;
      UIMailSettings uiMailSetting = uiPopupAction.activate(UIMailSettings.class, 750) ;
      MailService mailSrv = uiActionBar.getApplicationComponent(MailService.class);
      String username = Util.getPortalRequestContext().getRemoteUser();
      MailSetting mailSetting = mailSrv.getMailSetting(SessionsUtils.getSessionProvider(), username);
      List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
      for(Account acc : mailSrv.getAccounts(SessionsUtils.getSessionProvider(), username)) {
        SelectItemOption<String> itemOption = new SelectItemOption<String>(acc.getUserDisplayName() + " &lt;" + acc.getEmailAddress() + 
            "&gt;", acc.getId());
        options.add(itemOption) ;
      }
      uiMailSetting.fillFormAccount(options);
      uiMailSetting.fillAllField(mailSetting);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }

}