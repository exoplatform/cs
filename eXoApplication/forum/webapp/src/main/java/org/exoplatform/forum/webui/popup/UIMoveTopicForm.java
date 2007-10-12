/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicContainer;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/forum/webui/popup/UIMoveTopicForm.gtmpl",
    events = {
      @EventConfig(listeners = UIMoveTopicForm.SaveActionListener.class), 
      @EventConfig(listeners = UIMoveTopicForm.CancelActionListener.class),
      @EventConfig(listeners = UIMoveTopicForm.SetSelectActionListener.class)
    }
)
public class UIMoveTopicForm extends UIForm implements UIPopupComponent {
  private ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
  private String forumId ;
  private List<Topic> topics ;
  private String destForumPath ;
  private String forumSelected  ;
  
  public UIMoveTopicForm() throws Exception {
    
  }
  
  public void activate() throws Exception {
    // TODO Auto-generated method stub
  }
  public void deActivate() throws Exception {
    // TODO Auto-generated method stub
  }
  
  public void updateTopic(String forumId, List<Topic> topics) {
    this.forumId = forumId ;
    this.topics = topics ;
  }
  
  private List<Category> getCategories() throws Exception {
    return this.forumService.getCategories() ;
  }
  
  private boolean getSelectCate(String cateId) throws Exception {
    if(this.destForumPath != null && this.destForumPath.length() > 0) {
      if(this.destForumPath.contains(cateId)) return true;
    } else {
      if(this.topics.get(0).getPath().contains(cateId)) return true ;
    }
    return false ;
  }
  
  private List<Forum> getForums(String categoryId) throws Exception {
    List<Forum> forums = new ArrayList<Forum>() ;
    for(Forum forum : this.forumService.getForums(categoryId)) {
      if(forum.getId().equalsIgnoreCase(this.forumId)) continue ;
      forums.add(forum) ;
    }
    return forums ;
  }
  
  private boolean getSeclectedForum(String forumId) throws Exception {
    if(forumId.equalsIgnoreCase(this.forumSelected)) return true;
    else return false ;
  }
  
  static  public class SaveActionListener extends EventListener<UIMoveTopicForm> {
    public void execute(Event<UIMoveTopicForm> event) throws Exception {
      UIMoveTopicForm uiForm = event.getSource() ;
      if(uiForm.destForumPath != null && uiForm.destForumPath.length() > 0) {
        List<Topic> topics = uiForm.topics ;
        for (Topic topic : topics) {
          uiForm.forumService.moveTopic(topic.getId(), topic.getPath(), uiForm.destForumPath) ;
        }
        UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
        forumPortlet.cancelAction() ;
        UITopicContainer topicContainer = forumPortlet.findFirstComponentOfType(UITopicContainer.class) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(topicContainer) ;
      } else {
        Object[] args = { };
        throw new MessageException(new ApplicationMessage("UIMoveTopicForm.msg.notSelect", args, ApplicationMessage.WARNING)) ;
      }
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIMoveTopicForm> {
    public void execute(Event<UIMoveTopicForm> event) throws Exception {
      UIMoveTopicForm uiForm = event.getSource() ;
      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
      forumPortlet.cancelAction() ;
    }
  }

  static  public class SetSelectActionListener extends EventListener<UIMoveTopicForm> {
    public void execute(Event<UIMoveTopicForm> event) throws Exception {
      UIMoveTopicForm uiForm = event.getSource() ;
      String str = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String []Str = str.trim().split(",");
      uiForm.destForumPath = Str[0] ;
      uiForm.forumSelected = Str[1] ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
    }
  }



























}
