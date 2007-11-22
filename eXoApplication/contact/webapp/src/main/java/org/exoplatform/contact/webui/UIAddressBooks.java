/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactFilter;
import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.Tag;
import org.exoplatform.contact.webui.popup.UICategoryForm;
import org.exoplatform.contact.webui.popup.UICategorySelect;
import org.exoplatform.contact.webui.popup.UIContactForm;
import org.exoplatform.contact.webui.popup.UIExportAddressBookForm;
import org.exoplatform.contact.webui.popup.UIExportForm;
import org.exoplatform.contact.webui.popup.UIImportForm;
import org.exoplatform.contact.webui.popup.UIPopupAction;
import org.exoplatform.contact.webui.popup.UIPopupContainer;
import org.exoplatform.contact.webui.popup.UISendEmail;
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
    @EventConfig(listeners = UIAddressBooks.DeleteGroupActionListener.class, confirm = "UIAddressBooks.msg.confirm-delete"),
    @EventConfig(listeners = UIAddressBooks.SelectGroupActionListener.class),
    @EventConfig(listeners = UIAddressBooks.SelectSharedGroupActionListener.class),
    @EventConfig(listeners = UIAddressBooks.PrintActionListener.class),
    @EventConfig(listeners = UIAddressBooks.SendEmailActionListener.class) })
public class UIAddressBooks extends UIComponent {
  private String selectedGroup = null;
  private Map<String, String> privateGroupMap_ = new HashMap<String, String>() ;
  private Map<String, String> publicGroupMap_ = new HashMap<String, String>() ;
  
  public UIAddressBooks() throws Exception { }

  public List<ContactGroup> getGroups() throws Exception {
    List<ContactGroup> groupList = ContactUtils.getContactService()
      .getGroups(ContactUtils.getCurrentUser());
    privateGroupMap_.clear() ;
    for (ContactGroup group : groupList) privateGroupMap_.put(group.getId(), group.getName()) ; 
    return groupList;
  }
  public List<String> getSharedContactGroups() throws Exception {
    List<String> sharedGroup = ContactUtils.getContactService()
      .getSharedGroupContacts(ContactUtils.getUserGroups());
    publicGroupMap_.clear() ;
    for (String group : sharedGroup) publicGroupMap_.put(group, group) ; 
    return sharedGroup ;
  }
  public void setSelectedGroup(String groupId) { selectedGroup = groupId ; }
  public String getSelectedGroup() { return selectedGroup ; }

  public Map<String, String> getPrivateGroupMap() { return privateGroupMap_ ;}
  public Map<String, String> getPublicGroupMap() { return publicGroupMap_ ; }

