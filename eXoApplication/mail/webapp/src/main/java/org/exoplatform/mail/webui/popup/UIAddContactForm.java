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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import javax.jcr.PathNotFoundException;

import org.exoplatform.contact.service.AddressBook;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactAttachment;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.DataStorage;
import org.exoplatform.contact.service.SharedAddressBook;
import org.exoplatform.contact.service.Utils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.cs.common.webui.UIPopupAction;
import org.exoplatform.cs.common.webui.UIPopupActionContainer;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.portal.application.PortalRequestContext;
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
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.core.model.SelectOption;
import org.exoplatform.webui.core.model.SelectOptionGroup;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormRadioBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormSelectBoxWithGroups;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;

/**
 * Created by The eXo Platform SARL Author : Phung Nam phunghainam@gmail.com Nov
 * 8, 2007
 */

@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "app:/templates/mail/webui/popup/UIAddContactForm.gtmpl", events = {
    @EventConfig(listeners = UIAddContactForm.AddGroupActionListener.class, phase = Phase.DECODE),
    @EventConfig(listeners = UIAddContactForm.ChangeImageActionListener.class, phase = Phase.DECODE),
    @EventConfig(listeners = UIAddContactForm.DeleteImageActionListener.class, phase = Phase.DECODE),
    @EventConfig(listeners = UIAddContactForm.SaveActionListener.class),
    @EventConfig(listeners = UIAddContactForm.CancelActionListener.class, phase = Phase.DECODE) })
public class UIAddContactForm extends UIForm implements UIPopupComponent {
  private static final Log log = ExoLogger.getExoLogger(UIAddContactForm.class);
  
  public static final String       SELECT_GROUP     = "select-group".intern();

  public static final String       NAME             = "name".intern();

  public static final String       FIRST_NAME       = "first-name".intern();

  public static final String       LAST_NAME        = "last-name".intern();

  private static final String      NICKNAME         = "nickName";

  private static final String      GENDER           = "gender";

  private static final String      BIRTHDAY         = "birthday";

  private static final String      DAY              = "day";

  private static final String      MONTH            = "month";

  private static final String      YEAR             = "year";

  private static final String      JOBTITLE         = "jobTitle";

  private static final String      EMAIL            = "email";

  private UIFormMultiValueInputSet uiFormMultiValue = new UIFormMultiValueInputSet(EMAIL, EMAIL);

  private static final String      MALE             = "Male";

  private static final String      FEMALE           = "Female";

  private byte[]                   imageBytes_      = null;

  private Contact                  tempContact      = null;

  private String                   fileName_        = null;

  private String                   imageMimeType_   = null;

  public boolean                   isEdited_        = false;

  public String                    selectedGroup_;

  public Contact                   selectedContact_;

  public boolean                   addedNewGroup_   = false;

  private String                   sharedContacts_  = "sharedContacts";

  public UIAddContactForm() throws Exception {
    addUIFormInput(new UIFormSelectBoxWithGroups(SELECT_GROUP, SELECT_GROUP, getOptions()));
    addUIFormInput(new UIFormStringInput(FIRST_NAME, FIRST_NAME, null).addValidator(MandatoryValidator.class));
    addUIFormInput(new UIFormStringInput(LAST_NAME, LAST_NAME, null).addValidator(MandatoryValidator.class));
    addUIFormInput(new UIFormStringInput(NICKNAME, NICKNAME, null));
    List<SelectItemOption<String>> genderOptions = new ArrayList<SelectItemOption<String>>();
    genderOptions.add(new SelectItemOption<String>(MALE, MALE));
    genderOptions.add(new SelectItemOption<String>(FEMALE, FEMALE));
    addUIFormInput(new UIFormRadioBoxInput(GENDER, MALE, genderOptions));
    addUIFormInput(new UIFormInputInfo(BIRTHDAY, BIRTHDAY, null));

    List<SelectItemOption<String>> datesOptions = new ArrayList<SelectItemOption<String>>();
    datesOptions.add(new SelectItemOption<String>("- " + DAY + " -", DAY));
    for (int i = 1; i < 32; i++) {
      String date = i + "";
      datesOptions.add(new SelectItemOption<String>(date, date));
    }
    addUIFormInput(new UIFormSelectBox(DAY, DAY, datesOptions));

    List<SelectItemOption<String>> monthOptions = new ArrayList<SelectItemOption<String>>();
    monthOptions.add(new SelectItemOption<String>("-" + MONTH + "-", MONTH));
    for (int i = 1; i < 13; i++) {
      String month = i + "";
      monthOptions.add(new SelectItemOption<String>(month, month));
    }
    addUIFormInput(new UIFormSelectBox(MONTH, MONTH, monthOptions));
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    Locale locale = context.getParentAppRequestContext().getLocale();

    String date = MailUtils.formatDate("dd/MM/yyyy", new Date(), locale);
    String strDate = date.substring(date.lastIndexOf("/") + 1, date.length());
    int thisYear = Integer.parseInt(strDate);
    List<SelectItemOption<String>> yearOptions = new ArrayList<SelectItemOption<String>>();
    yearOptions.add(new SelectItemOption<String>("- " + YEAR + " -", YEAR));
    for (int i = thisYear; i >= 1900; i--) {
      String year = i + "";
      yearOptions.add(new SelectItemOption<String>(year, year));
    }
    addUIFormInput(new UIFormSelectBox(YEAR, YEAR, yearOptions));

    addUIFormInput(new UIFormStringInput(JOBTITLE, JOBTITLE, null));
    uiFormMultiValue.setType(UIFormStringInput.class);
    addUIFormInput(uiFormMultiValue);
  }

