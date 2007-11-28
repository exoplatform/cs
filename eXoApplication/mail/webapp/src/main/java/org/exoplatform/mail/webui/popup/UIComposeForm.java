/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui.popup;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadResource;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.Attachment;
import org.exoplatform.mail.service.BufferAttachment;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.MailSetting;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.UIFolderContainer;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.mail.webui.UINavigationContainer;
import org.exoplatform.mail.webui.UISelectAccount;
import org.exoplatform.web.application.ApplicationMessage;
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
    template = "app:/templates/mail/webui/UIComposeForm.gtmpl",
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
      @EventConfig(listeners = UIComposeForm.UseVisualEdiorActionListener.class)
    }
)
public class UIComposeForm extends UIForm implements UIPopupComponent{
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
  private List<Attachment> attachments_ = new ArrayList<Attachment>() ;
  private Message message_ = null;
  private long priority_ = Utils.PRIORITY_NORMAL;
  private Boolean isVisualEditor = true;

  public List<Contact> toContacts = new ArrayList<Contact>();
  public List<Contact> ccContacts = new ArrayList<Contact>();
  public List<Contact> bccContacts = new ArrayList<Contact>();
  
  public List<Contact> getToContacts(){ return toContacts; }
  public List<Contact> getCcContacts(){ return ccContacts; }
  public List<Contact> getBccContacts(){ return bccContacts; }
  
  public void setToContacts(List<Contact> contactList) { toContacts = contactList; }
  public void setCcContacts(List<Contact> contactList) { ccContacts = contactList; }
  public void setBccContacts(List<Contact> contactList) { bccContacts = contactList; }
  
  public boolean isVisualEditor() { return isVisualEditor; }
  public void setVisualEditor(boolean b) { isVisualEditor = b; }
  
