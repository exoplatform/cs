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
import org.exoplatform.forum.webui.UIForumContainer;
import org.exoplatform.forum.webui.UIForumDescription;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/forum/webui/popup/UIMoveForumForm.gtmpl",
    events = {
      @EventConfig(listeners = UIMoveForumForm.SaveActionListener.class), 
      @EventConfig(listeners = UIMoveForumForm.CancelActionListener.class,phase = Phase.DECODE)
    }
)
public class UIMoveForumForm extends UIForm implements UIPopupComponent {
	public static final String FIELD_CATEGORY_SELECTBOX = "SelectCategory" ;
  private List<Forum> forums_ ;
  private String categoryId_ ;
  private String newCategoryId_ ;
  private boolean isForumUpdate = false;
	
  public void setListForum(List<Forum> forums, String categoryId) {
		forums_ = forums ;
		categoryId_ = categoryId ;
	}
	
  public UIMoveForumForm() throws Exception {
  }
  
  public void setForumUpdate(boolean isForumUpdate) {
    this.isForumUpdate = isForumUpdate ;
  }
  
  private List<Category> getCategories() throws Exception {
    ForumService forumService =  (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
    List<Category> categorys =  new ArrayList<Category>();
    for (Category category :forumService.getCategories()) {
      if( !category.getId().equals(categoryId_) ) {
        categorys.add(category) ;
      }
    }
    return categorys ;
  }
  
  private boolean getSeclectedCategory(String cactegoryId) throws Exception {
    if(cactegoryId.equalsIgnoreCase(this.newCategoryId_)) return true;
    else return false ;
  }
  
  public void activate() throws Exception {
  }
  public void deActivate() throws Exception {
  }
  
  static  public class SaveActionListener extends EventListener<UIMoveForumForm> {
    public void execute(Event<UIMoveForumForm> event) throws Exception {
      UIMoveForumForm uiForm = event.getSource() ;
      String categoryId = event.getRequestContext().getRequestParameter(OBJECTID)  ;
      ForumService forumService =  (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
      String categoryPath = "";
      List<Category> categorys = uiForm.getCategories() ;
      for (Category category :categorys) {
        if( category.getId().equals(categoryId) ) {
          categoryPath = category.getPath() ;
        }
      }
      List<Forum> forums = uiForm.forums_ ;
      for (Forum forum : forums) {
				forumService.moveForum(forum.getId(), forum.getPath(), categoryPath) ;
			}
      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
      forumPortlet.cancelAction() ;
      if(uiForm.isForumUpdate) {
        forumPortlet.updateIsRendered(2);
        UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
        uiForumContainer.setIsRenderChild(true) ;
        UITopicContainer uiTopicContainer = uiForumContainer.getChild(UITopicContainer.class);
        uiForumContainer.getChild(UIForumDescription.class).setForumIds(categoryId, forums.get(0).getId());
        uiTopicContainer.setUpdateForum(categoryId, forums.get(0)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
      } else {
        UICategory uiCategory = forumPortlet.findFirstComponentOfType(UICategory.class);
  			event.getRequestContext().addUIComponentToUpdateByAjax(uiCategory) ;
      }
    }
  }
 
  static  public class CancelActionListener extends EventListener<UIMoveForumForm> {
    public void execute(Event<UIMoveForumForm> event) throws Exception {
      UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
      forumPortlet.cancelAction() ;
    }
  }
}
