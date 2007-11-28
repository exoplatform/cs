/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.webui;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.webui.popup.UIMovePostForm;
import org.exoplatform.forum.webui.popup.UIMoveTopicForm;
import org.exoplatform.forum.webui.popup.UIPollForm;
import org.exoplatform.forum.webui.popup.UIPopupAction;
import org.exoplatform.forum.webui.popup.UIPopupContainer;
import org.exoplatform.forum.webui.popup.UIPostForm;
import org.exoplatform.forum.webui.popup.UIRatingForm;
import org.exoplatform.forum.webui.popup.UITopicForm;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.PositiveNumberFormatValidator;

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
      @EventConfig(listeners = UITopicDetail.RatingTopicActionListener.class ),
      @EventConfig(listeners = UITopicDetail.GoNumberPageActionListener.class ),
      
      @EventConfig(listeners = UITopicDetail.PrintActionListener.class ),  
      @EventConfig(listeners = UITopicDetail.EditActionListener.class ),  
      @EventConfig(listeners = UITopicDetail.DeleteActionListener.class ),  
      @EventConfig(listeners = UITopicDetail.QuoteActionListener.class ),  
      @EventConfig(listeners = UITopicDetail.EditTopicActionListener.class ),  //Topic Menu
      @EventConfig(listeners = UITopicDetail.PrintPageActionListener.class ),
      @EventConfig(listeners = UITopicDetail.AddPollActionListener.class ),  
      @EventConfig(listeners = UITopicDetail.SetOpenTopicActionListener.class ),  
      @EventConfig(listeners = UITopicDetail.SetCloseTopicActionListener.class ),  
      @EventConfig(listeners = UITopicDetail.SetLockedTopicActionListener.class ),  
      @EventConfig(listeners = UITopicDetail.SetUnLockTopicActionListener.class ),  
      @EventConfig(listeners = UITopicDetail.SetMoveTopicActionListener.class ),  
      @EventConfig(listeners = UITopicDetail.SetStickTopicActionListener.class ),  
      @EventConfig(listeners = UITopicDetail.SetUnStickTopicActionListener.class ),  
      @EventConfig(listeners = UITopicDetail.SplitTopicActionListener.class ),  
      @EventConfig(listeners = UITopicDetail.SetApproveTopicActionListener.class ),  
      @EventConfig(listeners = UITopicDetail.SetUnApproveTopicActionListener.class ),  
      @EventConfig(listeners = UITopicDetail.SetDeleteTopicActionListener.class ),  
      @EventConfig(listeners = UITopicDetail.MergePostActionListener.class ), //Post Menu 
      @EventConfig(listeners = UITopicDetail.MovePostActionListener.class ),  
      @EventConfig(listeners = UITopicDetail.SetApprovePostActionListener.class ),  
      @EventConfig(listeners = UITopicDetail.SetUnApprovePostActionListener.class ),  
      @EventConfig(listeners = UITopicDetail.SetApproveAttachmentActionListener.class ),  
      @EventConfig(listeners = UITopicDetail.SetUnApproveAttachmentActionListener.class ),  
      @EventConfig(listeners = UITopicDetail.DeletePostActionListener.class )  
    }
)
public class UITopicDetail extends UIForm  {
  private ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
  private String categoryId ;
  private String forumId ; 
  private String topicId;
  private boolean viewTopic = true ;
  private Topic topic;
  private JCRPageList pageList ;
  private long pageSelect  = 1 ;
  private boolean isGopage = false ;
  private boolean isEditTopic = false ;
  private boolean isUpdatePageList = false ;
  private List<Post> posts ;
  public UITopicDetail() throws Exception {
    addUIFormInput( new UIFormStringInput("gopage1", null)) ;
    addUIFormInput( new UIFormStringInput("gopage2", null)) ;
  	addChild(UIForumPageIterator.class, null, "TopicPageIterator") ;
    addChild(UIPostRules.class, null, null);
    addChild(UIForumLinks.class, null, null);
  }
  
