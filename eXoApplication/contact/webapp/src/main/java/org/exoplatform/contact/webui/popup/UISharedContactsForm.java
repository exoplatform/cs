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
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.DataStorage;
import org.exoplatform.contact.webui.UIContactPortlet;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIBreadcumbs;
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
import org.exoplatform.webui.organization.account.UIGroupSelector;
import org.exoplatform.webui.organization.account.UIUserSelector;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfigs ( {
  @ComponentConfig(
      lifecycle = UIFormLifecycle.class,
      template = "system:/groovy/webui/form/UIForm.gtmpl",
      events = {
        @EventConfig(listeners = UISharedContactsForm.SaveActionListener.class),    
        @EventConfig(listeners = UISharedContactsForm.SelectPermissionActionListener.class, phase = Phase.DECODE),  
        @EventConfig(listeners = UISharedContactsForm.SelectGroupActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UISharedContactsForm.CancelActionListener.class, phase = Phase.DECODE)
      }
  ),
  @ComponentConfig(
                   id = "UIPopupWindowUserSelect",
                   type = UIPopupWindow.class,
                   template =  "system:/groovy/webui/core/UIPopupWindow.gtmpl",
                   events = {
                     @EventConfig(listeners = UIPopupWindow.CloseActionListener.class, name = "ClosePopup")  ,
                     @EventConfig(listeners = UISharedContactsForm.AddActionListener.class, name = "Add", phase = Phase.DECODE),
                     @EventConfig(listeners = UISharedContactsForm.CloseActionListener.class, name = "Close", phase = Phase.DECODE)
                   }
  )
})

public class UISharedContactsForm extends UIForm implements UIPopupComponent, UISelector {
  final static public String FIELD_CONTACT = "contactName".intern() ;  
  final static public String FIELD_USER = "user".intern() ;
  final static public String FIELD_GROUP = "group".intern() ;  
  final static public String FIELD_EDIT_PERMISSION = "canEdit".intern() ;
  private Map<String, String> permissionUser_ = new LinkedHashMap<String, String>() ;
  private Map<String, String> permissionGroup_ = new LinkedHashMap<String, String>() ;
  private Map<String, Contact> sharedContacts = new LinkedHashMap<String, Contact>() ;
  public UISharedContactsForm() { }
  
  public void init(Map<String, Contact> contacts) throws Exception {
    UIFormInputWithActions inputset = new UIFormInputWithActions("UIInputUserSelect") ;
    sharedContacts = contacts ; 
    StringBuffer buffer = new StringBuffer() ;
    // cs-1805
    int length = 0 ;    
    for (Contact contact : contacts.values()) {
      if (length > 50) {
        buffer.append("<br/>") ;
        length = 0 ;
      }
      if (buffer.length() > 0) buffer.append(", ") ;
      String fullName = contact.getFullName() ;
      if (ContactUtils.isEmpty(fullName))  {
        buffer.append(ContactUtils.emptyName()) ;
        length+=ContactUtils.emptyName().length() ;
      }
      else {
        buffer.append(ContactUtils.encodeHTML(fullName)) ;
        length += fullName.length() ;
      }
    }
    UIFormInputInfo inputInfo = new UIFormInputInfo(FIELD_CONTACT, FIELD_CONTACT, null) ;
    inputInfo.setValue(buffer.toString()) ;
    inputset.addChild(inputInfo) ;
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
    
    UIFormStringInput group = new UIFormStringInput(FIELD_GROUP, FIELD_GROUP, null) ;
    //group.setEditable(false) ;
    inputset.addUIFormInput(group) ;
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
    addChild(inputset) ;    
  }

  public String getLabel(String id) {
    try {
      return super.getLabel(id) ;
    } catch (Exception e) {
      return id ;
    }
  }

