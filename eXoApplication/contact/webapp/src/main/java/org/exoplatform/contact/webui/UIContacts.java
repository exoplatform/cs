/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
    template =  "app:/templates/contact/webui/UIContacts.gtmpl",
    events = {
        @EventConfig(listeners = UIContacts.SelectedContactActionListener.class)
    }
)

public class UIContacts extends UIContainer  {
  private String selectedGroupId_ ;
  private boolean isPersonalContact_ = true ;
  public UIContacts() throws Exception {}  

  protected String getGroupId() { return selectedGroupId_;}
  protected void setGroupId(String id) { selectedGroupId_ = id  ;}

  public void setIsPersonalContact(boolean isPersonal) {
    isPersonalContact_ = isPersonal ;
  } 
  
  public List<Contact> getContacts() throws Exception {
    List<Contact> contacts = new ArrayList<Contact>() ;
    if(isPersonalContact_) {
      if(selectedGroupId_ != null) {
        ContactService contactService = 
          (ContactService)PortalContainer.getComponent(ContactService.class) ;
        String username = Util.getPortalRequestContext().getRemoteUser() ;
        contacts.addAll(contactService.getContactsByGroup(username, getGroupId()));
      }
    } else {
      if(selectedGroupId_ != null) {
        UIContactPortlet uiPortlet = getAncestorOfType(UIContactPortlet.class) ;
        UIAddressBooks uiAddressBooks = uiPortlet.findFirstComponentOfType(UIAddressBooks.class) ;
        contacts.addAll(uiAddressBooks.getContactMapValue(selectedGroupId_)) ;
      }
    }
    return contacts ;
  }

  static public class SelectedContactActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      String contactId = event.getRequestContext().getRequestParameter(OBJECTID);
      ContactService contactService = uiContacts.getApplicationComponent(ContactService.class);
      String username = Util.getPortalRequestContext().getRemoteUser() ;
      Contact contact;
      if (uiContacts.isPersonalContact_) {
        contact = contactService.getContact(username, contactId);
      } else {
        contact = contactService.getSharedContact(contactId);
      }
      UIContactContainer uiContactContainer = uiContacts.getAncestorOfType(UIContactContainer.class);
      UIContactPreview uiContactPreview = uiContactContainer.findFirstComponentOfType(UIContactPreview.class);
      uiContactPreview.setContact(contact);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContactPreview);
    }
  }
}
