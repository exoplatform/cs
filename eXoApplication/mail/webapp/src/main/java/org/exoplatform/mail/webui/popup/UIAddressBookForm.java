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
package org.exoplatform.mail.webui.popup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.download.DownloadService;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.SessionsUtils;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;

/**
 * Created by The eXo Platform SARL
 * Author : Phung Nam
 *          phunghainam@gmail.com
 * Nov 01, 2007 8:48:18 AM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/templates/mail/webui/UIAddressBookForm.gtmpl",
    events = {  
      @EventConfig(listeners = UIAddressBookForm.AddContactActionListener.class),
      @EventConfig(listeners = UIAddressBookForm.SelectContactActionListener.class),
      @EventConfig(listeners = UIAddressBookForm.DeleteContactActionListener.class),
      @EventConfig(listeners = UIAddressBookForm.CloseActionListener.class)
    }
)
public class UIAddressBookForm extends UIForm implements UIPopupComponent{
  public final static String ALL_GROUP = "All group".intern();
  public final static String SELECT_GROUP = "select-group".intern();
  private Contact selectedContact ;
  Map<String, Contact> contactMap_ = new HashMap<String, Contact>() ;
  List<Contact> contactList_ = new ArrayList<Contact>();
  
  public UIAddressBookForm() throws Exception {
    String username = MailUtils.getCurrentUser();
    ContactService contactSrv = getApplicationComponent(ContactService.class);
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
    options.add(new SelectItemOption<String>(ALL_GROUP, ""));
    for (ContactGroup group : contactSrv.getGroups(SessionsUtils.getSessionProvider(), username)) {
      options.add(new SelectItemOption<String>(group.getName(), group.getId()));
    }
    UIFormSelectBox uiSelectGroup = new UIFormSelectBox(SELECT_GROUP, SELECT_GROUP, options);
    addUIFormInput(uiSelectGroup);
    
    List<Contact> contactList = contactSrv.getAllContact(SessionsUtils.getSessionProvider(), username);
    for (Contact ct : contactList) contactMap_.put(ct.getId(), ct);
    contactList_ = new ArrayList<Contact>(contactMap_.values());
    
    if (contactList_.size() > 0) selectedContact = contactList_.get(0);
  }
  
  public Contact getSelectedContact() { return this.selectedContact; }
  public void setSelectedContact(Contact contact) { this.selectedContact = contact; }
  
  public DownloadService getDownloadService() { 
    return getApplicationComponent(DownloadService.class) ; 
  }
  
  public List<Contact> getContacts() throws Exception { return contactList_ ;}
  
  
  public String[] getActions() { return new String[] {"Close"}; }
  
  public void activate() throws Exception { }

  public void deActivate() throws Exception { }
  
  static public class AddContactActionListener extends EventListener<UIAddressBookForm> {
    public void execute(Event<UIAddressBookForm> event) throws Exception {
      UIAddressBookForm uiAddressBookForm = event.getSource() ;
      UIPopupActionContainer uiActionContainer = uiAddressBookForm.getParent() ;
      UIPopupAction uiChildPopup = uiActionContainer.getChild(UIPopupAction.class) ;
      UIPopupActionContainer uiPopupContainer = uiChildPopup.activate(UIPopupActionContainer.class, 730) ;
      uiPopupContainer.setId("UIAddContactContainer");
      uiPopupContainer.addChild(UIAddContactForm.class, null, null) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;
    }
  }
  
  static public class SelectContactActionListener extends EventListener<UIAddressBookForm> {
    public void execute(Event<UIAddressBookForm> event) throws Exception {
      UIAddressBookForm uiAddressBook = event.getSource() ;
      String contactId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIMailPortlet uiPortlet = uiAddressBook.getAncestorOfType(UIMailPortlet.class);
      uiAddressBook.setSelectedContact(uiAddressBook.contactMap_.get(contactId));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.getChild(UIPopupAction.class)) ;
    }
  }
  
  static public class DeleteContactActionListener extends EventListener<UIAddressBookForm> {
    public void execute(Event<UIAddressBookForm> event) throws Exception {
      UIAddressBookForm uiAddressBookForm = event.getSource() ;
      UIMailPortlet mailPortlet = uiAddressBookForm.getAncestorOfType(UIMailPortlet.class);
      Contact contact = uiAddressBookForm.getSelectedContact();
      String username = MailUtils.getCurrentUser();
      ContactService contactServ = uiAddressBookForm.getApplicationComponent(ContactService.class);
      try {
        List<String> contactIds = new ArrayList<String>();
        contactIds.add(contact.getId());
        contactServ.removeContacts(SessionsUtils.getSessionProvider(), username, contactIds);
        if (uiAddressBookForm.contactList_.size() > 0) uiAddressBookForm.selectedContact = uiAddressBookForm.contactList_.get(0);
        event.getRequestContext().addUIComponentToUpdateByAjax(mailPortlet.getChild(UIPopupAction.class)) ;
      } catch(Exception e) {
        e.printStackTrace();
      } 
    }
  }
  
  static public class CloseActionListener extends EventListener<UIAddressBookForm> {
    public void execute(Event<UIAddressBookForm> event) throws Exception {
      UIAddressBookForm uiAddressBookForm = event.getSource();
      uiAddressBookForm.getAncestorOfType(UIMailPortlet.class).cancelAction();
    }
  }
}
