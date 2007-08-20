/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.webui;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.services.jcr.util.IdGenerator ;

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
	private List<String> breadcumbs_ = new ArrayList<String>();
	private String forumHomePath_ ;
  public UIBreadcumbs()throws Exception {
  	ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
  	forumHomePath_ = forumService.getForumHomePath() ;
    breadcumbs_.add(forumHomePath_) ;
    IdGenerator.generate() ;
  }
  
  public void setCurrentNode(Node selectedNode) throws Exception {
  	//currentNode_ = selectedNode ;
    breadcumbs_ = new ArrayList<String> () ;
    if(selectedNode.getPath().length() > forumHomePath_.length()) {
      Node parentNode = selectedNode.getParent() ;
      while(!forumHomePath_.equals(parentNode.getPath())) {
        breadcumbs_.add(parentNode.getPath()) ;
        parentNode = parentNode.getParent() ;
      }
    }
    breadcumbs_.add(forumHomePath_) ;
  }
  
  private List<String> getBreadcumbs() throws Exception {
    return breadcumbs_ ;
  }
  
  static public class ChangePathActionListener extends EventListener<UIBreadcumbs> {
    public void execute(Event<UIBreadcumbs> event) throws Exception {
      UIBreadcumbs uiBreadcums = event.getSource() ;      
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;
      SessionProviderService service = 
        (SessionProviderService)PortalContainer.getComponent(SessionProviderService.class) ;
      String userId = Util.getPortalRequestContext().getRemoteUser() ;
      SessionProvider sessionProvider = service.getSessionProvider(userId) ;
      if(sessionProvider == null) {
        sessionProvider = new SessionProvider(null) ;
        service.setSessionProvider(userId, sessionProvider) ;
      }
      RepositoryService repositoryService = (RepositoryService)PortalContainer.getComponent(RepositoryService.class) ;
      String defaultWS = repositoryService.getCurrentRepository().getConfiguration().getDefaultWorkspaceName() ;
      Session session = sessionProvider.getSession(defaultWS, repositoryService.getCurrentRepository()) ;
      Node selectNode = (Node)session.getItem(path) ;
      String type = selectNode.getPrimaryNodeType().getName() ;
      uiBreadcums.setCurrentNode(selectNode) ;
      UIForumPortlet forumPortelt = uiBreadcums.getAncestorOfType(UIForumPortlet.class) ;
      if(type.equals("exo:forum") || type.equals("exo:topic")) {
        forumPortelt.getChild(UICategoryContainer.class).setRendered(false) ;
        forumPortelt.getChild(UIForumContainer.class).setRendered(true) ;
      }else if(type.equals("exo:forumCategory")) {
        forumPortelt.getChild(UICategoryContainer.class).setRendered(false) ;
        forumPortelt.getChild(UICategoryContainer.class).getChild(UICategories.class).setRendered(false) ;
        forumPortelt.getChild(UICategoryContainer.class).getChild(UICategory.class).setRendered(true) ;
        forumPortelt.getChild(UIForumContainer.class).setRendered(true) ;        
      }else { //forum home        
        forumPortelt.getChild(UICategoryContainer.class).setRendered(true) ;
        forumPortelt.getChild(UICategoryContainer.class).getChild(UICategories.class).setRendered(true) ;
        forumPortelt.getChild(UICategoryContainer.class).getChild(UICategory.class).setRendered(false) ;
        forumPortelt.getChild(UIForumContainer.class).setRendered(false) ;        
      }
    }
  }  
  
  static public class RssActionListener extends EventListener<UIBreadcumbs> {
    public void execute(Event<UIBreadcumbs> event) throws Exception {
      UIBreadcumbs uiActionBar = event.getSource() ;      
    }
  }  

  
  
}