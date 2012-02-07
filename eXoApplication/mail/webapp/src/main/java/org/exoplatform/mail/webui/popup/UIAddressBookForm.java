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
package org.exoplatform.mail.webui.popup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.jcr.PathNotFoundException;

import org.exoplatform.contact.service.AddressBook;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactFilter;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.DataStorage;
import org.exoplatform.contact.service.SharedAddressBook;
import org.exoplatform.contact.service.Tag;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.cs.common.webui.UIPopupAction;
import org.exoplatform.cs.common.webui.UIPopupActionContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.mail.DataCache;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItem;
import org.exoplatform.webui.core.model.SelectOption;
import org.exoplatform.webui.core.model.SelectOptionGroup;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormSelectBoxWithGroups;

/**
 * Created by The eXo Platform SARL Author : Phung Nam phunghainam@gmail.com Nov
 * 01, 2007 8:48:18 AM
 */
@ComponentConfig(
                 lifecycle = UIFormLifecycle.class, 
                 template = "app:/templates/mail/webui/popup/UIAddressBookForm.gtmpl", 
                 events = {
                   @EventConfig(listeners = UIAddressBookForm.AddNewGroupActionListener.class),
                   @EventConfig(listeners = UIAddressBookForm.AddContactActionListener.class),
                   @EventConfig(listeners = UIAddressBookForm.EditContactActionListener.class),
                   @EventConfig(listeners = UIAddressBookForm.ChangeGroupActionListener.class),
                   @EventConfig(listeners = UIAddressBookForm.CheckContactActionListener.class),
                   @EventConfig(listeners = UIAddressBookForm.CheckAllContactActionListener.class),
                   @EventConfig(listeners = UIAddressBookForm.SelectContactActionListener.class),
                   @EventConfig(listeners = UIAddressBookForm.SendEmailActionListener.class),
                   @EventConfig(listeners = UIAddressBookForm.DeleteContactActionListener.class, confirm = "UIAddressBookForm.msg.confirm-remove-contact"),
                   @EventConfig(listeners = UIAddressBookForm.CloseActionListener.class),
                   @EventConfig(listeners = UIAddressBookForm.SendMultiEmailActionListener.class) 
                 }
)
public class UIAddressBookForm extends UIForm implements UIPopupComponent {
  private static final Log log = ExoLogger.getExoLogger(UIAddressBookForm.class);
  
  public final static String             ALL_GROUP          = "All group".intern();

  public final static String             SELECT_GROUP       = "select-group".intern();

  public final static String             SELECT_ALL         = "select-all".intern();

  private Contact                        selectedContact;

  private LinkedHashMap<String, Contact> contactMap_        = new LinkedHashMap<String, Contact>();

  private List<Contact>                  contactList_       = new ArrayList<Contact>();

  private HashMap<String, String>        checkedContactMap  = new LinkedHashMap<String, String>();

  static private Map<String, Contact>    selectedContactMap = new HashMap<String, Contact>();

  private String                         sharedContacts_    = "sharedContacts";

  public UIAddressBookForm() throws Exception {
    UIFormSelectBoxWithGroups uiSelectGroup = new UIFormSelectBoxWithGroups(SELECT_GROUP,
                                                                            SELECT_GROUP,
                                                                            getOptions());
    uiSelectGroup.setOnChange("ChangeGroup");
    addUIFormInput(uiSelectGroup);

    UIFormCheckBoxInput<Boolean> uiSelectAll = new UIFormCheckBoxInput<Boolean>(SELECT_ALL, SELECT_ALL, false);
    uiSelectAll.setOnChange("CheckAllContact");
    addUIFormInput(uiSelectAll);
    refrestContactList(uiSelectGroup.getValue(), null);
    for (Contact contact : contactList_) {
      String value = checkedContactMap.get(contact.getId());
      if (value.equals("1"))
        selectedContactMap.put(contact.getId(), contact);
    }
  }

  public boolean havePermission(Contact contact) throws Exception {
    if (!contact.getContactType().equals(DataStorage.SHARED))
      return true;
    // contact shared
    String currentUser = MailUtils.getCurrentUser();
    if (contact.getEditPermissionUsers() != null
        && Arrays.asList(contact.getEditPermissionUsers()).contains(currentUser
                                                                    + DataStorage.HYPHEN)) {
      return true;
    }
    String[] editPerGroups = contact.getEditPermissionGroups();
    if (editPerGroups != null)
      for (String editPer : editPerGroups)
        if (MailUtils.getUserGroups().contains(editPer)) {
          return true;
        }

    if (!getChild(UIFormSelectBoxWithGroups.class).getValue().equals(sharedContacts_)) {
      if (MailUtils.havePermission(getChild(UIFormSelectBoxWithGroups.class).getValue()))
        return true;
    }
    return false;
  }

