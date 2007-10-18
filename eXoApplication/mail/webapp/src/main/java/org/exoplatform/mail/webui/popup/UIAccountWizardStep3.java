/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.WizardStep;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan <tuan.pham@exoplatform.com>
 *          Phung Nam <phunghainam@gmail.com>
 * Aug 16, 2007  
 */

public class UIAccountWizardStep3 extends UIFormInputSet  implements WizardStep{
  
  public static final String FIELD_SERVERTYPE = "serverType" ;
  public static final String FIELD_INCOMINGSERVER = "incomingServer" ;
  public static final String FIELD_INCOMINGPORT = "incomeingPort" ;
  public static final String FIELD_OUTGOINGSERVER = "outgoingServer" ;
  public static final String FIELD_OUTGOINGPORT = "outgoingPort" ;
  public static final String FIELD_USESSL = "isSsl".intern() ;
  public static final String FIELD_STOREFOLDER = "storeFolder" ;
  
  public static final String DEFAULT_POP_SERVER = "pop.gmail.com".intern() ;
  public static final String DEFAULT_SMTP_SERVER = "smtp.gmail.com".intern() ;
  public static final String DEFAULT_POP_PORT = "110".intern() ;
  public static final String DEFAULT_SMTP_PORT = "25".intern() ;
  public static final String DEFAULT_POPSSL_PORT = "995".intern() ;
  public static final String DEFAULT_SMTPSSL_PORT = "465".intern() ;
  public static final String DEFAULT_SERVER_FOLDER = "Inbox".intern() ;
  public static final String DEFAULT_IMAP_PORT = "143".intern() ;
  public static final String DEFAULT_IMAPSSL_PORT = "993".intern() ;
  
  
  private boolean isValid_ = false ;
  private List<String> infoMessage_ = new ArrayList<String>() ;
  
  public UIAccountWizardStep3(String id){
    super(id) ;
    setComponentConfig(getClass(), null) ; 
    addChild(new UIFormSelectBox(FIELD_SERVERTYPE, null, getServerTypeValues())) ;
    UIFormSelectBox uiSelect = getUIFormSelectBox(FIELD_SERVERTYPE) ;
    uiSelect.setOnChange(UIAccountCreation.ACT_CHANGE_TYPE) ;
    addChild(new UIFormCheckBoxInput<Boolean>(FIELD_USESSL, null,null)) ;
    UIFormCheckBoxInput uiCheckBox = getUIFormCheckBoxInput(FIELD_USESSL) ;
    uiCheckBox.setOnChange(UIAccountCreation.ACT_CHANGE_SSL) ;
    addChild(new UIFormStringInput(FIELD_INCOMINGSERVER, null, null)) ;
    addChild(new UIFormStringInput(FIELD_INCOMINGPORT, null, null)) ;
    addChild(new UIFormStringInput(FIELD_OUTGOINGSERVER, null, null)) ;
    addChild(new UIFormStringInput(FIELD_OUTGOINGPORT, null, null)) ;
    addChild(new UIFormStringInput(FIELD_STOREFOLDER, null,null)) ;
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
    if(serverType.equals(Utils.POP3)) {
      if(isSSL) {
        getUIStringInput(FIELD_INCOMINGPORT).setValue(DEFAULT_POPSSL_PORT) ;
        getUIStringInput(FIELD_OUTGOINGPORT).setValue(DEFAULT_SMTPSSL_PORT) ;
      } else {
        getUIStringInput(FIELD_INCOMINGPORT).setValue(DEFAULT_POP_PORT) ;
        getUIStringInput(FIELD_OUTGOINGPORT).setValue(DEFAULT_SMTP_PORT) ;
      }
    } else {
      if(isSSL) {
        getUIStringInput(FIELD_INCOMINGPORT).setValue(DEFAULT_IMAPSSL_PORT) ;
        getUIStringInput(FIELD_OUTGOINGPORT).setValue(DEFAULT_SMTP_PORT) ;
      } else {
        getUIStringInput(FIELD_INCOMINGPORT).setValue(DEFAULT_IMAP_PORT) ;
        getUIStringInput(FIELD_OUTGOINGPORT).setValue(DEFAULT_SMTP_PORT) ;
      }
    }
  }

