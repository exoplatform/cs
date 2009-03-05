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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.PathNotFoundException;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.impl.JCRDataStorage;
import org.exoplatform.contact.webui.UIAddressBooks;
import org.exoplatform.contact.webui.UIContactPortlet;
import org.exoplatform.contact.webui.UIContacts;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
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
 * Author : Hung Hoang
 *          hung.hoang@exoplatform.com
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
  private boolean isNew_ = true ;
  public UISharedForm() { }
  
  public void setContact(Contact contact) { 
    isSharedGroup = false ;
    contact_ = contact ;
  }
  public void setGroup(ContactGroup group) {
    isSharedGroup = true ;
    group_ = group ; 
  }
  
  public void setNew(boolean isNew) { isNew_ = isNew ; }
  public boolean isNew() { return isNew_ ; }
  
  public void init() throws Exception {
    UIFormInputWithActions inputset = new UIFormInputWithActions("UIInputUserSelect") ;
    if (isSharedGroup) {
      UIFormInputInfo formInputInfo = new UIFormInputInfo(FIELD_ADDRESS, FIELD_ADDRESS, null) ;
      formInputInfo.setValue(group_.getName()) ;
      inputset.addChild(formInputInfo) ; 
    } else {
      UIFormInputInfo formInputInfo = new UIFormInputInfo(FIELD_CONTACT, FIELD_CONTACT, null) ;
      formInputInfo.setValue(ContactUtils.encodeHTML(contact_.getFullName())) ;
      inputset.addChild(formInputInfo) ;
    }
    
    addChild(inputset) ;
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
      // add to fix bug 902
      if (!ContactUtils.isEmpty(fieldInput.getValue()))
        for (String user : fieldInput.getValue().split(","))
          permissionUser_.put(user.trim(), user.trim()) ;
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
    @SuppressWarnings("unchecked")
	public void execute(Event<UISharedForm> event) throws Exception {
      UISharedForm uiForm = event.getSource() ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      String names = uiForm.getUIStringInput(FIELD_USER).getValue() ;
      String groups = uiForm.getUIStringInput(FIELD_GROUP).getValue() ;
      Map<String, String> receiveUsers = new LinkedHashMap<String, String>() ;
      Map<String, String> receiveGroups = new LinkedHashMap<String, String>() ;
      
      if(ContactUtils.isEmpty(names) && ContactUtils.isEmpty(groups)) {        
        uiApp.addMessage(new ApplicationMessage("UISharedForm.msg.empty-username", null,
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } 
      String username = ContactUtils.getCurrentUser() ;
      OrganizationService organizationService = 
        (OrganizationService)PortalContainer.getComponent(OrganizationService.class) ;
      if(!ContactUtils.isEmpty(names)) {
        StringBuilder invalidUsers = new StringBuilder() ;
        String[] array = names.split(",") ;
        for(String name : array) {
          if (organizationService.getUserHandler().findUserByName(name.trim()) != null) {
            if (!name.trim().equals(username)) {
              receiveUsers.put(name.trim() + JCRDataStorage.HYPHEN, name.trim() + JCRDataStorage.HYPHEN) ;
            }
          } else {
            if (invalidUsers.length() == 0) invalidUsers.append(name) ;
            else invalidUsers.append(", " + name) ;
          }
        }
        if (invalidUsers.length() > 0) {
          uiApp.addMessage(new ApplicationMessage("UISharedForm.msg.not-exist-username"
              , new Object[]{invalidUsers.toString()}, 1 )) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;          
        }      
      }
      ContactService contactService = ContactUtils.getContactService() ;
      SessionProvider sessionProvider = SessionProviderFactory.createSessionProvider() ;      
      Map<String, String>  receiveUsersByGroups = new LinkedHashMap<String, String>() ;
      if (!ContactUtils.isEmpty(groups)) {
        String[] arrayGroups = groups.split(",") ; 
        for (String group : arrayGroups) {
          group = group.trim() ;
          receiveGroups.put(group, group) ;
            List<User> users = organizationService.getUserHandler().findUsersByGroup(group.trim()).getAll() ;
          for (User user : users) {
            receiveUsersByGroups.put(user.getUserName(), user.getUserName()) ;
          }
        }
      }      
      receiveUsersByGroups.remove(ContactUtils.getCurrentUser()) ;

      if (receiveGroups.size() == 0 && receiveUsers.size() == 0) {
        uiApp.addMessage(new ApplicationMessage("UISharedForm.msg.shared-yourself", null,
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      
      // xong phan xu ly recieve users
      if (uiForm.isSharedGroup) {
        ContactGroup contactGroup = uiForm.group_ ;
        if(uiForm.getUIFormCheckBoxInput(UISharedForm.FIELD_EDIT_PERMISSION).isChecked()) {
          String[] editPerUsers = contactGroup.getEditPermissionUsers() ;
          Map<String, String> editMapUsers = new LinkedHashMap<String, String>() ; 
          if (editPerUsers != null)
            for (String edit : editPerUsers) editMapUsers.put(edit, edit) ;
          for (String user : receiveUsers.keySet()) editMapUsers.put(user, user) ;
          contactGroup.setEditPermissionUsers(editMapUsers.keySet().toArray(new String[] {})) ;
          
          String[] editPerGroups = contactGroup.getEditPermissionGroups() ;
          Map<String, String> editMapGroups = new LinkedHashMap<String, String>() ; 
          if (editPerGroups != null)
            for (String edit : editPerGroups) editMapGroups.put(edit, edit) ;
          for (String group : receiveGroups.keySet()) editMapGroups.put(group, group) ;
          contactGroup.setEditPermissionGroups(editMapGroups.keySet().toArray(new String[] {})) ; 
     
        } else { // cs-1570
          if (contactGroup.getEditPermissionUsers() != null) {
            List<String> oldPers = new ArrayList<String>() ;
            oldPers.addAll(Arrays.asList(contactGroup.getEditPermissionUsers())) ;
            for (String user : receiveUsers.keySet()) {
              oldPers.remove(user) ;              
            }
            contactGroup.setEditPermissionUsers(oldPers.toArray(new String[] {})) ;            
          }
          if (contactGroup.getEditPermissionGroups() != null) {
            List<String> oldPers = new ArrayList<String>() ;
            oldPers.addAll(Arrays.asList(contactGroup.getEditPermissionGroups())) ;
            for (String group : receiveGroups.keySet()) {
              oldPers.remove(group) ;              
            }
            contactGroup.setEditPermissionGroups(oldPers.toArray(new String[] {})) ;            
          }
        }
        if (uiForm.isNew_) {
          String[] viewPerUsers = contactGroup.getViewPermissionUsers() ;
          Map<String, String> viewMapUsers = new LinkedHashMap<String, String>() ; 
          if (viewPerUsers != null)
            for (String view : viewPerUsers) viewMapUsers.put(view, view) ; 
          for (String user : receiveUsers.keySet()) viewMapUsers.put(user, user) ;
          contactGroup.setViewPermissionUsers(viewMapUsers.keySet().toArray(new String[] {})) ;
          
          String[] viewPerGroups = contactGroup.getViewPermissionGroups() ;
          Map<String, String> viewMapGroups = new LinkedHashMap<String, String>() ; 
          if (viewPerGroups != null)
            for (String view : viewPerGroups) viewMapGroups.put(view, view) ; 
          for (String user : receiveGroups.keySet()) viewMapGroups.put(user, user) ;
          contactGroup.setViewPermissionGroups(viewMapGroups.keySet().toArray(new String[] {})) ;

          if (receiveUsers.size() > 0 ) {
            contactService.shareAddressBook(
                SessionProviderFactory.createSystemProvider(), username, contactGroup.getId(), Arrays.asList(receiveUsers.keySet().toArray(new String[] {}))) ;
          }     
          
          // remove if  to fix bug cs-1449
          //if (receiveUsersByGroups.size() > 0) {
            contactService.shareAddressBook(
                SessionProviderFactory.createSystemProvider(), username, contactGroup.getId(), Arrays.asList(receiveUsersByGroups.keySet().toArray(new String[] {}))) ;
          //}            
        } else { // change permission
          if (!uiForm.getUIFormCheckBoxInput(UISharedForm.FIELD_EDIT_PERMISSION).isChecked()) {
            List<String> newPerUsers = new ArrayList<String>() ; 
            if (contactGroup.getEditPermissionUsers() != null)
              for (String edit : contactGroup.getEditPermissionUsers())
                if(!receiveUsers.keySet().contains(edit)) {
                  newPerUsers.add(edit) ;
              }
            contactGroup.setEditPermissionUsers(newPerUsers.toArray(new String[newPerUsers.size()])) ;
            
            List<String> newPerGroups = new ArrayList<String>() ; 
            if (contactGroup.getEditPermissionGroups() != null)
              for (String edit : contactGroup.getEditPermissionGroups())
                if(!receiveGroups.keySet().contains(edit)) {
                  newPerGroups.add(edit) ;
              }
            contactGroup.setEditPermissionGroups(newPerGroups.toArray(new String[newPerGroups.size()])) ;              
          }
          uiForm.getUIStringInput(UISharedForm.FIELD_USER).setEditable(true) ;
        }
        contactService.saveGroup(sessionProvider, username, contactGroup, false) ;
        UIAddEditPermission uiAddEdit = uiForm.getParent() ;          
        uiAddEdit.updateGroupGrid(contactGroup);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiAddEdit) ;          
        event.getRequestContext().addUIComponentToUpdateByAjax(
            uiForm.getAncestorOfType(UIContactPortlet.class).findFirstComponentOfType(UIAddressBooks.class)) ;
      } else { // shared contact 
        if (uiForm.isNew_) {
          Map<String, String> viewMapUsers = new LinkedHashMap<String, String>() ; 
          for (String user : receiveUsers.keySet()) viewMapUsers.put(user, user) ;
          Map<String, String> editMapUsers = new LinkedHashMap<String, String>() ;
          if(uiForm.getUIFormCheckBoxInput(UISharedForm.FIELD_EDIT_PERMISSION).isChecked()) {
            for (String user : receiveUsers.keySet()) editMapUsers.put(user, user) ;
          }
          Map<String, String> viewMapGroups = new LinkedHashMap<String, String>() ; 
          for (String user : receiveGroups.keySet()) viewMapGroups.put(user, user) ; 
          Map<String, String> editMapGroups = new LinkedHashMap<String, String>() ;
          if(uiForm.getUIFormCheckBoxInput(UISharedForm.FIELD_EDIT_PERMISSION).isChecked()) {
            for (String user : receiveGroups.keySet()) editMapGroups.put(user, user) ;
          }
          // add to fix bug cs-1326
          Contact contact = contactService.getContact(sessionProvider, username, uiForm.contact_.getId()) ;
          //Contact contact = uiForm.contact_ ;
          addPerUsers(contact, viewMapUsers, editMapUsers) ;
          addPerGroups(contact, viewMapGroups, editMapGroups) ;
          
          if(!uiForm.getUIFormCheckBoxInput(UISharedForm.FIELD_EDIT_PERMISSION).isChecked()) { // cs-1570
            if (contact.getEditPermissionUsers() != null) {
              List<String> oldPers = new ArrayList<String>() ;
              oldPers.addAll(Arrays.asList(contact.getEditPermissionUsers())) ;
              for (String user : receiveUsers.keySet()) {
                oldPers.remove(user) ;              
              }
              contact.setEditPermissionUsers(oldPers.toArray(new String[] {})) ;            
            }
            if (contact.getEditPermissionGroups() != null) {
              List<String> oldPers = new ArrayList<String>() ;
              oldPers.addAll(Arrays.asList(contact.getEditPermissionGroups())) ;
              for (String group : receiveGroups.keySet()) {
                oldPers.remove(group) ;              
              }
              contact.setEditPermissionGroups(oldPers.toArray(new String[] {})) ;            
            }
          }
          
          
          // add to fix bug cs-1300
          UIContacts uiContacts = uiForm.getAncestorOfType(
              UIContactPortlet.class).findFirstComponentOfType(UIContacts.class) ;
          if (uiContacts.getContactMap().get(contact.getId()) != null) {
            uiContacts.getContactMap().put(contact.getId(), contact) ;
          }
          try {
            contactService.saveContact(sessionProvider, username, contact, false) ;
          }  catch (PathNotFoundException e) {
            uiApp.addMessage(new ApplicationMessage("UISharedForm.msg.contact-not-existed", null, 
                ApplicationMessage.WARNING)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return ;              
          } 
          UIAddEditPermission uiAddEdit = uiForm.getParent() ;
          uiAddEdit.updateContactGrid(contact);
          event.getRequestContext().addUIComponentToUpdateByAjax(uiAddEdit) ; 
          
          if (receiveUsers.size() > 0)
            contactService.shareContactSys( username, new String[] {contact.getId()}, Arrays.asList(receiveUsers.keySet().toArray(new String[] {}))) ;
          if (receiveUsersByGroups.size() > 0)
            contactService.shareContactSys(username, new String[] {contact.getId()}, Arrays.asList(receiveUsersByGroups.keySet().toArray(new String[] {}))) ; 
          
        } else {
          Contact contact = uiForm.contact_ ;
          if (uiForm.getUIFormCheckBoxInput(UISharedForm.FIELD_EDIT_PERMISSION).isChecked()) {
            String[] editPerUsers = contact.getEditPermissionUsers() ;
            Map<String, String> editMapUsers = new LinkedHashMap<String, String>() ;
            if (editPerUsers != null)
              for (String edit : editPerUsers) editMapUsers.put(edit, edit) ; 
            for (String user : receiveUsers.keySet()) editMapUsers.put(user, user) ;
            contact.setEditPermissionUsers(editMapUsers.keySet().toArray(new String[] {})) ;     
            
            String[] editPerGroups = contact.getEditPermissionGroups() ;
            Map<String, String> editMapGroups = new LinkedHashMap<String, String>() ;
            if (editPerUsers != null)
              for (String edit : editPerGroups) editMapGroups.put(edit, edit) ; 
            for (String user : receiveGroups.keySet()) editMapGroups.put(user, user) ;
            contact.setEditPermissionGroups(editMapGroups.keySet().toArray(new String[] {})) ;
          } else {
            List<String> newEditPerUsers = new ArrayList<String>() ;
            for (String edit : contact.getEditPermissionUsers()) 
              if (!receiveUsers.keySet().contains(edit)) newEditPerUsers.add(edit) ; 
            contact.setEditPermissionUsers(newEditPerUsers.toArray(new String[] {})) ;
            
            List<String> newEditPerGroups = new ArrayList<String>() ;
            if (contact.getEditPermissionGroups() != null)
              for (String edit : contact.getEditPermissionGroups()) 
                if (!receiveGroups.keySet().contains(edit)) newEditPerGroups.add(edit) ; 
            contact.setEditPermissionGroups(newEditPerGroups.toArray(new String[] {})) ;
          }
          contactService.saveContact(sessionProvider, username, contact, false) ;
          UIAddEditPermission uiAddEdit = uiForm.getParent() ;
          uiAddEdit.updateContactGrid(contact);
          event.getRequestContext().addUIComponentToUpdateByAjax(uiAddEdit) ;
          uiForm.getUIStringInput(UISharedForm.FIELD_USER).setEditable(true) ;
          
          UIContacts uiContacts = uiForm.getAncestorOfType(
              UIContactPortlet.class).findFirstComponentOfType(UIContacts.class) ;
          
//        add to fix bug cs-1300
          if (uiContacts.getContactMap().get(contact.getId()) != null) {
            uiContacts.getContactMap().put(contact.getId(), contact) ;
          }
          /*
          if (uiContacts.isDisplaySearchResult()) {
            uiContacts.getContactMap().put(contact.getId(), contact) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts) ;
          }*/
        } 
      }
      uiForm.getUIStringInput(UISharedForm.FIELD_USER).setValue(null) ;
      uiForm.getUIStringInput(UISharedForm.FIELD_GROUP).setValue(null) ;
      uiForm.getUIFormCheckBoxInput(UISharedForm.FIELD_EDIT_PERMISSION).setChecked(false) ;
      uiForm.permissionUser_.clear() ;
      uiForm.permissionGroup_.clear() ;        
  
      uiForm.isNew_ = true ;
    }
    private static Contact addPerUsers(Contact contact, Map<String, String> viewMap, Map<String, String> editMap) {
      String[] viewPer = contact.getViewPermissionUsers() ;
      if (viewPer != null)
        for (String view : viewPer) viewMap.put(view, view) ;
      contact.setViewPermissionUsers(viewMap.keySet().toArray(new String[] {})) ;
      
      String[] editPer = contact.getEditPermissionUsers() ;
      if (editPer != null)
        for (String edit : editPer) editMap.put(edit, edit) ; 
      contact.setEditPermissionUsers(editMap.keySet().toArray(new String[] {})) ;
      return contact ;
    }
    private static Contact addPerGroups(Contact contact, Map<String, String> viewMap, Map<String, String> editMap) {
      String[] viewPer = contact.getViewPermissionGroups() ;
      if (viewPer != null)
        for (String view : viewPer) viewMap.put(view, view) ;
      contact.setViewPermissionGroups(viewMap.keySet().toArray(new String[] {})) ;
      
      String[] editPer = contact.getEditPermissionGroups() ;
      if (editPer != null)
        for (String edit : editPer) editMap.put(edit, edit) ; 
      contact.setEditPermissionGroups(editMap.keySet().toArray(new String[] {})) ;
      return contact ;
    }
  }
  
  static  public class SelectPermissionActionListener extends EventListener<UISharedForm> {
    public void execute(Event<UISharedForm> event) throws Exception {
      UISharedForm uiForm = event.getSource() ;
      if (!uiForm.isNew_) {
        UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UISharedForm.msg.cannot-change-username", null,
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } 
      String permType = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIPopupAction childPopup = uiForm.getAncestorOfType(UIPopupContainer.class).getChild(UIPopupAction.class) ;
      UIGroupSelector uiGroupSelector = childPopup.activate(UIGroupSelector.class, 500) ;
      uiGroupSelector.setType(permType) ;
      uiGroupSelector.setSelectedGroups(null) ;
      
      if (permType.equals(UISelectComponent.TYPE_USER)) {
        // add to fix bug cs 997
        String users = uiForm.getUIStringInput(FIELD_USER).getValue() ;
        uiForm.permissionUser_.clear() ;
        if (!ContactUtils.isEmpty(users)) {
          if (users.indexOf(",") < 0) uiForm.permissionUser_.put(users.trim(), users.trim()) ;
          else {
            for (String user : users.split(",")) uiForm.permissionUser_.put(user.trim(), user.trim()) ;              
          }          
        }
        
        uiGroupSelector.setId("UIUserSelector") ;
        uiGroupSelector.setComponent(uiForm, new String[]{UISharedForm.FIELD_USER}) ;
      } else {
        uiGroupSelector.setId("UIGroupSelector") ;
        uiGroupSelector.setComponent(uiForm, new String[]{UISharedForm.FIELD_GROUP}) ;
      }
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
