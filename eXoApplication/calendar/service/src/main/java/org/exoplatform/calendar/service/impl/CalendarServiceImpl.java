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
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;

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

  public CalendarServiceImpl(NodeHierarchyCreator nodeHierarchyCreator) throws Exception {
    storage_ = new JCRDataStorage(nodeHierarchyCreator) ;
    calendarImportExport_.put(ICALENDAR, new ICalendarImportExport(storage_)) ;
  }

  public List<CalendarCategory> getCategories(SessionProvider sProvider, String username) throws Exception {
    return storage_.getCategories(sProvider, username) ;
  }

  public List<GroupCalendarData> getCalendarCategories(SessionProvider sProvider, String username) throws Exception {
    return storage_.getCalendarCategories(sProvider, username);
  }
  public CalendarCategory getCalendarCategory(SessionProvider sProvider, String username, String calendarCategoryId) throws Exception {
    return storage_.getCalendarCategory(sProvider, username, calendarCategoryId) ;
  }
  public void saveCalendarCategory(SessionProvider sProvider, String username, CalendarCategory calendarCategory, boolean isNew) throws Exception {
    storage_.saveCalendarCategory(sProvider, username, calendarCategory, isNew) ;
  }
  public CalendarCategory removeCalendarCategory(SessionProvider sProvider, String username, String calendarCategoryId) throws Exception {
    return storage_.removeCalendarCategory(sProvider, username, calendarCategoryId);
  }

  public Calendar getUserCalendar(SessionProvider sProvider, String username, String calendarId) throws Exception {
    return storage_.getUserCalendar(sProvider, username, calendarId);
  }
  public List<Calendar> getUserCalendars(SessionProvider sProvider, String username) throws Exception {
    return storage_.getUserCalendars(sProvider, username) ;
  }
  public List<Calendar> getUserCalendarsByCategory(SessionProvider sProvider, String username, String calendarCategoryId) throws Exception {
    return storage_.getUserCalendarsByCategory(sProvider, username, calendarCategoryId);
  }
  public void saveUserCalendar(SessionProvider sProvider, String username, Calendar calendar, boolean isNew) throws Exception {
    storage_.saveUserCalendar(sProvider, username, calendar, isNew) ;
  }
  public Calendar removeUserCalendar(SessionProvider sProvider, String username, String calendarId) throws Exception {
    return storage_.removeUserCalendar(sProvider, username, calendarId);
  }


  public Calendar getGroupCalendar(SessionProvider sProvider, String calendarId) throws Exception {
    return storage_.getGroupCalendar(sProvider, calendarId);
  }
  public List<GroupCalendarData> getGroupCalendars(SessionProvider sProvider, String[] groupIds) throws Exception {
    return storage_.getGroupCalendars(sProvider, groupIds);
  }
  public void saveGroupCalendar(SessionProvider sProvider, Calendar calendar, boolean isNew) throws Exception {
    storage_.saveGroupCalendar(sProvider, calendar, isNew) ;
  }
  public Calendar removeGroupCalendar(SessionProvider sProvider, String calendarId) throws Exception {
    return storage_.removeGroupCalendar(sProvider, calendarId);
  }

  public List<EventCategory> getEventCategories(SessionProvider sProvider, String username) throws Exception {
    return storage_.getEventCategories(sProvider, username) ;
  }
  public void saveEventCategory(SessionProvider sProvider, String username, EventCategory eventCategory, EventCategory newEventCategory, boolean isNew) throws Exception {
    storage_.saveEventCategory(sProvider, username, eventCategory, newEventCategory, isNew) ;
  }
  public void removeEventCategory(SessionProvider sProvider, String username, String eventCategoryName) throws Exception {
    storage_.removeEventCategory(sProvider, username,eventCategoryName);
  }  
  /*public void saveGroupEventCategory(Calendar calendar, EventCategory eventCategory, boolean isNew) throws Exception {
    storage_.saveGroupEventCategory(calendar, eventCategory, isNew) ;
  }
  public EventCategory removeGroupEventCategory(String calendarId, String eventCategoryId) throws Exception {
    return storage_.removeGroupEventCategory(calendarId, eventCategoryId);
  }*/


  public CalendarEvent getUserEvent(SessionProvider sProvider, String username, String calendarId, String eventId) throws Exception {
    return storage_.getUserEvent(sProvider, username, calendarId, eventId);
  }
  public List<CalendarEvent> getUserEventByCalendar(SessionProvider sProvider, String username, List<String> calendarIds) throws Exception {
    return storage_.getUserEventByCalendar(sProvider, username, calendarIds);
  }
  public List<CalendarEvent> getUserEvents(SessionProvider sProvider, String username, EventQuery eventQuery) throws Exception {
    return storage_.getUserEvents(sProvider, username, eventQuery) ;
  }
  public void saveUserEvent(SessionProvider sProvider, String username, String calendarId, CalendarEvent event, boolean isNew) throws Exception {
    storage_.saveUserEvent(sProvider, username, calendarId, event, isNew) ;
  }
  public CalendarEvent removeUserEvent(SessionProvider sProvider, String username, String calendarId, String eventId) throws Exception {
    return storage_.removeUserEvent(sProvider, username, calendarId, eventId);
  }


  public CalendarEvent getGroupEvent(SessionProvider sProvider, String calendarId, String eventId) throws Exception {
    return storage_.getGroupEvent(sProvider, calendarId, eventId);
  }  
  public List<CalendarEvent> getGroupEventByCalendar(SessionProvider sProvider, List<String> calendarIds) throws Exception {
    return storage_.getGroupEventByCalendar(sProvider, calendarIds);
  } 
  public List<CalendarEvent> getPublicEvents(SessionProvider sProvider, EventQuery eventQuery) throws Exception {
    return storage_.getPublicEvents(sProvider, eventQuery) ;
  }
  public void saveGroupEvent(SessionProvider sProvider, String calendarId, CalendarEvent event, boolean isNew) throws Exception {
    storage_.saveGroupEvent(sProvider, calendarId, event, isNew) ;
  }  
  public CalendarEvent removeGroupEvent(SessionProvider sProvider, String calendarId, String eventId) throws Exception {
    return storage_.removeGroupEvent(sProvider, calendarId, eventId);
  }

  public CalendarImportExport  getCalendarImportExports(String type) {
    return calendarImportExport_.get(type) ;
  }

  public String[] getExportImportType() throws Exception {
    return calendarImportExport_.keySet().toArray(new String[]{}) ;
  }

  public void saveCalendarSetting(SessionProvider sProvider, String username, CalendarSetting setting) throws Exception {
    storage_.saveCalendarSetting(sProvider, username, setting) ;

  }

  public CalendarSetting getCalendarSetting(SessionProvider sProvider, String username) throws Exception {
    return storage_.getCalendarSetting(sProvider, username) ;
  }

  public void generateRss(SessionProvider sProvider, String username, List<String> calendarIds, RssData rssData) throws Exception {
    storage_.generateRss(sProvider, username, calendarIds, rssData, calendarImportExport_.get(ICALENDAR)) ;
  }

  public List<FeedData> getFeeds(SessionProvider sProvider, String username) throws Exception {
    return storage_.getFeeds(sProvider, username) ;
  }

  public Node getRssHome(SessionProvider sProvider, String username) throws Exception {
    return storage_.getRssHome(sProvider, username) ;
  }
  
  public EventPageList searchEvent(SessionProvider sProvider, String username, EventQuery query, String[] publicCalendarIds)throws Exception {
    return storage_.searchEvent(sProvider, username, query, publicCalendarIds) ;
  }

  public EventCategory getEventCategory(SessionProvider sProvider, String username, String eventCategoryId) throws Exception {
    return storage_.getEventCategory(sProvider, username, eventCategoryId) ;
  }
  
  public Map<Integer, String > searchHightLightEvent(SessionProvider sProvider, String username, EventQuery eventQuery, String[] publicCalendarIds)throws Exception  {
    return storage_.searchHightLightEvent(sProvider, username, eventQuery, publicCalendarIds) ;
  }
  
  public void shareCalendar(SessionProvider sProvider, String username, String calendarId, List<String> receiverUsers) throws Exception {
    storage_.shareCalendar(sProvider, username, calendarId, receiverUsers) ;
  }
  
  public GroupCalendarData getSharedCalendars(SessionProvider sProvider, String username) throws Exception {
    return storage_.getSharedCalendars(sProvider, username) ;
  }
  
  public List<CalendarEvent> getEvent(SessionProvider sProvider, String username, EventQuery eventQuery, String[] publicCalendarIds) throws Exception{
    return storage_.getEvent(sProvider, username, eventQuery, publicCalendarIds) ;
  }
  
  public void removeSharedCalendar(SessionProvider sProvider, String username, String calendarId) throws Exception {
    storage_.removeSharedCalendar(sProvider, username, calendarId) ;
  }
  
  public void saveEventToSharedCalendar(SessionProvider sProvider, String username, String calendarId, CalendarEvent event, boolean isNew) throws Exception  {
    storage_.saveEventToSharedCalendar(sProvider, username, calendarId, event, isNew) ;
  }
}
