/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.webui;

import java.util.GregorianCalendar;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
    template =  "app:/templates/forum/webui/UICategories.gtmpl"
)
public class UICategories extends UIContainer  {
	protected ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	
  public UICategories() throws Exception {
  	
//  	UICategories uicomponent = this;
//  	List category = uicomponent.getcategoryList();
  	
  }

	private List<Category> getcategoryList() throws Exception {
		List<Category> categoryList = forumService.getCategories();
//		GregorianCalendar calendar = new GregorianCalendar() ;
//  	for (int i = 0; i < categoryList.size(); i++) {
//  		System.out.println("\n\n" + categoryList.get(i).getDescription() + "  :  " + categoryList.get(i).getCategoryOrder() + "\n\n");
//  		System.out.println("\n\n" + categoryList.get(i).getCreatedDate().toString()+ "\n\n");
//  		System.out.println("\n\n" + calendar.getTime().toString()+ "\n\n");
//  		long a = categoryList.get(i).getCreatedDate().getTime();
//  		long b = calendar.getTime().getTime();
//  		System.out.println("\n\n" + (b - a)/(86400000)+ "\n\n");
//		}
  	return categoryList;
	}  
	
	private List<Forum> getforumList(String categoryId) throws Exception {
		List<Forum> forumList = forumService.getForums(categoryId);
		//forumList.get(0).get//getDescription()//getForumName();
		return forumList;
	}
	
	private Post getLastPost(String categoryId, String forumId) throws Exception {
		Forum forum = forumService.getForum(categoryId, forumId);
		Post lastPost = (Post)forumService.getObjectByPath(forum.getLastPostPath());
		return lastPost;
	}
	
	private Topic getTopicNewPost(String categoryId, String forumId) throws Exception {
		Forum forum = forumService.getForum(categoryId, forumId);
		String path = forum.getLastPostPath();
		int t = 0;
    for (int i = path.length()-1; i >=0 ; i--) {
    	t++;
			if(path.charAt(i) == '/') break;
		}
    Topic topicNewPost = (Topic)forumService.getObjectByPath(path.substring(0, path.length() - t));
    return topicNewPost;
	}
	
	
	
}







