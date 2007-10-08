/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.JCRPageList;
import org.exoplatform.contact.webui.popup.UIContactPreviewForm;
import org.exoplatform.contact.webui.popup.UIMoveContactForm;
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
        @EventConfig(listeners = UIContacts.AddTagActionListener.class),
        @EventConfig(listeners = UIContacts.MoveContactsActionListener.class),
        @EventConfig(listeners = UIContacts.DeleteContactsActionListener.class),
        @EventConfig(listeners = UIContacts.SelectedContactActionListener.class),
        @EventConfig(listeners = UIContacts.ViewDetailsActionListener.class),
        @EventConfig(listeners = UIContacts.SortByNameActionListener.class),
        @EventConfig(listeners = UIContacts.SortByEmailActionListener.class),
        @EventConfig(listeners = UIContacts.SortByOrganitionActionListener.class),
        @EventConfig(listeners = UIContacts.FirstPageActionListener.class),
        @EventConfig(listeners = UIContacts.PreviousPageActionListener.class),
        @EventConfig(listeners = UIContacts.NextPageActionListener.class),
        @EventConfig(listeners = UIContacts.LastPageActionListener.class)
    }
)

public class UIContacts extends UIForm {
  public boolean viewContactsList = true ;
  private Map<String, Contact> contactMap = new HashMap<String, Contact> () ;
  private String selectedGroup = null ;
  private String selectedContact = null ;
  private JCRPageList pageList_ = null ;
  final public static String EDIT_CONTACT = "EditContact".intern() ;
  final public static String SEND_EMAIL = "Send Email".intern() ;
  final public static String INSTACE_MESSAGE = "Instant Message".intern() ;
  final public static String TAG = "Tag".intern() ;
  final public static String MOVE_CONTACT = "Move Contact".intern() ;
  final public static String DELETE_CONTACT = "Delete Contact".intern() ;
  final public static String PRINT_CONTACT = "Print this Contact".intern() ;
  final public static String[] SELECTIONS = { EDIT_CONTACT, SEND_EMAIL , INSTACE_MESSAGE, TAG, MOVE_CONTACT, DELETE_CONTACT, PRINT_CONTACT } ;
  private boolean nameAsc = true ;
  private int firstPoint, lastPoint ;
  
  public UIContacts() throws Exception {
    int totalContacts = getTotalContacts();
    if (totalContacts > 0)firstPoint = 1 ; {
      if (totalContacts >= 10) lastPoint = 10 ;
      else lastPoint = totalContacts ;
    }    
  } 
  public String[] getSelections() { return SELECTIONS ; }
  
  public int getTotalContacts() { return contactMap.size() ; }
  
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
  
  public DownloadService getDownloadService() { 
    return getApplicationComponent(DownloadService.class) ; 
  }
  
  // TO DO

  public void setPageList(JCRPageList pageList, long page) throws Exception {
    getChildren().clear();
    pageList_ = pageList ;
    for (Contact contact : pageList.getPage(page, ContactUtils.getCurrentUser())) {
      addUIFormInput(new UIFormCheckBoxInput<Boolean>(contact.getId(),contact.getId(), false)) ;
    }
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

  static public class AddTagActionListener extends EventListener<UIContacts> {
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
      UIMoveContactForm uiMoveForm = popupAction.createUIComponent(UIMoveContactForm.class, null, null) ;
      UIAddressBooks uiAddressBook = uiContactPortlet.findFirstComponentOfType(UIAddressBooks.class) ;
      uiMoveForm.setContacts(contactIds) ;
      uiMoveForm.setGroupId(uiAddressBook.getSelectedGroup()) ;
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
        UIApplication uiApp = uiContacts.getAncestorOfType(UIApplication.class) ;
        contactIds = uiContacts.getCheckedContacts() ;
        if (contactIds.size() == 0) {
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
      uiContacts.updateList() ; //refresh current page
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
  
  static public class SortByNameActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource() ;
      List<Contact> listContacts = new ArrayList<Contact>() ;
      Contact[] contacts = uiContacts.getContacts() ;
      for (Contact contact : contacts) {
        System.out.println("\n\n contact 1 :" +contact.getFullName() + "\n\n");
        listContacts.add(contact) ;
      }
      if (uiContacts.nameAsc = true) {
        Collections.sort(listContacts, new NameComparator()) ;
      }
      System.out.println("\n\n after sort: \n\n");
      Map<String, Contact> mapContacts = new HashMap<String, Contact> () ;
      for (Contact contact : listContacts) {
        System.out.println("\n\n contact 2 :" + contact.getFullName() + "\n\n");
        mapContacts.put(contact.getId(), contact) ;
      }
      uiContacts.contactMap.clear() ;
      uiContacts.contactMap = mapContacts ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts) ;
    }
  } 
  
  static public class NameComparator implements Comparator {
    public int compare(Object o1, Object o2) throws ClassCastException {
      String name1 = ((Contact) o1).getFullName() ;
      String name2 = ((Contact) o2).getFullName() ;
      return name1.compareToIgnoreCase(name2) ;
    }
  }
  
  static public class SortByEmailActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      System.out.println("\n\n email \n\n");      
    }
  }
  
  static public class SortByOrganitionActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      System.out.println("\n\n organition \n\n");      
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
  
}
