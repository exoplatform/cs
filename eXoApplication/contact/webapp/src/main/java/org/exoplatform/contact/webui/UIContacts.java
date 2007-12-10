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
import java.util.Map;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.SessionsUtils;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactFilter;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.JCRPageList;
import org.exoplatform.contact.service.Tag;
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
        @EventConfig(listeners = UIContacts.DNDContactsActionListener.class),
        @EventConfig(listeners = UIContacts.DNDContactsToTagActionListener.class),
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
        //@EventConfig(listeners = UIContacts.TagInfoActionListener.class),
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
  public void setDisplaySearchResult(boolean search) { isSearchResult = search ; }
  
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
  
  public boolean isAscName() { return FullNameComparator.isAsc ; }
  public boolean isAscEmail() { return EmailComparator.isAsc ; }
  public boolean isAscJob() { return JobTitleComparator.isAsc ; }
  
  public void updateList() throws Exception {
    getChildren().clear() ;
    contactMap.clear();
    UIContactPreview contactPreview = 
      getAncestorOfType(UIContactContainer.class).getChild(UIContactPreview.class) ;
    if(pageList_ != null) {
      List<Contact> contactList = pageList_.getPage(pageList_.getCurrentPage(),ContactUtils.getCurrentUser()) ;
      if(contactList.size() == 0 && pageList_.getCurrentPage() > 1)
        contactList = pageList_.getPage(pageList_.getCurrentPage() - 1,ContactUtils.getCurrentUser()) ;
      for(Contact contact : contactList) {
        UIFormCheckBoxInput<Boolean> checkbox = new UIFormCheckBoxInput<Boolean>(contact.getId(),contact.getId(), false) ;
        addUIFormInput(checkbox);
        contactMap.put(contact.getId(), contact) ; 
      }
      Contact[] array = contactMap.values().toArray(new Contact[]{}) ;
      if (array.length > 0) {
        Contact firstContact = array[0] ;        
        contactPreview.setContact(firstContact) ;
        selectedContact = firstContact.getId() ;
      } else contactPreview.setContact(null) ;
    } else contactPreview.setContact(null) ;
  }
  
  public Contact[] getContacts() throws Exception {  
    return contactMap.values().toArray(new Contact[]{}) ;
  }
  public LinkedHashMap<String, Contact> getContactMap() { return contactMap ;}
  
  public void setSelectedContact(String s) { selectedContact = s ; }
  public String getSelectedContact() { return selectedContact ; }
  
  public void setSelectedGroup(String s) { selectedGroup = s ; }
  public String getSelectedGroup() { return selectedGroup ; }
  
  public void setViewContactsList(boolean list) { viewContactsList = list ; }
  public boolean getViewContactsList() {
    if (viewContactsList) {
      getAncestorOfType(UIContactContainer.class).getChild(UIContactPreview.class).setRendered(true) ;
    } else {
      getAncestorOfType(UIContactContainer.class).getChild(UIContactPreview.class).setRendered(false) ;
    }
    return viewContactsList ; 
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
  
  public Map<String, Tag> getTagMap() {
    return getAncestorOfType(UIWorkingContainer.class)
      .findFirstComponentOfType(UITags.class).getTagMap() ;
  }
  
  public Map<String, String> getPrivateGroupMap() {
    return getAncestorOfType(UIWorkingContainer.class)
      .findFirstComponentOfType(UIAddressBooks.class).getPrivateGroupMap() ;
  }
  public Map<String, String> getPublicGroupMap() {
    return getAncestorOfType(UIWorkingContainer.class)
      .findFirstComponentOfType(UIAddressBooks.class).getPublicGroupMap() ;
  }
  
  static public class EditContactActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      String contactId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIContactPortlet contactPortlet = uiContacts.getAncestorOfType(UIContactPortlet.class) ;
      UIPopupAction popupAction = contactPortlet.getChild(UIPopupAction.class) ;
      UIPopupContainer popupContainer =  popupAction.activate(UIPopupContainer.class, 800) ;
      popupContainer.setId("AddNewContact");
      UICategorySelect uiCategorySelect = popupContainer.addChild(UICategorySelect.class, null, null) ;
      UIContactForm uiContactForm = popupContainer.addChild(UIContactForm.class, null, null) ;      
      Contact contact = uiContacts.contactMap.get(contactId) ;
      if (contact != null && contact.getCategories().length > 0){
        uiCategorySelect.setValue(contact.getCategories()[0]) ;
        uiCategorySelect.disableSelect() ;
        uiContactForm.setValues(contact);
        uiContactForm.setNew(false) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent()) ;
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
      UITagForm uiTagForm = uiContacts.createUIComponent(UITagForm.class, null, "UITagForm") ;
      List<Contact> contacts = new ArrayList<Contact>() ;
      for (String id : contactIds) { contacts.add(uiContacts.contactMap.get(id)) ; }
      uiTagForm.setContacts(contacts) ;
      popupAction.activate(uiTagForm, 600, 0, true) ;
      uiTagForm.update() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  static public class DNDContactsToTagActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      String tagId = event.getRequestContext().getRequestParameter(OBJECTID);   
      String type = event.getRequestContext().getRequestParameter("contactType");
      List<String> contactIds = new ArrayList<String>();
      UIApplication uiApp = uiContacts.getAncestorOfType(UIApplication.class) ;
      contactIds = uiContacts.getCheckedContacts() ;
      ContactService contactService = ContactUtils.getContactService(); 
      contactService.addTag(SessionsUtils.getSystemProvider(), ContactUtils.getCurrentUser(), contactIds, tagId);
      uiContacts.updateList() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent()) ;
    }
  }
  
  static public class MoveContactsActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      if (!ContactUtils.isEmpty(uiContacts.selectedGroup)) {
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
        UIContactPortlet uiContactPortlet = uiContacts.getAncestorOfType(UIContactPortlet.class) ;
        UIPopupAction popupAction = uiContactPortlet.getChild(UIPopupAction.class) ;
        UIMoveContactsForm uiMoveForm = popupAction.createUIComponent(UIMoveContactsForm.class, null, null) ;
        uiMoveForm.setContacts(contactIds) ;
        
        UIAddressBooks addressBooks = uiContactPortlet.findFirstComponentOfType(UIAddressBooks.class) ;
        List<String> sharedGroup = addressBooks.getSharedContactGroups() ;
        if (sharedGroup.contains(uiContacts.selectedGroup)) {
          uiMoveForm.addComponent() ;
          uiMoveForm.setPersonal(false) ;
          
          if (contactIds.size() == 1) {
            String[] categories = uiContacts.contactMap.get(contactIds.get(0)).getCategories() ;
            for (String category : categories) {
              UIFormCheckBoxInput check = uiMoveForm.getUIFormCheckBoxInput(category) ;
              if (check != null) check.setChecked(true) ;
            }  
          }
        } else {
          uiMoveForm.setPrivateGroupMap(addressBooks.getPrivateGroupMap()) ;
          uiMoveForm.setPersonal(true) ;
          uiMoveForm.setGroup(uiContacts.selectedGroup) ;
        }
        
        popupAction.activate(uiMoveForm, 410, 0, true) ;
        event.getRequestContext()
        .addUIComponentToUpdateByAjax(uiContactPortlet.findFirstComponentOfType(UIContactContainer.class));
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ; 
      }
    }
  }
  
  static public class DNDContactsActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      String addressBookId = event.getRequestContext().getRequestParameter(OBJECTID);   
      String type = event.getRequestContext().getRequestParameter("contactType");
      String[] addressBooks = {addressBookId} ;
      List<String> contactIds = new ArrayList<String>();
      UIApplication uiApp = uiContacts.getAncestorOfType(UIApplication.class) ;
      contactIds = uiContacts.getCheckedContacts() ;
      ContactService contactService = ContactUtils.getContactService(); 
      contactService.moveContacts(SessionsUtils.getSystemProvider(), ContactUtils.getCurrentUser(), contactIds, addressBooks) ;
      uiContacts.updateList() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent()) ;
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
      ContactService contactService = ContactUtils.getContactService() ;
      String username = ContactUtils.getCurrentUser() ;
      
      contactService.removeContacts(SessionsUtils.getSystemProvider(), username, contactIds) ;
      
