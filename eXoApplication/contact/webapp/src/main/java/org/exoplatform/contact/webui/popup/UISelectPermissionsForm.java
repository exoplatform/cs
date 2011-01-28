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

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.AddressBook;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.Utils;
import org.exoplatform.contact.webui.UIAddressBooks;
import org.exoplatform.contact.webui.UIContactPortlet;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIBreadcumbs;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UITree;
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
import org.exoplatform.webui.organization.UIGroupMembershipSelector;
import org.exoplatform.webui.organization.account.UIUserSelector;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Hoang
 *          hung.hoang@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfigs ( {
  @ComponentConfig(
      lifecycle = UIFormLifecycle.class,
      template = "system:/groovy/webui/form/UIForm.gtmpl",
      events = {
        @EventConfig(listeners = UISelectPermissionsForm.SaveActionListener.class),    
        @EventConfig(listeners = UISelectPermissionsForm.SelectPermissionActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UISelectPermissionsForm.SelectMembershipActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UISelectPermissionsForm.CancelActionListener.class, phase = Phase.DECODE)
      }
  ),
  @ComponentConfig(
                   id = "UIPopupWindowUserSelect",
                   type = UIPopupWindow.class,
                   template =  "system:/groovy/webui/core/UIPopupWindow.gtmpl",
                   events = {
                     @EventConfig(listeners = UIPopupWindow.CloseActionListener.class, name = "ClosePopup")  ,
                     @EventConfig(listeners = UISelectPermissionsForm.AddActionListener.class, name = "Add", phase = Phase.DECODE),
                     @EventConfig(listeners = UISelectPermissionsForm.CloseActionListener.class, name = "Close", phase = Phase.DECODE)
                   }
  )
})
public class UISelectPermissionsForm extends UIForm implements UIPopupComponent, UISelector{
  final static public String FIELD_ADDRESS = "addressName".intern() ;
  final static public String FIELD_USER = "user".intern() ;
  final static public String FIELD_GROUP = "group".intern() ;
  final static public String FIELD_EDIT_PERMISSION = "canEdit".intern() ;
  private AddressBook group_ = null ;
  public UISelectPermissionsForm() { }
  
  public void setGroup(AddressBook group) {
    group_ = group ; 
  }

  public void init() throws Exception {
    UIFormInputWithActions inputset = new UIFormInputWithActions("UIInputUserSelect") ;
    UIFormInputInfo formInputInfo = new UIFormInputInfo(FIELD_ADDRESS, FIELD_ADDRESS, null) ;
    formInputInfo.setValue(group_.getName()) ;
    inputset.addChild(formInputInfo) ; 

    
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
    StringBuilder sb = new StringBuilder("") ;
    if (!ContactUtils.isEmpty(fieldInput.getValue())) sb.append(fieldInput.getValue()) ;
    if (sb.indexOf(value) == -1) {
      if (sb.length() == 0) sb.append(value) ;
      else sb.append("," + value) ;
      fieldInput.setValue(sb.toString()) ;
    } 
  } 
  
  static  public class SaveActionListener extends EventListener<UISelectPermissionsForm> {
    @SuppressWarnings("unchecked")
    public void execute(Event<UISelectPermissionsForm> event) throws Exception {
      UISelectPermissionsForm uiForm = event.getSource() ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      String names = uiForm.getUIStringInput(FIELD_USER).getValue() ;
      String groups = uiForm.getUIStringInput(FIELD_GROUP).getValue() ;
      Map<String, String> receiveUsers = new LinkedHashMap<String, String>() ;
      Map<String, String> receiveGroups = new LinkedHashMap<String, String>() ;
      
      if(ContactUtils.isEmpty(names) && ContactUtils.isEmpty(groups)) {        
        uiApp.addMessage(new ApplicationMessage("UISelectPermissionsForm.msg.empty-username", null,
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
              receiveUsers.put(name.trim(), name.trim()) ;
            }
          } else {
            if (invalidUsers.length() == 0) invalidUsers.append(name) ;
            else invalidUsers.append(", " + name) ;
          }
        }
        if (invalidUsers.length() > 0) {
          uiApp.addMessage(new ApplicationMessage("UISelectPermissionsForm.msg.not-exist-username"
              , new Object[]{invalidUsers.toString()}, 1 )) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;          
        }      
      }
      
