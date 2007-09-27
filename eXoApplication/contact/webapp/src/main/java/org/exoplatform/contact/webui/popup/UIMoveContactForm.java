/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.webui.UIAddressBooks;
import org.exoplatform.contact.webui.UIContactPortlet;
import org.exoplatform.contact.webui.UIContacts;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.impl.GroupImpl;
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
      @EventConfig(listeners = UIMoveContactForm.SaveActionListener.class),      
      @EventConfig(listeners = UIMoveContactForm.CancelActionListener.class),
      @EventConfig(listeners = UIMoveContactForm.SelectGroupActionListener.class)
    }
)
public class UIMoveContactForm extends UIForm implements UIPopupComponent {
  private List<String> contactIds_ ;
  private String groupId_ ;
  
  public UIMoveContactForm() throws Exception { }
  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
  
  public boolean isPersonalAddressBookSelected() throws Exception {
    for(ContactGroup contactGroup : getContactGroups()) {
      if (getGroupId().equals(contactGroup.getId())) return true ;
    }
    return false ;
  }
  
  public void setGroupId(String groupId) { groupId_ = groupId ; }
  public String getGroupId() { return groupId_ ; }
  
  public void setContacts(List<String> contactIds) { contactIds_ = contactIds ; }
  public List<String> getContacts() { return contactIds_ ; }
  
  public String[] getActions() { return new String[] {"Save", "Cancel"} ; }
  
  public ContactGroup[] getContactGroups() throws Exception { 
    UIContactPortlet uiContactPortlet = getAncestorOfType(UIContactPortlet.class) ;
    UIAddressBooks uiAddressBook = uiContactPortlet.findFirstComponentOfType(UIAddressBooks.class) ;
    return uiAddressBook.getGroups(); 
  }
  
  public List<String> getSharedContactGroups() throws Exception {
    List<String> groupsName = new ArrayList<String>() ;
    OrganizationService organizationService = (OrganizationService)PortalContainer.getComponent(OrganizationService.class) ;
    Object[] groups = organizationService.getGroupHandler().getAllGroups().toArray() ;
    for(Object group : groups) {
      String groupName = ((GroupImpl)group).getId() ;
      groupsName.add(groupName) ;
    }
    return groupsName ;
  }

  static  public class SelectGroupActionListener extends EventListener<UIMoveContactForm> {
    public void execute(Event<UIMoveContactForm> event) throws Exception {
      UIMoveContactForm uiMoveContactForm = event.getSource() ;
      String groupId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIContactPortlet uiContactPortlet = uiMoveContactForm.getAncestorOfType(UIContactPortlet.class);
      UIContacts uiContacts = uiContactPortlet.findFirstComponentOfType(UIContacts.class) ;
      if (!uiMoveContactForm.getGroupId().equals(groupId)) {
        ContactService contactService = ContactUtils.getContactService();
        String username = ContactUtils.getCurrentUser() ;
        List<Contact> movedContacts = contactService.moveContacts(username, uiMoveContactForm.getContacts(), new String[] { groupId }) ;
        if (movedContacts.size() > 0) {
          List<String> contactIds = new ArrayList<String>() ;
          for (Contact contact : movedContacts) contactIds.add(contact.getId()) ;
          uiContacts.removeContacts(contactIds) ;
        }
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContactPortlet) ;
      uiContactPortlet.cancelAction() ;
    }
  }
  
  static  public class SaveActionListener extends EventListener<UIMoveContactForm> {
    public void execute(Event<UIMoveContactForm> event) throws Exception {
      
    }
  }
  static  public class CancelActionListener extends EventListener<UIMoveContactForm> {
    public void execute(Event<UIMoveContactForm> event) throws Exception {
      UIMoveContactForm uiForm = event.getSource() ;
      UIContactPortlet contactPortlet = uiForm.getAncestorOfType(UIContactPortlet.class) ;
      contactPortlet.cancelAction() ; 
    }
  }
  
}
