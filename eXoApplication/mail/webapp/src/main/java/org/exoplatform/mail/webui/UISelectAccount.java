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

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.cs.common.webui.UIPopupAction;
import org.exoplatform.cs.common.webui.UIPopupActionContainer;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.MessageFilter;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.popup.UIAccountCreation;
import org.exoplatform.mail.webui.popup.UIAccountList;
import org.exoplatform.mail.webui.popup.UIAccountSetting;
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
  static public String accountRefreshed = null ;

  public UISelectAccount() throws Exception {
    UIFormSelectBox uiSelect = new UIFormSelectBox(FIELD_SELECT, FIELD_SELECT, getValues()) ;
    uiSelect.setOnChange("SelectAccount") ;
    addChild(uiSelect) ; 
  }

  public String getLabel(String id) {
    try {
      return super.getLabel(id) ; 
    } catch(Exception e) {
      return id ;
    }
  }

  private List<SelectItemOption<String>> getValues() throws Exception {
    MailService mailSvr = (MailService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(MailService.class) ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    List<Account> accountList = new ArrayList<Account>(); 
    accountList =  mailSvr.getAccounts(username) ;
    accountList.addAll(mailSvr.getDelegatedAccounts(username)) ;
    String defaultAcc = mailSvr.getMailSetting(username).getDefaultAccount();
    List<SelectItemOption<String>>  options = new ArrayList<SelectItemOption<String>>() ;
    for(Account acc : accountList) {
      SelectItemOption<String> option = new SelectItemOption<String>(acc.getLabel(), acc.getId());
      if (defaultAcc != null && acc.getId().equals(defaultAcc)) {
        option = new SelectItemOption<String>(acc.getLabel() + " (" + getLabel("default") + ")", acc.getId());
        option.setSelected(true);
        accountRefreshed = acc.getId();
      }
      if(MailUtils.isDelegatedAccount(acc, username)) {
        option = new SelectItemOption<String>(acc.getLabel() + " (" + getLabel("delegated") + ")", acc.getId());
        accountRefreshed = acc.getId();
      }
      options.add(option) ;
    }
    return options ;
  }

  public void updateAccount() throws Exception {
    getUIFormSelectBox(FIELD_SELECT).setOptions(getValues());
  }

  public String getSelectedValue() {
    String id = getChild(UIFormSelectBox.class).getValue() ;
    try {
      String username = MailUtils.getCurrentUser();
      MailService mailSvr = MailUtils.getMailService();
      if (!MailUtils.isFieldEmpty(id) && mailSvr.getAccountById(username, id) != null) {
        return id;        
      } else if(MailUtils.isDelegated(id)){
        return id; 
      } else if(!MailUtils.isFieldEmpty(accountRefreshed)){
          if((mailSvr.getAccountById(username, accountRefreshed) != null) || mailSvr.getDelegatedAccount(username, accountRefreshed) != null) 
            return accountRefreshed;
      }
    } catch (Exception e) {
      // e.printStackTrace();
      return accountRefreshed;
    } 
    return null;
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
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
    }
  }

  static  public class EditAccountActionListener extends EventListener<UISelectAccount> {
    public void execute(Event<UISelectAccount> event) throws Exception {
      UISelectAccount uiForm = event.getSource() ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      String username = MailUtils.getCurrentUser();
      MailService mService = uiApp.getApplicationComponent(MailService.class);
      if(Utils.isEmptyField(uiForm.getSelectedValue()) || (mService.getAccounts(username).isEmpty() && mService.getDelegatedAccounts(username).isEmpty()) ){
        uiApp.addMessage(new ApplicationMessage("UISelectAccount.msg.account-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } 
      String accId = uiForm.getSelectedValue();
      if(mService.getAccounts(username).isEmpty() && !mService.getDelegatedAccounts(username).isEmpty()){
        uiForm.showMessage(event);
        return ;
      }
      if(MailUtils.isDelegated(accId) && !mService.getAccounts(username).isEmpty()) {
        accId = mService.getAccounts(username).get(0).getId();
      }
      UIMailPortlet uiPortlet = uiForm.getAncestorOfType(UIMailPortlet.class) ;
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
      UIPopupActionContainer uiPopupContainer = uiPopupAction.activate(UIPopupActionContainer.class, 800) ;
      uiPopupContainer.setId("UIAccountPopupSetting");
      UIAccountSetting uiAccountSetting = uiPopupContainer.createUIComponent(UIAccountSetting.class, null, null);
      uiPopupContainer.addChild(uiAccountSetting) ; 
      uiAccountSetting.setSelectedAccountId(accId);
      uiAccountSetting.fillField();     
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }

  static  public class DeleteAccountActionListener extends EventListener<UISelectAccount> {
    public void execute(Event<UISelectAccount> event) throws Exception {
      UISelectAccount uiForm = event.getSource() ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      MailService mService = uiApp.getApplicationComponent(MailService.class);
      String username = MailUtils.getCurrentUser();
      if(Utils.isEmptyField(uiForm.getSelectedValue())) {
        uiApp.addMessage(new ApplicationMessage("UISelectAccount.msg.account-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } else if(mService.getAccounts(username).isEmpty() && !mService.getDelegatedAccounts(username).isEmpty()){
        uiForm.showMessage(event);
        return ;
      } else {
        UIMailPortlet uiPortlet = uiForm.getAncestorOfType(UIMailPortlet.class) ;
        UIPopupAction uiPopup = uiPortlet.getChild(UIPopupAction.class) ;
        UIPopupActionContainer uiAccContainer =  uiPopup.activate(UIPopupActionContainer.class, 700) ;
        uiAccContainer.setId("UIPopupDeleteAccountContainer") ;
        uiAccContainer.addChild(UIAccountList.class, null, null) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
      }
    }
  }

  static  public class SelectAccountActionListener extends EventListener<UISelectAccount> {
    public void execute(Event<UISelectAccount> event) throws Exception {
      UISelectAccount uiSelectAcc = event.getSource() ;
      UIMailPortlet uiPortlet = uiSelectAcc.getAncestorOfType(UIMailPortlet.class);
      try {
        String accId = uiSelectAcc.getSelectedValue() ;
        UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType(UIMessageList.class) ;
        UIMessagePreview uiMessagePreview = uiPortlet.findFirstComponentOfType(UIMessagePreview.class) ;
        UIFolderContainer uiFolder = uiPortlet.findFirstComponentOfType(UIFolderContainer.class);
        MailService mailSvr = uiSelectAcc.getApplicationComponent(MailService.class) ;
        String username = uiPortlet.getCurrentUser();
        if(!MailUtils.isDelegated(accId)){
          if (mailSvr.getAccountById(username, accId) == null) {
            List<Account> accs = mailSvr.getAccounts(username);
            accs.addAll(mailSvr.getDelegatedAccounts(username));
            if (accs != null && accs.size() > 0) {
              accId = accs.get(0).getId();
              uiSelectAcc.refreshItems();
            } else {
              uiSelectAcc.setSelectedValue(null);
              uiSelectAcc.refreshItems();
              uiMessageList.init("");
              uiMessagePreview.setMessage(null);
              event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
              return; 
            }
          }
        }  
        uiSelectAcc.setSelectedValue(accId);
        uiFolder.setSelectedFolder(Utils.generateFID(accId, Utils.FD_INBOX, false));
        MessageFilter filter = new MessageFilter("Folder");
        filter.setAccountId(accId);
        filter.setFolder(new String[] {Utils.generateFID(accId, Utils.FD_INBOX, false)}) ;
        uiMessageList.setMessageFilter(filter);
        uiMessageList.init(accId);
        uiPortlet.findFirstComponentOfType(UIMessagePreview.class).setMessage(null);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
      } catch(Exception e) { 
        e.printStackTrace();
        return;
      }
    }
  } 
  private void showMessage(Event event) {
    UIApplication uiApp = getAncestorOfType(UIApplication.class) ;
    uiApp.addMessage(new ApplicationMessage("UISelectAccount.msg.account-list-no-permission", null)) ;
    event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
  }
}
