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

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.commons.utils.ListAccessImpl;
import org.exoplatform.contact.service.AddressBook;
import org.exoplatform.contact.service.ContactFilter;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.DataPageList;
import org.exoplatform.contact.service.DataStorage;
import org.exoplatform.contact.service.SharedAddressBook;
import org.exoplatform.contact.service.Utils;
import org.exoplatform.contact.service.impl.NewUserListener;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.cs.common.webui.UIPopupAction;
import org.exoplatform.cs.common.webui.UIPopupActionContainer;
import org.exoplatform.cs.common.webui.UIPopupComponent;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.webui.CalendarUtils;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItem;
import org.exoplatform.webui.core.model.SelectOption;
import org.exoplatform.webui.core.model.SelectOptionGroup;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormSelectBoxWithGroups;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL Author : Phung Nam <phunghainam@gmail.com>
 * Sep 25, 2007
 */
@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "app:/templates/mail/webui/popup/UIAddressForm.gtmpl", events = {
    @EventConfig(listeners = UIAddressForm.ChangeGroupActionListener.class, phase = Phase.DECODE),
    @EventConfig(listeners = UIAddressForm.SearchContactActionListener.class),
    @EventConfig(listeners = UIAddressForm.AddActionListener.class),
    @EventConfig(listeners = UIAddressForm.ReplaceActionListener.class),
    @EventConfig(listeners = UIAddressForm.ShowPageActionListener.class, phase = Phase.DECODE),
    @EventConfig(listeners = UIAddressForm.CancelActionListener.class, phase = Phase.DECODE) })
public class UIAddressForm extends UIForm implements UIPopupComponent {
  private static final Log log = ExoLogger.getExoLogger(UIAddressForm.class);
  
  public static final String                CONTACT_SEARCH     = "contact-search".intern();

  public static final String                CONTACT_GROUP      = "contact-group".intern();

  public static final String                SELECTED_GROUP     = "selected-group".intern();

  public LinkedHashMap<String, ContactData> checkedList_       = new LinkedHashMap<String, ContactData>();

  public LinkedHashMap<String, ContactData> newCheckedList_    = new LinkedHashMap<String, ContactData>();

  private String                            avaiAddressStr     = "";

  private Map<String, String>               addressBooksMap    = new HashMap<String, String>();

  private String                            selectedAddressId_ = "";

  private String                            recipientsType_    = "";

  private UIPageIterator                    uiPageList_;

  public final String                       all                = "all";

  private final String                      allContacts        = "All Contact";

  private final static String               PERSONAL           = "0".intern();

  private final static String               SHARED             = "1".intern();

  private final static String               PUBLIC             = "2".intern();

  public final String                       sharedContacts_    = "sharedContacts";

  public final String                       publicContact      = "public-contacts";

  public final String                       personnalContact   = "personal-contacts";

  public Map<String, String> getAddressBooksMap() {
    return addressBooksMap;
  }

  public void setRecipientsType(String type) {
    recipientsType_ = type;
  }

  public String getRecipientType() {
    return recipientsType_;
  }

  public void setAvaiAddressStr(String str) {
    avaiAddressStr = str;
  }

  public String getAvaiAddressStr() {
    return avaiAddressStr;
  }

  public UIAddressForm() throws Exception {
    addUIFormInput(new UIFormCheckBoxInput<Boolean>(SELECTED_GROUP, SELECTED_GROUP, false));
    addUIFormInput(new UIFormStringInput(CONTACT_SEARCH, CONTACT_SEARCH, null));
    UIFormSelectBoxWithGroups uiSelect = new UIFormSelectBoxWithGroups(CONTACT_GROUP,
                                                                       CONTACT_GROUP,
                                                                       getOptions());
    uiSelect.setOnChange("ChangeGroup");
    addUIFormInput(uiSelect);
    uiSelect.setValue(NewUserListener.DEFAULTGROUP + MailUtils.getCurrentUser());
    uiPageList_ = new UIPageIterator();
    uiPageList_.setId("UIMailAddressPage");
    // cs-1267
    /*
     * String username = MailUtils.getCurrentUser(); ContactService contactSrv =
     * getApplicationComponent(ContactService.class); List<AddressBook> groups =
     * contactSrv.getGroups(SessionProviderFactory.createSystemProvider(),
     * username) ; if (groups != null && groups.size() > 0) { String category =
     * groups.get(0).getId() ; setContactList(category) ; }
     */
    ContactFilter filter = new ContactFilter();
    filter.setCategories(new String[] { NewUserListener.DEFAULTGROUP + MailUtils.getCurrentUser() });
    setContactList(filter);
  }

