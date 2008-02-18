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
package org.exoplatform.contact.webui;

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.SharedAddressBook;
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
        @EventConfig(listeners = UIActionBar.ListViewActionListener.class),
        //@EventConfig(listeners = UIActionBar.VCardViewActionListener.class),
        @EventConfig(listeners = UIActionBar.ImportContactActionListener.class),
        @EventConfig(listeners = UIActionBar.ExportContactActionListener.class)
    }
)
public class UIActionBar extends UIContainer  {
  public UIActionBar() throws Exception { } 
  
  static public class AddContactActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ;
      UIContactPortlet uiContactPortlet = uiActionBar.getAncestorOfType(UIContactPortlet.class) ;
      UIPopupAction uiPopupAction = uiContactPortlet.getChild(UIPopupAction.class) ; 
      UIPopupContainer uiPopupContainer = uiPopupAction.activate(UIPopupContainer.class,800) ;  
      uiPopupContainer.setId("AddNewContact") ;
      UICategorySelect categorySelect = uiPopupContainer.addChild(UICategorySelect.class, null, null) ;
      categorySelect.setPrivateGroupMap(
          uiContactPortlet.findFirstComponentOfType(UIAddressBooks.class).getPrivateGroupMap()) ;
      categorySelect.addCategories() ;
      UIContactForm contactForm = uiPopupContainer.addChild(UIContactForm.class, null, null) ;
      contactForm.setNew(true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiActionBar.getParent()) ;
    }  
  }
  
  static public class ImportContactActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiForm = event.getSource() ;
      UIContactPortlet uiContactPortlet = uiForm.getAncestorOfType(UIContactPortlet.class) ;
      UIPopupAction uiPopupAction = uiContactPortlet.getChild(UIPopupAction.class) ;
      UIPopupContainer uiPopupContainer =  uiPopupAction.activate(UIPopupContainer.class, 600) ;
      uiPopupContainer.setId("ImportAddress") ;      
      UIImportForm importForm = uiPopupContainer.addChild(UIImportForm.class, null, null) ; 
      importForm.setGroup(uiContactPortlet.findFirstComponentOfType(UIAddressBooks.class).getPrivateGroupMap()) ;
      importForm.addConponent() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
    }  
  }

  static public class AddAddressBookActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ;
      UIContactPortlet uiContactPortlet = uiActionBar.getAncestorOfType(UIContactPortlet.class) ;
      UIPopupAction uiPopupAction = uiContactPortlet.getChild(UIPopupAction.class) ;
      uiPopupAction.activate(UICategoryForm.class, 400) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }  
  }
  
  static public class ListViewActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {      
      UIActionBar uiActionBar = event.getSource() ;
      String isList = event.getRequestContext().getRequestParameter(OBJECTID);
      UIContactPortlet uiContactPortlet = uiActionBar.getParent() ; 
      UIContacts uiContacts = uiContactPortlet.findFirstComponentOfType(UIContacts.class) ;
      
      if (isList.equals("true")) uiContacts.setViewContactsList(true) ;
      else uiContacts.setViewContactsList(false) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent()) ;
      
      // remove when print address book improved
      event.getRequestContext().addUIComponentToUpdateByAjax(
          uiContactPortlet.findFirstComponentOfType(UINavigationContainer.class)) ;
    }  
  }
  
  /*
  static public class VCardViewActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {      
      UIActionBar uiActionBar = event.getSource() ;
      UIContactPortlet uiContactPortlet = uiActionBar.getParent() ; 
      UIContacts uiContacts = uiContactPortlet.findFirstComponentOfType(UIContacts.class) ;
      uiContacts.setRendered(true) ;
      uiContacts.setViewContactsList(false) ;  
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent()) ;
      
//    remove when print address book improved
      event.getRequestContext().addUIComponentToUpdateByAjax(
          uiContactPortlet.findFirstComponentOfType(UIAddressBooks.class)) ;
    }  
  }
  */
  
  static public class ExportContactActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {        
      UIActionBar uiActionBar = event.getSource();
      UIContactPortlet uiContactPortlet = uiActionBar.getAncestorOfType(UIContactPortlet.class);
      UIPopupAction uiPopupAction = uiContactPortlet.getChild(UIPopupAction.class); 
      UIExportAddressBookForm uiExportForm = uiPopupAction.activate(UIExportAddressBookForm.class, 500);
      uiExportForm.setId("UIExportAddressBookForm") ;
      UIAddressBooks uiAddressBooks = uiActionBar.getAncestorOfType(UIContactPortlet.class)
        .findFirstComponentOfType(UIAddressBooks.class) ;
      Map<String, String> groups = uiAddressBooks.getPrivateGroupMap() ;
      Map<String, String> publicGroups = new HashMap<String, String>() ;
      for (String group : ContactUtils.getUserGroups()) publicGroups.put(group, group) ;      
      Map<String, SharedAddressBook> sharedGroups = uiAddressBooks.getSharedGroups() ;
      
      if ((publicGroups == null || publicGroups.size() == 0) && (groups == null || groups.size() == 0)
          && (sharedGroups == null || sharedGroups.size() == 0)) {
        UIApplication uiApp = uiActionBar.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.no-addressbook", null,
          ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;   
      }
      uiExportForm.setContactGroups(groups) ;
      uiExportForm.setPublicContactGroup(publicGroups) ;
      uiExportForm.setSharedContactGroups(sharedGroups) ;      
      uiExportForm.updateList();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
    }  
  }
  
}