  public boolean isFieldsValid() {
    return !(Utils.isEmptyField(getIncomingServer()) 
             || Utils.isEmptyField(getOutgoingServer()) 
             || Utils.isEmptyField(getStoreFolder()))
             && (Utils.isNumber(getIncomingPort()) 
             && Utils.isNumber(getOutgoingPort())) ;
    //return isValid_ ;
  }
  
  protected void fieldsValid(boolean isValid) {
    isValid_ = isValid ;
  }
  
  protected void lockFields(boolean isLock){
    boolean isEditable = !isLock ;
    getUIFormSelectBox(FIELD_SERVERTYPE).setEnable(isEditable) ;
    getUIStringInput(FIELD_INCOMINGSERVER).setEditable(isEditable) ;
    getUIStringInput(FIELD_OUTGOINGSERVER).setEditable(isEditable) ;
    getUIStringInput(FIELD_STOREFOLDER).setEditable(isEditable) ;
  }
  
  protected void resetFields(){
    reset() ;
    setIncomingServer(DEFAULT_POP_SERVER) ;
    setIncomingPort(DEFAULT_POP_PORT) ;
    setOutgoingServer(DEFAULT_SMTP_SERVER) ;
    setOutgoingPort(DEFAULT_SMTP_PORT) ;
    setStoreFolder(DEFAULT_SERVER_FOLDER) ;
  }
  protected void fillFields(String serverType, boolean isSsl, String incomingServer, String popPort,String outgoingServer, String smtpPort, String storeFolder){
    setServerType(serverType) ;
    setIncomingServer(incomingServer) ;
    setIncomingPort(popPort) ;
    setOutgoingServer(outgoingServer) ;
    setOutgoingPort(smtpPort) ;
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
    return getUIStringInput(FIELD_INCOMINGSERVER).getValue() ;
  }
  protected void setIncomingServer(String value) {
    getUIStringInput(FIELD_INCOMINGSERVER).setValue(value) ;
  }
  protected String getIncomingPort() {
    return getUIStringInput(FIELD_INCOMINGPORT).getValue() ;
  }
  protected void setIncomingPort(String value) {
    getUIStringInput(FIELD_INCOMINGPORT).setValue(value) ;
  }
  protected String getOutgoingServer() {
    return getUIStringInput(FIELD_OUTGOINGSERVER).getValue() ;
  }
  protected void setOutgoingServer(String value) {
    getUIStringInput(FIELD_OUTGOINGSERVER).setValue(value) ;
  }
  protected String getOutgoingPort() {
    return getUIStringInput(FIELD_OUTGOINGPORT).getValue() ;
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
  protected String getStoreFolder() {
    return getUIStringInput(FIELD_STOREFOLDER).getValue() ;
  }
  protected void setStoreFolder(String value) {
    getUIStringInput(FIELD_STOREFOLDER).setValue(value) ;
  }

  
  private List<SelectItemOption<String>> getServerTypeValues(){
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    options.add(new SelectItemOption<String>(Utils.POP3, Utils.POP3));
    options.add(new SelectItemOption<String>(Utils.IMAP, Utils.IMAP)) ;
    return options ;
  }
  public void fillFields(Account acc) {
    String serverType, incomingServer, popPort, outgoingServer, smtpPort, storeFolder ;
    boolean isSSL = false ;
    serverType = acc.getProtocol() ;
    storeFolder = acc.getIncomingFolder() ;
    isSSL = Boolean.parseBoolean(acc.getServerProperties().get(Utils.SVR_INCOMING_SSL)) ;
    incomingServer = acc.getServerProperties().get(Utils.SVR_INCOMING_HOST) ;
    popPort = acc.getServerProperties().get(Utils.SVR_INCOMING_PORT) ;
    outgoingServer = acc.getServerProperties().get(Utils.SVR_SMTP_HOST) ;
    smtpPort = acc.getServerProperties().get(Utils.SVR_SMTP_PORT) ;
    fillFields(serverType, isSSL, incomingServer, popPort, outgoingServer, smtpPort, storeFolder) ;
  }
}
