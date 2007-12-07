/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui ;

import java.io.ByteArrayInputStream;
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
  
  public Message getMessage() throws Exception { return selectedMessage_; }
  
  public void setMessage(Message msg) throws Exception { selectedMessage_ = msg; }
  
  public List<Message> getConversations() throws Exception {
    List<Message> msgList = new ArrayList<Message>();
    msgList.add(selectedMessage_);
    String username = MailUtils.getCurrentUser();
    String accountId = MailUtils.getAccountId();
    MailService mailSrv = MailUtils.getMailService();
    if (selectedMessage_.isRootConversation() && (selectedMessage_.getMessageIds() != null && selectedMessage_.getMessageIds().length > 0)) {
      for (int i=0; i < selectedMessage_.getMessageIds().length; i++) {
        msgList.add(mailSrv.getMessageById(SessionsUtils.getSessionProvider(), username, accountId, selectedMessage_.getMessageIds()[i]));
      }
    }
    return msgList ;
  }
  
  public DownloadService getDownloadService() { 
    return getApplicationComponent(DownloadService.class) ; 
  }
  
  public static class DownloadAttachmentActionListener extends EventListener<UIMessagePreview> {
    public void execute(Event<UIMessagePreview> event) throws Exception {
      UIMessagePreview uiMessagePreview = event.getSource();
      String attId = event.getRequestContext().getRequestParameter(OBJECTID);
      List<Attachment> attList = uiMessagePreview.getMessage().getAttachments();
      JCRMessageAttachment att = new JCRMessageAttachment();
      for (Attachment attach : attList) {
        if (attach.getId().equals(attId)) {
          att = (JCRMessageAttachment)attach;
        }
      }
      ByteArrayInputStream bis = (ByteArrayInputStream)att.getInputStream();
      DownloadResource dresource = new InputStreamDownloadResource(bis, att.getMimeType());
      DownloadService dservice = (DownloadService)PortalContainer.getInstance().getComponentInstanceOfType(DownloadService.class);
      dresource.setDownloadName(att.getName());
      String downloadLink = dservice.getDownloadLink(dservice.addDownloadResource(dresource));
      UIMailPortlet uiPortlet = uiMessagePreview.getAncestorOfType(UIMailPortlet.class);
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
        Message msg = mailServ.getMessageById(SessionsUtils.getSessionProvider(), username, accountId, msgId);
        msg.setHasStar(!msg.hasStar());
        mailServ.saveMessage(SessionsUtils.getSessionProvider(), username, accountId, msg, false);
      } catch (Exception e) { }
      uiMessageList.updateList();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class));
    }
  }
  
  static public class ReplyActionListener extends EventListener<UIMessagePreview> {
    public void execute(Event<UIMessagePreview> event) throws Exception {
      UIMessagePreview uiMessagePreview = event.getSource() ; 
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIMailPortlet uiPortlet = uiMessagePreview.getAncestorOfType(UIMailPortlet.class) ;
      UINavigationContainer uiNavigation = uiPortlet.getChild(UINavigationContainer.class) ;
      UISelectAccount uiSelect = uiNavigation.getChild(UISelectAccount.class) ;
      String accId = uiSelect.getSelectedValue() ;
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
      UIPopupActionContainer uiPopupContainer = uiPopupAction.activate(UIPopupActionContainer.class, 850) ;
      
      UIComposeForm uiComposeForm = uiPopupContainer.createUIComponent(UIComposeForm.class, null, null);
      MailService mailSvr = uiMessagePreview.getApplicationComponent(MailService.class) ;
      String username = uiPortlet.getCurrentUser() ;
      if (msgId != null) {
        Message message = mailSvr.getMessageById(SessionsUtils.getSessionProvider(), username, accId, msgId);
        uiComposeForm.setMessage(message);
        uiComposeForm.setFieldToValue(message.getFrom());
        uiComposeForm.setFieldSubjectValue("Re: " + message.getSubject());
        uiComposeForm.setFieldContentValue(message.getMessageBody());
      }
      uiPopupContainer.addChild(uiComposeForm) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.findFirstComponentOfType(UIMessagePreview.class));
    }
  }
  
  static public class ForwardActionListener extends EventListener<UIMessagePreview> {
    public void execute(Event<UIMessagePreview> event) throws Exception {
      UIMessagePreview uiMessagePreview = event.getSource() ; 
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIMailPortlet uiPortlet = uiMessagePreview.getAncestorOfType(UIMailPortlet.class) ;
      UINavigationContainer uiNavigation = uiPortlet.getChild(UINavigationContainer.class) ;
      UISelectAccount uiSelect = uiNavigation.getChild(UISelectAccount.class) ;
      String accId = uiSelect.getSelectedValue() ;
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ;
      UIPopupActionContainer uiPopupContainer = uiPopupAction.activate(UIPopupActionContainer.class, 850) ;
      
      UIComposeForm uiComposeForm = uiPopupContainer.createUIComponent(UIComposeForm.class, null, null);

      MailService mailSvr = uiMessagePreview.getApplicationComponent(MailService.class) ;
      String username = uiPortlet.getCurrentUser() ;
      if (msgId != null) {
        Message message = mailSvr.getMessageById(SessionsUtils.getSessionProvider(), username, accId, msgId);
        uiComposeForm.setMessage(message);
        uiComposeForm.setFieldSubjectValue("Fwd: " + message.getSubject());
        String forwardedText = "\n\n\n-------- Original Message --------\n" +
            "Subject: " + message.getSubject() + "\nDate: " + message.getSendDate() + 
            "\nFrom: " + message.getFrom() + 
            "\nTo: " + message.getMessageTo() + 
            "\n\n" + message.getMessageBody();         
        uiComposeForm.setFieldContentValue(forwardedText);
        uiComposeForm.setFieldToValue("");
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
      UIFolderContainer uiFolderContainer = uiPortlet.findFirstComponentOfType(UIFolderContainer.class);
      UIMessageArea uiMessageArea = uiPortlet.findFirstComponentOfType(UIMessageArea.class);
      UITagContainer uiTags = uiPortlet.findFirstComponentOfType(UITagContainer.class); 
      MailService mailSrv = uiPreview.getApplicationComponent(MailService.class);
      String username = MailUtils.getCurrentUser();
      String accountId = MailUtils.getAccountId();
      
      Message msg = mailSrv.getMessageById(SessionsUtils.getSessionProvider(), username, accountId, msgId);
      mailSrv.moveMessages(SessionsUtils.getSessionProvider(), username, accountId, msgId, msg.getFolders()[0],  Utils.createFolderId(accountId, Utils.FD_TRASH, false));
      
      uiPortlet.findFirstComponentOfType(UIMessageList.class).updateList();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiFolderContainer);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageArea);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTags);
    }
  }
  
  static public class PrintActionListener extends EventListener<UIMessagePreview> {
    public void execute(Event<UIMessagePreview> event) throws Exception {
      UIMessagePreview uiMessagePreview = event.getSource();
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIMailPortlet uiPortlet = uiMessagePreview.getAncestorOfType(UIMailPortlet.class);
      UIPopupAction uiPopup = uiPortlet.getChild(UIPopupAction.class);
      UIPrintPreview uiPrintPreview = uiPopup.activate(UIPrintPreview.class, 700) ;
      uiPrintPreview.setPrintMessageId(msgId) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.findFirstComponentOfType(UIMessagePreview.class));
    }
  }
  
  static public class AddContactActionListener extends EventListener<UIMessagePreview> {
    public void execute(Event<UIMessagePreview> event) throws Exception {
      UIMessagePreview uiMessagePreview = event.getSource() ;   
      String msgId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String username = MailUtils.getCurrentUser();
      String accountId = MailUtils.getAccountId() ;
      MailService mailServ = MailUtils.getMailService() ;
      Message msg = mailServ.getMessageById(SessionsUtils.getSessionProvider(), username, accountId, msgId);
      UIMailPortlet uiPortlet = uiMessagePreview.getAncestorOfType(UIMailPortlet.class);
      UIPopupAction uiPopup = uiPortlet.getChild(UIPopupAction.class);
      UIAddContactForm uiAddContactForm = uiPopup.createUIComponent(UIAddContactForm.class, null, null);
      uiPopup.activate(uiAddContactForm, 560, 0, true);
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
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.findFirstComponentOfType(UIMessagePreview.class));
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
      String username = uiPortlet.getCurrentUser();
      String accountId = MailUtils.getAccountId();
      MailService mailServ = MailUtils.getMailService();
      try {
      Message msg = mailServ.getMessageById(SessionsUtils.getSessionProvider(), username, accountId, msgId);
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
      String accountId = MailUtils.getAccountId();
      MailService mailSrv = MailUtils.getMailService();
      UINavigationContainer uiNavigation = uiPortlet.getChild(UINavigationContainer.class) ;
      UISelectAccount uiSelect = uiNavigation.getChild(UISelectAccount.class) ;
      String accId = uiSelect.getSelectedValue() ;
      List<Tag> listTags = mailSrv.getTags(SessionsUtils.getSessionProvider(), username, accId);
      uiPopupAction.activate(uiTagForm, 600, 0, true);
      List<Message> msgList = new ArrayList<Message>();
      msgList.add(mailSrv.getMessageById(SessionsUtils.getSessionProvider(), username, accountId, msgId));
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
      String username = MailUtils.getCurrentUser();
      String accountId = MailUtils.getAccountId();
      MailService mailSrv = MailUtils.getMailService();
      UIMailPortlet uiPortlet = uiMessagePreview.getAncestorOfType(UIMailPortlet.class);
      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class);     
      UIMoveMessageForm uiMoveMessageForm = uiMessagePreview.createUIComponent(UIMoveMessageForm.class,null, null);
      List<Message> msgList = new ArrayList<Message>();
      msgList.add(mailSrv.getMessageById(SessionsUtils.getSessionProvider(), username, accountId, msgId));
      uiMoveMessageForm.setMessageList(msgList);
      uiPopupAction.activate(uiMoveMessageForm, 600, 0, true);             
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);        
    }
  }
}
