/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.service.impl;

import java.util.List;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarCategory;
import org.exoplatform.calendar.service.Event;
import org.exoplatform.calendar.service.EventCategory;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Jul 2, 2007  
 */
public interface DataStorage {
  
  public List<CalendarCategory> getCalendarCategories(String username) throws Exception ;
  public CalendarCategory getCalendarCategory(String username, String calendarCategoryId) throws Exception ;
  public void saveCalendarCategory(String username, CalendarCategory calendarCategory, boolean isNew) throws Exception ; 
  public CalendarCategory removeCalendarCategory(String username, String calendarCategoryId) throws Exception ;
  
  
  public Calendar getCalendar(String username, String calendarId) throws Exception ;
  public Calendar getCalendar(String calendarId) throws Exception ;
  public List<Calendar> getCalendarsByCategory(String username, String calendarCategoryId) throws Exception ;
  public List<Calendar> getCalendarsByGroup(String groupName) throws Exception ;  
  public void saveCalendar(String username, Calendar calendar, boolean isNew) throws Exception ;
  public Calendar removeCalendar(String username, String calendarId) throws Exception ;
  public Calendar removeCalendar(String calendarId) throws Exception ;
  
  
  public EventCategory getEventCategory(String username, String calendarId, String eventCategoryId) throws Exception ;
  public List<EventCategory> getEventCategories(String username, String calendarId) throws Exception ;  
  public void saveEventCategory(String username, String calendarId, EventCategory eventCategory, boolean isNew) throws Exception ;
  public EventCategory removeEventCategory(String username, String calendarId, String eventCategoryId) throws Exception ;
  
  
  public Event getEvent(String username, String calendarId, String eventCategoryId, String eventId) throws Exception ;
  public Event getEvent(String calendarId, String eventCategoryId, String eventId) throws Exception ;
  public List<Event> getEventByCalendar(String username, String calendarId) throws Exception ;
  public List<Event> getEventByCalendar(String calendarId) throws Exception ;
  public void saveEvent(String username, String calendarId, String eventCategoryId, Event event, boolean isNew, boolean isPublicCalendar) throws Exception ;
  public Event removeEvent(String username, String calendarId, String eventCategoryId, String eventId, boolean isPublicCalendar) throws Exception ;
  
}
