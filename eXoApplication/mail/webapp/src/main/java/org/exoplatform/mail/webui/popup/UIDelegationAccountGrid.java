/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.commons.utils.ListAccessImpl;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.AccountDelegation;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormCheckBoxInput;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jan 28, 2011  
 */
@ComponentConfig(
                 template = "app:/templates/mail/webui/popup/UIDelegationAccountGrid.gtmpl",
                 events = {
                     @EventConfig(listeners = UIDelegationAccountGrid.RemoveActionListener.class, confirm = "UIDelegationAccountGrid.grid.msg.confirm-delete")
                 }
)
public class UIDelegationAccountGrid extends UIGrid {
  private static final Log log = ExoLogger.getExoLogger(UIDelegationAccountGrid.class);
  
  public static final String FULL_PRIVILEGE_FIELD = "isFull" ;  
  public static final String READONLY_PRIVILEGE_FIELD = "isReadOnly" ;
  String fields[] = {"accountName","delegatedUserName",UIDelegationAccountGrid.FULL_PRIVILEGE_FIELD};
  String actions[] = {"Remove"};
  public UIDelegationAccountGrid() throws Exception {
    configure("id",fields, actions);
    updateGrid();
  }

  private boolean isFull (String user, String perms) {
    return (user != null && perms != null) && Utils.SEND_RECIEVE.equalsIgnoreCase(perms) ;
  }

  public void updateGrid(){
    List<AccountDelegation> delegation = new ArrayList<AccountDelegation>();
    MailService mailSvr = getApplicationComponent(MailService.class) ;
    try {
      String currentuser = MailUtils.getCurrentUser();
      List<Account> acclist =  mailSvr.getAccounts(currentuser);
      for(Account a : acclist) {
        if(a.getPermissions() != null && a.getPermissions().keySet() != null)
          for (String receiver : a.getPermissions().keySet()) {
            AccountDelegation bean = new AccountDelegation(a.getId(), a.getLabel() +"(" + a.getEmailAddress() + ")",receiver, isFull(currentuser,a.getPermissions().get(receiver)));
            delegation.add(bean);
          }
      }
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("Exception in method updateGrid", e);
      }
    }
    LazyPageList<AccountDelegation> pageList = new LazyPageList<AccountDelegation>(
        new ListAccessImpl<AccountDelegation>(AccountDelegation.class, delegation), 10);
    getUIPageIterator().setPageList(pageList) ;
    addCheckboxToParent();
  }

  protected void addCheckboxToParent() {
    try {
      List<AccountDelegation>  data = getUIPageIterator().getCurrentPageData() ;
      UIDelegationInputSet deInputSet = getParent();
      deInputSet.removeChild(UIFormCheckBoxInput.class);
      deInputSet.addChild(new UIFormCheckBoxInput<Boolean>(UIMailSettings.FIELD_PRIVILEGE_FULL, UIMailSettings.FIELD_PRIVILEGE_FULL, null));
      for (AccountDelegation a : data) {
        UIFormCheckBoxInput<String> cb = new UIFormCheckBoxInput<String>(a.getId(), a.getId(),  null);
        cb.setChecked(a.isFull());
        cb.setOnChange("OnChange");
        deInputSet.addChild(cb);
      }
    } catch (Exception e) {

    }
  }

  protected void renderCheckbox (String id) throws Exception {
    UIDelegationInputSet deInputSet = getParent();
    if(deInputSet.getChildById(id) != null) deInputSet.renderChild(id);
  }

  static  public class RemoveActionListener extends EventListener<UIDelegationAccountGrid> {
    public void execute(Event<UIDelegationAccountGrid> event) throws Exception {
      UIDelegationAccountGrid uiDelegate = event.getSource();
      UIApplication uiApp = uiDelegate.getAncestorOfType(UIApplication.class) ;
      String currentuser = MailUtils.getCurrentUser();
      String delegateId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      try {
        MailService mService = MailUtils.getMailService() ;
        List<AccountDelegation> list = uiDelegate.getUIPageIterator().getCurrentPageData() ;
        for(AccountDelegation data : list) {
          if(data.getId().equalsIgnoreCase(delegateId)) {
            mService.removeDelegateAccount(currentuser, data.getDelegatedUserName() , data.getAccountId()) ;
            break;
          }
        }
        uiDelegate.updateGrid();
        event.getRequestContext().addUIComponentToUpdateByAjax(uiDelegate);
      }catch (Exception e) {
        uiApp.addMessage(new ApplicationMessage("UIDelegationAccountGrid.msg.remove-delegateion-fail", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      }
    }
  }   
}
