/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui;

import java.util.List;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.webui.popup.UICategoryForm;
import org.exoplatform.contact.webui.popup.UICategorySelect;
import org.exoplatform.contact.webui.popup.UIContactForm;
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
    @EventConfig(listeners = UIAddressBooks.AddressPopupActionListener.class),
    @EventConfig(listeners = UIAddressBooks.SendEmailActionListener.class) })
public class UIAddressBooks extends UIComponent {
  private String selectedGroup = null;

  public UIAddressBooks() throws Exception {
  }

  public List<ContactGroup> getGroups() throws Exception {
    List<ContactGroup> groupList = ContactUtils.getContactService().getGroups(
        ContactUtils.getCurrentUser());
    return groupList;
  }

  public void setSelectedGroup(String groupId) {
    selectedGroup = groupId;
  }

  public String getSelectedGroup() {
    return selectedGroup;
  }

  public List<String> getSharedContactGroups() throws Exception {
    return ContactUtils.getContactService().getSharedGroupContacts(ContactUtils.getUserGroups());
  }

  static public class AddAddressActionListener extends EventListener<UIAddressBooks> {
    public void execute(Event<UIAddressBooks> event) throws Exception {
      UIAddressBooks uiAddressBook = event.getSource();
      UIContactPortlet uiContactPortlet = uiAddressBook.getAncestorOfType(UIContactPortlet.class);
      UIPopupAction uiPopupAction = uiContactPortlet.getChild(UIPopupAction.class);
      UICategoryForm uiCategoryForm = uiPopupAction.createUIComponent(UICategoryForm.class, null,
          "UICategoryForm");
      UICategoryForm.isNew_ = true;
      uiPopupAction.activate(uiCategoryForm, 500, 0, true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
    }
  }

  static public class ImportAddressActionListener extends EventListener<UIAddressBooks> {
    public void execute(Event<UIAddressBooks> event) throws Exception {
      UIAddressBooks uiAddressBook = event.getSource();
      UIContactPortlet uiContactPortlet = uiAddressBook.getAncestorOfType(UIContactPortlet.class);
      UIPopupAction uiPopupAction = uiContactPortlet.getChild(UIPopupAction.class);
      UIPopupContainer popupContainer = uiPopupAction.createUIComponent(UIPopupContainer.class,
          null, "ImportForm");
      popupContainer.addChild(UICategorySelect.class, null, null);
      popupContainer.addChild(UIImportForm.class, null, null);
      /*
      String groupId = event.getRequestContext().getRequestParameter(OBJECTID);
      uiCategorySelect.setValue(groupId);
      */
      uiPopupAction.activate(popupContainer, 600, 0, true);
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
        uiExportForm.setSelectedGroup(addressBookId);
        uiExportForm.updateList();
        uiPopupAction.activate(uiExportForm, 500, 0, true);
      } 
      
      /*
      else {
        // There is no specific address book 
        // so display the address books list
        UIExportAddressBookForm uiExportForm = uiPopupAction.createUIComponent(
            UIExportAddressBookForm.class, null, "UIExportAddressBookForm");
        uiExportForm.updateList();
        uiPopupAction.activate(uiExportForm, 500, 0, true);
      }
      */
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
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
      UICategoryForm.isNew_ = false;
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
      contactService.removeGroup(username, groupId);
      if (groupId.equals(uiAddressBook.selectedGroup)) {
        uiAddressBook.selectedGroup = null;
        UIContactContainer contactContainer = uiAddressBook.getAncestorOfType(
            UIWorkingContainer.class).getChild(UIContactContainer.class);
        contactContainer.getChild(UIContacts.class).setContacts(null);
        event.getRequestContext().addUIComponentToUpdateByAjax(contactContainer);
      }
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

  public static class AddressPopupActionListener extends EventListener<UIAddressBooks> {
    public void execute(Event<UIAddressBooks> event) throws Exception {

    }
  }

}
