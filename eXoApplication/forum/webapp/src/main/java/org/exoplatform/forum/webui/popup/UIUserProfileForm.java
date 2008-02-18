/***************************************************************************
 * Copyright (C) 2003-2008 eXo Platform SAS.
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

import org.exoplatform.forum.ForumFormatUtils;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.webui.UIFormSelectBoxForum;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
/**
 * Created by The eXo Platform SARL
 * Author : Vu Duy Tu
 *					tu.duy@exoplatform.com
 *  Feb 18, 2008 9:55:05 AM
 */

@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/forum/webui/popup/UIEditUserProfile.gtmpl",
		events = {
			@EventConfig(listeners = UIUserProfileForm.SaveActionListener.class), 
      @EventConfig(listeners = UIUserProfileForm.CancelActionListener.class, phase=Phase.DECODE)
		}
)
public class UIUserProfileForm extends UIForm implements UIPopupComponent {
	
	public static final String FIELD_USERPROFILE_FORM = "UserProfile" ;
	public static final String FIELD_USEROPTION_FORM = "UserOption" ;
	public static final String FIELD_USERBAN_FORM = "UserBan" ;
	
	public static final String FIELD_USERID_INPUT = "UserName" ;
	public static final String FIELD_USERROLE_SELECTBOX = "UserRole" ;
	public static final String FIELD_SIGNATURE_TEXTAREA = "Signature" ;
	public static final String FIELD_ISDISPLAYSIGNATURE_CHECKBOX = "IsDisplaySignature" ;
	public static final String FIELD_MODERATEFORUMS_MULTIVALUE = "ModForums" ;
	public static final String FIELD_MODERATETOPICS_MULTIVALUE = "MosTopics" ;
	public static final String FIELD_ISDISPLAYAVATAR_CHECKBOX = "IsDisplayAvatar" ;
	
	public static final String FIELD_TIMEZONE_SELECTBOX = "TimeZone" ;
	public static final String FIELD_SHORTDATEFORMAT_SELECTBOX = "ShortDateformat" ;
	public static final String FIELD_LONGDATEFORMAT_SELECTBOX = "LongDateformat" ;
	public static final String FIELD_TIMEFORMAT_SELECTBOX = "Timeformat" ;
	public static final String FIELD_MAXTOPICS_SELECTBOX = "MaximumThreads" ;
	public static final String FIELD_MAXPOSTS_SELECTBOX = "MaximumPosts" ;
	public static final String FIELD_FORUMJUMP_CHECKBOX = "ShowForumJump" ;
	public static final String FIELD_TIMEZONE = "timeZone" ;
	
	public static final String FIELD_ISBANNED_CHECKBOX = "IsBanned" ;
	public static final String FIELD_BANUNTIL_INPUT = "BanUntil" ;
	public static final String FIELD_BANREASON_TEXTAREA = "BanReason" ;
	public static final String FIELD_BANCOUNTER_INPUT = "BanCounter" ;
	public static final String FIELD_BANREASONSUMMARY_MULTIVALUE = "BanReasonSummary" ;
	public static final String FIELD_CREATEDDATEBAN_INPUT = "CreatedDateBan" ;

	
	private UserProfile userProfile = new UserProfile();
	public UIUserProfileForm() {	  
  }
	
	public void setUserProfile(UserProfile userProfile) throws Exception {
	  this.userProfile = userProfile ;
	  this.initUserProfileForm() ;
	}
	
