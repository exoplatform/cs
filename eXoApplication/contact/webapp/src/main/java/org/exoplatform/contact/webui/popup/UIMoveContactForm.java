/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.GroupContactData;
import org.exoplatform.contact.webui.UIAddressBooks;
import org.exoplatform.contact.webui.UIContactPortlet;
import org.exoplatform.contact.webui.UIContacts;
import org.exoplatform.contact.webui.UITags;
import org.exoplatform.contact.webui.UIWorkingContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;

import sun.misc.Perf.GetPerfAction;

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
      @EventConfig(listeners = UIMoveContactForm.SaveActionListener.class),      
      @EventConfig(listeners = UIMoveContactForm.CancelActionListener.class),
      @EventConfig(listeners = UIMoveContactForm.SelectGroupActionListener.class)
    }
)
public class UIMoveContactForm extends UIForm implements UIPopupComponent {
  private boolean personalAddressBookSelected = true ;
  private List<String> contactIds_ ;
  private String groupId_ ;
  
  public UIMoveContactForm() throws Exception { }
  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
  
  public void setPersonalAddressBookSelected(boolean selected) { personalAddressBookSelected = selected ; }
  public boolean getPersonalAddressBookSelected() { return personalAddressBookSelected ; }
  
  public void setGroupId(String groupId) { groupId_ = groupId ; }
  public String getGroupId() { return groupId_ ; }
  
  public List<ContactGroup> getContactGroups() throws Exception { 
    UIContactPortlet uiContactPortlet = getAncestorOfType(UIContactPortlet.class) ;
    UIAddressBooks uiAddressBook = uiContactPortlet.findFirstComponentOfType(UIAddressBooks.class) ;
    return uiAddressBook.getGroups() ; 
  }
  
  public List<GroupContactData> getSharedContactGroups() throws Exception {
    UIContactPortlet uiContactPortlet = getAncestorOfType(UIContactPortlet.class) ;
    UIAddressBooks uiAddressBook = uiContactPortlet.findFirstComponentOfType(UIAddressBooks.class) ;
    return uiAddressBook.getSharedContactGroups() ;
  }
  
  public void setContacts(List<String> contactIds) { contactIds_ = contactIds ; }
  public List<String> getContacts() { return contactIds_ ; }
  
  static  public class SelectGroupActionListener extends EventListener<UIMoveContactForm> {
    public void execute(Event<UIMoveContactForm> event) throws Exception {
      UIMoveContactForm uiMoveContactForm = event.getSource() ;
      String groupId = event.getRequestContext().getRequestParameter(OBJECTID);
      Contact contact ;
      List<Contact> contacts = new ArrayList<Contact>();
      ContactService contactService = uiMoveContactForm.getApplicationComponent(ContactService.class);
      if (uiMoveContactForm.getPersonalAddressBookSelected()) {
        String username = Util.getPortalRequestContext().getRemoteUser() ;
        for (String contactId : uiMoveContactForm.getContacts()) {
          contact = contactService.getContact(username, contactId) ;
          contact.setCategories(new String[] { groupId }) ;
          contacts.add(contact) ;
          contactService.saveContact(username, contact, false) ;
        }
      } else {
        for (String contactId : uiMoveContactForm.getContacts()) {
          contact = contactService.getSharedContact(contactId) ;
          contact.setCategories(new String[] { groupId }) ;
          contacts.add(contact) ;
          contactService.saveSharedContact(contact, false) ;
        }
      }
      UIContactPortlet uiContactPortlet = uiMoveContactForm.getAncestorOfType(UIContactPortlet.class);
      UIContacts uiContacts = uiContactPortlet.findFirstComponentOfType(UIContacts.class) ;
      if (!uiMoveContactForm.getGroupId().equals(groupId))
        uiContacts.removeContacts(contacts) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts) ;
      uiContactPortlet.cancelAction() ;
    }
  }
  
  
  static  public class SaveActionListener extends EventListener<UIMoveContactForm> {
    public void execute(Event<UIMoveContactForm> event) throws Exception {
      UIMoveContactForm uiForm = event.getSource() ;
      
    }
  }
  
  static  public class CancelActionListener extends EventListener<UITagForm> {
    public void execute(Event<UITagForm> event) throws Exception {
      UITagForm uiForm = event.getSource() ;
      UIContactPortlet contactPortlet = uiForm.getAncestorOfType(UIContactPortlet.class) ;
      contactPortlet.cancelAction() ; 
    }
  }
  
}
