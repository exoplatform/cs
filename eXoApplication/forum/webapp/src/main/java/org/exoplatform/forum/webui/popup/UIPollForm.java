/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Poll;
import org.exoplatform.forum.webui.EmptyNameValidator;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicDetail;
import org.exoplatform.forum.webui.UITopicDetailContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormMultiValueInputSet;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/forum/webui/popup/UIPollForm.gtmpl",
    events = {
      @EventConfig(listeners = UIPollForm.SaveActionListener.class), 
      @EventConfig(listeners = UIPollForm.RefreshActionListener.class),
      @EventConfig(listeners = UIPollForm.CancelActionListener.class)
    }
)
public class UIPollForm extends UIForm implements UIPopupComponent {
  public static final String FIELD_QUESTION_INPUT = "Question" ;
  final static public String FIELD_OPTIONS = "Option" ;
  public static final String FIELD_TIMEOUT_INPUT = "TimeOut" ;
  public static final String FIELD_MULTIVOTE_CHECKBOX = "MultiVote" ;
  public UIFormMultiValueInputSet uiFormMultiValue = null ;
  
  private String TopicPath ;
  
  @SuppressWarnings("unchecked")
  public UIPollForm() throws Exception {
    UIFormStringInput question = new UIFormStringInput(FIELD_QUESTION_INPUT, FIELD_QUESTION_INPUT, null);
    UIFormStringInput timeOut = new UIFormStringInput(FIELD_TIMEOUT_INPUT, FIELD_TIMEOUT_INPUT, null);
    UIFormCheckBoxInput checkBox = new UIFormCheckBoxInput<Boolean>(FIELD_MULTIVOTE_CHECKBOX, FIELD_MULTIVOTE_CHECKBOX, false) ; 
    addUIFormInput(question) ;
    addUIFormInput(timeOut) ;
    addUIFormInput(checkBox);
  }

  private void initMultiValuesField() throws Exception {
    if( uiFormMultiValue != null ) removeChildById(FIELD_OPTIONS);
    uiFormMultiValue = createUIComponent(UIFormMultiValueInputSet.class, null, null) ;
    uiFormMultiValue.setId(FIELD_OPTIONS) ;
    uiFormMultiValue.setName(FIELD_OPTIONS) ;
    uiFormMultiValue.setType(UIFormStringInput.class) ;
    List<String> list = new ArrayList<String>() ;
    list.add("");
    list.add("");
    uiFormMultiValue.setValue(list) ;
    addUIFormInput(uiFormMultiValue) ;
  }
  
  public void setTopicPath( String topicPath) {
    this.TopicPath = topicPath ;
  }
  
  public void activate() throws Exception {
    // TODO Auto-generated method stub
    this.initMultiValuesField();
  }
  public void deActivate() throws Exception {
    // TODO Auto-generated method stub
    
  }
  
  static  public class SaveActionListener extends EventListener<UIPollForm> {
    public void execute(Event<UIPollForm> event) throws Exception {
      UIPollForm uiForm = event.getSource() ;
      UIFormStringInput questionInput = uiForm.getUIStringInput(FIELD_QUESTION_INPUT) ;
      //questionInput.addValidator(EmptyNameValidator.class) ;
      String question = questionInput.getValue() ;
      String timeOutStr = uiForm.getUIStringInput(FIELD_TIMEOUT_INPUT).getValue() ;
      long timeOut = 0;
      if(timeOutStr != null && timeOutStr.length() > 0) timeOut = Long.getLong(timeOutStr) ; 
      boolean isMulti = uiForm.getUIFormCheckBoxInput(FIELD_MULTIVOTE_CHECKBOX).isChecked() ;
      String sms = "";
      int i = 0 ; 
      List values = uiForm.uiFormMultiValue.getValue();
      String temp = "" ;
      String[] options = new String[values.size()] ;  
      if(values != null && values.size() > 0) {
        for(Object value : values) {
          temp = (String)value ;
          if(temp != null && temp.length() > 0){
            options[i] = temp;
          } 
          ++i;
        }
      }
      int sizeOption = options.length;
      if(sizeOption < 2) sms = "Minimum" ;
      if(sizeOption > 10) sms = "Maximum" ;
      if(question == null || question.length() == 0) {
        sms = "NotQuestion";
        sizeOption = 0;
      }
      if(sizeOption >= 2 && sizeOption <= 10) {
        String userName = Util.getPortalRequestContext().getRemoteUser() ;
        String[] vote = new String[sizeOption]  ;
        for (int j = 0; j < sizeOption; j++) {
          vote[j] = "0";
        }
        Poll poll = new Poll() ;
        poll.setOwner(userName) ;
        poll.setQuestion(question) ;
        poll.setCreatedDate(new Date());
        poll.setModifiedBy(userName) ;
        poll.setModifiedDate(new Date()) ;
        poll.setIsPublic(isMulti) ;
        poll.setOption(options) ;
        poll.setVote(vote) ;
        poll.setTimeOut(timeOut) ;
        poll.setUserVote("") ;
        
        String[] id = uiForm.TopicPath.trim().split("/") ;
        ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
        forumService.savePoll(id[id.length - 3], id[id.length - 2], id[id.length - 1], poll, true, false) ;
        UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
        forumPortlet.cancelAction() ;
        UITopicDetailContainer detailContainer = forumPortlet.findFirstComponentOfType(UITopicDetailContainer.class) ;
        detailContainer.setRederPoll(true) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(detailContainer);
        
      }
      if(sms != null && sms.length() > 0) {
        Object[] args = { };
        throw new MessageException(new ApplicationMessage("UIPollForm.msg." + sms, args, ApplicationMessage.WARNING)) ;
      }
    }
  }

  static  public class RefreshActionListener extends EventListener<UIPollForm> {
    public void execute(Event<UIPollForm> event) throws Exception {
      UIPollForm uiForm = event.getSource() ;
      uiForm.initMultiValuesField();
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIPollForm> {
    public void execute(Event<UIPollForm> event) throws Exception {
      UIPollForm uiForm = event.getSource() ;
      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
      forumPortlet.cancelAction() ;
    }
  }
}
