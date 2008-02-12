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
import java.util.List;

import javax.mail.internet.InternetAddress;

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
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
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
        @EventConfig(listeners = UIMessagePreview.MoveMessagesActionListener.class)
    }
)

public class UIMessagePreview extends UIComponent {
  private Message selectedMessage_ ;
  
  public UIMessagePreview() throws Exception {}
  
  public Message getMessage() throws Exception { 
      return selectedMessage_ ;
  }
  
  public void setMessage(Message msg) throws Exception {
    selectedMessage_ = msg ;
  }
  
  public DownloadService getDownloadService() { 
    return getApplicationComponent(DownloadService.class) ; 
  }
  
  public static class DownloadAttachmentActionListener extends EventListener<UIMessagePreview> {
    public void execute(Event<UIMessagePreview> event) throws Exception {
      UIMessagePreview uiMessagePreview = event.getSource();
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID);
      String attId = event.getRequestContext().getRequestParameter("attachId");
      UIMailPortlet uiPortlet = uiMessagePreview.getAncestorOfType(UIMailPortlet.class);
      UIMessageList uiMsgList = uiPortlet.findFirstComponentOfType(UIMessageList.class);
      Message message = uiMsgList.messageList_.get(msgId);
      List<Attachment> attList = message.getAttachments();
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
  
