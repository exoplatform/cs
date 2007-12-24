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

import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.SessionsUtils;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.popup.UIAccountCreation;
import org.exoplatform.mail.webui.popup.UIAccountList;
import org.exoplatform.mail.webui.popup.UIAccountSetting;
import org.exoplatform.mail.webui.popup.UIPopupActionContainer;
import org.exoplatform.mail.webui.popup.UIPopupAction;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/mail/webui/UISelectAccount.gtmpl",
    events = {
      @EventConfig( listeners = UISelectAccount.AddAccountActionListener.class),
      @EventConfig( listeners = UISelectAccount.EditAccountActionListener.class),
      @EventConfig( listeners = UISelectAccount.DeleteAccountActionListener.class),
      @EventConfig( listeners = UISelectAccount.SelectAccountActionListener.class)
    }
) 
public class UISelectAccount extends UIForm {
  final static public String FIELD_SELECT = "accSelect" ;
  
  public UISelectAccount() throws Exception {
    UIFormSelectBox uiSelect = new UIFormSelectBox(FIELD_SELECT, FIELD_SELECT, getValues()) ;
    uiSelect.setOnChange("SelectAccount") ;
    addChild(uiSelect) ; 
  }

  private List<Account> getAccounts() throws Exception {
    MailService mailSvr = getApplicationComponent(MailService.class) ;
    String currentUser = Util.getPortalRequestContext().getRemoteUser() ;
    return mailSvr.getAccounts(SessionsUtils.getSessionProvider(), currentUser) ;
  }
  
  private List<SelectItemOption<String>> getValues() throws Exception {
    List<SelectItemOption<String>>  options = new ArrayList<SelectItemOption<String>>() ;
    for(Account acc : getAccounts()) {
      options.add(new SelectItemOption<String>(acc.getLabel(), acc.getId())) ;
    }
    //TODO : get default account
    //if (getAccounts().size() > 0) { MailUtils.setAccountId(getAccounts().get(0).getId()); }
    return options ;
  }
  
  public void updateAccount() throws Exception {
    getUIFormSelectBox(FIELD_SELECT).setOptions(getValues());
  }
  
  public String getSelectedValue() {
    return getChild(UIFormSelectBox.class).getValue() ;
  }
  
  public void setSelectedValue(String value) {
    getChild(UIFormSelectBox.class).setValue(value) ;
  }
  
  public void refreshItems() throws Exception {
    getChild(UIFormSelectBox.class).getOptions().clear() ;
    getChild(UIFormSelectBox.class).setOptions(getValues()) ;
  }

  @Override
  public String[] getActions() {
    return new String[] {"AddAccount", "EditAccount", "DeleteAccount"} ;
  }

  static  public class AddAccountActionListener extends EventListener<UISelectAccount> {
    public void execute(Event<UISelectAccount> event) throws Exception {
      UISelectAccount uiForm = event.getSource() ;
      UIMailPortlet uiPortlet = uiForm.getAncestorOfType(UIMailPortlet.class) ;
      UIPopupAction uiPopup = uiPortlet.getChild(UIPopupAction.class) ;
      UIPopupActionContainer uiAccContainer = uiPortlet.createUIComponent(UIPopupActionContainer.class, null, null) ;
      uiAccContainer.setId("UIAccountPopupCreation");
      uiAccContainer.addChild(UIAccountCreation.class, null, null) ;
      uiPopup.activate(uiAccContainer, 700, 0, true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
    }
  }
  
  static  public class EditAccountActionListener extends EventListener<UISelectAccount> {
    public void execute(Event<UISelectAccount> event) throws Exception {
      UISelectAccount uiForm = event.getSource() ;
      UIMailPortlet uiPortlet = uiForm.getAncestorOfType(UIMailPortlet.class) ;
      
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
      UIPopupActionContainer uiPopupContainer = uiPopupAction.activate(UIPopupActionContainer.class, 730) ;
      
      uiPopupContainer.setId("UIAccountPopupSetting");
      UIAccountSetting uiAccountSetting = uiPopupContainer.createUIComponent(UIAccountSetting.class, null, null);
       
      uiAccountSetting.setSelectedAccountId(uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue());
      uiAccountSetting.fillAllField();
      
      uiPopupContainer.addChild(uiAccountSetting) ;      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }
  
  static  public class DeleteAccountActionListener extends EventListener<UISelectAccount> {
    public void execute(Event<UISelectAccount> event) throws Exception {
      UISelectAccount uiForm = event.getSource() ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      if(Utils.isEmptyField(uiForm.getSelectedValue())) {
        uiApp.addMessage(new ApplicationMessage("UISelectAccount.msg.account-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } else {
        UIMailPortlet uiPortlet = uiForm.getAncestorOfType(UIMailPortlet.class) ;
        UIPopupAction uiPopup = uiPortlet.getChild(UIPopupAction.class) ;
        UIPopupActionContainer uiAccContainer = uiPortlet.createUIComponent(UIPopupActionContainer.class, null, null) ;
        uiAccContainer.setId("UIPopupDeleteAccountContainer") ;
        uiAccContainer.addChild(UIAccountList.class, null, null) ;
        uiPopup.activate(uiAccContainer, 700, 500, true) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
      }
    }
  }
  
  static  public class SelectAccountActionListener extends EventListener<UISelectAccount> {
    public void execute(Event<UISelectAccount> event) throws Exception {
      UISelectAccount uiSelectAcc = event.getSource() ;
      UIMailPortlet uiPortlet = uiSelectAcc.getAncestorOfType(UIMailPortlet.class);
      String accId = uiSelectAcc.getSelectedValue() ;
      MailService mailSrv = MailUtils.getMailService();
      String username = MailUtils.getCurrentUser();
      mailSrv.updateCurrentAccount(SessionsUtils.getSessionProvider(), username, accId);
      uiPortlet.findFirstComponentOfType(UIMessageList.class).init(accId);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
    }
  }  
}
