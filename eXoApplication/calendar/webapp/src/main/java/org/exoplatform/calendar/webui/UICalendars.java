/**
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
 **/
package org.exoplatform.calendar.webui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.Colors;
import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.calendar.webui.popup.UIAddEditPermission;
import org.exoplatform.calendar.webui.popup.UICalDavForm;
import org.exoplatform.calendar.webui.popup.UICalendarCategoryForm;
import org.exoplatform.calendar.webui.popup.UICalendarCategoryManager;
import org.exoplatform.calendar.webui.popup.UICalendarForm;
import org.exoplatform.calendar.webui.popup.UICalendarSettingForm;
import org.exoplatform.calendar.webui.popup.UIEventCategoryManager;
import org.exoplatform.calendar.webui.popup.UIExportForm;
import org.exoplatform.calendar.webui.popup.UIImportForm;
import org.exoplatform.calendar.webui.popup.UIPopupAction;
import org.exoplatform.calendar.webui.popup.UIPopupContainer;
import org.exoplatform.calendar.webui.popup.UIQuickAddEvent;
import org.exoplatform.calendar.webui.popup.UIRssForm;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.command.handler.GetApplicationHandler;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template =  "app:/templates/calendar/webui/UICalendars.gtmpl",
		events = {
			@EventConfig(listeners = UICalendars.AddCalendarActionListener.class),
			@EventConfig(listeners = UICalendars.AddEventCategoryActionListener.class),
			@EventConfig(listeners = UICalendars.EditGroupActionListener.class),
			@EventConfig(phase=Phase.DECODE, listeners = UICalendars.DeleteGroupActionListener.class, confirm="UICalendars.msg.confirm-delete-group"), 
			@EventConfig(listeners = UICalendars.ExportCalendarActionListener.class), 
			@EventConfig(listeners = UICalendars.ExportCalendarsActionListener.class), 
			@EventConfig(listeners = UICalendars.ImportCalendarActionListener.class),
			@EventConfig(listeners = UICalendars.GenerateRssActionListener.class), 
			@EventConfig(listeners = UICalendars.GenerateCalDavActionListener.class), 
			@EventConfig(listeners = UICalendars.AddEventActionListener.class),
			@EventConfig(listeners = UICalendars.AddTaskActionListener.class),
			@EventConfig(listeners = UICalendars.EditCalendarActionListener.class),
			@EventConfig(phase=Phase.DECODE, listeners = UICalendars.RemoveCalendarActionListener.class, confirm="UICalendars.msg.confirm-delete-calendar"),
			@EventConfig(listeners = UICalendars.AddCalendarCategoryActionListener.class),
			@EventConfig(listeners = UICalendars.ShareCalendarActionListener.class),
			@EventConfig(listeners = UICalendars.ChangeColorActionListener.class),
			@EventConfig(listeners = UICalendars.CalendarSettingActionListener.class)
		}
)

public class UICalendars extends UIForm  {
	public static String CALENDARID = "calendarid".intern() ;
	public static String CALTYPE = "calType".intern() ;
	public static String CALNAME = "calName".intern() ;
	public static String CALCOLOR = "calColor".intern() ;
	public static String CURRENTTIME = "ct".intern() ;
	public static String TIMEZONE = "tz".intern() ;

	private String[] publicCalendarIds = {} ;
	private LinkedHashMap<String, String> colorMap_ = new LinkedHashMap<String, String>() ;

	public UICalendars() throws Exception {

	} 

	public String[] getPublicCalendarIds(){ return publicCalendarIds ; }
	private SessionProvider getSession() {
		return SessionProviderFactory.createSessionProvider() ;
	}
	private SessionProvider getSystemSession() {
		return SessionProviderFactory.createSystemProvider() ;
	}

	public void checkAll() {
		for(UIComponent cpm : getChildren())
			getUIFormCheckBoxInput(cpm.getId()).setChecked(true) ; 
	}

