/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.mail.webui.Utils;
import org.exoplatform.mail.webui.WizardStep;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Aug 16, 2007  
 */

public class UIAccountWizardStep4 extends UIFormInputSet implements WizardStep {
  public static final String FIELD_USERNAME = "userName" ;
  public static final String FIELD_PASSWORD = "password" ;
  public static final String FIELD_SAVEPASSWORD = "savePassword" ;
  
  private List<String> infoMessage_ = new ArrayList<String>() ;
  private boolean isValid_ = false ;
  public UIAccountWizardStep4(String id) throws Exception {
    setId(id) ;
    setComponentConfig(getClass(), null) ; 
    addUIFormInput(new UIFormStringInput(FIELD_USERNAME, null, null)) ;
    addUIFormInput(new UIFormStringInput(FIELD_PASSWORD, null, null).setType(UIFormStringInput.PASSWORD_TYPE)) ;
    UIFormCheckBoxInput isSavePassField = new UIFormCheckBoxInput<Boolean>(FIELD_SAVEPASSWORD, FIELD_SAVEPASSWORD, null) ;
    isSavePassField.setChecked(true) ;
    addChild(isSavePassField) ;
    infoMessage_.clear() ;
    infoMessage_.add("UIAccountWizardStep4.info.label1") ;
  }

  protected void lockFields(boolean isLock) {
    boolean isEditable = !isLock ;
    getUIStringInput(FIELD_USERNAME).setEditable(isEditable) ;
    getUIStringInput(FIELD_PASSWORD).setEditable(isEditable) ;
    getUIFormCheckBoxInput(FIELD_SAVEPASSWORD).setEnable(isEditable) ;
  }
  protected void resetFields(){
    reset() ;
    UIFormCheckBoxInput savePassField = getUIFormCheckBoxInput(FIELD_SAVEPASSWORD) ;
    savePassField.setChecked(true) ;
  }
  protected void fillFields(String userName, String password){
    setUserName(userName) ;
    setPassword(password) ;
  }
  public boolean isFieldsValid() {
    return !(Utils.isEmptyField(getUserName()) || Utils.isEmptyField(getPassword())) ;
    //return isValid_ ;
  }
  protected void fieldsValid(boolean isValid) {
    isValid_ = isValid ;
  }
  public List<String> getInfoMessage() {
    return infoMessage_;
  }
  protected String getUserName() {
    return getUIStringInput(FIELD_USERNAME).getValue() ;
  }
  protected void setUserName(String value) {
    getUIStringInput(FIELD_USERNAME).setValue(value) ;
  }
  
  protected String getPassword() {
    return getUIStringInput(FIELD_PASSWORD).getValue() ;
  }
  protected void setPassword(String value) {
    getUIStringInput(FIELD_PASSWORD).setValue(value) ;
  }
  
  protected boolean getIsSavePass() {
    return getUIFormCheckBoxInput(FIELD_SAVEPASSWORD).isChecked() ;
  }
  protected void setIsSavePass(boolean value) {
    getUIFormCheckBoxInput(FIELD_SAVEPASSWORD).setChecked(value) ;
  }

}
