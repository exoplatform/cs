/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.service.BufferAttachment;
import org.exoplatform.forum.service.ForumAttachment;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.webui.EmptyNameValidator;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicDetailContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputIconSelector;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormInputWithActions;
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
    template = "app:/templates/forum/webui/popup/UIPostForm.gtmpl",
    events = {
      @EventConfig(listeners = UIPostForm.PreviewPostActionListener.class), 
      @EventConfig(listeners = UIPostForm.SubmitPostActionListener.class), 
      @EventConfig(listeners = UIPostForm.AttachmentActionListener.class), 
      @EventConfig(listeners = UIPostForm.RemoveAttachmentActionListener.class), 
      @EventConfig(listeners = UIPostForm.CancelActionListener.class)
    }
)
public class UIPostForm extends UIForm implements UIPopupComponent {
  private ForumService forumService =  (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
  public static final String FIELD_POSTTITLE_INPUT = "PostTitle" ;
  public static final String FIELD_MESSENGER_TEXTAREA = "Messenger" ;
  public static final String FIELD_LABEL_QUOTE = "ReUser" ;
  
  final static public String ACT_REMOVE = "remove" ;
  final static public String FIELD_ATTACHMENTS = "attachments" ;
  final static public String FIELD_FROM_INPUT = "fromInput" ;
  
  private List<ForumAttachment> attachments_ = new ArrayList<ForumAttachment>() ;
  private String categoryId; 
  private String forumId ;
  private String topicId ;
  private String postId ;
  private boolean quote = false ;
  public UIPostForm() throws Exception {
    UIFormStringInput postTitle = new UIFormStringInput(FIELD_POSTTITLE_INPUT, FIELD_POSTTITLE_INPUT, null);
    postTitle.addValidator(EmptyNameValidator.class) ;
    UIFormTextAreaInput messenger = new UIFormTextAreaInput(FIELD_MESSENGER_TEXTAREA, FIELD_MESSENGER_TEXTAREA, null);
    messenger.addValidator(EmptyNameValidator.class) ;
    addUIFormInput(postTitle);
    addUIFormInput(messenger);
    
    UIFormInputIconSelector uiIconSelector = new UIFormInputIconSelector("Icon", "Icon") ;
    addUIFormInput(uiIconSelector) ;
    
    
    UIFormInputWithActions inputSet = new UIFormInputWithActions(FIELD_FROM_INPUT); 
    inputSet.addUIFormInput(new UIFormInputInfo(FIELD_ATTACHMENTS, FIELD_ATTACHMENTS, null)) ;
    inputSet.setActionField(FIELD_FROM_INPUT, getUploadFileList()) ;
    addUIFormInput(inputSet) ;
  }
  
  public void setPostIds(String categoryId, String forumId, String topicId) {
    this.categoryId = categoryId ;
    this.forumId = forumId ;
    this.topicId = topicId ;
  }
  
  
  public List<ActionData> getUploadFileList() { 
    List<ActionData> uploadedFiles = new ArrayList<ActionData>() ;
    for(ForumAttachment attachdata : attachments_) {
      ActionData fileUpload = new ActionData() ;
      fileUpload.setActionListener("") ;
      fileUpload.setActionType(ActionData.TYPE_ICON) ;
      fileUpload.setCssIconClass("AttachmentIcon ZipFileIcon") ;
      fileUpload.setActionName(attachdata.getName() + " ("+attachdata.getSize()+" Kb)" ) ;
      fileUpload.setShowLabel(true) ;
      uploadedFiles.add(fileUpload) ;
      ActionData removeAction = new ActionData() ;
      removeAction.setActionListener("RemoveAttachment") ;
      removeAction.setActionName(ACT_REMOVE);
      removeAction.setActionParameter(attachdata.getId());
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
  public void addToUploadFileList(ForumAttachment attachfile) {
    attachments_.add(attachfile) ;
  }
  public void removeFromUploadFileList(ForumAttachment attachfile) {
    attachments_.remove(attachfile);
  }  
  public void removeUploadFileList() {
    attachments_.clear() ;
  }
  public List<ForumAttachment> getAttachFileList() {
    return attachments_ ;
  }
   
  public void updatePost(String postId, boolean quote) throws Exception {
    this.postId = postId ;
    this.quote = quote ;
    if(this.postId != null && this.postId.length() > 0) {
      Post post = this.forumService.getPost(this.categoryId, this.forumId, this.topicId, postId) ;
      if(quote) {
        String title = "" ;
        if(post.getSubject().indexOf(": ") > 0) title = post.getSubject() ;
        else title = getLabel(FIELD_LABEL_QUOTE) + ": " + post.getSubject() ;
        getUIStringInput(FIELD_POSTTITLE_INPUT).setValue(title) ;
        String quoteTag = "[quote=" + post.getOwner() + "]" ;
        getUIFormTextAreaInput(FIELD_MESSENGER_TEXTAREA).setDefaultValue(quoteTag + post.getMessage() + "[/quote]") ;
      } else {
        getUIStringInput(FIELD_POSTTITLE_INPUT).setValue(post.getSubject()) ;
        getUIFormTextAreaInput(FIELD_MESSENGER_TEXTAREA).setDefaultValue(post.getMessage()) ;
        getChild(UIFormInputIconSelector.class).setSelectedIcon(post.getIcon());
      }
    } else {
      if(!quote) {
        Topic topic = this.forumService.getTopic(this.categoryId, this.forumId, this.topicId, false) ;
        String title = topic.getTopicName() ;
        getUIStringInput(FIELD_POSTTITLE_INPUT).setValue(getLabel(FIELD_LABEL_QUOTE) + ": " + title) ;
        getChild(UIFormInputIconSelector.class).setSelectedIcon(topic.getIcon());
      }
    }
  }
  
  public void activate() throws Exception {
    
  }
  
  public void deActivate() throws Exception {
    // TODO Auto-generated method stub
  }
  
  public String[] getActionsTopic() throws Exception {
    return (new String [] {"PreviewPost", "SubmitPost", "CancelAction"});
  }
    
  static  public class PreviewPostActionListener extends EventListener<UIPostForm> {
    public void execute(Event<UIPostForm> event) throws Exception {
      UIPostForm uiForm = event.getSource() ;
      
      String postTitle = uiForm.getUIStringInput(FIELD_POSTTITLE_INPUT).getValue().trim();
      String message = uiForm.getUIFormTextAreaInput(FIELD_MESSENGER_TEXTAREA).getValue() ;
      String userName = Util.getPortalRequestContext().getRemoteUser() ;
      if(message != null && message.length() > 0) message = message.trim() ;
      Post post = new Post() ;
      post.setSubject(postTitle.trim()) ;
      post.setMessage(message) ;
      post.setOwner(userName) ;
      post.setCreatedDate(new Date()) ;
      post.setModifiedBy(userName) ;
      post.setModifiedDate(new Date()) ;
      post.setRemoteAddr("") ;
      UIFormInputIconSelector uiIconSelector = uiForm.getChild(UIFormInputIconSelector.class);
      post.setIcon(uiIconSelector.getSelectedIcon());
      post.setNumberOfAttachment(0) ;
      post.setIsApproved(false) ;
      post.setAttachments(uiForm.attachments_) ;
      
      UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
      popupAction.getChild(UIPopupWindow.class).setRendered(true);
      UIViewTopic viewTopic = popupAction.activate(UIViewTopic.class, 670) ;
      viewTopic.setPostView(post) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  static  public class SubmitPostActionListener extends EventListener<UIPostForm> {
    public void execute(Event<UIPostForm> event) throws Exception {
      UIPostForm uiForm = event.getSource() ;
      String postTitle = uiForm.getUIStringInput(FIELD_POSTTITLE_INPUT).getValue().trim();
      String message = uiForm.getUIFormTextAreaInput(FIELD_MESSENGER_TEXTAREA).getValue() ;
      String userName = Util.getPortalRequestContext().getRemoteUser() ;
      if(message != null && message.length() > 0) message = message.trim() ;
      Post post = new Post() ;
      post.setSubject(postTitle.trim()) ;
      post.setMessage(message) ;
      post.setOwner(userName) ;
      post.setCreatedDate(new Date()) ;
      post.setModifiedBy(userName) ;
      post.setModifiedDate(new Date()) ;
      post.setRemoteAddr("") ;
      UIFormInputIconSelector uiIconSelector = uiForm.getChild(UIFormInputIconSelector.class);
      post.setIcon(uiIconSelector.getSelectedIcon());
      post.setNumberOfAttachment(0) ;
      post.setIsApproved(false) ;
      post.setAttachments(uiForm.attachments_) ;
      
      if(uiForm.postId != null && uiForm.postId.length() > 0) {
        if(uiForm.quote) {
          uiForm.forumService.savePost(uiForm.categoryId, uiForm.forumId, uiForm.topicId, post, true) ;
        } else {
          post.setId(uiForm.postId) ;
          uiForm.forumService.savePost(uiForm.categoryId, uiForm.forumId, uiForm.topicId, post, false) ;
        }
      } else {
        uiForm.forumService.savePost(uiForm.categoryId, uiForm.forumId, uiForm.topicId, post, true) ;
      }
      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
      forumPortlet.cancelAction() ;
      UITopicDetailContainer topicDetailContainer = forumPortlet.findFirstComponentOfType(UITopicDetailContainer.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(topicDetailContainer);
    }
  }
  
  static public class AttachmentActionListener extends EventListener<UIPostForm> {
    public void execute(Event<UIPostForm> event) throws Exception {
      UIPostForm uiForm = event.getSource() ;
      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
      UIPopupContainer popupContainer = forumPortlet.getChild(UIPopupContainer.class) ;
      UIPopupAction uiChildPopup = popupContainer.getChild(UIPopupAction.class) ;
      uiChildPopup.getChild(UIPopupWindow.class).setRendered(true);
      UIAttachFileForm attachFileForm = uiChildPopup.activate(UIAttachFileForm.class, 500) ;
      attachFileForm.updateIsTopicForm(false) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
    }
  }
  
  static public class RemoveAttachmentActionListener extends EventListener<UIPostForm> {
    public void execute(Event<UIPostForm> event) throws Exception {
      UIPostForm uiPostForm = event.getSource() ;
      String attFileId = event.getRequestContext().getRequestParameter(OBJECTID);
      BufferAttachment attachfile = new BufferAttachment();
      for (ForumAttachment att : uiPostForm.attachments_) {
        if (att.getId().equals(attFileId)) {
          attachfile = (BufferAttachment) att;
        }
      }
      uiPostForm.removeFromUploadFileList(attachfile);
      uiPostForm.refreshUploadFileList() ;
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIPostForm> {
    public void execute(Event<UIPostForm> event) throws Exception {
      UIPostForm uiForm = event.getSource() ;
      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
      forumPortlet.cancelAction() ;
      UITopicDetailContainer topicDetailContainer = forumPortlet.findFirstComponentOfType(UITopicDetailContainer.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(topicDetailContainer);
    }
  }  
}