	protected List<GroupCalendarData> getPrivateCalendars() throws Exception{
		CalendarService calendarService = CalendarUtils.getCalendarService() ;
		String username = Util.getPortalRequestContext().getRemoteUser() ;
		//List<CalendarCategory> categories = calendarService.getCategories(SessionsUtils.getSessionProvider(), username) ;
		List<GroupCalendarData> groupCalendars = calendarService.getCalendarCategories(getSession(), username, false) ;
		for(GroupCalendarData group : groupCalendars) {
			List<Calendar> calendars = group.getCalendars() ;
			if(calendars != null) {
				for(Calendar calendar : calendars) {
					colorMap_.put(Calendar.TYPE_PRIVATE + CalendarUtils.COLON + calendar.getId(), calendar.getCalendarColor()) ;
					if(getUIFormCheckBoxInput(calendar.getId()) == null){
						addUIFormInput(new UIFormCheckBoxInput<Boolean>(calendar.getId(), calendar.getId(), false).setChecked(true)) ;
					} else {
						//TODO wait for dunghm about javaScript
						//getUIFormCheckBoxInput(calendar.getId()).setChecked(true) ;
					}
				}
			}
		}
		return groupCalendars;
	}

	protected List<GroupCalendarData> getPublicCalendars() throws Exception{
		String username = Util.getPortalRequestContext().getRemoteUser() ;
		String[] groups = CalendarUtils.getUserGroups(username) ;
		CalendarService calendarService = CalendarUtils.getCalendarService() ;
		List<GroupCalendarData> groupCalendars = calendarService.getGroupCalendars(getSystemSession(), groups, false, username) ;
		Map<String, String> map = new HashMap<String, String> () ;    
		for(GroupCalendarData group : groupCalendars) {
			List<Calendar> calendars = group.getCalendars() ;
			for(Calendar calendar : calendars) {
				map.put(calendar.getId(), calendar.getId()) ;
				colorMap_.put(Calendar.TYPE_PUBLIC + CalendarUtils.COLON + calendar.getId(), calendar.getCalendarColor()) ;
				if(getUIFormCheckBoxInput(calendar.getId()) == null){
					addUIFormInput(new UIFormCheckBoxInput<Boolean>(calendar.getId(), calendar.getId(), false).setChecked(true)) ;
				}
			}
		}
		publicCalendarIds = map.values().toArray(new String[]{}) ;
		return groupCalendars ;
	}

	protected GroupCalendarData getSharedCalendars() throws Exception{
		CalendarService calendarService = CalendarUtils.getCalendarService() ;
		GroupCalendarData groupCalendars = calendarService.getSharedCalendars(getSystemSession(), CalendarUtils.getCurrentUser(), false) ;
		CalendarSetting setting = calendarService.getCalendarSetting(getSession(), CalendarUtils.getCurrentUser()) ;
		Map<String, String> map = new HashMap<String, String>() ;
		for(String key : setting.getSharedCalendarsColors()) {
			map.put(key.split(":")[0], key.split(":")[1]) ;
		}
		if(groupCalendars != null) {
			List<Calendar> calendars = groupCalendars.getCalendars() ;
			for(Calendar calendar : calendars) {
				String color = map.get(calendar.getId()) ;
				if(color == null) color = calendar.getCalendarColor() ;
				colorMap_.put(Calendar.TYPE_SHARED + CalendarUtils.COLON + calendar.getId(), color) ;
				if(getUIFormCheckBoxInput(calendar.getId()) == null){
					addUIFormInput(new UIFormCheckBoxInput<Boolean>(calendar.getId(), calendar.getId(), false).setChecked(true)) ;
				}
			}
		}
		return groupCalendars ;
	}

