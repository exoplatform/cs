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
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.jcr.ItemExistsException;
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
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.resources.ResourceBundleService;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SARL Author : Hung Nguyen Quang
 * hung.nguyen@exoplatform.com Jul 11, 2007
 */
public class CalendarServiceImpl implements CalendarService, Startable {

  final public static String                  ICALENDAR             = "ICalendar(.ics)".intern();
  final public static String                  EXPORTEDCSV           = "ExportedCsv(.csv)".intern();


  private ResourceBundleService rbs_ ;
  private JCRDataStorage                      storage_;
  private Map<String, CalendarImportExport>   calendarImportExport_ = new LinkedHashMap<String, CalendarImportExport>();
  protected List<CalendarUpdateEventListener> listeners_            = new ArrayList<CalendarUpdateEventListener>(3);
  public CalendarServiceImpl(NodeHierarchyCreator nodeHierarchyCreator, RepositoryService reposervice, ResourceBundleService rbs) throws Exception {
    storage_ = new JCRDataStorage(nodeHierarchyCreator, reposervice);
    calendarImportExport_.put(ICALENDAR, new ICalendarImportExport(storage_));
    calendarImportExport_.put(EXPORTEDCSV, new CsvImportExport(storage_));
    rbs_ = rbs;
  }

  /**
   * {@inheritDoc}
   */
  public List<CalendarCategory> getCategories(String username) throws Exception {
    return storage_.getCategories(username);
  }

  /**
   * {@inheritDoc}
   */
  public List<GroupCalendarData> getCalendarCategories(String username, boolean isShowAll) throws Exception {
    return storage_.getCalendarCategories(username, isShowAll);
  }

  /**
   * {@inheritDoc}
   */
  public CalendarCategory getCalendarCategory(String username, String calendarCategoryId) throws Exception {
    return storage_.getCalendarCategory(username, calendarCategoryId);
  }

  /**
   * {@inheritDoc}
   */
  public void saveCalendarCategory(String username, CalendarCategory calendarCategory, boolean isNew) throws Exception {
    storage_.saveCalendarCategory(username, calendarCategory, isNew);
  }

  /**
   * {@inheritDoc}
   */
  public CalendarCategory removeCalendarCategory(String username, String calendarCategoryId) throws Exception {
    return storage_.removeCalendarCategory(username, calendarCategoryId);
  }

  /**
   * {@inheritDoc}
   */
  public Calendar getUserCalendar(String username, String calendarId) throws Exception {
    return storage_.getUserCalendar(username, calendarId);
  }

  /**
   * {@inheritDoc}
   */
  public List<Calendar> getUserCalendars(String username, boolean isShowAll) throws Exception {
    return storage_.getUserCalendars(username, isShowAll);
  }

  /**
   * {@inheritDoc}
   */
  public List<Calendar> getUserCalendarsByCategory(String username, String calendarCategoryId) throws Exception {
    return storage_.getUserCalendarsByCategory(username, calendarCategoryId);
  }

  /**
   * {@inheritDoc}
   */
  public void saveUserCalendar(String username, Calendar calendar, boolean isNew) throws Exception {
    storage_.saveUserCalendar(username, calendar, isNew);
  }

  /**
   * {@inheritDoc}
   */
  public Calendar removeUserCalendar(String username, String calendarId) throws Exception {
    return storage_.removeUserCalendar(username, calendarId);
  }

  /**
   * {@inheritDoc}
   */
  public Calendar getGroupCalendar(String calendarId) throws Exception {
    return storage_.getGroupCalendar(calendarId);
  }

  /**
   * {@inheritDoc}
   */
  public List<GroupCalendarData> getGroupCalendars(String[] groupIds,
                                                   boolean isShowAll,
                                                   String username) throws Exception {
    return storage_.getGroupCalendars(groupIds, isShowAll, username);
  }

  /**
   * {@inheritDoc}
   */
  public void savePublicCalendar(Calendar calendar, boolean isNew, String username) throws Exception {
    storage_.savePublicCalendar(calendar, isNew, username);
  }

  /**
   * {@inheritDoc}
   */
  public Calendar removePublicCalendar(String calendarId) throws Exception {
    return storage_.removeGroupCalendar(calendarId);
  }

