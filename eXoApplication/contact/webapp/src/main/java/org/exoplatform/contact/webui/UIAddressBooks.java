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
package org.exoplatform.contact.webui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.jcr.PathNotFoundException;
import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactFilter;
import org.exoplatform.contact.service.AddressBook;
import org.exoplatform.contact.service.ContactPageList;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.SharedAddressBook;
import org.exoplatform.contact.service.Utils;
import org.exoplatform.contact.service.impl.JCRDataStorage;
import org.exoplatform.contact.service.impl.NewUserListener;
import org.exoplatform.contact.webui.popup.UIAddEditPermission;
import org.exoplatform.contact.webui.popup.UICategoryForm;
import org.exoplatform.contact.webui.popup.UICategorySelect;
import org.exoplatform.contact.webui.popup.UIComposeForm;
import org.exoplatform.contact.webui.popup.UIContactForm;
import org.exoplatform.contact.webui.popup.UIExportAddressBookForm;
import org.exoplatform.contact.webui.popup.UIExportForm;
import org.exoplatform.contact.webui.popup.UIImportForm;
import org.exoplatform.contact.webui.popup.UIPopupAction;
import org.exoplatform.contact.webui.popup.UIPopupContainer;
import org.exoplatform.contact.webui.popup.UIExportForm.ContactData;
import org.exoplatform.mail.service.Account;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL Author : Hung Nguyen
 * hung.nguyen@exoplatform.com Aus 01, 2007 2:48:18 PM
 */

@ComponentConfig(template = "app:/templates/contact/webui/UIAddressBooks.gtmpl", events = {
    @EventConfig(listeners = UIAddressBooks.AddContactActionListener.class),
    @EventConfig(listeners = UIAddressBooks.CopyAddressActionListener.class),
    @EventConfig(listeners = UIAddressBooks.PasteContactsActionListener.class),
    @EventConfig(listeners = UIAddressBooks.AddAddressActionListener.class),
    @EventConfig(listeners = UIAddressBooks.ImportAddressActionListener.class), 
    @EventConfig(listeners = UIAddressBooks.ExportAddressActionListener.class),
    @EventConfig(listeners = UIAddressBooks.EditGroupActionListener.class),
    @EventConfig(listeners = UIAddressBooks.ShareGroupActionListener.class),
    @EventConfig(listeners = UIAddressBooks.DeleteGroupActionListener.class
        , confirm = "UIAddressBooks.msg.confirm-delete"),
    @EventConfig(listeners = UIAddressBooks.DeleteSharedGroupActionListener.class
        , confirm = "UIAddressBooks.msg.confirm-discard"),
    @EventConfig(listeners = UIAddressBooks.SelectGroupActionListener.class),
    @EventConfig(listeners = UIAddressBooks.SelectPublicGroupActionListener.class),
    @EventConfig(listeners = UIAddressBooks.SelectSharedContactActionListener.class),
    @EventConfig(listeners = UIAddressBooks.SelectSharedGroupActionListener.class),
    @EventConfig(listeners = UIAddressBooks.PrintActionListener.class),
    @EventConfig(listeners = UIAddressBooks.SendEmailActionListener.class) }
)
    
public class UIAddressBooks extends UIComponent {
  private String selectedGroup = null;
  private Map<String, String> privateAddressBookMap_ = new LinkedHashMap<String, String>() ;
  private Map<String, SharedAddressBook> sharedAddressBookMap_ = new LinkedHashMap<String, SharedAddressBook>() ;
  private Map<String, String> copyContacts = new LinkedHashMap<String, String>() ;
  private String copyAddress = null ;
  public UIAddressBooks() throws Exception { }
  
  @SuppressWarnings("unused")
  private boolean hasSharedContacts() throws Exception {
    if (ContactUtils.getContactService().getSharedContacts( ContactUtils.getCurrentUser()).getAvailable() > 0) return true ;
    return false ;
  }
  
