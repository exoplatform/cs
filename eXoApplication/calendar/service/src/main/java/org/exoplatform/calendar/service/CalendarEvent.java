/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public class CalendarEvent {
  final public static String TYPE_EVENT = "Event".intern() ;
  final public static String TYPE_TASK = "Task".intern() ;
  final public static String TYPE_JOURNAL = "journal".intern() ;
  
  final public static String TENTATIVE = "tentative".intern() ;
  final public static String CONFIRMED = "confirmed".intern() ;
  final public static String CANCELLED = "canceled".intern() ;
  
  final public static String NEEDS_ACTION = "needs-action".intern() ;
  final public static String COMPLETED = "completed".intern() ;
  final public static String IN_PROCESS = "in-process".intern() ;
  
  final public static String PRIORITY_LOW = "low".intern() ;
  final public static String PRIORITY_NORMAL = "normal".intern() ;
  final public static String PRIORITY_HIGHT = "hight".intern() ;
  final public static String[] PRIORITY = {PRIORITY_HIGHT, PRIORITY_NORMAL, PRIORITY_LOW} ;
  
  final public static String DRAFT = "draft".intern() ;
  final public static String FINAL = "final".intern() ;
  
  final public static String[] EVENT_STATUS = {TENTATIVE, CONFIRMED, CANCELLED} ;
  final public static String[] TASK_STATUS = {NEEDS_ACTION, COMPLETED, IN_PROCESS, CANCELLED} ;
  final public static String[] JOURNAL_STATUS = {DRAFT, FINAL, CANCELLED} ;
  
  final public static String RP_NOREPEAT = "norepeat".intern() ;
  final public static String RP_DAILY = "daily".intern() ;
  final public static String RP_WEEKLY = "weekly".intern() ;
  final public static String RP_MONTHY = "monthy".intern() ;
  final public static String RP_YEARLY = "yearly".intern() ;
  final public static String RP_WEEKEND = "weekend".intern() ;
  final public static String RP_WORKINGDAYS = "workingdays".intern() ;
  
  final public static String[] REPEATTYPES = {RP_NOREPEAT, RP_DAILY, RP_WORKINGDAYS, RP_WEEKEND, RP_WEEKLY, RP_MONTHY, RP_YEARLY} ;
  
  private String id ;
  private String summary ;
  private String location ;
  private String description ;
  private String eventCategoryId ;
  private String calendarId ;
  private String repeatType ;
  private Date fromDateTime ;
  private Date toDateTime ;
  private Date completedDateTime ;
  private String taskDelegator  ;
  // value of eventType: event, task, journal(?)
  private String eventType ;
  private String priority ;
  //values: LOW, NORMAL, HIGHT
  private boolean isPrivate = true ;
  private String eventState ;
  //status for event: TENTATIVE, CONFIRMED, CANCELLED. 
  //       for task:  NEEDS-ACTION, COMPLETED, IN-PROCESS, CANCELLED
  //       for journal: DRAFT, FINAL, CANCELLED
  private String status = ""; 
  private String calType = "0";
  private String[] invitation ;
  private String[] participant ;
  private List<Reminder> reminders ;
  private List<Attachment> attachment ;
  private Map<String, String> properties ;
  
  public CalendarEvent() {
    id = "Event" + IdGenerator.generate() ;
  }
   
  public String getId() { return id; }
  public void   setId(String id) { this.id = id ;}

  public String getSummary() { return summary; }
  public void   setSummary(String sum) { this.summary = sum; }

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
  
  public Date getCompletedDateTime() { return completedDateTime; }
  public void setCompletedDateTime(Date completedDateTime) { this.completedDateTime = completedDateTime; }
  
  public String getLocation() { return location; }
  public void   setLocation(String location) { this.location = location; }
  
  public String getEventState() { return eventState; }
  public void   setEventState(String eventState) { this.eventState = eventState; }
  
  public String getStatus() { return status; }
  public void   setStatus(String status) { this.status = status; }
  
  public String getEventType() { return eventType ; }
  public void   setEventType(String eventType) { this.eventType = eventType ; }
  
  public String getPriority() { return priority ; }
  public void   setPriority(String priority) { this.priority = priority ; }
  
  public String[] getInvitation() { return invitation; }
  public void     setInvitation(String[] invitation) { this.invitation = invitation; }
  
  public List<Reminder> getReminders() { return reminders ; }
  public void setReminders(List<Reminder> rm) { this.reminders = rm ; }

  public void setRepeatType(String repeatType) {
    this.repeatType = repeatType;
  }

  public String getRepeatType() {
    return repeatType;
  }
  
  public List<Attachment> getAttachment() {return attachment ;}
  public void setAttachment(List<Attachment> list) {attachment = list ;}

  public void setParticipant(String[] participant) {
    this.participant = participant;
  }

  public String[] getParticipant() {
    return participant;
  }

  public void setTaskDelegator(String taskDelegator) {
    this.taskDelegator = taskDelegator;
  }

  public String getTaskDelegator() {
    return taskDelegator;
  }

  public void setCalType(String calType) {
    this.calType = calType;
  }

  public String getCalType() {
    return calType;
  }
}