  public List<SelectItem> getOptions() throws Exception {
    String username = MailUtils.getCurrentUser();
    ContactService contactSrv = getApplicationComponent(ContactService.class);
    List<SelectItem> options = new ArrayList<SelectItem>();
    options.add(new SelectOption(all, all));
    addressBooksMap.put(all, allContacts + "_" + all);
    List<AddressBook> contactGroup = contactSrv.getGroups(username);
    if (!contactGroup.isEmpty()) {
      SelectOptionGroup personalContacts = new SelectOptionGroup(personnalContact);
      for (AddressBook pcg : contactGroup) {
        personalContacts.addOption(new SelectOption(pcg.getName(), pcg.getId()));
        addressBooksMap.put(pcg.getId(), pcg.getName() + "_" + PERSONAL);
      }
      options.add(personalContacts);
    }

    DataPageList sharedContacts = contactSrv.getSharedContacts(username);
    List<SharedAddressBook> sharedAdds = contactSrv.getSharedAddressBooks(username);
    if (!sharedAdds.isEmpty() || sharedContacts.getAll().size() > 0) {
      SelectOptionGroup sharedAddress = new SelectOptionGroup("shared");
      for (SharedAddressBook scg : sharedAdds) {
        String name = "";
        if (!CalendarUtils.isEmpty(scg.getSharedUserId()))
          name = scg.getSharedUserId() + "-";
        sharedAddress.addOption(new SelectOption(name + scg.getName(), scg.getId()));
        addressBooksMap.put(scg.getId(), scg.getName() + "_" + SHARED);
      }
      sharedAddress.addOption(new SelectOption(sharedContacts_, sharedContacts_));
      options.add(sharedAddress);
    }

    OrganizationService organizationService = (OrganizationService) PortalContainer.getComponent(OrganizationService.class);

    List<String> publicAddressBookIdsOfUser = contactSrv.getPublicAddressBookIdsOfUser(null);
    if (!publicAddressBookIdsOfUser.isEmpty()) {
      SelectOptionGroup publicContacts = new SelectOptionGroup(publicContact);
      for (String publicCg : publicAddressBookIdsOfUser) {
        publicContacts.addOption(new SelectOption(organizationService.getGroupHandler()
                                                                     .findGroupById(publicCg)
                                                                     .getGroupName(), publicCg));
        addressBooksMap.put(publicCg, organizationService.getGroupHandler()
                                                         .findGroupById(publicCg)
                                                         .getGroupName()
            + "_" + PUBLIC);
      }
      options.add(publicContacts);
    }

    List<String> publicAddressBookIds = contactSrv.getAllsPublicAddressBookIds(null);
    publicAddressBookIds.removeAll(publicAddressBookIdsOfUser);
    if (!publicAddressBookIds.isEmpty()) {
      SelectOptionGroup publicContacts = new SelectOptionGroup("public-groups-contacts");
      for (String publicCg : publicAddressBookIds) {
        publicContacts.addOption(new SelectOption(publicCg, publicCg));
        addressBooksMap.put(publicCg, publicCg + "_" + PUBLIC);
      }
      options.add(publicContacts);
    }

    return options;
  }

