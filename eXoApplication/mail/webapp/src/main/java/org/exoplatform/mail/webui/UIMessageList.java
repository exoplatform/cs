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
import java.util.ResourceBundle;

import javax.jcr.PathNotFoundException;
import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.MailSetting;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.MessageFilter;
import org.exoplatform.mail.service.MessagePageList;
import org.exoplatform.mail.service.SpamFilter;
import org.exoplatform.mail.service.Tag;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.popup.UIAccountCreation;
import org.exoplatform.mail.webui.popup.UIAddContactForm;
import org.exoplatform.mail.webui.popup.UIAddMessageFilter;
import org.exoplatform.mail.webui.popup.UIAskmeReturnReceipt;
import org.exoplatform.mail.webui.popup.UIComfirmPassword;
import org.exoplatform.mail.webui.popup.UIComposeForm;
import org.exoplatform.mail.webui.popup.UIExportForm;
import org.exoplatform.mail.webui.popup.UIImportForm;
import org.exoplatform.mail.webui.popup.UIMoveMessageForm;
import org.exoplatform.mail.webui.popup.UIPopupAction;
import org.exoplatform.mail.webui.popup.UIPopupActionContainer;
import org.exoplatform.mail.webui.popup.UIPrintPreview;
import org.exoplatform.mail.webui.popup.UITagForm;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;

import com.sun.mail.smtp.SMTPSendFailedException;

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
      @EventConfig(listeners = UIMessageList.AddAccountActionListener.class),
      @EventConfig(listeners = UIMessageList.EditDraftActionListener.class),
      @EventConfig(listeners = UIMessageList.AddStarActionListener.class),
      @EventConfig(listeners = UIMessageList.RemoveStarActionListener.class),
      @EventConfig(listeners = UIMessageList.ReplyActionListener.class),
      @EventConfig(listeners = UIMessageList.ReplyAllActionListener.class),
      @EventConfig(listeners = UIMessageList.ForwardActionListener.class),
      @EventConfig(listeners = UIMessageList.CreateFilterActionListener.class),
      @EventConfig(listeners = UIMessageList.DeleteActionListener.class, confirm="UIMessageList.msg.confirm-remove-message"),
      @EventConfig(listeners = UIMessageList.ReportSpamActionListener.class),
      @EventConfig(listeners = UIMessageList.NotSpamActionListener.class),
      @EventConfig(listeners = UIMessageList.PrintActionListener.class),
      @EventConfig(listeners = UIMessageList.MarkAsReadActionListener.class),
      @EventConfig(listeners = UIMessageList.MarkAsUnReadActionListener.class),
      @EventConfig(listeners = UIMessageList.ViewAllActionListener.class),
      @EventConfig(listeners = UIMessageList.ViewAsListActionListener.class),
      @EventConfig(listeners = UIMessageList.ViewAsThreadActionListener.class),
      @EventConfig(listeners = UIMessageList.ViewAsConversationActionListener.class),
      @EventConfig(listeners = UIMessageList.GroupByDateActionListener.class),
      @EventConfig(listeners = UIMessageList.ViewStarredActionListener.class),
      @EventConfig(listeners = UIMessageList.ViewUnreadActionListener.class),
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
      @EventConfig(listeners = UIMessageList.RefreshActionListener.class),
      @EventConfig(listeners = UIMessageList.ComfirmPasswordActionListener.class),
      @EventConfig(listeners = UIMessageList.UpdateListActionListener.class)
    }
)

public class UIMessageList extends UIForm {
  public static final int MODE_LIST = 1 ;
  public static final int MODE_THREAD = 2 ;
  public static final int MODE_CONVERSATION = 3 ;
  public static final int MODE_GROUP_BY_DATE = 4 ;

  public static final int VIEW_ALL = 1 ;
  public static final int VIEW_STARRED = 2 ;
  public static final int VIEW_UNSTARRED = 3 ;
  public static final int VIEW_UNREAD = 4 ;
  public static final int VIEW_READ = 5 ;
  public static final int VIEW_ATTACHMENT = 6 ; 

  private String selectedMessageId_ = null ;
  private String selectedFolderId_ = null ;
  private String selectedTagId_ = null ;
  private String viewQuery_ = null;
  private String sortedBy_ = null;
  private boolean isAscending_ = false;
  private MessagePageList pageList_ = null ;
  private MessageFilter msgFilter_;
  private String accountId_ ;
  public int viewMode = MODE_THREAD ;
  public int viewing_ = VIEW_ALL ;
  public LinkedHashMap<String, Message> messageList_ = new LinkedHashMap<String, Message>();

  public UIMessageList() throws Exception {}

  public void init(String accountId) throws Exception {
    String selectedFolderId = Utils.generateFID(accountId,  Utils.FD_INBOX, false);
    updateMessagePageList(accountId, selectedFolderId);
    setFormId();
  }

  public void updateMessagePageList(String accountId, String selectedFolderId) throws Exception{
    accountId_ = accountId ;
    sortedBy_ = Utils.EXO_RECEIVEDDATE ;
    String username = MailUtils.getCurrentUser();
    MailService mailSrv = MailUtils.getMailService();
    MessageFilter filter = getMessageFilter();
    if (filter == null) filter = new MessageFilter("Folder");
    if (viewMode == MODE_THREAD || viewMode == MODE_CONVERSATION){
      filter.setOrderBy(Utils.EXO_LAST_UPDATE_TIME);
      filter.setHasStructure(true) ;
    }
    if (!Utils.isEmptyField(accountId)) {
      filter.setAccountId(accountId) ;
      if (filter.getFolder() == null) {
        if (!filter.getName().equals("Search")) {
          selectedFolderId_ = selectedFolderId;
          filter.setFolder(new String[] { selectedFolderId_ });
        }
        
      } else {
        selectedFolderId_ = filter.getFolder()[0];
      }
      long currentPage = 1 ;
      if (pageList_ != null) currentPage = pageList_.getCurrentPage() ;
      MessagePageList currentPageList = mailSrv.getMessagePageList(username, filter) ;
      setMessagePageList(currentPageList, currentPage);
    } else {
      messageList_.clear();
    }
    setMessageFilter(filter);
  }
  
  public void setFormId(){
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
    String formId = ((PortletRequestContext)context).getWindowId();
    UIMailPortlet mailportlet = getAncestorOfType(UIMailPortlet.class);
    UIMessageArea uiMsgArea = getAncestorOfType(UIMessageArea.class);
    if(mailportlet == null){
     if(uiMsgArea != null) {
       mailportlet = uiMsgArea.getAncestorOfType(UIMailPortlet.class);
     }
    }
    if(mailportlet != null) mailportlet.setFormId(formId);
  }
  
