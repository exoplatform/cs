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
package org.exoplatform.mail.webui.popup;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.mail.internet.InternetAddress;

import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadResource;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.Attachment;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.MailSetting;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.UIFolderContainer;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.mail.webui.UIMessageArea;
import org.exoplatform.mail.webui.UIMessageList;
import org.exoplatform.mail.webui.UIMessagePreview;
import org.exoplatform.mail.webui.UISelectAccount;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.UIFormWYSIWYGInput;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen <hung.nguyen@exoplatform.com>
 *          Phung Nam <phunghainam@gmail.com>
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/mail/webui/popup/UIComposeForm.gtmpl",
    events = {
      @EventConfig(listeners = UIComposeForm.SendActionListener.class),      
      @EventConfig(listeners = UIComposeForm.SaveDraftActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIComposeForm.DiscardChangeActionListener.class),
      @EventConfig(listeners = UIComposeForm.AttachmentActionListener.class),
      @EventConfig(listeners = UIComposeForm.DownloadActionListener.class),
      @EventConfig(listeners = UIComposeForm.RemoveAttachmentActionListener.class),
      @EventConfig(listeners = UIComposeForm.ToActionListener.class),
      @EventConfig(listeners = UIComposeForm.ToCCActionListener.class),
      @EventConfig(listeners = UIComposeForm.ToBCCActionListener.class),
      @EventConfig(listeners = UIComposeForm.ChangePriorityActionListener.class),
      @EventConfig(listeners = UIComposeForm.UseVisualEdiorActionListener.class),
      @EventConfig(listeners = UIComposeForm.ShowCcActionListener.class),
      @EventConfig(listeners = UIComposeForm.ShowBccActionListener.class)
    }
)
public class UIComposeForm extends UIForm implements UIPopupComponent {
  final static public String FIELD_FROM_INPUT = "fromInput" ;
  final static public String FIELD_FROM = "from" ;
  final static public String FIELD_SUBJECT = "subject" ;
  final static public String FIELD_TO = "to" ;
  final static public String FIELD_CC = "cc" ;
  final static public String FIELD_BCC = "bcc" ;
  final static public String FIELD_ATTACHMENTS = "attachments" ;
  final static public String FIELD_MESSAGECONTENT = "messageContent" ;
  final static public String ACT_TO = "To" ;
  final static public String ACT_CC = "ToCC" ;
  final static public String ACT_BCC = "ToBCC" ;
  final static public String ACT_REMOVE = "remove" ;
  final static public int MESSAGE_NEW = 0;
  final public int MESSAGE_IN_DRAFT = 1;
  final public int MESSAGE_REPLY = 2;
  final public int MESSAGE_REPLY_ALL = 3;
  final public int MESSAGE_FOWARD = 4;
  private List<Attachment> attachments_ = new ArrayList<Attachment>() ;
  private Message message_ = null;
  private long priority_ = Utils.PRIORITY_NORMAL;
  private Boolean isVisualEditor = true;
  private int composeType_ = MESSAGE_NEW;
  private String accountId_ ;
  public String parentPath_ ;
  private boolean showCc_ = false ;
  private boolean showBcc_ = false ;
  
  public List<Contact> toContacts = new ArrayList<Contact>();
  public List<Contact> ccContacts = new ArrayList<Contact>();
  public List<Contact> bccContacts = new ArrayList<Contact>();
  
  public boolean isVisualEditor() { return isVisualEditor; }
  public void setVisualEditor(boolean b) { isVisualEditor = b; }
  
  public UIComposeForm() throws Exception { }
  
