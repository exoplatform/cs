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
package org.exoplatform.contact.webui.popup;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

import javax.jcr.PathNotFoundException;

import org.exoplatform.contact.Colors;
import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.Tag;
import org.exoplatform.contact.service.impl.JCRDataStorage;
import org.exoplatform.contact.webui.UIContactPortlet;
import org.exoplatform.contact.webui.UIContactPreview;
import org.exoplatform.contact.webui.UIContacts;
import org.exoplatform.contact.webui.UIFormColorPicker;
import org.exoplatform.contact.webui.UITags;
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
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.NameValidator;
import org.exoplatform.webui.form.validator.StringLengthValidator;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/contact/webui/popup/UITagForm.gtmpl", 
    events = {
      @EventConfig(listeners = UITagForm.AddActionListener.class),
      @EventConfig(listeners = UITagForm.RemoveActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UITagForm.CancelActionListener.class, phase = Phase.DECODE)
    }
)
public class UITagForm extends UIForm implements UIPopupComponent {
  public static final String FIELD_TAGNAME_INPUT = "tagName";
  public static final String NO_TAG_INFO = "no Tag";
  public static final String FIELD_COLOR= "color";
  
  private Map<String, String> tags = new LinkedHashMap<String, String>() ;
  private List<Contact> contacts_ = new ArrayList<Contact>();
  private String[] tagNames = null ;
  private String[] contactNames = null ;
  public UITagForm() throws Exception { setId("UITagForm") ; }  
  
  public void setContacts(List<Contact> contacts) throws Exception { 
    getChildren().clear() ;
    contacts_ = contacts ; 
    
    int i = 0 ;
    contactNames = new String[contacts_.size()] ;
    tagNames = new String[contacts_.size()] ;
    Map<String, Tag> tapMap = 
      getAncestorOfType(UIContactPortlet.class).findFirstComponentOfType(UITags.class).getTagMap() ;
    for(Tag tag : tapMap.values()) {
      tags.put(tag.getId(), tag.getName()) ;
    }
    for (Contact contact : contacts_) {
      String[] tagIds  = null ;
      if (contact != null) { 
        tagIds = contact.getTags() ;
        StringBuffer buffer = new StringBuffer() ;          
        if (tagIds != null && tagIds.length > 0) {
          for (int j = 0; j < tagIds.length; j ++) {
            Tag tag = tapMap.get(tagIds[j]) ;  
            if (tag != null && buffer.length() < 1) {
              buffer.append(tag.getName()) ;
            }
            else if (tag != null) {
              buffer.append(", " + tag.getName()) ;
            }
          }
        }
        if (ContactUtils.isEmpty(buffer.toString())) buffer.append(NO_TAG_INFO) ;
        String fullName = contact.getFullName() ;
        if (ContactUtils.isEmpty(fullName)) contactNames[i] = ContactUtils.emptyName() ;
        else contactNames[i] = ContactUtils.encodeHTML(fullName) ;
        tagNames[i] = buffer.toString() ;
        i ++ ;
      }
    }
    addUIFormInput(new UIFormStringInput(FIELD_TAGNAME_INPUT, FIELD_TAGNAME_INPUT, null)
      .addValidator(MandatoryValidator.class).addValidator(NameValidator.class).addValidator(StringLengthValidator.class,1,40));
    addUIFormInput(new UIFormColorPicker(FIELD_COLOR, FIELD_COLOR, Colors.COLORS)) ;
    for (String tagId : tags.keySet()) {
      addUIFormInput(new UIFormCheckBoxInput<Boolean>(tagId, tagId, false));
    }
  }
  public List<Contact> getContacts() { return contacts_ ;}
  public void setValues(String tagName) throws Exception {
    getUIStringInput(FIELD_TAGNAME_INPUT).setValue(tagName) ;   
  }
  
