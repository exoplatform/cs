/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.webui.popup.UIAccountCreation;
import org.exoplatform.mail.webui.popup.UIAccountCreationContainer;
import org.exoplatform.mail.webui.popup.UIPopupAction;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
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
    return mailSvr.getAccounts(currentUser) ;
  }
  private List<SelectItemOption<String>> getValues() throws Exception {
    List<SelectItemOption<String>>  options = new ArrayList<SelectItemOption<String>>() ;
    for(Account acc : getAccounts()) {
      options.add(new SelectItemOption<String>(acc.getLabel(), acc.getId())) ;
    }
    return options ;
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
  static  public class SelectAccountActionListener extends EventListener<UISelectAccount> {
    public void execute(Event<UISelectAccount> event) throws Exception {
      UISelectAccount uiSelectAcc = event.getSource() ;
      System.out.println("\n\n SelectAccountActionListener");

    }
  }  

}
