/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS       All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.webui.popup;

import java.util.Date;

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
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
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
  private String categoryId; 
  private String forumId ;
  private String topicId ;
  public UITopicForm() throws Exception {
    UIFormStringInput topicTitle = new UIFormStringInput(FIELD_TOPICTITLE_INPUT, FIELD_TOPICTITLE_INPUT, null);
    //topicTitle.addValidator(EmptyNameValidator.class) ;
    UIFormTextAreaInput messenger = new UIFormTextAreaInput(FIELD_MESSENGER_TEXTAREA, FIELD_MESSENGER_TEXTAREA, null);
    
    addUIFormInput(topicTitle);
    addUIFormInput(messenger);
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
    }
  }
  
  
  static  public class PreviewThread extends EventListener<UITopicForm> {
    public void execute(Event<UITopicForm> event) throws Exception {
      UITopicForm uiForm = event.getSource() ;
      uiForm.getUIStringInput(FIELD_TOPICTITLE_INPUT).addValidator(EmptyNameValidator.class);
      String topicTitle = uiForm.getUIStringInput(FIELD_TOPICTITLE_INPUT).getValue().trim();
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
      uiForm.getUIStringInput(FIELD_TOPICTITLE_INPUT).addValidator(EmptyNameValidator.class);
      String topicTitle = uiForm.getUIStringInput(FIELD_TOPICTITLE_INPUT).getValue().trim();
      String messenger = uiForm.getUIFormTextAreaInput(FIELD_MESSENGER_TEXTAREA).getValue() ;
      
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
      topicNew.setPostCount(0);
      topicNew.setViewCount(0);
      
      topicNew.setIsNotifyWhenAddPost(false);
      topicNew.setIsModeratePost(false);
      topicNew.setIsClosed(false);
      topicNew.setIsLock(false);
      topicNew.setIsSticky(false);
      
      topicNew.setIcon("");
      topicNew.setAttachmentFirstPost(0) ;
      topicNew.setIsApproved(false);  
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
