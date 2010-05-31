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
package org.exoplatform.contact.webui.popup;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.webui.UIContactPortlet;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.MailSetting;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.Utils;
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
import org.exoplatform.webui.form.wysiwyg.FCKEditorConfig;
import org.exoplatform.webui.form.wysiwyg.UIFormWYSIWYGInput;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen <hung.nguyen@exoplatform.com>
 *          hung.hoang <hung.hoang@exoplatform.com>
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/contact/webui/popup/UIComposeForm.gtmpl",
    events = {
      @EventConfig(listeners = UIComposeForm.SendActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIComposeForm.DiscardChangeActionListener.class),
      @EventConfig(listeners = UIComposeForm.AttachmentActionListener.class),
      @EventConfig(listeners = UIComposeForm.DownloadActionListener.class),
      @EventConfig(listeners = UIComposeForm.RemoveAttachmentActionListener.class)
    }
)
public class UIComposeForm extends UIForm implements UIPopupComponent {
  final static public String FIELD_FROM_INPUT = "fromInput" ;
  final static public String FIELD_FROM = "from" ;
  
  final static public String FIELD_SUBJECT = "subject" ;
  final static public String FIELD_TO = "to" ;
  final static public String FIELD_ATTACHMENTS = "attachments" ;
  final static public String FIELD_MESSAGECONTENT = "messageContent" ;
  final static public String ACT_TO = "To" ;
  final static public String ACT_REMOVE = "remove" ;
  final static public int MESSAGE_NEW = 0;
 /* private List<Attachment> attachments_ = new ArrayList<Attachment>() ;
  private Message message_ = null;*/
  private Map<String, String> fromOptions = new LinkedHashMap<String, String>() ;
  private Boolean isVisualEditor = true;
  private int composeType_;  
  public List<Contact> toContacts = new ArrayList<Contact>();
  private boolean isCSMail = false ;

  public boolean isVisualEditor() { return isVisualEditor; }
  public void setVisualEditor(boolean b) { isVisualEditor = b; }
  
  public UIComposeForm() throws Exception { }
  
  public List<Contact> getToContacts(){ return toContacts; }
  
  public void setToContacts(List<Contact> contactList) { toContacts = contactList; }
  
  public int getComposeType() { return composeType_ ; }
  public void setComposeType(int t) { composeType_ = t; }

  public List<ActionData> getUploadFileList() {
    return null ;
  }
  
