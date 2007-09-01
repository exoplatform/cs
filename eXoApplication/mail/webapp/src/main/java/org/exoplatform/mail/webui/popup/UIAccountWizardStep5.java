/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.webui.WizardStep;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormInputSet;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Aug 16, 2007  
 */

public class UIAccountWizardStep5 extends UIFormInputSet implements WizardStep {
  private List<String> infoMessage_ = new ArrayList<String>() ;
  public static final String FIELD_GETMAIL = "getMail" ;
  public UIAccountWizardStep5(String id) {
    setId(id) ;
    setComponentConfig(getClass(), null) ; 
    addChild(new UIFormInputInfo(UIAccountWizardStep1.FIELD_ACCNAME, null, null)) ;
    addChild(new UIFormInputInfo(UIAccountWizardStep2.FIELD_OUTGOINGNAME, null, null)) ;
    addChild(new UIFormInputInfo(UIAccountWizardStep2.FIELD_EMAILADDRESS, null, null)) ;
    addChild(new UIFormInputInfo(UIAccountWizardStep3.FIELD_INCOMINGSERVER, null, null)) ;
    addChild(new UIFormInputInfo(UIAccountWizardStep3.FIELD_SERVERTYPE, null, null)) ;
    addChild(new UIFormInputInfo(UIAccountWizardStep3.FIELD_STOREFOLDER, null, null)) ;
    addChild(new UIFormCheckBoxInput<Boolean>(FIELD_GETMAIL, null, null)) ;
    infoMessage_.clear() ;
    infoMessage_.add("UIAccountWizardStep5.info.label1") ;
  }

  protected void fillFields(String accname, String accOutgoingName, 
      String email, String serverName, String serverType, String storeFolder) {
    setFieldAccName(accname) ;
    setFieldAccName(accOutgoingName) ;
    setFieldAccMail(email) ;
    setFieldAccPOP(serverName) ;
    setFieldAccServerType(serverType) ;
    setFieldAccFolder(storeFolder) ;
  }

  protected boolean isGetmail(){return getUIFormCheckBoxInput(FIELD_GETMAIL).isChecked() ;}
  
  public List<String> getInfoMessage() {
    return infoMessage_;
  }

  public void resetFields() {
    reset() ;
  }
  
  public boolean isFieldsValid() {
    return true;
  }

  protected void setFieldAccName(String value) {
    getUIFormInputInfo(UIAccountWizardStep1.FIELD_ACCNAME).setValue(value) ;
  }
  protected void setFieldAccDisplayName(String value) {
    getUIFormInputInfo(UIAccountWizardStep2.FIELD_OUTGOINGNAME).setValue(value) ;
  }
  protected void setFieldAccMail(String value) {
    getUIFormInputInfo(UIAccountWizardStep2.FIELD_EMAILADDRESS).setValue(value) ;
  }
  protected void setFieldAccPOP(String value) {
    getUIFormInputInfo(UIAccountWizardStep3.FIELD_INCOMINGSERVER).setValue(value) ;
  }
  protected void setFieldAccServerType(String value) {
    getUIFormInputInfo(UIAccountWizardStep3.FIELD_SERVERTYPE).setValue(value) ;
  }
  protected void setFieldAccFolder(String value) {
    getUIFormInputInfo(UIAccountWizardStep3.FIELD_STOREFOLDER).setValue(value) ;
  }

  public void fillFields(Account acc) {
    
  }

}
