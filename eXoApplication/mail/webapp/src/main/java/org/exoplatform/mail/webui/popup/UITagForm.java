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
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.Tag;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.UIFolderContainer;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.mail.webui.UIMessageArea;
import org.exoplatform.mail.webui.UIMessageList;
import org.exoplatform.mail.webui.UINavigationContainer;
import org.exoplatform.mail.webui.UISelectAccount;
import org.exoplatform.mail.webui.UITags;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
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
      @EventConfig(listeners = UITagForm.SaveActionListener.class), 
      @EventConfig(listeners = UITagForm.CancelActionListener.class, phase = Phase.DECODE)
    }  
)
public class UITagForm extends UIForm implements UIPopupComponent{
  public static final String SELECT_AVAIABLE_TAG = "New Tag";
  
  public Map<String, String> messageMap = new HashMap<String, String>() ;
  
  public UITagForm() { 
    addChild(new UIFormStringInput(SELECT_AVAIABLE_TAG, null, null));
  }
  
  public void createCheckBoxTagList(List<Tag> listTags) {
    removeChild(UIFormCheckBoxInput.class);
    for(Tag tag : listTags) {
      addUIFormInput(new UIFormCheckBoxInput<Boolean>(tag.getName(), tag.getName(), null)) ;
    }
  }
  
  public List<Tag> getCheckedTags() throws Exception {
    List<Tag> tagList = new ArrayList<Tag>();
    MailService mailServ = (MailService) PortalContainer.getComponent(MailService.class);
    UIMailPortlet uiPortlet = getAncestorOfType(UIMailPortlet.class);
    String username = uiPortlet.getCurrentUser() ;
    String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
    for (Tag tag : mailServ.getTags(username, accountId)) {
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
  
  static  public class SaveActionListener extends EventListener<UITagForm> {
    public void execute(Event<UITagForm> event) throws Exception {
      UITagForm uiTagForm = event.getSource(); 
      UIMailPortlet uiPortlet = uiTagForm.getAncestorOfType(UIMailPortlet.class);
      UIMessageArea uiMessageArea = uiPortlet.findFirstComponentOfType(UIMessageArea.class);
      String newTagName = uiTagForm.getUIStringInput(SELECT_AVAIABLE_TAG).getValue();
      List<String> messageList = new ArrayList<String>();
      messageList = Arrays.asList(uiTagForm.messageMap.values().toArray(new String[]{}));
      MailService service = uiTagForm.getApplicationComponent(MailService.class);
      List<Tag> tagList = new ArrayList<Tag>();

      if (newTagName != null && newTagName != "") {
        Tag newTag = new Tag();
        newTag.setName(newTagName);
        tagList.add(newTag);
      }
      for (Tag tag : uiTagForm.getCheckedTags()) {
        tagList.add(tag);
      }
      
      String username = uiPortlet.getCurrentUser() ;
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue() ;
      service.addTag(username, accountId, messageList, tagList);
      uiPortlet.cancelAction() ;
      UITags uiTags = uiPortlet.findFirstComponentOfType(UITags.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTags) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageArea) ;
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
