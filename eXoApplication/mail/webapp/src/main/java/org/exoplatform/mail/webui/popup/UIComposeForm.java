/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.mail.webui.Utils;
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
  final static public String FIELD_TO = "to" ;
  final static public String FIELD_CC = "cc" ;
  final static public String FIELD_BCC = "bcc" ;
  final static public String FIELD_SUBJECT = "subject" ;
  final static public String FIELD_ATTACHMENTS = "attachments" ;
  final static public String FIELD_MESSAGECONTENT = "messageContent" ;
  final static public String ACT_TO = "To" ;
  final static public String ACT_CC = "ToCC" ;
  final static public String ACT_BCC = "ToBCC" ;
  final static public String ACT_REMOVE = "remove" ;
  List<ActionData> uploadedFiles_ = new ArrayList<ActionData>() ;
  
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
    
    
    

    inputSet.addUIFormInput(new UIFormStringInput(FIELD_TO, null, null)) ;
    inputSet.addUIFormInput(new UIFormStringInput(FIELD_CC, null, null)) ;
    inputSet.addUIFormInput(new UIFormStringInput(FIELD_BCC, null, null)) ;
    inputSet.addUIFormInput(new UIFormInputInfo(FIELD_ATTACHMENTS, FIELD_ATTACHMENTS, null)) ;
   
    inputSet.setActionField(FIELD_ATTACHMENTS, getUploadFileList()) ;

    addUIFormInput(inputSet) ;
    addUIFormInput(new UIFormTextAreaInput(FIELD_MESSAGECONTENT, null, null)) ;
  }

  public List<ActionData> getUploadFileList() { 
    return uploadedFiles_ ;
  }
  public void setUploadFileList(String fileName, String fileSize,String mimeType) throws Exception {
    ActionData fileUpload = new ActionData() ;
    fileUpload.setActionListener("") ;
    fileUpload.setActionType(ActionData.TYPE_ICON) ;
    fileUpload.setCssIconClass("AttachmentIcon ZipFileIcon") ;
    fileUpload.setActionName(fileName + " ("+fileSize+")" ) ;
    fileUpload.setShowLabel(true) ;
    uploadedFiles_.add(fileUpload) ;
    ActionData removeAction = new ActionData() ;
    removeAction.setActionListener("RemoveAttachment") ;
    removeAction.setActionName(ACT_REMOVE) ;
    removeAction.setActionType(ActionData.TYPE_LINK) ;
    removeAction.setBreakLine(true) ;
    uploadedFiles_.add(removeAction) ;
    UIFormInputWithActions inputSet = getChildById(FIELD_FROM_INPUT) ;
    inputSet.setActionField(FIELD_ATTACHMENTS, uploadedFiles_) ;
  }
  public String getFieldFromValue() {
    UIFormInputWithActions inputSet = getChildById(FIELD_FROM_INPUT) ;
    return inputSet.getUIFormSelectBox(FIELD_FROM).getValue() ;
  }
  
  public void setFieldFromValue(List<SelectItemOption<String>> options) {
    UIFormInputWithActions inputSet = getChildById(FIELD_FROM_INPUT) ;
    inputSet.getUIFormSelectBox(FIELD_FROM).setOptions(options) ;
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
