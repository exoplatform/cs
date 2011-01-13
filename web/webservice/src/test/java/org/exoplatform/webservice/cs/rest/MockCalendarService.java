/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.webservice.cs.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.jcr.Node;

import org.apache.commons.httpclient.Credentials;
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
import org.exoplatform.calendar.service.RemoteCalendarService;
import org.exoplatform.calendar.service.RssData;
import org.exoplatform.calendar.service.impl.CalendarEventListener;
import org.exoplatform.calendar.service.impl.CsvImportExport;
import org.exoplatform.calendar.service.impl.ICalendarImportExport;
import org.exoplatform.calendar.service.impl.JCRDataStorage;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Mar 3, 2010  
 */
public class MockCalendarService implements CalendarService{

  private Calendar cal_;
  private Map<String, List<CalendarEvent>> data_;
  private Map<String, CalendarImportExport>   calendarImportExport_ = new LinkedHashMap<String, CalendarImportExport>();

  public MockCalendarService() throws Exception{
    calendarImportExport_.put(CalendarService.ICALENDAR, new ICalendarImportExport(new JCRDataStorage(null, null)));
    calendarImportExport_.put(CalendarService.EXPORTEDCSV, new CsvImportExport(new JCRDataStorage(null, null)));
  }

  @Override
  public void addListenerPlugin(CalendarUpdateEventListener listener) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public Map<String, String> checkFreeBusy(EventQuery eventQuery) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void confirmInvitation(String fromUserId,
                                String toUserId,
                                int calType,
                                String calendarId,
                                String eventId,
                                int answer) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void confirmInvitation(String fromUserId,
                                String confirmingEmail,
                                String confirmingUser,
                                int calType,
                                String calendarId,
                                String eventId,
                                int answer) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public int generateCalDav(String username,
                            LinkedHashMap<String, Calendar> calendars,
                            RssData rssData) throws Exception {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int generateCalDav(String username, List<String> calendarIds, RssData rssData) throws Exception {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int generateRss(String username, LinkedHashMap<String, Calendar> calendars, RssData rssData) throws Exception {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int generateRss(String username, List<String> calendarIds, RssData rssData) throws Exception {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public List<GroupCalendarData> getCalendarCategories(String username, boolean isShowAll) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public CalendarCategory getCalendarCategory(String username, String calendarCategoryId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public CalendarImportExport getCalendarImportExports(String type) {
    // TODO Auto-generated method stub
    return calendarImportExport_.get(type);
  }

  @Override
  public CalendarSetting getCalendarSetting(String username) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<CalendarCategory> getCategories(String username) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public CalendarEvent getEvent(String username, String eventId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<EventCategory> getEventCategories(String username) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public EventCategory getEventCategory(String username, String eventCategoryId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public EventCategory getEventCategoryByName(String username, String eventCategoryName) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<CalendarEvent> getEvents(String username,
                                       EventQuery eventQuery,
                                       String[] publicCalendarIds) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String[] getExportImportType() throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<FeedData> getFeeds(String username) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Calendar getGroupCalendar(String calendarId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<GroupCalendarData> getGroupCalendars(String[] groupIds,
                                                   boolean isShowAll,
                                                   String username) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public CalendarEvent getGroupEvent(String calendarId, String eventId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<CalendarEvent> getGroupEventByCalendar(List<String> calendarIds) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<CalendarEvent> getPublicEvents(EventQuery eventQuery) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Node getRssHome(String username) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public GroupCalendarData getSharedCalendars(String username, boolean isShowAll) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int getTypeOfCalendar(String userName, String calendarId) throws Exception {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public Calendar getUserCalendar(String username, String calendarId) throws Exception {
    // TODO Auto-generated method stub
    return cal_;
  }

  @Override
  public List<Calendar> getUserCalendars(String username, boolean isShowAll) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Calendar> getUserCalendarsByCategory(String username, String calendarCategoryId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<CalendarEvent> getUserEventByCalendar(String username, List<String> calendarIds) throws Exception {
    // TODO Auto-generated method stub
    return data_.get(calendarIds.get(0));
  }

  @Override
  public List<CalendarEvent> getUserEvents(String username, EventQuery eventQuery) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void moveEvent(String formCalendar,
                        String toCalendar,
                        String formType,
                        String toType,
                        List<CalendarEvent> calEvents,
                        String username) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public CalendarCategory removeCalendarCategory(String username, String calendarCategoryId) throws Exception {
    System.out.println("\n\n Data clean up");
    data_.clear();
    return null;
  }

  @Override
  public void removeEventCategory(String username, String eventCategoryName) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public Calendar removePublicCalendar(String calendarId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public CalendarEvent removePublicEvent(String calendarId, String eventId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void removeSharedCalendar(String username, String calendarId) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void removeSharedEvent(String username, String calendarId, String eventId) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public Calendar removeUserCalendar(String username, String calendarId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public CalendarEvent removeUserEvent(String username, String calendarId, String eventId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void saveCalendarCategory(String username, CalendarCategory calendarCategory, boolean isNew) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void saveCalendarSetting(String username, CalendarSetting setting) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void saveEventCategory(String username, EventCategory eventCategory, boolean isNew) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void saveEventToSharedCalendar(String username,
                                        String calendarId,
                                        CalendarEvent event,
                                        boolean isNew) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void savePublicCalendar(Calendar calendar, boolean isNew, String username) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void savePublicEvent(String calendarId, CalendarEvent event, boolean isNew) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void saveSharedCalendar(String username, Calendar calendar) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void saveUserCalendar(String username, Calendar calendar, boolean isNew) throws Exception {
    cal_ = calendar;
    List<CalendarEvent> events = new ArrayList<CalendarEvent>();
    data_ = new HashMap<String, List<CalendarEvent>>();
    data_.put(cal_.getId(), events);
    System.out.println("\n\n calendar Saved");
    // TODO Auto-generated method stub

  }

  @Override
  public void saveUserEvent(String username, String calendarId, CalendarEvent event, boolean isNew) throws Exception {

    data_.get(cal_.getId()).add(event);
    // TODO Auto-generated method stub

  }

  @Override
  public EventPageList searchEvent(String username,
                                   EventQuery eventQuery,
                                   String[] publicCalendarIds) throws Exception {
    if(data_ != null && cal_ != null && data_.get(cal_.getId()) != null)
      if(eventQuery.getEventType().equals(CalendarEvent.TYPE_EVENT)) {
        return new EventPageList(data_.get(cal_.getId()),0);
      } else if(eventQuery.getEventType().equals(CalendarEvent.TYPE_TASK)) {
        return new EventPageList(data_.get(cal_.getId()),0);
      }

    return null;
  }

  @Override
  public Map<Integer, String> searchHightLightEvent(String username,
                                                    EventQuery eventQuery,
                                                    String[] publicCalendarIds) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void shareCalendar(String username, String calendarId, List<String> receiverUsers) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void updateCalDav(String usename, String calendarId, CalendarImportExport imp) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void updateCalDav(String usename, String calendarId, CalendarImportExport imp, int number) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void updateRss(String usename, String calendarId, CalendarImportExport imp) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void updateRss(String usename, String calendarId, CalendarImportExport imp, int number) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public List<CalendarEvent> getSharedEventByCalendars(String username, List<String> calendarIds) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void removeFeedData(String username, String title) {
    // TODO Auto-generated method stub

  }
  @Override
  public ResourceBundle getResourceBundle() throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public void initNewUser(String userName, CalendarSetting defaultCalendarSetting)
  throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void addEventListenerPlugin(CalendarEventListener listener) throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void savePublicCalendar(Calendar calendar, boolean isNew) throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void assignGroupTask(String taskId, String calendarId, String assignee) throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void setGroupTaskStatus(String taskId, String calendarId, String status) throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public CalendarEvent getGroupEvent(String eventId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isRemoteCalendar(String username, String calendarId) throws Exception {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Calendar importCalDavCalendar(String username,
                           String calDavUrl,
                           String calendarName,
                           String syncPeriod,
                           Credentials credentials) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Calendar importRemoteIcs(String username,
                              String icalUrl,
                              String calendarName,
                              String syncPeriod, Credentials credentials) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Calendar refreshRemoteCalendar(String username, String remoteCalendarId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getRemoteCalendarUrl(String owner, String calendarId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getRemoteCalendarType(String owner, String calendarId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getRemoteCalendarUsername(String owner, String calendarId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getRemoteCalendarPassword(String owner, String calendarId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getRemoteCalendarSyncPeriod(String owner, String calendarId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public java.util.Calendar getRemoteCalendarLastUpdated(String owner, String calendarId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Calendar updateRemoteCalendarInfo(String username,
                                           String calendarId,
                                           String remoteUrl,
                                           String calendarName,
                                           String description,
                                           String syncPeriod,
                                           String remoteUser,
                                           String remotePassword) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isValidRemoteUrl(String url, String type) throws Exception {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isValidRemoteUrl(String url, String type, String remoteUser, String remotePassword) throws Exception {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public RemoteCalendarService getRemoteCalendarService() throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setRemoteCalendarLastUpdated(String owner,
                                           String calendarId,
                                           java.util.Calendar timeGMT) throws Exception {
    // TODO Auto-generated method stub
    
  }
}