  public String[] getActions() { return new String[] { "Save", "Cancel" } ; }
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}

  public void updateSelect(String selectField, String value) throws Exception {
    UIFormStringInput fieldInput = getUIStringInput(selectField) ;    
    ContactUtils.updateSelect(fieldInput, selectField, value);
  } 
  
  static  public class SaveActionListener extends EventListener<UISharedContactsForm> {
    @SuppressWarnings("unchecked")
  public void execute(Event<UISharedContactsForm> event) throws Exception {
      UISharedContactsForm uiForm = event.getSource() ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      String names = uiForm.getUIStringInput(FIELD_USER).getValue() ;
      String groups = uiForm.getUIStringInput(FIELD_GROUP).getValue() ;
      Map<String, String> receiverUserByGroup = new LinkedHashMap<String, String>() ;
      Map<String, String> receiverUser = new LinkedHashMap<String, String>() ;
      if(ContactUtils.isEmpty(names) && ContactUtils.isEmpty(groups)) {        
        uiApp.addMessage(new ApplicationMessage("UISharedContactsForm.msg.empty-username", null,
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } 
      String username = ContactUtils.getCurrentUser() ;
      if(!ContactUtils.isEmpty(names)) {
        OrganizationService organizationService = 
          (OrganizationService)PortalContainer.getComponent(OrganizationService.class) ;
        StringBuilder invalidUsers = new StringBuilder() ;
        String[] array = names.split(",") ;
        for(String name : array) {
          if (organizationService.getUserHandler().findUserByName(name.trim()) != null) {
            if (!name.trim().equals(username)) {
              receiverUser.put(name.trim() + DataStorage.HYPHEN, name.trim() + DataStorage.HYPHEN) ;
            }
          } else {
            if (invalidUsers.length() == 0) invalidUsers.append(name) ;
            else invalidUsers.append(", " + name) ;
          }
        }
        if (invalidUsers.length() > 0) {
          uiApp.addMessage(new ApplicationMessage("UISharedContactsForm.msg.not-exist-username"
              , new Object[]{invalidUsers.toString()}, 1 )) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;          
        }      
      }
      
      /*
      if(!ContactUtils.isEmpty(names)) {
        OrganizationService organizationService = 
          (OrganizationService)PortalContainer.getComponent(OrganizationService.class) ;
        try {
          String[] array = names.split(",") ;
          for(String name : array) {
            organizationService.getUserHandler().findUserByName(name.trim()).getFullName();
            receiverUser.put(name.trim() + JCRDataStorage.HYPHEN, name.trim() + JCRDataStorage.HYPHEN) ;
          }         
        } catch (NullPointerException e) {
          uiApp.addMessage(new ApplicationMessage("UISharedContactsForm.msg.not-exist-username", null,
              ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
      }
      */
      // CS-2604
    /*  if(!ContactUtils.isEmpty(groups)) {
        OrganizationService organizationService = 
          (OrganizationService)PortalContainer.getComponent(OrganizationService.class) ;
        try {
          String[] array = groups.split(",") ;
          for(String name : array) {
            organizationService.getGroupHandler().findGroupById(name).getGroupName() ;
          }         
        } catch (NullPointerException e) {
          uiApp.addMessage(new ApplicationMessage("UISharedContactsForm.msg.not-exist-group", null,
              ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
      }
      */
      if(!ContactUtils.isEmpty(groups)) {
        OrganizationService organizationService = 
          (OrganizationService)PortalContainer.getComponent(OrganizationService.class) ;
        StringBuilder invalidGroups = new StringBuilder() ;
        String[] array = groups.split(",") ;
        for(String name : array) {
          if (organizationService.getGroupHandler().findGroupById(name.trim()) == null) {
            if (invalidGroups.length() == 0) invalidGroups.append(name) ;
            else invalidGroups.append(", " + name) ;
          }
        }
        if (invalidGroups.length() > 0) {
          uiApp.addMessage(new ApplicationMessage("UISharedContactsForm.msg.not-exist-group"
              , new Object[]{invalidGroups.toString()}, 1 )) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;          
        }      
      }
      
      ContactService contactService = ContactUtils.getContactService() ;  
      Map<String, String> viewMapGroups = new LinkedHashMap<String, String>() ;
      if (!ContactUtils.isEmpty(groups)) {
        String[] arrayGroups = groups.split(",") ; 
        for (String group : arrayGroups) {
          viewMapGroups.put(group.trim(), group.trim()) ;
          OrganizationService organizationService = 
                (OrganizationService)PortalContainer.getComponent(OrganizationService.class) ;
          List<User> users = organizationService.getUserHandler().findUsersByGroup(group.trim()).getAll() ;
          for (User user : users) {
            receiverUserByGroup.put(user.getUserName() + DataStorage.HYPHEN, user.getUserName() + DataStorage.HYPHEN) ;
          }
        }        
      } 
      receiverUser.remove(username + DataStorage.HYPHEN) ;
      receiverUserByGroup.remove(username + DataStorage.HYPHEN) ;
      if (receiverUser.size() > 0 || !ContactUtils.isEmpty(groups)) {
        Map<String, String> viewMapUsers = new LinkedHashMap<String, String>() ;
        for (String user : receiverUser.keySet()) viewMapUsers.put(user, user) ;
        
        Map<String, String> editMapUsers = new LinkedHashMap<String, String>() ; 
        Map<String, String> editMapGroups = new LinkedHashMap<String, String>() ;
        if (uiForm.getUIFormCheckBoxInput(ContactUtils.FIELD_EDIT_PERMISSION).isChecked()) {
          for (String user : receiverUser.keySet()) editMapUsers.put(user, user) ;          
          editMapGroups.putAll(viewMapGroups) ;
        }
        for (Contact contact : uiForm.sharedContacts.values()) {
          String[] viewPer = contact.getViewPermissionUsers() ;
          Map<String, String> newViewMapUsers = new LinkedHashMap<String, String>() ;
          newViewMapUsers.putAll(viewMapUsers) ;
          if (viewPer != null)
            for (String view : viewPer) newViewMapUsers.put(view, view) ;
          String[] editPer = contact.getEditPermissionUsers() ;
          Map<String, String> newEditMapUsers = new LinkedHashMap<String, String>() ;
          newEditMapUsers.putAll(editMapUsers) ;
          if (editPer != null)
            for (String edit : editPer) newEditMapUsers.put(edit, edit) ; 
          
          Map<String, String> newViewMapGroups = new LinkedHashMap<String, String>() ;
          newViewMapGroups.putAll(viewMapGroups) ;
          String[] viewPerGroup = contact.getViewPermissionGroups() ;
          if (viewPerGroup != null) 
            for (String view : viewPerGroup) newViewMapGroups.put(view, view) ;
          String[] editPerGroup = contact.getEditPermissionGroups() ;
          Map<String, String> newEditMapGroups = new LinkedHashMap<String, String>() ;
          newEditMapGroups.putAll(editMapGroups) ;
          if (editPerGroup != null)
            for (String edit : editPerGroup) newEditMapGroups.put(edit, edit) ; 
          
          // cs-1777
          if (!uiForm.getUIFormCheckBoxInput(ContactUtils.FIELD_EDIT_PERMISSION).isChecked()) {
            for (String user : receiverUser.keySet()) newEditMapUsers.remove(user) ;
            if (!ContactUtils.isEmpty(groups)) {
              String[] arrayGroups = groups.split(",") ;
              for (String group : arrayGroups) newEditMapGroups.remove(group) ;
            }
          }            

          contact.setViewPermissionUsers(newViewMapUsers.keySet().toArray(new String[] {})) ;
          contact.setEditPermissionUsers(newEditMapUsers.keySet().toArray(new String[] {})) ;
          contact.setViewPermissionGroups(newViewMapGroups.keySet().toArray(new String[] {})) ;
          contact.setEditPermissionGroups(newEditMapGroups.keySet().toArray(new String[] {})) ;
          //TODO cs-2481
          try {
            contactService.saveContact(username, contact, false) ;
          } catch (PathNotFoundException e) {
            uiApp.addMessage(new ApplicationMessage("UISharedContactsForm.msg.deleted-contact", null,
                ApplicationMessage.WARNING)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return ;
          }
        }
        String[] contactIds = uiForm.sharedContacts.keySet().toArray(new String[]{}) ;
        for (String user : receiverUserByGroup.keySet()) receiverUser.put(user, user) ;
        contactService.shareContact(username, contactIds, Arrays.asList(receiverUser.keySet().toArray(new String[] {}))) ; 
        //contactService.shareContact(SessionProviderFactory.createSessionProvider(), username, contactIds, receiverUserByGroup) ;
        uiApp.addMessage(new ApplicationMessage("UISharedContactsForm.msg.contacts-shared", null)) ;
        UIContactPortlet contactPortlet = uiForm.getAncestorOfType(UIContactPortlet.class) ;
        contactPortlet.cancelAction() ;
      } else if (ContactUtils.isEmpty(groups)){
        uiApp.addMessage(new ApplicationMessage("UISharedContactsForm.msg.invalid-username", null)) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      return ;      
    }
  }
  static  public class SelectPermissionActionListener extends EventListener<UISharedContactsForm> {
    public void execute(Event<UISharedContactsForm> event) throws Exception {
      UISharedContactsForm uiForm = event.getSource() ;
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
      } else {
        UIPopupWindow uiPopup = uiForm.addChild(UIPopupWindow.class, null, "UIPopupGroupSelector");
        uiPopup.setWindowSize(540, 0);
        UIGroupSelector uiGroup = uiForm.createUIComponent(UIGroupSelector.class, null, null);
        uiPopup.setUIComponent(uiGroup);
        uiGroup.setId("UIGroupSelector");
        uiGroup.getChild(UITree.class).setId("TreeGroupSelector");
        uiGroup.getChild(UIBreadcumbs.class).setId("BreadcumbsGroupSelector");
        uiForm.getChild(UIPopupWindow.class).setShow(true);  
        event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ; 
      }
    }
  }
  
  static  public class SelectGroupActionListener extends EventListener<UIGroupSelector> {   
    public void execute(Event<UIGroupSelector> event) throws Exception {
      UIGroupSelector uiForm = event.getSource() ;
      String user = event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiForm.getAncestorOfType(UISharedContactsForm.class).updateSelect(UISharedContactsForm.FIELD_GROUP, user) ;      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ; 
    }
  }
  
  static  public class AddActionListener extends EventListener<UIUserSelector> {
    public void execute(Event<UIUserSelector> event) throws Exception {
      UIUserSelector uiUserSelector = event.getSource();
      UIPopupContainer uiContainer = uiUserSelector.getAncestorOfType(UIPopupContainer.class) ;
      UISharedContactsForm uiForm = uiContainer.findFirstComponentOfType(UISharedContactsForm.class);
      String values = uiUserSelector.getSelectedUsers();
      if (values == null) return ;
      for (String value : values.split(","))
        uiForm.updateSelect(FIELD_USER, value.trim()) ; 
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
  
  static  public class CancelActionListener extends EventListener<UISharedContactsForm> {
    public void execute(Event<UISharedContactsForm> event) throws Exception {
      UISharedContactsForm uiForm = event.getSource() ;
      UIContactPortlet contactPortlet = uiForm.getAncestorOfType(UIContactPortlet.class) ;
      contactPortlet.cancelAction() ;
    }
  }

}
