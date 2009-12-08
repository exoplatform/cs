/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.calendar.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;

import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * 
 * Deprecated API for CalendarService
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Mar 14, 2009  
 */
public interface CalendarServiceLegacy {

    /**
     * @deprecated use {@link CalendarService#getCategories(String)} 
     */
    public List<CalendarCategory> getCategories(SessionProvider userSession, String username) throws Exception ;

    /**
     * @deprecated use {@link CalendarService#getCalendarCategories(String, boolean)}
     */
    public List<GroupCalendarData> getCalendarCategories(SessionProvider userSession, String username, boolean isShowAll) throws Exception ;

    /**
     * @deprecated use {@link CalendarService#getCalendarCategory(String, String)}
     */
    public CalendarCategory getCalendarCategory(SessionProvider userSession, String username, String calendarCategoryId) throws Exception ;

    /**
     * @deprecated use {@link CalendarService#saveCalendarCategory(String, CalendarCategory, boolean)}
     */
    public void saveCalendarCategory(SessionProvider userSession, String username, CalendarCategory calendarCategory, boolean isNew) throws Exception ; 

    /**
     * @deprecated use {@link CalendarService#removeCalendarCategory(String, String)}
     */
    public CalendarCategory removeCalendarCategory(SessionProvider userSession, String username, String calendarCategoryId) throws Exception ;

    /**
     * @deprecated use {@link CalendarService#getUserCalendar(String, String)}
     */
    public Calendar getUserCalendar(SessionProvider userSession, String username, String calendarId) throws Exception ;

    /**
     * @deprecated use {@link CalendarService#getUserCalendars(String, boolean)}
     */
    public List<Calendar> getUserCalendars(SessionProvider userSession, String username, boolean isShowAll) throws Exception ;

    /**
     * @deprecated use {@link CalendarService#getUserCalendarsByCategory(String, String)}
     */
    public List<Calendar> getUserCalendarsByCategory(SessionProvider userSession, String username, String calendarCategoryId) throws Exception ;

    /**
     * @deprecated use {@link CalendarService#saveUserCalendar(String, Calendar, boolean)}
     */
    public void saveUserCalendar(SessionProvider userSession, String username, Calendar calendar, boolean isNew) throws Exception ;

    /**
     * @deprecated use {@link CalendarService#removeUserCalendar(String, String)}
     */
    public Calendar removeUserCalendar(SessionProvider userSession, String username, String calendarId) throws Exception ;

    /**
     * @deprecated use {@link CalendarService#saveSharedCalendar(String, Calendar)}
     */
    public void saveSharedCalendar(SessionProvider systemSession, String username, Calendar calendar) throws Exception ;

    /**
     * @deprecated use {@link CalendarService#getGroupCalendar(String)}
     */
    public Calendar getGroupCalendar(SessionProvider systemSession, String calendarId) throws Exception ;  

    /**
     * @deprecated use {@link CalendarService#getGroupCalendars(String[], boolean, String)}
     */
    public List<GroupCalendarData> getGroupCalendars(SessionProvider systemSession, String[] groupIds, boolean isShowAll, String username) throws Exception ;  

    /**
     * @deprecated use {@link CalendarService#savePublicCalendar(Calendar, boolean, String)}
     */
    public void savePublicCalendar(SessionProvider systemSession, Calendar calendar, boolean isNew, String username) throws Exception ;  

    /**
     * @deprecated use {@link CalendarService#removePublicCalendar(String)}
     */
    public Calendar removePublicCalendar(SessionProvider systemSession, String calendarId) throws Exception ;

    /**
     * @deprecated use {@link CalendarService#getEventCategories(String)}
     */
    public List<EventCategory> getEventCategories(SessionProvider userSession, String username) throws Exception ;

    /**
     * @deprecated use {@link CalendarService#saveEventCategory(String, EventCategory, String[], boolean)}
     */
    public void saveEventCategory(SessionProvider userSession, String username, EventCategory eventCategory, String[] values, boolean isNew) throws Exception ;

    /**
     * @deprecated use {@link CalendarService#removeEventCategory(String, String)}
     */
    public void removeEventCategory(SessionProvider userSession, String username, String eventCategoryName) throws Exception ;  

    /**
     * @deprecated use {@link CalendarService#getEventCategory(String, String)}
     */
    public EventCategory getEventCategory(SessionProvider userSession, String username, String eventCategoryId) throws Exception ;

    /**
     * @deprecated use {@link CalendarService#getUserEventByCalendar(String, List)}
     */
    public List<CalendarEvent> getUserEventByCalendar(SessionProvider userSession, String username, List<String> calendarIds) throws Exception ;

    /**
     * @deprecated use {@link CalendarService#getUserEvents(String, EventQuery)}
     */
    public List<CalendarEvent> getUserEvents(SessionProvider userSession, String username, EventQuery eventQuery) throws Exception ;

    /**
     * @deprecated use {@link CalendarService#saveUserEvent(String, String, CalendarEvent, boolean)}
     */
    public void saveUserEvent(SessionProvider userSession, String username, String calendarId, CalendarEvent event, boolean isNew) throws Exception ;

