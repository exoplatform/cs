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
package org.exoplatform.mail.webui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.mail.internet.InternetAddress;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.impl.CalendarServiceImpl;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.cs.common.webui.UIPopupAction;
import org.exoplatform.cs.common.webui.UIPopupActionContainer;
import org.exoplatform.download.DownloadResource;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.mail.DataCache;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.Attachment;
import org.exoplatform.mail.service.JCRMessageAttachment;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.MessageFilter;
import org.exoplatform.mail.service.Tag;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.action.HasAccountEventListener;
import org.exoplatform.mail.webui.action.FullDelegationEventListener;
import org.exoplatform.mail.webui.action.OwnerEventListener;
import org.exoplatform.mail.webui.popup.UIAddMessageFilter;
import org.exoplatform.mail.webui.popup.UIComposeForm;
import org.exoplatform.mail.webui.popup.UIEventForm;
import org.exoplatform.mail.webui.popup.UIExportForm;
import org.exoplatform.mail.webui.popup.UIMoveMessageForm;
import org.exoplatform.mail.webui.popup.UIPrintPreview;
import org.exoplatform.mail.webui.popup.UITagForm;
import org.exoplatform.mail.webui.popup.UIViewAllHeaders;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.commons.UISaveAttachment;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL Author : Hung Nguyen
 * hung.nguyen@exoplatform.com Aus 01, 2007 2:48:18 PM
 */

@ComponentConfig(template = "app:/templates/mail/webui/UIMessagePreview.gtmpl", events = {
    @EventConfig(listeners = UIMessagePreview.DownloadAttachmentActionListener.class),
    @EventConfig(listeners = UIMessagePreview.AddStarActionListener.class),
    @EventConfig(listeners = UIMessagePreview.ReplyActionListener.class),
    @EventConfig(listeners = UIMessagePreview.ReplyAllActionListener.class),
    @EventConfig(listeners = UIMessagePreview.DeleteActionListener.class, confirm = "UIMessagePreview.msg.confirm-remove-message"),
    @EventConfig(listeners = UIMessagePreview.ForwardActionListener.class),
    @EventConfig(listeners = UIMessagePreview.CreateFilterActionListener.class),
    @EventConfig(listeners = UIMessagePreview.PrintActionListener.class),
    @EventConfig(listeners = UIMessagePreview.ExportActionListener.class),
    @EventConfig(listeners = UIMessagePreview.AddTagActionListener.class),
    @EventConfig(listeners = UIMessagePreview.AddContactActionListener.class),
    @EventConfig(listeners = UIMessagePreview.MoveMessagesActionListener.class),
    @EventConfig(listeners = UIMessagePreview.AnswerInvitationActionListener.class),
    @EventConfig(listeners = UIMessagePreview.ViewAllHeadersActionListener.class),
    @EventConfig(listeners = UIMessagePreview.BackToListActionListener.class),
    @EventConfig(listeners = UIMessagePreview.SaveAttachmentToDMSActionListener.class),
    @EventConfig(listeners = UIMessagePreview.HideMessageListActionListener.class) })
public class UIMessagePreview extends UIContainer{
  private static final Log log = ExoLogger.getExoLogger(UIMessagePreview.class);
  
  public static String  QUESTION           = "question".intern();

  public static String  ANSWER_IMPORT      = "yes-import".intern();

  public static String  ANSWER_YES         = "yes".intern();

  public static String  ANSWER_NO          = "no".intern();

  public static String  ANSWER_MAYBE       = "maybe".intern();

  private Message       selectedMessage_;

  private List<Message> showedMsgs         = new ArrayList<Message>();

  private boolean       isHideMessageList_ = false;

  private List<String>  unreadMsgIds       = new ArrayList<String>();

  private Attachment    selectedAttachment_;

  public UIMessagePreview() throws Exception {
  }

  public Message getMessage() throws Exception {
    return selectedMessage_;
  }

  public void setMessage(Message msg) throws Exception {
    selectedMessage_ = msg;
  }