  public List<AddressBook> getGroups() throws Exception {
    List<AddressBook> groupList = ContactUtils.getContactService()
      .getGroups(ContactUtils.getCurrentUser());
    privateAddressBookMap_.clear() ;
    for (AddressBook group : groupList) {
      
      // task 825
      String groupName = group.getName() ;
      if (group.getId().equals(NewUserListener.DEFAULTGROUP + ContactUtils.getCurrentUser()) &&  groupName.equals(NewUserListener.DEFAULTGROUPNAME)
          && group.getDescription().equals(NewUserListener.DEFAULTGROUPDES)) {
        WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
        ResourceBundle res = context.getApplicationResourceBundle() ;
        try {
          groupName = res.getString("UIAddressBooks.label.defaultAddName");
          String des = res.getString("UIAddressBooks.label.defaultAddDes");
          group.setName(groupName) ;
          group.setDescription(des) ;          
          ContactUtils.getContactService().saveAddressBook(
              ContactUtils.getCurrentUser(), group, false) ;
        } catch (MissingResourceException e) {      
          e.printStackTrace() ;
        }
      } else if (group.getId().equals(NewUserListener.ADDRESSESGROUP + ContactUtils.getCurrentUser()) &&  groupName.equals(NewUserListener.ADDRESSESGROUPNAME)) {
        WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
        ResourceBundle res = context.getApplicationResourceBundle() ;
        try {
          groupName = res.getString("UIAddressBooks.label.collectedAddresses");
          group.setName(groupName) ;         
          ContactUtils.getContactService().saveAddressBook(
              ContactUtils.getCurrentUser(), group, false) ;
        } catch (MissingResourceException e) {      
          e.printStackTrace() ;
        }
      }
      privateAddressBookMap_.put(group.getId(), groupName) ; 
    }
    return groupList;
  }
  
  public String[] getPublicContactGroups() throws Exception {
    return ContactUtils.getUserGroups().toArray(new String[] {}) ;
  }
  public boolean isSelectSharedContacts() {
    return getAncestorOfType(UIWorkingContainer.class).findFirstComponentOfType(UIContacts.class).isSelectSharedContacts() ;    
  }
  
  public Map<String, SharedAddressBook> getSharedGroups() throws Exception { 
    sharedAddressBookMap_.clear() ;
    List<SharedAddressBook> addressList = ContactUtils.getContactService()
      .getSharedAddressBooks(ContactUtils.getCurrentUser()) ;
    for (SharedAddressBook address : addressList) {
      sharedAddressBookMap_.put(address.getId(), address) ;  
    }
    return sharedAddressBookMap_ ;  
  } 

  public boolean havePermission(String groupId) throws Exception { 
    String currentUser = ContactUtils.getCurrentUser() ;
    AddressBook sharedGroup = ContactUtils.getContactService().getSharedAddressBook(currentUser, groupId) ;
    if (sharedGroup == null) return false ;
    if (sharedGroup.getEditPermissionUsers() != null &&
        Arrays.asList(sharedGroup.getEditPermissionUsers()).contains(currentUser + JCRDataStorage.HYPHEN)) {
      return true ;
    }
    String[] editPerGroups = sharedGroup.getEditPermissionGroups() ;
    if (editPerGroups != null)
      for (String editPer : editPerGroups)
        if (ContactUtils.getUserGroups().contains(editPer)) return true ;
    return false ;
  }
  
  public void setSelectedGroup(String groupId) { selectedGroup = groupId ; }
  public String getSelectedGroup() { return selectedGroup ; }
  public Map<String, String> getPrivateGroupMap() { return privateAddressBookMap_ ;}
  
  public boolean canPaste() {
    if (!ContactUtils.isEmpty(copyAddress) || copyContacts.size() > 0 ) return true ;
    return false ;
  }
  public void setCopyAddress(String add) { copyAddress = add ; }
  public boolean isDefault(String groupId) throws Exception {
    if (groupId.contains(NewUserListener.DEFAULTGROUP) || groupId.contains(NewUserListener.ADDRESSESGROUP)) {
      return true ;
    }
    return false;
  }
  
  public void setCopyContacts(Map<String, String> contacts) { copyContacts = contacts ; }
  public Map<String, String> getCopyContacts() { return copyContacts ; }
  
