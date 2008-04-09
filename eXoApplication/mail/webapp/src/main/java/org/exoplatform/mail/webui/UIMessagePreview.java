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
import java.util.Iterator;
import java.util.List;

import javax.mail.internet.InternetAddress;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadResource;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.SessionsUtils;
import org.exoplatform.mail.service.Attachment;
import org.exoplatform.mail.service.JCRMessageAttachment;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.Tag;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.popup.UIAddContactForm;
import org.exoplatform.mail.webui.popup.UIComposeForm;
import org.exoplatform.mail.webui.popup.UIExportForm;
import org.exoplatform.mail.webui.popup.UIMoveMessageForm;
import org.exoplatform.mail.webui.popup.UIPopupAction;
import org.exoplatform.mail.webui.popup.UIPopupActionContainer;
import org.exoplatform.mail.webui.popup.UIPrintPreview;
import org.exoplatform.mail.webui.popup.UITagForm;
import org.exoplatform.mail.webui.popup.UIViewAllHeaders;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
    template =  "app:/templates/mail/webui/UIMessagePreview.gtmpl",
    events = {
        @EventConfig(listeners = UIMessagePreview.DownloadAttachmentActionListener.class),
        @EventConfig(listeners = UIMessagePreview.AddStarActionListener.class),
        @EventConfig(listeners = UIMessagePreview.ReplyActionListener.class),
        @EventConfig(listeners = UIMessagePreview.ReplyAllActionListener.class),
        @EventConfig(listeners = UIMessagePreview.DeleteActionListener.class),
        @EventConfig(listeners = UIMessagePreview.ForwardActionListener.class), 
        @EventConfig(listeners = UIMessagePreview.PrintActionListener.class),
        @EventConfig(listeners = UIMessagePreview.ExportActionListener.class),
        @EventConfig(listeners = UIMessagePreview.AddTagActionListener.class),
        @EventConfig(listeners = UIMessagePreview.AddContactActionListener.class),
        @EventConfig(listeners = UIMessagePreview.MoveMessagesActionListener.class),
        @EventConfig(listeners = UIMessagePreview.AnswerInvitationActionListener.class),
        @EventConfig(listeners = UIMessagePreview.ViewAllHeadersActionListener.class)
    }
)

public class UIMessagePreview extends UIComponent {
  private Message selectedMessage_ ;
  private List<Message> showedMsgs = new ArrayList<Message>() ;

  public UIMessagePreview() throws Exception {}

  public Message getMessage() throws Exception { 
    return selectedMessage_ ;
  }

  public void setMessage(Message msg) throws Exception {
    selectedMessage_ = msg ;
  }

  public List<Message> getShowedMessages() throws Exception {
    return showedMsgs ;
  } 

  public void setShowedMessages(List<Message> msgList) throws Exception {
    showedMsgs = msgList ;
  }

  public CalendarEvent getEvent(Message msg) throws Exception {
    CalendarService calendarSrv = getApplicationComponent(CalendarService.class) ;
    CalendarEvent calEvent = null ;
    if(Calendar.TYPE_PRIVATE == Integer.parseInt(MailUtils.getEventType(msg)) ) {
      List<String> calIds = new ArrayList<String>() ;
      calIds.add(MailUtils.getCalendarId(msg)) ;
      Iterator<CalendarEvent> iter =
        calendarSrv.getUserEventByCalendar(SessionProviderFactory.createSessionProvider(), MailUtils.getEventFrom(msg), calIds).iterator() ;
      while (iter.hasNext()) {
        calEvent = iter.next() ;
        if(MailUtils.getCalendarEventId(msg).equals(calEvent.getId())) ;
        break ;
      }
    } else if(Calendar.TYPE_SHARED == Integer.parseInt(MailUtils.getEventType(msg))) {
      //calendarSrv.get
    }
    else if(Calendar.TYPE_PUBLIC == Integer.parseInt(MailUtils.getEventType(msg))) {
      calEvent = calendarSrv.getGroupEvent(SessionProviderFactory.createSystemProvider(), MailUtils.getCalendarId(msg), MailUtils.getCalendarEventId(msg)) ; 
    }
    return calEvent ;
  }

  public Message getShowedMessageById(String id) throws Exception {
    for (Message msg : getShowedMessages()) {
      if (msg.getId().equals(id)) return msg ;
    }
    return null ;
  }