  public List<Message> getShowedMessages() throws Exception {
    return showedMsgs;
  }

  public void setShowedMessages(List<Message> msgList) throws Exception {
    showedMsgs = msgList;
  }

  public List<String> getUnreadMessages() throws Exception {
    return unreadMsgIds;
  }

  public void setUnreadMessages(List<String> unreadMsgIds) throws Exception {
    this.unreadMsgIds = unreadMsgIds;
  }

  /**
   * @return the selectedAttachment_
   */
  public Attachment getSelectedAttachment_() {
    return selectedAttachment_;
  }

  /**
   * @param selectedAttachment the selectedAttachment_ to set
   */
  public void setSelectedAttachment_(Attachment selectedAttachment) {
    selectedAttachment_ = selectedAttachment;
  }

  public Map<String, String> getImageLocationMap(Message message) throws Exception {
    Map<String, String> imageLocation = new HashMap<String, String>();
    DownloadService dservice = getDownloadService();
    String attLink = "", attId = "";
    if (message.getAttachments() != null) {
      for (Attachment att : message.getAttachments()) {
        if (att.isShownInBody()) {
          attLink = MailUtils.getImageSource(att, dservice);
          if (attLink != null) {
            attLink = MailUtils.getAttachmentLink(att);
            attId = att.getId();
            imageLocation.put(attId.substring(attId.lastIndexOf("/") + 1, attId.length()),
                              attLink.substring(0, attLink.lastIndexOf("/") + 1));
          }
        }
      }
    }
    return imageLocation;
  }

  public void setIsHideMessageList(boolean b) {
    isHideMessageList_ = b;
  }

  public boolean isHideMessageList() {
    return isHideMessageList_;
  }

  public boolean isShowBcc(Message msg) throws Exception {
    UIMailPortlet uiPortlet = getAncestorOfType(UIMailPortlet.class);
    DataCache dataCache = uiPortlet.getDataCache();
    
    String selectedFolder = uiPortlet.findFirstComponentOfType(UIFolderContainer.class).getSelectedFolder();
    String accId = dataCache.getSelectedAccountId();
    String username = MailUtils.getCurrentUser();
    Account account = dataCache.getAccountById(username, accId);
    InternetAddress[] fromAddress = Utils.getInternetAddress(msg.getFrom());
    InternetAddress from = fromAddress[0];
    
    return (!MailUtils.isFieldEmpty(selectedFolder)
        && selectedFolder.equals(Utils.generateFID(accId, Utils.FD_SENT, false))
        && !MailUtils.isFieldEmpty(msg.getMessageBcc()) && (account != null) && from.getAddress()
        .equalsIgnoreCase(account.getEmailAddress()));
  }

  public CalendarEvent getEvent(Message msg) throws Exception {
    CalendarService calendarSrv = getApplicationComponent(CalendarService.class);
    CalendarEvent calEvent = null;
    try {
      if (Calendar.TYPE_PRIVATE == Integer.parseInt(MailUtils.getEventType(msg))) {
        List<String> calIds = new ArrayList<String>();
        calIds.add(MailUtils.getCalendarId(msg));
        Iterator<CalendarEvent> iter = calendarSrv.getUserEventByCalendar(MailUtils.getEventFrom(msg),
                                                                          calIds)
                                                                          .iterator();
        while (iter.hasNext()) {
          calEvent = iter.next();
          if (MailUtils.getCalendarEventId(msg).equals(calEvent.getId()))
            break;
        }
        if (!MailUtils.getCalendarEventId(msg).equals(calEvent.getId()))
          calEvent = null;
      } else if (Calendar.TYPE_SHARED == Integer.parseInt(MailUtils.getEventType(msg))) {
        // calendarSrv.get
      } else if (Calendar.TYPE_PUBLIC == Integer.parseInt(MailUtils.getEventType(msg))) {
        calEvent = calendarSrv.getGroupEvent(MailUtils.getCalendarId(msg),
                                             MailUtils.getCalendarEventId(msg));
      }
    } catch (Exception e) {
      calEvent = null;
    }
    return calEvent;
  }

