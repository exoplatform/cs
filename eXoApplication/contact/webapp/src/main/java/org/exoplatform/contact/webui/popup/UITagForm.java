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
import org.exoplatform.webui.form.UIFormInputInfo;
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
    template = "system:/groovy/webui/form/UIForm.gtmpl", 
    events = {
      @EventConfig(listeners = UITagForm.AddActionListener.class),
      @EventConfig(listeners = UITagForm.RemoveActionListener.class),
      @EventConfig(listeners = UITagForm.CancelActionListener.class)
    }
)
public class UITagForm extends UIForm implements UIPopupComponent {
  public static final String FIELD_TAGSOFCONTACT_INFO = "TagsOfContact";
  public static final String FIELD_TAGNAME_INPUT = "tagName";
  public static final String FIELD_COLOR= "color";
  public static final String RED = "Red".intern() ;
  public static final String BLUE = "Blue".intern() ;
  public static final String GREEN = "Green".intern() ;
  public static String[] FIELD_TAG_BOX = null;
  public static List<String> contactIds_ ;
  public static boolean isNew = true ;

  public UITagForm() throws Exception {
    setId("UITagForm") ;
    if (isNew) {
      addUIFormInput(new UIFormInputInfo(FIELD_TAGSOFCONTACT_INFO, FIELD_TAGSOFCONTACT_INFO, null)) ;
      ContactService contactService = ContactUtils.getContactService();
      String username = ContactUtils.getCurrentUser() ;
      Contact contact ;
      for (String contactId : contactIds_) {
        contact = contactService.getContact(username, contactId) ;
        if (contact == null) contact = contactService.getSharedContact(contactId) ;
        String[] tags  = null ;
        if (contact != null) { 
          tags = contact.getTags() ;
          StringBuffer buffer = new StringBuffer(contact.getFullName() + ": ") ;          
          if (tags != null) {
            if (tags.length > 0) {
              buffer.append(tags[0]) ;
              for (int i = 1; i < tags.length; i ++) buffer.append(", " + tags[i]) ;
            }
          }
          String info = buffer.toString() ;
          addUIFormInput(new UIFormInputInfo(info, info,  null)) ;
        }
      }
      addUIFormInput(new UIFormStringInput(FIELD_TAGNAME_INPUT, FIELD_TAGNAME_INPUT, null));
      List<SelectItemOption<String>> colors = new ArrayList<SelectItemOption<String>>() ;
      colors.add(new SelectItemOption<String>(RED,RED)) ;
      colors.add(new SelectItemOption<String>(BLUE,BLUE)) ;
      colors.add(new SelectItemOption<String>(GREEN,GREEN)) ;
      addUIFormInput(new UIFormSelectBox(FIELD_COLOR, FIELD_COLOR, colors)) ;
      List<Tag> tags = contactService.getTags(username);
      FIELD_TAG_BOX = new String[tags.size()];
      for (int i = 0 ; i < tags.size(); i ++) {
        FIELD_TAG_BOX[i] = tags.get(i).getName();
        addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_TAG_BOX[i], FIELD_TAG_BOX[i], false));
      }
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
    for (int i = 0; i < FIELD_TAG_BOX.length; i ++) {
      if (getUIFormCheckBoxInput(FIELD_TAG_BOX[i]).isChecked()) {
        checkedTags.add(FIELD_TAG_BOX[i]);
      }
    }
    return checkedTags;
  }
  
  static  public class AddActionListener extends EventListener<UITagForm> {
    public void execute(Event<UITagForm> event) throws Exception {
      UITagForm uiTagForm = event.getSource() ;
      List<Tag> tags = new ArrayList<Tag>();
      Tag tag;
      String inputTag = uiTagForm.getUIStringInput(FIELD_TAGNAME_INPUT).getValue(); 
      if (!ContactUtils.isEmpty(inputTag)) {
        tag = new Tag();
        tag.setName(inputTag);
        tag.setColor(uiTagForm.getUIFormSelectBox(FIELD_COLOR).getValue()) ;
        tags.add(tag);
      }
      if (isNew) {
        for (String tagName : uiTagForm.getCheckedTags()) {
          tag = new Tag();
          tag.setName(tagName) ;
          tags.add(tag);
        } 
      }
      if (tags.size() == 0) {
        UIApplication uiApp = uiTagForm.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UITagForm.msg.tagName-required", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } 
      ContactService contactService = ContactUtils.getContactService();
      String username = ContactUtils.getCurrentUser() ;
      if (isNew)
        contactService.addTag(username, contactIds_, tags);
      else 
        System.out.println("\n\n tagName : " + inputTag + "\n\n");
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
      contactService.removeContactTag(username, contactIds_, uiForm.getCheckedTags()) ;
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
