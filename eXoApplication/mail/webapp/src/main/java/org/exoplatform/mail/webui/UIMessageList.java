/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui ;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.MessageFilter;
import org.exoplatform.mail.service.MessageHeader;
import org.exoplatform.mail.service.Tag;
import org.exoplatform.mail.webui.popup.UIPopupAction;
import org.exoplatform.mail.webui.popup.UITagForm;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;

import com.sun.corba.se.spi.legacy.connection.GetEndPointInfoAgainException;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/templates/mail/webui/UIMessageList.gtmpl",
    events = {
        @EventConfig(listeners = UIMessageList.SelectMessageActionListener.class),
        @EventConfig(listeners = UIMessageList.AddStarActionListener.class),
        @EventConfig(listeners = UIMessageList.RemoveStarActionListener.class),
        @EventConfig(listeners = UIMessageList.ReplyActionListener.class),
        @EventConfig(listeners = UIMessageList.ReplyAllActionListener.class),
        @EventConfig(listeners = UIMessageList.ForwardActionListener.class), 
        @EventConfig(listeners = UIMessageList.MarkAsReadActionListener.class),
        @EventConfig(listeners = UIMessageList.MarkAsUnReadActionListener.class),
        @EventConfig(listeners = UIMessageList.AddStarActionListener.class),
        @EventConfig(listeners = UIMessageList.RemoveStarActionListener.class),
        @EventConfig(listeners = UIMessageList.AddTagActionListener.class),
        @EventConfig(listeners = UIMessageList.MoveMessagesActionListener.class),
        @EventConfig(listeners = UIMessageList.ImportActionListener.class),
        @EventConfig(listeners = UIMessageList.ExportActionListener.class)
    }
)

public class UIMessageList extends UIForm {
  private String selectedMessageId_ = null ;
  private String selectedFolderId_ = null ;

  public UIMessageList() throws Exception {}

  protected String getSelectedMessageId() {return selectedMessageId_ ;}
  protected void setSelectedMessageId(String messageId) {selectedMessageId_ = messageId ;}
  
  protected String getSelectedFolderId() {return selectedFolderId_ ;}
  protected void setSelectedFolderId(String folderId) {selectedFolderId_ = folderId ;}
  
  public void initCheckboxForMessages()throws Exception {
    List<Message> messageList = getMessageByFolder();
    for (Message msg : messageList) {
      addChild(new UIFormCheckBoxInput<Boolean>(msg.getId(), msg.getId(), null));
    }
  }   
  
  public List<String> getCheckedMessage() throws Exception {
    List<String> messageList = new ArrayList<String>();
    for (Message msg : getMessageByFolder()) {
      UIFormCheckBoxInput<Boolean> uiCheckbox = getChildById(msg.getId());
      if (uiCheckbox != null && uiCheckbox.isChecked()) {
        messageList.add(msg.getId());
      }
    }
    return messageList;
  }
  
  public void removeCheckboxForMessages() throws Exception {
    //TODO: remove all checkboxs for message list before change folder
  }
  
  protected List<Message> getMessageByFolder() throws Exception {
    List<Message> messageList = new ArrayList<Message>() ;
    if(getSelectedFolderId() != null) {
      MailService mailSvr = getApplicationComponent(MailService.class) ;
      String username = getAncestorOfType(UIMailPortlet.class).getCurrentUser() ;
      String accountId = getAncestorOfType(UIMailPortlet.class).
      findFirstComponentOfType(UISelectAccount.class).getSelectedValue() ;
      MessageFilter filter = new MessageFilter("filterByFolder") ;
      Folder folder = mailSvr.getFolder(username, accountId, getSelectedFolderId()) ;
      filter.setFolder(new String[]{folder.getName()}) ;
      filter.setAccountId(accountId) ;
      List<MessageHeader> messageHeaders = new ArrayList<MessageHeader>() ;
      messageHeaders.addAll(mailSvr.getMessages(username, filter)) ;
      for(MessageHeader mh : messageHeaders) {
        messageList.add((Message)mh) ;
      }
    }
    return messageList ;
  }
  
  static public class SelectMessageActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;      
    }
  }
  
  static public class AddStarActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;      
    }
  }
  static public class RemoveStarActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;      
    }
  }
  
  static  public class ReplyActionListener extends EventListener<UIMessageList> {    
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiActionBar = event.getSource() ;      
    }
  }

  static public class ReplyAllActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiActionBar = event.getSource() ;      
    }
  } 
     
  static public class ForwardActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiActionBar = event.getSource() ;      
    }
  }  
  
  static public class MarkAsReadActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiActionBar = event.getSource() ;      
    }
  }
  
  static public class MarkAsUnReadActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiActionBar = event.getSource() ;      
    }
  }

  static public class AddTagActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource() ; 
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class);
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class);
      UITagForm uiTagForm = uiMessageList.createUIComponent(UITagForm.class, null, null) ;
      String username = uiPortlet.getCurrentUser();
      MailService mailService = uiMessageList.getApplicationComponent(MailService.class);
      UINavigationContainer uiNavigation = uiPortlet.getChild(UINavigationContainer.class) ;
      UISelectAccount uiSelect = uiNavigation.getChild(UISelectAccount.class) ;
      String accId = uiSelect.getSelectedValue() ;
      List<Tag> listTags = mailService.getTags(username, accId);
      uiTagForm.createCheckBoxTagList(listTags) ;
      for (String msgId : uiMessageList.getCheckedMessage()) {
        uiTagForm.messageMap.put(msgId, msgId); 
      }     
      uiPopupAction.activate(uiTagForm, 600, 0, true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
    }
  }
  
  static public class MoveMessagesActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiActionBar = event.getSource() ;      
    }
  }
  
  static public class ImportActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiActionBar = event.getSource() ;      
    }
  }
  
  static public class ExportActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiActionBar = event.getSource() ;      
    }
  }
}