  public void init(String accountId, Message msg, int composeType) throws Exception {
    List<SelectItemOption<String>>  options = new ArrayList<SelectItemOption<String>>() ;
    String username = MailUtils.getCurrentUser();
    accountId_ = accountId ;
    MailService mailSrv = getApplicationComponent(MailService.class);
    for(Account acc : mailSrv.getAccounts(SessionProviderFactory.createSystemProvider(), username)) {
      SelectItemOption<String> itemOption = new SelectItemOption<String>(acc.getUserDisplayName() + " &lt;" + acc.getEmailAddress() + "&gt;", acc.getId());
      if (acc.getId().equals(accountId)) { itemOption.setSelected(true); }
      options.add(itemOption) ;
    }
    addUIFormInput(new UIFormSelectBox(FIELD_FROM, FIELD_FROM, options)) ;

    addUIFormInput(new UIFormStringInput(FIELD_TO, null, null)) ;
    UIFormTextAreaInput textAreaCC= new UIFormTextAreaInput(FIELD_CC, null, null);
    textAreaCC.setId(FIELD_CC);
    textAreaCC.setColumns(2);
    addUIFormInput(textAreaCC) ;
    UIFormTextAreaInput textAreaBCC = new UIFormTextAreaInput(FIELD_BCC, null, null);
    textAreaBCC.setColumns(2);
    textAreaBCC.setId(FIELD_BCC);
    addUIFormInput(textAreaBCC) ;
    addUIFormInput(new UIFormStringInput(FIELD_SUBJECT, null, null)) ;
    UIFormInputWithActions inputSet = new UIFormInputWithActions(FIELD_FROM_INPUT);   
    inputSet.addUIFormInput(new UIFormInputInfo(FIELD_ATTACHMENTS, FIELD_ATTACHMENTS, null)) ;
    inputSet.setActionField(FIELD_ATTACHMENTS, getUploadFileList()) ;
    addUIFormInput(inputSet) ;
    MailSetting mailSetting = mailSrv.getMailSetting(SessionProviderFactory.createSystemProvider(), username);
    isVisualEditor = mailSetting.useWysiwyg() ;
    if (isVisualEditor) {
      addUIFormInput(new UIFormWYSIWYGInput(FIELD_MESSAGECONTENT, null, null, true));    
    } else {
      addUIFormInput(new UIFormTextAreaInput(FIELD_MESSAGECONTENT, null, null)) ;
    }  
    setPriority(Utils.PRIORITY_NORMAL);
    setMessage(msg, composeType);
  }
  
  public List<Contact> getToContacts(){ return toContacts; }
  public List<Contact> getCcContacts(){ return ccContacts; }
  public List<Contact> getBccContacts(){ return bccContacts; }
  
  public void setToContacts(List<Contact> contactList) { toContacts = contactList; }
  public void setCcContacts(List<Contact> contactList) { ccContacts = contactList; }
  public void setBccContacts(List<Contact> contactList) { bccContacts = contactList; }
  
  public int getComposeType() { return composeType_ ; }
  public void setComposeType(int t) { composeType_ = t; }

  public List<ActionData> getUploadFileList() throws Exception { 
    List<ActionData> uploadedFiles = new ArrayList<ActionData>() ;
    for(Attachment attachdata : attachments_) {
      ActionData fileUpload = new ActionData() ;
      fileUpload.setActionListener("Download") ;
      fileUpload.setActionParameter(attachdata.getId());
      fileUpload.setActionType(ActionData.TYPE_ICON) ;
      fileUpload.setCssIconClass("AttachmentIcon") ; // "AttachmentIcon ZipFileIcon"
      fileUpload.setActionName(attachdata.getName() + " (" + MailUtils.convertSize(attachdata.getSize()) + ")" ) ;
      fileUpload.setShowLabel(true) ;
      uploadedFiles.add(fileUpload) ;
      ActionData removeAction = new ActionData() ;
      removeAction.setActionListener("RemoveAttachment") ;
      removeAction.setActionName(ACT_REMOVE);
      removeAction.setActionParameter(attachdata.getId());
      removeAction.setCssIconClass("LabelLink");
      removeAction.setActionType(ActionData.TYPE_LINK) ;
      uploadedFiles.add(removeAction) ;
    }
    return uploadedFiles ;
  }
  
  public void refreshUploadFileList() throws Exception {
    UIFormInputWithActions inputSet = getChildById(FIELD_FROM_INPUT) ;
    inputSet.setActionField(FIELD_ATTACHMENTS, getUploadFileList()) ;
  }
  public void addToUploadFileList(Attachment attachfile) {
    attachments_.add(attachfile) ;
  }
  public void removeFromUploadFileList(Attachment attachfile) {
    attachments_.remove(attachfile);
  }  
  public void removeUploadFileList() {
    attachments_.clear() ;
  }
  public List<Attachment> getAttachFileList() {
    return attachments_ ;
  }
  
  public Message getMessage() { return message_; }
  public void setMessage(Message message , int composeType) throws Exception { 
    setComposeType(composeType) ;
    this.message_ = message; 
    fillFields(message_);
  }
  
