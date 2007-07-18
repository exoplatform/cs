/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reservd.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.service.test;

import java.util.List;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarCategory;
import org.exoplatform.calendar.service.EventCategory;

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
    Calendar cal = new Calendar() ;
    cal.setId("id") ;
    cal.setName("myCalendar") ;
    cal.setDescription("Desscription") ;
    cal.setCategoryId("category1") ;
    cal.setPrivate(true) ;
    //create/get calendar in private folder
    calendarService_.saveCalendar("nqhung", cal, true) ;
    Calendar myCal = calendarService_.getCalendar("nqhung", "id") ;
    assertNotNull(myCal) ;
    assertEquals(myCal.getName(), "myCalendar") ;
    
    //create/get calendar in public folder
    cal.setPrivate(false) ;
    cal.setGroups(new String[] {"users", "admin"}) ;
    cal.setViewPermission(new String [] {"member:/users", "member:/admin"}) ;
    cal.setEditPermission(new String [] {"admin"}) ;
    calendarService_.saveCalendar("nqhung", cal, true) ;
    myCal = calendarService_.getCalendar("id") ;
    assertNotNull(myCal) ;
    assertEquals(myCal.getName(), "myCalendar") ;
    
    //get calendar in private folder by categoryID
    List<Calendar> calendares = calendarService_.getCalendarsByCategory("nqhung", "category1") ;
    assertNotNull(calendares);
    assertEquals(calendares.size(), 1) ;
    
    //get calendar in public folder by groupId
    calendares = calendarService_.getCalendarsByGroup("users") ;
    assertNotNull(calendares);
    assertEquals(calendares.size(), 1) ;
    
    calendares = calendarService_.getCalendarsByGroup("admin") ;
    assertNotNull(calendares);
    assertEquals(calendares.size(), 1) ;
    
    calendares = calendarService_.getCalendarsByGroup("admin1") ;
    assertNotNull(calendares);
    assertEquals(calendares.size(), 0) ;
    
    //update public calendar 
    cal.setPrivate(false) ;
    cal.setName("myCalendarUpdated") ;
    calendarService_.saveCalendar("nqhung", cal, false) ;
    myCal = calendarService_.getCalendar("id") ;
    assertEquals(myCal.getName(),"myCalendarUpdated") ; 
    
    //remove public calendar
    Calendar removeCal = calendarService_.removeCalendar("id") ;
    assertEquals(removeCal.getName(), "myCalendarUpdated") ;
    
    //remove private calendar
    removeCal = calendarService_.removeCalendar("nqhung", "id") ;
    assertEquals(removeCal.getName(), "myCalendar") ;
  }
  
  public void testCalendarCategory() throws Exception {
    CalendarCategory calCategory = new CalendarCategory() ;
    calCategory.setId("categoryId") ;
    calCategory.setName("categoryName") ;
    calCategory.setDescription("Description") ;
    calCategory.setCalendars(new String [] {"calendar1", "calendar2"}) ;
    calendarService_.saveCalendarCategory("nqhung", calCategory, true) ;
    List<CalendarCategory> categories = calendarService_.getCalendarCategories("nqhung") ;
    assertEquals(categories.size(), 1) ;
    
    //get calendar category
    calCategory = calendarService_.getCalendarCategory("nqhung", "categoryId") ;
    assertEquals(calCategory.getName(), "categoryName") ;
    
    // update calendar category
    calCategory.setName("categoryNameUpdated") ;
    calendarService_.saveCalendarCategory("nqhung", calCategory, false) ;
    
    //remove calendar category
    CalendarCategory removeCate = calendarService_.removeCalendarCategory("nqhung", "categoryId") ;
    assertEquals(removeCate.getName(), "categoryNameUpdated") ;
  }
  
  public void testEventCategory() throws Exception {
    Calendar cal = new Calendar() ;
    cal.setId("calendarId") ;
    cal.setName("myCalendar") ;
    cal.setDescription("Desscription") ;
    cal.setCategoryId("category1") ;
    cal.setPrivate(true) ;
    //create/get calendar in private folder
    calendarService_.saveCalendar("nqhung", cal, true) ;
    Calendar myCal = calendarService_.getCalendar("nqhung", "calendarId") ;
    assertNotNull(myCal) ;
    assertEquals(myCal.getName(), "myCalendar") ;
    
    EventCategory eventCategory = new EventCategory() ;
    eventCategory.setId("eventCategoryId") ;
    eventCategory.setName("eventCategoryName") ;
    eventCategory.setDescription("description") ;
    calendarService_.saveEventCategory("nqhung", "calendarId", eventCategory, true) ;
    
    //get Event Category
    EventCategory eventCat = calendarService_.getEventCategory("nqhung", "calendarId", "eventCategoryId") ;
    assertEquals(eventCat.getName(), "eventCategoryName") ;
    
    //get Event categories
    List<EventCategory> eventCategories = calendarService_.getEventCategories("nqhung", "calendarId") ;
    assertEquals(eventCategories.size(), 1) ;
    
    //update Event category
    eventCategory.setName("eventCategoryNameUpdated") ;
    calendarService_.saveEventCategory("nqhung", "calendarId", eventCategory, false) ;
    
    //remove Event category
    eventCat = calendarService_.removeEventCategory("nqhung", "calendarId", "eventCategoryId") ;
    assertEquals(eventCat.getName(), "eventCategoryNameUpdated") ;
    
    calendarService_.removeCalendar("nqhung", "calendarId") ;
  }
  
  public void testEvent() throws Exception {
    assertNull(null) ;
  }
}