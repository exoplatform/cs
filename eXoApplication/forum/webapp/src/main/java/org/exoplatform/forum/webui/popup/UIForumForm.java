/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.webui.popup;

import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
//    template = "system:/groovy/webui/form/UIFormWithTitle.gtmpl",
    template = "app:/templates/forum/webui/popup/UIAddNewForum.gtmpl",
    events = {
      @EventConfig(listeners = UIForumForm.SaveActionListener.class), 
      @EventConfig(listeners = UIForumForm.CancelActionListener.class, phase=Phase.DECODE)
    }
)
public class UIForumForm extends UIForm implements UIPopupComponent{
  
  
  public UIForumForm() {
    
  }
  
  public void activate() throws Exception {
		// TODO Auto-generated method stub
		
	}
	public void deActivate() throws Exception {
		// TODO Auto-generated method stub
	}
  
  static  public class SaveActionListener extends EventListener<UIForumForm> {
    public void execute(Event<UIForumForm> event) throws Exception {
      UIForumForm uiForm = event.getSource() ;
      
      
      UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
      forumPortlet.cancelAction() ;
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIForumForm> {
    public void execute(Event<UIForumForm> event) throws Exception {
      UIForumForm uiForm = event.getSource() ;
      UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
      forumPortlet.cancelAction() ;
    }
  }
}
