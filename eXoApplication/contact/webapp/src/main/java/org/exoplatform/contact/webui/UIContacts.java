/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.webui.popup.UIMoveContactForm;
import org.exoplatform.contact.webui.popup.UITagForm;
import org.exoplatform.contact.webui.popup.UICategorySelect;
import org.exoplatform.contact.webui.popup.UIContactForm;
import org.exoplatform.contact.webui.popup.UIPopupAction;
import org.exoplatform.contact.webui.popup.UIPopupContainer;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/templates/contact/webui/UIContacts.gtmpl",
    events = {
        @EventConfig(listeners = UIContacts.SelectedContactActionListener.class),
        @EventConfig(listeners = UIContacts.AddTagActionListener.class),
        @EventConfig(listeners = UIContacts.EditContactActionListener.class),
        @EventConfig(listeners = UIContacts.DeleteContactsActionListener.class),
        @EventConfig(listeners = UIContacts.MoveContactsActionListener.class),
        @EventConfig(listeners = UIContacts.ViewDetailsActionListener.class)
    }
)

public class UIContacts extends UIForm  {
  public boolean viewContactsList = true ;
  private Map<String, Contact> contactMap = new HashMap<String, Contact> () ;
  final public static String EDIT_CONTACT = "EditContact".intern() ;
  final public static String SEND_EMAIL = "Send Email".intern() ;
  final public static String INSTACE_MESSAGE = "Instant Message".intern() ;
  final public static String TAG = "Tag".intern() ;
  final public static String MOVE_CONTACT = "Move Contact".intern() ;
  final public static String DELETE_CONTACT = "Delete Contact".intern() ;
  final public static String PRINT_CONTACT = "Print this Contact".intern() ;
  final public static String[] SELECTIONS = { EDIT_CONTACT, SEND_EMAIL , INSTACE_MESSAGE, TAG, MOVE_CONTACT, DELETE_CONTACT, PRINT_CONTACT } ;

  public UIContacts() throws Exception { } 
  public String[] getSelections() { return SELECTIONS ; }
  
  public void setContacts(List<Contact> contacts) throws Exception {
    getChildren().clear() ;
    contactMap.clear();
    for(Contact contact : contacts) {
      addUIFormInput(new UIFormCheckBoxInput<Boolean>(contact.getId(),contact.getId(), false));
      contactMap.put(contact.getId(), contact) ;
    }
    UIContactPreview uiContactPreview = getAncestorOfType(UIContactContainer.class).findFirstComponentOfType(UIContactPreview.class) ;
    uiContactPreview.updateContact() ;
  }
  public Contact[] getContacts() throws Exception { return contactMap.values().toArray(new Contact[]{}) ; }
  
  public void setViewContactsList(boolean list) { viewContactsList = list ; }
  public boolean getViewContactsList() { return viewContactsList ; }
  
  public void updateContact(Contact contact, boolean isNew) { 
    if (isNew) addUIFormInput(new UIFormCheckBoxInput<Boolean>(contact.getId(), contact.getId(), false)) ;
    contactMap.put(contact.getId(), contact) ; 
  }
  
  public void removeContacts(List<String> contactIds) throws Exception {
    for (String contactId : contactIds)  contactMap.remove(contactId) ;
    UIContactPreview uiContactPreview = getAncestorOfType(UIContactContainer.class).findFirstComponentOfType(UIContactPreview.class) ;
    uiContactPreview.updateContact() ;
  }
  
  public List<String> getCheckedContacts() throws Exception {
    List<String> checkedContacts = new ArrayList<String>() ;
    for (Contact contact : getContacts()) {
      UIFormCheckBoxInput uiCheckBox = getChildById(contact.getId()) ;
      if(uiCheckBox != null && uiCheckBox.isChecked()) {
        checkedContacts.add(contact.getId()) ;
      }
    }
    return checkedContacts ;
  }
  
