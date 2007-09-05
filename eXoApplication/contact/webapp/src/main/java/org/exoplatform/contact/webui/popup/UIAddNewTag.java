/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui.popup;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;

import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.Tag;
import org.exoplatform.contact.webui.UIContactPortlet;
import org.exoplatform.contact.webui.UIContacts;
import org.exoplatform.portal.webui.util.Util;
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
    template = "app:/templates/contact/webui/popup/UIAddNewTag.gtmpl", 
    events = {
      @EventConfig(listeners = UIAddNewTag.SaveActionListener.class),      
      @EventConfig(listeners = UIAddNewTag.CancelActionListener.class)
    }
)
public class UIAddNewTag extends UIForm implements UIPopupComponent {
  public static final String FIELD_TAGNAME_INPUT = "tagName";

  public static String[] FIELD_SHAREDCONTACT_BOX = null;

  public UIAddNewTag() throws Exception {
    addUIFormInput(new UIFormStringInput(FIELD_TAGNAME_INPUT, FIELD_TAGNAME_INPUT, null));

    ContactService contactService = getApplicationComponent(ContactService.class);
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    List<Tag> tags = contactService.getTags(username);

    FIELD_SHAREDCONTACT_BOX = new String[tags.size()];
    for (int i = 0 ; i < tags.size(); i ++) {
      FIELD_SHAREDCONTACT_BOX[i] = tags.get(i).getName();
      addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_SHAREDCONTACT_BOX[i], FIELD_SHAREDCONTACT_BOX[i], false));
    }
  }

  public String getLabel(String id) throws Exception {
    try {
      return  super.getLabel(id) ;
    }
    catch (MissingResourceException mre) {

    }
    return null ;

  } 

  public String[] getActions() { return new String[] {"Save", "Cancel"} ; }

  public void activate() throws Exception {
    // TODO Auto-generated method stub
  }

  public void deActivate() throws Exception {
    // TODO Auto-generated method stub
  }

  public List<String> getCheckedTags() throws Exception {
    List<String> checkedTags = new ArrayList<String>();
    for (int i = 0; i < FIELD_SHAREDCONTACT_BOX.length; i ++) {
      if (getUIFormCheckBoxInput(FIELD_SHAREDCONTACT_BOX[i]).isChecked()) {
        checkedTags.add(FIELD_SHAREDCONTACT_BOX[i]);
      }
    }
    return checkedTags;
  }

  static  public class SaveActionListener extends EventListener<UIAddNewTag> {
    public void execute(Event<UIAddNewTag> event) throws Exception {
      UIAddNewTag uiForm = event.getSource() ;
      UIContactPortlet uiContactPortlet = uiForm.getAncestorOfType(UIContactPortlet.class);
      UIContacts uiContacts = uiContactPortlet.findFirstComponentOfType(UIContacts.class);
      List<String> contactIds = uiContacts.getCheckedContacts();
      List<Tag> tags = new ArrayList<Tag>();
      Tag tag;
      String inputTag = uiForm.getUIStringInput(FIELD_TAGNAME_INPUT).getValue(); 
      if (inputTag != null && (!inputTag.trim().equals(""))) {
        tag = new Tag();
        tag.setName(inputTag);
        tags.add(tag);
      }
      for (String tagName : uiForm.getCheckedTags()) {
        tag = new Tag();
        tag.setName(tagName) ;
        tags.add(tag);
      }
      if (tags.size() == 0) {
        UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIAddNewTag.msg.tagName-required", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      ContactService contactService = uiForm.getApplicationComponent(ContactService.class);
      String username = Util.getPortalRequestContext().getRemoteUser() ;
      contactService.addTag(username, contactIds, tags);

      uiContactPortlet.cancelAction() ;  
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContactPortlet) ;
    }
  }

  static  public class CancelActionListener extends EventListener<UIAddNewTag> {
    public void execute(Event<UIAddNewTag> event) throws Exception {
      UIAddNewTag uiForm = event.getSource() ;
      UIContactPortlet contactPortlet = uiForm.getAncestorOfType(UIContactPortlet.class) ;
      contactPortlet.cancelAction() ; 
    }
  }
}