  public boolean isPrivateGroup(String groupId) {
    SelectOptionGroup privateGroups = (SelectOptionGroup) getChild(UIFormSelectBoxWithGroups.class).getOptions()
                                                                                                   .get(0);
    for (SelectOption option : privateGroups.getOptions())
      if (option.getValue().equals(groupId)) {
        return true;
      }
    return false;
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
    String group = getChild(UIFormSelectBoxWithGroups.class).getValue();
    if (group != null && !group.equals(sharedContacts_)) {
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

  public void setTempContact(Contact c) {
    tempContact = c;
  }

  public Contact getTempContact() {
    return tempContact;
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
      if (MailUtils.havePermission(scg.getId()))
        sharedContacts.addOption(new SelectOption(MailUtils.getDisplayAdddressShared(scg.getSharedUserId(),
                                                                                     scg.getName()),
                                                  scg.getId()));
    }
    options.add(sharedContacts);
    return options;
  }

  public void fillDatas(Contact ct, String groupId) throws Exception {
    isEdited_ = true;
    selectedGroup_ = groupId;
    selectedContact_ = ct;
    tempContact = ct;
    ((UIFormSelectBoxWithGroups) getChildById(SELECT_GROUP)).setSelectedValues(new String[] { groupId });
    ((UIFormSelectBoxWithGroups) getChildById(SELECT_GROUP)).setDisabled(true);
    getUIStringInput(FIRST_NAME).setValue(ct.getFirstName());
    getUIStringInput(LAST_NAME).setValue(ct.getLastName());
    getUIStringInput(NICKNAME).setValue(ct.getNickName());
    getChild(UIFormRadioBoxInput.class).setValue(ct.getGender());
    setFieldBirthday(ct.getBirthday());
    getUIStringInput(JOBTITLE).setValue(ct.getJobTitle());

    List<String> list;
    String emails = MailUtils.listToString(ct.getEmailAddresses());
    if (MailUtils.isFieldEmpty(emails))
      list = new ArrayList<String>();
    else
      list = Arrays.asList(emails.split(Utils.SEMI_COLON));
    if (uiFormMultiValue != null)
      removeChildById(EMAIL);
    uiFormMultiValue = createUIComponent(UIFormMultiValueInputSet.class, null, null);
    uiFormMultiValue.setId(EMAIL);
    uiFormMultiValue.setName(EMAIL);
    uiFormMultiValue.setType(UIFormStringInput.class);
    uiFormMultiValue.setValue(list);
    addUIFormInput(uiFormMultiValue);

    if (ct != null && ct.getAttachment() != null) {
      setImage(ct.getAttachment().getInputStream());
    }
  }

  public void setFieldBirthday(Date date) throws Exception {
    if (date != null) {
      Calendar cal = GregorianCalendar.getInstance();
      cal.setTime(date);
      getUIFormSelectBox(MONTH).setValue(String.valueOf(cal.get(Calendar.MONTH) + 1));
      getUIFormSelectBox(DAY).setValue(String.valueOf(cal.get(Calendar.DATE)));
      getUIFormSelectBox(YEAR).setValue(String.valueOf(cal.get(Calendar.YEAR)));
    }
  }

  public void refreshGroupList() throws Exception {
    ((UIFormSelectBoxWithGroups) getChildById(SELECT_GROUP)).setOptions(getOptions());
  }

  public void setFirstNameField(String firstName) throws Exception {
    getUIStringInput(FIRST_NAME).setValue(firstName);
  }

  public void setLastNameField(String lastName) throws Exception {
    getUIStringInput(LAST_NAME).setValue(lastName);
  }

  public void setAddedNewGroup(boolean b) {
    addedNewGroup_ = b;
  }

  public String getNickName() {
    return getUIStringInput(NICKNAME).getValue();
  }

  public String getJobTitle() {
    return getUIStringInput(JOBTITLE).getValue();
  }

  public void setEmailField(String emails) throws Exception {
    List<String> list;
    if (MailUtils.isFieldEmpty(emails))
      list = new ArrayList<String>();
    else
      list = Arrays.asList(emails.split(Utils.SEMI_COLON));
    if (uiFormMultiValue != null)
      removeChildById(EMAIL);
    uiFormMultiValue = createUIComponent(UIFormMultiValueInputSet.class, null, null);
    uiFormMultiValue.setId(EMAIL);
    uiFormMultiValue.setName(EMAIL);
    uiFormMultiValue.setType(UIFormStringInput.class);
    uiFormMultiValue.setValue(list);
    addUIFormInput(uiFormMultiValue);
  }

  @SuppressWarnings("unchecked")
  protected String getFieldEmail() {
    List<String> emails = (List<String>) uiFormMultiValue.getValue();
    StringBuffer email = new StringBuffer();
    for (String item : emails) {
      if (MailUtils.isFieldEmpty(item))
        continue;
      if (email.length() == 0)
        email.append(item);
      else
        email.append(Utils.SEMI_COLON + item);
    }
    return email.toString();
  }

  protected String getFieldGender() {
    return getChild(UIFormRadioBoxInput.class).getValue();
  }

  protected Date getFieldBirthday() {
    int day, month, year;
    day = month = year = 0;
    boolean emptyDay, emptyMonth, emptyYear;
    emptyDay = emptyMonth = emptyYear = false;
    try {
      day = Integer.parseInt(getUIFormSelectBox(DAY).getValue());
    } catch (NumberFormatException e) {
      emptyDay = true;
    }
    try {
      month = Integer.parseInt(getUIFormSelectBox(MONTH).getValue());
    } catch (NumberFormatException e) {
      emptyMonth = true;
    }
    try {
      year = Integer.parseInt(getUIFormSelectBox(YEAR).getValue());
    } catch (NumberFormatException e) {
      emptyYear = true;
    }
    if (emptyDay && emptyMonth && emptyYear)
      return null;
    else {
      Calendar cal = GregorianCalendar.getInstance();
      cal.setLenient(false);
      cal.set(Calendar.DATE, day);
      cal.set(Calendar.MONTH, month - 1);
      cal.set(Calendar.YEAR, year);
      return cal.getTime();
    }
  }

  public String[] getActions() {
    return new String[] { "Save", "Cancel" };
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }
  
  private static void renderMessage(WebuiRequestContext context, String key, int type) throws Exception {
    context.getUIApplication().addMessage(new ApplicationMessage(key, null, type));
    ((PortalRequestContext) context.getParentAppRequestContext()).ignoreAJAXUpdateOnPortlets(false);
  }

  public static class SaveActionListener extends EventListener<UIAddContactForm> {
    public void execute(Event<UIAddContactForm> event) throws Exception {
      UIAddContactForm uiContact = event.getSource();
      UIMailPortlet uiPortlet = uiContact.getAncestorOfType(UIMailPortlet.class);      
      String groupId = ((UIFormSelectBoxWithGroups) uiContact.getChildById(SELECT_GROUP)).getValue();
      if (uiContact.isEdited_){
        groupId = uiContact.selectedGroup_;
      }
      String firstName = uiContact.getUIStringInput(FIRST_NAME).getValue();
      String lastName = uiContact.getUIStringInput(LAST_NAME).getValue();
      String emails = uiContact.getFieldEmail();
      if (!MailUtils.isValidEmailAddresses(emails.replaceAll(Utils.SEMI_COLON, ","))) {
        renderMessage(event.getRequestContext(), "UIAddContactForm.msg.email-invalid", ApplicationMessage.WARNING);
        return;
      }

      if (!uiContact.isEdited_ && MailUtils.isFieldEmpty(groupId)) {
        renderMessage(event.getRequestContext(), "UIAddContactForm.msg.group-required", ApplicationMessage.WARNING);
        return;
      } else if (MailUtils.isFieldEmpty(firstName) && MailUtils.isFieldEmpty(lastName)) {
        renderMessage(event.getRequestContext(), "UIAddContactForm.msg.name-required", ApplicationMessage.WARNING);
        return;
      }
      Contact contact;
      if (uiContact.isEdited_)
        contact = uiContact.selectedContact_;
      else
        contact = new Contact();
      contact.setFullName(firstName.concat(" ").concat(lastName));
      contact.setFirstName(firstName);
      contact.setLastName(lastName);
      contact.setNickName(uiContact.getNickName());
      contact.setGender(uiContact.getFieldGender());
      try {
        Date birthday = uiContact.getFieldBirthday();
        Date today = new Date();
        if (birthday != null && birthday.after(today)) {
          renderMessage(event.getRequestContext(), "UIAddContactForm.msg.date-time-invalid", ApplicationMessage.WARNING);
          return;
        }
        contact.setBirthday(birthday);
      } catch (IllegalArgumentException e) {
        renderMessage(event.getRequestContext(), "UIAddContactForm.msg.birthday-incorrect", ApplicationMessage.WARNING);
        return;
      }
      contact.setEmailAddress(emails);
      if (uiContact.getImage() != null) {
        ContactAttachment attachment = new ContactAttachment();
        attachment.setInputStream(new ByteArrayInputStream(uiContact.getImage()));
        attachment.setFileName(uiContact.getFileName());
        attachment.setMimeType(uiContact.getMimeType());
        contact.setAttachment(attachment);
      } else {
        contact.setAttachment(null);
      }
      contact.setJobTitle(uiContact.getJobTitle());
      ContactService contactSrv = uiContact.getApplicationComponent(ContactService.class);
      String username = uiPortlet.getCurrentUser();
      try {
        if (!uiContact.isEdited_) {
          contact.setAddressBookIds(new String[] { groupId });
          if (uiContact.isPrivateGroup(groupId)) {
            contactSrv.saveContact(username, contact, true);
            contact = contactSrv.getContact(username, contact.getId());
          } else {
            if (!MailUtils.havePermission(groupId)) {
              renderMessage(event.getRequestContext(), "UIAddContactForm.msg.non-permission", ApplicationMessage.WARNING);
              return;
            }
            contactSrv.saveContactToSharedAddressBook(username, groupId, contact, true);
            contact = contactSrv.getSharedContactAddressBook(username, contact.getId());
          }
        } else {
          if (groupId == null || groupId.equals(uiContact.sharedContacts_)) {
            if (!uiContact.havePermission(contact)) {
              renderMessage(event.getRequestContext(), "UIAddContactForm.msg.non-permission", ApplicationMessage.WARNING);
              return;
            }
            contactSrv.saveSharedContact(username, contact);
            contact = contactSrv.getSharedContact(username, contact.getId());
          } else if (uiContact.isPrivateGroup(groupId)) {
            contactSrv.saveContact(uiPortlet.getCurrentUser(), contact, false);
            contact = contactSrv.getContact(username, contact.getId());
          } else {
            if (!MailUtils.havePermission(groupId)) {
              renderMessage(event.getRequestContext(), "UIAddContactForm.msg.non-permission", ApplicationMessage.WARNING);
              return;
            }
            contactSrv.saveContactToSharedAddressBook(username, groupId, contact, false);
            contact = contactSrv.getSharedContactAddressBook(username, contact.getId());
          }
        }
        UIAddressBookForm uiAddress = uiPortlet.findFirstComponentOfType(UIAddressBookForm.class);
        if (uiAddress != null) {
          if (!uiContact.isEdited_) {
            uiAddress.updateGroup(groupId);
          }
          uiAddress.setSelectedContact(contact);
          String selectedGroupId = ((UIFormSelectBoxWithGroups) uiAddress.getChildById(SELECT_GROUP)).getValue();
          uiAddress.refrestContactList(selectedGroupId, contact);
          uiAddress.setCheckAll();
          ((UIFormSelectBoxWithGroups) uiAddress.getChildById(SELECT_GROUP)).setValue(selectedGroupId);
          event.getRequestContext().addUIComponentToUpdateByAjax(uiAddress);
        }
        uiContact.getAncestorOfType(UIPopupAction.class).cancelPopupAction();
      } catch (Exception e) {
        if (log.isDebugEnabled()) {
          log.debug("Exception in method execute of class SaveActionListener", e);
        }
      }
      List<String> tempContact = new ArrayList<String>();
      tempContact.add(Utils.contactTempId);
      contactSrv.removeContacts(uiPortlet.getCurrentUser(), tempContact);
    }
  }

  public static class CancelActionListener extends EventListener<UIAddContactForm> {
    public void execute(Event<UIAddContactForm> event) throws Exception {
      UIAddContactForm uiContact = event.getSource();
      UIMailPortlet uiPortlet = uiContact.getAncestorOfType(UIMailPortlet.class);
      UIAddressBookForm uiAddress = uiPortlet.findFirstComponentOfType(UIAddressBookForm.class);
      Contact contact = uiContact.selectedContact_;
      if (uiAddress != null && uiContact.addedNewGroup_) {
        uiAddress.updateGroup(uiContact.selectedGroup_);
        uiAddress.refrestContactList(uiContact.selectedGroup_, contact);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiAddress.getParent());
        String selectedGroupId = ((UIFormSelectBoxWithGroups) uiAddress.getChildById(SELECT_GROUP)).getValue();
        uiAddress.setSelectedContact(contact);
        uiAddress.refrestContactList(selectedGroupId, contact);
        ((UIFormSelectBoxWithGroups) uiAddress.getChildById(SELECT_GROUP)).setValue(selectedGroupId);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiAddress.getParent());
      }
      UIPopupAction uiPopupAction = uiContact.getAncestorOfType(UIPopupAction.class);
      uiPopupAction.cancelPopupAction();
    }
  }

  static public class AddGroupActionListener extends EventListener<UIAddContactForm> {
    public void execute(Event<UIAddContactForm> event) throws Exception {
      UIAddContactForm uiContactForm = event.getSource();
      UIPopupActionContainer popupContainer = uiContactForm.getParent();
      UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class);
      popupAction.activate(UIAddGroupForm.class, 650);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
    }
  }

  static public class ChangeImageActionListener extends EventListener<UIAddContactForm> {
    public void execute(Event<UIAddContactForm> event) throws Exception {
      UIAddContactForm uiContactForm = event.getSource();
      UIPopupActionContainer popupContainer = uiContactForm.getAncestorOfType(UIPopupActionContainer.class);
      UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class);
      popupAction.activate(UIImageForm.class, 500);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
    }
  }

  static public class DeleteImageActionListener extends EventListener<UIAddContactForm> {
    public void execute(Event<UIAddContactForm> event) throws Exception {
      UIAddContactForm uiContactForm = event.getSource();
      uiContactForm.setImage(null);
      uiContactForm.setFileName(null);
      uiContactForm.setMimeType(null);
      if (uiContactForm.getTempContact() != null)
        uiContactForm.getTempContact().setAttachment(null);
      event.getRequestContext()
           .addUIComponentToUpdateByAjax(uiContactForm.getAncestorOfType(UIPopupAction.class));
    }
  }

  protected void setImage(InputStream input) throws Exception {
    if (input != null) {
      imageBytes_ = new byte[input.available()];
      input.read(imageBytes_);
    } else
      imageBytes_ = null;
  }

  protected byte[] getImage() {
    return imageBytes_;
  }

  protected String getMimeType() {
    return imageMimeType_;
  };

  protected void setMimeType(String mimeType) {
    imageMimeType_ = mimeType;
  }

  protected void setFileName(String name) {
    fileName_ = name;
  }

  protected String getFileName() {
    return fileName_;
  }

}
