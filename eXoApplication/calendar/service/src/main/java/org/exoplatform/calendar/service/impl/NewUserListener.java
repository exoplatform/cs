/*
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
 */
package org.exoplatform.calendar.service.impl;

import java.util.List;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarCategory;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValuesParam;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;

/**
 * Created by The eXo Platform SARL Author : Hung Nguyen Quang
 * hung.nguyen@exoplatform.com Nov 23, 2007 3:09:21 PM
 */
public class NewUserListener extends UserEventListener {

  //Calendar params
  //final public static String CALENDAR_CATEGORIES = "defaultCalendarCategory".intern() ;
  //final public static String CALENDAR_NAME  = "defaultCalendar".intern() ;
  final public static String COMA = ",".intern() ;

  //Calendar Setting params
  final public static String ST_VIEW_TYPE = "viewType".intern() ;
  final public static String ST_TIME_INTEVAL = "timeInterval".intern() ;
  final public static String ST_WEEK_START = "weekStartOn".intern() ;
  final public static String ST_DATE_FORMAT = "dateFormat".intern() ;
  final public static String ST_TIME_FORMAT = "timeFormat".intern() ;
  final public static String ST_LOCALE = "localeId".intern() ;
  final public static String ST_TIMEZONE = "timezoneId".intern() ;
  final public static String ST_BASE_URL = "baseUrlForRss".intern() ;
  final public static String ST_WORKINGTIME = "isShowWorkingTime".intern() ; 
  final public static String ST_TIME_BEGIN = "workingTimeBegin".intern() ;
  final public static String ST_TIME_END = "workingTimeEnd".intern() ;
  final public static String ST_USER_IGNORE = "ignoredUsers".intern() ;


  private CalendarService cservice_;
  //private String defaultCalendarCategory_;
  //private String[] defaultCalendar_;
  private List<String> ignore_users_ ;
  private CalendarSetting defaultCalendarSetting_ ;

  final public static String DEFAULT_CALENDAR_CATEGORYID = "defaultCalendarCategoryId";
  final public static String DEFAULT_CALENDAR_ID = "defaultCalendarId";
  
  final public static String DEFAULT_CALENDAR_CATEGORYNAME = "defaultCalendarCategoryName";
  final public static String DEFAULT_CALENDAR_NAME = "defaultCalendarName";
  
  final public static String DEFAULT_EVENTCATEGORY_ID_ALL = "defaultEventCategoryIdAll";
  final public static String DEFAULT_EVENTCATEGORY_ID_MEETING = "defaultEventCategoryIdMeeting";
  final public static String DEFAULT_EVENTCATEGORY_ID_CALLS = "defaultEventCategoryIdCalls";
  final public static String DEFAULT_EVENTCATEGORY_ID_CLIENTS = "defaultEventCategoryIdClients";
  final public static String DEFAULT_EVENTCATEGORY_ID_HOLIDAY = "defaultEventCategoryIdHoliday";
  final public static String DEFAULT_EVENTCATEGORY_ID_ANNIVERSARY = "defaultEventCategoryIdAnniversary";
  public static String[] defaultEventCategoryId = {DEFAULT_EVENTCATEGORY_ID_ALL, DEFAULT_EVENTCATEGORY_ID_MEETING, DEFAULT_EVENTCATEGORY_ID_CALLS
    , DEFAULT_EVENTCATEGORY_ID_CLIENTS, DEFAULT_EVENTCATEGORY_ID_HOLIDAY, DEFAULT_EVENTCATEGORY_ID_ANNIVERSARY} ;
 
  final public static String DEFAULT_EVENTCATEGORY_NAME_ALL = "defaultEventCategoryNameAll";
  final public static String DEFAULT_EVENTCATEGORY_NAME_MEETING = "defaultEventCategoryNameMeeting";
  final public static String DEFAULT_EVENTCATEGORY_NAME_CALLS = "defaultEventCategoryNameCalls";
  final public static String DEFAULT_EVENTCATEGORY_NAME_CLIENTS = "defaultEventCategoryNameClients";
  final public static String DEFAULT_EVENTCATEGORY_NAME_HOLIDAY = "defaultEventCategoryNameHoliday";
  final public static String DEFAULT_EVENTCATEGORY_NAME_ANNIVERSARY = "defaultEventCategoryNameAnniversary";
  public static String[] defaultEventCategoryName = {DEFAULT_EVENTCATEGORY_NAME_ALL, DEFAULT_EVENTCATEGORY_NAME_MEETING, DEFAULT_EVENTCATEGORY_NAME_CALLS
    , DEFAULT_EVENTCATEGORY_NAME_CLIENTS, DEFAULT_EVENTCATEGORY_NAME_HOLIDAY, DEFAULT_EVENTCATEGORY_NAME_ANNIVERSARY} ;

