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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.PathNotFoundException;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactFilter;
import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.DataPageList;
import org.exoplatform.contact.service.JCRPageList;
import org.exoplatform.contact.service.SharedAddressBook;
import org.exoplatform.contact.service.Tag;
import org.exoplatform.contact.service.impl.JCRDataStorage;
import org.exoplatform.contact.service.impl.NewUserListener;
import org.exoplatform.contact.webui.popup.UIAddEditPermission;
import org.exoplatform.contact.webui.popup.UIComposeForm;
import org.exoplatform.contact.webui.popup.UIContactPreviewForm;
import org.exoplatform.contact.webui.popup.UIExportForm;
import org.exoplatform.contact.webui.popup.UIMoveContactsForm;
import org.exoplatform.contact.webui.popup.UIPopupComponent;
import org.exoplatform.contact.webui.popup.UISharedContactsForm;
import org.exoplatform.contact.webui.popup.UITagForm;
import org.exoplatform.contact.webui.popup.UIContactForm;
import org.exoplatform.contact.webui.popup.UIPopupAction;
import org.exoplatform.contact.webui.popup.UIPopupContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.mail.service.Account;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
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
        @EventConfig(listeners = UIContacts.SendEmailActionListener.class),
        @EventConfig(listeners = UIContacts.TagActionListener.class),
        @EventConfig(listeners = UIContacts.MoveContactsActionListener.class),
        @EventConfig(listeners = UIContacts.DNDContactsActionListener.class),
        @EventConfig(listeners = UIContacts.DNDContactsToTagActionListener.class),
        @EventConfig(listeners = UIContacts.DeleteContactsActionListener.class
            , confirm = "UIContacts.msg.confirm-delete"),
        @EventConfig(listeners = UIContacts.SelectedContactActionListener.class), 
        @EventConfig(listeners = UIContacts.CopyContactActionListener.class),
        @EventConfig(listeners = UIContacts.ViewDetailsActionListener.class),
        @EventConfig(listeners = UIContacts.SortActionListener.class),
        @EventConfig(listeners = UIContacts.FirstPageActionListener.class),
        @EventConfig(listeners = UIContacts.PreviousPageActionListener.class),
        @EventConfig(listeners = UIContacts.NextPageActionListener.class),
        @EventConfig(listeners = UIContacts.LastPageActionListener.class),
        @EventConfig(listeners = UIContacts.ExportContactActionListener.class),
        @EventConfig(listeners = UIContacts.CancelActionListener.class),
        @EventConfig(listeners = UIContacts.SelectTagActionListener.class),
        @EventConfig(listeners = UIContacts.SharedContactsActionListener.class),
        @EventConfig(listeners = UIContacts.CloseSearchActionListener.class),
        @EventConfig(listeners = UIContacts.PrintActionListener.class), 
//        @EventConfig(listeners = UIContacts.ChatActionListener.class),
        @EventConfig(listeners = UIContacts.RefreshActionListener.class),
        @EventConfig(listeners = UIContacts.PrintDetailsActionListener.class)
    }
)

public class UIContacts extends UIForm implements UIPopupComponent {
  public boolean viewContactsList = true ;
  public boolean viewListBeforePrint = false ;
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
  private boolean defaultNameSorted = true ;
  private boolean isPrintForm = false ;
  @SuppressWarnings("unused")
  private boolean isPrintDetail = false ;
  private boolean isSelectSharedContacts = false ;
  private List<Contact> listBeforePrint = new ArrayList<Contact>() ; 
  
  public UIContacts() throws Exception { } 
  
  public void setListBeforePrint(List<Contact> contacts) { listBeforePrint = contacts ; }  
  public String[] getActions() { return new String[] {"Cancel"} ; }
  public void activate() throws Exception { }
  public void deActivate() throws Exception { } 

  public boolean canChat() {
    try {
      java.lang.Class.forName("org.exoplatform.services.xmpp.rest.RESTXMPPService") ;
      return true ;
    } catch (ClassNotFoundException e) {
      return false ;
    } catch (Exception ex) {
      ex.printStackTrace() ;
      return false ;
    }
  }
  