  /**
   * {@inheritDoc}
   */
  public List<EventCategory> getEventCategories(String username) throws Exception {
    return storage_.getEventCategories(username);
  }

  /**
   * {@inheritDoc}
   */
  public void saveEventCategory(String username,
                                EventCategory eventCategory,
                                boolean isNew) throws Exception {
    EventCategory ev = getEventCategoryByName(username, eventCategory.getName());
    if (ev != null && (isNew || !ev.getId().equals(eventCategory.getId()))) throw new ItemExistsException(); 
    storage_.saveEventCategory(username, eventCategory, isNew);
  }
  
  /**
   * {@inheritDoc}
   */
  public void saveEventCategory(String username,
                                EventCategory eventCategory,
                                String[] values,
                                boolean isNew) throws Exception {
    saveEventCategory(username, eventCategory, isNew);
  }

  /**
   * {@inheritDoc}
   */
  public void removeEventCategory(String username, String eventCategoryName) throws Exception {
    storage_.removeEventCategory(username, eventCategoryName);
  }

  /**
   * {@inheritDoc}
   */
  public List<CalendarEvent> getUserEventByCalendar(String username, List<String> calendarIds) throws Exception {
    return storage_.getUserEventByCalendar(username, calendarIds);
  }

  /**
   * {@inheritDoc}
   */
  public List<CalendarEvent> getUserEvents(String username, EventQuery eventQuery) throws Exception {
    return storage_.getUserEvents(username, eventQuery);
  }

  public CalendarEvent getEvent(String username, String eventId) throws Exception {
    return storage_.getEvent(username, eventId) ;
  }

  /**
   * {@inheritDoc}
   */
  public void saveUserEvent(String username, String calendarId, CalendarEvent event, boolean isNew) throws Exception {
    storage_.saveUserEvent(username, calendarId, event, isNew);
  }

  /**
   * {@inheritDoc}
   */
  public CalendarEvent removeUserEvent(String username, String calendarId, String eventId) throws Exception {
    return storage_.removeUserEvent(username, calendarId, eventId);
  }

  /**
   * {@inheritDoc}
   */
  public CalendarEvent getGroupEvent(String calendarId, String eventId) throws Exception {
    return storage_.getGroupEvent(calendarId, eventId);
  }

  /**
   * {@inheritDoc}
   */
  public List<CalendarEvent> getGroupEventByCalendar(List<String> calendarIds) throws Exception {
    return storage_.getGroupEventByCalendar(calendarIds);
  }

  /**
   * {@inheritDoc}
   */
  public List<CalendarEvent> getPublicEvents(EventQuery eventQuery) throws Exception {
    return storage_.getPublicEvents(eventQuery);
  }

  /**
   * {@inheritDoc}
   */
  public void savePublicEvent(String calendarId, CalendarEvent event, boolean isNew) throws Exception {
    storage_.savePublicEvent(calendarId, event, isNew);
  }

  /**
   * {@inheritDoc}
   */
  public CalendarEvent removePublicEvent(String calendarId, String eventId) throws Exception {
    return storage_.removePublicEvent(calendarId, eventId);
  }

  /**
   * {@inheritDoc}
   */
  public CalendarImportExport getCalendarImportExports(String type) {
    return calendarImportExport_.get(type);
  }

  /**
   * {@inheritDoc}
   */
  public String[] getExportImportType() throws Exception {
    return calendarImportExport_.keySet().toArray(new String[] {});
  }

  /**
   * {@inheritDoc}
   */
  public void saveCalendarSetting(String username, CalendarSetting setting) throws Exception {
    storage_.saveCalendarSetting(username, setting);
  }

  /**
   * {@inheritDoc}
   */
  public CalendarSetting getCalendarSetting(String username) throws Exception {
    return storage_.getCalendarSetting(username);
  }

  /**
   * {@inheritDoc}
   */
  public int generateRss(String username, LinkedHashMap<String, Calendar> calendars, RssData rssData) throws Exception {
    return storage_.generateRss(username,
                                calendars,
                                rssData,
                                calendarImportExport_.get(ICALENDAR));
  }

  /**
   * {@inheritDoc}
   */
  public int generateCalDav(String username, LinkedHashMap<String, Calendar> calendars, RssData rssData) throws Exception {
    return storage_.generateCalDav(username,
                                   calendars,
                                   rssData,
                                   calendarImportExport_.get(ICALENDAR));
  }

