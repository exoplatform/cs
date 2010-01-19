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
package org.exoplatform.calendar.service;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jan 19, 2010  
 */
public interface DataStorage {

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#getPublicCalendarServiceHome(org.exoplatform.services.jcr.ext.common.SessionProvider)
   */
  @Deprecated
  public Node getPublicCalendarServiceHome(SessionProvider sProvider) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#getPublicCalendarServiceHome()
   */
  public Node getPublicCalendarServiceHome() throws Exception;

  /**
   * @deprecated Use {@link #getSharedCalendarHome()}
   */
  @Deprecated
  public Node getSharedCalendarHome(SessionProvider sProvider) throws Exception;

  public Node getSharedCalendarHome() throws Exception;

  /**
   * @deprecated Use {@link #getPublicRoot()}
   */
  public Node getPublicRoot(SessionProvider sysProvider) throws Exception;

  public Node getPublicRoot() throws Exception;

  /**
   * @deprecated Use {@link #getUserCalendarServiceHome(String)}
   */
  @Deprecated
  public Node getUserCalendarServiceHome(SessionProvider removeme, String username) throws Exception;

  /**
   * Get the Calendar application user data storage root
   * @param username
   * @return the node that is on top of user data storage
   * @throws Exception
   */
  public Node getUserCalendarServiceHome(String username) throws Exception;

  /**
   * @deprecated User {@link #getPublicCalendarHome()}
   */
  @Deprecated
  public Node getPublicCalendarHome(SessionProvider sProvider) throws Exception;

  public Node getPublicCalendarHome() throws Exception;

  public Node getUserCalendarHome(String username) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#getRssHome(java.lang.String)
   */
  public Node getRssHome(String username) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#getUserCalendar(java.lang.String, java.lang.String)
   */
  public Calendar getUserCalendar(String username, String calendarId) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#getUserCalendars(java.lang.String, boolean)
   */
  public List<Calendar> getUserCalendars(String username, boolean isShowAll) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#getUserCalendarsByCategory(java.lang.String, java.lang.String)
   */
  public List<Calendar> getUserCalendarsByCategory(String username, String calendarCategoryId) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#saveUserCalendar(java.lang.String, org.exoplatform.calendar.service.Calendar, boolean)
   */
  public void saveUserCalendar(String username, Calendar calendar, boolean isNew) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#removeUserCalendar(java.lang.String, java.lang.String)
   */
  public Calendar removeUserCalendar(String username, String calendarId) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#getGroupCalendar(java.lang.String)
   */
  public Calendar getGroupCalendar(String calendarId) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#getGroupCalendars(java.lang.String[], boolean, java.lang.String)
   */
  public List<GroupCalendarData> getGroupCalendars(String[] groupIds,
                                                   boolean isShowAll,
                                                   String username) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#savePublicCalendar(org.exoplatform.calendar.service.Calendar, boolean, java.lang.String)
   */
  public void savePublicCalendar(Calendar calendar, boolean isNew, String username) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#removeGroupCalendar(java.lang.String)
   */
  public Calendar removeGroupCalendar(String calendarId) throws Exception;

  public Calendar getCalendar(String[] defaultFilterCalendars,
                              String username,
                              Node calNode,
                              boolean isShowAll) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#getCalendarCategories(java.lang.String, boolean)
   */
  public List<GroupCalendarData> getCalendarCategories(String username, boolean isShowAll) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#getCategories(java.lang.String)
   */
  public List<CalendarCategory> getCategories(String username) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#getCalendarCategory(java.lang.String, java.lang.String)
   */
  public CalendarCategory getCalendarCategory(String username, String calendarCategoryId) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#saveCalendarCategory(java.lang.String, org.exoplatform.calendar.service.CalendarCategory, boolean)
   */
  public void saveCalendarCategory(String username, CalendarCategory calendarCategory, boolean isNew) throws Exception;

  public void reparePermissions(Node node, String owner) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#removeCalendarCategory(java.lang.String, java.lang.String)
   */
  public CalendarCategory removeCalendarCategory(String username, String calendarCategoryId) throws Exception;

  public CalendarCategory getCalendarCategory(Node calCategoryNode) throws Exception;

  //Event Category APIs
  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#getEventCategories(java.lang.String)
   */
  public List<EventCategory> getEventCategories(String username) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#saveEventCategory(java.lang.String, org.exoplatform.calendar.service.EventCategory, java.lang.String[], boolean)
   */
  public void saveEventCategory(String username,
                                EventCategory eventCategory,
                                String[] values,
                                boolean isNew) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#removeEventCategory(java.lang.String, java.lang.String)
   */
  public void removeEventCategory(String username, String eventCategoryName) throws Exception;

  public EventCategory getEventCategory(Node eventCatNode) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#getEventCategory(java.lang.String, java.lang.String)
   */
  public EventCategory getEventCategory(String username, String eventCategoryName) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#getUserEvent(java.lang.String, java.lang.String, java.lang.String)
   */
  public CalendarEvent getUserEvent(String username, String calendarId, String eventId) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#getUserEventByCalendar(java.lang.String, java.util.List)
   */
  public List<CalendarEvent> getUserEventByCalendar(String username, List<String> calendarIds) throws Exception;