  public UIComposeForm() throws Exception {
    List<SelectItemOption<String>>  options = new ArrayList<SelectItemOption<String>>() ;
    String username = MailUtils.getCurrentUser();
    MailService mailSrv = getApplicationComponent(MailService.class);
    for(Account acc : mailSrv.getAccounts(username)) {
      SelectItemOption<String> itemOption = new SelectItemOption<String>(acc.getUserDisplayName() + " &lt;" + acc.getEmailAddress() + 
          "&gt;", acc.getUserDisplayName() + "<" + acc.getEmailAddress() + ">");
      if (acc.getId().equals(MailUtils.getAccountId())) { itemOption.setSelected(true); }
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
    MailSetting mailSetting = mailSrv.getMailSetting(username);
    isVisualEditor = ((mailSetting.getTypeOfEditor().equals(MailSetting.WYSIWYG)) ? true : false );
    if (isVisualEditor) {
      addUIFormInput(new UIFormWYSIWYGInput(FIELD_MESSAGECONTENT, null, null, true));    
    } else {
      addUIFormInput(new UIFormTextAreaInput(FIELD_MESSAGECONTENT, null, null)) ;
    }  
    setPriority(Utils.PRIORITY_NORMAL);
  }

  public List<ActionData> getUploadFileList() { 
    List<ActionData> uploadedFiles = new ArrayList<ActionData>() ;
    for(Attachment attachdata : attachments_) {
      ActionData fileUpload = new ActionData() ;
      fileUpload.setActionListener("Download") ;
      fileUpload.setActionParameter(attachdata.getId());
      fileUpload.setActionType(ActionData.TYPE_ICON) ;
      fileUpload.setCssIconClass("AttachmentIcon") ; // "AttachmentIcon ZipFileIcon"
      fileUpload.setActionName(attachdata.getName() + " ("+attachdata.getSize()+" Kb)" ) ;
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
  public void setMessage(Message message) throws Exception { 
    this.message_ = message; 
    fillFields(message_);
  }
  
  public void fillFields(Message msg) throws Exception {
    if (msg != null) {
      setFieldSubjectValue(msg.getSubject());
      setFieldToValue(msg.getMessageTo());
      setFieldContentValue(msg.getMessageBody());
    }
  }
  
  public long getPriority() { return priority_; }  
  public void setPriority(long priority) { priority_ = priority; }
  
  public String getFieldFromValue() {
    return getUIFormSelectBox(FIELD_FROM).getValue() ;
  }

  public String getFieldSubjectValue() {
    return getUIStringInput(FIELD_SUBJECT).getValue() ;
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
    return content;
  }
  
  public void setFieldContentValue(String value) throws Exception {
    String username = MailUtils.getCurrentUser();
    String accountId = MailUtils.getAccountId();
    MailService mailSrv = getApplicationComponent(MailService.class);
    Account account = mailSrv.getAccountById(username, accountId);
    if (isVisualEditor) {
      if (!MailUtils.isFieldEmpty(account.getSignature())) {value += "</br> -- <br />" + account.getSignature() + "";}
      getChild(UIFormWYSIWYGInput.class).setValue(value);
    } else {
      if (!MailUtils.isFieldEmpty(account.getSignature())) { value += account.getSignature() ; }
      getUIFormTextAreaInput(FIELD_MESSAGECONTENT).setValue(value);
    }
  }
  
  public List<Contact> getContacts() throws Exception {
    ContactService contactSrv = getApplicationComponent(ContactService.class);
    String username = MailUtils.getCurrentUser();
    return contactSrv.getAllContact(username);
  }
  
  public void resetFields() { reset() ; }
  
  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
  
  private Message getNewMessage() throws Exception {
    Message message = getMessage();
    if (getMessage() == null) { message = new Message(); }
    UIMailPortlet uiPortlet = getAncestorOfType(UIMailPortlet.class);
    UISelectAccount uiSelectAcc = uiPortlet.findFirstComponentOfType(UISelectAccount.class) ;
    String accountId = uiSelectAcc.getSelectedValue() ;
    String usename = uiPortlet.getCurrentUser() ;
    MailService mailSvr = this.getApplicationComponent(MailService.class) ;
    Account account = mailSvr.getAccountById(usename, accountId);
    String from = this.getFieldFromValue() ;
    String subject = this.getFieldSubjectValue() ;
    String to = this.getFieldToValue() ;
    String cc = this.getFieldCcValue() ;
    String bcc = this.getFieldBccValue() ;
    String body = this.getFieldContentValue() ;
    Long priority = this.getPriority();
    message.setSendDate(new Date()) ;
    message.setAccountId(accountId) ;
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
    message.setMessageBody(body) ;
    message.setUnread(false);
    message.setReplyTo(account.getUserDisplayName()+ "<" + account.getEmailReplyAddress() + ">");
    return message;
  }
  
  public boolean fromDrafts() {    
    String accountId = getAncestorOfType(UIMailPortlet.class).findFirstComponentOfType(UISelectAccount.class).getSelectedValue() ;
    if (getMessage() != null && getMessage().getFolders()[0].equals(Utils.createFolderId(accountId, Utils.FD_DRAFTS, false)) ) { 
      return true;
    } 
    return false;
  }

  static public class SendActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiForm = event.getSource() ;
      System.out.println(" === >>> Send Action Listener") ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      if(Utils.isEmptyField(uiForm.getFieldToValue())) {
        uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.to-field-required", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      UIMailPortlet uiPortlet = uiForm.getAncestorOfType(UIMailPortlet.class) ;
      UISelectAccount uiSelectAcc = uiPortlet.findFirstComponentOfType(UISelectAccount.class) ;
      UINavigationContainer uiNavigationContainer = uiPortlet.findFirstComponentOfType(UINavigationContainer.class) ;
      UIFolderContainer uiFolderContainer = uiNavigationContainer.getChild(UIFolderContainer.class) ;
      String accountId = uiSelectAcc.getSelectedValue() ;
      String usename = uiPortlet.getCurrentUser() ;
      MailService mailSvr = uiForm.getApplicationComponent(MailService.class) ;
      UIPopupAction uiChildPopup = uiForm.getAncestorOfType(UIPopupAction.class) ;
      Message message = uiForm.getNewMessage() ;      
      try {
        mailSvr.sendMessage(usename, message) ;
        uiChildPopup.deActivate() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;
      }catch (Exception e) {
        uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.send-mail-error", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        e.printStackTrace() ;
        return ;
      }
      try {
        MailSetting mailSetting = mailSvr.getMailSetting(usename);
        if (mailSetting.saveMessageInSent()) {
          message.setFolders(new String[]{Utils.createFolderId(accountId, Utils.FD_SENT, false)}) ;
          Folder folder = mailSvr.getFolder(usename, accountId, Utils.createFolderId(accountId, Utils.FD_SENT, false));
          folder.setTotalMessage(folder.getTotalMessage() + 1);
          mailSvr.saveFolder(usename, accountId, folder);
        }
        if (!uiForm.fromDrafts()) {
          mailSvr.saveMessage(usename, accountId, message, true) ;          
          Folder drafts = mailSvr.getFolder(usename, accountId, Utils.createFolderId(accountId, Utils.FD_DRAFTS, false));
          drafts.setTotalMessage(drafts.getTotalMessage() - 1);
          mailSvr.saveFolder(usename, accountId, drafts);
        } else {
          mailSvr.saveMessage(usename, accountId, message, false) ;
        }
      } catch (Exception e) {
        uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.save-sent-error", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        e.printStackTrace() ;
        uiChildPopup.deActivate() ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiFolderContainer) ;
      uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.send-mail-succsessfuly", null)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
    }
  }
  static public class SaveDraftActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiForm = event.getSource() ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      UIMailPortlet uiPortlet = uiForm.getAncestorOfType(UIMailPortlet.class) ;
      UISelectAccount uiSelectAcc = uiPortlet.findFirstComponentOfType(UISelectAccount.class) ;
      UINavigationContainer uiNavigationContainer = uiPortlet.findFirstComponentOfType(UINavigationContainer.class) ;
      UIFolderContainer uiFolderContainer = uiNavigationContainer.getChild(UIFolderContainer.class) ;
      String accountId = uiSelectAcc.getSelectedValue() ;
      String usename = uiPortlet.getCurrentUser() ;
      MailService mailSvr = uiForm.getApplicationComponent(MailService.class) ;
      UIPopupAction uiChildPopup = uiForm.getAncestorOfType(UIPopupAction.class) ;
      Message message = uiForm.getNewMessage() ;   
      try {
        message.setFolders(new String[]{Utils.createFolderId(accountId, Utils.FD_DRAFTS, false)}) ;
        if (! uiForm.fromDrafts()) {
          mailSvr.saveMessage(usename, accountId, message, true) ;
          Folder drafts = mailSvr.getFolder(usename, accountId, Utils.createFolderId(accountId, Utils.FD_DRAFTS, false));
          drafts.setTotalMessage(drafts.getTotalMessage() + 1);
          mailSvr.saveFolder(usename, accountId, drafts);
        } else {
          mailSvr.saveMessage(usename, accountId, message, false) ;
        }
      }
      catch (Exception e) {
        uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.save-draft-error", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        e.printStackTrace() ;
        uiChildPopup.deActivate() ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiFolderContainer) ;
      uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.save-mail-draff", null)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
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
      System.out.println(" ==== >>> Attachment Action Listener") ;
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
      BufferAttachment att = new BufferAttachment();
      for (Attachment attach : uiComposeForm.getAttachFileList()) {
        if (attach.getId().equals(attId)) {
          att = (BufferAttachment) attach ;
        }
      }
      if (att.getInputStream() != null) {
        System.out.println("===================================" + att.getInputStream().toString());
      } else {
        System.out.println("==== nulll" + att.getName());
      }
      ByteArrayInputStream bis = (ByteArrayInputStream) att.getInputStream();
      DownloadResource dresource = new InputStreamDownloadResource(bis, att.getMimeType());
      DownloadService dservice = (DownloadService)PortalContainer.getInstance().getComponentInstanceOfType(DownloadService.class);
      dresource.setDownloadName(att.getName());
      String downloadLink = dservice.getDownloadLink(dservice.addDownloadResource(dresource));
      event.getRequestContext().getJavascriptManager().addJavascript("ajaxRedirect('" + downloadLink + "');");
    }
  }
  