  /**
   * {@inheritDoc}
   */
  public List<FeedData> getFeeds(String username) throws Exception {
    return storage_.getFeeds(username);
  }

  /**
   * {@inheritDoc}
   */
  public Node getRssHome(String username) throws Exception {
    return storage_.getRssHome(username);
  }

  /**
   * {@inheritDoc}
   */
  public EventPageList searchEvent(String username, EventQuery query, String[] publicCalendarIds) throws Exception {
    return storage_.searchEvent(username, query, publicCalendarIds);
  }

  /**
   * {@inheritDoc}
   */
  public EventCategory getEventCategory(String username, String eventCategoryId) throws Exception {
    return storage_.getEventCategory(username, eventCategoryId);
  }

  /**
   * {@inheritDoc}
   */
  public Map<Integer, String> searchHightLightEvent(String username,
                                                    EventQuery eventQuery,
                                                    String[] publicCalendarIds) throws Exception {
    return storage_.searchHightLightEvent(username, eventQuery, publicCalendarIds);
  }

  /**
   * {@inheritDoc}
   */
  public void shareCalendar(String username, String calendarId, List<String> receiverUsers) throws Exception {
    storage_.shareCalendar(username, calendarId, receiverUsers);
  }

  /**
   * {@inheritDoc}
   */
  public GroupCalendarData getSharedCalendars(String username, boolean isShowAll) throws Exception {
    return storage_.getSharedCalendars(username, isShowAll);
  }

  /**
   * {@inheritDoc}
   */
  public List<CalendarEvent> getEvents(String username,
                                       EventQuery eventQuery,
                                       String[] publicCalendarIds) throws Exception {
    return storage_.getEvents(username, eventQuery, publicCalendarIds);
  }

  /**
   * {@inheritDoc}
   */
  public void removeSharedCalendar(String username, String calendarId) throws Exception {
    storage_.removeSharedCalendar(username, calendarId);
  }

  /**
   * {@inheritDoc}
   */
  public void saveEventToSharedCalendar(String username,
                                        String calendarId,
                                        CalendarEvent event,
                                        boolean isNew) throws Exception {
    storage_.saveEventToSharedCalendar(username, calendarId, event, isNew);
  }

  /**
   * {@inheritDoc}
   */
  public Map<String, String> checkFreeBusy(EventQuery eventQuery) throws Exception {
    return storage_.checkFreeBusy(eventQuery);
  }

  /**
   * {@inheritDoc}
   */
  public void saveSharedCalendar(String username, Calendar calendar) throws Exception {
    storage_.saveSharedCalendar(username, calendar);
  }

  /**
   * {@inheritDoc}
   */
  public void removeSharedEvent(String username, String calendarId, String eventId) throws Exception {
    storage_.removeSharedEvent(username, calendarId, eventId);
  }

  /**
   * {@inheritDoc}
   */
  public void moveEvent(String formCalendar,
                        String toCalendar,
                        String fromType,
                        String toType,
                        List<CalendarEvent> calEvents,
                        String username) throws Exception {
    storage_.moveEvent(formCalendar, toCalendar, fromType, toType, calEvents, username);
  }

  /**
   * {@inheritDoc}
   */
  public void confirmInvitation(String fromUserId,
                                String toUserId,
                                int calType,
                                String calendarId,
                                String eventId,
                                int answer) throws Exception {
    storage_.confirmInvitation(fromUserId, toUserId, calType, calendarId, eventId, answer);
  }

  /**
   * {@inheritDoc}
   */
  public void confirmInvitation(String fromUserId,
                                String confirmingEmail,
                                String confirmingUser,
                                int calType,
                                String calendarId,
                                String eventId,
                                int answer) throws Exception {
    storage_.confirmInvitation(fromUserId, confirmingEmail, confirmingUser, calType, calendarId, eventId, answer);
  }

  public void start() {
    for (CalendarUpdateEventListener updateListener : listeners_) {
      updateListener.preUpdate();
    }
  }


