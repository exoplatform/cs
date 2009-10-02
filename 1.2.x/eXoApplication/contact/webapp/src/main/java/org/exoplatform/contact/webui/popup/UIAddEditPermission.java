/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.jcr.PathNotFoundException;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.AddressBook;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.impl.JCRDataStorage;
import org.exoplatform.contact.webui.UIAddressBooks;
import org.exoplatform.contact.webui.UIContactPortlet;
import org.exoplatform.contact.webui.UIContacts;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.impl.GroupImpl;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SAS
 * Author : Ha Mai
 *          ha.mai@exoplatform.com
 * Feb 27, 2008  
 */
@ComponentConfig (
    lifecycle = UIContainerLifecycle.class, 
    events = {
      @EventConfig(listeners = UIAddEditPermission.EditActionListener.class),
      @EventConfig(listeners = UIAddEditPermission.DeleteActionListener.class, confirm = "UIAddEditPermission.msg.confirm-delete")
    }
)

public class UIAddEditPermission extends UIContainer implements UIPopupComponent {
  public static String[]  BEAN_FIELD = {"viewPermission","editPermission"} ;
  private static String[] ACTION = {"Edit", "Delete"} ;
  private String groupId_ ;
  private String contactId_ ;
  private boolean isSharedGroup ;
  
  public UIAddEditPermission() throws Exception {
    this.setName("UIAddEditPermission");
    UIGrid permissionList = addChild(UIGrid.class, null, "PermissionList") ;
    permissionList.configure("viewPermission", BEAN_FIELD, ACTION);
    permissionList.getUIPageIterator().setId("PermissionListIterator") ;
    addChild(UISharedForm.class, null, null) ;
  }
  public void activate() throws Exception { }
  public void deActivate() throws Exception { }

  public void initGroup(AddressBook group) throws Exception{
    UISharedForm shareForm = getChild(UISharedForm.class) ;
    shareForm.setGroup(group) ;
    shareForm.init() ; 
    updateGroupGrid(group) ;
    groupId_ = group.getId() ;
    isSharedGroup = true ;
  }
  public void initContact(Contact contact) throws Exception{
    UISharedForm shareForm = getChild(UISharedForm.class) ;
    shareForm.setContact(contact) ;
    shareForm.init() ; 
    updateContactGrid(contact) ;
    contactId_ = contact.getId() ;
    isSharedGroup = false ;
  }
    
  public void updateContactGrid(Contact contact) throws Exception {
    List<data> dataRow = new ArrayList<data>() ;
    if(contact.getViewPermissionUsers() != null) {
      for(String username : contact.getViewPermissionUsers() ) {
        dataRow.add(new data(username, (contact.getEditPermissionUsers()!= null && Arrays.asList(contact.getEditPermissionUsers()).contains(username)))) ;
      }
    }    
    if(contact.getViewPermissionGroups() != null) {
      for(String username : contact.getViewPermissionGroups() ) {
        dataRow.add(new data(username, (contact.getEditPermissionGroups()!= null && Arrays.asList(contact.getEditPermissionGroups()).contains(username)))) ;
      }
    }
    
    UIGrid permissionList = getChild(UIGrid.class) ;
    // cs-1702
    int currentPage = 1 ;
    try {
      currentPage = permissionList.getUIPageIterator().getPageList().getCurrentPage() ;
    } catch (NullPointerException e) { }
    ObjectPageList objPageList = new ObjectPageList(dataRow, 10) ;
    permissionList.getUIPageIterator().setPageList(objPageList) ;
    if (currentPage > 1) {
      permissionList.getUIPageIterator().setCurrentPage(currentPage) ;
    }
    getChild(UISharedForm.class).setContact(contact) ;
  }
  
  public void updateGroupGrid(AddressBook group) throws Exception {
    List<data> dataRow = new ArrayList<data>() ;
    if(group.getViewPermissionUsers() != null) {
      for(String username : group.getViewPermissionUsers() ) {
        dataRow.add(new data(username, (group.getEditPermissionUsers()!= null && Arrays.asList(group.getEditPermissionUsers()).contains(username)))) ;
      }
    }
    if(group.getViewPermissionGroups() != null) {
      for(String groupId : group.getViewPermissionGroups() ) {
        dataRow.add(new data(groupId, (group.getEditPermissionGroups()!= null && Arrays.asList(group.getEditPermissionGroups()).contains(groupId)))) ;
      }
    }
    UIGrid permissionList = getChild(UIGrid.class) ;
//  cs-1702
    int currentPage = 1 ;
    try {
      currentPage = permissionList.getUIPageIterator().getPageList().getCurrentPage() ;
    } catch (NullPointerException e) { }
    ObjectPageList objPageList = new ObjectPageList(dataRow, 10) ;
    permissionList.getUIPageIterator().setPageList(objPageList) ;
    if (currentPage > 1) {
      permissionList.getUIPageIterator().setCurrentPage(currentPage) ;
    }
    getChild(UISharedForm.class).setGroup(group) ;
  }