  static public class AddAddressActionListener extends EventListener<UIAddressBooks> {
    public void execute(Event<UIAddressBooks> event) throws Exception {
      UIAddressBooks uiAddressBook = event.getSource();
      UIContactPortlet uiContactPortlet = uiAddressBook.getAncestorOfType(UIContactPortlet.class);
      UIPopupAction uiPopupAction = uiContactPortlet.getChild(UIPopupAction.class);
      uiPopupAction.activate(UICategoryForm.class, 500) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressBook.getParent());
    }
  }
  
  static public class CopyAddressActionListener extends EventListener<UIAddressBooks> {
    public void execute(Event<UIAddressBooks> event) throws Exception {
      UIAddressBooks uiAddressBook = event.getSource();
      String addressBookId = event.getRequestContext().getRequestParameter(OBJECTID);
      uiAddressBook.copyAddress = addressBookId ;
      uiAddressBook.copyContacts.clear() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressBook.getParent()) ;
    }
  }
  
  static public class PasteContactsActionListener extends EventListener<UIAddressBooks> {
    public void execute(Event<UIAddressBooks> event) throws Exception {
      UIAddressBooks uiAddressBook = event.getSource();
      String destAddress = event.getRequestContext().getRequestParameter(OBJECTID);
      String username = ContactUtils.getCurrentUser() ;
      String destType ;
      if (uiAddressBook.privateAddressBookMap_.containsKey(destAddress))
        destType = JCRDataStorage.PERSONAL ;
      else {
        destType = JCRDataStorage.SHARED ;     
      }
      if (destType.equals(JCRDataStorage.SHARED) && (!uiAddressBook.havePermission(destAddress))) {
        UIApplication uiApp = uiAddressBook.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIAddressBooks.msg.removedPer", null,
          ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ; 
      }
      
      String srcAddress = uiAddressBook.copyAddress ;
      if (!ContactUtils.isEmpty(srcAddress)) {
        if (destAddress.equals(srcAddress)){
          UIApplication uiApp = uiAddressBook.getAncestorOfType(UIApplication.class) ;
          uiApp.addMessage(new ApplicationMessage("UIAddressBooks.msg.invalidAddress", null,
            ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
        String srcType ;
        if (uiAddressBook.privateAddressBookMap_.containsKey(srcAddress)) srcType = JCRDataStorage.PERSONAL ;
        else if (uiAddressBook.sharedAddressBookMap_.containsKey(srcAddress)) srcType = JCRDataStorage.SHARED ;
        else srcType = JCRDataStorage.PUBLIC ;
        ContactUtils.getContactService().pasteAddressBook(username
            , srcAddress, srcType, destAddress, destType) ;
      } else {
        ContactUtils.getContactService().pasteContacts(username
            , destAddress, destType, uiAddressBook.getCopyContacts()) ;
      }
      // bi update neu la shared contacts 
      UIContacts uiContacts = uiAddressBook
      .getAncestorOfType(UIWorkingContainer.class).findFirstComponentOfType(UIContacts.class) ;
      if (!uiContacts.isDisplaySearchResult() && uiAddressBook.selectedGroup != null) {
        uiContacts.updateList() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent()) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressBook.getParent()) ;
    }
  }
  
  
  static public class ExportAddressActionListener extends EventListener<UIAddressBooks> {
    public void execute(Event<UIAddressBooks> event) throws Exception {
      UIAddressBooks uiAddressBook = event.getSource();
      UIContactPortlet uiContactPortlet = uiAddressBook.getAncestorOfType(UIContactPortlet.class);
      UIPopupAction uiPopupAction = uiContactPortlet.getChild(UIPopupAction.class); 
      String addressBookId = event.getRequestContext().getRequestParameter(OBJECTID);
      if (addressBookId != null) {
        ContactFilter filter = new ContactFilter();
        filter.setAscending(true);
        filter.setCategories(new String[] { addressBookId });
        ContactPageList contacts = null ;
        UIExportForm uiExportForm = uiPopupAction.createUIComponent(UIExportForm.class, null, null) ;
        uiExportForm.setId("ExportForm");
        ContactService contactService = ContactUtils.getContactService() ;
        String username = ContactUtils.getCurrentUser() ;
        Map<String, String> privateGroup = uiAddressBook.privateAddressBookMap_ ;
        if (privateGroup.containsKey(addressBookId)) {
          uiExportForm.setSelectedGroup(JCRDataStorage.PERSONAL + Utils.SPLIT +
              addressBookId + Utils.SPLIT + privateGroup.get(addressBookId)) ;
          contacts = contactService.getPersonalContactsByAddressBook(username, addressBookId) ;
        } else if (ContactUtils.getUserGroups().contains(addressBookId)){        
          uiExportForm.setSelectedGroup(JCRDataStorage.PUBLIC + Utils.SPLIT +
              addressBookId + Utils.SPLIT + addressBookId) ;
          contacts = contactService.getPublicContactsByAddressBook(addressBookId);
        } else {
          SharedAddressBook address = uiAddressBook.sharedAddressBookMap_.get(addressBookId) ;
          uiExportForm.setSelectedGroup(JCRDataStorage.SHARED + Utils.SPLIT + 
              addressBookId + Utils.SPLIT + ContactUtils.getDisplayAdddressShared(address.getSharedUserId(), address.getName())) ;
          contacts = contactService.getSharedContactsByAddressBook(
              username, address) ;
        }
        if (contacts == null || contacts.getAvailable() == 0) {
          UIApplication uiApp = uiAddressBook.getAncestorOfType(UIApplication.class) ;
          uiApp.addMessage(new ApplicationMessage("UIAddressBooks.msg.noContactToExport", null,
            ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;  
        }
        Map<String, String> resultMap = contacts.getEmails() ;
        List<ContactData> data = new ArrayList<ContactData>() ;
        for(String ct : resultMap.keySet()) {
          String id  = ct ;
          String value = resultMap.get(id) ; 
          if(resultMap.get(id) != null && resultMap.get(id).trim().length() > 0) {
            if(value.lastIndexOf(Utils.SPLIT) > 0) {
              String fullName = value.substring(0,value.lastIndexOf(Utils.SPLIT)) ;
              String email = value.substring(value.lastIndexOf(Utils.SPLIT) + Utils.SPLIT.length()) ;
              ContactData contactData = uiExportForm.new ContactData(id, fullName, email) ;
              data.add(contactData) ;
            }
          }
        }
        uiExportForm.setContactList(data);
        uiPopupAction.activate(uiExportForm, 600, 0) ;
      } else {
        
        // There is no specific address book so display the address books list        
        Map<String, String> groups = uiAddressBook.privateAddressBookMap_ ;
        Map<String, SharedAddressBook> sharedGroups = uiAddressBook.sharedAddressBookMap_ ;
        Map<String, String> publicGroups = new HashMap<String, String>() ;
        for (String group : ContactUtils.getUserGroups()) publicGroups.put(group, group) ;
        if ((groups == null || groups.size() == 0) && (sharedGroups == null || sharedGroups.size() == 0)
            && (publicGroups == null || publicGroups.size() == 0)) {
          UIApplication uiApp = uiAddressBook.getAncestorOfType(UIApplication.class) ;
          uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.no-addressbook", null,
            ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;   
        }
        UIExportAddressBookForm uiExportForm = uiPopupAction.activate(UIExportAddressBookForm.class, 500) ;
        uiExportForm.setId("UIExportAddressBookForm");
        uiExportForm.setContactGroups(groups) ;
        uiExportForm.setPublicContactGroup(publicGroups) ;
        uiExportForm.setSharedContactGroups(sharedGroups) ;
        uiExportForm.updateList();
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
      //event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressBook.getParent());
    }
  }
  
  static public class ImportAddressActionListener extends EventListener<UIAddressBooks> {
    public void execute(Event<UIAddressBooks> event) throws Exception {
      UIAddressBooks uiAddressBook = event.getSource();
      UIContactPortlet uiContactPortlet = uiAddressBook.getAncestorOfType(UIContactPortlet.class);
      UIPopupAction uiPopupAction = uiContactPortlet.getChild(UIPopupAction.class);
      String addressBookId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIPopupContainer uiPopupContainer = uiPopupAction.activate(UIPopupContainer.class, 600) ;
      if (!ContactUtils.isEmpty(addressBookId)){
        uiPopupContainer.setId("ImportContacts") ;
      } else {
        uiPopupContainer.setId("ImportAddress") ;
      }
      UIImportForm uiImportForm = uiPopupContainer.addChild(UIImportForm.class, null, null) ;
      //cs-2188
      Map<String, String> addresses = new LinkedHashMap<String, String>() ;
      addresses.putAll(uiAddressBook.privateAddressBookMap_) ;
      for (SharedAddressBook address : uiAddressBook.sharedAddressBookMap_.values())
        if (uiAddressBook.havePermission(address.getId())) {
          addresses.put(address.getId(), ContactUtils
              .getDisplayAdddressShared(address.getSharedUserId(), address.getName())) ;
        } else if (!ContactUtils.isEmpty(addressBookId) && addressBookId.equals(address.getId())) {
          UIApplication uiApp = uiAddressBook.getAncestorOfType(UIApplication.class) ;
          uiApp.addMessage(new ApplicationMessage("UIAddressBooks.msg.removedPer", null,
            ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
      uiImportForm.setGroup(addresses) ;
      uiImportForm.addConponent() ;      
      if (!ContactUtils.isEmpty(addressBookId)) uiImportForm.setValues(addressBookId) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
      //event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressBook.getParent());
    }
  }

  static public class AddContactActionListener extends EventListener<UIAddressBooks> {
    public void execute(Event<UIAddressBooks> event) throws Exception {
      UIAddressBooks uiAddressBook = event.getSource() ;  
      String groupId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIContactPortlet uiContactPortlet = uiAddressBook.getAncestorOfType(UIContactPortlet.class) ;
      UIPopupAction uiPopupAction = uiContactPortlet.getChild(UIPopupAction.class) ;
      
      Map<String, String> addresses = new LinkedHashMap<String, String>() ; 
      addresses.putAll(uiAddressBook.privateAddressBookMap_) ;
      for (SharedAddressBook address : uiAddressBook.sharedAddressBookMap_.values())
        if (uiAddressBook.havePermission(address.getId())) {
          addresses.put(address.getId(), ContactUtils
              .getDisplayAdddressShared(address.getSharedUserId(), address.getName())) ;
        } else if (!ContactUtils.isEmpty(groupId) && groupId.equals(address.getId())) {
          UIApplication uiApp = uiAddressBook.getAncestorOfType(UIApplication.class) ;
          uiApp.addMessage(new ApplicationMessage("UIAddressBooks.msg.removedPer", null,
            ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }      
      UIPopupContainer popupContainer = uiPopupAction.activate(UIPopupContainer.class, 800) ;
      popupContainer.setId("AddNewContact") ;
      UICategorySelect uiCategorySelect = popupContainer.addChild(UICategorySelect.class, null, null) ;
      UIContactForm uiContactForm = popupContainer.addChild(UIContactForm.class, null, null) ;
      uiContactForm.setNew(true) ;
      uiCategorySelect.setPrivateGroupMap(addresses) ;    
      uiCategorySelect.setValue(groupId) ;
      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressBook.getParent());
    }
  }

  static public class EditGroupActionListener extends EventListener<UIAddressBooks> {
    public void execute(Event<UIAddressBooks> event) throws Exception {
      String groupId = event.getRequestContext().getRequestParameter(OBJECTID);      
      UIAddressBooks uiAddressBook = event.getSource();
      UIContactPortlet uiContactPortlet = uiAddressBook.getAncestorOfType(UIContactPortlet.class);

      UIPopupAction popupAction = uiContactPortlet.getChild(UIPopupAction.class);
      UICategoryForm uiCategoryForm = popupAction.activate(UICategoryForm.class, 500) ;
      if (uiAddressBook.privateAddressBookMap_.containsKey(groupId)) {
        uiCategoryForm.setValues(groupId, false) ; 
      } else if (uiAddressBook.havePermission(groupId)){
        uiCategoryForm.setValues(groupId, true) ;
      } else {
        UIApplication uiApp = uiAddressBook.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIAddressBooks.msg.removedPer", null,
          ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      uiCategoryForm.setNew(false) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
     //event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressBook.getParent());
    }
  }
  
  static public class ShareGroupActionListener extends EventListener<UIAddressBooks> {
    public void execute(Event<UIAddressBooks> event) throws Exception {
      UIAddressBooks uiAddressBook = event.getSource();
      String groupId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIContactPortlet contactPortlet = uiAddressBook.getAncestorOfType(UIContactPortlet.class) ;
      UIPopupAction popupAction = contactPortlet.getChild(UIPopupAction.class) ;
      UIPopupContainer uiPopupContainer = popupAction.activate(UIPopupContainer.class, 400) ;
      uiPopupContainer.setId("UIPermissionGroupForm") ;
      UIAddEditPermission uiAddNewEditPermission = uiPopupContainer.addChild(UIAddEditPermission.class, null, null);
      ContactService contactService = ContactUtils.getContactService();
      String username = ContactUtils.getCurrentUser(); 
      uiAddNewEditPermission.initGroup(contactService.getPersonalAddressBook(username, groupId)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      //event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressBook.getParent());
    }
  }

  public static class SendEmailActionListener extends EventListener<UIAddressBooks> {
    public void execute(Event<UIAddressBooks> event) throws Exception {
      UIAddressBooks uiAddressBook = event.getSource();
      UIContactPortlet uiContactPortlet = uiAddressBook.getAncestorOfType(UIContactPortlet.class);
      UIPopupAction uiPopupAction = uiContactPortlet.getChild(UIPopupAction.class);
      String groupId = event.getRequestContext().getRequestParameter(OBJECTID);
      
      String username = ContactUtils.getCurrentUser();
      ContactService contactService = ContactUtils.getContactService();
      List<String> addresses = null ;      
      if (uiAddressBook.privateAddressBookMap_.containsKey(groupId)) {
        addresses = contactService
        .getEmailsByAddressBook(username, groupId);
      } else if (ContactUtils.getUserGroups().contains(groupId)) {
        addresses = contactService.getAllEmailByPublicGroup(username, groupId) ;
      } else {
        addresses = contactService.getAllEmailBySharedGroup(username, groupId) ;
      }
      if (addresses == null || addresses.size() == 0) {
        UIApplication uiApp = uiAddressBook.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIAddressBooks.msg.no-email-found", null,
          ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;        
      }
      StringBuffer buffer = new StringBuffer(addresses.get(0)) ;
      for (int i = 1; i < addresses.size(); i ++) {
        buffer.append(", " + addresses.get(i)) ;
      }
      List<Account> acc = ContactUtils.getAccounts() ;
    /*  if (acc == null || acc.size() < 1) {
        UIApplication uiApp = uiAddressBook.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.invalidAcc", null,
            ApplicationMessage.WARNING)) ;
        return ;
      }*/
      UIComposeForm uiComposeForm = uiPopupAction.activate(UIComposeForm.class, 850) ;
      uiComposeForm.init(acc, buffer.toString()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressBook.getParent());
       
    }
  }

  static public class DeleteGroupActionListener extends EventListener<UIAddressBooks> {
    @SuppressWarnings("static-access")
    public void execute(Event<UIAddressBooks> event) throws Exception {
      UIAddressBooks uiAddressBook = event.getSource();
      UIWorkingContainer workingContainer = uiAddressBook.getAncestorOfType(UIWorkingContainer.class);
      workingContainer.getAncestorOfType(UIContactPortlet.class).cancelAction() ;
      UIContacts uiContacts = workingContainer.findFirstComponentOfType(UIContacts.class) ;
      String groupId = event.getRequestContext().getRequestParameter(OBJECTID);
      ContactService contactService = ContactUtils.getContactService();
      String username = ContactUtils.getCurrentUser();
      List <Contact> removedContacts = new ArrayList<Contact>() ;

//      cs-1644
      //if (uiContacts.isDisplaySearchResult())
      removedContacts = contactService.getPersonalContactsByAddressBook(
            username, groupId).getAll() ;
      contactService.removeAddressBook(username, groupId);
   
      if (groupId.equals(uiAddressBook.copyAddress)) uiAddressBook.copyAddress = null ;      
      if (groupId.equals(uiAddressBook.selectedGroup)) {
        uiAddressBook.selectedGroup = null;
        uiContacts.setContacts(null);
      }
      String selectedTag = uiContacts.getSelectedTag() ;
      if (!ContactUtils.isEmpty(selectedTag)) {
        uiContacts.setContacts(
            contactService.getContactPageListByTag(username, selectedTag)) ;
      }
      
      if (uiContacts.isDisplaySearchResult()) {
        //cs-1809 
        uiContacts.setContacts(contactService.searchContact(username
          , workingContainer.findFirstComponentOfType(UISearchForm.class).filter)) ;
      }
      if (uiContacts.getSelectedGroup() != null && groupId.equals(uiContacts.getSelectedGroup()))
        uiContacts.setSelectedGroup(null) ;
      
      // cs-1644
      for (Contact contact : removedContacts)
        uiAddressBook.copyContacts.remove(contact.getId()) ; 
      event.getRequestContext().addUIComponentToUpdateByAjax(workingContainer);     
    }
  }
  
  static public class DeleteSharedGroupActionListener extends EventListener<UIAddressBooks> {
    @SuppressWarnings("static-access")
    public void execute(Event<UIAddressBooks> event) throws Exception {
      UIAddressBooks uiAddressBook = event.getSource();
      UIWorkingContainer workingContainer = uiAddressBook.getAncestorOfType(UIWorkingContainer.class);
      workingContainer.getAncestorOfType(UIContactPortlet.class).cancelAction() ;
      UIContacts uiContacts = workingContainer.findFirstComponentOfType(UIContacts.class) ;
      String groupId = event.getRequestContext().getRequestParameter(OBJECTID);
      ContactService contactService = ContactUtils.getContactService();
      String username = ContactUtils.getCurrentUser();
      List <Contact> removedContacts = new ArrayList<Contact>() ;
      if (uiAddressBook.sharedAddressBookMap_.containsKey(groupId)) {
        //contactService.removeSharedAddressBook(SessionProviderFactory.createSystemProvider(), username, groupId) ;
//      cs-1644
        //if (uiContacts.isDisplaySearchResult())
        removedContacts = contactService.getSharedContactsByAddressBook(username, uiAddressBook.sharedAddressBookMap_.get(groupId)).getAll() ;
        try {
          contactService.unshareAddressBook(uiAddressBook.sharedAddressBookMap_.get(groupId).getSharedUserId()
              , groupId, username) ;          
        } catch (PathNotFoundException e) { }
      }
      if (groupId.equals(uiAddressBook.copyAddress)) uiAddressBook.copyAddress = null ;      
      if (groupId.equals(uiAddressBook.selectedGroup)) {
        uiAddressBook.selectedGroup = null;
        uiContacts.setContacts(null);
      }
      String selectedTag = uiContacts.getSelectedTag() ;
      if (!ContactUtils.isEmpty(selectedTag)) {
        uiContacts.setContacts(
            contactService.getContactPageListByTag(username, selectedTag)) ;
      }
      
      if (uiContacts.isDisplaySearchResult()) {
        //cs-1809 
        uiContacts.setContacts(contactService.searchContact(username
          , workingContainer.findFirstComponentOfType(UISearchForm.class).filter)) ;
      }
      if (uiContacts.getSelectedGroup() != null && groupId.equals(uiContacts.getSelectedGroup()))
        uiContacts.setSelectedGroup(null) ;
      
      // cs-1644
      for (Contact contact : removedContacts)
        uiAddressBook.copyContacts.remove(contact.getId()) ; 
      event.getRequestContext().addUIComponentToUpdateByAjax(workingContainer);     
    }
  }

  static public class SelectGroupActionListener extends EventListener<UIAddressBooks> {
    public void execute(Event<UIAddressBooks> event) throws Exception {
      UIAddressBooks uiAddressBook = event.getSource();
      UIWorkingContainer uiWorkingContainer = uiAddressBook.getAncestorOfType(UIWorkingContainer.class);
      uiWorkingContainer.findFirstComponentOfType(UITags.class).setSelectedTag(null);
      String groupId = event.getRequestContext().getRequestParameter(OBJECTID);
      uiAddressBook.selectedGroup = groupId ;
      UIContacts uiContacts = uiWorkingContainer.findFirstComponentOfType(UIContacts.class);
      uiContacts.setContacts(ContactUtils.getContactService().getPersonalContactsByAddressBook(
          ContactUtils.getCurrentUser(), groupId));
      uiContacts.setSortedBy(UIContacts.fullName) ;
      uiContacts.setSelectedGroup(groupId);
      uiContacts.setSelectedTag(null);
      uiContacts.setDisplaySearchResult(false) ;
      uiContacts.setDefaultNameSorted(true) ;
      uiContacts.setSelectSharedContacts(false) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingContainer);
    }
  }

  static public class SelectPublicGroupActionListener extends EventListener<UIAddressBooks> {
    public void execute(Event<UIAddressBooks> event) throws Exception {
      UIAddressBooks uiAddressBook = event.getSource();
      UIWorkingContainer uiWorkingContainer = uiAddressBook.getAncestorOfType(UIWorkingContainer.class);
      uiWorkingContainer.findFirstComponentOfType(UITags.class).setSelectedTag(null);
      String groupId = event.getRequestContext().getRequestParameter(OBJECTID);
      uiAddressBook.selectedGroup = groupId;
      UIContacts uiContacts = uiWorkingContainer.findFirstComponentOfType(UIContacts.class);
      uiContacts.setContacts(ContactUtils.getContactService()
          .getPublicContactsByAddressBook(groupId));
      uiContacts.setSortedBy(UIContacts.fullName) ;
      uiContacts.setSelectedGroup(groupId);
      uiContacts.setSelectedTag(null);
      uiContacts.setDisplaySearchResult(false) ;
      uiContacts.setDefaultNameSorted(true) ;
      uiContacts.setSelectSharedContacts(false) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingContainer);
    }
  }
  
  static public class SelectSharedContactActionListener extends EventListener<UIAddressBooks> {
    public void execute(Event<UIAddressBooks> event) throws Exception {
      UIAddressBooks uiAddressBook = event.getSource();
      UIWorkingContainer uiWorkingContainer = uiAddressBook.getAncestorOfType(UIWorkingContainer.class);
      uiWorkingContainer.findFirstComponentOfType(UITags.class).setSelectedTag(null);
      uiAddressBook.selectedGroup = null;
      UIContacts uiContacts = uiWorkingContainer.findFirstComponentOfType(UIContacts.class);
      uiContacts.setContacts(ContactUtils.getContactService().getSharedContacts( ContactUtils.getCurrentUser()));
      uiContacts.setSortedBy(UIContacts.fullName) ;
      uiContacts.setSelectedGroup(null);
      uiContacts.setSelectedTag(null);
      uiContacts.setDisplaySearchResult(false) ;
      uiContacts.setDefaultNameSorted(true) ;
      uiContacts.setSelectSharedContacts(true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingContainer);
    }
  }
  
  static public class SelectSharedGroupActionListener extends EventListener<UIAddressBooks> {
    public void execute(Event<UIAddressBooks> event) throws Exception {
      UIAddressBooks uiAddressBook = event.getSource();
      UIWorkingContainer uiWorkingContainer = uiAddressBook.getAncestorOfType(UIWorkingContainer.class);
      uiWorkingContainer.findFirstComponentOfType(UITags.class).setSelectedTag(null);
      String groupId = event.getRequestContext().getRequestParameter(OBJECTID);
      uiAddressBook.selectedGroup = groupId ;
      UIContacts uiContacts = uiWorkingContainer.findFirstComponentOfType(UIContacts.class);
      uiContacts.setContacts(ContactUtils.getContactService().
                                getSharedContactsByAddressBook( ContactUtils.getCurrentUser(),
                                                                uiAddressBook.sharedAddressBookMap_.get(groupId)));
      uiContacts.setSortedBy(UIContacts.fullName) ;
      uiContacts.setSelectedGroup(groupId);
      uiContacts.setSelectedTag(null);
      uiContacts.setDisplaySearchResult(false) ;
      uiContacts.setDefaultNameSorted(true) ;
      uiContacts.setSelectSharedContacts(false) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingContainer);
    }
  }  
  
  static public class PrintActionListener extends EventListener<UIAddressBooks> {
    public void execute(Event<UIAddressBooks> event) throws Exception {
      UIAddressBooks uiAddressBook = event.getSource();
      String groupId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIWorkingContainer workingContainer = uiAddressBook.getAncestorOfType(UIWorkingContainer.class) ;
      UIContacts uiContacts = workingContainer.findFirstComponentOfType(UIContacts.class) ;      
      UIContactPreview uiContactPreview = workingContainer.findFirstComponentOfType(UIContactPreview.class) ;
      uiContactPreview.setRendered(false) ;
      uiContacts.setListBeforePrint(Arrays.asList(uiContacts.getContacts())) ;
      uiContacts.setViewListBeforePrint(uiContacts.getViewContactsList()) ;
      uiContacts.setViewContactsList(false) ;
      uiContacts.setPrintForm(true) ;
      //uiContacts.setSelectedTag(null) ;

      ContactService service = ContactUtils.getContactService() ;
      String username = ContactUtils.getCurrentUser() ;
      ContactPageList pageList = null ;
      if (uiAddressBook.privateAddressBookMap_.containsKey(groupId)) {
        pageList = service.getPersonalContactsByAddressBook(username, groupId) ;
      } else if (uiAddressBook.sharedAddressBookMap_.containsKey(groupId)){
         pageList = service.getSharedContactsByAddressBook(
            username, uiAddressBook.sharedAddressBookMap_.get(groupId)) ;

      } else {
         pageList = service.getPublicContactsByAddressBook(groupId) ;
      }
      LinkedHashMap<String, Contact> contactMap = new LinkedHashMap<String, Contact> () ;
      
      if (pageList == null) return ;
      for (Contact contact : pageList.getAll()) {
        contactMap.put(contact.getId(), contact) ;
      }
      uiContacts.setContactMap(contactMap) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(workingContainer) ;
    }
  }
    
}
