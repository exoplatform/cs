/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL				 All rights reserved.	*
 * Please look at license.txt in info directory for more license detail.	 *
 **************************************************************************/
package org.exoplatform.forum.webui;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
		template =	"app:/templates/forum/webui/UIForumDescription.gtmpl"
)
public class UIForumDescription extends UIContainer	{
	private String forumId ;
	private String categoryId ;
	public UIForumDescription() throws Exception {		
	}
	
	public void setForumIds(String categoryId, String forumId) {
		this.forumId = forumId ;
		this.categoryId = categoryId ;
	}
	
	@SuppressWarnings("unused")
	private Forum getForum() throws Exception {
		ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
		return forumService.getForum(ForumUtils.getSystemProvider(), categoryId, forumId);
	}
	
}
