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

import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.WizardStep;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Aug 16, 2007  
 */

public class UIAccountWizardStep2 extends UIFormInputSet implements WizardStep{
  public static final String FIELD_OUTGOINGNAME = "outgoingName" ;
  public static final String FIELD_EMAILADDRESS = "emailAddress" ;
  public static final String FIELD_EMAILREPLY = "emailReply" ;
  public static final String FIELD_SIGNATURE = "signature" ;
  
  public boolean isValid_ = false ;
  private List<String> infoMessage_ = new ArrayList<String>() ;
  
  public UIAccountWizardStep2(String id) throws Exception {
    setId(id) ;
    setComponentConfig(getClass(), null) ; 
    addChild(new UIFormStringInput(FIELD_OUTGOINGNAME, null, null).addValidator(MandatoryValidator.class)) ;
    addChild(new UIFormStringInput(FIELD_EMAILADDRESS, null, null).addValidator(MandatoryValidator.class)) ;
    addChild(new UIFormStringInput(FIELD_EMAILREPLY, null, null)) ;
    addChild(new UIFormTextAreaInput(FIELD_SIGNATURE, null, null)) ;
    infoMessage_.clear() ;
    infoMessage_.add("UIAccountWizardStep2.info.label1") ;
  }
  public List<String> getInfoMessage() {
    return infoMessage_ ;
  } 
  protected void lockFields(boolean isLock){
    boolean isEditable = !isLock ;
    getUIStringInput(FIELD_OUTGOINGNAME).setEditable(isEditable) ;
    getUIStringInput(FIELD_EMAILADDRESS).setEditable(isEditable) ;
    getUIStringInput(FIELD_EMAILREPLY).setEditable(isEditable) ;
  }
  protected void resetFields(){
    reset() ;
  }
  protected void fillFields(String outgoingName, String emailAddress, String emailReply, String signature){
    setOutgoingName(outgoingName);
    setEmailAddress(emailAddress) ;
    setEmailReply(emailReply) ;
    setSignature(signature) ;
  }
  
  public boolean isFieldsValid() {
    boolean isValid = true ;
    WebuiRequestContext context  = WebuiRequestContext.getCurrentInstance();
    try {
      if (Utils.isEmptyField(getOutgoingName())) {
        context.getUIApplication().addMessage(new ApplicationMessage("UIAccountCreation.msg.display-name-requirement", null, ApplicationMessage.WARNING)) ;
        isValid = false ;
      } 
      if (Utils.isEmptyField(getEmailAddress())) {
        context.getUIApplication().addMessage(new ApplicationMessage("UIAccountCreation.msg.email-address-requirement", null, ApplicationMessage.WARNING)) ;
        isValid = false ;
      } else if(!MailUtils.isValidEmailAddresses(getEmailAddress())) {
        context.getUIApplication().addMessage(new ApplicationMessage("UIAccountCreation.msg.email-address-is-invalid", null, ApplicationMessage.WARNING)) ;
        isValid = false ;
      }
      if (!MailUtils.isValidEmailAddresses(getEmailReply())) {
        context.getUIApplication().addMessage(new ApplicationMessage("UIAccountCreation.msg.reply-address-is-invalid", null, ApplicationMessage.WARNING)) ;
        isValid = false ;
      }
    } catch(Exception e) {
      return false ;
    }
    return isValid ;
  }
  protected void fieldsValid(boolean isValid) {
    isValid_ = isValid ;
  }
  
  protected String getOutgoingName() {
    return getUIStringInput(FIELD_OUTGOINGNAME).getValue() ;
  }
  protected void setOutgoingName(String value) {
    getUIStringInput(FIELD_OUTGOINGNAME).setValue(value) ;
  }

  protected String getEmailAddress() {
    return getUIStringInput(FIELD_EMAILADDRESS).getValue() ;
  }
  protected void setEmailAddress(String value) {
    getUIStringInput(FIELD_EMAILADDRESS).setValue(value) ;
  }

  protected String getEmailReply() {
    return getUIStringInput(FIELD_EMAILREPLY).getValue() ;
  }
  protected void setEmailReply(String value) {
    getUIStringInput(FIELD_EMAILREPLY).setValue(value) ;
  }

  protected void setSignature(String value) {
    getUIFormTextAreaInput(FIELD_SIGNATURE).setValue(value) ;
  }
  protected String getSignature() {
    return getUIFormTextAreaInput(FIELD_SIGNATURE).getValue() ;
  }
  public void fillFields(Account acc) {
   fillFields(acc.getUserDisplayName(), acc.getEmailAddress(), acc.getEmailReplyAddress(), acc.getSignature()) ;
  }







}
