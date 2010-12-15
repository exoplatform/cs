/*
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
 */
package org.exoplatform.calendar.service.test;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarCategory;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.CalendarSetting;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.calendar.service.RssData;
import org.exoplatform.calendar.service.Utils;
import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * July 3, 2008  
 */


public class TestCalendarService extends BaseCalendarServiceTestCase{
  private CalendarService calendarService_ ;
  private static String  username = "root";

  //private JCRDataStorage datastorage;

  public TestCalendarService() throws Exception {
    super();
    calendarService_ = (CalendarService) container.getComponentInstanceOfType(CalendarService.class);
    //datastorage = (JCRDataStorage) container.getComponentInstanceOfType(JCRDataStorage.class);
  }

  public void setUp() throws Exception {
    super.setUp();
  }

  public void testCalendar() throws Exception {
    CalendarCategory calCategory = new CalendarCategory() ;
    calCategory.setName("categoryName") ;
    calCategory.setDescription("Description") ;
    calendarService_.saveCalendarCategory("root", calCategory, true) ;

    //create/get calendar in private folder
    Calendar cal = new Calendar() ;
    cal.setName("myCalendar") ;
    cal.setDescription("Desscription") ;
    cal.setCategoryId(calCategory.getId()) ;
    cal.setPublic(true) ;
    calendarService_.saveUserCalendar(username, cal, true) ;
    Calendar myCal = calendarService_.getUserCalendar(username,cal.getId()) ;
    assertNotNull(myCal) ;
    assertEquals(myCal.getName(), "myCalendar") ;

    //create/get calendar in public folder
    cal.setPublic(false) ;
    cal.setGroups(new String[] {"users", "admin"}) ;
    cal.setViewPermission(new String [] {"member:/users", "member:/admin"}) ;
    cal.setEditPermission(new String [] {"admin"}) ;
    calendarService_.savePublicCalendar(cal, true, username) ;
    Calendar publicCal = calendarService_.getGroupCalendar(cal.getId()) ;
    assertNotNull(publicCal) ;
    assertEquals(publicCal.getName(), "myCalendar") ;

    //get calendar in private folder by categoryID
    List<Calendar> calendares = calendarService_.getUserCalendarsByCategory(username, calCategory.getId()) ;
    assertNotNull(calendares);
    assertEquals(calendares.size(), 1) ;

    //get calendar in public folder by groupId
    List<GroupCalendarData> groupCalendarList = calendarService_.getGroupCalendars(new String[] {"users"}, true, username) ;
    assertNotNull(groupCalendarList);
    assertEquals(groupCalendarList.size(), 1) ;

    groupCalendarList = calendarService_.getGroupCalendars(new String[] {"admin"}, true, username);
    assertNotNull(groupCalendarList);
    assertEquals(groupCalendarList.size(), 1) ;

    groupCalendarList = calendarService_.getGroupCalendars(new String[] {"admin1"}, true, username) ;
    assertNotNull(groupCalendarList);
    assertEquals(groupCalendarList.size(), 0) ;

    //update public calendar 
    cal.setPublic(false) ;
    cal.setName("myCalendarUpdated") ;
    calendarService_.savePublicCalendar(cal, false, username) ;
    myCal = calendarService_.getGroupCalendar(cal.getId()) ;
    assertEquals(myCal.getName(),"myCalendarUpdated") ;

    //remove public calendar
    Calendar removeCal = calendarService_.removePublicCalendar(cal.getId()) ;
    assertEquals(removeCal.getName(), "myCalendarUpdated") ;

    //remove private calendar
    removeCal = calendarService_.removeUserCalendar(username, cal.getId()) ;
    assertEquals(removeCal.getName(), "myCalendar") ;

    //remove private calendar category
    assertNotNull(calendarService_.removeCalendarCategory(username, calCategory.getId())) ;

    // calendar setting
    CalendarSetting setting = new CalendarSetting() ;
    setting.setBaseURL("url") ;
    setting.setLocation("location") ;
    calendarService_.saveCalendarSetting(username, setting) ;
    assertEquals("url",calendarService_.getCalendarSetting(username).getBaseURL()) ;


  }