  public String[] getActions() { return new String[] {"Add", "Remove", "Cancel"} ; }
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}
  public String getLabel(String id) throws Exception {
    try {
      return  super.getLabel(id) ;
    } catch (MissingResourceException mre) {
      return id ;  
    }
  }

  public List<String> getCheckedTags() throws Exception {
    List<String> checkedTags = new ArrayList<String>();
    for (String tagId : tags.keySet()) {
      if (getUIFormCheckBoxInput(tagId).isChecked()) {
        checkedTags.add(tagId);
      }
    }
    return checkedTags;
  }
  
  protected String getSelectedColor() {
    return getChild(UIFormColorPicker.class).getValue() ;
  }
  protected void setSelectedColor(String value) {
    getChild(UIFormColorPicker.class).setValue(value) ;
  }
  
  static  public class AddActionListener extends EventListener<UITagForm> {
    public void execute(Event<UITagForm> event) throws Exception { 
      UITagForm uiTagForm = event.getSource() ;
      ContactService contactService = ContactUtils.getContactService();
      String username = ContactUtils.getCurrentUser() ;
      UIApplication uiApp = uiTagForm.getAncestorOfType(UIApplication.class) ;
      List<Tag> tags = new ArrayList<Tag>();

      Map<String, String> tagIds = new LinkedHashMap<String, String>() ;
      String inputTag = uiTagForm.getUIStringInput(FIELD_TAGNAME_INPUT).getValue();
//    CS-3009
      inputTag = ContactUtils.reduceSpace(inputTag) ;
      UIContactPortlet uiContactPortlet = uiTagForm.getAncestorOfType(UIContactPortlet.class);
      UITags uiTags = uiContactPortlet.findFirstComponentOfType(UITags.class) ;
      if (!ContactUtils.isEmpty(inputTag)) {
        for (Tag tag : uiTags.getTagMap().values())
          if (tag.getName().equals(inputTag)) {
            uiApp.addMessage(new ApplicationMessage("UITagForm.msg.tagName-existed", null, 
                ApplicationMessage.WARNING)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return ;
          }
       /* if (ContactUtils.isNameLong(inputTag)) {
          uiApp.addMessage(new ApplicationMessage("UITagForm.msg.nameTooLong", null, 
              ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }*/
        Tag tag = new Tag();
        tag.setName(inputTag);
        tag.setColor(uiTagForm.getSelectedColor()) ;
        tags.add(tag);
        tagIds.put(tag.getId(), tag.getId()) ;
      } 
      for (String tagId : uiTagForm.getCheckedTags()) {
        Tag tag = uiTags.getTagMap().get(tagId) ;
        tags.add(tag);
        tagIds.put(tagId, tagId) ;
      } 
      if (tags.size() == 0) {
        uiApp.addMessage(new ApplicationMessage("UITagForm.msg.tagName-required", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } 
      List<String> contactIds = new ArrayList<String>() ;
      UIContacts uiContacts = uiContactPortlet.findFirstComponentOfType(UIContacts.class) ;
      try {
        for (Contact contact : uiTagForm.contacts_) {
          if (contact == null) throw new PathNotFoundException() ;
          Map<String, String> newTagIds = new LinkedHashMap<String, String>() ;
          for (String key : tagIds.keySet()) newTagIds.put(key, key) ;
          if (contact.getTags() != null)
            for (String tagId : contact.getTags()) newTagIds.put(tagId, tagId) ;
          contact.setTags(newTagIds.keySet().toArray(new String[] {})) ;
          contactIds.add(contact.getId() + JCRDataStorage.SPLIT + contact.getContactType()) ;
        }      
        contactService.addTag(username, contactIds, tags);
      } catch (PathNotFoundException e) {
        uiApp.addMessage(new ApplicationMessage("UITagForm.msg.contact-not-existed", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTags) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts) ;
      uiContactPortlet.cancelAction() ;  
    }
  }
  
  static  public class RemoveActionListener extends EventListener<UITagForm> {
    public void execute(Event<UITagForm> event) throws Exception {      
      UITagForm uiForm = event.getSource() ;      
      List<String> checkedTags = uiForm.getCheckedTags() ;
      if (checkedTags == null || checkedTags.size() == 0) {
        UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UITagForm.msg.checkTag-required", null,
            ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;   
      }
      ContactService contactService = ContactUtils.getContactService() ; 
      String username = ContactUtils.getCurrentUser() ;
      List<String> contactIds = new ArrayList<String>() ;
      List<String> newContactIds = new ArrayList<String>() ;
      
      // cs-1653 
      List<String> noTags = new ArrayList<String>() ;
      noTags.addAll(checkedTags) ;
      for (Contact contact : uiForm.contacts_) {
        contactIds.add(contact.getId()) ;
        newContactIds.add(contact.getId() + JCRDataStorage.SPLIT + contact.getContactType()) ;
        if (contact.getTags() != null) {
          for (String tag : contact.getTags()) noTags.remove(tag) ;          
        }
      }
      try {
        contactService.removeContactTag(
            username, newContactIds, checkedTags) ;
      } catch (PathNotFoundException e) {
        UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UITagForm.msg.contact-not-exist-remove", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      
      UIContactPortlet contactPortlet = uiForm.getAncestorOfType(UIContactPortlet.class) ;
      UITags uiTags = contactPortlet.findFirstComponentOfType(UITags.class) ;
      String selectedTag = uiTags.getSelectedTag() ;
      UIContacts uiContacts = contactPortlet.findFirstComponentOfType(UIContacts.class) ;

      //comment to fix bug 215
      //if(uiContacts.isDisplaySearchResult()) uiContacts.setContact(uiForm.contacts_, false) ;      
      if (!ContactUtils.isEmpty(selectedTag)) {
        uiContacts.setContacts(contactService.getContactPageListByTag(
            username, selectedTag)) ;
      }
      if(uiContacts.isDisplaySearchResult() || 
          (ContactUtils.isEmpty(uiContacts.getSelectedGroup()) && ContactUtils.isEmpty(uiContacts.getSelectedTag()))) {
        List<Contact> contacts = new ArrayList<Contact>() ;
        for (String contactId : contactIds) {
          Contact contact = uiContacts.getContactMap().get(contactId) ;
          String[] oldTag = contact.getTags() ;
          List<String> newTags = new ArrayList<String>() ;
          if (oldTag != null)
            for (String tag : oldTag) {
              if (!checkedTags.contains(tag)) newTags.add(tag) ;
            }             
          contact.setTags(newTags.toArray(new String[] {})) ;
          contacts.add(contact) ;
        }
        uiContacts.setContact(contacts, true) ;
      }
      uiContacts.updateList() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTags) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts) ;
      
      if (uiContacts.getContactMap().size() == 0) {
        UIContactPreview uiContactPreview = contactPortlet.findFirstComponentOfType(UIContactPreview.class) ;
        uiContactPreview.setContact(null) ;
        if (uiContactPreview.isRendered())
          event.getRequestContext().addUIComponentToUpdateByAjax(uiContactPreview) ;
      }      
      contactPortlet.cancelAction() ; 
      if (noTags.size() > 0) {
        Map<String, Tag> tagMap = uiTags.getTagMap() ;
        StringBuilder builder = new StringBuilder("") ;
        for (String tag : noTags) {
          if (builder.length() > 0) builder.append(" ,") ;
          builder.append(tagMap.get(tag).getName()) ;
        }
        UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UITagForm.msg.noTagRemoved", new Object[]{builder.toString()}, 
            ApplicationMessage.INFO)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;       
      }
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
