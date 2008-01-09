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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.SessionsUtils;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactFilter;
import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.webui.popup.UICategoryForm;
import org.exoplatform.contact.webui.popup.UICategorySelect;
import org.exoplatform.contact.webui.popup.UIContactForm;
import org.exoplatform.contact.webui.popup.UIExportAddressBookForm;
import org.exoplatform.contact.webui.popup.UIExportForm;
import org.exoplatform.contact.webui.popup.UIImportForm;
import org.exoplatform.contact.webui.popup.UIPopupAction;
import org.exoplatform.contact.webui.popup.UIPopupContainer;
import org.exoplatform.contact.webui.popup.UISendEmail;
import org.exoplatform.contact.webui.popup.UISharedForm;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.web.application.ApplicationMessage;
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
    @EventConfig(listeners = UIAddressBooks.AddAddressActionListener.class),
    @EventConfig(listeners = UIAddressBooks.ImportAddressActionListener.class), 
    @EventConfig(listeners = UIAddressBooks.ExportAddressActionListener.class),
    @EventConfig(listeners = UIAddressBooks.EditGroupActionListener.class),
    @EventConfig(listeners = UIAddressBooks.ShareGroupActionListener.class),
    @EventConfig(listeners = UIAddressBooks.DeleteGroupActionListener.class
        , confirm = "UIAddressBooks.msg.confirm-delete"),
    @EventConfig(listeners = UIAddressBooks.SelectGroupActionListener.class),
    @EventConfig(listeners = UIAddressBooks.SelectPublicGroupActionListener.class),
    @EventConfig(listeners = UIAddressBooks.SelectSharedGroupActionListener.class),
    @EventConfig(listeners = UIAddressBooks.PrintActionListener.class),
    @EventConfig(listeners = UIAddressBooks.SendEmailActionListener.class) }
)
    
public class UIAddressBooks extends UIComponent {
  private String selectedGroup = null;
  private Map<String, String> privateGroupMap_ = new HashMap<String, String>() ;
  private Map<String, String> publicGroupMap_ = new HashMap<String, String>() ;
  private Map<String, String> sharedGroupMap_ = new HashMap<String, String>() ;
  private String defaultGroup ;
  public UIAddressBooks() throws Exception { }

  public List<ContactGroup> getGroups() throws Exception {
    List<ContactGroup> groupList = ContactUtils.getContactService()
      .getGroups(SessionsUtils.getSessionProvider(), ContactUtils.getCurrentUser());
    if (groupList != null && groupList.size() > 0) defaultGroup = groupList.get(0).getId() ;
    privateGroupMap_.clear() ;
    for (ContactGroup group : groupList) privateGroupMap_.put(group.getId(), group.getName()) ; 
    return groupList;
  }
  public List<String> getPublicContactGroups() throws Exception {
    List<String> publicGroup = ContactUtils.getContactService()
      .getPublicAddressBookContacts(SessionsUtils.getSystemProvider(), ContactUtils.getUserGroups());
    publicGroupMap_.clear() ;
    for (String group : publicGroup) publicGroupMap_.put(group, group) ; 
    return publicGroup ;
  }
  public Map<String, String> getSharedGroups() throws Exception { 
    sharedGroupMap_.clear() ;
    List<String> addressList = ContactUtils.getContactService()
      .getSharedAddressBooks(SessionsUtils.getSystemProvider(), ContactUtils.getCurrentUser()) ;
    for (String address : addressList) {
      String[] array = address.split("::") ;
      sharedGroupMap_.put(array[1], array[0]) ;
    }
    return sharedGroupMap_ ;  
  } 

  public void setSelectedGroup(String groupId) { selectedGroup = groupId ; }
  public String getSelectedGroup() { return selectedGroup ; }

  public Map<String, String> getPrivateGroupMap() { return privateGroupMap_ ;}
  public Map<String, String> getPublicGroupMap() { return publicGroupMap_ ; }

  // to show print address book when contacts view is Thumbnail ;
  public boolean getListView(String groupId) {
    if (!groupId.equals(selectedGroup)) return true ;    
    return getAncestorOfType(UIWorkingContainer.class)
      .findFirstComponentOfType(UIContacts.class).getViewContactsList() ;
  }  
  public boolean isDefault(String groupId) { return groupId.equals(defaultGroup) ; }
  
