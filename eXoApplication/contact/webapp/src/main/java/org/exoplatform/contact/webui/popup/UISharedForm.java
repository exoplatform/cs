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
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.webui.UIContactPortlet;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.organization.OrganizationService;
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
  final static public String FIELD_EDIT_PERMISSION = "canEdit".intern() ;
  private Map<String, String> permissionUser_ = new LinkedHashMap<String, String>() ;
  private Map<String, String> permissionGroup_ = new LinkedHashMap<String, String>() ;
  private ContactGroup group_ = null ;
  private Contact contact_ = null ;
  private boolean isSharedGroup ;
  public UISharedForm() { }
  
  public void setContact(Contact contact) { 
    isSharedGroup = false ;
    contact_ = contact ;
    UIFormInputWithActions inputset = new UIFormInputWithActions("UIInputUserSelect") ;
    UIFormInputInfo formInputInfo = new UIFormInputInfo(FIELD_CONTACT, FIELD_CONTACT, null) ;
    formInputInfo.setValue(contact.getFullName()) ;
    inputset.addChild(formInputInfo) ; 
    addChild(inputset) ;
  }
  public void setGroup(ContactGroup group) {
    isSharedGroup = true ;
    group_ = group ;
    UIFormInputWithActions inputset = new UIFormInputWithActions("UIInputUserSelect") ;
    UIFormInputInfo formInputInfo = new UIFormInputInfo(FIELD_ADDRESS, FIELD_ADDRESS, null) ;
    formInputInfo.setValue(group.getName()) ;
    inputset.addChild(formInputInfo) ; 
    addChild(inputset) ;
  }
  
  public void init() throws Exception {
    UIFormInputWithActions inputset = getChild(UIFormInputWithActions.class) ;
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
    
    UIFormStringInput groupField = new UIFormStringInput(FIELD_GROUP, FIELD_GROUP, null) ;
    groupField.setEditable(false) ;
    inputset.addUIFormInput(groupField) ;
    List<ActionData> actionGroup = new ArrayList<ActionData>() ;
    ActionData selectGroupAction = new ActionData() ;
    selectGroupAction.setActionListener("SelectPermission") ;
    selectGroupAction.setActionName("SelectGroup") ;    
    selectGroupAction.setActionType(ActionData.TYPE_ICON) ;  
    selectGroupAction.setCssIconClass("SelectGroupIcon") ;
    selectGroupAction.setActionParameter(UISelectComponent.TYPE_GROUP) ;
    actionGroup.add(selectGroupAction) ;
    inputset.setActionField(FIELD_GROUP, actionGroup) ;    
    inputset.addChild(new UIFormCheckBoxInput<Boolean>(FIELD_EDIT_PERMISSION, FIELD_EDIT_PERMISSION, null)) ;     
  }
  public String getLabel(String id) {
    try {
      return super.getLabel(id) ;
    } catch (Exception e) {
      return id ;
    }
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
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      String names = uiForm.getUIStringInput(FIELD_USER).getValue() ;
      String groups = uiForm.getUIStringInput(FIELD_GROUP).getValue() ;
      List<String> receiverUser = new ArrayList<String>() ;
      if(ContactUtils.isEmpty(names) && ContactUtils.isEmpty(groups)) {        
        uiApp.addMessage(new ApplicationMessage("UISharedForm.msg.empty-username", null,
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } 
      if(!ContactUtils.isEmpty(names)) {
        OrganizationService organizationService = 
          (OrganizationService)PortalContainer.getComponent(OrganizationService.class) ;
        try {
          if (names.indexOf(",") > 0) {
            String[] array = names.split(",") ;
            for(String name : array) {
              organizationService.getUserHandler().findUserByName(name).getFullName();
              receiverUser.add(name.trim()) ;
            }
          } else {
            organizationService.getUserHandler().findUserByName(names.trim()).getFullName();
            receiverUser.add(names.trim()) ;
          }
        } catch (NullPointerException e) {
          uiApp.addMessage(new ApplicationMessage("UISharedForm.msg.not-exist-username", null,
              ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
      }
      ContactService contactService = ContactUtils.getContactService() ;
      String username = ContactUtils.getCurrentUser() ;
      if (!ContactUtils.isEmpty(groups)) {
        String[] arrayGroups = groups.split(",") ; 
        for (String group : arrayGroups) {
          List<Contact> contacts = contactService
            .getPublicContactsByAddressBook(SessionProviderFactory.createSystemProvider(), group).getAll() ; 
          for (Contact contact : contacts) {
            receiverUser.add(contact.getId()) ;
          }
        }        
      } 
      receiverUser.remove(ContactUtils.getCurrentUser()) ;
      if (receiverUser.size() > 0) {
        if (uiForm.isSharedGroup) {
          ContactGroup contactGroup = uiForm.group_ ;  
          String[] viewPer = contactGroup.getViewPermission() ;
          Map<String, String> viewMap = new LinkedHashMap<String, String>() ; 
          if (viewPer != null)
            for (String view : viewPer) viewMap.put(view, view) ; 
          for (String user : receiverUser) viewMap.put(user, user) ;
          contactGroup.setViewPermission(viewMap.keySet().toArray(new String[] {})) ;
          
          UIAddEditPermission uiAddEdit = uiForm.getParent() ;
          if(uiForm.getUIFormCheckBoxInput(UISharedForm.FIELD_EDIT_PERMISSION).isChecked()) {
            String[] editPer = contactGroup.getEditPermission() ;
            Map<String, String> editMap = new LinkedHashMap<String, String>() ; 
            if (editPer != null)
              for (String edit : editPer) editMap.put(edit, edit) ; 
            for (String user : receiverUser) editMap.put(user, user) ;
            contactGroup.setEditPermission(editMap.keySet().toArray(new String[] {})) ;
          }
          contactService.saveGroup(SessionProviderFactory.createSessionProvider(), username, contactGroup, false) ;
          uiAddEdit.updateGroupGrid(contactGroup);
          event.getRequestContext().addUIComponentToUpdateByAjax(uiAddEdit) ; 
          contactService.shareAddressBook(
              SessionProviderFactory.createSystemProvider(), username, contactGroup.getId(), receiverUser) ;
        
          // added
          List<Contact> contacts = contactService.getContactPageListByGroup(
              SessionProviderFactory.createSessionProvider(), username, uiForm.group_.getId()).getAll() ;
          Map<String, String> viewMapContact = new LinkedHashMap<String, String>() ; 
          for (String user : receiverUser) viewMapContact.put(user, user) ;
          Map<String, String> editMapContact = new LinkedHashMap<String, String>() ; 
          if(uiForm.getUIFormCheckBoxInput(UISharedForm.FIELD_EDIT_PERMISSION).isChecked())
            for (String user : receiverUser) editMapContact.put(user, user) ;
          for (Contact contact : contacts) {
            contactService.saveContact(SessionProviderFactory.createSessionProvider()
                , username, addPer(contact, viewMapContact, editMapContact), false);
          }
        } else {
          Map<String, String> viewMap = new LinkedHashMap<String, String>() ; 
          for (String user : receiverUser) viewMap.put(user, user) ;
          Map<String, String> editMap = new LinkedHashMap<String, String>() ; 
          if(uiForm.getUIFormCheckBoxInput(UISharedForm.FIELD_EDIT_PERMISSION).isChecked())
            for (String user : receiverUser) editMap.put(user, user) ;
          Contact contact = addPer(uiForm.contact_, viewMap, editMap)  ;
          UIAddEditPermission uiAddEdit = uiForm.getParent() ;
          contactService.saveContact(SessionProviderFactory.createSessionProvider(), username, contact, false) ;
          uiAddEdit.updateContactGrid(contact);
          event.getRequestContext().addUIComponentToUpdateByAjax(uiAddEdit) ; 
          contactService.shareContact(SessionProviderFactory
              .createSystemProvider(), username, new String[] {contact.getId()}, receiverUser) ; 
        } 
      } else {
        uiApp.addMessage(new ApplicationMessage("UISharedForm.msg.invalid-username", null,
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
    }
    private Contact addPer(Contact contact, Map<String, String> viewMap, Map<String, String> editMap) {
      String[] viewPer = contact.getViewPermission() ;
      if (viewPer != null)
        for (String view : viewPer) viewMap.put(view, view) ;
      contact.setViewPermission(viewMap.keySet().toArray(new String[] {})) ;
      
      String[] editPer = contact.getEditPermission() ;
      if (editPer != null)
        for (String edit : editPer) editMap.put(edit, edit) ; 
      contact.setEditPermission(editMap.keySet().toArray(new String[] {})) ;
      
      return contact ;
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
