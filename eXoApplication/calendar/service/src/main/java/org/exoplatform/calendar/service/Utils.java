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
package org.exoplatform.calendar.service;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Sep 28, 2007  
 */
public class Utils {


  public static final String EXO_ID = "exo:id".intern() ;
  public static final String EXO_LABEL = "exo:label".intern() ;
  public static final String EXO_NAME = "exo:name".intern() ;
  public static final String EXO_DESCRIPTION = "exo:description".intern() ;
  public static final String EXO_EVENT_ID = "exo:eventId".intern() ;
  public static final String EXO_EVENT_CATEGORYID = "exo:eventCategoryId".intern() ;
  public static final String EXO_EVENT_CATEGORY_NAME = "exo:eventCategoryName".intern() ;
  public static final String EXO_TASK_DELEGATOR = "exo:taskDelegator".intern() ;
  public static final String EXO_REPEAT = "exo:repeat".intern() ;
  public static final String EXO_EVENT_TYPE = "exo:eventType".intern() ;
  public static final String EXO_PRIORITY = "exo:priority".intern() ;
  public static final String EXO_IS_PRIVATE = "exo:isPrivate".intern() ;
  public static final String EXO_EVENT_STATE = "exo:eventState".intern() ;
  public static final String EXO_INVITATION = "exo:invitation".intern() ;
  public static final String EXO_CALENDAR_EVENT = "exo:calendarEvent".intern() ;
  public static final String EXO_REMINDER_TYPE = "exo:reminderType".intern() ;
  public static final String EXO_ALARM_BEFORE = "exo:alarmBefore".intern() ;
  public static final String EXO_EMAIL = "exo:email".intern() ;
  public static final String EXO_OWNER = "exo:creator".intern() ;
  public static final String EXO_REMINDER = "exo:reminder".intern() ;
  public static final String EXO_FROM_DATE_TIME = "exo:fromDateTime".intern() ;
  public static final String EXO_TO_DATE_TIME = "exo:toDateTime".intern() ;
  public static final String EXO_SUMMARY = "exo:summary".intern() ;
  public static final String EXO_IS_REPEAT = "exo:isRepeat".intern() ;
  public static final String EXO_IS_OVER = "exo:isOver".intern() ;
  public static final String EXO_CALENDAR_PUBLIC_EVENT = "exo:calendarPublicEvent".intern() ;
  public static final String EXO_EVENT_CATEGORY = "exo:eventCategory".intern() ;
  
  public static final String EXO_DATA = "exo:data".intern() ;
  public static final String EXO_ICAL_DATA = "exo:iCalData".intern() ;
  public static final String EXO_TITLE = "exo:title".intern() ;
  public static final String EXO_CONTENT = "exo:content".intern() ;
  
  
  public static final String EXO_CALENDAR_SETTING = "exo:calendarSetting".intern() ;
  public static final String EXO_IS_SHOW_WORKING_TIME = "exo:showWorkingTime".intern() ;
  public static final String EXO_WORKING_BEGIN = "exo:workingTimeBegin".intern() ;
  public static final String EXO_WORKING_END = "exo:workingTimeEnd".intern() ;
  public static final String EXO_PRIVATE_CALENDARS = "exo:defaultPrivateCalendars".intern() ;
  public static final String EXO_PUBLIC_CALENDARS = "exo:defaultPublicCalendars".intern() ;
  public static final String EXO_SHARED_CALENDARS = "exo:defaultSharedCalendars".intern() ;
  public static final String EXO_SHARED_CALENDAR_COLORS = "exo:sharedCalendarsColors".intern() ;
  