  public void fillFields(Message msg) throws Exception {
    if (msg != null) {
      MailService mailSrv = MailUtils.getMailService();
      MailSetting mailSetting = mailSrv.getMailSetting(SessionProviderFactory.createSystemProvider(), MailUtils.getCurrentUser());
      switch (getComposeType()) {
        case MESSAGE_IN_DRAFT :
          setFieldSubjectValue(msg.getSubject());
          setFieldToValue(msg.getMessageTo());
          setFieldCcValue(msg.getMessageCc()) ;
          setFieldBccValue(msg.getMessageBcc()) ;
          setFieldContentValue(formatContent(msg));
          if (msg != null && msg.hasAttachment()) {
            String username = MailUtils.getCurrentUser();
            msg = mailSrv.loadAttachments(SessionProviderFactory.createSystemProvider(), username, this.accountId_, msg) ;
            for (Attachment att : msg.getAttachments()) {
              attachments_.add(att);
              refreshUploadFileList();
            }
          }
          break;
        case MESSAGE_REPLY :
          setFieldToValue(msg.getReplyTo());
          setFieldSubjectValue("Re: " + msg.getSubject());
          String content = getReplyContent(msg);   
          setFieldContentValue(content);
          if (mailSetting.replyWithAttach()) {
            if (msg != null && msg.hasAttachment()) {
              if (msg.getAttachments() == null) {
                String username = MailUtils.getCurrentUser();
                msg = mailSrv.loadAttachments(SessionProviderFactory.createSystemProvider(), username, this.accountId_, msg) ;
              }
              for (Attachment att : msg.getAttachments()) {
                attachments_.add(att);
                refreshUploadFileList();
              }
            }
          }
          break ;
        case MESSAGE_REPLY_ALL :
          setFieldSubjectValue("Re: " + msg.getSubject());
          String replyTo = msg.getReplyTo();
          setFieldToValue(replyTo);
          
          String replyCc = "";
          
          String msgTo = (msg.getMessageTo() != null) ? msg.getMessageTo() : "" ;
          InternetAddress[] msgToAdds = Utils.getInternetAddress(msgTo) ;
          
          MailService mailSvr = this.getApplicationComponent(MailService.class) ;
          Account account = mailSvr.getAccountById(SessionProviderFactory.createSystemProvider(), MailUtils.getCurrentUser(), this.getFieldFromValue());
          for (int i = 0 ; i < msgToAdds.length; i++) {
            if (msgToAdds[i] != null && !msgToAdds[i].getAddress().equalsIgnoreCase(account.getEmailAddress()) &&
                !msgToAdds[i].getAddress().equalsIgnoreCase(account.getIncomingUser())) {
              if (replyCc.trim().length() > 0) replyCc += ", ";
              replyCc += msgToAdds[i].toString();
            }
          }          
          if (msg.getMessageCc() != null) replyCc += "," + msg.getMessageCc();
          
          if (replyCc.trim().length() > 0) {
            setFieldCcValue(replyCc);
            showCc_ = true;
          }
          
          String replyContent = getReplyContent(msg);
          setFieldContentValue(replyContent);
          if (mailSetting.replyWithAttach()) {
            if (msg != null && msg.hasAttachment()) {
              if (msg.getAttachments() == null) {
                String username = MailUtils.getCurrentUser();
                msg = mailSrv.loadAttachments(SessionProviderFactory.createSystemProvider(), username, this.accountId_, msg) ;
              }
              for (Attachment att : msg.getAttachments()) {
                attachments_.add(att);
                refreshUploadFileList();
              }
            }
          }
          break;
        case MESSAGE_FOWARD : 
          //TODO should replate text by value form resource boundle
          String toAddress = msg.getMessageTo() != null ? msg.getMessageTo() : "" ;
          setFieldSubjectValue("Fwd: " + msg.getSubject());
          StringBuffer forwardTxt = new StringBuffer("<br><br>-------- Original Message --------<br>") ;
          forwardTxt.append("Subject: ").append(MailUtils.encodeHTML(msg.getSubject())).append("<br>") ;
          forwardTxt.append("Date: ").append(msg.getSendDate()).append("<br>") ;
          forwardTxt.append("From: ") ;
          
          InternetAddress[] addresses = Utils.getInternetAddress(msg.getFrom()) ;
          for (int i = 0 ; i < addresses.length; i++) {
            if (i > 0) forwardTxt.append(", ") ;
            if (addresses[i] != null) forwardTxt.append(Utils.getPersonal(addresses[i])).append(" \"").append(addresses[i].getAddress()).append("\"") ;
          }
          forwardTxt.append("<br>To: ") ;
          
          InternetAddress[] toAddresses = Utils.getInternetAddress(toAddress) ;
          for (int i = 0 ; i < toAddresses.length; i++) {
            if (i > 0) forwardTxt.append(", ") ;
            if (toAddresses[i] != null) forwardTxt.append(Utils.getPersonal(toAddresses[i])).append(" \"").append(toAddresses[i].getAddress()).append("\"") ;
          }
          
          forwardTxt.append("<br><br>").append(formatContent(msg)) ;
      
          setFieldContentValue(forwardTxt.toString()) ;
          
          setFieldToValue("");
          if (mailSetting.forwardWithAtt()) {
            if (msg != null && msg.hasAttachment()) {
              if (msg.getAttachments() == null) {
                String username = MailUtils.getCurrentUser();
                msg = mailSrv.loadAttachments(SessionProviderFactory.createSystemProvider(), username, this.accountId_, msg) ;
              }
              for (Attachment att : msg.getAttachments()) {
                attachments_.add(att);
                refreshUploadFileList();
              }
            }
          }
          break ;
        default :
          break;
      }
    } else {
      setFieldContentValue("") ;
    }
  }
  
