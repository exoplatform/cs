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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

import javax.jcr.AccessDeniedException;
import javax.jcr.PathNotFoundException;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.AddressBook;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.DataStorage;
import org.exoplatform.contact.service.SharedAddressBook;
import org.exoplatform.contact.webui.UIAddressBooks;
import org.exoplatform.contact.webui.UIContactPortlet;
import org.exoplatform.contact.webui.UIContacts;
import org.exoplatform.contact.webui.UIWorkingContainer;
import org.exoplatform.contact.webui.UIContacts.EmailComparator;
import org.exoplatform.contact.webui.UIContacts.FullNameComparator;
import org.exoplatform.contact.webui.UIContacts.JobTitleComparator;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
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
    for (Contact contact : movedContacts.values()) {
      String fullName = contact.getFullName() ;
      if (buffer.length() > 0) buffer.append(", ") ;
      if (ContactUtils.isEmpty(fullName)) buffer.append(ContactUtils.emptyName()) ;
      else buffer.append(ContactUtils.encodeHTML(fullName)) ;
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
      ContactService contactService = ContactUtils.getContactService() ;
      String username = ContactUtils.getCurrentUser() ;
      if (uiMoveContactForm. sharedGroupMap_.containsKey(addressBookId)) {
        AddressBook group = contactService.getSharedAddressBook(username, addressBookId) ;        
        if (group.getEditPermissionUsers() == null || 
            !Arrays.asList(group.getEditPermissionUsers()).contains(username + DataStorage.HYPHEN)) {
          boolean canEdit = false ;
          String[] editPerGroups = group.getEditPermissionGroups() ;
          if (editPerGroups != null)
            for (String editPer : editPerGroups)
              if (ContactUtils.getUserGroups().contains(editPer)) canEdit = true ;          
          if (!canEdit) {
            event.getRequestContext()
                 .getUIApplication()
                 .addMessage(new ApplicationMessage("UIMoveContactsForm.msg.non-permission", null, ApplicationMessage.WARNING));
            return ; 
          }           
        }
      }
      String type = event.getRequestContext().getRequestParameter("addressType");
      UIContactPortlet uiContactPortlet = uiMoveContactForm.getAncestorOfType(UIContactPortlet.class);
      List<Contact> contacts = new ArrayList<Contact>() ;
      List<Contact> sharedContacts = new ArrayList<Contact>() ;
      Map<String, String> copySharedContacts = new LinkedHashMap<String, String>() ;
      UIContacts uiContacts = uiContactPortlet.findFirstComponentOfType(UIContacts.class) ;
      
//    cs- 1630
      Map<String, String> copyedContacts = uiContactPortlet.findFirstComponentOfType(UIAddressBooks.class).getCopyContacts() ;
      for(String id : uiMoveContactForm.getContactIds()) {
        Contact contact = uiMoveContactForm.movedContacts.get(id) ;
        if (!contact.getAddressBookIds()[0].equals(addressBookId)) copyedContacts.remove(id) ;
        if (contact.getContactType().equals(DataStorage.SHARED)) {          
          // check for existing contact
          Contact tempContact = null ;
          if (uiContacts.isSharedAddress(contact)) {
            tempContact = contactService.getSharedContactAddressBook(username, id) ;
          } else {
            try {
              tempContact = contactService.getSharedContact(username, id) ;              
            } catch (PathNotFoundException e) { }
          }  
          if (!uiContacts.havePermission(contact) && uiContacts.isSharedAddress(contact)) {;
            event.getRequestContext()
                 .getUIApplication()
                 .addMessage(new ApplicationMessage("UIMoveContactsForm.msg.non-permission", null, ApplicationMessage.WARNING));
            return ; 
          }          
          if (tempContact == null) {
            event.getRequestContext()
                 .getUIApplication()
                 .addMessage(new ApplicationMessage("UIMoveContactsForm.msg.contact-not-existed",
                                                    null,
                                                    ApplicationMessage.WARNING));
            return ; 
          } 
          sharedContacts.add(contact) ;
          copySharedContacts.put(id, DataStorage.SHARED) ;
        } else {
          contacts.add(contact) ;
          contact.setAddressBookIds(new String[] { addressBookId }) ;
        }
      }
      
      List<Contact> pastedContact = new ArrayList<Contact>() ;
      List<String> unMove = new ArrayList<String>();
      if (sharedContacts.size() > 0 ) {
        try {
          pastedContact = contactService.pasteContacts(username, addressBookId, type, copySharedContacts);   
        } catch (AccessDeniedException e) {
          event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIContacts.msg.noeditpermission",
                                                                                         null,
                                                                                         ApplicationMessage.WARNING));
          return;
        }
        for (Contact contact : sharedContacts) {
          //if(contactService.haveEditPermissionOnContact(username,contact)){
          // TODO CS-4542
          if(uiContacts.havePermission(contact)){
            if (uiContacts.isSharedAddress(contact)) {
              String addressId = null ;
              for (String add : contact.getAddressBookIds())
                if (uiContacts.getSharedGroupMap().containsKey(add)) addressId = add ;
              contactService.removeSharedContact(username, addressId, contact.getId()) ;
             } else {
              try {
                contactService.removeUserShareContact(contact.getAuthor(), contact.getId(), username) ;              
              } catch (PathNotFoundException e) {
                event.getRequestContext()
                     .getUIApplication()
                     .addMessage(new ApplicationMessage("UIMoveContactsForm.msg.contact-not-existed",
                                                        null,
                                                        ApplicationMessage.WARNING));
                return ; 
              }
             }
            contact.setAddressBookIds(new String[] { addressBookId }) ; 
          }else unMove.add(contact.getFirstName());
        } 
        if(unMove.size()>0){
          StringBuffer sb = new StringBuffer();
          for(String ct : unMove){
            sb.append(ct);
            if(sb.length()<unMove.size())
            sb.append(", ");
          }
          event.getRequestContext()
               .getUIApplication()
               .addMessage(new ApplicationMessage("UIContacts.msg.noeditpermission.detail",
                                                  new String[] { sb.toString() },
                                                  ApplicationMessage.WARNING));
        } 
      } 
      if (contacts.size() > 0) {
        try {
          contactService.moveContacts(username, contacts, type) ;                  
        } catch (PathNotFoundException e) {
          event.getRequestContext()
               .getUIApplication()
               .addMessage(new ApplicationMessage("UIMoveContactsForm.msg.contact-deleted", null, ApplicationMessage.WARNING));
          return ;
        }
      }
      if (uiContacts.isDisplaySearchResult()) {
        for (String contactId : uiMoveContactForm.getContactIds()) {
          Contact contact = uiMoveContactForm.movedContacts.get(contactId) ;
          contact.setContactType(type) ;
          contact.setViewPermissionGroups(null) ;
          contact.setViewPermissionUsers(null) ;
        }
//      cs-2157 
        if (pastedContact.size() > 0) {
          uiContacts.setContact(sharedContacts, false) ;
          uiContacts.getContactPageList().getAll().addAll(pastedContact) ;
        }
        if (contacts.size() >0 && type.equals(DataStorage.SHARED)) {
          uiContacts.getContactPageList().getAll().removeAll(contacts) ;
          for (Contact contact : contacts) {
            uiContacts.getContactPageList().getAll().add(contactService.getSharedContactAddressBook(username, contact.getId())) ;
          }
        }
        if (uiContacts.getSortedBy().equals(UIContacts.fullName)) {
          Collections.sort(uiContacts.getContactPageList().getAll(), new FullNameComparator()) ;
        } else if (uiContacts.getSortedBy().equals(UIContacts.emailAddress)) {
          Collections.sort(uiContacts.getContactPageList().getAll(), new EmailComparator()) ;
        } else if (uiContacts.getSortedBy().equals(UIContacts.jobTitle)) {
          Collections.sort(uiContacts.getContactPageList().getAll(), new JobTitleComparator()) ;
        }
      } else if (ContactUtils.isEmpty(uiContacts.getSelectedGroup()) && 
          ContactUtils.isEmpty(uiContacts.getSelectedTag())) {
        //select shared contacts        
        if (contacts.size() > 0) uiContacts.setContact(contacts, false) ;
        if (sharedContacts.size() > 0) uiContacts.setContact(sharedContacts, false) ;
      }
      uiContacts.updateList();
      uiContactPortlet.cancelAction() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContactPortlet.getChild(UIWorkingContainer.class)) ;
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
