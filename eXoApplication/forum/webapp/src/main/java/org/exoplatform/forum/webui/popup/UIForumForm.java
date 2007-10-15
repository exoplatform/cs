/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.webui.EmptyNameValidator;
import org.exoplatform.forum.webui.UICategories;
import org.exoplatform.forum.webui.UICategory;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.validator.PositiveNumberFormatValidator;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/forum/webui/popup/UIForumForm.gtmpl",
    events = {
      @EventConfig(listeners = UIForumForm.SaveActionListener.class), 
      @EventConfig(listeners = UIForumForm.CancelActionListener.class, phase=Phase.DECODE)
    }
)
public class UIForumForm extends UIForm implements UIPopupComponent {
	private ForumService forumService =  (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	private boolean isCategoriesUpdate = true;
  private boolean isForumUpdate = false;
	private String forumId = "";
	public static final String FIELD_CATEGORY_SELECTBOX = "Category" ;
	public static final String FIELD_FORUMTITLE_INPUT = "ForumTitle" ;
	public static final String FIELD_FORUMORDER_INPUT = "ForumOrder" ;
	public static final String FIELD_FORUMSTATUS_SELECTBOX = "ForumStatus" ;
	public static final String FIELD_FORUMSTATE_SELECTBOX = "ForumState" ;
	public static final String FIELD_DESCRIPTION_TEXTAREA = "Description" ;

	public static final String FIELD_NOTIFYWHENADDTOPIC_MULTIVALUE = "NotifyWhenAddTopic" ;
	public static final String FIELD_NOTIFYWHENADDPOST_MULTIVALUE = "NotifyWhenAddPost" ;
	public static final String FIELD_MODERATETHREAD_CHECKBOX = "ModerateThread" ;
	public static final String FIELD_MODERATEPOST_CHECKBOX = "ModeratePost" ;
  
  public UIForumForm() throws Exception {
  	List<Category> categorys = forumService.getCategories();
  	List<SelectItemOption<String>> list = new ArrayList<SelectItemOption<String>>() ;
  	for (Category category :categorys) {
  		list.add(new SelectItemOption<String>(category.getCategoryName(), category.getId())) ;
		}
  	UIFormSelectBox categoryId = new UIFormSelectBox(FIELD_CATEGORY_SELECTBOX, FIELD_CATEGORY_SELECTBOX, list) ;
  	categoryId.setDefaultValue(categorys.get(0).getId());
  	UIFormStringInput forumTitle = new UIFormStringInput(FIELD_FORUMTITLE_INPUT, FIELD_FORUMTITLE_INPUT, null);
  	forumTitle.addValidator(EmptyNameValidator.class) ;
    UIFormStringInput forumOrder = new UIFormStringInput(FIELD_FORUMORDER_INPUT, FIELD_FORUMORDER_INPUT, "0");
  	forumOrder.addValidator(PositiveNumberFormatValidator.class) ;
  	List<SelectItemOption<String>> ls = new ArrayList<SelectItemOption<String>>() ;
    ls.add(new SelectItemOption<String>("Open", "open")) ;
    ls.add(new SelectItemOption<String>("Closed", "closed")) ;
    UIFormSelectBox forumState = new UIFormSelectBox(FIELD_FORUMSTATE_SELECTBOX, FIELD_FORUMSTATE_SELECTBOX, ls) ;
    forumState.setDefaultValue("open");
    List<SelectItemOption<String>> ls1 = new ArrayList<SelectItemOption<String>>() ;
    ls1.add(new SelectItemOption<String>("UnLock", "unlock")) ;
    ls1.add(new SelectItemOption<String>("Locked", "locked")) ;
    UIFormSelectBox forumStatus = new UIFormSelectBox(FIELD_FORUMSTATUS_SELECTBOX, FIELD_FORUMSTATUS_SELECTBOX, ls1) ;
    forumStatus.setDefaultValue("unlock");
    
  	UIFormStringInput description = new UIFormTextAreaInput(FIELD_DESCRIPTION_TEXTAREA, FIELD_DESCRIPTION_TEXTAREA, null);
    description.addValidator(EmptyNameValidator.class) ;
  	UIFormTextAreaInput notifyWhenAddPost = new UIFormTextAreaInput(FIELD_NOTIFYWHENADDPOST_MULTIVALUE, FIELD_NOTIFYWHENADDPOST_MULTIVALUE, null);
  	UIFormTextAreaInput notifyWhenAddTopic = new UIFormTextAreaInput(FIELD_NOTIFYWHENADDTOPIC_MULTIVALUE, FIELD_NOTIFYWHENADDTOPIC_MULTIVALUE, null);
  	
  	
  	UIFormCheckBoxInput checkWhenAddTopic = new UIFormCheckBoxInput<Boolean>(FIELD_MODERATETHREAD_CHECKBOX, FIELD_MODERATETHREAD_CHECKBOX, false);
  	UIFormCheckBoxInput checkWhenAddPost = new UIFormCheckBoxInput<Boolean>(FIELD_MODERATEPOST_CHECKBOX, FIELD_MODERATEPOST_CHECKBOX, false);
  	
  	addUIFormInput(categoryId) ;
  	addUIFormInput(forumTitle) ;
  	addUIFormInput(forumOrder) ;
  	addUIFormInput(forumState) ;
  	addUIFormInput(forumStatus) ;
  	addUIFormInput(description) ;

  	addUIFormInput(notifyWhenAddPost);
  	addUIFormInput(notifyWhenAddTopic);
  	addUIFormInput(checkWhenAddTopic);
  	addUIFormInput(checkWhenAddPost);
  	 
//  	UIForumForm uicomponent = this;
//  	uicomponent.getChild(0).getName()
  }
  
  public void activate() throws Exception {
		// TODO Auto-generated method stub
		
	}
	public void deActivate() throws Exception {
		// TODO Auto-generated method stub
	}
	
	public void setForumValue(Forum forum, boolean isUpdate) throws Exception {
		if(isUpdate) {
			forumId = forum.getId();
			getUIStringInput(FIELD_FORUMTITLE_INPUT).setValue(forum.getForumName());
      getUIStringInput(FIELD_FORUMORDER_INPUT).setValue(String.valueOf(forum.getForumOrder()));
      String stat = "open";
      if(forum.getIsClosed()) stat = "closed";
      System.out.println("\n\n   " + stat + "\n\n");
      getUIFormSelectBox(FIELD_FORUMSTATE_SELECTBOX).setDefaultValue(stat);
      if(forum.getIsLock()) stat = "locked";
      else stat = "unlock";
      getUIFormSelectBox(FIELD_FORUMSTATUS_SELECTBOX).setDefaultValue(stat);
      getUIFormTextAreaInput(FIELD_DESCRIPTION_TEXTAREA).setDefaultValue(forum.getDescription());
      String temp = "";
      for (String Str : forum.getNotifyWhenAddPost()) {
				temp = temp + Str + ", ";
			}
      getUIFormTextAreaInput(FIELD_NOTIFYWHENADDPOST_MULTIVALUE).setDefaultValue(temp);
      temp = "";
      for (String Str : forum.getNotifyWhenAddTopic()) {
      	temp = temp + Str + ", ";
      }
      getUIFormTextAreaInput(FIELD_NOTIFYWHENADDTOPIC_MULTIVALUE).setDefaultValue(temp);
      getUIFormCheckBoxInput(FIELD_MODERATETHREAD_CHECKBOX).setChecked(forum.getIsModerateTopic());
      getUIFormCheckBoxInput(FIELD_MODERATEPOST_CHECKBOX).setChecked(forum.getIsModeratePost());
		}
	}
  
	public void setCategoryValue(String categoryId, boolean isEditable) throws Exception {
		if(!isEditable) getUIFormSelectBox(FIELD_CATEGORY_SELECTBOX).setValue(categoryId) ;
		getUIFormSelectBox(FIELD_CATEGORY_SELECTBOX).setEnable(isEditable) ;
		isCategoriesUpdate = isEditable;
	}
	
  public void setForumUpdate(boolean isForumUpdate) {
    this.isForumUpdate = isForumUpdate ;
  }
  
  static  public class SaveActionListener extends EventListener<UIForumForm> {
    public void execute(Event<UIForumForm> event) throws Exception {
      UIForumForm uiForm = event.getSource() ;
      UIFormSelectBox categorySelectBox = uiForm.getUIFormSelectBox(FIELD_CATEGORY_SELECTBOX);
      String categoryId = categorySelectBox.getValue();
      String forumTitle = uiForm.getUIStringInput(FIELD_FORUMTITLE_INPUT).getValue().trim();
      String forumOrder = uiForm.getUIStringInput(FIELD_FORUMORDER_INPUT).getValue();
      String forumState = uiForm.getUIFormSelectBox(FIELD_FORUMSTATE_SELECTBOX).getValue();
      String forumStatus = uiForm.getUIFormSelectBox(FIELD_FORUMSTATUS_SELECTBOX).getValue();
      String description = uiForm.getUIFormTextAreaInput(FIELD_DESCRIPTION_TEXTAREA).getValue().trim();
      String whenNewPost = uiForm.getUIFormTextAreaInput(FIELD_NOTIFYWHENADDPOST_MULTIVALUE).getValue();
      String whenNewTopic = uiForm.getUIFormTextAreaInput(FIELD_NOTIFYWHENADDTOPIC_MULTIVALUE).getValue();
      String[] notifyWhenAddTopic;
      String[] notifyWhenAddPost;
      if(whenNewTopic != null && whenNewTopic.length() > 0) notifyWhenAddTopic = whenNewTopic.trim().split(",") ;
      else notifyWhenAddTopic = new String[] {};
      if(whenNewPost != null && whenNewPost.length() > 0) notifyWhenAddPost =  whenNewPost.trim().split(",");
      else notifyWhenAddPost = new String[] {};
      
      Boolean  ModerateTopic = (Boolean) uiForm.getUIFormCheckBoxInput(FIELD_MODERATETHREAD_CHECKBOX).getValue();
      Boolean  ModeratePost = (Boolean) uiForm.getUIFormCheckBoxInput(FIELD_MODERATEPOST_CHECKBOX).getValue();
      
      String userName = Util.getPortalRequestContext().getRemoteUser() ;
  		Forum newForum = new Forum();
  		
  		newForum.setForumName(forumTitle);
  		newForum.setOwner(userName);
  		newForum.setForumOrder(Integer.valueOf(forumOrder).intValue());
      newForum.setCreatedDate(new Date());
      newForum.setDescription(description);
      newForum.setLastTopicPath("");
      newForum.setPath("");
      newForum.setModifiedBy(userName);
      newForum.setModifiedDate(new Date());
      newForum.setPostCount(0);
      newForum.setTopicCount(0);
  		
      newForum.setNotifyWhenAddPost(notifyWhenAddPost);
      newForum.setNotifyWhenAddTopic(notifyWhenAddTopic);
      newForum.setIsModeratePost(ModeratePost);
      newForum.setIsModerateTopic(ModerateTopic);
      if(forumState.equals("closed")) {
      	newForum.setIsClosed(true);
      }
      if(forumStatus.equals("locked")) {
      	newForum.setIsLock(true) ;
      }
      newForum.setViewForumRole(new String[] {"member:/admin"});
      newForum.setCreateTopicRole(new String[] {"member:/admin"});
      newForum.setReplyTopicRole(new String[] {});
  		newForum.setModerators(new String[] {"member:/admin"});
  		
  		ForumService forumService =  (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
      if(uiForm.forumId.length() > 0)	{
      	newForum.setId(uiForm.forumId);
      	forumService.saveForum(categoryId, newForum, false);
      }
      else {
      	forumService.saveForum(categoryId, newForum, true);
      }
      
      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
      forumPortlet.cancelAction() ;
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
      if(!uiForm.isForumUpdate) {
        if(uiForm.isCategoriesUpdate) {
          UICategories uiCategories = forumPortlet.findFirstComponentOfType(UICategories.class) ;
          context.addUIComponentToUpdateByAjax(uiCategories) ;
        }else {
        	UICategory uiCategory = forumPortlet.findFirstComponentOfType(UICategory.class) ;
        	context.addUIComponentToUpdateByAjax(uiCategory) ;
        }
      } else {
        context.addUIComponentToUpdateByAjax(forumPortlet) ;
      }
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIForumForm> {
    public void execute(Event<UIForumForm> event) throws Exception {
      UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
      forumPortlet.cancelAction() ;
    }
  }
}
