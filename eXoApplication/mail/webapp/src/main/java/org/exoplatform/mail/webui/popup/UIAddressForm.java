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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactFilter;
import org.exoplatform.contact.service.AddressBook;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.DataPageList;
import org.exoplatform.contact.service.SharedAddressBook;
import org.exoplatform.contact.service.impl.NewUserListener;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.webui.CalendarUtils;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.impl.GroupImpl;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItem;
import org.exoplatform.webui.core.model.SelectOption;
import org.exoplatform.webui.core.model.SelectOptionGroup;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormSelectBoxWithGroups;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL 
 * Author : Phung Nam <phunghainam@gmail.com>
 * Sep 25, 2007
 */
@ComponentConfig(lifecycle = UIFormLifecycle.class, 
                 template = "app:/templates/mail/webui/popup/UIAddressForm.gtmpl", 
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

  public LinkedHashMap<String, Contact> checkedList_ = new LinkedHashMap<String, Contact>() ;
  public LinkedHashMap<String, Contact> newCheckedList_ = new LinkedHashMap<String, Contact>() ;
  private String avaiAddressStr = "";

  private String selectedAddressId_ = "" ;
  private String recipientsType_ = "";
  private UIPageIterator uiPageList_ ;

  public void setRecipientsType(String type) {
    recipientsType_ = type;
  }

  public String getRecipientType() {
    return recipientsType_;
  }

  public void setAvaiAddressStr(String str) {
    avaiAddressStr = str ;
  }

  public String getAvaiAddressStr() {
    return avaiAddressStr;
  }

  public UIAddressForm() throws Exception {
    addUIFormInput(new UIFormStringInput(CONTACT_SEARCH, CONTACT_SEARCH, null)) ;
    UIFormSelectBoxWithGroups uiSelect = new UIFormSelectBoxWithGroups(CONTACT_GROUP, CONTACT_GROUP, getOptions()) ;
    uiSelect.setOnChange("ChangeGroup") ;
    addUIFormInput(uiSelect) ;
    uiPageList_ = new UIPageIterator() ;
    uiPageList_.setId("UIMailAddressPage") ;
    String username = MailUtils.getCurrentUser();
    ContactService contactSrv = getApplicationComponent(ContactService.class);
    List<AddressBook> groups = contactSrv.getGroups(SessionProviderFactory.createSystemProvider(), username) ;
    if (groups != null && groups.size() > 0) {
      String category = groups.get(0).getId() ;
      setContactList(category) ;
    }
  }

  public List<SelectItem> getOptions() throws Exception {
    String username = MailUtils.getCurrentUser();
    ContactService contactSrv = getApplicationComponent(ContactService.class);
    List<SelectItem> options = new ArrayList<SelectItem>() ;
    List<AddressBook> contactGroup = contactSrv.getGroups(SessionProviderFactory.createSystemProvider(), username);
    if(!contactGroup.isEmpty()) {
      SelectOptionGroup personalContacts = new SelectOptionGroup("personal-contacts");
      for(AddressBook pcg : contactGroup) {
        personalContacts.addOption(new SelectOption(pcg.getName(), pcg.getId())) ;
      }
      options.add(personalContacts);
    }
    List<SharedAddressBook> sharedAdd = contactSrv.getSharedAddressBooks(username);
    if(!sharedAdd.isEmpty()) {
      SelectOptionGroup sharedContacts = new SelectOptionGroup("shared-contacts");
      for(SharedAddressBook scg : sharedAdd) {
        String name = "" ;
        if(!CalendarUtils.isEmpty(scg.getSharedUserId())) name = scg.getSharedUserId() + "-" ;
        sharedContacts.addOption(new SelectOption(name + scg.getName(), scg.getId())) ;
      }
      options.add(sharedContacts);
    }
    OrganizationService organizationService = 
      (OrganizationService)PortalContainer.getComponent(OrganizationService.class) ;
    Object[] objGroupIds = organizationService.getGroupHandler().findGroupsOfUser(username).toArray() ;
    List<String> groupIds = new ArrayList<String>() ;
    for (Object object : objGroupIds) {
      groupIds.add(((GroupImpl)object).getId()) ;
    }
    if(!groupIds.isEmpty()){
      SelectOptionGroup publicContacts = new SelectOptionGroup("public-contacts");
      for(String publicCg : groupIds) {
        publicContacts.addOption(new SelectOption(publicCg, publicCg)) ;
      }
      options.add(publicContacts);
    }
    return options ;
  }

  public String[] getActions() {
    return new String[] { "Save", "Cancel" };
  }

  public void activate() throws Exception { }
  public void deActivate() throws Exception { }

  @SuppressWarnings("unchecked")
  public List<Contact> getContacts() throws Exception {
    List<Contact> contacts = new ArrayList<Contact>(uiPageList_.getCurrentPageData()) ;
    for(Contact c : contacts) {
      UIFormCheckBoxInput uiInput = getUIFormCheckBoxInput(c.getId()) ;
      if(uiInput == null){
        uiInput = new UIFormCheckBoxInput<Boolean>(c.getId(),c.getId(), null);
        addUIFormInput(uiInput) ;
      }
    }
    for(Contact c : checkedList_.values()) {
      UIFormCheckBoxInput uiInput = getUIFormCheckBoxInput(c.getId()) ;
      if(uiInput != null) uiInput.setChecked(true) ;
    } 
    return contacts ;
  }

  public UIPageIterator  getUIPageIterator() {  return uiPageList_ ; }

  public long getAvailablePage(){ return uiPageList_.getAvailablePage() ; }

  public long getCurrentPage() { return uiPageList_.getCurrentPage(); }

  protected void updateCurrentPage(int page) throws Exception{
    uiPageList_.setCurrentPage(page) ;
  }

  public void setContactList(String groupId) throws Exception {
    ContactService contactSrv = getApplicationComponent(ContactService.class);
    ContactFilter filter = new ContactFilter() ;
    if(!MailUtils.isFieldEmpty(groupId)) {
      filter.setCategories(new String[]{ groupId }) ;
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

  @SuppressWarnings("unchecked")
  public List<Contact> getCheckedContact() throws Exception {
    List<Contact> contactList = new ArrayList<Contact>();
    for (Contact contact : new ArrayList<Contact>(uiPageList_.getCurrentPageData())) {
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
      String category = ((UIFormSelectBoxWithGroups)uiAddressForm.getChildById(UIAddressForm.CONTACT_GROUP)).getValue() ;
      if(category.equals(NewUserListener.DEFAULTGROUP)) category = category + MailUtils.getCurrentUser() ;
      uiAddressForm.selectedAddressId_ = category ;
      ContactFilter filter = new ContactFilter() ;
      if(!MailUtils.isFieldEmpty(uiAddressForm.selectedAddressId_)) {
        filter.setCategories(new String[]{ uiAddressForm.selectedAddressId_ }) ;
      }
      DataPageList resultPageList = 
        contactService.searchContact(SessionProviderFactory.createSystemProvider(), MailUtils.getCurrentUser(), filter) ;
      uiAddressForm.setContactList(resultPageList.getAll()) ;
      uiAddressForm.getUIStringInput(UIAddressForm.CONTACT_SEARCH).setValue(null) ;
      ((UIFormSelectBoxWithGroups)uiAddressForm.getChildById(UIAddressForm.CONTACT_GROUP)).setValue(uiAddressForm.selectedAddressId_) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressForm) ;
    }
  }

  static public class SearchContactActionListener extends EventListener<UIAddressForm> {
    public void execute(Event<UIAddressForm> event) throws Exception {
      UIAddressForm uiForm = event.getSource() ;  
      ContactService contactService = uiForm.getApplicationComponent(ContactService.class) ;
      String text = uiForm.getUIStringInput(UIAddressForm.CONTACT_SEARCH).getValue() ;
      String category = ((UIFormSelectBoxWithGroups)uiForm.getChildById(UIAddressForm.CONTACT_GROUP)).getValue() ;
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
        ((UIFormSelectBoxWithGroups)uiForm.getChildById(UIAddressForm.CONTACT_GROUP)).setValue(category) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
      } catch (Exception e) {
        UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIAddressForm.msg.search-error-keyword", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
    }
  }

  static public class ReplaceActionListener extends EventListener<UIAddressForm> {
    public void execute(Event<UIAddressForm> event) throws Exception {
      UIAddressForm uiAddressForm = event.getSource() ;
      List<Contact> checkedContact = uiAddressForm.getCheckedContact();
      if(checkedContact.isEmpty()) {
        UIApplication uiApp = uiAddressForm.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIAddressForm.msg.contact-email-required",null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      UIPopupActionContainer uiPopupContainer = uiAddressForm.getAncestorOfType(UIPopupActionContainer.class) ;
      UIPopupAction childPopup = uiAddressForm.getAncestorOfType(UIPopupAction.class);
      uiAddressForm.checkedList_.clear() ;
      uiAddressForm.newCheckedList_.clear() ;
      String toAddress = "";

      for (Contact contact : checkedContact) {
        if(MailUtils.isFieldEmpty(contact.getEmailAddress())) {
          UIApplication uiApp = uiAddressForm.getAncestorOfType(UIApplication.class) ;
          uiApp.addMessage(new ApplicationMessage("UIAddressForm.msg.you-should-only-choose-contact-with-email-address", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
      }
      for (Contact ct : checkedContact) {
        uiAddressForm.newCheckedList_.put(ct.getId(), ct) ;
      }

      UIComposeForm uiComposeForm = uiPopupContainer.getChild(UIComposeForm.class) ;
      UIEventForm uiEventForm = uiPopupContainer.getChild(UIEventForm.class) ;
      StringBuffer sb = new StringBuffer() ;
      for (Contact contact : uiAddressForm.newCheckedList_.values()) {
        String addresses = contact.getEmailAddress();
        if(addresses != null && addresses.trim().length() > 0) {
          String add = contact.getEmailAddress().replace(";", ",") ;
          if(uiEventForm != null) {
           if(sb.length() > 0) sb.append(",") ;
            sb.append(add) ;
          } else {  
            String[] eAddresses = null;
            if (addresses.contains(";")) eAddresses = addresses.split(";") ;
            if (eAddresses != null) {
              for (int i = 0; i < eAddresses.length; i++) {
                toAddress += contact.getFullName() + "<" + eAddresses[i] + "> ," ;
              }
            } else {
              toAddress += contact.getFullName() + "<" + contact.getEmailAddress() + "> ," ;
            }
          }
        }
      }
      /*List<String> listMail = Arrays.asList( sb.toString().split(MailUtils.COMMA)) ; 
      String email = null ;
      for(Contact c : checkedContact) {
        email = c.getEmailAddress() ;
        if(!listMail.contains(email)) {
          if(sb != null && sb.length() > 0) sb.append(MailUtils.COMMA) ;
          if(email != null) sb.append(email) ;
        }
      }*/
      if(uiEventForm != null) {
        uiEventForm.setSelectedTab(UIEventForm.TAB_EVENTREMINDER) ;
        uiEventForm.setEmailAddress(sb.toString()) ;
        uiAddressForm.checkedList_ = uiAddressForm.newCheckedList_ ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiEventForm) ;
      } else if (uiComposeForm != null) {
        if (uiAddressForm.getRecipientType().equals("to")) {
          uiComposeForm.setFieldToValue(toAddress) ;
          uiComposeForm.setToContacts(checkedContact) ;
        }
        if (uiAddressForm.getRecipientType().equals("cc")) {
          uiComposeForm.setFieldCcValue(toAddress) ;
          uiComposeForm.setCcContacts(checkedContact) ;
        }

        if (uiAddressForm.getRecipientType().equals("bcc")) {
          uiComposeForm.setFieldBccValue(toAddress) ;
          uiComposeForm.setBccContacts(checkedContact) ;
        }
        uiAddressForm.checkedList_ = uiAddressForm.newCheckedList_ ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiComposeForm.getChildById(UIComposeForm.FIELD_TO_SET)) ;
      }
      childPopup.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(childPopup) ;
    }
  }

  static public class AddActionListener extends EventListener<UIAddressForm> {
    public void execute(Event<UIAddressForm> event) throws Exception {
      UIAddressForm uiAddressForm = event.getSource() ;
      List<Contact> checkedContact = uiAddressForm.getCheckedContact();     
      if(checkedContact.size() <= 0) {
        UIApplication uiApp = uiAddressForm.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIAddressForm.msg.contact-email-required",null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      UIMailPortlet uiPortlet = uiAddressForm.getAncestorOfType(UIMailPortlet.class) ;

      UIComposeForm uiComposeForm = uiPortlet.findFirstComponentOfType(UIComposeForm.class) ;
      UIEventForm uiEventForm = uiPortlet.findFirstComponentOfType(UIEventForm.class) ;
      for (Contact contact : checkedContact) {
        if(MailUtils.isFieldEmpty(contact.getEmailAddress())) {
          UIApplication uiApp = uiAddressForm.getAncestorOfType(UIApplication.class) ;
          uiApp.addMessage(new ApplicationMessage("UIAddressForm.msg.you-should-only-choose-contact-with-email-address", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
      }
      for (Contact ct : checkedContact) {
        if (!uiAddressForm.checkedList_.containsKey(ct.getId())) uiAddressForm.newCheckedList_.put(ct.getId(), ct) ;
      }
      if(uiEventForm != null) {
        StringBuffer sb = new StringBuffer() ;
        if(uiEventForm.getEmailAddress() != null && uiEventForm.getEmailAddress().trim().length() > 0) {
          sb.append(uiEventForm.getEmailAddress()) ;
        }
        List<String> listMail = Arrays.asList( sb.toString().split(MailUtils.COMMA)) ; 
        String email = null ;
        for(Contact c : uiAddressForm.newCheckedList_.values()) {
          email = c.getEmailAddress() ;
          if(!listMail.contains(email)) {
            if(sb != null && sb.length() > 0) sb.append(MailUtils.COMMA) ;
            if(email != null) sb.append(email.replace(";", ",")) ;
          }
        }
        uiEventForm.setSelectedTab(UIEventForm.TAB_EVENTREMINDER) ;
        uiEventForm.setEmailAddress(sb.toString()) ;
        uiAddressForm.checkedList_ = uiAddressForm.newCheckedList_ ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiEventForm) ;
        return ;
      } else if (uiComposeForm != null) {
        String toAddress = uiAddressForm.getAvaiAddressStr() != null ? uiAddressForm.getAvaiAddressStr() : "";
        if (!toAddress.equals("") && !toAddress.endsWith(",")) {
          toAddress = toAddress + "," ;
        }

        for (Contact ct : checkedContact) {
          if (!uiAddressForm.checkedList_.containsKey(ct.getId())) uiAddressForm.newCheckedList_.put(ct.getId(), ct) ;
        }
        for (Contact contact : uiAddressForm.newCheckedList_.values()) {
          String addresses = contact.getEmailAddress();
          if(addresses != null && addresses.trim().length() > 0) {
            String[] eAddresses = null;
            if (addresses.contains(";")) eAddresses = addresses.split(";") ;
            if (eAddresses != null) {
              for (int i = 0; i < eAddresses.length; i++) {
                toAddress += contact.getFullName() + "<" + eAddresses[i] + "> ," ;
              }
            } else {
              toAddress += contact.getFullName() + "<" + contact.getEmailAddress() + "> ," ;
            }
          } 
        }
        if (uiAddressForm.getRecipientType().equals("to")) {
          uiComposeForm.setFieldToValue(toAddress) ;
          uiComposeForm.setToContacts(new ArrayList<Contact>(uiAddressForm.newCheckedList_.values())) ;
        } else if (uiAddressForm.getRecipientType().equals("cc")) {
          uiComposeForm.setFieldCcValue(toAddress) ;
          uiComposeForm.setCcContacts(new ArrayList<Contact>(uiAddressForm.newCheckedList_.values())) ;
        } else if (uiAddressForm.getRecipientType().equals("bcc")) {
          uiComposeForm.setFieldBccValue(toAddress) ;
          uiComposeForm.setBccContacts(new ArrayList<Contact>(uiAddressForm.newCheckedList_.values())) ;
        }

        uiAddressForm.checkedList_ = uiAddressForm.newCheckedList_ ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiComposeForm.getChildById(UIComposeForm.FIELD_TO_SET)) ;
      }
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

  static public class ShowPageActionListener extends EventListener<UIAddressForm> {
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
