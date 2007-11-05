/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.service.impl;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarCategory;
import org.exoplatform.calendar.service.CalendarImportExport;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.service.EventPageList;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.FeedData;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.calendar.service.RssData;
import org.exoplatform.registry.JCRRegistryService;
import org.exoplatform.services.jcr.RepositoryService;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public class CalendarServiceImpl implements CalendarService{

  final private static String ICALENDAR = "ICalendar(.ics)".intern() ;

  private JCRDataStorage storage_ ;
  private Map<String, CalendarImportExport> calendarImportExport_ = new HashMap<String, CalendarImportExport>() ;

  public CalendarServiceImpl(RepositoryService  repositoryService, 
      JCRRegistryService jcrRegistryService) throws Exception {
    storage_ = new JCRDataStorage(repositoryService, jcrRegistryService) ;
    calendarImportExport_.put(ICALENDAR, new ICalendarImportExport(storage_)) ;
  }

  public List<CalendarCategory> getCategories(String username) throws Exception {
    return storage_.getCategories(username) ;
  }

  public List<GroupCalendarData> getCalendarCategories(String username) throws Exception {
    return storage_.getCalendarCategories(username);
  }
  public CalendarCategory getCalendarCategory(String username, String calendarCategoryId) throws Exception {
    return storage_.getCalendarCategory(username, calendarCategoryId) ;
  }
  public void saveCalendarCategory(String username, CalendarCategory calendarCategory, boolean isNew) throws Exception {
    storage_.saveCalendarCategory(username, calendarCategory, isNew) ;
  }
  public CalendarCategory removeCalendarCategory(String username, String calendarCategoryId) throws Exception {
    return storage_.removeCalendarCategory(username, calendarCategoryId);
  }

  public Calendar getUserCalendar(String username, String calendarId) throws Exception {
    return storage_.getUserCalendar(username, calendarId);
  }
  public List<Calendar> getUserCalendars(String username) throws Exception {
    return storage_.getUserCalendars(username) ;
  }
  public List<Calendar> getUserCalendarsByCategory(String username, String calendarCategoryId) throws Exception {
    return storage_.getUserCalendarsByCategory(username, calendarCategoryId);
  }
  public void saveUserCalendar(String username, Calendar calendar, boolean isNew) throws Exception {
    storage_.saveUserCalendar(username, calendar, isNew) ;
  }
  public Calendar removeUserCalendar(String username, String calendarId) throws Exception {
    return storage_.removeUserCalendar(username, calendarId);
  }


  public Calendar getGroupCalendar(String calendarId) throws Exception {
    return storage_.getGroupCalendar(calendarId);
  }
  public List<GroupCalendarData> getGroupCalendars(String[] groupIds) throws Exception {
    return storage_.getGroupCalendars(groupIds);
  }
  public void saveGroupCalendar(Calendar calendar, boolean isNew) throws Exception {
    storage_.saveGroupCalendar(calendar, isNew) ;
  }
  public Calendar removeGroupCalendar(String calendarId) throws Exception {
    return storage_.removeGroupCalendar(calendarId);
  }

  public List<EventCategory> getEventCategories(String username) throws Exception {
    return storage_.getEventCategories(username) ;
  }
  public void saveEventCategory(String username, EventCategory eventCategory, EventCategory newEventCategory, boolean isNew) throws Exception {
    storage_.saveEventCategory(username, eventCategory, newEventCategory, isNew) ;
  }
  public void removeEventCategory(String username, String eventCategoryName) throws Exception {
    storage_.removeEventCategory(username,eventCategoryName);
  }  
  /*public void saveGroupEventCategory(Calendar calendar, EventCategory eventCategory, boolean isNew) throws Exception {
    storage_.saveGroupEventCategory(calendar, eventCategory, isNew) ;
  }
  public EventCategory removeGroupEventCategory(String calendarId, String eventCategoryId) throws Exception {
    return storage_.removeGroupEventCategory(calendarId, eventCategoryId);
  }*/


  public CalendarEvent getUserEvent(String username, String calendarId, String eventId) throws Exception {
    return storage_.getUserEvent(username, calendarId, eventId);
  }
  public List<CalendarEvent> getUserEventByCalendar(String username, List<String> calendarIds) throws Exception {
    return storage_.getUserEventByCalendar(username, calendarIds);
  }
  public List<CalendarEvent> getUserEvents(String username, EventQuery eventQuery) throws Exception {
    return storage_.getUserEvents(username, eventQuery) ;
  }
  public void saveUserEvent(String username, String calendarId, CalendarEvent event, boolean isNew) throws Exception {
    storage_.saveUserEvent(username, calendarId, event, isNew) ;
  }
  public CalendarEvent removeUserEvent(String username, String calendarId, String eventId) throws Exception {
    return storage_.removeUserEvent(username, calendarId, eventId);
  }


  public CalendarEvent getGroupEvent(String calendarId, String eventId) throws Exception {
    return storage_.getGroupEvent(calendarId, eventId);
  }  
  public List<CalendarEvent> getGroupEventByCalendar(List<String> calendarIds) throws Exception {
    return storage_.getGroupEventByCalendar(calendarIds);
  } 
  public List<CalendarEvent> getPublicEvents(EventQuery eventQuery) throws Exception {
    return storage_.getPublicEvents(eventQuery) ;
  }
  public void saveGroupEvent(String calendarId, CalendarEvent event, boolean isNew) throws Exception {
    storage_.saveGroupEvent(calendarId, event, isNew) ;
  }  
  public CalendarEvent removeGroupEvent(String calendarId, String eventId) throws Exception {
    return storage_.removeGroupEvent(calendarId, eventId);
  }

  public CalendarImportExport  getCalendarImportExports(String type) {
    return calendarImportExport_.get(type) ;
  }

  public String[] getExportImportType() throws Exception {
    return calendarImportExport_.keySet().toArray(new String[]{}) ;
  }

  public void saveCalendarSetting(String username, CalendarSetting setting) throws Exception {
    storage_.saveCalendarSetting(username, setting) ;

  }

  public CalendarSetting getCalendarSetting(String username) throws Exception {
    return storage_.getCalendarSetting(username) ;
  }

  public void generateRss(String username, List<String> calendarIds, RssData rssData) throws Exception {
    storage_.generateRss(username, calendarIds, rssData, calendarImportExport_.get(ICALENDAR)) ;
  }

  public List<FeedData> getFeeds(String username) throws Exception {
    return storage_.getFeeds(username) ;
  }

  public Node getRssHome(String username) throws Exception {
    return storage_.getRssHome(username) ;
  }
  
  public EventPageList searchEvent(String username, EventQuery query, String[] publicCalendarIds)throws Exception {
    return storage_.searchEvent(username, query, publicCalendarIds) ;
  }

  public EventCategory getEventCategory(String username, String eventCategoryId) throws Exception {
    return storage_.getEventCategory(username, eventCategoryId) ;
  }
  
  public Map<Integer, String > searchHightLightEvent(String username, EventQuery eventQuery)throws Exception  {
    return storage_.searchHightLightEvent(username, eventQuery) ;
  }
  
  public void shareCalendar(String username, String calendarId, List<String> receiverUsers) throws Exception {
    storage_.shareCalendar(username, calendarId, receiverUsers) ;
  }
  
  public GroupCalendarData getSharedCalendars(String username) throws Exception {
    return storage_.getSharedCalendars(username) ;
  }
  
  public List<CalendarEvent> getEvent(String username, EventQuery eventQuery, String[] publicCalendarIds) throws Exception{
    return storage_.getEvent(username, eventQuery, publicCalendarIds) ;
  }
  
  public void removeSharedCalendar(String username, String calendarId) throws Exception {
    storage_.removeSharedCalendar(username, calendarId) ;
  }
  
  public void saveEventToSharedCalendar(String username, String calendarId, CalendarEvent event, boolean isNew) throws Exception  {
    storage_.saveEventToSharedCalendar(username, calendarId, event, isNew) ;
  }
}
