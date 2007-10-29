/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.webui;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Poll;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SARL
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Octo 26, 2007 9:48:18 AM 
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class ,
    template =  "app:/templates/forum/webui/UITopicPoll.gtmpl", 
    events = {
      @EventConfig(listeners = UITopicPoll.VoteActionListener.class )  
    }
)
public class UITopicPoll extends UIForm  {
  private ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
  private Poll poll_ ;
  private String categoryId, forumId, topicId ;
  public UITopicPoll() throws Exception {
    
  }

  private Poll getPoll() throws Exception {
    Poll poll = forumService.getPoll(categoryId, forumId, topicId) ; 
    poll_ = poll ;
    return poll ;
  }
  
  public void updatePoll(String categoryId, String forumId, String topicId) throws Exception {
    this.categoryId = categoryId; 
    this.forumId = forumId; 
    this.topicId = topicId;
  }
  
  private boolean getIsVoted(String userVote) throws Exception {
    Poll poll = poll_ ;
    if(poll.getUserVote().contains(userVote)) return true ;
    return false ;
  }
  
  private long[] getVoteNumber() throws Exception {
    Poll poll = poll_ ;
    String[] voteNumber = poll_.getVote() ;
    long[] temp = new long[voteNumber.length] ;
    for (int i = 0; i < voteNumber.length; i++) {
      temp[i] = Long.parseLong(voteNumber[i]);
    }
    return temp ;
  }
  
  private long getSumVote() throws Exception {
    long[] sumVote = this.getVoteNumber();
    long temp = 0;
    for (int i = 0; i < sumVote.length; i++) {
      temp = temp + sumVote[i] ;
    }
    return temp ;
  }
  
  private String[] getColor() throws Exception {
    return new String[] {"blue", "DarkGoldenRod", "green", "yellow", "ndianRed", "orange","darkBlue", "wheat","DarkCyan" ,"lawnGreen"} ; 
  }
  static public class VoteActionListener extends EventListener<UITopicPoll> {
    public void execute(Event<UITopicPoll> event) throws Exception {
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;      
    }
  }
}
