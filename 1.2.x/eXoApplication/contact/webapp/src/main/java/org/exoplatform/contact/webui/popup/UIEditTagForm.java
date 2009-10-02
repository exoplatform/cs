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
import java.util.List;

import javax.jcr.PathNotFoundException;

import org.exoplatform.contact.Colors;
import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.Tag;
import org.exoplatform.contact.webui.UIContactPortlet;
import org.exoplatform.contact.webui.UIContacts;
import org.exoplatform.contact.webui.UIFormColorPicker;
import org.exoplatform.contact.webui.UITags;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;


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
      @EventConfig(listeners = UIEditTagForm.CancelActionListener.class, phase = Phase.DECODE)
    }
)
public class UIEditTagForm extends UIForm implements UIPopupComponent {
  private Tag tag_ = null ;
  public static final String FIELD_TAGNAME_INPUT = "tagName";
  public static final String FIELD_COLOR = "color";
  public static final String FIELD_DESCRIPTION_INPUT = "description";
  private boolean isNew = true ;
  
  public UIEditTagForm() throws Exception {
    addUIFormInput(new UIFormStringInput(FIELD_TAGNAME_INPUT, FIELD_TAGNAME_INPUT, null).addValidator(MandatoryValidator.class));
    addUIFormInput(new UIFormTextAreaInput(FIELD_DESCRIPTION_INPUT, FIELD_DESCRIPTION_INPUT, null)) ;
    addUIFormInput(new UIFormColorPicker(FIELD_COLOR, FIELD_COLOR, Colors.COLORS)) ;   
  }
  
  public String[] getActions() { return new String[] {"Save", "Cancel"} ; }
  public void activate() throws Exception { }
  public void deActivate() throws Exception { }

  public void setValues(Tag tag) throws Exception {
    isNew = false ;
    tag_ = tag ;
    if (tag != null) {
      getUIStringInput(FIELD_TAGNAME_INPUT).setValue(tag.getName()) ;
      getUIFormTextAreaInput(FIELD_DESCRIPTION_INPUT).setValue(tag.getDescription()) ;
      getChild(UIFormColorPicker.class).setValue(tag.getColor()) ;
    }
  }
  public boolean isNew() { return isNew; }
  public void setNew(boolean isNew) { this.isNew = isNew; }  
  
  static  public class SaveActionListener extends EventListener<UIEditTagForm> {
    public void execute(Event<UIEditTagForm> event) throws Exception {
      UIEditTagForm uiEditTagForm = event.getSource() ;
      String tagName = uiEditTagForm.getUIStringInput(FIELD_TAGNAME_INPUT).getValue(); 
      String des = uiEditTagForm.getUIFormTextAreaInput(FIELD_DESCRIPTION_INPUT).getValue() ;
      UIApplication uiApp = uiEditTagForm.getAncestorOfType(UIApplication.class) ;
      if (ContactUtils.isEmpty(tagName)) {
        uiApp.addMessage(new ApplicationMessage("UIEditTagForm.msg.tagName-required", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ; 
      }
      if (ContactUtils.isNameLong(tagName)) {
        uiApp.addMessage(new ApplicationMessage("UIEditTagForm.msg.nameTooLong", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ; 
      }
      
      UIContactPortlet uiContactPortlet = uiEditTagForm.getAncestorOfType(UIContactPortlet.class) ;
      UITags uiTags = uiContactPortlet.findFirstComponentOfType(UITags.class) ;
      if (uiEditTagForm.isNew) {
        for (Tag oldTag : uiTags.getTagMap().values()) 
          if (oldTag.getName().equals(tagName)) {
            uiApp.addMessage(new ApplicationMessage("UIEditTagForm.msg.tagName-existed", null, 
                ApplicationMessage.WARNING)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return ;
          }
        Tag tag = new Tag() ;
        tag.setName(tagName) ;
        tag.setDescription(des) ;
        tag.setColor(uiEditTagForm.getChild(UIFormColorPicker.class).getValue()) ;
        List<Tag> tags = new ArrayList<Tag>() ;
        tags.add(tag) ;
        ContactUtils.getContactService().addTag(SessionProviderFactory.createSessionProvider()
            , ContactUtils.getCurrentUser(), null, tags) ;        
      } else {
        Tag tag = uiEditTagForm.tag_ ;
        if (!tag.getName().equals(tagName))
          for (Tag oldTag : uiTags.getTagMap().values()) 
            if (oldTag.getName().equals(tagName)) {
              uiApp.addMessage(new ApplicationMessage("UIEditTagForm.msg.tagName-existed", null, 
                  ApplicationMessage.WARNING)) ;
              event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
              return ;
            }
        tag.setName(tagName) ;
        tag.setDescription(des) ;
        tag.setColor(uiEditTagForm.getChild(UIFormColorPicker.class).getValue()) ;
        try {
          ContactUtils.getContactService().updateTag(
              SessionProviderFactory.createSessionProvider(), ContactUtils.getCurrentUser(), tag) ;
        } catch (PathNotFoundException e) {
          uiApp.addMessage(new ApplicationMessage("UIEditTagForm.msg.tag-deleted", null, 
              ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
      }
      WebuiRequestContext context = event.getRequestContext() ;
      context.addUIComponentToUpdateByAjax(uiTags) ;
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
