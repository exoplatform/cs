/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS       All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.service.BufferAttachment;
import org.exoplatform.forum.service.ForumAttachment;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.webui.EmptyNameValidator;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputIconSelector;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormWYSIWYGInput;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;

/**
 * Created by The eXo Platform SARL
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Aug 22, 2007  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/forum/webui/popup/UITopicForm.gtmpl",
    events = {
      @EventConfig(listeners = UITopicForm.PreviewThreadActionListener.class, phase = Phase.DECODE), 
      @EventConfig(listeners = UITopicForm.SubmitThreadActionListener.class), 
      @EventConfig(listeners = UITopicForm.AttachmentActionListener.class), 
      @EventConfig(listeners = UITopicForm.RemoveAttachmentActionListener.class), 
      @EventConfig(listeners = UITopicForm.CancelActionListener.class,phase = Phase.DECODE)
    }
)
public class UITopicForm extends UIForm implements UIPopupComponent {
  public static final String FIELD_TOPICTITLE_INPUT = "ThreadTitle" ;
  public static final String FIELD_MESSENGER_TEXTAREA = "Messenger" ;
  final static public String FIELD_MESSAGECONTENT = "messageContent" ;
  public static final String FIELD_TOPICSTATUS_SELECTBOX = "TopicStatus" ;
  public static final String FIELD_TOPICSTATE_SELECTBOX = "TopicState" ;
  
  public static final String FIELD_APPROVED_CHECKBOX = "Approved" ;
  public static final String FIELD_MODERATEPOST_CHECKBOX = "ModeratePost" ;
  public static final String FIELD_NOTIFYWHENADDPOST_CHECKBOX = "NotifyWhenAddPost" ;
  public static final String FIELD_STICKY_CHECKBOX = "Sticky" ;
  
  public static final String FIELD_CANVIEW_INPUT = "CanView" ;
  public static final String FIELD_CANPOST_INPUT = "CanPost" ;
  final static public String ACT_REMOVE = "remove" ;
  final static public String FIELD_ATTACHMENTS = "attachments" ;
  final static public String FIELD_FROM_INPUT = "fromInput" ;
  
  private List<ForumAttachment> attachments_ = new ArrayList<ForumAttachment>() ;
  private String categoryId; 
  private String forumId ;
  private String topicId ;
  public UITopicForm() throws Exception {
    UIFormStringInput topicTitle = new UIFormStringInput(FIELD_TOPICTITLE_INPUT, FIELD_TOPICTITLE_INPUT, null);
    topicTitle.addValidator(EmptyNameValidator.class) ;
//    UIFormTextAreaInput messenger = new UIFormTextAreaInput(FIELD_MESSENGER_TEXTAREA, FIELD_MESSENGER_TEXTAREA, null);
    
    List<SelectItemOption<String>> ls = new ArrayList<SelectItemOption<String>>() ;
    ls.add(new SelectItemOption<String>("Open", "open")) ;
    ls.add(new SelectItemOption<String>("Closed", "closed")) ;
    UIFormSelectBox topicState = new UIFormSelectBox(FIELD_TOPICSTATE_SELECTBOX, FIELD_TOPICSTATE_SELECTBOX, ls) ;
    topicState.setDefaultValue("open");
    List<SelectItemOption<String>> ls1 = new ArrayList<SelectItemOption<String>>() ;
    ls1.add(new SelectItemOption<String>("UnLock", "unlock")) ;
    ls1.add(new SelectItemOption<String>("Locked", "locked")) ;
    UIFormSelectBox topicStatus = new UIFormSelectBox(FIELD_TOPICSTATUS_SELECTBOX, FIELD_TOPICSTATUS_SELECTBOX, ls1) ;
    topicStatus.setDefaultValue("unlock");
    
    UIFormCheckBoxInput approved = new UIFormCheckBoxInput<Boolean>(FIELD_APPROVED_CHECKBOX, FIELD_APPROVED_CHECKBOX, false);
    UIFormCheckBoxInput moderatePost = new UIFormCheckBoxInput<Boolean>(FIELD_MODERATEPOST_CHECKBOX, FIELD_MODERATEPOST_CHECKBOX, false);
    UIFormCheckBoxInput checkWhenAddPost = new UIFormCheckBoxInput<Boolean>(FIELD_NOTIFYWHENADDPOST_CHECKBOX, FIELD_NOTIFYWHENADDPOST_CHECKBOX, false);
    UIFormCheckBoxInput sticky = new UIFormCheckBoxInput<Boolean>(FIELD_STICKY_CHECKBOX, FIELD_STICKY_CHECKBOX, false);
    
    UIFormStringInput canView = new UIFormStringInput(FIELD_CANVIEW_INPUT, FIELD_CANVIEW_INPUT, null);
    UIFormStringInput canPost = new UIFormStringInput(FIELD_CANPOST_INPUT, FIELD_CANPOST_INPUT, null);
    
    addUIFormInput(topicTitle);
   // addUIFormInput(messenger);
    
    addUIFormInput(topicState);
    addUIFormInput(topicStatus);
    addUIFormInput(approved);
    addUIFormInput(moderatePost);
    addUIFormInput(checkWhenAddPost);
    addUIFormInput(sticky);
    
    addUIFormInput(canView);
    addUIFormInput(canPost);
    UIFormInputIconSelector uiIconSelector = new UIFormInputIconSelector("Icon", "Icon") ;
    //uiIconSelector.setRendered(false)  ;
    addUIFormInput(uiIconSelector) ;
    addUIFormInput(new UIFormWYSIWYGInput(FIELD_MESSAGECONTENT, null, null, true));
    UIFormInputWithActions inputSet = new UIFormInputWithActions(FIELD_FROM_INPUT); 
    inputSet.addUIFormInput(new UIFormInputInfo(FIELD_ATTACHMENTS, FIELD_ATTACHMENTS, null)) ;
    inputSet.setActionField(FIELD_FROM_INPUT, getUploadFileList()) ;
    addUIFormInput(inputSet) ;
  }
  
  public void setTopicIds(String categoryId, String forumId) {
    this.categoryId = categoryId ;
    this.forumId = forumId ;
  }
  
  public void activate() throws Exception {
    // TODO Auto-generated method stub
    
  }
  public void deActivate() throws Exception {
    // TODO Auto-generated method stub
  }
  
  
  public List<ActionData> getUploadFileList() { 
    List<ActionData> uploadedFiles = new ArrayList<ActionData>() ;
    for(ForumAttachment attachdata : attachments_) {
      ActionData fileUpload = new ActionData() ;
      fileUpload.setActionListener("") ;
      fileUpload.setActionType(ActionData.TYPE_ICON) ;
      fileUpload.setCssIconClass("AttachmentIcon ZipFileIcon") ;
      fileUpload.setActionName(attachdata.getName() + " ("+attachdata.getSize()+" Kb)" ) ;
      fileUpload.setShowLabel(true) ;
      uploadedFiles.add(fileUpload) ;
      ActionData removeAction = new ActionData() ;
      removeAction.setActionListener("RemoveAttachment") ;
      removeAction.setActionName(ACT_REMOVE);
      removeAction.setActionParameter(attachdata.getId());
      removeAction.setActionType(ActionData.TYPE_LINK) ;
      removeAction.setBreakLine(true) ;
      uploadedFiles.add(removeAction) ;
    }
    return uploadedFiles ;
  }
  public void refreshUploadFileList() throws Exception {
    UIFormInputWithActions inputSet = getChildById(FIELD_FROM_INPUT) ;
    inputSet.setActionField(FIELD_ATTACHMENTS, getUploadFileList()) ;
  }
  public void addToUploadFileList(ForumAttachment attachfile) {
    attachments_.add(attachfile) ;
  }
  public void removeFromUploadFileList(ForumAttachment attachfile) {
    attachments_.remove(attachfile);
  }  
  public void removeUploadFileList() {
    attachments_.clear() ;
  }
  public List<ForumAttachment> getAttachFileList() {
    return attachments_ ;
  }

  public  String[] getIdChild(int Tab) throws Exception {
    String[] actions ;
    switch (Tab) {
      case 1:actions = new String[] {FIELD_TOPICTITLE_INPUT, FIELD_MESSAGECONTENT, FIELD_FROM_INPUT} ;  break;
      case 2:actions = new String[] {} ;  break;
      case 3:actions = new String[] {FIELD_TOPICSTATUS_SELECTBOX, FIELD_TOPICSTATE_SELECTBOX, FIELD_APPROVED_CHECKBOX, "ModeratePost", 
                                     FIELD_NOTIFYWHENADDPOST_CHECKBOX, FIELD_STICKY_CHECKBOX} ;  break;
      case 4:actions = new String[] {FIELD_CANVIEW_INPUT, FIELD_CANPOST_INPUT} ;  break;
     default:actions = new String[] {}; break;
    }
    return actions;
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
  
  public void setUpdateTopic(Topic topic, boolean isUpdate) throws Exception {
    if(isUpdate) {
      this.topicId = topic.getId() ;
      getUIStringInput(FIELD_TOPICTITLE_INPUT).setValue(topic.getTopicName());
      //getUIFormTextAreaInput(FIELD_MESSENGER_TEXTAREA).setDefaultValue(topic.getDescription());
      getChild(UIFormWYSIWYGInput.class).setValue(topic.getDescription());
      String stat = "open";
      if(topic.getIsClosed()) stat = "closed";
      getUIFormSelectBox(FIELD_TOPICSTATE_SELECTBOX).setValue(stat);
      if(topic.getIsLock()) stat = "locked";
      else stat = "unlock";
      getUIFormSelectBox(FIELD_TOPICSTATUS_SELECTBOX).setValue(stat);
      getUIFormCheckBoxInput(FIELD_APPROVED_CHECKBOX).setChecked(topic.getIsApproved());
      getUIFormCheckBoxInput(FIELD_MODERATEPOST_CHECKBOX).setChecked(topic.getIsModeratePost());
      getUIFormCheckBoxInput(FIELD_NOTIFYWHENADDPOST_CHECKBOX).setChecked(topic.getIsNotifyWhenAddPost());
      getUIFormCheckBoxInput(FIELD_STICKY_CHECKBOX).setChecked(topic.getIsSticky());
      getUIStringInput(FIELD_CANVIEW_INPUT).setValue(unSplitForForum(topic.getCanView()));
      getUIStringInput(FIELD_CANPOST_INPUT).setValue(unSplitForForum(topic.getCanPost()));
      
      getChild(UIFormInputIconSelector.class).setSelectedIcon(topic.getIcon());
    }
  }
  
  static  public class PreviewThreadActionListener extends EventListener<UITopicForm> {
    public void execute(Event<UITopicForm> event) throws Exception {
      UITopicForm uiForm = event.getSource() ;
      UIFormStringInput stringInputTitle = uiForm.getUIStringInput(FIELD_TOPICTITLE_INPUT) ; 
      stringInputTitle.addValidator(EmptyNameValidator.class);
      String topicTitle = stringInputTitle.getValue().trim();
      String messenger = uiForm.getChild(UIFormWYSIWYGInput.class).getValue();
      	//uiForm.getUIFormTextAreaInput(FIELD_MESSENGER_TEXTAREA).getValue() ;
      String userName = Util.getPortalRequestContext().getRemoteUser() ;
      Post postNew = new Post();
      postNew.setOwner(userName);
      postNew.setSubject(topicTitle);
      postNew.setCreatedDate(new Date());
      postNew.setModifiedBy(userName);
      postNew.setModifiedDate(new Date());
      postNew.setMessage(messenger);
      postNew.setAttachments(uiForm.attachments_) ;
      UIFormInputIconSelector uiIconSelector = uiForm.getChild(UIFormInputIconSelector.class);
      postNew.setIcon(uiIconSelector.getSelectedIcon());
      postNew.setNumberOfAttachment(0) ;
      
      UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
      UIViewTopic viewTopic = popupAction.activate(UIViewTopic.class, 670) ;
      viewTopic.setPostView(postNew) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
    }
  }
  
  static  public class SubmitThreadActionListener extends EventListener<UITopicForm> {
    public void execute(Event<UITopicForm> event) throws Exception {
      UITopicForm uiForm = event.getSource() ;
      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
      int t = 0, k = 1 ;
      UIFormStringInput stringInputTitle = uiForm.getUIStringInput(FIELD_TOPICTITLE_INPUT) ; 
      String topicTitle = "     " + stringInputTitle.getValue();
      topicTitle = topicTitle.trim() ;
      String messenger = "     " +  uiForm.getChild(UIFormWYSIWYGInput.class).getValue();
      messenger = messenger.trim() ;
      if(messenger != null && messenger.length() > 4 ) {
      	for (int i = 0; i < messenger.length(); i++) {
      		if(messenger.charAt(i) == ' ') t++ ;
      		if(t == 3) break ;
      	}
      }
      if(topicTitle.length() <= 3) {k = 0;}
	    if(t >= 3 && k != 0) {  
      	// uiForm.getUIFormTextAreaInput(FIELD_MESSENGER_TEXTAREA).getValue() ;
	      String topicState = uiForm.getUIFormSelectBox(FIELD_TOPICSTATE_SELECTBOX).getValue();
	      String topicStatus = uiForm.getUIFormSelectBox(FIELD_TOPICSTATUS_SELECTBOX).getValue();
	      
	      Boolean approved = (Boolean)uiForm.getUIFormCheckBoxInput(FIELD_APPROVED_CHECKBOX).getValue();
	      Boolean moderatePost = (Boolean)uiForm.getUIFormCheckBoxInput(FIELD_MODERATEPOST_CHECKBOX).getValue();
	      Boolean whenNewPost = (Boolean)uiForm.getUIFormCheckBoxInput(FIELD_NOTIFYWHENADDPOST_CHECKBOX).getValue();
	      Boolean sticky = (Boolean)uiForm.getUIFormCheckBoxInput(FIELD_STICKY_CHECKBOX).getValue();
	      
	      String[] canView = uiForm.splitForForum(uiForm.getUIStringInput(FIELD_CANVIEW_INPUT).getValue()) ;
	      String[] canPost = uiForm.splitForForum(uiForm.getUIStringInput(FIELD_CANPOST_INPUT).getValue()) ;
	      
	      String userName = Util.getPortalRequestContext().getRemoteUser() ;
	      Topic topicNew = new Topic();
	      topicNew.setOwner(userName);
	      topicNew.setTopicName(topicTitle);
	      topicNew.setCreatedDate(new Date());
	      topicNew.setModifiedBy(userName);
	      topicNew.setModifiedDate(new Date());
	      topicNew.setLastPostBy(userName);
	      topicNew.setLastPostDate(new Date());
	      topicNew.setDescription(messenger);
	      
	      topicNew.setIsNotifyWhenAddPost(whenNewPost);
	      topicNew.setIsModeratePost(moderatePost);
	      topicNew.setAttachments(uiForm.attachments_) ;
	      if(topicState.equals("closed")) {
	        topicNew.setIsClosed(true);
	      }
	      if(topicStatus.equals("locked")) {
	        topicNew.setIsLock(true) ;
	      }
	      topicNew.setIsSticky(sticky);
	      topicNew.setIsApproved(approved);  
	      
	      UIFormInputIconSelector uiIconSelector = uiForm.getChild(UIFormInputIconSelector.class);
	      topicNew.setIcon(uiIconSelector.getSelectedIcon());
	      //topicNew.setAttachmentFirstPost(0) ;
	      topicNew.setCanView(canView);
	      topicNew.setCanPost(canPost);
	      
	      ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	      if(uiForm.topicId != null && uiForm.topicId.length() > 0) {
	        topicNew.setId(uiForm.topicId);
	        forumService.saveTopic(uiForm.categoryId, uiForm.forumId, topicNew, false);
	      } else {
	        topicNew.setVoteRating(0.0) ;
	        topicNew.setUserVoteRating(new String[] {}) ;
	        forumService.saveTopic(uiForm.categoryId, uiForm.forumId, topicNew, true);
	      }
	      forumPortlet.cancelAction() ;
	      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
	      context.addUIComponentToUpdateByAjax(forumPortlet) ;
	    } else {
	    	String[] args = { ""} ;
		    if(k == 0) {
		    	args = new String[] { "Thread Title" } ;
		    	if(t < 3) args = new String[] { "Thread Title and Messenger" } ;
		    } else if(t < 3) {
		    	args = new String[] { "Messenger" } ;
		    }
		    	throw new MessageException(new ApplicationMessage("NameValidator.msg.ShortText", args)) ;
	    }
    }
  }
  
  static public class AttachmentActionListener extends EventListener<UITopicForm> {
    public void execute(Event<UITopicForm> event) throws Exception {
      UITopicForm uiForm = event.getSource() ;
      UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction uiChildPopup = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
      UIAttachFileForm attachFileForm = uiChildPopup.activate(UIAttachFileForm.class, 500) ;
      attachFileForm.updateIsTopicForm(true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
    }
  }
  
  static public class RemoveAttachmentActionListener extends EventListener<UITopicForm> {
    public void execute(Event<UITopicForm> event) throws Exception {
      UITopicForm uiTopicForm = event.getSource() ;
      String attFileId = event.getRequestContext().getRequestParameter(OBJECTID);
      BufferAttachment attachfile = new BufferAttachment();
      for (ForumAttachment att : uiTopicForm.attachments_) {
        if (att.getId().equals(attFileId)) {
          attachfile = (BufferAttachment) att;
        }
      }
      uiTopicForm.removeFromUploadFileList(attachfile);
      uiTopicForm.refreshUploadFileList() ;
    }
  }
  
  
  static  public class CancelActionListener extends EventListener<UITopicForm> {
    public void execute(Event<UITopicForm> event) throws Exception {
      UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
      forumPortlet.cancelAction() ;
    }
  }
  
}
