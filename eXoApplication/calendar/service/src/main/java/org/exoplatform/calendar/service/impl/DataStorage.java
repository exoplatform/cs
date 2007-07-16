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
import org.exoplatform.calendar.service.Task;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Jul 2, 2007  
 */
public interface DataStorage {
  
  public void createEventCategory(String username, EventCategory category) throws Exception ;
  public List<EventCategory> getEventCategories(String username) throws Exception ;
  public EventCategory getEventCategory(String username, String categoryId) throws Exception ;
  public void updateEventCategory(String username, EventCategory category) throws Exception ;
  public void removeEventCategory(String username, String categoryId) throws Exception ;
  
  public void createCalendar(String username, Calendar calendar) throws Exception ;
  public List<Calendar> getCalendarsByCategory(String username, String calendarCategoryId) throws Exception ;
  public List<Calendar> getCalendarsByGroup(String groupName) throws Exception ;
  public Calendar getCalendar(String username, String calendarId) throws Exception ;
  public Calendar getCalendar(String calendarId) throws Exception ;  
  public void updateCalendar(String username, Calendar calendar) throws Exception ;
  public void removeCalendar(String username, String calendarId) throws Exception ;
  public void removeCalendar(String calendarId) throws Exception ;
  
  public void createEvent(String username, Event event) throws Exception ;
  public void createEvent(Event event) throws Exception ;
  public List<Event> getEventByCategory(String categoryId) throws Exception ;
  public List<Event> getEventByCalendar(String username, String calendarId) throws Exception ;
  public List<Event> getEventByCalendar(String calendarId) throws Exception ;
  public Event getEvent(String username, String eventId) throws Exception ;
  public Event getEvent(String eventId) throws Exception ;
  public void updateEvent(String username, Event event) throws Exception ;
  public void updateEvent(Event event) throws Exception ;
  public void removeEvent(String username, String eventId) throws Exception ;
  public void removeEvent(String eventId) throws Exception ;
  
  public void createTask(String username, Task task) throws Exception ;
  public void createTask(Task task) throws Exception ;
  public List<Task> getTaskByCalendar(String username, String calendarId) throws Exception ;
  public List<Task> getTaskByCalendar(String calendarId) throws Exception ;
  public Task getTask(String username, String taskId) throws Exception ;
  public Task getTask(String taskId) throws Exception ;
  public void updateTask(String username, Task task) throws Exception ;
  public void updateTask(Task task) throws Exception ;
  public void removeTask(String username, String taskId) throws Exception ;
  public void removeTask(String taskId) throws Exception ;
  
  public void createCalendarCategory(String username, CalendarCategory calendarCategory) throws Exception ;
  public List<CalendarCategory> getCalendarCategories(String username) throws Exception ;
  public void updateCalendarCategory(String username, CalendarCategory calendarCategory) throws Exception ;
  public void removeCalendarCategory(String username, String calendarCategoryId) throws Exception ;
  
}
