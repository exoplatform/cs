/***************************************************************************
 * Copyright 2001-2008 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.service.impl;

import java.util.Locale;
import java.util.TimeZone;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupEventListener;

/**
 * Author : Huu-Dung Kieu huu-dung.kieu@bull.be 14 févr. 08
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
  
  /**
   * 
   * @param calendarService Calendar service geeting from the Portlet Container
   * @param params  parameters defined in the plugin configuration
   */
  public NewGroupListener(CalendarService calendarService, InitParams params) {
    
    calendarService_ = calendarService;
    
    defaultCalendarName = params.getValueParam("defaultCalendarName").getValue() ;
    defaultCalendarDescription = params.getValueParam("defaultCalendarDescription").getValue() ;
  }
  
  public void postSave(Group group, boolean isNew) throws Exception { 
    if (!isNew)
      return;
    
    String groupId = group.getId();
    
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    boolean isPublic = true;

    Locale locale = Locale.getDefault();
    TimeZone timezone = TimeZone.getDefault();
    
    Calendar calendar = new Calendar() ;
    calendar.setName(defaultCalendarName) ;
    calendar.setDescription(defaultCalendarDescription) ;
    calendar.setGroups(new String[]{groupId}) ;
    calendar.setPublic(isPublic) ;
    calendar.setLocale(locale.getDisplayName()) ;
    calendar.setTimeZone(timezone.getDisplayName()) ;
    calendar.setCalendarColor(Calendar.SEASHELL);

    calendarService_.savePublicCalendar(sProvider, calendar, isNew, null) ;
  }
}
