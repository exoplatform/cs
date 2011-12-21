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

import org.exoplatform.mail.DataCache;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.WizardStep;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormSelectBox;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Aug 31, 2007  
 */

public class UIAccountWizardStepIntro extends UIFormInputSet implements WizardStep {
  public static final String ITEM_ADDNEW = "true" ;
  public static final String ITEM_EDIT = "false" ;
  public static final String FIELD_SELECT = "selectActions" ;
  public static final String FIELD_ACCOUNTS = "acoounts" ;
  public boolean isValid_ = false ;
  private List<String> infoMessage_ = new ArrayList<String>() ;

  public UIAccountWizardStepIntro(String id) throws Exception {
    setId(id) ;
    List<SelectItemOption<String>> folderOptions = new ArrayList<SelectItemOption<String>>() ;
    folderOptions.add(new SelectItemOption<String>(ITEM_ADDNEW, ITEM_ADDNEW)) ;
    folderOptions.add(new SelectItemOption<String>(ITEM_EDIT, ITEM_EDIT)) ;
    addUIFormInput(new UIFormSelectBox(FIELD_SELECT, FIELD_SELECT, folderOptions)) ;
    UIFormSelectBox uiSelect = getUIFormSelectBox(FIELD_SELECT) ;
    uiSelect.setOnChange(UIAccountCreation.ACT_CHANGE_ACT) ;
    addUIFormInput(new UIFormSelectBox(FIELD_ACCOUNTS, FIELD_ACCOUNTS, getAccounts())) ;
    infoMessage_.clear() ;
    infoMessage_.add("UIAccountWizardStepIntro.info.label1") ;
    infoMessage_.add("UIAccountWizardStepIntro.info.label2") ;
  }
  public List<String> getInfoMessage() {
    return infoMessage_ ;
  } 

  public List<SelectItemOption<String>> getAccounts() throws Exception {
    DataCache dataCache = (DataCache) WebuiRequestContext.getCurrentInstance().getAttribute(DataCache.class);
    
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
    if (!isCreateNew()) {
      String username = Util.getPortalRequestContext().getRemoteUser();
      for (Account acc : dataCache.getAccounts(username)) {
        options.add(new SelectItemOption<String>(acc.getUserDisplayName(), acc.getId()));
      }
    }
    return options;
  }

  protected void lockFields(boolean isLock) {
    boolean isEditable = !isLock ;
    getUIFormSelectBox(FIELD_SELECT).setEnable(isEditable) ;
    getUIFormSelectBox(FIELD_ACCOUNTS).setEnable(isEditable) ;
  }
  protected void resetFields(){
    reset() ;
  }

  public boolean isFieldsValid() {
    return !Utils.isEmptyField(getSelectedAccount()) || isCreateNew() ;
  }
  protected void fieldsValid(boolean isValid) {
    isValid_ = isValid ;
  }
  protected void setAccounts(List<SelectItemOption<String>> options) {
    getUIFormSelectBox(FIELD_ACCOUNTS).setOptions(options) ;
  }
  protected String getSelectedAccount() {
    return getUIFormSelectBox(FIELD_ACCOUNTS).getValue() ;
  }
  protected void setSelectedAccount(String value){
    getUIFormSelectBox(FIELD_ACCOUNTS).setValue(value) ;
  }

  protected boolean isCreateNew() {return getSelectType().equals(ITEM_ADDNEW) ;}
  protected String getSelectType() {
    return getUIFormSelectBox(FIELD_SELECT).getValue() ;
  }
  public void fillFields(Account acc) { }
}
