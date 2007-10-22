/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactPageList;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.JCRPageList;
import org.exoplatform.contact.webui.popup.UIContactPreviewForm;
import org.exoplatform.contact.webui.popup.UIMoveContactsForm;
import org.exoplatform.contact.webui.popup.UIPopupComponent;
import org.exoplatform.contact.webui.popup.UITagForm;
import org.exoplatform.contact.webui.popup.UICategorySelect;
import org.exoplatform.contact.webui.popup.UIContactForm;
import org.exoplatform.contact.webui.popup.UIPopupAction;
import org.exoplatform.contact.webui.popup.UIPopupContainer;
import org.exoplatform.download.DownloadService;
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
        @EventConfig(listeners = UIContacts.EditContactActionListener.class),
        @EventConfig(listeners = UIContacts.TagActionListener.class),
        @EventConfig(listeners = UIContacts.MoveContactsActionListener.class),
        @EventConfig(listeners = UIContacts.DeleteContactsActionListener.class),
        @EventConfig(listeners = UIContacts.SelectedContactActionListener.class),
        @EventConfig(listeners = UIContacts.ViewDetailsActionListener.class),
        @EventConfig(listeners = UIContacts.SortActionListener.class),
        @EventConfig(listeners = UIContacts.FirstPageActionListener.class),
        @EventConfig(listeners = UIContacts.PreviousPageActionListener.class),
        @EventConfig(listeners = UIContacts.NextPageActionListener.class),
        @EventConfig(listeners = UIContacts.LastPageActionListener.class),
        @EventConfig(listeners = UIContacts.CancelActionListener.class)
    }
)

public class UIContacts extends UIForm implements UIPopupComponent {
  public boolean viewContactsList = true ;
  private String selectedTag_ = null ;
  private LinkedHashMap<String, Contact> contactMap = new LinkedHashMap<String, Contact> () ;
  private String selectedGroup = null ;
  private String selectedContact = null ;
  private JCRPageList pageList_ = null ;
  private String sortedBy_ = null;
  private boolean isAscending_ = true;
  private String viewQuery_ = null;
  
  public UIContacts() throws Exception { } 
  public String[] getActions() { return new String[] {"Cancel"} ; }
  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
  
  public void setAscending(boolean isAsc) { isAscending_ = isAsc ; }
  public boolean isAscending() {return isAscending_ ; }
  public void setSortedBy(String s) { sortedBy_ = s ; }
  public String getSortedBy() { return sortedBy_ ; }
  public String getViewQuery() {return viewQuery_ ;}
  public void setViewQuery(String view) {viewQuery_ = view ;}
  
  
  public JCRPageList getContactPageList() { return pageList_ ; }
  
  public void setContacts(JCRPageList pageList) throws Exception {
    pageList_ = pageList ;
    updateList() ;
  }
  public Contact[] getContacts() throws Exception { return contactMap.values().toArray(new Contact[]{}) ; }
  
  public void setSelectedContact(String s) { selectedContact = s ; }
  public String getSelectedContact() { return selectedContact ; }
  
  public void setSelectedGroup(String s) { selectedGroup = s ; }
  public String getSelectedGroup() { return selectedGroup ; }
  
  public void setViewContactsList(boolean list) { viewContactsList = list ; }
  public boolean getViewContactsList() { return viewContactsList ; }
  
