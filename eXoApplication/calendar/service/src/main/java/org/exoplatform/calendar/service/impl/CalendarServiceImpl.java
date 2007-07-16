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
import org.exoplatform.calendar.service.Task;
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
  public void createCalendar(String username, Calendar calendar) throws Exception {
    storage_.createCalendar(username, calendar) ;
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
  
  public void removeCalendar(String username, String calendarId) throws Exception {
    storage_.removeCalendar(username, calendarId) ;    
  }

  public void removeCalendar(String calendarId) throws Exception {
    storage_.removeCalendar(calendarId) ;
  }
  
  public void updateCalendar(String username, Calendar calendar) throws Exception {
    storage_.updateCalendar(username, calendar) ; 
  }
  
  public void createCalendarCategory(String username, CalendarCategory calendarCategory) throws Exception {
    // TODO Auto-generated method stub
    
  }

  public void createEvent(String username, Event event) throws Exception {
    // TODO Auto-generated method stub
    
  }

  public void createEvent(Event event) throws Exception {
    // TODO Auto-generated method stub
    
  }

  public void createEventCategory(String username, EventCategory category) throws Exception {
    // TODO Auto-generated method stub
    
  }

  public void createTask(String username, Task task) throws Exception {
    // TODO Auto-generated method stub
    
  }

  public void createTask(Task task) throws Exception {
    // TODO Auto-generated method stub
    
  }

  public List<CalendarCategory> getCalendarCategories(String username) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public Event getEvent(String username, String eventId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public Event getEvent(String eventId) throws Exception {
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

  public List<Event> getEventByCategory(String username, String categoryId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public List<Event> getEventByCategory(String categoryId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public List<EventCategory> getEventCategories(String username) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public EventCategory getEventCategory(String username, String categoryId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public Task getTask(String username, String taskId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public Task getTask(String taskId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public List<Task> getTaskByCalendar(String username, String calendarId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public List<Task> getTaskByCalendar(String calendarId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public void removeCalendarCategory(String username, String calendarCategoryId) throws Exception {
    // TODO Auto-generated method stub
    
  }

  public void removeEvent(String username, String eventId) throws Exception {
    // TODO Auto-generated method stub
    
  }

  public void removeEvent(String eventId) throws Exception {
    // TODO Auto-generated method stub
    
  }

  public void removeEventCategory(String username, String categoryId) throws Exception {
    // TODO Auto-generated method stub
    
  }

  public void removeTask(String username, String taskId) throws Exception {
    // TODO Auto-generated method stub
    
  }

  public void removeTask(String taskId) throws Exception {
    // TODO Auto-generated method stub
    
  }

  public void updateCalendarCategory(String username, CalendarCategory calendarCategory) throws Exception {
    // TODO Auto-generated method stub
    
  }

  public void updateEvent(String username, Event event) throws Exception {
    // TODO Auto-generated method stub
    
  }

  public void updateEvent(Event event) throws Exception {
    // TODO Auto-generated method stub
    
  }

  public void updateEventCategory(String username, EventCategory category) throws Exception {
    // TODO Auto-generated method stub
    
  }

  public void updateTask(String username, Task task) throws Exception {
    // TODO Auto-generated method stub
    
  }

  public void updateTask(Task task) throws Exception {
    // TODO Auto-generated method stub
    
  }

  

}
