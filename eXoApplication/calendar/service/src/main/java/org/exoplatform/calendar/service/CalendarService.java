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
package org.exoplatform.calendar.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.jcr.Node;

import org.exoplatform.calendar.service.impl.CalendarEventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */

public interface CalendarService {

  final public static String                  ICALENDAR             = "ICalendar(.ics)".intern();
  final public static String                  EXPORTEDCSV           = "ExportedCsv(.csv)".intern();

  /**
   * The method gets all calendar category of current user from data base
   * @param username current user name
   * @return List of CalendarCategory object
   * @throws Exception
   * @see CalendarCategory
   */
  public List<CalendarCategory> getCategories(String username) throws Exception ;

  /**
   * The method gets all groups of private calendar, and each GroupCalendar contains List of Calendar object
   * @param username current user name
   * @param isShowAll The parameter to make sure that the user want to show all calendar or not, if it is <b>true</b> then 
   * it gets all calendars, if <b>false</b> it will check from calendar setting to know which calendar will be shown
   * @return List<GroupCalendarData> List of GroupCalendarData
   * @throws Exception
   * @see GroupCalendarData
   */
  public List<GroupCalendarData> getCalendarCategories(String username, boolean isShowAll) throws Exception ;

  /**
   * The method gets the calendar category by given id
   * @param username current user name 
   * @param calendarCategoryId id of calendar category
   * @return CalendarCategory
   * @throws Exception
   * @see CalendarCategory
   */
  public CalendarCategory getCalendarCategory(String username, String calendarCategoryId) throws Exception ;

  /**
   * Save details of category for a user
   * @param username current user name(or user id)
   * @param calendarCategory the object that contains category details
   * @param isNew the boolean value to point out that add new category or update
   * @throws Exception
   */
  public void saveCalendarCategory(String username, CalendarCategory calendarCategory, boolean isNew) throws Exception ; 

  /**
   * The method used for removing one category by id
   * @param username current user name(or user id)
   * @param calendarCategoryId given category id
   * @return
   * @throws Exception
   * @see CalendarCategory
   */
  public CalendarCategory removeCalendarCategory(String username, String calendarCategoryId) throws Exception ;

  /**
   * The method get private calendar by given calendarId, and all calendar related to this category will be removed
   * @param username current user name(or user id)
   * @param calendarId given calendar id
   * @return Calendar object returned contants details of a calendar
   * @throws Exception
   * @see Calendar
   */
  public Calendar getUserCalendar(String username, String calendarId) throws Exception ;

  /**
   * The method queries all private calendars of current user
   * @param username current user name(or user id)
   * @param isShowAll boolean value if equals <b>true</b> will get all private calendars, equals <b>false</b> it will take only 
   * the calendars in current user's setting
   * @return List of calendar object
   * @throws Exception
   * @see Calendar
   */
  public List<Calendar> getUserCalendars(String username, boolean isShowAll) throws Exception ;

  /**
   * The method look up all private calendars by given category id
   * @param username current user name(or user id)
   * @param calendarCategoryId given calendar category id
   * @return List calendar object
   * @throws Exception
   * @see Calendar
   */
  public List<Calendar> getUserCalendarsByCategory(String username, String calendarCategoryId) throws Exception ;

  /**
   * The method saves private calendar infomations in to data base
   * @param username current user name(or user id)
   * @param calendar object contants infomations
   * @param isNew Boolean value to know add new calendar or update infomations only
   * @throws Exception
   */
  public void saveUserCalendar(String username, Calendar calendar, boolean isNew) throws Exception ;

  /**
   * Remove private calendar by given id, all events and tasks belong to this calendar will be removed
   * @param username current user name(or user id)
   * @param calendarId given calendar id
   * @return
   * @throws Exception
   */
  public Calendar removeUserCalendar(String username, String calendarId) throws Exception ;

  /**
   * The method save all infomations about shared calendar, it will be updated original calendar
   * @param username current user name(or user id)
   * @param calendar the oject contants infomations
   * @throws Exception
   */
  public void saveSharedCalendar(String username, Calendar calendar) throws Exception ;

  /**
   * The method  gets all calendar of a group user, we called it is group calendar
   * it means the calendar for group of users and depen on the permission the user will have right to view or edit that calendar
   * @param calendarId given calendar id
   * @return Calendar object contants infomations
   * @throws Exception
   * @see Calendar
   */
  public Calendar getGroupCalendar(String calendarId) throws Exception ;  

  /**
   * The method  gets all the group calendar data of current user and list of calendars belong to that group
   * with group calendar data it will classify calendar to each group
   * @param groupIds The group ids that current user belong
   * @param isShowAll Gets all calendar or use setting from calendar setting
   * @param username current user name(or user id)
   * @return List of GroupCalendarData and each GroupCalendarData contants List of calendar object too
   * @throws Exception
   * @see GroupCalendarData
   */
  public List<GroupCalendarData> getGroupCalendars(String[] groupIds, boolean isShowAll, String username) throws Exception ;  

