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

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import javax.jcr.AccessDeniedException;
import javax.jcr.PathNotFoundException;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.AddressBook;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactFilter;
import org.exoplatform.contact.service.ContactPageList;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.DataPageList;
import org.exoplatform.contact.service.DataStorage;
import org.exoplatform.contact.service.JCRPageList;
import org.exoplatform.contact.service.SharedAddressBook;
import org.exoplatform.contact.service.Tag;
import org.exoplatform.contact.service.impl.NewUserListener;
import org.exoplatform.contact.webui.popup.UIAddEditPermission;
import org.exoplatform.contact.webui.popup.UICategorySelect;
import org.exoplatform.contact.webui.popup.UIComposeForm;
import org.exoplatform.contact.webui.popup.UIContactForm;
import org.exoplatform.contact.webui.popup.UIContactPreviewForm;
import org.exoplatform.contact.webui.popup.UIMoveContactsForm;
import org.exoplatform.contact.webui.popup.UIPopupAction;
import org.exoplatform.contact.webui.popup.UIPopupComponent;
import org.exoplatform.contact.webui.popup.UIPopupContainer;
import org.exoplatform.contact.webui.popup.UISharedContactsForm;
import org.exoplatform.contact.webui.popup.UITagForm;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadResource;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.mail.service.Account;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL 
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com 
 * Aus 01, 2007 2:48:18 PM
 */

@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "app:/templates/contact/webui/UIContacts.gtmpl", events = {
    @EventConfig(listeners = UIContacts.EditContactActionListener.class),
    @EventConfig(listeners = UIContacts.SendEmailActionListener.class),
    @EventConfig(listeners = UIContacts.TagActionListener.class),
    @EventConfig(listeners = UIContacts.MoveContactsActionListener.class),
    @EventConfig(listeners = UIContacts.DNDContactsActionListener.class),
    @EventConfig(listeners = UIContacts.DNDContactsToTagActionListener.class),
    @EventConfig(listeners = UIContacts.DeleteContactsActionListener.class, confirm = "UIContacts.msg.confirm-delete"),
    @EventConfig(listeners = UIContacts.SelectedContactActionListener.class),
    @EventConfig(listeners = UIContacts.CopyContactActionListener.class),
    @EventConfig(listeners = UIContacts.ViewDetailsActionListener.class),
    @EventConfig(listeners = UIContacts.SortActionListener.class),
    
    @EventConfig(listeners = UIContacts.FirstPageActionListener.class),
    @EventConfig(listeners = UIContacts.PreviousPageActionListener.class),
    @EventConfig(listeners = UIContacts.NextPageActionListener.class),
    @EventConfig(listeners = UIContacts.LastPageActionListener.class),
    
    @EventConfig(listeners = UIContacts.GotoPageActionListener.class),
    
    @EventConfig(listeners = UIContacts.ExportContactActionListener.class),
    @EventConfig(listeners = UIContacts.CancelActionListener.class),
    @EventConfig(listeners = UIContacts.SelectTagActionListener.class),
    @EventConfig(listeners = UIContacts.SharedContactsActionListener.class),
    @EventConfig(listeners = UIContacts.CloseSearchActionListener.class),
    @EventConfig(listeners = UIContacts.PrintActionListener.class),
    @EventConfig(listeners = UIContacts.RefreshActionListener.class),
    @EventConfig(listeners = UIContacts.PrintDetailsActionListener.class) })
public class UIContacts extends UIForm implements UIPopupComponent {
  public boolean                         viewContactsList                   = true;

  public boolean                         viewListBeforePrint                = false;

  private String                         selectedTag_                       = null;


  private String                         selectedGroup                      = null;

  private String                         selectedContact                    = null;

  private JCRPageList                    pageList_                          = null;

  private String                         sortedBy_                          = null;

  private boolean                        isAscending_                       = true;

  private String                         viewQuery_                         = null;

  public static String                   fullName                           = "fullName".intern();

  public static String                   emailAddress                       = "emailAddress".intern();

  public static String                   jobTitle                           = "jobTitle".intern();

  private boolean                        isSearchResult                     = false;

  private boolean                        defaultNameSorted                  = true;

  private boolean                        isPrintForm                        = false;

  private boolean                        isPrintDetail                      = false;

  private boolean                        isSelectSharedContacts             = false;


  private String                         selectedTagBeforeSearch_           = null;

  private String                         selectedGroupBeforeSearch          = null;

  private boolean                        isSelectSharedContactsBeforeSearch = false;

  private boolean                        viewListBeforeSearch               = true;

  public String                          checkedAll                         = "";

  public static final String             FIRST_PAGE                         = "FirstPage";

  public static final String             PREVIOUS_PAGE                      = "PreviousPage";

  public static final String             NEXT_PAGE                          = "NextPage";

  public static final String             LAST_PAGE                          = "LastPage";

  public int                             totalCheked                        = 0;

  private List<String>                   currentCheckedList                 = new ArrayList<String>();

  private Map<Long, List<String>>        pageCheckedList                    = new HashMap<Long, List<String>>();

  private Map<String, Contact>           contactOpened                      = new HashMap<String, Contact>();

  private LinkedHashMap<String, Contact> contactInPage                      = new LinkedHashMap<String, Contact>();

  private LinkedHashMap<String, Contact> beforePrintMap                     = new LinkedHashMap<String, Contact>();
  
  public UIContacts() throws Exception {
  }

  public List<String> getCurrentCheckedList() {
    return (currentCheckedList != null )?currentCheckedList: new ArrayList<String>();
  }

  public int getTotalChecked() {
    return totalCheked;
  }
  
  public String getOnclickEvent(String eventName, String confirm) throws Exception {
    if (totalCheked > 0){
      confirm = "onclick=\""+event(eventName)+"\"";
    }
    return confirm;
  }

  public void clearCheckedList() {
    totalCheked = 0;
    pageCheckedList.clear();
  }
  
  public void clearContactOpened() {
    contactOpened.clear();
  }
  
  public void backupBeforePrint() {
    beforePrintMap.clear();
    beforePrintMap.putAll(contactInPage);
  }

