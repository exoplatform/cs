/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.GroupContactData;
import org.exoplatform.contact.webui.popup.UICategoryForm;
import org.exoplatform.contact.webui.popup.UIPopupAction;
import org.exoplatform.contact.webui.popup.UISendEmail;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.impl.GroupImpl;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
    template =  "app:/templates/contact/webui/UIAddressBooks.gtmpl", 
    events = {
        @EventConfig(listeners = UIAddressBooks.EditGroupActionListener.class),
        @EventConfig(listeners = UIAddressBooks.DeleteGroupActionListener.class,
            confirm = "UIAddressBooks.msg.confirm-delete"),
        @EventConfig(listeners = UIAddressBooks.SelectGroupActionListener.class),
        @EventConfig(listeners = UIAddressBooks.SelectSharedGroupActionListener.class),
        @EventConfig(listeners = UIAddressBooks.AddressPopupActionListener.class),
        @EventConfig(listeners = UIAddressBooks.SendEmailActionListener.class)
    }
)
public class UIAddressBooks extends UIComponent  {
  
  public UIAddressBooks() throws Exception {}

  public ContactGroup[] getGroups() throws Exception { 
    UIWorkingContainer uiWorkingContainer = getAncestorOfType(UIWorkingContainer.class) ; 
    return uiWorkingContainer.getContactGroups() ;
  }

  public String getSelectedGroup() throws Exception { 
    UIWorkingContainer uiWorkingContainer = getAncestorOfType(UIWorkingContainer.class) ;
    return uiWorkingContainer.getSelectedGroup() ;
  }

  public List<GroupContactData> getSharedContactGroups() throws Exception {
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    OrganizationService organizationService = getApplicationComponent(OrganizationService.class) ;
    ContactService contactService = ContactUtils.getContactService() ;
    Object[] objGroupIds = organizationService.getGroupHandler().findGroupsOfUser(username).toArray() ;
    String[] groupIds = new String[objGroupIds.length];
    for (int i = 0; i < groupIds.length; i++) {
      groupIds[i] = ((GroupImpl)objGroupIds[i]).getId() ;
    }
    return contactService.getSharedContacts(groupIds);
  }
  
  static  public class EditGroupActionListener extends EventListener<UIAddressBooks> {
    public void execute(Event<UIAddressBooks> event) throws Exception {
      UIAddressBooks uiAddressBook = event.getSource() ;  
      UIContactPortlet uiContactPortlet = uiAddressBook.getAncestorOfType(UIContactPortlet.class) ;
      UIPopupAction popupAction = uiContactPortlet.getChild(UIPopupAction.class) ;
      UICategoryForm uiCategoryForm = popupAction.createUIComponent(UICategoryForm.class, null, "UICategoryForm") ;
      String groupId = event.getRequestContext().getRequestParameter(OBJECTID);
      uiCategoryForm.setValues(groupId) ;
      UICategoryForm.isNew_ = false ;
      popupAction.activate(uiCategoryForm, 500, 0, true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  public static class SendEmailActionListener extends EventListener<UIAddressBooks> {
    public void execute(Event<UIAddressBooks> event) throws Exception {   
      UIAddressBooks uiAddressBook = event.getSource() ;  
      UIContactPortlet uiContactPortlet = uiAddressBook.getAncestorOfType(UIContactPortlet.class) ;
      UIPopupAction uiPopupAction = uiContactPortlet.getChild(UIPopupAction.class) ;
      UISendEmail uiSendEmail = uiPopupAction.createUIComponent(UISendEmail.class, null, "UISendEmail") ; 
      String groupId = event.getRequestContext().getRequestParameter(OBJECTID);
      String username = ContactUtils.getCurrentUser() ;
      ContactService contactService = ContactUtils.getContactService() ;
      List<Contact> contacts = contactService.getContactsByGroup(username, groupId) ;
      if (contacts.size() == 0) contacts = contactService.getSharedContactsByGroup(groupId);
      List<String> emails = new ArrayList<String>() ;
      for (Contact contact : contacts) emails.add(contact.getEmailAddress()) ;
      uiSendEmail.setEmails(emails) ;
      uiPopupAction.activate(uiSendEmail, 700, 0, true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }
  
  static  public class DeleteGroupActionListener extends EventListener<UIAddressBooks> {
    public void execute(Event<UIAddressBooks> event) throws Exception {
      UIAddressBooks uiAddressBook = event.getSource() ;  
      String groupId = event.getRequestContext().getRequestParameter(OBJECTID);
      ContactService contactService = ContactUtils.getContactService() ;
      String username = ContactUtils.getCurrentUser() ;
      contactService.removeGroup(username, groupId) ;
      UIWorkingContainer uiWorkingContainer = uiAddressBook.getAncestorOfType(UIWorkingContainer.class) ;
      uiWorkingContainer.removeContactGroup(groupId) ;
      
      UIContacts uiContacts = uiWorkingContainer.findFirstComponentOfType(UIContacts.class) ;
      uiContacts.setContacts(new ArrayList<Contact>()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingContainer) ;      
    }
  }

  static  public class SelectGroupActionListener extends EventListener<UIAddressBooks> {
    public void execute(Event<UIAddressBooks> event) throws Exception {
      UIAddressBooks uiAddressBook = event.getSource() ;  
      UIWorkingContainer uiWorkingContainer = uiAddressBook.getAncestorOfType(UIWorkingContainer.class) ;
      String groupId = event.getRequestContext().getRequestParameter(OBJECTID) ;    
      uiWorkingContainer.setSelectedGroup(groupId) ;
      String username = ContactUtils.getCurrentUser() ;
      ContactService contactService = ContactUtils.getContactService();
      UIContacts uiContacts = uiWorkingContainer.findFirstComponentOfType(UIContacts.class) ;
      uiContacts.setContacts(contactService.getContactsByGroup(username, groupId)) ; 
      UIContactPreview uiContactPreview = uiWorkingContainer.findFirstComponentOfType(UIContactPreview.class);
      uiContactPreview.updateContact() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingContainer) ;
    }
  }
  
  static  public class SelectSharedGroupActionListener extends EventListener<UIAddressBooks> {
    public void execute(Event<UIAddressBooks> event) throws Exception {
      UIAddressBooks uiAddressBook = event.getSource() ;  
      UIWorkingContainer uiWorkingContainer = uiAddressBook.getAncestorOfType(UIWorkingContainer.class) ;
      String groupId = event.getRequestContext().getRequestParameter(OBJECTID) ;    
      ContactService contactService = ContactUtils.getContactService();
      uiWorkingContainer.setSelectedGroup(groupId) ;
      UIContacts uiContacts = uiWorkingContainer.findFirstComponentOfType(UIContacts.class) ; 
      if (contactService.getSharedContacts(new String[] {groupId}) != null && contactService.getSharedContacts(new String[] {groupId}).size() > 0)
        uiContacts.setContacts(contactService.getSharedContacts(new String[] {groupId}).get(0).getContacts()) ;      
      UIContactPreview uiContactPreview = uiWorkingContainer.findFirstComponentOfType(UIContactPreview.class);
      uiContactPreview.updateContact() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingContainer) ;
    }
  }
  
  public static class AddressPopupActionListener extends EventListener<UIAddressBooks> {
    public void execute(Event<UIAddressBooks> event) throws Exception {   
      String viewType = event.getRequestContext().getRequestParameter(OBJECTID) ;
      System.out.println("\n\n view type :" + viewType + "\n\n");
    }
  }
  
  
  
}
