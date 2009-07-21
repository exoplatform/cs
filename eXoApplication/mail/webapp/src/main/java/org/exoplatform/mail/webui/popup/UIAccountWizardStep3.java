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

import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.WizardStep;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan <tuan.pham@exoplatform.com>
 *          Phung Nam <phunghainam@gmail.com>
 * Aug 16, 2007  
 */

public class UIAccountWizardStep3 extends UIFormInputSet implements WizardStep{
  
  public static final String FIELD_SERVERTYPE = "serverType" ;
  public static final String FIELD_INCOMING_SERVER = "incomingServer" ;
  public static final String FIELD_INCOMINGPORT = "incomingPort" ;
  public static final String FIELD_OUTGOING_SERVER = "outgoingServer" ;
  public static final String FIELD_OUTGOINGPORT = "outgoingPort" ;
  public static final String FIELD_USESSL = "isSsl".intern() ;
  public static final String FIELD_OUTGOING_SSL = "outgoingSsl".intern();
  public static final String FIELD_STOREFOLDER = "storeFolder" ;
  
  public boolean isValid_ = false ;
  
  private List<String> infoMessage_ = new ArrayList<String>() ;
  
  public UIAccountWizardStep3(String id) throws Exception {
    super(id) ;
    setComponentConfig(getClass(), null) ; 
    addChild(new UIFormSelectBox(FIELD_SERVERTYPE, null, getServerTypeValues())) ;
    UIFormSelectBox uiSelect = getUIFormSelectBox(FIELD_SERVERTYPE) ;
    uiSelect.setOnChange(UIAccountCreation.ACT_CHANGE_TYPE) ;
    addChild(new UIFormStringInput(FIELD_INCOMING_SERVER, null, null).addValidator(MandatoryValidator.class)) ;
    addChild(new UIFormStringInput(FIELD_INCOMINGPORT, null, null).addValidator(MandatoryValidator.class)) ;
    addChild(new UIFormCheckBoxInput<Boolean>(FIELD_USESSL, null,null)) ;
    UIFormCheckBoxInput uiCheckBox = getUIFormCheckBoxInput(FIELD_USESSL) ;
    uiCheckBox.setOnChange(UIAccountCreation.ACT_CHANGE_SSL) ;
    addChild(new UIFormStringInput(FIELD_OUTGOING_SERVER, null, null).addValidator(MandatoryValidator.class)) ;
    addChild(new UIFormStringInput(FIELD_OUTGOINGPORT, null, null).addValidator(MandatoryValidator.class)) ;
    addChild(new UIFormCheckBoxInput<Boolean>(FIELD_OUTGOING_SSL, null,null)) ;
    UIFormCheckBoxInput outgoingSsl = getUIFormCheckBoxInput(FIELD_OUTGOING_SSL) ;
    outgoingSsl.setOnChange(UIAccountCreation.ACT_CHANGE_OUTGOINGSSL) ;
    addChild(new UIFormStringInput(FIELD_STOREFOLDER, null,null).addValidator(MandatoryValidator.class)) ;
    setDefaultValue(uiSelect.getValue(), uiCheckBox.isChecked()) ;
    resetFields() ;
    infoMessage_.clear() ;
    infoMessage_.add("UIAccountWizardStep3.info.label1") ;
    infoMessage_.add("UIAccountWizardStep3.info.label2") ;
    infoMessage_.add("UIAccountWizardStep3.info.label3") ;
  }
  public List<String> getInfoMessage() {
    return infoMessage_ ;
  } 
  
  protected void setDefaultValue(String serverType, boolean isSSL) {
    getUIStringInput(FIELD_INCOMINGPORT).setRendered(false);
    getUIStringInput(FIELD_OUTGOINGPORT).setRendered(false);
    getUIStringInput(FIELD_STOREFOLDER).setRendered(false);
    if(serverType.equals(Utils.POP3)) {
      getUIStringInput(FIELD_INCOMING_SERVER).setValue(UIAccountCreation.DEFAULT_POP_SERVER) ;
      if(isSSL) {
        getUIStringInput(FIELD_INCOMINGPORT).setValue(UIAccountCreation.DEFAULT_POPSSL_PORT) ;
      } else {
        getUIStringInput(FIELD_INCOMINGPORT).setValue(UIAccountCreation.DEFAULT_POP_PORT) ;
      }
    } else {
      getUIStringInput(FIELD_INCOMING_SERVER).setValue(UIAccountCreation.DEFAULT_IMAP_SERVER) ;
      if(isSSL) {
        getUIStringInput(FIELD_INCOMINGPORT).setValue(UIAccountCreation.DEFAULT_IMAPSSL_PORT) ;
      } else {
        getUIStringInput(FIELD_INCOMINGPORT).setValue(UIAccountCreation.DEFAULT_IMAP_PORT) ;
      }
    }
  }

  public boolean isFieldsValid() {
    UIApplication uiApp = getAncestorOfType(UIApplication.class) ;
    boolean isValid = true ;
    if (Utils.isEmptyField(getIncomingServer())) {
      uiApp.addMessage(new ApplicationMessage("UIAccountCreation.msg.incoming-server-requirement", null, ApplicationMessage.WARNING)) ;
      isValid = false ;
    } 
    if (Utils.isEmptyField(getOutgoingServer())) {
      uiApp.addMessage(new ApplicationMessage("UIAccountCreation.msg.outgoing-server-requirement", null, ApplicationMessage.WARNING)) ;
      isValid = false ;
    }
    if (Utils.isEmptyField(getStoreFolder())) {
      uiApp.addMessage(new ApplicationMessage("UIAccountCreation.msg.store-folder-requirement", null, ApplicationMessage.WARNING)) ;
      isValid = false ;
    }
    if (!Utils.isNumber(getIncomingPort())) {
      uiApp.addMessage(new ApplicationMessage("UIAccountCreation.msg.incoming-port-is-not-number", null, ApplicationMessage.WARNING)) ;
      isValid = false ;
    }
    if (!Utils.isNumber(getOutgoingPort())) {
      uiApp.addMessage(new ApplicationMessage("UIAccountCreation.msg.outgoing-port-is-not-number", null, ApplicationMessage.WARNING)) ;
      isValid = false ;
    }
    
    return isValid ;
  }
  
