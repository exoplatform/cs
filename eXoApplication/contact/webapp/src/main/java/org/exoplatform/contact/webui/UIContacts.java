/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.webui.popup.UIMoveContactForm;
import org.exoplatform.contact.webui.popup.UITagForm;
import org.exoplatform.contact.webui.popup.UICategorySelect;
import org.exoplatform.contact.webui.popup.UIContactForm;
import org.exoplatform.contact.webui.popup.UIPopupAction;
import org.exoplatform.contact.webui.popup.UIPopupContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
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
        @EventConfig(phase=Phase.DECODE, listeners = UIContacts.DeleteContactsActionListener.class,
            confirm = "UIContacts.msg.confirm-delete"),
        @EventConfig(listeners = UIContacts.MoveContactActionListener.class)
    }
)

public class UIContacts extends UIForm  {
  private boolean addressBookSelected = true ;
  private boolean personalAddressBookSelected = true ;
  private String groupId_ = "";
  private String tagName_ = "";
  public boolean viewContactsList = true ;
  private Map<String, Contact> contactMap = new HashMap<String, Contact> () ;
  public UIContacts() throws Exception {
  } 
  
  public void setContacts(List<Contact> contacts) {
    getChildren().clear() ;
    contactMap.clear();
    for(Contact contact : contacts) {
      addUIFormInput(new UIFormCheckBoxInput<Boolean>(contact.getId(),contact.getId(), false));
      contactMap.put(contact.getId(), contact) ;
    }
  }
  
  public void setAddressBookSelected(boolean selected) { addressBookSelected = selected ; }
  public boolean getAddressBookSelected() { return addressBookSelected ; }
  
  public void setPersonalAddressBookSelected(boolean selected) { personalAddressBookSelected = selected ; }
  public boolean getPersonalAddressBookSelected() { return personalAddressBookSelected ; }
  
  public void setGroupId(String groupId) { groupId_ = groupId ; }
  public String getGroupId() { return groupId_ ; }
  
  public void setTagName(String tagName) { tagName_ = tagName ; }
  public String getTagName() { return tagName_ ; }
  
  public void setViewContactsList(boolean list) { viewContactsList = list ; }
  public boolean getViewContactsList() { return viewContactsList ; }
  
  public void updateContact(Contact contact, boolean isNew) { 
    if (isNew) addUIFormInput(new UIFormCheckBoxInput<Boolean>(contact.getId(), contact.getId(), false)) ;
    contactMap.put(contact.getId(), contact) ;
  }
  
  public void removeContacts(List<Contact> contacts) {
    for (Contact contact : contacts) { contactMap.remove(contact.getId()) ; }
  }
  
  public Contact[] getContacts() throws Exception {
    return contactMap.values().toArray(new Contact[]{}) ;
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
  
  static public class AddTagActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource() ;
      List<String> contactIds = uiContacts.getCheckedContacts() ;
      if (contactIds.size() == 0) {
        UIApplication uiApp = uiContacts.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIContacts.msg.checkContact-required", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      UIContactPortlet contactPortlet = uiContacts.getAncestorOfType(UIContactPortlet.class) ;
      UIPopupAction popupAction = contactPortlet.getChild(UIPopupAction.class) ;
      UITagForm uiTagForm = popupAction.createUIComponent(UITagForm.class, null, null) ;
      uiTagForm.update(contactIds) ;
      popupAction.activate(uiTagForm, 600, 0, true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  static public class DeleteContactsActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      List<String> contactIds = uiContacts.getCheckedContacts() ;
      if (contactIds.size() == 0) {
        UIApplication uiApp = uiContacts.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIContacts.msg.checkContact-required", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      ContactService contactService = uiContacts.getApplicationComponent(ContactService.class);
      String username = Util.getPortalRequestContext().getRemoteUser() ;
      List<Contact> removedContacts = new ArrayList<Contact>();
      for (String contactId : contactIds) {
        if (contactService.getContact(username, contactId) != null)
          removedContacts.add(contactService.getContact(username, contactId)) ;
        if (contactService.getSharedContact(contactId) != null)
          removedContacts.add(contactService.getSharedContact(contactId)) ;
      }
      List<Contact> unremovedContacts = contactService.removeContacts(username, contactIds) ;
      if (unremovedContacts.size() > 0) 
        System.out.println("\n\n unremoved contact size :" + unremovedContacts.size() + "\n\n");
      uiContacts.removeContacts(removedContacts) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts) ;
    }
  }
  
  static public class MoveContactActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      List<String> contactIds = uiContacts.getCheckedContacts() ;
      if (contactIds.size() == 0) {
        UIApplication uiApp = uiContacts.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIContacts.msg.checkContact-required", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      UIContactPortlet contactPortlet = uiContacts.getAncestorOfType(UIContactPortlet.class) ;
      UIPopupAction popupAction = contactPortlet.getChild(UIPopupAction.class) ;
      UIMoveContactForm uiMoveForm = popupAction.createUIComponent(UIMoveContactForm.class, null, null) ;
      uiMoveForm.setPersonalAddressBookSelected(uiContacts.getPersonalAddressBookSelected()) ;
      uiMoveForm.setContacts(contactIds) ;
      uiMoveForm.setGroupId(uiContacts.getGroupId()) ;
      popupAction.activate(uiMoveForm, 600, 0, true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
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
  
  static public class EditContactActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      String contactId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIContactPortlet contactPortlet = uiContacts.getAncestorOfType(UIContactPortlet.class) ;
      UIPopupAction popupAction = contactPortlet.getChild(UIPopupAction.class) ;
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, "UITagForm") ;
      popupContainer.addChild(UICategorySelect.class, null, null) ;
      popupContainer.addChild(UIContactForm.class, null, null) ;
    
      UICategorySelect uiCategorySelect = popupContainer.findFirstComponentOfType(UICategorySelect.class);
      uiCategorySelect.setValues(contactId);
      ContactService contactService = uiContacts.getApplicationComponent(ContactService.class);
      if (contactService.getSharedContact(contactId) != null) uiCategorySelect.disableSelect() ;
      
      UIContactForm uiContactForm = popupContainer.findFirstComponentOfType(UIContactForm.class);
      uiContactForm.setValues(contactId);
      UIContactForm.isNew_ = false ;

      popupAction.activate(popupContainer, 800, 450, true) ;
//      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  
  
  
}