    /**
     * @deprecated use {@link CalendarService#removeUserEvent(String, String, String)}
     */
    public CalendarEvent removeUserEvent(SessionProvider userSession, String username, String calendarId, String eventId) throws Exception ;

    /**
     * @deprecated use {@link CalendarService#getGroupEvent(String, String)}
     */
    public CalendarEvent getGroupEvent(SessionProvider systemSession, String calendarId, String eventId) throws Exception ;

    /**
     * @deprecated use {@link CalendarService#getGroupEventByCalendar(List)}
     */
    public List<CalendarEvent> getGroupEventByCalendar(SessionProvider systemSession, List<String> calendarIds) throws Exception ;

    /**
     * @deprecated use {@link CalendarService#getPublicEvents(EventQuery)}
     */
    public List<CalendarEvent> getPublicEvents(SessionProvider systemSession, EventQuery eventQuery) throws Exception ;

    /**
     * @deprecated use {@link CalendarService#savePublicEvent(String, CalendarEvent, boolean)}
     */
    public void savePublicEvent(SessionProvider systemSession, String calendarId, CalendarEvent event, boolean isNew) throws Exception ;

    /**
     * @deprecated use {@link CalendarService#removePublicEvent(String, String)}
     */
    public CalendarEvent removePublicEvent(SessionProvider systemSession, String calendarId, String eventId) throws Exception ;

    /**
     * @deprecated use {@link CalendarService#saveCalendarSetting(String, CalendarSetting)}
     */
    public void saveCalendarSetting(SessionProvider userSession, String username, CalendarSetting setting) throws Exception ;

    /**
     * @deprecated use {@link CalendarService#getCalendarSetting(String)}
     */
    public CalendarSetting getCalendarSetting(SessionProvider userSession, String username) throws Exception ;
 
    /**
     * @deprecated use {@link CalendarService#generateRss(String, LinkedHashMap, RssData)}
     */
    public int generateRss(SessionProvider systemSession, String username, LinkedHashMap<String, Calendar> calendars, RssData rssData) throws Exception ;

    /**
     * @deprecated use {@link CalendarService#getFeeds(String)}
     */
    public List<FeedData> getFeeds(SessionProvider systemSession, String username) throws Exception  ;

    /**
     * @deprecated use {@link CalendarService#getRssHome(String)}
     */
    public Node getRssHome(SessionProvider systemSession, String username) throws Exception ;

    /**
     * @deprecated use {@link CalendarService#searchEvent(String, EventQuery, String[])}
     */
    public EventPageList searchEvent(SessionProvider userSession, String username, EventQuery eventQuery, String[] publicCalendarIds)throws Exception ;

    /**
     * @deprecated use {@link CalendarService#searchHightLightEvent(String, EventQuery, String[])}
     */
    public Map<Integer, String > searchHightLightEvent(SessionProvider userSession, String username, EventQuery eventQuery, String[] publicCalendarIds)throws Exception ; 

    /**
     * @deprecated use {@link CalendarService#shareCalendar(String, String, List)}
     */
    public void shareCalendar(SessionProvider systemSession, String username, String calendarId, List<String> receiverUsers) throws Exception ;

    /**
     * @deprecated use {@link CalendarService#getSharedCalendars(String, boolean)}
     */
    public GroupCalendarData getSharedCalendars(SessionProvider systemSession, String username, boolean isShowAll) throws Exception ;

    /**
     * @deprecated use {@link CalendarService#getEvents(String, EventQuery, String[])}
     */
    public List<CalendarEvent> getEvents(SessionProvider userSession, String username, EventQuery eventQuery, String[] publicCalendarIds) throws Exception ;

    /**
     * @deprecated use {@link CalendarService#removeSharedCalendar(String, String)}
     */
    public void removeSharedCalendar(SessionProvider systemSesssion, String username, String calendarId) throws Exception  ;

    /**
     * @deprecated use {@link CalendarService#saveEventToSharedCalendar(String, String, CalendarEvent, boolean)}
     */
    public void saveEventToSharedCalendar(SessionProvider systemSession, String username, String calendarId, CalendarEvent event, boolean isNew) throws Exception  ;

    /**
     * @deprecated use {@link CalendarService#checkFreeBusy(EventQuery)}
     */
    public Map<String, String> checkFreeBusy(SessionProvider systemSession, EventQuery eventQuery) throws Exception  ;

    /**
     * @deprecated use {@link CalendarService#generateCalDav(String username, LinkedHashMap<String, Calendar> calendars, RssData rssData)}
     */
    public int generateCalDav(String username, List<String> calendarIds, RssData rssData) throws Exception ;

    
    /**
     * @deprecated use {@link CalendarService#generateRss(String username, LinkedHashMap<String, Calendar> calendars, RssData rssData)}
     */
    public int generateRss(String username, List<String> calendarIds, RssData rssData) throws Exception ;
    
    /**
     * @deprecated use {@link CalendarService#removeSharedEvent(String, String, String)}
     */
    public void removeSharedEvent(SessionProvider systemSession, String username, String calendarId, String eventId) throws Exception ;

   
    public void moveEvent(SessionProvider userSession, String formCalendar, String toCalendar, String formType, String toType, List<CalendarEvent> calEvents, String username) throws Exception ;
  
}