  public List<CalendarEvent> getPublicEventByCategory(String username, String eventCategoryId) throws Exception;

  public List<CalendarEvent> getSharedEventByCategory(String username, String eventCategoryId) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#getUserEventByCategory(java.lang.String, java.lang.String)
   */
  public List<CalendarEvent> getUserEventByCategory(String username, String eventCategoryId) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#getEvent(java.lang.String, java.lang.String)
   */
  public CalendarEvent getEvent(String username, String eventId) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#getUserEvents(java.lang.String, org.exoplatform.calendar.service.EventQuery)
   */
  public List<CalendarEvent> getUserEvents(String username, EventQuery eventQuery) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#saveUserEvent(java.lang.String, java.lang.String, org.exoplatform.calendar.service.CalendarEvent, boolean)
   */
  public void saveUserEvent(String username, String calendarId, CalendarEvent event, boolean isNew) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#removeUserEvent(java.lang.String, java.lang.String, java.lang.String)
   */
  public CalendarEvent removeUserEvent(String username, String calendarId, String eventId) throws Exception;

  public void removeReminder(Node eventNode) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#getGroupEvent(java.lang.String, java.lang.String)
   */
  public CalendarEvent getGroupEvent(String calendarId, String eventId) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#getGroupEventByCalendar(java.util.List)
   */
  public List<CalendarEvent> getGroupEventByCalendar(List<String> calendarIds) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#getPublicEvents(org.exoplatform.calendar.service.EventQuery)
   */
  public List<CalendarEvent> getPublicEvents(EventQuery eventQuery) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#savePublicEvent(java.lang.String, org.exoplatform.calendar.service.CalendarEvent, boolean)
   */
  public void savePublicEvent(String calendarId, CalendarEvent event, boolean isNew) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#removePublicEvent(java.lang.String, java.lang.String)
   */
  public CalendarEvent removePublicEvent(String calendarId, String eventId) throws Exception;

  public CalendarEvent getEvent(Node eventNode) throws Exception;

  public void saveEvent(Node calendarNode, CalendarEvent event, Node reminderFolder, boolean isNew) throws Exception;

  public void addReminder(Node eventNode, Node reminderFolder, Reminder reminder) throws Exception;

  public void addEvent(CalendarEvent event) throws Exception;

  public void syncRemoveEvent(Node eventFolder, String rootEventId) throws Exception;

  public Node getReminderFolder(Date fromDate) throws Exception;

  public Node getEventFolder(SessionProvider provider, Date fromDate) throws Exception;

  public Node getEventFolder(Date fromDate) throws Exception;

  public Node getDateFolder(Node publicApp, Date date) throws Exception;

  public List<Reminder> getReminders(Node eventNode) throws Exception;

  public void addAttachment(Node eventNode, Attachment attachment, boolean isNew) throws Exception;

  public List<Attachment> getAttachments(Node eventNode) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#saveCalendarSetting(java.lang.String, org.exoplatform.calendar.service.CalendarSetting)
   */
  public void saveCalendarSetting(String username, CalendarSetting setting) throws Exception;

  public void addCalendarSetting(Node calendarHome, CalendarSetting setting) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#getCalendarSetting(java.lang.String)
   */
  public CalendarSetting getCalendarSetting(String username) throws Exception;

  public void storeXML(String feedXML, Node rssHome, String rssNodeName, RssData rssData) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#generateCalDav(java.lang.String, java.util.LinkedHashMap, org.exoplatform.calendar.service.RssData, org.exoplatform.calendar.service.CalendarImportExport)
   */
  public int generateCalDav(String username,
                            LinkedHashMap<String, Calendar> calendars,
                            RssData rssData,
                            CalendarImportExport importExport) throws Exception;

  public void removeFeed(String username, String calendarId) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#getFeeds(java.lang.String)
   */
  public List<FeedData> getFeeds(String username) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#generateRss(java.lang.String, java.util.List, org.exoplatform.calendar.service.RssData, org.exoplatform.calendar.service.CalendarImportExport)
   */
  public int generateRss(String username,
                         List<String> calendarIds,
                         RssData rssData,
                         CalendarImportExport importExport) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#generateRss(java.lang.String, java.util.LinkedHashMap, org.exoplatform.calendar.service.RssData, org.exoplatform.calendar.service.CalendarImportExport)
   */
  public int generateRss(String username,
                         LinkedHashMap<String, Calendar> calendars,
                         RssData rssData,
                         CalendarImportExport importExport) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#updateRss(java.lang.String, java.lang.String, org.exoplatform.calendar.service.CalendarImportExport)
   */
  public void updateRss(String username, String calendarId, CalendarImportExport imp) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#updateRss(java.lang.String, java.lang.String, org.exoplatform.calendar.service.CalendarImportExport, int)
   */
  public void updateRss(String username, String calendarId, CalendarImportExport imp, int number) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#updateCalDav(java.lang.String, java.lang.String, org.exoplatform.calendar.service.CalendarImportExport)
   */
  public void updateCalDav(String username, String calendarId, CalendarImportExport imp) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#updateCalDav(java.lang.String, java.lang.String, org.exoplatform.calendar.service.CalendarImportExport, int)
   */
  public void updateCalDav(String username, String calendarId, CalendarImportExport imp, int number) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#generateCalDav(java.lang.String, java.util.List, org.exoplatform.calendar.service.RssData, org.exoplatform.calendar.service.CalendarImportExport)
   */
  public int generateCalDav(String username,
                            List<String> calendarIds,
                            RssData rssData,
                            CalendarImportExport importExport) throws Exception;