  public String getPortalName() {
    PortalContainer pcontainer = PortalContainer.getInstance();
    return pcontainer.getPortalContainerInfo().getContainerName();
  }

  public String getRepository() throws Exception {
    RepositoryService rService = getApplicationComponent(RepositoryService.class);
    return rService.getCurrentRepository().getConfiguration().getName();
  }

  public List<SelectItem> getOptions() throws Exception {
    String username = MailUtils.getCurrentUser();
    ContactService contactSrv = getApplicationComponent(ContactService.class);
    List<SelectItem> options = new ArrayList<SelectItem>();
    SelectOptionGroup personalContacts = new SelectOptionGroup("personal-contacts");
    for (AddressBook pcg : contactSrv.getGroups(username)) {
      personalContacts.addOption(new SelectOption(pcg.getName(), pcg.getId()));
    }
    options.add(personalContacts);

    SelectOptionGroup sharedContacts = new SelectOptionGroup("shared-contacts");
    for (SharedAddressBook scg : contactSrv.getSharedAddressBooks(username)) {
      sharedContacts.addOption(new SelectOption(MailUtils.getDisplayAdddressShared(scg.getSharedUserId(), scg.getName()), scg.getId()));
    }
    sharedContacts.addOption(new SelectOption(sharedContacts_, sharedContacts_));
    options.add(sharedContacts);
    /*
     * SelectItemOptionGroup publicContacts = new
     * SelectItemOptionGroup("public-contacts"); for(String publicCg :
     * MailUtils.getUserGroups()) { publicContacts.addOption(new
     * org.exoplatform.mail.webui.SelectItemOption<String>(publicCg, publicCg))
     * ; } options.add(publicContacts);
     */

    //Tag's contact
    List<Tag> tags = contactSrv.getTags(username);
    if(tags.size() > 0){
      SelectOptionGroup tagContacts = new SelectOptionGroup("tag-contacts");
      for (Tag tag : tags) {
        tagContacts.addOption(new SelectOption(tag.getName(), tag.getId()));
      }
      options.add(tagContacts); 
    }

    return options;
  }

  public Contact getSelectedContact() {
    return this.selectedContact;
  }

  public void setSelectedContact(Contact contact) {
    this.selectedContact = contact;
  }

  public HashMap<String, String> getCheckedContactMap() {
    return checkedContactMap;
  }

  public void flipFlopCheckedContactMap(String contactId) {
    for (Entry<String, String> entry : checkedContactMap.entrySet()) {
      if (entry.getKey().equals(contactId)) {
        if (entry.getValue().equals("0"))
          checkedContactMap.put(entry.getKey(), "1");
        else
          checkedContactMap.put(entry.getKey(), "0");
        break;
      }
    }
  }

  public boolean isCheckedContact(String contactId) {
    for (Entry<String, String> entry : checkedContactMap.entrySet()) {
      if (entry.getKey().equals(contactId)) {
        if (entry.getValue().equals("0"))
          return false;
        else
          return true;
      }
    }
    return false;
  }
  public void checkAll() throws Exception{
    boolean isSelectAll = getUIFormCheckBoxInput(UIAddressBookForm.SELECT_ALL).isChecked();
    List<Contact> contactList = getContacts();
    if(isSelectAll){
      for (Contact contact : contactList) {
        checkedContactMap.put(contact.getId(),"1");
        selectedContactMap.put(contact.getId(),contact);
      }        
    }else {
      for (Contact contact : contactList) {
        checkedContactMap.put(contact.getId(),"0");
        selectedContactMap.clear();
      }
    }
  }
  
  public boolean isCheckedAllContact(){
    return getUIFormCheckBoxInput(UIAddressBookForm.SELECT_ALL).isChecked();
  }