  // only called when refresh brower
  @SuppressWarnings({ "unchecked", "unused" })
  private void refreshData() throws Exception {
    if (isDisplaySearchResult() || isPrintForm) return ;
    if (selectedGroup != null) {
      if (getPrivateGroupMap().containsKey(selectedGroup)) {
        setContacts(ContactUtils.getContactService().getContactPageListByGroup(
            SessionProviderFactory.createSessionProvider(), ContactUtils.getCurrentUser(), selectedGroup));
      } else if (ContactUtils.getUserGroups().contains(selectedGroup)) {
        setContacts(ContactUtils.getContactService()
            .getPublicContactsByAddressBook(SessionProviderFactory.createSystemProvider(), selectedGroup));
      } else {
        UIAddressBooks uiAddressBooks = getAncestorOfType(
            UIWorkingContainer.class).findFirstComponentOfType(UIAddressBooks.class) ;       
        setContacts(ContactUtils.getContactService().getSharedContactsByAddressBook(SessionProviderFactory
            .createSystemProvider(),ContactUtils.getCurrentUser(), uiAddressBooks.getSharedGroups().get(selectedGroup))); 
      }
    } else if (selectedTag_ != null) {
      DataPageList pageList =ContactUtils.getContactService().getContactPageListByTag(
          SessionProviderFactory.createSystemProvider(), ContactUtils.getCurrentUser(), selectedTag_) ;
      if (pageList != null) {
        List<Contact> contacts = new ArrayList<Contact>() ;
        contacts = pageList.getAll() ;
        if (getSortedBy().equals(UIContacts.fullName)) {
          Collections.sort(contacts, new FullNameComparator()) ;
        } else if (getSortedBy().equals(UIContacts.emailAddress)) {
          Collections.sort(contacts, new EmailComparator()) ;
        } else if (getSortedBy().equals(UIContacts.jobTitle)) {
          Collections.sort(contacts, new JobTitleComparator()) ;
        }
        pageList.setList(contacts) ;     
      }
      setContacts(pageList) ;
    } else if (isSelectSharedContacts) {
      setContacts(ContactUtils.getContactService().getSharedContacts( ContactUtils.getCurrentUser())); 
    }
  }
  
  public void setSelectSharedContacts(boolean selected) { isSelectSharedContacts = selected ; }
  public boolean isSelectSharedContacts() { return isSelectSharedContacts ; }
  public boolean havePermission(Contact contact) throws Exception {
    if (!contact.getContactType().equals(JCRDataStorage.SHARED)) return true ;
    // contact shared
    String currentUser = ContactUtils.getCurrentUser() ;
    if (contact.getEditPermissionUsers() != null &&
        Arrays.asList(contact.getEditPermissionUsers()).contains(currentUser + JCRDataStorage.HYPHEN)) {
      return true ;
    }
    String[] editPerGroups = contact.getEditPermissionGroups() ;
    if (editPerGroups != null)
      for (String editPer : editPerGroups)
        if (ContactUtils.getUserGroups().contains(editPer)) {
          return true ;
        }    
    Map<String, SharedAddressBook> sharedGroupMap = getAncestorOfType(UIWorkingContainer.class)
        .findFirstComponentOfType(UIAddressBooks.class).getSharedGroups() ;
    for (String address : contact.getAddressBook()) {
      try {
        SharedAddressBook add = sharedGroupMap.get(address) ;
        if (add.getEditPermissionUsers() != null &&
            Arrays.asList(add.getEditPermissionUsers()).contains(currentUser + JCRDataStorage.HYPHEN)) {
          return true ;
        }
        editPerGroups = add.getEditPermissionGroups() ;
        if (editPerGroups != null)
          for (String editPer : editPerGroups)
            if (ContactUtils.getUserGroups().contains(editPer)) {
              return true ;
            }
      } catch (NullPointerException e) { return false ; }
    }
    return false ;    
  }
  
  public boolean isSharedAddress(String[] adds) throws Exception {
    //if (isSelectSharedContacts) return false ;
    for (String add : adds)
      if (getSharedGroupMap().containsKey(add)) return true ;
    return false ;
  }
  
  public void setPrintForm(boolean isPrint) { isPrintForm = isPrint ; }
  public boolean isPrintForm() { return isPrintForm ; }
  public void setPrintDetail(boolean isDetail) { isPrintDetail = isDetail ; }
  
  public boolean isDisplaySearchResult() {return isSearchResult ;}
  public void setDisplaySearchResult(boolean search) { isSearchResult = search ; }
  public void setViewListBeforePrint(boolean isList) { viewListBeforePrint = isList ; }
  
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
  public void setDefaultNameSorted(boolean name) { defaultNameSorted = name ; }
  public boolean isNameSorted() { return defaultNameSorted ; }
  
