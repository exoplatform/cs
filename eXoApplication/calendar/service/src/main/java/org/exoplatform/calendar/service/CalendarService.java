/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.service;

import java.util.List;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public interface CalendarService {
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Check exists of category
   * 3. Create and save category
   * 4. Invalidate cache 
   * @param username
   * @param category
   * @throws Exception
   */
  public void createEventCategory(String username, EventCategory category) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Check exists of categories list in cache
   * 3. Return category list and put category list to cache if not exists
   * @param username
   * @return Category list
   * @throws Exception
   */  
  public List<EventCategory> getEventCategories(String username) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Get Category and return Category object
   * @param username
   * @param categoryId
   * @return Category
   * @throws Exception
   */
  public EventCategory getEventCategory(String username, String categoryId) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Get Category and update data
   * 3. Invalidate cache
   * @param username
   * @param category
   * @throws Exception
   */
  public void updateEventCategory(String username, EventCategory category) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Check in using of event category   * 
   * 3. Remove event category 
   * 4. Invalidate cache
   * @param username
   * @param categoryId
   * @throws Exception
   */
  public void removeEventCategory(String username, String categoryId) throws Exception ;
  
  /**
   * This method should:
   * 1. Check calendar is private or public
   * 2. Save calendar in private folder or service folder(public) 
   * 3. Invalidate cache
   * @param username
   * @param calendar
   * @throws Exception
   */
  public void createCalendar(String username, Calendar calendar) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Check exists of calendar list in cache
   * 3. Get calendar list by category and return
   * @param username
   * @return Calendar list
   * @throws Exception
   */
  public List<Calendar> getCalendarsByCategory(String username, String calendarCategoryId) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node
   * 2. Check exists of calendar list in cache
   * 3. Get calendar list
   * @param username
   * @return Calendar list
   * @throws Exception
   */
  public List<Calendar> getCalendarsByGroup(String groupName) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Get calendar from private folder
   * @param username
   * @param calendarId
   * @return Calendar
   * @throws Exception
   */
  public Calendar getCalendar(String username, String calendarId) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node
   * 2. Get group node
   * 3. Get calendar
   * @param username
   * @param groupId
   * @param calendarId
   * @return Calendar
   * @throws Exception
   */
  public Calendar getCalendar(String username, String groupId, String calendarId) throws Exception ;
  
  /**
   * This method should:
   * 1. Check calendar is private or public
   * 2. Get Calendar and update data
   * 3. Invalidate cache
   * @param username
   * @param Calendar
   * @throws Exception
   */
  public void updateCalendar(String username, Calendar calendar) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node
   * 2. Get group node
   * 3. Remove calendar
   * 4. Invalidate cache
   * @param username
   * @param groupId
   * @param calendarId
   * @throws Exception
   */
  public void removeCalendar(String username, String groupId, String calendarId) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Remove calendar
   * 3. Invalidate cache
   * @param username
   * @param calendarId
   * @throws Exception
   */
  public void removeCalendar(String username, String calendarId) throws Exception ;
  
  /**
   * This method should:
   * 1. Get calendar service root node
   * 2. Check exists of event
   * 3. Create new event
   * 4. Invalidate cache
   * @param username
   * @param event
   * @throws Exception
   */
  public void createEvent(String username, Event event) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node
   * 2. Check exists of event
   * 3. Create new event
   * 4. Invalidate cache
   * @param event
   * @throws Exception
   */
  public void createEvent(Event event) throws Exception ;
  
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Check exists of event list in cache
   * 3. Get event list and return
   * @param username
   * @param categoryId
   * @return event list
   * @throws Exception
   */
  public List<Event> getEventByCategory(String username, String categoryId) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node
   * 2. Check exists of event list in cache
   * 3. Get event list and return
   * @param categoryId
   * @return event list
   * @throws Exception
   */
  public List<Event> getEventByCategory(String categoryId) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Check exists of event list in cache
   * 3. Get event list and return
   * @param username
   * @param calendarId
   * @return event list
   * @throws Exception
   */
  public List<Event> getEventByCalendar(String username, String calendarId) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Check exists of event list in cache
   * 3. Get event list and return
   * @param calendarId
   * @return event list
   * @throws Exception
   */
  public List<Event> getEventByCalendar(String calendarId) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Get Event and return Event object
   * @param username
   * @param eventId
   * @return Event
   * @throws Exception
   */
  public Event getEvent(String username, String eventId) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Get Event and return Event object
   * @param eventId
   * @return Event
   * @throws Exception
   */
  public Event getEvent(String eventId) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Get Event and update data
   * 3. Invalidate cache
   * @param username
   * @param Event
   * @throws Exception
   */
  public void updateEvent(String username, Event event) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node
   * 2. Get Event and update data
   * 3. Invalidate cache
   * @param Event
   * @throws Exception
   */
  public void updateEvent(Event event) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Remove event
   * 3. Invalidate cache
   * @param username
   * @param eventId
   * @throws Exception
   */
  public void removeEvent(String username, String eventId) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Remove event
   * 3. Invalidate cache
   * @param username
   * @param eventId
   * @throws Exception
   */
  public void removeEvent(String eventId) throws Exception ;
  
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Check exists of task
   * 3. Create new task
   * 4. Invalidate cache
   * @param username
   * @param task
   * @throws Exception
   */
  public void createTask(String username, Task task) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Check exists of task
   * 3. Create new task
   * 4. Invalidate cache
   * @param task
   * @throws Exception
   */
  public void createTask(Task task) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Check exists of task list in cache
   * 3. Get task list and return
   * @param username
   * @return task list
   * @throws Exception
   */
  public List<Task> getTaskByCalendar(String username, String calendarId) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node
   * 2. Check exists of task list in cache
   * 3. Get task list and return
   * @return task list
   * @throws Exception
   */
  public List<Task> getTaskByCalendar(String calendarId) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Get Task and return Task object
   * @param username
   * @param taskId
   * @return Task
   * @throws Exception
   */
  public Task getTask(String username, String taskId) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node
   * 2. Get Task and return Task object
   * @param taskId
   * @return Task
   * @throws Exception
   */
  public Task getTask(String taskId) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Get Task and update data
   * 3. Invalidate cache
   * @param username
   * @param Task
   * @throws Exception
   */
  public void updateTask(String username, Task task) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node
   * 2. Get Task and update data
   * 3. Invalidate cache
   * @param Task
   * @throws Exception
   */
  public void updateTask(Task task) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Remove task
   * 3. Invalidate cache
   * @param username
   * @param taskId
   * @throws Exception
   */
  public void removeTask(String username, String taskId) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node
   * 2. Remove task
   * 3. Invalidate cache
   * @param taskId
   * @throws Exception
   */
  public void removeTask(String taskId) throws Exception ;
  
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Check exists of group
   * 3. Create new group
   * 4. Invalidate cache
   * @param username
   * @param group
   * @throws Exception
   */
  public void createCalendarCategory(String username, CalendarCategory calendarCategory) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Check exists of group list in cache
   * 3. Get group list and return
   * @param username
   * @return group list
   * @throws Exception
   */
  public List<CalendarCategory> getCalendarCategories(String username) throws Exception ;
  
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Get Group and update data
   * 3. Invalidate cache
   * @param username
   * @param CalendarCategory
   * @throws Exception
   */
  public void updateCalendarCategory(String username, CalendarCategory calendarCategory) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Remove group
   * 3. Invalidate cache
   * @param username
   * @param calendarCategoryId
   * @throws Exception
   */
  public void removeCalendarCategory(String username, String calendarCategoryId) throws Exception ;
  
  
  
}
