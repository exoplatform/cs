/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui;

import org.exoplatform.contact.webui.popup.UICategorySelect;
import org.exoplatform.contact.webui.popup.UIContactForm;
import org.exoplatform.contact.webui.popup.UIPopupAction;
import org.exoplatform.contact.webui.popup.UIPopupContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
    template =  "app:/templates/contact/webui/UIActionBar.gtmpl", 
    events = {
        @EventConfig(listeners = UIActionBar.ChangeViewActionListener.class),
        @EventConfig(listeners = UIActionBar.AddContactActionListener.class),
        @EventConfig(listeners = UIActionBar.ChangeContactsViewActionListener.class),
        @EventConfig(listeners = UIActionBar.CustomLayoutActionListener.class),
        @EventConfig(listeners = UIActionBar.AddressBookActionListener.class)
    }
)
public class UIActionBar extends UIContainer  {
  final public static String CONTACTS_LIST = "ContactList".intern() ;
  final public static String DETAILED_CARDS = "Contact".intern() ;
  final public static String[] VIEW_TYPES = { CONTACTS_LIST, DETAILED_CARDS } ;
  
  public UIActionBar() throws Exception { } 
  
  public String[] getViewTypes() { return VIEW_TYPES ; }
  
  static public class ChangeViewActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      
    }
  }  
  static public class AddContactActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ;
      UIContactPortlet contactPortlet = uiActionBar.getAncestorOfType(UIContactPortlet.class) ;
      UIPopupAction popupAction = contactPortlet.getChild(UIPopupAction.class) ;
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, "AddNewContact") ;
      popupContainer.addChild(UICategorySelect.class, null, null) ;
      popupContainer.addChild(UIContactForm.class, null, null) ;
      UIContactForm.isNew_ = true ;
      popupAction.activate(popupContainer, 800, 450, true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  static public class ChangeContactsViewActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {      
      UIActionBar uiActionBar = event.getSource() ;
      UIContactPortlet uiContactPortlet = uiActionBar.getAncestorOfType(UIContactPortlet.class) ;
      
      UIContacts uiContacts = uiContactPortlet.findFirstComponentOfType(UIContacts.class) ;      
      UIContactPreview uiContactPreview = uiContactPortlet.findFirstComponentOfType(UIContactPreview.class) ;
      String viewType = event.getRequestContext().getRequestParameter(OBJECTID) ;
      if (viewType.equals(CONTACTS_LIST)) {
        uiContactPreview.setRendered(true) ;
        uiContacts.setViewContactsList(true) ;  
      } else if (viewType.equals(DETAILED_CARDS)){ 
        uiContactPreview.setRendered(false) ;
        uiContacts.setViewContactsList(false) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent()) ;
    }  
  }
  
  static public class CustomLayoutActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      System.out.println("\n\n\n CustomLayoutActionListener\n\n\n");
    }  
  }
  
  static public class AddressBookActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      System.out.println("\n\n\n AddressBookActionListener\n\n\n");
    }  
  }
  
  
}
