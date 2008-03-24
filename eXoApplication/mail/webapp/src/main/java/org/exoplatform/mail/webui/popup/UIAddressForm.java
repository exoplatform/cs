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

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactFilter;
import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.DataPageList;
import org.exoplatform.contact.service.SharedAddressBook;
import org.exoplatform.contact.service.impl.NewUserListener;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.organization.impl.GroupImpl;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL 
 * Author : Nguyen Hai <haiexo10022@gmail.com>
 *          Phung Nam <phunghainam@gmail.com>
 * Sep 25, 2007
 */
@ComponentConfig(lifecycle = UIFormLifecycle.class, 
    template = "app:/templates/mail/webui/UIAddressForm.gtmpl", 
    events = {
      @EventConfig(listeners = UIAddressForm.ChangeGroupActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIAddressForm.SearchContactActionListener.class),
      @EventConfig(listeners = UIAddressForm.AddActionListener.class),
      @EventConfig(listeners = UIAddressForm.ReplaceActionListener.class),
      @EventConfig(listeners = UIAddressForm.ShowPageActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIAddressForm.CancelActionListener.class, phase = Phase.DECODE)
    })
    
public class UIAddressForm extends UIForm implements UIPopupComponent {
  public static final String CONTACT_SEARCH = "contact-search".intern();
  public static final String CONTACT_GROUP = "contact-group".intern();
  
  public Map<String, Contact> checkedList_ = new HashMap<String, Contact>() ;
  public Map<String, Contact> newCheckedList_ = new HashMap<String, Contact>() ;
  
  private String selectedAddressId_ = "" ;
  private String recipientsType_ = "";
  private UIPageIterator uiPageList_ ;
  public void setRecipientsType(String type) {
    recipientsType_ = type;
  }
  public String getRecipientType() {
    return recipientsType_;
  }

  public UIAddressForm() throws Exception {
    addUIFormInput(new UIFormStringInput(CONTACT_SEARCH, CONTACT_SEARCH, null)) ;
    UIFormSelectBox uiSelect = new UIFormSelectBox(CONTACT_GROUP, CONTACT_GROUP, getGroups()) ;
    uiSelect.setOnChange("ChangeGroup") ;
    addUIFormInput(uiSelect) ;
    uiPageList_ = new UIPageIterator() ;
    uiPageList_.setId("UIMailAddressPage") ;
  }

  public List<SelectItemOption<String>> getGroups() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    ContactService contactService = getApplicationComponent(ContactService.class) ;
    options.add(new SelectItemOption<String>("All Group", "")) ;
    SessionProvider sessionPro = SessionProviderFactory.createSessionProvider() ;
    for( ContactGroup cg : contactService.getGroups(sessionPro, MailUtils.getCurrentUser())) {
      options.add(new SelectItemOption<String>(cg.getName(), cg.getId())) ;
    }
    List<SharedAddressBook> addressList = contactService.getSharedAddressBooks(sessionPro, MailUtils.getCurrentUser()) ;
    for(SharedAddressBook sa : addressList) {
      options.add(new SelectItemOption<String>(sa.getName(), sa.getId())) ;
    }
    Object[] objGroups = 
      MailUtils.getOrganizationService().getGroupHandler().findGroupsOfUser(MailUtils.getCurrentUser()).toArray() ;
    for (Object object : objGroups) {
      if(object != null) {
        GroupImpl g = (GroupImpl)object ;
        options.add(new SelectItemOption<String>(g.getGroupName(), g.getId())) ;
      }
    }
    