  public void setUpdateTopic(String categoryId, String forumId, String topicId, boolean viewTopic) throws Exception {
    this.categoryId = categoryId ;
    this.forumId = forumId ;
    this.topicId = topicId ;
    this.viewTopic = viewTopic ;
    this.getAncestorOfType(UIForumPortlet.class).getChild(UIBreadcumbs.class).setUpdataPath((categoryId + "/" + forumId + "/" + topicId)) ;
    this.topic = forumService.getTopic(categoryId, forumId, topicId, viewTopic) ;
  }
  
  public void setUpdateContainer(String categoryId, String forumId, Topic topic, boolean viewTopic , long numberPage) throws Exception {
  	this.categoryId = categoryId ;
  	this.forumId = forumId ;
  	this.topicId = topic.getId() ;
  	this.viewTopic = viewTopic ;
  	this.pageSelect = numberPage ;
	  this.isGopage = true ;
	  this.topic = topic ;
	  this.getChild(UIForumPageIterator.class).setSelectPage(numberPage) ;
  	this.getAncestorOfType(UIForumPortlet.class).getChild(UIBreadcumbs.class).setUpdataPath((categoryId + "/" + forumId + "/" + topicId)) ;
  }
  
  public void setUpdatePageList(JCRPageList pageList) throws Exception {
	  this.pageList = pageList ;
	  this.isUpdatePageList = true ;
  }
  @SuppressWarnings("unused")
  private Topic getTopic() throws Exception {
    try {
    	if(this.isEditTopic) {
    		this.topic = forumService.getTopic(categoryId, forumId, topicId, viewTopic) ;
    		this.isEditTopic = false ;
    	}
      return this.topic ;
    } catch (Exception e) {
      return null ;
    }
  }
  
  @SuppressWarnings("unused")
  private void initPage() throws Exception {
  	if(this.isUpdatePageList) {
	  	this.isUpdatePageList = false ;
  	} else {
  		this.pageList = forumService.getPosts(categoryId, forumId, topicId) ;
  	}
  	pageList.setPageSize(4) ;
  	this.getChild(UIForumPageIterator.class).updatePageList(this.pageList) ;
  }
  
  @SuppressWarnings({ "unchecked", "unused" })
  private List<Post> getPostPageList() throws Exception {
  	if(!this.isGopage) {
  		this.pageSelect = this.getChild(UIForumPageIterator.class).getPageSelected() ;
  	}
    if(this.pageList == null || this.pageSelect < 1) return null ;
    this.posts = forumService.getPage(this.pageSelect, this.pageList) ;
    for (Post post : this.posts) {
      if(getUIFormCheckBoxInput(post.getId()) != null) {
        getUIFormCheckBoxInput(post.getId()).setChecked(false) ;
      }else {
        addUIFormInput(new UIFormCheckBoxInput(post.getId(), post.getId(), false) );
      }
    }
    this.isGopage = false ;
    return this.posts ;
  }
  
  private Post getPost(String postId) throws Exception {
  	for(Post post : this.posts) {
  		if(post.getId().equals(postId)) return post;
  	}
  	return null ;
  }
  
  static public class AddPostActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
      UITopicDetail topicDetail = event.getSource() ;
      UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
      UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
      UIPostForm postForm = popupContainer.addChild(UIPostForm.class, null, null) ;
      postForm.setPostIds(topicDetail.categoryId, topicDetail.forumId, topicDetail.topicId) ;
      postForm.updatePost("", false) ;
      topicDetail.viewTopic = false ;
      popupContainer.setId("UIAddPostContainer") ;
      popupAction.activate(popupContainer, 670, 440) ;
      topicDetail.getChild(UIForumPageIterator.class).setSelectPage(topicDetail.pageList.getAvailablePage()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }

  static public class RatingTopicActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
      UITopicDetail topicDetail = event.getSource() ;
      String userName = Util.getPortalRequestContext().getRemoteUser() ;
      String[] userVoteRating = topicDetail.topic.getUserVoteRating() ;
      boolean erro = false ;
      for (String string : userVoteRating) {
        if(string.equalsIgnoreCase(userName)) erro = true ; 
      }
      if(!erro) {
        UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
        UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
        UIRatingForm ratingForm = popupAction.createUIComponent(UIRatingForm.class, null, null) ;
        ratingForm.updateRating(topicDetail.topic, topicDetail.categoryId, topicDetail.forumId) ;
        popupAction.activate(ratingForm, 300, 145) ;
        topicDetail.viewTopic = false ;
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
        topicDetail.isEditTopic = true ;
      } else {
        Object[] args = { userName };
        throw new MessageException(new ApplicationMessage("UITopicDetail.sms.VotedRating", args, ApplicationMessage.WARNING)) ;
      }
    }
  }
  
  static public class PrintActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
      UITopicDetail topicDetail = event.getSource() ;
    }
  }

  static public class GoNumberPageActionListener extends EventListener<UITopicDetail> {
  	public void execute(Event<UITopicDetail> event) throws Exception {
  		UITopicDetail topicDetail = event.getSource() ;
  		UIFormStringInput stringInput1 = topicDetail.getUIStringInput("gopage1") ;
  		UIFormStringInput stringInput2 = topicDetail.getUIStringInput("gopage2") ;
  		stringInput1.addValidator(PositiveNumberFormatValidator.class) ;
  		stringInput2.addValidator(PositiveNumberFormatValidator.class) ;
  		String numberPage1 = stringInput1.getValue() ;
  		String numberPage2 = stringInput2.getValue() ;
  		String numberPage = "" ;
  		if(numberPage1 != null && numberPage1.length() > 0) {
  			numberPage = numberPage1 ;
  		} else numberPage = numberPage2 ;
  		if(numberPage != null && numberPage.length() > 0) {
  			Long page = Long.parseLong(numberPage);
  			if(page == 0) {
  				page = (long)1;
  			} else if(page > topicDetail.pageList.getAvailablePage()){
  				page = topicDetail.pageList.getAvailablePage() ;
  			}
  			topicDetail.isGopage = true ;
  			topicDetail.pageSelect = page ;
  			topicDetail.getChild(UIForumPageIterator.class).setSelectPage(page) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail) ;
  		}
  		stringInput1.setValue("") ;
  		stringInput2.setValue("") ;
  	}
  }

  static public class EditActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
      UITopicDetail topicDetail = event.getSource() ;
      String postId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
      UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
      UIPostForm postForm = popupContainer.addChild(UIPostForm.class, null, null) ;
      postForm.setPostIds(topicDetail.categoryId, topicDetail.forumId, topicDetail.topicId) ;
      postForm.updatePost(postId, false) ;
      topicDetail.viewTopic = false ;
      popupContainer.setId("UIEditPostContainer") ;
      popupAction.activate(popupContainer, 670, 440) ;
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
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
      UIPostForm postForm = popupContainer.addChild(UIPostForm.class, null, null) ;
      postForm.setPostIds(topicDetail.categoryId, topicDetail.forumId, topicDetail.topicId) ;
      postForm.updatePost(postId, true) ;
      topicDetail.viewTopic = false ;
      popupContainer.setId("UIQuoteContainer") ;
      popupAction.activate(popupContainer, 670, 440) ;
      topicDetail.getChild(UIForumPageIterator.class).setSelectPage(topicDetail.pageList.getAvailablePage()) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
