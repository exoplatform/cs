/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui.popup;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.EmptyFieldValidator;

/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trongtt@gmail.com
 * Jun 28, 2006
 */
@ComponentConfig(template = "app:/templates/contact/webui/popup/UIFormInputSetWithAction.gtmpl")

public class UIPermissionInputSet extends UIFormInputSetWithAction {
  final static public String FIELD_EDITPERMISSION = "editPermission" ;
  public static String[] FIELD_SHAREDCONTACT_BOX = null ;
  public static String FIELD_INPUT_INFO = "selectGroups";
  
  public UIPermissionInputSet(String name) throws Exception {
    super(name); 
    setComponentConfig(getClass(), null) ;
    addUIFormInput(new UIFormInputInfo(FIELD_INPUT_INFO, FIELD_INPUT_INFO, null)) ;
    String[] groups = ContactUtils.getUserGroups() ;
    FIELD_SHAREDCONTACT_BOX = new String[groups.length];
    for(int i = 0; i < groups.length; i ++) {
      FIELD_SHAREDCONTACT_BOX[i] = groups[i] ;
      addUIFormInput(
          new UIFormCheckBoxInput<Boolean>(FIELD_SHAREDCONTACT_BOX[i], FIELD_SHAREDCONTACT_BOX[i], false));
    } 
    UIFormStringInput userGroup = new UIFormStringInput(FIELD_EDITPERMISSION, FIELD_EDITPERMISSION, null) ;
    userGroup.addValidator(EmptyFieldValidator.class) ;   
    userGroup.setEditable(false) ;
    addUIFormInput(userGroup) ;
    setActionInfo(FIELD_EDITPERMISSION, new String[] { "SelectUser", "SelectMember" }) ;
  }

  
  public String getCheckedSharedGroup() {
    StringBuffer sharedGroups = new StringBuffer("");
    for (int i = 0; i < FIELD_SHAREDCONTACT_BOX.length; i ++) {
      if (getUIFormCheckBoxInput(FIELD_SHAREDCONTACT_BOX[i]).isChecked())
        sharedGroups.append(FIELD_SHAREDCONTACT_BOX[i] + ",");
    }
    return sharedGroups.toString() ;
  }
  
}