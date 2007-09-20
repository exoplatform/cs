/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.command.handler.GetApplicationHandler;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTabPane;
import org.exoplatform.webui.form.UIFormTextAreaInput;

/**
 * Created by The eXo Platform SARL
 * Author : Nam Phung
 *          phunghainam@gmail.com
 * Sep 18, 2007  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/templates/mail/webui/UIAccountSetting.gtmpl",
    events = {
        @EventConfig(listeners = UIAccountSetting.SelectAccountActionListener.class),
        @EventConfig(listeners = UIAccountSetting.SaveActionListener.class),
        @EventConfig(listeners = UIAccountSetting.CancelActionListener.class)
    }
)

public class UIAccountSetting extends UIFormTabPane {  
  public static final String TAB_ACCOUNT = "account";
  public static final String TAB_IDENTITY_SETTINGS = "identitySettings";
  public static final String TAB_SERVER_SETTINGS = "serverSettings";
  public static final String FIELD_ACCOUNT_NAME = "accountName";
  public static final String FIELD_INCOMING_USERNAME = "incomingUsername";
  public static final String FIELD_ACCOUNT_DESCRIPTION = "description";
  public static final String FIELD_OUTGOING_NAME = "yourOutgoingName";
  public static final String FIELD_EMAIL_ADDRESS = "yourEmailAddress";
  public static final String FIELD_EMAIL_PASSWORD = "yourEmailPassword";
  public static final String FIELD_REPLYTO_ADDRESS = "replyToAddress";
  public static final String FIELD_MAIL_SIGNATURE = "mailSignature" ;
  public static final String FIELD_PLACE_SIGNATURE = "placeSignature";
  public static final String FIELD_APPLY_SIGNATURE = "applySignatureBy";
  public static final String FIELD_SERVER_TYPE = "serverType";
  public static final String FIELD_INCOMING_SERVER = "incomingServer";
  public static final String FIELD_INCOMING_FOLDER = "messageComeInServer";
  public static final String FIELD_PORT = "port";
  public static final String FIELD_ISSSL = "isSSL";
  public static final String FIELD_CHECKMAIL_AUTO = "checkMailAutomatically";
  public static final String FIELD_EMPTY_TRASH = "emptyTrashWhenExit";
  public static final String OPTION_HEAD = "signOnHeadOfMessage";
  public static final String OPTION_FOOT = "signOnFootOfMessage";
  private String accountId_ = null;
  
  public UIAccountSetting() throws Exception {
    super("UIAccountSetting");
    UIFormInputWithActions  accountInputSet = new UIFormInputWithActions(TAB_ACCOUNT);
    accountInputSet.addUIFormInput(new UIFormStringInput(FIELD_ACCOUNT_NAME, null, null)) ;
    accountInputSet.addUIFormInput(new UIFormTextAreaInput(FIELD_ACCOUNT_DESCRIPTION, null, null)) ;
    addUIFormInput(accountInputSet); 
    
    UIFormInputWithActions  identityInputSet = new UIFormInputWithActions(TAB_IDENTITY_SETTINGS);
    identityInputSet.addUIFormInput(new UIFormStringInput(FIELD_OUTGOING_NAME, null, null));
    identityInputSet.addUIFormInput(new UIFormStringInput(FIELD_EMAIL_ADDRESS, null, null));
    identityInputSet.addUIFormInput(new UIFormStringInput(FIELD_EMAIL_PASSWORD, null, null).setType(UIFormStringInput.PASSWORD_TYPE));
    identityInputSet.addUIFormInput(new UIFormStringInput(FIELD_REPLYTO_ADDRESS, null, null));
    identityInputSet.addUIFormInput(new UIFormTextAreaInput(FIELD_MAIL_SIGNATURE, null, null));
    List<SelectItemOption<String>> signPlaceOptions = new ArrayList<SelectItemOption<String>>();
    signPlaceOptions.add(new SelectItemOption<String>(OPTION_HEAD));
    signPlaceOptions.add(new SelectItemOption<String>(OPTION_FOOT));
    identityInputSet.addUIFormInput(new UIFormSelectBox(FIELD_PLACE_SIGNATURE, FIELD_PLACE_SIGNATURE, signPlaceOptions));
    addUIFormInput(identityInputSet); 
    
    UIFormInputWithActions serverInputSet = new UIFormInputWithActions(TAB_SERVER_SETTINGS);
    serverInputSet.addUIFormInput(new UIFormInputInfo(FIELD_SERVER_TYPE, null, null));
    serverInputSet.addUIFormInput(new UIFormStringInput(FIELD_INCOMING_SERVER, null, null));
    serverInputSet.addUIFormInput(new UIFormStringInput(FIELD_INCOMING_FOLDER, null, null));
    serverInputSet.addUIFormInput(new UIFormStringInput(FIELD_PORT, null, null));
    serverInputSet.addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_ISSSL, null, null));
    serverInputSet.addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_CHECKMAIL_AUTO, null, null));
    serverInputSet.addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_EMPTY_TRASH, null, null));
    addUIFormInput(serverInputSet); 
    
    setRenderedChild(TAB_ACCOUNT);
    if (getAccounts() != null && getAccounts().size() > 0) { 
      setSelectedAccountId(getAccounts().get(0).getId()); 
    }
  }
  
  public String getSelectedAccountId() throws Exception { return accountId_; }
  public void setSelectedAccountId(String accountId) throws Exception { this.accountId_ = accountId; }  

  public String getFieldAccountNameValue() { 
    UIFormInputWithActions uiInput = getChildById(TAB_ACCOUNT);
    return uiInput.getUIStringInput(FIELD_ACCOUNT_NAME).getValue();
  }
  
  public String getFieldAccountDescription() {
    UIFormInputWithActions uiInput = getChildById(TAB_ACCOUNT);
    return uiInput.getUIStringInput(FIELD_ACCOUNT_DESCRIPTION).getValue();
  }
  
  public String getFieldOutgoingName() {
    UIFormInputWithActions uiInput = getChildById(TAB_IDENTITY_SETTINGS);
    return uiInput.getUIStringInput(FIELD_OUTGOING_NAME).getValue();
  }
  
  public String getFieldMailAddress() {
    UIFormInputWithActions uiInput = getChildById(TAB_IDENTITY_SETTINGS);
    return uiInput.getUIStringInput(FIELD_EMAIL_ADDRESS).getValue();
  }
  
  public String getFieldMailPassword() {
    UIFormInputWithActions uiInput = getChildById(TAB_IDENTITY_SETTINGS);
    return uiInput.getUIStringInput(FIELD_EMAIL_PASSWORD).getValue();
  }
  
  
  public String getFieldIncomingServer() {
    UIFormInputWithActions uiInput = getChildById(TAB_SERVER_SETTINGS);
    return uiInput.getUIStringInput(FIELD_INCOMING_SERVER).getValue();
  }
  
  public String getFieldMailSignature() {
    UIFormInputWithActions uiInput = getChildById(TAB_IDENTITY_SETTINGS);
    return uiInput.getUIStringInput(FIELD_MAIL_SIGNATURE).getValue();
  }
  
  public String getFieldPlaceSignature() {
    UIFormInputWithActions uiInput = getChildById(TAB_IDENTITY_SETTINGS);
    return uiInput.getUIFormSelectBox(FIELD_PLACE_SIGNATURE).getValue();
  }
  
  
  public String getFieldReplyAddress() {
    UIFormInputWithActions uiInput = getChildById(TAB_IDENTITY_SETTINGS);
    return uiInput.getUIStringInput(FIELD_REPLYTO_ADDRESS).getValue();
  }
  
  public String getFieldIncomingFolder() {
    UIFormInputWithActions uiInput = getChildById(TAB_SERVER_SETTINGS);
    return uiInput.getUIStringInput(FIELD_INCOMING_FOLDER).getValue();
  }
  
  public String getFieldPort() {
    UIFormInputWithActions uiInput = getChildById(TAB_SERVER_SETTINGS);
    return uiInput.getUIStringInput(FIELD_PORT).getValue();
  }
  
  public boolean getFieldIsSSL() {
    UIFormInputWithActions uiInput = getChildById(TAB_SERVER_SETTINGS);
    return uiInput.getUIFormCheckBoxInput(FIELD_ISSSL).isChecked();
  }
  
  public boolean getFieldCheckMailAuto() {
    UIFormInputWithActions uiInput = getChildById(TAB_SERVER_SETTINGS);
    return uiInput.getUIFormCheckBoxInput(FIELD_CHECKMAIL_AUTO).isChecked();
  }
  
  public boolean getFieldEmptyTrash() {
    UIFormInputWithActions uiInput = getChildById(TAB_SERVER_SETTINGS);
    return uiInput.getUIFormCheckBoxInput(FIELD_EMPTY_TRASH).isChecked();
  }
  
  public void fillAllField() throws Exception {
    MailService mailSrv = getApplicationComponent(MailService.class);
    String username = Util.getPortalRequestContext().getRemoteUser();
    Account account = mailSrv.getAccountById(username, getSelectedAccountId());
    fillAllField(account);
  }
  
  public void fillAllField(Account account) throws Exception {
    UIFormInputWithActions uiAccountInput = getChildById(TAB_ACCOUNT);
    uiAccountInput.getUIStringInput(FIELD_ACCOUNT_NAME).setValue(account.getLabel());
    uiAccountInput.getUIStringInput(FIELD_ACCOUNT_DESCRIPTION).setValue(account.getDescription());
    
    UIFormInputWithActions uiIdentityInput = getChildById(TAB_IDENTITY_SETTINGS);
    uiIdentityInput.getUIStringInput(FIELD_OUTGOING_NAME).setValue(account.getUserDisplayName());
    uiIdentityInput.getUIStringInput(FIELD_EMAIL_ADDRESS).setValue(account.getEmailAddress());
    uiIdentityInput.getUIStringInput(FIELD_EMAIL_PASSWORD).setValue(account.getPassword());
    uiIdentityInput.getUIStringInput(FIELD_REPLYTO_ADDRESS).setValue(account.getEmailReplyAddress());
    uiIdentityInput.getUIStringInput(FIELD_MAIL_SIGNATURE).setValue(account.getSignature());
    
    UIFormInputWithActions uiServerInput = getChildById(TAB_SERVER_SETTINGS);
    uiServerInput.getUIStringInput(FIELD_PORT).setValue(account.getPort());
    uiServerInput.getUIStringInput(FIELD_INCOMING_FOLDER).setValue(account.getFolder());
    uiServerInput.getUIStringInput(FIELD_INCOMING_SERVER).setValue(account.getHost());
    uiServerInput.getUIFormInputInfo(FIELD_SERVER_TYPE).setValue(account.getProtocol());
    uiServerInput.getUIFormInputInfo(FIELD_SERVER_TYPE).setValue(account.getProtocol());
    uiServerInput.getUIFormCheckBoxInput(FIELD_ISSSL).setChecked(account.isSsl());
    
  } 
  
  public String[] getActions() {return new String[]{"Save", "Cancel"};}
  
  public List<Account> getAccounts() throws Exception {
    MailService mailSrv = getApplicationComponent(MailService.class);
    String username = Util.getPortalRequestContext().getRemoteUser();
    return mailSrv.getAccounts(username);
  }
  
  static  public class SelectAccountActionListener extends EventListener<UIAccountSetting> {
    public void execute(Event<UIAccountSetting> event) throws Exception {
      UIAccountSetting uiAccountSetting = event.getSource() ;
      System.out.println(" ==========> SelectAccountActionListener") ;
      String accountId = event.getRequestContext().getRequestParameter(OBJECTID);
      uiAccountSetting.setSelectedAccountId(accountId);
      uiAccountSetting.fillAllField();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAccountSetting.getParent());
    }
  }
  
  static  public class SaveActionListener extends EventListener<UIAccountSetting> {
    public void execute(Event<UIAccountSetting> event) throws Exception {
      UIAccountSetting uiAccountSetting = event.getSource() ;
      System.out.println(" ==========> SaveActionListener") ;
      MailService mailSrv = uiAccountSetting.getApplicationComponent(MailService.class);
      String username = Util.getPortalRequestContext().getRemoteUser();
      Account acc = mailSrv.getAccountById(username, uiAccountSetting.getSelectedAccountId());
      String accName = uiAccountSetting.getFieldAccountNameValue();
      String accDes = uiAccountSetting.getFieldAccountDescription();
      String displayName = uiAccountSetting.getFieldOutgoingName();
      String email = uiAccountSetting.getFieldMailAddress();
      String replyMail = uiAccountSetting.getFieldReplyAddress();
      String signature = uiAccountSetting.getFieldMailSignature();
      String userName = uiAccountSetting.getFieldMailAddress();
      String password = uiAccountSetting.getFieldMailPassword();
      String popHost = uiAccountSetting.getFieldIncomingServer();
      String popPort = uiAccountSetting.getFieldPort();
      boolean isSSL = uiAccountSetting.getFieldIsSSL();
      String storeFolder = uiAccountSetting.getFieldIncomingFolder();
      
      acc.setLabel(accName) ;
      acc.setDescription(accDes) ;
      acc.setUserDisplayName(displayName) ;
      acc.setEmailAddress(email) ;
      acc.setEmailReplyAddress(replyMail) ;
      acc.setSignature(signature) ;
      acc.setServerProperty(Utils.SVR_USERNAME, userName); 
      acc.setServerProperty(Utils.SVR_PASSWORD, password);
      acc.setServerProperty(Utils.SVR_POP_HOST, popHost);
      acc.setServerProperty(Utils.SVR_POP_PORT, popPort);  
      acc.setServerProperty(Utils.SVR_SSL, String.valueOf(isSSL));
      acc.setServerProperty(Utils.SVR_FOLDER, storeFolder) ;
      acc.setServerProperty(Utils.SVR_SMTP_USER, userName);
      
      UIApplication uiApp = uiAccountSetting.getAncestorOfType(UIApplication.class) ;
      try {
        mailSrv.updateAccount(username, acc);
        uiApp.addMessage(new ApplicationMessage("UIAccountSetting.msg.edit-acc-successfully", null));
      } catch(Exception e) {
        uiApp.addMessage(new ApplicationMessage("UIAccountSetting.msg.edit-acc-unsuccessfully", null));
        e.printStackTrace() ;
        return ;
      }
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIAccountSetting> {
    public void execute(Event<UIAccountSetting> event) throws Exception {
      System.out.println(" ==========> CancelActionListener") ;
      event.getSource().getAncestorOfType(UIMailPortlet.class).cancelAction();
    }
  }
}