  public boolean isMessagePreviewRendered() {
    try {
      return getAncestorOfType(UIMessageArea.class).getChild(UIMessagePreview.class).isRendered() ;
    } catch (Exception e) {
      return false ;
    }
  }
  
  public void refreshBrowser(String accId) throws Exception {
    String username = MailUtils.getCurrentUser();
    MailService mailSrv = MailUtils.getMailService();
    try {
      Account acc = mailSrv.getAccountById(username, accId);
      if (acc == null) {
        UIMailPortlet uiPortlet = getAncestorOfType(UIMailPortlet.class);
        try {
          String selectedAccId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
          if (!MailUtils.isFieldEmpty(selectedAccId)) {
            init(selectedAccId);
          } else {
            init(null);
            uiPortlet.findFirstComponentOfType(UIMessagePreview.class).setMessage(null);
          }
        } catch(Exception e) {
          init(null);
          uiPortlet.findFirstComponentOfType(UIMessagePreview.class).setMessage(null);
        }
      }
    } catch (Exception ex) { }
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
    return (getSelectedFolderId() != null) ? getSelectedFolderId().equals(Utils.generateFID(accountId_, Utils.FD_SPAM, false)) : false ;
  }

  public boolean selectedDraftFolder() throws Exception {
    return (getSelectedFolderId() != null) ? getSelectedFolderId().equals(Utils.generateFID(accountId_, Utils.FD_DRAFTS, false)) : false ;
  }

  public boolean selectedSentFolder() throws Exception {
    return (getSelectedFolderId() != null) ? getSelectedFolderId().equals(Utils.generateFID(accountId_, Utils.FD_SENT, false)) : false ;
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
    setMessagePageList(pageList, 0);
  }
  
  public void setMessagePageList(MessagePageList pageList, long currentPage) throws Exception {
    pageList_ = pageList ;
    if (currentPage > 0 ) updateList(currentPage);
    else updateList();
  }

  public void updateList() throws Exception {
    long page = 1;
    if (pageList_ != null) page = pageList_.getCurrentPage() ;
    updateList(page);
  }

  public void updateList(long page) throws Exception {
    getChildren().clear();
    messageList_.clear();    
    if(pageList_ != null) {
      List<Message> msgList = new ArrayList<Message>() ;
      try {
        msgList = pageList_.getPage(page, MailUtils.getCurrentUser()) ;
        if (page > 1 && msgList.size() == 0) {
          msgList = pageList_.getPage(page - 1, MailUtils.getCurrentUser()) ;
        }
      } catch(Exception e) {
        String username = MailUtils.getCurrentUser();
        MailService mailSrv = MailUtils.getMailService();
        try {
          setMessagePageList(mailSrv.getMessagePageList(username, getMessageFilter()));
        } catch(Exception ex) {
          setMessagePageList(null);
        }
        return ;
      }
      for (Message message : msgList) {
        String encodeId = MailUtils.encodeMailId(message.getId());
        UIFormCheckBoxInput<Boolean> uiCheckBox = new UIFormCheckBoxInput<Boolean>(encodeId, encodeId, false);
        addUIFormInput(uiCheckBox);
        messageList_.put(message.getId(), message);
      }
    }
  }

  public List<Message> getCheckedMessage() throws Exception {
    return getCheckedMessage(true);
  }
  
  @SuppressWarnings("unchecked")
  public List<Message> getCheckedMessage(boolean includeGroupedMsgs) throws Exception {
    List<Message> checkedList = new ArrayList<Message>();
    UIFormCheckBoxInput uiCheckbox;
    for (Message msg : getMessageList()) {
      uiCheckbox = getUIFormCheckBoxInput(MailUtils.encodeMailId(msg.getId()));
      if (uiCheckbox != null && uiCheckbox.isChecked()) {
        checkedList.add(msg);
        if (viewMode == MODE_CONVERSATION && includeGroupedMsgs) {
          Message childMsg ;
          for (String childMsgId : msg.getGroupedMessageIds()) {
            childMsg = messageList_.get(childMsgId);
            if (childMsg != null && 
                !childMsg.getFolders()[0].equals(Utils.generateFID(accountId_, Utils.FD_SENT, false))) 
              checkedList.add(childMsg) ;
          }
        }
      }
    }
    return checkedList;
  }

  public List<Tag> getTags(Message msg) throws Exception {
    UIMailPortlet uiPortlet = getAncestorOfType(UIMailPortlet.class);
    String username = uiPortlet.getCurrentUser() ;
    MailService mailSrv = getApplicationComponent(MailService.class);
    List<Tag> tagList = new ArrayList<Tag>();
    try {
      if (msg.getTags() != null && msg.getTags().length > 0) {
        for (int i = 0; i < msg.getTags().length; i++) {
          Tag tag = mailSrv.getTag(username, accountId_, msg.getTags()[i]);
          tagList.add(tag);
        }
      }
    } catch(Exception e) {
      e.printStackTrace() ;
    }
    return tagList;
  } 

  public List<Folder> getFolders(Message msg) throws Exception {
    UIMailPortlet uiPortlet = getAncestorOfType(UIMailPortlet.class) ;
    String username = uiPortlet.getCurrentUser() ;
    MailService mailSrv = getApplicationComponent(MailService.class) ;
    List<Folder> folderList = new ArrayList<Folder>() ;
    String[] folders = msg.getFolders() ;
    if (folders != null && folders.length > 0) {
      for (int i = 0; i < folders.length; i++) {
        Folder folder = mailSrv.getFolder(username, accountId_, folders[i]) ;
        folderList.add(folder) ;
      }
    }
    return folderList ;
  }