  public String[] getActions() {
    return new String[] { "Save", "Cancel" };
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  @SuppressWarnings("unchecked")
  public List<ContactData> getContacts() throws Exception {
    List<ContactData> contacts = new ArrayList<ContactData>(uiPageList_.getCurrentPageData());
    for (ContactData c : contacts) {
      UIFormCheckBoxInput uiInput = getUIFormCheckBoxInput(c.getId());
      if (uiInput == null) {
        uiInput = new UIFormCheckBoxInput<Boolean>(c.getId(), c.getId(), null);
        addUIFormInput(uiInput);
      }
    }
    for (ContactData c : checkedList_.values()) {
      UIFormCheckBoxInput uiInput = getUIFormCheckBoxInput(c.getId());
      if (uiInput != null)
        uiInput.setChecked(true);
    }
    return contacts;
  }

  public UIPageIterator getUIPageIterator() {
    return uiPageList_;
  }

  public long getAvailablePage() {
    return uiPageList_.getAvailablePage();
  }

  public long getCurrentPage() {
    return uiPageList_.getCurrentPage();
  }

  protected void updateCurrentPage(int page) throws Exception {
    uiPageList_.setCurrentPage(page);
  }

  public void setContactList(ContactFilter filter) throws Exception {
    ContactService contactSrv = getApplicationComponent(ContactService.class);
    Map<String, String> resultMap = contactSrv.searchEmails(MailUtils.getCurrentUser(), filter);
    List<ContactData> data = new ArrayList<ContactData>();
    for (String ct : resultMap.keySet()) {
      String id = ct;
      String value = resultMap.get(id);
      if (resultMap.get(id) != null && resultMap.get(id).trim().length() > 0) {
        if (value.lastIndexOf(Utils.SPLIT) > 0) {
          String fullName = value.substring(0, value.lastIndexOf(Utils.SPLIT));
          String email = value.substring(value.lastIndexOf(Utils.SPLIT) + Utils.SPLIT.length());
          if (!CalendarUtils.isEmpty(email))
            data.add(new ContactData(id, fullName, email));
        }
      }
    }
    setContactList(data);
  }

  public void setContactList(List<ContactData> contactList) throws Exception {
    //ObjectPageList objPageList = new ObjectPageList(contactList, 10);
    LazyPageList<ContactData> pageList = new LazyPageList<ContactData>(new ListAccessImpl<ContactData>(ContactData.class, contactList), 10);
    uiPageList_.setPageList(pageList);
  }

  public void setAlreadyCheckedContact(List<ContactData> alreadyCheckedContact) throws Exception {
    for (ContactData ct : alreadyCheckedContact) {
      checkedList_.put(ct.getId(), ct);
    }
  }

  @SuppressWarnings("unchecked")
  public List<ContactData> getCheckedContact() throws Exception {
    List<ContactData> contactList = new ArrayList<ContactData>();
    for (ContactData contact : new ArrayList<ContactData>(uiPageList_.getCurrentPageData())) {
      UIFormCheckBoxInput<Boolean> uiCheckbox = getChildById(contact.getId());
      if (uiCheckbox != null && uiCheckbox.isChecked()) {
        contactList.add(contact);
      }
    }
    return contactList;
  }

  // Added fo fixing the CS-4028
  @SuppressWarnings("unchecked")
  public List<String> getAllEmailOfCategory() throws Exception {
    List<String> emailList = new ArrayList<String>();
    for (ContactData contact : new ArrayList<ContactData>(uiPageList_.getCurrentPageData())) {
      emailList.add(contact.getEmail());
    }
    return emailList;
  }

  static public class ChangeGroupActionListener extends EventListener<UIAddressForm> {
    public void execute(Event<UIAddressForm> event) throws Exception {
      UIAddressForm uiAddressForm = event.getSource();
      String category = ((UIFormSelectBoxWithGroups) uiAddressForm.getChildById(UIAddressForm.CONTACT_GROUP)).getValue();
      String groupID = null;
      String gottenAddress = uiAddressForm.addressBooksMap.get(category);
      if (gottenAddress != null) {
        String[] arr = gottenAddress.split("_");
        if (arr != null && arr.length > 1)
          groupID = arr[arr.length - 1];
      }
      if (groupID != null) {
        if (groupID.equals(uiAddressForm.all)) {
          uiAddressForm.setContactList(new ContactFilter());
        } else if (groupID.equals(UIAddressForm.SHARED)) {

          ContactFilter filter = new ContactFilter();
          filter.setSearchSharedContacts(true);
          ContactService contactSrv = uiAddressForm.getApplicationComponent(ContactService.class);
          Map<String, String> resultMap = contactSrv.searchEmails(MailUtils.getCurrentUser(),
                                                                  filter);
          List<ContactData> data = new ArrayList<ContactData>();
          for (String ct : resultMap.keySet()) {
            String id = ct;
            String value = resultMap.get(id);
            if (resultMap.get(id) != null && resultMap.get(id).trim().length() > 0) {
              if (value.lastIndexOf(Utils.SPLIT) > 0) {
                String fullName = value.substring(0, value.lastIndexOf(Utils.SPLIT));
                String email = value.substring(value.lastIndexOf(Utils.SPLIT)
                    + Utils.SPLIT.length());
                if (!MailUtils.isFieldEmpty(email)) {
                  data.add(uiAddressForm.new ContactData(id, fullName, email));
                }
              }
            }
          }
          uiAddressForm.setContactList(data);
        } else {
          // if (category.equals(NewUserListener.DEFAULTGROUP))
          // category = category + MailUtils.getCurrentUser();
          uiAddressForm.selectedAddressId_ = category;
          ContactFilter filter = new ContactFilter();
          if (!MailUtils.isFieldEmpty(uiAddressForm.selectedAddressId_)) {
            filter.setCategories(new String[] { uiAddressForm.selectedAddressId_ });
          }
          uiAddressForm.setContactList(filter);
        }
        uiAddressForm.getUIStringInput(UIAddressForm.CONTACT_SEARCH).setValue(null);
        // ((UIFormSelectBoxWithGroups)uiAddressForm.getChildById(UIAddressForm.CONTACT_GROUP)).setValue(uiAddressForm.selectedAddressId_)
        // ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressForm);
      }
    }
  }

  static public class SearchContactActionListener extends EventListener<UIAddressForm> {
    public void execute(Event<UIAddressForm> event) throws Exception {
      UIAddressForm uiForm = event.getSource();
      String text = uiForm.getUIStringInput(UIAddressForm.CONTACT_SEARCH).getValue();
      String category = ((UIFormSelectBoxWithGroups) uiForm.getChildById(UIAddressForm.CONTACT_GROUP)).getValue();
      ContactFilter filter = new ContactFilter();
      if (!MailUtils.isFieldEmpty(text))
        filter.setText(MailUtils.encodeJCRText(text));
      if (category.equals(uiForm.all)) {
        uiForm.setContactList(filter);
      } else if (category.equals(uiForm.sharedContacts_)) {
        filter.setType(DataStorage.SHARED);
        filter.setSearchSharedContacts(true);
        uiForm.setContactList(filter);
      } else {
        if (category.equals(NewUserListener.DEFAULTGROUP))
          category = category + MailUtils.getCurrentUser();
        uiForm.selectedAddressId_ = category;
        try {
          if (!MailUtils.isFieldEmpty(uiForm.selectedAddressId_)) {
            filter.setCategories(new String[] { uiForm.selectedAddressId_ });
          }
          uiForm.setContactList(filter);
          ((UIFormSelectBoxWithGroups) uiForm.getChildById(UIAddressForm.CONTACT_GROUP)).setValue(category);
          event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
        } catch (Exception e) {          
          event.getRequestContext()
               .getUIApplication()
               .addMessage(new ApplicationMessage("UIAddressForm.msg.search-error-keyword", null));
          return;
        }
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
    }
  }

  static public class ReplaceActionListener extends EventListener<UIAddressForm> {
    public void execute(Event<UIAddressForm> event) throws Exception {
      UIAddressForm uiAddressForm = event.getSource();
      boolean isSendToGroup = uiAddressForm.getUIFormCheckBoxInput(UIAddressForm.SELECTED_GROUP)
                                           .isChecked();
      List<ContactData> checkedContact = uiAddressForm.getCheckedContact();
      if (checkedContact.isEmpty() && !isSendToGroup) {        
        event.getRequestContext()
             .getUIApplication()
             .addMessage(new ApplicationMessage("UIAddressForm.msg.contact-email-required", null));
        return;
      }
      UIPopupActionContainer uiPopupContainer = uiAddressForm.getAncestorOfType(UIPopupActionContainer.class);
      UIPopupAction childPopup = uiAddressForm.getAncestorOfType(UIPopupAction.class);
      uiAddressForm.checkedList_.clear();
      uiAddressForm.newCheckedList_.clear();
      String toAddress = "";

      for (ContactData contact : checkedContact) {
        if (MailUtils.isFieldEmpty(contact.getEmail())) {          
          event.getRequestContext()
               .getUIApplication()
               .addMessage(new ApplicationMessage("UIAddressForm.msg.you-should-only-choose-contact-with-email-address", null));    
          return;
        }
      }
      for (ContactData ct : checkedContact) {
        uiAddressForm.newCheckedList_.put(ct.getId(), ct);
      }

      UIComposeForm uiComposeForm = uiPopupContainer.getChild(UIComposeForm.class);
      UIEventForm uiEventForm = uiPopupContainer.getChild(UIEventForm.class);
      StringBuffer sb = new StringBuffer();
      for (ContactData contact : uiAddressForm.newCheckedList_.values()) {
        String addresses = contact.getEmail();
        if (addresses != null && addresses.trim().length() > 0) {
          String add = contact.getEmail().replace(";", ",");
          if (uiEventForm != null) {
            if (sb.length() > 0)
              sb.append(",");
            sb.append(add);
          } else {
            String[] eAddresses = null;
            if (addresses.contains(";"))
              eAddresses = addresses.split(";");
            if (eAddresses != null) {
              for (int i = 0; i < eAddresses.length; i++) {
                toAddress += contact.getFullName() + "<" + eAddresses[i] + "> ,";
              }
            } else {
              toAddress += contact.getFullName() + "<" + contact.getEmail() + "> ,";
            }
          }
        }
      }
      /*
       * List<String> listMail = Arrays.asList(
       * sb.toString().split(MailUtils.COMMA)) ; String email = null ;
       * for(Contact c : checkedContact) { email = c.getEmailAddress() ;
       * if(!listMail.contains(email)) { if(sb != null && sb.length() > 0)
       * sb.append(MailUtils.COMMA) ; if(email != null) sb.append(email) ; } }
       */
      if (uiEventForm != null) {
        uiEventForm.setSelectedTab(UIEventForm.TAB_EVENTREMINDER);
        uiEventForm.setEmailAddress(sb.toString());
        uiAddressForm.checkedList_ = uiAddressForm.newCheckedList_;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiEventForm);
      } else if (uiComposeForm != null) {
        // Check the checked group
        String value1 = "";
        String value2 = "";
        if (isSendToGroup) {
          try {
            value1 = ((UIFormSelectBoxWithGroups) uiAddressForm.getChildById(UIAddressForm.CONTACT_GROUP)).getValue();
            String[] values = ((UIFormSelectBoxWithGroups) uiAddressForm.getChildById(UIAddressForm.CONTACT_GROUP)).getSelectedValues();
            value2 = values[0];
          } catch (Exception e) {
            if (log.isDebugEnabled()) {
              log.debug("Exception in method execute of class ReplaceActionListener", e);
            }
          }
        }

        if (uiAddressForm.getRecipientType().equals("to")) {
          uiComposeForm.getGroupDataValues(UIComposeForm.FIELD_TO_GROUP).clear();
          uiComposeForm.addGroupDataValue(UIComposeForm.FIELD_TO_GROUP, value1, value2);
          uiComposeForm.refreshGroupFileList(UIComposeForm.FIELD_TO_GROUP);

          uiComposeForm.setFieldToValue(toAddress);
          uiComposeForm.setToContacts(checkedContact);
        }
        if (uiAddressForm.getRecipientType().equals("cc")) {
          uiComposeForm.getGroupDataValues(UIComposeForm.FIELD_CC_GROUP).clear();
          uiComposeForm.addGroupDataValue(UIComposeForm.FIELD_CC_GROUP, value1, value2);
          uiComposeForm.refreshGroupFileList(UIComposeForm.FIELD_CC_GROUP);

          uiComposeForm.setFieldCcValue(toAddress);
          uiComposeForm.setCcContacts(checkedContact);
        }

        if (uiAddressForm.getRecipientType().equals("bcc")) {
          uiComposeForm.getGroupDataValues(UIComposeForm.FIELD_BCC_GROUP).clear();
          uiComposeForm.addGroupDataValue(UIComposeForm.FIELD_BCC_GROUP, value1, value2);
          uiComposeForm.refreshGroupFileList(UIComposeForm.FIELD_BCC_GROUP);

          uiComposeForm.setFieldBccValue(toAddress);
          uiComposeForm.setBccContacts(checkedContact);
        }
        uiAddressForm.checkedList_ = uiAddressForm.newCheckedList_;
        event.getRequestContext()
             .addUIComponentToUpdateByAjax(uiComposeForm.getChildById(UIComposeForm.FIELD_TO_SET));
      }
      childPopup.deActivate();
      event.getRequestContext().addUIComponentToUpdateByAjax(childPopup);
    }
  }

  static public class AddActionListener extends EventListener<UIAddressForm> {
    public void execute(Event<UIAddressForm> event) throws Exception {
      UIAddressForm uiAddressForm = event.getSource();

      List<ContactData> checkedContact = uiAddressForm.getCheckedContact();
      boolean isSendToGroup = uiAddressForm.getUIFormCheckBoxInput(UIAddressForm.SELECTED_GROUP)
                                           .isChecked();
      if (checkedContact.size() <= 0 && !isSendToGroup) {
        event.getRequestContext()
             .getUIApplication()
             .addMessage(new ApplicationMessage("UIAddressForm.msg.contact-email-required", null));
        return;
      }
      UIMailPortlet uiPortlet = uiAddressForm.getAncestorOfType(UIMailPortlet.class);

      UIComposeForm uiComposeForm = uiPortlet.findFirstComponentOfType(UIComposeForm.class);
      UIEventForm uiEventForm = uiPortlet.findFirstComponentOfType(UIEventForm.class);
      for (ContactData contact : checkedContact) {
        if (MailUtils.isFieldEmpty(contact.getEmail())) {          
          event.getRequestContext()
               .getUIApplication()
               .addMessage(new ApplicationMessage("UIAddressForm.msg.you-should-only-choose-contact-with-email-address", null));      
          return;
        }
      }
      for (ContactData ct : checkedContact) {
        if (!uiAddressForm.checkedList_.containsKey(ct.getId()))
          uiAddressForm.newCheckedList_.put(ct.getId(), ct);
      }
      if (uiEventForm != null) {
        StringBuffer sb = new StringBuffer();
        if (uiEventForm.getEmailAddress() != null
            && uiEventForm.getEmailAddress().trim().length() > 0) {
          sb.append(uiEventForm.getEmailAddress());
        }
        List<String> listMail = Arrays.asList(sb.toString().split(MailUtils.COMMA));

        List<String> emailList = uiAddressForm.getAllEmailOfCategory();
        // IF: fixed for CS-4028
        if (isSendToGroup) {
          for (String email : emailList) {
            if (!listMail.contains(email)) {
              if (sb != null && sb.length() > 0)
                sb.append(MailUtils.COMMA);
              if (email != null)
                sb.append(email.replace(";", ","));
            }
          }
        } else {
          String email = null;
          for (ContactData c : uiAddressForm.newCheckedList_.values()) {
            email = c.getEmail();
            if (!listMail.contains(email)) {
              if (sb != null && sb.length() > 0)
                sb.append(MailUtils.COMMA);
              if (email != null)
                sb.append(email.replace(";", ","));
            }
          }
        }
        uiEventForm.setSelectedTab(UIEventForm.TAB_EVENTREMINDER);
        uiEventForm.setEmailAddress(sb.toString());
        uiAddressForm.checkedList_ = uiAddressForm.newCheckedList_;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiEventForm);
        return;
      } else if (uiComposeForm != null) {
        String toAddress = uiAddressForm.getAvaiAddressStr() != null ? uiAddressForm.getAvaiAddressStr()
                                                                    : "";
        if (!toAddress.equals("") && !toAddress.endsWith(",")) {
          toAddress = toAddress + ",";
        }

        for (ContactData ct : checkedContact) {
          if (!uiAddressForm.checkedList_.containsKey(ct.getId()))
            uiAddressForm.newCheckedList_.put(ct.getId(), ct);
        }
        for (ContactData contact : uiAddressForm.newCheckedList_.values()) {
          String addresses = contact.getEmail();
          if (addresses != null && addresses.trim().length() > 0) {
            String[] eAddresses = null;
            if (addresses.contains(";"))
              eAddresses = addresses.split(";");
            if (eAddresses != null) {
              for (int i = 0; i < eAddresses.length; i++) {
                toAddress += contact.getFullName() + "<" + eAddresses[i] + "> ,";
              }
            } else {
              toAddress += contact.getFullName() + "<" + contact.getEmail() + "> ,";
            }
          }
        }
        if (uiAddressForm.getRecipientType().equals("to")) {
          uiComposeForm.setFieldToValue(toAddress);
        } else if (uiAddressForm.getRecipientType().equals("cc")) {
          uiComposeForm.setFieldCcValue(toAddress);
        } else if (uiAddressForm.getRecipientType().equals("bcc")) {
          uiComposeForm.setFieldBccValue(toAddress);
        }
        // Check the checked group
        String value1 = "";
        String value2 = "";
        if (isSendToGroup) {
          try {
            value1 = ((UIFormSelectBoxWithGroups) uiAddressForm.getChildById(UIAddressForm.CONTACT_GROUP)).getValue();
            String[] values = ((UIFormSelectBoxWithGroups) uiAddressForm.getChildById(UIAddressForm.CONTACT_GROUP)).getSelectedValues();
            value2 = values[0];
          } catch (Exception e) {
            if (log.isDebugEnabled()) {
              log.debug("Exception in method execute of class AddActionListener", e);
            }
          }
          String gottenAddress = uiAddressForm.addressBooksMap.get(value1);
          if (gottenAddress != null) {
            String[] arrString = gottenAddress.split("_");
            if (arrString.length > 0)
              value1 = arrString[0];
            uiComposeForm.addAddressBooksMap(value2, gottenAddress);
          } else {
            uiComposeForm.addAddressBooksMap(value2, value1);
          }
          if (uiAddressForm.getRecipientType().equals("to")) {
            uiComposeForm.addGroupDataValue(UIComposeForm.FIELD_TO_GROUP, value1, value2);
            uiComposeForm.refreshGroupFileList(UIComposeForm.FIELD_TO_GROUP);
            uiComposeForm.setFieldToValue(toAddress);
            uiComposeForm.setToContacts(new ArrayList<ContactData>(uiAddressForm.newCheckedList_.values()));
          } else if (uiAddressForm.getRecipientType().equals("cc")) {
            uiComposeForm.addGroupDataValue(UIComposeForm.FIELD_CC_GROUP, value1, value2);
            uiComposeForm.refreshGroupFileList(UIComposeForm.FIELD_CC_GROUP);
            uiComposeForm.setFieldCcValue(toAddress);
            uiComposeForm.setCcContacts(new ArrayList<ContactData>(uiAddressForm.newCheckedList_.values()));
          } else if (uiAddressForm.getRecipientType().equals("bcc")) {
            uiComposeForm.addGroupDataValue(UIComposeForm.FIELD_BCC_GROUP, value1, value2);
            uiComposeForm.refreshGroupFileList(UIComposeForm.FIELD_BCC_GROUP);
            uiComposeForm.setFieldBccValue(toAddress);
            uiComposeForm.setBccContacts(new ArrayList<ContactData>(uiAddressForm.newCheckedList_.values()));
          }
        }
        uiAddressForm.checkedList_ = uiAddressForm.newCheckedList_;
        event.getRequestContext()
             .addUIComponentToUpdateByAjax(uiComposeForm.getChildById(UIComposeForm.FIELD_TO_SET));
      }
    }
  }

