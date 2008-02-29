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
import java.util.List;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.ServerConfiguration;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormInputWithActions;
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
  private Boolean isVisualEditor = true;
  private int composeType_;  
  public List<Contact> toContacts = new ArrayList<Contact>();
  
  public boolean isVisualEditor() { return isVisualEditor; }
  public void setVisualEditor(boolean b) { isVisualEditor = b; }
  public UIComposeForm() throws Exception { }
  public List<Contact> getToContacts(){ return toContacts; }
  
  public void setToContacts(List<Contact> contactList) { toContacts = contactList; }
  
  public int getComposeType() { return composeType_ ; }
  public void setComposeType(int t) { composeType_ = t; }

  public List<ActionData> getUploadFileList() { 
/*    List<ActionData> uploadedFiles = new ArrayList<ActionData>() ;
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
    return uploadedFiles ;*/
    return null ;
  }
  
  public void refreshUploadFileList() throws Exception {
    UIFormInputWithActions inputSet = getChildById(FIELD_FROM_INPUT) ;
    inputSet.setActionField(FIELD_ATTACHMENTS, getUploadFileList()) ;
  }
  
  public void init(String emails) throws Exception {
    addUIFormInput(new UIFormStringInput(FIELD_FROM, null, ContactUtils.getCurrentEmail())) ;
    addUIFormInput(new UIFormStringInput(FIELD_TO, null, emails)) ;
    addUIFormInput(new UIFormStringInput(FIELD_SUBJECT, null, null)) ;
    UIFormInputWithActions inputSet = new UIFormInputWithActions(FIELD_FROM_INPUT);   
    inputSet.addUIFormInput(new UIFormInputInfo(FIELD_ATTACHMENTS, FIELD_ATTACHMENTS, null)) ;
    inputSet.setActionField(FIELD_ATTACHMENTS, getUploadFileList()) ;
    addUIFormInput(inputSet) ;
    isVisualEditor = true ;
    if (isVisualEditor) {
      addUIFormInput(new UIFormWYSIWYGInput(FIELD_MESSAGECONTENT, null, null, true));    
    } else {
      addUIFormInput(new UIFormTextAreaInput(FIELD_MESSAGECONTENT, null, null)) ;
    }
  }
  
  
  
 /* public void addToUploadFileList(Attachment attachfile) {
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
*/

  
  public String getFieldFromValue() {
    return getUIStringInput(FIELD_FROM).getValue() ;
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
      String subject = uiForm.getFieldSubjectValue() ;
      String content = uiForm.getFieldContentValue() ;
      
      if (ContactUtils.isEmpty(to)) {
        uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.to-field-empty", null,
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } else if (ContactUtils.isEmpty(subject)){
        uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.subject-field-empty", null,
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } else if (ContactUtils.isEmpty(content)){
        uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.content-field-empty", null,
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      MailService mailSvr = uiForm.getApplicationComponent(MailService.class) ;
      UIPopupAction uiChildPopup = uiForm.getAncestorOfType(UIPopupAction.class) ;
      Message message = new Message() ;
      message.setSendDate(new Date()) ;

      message.setFrom(uiForm.getFieldFromValue()) ;
      String contentType = Utils.MIMETYPE_TEXTHTML;
      message.setContentType(contentType);
      message.setSubject(subject) ;
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
      List<Message> msgList = new ArrayList<Message>() ;
      msgList.add(message) ;
      ServerConfiguration serverConfig = new ServerConfiguration() ;
      serverConfig.setOutgoingHost("smtp.gmail.com");
      serverConfig.setOutgoingPort("465");
      serverConfig.setSsl(true);
      serverConfig.setUserName("exomailtest@gmail.com");
      serverConfig.setPassword("exoadmin") ;
      try {
        mailSvr.sendMessages(msgList, serverConfig);
        uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.send-mail-succsessfuly", null)) ;
        uiChildPopup.deActivate() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;
      }catch (Exception e) {
        uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.send-mail-error", null,
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        e.printStackTrace() ;
        return ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
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
