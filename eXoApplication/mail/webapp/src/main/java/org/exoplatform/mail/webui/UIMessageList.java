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
package org.exoplatform.mail.webui ;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.mail.internet.InternetAddress;

import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.SessionsUtils;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.MessageFilter;
import org.exoplatform.mail.service.MessagePageList;
import org.exoplatform.mail.service.SpamFilter;
import org.exoplatform.mail.service.Tag;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.popup.UIAddContactForm;
import org.exoplatform.mail.webui.popup.UIComposeForm;
import org.exoplatform.mail.webui.popup.UIExportForm;
import org.exoplatform.mail.webui.popup.UIImportForm;
import org.exoplatform.mail.webui.popup.UIMoveMessageForm;
import org.exoplatform.mail.webui.popup.UIPopupAction;
import org.exoplatform.mail.webui.popup.UIPopupActionContainer;
import org.exoplatform.mail.webui.popup.UIPrintPreview;
import org.exoplatform.mail.webui.popup.UITagForm;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;

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
      @EventConfig(listeners = UIMessageList.ReadActionListener.class),
      @EventConfig(listeners = UIMessageList.EditDraftActionListener.class),
      @EventConfig(listeners = UIMessageList.AddStarActionListener.class),
      @EventConfig(listeners = UIMessageList.RemoveStarActionListener.class),
      @EventConfig(listeners = UIMessageList.ReplyActionListener.class),
      @EventConfig(listeners = UIMessageList.ReplyAllActionListener.class),
      @EventConfig(listeners = UIMessageList.ForwardActionListener.class), 
      @EventConfig(listeners = UIMessageList.DeleteActionListener.class),
      @EventConfig(listeners = UIMessageList.ReportSpamActionListener.class),
      @EventConfig(listeners = UIMessageList.NotSpamActionListener.class),
      @EventConfig(listeners = UIMessageList.PrintActionListener.class),
      @EventConfig(listeners = UIMessageList.MarkAsReadActionListener.class),
      @EventConfig(listeners = UIMessageList.MarkAsUnReadActionListener.class),
      @EventConfig(listeners = UIMessageList.RemoveStarActionListener.class),
      @EventConfig(listeners = UIMessageList.ViewAllActionListener.class),
      @EventConfig(listeners = UIMessageList.ViewAsListActionListener.class),
      @EventConfig(listeners = UIMessageList.ViewAsThreadActionListener.class),
      @EventConfig(listeners = UIMessageList.ViewAsConversationActionListener.class),
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
      @EventConfig(listeners = UIMessageList.AddTagDnDActionListener.class),
      @EventConfig(listeners = UIMessageList.MoveMessagesActionListener.class),
      @EventConfig(listeners = UIMessageList.MoveDirectMessagesActionListener.class),
      @EventConfig(listeners = UIMessageList.AddContactActionListener.class),
      @EventConfig(listeners = UIMessageList.ImportActionListener.class),
      @EventConfig(listeners = UIMessageList.ExportActionListener.class),
      @EventConfig(listeners = UIMessageList.SortActionListener.class),
      @EventConfig(listeners = UIMessageList.RefreshActionListener.class)
    }
)

public class UIMessageList extends UIForm {
  public final int MODE_LIST = 1 ;
  public final int MODE_THREAD = 2 ;
  public final int MODE_CONVERSATION = 3 ;
  
  private String selectedMessageId_ = null ;
  private String selectedFolderId_ = null ;
  private String selectedTagId_ = null ;
  private String viewQuery_ = null;
  private String sortedBy_ = null;
  private boolean isAscending_ = true;
  private MessagePageList pageList_ = null ;
  private MessageFilter msgFilter_;
  private String accountId_ ;
  public int viewMode = MODE_THREAD ;
  public LinkedHashMap<String, Message> messageList_ = new LinkedHashMap<String, Message>();

  public UIMessageList() throws Exception {}

