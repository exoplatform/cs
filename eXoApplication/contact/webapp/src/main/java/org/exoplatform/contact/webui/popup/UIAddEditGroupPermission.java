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
import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.contact.service.ContactService;
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
      @EventConfig(listeners = UIAddEditGroupPermission.EditActionListener.class),
      @EventConfig(listeners = UIAddEditGroupPermission.DeleteActionListener.class, confirm = "UIAddEditGroupPermission.msg.confirm-delete")
    }
)

public class UIAddEditGroupPermission extends UIContainer implements UIPopupComponent {
  public static String[]  BEAN_FIELD = {"viewPermission","editPermission"} ;
  private static String[] ACTION = {"Edit", "Delete"} ;
  private String groupId_ ;
  
  public UIAddEditGroupPermission() throws Exception {
    this.setName("UIAddEditGroupPermission");
    UIGrid permissionList = addChild(UIGrid.class, null, "PermissionList") ;
    permissionList.configure("viewPermission", BEAN_FIELD, ACTION);
    permissionList.getUIPageIterator().setId("PermissionListIterator") ;
    addChild(UISharedGroupForm.class, null, null) ;
    ///shareForm.init(null, cal, true);
  }
  public void activate() throws Exception { }
  public void deActivate() throws Exception { }

  public void init(ContactGroup group) throws Exception{
    UISharedGroupForm shareForm = getChild(UISharedGroupForm.class) ;
    shareForm.init(group) ; 
    updateGrid(group) ;
    groupId_ = group.getId() ;
  }

  public void updateGrid(ContactGroup group) throws Exception {
    List<data> dataRow = new ArrayList<data>() ;
    if(group.getViewPermission() != null) {
      for(String username : group.getViewPermission() ) {
        dataRow.add(new data(username, (group.getEditPermission()!= null && Arrays.asList(group.getEditPermission()).contains(username)))) ;
      }
    }
    UIGrid permissionList = getChild(UIGrid.class) ;
    ObjectPageList objPageList = new ObjectPageList(dataRow, 10) ;
    permissionList.getUIPageIterator().setPageList(objPageList) ;   
    //cal.getEditPermission()
  }

  static public class EditActionListener extends EventListener<UIAddEditGroupPermission> {
    public void execute(Event<UIAddEditGroupPermission> event) throws Exception {
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
  static public class DeleteActionListener extends EventListener<UIAddEditGroupPermission> {
    public void execute(Event<UIAddEditGroupPermission> event) throws Exception {
      UIAddEditGroupPermission addEdit = event.getSource();
      String receiverUser = event.getRequestContext().getRequestParameter(OBJECTID);
      ContactService contactService = ContactUtils.getContactService();
      String username = ContactUtils.getCurrentUser() ;
      ContactGroup group = contactService.getGroup(SessionProviderFactory.createSessionProvider(), username, addEdit.groupId_) ;
      if(group.getViewPermission() != null) {
        List<String> newPerms = new ArrayList<String>() ;
        for(String s : group.getViewPermission()) {
          if(!s.equals(receiverUser)) {
            newPerms.add(s) ;
          }
        }
        group.setViewPermission(newPerms.toArray(new String[newPerms.size()])) ;
      }
      if(group.getEditPermission() != null) {
        List<String> newPerms = new ArrayList<String>() ;
        for(String s : group.getEditPermission()) {
          if(!s.equals(receiverUser)) {
            newPerms.add(s) ;
          }
        }
        group.setEditPermission(newPerms.toArray(new String[newPerms.size()])) ;
      }
      List<String> removedUsers = new ArrayList<String>() ;
      removedUsers.add(receiverUser) ;
      contactService.removeUserShareAddressBook(SessionProviderFactory.createSessionProvider()
          , username, addEdit.groupId_, removedUsers) ;
      contactService.saveGroup(SessionProviderFactory.createSessionProvider(), username, group, false) ;
      
      addEdit.updateGrid(group);
      event.getRequestContext().addUIComponentToUpdateByAjax(addEdit) ;
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
