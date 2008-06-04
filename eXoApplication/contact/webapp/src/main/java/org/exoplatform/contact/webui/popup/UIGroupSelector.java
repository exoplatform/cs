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
import java.util.Collection;
import java.util.List;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.contact.ContactUtils;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIBreadcumbs;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UITree;
import org.exoplatform.webui.core.UIBreadcumbs.LocalPath;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.organization.UIGroupMembershipSelector;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          phamtuanchip@yahoo.de
 * Aug 29, 2007 11:57:56 AM 
 */
@ComponentConfigs({ 
  @ComponentConfig(
      template = "app:/templates/contact/webui/popup/UIGroupSelector.gtmpl",
      events = {
          @EventConfig(listeners = UIGroupSelector.ChangeNodeActionListener.class),
          @EventConfig(listeners = UIGroupSelector.SelectMembershipActionListener.class),
          @EventConfig(listeners = UIGroupSelector.SelectPathActionListener.class)  
      }  
  ),
  @ComponentConfig(
      type = UITree.class, id = "UITreeGroupSelector",
      template = "system:/groovy/webui/core/UITree.gtmpl",
      events = @EventConfig(listeners = UITree.ChangeNodeActionListener.class)
  ),
  @ComponentConfig(
      type = UIBreadcumbs.class, id = "BreadcumbGroupSelector",
      template = "system:/groovy/webui/core/UIBreadcumbs.gtmpl",
      events = @EventConfig(listeners = UIBreadcumbs.SelectPathActionListener.class)
  )
})

public class UIGroupSelector extends UIGroupMembershipSelector implements UIPopupComponent, UISelectComponent {
  private UIComponent uiComponent ;
  private String type_ = null ;
  private List selectedGroups_ ;
  private String returnFieldName = null ;
  private boolean isNull = true ;

  public UIGroupSelector() throws Exception {}

  public UIComponent getReturnComponent() { return uiComponent ; }
  public String getReturnField() { return returnFieldName ; }
 
  public void setComponent(UIComponent uicomponent, String[] initParams) {
    uiComponent = uicomponent ;
    if(initParams == null || initParams.length < 0) return ;
    for(int i = 0; i < initParams.length; i ++) {
      if(initParams[i].indexOf("returnField") > -1) {
        String[] array = initParams[i].split("=") ;
        returnFieldName = array[1] ;
        break ;
      }
      returnFieldName = initParams[0] ;
    }
  }

  @SuppressWarnings({ "unchecked", "cast" })
  public List getChildGroup() throws Exception {
    List children = new ArrayList() ;    
    OrganizationService service = getApplicationComponent(OrganizationService.class) ;
    for (Object child : service.getGroupHandler().findGroups(this.getCurrentGroup())) {
      children.add((Group)child) ;
    }
    return children ;
  }
  public boolean isSelectGroup() {return TYPE_GROUP.equals(type_);}
  public boolean isSelectUser() {return TYPE_USER.equals(type_);}
  public boolean isSelectMemberShip() {return TYPE_MEMBERSHIP.equals(type_);}
  @SuppressWarnings({ "unchecked", "cast" })
  public List<String> getList() throws Exception {
    List<String> children = new ArrayList<String>() ; 
    OrganizationService service = getApplicationComponent(OrganizationService.class) ;
    if(TYPE_USER.equals(type_)){
      PageList userPageList = service.getUserHandler().findUsersByGroup(this.getCurrentGroup().getId()) ;    
      for(Object child : userPageList.getAll()){
        children.add(((User)child).getUserName()) ;
      }
    } else if(TYPE_MEMBERSHIP.equals(type_)) {
      for(String child :  getListMemberhip()){
        children.add(child) ;
      } 
    } else if(TYPE_GROUP.equals(type_)) {
      if (!isNull) {
        Collection  groups = service.getGroupHandler().findGroups(this.getCurrentGroup()) ;    
        for(Object child : groups){
          children.add(((Group)child).getGroupName()) ;
        }
      } else {
        Collection  groups = service.getGroupHandler().findGroups(null) ;    
        for(Object child : groups){
          children.add(((Group)child).getGroupName()) ;
        }        
      }      
    }
    return children ;
  }
  public void setSelectedGroups(List groups){
    if(groups != null) {
      selectedGroups_ = groups ;
      getChild(UITree.class).setSibbling(selectedGroups_) ;
    }
  }
  public void changeGroup(String groupId) throws Exception {    
    super.changeGroup(groupId) ;  
    if(selectedGroups_ != null) {
      UITree tree = getChild(UITree.class);
      tree.setSibbling(selectedGroups_);
      tree.setChildren(null);
    }
  }

  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
  public void setType(String type) { this.type_ = type; }
  public String getType() { return type_; }
  
  static  public class SelectMembershipActionListener extends EventListener<UIGroupSelector> {   
    public void execute(Event<UIGroupSelector> event) throws Exception {
      UIGroupSelector uiForm = event.getSource() ;
      String user = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      if(user.equals(ContactUtils.getCurrentUser())) {        
        uiApp.addMessage(new ApplicationMessage("UIGroupSelector.msg.invalid-username", null,
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      UIGroupSelector uiGroupSelector = event.getSource();
      UIPopupContainer uiPopupContainer = uiGroupSelector.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction uiPopup = uiPopupContainer.getChild(UIPopupAction.class) ;
      String returnField = uiGroupSelector.getReturnField() ;
      ((UISelector)uiGroupSelector.getReturnComponent()).updateSelect(returnField, user) ;
      uiPopup.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupContainer.getAncestorOfType(UIPopupAction.class)) ;
    }
  }

  @SuppressWarnings("unused")
  private List<LocalPath> getPath(List<LocalPath> list, String id) throws Exception {
    if(list == null) list = new ArrayList<LocalPath>(5);
    if(id == null) return list;
    OrganizationService service = getApplicationComponent(OrganizationService.class) ;
    Group group = service.getGroupHandler().findGroupById(id);
    if(group == null) return list;
    list.add(0, new LocalPath(group.getId(), group.getGroupName())); 
    getPath(list, group.getParentId());
    return list ;
  }
  
  static  public class ChangeNodeActionListener extends EventListener<UITree> {   
    public void execute(Event<UITree> event) throws Exception {
      UIGroupSelector uiGroupSelector = event.getSource().getAncestorOfType(UIGroupSelector.class) ;
      String groupId = event.getRequestContext().getRequestParameter(OBJECTID) ; 
      uiGroupSelector.changeGroup(groupId) ;   
      if (uiGroupSelector.isSelectGroup()) {
        if (groupId == null) uiGroupSelector.isNull = true ;
        else  uiGroupSelector.isNull = false ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiGroupSelector) ;
    }
  }

  static  public class SelectPathActionListener extends EventListener<UIBreadcumbs> {
    public void execute(Event<UIBreadcumbs> event) throws Exception {
      UIBreadcumbs uiBreadcumbs = event.getSource() ;
      UIGroupSelector uiGroupSelector = uiBreadcumbs.getParent() ;  
      String objectId =  event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiBreadcumbs.setSelectPath(objectId);     
      uiGroupSelector.changeGroup(objectId) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiGroupSelector) ;
    }
  }

}