  public Message getShowedMessageById(String id) throws Exception {
    for (Message msg : getShowedMessages()) {
      if (msg.getId().equals(id))
        return msg;
    }
    return null;
  }

  public DownloadService getDownloadService() {
    return getApplicationComponent(DownloadService.class);
  }

  public String getPortalName() {
    PortalContainer pcontainer = PortalContainer.getInstance();
    return pcontainer.getPortalContainerInfo().getContainerName();
  }

  public String getRepository() throws Exception {
    RepositoryService rService = getApplicationComponent(RepositoryService.class);
    return rService.getCurrentRepository().getConfiguration().getName();
  }

  public String getAnswerStatus() throws Exception {
    CalendarEvent calEvent = getEvent(getMessage());
    if (calEvent == null) {
      return null;
    }
    
    String[] parStatus = calEvent.getParticipantStatus();
    UIMailPortlet uiPortlet = this.getAncestorOfType(UIMailPortlet.class);
    DataCache dataCache = uiPortlet.getDataCache();
    String username = uiPortlet.getCurrentUser();
    String accId = dataCache.getSelectedAccountId();
    Account account = dataCache.getAccountById(username, accId);
    String currentEmail = "";
    if (account != null) {
      currentEmail = account.getEmailAddress();
    }

    for (String par : parStatus) {
      String[] entry = par.split(":");
      if (entry[0].equalsIgnoreCase(username) || entry[0].equalsIgnoreCase(currentEmail)) {
        if (entry.length > 1) {
          return entry[1];
        } else {
          return new String("");
        }
      }
    }
    return null;
  }

  public static class SaveAttachmentToDMSActionListener extends EventListener<UIMessagePreview> {
    public void execute(Event<UIMessagePreview> event) throws Exception {
      UIMessagePreview uiMsgPreview = event.getSource();
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID);
      msgId = Utils.decodeMailId(msgId);
      String attId = event.getRequestContext().getRequestParameter("attachId");
      attId = MailUtils.decodeURL(attId);
      UIMailPortlet portlet = uiMsgPreview.getAncestorOfType(UIMailPortlet.class);

