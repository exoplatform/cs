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
      @EventConfig(listeners = UITopicPoll.RemovePollActionListener.class ),
      @EventConfig(listeners = UITopicPoll.VoteAgainPollActionListener.class )
    }
)
public class UITopicPoll extends UIForm  {
  private ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
  private Poll poll_ ;
  private String categoryId, forumId, topicId ;
  private boolean isMultiCheck = false ;
  public UITopicPoll() throws Exception {
  }

  private void init() throws Exception {
      if(this.hasChildren()) {
        this.removeChild(UIFormRadioBoxInput.class) ;
      }
      List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
      if(poll_ != null) {
        for (String s : poll_.getOption()) {
          options.add( new SelectItemOption<String>(s, s) ) ;
        }
      }
      UIFormRadioBoxInput input = new UIFormRadioBoxInput("vote", "vote", options);
      input.setAlign(1) ;
      addUIFormInput(input);
  }
  
  @SuppressWarnings("unused")
  private Poll getPoll() throws Exception {
    if(categoryId != null && categoryId.length() > 0) {
      Topic topic = forumService.getTopic(categoryId, forumId, topicId, false) ;
      if(topic.getIsPoll()) {
        Poll poll = forumService.getPoll(categoryId, forumId, topicId) ; 
        poll_ = poll ;
        this.init() ;
        return poll ;
      }
    }
    return null ;
  }
  
  public void updatePoll(String categoryId, String forumId, String topicId) throws Exception {
    this.categoryId = categoryId; 
    this.forumId = forumId; 
    this.topicId = topicId;
  }
  
  @SuppressWarnings("unused")
  private boolean getIsVoted() throws Exception {
    Poll poll = forumService.getPoll(categoryId, forumId, topicId) ;
    if(this.isMultiCheck) {
      return false ;
    }
    String userVote = Util.getPortalRequestContext().getRemoteUser() ;
    String[] userVotes = poll.getUserVote() ;
    for (String string : userVotes) {
      string = string.substring(0, string.length() - 2) ;
      if(string.equalsIgnoreCase(userVote)) return true ;
    }
    if(poll_.getTimeOut() > 0) {
    Date today = new Date() ;
    if((today.getTime() - this.poll_.getCreatedDate().getTime()) >= poll_.getTimeOut()*86400000) return true ;
    }
    return false ;
  }
  
  @SuppressWarnings("unused")
  private String[] getInfoVote() throws Exception {
    Poll poll = poll_ ;
    String[] voteNumber = poll.getVote() ;
    long size = poll.getUserVote().length  ;
    String[] infoVote = new String[(voteNumber.length + 1)] ;
    int i = 0;
    for (String string : voteNumber) {
      double tmp = Double.parseDouble(string) ;
      double k = (double)(tmp*size)/100 ;
      int t = (int)Math.round(k) ;
      string = "" + (double) t*100/size ;
      infoVote[i] = string + ":" + t ;
      i = i + 1 ;
    }
    infoVote[i] = "" + size ;
    return infoVote ;
  }
  
  @SuppressWarnings("unused")
  private String[] getColor() throws Exception {
    return new String[] {"blue", "DarkGoldenRod", "green", "yellow", "BlueViolet", "orange","darkBlue", "IndianRed","DarkCyan" ,"lawnGreen"} ; 
  }
  
  static public class VoteActionListener extends EventListener<UITopicPoll> {
    public void execute(Event<UITopicPoll> event) throws Exception {
      UITopicPoll topicPoll = event.getSource() ;
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
        // order number
        List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
        options = radioInput.getOptions() ;
        int i = 0, j = 0 ;
        for (SelectItemOption<String> option : options) {
          if(option.getValue().equalsIgnoreCase(radioInput.getValue())){ j = i ; break ;}
          i = i + 1;
        }
        //User vote and vote number
        String[] temporary = topicPoll.poll_.getUserVote() ;
        int size = 0 ;
        if(temporary != null && temporary.length > 0) {
          size = temporary.length ;
        }
        String[] setUserVote ; int index = 0 ;
        String userVote = Util.getPortalRequestContext().getRemoteUser() ;
        if(topicPoll.isMultiCheck) {
          setUserVote = new String[size] ;
          for (int t = 0; t < size; t++) {
            String string = temporary[t].substring(0, temporary[t].length() - 2) ;
            if(string.equalsIgnoreCase(userVote)) {
              setUserVote[t] = userVote + ":" + j;
              index = t;
            } else {
              setUserVote[t] = temporary[t];
            }
          }
        } else {
          setUserVote = new String[(size+1)] ;
          for (int t = 0; t < size; t++) {
            setUserVote[t] = temporary[t];
          }
          setUserVote[size] = userVote + ":" + j;
          size = size + 1 ;
        }
        String[] votes = topicPoll.poll_.getVote() ;
        double onePercent = (double)100/size;
        if(topicPoll.isMultiCheck) {
          char tmp = temporary[index].charAt((temporary[index].length() - 1));
          int k = (new Integer(tmp)).intValue() - 48;
          if( k < votes.length) votes[k] = String.valueOf((Double.parseDouble(votes[k]) - onePercent)) ;
          votes[j] = String.valueOf((Double.parseDouble(votes[j]) + onePercent)) ;
        } else {
          i = 0;
          for(String vote : votes) {
            double a  = Double.parseDouble(vote) ;
            if(i == j) votes[i] = "" + ((a - a/size)+ onePercent) ;
            else votes[i] = "" + (a - a/size) ;
            i = i + 1;
          }
        }
        //save Poll
        Poll poll = new Poll() ; 
        poll.setId(topicPoll.poll_.getId()) ;
        poll.setVote(votes) ;
        poll.setUserVote(setUserVote) ;
        topicPoll.forumService.savePoll(topicPoll.categoryId, topicPoll.forumId, topicPoll.topicId, poll, false, true) ;
        topicPoll.isMultiCheck = false ;
        event.getRequestContext().addUIComponentToUpdateByAjax(topicPoll.getParent()) ;
      }
    }
  }
  
  static public class EditPollActionListener extends EventListener<UITopicPoll> {
    public void execute(Event<UITopicPoll> event) throws Exception {
      UITopicPoll topicPoll = event.getSource() ;
      UIForumPortlet forumPortlet = topicPoll.getAncestorOfType(UIForumPortlet.class) ;
      UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
      UIPollForm  pollForm = popupAction.createUIComponent(UIPollForm.class, null, null) ;
      String path = topicPoll.categoryId + "/" + topicPoll.forumId + "/" + topicPoll.topicId;
      pollForm.setTopicPath(path) ;
      pollForm.setUpdatePoll(topicPoll.poll_, true) ;
      popupAction.activate(pollForm, 662, 466) ;
    }
  }

  static public class RemovePollActionListener extends EventListener<UITopicPoll> {
    public void execute(Event<UITopicPoll> event) throws Exception {
      UITopicPoll topicPoll = event.getSource() ;
      topicPoll.forumService.removePoll(topicPoll.categoryId, topicPoll.forumId, topicPoll.topicId) ;
      topicPoll.removeChild(UIFormRadioBoxInput.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(topicPoll.getParent()) ;
    }
  }

  static public class VoteAgainPollActionListener extends EventListener<UITopicPoll> {
    public void execute(Event<UITopicPoll> event) throws Exception {
      UITopicPoll topicPoll = event.getSource() ;
      topicPoll.isMultiCheck = true ;
      topicPoll.removeChild(UIFormRadioBoxInput.class) ;
      topicPoll.init() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(topicPoll) ;
    }
  }
  
}