  static public class SelectMessageActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      msgId = MailUtils.decodeMailId(msgId);
      UIMessageList uiMessageList = event.getSource();
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class);
      UIMessagePreview uiMessagePreview = uiPortlet.findFirstComponentOfType(UIMessagePreview.class);
      UIFolderContainer uiFolderContainer = uiPortlet.findFirstComponentOfType(UIFolderContainer.class);
      String username = uiPortlet.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      String folderId = uiPortlet.findFirstComponentOfType(UIFolderContainer.class).getSelectedFolder();
      if (Utils.isEmptyField(folderId)) folderId = "";
      
      uiMessageList.updateList();
      Message msg = uiMessageList.messageList_.get(msgId);
      
      if (msg != null) {
        MailService mailSrv = uiPortlet.getApplicationComponent(MailService.class);
        msg = mailSrv.loadTotalMessage(username, accountId, msg) ;
        Account account = mailSrv.getAccountById(username, accountId);
              
        uiMessageList.setSelectedMessageId(msgId);
        for (Message uncheckedMsg : uiMessageList.messageList_.values()) {
          UIFormCheckBoxInput<Boolean> uiCheckbox = uiMessageList.getChildById(MailUtils.encodeMailId(uncheckedMsg.getId()));
          if (uiCheckbox != null ) {
            if (uncheckedMsg.getId().equals(msg.getId())) uiCheckbox.setChecked(true);
            else uiCheckbox.setChecked(false); 
          }
        }
        
        if(msg.hasAttachment()) {
        	Message msgMem = mailSrv.loadTotalMessage(username, accountId, msg) ;
          msg.setAttachements(msgMem.getAttachments());
        }
        uiMessagePreview.setMessage(msg);
        
        List<Message> showedMessages = new ArrayList<Message>() ;
        showedMessages.add(msg) ;
        if (uiMessageList.viewMode == MODE_CONVERSATION) {
          if (msg != null && msg.getGroupedMessageIds().size() > 0) {
            for (String id : msg.getGroupedMessageIds()) {
              Message msgMem = uiMessageList.messageList_.get(id);
              if (msgMem.hasAttachment()) {
                msgMem = mailSrv.loadTotalMessage(username, accountId, msgMem) ;
              }
              showedMessages.add(msgMem) ;
            }
          }
        }
        uiMessagePreview.setShowedMessages(showedMessages) ;
       

        List<Message> msgs  = new ArrayList<Message>();
        List<String> unreadMsgIds = new ArrayList<String>();
        if (msg.isUnread()) msgs.add(msg);
        if ((uiMessageList.viewMode == MODE_CONVERSATION) && (msg.getGroupedMessageIds().size() > 0)) {
          for (String id : msg.getGroupedMessageIds()) {
            Message m = uiMessageList.messageList_.get(id);
            if (m.isUnread()) {
              msgs.add(m);
              unreadMsgIds.add(m.getId());
            }
          }
          uiMessagePreview.setUnreadMessages(unreadMsgIds);
        }
        
        if (msgs.size() > 0) {
          try {
            uiMessageList.toggleMsgStatus(username, accountId, msgs, folderId, false);
          } catch (PathNotFoundException e) {
            e.printStackTrace();
            uiMessageList.setMessagePageList(null) ;
            uiPortlet.findFirstComponentOfType(UISelectAccount.class).refreshItems();
            event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet); 
            
            UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
            uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.deleted_account", null, ApplicationMessage.WARNING)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return ;
          }
          for (Message m : msgs) {
            m.setUnread(false);
            uiMessageList.messageList_.put(m.getId(), m);
          }
          event.getRequestContext().addUIComponentToUpdateByAjax(uiFolderContainer); 
        }
        
        if (msg.isReturnReceipt() && !msg.getFrom().contains(account.getEmailAddress())) {
          ((UIMessageArea)uiMessageList.getParent()).reloadMailSetting();
          long requestReturnReceipt = ((UIMessageArea)uiMessageList.getParent()).getMailSetting().getSendReturnReceipt();
          if (requestReturnReceipt == MailSetting.SEND_RECEIPT_ASKSME) {
            UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class);
            uiPopupAction.activate(UIAskmeReturnReceipt.class, 600).setSelectedMsg(msg);
            event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
          } else if (requestReturnReceipt == MailSetting.SEND_RECEIPT_ALWAYS) {
            MailService mailService = uiMessageList.getApplicationComponent(MailService.class);
            UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
            ResourceBundle res = event.getRequestContext().getApplicationResourceBundle() ;
            try {             
              mailService.sendReturnReceipt(username, accountId, msgId, res);
            } catch (AddressException e) {
              uiApp.addMessage(new ApplicationMessage("UIEnterPasswordDialog.msg.there-was-an-error-parsing-the-addresses-sending-failed", null)) ;
              event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
              return;
            } catch (AuthenticationFailedException e) {
              uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.please-check-configuration-for-smtp-server", null)) ;
              event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
              return;
            } catch (SMTPSendFailedException e) {
              uiApp.addMessage(new ApplicationMessage("UIEnterPasswordDialog.msg.sorry-there-was-an-error-sending-the-message-sending-failed", null)) ;
              event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
              return;
            } catch (MessagingException e) {
              uiApp.addMessage(new ApplicationMessage("UIEnterPasswordDialog.msg.there-was-an-unexpected-error-sending-falied", null)) ;
              event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
              return ;
            }
            
            List<Message> msgL = new ArrayList<Message>();
            msgL.add(msg);
            mailSrv.toggleMessageProperty(username, accountId, msgL, folderId, Utils.IS_RETURN_RECEIPT, false);
          }
        } 
        
      //  event.getRequestContext().addUIComponentToUpdateByAjax(uiFolderContainer);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getParent());
      }          
    }
  }
  
  private void toggleMsgStatus(String username, String accountId, List<Message> msgs, String folderId, boolean value) throws Exception {
    MailUtils.getMailService().toggleMessageProperty(username, accountId, msgs, folderId, Utils.EXO_ISUNREAD, value);
  }
  
  public boolean isShowUnread(Message msg) throws Exception {
    boolean showUnread = false;
    try {
      if ((viewMode == MODE_CONVERSATION) && (msg.getGroupedMessageIds().size() > 0)) {
        for (String id : msg.getGroupedMessageIds()) {
          Message m = messageList_.get(id);
          if (m.isUnread()) showUnread = true; 
        }
      }
    } catch(Exception e) { }
    
    return showUnread;
  }

  static public class ReadActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      // need to implement
    }
  }

  static public class EditDraftActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      msgId = MailUtils.decodeMailId(msgId);
      UIMessageList uiMessageList = event.getSource();
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class);
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      Message msg = uiMessageList.messageList_.get(msgId);
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
      UIPopupActionContainer uiPopupContainer = uiPopupAction.createUIComponent(UIPopupActionContainer.class, null, "UIPopupActionComposeContainer") ;
      uiPopupAction.activate(uiPopupContainer, MailUtils.MAX_POPUP_WIDTH, 0, true);

      UIComposeForm uiComposeForm = uiPopupContainer.createUIComponent(UIComposeForm.class, null, null);
      try {
        if (msg != null) msg = uiMessageList.getApplicationComponent(MailService.class).loadTotalMessage(uiPortlet.getCurrentUser(), accountId, msg);
        uiComposeForm.init(accountId, msg, uiComposeForm.MESSAGE_IN_DRAFT);
      } catch (PathNotFoundException e) {
        uiMessageList.setMessagePageList(null) ;
        uiPortlet.findFirstComponentOfType(UISelectAccount.class).refreshItems();
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
        UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.deleted_account", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      uiPopupContainer.addChild(uiComposeForm) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;  
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getParent());
    }
  }

  static public class AddStarActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      msgId = MailUtils.decodeMailId(msgId);
      UIMessageList uiMessageList = event.getSource();
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class);
      String username = uiPortlet.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      String folderId = uiPortlet.findFirstComponentOfType(UIFolderContainer.class).getSelectedFolder();
      if (Utils.isEmptyField(folderId)) folderId = "";
      UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
      if(Utils.isEmptyField(accountId)) {
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.account-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      if(msgId == null && uiMessageList.getCheckedMessage().isEmpty()) {
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.checkMessage-select-no-messages", null, ApplicationMessage.INFO)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      }
      MailService mailSrv = uiPortlet.getApplicationComponent(MailService.class);
      Message msg = uiMessageList.messageList_.get(msgId);
      if ( msg != null ) {
        List<Message> msgList = new ArrayList<Message>() ;
        msgList.add(msg);
        msg.setHasStar(!msg.hasStar());
        uiMessageList.messageList_.put(msg.getId(), msg);
        try {
          mailSrv.toggleMessageProperty(username, accountId, msgList, folderId, Utils.EXO_STAR, msg.hasStar());
        } catch (PathNotFoundException e) {
          uiMessageList.setMessagePageList(null) ;
          uiPortlet.findFirstComponentOfType(UISelectAccount.class).refreshItems();
          event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
          
          uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.deleted_account", null, ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
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
        try {
          mailSrv.toggleMessageProperty(username, accountId, msgList, folderId, Utils.EXO_STAR, true);
        } catch (PathNotFoundException e) {
          uiMessageList.setMessagePageList(null) ;
          uiPortlet.findFirstComponentOfType(UISelectAccount.class).refreshItems();
          event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
          
          uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.deleted_account", null, ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
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
      String folderId = uiPortlet.findFirstComponentOfType(UIFolderContainer.class).getSelectedFolder();
      if (Utils.isEmptyField(folderId)) folderId = "";
      UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
      if(Utils.isEmptyField(accountId)) {
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.account-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      List<Message> checkedMsgList = uiMessageList.getCheckedMessage();  
      MailService mailSrv = uiPortlet.getApplicationComponent(MailService.class);
      if(checkedMsgList.isEmpty()) {
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.checkMessage-select-no-messages", null, ApplicationMessage.INFO)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      }
      List<Message> msgList = new ArrayList<Message>() ;
      for (Message msg : checkedMsgList) {
        if (msg.hasStar()) {
          msgList.add(msg);
          msg.setHasStar(false);
          uiMessageList.messageList_.put(msg.getId(), msg);
        }
      }
      try {
        mailSrv.toggleMessageProperty(username, accountId, msgList, folderId, Utils.EXO_STAR, false);       
      } catch (PathNotFoundException e) {
        uiMessageList.setMessagePageList(null) ;
        uiPortlet.findFirstComponentOfType(UISelectAccount.class).refreshItems();
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
        
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.deleted_account", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getParent());
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
      try {
        uiMessageList.filterMessage("");
      } catch (PathNotFoundException e) {
        uiMessageList.setMessagePageList(null) ;
        uiPortlet.findFirstComponentOfType(UISelectAccount.class).refreshItems();
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
        UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.deleted_account", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      uiMessageList.viewing_ = VIEW_ALL ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class));
    }
  }

  static public class ViewAsListActionListener extends EventListener<UIMessageList> {
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
      if (uiMessageList.viewMode == MODE_LIST) return ;
      if (uiMessageList.viewMode == MODE_CONVERSATION) {
        UIMessagePreview uiMsgPreview = uiPortlet.findFirstComponentOfType(UIMessagePreview.class) ;
        List<Message> showedMsgs = new ArrayList<Message>();
        showedMsgs.add(uiMsgPreview.getMessage()) ;
        uiMsgPreview.setShowedMessages(showedMsgs);
      }
      MailService mailSrv = uiMessageList.getApplicationComponent(MailService.class);
      String username = uiPortlet.getCurrentUser();
      MessageFilter filter = uiMessageList.getMessageFilter() ;
      filter.setHasStructure(false) ;
      try {
        uiMessageList.setMessagePageList(mailSrv.getMessagePageList(username, filter)) ;
      } catch (PathNotFoundException e) {
        uiMessageList.setMessagePageList(null) ;
        uiPortlet.findFirstComponentOfType(UISelectAccount.class).refreshItems();
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
        UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.deleted_account", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      uiMessageList.viewMode = MODE_LIST ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class));
    }
  }

  static public class ViewAsThreadActionListener extends EventListener<UIMessageList> {
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
      if (uiMessageList.viewMode == MODE_THREAD) return ;
      if (uiMessageList.viewMode == MODE_CONVERSATION) {
        UIMessagePreview uiMsgPreview = uiPortlet.findFirstComponentOfType(UIMessagePreview.class) ;
        List<Message> showedMsgs = new ArrayList<Message>();
        showedMsgs.add(uiMsgPreview.getMessage()) ;
        uiMsgPreview.setShowedMessages(showedMsgs);
      } else if (uiMessageList.viewMode == MODE_LIST){
        MailService mailSrv = uiMessageList.getApplicationComponent(MailService.class);
        String username = uiPortlet.getCurrentUser();
        MessageFilter filter = uiMessageList.getMessageFilter() ;
        filter.setHasStructure(true) ;
        filter.setOrderBy(Utils.EXO_LAST_UPDATE_TIME);
        try {
          uiMessageList.setMessagePageList(mailSrv.getMessagePageList(username, filter)) ;
        } catch (PathNotFoundException e) {
          uiMessageList.setMessagePageList(null) ;
          uiPortlet.findFirstComponentOfType(UISelectAccount.class).refreshItems();
          event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
          UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
          uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.deleted_account", null, ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
      }
      uiMessageList.viewMode = MODE_THREAD ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class));
    }
  }

  static public class ViewAsConversationActionListener extends EventListener<UIMessageList> {
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
      if (uiMessageList.viewMode == MODE_CONVERSATION) return ;
      if (uiMessageList.viewMode == MODE_THREAD) {
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
      } else if (uiMessageList.viewMode == MODE_LIST) {
        MailService mailSrv = uiMessageList.getApplicationComponent(MailService.class);
        String username = uiPortlet.getCurrentUser();
        MessageFilter filter = uiMessageList.getMessageFilter() ;
        filter.setHasStructure(true) ;
        filter.setOrderBy(Utils.EXO_LAST_UPDATE_TIME);
        try {
          uiMessageList.setMessagePageList(mailSrv.getMessagePageList(username, filter)) ;
        } catch (PathNotFoundException e) {
          uiMessageList.setMessagePageList(null) ;
          uiPortlet.findFirstComponentOfType(UISelectAccount.class).refreshItems();
          event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
          UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
          uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.deleted_account", null, ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
      }
      uiMessageList.viewMode = MODE_CONVERSATION ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class));
    }
  }

  static public class GroupByDateActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      //TODO: haven't implemented yet
      UIMessageList uiMessageList = event.getSource();
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class);
      String accId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      if(Utils.isEmptyField(accId)) {
        UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.account-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      if (uiMessageList.viewMode == MODE_GROUP_BY_DATE) return ;
      if (uiMessageList.viewMode == MODE_CONVERSATION) {
        UIMessagePreview uiMsgPreview = uiPortlet.findFirstComponentOfType(UIMessagePreview.class) ;
        List<Message> showedMsgs = new ArrayList<Message>();
        showedMsgs.add(uiMsgPreview.getMessage()) ;
        uiMsgPreview.setShowedMessages(showedMsgs);
      }
      MailService mailSrv = uiMessageList.getApplicationComponent(MailService.class);
      String username = uiPortlet.getCurrentUser();
      MessageFilter filter = uiMessageList.getMessageFilter() ;
      filter.setHasStructure(false) ;
      try {
        uiMessageList.setMessagePageList(mailSrv.getMessagePageList(username, filter)) ;
      } catch (PathNotFoundException e) {
        uiMessageList.setMessagePageList(null) ;
        uiPortlet.findFirstComponentOfType(UISelectAccount.class).refreshItems();
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
        UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.deleted_account", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      uiMessageList.viewMode = MODE_GROUP_BY_DATE ;
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
      try {
        uiMessageList.filterMessage("@" + Utils.EXO_STAR + "='true'");
      } catch (PathNotFoundException e) {
        uiMessageList.setMessagePageList(null) ;
        uiPortlet.findFirstComponentOfType(UISelectAccount.class).refreshItems();
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
        UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.deleted_account", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      uiMessageList.viewing_ = VIEW_STARRED ;
      uiMessageList.viewMode = MODE_LIST;
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
      try {
      uiMessageList.filterMessage("@" + Utils.EXO_ISUNREAD + "='true'");
      } catch (PathNotFoundException e) {
        uiMessageList.setMessagePageList(null) ;
        uiPortlet.findFirstComponentOfType(UISelectAccount.class).refreshItems();
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
        UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.deleted_account", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      uiMessageList.viewing_ = VIEW_UNREAD ;
      uiMessageList.viewMode = MODE_LIST;
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
      try {
        uiMessageList.filterMessage("@" + Utils.EXO_HASATTACH + "='true'");
      } catch (PathNotFoundException e) {
        uiMessageList.setMessagePageList(null) ;
        uiPortlet.findFirstComponentOfType(UISelectAccount.class).refreshItems();
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
        UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.deleted_account", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      uiMessageList.viewing_ = VIEW_ATTACHMENT ;
      uiMessageList.viewMode = MODE_LIST;
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
    msgFilter.setHasStructure(false) ;
    setMessagePageList(mailSrv.getMessagePageList(username, msgFilter));
  }

  static public class ReplyActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource() ; 
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class) ;
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      msgId = MailUtils.decodeMailId(msgId);
      String accId = uiPortlet.getChild(UINavigationContainer.class).getChild(UISelectAccount.class).getSelectedValue() ;

      // Verify
      UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;     
      if(Utils.isEmptyField(accId)) {
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.account-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      
      List<Message> checkedMsgs = uiMessageList.getCheckedMessage(false);
      if(checkedMsgs.isEmpty()) {
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.checkMessage-select-no-messages", null, ApplicationMessage.INFO)) ;
        return;
      } else if (checkedMsgs.size() > 1){
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.checkMessage-select-many-messages", null, ApplicationMessage.INFO)) ;
        return;
      }      
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
      UIPopupActionContainer uiPopupContainer = uiPopupAction.createUIComponent(UIPopupActionContainer.class, null, "UIPopupActionComposeContainer") ;
      uiPopupAction.activate(uiPopupContainer, MailUtils.MAX_POPUP_WIDTH, 0, true);

      UIComposeForm uiComposeForm = uiPopupContainer.createUIComponent(UIComposeForm.class, null, null);

      Message message = null;
      if (!Utils.isEmptyField(msgId)) message = uiMessageList.messageList_.get(msgId) ; 
      else  message = checkedMsgs.get(0);
      try {
        if (message != null) message = uiMessageList.getApplicationComponent(MailService.class).loadTotalMessage(uiPortlet.getCurrentUser(), accId, message);
        uiComposeForm.init(accId, message, uiComposeForm.MESSAGE_REPLY);
      } catch (Exception e) {
        uiMessageList.setMessagePageList(null) ;
        uiPortlet.findFirstComponentOfType(UISelectAccount.class).refreshItems();
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.deleted_account", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return; 
      }
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
      msgId = MailUtils.decodeMailId(msgId);
      String accId = uiPortlet.getChild(UINavigationContainer.class).getChild(UISelectAccount.class).getSelectedValue() ;

      // Verify
      UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ; 
      if(Utils.isEmptyField(accId)) {
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.account-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      
      List<Message> checkedMsgs = uiMessageList.getCheckedMessage(false);
      if(checkedMsgs.isEmpty()) {
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.checkMessage-select-no-messages", null, ApplicationMessage.INFO)) ;
        return;
      } else if (checkedMsgs.size() > 1){
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.checkMessage-select-many-messages", null, ApplicationMessage.INFO)) ;
        return;
      }      
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
      UIPopupActionContainer uiPopupContainer = uiPopupAction.createUIComponent(UIPopupActionContainer.class, null, "UIPopupActionComposeContainer") ;
      uiPopupAction.activate(uiPopupContainer, MailUtils.MAX_POPUP_WIDTH, 0, true);

      UIComposeForm uiComposeForm = uiPopupContainer.createUIComponent(UIComposeForm.class, null, null);

      Message message = null;
      if (!Utils.isEmptyField(msgId)) message = uiMessageList.messageList_.get(msgId) ;
      else  message = checkedMsgs.get(0);
      try {
        if (message != null) message = uiMessageList.getApplicationComponent(MailService.class).loadTotalMessage(uiPortlet.getCurrentUser(), accId, message);
        uiComposeForm.init(accId, message, uiComposeForm.MESSAGE_REPLY_ALL);
      } catch (Exception e) {
        uiMessageList.setMessagePageList(null) ;
        uiPortlet.findFirstComponentOfType(UISelectAccount.class).refreshItems();
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.deleted_account", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return; 
      }
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
      msgId = MailUtils.decodeMailId(msgId);
      String accId = uiPortlet.getChild(UINavigationContainer.class).getChild(UISelectAccount.class).getSelectedValue() ;

      // Verify
      UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
      if(Utils.isEmptyField(accId)) {
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.account-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      
      List<Message> checkedMsgs = uiMessageList.getCheckedMessage(false);
      if(checkedMsgs.isEmpty()) {
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.checkMessage-select-no-messages", null, ApplicationMessage.INFO)) ;
        return;
      } else if (checkedMsgs.size() > 1){
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.checkMessage-select-many-messages", null, ApplicationMessage.INFO)) ;
        return;
      }      
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
      UIPopupActionContainer uiPopupContainer = uiPopupAction.createUIComponent(UIPopupActionContainer.class, null, "UIPopupActionComposeContainer") ;
      uiPopupAction.activate(uiPopupContainer, MailUtils.MAX_POPUP_WIDTH, 0, true);

      UIComposeForm uiComposeForm = uiPopupContainer.createUIComponent(UIComposeForm.class, null, null);

      Message message = null;
      if (!Utils.isEmptyField(msgId)) message = uiMessageList.messageList_.get(msgId) ;
      else  message = checkedMsgs.get(0);
      try {
        if (message != null) message = uiMessageList.getApplicationComponent(MailService.class).loadTotalMessage(uiPortlet.getCurrentUser(), accId, message);
        uiComposeForm.init(accId, message, uiComposeForm.MESSAGE_FOWARD);
      } catch (Exception e) {
        uiMessageList.setMessagePageList(null) ;
        uiPortlet.findFirstComponentOfType(UISelectAccount.class).refreshItems();
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.deleted_account", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return; 
      }
      uiPopupContainer.addChild(uiComposeForm) ;

      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.findFirstComponentOfType(UIMessageArea.class));
    }
  }  

  static  public class CreateFilterActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource() ;
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class);
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      msgId = MailUtils.decodeMailId(msgId);
      // Verify
      UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
      List<Message> checkedMsgs = uiMessageList.getCheckedMessage(false) ;
      if(checkedMsgs.isEmpty()) {
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.checkMessage-select-no-messages", null, ApplicationMessage.INFO)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      } else if (checkedMsgs.size() > 1){
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.checkMessage-select-many-messages", null, ApplicationMessage.INFO)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      }
      
      Message message = null;
      if (!Utils.isEmptyField(msgId)) message = uiMessageList.messageList_.get(msgId) ;
      else  message = uiMessageList.getCheckedMessage().get(0);
      
      String from = Utils.getAddresses(message.getFrom())[0];
      MessageFilter filter = new MessageFilter(from);
      filter.setFrom(from);
      filter.setFromCondition(Utils.CONDITION_CONTAIN);
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
      UIAddMessageFilter uiEditMessageFilter = uiPopupAction.createUIComponent(UIAddMessageFilter.class, null, null);
      try {
        uiEditMessageFilter.init(accountId);
      } catch (Exception e) {
        uiMessageList.setMessagePageList(null) ;
        uiPortlet.findFirstComponentOfType(UISelectAccount.class).refreshItems();
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.deleted_account", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return; 
      }
      uiPopupAction.activate(uiEditMessageFilter, 650, 0, false) ;
      uiEditMessageFilter.setCurrentFilter(filter);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.findFirstComponentOfType(UIMessageArea.class)) ;
    }
  }
  
  static public class DeleteActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource();
      String msgId = null ;
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class);
      String username = uiPortlet.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();

      // Verify
      UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
      if(Utils.isEmptyField(accountId)) {
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.account-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } else if(uiMessageList.getCheckedMessage().isEmpty()) {
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.checkMessage-select-no-messages", null, ApplicationMessage.INFO)) ;
        return;
      }
      UIFolderContainer uiFolderContainer = uiPortlet.findFirstComponentOfType(UIFolderContainer.class);
      UIMessagePreview uiMessagePreview = uiPortlet.findFirstComponentOfType(UIMessagePreview.class);
      Message msgPreview = null;
      if (uiMessagePreview.getMessage() != null) msgPreview = uiMessagePreview.getMessage();
      UITagContainer uiTags = uiPortlet.findFirstComponentOfType(UITagContainer.class); 
      MailService mailSrv = uiMessageList.getApplicationComponent(MailService.class);
      List<Message> appliedMsgList = new ArrayList<Message>();
      if (!Utils.isEmptyField(msgId)) { 
        appliedMsgList.add(uiMessageList.messageList_.get(msgId));
      } else {
        appliedMsgList = uiMessageList.getCheckedMessage();
      }
      String trashFolderId = Utils.generateFID(accountId, Utils.FD_TRASH, false) ;
      String selectedFolderId = uiMessageList.getSelectedFolderId() ;
      try {
        if (selectedFolderId != null && selectedFolderId.equals(trashFolderId)) { 
          mailSrv.removeMessages(username, accountId, appliedMsgList, true);
        } else {
          for (Message message : appliedMsgList)
            mailSrv.moveMessage(username, accountId, message, message.getFolders()[0], trashFolderId);
        }
      } catch (PathNotFoundException e) {
        uiMessageList.setMessagePageList(null) ;
        uiPortlet.findFirstComponentOfType(UISelectAccount.class).refreshItems();
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
        
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.deleted_account", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      if (msgPreview != null && appliedMsgList.contains(msgPreview)) uiMessagePreview.setMessage(null);
      uiMessageList.updateList();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiFolderContainer);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getParent());
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTags);
    }
  }

  static public class ReportSpamActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource();
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      msgId = MailUtils.decodeMailId(msgId);
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class) ;
      String username = MailUtils.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      if(Utils.isEmptyField(accountId)) {
        UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.account-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
      if(uiMessageList.getCheckedMessage().isEmpty()) {
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.checkMessage-select-no-messages", null, ApplicationMessage.INFO)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      } 
      
      MailService mailSrv = MailUtils.getMailService();
      List<Message> checkedMessageList = new ArrayList<Message>();
      if (!Utils.isEmptyField(msgId)) { 
        checkedMessageList.add(uiMessageList.messageList_.get(msgId));
      } else {
        checkedMessageList = uiMessageList.getCheckedMessage();
      }    
      SpamFilter spamFilter = null ;
      try {
        spamFilter = mailSrv.getSpamFilter(username, accountId);
      } catch (PathNotFoundException e) {
        uiMessageList.setMessagePageList(null) ;
        uiPortlet.findFirstComponentOfType(UISelectAccount.class).refreshItems();
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.deleted_account", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return; 
      }
      for(Message message: checkedMessageList) {
        mailSrv.moveMessage(username, accountId, message, message.getFolders()[0], Utils.generateFID(accountId, Utils.FD_SPAM, false));
        spamFilter.reportSpam(message);
      }       
      mailSrv.saveSpamFilter(username, accountId, spamFilter);

      uiMessageList.updateList(); 
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.findFirstComponentOfType(UIFolderContainer.class)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class));  
    }
  }

  static public class NotSpamActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource();
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      msgId = MailUtils.decodeMailId(msgId);
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class) ;
      String username = MailUtils.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
      if(Utils.isEmptyField(accountId)) {
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.account-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      if(uiMessageList.getCheckedMessage().isEmpty()) {
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.checkMessage-select-no-messages", null, ApplicationMessage.INFO)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      } 
      
      MailService mailSrv = MailUtils.getMailService();
      List<Message> checkedMessageList = new ArrayList<Message>();
      if (!Utils.isEmptyField(msgId)) { 
        checkedMessageList.add(uiMessageList.messageList_.get(msgId));
      } else {
        checkedMessageList = uiMessageList.getCheckedMessage();
      }    

      SpamFilter spamFilter = null ;
      try {
        spamFilter = mailSrv.getSpamFilter(username, accountId);
      } catch (PathNotFoundException e) {
        uiMessageList.setMessagePageList(null) ;
        uiPortlet.findFirstComponentOfType(UISelectAccount.class).refreshItems();
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.deleted_account", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return; 
      }

      for(Message message: checkedMessageList) {
        mailSrv.moveMessage(username, accountId, message, message.getFolders()[0], Utils.generateFID(accountId, Utils.FD_INBOX, false));
        spamFilter.notSpam(message);
      }       
      mailSrv.saveSpamFilter(username, accountId, spamFilter);

      uiMessageList.updateList(); 
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.findFirstComponentOfType(UIFolderContainer.class)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class));  
    }
  }

  static public class PrintActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource();
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      msgId = MailUtils.decodeMailId(msgId);
      UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class);
      List<Message> checkedMsgs = uiMessageList.getCheckedMessage(false);
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      if(Utils.isEmptyField(accountId)) {
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.account-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      if(checkedMsgs.isEmpty()) {
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.checkMessage-select-no-messages", null, ApplicationMessage.INFO)) ;
        return;
      } else if (checkedMsgs.size() > 1){
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.checkMessage-select-many-messages", null, ApplicationMessage.INFO)) ;
        return;
      }
      String username = MailUtils.getCurrentUser();
      MailService mailSrv = MailUtils.getMailService();
      UIPopupAction uiPopup = uiPortlet.getChild(UIPopupAction.class);
      UIPrintPreview uiPrintPreview ;
      Account acc = mailSrv.getAccountById(username, accountId);
      if (acc != null ){
        uiPrintPreview = uiPopup.activate(UIPrintPreview.class, 700) ;
        uiPrintPreview.setAcc(acc) ; 
      } else {
        uiMessageList.setMessagePageList(null) ;
        uiPortlet.findFirstComponentOfType(UISelectAccount.class).refreshItems();
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.deleted_account", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return; 
      }
      Message message = null ;
      if (!Utils.isEmptyField(msgId)) message = uiMessageList.messageList_.get(msgId) ;
      else  message = checkedMsgs.get(0);
      if (message != null) message = uiMessageList.getApplicationComponent(MailService.class).loadTotalMessage(uiPortlet.getCurrentUser(), accountId, message);  
      if (message != null && !message.isLoaded()) {
        message = mailSrv.loadTotalMessage(username, accountId, message) ;
      }
      uiPrintPreview.setPrintMessage(message) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
    }
  }

  static public class MarkAsReadActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource() ;  
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class) ;
      String username = uiPortlet.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      String folderId = uiPortlet.findFirstComponentOfType(UIFolderContainer.class).getSelectedFolder();
      UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
      if(Utils.isEmptyField(accountId)) {
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.account-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      List<Message> checkedMsgs = uiMessageList.getCheckedMessage();
      if(checkedMsgs.isEmpty()) {
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.checkMessage-select-no-messages", null, ApplicationMessage.INFO)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      }
      MailService mailSrv = uiMessageList.getApplicationComponent(MailService.class);
      List<Message> msgList = new ArrayList<Message>();
      for (Message msg : checkedMsgs) {
        if (msg.isUnread()) {
          msgList.add(msg);
          msg.setUnread(false);
          uiMessageList.messageList_.put(msg.getId(), msg);
        }
      }
      try {
        if (Utils.isEmptyField(folderId)) folderId = "";
        mailSrv.toggleMessageProperty(username, accountId, msgList, folderId, Utils.EXO_ISUNREAD, false);
      } catch (PathNotFoundException e) {
        uiMessageList.setMessagePageList(null) ;
        uiPortlet.findFirstComponentOfType(UISelectAccount.class).refreshItems();
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
        
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.deleted_account", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
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
      String folderId = uiPortlet.findFirstComponentOfType(UIFolderContainer.class).getSelectedFolder();
      if (Utils.isEmptyField(folderId)) folderId = "";
      UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
      if(Utils.isEmptyField(accountId)) {
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.account-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      List<Message> appliedList = uiMessageList.getCheckedMessage();
      if(appliedList.isEmpty()) {
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.checkMessage-select-no-messages", null, ApplicationMessage.INFO)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      }
      MailService mailSrv = uiMessageList.getApplicationComponent(MailService.class);
      List<Message> msgList = new ArrayList<Message>();
      for (Message msg : appliedList) {
        if (!msg.isUnread()) {
          msgList.add(msg);
          msg.setUnread(true);
          uiMessageList.messageList_.put(msg.getId(), msg);
        }
      }
      try {
        mailSrv.toggleMessageProperty(username, accountId, msgList, folderId, Utils.EXO_ISUNREAD, true);
      } catch (PathNotFoundException e) {
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.checkMessage-select-no-messages", null, ApplicationMessage.INFO)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      }
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
      List<Tag> listTags = null ; 
      try {
        listTags = mailService.getTags(username, accId);
      } catch (PathNotFoundException e) {
        uiMessageList.setMessagePageList(null) ;
        uiPortlet.findFirstComponentOfType(UISelectAccount.class).refreshItems();
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
        
        UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.deleted_account", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
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
      tagList.add(mailSrv.getTag(username, accountId, tagId));
      mailSrv.addTag(username, accountId, uiMessageList.getCheckedMessage(), tagList);
      List<String> tagIdList = new ArrayList<String>() ;
      try {
      for (Message msg : uiMessageList.getCheckedMessage()) {
        if (msg.getTags() != null && msg.getTags().length > 0) {
          boolean add = true;
          for (int i=0 ; i < msg.getTags().length; i++) {
            if (tagId.equals(msg.getTags()[i])) add = false;   
            tagIdList.add(msg.getTags()[i]);
          }
          if (add) tagIdList.add(tagId) ;
        }
        if (tagIdList.size() == 0) tagIdList.add(tagId); 
        msg.setTags(tagIdList.toArray(new String[]{})) ;
        uiMessageList.messageList_.put(msg.getId(), msg) ;
      }
      } catch(Exception e) {
        uiMessageList.updateList();
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
      UIFolderContainer uiFolderContainer = uiPortlet.findFirstComponentOfType(UIFolderContainer.class) ;
      UIMessagePreview uiMsgPreview = uiPortlet.findFirstComponentOfType(UIMessagePreview.class);
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      List<Message> appliedMsgList = uiMessageList.getCheckedMessage();
      String fromFolderId = uiFolderContainer.getSelectedFolder() ;
      if (fromFolderId != null) {
        mailSrv.moveMessages(username, accountId, appliedMsgList, fromFolderId, folderId) ;
      } else {
        for (Message message : appliedMsgList) {
          mailSrv.moveMessage(username, accountId, message, message.getFolders()[0], folderId);
        }
      }       
      uiMessageList.updateList();     
      Message msgPreview = uiMsgPreview.getMessage();
      if (msgPreview != null && appliedMsgList.contains(msgPreview)) uiMsgPreview.setMessage(null);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiFolderContainer) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class));  
    }
  }

  static public class AddContactActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource() ;   
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      msgId = MailUtils.decodeMailId(msgId);
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class);
      Message msg = uiMessageList.messageList_.get(msgId);
      UIPopupAction uiPopup = uiPortlet.getChild(UIPopupAction.class);
      UIPopupActionContainer uiPopupContainer = uiPopup.createUIComponent(UIPopupActionContainer.class, null, null) ;
      uiPopupContainer.setId("UIPopupAddContactForm") ;
      uiPopup.activate(uiPopupContainer, 730, 0, true);

      UIAddContactForm uiAddContactForm = uiPopupContainer.createUIComponent(UIAddContactForm.class, null, null);
      uiPopupContainer.addChild(uiAddContactForm);
      InternetAddress[] addresses  = Utils.getInternetAddress(msg.getFrom());
      String personal = (addresses[0] != null) ? Utils.getPersonal(addresses[0]) : "";
      String firstName = personal;
      String email = (addresses[0] != null) ? addresses[0].getAddress() : "";
      String lastName = "";
      if (personal.indexOf(" ") > 0) {
        firstName = personal.substring(0, personal.indexOf(" "));
        lastName = personal.substring(personal.indexOf(" ") + 1, personal.length());
      }
      uiAddContactForm.setFirstNameField(firstName);
      uiAddContactForm.setLastNameField(lastName);
      uiAddContactForm.setEmailField(email);
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
        uiImportForm.setSelectedFolder(Utils.generateFID(accId, Utils.FD_INBOX, false));
      }
      uiPopup.activate(uiImportForm, 600, 0, true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup);    
    }
  }

  static public class ExportActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource() ;   
      //String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class);
      String accId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      UIApplication uiApp = uiMessageList.getAncestorOfType(UIApplication.class) ;
      if(Utils.isEmptyField(accId)) {
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.account-list-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      UIPopupAction uiPopup = uiPortlet.getChild(UIPopupAction.class);
      List<Message> checkedMsgs = uiMessageList.getCheckedMessage(false) ;
      if(checkedMsgs.isEmpty()) {
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.checkMessage-select-no-messages", null, ApplicationMessage.INFO)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      } else if (checkedMsgs.size() > 1){
        uiApp.addMessage(new ApplicationMessage("UIMessageList.msg.checkMessage-select-many-messages", null, ApplicationMessage.INFO)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      }
      try {
        Message msg = checkedMsgs.get(0) ;
        if (msg != null) {
          UIExportForm uiExportForm = uiPopup.activate(UIExportForm.class, 600);
          String username = uiPortlet.getCurrentUser() ;
          MailService mailSrv = uiPortlet.getApplicationComponent(MailService.class);
          msg = mailSrv.loadTotalMessage(username, accId, msg) ;  
          uiExportForm.setExportMessage(msg);
          event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup);
        }
      } catch (Exception e) {
        System.out.println("\n\n error when export");
      }
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
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class));
    }
  }

  static public class RefreshActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource() ; 
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class);
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      uiMessageList.init(accountId);
      uiPortlet.findFirstComponentOfType(UIFetchingBar.class).setIsShown(false);
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
        // TODO CS-3734
        if (uiMessageList.viewMode == UIMessageList.MODE_THREAD){
          msgFilter.setHasStructure(true) ;
        }
        else msgFilter.setHasStructure(false) ;
      }
      try {
        uiMessageList.setMessagePageList(mailSrv.getMessagePageList(username, msgFilter));
        uiMessageList.setMessageFilter(msgFilter);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList);
      } catch (Exception e) {
        return;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getParent());
    }
  }

  static public class ComfirmPasswordActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMessageList = event.getSource() ;    
      UIMailPortlet uiPortlet = uiMessageList.getAncestorOfType(UIMailPortlet.class);
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class);    
      UIComfirmPassword uiComfirmPassword = uiMessageList.createUIComponent(UIComfirmPassword.class,null, null);
      uiPopupAction.activate(uiComfirmPassword, 600, 0, true);             
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);   
    }
  }
  
  static public class UpdateListActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiMsgList = event.getSource() ;  
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      msgId = MailUtils.decodeMailId(msgId);
      UIMailPortlet uiPortlet = uiMsgList.getAncestorOfType(UIMailPortlet.class);
      MailService mailSrv = uiMsgList.getApplicationComponent(MailService.class);
      String username = uiPortlet.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      try {
        Message msg = mailSrv.getMessageById(username, accountId, msgId);        
        String newId = MailUtils.encodeMailId(msg.getId());
        UIFormCheckBoxInput<Boolean> uiCheckBox = new UIFormCheckBoxInput<Boolean>(newId, newId, false);
        uiMsgList.addUIFormInput(uiCheckBox);
        uiMsgList.messageList_.put(msg.getId(), msg);
      } catch(Exception e) { 
        // do nothing
      }
    }
  }
  
  static  public class AddAccountActionListener extends EventListener<UIMessageList> {
    public void execute(Event<UIMessageList> event) throws Exception {
      UIMessageList uiForm = event.getSource() ;
      UIMailPortlet uiPortlet = uiForm.getAncestorOfType(UIMailPortlet.class) ;
      UIPopupAction uiPopup = uiPortlet.getChild(UIPopupAction.class) ;
      UIPopupActionContainer uiAccContainer = uiPortlet.createUIComponent(UIPopupActionContainer.class, null, null) ;
      uiAccContainer.setId("UIAccountPopupCreation");
      uiAccContainer.addChild(UIAccountCreation.class, null, null) ;
      uiPopup.activate(uiAccContainer, 700, 0, true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
    }
  }
}