  /**
   * The method save calendar to public area (group calendar)
   * @param calendar
   * @param isNew Boolean value will be checked is it add new or update infomations only
   * @param username current user name(or user id)
   * @throws Exception
   */
  public void savePublicCalendar(Calendar calendar, boolean isNew, String username) throws Exception ;  

  /**
   * Remove the group calendar form data base, every events, tasks inside this calendar will be removed too
   * @param calendarId
   * @return
   * @throws Exception
   */
  public Calendar removePublicCalendar(String calendarId) throws Exception ;

  /**
   * The method gets all categories of event
   * @param username current user name(or user id)
   * @return List event category object
   * @throws Exception
   * @see EventCategory
   */
  public List<EventCategory> getEventCategories(String username) throws Exception ;

  /**
   * Save event category to data base, every user will have their own category to classify events, and it will use unique name in data base
   * @param username current user name(or user id)
   * @param eventCategory
   * @param values 
   * @param isNew
   * @throws Exception
   */
  public void saveEventCategory(String username, EventCategory eventCategory, boolean isNew) throws Exception ;

  /**
   * Remove event category, all events and tasks belong to this category will be destroyed
   * @param username current user name(or user id)
   * @param eventCategoryName The unique name of category
   * @throws Exception
   */
  public void removeEventCategory(String username, String eventCategoryName) throws Exception ;  

  /**
   * The method gets category of event by given id
   * @param userSession The session of current logedin user
   * @param username current user name(or user id)
   * @param eventCategoryId given event category id
   * @return event category object contents infomations
   * @throws Exception
   * @see EventCategory
   */
  public EventCategory getEventCategory(String username, String eventCategoryId) throws Exception ;
  
  /**
   * The method gets category of event by given id
   * @param userSession The session of current logedin user
   * @param username current user name(or user id)
   * @param eventCategoryName given event category name
   * @return event category object contents infomations
   * @throws Exception
   * @see EventCategory
   */
  public EventCategory getEventCategoryByName(String username, String eventCategoryName) throws Exception ;
  
  

  /**
   * The method gets list events and tasks of given private calendar ids 
   * @param userSession The session of current logedin user
   * @param username current user name(or user id)
   * @param calendarIds given calendar ids
   * @return List of events and tasks
   * @throws Exception
   */
  public List<CalendarEvent> getUserEventByCalendar(String username, List<String> calendarIds) throws Exception ;

  /**
   * The method gets all events and tasks by given conditions in event query
   * @param userSession The session of current logedin user
   * @param username current user name(or user id)
   * @param eventQuery given coditons
   * @return List of CalendarEvent object (events and tasks)
   * @throws Exception
   * @see CalendarEvent
   */
  public List<CalendarEvent> getUserEvents(String username, EventQuery eventQuery) throws Exception ;

  /**
   * Get a personal event for a given owner
   * @param owner user id of the event owner
   * @param eventId id of event to get
   * @return CalendarEvent in the personal events of owner 
   * @throws Exception 
   */
  public CalendarEvent getEvent(String username, String eventId) throws Exception ;  
  
  /**
   * The method save infomation to an event or a task by given private calendar id to data
   * @param username current user name(or user id)
   * @param calendarId given calendar id
   * @param event object contants infomations
   * @param isNew boolean value, is update or add new event
   * @throws Exception
   */
  public void saveUserEvent(String username, String calendarId, CalendarEvent event, boolean isNew) throws Exception ;

  /**
   * Remove given event or task in private calendar with calendar id, all attachments and reminders will be removed
   * @param username current user name(or user id)
   * @param calendarId given calendar id
   * @param eventId given event id
   * @return
   * @throws Exception
   */
  public CalendarEvent removeUserEvent(String username, String calendarId, String eventId) throws Exception ;

  /**
   * The menthod gets event or task form group calendar by given calendar id
   * @param calendarId given calendar id
   * @param eventId given event id
   * @return CalendarEvent object containts infomations and attachments, reminders
   * @throws Exception
   * @see CalendarEvent
   */
  public CalendarEvent getGroupEvent(String calendarId, String eventId) throws Exception ;

  /**
   * The method gets events and tasks by given public calendar ids  
   * @param calendarIds public calendar ids
   * @return List calendar event object
   * @throws Exception
   * @see CalendarEvent
   */
  public List<CalendarEvent> getGroupEventByCalendar(List<String> calendarIds) throws Exception ;

