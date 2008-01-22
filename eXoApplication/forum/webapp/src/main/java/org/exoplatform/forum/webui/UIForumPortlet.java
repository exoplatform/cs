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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.service.ForumOption;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.webui.popup.UIPopupAction;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPopupMessages;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/**
 * Author : Nguyen Quang Hung
 *					hung.nguyen@exoplatform.com
 * Aug 01, 2007
 */
@ComponentConfig(
	 lifecycle = UIApplicationLifecycle.class, 
	 template = "app:/templates/forum/webui/UIForumPortlet.gtmpl"
)
public class UIForumPortlet extends UIPortletApplication {
	private	ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	private boolean isCategoryRendered = true;
	private boolean isForumRendered = false;
	private boolean isTagRendered = false;
	private boolean isJumpRendered = false;
	private double timeZone ;
	private String shortDateformat ;
	private String longDateformat ;
	private String timeFormat ;
	private long maxTopic ;
	private long maxPost ;
	public UIForumPortlet() throws Exception {
		addChild(UIBreadcumbs.class, null, null) ;
		addChild(UICategoryContainer.class, null, null).setRendered(isCategoryRendered) ;
		addChild(UIForumContainer.class, null, null).setRendered(isForumRendered) ;
		addChild(UITopicsTag.class, null, null).setRendered(isTagRendered) ;
		addChild(UIForumLinks.class, null, null).setRendered(false) ;
		addChild(UIPopupAction.class, null, null) ;
		initOption();
		String []newStr = ForumSessionUtils.getUserGroups() ;
		for (String string : newStr) {
      System.out.println("\n" + string);
    }
	}

	public void updateIsRendered(int selected) throws Exception {
		if(selected == 1) {
			isCategoryRendered = true ;
			isForumRendered = false ;
			isTagRendered = false ;
		} else {
			if(selected == 2) {
				isForumRendered = true ;
				isCategoryRendered = false ;
				isTagRendered = false ;
			} else {
				isTagRendered = true ;
				isForumRendered = false ;
				isCategoryRendered = false ;
			}
		}
		UICategoryContainer categoryContainer = getChild(UICategoryContainer.class).setRendered(isCategoryRendered) ;
		categoryContainer.setIsRenderJump(isJumpRendered) ;
		UIForumContainer forumContainer = getChild(UIForumContainer.class).setRendered(isForumRendered) ;
		forumContainer.setIsRenderJump(isJumpRendered) ;
		getChild(UITopicsTag.class).setRendered(isTagRendered) ;
	}
	
	@SuppressWarnings("unused")
  private boolean  getIsJumpRendered() {
		return isJumpRendered ;
	}

	public void renderPopupMessages() throws Exception {
		UIPopupMessages popupMess = getUIPopupMessages();
		if(popupMess == null)	return ;
		WebuiRequestContext	context =	RequestContext.getCurrentInstance() ;
		popupMess.processRender(context);
	}

	public void cancelAction() throws Exception {
		WebuiRequestContext context = RequestContext.getCurrentInstance() ;
		UIPopupAction popupAction = getChild(UIPopupAction.class) ;
		popupAction.deActivate() ;
		context.addUIComponentToUpdateByAjax(popupAction) ;
	}
	
  @SuppressWarnings("deprecation")
	public void initOption() throws Exception {
  	String userId = ForumSessionUtils.getCurrentUser() ;
		Date dateHost = new Date() ;
		timeZone = dateHost.getTimezoneOffset()/ 60 ;
		shortDateformat = "mm/dd/yyyy";
		longDateformat = "ddd,mmm,dd,yyyy";
		timeFormat = "12h";
		maxTopic = 10 ;
		maxPost = 10 ;
		isJumpRendered = true ;
		if(userId != null && userId.length() > 0) {
			ForumOption forumOption = new ForumOption() ;
			forumOption = forumService.getOption(ForumSessionUtils.getSystemProvider(), userId) ;
			if(forumOption != null) {
				timeZone = forumOption.getTimeZone() ;
				shortDateformat = forumOption.getShortDateFormat() ;
				longDateformat = forumOption.getLongDateFormat() ;
				timeFormat = forumOption.getTimeFormat() ;
				maxTopic = forumOption.getMaxTopicInPage() ;
				maxPost = forumOption.getMaxPostInPage() ;
				isJumpRendered = forumOption.getIsShowForumJump() ;
			}
		}
		UICategoryContainer categoryContainer = getChild(UICategoryContainer.class);
		categoryContainer.getChild(UICategories.class).setFormat(timeZone, shortDateformat, longDateformat, timeFormat);
		categoryContainer.getChild(UICategory.class).setFormat(timeZone, shortDateformat, longDateformat, timeFormat);
		UITopicContainer topicContainer = findFirstComponentOfType(UITopicContainer.class);
		topicContainer.setFormat(timeZone, shortDateformat, longDateformat, timeFormat);
		topicContainer.setMaxItemInPage(maxTopic,maxPost) ;
		UITopicDetail topicDetail = findFirstComponentOfType(UITopicDetail.class) ;
		topicDetail.setMaxPostInPage(maxPost);
		topicDetail.setFormat(timeZone, shortDateformat, longDateformat, timeFormat);
		findFirstComponentOfType(UITopicPoll.class).setFormat(timeZone, shortDateformat, longDateformat, timeFormat);
		UITopicsTag topicsTag = getChild(UITopicsTag.class);
		topicsTag.setMaxItemInPage(maxTopic, maxPost);
		topicsTag.setFormat(timeZone, shortDateformat, longDateformat, timeFormat);
  }
}