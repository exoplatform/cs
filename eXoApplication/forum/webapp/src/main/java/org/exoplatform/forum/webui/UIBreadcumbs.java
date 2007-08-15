/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.webui;

import javax.jcr.Node;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
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
    template =  "app:/templates/forum/webui/UIBreadcumbs.gtmpl" ,
    events = {
        @EventConfig(listeners = UIBreadcumbs.ChangePathActionListener.class),
        @EventConfig(listeners = UIBreadcumbs.RssActionListener.class)
    }
)
public class UIBreadcumbs extends UIContainer {
	private Node currentNode_ ;
	private String[] breadcumbs ;
	private String forumHomePath_ ;
  public UIBreadcumbs()throws Exception {
  	ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
  	forumHomePath_ = forumService.getForumHomePath() ;
  	
  }
  
  public void setCurrentNode(Node selectedNode) throws Exception {
  	currentNode_ = selectedNode ;
  	//forumService_.getForumHomePath() ;
  	
  }
  static public class ChangePathActionListener extends EventListener<UIBreadcumbs> {
    public void execute(Event<UIBreadcumbs> event) throws Exception {
      UIBreadcumbs uiActionBar = event.getSource() ;      
      System.out.println("====================> testOpen");
    }
  }  
  
  static public class RssActionListener extends EventListener<UIBreadcumbs> {
    public void execute(Event<UIBreadcumbs> event) throws Exception {
      UIBreadcumbs uiActionBar = event.getSource() ;      
    }
  }  

  
  
}