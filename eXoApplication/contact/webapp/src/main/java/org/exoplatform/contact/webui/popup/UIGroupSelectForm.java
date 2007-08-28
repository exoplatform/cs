/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.webui.UIContactPortlet;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.impl.GroupImpl;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    //template = "app:/templates/contact/webui/UICategoryForm.gtmpl",
    template = "system:/groovy/webui/form/UIForm.gtmpl", 
    events = {
      @EventConfig(listeners = UIGroupSelectForm.SaveActionListener.class),      
      @EventConfig(listeners = UIGroupSelectForm.CancelActionListener.class)
    }
)
public class UIGroupSelectForm extends UIForm implements UIPopupComponent{
  
  public UIGroupSelectForm() throws Exception {
    update();
  }
  
  private void update() throws Exception {
    OrganizationService organizationService = (OrganizationService)PortalContainer.getComponent(OrganizationService.class) ;
    Object[] groups = organizationService.getGroupHandler().getAllGroups().toArray() ;
    getChildren().clear() ;
    for(int i = 0; i < groups.length; i ++) {
      String groupName = ((GroupImpl)groups[i]).getId() ;
      addUIFormInput(new UIFormCheckBoxInput<Boolean>(groupName, groupName, false));
    }
  }
  
  public String getLabel(String arg0) throws Exception {
    // TODO Auto-generated method stub
    return arg0 ;
  }
  
  public String[] getActions() { return new String[] {"Save", "Cancel"} ; }
  
  public void activate() throws Exception {
    // TODO Auto-generated method stub
    
  }

  public void deActivate() throws Exception {
    // TODO Auto-generated method stub
    
  }
  
  static  public class SaveActionListener extends EventListener<UIGroupSelectForm> {
    public void execute(Event<UIGroupSelectForm> event) throws Exception {
      UIGroupSelectForm uiForm = event.getSource() ;
      List<UIComponent> children = uiForm.getChildren() ;
      StringBuffer selectedGroups = new StringBuffer("") ;
      for(UIComponent child : children) {
        if(child instanceof UIFormCheckBoxInput) {
          if(((UIFormCheckBoxInput)child).isChecked()) {
            selectedGroups.append(((UIFormCheckBoxInput)child).getName() + "\n") ; 
          }
        }
      }
      if (!selectedGroups.toString().equals("")) {
        UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
        UIContactForm contactForm = popupContainer.getChild(UIContactForm.class) ;
        //contactForm.updateSharedGroups(selectedGroups.toString()) ;
        WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
        context.addUIComponentToUpdateByAjax(popupContainer) ;
      }
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIGroupSelectForm> {
    public void execute(Event<UIGroupSelectForm> event) throws Exception {
      UIGroupSelectForm uiForm = event.getSource() ;
      UIPopupAction uiPopup = uiForm.getAncestorOfType(UIPopupAction.class) ;
      UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      uiPopup.deActivate() ;
      //event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer.getAncestorOfType(UIContactPortlet.class)) ;
      //popupContainer.cancelAction() ; */
    }
  }
}
