/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.webui.popup.UIAddNewTag;
import org.exoplatform.contact.webui.popup.UICategorySelect;
import org.exoplatform.contact.webui.popup.UIContactForm;
import org.exoplatform.contact.webui.popup.UIPopupAction;
import org.exoplatform.contact.webui.popup.UIPopupContainer;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.organization.impl.GroupImpl;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputWithActions;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
    template =  "app:/templates/contact/webui/UIContacts.gtmpl",
    events = {
        @EventConfig(listeners = UIContacts.SelectedContactActionListener.class),
        @EventConfig(listeners = UIContacts.AddTagActionListener.class)
    }
)

public class UIContacts extends UIForm  {
  private String selectedGroupId_ ;
  private String selectedTag_;
  private boolean selectTag = false;
  private boolean isPersonalContact_ = true ;
  
  public static String[] FIELD_SHAREDCONTACT_BOX = null;
  public static final String FIELD_CHECKCONTACT_CHECKBOX =  "checkContact" ;
  
  public UIContacts() throws Exception {
    UIFormInputWithActions ShareTab = new UIFormInputWithActions(FIELD_CHECKCONTACT_CHECKBOX) ;
    getChildren().clear() ;
    FIELD_SHAREDCONTACT_BOX = new String[getContacts().size()];
    for(int i = 0; i < FIELD_SHAREDCONTACT_BOX.length; i ++) {
      FIELD_SHAREDCONTACT_BOX[i] = getContacts().get(i).getId();
      ShareTab.addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_SHAREDCONTACT_BOX[i],"", false));
    }
    addUIFormInput(ShareTab) ;
  } 
  
  public List<String> getCheckedContacts() {
    List<String> sharedGroups = new ArrayList<String>() ;
    for (int i = 0; i < FIELD_SHAREDCONTACT_BOX.length; i ++) {
      if (getUIFormCheckBoxInput(FIELD_SHAREDCONTACT_BOX[i]).isChecked()) {
        sharedGroups.add(FIELD_SHAREDCONTACT_BOX[i]) ;
      }
    }
    return sharedGroups ;
  }
  
  static public class AddTagActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContact = event.getSource() ;
      UIContactPortlet contactPortlet = uiContact.getAncestorOfType(UIContactPortlet.class) ;
      UIPopupAction popupAction = contactPortlet.getChild(UIPopupAction.class) ;
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
      popupContainer.addChild(UIAddNewTag.class, null, null) ;
      popupAction.activate(popupContainer, 500, 200, true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }

  protected String getGroupId() { return selectedGroupId_;}
  protected void setGroupId(String id) { selectedGroupId_ = id  ;}

  protected String getTagName() { return selectedTag_ ;}
  protected void setTagName(String name) { selectedTag_ = name ;}
  
  public void setIsPersonalContact(boolean isPersonal) {
    isPersonalContact_ = isPersonal ;
  } 
  public void setSelectTag(boolean selectTag) {
    this.selectTag = selectTag; 
  }
  
  public List<Contact> getContacts() throws Exception {
    List<Contact> contacts = new ArrayList<Contact>() ;
    if (selectTag == false) {
      if(isPersonalContact_) {
        if(selectedGroupId_ != null) {
          ContactService contactService = getApplicationComponent(ContactService.class);
          String username = Util.getPortalRequestContext().getRemoteUser() ;
          contacts.addAll(contactService.getContactsByGroup(username, getGroupId()));
        }
      } else {
        if(selectedGroupId_ != null) {
          UIContactPortlet uiPortlet = getAncestorOfType(UIContactPortlet.class) ;
          UIAddressBooks uiAddressBooks = uiPortlet.findFirstComponentOfType(UIAddressBooks.class) ;
          contacts.addAll(uiAddressBooks.getContactMapValue(selectedGroupId_)) ;
        }
      }
    } else {
      ContactService contactService = getApplicationComponent(ContactService.class);
      String username = Util.getPortalRequestContext().getRemoteUser() ;
      contacts.addAll(contactService.getContactsByTag(username, getTagName()));
    }
    return contacts ;
  }

  static public class SelectedContactActionListener extends EventListener<UIContacts> {
    public void execute(Event<UIContacts> event) throws Exception {
      UIContacts uiContacts = event.getSource();
      String contactId = event.getRequestContext().getRequestParameter(OBJECTID);
      ContactService contactService = uiContacts.getApplicationComponent(ContactService.class);
      String username = Util.getPortalRequestContext().getRemoteUser() ;
      Contact contact = null ;
      if (uiContacts.isPersonalContact_) {
        contact = contactService.getContact(username, contactId);
      } else {
        contact = contactService.getSharedContact(contactId);
      }
      if (contact == null) contact = contactService.getContact(username, contactId);
      UIContactContainer uiContactContainer = uiContacts.getAncestorOfType(UIContactContainer.class);
      UIContactPreview uiContactPreview = uiContactContainer.findFirstComponentOfType(UIContactPreview.class);
      uiContactPreview.setContact(contact);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContactPreview);
    }
  }
}
