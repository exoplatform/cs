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
  
  public List<CalendarCategory> getCalendarCategories(String username) throws Exception {
    return storage_.getCalendarCategories(username);
  }
  public CalendarCategory getCalendarCategory(String username, String calendarCategoryId) throws Exception {
    return storage_.getCalendarCategory(username, calendarCategoryId) ;
  }
  public void saveCalendarCategory(String username, CalendarCategory calendarCategory, boolean isNew) throws Exception {
    storage_.saveCalendarCategory(username, calendarCategory, isNew) ;
  }
  public CalendarCategory removeCalendarCategory(String username, String calendarCategoryId) throws Exception {
    return storage_.removeCalendarCategory(username, calendarCategoryId);
  }
  
  public Calendar getCalendar(String username, String calendarId) throws Exception {
    return storage_.getCalendar(username, calendarId);
  }
  public Calendar getCalendar(String calendarId) throws Exception {
    return storage_.getCalendar(calendarId);
  }
  public List<Calendar> getCalendarsByCategory(String username, String calendarCategoryId) throws Exception {
    return storage_.getCalendarsByCategory(username, calendarCategoryId);
  }
  public List<Calendar> getCalendarsByGroup(String groupName) throws Exception {
    return storage_.getCalendarsByGroup(groupName);
  }
  public void saveCalendar(String username, Calendar calendar, boolean isNew) throws Exception {
    storage_.saveCalendar(username, calendar, isNew) ;
  }
  public Calendar removeCalendar(String username, String calendarId) throws Exception {
    return storage_.removeCalendar(username, calendarId);
  }
  public Calendar removeCalendar(String calendarId) throws Exception {
    return storage_.removeCalendar(calendarId);
  }
  
  public List<EventCategory> getEventCategories(String username, String calendarId) throws Exception {
    return storage_.getEventCategories(username, calendarId);
  }
  public EventCategory getEventCategory(String username, String calendarId, String eventCategoryId) throws Exception {
    return storage_.getEventCategory(username, calendarId, eventCategoryId);
  }
  public void saveEventCategory(String username, String calendarId, EventCategory eventCategory, boolean isNew) throws Exception {
    storage_.saveEventCategory(username, calendarId, eventCategory, isNew) ;
  }
  public EventCategory removeEventCategory(String username, String calendarId, String eventCategoryId) throws Exception {
    return storage_.removeEventCategory(username, calendarId,eventCategoryId);
  }
  
  public Event getEvent(String username, String calendarId, String eventCategoryId, String eventId) throws Exception {
    return storage_.getEvent(username, calendarId, eventCategoryId, eventId);
  }
  public Event getEvent(String calendarId, String eventCategoryId, String eventId) throws Exception {
    return storage_.getEvent(calendarId, eventCategoryId, eventId);
  }
  public List<Event> getEventByCalendar(String username, String calendarId) throws Exception {
    return storage_.getEventByCalendar(username, calendarId);
  }
  public List<Event> getEventByCalendar(String calendarId) throws Exception {
    return storage_.getEventByCalendar(calendarId);
  } 
  public void saveEvent(String username, String calendarId, String eventCategoryId, Event event, boolean isNew, boolean isPublicCalendar) throws Exception {
    storage_.saveEvent(username, calendarId, eventCategoryId, event, isNew, isPublicCalendar) ;
  }
  public Event removeEvent(String username, String calendarId, String eventCategoryId, String eventId, boolean isPublicCalendar) throws Exception {
    return storage_.removeEvent(username, calendarId, eventCategoryId, eventId, isPublicCalendar);
  }
  
}
