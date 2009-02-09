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


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarCategory;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarImportExport;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.CalendarUpdateEventListener;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.service.EventPageList;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.FeedData;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.calendar.service.RssData;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public class CalendarServiceImpl implements CalendarService, Startable {

  final public static String ICALENDAR = "ICalendar(.ics)".intern() ;
  final public static String EXPORTEDCSV = "ExportedCsv(.csv)".intern() ;

  private RepositoryService repositorySerivce_ ;
  private JCRDataStorage storage_ ;
  private Map<String, CalendarImportExport> calendarImportExport_ = new LinkedHashMap<String, CalendarImportExport>() ;
  protected List<CalendarUpdateEventListener> listeners_ = new ArrayList<CalendarUpdateEventListener>(3);

  public CalendarServiceImpl(NodeHierarchyCreator nodeHierarchyCreator) throws Exception {
    storage_ = new JCRDataStorage(nodeHierarchyCreator) ;
    calendarImportExport_.put(ICALENDAR, new ICalendarImportExport(storage_)) ;
    calendarImportExport_.put(EXPORTEDCSV, new CsvImportExport(storage_)) ;
  }

  public List<CalendarCategory> getCategories(String username) throws Exception {
    return storage_.getCategories(username) ;
  }

  public List<GroupCalendarData> getCalendarCategories(String username, boolean isShowAll) throws Exception {
    return storage_.getCalendarCategories(username, isShowAll);
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
  public List<Calendar> getUserCalendars(String username, boolean isShowAll) throws Exception {
    return storage_.getUserCalendars(username, isShowAll) ;
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
  public List<GroupCalendarData> getGroupCalendars(String[] groupIds, boolean isShowAll, String username) throws Exception {
    return storage_.getGroupCalendars(groupIds, isShowAll, username);
  }
  public void savePublicCalendar(Calendar calendar, boolean isNew, String username) throws Exception {
    storage_.savePublicCalendar(calendar, isNew, username) ;
  }
  public Calendar removePublicCalendar(String calendarId) throws Exception {
    return storage_.removeGroupCalendar(calendarId);
  }

  public List<EventCategory> getEventCategories(String username) throws Exception {
    return storage_.getEventCategories(username) ;
  }
  public void saveEventCategory(String username, EventCategory eventCategory, String[] values, boolean isNew) throws Exception {
    storage_.saveEventCategory(username, eventCategory, values, isNew) ;
  }
  public void removeEventCategory(String username, String eventCategoryName) throws Exception {
    storage_.removeEventCategory(username, eventCategoryName);
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
  public void savePublicEvent(String calendarId, CalendarEvent event, boolean isNew) throws Exception {
    storage_.savePublicEvent(calendarId, event, isNew) ;
  }  
  public CalendarEvent removePublicEvent(String calendarId, String eventId) throws Exception {
    return storage_.removePublicEvent(calendarId, eventId);
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

  public int generateRss(String username, List<String> calendarIds, RssData rssData) throws Exception {
    return storage_.generateRss(username, calendarIds, rssData, calendarImportExport_.get(ICALENDAR)) ;
  }
  public int generateCalDav(String username, List<String> calendarIds, RssData rssData) throws Exception {
    return storage_.generateCalDav(username, calendarIds, rssData, calendarImportExport_.get(ICALENDAR)) ;
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

  public Map<Integer, String > searchHightLightEvent(String username, EventQuery eventQuery, String[] publicCalendarIds)throws Exception  {
    return storage_.searchHightLightEvent(username, eventQuery, publicCalendarIds) ;
  }

  public void shareCalendar(String username, String calendarId, List<String> receiverUsers) throws Exception {
    storage_.shareCalendar(username, calendarId, receiverUsers) ;
  }

  public GroupCalendarData getSharedCalendars( String username, boolean isShowAll) throws Exception {
    return storage_.getSharedCalendars(username, isShowAll) ;
  }

  public List<CalendarEvent> getEvents(String username, EventQuery eventQuery, String[] publicCalendarIds) throws Exception{
    return storage_.getEvents(username, eventQuery, publicCalendarIds) ;
  }

  public void removeSharedCalendar(String username, String calendarId) throws Exception {
    storage_.removeSharedCalendar(username, calendarId) ;
  }

  public void saveEventToSharedCalendar(String username, String calendarId, CalendarEvent event, boolean isNew) throws Exception  {
    storage_.saveEventToSharedCalendar(username, calendarId, event, isNew) ;
  }

  public Map<String, String> checkFreeBusy(EventQuery eventQuery) throws Exception {
    return storage_.checkFreeBusy(eventQuery) ;
  }

  public void saveSharedCalendar(String username, Calendar calendar) throws Exception {
    storage_.saveSharedCalendar(username, calendar) ;

  }

  public void removeSharedEvent(String username, String calendarId, String eventId)throws Exception  {
    storage_.removeSharedEvent(username, calendarId, eventId) ;

  }

  public void moveEvent(String formCalendar, String toCalendar, String fromType,String toType, List<CalendarEvent> calEvents, String username) throws Exception {
    storage_.moveEvent(formCalendar,toCalendar, fromType, toType, calEvents, username)  ;
  }

  public void confirmInvitation(String fromUserId, String toUserId,int calType,String calendarId, String eventId, int answer) throws Exception {
    storage_.confirmInvitation(fromUserId, toUserId, calType, calendarId, eventId, answer) ;
  }

  public void start() {
    for (CalendarUpdateEventListener updateListener : listeners_) {
      updateListener.preUpdate() ;
    }
  }

  public void stop() {
    // TODO Auto-generated method stub

  }

  public synchronized void addListenerPlugin(CalendarUpdateEventListener listener) throws Exception {
	  listeners_.add(listener) ;
  }
}
