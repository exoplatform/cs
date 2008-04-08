/***************************************************************************
 * Copyright 2001-2008 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.service.impl;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupEventListener;

/**
 * Author : Huu-Dung Kieu huu-dung.kieu@bull.be 14 f�vr. 08
 * 
 * This is a plugin running every time a new group is create.
 * The goal is to create a default calendar for each group.
 * The plugin configuration is defined in the portal/conf/cs/cs-plugin-configuration.xml file. 
 *
 */
public class NewGroupListener extends GroupEventListener {

	protected CalendarService calendarService_;

	private String defaultCalendarName;
	private String defaultCalendarDescription;
	private String defaultLocale ;
	private String defaultTimeZone ;
	private String[] editPermission ; ;
	private String[] viewPermission ;
	/**
	 * 
	 * @param calendarService Calendar service geeting from the Portlet Container
	 * @param params  parameters defined in the plugin configuration
	 */
	public NewGroupListener(CalendarService calendarService, InitParams params) {

		calendarService_ = calendarService;

		if(params.getValueParam("defaultEditPermission") != null)
			editPermission = params.getValueParam("defaultEditPermission").getValue().split(",") ;
		if(params.getValueParam("defaultViewPermission") != null)
			viewPermission = params.getValueParam("defaultViewPermission").getValue().split(",") ;
		if(params.getValueParam("defaultCalendarDescription") != null)
			defaultCalendarDescription = params.getValueParam("defaultCalendarDescription").getValue() ;
		if(params.getValueParam("defaultLocale") != null) defaultLocale = params.getValueParam("defaultLocale").getValue() ;
		if(params.getValueParam("defaultTimeZone") != null) defaultTimeZone = params.getValueParam("defaultTimeZone").getValue() ;
	}

	public void postSave(Group group, boolean isNew) throws Exception { 
		if (!isNew)
			return;
		String groupId = group.getId();
		SessionProvider sProvider = SessionProvider.createSystemProvider();
		boolean isPublic = true;
		Calendar calendar = new Calendar() ;
		calendar.setName(group.getGroupName()+" calendar") ;
		if(defaultCalendarDescription != null)
			calendar.setDescription(defaultCalendarDescription) ;
		calendar.setGroups(new String[]{groupId}) ;
		calendar.setPublic(isPublic) ;
		if(defaultLocale != null) calendar.setLocale(defaultLocale) ;
		if(defaultTimeZone != null) calendar.setTimeZone(defaultTimeZone) ;
		calendar.setCalendarColor(Calendar.SEASHELL);
		List<String> perms = new ArrayList<String>() ;
		for(String s : viewPermission) {
			if(s.split(":").length > 0) perms.add(s.split(":")[0].trim() + ":" + groupId) ;
		}
		calendar.setViewPermission(perms.toArray(new String[perms.size()])) ;
		perms.clear() ;
		for(String s : editPermission) {
			if(s.split(":").length > 0) perms.add(s.split(":")[0].trim() + ":" + groupId) ;
		}
		calendar.setEditPermission(perms.toArray(new String[perms.size()])) ;
		calendarService_.savePublicCalendar(sProvider, calendar, isNew, null) ;

//		calendarService_.saveCalendarSetting(sProvider, username, new CalendarSetting());
	}
}