  public void init(String accountId) throws Exception {
    accountId_ = accountId ;
    sortedBy_ = Utils.EXO_RECEIVEDDATE ;
    String username = MailUtils.getCurrentUser();
    MailService mailSrv = MailUtils.getMailService();
    MessageFilter filter = getMessageFilter();
    if (filter == null) filter = new MessageFilter("Folder");
    if (accountId != null && accountId != ""){
      filter.setAccountId(accountId);
      //if(filter.getFolder() == null || (filter.getFolder() != null && (!filter.getFolder()[0].equals(selectedFolderId_)) ||  pageList_ == null)) {
      if (filter.getFolder() == null) {        
        selectedFolderId_ = Utils.createFolderId(accountId, Utils.FD_INBOX, false);
        filter.setFolder(new String[] { selectedFolderId_ });
      } else selectedFolderId_ = filter.getFolder()[0];
      setMessagePageList(mailSrv.getMessagePageListByFolder(SessionsUtils.getSessionProvider(), username, accountId, selectedFolderId_));
      //}
    } else messageList_.clear();
    setMessageFilter(filter);
  }

  public String getAccountId() { return accountId_ ; }

  public String getSelectedMessageId() throws Exception {
    return selectedMessageId_ ;
  }

  public void setSelectedMessageId(String messageId) {selectedMessageId_ = messageId ;}

  public String getSelectedFolderId() {return selectedFolderId_ ;}
  public void setSelectedFolderId(String folderId) { selectedFolderId_ = folderId ; }

  public String getSelectedTagId() {return selectedTagId_ ;}
  public void setSelectedTagId(String tagId) {selectedTagId_ = tagId ;}

  public boolean selectedSpamFolder() throws Exception {
    return (getSelectedFolderId() != null) ? getSelectedFolderId().equals(Utils.createFolderId(accountId_, Utils.FD_SPAM, false)) : false ;
  }

  public boolean selectedDraftFolder() throws Exception {
    return (getSelectedFolderId() != null) ? getSelectedFolderId().equals(Utils.createFolderId(accountId_, Utils.FD_DRAFTS, false)) : false ;
  }

  public boolean selectedSentFolder() throws Exception {
    return (getSelectedFolderId() != null) ? getSelectedFolderId().equals(Utils.createFolderId(accountId_, Utils.FD_SENT, false)) : false ;
  }

  public String getViewQuery() {return viewQuery_ ;}
  public void setViewQuery(String view) {viewQuery_ = view ;}

  public MessageFilter getMessageFilter() { return msgFilter_; }
  public void setMessageFilter(MessageFilter msgFilter) { msgFilter_ = msgFilter; }

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
    long page = pageList_.getCurrentPage();
    if (pageList_ == null) page = 1 ;
    updateList(page);
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

  public List<Tag> getTags(Message msg) throws Exception {
    UIMailPortlet uiPortlet = getAncestorOfType(UIMailPortlet.class);
    String username = uiPortlet.getCurrentUser() ;
    MailService mailSrv = getApplicationComponent(MailService.class);
    List<Tag> tagList = new ArrayList<Tag>();
    try {
      if (msg.getTags() != null && msg.getTags().length > 0) {
        for (int i = 0; i < msg.getTags().length; i++) {
          Tag tag = mailSrv.getTag(SessionsUtils.getSessionProvider(), username, accountId_, msg.getTags()[i]);
          tagList.add(tag);
        }
      }
    } catch(Exception e) {
      e.printStackTrace() ;
    }
    return tagList;
  } 

