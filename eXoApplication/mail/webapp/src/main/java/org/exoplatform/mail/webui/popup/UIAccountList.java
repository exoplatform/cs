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
package org.exoplatform.mail.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.commons.utils.ListAccessImpl;
import org.exoplatform.cs.common.webui.UIPopupAction;
import org.exoplatform.mail.DataCache;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.MailSetting;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.UIFolderContainer;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.mail.webui.UIMessageList;
import org.exoplatform.mail.webui.UIMessagePreview;
import org.exoplatform.mail.webui.UISelectAccount;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Sep 4, 2007  
 */
@ComponentConfig(
    template = "app:/templates/mail/webui/UIGridWithButton.gtmpl",
    events = {
        @EventConfig(listeners = UIAccountList.DeleteActionListener.class, confirm = "UIAccountList.msg.confirm-delete"),
        @EventConfig(listeners = UIAccountList.CloseActionListener.class)
    }
)
public class UIAccountList extends UIGrid  implements UIPopupComponent{
  private static final Log log = ExoLogger.getExoLogger(UIAccountList.class);
  
  private static String[] BEAN_FIELD = {"name", "email", "server","protocol"} ;
  private static String[] BEAN_ACTION = {"Delete"} ;

  public UIAccountList() throws Exception {
    configure("id", BEAN_FIELD, BEAN_ACTION) ;
    updateGrid() ;
  }

  public void updateGrid() throws Exception {
    DataCache dataCache = (DataCache) WebuiRequestContext.getCurrentInstance().getAttribute(DataCache.class);
    
    List<AccountData> accounts = new ArrayList<AccountData>() ;
    String userId = Util.getPortalRequestContext().getRemoteUser() ;
    for(Account acc : dataCache.getAccounts(userId)) {
      accounts.add(new AccountData(acc.getId(), acc.getLabel(), acc.getEmailAddress(), 
          acc.getServerProperties().get(Utils.SVR_INCOMING_HOST), acc.getProtocol())) ;
    }

    //ObjectPageList objPageList = new ObjectPageList(accounts, 10) ;
    LazyPageList<AccountData> pageList = new LazyPageList<AccountData>(new ListAccessImpl<AccountData>(AccountData.class, accounts), 10);
    getUIPageIterator().setPageList(pageList) ; 
  }

  public void activate() throws Exception { }
  
  public void deActivate() throws Exception { }
  public String[] getButtons(){
    return new String[] {"Close"} ;
  }
  public class AccountData {
    String id ;
    String name ;
    String email ;
    String server ;
    String protocol ;

    public AccountData(String iId, String iName, String iEmail, String iServer, String iProtocol){
      id = iId ;
      name = iName ;
      email = iEmail ;
      server = iServer ;
      protocol = iProtocol ;
    }
    public String getId() {return id ;} ;
    public String getName() {return name ;}
    public String getEmail () {return email ;}
    public String getServer() {return server ;}
    public String getProtocol() {return protocol ;}
  }

  static public class DeleteActionListener extends EventListener<UIAccountList> {
    public void execute(Event<UIAccountList> event) throws Exception {
      UIAccountList uiAccountList = event.getSource();
      UIMailPortlet uiPortlet = uiAccountList.getAncestorOfType(UIMailPortlet.class);
      DataCache dataCache = uiPortlet.getDataCache();

      UISelectAccount uiSelectAccount = uiPortlet.findFirstComponentOfType(UISelectAccount.class);
      UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType(UIMessageList.class);
      UIMessagePreview uiMessagePreview = uiPortlet.findFirstComponentOfType(UIMessagePreview.class);
      String currAccountId = dataCache.getSelectedAccountId();
      String accId = event.getRequestContext().getRequestParameter(OBJECTID);
      MailService mailSvr = uiAccountList.getApplicationComponent(MailService.class);
      String username = event.getRequestContext().getRemoteUser();

      try {
        mailSvr.removeAccount(username, accId);
        dataCache.clearAccountCache();
        
        uiSelectAccount.refreshItems();
        uiAccountList.updateGrid();
        MailSetting mailSetting = mailSvr.getMailSetting(username);
        if (currAccountId.equals(accId)) {
          List<Account> accounts = dataCache.getAccounts(username);
          if (accounts.size() == 0) {
            uiSelectAccount.setSelectedValue(null);
            mailSetting.setDefaultAccount(null);
            uiMessageList.init("");
          } else {
            String selectedAcc = accounts.get(0).getId();
            uiSelectAccount.setSelectedValue(selectedAcc);
            uiPortlet.findFirstComponentOfType(UIFolderContainer.class).setSelectedFolder(Utils.generateFID(selectedAcc, Utils.FD_INBOX, false));
            mailSetting.setDefaultAccount(selectedAcc);
            uiMessageList.setMessageFilter(null);
            uiMessageList.init(selectedAcc);
          }
          mailSvr.saveMailSetting(username, mailSetting);
          uiMessagePreview.setMessage(null);
          event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
        } else {
          uiSelectAccount.setSelectedValue(currAccountId);
          event.getRequestContext().addUIComponentToUpdateByAjax(uiAccountList.getAncestorOfType(UIPopupAction.class));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiSelectAccount);
        }
      } catch (Exception e) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIAccountList.msg.remove-accout-error", null));
        if (log.isDebugEnabled()) {
          log.debug("Exception in method execute of class DeleteActionListener", e);
        }
      }
    }
  }
  
  static  public class CloseActionListener extends EventListener<UIAccountList> {
    public void execute(Event<UIAccountList> event) throws Exception {
      UIPopupAction uiPopup = event.getSource().getAncestorOfType(UIPopupAction.class);
      uiPopup.cancelPopupAction();
    }
  }
}
