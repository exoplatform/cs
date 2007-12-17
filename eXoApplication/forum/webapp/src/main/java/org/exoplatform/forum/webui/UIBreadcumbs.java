/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL				 All rights reserved.	*
 * Please look at license.txt in info directory for more license detail.	 *
 **************************************************************************/
package org.exoplatform.forum.webui;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Tag;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
		template =	"app:/templates/forum/webui/UIBreadcumbs.gtmpl" ,
		events = {
				@EventConfig(listeners = UIBreadcumbs.ChangePathActionListener.class),
				@EventConfig(listeners = UIBreadcumbs.RssActionListener.class)
		}
)
public class UIBreadcumbs extends UIContainer {
	private ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	private List<String> breadcumbs_ = new ArrayList<String>();
	private List<String> path_ = new ArrayList<String>();
	private String forumHomePath_ ;
	public static final String FIELD_FORUMHOME_BREADCUMBS = "forumHome" ;
	public UIBreadcumbs()throws Exception {
		forumHomePath_ = forumService.getForumHomePath(ForumUtils.getSystemProvider()) ;
		breadcumbs_.add("eXo Forum") ;
		path_.add("ForumService") ;
	}
	
	public void setUpdataPath(String path) throws Exception {
		if(path != null && path.length() > 0 && !path.equals("ForumService")) {
			String temp[] = path.split("/") ;
			int t = 0;
			String pathNode = forumHomePath_;
			path_.clear() ;
			breadcumbs_.clear() ;
			path_.add("ForumService") ;
			breadcumbs_.add("eXo Forum") ;
			String tempPath = "";
			for (String string : temp) {
				pathNode = pathNode + "/" + string;
				if(t == 0) {
					tempPath = string;
					if(string.indexOf("ategory")> 0) {
						Category category = (Category)forumService.getObjectNameByPath(ForumUtils.getSystemProvider(), pathNode);
						breadcumbs_.add(category.getCategoryName()) ;
					} else {
						Tag tag = (Tag)forumService.getObjectNameByPath(ForumUtils.getSystemProvider(), pathNode);
						breadcumbs_.add(tag.getName()) ;
					}
				}else if(t == 1) {
					tempPath = tempPath + "/" + string ;
					Forum forum = (Forum)forumService.getObjectNameByPath(ForumUtils.getSystemProvider(), pathNode);
					breadcumbs_.add(forum.getForumName()) ;
				}else if(t == 2) {
					tempPath = tempPath + "/" + string ;
					Topic topic = (Topic)forumService.getObjectNameByPath(ForumUtils.getSystemProvider(), pathNode);
					breadcumbs_.add(topic.getTopicName()) ;
				}
				path_.add(tempPath) ;
				++t;
			}
		} else {
			path_.clear() ;
			breadcumbs_.clear() ;
			path_.add("ForumService") ;
			breadcumbs_.add("eXo Forum") ;
		}
	}
	
	@SuppressWarnings("unused")
	private String getPath(int index) {
		return this.path_.get(index) ;
	}
	
	@SuppressWarnings("unused")
	private int getMaxPath() {
		return breadcumbs_.size() ;
	}
	
	@SuppressWarnings("unused")
	private List<String> getBreadcumbs() throws Exception {
		return breadcumbs_ ;
	}
	
	
	static public class ChangePathActionListener extends EventListener<UIBreadcumbs> {
		public void execute(Event<UIBreadcumbs> event) throws Exception {
			UIBreadcumbs uiBreadcums = event.getSource() ;			
			String path = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIForumPortlet forumPortlet = uiBreadcums.getAncestorOfType(UIForumPortlet.class) ;
			if(path.equals("ForumService")) {
				UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
				categoryContainer.updateIsRender(true) ;
				forumPortlet.updateIsRendered(1);
			}else if(path.indexOf("forum") > 0) {
				String id[] = path.split("/");
				forumPortlet.updateIsRendered(2);
				UIForumContainer forumContainer = forumPortlet.findFirstComponentOfType(UIForumContainer.class);
				forumContainer.setIsRenderChild(true) ;
				forumContainer.getChild(UIForumDescription.class).setForumIds(id[0], id[1]);
				forumContainer.getChild(UITopicContainer.class).updateByBreadcumbs(id[0], id[1], true) ;
			}else {
				UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
				categoryContainer.getChild(UICategory.class).updateByBreadcumbs(path) ;
				categoryContainer.updateIsRender(false) ;
				forumPortlet.updateIsRendered(1);
			}
			uiBreadcums.setUpdataPath(path);
			forumPortlet.getChild(UIForumLinks.class).setValueOption(path);
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}	
	
	static public class RssActionListener extends EventListener<UIBreadcumbs> {
		public void execute(Event<UIBreadcumbs> event) throws Exception {
			UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
			UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
			categoryContainer.updateIsRender(true) ;
			forumPortlet.updateIsRendered(1);
			event.getSource().setUpdataPath("ForumService");
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}	
	
}