  static public class SelectMessageActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIMessageList uiMessageList = event.getSource();
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class);
      UIMessagePreview uiMessagePreview = uiPortlet.findFirstComponentOfType(UIMessagePreview.class);
      UIFolderContainer uiFolderContainer = uiPortlet.findFirstComponentOfType(UIFolderContainer.class);
      String username = uiPortlet.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();

      Message msg = uiMessageList.messageList_.get(msgId);
      if (msg != null) {
        if (msg.isUnread()) {
          List<Message> msgIds  = new ArrayList<Message>();
          msgIds.add(msg);
          MailUtils.getMailService().toggleMessageProperty(SessionsUtils.getSessionProvider(), username, accountId, msgIds, Utils.EXO_ISUNREAD);
          msg.setUnread(false);
          uiMessageList.setSelectedMessageId(msgId);
          uiMessageList.messageList_.put(msg.getId(), msg);
          event.getRequestContext().addUIComponentToUpdateByAjax(uiFolderContainer); 
        }
        for (Message uncheckedMsg : uiMessageList.messageList_.values()) {
          UIFormCheckBoxInput<Boolean> uiCheckbox = uiMessageList.getChildById(uncheckedMsg.getId());
          if (uiCheckbox != null ) {
            if (uncheckedMsg.getId().equals(msg.getId())) uiCheckbox.setChecked(true);
            else uiCheckbox.setChecked(false); 
          }
        }
        uiMessagePreview.setMessage(msg);
        List<Message> showedMessages = new ArrayList<Message>() ;
        showedMessages.add(msg) ;
        if (uiMessageList.viewMode == uiMessageList.MODE_CONVERSATION) {
          if (msg != null && msg.getGroupedMessageIds().size() > 0) {
            for (String id : msg.getGroupedMessageIds()) {
              showedMessages.add(uiMessageList.messageList_.get(id)) ;
            }
          }
        }
        uiMessagePreview.setShowedMessages(showedMessages) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.findFirstComponentOfType(UIMessageArea.class));
      }          
    }
  }

  static public class ReadActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {

    }
  }

  static public class EditDraftActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIMessageList uiMessageList = event.getSource();
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class);
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      Message msg = uiMessageList.messageList_.get(msgId);
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
      UIPopupActionContainer uiPopupContainer = uiPopupAction.activate(UIPopupActionContainer.class, 850) ;
      UIComposeForm uiComposeForm = uiPopupContainer.createUIComponent(UIComposeForm.class, null, null);
      uiComposeForm.init(accountId, msg, uiComposeForm.MESSAGE_IN_DRAFT);
      uiPopupContainer.addChild(uiComposeForm) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;  
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getParent());
    }
  }

  static public class AddStarActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;  
      UIMessageList uiMessageList = event.getSource();
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class);
      String username = uiPortlet.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      if(Utils.isEmptyField(accountId)) {
        UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.account-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      MailService mailSrv = uiPortlet.getApplicationComponent(MailService.class);
      Message msg = uiMessageList.messageList_.get(msgId);
      if ( msg != null ) {
        List<Message> msgList = new ArrayList<Message>() ;
        msgList.add(msg);
        msg.setHasStar(!msg.hasStar());
        uiMessageList.messageList_.put(msg.getId(), msg);
        mailSrv.toggleMessageProperty(SessionsUtils.getSessionProvider(), username, accountId, msgList, Utils.EXO_STAR);
        uiMessageList.setSelectedMessageId(msgId);
      } else {
        List<Message> msgList = new ArrayList<Message>() ;
        for (Message checkedMessage : uiMessageList.getCheckedMessage()) {
          if (!checkedMessage.hasStar()) {
            msgList.add(checkedMessage);
            checkedMessage.setHasStar(true);
            uiMessageList.messageList_.put(checkedMessage.getId(), checkedMessage);
          }
        }
        mailSrv.toggleMessageProperty(SessionsUtils.getSessionProvider(), username, accountId, msgList, Utils.EXO_STAR);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getParent());
    }
  }

  static public class RemoveStarActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource();
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class);
      String username = uiPortlet.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      if(Utils.isEmptyField(accountId)) {
        UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.account-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      MailService mailSrv = uiPortlet.getApplicationComponent(MailService.class);
      List<Message> msgList = new ArrayList<Message>() ;
      for (Message msg : uiMessageList.getCheckedMessage()) {
        if (msg.hasStar()) {
          msgList.add(msg);
          msg.setHasStar(false);
          uiMessageList.messageList_.put(msg.getId(), msg);
        }
      }
      mailSrv.toggleMessageProperty(SessionsUtils.getSessionProvider(), username, accountId, msgList, Utils.EXO_STAR);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList);
    }
  }

  static public class ViewAllActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource();
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class);
      String accId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      if(Utils.isEmptyField(accId)) {
        UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.account-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      uiMessageList.filterMessage("");
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class));
    }
  }
  
  static public class ViewAsListActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource();
      if (uiMessageList.viewMode == uiMessageList.MODE_CONVERSATION) {
        UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class);
        UIMessagePreview uiMsgPreview = uiPortlet.findFirstComponentOfType(UIMessagePreview.class) ;
        List<Message> showedMsgs = new ArrayList<Message>();
        showedMsgs.add(uiMsgPreview.getMessage()) ;
        uiMsgPreview.setShowedMessages(showedMsgs);
      }
      uiMessageList.viewMode = uiMessageList.MODE_LIST ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class));
    }
  }
  
  static public class ViewAsThreadActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource();
      if (uiMessageList.viewMode == uiMessageList.MODE_CONVERSATION) {
        UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class);
        UIMessagePreview uiMsgPreview = uiPortlet.findFirstComponentOfType(UIMessagePreview.class) ;
        List<Message> showedMsgs = new ArrayList<Message>();
        showedMsgs.add(uiMsgPreview.getMessage()) ;
        uiMsgPreview.setShowedMessages(showedMsgs);
      }
      uiMessageList.viewMode = uiMessageList.MODE_THREAD ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class));
    }
  }
  
  static public class ViewAsConversationActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource();
      if (uiMessageList.viewMode != uiMessageList.MODE_CONVERSATION) {
        UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class);
        UIMessagePreview uiMsgPreview = uiPortlet.findFirstComponentOfType(UIMessagePreview.class) ;
        List<Message> showedMsgs = new ArrayList<Message>();
        Message msg = uiMsgPreview.getMessage();
        showedMsgs.add(msg) ;
        if (msg != null && msg.getGroupedMessageIds().size() > 0) {
          for (String id : msg.getGroupedMessageIds()) {
            showedMsgs.add(uiMessageList.messageList_.get(id)) ;
          }
        }
        uiMsgPreview.setShowedMessages(showedMsgs);
      }
      uiMessageList.viewMode = uiMessageList.MODE_CONVERSATION ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class));
    }
  }

  static public class ViewStarredActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource();     
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class);
      String accId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      if(Utils.isEmptyField(accId)) {
        UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.account-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      uiMessageList.filterMessage("@" + Utils.EXO_STAR + "='true'");
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class));
    }
  }

  static public class ViewUnstarredActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource();
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class);
      String accId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      if(Utils.isEmptyField(accId)) {
        UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.account-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      uiMessageList.filterMessage("@" + Utils.EXO_STAR + "='false'");
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class));
    }
  }

  static public class ViewUnreadActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource();
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class);
      String accId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      if(Utils.isEmptyField(accId)) {
        UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.account-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      uiMessageList.filterMessage("@" + Utils.EXO_ISUNREAD + "='true'");
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class));
    }
  }

  static public class ViewReadActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource();
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class);
      String accId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      if(Utils.isEmptyField(accId)) {
        UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.account-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      uiMessageList.filterMessage("@" + Utils.EXO_ISUNREAD + "='false'");
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class));
    }
  }

  static public class ViewAttachmentActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource();
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class);
      String accId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      if(Utils.isEmptyField(accId)) {
        UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.account-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
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

    MessageFilter msgFilter = getMessageFilter();
    msgFilter.setAccountId(accountId);
    msgFilter.setOrderBy(getSortedBy());
    msgFilter.setAscending(isAscending_);
    msgFilter.setViewQuery(getViewQuery());
    if (!msgFilter.getName().equals("Search")) {
      msgFilter.setText("");
      msgFilter.setFolder((getSelectedFolderId() == null) ? null : new String[] {getSelectedFolderId()});
      msgFilter.setTag((getSelectedTagId() == null) ? null : new String[] {getSelectedTagId()});
    }
    setMessagePageList(mailSrv.getMessagePageList(SessionsUtils.getSessionProvider(), username, msgFilter));
  }

  static public class ReplyActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource() ; 
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class) ;
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String accId = uiPortlet.getChild(UINavigationContainer.class).getChild(UISelectAccount.class).getSelectedValue() ;

      // Verify
      UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
      if(uiMessageList.getCheckedMessage().isEmpty()) {
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.checkMessage-select-no-messages", null, ApplicationMessage.INFO)) ;
        return;
      } else if (uiMessageList.getCheckedMessage().size() > 1){
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.checkMessage-select-many-messages", null, ApplicationMessage.INFO)) ;
        return;
      }      
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
      UIPopupActionContainer uiPopupContainer = uiPopupAction.activate(UIPopupActionContainer.class, 850) ;

      UIComposeForm uiComposeForm = uiPopupContainer.createUIComponent(UIComposeForm.class, null, null);

      Message message ;
      if (msgId != null) message = uiMessageList.messageList_.get(msgId) ;
      else  message = uiMessageList.getCheckedMessage().get(0);
      uiComposeForm.init(accId, message, uiComposeForm.MESSAGE_REPLY);
      uiPopupContainer.addChild(uiComposeForm) ;

      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.findFirstComponentOfType(UIMessageArea.class));
    }
  }

  static  public class ReplyAllActionListener extends EventListener<UIMessageList> {    
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource() ; 
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class) ;
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String accId = uiPortlet.getChild(UINavigationContainer.class).getChild(UISelectAccount.class).getSelectedValue() ;

      // Verify
      UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
      if(uiMessageList.getCheckedMessage().isEmpty()) {
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.checkMessage-select-no-messages", null, ApplicationMessage.INFO)) ;
        return;
      } else if (uiMessageList.getCheckedMessage().size() > 1){
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.checkMessage-select-many-messages", null, ApplicationMessage.INFO)) ;
        return;
      }      
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
      UIPopupActionContainer uiPopupContainer = uiPopupAction.activate(UIPopupActionContainer.class, 850) ;

      UIComposeForm uiComposeForm = uiPopupContainer.createUIComponent(UIComposeForm.class, null, null);

      Message message ;
      if (msgId != null) message = uiMessageList.messageList_.get(msgId) ; 
      else  message = uiMessageList.getCheckedMessage().get(0);
      uiComposeForm.init(accId, message, uiComposeForm.MESSAGE_REPLY_ALL);
      uiPopupContainer.addChild(uiComposeForm) ;

      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.findFirstComponentOfType(UIMessageArea.class));
    }
  }

  static public class ForwardActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource() ; 
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class) ;
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String accId = uiPortlet.getChild(UINavigationContainer.class).getChild(UISelectAccount.class).getSelectedValue() ;

      // Verify
      UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
      if(uiMessageList.getCheckedMessage().isEmpty()) {
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.checkMessage-select-no-messages", null, ApplicationMessage.INFO)) ;
        return;
      } else if (uiMessageList.getCheckedMessage().size() > 1){
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.checkMessage-select-many-messages", null, ApplicationMessage.INFO)) ;
        return;
      }      
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
      UIPopupActionContainer uiPopupContainer = uiPopupAction.activate(UIPopupActionContainer.class, 850) ;

      UIComposeForm uiComposeForm = uiPopupContainer.createUIComponent(UIComposeForm.class, null, null);

      Message message ;
      if (msgId != null) message = uiMessageList.messageList_.get(msgId) ;
      else  message = uiMessageList.getCheckedMessage().get(0);
      uiComposeForm.init(accId, message, uiComposeForm.MESSAGE_FOWARD);
      uiPopupContainer.addChild(uiComposeForm) ;

      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.findFirstComponentOfType(UIMessageArea.class));
    }
  }  

  static public class DeleteActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource();
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class);
      String username = uiPortlet.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      if(Utils.isEmptyField(accountId)) {
        UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.account-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      UIFolderContainer uiFolderContainer = uiPortlet.findFirstComponentOfType(UIFolderContainer.class);
      UIMessageArea uiMessageArea = uiPortlet.findFirstComponentOfType(UIMessageArea.class);
      UIMessagePreview uiMessagePreview = uiPortlet.findFirstComponentOfType(UIMessagePreview.class);
      Message msgPreview = null;
      if (uiMessagePreview.getMessage() != null) msgPreview = uiMessagePreview.getMessage();
      UITagContainer uiTags = uiPortlet.findFirstComponentOfType(UITagContainer.class); 
      MailService mailSrv = uiMessageList.getApplicationComponent(MailService.class);
      List<Message> appliedMsgList = new ArrayList<Message>();
      if (msgId != null) { 
        appliedMsgList.add(uiMessageList.messageList_.get(msgId));
      } else {
        appliedMsgList = uiMessageList.getCheckedMessage();
      }
      String trashFolderId = Utils.createFolderId(accountId, Utils.FD_TRASH, false) ;
      String selectedFolderId = uiMessageList.getSelectedFolderId() ;
      if (selectedFolderId != null && selectedFolderId.equals(trashFolderId)) { 
        mailSrv.removeMessage(SessionsUtils.getSessionProvider(), username, accountId, appliedMsgList);
      } else {
        for (Message message : appliedMsgList)
          mailSrv.moveMessages(SessionsUtils.getSessionProvider(), username, accountId, message, message.getFolders()[0], trashFolderId);
      }

      if (msgPreview != null && appliedMsgList.contains(msgPreview)) uiMessagePreview.setMessage(null);
      uiMessageList.updateList();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiFolderContainer);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageArea);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTags);
    }
  }

  static public class ReportSpamActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource();
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class) ;
      String username = MailUtils.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      if(Utils.isEmptyField(accountId)) {
        UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.account-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      MailService mailSrv = MailUtils.getMailService();
      List<Message> checkedMessageList = new ArrayList<Message>();
      if (msgId != null) { 
        checkedMessageList.add(uiMessageList.messageList_.get(msgId));
      } else {
        checkedMessageList = uiMessageList.getCheckedMessage();
      }    

      SpamFilter spamFilter = mailSrv.getSpamFilter(SessionsUtils.getSessionProvider(), username, accountId);

      for(Message message: checkedMessageList) {
        mailSrv.moveMessages(SessionsUtils.getSessionProvider(), username, accountId, message, message.getFolders()[0], Utils.createFolderId(accountId, Utils.FD_SPAM, false));
        spamFilter.reportSpam(message);
      }       
      mailSrv.saveSpamFilter(SessionsUtils.getSessionProvider(), username, accountId, spamFilter);

      uiMessageList.updateList(); 
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.findFirstComponentOfType(UIFolderContainer.class)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class));  
    }
  }

  static public class NotSpamActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource();
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class) ;
      String username = MailUtils.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
      if(Utils.isEmptyField(accountId)) {
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.account-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      MailService mailSrv = MailUtils.getMailService();
      List<Message> checkedMessageList = new ArrayList<Message>();
      if (msgId != null) { 
        checkedMessageList.add(uiMessageList.messageList_.get(msgId));
      } else {
        checkedMessageList = uiMessageList.getCheckedMessage();
      }    

      SpamFilter spamFilter = mailSrv.getSpamFilter(SessionsUtils.getSessionProvider(), username, accountId);

      for(Message message: checkedMessageList) {
        mailSrv.moveMessages(SessionsUtils.getSessionProvider(), username, accountId, message, message.getFolders()[0], Utils.createFolderId(accountId, Utils.FD_INBOX, false));
        spamFilter.notSpam(message);
      }       
      mailSrv.saveSpamFilter(SessionsUtils.getSessionProvider(), username, accountId, spamFilter);

      uiMessageList.updateList(); 
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.findFirstComponentOfType(UIFolderContainer.class)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class));  
    }
  }

  static public class PrintActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource();
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
      if(uiMessageList.getCheckedMessage().isEmpty()) {
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.checkMessage-select-no-messages", null, ApplicationMessage.INFO)) ;
        return;
      } else if (uiMessageList.getCheckedMessage().size() > 1){
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.checkMessage-select-many-messages", null, ApplicationMessage.INFO)) ;
        return;
      }
      Message message = null ;
      if (msgId != null) message = uiMessageList.messageList_.get(msgId) ;
      else  message = uiMessageList.getCheckedMessage().get(0);
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class);
      UIPopupAction uiPopup = uiPortlet.getChild(UIPopupAction.class);
      UIPrintPreview uiPrintPreview = uiPopup.activate(UIPrintPreview.class, 700) ;
      uiPrintPreview.setPrintMessage(message) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.findFirstComponentOfType(UIMessageArea.class));
    }
  }

  static public class MarkAsReadActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource() ;  
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class) ;
      String username = uiPortlet.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      if(Utils.isEmptyField(accountId)) {
        UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.account-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      List<Message> checkedMsgs = uiMessageList.getCheckedMessage();
      MailService mailSrv = uiMessageList.getApplicationComponent(MailService.class);
      List<Message> msgList = new ArrayList<Message>();
      for (Message msg : checkedMsgs) {
        if (msg.isUnread()) {
          msgList.add(msg);
          msg.setUnread(false);
          uiMessageList.messageList_.put(msg.getId(), msg);
        }
      }
      mailSrv.toggleMessageProperty(SessionsUtils.getSessionProvider(), username, accountId, msgList, Utils.EXO_ISUNREAD);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.findFirstComponentOfType(UIMessageArea.class));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.findFirstComponentOfType(UIFolderContainer.class));
    }
  }

  static public class MarkAsUnReadActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource() ;  
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class) ;
      String username = uiPortlet.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      if(Utils.isEmptyField(accountId)) {
        UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.account-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      List<Message> appliedList = uiMessageList.getCheckedMessage();
      MailService mailSrv = uiMessageList.getApplicationComponent(MailService.class);
      List<Message> msgList = new ArrayList<Message>();
      for (Message msg : appliedList) {
        if (!msg.isUnread()) {
          msgList.add(msg);
          msg.setUnread(true);
          uiMessageList.messageList_.put(msg.getId(), msg);
        }
      }
      mailSrv.toggleMessageProperty(SessionsUtils.getSessionProvider(), username, accountId, msgList, Utils.EXO_ISUNREAD);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.findFirstComponentOfType(UIMessageArea.class));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.findFirstComponentOfType(UIFolderContainer.class));
    }
  }

  static public class AddTagActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource() ; 
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class);
      String accId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      if(Utils.isEmptyField(accId)) {
        UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.account-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class);
      UITagForm uiTagForm = uiMessageList.createUIComponent(UITagForm.class, null, null) ;
      String username = uiPortlet.getCurrentUser();
      MailService mailService = uiMessageList.getApplicationComponent(MailService.class);
      List<Tag> listTags = mailService.getTags(SessionsUtils.getSessionProvider(), username, accId);
      uiPopupAction.activate(uiTagForm, 600, 0, true);
      uiTagForm.setMessageList(uiMessageList.getCheckedMessage());
      uiTagForm.setTagList(listTags) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
    }
  }

  static public class AddTagDnDActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource() ; 
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class); 
      String tagId = event.getRequestContext().getRequestParameter(OBJECTID) ; 
      UITagContainer uiTagContainer = uiPortlet.findFirstComponentOfType(UITagContainer.class);
      String username = uiPortlet.getCurrentUser() ;
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      MailService mailSrv = uiMessageList.getApplicationComponent(MailService.class);
      List<Tag> tagList = new ArrayList<Tag>();      
      tagList.add(mailSrv.getTag(SessionsUtils.getSessionProvider(), username, accountId, tagId));
      mailSrv.addTag(SessionsUtils.getSessionProvider(), username, accountId, uiMessageList.getCheckedMessage(), tagList);
      List<String> tagIdList = new ArrayList<String>() ;
      for (Tag tag : tagList) tagIdList.add(tag.getId()) ;
      for (Message msg : uiMessageList.getCheckedMessage()) {
        if (msg.getTags() != null && msg.getTags().length > 0) {
          for (int i=0 ; i < msg.getTags().length; i++) {
            tagIdList.add(msg.getTags()[i]) ;
          }
        }
        msg.setTags(tagIdList.toArray(new String[]{})) ;
        uiMessageList.messageList_.put(msg.getId(), msg) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getParent()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTagContainer) ;
    }
  }

  static public class MoveMessagesActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource() ;    
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class);
      String accId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
      if(Utils.isEmptyField(accId)) {
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.account-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class);    
      if(uiMessageList.getCheckedMessage().isEmpty()) {
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.checkMessage-select-no-messages", null, ApplicationMessage.INFO)) ;
        return;
      }
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      UIMoveMessageForm uiMoveMessageForm = uiMessageList.createUIComponent(UIMoveMessageForm.class,null, null);
      uiMoveMessageForm.init(accountId);
      uiMoveMessageForm.setMessageList(uiMessageList.getCheckedMessage());
      uiPopupAction.activate(uiMoveMessageForm, 600, 0, true);             
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);        
    }
  }

  static public class MoveDirectMessagesActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource() ;   
      String folderId = event.getRequestContext().getRequestParameter(OBJECTID) ; 
      MailService mailSrv = MailUtils.getMailService();
      String username = MailUtils.getCurrentUser();
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class);
      UIMessagePreview uiMsgPreview = uiPortlet.findFirstComponentOfType(UIMessagePreview.class);
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      List<Message> appliedMsgList = uiMessageList.getCheckedMessage();
      for(Message message : appliedMsgList) {
        mailSrv.moveMessages(SessionsUtils.getSessionProvider(), username, accountId, message, message.getFolders()[0], folderId);
      }       
      uiMessageList.updateList();     
      Message msgPreview = uiMsgPreview.getMessage();
      if (msgPreview != null && appliedMsgList.contains(msgPreview)) uiMsgPreview.setMessage(null);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.findFirstComponentOfType(UIFolderContainer.class)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class));  
    }
  }

  static public class AddContactActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource() ;   
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class);
      Message msg = uiMessageList.messageList_.get(msgId);
      UIPopupAction uiPopup = uiPortlet.getChild(UIPopupAction.class);
      UIPopupActionContainer uiPopupContainer = uiPopup.createUIComponent(UIPopupActionContainer.class, null, null) ;
      uiPopupContainer.setId("UIPopupAddContactForm") ;
      uiPopup.activate(uiPopupContainer, 730, 0, true);

      UIAddContactForm uiAddContactForm = uiPopupContainer.createUIComponent(UIAddContactForm.class, null, null);
      uiPopupContainer.addChild(uiAddContactForm);
      InternetAddress[] addresses  = Utils.getInternetAddress(msg.getFrom());
      String personal = Utils.getPersonal(addresses[0]);
      String firstName = personal;
      String lastName = "";
      if (personal.indexOf(" ") > 0) {
        firstName = personal.substring(0, personal.indexOf(" "));
        lastName = personal.substring(personal.indexOf(" ") + 1, personal.length());
      }
      uiAddContactForm.setFirstNameField(firstName);
      uiAddContactForm.setLastNameField(lastName);
      uiAddContactForm.setEmailField(addresses[0].getAddress());
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup);    
    }
  }

  static public class ImportActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource() ;   
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class);
      String accId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      if(Utils.isEmptyField(accId)) {
        UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.account-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      UIPopupAction uiPopup = uiPortlet.getChild(UIPopupAction.class);
      UIImportForm uiImportForm = uiPopup.createUIComponent(UIImportForm.class, null, null);
      uiImportForm.init(accId);
      if (uiMessageList.getSelectedFolderId() != null) {
        uiImportForm.setSelectedFolder(uiMessageList.getSelectedFolderId());
      } else {
        uiImportForm.setSelectedFolder(Utils.createFolderId(accId, Utils.FD_INBOX, false));
      }
      uiPopup.activate(uiImportForm, 600, 0, true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup);    
    }
  }

  static public class ExportActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource() ;   
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class);
      String accId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
      if(Utils.isEmptyField(accId)) {
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.account-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      UIPopupAction uiPopup = uiPortlet.getChild(UIPopupAction.class);
      if(uiMessageList.getCheckedMessage().isEmpty()) {
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.checkMessage-select-no-messages", null, ApplicationMessage.INFO)) ;
        return;
      } else if (uiMessageList.getCheckedMessage().size() > 1){
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.checkMessage-select-many-messages", null, ApplicationMessage.INFO)) ;
        return;
      }
      if (msgId == null) msgId = uiMessageList.getCheckedMessage().get(0).getId();

      UIExportForm uiExportForm = uiPopup.createUIComponent(UIExportForm.class, null, null);
      uiPopup.activate(uiExportForm, 600, 0, true);
      try {
        Message msg = uiMessageList.messageList_.get(msgId);
        uiExportForm.setExportMessage(msg);
      } catch (Exception e) { }

      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup);  
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
      uiMessageList.updateList(uiMessageList.getMessagePageList().getAvailablePage());
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList);
    }
  }

  static public class RefreshActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource() ; 
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class);
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      uiMessageList.init(accountId);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.findFirstComponentOfType(UIFolderContainer.class));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getParent());
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
      MessageFilter msgFilter = uiMessageList.getMessageFilter();
      msgFilter.setAccountId(accountId);
      msgFilter.setOrderBy(uiMessageList.getSortedBy());
      msgFilter.setAscending(uiMessageList.isAscending_);
      msgFilter.setViewQuery(uiMessageList.getViewQuery());
      if (!msgFilter.getName().equals("Search")) {
        msgFilter.setText("");
        msgFilter.setFolder((uiMessageList.getSelectedFolderId() == null) ? null : new String[] {uiMessageList.getSelectedFolderId()});
        msgFilter.setTag((uiMessageList.getSelectedTagId() == null) ? null : new String[] {uiMessageList.getSelectedTagId()});
      }
      try {
        uiMessageList.setMessagePageList(mailSrv.getMessagePageList(SessionsUtils.getSessionProvider(), username, msgFilter));
        uiMessageList.setMessageFilter(msgFilter);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList);
      } catch (Exception e) {
        return;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getParent());
    }
  }
}