  /**
   * The method gets events and tasks by given event query
   * @param eventQuery object contants given conditions 
   * @return List calendar event object
   * @throws Exception
   * @see CalendarEvent
   */
  public List<CalendarEvent> getPublicEvents(EventQuery eventQuery) throws Exception ;
  /**
   * Save event or task by given group calendar id
   * @param calendarId given calendar id
   * @param event object contants infomation about event
   * @param isNew boolean value to check update or add new event
   * @throws Exception
   */
  public void savePublicEvent(String calendarId, CalendarEvent event, boolean isNew) throws Exception ;

  /**
   * Remove event or task, all attachments and reminders item will be removed
   * @param calendarId given calendar id
   * @param eventId given event or task id
   * @return
   * @throws Exception
   */
  public CalendarEvent removePublicEvent(String calendarId, String eventId) throws Exception ;

  /**
   * This menthod stores individual setting of each user, with setting you can configue many things like Default view
   * date, time formating, time inteval 
   * @param username current user name(or user id)
   * @param setting Obicject containts infomations about setting
   * @throws Exception
   */
  public void saveCalendarSetting(String username, CalendarSetting setting) throws Exception ;

  /**
   * This method gets infomations of current user's setting
   * @param username current user name(or user id)
   * @return
   * @throws Exception
   * @see CalendarSetting
   */
  public CalendarSetting getCalendarSetting(String username) throws Exception ;

  /**
   * The method  gets Import/Export implement class to import or export ics,csv
   * @param type type of import, export, it supports two types, ICS and CSV 
   * @return CalendarImportExport
   * @see CalendarImportExport
   */
  public CalendarImportExport getCalendarImportExports(String type) ;

  /**
   * The method gets types of data will be imported and exported
   * @return types of inport/export
   * @throws Exception
   */
  public String[] getExportImportType() throws Exception ;

  /**
   * The menthod uses to make url to contants links to subcribe calendar folows RSS stand
   * @param username current user name(or user id)
   * @param calendars
   * @param rssData object contants infomations about the rss feed
   * @return
   * @throws Exception
   * @see RssData
   */
  public int generateRss(String username, LinkedHashMap<String, Calendar> calendars, RssData rssData) throws Exception ;

  /**
   * The menthod uses to make url to contants links to subcribe calendar folows RSS stand
   * @param username current user name(or user id)
   * @param calendars
   * @param rssData object contants infomations about the rss feed
   * @return
   * @throws Exception
   * @see RssData
   */
  public int generateRss(String username, List<String> calendarIds, RssData rssData) throws Exception ;
  
  
  /**
   * It gets data form server and show the url to view contents of RSS
   * @param systemSession Sessesion to access the public data
   * @param username current user name(or user id)
   * @return List of FeedData
   * @throws Exception
   * @see FeedData
   */
  public List<FeedData> getFeeds(String username) throws Exception  ;

  /**
   * The method return root of rss data store area
   * @param username current user name(or user id)
   * @return
   * @throws Exception
   */
  public Node getRssHome(String username) throws Exception ;

  /**
   * The method query events and tasks form given coditions, the coditions know by set value for eventquery
   * @param userSession The session of current logedin user
   * @param username current user name(or user id)
   * @param eventQuery object contants coditions to query
   * @param publicCalendarIds pulic calendar ids
   * @return
   * @throws Exception
   * @see EventPageList
   */
  public EventPageList searchEvent(String username, EventQuery eventQuery, String[] publicCalendarIds)throws Exception ;

  /**
   * The method query all events, tasks and mark to hightlight the date have events or tasks 
   * @param userSession The session of current logedin user
   * @param username current user name(or user id)
   * @param eventQuery object contants coditions to query
   * @param publicCalendarIds publicCalendarIds pulic calendar ids
   * @return
   * @throws Exception
   */
  public Map<Integer, String > searchHightLightEvent(String username, EventQuery eventQuery, String[] publicCalendarIds)throws Exception ; 

  /**
   * The method share the private calendar to other user, it can share for one or many users
   * @param systemSession Sessesion to access the public data
   * @param username current user name(or user id)
   * @param calendarId given calendar id
   * @param receiverUsers List receive user username or id
   * @throws Exception
   */
  public void shareCalendar(String username, String calendarId, List<String> receiverUsers) throws Exception ;

  /**
   * The method gets all shared calendars of the current user
   * @param systemSession Sessesion to access the public data
   * @param username current user name(or user id)
   * @param isShowAll boolean value to point out that it will get all calendars or use user's clendar setting
   * @return
   * @throws Exception
   * @see GroupCalendarData
   */
  public GroupCalendarData getSharedCalendars(String username, boolean isShowAll) throws Exception ;

  /**
   * The method selects all the events and tasks by given conditions, it includes events of private, public and share calendars
   * @param username current user name(or user id)
   * @param eventQuery given coditions
   * @param publicCalendarIds public calendar ids
   * @return
   * @throws Exception
   * @see CalendarEvent
   */
  public List<CalendarEvent> getEvents(String username, EventQuery eventQuery, String[] publicCalendarIds) throws Exception ;