  public void testSharedCalendar() throws Exception {
    CalendarCategory calCategory = new CalendarCategory() ;
    calCategory.setName("categoryName") ;
    calendarService_.saveCalendarCategory("root", calCategory, true) ;

    Calendar cal = new Calendar() ;
    cal.setName("myCalendar") ;
    cal.setCategoryId(calCategory.getId()) ;
    cal.setPublic(true) ;    
    cal.setViewPermission(new String[]{"*.*"});
    cal.setEditPermission(new String[]{"*.*", "john"});

    calendarService_.saveUserCalendar(username, cal, true) ;

    //Share calendar   
    List<String> receiverUser = new ArrayList<String>() ;
    receiverUser.add("john") ;
    calendarService_.shareCalendar(username, cal.getId(), receiverUser) ;
    Calendar sharedCalendar = calendarService_.getSharedCalendars("john", true).getCalendarById(cal.getId()) ;
    assertEquals("myCalendar", sharedCalendar.getName()) ;

    sharedCalendar.setDescription("shared description") ;
    calendarService_.saveSharedCalendar("john", sharedCalendar) ;
    Calendar editedCalendar = calendarService_.getSharedCalendars("john", true).getCalendarById(cal.getId()) ;
    assertEquals("shared description", editedCalendar.getDescription()) ;

    CalendarEvent calendarEvent = new CalendarEvent() ;
    calendarEvent.setCalendarId(cal.getId()) ;
    calendarEvent.setSummary("calendarEvent") ;
    calendarEvent.setEventType(CalendarEvent.TYPE_EVENT) ;
    calendarEvent.setFromDateTime(new Date()) ;
    calendarEvent.setToDateTime(new Date()) ;
    calendarService_.saveEventToSharedCalendar("john", cal.getId(), calendarEvent, true) ;

    List<String> calendarIds = new ArrayList<String>() ;    
    calendarIds.add(cal.getId()) ;
    assertEquals(1,calendarService_.getSharedEventByCalendars("john", calendarIds).size());

    CalendarEvent event = calendarService_.getUserEventByCalendar(username, calendarIds).get(0) ;
    assertEquals("calendarEvent", event.getSummary()) ;

    calendarService_.removeSharedEvent("john", cal.getId(), calendarEvent.getId()) ;
    List<CalendarEvent> events = calendarService_.getUserEventByCalendar(username, calendarIds);
    assertEquals(0, events.size()) ;

    calendarService_.removeSharedCalendar("john", cal.getId()) ;
    assertNull(calendarService_.getSharedCalendars("john", true)) ;
    calendarService_.removeCalendarCategory(username, calCategory.getId()) ;
  }

  public void testCalendarCategory() throws Exception {
    CalendarCategory calCategory = new CalendarCategory() ;
    calCategory.setName("categoryName") ;
    calCategory.setDescription("Description") ;
    //calCategory.setCalendars(new String [] {"calendar1", "calendar2"}) ;
    calendarService_.saveCalendarCategory(username, calCategory, true) ;
    List<GroupCalendarData> categories = calendarService_.getCalendarCategories(username, true) ;
    assertEquals(categories.size(), 1) ;
    assertEquals(1, calendarService_.getCategories(username).size()) ;


    //get calendar category
    calCategory = calendarService_.getCalendarCategory(username, calCategory.getId()) ;
    assertEquals(calCategory.getName(), "categoryName") ;

    // update calendar category
    calCategory.setName("categoryNameUpdated") ;
    calendarService_.saveCalendarCategory(username, calCategory, false) ;

    //remove calendar category
    CalendarCategory removeCate = calendarService_.removeCalendarCategory(username, calCategory.getId()) ;
    assertEquals(removeCate.getName(), "categoryNameUpdated") ;
  }

