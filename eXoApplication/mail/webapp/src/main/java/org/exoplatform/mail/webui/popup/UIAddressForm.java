/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui.popup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactFilter;
import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.DataPageList;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL 
 * Author : Nguyen Hai <haiexo10022@gmail.com>
 *          Phung Nam <phunghainam@gmail.com>
 * Sep 25, 2007
 */
@ComponentConfig(lifecycle = UIFormLifecycle.class, 
    template = "app:/templates/mail/webui/UIAddressForm.gtmpl", 
    events = {
      @EventConfig(listeners = UIAddressForm.ChangeGroupActionListener.class),
      @EventConfig(listeners = UIAddressForm.SearchContactActionListener.class),
      @EventConfig(listeners = UIAddressForm.SaveActionListener.class),
      @EventConfig(listeners = UIAddressForm.CancelActionListener.class, phase = Phase.DECODE)
    })
    
public class UIAddressForm extends UIForm implements UIPopupComponent {
  public static final String ALL_GROUP_ITEM = "All Group".intern();
  public static final String CONTACT_SEARCH = "contact-search".intern();
  public static final String CONTACT_GROUP = "contact-group".intern();
  public static final String CHANGE_GROUP = "change-group".intern();
  
  private List<Contact> alreadyCheckedContact = new ArrayList<Contact>();
  
  private String recipientsType = "";

  public void setRecipientsType(String type) {
    recipientsType = type;
  }

  public String getRecipientType() {
    return recipientsType;
  }

  public UIAddressForm() throws Exception {
    String username = MailUtils.getCurrentUser();
    ContactService contactSrv = getApplicationComponent(ContactService.class);
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
    options.add(new SelectItemOption<String>(ALL_GROUP_ITEM, ""));
    for (ContactGroup group : contactSrv.getGroups(username)) {
      options.add(new SelectItemOption<String>(group.getName(), group.getId()));
    }
    addUIFormInput(new UIFormStringInput(CONTACT_SEARCH, CONTACT_SEARCH, null));
    UIFormSelectBox uiSelectGroup = new UIFormSelectBox(CONTACT_GROUP, CONTACT_GROUP, options);
    uiSelectGroup.setOnChange("ChangeGroup");
    addUIFormInput(uiSelectGroup);
    setContactList();
  }

  public UIAddressForm(String recipientsType) throws Exception {
    setRecipientsType("recipientsType");
    setContactList();
  }

  public String[] getActions() {
    return new String[] { "Save", "Cancel" };
  }

  private Map<String, Contact> contactMap_ = new HashMap<String, Contact>();

  public void activate() throws Exception { }

  public void deActivate() throws Exception { }

  public List<Contact> getContacts() throws Exception {
    return new ArrayList<Contact>(contactMap_.values());
  }

  public void setContactList() throws Exception {
    setContactList("");
  }

  public void setContactList(String groupId) throws Exception {
    List<Contact> contacts = new ArrayList<Contact>();
    ContactService contactSrv = getApplicationComponent(ContactService.class);
    String username = Util.getPortalRequestContext().getRemoteUser();
    if (groupId == null || groupId == "") {
      contacts = contactSrv.getAllContact(username);
    } else {
      contacts = contactSrv.getContactPageListByGroup(username, groupId).getAll();
    }
    setContactList(contacts);
  }

  public void setContactList(List<Contact> contactList) throws Exception {
    Iterator iter = getChildren().iterator();
    while (iter.hasNext()) {
      if (iter.next() instanceof UIFormCheckBoxInput) {
        iter.remove();        
      }
    }
    contactMap_.clear();
    for (Contact contact : contactList) {
      UIFormCheckBoxInput<Boolean> uiCheckbox = new UIFormCheckBoxInput<Boolean>(contact.getId(),
          contact.getId(), false);
      addUIFormInput(uiCheckbox);
      for (Contact ct : getAlreadyCheckedContact()) {
        if (ct.getId().equals(contact.getId())) {
          uiCheckbox.setChecked(true);
        }
      }
      contactMap_.put(contact.getId(), contact);
    }
  }

  public void setAlreadyCheckedContact(List<Contact> alreadyCheckedContact) throws Exception {
    this.alreadyCheckedContact = alreadyCheckedContact;
  }

  public List<Contact> getAlreadyCheckedContact() {
    return alreadyCheckedContact;
  }

  public List<Contact> getCheckedContact() throws Exception {
    List<Contact> contactList = new ArrayList<Contact>();
    for (Contact contact : getContacts()) {
      UIFormCheckBoxInput<Boolean> uiCheckbox = getChildById(contact.getId());
      if (uiCheckbox != null && uiCheckbox.isChecked()) {
        contactList.add(contact);
      }
    }
    return contactList;
  }
  
  static public class ChangeGroupActionListener extends EventListener<UIAddressForm> {
    public void execute(Event<UIAddressForm> event) throws Exception {
      UIAddressForm uiAddressForm = event.getSource();
      String groupId = uiAddressForm.getUIFormSelectBox(CONTACT_GROUP).getValue();
      uiAddressForm.setContactList(groupId);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressForm);
    }
  }
  
  static public class SearchContactActionListener extends EventListener<UIAddressForm> {
    public void execute(Event<UIAddressForm> event) throws Exception {
      UIAddressForm uiAddressForm = event.getSource();
      String groupId = uiAddressForm.getUIFormSelectBox(CONTACT_GROUP).getValue();
      String text = uiAddressForm.getUIStringInput(CONTACT_SEARCH).getValue();
      String username = MailUtils.getCurrentUser();
      ContactService contactSrv = uiAddressForm.getApplicationComponent(ContactService.class);
      ContactFilter filter = new ContactFilter();
      if (groupId != null && !groupId.equals("")) 
        filter.setCategories(new String[] {groupId});
      filter.setText(text);
      DataPageList pageList = contactSrv.searchContact(username, filter);
      uiAddressForm.setContactList(pageList.getAll());
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressForm);
    }
  }

  static public class SaveActionListener extends EventListener<UIAddressForm> {
    public void execute(Event<UIAddressForm> event) throws Exception {
      UIAddressForm uiAddressForm = event.getSource();
      UIMailPortlet uiPortlet = uiAddressForm.getAncestorOfType(UIMailPortlet.class);
      String toAddress = "";
      for (Contact contact : uiAddressForm.getCheckedContact()) {
        toAddress += contact.getFullName() + "<" + contact.getEmailAddress() + "> ,";
      }

      UIComposeForm uiComposeForm = uiPortlet.findFirstComponentOfType(UIComposeForm.class);
      if (uiAddressForm.getRecipientType().equals("to")) {
        uiComposeForm.setFieldToValue(toAddress);
        uiComposeForm.setToContacts(uiAddressForm.getCheckedContact());
      }

      if (uiAddressForm.getRecipientType().equals("cc")) {
        uiComposeForm.setFieldCcValue(toAddress);
        uiComposeForm.setCcContacts(uiAddressForm.getCheckedContact());
      }

      if (uiAddressForm.getRecipientType().equals("bcc")) {
        uiComposeForm.setFieldBccValue(toAddress);
        uiComposeForm.setBccContacts(uiAddressForm.getCheckedContact());
      }
    }
  }

  static public class CancelActionListener extends EventListener<UIAddressForm> {
    public void execute(Event<UIAddressForm> event) throws Exception {
      UIAddressForm uiAddressForm = event.getSource();
      uiAddressForm.deActivate();
    }
  }
}