  public void setCheckAll() throws Exception{
    List<Contact> contactList = getContacts();
    int i = 0;
    for (Entry<String, String> entry : checkedContactMap.entrySet()) {
      if (entry.getValue().equals("1")) i++; 
    }
    if(i == contactList.size()){
      getUIFormCheckBoxInput(UIAddressBookForm.SELECT_ALL).setChecked(true);
      return ;
    }
    getUIFormCheckBoxInput(UIAddressBookForm.SELECT_ALL).setChecked(false);
  }
  public DownloadService getDownloadService() {
    return getApplicationComponent(DownloadService.class);
  }

  public List<Contact> getContacts() throws Exception {
    return contactList_;
  }

  public void refrestContactList(String groupId, Contact contact) throws Exception {
    String username = MailUtils.getCurrentUser();
    ContactService contactSrv = getApplicationComponent(ContactService.class);
    List<Contact> contactList = new ArrayList<Contact>();
    ContactFilter ctFilter = new ContactFilter();
    ctFilter.setOrderBy("fullName");
    ctFilter.setAscending(true);

    if (groupId != null && groupId.trim().length() > 0) {
      SelectOptionGroup privateGroups = (SelectOptionGroup) getChild(UIFormSelectBoxWithGroups.class).getOptions().get(0);
      for (SelectOption option : privateGroups.getOptions())
        if (option.getValue().equals(groupId)) {
          ctFilter.setType(DataStorage.PERSONAL);
          break;
        }

      if (ctFilter.getType() == null)
        ctFilter.setType(DataStorage.SHARED);

      if (!groupId.equals(sharedContacts_))
        ctFilter.setCategories(new String[] { groupId });
      else
        ctFilter.setSearchSharedContacts(true);


      if(groupId.substring(0,3).equalsIgnoreCase("Tag")){
        ctFilter.setTag(new String[]{groupId});
      }

      contactList = contactSrv.searchContact(username, ctFilter).getAll();      
    } else {
      ctFilter.setType(DataStorage.PERSONAL);
      ctFilter.setCategories(new String[] { ((SelectOptionGroup) getChild(UIFormSelectBoxWithGroups.class).getOptions()
          .get(0)).getOptions()
          .get(0)
          .getValue() });
      contactList = contactSrv.searchContact(username, ctFilter).getAll();
    }
    contactMap_.clear();
    for (Contact ct : contactList) {
      String id = ct.getId();
      contactMap_.put(id, ct);
    }
    contactList_ = new ArrayList<Contact>(contactMap_.values());
    if (contactList_.size() > 0) {
      if (contact != null)
        selectedContact = contact;
      else
        selectedContact = contactList_.get(0);
      for (Contact cont : contactList_) {
        String id = cont.getId();
        String checkedContact = checkedContactMap.get(id);
        if (checkedContact != null) {
          checkedContactMap.put(id, checkedContactMap.get(id));
        } else {
          checkedContactMap.put(id, "0");
        }
        selectedContactMap.put(id, contactMap_.get(id));
      }
    } else
      selectedContact = null;
  }

  public void updateGroup(String selectedGroup) throws Exception {
    ((UIFormSelectBoxWithGroups) getChildById(SELECT_GROUP)).setOptions(getOptions());
    ((UIFormSelectBoxWithGroups) getChildById(SELECT_GROUP)).setValue(selectedGroup);
  }

  public String[] getActions() {
    return new String[] { "Close" };
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  static public class AddNewGroupActionListener extends EventListener<UIAddressBookForm> {
    public void execute(Event<UIAddressBookForm> event) throws Exception {
      UIAddressBookForm uiAddressBookForm = event.getSource();
      UIPopupActionContainer popupContainer = uiAddressBookForm.getParent();
      UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class);
      popupAction.activate(UIAddGroupForm.class, 600);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
    }
  }

