/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui.popup;

import java.util.List;

import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.webui.UIContactPortlet;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIForm.gtmpl", 
    events = {
      @EventConfig(listeners = UICategoryForm.SaveActionListener.class),      
      @EventConfig(listeners = UICategoryForm.CancelActionListener.class)
    }
)
public class UICategoryForm extends UIForm implements UIPopupComponent{
  public static final String FIELD_CATEGORYNAME_INPUT = "categoryName";
  
  public UICategoryForm() {
    addUIFormInput(new UIFormStringInput(FIELD_CATEGORYNAME_INPUT, FIELD_CATEGORYNAME_INPUT, null));
  }
  
  public String[] getActions() { return new String[] {"Save", "Cancel"} ; }
  
  public void activate() throws Exception {
    // TODO Auto-generated method stub
  }

  public void deActivate() throws Exception {
    // TODO Auto-generated method stub
  }
  
  static  public class SaveActionListener extends EventListener<UICategoryForm> {
    public void execute(Event<UICategoryForm> event) throws Exception {
      UICategoryForm uiForm = event.getSource() ;
      ContactService contactService = uiForm.getApplicationComponent(ContactService.class);
      String  groupName = uiForm.getUIStringInput(FIELD_CATEGORYNAME_INPUT).getValue(); 
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      if (groupName == null || groupName.trim().length() == 0) {
        uiApp.addMessage(new ApplicationMessage("UICategoryForm.msg.categoryName-required", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ; 
      }
      ContactGroup group = new ContactGroup();
      group.setName(groupName);
      String username = Util.getPortalRequestContext().getRemoteUser() ;
      contactService.saveGroup(username, group, true);  
      UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UICategorySelect uiCategorySelect = popupContainer.findFirstComponentOfType(UICategorySelect.class);
      List<SelectItemOption<String>> ls = uiCategorySelect.getCategoryList();
      uiCategorySelect.setCategoryList(ls);
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
      context.addUIComponentToUpdateByAjax(popupContainer) ;
    }
  }
  
  static  public class CancelActionListener extends EventListener<UICategoryForm> {
    public void execute(Event<UICategoryForm> event) throws Exception {
      UICategoryForm uiForm = event.getSource() ;
      UIPopupAction uiPopup = uiForm.getAncestorOfType(UIPopupAction.class) ;
      UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      uiPopup.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer.getAncestorOfType(UIContactPortlet.class)) ;
    }
  }
}
