/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.webui.EmptyNameValidator;
import org.exoplatform.forum.webui.UIBreadcumbs;
import org.exoplatform.forum.webui.UICategories;
import org.exoplatform.forum.webui.UICategory;
import org.exoplatform.forum.webui.UIForumLinks;
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
  
	public static final String FIELD_MODERATOR_INPUT = "Moderator" ;
	public static final String FIELD_VIEWER_INPUT = "Viewer" ;
	public static final String FIELD_POSTABLE_INPUT = "Postable" ;
	public static final String FIELD_TOPICABLE_INPUT = "Topicable" ;
  
  @SuppressWarnings("unchecked")
  public UIForumForm() throws Exception {
  	List<Category> categorys = forumService.getCategories(ForumUtils.getSystemProvider());
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
  	
  	UIFormStringInput moderator = new UIFormStringInput(FIELD_MODERATOR_INPUT, FIELD_MODERATOR_INPUT, null);
  	UIFormStringInput viewer = new UIFormStringInput(FIELD_VIEWER_INPUT, FIELD_VIEWER_INPUT, null);
  	UIFormStringInput postable = new UIFormStringInput(FIELD_POSTABLE_INPUT, FIELD_POSTABLE_INPUT, null);
  	UIFormStringInput topicable = new UIFormStringInput(FIELD_TOPICABLE_INPUT, FIELD_TOPICABLE_INPUT, null);
  	
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

    addUIFormInput(moderator) ;
    addUIFormInput(viewer) ;
    addUIFormInput(topicable) ;
    addUIFormInput(postable) ;
  }
  
  public void activate() throws Exception {
		// TODO Auto-generated method stub
	}
	public void deActivate() throws Exception {
		// TODO Auto-generated method stub
	}
	
  private String[] splitForForum (String str) throws Exception {
    if(str != null && str.length() > 0) {
      if(str.contains(",")) return str.trim().split(",") ;
      else return str.trim().split(";") ;
    } else return new String[] {} ;
  }
  
  private String unSplitForForum (String[] str) throws Exception {
    StringBuilder rtn = new StringBuilder();
    if(str.length > 0) {
      for (String temp : str) {
        rtn.append(temp).append(",") ; 
      }
    }
    return rtn.toString() ;
  }
  
	public void setForumValue(Forum forum, boolean isUpdate) throws Exception {
		if(isUpdate) {
			forumId = forum.getId();
			getUIStringInput(FIELD_FORUMTITLE_INPUT).setValue(forum.getForumName());
      getUIStringInput(FIELD_FORUMORDER_INPUT).setValue(String.valueOf(forum.getForumOrder()));
      String stat = "open";
      if(forum.getIsClosed()) stat = "closed";
      getUIFormSelectBox(FIELD_FORUMSTATE_SELECTBOX).setValue(stat);
      if(forum.getIsLock()) stat = "locked";
      else stat = "unlock";
      getUIFormSelectBox(FIELD_FORUMSTATUS_SELECTBOX).setValue(stat);
      getUIFormTextAreaInput(FIELD_DESCRIPTION_TEXTAREA).setDefaultValue(forum.getDescription());
      getUIFormTextAreaInput(FIELD_NOTIFYWHENADDPOST_MULTIVALUE).setDefaultValue(this.unSplitForForum(forum.getNotifyWhenAddPost()));
      getUIFormTextAreaInput(FIELD_NOTIFYWHENADDTOPIC_MULTIVALUE).setDefaultValue(this.unSplitForForum(forum.getNotifyWhenAddTopic()));
      
      getUIFormCheckBoxInput(FIELD_MODERATETHREAD_CHECKBOX).setChecked(forum.getIsModerateTopic());
      getUIFormCheckBoxInput(FIELD_MODERATEPOST_CHECKBOX).setChecked(forum.getIsModeratePost());
      
      getUIStringInput(FIELD_MODERATOR_INPUT).setValue(unSplitForForum(forum.getModerators()));
      getUIStringInput(FIELD_TOPICABLE_INPUT).setValue(unSplitForForum(forum.getCreateTopicRole()));
      getUIStringInput(FIELD_POSTABLE_INPUT).setValue(unSplitForForum(forum.getReplyTopicRole()));
      getUIStringInput(FIELD_VIEWER_INPUT).setValue(unSplitForForum(forum.getViewForumRole()));
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
      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
      
      UIFormSelectBox categorySelectBox = uiForm.getUIFormSelectBox(FIELD_CATEGORY_SELECTBOX);
      String categoryId = categorySelectBox.getValue();
      String forumTitle = uiForm.getUIStringInput(FIELD_FORUMTITLE_INPUT).getValue().trim();
      String forumOrder = uiForm.getUIStringInput(FIELD_FORUMORDER_INPUT).getValue();
      String forumState = uiForm.getUIFormSelectBox(FIELD_FORUMSTATE_SELECTBOX).getValue();
      String forumStatus = uiForm.getUIFormSelectBox(FIELD_FORUMSTATUS_SELECTBOX).getValue();
      String description = uiForm.getUIFormTextAreaInput(FIELD_DESCRIPTION_TEXTAREA).getValue().trim();
      String[] notifyWhenAddTopic = uiForm.splitForForum(uiForm.getUIFormTextAreaInput(FIELD_NOTIFYWHENADDTOPIC_MULTIVALUE).getValue()) ;
      String[] notifyWhenAddPost = uiForm.splitForForum(uiForm.getUIFormTextAreaInput(FIELD_NOTIFYWHENADDPOST_MULTIVALUE).getValue()) ;
      
      String[] setModerator = uiForm.splitForForum(uiForm.getUIStringInput(FIELD_MODERATOR_INPUT).getValue()) ;
      String[] setViewer = uiForm.splitForForum(uiForm.getUIStringInput(FIELD_VIEWER_INPUT).getValue()) ; 
      String[] setTopicable = uiForm.splitForForum(uiForm.getUIStringInput(FIELD_TOPICABLE_INPUT).getValue()) ; 
      String[] setPostable = uiForm.splitForForum(uiForm.getUIStringInput(FIELD_POSTABLE_INPUT).getValue()) ; 
      
      
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
      
  		newForum.setModerators(setModerator);
  		newForum.setCreateTopicRole(setPostable);
  		newForum.setViewForumRole(setViewer);
      newForum.setReplyTopicRole(setTopicable);
  		
  		ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
      if(uiForm.forumId.length() > 0)	{
      	newForum.setId(uiForm.forumId);
      	forumService.saveForum(ForumUtils.getSystemProvider(), categoryId, newForum, false);
      }
      else {
      	forumService.saveForum(ForumUtils.getSystemProvider(), categoryId, newForum, true);
      }
      forumPortlet.getChild(UIForumLinks.class).setUpdateForumLinks() ;
      forumPortlet.cancelAction() ;
      WebuiRequestContext context = event.getRequestContext() ;
      if(!uiForm.isForumUpdate) {
        if(uiForm.isCategoriesUpdate) {
          UICategories uiCategories = forumPortlet.findFirstComponentOfType(UICategories.class) ;
          context.addUIComponentToUpdateByAjax(uiCategories) ;
        }else {
        	UICategory uiCategory = forumPortlet.findFirstComponentOfType(UICategory.class) ;
        	context.addUIComponentToUpdateByAjax(uiCategory) ;
        }
      } else {
      	UIBreadcumbs breadcumbs = forumPortlet.getChild(UIBreadcumbs.class);
      	breadcumbs.setUpdataPath(categoryId + "/" + uiForm.forumId);
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
