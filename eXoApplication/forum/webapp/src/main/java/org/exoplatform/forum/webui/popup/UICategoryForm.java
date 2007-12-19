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
package org.exoplatform.forum.webui.popup;

import java.util.Date;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.webui.EmptyNameValidator;
import org.exoplatform.forum.webui.UIBreadcumbs;
import org.exoplatform.forum.webui.UICategories;
import org.exoplatform.forum.webui.UICategory;
import org.exoplatform.forum.webui.UICategoryContainer;
import org.exoplatform.forum.webui.UIForumLinks;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.validator.PositiveNumberFormatValidator;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/forum/webui/popup/UICategoryForm.gtmpl",
		events = {
			@EventConfig(listeners = UICategoryForm.SaveActionListener.class), 
			@EventConfig(listeners = UICategoryForm.CancelActionListener.class, phase=Phase.DECODE)
		}
)
public class UICategoryForm extends UIForm implements UIPopupComponent{
	private String categoryId = "";
	public static final String FIELD_CATEGORYTITLE_INPUT = "CategoryTitle" ;
	public static final String FIELD_CATEGORYORDER_INPUT = "CategoryOrder" ;
	public static final String FIELD_DESCRIPTION_TEXTAREA = "Description" ;
	
	public static final String FIELD_USERPRIVATE_INPUT = "UserPrivate" ;
	
	public UICategoryForm() throws Exception {
		UIFormStringInput categoryTitle = new UIFormStringInput(FIELD_CATEGORYTITLE_INPUT, FIELD_CATEGORYTITLE_INPUT, null);
		categoryTitle.addValidator(EmptyNameValidator.class);
		UIFormStringInput categoryOrder = new UIFormStringInput(FIELD_CATEGORYORDER_INPUT, FIELD_CATEGORYORDER_INPUT, "0");
		categoryOrder.addValidator(PositiveNumberFormatValidator.class);
		UIFormStringInput description = new UIFormTextAreaInput(FIELD_DESCRIPTION_TEXTAREA, FIELD_DESCRIPTION_TEXTAREA, null);

		UIFormStringInput userPrivate = new UIFormStringInput(FIELD_USERPRIVATE_INPUT, FIELD_USERPRIVATE_INPUT, null);
		
		addUIFormInput(categoryTitle);
		addUIFormInput(categoryOrder);
		addUIFormInput(userPrivate);
		addUIFormInput(description);
	}
	
	public void activate() throws Exception {
		// TODO Auto-generated method stub
		
	}
	public void deActivate() throws Exception {
		// TODO Auto-generated method stub
		//System.out.println("\n\n description: sfdsf\n\n");
	}
	
	public void setCategoryValue(Category category, boolean isUpdate) {
		if(isUpdate) {
			this.categoryId = category.getId() ;
			getUIStringInput(FIELD_CATEGORYTITLE_INPUT).setValue(category.getCategoryName()) ;
			getUIStringInput(FIELD_CATEGORYORDER_INPUT).setValue(Long.toString(category.getCategoryOrder())) ;
			getUIFormTextAreaInput(FIELD_DESCRIPTION_TEXTAREA).setDefaultValue(category.getDescription()) ;
			getUIStringInput(FIELD_USERPRIVATE_INPUT).setValue(category.getUserPrivate()) ;
		}
	}
	
	static	public class SaveActionListener extends EventListener<UICategoryForm> {
		public void execute(Event<UICategoryForm> event) throws Exception {
			UICategoryForm uiForm = event.getSource() ;
			String categoryTitle = uiForm.getUIStringInput(FIELD_CATEGORYTITLE_INPUT).getValue();
			String categoryOrder = uiForm.getUIStringInput(FIELD_CATEGORYORDER_INPUT).getValue();
			String description = uiForm.getUIFormTextAreaInput(FIELD_DESCRIPTION_TEXTAREA).getValue();
			String userPrivate = uiForm.getUIStringInput(FIELD_USERPRIVATE_INPUT).getValue();
			String userName = Util.getPortalRequestContext().getRemoteUser() ;
			Category cat = new Category();
			cat.setOwner(userName) ;
			cat.setCategoryName(categoryTitle.trim()) ;
			cat.setCategoryOrder(Long.parseLong(categoryOrder)) ;
			cat.setCreatedDate(new Date()) ;
			cat.setDescription(description) ;
			cat.setModifiedBy(userName) ;
			cat.setModifiedDate(new Date()) ;
			cat.setUserPrivate(userPrivate) ;
			
			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			ForumService forumService =	(ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
			
			if(uiForm.categoryId.length() > 0) {
				cat.setId(uiForm.categoryId) ;
				forumService.saveCategory(ForumUtils.getSystemProvider(), cat, false);
				forumPortlet.cancelAction() ;
				UICategory uiCategory = forumPortlet.getChild(UICategoryContainer.class).getChild(UICategory.class) ;
				WebuiRequestContext context = event.getRequestContext() ;
				context.addUIComponentToUpdateByAjax(forumPortlet.getChild(UIBreadcumbs.class)) ;
				context.addUIComponentToUpdateByAjax(uiCategory) ;
			} else {
				forumService.saveCategory(ForumUtils.getSystemProvider(), cat, true);
				forumPortlet.cancelAction() ;
				UICategories uiCategories = forumPortlet.findFirstComponentOfType(UICategories.class) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiCategories) ;
			}
			forumPortlet.getChild(UIForumLinks.class).setUpdateForumLinks() ;
		}
	}
	
	static	public class CancelActionListener extends EventListener<UICategoryForm> {
		public void execute(Event<UICategoryForm> event) throws Exception {
			UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.cancelAction() ;
		}
	}

	
}
