/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.service.impl;

import java.util.List;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarCategory;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.Event;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.registry.JCRRegistryService;
import org.exoplatform.services.jcr.RepositoryService;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public class CalendarServiceImpl implements CalendarService{
  private JCRDataStorage storage_ ;
  public CalendarServiceImpl(RepositoryService  repositoryService, 
                             JCRRegistryService jcrRegistryService) throws Exception {
    storage_ = new JCRDataStorage(repositoryService, jcrRegistryService) ;
  }
  public void createCalendarCategory(String username, CalendarCategory calendarCategory, boolean isNew) throws Exception {
    // TODO Auto-generated method stub
    
  }
  public void createEvent(String username, String calendarId, String eventCategoryId, Event event, boolean isNew) throws Exception {
    // TODO Auto-generated method stub
    
  }
  public void createEvent(String calendarId, String eventCategoryId, Event event, boolean isNew) throws Exception {
    // TODO Auto-generated method stub
    
  }
  public Calendar getCalendar(String username, String calendarId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
  public Calendar getCalendar(String calendarId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
  public List<CalendarCategory> getCalendarCategories(String username) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
  public List<Calendar> getCalendarsByCategory(String username, String calendarCategoryId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
  public List<Calendar> getCalendarsByGroup(String groupName) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
  public Event getEvent(String username, String calendarId, String eventCategoryId, String eventId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
  public Event getEvent(String calendarId, String eventCategoryId, String eventId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
  public List<Event> getEventByCalendar(String username, String calendarId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
  public List<Event> getEventByCalendar(String calendarId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
  public List<EventCategory> getEventCategories(String username, String calendarId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
  public EventCategory getEventCategory(String username, String calendarId, String eventCategoryId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
  public Calendar removeCalendar(String username, String calendarId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
  public Calendar removeCalendar(String calendarId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
  public CalendarCategory removeCalendarCategory(String username, String calendarCategoryId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
  public Event removeEvent(String username, String calendarId, String eventCategoryId, String eventId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
  public void removeEvent(String calendarId, String eventCategoryId, String eventId) throws Exception {
    // TODO Auto-generated method stub
    
  }
  public EventCategory removeEventCategory(String username, String calendarId, String eventCategoryId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
  public void saveCalendar(String username, Calendar calendar, boolean isNew) throws Exception {
    // TODO Auto-generated method stub
    
  }
  public void saveEventCategory(String username, String calendarId, EventCategory eventCategory, boolean isNew) throws Exception {
    // TODO Auto-generated method stub
    
  }  

}
