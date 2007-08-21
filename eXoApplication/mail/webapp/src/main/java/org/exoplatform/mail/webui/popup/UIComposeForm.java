/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui.popup;

//import org.exoplatform.ecm.webui.component.UIFormWYSIWYGInput;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIDropDownControl;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;

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
      @EventConfig(listeners = UIComposeForm.DiscardChangeActionListener.class),
      @EventConfig(listeners = UIComposeForm.AttachmentActionListener.class),
      @EventConfig(listeners = UIComposeForm.RemoveAttachmentActionListener.class),
      @EventConfig(listeners = UIComposeForm.PriorityActionListener.class),
      @EventConfig(listeners = UIComposeForm.SelectContactActionListener.class)
    }
)
public class UIComposeForm extends UIForm implements UIPopupComponent{
  final static public String FIELD_FROM = "from" ;
  final static public String FIELD_TO = "to" ;
  final static public String FIELD_CC = "cc" ;
  final static public String FIELD_BCC = "bcc" ;
  final static public String FIELD_SUBJECT = "subject" ;
  final static public String FIELD_ATTACHMENTS = "attachments" ;
  final static public String FIELD_MESSAGECONTENT = "messageContent" ;
  
  public UIComposeForm() throws Exception {
    addChild(UIDropDownControl.class, null, null) ;
    addUIFormInput(new UIFormStringInput(FIELD_TO, null, null)) ;
    addUIFormInput(new UIFormStringInput(FIELD_CC, null, null)) ;
    addUIFormInput(new UIFormStringInput(FIELD_BCC, null, null)) ;
    addUIFormInput(new UIFormInputInfo(FIELD_ATTACHMENTS, null, null)) ;
    addUIFormInput(new UIFormTextAreaInput(FIELD_MESSAGECONTENT, null, null)) ;
    //addUIFormInput(new UIFormWYSIWYGInput(FIELD_MESSAGECONTENT, FIELD_MESSAGECONTENT, "basic" )) ;
  }
  protected String getFieldFromValue() {
    return getChild(UIDropDownControl.class).getValue() ;
  }
  
  public UIFormStringInput getFieldTo() {
    return getUIStringInput(FIELD_TO) ;
  }
  protected String getFieldToValue() {
    return getFieldTo().getValue() ;
  }
  public UIFormStringInput getFieldCc() {
    return getUIStringInput(FIELD_CC) ;
  }
  protected String getFieldCcValue() {
    return getFieldCc().getValue() ;
  }
  public UIFormStringInput getFieldBcc() {
    return getUIStringInput(FIELD_BCC) ;
  }
  public UIFormInputInfo getFieldAttachments() {
    return getUIFormInputInfo(FIELD_ATTACHMENTS) ;
  }
  public UIFormTextAreaInput getFieldMessageContent() {
    return getUIFormTextAreaInput(FIELD_MESSAGECONTENT) ;
  }
  /*public UIFormWYSIWYGInput getFieldMessageContent() {
    return (UIFormWYSIWYGInput)getChildById(FIELD_MESSAGECONTENT) ;
  }*/
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
      UIMailPortlet mailPortlet = event.getSource().getAncestorOfType(UIMailPortlet.class) ;
      mailPortlet.cancelAction() ;
    }
  }
  static  public class AttachmentActionListener extends EventListener<UIComposeForm> {
    public void execute(Event<UIComposeForm> event) throws Exception {
      UIComposeForm uiForm = event.getSource() ;
      System.out.println(" ==========> AttachmentActionListener") ;
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
  
  
}
