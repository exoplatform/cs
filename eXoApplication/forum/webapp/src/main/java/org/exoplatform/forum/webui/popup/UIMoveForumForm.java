/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.webui.UICategory;
import org.exoplatform.forum.webui.UICategoryContainer;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/forum/webui/popup/UICategoryForm.gtmpl",
    events = {
      @EventConfig(listeners = UIMoveForumForm.SaveActionListener.class), 
      @EventConfig(listeners = UIMoveForumForm.CancelActionListener.class, phase=Phase.DECODE)
    }
)
public class UIMoveForumForm extends UIForm implements UIPopupComponent {
	public static final String FIELD_CATEGORY_SELECTBOX = "SelectCategory" ;
  private List<Forum> forums_ ;
  private String categoryId_ ;
	
  public  void setListForum(List<Forum> forums, String categoryId) {
		forums_ = forums ;
		categoryId_ = categoryId ;
	}
	
  public UIMoveForumForm() throws Exception {
  }
  
  public void activate() throws Exception {
  	ForumService forumService =  (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
  	List<Category> categorys = forumService.getCategories() ;
  	List<SelectItemOption<String>> list = new ArrayList<SelectItemOption<String>>() ;
  	for (Category category :categorys) {
  		if( !category.getId().equals(categoryId_) ) {
  			list.add(new SelectItemOption<String>(category.getCategoryName(), category.getPath())) ;
  		}
  	}
  	UIFormSelectBox categoryPath = new UIFormSelectBox(FIELD_CATEGORY_SELECTBOX, FIELD_CATEGORY_SELECTBOX, list) ;
  	
  	addUIFormInput(categoryPath) ;
  }
  public void deActivate() throws Exception {
  }

  static  public class SaveActionListener extends EventListener<UIMoveForumForm> {
    public void execute(Event<UIMoveForumForm> event) throws Exception {
      UIMoveForumForm uiForm = event.getSource() ;
      ForumService forumService =  (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
      String categoryPath = uiForm.getUIFormSelectBox(FIELD_CATEGORY_SELECTBOX).getValue() ;
      List<Forum> forums = uiForm.forums_ ;
      for (Forum forum : forums) {
				forumService.moveForum(forum.getId(), forum.getPath(), categoryPath) ;
			}
      UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
      forumPortlet.cancelAction() ;
      UICategory uiCategory = forumPortlet.getChild(UICategoryContainer.class).getChild(UICategory.class);
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
			context.addUIComponentToUpdateByAjax(uiCategory) ;
    }
  }
 
  static  public class CancelActionListener extends EventListener<UIMoveForumForm> {
    public void execute(Event<UIMoveForumForm> event) throws Exception {
      UIMoveForumForm uiForm = event.getSource() ;
      UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
      forumPortlet.cancelAction() ;
    }
  }
}
