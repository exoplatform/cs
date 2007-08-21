/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.webui;

import org.exoplatform.forum.webui.popup.UIPopupComponent;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
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
    template =  "app:/templates/forum/webui/UITopicContainer.gtmpl", 
    events = {
      @EventConfig(listeners = UITopicContainer.AddTopicActionListener.class ),  
      @EventConfig(listeners = UITopicContainer.OpenTopicActionListener.class )  
    }
)
public class UITopicContainer extends UIForm implements UIPopupComponent {
  public UITopicContainer() throws Exception {
    // render Topic page list
    // render topic action bar
    // render topic page list
  }
  
  public void activate() throws Exception {
  	// TODO Auto-generated method stub
  }
  
  public void deActivate() throws Exception {
  	// TODO Auto-generated method stub
  }

  static public class AddTopicActionListener extends EventListener<UITopicContainer> {
    public void execute(Event<UITopicContainer> event) throws Exception {
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;      
    }
  }

  static public class OpenTopicActionListener extends EventListener<UITopicContainer> {
  	public void execute(Event<UITopicContainer> event) throws Exception {
  		UITopicContainer uiTopicContainer = event.getSource();
  		String path = event.getRequestContext().getRequestParameter(OBJECTID) ; 
  		System.out.println("\n\n topicId:  " + path);
  		UIForumContainer uiForumContainer = uiTopicContainer.getAncestorOfType(UIForumContainer.class) ;
  		uiForumContainer.getChild(UITopicDetailContainer.class).setRendered(true) ;
  		uiForumContainer.getChild(UITopicContainer.class).setRendered(false) ;
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
      context.addUIComponentToUpdateByAjax(uiForumContainer) ;
  	}
  }

  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
}
