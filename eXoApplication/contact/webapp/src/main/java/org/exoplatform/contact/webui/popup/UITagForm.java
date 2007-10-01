/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui.popup;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.Tag;
import org.exoplatform.contact.webui.UIContactPortlet;
import org.exoplatform.contact.webui.UIContacts;
import org.exoplatform.contact.webui.UITags;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIForm.gtmpl", 
    events = {
      @EventConfig(listeners = UITagForm.SaveActionListener.class),      
      @EventConfig(listeners = UITagForm.CancelActionListener.class)
    }
)
public class UITagForm extends UIForm implements UIPopupComponent {
  public static final String FIELD_TAGNAME_INPUT = "tagName";
  public static String[] FIELD_TAG_BOX = null;
  private List<String> contactIds_ ;

  public UITagForm() throws Exception {
    setId("UITagForm") ;
    addUIFormInput(new UIFormStringInput(FIELD_TAGNAME_INPUT, FIELD_TAGNAME_INPUT, null));
    ContactService contactService = ContactUtils.getContactService();
    String username = ContactUtils.getCurrentUser() ;
    List<Tag> tags = contactService.getTags(username);
    FIELD_TAG_BOX = new String[tags.size()];
    for (int i = 0 ; i < tags.size(); i ++) {
      FIELD_TAG_BOX[i] = tags.get(i).getName();
      addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_TAG_BOX[i], FIELD_TAG_BOX[i], false));
    }
  }
  
  public String[] getActions() { return new String[] {"Save", "Cancel"} ; }
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}
  public String getLabel(String id) throws Exception {
    try {
      return  super.getLabel(id) ;
    } catch (MissingResourceException mre) {
      return id ;  
    }
  } 
 
  public void setContacts(List<String> contactIds) { contactIds_ = contactIds ; }
  public List<String> getContacts() { return contactIds_ ; }

  public List<String> getCheckedTags() throws Exception {
    List<String> checkedTags = new ArrayList<String>();
    for (int i = 0; i < FIELD_TAG_BOX.length; i ++) {
      if (getUIFormCheckBoxInput(FIELD_TAG_BOX[i]).isChecked()) {
        checkedTags.add(FIELD_TAG_BOX[i]);
      }
    }
    return checkedTags;
  }
  
  static  public class SaveActionListener extends EventListener<UITagForm> {
    public void execute(Event<UITagForm> event) throws Exception {
      UITagForm uiTagForm = event.getSource() ;
      List<Tag> tags = new ArrayList<Tag>();
      Tag tag;
      String inputTag = uiTagForm.getUIStringInput(FIELD_TAGNAME_INPUT).getValue(); 
      if (inputTag != null && inputTag.trim().length() > 0) {
        tag = new Tag();
        tag.setName(inputTag);
        tags.add(tag);
      }
      for (String tagName : uiTagForm.getCheckedTags()) {
        tag = new Tag();
        tag.setName(tagName) ;
        tags.add(tag);
      }
      if (tags.size() == 0) {
        UIApplication uiApp = uiTagForm.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIAddNewTag.msg.tagName-required", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } 
      ContactService contactService = ContactUtils.getContactService();
      String username = ContactUtils.getCurrentUser() ;
      contactService.addTag(username, uiTagForm.getContacts(), tags);
      UIContactPortlet uiContactPortlet = uiTagForm.getAncestorOfType(UIContactPortlet.class);
      UIContacts uiContacts = uiContactPortlet.findFirstComponentOfType(UIContacts.class) ;
      Contact contact ;
      for (String contactId : uiTagForm.getContacts()) {
        contact = contactService.getContact(username, contactId) ;
        if (contact == null)
          contact = contactService.getSharedContact(contactId) ;
        if (contact != null) uiContacts.updateContact(contact, false) ;
      }
      UITags uiTags = uiContactPortlet.findFirstComponentOfType(UITags.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTags) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts) ;
      uiContactPortlet.cancelAction() ;  
    }
  }

  static  public class CancelActionListener extends EventListener<UITagForm> {
    public void execute(Event<UITagForm> event) throws Exception {
      UITagForm uiForm = event.getSource() ;
      UIContactPortlet contactPortlet = uiForm.getAncestorOfType(UIContactPortlet.class) ;
      contactPortlet.cancelAction() ; 
    }
  }
}