      if(!ContactUtils.isEmpty(groups)) {
        StringBuilder invalidGroups = new StringBuilder() ;
        String[] array = groups.split(",") ;
        for(String name : array) {
          if(name != null && name.trim().length() != 0){
            if (name.indexOf(Utils.COLON) > 0) name = name.split(Utils.COLON)[0];
            if (organizationService.getGroupHandler().findGroupById(name.trim()) == null) {
              if (invalidGroups.length() == 0) invalidGroups.append(name) ;
              else invalidGroups.append(", " + name) ;
            }
          }
        }
        if (invalidGroups.length() > 0) {
          uiApp.addMessage(new ApplicationMessage("UISelectPermissionsForm.msg.not-exist-group"
              , new Object[]{invalidGroups.toString()}, 1 )) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;          
        }      
      }
      
      ContactService contactService = ContactUtils.getContactService() ;
      if (!ContactUtils.isEmpty(groups)) {
        String[] arrayGroups = groups.split(",") ;
        for (String group : arrayGroups) {
          if(group != null && group.trim().length() != 0){
            group = group.trim() ;
            receiveGroups.put(group, group) ;
          }
        }
      }
      if (receiveGroups.size() == 0 && receiveUsers.size() == 0) {
        uiApp.addMessage(new ApplicationMessage("UISelectPermissionsForm.msg.shared-yourself", null,
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      AddressBook contactGroup = uiForm.group_ ;
      if(uiForm.getUIFormCheckBoxInput(UISelectPermissionsForm.FIELD_EDIT_PERMISSION).isChecked()) {
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
   
      } else {
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

      if (!uiForm.getUIFormCheckBoxInput(UISelectPermissionsForm.FIELD_EDIT_PERMISSION).isChecked()) {
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
      } else {
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
        for (String user : receiveGroups.keySet()) editMapGroups.put(user, user) ;
        contactGroup.setEditPermissionGroups(editMapGroups.keySet().toArray(new String[] {})) ;
      }

      contactService.savePublicAddressBook(contactGroup, false) ;
      UIPublicAddressPermission uiAddEdit = uiForm.getParent() ;          
      uiAddEdit.updateGroupGrid(contactGroup);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAddEdit.getChild(UIGrid.class)) ;          
      event.getRequestContext().addUIComponentToUpdateByAjax(
          uiForm.getAncestorOfType(UIContactPortlet.class).findFirstComponentOfType(UIAddressBooks.class)) ;
      uiForm.getUIStringInput(UISelectPermissionsForm.FIELD_USER).setValue(null) ;
      uiForm.getUIStringInput(UISelectPermissionsForm.FIELD_GROUP).setValue(null) ;
      uiForm.getUIFormCheckBoxInput(UISelectPermissionsForm.FIELD_EDIT_PERMISSION).setChecked(false) ;  
    }
  }
  
  static  public class AddActionListener extends EventListener<UIUserSelector> {
    public void execute(Event<UIUserSelector> event) throws Exception {
      UIUserSelector uiUserSelector = event.getSource();
      UIPopupContainer uiContainer = uiUserSelector.getAncestorOfType(UIPopupContainer.class) ;
      UISelectPermissionsForm uiForm = uiContainer.findFirstComponentOfType(UISelectPermissionsForm.class);
      String values = uiUserSelector.getSelectedUsers();
      if (values == null) return ;
      for (String value : values.split(","))
        uiForm.updateSelect(FIELD_USER, value.trim()) ; 
      UIPopupWindow uiPoupPopupWindow = uiUserSelector.getParent() ;
      uiPoupPopupWindow.setUIComponent(null) ;
      uiPoupPopupWindow.setShow(false) ; 
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer);
    }
  }
  
