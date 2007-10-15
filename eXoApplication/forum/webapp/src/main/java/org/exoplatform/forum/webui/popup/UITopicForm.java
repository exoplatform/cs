/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS       All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.webui.EmptyNameValidator;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;

/**
 * Created by The eXo Platform SARL
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Aug 22, 2007  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/forum/webui/popup/UITopicForm.gtmpl",
    events = {
      @EventConfig(listeners = UITopicForm.PreviewThread.class), 
      @EventConfig(listeners = UITopicForm.SubmitThread.class), 
      @EventConfig(listeners = UITopicForm.CancelAction.class)
    }
)
public class UITopicForm extends UIForm implements UIPopupComponent {
  public static final String FIELD_TOPICTITLE_INPUT = "ThreadTitle" ;
  public static final String FIELD_MESSENGER_TEXTAREA = "Messenger" ;
  public static final String FIELD_TOPICSTATUS_SELECTBOX = "TopicStatus" ;
  public static final String FIELD_TOPICSTATE_SELECTBOX = "TopicState" ;
  
  public static final String FIELD_APPROVED_CHECKBOX = "Approved" ;
  public static final String FIELD_MODERATEPOST_CHECKBOX = "ModeratePost" ;
  public static final String FIELD_NOTIFYWHENADDPOST_CHECKBOX = "NotifyWhenAddPost" ;
  public static final String FIELD_STICKY_CHECKBOX = "Sticky" ;
  
  private String categoryId; 
  private String forumId ;
  private String topicId ;
  public UITopicForm() throws Exception {
    UIFormStringInput topicTitle = new UIFormStringInput(FIELD_TOPICTITLE_INPUT, FIELD_TOPICTITLE_INPUT, null);
    UIFormTextAreaInput messenger = new UIFormTextAreaInput(FIELD_MESSENGER_TEXTAREA, FIELD_MESSENGER_TEXTAREA, null);
    
    List<SelectItemOption<String>> ls = new ArrayList<SelectItemOption<String>>() ;
    ls.add(new SelectItemOption<String>("Open", "open")) ;
    ls.add(new SelectItemOption<String>("Closed", "closed")) ;
    UIFormSelectBox topicState = new UIFormSelectBox(FIELD_TOPICSTATE_SELECTBOX, FIELD_TOPICSTATE_SELECTBOX, ls) ;
    topicState.setDefaultValue("open");
    List<SelectItemOption<String>> ls1 = new ArrayList<SelectItemOption<String>>() ;
    ls1.add(new SelectItemOption<String>("UnLock", "unlock")) ;
    ls1.add(new SelectItemOption<String>("Locked", "locked")) ;
    UIFormSelectBox topicStatus = new UIFormSelectBox(FIELD_TOPICSTATUS_SELECTBOX, FIELD_TOPICSTATUS_SELECTBOX, ls1) ;
    topicStatus.setDefaultValue("unlock");
    
    UIFormCheckBoxInput approved = new UIFormCheckBoxInput<Boolean>(FIELD_APPROVED_CHECKBOX, FIELD_APPROVED_CHECKBOX, false);
    UIFormCheckBoxInput moderatePost = new UIFormCheckBoxInput<Boolean>(FIELD_MODERATEPOST_CHECKBOX, FIELD_MODERATEPOST_CHECKBOX, false);
    UIFormCheckBoxInput checkWhenAddPost = new UIFormCheckBoxInput<Boolean>(FIELD_NOTIFYWHENADDPOST_CHECKBOX, FIELD_NOTIFYWHENADDPOST_CHECKBOX, false);
    UIFormCheckBoxInput sticky = new UIFormCheckBoxInput<Boolean>(FIELD_STICKY_CHECKBOX, FIELD_STICKY_CHECKBOX, false);
    
    
    addUIFormInput(topicTitle);
    addUIFormInput(messenger);
    
    addUIFormInput(topicState);
    addUIFormInput(topicStatus);
    addUIFormInput(approved);
    addUIFormInput(moderatePost);
    addUIFormInput(checkWhenAddPost);
    addUIFormInput(sticky);
  }
  
  public void setTopicIds(String categoryId, String forumId) {
    this.categoryId = categoryId ;
    this.forumId = forumId ;
  }
  
  public void activate() throws Exception {
    // TODO Auto-generated method stub
    
  }
  public void deActivate() throws Exception {
    // TODO Auto-generated method stub
  }
  
  public String[] getActionsTopic() throws Exception {
    return (new String [] {"PreviewThread", "SubmitThread", "CancelAction"});
  }
  
  public void setUpdateTopic(Topic topic, boolean isUpdate) {
    if(isUpdate) {
      this.topicId = topic.getId() ;
      getUIStringInput(FIELD_TOPICTITLE_INPUT).setValue(topic.getTopicName());
      getUIFormTextAreaInput(FIELD_MESSENGER_TEXTAREA).setDefaultValue(topic.getDescription());
      String stat = "open";
      if(topic.getIsClosed()) stat = "closed";
      getUIFormSelectBox(FIELD_TOPICSTATE_SELECTBOX).setValue(stat);
      if(topic.getIsLock()) stat = "locked";
      else stat = "unlock";
      System.out.println("\n\n   " + stat + "\n\n");
      getUIFormSelectBox(FIELD_TOPICSTATUS_SELECTBOX).setValue(stat);
      
      getUIFormCheckBoxInput(FIELD_APPROVED_CHECKBOX).setChecked(topic.getIsApproved());
      getUIFormCheckBoxInput(FIELD_MODERATEPOST_CHECKBOX).setChecked(topic.getIsModeratePost());
      getUIFormCheckBoxInput(FIELD_NOTIFYWHENADDPOST_CHECKBOX).setChecked(topic.getIsNotifyWhenAddPost());
      getUIFormCheckBoxInput(FIELD_STICKY_CHECKBOX).setChecked(topic.getIsSticky());
    }
  }
  
  
  static  public class PreviewThread extends EventListener<UITopicForm> {
    public void execute(Event<UITopicForm> event) throws Exception {
      UITopicForm uiForm = event.getSource() ;
      UIFormStringInput stringInputTitle = uiForm.getUIStringInput(FIELD_TOPICTITLE_INPUT) ; 
      stringInputTitle.addValidator(EmptyNameValidator.class);
      String topicTitle = stringInputTitle.getValue().trim();
      String messenger = uiForm.getUIFormTextAreaInput(FIELD_MESSENGER_TEXTAREA).getValue() ;
      
      String userName = Util.getPortalRequestContext().getRemoteUser() ;
      
      Post postNew = new Post();
      postNew.setOwner(userName);
      postNew.setSubject(topicTitle);
      postNew.setCreatedDate(new Date());
      postNew.setModifiedBy(userName);
      postNew.setModifiedDate(new Date());
      postNew.setMessage(messenger);
      
      postNew.setIcon("");
      postNew.setNumberOfAttachment(0) ;
      
      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
      UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
      UIViewTopic viewTopic = popupAction.createUIComponent(UIViewTopic.class, null, null) ;
      viewTopic.setPostView(postNew) ;
      popupAction.activate(viewTopic, 670, 440) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  static  public class SubmitThread extends EventListener<UITopicForm> {
    public void execute(Event<UITopicForm> event) throws Exception {
      UITopicForm uiForm = event.getSource() ;
      UIFormStringInput stringInputTitle = uiForm.getUIStringInput(FIELD_TOPICTITLE_INPUT) ; 
      stringInputTitle.addValidator(EmptyNameValidator.class);
      String topicTitle = stringInputTitle.getValue().trim();
      String messenger = uiForm.getUIFormTextAreaInput(FIELD_MESSENGER_TEXTAREA).getValue() ;
      String topicState = uiForm.getUIFormSelectBox(FIELD_TOPICSTATE_SELECTBOX).getValue();
      String topicStatus = uiForm.getUIFormSelectBox(FIELD_TOPICSTATUS_SELECTBOX).getValue();
      
      Boolean approved = (Boolean)uiForm.getUIFormCheckBoxInput(FIELD_APPROVED_CHECKBOX).getValue();
      Boolean moderatePost = (Boolean)uiForm.getUIFormCheckBoxInput(FIELD_MODERATEPOST_CHECKBOX).getValue();
      Boolean whenNewPost = (Boolean)uiForm.getUIFormCheckBoxInput(FIELD_NOTIFYWHENADDPOST_CHECKBOX).getValue();
      Boolean sticky = (Boolean)uiForm.getUIFormCheckBoxInput(FIELD_STICKY_CHECKBOX).getValue();
      
      String userName = Util.getPortalRequestContext().getRemoteUser() ;
      Topic topicNew = new Topic();
      topicNew.setOwner(userName);
      topicNew.setTopicName(topicTitle);
      topicNew.setCreatedDate(new Date());
      topicNew.setModifiedBy(userName);
      topicNew.setModifiedDate(new Date());
      topicNew.setLastPostBy(userName);
      topicNew.setLastPostDate(new Date());
      topicNew.setDescription(messenger);
      
      topicNew.setIsNotifyWhenAddPost(whenNewPost);
      topicNew.setIsModeratePost(moderatePost);
      if(topicState.equals("closed")) {
        topicNew.setIsClosed(true);
      }
      if(topicStatus.equals("locked")) {
        topicNew.setIsLock(true) ;
      }
      topicNew.setIsSticky(sticky);
      topicNew.setIsApproved(approved);  
      topicNew.setIcon("");
      //topicNew.setAttachmentFirstPost(0) ;
      topicNew.setViewPermissions(new String[] {});
      topicNew.setEditPermissions(new String[] {});
      
      ForumService forumService =  (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
      if(uiForm.topicId != null && uiForm.topicId.length() > 0) {
        topicNew.setId(uiForm.topicId);
        forumService.saveTopic(uiForm.categoryId, uiForm.forumId, topicNew, false);
      } else {
        forumService.saveTopic(uiForm.categoryId, uiForm.forumId, topicNew, true);
      }
      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
      forumPortlet.cancelAction() ;
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
      context.addUIComponentToUpdateByAjax(forumPortlet) ;
    }
  }
  
  static  public class CancelAction extends EventListener<UITopicForm> {
    public void execute(Event<UITopicForm> event) throws Exception {
      UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
      forumPortlet.cancelAction() ;
    }
  }
  
}
