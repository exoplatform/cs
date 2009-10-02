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
package org.exoplatform.calendar.service.impl;

import java.util.List;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarCategory;
import org.exoplatform.calendar.service.CalendarImportExport;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.service.EventPageList;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.FeedData;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.calendar.service.RssData;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Jul 2, 2007  
 */
public interface DataStorage {
  
  public Calendar getUserCalendar(SessionProvider sProvider, String username, String calendarId) throws Exception ;
  public List<Calendar> getUserCalendars(SessionProvider sProvider, String username) throws Exception ;
  public List<Calendar> getUserCalendarsByCategory(SessionProvider sProvider, String username, String calendarCategoryId) throws Exception ;
  public void saveUserCalendar(SessionProvider sProvider, String username, Calendar calendar, boolean isNew) throws Exception ;
  public Calendar removeUserCalendar(SessionProvider sProvider, String username, String calendarId) throws Exception ;
  
  public Calendar getGroupCalendar(SessionProvider sProvider, String calendarId) throws Exception ;  
  public List<GroupCalendarData> getGroupCalendars(SessionProvider sProvider, String[] groupId) throws Exception ;  
  public void saveGroupCalendar(SessionProvider sProvider, Calendar calendar, boolean isNew) throws Exception ;  
  public Calendar removeGroupCalendar(SessionProvider sProvider, String calendarId) throws Exception ;
  
  public List<CalendarCategory> getCategories(SessionProvider sProvider, String username) throws Exception ;
  public List<GroupCalendarData> getCalendarCategories(SessionProvider sProvider, String username) throws Exception ;
  public CalendarCategory getCalendarCategory(SessionProvider sProvider, String username, String calendarCategoryId) throws Exception ;
  public void saveCalendarCategory(SessionProvider sProvider, String username, CalendarCategory calendarCategory, boolean isNew) throws Exception ; 
  public CalendarCategory removeCalendarCategory(SessionProvider sProvider, String username, String calendarCategoryId) throws Exception ;
  
  public List<EventCategory> getEventCategories(SessionProvider sProvider, String username) throws Exception ;
  public void saveEventCategory(SessionProvider sProvider, String username, EventCategory eventCategory,EventCategory newEventCategory, boolean isNew) throws Exception ;
  public void removeEventCategory(SessionProvider sProvider, String username, String eventCategoryId) throws Exception ;
  
  public CalendarEvent getUserEvent(SessionProvider sProvider, String username, String eventCategoryId, String eventId) throws Exception ;
  public List<CalendarEvent> getUserEventByCalendar(SessionProvider sProvider, String username, List<String> calendarIds) throws Exception ;
  public List<CalendarEvent> getUserEvents(SessionProvider sProvider, String username, EventQuery eventQuery) throws Exception ;
  public void saveUserEvent(SessionProvider sProvider, String username, String calendarId, CalendarEvent event, boolean isNew) throws Exception ;
  public CalendarEvent removeUserEvent(SessionProvider sProvider, String username, String calendarId, String eventId) throws Exception ;
  
  
  public CalendarEvent getGroupEvent(SessionProvider sProvider, String calendarId, String eventId) throws Exception ;
  public List<CalendarEvent> getGroupEventByCalendar(SessionProvider sProvider, List<String> calendarIds) throws Exception ;
  public List<CalendarEvent> getPublicEvents(SessionProvider sProvider, EventQuery eventQuery) throws Exception ;
  public void saveGroupEvent(SessionProvider sProvider, String calendarId, CalendarEvent event, boolean isNew) throws Exception ;
  public CalendarEvent removeGroupEvent(SessionProvider sProvider, String calendarId, String eventId) throws Exception ;
  
  public void saveCalendarSetting(SessionProvider sProvider, String username, CalendarSetting setting) throws Exception ;
  public CalendarSetting getCalendarSetting(SessionProvider sProvider, String username) throws Exception ;
  public void generateRss(SessionProvider sProvider, String username, List<String> calendarIds, RssData rssData, CalendarImportExport importExport) throws Exception ;
  public List<FeedData> getFeeds(SessionProvider sProvider, String username) throws Exception  ;
  public EventPageList searchEvent(SessionProvider sProvider, String username, EventQuery query, String[] publicCalendarIds)throws Exception ;
  
  public void shareCalendar(SessionProvider sProvider, String username, String calendarId, List<String> receiverUsers) throws Exception ;
  public GroupCalendarData getSharedCalendars(SessionProvider sProvider, String username) throws Exception  ;
  public List<CalendarEvent> getEvent(SessionProvider sProvider, String username, EventQuery eventQuery, String[] publicCalendarIds) throws Exception ;
  
  public void removeSharedCalendar(SessionProvider sProvider, String username, String calendarId) throws Exception ;
  public void saveEventToSharedCalendar(SessionProvider sProvider, String username, String calendarId, CalendarEvent event, boolean isNew) throws Exception  ; 
}