  static  public class CloseActionListener extends EventListener<UIUserSelector> {
    public void execute(Event<UIUserSelector> event) throws Exception {
      UIUserSelector uiUseSelector = event.getSource() ;
      UIPopupWindow uiPoupPopupWindow = uiUseSelector.getParent() ;
      UIPopupContainer uiContainer = uiPoupPopupWindow.getAncestorOfType(UIPopupContainer.class) ;
      uiPoupPopupWindow.setUIComponent(null) ;
      uiPoupPopupWindow.setShow(false) ;      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;  
    }
  }

  static  public class SelectPermissionActionListener extends EventListener<UISelectPermissionsForm> {
    public void execute(Event<UISelectPermissionsForm> event) throws Exception {
      UISelectPermissionsForm uiForm = event.getSource() ; 
      String permType = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIPopupContainer uiContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      uiContainer.removeChild(UIPopupWindow.class) ;
      uiForm.removeChild(UIPopupWindow.class) ;
      if (permType.equals(UISelectComponent.TYPE_USER)) {
        UIPopupWindow uiPopupWindow = uiContainer.getChild(UIPopupWindow.class) ;        
        if (uiPopupWindow == null) {
          uiPopupWindow = uiContainer.addChild(UIPopupWindow.class, "UIPopupWindowUserSelect", "UIPopupWindowUserSelect") ;
        }
        UIUserSelector uiUserSelector = uiContainer.createUIComponent(UIUserSelector.class, null, null) ;
        uiUserSelector.setShowSearch(true);
        uiUserSelector.setShowSearchUser(true) ;
        uiUserSelector.setShowSearchGroup(true);

        uiPopupWindow.setUIComponent(uiUserSelector);
        uiPopupWindow.setShow(true);
        uiPopupWindow.setWindowSize(740, 400) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;        
      } else  if (permType.equals(UISelectComponent.TYPE_GROUP)) {
        UIPopupWindow uiPopup = uiForm.addChild(UIPopupWindow.class, null, "UIPopupGroupSelector");
        uiPopup.setWindowSize(540, 0);
        UIGroupMembershipSelector uiGroup = uiForm.createUIComponent(UIGroupMembershipSelector.class, null, null);
        uiPopup.setUIComponent(uiGroup);
        uiGroup.setId("UIGroupSelector");
        uiGroup.getChild(UITree.class).setId("TreeGroupSelector");
        uiGroup.getChild(UIBreadcumbs.class).setId("BreadcumbsGroupSelector");
        uiForm.getChild(UIPopupWindow.class).setShow(true);  
        event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ; 
      }
    }
  }
  
  static  public class SelectMembershipActionListener extends EventListener<UIGroupMembershipSelector> {   
    public void execute(Event<UIGroupMembershipSelector> event) throws Exception {
      UIGroupMembershipSelector uiForm = event.getSource() ;
      String membership = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String groupId = uiForm.getCurrentGroup().getId();
      UIPopupContainer uiContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UISelectPermissionsForm uiSelectPermissionsForm = uiContainer.findFirstComponentOfType(UISelectPermissionsForm.class);
      uiSelectPermissionsForm.updateSelect(FIELD_GROUP, groupId + Utils.COLON + Utils.MEMBERSHIP +  membership) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSelectPermissionsForm);
    }
  }
  
  static  public class CancelActionListener extends EventListener<UISelectPermissionsForm> {
    public void execute(Event<UISelectPermissionsForm> event) throws Exception {
      UISelectPermissionsForm uiForm = event.getSource() ;
      UIContactPortlet contactPortlet = uiForm.getAncestorOfType(UIContactPortlet.class) ;
      contactPortlet.cancelAction() ;
    }
  }

}