  static public class AddAddressActionListener extends EventListener<UIAddressBooks> {
    public void execute(Event<UIAddressBooks> event) throws Exception {
      UIAddressBooks uiAddressBook = event.getSource();
      UIContactPortlet uiContactPortlet = uiAddressBook.getAncestorOfType(UIContactPortlet.class);
      UIPopupAction uiPopupAction = uiContactPortlet.getChild(UIPopupAction.class);
      uiPopupAction.activate(UICategoryForm.class, 500) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
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
        Contact[] contacts = null ;
        UIExportForm uiExportForm = uiPopupAction.activate(UIExportForm.class, 500) ;
        uiExportForm.setId("ExportForm");
        ContactService contactService = ContactUtils.getContactService() ;
        SessionProvider sessionProvider = SessionsUtils.getSessionProvider() ;
        String username = ContactUtils.getCurrentUser() ;
        Map<String, String> privateGroup = uiAddressBook.privateGroupMap_ ;
        if (privateGroup.containsKey(addressBookId)) {
          uiExportForm.setSelectedGroup(privateGroup.get(addressBookId)) ;
          contacts = contactService.getContactPageListByGroup(
              sessionProvider, username, filter, false).getAll().toArray(new Contact[] {});
        } else if (uiAddressBook.publicGroupMap_.containsKey(addressBookId)){        
          uiExportForm.setSelectedGroup(addressBookId) ;
          contacts = contactService.getContactPageListByGroup(
              sessionProvider, username, filter, true).getAll().toArray(new Contact[] {});
        } else {
          uiExportForm.setSelectedGroup(uiAddressBook.sharedGroupMap_.get(addressBookId)) ;
          contacts = contactService.getSharedContactsByAddressBook(
              sessionProvider, username, addressBookId).getAll().toArray(new Contact[] {}) ;
        }
        if (contacts == null || contacts.length == 0) {
          UIApplication uiApp = uiAddressBook.getAncestorOfType(UIApplication.class) ;
          uiApp.addMessage(new ApplicationMessage("UIAddressBooks.msg.noContactToExport", null,
            ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;  
        }
        uiExportForm.setContacts(contacts) ;         
        uiExportForm.updateList();
      } else {
        
        // There is no specific address book so display the address books list        
        UIExportAddressBookForm uiExportForm = uiPopupAction.activate(UIExportAddressBookForm.class, 500) ;
        uiExportForm.setId("UIExportAddressBookForm");
        Map<String, String> groups = uiAddressBook.privateGroupMap_ ;
        Map<String, String> sharedGroups = uiAddressBook.sharedGroupMap_ ;
        Map<String, String> publicGroups = uiAddressBook.publicGroupMap_ ;
        if ((groups == null || groups.size() == 0) && (sharedGroups == null || sharedGroups.size() == 0)
            && (publicGroups == null || publicGroups.size() == 0)) {
          UIApplication uiApp = uiAddressBook.getAncestorOfType(UIApplication.class) ;
          uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.no-addressbook", null,
            ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;   
        }
        uiExportForm.setContactGroups(groups) ;
        uiExportForm.setPublicContactGroup(publicGroups) ;
        uiExportForm.setSharedContactGroups(sharedGroups) ;
        uiExportForm.updateList();
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
    }
  }
  
  static public class ImportAddressActionListener extends EventListener<UIAddressBooks> {
    public void execute(Event<UIAddressBooks> event) throws Exception {
      UIAddressBooks uiAddressBook = event.getSource();
      UIContactPortlet uiContactPortlet = uiAddressBook.getAncestorOfType(UIContactPortlet.class);
      UIPopupAction uiPopupAction = uiContactPortlet.getChild(UIPopupAction.class);
      String addressBookId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIPopupContainer uiPopupContainer = uiPopupAction.activate(UIPopupContainer.class, 600) ;;
      if (!ContactUtils.isEmpty(addressBookId)){
        uiPopupContainer.setId("ImportContacts") ;
      } else {
        uiPopupContainer.setId("ImportAddress") ;
      }
      UIImportForm uiImportForm = uiPopupContainer.addChild(UIImportForm.class, null, null) ; 
      uiImportForm.setGroup(uiAddressBook.privateGroupMap_) ;
      uiImportForm.addConponent() ;      
      if (!ContactUtils.isEmpty(addressBookId)) uiImportForm.setValues(addressBookId) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction); 
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressBook.getParent());
    }
  }

