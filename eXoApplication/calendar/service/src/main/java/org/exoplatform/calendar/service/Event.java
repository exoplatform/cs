/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public class Event {
  
  private String id ;
  private String name ;
  private String location ;
  private String description ;
  private String eventCategoryId ;
  private String calendarId ;
  private Date fromDateTime ;
  private Date toDateTime ;
  private String eventType ;
  private String priority ;
  private boolean isPrivate = true ;
  private String eventState ;
  private String[] invitation ;
  private List<Reminder> reminders ;
  private Map<String, String> properties ;
  
  
   
  public String getId() { return id; }
  public void   setId(String id) { this.id = id ;}

  public String getName() { return name; }
  public void   setName(String name) { this.name = name; }

  public String getDescription() { return description; }
  public void   setDescription(String description) { this.description = description; }

  public boolean isPrivate() { return isPrivate; }
  public void    setPrivate(boolean isPrivate) { this.isPrivate = isPrivate; }

  public String getEventCategoryId() { return eventCategoryId; }
  public void   setEventCategoryId(String eventCategoryId) { this.eventCategoryId = eventCategoryId;}

  public String getCalendarId() { return calendarId; }
  public void   setCalendarId(String calendarId) { this.calendarId = calendarId; }

  public Date getFromDateTime() { return fromDateTime ; }
  public void setFromDateTime(Date fromDateTime) { this.fromDateTime = fromDateTime; }

  public Date getToDateTime() { return toDateTime; }
  public void setToDateTime(Date toDateTime) { this.toDateTime = toDateTime; }

  public String getLocation() { return location; }
  public void   setLocation(String location) { this.location = location; }
  
  public String getEventState() { return eventState; }
  public void   setEventState(String eventState) { this.eventState = eventState; }
  
  public String getEventType() { return eventType ; }
  public void   setEventType(String eventType) { this.eventType = eventType ; }
  
  public String getPriority() { return priority ; }
  public void   setPriority(String priority) { this.priority = priority ; }
  
  public String[] getInvitation() { return invitation; }
  public void     setInvitation(String[] invitation) { this.invitation = invitation; }
    
  public List<Reminder> getReminders() { return reminders ; }
  public void           setReminders(List<Reminder> rm) { this.reminders = rm ; }
}
