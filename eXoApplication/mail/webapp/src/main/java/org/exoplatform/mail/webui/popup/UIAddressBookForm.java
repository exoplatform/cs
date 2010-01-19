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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.jcr.PathNotFoundException;

import org.exoplatform.mail.webui.popup.UIPopupAction;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactFilter;
import org.exoplatform.contact.service.AddressBook;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.DataStorage;
import org.exoplatform.contact.service.SharedAddressBook;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.mail.webui.UISelectAccount;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItem;
import org.exoplatform.webui.core.model.SelectOption;
import org.exoplatform.webui.core.model.SelectOptionGroup;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBoxWithGroups;

/**
 * Created by The eXo Platform SARL
 * Author : Phung Nam
 *          phunghainam@gmail.com
 * Nov 01, 2007 8:48:18 AM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/templates/mail/webui/popup/UIAddressBookForm.gtmpl",
    events = {  
    	@EventConfig(listeners = UIAddressBookForm.AddNewGroupActionListener.class),
      @EventConfig(listeners = UIAddressBookForm.AddContactActionListener.class),
      @EventConfig(listeners = UIAddressBookForm.EditContactActionListener.class),
      @EventConfig(listeners = UIAddressBookForm.ChangeGroupActionListener.class),
      @EventConfig(listeners = UIAddressBookForm.CheckContactActionListener.class),
      @EventConfig(listeners = UIAddressBookForm.SelectContactActionListener.class),
      @EventConfig(listeners = UIAddressBookForm.SendEmailActionListener.class),
      @EventConfig(listeners = UIAddressBookForm.DeleteContactActionListener.class, confirm="UIAddressBookForm.msg.confirm-remove-contact"),
      @EventConfig(listeners = UIAddressBookForm.CloseActionListener.class),
      @EventConfig(listeners = UIAddressBookForm.SendMultiEmailActionListener.class)
    }
)
public class UIAddressBookForm extends UIForm implements UIPopupComponent{
  public final static String ALL_GROUP = "All group".intern();
  public final static String SELECT_GROUP = "select-group".intern();
  private Contact selectedContact ;
  LinkedHashMap<String, Contact> contactMap_ = new LinkedHashMap<String, Contact>() ;
  List<Contact> contactList_ = new ArrayList<Contact>();
  HashMap<String, String> checkedContactMap = new LinkedHashMap<String, String>();
  private String sharedContacts_ = "sharedContacts";
  
  public UIAddressBookForm() throws Exception {
    UIFormSelectBoxWithGroups uiSelectGroup = new UIFormSelectBoxWithGroups(SELECT_GROUP, SELECT_GROUP, getOptions());
    uiSelectGroup.setOnChange("ChangeGroup");
    addUIFormInput(uiSelectGroup);
    refrestContactList(uiSelectGroup.getValue());
  }
  
  public boolean havePermission(String groupId) throws Exception { 
    String currentUser = MailUtils.getCurrentUser() ;
    AddressBook sharedGroup = getApplicationComponent(ContactService.class).getSharedAddressBook(currentUser, groupId) ;
    if (sharedGroup == null) return false ;
    if (sharedGroup.getEditPermissionUsers() != null &&
        Arrays.asList(sharedGroup.getEditPermissionUsers()).contains(currentUser + DataStorage.HYPHEN)) {
      return true ;
    }
    String[] editPerGroups = sharedGroup.getEditPermissionGroups() ;
    if (editPerGroups != null)
      for (String editPer : editPerGroups)
        if (MailUtils.getUserGroups().contains(editPer)) return true ;
    return false ;
  }
  
  public boolean havePermission(Contact contact) throws Exception {
    if (!contact.getContactType().equals(DataStorage.SHARED)) return true ;
    // contact shared
    String currentUser = MailUtils.getCurrentUser() ;
    if (contact.getEditPermissionUsers() != null &&
        Arrays.asList(contact.getEditPermissionUsers()).contains(currentUser + DataStorage.HYPHEN)) {
      return true ;
    }
    String[] editPerGroups = contact.getEditPermissionGroups() ;
    if (editPerGroups != null)
      for (String editPer : editPerGroups)
        if (MailUtils.getUserGroups().contains(editPer)) {
          return true ;
        }
    
    if (!getChild(UIFormSelectBoxWithGroups.class).getValue().equals(sharedContacts_)) {
      if (havePermission(getChild(UIFormSelectBoxWithGroups.class).getValue())) return true ;      
    }
    return false ;    
  }
  
  public String getPortalName() {
    PortalContainer pcontainer =  PortalContainer.getInstance() ;
    return pcontainer.getPortalContainerInfo().getContainerName() ;  
  }
  public String getRepository() throws Exception {
    RepositoryService rService = getApplicationComponent(RepositoryService.class) ;    
    return rService.getCurrentRepository().getConfiguration().getName() ;
  }
  
  
  public List<SelectItem> getOptions() throws Exception {
    String username = MailUtils.getCurrentUser();
    ContactService contactSrv = getApplicationComponent(ContactService.class);
    List<SelectItem> options = new ArrayList<SelectItem>() ;
    SelectOptionGroup personalContacts = new SelectOptionGroup("personal-contacts");
    for(AddressBook pcg : contactSrv.getGroups(username)) {
      personalContacts.addOption(new SelectOption(pcg.getName(), pcg.getId())) ;
    }
    options.add(personalContacts);
      
    SelectOptionGroup sharedContacts = new SelectOptionGroup("shared-contacts");
    for(SharedAddressBook scg : contactSrv.getSharedAddressBooks(username)) {
      sharedContacts.addOption(new SelectOption(MailUtils.getDisplayAdddressShared(scg.getSharedUserId(), scg.getName()), scg.getId())) ;
    }
    sharedContacts.addOption(new SelectOption(sharedContacts_, sharedContacts_));
    options.add(sharedContacts);
    /*
    SelectItemOptionGroup publicContacts = new SelectItemOptionGroup("public-contacts");
    for(String publicCg : MailUtils.getUserGroups()) {
      publicContacts.addOption(new org.exoplatform.mail.webui.SelectItemOption<String>(publicCg, publicCg)) ;
    }
    options.add(publicContacts);
    */
    return options ;
  }
  
  public Contact getSelectedContact() { return this.selectedContact; }
  public void setSelectedContact(Contact contact) { this.selectedContact = contact; }
  
  public HashMap<String, String> getCheckedContactMap() {
    return checkedContactMap;
  }

  public void flipFlopCheckedContactMap(String contactId) {
    for(Entry<String, String> entry: checkedContactMap.entrySet()){
      if(entry.getKey().equals(contactId)){
        if(entry.getValue().equals("0"))
          checkedContactMap.put(entry.getKey(), "1");
        else
          checkedContactMap.put(entry.getKey(), "0");
        break;
      }
    }
  }

  public boolean isCheckedContact(String contactId){
    for(Entry<String, String> entry: checkedContactMap.entrySet()){
      if(entry.getKey().equals(contactId)){
        if(entry.getValue().equals("0"))
          return false;
        else
          return true;
      }
    }
    return false;
  }
  
  public DownloadService getDownloadService() { 
    return getApplicationComponent(DownloadService.class) ; 
  }
  
  public List<Contact> getContacts() throws Exception { return contactList_ ;}
  
  public void refrestContactList(String groupId) throws Exception {
    String username = MailUtils.getCurrentUser();
    ContactService contactSrv = getApplicationComponent(ContactService.class);
    List<Contact> contactList = new ArrayList<Contact>();
    ContactFilter ctFilter = new ContactFilter() ;
    ctFilter.setOrderBy("fullName");
    ctFilter.setAscending(true);
 
    if (groupId != null && groupId.trim().length() > 0 ) {
      SelectOptionGroup privateGroups = (SelectOptionGroup)getChild(UIFormSelectBoxWithGroups.class).getOptions().get(0) ;
      for (SelectOption option : privateGroups.getOptions())
        if (option.getValue().equals(groupId)) {
          ctFilter.setType("0") ;
          break ;
        }
      if (ctFilter.getType() == null) ctFilter.setType("1") ;
      
      
      if (!groupId.equals(sharedContacts_)) ctFilter.setCategories(new String[] {groupId});
      else ctFilter.setSearchSharedContacts(true) ;
      contactList = contactSrv.searchContact(username, ctFilter).getAll();
    } else {
      //ctFilter.setCategories(new String[] {contactSrv.getGroups(username).get(0).getId()});
      ctFilter.setType("0") ;
      ctFilter.setCategories(new String[] { ((SelectOptionGroup)getChild(UIFormSelectBoxWithGroups.class).getOptions().get(0)).getOptions().get(0).getValue() });
      contactList = contactSrv.searchContact(username, ctFilter).getAll();
    }
    contactMap_.clear();
    checkedContactMap.clear();
    for (Contact ct : contactList) {
      contactMap_.put(ct.getId(), ct);
      checkedContactMap.put(ct.getId(),"0");
    }
    contactList_ = new ArrayList<Contact>(contactMap_.values());
    if (contactList_.size() > 0) {
      selectedContact = contactList_.get(0);
      checkedContactMap.put(selectedContact.getId(),"1");
    }
    else selectedContact = null;
  }
  
  public void updateGroup(String selectedGroup) throws Exception {
    ((UIFormSelectBoxWithGroups)getChildById(SELECT_GROUP)).setOptions(getOptions());
    ((UIFormSelectBoxWithGroups)getChildById(SELECT_GROUP)).setValue(selectedGroup);
  }
  
  public String[] getActions() { return new String[] { "Close" } ; }
  
  public void activate() throws Exception { }

  public void deActivate() throws Exception { }
  
  static  public class AddNewGroupActionListener extends EventListener<UIAddressBookForm> {
    public void execute(Event<UIAddressBookForm> event) throws Exception {
      UIAddressBookForm uiAddressBookForm = event.getSource() ;
      UIPopupActionContainer popupContainer = uiAddressBookForm.getParent() ;
      UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
      popupAction.activate(UIAddGroupForm.class, 600) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  static public class AddContactActionListener extends EventListener<UIAddressBookForm> {
    public void execute(Event<UIAddressBookForm> event) throws Exception {
      UIAddressBookForm uiAddressBookForm = event.getSource() ;
      UIPopupActionContainer uiActionContainer = uiAddressBookForm.getParent() ;
      UIPopupAction uiChildPopup = uiActionContainer.getChild(UIPopupAction.class) ;
      UIPopupActionContainer uiPopupContainer = uiChildPopup.activate(UIPopupActionContainer.class, 730) ;
      uiPopupContainer.setId("UIPopupAddContactForm") ;
      UIAddContactForm uiContactForm = uiPopupContainer.addChild(UIAddContactForm.class, null, null) ;
//    cs-2082
      String groupId = ((UIFormSelectBoxWithGroups)uiAddressBookForm.getChildById(SELECT_GROUP)).getValue();
      UIFormSelectBoxWithGroups e = ((UIFormSelectBoxWithGroups)uiContactForm.getChildById(SELECT_GROUP)) ;
      e.setSelectedValues(new String[] {groupId});
      event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;
    }
  }
  
  static public class EditContactActionListener extends EventListener<UIAddressBookForm> {
    public void execute(Event<UIAddressBookForm> event) throws Exception {
      UIAddressBookForm uiAddBook = event.getSource() ;
      Contact selectedContact = uiAddBook.getSelectedContact() ;
      UIApplication uiApp = uiAddBook.getAncestorOfType(UIApplication.class) ;
      String groupId = ((UIFormSelectBoxWithGroups)uiAddBook.getChildById(SELECT_GROUP)).getValue();
      if (selectedContact != null) {
        if (selectedContact.getContactType().equals("1") && !uiAddBook.havePermission(selectedContact)) {
          uiApp.addMessage(new ApplicationMessage("UIAddressBookForm.msg.cannot-edit", null)) ;;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
        /*
        if (selectedContact.getContactType().equals("2") ||(selectedContact.getContactType().equals("1"))) {
          uiApp.addMessage(new ApplicationMessage("UIAddressBookForm.msg.cannot-edit", null)) ;;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }*/
        UIPopupActionContainer uiActionContainer = uiAddBook.getParent() ;
        UIPopupAction uiChildPopup = uiActionContainer.getChild(UIPopupAction.class) ;
        UIPopupActionContainer uiPopupContainer = uiChildPopup.activate(UIPopupActionContainer.class, 730) ;
        uiPopupContainer.setId("UIPopupAddContactForm") ;
        UIAddContactForm uiAddContact = uiPopupContainer.createUIComponent(UIAddContactForm.class, null, null) ;
        uiPopupContainer.addChild(uiAddContact) ;
        if (selectedContact.getContactType().equals("1") && uiAddBook.getChild(UIFormSelectBoxWithGroups.class).getValue().equals(uiAddBook.sharedContacts_)){
          uiAddContact.fillDatas(selectedContact, groupId) ;
          ((UIFormSelectBoxWithGroups)uiAddContact.getChildById(SELECT_GROUP)).getOptions().clear() ;
          List<SelectItem> options = new ArrayList<SelectItem>() ;
          SelectOptionGroup personalContacts = new SelectOptionGroup("personal-contacts");
          personalContacts.addOption(new SelectOption(uiAddBook.sharedContacts_, uiAddBook.sharedContacts_)) ;
          options.add(personalContacts);   
          ((UIFormSelectBoxWithGroups)uiAddContact.getChildById(SELECT_GROUP)).setOptions(options) ;
        } else {
          uiAddContact.fillDatas(selectedContact, groupId) ;
        }
        event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;
      } else {
        uiApp.addMessage(new ApplicationMessage("UIAddressBookForm.msg.no-selected-contact-to-edit", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
    }
  }
  
  static public class SelectContactActionListener extends EventListener<UIAddressBookForm> {
    public void execute(Event<UIAddressBookForm> event) throws Exception {
      UIAddressBookForm uiAddressBook = event.getSource() ;
      String contactId = event.getRequestContext().getRequestParameter(OBJECTID);
      uiAddressBook.setSelectedContact(uiAddressBook.contactMap_.get(contactId));      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressBook.getParent()) ;
    }
  }
  
  static public class CheckContactActionListener extends EventListener<UIAddressBookForm> {
    public void execute(Event<UIAddressBookForm> event) throws Exception {
      UIAddressBookForm uiAddressBook = event.getSource() ;
      String contactId = event.getRequestContext().getRequestParameter(OBJECTID);
      uiAddressBook.flipFlopCheckedContactMap(contactId);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressBook.getParent()) ;
    }
  }
  
  static public class DeleteContactActionListener extends EventListener<UIAddressBookForm> {
    public void execute(Event<UIAddressBookForm> event) throws Exception {
      UIAddressBookForm uiAddressBook = event.getSource() ;
      Contact contact = uiAddressBook.getSelectedContact();
      UIApplication uiApp = uiAddressBook.getAncestorOfType(UIApplication.class) ;
      if (contact != null) {
        String username = MailUtils.getCurrentUser();
        ContactService contactServ = uiAddressBook.getApplicationComponent(ContactService.class);
        try {
          if (contact.isOwner()) {
            uiApp.addMessage(new ApplicationMessage("UIAddressBookForm.msg.cannot-delete-ownerContact", null)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return ;
          } /*else if (contact.getContactType().equals("2") ||(contact.getContactType().equals("1"))) {
            uiApp.addMessage(new ApplicationMessage("UIAddressBookForm.msg.cannot-delete", null)) ;;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return ;
          }*/
          
          SelectOptionGroup privateGroups = (SelectOptionGroup)uiAddressBook.getChild(UIFormSelectBoxWithGroups.class).getOptions().get(0) ;
          boolean isPrivate = false ;
          for (SelectOption option : privateGroups.getOptions())
            if (option.getValue().equals(contact.getAddressBookIds()[0])) {
              isPrivate = true ;
              List<String> contactIds = new ArrayList<String>();
              if (!contact.getId().equals(MailUtils.getCurrentUser())) {
                contactIds.add(contact.getId()) ;
                contactServ.removeContacts(username, contactIds); // do we require a system provider here ?
                
              } else {
                uiApp.addMessage(new ApplicationMessage("UIAddressBookForm.msg.cannot-delete-this-contact", null)) ;
                event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
                return ;
              }
              break ;
            }
          if (!isPrivate) {
            if (((UIFormSelectBoxWithGroups)uiAddressBook.getChildById(SELECT_GROUP)).getValue().equals(uiAddressBook.sharedContacts_)) {
              /*if (!uiAddressBook.havePermission(contact)) {
                uiApp.addMessage(new ApplicationMessage("UIAddressBookForm.msg.cannot-edit", null)) ;
                event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
                return ;
              }*/
              try {
                contactServ.removeUserShareContact(contact.getPath(), contact.getId(), username) ;
              } catch (PathNotFoundException e) {}
            } else {
              uiApp.addMessage(new ApplicationMessage("UIAddressBookForm.msg.cannot-delete", null)) ;
              event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
              return ;              
            }            
          }
          uiAddressBook.refrestContactList(((UIFormSelectBoxWithGroups)uiAddressBook.getChildById(SELECT_GROUP)).getValue());
          event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressBook.getParent()) ;
        } catch(Exception e) {
          e.printStackTrace();
        } 
      } else {
        uiApp.addMessage(new ApplicationMessage("UIAddressBookForm.msg.no-selected-contact-to-delete", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
    }
  }
  
  static public class ChangeGroupActionListener extends EventListener<UIAddressBookForm> {
    public void execute(Event<UIAddressBookForm> event) throws Exception {
      UIAddressBookForm uiAddressBook = event.getSource();
      String selectedGroupId = ((UIFormSelectBoxWithGroups)uiAddressBook.getChildById(SELECT_GROUP)).getValue();
      uiAddressBook.refrestContactList(selectedGroupId);
      ((UIFormSelectBoxWithGroups)uiAddressBook.getChildById(SELECT_GROUP)).setValue(selectedGroupId);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressBook.getParent()) ;
    }
  }
  
  static public class CloseActionListener extends EventListener<UIAddressBookForm> {
    public void execute(Event<UIAddressBookForm> event) throws Exception {
      UIAddressBookForm uiAddressBookForm = event.getSource();
      uiAddressBookForm.getAncestorOfType(UIMailPortlet.class).cancelAction();
    }
  }
  
  static public class SendEmailActionListener extends EventListener<UIAddressBookForm> {
    public void execute(Event<UIAddressBookForm> event) throws Exception {
      UIAddressBookForm uiForm = event.getSource() ;
      String email = event.getRequestContext().getRequestParameter(OBJECTID);
      if (!MailUtils.isFieldEmpty(email)) {
        UIMailPortlet uiPortlet = uiForm.getAncestorOfType(UIMailPortlet.class) ;
        String accId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue() ;      
        if(Utils.isEmptyField(accId)) {
          UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
          uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.account-list-empty", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
        /*
        UIPopupActionContainer uiPopupContainer = uiForm.getAncestorOfType(UIPopupActionContainer.class) ;
        UIPopupAction popupAction = uiPopupContainer.getChild(UIPopupAction.class) ;
        //uiPopupContainer.setId("UIPopupActionComposeContainer") ;
       
        
        UIComposeForm uiComposeForm = popupAction.activate(UIComposeForm.class, MailUtils.MAX_POPUP_WIDTH);
        uiComposeForm.init(accId, null, 0) ;
        uiComposeForm.setFieldToValue(email) ;
        
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;*/
/*        
        UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
        UIPopupActionContainer uiPopupContainer = uiPopupAction.createUIComponent(UIPopupActionContainer.class, null, "UIPopupActionComposeContainer") ;
        uiPopupAction.activate(uiPopupContainer, MailUtils.MAX_POPUP_WIDTH, 0, true);
        
        UIComposeForm uiComposeForm = uiPopupContainer.createUIComponent(UIComposeForm.class, null, null);
        uiComposeForm.init(accId, null, 0);
        uiPopupContainer.addChild(uiComposeForm) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;*/
        
        UIPopupActionContainer uiActionContainer = uiForm.getParent() ;
        UIPopupAction uiChildPopup = uiActionContainer.getChild(UIPopupAction.class) ;
        UIPopupActionContainer uiPopupContainer = uiChildPopup.activate(UIPopupActionContainer.class, MailUtils.MAX_POPUP_WIDTH) ;
        uiPopupContainer.setId("UIPopupActionComposeContainer") ;
        UIComposeForm uiComposeForm = uiPopupContainer.addChild(UIComposeForm.class, null, null) ;
        uiComposeForm.init(accId, null, 0);
        uiComposeForm.setFieldToValue(email) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;
        
      }
      
    }
  }
  static public class SendMultiEmailActionListener extends EventListener<UIAddressBookForm> {
    public void execute(Event<UIAddressBookForm> event) throws Exception {
      UIAddressBookForm uiForm = event.getSource() ;
      String emails =  new String("");
      for (Contact contact : uiForm.getContacts()) {
        if(uiForm.isCheckedContact(contact.getId())){        
          String emailAddress = contact.getEmailAddress();
          if(emailAddress!= null && emailAddress.length()>0){
            if(emails.length() > 0) emails += ", ";
            emails += emailAddress.trim();
          } 
        }  
      }
      if (!MailUtils.isFieldEmpty(emails)) {
        UIMailPortlet uiPortlet = uiForm.getAncestorOfType(UIMailPortlet.class) ;
        String accId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue() ;      
        if(Utils.isEmptyField(accId)) {
          UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
          uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.account-list-empty", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
      UIPopupActionContainer uiActionContainer = uiForm.getParent() ;
      UIPopupAction uiChildPopup = uiActionContainer.getChild(UIPopupAction.class) ;
      UIPopupActionContainer uiPopupContainer = uiChildPopup.activate(UIPopupActionContainer.class, MailUtils.MAX_POPUP_WIDTH) ;
      uiPopupContainer.setId("UIPopupActionComposeContainer") ;
      UIComposeForm uiComposeForm = uiPopupContainer.addChild(UIComposeForm.class, null, null) ;
      uiComposeForm.init(accId, null, 0);
      uiComposeForm.setFieldToValue(emails) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;
      }
      else {
        UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIAddressBookForm.msg.no-selected-contact-to-send-mail",null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIPopupAction.class)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      }
    }
  }
}
