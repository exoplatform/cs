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

import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValuesParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;

/**
 * Created by The eXo Platform SARL Author : Hung Nguyen Quang
 * hung.nguyen@exoplatform.com Nov 23, 2007 3:09:21 PM
 */
public class NewUserListener extends UserEventListener {

	private static final Log LOG = ExoLogger.getLogger(NewUserListener.class);
	
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
   * @param cservice : pass throw create object 
   * @param params : given by config xml
   * @throws Exception
   */
  
  @SuppressWarnings("unchecked")
  public NewUserListener(CalendarService cservice, InitParams params)
  throws Exception {
    cservice_ = cservice;
    defaultCalendarSetting_ = new CalendarSetting() ;
    if(params.getValueParam(ST_VIEW_TYPE) != null) {
      defaultCalendarSetting_.setViewType(params.getValueParam(ST_VIEW_TYPE).getValue()) ;
    } else {
      defaultCalendarSetting_.setViewType(CalendarSetting.WORKING_VIEW) ;
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
        } else {
          defaultCalendarSetting_.setWorkingTimeBegin("09:00") ;
        }
        if(params.getValueParam(ST_TIME_END) != null) {
          defaultCalendarSetting_.setWorkingTimeEnd(params.getValueParam(ST_TIME_END).getValue()) ;
        } else {
          defaultCalendarSetting_.setWorkingTimeEnd("18:00") ;
        }
      }
    } else {
      defaultCalendarSetting_.setShowWorkingTime(true) ;
      if(params.getValueParam(ST_TIME_BEGIN) != null) {
        defaultCalendarSetting_.setWorkingTimeBegin(params.getValueParam(ST_TIME_BEGIN).getValue()) ;
      } else {
        defaultCalendarSetting_.setWorkingTimeBegin("09:00") ;
      }
      if(params.getValueParam(ST_TIME_END) != null) {
        defaultCalendarSetting_.setWorkingTimeEnd(params.getValueParam(ST_TIME_END).getValue()) ;
      } else {
        defaultCalendarSetting_.setWorkingTimeEnd("18:00") ;
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
      try {
    	  cservice_.initNewUser(user.getUserName(), defaultCalendarSetting_);
    } catch (Exception e) {
      LOG.error("Failed to initialize calendar account for " + user.getUserName());
    }  
  }
}