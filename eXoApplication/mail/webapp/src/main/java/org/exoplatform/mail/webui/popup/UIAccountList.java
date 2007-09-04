/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIGrid;
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
  private static String[] BEAN_FIELD = {"name", "email", "server","protocol"} ;
  private static String[] BEAN_ACTION = {"Delete"} ;

  public UIAccountList() throws Exception {
    configure("id", BEAN_FIELD, BEAN_ACTION) ;
    updateGrid() ;
  }

  public void updateGrid() throws Exception {
    List<AccountData> accounts = new ArrayList<AccountData>() ;
    String userId = Util.getPortalRequestContext().getRemoteUser() ;
    MailService mailSvr = getApplicationComponent(MailService.class) ;
    for(Account acc : mailSvr.getAccounts(userId)) {
      accounts.add(new AccountData(acc.getId(), acc.getUserDisplayName(), acc.getEmailAddress(), 
          acc.getServerProperties().get(Utils.SVR_POP_HOST), acc.getProtocol())) ;
    }

    ObjectPageList objPageList = new ObjectPageList(accounts, 10) ;
    getUIPageIterator().setPageList(objPageList) ; 
  }

  public void activate() throws Exception {
    // TODO Auto-generated method stub

  }
  public void deActivate() throws Exception {
    // TODO Auto-generated method stub

  }
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

  static  public class DeleteActionListener extends EventListener<UIAccountList> {
    public void execute(Event<UIAccountList> event) throws Exception {
      System.out.println("=====>>> DeleteActionListener");
      UIAccountList uiAccountList = event.getSource() ;
      String accId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      System.out.println("accId " + accId);
      UIApplication uiApp = uiAccountList.getAncestorOfType(UIApplication.class) ;
      MailService mailSvr = uiAccountList.getApplicationComponent(MailService.class) ;
      String username = event.getRequestContext().getRemoteUser() ;
      Account account = mailSvr.getAccountById(username, accId) ;
      try {
        mailSvr.removeAccount(username, account) ;
      } catch (Exception e) {
        uiApp.addMessage(new ApplicationMessage("UIAccountList.msg.remove-accout-error", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        e.printStackTrace() ;
      }
    }
  }
  static  public class CloseActionListener extends EventListener<UIAccountList> {
    public void execute(Event<UIAccountList> event) throws Exception {
      UIPopupAction uiPopup = event.getSource().getAncestorOfType(UIPopupAction.class);
      uiPopup.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
    }
  }
}