      Message msg = uiMsgPreview.getShowedMessageById(msgId);
      if (msg != null) {
        List<Attachment> attList = msg.getAttachments();
        JCRMessageAttachment att = null;
        for (Attachment attach : attList) {
          if (attach.getId().equals(attId)) {
            att = (JCRMessageAttachment) attach;
          }
        }

        if (att != null) {
          uiMsgPreview.setSelectedAttachment_(att);
          UIPopupAction popupAction = portlet.getChild(UIPopupAction.class);
          UIPopupWindow popupChildWindow = popupAction.getChild(UIPopupWindow.class);
          UISaveAttachment uiSaveAttach = popupChildWindow.createUIComponent(UISaveAttachment.class,
                                                                            null,
                                                                            null);
          popupChildWindow.setUIComponent(uiSaveAttach);
          uiSaveAttach.setFilePath(att.getPath());
          uiSaveAttach.setFileName(att.getName());
          popupChildWindow.setWindowSize(600, 0);
          popupChildWindow.setRendered(true);
          popupChildWindow.setShow(true);

          popupChildWindow.setResizable(true);
          event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
        }

      }
    }
  }

  public static class DownloadAttachmentActionListener extends EventListener<UIMessagePreview> {
    public void execute(Event<UIMessagePreview> event) throws Exception {
      UIMessagePreview uiMsgPreview = event.getSource();
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID);
      msgId = Utils.decodeMailId(msgId);
      String attId = event.getRequestContext().getRequestParameter("attachId");
      attId = MailUtils.decodeURL(attId);
      UIMailPortlet uiPortlet = uiMsgPreview.getAncestorOfType(UIMailPortlet.class);
      Message msg = uiMsgPreview.getShowedMessageById(msgId);
      if (msg != null) {
        List<Attachment> attList = msg.getAttachments();
        JCRMessageAttachment att = new JCRMessageAttachment();
        for (Attachment attach : attList) {
          if (attach.getId().equals(attId)) {
            att = (JCRMessageAttachment) attach;
          }
        }
        DownloadResource dresource = new InputStreamDownloadResource(att.getInputStream(),
                                                                     att.getMimeType());
        DownloadService dservice = (DownloadService) PortalContainer.getInstance()
        .getComponentInstanceOfType(DownloadService.class);
        dresource.setDownloadName(att.getName());
        String downloadLink = dservice.getDownloadLink(dservice.addDownloadResource(dresource));
        event.getRequestContext().getJavascriptManager().addJavascript("ajaxRedirect('"
                                                                       + downloadLink + "');");
        uiPortlet.cancelAction();
      }
    }
  }

  static public class AddStarActionListener extends EventListener<UIMessagePreview> {
    public void execute(Event<UIMessagePreview> event) throws Exception {
      UIMessagePreview uiMsgPreview = event.getSource();
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID);
      msgId = Utils.decodeMailId(msgId);
      UIMailPortlet uiPortlet = uiMsgPreview.getAncestorOfType(UIMailPortlet.class);
      UIMessageArea uiMsgArea = uiPortlet.findFirstComponentOfType(UIMessageArea.class);
      UIMessageList uiMessageList = uiMsgArea.getChild(UIMessageList.class);
      String username = uiPortlet.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class)
      .getSelectedValue();
      MailService mailServ = uiPortlet.getApplicationComponent(MailService.class);
      Message msg = uiMsgPreview.getShowedMessageById(msgId);
      if (msg != null) {
        List<Message> msgList = new ArrayList<Message>();
        msg.setHasStar(!msg.hasStar());
        msgList.add(msg);
        mailServ.toggleMessageProperty(username,
                                       accountId,
                                       msgList,
                                       msg.getFolders()[0],
                                       Utils.EXO_STAR,
                                       !msg.hasStar());
        uiMessageList.messageList_.put(msgId, msg);
        uiMsgPreview.setMessage(msg);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMsgArea);
    }
  }

  static public class ReplyActionListener extends FullDelegationEventListener<UIMessagePreview> {
    @Override
    public void processEvent(Event<UIMessagePreview> event) throws Exception {
      UIMessagePreview.reply(event, false);
    }
  }

  static public class ReplyAllActionListener extends FullDelegationEventListener<UIMessagePreview> {
    @Override
    public void processEvent(Event<UIMessagePreview> event) throws Exception {
      UIMessagePreview.reply(event, true);
    }
  }

  static public class ForwardActionListener extends FullDelegationEventListener<UIMessagePreview> {
    @Override
    public void processEvent(Event<UIMessagePreview> event) throws Exception {
      UIMessagePreview uiMsgPreview = event.getSource();
      UIMailPortlet uiPortlet = uiMsgPreview.getAncestorOfType(UIMailPortlet.class);
      DataCache dataCache = uiPortlet.getDataCache();
      
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID);
      msgId = Utils.decodeMailId(msgId);      
      String accId = dataCache.getSelectedAccountId();
      if (msgId != null) {
        Message msg = uiMsgPreview.getShowedMessageById(msgId);
        if (msg != null) {
          UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class);
          UIPopupActionContainer uiPopupContainer = uiPopupAction.createUIComponent(UIPopupActionContainer.class,
                                                                                    null,
          "UIPopupActionComposeContainer");
          uiPopupAction.activate(uiPopupContainer, MailUtils.MAX_POPUP_WIDTH, 0, true);
          UIComposeForm uiComposeForm = uiPopupContainer.createUIComponent(UIComposeForm.class,
                                                                           null,
                                                                           null);
          uiComposeForm.init(accId, msg, uiComposeForm.MESSAGE_FOWARD);
          uiPopupContainer.addChild(uiComposeForm);
          event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
        }
      }
    }
  }

  static public class CreateFilterActionListener extends OwnerEventListener<UIMessagePreview> {
    @Override
    public void processEvent(Event<UIMessagePreview> event) throws Exception {
      UIMessagePreview uiMsgPreview = event.getSource();
      UIMailPortlet uiPortlet = uiMsgPreview.getAncestorOfType(UIMailPortlet.class);
      DataCache dataCache = uiPortlet.getDataCache();
      
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID);
      msgId = Utils.decodeMailId(msgId);
      String accId = dataCache.getSelectedAccountId();
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class);
      if (msgId != null) {
        Message msg = uiMsgPreview.getShowedMessageById(msgId);
        if (msg != null) {
          UIPopupActionContainer uiPopupContainer = uiPopupAction.createUIComponent(UIPopupActionContainer.class,
                                                                                    null,
          "UIPopupActionComposeContainer");
          uiPopupAction.activate(uiPopupContainer, MailUtils.MAX_POPUP_WIDTH, 0, true);
          UIComposeForm uiComposeForm = uiPopupContainer.createUIComponent(UIComposeForm.class,
                                                                           null,
                                                                           null);
          uiComposeForm.init(accId, msg, uiComposeForm.MESSAGE_FOWARD);
          uiPopupContainer.addChild(uiComposeForm);
          event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
        }
        String from = Utils.getAddresses(msg.getFrom())[0];
        MessageFilter filter = new MessageFilter(from);
        filter.setFrom(from);
        filter.setFromCondition(Utils.CONDITION_CONTAIN);
        UIAddMessageFilter uiEditMessageFilter = uiPopupAction.createUIComponent(UIAddMessageFilter.class, null, null);
        uiPopupAction.activate(uiEditMessageFilter, 650, 0, false);
        String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
        uiEditMessageFilter.init(accountId);
        uiEditMessageFilter.setCurrentFilter(filter);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
    }
  }

  static public class DeleteActionListener extends FullDelegationEventListener<UIMessagePreview> {
    @Override
    public void processEvent(Event<UIMessagePreview> event) throws Exception {
      UIMessagePreview uiMsgPreview = event.getSource();
      UIMailPortlet uiPortlet = uiMsgPreview.getAncestorOfType(UIMailPortlet.class);
      DataCache dataCache = uiPortlet.getDataCache();
      
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID);
      msgId = Utils.decodeMailId(msgId);
      String accountId = dataCache.getSelectedAccountId();
      UIMessageArea uiMsgArea = uiPortlet.findFirstComponentOfType(UIMessageArea.class);
      UIMessageList uiMsgList = uiMsgArea.getChild(UIMessageList.class);
      UIFolderContainer uiFolderCon = uiPortlet.findFirstComponentOfType(UIFolderContainer.class);
      Message msg = uiMsgPreview.getShowedMessageById(msgId);
      MailService mailSrv = uiMsgPreview.getApplicationComponent(MailService.class);
      String username = MailUtils.getCurrentUser();
      Message mResult = null;
      if (msg != null) {
        String selectedFolderId = uiFolderCon.getSelectedFolder();
        if ((selectedFolderId != null) && selectedFolderId.equals(Utils.generateFID(accountId, Utils.FD_TRASH, false))) {
          mailSrv.removeMessage(username, accountId, msg);
        } else {
          mResult = mailSrv.moveMessage(username, accountId, msg, msg.getFolders()[0], Utils.generateFID(accountId, Utils.FD_TRASH, false));
        }
        uiMsgList.updateList();
        
        List<Message> showedMsgList = uiMsgPreview.getShowedMessages();
        if (showedMsgList != null && showedMsgList.size() > 1) {
          showedMsgList.remove(msg);
        } else {
          uiMsgPreview.setMessage(null);
          uiMsgPreview.setShowedMessages(null);
        }
      }
      
      if(mResult == null){
        event.getRequestContext()
             .getUIApplication()
             .addMessage(new ApplicationMessage("UIMoveMessageForm.msg.move_delete_not_successful", null, ApplicationMessage.INFO));
      }
      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiFolderCon.getParent());
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMsgArea);
    }
  }

  static public class PrintActionListener extends EventListener<UIMessagePreview> {
    public void execute(Event<UIMessagePreview> event) throws Exception {
      UIMessagePreview uiMsgPreview = event.getSource();
      UIMailPortlet uiPortlet = uiMsgPreview.getAncestorOfType(UIMailPortlet.class);
      DataCache dataCache = uiPortlet.getDataCache();
      
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID);
      msgId = Utils.decodeMailId(msgId);
      Message msg = uiMsgPreview.getShowedMessageById(msgId);
      UIPopupAction uiPopup = uiPortlet.getChild(UIPopupAction.class);
      UIPrintPreview uiPrintPreview = uiPopup.activate(UIPrintPreview.class, 700);
      String accountId = dataCache.getSelectedAccountId();
      
      String username = MailUtils.getDelegateFrom(accountId, dataCache);
      
      Account acc = dataCache.getAccountById(username, accountId);
      if (acc != null) {
        uiPrintPreview.setAcc(acc);
        uiPrintPreview.setPrintMessage(msg);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup);
    }
  }

  static public class AddContactActionListener extends EventListener<UIMessagePreview> {
    public void execute(Event<UIMessagePreview> event) throws Exception {
      UIMessagePreview uiMsgPreview = event.getSource();
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID);
      msgId = Utils.decodeMailId(msgId);
      UIMailPortlet uiPortlet = uiMsgPreview.getAncestorOfType(UIMailPortlet.class);

      Message msg = uiMsgPreview.getShowedMessageById(msgId);
      if (msg != null) {
        UIPopupAction uiPopup = uiPortlet.getChild(UIPopupAction.class);
        MailUtils.createContactForm(event, uiPopup, msg, "UIPopupActionAddContactContainer");
      }
    }
  }

  static public class ExportActionListener extends EventListener<UIMessagePreview> {
    public void execute(Event<UIMessagePreview> event) throws Exception {
      UIMessagePreview uiMsgPreview = event.getSource();
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID);
      msgId = Utils.decodeMailId(msgId);
      UIMailPortlet uiPortlet = uiMsgPreview.getAncestorOfType(UIMailPortlet.class);
      try {
        Message msg = uiMsgPreview.getShowedMessageById(msgId);
        if (msg != null) {
          UIPopupAction uiPopup = uiPortlet.getChild(UIPopupAction.class);
          UIExportForm uiExportForm = uiPopup.createUIComponent(UIExportForm.class, null, null);
          uiPopup.activate(uiExportForm, 600, 0, true);
          uiExportForm.setExportMessage(msg);
          event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup);
        }
      } catch (Exception e) {
      }
    }
  }

  static public class AddTagActionListener extends EventListener<UIMessagePreview> {
    public void execute(Event<UIMessagePreview> event) throws Exception {
      UIMessagePreview uiMsgPreview = event.getSource();
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID);
      msgId = Utils.decodeMailId(msgId);
      UIMailPortlet uiPortlet = uiMsgPreview.getAncestorOfType(UIMailPortlet.class);
      Message msg = uiMsgPreview.getShowedMessageById(msgId);
      if (msg != null) {
        UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class);
        UITagForm uiTagForm = uiMsgPreview.createUIComponent(UITagForm.class, null, null);
        String username = uiPortlet.getCurrentUser();
        String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class)
        .getSelectedValue();
        MailService mailSrv = MailUtils.getMailService();
        List<Tag> listTags = mailSrv.getTags(username, accountId);
        uiPopupAction.activate(uiTagForm, 600, 0, true);
        List<Message> msgList = new ArrayList<Message>();
        msgList.add(msg);
        uiTagForm.setMessageList(msgList);
        uiTagForm.setTagList(listTags);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
      }
    }
  }

  static public class MoveMessagesActionListener extends OwnerEventListener<UIMessagePreview> {
    @Override
    public void processEvent(Event<UIMessagePreview> event) throws Exception {
      UIMessagePreview uiMsgPreview = event.getSource();
      UIMailPortlet uiPortlet = uiMsgPreview.getAncestorOfType(UIMailPortlet.class);
      DataCache dataCache = uiPortlet.getDataCache();
      
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID);
      msgId = Utils.decodeMailId(msgId);      
      String accountId = dataCache.getSelectedAccountId();      
      Message msg = uiMsgPreview.getShowedMessageById(msgId);
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class);
      if (msg != null) {
        UIMoveMessageForm uiMoveMessageForm = uiMsgPreview.createUIComponent(UIMoveMessageForm.class, null, null);
        uiPopupAction.activate(uiMoveMessageForm, 600, 0, true);
        uiMoveMessageForm.init(accountId);
        List<Message> msgList = new ArrayList<Message>();
        msgList.add(msg);
        uiMoveMessageForm.setMessageList(msgList);
      }
      uiMsgPreview.setMessage(null);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
    }
  }

  static public class AnswerInvitationActionListener extends HasAccountEventListener<UIMessagePreview> {
    public void execute(Event<UIMessagePreview> event) throws Exception {
      UIMessagePreview uiMsgPreview = event.getSource();
      UIMailPortlet uiMailPortlet = uiMsgPreview.getAncestorOfType(UIMailPortlet.class);
      DataCache dataCache = uiMailPortlet.getDataCache();
      
      String answer = event.getRequestContext().getRequestParameter(OBJECTID);
      String msgId = event.getRequestContext().getRequestParameter("messageId");
      msgId = Utils.decodeMailId(msgId);
      CalendarService calService = uiMsgPreview.getApplicationComponent(CalendarService.class);
      Message msg = uiMsgPreview.getShowedMessageById(msgId);
      String fromUserId = MailUtils.getEventFrom(msg);
      String accId = dataCache.getSelectedAccountId();
      String confirmingUser = uiMailPortlet.getCurrentUser();
      Account account = dataCache.getAccountById(confirmingUser, accId);
      String confirmingEmail = "";
      if (account != null) {
        confirmingEmail = account.getEmailAddress();
      }
      int calType = Integer.parseInt(MailUtils.getEventType(msg));
      String calendarId = MailUtils.getCalendarId(msg);
      String eventId = MailUtils.getCalendarEventId(msg);
      try {
        if (Integer.parseInt(answer) == 3) {
          List<Attachment> attList = msg.getAttachments();
          List<org.exoplatform.calendar.service.Attachment> attachment = new ArrayList<org.exoplatform.calendar.service.Attachment>();
          List<CalendarEvent> eventList = new ArrayList<CalendarEvent>();
          for (Attachment att : attList) {
            if (att.getMimeType() != null && att.getMimeType().equalsIgnoreCase("TEXT/CALENDAR")) {
              eventList.addAll(calService.getCalendarImportExports(CalendarServiceImpl.ICALENDAR).getEventObjects(
                  att.getInputStream()));
            } else {
              org.exoplatform.calendar.service.Attachment a = new org.exoplatform.calendar.service.Attachment();
              a.setId(att.getId());
              a.setInputStream(att.getInputStream());
              a.setMimeType(att.getMimeType());
              a.setName(att.getName());
              a.setSize(att.getSize());
              attachment.add(a);
            }
          }
          CalendarEvent calEvent = null;
          for (CalendarEvent calEv : eventList) {
            if (eventId.equals(calEv.getId())) {
              calEvent = calEv;
              break;
            }
          }
          if (calEvent != null) {
            calEvent.setAttachment(attachment);
            UIMailPortlet uiPortlet = uiMsgPreview.getAncestorOfType(UIMailPortlet.class);
            UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class);
            UIPopupActionContainer uiPopupContainer = uiPopupAction.createUIComponent(UIPopupActionContainer.class, null,
                "UIPopupActionEventContainer");
            uiPopupAction.activate(uiPopupContainer, 600, 0, true);
            UIEventForm uiEventForm = uiPopupContainer.createUIComponent(UIEventForm.class, null, null);
            uiPopupContainer.addChild(uiEventForm);
            uiEventForm.initForm(calService.getCalendarSetting(MailUtils.getCurrentUser()), calEvent);
            uiEventForm.isAddNew_ = true;
            uiEventForm.update(CalendarUtils.PRIVATE_TYPE, null);
            event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
            calService.confirmInvitation(fromUserId, confirmingEmail, confirmingUser, calType, calendarId, eventId, 1);
          }
        } else {
          calService.confirmInvitation(fromUserId, confirmingEmail, confirmingUser, calType, calendarId, eventId,
              Integer.parseInt(answer));
        }
      } catch (Exception e) {
        if (log.isDebugEnabled()) {
          log.debug("Exception in method execute of class AnswerInvitationActionListener", e);
        }
        event.getRequestContext()
             .getUIApplication()
             .addMessage(new ApplicationMessage("UIMessagePreview.msg.trouble-loading-event", null));        
        return;
      }
    }
  }

  static public class ViewAllHeadersActionListener extends EventListener<UIMessagePreview> {
    public void execute(Event<UIMessagePreview> event) throws Exception {
      UIMessagePreview uiMsgPreview = event.getSource();
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID);
      msgId = Utils.decodeMailId(msgId);
      UIMailPortlet uiPortlet = uiMsgPreview.getAncestorOfType(UIMailPortlet.class);
      UIPopupAction uiPopup = uiPortlet.getChild(UIPopupAction.class);
      Message msg = uiMsgPreview.getShowedMessageById(msgId);
      UIViewAllHeaders uiAllHeader = uiPopup.createUIComponent(UIViewAllHeaders.class, null, null);
      uiAllHeader.init(msg);
      uiPopup.activate(uiAllHeader, 700, 0, true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup);
    }
  }

  static public class BackToListActionListener extends EventListener<UIMessagePreview> {
    public void execute(Event<UIMessagePreview> event) throws Exception {
      UIMessagePreview uiMsgPreview = event.getSource();
      uiMsgPreview.setMessage(null);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMsgPreview.getParent());
    }
  }

  static public class HideMessageListActionListener extends EventListener<UIMessagePreview> {
    public void execute(Event<UIMessagePreview> event) throws Exception {
      UIMessagePreview uiMsgPreview = event.getSource();
      uiMsgPreview.setIsHideMessageList(!uiMsgPreview.isHideMessageList());
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMsgPreview.getParent());
    }
  }

  public boolean isShowPicInBody(Message msg){
    List<Attachment> atts = msg.getAttachments();
    if(atts != null && atts.size()>0){
      for (Attachment attach : atts) {
        if(!attach.isShownInBody()) return false; 
      }  
    }
    return true;
  }
  
  static void reply(Event<UIMessagePreview> event, boolean isReplyAll) throws Exception{
    UIMessagePreview uiMsgPreview = event.getSource();
    UIMailPortlet uiPortlet = uiMsgPreview.getAncestorOfType(UIMailPortlet.class);
    DataCache dataCache = uiPortlet.getDataCache();
    
    String msgId = event.getRequestContext().getRequestParameter(OBJECTID);
    msgId = Utils.decodeMailId(msgId);
    
    String accId = dataCache.getSelectedAccountId();    
    UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class);
    if (msgId != null) {
      Message msg = uiMsgPreview.getShowedMessageById(msgId);
      if (msg != null) {
        UIPopupActionContainer uiPopupContainer = uiPopupAction.createUIComponent(UIPopupActionContainer.class, null, "UIPopupActionComposeContainer");
        uiPopupAction.activate(uiPopupContainer, MailUtils.MAX_POPUP_WIDTH, 0, true);
        UIComposeForm uiComposeForm = uiPopupContainer.createUIComponent(UIComposeForm.class, null, null);
        if(!isReplyAll) {
          uiComposeForm.init(accId, msg, uiComposeForm.MESSAGE_REPLY);
        } else {
          uiComposeForm.init(accId, msg, uiComposeForm.MESSAGE_REPLY_ALL);
        }
        uiPopupContainer.addChild(uiComposeForm);
      }
    }
    event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
  }
}
