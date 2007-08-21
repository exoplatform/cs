/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.webui;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.webui.popup.UIForumForm;
import org.exoplatform.forum.webui.popup.UIMoveForumForm;
import org.exoplatform.forum.webui.popup.UIPopupAction;
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
		lifecycle = UIFormLifecycle.class ,
    template =  "app:/templates/forum/webui/UICategory.gtmpl",
    events = {
        @EventConfig(listeners = UICategory.AddForumActionListener.class),
        @EventConfig(listeners = UICategory.EditForumActionListener.class),
        @EventConfig(listeners = UICategory.SetLockedActionListener.class),
        @EventConfig(listeners = UICategory.SetUnLockActionListener.class),
        @EventConfig(listeners = UICategory.SetOpenActionListener.class),
        @EventConfig(listeners = UICategory.SetCloseActionListener.class),
        @EventConfig(listeners = UICategory.MoveForumActionListener.class),
        @EventConfig(listeners = UICategory.RemoveForumActionListener.class),
        @EventConfig(listeners = UICategory.OpenForumLink.class)
    }
)
public class UICategory extends UIForm  {
	private String categoryId ;
	private	ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
  
	public UICategory() throws Exception {

  }
  
  public void update(String id) throws Exception {
  	categoryId = id ;
  }
  
  private Category getCategory() throws Exception{
		return forumService.getCategory(categoryId);
	}
  
	private List<Forum> getForumList(String categoryId) throws Exception {
		List<Forum> forumList = forumService.getForums(categoryId);
		for(Forum forum : forumList) {
			if(getUIFormCheckBoxInput(forum.getId()) != null) {
				getUIFormCheckBoxInput(forum.getId()).setChecked(false) ;
			}else {
				addUIFormInput(new UIFormCheckBoxInput(forum.getId(), forum.getId(), false) );
			}
			
		}
		return forumList;
	}
	