  public void testEventCategory() throws Exception {
    CalendarCategory calCategory = new CalendarCategory() ;
    calCategory.setName("categoryName") ;
    calCategory.setDescription("Description") ;
    //calCategory.setCalendars(new String [] {""}) ;
    calendarService_.saveCalendarCategory(username, calCategory, true) ;

    Calendar cal = new Calendar() ;
    cal.setName("myCalendar") ;
    cal.setDescription("Desscription") ;
    cal.setCategoryId(calCategory.getId()) ;
    cal.setPublic(true) ;
    //create/get calendar in private folder
    calendarService_.saveUserCalendar(username, cal, true) ;
    Calendar myCal = calendarService_.getUserCalendar(username, cal.getId()) ;
    assertNotNull(myCal) ;
    assertEquals(myCal.getName(), "myCalendar") ;

    EventCategory eventCategory = new EventCategory() ;
    String name = "eventCategoryName" ;
    eventCategory.setName(name) ;
    eventCategory.setDescription("description") ;
    calendarService_.saveEventCategory(username, eventCategory, true) ;
    assertEquals(1, calendarService_.getEventCategories(username).size()) ;
    assertNotNull(calendarService_.getEventCategory(username, eventCategory.getId())) ;

    // import, export calendar
    CalendarEvent calendarEvent = new CalendarEvent() ;
    calendarEvent.setCalendarId(cal.getId()) ;
    calendarEvent.setSummary("sum") ;
    calendarEvent.setEventType(CalendarEvent.TYPE_EVENT) ;
    calendarEvent.setFromDateTime(new Date()) ;
    calendarEvent.setToDateTime(new Date()) ;
    calendarService_.saveUserEvent(username, cal.getId(), calendarEvent, true) ;

    List<String> calendarIds = new ArrayList<String>() ;
    calendarIds.add(cal.getId()) ;
    OutputStream out = calendarService_.getCalendarImportExports(
                                                                 CalendarService.ICALENDAR).exportCalendar(username, calendarIds, "0") ;
    ByteArrayInputStream is = new ByteArrayInputStream(out.toString().getBytes()) ;

    assertNotNull(calendarService_.removeUserEvent(username, cal.getId(), calendarEvent.getId())) ;
    assertEquals(0, calendarService_.getUserEventByCalendar(username, calendarIds).size()) ;
    assertNotNull(calendarService_.removeUserCalendar(username, cal.getId())) ;

    startSessionAs(username) ;
    calendarService_.getCalendarImportExports(CalendarService.ICALENDAR)
    .importCalendar(username, is, "importedCalendar") ;
    List<Calendar> cals = calendarService_.getUserCalendars(username, true) ;
    List<String> newCalendarIds = new ArrayList<String>() ;
    for (Calendar calendar : cals) newCalendarIds.add(calendar.getId()) ;
    List<CalendarEvent> events = calendarService_.getUserEventByCalendar(username, newCalendarIds) ;
    assertEquals(events.get(0).getSummary(), "sum") ;

    //remove Event category
    calendarService_.removeEventCategory(username, eventCategory.getId()) ;

    assertNotNull(calendarService_.removeUserCalendar(username, newCalendarIds.get(0))) ;
    assertNotNull(calendarService_.removeCalendarCategory(username, calCategory.getId())) ;
  }

  public void testPublicEvent() throws Exception {
    CalendarCategory calCategory = new CalendarCategory();
    calCategory.setName("CalendarCategoryName");
    calCategory.setDescription("CaldendarCategoryDescription");
    calendarService_.saveCalendarCategory(username, calCategory, true);

    Calendar cal = new Calendar();
    cal.setName("CalendarName") ;
    cal.setDescription("CalendarDesscription") ;
    cal.setCategoryId(calCategory.getId()) ;
    cal.setPublic(true) ;
    calendarService_.savePublicCalendar(cal, true, username) ;

    EventCategory eventCategory = new EventCategory();
    eventCategory.setName("EventCategoryName1");
    eventCategory.setDescription("EventCategoryDescription");
    calendarService_.saveEventCategory(username, eventCategory, true) ;

    CalendarEvent calEvent = new CalendarEvent();
    calEvent.setEventCategoryId(eventCategory.getName());
    calEvent.setSummary("Have a meeting");
    java.util.Calendar fromCal = java.util.Calendar.getInstance();
    java.util.Calendar toCal = java.util.Calendar.getInstance(); 
    toCal.add(java.util.Calendar.HOUR, 1) ;
    calEvent.setFromDateTime(fromCal.getTime());
    calEvent.setToDateTime(toCal.getTime());
    calendarService_.savePublicEvent(cal.getId(), calEvent , true);

    assertNotNull(calendarService_.getGroupEvent(cal.getId(), calEvent.getId()));
    List<String> calendarIds = new ArrayList<String>() ;
    calendarIds.add(cal.getId()) ;
    assertEquals(1, calendarService_.getGroupEventByCalendar(calendarIds).size()) ;
    assertNotNull(calendarService_.removePublicEvent(cal.getId(),calEvent.getId())) ;

    calendarService_.removeEventCategory(username, eventCategory.getId()) ;
    calendarService_.removeUserCalendar(username, cal.getId()) ;
    calendarService_.removeCalendarCategory(username, calCategory.getId()) ;
  }

