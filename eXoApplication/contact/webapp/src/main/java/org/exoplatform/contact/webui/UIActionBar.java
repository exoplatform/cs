/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui;

import java.util.List;

import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.contact.webui.popup.UICategoryForm;
import org.exoplatform.contact.webui.popup.UICategorySelect;
import org.exoplatform.contact.webui.popup.UIContactForm;
import org.exoplatform.contact.webui.popup.UIExportAddressBookForm;
import org.exoplatform.contact.webui.popup.UIImportForm;
import org.exoplatform.contact.webui.popup.UIPopupAction;
import org.exoplatform.contact.webui.popup.UIPopupContainer;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
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
        @EventConfig(listeners = UIActionBar.AddContactActionListener.class),
        @EventConfig(listeners = UIActionBar.AddAddressBookActionListener.class),
        @EventConfig(listeners = UIActionBar.ChangeViewActionListener.class),
        @EventConfig(listeners = UIActionBar.ListViewActionListener.class),
        @EventConfig(listeners = UIActionBar.VCardViewActionListener.class),
        @EventConfig(listeners = UIActionBar.ImportContactActionListener.class),
        @EventConfig(listeners = UIActionBar.ExportContactActionListener.class)
    }
)
public class UIActionBar extends UIContainer  {
  public UIActionBar() throws Exception { } 
  
  static public class ChangeViewActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception { }
  } 
  
  static public class AddContactActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ;
      UIContactPortlet uiContactPortlet = uiActionBar.getAncestorOfType(UIContactPortlet.class) ;
      UIPopupAction uiPopupAction = uiContactPortlet.getChild(UIPopupAction.class) ;     
      UIPopupContainer popupContainer = uiPopupAction.activate(UIPopupContainer.class, 800) ;        
      popupContainer.setId("AddNewContact") ; 
      popupContainer.addChild(UICategorySelect.class, null, null) ;
      popupContainer.addChild(UIContactForm.class, null, null) ;
      UIContactForm.isNew_ = true ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }  
  }
  
  static public class AddAddressBookActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ;
      UIContactPortlet uiContactPortlet = uiActionBar.getAncestorOfType(UIContactPortlet.class) ;
      UIPopupAction uiPopupAction = uiContactPortlet.getChild(UIPopupAction.class) ;     
      UICategoryForm uiCategoryForm = uiPopupAction.createUIComponent(UICategoryForm.class, null, "UICategoryForm") ;
      UICategoryForm.isNew_ = true ;
      uiPopupAction.activate(uiCategoryForm, 500, 0, true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }  
  }
  
  static public class ListViewActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {      
      UIActionBar uiActionBar = event.getSource() ;
      UIContactPortlet uiContactPortlet = uiActionBar.getParent() ; 
      UIContacts uiContacts = uiContactPortlet.findFirstComponentOfType(UIContacts.class) ;      
      UIContactPreview uiContactPreview = uiContactPortlet.findFirstComponentOfType(UIContactPreview.class) ;
      uiContactPreview.setRendered(true) ;
      uiContacts.setViewContactsList(true) ;  
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent()) ;
    }  
  }
  
  static public class VCardViewActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {      
      UIActionBar uiActionBar = event.getSource() ;
      UIContactPortlet uiContactPortlet = uiActionBar.getParent() ; 
      UIContacts uiContacts = uiContactPortlet.findFirstComponentOfType(UIContacts.class) ;      
      UIContactPreview uiContactPreview = uiContactPortlet.findFirstComponentOfType(UIContactPreview.class) ;
      uiContactPreview.setRendered(false) ;
      uiContacts.setViewContactsList(false) ;  
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent()) ;
    }  
  }
  
  static public class ImportContactActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiForm = event.getSource() ;
      UIContactPortlet uiContactPortlet = uiForm.getAncestorOfType(UIContactPortlet.class) ;
      UIPopupAction uiPopupAction = uiContactPortlet.getChild(UIPopupAction.class) ;
      UIImportForm uiImportForm = uiPopupAction.createUIComponent(UIImportForm.class, null, "UIImportForm") ;
      uiPopupAction.activate(uiImportForm, 600, 0, true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }  
  }
  
  static public class ExportContactActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {        
      UIActionBar uiActionBar = event.getSource();
      UIContactPortlet uiContactPortlet = uiActionBar.getAncestorOfType(UIContactPortlet.class);
      UIPopupAction uiPopupAction = uiContactPortlet.getChild(UIPopupAction.class);
      //String addressBookId = event.getRequestContext().getRequestParameter(OBJECTID);
      /*
      if (addressBookId != null) {
        UIExportForm uiExportForm = uiPopupAction.createUIComponent(UIExportForm.class, null,
            "ExportForm");
        uiExportForm.setSelectedGroup(addressBookId);
        uiExportForm.updateList();
        uiPopupAction.activate(uiExportForm, 500, 0, true);
      } 
      */
        // There is no specific address book 
        // so display the address books list
        
        UIExportAddressBookForm uiExportForm = uiPopupAction.createUIComponent(
            UIExportAddressBookForm.class, null, "UIExportAddressBookForm");
        UIAddressBooks uiAddressBooks = uiActionBar.getAncestorOfType(UIContactPortlet.class)
          .findFirstComponentOfType(UIAddressBooks.class) ;
        ContactGroup[] groups = uiAddressBooks.getGroups().toArray(new ContactGroup[] {}) ;
        List<String> sharedGroups = uiAddressBooks.getSharedContactGroups() ;
        if ((sharedGroups == null || sharedGroups.size() == 0) && (groups == null || groups.length == 0)) {
          UIApplication uiApp = uiActionBar.getAncestorOfType(UIApplication.class) ;
          uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.no-addressbook", null,
            ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;   
        }
        uiExportForm.setContactGroups(groups) ;
        uiExportForm.setSharedContactGroup(sharedGroups) ;
        uiExportForm.updateList();
        uiPopupAction.activate(uiExportForm, 500, 0, true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
    }  
  }
  
}
