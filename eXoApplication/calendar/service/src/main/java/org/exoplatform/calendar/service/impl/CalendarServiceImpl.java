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

import sun.util.calendar.CalendarUtils;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public class CalendarServiceImpl implements CalendarService{

  final public static String ICALENDAR = "ICalendar(.ics)".intern() ;
  final public static String EXPORTEDCSV = "ExportedCsv(.csv)".intern() ;
  
  private JCRDataStorage storage_ ;
  private Map<String, CalendarImportExport> calendarImportExport_ = new HashMap<String, CalendarImportExport>() ;

  public CalendarServiceImpl(NodeHierarchyCreator nodeHierarchyCreator) throws Exception {
    storage_ = new JCRDataStorage(nodeHierarchyCreator) ;
    calendarImportExport_.put(ICALENDAR, new ICalendarImportExport(storage_)) ;
  }

  public List<CalendarCategory> getCategories(SessionProvider sProvider, String username) throws Exception {
    return storage_.getCategories(sProvider, username) ;
  }

  public List<GroupCalendarData> getCalendarCategories(SessionProvider sProvider, String username, boolean isShowAll) throws Exception {
    return storage_.getCalendarCategories(sProvider, username, isShowAll);
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
  public List<Calendar> getUserCalendars(SessionProvider sProvider, String username, boolean isShowAll) throws Exception {
    return storage_.getUserCalendars(sProvider, username, isShowAll) ;
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
  public List<GroupCalendarData> getGroupCalendars(SessionProvider sProvider, String[] groupIds, boolean isShowAll, String username) throws Exception {
    return storage_.getGroupCalendars(sProvider, groupIds, isShowAll, username);
  }
  public void savePublicCalendar(SessionProvider sProvider, Calendar calendar, boolean isNew, String username) throws Exception {
    storage_.savePublicCalendar(sProvider, calendar, isNew, username) ;
  }
  public Calendar removePublicCalendar(SessionProvider sProvider, String calendarId) throws Exception {
    return storage_.removeGroupCalendar(sProvider, calendarId);
  }

  public List<EventCategory> getEventCategories(SessionProvider sProvider, String username) throws Exception {
    return storage_.getEventCategories(sProvider, username) ;
  }
  public void saveEventCategory(SessionProvider sProvider, String username, EventCategory eventCategory, String[] values, boolean isNew) throws Exception {
    storage_.saveEventCategory(sProvider, username, eventCategory, values, isNew) ;
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


  /*public CalendarEvent getUserEvent(SessionProvider sProvider, String username, String calendarId, String eventId) throws Exception {
    return storage_.getUserEvent(sProvider, username, calendarId, eventId);
  }*/
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
  public void savePublicEvent(SessionProvider sProvider, String calendarId, CalendarEvent event, boolean isNew) throws Exception {
    storage_.savePublicEvent(sProvider, calendarId, event, isNew) ;
  }  
  public CalendarEvent removePublicEvent(SessionProvider sProvider, String calendarId, String eventId) throws Exception {
    return storage_.removePublicEvent(sProvider, calendarId, eventId);
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
  public void generateCalDav(SessionProvider sProvider, String username, List<String> calendarIds, RssData rssData) throws Exception {
    storage_.generateCalDav(sProvider, username, calendarIds, rssData, calendarImportExport_.get(ICALENDAR)) ;
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
  
  public GroupCalendarData getSharedCalendars(SessionProvider sProvider, String username, boolean isShowAll) throws Exception {
    return storage_.getSharedCalendars(sProvider, username, isShowAll) ;
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
  
  public Map<String, String> checkFreeBusy(SessionProvider sysProvider, EventQuery eventQuery) throws Exception {
  	return storage_.checkFreeBusy(sysProvider, eventQuery) ;
  }

  public void saveSharedCalendar(SessionProvider sProvider, String username, Calendar calendar) throws Exception {
    storage_.saveSharedCalendar(sProvider, username, calendar) ;
    
  }

  public void removeSharedEvent(SessionProvider sessionProvider, String username, String calendarId, String eventId)throws Exception  {
    storage_.removeSharedEvent(sessionProvider, username, calendarId, eventId) ;
    
  }

  public boolean hasEditPermission(SessionProvider sProvider, String sharedCalendarId, String username) throws Exception {
    return storage_.hasEditPermission(sProvider, sharedCalendarId, username);
  }
  public void moveEvent(SessionProvider sProvider, String formCalendar, String toCalendar,String fromType, String toType, List<CalendarEvent> calEvents, String username) throws Exception {
    storage_.moveEvent(sProvider, formCalendar,toCalendar, fromType, toType, calEvents, username)  ;
  }

  public void confirmInvitation(String fromUserId, String toUserId,int calType,String calendarId, String eventId, int answer) throws Exception {
    storage_.confirmInvitation(fromUserId, toUserId, calType, calendarId, eventId, answer) ;
  }
}
