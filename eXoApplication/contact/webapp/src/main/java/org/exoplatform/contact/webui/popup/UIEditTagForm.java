/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.SessionsUtils;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.Tag;
import org.exoplatform.contact.webui.UIContactPortlet;
import org.exoplatform.contact.webui.UIContacts;
import org.exoplatform.contact.webui.UITags;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
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
      @EventConfig(listeners = UIEditTagForm.SaveActionListener.class),      
      @EventConfig(listeners = UIEditTagForm.CancelActionListener.class)
    }
)
public class UIEditTagForm extends UIForm implements UIPopupComponent {
  private String tagId_ = null ;
  public static final String FIELD_TAGNAME_INPUT = "tagName";
  public static final String FIELD_COLOR = "color";
  public static final String RED = "Red".intern() ;
  public static final String BLUE = "Blue".intern() ;
  public static final String GREEN = "Green".intern() ;
  
  public UIEditTagForm() {
    addUIFormInput(new UIFormStringInput(FIELD_TAGNAME_INPUT, FIELD_TAGNAME_INPUT, null));
    List<SelectItemOption<String>> colors = new ArrayList<SelectItemOption<String>>() ;
    colors.add(new SelectItemOption<String>(RED,RED)) ;
    colors.add(new SelectItemOption<String>(BLUE,BLUE)) ;
    colors.add(new SelectItemOption<String>(GREEN,GREEN)) ;
    addUIFormInput(new UIFormSelectBox(FIELD_COLOR, FIELD_COLOR, colors)) ;    
  }
  
  public String[] getActions() { return new String[] {"Save", "Cancel"} ; }
  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
  
  public void setValues(String tagId) throws Exception {
    ContactService contactService = ContactUtils.getContactService();
    String username = ContactUtils.getCurrentUser() ;
    Tag tag = contactService.getTag(SessionsUtils.getSessionProvider(), username, tagId) ;
    tagId_ = tagId ;
    if (tag != null) {
      getUIStringInput(FIELD_TAGNAME_INPUT).setValue(tag.getName()) ;
      getUIFormSelectBox(FIELD_COLOR).setValue(tag.getColor()) ;
    }
  }
  
  static  public class SaveActionListener extends EventListener<UIEditTagForm> {
    public void execute(Event<UIEditTagForm> event) throws Exception {
      UIEditTagForm uiEditTagForm = event.getSource() ;
      String  tagName = uiEditTagForm.getUIStringInput(FIELD_TAGNAME_INPUT).getValue(); 
      UIApplication uiApp = uiEditTagForm.getAncestorOfType(UIApplication.class) ;
      if (ContactUtils.isEmpty(tagName)) {
        uiApp.addMessage(new ApplicationMessage("UIEditTagForm.msg.tagName-required", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ; 
      }
      ContactService contactService = ContactUtils.getContactService() ;
      String username = ContactUtils.getCurrentUser() ;
      Tag tag = contactService.getTag(SessionsUtils.getSessionProvider(), username, uiEditTagForm.tagId_) ;
      if (!tag.getName().equalsIgnoreCase(tagName) && ContactUtils.isTagNameExisted(tagName)) {
        uiApp.addMessage(new ApplicationMessage("UIEditTagForm.msg.tagName-existed", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      tag.setName(tagName) ;
      tag.setColor(uiEditTagForm.getUIFormSelectBox(FIELD_COLOR).getValue()) ;
      contactService.updateTag(SessionsUtils.getSessionProvider(), username, tag) ;
      UIContactPortlet uiContactPortlet = uiEditTagForm.getAncestorOfType(UIContactPortlet.class) ;
      WebuiRequestContext context = event.getRequestContext() ;
      context.addUIComponentToUpdateByAjax(uiContactPortlet.findFirstComponentOfType(UITags.class)) ;
      context.addUIComponentToUpdateByAjax(uiContactPortlet.findFirstComponentOfType(UIContacts.class)) ;
      uiContactPortlet.cancelAction() ;
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIEditTagForm> {
    public void execute(Event<UIEditTagForm> event) throws Exception {
      UIEditTagForm uiEditTagForm = event.getSource() ;
      UIPopupAction uiPopupAction = uiEditTagForm.getAncestorOfType(UIPopupAction.class) ;
      uiPopupAction.deActivate() ;
    }
  }
  
}
