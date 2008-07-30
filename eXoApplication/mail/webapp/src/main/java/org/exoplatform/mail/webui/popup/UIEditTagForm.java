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
package org.exoplatform.mail.webui.popup;

import java.util.List;

import org.exoplatform.mail.Colors;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Tag;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.mail.webui.UIMessageArea;
import org.exoplatform.mail.webui.UIMessageList;
import org.exoplatform.mail.webui.UISelectAccount;
import org.exoplatform.mail.webui.UITagContainer;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;


/**
 * Created by The eXo Platform SARL
 * Author : Hai Nguyen      
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

  final public static String NEW_TAG_NAME = "newTagName" ;
  final public static String DESCRIPTION = "description" ;
  final public static String COLOR = "color" ;
  
  private String tagId;
  public UIEditTagForm() {       
    addUIFormInput(new UIFormStringInput(NEW_TAG_NAME, NEW_TAG_NAME, null)) ;
    addUIFormInput(new UIFormColorPicker(COLOR, COLOR, Colors.COLORS)) ;
    addUIFormInput(new UIFormTextAreaInput(DESCRIPTION,DESCRIPTION,null)) ;    
  }
  
  public String getSelectedColor() {
    return getChild(UIFormColorPicker.class).getValue() ;
  }
  public void setSelectedColor(String value) {
    getChild(UIFormColorPicker.class).setValue(value) ;
  }
  public String getTagId() throws Exception { return tagId; }
  
  public void setTag(String tagId) throws Exception {
    this.tagId = tagId;
    
    MailService mailSrv = getApplicationComponent(MailService.class);
    String username = MailUtils.getCurrentUser();
    String accountId = getAncestorOfType(UIMailPortlet.class).findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
    List<Tag> tagList= mailSrv.getTags(SessionProviderFactory.createSystemProvider(), username, accountId);
    
    if (tagList.isEmpty()) return;   
    
    for (Tag tag : tagList) {      
      if (tag.getId().equals(tagId)){
        getUIStringInput(NEW_TAG_NAME).setValue(tag.getName()); 
        getUIFormTextAreaInput(DESCRIPTION).setValue(tag.getDescription());
        getChild(UIFormColorPicker.class).setValue(tag.getColor()) ;       
      }
    }
  }
  
  public void activate() throws Exception {}
  public void deActivate() throws Exception{}
 
  static  public class SaveActionListener extends EventListener<UIEditTagForm> {
    public void execute(Event<UIEditTagForm> event) throws Exception {
      UIEditTagForm uiEditTagForm  = event.getSource() ;
      UIMailPortlet uiPortlet = uiEditTagForm.getAncestorOfType(UIMailPortlet.class);
      UIMailPortlet uiMailPortlet = uiEditTagForm.getAncestorOfType(UIMailPortlet.class);
      MailService mailService = uiEditTagForm.getApplicationComponent(MailService.class) ;

      String username = uiMailPortlet.getCurrentUser() ;
      String accountId =  uiMailPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue() ;
      String tagId = uiEditTagForm.getTagId();
      String newTagName = uiEditTagForm.getUIStringInput(NEW_TAG_NAME).getValue().trim() ;
      String description = uiEditTagForm.getUIFormTextAreaInput(DESCRIPTION).getValue() ;
      String color = uiEditTagForm.getSelectedColor(); 
      UIApplication uiApp = uiEditTagForm.getAncestorOfType(UIApplication.class) ;
      
      if(Utils.isEmptyField(newTagName)) {
        uiApp.addMessage(new ApplicationMessage("UIEditTagForm.msg.name-required", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }

      if (tagId != null) {
        try {      
          uiEditTagForm.setTag(tagId);        
          List<Tag> tagList = mailService.getTags(SessionProviderFactory.createSystemProvider(), username, accountId);
          for (Tag tag : tagList) {
            if(tag.getName().equals(newTagName)&&!tag.getId().equals(tagId)) {
              uiApp.addMessage(new ApplicationMessage("UIEditTagForm.msg.tag-already-exists", new Object[]{newTagName})) ;
              event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
              return ;
            }
            if (tag.getId().equals(tagId)){
              tag.setName(newTagName);
              tag.setColor(color);
              tag.setDescription(description);
              mailService.updateTag(SessionProviderFactory.createSystemProvider(), username, accountId, tag);
            }
          }
        } catch (Exception e){
          uiApp.addMessage(new ApplicationMessage("UIRenameTagForm.msg.error-rename-tag", null)) ;
          e.printStackTrace() ;
        }
        UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType(UIMessageList.class);
        uiMessageList.updateList();
        event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class)) ;
      } else {
        Tag newTag = new Tag() ;
        newTag.setName(newTagName);
        newTag.setColor(color);
        newTag.setDescription(description);
        mailService.addTag(SessionProviderFactory.createSystemProvider(), username, accountId, newTag);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.findFirstComponentOfType(UIMessageArea.class)) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.findFirstComponentOfType(UITagContainer.class)) ;
      uiEditTagForm.getAncestorOfType(UIPopupAction.class).deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiEditTagForm.getAncestorOfType(UIPopupAction.class)) ;
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIEditTagForm> {
    public void execute(Event<UIEditTagForm> event) throws Exception {
      UIEditTagForm uiForm = event.getSource() ;
      uiForm.getAncestorOfType(UIPopupAction.class).deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIPopupAction.class)) ;
    }
  }
}
