/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reservd.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.service.test;

import java.util.List;

import org.exoplatform.calendar.service.Calendar;

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
    calendarService_.createCalendar("nqhung", cal) ;
    Calendar myCal = calendarService_.getCalendar("nqhung", "id") ;
    assertNotNull(myCal) ;
    assertEquals(myCal.getName(), "myCalendar") ;
    
    //create/get calendar in public folder
    cal.setPrivate(false) ;
    cal.setGroups(new String[] {"users", "admin"}) ;
    cal.setViewPermission(new String [] {"member:/users", "member:/admin"}) ;
    cal.setEditPermission(new String [] {"admin"}) ;
    calendarService_.createCalendar("nqhung", cal) ;
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
    
    //update calendar
    cal.setName("myCalendarUpdated") ;
    calendarService_.updateCalendar("nqhung", cal) ;
    myCal = calendarService_.getCalendar("nqhung", "id") ;
    assertEquals(myCal.getName(),"myCalendarUpdated") ; 
  }
}