  public DownloadService getDownloadService() { 
    return getApplicationComponent(DownloadService.class) ; 
  }

  public static class DownloadAttachmentActionListener extends EventListener<UIMessagePreview> {
    public void execute(Event<UIMessagePreview> event) throws Exception {
      UIMessagePreview uiMsgPreview = event.getSource();
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID);
      String attId = event.getRequestContext().getRequestParameter("attachId");
      UIMailPortlet uiPortlet = uiMsgPreview.getAncestorOfType(UIMailPortlet.class);
      Message msg = uiMsgPreview.getShowedMessageById(msgId) ;
      if (msg != null) {
        List<Attachment> attList = msg.getAttachments();
        JCRMessageAttachment att = new JCRMessageAttachment();
        for (Attachment attach : attList) {
          if (attach.getId().equals(attId)) {
            att = (JCRMessageAttachment)attach;
          }
        }
        DownloadResource dresource = new InputStreamDownloadResource(att.getInputStream(), att.getMimeType());
        DownloadService dservice = (DownloadService)PortalContainer.getInstance().getComponentInstanceOfType(DownloadService.class);
        dresource.setDownloadName(att.getName());
        String downloadLink = dservice.getDownloadLink(dservice.addDownloadResource(dresource));
        event.getRequestContext().getJavascriptManager().addJavascript("ajaxRedirect('" + downloadLink + "');");
        uiPortlet.cancelAction() ;
      }
    }
  }

  static public class AddStarActionListener extends EventListener<UIMessagePreview> {
    public void execute(Event<UIMessagePreview> event) throws Exception { 
      UIMessagePreview uiMsgPreview = event.getSource();
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIMailPortlet uiPortlet = uiMsgPreview.getAncestorOfType(UIMailPortlet.class);
      UIMessageArea uiMsgArea = uiPortlet.findFirstComponentOfType(UIMessageArea.class) ;
      UIMessageList uiMessageList = uiMsgArea.getChild(UIMessageList.class) ;
      String username = uiPortlet.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      MailService mailServ = uiPortlet.getApplicationComponent(MailService.class);
      Message msg = uiMsgPreview.getShowedMessageById(msgId) ;
      if (msg != null) {
        List<Message> msgList = new ArrayList<Message>() ;
        msg.setHasStar(!msg.hasStar());
        msgList.add(msg) ;
        mailServ.toggleMessageProperty(SessionsUtils.getSessionProvider(), username, accountId, msgList, Utils.EXO_STAR);
        uiMessageList.messageList_.put(msgId, msg);
        uiMsgPreview.setMessage(msg);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMsgArea);
    }
  }

  static public class ReplyActionListener extends EventListener<UIMessagePreview> {
    public void execute(Event<UIMessagePreview> event) throws Exception {
      UIMessagePreview uiMsgPreview = event.getSource() ; 
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIMailPortlet uiPortlet = uiMsgPreview.getAncestorOfType(UIMailPortlet.class) ;
      String accId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();

      if (msgId != null) {
        Message msg = uiMsgPreview.getShowedMessageById(msgId);
        if (msg != null) {
          UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
          UIPopupActionContainer uiPopupContainer = uiPopupAction.activate(UIPopupActionContainer.class, 850) ;
          UIComposeForm uiComposeForm = uiPopupContainer.createUIComponent(UIComposeForm.class, null, null);
          uiComposeForm.init(accId, msg, uiComposeForm.MESSAGE_REPLY);
          uiPopupContainer.addChild(uiComposeForm) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
        }
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMsgPreview) ;
    }
  }

  static  public class ReplyAllActionListener extends EventListener<UIMessagePreview> {    
    public void execute(Event<UIMessagePreview> event) throws Exception {
      UIMessagePreview uiMsgPreview = event.getSource() ; 
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIMailPortlet uiPortlet = uiMsgPreview.getAncestorOfType(UIMailPortlet.class) ;
      String accId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();

      if (msgId != null) {
        Message msg = uiMsgPreview.getShowedMessageById(msgId);
        if (msg != null) {
          UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
          UIPopupActionContainer uiPopupContainer = uiPopupAction.activate(UIPopupActionContainer.class, 850) ;
          UIComposeForm uiComposeForm = uiPopupContainer.createUIComponent(UIComposeForm.class, null, null);
          uiComposeForm.init(accId, msg, uiComposeForm.MESSAGE_REPLY_ALL);
          uiPopupContainer.addChild(uiComposeForm) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
        }
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMsgPreview) ;
    }
  }

  static public class ForwardActionListener extends EventListener<UIMessagePreview> {
    public void execute(Event<UIMessagePreview> event) throws Exception {
      UIMessagePreview uiMsgPreview = event.getSource() ; 
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIMailPortlet uiPortlet = uiMsgPreview.getAncestorOfType(UIMailPortlet.class) ;
      String accId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();

      if (msgId != null) {
        Message msg = uiMsgPreview.getShowedMessageById(msgId);
        if (msg != null) {
          UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
          UIPopupActionContainer uiPopupContainer = uiPopupAction.activate(UIPopupActionContainer.class, 850) ;
          UIComposeForm uiComposeForm = uiPopupContainer.createUIComponent(UIComposeForm.class, null, null);
          uiComposeForm.init(accId, msg, uiComposeForm.MESSAGE_FOWARD);
          uiPopupContainer.addChild(uiComposeForm) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
        }
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMsgPreview) ;
    }
  }

  static public class DeleteActionListener extends EventListener<UIMessagePreview> {
    public void execute(Event<UIMessagePreview> event) throws Exception {
      UIMessagePreview uiMsgPreview = event.getSource();
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIMailPortlet uiPortlet = uiMsgPreview.getAncestorOfType(UIMailPortlet.class);
      UIMessageArea uiMsgArea = uiPortlet.findFirstComponentOfType(UIMessageArea.class);
      UIMessageList uiMsgList = uiMsgArea.getChild(UIMessageList.class) ;
      Message msg = uiMsgPreview.getShowedMessageById(msgId) ;
      if (msg != null) {
        MailService mailSrv = uiMsgPreview.getApplicationComponent(MailService.class);
        String username = MailUtils.getCurrentUser();
        String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
        mailSrv.moveMessages(SessionsUtils.getSessionProvider(), username, accountId, msg, msg.getFolders()[0],  Utils.createFolderId(accountId, Utils.FD_TRASH, false));
        uiMsgList.updateList();
        uiMsgPreview.setMessage(null);
      }
      uiMsgPreview.setShowedMessages(null) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.findFirstComponentOfType(UINavigationContainer.class));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMsgArea);
    }
  }

  static public class PrintActionListener extends EventListener<UIMessagePreview> {
    public void execute(Event<UIMessagePreview> event) throws Exception {
      UIMessagePreview uiMsgPreview = event.getSource();
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIMailPortlet uiPortlet = uiMsgPreview.getAncestorOfType(UIMailPortlet.class);
      Message msg = uiMsgPreview.getShowedMessageById(msgId) ;
      if (msg != null) {
        UIPopupAction uiPopup = uiPortlet.getChild(UIPopupAction.class);
        UIPrintPreview uiPrintPreview = uiPopup.activate(UIPrintPreview.class, 700) ;
        uiPrintPreview.setPrintMessage(msg) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMsgPreview);
    }
  }

  static public class AddContactActionListener extends EventListener<UIMessagePreview> {
    public void execute(Event<UIMessagePreview> event) throws Exception {
      UIMessagePreview uiMsgPreview = event.getSource() ;   
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIMailPortlet uiPortlet = uiMsgPreview.getAncestorOfType(UIMailPortlet.class);

      Message msg = uiMsgPreview.getShowedMessageById(msgId) ;
      if (msg != null) {
        UIPopupAction uiPopup = uiPortlet.getChild(UIPopupAction.class);
        UIPopupActionContainer uiPopupContainer = uiPopup.createUIComponent(UIPopupActionContainer.class, null, null) ;
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
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMsgPreview);
    }
  }

  static public class ExportActionListener extends EventListener<UIMessagePreview> {
    public void execute(Event<UIMessagePreview> event) throws Exception {
      UIMessagePreview uiMsgPreview = event.getSource() ;   
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIMailPortlet uiPortlet = uiMsgPreview.getAncestorOfType(UIMailPortlet.class);
      try {
        Message msg = uiMsgPreview.getShowedMessageById(msgId) ;
        if (msg != null) {
          UIPopupAction uiPopup = uiPortlet.getChild(UIPopupAction.class);
          UIExportForm uiExportForm = uiPopup.createUIComponent(UIExportForm.class, null, null);
          uiPopup.activate(uiExportForm, 600, 0, true);
          uiExportForm.setExportMessage(msg);
          event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup);
        }
      } catch (Exception e) { }  
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMsgPreview);
    }
  }

  static public class AddTagActionListener extends EventListener<UIMessagePreview> {
    public void execute(Event<UIMessagePreview> event) throws Exception {
      UIMessagePreview uiMsgPreview = event.getSource() ; 
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIMailPortlet uiPortlet = uiMsgPreview.getAncestorOfType(UIMailPortlet.class);
      Message msg = uiMsgPreview.getShowedMessageById(msgId) ;
      if (msg != null) {
        UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class);
        UITagForm uiTagForm = uiMsgPreview.createUIComponent(UITagForm.class, null, null) ;
        String username = uiPortlet.getCurrentUser();
        String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
        MailService mailSrv = MailUtils.getMailService();
        List<Tag> listTags = mailSrv.getTags(SessionsUtils.getSessionProvider(), username, accountId);
        uiPopupAction.activate(uiTagForm, 600, 0, true);
        List<Message> msgList = new ArrayList<Message>();
        msgList.add(msg);
        uiTagForm.setMessageList(msgList);
        uiTagForm.setTagList(listTags) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMsgPreview);
    }
  }

  static public class MoveMessagesActionListener extends EventListener<UIMessagePreview> {
    public void execute(Event<UIMessagePreview> event) throws Exception {
      UIMessagePreview uiMsgPreview = event.getSource() ;    
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIMailPortlet uiPortlet = uiMsgPreview.getAncestorOfType(UIMailPortlet.class) ;
      Message msg = uiMsgPreview.getShowedMessageById(msgId) ;
      if (msg != null) {
        String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue() ;
        UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;     
        UIMoveMessageForm uiMoveMessageForm = uiMsgPreview.createUIComponent(UIMoveMessageForm.class,null, null) ;
        uiMoveMessageForm.init(accountId);
        List<Message> msgList = new ArrayList<Message>() ;
        msgList.add(msg) ;
        uiMoveMessageForm.setMessageList(msgList);
        uiPopupAction.activate(uiMoveMessageForm, 600, 0, true) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      }
      uiMsgPreview.setMessage(null) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMsgPreview) ;
    }
  }
  
  static public class AnswerInvitationActionListener extends EventListener<UIMessagePreview> {
    public void execute(Event<UIMessagePreview> event) throws Exception {
      UIMessagePreview uiMsgPreview = event.getSource() ;    
      String answer = event.getRequestContext().getRequestParameter(OBJECTID) ;
      CalendarService calService = uiMsgPreview.getApplicationComponent(CalendarService.class) ;
      String fromUserId = MailUtils.getEventFrom(uiMsgPreview.selectedMessage_) ;
      String toUserId = MailUtils.getEventTo(uiMsgPreview.selectedMessage_) ;
      int calType = Integer.parseInt(MailUtils.getEventType(uiMsgPreview.selectedMessage_)) ;
      String calendarId = MailUtils.getCalendarId(uiMsgPreview.selectedMessage_) ;
      String eventId = MailUtils.getCalendarEventId(uiMsgPreview.selectedMessage_) ;
      try {
        calService.confirmInvitation(fromUserId, toUserId, calType, calendarId, eventId, Integer.parseInt(answer)) ;
      } catch (Exception e) {
        UIApplication uiApp = uiMsgPreview.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIMessagePreview.msg.trouble-loading-event", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
    }
  }
  
  static public class ViewAllHeadersActionListener extends EventListener<UIMessagePreview> {
    public void execute(Event<UIMessagePreview> event) throws Exception {
      UIMessagePreview uiMsgPreview = event.getSource() ;    
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIMailPortlet uiPortlet = uiMsgPreview.getAncestorOfType(UIMailPortlet.class) ;
      UIPopupAction uiPopup = uiPortlet.getChild(UIPopupAction.class) ;
      Message msg = uiMsgPreview.getShowedMessageById(msgId) ;
      UIViewAllHeaders uiAllHeader = uiPopup.createUIComponent(UIViewAllHeaders.class,null, null) ;
      uiAllHeader.init(msg);
      uiPopup.activate(uiAllHeader, 700, 0, true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
    }
  }
}
