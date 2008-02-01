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

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.Tag;
import org.exoplatform.contact.webui.UIContactPortlet;
import org.exoplatform.contact.webui.UIContacts;
import org.exoplatform.contact.webui.UITags;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
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
  private Tag tag_ = null ;
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

  public void setValues(Tag tag) throws Exception {
    tag_ = tag ;
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
      Tag tag = uiEditTagForm.tag_ ;
      if (!tag.getName().equalsIgnoreCase(tagName) && ContactUtils.isTagNameExisted(tagName)) {
        uiApp.addMessage(new ApplicationMessage("UIEditTagForm.msg.tagName-existed", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      tag.setName(tagName) ;
      tag.setColor(uiEditTagForm.getUIFormSelectBox(FIELD_COLOR).getValue()) ;
      ContactUtils.getContactService().updateTag(
          SessionProviderFactory.createSessionProvider(), ContactUtils.getCurrentUser(), tag) ;
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
