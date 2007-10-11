/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui.popup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.MailSetting;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.Tag;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.UIFolderContainer;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.mail.webui.UIMessageArea;
import org.exoplatform.mail.webui.UIMessageList;
import org.exoplatform.mail.webui.UINavigationContainer;
import org.exoplatform.mail.webui.UISelectAccount;
import org.exoplatform.mail.webui.UITagContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;

import com.sun.corba.se.pept.protocol.MessageMediator;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UITagForm.AddActionListener.class), 
      @EventConfig(listeners = UITagForm.RemoveActionListener.class), 
      @EventConfig(listeners = UITagForm.CancelActionListener.class, phase = Phase.DECODE)
    }  
)
public class UITagForm extends UIForm implements UIPopupComponent{
  public static final String SELECT_AVAIABLE_TAG = "Tag Name";
  public static final String TAG_COLOR = "Choose Color" ;
  
  private Map<String, String> messageMap = new HashMap<String, String>() ;
  private Map<String, Tag> tagMap = new HashMap<String, Tag>();
  
  public UITagForm() { }
  
  public void setTagList(List<Tag> tagList) throws Exception {
    tagMap.clear();   
    addUIFormInput(new UIFormStringInput(SELECT_AVAIABLE_TAG, SELECT_AVAIABLE_TAG, null));
    
    List<SelectItemOption<String>> selectColor = new ArrayList<SelectItemOption<String>>();
    for (String color : Utils.TAG_COLOR) {
      selectColor.add(new SelectItemOption<String>(color, color));
    }
    addUIFormInput(new UIFormSelectBox(TAG_COLOR, TAG_COLOR, selectColor));
    
    for(Tag tag : tagList) {
      UIFormCheckBoxInput<Boolean> uiCheckBox = new UIFormCheckBoxInput<Boolean>(tag.getName(), tag.getName(), null);
      addUIFormInput(uiCheckBox) ;
      tagMap.put(tag.getName(), tag);
    }
  }
  
  public String[] getActions() { return new String[] {"Add", "Remove", "Cancel"}; }
  
  public List<Tag> getTagList() {
    return new ArrayList<Tag>(tagMap.values());
  }
  
  public void setMessageList(List<Message> messageList) throws Exception {
    messageMap.clear();
    UIMailPortlet uiPortlet = getAncestorOfType(UIMailPortlet.class);
    String username = uiPortlet.getCurrentUser() ;
    String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
    MailService mailSrv = getApplicationComponent(MailService.class);
    for(Message msg : messageList) {
      String mesSub = "Sub : " + ((msg.getSubject().length() >= 30) ? (msg.getSubject().substring(0, 30) + "...") : msg.getSubject());
      UIFormInputInfo uiTags = new UIFormInputInfo(mesSub, mesSub, null);
      String tags = "";
      if (msg.getTags() != null && msg.getTags().length > 0) {
        for (int i = 0; i < msg.getTags().length; i++) {
          if (i > 0) tags += ", ";
          Tag tag = mailSrv.getTag(username, accountId, msg.getTags()[i]);
          tags += "[" + tag.getName() + "]";
        }
      } else tags = "No tag";
      
      uiTags.setValue(tags);
      addUIFormInput(uiTags) ;
      messageMap.put(msg.getId(), msg.getId());
    }
  }
  
  public List<String> getMessageList() {
    return new ArrayList<String>(messageMap.values());
  }
  
  public List<Tag> getCheckedTags() throws Exception {
    List<Tag> tagList = new ArrayList<Tag>();
    for (Tag tag : getTagList()) {
      UIFormCheckBoxInput<Boolean> checkbox = getChildById(tag.getName());
      if (checkbox != null && checkbox.isChecked()) {
        tagList.add(tag);
      } 
    }
    return tagList;
  }
  
  public String getLabel(String id) { return id ;}
  
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}
  
  static  public class AddActionListener extends EventListener<UITagForm> {
    public void execute(Event<UITagForm> event) throws Exception {
      UITagForm uiTagForm = event.getSource(); 
      String newTagName = uiTagForm.getUIStringInput(SELECT_AVAIABLE_TAG).getValue();
      String tagColor = uiTagForm.getUIStringInput(TAG_COLOR).getValue();
      UIMailPortlet uiPortlet = uiTagForm.getAncestorOfType(UIMailPortlet.class);
      UITagContainer uiTagContainer = uiPortlet.findFirstComponentOfType(UITagContainer.class);
      String username = uiPortlet.getCurrentUser() ;
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      MailService mailSrv = uiTagForm.getApplicationComponent(MailService.class);
      List<Tag> tagList = new ArrayList<Tag>();

      if (newTagName != null && newTagName != "") {
        boolean isExist = false;
        for (Tag tag: mailSrv.getTags(username, accountId)) {
          if (tag.getName().equals(newTagName)) { 
            isExist = true;
            tagList.add(tag);
          }
        }
        if (!isExist) {
          Tag newTag = new Tag();
          newTag.setName(newTagName);
          newTag.setColor(tagColor);
          newTag.setDescription("Tag's description");
          tagList.add(newTag);
        } 
      }
      
      tagList.addAll(uiTagForm.getCheckedTags());
      mailSrv.addTag(username, accountId, uiTagForm.getMessageList(), tagList);
      UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType(UIMessageList.class);
      uiMessageList.updateList();
      uiPortlet.cancelAction() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTagContainer) ;
    }
  }
  
  static  public class RemoveActionListener extends EventListener<UITagForm> {
    public void execute(Event<UITagForm> event) throws Exception {
      UITagForm uiTagForm = event.getSource(); 
      UIMailPortlet uiPortlet = uiTagForm.getAncestorOfType(UIMailPortlet.class);
      String username = uiPortlet.getCurrentUser() ;
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      MailService mailSrv = uiTagForm.getApplicationComponent(MailService.class);
      List<String> tagList = new ArrayList<String>();
      for (Tag tag : uiTagForm.getCheckedTags()) {
        tagList.add(tag.getId());
      }
      mailSrv.removeMessageTag(username, accountId, uiTagForm.getMessageList(), tagList);
      UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType(UIMessageList.class);
      uiMessageList.updateList();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList) ;
      uiPortlet.cancelAction() ;
    }
  }
  
  static  public class CancelActionListener extends EventListener<UITagForm> {
    public void execute(Event<UITagForm> event) throws Exception {
      UITagForm uiForm = event.getSource() ;
      UIMailPortlet uiPortlet = uiForm.getAncestorOfType(UIMailPortlet.class);
      uiPortlet.cancelAction();
    }
  }
   
}
