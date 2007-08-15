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
import org.exoplatform.forum.webui.popup.UIForumForm;
import org.exoplatform.forum.webui.popup.UIPopupAction;
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
    template =  "app:/templates/forum/webui/UICategory.gtmpl",
    events = {
        @EventConfig(listeners = UICategory.AddForumActionListener.class)
    }
)
public class UICategory extends UIContainer  {
	private String categoryId ;
  public UICategory() throws Exception {

  }
  
  public void update(String id) throws Exception {
  	categoryId = id ;
  }
  
  private Category getCategory() throws Exception{
  	ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
		return forumService.getCategory(categoryId);
	}
  
	private List<Forum> getForumList(String categoryId) throws Exception {
		ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
		List<Forum> forumList = forumService.getForums(categoryId);
		return forumList;
	}
	
	private Topic getTopicNewPost(String categoryId, String forumId) throws Exception {
		ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
		Forum forum = forumService.getForum(categoryId, forumId);
		String path = forum.getLastPostPath();
		if(path.length() < 1) return null;
		int t = 0;
    for (int i = path.length()-1; i >=0 ; i--) {
    	t++;
			if(path.charAt(i) == '/') break;
		}
    Topic topicNewPost = forumService.getTopicByPath(path.substring(0, path.length() - t));
    return topicNewPost;
	}
  
  static public class AddForumActionListener extends EventListener<UICategory> {
    public void execute(Event<UICategory> event) throws Exception {
      UICategory uiActionBar = event.getSource() ;      
      UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
      UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
      UIForumForm forumForm = popupAction.createUIComponent(UIForumForm.class, null, null) ;
      forumForm.setCategoryValue(uiActionBar.categoryId, false) ;
      popupAction.activate(forumForm, 662, 466) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
}
