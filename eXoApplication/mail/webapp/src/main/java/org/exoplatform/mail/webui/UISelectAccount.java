/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.webui.popup.UIAccountCreation;
import org.exoplatform.mail.webui.popup.UIAccountCreationContainer;
import org.exoplatform.mail.webui.popup.UIPopupAction;
import org.exoplatform.mail.webui.popup.UIPopupComponent;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
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
      @EventConfig(phase = Phase.DECODE, listeners = UISelectAccount.CancelAccountActionListener.class)
    }
)
public class UISelectAccount extends UIForm {
  final static public String FIELD_ACCOUNT = "accountSelect" ;


  public UISelectAccount() {
    List<SelectItemOption<String>> accountOptions = new ArrayList<SelectItemOption<String>>() ;
    addUIFormInput(new UIFormSelectBox(FIELD_ACCOUNT, FIELD_ACCOUNT, accountOptions)) ;
  }
  
  protected List<SelectItemOption<String>> getAccounts() throws Exception {
    List<SelectItemOption<String>> accountItems = new ArrayList<SelectItemOption<String>>() ;
    MailService mailSvr = getApplicationComponent(MailService.class) ;
    String currentUser = Util.getPortalRequestContext().getRemoteUser() ;
    System.out.println("\n\n currentUser" + currentUser);
    for(Account acc : mailSvr.getAccounts(currentUser)) {
      System.out.println("\n\n acc ");
      accountItems.add(new SelectItemOption<String>(acc.getId(), acc.getLabel())) ;
    } 
    return accountItems ;
  }

  public void refreshAccountList() throws Exception {
    System.out.println("\n\n refresh ");
    getUIFormSelectBox(FIELD_ACCOUNT).setOptions(getAccounts());
  }

  static  public class AddAccountActionListener extends EventListener<UISelectAccount> {
    public void execute(Event<UISelectAccount> event) throws Exception {
      System.out.println("========> AddAccountActionListener") ;
      UISelectAccount uiForm = event.getSource() ;
      UIMailPortlet uiPortlet = uiForm.getAncestorOfType(UIMailPortlet.class) ;
      UIPopupAction uiPopup = uiPortlet.getChild(UIPopupAction.class) ;
      UIAccountCreationContainer uiAccContainer = uiPortlet.createUIComponent(UIAccountCreationContainer.class, null, null) ;
      uiAccContainer.addChild(UIAccountCreation.class, null, null) ;
      uiPopup.activate(uiAccContainer, 700, 500, true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
    }
  }
  static  public class CancelAccountActionListener extends EventListener<UISelectAccount> {
    public void execute(Event<UISelectAccount> event) throws Exception {


    }
  }  

}