  static public class AddStarActionListener extends EventListener<UIMessagePreview> {
    public void execute(Event<UIMessagePreview> event) throws Exception { 
      UIMessagePreview uiMessagePreview = event.getSource();
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIMailPortlet uiPortlet = uiMessagePreview.getAncestorOfType(UIMailPortlet.class);
      UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType(UIMessageList.class);
      String username = uiPortlet.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      MailService mailServ = uiPortlet.getApplicationComponent(MailService.class);
      try {
        Message msg = uiMessageList.messageList_.get(msgId);
        msg.setHasStar(!msg.hasStar());
        mailServ.saveMessage(SessionsUtils.getSessionProvider(), username, accountId, msg, false);
        uiMessageList.messageList_.put(msgId, msg);
        uiMessagePreview.setMessage(msg);
      } catch (Exception e) { }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class));
    }
  }
  
  static public class ReplyActionListener extends EventListener<UIMessagePreview> {
    public void execute(Event<UIMessagePreview> event) throws Exception {
      UIMessagePreview uiMessagePreview = event.getSource() ; 
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIMailPortlet uiPortlet = uiMessagePreview.getAncestorOfType(UIMailPortlet.class) ;
      UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType(UIMessageList.class);
      String accId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
      UIPopupActionContainer uiPopupContainer = uiPopupAction.activate(UIPopupActionContainer.class, 850) ;
      
      UIComposeForm uiComposeForm = uiPopupContainer.createUIComponent(UIComposeForm.class, null, null);
      if (msgId != null) {
        Message message = uiMessageList.messageList_.get(msgId);
        uiComposeForm.init(accId, message, uiComposeForm.MESSAGE_REPLY);
      }
      uiPopupContainer.addChild(uiComposeForm) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.findFirstComponentOfType(UIMessagePreview.class));
    }
  }
  
  static  public class ReplyAllActionListener extends EventListener<UIMessagePreview> {    
    public void execute(Event<UIMessagePreview> event) throws Exception {
      UIMessagePreview uiMessagePreview = event.getSource() ; 
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIMailPortlet uiPortlet = uiMessagePreview.getAncestorOfType(UIMailPortlet.class) ;
      String accId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
      UIPopupActionContainer uiPopupContainer = uiPopupAction.activate(UIPopupActionContainer.class, 850) ;
      UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType(UIMessageList.class);
      UIComposeForm uiComposeForm = uiPopupContainer.createUIComponent(UIComposeForm.class, null, null);
      if (msgId != null) {
        Message message = uiMessageList.messageList_.get(msgId);
        uiComposeForm.init(accId, message, uiComposeForm.MESSAGE_REPLY_ALL);
      }
      uiPopupContainer.addChild(uiComposeForm) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.findFirstComponentOfType(UIMessageArea.class));
    }
  }
  
  static public class ForwardActionListener extends EventListener<UIMessagePreview> {
    public void execute(Event<UIMessagePreview> event) throws Exception {
      UIMessagePreview uiMessagePreview = event.getSource() ; 
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIMailPortlet uiPortlet = uiMessagePreview.getAncestorOfType(UIMailPortlet.class) ;
      String accId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
      UIPopupActionContainer uiPopupContainer = uiPopupAction.activate(UIPopupActionContainer.class, 850) ;
      UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType(UIMessageList.class);
      UIComposeForm uiComposeForm = uiPopupContainer.createUIComponent(UIComposeForm.class, null, null);

      if (msgId != null) {
        Message message = uiMessageList.messageList_.get(msgId);
        uiComposeForm.init(accId, message, uiComposeForm.MESSAGE_FOWARD);
      }
      uiPopupContainer.addChild(uiComposeForm) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.findFirstComponentOfType(UIMessagePreview.class));
    }
  }
  
  static public class DeleteActionListener extends EventListener<UIMessagePreview> {
    public void execute(Event<UIMessagePreview> event) throws Exception {
      UIMessagePreview uiPreview = event.getSource();
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIMailPortlet uiPortlet = uiPreview.getAncestorOfType(UIMailPortlet.class);
      UIMessageArea uiMessageArea = uiPortlet.findFirstComponentOfType(UIMessageArea.class);
      MailService mailSrv = uiPreview.getApplicationComponent(MailService.class);
      UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType(UIMessageList.class);
      String username = MailUtils.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
       
      Message msg = uiMessageList.messageList_.get(msgId);
      mailSrv.moveMessages(SessionsUtils.getSessionProvider(), username, accountId, msg, msg.getFolders()[0],  Utils.createFolderId(accountId, Utils.FD_TRASH, false));
      uiPreview.setMessage(null);
      uiPortlet.findFirstComponentOfType(UIMessageList.class).updateList();
      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.findFirstComponentOfType(UINavigationContainer.class));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageArea);
    }
  }
  
  static public class PrintActionListener extends EventListener<UIMessagePreview> {
    public void execute(Event<UIMessagePreview> event) throws Exception {
      UIMessagePreview uiMessagePreview = event.getSource();
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIMailPortlet uiPortlet = uiMessagePreview.getAncestorOfType(UIMailPortlet.class);
      UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType(UIMessageList.class);
      UIPopupAction uiPopup = uiPortlet.getChild(UIPopupAction.class);
      UIPrintPreview uiPrintPreview = uiPopup.activate(UIPrintPreview.class, 700) ;
      uiPrintPreview.setPrintMessage(uiMessageList.messageList_.get(msgId)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.findFirstComponentOfType(UIMessagePreview.class));
    }
  }
  
  static public class AddContactActionListener extends EventListener<UIMessagePreview> {
    public void execute(Event<UIMessagePreview> event) throws Exception {
      UIMessagePreview uiMessagePreview = event.getSource() ;   
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIMailPortlet uiPortlet = uiMessagePreview.getAncestorOfType(UIMailPortlet.class);
      
      Message msg = uiPortlet.findFirstComponentOfType(UIMessageList.class).messageList_.get(msgId);
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
  }
  
  static public class ExportActionListener extends EventListener<UIMessagePreview> {
    public void execute(Event<UIMessagePreview> event) throws Exception {
      UIMessagePreview uiMessagePreview = event.getSource() ;   
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIMailPortlet uiPortlet = uiMessagePreview.getAncestorOfType(UIMailPortlet.class);
      UIPopupAction uiPopup = uiPortlet.getChild(UIPopupAction.class);
      UIExportForm uiExportForm = uiPopup.createUIComponent(UIExportForm.class, null, null);
      uiPopup.activate(uiExportForm, 600, 0, true);
      try {
        Message msg = uiPortlet.findFirstComponentOfType(UIMessageList.class).messageList_.get(msgId);
      uiExportForm.setExportMessage(msg);
      } catch (Exception e) { }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup);  
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.findFirstComponentOfType(UIMessagePreview.class));
    }
  }
  
  static public class AddTagActionListener extends EventListener<UIMessagePreview> {
    public void execute(Event<UIMessagePreview> event) throws Exception {
      UIMessagePreview uiMessagePreview = event.getSource() ; 
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIMailPortlet uiPortlet = uiMessagePreview.getAncestorOfType(UIMailPortlet.class);
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class);
      UITagForm uiTagForm = uiMessagePreview.createUIComponent(UITagForm.class, null, null) ;
      String username = uiPortlet.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      MailService mailSrv = MailUtils.getMailService();
      List<Tag> listTags = mailSrv.getTags(SessionsUtils.getSessionProvider(), username, accountId);
      uiPopupAction.activate(uiTagForm, 600, 0, true);
      List<Message> msgList = new ArrayList<Message>();
      msgList.add(uiPortlet.findFirstComponentOfType(UIMessageList.class).messageList_.get(msgId));
      uiTagForm.setMessageList(msgList);
      uiTagForm.setTagList(listTags) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.findFirstComponentOfType(UIMessagePreview.class));
    }
  }

  static public class MoveMessagesActionListener extends EventListener<UIMessagePreview> {
    public void execute(Event<UIMessagePreview> event) throws Exception {
      UIMessagePreview uiMessagePreview = event.getSource() ;    
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIMailPortlet uiPortlet = uiMessagePreview.getAncestorOfType(UIMailPortlet.class);
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class);     
      UIMoveMessageForm uiMoveMessageForm = uiMessagePreview.createUIComponent(UIMoveMessageForm.class,null, null);
      uiMoveMessageForm.init(accountId);
      List<Message> msgList = new ArrayList<Message>();
      msgList.add(uiPortlet.findFirstComponentOfType(UIMessageList.class).messageList_.get(msgId));
      uiMoveMessageForm.setMessageList(msgList);
      uiPopupAction.activate(uiMoveMessageForm, 600, 0, true);             
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);        
    }
  }
}
