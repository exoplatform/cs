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

import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.UIFormDateTimePicker;
import org.exoplatform.mail.webui.WizardStep;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;

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
  public static final String CHECK_FROM_DATE = "checkFromDate";
  public static final String FROM_DATE = "fromDate";

  private List<String> infoMessage_ = new ArrayList<String>() ;
  public boolean isValid_ = false ;
  public UIAccountWizardStep4(String id) throws Exception {
    setId(id) ;
    setComponentConfig(getClass(), null) ; 
    addUIFormInput(new UIFormStringInput(FIELD_USERNAME, null, null).addValidator(MandatoryValidator.class)) ;
    addUIFormInput(new UIFormStringInput(FIELD_PASSWORD, null, null).setType(UIFormStringInput.PASSWORD_TYPE).addValidator(MandatoryValidator.class)) ;
    UIFormCheckBoxInput isSavePassField = new UIFormCheckBoxInput<Boolean>(FIELD_SAVEPASSWORD, FIELD_SAVEPASSWORD, null) ;
    isSavePassField.setChecked(true) ;
    addChild(isSavePassField) ;
    UIFormCheckBoxInput checkFromDate = new UIFormCheckBoxInput<Boolean>(CHECK_FROM_DATE, CHECK_FROM_DATE, null).setChecked(true) ;
    checkFromDate.setOnChange("CheckFromDate"); 
    addChild(checkFromDate) ;
    UIFormDateTimePicker uiFromDate = new UIFormDateTimePicker(FROM_DATE, FROM_DATE, null, true) ;
    Calendar sc = GregorianCalendar.getInstance();
    uiFromDate.setCalendar(sc);
    addUIFormInput(uiFromDate) ;   
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
    UIApplication uiApp = getAncestorOfType(UIApplication.class) ;
    boolean isValid = true ;
    if (Utils.isEmptyField(getUserName())) {
      uiApp.addMessage(new ApplicationMessage("UIAccountCreation.msg.username-requirement", null, ApplicationMessage.WARNING)) ;
      isValid = false ;
    } 
    if (Utils.isEmptyField(getPassword())) {
      uiApp.addMessage(new ApplicationMessage("UIAccountCreation.msg.password-requirement", null, ApplicationMessage.WARNING)) ;
      isValid = false ;
    } 
    
    return isValid ;
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
  
  protected boolean checkFromDate() throws Exception {
    return getUIFormCheckBoxInput(CHECK_FROM_DATE).isChecked() ;
  }

  public void fillFields(Account acc) {
    fillFields(acc.getIncomingUser(), acc.getIncomingPassword()) ;
  }

}
