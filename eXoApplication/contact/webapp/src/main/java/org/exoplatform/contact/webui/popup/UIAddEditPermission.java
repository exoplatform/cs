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
import java.util.LinkedHashMap;
import java.util.List;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.contact.service.ContactService;
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
    if(contact.getViewPermission() != null) {
      for(String username : contact.getViewPermission() ) {
        dataRow.add(new data(username, (contact.getEditPermission()!= null && Arrays.asList(contact.getEditPermission()).contains(username)))) ;
      }
    }
    UIGrid permissionList = getChild(UIGrid.class) ;
    ObjectPageList objPageList = new ObjectPageList(dataRow, 10) ;
    permissionList.getUIPageIterator().setPageList(objPageList) ;
  }
  
  public void updateGroupGrid(ContactGroup group) throws Exception {
    List<data> dataRow = new ArrayList<data>() ;
    if(group.getViewPermission() != null) {
      for(String username : group.getViewPermission() ) {
        dataRow.add(new data(username, (group.getEditPermission()!= null && Arrays.asList(group.getEditPermission()).contains(username)))) ;
      }
    }
    UIGrid permissionList = getChild(UIGrid.class) ;
    ObjectPageList objPageList = new ObjectPageList(dataRow, 10) ;
    permissionList.getUIPageIterator().setPageList(objPageList) ;
  }

  static public class EditActionListener extends EventListener<UIAddEditPermission> {
    public void execute(Event<UIAddEditPermission> event) throws Exception {
      /*UIAddEditPermission addEdit = event.getSource();
      UISharedForm shareForm = addEdit.getChild(UISharedForm.class);
      String resiceUser = event.getRequestContext().getRequestParameter(OBJECTID);
      UIFormCheckBoxInput checkBox = shareForm.getUIFormCheckBoxInput(UISharedForm.FIELD_EDIT) ;
      CalendarService calService = CalendarUtils.getCalendarService() ;
      String username = CalendarUtils.getCurrentUser() ;
      shareForm.setSharedUser(resiceUser) ;
      Calendar cal = calService.getUserCalendar(SessionProviderFactory.createSessionProvider(), username, addEdit.calendarId_) ;
      checkBox.setChecked((cal.getEditPermission() != null) && Arrays.asList(cal.getEditPermission()).contains(resiceUser)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(shareForm) ;*/
    }
  }
  static public class DeleteActionListener extends EventListener<UIAddEditPermission> {
    public void execute(Event<UIAddEditPermission> event) throws Exception {
      UIAddEditPermission uiForm = event.getSource();
      String removedUser = event.getRequestContext().getRequestParameter(OBJECTID);
      ContactService contactService = ContactUtils.getContactService();
      String username = ContactUtils.getCurrentUser() ;
      
      if (uiForm.isSharedGroup) {
        ContactGroup group = contactService.getGroup(
            SessionProviderFactory.createSessionProvider(), username, uiForm.groupId_) ;
        if(group.getViewPermission() != null) {
          List<String> newPerms = new ArrayList<String>() ;
          for(String s : group.getViewPermission()) {
            if(!s.equals(removedUser)) {
              newPerms.add(s) ;
            }
          }
          group.setViewPermission(newPerms.toArray(new String[newPerms.size()])) ;
        }
        if(group.getEditPermission() != null) {
          List<String> newPerms = new ArrayList<String>() ;
          for(String s : group.getEditPermission()) {
            if(!s.equals(removedUser)) {
              newPerms.add(s) ;
            }
          }
          group.setEditPermission(newPerms.toArray(new String[newPerms.size()])) ;
        }        
        contactService.removeUserShareAddressBook(SessionProviderFactory.createSessionProvider()
            , username, uiForm.groupId_, removedUser) ;
        contactService.saveGroup(SessionProviderFactory.createSessionProvider(), username, group, false) ;
        uiForm.updateGroupGrid(group);
        
        List<Contact> contacts = contactService.getContactPageListByGroup(
            SessionProviderFactory.createSessionProvider(), username, uiForm.groupId_).getAll() ;
        for (Contact contact : contacts) {
          contactService.saveContact(SessionProviderFactory.createSessionProvider()
              , username, removePer(contact, removedUser), false) ;
        }
        
      } else {
        Contact contact = removePer(contactService.getContact(SessionProviderFactory
            .createSessionProvider(), username, uiForm.contactId_), removedUser) ;        
        contactService.removeUserShareContact(SessionProviderFactory.createSessionProvider()
            , username, uiForm.contactId_, removedUser) ;
        //if (contact.getViewPermission() != null)
        contactService.saveContact(
            SessionProviderFactory.createSessionProvider(), username, contact, false) ;
        uiForm.updateContactGrid(contact);
        
        UIContacts uiContacts = uiForm.getAncestorOfType(
            UIContactPortlet.class).findFirstComponentOfType(UIContacts.class) ;
        uiContacts.updateList() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts) ;
        
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
    }
    private Contact removePer(Contact contact, String removedUser) {
      if(contact.getViewPermission() != null) {
        List<String> newPerms = new ArrayList<String>() ;
        for(String s : contact.getViewPermission()) {
          if(!s.equals(removedUser)) {
            newPerms.add(s) ;
          }
        }
        contact.setViewPermission(newPerms.toArray(new String[newPerms.size()])) ;
      }
      if(contact.getEditPermission() != null) {
        List<String> newPerms = new ArrayList<String>() ;
        for(String s : contact.getEditPermission()) {
          if(!s.equals(removedUser)) {
            newPerms.add(s) ;
          }
        }
        contact.setEditPermission(newPerms.toArray(new String[newPerms.size()])) ;
      }
      return contact ;
    }
  }
  public class data {
    String viewPermission = null ;
    String editPermission = null ;

  public  String getViewPermission() {return viewPermission ;}
  public  String getEditPermission() {return editPermission ;}
    
    public data(String username, boolean canEdit) {
      viewPermission = username ;
      editPermission = String.valueOf(canEdit) ;
    }
  }
}