  public String[] getActions() {
    return new String[] { "Cancel" };
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  public boolean canChat() {
    try {
      java.lang.Class.forName("org.exoplatform.services.xmpp.rest.RESTXMPPService");
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    } catch (Exception ex) {
      ex.printStackTrace();
      return false;
    }
  }

  // only called when refresh browser and close search ;
  @SuppressWarnings("unchecked")
  private void refreshData() throws Exception {
    if (isDisplaySearchResult() || isPrintForm)
      return;
    long currentPage = 1;
    if (selectedGroup != null) {
      if (pageList_ != null)
        currentPage = pageList_.getCurrentPage();
      ContactPageList pageList = null;
      if (getPrivateGroupMap().containsKey(selectedGroup)) {
        pageList = ContactUtils.getContactService()
                               .getPersonalContactsByAddressBook(ContactUtils.getCurrentUser(),
                                                                 selectedGroup);
      } else if (ContactUtils.getUserGroups().contains(selectedGroup)) {
        pageList = ContactUtils.getContactService().getPublicContactsByAddressBook(selectedGroup);
      } else if (getSharedGroupMap().containsKey(selectedGroup)) {
        UIAddressBooks uiAddressBooks = getAncestorOfType(UIWorkingContainer.class).findFirstComponentOfType(UIAddressBooks.class);
        pageList = ContactUtils.getContactService()
                               .getSharedContactsByAddressBook(ContactUtils.getCurrentUser(),
                                                               uiAddressBooks.getSharedGroups().get(selectedGroup));
      } else {
        selectedGroup = null;
      }
      if (pageList != null)
        pageList.setCurrentPage(currentPage);
      setContacts(pageList);
    } else if (selectedTag_ != null) {
      if (pageList_ != null)
        currentPage = pageList_.getCurrentPage();
      DataPageList pageList = ContactUtils.getContactService()
                                          .getContactPageListByTag(ContactUtils.getCurrentUser(),
                                                                   selectedTag_);
      if (pageList != null) {
        List<Contact> contacts = new ArrayList<Contact>();
        contacts = pageList.getAll();
        if (getSortedBy().equals(UIContacts.fullName)) {
          Collections.sort(contacts, new FullNameComparator());
        } else if (getSortedBy().equals(UIContacts.emailAddress)) {
          Collections.sort(contacts, new EmailComparator());
        } else if (getSortedBy().equals(UIContacts.jobTitle)) {
          Collections.sort(contacts, new JobTitleComparator());
        }
        pageList.setList(contacts);
        pageList.setCurrentPage(currentPage);
      }
      setContacts(pageList);
    } else if (isSelectSharedContacts) {
      setContacts(ContactUtils.getContactService().getSharedContacts(ContactUtils.getCurrentUser()));
    }
  }

  public void setSelectSharedContacts(boolean selected) {
    isSelectSharedContacts = selected;
  }

  public boolean isSelectSharedContacts() {
    return isSelectSharedContacts;
  }

  public boolean havePermissionAdd(Contact contact) throws Exception {
    if (!contact.getContactType().equals(DataStorage.SHARED))
      return false;
    Map<String, SharedAddressBook> sharedGroupMap = getAncestorOfType(UIWorkingContainer.class).findFirstComponentOfType(UIAddressBooks.class)
                                                                                               .getSharedGroups();
    String currentUser = ContactUtils.getCurrentUser();
    for (String address : contact.getAddressBookIds()) {
      SharedAddressBook add = sharedGroupMap.get(address);
      if (add != null) {
        if (add.getEditPermissionUsers() != null
            && Arrays.asList(add.getEditPermissionUsers()).contains(currentUser
                + DataStorage.HYPHEN)) {
          return true;
        }
        String[] editPerGroups = add.getEditPermissionGroups();
        if (editPerGroups != null)
          for (String editPer : editPerGroups)
            if (ContactUtils.getUserGroups().contains(editPer)) {
              return true;
            }
        return false;
      }
    }
    return false;
  }

  public boolean isShareForOther(Contact contact) {
    if (!contact.getContactType().equals(DataStorage.PERSONAL)) return false;
    if ((contact.getViewPermissionGroups() != null && contact.getViewPermissionGroups().length > 0) || 
        (contact.getViewPermissionUsers() != null && contact.getViewPermissionUsers().length > 0))
        return true;
    return false;
  }
  
  public boolean havePermission(Contact contact) throws Exception {
    if (!contact.getContactType().equals(DataStorage.SHARED))
      return true;
    // contact shared
    String currentUser = ContactUtils.getCurrentUser();
    if (contact.getEditPermissionUsers() != null
        && Arrays.asList(contact.getEditPermissionUsers()).contains(currentUser
            + DataStorage.HYPHEN)) {
      return true;
    }
    String[] editPerGroups = contact.getEditPermissionGroups();
    if (editPerGroups != null)
      for (String editPer : editPerGroups)
        if (ContactUtils.getUserGroups().contains(editPer)) {
          return true;
        }
    Map<String, SharedAddressBook> sharedGroupMap = getAncestorOfType(UIWorkingContainer.class).findFirstComponentOfType(UIAddressBooks.class)
                                                                                               .getSharedGroups();
    for (String address : contact.getAddressBookIds()) {
      try {
        SharedAddressBook add = sharedGroupMap.get(address);
        if (add.getEditPermissionUsers() != null
            && Arrays.asList(add.getEditPermissionUsers()).contains(currentUser
                + DataStorage.HYPHEN)) {
          return true;
        }
        editPerGroups = add.getEditPermissionGroups();
        if (editPerGroups != null)
          for (String editPer : editPerGroups)
            if (ContactUtils.getUserGroups().contains(editPer)) {
              return true;
            }
      } catch (NullPointerException e) {
        return false;
      }
    }
    return false;
  }

  public boolean isSharedAddress(Contact contact) throws Exception {
    if (isSelectSharedContacts)
      return false;
    for (String add : contact.getAddressBookIds()) {
      if (getSharedGroupMap().containsKey(add)) {
        return true;
      }
    }
    return false;
  }

  public void setPrintForm(boolean isPrint) {
    isPrintForm = isPrint;
  }

  public boolean isPrintForm() {
    return isPrintForm;
  }

  public void setPrintDetail(boolean isDetail) {
    isPrintDetail = isDetail;
  }

  public boolean isDisplaySearchResult() {
    if (!isSearchResult) {
      getAncestorOfType(UIContactPortlet.class).findFirstComponentOfType(UISearchForm.class)
                                               .getChild(UIFormStringInput.class).setValue(null);
    }
    return isSearchResult;
  }

  public void setDisplaySearchResult(boolean search) {
    isSearchResult = search;
  }

  public void setViewListBeforePrint(boolean isList) {
    viewListBeforePrint = isList;
  }

  public void setAscending(boolean isAsc) {
    isAscending_ = isAsc;
  }

  public boolean isAscending() {
    return isAscending_;
  }

  public void setSortedBy(String s) {
    sortedBy_ = s;
  }

  public String getSortedBy() {
    return sortedBy_;
  }

  public String getViewQuery() {
    return viewQuery_;
  }

  public void setViewQuery(String view) {
    viewQuery_ = view;
  }

  public void setContacts(JCRPageList pageList) throws Exception {
    pageList_ = pageList;
    clearCheckedList();
    clearContactOpened();
    updateList();
  }

  public JCRPageList getContactPageList() {
    return pageList_;
  }

  public boolean isAscName() {
    return FullNameComparator.isAsc;
  }

  public boolean isAscEmail() {
    return EmailComparator.isAsc;
  }

  public boolean isAscJob() {
    return JobTitleComparator.isAsc;
  }

  public void setDefaultNameSorted(boolean name) {
    defaultNameSorted = name;
  }

  public boolean isNameSorted() {
    return defaultNameSorted;
  }

  public void setContact(List<Contact> contacts, boolean isUpdate) throws Exception {
    if (pageList_ != null)
      pageList_.setContact(contacts, isUpdate);
  }

  public List<String> getAllContactChecked() throws Exception {
    List<String> ids = new ArrayList<String>();
    for (int i = 0; i <= pageList_.getAvailablePage(); i++) {
      if (pageCheckedList.get(Long.valueOf(i)) != null) {
        ids.addAll(pageCheckedList.get(Long.valueOf(i)));
      }
    }
    clearCheckedList();
    return getCheckedCurrentPage(ids);
  }
  
  private List<String> getCheckedCurrentPage(List<String> ids) {
    for (String contactId : contactInPage.keySet()) {
      UIFormCheckBoxInput<Boolean> boxInput = getChildById(contactId);
      if (boxInput.isChecked()) {
        if (!ids.contains(contactId)) {
          ids.add(contactId);
        }
      } else {
        if (ids.contains(contactId)) {
          ids.remove(contactId);
        }
      }
    }
    return ids;
  }
  
  public void updateList() throws Exception {
    contactInPage.clear();
    UIContactPreview contactPreview = getAncestorOfType(UIContactContainer.class).getChild(UIContactPreview.class);
    if (pageList_ != null) {
      List<Contact> contactList = pageList_.getPage(pageList_.getCurrentPage(),
                                                    ContactUtils.getCurrentUser());
      if (contactList.size() == 0 && pageList_.getCurrentPage() > 1) {
        contactList = pageList_.getPage(pageList_.getCurrentPage() - 1,
                                        ContactUtils.getCurrentUser());
      }
      currentCheckedList = pageCheckedList.get(Long.valueOf(pageList_.getCurrentPage()));
      checkedAll = "checked";
      for (Contact contact : contactList) {
        UIFormCheckBoxInput<Boolean> checkbox = getChildById(contact.getId()) ;
        if(checkbox == null){
          checkbox = new UIFormCheckBoxInput<Boolean>(contact.getId(), contact.getId(), false);
          addUIFormInput(checkbox);
        }
        if (currentCheckedList != null && currentCheckedList.contains(contact.getId())) {
          checkbox.setChecked(true);
        } else {
          checkbox.setChecked(false);
          checkedAll = "";
        }
        contactInPage.put(contact.getId(), contact);
      }
      if(contactInPage.size() > 0) {
        contactOpened.putAll(contactInPage);
        if (!ContactUtils.isEmpty(selectedContact) && contactInPage.containsKey(selectedContact)) {
          contactPreview.setContact(contactInPage.get(selectedContact));
        } else {
          Contact firstContact = contactInPage.values().iterator().next();
          contactPreview.setContact(firstContact);
          selectedContact = firstContact.getId();
        }
      } else
        contactPreview.setContact(null);
    } else
      contactPreview.setContact(null);
  }

  public List<Contact> getContacts() throws Exception {
    Collection<Contact> cts = contactInPage.values();
    if(cts.size() > 0){
      return new ArrayList<Contact>(cts) ;
    } else {
      return new ArrayList<Contact>() ;
    }
  }

  public LinkedHashMap<String, Contact> getContactMap() {
    return contactInPage;
  }

  public void setContactMap(LinkedHashMap<String, Contact> map) {
    contactInPage = map;
  }

  public void setSelectedContact(String s) {
    selectedContact = s;
  }

  public String getSelectedContact() {
    return selectedContact;
  }

  public void setSelectedGroup(String s) {
    selectedGroup = s;
  }

  public String getSelectedGroup() {
    return selectedGroup;
  }

  public void setViewContactsList(boolean list) {
    viewContactsList = list;
  }

  public boolean getViewContactsList() {
    if (viewContactsList) {
      getAncestorOfType(UIContactContainer.class).getChild(UIContactPreview.class)
                                                 .setRendered(true);
    } else {
      getAncestorOfType(UIContactContainer.class).getChild(UIContactPreview.class)
                                                 .setRendered(false);
    }
    return viewContactsList;
  }

  // remove
  public DownloadService getDownloadService() {
    return getApplicationComponent(DownloadService.class);
  }

  public String getPortalName() {
    PortalContainer pcontainer = PortalContainer.getInstance();
    return pcontainer.getPortalContainerInfo().getContainerName();
  }

  public String getRepository() throws Exception {
    RepositoryService rService = getApplicationComponent(RepositoryService.class);
    return rService.getCurrentRepository().getConfiguration().getName();
  }

  public String getSelectedTag() {
    return selectedTag_;
  }

  public void setSelectedTag(String tagId) {
    selectedTag_ = tagId;
  }

  public Map<String, Tag> getTagMap() {
    return getAncestorOfType(UIWorkingContainer.class).findFirstComponentOfType(UITags.class)
                                                      .getTagMap();
  }

  public Map<String, String> getPrivateGroupMap() {
    return getAncestorOfType(UIWorkingContainer.class).findFirstComponentOfType(UIAddressBooks.class)
                                                      .getPrivateGroupMap();
  }

  public Map<String, SharedAddressBook> getSharedGroupMap() throws Exception {
    return getAncestorOfType(UIWorkingContainer.class).findFirstComponentOfType(UIAddressBooks.class)
                                                      .getSharedGroups();
  }

  public List<String> getPublicContactGroups() throws Exception {
    return Arrays.asList(ContactUtils.getUserGroups().toArray(new String[] {}));
  }

  public String getDefaultGroup() {
    return NewUserListener.DEFAULTGROUP;
  }

  public String getSelectedTagBeforeSearch_() {
    return selectedTagBeforeSearch_;
  }

  public void setSelectedTagBeforeSearch_(String selectedTagBeforeSearch_) {
    this.selectedTagBeforeSearch_ = selectedTagBeforeSearch_;
  }

  public String getSelectedGroupBeforeSearch() {
    return selectedGroupBeforeSearch;
  }

  public void setSelectedGroupBeforeSearch(String selectedGroupBeforeSearch) {
    this.selectedGroupBeforeSearch = selectedGroupBeforeSearch;
  }

  public boolean isSelectSharedContactsBeforeSearch() {
    return isSelectSharedContactsBeforeSearch;
  }

  public void setSelectSharedContactsBeforeSearch(boolean isSelectSharedContactsBeforeSearch) {
    this.isSelectSharedContactsBeforeSearch = isSelectSharedContactsBeforeSearch;
  }

  public boolean isViewListBeforeSearch() {
    return viewListBeforeSearch;
  }

  public void setViewListBeforeSearch(boolean viewListBeforeSearch) {
    this.viewListBeforeSearch = viewListBeforeSearch;
  }

  static public class EditContactActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      String contactId = event.getRequestContext().getRequestParameter(OBJECTID);
      Contact contact = uiContacts.contactInPage.get(contactId);
      if (contact == null) {
        UIApplication uiApp = uiContacts.getAncestorOfType(UIApplication.class);
        uiApp.addMessage(new ApplicationMessage("UIContacts.msg.contact-deleted",
                                                null,
                                                ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent());
        return;
      }
      ContactService service = ContactUtils.getContactService();
      String username = ContactUtils.getCurrentUser();
      if (contact.getContactType().equalsIgnoreCase(DataStorage.PERSONAL)) {
        try {
          contact = service.getContact(username, contactId);
        } catch (NullPointerException e) {
          contact = null;
        }
      } else {// shared
        try {
          contact = service.getSharedContact(username, contactId);
        } catch (NullPointerException e) {
          contact = null;
        }
        if (contact == null) {
          try {
            contact = service.getSharedContactAddressBook(username, contactId);
          } catch (NullPointerException e) {
          }
        }
      }
      if (contact == null) {
        UIApplication uiApp = uiContacts.getAncestorOfType(UIApplication.class);
        uiApp.addMessage(new ApplicationMessage("UIContacts.msg.contact-deleted",
                                                null,
                                                ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent());
        return;
      }

      // avoid cache id of edited old contact
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent());
      UIContactPortlet contactPortlet = uiContacts.getAncestorOfType(UIContactPortlet.class);
      UIPopupAction popupAction = contactPortlet.getChild(UIPopupAction.class);
      UIPopupContainer popupContainer = popupAction.activate(UIPopupContainer.class, 800);
      popupContainer.setId("AddNewContact");

      UICategorySelect uiCategorySelect = popupContainer.addChild(UICategorySelect.class,
                                                                  null,
                                                                  null);
      UIAddressBooks uiAddressBooks = uiContacts.getAncestorOfType(UIWorkingContainer.class)
                                                .findFirstComponentOfType(UIAddressBooks.class);
      Map<String, String> privateGroups = uiAddressBooks.getPrivateGroupMap();
      Map<String, SharedAddressBook> sharedAddress = uiAddressBooks.getSharedGroups();
      List<SelectItemOption<String>> categories = new ArrayList<SelectItemOption<String>>();
      for (String add : contact.getAddressBookIds()) {
        if (privateGroups.containsKey(add)) {
          categories.add(new SelectItemOption<String>(ContactUtils.encodeHTML(privateGroups.get(add)), add));
          continue;
        } else if (sharedAddress.containsKey(add)) {
          categories.add(new SelectItemOption<String>(ContactUtils.encodeHTML(
                                                       ContactUtils.getDisplayAdddressShared(sharedAddress.get(add).getSharedUserId(),
                                                       sharedAddress.get(add).getName())), add));
          continue;
        }
      }
      // cs-1899
      if (categories.size() == 0) {
        WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
        ResourceBundle res = context.getApplicationResourceBundle();
        String sharedLabel = "Shared Contact";
        try {
          sharedLabel = res.getString("UIContacts.label.sharedContacts");
        } catch (MissingResourceException e) {
          e.printStackTrace();
        }
        categories.add(new SelectItemOption<String>(sharedLabel, sharedLabel));
      }

      UIFormInputWithActions input = new UIFormInputWithActions(UICategorySelect.INPUT_CATEGORY);
      UIFormSelectBox uiSelectBox = new UIFormSelectBox(UICategorySelect.FIELD_CATEGORY, UICategorySelect.FIELD_CATEGORY, categories);
      uiSelectBox.setEnable(false);
      input.addUIFormInput(uiSelectBox);
      uiCategorySelect.addUIFormInput(input);
      UIContactForm uiContactForm = popupContainer.addChild(UIContactForm.class, null, null);
      uiContactForm.setValues(contact);
      uiContactForm.setNew(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
    }
  }

  static public class TagActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      String contactId = event.getRequestContext().getRequestParameter(OBJECTID);
      List<String> contactIds = new ArrayList<String>();
      if (!ContactUtils.isEmpty(contactId) && !contactId.equals("null")) {
        contactIds.add(contactId);
      } else {
        contactIds = uiContacts.getAllContactChecked();
        if (contactIds.size() == 0) {
          UIApplication uiApp = uiContacts.getAncestorOfType(UIApplication.class);
          uiApp.addMessage(new ApplicationMessage("UIContacts.msg.checkContact-toTag", null, ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          return;
        }
      }

      UIContactPortlet contactPortlet = uiContacts.getAncestorOfType(UIContactPortlet.class);
      UIPopupAction popupAction = contactPortlet.getChild(UIPopupAction.class);
      UITagForm uiTagForm = popupAction.activate(UITagForm.class, 600);
      List<Contact> contacts = new ArrayList<Contact>();
      for (String id : contactIds) {
        contacts.add(uiContacts.contactOpened.get(id));
      }
      uiTagForm.setContacts(contacts);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
    }
  }

  static public class DNDContactsToTagActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      uiContacts.getAncestorOfType(UIContactPortlet.class).cancelAction();
      String tagId = event.getRequestContext().getRequestParameter(OBJECTID);
      @SuppressWarnings("unused")
      String type = event.getRequestContext().getRequestParameter("contactType");
      List<String> contactIds = new ArrayList<String>();
      UIApplication uiApp = uiContacts.getAncestorOfType(UIApplication.class);
      contactIds = uiContacts.getAllContactChecked();
      List<String> newContactIds = new ArrayList<String>();
      for (String contactId : contactIds) {
        Contact contact = uiContacts.contactOpened.get(contactId);
        newContactIds.add(contactId + DataStorage.SPLIT + contact.getContactType());
      }
      try {
        ContactUtils.getContactService().addTag(ContactUtils.getCurrentUser(), newContactIds, tagId);
      } catch (PathNotFoundException e) {
        uiApp.addMessage(new ApplicationMessage("UIContacts.msg.contact-deleted", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent());
        return;
      }

      for (String contactId : contactIds) {
        Contact contact = uiContacts.contactOpened.get(contactId);
        String[] tags = contact.getTags();
        if (tags != null && tags.length > 0) {
          Map<String, String> newTags = new LinkedHashMap<String, String>();
          for (String tag : tags)
            newTags.put(tag, tag);
          newTags.put(tagId, tagId);
          contact.setTags(newTags.keySet().toArray(new String[] {}));
        } else {
          contact.setTags(new String[] { tagId });
        }
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent());
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts);
    }
  }

  static public class MoveContactsActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      String contactId = event.getRequestContext().getRequestParameter(OBJECTID);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent());
      List<String> contactIds = new ArrayList<String>();
      UIApplication uiApp = uiContacts.getAncestorOfType(UIApplication.class);
      UIContactPortlet uiContactPortlet = uiContacts.getAncestorOfType(UIContactPortlet.class);
      UIAddressBooks addressBooks = uiContactPortlet.findFirstComponentOfType(UIAddressBooks.class);

      Map<String, Contact> movedContacts = new HashMap<String, Contact>();
      if (!ContactUtils.isEmpty(contactId) && !contactId.equals("null")) {
        contactIds.add(contactId);
        movedContacts.put(contactId, uiContacts.contactInPage.get(contactId));
      } else {
        contactIds = uiContacts.getAllContactChecked();
        if (contactIds.size() == 0) {
          uiApp.addMessage(new ApplicationMessage("UIContacts.msg.checkContact-toMove", null,  ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          return;
        }
        for (String id : contactIds) {
          Contact contact = uiContacts.contactOpened.get(id);
          if (contact.getContactType().equals(DataStorage.PUBLIC)) {
            uiApp.addMessage(new ApplicationMessage("UIContacts.msg.cannot-move", null, ApplicationMessage.WARNING));
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
            return;
          } else if (contact.getContactType().equals(DataStorage.SHARED)
              && uiContacts.isSharedAddress(contact)) {
            if (contact.isOwner()) {
              uiApp.addMessage(new ApplicationMessage("UIContacts.msg.cannot-move", null, ApplicationMessage.WARNING));
              event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
              return;
            } else {
              String groupId = null;
              for (String add : contact.getAddressBookIds())
                if (addressBooks.getSharedGroups().containsKey(add))
                  groupId = add;
              if (groupId != null && !addressBooks.havePermission(groupId)) {
                uiApp.addMessage(new ApplicationMessage("UIContacts.msg.cannot-move", null, ApplicationMessage.WARNING));
                event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
                return;
              }
            }
          } else if (contact.getId().equals(ContactUtils.getCurrentUser())) {
            uiApp.addMessage(new ApplicationMessage("UIContacts.msg.cannot-move-ownerContact", null, ApplicationMessage.WARNING));
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
            return;
          }
          movedContacts.put(id, contact);
        }
      }

      UIPopupAction popupAction = uiContactPortlet.getChild(UIPopupAction.class);
      UIMoveContactsForm uiMoveForm = popupAction.activate(UIMoveContactsForm.class, 540);
      uiMoveForm.setContacts(movedContacts);
      uiMoveForm.setPrivateGroupMap(addressBooks.getPrivateGroupMap());
      uiMoveForm.setSharedGroupMap(addressBooks.getSharedGroups());
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
    }
  }
  
  private static void showWarnCanNotMove(UIContacts uiContacts, UIApplication uiApplication,Event<UIContacts> event){
    uiApplication.addMessage(new ApplicationMessage("UIContacts.msg.cannot-move", null, ApplicationMessage.WARNING));
    event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
    event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent());
  }
  
  static public class DNDContactsActionListener extends EventListener<UIContacts> {
    @SuppressWarnings("unchecked")
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      uiContacts.getAncestorOfType(UIContactPortlet.class).cancelAction();
      String addressBookId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIAddressBooks uiAddressBooks = uiContacts.getAncestorOfType(UIWorkingContainer.class)
                                                .findFirstComponentOfType(UIAddressBooks.class);
      UIApplication uiApp = uiAddressBooks.getAncestorOfType(UIApplication.class) ;
      ContactService contactService = ContactUtils.getContactService();
      String username = ContactUtils.getCurrentUser();
      
      if (uiAddressBooks.getSharedGroups().containsKey(addressBookId)) {
        AddressBook group = contactService.getSharedAddressBook(username, addressBookId);
        if (group.getEditPermissionUsers() == null
            || !Arrays.asList(group.getEditPermissionUsers()).contains(username
                + DataStorage.HYPHEN)) {
          boolean canEdit = false;
          String[] editPerGroups = group.getEditPermissionGroups();
          if (editPerGroups != null)
            for (String editPer : editPerGroups)
              if (ContactUtils.getUserGroups().contains(editPer))
                canEdit = true;
          if (canEdit == false) {
            uiApp = ContactUtils.initWarnPopup(uiContacts, "UIContacts.msg.non-permission");
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
            event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent());
            return;
          }
        }
      }
      String type = event.getRequestContext().getRequestParameter("addressType");
      List<String> contactIds = uiContacts.getAllContactChecked();
      List<Contact> contacts = new ArrayList<Contact>();
      List<Contact> sharedContacts = new ArrayList<Contact>();
      List<Contact> pastedContact = new ArrayList<Contact>();
      Map<String, String> copySharedContacts = new LinkedHashMap<String, String>();
      List<Contact> unMoveContacts  = new ArrayList<Contact>();
      
      for (String contactId : contactIds) {
        Contact contact = uiContacts.contactOpened.get(contactId);
        if (contact.getId().equals(ContactUtils.getCurrentUser())) {
          uiApp = ContactUtils.initWarnPopup(uiContacts, "UIContacts.msg.cannot-move-ownerContact");
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent());
          return;
        }
        if (contact.getContactType().equals(DataStorage.PUBLIC)) {
          showWarnCanNotMove(uiContacts,uiApp,event);
          return;
        } else if (contact.getContactType().equals(DataStorage.SHARED)
            && uiContacts.isSharedAddress(contact)) {
          if (contact.isOwner()) {
            showWarnCanNotMove(uiContacts,uiApp,event);
            return;
          } else {
            String groupId = null;
            for (String add : contact.getAddressBookIds())
              if (uiAddressBooks.getSharedGroups().containsKey(add))
                groupId = add;
            if (groupId != null && !uiAddressBooks.havePermission(groupId)) {
              showWarnCanNotMove(uiContacts,uiApp,event);
              return;
            }
          }
        }
      }
      // cs- 1630
      Map<String, String> copyedContacts = uiAddressBooks.getCopyContacts();
      for (String contactId : contactIds) {
        Contact contact = uiContacts.contactOpened.get(contactId);
        if (!contact.getAddressBookIds()[0].equals(addressBookId))
          copyedContacts.remove(contactId);
        if (contact.getContactType().equals(DataStorage.SHARED)) {
          // check for existing contact
          Contact tempContact = null;
          if (uiContacts.isSharedAddress(contact)) {
            tempContact = contactService.getSharedContactAddressBook(username, contactId);
          } else {
            try {
              tempContact = contactService.getSharedContact(username, contactId);
            } catch (PathNotFoundException e) {
            }
          }
          if (tempContact == null) {
            uiApp = ContactUtils.initWarnPopup(uiContacts, "UIContacts.msg.contact-not-existed");
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
            return;
          }
          sharedContacts.add(contact);
          copySharedContacts.put(contactId, DataStorage.SHARED);
        } else {
          contact.setAddressBookIds(new String[] { addressBookId });
          contacts.add(contact);
        }
      }
  
      if (sharedContacts.size() > 0) {
        try {
          pastedContact = contactService.pasteContacts(username, addressBookId, type, copySharedContacts);   
        } catch (AccessDeniedException e) {
          uiApp = ContactUtils.initWarnPopup(uiContacts,"UIContacts.msg.noeditpermission");
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          return;
        }
        for (Contact contact : sharedContacts) {
          //check permission
          //if(contactService.haveEditPermissionOnContact(username,contact)){
          // TODO CS-4542
          if (uiContacts.havePermission(contact)) {
            if (uiContacts.isSharedAddress(contact)) {
              String addressId = null;
              for (String add : contact.getAddressBookIds())
                if (uiContacts.getSharedGroupMap().containsKey(add))
                  addressId = add;
              contactService.removeSharedContact(username, addressId, contact.getId());
            } else {
              contactService.removeUserShareContact(contact.getPath(), contact.getId(), username);
            }
            contact.setAddressBookIds(new String[] { addressBookId });
          }else{
            unMoveContacts.add(contact);
          } 
        }
        
        //check edit permission  
        
        if(unMoveContacts.size()>0){
          StringBuffer sb = new StringBuffer();
          for(Contact ct : unMoveContacts){
            sb.append(ct.getFirstName());
            if(sb.length()<unMoveContacts.size())
            sb.append(", ");
          }
          uiApp = ContactUtils.initPopup(uiContacts,"UIContacts.msg.noeditpermission.detail",new Object[]{sb.toString()},ApplicationMessage.WARNING);
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        }
      }
      
      if (contacts.size() > 0) {
        try {
          contactService.moveContacts(username, contacts, type);
        } catch (PathNotFoundException e) {
          uiApp = ContactUtils.initWarnPopup(uiContacts, "UIContacts.msg.contact-deleted");
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          return;
        }
      }

      // update addressbook when search
      if (uiContacts.isSearchResult) {
        for (String contactId : contactIds) {
          Contact contact = uiContacts.contactOpened.get(contactId);
          contact.setContactType(type);
          contact.setViewPermissionUsers(null);
          contact.setViewPermissionGroups(null);
        }
        // cs-2157
        if (pastedContact.size() > 0) {
          uiContacts.setContact(sharedContacts, false);
          uiContacts.pageList_.getAll().addAll(pastedContact);
        }
        if (contacts.size() > 0 && type.equals(DataStorage.SHARED)) {
          uiContacts.pageList_.getAll().removeAll(contacts);
          for (Contact contact : contacts) {
            uiContacts.pageList_.getAll()
                                .add(contactService.getSharedContactAddressBook(username,
                                                                                contact.getId()));
          }
        }
      /*  if(unMoveContacts.size() > 0){
          uiContacts.pageList_.getAll().addAll(unMoveContacts);
        }*/
        if (uiContacts.getSortedBy().equals(UIContacts.fullName)) {
          Collections.sort(uiContacts.pageList_.getAll(), new FullNameComparator());
        } else if (uiContacts.getSortedBy().equals(UIContacts.emailAddress)) {
          Collections.sort(uiContacts.pageList_.getAll(), new EmailComparator());
        } else if (uiContacts.getSortedBy().equals(UIContacts.jobTitle)) {
          Collections.sort(uiContacts.pageList_.getAll(), new JobTitleComparator());
        }
      } else if (uiContacts.isSelectSharedContacts && !ContactUtils.isEmpty(addressBookId)) {
        if (contacts.size() > 0)
          uiContacts.setContact(contacts, false);
        if (sharedContacts.size() > 0)
          uiContacts.setContact(sharedContacts, false);
      }
      uiContacts.setContacts(ContactUtils.getContactService().getSharedContacts(username));
      uiContacts.refreshData();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent());
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressBooks);
    }
  }

  static public class CopyContactActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      String contactId = event.getRequestContext().getRequestParameter(OBJECTID);
      List<String> contactIds = new ArrayList<String>();
      if (!ContactUtils.isEmpty(contactId)) {
        contactIds.add(contactId);
      } else {
        contactIds = uiContacts.getAllContactChecked();
        if (contactIds.size() == 0) {
          UIApplication uiApp = uiContacts.getAncestorOfType(UIApplication.class);
          uiApp.addMessage(new ApplicationMessage("UIContacts.msg.checkContact-toCopy",
                                                  null,
                                                  ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          return;
        }
      }
      UIAddressBooks uiAddressBooks = uiContacts.getAncestorOfType(UIWorkingContainer.class)
                                                .findFirstComponentOfType(UIAddressBooks.class);
      uiAddressBooks.setCopyAddress(null);
      Map<String, String> copyContacts = new LinkedHashMap<String, String>();
      for (String id : contactIds) {
        Contact contact = uiContacts.contactOpened.get(id);
        if (contact == null) {
          UIApplication uiApp = uiContacts.getAncestorOfType(UIApplication.class);
          uiApp.addMessage(new ApplicationMessage("UIContacts.msg.contact-deleted",
                                                  null,
                                                  ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent());
          return;
        }
        copyContacts.put(id, contact.getContactType());
      }
      uiAddressBooks.setCopyContacts(copyContacts);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressBooks);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent());
    }
  }

  static public class DeleteContactsActionListener extends EventListener<UIContacts> {
    @SuppressWarnings("unchecked")
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      String contactId = event.getRequestContext().getRequestParameter(OBJECTID);
      List<String> contactIds = new ArrayList<String>();
      UIApplication uiApp = uiContacts.getAncestorOfType(UIApplication.class);
      UIWorkingContainer uiWorkingContainer = uiContacts.getAncestorOfType(UIWorkingContainer.class);
      uiWorkingContainer.getAncestorOfType(UIContactPortlet.class).cancelAction();
      UIAddressBooks addressBooks = uiWorkingContainer.findFirstComponentOfType(UIAddressBooks.class);
      if (!ContactUtils.isEmpty(contactId) && !contactId.toString().equals("null")) {
        contactIds.add(contactId);
      } else {
        contactIds = uiContacts.getAllContactChecked();
        if (contactIds.size() == 0) {
          uiApp.addMessage(new ApplicationMessage("UIContacts.msg.checkContact-toDelete",
                                                  null,
                                                  ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent());
          return;
        }
      }
      for (String id : contactIds) {
        Contact contact = uiContacts.contactOpened.get(id);
        if (contact.getId().equals(ContactUtils.getCurrentUser())) {
          //raise unknown error when delete itself
          uiApp.addMessage(new ApplicationMessage("UIContacts.msg.cannot-delete-ownerContact",
                                                  null,
                                                  ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent());
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          return;
        } else if (contact.getContactType().equals(DataStorage.PUBLIC)) {
          uiApp.addMessage(new ApplicationMessage("UIContacts.msg.cannot-delete",
                                                  null,
                                                  ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent());
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          return;
        } else if (contact.getContactType().equals(DataStorage.SHARED)
            && uiContacts.isSharedAddress(contact)) {
          if (contact.isOwner()) {
            uiApp.addMessage(new ApplicationMessage("UIContacts.msg.cannot-delete",
                                                    null,
                                                    ApplicationMessage.WARNING));
            event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent());
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
            return;
          } else {
            String groupId = null;
            for (String add : contact.getAddressBookIds())
              if (addressBooks.getSharedGroups().containsKey(add))
                groupId = add;
            if (groupId != null && !addressBooks.havePermission(groupId)) {
              uiApp.addMessage(new ApplicationMessage("UIContacts.msg.cannot-delete",
                                                      null,
                                                      ApplicationMessage.WARNING));
              event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent());
              event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
              return;
            }
          }
        }
      }

      ContactService contactService = ContactUtils.getContactService();
      String username = ContactUtils.getCurrentUser();
      List<Contact> removedContacts = new ArrayList<Contact>();
      // cs- 1630
      Map<String, String> copyedContacts = addressBooks.getCopyContacts();

      // remove shared contacts
      for (String id : contactIds) {
        copyedContacts.remove(id);
        Contact contact = uiContacts.contactOpened.get(id);
        if (contact.getContactType().equals(DataStorage.SHARED)) {
          if (uiContacts.isSharedAddress(contact)) {
            String addressBookId = null;
            for (String add : contact.getAddressBookIds())
              if (uiContacts.getSharedGroupMap().containsKey(add))
                addressBookId = add;
            try {
              contactService.removeSharedContact(username, addressBookId, id);
            } catch (PathNotFoundException e) {
            }
          } else {
            try {
              String[] tags = contact.getTags();
              if (tags != null && tags.length > 0) {
                Set<String> tagsMap = uiWorkingContainer.findFirstComponentOfType(UITags.class)
                                                        .getTagMap()
                                                        .keySet();
                List<String> tagsList = new ArrayList<String>();
                tagsList.addAll(Arrays.asList(tags));
                for (String tagId : tags) {
                  if (tagsMap.contains(tagId)) {
                    tagsList.remove(tagId);
                    contact.setTags(tagsList.toArray(new String[] {}));
                    contactService.saveSharedContact(username, contact);
                  }
                }
              }
              contactService.removeUserShareContact(contact.getPath(), id, username);
            } catch (PathNotFoundException e) {
              uiApp.addMessage(new ApplicationMessage("UIContacts.msg.contact-not-existed",
                                                      null,
                                                      ApplicationMessage.WARNING));
              event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent());
              event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
              return;
            }
          }
          removedContacts.add(contact);
        }
      }
      if (!uiContacts.isSelectSharedContacts) {
        try{
          removedContacts.addAll(contactService.removeContacts(username, contactIds));
        }catch (Exception ex){
          uiApp = ContactUtils.initWarnPopup(uiContacts,"UIContacts.msg.noeditpermission");
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          return;
        }
      }
      if (ContactUtils.isEmpty(uiContacts.selectedGroup)
          && ContactUtils.isEmpty(uiContacts.selectedTag_)) {
        uiContacts.setContact(removedContacts, false);
      }
      if (uiContacts.getSelectedTag() != null) {
        String tagId = uiWorkingContainer.findFirstComponentOfType(UITags.class).getSelectedTag();
        DataPageList pageList = contactService.getContactPageListByTag(username, tagId);
        if (pageList != null) {
          List<Contact> contacts = new ArrayList<Contact>();
          contacts = pageList.getAll();
          if (uiContacts.getSortedBy().equals(UIContacts.fullName)) {
            Collections.sort(contacts, new FullNameComparator());
          } else if (uiContacts.getSortedBy().equals(UIContacts.emailAddress)) {
            Collections.sort(contacts, new EmailComparator());
          } else if (uiContacts.getSortedBy().equals(UIContacts.jobTitle)) {
            Collections.sort(contacts, new JobTitleComparator());
          }
          pageList.setList(contacts);
        }
        uiContacts.setContacts(pageList);
      } else {
        uiContacts.updateList();
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingContainer);
    }
  }

  static public class ExportContactActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      uiContacts.getAncestorOfType(UIContactPortlet.class).cancelAction();
      String contactId = event.getRequestContext().getRequestParameter(OBJECTID);
      String username = ContactUtils.getCurrentUser();
      ContactService contactService = ContactUtils.getContactService();
      List<Contact> contacts = new ArrayList<Contact>();
      Contact contact = uiContacts.contactInPage.get(contactId);
      contacts.add(contact);
      OutputStream out = contactService.getContactImportExports(contactService.getImportExportType()[0])
                                       .exportContact(username, contacts);
      String contentType = null;
      contentType = "text/x-vcard";
      ByteArrayInputStream is = new ByteArrayInputStream(out.toString().getBytes());
      DownloadResource dresource = new InputStreamDownloadResource(is, contentType);
      DownloadService dservice = (DownloadService) PortalContainer.getInstance()
                                                                  .getComponentInstanceOfType(DownloadService.class);
      dresource.setDownloadName(contact.getFullName() + ".vcf");
      String downloadLink = dservice.getDownloadLink(dservice.addDownloadResource(dresource));
      event.getRequestContext().getJavascriptManager().addJavascript("ajaxRedirect('" + downloadLink + "');");
    }
  }

  static public class SelectedContactActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      String contactId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIContactContainer uiContactContainer = uiContacts.getAncestorOfType(UIContactContainer.class);
      Contact oldContact = uiContacts.contactInPage.get(contactId);
      Contact newContact = null;
      ContactService service = ContactUtils.getContactService();
      String username = ContactUtils.getCurrentUser();
      if (oldContact.getContactType().equals(DataStorage.PERSONAL)) {
        newContact = service.getContact(username, contactId);
      } else if (oldContact.getContactType().equals(DataStorage.SHARED)) {
        newContact = service.getSharedContactAddressBook(username, contactId);
        if (newContact == null)
          newContact = service.getSharedContact(username, contactId);
      } else {
        newContact = service.getPublicContact(contactId);
      }
      if (newContact == null) {
        UIApplication uiApp = uiContacts.getAncestorOfType(UIApplication.class);
        uiApp.addMessage(new ApplicationMessage("UIContacts.msg.contact-deleted",
                                                null,
                                                ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        List<Contact> contacts = new ArrayList<Contact>();
        contacts.add(oldContact);
        uiContacts.setContact(contacts, false);
        uiContacts.updateList();
        event.getRequestContext().addUIComponentToUpdateByAjax(uiContactContainer);
        return;
      }
      uiContacts.contactInPage.put(contactId, newContact);
      uiContacts.setSelectedContact(contactId);
      UIContactPreview uiContactPreview = uiContactContainer.findFirstComponentOfType(UIContactPreview.class);
      uiContactPreview.setContact(newContact);
      uiContactPreview.setRendered(true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContactContainer);
    }
  }

  static public class ViewDetailsActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      String contactId = event.getRequestContext().getRequestParameter(OBJECTID);
      // cs-1278
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent());
      UIContactPortlet contactPortlet = uiContacts.getAncestorOfType(UIContactPortlet.class);
      UIPopupAction popupAction = contactPortlet.getChild(UIPopupAction.class);
      UIPopupContainer uiPopupContainer = popupAction.activate(UIPopupContainer.class, 800);
      uiPopupContainer.setId("ContactDetails");
      UIContactPreviewForm uiContactPreviewForm = uiPopupContainer.addChild(UIContactPreviewForm.class,
                                                                            null,
                                                                            null);
      uiContactPreviewForm.setPrintForm(false);
      uiContactPreviewForm.setContact(uiContacts.contactInPage.get(contactId));
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
    }
  }

  static public class PrintDetailsActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      uiContacts.isPrintDetail = true;
      String contactId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIContactPortlet contactPortlet = uiContacts.getAncestorOfType(UIContactPortlet.class);
      UIContactPreviewForm uiPreviewForm = contactPortlet.createUIComponent(UIContactPreviewForm.class, null, null);
      uiPreviewForm.setId("ContactDetails");
      uiPreviewForm.setPrintForm(true);
      uiPreviewForm.setContact(uiContacts.contactInPage.get(contactId));
      UIPopupAction popupAction = contactPortlet.getChild(UIPopupAction.class);
      popupAction.activate(uiPreviewForm, 800, 0);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
    }
  }

  
  static public class GotoPageActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      String stateClick = event.getRequestContext().getRequestParameter(OBJECTID).trim();
      if (uiContacts.pageList_ != null) {
        long currentPage = uiContacts.pageList_.getCurrentPage();
        long maxPage = uiContacts.pageList_.getAvailablePage();
        currentPage = (currentPage <= 0)?1:currentPage;
        if (stateClick.equals(FIRST_PAGE)) {
          uiContacts.pageList_.setCurrentPage(1);
        } else if (stateClick.equals(PREVIOUS_PAGE)) {
          uiContacts.pageList_.setCurrentPage((currentPage > 1) ? currentPage - 1 : 1);
        } else if (stateClick.equals(NEXT_PAGE)) {
          uiContacts.pageList_.setCurrentPage((currentPage + 1 > maxPage)?currentPage:currentPage + 1);
        } else if (stateClick.equals(LAST_PAGE)) {
          uiContacts.pageList_.setCurrentPage((maxPage > 0) ? maxPage : 1);
        } else {
          try {
            int number = Integer.parseInt(stateClick);
            if (number > 0 && number <= maxPage && number != currentPage) {
              uiContacts.pageList_.setCurrentPage(number);
            }
          } catch (NumberFormatException e) {
            UIApplication uiApp = ContactUtils.initPopup(uiContacts, "UIContacts.msg.numberFormatException", 
                                                         new String[] { stateClick }, ApplicationMessage.WARNING);
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
            return;
          }
        }
        
        List<String> checkedList = uiContacts.getCheckedCurrentPage(new ArrayList<String>());
        uiContacts.pageCheckedList.put(Long.valueOf(currentPage), checkedList);
        int checked = 0;
        for (int i = 1; i <= maxPage; i++) {
          if (uiContacts.pageCheckedList.get(Long.valueOf(i)) != null) {
            checked = checked + ((List<String>) uiContacts.pageCheckedList.get(Long.valueOf(i))).size();
          }
        }
        uiContacts.totalCheked = checked;
        uiContacts.updateList();
        event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent());
      } else {
        uiContacts.clearCheckedList();
      }
    }
  }

  static public class FirstPageActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      JCRPageList pageList = uiContacts.getContactPageList();
      if (pageList != null) {
        pageList.setCurrentPage(1);
        uiContacts.updateList();
        event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent());
      }
    }
  }

  static public class PreviousPageActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      JCRPageList pageList = uiContacts.getContactPageList();
      if (pageList != null && pageList.getCurrentPage() > 1) {
        pageList.setCurrentPage(pageList.getCurrentPage() - 1);
        uiContacts.updateList();
        event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent());
      }
    }
  }

  static public class NextPageActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      JCRPageList pageList = uiContacts.getContactPageList();
      if (pageList != null && pageList.getCurrentPage() < pageList.getAvailablePage()) {
        pageList.setCurrentPage(pageList.getCurrentPage() + 1);
        uiContacts.updateList();
        event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent());
      }
    }
  }

  static public class LastPageActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      JCRPageList pageList = uiContacts.getContactPageList();
      if (pageList != null) {
        pageList.setCurrentPage(pageList.getAvailablePage());
        uiContacts.updateList();
        event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent());
      }
    }
  }

  static public class SortActionListener extends EventListener<UIContacts> {
    @SuppressWarnings("unchecked")
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      String sortedBy = event.getRequestContext().getRequestParameter(OBJECTID);
      uiContacts.setAscending(!uiContacts.isAscending_);
      uiContacts.setSortedBy(sortedBy);
      uiContacts.setDefaultNameSorted(false);

      JCRPageList pageList = null;
      String group = uiContacts.selectedGroup;
      if (!ContactUtils.isEmpty(group)) {
        ContactFilter filter = new ContactFilter();
        filter.setViewQuery(uiContacts.getViewQuery());
        filter.setAscending(uiContacts.isAscending_);
        filter.setOrderBy(sortedBy);
        filter.setCategories(new String[] { group });

        UIAddressBooks addressBooks = uiContacts.getAncestorOfType(UIWorkingContainer.class)
                                                .findFirstComponentOfType(UIAddressBooks.class);
        if (addressBooks.getPrivateGroupMap().containsKey(group))
          pageList = ContactUtils.getContactService()
                                 .getPersonalContactsByFilter(ContactUtils.getCurrentUser(), filter);
        else if (addressBooks.getSharedGroups().containsKey(group))
          pageList = ContactUtils.getContactService()
                                 .getSharedContactsByFilter(ContactUtils.getCurrentUser(), filter);
        else
          pageList = ContactUtils.getContactService()
                                 .getPublicContactsByFilter(ContactUtils.getCurrentUser(), filter);

      } else { // selected group = null ;
        pageList = uiContacts.pageList_;
        if (pageList != null) {
          List<Contact> contacts = new ArrayList<Contact>();
          contacts = pageList.getAll();
          if (uiContacts.getSortedBy().equals(UIContacts.fullName)) {
            FullNameComparator.isAsc = (!FullNameComparator.isAsc);
            Collections.sort(contacts, new FullNameComparator());
          } else if (uiContacts.getSortedBy().equals(UIContacts.emailAddress)) {
            EmailComparator.isAsc = (!EmailComparator.isAsc);
            Collections.sort(contacts, new EmailComparator());
          } else if (uiContacts.getSortedBy().equals(UIContacts.jobTitle)) {
            JobTitleComparator.isAsc = (!JobTitleComparator.isAsc);
            Collections.sort(contacts, new JobTitleComparator());
          }
          pageList.setList(contacts);
        }
      }
      uiContacts.setContacts(pageList);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent());
    }
  }

  static public class FullNameComparator implements Comparator {
    public static boolean isAsc;

    public int compare(Object o1, Object o2) throws ClassCastException {
      String name1 = ((Contact) o1).getFullName();
      String name2 = ((Contact) o2).getFullName();
      if (isAsc == true)
        return name1.compareTo(name2);
      else
        return name2.compareTo(name1);
    }
  }

  static public class EmailComparator implements Comparator {
    private static boolean isAsc;

    public int compare(Object o1, Object o2) throws ClassCastException {
      String email1 = ContactUtils.listToString(((Contact) o1).getEmailAddresses());
      String email2 = ContactUtils.listToString(((Contact) o2).getEmailAddresses());
      if (ContactUtils.isEmpty(email1) || ContactUtils.isEmpty(email2))
        return 0;
      if (isAsc == true)
        return email1.compareTo(email2);
      else
        return email2.compareTo(email1);
    }
  }

  static public class JobTitleComparator implements Comparator {
    private static boolean isAsc;

    public int compare(Object o1, Object o2) throws ClassCastException {
      String job1 = ((Contact) o1).getJobTitle();
      String job2 = ((Contact) o2).getJobTitle();
      if (ContactUtils.isEmpty(job1) || ContactUtils.isEmpty(job2))
        return 0;
      if (isAsc == true)
        return job1.compareTo(job2);
      else
        return job2.compareTo(job1);
    }
  }

  static public class CloseSearchActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      uiContacts.setDisplaySearchResult(false);
      uiContacts.setSelectedGroup(uiContacts.selectedGroupBeforeSearch);
      uiContacts.setSelectedTag(uiContacts.selectedTagBeforeSearch_);
      uiContacts.setSelectSharedContacts(uiContacts.isSelectSharedContactsBeforeSearch);
      uiContacts.setViewContactsList(uiContacts.viewListBeforeSearch);
      uiContacts.refreshData();
      if (ContactUtils.isEmpty(uiContacts.selectedGroup)
          && ContactUtils.isEmpty(uiContacts.selectedTag_) && !uiContacts.isSelectSharedContacts)
        uiContacts.setContacts(null);
      UIWorkingContainer uiWorkingContainer = uiContacts.getAncestorOfType(UIWorkingContainer.class);
      uiWorkingContainer.findFirstComponentOfType(UIAddressBooks.class)
                        .setSelectedGroup(uiContacts.selectedGroup);
      uiWorkingContainer.findFirstComponentOfType(UITags.class)
                        .setSelectedTag(uiContacts.selectedTag_);
      UISearchForm uiSearchForm = uiContacts.getAncestorOfType(UIContactPortlet.class)
                                            .findFirstComponentOfType(UISearchForm.class);
      uiSearchForm.getChild(UIFormStringInput.class).setValue(null);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSearchForm);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingContainer);
    }
  }

  static public class SelectTagActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      String tagId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIWorkingContainer uiWorkingContainer = uiContacts.getAncestorOfType(UIWorkingContainer.class);
      uiWorkingContainer.findFirstComponentOfType(UIAddressBooks.class).setSelectedGroup(null);
      UITags tags = uiWorkingContainer.findFirstComponentOfType(UITags.class);
      tags.setSelectedTag(tagId);
      uiContacts.setContacts(ContactUtils.getContactService()
                                         .getContactPageListByTag(ContactUtils.getCurrentUser(),
                                                                  tagId));
      uiContacts.setSelectedGroup(null);
      uiContacts.setSelectedTag(tagId);
      uiContacts.setDisplaySearchResult(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingContainer);
    }
  }

  static public class SendEmailActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      String objectId = event.getRequestContext().getRequestParameter(OBJECTID);
      String emails = null;
      if (!ContactUtils.isEmpty(objectId)) {
        if (uiContacts.contactInPage.containsKey(objectId)) {
          String email = ContactUtils.listToString(uiContacts.contactInPage.get(objectId)
                                                                        .getEmailAddresses());
          if (!ContactUtils.isEmpty(email))
            emails = email.split(",")[0].split(";")[0];
        } else
          emails = objectId;
      } else {
        List<String> contactIds = uiContacts.getAllContactChecked();
        if (contactIds.size() < 1) {
          UIApplication uiApp = uiContacts.getAncestorOfType(UIApplication.class);
          uiApp.addMessage(new ApplicationMessage("UIContacts.msg.checkContact-toSendMail",
                                                  null,
                                                  ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          return;
        }
        StringBuffer buffer = new StringBuffer();
        for (String id : contactIds) {
          String email = uiContacts.contactOpened.get(id).getEmailAddress();
          if (!ContactUtils.isEmpty(email)) {
            if (buffer.length() > 0)
              buffer.append(", " + email);
            else
              buffer.append(email);
          }
        }
        emails = buffer.toString();
      }
      if (ContactUtils.isEmpty(emails)) {
        UIApplication uiApp = uiContacts.getAncestorOfType(UIApplication.class);
        uiApp.addMessage(new ApplicationMessage("UIContacts.msg.no-email-found",
                                                null,
                                                ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent());
      UIContactPortlet contactPortlet = uiContacts.getAncestorOfType(UIContactPortlet.class);
      UIPopupAction popupAction = contactPortlet.getChild(UIPopupAction.class);
      List<Account> acc = ContactUtils.getAccounts();
      UIComposeForm uiComposeForm = popupAction.activate(UIComposeForm.class, 850);
      uiComposeForm.init(acc, emails);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
    }
  }

  static public class SharedContactsActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      Map<String, Contact> mapContacts = new LinkedHashMap<String, Contact>();
      for (String contactId : uiContacts.getAllContactChecked()) {
        Contact contact = uiContacts.contactOpened.get(contactId);
        String contactType = contact.getContactType();
        if (contactType.equals(DataStorage.PUBLIC) || contactType.equals(DataStorage.SHARED)) {
          UIApplication uiApp = uiContacts.getAncestorOfType(UIApplication.class);
          uiApp.addMessage(new ApplicationMessage("UIContacts.msg.cannot-share",
                                                  null,
                                                  ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          return;
        }
        mapContacts.put(contactId, contact);
      }
      String objectId = event.getRequestContext().getRequestParameter(OBJECTID);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent());
      if (!ContactUtils.isEmpty(objectId) || uiContacts.getAllContactChecked().size() == 1) {
        if (ContactUtils.isEmpty(objectId))
          objectId = uiContacts.getAllContactChecked().get(0);
        UIContactPortlet contactPortlet = uiContacts.getAncestorOfType(UIContactPortlet.class);
        UIPopupAction popupAction = contactPortlet.getChild(UIPopupAction.class);
        UIPopupContainer uiPopupContainer = popupAction.activate(UIPopupContainer.class, 400);
        uiPopupContainer.setId("UIPermissionContactForm");
        UIAddEditPermission uiAddNewEditPermission = uiPopupContainer.addChild(UIAddEditPermission.class,
                                                                               null,
                                                                               null);
        // cs-2153
        Contact contact = ContactUtils.getContactService()
                                      .getContact(ContactUtils.getCurrentUser(), objectId);
        if (contact == null) {
          UIApplication uiApp = uiContacts.getAncestorOfType(UIApplication.class);
          uiApp.addMessage(new ApplicationMessage("UIContacts.msg.contact-deleted",
                                                  null,
                                                  ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent());
          return;
        }
        uiAddNewEditPermission.initContact(contact);
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
      } else {
        UIContactPortlet contactPortlet = uiContacts.getAncestorOfType(UIContactPortlet.class);
        UIPopupAction popupAction = contactPortlet.getChild(UIPopupAction.class);
        UIPopupContainer uiPopupContainer = popupAction.activate(UIPopupContainer.class, 600);
        uiPopupContainer.setId("UIPermissionContactsForm");
        UISharedContactsForm uiSharedForm = uiPopupContainer.addChild(UISharedContactsForm.class,
                                                                      null,
                                                                      null);
        uiSharedForm.init(mapContacts);
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
      }

    }
  }

  static public class PrintActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      List<String> contactIds = uiContacts.getAllContactChecked();
      if (contactIds.size() < 1) {
        UIApplication uiApp = uiContacts.getAncestorOfType(UIApplication.class);
        uiApp.addMessage(new ApplicationMessage("UIContacts.msg.checkContact-toPrint",
                                                null,
                                                ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      uiContacts.backupBeforePrint() ;
      uiContacts.contactInPage.clear();
      for (String contactId : contactIds){
        uiContacts.contactInPage.put(contactId, uiContacts.contactOpened.get(contactId));
      }
      uiContacts.viewListBeforePrint = uiContacts.viewContactsList;
      uiContacts.viewContactsList = false;
      uiContacts.isPrintForm = true;
      uiContacts.isPrintDetail = false;
      uiContacts.getAncestorOfType(UIContactContainer.class)
                .findFirstComponentOfType(UIContactPreview.class)
                .setRendered(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent());
    }
  }

  static public class CancelActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      uiContacts.isPrintForm = false;
      uiContacts.viewContactsList = uiContacts.viewListBeforePrint;
      uiContacts.contactInPage.clear();
      uiContacts.contactInPage.putAll(uiContacts.beforePrintMap);
      UIContactPreview contactPreview = uiContacts.getAncestorOfType(UIContactContainer.class)
                                                  .getChild(UIContactPreview.class);
      if(uiContacts.contactInPage.size() > 0) {
        Contact firstContact = uiContacts.contactInPage.values().iterator().next();
        contactPreview.setContact(firstContact);
        uiContacts.selectedContact = firstContact.getId();
      } else {
        contactPreview.setContact(null);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent());
    }
  }

  static public class RefreshActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      uiContacts.refreshData();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts.getParent());
    }
  }

}
