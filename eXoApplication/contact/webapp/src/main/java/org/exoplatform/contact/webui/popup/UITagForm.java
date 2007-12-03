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
import org.exoplatform.contact.service.JCRPageList;
import org.exoplatform.contact.service.Tag;
import org.exoplatform.contact.webui.UIContactPortlet;
import org.exoplatform.contact.webui.UIContacts;
import org.exoplatform.contact.webui.UITags;
import org.exoplatform.contact.webui.UIWorkingContainer;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;

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
      @EventConfig(listeners = UITagForm.RemoveActionListener.class),
      @EventConfig(listeners = UITagForm.CancelActionListener.class)
    }
)
public class UITagForm extends UIForm implements UIPopupComponent {
  public static final String FIELD_TAGNAME_INPUT = "tagName";
  public static final String NO_TAG_INFO = "no Tag";
  public static final String FIELD_COLOR= "color";
  public static final String RED = "Red".intern() ;
  public static final String BLUE = "Blue".intern() ;
  public static final String GREEN = "Green".intern() ;
  public static String[] FIELD_TAG_BOX_KEY = null;
  public static String[] FIELD_TAG_BOX_LABLE = null;
  private List<String> contactIds_ ;
  private String[] tagNames = null ;
  private String[] contactNames = null ;
  public UITagForm() throws Exception { setId("UITagForm") ; }
  
  public List<String> getContactIds() { return contactIds_ ;}
  public void setContactIds(List<String> contactIds) { contactIds_ = contactIds ; }
  
  public void update() throws Exception {
    getChildren().clear() ;
    ContactService contactService = ContactUtils.getContactService();
    String username = ContactUtils.getCurrentUser() ;
    int i = 0 ;
    contactNames = new String[contactIds_.size()] ;
    tagNames = new String[contactIds_.size()] ;
    for (String contactId : contactIds_) {
      Contact contact = contactService.getContact(username, contactId) ;
      if (contact == null) contact = contactService.getSharedContact(contactId) ;
      String[] tagIds  = null ;
      if (contact != null) { 
        tagIds = contact.getTags() ;
        StringBuffer buffer = new StringBuffer("") ;          
        if (tagIds != null && tagIds.length > 0) {
          boolean hascomma = false ;
          Tag tag = contactService.getTag(username, tagIds[0]) ;
          if (tag != null) {
            buffer.append(tag.getName()) ;
            hascomma = true ;
          }          
          for (int j = 1; j < tagIds.length; j ++) {
            tag = contactService.getTag(username, tagIds[j]) ;  
            if (tag != null && hascomma) buffer.append(", " + tag.getName()) ;
            else if (tag != null){
              hascomma = true ;
              buffer.append(tag.getName()) ;  
            }
          }   
          
        }
        if (ContactUtils.isEmpty(buffer.toString())) buffer.append(NO_TAG_INFO) ;
        contactNames[i] = contact.getFullName() ;
        tagNames[i] = buffer.toString() ;
        i ++ ;
      }
    }
    addUIFormInput(new UIFormStringInput(FIELD_TAGNAME_INPUT, FIELD_TAGNAME_INPUT, null));
    List<SelectItemOption<String>> colors = new ArrayList<SelectItemOption<String>>() ;
    colors.add(new SelectItemOption<String>(RED,RED)) ;
    colors.add(new SelectItemOption<String>(BLUE,BLUE)) ;
    colors.add(new SelectItemOption<String>(GREEN,GREEN)) ;
    addUIFormInput(new UIFormSelectBox(FIELD_COLOR, FIELD_COLOR, colors)) ;
    List<Tag> tags = contactService.getTags(username);
    FIELD_TAG_BOX_KEY = new String[tags.size()];
    FIELD_TAG_BOX_LABLE = new String[tags.size()] ;
    for (int k = 0 ; k < tags.size(); k ++) {
      Tag tag = tags.get(k) ;
      FIELD_TAG_BOX_KEY[k] = tag.getId();
      FIELD_TAG_BOX_LABLE[k] = tag.getName() ;
      addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_TAG_BOX_LABLE[k], FIELD_TAG_BOX_KEY[k], false));
    }
  }
  
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
    for (int i = 0; i < FIELD_TAG_BOX_LABLE.length; i ++) {
      if (getUIFormCheckBoxInput(FIELD_TAG_BOX_LABLE[i]).isChecked()) {
        checkedTags.add(FIELD_TAG_BOX_KEY[i]);
      }
    }
    return checkedTags;
  }
  
  static  public class AddActionListener extends EventListener<UITagForm> {
    public void execute(Event<UITagForm> event) throws Exception { 
      UITagForm uiTagForm = event.getSource() ;
      ContactService contactService = ContactUtils.getContactService();
      String username = ContactUtils.getCurrentUser() ;
      UIApplication uiApp = uiTagForm.getAncestorOfType(UIApplication.class) ;
      List<Tag> tags = new ArrayList<Tag>();
      Tag tag;
      String inputTag = uiTagForm.getUIStringInput(FIELD_TAGNAME_INPUT).getValue();
      if (!ContactUtils.isEmpty(inputTag)) {
        if (ContactUtils.isTagNameExisted(inputTag)) {
          uiApp.addMessage(new ApplicationMessage("UITagForm.msg.tagName-existed", null, 
              ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
        tag = new Tag();
        tag.setName(inputTag);
        tag.setColor(uiTagForm.getUIFormSelectBox(FIELD_COLOR).getValue()) ;
        tags.add(tag);
      }
      for (String tagId : uiTagForm.getCheckedTags()) {
        tag = contactService.getTag(username, tagId) ;
        tags.add(tag);
      } 
      if (tags.size() == 0) {
        uiApp.addMessage(new ApplicationMessage("UITagForm.msg.tagName-required", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } 
      contactService.addTag(username, uiTagForm.getContactIds(), tags);
      UIContactPortlet uiContactPortlet = uiTagForm.getAncestorOfType(UIContactPortlet.class);
      UIContacts uiContacts = uiContactPortlet.findFirstComponentOfType(UIContacts.class) ;
      uiContacts.updateList() ;
      UITags uiTags = uiContactPortlet.findFirstComponentOfType(UITags.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTags) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContacts) ;
      uiContactPortlet.cancelAction() ;  
    }
  }
  
  static  public class RemoveActionListener extends EventListener<UITagForm> {
    public void execute(Event<UITagForm> event) throws Exception {
      UITagForm uiForm = event.getSource() ;
      ContactService contactService = ContactUtils.getContactService() ; 
      String username = ContactUtils.getCurrentUser() ;
      List<String> checkedTags = uiForm.getCheckedTags() ;
      if (checkedTags == null || checkedTags.size() ==0) {
        UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UITagForm.msg.checkTag-required", null,
            ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;   
      }
      contactService.removeContactTag(username, uiForm.getContactIds(), checkedTags) ;
      UIContactPortlet contactPortlet = uiForm.getAncestorOfType(UIContactPortlet.class) ;
      UITags uiTags = contactPortlet.findFirstComponentOfType(UITags.class) ;
      String selectedTag = uiTags.getSelectedTag() ;
      UIContacts uiContacts = contactPortlet.findFirstComponentOfType(UIContacts.class) ;
      if (!ContactUtils.isEmpty(selectedTag)) {
        uiContacts.setContacts(contactService.getContactPageListByTag(username, selectedTag)) ;
      } else {
        uiContacts.updateList() ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(contactPortlet.getChild(UIWorkingContainer.class)) ;
      contactPortlet.cancelAction() ; 
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
