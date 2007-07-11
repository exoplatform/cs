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
  public void createCategory(String username, Category category) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Check exists of categories list in cache
   * 3. Return category list and put category list to cache if not exists
   * @param username
   * @return Category list
   * @throws Exception
   */  
  public List<Category> getCategories(String username) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Get Category and return Category object
   * @param username
   * @param categoryId
   * @return Category
   * @throws Exception
   */
  public Category getCategory(String username, String categoryId) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Get Category and update data
   * 3. Invalidate cache
   * @param username
   * @param category
   * @throws Exception
   */
  public void updateCategory(String username, Category category) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Remove category
   * 3. Invalidate cache
   * @param username
   * @param categoryId
   * @throws Exception
   */
  public void removeCategory(String username, String categoryId) throws Exception ;
  
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Check exists of calendar
   * 3. Create new calendar
   * 4. Invalidate cache
   * @param username
   * @param calendar
   * @throws Exception
   */
  public void createCalendar(String username, Calendar calendar) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Check exists of calendar list in cache
   * 3. Get calendar list and return
   * @param username
   * @return Calendar list
   * @throws Exception
   */
  public List<Calendar> getCalendars(String username) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Get calendar and return Category object
   * @param username
   * @param calendarId
   * @return Calendar
   * @throws Exception
   */
  public Calendar getCalendar(String username, String calendarId) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Get Calendar and update data
   * 3. Invalidate cache
   * @param username
   * @param Calendar
   * @throws Exception
   */
  public void updateCalendar(String username, Calendar calendar) throws Exception ;
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
   * 1. Get calendar service root node by current user
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
   * 1. Get calendar service root node by current user
   * 2. Check exists of event list in cache
   * 3. Get event list and return
   * @param username
   * @return event list
   * @throws Exception
   */
  public List<Event> getEventByCategory(String username, String categoryId) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Check exists of event list in cache
   * 3. Get event list and return
   * @param username
   * @return event list
   * @throws Exception
   */
  public List<Event> getEventByCalendar(String username, String calendarId) throws Exception ;
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
   * 2. Get Event and update data
   * 3. Invalidate cache
   * @param username
   * @param Event
   * @throws Exception
   */
  public void updateEvent(String username, Event event) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Remove event
   * 3. Invalidate cache
   * @param username
   * @param eventId
   * @throws Exception
   */
  public void removeEvent(String username, Event event) throws Exception ;
  
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
   * 2. Check exists of task list in cache
   * 3. Get task list and return
   * @param username
   * @return task list
   * @throws Exception
   */
  public List<Task> getTaskByCalendar(String username, String calendarId) throws Exception ;
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
   * 1. Get calendar service root node by current user
   * 2. Remove task
   * 3. Invalidate cache
   * @param username
   * @param taskId
   * @throws Exception
   */
  public void removeTask(String username, Task task) throws Exception ;
  
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
  public void createGroup(String username, Group group) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Check exists of group list in cache
   * 3. Get group list and return
   * @param username
   * @return group list
   * @throws Exception
   */
  public List<Group> getGroups(String username, String groupId) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Get Group and return Category object
   * @param username
   * @param groupId
   * @return Group
   * @throws Exception
   */
  public Group getGroup(String username, String groupId) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Get Group and update data
   * 3. Invalidate cache
   * @param username
   * @param Group
   * @throws Exception
   */
  public void updateGroup(String username, Group group) throws Exception ;
  /**
   * This method should:
   * 1. Get calendar service root node by current user
   * 2. Remove group
   * 3. Invalidate cache
   * @param username
   * @param groupId
   * @throws Exception
   */
  public void removeGroup(String username, Group group) throws Exception ;
  
  
  
}
