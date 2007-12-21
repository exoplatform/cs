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

import org.exoplatform.forum.webui.UIFormSelectBoxForum;
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
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/forum/webui/popup/UIForm.gtmpl",
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
	@SuppressWarnings("unchecked")
	public UIForumOptionForm() throws Exception {
		List<SelectItemOption<String>> list ;
		list = new ArrayList<SelectItemOption<String>>() ;
		for(String string : timeZone) {
			list.add(new SelectItemOption<String>(string + "/timeZone", getTimeZoneNumberInString(string))) ;
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
		System.out.println("\ntimeZoneMyHost: " + mark + timeZoneMyHost + "0");
		timeZone.setValue(mark + timeZoneMyHost + "0");

		list = new ArrayList<SelectItemOption<String>>() ;
		list.add(new SelectItemOption<String>("mm-dd-yy (example: 07-20-2007)", "id1")) ;
		list.add(new SelectItemOption<String>("dd-mm-yy (example: 20-07-2007)", "id2")) ;
		UIFormSelectBox dateFormat = new UIFormSelectBox(FIELD_DATEFORMAT_SELECTBOX, FIELD_DATEFORMAT_SELECTBOX, list) ;
		dateFormat.setDefaultValue("id1");

		list = new ArrayList<SelectItemOption<String>>() ;
		list.add(new SelectItemOption<String>("12-hour format", "id12")) ;
		list.add(new SelectItemOption<String>("24-hour format", "id23")) ;
		UIFormSelectBox timeFormat = new UIFormSelectBox(FIELD_TIMEFORMAT_SELECTBOX, FIELD_TIMEFORMAT_SELECTBOX, list) ;
		timeFormat.setDefaultValue("id12");

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
		addUIFormInput(dateFormat) ;
		addUIFormInput(timeFormat) ;
		addUIFormInput(maximumThreads) ;
		addUIFormInput(maximumPosts) ;
		addUIFormInput(isShowForumJump) ;
	}
	
	public UIFormSelectBoxForum getUIFormSelectBoxForum(String name) {
		return	findComponentById(name) ;
	}
	
  @SuppressWarnings("deprecation")
  public String getFormatDate(String format,Date postDate) {
		 long time = postDate.getHours() ;
		 StringBuffer stringBuffer = new StringBuffer() ;
		 if(format.equals("24H")){
			 stringBuffer.append(time).append(":").append(postDate.getMinutes());
		 } else {
			 String str = "" ;
			 if(time < 12) str = "AM";
			 else {
				 str = "PM";
				 if(time > 12) time = time - 12 ;
			 }
			 stringBuffer.append(time).append(":").append(postDate.getMinutes()).append(" ").append(str);
		 }
		return stringBuffer.toString();
	}

	private String getTimeZoneNumberInString(String string) {
		if(string != null && string.length() > 0) {
			StringBuffer stringBuffer = new StringBuffer();
			int t = 0;
			for(int i = 0; i <	string.length(); ++i) {
				char c = string.charAt(i) ; 
				if (Character.isDigit(c) || c == '-' || c == '+' || c == ':'){
					if(c == ':') c = '.';
					if(c == '-' || c == '+') t = t + 1;
					if(c == '3' && string.charAt(i-1) == ':') c = '5';
					if(t == 1 || t == 0) stringBuffer.append(c);
					if(t == 2) t = 1;
				}
			}
			return stringBuffer.toString() ;
		}
		return null ;
	}
	public void activate() throws Exception {
		// TODO Auto-generated method stub
	}
	public void deActivate() throws Exception {
		// TODO Auto-generated method stub
	}
	
//	public void setUpdate() {
//		getUIFormSelectBox(FIELD_MAXTOPICS_SELECTBOX).setValue(("id" + this.maxTopic)) ;
//		getUIFormSelectBox(FIELD_MAXPOSTS_SELECTBOX).setValue(("id" + this.maxPost)) ;
//		System.out.println("\n\n" + this.maxTopic + "\n" + this.maxPost);
//	}
//	
	static	public class SaveActionListener extends EventListener<UIForumOptionForm> {
		public void execute(Event<UIForumOptionForm> event) throws Exception {
			UIForumOptionForm uiForm = event.getSource() ;
			long maxTopic = Long.parseLong(uiForm.getUIFormSelectBox(FIELD_MAXTOPICS_SELECTBOX).getValue().replaceFirst("id", "")) ;
			long maxPost = Long.parseLong(uiForm.getUIFormSelectBox(FIELD_MAXPOSTS_SELECTBOX).getValue().replaceFirst("id", "")) ;
			double timezone = Double.parseDouble(uiForm.getUIFormSelectBoxForum(FIELD_TIMEZONE_SELECTBOX).getValue());
			System.out.println("\n\nTimeZone:	"	+ timezone);
			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			UITopicContainer topicContainer = forumPortlet.findFirstComponentOfType(UITopicContainer.class);
			topicContainer.setMaxPostInPage(maxPost) ;
			topicContainer.setMaxTopicInPage(maxTopic) ;
			forumPortlet.findFirstComponentOfType(UITopicDetail.class).setMaxPostInPage(maxPost);
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
