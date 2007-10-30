/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.webui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Poll;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormRadioBoxInput;

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

  private void init() throws Exception {
    System.out.println("\n\n==================>  Init");
    if(categoryId != null && categoryId.length() > 0) {
      Topic topic = forumService.getTopic(categoryId, forumId, topicId, false) ;
      if(topic.getIsPoll()) {
        Poll poll = forumService.getPoll(categoryId, forumId, topicId) ; 
        List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
        if(poll != null) {
          for (String s : poll.getOption()) {
            options.add( new SelectItemOption<String>(s, s) ) ;
          }
        }
        UIFormRadioBoxInput input = new UIFormRadioBoxInput("vote", "vote", options);
        input.setAlign(1) ;
        addUIFormInput(input);
        poll_ = poll ;
      }
    }
  }
  
  private Poll getPoll() throws Exception {
    System.out.println("\n\n==================>  Get:  ");
    return poll_ ;
  }
  
  public void updatePoll(String categoryId, String forumId, String topicId) throws Exception {
    System.out.println("\n\n==================>  Update:  ");
    this.categoryId = categoryId; 
    this.forumId = forumId; 
    this.topicId = topicId;
    this.init() ;
  }
  
  private boolean getIsVoted() throws Exception {
    Poll poll = poll_ ;
    String userVote = Util.getPortalRequestContext().getRemoteUser() ;
    String[] userVotes = poll.getUserVote() ;
    for (String string : userVotes) {
      if(string.equalsIgnoreCase(userVote)) return true ;
    }
    Date today = new Date() ;
    if((today.getTime() - this.poll_.getCreatedDate().getTime()) >= poll_.getTimeOut()*86400000) return true ;
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
      UITopicPoll topicPoll = event.getSource() ;
      Poll poll = topicPoll.poll_ ;
      String[] temporary = poll.getUserVote() ;
      int size = 0 ;
      if(temporary != null && temporary.length > 0) {
        size = temporary.length ;
      }
      String[] setUserVote = new String[(size+1)] ;
      for (int t = 0; t < size; t++) {
        setUserVote[t] = temporary[t];
      }
      setUserVote[size] = Util.getPortalRequestContext().getRemoteUser() ;
      poll.setUserVote(setUserVote) ;
      UIFormRadioBoxInput radioInput = null ;
      List<UIComponent> children = topicPoll.getChildren() ;
      for(UIComponent child : children) {
        if(child instanceof UIFormRadioBoxInput) {
          radioInput = (UIFormRadioBoxInput) child ;
        }
      }
      List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
      options = radioInput.getOptions() ;
      int i = 0, j = 0 ;
      for (SelectItemOption<String> option : options) {
        if(option.getValue().equalsIgnoreCase(radioInput.getValue())){ j = i ; break ;}
        i = i + 1;
      }
      String[] votes = poll.getVote() ;
      String temp = votes[j] ;
      votes[j] = "" + (Long.parseLong(temp) + 1) ;
      if(radioInput.getValue().equalsIgnoreCase("vote")) {
        System.out.println("\n\n==================>  NotCheck");
      }
      poll.setVote(votes) ;
      topicPoll.forumService.savePoll(topicPoll.categoryId, topicPoll.forumId, topicPoll.topicId, poll, false, true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(topicPoll.getParent()) ;
    }
  }
}