  /**
   * Removed shared calendar, but not the orloginal calendar
   * @param username current user name(or user id)
   * @param calendarId given calendar id
   * @throws Exception
   */
  public void removeSharedCalendar(String username, String calendarId) throws Exception  ;

  /**
   * Add event to shared calendar, mean add event to orloginal calendar too
   * @param username current user name(or user id)
   * @param calendarId given calendar id
   * @param event object contants infomations about event
   * @param isNew boolean value to check that add new or update event
   * @throws Exception
   */
  public void saveEventToSharedCalendar(String username, String calendarId, CalendarEvent event, boolean isNew) throws Exception  ;

  /**
   * The method  will check the time free or busy of the user, it depents on events and tasks of this user 
   * now it only check on one day and if the events and tasks marked with busy, out side status will be checked 
   * @param eventQuery The query object it containts query statement to look up the data 
   * @return Map data with key is user name (or user id), and value is the a pair of <i>from time</i> and <i>to time</i> by miliseconds and sperate by coma(,)
   * @throws Exception 
   * @see EventQuery
   */
  public Map<String, String> checkFreeBusy(EventQuery eventQuery) throws Exception  ;

  /**
   * The method genarete links to access calendar throw WEBDAV, it will require user name and password when access
   * @param username current user name(or user id)
   * @param calendars List calendar ids will look up and publicing
   * @param rssData Object contants infomations about rss feed
   * @return
   * @throws Exception
   */
  public int generateCalDav(String username, LinkedHashMap<String, Calendar> calendars, RssData rssData) throws Exception ;

  /**
   * The method genarete links to access calendar throw WEBDAV, it will require user name and password when access
   * @param username current user name(or user id)
   * @param calendars List calendar ids will look up and publicing
   * @param rssData Object contants infomations about rss feed
   * @return
   * @throws Exce
   */
  public int generateCalDav(String username, List<String> calendarIds, RssData rssData) throws Exception ;
  
  /**
   * The method removes the events or tasks form shared calendar, orloginal item will be removed
   * @param username current user name(or user id)
   * @param calendarId given calendar id
   * @param eventId given event id
   * @throws Exception
   */
  public void removeSharedEvent(String username, String calendarId, String eventId) throws Exception ;

  /**
   * The method  move and save events form private calendars share calendars public calendars each other
   * @param formCalendar the source calendar id
   * @param toCalendar  destination calendar id
   * @param formType type of source calendar
   * @param toType type of destination calendar
   * @param calEvents List of object contant infomations
   * @param username current user name(or user id)
   * @throws Exception
   */
  public void moveEvent(String formCalendar, String toCalendar, String formType, String toType, List<CalendarEvent> calEvents, String username) throws Exception ;

  /**
   * The method calls when the user use exomail product only, when user receives an invitation (in the same data system), the user will 
   * congfirme that do they want to take part in or not
   * @param fromUserId id or user name of the user, who make the invitation
   * @param toUserId receiver user's id or name
   * @param calType type of calendar contants the event
   * @param calendarId given calendar id
   * @param eventId given event id
   * @param answer The answer of the receive user 
   * @throws Exception
   */
  public void confirmInvitation(String fromUserId, String toUserId,int calType,String calendarId, String eventId, int answer) throws Exception ;
  
  public void confirmInvitation(String fromUserId, String confirmingEmail, String confirmingUser, int calType,String calendarId, String eventId, int answer) throws Exception ;
  
  public void addListenerPlugin(CalendarUpdateEventListener listener) throws Exception ;
  
  public void addEventListenerPlugin(CalendarEventListener listener) throws Exception ;
  
  /**
   * The method update exited rss data when calendar has been changed
   * @param usename 
   * @param calendarId calendar id
   * Added from 1.3
   */  
  public void updateRss(String usename, String calendarId, CalendarImportExport imp) throws Exception ;
  
  public void updateRss(String usename, String calendarId, CalendarImportExport imp, int number) throws Exception ;
  
  /**
   * The method update exited rss data when calendar has been changed
   * @param usename 
   * @param calendarId calendar id
   * Added from 1.3
   */  
  public void updateCalDav(String usename, String calendarId, CalendarImportExport imp) throws Exception ;
  
  public void updateCalDav(String usename, String calendarId, CalendarImportExport imp, int number) throws Exception ;
  
  public int getTypeOfCalendar(String userName, String calendarId) throws Exception;
  
  public List<CalendarEvent> getSharedEventByCalendars(String username, List<String> calendarIds) throws Exception ;
  
  public void removeFeedData(String username, String title);
  
  public ResourceBundle getResourceBundle() throws Exception;

  public void initNewUser(String userName, CalendarSetting defaultCalendarSetting) throws Exception;	
}