  static public class AddAddressActionListener extends EventListener<UIAddressBooks> {
    public void execute(Event<UIAddressBooks> event) throws Exception {
      UIAddressBooks uiAddressBook = event.getSource();
      UIContactPortlet uiContactPortlet = uiAddressBook.getAncestorOfType(UIContactPortlet.class);
      UIPopupAction uiPopupAction = uiContactPortlet.getChild(UIPopupAction.class);
      UICategoryForm uiCategoryForm = uiPopupAction.createUIComponent(UICategoryForm.class, null,
          "UICategoryForm");
      uiPopupAction.activate(uiCategoryForm, 500, 0, true);
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
        UIExportForm uiExportForm = uiPopupAction.createUIComponent(UIExportForm.class, null,
            "ExportForm");     
        uiExportForm.setSelectedGroup(addressBookId) ;
        ContactFilter filter = new ContactFilter();
        filter.setAscending(true);
        filter.setCategories(new String[] { addressBookId });
        ContactService contactService = ContactUtils.getContactService() ;
        String username = ContactUtils.getCurrentUser() ;
        Contact[] contacts = null ;
        if (ContactUtils.isPublicGroup(addressBookId))
          contacts = contactService.getContactPageListByGroup(username, filter, true)
            .getAll().toArray(new Contact[] {});
        else
          contacts = contactService.getContactPageListByGroup(username, filter, false)
            .getAll().toArray(new Contact[] {});
        if (contacts == null || contacts.length == 0) {
          UIApplication uiApp = uiAddressBook.getAncestorOfType(UIApplication.class) ;
          uiApp.addMessage(new ApplicationMessage("UIAddressBooks.msg.noContactToExport", null,
            ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;  
        }
        
        uiExportForm.setContacts(contacts) ;         
        uiExportForm.updateList();
        uiPopupAction.activate(uiExportForm, 500, 0, true);
      } else {      
        // There is no specific address book 
        // so display the address books list      
        
        UIExportAddressBookForm uiExportForm = uiPopupAction.createUIComponent(
            UIExportAddressBookForm.class, null, "UIExportAddressBookForm");
        ContactGroup[] groups = uiAddressBook.getGroups().toArray(new ContactGroup[] {}) ;
        List<String> sharedGroups = uiAddressBook.getSharedContactGroups() ;
        if ((sharedGroups == null || sharedGroups.size() == 0) && (groups == null || groups.length == 0)) {
          UIApplication uiApp = uiAddressBook.getAncestorOfType(UIApplication.class) ;
          uiApp.addMessage(new ApplicationMessage("UIActionBar.msg.no-addressbook", null,
            ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;   
        }
        uiExportForm.setContactGroups(groups) ;
        uiExportForm.setSharedContactGroup(sharedGroups) ;
        uiExportForm.updateList();
        uiPopupAction.activate(uiExportForm, 500, 0, true);
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
      UIPopupContainer uiPopupContainer ;
      if (!ContactUtils.isEmpty(addressBookId)) 
        uiPopupContainer =  uiPopupAction.createUIComponent(UIPopupContainer.class, null, "ImportContacts") ;
      else
        uiPopupContainer =  uiContactPortlet.createUIComponent(UIPopupContainer.class, null, "ImportAddress") ;
      UIImportForm uiImportForm = uiPopupContainer.addChild(UIImportForm.class, null, null) ; 
      
      if (!ContactUtils.isEmpty(addressBookId)) uiImportForm.setValues(addressBookId) ;
      uiPopupAction.activate(uiPopupContainer, 600, 0, true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction); 
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressBook.getParent());
    }
  }

  static public class AddContactActionListener extends EventListener<UIAddressBooks> {
    public void execute(Event<UIAddressBooks> event) throws Exception {
      UIAddressBooks uiAddressBook = event.getSource() ;  
      UIContactPortlet uiContactPortlet = uiAddressBook.getAncestorOfType(UIContactPortlet.class) ;
      UIPopupAction uiPopupAction = uiContactPortlet.getChild(UIPopupAction.class) ; 
      UIPopupContainer popupContainer = uiPopupAction.createUIComponent(UIPopupContainer.class, null, "AddNewContact") ;
      UICategorySelect uiCategorySelect = popupContainer.addChild(UICategorySelect.class, null, null) ;
      UIContactForm.isNew_ = true ;
      UIContactForm uiContactForm = popupContainer.addChild(UIContactForm.class, null, null) ;
      String groupId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      if (ContactUtils.isPublicGroup(groupId)) {
        uiContactForm.getUIFormCheckBoxInput(UIContactForm.FIELD_ISPUBLIC_BOX).setChecked(true);
        uiContactForm.getUIFormCheckBoxInput(groupId).setChecked(true) ;   
      } else {
        uiCategorySelect.setValue(groupId) ; 
      }
      uiPopupAction.activate(popupContainer, 800, 0, true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }

  static public class EditGroupActionListener extends EventListener<UIAddressBooks> {
    public void execute(Event<UIAddressBooks> event) throws Exception {
      UIAddressBooks uiAddressBook = event.getSource();
      UIContactPortlet uiContactPortlet = uiAddressBook.getAncestorOfType(UIContactPortlet.class);
      UIPopupAction popupAction = uiContactPortlet.getChild(UIPopupAction.class);
      UICategoryForm uiCategoryForm = popupAction.createUIComponent(UICategoryForm.class, null,
          "UICategoryForm");
      String groupId = event.getRequestContext().getRequestParameter(OBJECTID);
      uiCategoryForm.setValues(groupId);
      uiCategoryForm.setNew(false) ;
      popupAction.activate(uiCategoryForm, 500, 0, true);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
    }
  }
  
  static public class ShareGroupActionListener extends EventListener<UIAddressBooks> {
    public void execute(Event<UIAddressBooks> event) throws Exception {
      UIAddressBooks uiAddressBook = event.getSource();
      UIApplication uiApp = uiAddressBook.getAncestorOfType(UIApplication.class) ;
      uiApp.addMessage(new ApplicationMessage("UIAddressBooks.msg.not-already", null)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      return ;
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
      List<String> addresses = contactService.getAllEmailAddressByGroup(username, groupId);
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
      
      
      UIContactContainer contactContainer = uiAddressBook.getAncestorOfType(
        UIWorkingContainer.class).getChild(UIContactContainer.class);
      UIContacts uiContacts = contactContainer.getChild(UIContacts.class) ;
      if (groupId.equals(uiAddressBook.selectedGroup)) {
        uiAddressBook.selectedGroup = null;
        uiContacts.setContacts(null);
        event.getRequestContext().addUIComponentToUpdateByAjax(contactContainer);
      }  
      contactService.removeGroup(username, groupId);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressBook);
    }
  }

  static public class SelectGroupActionListener extends EventListener<UIAddressBooks> {
    public void execute(Event<UIAddressBooks> event) throws Exception {
      UIAddressBooks uiAddressBook = event.getSource();
      UIWorkingContainer uiWorkingContainer = uiAddressBook
          .getAncestorOfType(UIWorkingContainer.class);
      uiWorkingContainer.findFirstComponentOfType(UITags.class).setSelectedTag(null);
      String groupId = event.getRequestContext().getRequestParameter(OBJECTID);
      uiAddressBook.selectedGroup = groupId;
      ContactService contactService = ContactUtils.getContactService();
      UIContacts uiContacts = uiWorkingContainer.findFirstComponentOfType(UIContacts.class);
      uiContacts.setContacts(contactService.getContactPageListByGroup(
          ContactUtils.getCurrentUser(), groupId));
      uiContacts.setSelectedGroup(groupId);
      uiContacts.setSelectedTag(null);
      uiContacts.setDisplaySearchResult(false) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingContainer);
    }
  }

  static public class SelectSharedGroupActionListener extends EventListener<UIAddressBooks> {
    public void execute(Event<UIAddressBooks> event) throws Exception {
      UIAddressBooks uiAddressBook = event.getSource();
      UIWorkingContainer uiWorkingContainer = uiAddressBook
          .getAncestorOfType(UIWorkingContainer.class);
      uiWorkingContainer.findFirstComponentOfType(UITags.class).setSelectedTag(null);
      String groupId = event.getRequestContext().getRequestParameter(OBJECTID);
      uiAddressBook.selectedGroup = groupId;
      ContactService contactService = ContactUtils.getContactService();
      UIContacts uiContacts = uiWorkingContainer.findFirstComponentOfType(UIContacts.class);
      uiContacts.setContacts(contactService.getSharedContactsByGroup(groupId));
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
