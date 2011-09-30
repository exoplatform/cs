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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.exoplatform.cs.common.webui.UIPopupAction;
import org.exoplatform.cs.common.webui.UIPopupActionContainer;
import org.exoplatform.mail.DataCache;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.MailSetting;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.UIFormDateTimePicker;
import org.exoplatform.mail.webui.UIFormInputWithActions;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.mail.webui.UIMessageArea;
import org.exoplatform.mail.webui.UIMessageList;
import org.exoplatform.mail.webui.UIMessagePreview;
import org.exoplatform.mail.webui.UINavigationContainer;
import org.exoplatform.mail.webui.UISelectAccount;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTabPane;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;

/**
 * Created by The eXo Platform SARL
 * Author : Nam Phung
 *          phunghainam@gmail.com
 * Sep 18, 2007  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/templates/mail/webui/popup/UIAccountSetting.gtmpl",
    events = {
        @EventConfig(listeners = UIAccountSetting.SelectAccountActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UIAccountSetting.AddNewAccountActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UIAccountSetting.DeleteAccountActionListener.class, phase = Phase.DECODE, confirm="UIAccountSetting.msg.confirm-remove-account"),
        @EventConfig(listeners = UIAccountSetting.SaveActionListener.class),
        @EventConfig(listeners = UIAccountSetting.CancelActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UIAccountSetting.CheckFromDateActionListener.class, phase = Phase.DECODE), 
        @EventConfig(listeners = UIAccountSetting.IsCustomInboxActionListener.class, phase = Phase.DECODE)
    }
)

public class UIAccountSetting extends UIFormTabPane { 
  private static final Log log = ExoLogger.getExoLogger(UIAccountSetting.class);
  
  public static final String TAB_IDENTITY_SETTINGS = "identitySettings";
  public static final String TAB_INCOMING = "incoming";
  public static final String TAB_OUTGOING = "outgoing";
  public static final String TAB_FETCH_OPTIONS = "fetchOptions";
  
  public static final String FIELD_ACCOUNT_NAME = "accountName";
  public static final String FIELD_DISPLAY_NAME = "display-name".intern();
  public static final String FIELD_INCOMING_USERNAME = "incomingUsername";
  public static final String FIELD_OUTGOING_NAME = "yourOutgoingName";
  public static final String FIELD_EMAIL_ADDRESS = "yourEmailAddress";
  public static final String FIELD_INCOMING_ACCOUNT = "incomingAccount";
  public static final String FIELD_INCOMING_PASSWORD = "incomingPassword";
  public static final String FIELD_REPLYTO_ADDRESS = "replyToAddress";
  public static final String FIELD_MAIL_SIGNATURE = "mailSignature" ;
  public static final String FIELD_SERVER_TYPE = "serverType";
  public static final String FIELD_INCOMING_SERVER = "incomingServer";
  public static final String FIELD_INCOMING_PORT = "incomingPort";
  public static final String FIELD_OUTGOING_SERVER = "outgoingServer";
  public static final String FIELD_OUTGOING_PORT = "outgoingPort";
  public static final String FIELD_INCOMING_FOLDER = "messageComeInFolder";
  public static final String FIELD_IS_INCOMING_SSL = "isSSL";
  public static final String FIELD_IS_OUTGOING_SSL = "isOutgoingSsl";
  public static final String IS_OUTGOING_AUTHENTICATION = "isOutgoingAuthentication";
  public static final String USE_INCOMINGSETTING_FOR_OUTGOING_AUTHEN = "useIncomingSettingForOutgoingAuthent";
  public static final String OUTGOING_USERNAME = "outgoingUsername";
  public static final String OUTGOING_PASSWORD = "outgoingPassword";
  public static final String FIELD_CHECKMAIL_AUTO = "checkMailAutomatically";
  public static final String FIELD_LEAVE_ON_SERVER = "leaveMailOnServer";
  public static final String FIELD_IS_SAVE_PASSWORD = "isSavePassword" ;
  private String accountId_ = null;
  UIFormCheckBoxInput<Boolean> leaveOnServer_ ;
  public static final String CHECK_FROM_DATE = "checkFromDate";
  public static final String FROM_DATE = "fromDate";
  public static final String IS_CUSTOM_INBOX = "isCustomInbox";
  
  public static final String FIELD_SECURE_AUTHENTICATION_INCOMING = "secureAuthenticationIncoming";
  public static final String FIELD_AUTHENTICATIONS_MECHANISM = "authenticationMechanism";
  public static final String FIELD_BUTTON_CHEK_FOR_SUPPORTED = "checkForSupported";
  
  public static final String FIELD_SECURE_AUTHENTICATION_OUTGOING = "secureAuthenticationOutgoing";
  public static final String FIELD_AUTHENTICATIONS_MECHANISM_OUTGOING = "authenticationMechanismOutgoing";
  
  public UIAccountSetting() throws Exception {
    super("UIAccountSetting");
    
    UIFormInputWithActions  identityInputSet = new UIFormInputWithActions(TAB_IDENTITY_SETTINGS);
    identityInputSet.addUIFormInput(new UIFormStringInput(FIELD_ACCOUNT_NAME, null, null).addValidator(MandatoryValidator.class)) ;
    identityInputSet.addUIFormInput(new UIFormStringInput(FIELD_DISPLAY_NAME, null, null).addValidator(MandatoryValidator.class)) ;
    identityInputSet.addUIFormInput(new UIFormStringInput(FIELD_EMAIL_ADDRESS, null, null).addValidator(MandatoryValidator.class));
    identityInputSet.addUIFormInput(new UIFormStringInput(FIELD_REPLYTO_ADDRESS, null, null));
    identityInputSet.addUIFormInput(new UIFormTextAreaInput(FIELD_MAIL_SIGNATURE, null, null));
    addUIFormInput(identityInputSet); 
    
    UIIncomingInputSet incomingInputSet = new UIIncomingInputSet(TAB_INCOMING);
    UIFormSelectBox serverType = new UIFormSelectBox(FIELD_SERVER_TYPE, null, getServerTypeValues()) ;
    serverType.setEditable(false);
    serverType.setEnable(false);
    incomingInputSet.addUIFormInput(serverType) ;
    
    UIFormStringInput incomingServer = new UIFormStringInput(FIELD_INCOMING_SERVER, null, null);
    incomingServer.addValidator(MandatoryValidator.class);
    if (!Utils.isUserAllowedInconmingServer()) incomingServer.setEnable(false);
    incomingInputSet.addUIFormInput(incomingServer);
    
    UIFormStringInput incomingServerPort = new UIFormStringInput(FIELD_INCOMING_PORT, null, null);
    incomingServerPort.addValidator(MandatoryValidator.class);
    if (!Utils.isUserAllowedIncomingPort()) incomingServerPort.setEnable(false);
    incomingInputSet.addUIFormInput(incomingServerPort);
    
    incomingInputSet.addUIFormInput(new UIFormStringInput(FIELD_INCOMING_ACCOUNT, null, null).addValidator(MandatoryValidator.class));
    UIFormStringInput passwordField = new UIFormStringInput(FIELD_INCOMING_PASSWORD, null, null) ;
    passwordField.setType(UIFormStringInput.PASSWORD_TYPE) ;
    passwordField.addValidator(MandatoryValidator.class) ;
    incomingInputSet.addUIFormInput(passwordField);
    incomingInputSet.addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_IS_SAVE_PASSWORD, null, null));
    UIFormCheckBoxInput<Boolean> ssl = new UIFormCheckBoxInput<Boolean>(FIELD_IS_INCOMING_SSL, FIELD_IS_INCOMING_SSL, null);//getFieldIsSSL()
    UIFormSelectBox secureAuths = new UIFormSelectBox(FIELD_SECURE_AUTHENTICATION_INCOMING, null, getSecureAuthsValues()) ;
    secureAuths.setValue(Utils.getIncomingSecureAuthentication());
    if (!Utils.isUserAllowedIncomingSecureAuthentication()) {
      ssl.setEnable(false);
      secureAuths.setEnable(false);
    }
   // ssl.setOnChange("ChangeSSL");
    //secureAuths.setOnChange("ChangeSSLType");
    UIFormSelectBox authMechanism = new UIFormSelectBox(FIELD_AUTHENTICATIONS_MECHANISM, null, getAuthMechanismsValues()) ;
    authMechanism.setValue(Utils.getIncomingAuthenticationMechanism());
    if (!Utils.isUserAllowedIncomingAuthenticationMechanism()) {
      authMechanism.setEnable(false);
    }
    //authMechanism.setOnChange("ChangeAuthMechsIncoming");
    incomingInputSet.addUIFormInput(ssl);
    incomingInputSet.addUIFormInput(secureAuths);
    incomingInputSet.addUIFormInput(authMechanism);
    
    UIOutgoingInputSet outGoingInputSet = new UIOutgoingInputSet(TAB_OUTGOING);
    
    UIFormStringInput outgoingServer = new UIFormStringInput(FIELD_OUTGOING_SERVER, null, null);
    outgoingServer.addValidator(MandatoryValidator.class);
    if (!Utils.isUserAllowedOutgoingServer()) outgoingServer.setEnable(false);
    outGoingInputSet.addUIFormInput(outgoingServer);
    
    UIFormStringInput outgoingServerPort = new UIFormStringInput(FIELD_OUTGOING_PORT, null, null);
    outgoingServerPort.addValidator(MandatoryValidator.class);
    if (!Utils.isUserAllowedOutgoingPort()) outgoingServerPort.setEnable(false);
    outGoingInputSet.addUIFormInput(outgoingServerPort);
    
    UIFormCheckBoxInput<Boolean> outgoingssl = new UIFormCheckBoxInput<Boolean>(FIELD_IS_OUTGOING_SSL, FIELD_IS_OUTGOING_SSL, null);
    UIFormSelectBox secureAuthsOutgoing = new UIFormSelectBox(FIELD_SECURE_AUTHENTICATION_OUTGOING, null, getSecureAuthsValues()) ;
    
    secureAuthsOutgoing.setValue(Utils.getOutgoingSecureAuthentication());
    if (!Utils.isUserAllowedOutgoingSecureAuthentication()) {
      outgoingssl.setEnable(false);
      secureAuthsOutgoing.setEnable(false);
    }
    //secureAuthsOutgoing.setOnChange("ChangeOutgoingSSLType");
    //outgoingssl.setOnChange("ChangeOutgoingSSL"); 
    outGoingInputSet.addUIFormInput(outgoingssl);
    UIFormCheckBoxInput<Boolean> isOutgoingAuthen = new UIFormCheckBoxInput<Boolean>(IS_OUTGOING_AUTHENTICATION, null, null);
    //isOutgoingAuthen.setOnChange("EnableSMTPAuthentication"); 
    outGoingInputSet.addUIFormInput(isOutgoingAuthen);
    UIFormCheckBoxInput<Boolean> useIncomingSetting = new UIFormCheckBoxInput<Boolean>(USE_INCOMINGSETTING_FOR_OUTGOING_AUTHEN, null, null);
    //useIncomingSetting.setOnChange("UseIncoming");
    outGoingInputSet.addUIFormInput(useIncomingSetting);
    outGoingInputSet.addUIFormInput(new UIFormStringInput(OUTGOING_USERNAME, null, null).addValidator(MandatoryValidator.class));
    outGoingInputSet.addUIFormInput(new UIFormStringInput(OUTGOING_PASSWORD, null, null).setType(UIFormStringInput.PASSWORD_TYPE).addValidator(MandatoryValidator.class));
    UIFormSelectBox authMechanismOutgoing = new UIFormSelectBox(FIELD_AUTHENTICATIONS_MECHANISM_OUTGOING, null, getAuthMechanismsValues()) ;
    authMechanismOutgoing.setValue(Utils.getOutgoingAuthenticationMechanism());
    if (!Utils.isUserAllowedOutgoingAuthenticationMechanism()) {
      authMechanismOutgoing.setEnable(false);
    }
    
    outGoingInputSet.addUIFormInput(authMechanismOutgoing);
    outGoingInputSet.addUIFormInput(secureAuthsOutgoing);
    
    UIFetchOptionsInputSet fetchOptionsInputSet = new UIFetchOptionsInputSet(TAB_FETCH_OPTIONS);
    fetchOptionsInputSet.addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_CHECKMAIL_AUTO, null, null));
    leaveOnServer_ = new UIFormCheckBoxInput<Boolean>(FIELD_LEAVE_ON_SERVER, null, null) ;
    fetchOptionsInputSet.addUIFormInput(leaveOnServer_);      
    if (!Utils.isUserAllowedLeaveOnServer()) {
      leaveOnServer_.setEnable(false);
    }
    UIFormCheckBoxInput<Boolean> checkFromDate = new UIFormCheckBoxInput<Boolean>(CHECK_FROM_DATE, CHECK_FROM_DATE, null);
    checkFromDate.setOnChange("CheckFromDate");
    fetchOptionsInputSet.addUIFormInput(checkFromDate);
    fetchOptionsInputSet.addUIFormInput(new UIFormDateTimePicker(FROM_DATE, FROM_DATE, null, true));
    UIFormCheckBoxInput<Boolean> isCustomInbox = new UIFormCheckBoxInput<Boolean>(IS_CUSTOM_INBOX, IS_CUSTOM_INBOX, null);
    isCustomInbox.setOnChange("IsCustomInbox");
    fetchOptionsInputSet.addUIFormInput(isCustomInbox);
    fetchOptionsInputSet.addUIFormInput(new UIFormStringInput(FIELD_INCOMING_FOLDER, null, null));
    addUIFormInput(incomingInputSet);
    addUIFormInput(outGoingInputSet);
    addUIFormInput(fetchOptionsInputSet);
    setSelectedTab(identityInputSet.getId()) ;
  }
  
  private List<SelectItemOption<String>> getServerTypeValues(){
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    options.add(new SelectItemOption<String>(Utils.POP3, Utils.POP3));
    options.add(new SelectItemOption<String>(Utils.IMAP, Utils.IMAP)) ;
    return options ;
  }
  
  public String getSelectedAccountId() throws Exception { return accountId_; }
  public void setSelectedAccountId(String accountId) throws Exception { this.accountId_ = accountId; }  

  public String getFieldAccountNameValue() {
    UIFormInputWithActions uiInput = getChildById(TAB_IDENTITY_SETTINGS);
    return uiInput.getUIStringInput(FIELD_ACCOUNT_NAME).getValue();
  }
  
  public String getDisplayName() { 
    UIFormInputWithActions uiInput = getChildById(TAB_IDENTITY_SETTINGS);
    return uiInput.getUIStringInput(FIELD_DISPLAY_NAME).getValue();
  }

  public String getFieldMailAddress() {
    UIFormInputWithActions uiInput = getChildById(TAB_IDENTITY_SETTINGS);
    return uiInput.getUIStringInput(FIELD_EMAIL_ADDRESS).getValue();
  }
  
  public String getFieldProtocol() {
    UIFormInputWithActions uiInput = getChildById(TAB_INCOMING);
    return uiInput.getUIFormSelectBox(FIELD_SERVER_TYPE).getValue();
  }
  
  public String getFieldIncomingAccount() {
    UIFormInputWithActions uiInput = getChildById(TAB_INCOMING);
    return uiInput.getUIStringInput(FIELD_INCOMING_ACCOUNT).getValue();
  }
  
  public String getFieldIncomingPassword() {
    UIFormInputWithActions uiInput = getChildById(TAB_INCOMING);
    return uiInput.getUIStringInput(FIELD_INCOMING_PASSWORD).getValue() ;
  }  
  
  public String getFieldIncomingServer() {
    UIFormInputWithActions uiInput = getChildById(TAB_INCOMING);
    return uiInput.getUIStringInput(FIELD_INCOMING_SERVER).getValue();
  }
  
  public String getFieldIncomingPort() {
    UIFormInputWithActions uiInput = getChildById(TAB_INCOMING);
    return uiInput.getUIStringInput(FIELD_INCOMING_PORT).getValue();
  }
  
  public String getFieldOutgoingServer() {
    UIFormInputWithActions uiInput = getChildById(TAB_OUTGOING);
    return uiInput.getUIStringInput(FIELD_OUTGOING_SERVER).getValue();
  }
  
  public String getFieldOutgoingPort() {
    UIFormInputWithActions uiInput = getChildById(TAB_OUTGOING);
    return uiInput.getUIStringInput(FIELD_OUTGOING_PORT).getValue();
  }
  
  public String getFieldMailSignature() {
    UIFormInputWithActions uiInput = getChildById(TAB_IDENTITY_SETTINGS);
    return uiInput.getUIFormTextAreaInput(FIELD_MAIL_SIGNATURE).getValue();
  }
  
  
  public String getOutgoingUser() {
    UIFormInputWithActions uiInput = getChildById(TAB_OUTGOING);
    return uiInput.getUIStringInput(OUTGOING_USERNAME).getValue();
  }
  
  public String getOutgoingPassword() {
    UIFormInputWithActions uiInput = getChildById(TAB_OUTGOING);
    return uiInput.getUIStringInput(OUTGOING_PASSWORD).getValue();
  }
  
  public boolean isOutgoingAuthen() {
    UIFormInputWithActions uiInput = getChildById(TAB_OUTGOING);
    return uiInput.getUIFormCheckBoxInput(IS_OUTGOING_AUTHENTICATION).isChecked();
  }
  
  public boolean useIncomingSettingForOutgoingAuthen() {
    UIFormInputWithActions uiInput = getChildById(TAB_OUTGOING);
    return uiInput.getUIFormCheckBoxInput(USE_INCOMINGSETTING_FOR_OUTGOING_AUTHEN).isChecked();
  }
  
  public boolean isSavePassword() {
    UIFormInputWithActions uiInput = getChildById(TAB_INCOMING);
    return uiInput.getUIFormCheckBoxInput(FIELD_IS_SAVE_PASSWORD).isChecked();
  }
  
  public String getFieldReplyAddress() {
    UIFormInputWithActions uiInput = getChildById(TAB_IDENTITY_SETTINGS);
    return uiInput.getUIStringInput(FIELD_REPLYTO_ADDRESS).getValue();
  }
  
  public String getFieldIncomingFolder() {
    UIFormInputWithActions uiInput = getChildById(TAB_FETCH_OPTIONS);
    return uiInput.getUIStringInput(FIELD_INCOMING_FOLDER).getValue();
  }
  
  public boolean getFieldIsSSL() {
    UIFormInputWithActions uiInput = getChildById(TAB_INCOMING);
    return uiInput.getUIFormCheckBoxInput(FIELD_IS_INCOMING_SSL).isChecked();
  }
  
  public boolean getFieldOutgoingSSL() {
    UIFormInputWithActions uiInput = getChildById(TAB_OUTGOING);
    return uiInput.getUIFormCheckBoxInput(FIELD_IS_OUTGOING_SSL).isChecked();
  }
  
  public boolean getFieldCheckMailAuto() {
    UIFormInputWithActions uiInput = getChildById(TAB_FETCH_OPTIONS);
    return uiInput.getUIFormCheckBoxInput(FIELD_CHECKMAIL_AUTO).isChecked();
  }
  
  public boolean getFieldCheckFromDate() {
    UIFormInputWithActions uiInput = getChildById(TAB_FETCH_OPTIONS);
    return uiInput.getUIFormCheckBoxInput(CHECK_FROM_DATE).isChecked();
  }
  
  public boolean isCustomInbox() {
    UIFormInputWithActions uiInput = getChildById(TAB_FETCH_OPTIONS);
    return uiInput.getUIFormCheckBoxInput(IS_CUSTOM_INBOX).isChecked();
  }
  
  public Calendar getFieldCheckFrom() {
    UIFormInputWithActions uiInput = getChildById(TAB_FETCH_OPTIONS);
    return ((UIFormDateTimePicker) uiInput.getChildById(FROM_DATE)).getCalendar();    
  }
  
  public boolean getFieldLeaveOnServer() {
    UIFormInputWithActions uiInput = getChildById(TAB_FETCH_OPTIONS);
    return uiInput.getUIFormCheckBoxInput(FIELD_LEAVE_ON_SERVER).isChecked();
  }
  
  public void fillField() throws Exception {
    DataCache dataCache = (DataCache) WebuiRequestContext.getCurrentInstance().getAttribute(DataCache.class);
    String username = Util.getPortalRequestContext().getRemoteUser();
    Account account = dataCache.getAccountById(username, getSelectedAccountId());
    UIFormInputWithActions uiIdentityInput = getChildById(TAB_IDENTITY_SETTINGS);
    uiIdentityInput.getUIStringInput(FIELD_ACCOUNT_NAME).setValue(account.getLabel());
    uiIdentityInput.getUIStringInput(FIELD_DISPLAY_NAME).setValue(account.getUserDisplayName());
    uiIdentityInput.getUIStringInput(FIELD_EMAIL_ADDRESS).setValue(account.getEmailAddress());
    uiIdentityInput.getUIStringInput(FIELD_REPLYTO_ADDRESS).setValue(account.getEmailReplyAddress());
    uiIdentityInput.getUIFormTextAreaInput(FIELD_MAIL_SIGNATURE).setValue(account.getSignature());

    UIFormInputWithActions uiIncomingInput = getChildById(TAB_INCOMING);
    uiIncomingInput.getUIStringInput(FIELD_INCOMING_SERVER).setValue(account.getIncomingHost());
    uiIncomingInput.getUIStringInput(FIELD_INCOMING_PORT).setValue(account.getIncomingPort());
    uiIncomingInput.getUIFormCheckBoxInput(FIELD_IS_INCOMING_SSL).setChecked(account.isIncomingSsl());
    if (this.getFieldIsSSL()) {
      uiIncomingInput.getUIFormSelectBox(FIELD_SECURE_AUTHENTICATION_INCOMING).setValue(account.getSecureAuthsIncoming());
      uiIncomingInput.getUIFormSelectBox(FIELD_AUTHENTICATIONS_MECHANISM).setValue(account.getAuthMechsIncoming());
    } else {
      uiIncomingInput.getUIFormSelectBox(FIELD_SECURE_AUTHENTICATION_INCOMING).setValue(account.getSecureAuthsIncoming());
      uiIncomingInput.getUIFormSelectBox(FIELD_AUTHENTICATIONS_MECHANISM).setValue(account.getAuthMechsIncoming());
    }

    uiIncomingInput.getUIStringInput(FIELD_INCOMING_ACCOUNT).setValue(account.getIncomingUser());
    uiIncomingInput.getUIStringInput(FIELD_INCOMING_PASSWORD).setValue(account.getIncomingPassword());
    uiIncomingInput.getUIFormCheckBoxInput(FIELD_IS_SAVE_PASSWORD).setChecked(account.isSavePassword());

    UIFormInputWithActions uiOutgoingInput = getChildById(TAB_OUTGOING);
    uiOutgoingInput.getUIStringInput(FIELD_OUTGOING_SERVER).setValue(account.getOutgoingHost());
    uiOutgoingInput.getUIStringInput(FIELD_OUTGOING_PORT).setValue(account.getOutgoingPort());
    uiOutgoingInput.getUIFormCheckBoxInput(FIELD_IS_OUTGOING_SSL).setChecked(account.isOutgoingSsl());
    uiOutgoingInput.getUIFormCheckBoxInput(IS_OUTGOING_AUTHENTICATION).setChecked(account.isOutgoingAuthentication());
    if (isOutgoingAuthen()) {
      uiOutgoingInput.getUIFormCheckBoxInput(USE_INCOMINGSETTING_FOR_OUTGOING_AUTHEN).setEnable(true);
      uiOutgoingInput.getUIFormCheckBoxInput(USE_INCOMINGSETTING_FOR_OUTGOING_AUTHEN).setChecked(
          account.useIncomingSettingForOutgoingAuthent());
      if (account.useIncomingSettingForOutgoingAuthent()) {
        uiOutgoingInput.getUIStringInput(OUTGOING_USERNAME).setEnable(false).setValue(account.getIncomingUser());
        uiOutgoingInput.getUIStringInput(OUTGOING_PASSWORD).setEnable(false).setValue(account.getIncomingPassword());
      } else {
        uiOutgoingInput.getUIStringInput(OUTGOING_USERNAME).setValue(account.getOutgoingUserName());
        uiOutgoingInput.getUIStringInput(OUTGOING_PASSWORD).setValue(account.getOutgoingPassword());
      }
    } else {
      uiOutgoingInput.getUIFormCheckBoxInput(USE_INCOMINGSETTING_FOR_OUTGOING_AUTHEN).setEnable(false);
      uiOutgoingInput.getUIFormCheckBoxInput(USE_INCOMINGSETTING_FOR_OUTGOING_AUTHEN).setChecked(
          account.useIncomingSettingForOutgoingAuthent());
      uiOutgoingInput.getUIStringInput(OUTGOING_USERNAME).setEnable(false).setValue(account.getIncomingUser());
      uiOutgoingInput.getUIStringInput(OUTGOING_PASSWORD).setEnable(false).setValue(account.getIncomingPassword());
    }

    if (getFieldOutgoingSSL()) {
      uiOutgoingInput.getUIFormSelectBox(FIELD_SECURE_AUTHENTICATION_OUTGOING).setValue(account.getSecureAuthsOutgoing());
      uiOutgoingInput.getUIFormSelectBox(FIELD_AUTHENTICATIONS_MECHANISM_OUTGOING).setValue(account.getAuthMechsOutgoing());
    } else {
      uiOutgoingInput.getUIFormSelectBox(FIELD_SECURE_AUTHENTICATION_OUTGOING).setValue(account.getSecureAuthsOutgoing());
      uiOutgoingInput.getUIFormSelectBox(FIELD_AUTHENTICATIONS_MECHANISM_OUTGOING).setValue(account.getAuthMechsOutgoing());
    }

    UIFormInputWithActions uifetchOptionsInput = getChildById(TAB_FETCH_OPTIONS);
    uifetchOptionsInput.getUIFormCheckBoxInput(CHECK_FROM_DATE).setChecked(!account.isCheckAll());
    if (account.isCheckAll()) {
      ((UIFormDateTimePicker) uifetchOptionsInput.getChildById(FROM_DATE)).setEditable(false);
    } else {
      GregorianCalendar cal = new GregorianCalendar();
      cal.setTime(account.getCheckFromDate());
      ((UIFormDateTimePicker) uifetchOptionsInput.getChildById(FROM_DATE)).setCalendar(cal);
    }

    uifetchOptionsInput.getUIFormCheckBoxInput(IS_CUSTOM_INBOX).setChecked(account.isCustomInbox());
    if (isCustomInbox()) {
      uifetchOptionsInput.getUIStringInput(FIELD_INCOMING_FOLDER).setEnable(true).setValue(account.getIncomingFolder());
    } else {
      uifetchOptionsInput.getUIStringInput(FIELD_INCOMING_FOLDER).setEnable(false).setValue(account.getIncomingFolder());
    }
    uiIncomingInput.getUIFormSelectBox(FIELD_SERVER_TYPE).setValue(account.getProtocol());
    uifetchOptionsInput.getUIFormCheckBoxInput(FIELD_CHECKMAIL_AUTO).setChecked(account.checkedAuto());
    uifetchOptionsInput.getUIFormCheckBoxInput(FIELD_LEAVE_ON_SERVER).setChecked(
        Boolean.valueOf(account.getServerProperties().get(Utils.SVR_LEAVE_ON_SERVER)));
  }
  
  public String[] getActions() {return new String[]{"Save", "Cancel"};}
  
  public List<Account> getAccounts() throws Exception {
    DataCache dataCache = (DataCache) WebuiRequestContext.getCurrentInstance().getAttribute(DataCache.class);
    String username = Util.getPortalRequestContext().getRemoteUser();
    return dataCache.getAccounts(username);
  }
  
  static  public class SelectAccountActionListener extends EventListener<UIAccountSetting> {
    public void execute(Event<UIAccountSetting> event) throws Exception {
      UIAccountSetting uiAccountSetting = event.getSource() ;
      String accountId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiAccountSetting.setSelectedAccountId(accountId) ;
      uiAccountSetting.fillField();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAccountSetting.getParent()) ;
    }
  }
  
  static  public class AddNewAccountActionListener extends EventListener<UIAccountSetting> {
    public void execute(Event<UIAccountSetting> event) throws Exception {
      UIAccountSetting uiAccountSetting = event.getSource() ;
      UIPopupActionContainer uiActionContainer = uiAccountSetting.getAncestorOfType(UIPopupActionContainer.class) ;
      UIPopupAction uiChildPopup = uiActionContainer.getChild(UIPopupAction.class) ;
      UIAccountCreation uiAccountCreation = uiChildPopup.activate(UIAccountCreation.class, 700) ;
      uiAccountCreation.setChildPopup(true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiActionContainer) ;
    }
  }
  
  static  public class DeleteAccountActionListener extends EventListener<UIAccountSetting> {
    public void execute(Event<UIAccountSetting> event) throws Exception {
      UIAccountSetting uiAccSetting = event.getSource();
      UIMailPortlet uiPortlet = uiAccSetting.getAncestorOfType(UIMailPortlet.class);
      DataCache dataCache = uiPortlet.getDataCache();
      
      UIMessageList uiMsgList = uiPortlet.findFirstComponentOfType(UIMessageList.class);
      UIMessagePreview uiMsgPreview = uiPortlet.findFirstComponentOfType(UIMessagePreview.class);
      UISelectAccount uiSelectAccount = uiPortlet.findFirstComponentOfType(UISelectAccount.class);
      String username = uiPortlet.getCurrentUser();
      MailService mailSvr = uiPortlet.getApplicationComponent(MailService.class);
      try {
        String removedAccId = uiAccSetting.getSelectedAccountId();
        mailSvr.removeAccount(username, removedAccId);
        dataCache.clearAccountCache();
        
        MailSetting mailSetting = mailSvr.getMailSetting(username);
        if (uiAccSetting.getAccounts().size() > 0) {
          String newSelectedAcc = uiAccSetting.getAccounts().get(0).getId();
          uiAccSetting.setSelectedAccountId(newSelectedAcc);
          uiSelectAccount.updateAccount();
          if (removedAccId.equals(dataCache.getSelectedAccountId()))
            uiSelectAccount.setSelectedValue(newSelectedAcc);
          String defaultAcc = mailSetting.getDefaultAccount();
          if (removedAccId.equals(defaultAcc)) {
            mailSetting.setDefaultAccount(newSelectedAcc);
            mailSvr.saveMailSetting(username, mailSetting);
          }
          uiAccSetting.fillField();
          uiMsgList.setMessageFilter(null);
          uiMsgList.init(newSelectedAcc);
          uiMsgPreview.setMessage(null);
          event.getRequestContext().addUIComponentToUpdateByAjax(uiAccSetting.getAncestorOfType(UIPopupActionContainer.class));
        } else {
          uiSelectAccount.updateAccount();
          uiSelectAccount.setSelectedValue(null);
          mailSetting.setDefaultAccount(null);
          mailSvr.saveMailSetting(username, mailSetting);
          event.getSource().getAncestorOfType(UIMailPortlet.class).cancelAction();
          uiMsgList.init(null);
          uiMsgPreview.setMessage(null);
        }
        event.getRequestContext().addUIComponentToUpdateByAjax(uiSelectAccount);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiMsgList.getAncestorOfType(UIMessageArea.class));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiSelectAccount.getAncestorOfType(UINavigationContainer.class));
      } catch (Exception e) {
        if (log.isDebugEnabled()) {
          log.debug("Exception in method execute of class DeleteAccountActionListener", e);
        }
      }
    }
  }
  
  static public class SaveActionListener extends EventListener<UIAccountSetting> {
    public void execute(Event<UIAccountSetting> event) throws Exception {
      UIAccountSetting uiSetting = event.getSource();
      UIMailPortlet uiPortlet = uiSetting.getAncestorOfType(UIMailPortlet.class);
      DataCache dataCache = uiPortlet.getDataCache();      
      MailService mailSrv = uiSetting.getApplicationComponent(MailService.class);
      String username = Util.getPortalRequestContext().getRemoteUser();
      String editedAccountId = uiSetting.getSelectedAccountId();
      Account acc = dataCache.getAccountById(username, editedAccountId);
      
      String userName = uiSetting.getFieldIncomingAccount();
      String email = uiSetting.getFieldMailAddress();
      String reply = uiSetting.getFieldReplyAddress();
      String incomingPort = uiSetting.getFieldIncomingPort();
      String outgoingPort = uiSetting.getFieldOutgoingPort();
      String password = uiSetting.getFieldIncomingPassword();
      String secureAuthIncoming = uiSetting.getFieldSecureAuthInComing();
      String secureAuthOutgoing = uiSetting.getFieldSecureAuthOutgoing();
      String secureAuthMechIncoming = uiSetting.getFieldAuthMechInComing();
      String secureAuthMechOutgoing = uiSetting.getFieldAuthMechOutgoing();

      if (!MailUtils.isValidEmailAddresses(email)) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIAccountSetting.msg.email-address-is-invalid", null, ApplicationMessage.WARNING));
        return;
      }
      
      if (!MailUtils.isFieldEmpty(reply) && !MailUtils.isValidEmailAddresses(reply)) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIAccountSetting.msg.reply-address-is-invalid", null, ApplicationMessage.WARNING));
        return;
      }
      
      if (!Utils.isNumber(incomingPort)) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIAccountSetting.msg.incoming-port-is-not-number", null, ApplicationMessage.WARNING));
        return;
      }
      
      if (!Utils.isNumber(outgoingPort)) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIAccountSetting.msg.outgoing-port-is-not-number", null, ApplicationMessage.WARNING));
        return;
      }

      if (MailUtils.isFieldEmpty(password)) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIAccountSetting.msg.field-password-is-required", null, ApplicationMessage.WARNING));
        return;
      }

      acc.setProtocol(uiSetting.getFieldProtocol());
      acc.setLabel(uiSetting.getFieldAccountNameValue());
      acc.setUserDisplayName(uiSetting.getDisplayName());
      acc.setEmailAddress(email);
      acc.setEmailReplyAddress(reply);
      acc.setSignature(uiSetting.getFieldMailSignature());
      acc.setCheckedAuto(uiSetting.getFieldCheckMailAuto());
      acc.setIncomingUser(userName);
      if (uiSetting.isSavePassword()) {
        acc.setIncomingPassword(password);
      } else {
        acc.setIncomingPassword("");
      }
      acc.setIncomingHost(uiSetting.getFieldIncomingServer());
      acc.setIncomingPort(incomingPort);
      acc.setIncomingSsl(uiSetting.getFieldIsSSL());
      if (uiSetting.getFieldIsSSL()) {
        acc.setSecureAuthsIncoming(secureAuthIncoming);
        acc.setAuthMechsIncoming(secureAuthMechIncoming);
      }
      acc.setOutgoingSsl(uiSetting.getFieldOutgoingSSL());
      if (uiSetting.getFieldOutgoingSSL()) {
        acc.setSecureAuthsOutgoing(secureAuthOutgoing);
        acc.setAuthMechsOutgoing(secureAuthMechOutgoing);
      }
      acc.setIncomingFolder(uiSetting.getFieldIncomingFolder());
      acc.setOutgoingHost(uiSetting.getFieldOutgoingServer());
      acc.setOutgoingPort(outgoingPort);
      acc.setIsOutgoingAuthentication(uiSetting.isOutgoingAuthen());
      acc.setUseIncomingForAuthentication(uiSetting.useIncomingSettingForOutgoingAuthen());
      if (!uiSetting.useIncomingSettingForOutgoingAuthen()) {
        acc.setOutgoingUserName(uiSetting.getOutgoingUser());
        acc.setOutgoingPassword(uiSetting.getOutgoingPassword());
      }
      acc.setIsSavePassword(uiSetting.isSavePassword());
      acc.setServerProperty(Utils.SVR_SMTP_USER, userName);
      acc.setIsCustomInbox(uiSetting.isCustomInbox());

      if (acc.getProtocol().equals(Utils.IMAP)) {
        acc.setCheckAll(!uiSetting.getFieldCheckFromDate());
        if (uiSetting.getFieldCheckFrom() != null) {
          acc.setCheckFromDate(uiSetting.getFieldCheckFrom().getTime());
        } else if (!acc.isCheckAll()) {
          event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIAccountSetting.msg.please-choose-specified-date", null));
          return;
        }
        if (acc.isCheckAll()) {
          acc.setCheckFromDate(null);
        }
      }
      if (Utils.isUserAllowedLeaveOnServer()) {
        boolean leaveOnServer = uiSetting.getFieldLeaveOnServer();
        acc.setServerProperty(Utils.SVR_LEAVE_ON_SERVER, String.valueOf(leaveOnServer));
      }
      try {
        mailSrv.updateAccount(username, acc);
        UISelectAccount uiSelectAccount = uiPortlet.findFirstComponentOfType(UISelectAccount.class);
        String accountId = dataCache.getSelectedAccountId();
        uiSelectAccount.updateAccount();
        uiSelectAccount.setSelectedValue(accountId);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiSelectAccount);

        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIAccountSetting.msg.edit-acc-successfully", null));
        event.getSource().getAncestorOfType(UIMailPortlet.class).cancelAction();
        
      } catch (Exception e) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIAccountSetting.msg.edit-acc-unsuccessfully", null));
        if (log.isDebugEnabled()) {
          log.debug("Exception in method execute of class SaveActionListener", e);
        }
        return;
      }
    }
  }
  
  static public class ChangeOutgoingSSLActionListener extends EventListener<UIAccountSetting> {
    public void execute(Event<UIAccountSetting> event) throws Exception {
      UIAccountSetting uiSetting = event.getSource() ;
      boolean isOutgoingSsl = uiSetting.getFieldOutgoingSSL();
      if (isOutgoingSsl) {
        uiSetting.getUIStringInput(FIELD_OUTGOING_PORT).setValue(UIAccountCreation.DEFAULT_SMTPSSL_PORT) ;
      } else {
        uiSetting.getUIStringInput(FIELD_OUTGOING_PORT).setValue(UIAccountCreation.DEFAULT_SMTP_PORT) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSetting.getParent()) ;
    }
  }
  
  static  public class CheckFromDateActionListener extends EventListener<UIAccountSetting> {
    public void execute(Event<UIAccountSetting> event) throws Exception {
      UIAccountSetting uiSetting = event.getSource() ;
      UIFetchOptionsInputSet uiInput = uiSetting.getChildById(TAB_FETCH_OPTIONS);
      boolean checkAllMail = !uiInput.getUIFormCheckBoxInput(CHECK_FROM_DATE).isChecked();
      UIFormDateTimePicker fromDateField = ((UIFormDateTimePicker) uiInput.getChildById(FROM_DATE));
      
      if (checkAllMail) {
        fromDateField.setEditable(false);
      } else {
        GregorianCalendar cal = new GregorianCalendar();
        fromDateField.setEditable(true);
        if (fromDateField.getCalendar() == null) fromDateField.setCalendar(cal);
      }
      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSetting) ;
    }
  }
  
  static  public class IsCustomInboxActionListener extends EventListener<UIAccountSetting> {
    public void execute(Event<UIAccountSetting> event) throws Exception {
      UIAccountSetting uiSetting = event.getSource() ;
      UIFetchOptionsInputSet uiInput = uiSetting.getChildById(TAB_FETCH_OPTIONS);
      if (uiSetting.isCustomInbox()) {
        ((UIFormStringInput) uiInput.getChildById(FIELD_INCOMING_FOLDER)).setEnable(true);
      } else {
        ((UIFormStringInput) uiInput.getChildById(FIELD_INCOMING_FOLDER)).setEnable(false);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSetting) ;
    }
  }
   
  static  public class CancelActionListener extends EventListener<UIAccountSetting> {
    public void execute(Event<UIAccountSetting> event) throws Exception {
      event.getSource().getAncestorOfType(UIMailPortlet.class).cancelAction();
    }
  }
  
  private List<SelectItemOption<String>> getSecureAuthsValues(){
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    options.add(new SelectItemOption<String>(Utils.TLS_SSL.toUpperCase(), Utils.TLS_SSL));
    options.add(new SelectItemOption<String>(Utils.STARTTLS.toUpperCase(), Utils.STARTTLS)) ;
    return options ;
  }
 
  private List<SelectItemOption<String>> getAuthMechanismsValues(){
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    for(int i=0; i<Utils.MECHANISM.length; i++){
      options.add(new SelectItemOption<String>(Utils.MECHANISM[i].toUpperCase(), Utils.MECHANISM[i]));
    }
    
    return options ;
  }
  
  public String getFieldSecureAuthInComing() {
    UIIncomingInputSet uiInput = getChildById(TAB_INCOMING);
    return uiInput.getUIFormSelectBox(FIELD_SECURE_AUTHENTICATION_INCOMING).getValue();
  }
  
  public String getFieldSecureAuthOutgoing() {
    UIOutgoingInputSet uiOutput = getChildById(TAB_OUTGOING);
    return uiOutput.getUIFormSelectBox(FIELD_SECURE_AUTHENTICATION_OUTGOING).getValue();
  }
  
  public String getFieldAuthMechInComing() {
    UIIncomingInputSet uiInput = getChildById(TAB_INCOMING);
    return uiInput.getUIFormSelectBox(FIELD_AUTHENTICATIONS_MECHANISM).getValue();
  }
  
  public String getFieldAuthMechOutgoing() {
    UIOutgoingInputSet uiOutput = getChildById(TAB_OUTGOING);
    return uiOutput.getUIFormSelectBox(FIELD_AUTHENTICATIONS_MECHANISM_OUTGOING).getValue();
  }
}