//--------------------------------   Topic Menu    -------------------------------------------//
  static public class EditTopicActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
      UITopicDetail topicDetail = event.getSource() ;
      UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
      UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
      UITopicForm topicForm = popupContainer.addChild(UITopicForm.class, null, null) ;
      topicForm.setTopicIds(topicDetail.categoryId, topicDetail.forumId) ;
      topicForm.setUpdateTopic(topicDetail.topic, true) ;
      popupContainer.setId("UIEditTopicContainer") ;
      popupAction.activate(popupContainer, 662, 466) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      topicDetail.isEditTopic = true ;
    }
  }
  
  static public class PrintPageActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
      UITopicDetail topicDetail = event.getSource() ;
    }
  }

  static public class AddPollActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
      UITopicDetail topicDetail = event.getSource() ;
      Topic topic = topicDetail.topic ;
      UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
      UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
      UIPollForm  pollForm = popupAction.createUIComponent(UIPollForm.class, null, null) ;
      pollForm.setTopicPath(topic.getPath()) ;
      popupAction.activate(pollForm, 655, 455) ;
      topicDetail.isEditTopic = true ;
    }
  }

  static public class SetOpenTopicActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
      UITopicDetail topicDetail = event.getSource() ;
      Topic topic = topicDetail.topic ;
      if(topic.getIsClosed()) {
        topic.setIsClosed(false) ;
        topicDetail.forumService.saveTopic(topicDetail.categoryId, topicDetail.forumId, topic, false) ;
        topicDetail.viewTopic = false ;
        topicDetail.isEditTopic = true ;
        event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail) ;
      } else {
        Object[] args = { topic.getTopicName() };
        throw new MessageException(new ApplicationMessage("UITopicContainer.sms.Open", args, ApplicationMessage.WARNING)) ;
      }
    }
  }

  static public class SetCloseTopicActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
      UITopicDetail topicDetail = event.getSource() ;
      Topic topic = topicDetail.topic ;
      if(!topic.getIsClosed()) {
        topic.setIsClosed(true) ;
        topicDetail.forumService.saveTopic(topicDetail.categoryId, topicDetail.forumId, topic, false) ;
        topicDetail.viewTopic = false ;
        topicDetail.isEditTopic = true ;
        event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail) ;
      } else {
        Object[] args = { topic.getTopicName() };
        throw new MessageException(new ApplicationMessage("UITopicContainer.sms.Close", args, ApplicationMessage.WARNING)) ;
      }
    }
  }

  static public class SetLockedTopicActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
      UITopicDetail topicDetail = event.getSource() ;
      Topic topic = topicDetail.topic ;
      if(!topic.getIsLock()) {
        topic.setIsLock(true) ;
        topicDetail.forumService.saveTopic(topicDetail.categoryId, topicDetail.forumId, topic, false) ;
        topicDetail.viewTopic = false ;
        topicDetail.isEditTopic = true ;
        event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail) ;
      } else {
        Object[] args = { topic.getTopicName() };
        throw new MessageException(new ApplicationMessage("UITopicContainer.sms.Locked", args, ApplicationMessage.WARNING)) ;
      }
    }
  }

  static public class SetUnLockTopicActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
      UITopicDetail topicDetail = event.getSource() ;
      Topic topic = topicDetail.topic ;
      if(topic.getIsLock()) {
        topic.setIsLock(false) ;
        topicDetail.forumService.saveTopic(topicDetail.categoryId, topicDetail.forumId, topic, false) ;
        topicDetail.viewTopic = false ;
        topicDetail.isEditTopic = true ;
        event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail) ;
      } else {
        Object[] args = { topic.getTopicName() };
        throw new MessageException(new ApplicationMessage("UITopicContainer.sms.UnLock", args, ApplicationMessage.WARNING)) ;
      }
    }
  }

  static public class SetMoveTopicActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
      UITopicDetail topicDetail = event.getSource() ;
      UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
      UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
      UIMoveTopicForm moveTopicForm = popupAction.createUIComponent(UIMoveTopicForm.class, null, null) ;
      List <Topic> topics = new ArrayList<Topic>();
      topics.add(topicDetail.topic) ;
      topicDetail.isEditTopic = true ;
      moveTopicForm.updateTopic(topicDetail.forumId, topics, true);
      popupAction.activate(moveTopicForm, 400, 420) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  static public class SetStickTopicActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
      UITopicDetail topicDetail = event.getSource() ;
      Topic topic = topicDetail.topic ;
      if(!topic.getIsSticky()) {
        topic.setIsSticky(true) ;
        topicDetail.forumService.saveTopic(topicDetail.categoryId, topicDetail.forumId, topic, false) ;
        topicDetail.viewTopic = false ;
        topicDetail.isEditTopic = true ;
        event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail) ;
      } else {
        Object[] args = { topic.getTopicName() };
        throw new MessageException(new ApplicationMessage("UITopicContainer.sms.Stick", args, ApplicationMessage.WARNING)) ;
      }
    }
  }

  static public class SetUnStickTopicActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
      UITopicDetail topicDetail = event.getSource() ;
      Topic topic = topicDetail.topic ;
      if(topic.getIsSticky()) {
        topic.setIsSticky(false) ;
        topicDetail.forumService.saveTopic(topicDetail.categoryId, topicDetail.forumId, topic, false) ;
        topicDetail.viewTopic = false ;
        topicDetail.isEditTopic = true ;
        event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail) ;
      } else {
        Object[] args = { topic.getTopicName() };
        throw new MessageException(new ApplicationMessage("UITopicContainer.sms.UnStick", args, ApplicationMessage.WARNING)) ;
      }
    }
  }

  static public class SplitTopicActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
      UITopicDetail topicDetail = event.getSource() ;
    }
  }

  static public class SetApproveTopicActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
      UITopicDetail topicDetail = event.getSource() ;
    }
  }

  static public class SetUnApproveTopicActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
      UITopicDetail topicDetail = event.getSource() ;
    }
  }

  static public class SetDeleteTopicActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
      UITopicDetail topicDetail = event.getSource() ;
      Topic topic = topicDetail.topic ;
      topicDetail.forumService.removeTopic(topicDetail.categoryId, topicDetail.forumId, topic.getId()) ;
      UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
      UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
      uiForumContainer.setIsRenderChild(true) ;
      UIBreadcumbs breadcumbs = forumPortlet.getChild(UIBreadcumbs.class) ;
      breadcumbs.setUpdataPath((topicDetail.categoryId + "/" + topicDetail.forumId)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForumContainer) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(breadcumbs) ;
    }
  }

  //---------------------------------  Post Menu   --------------------------------------//
  static public class MergePostActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
    }
  }

  static public class MovePostActionListener extends EventListener<UITopicDetail> {
    @SuppressWarnings("unchecked")
    public void execute(Event<UITopicDetail> event) throws Exception {
      UITopicDetail topicDetail = event.getSource() ;
      UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
      List <Post> posts = new ArrayList<Post>();
      List<UIComponent> children = topicDetail.getChildren() ;
      for(UIComponent child : children) {
        if(child instanceof UIFormCheckBoxInput) {
          if(((UIFormCheckBoxInput)child).isChecked()) {
            posts.add(topicDetail.getPost(((UIFormCheckBoxInput)child).getName()));
          }
        }
      }
      if(posts.size() > 0) {
        UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
        UIMovePostForm movePostForm = popupAction.createUIComponent(UIMovePostForm.class, null, null) ;
        movePostForm.updatePost(topicDetail.topicId, posts);
        popupAction.activate(movePostForm, 400, 430) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      } else {
        Object[] args = { };
        throw new MessageException(new ApplicationMessage("UITopicDetail.msg.notCheck", args, ApplicationMessage.WARNING)) ;
      }
    }
  }

  static public class SetApprovePostActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
    }
  }
  
  static public class SetUnApprovePostActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
    }
  }

  static public class SetApproveAttachmentActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
    }
  }
  
  static public class SetUnApproveAttachmentActionListener extends EventListener<UITopicDetail> {
    public void execute(Event<UITopicDetail> event) throws Exception {
    }
  }
  
  static public class DeletePostActionListener extends EventListener<UITopicDetail> {
    @SuppressWarnings("unchecked")
    public void execute(Event<UITopicDetail> event) throws Exception {
      UITopicDetail topicDetail = event.getSource() ;
      List<UIComponent> children = topicDetail.getChildren() ;
      List<Post> posts = new ArrayList<Post>() ;
      for(UIComponent child : children) {
        if(child instanceof UIFormCheckBoxInput) {
          if(((UIFormCheckBoxInput)child).isChecked()) {
            posts.add(topicDetail.getPost(((UIFormCheckBoxInput)child).getName()));
          }
        }
      }
      if(posts.size() > 0) {
        for(Post post : posts) {
          topicDetail.forumService.removePost(topicDetail.categoryId, topicDetail.forumId, topicDetail.topicId, post.getId()) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail) ;
        }
      } else {
        Object[] args = { };
        throw new MessageException(new ApplicationMessage("UITopicDetail.msg.notCheck", args, ApplicationMessage.WARNING)) ;
      }
    }
  }










}
