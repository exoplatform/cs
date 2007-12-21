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
package org.exoplatform.contact.webui.popup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.SessionsUtils;
import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.contact.webui.UIAddressBooks;
import org.exoplatform.contact.webui.UIContactPortlet;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UISharedForm.SaveActionListener.class),    
      @EventConfig(listeners = UISharedForm.SelectPermissionActionListener.class, phase = Phase.DECODE),  
      @EventConfig(listeners = UISharedForm.CancelActionListener.class, phase = Phase.DECODE)
    }
)
public class UISharedForm extends UIForm implements UIPopupComponent, UISelector{
  final static public String FIELD_NAME = "addressName".intern() ;
  final static public String FIELD_USER = "username".intern() ;
  //final static public String FIELD_EDIT = "canEdit".intern() ;
  private Map<String, String> permission_ = new HashMap<String, String>() ;
  private String addressId_ ;
  //private boolean isAddNew_ = true ;
  public UISharedForm() throws Exception{
    UIFormInputWithActions inputset = new UIFormInputWithActions("UIInputUserSelect") ;
    inputset.addChild(new UIFormInputInfo(FIELD_NAME, FIELD_NAME, null)) ;
    inputset.addUIFormInput(new UIFormStringInput(FIELD_USER, FIELD_USER, null)) ;
    List<ActionData> actions = new ArrayList<ActionData>() ;

    ActionData selectUserAction = new ActionData() ;
    selectUserAction.setActionListener("SelectPermission") ;
    selectUserAction.setActionName("SelectUser") ;
    selectUserAction.setCssIconClass("SelectUserIcon") ;
    selectUserAction.setActionType(ActionData.TYPE_ICON) ;
    selectUserAction.setActionParameter(UISelectComponent.TYPE_USER) ;
    actions.add(selectUserAction) ;

    inputset.setActionField(FIELD_USER, actions) ;
    //inputset.addChild(new UIFormCheckBoxInput<Boolean>(FIELD_EDIT, FIELD_EDIT, null)) ;
    addChild(inputset) ;
  }

  public void init(String username, ContactGroup contactGroup, boolean isAddNew) {
    //isAddNew_ = isAddNew ;
    addressId_ = contactGroup.getId() ;
    setAddressName(contactGroup.getName()) ;
        
    /*
    boolean canEdit = false ;
    if(cal.getEditPermission() != null) {
      for(String editPerm : cal.getEditPermission() ) {
        if(editPerm.equals(username)) {
          canEdit = true ;
          break ;
        }
      }
    }
    */
    //setCanEdit(canEdit) ;
  }
  public String getLabel(String id) {
    try {
      return super.getLabel(id) ;
    } catch (Exception e) {
      return id ;
    }
  }
  public void setSelectedAddressId(String id) { addressId_ = id ;}
  public void setAddressName(String value) {
    getUIFormInputInfo(FIELD_NAME).setValue(value) ;
  }
  /*protected void setCanEdit(boolean canEdit) {
    getUIFormCheckBoxInput(FIELD_EDIT).setChecked(canEdit) ;
  }
  protected boolean canEdit() {
    return getUIFormCheckBoxInput(FIELD_EDIT).isChecked() ;
  }*/
  protected void setSharedUser(String value) {
    getUIStringInput(FIELD_NAME).setValue(value) ;
  }
  public String[] getActions() {
    return new String[] {"Save","Cancel"} ;
  }
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}

  public void updateSelect(String selectField, String value) throws Exception {
    UIFormStringInput fieldInput = getUIStringInput(selectField) ;
    permission_.put(value, value) ;
    StringBuilder sb = new StringBuilder() ;
    for(String s : permission_.values()) {
      if(sb != null && sb.length() > 0) sb.append(", ") ;
      sb.append(s) ;
    }
    fieldInput.setValue(sb.toString()) ;
  }  
  static  public class SaveActionListener extends EventListener<UISharedForm> {
    public void execute(Event<UISharedForm> event) throws Exception {
      UISharedForm uiForm = event.getSource() ;
      String names = uiForm.getUIStringInput(FIELD_USER).getValue() ;
      if(names == null || names.length() < 1) {
        UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UISharedForm.msg.invalid-username", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      List<String> receiverUser = new ArrayList<String>() ;
      if(names.indexOf(",") > 0) {
        String[] array = names.split(",") ;
        for(String name : array) {
          receiverUser.add(name.trim()) ;
        }
      }else {
        receiverUser.add(names.trim()) ;
      }   
      /*
      if(uiForm.canEdit()) {
        ContactGroup contactGroup = contactService.getGroup(
            SessionsUtils.getSessionProvider(), ContactUtils.getCurrentUser(), uiForm.addressId_) ;
        String[] perms =  new String[]{} ;
        receiverUser.toArray(perms) ;
        //contactGroup.setEditPermission(perms) ;
        contactService.saveGroup(
          SessionsUtils.getSessionProvider(), ContactUtils.getCurrentUser(), contactGroup, false) ;
      }*/
      ContactUtils.getContactService().shareAddressBook(SessionsUtils
          .getSystemProvider(), ContactUtils.getCurrentUser(), uiForm.addressId_, receiverUser) ;
      UIContactPortlet contactPortlet = uiForm.getAncestorOfType(UIContactPortlet.class) ;
      UIAddressBooks addressBooks = contactPortlet.findFirstComponentOfType(UIAddressBooks.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(addressBooks) ;
      contactPortlet.cancelAction() ;       
    }
  }
  static  public class SelectPermissionActionListener extends EventListener<UISharedForm> {
    public void execute(Event<UISharedForm> event) throws Exception {
      UISharedForm uiForm = event.getSource() ;
      String permType = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIPopupAction childPopup = uiForm.getAncestorOfType(UIPopupContainer.class).getChild(UIPopupAction.class) ;
      UIGroupSelector uiGroupSelector = childPopup.activate(UIGroupSelector.class, 500) ;
      uiGroupSelector.setType(permType) ;
      uiGroupSelector.setSelectedGroups(null) ;
      uiGroupSelector.setComponent(uiForm, new String[]{UISharedForm.FIELD_USER}) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(childPopup) ;
    }
  }
  static  public class CancelActionListener extends EventListener<UISharedForm> {
    public void execute(Event<UISharedForm> event) throws Exception {
      UISharedForm uiForm = event.getSource() ;
      UIContactPortlet contactPortlet = uiForm.getAncestorOfType(UIContactPortlet.class) ;
      contactPortlet.cancelAction() ;
    }
  }

}