  public NewUserListener() {};
  /**
   * 
   * @param  Calendar service geeting from the Portlet Container
   * @param params  parameters defined in the cs-plugins-configuration.xml
   */
  @SuppressWarnings("unchecked")
  public NewUserListener(CalendarService cservice, InitParams params)
  throws Exception {
    cservice_ = cservice;
    defaultCalendarSetting_ = new CalendarSetting() ;
    if(params.getValueParam(ST_VIEW_TYPE) != null) {
      defaultCalendarSetting_.setViewType(params.getValueParam(ST_VIEW_TYPE).getValue()) ;
    }
    if(params.getValueParam(ST_TIME_INTEVAL) != null) {
      defaultCalendarSetting_.setTimeInterval(Long.parseLong(params.getValueParam(ST_TIME_INTEVAL).getValue())) ;
    }

    if(params.getValueParam(ST_WEEK_START) != null) {
      defaultCalendarSetting_.setWeekStartOn(params.getValueParam(ST_WEEK_START).getValue()) ;
    }
    if(params.getValueParam(ST_DATE_FORMAT) != null) {
      defaultCalendarSetting_.setDateFormat(params.getValueParam(ST_DATE_FORMAT).getValue()) ;
    }
    if(params.getValueParam(ST_TIME_FORMAT) != null) {
      defaultCalendarSetting_.setTimeFormat(params.getValueParam(ST_TIME_FORMAT).getValue()) ;
    }
    if(params.getValueParam(ST_LOCALE) != null) {
      defaultCalendarSetting_.setLocation(params.getValueParam(ST_LOCALE).getValue()) ;
    }
    if(params.getValueParam(ST_TIMEZONE) != null) {
      defaultCalendarSetting_.setTimeZone(params.getValueParam(ST_TIMEZONE).getValue()) ;
    }
    if(params.getValueParam(ST_BASE_URL) != null) {
      defaultCalendarSetting_.setBaseURL(params.getValueParam(ST_BASE_URL).getValue()) ;
    }
    if(params.getValueParam(ST_WORKINGTIME) != null) {
      defaultCalendarSetting_.setShowWorkingTime(Boolean.parseBoolean(params.getValueParam(ST_WORKINGTIME).getValue())) ;
      if(defaultCalendarSetting_.isShowWorkingTime()) {
        if(params.getValueParam(ST_TIME_BEGIN) != null) {
          defaultCalendarSetting_.setWorkingTimeBegin(params.getValueParam(ST_TIME_BEGIN).getValue()) ;
        }
        if(params.getValueParam(ST_TIME_END) != null) {
          defaultCalendarSetting_.setWorkingTimeEnd(params.getValueParam(ST_TIME_END).getValue()) ;
        }
      }
    }
    ValuesParam ignoredUsers = params.getValuesParam(ST_USER_IGNORE) ;
    if(ignoredUsers != null && !ignoredUsers.getValues().isEmpty()) {
      ignore_users_ = ignoredUsers.getValues() ;
    }
  }

  public void postSave(User user, boolean isNew) throws Exception {
    if(!isNew) return ;
    if(ignore_users_ != null && !ignore_users_.isEmpty())
      for(String u : ignore_users_) {
        if (user.getUserName().equalsIgnoreCase(u)) return ;
      }
    SessionProvider sysProvider = createSystemProvider();
    ThreadLocalSessionProviderService sessionProviderService = (ThreadLocalSessionProviderService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ThreadLocalSessionProviderService.class);
    sessionProviderService.setSessionProvider(null, sysProvider);
    try {
      EventCategory eventCategory = new EventCategory();
      eventCategory.setDataInit(true);
      for (int id = 0; id < defaultEventCategoryId.length; id ++) {
        eventCategory.setId(defaultEventCategoryId[id]);
        eventCategory.setName(defaultEventCategoryName[id]);
        cservice_.saveEventCategory(user.getUserName(), eventCategory, null, true);
      }

      // save default calendar category
      CalendarCategory calCategory = new CalendarCategory();
      calCategory.setId(DEFAULT_CALENDAR_CATEGORYID);
      calCategory.setName(DEFAULT_CALENDAR_CATEGORYNAME);
      calCategory.setDataInit(true) ;
      cservice_.saveCalendarCategory(user.getUserName(),	calCategory, true);

      // save default calendar
      Calendar cal = new Calendar();
      cal.setId(DEFAULT_CALENDAR_ID);
      cal.setName(DEFAULT_CALENDAR_NAME);
      cal.setCategoryId(calCategory.getId());
      cal.setDataInit(true) ;
      cal.setCalendarOwner(user.getUserName()) ;
      if(defaultCalendarSetting_ != null) {
        if(defaultCalendarSetting_.getLocation() != null)
          cal.setLocale(defaultCalendarSetting_.getLocation()) ;
        if(defaultCalendarSetting_.getTimeZone() != null)
          cal.setTimeZone(defaultCalendarSetting_.getTimeZone()) ;
      }
      cservice_.saveUserCalendar(user.getUserName(), cal,	true);

      if(defaultCalendarSetting_ != null && user != null) {
        cservice_.saveCalendarSetting(user.getUserName(), defaultCalendarSetting_) ;
      }
    } catch (Exception e) {
      e.printStackTrace() ;
    } finally {
      sessionProviderService.removeSessionProvider(null);
    }
  }

  @Override
  public void postDelete(User user) throws Exception {
    SessionProvider session = createSystemProvider(); ;
    String username = user.getUserName() ;
    List<GroupCalendarData> gCalData = cservice_.getCalendarCategories(username, true) ;  
    try {
      if(!gCalData.isEmpty())
        for (GroupCalendarData gCal : gCalData) {
          cservice_.removeCalendarCategory(username, gCal.getId()) ;
        }
      List<EventCategory> eCats = cservice_.getEventCategories(username) ;
      if(!eCats.isEmpty())
        for(EventCategory ecat : eCats) {
          cservice_.removeEventCategory(username, ecat.getId()) ;
        }
      GroupCalendarData   calData = cservice_.getSharedCalendars(username, true) ;
      if(calData != null && !calData.getCalendars().isEmpty())
        for(Calendar cal : calData.getCalendars()) {
          cservice_.removeSharedCalendar(username, cal.getId()) ;
        }
    } catch (Exception e) {
      e.printStackTrace() ;
    } finally {
      session.close() ;
    }
    super.postDelete(user);
  }
  
  private SessionProvider createSystemProvider() {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    SessionProviderService service = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class);
    return service.getSystemSessionProvider(null) ;    
  }
}