  public void updateList() throws Exception { 
    getChildren().clear() ;
    contactMap.clear();
    if(pageList_ != null) {
      for(Contact contact : pageList_.getPage(pageList_.getCurrentPage(),ContactUtils.getCurrentUser())) {
        UIFormCheckBoxInput<Boolean> checkbox = new UIFormCheckBoxInput<Boolean>(contact.getId(),contact.getId(), false) ;
        addUIFormInput(checkbox);
        contactMap.put(contact.getId(), contact) ;
      }
      Contact[] array = contactMap.values().toArray(new Contact[]{}) ;
      if (array.length > 0) {
        Contact firstContact = array[0] ;
        getAncestorOfType(UIContactContainer.class).getChild(UIContactPreview.class).setContact(firstContact) ;
        selectedContact = firstContact.getId() ;
      } else getAncestorOfType(UIContactContainer.class).getChild(UIContactPreview.class).setContact(null) ;
    } else getAncestorOfType(UIContactContainer.class).getChild(UIContactPreview.class).setContact(null) ;
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
  
  // TODO : remove ....
  public List<String> getAllContactIds() throws Exception {
    List<String> contactIds = new ArrayList<String>() ;
    System.out.println("\n\n\n>>>khd : UIContacts.getAllContactIds ...");
    for (Contact contact : getContacts()) {
      System.out.println(">>>khd : contactId = " + contact.getId());
      UIFormCheckBoxInput uiCheckBox = getChildById(contact.getId()) ;
      if(uiCheckBox != null) {
        if (uiCheckBox.isChecked())
          System.out.println(">>>khd : uiCheckBox is CHECKED");
        else
          System.out.println(">>>khd : uiCheckBox is NOT checked");

      } else {
        System.out.println(">>>khd : uiCheckBox is NULLLLLLLLLLLLLLLLLLLLLL");
      }
      contactIds.add(contact.getId()) ;
    }
    System.out.println(">>>khd : QUITTTTTTTTTTTTTTTTTTTTTTTTT");
    return contactIds ;
  }
  
  public DownloadService getDownloadService() { 
    return getApplicationComponent(DownloadService.class) ; 
  }
  
  public void setPageList(JCRPageList pageList, long page) throws Exception {
    getChildren().clear();
    pageList_ = pageList ;
    for (Contact contact : pageList.getPage(page, ContactUtils.getCurrentUser())) {
      addUIFormInput(new UIFormCheckBoxInput<Boolean>(contact.getId(),contact.getId(), false)) ;
    }
  }
  
  public String getSelectedTag() {return selectedTag_ ;}
  public void setSelectedTag(String tagId) {selectedTag_ = tagId ;}
  
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
      
      ContactService contactService = ContactUtils.getContactService();
      String username = ContactUtils.getCurrentUser() ;
      Contact contact = contactService.getContact(username, contactId);
      if (contact != null && contact.getCategories().length > 0) uiCategorySelect.setValue(contact.getCategories()[0]) ;
      uiCategorySelect.disableSelect() ;
      UIContactForm uiContactForm = popupContainer.findFirstComponentOfType(UIContactForm.class);
      uiContactForm.setValues(uiContacts.contactMap.get(contactId));
      UIContactForm.isNew_ = false ;
      popupAction.activate(popupContainer, 800, 0, true) ;
    }
  }

  static public class TagActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource() ;
      String contactId = event.getRequestContext().getRequestParameter(OBJECTID);
      List<String> contactIds = new ArrayList<String>();
      if (!ContactUtils.isEmpty(contactId)) contactIds.add(contactId) ;
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
      UITagForm.isNew = true ;
      UITagForm.contactIds_ = contactIds ;
      UITagForm uiTagForm = popupAction.createUIComponent(UITagForm.class, null, "UITagForm") ;
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
      if (!ContactUtils.isEmpty(contactId)) contactIds.add(contactId) ;
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
      UIMoveContactsForm.groupId_ = uiContacts.selectedGroup ;
      UIMoveContactsForm.contactIds_ = contactIds ;
      UIMoveContactsForm uiMoveForm = popupAction.createUIComponent(UIMoveContactsForm.class, null, null) ;
      if (contactIds.size() == 1)  uiMoveForm.setChecked() ;
      popupAction.activate(uiMoveForm, 410, 0, true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  static public class DeleteContactsActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      String contactId = event.getRequestContext().getRequestParameter(OBJECTID);
      List<String> contactIds = new ArrayList<String>();
      if (!ContactUtils.isEmpty(contactId)) contactIds.add(contactId) ;
      else {
        contactIds = uiContacts.getCheckedContacts() ;
        if (contactIds.size() == 0) {
          UIApplication uiApp = uiContacts.getAncestorOfType(UIApplication.class) ;
          uiApp.addMessage(new ApplicationMessage("UIContacts.msg.checkContact-required", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
      }
      UIWorkingContainer uiWorkingContainer = uiContacts.getAncestorOfType(UIWorkingContainer.class) ;
      UIContactPreview uiContactPreview = uiWorkingContainer.findFirstComponentOfType(UIContactPreview.class) ;
      ContactUtils.getContactService().removeContacts(ContactUtils.getCurrentUser(), contactIds) ;
      if(contactIds.contains(uiContactPreview.getContact().getId())) 
        uiContactPreview.setContact(null) ;
      if (uiContacts.getSelectedTag() != null) {
        String tagName = uiWorkingContainer.findFirstComponentOfType(UITags.class).getSelectedTag() ;
        uiContacts.setContacts(ContactUtils.getContactService()
          .getContactPageListByTag(ContactUtils.getCurrentUser(), tagName)) ;
      } else {
        uiContacts.updateList() ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingContainer.getChild(UIContactContainer.class)) ;
    }
  }
  
  static public class SelectedContactActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      String contactId = event.getRequestContext().getRequestParameter(OBJECTID);
      uiContacts.setSelectedContact(contactId) ;
      UIContactContainer uiContactContainer = uiContacts.getAncestorOfType(UIContactContainer.class);
      UIContactPreview uiContactPreview = uiContactContainer.findFirstComponentOfType(UIContactPreview.class);
      uiContactPreview.setContact(uiContacts.contactMap.get(contactId));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContactContainer);
    }
  }
  
  static public class ViewDetailsActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      String contactId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIContactPortlet contactPortlet = uiContacts.getAncestorOfType(UIContactPortlet.class) ;
      UIPopupAction popupAction = contactPortlet.getChild(UIPopupAction.class) ;
      UIContactPreviewForm uiContactPreviewForm = popupAction.createUIComponent(UIContactPreviewForm.class, null, "UIContactPreviewForm") ; 
      uiContactPreviewForm.setContact(uiContacts.contactMap.get(contactId)) ;
      popupAction.activate(uiContactPreviewForm, 700, 0, true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;      
    }
  } 
  
  static public class FirstPageActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource() ; 
      JCRPageList pageList = uiContacts.getContactPageList(); 
      uiContacts.setPageList(pageList, 1) ;
      uiContacts.updateList() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts);
    }
  }
  
  static public class PreviousPageActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource() ; 
      JCRPageList pageList = uiContacts.getContactPageList(); 
      if (pageList.getCurrentPage() > 1){
        uiContacts.setPageList(pageList, pageList.getCurrentPage() - 1);
      }
      uiContacts.updateList() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts);
    }
  }
  
  static public class NextPageActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource() ; 
      JCRPageList pageList = uiContacts.getContactPageList() ; 
      if (pageList.getCurrentPage() < pageList.getAvailablePage()){
        uiContacts.setPageList(pageList, pageList.getCurrentPage() + 1);
      }
      uiContacts.updateList() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts);
    }
  }
  
  static public class LastPageActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource() ; 
      JCRPageList pageList = uiContacts.getContactPageList(); 
      uiContacts.setPageList(pageList, pageList.getAvailablePage());
      uiContacts.updateList() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts);
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource() ;
      UIPopupAction uiPopupAction = uiContacts.getAncestorOfType(UIPopupAction.class) ;
      uiPopupAction.deActivate() ;
    }
  }
  
  static public class SortActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource() ;
      String sortedBy = event.getRequestContext().getRequestParameter(OBJECTID) ;
      ContactService contactService = ContactUtils.getContactService() ;
      String username = ContactUtils.getCurrentUser() ;
      uiContacts.setAscending(!uiContacts.isAscending_);
      uiContacts.setSortedBy(sortedBy);
      if (uiContacts.selectedGroup != null) {
        ContactPageList list =  contactService.getContactPageListByGroup(username, uiContacts.selectedGroup
            , uiContacts.getViewQuery(), uiContacts.getSortedBy(), uiContacts.isAscending_) ;
        List<Contact> contacts = list.getPage(list.getCurrentPage(), username) ;
        for (Contact contact : contacts) System.out.println("\n\n name:" + contact.getFullName() + "\n\n");

        uiContacts.setContacts(contactService.getContactPageListByGroup(username, uiContacts.selectedGroup, uiContacts.getViewQuery(), uiContacts.getSortedBy(), uiContacts.isAscending_));
      } else if (uiContacts.getSelectedTag() != null) {
        uiContacts.setContacts(contactService.getContactPageListByTag(username, uiContacts.getSelectedTag(), uiContacts.getViewQuery(), uiContacts.getSortedBy(), uiContacts.isAscending_));
      }
      uiContacts.updateList();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent());
    }
  }
  
}