  protected void fieldsValid(boolean isValid) {
    isValid_ = isValid ;
  }
  
  protected void lockFields(boolean isLock){
    boolean isEditable = !isLock ;
    getUIFormSelectBox(FIELD_SERVERTYPE).setEnable(isEditable) ;
    getUIStringInput(FIELD_INCOMING_SERVER).setEditable(isEditable) ;
    getUIStringInput(FIELD_OUTGOING_SERVER).setEditable(isEditable) ;
    getUIStringInput(FIELD_STOREFOLDER).setEditable(isEditable) ;
  }
  
  protected void resetFields(){
    reset() ;
    setIncomingServer(UIAccountCreation.DEFAULT_IMAP_SERVER) ;
    setIncomingPort(UIAccountCreation.DEFAULT_IMAP_PORT) ;
    setOutgoingServer(UIAccountCreation.DEFAULT_SMTP_SERVER) ;
    setOutgoingPort(UIAccountCreation.DEFAULT_SMTP_PORT) ;
    setStoreFolder(UIAccountCreation.DEFAULT_SERVER_FOLDER) ;
  }
  protected void fillFields(String serverType, boolean isSsl, String incomingServer, String popPort,String outgoingServer, String smtpPort, boolean outgoingSsl, String storeFolder){
    setServerType(serverType) ;
    setIncomingServer(incomingServer) ;
    setIncomingPort(popPort) ;
    setOutgoingServer(outgoingServer) ;
    setOutgoingPort(smtpPort) ;
    setOutgoingSsl(outgoingSsl);
    setStoreFolder(storeFolder) ;
    setIsSSL(isSsl) ;
  }
  
  protected String getServerType() {
    return getUIFormSelectBox(FIELD_SERVERTYPE).getValue() ;
  }
  protected void setServerType(String value) {
    getUIFormSelectBox(FIELD_SERVERTYPE).setValue(value) ;
  }

  protected String getIncomingServer() {
    return getUIStringInput(FIELD_INCOMING_SERVER).getValue() ;
  }
  protected void setIncomingServer(String value) {
    getUIStringInput(FIELD_INCOMING_SERVER).setValue(value) ;
  }
  protected String getIncomingPort() {
    return getUIStringInput(FIELD_INCOMINGPORT).getValue() ;
  }
  protected void setIncomingPort(String value) {
    getUIStringInput(FIELD_INCOMINGPORT).setValue(value) ;
  }
  protected String getOutgoingServer() {
    return getUIStringInput(FIELD_OUTGOING_SERVER).getValue() ;
  }
  protected void setOutgoingServer(String value) {
    getUIStringInput(FIELD_OUTGOING_SERVER).setValue(value) ;
  }
  protected String getOutgoingPort() {
    return getUIStringInput(FIELD_OUTGOINGPORT).getValue() ;
  }
  protected boolean getOutgoingSsl() {
	return  getUIFormCheckBoxInput(FIELD_OUTGOING_SSL).isChecked(); 
  }
  protected void setOutgoingPort(String value) {
    getUIStringInput(FIELD_OUTGOINGPORT).setValue(value) ;
  }
  protected boolean getIsSSL() {
    return getUIFormCheckBoxInput(FIELD_USESSL).isChecked() ;
  }
  protected void setIsSSL(boolean value) {
    getUIFormCheckBoxInput(FIELD_USESSL).setChecked(value) ;
  }
  protected void setOutgoingSsl(boolean value) {
	getUIFormCheckBoxInput(FIELD_OUTGOING_SSL).setChecked(value) ;
  }
  protected String getStoreFolder() {
    return getUIStringInput(FIELD_STOREFOLDER).getValue() ;
  }
  protected void setStoreFolder(String value) {
    getUIStringInput(FIELD_STOREFOLDER).setValue(value) ;
  }

  
  private List<SelectItemOption<String>> getServerTypeValues(){
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    options.add(new SelectItemOption<String>(Utils.IMAP, Utils.IMAP)) ;
    options.add(new SelectItemOption<String>(Utils.POP3, Utils.POP3));
    return options ;
  }
  public void fillFields(Account acc) {
    String serverType, incomingServer, popPort, outgoingServer, smtpPort, storeFolder ;
    boolean isSSL = false, isOutgoingSsl = false;
    serverType = acc.getProtocol() ;
    storeFolder = acc.getIncomingFolder() ;
    isSSL = Boolean.parseBoolean(acc.getServerProperties().get(Utils.SVR_INCOMING_SSL)) ;
    incomingServer = acc.getServerProperties().get(Utils.SVR_INCOMING_HOST) ;
    popPort = acc.getServerProperties().get(Utils.SVR_INCOMING_PORT) ;
    outgoingServer = acc.getServerProperties().get(Utils.SVR_SMTP_HOST) ;
    isOutgoingSsl = Boolean.parseBoolean(acc.getServerProperties().get(Utils.SVR_OUTGOING_SSL)) ;
    smtpPort = acc.getServerProperties().get(Utils.SVR_SMTP_PORT) ;
    fillFields(serverType, isSSL, incomingServer, popPort, outgoingServer, smtpPort, isOutgoingSsl, storeFolder) ;
  }
}
