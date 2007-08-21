/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui;

import javax.mail.AuthenticationFailedException;

import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.webui.popup.UIAccountCreation;
import org.exoplatform.mail.webui.popup.UIAccountCreationContainer;
import org.exoplatform.mail.webui.popup.UIComposeForm;
import org.exoplatform.mail.webui.popup.UIMailSettings;
import org.exoplatform.mail.webui.popup.UIPopupAction;
import org.exoplatform.web.application.ApplicationMessage;
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
      System.out.println(" =========== > Check Mail");
      UIMailPortlet uiPortlet = uiActionBar.getAncestorOfType(UIMailPortlet.class) ;
      UINavigationContainer uiNavigation = uiPortlet.getChild(UINavigationContainer.class) ;
      UISelectAccount uiSelect = uiNavigation.getChild(UISelectAccount.class) ;
      MailService mailSvr = uiActionBar.getApplicationComponent(MailService.class) ;
      String username =  uiPortlet.getCurrentUser() ;
      String accId = uiSelect.getSelectedValue() ;
      Account account = mailSvr.getAccountById(username, accId) ;
      System.out.println("\n\n account " + account.getId());
      UIDefaultFolders uiDefaultFolders = uiNavigation.getChild(UIFolderContainer.class).getChild(UIDefaultFolders.class) ;
      UIApplication uiApp = uiActionBar.getAncestorOfType(UIApplication.class) ;
      try {
        mailSvr.checkNewMessage(username, account) ;
        //event.getRequestContext().addUIComponentToUpdateByAjax(uiDefaultFolders) ;
      } catch (AuthenticationFailedException afe) {
        afe.printStackTrace() ;
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.userName-password-incorrect", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      } catch (Exception e) {
        e.printStackTrace() ;
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.check-mail-error", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      }
    }
  }

  static public class ComposeActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ; 
      System.out.println(" =========== > Compose Action");
      UIMailPortlet mailPortlet = uiActionBar.getParent() ;
      UIPopupAction uiPopupAction = mailPortlet.getChild(UIPopupAction.class) ;
      uiPopupAction.activate(UIComposeForm.class, 1000) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }

  static public class MailSettingsActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ; 
      System.out.println(" =========== > Mail Settings Action");
      UIMailPortlet mailPortlet = uiActionBar.getParent() ;
      UIPopupAction uiPopupAction = mailPortlet.getChild(UIPopupAction.class) ;
      uiPopupAction.activate(UIMailSettings.class, 600) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }

  static public class ContactActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ;   
    }
  }

  static public class RssActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ;      
    }
  }
}