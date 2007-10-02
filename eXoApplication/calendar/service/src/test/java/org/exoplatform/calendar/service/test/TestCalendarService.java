/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reservd.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.service.test;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarCategory;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.calendar.service.GroupCalendarData;
import org.exoplatform.calendar.service.Reminder;
import org.exoplatform.calendar.service.impl.ICalendarImportExport;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * July 3, 2007  
 */
public class TestCalendarService extends BaseCalendarTestCase{

  public void testCalendarService() throws Exception {
    assertNull(null) ;
  }
  
  public void testCalendar() throws Exception {
    assertNull(null) ;
    
    CalendarCategory calCategory = new CalendarCategory() ;
    //calCategory.setId("categoryId") ;
    calCategory.setName("categoryName") ;
    calCategory.setDescription("Description") ;
    //calCategory.setCalendars(new String [] {""}) ;
    calendarService_.saveCalendarCategory("nqhung", calCategory, true) ;
    
    
    Calendar cal = new Calendar() ;
    //cal.setId("id") ;
    cal.setName("myCalendar") ;
    cal.setDescription("Desscription") ;
    cal.setCategoryId(calCategory.getId()) ;
    cal.setPublic(true) ;
    //create/get calendar in private folder
    calendarService_.saveUserCalendar("nqhung", cal, true) ;
    Calendar myCal = calendarService_.getUserCalendar("nqhung", cal.getId()) ;
    assertNotNull(myCal) ;
    assertEquals(myCal.getName(), "myCalendar") ;
    
    //create/get calendar in public folder
    cal.setPublic(false) ;
    cal.setGroups(new String[] {"users", "admin"}) ;
    cal.setViewPermission(new String [] {"member:/users", "member:/admin"}) ;
    cal.setEditPermission(new String [] {"admin"}) ;
    calendarService_.saveGroupCalendar(cal, true) ;
    myCal = calendarService_.getGroupCalendar(cal.getId()) ;
    assertNotNull(myCal) ;
    assertEquals(myCal.getName(), "myCalendar") ;
    
    //get calendar in private folder by categoryID
    List<Calendar> calendares = calendarService_.getUserCalendarsByCategory("nqhung", calCategory.getId()) ;
    assertNotNull(calendares);
    assertEquals(calendares.size(), 1) ;
    
    //get calendar in public folder by groupId
    List<GroupCalendarData> groupCalendarList = calendarService_.getGroupCalendars(new String[] {"users"}) ;
    assertNotNull(groupCalendarList);
    assertEquals(groupCalendarList.size(), 1) ;
    
    groupCalendarList = calendarService_.getGroupCalendars(new String[] {"admin"});
    assertNotNull(groupCalendarList);
    assertEquals(groupCalendarList.size(), 1) ;
    
    groupCalendarList = calendarService_.getGroupCalendars(new String[] {"admin1"}) ;
    assertNotNull(groupCalendarList);
    assertEquals(groupCalendarList.size(), 0) ;
    
    //update public calendar 
    cal.setPublic(false) ;
    cal.setName("myCalendarUpdated") ;
    calendarService_.saveGroupCalendar(cal, false) ;
    myCal = calendarService_.getGroupCalendar(cal.getId()) ;
    assertEquals(myCal.getName(),"myCalendarUpdated") ; 
    
    //remove public calendar
    Calendar removeCal = calendarService_.removeGroupCalendar(cal.getId()) ;
    assertEquals(removeCal.getName(), "myCalendarUpdated") ;
    
    //remove private calendar
    removeCal = calendarService_.removeUserCalendar("nqhung", cal.getId()) ;
    assertEquals(removeCal.getName(), "myCalendar") ;
    
    //remove private calendar category
    assertNotNull(calendarService_.removeCalendarCategory("nqhung", calCategory.getId())) ;
  }
  
  public void testCalendarCategory() throws Exception {
    CalendarCategory calCategory = new CalendarCategory() ;
    //calCategory.setId("categoryId") ;
    calCategory.setName("categoryName") ;
    calCategory.setDescription("Description") ;
    //calCategory.setCalendars(new String [] {"calendar1", "calendar2"}) ;
    calendarService_.saveCalendarCategory("nqhung", calCategory, true) ;
    List<GroupCalendarData> categories = calendarService_.getCalendarCategories("nqhung") ;
    assertEquals(categories.size(), 1) ;
    
    //get calendar category
    calCategory = calendarService_.getCalendarCategory("nqhung", calCategory.getId()) ;
    assertEquals(calCategory.getName(), "categoryName") ;
    
    // update calendar category
    calCategory.setName("categoryNameUpdated") ;
    calendarService_.saveCalendarCategory("nqhung", calCategory, false) ;
    
    //remove calendar category
    CalendarCategory removeCate = calendarService_.removeCalendarCategory("nqhung", calCategory.getId()) ;
    assertEquals(removeCate.getName(), "categoryNameUpdated") ;
  }
  
