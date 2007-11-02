/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.webui;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.webui.popup.UIForumForm;
import org.exoplatform.forum.webui.popup.UIMoveForumForm;
import org.exoplatform.forum.webui.popup.UIMoveTopicForm;
import org.exoplatform.forum.webui.popup.UIPopupAction;
import org.exoplatform.forum.webui.popup.UIPopupComponent;
import org.exoplatform.forum.webui.popup.UITopicForm;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;

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
      @EventConfig(listeners = UITopicContainer.OpenTopicActionListener.class ),
      @EventConfig(listeners = UITopicContainer.DisplayOptionActionListener.class ),
      @EventConfig(listeners = UITopicContainer.EditForumActionListener.class ),  
      @EventConfig(listeners = UITopicContainer.SetLockedForumActionListener.class),
      @EventConfig(listeners = UITopicContainer.SetUnLockForumActionListener.class),
      @EventConfig(listeners = UITopicContainer.SetOpenForumActionListener.class),
      @EventConfig(listeners = UITopicContainer.SetCloseForumActionListener.class),
      @EventConfig(listeners = UITopicContainer.MoveForumActionListener.class),
      @EventConfig(listeners = UITopicContainer.RemoveForumActionListener.class),
      @EventConfig(listeners = UITopicContainer.EditTopicActionListener.class),
      @EventConfig(listeners = UITopicContainer.SetOpenTopicActionListener.class),
      @EventConfig(listeners = UITopicContainer.SetCloseTopicActionListener.class),
      @EventConfig(listeners = UITopicContainer.SetLockedTopicActionListener.class),
      @EventConfig(listeners = UITopicContainer.SetUnLockTopicActionListener.class),
      @EventConfig(listeners = UITopicContainer.SetStickTopicActionListener.class),
      @EventConfig(listeners = UITopicContainer.SetUnStickTopicActionListener.class),
      @EventConfig(listeners = UITopicContainer.SetMoveTopicActionListener.class),
      @EventConfig(listeners = UITopicContainer.SetDeleteTopicActionListener.class)
    }
)
public class UITopicContainer extends UIForm implements UIPopupComponent {
  private ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
  private String forumId = "";
  private String categoryId = "";
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
  
  public void setUpdateForum(String categoryId, String forumId) {
    this.forumId = forumId ;
    this.categoryId = categoryId ;
  }
  
  private Forum getForum() throws Exception {
    return forumService.getForum(categoryId, forumId);
  }
  
  private JCRPageList getPageTopics() throws Exception {
    return forumService.getPageTopic(categoryId, forumId);
  }

  private String[] getActionMenuForum() throws Exception {
    String []actions = {"DisplayOptions", "EditForum", "SetLockedForum", "SetUnLockForum", "SetOpenForum", "SetCloseForum", "MoveForum", "RemoveForum"};
    return actions;
  }

  private String[] getActionMenuTopic() throws Exception {
    String []actions = {"EditTopic", "SetOpenTopic", "SetCloseTopic", "SetLockedTopic", "SetUnLockTopic", "SetStickTopic", "SetUnStickTopic", "SetMoveTopic", "SetDeleteTopic"}; 
    return actions;
  }
  
  
  private List<Topic> getTopicPageLits(long page) throws Exception {
    JCRPageList pageList = getPageTopics();
    List<Topic> topicList = this.forumService.getPage(page, pageList);
    for(Topic topic : topicList) {
      if(getUIFormCheckBoxInput(topic.getId()) != null) {
        getUIFormCheckBoxInput(topic.getId()).setChecked(false) ;
      }else {
        addUIFormInput(new UIFormCheckBoxInput(topic.getId(), topic.getId(), false) );
      }
    }
    return topicList ;
  }
  
  private Topic getTopic(String topicId) throws Exception {
    return  this.forumService.getTopic(this.categoryId, this.forumId, topicId, false) ;
  }
  
  private int getStarNumber(Topic topic) throws Exception {
    String []temp = topic.getVoteRating() ;
    int i = 0,j = 0, k = 0, t = 0, l = 0, star = 0;
    for (String string : temp) {
      if(Integer.valueOf(string).intValue() == 1) i = i + 1;
      if(Integer.valueOf(string).intValue() == 2) j = j + 1;
      if(Integer.valueOf(string).intValue() == 3) k = k + 1;
      if(Integer.valueOf(string).intValue() == 4) t = t + 1;
      if(Integer.valueOf(string).intValue() == 5) l = l + 1;
    }
    if((i+j+k+t+l) > 0) {
      float fl = (i*1+j*2+k*3+t*4+l*5) / (i+j+k+t+l) ;
      star = Math.round(fl) ;
    }
    return star ;
  }
  
