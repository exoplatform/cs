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
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan <tuan.pham@exoplatform.com>
 *          Phung Nam <phunghainam@gmail.com>
 * Aug 16, 2007  
 */

public class UIAccountWizardStep1 extends UIFormInputSet implements WizardStep {
  public static final String FIELD_ACCNAME = "accountName" ;
  public static final String FIELD_ACCDESCRIPTION = "description" ;
  public boolean isValid_ = false ;
  private List<String> infoMessage_ = new ArrayList<String>() ;
  
  public UIAccountWizardStep1(String id) throws Exception {
    setId(id) ;
    setComponentConfig(getClass(), null) ;  
    addChild(new UIFormStringInput(FIELD_ACCNAME, null, null).addValidator(MandatoryValidator.class)) ;
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
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    if (Utils.isEmptyField(getAccName())) {
      context.getUIApplication().addMessage(new ApplicationMessage("UIAccountCreation.msg.account-name-requirement",
                                                                   null,
                                                                   ApplicationMessage.WARNING));
    }
    return !Utils.isEmptyField(getAccName()) ;
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
    getUIFormTextAreaInput(FIELD_ACCDESCRIPTION).setValue(value) ;
  }
  public void fillFields(Account acc) {
    fillFields(acc.getLabel(), acc.getDescription()) ;
  }
   
}
