/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.webui.popup;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SARL
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * November 01 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/forum/webui/popup/UIRatingForm.gtmpl",
    events = {
      @EventConfig(listeners = UIRatingForm.VoteTopicActionListener.class), 
      @EventConfig(listeners = UIRatingForm.CancelActionListener.class,phase = Phase.DECODE)
    }
)
public class UIRatingForm extends UIForm implements UIPopupComponent {
  private Topic topic ;
  private String categoryId ;
  private String forumId ;
  
  public UIRatingForm() throws Exception {
    
  }
  
  public void updateRating(Topic topic,  String categoryId, String forumId) {
    this.topic = topic ;
    this.categoryId = categoryId ;
    this.forumId = forumId ;
  }
  public void activate() throws Exception {
		// TODO Auto-generated method stub
	}
	public void deActivate() throws Exception {
		// TODO Auto-generated method stub
	}
  
  static  public class VoteTopicActionListener extends EventListener<UIRatingForm> {
    public void execute(Event<UIRatingForm> event) throws Exception {
      UIRatingForm uiForm = event.getSource() ;
      String vote = event.getRequestContext().getRequestParameter(OBJECTID)  ;
      Topic topic = uiForm.topic ;
      
      String userName = Util.getPortalRequestContext().getRemoteUser() ;
      String[] Vote = topic.getUserVoteRating() ;
      int k = Vote.length ;
      Double voteRating = topic.getVoteRating() ;
      voteRating = (voteRating*k + Integer.parseInt(vote))/(k+1) ;
      String[] temp = new String[k + 1] ;
      for (int i = 0; i < k; i++) {
        temp[i] = Vote[i] ;
      }
      temp[k] = userName ;
      topic.setVoteRating(voteRating) ;
      topic.setUserVoteRating(temp) ;
      ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
      forumService.saveTopic(uiForm.categoryId, uiForm.forumId, topic, false) ;
      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
      forumPortlet.cancelAction() ;
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIRatingForm> {
    public void execute(Event<UIRatingForm> event) throws Exception {
      UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
      forumPortlet.cancelAction() ;
    }
  }
}
