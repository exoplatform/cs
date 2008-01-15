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
import org.exoplatform.forum.ForumFormatFunction;
import org.exoplatform.forum.ForumUtils;
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
	private static final String[] timeZone = {
			"(GMT -12:00) Eniwetok, Kwajalein", 
			"(GMT -11:00) Midway Island, Samoa", 
			"(GMT -10:00) Hawaii", 
			"(GMT -9:00) Alaska", 
			"(GMT -8:00) Pacific Time (US &amp; Canada)", 
			"(GMT -7:00) Mountain Time (US &amp; Canada)", 
			"(GMT -6:00) Central Time (US &amp; Canada), Mexico City", 
			"(GMT -5:00) Eastern Time (US &amp; Canada), Bogota, Lima", 
			"(GMT -4:00) Atlantic Time (Canada), Caracas, La Paz", 
			"(GMT -3:30) Newfoundland", 
			"(GMT -3:00) Brazil, Buenos Aires, Georgetown", 
			"(GMT -2:00) Mid-Atlantic", 
			"(GMT -1:00) Azores, Cape Verde Islands", 
			"(GMT	0:00) Greenwich Mean Time: Dublin, London, Lisbon, Casablanca", 
			"(GMT +1:00) Brussels, Copenhagen, Madrid, Paris", 
			"(GMT +2:00) Kaliningrad, South Africa", 
			"(GMT +3:00) Baghdad, Riyadh, Moscow, St. Petersburg", 
			"(GMT +3:30) Tehran", 
			"(GMT +4:00) Abu Dhabi, Muscat, Baku, Tbilisi", 
			"(GMT +4:30) Kabul", 
			"(GMT +5:00) Ekaterinburg, Islamabad, Karachi, Tashkent", 
			"(GMT +5:30) Bombay, Calcutta, Madras, New Delhi", 
			"(GMT +6:00) Almaty, Dhaka, Colombo", 
			"(GMT +7:00) Bangkok, Ha Noi, Jakarta", 
			"(GMT +8:00) Beijing, Perth, Singapore, Hong Kong", 
			"(GMT +9:00) Tokyo, Seoul, Osaka, Sapporo, Yakutsk", 
			"(GMT +9:30) Adelaide, Darwin", 
			"(GMT +10:00) Eastern Australia, Guam, Vladivostok", 
			"(GMT +11:00) Magadan, Solomon Islands, New Caledonia", 
			"(GMT +12:00) Auckland, Wellington, Fiji, Kamchatka", 
		} ;
	@SuppressWarnings({ "unchecked", "deprecation" })
	public UIForumOptionForm() throws Exception {
		
		List<SelectItemOption<String>> list ;
		list = new ArrayList<SelectItemOption<String>>() ;
		for(String string : timeZone) {
			list.add(new SelectItemOption<String>(string + "/timeZone", ForumFormatFunction.getTimeZoneNumberInString(string))) ;
		}
		UIFormSelectBoxForum timeZone = new UIFormSelectBoxForum(FIELD_TIMEZONE_SELECTBOX, FIELD_TIMEZONE_SELECTBOX, list) ;
		Date date = new Date() ;
		double timeZoneMyHost = date.getTimezoneOffset()/ 60 ;
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
		String []format = new String[] {"m-d-yyyy", "m-d-yy", "mm-dd-yy", "mm-dd-yyyy","yyyy-mm-dd", "yy-mm-dd", "dd-mm-yyyy", "dd-mm-yy",
				"m/d/yyyy", "m/d/yy", "mm/dd/yy", "mm/dd/yyyy","yyyy/mm/dd", "yy/mm/dd", "dd/mm/yyyy", "dd/mm/yy"} ;
		for (String frm : format) {
			list.add(new SelectItemOption<String>((frm +" ("  + ForumFormatFunction.getFormatDate(frm, date)+")"), frm)) ;
    }
		UIFormSelectBox shortdateFormat = new UIFormSelectBox(FIELD_SHORTDATEFORMAT_SELECTBOX, FIELD_SHORTDATEFORMAT_SELECTBOX, list) ;
		shortdateFormat.setDefaultValue("m-d-yyyy");

		list = new ArrayList<SelectItemOption<String>>() ;
		format = new String[] {"ddd, MMMM dd, yyyy", "dddd, MMMM dd, yyyy", "dddd, dd MMMM, yyyy", "MMMM dd, yyyy", "dd MMMM, yyyy"};
		String []idFormat = new String[] {"ddd,mmm,dd,yyyy", "dddd,mmm,dd,yyyy", "dddd,dd,mmm,yyyy", "mmm,dd,yyyy", "dd,mmm,yyyy"} ;
		for (int i = 0; i < idFormat.length; i++) {
			list.add(new SelectItemOption<String>((format[i] +" (" + ForumFormatFunction.getFormatDate(idFormat[i], date)+")"), idFormat[i])) ;
    }
		UIFormSelectBox longDateFormat = new UIFormSelectBox(FIELD_LONGDATEFORMAT_SELECTBOX, FIELD_LONGDATEFORMAT_SELECTBOX, list) ;
		longDateFormat.setDefaultValue("ddd,mmm,dd,yyyy");

		list = new ArrayList<SelectItemOption<String>>() ;
		list.add(new SelectItemOption<String>("12-hour format", "id12h")) ;
		list.add(new SelectItemOption<String>("24-hour format", "id24h")) ;
		UIFormSelectBox timeFormat = new UIFormSelectBox(FIELD_TIMEFORMAT_SELECTBOX, FIELD_TIMEFORMAT_SELECTBOX, list) ;
		timeFormat.setDefaultValue("id12h");

		list = new ArrayList<SelectItemOption<String>>() ;
		for(int i=5; i <= 35; i = i + 5) {
			list.add(new SelectItemOption<String>(String.valueOf(i),("id" + i))) ;
		}
		UIFormSelectBox maximumThreads = new UIFormSelectBox(FIELD_MAXTOPICS_SELECTBOX, FIELD_MAXTOPICS_SELECTBOX, list) ;
		maximumThreads.setValue("id10");

		list = new ArrayList<SelectItemOption<String>>() ;
		for(int i=5; i <= 45; i = i + 5) {
			list.add(new SelectItemOption<String>(String.valueOf(i), ("id" + i))) ;
		}
		UIFormSelectBox maximumPosts = new UIFormSelectBox(FIELD_MAXPOSTS_SELECTBOX, FIELD_MAXPOSTS_SELECTBOX, list) ;
		maximumPosts.setValue("id10");
		UIFormCheckBoxInput isShowForumJump = new UIFormCheckBoxInput<Boolean>(FIELD_FORUMJUMP_CHECKBOX, FIELD_FORUMJUMP_CHECKBOX, false);
	
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
		// TODO Auto-generated method stub
		setUpdate() ;
	}
	public void deActivate() throws Exception {
		// TODO Auto-generated method stub
	}
	
	public void setUpdate() throws Exception {
		String userName = ForumUtils.getCurrentUser() ;
		ForumOption forumOption = new ForumOption() ;
		forumOption = forumService.getOption(ForumUtils.getSystemProvider(), userName) ;
		if(forumOption != null) {
			double timeZone = forumOption.getTimeZone();
			String mark = "+";
			if(timeZone < 0) {
				timeZone = -timeZone ;
			} else if(timeZone > 0){
				mark = "-" ;
			} else {
				timeZone = 0.0 ;
				mark = "";
			}
			System.out.println(getLabel(FIELD_TIMEZONE));
			getUIFormSelectBoxForum(FIELD_TIMEZONE_SELECTBOX).setValue(mark + timeZone + "0") ;
			getUIFormSelectBox(FIELD_SHORTDATEFORMAT_SELECTBOX).setValue(forumOption.getShortDateFormat()) ;
			getUIFormSelectBox(FIELD_LONGDATEFORMAT_SELECTBOX).setValue(forumOption.getLongDateFormat()) ;
			getUIFormSelectBox(FIELD_TIMEFORMAT_SELECTBOX).setValue(("id" + forumOption.getTimeFormat())) ;
			getUIFormSelectBox(FIELD_MAXTOPICS_SELECTBOX).setValue(("id" + forumOption.getMaxTopicInPage())) ;
			getUIFormSelectBox(FIELD_MAXPOSTS_SELECTBOX).setValue(("id" + forumOption.getMaxPostInPage())) ;
			getUIFormCheckBoxInput(FIELD_FORUMJUMP_CHECKBOX).setChecked(forumOption.getIsShowForumJump());
		}
	}
	
	static	public class SaveActionListener extends EventListener<UIForumOptionForm> {
		@Override
    public void execute(Event<UIForumOptionForm> event) throws Exception {
			UIForumOptionForm uiForm = event.getSource() ;
			long maxTopic = Long.parseLong(uiForm.getUIFormSelectBox(FIELD_MAXTOPICS_SELECTBOX).getValue().substring(2)) ;
			long maxPost = Long.parseLong(uiForm.getUIFormSelectBox(FIELD_MAXPOSTS_SELECTBOX).getValue().substring(2)) ;
			double timeZone = Double.parseDouble(uiForm.getUIFormSelectBoxForum(FIELD_TIMEZONE_SELECTBOX).getValue());
			String shortDateFormat = uiForm.getUIFormSelectBox(FIELD_SHORTDATEFORMAT_SELECTBOX).getValue();
			String longDateFormat = uiForm.getUIFormSelectBox(FIELD_LONGDATEFORMAT_SELECTBOX).getValue();
			String timeFormat = uiForm.getUIFormSelectBox(FIELD_TIMEFORMAT_SELECTBOX).getValue().substring(2);
			boolean isJump = (Boolean)uiForm.getUIFormCheckBoxInput(FIELD_FORUMJUMP_CHECKBOX).getValue() ;
			String userName = ForumUtils.getCurrentUser() ;
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
				uiForm.forumService.saveOption(ForumUtils.getSystemProvider(), forumOption);
				forumPortlet.initOption() ;
			}
			forumPortlet.cancelAction() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
		}
	}
	
	static	public class CancelActionListener extends EventListener<UIForumOptionForm> {
		@Override
    public void execute(Event<UIForumOptionForm> event) throws Exception {
			UIForumOptionForm uiForm = event.getSource() ;
			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.cancelAction() ;
		}
	}
}