  static public class AddTopicActionListener extends EventListener<UITopicContainer> {
    public void execute(Event<UITopicContainer> event) throws Exception {
      UITopicContainer uiTopicContainer = event.getSource() ;
      UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
      UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
      UITopicForm topicForm = popupAction.createUIComponent(UITopicForm.class, null, null) ;
      topicForm.setTopicIds(uiTopicContainer.categoryId, uiTopicContainer.forumId) ;
      popupAction.activate(topicForm, 670, 440) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  static public class OpenTopicActionListener extends EventListener<UITopicContainer> {
  	public void execute(Event<UITopicContainer> event) throws Exception {
  		UITopicContainer uiTopicContainer = event.getSource();
  		String topicId = event.getRequestContext().getRequestParameter(OBJECTID) ; 
  		UIForumContainer uiForumContainer = uiTopicContainer.getAncestorOfType(UIForumContainer.class) ;
      UITopicDetailContainer uiTopicDetailContainer = uiForumContainer.getChild(UITopicDetailContainer.class) ;
      uiForumContainer.setIsRenderChild(false) ;
      UITopicDetail uiTopicDetail = uiTopicDetailContainer.getChild(UITopicDetail.class) ;
      uiTopicDetail.setUpdateTopic(uiTopicContainer.categoryId, uiTopicContainer.forumId, topicId, true) ;
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
      context.addUIComponentToUpdateByAjax(uiForumContainer) ;
  	}
  }

  static public class EditForumActionListener extends EventListener<UITopicContainer> {
    public void execute(Event<UITopicContainer> event) throws Exception {
      UITopicContainer uiTopicContainer = event.getSource();
      Forum forum = uiTopicContainer.getForum() ;
      UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
      UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
      UIForumForm forumForm = popupAction.createUIComponent(UIForumForm.class, null, null) ;
      forumForm.setCategoryValue(uiTopicContainer.categoryId, false) ;
      forumForm.setForumValue(forum, true);
      forumForm.setForumUpdate(true);
      popupAction.activate(forumForm, 662, 466) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }  
  
  static public class SetLockedForumActionListener extends EventListener<UITopicContainer> {
    public void execute(Event<UITopicContainer> event) throws Exception {
      UITopicContainer uiTopicContainer = event.getSource();
      Forum forum = uiTopicContainer.getForum() ;
      forum.setIsLock(true);
      uiTopicContainer.forumService.saveForum(uiTopicContainer.categoryId, forum, false) ;
      UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
    }
  }  

  static public class SetUnLockForumActionListener extends EventListener<UITopicContainer> {
    public void execute(Event<UITopicContainer> event) throws Exception {
      UITopicContainer uiTopicContainer = event.getSource();
      Forum forum = uiTopicContainer.getForum() ;
      forum.setIsLock(false);
      uiTopicContainer.forumService.saveForum(uiTopicContainer.categoryId, forum, false) ;
      UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
    }
  }  

  static public class SetOpenForumActionListener extends EventListener<UITopicContainer> {
    public void execute(Event<UITopicContainer> event) throws Exception {
      UITopicContainer uiTopicContainer = event.getSource();
      Forum forum = uiTopicContainer.getForum() ;
      forum.setIsClosed(false);
      uiTopicContainer.forumService.saveForum(uiTopicContainer.categoryId, forum, false) ;
      UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
    }
  }  

  static public class SetCloseForumActionListener extends EventListener<UITopicContainer> {
    public void execute(Event<UITopicContainer> event) throws Exception {
      UITopicContainer uiTopicContainer = event.getSource();
      Forum forum = uiTopicContainer.getForum() ;
      forum.setIsClosed(true);
      uiTopicContainer.forumService.saveForum(uiTopicContainer.categoryId, forum, false) ;
      UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
    }
  } 
  
  static public class DisplayOptionActionListener extends EventListener<UITopicContainer> {
    public void execute(Event<UITopicContainer> event) throws Exception {
      UITopicContainer uiTopicContainer = event.getSource();
      
    }
  }  
  
  static public class MoveForumActionListener extends EventListener<UITopicContainer> {
    public void execute(Event<UITopicContainer> event) throws Exception {
      UITopicContainer uiTopicContainer = event.getSource();
      Forum forum = uiTopicContainer.getForum() ;
      List <Forum> forums = new ArrayList<Forum>();
      forums.add(forum);
      UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
      UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
      UIMoveForumForm moveForumForm = popupAction.createUIComponent(UIMoveForumForm.class, null, null) ;
      moveForumForm.setListForum(forums, uiTopicContainer.categoryId);
      moveForumForm.setForumUpdate(true) ;
      popupAction.activate(moveForumForm, 400, 165) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }  
  
  static public class RemoveForumActionListener extends EventListener<UITopicContainer> {
    public void execute(Event<UITopicContainer> event) throws Exception {
      UITopicContainer uiTopicContainer = event.getSource();
      Forum forum = uiTopicContainer.getForum() ;
      UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
      uiTopicContainer.forumService.removeForum(uiTopicContainer.categoryId, forum.getId()) ;
      forumPortlet.updateIsRendered(1) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
    }
  }  
  
  //----------------------------------MenuThread---------------------------------
  

  static public class EditTopicActionListener extends EventListener<UITopicContainer> {
    public void execute(Event<UITopicContainer> event) throws Exception {
      UITopicContainer uiTopicContainer = event.getSource();
      List<UIComponent> children = uiTopicContainer.getChildren() ;
      Topic topic = null ;
      boolean checked = false ;
      for(UIComponent child : children) {
        if(child instanceof UIFormCheckBoxInput) {
          if(((UIFormCheckBoxInput)child).isChecked()) {
            topic = uiTopicContainer.getTopic(child.getName());
            checked = true ;
            break ;
          }
        }
      }
      UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
      if(checked) {
        UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
        UITopicForm topicForm = popupAction.createUIComponent(UITopicForm.class, null, null) ;
        topicForm.setTopicIds(uiTopicContainer.categoryId, uiTopicContainer.forumId) ;
        topicForm.setUpdateTopic(topic, true) ;
        popupAction.activate(topicForm, 662, 466) ;
      } else {
        Object[] args = { };
        throw new MessageException(new ApplicationMessage("UICategory.msg.notCheck", args, ApplicationMessage.WARNING)) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
    }
  }  
  
  static public class SetOpenTopicActionListener extends EventListener<UITopicContainer> {
    public void execute(Event<UITopicContainer> event) throws Exception {
      UITopicContainer uiTopicContainer = event.getSource();
      List<UIComponent> children = uiTopicContainer.getChildren() ;
      List <Topic> topics = new ArrayList<Topic>();
      String sms = "";
      int i = 0 ;
      for(UIComponent child : children) {
        if(child instanceof UIFormCheckBoxInput) {
          if(((UIFormCheckBoxInput)child).isChecked()) {
            topics.add(uiTopicContainer.getTopic(child.getName()));
            if(!topics.get(i).getIsClosed()){ sms = topics.get(i).getTopicName() ; break ;} 
            ++i ;
          }
        }
      }
      UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
      if(topics.size() > 0 && sms.length() == 0) {
        for(Topic topic : topics) {
          topic.setIsClosed(false) ;
          uiTopicContainer.forumService.saveTopic(uiTopicContainer.categoryId, uiTopicContainer.forumId, topic, false) ;
        }
      } 
      if(topics.size() == 0 && sms.length() == 0){
        Object[] args = { };
        throw new MessageException(new ApplicationMessage("UITopicContainer.sms.notCheck", args, ApplicationMessage.WARNING)) ;
      }
      if(sms.length() > 0){
        Object[] args = { sms };
        throw new MessageException(new ApplicationMessage("UITopicContainer.sms.Open", args, ApplicationMessage.WARNING)) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
    }
  }  

  static public class SetCloseTopicActionListener extends EventListener<UITopicContainer> {
    public void execute(Event<UITopicContainer> event) throws Exception {
      UITopicContainer uiTopicContainer = event.getSource();
      List<UIComponent> children = uiTopicContainer.getChildren() ;
      List <Topic> topics = new ArrayList<Topic>();
      String sms = "";
      int i = 0 ;
      for(UIComponent child : children) {
        if(child instanceof UIFormCheckBoxInput) {
          if(((UIFormCheckBoxInput)child).isChecked()) {
            topics.add(uiTopicContainer.getTopic(child.getName()));
            if(topics.get(i).getIsClosed()){ sms = topics.get(i).getTopicName() ; break ;} 
            ++i ;
          }
        }
      }
      UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
      if(topics.size() > 0 && sms.length() == 0) {
        for(Topic topic : topics) {
          topic.setIsClosed(true) ;
          uiTopicContainer.forumService.saveTopic(uiTopicContainer.categoryId, uiTopicContainer.forumId, topic, false) ;
        }
      } 
      if(topics.size() == 0 && sms.length() == 0){
        Object[] args = { };
        throw new MessageException(new ApplicationMessage("UITopicContainer.sms.notCheck", args, ApplicationMessage.WARNING)) ;
      }
      if(sms.length() > 0){
        Object[] args = { sms };
        throw new MessageException(new ApplicationMessage("UITopicContainer.sms.Close", args, ApplicationMessage.WARNING)) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
    }
  }  
  
  static public class SetLockedTopicActionListener extends EventListener<UITopicContainer> {
    public void execute(Event<UITopicContainer> event) throws Exception {
      UITopicContainer uiTopicContainer = event.getSource();
      List<UIComponent> children = uiTopicContainer.getChildren() ;
      List <Topic> topics = new ArrayList<Topic>();
      String sms = "";
      int i = 0 ;
      for(UIComponent child : children) {
        if(child instanceof UIFormCheckBoxInput) {
          if(((UIFormCheckBoxInput)child).isChecked()) {
            topics.add(uiTopicContainer.getTopic(child.getName()));
            if(topics.get(i).getIsLock()){ sms = topics.get(i).getTopicName() ; break ;} 
            ++i ;
          }
        }
      }
      UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
      if(topics.size() > 0 && sms.length() == 0) {
        for(Topic topic : topics) {
          topic.setIsLock(true) ;
          uiTopicContainer.forumService.saveTopic(uiTopicContainer.categoryId, uiTopicContainer.forumId, topic, false) ;
        }
      } 
      if(topics.size() == 0 && sms.length() == 0){
        Object[] args = { };
        throw new MessageException(new ApplicationMessage("UITopicContainer.sms.notCheck", args, ApplicationMessage.WARNING)) ;
      }
      if(sms.length() > 0){
        Object[] args = { sms };
        throw new MessageException(new ApplicationMessage("UITopicContainer.sms.Locked", args, ApplicationMessage.WARNING)) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
    }
  }
  
  static public class SetUnLockTopicActionListener extends EventListener<UITopicContainer> {
    public void execute(Event<UITopicContainer> event) throws Exception {
      UITopicContainer uiTopicContainer = event.getSource();
      List<UIComponent> children = uiTopicContainer.getChildren() ;
      List <Topic> topics = new ArrayList<Topic>();
      String sms = "";
      int i = 0 ;
      for(UIComponent child : children) {
        if(child instanceof UIFormCheckBoxInput) {
          if(((UIFormCheckBoxInput)child).isChecked()) {
            topics.add(uiTopicContainer.getTopic(child.getName()));
            if(!topics.get(i).getIsLock()){ sms = topics.get(i).getTopicName() ; break ;} 
            ++i ;
          }
        }
      }
      UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
      if(topics.size() > 0 && sms.length() == 0) {
        for(Topic topic : topics) {
          topic.setIsLock(false) ;
          uiTopicContainer.forumService.saveTopic(uiTopicContainer.categoryId, uiTopicContainer.forumId, topic, false) ;
        }
      } 
      if(topics.size() == 0 && sms.length() == 0){
        Object[] args = { };
        throw new MessageException(new ApplicationMessage("UITopicContainer.sms.notCheck", args, ApplicationMessage.WARNING)) ;
      }
      if(sms.length() > 0){
        Object[] args = { sms };
        throw new MessageException(new ApplicationMessage("UITopicContainer.sms.UnLock", args, ApplicationMessage.WARNING)) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
    }
  }  

  static public class SetUnStickTopicActionListener extends EventListener<UITopicContainer> {
	  public void execute(Event<UITopicContainer> event) throws Exception {
		  UITopicContainer uiTopicContainer = event.getSource();
		  List<UIComponent> children = uiTopicContainer.getChildren() ;
		  List <Topic> topics = new ArrayList<Topic>();
		  String sms = "";
		  int i = 0 ;
		  for(UIComponent child : children) {
			  if(child instanceof UIFormCheckBoxInput) {
				  if(((UIFormCheckBoxInput)child).isChecked()) {
					  topics.add(uiTopicContainer.getTopic(child.getName()));
					  if(!topics.get(i).getIsSticky()){ sms = topics.get(i).getTopicName() ; break ;} 
            ++i ;
				  }
			  }
		  }
		  UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
		  if(topics.size() > 0 && sms.length() == 0) {
			  for(Topic topic : topics) {
				  topic.setIsSticky(false) ;
				  uiTopicContainer.forumService.saveTopic(uiTopicContainer.categoryId, uiTopicContainer.forumId, topic, false) ;
			  }
		  } 
		  if(topics.size() == 0 && sms.length() == 0){
			  Object[] args = { };
			  throw new MessageException(new ApplicationMessage("UITopicContainer.sms.notCheck", args, ApplicationMessage.WARNING)) ;
		  }
		  if(sms.length() > 0){
			  Object[] args = { sms };
			  throw new MessageException(new ApplicationMessage("UITopicContainer.sms.UnStick", args, ApplicationMessage.WARNING)) ;
		  }
		  event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
	  }
  }  
  
  static public class SetStickTopicActionListener extends EventListener<UITopicContainer> {
	  public void execute(Event<UITopicContainer> event) throws Exception {
		  UITopicContainer uiTopicContainer = event.getSource();
		  List<UIComponent> children = uiTopicContainer.getChildren() ;
		  List <Topic> topics = new ArrayList<Topic>();
		  String sms = "";
		  int i = 0 ;
		  for(UIComponent child : children) {
			  if(child instanceof UIFormCheckBoxInput) {
				  if(((UIFormCheckBoxInput)child).isChecked()) {
					  topics.add(uiTopicContainer.getTopic(child.getName()));
					  if(topics.get(i).getIsSticky()){ sms = topics.get(i).getTopicName() ; break ;} 
            ++i ;
				  }
			  }
		  }
		  UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
		  if(topics.size() > 0 && sms.length() == 0) {
			  for(Topic topic : topics) {
				  topic.setIsSticky(true) ;
				  uiTopicContainer.forumService.saveTopic(uiTopicContainer.categoryId, uiTopicContainer.forumId, topic, false) ;
			  }
		  } 
		  if(topics.size() == 0 && sms.length() == 0){
			  Object[] args = { };
			  throw new MessageException(new ApplicationMessage("UITopicContainer.sms.notCheck", args, ApplicationMessage.WARNING)) ;
		  }
		  if(sms.length() > 0){
			  Object[] args = { sms };
			  throw new MessageException(new ApplicationMessage("UITopicContainer.sms.Stick", args, ApplicationMessage.WARNING)) ;
		  }
		  event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
	  }
  }  
  
  static public class SetMoveTopicActionListener extends EventListener<UITopicContainer> {
    public void execute(Event<UITopicContainer> event) throws Exception {
      UITopicContainer uiTopicContainer = event.getSource();
      List<UIComponent> children = uiTopicContainer.getChildren() ;
      List <Topic> topics = new ArrayList<Topic>();
      for(UIComponent child : children) {
        if(child instanceof UIFormCheckBoxInput) {
          if(((UIFormCheckBoxInput)child).isChecked()) {
            topics.add(uiTopicContainer.getTopic(child.getName()));
          }
        }
      }
      UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
      if(topics.size() > 0) {
        UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
        UIMoveTopicForm moveTopicForm = popupAction.createUIComponent(UIMoveTopicForm.class, null, null) ;
        moveTopicForm.updateTopic(uiTopicContainer.forumId, topics, false);
        popupAction.activate(moveTopicForm, 400, 420) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      } 
      if(topics.size() == 0){
        Object[] args = { };
        throw new MessageException(new ApplicationMessage("UITopicContainer.sms.notCheck", args, ApplicationMessage.WARNING)) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
    }
  }  

  static public class SetDeleteTopicActionListener extends EventListener<UITopicContainer> {
    public void execute(Event<UITopicContainer> event) throws Exception {
      UITopicContainer uiTopicContainer = event.getSource();
      List<UIComponent> children = uiTopicContainer.getChildren() ;
      List <Topic> topics = new ArrayList<Topic>();
      for(UIComponent child : children) {
        if(child instanceof UIFormCheckBoxInput) {
          if(((UIFormCheckBoxInput)child).isChecked()) {
            topics.add(uiTopicContainer.getTopic(child.getName()));
          }
        }
      }
      UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
      if(topics.size() > 0) {
        for(Topic topic : topics) {
          uiTopicContainer.forumService.removeTopic(uiTopicContainer.categoryId, uiTopicContainer.forumId, topic.getId()) ;
        }
      } 
      if(topics.size() == 0){
        Object[] args = { };
        throw new MessageException(new ApplicationMessage("UITopicContainer.sms.notCheck", args, ApplicationMessage.WARNING)) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
    }
  }  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
}
