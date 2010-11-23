/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.mail.webui.popup;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.webui.UIFolderContainer;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.mail.webui.UIMessageList;
import org.exoplatform.mail.webui.UISelectAccount;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;

import com.sun.mail.smtp.SMTPSendFailedException;

/**
 * Created by The eXo Platform SAS
 * Author : Phung Hai Nam
 *          phunghainam@gmail.com
 * Mar 24, 2008  
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/mail/webui/popup/UIEnterPasswordDialog.gtmpl",
    events = {
      @EventConfig(listeners = UIEnterPasswordDialog.OkActionListener.class), 
      @EventConfig(listeners = UIEnterPasswordDialog.CancelActionListener.class, phase = Phase.DECODE)
    }
)
public class UIEnterPasswordDialog extends UIForm implements UIPopupComponent{
  public static final String FIELD_PASSWORD = "input-password";
  public static final String FIELD_SAVED_PASSWORD = "saved-message";
  
  private boolean showWarning_;
  private Message sendMessage_ ;
  private String accId_;
  
  public UIEnterPasswordDialog() throws Exception { 
    UIFormStringInput uiPassword = new UIFormStringInput(FIELD_PASSWORD, FIELD_PASSWORD, null);
    uiPassword.addValidator(MandatoryValidator.class);
    uiPassword.setType(UIFormStringInput.PASSWORD_TYPE);
    addUIFormInput(uiPassword);
    addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_SAVED_PASSWORD, FIELD_SAVED_PASSWORD, null));
  }
  
  public void activate() throws Exception { }

  public void deActivate() throws Exception { }
  
  public Message getSendMessage() {
    return sendMessage_;
  }
  
  public void setSendMessage(Message sendMessage) {
    sendMessage_ = sendMessage;
  }
  
  public void setAccountId(String accId) { accId_ = accId; }
  public String getAccountId() { return accId_; }
  
  public Account getAccount() throws Exception {
    MailService mailSrv = getApplicationComponent(MailService.class);
    String username = MailUtils.getCurrentUser();
    return mailSrv.getAccountById(username, getAccountId());
  }
  
  public boolean showWarning() { return showWarning_; }
  public void setShowWarning(boolean b) { showWarning_ = b; }
  
  static  public class OkActionListener extends EventListener<UIEnterPasswordDialog> {
    public void execute(Event<UIEnterPasswordDialog> event) throws Exception {
      UIEnterPasswordDialog uiForm = event.getSource() ;
      UIMailPortlet uiPortlet = uiForm.getAncestorOfType(UIMailPortlet.class) ;
      UIPopupAction uiPopup = uiPortlet.getChild(UIPopupAction.class);
      UIComposeForm composeForm = uiPortlet.findFirstComponentOfType(UIComposeForm.class);
      String newPw = uiForm.getUIStringInput(FIELD_PASSWORD).getValue() ;
      boolean isSavePw = uiForm.getUIFormCheckBoxInput(FIELD_SAVED_PASSWORD).isChecked() ;
      MailService mailSrv = uiPortlet.getApplicationComponent(MailService.class);
      String username = uiPortlet.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      Account acc = mailSrv.getAccountById(username, accountId) ;
      acc.setIsSavePassword(isSavePw) ;
      acc.setIncomingPassword(newPw) ;
      
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      
      try {
        mailSrv.sendMessage(username, acc, uiForm.getSendMessage());
      } catch (AddressException e) {
        uiApp.addMessage(new ApplicationMessage("UIEnterPasswordDialog.msg.there-was-an-error-parsing-the-addresses-sending-failed", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      } catch (AuthenticationFailedException e) {
        uiForm.setShowWarning(true);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
        return;
      } catch (SMTPSendFailedException e) {
        uiApp.addMessage(new ApplicationMessage("UIEnterPasswordDialog.msg.sorry-there-was-an-error-sending-the-message-sending-failed", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      } catch (MessagingException e) {
        uiApp.addMessage(new ApplicationMessage("UIEnterPasswordDialog.msg.there-was-an-unexpected-error-sending-falied", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      
      try {
        if(!composeForm.saveToSentFolder(username, acc, uiForm.getSendMessage())){
          uiApp.addMessage(new ApplicationMessage("UIMoveMessageForm.msg.create-massage-not-successful",
                                                  null, ApplicationMessage.INFO)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        }
        UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType(UIMessageList.class) ;
        uiMessageList.updateList();
        event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.findFirstComponentOfType(UIFolderContainer.class)) ;
      } catch (Exception e) {
        uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.save-sent-error", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        uiForm.getAncestorOfType(UIPopupAction.class).deActivate() ;
      }
      
      if (isSavePw) {
        mailSrv.updateAccount(username, acc) ;
      }
      
      uiPopup.deActivate();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIEnterPasswordDialog> {
    public void execute(Event<UIEnterPasswordDialog> event) throws Exception {
      UIEnterPasswordDialog uiForm = event.getSource() ;
      UIPopupAction uiPopupAction = uiForm.getAncestorOfType(UIPopupAction.class) ; 
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }

}