  static public class EditActionListener extends EventListener<UIAddEditPermission> {
    public void execute(Event<UIAddEditPermission> event) throws Exception {
      UIAddEditPermission addEdit = event.getSource();
      String reciever = event.getRequestContext().getRequestParameter(OBJECTID);
      UISharedForm shareForm = addEdit.getChild(UISharedForm.class);
      shareForm.setNew(false) ;
      UIFormStringInput uiStringInput = shareForm.getUIStringInput(UISharedForm.FIELD_USER) ;
      uiStringInput.setValue(reciever) ;
      uiStringInput.setEditable(false) ;
      if (addEdit.isSharedGroup) {
        AddressBook group = ContactUtils.getContactService().getPersonalAddressBook(
            ContactUtils.getCurrentUser(), addEdit.groupId_) ;        
        shareForm.setGroup(group) ;
        if (group.getViewPermissionGroups() != null && Arrays.asList(group.getViewPermissionGroups()).contains(reciever)) {
          shareForm.getUIStringInput(UISharedForm.FIELD_GROUP).setValue(reciever) ;
          shareForm.getUIStringInput(UISharedForm.FIELD_USER).setValue(null) ;
          shareForm.getUIFormCheckBoxInput(UISharedForm.FIELD_EDIT_PERMISSION).setChecked(
              (group.getEditPermissionGroups() != null) && Arrays.asList(group.getEditPermissionGroups()).contains(reciever)) ;
        } else {
          shareForm.getUIStringInput(UISharedForm.FIELD_USER).setValue(reciever) ;
          shareForm.getUIStringInput(UISharedForm.FIELD_GROUP).setValue(null) ;
          shareForm.getUIFormCheckBoxInput(UISharedForm.FIELD_EDIT_PERMISSION).setChecked((group.getEditPermissionUsers()
              != null) && Arrays.asList(group.getEditPermissionUsers()).contains(reciever + JCRDataStorage.HYPHEN)) ;
        }
      } else {
        Contact contact = ContactUtils.getContactService().getContact(
            ContactUtils.getCurrentUser(), addEdit.contactId_) ;        
        shareForm.setContact(contact) ;
        if (contact.getViewPermissionGroups() != null && Arrays.asList(contact.getViewPermissionGroups()).contains(reciever)) {
          shareForm.getUIStringInput(UISharedForm.FIELD_GROUP).setValue(reciever) ;
          shareForm.getUIStringInput(UISharedForm.FIELD_USER).setValue(null) ;
          shareForm.getUIFormCheckBoxInput(UISharedForm.FIELD_EDIT_PERMISSION).setChecked(
              (contact.getEditPermissionGroups() != null) && Arrays.asList(contact.getEditPermissionGroups()).contains(reciever)) ;
        } else {
          shareForm.getUIStringInput(UISharedForm.FIELD_USER).setValue(reciever) ;
          shareForm.getUIStringInput(UISharedForm.FIELD_GROUP).setValue(null) ;
          shareForm.getUIFormCheckBoxInput(UISharedForm.FIELD_EDIT_PERMISSION).setChecked((contact.getEditPermissionUsers()
              != null) && Arrays.asList(contact.getEditPermissionUsers()).contains(reciever + JCRDataStorage.HYPHEN)) ;
        }
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(shareForm) ;
    }
  }
  static public class DeleteActionListener extends EventListener<UIAddEditPermission> {
    @SuppressWarnings("unchecked")
  public void execute(Event<UIAddEditPermission> event) throws Exception {
      UIAddEditPermission uiForm = event.getSource();
      String remover = event.getRequestContext().getRequestParameter(OBJECTID);
      ContactService contactService = ContactUtils.getContactService();
      String username = ContactUtils.getCurrentUser() ;      
      if (uiForm.isSharedGroup) {
        AddressBook group = contactService.getPersonalAddressBook(
            username, uiForm.groupId_) ;
        if (group.getViewPermissionGroups() != null && Arrays.asList(group.getViewPermissionGroups()).contains(remover)) {
          List<String> newPerms = new ArrayList<String>() ;
          newPerms.addAll(Arrays.asList(group.getViewPermissionGroups())) ;
          newPerms.remove(remover) ;
          group.setViewPermissionGroups(newPerms.toArray(new String[newPerms.size()])) ;
          if(group.getEditPermissionGroups() != null) {
            newPerms.clear() ;
            newPerms.addAll(Arrays.asList(group.getEditPermissionGroups())) ;
            newPerms.remove(remover) ;
            group.setEditPermissionGroups(newPerms.toArray(new String[newPerms.size()])) ;
          }
          
          OrganizationService organizationService = 
              (OrganizationService)PortalContainer.getComponent(OrganizationService.class) ;
          List<User> users = organizationService.getUserHandler().findUsersByGroup(remover).getAll() ;
          List<String> viewUsers = new ArrayList<String>() ;
          if (group.getViewPermissionUsers() != null) {
            viewUsers = Arrays.asList(group.getViewPermissionUsers()) ;
          }
//        
          for (User user : users) {
            boolean deleteShared = true ;
            if (!viewUsers.contains(user.getUserName() + JCRDataStorage.HYPHEN)) {
              Object[] groups = organizationService.getGroupHandler().findGroupsOfUser(user.getUserName()).toArray() ;
              for (Object object : groups) {
                if (Arrays.asList(group.getViewPermissionGroups()).contains(((GroupImpl)object).getId())) {
                  deleteShared = false ;
                  break ;
                }               
              } 
              if (deleteShared) {
                contactService.unshareAddressBook(
                  username, uiForm.groupId_, user.getUserName()) ;
              }
            }
          }
        } else {
          if(group.getViewPermissionUsers() != null) {
            List<String> newPerms = new ArrayList<String>() ;
            newPerms.addAll(Arrays.asList(group.getViewPermissionUsers())) ;
            newPerms.remove(remover + JCRDataStorage.HYPHEN) ;
            group.setViewPermissionUsers(newPerms.toArray(new String[newPerms.size()])) ;
          }
          if(group.getEditPermissionUsers() != null) {
            List<String> newPerms = new ArrayList<String>() ;
            newPerms.addAll(Arrays.asList(group.getEditPermissionUsers())) ;
            newPerms.remove(remover + JCRDataStorage.HYPHEN) ;
            group.setEditPermissionUsers(newPerms.toArray(new String[newPerms.size()])) ;
          }        
          // add to fix bug cs-1592
          OrganizationService organizationService = 
            (OrganizationService)PortalContainer.getComponent(OrganizationService.class) ;
          boolean sharedByGroup = false ;
          if (group.getViewPermissionGroups() != null) {
            Object[] groups = organizationService.getGroupHandler().findGroupsOfUser(remover).toArray() ;
            for (Object object : groups) {
              if (Arrays.asList(group.getViewPermissionGroups()).contains(((GroupImpl)object).getId())) {
                sharedByGroup = true ;
                break ;
              }               
            }
          }
          if (!sharedByGroup)
            contactService.unshareAddressBook(username
              , uiForm.groupId_, remover) ;
        }
        contactService.saveAddressBook(username, group, false) ;
        uiForm.updateGroupGrid(group); 
        event.getRequestContext().addUIComponentToUpdateByAjax(
            uiForm.getAncestorOfType(UIContactPortlet.class).findFirstComponentOfType(UIAddressBooks.class)) ;
      } else {
        Contact contact = contactService.getContact(
            username, uiForm.contactId_) ;
        if (contact.getViewPermissionGroups() != null && Arrays.asList(contact.getViewPermissionGroups()).contains(remover)) {
          removePerGroup(contact, remover) ;
          OrganizationService organizationService = 
              (OrganizationService)PortalContainer.getComponent(OrganizationService.class) ;
          List<User> users = organizationService.getUserHandler().findUsersByGroup(remover).getAll() ;
          List<String> viewUsers = new ArrayList<String>() ;
          if (contact.getViewPermissionUsers() != null) {
            viewUsers = Arrays.asList(contact.getViewPermissionUsers()) ;
          }
//        
          for (User user : users) {
            boolean deleteShared = true ;
            if (!viewUsers.contains(user.getUserName() + JCRDataStorage.HYPHEN)) {
              Object[] groups = organizationService.getGroupHandler().findGroupsOfUser(user.getUserName()).toArray() ;
              for (Object object : groups) {
                if (Arrays.asList(contact.getViewPermissionGroups()).contains(((GroupImpl)object).getId())) {
                  deleteShared = false ;
                  break ;
                }               
              } 
              if (deleteShared) {
                try {
                  contactService.removeUserShareContact(
                    SessionProviderFactory.createSystemProvider(), username, uiForm.contactId_, user.getUserName()) ;
                  } catch (PathNotFoundException e) {
                  UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
                  uiApp.addMessage(new ApplicationMessage("UIAddEditPermission.msg.cannot-deleteShared", null,
                    ApplicationMessage.WARNING)) ;
                  event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
                  return ;
                  }
              }
            }
          }
        } else {
          removePerUser(contact, remover + JCRDataStorage.HYPHEN) ;
          try {
//          add to fix bug cs-1592
            OrganizationService organizationService = 
              (OrganizationService)PortalContainer.getComponent(OrganizationService.class) ;
            boolean sharedByGroup = false ;
            if (contact.getViewPermissionGroups() != null) {
              Object[] groups = organizationService.getGroupHandler().findGroupsOfUser(remover).toArray() ;
              for (Object object : groups) {
                if (Arrays.asList(contact.getViewPermissionGroups()).contains(((GroupImpl)object).getId())) {
                  sharedByGroup = true ;
                  break ;
                }               
              }
            }
            if (!sharedByGroup)
              contactService.removeUserShareContact(SessionProviderFactory.createSystemProvider()
                  , username, uiForm.contactId_, remover) ;            
          } catch (PathNotFoundException e) { }
        }        
        contactService.saveContact(username, contact, false) ;
        uiForm.updateContactGrid(contact);
        
        UIContacts uiContacts = uiForm
          .getAncestorOfType(UIContactPortlet.class).findFirstComponentOfType(UIContacts.class) ;
        if (uiContacts.isDisplaySearchResult()) {
          uiContacts.getContactMap().put(contact.getId(), contact) ;
        } else {
          uiContacts.updateList() ;
        } 
        event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts) ;
      }
      UISharedForm uiSharedForm = uiForm.getChild(UISharedForm.class) ;
      if (!uiSharedForm.isNew()) {
        UIFormStringInput uiStringInput = uiSharedForm.getUIStringInput(UISharedForm.FIELD_USER) ;
        if (uiStringInput.getValue() != null && uiStringInput.getValue().equals(remover)) {
          uiStringInput = uiSharedForm.getUIStringInput(UISharedForm.FIELD_USER) ;
          uiStringInput.setValue(null) ;
          uiStringInput.setEditable(true) ;
          uiSharedForm.getUIFormCheckBoxInput(UISharedForm.FIELD_EDIT_PERMISSION).setChecked(false) ;
          uiSharedForm.setNew(true) ;          
        } else {
          uiStringInput = uiSharedForm.getUIStringInput(UISharedForm.FIELD_GROUP) ;  
          if (uiStringInput.getValue() != null && uiStringInput.getValue().equals(remover)) {
            uiStringInput = uiSharedForm.getUIStringInput(UISharedForm.FIELD_GROUP) ;
            uiStringInput.setValue(null) ;
            uiSharedForm.getUIFormCheckBoxInput(UISharedForm.FIELD_EDIT_PERMISSION).setChecked(false) ;
            uiSharedForm.setNew(true) ;          
          }
        }
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
    }
    private void removePerUser(Contact contact, String removedUser) {
      if(contact.getViewPermissionUsers() != null) {
        List<String> newPerms = new ArrayList<String>() ;
        newPerms.addAll(Arrays.asList(contact.getViewPermissionUsers())) ;
        newPerms.remove(removedUser) ;
        contact.setViewPermissionUsers(newPerms.toArray(new String[newPerms.size()])) ;
      }
      if(contact.getEditPermissionUsers() != null) {
        List<String> newPerms = new ArrayList<String>() ;
        newPerms.addAll(Arrays.asList(contact.getEditPermissionUsers())) ;
        newPerms.remove(removedUser) ;
        contact.setEditPermissionUsers(newPerms.toArray(new String[newPerms.size()])) ;
      }
    }
    private void removePerGroup(Contact contact, String removedGroup) {
      if(contact.getViewPermissionGroups() != null) {
        List<String> newPerms = new ArrayList<String>() ;
        newPerms.addAll(Arrays.asList(contact.getViewPermissionGroups())) ;
        newPerms.remove(removedGroup) ;
        contact.setViewPermissionGroups(newPerms.toArray(new String[newPerms.size()])) ;
      }
      if(contact.getEditPermissionGroups() != null) {
        List<String> newPerms = new ArrayList<String>() ;
        newPerms.addAll(Arrays.asList(contact.getEditPermissionGroups())) ;
        newPerms.remove(removedGroup) ;
        contact.setEditPermissionGroups(newPerms.toArray(new String[newPerms.size()])) ;
      }
    }
  }
  public class data {
    String viewPermission = null ;
    String editPermission = null ;

    public  String getViewPermission() {return viewPermission ;}
    public  String getEditPermission() {
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
      ResourceBundle res = context.getApplicationResourceBundle() ;
      try {
        if (editPermission != null && editPermission.equalsIgnoreCase("true")) {
          return  res.getString("UIAddEditPermission.label.true");
        } else {
          return res.getString("UIAddEditPermission.label.false");
        }
      } catch (MissingResourceException e) {      
        e.printStackTrace() ;
        return editPermission ;
      }
    }
      
    public data(String username, boolean canEdit) throws Exception {
      viewPermission = username.replaceFirst(JCRDataStorage.HYPHEN, "") ;
      String edit = String.valueOf(canEdit) ;
      editPermission = edit.replaceFirst(JCRDataStorage.HYPHEN, "") ;
    }
  }
}
