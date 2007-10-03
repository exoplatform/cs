/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.service.impl;

import java.util.List;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarCategory;
import org.exoplatform.calendar.service.CalendarImportExport;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.FeedData;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.calendar.service.RssData;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Jul 2, 2007  
 */
public interface DataStorage {
  
  public Calendar getUserCalendar(String username, String calendarId) throws Exception ;
  public List<Calendar> getUserCalendars(String username) throws Exception ;
  public List<Calendar> getUserCalendarsByCategory(String username, String calendarCategoryId) throws Exception ;
  public void saveUserCalendar(String username, Calendar calendar, boolean isNew) throws Exception ;
  public Calendar removeUserCalendar(String username, String calendarId) throws Exception ;
  
  public Calendar getGroupCalendar(String calendarId) throws Exception ;  
  public List<GroupCalendarData> getGroupCalendars(String[] groupId) throws Exception ;  
  public void saveGroupCalendar(Calendar calendar, boolean isNew) throws Exception ;  
  public Calendar removeGroupCalendar(String calendarId) throws Exception ;
  
  public List<CalendarCategory> getCategories(String username) throws Exception ;
  public List<GroupCalendarData> getCalendarCategories(String username) throws Exception ;
  public CalendarCategory getCalendarCategory(String username, String calendarCategoryId) throws Exception ;
  public void saveCalendarCategory(String username, CalendarCategory calendarCategory, boolean isNew) throws Exception ; 
  public CalendarCategory removeCalendarCategory(String username, String calendarCategoryId) throws Exception ;
  
  public List<EventCategory> getEventCategories(String username) throws Exception ;
  public void saveEventCategory(String username, EventCategory eventCategory,EventCategory newEventCategory, boolean isNew) throws Exception ;
  public void removeEventCategory(String username, String eventCategoryId) throws Exception ;
  
  public CalendarEvent getUserEvent(String username, String eventCategoryId, String eventId) throws Exception ;
  public List<CalendarEvent> getUserEventByCalendar(String username, List<String> calendarIds) throws Exception ;
  public List<CalendarEvent> getUserEvents(String username, EventQuery eventQuery) throws Exception ;
  public void saveUserEvent(String username, String calendarId, CalendarEvent event, boolean isNew) throws Exception ;
  public CalendarEvent removeUserEvent(String username, String calendarId, String eventId) throws Exception ;
  
  
  public CalendarEvent getGroupEvent(String calendarId, String eventId) throws Exception ;
  public List<CalendarEvent> getGroupEventByCalendar(List<String> calendarIds) throws Exception ;
  public List<CalendarEvent> getPublicEvents(EventQuery eventQuery) throws Exception ;
  public void saveGroupEvent(String calendarId, CalendarEvent event, boolean isNew) throws Exception ;
  public CalendarEvent removeGroupEvent(String calendarId, String eventId) throws Exception ;
  
  public void saveCalendarSetting(String username, CalendarSetting setting) throws Exception ;
  public CalendarSetting getCalendarSetting(String username) throws Exception ;
  public void generateRss(String username, List<String> calendarIds, RssData rssData, CalendarImportExport importExport) throws Exception ;
  public List<FeedData> getFeeds(String username) throws Exception  ;
}