  public void refreshUploadFileList() throws Exception {
    UIFormInputWithActions inputSet = getChildById(FIELD_FROM_INPUT) ;
    inputSet.setActionField(FIELD_ATTACHMENTS, getUploadFileList()) ;
  }
  
  
  @SuppressWarnings("deprecation")
  public void init(List<Account> accs, String toEmails) throws Exception {
    fromOptions.clear() ;
    List<SelectItemOption<String>>  options = new ArrayList<SelectItemOption<String>>() ;
    if (accs != null && accs.size() > 0) { // use mail Cs
      isCSMail = true ;
//    improve later ;
      for(Account acc : accs) {
        String fromEmail = acc.getUserDisplayName() + " &lt;" + acc.getEmailAddress() + "&gt;" ;      
        options.add(new SelectItemOption<String>(fromEmail, fromEmail)) ;
        fromOptions.put(acc.getId(), acc.getUserDisplayName() + " <" + acc.getEmailAddress() + ">") ;
      }
    } else { // use mail portal
      isCSMail = false ;
      String email = "" ;
      String name = "" ;
      String userName = ContactUtils.getCurrentUser() ;
      if(!ContactUtils.isEmpty(userName)){
        name = ContactUtils.getFullName(userName) ;
        email = ContactUtils.getEmailUser(userName) ;
      }
      String fromEmail = name+ " &lt;" + email + "&gt;" ;      
      options.add(new SelectItemOption<String>(fromEmail, fromEmail)) ;
      fromOptions.put(userName, name + " <" + email + ">") ;
    }
    addUIFormInput(new UIFormSelectBox(FIELD_FROM, FIELD_FROM, options)) ;
    /*
    List<SelectItemOption<String>>  fromOptions = new ArrayList<SelectItemOption<String>>() ;
    for (String email : emails.split(org.exoplatform.contact.service.Utils.SEMI_COLON)) {
      fromOptions.add(new SelectItemOption<String>(email, email)) ;
    }
    addUIFormInput(new UIFormSelectBox(FIELD_TO, FIELD_TO, fromOptions)) ;*/
    UIFormStringInput fieldTo = new UIFormStringInput(FIELD_TO, null, null) ;
    fieldTo.setValue(toEmails.replaceAll(org.exoplatform.contact.service.Utils.SEMI_COLON, ",")) ;
    addUIFormInput(fieldTo) ;
    addUIFormInput(new UIFormStringInput(FIELD_SUBJECT, null, null)) ;
    UIFormInputWithActions inputSet = new UIFormInputWithActions(FIELD_FROM_INPUT);   
    inputSet.addUIFormInput(new UIFormInputInfo(FIELD_ATTACHMENTS, FIELD_ATTACHMENTS, null)) ;
    inputSet.setActionField(FIELD_ATTACHMENTS, getUploadFileList()) ;
    addUIFormInput(inputSet) ;
    isVisualEditor = true ;
    if (isVisualEditor) {
    	UIFormWYSIWYGInput uiFormWYSIWYGInput = new UIFormWYSIWYGInput(FIELD_MESSAGECONTENT, FIELD_MESSAGECONTENT, null);
        FCKEditorConfig fckconfig = new FCKEditorConfig();
        fckconfig.put("CustomConfigurationsPath", "/csResources/javascript/eXo/cs/fckconfig.js");
        uiFormWYSIWYGInput.setFCKConfig(fckconfig);
        addUIFormInput(uiFormWYSIWYGInput);    
    } else {
      addUIFormInput(new UIFormTextAreaInput(FIELD_MESSAGECONTENT, null, null)) ;
    }
  }

  public String getFieldFromValue() {
    return getUIFormSelectBox(FIELD_FROM).getValue() ;
  }