  static public class AddContactActionListener extends EventListener<UIAddressBookForm> {
    public void execute(Event<UIAddressBookForm> event) throws Exception {
      UIAddressBookForm uiAddressBookForm = event.getSource();
      UIPopupActionContainer uiActionContainer = uiAddressBookForm.getParent();
      UIPopupAction uiChildPopup = uiActionContainer.getChild(UIPopupAction.class);
      UIPopupActionContainer uiPopupContainer = uiChildPopup.activate(UIPopupActionContainer.class,
                                                                      730);
      uiPopupContainer.setId("UIPopupAddContactForm");
      UIAddContactForm uiContactForm = uiPopupContainer.addChild(UIAddContactForm.class, null, null);
      // cs-2082
      String groupId = ((UIFormSelectBoxWithGroups) uiAddressBookForm.getChildById(SELECT_GROUP)).getValue();
      UIFormSelectBoxWithGroups e = ((UIFormSelectBoxWithGroups) uiContactForm.getChildById(SELECT_GROUP));
      e.setSelectedValues(new String[] { groupId });
      event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup);
    }
  }

  static public class EditContactActionListener extends EventListener<UIAddressBookForm> {
    public void execute(Event<UIAddressBookForm> event) throws Exception {
      UIAddressBookForm uiAddBook = event.getSource();
      Contact selectedContact = uiAddBook.getSelectedContact();      
      String groupId = ((UIFormSelectBoxWithGroups) uiAddBook.getChildById(SELECT_GROUP)).getValue();
      if (selectedContact != null) {
        if (selectedContact.getContactType().equals("1")
            && !uiAddBook.havePermission(selectedContact)) {
          event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIAddressBookForm.msg.cannot-edit",
                                                                                         null));
          return;
        }
        UIPopupActionContainer uiActionContainer = uiAddBook.getParent();
        UIPopupAction uiChildPopup = uiActionContainer.getChild(UIPopupAction.class);
        UIPopupActionContainer uiPopupContainer = uiChildPopup.activate(UIPopupActionContainer.class,
                                                                        730);
        uiPopupContainer.setId("UIPopupAddContactForm");
        UIAddContactForm uiAddContact = uiPopupContainer.createUIComponent(UIAddContactForm.class,
                                                                           null,
                                                                           null);
        uiPopupContainer.addChild(uiAddContact);
        if (selectedContact.getContactType().equals("1")
            && uiAddBook.getChild(UIFormSelectBoxWithGroups.class)
            .getValue()
            .equals(uiAddBook.sharedContacts_)) {
          uiAddContact.fillDatas(selectedContact, groupId);
          ((UIFormSelectBoxWithGroups) uiAddContact.getChildById(SELECT_GROUP)).getOptions()
          .clear();
          List<SelectItem> options = new ArrayList<SelectItem>();
          SelectOptionGroup personalContacts = new SelectOptionGroup("personal-contacts");
          personalContacts.addOption(new SelectOption(uiAddBook.sharedContacts_,
                                                      uiAddBook.sharedContacts_));
          options.add(personalContacts);
          ((UIFormSelectBoxWithGroups) uiAddContact.getChildById(SELECT_GROUP)).setOptions(options);
        } else {
          uiAddContact.fillDatas(selectedContact, groupId);
        }
        event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup);
        uiAddBook.refrestContactList(groupId, selectedContact);
      } else {
        event.getRequestContext()
             .getUIApplication()
             .addMessage(new ApplicationMessage("UIAddressBookForm.msg.no-selected-contact-to-edit", null));        
        return;
      }
    }
  }

  static public class SelectContactActionListener extends EventListener<UIAddressBookForm> {
    public void execute(Event<UIAddressBookForm> event) throws Exception {
      UIAddressBookForm uiAddressBook = event.getSource();
      String contactId = event.getRequestContext().getRequestParameter(OBJECTID);
      Contact contact = uiAddressBook.contactMap_.get(contactId);
      uiAddressBook.setSelectedContact(contact);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressBook.getParent());
    }
  }

  public void setSelectedContactMap(UIAddressBookForm uiAddressBook, String contactId) {
    // if (!uiAddressBook.isCheckedContact(contactId)) {
    // Contact contact = uiAddressBook.contactMap_.get(contactId);
    // selectedContactMap.put(contactId, contact);
    // } else {
    // if (selectedContactMap.containsKey(contactId))
    // selectedContactMap.remove(contactId);
    // }
    if (uiAddressBook.isCheckedContact(contactId)) {
      Contact contact = uiAddressBook.contactMap_.get(contactId);
      selectedContactMap.put(contactId, contact);
    } else {
      if (selectedContactMap.containsKey(contactId))
        selectedContactMap.remove(contactId);
    }
  }

  static public class CheckContactActionListener extends EventListener<UIAddressBookForm> {
    public void execute(Event<UIAddressBookForm> event) throws Exception {
      UIAddressBookForm uiAddressBook = event.getSource();
      String contactId = event.getRequestContext().getRequestParameter(OBJECTID);
      uiAddressBook.setSelectedContactMap(uiAddressBook, contactId);
      uiAddressBook.flipFlopCheckedContactMap(contactId);
      uiAddressBook.setCheckAll();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressBook.getParent());
    }
  }

  static public class DeleteContactActionListener extends EventListener<UIAddressBookForm> {
    public void execute(Event<UIAddressBookForm> event) throws Exception {
      UIAddressBookForm uiAddressBook = event.getSource();
      boolean isSelectAll = uiAddressBook.getUIFormCheckBoxInput(UIAddressBookForm.SELECT_ALL).isChecked();
      SelectOptionGroup privateGroups = (SelectOptionGroup) uiAddressBook.getChild(UIFormSelectBoxWithGroups.class).getOptions().get(0);
      Contact selectedContact = uiAddressBook.getSelectedContact();
      List<Contact> contactList = new ArrayList<Contact>();
      if (isSelectAll) {
        contactList.addAll(uiAddressBook.contactList_);
      } else {
        for (Contact contact : uiAddressBook.getContacts()) {
          if (uiAddressBook.isCheckedContact(contact.getId())) {
            contactList.add(contact);
          }
        }
      }

      if (contactList.size() == 0) {
        event.getRequestContext()
             .getUIApplication()
             .addMessage(new ApplicationMessage("UIAddressBookForm.msg.no-selected-contact-to-delete", null));       
        return;
      }

      for (Contact contact : contactList) {
        if (contact != null) {
          String username = MailUtils.getCurrentUser();
          ContactService contactServ = uiAddressBook.getApplicationComponent(ContactService.class);
          try {
            boolean isOk = false;
            if (contact.isOwner()) {
              event.getRequestContext()
                   .getUIApplication()
                   .addMessage(new ApplicationMessage("UIAddressBookForm.msg.cannot-delete-ownerContact", null));              
              // return;
            } else {
              boolean isPrivate = false;

              for (SelectOption option : privateGroups.getOptions()) {
                if (option.getValue().equals(contact.getAddressBookIds()[0])) {
                  isPrivate = true;
                  List<String> contactIds = new ArrayList<String>();
                  String id = contact.getId();
                  if (!id.equals(MailUtils.getCurrentUser())) {
                    contactIds.add(id);
                    contactServ.removeContacts(username, contactIds);
                    selectedContactMap.remove(id);
                  }
                }
              }
              
              if (!isPrivate) {
                if (((UIFormSelectBoxWithGroups) uiAddressBook.getChildById(SELECT_GROUP)).getValue()
                    .equals(uiAddressBook.sharedContacts_)) {
                  contactServ.removeUserShareContact(contact.getAuthor(), contact.getId(), username);                 
                } else {
                  event.getRequestContext()
                       .getUIApplication()
                       .addMessage(new ApplicationMessage("UIAddressBookForm.msg.cannot-delete", null));
                  
                }
              }
              isOk = true;
            }
            if (isOk) {
              String groupId = ((UIFormSelectBoxWithGroups) uiAddressBook.getChildById(SELECT_GROUP)).getValue();
              if (selectedContact.getId().equals(contact.getId()))
                uiAddressBook.refrestContactList(groupId, null);
              else
                uiAddressBook.refrestContactList(groupId, selectedContact);
              event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressBook.getParent());
            }
          } catch (Exception e) {
            if (log.isDebugEnabled()) {
              log.debug("Exception in method execute of class DeleteContactActionListener", e);
            }
          }
        }
      }
    }
  }

  static public class ChangeGroupActionListener extends EventListener<UIAddressBookForm> {
    public void execute(Event<UIAddressBookForm> event) throws Exception {
      UIAddressBookForm uiAddressBook = event.getSource();
      String selectedGroupId = ((UIFormSelectBoxWithGroups) uiAddressBook.getChildById(SELECT_GROUP)).getValue();
      Contact contact = uiAddressBook.getSelectedContact();
      if (contact != null) {
        uiAddressBook.refrestContactList(selectedGroupId, contact);
      } else {
        uiAddressBook.refrestContactList(selectedGroupId, null);
      }

      ((UIFormSelectBoxWithGroups) uiAddressBook.getChildById(SELECT_GROUP)).setValue(selectedGroupId);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressBook.getParent());
    }
  }

  static public class CloseActionListener extends EventListener<UIAddressBookForm> {
    public void execute(Event<UIAddressBookForm> event) throws Exception {
      UIAddressBookForm uiAddressBookForm = event.getSource();
      uiAddressBookForm.getAncestorOfType(UIMailPortlet.class).cancelAction();
    }
  }

  static public class SendEmailActionListener extends EventListener<UIAddressBookForm> {
    public void execute(Event<UIAddressBookForm> event) throws Exception {
      UIAddressBookForm uiForm = event.getSource();
      
      String email = event.getRequestContext().getRequestParameter(OBJECTID);
      if (!MailUtils.isFieldEmpty(email)) {
        DataCache dataCache = (DataCache) WebuiRequestContext.getCurrentInstance().getAttribute(DataCache.class);
        String accId = dataCache.getSelectedAccountId();
        if (Utils.isEmptyField(accId)) {          
          event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIActionBar.msg.account-list-empty",
                                                                                         null));          
          return;
        }

        UIPopupActionContainer uiActionContainer = uiForm.getParent();
        UIPopupAction uiChildPopup = uiActionContainer.getChild(UIPopupAction.class);
        UIPopupActionContainer uiPopupContainer = uiChildPopup.activate(UIPopupActionContainer.class,
                                                                        MailUtils.MAX_POPUP_WIDTH);
        uiPopupContainer.setId("UIPopupActionComposeContainer");
        UIComposeForm uiComposeForm = uiPopupContainer.addChild(UIComposeForm.class, null, null);
        uiComposeForm.init(accId, null, 0);
        uiComposeForm.setFieldToValue(email);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup);
      }
    }
  }
  
  static public class CheckAllContactActionListener extends EventListener<UIAddressBookForm> {
    public void execute(Event<UIAddressBookForm> event) throws Exception {
      UIAddressBookForm uiAddressBook = event.getSource();
      UIFormCheckBoxInput<Boolean> uiFormCheckBox = uiAddressBook.getUIFormCheckBoxInput(UIAddressBookForm.SELECT_ALL);
      boolean bAllChecked = uiFormCheckBox.isChecked();
      uiFormCheckBox.setChecked(!bAllChecked);
      uiAddressBook.checkAll();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressBook.getParent());
    }
  }
  
  static public class SendMultiEmailActionListener extends EventListener<UIAddressBookForm> {
    public void execute(Event<UIAddressBookForm> event) throws Exception {
      UIAddressBookForm uiForm = event.getSource();
      Set<String> emailSet = new TreeSet<String>();
      
      boolean isSelectAll = uiForm.getUIFormCheckBoxInput(UIAddressBookForm.SELECT_ALL).isChecked();
      if (isSelectAll) {
        for (Contact contact : uiForm.contactList_) {
          String email = contact.getEmailAddress();
          if (email != null && email.length() > 0)
            emailSet.add(email);
        }
      } else {
        for (Contact contact : uiForm.getContacts()) {
          if (uiForm.isCheckedContact(contact.getId())) {
            String emailAddress = contact.getEmailAddress();
            if (emailAddress != null && emailAddress.length() > 0) {
              emailSet.add(emailAddress.trim());
            }
          }
        }
      }
      
      if (!emailSet.isEmpty()) {
        DataCache dataCache = (DataCache) WebuiRequestContext.getCurrentInstance().getAttribute(DataCache.class);
        String accId = dataCache.getSelectedAccountId();
        if (Utils.isEmptyField(accId)) {
          event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIActionBar.msg.account-list-empty",
                                                                                         null));          
          return;
        }
        UIPopupActionContainer uiActionContainer = uiForm.getParent();
        UIPopupAction uiChildPopup = uiActionContainer.getChild(UIPopupAction.class);
        UIPopupActionContainer uiPopupContainer = uiChildPopup.activate(UIPopupActionContainer.class,
                                                                        MailUtils.MAX_POPUP_WIDTH);
        uiPopupContainer.setId("UIPopupActionComposeContainer");
        UIComposeForm uiComposeForm = uiPopupContainer.addChild(UIComposeForm.class, null, null);
        uiComposeForm.init(accId, null, 0);
        StringBuffer emails = new StringBuffer();
        for (String email : emailSet) {
          if (emails.length() > 0)
            emails.append(", ");
          emails.append(email);
        }
        uiComposeForm.setFieldToValue(emails.toString());
        event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup);
      } else {
        
        event.getRequestContext()
             .getUIApplication()
             .addMessage(new ApplicationMessage("UIAddressBookForm.msg.no-selected-contact-to-send-mail", null));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIPopupAction.class));
      }
    }
  }
}
