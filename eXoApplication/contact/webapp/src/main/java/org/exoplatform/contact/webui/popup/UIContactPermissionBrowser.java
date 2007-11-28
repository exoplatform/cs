/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui.popup;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIBreadcumbs;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UITree;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.organization.UIGroupMembershipSelector;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Nov 17, 2006  
 */
@ComponentConfigs({ 
  @ComponentConfig(
      template = "system:/groovy/organization/webui/component/UIGroupMembershipSelector.gtmpl",
      events = {
          @EventConfig(listeners = UIContactPermissionBrowser.ChangeNodeActionListener.class),
          @EventConfig(listeners = UIContactPermissionBrowser.SelectMembershipActionListener.class),
          @EventConfig(listeners = UIContactPermissionBrowser.SelectPathActionListener.class)  
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
public class UIContactPermissionBrowser extends UIGroupMembershipSelector implements UIPopupComponent, ComponentSelector {

  final static public String defaultValue = "/admin" ;
  private UIComponent uiComponent ;
  private String returnFieldName = null ;
  public boolean isUsePopup_ = true ;
  public UIContactPermissionBrowser() throws Exception {
    changeGroup(defaultValue) ;
  }

  public void setCurrentPermission(String per) throws Exception { changeGroup(per) ; }

  public UIComponent getReturnComponent() { return uiComponent ; }
  public String getReturnField() { return returnFieldName ; }
  
  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
  
  public void setComponent(UIComponent uicomponent, String[] initParams) {
    uiComponent = uicomponent ;
    if(initParams == null || initParams.length == 0) return ;
    for(int i = 0; i < initParams.length; i ++) {
      if(initParams[i].indexOf("returnField") > -1) {
        String[] array = initParams[i].split("=") ;
        returnFieldName = array[1] ;
        break ;
      }
      returnFieldName = initParams[0] ;
    }
  }

  static  public class SelectMembershipActionListener extends EventListener<UIContactPermissionBrowser> {   
    public void execute(Event<UIContactPermissionBrowser> event) throws Exception {
      UIContactPermissionBrowser uiPermissionSelector = event.getSource();
      if(uiPermissionSelector.getCurrentGroup() == null) return ;
      String groupId = uiPermissionSelector.getCurrentGroup().getId() ;
      String permission = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String value = permission + ":" + groupId ;
      String returnField = uiPermissionSelector.getReturnField() ;
      ((UISelector)uiPermissionSelector.getReturnComponent()).updateSelect(returnField, value) ;
      if(uiPermissionSelector.isUsePopup_) {
        UIPopupContainer uiPopupContainer = uiPermissionSelector.getAncestorOfType(UIPopupContainer.class) ;
        UIPopupAction uiPopup = uiPopupContainer.getChild(UIPopupAction.class) ;
        
        /*
        uiPopup.setShow(false) ;
        UIComponent uicomp = uiPermissionSelector.getReturnComponent().getParent() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uicomp) ;
        if(!uiPopup.getId().equals("PopupComponent"))event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
        */
        uiPopup.deActivate() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(
            uiPopupContainer.getAncestorOfType(UIPopupAction.class)) ;
      
      
      } else {
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPermissionSelector.getReturnComponent()) ;
      }
    }
  }

  static  public class ChangeNodeActionListener extends EventListener<UITree> {   
    public void execute(Event<UITree> event) throws Exception {   
      UIContactPermissionBrowser uiPermissionSelector = event.getSource().getParent() ;
      String groupId = event.getRequestContext().getRequestParameter(OBJECTID)  ;
      uiPermissionSelector.changeGroup(groupId) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPermissionSelector) ;
    }
  }

  static  public class SelectPathActionListener extends EventListener<UIBreadcumbs> {
    public void execute(Event<UIBreadcumbs> event) throws Exception {
      UIBreadcumbs uiBreadcumbs = event.getSource() ;
      UIContactPermissionBrowser uiPermissionSelector = uiBreadcumbs.getParent() ;
      String objectId =  event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiBreadcumbs.setSelectPath(objectId);     
      String selectGroupId = uiBreadcumbs.getSelectLocalPath().getId() ;
      uiPermissionSelector.changeGroup(selectGroupId) ;
      if(uiPermissionSelector.isUsePopup_) {
        UIPopupWindow uiPopup = uiBreadcumbs.getAncestorOfType(UIPopupWindow.class) ;
        uiPopup.setShow(true);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPermissionSelector) ;
    }
  }
}
