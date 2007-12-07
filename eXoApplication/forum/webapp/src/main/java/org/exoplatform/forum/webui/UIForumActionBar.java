/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.webui;

import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.service.ForumService;
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
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
    template =  "app:/templates/forum/webui/UIForumActionBar.gtmpl", 
    events = {
        @EventConfig(listeners = UIForumActionBar.AddCategoryActionListener.class),
        @EventConfig(listeners = UIForumActionBar.AddForumActionListener.class),
        @EventConfig(listeners = UIForumActionBar.ManageModeratorActionListener.class),
        @EventConfig(listeners = UIForumActionBar.ForumOptionActionListener.class)
    }
)
public class UIForumActionBar extends UIContainer  {
  public UIForumActionBar() throws Exception {    
  } 
  
  static public class AddCategoryActionListener extends EventListener<UIForumActionBar> {
    public void execute(Event<UIForumActionBar> event) throws Exception {
      UIForumActionBar uiActionBar = event.getSource() ;
      UIForumPortlet forumPortlet = uiActionBar.getAncestorOfType(UIForumPortlet.class) ;
      UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
      popupAction.activate(UICategoryForm.class, 600) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }  
  
  static public class AddForumActionListener extends EventListener<UIForumActionBar> {
    @SuppressWarnings("unchecked")
    public void execute(Event<UIForumActionBar> event) throws Exception {
      UIForumActionBar uiActionBar = event.getSource() ;      
      ForumService forumService =  (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
      List cates = forumService.getCategories() ;
      if(cates.size() > 0) {
        UIForumPortlet forumPortlet = uiActionBar.getAncestorOfType(UIForumPortlet.class) ;
        UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
        UIForumForm forumForm = popupAction.createUIComponent(UIForumForm.class, null, null) ;
        forumForm.setCategoryValue("", true) ;
        forumForm.setForumUpdate(false) ;
        popupAction.activate(forumForm, 662, 466) ;
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
    public void execute(Event<UIForumActionBar> event) throws Exception {
      UIForumActionBar uiActionBar = event.getSource() ;
        UIForumPortlet forumPortlet = uiActionBar.getAncestorOfType(UIForumPortlet.class) ;
        UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
        popupAction.activate(UIModeratorManagementForm.class, 662) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }  
  
  static public class ForumOptionActionListener extends EventListener<UIForumActionBar> {
    public void execute(Event<UIForumActionBar> event) throws Exception {
      UIForumActionBar uiActionBar = event.getSource() ;
      UIForumPortlet forumPortlet = uiActionBar.getAncestorOfType(UIForumPortlet.class) ;
      UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
      UIForumOptionForm forumOptionForm = popupAction.createUIComponent(UIForumOptionForm.class, null, null) ;
      popupAction.activate(forumOptionForm, 662, 280) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }  
}
