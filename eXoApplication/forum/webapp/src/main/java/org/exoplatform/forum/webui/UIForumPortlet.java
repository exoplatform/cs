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

import org.exoplatform.forum.webui.popup.UIPopupAction;
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
	private boolean isCategoryRendered = true;
	private boolean isForumRendered = false;
	private boolean isPostRendered = false;
	public UIForumPortlet() throws Exception {
		addChild(UIBreadcumbs.class, null, null) ;
		addChild(UICategoryContainer.class, null, null).setRendered(isCategoryRendered) ;
		addChild(UIForumContainer.class, null, null).setRendered(isForumRendered) ;
		addChild(UITopicsTag.class, null, null).setRendered(isPostRendered) ;
		addChild(UIForumLinks.class, null, null).setRendered(isForumRendered) ;
		addChild(UIPopupAction.class, null, null) ;
		addChild(UIForumOption.class, null, null).setRendered(true) ;
	}

	public void updateIsRendered(int selected) {
		if(selected == 1) {
			isCategoryRendered = true ;
			isForumRendered = false ;
			isPostRendered = false ;
		} else {
			if(selected == 2) {
				isForumRendered = true ;
				isCategoryRendered = false ;
				isPostRendered = false ;
			} else {
				isPostRendered = true ;
				isForumRendered = false ;
				isCategoryRendered = false ;
			}
		}
		getChild(UICategoryContainer.class).setRendered(isCategoryRendered) ;
		getChild(UIForumContainer.class).setRendered(isForumRendered) ;
		getChild(UITopicsTag.class).setRendered(isPostRendered) ;
	}
	
	public void renderPopupMessages() throws Exception {
		UIPopupMessages popupMess = getUIPopupMessages();
		if(popupMess == null)	return ;
		WebuiRequestContext	context =	WebuiRequestContext.getCurrentInstance() ;
		popupMess.processRender(context);
	}

	public void cancelAction() throws Exception {
		WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
		UIPopupAction popupAction = getChild(UIPopupAction.class) ;
		popupAction.deActivate() ;
		context.addUIComponentToUpdateByAjax(popupAction) ;
	}
}