  static public class EditContactActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      String contactId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIContactPortlet contactPortlet = uiContacts.getAncestorOfType(UIContactPortlet.class) ;
      UIPopupAction popupAction = contactPortlet.getChild(UIPopupAction.class) ;
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, "AddNewContact") ;
      popupContainer.addChild(UICategorySelect.class, null, null) ;
      popupContainer.addChild(UIContactForm.class, null, null) ;
      UICategorySelect uiCategorySelect = popupContainer.findFirstComponentOfType(UICategorySelect.class);
      uiCategorySelect.setValue(contactId);
      uiCategorySelect.disableSelect() ;
      UIContactForm uiContactForm = popupContainer.findFirstComponentOfType(UIContactForm.class);
      uiContactForm.setValues(contactId);
      UIContactForm.isNew_ = false ;
      popupAction.activate(popupContainer, 800, 0, true) ;
    }
  }
  
  static public class ViewDetailsActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      String contactId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIContactPortlet contactPortlet = uiContacts.getAncestorOfType(UIContactPortlet.class) ;
      UIPopupAction popupAction = contactPortlet.getChild(UIPopupAction.class) ;
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, "AddNewContact") ;
      popupContainer.addChild(UICategorySelect.class, null, null) ;
      popupContainer.addChild(UIContactForm.class, null, null) ;
      UICategorySelect uiCategorySelect = popupContainer.findFirstComponentOfType(UICategorySelect.class);
      uiCategorySelect.setValue(contactId);
      uiCategorySelect.disableSelect() ;
      UIContactForm uiContactForm = popupContainer.findFirstComponentOfType(UIContactForm.class);
      uiContactForm.setValues(contactId);
      UIContactForm.isNew_ = false ;
      popupAction.activate(popupContainer, 800, 0, true) ;
    }
  }
  
  static public class AddTagActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource() ;
      String contactId = event.getRequestContext().getRequestParameter(OBJECTID);
      List<String> contactIds = new ArrayList<String>();
      if (contactId != null) contactIds.add(contactId) ;
      else {
        contactIds = uiContacts.getCheckedContacts() ;
        if (contactIds.size() == 0) {
          UIApplication uiApp = uiContacts.getAncestorOfType(UIApplication.class) ;
          uiApp.addMessage(new ApplicationMessage("UIContacts.msg.checkContact-required", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
      }    
      UIContactPortlet contactPortlet = uiContacts.getAncestorOfType(UIContactPortlet.class) ;
      UIPopupAction popupAction = contactPortlet.getChild(UIPopupAction.class) ;
      UITagForm uiTagForm = popupAction.createUIComponent(UITagForm.class, null, null) ;
      uiTagForm.setContacts(contactIds) ;
      popupAction.activate(uiTagForm, 600, 0, true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  static public class MoveContactsActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      String contactId = event.getRequestContext().getRequestParameter(OBJECTID);   
      List<String> contactIds = new ArrayList<String>();
      UIApplication uiApp = uiContacts.getAncestorOfType(UIApplication.class) ;
      if (contactId != null) contactIds.add(contactId) ;
      else {
        contactIds = uiContacts.getCheckedContacts() ;
        if (contactIds.size() == 0) { 
          uiApp.addMessage(new ApplicationMessage("UIContacts.msg.checkContact-required", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
      }    
      UIContactPortlet uiContactPortlet = uiContacts.getAncestorOfType(UIContactPortlet.class) ;
      UIPopupAction popupAction = uiContactPortlet.getChild(UIPopupAction.class) ;
      UIMoveContactForm uiMoveForm = popupAction.createUIComponent(UIMoveContactForm.class, null, null) ;
      UIAddressBooks uiAddressBook = uiContactPortlet.findFirstComponentOfType(UIAddressBooks.class) ;
      uiMoveForm.setContacts(contactIds) ;
      uiMoveForm.setGroupId(uiAddressBook.getSelectedGroup()) ;
      popupAction.activate(uiMoveForm, 600, 0, true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  static public class DeleteContactsActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      String contactId = event.getRequestContext().getRequestParameter(OBJECTID);
      List<String> contactIds = new ArrayList<String>();
      if (contactId != null) contactIds.add(contactId) ;
      else {
        UIApplication uiApp = uiContacts.getAncestorOfType(UIApplication.class) ;
        contactIds = uiContacts.getCheckedContacts() ;
        if (contactIds.size() == 0) {
          uiApp.addMessage(new ApplicationMessage("UIContacts.msg.checkContact-required", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
      }
      ContactService contactService = ContactUtils.getContactService();
      String username =  ContactUtils.getCurrentUser();
      List<Contact> unremovedContacts = contactService.removeContacts(username, contactIds) ;
      if (unremovedContacts != null && unremovedContacts.size() > 0) 
        System.out.println("\n\n unremoved contacts size :" + unremovedContacts.size() + "\n\n");
      for (Contact contact : unremovedContacts) contactIds.remove(contact.getId()) ;
      uiContacts.removeContacts(contactIds) ;
      UIWorkingContainer uiWorkingContainer = uiContacts.getAncestorOfType(UIWorkingContainer.class) ;
      UIContactPreview uiContactPreview = uiWorkingContainer.findFirstComponentOfType(UIContactPreview.class) ;
      uiContactPreview.updateContact() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingContainer) ;
    }
  }

  static public class SelectedContactActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      String contactId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIContactContainer uiContactContainer = uiContacts.getAncestorOfType(UIContactContainer.class);
      UIContactPreview uiContactPreview = uiContactContainer.findFirstComponentOfType(UIContactPreview.class);
      uiContactPreview.setContact(uiContacts.contactMap.get(contactId));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContactContainer);
    }
  } 
  
}
