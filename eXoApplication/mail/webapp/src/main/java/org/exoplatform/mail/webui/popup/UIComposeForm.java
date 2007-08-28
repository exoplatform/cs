/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.mail.service.Attachment;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.webui.UIFolderContainer;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.mail.webui.UINavigationContainer;
import org.exoplatform.mail.webui.UISelectAccount;
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
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
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
      @EventConfig(listeners = UIComposeForm.RemoveAttachmentActionListener.class),
      @EventConfig(listeners = UIComposeForm.PriorityActionListener.class),
      @EventConfig(listeners = UIComposeForm.SelectContactActionListener.class),
      @EventConfig(listeners = UIComposeForm.ToCCActionListener.class),
      @EventConfig(listeners = UIComposeForm.ToBCCActionListener.class)
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
  List<Attachment> attachments_ = new ArrayList<Attachment>() ;

  public UIComposeForm() throws Exception {
    UIFormInputWithActions inputSet = new UIFormInputWithActions(FIELD_FROM_INPUT); 
    List<SelectItemOption<String>>  options = new ArrayList<SelectItemOption<String>>() ;
    inputSet.addUIFormInput(new UIFormSelectBox(FIELD_FROM, FIELD_FROM, options)) ;
    List<ActionData> actions = new ArrayList<ActionData>() ;
    ActionData toAction = new ActionData() ;
    toAction.setActionListener(ACT_TO) ;
    toAction.setActionType(ActionData.TYPE_LINK) ;
    toAction.setActionName(ACT_TO);    
    actions.add(toAction);
    inputSet.setActionField(FIELD_TO, actions) ;

    actions = new ArrayList<ActionData>() ;
    ActionData ccAction = new ActionData() ;
    ccAction.setActionListener(ACT_CC) ;
    ccAction.setActionType(ActionData.TYPE_LINK) ;
    ccAction.setActionName(ACT_CC);
    actions.add(ccAction);
    inputSet.setActionField(FIELD_CC, actions) ;

    actions = new ArrayList<ActionData>() ;
    ActionData bccAction = new ActionData() ;
    bccAction.setActionListener(ACT_BCC) ;
    bccAction.setActionType(ActionData.TYPE_LINK) ;
    bccAction.setActionName(ACT_BCC);    
    actions.add(bccAction);
    inputSet.setActionField(FIELD_BCC, actions) ;

    inputSet.addUIFormInput(new UIFormStringInput(FIELD_SUBJECT, null, null)) ;
    inputSet.addUIFormInput(new UIFormStringInput(FIELD_TO, null, null)) ;
    inputSet.addUIFormInput(new UIFormStringInput(FIELD_CC, null, null)) ;
    inputSet.addUIFormInput(new UIFormStringInput(FIELD_BCC, null, null)) ;
    inputSet.addUIFormInput(new UIFormInputInfo(FIELD_ATTACHMENTS, FIELD_ATTACHMENTS, null)) ;

    inputSet.setActionField(FIELD_ATTACHMENTS, getUploadFileList()) ;

    addUIFormInput(inputSet) ;
    addUIFormInput(new UIFormTextAreaInput(FIELD_MESSAGECONTENT, null, null)) ;
  }

  public List<ActionData> getUploadFileList() { 
    List<ActionData> uploadedFiles = new ArrayList<ActionData>() ;
    for(Attachment attachdata : attachments_) {
      ActionData fileUpload = new ActionData() ;
      fileUpload.setActionListener("") ;
      fileUpload.setActionType(ActionData.TYPE_ICON) ;
      fileUpload.setCssIconClass("AttachmentIcon ZipFileIcon") ;
      fileUpload.setActionName(attachdata.getName() + " ("+attachdata.getSize()+" Kb)" ) ;
      fileUpload.setShowLabel(true) ;
      uploadedFiles.add(fileUpload) ;
      ActionData removeAction = new ActionData() ;
      removeAction.setActionListener("RemoveAttachment") ;
      removeAction.setActionName(ACT_REMOVE) ;
      removeAction.setActionType(ActionData.TYPE_LINK) ;
      removeAction.setBreakLine(true) ;
      uploadedFiles.add(removeAction) ;
    }
    return uploadedFiles ;
  }
  public void refreshUploadFileList() throws Exception {
    UIFormInputWithActions inputSet = getChildById(FIELD_FROM_INPUT) ;
    inputSet.setActionField(FIELD_ATTACHMENTS, getUploadFileList()) ;
  }
  public void addUploadFileList(Attachment attachfile) {
    attachments_.add(attachfile) ;
  }
  public void removeUploadFileList() {
    attachments_.clear() ;
  }
  public List<Attachment> getAttachFileList() {
    return attachments_ ;
  }
  public String getFieldFromValue() {
    UIFormInputWithActions inputSet = getChildById(FIELD_FROM_INPUT) ;
    return inputSet.getUIFormSelectBox(FIELD_FROM).getValue() ;
  }

  public void setFieldFromValue(List<SelectItemOption<String>> options) {
    UIFormInputWithActions inputSet = getChildById(FIELD_FROM_INPUT) ;
    inputSet.getUIFormSelectBox(FIELD_FROM).setOptions(options) ;
  }

  public String getFieldSubjectValue() {
    UIFormInputWithActions inputSet = getChildById(FIELD_FROM_INPUT) ;
    return inputSet.getUIStringInput(FIELD_SUBJECT).getValue() ;
  }
  public void setFieldSubjectValue(String value) {
    UIFormInputWithActions inputSet = getChildById(FIELD_FROM_INPUT) ;
    inputSet.getUIFormSelectBox(FIELD_SUBJECT).setValue(value) ;
  }
  public String getFieldToValue() {
    UIFormInputWithActions inputSet = getChildById(FIELD_FROM_INPUT) ;
    return inputSet.getUIStringInput(FIELD_TO).getValue() ;
  }

  public String getFieldCcValue() {
    UIFormInputWithActions inputSet = getChildById(FIELD_FROM_INPUT) ;
    return inputSet.getUIStringInput(FIELD_CC).getValue() ;
  }

  public String getFieldBccValue() {
    UIFormInputWithActions inputSet = getChildById(FIELD_FROM_INPUT) ;
    return inputSet.getUIStringInput(FIELD_BCC).getValue() ;
  }

  public String getFieldAttachmentsValue() {
    UIFormInputWithActions inputSet = getChildById(FIELD_FROM_INPUT) ;
    return inputSet.getUIFormInputInfo(FIELD_ATTACHMENTS).getValue() ;
  }
  public UIFormTextAreaInput getFieldMessageContent() {
    return getUIFormTextAreaInput(FIELD_MESSAGECONTENT) ;
  }
  public String getFieldMessageContentValue() {
    return getFieldMessageContent().getValue() ;
  }
  public void resetFields() {
    reset() ;
  }
  public void activate() throws Exception {
    // TODO Auto-generated method stub

  }
  public void deActivate() throws Exception {
    // TODO Auto-generated method stub

  }

  static  public class SendActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiForm = event.getSource() ;
      System.out.println(" ==========> SendActionListener") ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      if(Utils.isEmptyField(uiForm.getFieldToValue())) {
        uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.to-field-required", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      String from = uiForm.getFieldFromValue() ;
      String subject = uiForm.getFieldSubjectValue() ;
      String to = uiForm.getFieldToValue() ;
      String cc = uiForm.getFieldCcValue() ;
      String bcc = uiForm.getFieldBccValue() ;
      String body = uiForm.getFieldMessageContentValue() ;
      UIMailPortlet uiPortlet = uiForm.getAncestorOfType(UIMailPortlet.class) ;
      UISelectAccount uiSelectAcc = uiPortlet.findFirstComponentOfType(UISelectAccount.class) ;
      UINavigationContainer uiNavigationContainer = uiPortlet.findFirstComponentOfType(UINavigationContainer.class) ;
      UIFolderContainer uiFolderContainer = uiNavigationContainer.getChild(UIFolderContainer.class) ;
      String accountId = uiSelectAcc.getSelectedValue() ;
      String usename = uiPortlet.getCurrentUser() ;
      MailService mailSvr = uiForm.getApplicationComponent(MailService.class) ;
      UIPopupAction uiChildPopup = uiForm.getAncestorOfType(UIPopupAction.class) ;
      try {
        Message message = new Message() ;
        message.setAccountId(accountId) ;
        message.setAttachements(uiForm.getAttachFileList()) ;
        message.setFrom(from) ;
        message.setSubject(subject) ;
        message.setMessageTo(to) ;
        message.setMessageCc(cc) ;
        message.setMessageBcc(bcc) ;
        message.setMessageBody(body) ;
        mailSvr.sendMessage(usename, message) ;
        uiChildPopup.deActivate() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;
      }catch (Exception e) {
        uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.send-mail-error", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        e.printStackTrace() ;
        return ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiFolderContainer) ;
      uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.send-mail-succsessfuly", null)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
    }
  }
  static  public class SaveDraftActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiForm = event.getSource() ;
      System.out.println(" ==========> SaveDraftActionListener") ;
    }
  }
  static  public class DiscardChangeActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiForm = event.getSource() ;
      uiForm.resetFields() ;
      UIMailPortlet mailPortlet = event.getSource().getAncestorOfType(UIMailPortlet.class) ;
      mailPortlet.cancelAction() ;
    }
  }
  static  public class AttachmentActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiForm = event.getSource() ;
      System.out.println(" ==========> AttachmentActionListener") ;
      UIPopupActionContainer uiActionContainer = uiForm.getAncestorOfType(UIPopupActionContainer.class) ;
      UIPopupAction uiChildPopup = uiActionContainer.getChild(UIPopupAction.class) ;
      uiChildPopup.activate(UIAttachFileForm.class, 500) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiActionContainer) ;
    }
  }
  static  public class RemoveAttachmentActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiForm = event.getSource() ;
      System.out.println(" ==========> RemoveAttachmentActionListener") ;
    }
  }
  static  public class PriorityActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiForm = event.getSource() ;
      System.out.println(" ==========> PriorityActionListener") ;
    }
  }
  static  public class SelectContactActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiForm = event.getSource() ;
      System.out.println(" ==========> SelectContactActionListener") ;
    }
  }
  static  public class ToActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiForm = event.getSource() ;
      System.out.println(" ==========> ToActionListener") ;
    }
  }
  static  public class ToCCActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiForm = event.getSource() ;
      System.out.println(" ==========> ToCCActionListener") ;
    }
  }
  static  public class ToBCCActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiForm = event.getSource() ;
      System.out.println(" ==========> ToBCCActionListener") ;
    }
  }
}
