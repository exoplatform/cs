/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui.popup;

import java.util.List;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormMultiValueInputSet;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.EmailAddressValidator;


/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan.nguyen@exoplatform.com
 * Aug 24, 2007  
 */
@ComponentConfig(
    template = "app:/templates/contact/webui/popup/UIProfileInputSet.gtmpl"
)
public class UIProfileInputSet extends UIFormInputSet {
  public static final String FIELD_FULLNAME_INPUT = "fullName";
  public static final String FIELD_FIRSTNAME_INPUT = "firstName";
  public static final String FIELD_MIDDLENAME_INPUT = "middleName";
  public static final String FIELD_LASTNAME_INPUT = "lastName";
  public static final String FIELD_NICKNAME_INPUT = "nickName";
  public static final String FIELD_JOBTITLE_INPUT = "jobTitle";
  public static final String FIELD_EMAIL_INPUT = "preferredEmail" ;
  
  public UIProfileInputSet(String id) throws Exception {
    super(id) ;
    setComponentConfig(getClass(), null) ;  
    addUIFormInput(new UIFormStringInput(FIELD_FULLNAME_INPUT, FIELD_FULLNAME_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_FIRSTNAME_INPUT, FIELD_FIRSTNAME_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_MIDDLENAME_INPUT, FIELD_MIDDLENAME_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_LASTNAME_INPUT, FIELD_LASTNAME_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_NICKNAME_INPUT, FIELD_NICKNAME_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_JOBTITLE_INPUT, FIELD_JOBTITLE_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_EMAIL_INPUT, FIELD_EMAIL_INPUT, null)
    .addValidator(EmailAddressValidator.class));
    
  }
  public List<UIComponent> getChidren(){
    return super.getChildren() ;
  }
  public void processRender(WebuiRequestContext context) throws Exception {
    super.processRender(context) ;
  }
  
  protected String getFieldFullNameValue() {
    return getUIStringInput(FIELD_FULLNAME_INPUT).getValue() ;
  }
  protected String getFieldFirstNameValue() {
    return getUIStringInput(FIELD_FIRSTNAME_INPUT).getValue() ;
  }
  protected String getFieldMiddleNameValue() {
    return getUIStringInput(FIELD_MIDDLENAME_INPUT).getValue() ;
  }
  protected String getFieldLastNameValue() {
    return getUIStringInput(FIELD_LASTNAME_INPUT).getValue() ;
  }
  protected String getFieldNickNameValue() {
    return getUIStringInput(FIELD_NICKNAME_INPUT).getValue() ;
  }
  protected String getFieldJobNameValue() {
    return getUIStringInput(FIELD_JOBTITLE_INPUT).getValue() ;
  }
  protected String getFieldEmailValue() {
    return getUIStringInput(FIELD_EMAIL_INPUT).getValue();
  }
  
  protected String getImageUrl(){
    return null ;
  }
}
