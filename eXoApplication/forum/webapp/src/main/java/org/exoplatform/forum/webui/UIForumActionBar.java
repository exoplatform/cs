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

import org.exoplatform.forum.webui.popup.UICategoryForm;
import org.exoplatform.forum.webui.popup.UIForumForm;
import org.exoplatform.forum.webui.popup.UIForumOptionForm;
import org.exoplatform.forum.webui.popup.UIModeratorManagementForm;
import org.exoplatform.forum.webui.popup.UIPopupAction;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
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
		template =	"app:/templates/forum/webui/UIForumActionBar.gtmpl", 
		events = {
				@EventConfig(listeners = UIForumActionBar.AddCategoryActionListener.class),
				@EventConfig(listeners = UIForumActionBar.AddForumActionListener.class),
				@EventConfig(listeners = UIForumActionBar.ManageModeratorActionListener.class),
				@EventConfig(listeners = UIForumActionBar.ForumOptionActionListener.class)
		}
)
public class UIForumActionBar extends UIContainer	{
	private boolean hasCategory = false ;
	public UIForumActionBar() throws Exception {		
	} 
	
	public void setHasCategory(boolean hasCategory) {
	  this.hasCategory = hasCategory ;
  }
	static public class AddCategoryActionListener extends EventListener<UIForumActionBar> {
		@Override
    public void execute(Event<UIForumActionBar> event) throws Exception {
			UIForumActionBar uiActionBar = event.getSource() ;
			UIForumPortlet forumPortlet = uiActionBar.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			popupAction.activate(UICategoryForm.class, 500) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}	
	
	static public class AddForumActionListener extends EventListener<UIForumActionBar> {
		@Override
    @SuppressWarnings("unchecked")
		public void execute(Event<UIForumActionBar> event) throws Exception {
			UIForumActionBar uiActionBar = event.getSource() ;
			if(uiActionBar.hasCategory) {
				UIForumPortlet forumPortlet = uiActionBar.getAncestorOfType(UIForumPortlet.class) ;
				UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
				UIForumForm forumForm = popupAction.createUIComponent(UIForumForm.class, null, null) ;
				forumForm.setCategoryValue("", true) ;
				forumForm.setForumUpdate(false) ;
				popupAction.activate(forumForm, 650, 450) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			} else {
				UIApplication uiApp = uiActionBar.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UIForumActionBar.msg.notCategory", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				return ;
			}
		}
	} 
	
	static public class ManageModeratorActionListener extends EventListener<UIForumActionBar> {
		@Override
    public void execute(Event<UIForumActionBar> event) throws Exception {
			UIForumActionBar uiActionBar = event.getSource() ;
				UIForumPortlet forumPortlet = uiActionBar.getAncestorOfType(UIForumPortlet.class) ;
				UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
				popupAction.activate(UIModeratorManagementForm.class, 662) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}	
	
	static public class ForumOptionActionListener extends EventListener<UIForumActionBar> {
		@Override
    public void execute(Event<UIForumActionBar> event) throws Exception {
			UIForumActionBar uiActionBar = event.getSource() ;
			UIForumPortlet forumPortlet = uiActionBar.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIForumOptionForm forumOptionForm = popupAction.createUIComponent(UIForumOptionForm.class, null, null) ;
			popupAction.activate(forumOptionForm, 580, 290) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}	
}