  public void stop() {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   */
  public synchronized void addListenerPlugin(CalendarUpdateEventListener listener) throws Exception {
    listeners_.add(listener);
  }

  // //// LEGACY API //////

  /**
   * {@inheritDoc}
   */
  public Map<String, String> checkFreeBusy(SessionProvider systemSession, EventQuery eventQuery) throws Exception {
    return checkFreeBusy(eventQuery);
  }

  /**
   * {@inheritDoc}
   */
  public int generateCalDav(SessionProvider systemSession,
                            String username,
                            LinkedHashMap<String, Calendar> calendars,
                            RssData rssData) throws Exception {
    return 0 ;//generateCalDav(username, calendars, rssData);
  }

  /**
   * {@inheritDoc}
   */
  public int generateRss(SessionProvider systemSession,
                         String username,
                         LinkedHashMap<String, Calendar> calendars,
                         RssData rssData) throws Exception {
    return generateRss(username, calendars, rssData);
  }

  /**
   * {@inheritDoc}
   */
  public List<GroupCalendarData> getCalendarCategories(SessionProvider userSession,
                                                       String username,
                                                       boolean isShowAll) throws Exception {
    return getCalendarCategories(username, isShowAll);
  }

  /**
   * {@inheritDoc}
   */
  public CalendarCategory getCalendarCategory(SessionProvider userSession,
                                              String username,
                                              String calendarCategoryId) throws Exception {
    return getCalendarCategory(username, calendarCategoryId);
  }

  /**
   * {@inheritDoc}
   */
  public CalendarSetting getCalendarSetting(SessionProvider userSession, String username) throws Exception {
    return getCalendarSetting(username);
  }

  /**
   * {@inheritDoc}
   */
  public List<CalendarCategory> getCategories(SessionProvider userSession, String username) throws Exception {
    return getCategories(username);
  }

  /**
   * {@inheritDoc}
   */
  public List<EventCategory> getEventCategories(SessionProvider userSession, String username) throws Exception {
    return getEventCategories(username);
  }

  /**
   * {@inheritDoc}
   */
  public EventCategory getEventCategory(SessionProvider userSession,
                                        String username,
                                        String eventCategoryId) throws Exception {
    return getEventCategory(username, eventCategoryId);
  }

  /**
   * {@inheritDoc}
   */
  public List<CalendarEvent> getEvents(SessionProvider userSession,
                                       String username,
                                       EventQuery eventQuery,
                                       String[] publicCalendarIds) throws Exception {
    return getEvents(username, eventQuery, publicCalendarIds);
  }

  /**
   * {@inheritDoc}
   */
  public List<FeedData> getFeeds(SessionProvider systemSession, String username) throws Exception {
    return getFeeds(username);
  }

  /**
   * {@inheritDoc}
   */
  public Calendar getGroupCalendar(SessionProvider systemSession, String calendarId) throws Exception {
    return getGroupCalendar(calendarId);
  }

  /**
   * {@inheritDoc}
   */
  public List<GroupCalendarData> getGroupCalendars(SessionProvider systemSession,
                                                   String[] groupIds,
                                                   boolean isShowAll,
                                                   String username) throws Exception {
    return getGroupCalendars(groupIds, isShowAll, username);
  }

  /**
   * {@inheritDoc}
   */
  public CalendarEvent getGroupEvent(SessionProvider systemSession,
                                     String calendarId,
                                     String eventId) throws Exception {
    return getGroupEvent(calendarId, eventId);
  }

  /**
   * {@inheritDoc}
   */
  public List<CalendarEvent> getGroupEventByCalendar(SessionProvider systemSession,
                                                     List<String> calendarIds) throws Exception {
    return getGroupEventByCalendar(calendarIds);
  }

  /**
   * {@inheritDoc}
   */
  public List<CalendarEvent> getPublicEvents(SessionProvider systemSession, EventQuery eventQuery) throws Exception {
    return getPublicEvents(eventQuery);
  }

  /**
   * {@inheritDoc}
   */
  public Node getRssHome(SessionProvider systemSession, String username) throws Exception {
    return getRssHome(username);
  }

  /**
   * {@inheritDoc}
   */
  public GroupCalendarData getSharedCalendars(SessionProvider systemSession,
                                              String username,
                                              boolean isShowAll) throws Exception {
    return getSharedCalendars(username, isShowAll);
  }

  /**
   * {@inheritDoc}
   */
  public Calendar getUserCalendar(SessionProvider userSession, String username, String calendarId) throws Exception {
    return getUserCalendar(username, calendarId);
  }

  /**
   * {@inheritDoc}
   */
  public List<Calendar> getUserCalendars(SessionProvider userSession,
                                         String username,
                                         boolean isShowAll) throws Exception {
    return getUserCalendars(username, isShowAll);
  }

  /**
   * {@inheritDoc}
   */
  public List<Calendar> getUserCalendarsByCategory(SessionProvider userSession,
                                                   String username,
                                                   String calendarCategoryId) throws Exception {
    return getUserCalendarsByCategory(username, calendarCategoryId);
  }

  /**
   * {@inheritDoc}
   */
  public List<CalendarEvent> getUserEventByCalendar(SessionProvider userSession,
                                                    String username,
                                                    List<String> calendarIds) throws Exception {
    return getUserEventByCalendar(username, calendarIds);
  }

  /**
   * {@inheritDoc}
   */
  public List<CalendarEvent> getUserEvents(SessionProvider userSession,
                                           String username,
                                           EventQuery eventQuery) throws Exception {
    return getUserEvents(username, eventQuery);
  }

  /**
   * {@inheritDoc}
   */
  public void moveEvent(SessionProvider userSession,
                        String formCalendar,
                        String toCalendar,
                        String formType,
                        String toType,
                        List<CalendarEvent> calEvents,
                        String username) throws Exception {
    moveEvent(formCalendar, toCalendar, formType, toType, calEvents, username);
  }

  /**
   * {@inheritDoc}
   */
  public CalendarCategory removeCalendarCategory(SessionProvider userSession,
                                                 String username,
                                                 String calendarCategoryId) throws Exception {
    return removeCalendarCategory(username, calendarCategoryId);
  }

  /**
   * {@inheritDoc}
   */
  public void removeEventCategory(SessionProvider userSession,
                                  String username,
                                  String eventCategoryName) throws Exception {
    removeEventCategory(username, eventCategoryName);
  }

  /**
   * {@inheritDoc}
   */
  public Calendar removePublicCalendar(SessionProvider systemSession, String calendarId) throws Exception {
    return removePublicCalendar(calendarId);
  }

  /**
   * {@inheritDoc}
   */
  public CalendarEvent removePublicEvent(SessionProvider systemSession,
                                         String calendarId,
                                         String eventId) throws Exception {
    return removePublicEvent(calendarId, eventId);
  }

  /**
   * {@inheritDoc}
   */
  public void removeSharedCalendar(SessionProvider systemSesssion,
                                   String username,
                                   String calendarId) throws Exception {
    removeSharedCalendar(username, calendarId);

  }

  /**
   * {@inheritDoc}
   */
  public void removeSharedEvent(SessionProvider systemSession,
                                String username,
                                String calendarId,
                                String eventId) throws Exception {
    removeSharedEvent(username, calendarId, eventId);

  }

  /**
   * {@inheritDoc}
   */
  public Calendar removeUserCalendar(SessionProvider userSession, String username, String calendarId) throws Exception {
    return removeUserCalendar(username, calendarId);
  }

  /**
   * {@inheritDoc}
   */
  public CalendarEvent removeUserEvent(SessionProvider userSession,
                                       String username,
                                       String calendarId,
                                       String eventId) throws Exception {
    return removeUserEvent(username, calendarId, eventId);
  }

  /**
   * {@inheritDoc}
   */
  public void saveCalendarCategory(SessionProvider userSession,
                                   String username,
                                   CalendarCategory calendarCategory,
                                   boolean isNew) throws Exception {
    saveCalendarCategory(username, calendarCategory, isNew);
  }

  /**
   * {@inheritDoc}
   */
  public void saveCalendarSetting(SessionProvider userSession,
                                  String username,
                                  CalendarSetting setting) throws Exception {
    saveCalendarSetting(username, setting);
  }

  /**
   * {@inheritDoc}
   */
  public void saveEventCategory(SessionProvider userSession,
                                String username,
                                EventCategory eventCategory,
                                String[] values,
                                boolean isNew) throws Exception {
    saveEventCategory(username, eventCategory, isNew);

  }

  /**
   * {@inheritDoc}
   */
  public void saveEventToSharedCalendar(SessionProvider systemSession,
                                        String username,
                                        String calendarId,
                                        CalendarEvent event,
                                        boolean isNew) throws Exception {
    saveEventToSharedCalendar(username, calendarId, event, isNew);
  }

  /**
   * {@inheritDoc}
   */
  public void savePublicCalendar(SessionProvider systemSession,
                                 Calendar calendar,
                                 boolean isNew,
                                 String username) throws Exception {
    savePublicCalendar(calendar, isNew, username);
  }

  /**
   * {@inheritDoc}
   */
  public void savePublicEvent(SessionProvider systemSession,
                              String calendarId,
                              CalendarEvent event,
                              boolean isNew) throws Exception {
    savePublicEvent(calendarId, event, isNew);
  }

  /**
   * {@inheritDoc}
   */
  public void saveSharedCalendar(SessionProvider systemSession, String username, Calendar calendar) throws Exception {
    saveSharedCalendar(username, calendar);
  }

  /**
   * {@inheritDoc}
   */
  public void saveUserCalendar(SessionProvider userSession,
                               String username,
                               Calendar calendar,
                               boolean isNew) throws Exception {
    saveUserCalendar(username, calendar, isNew);
  }

  /**
   * {@inheritDoc}
   */
  public void saveUserEvent(SessionProvider userSession,
                            String username,
                            String calendarId,
                            CalendarEvent event,
                            boolean isNew) throws Exception {
    saveUserEvent(username, calendarId, event, isNew);
  }

  /**
   * {@inheritDoc}
   */
  public EventPageList searchEvent(SessionProvider userSession,
                                   String username,
                                   EventQuery eventQuery,
                                   String[] publicCalendarIds) throws Exception {
    return searchEvent(username, eventQuery, publicCalendarIds);
  }

  /**
   * {@inheritDoc}
   */
  public Map<Integer, String> searchHightLightEvent(SessionProvider userSession,
                                                    String username,
                                                    EventQuery eventQuery,
                                                    String[] publicCalendarIds) throws Exception {
    return searchHightLightEvent(username, eventQuery, publicCalendarIds);
  }

  /**
   * {@inheritDoc}
   */
  public void shareCalendar(SessionProvider systemSession,
                            String username,
                            String calendarId,
                            List<String> receiverUsers) throws Exception {
    shareCalendar(username, calendarId, receiverUsers);
  }

  public void updateCalDav(String usename, String calendarId, CalendarImportExport imp) throws Exception {
    storage_.updateCalDav(usename, calendarId, imp) ;
  }

  public void updateCalDav(String usename, String calendarId, CalendarImportExport imp, int number) throws Exception {
    storage_.updateCalDav(usename, calendarId, imp, number) ;
  }

  public void updateRss(String usename, String calendarId, CalendarImportExport imp) throws Exception {
    storage_.updateRss(usename, calendarId, imp) ;

  }

  public void updateRss(String usename, String calendarId, CalendarImportExport imp, int number) throws Exception {
    storage_.updateRss(usename, calendarId, imp, number) ;
  }

  public int getTypeOfCalendar(String userName, String calendarId) throws Exception {
    return storage_.getTypeOfCalendar(userName, calendarId);
  }

  public int generateCalDav(String username, List<String> calendarIds, RssData rssData) throws Exception {
    return storage_.generateCalDav(username,calendarIds, rssData, calendarImportExport_.get(ICALENDAR));
  }

  public int generateRss(String username, List<String> calendarIds, RssData rssData) throws Exception {
    return storage_.generateRss(username,calendarIds, rssData, calendarImportExport_.get(ICALENDAR));
  }

  public EventCategory getEventCategoryByName(String username, String eventCategoryName) throws Exception {
    ResourceBundle rb = null ;
    try { 
      rb = rbs_.getResourceBundle("locale.portlet.calendar.CalendarPortlet", Locale.getDefault()) ;
    } catch (MissingResourceException e) {
      //TODO the fist time load 
    }
    for (EventCategory ev : storage_.getEventCategories(username)) {
      if(ev.getName().equalsIgnoreCase(eventCategoryName)) {
        return ev ;
      } else if (rb != null) {
        try {
          if (eventCategoryName.equalsIgnoreCase(rb.getString("UICalendarView.label."+ev.getId()))) return ev ;
        } catch (MissingResourceException e) { }
      }
    }
    return null ;
  }
}