//      if(contactIds.contains(uiContactPreview.getContact().getId())) 
//        uiContactPreview.setContact(null) ;
    
      if (uiContacts.getSelectedTag() != null) {
        String tagName = uiWorkingContainer.findFirstComponentOfType(UITags.class).getSelectedTag() ;
        uiContacts.setContacts(contactService.getContactPageListByTag(SessionsUtils.getSystemProvider(), username, tagName)) ;
      } else {
        uiContacts.updateList() ;
      }
//      if (uiContacts.isDisplaySearchResult()) {
//        for (String contact : contactIds)
//          uiContacts.pageList_.getAll().remove(contact) ;        
//      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingContainer) ;
    }
  } 
  
  static public class ExportContactActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      String contactId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIContactPortlet uiContactPortlet = uiContacts.getAncestorOfType(UIContactPortlet.class);
      UIPopupAction uiPopupAction = uiContactPortlet.getChild(UIPopupAction.class);
      UIExportForm uiExportForm = uiPopupAction.createUIComponent(UIExportForm.class, null,"ExportForm");
      
      //uiExportForm.setSelectedGroup(uiContacts.selectedGroup) ;
      //uiExportForm.setSelectedTag(uiContacts.selectedTag_) ;
      Contact contact = uiContacts.contactMap.get(contactId) ;
      uiExportForm.setContacts(new Contact[] { contact }) ;
      uiExportForm.updateList();
      //uiExportForm.getUIFormCheckBoxInput(contact.getId()).setChecked(true) ;
      
      uiPopupAction.activate(uiExportForm, 500, 0, true);
      event.getRequestContext()
        .addUIComponentToUpdateByAjax(uiContactPortlet.findFirstComponentOfType(UIContactContainer.class));
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
      uiContactPreview.setRendered(true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContactContainer);   
    }
  }
  
  static public class ViewDetailsActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      String contactId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIContactPortlet contactPortlet = uiContacts.getAncestorOfType(UIContactPortlet.class) ;
      UIPopupAction popupAction = contactPortlet.getChild(UIPopupAction.class) ;
      UIContactPreviewForm uiContactPreviewForm = popupAction
        .createUIComponent(UIContactPreviewForm.class, null, "UIContactPreviewForm") ; 
      uiContactPreviewForm.setContact(uiContacts.contactMap.get(contactId)) ;
      popupAction.activate(uiContactPreviewForm, 700, 0, true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;  
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent()) ;
    }
  } 
  
  static public class FirstPageActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource() ; 
      JCRPageList pageList = uiContacts.getContactPageList();
      if (pageList != null) {
        uiContacts.setPageList(pageList, 1) ;
        uiContacts.updateList() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent());
      }
    }
  }
  
  static public class PreviousPageActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource() ; 
      JCRPageList pageList = uiContacts.getContactPageList(); 
      if (pageList != null && pageList.getCurrentPage() > 1){
        uiContacts.setPageList(pageList, pageList.getCurrentPage() - 1);
        uiContacts.updateList() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent());
      }      
    }
  }
  
  static public class NextPageActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource() ; 
      JCRPageList pageList = uiContacts.getContactPageList() ; 
      if (pageList != null && pageList.getCurrentPage() < pageList.getAvailablePage()){
        uiContacts.setPageList(pageList, pageList.getCurrentPage() + 1);
        uiContacts.updateList() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent());
      }      
    }
  }
  
  static public class LastPageActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource() ; 
      JCRPageList pageList = uiContacts.getContactPageList(); 
      if (pageList != null) {
        uiContacts.setPageList(pageList, pageList.getAvailablePage());
        uiContacts.updateList() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent());
      }      
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
        boolean isPublic = ContactUtils.isPublicGroup(group) ;
        if(isPublic){
        	pageList = ContactUtils.getContactService().getContactPageListByGroup(SessionsUtils.getSystemProvider(), 
              ContactUtils.getCurrentUser(), filter, isPublic) ;
        }else {
        	pageList = ContactUtils.getContactService().getContactPageListByGroup(SessionsUtils.getSessionProvider(), 
              ContactUtils.getCurrentUser(), filter, isPublic) ;
        }
        
      } else {      //if (!ContactUtils.isEmpty(uiContacts.getSelectedTag())) {
          pageList = uiContacts.pageList_ ;
          if (pageList != null) {
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
      }
      uiContacts.setContacts(pageList) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent());
    }
  }
  
  static public class FullNameComparator implements Comparator {
    private static boolean isAsc ;
    public int compare(Object o1, Object o2) throws ClassCastException {
      String name1 = ((Contact) o1).getFullName() ;
      String name2 = ((Contact) o2).getFullName() ;
      if (isAsc == true) return name1.compareToIgnoreCase(name2) ;
      else return name2.compareToIgnoreCase(name1) ;
    }
  }
  static public class EmailComparator implements Comparator {
    private static boolean isAsc ;
    public int compare(Object o1, Object o2) throws ClassCastException {
      String email1 = ((Contact) o1).getEmailAddress() ;
      String email2 = ((Contact) o2).getEmailAddress() ;
      if (ContactUtils.isEmpty(email1) || ContactUtils.isEmpty(email2)) return 0 ;
      if (isAsc == true) return email1.compareToIgnoreCase(email2) ;
      else return email2.compareToIgnoreCase(email1) ;
    }
  }
  static public class JobTitleComparator implements Comparator {
    private static boolean isAsc ;
    public int compare(Object o1, Object o2) throws ClassCastException {
      String job1 = ((Contact) o1).getJobTitle() ;
      String job2 = ((Contact) o2).getJobTitle() ;
      if (ContactUtils.isEmpty(job1) || ContactUtils.isEmpty(job2)) return 0 ;
      if (isAsc == true) return job1.compareToIgnoreCase(job2) ;
      else return job2.compareToIgnoreCase(job1) ;
    }
  }
  
  static public class CloseSearchActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource() ;
      uiContacts.setDisplaySearchResult(false) ;
      uiContacts.setContacts(null) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent()) ;
      
      /*
      ContactService contactService = ContactUtils.getContactService();
      String username = ContactUtils.getCurrentUser() ;
      String group = uiContacts.selectedGroup ;  
      UIWorkingContainer uiWorkingContainer = uiContacts.getAncestorOfType(UIWorkingContainer.class) ;
      UIAddressBooks addressBooks = uiWorkingContainer.findFirstComponentOfType(UIAddressBooks.class) ;
      UITags uiTags = uiWorkingContainer.findFirstComponentOfType(UITags.class) ;
      if (!ContactUtils.isEmpty(group)) {        
        addressBooks.setSelectedGroup(group) ;
        if (ContactUtils.isPublicGroup(group)) {
          uiContacts.setContacts(contactService.getSharedContactsByGroup(SessionsUtils.getSystemProvider(), group)); 
        } else {
          uiContacts.setContacts(contactService.getContactPageListByGroup(SessionsUtils.getSessionProvider(), username, group));
        }
      } else if (!ContactUtils.isEmpty(uiContacts.selectedTag_)) {
        uiTags.setSelectedTag(uiContacts.selectedTag_) ;
        uiContacts.setContacts(ContactUtils.getContactService()
          .getContactPageListByTag(SessionsUtils.getSystemProvider(), username, uiContacts.selectedTag_)) ;
      } else {
        uiContacts.setContacts(null) ;
      } 
      
      uiContacts.setViewContactsList(uiContacts.getListBeforeSearch()) ;
      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingContainer) ;
>>>>>>> .r22506
       */
    }
  }
  /*
  static public class TagInfoActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource() ;
      String contactId = event.getRequestContext().getRequestParameter(OBJECTID);
      Contact contact = uiContacts.contactMap.get(contactId) ;
      String[] tagIds = contact.getTags() ;
      List<Tag> tags = new ArrayList<Tag>() ;
      Map<String, Tag> tagMap = uiContacts.getTagMap() ;
      for (String tagId : tagIds) tags.add(tagMap.get(tagId)) ;
      UIContactPortlet contactPortlet = uiContacts.getAncestorOfType(UIContactPortlet.class) ;
      UIPopupAction popupAction = contactPortlet.getChild(UIPopupAction.class) ;
      UITagInfo uiTagInfo = popupAction.createUIComponent(UITagInfo.class, null, "UITagInfo") ;
      uiTagInfo.setTags(tags) ;
      popupAction.activate(uiTagInfo, 400, 0, true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;  
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent()) ;
    }
  }
  */
}
