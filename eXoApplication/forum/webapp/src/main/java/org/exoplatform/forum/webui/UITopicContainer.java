/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.webui;

import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.webui.popup.UICategoryForm;
import org.exoplatform.forum.webui.popup.UIForumForm;
import org.exoplatform.forum.webui.popup.UIPopupAction;
import org.exoplatform.forum.webui.popup.UIPopupComponent;
import org.exoplatform.forum.webui.popup.UITopicForm;
import org.exoplatform.webui.application.WebuiRequestContext;
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
    template =  "app:/templates/forum/webui/UITopicContainer.gtmpl", 
    events = {
      @EventConfig(listeners = UITopicContainer.AddTopicActionListener.class ),  
      @EventConfig(listeners = UITopicContainer.OpenTopicActionListener.class )  
    }
)
public class UITopicContainer extends UIForm implements UIPopupComponent {
  private ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
  private String forumId = "";
  private String categoryId = "";
  public UITopicContainer() throws Exception {
    // render Topic page list
    // render topic action bar
    // render topic page list
    List<Topic> topics = getTopicPageLits(1);
    for(Topic topic : topics) {
    }
  }
  
  public void activate() throws Exception {
  	// TODO Auto-generated method stub
  }
  
  public void deActivate() throws Exception {
  	// TODO Auto-generated method stub
  }
  
  public void setForumIds(String categoryId, String forumId) {
    this.forumId = forumId ;
    this.categoryId = categoryId ;
  }
  
  private Forum getForum() throws Exception {
    return forumService.getForum(categoryId, forumId);
  }
  
  private JCRPageList getPageTopics() throws Exception {
    return forumService.getTopics(categoryId, forumId);
  }

  private List<Topic> getTopicPageLits(long page) throws Exception {
    JCRPageList pageList = getPageTopics();
    List<Topic> topicList = forumService.getPage(page, pageList);
    return topicList ;
  }
  
  static public class AddTopicActionListener extends EventListener<UITopicContainer> {
    public void execute(Event<UITopicContainer> event) throws Exception {
      UITopicContainer uiTopicContainer = event.getSource() ;
      UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
      UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
      popupAction.activate(UITopicForm.class, 600) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }

  
  
  
  
  static public class OpenTopicActionListener extends EventListener<UITopicContainer> {
  	public void execute(Event<UITopicContainer> event) throws Exception {
  		UITopicContainer uiTopicContainer = event.getSource();
  		String path = event.getRequestContext().getRequestParameter(OBJECTID) ; 
  		System.out.println("\n\n topicId:  " + path);
  		UIForumContainer uiForumContainer = uiTopicContainer.getAncestorOfType(UIForumContainer.class) ;
  		uiForumContainer.getChild(UITopicDetailContainer.class).setRendered(true) ;
  		uiForumContainer.getChild(UITopicContainer.class).setRendered(false) ;
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
      context.addUIComponentToUpdateByAjax(uiForumContainer) ;
  	}
  }

  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
}