  static public class RemoveAttachmentActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiComposeForm = event.getSource() ;
      String attFileId = event.getRequestContext().getRequestParameter(OBJECTID);
      BufferAttachment attachfile = new BufferAttachment();
      for (Attachment att : uiComposeForm.attachments_) {
        if (att.getId().equals(attFileId)) {
          attachfile = (BufferAttachment) att;
        }
      }
      uiComposeForm.removeFromUploadFileList(attachfile);
      uiComposeForm.refreshUploadFileList() ;
    }
  }

  static public class ToActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiComposeForm = event.getSource() ;
      UIPopupActionContainer uiActionContainer = uiComposeForm.getAncestorOfType(UIPopupActionContainer.class) ;    
      UIPopupAction uiChildPopup = uiActionContainer.getChild(UIPopupAction.class) ;

      UIAddressForm uiAddressForm = uiChildPopup.activate(UIAddressForm.class, 700) ; 
      uiAddressForm.setRecipientsType(FIELD_TO);
      if (uiComposeForm.getToContacts() != null && uiComposeForm.getToContacts().size() > 0) {        
        uiAddressForm.setAlreadyCheckedContact(uiComposeForm.getToContacts());      
        uiAddressForm.setContactList();
      }
      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiActionContainer) ;
    }
  }
  static public class ToCCActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiComposeForm = event.getSource() ;
      UIPopupActionContainer uiActionContainer = uiComposeForm.getAncestorOfType(UIPopupActionContainer.class) ;    
      UIPopupAction uiChildPopup = uiActionContainer.getChild(UIPopupAction.class) ;
      UIAddressForm uiAddressForm = uiChildPopup.activate(UIAddressForm.class,700) ; 
      uiAddressForm.setRecipientsType(FIELD_CC);
      if (uiComposeForm.getCcContacts()!= null && uiComposeForm.getCcContacts().size()>0) {        
       uiAddressForm.setAlreadyCheckedContact(uiComposeForm.getCcContacts());      
        uiAddressForm.setContactList();
      }
      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiActionContainer) ;
    }
  }
  
  static public class ToBCCActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiComposeForm = event.getSource() ;
      System.out.println(" === >>> ToBccActionListener") ;
      
      UIPopupActionContainer uiActionContainer = uiComposeForm.getAncestorOfType(UIPopupActionContainer.class) ;    
      UIPopupAction uiChildPopup = uiActionContainer.getChild(UIPopupAction.class) ;
      UIAddressForm uiAddressForm = uiChildPopup.activate(UIAddressForm.class,700) ; 
      
      uiAddressForm.setRecipientsType(FIELD_BCC);
      if (uiComposeForm.getCcContacts()!= null && uiComposeForm.getBccContacts().size()>0) {        
       uiAddressForm.setAlreadyCheckedContact(uiComposeForm.getBccContacts());      
        uiAddressForm.setContactList();
      }
      
      event.getRequestContext().addUIComponentToUpdateByAjax(uiActionContainer) ;
    }   
  }
  
  static public class ChangePriorityActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiForm = event.getSource() ;
      System.out.println(" === >>> Change Priority Action Listener") ;
      String priority = event.getRequestContext().getRequestParameter(OBJECTID) ;  
      uiForm.setPriority(Long.valueOf(priority));
    }
  }
  
  static public class UseVisualEdiorActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiForm = event.getSource() ;
      System.out.println(" === >>> Change Editor Action Listener") ;
      boolean isVisualEditor = Boolean.valueOf(event.getRequestContext().getRequestParameter(OBJECTID)) ;  
      String content = "";
      if (isVisualEditor) {
        content = uiForm.getUIFormTextAreaInput(FIELD_MESSAGECONTENT).getValue();
        uiForm.removeChildById(FIELD_MESSAGECONTENT);
        UIFormWYSIWYGInput wysiwyg = new UIFormWYSIWYGInput(FIELD_MESSAGECONTENT, null, null, true) ;
        uiForm.addUIFormInput(wysiwyg);
        wysiwyg.setValue(content);
      } else {
        content = uiForm.getChild(UIFormWYSIWYGInput.class).getValue();
        uiForm.removeChild(UIFormWYSIWYGInput.class) ;
        UIFormTextAreaInput textArea = new UIFormTextAreaInput(FIELD_MESSAGECONTENT, null, null);
        textArea.setValue(content);
        uiForm.addUIFormInput(textArea) ;
      }
      uiForm.setVisualEditor(isVisualEditor);
    }
  }
}