  private String getReplyContent(Message msg) throws Exception {
    String msgContent = formatContent(msg) ;
    String content = msgContent ;
    if (isVisualEditor) {
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
      Locale locale = context.getParentAppRequestContext().getLocale() ;
      content = "<br><br><div> On " + MailUtils.formatDate("MMM dd, yyyy HH:mm aaa", msg.getSendDate(), locale) + ", " + msg.getFrom() + " wrote: <br>" ;
      content += "<blockquote style=\"border-left:1px #cccccc solid ; margin-left: 10px; padding-left: 5px;\">" + msgContent + "</blockquote></div>" ;
    }
    return content ;
  }
  
  private String formatContent(Message msg) throws Exception {
    String msgContent = msg.getMessageBody();
    if (msg.getContentType().indexOf("text/plain") > -1) {
      msgContent = MailUtils.encodeHTML(msg.getMessageBody()).replace("\n", "<br>") ;
    } 
    return msgContent ;
  }
  
  public long getPriority() { return priority_; }  
  public void setPriority(long priority) { priority_ = priority; }
  
  public String getFieldFromValue() {
    return getUIFormSelectBox(FIELD_FROM).getValue() ;
  }

  public String getFieldSubjectValue() {
    String subject = getUIStringInput(FIELD_SUBJECT).getValue() ;
    if (subject == null ) subject = "(no subject)";
    return subject ;   
  }
  
  public void setFieldSubjectValue(String value) {
    getUIStringInput(FIELD_SUBJECT).setValue(value) ;
  }
  
  public String getFieldToValue() {
    return getUIStringInput(FIELD_TO).getValue() ;
  }
  
  public void setFieldToValue(String value) {
    getUIStringInput(FIELD_TO).setValue(value);
  }

  public String getFieldCcValue() {
    return getUIFormTextAreaInput(FIELD_CC).getValue() ;
  }
  
  public void setFieldCcValue(String value) {
    getUIFormTextAreaInput(FIELD_CC).setValue(value);
  }

  public String getFieldBccValue() {
    return getUIFormTextAreaInput(FIELD_BCC).getValue() ;
  }
  
  public void setFieldBccValue(String value) {
    getUIFormTextAreaInput(FIELD_BCC).setValue(value);
  }
  public String getFieldAttachmentsValue() {
    UIFormInputWithActions inputSet = getChildById(FIELD_FROM_INPUT) ;
    return inputSet.getUIFormInputInfo(FIELD_ATTACHMENTS).getValue() ;
  }
  
  public String getFieldContentValue() {
    String content = "";
    if (isVisualEditor) {
      content = getChild(UIFormWYSIWYGInput.class).getValue();
    } else {
      content = getUIFormTextAreaInput(FIELD_MESSAGECONTENT).getValue();
    }
    if (content == null) content = "" ;
    return content;
  }
  
  public void setFieldContentValue(String value) throws Exception {
    String username = MailUtils.getCurrentUser();
    MailService mailSrv = getApplicationComponent(MailService.class);
    Account account = mailSrv.getAccountById(SessionProviderFactory.createSystemProvider(), username, accountId_);
    if (isVisualEditor) {
      if (!MailUtils.isFieldEmpty(account.getSignature()) && !fromDrafts()) {value += "<br><br> -- <br >" + account.getSignature().replace("\n", "<br>") + "";}
      getChild(UIFormWYSIWYGInput.class).setValue(value);
    } else {
      if (!MailUtils.isFieldEmpty(account.getSignature())) { value += "\n\n -- \n" + account.getSignature() ; }
      getUIFormTextAreaInput(FIELD_MESSAGECONTENT).setValue(value);
    }
  }
  