	public LinkedHashMap<String, String> getColorMap() {
		return colorMap_;
	}
	public String[] getColors() {
		return Colors.COLORNAME ;
	}
	static  public class AddCalendarActionListener extends EventListener<UICalendars> {
		public void execute(Event<UICalendars> event) throws Exception {
			UICalendars uiComponent = event.getSource() ;
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			String clientTime = event.getRequestContext().getRequestParameter(CURRENTTIME) ;
			java.util.Calendar cal = new GregorianCalendar() ;
			try {
				cal.setTimeInMillis(Long.parseLong(clientTime)) ;
			} catch (Exception e) {
				System.out.println("invalid clientTime");
			}
			TimeZone timeZone = cal.getTimeZone() ;
			UICalendarPortlet uiCalendarPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
			UIPopupAction popupAction = uiCalendarPortlet.getChild(UIPopupAction.class) ;
			popupAction.deActivate() ;
			UIPopupContainer uiPopupContainer = popupAction.activate(UIPopupContainer.class, 600) ;
			uiPopupContainer.setId(UIPopupContainer.UICALENDARPOPUP) ;
			UICalendarForm calendarForm = uiPopupContainer.addChild(UICalendarForm.class, null, null) ;
			calendarForm.setTimeZone(timeZone.getID()) ;
			calendarForm.setSelectedGroup(categoryId) ;
			//popupAction.activate(uiPopupContainer, 600, 0, true) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			//event.getRequestContext().addUIComponentToUpdateByAjax(uiComponent.getParent()) ;
		}
	}
	static  public class AddEventCategoryActionListener extends EventListener<UICalendars> {
		public void execute(Event<UICalendars> event) throws Exception {
			UICalendars uiCalendars = event.getSource() ;
			UICalendarPortlet calendarPortlet = uiCalendars.getAncestorOfType(UICalendarPortlet.class) ;
			UIPopupAction popupAction = calendarPortlet.getChild(UIPopupAction.class) ;
			popupAction.deActivate() ;
			popupAction.activate(UIEventCategoryManager.class, 470) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	static  public class EditGroupActionListener extends EventListener<UICalendars> {
		public void execute(Event<UICalendars> event) throws Exception {
			UICalendars uiComponent = event.getSource() ;
			UICalendarPortlet uiCalendarPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
			UIPopupAction popupAction = uiCalendarPortlet.getChild(UIPopupAction.class) ;
			popupAction.deActivate() ;
			UICalendarCategoryManager uiManager = popupAction.activate(UICalendarCategoryManager.class, 470) ;
			UICalendarCategoryForm uiForm = uiManager.getChild(UICalendarCategoryForm.class) ;
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			uiForm.init(categoryId) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			//event.getRequestContext().addUIComponentToUpdateByAjax(uiComponent.getParent()) ;
		}
	}
	static  public class DeleteGroupActionListener extends EventListener<UICalendars> {
		public void execute(Event<UICalendars> event) throws Exception {
			UICalendars uiComponent = event.getSource() ;
			String calendarCategoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UICalendarPortlet uiPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
			uiPortlet.cancelAction() ;
			CalendarService calService = uiComponent.getApplicationComponent(CalendarService.class) ;
			String username = event.getRequestContext().getRemoteUser() ;
			calService.removeCalendarCategory(uiComponent.getSession(), username, calendarCategoryId) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(uiComponent) ; 
			UICalendarWorkingContainer uiWorkingContainer = uiPortlet.findFirstComponentOfType(UICalendarWorkingContainer.class) ;
			UICalendarViewContainer uiViewContainer = uiPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
			uiViewContainer.refresh() ;
			try{       
				UIMiniCalendar uiMiniCalendar = uiPortlet.findFirstComponentOfType(UIMiniCalendar.class) ;
				uiMiniCalendar.updateMiniCal() ;
			} catch (Exception e) {
				e.printStackTrace() ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(uiWorkingContainer) ;
		}
	}

	private boolean canAddTaskAndEvent(UICalendars uiComponent, String calendarId, String calType) throws Exception {
		CalendarService calService = CalendarUtils.getCalendarService() ;
		Calendar calendar = null;
		String currentUser = CalendarUtils.getCurrentUser() ;
		if(calType.equals(CalendarUtils.SHARED_TYPE)) {
			calendar = calService.getSharedCalendars(getSystemSession(), currentUser, true).getCalendarById(calendarId) ;
			return calendar.getEditPermission()!=null && CalendarUtils.canEdit(null, calendar.getEditPermission(), currentUser) ;
		} else if(calType.equals(CalendarUtils.PUBLIC_TYPE)) {
			calendar = calService.getGroupCalendar(getSystemSession(), calendarId) ;
			return calendar.getEditPermission()!=null && CalendarUtils.canEdit(uiComponent.getApplicationComponent(OrganizationService.class), calendar.getEditPermission(), currentUser) ;
		}  
		return false ;
	}
	public boolean canEdit(String[] savePerms) throws Exception{
		return CalendarUtils.canEdit(CalendarUtils.getOrganizationService(), savePerms, CalendarUtils.getCurrentUser()) ;
	}
	static  public class AddEventActionListener extends EventListener<UICalendars> {
		public void execute(Event<UICalendars> event) throws Exception {
			UICalendars uiComponent = event.getSource() ;
			String calendarId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			//String calendarName = event.getRequestContext().getRequestParameter(CALNAME) ;
			String calType = event.getRequestContext().getRequestParameter(CALTYPE) ;

			if(!calType.equals(CalendarUtils.PRIVATE_TYPE)) {
				if(!uiComponent.canAddTaskAndEvent(uiComponent, calendarId, calType)) {
					UIApplication uiApp = uiComponent.getAncestorOfType(UIApplication.class) ;
					uiApp.addMessage(new ApplicationMessage("UICalendars.msg.have-no-permission-to-edit", null, 1)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
					return;
				}
			}

			String clientTime = event.getRequestContext().getRequestParameter(CURRENTTIME) ;
			String timeZone = event.getRequestContext().getRequestParameter(TIMEZONE) ;
			String categoryId = event.getRequestContext().getRequestParameter("categoryId") ;
			UICalendarPortlet uiCalendarPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
			UIPopupAction popupAction = uiCalendarPortlet.getChild(UIPopupAction.class) ;
			popupAction.deActivate() ;
			UIQuickAddEvent uiQuickAddEvent = popupAction.activate(UIQuickAddEvent.class, 600) ;
			uiQuickAddEvent.setEvent(true) ;  
			uiQuickAddEvent.setId("UIQuickAddEvent") ;
			/*if(calType.equals(CalendarUtils.PRIVATE_TYPE)) {
        options = null ;
      } else if(calType.equals(CalendarUtils.SHARED_TYPE)) {
        GroupCalendarData calendarData = uiComponent.getSharedCalendars() ;
        for(Calendar cal : calendarData.getCalendars()) {
          options.add(new SelectItemOption<String>(cal.getName(), cal.getId())) ;
        }
      } else if(calType.equals(CalendarUtils.PUBLIC_TYPE)) {
        for (GroupCalendarData calendarData : uiComponent.getPublicCalendars()) {
          for(Calendar cal : calendarData.getCalendars()) {
            options.add(new SelectItemOption<String>(cal.getName(), cal.getId())) ;
          }
        }
      }    */
			uiQuickAddEvent.update(calType, null) ;
			uiQuickAddEvent.setSelectedCalendar(calendarId) ;
			uiQuickAddEvent.init(uiCalendarPortlet.getCalendarSetting(), clientTime, null) ;
			if(categoryId != null && categoryId.trim().length() >0 && !categoryId.toLowerCase().equals("null")) {
				uiQuickAddEvent.setSelectedCategory(categoryId) ;
			} else {
				uiQuickAddEvent.setSelectedCategory("Meeting") ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			//event.getRequestContext().addUIComponentToUpdateByAjax(uiComponent) ;
		}
	}

	static  public class AddTaskActionListener extends EventListener<UICalendars> {
		public void execute(Event<UICalendars> event) throws Exception {
			UICalendars uiComponent = event.getSource() ;
			String calendarId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			//String calendarName = event.getRequestContext().getRequestParameter(CALNAME) ;
			String clientTime = event.getRequestContext().getRequestParameter(CURRENTTIME) ;
			String timeZone = event.getRequestContext().getRequestParameter(TIMEZONE) ;
			String calType = event.getRequestContext().getRequestParameter(CALTYPE) ;
			String categoryId = event.getRequestContext().getRequestParameter("categoryId") ;

			if(!calType.equals(CalendarUtils.PRIVATE_TYPE)) {
				if(!uiComponent.canAddTaskAndEvent(uiComponent, calendarId, calType)) {
					UIApplication uiApp = uiComponent.getAncestorOfType(UIApplication.class) ;
					uiApp.addMessage(new ApplicationMessage("UICalendars.msg.have-no-permission-to-edit", null, 1)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
					return;
				}
			}

			UICalendarPortlet uiCalendarPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
			UIPopupAction popupAction = uiCalendarPortlet.getChild(UIPopupAction.class) ;
			popupAction.deActivate() ;
			UIQuickAddEvent uiQuickAddEvent = popupAction.activate(UIQuickAddEvent.class, 600) ;
			uiQuickAddEvent.setEvent(false) ;  
			uiQuickAddEvent.setId("UIQuickAddTask") ;
			uiQuickAddEvent.init(uiCalendarPortlet.getCalendarSetting(), clientTime, null) ;
			/*if(calType.equals(CalendarUtils.PRIVATE_TYPE)) {
        options = null ;
      } else if(calType.equals(CalendarUtils.SHARED_TYPE)) {
        GroupCalendarData calendarData = uiComponent.getSharedCalendars() ;
        for(Calendar cal : calendarData.getCalendars()) {
          options.add(new SelectItemOption<String>(cal.getName(), cal.getId())) ;
        }
      } else if(calType.equals(CalendarUtils.PUBLIC_TYPE)) {
        for (GroupCalendarData calendarData : uiComponent.getPublicCalendars()) {
          for(Calendar cal : calendarData.getCalendars()) {
            options.add(new SelectItemOption<String>(cal.getName(), cal.getId())) ;
          }
        }
      }    */
			uiQuickAddEvent.update(calType, null) ;
			uiQuickAddEvent.setSelectedCalendar(calendarId) ;
			if(categoryId != null && categoryId.trim().length() >0 && !categoryId.toLowerCase().equals("null")) {
				uiQuickAddEvent.setSelectedCategory(categoryId) ;
			} else {
				uiQuickAddEvent.setSelectedCategory("Meeting") ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			//event.getRequestContext().addUIComponentToUpdateByAjax(uiComponent.getParent()) ;
		}
	}

	static  public class EditCalendarActionListener extends EventListener<UICalendars> {
		public void execute(Event<UICalendars> event) throws Exception {
			UICalendars uiComponent = event.getSource() ;
			String calendarId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			String username = event.getRequestContext().getRemoteUser() ;
			String calType = event.getRequestContext().getRequestParameter(CALTYPE) ;
			UICalendarPortlet uiCalendarPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
			UIPopupAction popupAction = uiCalendarPortlet.getChild(UIPopupAction.class) ;
			popupAction.deActivate() ;
			UIPopupContainer uiPopupContainer = uiCalendarPortlet.createUIComponent(UIPopupContainer.class, null, null) ;
			uiPopupContainer.setId(UIPopupContainer.UICALENDARPOPUP) ;
			UICalendarForm uiCalendarForm = uiPopupContainer.addChild(UICalendarForm.class, null, null) ;
			CalendarService calService = uiCalendarForm.getApplicationComponent(CalendarService.class) ;
			Calendar calendar = null ;
			uiCalendarForm.calType_ = calType ;
			if(CalendarUtils.PRIVATE_TYPE.equals(calType)) { 
				calendar = calService.getUserCalendar(uiComponent.getSession(), username, calendarId) ;
			} else if(CalendarUtils.SHARED_TYPE.equals(calType)) {
				Iterator iter = calService.getSharedCalendars(uiComponent.getSystemSession(), username, true).getCalendars().iterator() ;
				while (iter.hasNext()) {
					Calendar cal = ((Calendar)iter.next()) ;
					if(cal.getId().equals(calendarId)) {
						calendar = cal ;
						break ;
					}
				}
			} else if (CalendarUtils.PUBLIC_TYPE.equals(calType)) {
				calendar = calService.getGroupCalendar(uiComponent.getSystemSession(), calendarId) ;
			}
			if(calendar != null) {
				uiCalendarForm.init(calendar) ;
				popupAction.activate(uiPopupContainer, 600, 0, true) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
				//event.getRequestContext().addUIComponentToUpdateByAjax(uiComponent.getParent()) ;
			}	
		}
	}
	static  public class RemoveCalendarActionListener extends EventListener<UICalendars> {
		public void execute(Event<UICalendars> event) throws Exception {
			UICalendars uiComponent = event.getSource() ;
			String username = event.getRequestContext().getRemoteUser() ;
			String calendarId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			String calType = event.getRequestContext().getRequestParameter(CALTYPE) ;
			CalendarService calService = CalendarUtils.getCalendarService() ;
			if(calType.equals(CalendarUtils.PRIVATE_TYPE)) {
				calService.removeUserCalendar(uiComponent.getSession(), username, calendarId) ;
			}else if(calType.equals(CalendarUtils.SHARED_TYPE)) {
				calService.removeSharedCalendar(uiComponent.getSystemSession(), username, calendarId) ;
			}else if(calType.equals(CalendarUtils.PUBLIC_TYPE)) {
				boolean canEdit = false ;
				OrganizationService oService = uiComponent.getApplicationComponent(OrganizationService.class) ;
				for(GroupCalendarData groupCal : uiComponent.getPublicCalendars()) {
					for(Calendar cal : groupCal.getCalendars()) {
						if(cal.getId().equals(calendarId)) {
							canEdit = CalendarUtils.canEdit(oService, groupCal.getCalendarById(calendarId).getEditPermission(), username) ;
							break ;
						}
					}
				}
				if(canEdit) {
					calService.removePublicCalendar(uiComponent.getSystemSession(), calendarId) ;
				} else {
					UIApplication uiApp = uiComponent.getAncestorOfType(UIApplication.class) ;
					uiApp.addMessage(new ApplicationMessage("UICalendarView.msg.have-no-delete-permission", null)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
					return ;
				}
			}
			UICalendarPortlet uiPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
			uiPortlet.cancelAction() ;
			UIMiniCalendar uiMiniCalendar = uiPortlet.findFirstComponentOfType(UIMiniCalendar.class) ;
			UICalendarViewContainer uiViewContainer = uiPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
			UICalendarWorkingContainer workingContainer = uiComponent.getAncestorOfType(UICalendarWorkingContainer.class) ;
			CalendarSetting setting = calService.getCalendarSetting(uiComponent.getSession(), username) ;
			uiMiniCalendar.updateMiniCal() ;
			uiViewContainer.refresh() ;
			uiPortlet.setCalendarSetting(setting) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
		}
	}

	static  public class AddCalendarCategoryActionListener extends EventListener<UICalendars> {
		public void execute(Event<UICalendars> event) throws Exception {
			UICalendars uiComponent = event.getSource() ;
			UICalendarPortlet uiCalendarPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
			UIPopupAction popupAction = uiCalendarPortlet.getChild(UIPopupAction.class) ;
			popupAction.deActivate() ;
			popupAction.activate(UICalendarCategoryManager.class, 470) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			//event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendarPortlet) ;
		}
	}

	static  public class ExportCalendarActionListener extends EventListener<UICalendars> {
		public void execute(Event<UICalendars> event) throws Exception {
			UICalendars uiComponent = event.getSource() ;
			String selectedCalendarId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			String calType = event.getRequestContext().getRequestParameter(CALTYPE) ;
			UICalendarPortlet uiCalendarPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
			UIPopupAction popupAction = uiCalendarPortlet.getChild(UIPopupAction.class) ;
			popupAction.deActivate() ;
			UIExportForm exportForm = popupAction.activate(UIExportForm.class, 500) ;
			exportForm.update(calType, selectedCalendarId) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			//event.getRequestContext().addUIComponentToUpdateByAjax(uiComponent.getParent()) ;
		}
	}
	static  public class ExportCalendarsActionListener extends EventListener<UICalendars> {
		public void execute(Event<UICalendars> event) throws Exception {
			UICalendars uiComponent = event.getSource() ;
			String groupId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UICalendarPortlet uiCalendarPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
			UIPopupAction popupAction = uiCalendarPortlet.getChild(UIPopupAction.class) ;
			popupAction.deActivate() ;
			UIExportForm exportForm = popupAction.activate(UIExportForm.class, 500) ;
			String username = event.getRequestContext().getRemoteUser() ;
			exportForm.initCheckBox(CalendarUtils.getCalendarService().getUserCalendarsByCategory(uiComponent.getSession(), username, groupId), null) ;
			//exportForm.addUIFormInput(arg0)
			//exportForm.update("0", groupId) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			// event.getRequestContext().addUIComponentToUpdateByAjax(uiComponent.getParent()) ;
		}
	}
	static  public class ImportCalendarActionListener extends EventListener<UICalendars> {
		public void execute(Event<UICalendars> event) throws Exception {
			UICalendars uiComponent = event.getSource() ;
			UICalendarPortlet uiCalendarPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
			UIPopupAction popupAction = uiCalendarPortlet.getChild(UIPopupAction.class) ;
			popupAction.deActivate() ;
			popupAction.activate(UIImportForm.class, 600) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			//event.getRequestContext().addUIComponentToUpdateByAjax(uiComponent.getParent()) ;
		}
	}

	static  public class GenerateRssActionListener extends EventListener<UICalendars> {
		public void execute(Event<UICalendars> event) throws Exception {
			UICalendars uiComponent = event.getSource() ;
			UICalendarPortlet uiCalendarPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
			UIPopupAction popupAction = uiCalendarPortlet.getChild(UIPopupAction.class) ;
			popupAction.deActivate() ;
			UIRssForm uiRssForm = popupAction.activate(UIRssForm.class, 600) ;
			uiRssForm.init() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			//event.getRequestContext().addUIComponentToUpdateByAjax(uiComponent.getParent()) ;
		}
	}
	static  public class GenerateCalDavActionListener extends EventListener<UICalendars> {
		public void execute(Event<UICalendars> event) throws Exception {
			UICalendars uiComponent = event.getSource() ;
			UICalendarPortlet uiCalendarPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
			UIPopupAction popupAction = uiCalendarPortlet.getChild(UIPopupAction.class) ;
			popupAction.deActivate() ;
			UICalDavForm uiCalDavForm = popupAction.activate(UICalDavForm.class, 600) ;
			uiCalDavForm.init() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			//event.getRequestContext().addUIComponentToUpdateByAjax(uiComponent.getParent()) ;
		}
	}
	static  public class ShareCalendarActionListener extends EventListener<UICalendars> {
		public void execute(Event<UICalendars> event) throws Exception {
			UICalendars uiComponent = event.getSource() ;
			String selectedCalendarId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UICalendarPortlet uiCalendarPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
			UIPopupAction popupAction = uiCalendarPortlet.getChild(UIPopupAction.class) ;
			popupAction.deActivate() ;
			UIPopupContainer uiPopupContainer = popupAction.activate(UIPopupContainer.class, 400) ;
			uiPopupContainer.setId("UIPermissionSelectPopup") ;
			//UISharedForm uiSharedForm = uiPopupContainer.addChild(UISharedForm.class, null, null) ;
			UIAddEditPermission uiAddNewEditPermission = uiPopupContainer.addChild(UIAddEditPermission.class, null, null);
			CalendarService calService = CalendarUtils.getCalendarService() ;
			String username = event.getRequestContext().getRemoteUser() ;
			Calendar cal = calService.getUserCalendar(uiComponent.getSession(), username, selectedCalendarId) ;
			uiAddNewEditPermission.init(null, cal, true) ;
			//event.getRequestContext().addUIComponentToUpdateByAjax(uiCalendarPortlet) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			//event.getRequestContext().addUIComponentToUpdateByAjax(uiComponent.getParent()) ;
		}
	}
	static  public class ChangeColorActionListener extends EventListener<UICalendars> {
		public void execute(Event<UICalendars> event) throws Exception {
			System.out.println("\n\n ChangeColorActionListener");
			UICalendars uiComponent = event.getSource() ;
			uiComponent.getAncestorOfType(UICalendarPortlet.class).cancelAction() ;
			String calendarId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			String color = event.getRequestContext().getRequestParameter(CALCOLOR) ;
			String calType = event.getRequestContext().getRequestParameter(CALTYPE) ;
			CalendarService calService = CalendarUtils.getCalendarService() ;
			SessionProvider session = uiComponent.getSession() ;
			SessionProvider systemSession = uiComponent.getSystemSession() ;
			String username = event.getRequestContext().getRemoteUser() ;
			try{
				Calendar calendar = null ;
				if(CalendarUtils.PRIVATE_TYPE.equals(calType)) {
					calendar = calService.getUserCalendar(session, username, calendarId) ;
					calendar.setCalendarColor(color) ;
					calService.saveUserCalendar(session, username, calendar, false) ;
				} else if(CalendarUtils.SHARED_TYPE.equals(calType)){
					Iterator iter = calService.getSharedCalendars(systemSession, username, true).getCalendars().iterator() ;
					while (iter.hasNext()) {
						Calendar cal = ((Calendar)iter.next()) ;
						if(cal.getId().equals(calendarId)) {
							calendar = cal ;
							break ;
						}
					}
					calendar.setCalendarColor(color) ;
					calService.saveSharedCalendar(systemSession, username, calendar) ;
					CalendarSetting setting = calService.getCalendarSetting(session, username) ;
					uiComponent.getAncestorOfType(UICalendarPortlet.class).setCalendarSetting(setting) ;
					//calService.save UserCalendar(SessionsUtils.getSessionProvider(), username, calendar, false) ;
				} else if(CalendarUtils.PUBLIC_TYPE.equals(calType)){
					calendar = calService.getGroupCalendar(systemSession, calendarId) ;
					if(!CalendarUtils.canEdit(uiComponent.getApplicationComponent(OrganizationService.class), calendar.getEditPermission(), username)){
						UIApplication uiApp = uiComponent.getAncestorOfType(UIApplication.class) ;
						uiApp.addMessage(new ApplicationMessage("UICalendars.msg.have-no-permission-to-edit", null, ApplicationMessage.WARNING)) ;
						event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
						return ;
					}
					calendar.setCalendarColor(color) ;
					calService.savePublicCalendar(systemSession, calendar, false, username) ;
				}
			} catch (Exception e) {
				e.printStackTrace() ;
			}
			//uiComponent.colorMap_.put(calendarId, color) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(uiComponent.getAncestorOfType(UICalendarPortlet.class)) ;
			//event.getRequestContext().addUIComponentToUpdateByAjax(uiComponent) ;
		}
	}
	static  public class CalendarSettingActionListener extends EventListener<UICalendars> {
		public void execute(Event<UICalendars> event) throws Exception {
			UICalendars uiComponent = event.getSource() ;
			UICalendarPortlet uiCalendarPortlet = uiComponent.getAncestorOfType(UICalendarPortlet.class) ;
			UIPopupAction popupAction = uiCalendarPortlet.getChild(UIPopupAction.class) ;
			popupAction.deActivate() ;
			UICalendarSettingForm uiCalendarSettingForm = popupAction.activate(UICalendarSettingForm.class, 600) ;
			CalendarService cservice = CalendarUtils.getCalendarService() ;
			//String username = Util.getPortalRequestContext().getRemoteUser() ;
			CalendarSetting calendarSetting = uiComponent.getAncestorOfType(UICalendarPortlet.class).getCalendarSetting() ; 
			// = cservice.getCalendarSetting(uiComponent.getSession(), username) ;
			uiCalendarSettingForm.init(calendarSetting, cservice) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			//event.getRequestContext().addUIComponentToUpdateByAjax(uiComponent.getParent()) ;
		}
	}
}
