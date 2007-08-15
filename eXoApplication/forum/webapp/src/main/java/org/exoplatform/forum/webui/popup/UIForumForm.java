/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.poi.hssf.record.formula.functions.Int;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.webui.UICategories;
import org.exoplatform.forum.webui.UICategory;
import org.exoplatform.forum.webui.UICategoryContainer;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.portal.application.PortalRequestContext;
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
import org.exoplatform.webui.form.UIFormMultiValueInputSet;
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
    template = "app:/templates/forum/webui/popup/UIAddNewForum.gtmpl",
    events = {
      @EventConfig(listeners = UIForumForm.SaveActionListener.class), 
      @EventConfig(listeners = UIForumForm.CancelActionListener.class, phase=Phase.DECODE)
    }
)
public class UIForumForm extends UIForm implements UIPopupComponent{
	ForumService forumService =  (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	private boolean isCategoriesUpdate = true;
	public static final String FIELD_CATEGORY_SELECTBOX = "Category" ;
	public static final String FIELD_FORUMTITLE_INPUT = "ForumTitle" ;
	public static final String FIELD_FORUMORDER_INPUT = "ForumOrder" ;
	public static final String FIELD_FORUMSTATUS_SELECTBOX = "ForumStatus" ;
	public static final String FIELD_TEXT_AREA = "Description" ;

	public static final String FIELD_NOTIFYWHENADDTOPIC_MULTIVALUE = "NotifyWhenAddTopic" ;
	public static final String FIELD_NOTIFYWHENADDPOST_MULTIVALUE = "NotifyWhenAddPost" ;
	public static final String FIELD_MODERATETHREAD_CHECKBOX = "ModerateThread" ;
	public static final String FIELD_MODERATEPOST_CHECKBOX = "ModeratePost" ;
  
  public UIForumForm() throws Exception {
  	List<Category> category = forumService.getCategories();
  	List<SelectItemOption<String>> cate = new ArrayList<SelectItemOption<String>>() ;
  	for (int i = 0; i < category.size(); i++) {
  		cate.add(new SelectItemOption<String>(category.get(i).getCategoryName(), category.get(i).getId())) ;
		}
  	UIFormSelectBox categoryId = new UIFormSelectBox(FIELD_CATEGORY_SELECTBOX, FIELD_CATEGORY_SELECTBOX, cate) ;
  	categoryId.setDefaultValue(category.get(0).getId());
  	UIFormStringInput forumTitle = new UIFormStringInput(FIELD_FORUMTITLE_INPUT, FIELD_FORUMTITLE_INPUT, null);
  	UIFormStringInput forumOrder = new UIFormStringInput(FIELD_FORUMORDER_INPUT, FIELD_FORUMORDER_INPUT, null);
  	forumOrder.addValidator(PositiveNumberFormatValidator.class) ;
  	List<SelectItemOption<String>> ls = new ArrayList<SelectItemOption<String>>() ;
    ls.add(new SelectItemOption<String>("Open", "open")) ;
    ls.add(new SelectItemOption<String>("Locked", "locked")) ;
    ls.add(new SelectItemOption<String>("Closed", "closed")) ;
    UIFormSelectBox forumStatus = new UIFormSelectBox(FIELD_FORUMSTATUS_SELECTBOX, FIELD_FORUMSTATUS_SELECTBOX, ls) ;
    forumStatus.setDefaultValue("open");
  	UIFormStringInput description = new UIFormTextAreaInput(FIELD_TEXT_AREA, FIELD_TEXT_AREA, null);
  	
  	UIFormTextAreaInput notifyWhenAddTopic = new UIFormTextAreaInput(FIELD_NOTIFYWHENADDTOPIC_MULTIVALUE, FIELD_NOTIFYWHENADDTOPIC_MULTIVALUE, null);
  	UIFormTextAreaInput notifyWhenAddPost = new UIFormTextAreaInput(FIELD_NOTIFYWHENADDPOST_MULTIVALUE, FIELD_NOTIFYWHENADDPOST_MULTIVALUE, null);
  	
  	
  	UIFormCheckBoxInput checkWhenAddTopic = new UIFormCheckBoxInput<Boolean>(FIELD_MODERATETHREAD_CHECKBOX, FIELD_MODERATETHREAD_CHECKBOX, false);
  	UIFormCheckBoxInput checkWhenAddPost = new UIFormCheckBoxInput<Boolean>(FIELD_MODERATEPOST_CHECKBOX, FIELD_MODERATEPOST_CHECKBOX, false);
  	
  	addUIFormInput(categoryId) ;
  	addUIFormInput(forumTitle) ;
  	addUIFormInput(forumOrder) ;
  	addUIFormInput(forumStatus) ;
  	addUIFormInput(description) ;

  	addUIFormInput(notifyWhenAddTopic);
  	addUIFormInput(notifyWhenAddPost);
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
  
	public void setCategoryValue(String categoryId, boolean isEditable) throws Exception {
		if(!isEditable) getUIFormSelectBox(FIELD_CATEGORY_SELECTBOX).setValue(categoryId) ;
		getUIFormSelectBox(FIELD_CATEGORY_SELECTBOX).setEnable(isEditable) ;
		isCategoriesUpdate = isEditable;
	}
	
  static  public class SaveActionListener extends EventListener<UIForumForm> {
    public void execute(Event<UIForumForm> event) throws Exception {
      UIForumForm uiForm = event.getSource() ;
      UIFormSelectBox categorySelectBox = uiForm.getUIFormSelectBox(FIELD_CATEGORY_SELECTBOX);
      String categoryId = categorySelectBox.getValue();
      String forumTitle = uiForm.getUIStringInput(FIELD_FORUMTITLE_INPUT).getValue();
      String forumOrder = uiForm.getUIStringInput(FIELD_FORUMORDER_INPUT).getValue();
      String forumStatus = uiForm.getUIFormSelectBox(FIELD_FORUMSTATUS_SELECTBOX).getValue();
      String description = uiForm.getUIFormTextAreaInput(FIELD_TEXT_AREA).getValue();
      String whenNewTopic = uiForm.getUIFormTextAreaInput(FIELD_NOTIFYWHENADDTOPIC_MULTIVALUE).getValue();
      String whenNewPost = uiForm.getUIFormTextAreaInput(FIELD_NOTIFYWHENADDPOST_MULTIVALUE).getValue();
      String[] notifyWhenAddTopic;
      String[] notifyWhenAddPost;
      if(whenNewTopic != null && whenNewTopic.length() > 0) notifyWhenAddTopic = whenNewTopic.split(",") ;
      else notifyWhenAddTopic = new String[] {};
      
      if(whenNewPost != null && whenNewPost.length() > 0) notifyWhenAddPost =  whenNewPost.split(",");
      else notifyWhenAddPost = new String[] {};
      
      Boolean  ModerateTopic = (Boolean) uiForm.getUIFormCheckBoxInput(FIELD_MODERATETHREAD_CHECKBOX).getValue();
      Boolean  ModeratePost = (Boolean) uiForm.getUIFormCheckBoxInput(FIELD_MODERATEPOST_CHECKBOX).getValue();
      
      GregorianCalendar calendar = new GregorianCalendar() ;
      PortalRequestContext pContext = Util.getPortalRequestContext();
      String userName = pContext.getRemoteUser() ;
  		String id = String.valueOf(calendar.getTimeInMillis());
  		
  		Forum newForum = new Forum();
  		newForum.setId(id);
  		newForum.setForumName(forumTitle);
  		newForum.setOwner(userName);
  		newForum.setForumOrder(Integer.valueOf(forumOrder).intValue());
      newForum.setCreatedDate(new Date());
      newForum.setDescription(description);
      newForum.setLastPostPath("");
      newForum.setPath("");
      newForum.setModifiedBy(userName);
      newForum.setModifiedDate(new Date());
      newForum.setPostCount(0);
      newForum.setTopicCount(0);
  		
      newForum.setNotifyWhenAddTopic(notifyWhenAddTopic);
      newForum.setNotifyWhenAddPost(notifyWhenAddPost);
      newForum.setIsModeratePost(ModeratePost);
      newForum.setIsModerateTopic(ModerateTopic);
      if(forumStatus == "locked") {
      	newForum.setIsClosed(true);
      }
      if(forumStatus == "closed") {
      	newForum.setIsLock(true) ;
      }
      
      
  	  
      newForum.setViewForumRole(new String[] {"member:/admin"});
      newForum.setCreateTopicRole(new String[] {"member:/admin"});
      newForum.setReplyTopicRole(new String[] {});
  		newForum.setModerators(new String[] {"member:/admin"});
  		
  		ForumService forumService =  (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
      forumService.saveForum(categoryId, newForum, true);
      
      UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
      forumPortlet.cancelAction() ;
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
      if(uiForm.isCategoriesUpdate) {
        UICategories uiCategories = forumPortlet.getChild(UICategoryContainer.class).getChild(UICategories.class) ;
        context.addUIComponentToUpdateByAjax(uiCategories) ;
      }else {
      	UICategory uiCategory = forumPortlet.getChild(UICategoryContainer.class).getChild(UICategory.class) ;
      	context.addUIComponentToUpdateByAjax(uiCategory) ;
      }
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIForumForm> {
    public void execute(Event<UIForumForm> event) throws Exception {
      UIForumForm uiForm = event.getSource() ;
      UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
      forumPortlet.cancelAction() ;
    }
  }
}
