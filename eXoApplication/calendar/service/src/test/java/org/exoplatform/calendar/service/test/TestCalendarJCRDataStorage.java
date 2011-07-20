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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarCategory;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.service.impl.JCRDataStorage;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;

/**
 * Created by The eXo Platform SAS
 * Author : phong tran
 *          phongth@exoplatform.com
 * July 20, 2011  
 */

public class TestCalendarJCRDataStorage extends BaseCalendarTestCase {
  private JCRDataStorage storage_;
  
  private static String username = "root";

  public TestCalendarJCRDataStorage() throws Exception {
    super();
    NodeHierarchyCreator nodeHierarchyCreator = (NodeHierarchyCreator) container.getComponentInstanceOfType(NodeHierarchyCreator.class);
    storage_ = new JCRDataStorage(nodeHierarchyCreator, repositoryService);
  }

  public void setUp() throws Exception {
    super.setUp();
  }
  
  public void testCreateSessionProvider() {
    try {
      SessionProvider sessionProvider = storage_.createSessionProvider();
      assertNotNull(sessionProvider);
    } catch (Exception e) {
      fail();
    }
  }
  
  public void testUpdateRecurrenceSeries() {
    try {
      CalendarCategory calendarCategory = createCalendarCategory("categoryName", "description");
      Calendar calendar = createCalendar("myCalendar", "Description", calendarCategory.getId());
      Calendar publicCalendar = createPublicCalendar("publicCalendar", "publicDescription", calendarCategory.getId());
      
      EventCategory eventCategory = createEventCategory("eventCategoryName0", "description");
      
      java.util.Calendar fromCal = java.util.Calendar.getInstance();
      java.util.Calendar toCal = java.util.Calendar.getInstance();
      toCal.add(java.util.Calendar.HOUR, 1);
      java.util.Calendar repeatUntilDate = java.util.Calendar.getInstance();
      repeatUntilDate.add(java.util.Calendar.DATE, 5);
      
      CalendarEvent userEvent = new CalendarEvent();
      userEvent.setSummary("Have a meeting");
      userEvent.setFromDateTime(fromCal.getTime());
      userEvent.setToDateTime(toCal.getTime());
      userEvent.setCalendarId(calendar.getId());
      userEvent.setEventCategoryId(eventCategory.getId());
      userEvent.setRepeatType(CalendarEvent.RP_DAILY);
      userEvent.setRepeatInterval(2);
      userEvent.setRepeatCount(3);
      userEvent.setRepeatUntilDate(repeatUntilDate.getTime());
      userEvent.setRepeatByDay(null);
      userEvent.setRepeatByMonthDay(new long[] {2, 3, 4, 5, 7} );
      storage_.saveOccurrenceEvent(username, calendar.getId(), userEvent, true);
      
      TimeZone timezone = TimeZone.getDefault();
      storage_.getOccurrenceEvents(userEvent, fromCal, toCal, timezone.toString());
      
      List<CalendarEvent> listEvent = new ArrayList<CalendarEvent>();
      listEvent.add(userEvent) ;
      storage_.updateOccurrenceEvent(calendar.getId(), publicCalendar.getId(), String.valueOf(Calendar.TYPE_PRIVATE), String.valueOf(Calendar.TYPE_PUBLIC), listEvent, username);
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }
  
  public void testCalculateRecurrenceFinishDate() {
    try {
      java.util.Calendar fromCal = java.util.Calendar.getInstance();
      fromCal.set(2011, 6, 20, 5, 30);
      
      java.util.Calendar toCal = java.util.Calendar.getInstance();
      toCal.set(2011, 6, 25, 5, 30);
      
      CalendarEvent userEvent = new CalendarEvent();
      userEvent.setFromDateTime(fromCal.getTime());
      userEvent.setToDateTime(toCal.getTime());
      userEvent.setRepeatType(CalendarEvent.RP_DAILY);
      userEvent.setRepeatInterval(2);
      userEvent.setRepeatCount(3);
      userEvent.setRepeatUntilDate(null);
      userEvent.setRepeatByDay(null);
      userEvent.setRepeatByMonthDay(new long[] {2, 3, 4, 5, 7} );
      
      Date date = storage_.calculateRecurrenceFinishDate(userEvent);
      
      java.util.Calendar calendar = java.util.Calendar.getInstance();
      calendar.setTime(date);
      
      assertEquals(2011, calendar.get(java.util.Calendar.YEAR));
      assertEquals(6, calendar.get(java.util.Calendar.MONTH));
      assertEquals(24, calendar.get(java.util.Calendar.DATE));
      assertEquals(7, calendar.get(java.util.Calendar.HOUR));
      assertEquals(0, calendar.get(java.util.Calendar.MINUTE));
    } catch (Exception e) {
      fail();
    }
  }
  
  private CalendarCategory createCalendarCategory(String name, String description) {
    try {
      // Create and save calendar category
      CalendarCategory calendarCategory = new CalendarCategory();
      calendarCategory.setName(name);
      calendarCategory.setDescription(description);
      storage_.saveCalendarCategory(username, calendarCategory, true);
      return calendarCategory;
    } catch (Exception e) {
      fail();
      return null;
    }
  }
  
  private Calendar createSharedCalendar(String name, String description, String calendarCategoryId) {
    try {
      Calendar sharedCalendar = new Calendar();
      sharedCalendar.setName(name);
      sharedCalendar.setDescription(description);
      sharedCalendar.setCategoryId(calendarCategoryId);
      sharedCalendar.setPublic(true);
      sharedCalendar.setViewPermission(new String[] { "*.*" });
      sharedCalendar.setEditPermission(new String[] { "*.*", "john" });
      storage_.saveUserCalendar(username, sharedCalendar, true);
      
      List<String> receiverUser = new ArrayList<String>();
      receiverUser.add("john");
      storage_.shareCalendar(username, sharedCalendar.getId(), receiverUser);
      return sharedCalendar;
    } catch (Exception e) {
      fail();
      return null;
    }
  }
  
  private Calendar createCalendar(String name, String desscription, String calendarCategoryId) {
    try {
      // Create and save calendar
      Calendar calendar = new Calendar();
      calendar.setName(name);
      calendar.setDescription(desscription);
      calendar.setCategoryId(calendarCategoryId);
      calendar.setPublic(false);
      storage_.saveUserCalendar(username, calendar, true);
      return calendar;
    } catch (Exception e) {
      fail();
      return null;
    }
  }
  
  private Calendar createPublicCalendar(String name, String desscription, String calendarCategoryId) {
    try {
      Calendar publicCalendar = new Calendar();
      publicCalendar.setName(name);
      publicCalendar.setDescription(desscription);
      publicCalendar.setCategoryId(calendarCategoryId);
      publicCalendar.setPublic(true);
      storage_.savePublicCalendar(publicCalendar, true, username);
      return publicCalendar;
    } catch (Exception e) {
      fail();
      return null;
    }
  }
  
  private EventCategory createEventCategory(String name, String description) {
    try {
      EventCategory eventCategory = new EventCategory();
      eventCategory.setName(name);
      eventCategory.setDescription("description");
      storage_.saveEventCategory(username, eventCategory, true);
      return eventCategory;
    } catch (Exception e) {
      fail();
      return null;
    }
  }
  
  private CalendarEvent createEvent(String calendarId, EventCategory eventCategory, String summary, java.util.Calendar fromCal, java.util.Calendar toCal) {
    try {
      CalendarEvent calendarEvent = new CalendarEvent();
      calendarEvent.setEventCategoryId(eventCategory.getId());
      calendarEvent.setEventCategoryName(eventCategory.getName());
      calendarEvent.setSummary(summary);
      calendarEvent.setFromDateTime(fromCal.getTime());
      calendarEvent.setToDateTime(toCal.getTime());
      storage_.saveUserEvent(username, calendarId, calendarEvent, true);
      return calendarEvent;
    } catch (Exception e) {
      fail();
      return null;
    }
  }
  
  private CalendarEvent createPublicEvent(String publicCalendarId, EventCategory eventCategory, String summary, java.util.Calendar fromCal, java.util.Calendar toCal) {
    try {
      CalendarEvent publicEvent = new CalendarEvent();
      publicEvent.setEventCategoryId(eventCategory.getId());
      publicEvent.setEventCategoryName(eventCategory.getName());
      publicEvent.setSummary("Have a meeting");
      publicEvent.setFromDateTime(fromCal.getTime());
      publicEvent.setToDateTime(toCal.getTime());
      storage_.savePublicEvent(publicCalendarId, publicEvent, true);
      return publicEvent;
    } catch (Exception e) {
      fail();
      return null;
    }
  }
  
  private CalendarEvent createSharedEvent(String sharedCalendarId, EventCategory eventCategory, String summary, java.util.Calendar fromCal, java.util.Calendar toCal) {
    try {
      CalendarEvent sharedEvent = new CalendarEvent();
      sharedEvent.setCalendarId(sharedCalendarId);
      sharedEvent.setSummary(summary);
      sharedEvent.setEventType(CalendarEvent.TYPE_EVENT);
      sharedEvent.setFromDateTime(fromCal.getTime());
      sharedEvent.setToDateTime(toCal.getTime());
      storage_.saveEventToSharedCalendar("john", sharedCalendarId, sharedEvent, true);
      return sharedEvent;
    } catch (Exception e) {
      fail();
      return null;
    }
  }
}
