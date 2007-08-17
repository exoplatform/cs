/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui.popup;

import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.webui.UIContactPortlet;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
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
    //template = "app:/templates/contact/webui/UICategoryForm.gtmpl",
    template = "system:/groovy/webui/form/UIForm.gtmpl", 
    events = {
      @EventConfig(listeners = UICategoryForm.SaveActionListener.class),      
      @EventConfig(listeners = UICategoryForm.CancelActionListener.class)
    }
)
public class UICategoryForm extends UIForm implements UIPopupComponent{
  public static final String FIELD_CATEGORYNAME_INPUT = "categoryName";
  
  public UICategoryForm() {
    //Add field categoryName
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
      ContactService contactService = 
        (ContactService)PortalContainer.getInstance().getComponentInstanceOfType(ContactService.class) ;
      ContactGroup group = new ContactGroup();
      group.setId("group id add");
      group.setName(uiForm.getUIStringInput(FIELD_CATEGORYNAME_INPUT).getValue());
      contactService.saveGroup("exo", group, true);
      
      System.out.println("\n\n\n group add 2 :" + contactService.getGroup("exo", group.getId()).getName() + "\n\n");
      UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIContactForm contactForm = popupContainer.getChild(UIContactForm.class) ;
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
      context.addUIComponentToUpdateByAjax(contactForm) ;
      //popupContainer.cancelAction() ;  
    }
  }
  
  static  public class CancelActionListener extends EventListener<UICategoryForm> {
    public void execute(Event<UICategoryForm> event) throws Exception {
      UICategoryForm uiForm = event.getSource() ;
      UIPopupAction uiPopup = uiForm.getAncestorOfType(UIPopupAction.class) ;
      UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      uiPopup.deActivate() ;
      //event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer.getAncestorOfType(UIContactPortlet.class)) ;
      //popupContainer.cancelAction() ; */
    }
  }
}
