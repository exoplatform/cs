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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jcr.PathNotFoundException;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarCategory;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.calendar.service.impl.CalendarServiceImpl;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.impl.UserImpl;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * July 3, 2008  
 */


public class TestCalendarService extends BaseCalendarServiceTestCase{
	private CalendarService calendarService_ ;
	private SessionProvider sProvider_ ;
  private final static String username = "root".intern() ;
  
	public void setUp() throws Exception {
    super.setUp() ;
    calendarService_ = (CalendarService) container.getComponentInstanceOfType(CalendarService.class) ;
    SessionProviderService sessionProviderService = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class) ;
    sProvider_ = sessionProviderService.getSystemSessionProvider(null) ;
  }
  
  public void testCalendarService() throws Exception { }
  
public void testCalendar() throws Exception {
    CalendarCategory calCategory = new CalendarCategory() ;
    calCategory.setName("categoryName") ;
    calCategory.setDescription("Description") ;
    calendarService_.saveCalendarCategory(sProvider_, "root", calCategory, true) ;

    //create/get calendar in private folder
    Calendar cal = new Calendar() ;
    cal.setName("myCalendar") ;
    cal.setDescription("Desscription") ;
    cal.setCategoryId(calCategory.getId()) ;
    cal.setPublic(true) ;    
    calendarService_.saveUserCalendar(sProvider_, username, cal, true) ;
    Calendar myCal = calendarService_.getUserCalendar(sProvider_,username, cal.getId()) ;
    assertNotNull(myCal) ;
    assertEquals(myCal.getName(), "myCalendar") ;
    
    // shared calendar, get shared calendars 
    List<String> receiverUser = new ArrayList<String>() ;
    receiverUser.add("sharedUser") ;
    calendarService_.shareCalendar(sProvider_, username, cal.getId(), receiverUser) ;
    Calendar calendar = calendarService_.getSharedCalendars(sProvider_, "sharedUser", true).getCalendarById(cal.getId()) ;
    assertEquals("myCalendar", calendar.getName()) ;
    
    CalendarEvent calendarEvent = new CalendarEvent() ;
    calendarEvent.setCalendarId(cal.getId()) ;
    calendarEvent.setSummary("calendarEvent") ;
    calendarEvent.setEventType(CalendarEvent.TYPE_EVENT) ;
    calendarEvent.setFromDateTime(new Date()) ;
    calendarEvent.setToDateTime(new Date()) ;
    calendarService_.saveEventToSharedCalendar(sProvider_, "sharedUser", cal.getId(), calendarEvent, true) ;
    List<String> calendarIds = new ArrayList<String>() ;
    calendarIds.add(cal.getId()) ;
    
    CalendarEvent event = calendarService_.getUserEventByCalendar(sProvider_, username, calendarIds).get(0) ;
    assertEquals("calendarEvent", event.getSummary()) ;
    calendarService_.removeSharedCalendar(sProvider_, "sharedUser", cal.getId()) ;
    assertNull(calendarService_.getSharedCalendars(sProvider_, "sharedUser", true)) ;
    
    //create/get calendar in public folder
    cal.setPublic(false) ;
    cal.setGroups(new String[] {"users", "admin"}) ;
    cal.setViewPermission(new String [] {"member:/users", "member:/admin"}) ;
    cal.setEditPermission(new String [] {"admin"}) ;
    calendarService_.savePublicCalendar(sProvider_, cal, true, username) ;
    Calendar publicCal = calendarService_.getGroupCalendar(sProvider_, cal.getId()) ;
    assertNotNull(publicCal) ;
    assertEquals(publicCal.getName(), "myCalendar") ;
    
    //get calendar in private folder by categoryID
    List<Calendar> calendares = calendarService_.getUserCalendarsByCategory(sProvider_, username, calCategory.getId()) ;
    assertNotNull(calendares);
    assertEquals(calendares.size(), 1) ;
    
    //get calendar in public folder by groupId
    List<GroupCalendarData> groupCalendarList = calendarService_.getGroupCalendars(sProvider_, new String[] {"users"}, true, username) ;
    assertNotNull(groupCalendarList);
    assertEquals(groupCalendarList.size(), 1) ;
    
    groupCalendarList = calendarService_.getGroupCalendars(sProvider_, new String[] {"admin"}, true, username);
    assertNotNull(groupCalendarList);
    assertEquals(groupCalendarList.size(), 1) ;
    
    groupCalendarList = calendarService_.getGroupCalendars(sProvider_, new String[] {"admin1"}, true, username) ;
    assertNotNull(groupCalendarList);
    assertEquals(groupCalendarList.size(), 0) ;
    
    //update public calendar 
    cal.setPublic(false) ;
    cal.setName("myCalendarUpdated") ;
    calendarService_.savePublicCalendar(sProvider_, cal, false, username) ;
    myCal = calendarService_.getGroupCalendar(sProvider_, cal.getId()) ;
    assertEquals(myCal.getName(),"myCalendarUpdated") ;
    
    //remove public calendar
    Calendar removeCal = calendarService_.removePublicCalendar(sProvider_,cal.getId()) ;
    assertEquals(removeCal.getName(), "myCalendarUpdated") ;
    
    //remove private calendar
    removeCal = calendarService_.removeUserCalendar(sProvider_, username, cal.getId()) ;
    assertEquals(removeCal.getName(), "myCalendar") ;
    
    //remove private calendar category
    assertNotNull(calendarService_.removeCalendarCategory(sProvider_, username, calCategory.getId())) ;
  }
  

  public void testCalendarCategory() throws Exception {
    CalendarCategory calCategory = new CalendarCategory() ;
    calCategory.setName("categoryName") ;
    calCategory.setDescription("Description") ;
    //calCategory.setCalendars(new String [] {"calendar1", "calendar2"}) ;
    calendarService_.saveCalendarCategory(sProvider_, username, calCategory, true) ;
    List<GroupCalendarData> categories = calendarService_.getCalendarCategories(sProvider_, username, true) ;
    assertEquals(categories.size(), 1) ;
    
    //get calendar category
    calCategory = calendarService_.getCalendarCategory(sProvider_, username, calCategory.getId()) ;
    assertEquals(calCategory.getName(), "categoryName") ;
    
    // update calendar category
    calCategory.setName("categoryNameUpdated") ;
    calendarService_.saveCalendarCategory(sProvider_, username, calCategory, false) ;
    
    //remove calendar category
    CalendarCategory removeCate = calendarService_.removeCalendarCategory(sProvider_, username, calCategory.getId()) ;
    assertEquals(removeCate.getName(), "categoryNameUpdated") ;
  }

  public void testEventCategory() throws Exception {
    CalendarCategory calCategory = new CalendarCategory() ;
    calCategory.setName("categoryName") ;
    calCategory.setDescription("Description") ;
    //calCategory.setCalendars(new String [] {""}) ;
    calendarService_.saveCalendarCategory(sProvider_, username, calCategory, true) ;
    
    Calendar cal = new Calendar() ;
    cal.setName("myCalendar") ;
    cal.setDescription("Desscription") ;
    cal.setCategoryId(calCategory.getId()) ;
    cal.setPublic(true) ;
    //create/get calendar in private folder
    calendarService_.saveUserCalendar(sProvider_, username, cal, true) ;
    Calendar myCal = calendarService_.getUserCalendar(sProvider_, username, cal.getId()) ;
    assertNotNull(myCal) ;
    assertEquals(myCal.getName(), "myCalendar") ;

    EventCategory eventCategory = new EventCategory() ;
    String name = "eventCategoryName" ;
    eventCategory.setName(name) ;
    eventCategory.setDescription("description") ;
    calendarService_.saveEventCategory(sProvider_, username, eventCategory, null, true) ;
    assertNotNull(calendarService_.getEventCategory(sProvider_, username, name.toLowerCase())) ;

    // import, export calendar
    CalendarEvent calendarEvent = new CalendarEvent() ;
    calendarEvent.setCalendarId(cal.getId()) ;
    calendarEvent.setSummary("sum") ;
    calendarEvent.setEventType(CalendarEvent.TYPE_EVENT) ;
    calendarEvent.setFromDateTime(new Date()) ;
    calendarEvent.setToDateTime(new Date()) ;
    calendarService_.saveUserEvent(sProvider_, username, cal.getId(), calendarEvent, true) ;
    
    List<String> calendarIds = new ArrayList<String>() ;
    calendarIds.add(cal.getId()) ;
    OutputStream out = calendarService_.getCalendarImportExports(
        CalendarServiceImpl.ICALENDAR).exportCalendar(sProvider_, username, calendarIds, "0") ;
    ByteArrayInputStream is = new ByteArrayInputStream(out.toString().getBytes()) ;
    
    assertNotNull(calendarService_.removeUserEvent(sProvider_, username, cal.getId(), calendarEvent.getId())) ;
    assertEquals(0, calendarService_.getUserEventByCalendar(sProvider_, username, calendarIds).size()) ;
    assertNotNull(calendarService_.removeUserCalendar(sProvider_, username, cal.getId())) ;
    
    calendarService_.getCalendarImportExports(CalendarServiceImpl.ICALENDAR)
     .importCalendar(sProvider_, username, is, "importedCalendar") ;
    List<Calendar> cals = calendarService_.getUserCalendars(sProvider_, username, true) ;
    List<String> newCalendarIds = new ArrayList<String>() ;
    for (Calendar calendar : cals) newCalendarIds.add(calendar.getId()) ;
    List<CalendarEvent> events = calendarService_.getUserEventByCalendar(sProvider_, username, newCalendarIds) ;
    assertEquals(events.get(0).getSummary(), "sum") ;
    
    //update Event category
    calendarService_.saveEventCategory(sProvider_, username, eventCategory
        , new String[] { name, "descriptionUpdate"}, false) ;
    String des = calendarService_.getEventCategory(sProvider_, username, name.toLowerCase()).getDescription() ;
    assertEquals(des, "descriptionUpdate") ;
    
    //remove Event category
    calendarService_.removeEventCategory(sProvider_, username, eventCategory.getName()) ;

    assertNotNull(calendarService_.removeUserCalendar(sProvider_, username, newCalendarIds.get(0))) ;
    assertNotNull(calendarService_.removeCalendarCategory(sProvider_, username, calCategory.getId())) ;
  }
  
}