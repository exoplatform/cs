/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.service;

import java.util.Date;
import java.util.Map;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public class Event {
  
  private String id_ ;
  private String name_ ;
  private String location_ ;
  private String description_ ;
  private String categoryId_ ;
  private String calendarId_ ;
  private Date fromDateTime_ ;
  private Date toDateTime_ ;
   
  private String repeat_ ;
  private String reminder_ ;
  private String alarmBefore_ ; 
  
  private String eventType_ ;
  
  //Reminder
  private boolean isPrivate_ = true ;
  
  private String eventState_ ;
  
  private String[] invitation_ ;
 
  private Map<String, String> properties_ ;
  
  public String getId() { return id_; }
  public void setId(String id) { this.id_ = id ;}

  public String getName() { return name_; }
  public void setName(String name) { this.name_ = name; }

  public String getDescription() { return description_; }
  public void setDescription(String description_) { this.description_ = description_; }

  public boolean isPrivate() { return isPrivate_; }
  public void setPrivate(boolean isPrivate) { this.isPrivate_ = isPrivate; }

  public String getEventCategoryId() { return categoryId_; }
  public void setCategoryId(String categoryId) { this.categoryId_ = categoryId;}

  public String getCalendarId() { return calendarId_; }
  public void setCalendarId(String calendarId_) { this.calendarId_ = calendarId_; }

  public Date getFromDateTime() { return fromDateTime_ ; }
  public void setFromDateTime(Date fromDateTime) { this.fromDateTime_ = fromDateTime; }

  public Date getToDateTime() { return toDateTime_; }
  public void setToDateTime(Date toDateTime_) { this.toDateTime_ = toDateTime_; }

  public void setRepeat(String repeat_) { this.repeat_ = repeat_; }
  public String getRepeat() { return repeat_; }

  public void setLocation(String location_) { this.location_ = location_; }
  public String getLocation() { return location_; }

  public void setReminder(String reminder_) { this.reminder_ = reminder_; }
  public String getReminder() { return reminder_; }

  public void setAlarmBefore(String alarmBefore_) { this.alarmBefore_ = alarmBefore_; }
  public String getAlarmBefore() { return alarmBefore_; }

  public String getEventState() { return eventState_; }
  public void setEventState(String eventState_) { this.eventState_ = eventState_; }
  
  public void     setInvitation(String[] invitation) { this.invitation_ = invitation; }
  public String[] getInvitation() { return invitation_; }  
  
}