	private Topic getLastTopic(String topicPath) throws Exception {
		return forumService.getTopicByPath(topicPath) ;
	}
	
  
  static public class AddForumActionListener extends EventListener<UICategory> {
    public void execute(Event<UICategory> event) throws Exception {
      UICategory uiCategory = event.getSource() ;      
      UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
      UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
      UIForumForm forumForm = popupAction.createUIComponent(UIForumForm.class, null, null) ;
      forumForm.setCategoryValue(uiCategory.categoryId, false) ;
      popupAction.activate(forumForm, 662, 466) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  static public class EditForumActionListener extends EventListener<UICategory> {
  	public void execute(Event<UICategory> event) throws Exception {
  		UICategory uiCategory = event.getSource() ;      
  		List<UIComponent> children = uiCategory.getChildren() ;
  		Forum forum = null ;
  		for(UIComponent child : children) {
  			if(child instanceof UIFormCheckBoxInput) {
  				if(((UIFormCheckBoxInput)child).isChecked()) {
  					forum = uiCategory.forumService.getForum(uiCategory.categoryId, ((UIFormCheckBoxInput)child).getName());
  					break ;
  				}
  			}
  		}
  		if(forum != null) {
  			UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
    		UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
    		UIForumForm forumForm = popupAction.createUIComponent(UIForumForm.class, null, null) ;
    		forumForm.setCategoryValue(uiCategory.categoryId, false) ;
  			forumForm.setForumValue(forum, true);
    		popupAction.activate(forumForm, 662, 466) ;
    		event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
  		} else {
  			Object[] args = {  };
        throw new MessageException(new ApplicationMessage("UICategory.msg.notCheck", args, ApplicationMessage.WARNING)) ;
  		}
  	}
  }

  static public class SetLockedActionListener extends EventListener<UICategory> {
  	public void execute(Event<UICategory> event) throws Exception {
  		UICategory uiCategory = event.getSource() ;
  		List<UIComponent> children = uiCategory.getChildren() ;
  		List<Forum> forums = new ArrayList<Forum>() ;
  		int i = 0 ;
  		String sms = "";
  		for(UIComponent child : children) {
  			if(child instanceof UIFormCheckBoxInput) {
  				if(((UIFormCheckBoxInput)child).isChecked()) {
  					forums.add(uiCategory.forumService.getForum(uiCategory.categoryId, ((UIFormCheckBoxInput)child).getName()));
  					if(forums.get(i).getIsLock()){sms = forums.get(i).getForumName(); break;}
  					i++;
  				}
  			}
  		}
  		if((forums.size() > 0) && (sms.length() == 0)) {
  			for (Forum forum : forums) {
  				forum.setIsLock(true) ;
  				uiCategory.forumService.saveForum(uiCategory.categoryId, forum, false);
				}
  			WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
  			context.addUIComponentToUpdateByAjax(uiCategory) ;
  		}  
  		if((forums.size() == 0) && (sms.length() == 0)) {
				Object[] args = { };
				throw new MessageException(new ApplicationMessage("UICategory.msg.notCheck", args, ApplicationMessage.WARNING)) ;
			}	
			if(sms.length() > 0) {
				Object[] args = { sms };
				throw new MessageException(new ApplicationMessage("UICategory.msg.locked", args, ApplicationMessage.WARNING)) ;
			}
  	}
  }
  
  static public class SetUnLockActionListener extends EventListener<UICategory> {
  	public void execute(Event<UICategory> event) throws Exception {
  		UICategory uiCategory = event.getSource() ;
  		List<UIComponent> children = uiCategory.getChildren() ;
  		List<Forum> forums = new ArrayList<Forum>() ;
  		int i = 0 ;
  		String sms = "";
  		for(UIComponent child : children) {
  			if(child instanceof UIFormCheckBoxInput) {
  				if(((UIFormCheckBoxInput)child).isChecked()) {
  					forums.add(uiCategory.forumService.getForum(uiCategory.categoryId, ((UIFormCheckBoxInput)child).getName()));
  					if(!forums.get(i).getIsLock()){sms = forums.get(i).getForumName(); break;}
  					i++;
  				}
  			}
  		}
  		if((forums.size() > 0) && (sms.length() == 0)) {
  			for (Forum forum : forums) {
  				forum.setIsLock(false) ;
  				uiCategory.forumService.saveForum(uiCategory.categoryId, forum, false);
  			}
  		} 
  		if((forums.size() == 0) && (sms.length() == 0)) {
				Object[] args = { };
				throw new MessageException(new ApplicationMessage("UICategory.msg.notCheck", args, ApplicationMessage.WARNING)) ;
			}	
			if(sms.length() > 0) {
				Object[] args = { sms };
				throw new MessageException(new ApplicationMessage("UICategory.msg.unlock", args, ApplicationMessage.WARNING)) ;
			}
  	}
  }
  
  static public class SetOpenActionListener extends EventListener<UICategory> {
  	public void execute(Event<UICategory> event) throws Exception {
  		UICategory uiCategory = event.getSource() ;
  		List<UIComponent> children = uiCategory.getChildren() ;
  		List<Forum> forums = new ArrayList<Forum>() ;
  		int i = 0 ;
  		String sms = "";
  		for(UIComponent child : children) {
  			if(child instanceof UIFormCheckBoxInput) {
  				if(((UIFormCheckBoxInput)child).isChecked()) {
  					forums.add(uiCategory.forumService.getForum(uiCategory.categoryId, ((UIFormCheckBoxInput)child).getName()));
  					if(!forums.get(i).getIsClosed()){sms = forums.get(i).getForumName(); break;}
  					i++;
  				}
  			}
  		}
  		if((forums.size() > 0) && (sms.length() == 0)) {
  			for (Forum forum : forums) {
  				forum.setIsClosed(false) ;
  				uiCategory.forumService.saveForum(uiCategory.categoryId, forum, false);
  			}
  		} 
  		if((forums.size() == 0) && (sms.length() == 0)) {
				Object[] args = { };
				throw new MessageException(new ApplicationMessage("UICategory.msg.notCheck", args, ApplicationMessage.WARNING)) ;
			}	
			if(sms.length() > 0) {
				Object[] args = { sms };
				throw new MessageException(new ApplicationMessage("UICategory.msg.open", args, ApplicationMessage.WARNING)) ;
			}
  	}
  }

  static public class SetCloseActionListener extends EventListener<UICategory> {
  	public void execute(Event<UICategory> event) throws Exception {
  		UICategory uiCategory = event.getSource() ;
  		List<UIComponent> children = uiCategory.getChildren() ;
  		List<Forum> forums = new ArrayList<Forum>() ;
  		int i = 0 ;
  		String sms = "";
  		for(UIComponent child : children) {
  			if(child instanceof UIFormCheckBoxInput) {
  				if(((UIFormCheckBoxInput)child).isChecked()) {
  					forums.add(uiCategory.forumService.getForum(uiCategory.categoryId, ((UIFormCheckBoxInput)child).getName()));
  					if(forums.get(i).getIsClosed()){sms = forums.get(i).getForumName(); break;}
  					i++;
  				}
  			}
  		}
  		if((forums.size() > 0) && (sms.length() == 0)) {
  			for (Forum forum : forums) {
  				forum.setIsClosed(true) ;
  				uiCategory.forumService.saveForum(uiCategory.categoryId, forum, false);
  			}
  		} 
  		if((forums.size() == 0) && (sms.length() == 0)) {
				Object[] args = { };
				throw new MessageException(new ApplicationMessage("UICategory.msg.notCheck", args, ApplicationMessage.WARNING)) ;
			}	
			if(sms.length() > 0) {
				Object[] args = { sms };
				throw new MessageException(new ApplicationMessage("UICategory.msg.close", args, ApplicationMessage.WARNING)) ;
			}
  	}
  }

  static public class MoveForumActionListener extends EventListener<UICategory> {
  	public void execute(Event<UICategory> event) throws Exception {
  		UICategory uiCategory = event.getSource() ;
  		List<UIComponent> children = uiCategory.getChildren() ;
  		List<Forum> forums = new ArrayList<Forum>() ;
  		for(UIComponent child : children) {
  			if(child instanceof UIFormCheckBoxInput) {
  				if(((UIFormCheckBoxInput)child).isChecked()) {
  					forums.add(uiCategory.forumService.getForum(uiCategory.categoryId, ((UIFormCheckBoxInput)child).getName()));
  				}
  			}
  		}
  		if((forums.size() > 0)) {
  			UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
    		UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
    		UIMoveForumForm moveForumForm = popupAction.createUIComponent(UIMoveForumForm.class, null, null) ;
    		moveForumForm.setListForum(forums, uiCategory.categoryId);
    		popupAction.activate(moveForumForm, 400, 165) ;
    		event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
  		} else {
  			Object[] args = { };
  			throw new MessageException(new ApplicationMessage("UICategory.msg.notCheck", args, ApplicationMessage.WARNING)) ;
  		}	
  	}
  }
  
  static public class RemoveForumActionListener extends EventListener<UICategory> {
  	public void execute(Event<UICategory> event) throws Exception {
  		UICategory uiCategory = event.getSource() ;
  		List<UIComponent> children = uiCategory.getChildren() ;
  		List<Forum> forums = new ArrayList<Forum>() ;
  		for(UIComponent child : children) {
  			if(child instanceof UIFormCheckBoxInput) {
  				if(((UIFormCheckBoxInput)child).isChecked()) {
  					forums.add(uiCategory.forumService.getForum(uiCategory.categoryId, ((UIFormCheckBoxInput)child).getName()));
  				}
  			}
  		}
  		if((forums.size() > 0)) {
  			for (Forum forum : forums) {
  				uiCategory.forumService.removeForum(uiCategory.categoryId, forum.getId()) ;
  			}
  		} else {
  			Object[] args = { };
  			throw new MessageException(new ApplicationMessage("UICategory.msg.notCheck", args, ApplicationMessage.WARNING)) ;
  		}	
  	}
  }
  
  static public class OpenForumLink extends EventListener<UICategory> {
    public void execute(Event<UICategory> event) throws Exception {
      UICategory uiCategory = event.getSource();
      String forumId = event.getRequestContext().getRequestParameter(OBJECTID)  ;
      System.out.println("\n\n--------------->  id:  " + forumId);
      UIForumPortlet forumPortlet = uiCategory.getAncestorOfType(UIForumPortlet.class) ;
      forumPortlet.updateIsRendered(2);
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
      context.addUIComponentToUpdateByAjax(forumPortlet) ;
    }
  }
  
}
