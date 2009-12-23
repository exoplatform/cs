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
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;

/**
 * Created by The eXo Platform SARL
 * Author : Phung Nam 
 *          phunghainam@gmail.com
 * Sep 25, 2007  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/templates/mail/webui/UIAddressForm.gtmpl",
    events = {
      @EventConfig(listeners = UIAddressSearchForm.SaveActionListener.class), 
      @EventConfig(listeners = UIAddressSearchForm.CancelActionListener.class, phase = Phase.DECODE)
    }
)

public class UIAddressSearchForm extends UIForm implements UIPopupComponent { 

  private String type = "";
  
  public UIAddressSearchForm() throws Exception {  
    setContactList();
  }
  
  public void setType(String type)  {
    this.type = type;
  }
  
  public String getType(){
    return type;
  }
    
  public String[] getActions() { return new String[]{"Save", "Cancel"}; }

  private Map<String, Contact> contactMap_ = new HashMap<String, Contact>(); 

  public void activate() throws Exception {}
  
  public void deActivate() throws Exception {} 
  
  public List<Contact> getContacts() throws Exception { 
    return new ArrayList<Contact>(contactMap_.values());
  }
  
  public void setContactList() throws Exception {
    setContactList("");
  }

  public void setContactList(String groupId) throws Exception {
    List<Contact> contacts = new ArrayList<Contact>();
    ContactService contactSrv = getApplicationComponent(ContactService.class);
    String username = Util.getPortalRequestContext().getRemoteUser();   
    if (groupId == null || groupId == "") {
      contacts = contactSrv.getPersonalContacts(username);
    } else {
      contacts = contactSrv.getPersonalContactsByAddressBook(username, groupId).getAll();
    }
    setContactList(contacts);
  }
  
  public void setContactList(List<Contact> contactList) throws Exception {
    getChildren().clear();
    contactMap_.clear();
    for (Contact contact : contactList) {
      UIFormCheckBoxInput<Boolean> uiCheckbox = new UIFormCheckBoxInput<Boolean>(contact.getId(), contact.getId(), false);
      addUIFormInput(uiCheckbox);   
      contactMap_.put(contact.getId(), contact);
    }        
  }
  
  public List<Contact> getCheckedContact() throws Exception {
    List<Contact> contactList = new ArrayList<Contact>();  
    for (Contact contact : getContacts()) {
      UIFormCheckBoxInput<Boolean> uiCheckbox = getChildById(contact.getId());
      if (uiCheckbox!=null && uiCheckbox.isChecked()) {
        contactList.add(contact);
      }
    }
    return contactList;
  }
  
  static  public class SaveActionListener extends EventListener<UIAddressSearchForm> { 
    public void execute(Event<UIAddressSearchForm> event) throws Exception { 
      UIAddressSearchForm uiAddressSearchForm = event.getSource();
      UIMailPortlet uiPortlet = uiAddressSearchForm.getAncestorOfType(UIMailPortlet.class);
      String toAddress = "";
      for (Contact contact : uiAddressSearchForm.getCheckedContact()) {   
        toAddress += MailUtils.listToString(contact.getEmailAddresses()) + "," ;
      }
      UIAdvancedSearchForm uiAdvancedSearchForm = uiPortlet.findFirstComponentOfType(UIAdvancedSearchForm.class);
      
      if(uiAddressSearchForm.getType().equals("To")){      
        uiAdvancedSearchForm.setToContacts(uiAddressSearchForm.getCheckedContact());
      }  
      
      if(uiAddressSearchForm.getType().equals("From")) {
        uiAdvancedSearchForm.setFieldEmailFrom(toAddress);       
      }      
      uiAddressSearchForm.deActivate();
    }  
  } 
  
  static  public class CancelActionListener extends EventListener<UIAddressSearchForm> {
    public void execute(Event<UIAddressSearchForm> event) throws Exception {
      UIAddressSearchForm uiAddressSearchForm = event.getSource();        
      uiAddressSearchForm.deActivate();
    }
  }
}
