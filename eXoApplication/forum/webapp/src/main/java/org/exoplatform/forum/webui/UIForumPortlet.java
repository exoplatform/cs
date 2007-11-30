/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.webui;

import org.exoplatform.forum.webui.popup.UIPopupAction;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPopupMessages;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/**
 * Author : Nguyen Quang Hung
 *          hung.nguyen@exoplatform.com
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
    addChild(UIBannerContainer.class, null, null) ;
    addChild(UIBreadcumbs.class, null, null) ;
    addChild(UICategoryContainer.class, null, null).setRendered(isCategoryRendered) ;
    addChild(UIForumContainer.class, null, null).setRendered(isForumRendered) ;
    addChild(UIForumLinks.class, null, null).setRendered(isForumRendered) ;
    addChild(UIPostPreview.class, null, null).setRendered(isPostRendered) ;
    addChild(UIPopupAction.class, null, null) ;
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
	  getChild(UIPostPreview.class).setRendered(isPostRendered) ;
  }
	
	public void renderPopupMessages() throws Exception {
    UIPopupMessages popupMess = getUIPopupMessages();
    if(popupMess == null)  return ;
    WebuiRequestContext  context =  WebuiRequestContext.getCurrentInstance() ;
    popupMess.processRender(context);
  }

	public void cancelAction() throws Exception {
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
    UIPopupAction popupAction = getChild(UIPopupAction.class) ;
    popupAction.deActivate() ;
    context.addUIComponentToUpdateByAjax(popupAction) ;
  }
}