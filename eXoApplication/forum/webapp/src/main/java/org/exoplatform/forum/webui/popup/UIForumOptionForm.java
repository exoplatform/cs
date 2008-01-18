/***************************************************************************
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumFormatUtils;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.service.ForumOption;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.webui.UIFormSelectBoxForum;
import org.exoplatform.forum.webui.UIForumPortlet;
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
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/forum/webui/popup/UIFormForum.gtmpl",
		events = {
			@EventConfig(listeners = UIForumOptionForm.SaveActionListener.class), 
			@EventConfig(listeners = UIForumOptionForm.CancelActionListener.class, phase=Phase.DECODE)
		}
)
public class UIForumOptionForm extends UIForm implements UIPopupComponent {
	public static final String FIELD_TIMEZONE_SELECTBOX = "TimeZone" ;
	public static final String FIELD_SHORTDATEFORMAT_SELECTBOX = "ShortDateformat" ;
	public static final String FIELD_LONGDATEFORMAT_SELECTBOX = "LongDateformat" ;
	public static final String FIELD_TIMEFORMAT_SELECTBOX = "Timeformat" ;
	public static final String FIELD_MAXTOPICS_SELECTBOX = "MaximumThreads" ;
	public static final String FIELD_MAXPOSTS_SELECTBOX = "MaximumPosts" ;
	public static final String FIELD_FORUMJUMP_CHECKBOX = "ShowForumJump" ;
	public static final String FIELD_TIMEZONE = "timeZone" ;
	
	private ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	public UIForumOptionForm() throws Exception {
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
  private void add() throws Exception {
		String userName = ForumSessionUtils.getCurrentUser() ;
		ForumOption forumOption = new ForumOption() ;
		forumOption = forumService.getOption(ForumSessionUtils.getSystemProvider(), userName) ;
		boolean isForumOption = false ;
		if(forumOption != null) {
			isForumOption = true ;
		}
		List<SelectItemOption<String>> list ;
		String []timeZone1 = getLabel(FIELD_TIMEZONE).split("/") ;
		list = new ArrayList<SelectItemOption<String>>() ;
		for(String string : timeZone1) {
			list.add(new SelectItemOption<String>(string + "/timeZone", ForumFormatUtils.getTimeZoneNumberInString(string))) ;
		}
		UIFormSelectBoxForum timeZone = new UIFormSelectBoxForum(FIELD_TIMEZONE_SELECTBOX, FIELD_TIMEZONE_SELECTBOX, list) ;
		Date date = new Date() ;
		double timeZoneMyHost ;
		if(isForumOption) {
			timeZoneMyHost = forumOption.getTimeZone() ;
		} else {
			timeZoneMyHost = date.getTimezoneOffset()/ 60 ;
		}
		String mark = "+";
		if(timeZoneMyHost < 0) {
			timeZoneMyHost = -timeZoneMyHost ;
		} else if(timeZoneMyHost > 0){
			mark = "-" ;
		} else {
			timeZoneMyHost = 0.0 ;
			mark = "";
		}
		timeZone.setValue(mark + timeZoneMyHost + "0");
		list = new ArrayList<SelectItemOption<String>>() ;
		String []format = new String[] {"M-D-yyyy", "M-D-yy", "MM-DD-yy", "MM-DD-yyyy","yyyy-MM-DD", "yy-MM-DD", "DD-MM-yyyy", "DD-MM-yy",
				"M/D/yyyy", "M/D/yy", "MM/DD/yy", "MM/DD/yyyy","yyyy/MM/DD", "yy/MM/DD", "DD/MM/yyyy", "DD/MM/yy"} ;
		for (String frm : format) {
			list.add(new SelectItemOption<String>((frm.toLowerCase() +" ("  + ForumFormatUtils.getFormatDate(frm, date)+")"), frm)) ;
    }
		UIFormSelectBox shortdateFormat = new UIFormSelectBox(FIELD_SHORTDATEFORMAT_SELECTBOX, FIELD_SHORTDATEFORMAT_SELECTBOX, list) ;
		if(isForumOption) {
			shortdateFormat.setValue(forumOption.getShortDateFormat());
		} else {
			shortdateFormat.setValue("M-D-yyyy");
		}
		list = new ArrayList<SelectItemOption<String>>() ;
		format = new String[] {"DDD,MMMM DD,yyyy", "DDDD,MMMM DD,yyyy", "DDDD,DD MMMM,yyyy", "DDD,MMM DD,yyyy", "DDDD,MMM DD,yyyy", "DDDD,DD MMM,yyyy",
				 								"MMMM DD,yyyy", "DD MMMM,yyyy","MMM DD,yyyy", "DD MMM,yyyy"} ;
		for (String idFrm : format) {
			list.add(new SelectItemOption<String>((idFrm.toLowerCase() +" (" + ForumFormatUtils.getFormatDate(idFrm, date)+")"), idFrm)) ;
		}
		UIFormSelectBox longDateFormat = new UIFormSelectBox(FIELD_LONGDATEFORMAT_SELECTBOX, FIELD_LONGDATEFORMAT_SELECTBOX, list) ;
		if(isForumOption) {
			longDateFormat.setValue(forumOption.getLongDateFormat());
		} else {
			longDateFormat.setValue("DDD,MMMM DD,yyyy");
		}
		list = new ArrayList<SelectItemOption<String>>() ;
		list.add(new SelectItemOption<String>("12-hour format", "id12h")) ;
		list.add(new SelectItemOption<String>("24-hour format", "id24h")) ;
		UIFormSelectBox timeFormat = new UIFormSelectBox(FIELD_TIMEFORMAT_SELECTBOX, FIELD_TIMEFORMAT_SELECTBOX, list) ;
		if(isForumOption) {
			timeFormat.setValue("id" + forumOption.getTimeFormat());
		} else {
			timeFormat.setValue("id12h");
		}
		list = new ArrayList<SelectItemOption<String>>() ;
		for(int i=5; i <= 35; i = i + 5) {
			list.add(new SelectItemOption<String>(String.valueOf(i),("id" + i))) ;
		}
		UIFormSelectBox maximumThreads = new UIFormSelectBox(FIELD_MAXTOPICS_SELECTBOX, FIELD_MAXTOPICS_SELECTBOX, list) ;
		if(isForumOption) {
			maximumThreads.setValue("id" + forumOption.getMaxTopicInPage());
		} else {
			maximumThreads.setValue("id10");
		}
		list = new ArrayList<SelectItemOption<String>>() ;
		for(int i=5; i <= 45; i = i + 5) {
			list.add(new SelectItemOption<String>(String.valueOf(i), ("id" + i))) ;
		}
		UIFormSelectBox maximumPosts = new UIFormSelectBox(FIELD_MAXPOSTS_SELECTBOX, FIELD_MAXPOSTS_SELECTBOX, list) ;
		if(isForumOption) {
			maximumPosts.setValue("id" + forumOption.getMaxPostInPage());
		} else {
			maximumPosts.setValue("id10");
		}
		boolean isJump = false ;
		if(isForumOption) {
			isJump = forumOption.getIsShowForumJump() ;
		}
		UIFormCheckBoxInput isShowForumJump = new UIFormCheckBoxInput<Boolean>(FIELD_FORUMJUMP_CHECKBOX, FIELD_FORUMJUMP_CHECKBOX, isJump);
		isShowForumJump.setChecked(isJump) ;
		
		addUIFormInput(timeZone) ;
		addUIFormInput(shortdateFormat) ;
		addUIFormInput(longDateFormat) ;
		addUIFormInput(timeFormat) ;
		addUIFormInput(maximumThreads) ;
		addUIFormInput(maximumPosts) ;
		addUIFormInput(isShowForumJump) ;
  }
	
	public UIFormSelectBoxForum getUIFormSelectBoxForum(String name) {
		return	findComponentById(name) ;
	}
  
	public void activate() throws Exception {
		add() ;
	}
	public void deActivate() throws Exception {
		// TODO Auto-generated method stub
	}
	
	static	public class SaveActionListener extends EventListener<UIForumOptionForm> {
    public void execute(Event<UIForumOptionForm> event) throws Exception {
			UIForumOptionForm uiForm = event.getSource() ;
			long maxTopic = Long.parseLong(uiForm.getUIFormSelectBox(FIELD_MAXTOPICS_SELECTBOX).getValue().substring(2)) ;
			long maxPost = Long.parseLong(uiForm.getUIFormSelectBox(FIELD_MAXPOSTS_SELECTBOX).getValue().substring(2)) ;
			double timeZone = Double.parseDouble(uiForm.getUIFormSelectBoxForum(FIELD_TIMEZONE_SELECTBOX).getValue());
			String shortDateFormat = uiForm.getUIFormSelectBox(FIELD_SHORTDATEFORMAT_SELECTBOX).getValue();
			String longDateFormat = uiForm.getUIFormSelectBox(FIELD_LONGDATEFORMAT_SELECTBOX).getValue();
			String timeFormat = uiForm.getUIFormSelectBox(FIELD_TIMEFORMAT_SELECTBOX).getValue().substring(2);
			boolean isJump = (Boolean)uiForm.getUIFormCheckBoxInput(FIELD_FORUMJUMP_CHECKBOX).getValue() ;
			String userName = ForumSessionUtils.getCurrentUser() ;
			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			if(userName != null && userName.length() > 0) {
				ForumOption forumOption = new ForumOption() ;
				forumOption.setUserName(userName);
				forumOption.setTimeZone(-timeZone) ;
				forumOption.setTimeFormat(timeFormat);
				forumOption.setShortDateFormat(shortDateFormat);
				forumOption.setLongDateFormat(longDateFormat);
				forumOption.setMaxPostInPage(maxPost);
				forumOption.setMaxTopicInPage(maxTopic);
				forumOption.setIsShowForumJump(isJump);
				uiForm.forumService.saveOption(ForumSessionUtils.getSystemProvider(), forumOption);
				forumPortlet.initOption() ;
			}
			forumPortlet.cancelAction() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
		}
	}
	
	static	public class CancelActionListener extends EventListener<UIForumOptionForm> {
    public void execute(Event<UIForumOptionForm> event) throws Exception {
			UIForumOptionForm uiForm = event.getSource() ;
			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.cancelAction() ;
		}
	}
}
