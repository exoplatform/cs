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
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.ForumOption;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.core.UIContainer;

/**
 * Created by The eXo Platform SARL
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Dec 26, 2007 5:50:44 PM 
 */
public class UIForumOption extends UIContainer {
	private	ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	private double timeZone ;
	private String shortDateformat ;
	private String longDateformat ;
	private String timeFormat ;
	private long maxTopic ;
	private long maxPost ;
	private boolean isShowForumJump = false ;
	
  public UIForumOption() throws Exception {
		initOption() ;
  }
	
  @SuppressWarnings("deprecation")
	public void initOption() throws Exception {
		String userName = Util.getPortalRequestContext().getRemoteUser() ;
		ForumOption forumOption = new ForumOption() ;
		forumOption = forumService.getOption(ForumUtils.getSystemProvider(), userName) ;
		Date dateHost = new Date() ;
		if(forumOption == null) {
			timeZone = dateHost.getTimezoneOffset()/ 60 ;
			shortDateformat = "mm/dd/yyyy";
			longDateformat = "ddd,mmm,dd,yyyy";
			timeFormat = "24h";
			maxTopic = 10 ;
			maxPost = 10 ;
			isShowForumJump = false ;
		} else {
			timeZone = forumOption.getTimeZone() ;
			shortDateformat = forumOption.getShortDateFormat() ;
			longDateformat = forumOption.getLongDateFormat() ;
			timeFormat = forumOption.getTimeFormat() ;
			maxTopic = forumOption.getMaxTopicInPage() ;
			maxPost = forumOption.getMaxPostInPage() ;
			isShowForumJump = false ;
		}
  }
	
  public void setFormatAll() throws Exception {
	  UIForumPortlet forumPortlet = (UIForumPortlet)this.getParent() ;
  }
	
}
