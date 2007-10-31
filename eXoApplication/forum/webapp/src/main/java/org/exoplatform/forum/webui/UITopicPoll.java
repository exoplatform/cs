/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.webui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Poll;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.webui.popup.UIPollForm;
import org.exoplatform.forum.webui.popup.UIPopupAction;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
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
      @EventConfig(listeners = UITopicPoll.VoteActionListener.class ),  
      @EventConfig(listeners = UITopicPoll.EditPollActionListener.class ) ,
      @EventConfig(listeners = UITopicPoll.RemovePollActionListener.class )
    }
)
public class UITopicPoll extends UIForm  {
  private ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
  private Poll poll_ ;
  private Topic topic_ ;
  private String categoryId, forumId, topicId ;
  public UITopicPoll() throws Exception {
  }

  private void init() throws Exception {
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
        topic_ = topic ;
      }
    }
  }
  
  private Poll getPoll() throws Exception {
    return poll_ ;
  }
  
  public void updatePoll(String categoryId, String forumId, String topicId) throws Exception {
    this.categoryId = categoryId; 
    this.forumId = forumId; 
    this.topicId = topicId;
    this.init() ;
  }
  
  private boolean getIsVoted() throws Exception {
    Poll poll = forumService.getPoll(categoryId, forumId, topicId) ;
    String userVote = Util.getPortalRequestContext().getRemoteUser() ;
    String[] userVotes = poll.getUserVote() ;
    for (String string : userVotes) {
      if(string.equalsIgnoreCase(userVote)) return true ;
    }
    if(poll_.getTimeOut() > 0) {
    Date today = new Date() ;
    if((today.getTime() - this.poll_.getCreatedDate().getTime()) >= poll_.getTimeOut()*86400000) return true ;
    }
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
    return new String[] {"blue", "DarkGoldenRod", "green", "yellow", "BlueViolet", "orange","darkBlue", "IndianRed","DarkCyan" ,"lawnGreen"} ; 
  }
  
  static public class VoteActionListener extends EventListener<UITopicPoll> {
    public void execute(Event<UITopicPoll> event) throws Exception {
      UITopicPoll topicPoll = event.getSource() ;
      String[] temporary = topicPoll.poll_.getUserVote() ;
      int size = 0 ;
      if(temporary != null && temporary.length > 0) {
        size = temporary.length ;
      }
      String[] setUserVote = new String[(size+1)] ;
      for (int t = 0; t < size; t++) {
        setUserVote[t] = temporary[t];
      }
      setUserVote[size] = Util.getPortalRequestContext().getRemoteUser() ;
      UIFormRadioBoxInput radioInput = null ;
      List<UIComponent> children = topicPoll.getChildren() ;
      for(UIComponent child : children) {
        if(child instanceof UIFormRadioBoxInput) {
          radioInput = (UIFormRadioBoxInput) child ;
        }
      }
      if(radioInput.getValue().equalsIgnoreCase("vote")) {
        UIApplication uiApp = topicPoll.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UITopicPoll.msg.notCheck", null, ApplicationMessage.WARNING)) ;
      } else {
        List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
        options = radioInput.getOptions() ;
        int i = 0, j = 0 ;
        for (SelectItemOption<String> option : options) {
          if(option.getValue().equalsIgnoreCase(radioInput.getValue())){ j = i ; break ;}
          i = i + 1;
        }
        String[] votes = topicPoll.poll_.getVote() ;
        String temp = votes[j] ;
        votes[j] = String.valueOf((Long.parseLong(temp) + 1)) ;
        Poll poll = new Poll() ; 
        poll.setId(topicPoll.poll_.getId()) ;
        poll.setVote(votes) ;
        poll.setUserVote(setUserVote) ;
        topicPoll.forumService.savePoll(topicPoll.categoryId, topicPoll.forumId, topicPoll.topicId, poll, false, true) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(topicPoll) ;
      }
    }
  }
  
  static public class EditPollActionListener extends EventListener<UITopicPoll> {
    public void execute(Event<UITopicPoll> event) throws Exception {
      UITopicPoll topicPoll = event.getSource() ;
      Topic topic = topicPoll.topic_ ;
      UIForumPortlet forumPortlet = topicPoll.getAncestorOfType(UIForumPortlet.class) ;
      UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
      UIPollForm  pollForm = popupAction.createUIComponent(UIPollForm.class, null, null) ;
      pollForm.setTopicPath(topic.getPath()) ;
      pollForm.setUpdatePoll(topicPoll.poll_, true) ;
      popupAction.activate(pollForm, 662, 466) ;
    }
  }

  static public class RemovePollActionListener extends EventListener<UITopicPoll> {
    public void execute(Event<UITopicPoll> event) throws Exception {
      UITopicPoll topicPoll = event.getSource() ;
//      UIApplication uiApp = topicPoll.getAncestorOfType(UIApplication.class) ;
//      uiApp.addMessage(new ApplicationMessage("UITopicPoll.msg.notCheck", null, ApplicationMessage.WARNING)) ;
      topicPoll.forumService.removePoll(topicPoll.categoryId, topicPoll.forumId, topicPoll.topicId) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(topicPoll.getParent()) ;
    }
  }
  
}
