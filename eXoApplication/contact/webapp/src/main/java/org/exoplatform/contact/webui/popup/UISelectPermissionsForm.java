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

import java.util.LinkedHashMap;
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
import org.exoplatform.webui.core.UIBreadcumbs;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UITree;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormStringInput;
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

    inputset = ContactUtils.initSelectPermissions(inputset);
    addChild(inputset) ;  
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
    ContactUtils.updateSelect(fieldInput, selectField, value);
  } 
  
  static  public class SaveActionListener extends EventListener<UISelectPermissionsForm> {
    @SuppressWarnings("unchecked")
    public void execute(Event<UISelectPermissionsForm> event) throws Exception {
      UISelectPermissionsForm uiForm = event.getSource() ;
      String names = uiForm.getUIStringInput(ContactUtils.FIELD_USER).getValue() ;
      String groups = uiForm.getUIStringInput(ContactUtils.FIELD_GROUP).getValue() ;
      Map<String, String> receiveUsers = new LinkedHashMap<String, String>() ;
      Map<String, String> receiveGroups = new LinkedHashMap<String, String>() ;      
      if(ContactUtils.isEmpty(names) && ContactUtils.isEmpty(groups)) {        
        event.getRequestContext()
             .getUIApplication()
             .addMessage(new ApplicationMessage("UISelectPermissionsForm.msg.empty-username", null, ApplicationMessage.WARNING));
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
          event.getRequestContext()
          .getUIApplication().addMessage(new ApplicationMessage("UISelectPermissionsForm.msg.not-exist-username"
              , new Object[]{invalidUsers.toString()},  ApplicationMessage.WARNING )) ;
          
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
          event.getRequestContext()
               .getUIApplication()
               .addMessage(new ApplicationMessage("UISelectPermissionsForm.msg.not-exist-group",
                                                  new Object[] { invalidGroups.toString() },
                                                  ApplicationMessage.WARNING));
          
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
        event.getRequestContext()
             .getUIApplication()
             .addMessage(new ApplicationMessage("UISelectPermissionsForm.msg.shared-yourself", null, ApplicationMessage.WARNING));
        
        return ;
      }
      AddressBook contactGroup = uiForm.group_ ;
      if(uiForm.getUIFormCheckBoxInput(ContactUtils.FIELD_EDIT_PERMISSION).isChecked()) {
        contactGroup = ContactUtils.setEditPermissionAddress(contactGroup, receiveUsers, receiveGroups);   
      } else {
        ContactUtils.removeEditPermissionAddress(contactGroup, receiveUsers, receiveGroups);
      }
      contactGroup = ContactUtils.setViewPermissionAddress(contactGroup, receiveUsers, receiveGroups);

      if (!uiForm.getUIFormCheckBoxInput(ContactUtils.FIELD_EDIT_PERMISSION).isChecked()) {
        contactGroup = ContactUtils.removeEditPermissionAddress(contactGroup, receiveUsers, receiveGroups);             
      } else {
        ContactUtils.setEditPermissionAddress(contactGroup, receiveUsers, receiveGroups);
      }

      contactService.savePublicAddressBook(contactGroup, false) ;
      UIPublicAddressPermission uiAddEdit = uiForm.getParent() ;          
      uiAddEdit.updateGroupGrid(contactGroup);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAddEdit.getChild(UIGrid.class)) ;          
      event.getRequestContext().addUIComponentToUpdateByAjax(
          uiForm.getAncestorOfType(UIContactPortlet.class).findFirstComponentOfType(UIAddressBooks.class)) ;
      uiForm.getUIStringInput(ContactUtils.FIELD_USER).setValue(null) ;
      uiForm.getUIStringInput(ContactUtils.FIELD_GROUP).setValue(null) ;
      uiForm.getUIFormCheckBoxInput(ContactUtils.FIELD_EDIT_PERMISSION).setChecked(false) ;  
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
        uiForm.updateSelect(ContactUtils.FIELD_USER, value.trim()) ; 
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
      uiSelectPermissionsForm.updateSelect(ContactUtils.FIELD_GROUP, groupId + Utils.COLON + Utils.MEMBERSHIP +  membership) ;
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
