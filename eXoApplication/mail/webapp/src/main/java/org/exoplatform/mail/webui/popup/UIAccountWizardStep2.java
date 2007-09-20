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
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;

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
  
  private boolean isValid_ = false ;
  private List<String> infoMessage_ = new ArrayList<String>() ;
  
  public UIAccountWizardStep2(String id) {
    setId(id) ;
    setComponentConfig(getClass(), null) ; 
    addChild(new UIFormStringInput(FIELD_OUTGOINGNAME, null, null)) ;
    addChild(new UIFormStringInput(FIELD_EMAILADDRESS, null, null)) ;
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
   return !(Utils.isEmptyField(getOutgoingName()) || Utils.isEmptyField(getEmailAddress())) ;
    // return isValid_ ;
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
