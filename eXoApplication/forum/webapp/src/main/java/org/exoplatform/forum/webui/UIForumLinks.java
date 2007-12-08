/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.webui;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.ForumLinkData;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/forum/webui/UIForumLinks.gtmpl",
    events = {
      @EventConfig(listeners = UIForumLinks.SelectActionListener.class)      
    }
)
public class UIForumLinks extends UIForm {
	private ForumService forumService =  (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	public static final String FIELD_FORUMLINK_SELECTBOX = "forumLink" ;
	private String path  = "";
	public UIForumLinks() throws Exception {
  	List<ForumLinkData> forumLinks = forumService.getAllLink(ForumUtils.getSystemProvider());
  	List<SelectItemOption<String>> list = new ArrayList<SelectItemOption<String>>() ;
  	list.add(new SelectItemOption<String>("Forum Home Page/hompage", "ForumService")) ;
  	for(ForumLinkData linkData : forumLinks) {
  		list.add(new SelectItemOption<String>(linkData.getName(), linkData.getPath())) ;
		}
  	UIFormSelectBoxForum forumLink = new UIFormSelectBoxForum(FIELD_FORUMLINK_SELECTBOX, FIELD_FORUMLINK_SELECTBOX, list) ;
  	forumLink.setDefaultValue("ForumService");
  	addUIFormInput(forumLink) ;
  }
  
  public UIFormSelectBoxForum getUIFormSelectBoxForum(String name) {
  	return  findComponentById(name) ;
  }
  
  public void setUpdateForumLinks() throws Exception {
  	List<ForumLinkData> forumLinks = forumService.getAllLink(ForumUtils.getSystemProvider());
  	List<SelectItemOption<String>> list = new ArrayList<SelectItemOption<String>>() ;
  	list.add(new SelectItemOption<String>("Forum Home Page;hompage", "ForumService")) ;
  	for(ForumLinkData linkData : forumLinks) {
  		list.add(new SelectItemOption<String>(linkData.getName(), linkData.getPath())) ;
		}
  	this.getChild(UIFormSelectBoxForum.class).setOptions(list) ;
  }
  
  public void setValueOption(String path) throws Exception {
  	this.path = path ;
	  this.getChild(UIFormSelectBoxForum.class).setValue(path.trim()) ;
  }
  
  static  public class SelectActionListener extends EventListener<UIForumLinks> {
    public void execute(Event<UIForumLinks> event) throws Exception {
      UIForumLinks uiForm = event.getSource() ;
      UIFormSelectBoxForum selectBoxForum = uiForm.getUIFormSelectBoxForum(FIELD_FORUMLINK_SELECTBOX) ;
      String path = selectBoxForum.getValue();
      if(!path.equals(uiForm.path)) {
      	uiForm.path = path ;
	      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
	      if(path.indexOf("orumServic") > 0) {
	      	UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
	      	categoryContainer.updateIsRender(true) ;
	      	forumPortlet.updateIsRendered(1);
	      }else if(path.indexOf("forum") > 0) {
	      	String id[] = path.trim().split("/");
	      	forumPortlet.updateIsRendered(2);
	      	UIForumContainer forumContainer = forumPortlet.findFirstComponentOfType(UIForumContainer.class);
	      	forumContainer.setIsRenderChild(true) ;
	      	forumContainer.getChild(UIForumDescription.class).setForumIds(id[0], id[1]);
	      	forumContainer.getChild(UITopicContainer.class).updateByBreadcumbs(id[0], id[1], true) ;
	      }else {
	      	UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
	      	categoryContainer.getChild(UICategory.class).updateByBreadcumbs(path.trim()) ;
	        categoryContainer.updateIsRender(false) ;
	        forumPortlet.updateIsRendered(1);
	      }
	      forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(path.trim());
	      event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
      }
    }
  }
}
