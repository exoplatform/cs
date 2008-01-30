/***************************************************************************
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.forum.webui;

import java.util.Date;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumFormatUtils;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
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
		template =	"app:/templates/forum/webui/UICategories.gtmpl",
		events = {
			@EventConfig(listeners = UICategories.OpenCategoryActionListener.class),
			@EventConfig(listeners = UICategories.OpenForumLinkActionListener.class),
			@EventConfig(listeners = UICategories.OpenLastTopicLinkActionListener.class)
		}
)
public class UICategories extends UIContainer	{
	private double timeZone ;
	private String shortDateformat ;
	private String longDateformat ;
	private String timeFormat ;
	protected ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	
	public UICategories() throws Exception {
	}

	public void setFormat(double timeZone, String shortDateformat, String longDateformat, String timeFormat) {
	  this.timeZone = timeZone ;
	  this.shortDateformat = shortDateformat;
	  this.longDateformat = longDateformat ;
	  this.timeFormat = timeFormat ;
  }
	@SuppressWarnings({ "deprecation", "unused" })
  private String getTime(Date myDate) {
		myDate.setMinutes(myDate.getMinutes() - (int)(timeZone*60));
		return ForumFormatUtils.getFormatDate(timeFormat, myDate) ;
	}
	@SuppressWarnings({ "deprecation", "unused" })
  private String getShortDate(Date myDate) {
		myDate.setMinutes(myDate.getMinutes() - (int)(timeZone*60));
		return ForumFormatUtils.getFormatDate(shortDateformat, myDate) ;
	}
	@SuppressWarnings({ "deprecation", "unused" })
	private String getLongDate(Date myDate) {
		myDate.setMinutes(myDate.getMinutes() - (int)(timeZone*60));
		return ForumFormatUtils.getFormatDate(longDateformat, myDate) ;
	}
	private List<Category> getCategoryList() throws Exception {
		this.getAncestorOfType(UIForumPortlet.class).getChild(UIBreadcumbs.class).setUpdataPath("ForumService") ;
		List<Category> categoryList = forumService.getCategories(ForumSessionUtils.getSystemProvider());
		if(categoryList.size() > 0)
			((UICategoryContainer)getParent()).getChild(UIForumActionBar.class).setHasCategory(true) ;
		else 
			((UICategoryContainer)getParent()).getChild(UIForumActionBar.class).setHasCategory(false) ;
		return categoryList;
	}	
	
	private List<Forum> getForumList(String categoryId) throws Exception {
		List<Forum> forumList = forumService.getForums(ForumSessionUtils.getSystemProvider(), categoryId);
		return forumList;
	}
	
	@SuppressWarnings("unused")
	private Topic getLastTopic(String topicPath) throws Exception {
		return forumService.getTopicByPath(ForumSessionUtils.getSystemProvider(), topicPath) ;
	}
	
	private Category getCategory(String categoryId) throws Exception {
		for(Category category : this.getCategoryList()) {
			if(category.getId().equals(categoryId)) return category ;
		}
		return null ;
	}
	
	static public class OpenCategoryActionListener extends EventListener<UICategories> {
    public void execute(Event<UICategories> event) throws Exception {
			UICategories uiContainer = event.getSource();
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID)	;
			UICategoryContainer categoryContainer = uiContainer.getParent() ;
			categoryContainer.updateIsRender(false) ;
			UICategory uiCategory = categoryContainer.getChild(UICategory.class) ;
			uiCategory.update(uiContainer.getCategory(categoryId), uiContainer.getForumList(categoryId)) ;
			((UIForumPortlet)categoryContainer.getParent()).getChild(UIForumLinks.class).setValueOption(categoryId);
		}
	}
	
	static public class OpenForumLinkActionListener extends EventListener<UICategories> {
    public void execute(Event<UICategories> event) throws Exception {
			UICategories uiContainer = event.getSource();
			String forumId = event.getRequestContext().getRequestParameter(OBJECTID)	;
			String []id = forumId.trim().split(",");
			UIForumPortlet forumPortlet = uiContainer.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.updateIsRendered(2);
			UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
			uiForumContainer.setIsRenderChild(true) ;
			UITopicContainer uiTopicContainer = uiForumContainer.getChild(UITopicContainer.class) ;
			uiForumContainer.getChild(UIForumDescription.class).setForumIds(id[0], id[1]);
			uiTopicContainer.updateByBreadcumbs(id[0], id[1], false) ;
			forumPortlet.getChild(UIForumLinks.class).setValueOption((id[0]+"/"+id[1]));
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
	
	static public class OpenLastTopicLinkActionListener extends EventListener<UICategories> {
    public void execute(Event<UICategories> event) throws Exception {
			UICategories uiContainer = event.getSource();
			String Id = event.getRequestContext().getRequestParameter(OBJECTID)	;
			String []id = Id.trim().split(",");
			UIForumPortlet forumPortlet = uiContainer.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.updateIsRendered(2);
			UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
			UITopicDetailContainer uiTopicDetailContainer = uiForumContainer.getChild(UITopicDetailContainer.class) ;
			uiForumContainer.setIsRenderChild(false) ;
			UITopicDetail uiTopicDetail = uiTopicDetailContainer.getChild(UITopicDetail.class) ;
			uiForumContainer.getChild(UIForumDescription.class).setForumIds(id[0], id[1]);
			uiTopicDetail.setUpdateTopic(id[0], id[1], id[2], true) ;
			uiTopicDetail.setIdPostView("true") ;
			uiTopicDetailContainer.getChild(UITopicPoll.class).updateFormPoll(id[0], id[1], id[2]) ;
			forumPortlet.getChild(UIForumLinks.class).setValueOption((id[0]+"/"+id[1] + " "));
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
}