  public void setContact(List<Contact> contacts, boolean isUpdate) throws Exception{
    pageList_.setContact(contacts, isUpdate) ;
  }
  public void updateList() throws Exception {
    getChildren().clear() ;
    contactMap.clear();
    UIContactPreview contactPreview = 
      getAncestorOfType(UIContactContainer.class).getChild(UIContactPreview.class) ;    
    if(pageList_ != null) {
      List<Contact> contactList = pageList_.getPage(pageList_.getCurrentPage(),ContactUtils.getCurrentUser()) ;
      if(contactList.size() == 0 && pageList_.getCurrentPage() > 1) {
        contactList = pageList_.getPage(pageList_.getCurrentPage() - 1,ContactUtils.getCurrentUser()) ;
      }
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
    /*
    if (isSelectSharedContacts && !isPrintForm) {
      setContacts(ContactUtils.getContactService().getSharedContacts( ContactUtils.getCurrentUser())); 
    } else {
      UIAddressBooks uiAddressBooks = getAncestorOfType(
          UIWorkingContainer.class).findFirstComponentOfType(UIAddressBooks.class) ;
      if (!ContactUtils.isEmpty(selectedGroup)) {
        if (!uiAddressBooks.getPrivateGroupMap().containsKey(selectedGroup) && !uiAddressBooks.getSharedGroups().containsKey(selectedGroup)
            && !ContactUtils.getUserGroups().contains(selectedGroup)) setContacts(null) ;
        else if (uiAddressBooks.getSharedGroups().containsKey(selectedGroup) && !isPrintForm) {
          setContacts(ContactUtils.getContactService().getSharedContactsByAddressBook(SessionProviderFactory
              .createSystemProvider(),ContactUtils.getCurrentUser(), uiAddressBooks.getSharedGroups().get(selectedGroup)));
        }
      }
    }
    */
    return contactMap.values().toArray(new Contact[]{}) ;
  }
  public LinkedHashMap<String, Contact> getContactMap() { return contactMap ;}
  public void setContactMap(LinkedHashMap<String, Contact> map) { contactMap = map ; }
  
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
    for (String contactId : contactMap.keySet()) {
      UIFormCheckBoxInput uiCheckBox = getChildById(contactId) ;
      if(uiCheckBox != null && uiCheckBox.isChecked()) {
        checkedContacts.add(contactId) ;
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
  public Map<String, SharedAddressBook> getSharedGroupMap() throws Exception {
    return getAncestorOfType(UIWorkingContainer.class)
      .findFirstComponentOfType(UIAddressBooks.class).getSharedGroups() ;
  }
  public List<String> getPublicContactGroups() throws Exception {
    return Arrays.asList(ContactUtils.getUserGroups().toArray(new String[] {})) ;
  }  
  public String getDefaultGroup() { return NewUserListener.DEFAULTGROUP ;}

  static public class EditContactActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      String contactId = event.getRequestContext().getRequestParameter(OBJECTID);
      Contact contact = uiContacts.contactMap.get(contactId) ;
      UIContactPortlet contactPortlet = uiContacts.getAncestorOfType(UIContactPortlet.class) ;
      UIPopupAction popupAction = contactPortlet.getChild(UIPopupAction.class) ;
      UIPopupContainer popupContainer =  popupAction.activate(UIPopupContainer.class, 800) ;
      popupContainer.setId("AddNewContact");
      UIContactForm uiContactForm = popupContainer.addChild(UIContactForm.class, null, null) ;
      uiContactForm.setValues(contact);
      uiContactForm.setNew(false) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      //event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent()) ;
    }
  }
  
  static public class TagActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource() ;
      String contactId = event.getRequestContext().getRequestParameter(OBJECTID);
      List<String> contactIds = new ArrayList<String>();
      if (!ContactUtils.isEmpty(contactId)) {
        contactIds.add(contactId) ;
      } else {
        contactIds = uiContacts.getCheckedContacts() ;
        if (contactIds.size() == 0) {
          UIApplication uiApp = uiContacts.getAncestorOfType(UIApplication.class) ;
          uiApp.addMessage(new ApplicationMessage("UIContacts.msg.checkContact-toTag", null,
              ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
      }    
      UIContactPortlet contactPortlet = uiContacts.getAncestorOfType(UIContactPortlet.class) ;
      UIPopupAction popupAction = contactPortlet.getChild(UIPopupAction.class) ;
      UITagForm uiTagForm = popupAction.activate(UITagForm.class, 600) ;
      List<Contact> contacts = new ArrayList<Contact>() ;      
      for (String id : contactIds) {
        contacts.add(uiContacts.contactMap.get(id)) ;
      }
      uiTagForm.setContacts(contacts) ;      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  static public class DNDContactsToTagActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      String tagId = event.getRequestContext().getRequestParameter(OBJECTID);   
      @SuppressWarnings("unused")
      String type = event.getRequestContext().getRequestParameter("contactType");
      List<String> contactIds = new ArrayList<String>();
      @SuppressWarnings("unused")
      UIApplication uiApp = uiContacts.getAncestorOfType(UIApplication.class) ;
      contactIds = uiContacts.getCheckedContacts() ;
      List<String> newContactIds = new ArrayList<String>();
      for (String contactId : contactIds) {
        Contact contact = uiContacts.contactMap.get(contactId) ;
        newContactIds.add(contactId + JCRDataStorage.SPLIT + contact.getContactType()) ;
      }
      try {
        ContactUtils.getContactService().addTag(SessionProviderFactory
            .createSessionProvider(), ContactUtils.getCurrentUser(), newContactIds, tagId);
      } catch (PathNotFoundException e) {
        uiApp.addMessage(new ApplicationMessage("UIContacts.msg.contact-deleted", null,
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      
      // when select shared contacts 
      if(ContactUtils.isEmpty(uiContacts.selectedGroup) && ContactUtils.isEmpty(uiContacts.selectedTag_)) {
        List<Contact> contacts = new ArrayList<Contact>() ;
        for (String contactId : contactIds) {
          Contact contact = uiContacts.contactMap.get(contactId) ;
          String[] tags = contact.getTags() ;
          if (tags != null && tags.length > 1) {
            List<String> newTags = new ArrayList<String>() ;
            for (String tag : tags) newTags.add(tag) ;
            newTags.add(tagId) ;
            contact.setTags(newTags.toArray(new String[] {})) ;
          }
          else {
            contact.setTags(new String[] {tagId}) ;
          }
          contacts.add(contact) ;
        }
        uiContacts.setContact(contacts, true) ;
      }
      uiContacts.updateList() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent()) ;
    }
  }
  
  static public class MoveContactsActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      String contactId = event.getRequestContext().getRequestParameter(OBJECTID);
      List<String> contactIds = new ArrayList<String>();
      UIApplication uiApp = uiContacts.getAncestorOfType(UIApplication.class) ;
      
      Map<String, Contact> movedContacts = new HashMap<String, Contact>() ;
      if (!ContactUtils.isEmpty(contactId) && !contactId.equals("null")) {
        contactIds.add(contactId) ;
        movedContacts.put(contactId, uiContacts.contactMap.get(contactId)) ;
      } else {
        contactIds = uiContacts.getCheckedContacts() ;
        if (contactIds.size() == 0) {          
          uiApp.addMessage(new ApplicationMessage("UIContacts.msg.checkContact-toMove", null,
              ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
        
        // need improve
        for (String id : contactIds) {
          Contact contact = uiContacts.contactMap.get(id) ;         
          if (contact.getContactType().equals(JCRDataStorage.PUBLIC) || (contact.getContactType()
              .equals(JCRDataStorage.SHARED)&& uiContacts.isSharedAddress(contact.getAddressBook()) && !uiContacts.havePermission(contact))) {
            uiApp.addMessage(new ApplicationMessage("UIContacts.msg.cannot-move", null
                , ApplicationMessage.WARNING)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return ;
          } else if (contact.isOwner()) {
            uiApp.addMessage(new ApplicationMessage("UIContacts.msg.cannot-move-ownerContact", null
                , ApplicationMessage.WARNING)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return ;
          }
          movedContacts.put(id, contact) ;
        }
      }  
      
      UIContactPortlet uiContactPortlet = uiContacts.getAncestorOfType(UIContactPortlet.class) ;
      UIPopupAction popupAction = uiContactPortlet.getChild(UIPopupAction.class) ;
      UIMoveContactsForm uiMoveForm = popupAction.activate(UIMoveContactsForm.class, 540) ;
      uiMoveForm.setContacts(movedContacts) ;
      UIAddressBooks addressBooks = uiContactPortlet.findFirstComponentOfType(UIAddressBooks.class) ;
      uiMoveForm.setPrivateGroupMap(addressBooks.getPrivateGroupMap()) ;
      uiMoveForm.setSharedGroupMap(addressBooks.getSharedGroups()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent());
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;     
    }
  }
  
  static public class DNDContactsActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      String addressBookId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIAddressBooks uiAddressBooks = uiContacts.getAncestorOfType(
          UIWorkingContainer.class).findFirstComponentOfType(UIAddressBooks.class) ; 
      UIApplication uiApp = uiContacts.getAncestorOfType(UIApplication.class) ;
      ContactService contactService = ContactUtils.getContactService() ;
      String username = ContactUtils.getCurrentUser() ;
      SessionProvider sessionProvider = SessionProviderFactory.createSessionProvider() ;

      if (uiAddressBooks.getSharedGroups().containsKey(addressBookId)) {
        ContactGroup group = contactService.getSharedGroup(username, addressBookId) ;
        if (group.getEditPermissionUsers() == null || 
            !Arrays.asList(group.getEditPermissionUsers()).contains(username + JCRDataStorage.HYPHEN)) {
          boolean canEdit = false ;
          String[] editPerGroups = group.getEditPermissionGroups() ;
          if (editPerGroups != null)
            for (String editPer : editPerGroups)
              if (ContactUtils.getUserGroups().contains(editPer)) canEdit = true ;          
          if (canEdit == false) {
            uiApp.addMessage(new ApplicationMessage("UIContacts.msg.non-permission", null
                , ApplicationMessage.WARNING)) ;
              event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
              event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent()) ;
              return ;
          }
        }
      }
      String type = event.getRequestContext().getRequestParameter("addressType");
      List<String> contactIds = uiContacts.getCheckedContacts() ;
      List<Contact> contacts = new ArrayList<Contact>();
      List<Contact> sharedContacts = new ArrayList<Contact>();
      for(String contactId : contactIds) {
      	Contact contact = uiContacts.contactMap.get(contactId) ;
        if (contact.isOwner()){ 
          uiApp.addMessage(new ApplicationMessage("UIContacts.msg.cannot-move-ownerContact", null
              , ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent()) ;
          return ;          
        } else if (contact.getContactType().equals(JCRDataStorage.PUBLIC)){
          uiApp.addMessage(new ApplicationMessage("UIContacts.msg.cannot-move", null
              , ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent()) ;
          return ;
        } else if (contact.getContactType().equals(JCRDataStorage.SHARED) && uiContacts.isSharedAddress(contact.getAddressBook()) && !uiContacts.havePermission(contact)) {
          uiApp.addMessage(new ApplicationMessage("UIContacts.msg.cannot-move", null
              , ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent()) ;
          return ;          
        } 
      }
      for(String contactId : contactIds) {
        Contact contact = uiContacts.contactMap.get(contactId) ;
        if (contact.getContactType().equals(JCRDataStorage.SHARED)) {
          if (uiContacts.isSharedAddress(contact.getAddressBook())) {
            String addressId = null ;
            for (String add : contact.getAddressBook())
              if (uiContacts.getSharedGroupMap().containsKey(add)) addressId = add ;
            contactService.removeSharedContact(SessionProviderFactory.createSystemProvider(), username, addressId, contactId) ;
          } else {
            try {
              contactService.removeUserShareContact(
                  SessionProviderFactory.createSystemProvider(), contact.getPath(), contact.getId(), username) ;              
            } catch (PathNotFoundException e) {
              uiApp.addMessage(new ApplicationMessage("UIContacts.msg.contact-not-existed", null, 
                  ApplicationMessage.WARNING)) ;
              event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
              return ;              
            }
          }
        }
        contact.setAddressBook(new String[] { addressBookId }) ;
        if (contact.getContactType().equals(JCRDataStorage.SHARED)) sharedContacts.add(contact) ;
        else contacts.add(contact) ;      
      }
      if (sharedContacts.size() > 0 ) {
        contactService.pasteContacts(sessionProvider, username, addressBookId, type, sharedContacts) ;
      }
      if (contacts.size() > 0) {
        try {
          contactService.moveContacts(sessionProvider, username, contacts, type);           
        }  catch (PathNotFoundException e) {
          uiApp.addMessage(new ApplicationMessage("UIContacts.msg.contact-not-existed", null, 
              ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;              
        }
      }

      // update addressbook when search
      if (uiContacts.isSearchResult) {
        for (String contactId : contactIds) {
          uiContacts.contactMap.get(contactId).setContactType(type) ;
        }
      } else if (uiContacts.isSelectSharedContacts  && !ContactUtils.isEmpty(addressBookId)) { //select shared contacts        
        if (contacts.size() > 0) uiContacts.setContact(contacts, false) ;
        if (sharedContacts.size() > 0) uiContacts.setContact(sharedContacts, false) ;
      }
      uiContacts.updateList() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent()) ;
    }
  }
  
  static public class CopyContactActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource() ;
      String contactId = event.getRequestContext().getRequestParameter(OBJECTID);
      List<String> contactIds = new ArrayList<String>() ; 
      if (!ContactUtils.isEmpty(contactId)) {
        contactIds.add(contactId) ;
      } else {
        contactIds =  uiContacts.getCheckedContacts() ;
        if (contactIds.size() < 1) {
          UIApplication uiApp = uiContacts.getAncestorOfType(UIApplication.class) ;
          uiApp.addMessage(new ApplicationMessage("UIContacts.msg.checkContact-toCopy", null,
              ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;        
        }
      }
      UIAddressBooks uiAddressBooks = uiContacts.getAncestorOfType(
          UIWorkingContainer.class).findFirstComponentOfType(UIAddressBooks.class) ;     
      uiAddressBooks.setCopyAddress(null) ;
      List<Contact> copyContacts = new ArrayList<Contact>();
      for (String id : contactIds)
        copyContacts.add(uiContacts.contactMap.get(id)) ;
      uiAddressBooks.setCopyContacts(copyContacts) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressBooks) ;
    }
  }
  
  
  static public class DeleteContactsActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      String contactId = event.getRequestContext().getRequestParameter(OBJECTID);
      List<String> contactIds = new ArrayList<String>();
      UIApplication uiApp = uiContacts.getAncestorOfType(UIApplication.class) ;
      if (!ContactUtils.isEmpty(contactId) && !contactId.toString().equals("null")) {
        contactIds.add(contactId) ;
      } else {
        contactIds = uiContacts.getCheckedContacts() ;
        if (contactIds.size() == 0) {          
          uiApp.addMessage(new ApplicationMessage("UIContacts.msg.checkContact-toDelete", null,
              ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
      }
      for (String id : contactIds) {
        Contact contact = uiContacts.contactMap.get(id) ;
        if (contact.isOwner()) {
          uiApp.addMessage(new ApplicationMessage("UIContacts.msg.cannot-delete-ownerContact", null
              , ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent()) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        } else if (contact.getContactType().equals(JCRDataStorage.PUBLIC) ||(contact.getContactType().equals
            (JCRDataStorage.SHARED) && uiContacts.isSharedAddress(contact.getAddressBook()) && !uiContacts.havePermission(contact))) {
          uiApp.addMessage(new ApplicationMessage("UIContacts.msg.cannot-delete", null
              , ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent()) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
      }
      UIWorkingContainer uiWorkingContainer = uiContacts.getAncestorOfType(UIWorkingContainer.class) ;
      ContactService contactService = ContactUtils.getContactService() ;
      String username = ContactUtils.getCurrentUser() ;
      List <Contact> removedContacts = new ArrayList<Contact>() ;
      
      // remove shared contacts
      for (String id : contactIds) {
        Contact contact = uiContacts.contactMap.get(id) ;    
        if (contact.getContactType().equals(JCRDataStorage.SHARED)) {
          if (uiContacts.isSharedAddress(contact.getAddressBook())) {
            String addressBookId = null ;
            for (String add : contact.getAddressBook())
              if (uiContacts.getSharedGroupMap().containsKey(add)) addressBookId = add ;
            contactService.removeSharedContact(SessionProviderFactory.createSystemProvider(), username, addressBookId, contactId) ;
          } else {
            contactService.removeUserShareContact(
                SessionProviderFactory.createSystemProvider(), contact.getPath(), id, username) ;
          }
          removedContacts.add(contact) ;
        }
      }
      if (!uiContacts.isSelectSharedContacts) {
        removedContacts.addAll(contactService.removeContacts(SessionProviderFactory.createSessionProvider(), username, contactIds)) ;          
      }
      
      if (ContactUtils.isEmpty(uiContacts.selectedGroup) && ContactUtils.isEmpty(uiContacts.selectedTag_)) {
        uiContacts.setContact(removedContacts, false) ;
      }
      if (uiContacts.getSelectedTag() != null) {
        String tagName = uiWorkingContainer.findFirstComponentOfType(UITags.class).getSelectedTag() ;
        uiContacts.setContacts(contactService
            .getContactPageListByTag(SessionProviderFactory.createSystemProvider(), username, tagName)) ;
      } else {
        uiContacts.updateList() ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingContainer) ;
    }
  }  
  
  static public class ExportContactActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      String contactId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIContactPortlet uiContactPortlet = uiContacts.getAncestorOfType(UIContactPortlet.class);
      UIPopupAction uiPopupAction = uiContactPortlet.getChild(UIPopupAction.class);
      UIExportForm uiExportForm = uiPopupAction.activate(UIExportForm.class, 500) ;
      uiExportForm.setId("ExportForm");
      Contact contact = uiContacts.contactMap.get(contactId) ;
      uiExportForm.setContacts(new Contact[] { contact }) ;
      uiExportForm.updateList();
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
      UIPopupContainer uiPopupContainer = popupAction.activate(UIPopupContainer.class, 700) ;
      uiPopupContainer.setId("ContactDetails") ;  
      UIContactPreviewForm uiContactPreviewForm = uiPopupContainer.addChild(UIContactPreviewForm.class, null, null) ; 
      uiContactPreviewForm.setPrintForm(false) ;
      uiContactPreviewForm.setContact(uiContacts.contactMap.get(contactId)) ;
      
      // edited 17-4 
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;  
      //event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent()) ;
    }
  }
  
  static public class PrintDetailsActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      uiContacts.isPrintDetail = true ;
      String contactId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIContactPortlet contactPortlet = uiContacts.getAncestorOfType(UIContactPortlet.class) ;
      UIContactPreviewForm uiPreviewForm = contactPortlet.createUIComponent(UIContactPreviewForm.class, null, null) ;
      uiPreviewForm.setId("ContactDetails") ;
      uiPreviewForm.setPrintForm(true) ;
      uiPreviewForm.setContact(uiContacts.contactMap.get(contactId)) ;
      UIPopupAction popupAction = contactPortlet.getChild(UIPopupAction.class) ;
      popupAction.activate(uiPreviewForm, 700, 0) ;
      
//    edited 17-4
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;  
//      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent()) ;
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
  
  static public class SortActionListener extends EventListener<UIContacts> {
    @SuppressWarnings("unchecked")
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource() ;
      String sortedBy = event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiContacts.setAscending(!uiContacts.isAscending_);
      uiContacts.setSortedBy(sortedBy);
      uiContacts.setDefaultNameSorted(false) ;
      
      JCRPageList pageList = null ;
      String group = uiContacts.selectedGroup ;
      if (!ContactUtils.isEmpty(group)) {
        ContactFilter filter = new ContactFilter() ;
        filter.setViewQuery(uiContacts.getViewQuery());        
        filter.setAscending(uiContacts.isAscending_);
        filter.setOrderBy(sortedBy);
        filter.setCategories(new String[] { group } ) ;

        String type = null;
        UIAddressBooks addressBooks = uiContacts.getAncestorOfType(
            UIWorkingContainer.class).findFirstComponentOfType(UIAddressBooks.class) ;
        if (addressBooks.getPrivateGroupMap().containsKey(group)) type = JCRDataStorage.PRIVATE ;
        else if (addressBooks.getSharedGroups().containsKey(group)) type = JCRDataStorage.SHARED ;
        else type = JCRDataStorage.PUBLIC ;
        
        //else if (addressBooks.getPublicGroupMap().containsKey(group)) type = JCRDataStorage.PUBLIC ;
        
        if(type != null)
          pageList = ContactUtils.getContactService().getContactPageListByGroup(
            SessionProviderFactory.createSystemProvider(),ContactUtils.getCurrentUser(), filter, type) ;
      } else {      //selected group = null ;
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
    public static boolean isAsc ;
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
    }
  }
  
  static public class SelectTagActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource() ;
      String tagId = event.getRequestContext().getRequestParameter(OBJECTID) ; 
      UIWorkingContainer uiWorkingContainer = uiContacts.getAncestorOfType(UIWorkingContainer.class) ;
      uiWorkingContainer.findFirstComponentOfType(UIAddressBooks.class).setSelectedGroup(null) ;
      UITags tags = uiWorkingContainer.findFirstComponentOfType(UITags.class) ;
      tags.setSelectedTag(tagId) ;
      uiContacts.setContacts(ContactUtils.getContactService()
        .getContactPageListByTag(SessionProviderFactory.createSystemProvider(), ContactUtils.getCurrentUser(), tagId)) ;
      uiContacts.setSelectedGroup(null) ;
      uiContacts.setSelectedTag(tagId) ;
      uiContacts.setDisplaySearchResult(false) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingContainer) ;
    }
  }

  static public class SendEmailActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource() ;
      String objectId = event.getRequestContext().getRequestParameter(OBJECTID);
      String emails = null ;
      
      if (!ContactUtils.isEmpty(objectId)) {
        if (uiContacts.contactMap.containsKey(objectId))
          emails = uiContacts.contactMap.get(objectId).getEmailAddress() ;
        else emails = objectId ;
      } else {
        List<String> contactIds = uiContacts.getCheckedContacts() ;
        if (contactIds.size() < 1) {
          UIApplication uiApp = uiContacts.getAncestorOfType(UIApplication.class) ;
          uiApp.addMessage(new ApplicationMessage("UIContacts.msg.checkContact-toSendMail", null,
              ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
        StringBuffer buffer = new StringBuffer() ;
        /*
        String email = uiContacts.contactMap.get(contactIds.get(0)).getEmailAddress() ;
        if (!ContactUtils.isEmpty(email)) buffer.append(email) ; 
        for (int i = 1; i < contactIds.size(); i ++) {
          email = uiContacts.contactMap.get(contactIds.get(i)).getEmailAddress() ;
          if (!ContactUtils.isEmpty(email)) buffer.append(", " + email) ;
        }
        */
        for (String id : contactIds) {
          String email = uiContacts.contactMap.get(id).getEmailAddress() ;
          if (!ContactUtils.isEmpty(email)) {
            if (buffer.length() > 0) buffer.append(", " + email) ;
            else buffer.append(email) ;
          }
        }
        
        emails = buffer.toString() ; 
      }
      if (ContactUtils.isEmpty(emails)) {
        UIApplication uiApp = uiContacts.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIContacts.msg.no-email-found", null,
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;        
      }
      UIContactPortlet contactPortlet = uiContacts.getAncestorOfType(UIContactPortlet.class) ;
      UIPopupAction popupAction = contactPortlet.getChild(UIPopupAction.class) ;
      
      List<Account> acc = ContactUtils.getAccounts() ;
      if (acc == null || acc.size() < 1) {
        UIApplication uiApp = uiContacts.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.invalidAcc", null,
            ApplicationMessage.WARNING)) ;
        return ;
      }
      
      UIComposeForm uiComposeForm = popupAction.activate(UIComposeForm.class, 850) ;
      uiComposeForm.init(acc, emails) ;  
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;  
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent()) ;
    }
  }
  
  static public class SharedContactsActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource() ;
      Map<String, Contact> mapContacts = new LinkedHashMap<String, Contact>() ;
      for (String contactId : uiContacts.getCheckedContacts()) {
        Contact contact = uiContacts.contactMap.get(contactId) ;        
        String contactType = contact.getContactType() ; 
        if (contactType.equals(JCRDataStorage.PUBLIC) || contactType.equals(JCRDataStorage.SHARED)) {
          UIApplication uiApp = uiContacts.getAncestorOfType(UIApplication.class) ;
          uiApp.addMessage(new ApplicationMessage("UIContacts.msg.cannot-share", null
              , ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
        mapContacts.put(contactId, contact) ;
      }
      String objectId = event.getRequestContext().getRequestParameter(OBJECTID);
      if (!ContactUtils.isEmpty(objectId) || uiContacts.getCheckedContacts().size() == 1) {
        if (ContactUtils.isEmpty(objectId)) objectId = uiContacts.getCheckedContacts().get(0) ; 
        UIContactPortlet contactPortlet = uiContacts.getAncestorOfType(UIContactPortlet.class) ;
        UIPopupAction popupAction = contactPortlet.getChild(UIPopupAction.class) ;
        UIPopupContainer uiPopupContainer = popupAction.activate(UIPopupContainer.class, 400) ;
        uiPopupContainer.setId("UIPermissionContactForm") ;
        UIAddEditPermission uiAddNewEditPermission = uiPopupContainer.addChild(UIAddEditPermission.class, null, null); 
        uiAddNewEditPermission.initContact(uiContacts.contactMap.get(objectId)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;  
      } else {        
        UIContactPortlet contactPortlet = uiContacts.getAncestorOfType(UIContactPortlet.class) ;
        UIPopupAction popupAction = contactPortlet.getChild(UIPopupAction.class) ;
        UIPopupContainer uiPopupContainer = popupAction.activate(UIPopupContainer.class, 600) ;
        uiPopupContainer.setId("UIPermissionContactsForm") ;
        UISharedContactsForm uiSharedForm = uiPopupContainer.addChild(UISharedContactsForm.class, null, null) ;   
        uiSharedForm.init(mapContacts) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent());    
    }
  }
  
  static public class PrintActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource() ;
      List<String> contactIds = uiContacts.getCheckedContacts() ;
      if (contactIds.size() < 1) {
        UIApplication uiApp = uiContacts.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIContacts.msg.checkContact-toPrint", null,
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      LinkedHashMap<String, Contact> contactMap = new LinkedHashMap<String, Contact> () ;
      uiContacts.setListBeforePrint(Arrays.asList(uiContacts.getContacts())) ;
      for (String contactId : contactIds) contactMap.put(contactId, uiContacts.contactMap.get(contactId)) ;
      uiContacts.contactMap = contactMap ;
      uiContacts.viewListBeforePrint = uiContacts.viewContactsList ;
      uiContacts.viewContactsList = false ;
      uiContacts.isPrintForm = true ;
      uiContacts.isPrintDetail = false ;
      uiContacts.getAncestorOfType(UIContactContainer.class).findFirstComponentOfType(UIContactPreview.class).setRendered(false) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent()) ;
    }
  }
 /* 
  static  public class ChatActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource() ;
      String contactId = event.getRequestContext().getRequestParameter(OBJECTID);
      
      if (!ContactUtils.isEmpty(contactId)) { 
      } else {
        List<String> contactIds = uiContacts.getCheckedContacts() ;
        if (contactIds.size() < 1) {
          UIApplication uiApp = uiContacts.getAncestorOfType(UIApplication.class) ;
          uiApp.addMessage(new ApplicationMessage("UIContacts.msg.checkContact-toChat", null,
              ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
      }
      
      try {
        Class im = java.lang.Class.forName("org.exoplatform.services.xmpp.rest.RESTXMPPService") ;
        
        System.out.println("\n\n 11:" + im.toString() );
        
        for (Constructor c : im.getConstructors()) {
          System.out.println("\n\n 22:" + c.toString());
        }
        
     
      } catch (ClassNotFoundException e) {
        UIApplication uiApp = uiContacts.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIContacts.msg.chatApp-notAvaiable", null,
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;        
      }
      
      
    }
  }
  */
  static  public class CancelActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource() ;
      uiContacts.isPrintForm = false ;
      uiContacts.viewContactsList = uiContacts.viewListBeforePrint ;
      uiContacts.getChildren().clear() ;
      uiContacts.contactMap.clear();
      UIContactPreview contactPreview = 
        uiContacts.getAncestorOfType(UIContactContainer.class).getChild(UIContactPreview.class) ;    
      if (uiContacts.listBeforePrint != null && uiContacts.listBeforePrint.size() > 0) {
        for(Contact contact : uiContacts.listBeforePrint) {
          UIFormCheckBoxInput<Boolean> checkbox = new UIFormCheckBoxInput<Boolean>(contact.getId(),contact.getId(), false) ;
          uiContacts.addUIFormInput(checkbox);
          uiContacts.contactMap.put(contact.getId(), contact) ; 
        }
        Contact[] array = uiContacts.contactMap.values().toArray(new Contact[]{}) ;
        if (array.length > 0) {
          Contact firstContact = array[0] ;
          contactPreview.setContact(firstContact) ;
          uiContacts.selectedContact = firstContact.getId() ;
        } else contactPreview.setContact(null) ;
      } else contactPreview.setContact(null) ;
      uiContacts.setListBeforePrint(new ArrayList<Contact>()) ;
      //uiContacts.updateList() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent()) ;
    }
  }
  
  static  public class RefreshActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource() ;
      uiContacts.refreshData() ;
    }
  }
 
}
