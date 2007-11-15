/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui;

import java.util.ArrayList;
import java.util.List;

import javax.mail.AuthenticationFailedException;

import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.MailSetting;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.popup.UIComposeForm;
import org.exoplatform.mail.webui.popup.UIFeed;
import org.exoplatform.mail.webui.popup.UIMailSettings;
import org.exoplatform.mail.webui.popup.UIPopupAction;
import org.exoplatform.mail.webui.popup.UIPopupActionContainer;
import org.exoplatform.mail.webui.popup.UIQuickAddEvent;
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
        @EventConfig(listeners = UIActionBar.ContactActionListener.class),
        @EventConfig(listeners = UIActionBar.MailSettingsActionListener.class)
    }
)

public class UIActionBar extends UIContainer {

  public UIActionBar()throws Exception {}

  static  public class CheckMailActionListener extends EventListener<UIActionBar> {    
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ;
      System.out.println(" =========== > Check Mail ...");
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
        mailSvr.checkNewMessage(username, accId) ;
        uiMessageList.updateList();
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.findFirstComponentOfType(UINavigationContainer.class)); 
        event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class)); 
      } catch (AuthenticationFailedException afe) {
        afe.printStackTrace() ;
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.userName-password-incorrect", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      } catch (Exception e) {
        //e.printStackTrace() ;
        //uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.check-mail-error", null)) ;
        //event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      }
    }
  }

  static public class ComposeActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ; 
      System.out.println(" =========== > Compose Action");
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
      UIPopupActionContainer uiPopupContainer = uiPopupAction.activate(UIPopupActionContainer.class, 850) ;
      
      UIComposeForm uiComposeForm = uiPopupContainer.createUIComponent(UIComposeForm.class, null, null);
      uiPopupContainer.addChild(uiComposeForm) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }
  
  static public class AddressActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      //UIActionBar uiActionBar = event.getSource() ; 
      System.out.println(" =========== > AddAddressActionListener");
    }
  }
  
  static public class AddEventActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      System.out.println(" =========== > AddEventActionListener");
      UIActionBar uiActionBar = event.getSource() ; 
      UIMailPortlet uiPortlet = uiActionBar.getParent() ;
      CalendarService calenderSrv = uiPortlet.getApplicationComponent(CalendarService.class);
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
      UIQuickAddEvent uiQuickAddEvent = uiPopupAction.activate(UIQuickAddEvent.class, 600) ;
      uiQuickAddEvent.setEvent(true) ;        
      uiQuickAddEvent.init(calenderSrv.getCalendarSetting(MailUtils.getCurrentUser()), null, null) ;
      uiQuickAddEvent.update("0", null) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }
  static public class RssActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ; 
      System.out.println(" =========== > RssActionListener");
      UIMailPortlet uiPortlet = uiActionBar.getAncestorOfType(UIMailPortlet.class);
      UIPopupAction uiPopup = uiPortlet.findFirstComponentOfType(UIPopupAction.class);
      uiPopup.activate(UIFeed.class, 600);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup);
    }
  }
  static public class ChangeViewActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
//      UIActionBar uiActionBar = event.getSource() ; 
      System.out.println(" =========== > ChangeViewActionListener");
      String viewType = event.getRequestContext().getRequestParameter(OBJECTID) ;  
      System.out.println(" =========== > viewType " + viewType);
    }
  }
  static public class MailSettingsActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ; 
      System.out.println(" =========== > Mail Settings Action");
      UIMailPortlet mailPortlet = uiActionBar.getParent() ;
      UIPopupAction uiPopupAction = mailPortlet.getChild(UIPopupAction.class) ;
      UIMailSettings uiMailSetting = uiPopupAction.activate(UIMailSettings.class, 750) ;
      MailService mailSrv = uiActionBar.getApplicationComponent(MailService.class);
      String username = Util.getPortalRequestContext().getRemoteUser();
      MailSetting mailSetting = mailSrv.getMailSetting(username);
      List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
      for(Account acc : mailSrv.getAccounts(username)) {
        SelectItemOption<String> itemOption = new SelectItemOption<String>(acc.getUserDisplayName() + " &lt;" + acc.getEmailAddress() + 
            "&gt;", acc.getUserDisplayName() + "<" + acc.getEmailAddress() + ">");
        options.add(itemOption) ;
      }
      uiMailSetting.fillFormAccount(options);
      uiMailSetting.fillAllField(mailSetting);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }

  static public class ContactActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      //UIActionBar uiActionBar = event.getSource();   
    }
  }

}