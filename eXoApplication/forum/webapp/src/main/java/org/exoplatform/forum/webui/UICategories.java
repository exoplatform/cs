/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.webui;

import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
    template =  "app:/templates/forum/webui/UICategories.gtmpl",
    events = {
    	@EventConfig(listeners = UICategories.OpenCategory.class),
    	@EventConfig(listeners = UICategories.OpenForumLink.class),
    	@EventConfig(listeners = UICategories.OpenLastTopicLink.class)
    }
)
public class UICategories extends UIContainer  {
	protected ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	
  public UICategories() throws Exception {
  	
  }

	private List<Category> getCategoryList() throws Exception {
		List<Category> categoryList = forumService.getCategories();
  	return categoryList;
	}  
	
	private List<Forum> getForumList(String categoryId) throws Exception {
		List<Forum> forumList = forumService.getForums(categoryId);
		return forumList;
	}
	
	private Topic getLastTopic(String topicPath) throws Exception {
		return forumService.getTopicByPath(topicPath) ;
	}
	
	static public class OpenCategory extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories uiContainer = event.getSource();
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID)  ;
      UICategoryContainer categoryContainer = uiContainer.getAncestorOfType(UICategoryContainer.class) ;
      categoryContainer.getChild(UIForumActionBar.class).setRendered(false) ;
			categoryContainer.isRenderCategories = false ;
			categoryContainer.getChild(UICategories.class).setRendered(false) ;
			UICategory uiCategory = categoryContainer.getChild(UICategory.class) ;
			categoryContainer.findFirstComponentOfType(UICategoryInfo.class).setRendered(false) ;
			uiCategory.update(categoryId) ;
			uiCategory.setRendered(true) ;
		}
	}
	
	static public class OpenForumLink extends EventListener<UICategories> {
    public void execute(Event<UICategories> event) throws Exception {
    	UICategories uiContainer = event.getSource();
      String forumId = event.getRequestContext().getRequestParameter(OBJECTID)  ;
      String []id = forumId.trim().split(",");
      UIForumPortlet forumPortlet = uiContainer.getAncestorOfType(UIForumPortlet.class) ;
      forumPortlet.updateIsRendered(2);
      UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
      uiForumContainer.setIsRenderChild(true) ;
      UITopicContainer uiTopicContainer = uiForumContainer.getChild(UITopicContainer.class) ;
      uiForumContainer.getChild(UIForumDescription.class).setForumIds(id[0], id[1]);
      uiTopicContainer.setUpdateForum(id[0], id[1]) ;
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
      context.addUIComponentToUpdateByAjax(forumPortlet) ;
    }
  }
	
  static public class OpenLastTopicLink extends EventListener<UICategories> {
    public void execute(Event<UICategories> event) throws Exception {
      UICategories uiContainer = event.getSource();
      String Id = event.getRequestContext().getRequestParameter(OBJECTID)  ;
      String []id = Id.trim().split(",");
      UIForumPortlet uiForumPortlet = uiContainer.getAncestorOfType(UIForumPortlet.class) ;
      uiForumPortlet.updateIsRendered(2);
      UIForumContainer uiForumContainer = uiForumPortlet.getChild(UIForumContainer.class) ;
      UITopicDetailContainer uiTopicDetailContainer = uiForumContainer.getChild(UITopicDetailContainer.class) ;
      uiForumContainer.setIsRenderChild(false) ;
      UITopicDetail uiTopicDetail = uiTopicDetailContainer.getChild(UITopicDetail.class) ;
      uiForumContainer.getChild(UIForumDescription.class).setForumIds(id[0], id[1]);
      uiTopicDetail.setUpdateTopic(id[0], id[1], id[2], true) ;
      uiTopicDetailContainer.getChild(UITopicPoll.class).updatePoll(id[0], id[1], id[2]) ;
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
      context.addUIComponentToUpdateByAjax(uiForumPortlet) ;
    }
  }
	
	
}