  public void testPrivateEvent() throws Exception {
    CalendarCategory calCategory = new CalendarCategory();
    calCategory.setName("CalendarCategoryName");
    calCategory.setDescription("CaldendarCategoryDescription");
    calendarService_.saveCalendarCategory(username, calCategory, true);

    Calendar cal = new Calendar();
    cal.setName("CalendarName") ;
    cal.setDescription("CalendarDesscription") ;
    cal.setCategoryId(calCategory.getId()) ;
    cal.setPublic(false) ;
    calendarService_.saveUserCalendar(username, cal, true) ;

    EventCategory eventCategory = new EventCategory();
    eventCategory.setName("EventCategoryName2");
    eventCategory.setDescription("EventCategoryDescription");
    calendarService_.saveEventCategory(username, eventCategory, true) ;

    CalendarEvent calEvent = new CalendarEvent();
    calEvent.setEventCategoryId(eventCategory.getName());
    calEvent.setSummary("Have a meeting");
    java.util.Calendar fromCal = java.util.Calendar.getInstance();
    java.util.Calendar toCal = java.util.Calendar.getInstance(); 
    toCal.add(java.util.Calendar.HOUR, 1) ;
    calEvent.setFromDateTime(fromCal.getTime());
    calEvent.setToDateTime(toCal.getTime());
    calendarService_.saveUserEvent(username, cal.getId(), calEvent , true);

    EventQuery query = new EventQuery();
    query.setCategoryId(new String[] {eventCategory.getName()});    
    assertEquals(calendarService_.getUserEvents(username, query).size(), 1);

    EventQuery eventQuery = new EventQuery() ;
    eventQuery.setText("Have a meeting") ;

    assertEquals(1, calendarService_.searchEvent(username, eventQuery, new String[] {}).getAll().size()) ;
    assertEquals(1, calendarService_.getEvents(username, eventQuery, new String[] {}).size()) ;

    List<CalendarEvent> list = new ArrayList<CalendarEvent>() ;
    list.add(calEvent) ;
    Calendar movedCal = new Calendar();
    movedCal.setName("MovedCalendarName") ;
    movedCal.setDescription("CalendarDesscription") ;
    movedCal.setCategoryId(calCategory.getId()) ;
    movedCal.setPublic(false) ;
    calendarService_.saveUserCalendar(username, movedCal, true) ;

    calendarService_.moveEvent(cal.getId(), movedCal.getId(), calEvent.getCalType(), calEvent.getCalType(), list, username) ;
    eventQuery = new EventQuery() ;
    eventQuery.setCalendarId(new String[] { movedCal.getId()}) ;
    assertEquals(1,calendarService_.getEvents(username, eventQuery, new String[] {}).size()) ;

    calendarService_.removeEventCategory(username, eventCategory.getId()) ;
    calendarService_.removeUserCalendar(username, cal.getId()) ;
    calendarService_.removeCalendarCategory(username, calCategory.getId()) ;
  } 
  public void testLastUpdatedTime() throws Exception {
    CalendarCategory calCategory = new CalendarCategory();
    calCategory.setName("CalendarCategoryName");
    calCategory.setDescription("CaldendarCategoryDescription");
    calendarService_.saveCalendarCategory(username, calCategory, true);

    Calendar cal = new Calendar();
    cal.setName("CalendarName") ;
    cal.setDescription("CalendarDesscription") ;
    cal.setCategoryId(calCategory.getId()) ;
    cal.setPublic(true) ;
    calendarService_.savePublicCalendar(cal, true, username) ;

    EventCategory eventCategory = new EventCategory();
    eventCategory.setName("LastUpdatedTimeEventCategoryName");
    eventCategory.setDescription("EventCategoryDescription");
    calendarService_.saveEventCategory(username, eventCategory, true) ;

    CalendarEvent calEvent = new CalendarEvent();
    calEvent.setEventCategoryId(eventCategory.getName());
    calEvent.setSummary("Have a meeting");
    java.util.Calendar fromCal = java.util.Calendar.getInstance();
    java.util.Calendar toCal = java.util.Calendar.getInstance(); 
    toCal.add(java.util.Calendar.HOUR, 1) ;
    calEvent.setFromDateTime(fromCal.getTime());
    calEvent.setToDateTime(toCal.getTime());
    calendarService_.savePublicEvent(cal.getId(), calEvent , true);

    CalendarEvent event = calendarService_.getGroupEvent(cal.getId(), calEvent.getId());
    Date createdDate = event.getLastUpdatedTime();
    assertNotNull(createdDate);
    event.setSummary("Have a new meeting");
    calendarService_.savePublicEvent(cal.getId(), event , false);
    Date modifiedDate = calendarService_.getGroupEvent(cal.getId(), event.getId()).getLastUpdatedTime();
    assertNotNull(modifiedDate);
    assertTrue(modifiedDate.after(createdDate));

    calendarService_.removeEventCategory(username, eventCategory.getId()) ;
    calendarService_.removeUserCalendar(username, cal.getId()) ;
    calendarService_.removeCalendarCategory(username, calCategory.getId()) ;
  }