  static public class CancelActionListener extends EventListener<UIAddressForm> {
    public void execute(Event<UIAddressForm> event) throws Exception {
      UIAddressForm uiAddressForm = event.getSource();
      UIPopupAction uiPopupAction = uiAddressForm.getAncestorOfType(UIPopupAction.class);
      uiPopupAction.deActivate();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
    }
  }

  static public class ShowPageActionListener extends EventListener<UIAddressForm> {
    public void execute(Event<UIAddressForm> event) throws Exception {
      UIAddressForm uiAddressForm = event.getSource();
      int page = Integer.parseInt(event.getRequestContext().getRequestParameter(OBJECTID));
      for (ContactData ct : uiAddressForm.getCheckedContact()) {
        uiAddressForm.newCheckedList_.put(ct.getId(), ct);
        uiAddressForm.checkedList_.put(ct.getId(), ct);
      }
      uiAddressForm.updateCurrentPage(page);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressForm);
    }
  }

  public class ContactData {
    private String id;

    private String fullName;

    private String email;

    public ContactData(String id, String fullName, String email) {
      this.id = id;
      this.fullName = fullName;
      this.email = email;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getId() {
      return id;
    }

    public void setFullName(String fullName) {
      this.fullName = fullName;
    }

    public String getFullName() {
      return fullName;
    }

    public void setEmail(String email) {
      this.email = email;
    }

    public String getEmail() {
      return email;
    }
  }

}