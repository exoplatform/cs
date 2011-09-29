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
import java.util.LinkedHashMap;
import java.util.Map;

import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.contact.CalendarUtils;
import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.SharedAddressBook;
import org.exoplatform.contact.webui.popup.UICategoryForm;
import org.exoplatform.contact.webui.popup.UICategorySelect;
import org.exoplatform.contact.webui.popup.UIContactForm;
import org.exoplatform.contact.webui.popup.UIEventForm;
import org.exoplatform.contact.webui.popup.UIExportAddressBookForm;
import org.exoplatform.contact.webui.popup.UIImportForm;
import org.exoplatform.contact.webui.popup.UIPopupAction;
import org.exoplatform.contact.webui.popup.UIPopupContainer;
import org.exoplatform.web.application.ApplicationMessage;
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
        @EventConfig(listeners = UIActionBar.AddContactActionListener.class),
        @EventConfig(listeners = UIActionBar.AddAddressBookActionListener.class),
        @EventConfig(listeners = UIActionBar.ChangeViewActionListener.class),
        @EventConfig(listeners = UIActionBar.ImportContactActionListener.class),
        @EventConfig(listeners = UIActionBar.AddEventActionListener.class),
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
      UICategorySelect uiCategorySelect = uiPopupContainer.addChild(UICategorySelect.class, null, null) ;
      UIAddressBooks uiAddressBooks = uiContactPortlet.findFirstComponentOfType(UIAddressBooks.class) ;
      Map<String, String> privateAddresses = uiAddressBooks.getPrivateGroupMap() ;
      Map<String, String> addresses = new LinkedHashMap<String, String>() ;
      addresses.putAll(privateAddresses) ;
      for (SharedAddressBook address : uiAddressBooks.getSharedGroups().values())
        if (uiAddressBooks.havePermission(address.getId())) {
          addresses.put(address.getId(), ContactUtils.getDisplayAdddressShared(address.getSharedUserId(), address.getName())) ;
        }
      uiCategorySelect.setPrivateGroupMap(addresses) ;
      UIContactForm contactForm = uiPopupContainer.addChild(UIContactForm.class, null, null) ;
      contactForm.setNew(true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      //event.getRequestContext().addUIComponentToUpdateByAjax(uiActionBar.getParent()) ;
    }  
  }
  
  static public class ExportContactActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {        
      UIActionBar uiActionBar = event.getSource();
      UIContactPortlet uiContactPortlet = uiActionBar.getAncestorOfType(UIContactPortlet.class);
      UIPopupAction uiPopupAction = uiContactPortlet.getChild(UIPopupAction.class); 
      UIExportAddressBookForm uiExportForm = uiPopupAction.activate(UIExportAddressBookForm.class, 500);
      uiExportForm.setId("UIExportAddressBookForm") ;
      UIAddressBooks uiAddressBooks = uiActionBar.getAncestorOfType(UIContactPortlet.class)
        .findFirstComponentOfType(UIAddressBooks.class) ;
      Map<String, String> publicGroups = new HashMap<String, String>() ;
      for (String group : ContactUtils.getUserGroups()) publicGroups.put(group, group) ;
      uiExportForm.setContactGroups(uiAddressBooks.getPrivateGroupMap()) ;
      uiExportForm.setPublicContactGroup(publicGroups) ;
      uiExportForm.setSharedContactGroups(uiAddressBooks.getSharedGroups()) ;      
      uiExportForm.updateList();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
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
      
      UIAddressBooks uiAddressBook = uiContactPortlet.findFirstComponentOfType(UIAddressBooks.class) ;
      Map<String, String> privateAddresses = uiAddressBook.getPrivateGroupMap() ;
      Map<String, String> addresses = new LinkedHashMap<String, String>() ;
      addresses.putAll(privateAddresses) ;
      for (SharedAddressBook address : uiAddressBook.getSharedGroups().values())
        if (uiAddressBook.havePermission(address.getId())) {
          addresses.put(address.getId(), ContactUtils
              .getDisplayAdddressShared(address.getSharedUserId(), address.getName())) ;
        }
      importForm.setGroup(addresses) ;
      importForm.addConponent() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      //event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
    }  
  }

  static public class AddAddressBookActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {
      UIActionBar uiActionBar = event.getSource() ;
      UIContactPortlet uiContactPortlet = uiActionBar.getAncestorOfType(UIContactPortlet.class) ;
      UIPopupAction uiPopupAction = uiContactPortlet.getChild(UIPopupAction.class) ;
      uiPopupAction.activate(UICategoryForm.class, 500) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }  
  }
  
  static public class ChangeViewActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {      
      UIActionBar uiActionBar = event.getSource() ;
      String isList = event.getRequestContext().getRequestParameter(OBJECTID);
      UIContactPortlet uiContactPortlet = uiActionBar.getParent() ; 
      UIContacts uiContacts = uiContactPortlet.findFirstComponentOfType(UIContacts.class) ;
      if (uiContacts.isDisplaySearchResult()) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIActionBar.msg.cannot-changeView",
                                                                                       null,
                                                                                       ApplicationMessage.WARNING));
        return ;        
      }      
      if (isList.equals("true")) uiContacts.setViewContactsList(true) ;
      else uiContacts.setViewContactsList(false) ;
    }  
  }

  static public class AddEventActionListener extends EventListener<UIActionBar> {
    public void execute(Event<UIActionBar> event) throws Exception {     
      UIActionBar uiActionBar = event.getSource() ;
      CalendarService calendarService = uiActionBar.getApplicationComponent(CalendarService.class);
      UIContactPortlet uiPortlet = uiActionBar.getParent() ;
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
      UIPopupContainer uiPopupContainer = uiPopupAction.activate(UIPopupContainer.class,800) ;  
      uiPopupContainer.setId("AddNewEvent") ;
      uiPopupAction.activate(uiPopupContainer, 600, 0, true) ;
      UIEventForm uiEventForm = uiPopupContainer.createUIComponent(UIEventForm.class, null, null);
      uiPopupContainer.addChild(uiEventForm) ;
      uiEventForm.initForm(calendarService.getCalendarSetting(ContactUtils.getCurrentUser()), null) ;
      uiEventForm.update(CalendarUtils.PRIVATE_TYPE, null) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }
  
}