  public static final String EXO_EVEN_TATTACHMENT = "exo:eventAttachment".intern() ;
  public static final String EXO_FILE_NAME = "exo:fileName".intern() ;
  public static final String EXO_CATEGORY_ID = "exo:categoryId".intern() ;
  public static final String EXO_VIEW_PERMISSIONS = "exo:viewPermissions".intern() ;
  public static final String EXO_EDIT_PERMISSIONS = "exo:editPermissions".intern() ;
  public static final String EXO_GROUPS = "exo:groups".intern() ;
  public static final String EXO_LOCALE = "exo:locale".intern() ;
  public static final String EXO_TIMEZONE = "exo:timeZone".intern() ;
  public static final String EXO_CALENDAR_ID = "exo:calendarId".intern() ;
  public static final String EXO_SHARED_MIXIN = "exo:calendarShared".intern();
  public static final String EXO_SHARED_ID = "exo:sharedId".intern();
  public static final String EXO_PARTICIPANT = "exo:participant".intern() ;
  public static final String EXO_CALENDAR = "exo:calendar".intern() ;
  public static final String EXO_CALENDAR_COLOR = "exo:calendarColor".intern() ;
  public static final String EXO_CALENDAR_CATEGORY = "exo:calendarCategory".intern() ;
  public static final String EXO_CALENDAR_OWNER = "exo:calendarOwner".intern() ;
  public static final String EXO_SHARED_COLOR = "exo:sharedColor".intern() ;
  public static final String EXO_VIEW_TYPE = "exo:viewType".intern() ;
  public static final String EXO_TIME_INTERVAL = "exo:timeInterval".intern() ;
  public static final String EXO_WEEK_START_ON = "exo:weekStartOn".intern() ;
  public static final String EXO_DATE_FORMAT = "exo:dateFormat".intern()  ;
  public static final String EXO_TIME_FORMAT = "exo:timeFormat".intern() ;
  public static final String EXO_LOCATION = "exo:location".intern() ;
  public static final String EXO_REMINDER_DATE = "exo:remindDateTime".intern() ;
  public static final String EXO_ROOT_EVENT_ID = "exo:rootEventId".intern() ;
  public static final String EXO_RSS_DATA = "exo:rssData".intern() ;
  public static final String EXO_BASE_URL = "exo:baseUrl".intern() ;
  public static final String EXO_SEND_OPTION = "exo:sendOption".intern() ;
  public static final String EXO_MESSAGE = "exo:message".intern() ;
  public static final String EXO_PARTICIPANT_STATUS = "exo:participantStatus".intern() ;
  
  public static final String X_STATUS = "X-STATUS".intern() ;
  

  public static final String ATTACHMENT_NODE = "attachment".intern() ; ;
  public static final String REMINDERS_NODE = "reminders".intern() ; ;


  public static final String NT_UNSTRUCTURED = "nt:unstructured".intern() ;
  public static final String NT_FILE = "nt:file".intern() ;
  public static final String NT_RESOURCE = "nt:resource".intern() ;


  public static final String MIX_REFERENCEABLE = "mix:referenceable".intern() ;

  public static final String JCR_LASTMODIFIED = "jcr:lastModified".intern() ;
  public static final String JCR_CONTENT = "jcr:content".intern() ;
  public static final String JCR_MIMETYPE = "jcr:mimeType".intern() ;
  public static final String JCR_DATA = "jcr:data".intern() ;


  public static final String MIMETYPE_TEXTPLAIN = "text/plain".intern() ;
  public static final String MIMETYPE_ICALENDAR = "TEXT/CALENDAR".intern() ;
  public static final String ATTACHMENT = "ATTACHMENT".intern();
  public static final String INLINE = "INLINE".intern();

  public static final String COMMA = ",".intern();
  public static final String COLON = ":".intern() ;
  public static final String SLASH = "/".intern() ;
  
  final public static String CALENDAR_REMINDER = "reminders".intern();
  final public static String CALENDAR_APP = "CalendarApplication".intern() ;

  public static final int DENY = 0 ;
  public static final int ACCEPT = 1 ;
  public static final int NOTSURE = 2 ;
  
  public static final String RSS_NODE  = "iCalendars".intern() ;
  public static final String CALDAV_NODE  = "WebDavCalendars".intern() ;
  
  public static final String ICS_EXT  = ".ics".intern() ;
  
  final public static String STATUS_EMPTY = "".intern();
  final public static String STATUS_PENDING = "pending".intern();
  final public static String STATUS_YES = "yes".intern();
  final public static String STATUS_NO = "no".intern();
  
  public static final int INVALID_TYPE = -1;
  public static final int PRIVATE_TYPE = 0;
  public static final int SHARED_TYPE = 1;
  public static final int PUBLIC_TYPE = 2;
  
  public static final String SPLITTER = "splitter";
  
  /**
   * The method creates instance calendar object with time zone is GMT 0
   * @return GregorianCalendar
   */
  public static GregorianCalendar getInstanceTempCalendar() { 
    GregorianCalendar  calendar = new GregorianCalendar() ;
    int gmtoffset = calendar.get(Calendar.DST_OFFSET) + calendar.get(Calendar.ZONE_OFFSET);
    calendar.setTimeInMillis(System.currentTimeMillis() - gmtoffset) ;
    return  calendar;
  }
  /**
   * The method validates the string value is empty or not
   * @param string String input value
   * @return boolean value
   */
  public static boolean isEmpty(String string) {
    return string == null || string.trim().length() == 0 ;
  }
}
