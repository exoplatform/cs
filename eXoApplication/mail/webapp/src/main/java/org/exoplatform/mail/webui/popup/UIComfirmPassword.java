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

import org.exoplatform.cs.common.webui.UIPopupAction;
import org.exoplatform.cs.common.webui.UIPopupComponent;
import org.exoplatform.mail.DataCache;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SAS
 * Author : Phung Hai Nam
 *          phunghainam@gmail.com
 * Mar 24, 2008  
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UIComfirmPassword.CheckActionListener.class), 
      @EventConfig(listeners = UIComfirmPassword.CancelActionListener.class)
    }
)
public class UIComfirmPassword extends UIForm implements UIPopupComponent{
  public static final String FIELD_PASSWORD = "input-password";
  public static final String FIELD_SAVED_PASSWORD = "saved-message";
  
  public UIComfirmPassword() { 
    addUIFormInput(new UIFormStringInput(FIELD_PASSWORD, FIELD_PASSWORD, null).setType(UIFormStringInput.PASSWORD_TYPE));
    addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_SAVED_PASSWORD, FIELD_SAVED_PASSWORD, null));
  }
  
  public void activate() throws Exception { }

  public void deActivate() throws Exception { }
  
  static  public class CheckActionListener extends EventListener<UIComfirmPassword> {
    public void execute(Event<UIComfirmPassword> event) throws Exception {
      UIComfirmPassword uiForm = event.getSource();
      UIMailPortlet uiPortlet = uiForm.getAncestorOfType(UIMailPortlet.class);
      DataCache dataCache = uiPortlet.getDataCache();
      
      UIPopupAction uiPopup = uiPortlet.getChild(UIPopupAction.class);
      String newPw = uiForm.getUIStringInput(FIELD_PASSWORD).getValue() ;
      boolean isSavePw = uiForm.getUIFormCheckBoxInput(FIELD_SAVED_PASSWORD).isChecked() ;
      MailService mailSrv = uiPortlet.getApplicationComponent(MailService.class);
      String accountId = dataCache.getSelectedAccountId();
      String username = MailUtils.getDelegateFrom(accountId, dataCache);
      
      Account acc = dataCache.getAccountById(username, accountId) ;
      acc.setIsSavePassword(isSavePw) ;
      acc.setIncomingPassword(newPw) ;
      mailSrv.updateErrorAccount(username, acc) ;
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
      context.getJavascriptManager().importJavascript("eXo.mail.MailServiceHandler","/mail/javascript/");
      context.getJavascriptManager().addJavascript("eXo.mail.MailServiceHandler.checkMail(true) ;");
      context.getJavascriptManager().addJavascript("eXo.mail.MailServiceHandler.showStatusBox() ;");        
      uiPortlet.cancelAction() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup);
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIComfirmPassword> {
    public void execute(Event<UIComfirmPassword> event) throws Exception {
      UIComfirmPassword uiForm = event.getSource() ;
      uiForm.getAncestorOfType(UIPopupAction.class).deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIPopupAction.class)) ;
    }
  }
}