  static public class AddContactActionListener extends EventListener<UIAddressBooks> {
    public void execute(Event<UIAddressBooks> event) throws Exception {
      UIAddressBooks uiAddressBook = event.getSource() ;  
      UIContactPortlet uiContactPortlet = uiAddressBook.getAncestorOfType(UIContactPortlet.class) ;
      UIPopupAction uiPopupAction = uiContactPortlet.getChild(UIPopupAction.class) ; 
      UIPopupContainer popupContainer = uiPopupAction.activate(UIPopupContainer.class, 800) ;
      popupContainer.setId("AddNewContact") ;
      UICategorySelect uiCategorySelect = popupContainer.addChild(UICategorySelect.class, null, null) ;
      UIContactForm uiContactForm = popupContainer.addChild(UIContactForm.class, null, null) ;
      uiContactForm.setNew(true) ;
      String groupId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      if (uiAddressBook.publicGroupMap_.containsKey(groupId)) {
        uiContactForm.getUIFormCheckBoxInput(groupId).setChecked(true) ;   
      } else {
        if (uiAddressBook.privateGroupMap_.containsKey(groupId)) {
          uiCategorySelect.setPrivateGroupMap(uiAddressBook.privateGroupMap_) ;
          uiContactForm.setShared(false) ;
        } else {
          uiCategorySelect.setPrivateGroupMap(uiAddressBook.sharedGroupMap_) ;
          uiContactForm.setShared(true) ;
        }        
        uiCategorySelect.addCategories() ;
        uiCategorySelect.setValue(groupId) ; 
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }

  static public class EditGroupActionListener extends EventListener<UIAddressBooks> {
    public void execute(Event<UIAddressBooks> event) throws Exception {
      UIAddressBooks uiAddressBook = event.getSource();
      UIContactPortlet uiContactPortlet = uiAddressBook.getAncestorOfType(UIContactPortlet.class);
      UIPopupAction popupAction = uiContactPortlet.getChild(UIPopupAction.class);
      UICategoryForm uiCategoryForm = popupAction.activate(UICategoryForm.class, 500) ;
      String groupId = event.getRequestContext().getRequestParameter(OBJECTID);
      uiCategoryForm.setValues(groupId) ;
      uiCategoryForm.setNew(false) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
    }
  }
  
  static public class ShareGroupActionListener extends EventListener<UIAddressBooks> {
    public void execute(Event<UIAddressBooks> event) throws Exception {
      UIAddressBooks uiAddressBook = event.getSource();
      String groupId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIContactPortlet contactPortlet = uiAddressBook.getAncestorOfType(UIContactPortlet.class) ;
      UIPopupAction popupAction = contactPortlet.getChild(UIPopupAction.class) ;
      UIPopupContainer uiPopupContainer = popupAction.activate(UIPopupContainer.class, 600) ;
      uiPopupContainer.setId("UIPermissionSelectPopup") ;
      UISharedForm uiSharedForm = uiPopupContainer.addChild(UISharedForm.class, null, null) ;
      
      ContactGroup contactGroup = ContactUtils.getContactService()
        .getGroup(SessionsUtils.getSessionProvider(), ContactUtils.getCurrentUser(), groupId) ;
      if (contactGroup != null) {
        uiSharedForm.init(null, contactGroup, true) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      }
    }
  }

  public static class SendEmailActionListener extends EventListener<UIAddressBooks> {
    public void execute(Event<UIAddressBooks> event) throws Exception {
      UIAddressBooks uiAddressBook = event.getSource();
      UIContactPortlet uiContactPortlet = uiAddressBook.getAncestorOfType(UIContactPortlet.class);
      UIPopupAction uiPopupAction = uiContactPortlet.getChild(UIPopupAction.class);
      UISendEmail uiSendEmail = uiPopupAction.createUIComponent(UISendEmail.class, null,
          "UISendEmail");
      String groupId = event.getRequestContext().getRequestParameter(OBJECTID);
      String username = ContactUtils.getCurrentUser();
      ContactService contactService = ContactUtils.getContactService();
      List<String> addresses = contactService
        .getAllEmailAddressByGroup(SessionsUtils.getSessionProvider(), username, groupId);
      uiSendEmail.setEmails(addresses);
      uiPopupAction.activate(uiSendEmail, 700, 0, true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
    }
  }

  static public class DeleteGroupActionListener extends EventListener<UIAddressBooks> {
    public void execute(Event<UIAddressBooks> event) throws Exception {
      UIAddressBooks uiAddressBook = event.getSource();
      String groupId = event.getRequestContext().getRequestParameter(OBJECTID);
      ContactService contactService = ContactUtils.getContactService();
      String username = ContactUtils.getCurrentUser();
      UIWorkingContainer workingContainer = uiAddressBook.getAncestorOfType(UIWorkingContainer.class);
      UIContacts uiContacts = workingContainer.findFirstComponentOfType(UIContacts.class) ;
      if (groupId.equals(uiAddressBook.selectedGroup)) {
        uiAddressBook.selectedGroup = null;
        uiContacts.setContacts(null);
      }
      ContactGroup contactGroup = contactService
        .removeGroup(SessionsUtils.getSessionProvider(), username, groupId);
      if (contactGroup == null) {
        contactService.removeSharedAddressBook(SessionsUtils.getSessionProvider(), username, groupId) ;
      }
      String selectedTag = uiContacts.getSelectedTag() ;
      if (!ContactUtils.isEmpty(selectedTag)) {
        uiContacts.setContacts(
            contactService.getContactPageListByTag(SessionsUtils.getSystemProvider(), username, selectedTag)) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(workingContainer);
    }
  }

  static public class SelectGroupActionListener extends EventListener<UIAddressBooks> {
    public void execute(Event<UIAddressBooks> event) throws Exception {
      UIAddressBooks uiAddressBook = event.getSource();
      UIWorkingContainer uiWorkingContainer = uiAddressBook.getAncestorOfType(UIWorkingContainer.class);
      uiWorkingContainer.findFirstComponentOfType(UITags.class).setSelectedTag(null);
      String groupId = event.getRequestContext().getRequestParameter(OBJECTID);
      uiAddressBook.selectedGroup = groupId;
      UIContacts uiContacts = uiWorkingContainer.findFirstComponentOfType(UIContacts.class);
      uiContacts.setContacts(ContactUtils.getContactService().getContactPageListByGroup(
          SessionsUtils.getSessionProvider(), ContactUtils.getCurrentUser(), groupId));
      uiContacts.setSelectedGroup(groupId);
      uiContacts.setSelectedTag(null);
      uiContacts.setDisplaySearchResult(false) ;
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
          .getPublicContactsByAddressBook(SessionsUtils.getSystemProvider(), groupId));
      uiContacts.setSelectedGroup(groupId);
      uiContacts.setSelectedTag(null);
      uiContacts.setDisplaySearchResult(false) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingContainer);
    }
  }

  static public class SelectSharedGroupActionListener extends EventListener<UIAddressBooks> {
    public void execute(Event<UIAddressBooks> event) throws Exception {
      UIAddressBooks uiAddressBook = event.getSource();
      UIWorkingContainer uiWorkingContainer = uiAddressBook.getAncestorOfType(UIWorkingContainer.class);
      uiWorkingContainer.findFirstComponentOfType(UITags.class).setSelectedTag(null);
      String groupId = event.getRequestContext().getRequestParameter(OBJECTID);
      uiAddressBook.selectedGroup = groupId;
      UIContacts uiContacts = uiWorkingContainer.findFirstComponentOfType(UIContacts.class);
      uiContacts.setContacts(ContactUtils.getContactService().getSharedContactsByAddressBook(
          SessionsUtils.getSystemProvider(),ContactUtils.getCurrentUser(), groupId));
      uiContacts.setSelectedGroup(groupId);
      uiContacts.setSelectedTag(null);
      uiContacts.setDisplaySearchResult(false) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingContainer);
    }
  }  
  
  static public class PrintActionListener extends EventListener<UIAddressBooks> {
    public void execute(Event<UIAddressBooks> event) throws Exception {
      UIAddressBooks uiAddressBook = event.getSource();
      UIWorkingContainer workingContainer = uiAddressBook.getAncestorOfType(UIWorkingContainer.class) ;
      UIContacts uiContacts = workingContainer.findFirstComponentOfType(UIContacts.class) ;      
      UIContactPreview uiContactPreview = workingContainer.findFirstComponentOfType(UIContactPreview.class) ;
      uiContactPreview.setRendered(false) ;
      uiContacts.setViewContactsList(false) ;  
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent()) ;
    }
  }
    
}