  public String getFieldSubjectValue() {
    String subject = getUIStringInput(FIELD_SUBJECT).getValue() ;
    if (subject !=null )
      return subject ;
    else return "" ;
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
  
  public void resetFields() { reset() ; }
  
  public void activate() throws Exception { }
  public void deActivate() throws Exception { }

  static public class SendActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiForm = event.getSource() ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      String to = uiForm.getFieldToValue() ;      
      if (ContactUtils.isEmpty(to)) {
        uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.to-field-empty", null,
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      to = to.replaceAll(";", ",") ;
      String EMAIL_REGEX = 
        "[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[_A-Za-z0-9-.]+";
      for (String email : to.split(",")) {
        if (!ContactUtils.isEmpty(email) && !email.trim().matches(EMAIL_REGEX)) {
          uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.invalid-email", null, 
              ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
      }
      if (uiForm.isCSMail) {
        UIPopupAction uiChildPopup = uiForm.getAncestorOfType(UIPopupAction.class) ;
        Message message = new Message() ;
        message.setSendDate(new Date()) ;
        String contentType = Utils.MIMETYPE_TEXTHTML;
        message.setContentType(contentType);
        message.setSubject(uiForm.getFieldSubjectValue()) ;
        message.setMessageTo(to) ;
  
        if (message.getReceivedDate() == null) {
          message.setReceivedDate(new Date());
        }
        message.setHasStar(false);
  
        //message.setAttachements(this.getAttachFileList()) ;
        String body = uiForm.getFieldContentValue() ;
        message.setMessageBody(body) ;
        message.setUnread(false);
        message.setSize(body.getBytes().length);
        String fieldFrom = uiForm.getFieldFromValue() ;
        message.setFrom(fieldFrom) ;
//      String accId = uiForm.fromOptions.keySet().toArray(new String[] {})[0] ;      
        String accId = null ;
        for (String key : uiForm.fromOptions.keySet()) {
          if (uiForm.fromOptions.get(key).equals(fieldFrom))
              accId = key ;
        }
        MailService mailSvr = uiForm.getApplicationComponent(MailService.class) ; 
        String username = ContactUtils.getCurrentUser() ;
        try {
          mailSvr.sendMessage(username, accId, message) ;
          uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.send-mail-succsessfuly", null)) ;
          uiChildPopup.deActivate() ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;
          
//        TODO cs-1141
          ContactUtils.getContactService().saveAddress(username, to) ;          
        }catch (Exception e) {
          uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.send-mail-error", null,
              ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        try {
          MailSetting setting = mailSvr.getMailSetting(username);
          if (setting.saveMessageInSent()) {
            message.setFolders(new String[]{ Utils.generateFID(accId, Utils.FD_SENT, false) }) ;
          }
          message.setReplyTo(message.getMessageTo()) ;
          mailSvr.saveMessage(username, accId, message.getPath(), message, true) ;
          uiChildPopup.deActivate() ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;
        } catch (Exception e) {
          uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.save-sent-error", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          uiChildPopup.deActivate() ;
        }
      } else { // portal send mail
        try {
          org.exoplatform.services.mail.Message  message = new org.exoplatform.services.mail.Message(); 
          message.setMimeType(Utils.MIMETYPE_TEXTHTML) ;
          message.setFrom(uiForm.getFieldFromValue()) ;
          message.setTo(to) ;
          message.setSubject(uiForm.getFieldSubjectValue()) ;
          message.setBody(uiForm.getFieldContentValue()) ;
          org.exoplatform.services.mail.MailService mService = uiForm.getApplicationComponent(org.exoplatform.services.mail.impl.MailServiceImpl.class) ;
          mService.sendMessage(message) ;
          // TODO cs-1141
          ContactUtils.getContactService().saveAddress(ContactUtils.getCurrentUser(), to) ;          
          
          //ContactUtils.sendMessage(message) ;
          /*uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.send-mail-succsessfuly", null, ApplicationMessage.INFO)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;*/
          UIContactPortlet portlet = uiForm.getAncestorOfType(UIContactPortlet.class) ;
          UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
          popupAction.deActivate() ;
          event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
        } catch(Exception e) {
          uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.send-mail-error", null, ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          e.printStackTrace() ;
          return ;
        } 
      }
    }
  }
  
  static public class DiscardChangeActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiForm = event.getSource() ;
      UIPopupAction uiPopupAction = uiForm.getAncestorOfType(UIPopupAction.class) ;
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }
  static public class AttachmentActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      /*UIComposeForm uiForm = event.getSource() ;
      UIPopupActionContainer uiActionContainer = uiForm.getAncestorOfType(UIPopupActionContainer.class) ;
      UIPopupAction uiChildPopup = uiActionContainer.getChild(UIPopupAction.class) ;
      uiChildPopup.activate(UIAttachFileForm.class, 600) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiActionContainer) ;*/
    }
  }
  
  static public class DownloadActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      /*UIComposeForm uiComposeForm = event.getSource();
      String attId = event.getRequestContext().getRequestParameter(OBJECTID);
      BufferAttachment att = new BufferAttachment();
      for (Attachment attach : uiComposeForm.getAttachFileList()) {
        if (attach.getId().equals(attId)) {
          att = (BufferAttachment) attach ;
        }
      }
      //ByteArrayInputStream bis = (ByteArrayInputStream) att.getInputStream();
      DownloadResource dresource = new InputStreamDownloadResource(att.getInputStream(), att.getMimeType());
      DownloadService dservice = (DownloadService)PortalContainer.getInstance().getComponentInstanceOfType(DownloadService.class);
      dresource.setDownloadName(att.getName());
      String downloadLink = dservice.getDownloadLink(dservice.addDownloadResource(dresource));
      event.getRequestContext().getJavascriptManager().addJavascript("ajaxRedirect('" + downloadLink + "');");*/
    }
  }
  
  static public class RemoveAttachmentActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
     /* UIComposeForm uiComposeForm = event.getSource() ;
      String attFileId = event.getRequestContext().getRequestParameter(OBJECTID);
      BufferAttachment attachfile = new BufferAttachment();
      for (Attachment att : uiComposeForm.attachments_) {
        if (att.getId().equals(attFileId)) {
          attachfile = (BufferAttachment) att;
        }
      }
      uiComposeForm.removeFromUploadFileList(attachfile);
      uiComposeForm.refreshUploadFileList() ;*/
    }
  }
  
}