    return options;
  }
  
  public String[] getActions() {
    return new String[] { "Save", "Cancel" };
  }

  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
  
  public List<Contact> getContacts() throws Exception {
    List<Contact> contacts = new ArrayList<Contact>(uiPageList_.getCurrentPageData()) ;
    for(Contact c : contacts) {
      UIFormCheckBoxInput uiInput = getUIFormCheckBoxInput(c.getId()) ;
      if(uiInput == null) addUIFormInput(new UIFormCheckBoxInput<Boolean>(c.getId(),c.getId(), null)) ;
    }
    for(Contact c : checkedList_.values()) {
      UIFormCheckBoxInput uiInput = getUIFormCheckBoxInput(c.getId()) ;
      if(uiInput != null) uiInput.setChecked(true) ;
    }
    return contacts ;
  }
  
  public UIPageIterator  getUIPageIterator() {  return uiPageList_ ; }
  public long getAvailablePage(){ return uiPageList_.getAvailablePage() ;}
  public long getCurrentPage() { return uiPageList_.getCurrentPage();}
  
  protected void updateCurrentPage(int page) throws Exception{
    uiPageList_.setCurrentPage(page) ;
  }
  
  public void setContactList(String groupId) throws Exception {
    ContactService contactSrv = getApplicationComponent(ContactService.class);
    ContactFilter filter = new ContactFilter() ;
    if(!MailUtils.isFieldEmpty(groupId)) {
      filter.setCategories(new String[]{groupId}) ;
    }  
    DataPageList resultPageList = 
      contactSrv.searchContact(SessionProviderFactory.createSystemProvider(), MailUtils.getCurrentUser(), filter) ;
    setContactList(resultPageList.getAll()); 
  }

  public void setContactList(List<Contact> contactList) throws Exception {
    ObjectPageList objPageList = new ObjectPageList(contactList, 10) ;
    uiPageList_.setPageList(objPageList) ;
  }
  
  public void setAlreadyCheckedContact(List<Contact> alreadyCheckedContact) throws Exception {
    for (Contact ct : alreadyCheckedContact) {
      checkedList_.put(ct.getId(), ct) ;
    }
  }
  
  public List<Contact> getCheckedContact() throws Exception {
    List<Contact> contactList = new ArrayList<Contact>();
    for (Contact contact : getContacts()) {
      UIFormCheckBoxInput<Boolean> uiCheckbox = getChildById(contact.getId());
      if (uiCheckbox != null && uiCheckbox.isChecked()) {
        contactList.add(contact);
      }
    }
    return contactList;
  }
  
  static public class ChangeGroupActionListener extends EventListener<UIAddressForm> {
    public void execute(Event<UIAddressForm> event) throws Exception {
      UIAddressForm uiAddressForm = event.getSource();  
      ContactService contactService = uiAddressForm.getApplicationComponent(ContactService.class) ;
      String category = uiAddressForm.getUIFormSelectBox(UIAddressForm.CONTACT_GROUP).getValue() ;
      if(category.equals(NewUserListener.DEFAULTGROUP)) category = category + MailUtils.getCurrentUser() ;
      uiAddressForm.selectedAddressId_ = category ;
      ContactFilter filter = new ContactFilter() ;
      if(!MailUtils.isFieldEmpty(uiAddressForm.selectedAddressId_)) {
        filter.setCategories(new String[]{uiAddressForm.selectedAddressId_}) ;
      }
      DataPageList resultPageList = 
        contactService.searchContact(SessionProviderFactory.createSystemProvider(), event.getRequestContext().getRemoteUser(), filter) ;
      uiAddressForm.setContactList(resultPageList.getAll()) ;
      uiAddressForm.getUIStringInput(UIAddressForm.CONTACT_SEARCH).setValue(null) ;
      uiAddressForm.getUIFormSelectBox(UIAddressForm.CONTACT_GROUP).setValue(uiAddressForm.selectedAddressId_) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressForm) ;
    }
  }
  
  static public class SearchContactActionListener extends EventListener<UIAddressForm> {
    public void execute(Event<UIAddressForm> event) throws Exception {
      UIAddressForm uiForm = event.getSource() ;  
      ContactService contactService = uiForm.getApplicationComponent(ContactService.class) ;
      String text = uiForm.getUIStringInput(UIAddressForm.CONTACT_SEARCH).getValue() ;
      String category = uiForm.getUIFormSelectBox(UIAddressForm.CONTACT_GROUP).getValue() ;
      if(category.equals(NewUserListener.DEFAULTGROUP)) category = category + MailUtils.getCurrentUser() ;
      uiForm.selectedAddressId_ = category ;
      try {
        ContactFilter filter = new ContactFilter() ;
        if(!MailUtils.isFieldEmpty(uiForm.selectedAddressId_)) {
          filter.setCategories(new String[]{uiForm.selectedAddressId_}) ;
        } 
        if(!MailUtils.isFieldEmpty(text))
        filter.setText(MailUtils.encodeJCRText(text)) ;
        DataPageList resultPageList = 
          contactService.searchContact(SessionProviderFactory.createSystemProvider(), event.getRequestContext().getRemoteUser(), filter) ;
        uiForm.setContactList(resultPageList.getAll()) ;
        uiForm.getUIFormSelectBox(UIAddressForm.CONTACT_GROUP).setValue(category) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
      } catch (Exception e) {
        UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIAddressForm.msg.search-error-keyword", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      }
    }
  }

  static public class ReplaceActionListener extends EventListener<UIAddressForm> {
    public void execute(Event<UIAddressForm> event) throws Exception {
      UIAddressForm uiAddressForm = event.getSource() ;
      UIMailPortlet uiPortlet = uiAddressForm.getAncestorOfType(UIMailPortlet.class) ;
      String toAddress = "";
      uiAddressForm.checkedList_.clear() ;
      for (Contact ct : uiAddressForm.getCheckedContact()) {
        toAddress += ct.getFullName() + "<" + ct.getEmailAddress() + "> ," ;
        uiAddressForm.checkedList_.put(ct.getId(), ct) ;
      }
      UIComposeForm uiComposeForm = uiPortlet.findFirstComponentOfType(UIComposeForm.class) ;
      if (uiAddressForm.getRecipientType().equals("to")) {
        uiComposeForm.setFieldToValue(toAddress) ;
        uiComposeForm.setToContacts(new ArrayList<Contact>(uiAddressForm.checkedList_.values())) ;
      }

      if (uiAddressForm.getRecipientType().equals("cc")) {
        uiComposeForm.setFieldCcValue(toAddress) ;
        uiComposeForm.setCcContacts(new ArrayList<Contact>(uiAddressForm.checkedList_.values())) ;
      }

      if (uiAddressForm.getRecipientType().equals("bcc")) {
        uiComposeForm.setFieldBccValue(toAddress) ;
        uiComposeForm.setBccContacts(new ArrayList<Contact>(uiAddressForm.checkedList_.values())) ;
      }
    }
  }

  static public class AddActionListener extends EventListener<UIAddressForm> {
    public void execute(Event<UIAddressForm> event) throws Exception {
      UIAddressForm uiAddressForm = event.getSource() ;
      UIMailPortlet uiPortlet = uiAddressForm.getAncestorOfType(UIMailPortlet.class) ;
      String toAddress = "";
      for (Contact ct : uiAddressForm.getCheckedContact()) {
        uiAddressForm.newCheckedList_.put(ct.getId(), ct) ;
      }
      for (Contact contact : uiAddressForm.newCheckedList_.values()) {
        toAddress += contact.getFullName() + "<" + contact.getEmailAddress() + "> ," ;
      }
      UIComposeForm uiComposeForm = uiPortlet.findFirstComponentOfType(UIComposeForm.class) ;
      if (uiAddressForm.getRecipientType().equals("to")) {
        uiComposeForm.setFieldToValue(toAddress) ;
        uiComposeForm.setToContacts(new ArrayList<Contact>(uiAddressForm.newCheckedList_.values())) ;
      }

      if (uiAddressForm.getRecipientType().equals("cc")) {
        uiComposeForm.setFieldCcValue(toAddress) ;
        uiComposeForm.setCcContacts(new ArrayList<Contact>(uiAddressForm.newCheckedList_.values())) ;
      }

      if (uiAddressForm.getRecipientType().equals("bcc")) {
        uiComposeForm.setFieldBccValue(toAddress) ;
        uiComposeForm.setBccContacts(new ArrayList<Contact>(uiAddressForm.newCheckedList_.values())) ;
      }
      uiAddressForm.checkedList_ = uiAddressForm.newCheckedList_ ;
    }
  }

  static public class CancelActionListener extends EventListener<UIAddressForm> {
    public void execute(Event<UIAddressForm> event) throws Exception {
      UIAddressForm uiAddressForm = event.getSource() ;
      UIPopupAction uiPopupAction = uiAddressForm.getAncestorOfType(UIPopupAction.class) ; 
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }
  
  static  public class ShowPageActionListener extends EventListener<UIAddressForm> {
    public void execute(Event<UIAddressForm> event) throws Exception {
      UIAddressForm uiAddressForm = event.getSource() ;
      int page = Integer.parseInt(event.getRequestContext().getRequestParameter(OBJECTID)) ;
      for (Contact ct : uiAddressForm.getCheckedContact()) {
        uiAddressForm.newCheckedList_.put(ct.getId(), ct) ;
        uiAddressForm.checkedList_.put(ct.getId(), ct) ;
      }
      uiAddressForm.updateCurrentPage(page) ; 
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressForm);           
    }
  }
}
