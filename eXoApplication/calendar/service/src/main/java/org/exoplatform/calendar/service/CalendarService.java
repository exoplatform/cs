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

import java.util.List;
import java.util.Map;

import javax.jcr.Node;

import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public interface CalendarService {
  
  public List<CalendarCategory> getCategories(SessionProvider sProvider, String username) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Check exists of Calendar category list in cache
   * 3. Get calendar category list and return
   * @param username
   * @param isShowAll TODO
   * @return Calendar Category list
   * @throws Exception
   */
  public List<GroupCalendarData> getCalendarCategories(SessionProvider sProvider, String username, boolean isShowAll) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Get calendar category
   * @param username
   * @param calendarCategoryId
   * @return calendarCategory
   * @throws Exception
   */
  public CalendarCategory getCalendarCategory(SessionProvider sProvider, String username, String calendarCategoryId) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Check isNew parameter
   * 3. Create new or update Calendar category
   * 4. Invalidate cache
   * @param username
   * @param CalendarCategory
   * @param isNew
   * @throws Exception
   */
  public void saveCalendarCategory(SessionProvider sProvider, String username, CalendarCategory calendarCategory, boolean isNew) throws Exception ; 
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Remove calendarCategory
   * 3. Invalidate cache
   * @param username
   * @param calendarCategoryId
   * @return CalendarCategory
   * @throws Exception
   */
  public CalendarCategory removeCalendarCategory(SessionProvider sProvider, String username, String calendarCategoryId) throws Exception ;
  
  
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Get calendar from private folder
   * @param username
   * @param calendarId
   * @return Calendar
   * @throws Exception
   */
  public Calendar getUserCalendar(SessionProvider sProvider, String username, String calendarId) throws Exception ;
  
  public List<Calendar> getUserCalendars(SessionProvider sProvider, String username, boolean isShowAll) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Check exists of calendar list in cache
   * 3. Get calendar list by category and return
   * @param username
   * @return Calendar list
   * @throws Exception
   */  
  public List<Calendar> getUserCalendarsByCategory(SessionProvider sProvider, String username, String calendarCategoryId) throws Exception ;
  /**
   * This method should:
   * 1. Check calendar is private or public
   * 2. Check isNew parameter
   * 3. Create new of update calendar in private folder or service folder(public) 
   * 3. Invalidate cache
   * @param username
   * @param calendar
   * @param isNew
   * @throws Exception
   */
  public void saveUserCalendar(SessionProvider sProvider, String username, Calendar calendar, boolean isNew) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node
   * 2. Get group node
   * 3. Remove calendar
   * 4. Invalidate cache
   * @param username
   * @param groupId
   * @param calendarId
   * @return Calendar
   * @throws Exception
   */
  public Calendar removeUserCalendar(SessionProvider sProvider, String username, String calendarId) throws Exception ;
  
  
  /**
   * 
   * This method should:
   * 1. Get calendar service root node
   * 2. Get group node
   * 3. Get calendar
   * @param username
   * @param groupId
   * @param calendarId
   * @return Calendar
   * @throws Exception
   */
  public Calendar getGroupCalendar(SessionProvider sProvider, String calendarId) throws Exception ;  
  /**
   * This method should:
   * 1. Get calendar service root node
   * 2. Check exists of calendar list in cache
   * 3. Get calendar list
   * @param isShowAll TODO
   * @param username TODO
   * @param username
   * @return Calendar list
   * @throws Exception
   */
  public List<GroupCalendarData> getGroupCalendars(SessionProvider sProvider, String[] groupId, boolean isShowAll, String username) throws Exception ;  
  /**
   * This method should:
   * 1. Check calendar is private or public
   * 2. Check isNew parameter
   * 3. Create new of update calendar in private folder or service folder(public) 
   * 3. Invalidate cache
   * @param username
   * @param calendar
   * @param isNew
   * @throws Exception
   */
  public void saveGroupCalendar(SessionProvider sProvider, Calendar calendar, boolean isNew) throws Exception ;  
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Remove calendar
   * 3. Invalidate cache
   * @param username
   * @param calendarId
   * @return Calendar
   * @throws Exception
   */
  public Calendar removeGroupCalendar(SessionProvider sProvider, String calendarId) throws Exception ;
  
  
  public List<EventCategory> getEventCategories(SessionProvider sProvider, String username) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Check isNew parameter
   * 3. Create new or update category
   * 4. Invalidate cache 
   * @param username
   * @param calendarId
   * @param category
   * @param isNew
   * @throws Exception
   */
  public void saveEventCategory(SessionProvider sProvider, String username, EventCategory eventCategory, EventCategory newEventCategory, boolean isNew) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Get calendar by id 
   * 3. Remove event category 
   * 4. Invalidate cache
   * @param username
   * @param calendarId
   * @param categoryId
   * @return EventCategory
   * @throws Exception
   */
  public void removeEventCategory(SessionProvider sProvider, String username, String eventCategoryName) throws Exception ;  
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Get calendar by id 
   * 3. Remove event category 
   * 4. Invalidate cache
   * @param username
   * @param calendarId
   * @param categoryId
   * @return EventCategory
   * @throws Exception
   */
  public EventCategory getEventCategory(SessionProvider sProvider, String username, String eventCategoryId) throws Exception ;
  
  
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Get calendar and event category by parameters id 
   * 3. Get and return Event object
   * @param username
   * @param calendarId
   * @param eventCategoryId
   * @param eventId
   * @return Event
   * @throws Exception
   */
  //public CalendarEvent getUserEvent(SessionProvider sProvider, String username, String calendarId, String eventId) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Check exists of event list in cache
   * 3. Get event list and return
   * @param username
   * @param calendarId
   * @return event list
   * @throws Exception
   */
  public List<CalendarEvent> getUserEventByCalendar(SessionProvider sProvider, String username, List<String> calendarIds) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Check exists of event list in cache
   * 3. Get event list and return
   * @param EventQuery
   * @return event list
   * @throws Exception
   */
  public List<CalendarEvent> getUserEvents(SessionProvider sProvider, String username, EventQuery eventQuery) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by username
   * 2. Get calendar and even catetory by parameters id
   * 3. Check isNew parameter
   * 4. Save new or update event
   * 5. Invalidate cache
   * @param username
   * @param calendarId
   * @param eventCategoryId
   * @param event
   * @param isNew
   * @throws Exception
   */  
  public void saveUserEvent(SessionProvider sProvider, String username, String calendarId, CalendarEvent event, boolean isNew) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Get calendar and category by parameters id 
   * 3. Remove event
   * 4. Invalidate cache
   * @param username
   * param calendarId
   * param eventCategoryId
   * @param eventId
   * @return Event
   * @throws Exception
   */
  public CalendarEvent removeUserEvent(SessionProvider sProvider, String username, String calendarId, String eventId) throws Exception ;
  
  
  /**
   * This method should:
   * 1. Get calendar service root node
   * 2. Get calendar and event category by parameters id 
   * 3. Get and return Event object
   * @param calendarId
   * @param eventCategoryId
   * @param eventId
   * @return Event
   * @throws Exception
   */
  public CalendarEvent getGroupEvent(SessionProvider sProvider, String calendarId, String eventId) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Check exists of event list in cache
   * 3. Get event list and return
   * @param username
   * @param calendarId
   * @return event list
   * @throws Exception
   */
  public List<CalendarEvent> getGroupEventByCalendar(SessionProvider sProvider, List<String> calendarIds) throws Exception ;
  public List<CalendarEvent> getPublicEvents(SessionProvider sProvider, EventQuery eventQuery) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by username
   * 2. Get calendar and even catetory by parameters id
   * 3. Check isNew parameter
   * 4. Save new or update event
   * 5. Invalidate cache
   * @param username
   * @param calendarId
   * @param eventCategoryId
   * @param event
   * @param isNew
   * @throws Exception
   */  
  public void saveGroupEvent(SessionProvider sProvider, String calendarId, CalendarEvent event, boolean isNew) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Get calendar and category by parameters id 
   * 3. Remove event
   * 4. Invalidate cache
   * @param username
   * param calendarId
   * param eventCategoryId
   * @param eventId
   * @return Event
   * @throws Exception
   */
  public CalendarEvent removeGroupEvent(SessionProvider sProvider, String calendarId, String eventId) throws Exception ;
  
  public void saveCalendarSetting(SessionProvider sProvider, String username, CalendarSetting setting) throws Exception ;
  public CalendarSetting getCalendarSetting(SessionProvider sProvider, String username) throws Exception ;
  
  public CalendarImportExport getCalendarImportExports(String type) ;
  public String[] getExportImportType() throws Exception ;
  public void generateRss(SessionProvider sProvider, String username, List<String> calendarIds, RssData rssData) throws Exception ;
  public List<FeedData> getFeeds(SessionProvider sProvider, String username) throws Exception  ;
  public Node getRssHome(SessionProvider sProvider, String username) throws Exception ;
  
  public EventPageList searchEvent(SessionProvider sProvider, String username, EventQuery query, String[] publicCalendarIds)throws Exception ;
  public Map<Integer, String > searchHightLightEvent(SessionProvider sProvider, String username, EventQuery eventQuery, String[] publicCalendarIds)throws Exception ; 
  
  public void shareCalendar(SessionProvider sProvider, String username, String calendarId, List<String> receiverUsers) throws Exception ;
  public GroupCalendarData getSharedCalendars(SessionProvider sProvider, String username, boolean isShowAll) throws Exception ;
  public List<CalendarEvent> getEvent(SessionProvider sProvider, String username, EventQuery eventQuery, String[] publicCalendarIds) throws Exception ;
  public void removeSharedCalendar(SessionProvider sProvider, String username, String calendarId) throws Exception  ;
  public void saveEventToSharedCalendar(SessionProvider sProvider, String username, String calendarId, CalendarEvent event, boolean isNew) throws Exception  ;
  public Map<String, String> checkFreeBusy(SessionProvider sysProvider, EventQuery eventQuery) throws Exception  ;
  
  public void generateCalDav(SessionProvider sProvider, String username, List<String> calendarIds, RssData rssData) throws Exception ;
}
