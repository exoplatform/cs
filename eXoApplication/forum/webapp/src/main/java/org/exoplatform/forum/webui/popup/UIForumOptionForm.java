/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicContainer;
import org.exoplatform.forum.webui.UITopicDetail;
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

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/forum/webui/popup/UICategoryForm.gtmpl",
    events = {
      @EventConfig(listeners = UIForumOptionForm.SaveActionListener.class), 
      @EventConfig(listeners = UIForumOptionForm.CancelActionListener.class, phase=Phase.DECODE)
    }
)
public class UIForumOptionForm extends UIForm implements UIPopupComponent {
	public static final String FIELD_TIMEZONE_SELECTBOX = "TimeZone" ;
	public static final String FIELD_DATEFORMAT_SELECTBOX = "Dateformat" ;
	public static final String FIELD_TIMEFORMAT_SELECTBOX = "Timeformat" ;
	public static final String FIELD_MAXTOPICS_SELECTBOX = "MaximumThreads" ;
	public static final String FIELD_MAXPOSTS_SELECTBOX = "MaximumPosts" ;
	public static final String FIELD_FORUMJUMP_CHECKBOX= "ShowForumJump" ;
  
	private long maxTopic = 5 ;
  private long maxPost = 5 ;
  @SuppressWarnings("unchecked")
  public UIForumOptionForm() throws Exception {
  	List<SelectItemOption<String>> list ;
  	list = new ArrayList<SelectItemOption<String>>() ;
  	list.add(new SelectItemOption<String>("(GMT +7:00) Bangkok, Hanoi, Jakarta", "id1")) ;
  	list.add(new SelectItemOption<String>("(GMT +6:00) Yangon", "id2")) ;
  	UIFormSelectBox timeZone = new UIFormSelectBox(FIELD_TIMEZONE_SELECTBOX, FIELD_TIMEZONE_SELECTBOX, list) ;
  	timeZone.setDefaultValue("id1");

  	list = new ArrayList<SelectItemOption<String>>() ;
  	list.add(new SelectItemOption<String>("mm-dd-yy (example: 07-20-2007)", "id1")) ;
  	list.add(new SelectItemOption<String>("dd-mm-yy (example: 20-07-2007)", "id2")) ;
  	UIFormSelectBox dateFormat = new UIFormSelectBox(FIELD_DATEFORMAT_SELECTBOX, FIELD_DATEFORMAT_SELECTBOX, list) ;
  	dateFormat.setDefaultValue("id1");

  	list = new ArrayList<SelectItemOption<String>>() ;
  	list.add(new SelectItemOption<String>("12-hour format", "id1")) ;
  	list.add(new SelectItemOption<String>("24-hour format", "id2")) ;
  	UIFormSelectBox timeFormat = new UIFormSelectBox(FIELD_TIMEFORMAT_SELECTBOX, FIELD_TIMEFORMAT_SELECTBOX, list) ;
  	timeFormat.setDefaultValue("id1");

  	list = new ArrayList<SelectItemOption<String>>() ;
  	for(int i=5; i <= 35; i = i + 5) {
  		list.add(new SelectItemOption<String>(("" + i), ("" + i))) ;
  	}
  	UIFormSelectBox maximumThreads = new UIFormSelectBox(FIELD_MAXTOPICS_SELECTBOX, FIELD_MAXTOPICS_SELECTBOX, list) ;
  	maximumThreads.setValue("5");

  	list = new ArrayList<SelectItemOption<String>>() ;
  	for(int i=5; i <= 45; i = i + 5) {
  		list.add(new SelectItemOption<String>(("" + i), ("" + i))) ;
  	}
  	UIFormSelectBox maximumPosts = new UIFormSelectBox(FIELD_MAXPOSTS_SELECTBOX, FIELD_MAXPOSTS_SELECTBOX, list) ;
  	maximumPosts.setValue("5");
  	UIFormCheckBoxInput isShowForumJump = new UIFormCheckBoxInput<Boolean>(FIELD_FORUMJUMP_CHECKBOX, FIELD_FORUMJUMP_CHECKBOX, false);
  
  	addUIFormInput(timeZone) ;
  	addUIFormInput(dateFormat) ;
  	addUIFormInput(timeFormat) ;
  	addUIFormInput(maximumThreads) ;
  	addUIFormInput(maximumPosts) ;
  	addUIFormInput(isShowForumJump) ;
  }
  
  public void activate() throws Exception {
		// TODO Auto-generated method stub
	}
	public void deActivate() throws Exception {
		this.setUpdate() ;
		// TODO Auto-generated method stub
	}
	
	public void setUpdate() {
	  getUIFormSelectBox(FIELD_MAXTOPICS_SELECTBOX).setValue("" + this.maxTopic) ;
	  getUIFormSelectBox(FIELD_MAXPOSTS_SELECTBOX).setValue("" + this.maxPost) ;
  }
  
  static  public class SaveActionListener extends EventListener<UIForumOptionForm> {
    public void execute(Event<UIForumOptionForm> event) throws Exception {
      UIForumOptionForm uiForm = event.getSource() ;
      uiForm.maxTopic = Long.parseLong(uiForm.getUIFormSelectBox(FIELD_MAXTOPICS_SELECTBOX).getValue() ) ;
      uiForm.maxPost = Long.parseLong(uiForm.getUIFormSelectBox(FIELD_MAXPOSTS_SELECTBOX).getValue() ) ;
      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
      UITopicContainer topicContainer = forumPortlet.findFirstComponentOfType(UITopicContainer.class);
      topicContainer.setMaxPostInPage(uiForm.maxPost) ;
      topicContainer.setMaxTopicInPage(uiForm.maxTopic) ;
      forumPortlet.findFirstComponentOfType(UITopicDetail.class).setMaxPostInPage(uiForm.maxPost);
      forumPortlet.cancelAction() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIForumOptionForm> {
    public void execute(Event<UIForumOptionForm> event) throws Exception {
      UIForumOptionForm uiForm = event.getSource() ;
      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
      forumPortlet.cancelAction() ;
    }
  }
}
