/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.webui;

import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.webui.popup.UIPopupAction;
import org.exoplatform.forum.webui.popup.UIPostForm;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/templates/forum/webui/UITopicDetail.gtmpl", 
    events = {
      @EventConfig(listeners = UITopicDetail.AddPostActionListener.class ),  
      @EventConfig(listeners = UITopicDetail.PrintActionListener.class ),  
      @EventConfig(listeners = UITopicDetail.EditActionListener.class ),  
      @EventConfig(listeners = UITopicDetail.DeleteActionListener.class ),  
      @EventConfig(listeners = UITopicDetail.QuoteActionListener.class )  
    }
)
public class UITopicDetail extends UIForm  {
  private ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
  private String categoryId ;
  private String forumId ; 
  private String topicId;
  public UITopicDetail() throws Exception {
    // render post page list
    // render actions bar
    // render posts detail and post actions
    addChild(UIPostRules.class, null, null);
    addChild(UIForumLinks.class, null, null);
  }
  
  public void setPostIds(String categoryId, String forumId, String topicId) {
    this.categoryId = categoryId ;
    this.forumId = forumId ;
    this.topicId = topicId ;
  }
  
  private Topic getTopic() throws Exception {
    return forumService.getTopic(categoryId, forumId, topicId, true) ;
  }
  
  private JCRPageList getPagePosts() throws Exception {
    return forumService.getPosts(categoryId, forumId, topicId) ;
  }
  
  private List<Post> getPostPageList( long page) throws Exception {
    JCRPageList pageList = getPagePosts() ;
    List<Post> postList = forumService.getPage(page, pageList) ;
    return postList ;
  }
  
  static public class AddPostActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
      UITopicDetail topicDetail = event.getSource() ;
      UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
      UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
      UIPostForm postForm = popupAction.createUIComponent(UIPostForm.class, null, null) ;
      postForm.setPostIds(topicDetail.categoryId, topicDetail.forumId, topicDetail.topicId) ;
      postForm.updatePost("", false) ;
      popupAction.activate(postForm, 670, 440) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }

  static public class PrintActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
      UITopicDetail topicDetail = event.getSource() ;
    }
  }
  
  static public class EditActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
      UITopicDetail topicDetail = event.getSource() ;
      String postId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
      UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
      UIPostForm postForm = popupAction.createUIComponent(UIPostForm.class, null, null) ;
      postForm.setPostIds(topicDetail.categoryId, topicDetail.forumId, topicDetail.topicId) ;
      postForm.updatePost(postId, false) ;
      popupAction.activate(postForm, 670, 440) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  static public class DeleteActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
      UITopicDetail topicDetail = event.getSource() ;
      String postId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      topicDetail.forumService.removePost(topicDetail.categoryId, topicDetail.forumId, topicDetail.topicId, postId) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail.getParent()) ;
    }
  }
  
  static public class QuoteActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
      UITopicDetail topicDetail = event.getSource() ;
      String postId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
      UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
      UIPostForm postForm = popupAction.createUIComponent(UIPostForm.class, null, null) ;
      postForm.setPostIds(topicDetail.categoryId, topicDetail.forumId, topicDetail.topicId) ;
      postForm.updatePost(postId, true) ;
      popupAction.activate(postForm, 670, 440) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }

























}
