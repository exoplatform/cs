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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.contact.service.SharedAddressBook;
import org.exoplatform.contact.webui.UIContactPortlet;
import org.exoplatform.contact.webui.UIContacts;
import org.exoplatform.contact.webui.UIWorkingContainer;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/contact/webui/popup/UIMoveContactsForm.gtmpl",
    events = {     
      @EventConfig(listeners = UIMoveContactsForm.CancelActionListener.class),
      @EventConfig(listeners = UIMoveContactsForm.SelectGroupActionListener.class)
    }
)
public class UIMoveContactsForm extends UIForm implements UIPopupComponent {
  private Map<String, Contact> movedContacts = new HashMap<String, Contact>() ;
  private Map<String, String> privateGroupMap_ = new HashMap<String, String>() ;
  private Map<String, SharedAddressBook> sharedGroupMap_ = new HashMap<String, SharedAddressBook>() ;
  
  public UIMoveContactsForm() throws Exception { }
  public String getLabel(String id) throws Exception {
    try {
      return  super.getLabel(id) ;
    } catch (MissingResourceException mre) {
      return id ;
    }
  }
  
  public void setContacts(Map<String, Contact> contacts) { movedContacts = contacts ; }
  public Map<String, Contact> getContacts() { return movedContacts ; }

  public String getContactsName() {
    StringBuffer buffer = new StringBuffer() ;
    String[] contactIds = movedContacts.keySet().toArray(new String[] {}) ;
    buffer.append(movedContacts.get(contactIds[0]).getFullName()) ;
    for (int i = 1; i < contactIds.length; i ++) {
      buffer.append(", " + movedContacts.get(contactIds[i]).getFullName()) ;
    }
    return buffer.toString() ;
  }
  
  private List<String> getContactIds() { 
    String[] contacts = movedContacts.keySet().toArray(new String[] {}) ;
    List<String> contactIds = new ArrayList<String>() ;
    for(String contactId : contacts) {
      contactIds.add(contactId) ;
    }
    return contactIds ;
  }
  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
  public String[] getActions() { return new String[] {"Cancel"} ; }

  public Map<String, String> getPrivateGroupMap() { return privateGroupMap_ ; }
  public void setPrivateGroupMap(Map<String, String> map) { privateGroupMap_ = map ; }

  public Map<String, SharedAddressBook> getSharedGroupMap() { return sharedGroupMap_ ; }
  public void setSharedGroupMap(Map<String, SharedAddressBook> map) { sharedGroupMap_ = map ; }
  
  static  public class SelectGroupActionListener extends EventListener<UIMoveContactsForm> {
    public void execute(Event<UIMoveContactsForm> event) throws Exception {
      UIMoveContactsForm uiMoveContactForm = event.getSource() ;
      String addressBookId = event.getRequestContext().getRequestParameter(OBJECTID);
      if (uiMoveContactForm. sharedGroupMap_.containsKey(addressBookId)) {
        String username = ContactUtils.getCurrentUser() ;
        ContactGroup group = ContactUtils.getContactService().getSharedGroup(username, addressBookId) ;
        if (group.getEditPermission() == null ||  !Arrays.asList(group.getEditPermission()).contains(username)) {
          UIApplication uiApp = uiMoveContactForm.getAncestorOfType(UIApplication.class) ;
          uiApp.addMessage(new ApplicationMessage("UIMoveContactsForm.msg.non-permission", null,
            ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;  
        }
      }
      String type = event.getRequestContext().getRequestParameter("addressType");
      UIContactPortlet uiContactPortlet = uiMoveContactForm.getAncestorOfType(UIContactPortlet.class);
      List<Contact> contacts = new ArrayList<Contact>() ;
      for(String id : uiMoveContactForm.getContactIds()) {
      	Contact contact = uiMoveContactForm.movedContacts.get(id) ;
      	contact.setAddressBook(new String[]{addressBookId}) ;
      	contacts.add(contact) ;
      }
      ContactUtils.getContactService().moveContacts(SessionProviderFactory.createSessionProvider()
        , ContactUtils.getCurrentUser(), contacts, type) ;

      UIContacts uiContacts = uiContactPortlet.findFirstComponentOfType(UIContacts.class) ;
      if (uiContacts.isDisplaySearchResult()) {
        for (String contactId : uiMoveContactForm.getContactIds()) {
          uiMoveContactForm.movedContacts.get(contactId).setContactType(type) ;
        }
      } else if (ContactUtils.isEmpty(uiContacts.getSelectedGroup()) && 
          ContactUtils.isEmpty(uiContacts.getSelectedTag())) {

        //select shared contacts        
        uiContacts.setContact(contacts, false) ;
      }
      uiContacts.updateList() ;
      uiContactPortlet.cancelAction() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(
          uiContactPortlet.getChild(UIWorkingContainer.class)) ;
    }
  }

  
  static  public class CancelActionListener extends EventListener<UIMoveContactsForm> {
    public void execute(Event<UIMoveContactsForm> event) throws Exception {
      UIMoveContactsForm uiForm = event.getSource() ;
      UIContactPortlet contactPortlet = uiForm.getAncestorOfType(UIContactPortlet.class) ;
      contactPortlet.cancelAction() ; 
    }
  }
  
}
