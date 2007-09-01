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
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Aug 16, 2007  
 */

public class UIAccountWizardStep1 extends UIFormInputSet implements WizardStep {
  public static final String FIELD_ACCNAME = "accountName" ;
  public static final String FIELD_ACCDESCRIPTION = "description" ;
  private boolean isValid_ = false ;
  private List<String> infoMessage_ = new ArrayList<String>() ;
  
  public UIAccountWizardStep1(String id) {
    setId(id) ;
    setComponentConfig(getClass(), null) ;  
    addChild(new UIFormStringInput(FIELD_ACCNAME, null, null)) ;
    addChild(new UIFormTextAreaInput(FIELD_ACCDESCRIPTION, null, null)) ;
    infoMessage_.clear() ;
    infoMessage_.add("UIAccountWizardStep1.info.label1") ;
    infoMessage_.add("UIAccountWizardStep1.info.label2") ;
  }
  public List<String> getInfoMessage() {
    return infoMessage_ ;
  } 
  
  protected void lockFields(boolean isLock) {
    boolean isEditable = !isLock ;
    getUIStringInput(FIELD_ACCNAME).setEditable(isEditable) ;
    getUIFormTextAreaInput(FIELD_ACCDESCRIPTION).setEditable(isEditable) ;
  }
  protected void resetFields(){
    reset() ;
  }
  protected void fillFields(String accName, String description){
    setAccName(accName) ;
    setAccDescription(description) ;
  }
  public boolean isFieldsValid() {
    return !Utils.isEmptyField(getAccName()) ;
    //return isValid_ ;
  }
  protected void fieldsValid(boolean isValid) {
    isValid_ = isValid ;
  }
  protected String getAccName() {
    return getUIStringInput(FIELD_ACCNAME).getValue() ;
  }
  protected void setAccName(String value){
    getUIStringInput(FIELD_ACCNAME).setValue(value) ;
  }
  protected String getAccDescription() {
    return getUIFormTextAreaInput(FIELD_ACCDESCRIPTION).getValue() ;
  }
  protected void setAccDescription(String value){
    getUIStringInput(FIELD_ACCDESCRIPTION).setValue(value) ;
  }
  public void fillFields(Account acc) {
    fillFields(acc.getLabel(), acc.getDescription()) ;
  }
   
}
