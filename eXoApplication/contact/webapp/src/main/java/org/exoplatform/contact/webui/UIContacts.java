/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactFilter;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.JCRPageList;
import org.exoplatform.contact.webui.popup.UIContactPreviewForm;
import org.exoplatform.contact.webui.popup.UIExportForm;
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
        @EventConfig(listeners = UIContacts.EditContactActionListener.class),
        @EventConfig(listeners = UIContacts.InstantMessageActionListener.class),
        @EventConfig(listeners = UIContacts.TagActionListener.class),
        @EventConfig(listeners = UIContacts.MoveContactsActionListener.class),
        @EventConfig(phase=Phase.DECODE, listeners = UIContacts
          .DeleteContactsActionListener.class, confirm="UIContacts.msg.confirm-delete-contact"),
        @EventConfig(listeners = UIContacts.SelectedContactActionListener.class), 
        @EventConfig(listeners = UIContacts.ViewDetailsActionListener.class),
        @EventConfig(listeners = UIContacts.SortActionListener.class),
        @EventConfig(listeners = UIContacts.FirstPageActionListener.class),
        @EventConfig(listeners = UIContacts.PreviousPageActionListener.class),
        @EventConfig(listeners = UIContacts.NextPageActionListener.class),
        @EventConfig(listeners = UIContacts.LastPageActionListener.class),
        @EventConfig(listeners = UIContacts.ExportContactActionListener.class),
        @EventConfig(listeners = UIContacts.CancelActionListener.class),
        @EventConfig(listeners = UIContacts.CloseSearchActionListener.class)
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
  public static String fullName = "fullName".intern() ;
  public static String emailAddress = "emailAddress".intern() ;
  public static String jobTitle = "jobTitle".intern() ;
  private boolean isSearchResult = false ;
  
  public UIContacts() throws Exception { } 
  public String[] getActions() { return new String[] {"Cancel"} ; }
  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
  
  protected boolean isDisplaySearchResult() {return isSearchResult ;}
  public void setDisplaySearchResult(boolean search) {isSearchResult = search ;}
  
  public void setAscending(boolean isAsc) { isAscending_ = isAsc ; }
  public boolean isAscending() {return isAscending_ ; }
  public void setSortedBy(String s) { sortedBy_ = s ; }
  public String getSortedBy() { return sortedBy_ ; }
  public String getViewQuery() {return viewQuery_ ; }
  public void setViewQuery(String view) {viewQuery_ = view ;}
  
  public void setContacts(JCRPageList pageList) throws Exception {
    pageList_ = pageList ;
    updateList() ;
  }
  public JCRPageList getContactPageList() { return pageList_ ; }
  
  public Contact[] getContacts() throws Exception { 
    return contactMap.values().toArray(new Contact[]{}) ;
  }
  
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
      
      Contact contact = uiContacts.contactMap.get(contactId) ;
      if (contact != null && contact.getCategories().length > 0) uiCategorySelect.setValue(contact.getCategories()[0]) ;
      uiCategorySelect.disableSelect() ;
      UIContactForm uiContactForm = popupContainer.findFirstComponentOfType(UIContactForm.class);
      uiContactForm.setValues(contact);
      UIContactForm.isNew_ = false ;
      popupAction.activate(popupContainer, 800, 0, true) ;
    }
  } 
  
  static public class InstantMessageActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      UIApplication uiApp = uiContacts.getAncestorOfType(UIApplication.class) ;
      uiApp.addMessage(new ApplicationMessage("UIContacts.msg.not-already", null)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      return ;
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
  
  static public class ExportContactActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      String contactId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIContactPortlet uiContactPortlet = uiContacts.getAncestorOfType(UIContactPortlet.class);
      UIPopupAction uiPopupAction = uiContactPortlet.getChild(UIPopupAction.class);
      UIExportForm uiExportForm = uiPopupAction.createUIComponent(UIExportForm.class, null,"ExportForm");
      uiExportForm.setSelectedGroup(uiContacts.selectedGroup) ;
      uiExportForm.setSelectedTag(uiContacts.selectedTag_) ;
      Contact contact = uiContacts.contactMap.get(contactId) ;
      uiExportForm.setContacts(new Contact[] { contact }) ;
      uiExportForm.updateList();
      uiExportForm.getUIFormCheckBoxInput(contact.getId()).setChecked(true) ;
      uiPopupAction.activate(uiExportForm, 500, 0, true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
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
    @SuppressWarnings("unchecked")
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource() ;
      String sortedBy = event.getRequestContext().getRequestParameter(OBJECTID) ;
      ContactService contactService = ContactUtils.getContactService() ;
      String username = ContactUtils.getCurrentUser() ;
      uiContacts.setAscending(!uiContacts.isAscending_);
      uiContacts.setSortedBy(sortedBy);

      JCRPageList pageList = null ;
      String group = uiContacts.selectedGroup ;
      if (!ContactUtils.isEmpty(group)) {
        ContactFilter filter = new ContactFilter() ;
        filter.setViewQuery(uiContacts.getViewQuery());        
        filter.setAscending(uiContacts.isAscending_);
        filter.setOrderBy(uiContacts.getSortedBy());
        filter.setCategories(new String[] { group } ) ;
        pageList = contactService.getContactPageListByGroup(username, filter, ContactUtils.isPublicGroup(group)) ;
      } else if (!ContactUtils.isEmpty(uiContacts.getSelectedTag())) {
          pageList = uiContacts.pageList_ ;
          List<Contact> contacts = new ArrayList<Contact>() ;
          contacts = pageList.getAll() ;
          if (uiContacts.getSortedBy().equals(UIContacts.fullName)) {
            FullNameComparator.isAsc = (!FullNameComparator.isAsc) ;
            Collections.sort(contacts, new FullNameComparator()) ;
          } else if (uiContacts.getSortedBy().equals(UIContacts.emailAddress)) {
            EmailComparator.isAsc = (!EmailComparator.isAsc) ;
            Collections.sort(contacts, new EmailComparator()) ;
          } else if (uiContacts.getSortedBy().equals(UIContacts.jobTitle)) {
            JobTitleComparator.isAsc = (!JobTitleComparator.isAsc) ;
            Collections.sort(contacts, new JobTitleComparator()) ;
          }  
         pageList.setList(contacts) ;
      }
      uiContacts.setContacts(pageList) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent());
    }
  }
  
  static public class FullNameComparator implements Comparator {
    public static boolean isAsc ;
    public int compare(Object o1, Object o2) throws ClassCastException {
      String name1 = ((Contact) o1).getFullName() ;
      String name2 = ((Contact) o2).getFullName() ;
      if (ContactUtils.isEmpty(name1)) name1 = "" ;
      if (ContactUtils.isEmpty(name2)) name2 = "" ;
      if (isAsc == true) return name1.compareToIgnoreCase(name2) ;
      else return name2.compareToIgnoreCase(name1) ;
    }
  }
  static public class EmailComparator implements Comparator {
    public static boolean isAsc ;
    public int compare(Object o1, Object o2) throws ClassCastException {
      String email1 = ((Contact) o1).getEmailAddress() ;
      String email2 = ((Contact) o2).getEmailAddress() ;
      if (ContactUtils.isEmpty(email1)) email1 = "" ;
      if (ContactUtils.isEmpty(email2)) email2 = "" ;
      if (isAsc == true) return email1.compareToIgnoreCase(email2) ;
      else return email2.compareToIgnoreCase(email1) ;
    }
  }
  static public class JobTitleComparator implements Comparator {
    public static boolean isAsc ;
    public int compare(Object o1, Object o2) throws ClassCastException {
      String job1 = ((Contact) o1).getJobTitle() ;
      String job2 = ((Contact) o2).getJobTitle() ;
      if (ContactUtils.isEmpty(job1)) job1 = "" ;
      if (ContactUtils.isEmpty(job2)) job2 = "" ;
      if (isAsc == true) return job1.compareToIgnoreCase(job2) ;
      else return job2.compareToIgnoreCase(job1) ;
    }
  }
  
  static public class CloseSearchActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource() ;
      uiContacts.setDisplaySearchResult(false) ;
      ContactService contactService = ContactUtils.getContactService();
      String username = ContactUtils.getCurrentUser() ;
      String group = uiContacts.selectedGroup ;  
      UIWorkingContainer uiWorkingContainer = uiContacts.getAncestorOfType(UIWorkingContainer.class) ;
      UIAddressBooks addressBooks = uiWorkingContainer.findFirstComponentOfType(UIAddressBooks.class) ;
      UITags uiTags = uiWorkingContainer.findFirstComponentOfType(UITags.class) ;
      if (!ContactUtils.isEmpty(group)) {        
        addressBooks.setSelectedGroup(group) ;
        if (ContactUtils.isPublicGroup(group)) {
          uiContacts.setContacts(contactService.getSharedContactsByGroup(group)); 
        } else {
          uiContacts.setContacts(contactService.getContactPageListByGroup(username, group));
        }
      } else if (!ContactUtils.isEmpty(uiContacts.selectedTag_)) {
        uiTags.setSelectedTag(uiContacts.selectedTag_) ;
        uiContacts.setContacts(ContactUtils.getContactService()
          .getContactPageListByTag(username, uiContacts.selectedTag_)) ;
      }      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingContainer) ;
    }
  }
  
}