  public void testFeed() throws Exception {
    CalendarCategory calCategory = new CalendarCategory();
    calCategory.setName("CalendarCategoryName");
    calendarService_.saveCalendarCategory(username, calCategory, true);

    Calendar cal = new Calendar();
    cal.setName("CalendarName") ;
    cal.setCategoryId(calCategory.getId()) ;
    cal.setPublic(false) ;
    calendarService_.saveUserCalendar(username, cal, true) ;

    EventCategory eventCategory = new EventCategory();
    eventCategory.setName("EventCategoryName3");
    eventCategory.setDescription("EventCategoryDescription");
    calendarService_.saveEventCategory(username, eventCategory, true) ;

    CalendarEvent calEvent = new CalendarEvent();
    calEvent.setEventCategoryId(eventCategory.getName());
    calEvent.setSummary("Have a meeting");
    java.util.Calendar fromCal = java.util.Calendar.getInstance();
    java.util.Calendar toCal = java.util.Calendar.getInstance(); 
    toCal.add(java.util.Calendar.HOUR, 1) ;
    calEvent.setFromDateTime(fromCal.getTime());
    calEvent.setToDateTime(toCal.getTime());
    calendarService_.saveUserEvent(username, cal.getId(), calEvent , true);

    LinkedHashMap<String, Calendar> calendars = new LinkedHashMap<String, Calendar>();
    calendars.put(Utils.PRIVATE_TYPE + Utils.COLON + cal.getId(), cal);
    RssData rssData = new RssData() ;

    String name = "RSS";
    rssData.setName(name + Utils.RSS_EXT) ;
    String url = "http://localhost:8080/csdemo/rest-csdemo/cs/calendar/feed/" + username + Utils.SLASH
    + name + Utils.SLASH + IdGenerator.generate() + Utils.RSS_EXT;
    rssData.setUrl(url) ;
    rssData.setTitle(name) ;
    rssData.setDescription("Description");
    rssData.setLink(url);
    rssData.setVersion("rss_2.0") ; 

    calendarService_.generateRss(username, calendars, rssData);
    assertEquals(1, calendarService_.getFeeds(username).size());
    calendarService_.removeFeedData(username, name);
    assertEquals(0, calendarService_.getFeeds(username).size());

    calendarService_.removeEventCategory(username, eventCategory.getId()) ;
    calendarService_.removeUserCalendar(username, cal.getId()) ;
    calendarService_.removeCalendarCategory(username, calCategory.getId()) ;
  }



}