  public void testEventCategory() throws Exception {
    CalendarCategory calCategory = new CalendarCategory() ;
    calCategory.setName("categoryName") ;
    calCategory.setDescription("Description") ;
    //calCategory.setCalendars(new String [] {""}) ;
    calendarService_.saveCalendarCategory("nqhung", calCategory, true) ;
    
    Calendar cal = new Calendar() ;
    cal.setName("myCalendar") ;
    cal.setDescription("Desscription") ;
    cal.setCategoryId(calCategory.getId()) ;
    cal.setPublic(true) ;
    //create/get calendar in private folder
    calendarService_.saveUserCalendar("nqhung", cal, true) ;
    Calendar myCal = calendarService_.getUserCalendar("nqhung", cal.getId()) ;
    assertNotNull(myCal) ;
    assertEquals(myCal.getName(), "myCalendar") ;
    
    EventCategory eventCategory = new EventCategory() ;
    eventCategory.setName("eventCategoryName") ;
    eventCategory.setDescription("description") ;
    calendarService_.saveEventCategory("nqhung", eventCategory, true) ;
    
    //update Event category
    eventCategory.setDescription("eventCategoryNameUpdated") ;
    calendarService_.saveEventCategory("nqhung", eventCategory, false) ;
    
    //remove Event category
    EventCategory eventCat = calendarService_.removeEventCategory("nqhung", eventCategory.getName()) ;
    assertNotNull(eventCat) ;
    assertEquals(eventCat.getDescription(), "eventCategoryNameUpdated") ;
    
    calendarService_.removeUserCalendar("nqhung", cal.getId()) ;
    calendarService_.removeCalendarCategory("nqhung", calCategory.getId()) ;
  }
  
  public void testCalendarEvent() throws Exception {
    CalendarCategory calCategory = new CalendarCategory() ;
    //calCategory.setId("categoryId") ;
    calCategory.setName("categoryName") ;
    calCategory.setDescription("Description") ;
    //calCategory.setCalendars(new String [] {""}) ;
    calendarService_.saveCalendarCategory("nqhung", calCategory, true) ;
    
    Calendar cal = new Calendar() ;
    //cal.setId("calendarId") ;
    cal.setName("myCalendar") ;
    cal.setDescription("Desscription") ;
    cal.setCategoryId(calCategory.getId()) ;
    cal.setPublic(true) ;
    calendarService_.saveUserCalendar("nqhung", cal, true) ;
    
    EventCategory eventCategory = new EventCategory() ;
    //eventCategory.setId("eventCategoryId") ;
    eventCategory.setName("eventCategoryName") ;
    eventCategory.setDescription("description") ;
    calendarService_.saveEventCategory("nqhung", eventCategory, true) ;
    
    CalendarEvent event = new CalendarEvent() ;
    //event.setId("eventId") ;
    event.setCalendarId(cal.getId()) ;
    event.setEventCategoryId(eventCategory.getName()) ;
    event.setDescription("description") ;
    event.setSummary("myEvent") ;
    event.setEventState("free") ;
    event.setEventType("event") ;
    event.setFromDateTime(new Date()) ;
    event.setToDateTime(new Date()) ;
    event.setInvitation(new String [] {"nqhung@yahoo.com", "dvminh@yahoo.com", "ptuan@yahoo.com"}) ;
    event.setLocation("meeting room") ;
    event.setPriority("1") ;
    event.setPrivate(true) ;
    
    Reminder reminder = new Reminder() ;
    //reminder.setId("reminderId") ;
    reminder.setEventId("eventId") ;
    reminder.setAlarmBefore("5") ;
    reminder.setReminder("via mail") ;
    reminder.setSnooze(2) ;
    List<Reminder> reminders = new ArrayList<Reminder>() ;
    reminders.add(reminder) ;
    
    event.setReminders(reminders) ;
    
    calendarService_.saveUserEvent("nqhung", cal.getId(), event, true) ;
    CalendarEvent ev = calendarService_.getUserEvent("nqhung", cal.getId(), event.getId()) ;
    assertNotNull(ev) ;
    
    //update event
    event.setSummary("myEventUpdated") ;
    event.setStatus("TENTATIVE") ;
    calendarService_.saveUserEvent("nqhung", cal.getId(), event, false) ;
    ev = calendarService_.getUserEvent("nqhung", cal.getId(), event.getId()) ;
    assertEquals(event.getSummary(), "myEventUpdated") ;
    
    //get event list
    List<String> calendarIds = new ArrayList<String>() ;
    calendarIds.add(cal.getId()) ;
    List<CalendarEvent> events = calendarService_.getUserEventByCalendar("nqhung", calendarIds) ;
    assertEquals(events.size(), 1) ;
    
    //export/import ical
    ICalendarImportExport importExport = (ICalendarImportExport)calendarService_.getCalendarImportExports("ICalendar");
    OutputStream ical = importExport.exportCalendar("nqhung", calendarIds) ;
    System.out.println("\n\n\n" + ical.toString()) ;
    ByteArrayInputStream icalInputStream = new ByteArrayInputStream(ical.toString().getBytes()) ;
    importExport.importCalendar("nqhung", icalInputStream, "importClanedar") ;
    List<GroupCalendarData> cateList = calendarService_.getCalendarCategories("nqhung") ;
    assertEquals(cateList.size(), 2) ;
    List<Calendar> calList = calendarService_.getUserCalendars("nqhung") ;
    assertEquals(calList.size(), 2) ;
    //remove event
    ev = calendarService_.removeUserEvent("nqhung", cal.getId(), event.getId()) ;
    assertNotNull(ev);     
    
  }
}