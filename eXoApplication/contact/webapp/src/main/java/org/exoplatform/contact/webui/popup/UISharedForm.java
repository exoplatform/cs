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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.SessionsUtils;
import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.contact.service.ContactService;
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
import org.exoplatform.webui.form.UIFormCheckBoxInput;
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
  final static public String FIELD_ADDRESS = "addressName".intern() ;
  final static public String FIELD_CONTACT = "contactName".intern() ;  
  final static public String FIELD_USER = "user".intern() ;
  final static public String FIELD_GROUP = "group".intern() ;  
  final static public String FIELD_EDIT = "canEdit".intern() ;
  private Map<String, String> permissionUser_ = new LinkedHashMap<String, String>() ;
  private Map<String, String> permissionGroup_ = new LinkedHashMap<String, String>() ;
  private Map<String, String> sharedContacts = new LinkedHashMap<String, String>() ;
  private String addressId_ ;
  public UISharedForm() { }
  
  public void init(boolean isSharedAdd) throws Exception {
    UIFormInputWithActions inputset = new UIFormInputWithActions("UIInputUserSelect") ;
    if (isSharedAdd)
      inputset.addChild(new UIFormInputInfo(FIELD_ADDRESS, FIELD_ADDRESS, null)) ;
    else inputset.addChild(new UIFormInputInfo(FIELD_CONTACT, FIELD_CONTACT, null)) ;
    
    inputset.addUIFormInput(new UIFormStringInput(FIELD_USER, FIELD_USER, null)) ;
    List<ActionData> actionUser = new ArrayList<ActionData>() ;
    actionUser = new ArrayList<ActionData>() ;
    ActionData selectUserAction = new ActionData() ;
    selectUserAction.setActionListener("SelectPermission") ;
    selectUserAction.setActionName("SelectUser") ;    
    selectUserAction.setActionType(ActionData.TYPE_ICON) ;
    selectUserAction.setCssIconClass("SelectUserIcon") ;
    selectUserAction.setActionParameter(UISelectComponent.TYPE_USER) ;
    actionUser.add(selectUserAction) ;
    inputset.setActionField(FIELD_USER, actionUser) ;
/*
    inputset.addUIFormInput(new UIFormStringInput(FIELD_GROUP, FIELD_GROUP, null)) ;
    List<ActionData> actionGroup = new ArrayList<ActionData>() ;
    ActionData selectGroupAction = new ActionData() ;
    selectGroupAction.setActionListener("SelectPermission") ;
    selectGroupAction.setActionName("SelectGroup") ;    
    selectGroupAction.setActionType(ActionData.TYPE_ICON) ;  
    selectGroupAction.setCssIconClass("SelectGroupIcon") ;
    selectGroupAction.setActionParameter(UISelectComponent.TYPE_GROUP) ;
    actionGroup.add(selectGroupAction) ;
    inputset.setActionField(FIELD_GROUP, actionGroup) ;
*/
    inputset.addChild(new UIFormCheckBoxInput<Boolean>(FIELD_EDIT, FIELD_EDIT, null)) ;
    addChild(inputset) ;    
  }
  
  
  public String getLabel(String id) {
    try {
      return super.getLabel(id) ;
    } catch (Exception e) {
      return id ;
    }
  }

  public void setAddress(String addressId, String addressName) {
    addressId_ = addressId ;
    getUIFormInputInfo(FIELD_ADDRESS).setValue(addressName) ;
  }
  public void setSharedContacts(Map<String, String> contacts) { 
    sharedContacts = contacts ; 
    StringBuffer buffer = new StringBuffer() ;
    for (String contactName : contacts.values()) {
      if (buffer.length() > 0) buffer.append(", ") ;
      buffer.append(contactName) ;
    }
    getUIFormInputInfo(FIELD_CONTACT).setValue(buffer.toString()) ;
  }

  public String[] getActions() { return new String[] {"Save","Cancel"} ; }
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}

  public void updateSelect(String selectField, String value) throws Exception {
    UIFormStringInput fieldInput = getUIStringInput(selectField) ;
    Map<String, String> permission ;
    if (selectField.equals(FIELD_USER)) {
      permissionUser_.put(value, value) ;
      permission = permissionUser_ ;
    } else {
      permissionGroup_.put(value, value) ;
      permission = permissionGroup_ ;
    }  
    StringBuilder sb = new StringBuilder() ;
    for(String s : permission.values()) {      
      if(sb != null && sb.length() > 0) sb.append(", ") ;
      sb.append(s) ;
    }
    
    fieldInput.setValue(sb.toString()) ;
  } 
  
  static  public class SaveActionListener extends EventListener<UISharedForm> {
    public void execute(Event<UISharedForm> event) throws Exception {
      UISharedForm uiForm = event.getSource() ;
      String names = uiForm.getUIStringInput(FIELD_USER).getValue() ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      if(names == null || names.length() < 1) {        
        uiApp.addMessage(new ApplicationMessage("UISharedForm.msg.empty-username", null,
            ApplicationMessage.WARNING)) ;
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
      receiverUser.remove(ContactUtils.getCurrentUser()) ;
      if (receiverUser.size() > 0) {      
        ContactService contactService = ContactUtils.getContactService() ;
        String username = ContactUtils.getCurrentUser() ;
        if(uiForm.getUIFormCheckBoxInput(UISharedForm.FIELD_EDIT).isChecked()) {
          ContactGroup contactGroup = contactService.getGroup(
              SessionsUtils.getSessionProvider(), ContactUtils.getCurrentUser(), uiForm.addressId_) ;
          String[] perms = receiverUser.toArray(new String[] {}) ;
          contactGroup.setEditPermission(perms) ;
          contactService.saveGroup(SessionsUtils.getSessionProvider(), username, contactGroup, false) ;
        }      
        contactService.shareAddressBook(
            SessionsUtils.getSystemProvider(), username, uiForm.addressId_, receiverUser) ;
        UIContactPortlet contactPortlet = uiForm.getAncestorOfType(UIContactPortlet.class) ;
        UIAddressBooks addressBooks = contactPortlet.findFirstComponentOfType(UIAddressBooks.class) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(addressBooks) ;
        contactPortlet.cancelAction() ;
        uiApp.addMessage(new ApplicationMessage("UISharedForm.msg.address-shared", null)) ;
      } else {
        uiApp.addMessage(new ApplicationMessage("UISharedForm.msg.address-not-shared", null)) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      return ;      
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
      
      if (permType.equals(UISelectComponent.TYPE_USER))      
        uiGroupSelector.setComponent(uiForm, new String[]{UISharedForm.FIELD_USER}) ;
      else uiGroupSelector.setComponent(uiForm, new String[]{UISharedForm.FIELD_GROUP}) ;
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