  public List<Contact> getContacts() throws Exception {
    ContactService contactSrv = getApplicationComponent(ContactService.class);
    String username = MailUtils.getCurrentUser();
    return contactSrv.getAllContact(SessionProviderFactory.createSystemProvider(), username);
  }
  
  public void resetFields() { reset() ; }
  
  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
  
  private Message getNewMessage() throws Exception {
    Message message = getMessage();
    if (!fromDrafts()) {
      if (message != null) parentPath_ = message.getPath() ;
      message = new Message(); 
    }
    UIMailPortlet uiPortlet = getAncestorOfType(UIMailPortlet.class);
    String usename = uiPortlet.getCurrentUser() ;
    MailService mailSvr = this.getApplicationComponent(MailService.class) ;
    Account account = mailSvr.getAccountById(SessionProviderFactory.createSystemProvider(), usename, this.getFieldFromValue());
    String from = account.getUserDisplayName() + "<" + account.getEmailAddress() + ">" ;
    String subject = this.getFieldSubjectValue() ;
    String to = this.getFieldToValue() ;
    if (to != null && to.indexOf(";") > -1) to = to.replace(';', ',') ;
    String cc = this.getFieldCcValue() ;
    if (cc != null && cc.indexOf(";") > -1) cc = cc.replace(';', ',') ;
    String bcc = this.getFieldBccValue() ;
    if (bcc != null && bcc.indexOf(";") > -1) bcc = bcc.replace(';', ',') ;
    String body = this.getFieldContentValue() ;
    Long priority = this.getPriority();
    message.setSendDate(new Date()) ;
    message.setAccountId(accountId_) ;
    message.setFrom(from) ;
    String contentType = Utils.MIMETYPE_TEXTHTML;
    if (!isVisualEditor()) { contentType = Utils.MIMETYPE_TEXTPLAIN; }
    message.setContentType(contentType);
    message.setSubject(subject) ;
    message.setMessageTo(to) ;
    message.setMessageCc(cc) ;
    if (message.getReceivedDate() == null) {
      message.setReceivedDate(new Date());
    }
    message.setMessageBcc(bcc) ;
    message.setHasStar(false);
    message.setPriority(priority);
    message.setAttachements(this.getAttachFileList()) ;
    long attSize = 0 ;
    for (Attachment att : this.getAttachFileList()) {
      attSize += att.getSize() ;
    }
    message.setMessageBody(body) ;
    message.setUnread(false);
    message.setSize(body.getBytes().length + attSize) ;
    message.setReplyTo(account.getEmailReplyAddress());
    if (getComposeType() == MESSAGE_REPLY || getComposeType() == MESSAGE_REPLY_ALL || getComposeType() == MESSAGE_FOWARD) {
      message.setHeader(Utils.HEADER_IN_REPLY_TO, getMessage().getId()) ;
    }
    return message;
  }
  
  public boolean fromDrafts() {    
    return (getMessage() != null && getMessage().getFolders()[0].equals(Utils.createFolderId(accountId_, Utils.FD_DRAFTS, false)) || getComposeType() == MESSAGE_IN_DRAFT) ;
  }

  static public class SendActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiForm = event.getSource() ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      UIMailPortlet uiPortlet = uiForm.getAncestorOfType(UIMailPortlet.class) ;
      UIFolderContainer uiFolderContainer = uiPortlet.findFirstComponentOfType(UIFolderContainer.class) ;
      String accountId = uiForm.getFieldFromValue() ;
      String usename = uiPortlet.getCurrentUser() ;
      MailService mailSvr = uiForm.getApplicationComponent(MailService.class) ;
      UIPopupAction uiChildPopup = uiForm.getAncestorOfType(UIPopupAction.class) ;
      Message message = uiForm.getNewMessage() ; 
      