	@SuppressWarnings("unchecked")
  private void initUserProfileForm() throws Exception {
		List<SelectItemOption<String>> list ;
		UIFormStringInput userId = new UIFormStringInput(FIELD_USERID_INPUT, FIELD_USERID_INPUT, null);
		userId.setValue(this.userProfile.getUserId());
		UIFormStringInput userTitle = new UIFormStringInput(FIELD_USERID_INPUT, FIELD_USERID_INPUT, null);
		userTitle.setValue(this.userProfile.getUserTitle());
		list = new ArrayList<SelectItemOption<String>>() ;
		list.add(new SelectItemOption<String>("Admin", "id0")) ;
		list.add(new SelectItemOption<String>("Moderator", "id1")) ;
		list.add(new SelectItemOption<String>("Register User", "id2")) ;
		UIFormSelectBox userRole = new UIFormSelectBox(FIELD_USERROLE_SELECTBOX, FIELD_USERROLE_SELECTBOX, list) ;
		userRole.setValue("id" + this.userProfile.getUserRole());
		UIFormTextAreaInput signature = new UIFormTextAreaInput(FIELD_SIGNATURE_TEXTAREA, FIELD_SIGNATURE_TEXTAREA, null);
		signature.setValue(this.userProfile.getSignature());
		UIFormCheckBoxInput isDisplaySignature = new UIFormCheckBoxInput<Boolean>(FIELD_ISDISPLAYSIGNATURE_CHECKBOX, FIELD_ISDISPLAYSIGNATURE_CHECKBOX, this.userProfile.getIsDisplaySignature());
		UIFormTextAreaInput moderateForums = new UIFormTextAreaInput(FIELD_MODERATEFORUMS_MULTIVALUE, FIELD_MODERATEFORUMS_MULTIVALUE, null);
		UIFormTextAreaInput moderateTopics = new UIFormTextAreaInput(FIELD_MODERATETOPICS_MULTIVALUE, FIELD_MODERATETOPICS_MULTIVALUE, null);
		UIFormCheckBoxInput isDisplayAvatar = new UIFormCheckBoxInput<Boolean>(FIELD_ISDISPLAYAVATAR_CHECKBOX, FIELD_ISDISPLAYAVATAR_CHECKBOX, this.userProfile.getIsDisplayAvatar());
		//Option
		String []timeZone1 = getLabel(FIELD_TIMEZONE).split("/") ;
		list = new ArrayList<SelectItemOption<String>>() ;
		for(String string : timeZone1) {
			list.add(new SelectItemOption<String>(string + "/timeZone", ForumFormatUtils.getTimeZoneNumberInString(string))) ;
		}
		UIFormSelectBoxForum timeZone = new UIFormSelectBoxForum(FIELD_TIMEZONE_SELECTBOX, FIELD_TIMEZONE_SELECTBOX, list) ;
		Date date = new Date() ;
		double timeZoneMyHost = userProfile.getTimeZone() ;
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
		String []format = new String[] {"M-d-yyyy", "M-d-yy", "MM-dd-yy", "MM-dd-yyyy","yyyy-MM-dd", "yy-MM-dd", "dd-MM-yyyy", "dd-MM-yy",
				"M/d/yyyy", "M/d/yy", "MM/dd/yy", "MM/dd/yyyy","yyyy/MM/dd", "yy/MM/dd", "dd/MM/yyyy", "dd/MM/yy"} ;
		for (String frm : format) {
			list.add(new SelectItemOption<String>((frm.toLowerCase() +" ("  + ForumFormatUtils.getFormatDate(frm, date)+")"), frm)) ;
    }
		UIFormSelectBox shortdateFormat = new UIFormSelectBox(FIELD_SHORTDATEFORMAT_SELECTBOX, FIELD_SHORTDATEFORMAT_SELECTBOX, list) ;
		shortdateFormat.setValue(userProfile.getShortDateFormat());
		list = new ArrayList<SelectItemOption<String>>() ;
		format = new String[] {"DDD,MMMM dd,yyyy", "DDDD,MMMM dd,yyyy", "DDDD,dd MMMM,yyyy", "DDD,MMM dd,yyyy", "DDDD,MMM dd,yyyy", "DDDD,dd MMM,yyyy",
				 								"MMMM dd,yyyy", "dd MMMM,yyyy","MMM dd,yyyy", "dd MMM,yyyy"} ;
		for (String idFrm : format) {
			list.add(new SelectItemOption<String>((idFrm.toLowerCase() +" (" + ForumFormatUtils.getFormatDate(idFrm, date)+")"), idFrm.replaceFirst(" ", "="))) ;
		}
		UIFormSelectBox longDateFormat = new UIFormSelectBox(FIELD_LONGDATEFORMAT_SELECTBOX, FIELD_LONGDATEFORMAT_SELECTBOX, list) ;
		longDateFormat.setValue(userProfile.getLongDateFormat());
		list = new ArrayList<SelectItemOption<String>>() ;
		list.add(new SelectItemOption<String>("12-hour ("+ForumFormatUtils.getFormatDate("h:mm a", date)+")", "h:mm=a")) ;
		list.add(new SelectItemOption<String>("12-hour ("+ForumFormatUtils.getFormatDate("hh:mm a", date)+")", "hh:mm=a")) ;
		list.add(new SelectItemOption<String>("24-hour ("+ForumFormatUtils.getFormatDate("H:mm", date)+")", "H:mm")) ;
		list.add(new SelectItemOption<String>("24-hour ("+ForumFormatUtils.getFormatDate("HH:mm", date)+")", "HH:mm")) ;
		UIFormSelectBox timeFormat = new UIFormSelectBox(FIELD_TIMEFORMAT_SELECTBOX, FIELD_TIMEFORMAT_SELECTBOX, list) ;
		timeFormat.setValue(userProfile.getTimeFormat().replace(' ', '='));
		list = new ArrayList<SelectItemOption<String>>() ;
		for(int i=5; i <= 35; i = i + 5) {
			list.add(new SelectItemOption<String>(String.valueOf(i),("id" + i))) ;
		}
		UIFormSelectBox maximumThreads = new UIFormSelectBox(FIELD_MAXTOPICS_SELECTBOX, FIELD_MAXTOPICS_SELECTBOX, list) ;
		maximumThreads.setValue("id" + userProfile.getMaxTopicInPage());
		list = new ArrayList<SelectItemOption<String>>() ;
		for(int i=5; i <= 45; i = i + 5) {
			list.add(new SelectItemOption<String>(String.valueOf(i), ("id" + i))) ;
		}
		UIFormSelectBox maximumPosts = new UIFormSelectBox(FIELD_MAXPOSTS_SELECTBOX, FIELD_MAXPOSTS_SELECTBOX, list) ;
		maximumPosts.setValue("id" + userProfile.getMaxPostInPage());
		boolean isJump = userProfile.getIsShowForumJump() ;
		UIFormCheckBoxInput isShowForumJump = new UIFormCheckBoxInput<Boolean>(FIELD_FORUMJUMP_CHECKBOX, FIELD_FORUMJUMP_CHECKBOX, isJump);
		isShowForumJump.setChecked(isJump) ;
		//Ban
		UIFormCheckBoxInput isBanned = new UIFormCheckBoxInput<Boolean>(FIELD_ISBANNED_CHECKBOX, FIELD_ISBANNED_CHECKBOX, this.userProfile.getIsBanned());
		boolean isBan = userProfile.getIsBanned() ;
		isBanned.setValue(isBan) ;
		UIFormStringInput banUntil = new UIFormStringInput(FIELD_BANUNTIL_INPUT,FIELD_BANUNTIL_INPUT, null) ;
		UIFormTextAreaInput banReason = new UIFormTextAreaInput(FIELD_BANREASON_TEXTAREA, FIELD_BANREASON_TEXTAREA, null);
		UIFormStringInput banCounter = new UIFormStringInput(FIELD_BANCOUNTER_INPUT, FIELD_BANCOUNTER_INPUT, null) ;
		banCounter.setValue(userProfile.getBanCounter() + "");
		UIFormTextAreaInput banReasonSummary = new UIFormTextAreaInput(FIELD_BANREASONSUMMARY_MULTIVALUE, FIELD_BANREASONSUMMARY_MULTIVALUE, null);
		banReasonSummary.setValue(userProfile.getBanReasonSummary().toString());
		UIFormStringInput createdDateBan = new UIFormStringInput(FIELD_CREATEDDATEBAN_INPUT, FIELD_CREATEDDATEBAN_INPUT, null) ;
		if(isBan) {
			banUntil.setValue("" + userProfile.getBanUntil());
			banReason.setValue(userProfile.getBanReason());
			createdDateBan.setValue(ForumFormatUtils.getFormatDate("MM/dd/yyyy, hh:mm a",userProfile.getCreatedDateBan()));
		}
		
		UIFormInputWithActions inputSetProfile = new UIFormInputWithActions(FIELD_USERPROFILE_FORM); 
		inputSetProfile.addUIFormInput(userId);
		inputSetProfile.addUIFormInput(userTitle);
		inputSetProfile.addUIFormInput(userRole);
		inputSetProfile.addUIFormInput(signature);
		inputSetProfile.addUIFormInput(isDisplaySignature);
		inputSetProfile.addUIFormInput(moderateForums);
		inputSetProfile.addUIFormInput(moderateTopics);
		inputSetProfile.addUIFormInput(isDisplayAvatar);
		addUIFormInput(inputSetProfile);
		
		UIFormInputWithActions inputSetOption = new UIFormInputWithActions(FIELD_USEROPTION_FORM); 
		inputSetOption.addUIFormInput(timeZone) ;
		inputSetOption.addUIFormInput(shortdateFormat) ;
		inputSetOption.addUIFormInput(longDateFormat) ;
		inputSetOption.addUIFormInput(timeFormat) ;
		inputSetOption.addUIFormInput(maximumThreads) ;
		inputSetOption.addUIFormInput(maximumPosts) ;
		inputSetOption.addUIFormInput(isShowForumJump) ;
		addUIFormInput(inputSetOption);
		
		UIFormInputWithActions inputSetBan = new UIFormInputWithActions(FIELD_USERBAN_FORM); 
		inputSetBan.addUIFormInput(isBanned);
		inputSetBan.addUIFormInput(banUntil);
		inputSetBan.addUIFormInput(banReason);
		inputSetBan.addUIFormInput(banCounter);
		inputSetBan.addUIFormInput(banReasonSummary);
		inputSetBan.addUIFormInput(createdDateBan);
		addUIFormInput(inputSetBan);
		
	}
	
	public void activate() throws Exception {
	}

	public void deActivate() throws Exception {
	  // TODO Auto-generated method stub
  }
	
	static  public class SaveActionListener extends EventListener<UIUserProfileForm> {
    public void execute(Event<UIUserProfileForm> event) throws Exception {
    	UIUserProfileForm uiForm = event.getSource() ;
    	UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
			popupContainer.getChild(UIPopupAction.class).deActivate() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
    }
  }
	
	static  public class CancelActionListener extends EventListener<UIUserProfileForm> {
    public void execute(Event<UIUserProfileForm> event) throws Exception {
    	UIUserProfileForm uiForm = event.getSource() ;
    	UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
			popupContainer.getChild(UIPopupAction.class).deActivate() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
    }
  }
	
	
	
	
}
