/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui.popup;

import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.webui.UIAddressBooks;
import org.exoplatform.contact.webui.UIContactPortlet;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;

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
      @EventConfig(listeners = UIAddressBookForm.SaveActionListener.class),      
      @EventConfig(listeners = UIAddressBookForm.CancelActionListener.class)
    }
)
public class UIAddressBookForm extends UIForm implements UIPopupComponent {
  public static final String FIELD_ADDRESSBOOKNAME_INPUT = "addressBookName";
  public static final String FIELD_DESCRIPTION_INPUT = "description";
  
  public UIAddressBookForm() throws Exception {
    setId("UIAddressBookForm") ;
    addUIFormInput(new UIFormStringInput(FIELD_ADDRESSBOOKNAME_INPUT, FIELD_ADDRESSBOOKNAME_INPUT, null));
    addUIFormInput(new UIFormTextAreaInput(FIELD_DESCRIPTION_INPUT, FIELD_DESCRIPTION_INPUT, null)) ;
  }
  
  public String[] getActions() { return new String[] {"Save", "Cancel"} ; }
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}
  
  static  public class SaveActionListener extends EventListener<UIAddressBookForm> {
    public void execute(Event<UIAddressBookForm> event) throws Exception {
      UIAddressBookForm uiAddressBookForm = event.getSource() ;
      String addressBookName = uiAddressBookForm.getUIStringInput(FIELD_ADDRESSBOOKNAME_INPUT).getValue() ;
      String description = uiAddressBookForm.getUIFormTextAreaInput(FIELD_DESCRIPTION_INPUT).getValue() ;
      ContactGroup group = new ContactGroup() ;
      group.setName(addressBookName) ;
      ContactService contactService = uiAddressBookForm.getApplicationComponent(ContactService.class) ;
      String username = Util.getPortalRequestContext().getRemoteUser() ;
      contactService.saveGroup(username, group, true) ;
      UIContactPortlet uiContactPortlet = uiAddressBookForm.getAncestorOfType(UIContactPortlet.class) ;
      UIAddressBooks uiAddressBook = uiContactPortlet.findFirstComponentOfType(UIAddressBooks.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressBook) ;
      uiContactPortlet.cancelAction() ;
    }
  }

  static  public class CancelActionListener extends EventListener<UIAddressBookForm> {
    public void execute(Event<UIAddressBookForm> event) throws Exception {
      UIAddressBookForm uiAddressBookForm = event.getSource() ;
      UIContactPortlet contactPortlet = uiAddressBookForm.getAncestorOfType(UIContactPortlet.class) ;
      contactPortlet.cancelAction() ; 
    }
  }
}
