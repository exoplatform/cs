/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui ;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.MessagePageList;
import org.exoplatform.mail.service.Tag;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.popup.UIComposeForm;
import org.exoplatform.mail.webui.popup.UIMoveMessageForm;
import org.exoplatform.mail.webui.popup.UIPopupAction;
import org.exoplatform.mail.webui.popup.UIPopupActionContainer;
import org.exoplatform.mail.webui.popup.UITagForm;
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

/**
 * Created by The eXo Platform SARL
 * Author : Nam Phung
 *          phunghainam@gmail.com
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
        @EventConfig(listeners = UIMessageList.DeleteActionListener.class),
        @EventConfig(listeners = UIMessageList.MarkAsReadActionListener.class),
        @EventConfig(listeners = UIMessageList.MarkAsUnReadActionListener.class),
        @EventConfig(listeners = UIMessageList.AddStarActionListener.class),
        @EventConfig(listeners = UIMessageList.RemoveStarActionListener.class),
        @EventConfig(listeners = UIMessageList.ViewAllActionListener.class),
        @EventConfig(listeners = UIMessageList.ViewStarredActionListener.class),
        @EventConfig(listeners = UIMessageList.ViewUnstarredActionListener.class),
        @EventConfig(listeners = UIMessageList.ViewUnreadActionListener.class),
        @EventConfig(listeners = UIMessageList.ViewReadActionListener.class),
        @EventConfig(listeners = UIMessageList.ViewAttachmentActionListener.class),
        @EventConfig(listeners = UIMessageList.FirstPageActionListener.class),
        @EventConfig(listeners = UIMessageList.PreviousPageActionListener.class),
        @EventConfig(listeners = UIMessageList.NextPageActionListener.class),
        @EventConfig(listeners = UIMessageList.LastPageActionListener.class),
        @EventConfig(listeners = UIMessageList.AddTagActionListener.class),
        @EventConfig(listeners = UIMessageList.MoveMessagesActionListener.class),
        @EventConfig(listeners = UIMessageList.ImportActionListener.class),
        @EventConfig(listeners = UIMessageList.ExportActionListener.class),
        @EventConfig(listeners = UIMessageList.SortActionListener.class)
    }
)

public class UIMessageList extends UIForm {
  private String selectedMessageId_ = null ;
  private String selectedFolderId_ = null ;
  private String selectedTagId_ = null ;
  private String viewQuery_ = null;
  private String sortedBy_ = null;
  private boolean isAscending_ = true;
  private MessagePageList pageList_ = null ;
  private LinkedHashMap<String, Message> messageList_ = new LinkedHashMap<String, Message>();
  
  final static public String INFO = "INFO" ;
  public UIMessageList() throws Exception {
    sortedBy_ = Utils.EXO_RECEIVEDDATE ;
  }
  
  public String getSelectedMessageId() throws Exception {
    if (getCheckedMessage() != null && getCheckedMessage().size() > 0) {
      return getCheckedMessage().get(0).getId();
    }
    return selectedMessageId_ ;
  }
  
  public void setSelectedMessageId(String messageId) {selectedMessageId_ = messageId ;}
  
  public String getSelectedFolderId() {return selectedFolderId_ ;}
  public void setSelectedFolderId(String folderId) {selectedFolderId_ = folderId ;}
  
  public String getSelectedTagId() {return selectedTagId_ ;}
  public void setSelectedTagId(String tagId) {selectedTagId_ = tagId ;}
  
  public String getViewQuery() {return viewQuery_ ;}
  public void setViewQuery(String view) {viewQuery_ = view ;}
  
  
  public String getSortedBy() { return sortedBy_; }
  public void setSortedBy(String sortedBy) { sortedBy_ = sortedBy; }
  
  public boolean isAscending() { return isAscending_; }
  public void setAscending(boolean b) { isAscending_ = b; }
  
  public MessagePageList getMessagePageList() { return pageList_; } 
  
  public List<Message> getMessageList() throws Exception { 
    return new ArrayList<Message>(messageList_.values());
  }
  
  public void setMessagePageList(MessagePageList pageList) throws Exception {
    pageList_ = pageList ;
    updateList();
  }
  
  public void updateList() throws Exception {
    updateList(pageList_.getCurrentPage());
  }
  
  public void updateList(long page) throws Exception {
    getChildren().clear();
    messageList_.clear();    
    if(pageList_ != null) {
      for (Message message : pageList_.getPage(page, MailUtils.getCurrentUser())) {
        UIFormCheckBoxInput<Boolean> uiCheckBox = new UIFormCheckBoxInput<Boolean>(message.getId(), message.getId(), false);
        addUIFormInput(uiCheckBox);
        messageList_.put(message.getId(), message);
      }
    }
  }
  
  public List<Message> getCheckedMessage() throws Exception {
    List<Message> messageList = new ArrayList<Message>();
    for (Message msg : getMessageList()) {
      UIFormCheckBoxInput<Boolean> uiCheckbox = getChildById(msg.getId());
      if (uiCheckbox != null && uiCheckbox.isChecked()) {
        messageList.add(msg);
      }
    }
    return messageList;
  }
  
  public Tag getFirstTag(Message msg) throws Exception {
    UIMailPortlet uiPortlet = getAncestorOfType(UIMailPortlet.class);
    String username = uiPortlet.getCurrentUser();
    String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
    MailService mailServ = uiPortlet.getApplicationComponent(MailService.class);
    String[] tagIds = msg.getTags();
    String tagId = "";
    if (tagIds != null && tagIds.length > 0) tagId = tagIds[0];
    List<Tag> tagList = mailServ.getTags(username, accountId);
    for (Tag tag : tagList) {
      if (tag.getId().equals(tagId)) return tag;
    }
    return new Tag();
  } 
  
  public String getAllTagName(Message msg) throws Exception {
    UIMailPortlet uiPortlet = getAncestorOfType(UIMailPortlet.class);
    String username = uiPortlet.getCurrentUser() ;
    String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
    MailService mailSrv = getApplicationComponent(MailService.class);

    String tags = "";
    if (msg.getTags() != null && msg.getTags().length > 0) {
      for (int i = 0; i < msg.getTags().length; i++) {
        if (i > 0) tags += ", ";
        Tag tag = mailSrv.getTag(username, accountId, msg.getTags()[i]);
        tags += "[" + tag.getName() + "]";
      }
    }
    return tags;
  }
  
  static public class SelectMessageActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIMessageList uiMessageList = event.getSource();
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class);
      UIMessagePreview uiMessagePreview = uiPortlet.findFirstComponentOfType(UIMessagePreview.class);
      UIMessageArea uiMessageArea = uiPortlet.findFirstComponentOfType(UIMessageArea.class);
      UIFolderContainer uiFolderContainer = uiPortlet.findFirstComponentOfType(UIFolderContainer.class);
      String username = uiPortlet.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      MailService mailServ = uiPortlet.getApplicationComponent(MailService.class);
      Message msg = mailServ.getMessageById(username, msgId, accountId);
      if (msg.isUnread()) {
        msg.setUnread(false);
        mailServ.saveMessage(username, accountId, msg, false);
      }
      uiMessageList.setSelectedMessageId(msgId);
      uiMessagePreview.setMessage(msg);
      uiMessageList.updateList();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageArea);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiFolderContainer);
    }
  }
  
  static public class AddStarActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;  
      UIMessageList uiMessageList = event.getSource();
      UIMessageArea uiMessageArea = uiMessageList.getParent();
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class);
      String username = uiPortlet.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      MailService mailServ = uiPortlet.getApplicationComponent(MailService.class);
      try {
        Message msg = mailServ.getMessageById(username, msgId, accountId);
        msg.setHasStar(!msg.hasStar());
        mailServ.saveMessage(username, accountId, msg, false);
        uiMessageList.setSelectedMessageId(msgId);
      } catch (Exception e) {
        for (Message msg : uiMessageList.getCheckedMessage()) {
          if (!msg.hasStar()) {
            msg.setHasStar(true);
            mailServ.saveMessage(username, accountId, msg, false);
          }
        }
      }
      uiMessageList.updateList();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageArea);
    }
  }
  static public class RemoveStarActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource();
      UIMessageArea uiMessageArea = uiMessageList.getParent();
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class);
      String username = uiPortlet.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      MailService mailServ = uiPortlet.getApplicationComponent(MailService.class);
      for (Message msg : uiMessageList.getCheckedMessage()) {
        if (msg.hasStar()) {
          msg.setHasStar(false);
          mailServ.saveMessage(username, accountId, msg, false);
        }
      }
      uiMessageList.updateList();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageArea);
    }
  }
  
  static public class ViewAllActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource();
      System.out.println("======>>>>> ViewAllActionListener");
      uiMessageList.filterMessage("");
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class));
    }
  }
  
  static public class ViewStarredActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource();
      System.out.println("======>>>>> ViewStaredActionListener");      
      uiMessageList.filterMessage("@" + Utils.EXO_STAR + "='true'");
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class));
    }
  }
  
  static public class ViewUnstarredActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource();
      System.out.println("======>>>>> ViewUnstaredActionListener");
      uiMessageList.filterMessage("@" + Utils.EXO_STAR + "='false'");
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class));
    }
  }
  
  static public class ViewUnreadActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource();
      System.out.println("======>>>>> ViewUnreadActionListener");
      uiMessageList.filterMessage("@" + Utils.EXO_ISUNREAD + "='true'");
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class));
    }
  }
  
  static public class ViewReadActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource();
      System.out.println("======>>>>> ViewReadActionListener");
      uiMessageList.filterMessage("@" + Utils.EXO_ISUNREAD + "='false'");
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class));
    }
  }
    
  static public class ViewAttachmentActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource();
      System.out.println("======>>>>> ViewAttachmentActionListener");
      uiMessageList.filterMessage("@" + Utils.EXO_HASATTACH + "='true'");
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class));
    }
  }
  
  public void filterMessage(String viewQuery) throws Exception {
    UIMailPortlet uiPortlet = getAncestorOfType(UIMailPortlet.class);
    MailService mailSrv = getApplicationComponent(MailService.class);
    setViewQuery(viewQuery);
    String username = uiPortlet.getCurrentUser();
    String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
    if (getSelectedFolderId() != null) {
      setMessagePageList(mailSrv.getMessageByFolder(username, accountId, getSelectedFolderId(), getViewQuery()));
    } else if (getSelectedTagId() != null) {
      setMessagePageList(mailSrv.getMessagePagelistByTag(username, accountId, getSelectedTagId(), getViewQuery()));
    }
    updateList();
  }

  static public class ReplyActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource() ; 
      System.out.println(" =========== > Reply Action");
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class) ;
      UINavigationContainer uiNavigation = uiPortlet.getChild(UINavigationContainer.class) ;
      UISelectAccount uiSelect = uiNavigation.getChild(UISelectAccount.class) ;
      String accId = uiSelect.getSelectedValue() ;
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
      UIPopupActionContainer uiPopupContainer = uiPopupAction.activate(UIPopupActionContainer.class, 1000) ;
      
      UIComposeForm uiComposeForm = uiPopupContainer.createUIComponent(UIComposeForm.class, null, null);
      List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
      MailService mailSvr = uiMessageList.getApplicationComponent(MailService.class) ;
      String username = uiPortlet.getCurrentUser() ;
      if (uiMessageList.getSelectedMessageId() != null) {
        String messageId = uiMessageList.getSelectedMessageId();
        Message message = mailSvr.getMessageById(username, messageId, accId);
        uiComposeForm.setFieldSubjectValue("Re: " + message.getSubject());
        uiComposeForm.setFieldToValue(message.getFrom());
        uiComposeForm.setFieldMessageContentValue(message.getMessageBody());
      }
      for(Account acc : mailSvr.getAccounts(username)) {
        SelectItemOption<String> itemOption = new SelectItemOption<String>(acc.getUserDisplayName() + " &lt;" + acc.getEmailAddress() + 
            "&gt;", acc.getUserDisplayName() + "<" + acc.getEmailAddress() + ">");
        if (acc.getId() == accId) itemOption.setSelected(true);
        options.add(itemOption) ;
      }
      uiComposeForm.setFieldFromValue(options) ;
      uiPopupContainer.addChild(uiComposeForm) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }
  
  static  public class ReplyAllActionListener extends EventListener<UIMessageList> {    
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource() ; 
      System.out.println(" =========== > Reply All Action");
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class) ;
      UINavigationContainer uiNavigation = uiPortlet.getChild(UINavigationContainer.class) ;
      UISelectAccount uiSelect = uiNavigation.getChild(UISelectAccount.class) ;
      String accId = uiSelect.getSelectedValue() ;
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
      UIPopupActionContainer uiPopupContainer = uiPopupAction.activate(UIPopupActionContainer.class, 1000) ;
      
      UIComposeForm uiComposeForm = uiPopupContainer.createUIComponent(UIComposeForm.class, null, null);
      List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
      MailService mailSvr = uiMessageList.getApplicationComponent(MailService.class) ;
      String username = uiPortlet.getCurrentUser() ;
      if (uiMessageList.getSelectedMessageId() != null) {
        String messageId = uiMessageList.getSelectedMessageId();
        Message message = mailSvr.getMessageById(username, messageId, accId);
        uiComposeForm.setFieldSubjectValue("Re: " + message.getSubject());
        uiComposeForm.setFieldToValue(message.getFrom() + "," + 
            message.getMessageCc() + "," + message.getMessageBcc());
        uiComposeForm.setFieldMessageContentValue(message.getMessageBody());
      }
      for(Account acc : mailSvr.getAccounts(username)) {
        SelectItemOption<String> itemOption = new SelectItemOption<String>(acc.getUserDisplayName() + " &lt;" + acc.getEmailAddress() + 
            "&gt;", acc.getUserDisplayName() + "<" + acc.getEmailAddress() + ">");
        if (acc.getId() == accId) itemOption.setSelected(true);
        options.add(itemOption) ;
      }
      uiComposeForm.setFieldFromValue(options) ;
      uiPopupContainer.addChild(uiComposeForm) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }
     
  static public class ForwardActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource() ; 
      System.out.println(" =========== > Forward Action");
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class) ;
      UINavigationContainer uiNavigation = uiPortlet.getChild(UINavigationContainer.class) ;
      UISelectAccount uiSelect = uiNavigation.getChild(UISelectAccount.class) ;
      String accId = uiSelect.getSelectedValue() ;
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
      UIPopupActionContainer uiPopupContainer = uiPopupAction.activate(UIPopupActionContainer.class, 1000) ;
      
      UIComposeForm uiComposeForm = uiPopupContainer.createUIComponent(UIComposeForm.class, null, null);
      List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
      MailService mailSvr = uiMessageList.getApplicationComponent(MailService.class) ;
      String username = uiPortlet.getCurrentUser() ;
      if (uiMessageList.getSelectedMessageId() != null) {
        String messageId = uiMessageList.getSelectedMessageId();
        Message message = mailSvr.getMessageById(username, messageId, accId);
        uiComposeForm.setFieldSubjectValue("Fwd: " + message.getSubject());
        String forwardedText = "\n\n\n-------- Original Message --------\n" +
            "Subject: " + message.getSubject() + "\nDate: " + message.getSendDate() + 
            "\nFrom: " + message.getFrom() + 
            "\nTo: " + message.getMessageTo() + 
            "\n\n" + message.getMessageBody();         
        message.setMessageBody(forwardedText);
        uiComposeForm.setFieldMessageContentValue(message.getMessageBody());
      }
      for(Account acc : mailSvr.getAccounts(username)) {
        SelectItemOption<String> itemOption = new SelectItemOption<String>(acc.getUserDisplayName() + " &lt;" + acc.getEmailAddress() + 
            "&gt;", acc.getUserDisplayName() + "<" + acc.getEmailAddress() + ">");
        if (acc.getId() == accId) itemOption.setSelected(true);
        options.add(itemOption) ;
      }
      uiComposeForm.setFieldFromValue(options) ;
      uiPopupContainer.addChild(uiComposeForm) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }  
  
  static public class DeleteActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource();
      System.out.println("======== >>> DeleteActionListener");
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class);
      UIFolderContainer uiFolderContainer = uiPortlet.findFirstComponentOfType(UIFolderContainer.class);
      UIMessageArea uiMessageArea = uiPortlet.findFirstComponentOfType(UIMessageArea.class);
      UITagContainer uiTags = uiPortlet.findFirstComponentOfType(UITagContainer.class); 
      MailService mailSrv = uiMessageList.getApplicationComponent(MailService.class);
      String username = uiPortlet.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      List<Message> checkedMessageList = uiMessageList.getCheckedMessage();
      List<String> messageIdList = new ArrayList<String>();
      for (Message message : checkedMessageList) {
        messageIdList.add(message.getId());
      }
      try {
        mailSrv.removeMessage(username, accountId, messageIdList);
        uiMessageList.updateList(uiMessageList.getMessagePageList().getCurrentPage());
      } catch (Exception e) {
        System.err.println("Error : Exception occur while delete message");
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiFolderContainer);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageArea);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTags);
    }
  }
  
  static public class MarkAsReadActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource() ;  
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class) ;
      List<Message> checkedMessage = uiMessageList.getCheckedMessage();
      MailService mailSrv = uiMessageList.getApplicationComponent(MailService.class);
      String username = uiPortlet.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      for (Message msg : checkedMessage) {
        if (msg.isUnread()) {
          msg.setUnread(false);
          mailSrv.saveMessage(username, accountId, msg, false);
        }
      }
      uiMessageList.updateList();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList);
    }
  }
  
  static public class MarkAsUnReadActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource() ;  
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class) ;
      List<Message> checkedMessage = uiMessageList.getCheckedMessage();
      MailService mailSrv = uiMessageList.getApplicationComponent(MailService.class);
      String username = uiPortlet.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      for (Message msg : checkedMessage) {
        if (!msg.isUnread()) {
          msg.setUnread(true);
          mailSrv.saveMessage(username, accountId, msg, false);
        }
      }
      uiMessageList.updateList();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList);
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
      uiPopupAction.activate(uiTagForm, 600, 0, true);
      uiTagForm.setMessageList(uiMessageList.getCheckedMessage());
      uiTagForm.setTagList(listTags) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
    }
  }
  
  static public class MoveMessagesActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      System.out.println("MoveMessagesActionListener");
      UIMessageList uiMessageList = event.getSource() ; 
      
      
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class);
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class);    
      String username = uiPortlet.getCurrentUser();
      MailService mailService = uiMessageList.getApplicationComponent(MailService.class);
      
      if(uiMessageList.getCheckedMessage().isEmpty())
      {
        UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.checkMessage-select-no-messages", null, ApplicationMessage.INFO)) ;
        return;
      }
            
      UINavigationContainer uiNavigation = uiPortlet.getChild(UINavigationContainer.class) ;
      UISelectAccount uiSelectAccount = uiNavigation.getChild(UISelectAccount.class) ;
      String accountId = uiSelectAccount.getSelectedValue() ;      
      UIMoveMessageForm uiMoveMessageForm = uiMessageList.createUIComponent(UIMoveMessageForm.class,null, null);
      List<Folder> folderList = new ArrayList<Folder>();
      
      folderList.addAll(mailService.getFolders(username, accountId, false)); 
      folderList.addAll(mailService.getFolders(username, accountId, true));  

      uiMoveMessageForm.setMessageList(uiMessageList.getCheckedMessage());  
      uiMoveMessageForm.setFolderList(folderList);
      
      uiPopupAction.activate(uiMoveMessageForm, 600, 0, true);             
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);        
    }
  }

  
  static public class ImportActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource() ;      
    }
  }
  
  static public class ExportActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource() ;      
    }
  }
  
  static public class FirstPageActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource() ; 
      uiMessageList.updateList(1);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class));
    }
  }
  
  static public class PreviousPageActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource() ; 
      MessagePageList pageList = uiMessageList.getMessagePageList(); 
      if (pageList.getCurrentPage() > 1){
        uiMessageList.updateList(pageList.getCurrentPage() - 1);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class));
    }
  }
  
  static public class NextPageActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource() ; 
      MessagePageList pageList = uiMessageList.getMessagePageList(); 
      if (pageList.getCurrentPage() < pageList.getAvailablePage()){
        uiMessageList.updateList(pageList.getCurrentPage() + 1);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class));
    }
  }
  
  static public class LastPageActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource() ; 
      MessagePageList pageList = uiMessageList.getMessagePageList(); 
      uiMessageList.updateList(pageList.getAvailablePage());
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class));
    }
  }
  
  static public class SortActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource() ;
      String sortedBy = event.getRequestContext().getRequestParameter(OBJECTID) ;  
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class);
      MailService mailSrv = uiMessageList.getApplicationComponent(MailService.class);
      String username = uiPortlet.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      uiMessageList.setAscending(!uiMessageList.isAscending_);
      uiMessageList.setSortedBy(sortedBy);
      if (uiMessageList.getSelectedFolderId() != null) {
        uiMessageList.setMessagePageList(mailSrv.getMessageByFolder(username, accountId, uiMessageList.getSelectedFolderId(), uiMessageList.getViewQuery(), uiMessageList.getSortedBy(), uiMessageList.isAscending_));
      } else if (uiMessageList.getSelectedTagId() != null) {
        uiMessageList.setMessagePageList(mailSrv.getMessagePagelistByTag(username, accountId, uiMessageList.getSelectedTagId(), uiMessageList.getViewQuery(), uiMessageList.getSortedBy(), uiMessageList.isAscending_));
      }
      uiMessageList.updateList();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class));
    }
  }
}