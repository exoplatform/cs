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

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.impl.JCRDataStorage;
import org.exoplatform.contact.webui.UIAddressBooks;
import org.exoplatform.contact.webui.UIContactPortlet;
import org.exoplatform.contact.webui.UIContacts;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
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
    ///shareForm.init(null, cal, true);
  }
  public void activate() throws Exception { }
  public void deActivate() throws Exception { }

  public void initGroup(ContactGroup group) throws Exception{
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
    ObjectPageList objPageList = new ObjectPageList(dataRow, 10) ;
    permissionList.getUIPageIterator().setPageList(objPageList) ;
    getChild(UISharedForm.class).setContact(contact) ;
  }
  
  public void updateGroupGrid(ContactGroup group) throws Exception {
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
    ObjectPageList objPageList = new ObjectPageList(dataRow, 10) ;
    permissionList.getUIPageIterator().setPageList(objPageList) ;
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
        ContactGroup group = ContactUtils.getContactService().getGroup(
            SessionProviderFactory.createSessionProvider(), ContactUtils.getCurrentUser(), addEdit.groupId_) ;        
        shareForm.setGroup(group) ;
        if (Arrays.asList(group.getViewPermissionGroups()).contains(reciever)) {
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
            SessionProviderFactory.createSessionProvider(), ContactUtils.getCurrentUser(), addEdit.contactId_) ;        
        shareForm.setContact(contact) ;
        if (Arrays.asList(contact.getViewPermissionGroups()).contains(reciever)) {
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
    public void execute(Event<UIAddEditPermission> event) throws Exception {
      UIAddEditPermission uiForm = event.getSource();
      String remover = event.getRequestContext().getRequestParameter(OBJECTID);
      ContactService contactService = ContactUtils.getContactService();
      String username = ContactUtils.getCurrentUser() ;      
      if (uiForm.isSharedGroup) {
        ContactGroup group = contactService.getGroup(
            SessionProviderFactory.createSessionProvider(), username, uiForm.groupId_) ;
        
        // delete group permission
        if (Arrays.asList(group.getViewPermissionGroups()).contains(remover)) {
          if(group.getViewPermissionGroups() != null) {
            List<String> newPerms = new ArrayList<String>() ;
            for(String s : group.getViewPermissionGroups()) {
              if(!s.equals(remover)) {
                newPerms.add(s) ;
              }
            }
            group.setViewPermissionGroups(newPerms.toArray(new String[newPerms.size()])) ;
          }
          if(group.getEditPermissionGroups() != null) {
            List<String> newPerms = new ArrayList<String>() ;
            for(String s : group.getEditPermissionGroups()) {
              if(!s.equals(remover)) {
                newPerms.add(s) ;
              }
            }
            group.setEditPermissionGroups(newPerms.toArray(new String[newPerms.size()])) ;
          }        
          List<Contact> users = contactService
            .getPublicContactsByAddressBook(SessionProviderFactory.createSystemProvider(), remover).getAll() ;
          for (Contact user : users)
            contactService.removeUserShareAddressBook(
                SessionProviderFactory.createSessionProvider(), username, uiForm.groupId_, user.getId()) ;
          
          
          // + hyphen ?
          
          List<Contact> contacts = contactService.getContactPageListByGroup(
              SessionProviderFactory.createSessionProvider(), username, uiForm.groupId_).getAll() ;
          for (Contact contact : contacts) {
            removePerGroup(contact, remover) ;
            contactService.saveContact(SessionProviderFactory.createSessionProvider()
                , username,contact , false) ;
          }
        } else {
          if(group.getViewPermissionUsers() != null) {
            List<String> newPerms = new ArrayList<String>() ;
            for(String s : group.getViewPermissionUsers()) {
              if(!s.equals(remover + JCRDataStorage.HYPHEN)) {
                newPerms.add(s) ;
              }
            }
            group.setViewPermissionUsers(newPerms.toArray(new String[newPerms.size()])) ;
          }
          if(group.getEditPermissionUsers() != null) {
            List<String> newPerms = new ArrayList<String>() ;
            for(String s : group.getEditPermissionUsers()) {
              if(!s.equals(remover + JCRDataStorage.HYPHEN)) {
                newPerms.add(s) ;
              }
            }
            group.setEditPermissionUsers(newPerms.toArray(new String[newPerms.size()])) ;
          }        
          contactService.removeUserShareAddressBook(SessionProviderFactory.createSessionProvider()
              , username, uiForm.groupId_, remover) ;
          List<Contact> contacts = contactService.getContactPageListByGroup(
              SessionProviderFactory.createSessionProvider(), username, uiForm.groupId_).getAll() ;
          for (Contact contact : contacts) {
            
            removePerUser(contact, remover + JCRDataStorage.HYPHEN) ;
            contactService.saveContact(SessionProviderFactory.createSessionProvider()
                , username,contact , false) ;
          }
        }
        contactService.saveGroup(SessionProviderFactory.createSessionProvider(), username, group, false) ;
        uiForm.updateGroupGrid(group); 
        event.getRequestContext().addUIComponentToUpdateByAjax(
            uiForm.getAncestorOfType(UIContactPortlet.class).findFirstComponentOfType(UIAddressBooks.class)) ;
        
        UIContacts uiContacts = uiForm.getAncestorOfType(UIContactPortlet.class).findFirstComponentOfType(UIContacts.class) ;
        uiContacts.updateList() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts) ;
      } else {
        
        // ko luu ca object contact vi co the ko dung den delete va edit
        Contact contact = contactService.getContact(
            SessionProviderFactory.createSessionProvider(), username, uiForm.contactId_) ;
        if (Arrays.asList(contact.getViewPermissionGroups()).contains(remover)) {
          removePerGroup(contact, remover) ;
          List<Contact> users = contactService
            .getPublicContactsByAddressBook(SessionProviderFactory.createSystemProvider(), remover).getAll() ;
          for (Contact user : users)
            contactService.removeUserShareContact(
                SessionProviderFactory.createSessionProvider(), username, uiForm.contactId_, user.getId()) ;
        } else {
          removePerUser(contact, remover + JCRDataStorage.HYPHEN) ;
          contactService.removeUserShareContact(SessionProviderFactory.createSessionProvider()
              , username, uiForm.contactId_, remover) ;
        }        
        contactService.saveContact(SessionProviderFactory.createSessionProvider(), username, contact, false) ;
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
        for(String s : contact.getViewPermissionUsers()) {
          if(!s.equals(removedUser)) {
            newPerms.add(s) ;
          }
        }
        contact.setViewPermissionUsers(newPerms.toArray(new String[newPerms.size()])) ;
      }
      if(contact.getEditPermissionUsers() != null) {
        List<String> newPerms = new ArrayList<String>() ;
        for(String s : contact.getEditPermissionUsers()) {
          if(!s.equals(removedUser)) {
            newPerms.add(s) ;
          }
        }
        contact.setEditPermissionUsers(newPerms.toArray(new String[newPerms.size()])) ;
      }
    }
    private void removePerGroup(Contact contact, String removedGroup) {
      if(contact.getViewPermissionGroups() != null) {
        List<String> newPerms = new ArrayList<String>() ;
        for(String s : contact.getViewPermissionGroups()) {
          if(!s.equals(removedGroup)) {
            newPerms.add(s) ;
          }
        }
        contact.setViewPermissionGroups(newPerms.toArray(new String[newPerms.size()])) ;
      }
      if(contact.getEditPermissionGroups() != null) {
        List<String> newPerms = new ArrayList<String>() ;
        for(String s : contact.getEditPermissionGroups()) {
          if(!s.equals(removedGroup)) {
            newPerms.add(s) ;
          }
        }
        contact.setEditPermissionGroups(newPerms.toArray(new String[newPerms.size()])) ;
      }
    }
  }
  public class data {
    String viewPermission = null ;
    String editPermission = null ;

    public  String getViewPermission() {return viewPermission ;}
    public  String getEditPermission() {return editPermission ;}
      
    public data(String username, boolean canEdit) {
      if (username.endsWith(JCRDataStorage.HYPHEN))
        viewPermission = username.replaceFirst(JCRDataStorage.HYPHEN, "") ;
      else viewPermission = username ;
      String edit = String.valueOf(canEdit) ;
      if (edit.endsWith(JCRDataStorage.HYPHEN)) editPermission = edit.replaceFirst(JCRDataStorage.HYPHEN, "") ;
      else editPermission = edit ;
    }
  }
}