      if (Utils.isEmptyField(message.getMessageTo())) {
        uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.to-field-empty", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } else if (!MailUtils.isValidEmailAddresses(message.getMessageTo())) {
        uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.invalid-to-field", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } else if (!MailUtils.isValidEmailAddresses(message.getMessageCc())) {
        uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.invalid-cc-field", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } else if (!MailUtils.isValidEmailAddresses(message.getMessageBcc())) {
        uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.invalid-bcc-field", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      
      try {
        mailSvr.sendMessage(SessionProviderFactory.createSystemProvider(), usename, message) ;
      } catch(Exception e) {
        uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.send-mail-error", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        e.printStackTrace() ;
        return ;
      }
      
      try {
        MailSetting setting = mailSvr.getMailSetting(SessionProviderFactory.createSystemProvider(), usename);
        boolean isSaved = setting.saveMessageInSent() ; 
        if (!uiForm.fromDrafts()) {
          if (isSaved) {
            message.setReplyTo(message.getMessageTo()) ;
            message.setFolders(new String[]{ Utils.createFolderId(accountId, Utils.FD_SENT, false) }) ;
            mailSvr.saveMessage(SessionProviderFactory.createSystemProvider(), usename, accountId, uiForm.parentPath_, message, true) ;
          }
        } else {
          Folder drafts = mailSvr.getFolder(SessionProviderFactory.createSystemProvider(), usename, accountId, Utils.createFolderId(accountId, Utils.FD_DRAFTS, false));
          if (isSaved) {
            message.setFolders(new String[]{ Utils.createFolderId(accountId, Utils.FD_SENT, false) }) ;
            mailSvr.saveMessage(SessionProviderFactory.createSystemProvider(), usename, accountId, uiForm.parentPath_, message, false) ;
          } else {
            mailSvr.removeMessage(SessionProviderFactory.createSystemProvider(), usename, accountId, message);
          }
          drafts.setTotalMessage(drafts.getTotalMessage() - 1);
          mailSvr.saveFolder(SessionProviderFactory.createSystemProvider(), usename, accountId, drafts);
        }
        UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType(UIMessageList.class) ;
        uiMessageList.updateList();
        event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiFolderContainer) ;
        uiChildPopup.deActivate() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;
      } catch (Exception e) {
        uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.save-sent-error", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        uiChildPopup.deActivate() ;
      }
    }
  }
  
  static public class SaveDraftActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiForm = event.getSource() ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      UIMailPortlet uiPortlet = uiForm.getAncestorOfType(UIMailPortlet.class) ;
      UIFolderContainer uiFolderContainer = uiPortlet.findFirstComponentOfType(UIFolderContainer.class) ;
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue() ;
      String usename = uiPortlet.getCurrentUser() ;
      MailService mailSvr = uiForm.getApplicationComponent(MailService.class) ;
      UIPopupAction uiChildPopup = uiForm.getAncestorOfType(UIPopupAction.class) ;
      Message message = uiForm.getNewMessage() ; 
      
      if (!MailUtils.isValidEmailAddresses(message.getMessageTo())) {
        uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.invalid-to-field", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } else if (!MailUtils.isValidEmailAddresses(message.getMessageCc())) {
        uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.invalid-cc-field", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } else if (!MailUtils.isValidEmailAddresses(message.getMessageBcc())) {
        uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.invalid-bcc-field", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      
      message.setReplyTo(message.getMessageTo()) ;
      try {
        String draftFolderId = Utils.createFolderId(accountId, Utils.FD_DRAFTS, false) ;
        message.setFolders(new String[]{ draftFolderId }) ;
        if (!uiForm.fromDrafts()) {
          mailSvr.saveMessage(SessionProviderFactory.createSystemProvider(), usename, accountId, uiForm.parentPath_, message, true) ;
          Folder drafts = mailSvr.getFolder(SessionProviderFactory.createSystemProvider(), usename, accountId, draftFolderId);
          drafts.setTotalMessage(drafts.getTotalMessage() + 1);
          mailSvr.saveFolder(SessionProviderFactory.createSystemProvider(), usename, accountId, drafts);
        } else {
          mailSvr.saveMessage(SessionProviderFactory.createSystemProvider(), usename, accountId, uiForm.parentPath_, message, false) ;
        }
      } catch (Exception e) {
        uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.save-draft-error", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        e.printStackTrace() ;
        uiChildPopup.deActivate() ;
      }
      String selectedFolder = uiFolderContainer.getSelectedFolder() ;
      if (selectedFolder != null && selectedFolder.equals(Utils.createFolderId(accountId, Utils.FD_DRAFTS, false))) {
        UIMessageList uiMsgList = uiPortlet.findFirstComponentOfType(UIMessageList.class) ;
        UIMessagePreview uiMsgPreview = uiPortlet.findFirstComponentOfType(UIMessagePreview.class) ;
        uiMsgList.setMessagePageList(mailSvr.getMessagePageList(SessionProviderFactory.createSystemProvider(), usename, uiMsgList.getMessageFilter())) ;
        //TODO check this code because it does nothing here
        List<Message> showedMsg = uiMsgPreview.getShowedMessages() ;
        try {
          if (showedMsg != null && showedMsg.size() > 0) {
            for (Message msg : showedMsg) {
              if (message.getId().equals(msg.getId())) {
                int index = showedMsg.indexOf(msg) ;
                showedMsg.remove(index) ;
                message = mailSvr.loadAttachments(SessionProviderFactory.createSystemProvider(), usename, accountId, 
                    mailSvr.getMessageById(SessionProviderFactory.createSystemProvider(), usename, accountId, message.getId())) ;
                showedMsg.add(index, message) ;
              }
            }
          }
        }catch(Exception e) {}
        //
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.findFirstComponentOfType(UIMessageArea.class)) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiFolderContainer) ;
      uiPortlet.cancelAction();
    }
  }
  
  static public class DiscardChangeActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiForm = event.getSource() ;
      UIMailPortlet uiPortlet = uiForm.getAncestorOfType(UIMailPortlet.class);
      uiForm.resetFields() ;
      uiPortlet.cancelAction();
    }
  }
  static public class AttachmentActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiForm = event.getSource() ;
      UIPopupActionContainer uiActionContainer = uiForm.getAncestorOfType(UIPopupActionContainer.class) ;
      UIPopupAction uiChildPopup = uiActionContainer.getChild(UIPopupAction.class) ;
      uiChildPopup.activate(UIAttachFileForm.class, 600) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiActionContainer) ;
    }
  }
  
  static public class DownloadActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiComposeForm = event.getSource();
      String attId = event.getRequestContext().getRequestParameter(OBJECTID);
      for (Attachment attach : uiComposeForm.getAttachFileList()) {
        if (attach.getId().equals(attId)) {
          DownloadResource dresource = new InputStreamDownloadResource(attach.getInputStream(), attach.getMimeType());
          DownloadService dservice = (DownloadService)PortalContainer.getInstance().getComponentInstanceOfType(DownloadService.class);
          dresource.setDownloadName(attach.getName());
          String downloadLink = dservice.getDownloadLink(dservice.addDownloadResource(dresource));
          event.getRequestContext().getJavascriptManager().addJavascript("ajaxRedirect('" + downloadLink + "');");
        }
      }
    }
  }
  
  static public class RemoveAttachmentActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiComposeForm = event.getSource() ;
      String attFileId = event.getRequestContext().getRequestParameter(OBJECTID);
      Iterator<Attachment> iter =  uiComposeForm.attachments_.iterator() ;
      Attachment att ;
      while (iter.hasNext()) {
        att = (Attachment) iter.next() ;
        if (att.getId().equals(attFileId)) {
          iter.remove() ;
        }
      }
      uiComposeForm.refreshUploadFileList() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiComposeForm.getParent()) ;
    }
  }

  static public class ToActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiComposeForm = event.getSource() ;
      UIPopupActionContainer uiActionContainer = uiComposeForm.getAncestorOfType(UIPopupActionContainer.class) ;    
      UIPopupAction uiChildPopup = uiActionContainer.getChild(UIPopupAction.class) ;
      UIAddressForm uiAddress = uiChildPopup.activate(UIAddressForm.class, 650) ; 
      
      uiAddress.setRecipientsType(FIELD_TO);
      String toAddressString = uiComposeForm.getFieldToValue() ;
      InternetAddress[] toAddresses = Utils.getInternetAddress(toAddressString) ;
      List<String> emailList = new ArrayList<String>();
      for (int i = 0 ; i < toAddresses.length; i++) {
        if (toAddresses[i] != null) emailList.add(toAddresses[i].getAddress());
      }
      List<Contact> toContact = uiComposeForm.getToContacts() ;
      if (toContact != null && toContact.size() > 0) {
        List<Contact> contactList = new ArrayList<Contact>();
        for (Contact ct : toContact) {
          if (emailList.contains(ct.getEmailAddress())) contactList.add(ct) ;
        }
        uiAddress.setAlreadyCheckedContact(contactList);
      }
      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiActionContainer) ;
    }
  }
  static public class ToCCActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiComposeForm = event.getSource() ;
      UIPopupActionContainer uiActionContainer = uiComposeForm.getAncestorOfType(UIPopupActionContainer.class) ;    
      UIPopupAction uiChildPopup = uiActionContainer.getChild(UIPopupAction.class) ;
      UIAddressForm uiAddress = uiChildPopup.activate(UIAddressForm.class, 650) ; 
      
      uiAddress.setRecipientsType(FIELD_CC);
      String ccAddressString = uiComposeForm.getFieldCcValue() ;
      InternetAddress[] ccAddresses = Utils.getInternetAddress(ccAddressString) ;
      List<String> emailList = new ArrayList<String>();
      for (int i = 0 ; i < ccAddresses.length; i++) {
        if (ccAddresses[i] != null) emailList.add(ccAddresses[i].getAddress());
      }
      List<Contact> ccContact = uiComposeForm.getCcContacts() ;
      if (ccContact != null && ccContact.size() > 0) {
        List<Contact> contactList = new ArrayList<Contact>();
        for (Contact ct : ccContact) {
          if (emailList.contains(ct.getEmailAddress())) contactList.add(ct) ;
        }
        uiAddress.setAlreadyCheckedContact(contactList);
      }
      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiActionContainer) ;
    }
  }
  
  static public class ToBCCActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiComposeForm = event.getSource() ;
      UIPopupActionContainer uiActionContainer = uiComposeForm.getAncestorOfType(UIPopupActionContainer.class) ;    
      UIPopupAction uiChildPopup = uiActionContainer.getChild(UIPopupAction.class) ;
      UIAddressForm uiAddress = uiChildPopup.activate(UIAddressForm.class, 650) ; 
      
      uiAddress.setRecipientsType(FIELD_BCC);
      String bccAddressString = uiComposeForm.getFieldBccValue() ;
      InternetAddress[] bccAddresses = Utils.getInternetAddress(bccAddressString) ;
      List<String> emailList = new ArrayList<String>();
      for (int i = 0 ; i < bccAddresses.length; i++) {
        if (bccAddresses[i] != null) emailList.add(bccAddresses[i].getAddress());
      }
      List<Contact> bccContact = uiComposeForm.getBccContacts() ;
      if (bccContact != null && bccContact.size() > 0) {
        List<Contact> contactList = new ArrayList<Contact>();
        for (Contact ct : bccContact) {
          if (emailList.contains(ct.getEmailAddress())) contactList.add(ct) ;
        }
        uiAddress.setAlreadyCheckedContact(contactList);
      }
      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiActionContainer) ;
    }   
  }
  
  static public class ChangePriorityActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiForm = event.getSource() ;
      String priority = event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiForm.setPriority(Long.valueOf(priority)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
    }
  }
  
  static public class UseVisualEdiorActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiForm = event.getSource() ;
      boolean isVisualEditor = Boolean.valueOf(event.getRequestContext().getRequestParameter(OBJECTID)) ;  
      String content = "";
      if (isVisualEditor) {
        try {
          content = uiForm.getUIFormTextAreaInput(FIELD_MESSAGECONTENT).getValue() ;
          uiForm.removeChildById(FIELD_MESSAGECONTENT);
          UIFormWYSIWYGInput wysiwyg = new UIFormWYSIWYGInput(FIELD_MESSAGECONTENT, null, null, true) ;
          uiForm.addUIFormInput(wysiwyg) ;
          wysiwyg.setValue(content);
          uiForm.setVisualEditor(true) ;
        } catch(Exception e) { }
      } else {
        try {
          content = uiForm.getChild(UIFormWYSIWYGInput.class).getValue() ;
          uiForm.removeChild(UIFormWYSIWYGInput.class) ;
          UIFormTextAreaInput textArea = new UIFormTextAreaInput(FIELD_MESSAGECONTENT, null, null);
          textArea.setValue(content);
          uiForm.addUIFormInput(textArea) ;
          uiForm.setVisualEditor(false) ;
        } catch (Exception e) { }
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
    }
  }
  
  static public class ShowCcActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiForm = event.getSource() ;
      uiForm.showCc_ = !uiForm.showCc_ ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
    }
  }
  
  static public class ShowBccActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiForm = event.getSource() ;
      uiForm.showBcc_ = !uiForm.showBcc_ ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
    }
  }
}