  public String getEntryUrl(String portalName,
                            String wsName,
                            String username,
                            String path,
                            String baseUrl) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#searchEvent(java.lang.String, org.exoplatform.calendar.service.EventQuery, java.lang.String[])
   */
  public EventPageList searchEvent(String username,
                                   EventQuery eventQuery,
                                   String[] publicCalendarIds) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#searchHightLightEvent(java.lang.String, org.exoplatform.calendar.service.EventQuery, java.lang.String[])
   */
  public Map<Integer, String> searchHightLightEvent(String username,
                                                    EventQuery eventQuery,
                                                    String[] publicCalendarIds) throws Exception;

  public Map<Integer, String> updateMap(Map<Integer, String> data,
                                        NodeIterator it,
                                        java.util.Calendar fromDate,
                                        java.util.Calendar toDate,
                                        String[] filterCalIds) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#shareCalendar(java.lang.String, java.lang.String, java.util.List)
   */
  public void shareCalendar(String username, String calendarId, List<String> receiverUsers) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#getSharedCalendars(java.lang.String, boolean)
   */
  public GroupCalendarData getSharedCalendars(String username, boolean isShowAll) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#saveSharedCalendar(java.lang.String, org.exoplatform.calendar.service.Calendar)
   */
  public void saveSharedCalendar(String username, Calendar calendar) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#getSharedEvents(java.lang.String, org.exoplatform.calendar.service.EventQuery)
   */
  public List<CalendarEvent> getSharedEvents(String username, EventQuery eventQuery) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#getSharedEventByCalendars(java.lang.String, java.util.List)
   */
  public List<CalendarEvent> getSharedEventByCalendars(String username, List<String> calendarIds) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#removeSharedCalendar(java.lang.String, java.lang.String)
   */
  public void removeSharedCalendar(String username, String calendarId) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#saveEventToSharedCalendar(java.lang.String, java.lang.String, org.exoplatform.calendar.service.CalendarEvent, boolean)
   */
  public void saveEventToSharedCalendar(String username,
                                        String calendarId,
                                        CalendarEvent event,
                                        boolean isNew) throws Exception;

  public boolean canEdit(Node calNode, String username) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#getEvents(java.lang.String, org.exoplatform.calendar.service.EventQuery, java.lang.String[])
   */
  public List<CalendarEvent> getEvents(String username,
                                       EventQuery eventQuery,
                                       String[] publicCalendarIds) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#checkFreeBusy(org.exoplatform.calendar.service.EventQuery)
   */
  public Map<String, String> checkFreeBusy(EventQuery eventQuery) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#removeSharedEvent(java.lang.String, java.lang.String, java.lang.String)
   */
  public void removeSharedEvent(String username, String calendarId, String eventId) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#moveEvent(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.List, java.lang.String)
   */
  public void moveEvent(String formCalendar,
                        String toCalendar,
                        String fromType,
                        String toType,
                        List<CalendarEvent> calEvents,
                        String username) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#confirmInvitation(java.lang.String, java.lang.String, int, java.lang.String, java.lang.String, int)
   */
  public void confirmInvitation(String fromUserId,
                                String toUserId,
                                int calType,
                                String calendarId,
                                String eventId,
                                int answer) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#confirmInvitation(java.lang.String, java.lang.String, java.lang.String, int, java.lang.String, java.lang.String, int)
   */
  public void confirmInvitation(String fromUserId,
                                String confirmingEmail,
                                String confirmingUser,
                                int calType,
                                String calendarId,
                                String eventId,
                                int answer) throws Exception;

  /* (non-Javadoc)
   * @see org.exoplatform.calendar.service.impl.DataStorage#getTypeOfCalendar(java.lang.String, java.lang.String)
   */
  public int getTypeOfCalendar(String userName, String calendarId);

  /**
   * Create a session provider for current context. The method first try to get a normal session provider, 
   * then attempts to create a system provider if the first one was not available.
   * @return a SessionProvider initialized by current SessionProviderService
   * @see SessionProviderService#getSessionProvider(null)
   */
  public SessionProvider createSessionProvider();

  public SessionProvider createUserProvider();

  public SessionProvider createSystemProvider();

  /**
   * Safely closes JCR session provider. Call this method in finally to clean any provider initialized by createSessionProvider()
   * @param sessionProvider the sessionProvider to close
   * @see SessionProvider#close();
   */
  public void closeSessionProvider(SessionProvider sessionProvider);

  public Node getNodeByPath(String nodePath, SessionProvider sessionProvider) throws Exception;

  public Session getSession(SessionProvider sprovider) throws Exception;

}
